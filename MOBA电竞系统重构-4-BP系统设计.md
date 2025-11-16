# MOBA电竞系统 - BP系统设计

## 一、BP系统概述

**BP**（Ban/Pick）是MOBA游戏赛前最重要的策略环节，双方轮流禁用（Ban）和选择（Pick）英雄。

### 核心要素
- 每队3个BAN位（共6个）
- 每队5个PICK位（共10个）
- 蓝方先Pick，红方后Pick（红方优势）
- 已被Ban/Pick的英雄不能再选

---

## 二、BP流程设计

### 2.1 标准BO5 BP流程

```kotlin
data class BPPhase(
    val phaseNumber: Int,
    val action: BPAction,
    val side: TeamSide,
    val timeLimit: Int = 30  // 秒
)

enum class BPAction {
    BAN,
    PICK
}

enum class TeamSide {
    BLUE,  // 蓝方
    RED    // 红方
}

// BO5标准流程（共16步）
val standardBPSequence = listOf(
    // 第一轮BAN（各3个）
    BPPhase(1, BPAction.BAN, TeamSide.BLUE),
    BPPhase(2, BPAction.BAN, TeamSide.RED),
    BPPhase(3, BPAction.BAN, TeamSide.BLUE),
    BPPhase(4, BPAction.BAN, TeamSide.RED),
    BPPhase(5, BPAction.BAN, TeamSide.BLUE),
    BPPhase(6, BPAction.BAN, TeamSide.RED),
    
    // 第一轮PICK（各2个）
    BPPhase(7, BPAction.PICK, TeamSide.BLUE),
    BPPhase(8, BPAction.PICK, TeamSide.RED),
    BPPhase(9, BPAction.PICK, TeamSide.RED),
    BPPhase(10, BPAction.PICK, TeamSide.BLUE),
    
    // 第二轮PICK（各3个）
    BPPhase(11, BPAction.PICK, TeamSide.RED),
    BPPhase(12, BPAction.PICK, TeamSide.BLUE),
    BPPhase(13, BPAction.PICK, TeamSide.BLUE),
    BPPhase(14, BPAction.PICK, TeamSide.RED),
    BPPhase(15, BPAction.PICK, TeamSide.RED),
    BPPhase(16, BPAction.PICK, TeamSide.BLUE)
)
```

### 2.2 BP数据结构

```kotlin
data class BPSession(
    val matchId: String,
    val blueTeam: Team,
    val redTeam: Team,
    val currentPhase: Int = 0,
    val blueBans: MutableList<String> = mutableListOf(),  // 英雄ID
    val redBans: MutableList<String> = mutableListOf(),
    val bluePicks: MutableList<PickedHero> = mutableListOf(),
    val redPicks: MutableList<PickedHero> = mutableListOf(),
    val isCompleted: Boolean = false
)

data class PickedHero(
    val heroId: String,
    val playerId: String,
    val position: HeroPosition,
    val proficiency: Int  // 选手对该英雄的熟练度
)
```

---

## 三、AI BP策略

### 3.1 策略接口

```kotlin
interface BPStrategy {
    fun selectBan(
        session: BPSession,
        availableHeroes: List<MobaHero>,
        enemyTeam: Team,
        myTeam: Team
    ): MobaHero
    
    fun selectPick(
        session: BPSession,
        availableHeroes: List<MobaHero>,
        position: HeroPosition,
        enemyTeam: Team,
        myTeam: Team
    ): MobaHero
}
```

### 3.2 标准BP策略实现

```kotlin
class StandardBPStrategy : BPStrategy {
    
    override fun selectBan(
        session: BPSession,
        availableHeroes: List<MobaHero>,
        enemyTeam: Team,
        myTeam: Team
    ): MobaHero {
        // BAN策略优先级：
        // 1. 对方选手的招牌英雄（proficiency > 85）
        // 2. 当前版本OP英雄（banRate > 20%）
        // 3. 克制我方体系的英雄
        
        val targetHeroes = mutableListOf<Pair<MobaHero, Double>>()
        
        availableHeroes.forEach { hero ->
            var banValue = 0.0
            
            // 检查是否是对方招牌英雄
            enemyTeam.players.forEach { player ->
                val mastery = player.heroPool.find { it.heroId == hero.id }
                if (mastery != null && mastery.proficiency > 85) {
                    banValue += 100.0
                }
            }
            
            // 版本强度
            if (hero.banRate > 20.0) {
                banValue += hero.banRate * 2
            }
            
            // 克制关系
            session.bluePicks.forEach { pick ->
                if (hero.counters.contains(pick.heroId)) {
                    banValue += 30.0
                }
            }
            
            if (banValue > 0) {
                targetHeroes.add(hero to banValue)
            }
        }
        
        // 返回价值最高的
        return targetHeroes.maxByOrNull { it.second }?.first
            ?: availableHeroes.random()
    }
    
    override fun selectPick(
        session: BPSession,
        availableHeroes: List<MobaHero>,
        position: HeroPosition,
        enemyTeam: Team,
        myTeam: Team
    ): MobaHero {
        // PICK策略优先级：
        // 1. 选择该位置选手擅长的英雄（proficiency > 70）
        // 2. 考虑阵容平衡（前排、输出、控制）
        // 3. 选择克制对方已选英雄的英雄
        
        val player = myTeam.players.find { it.position == position }
            ?: return availableHeroes.first { it.position == position }
        
        val candidateHeroes = availableHeroes.filter { it.position == position }
        val scoredHeroes = mutableListOf<Pair<MobaHero, Double>>()
        
        candidateHeroes.forEach { hero ->
            var pickValue = 0.0
            
            // 选手熟练度
            val mastery = player.heroPool.find { it.heroId == hero.id }
            if (mastery != null) {
                pickValue += mastery.proficiency * 1.5
            }
            
            // 克制关系
            session.redPicks.forEach { enemyPick ->
                if (hero.counters.contains(enemyPick.heroId)) {
                    pickValue += 50.0
                }
                if (hero.counteredBy.contains(enemyPick.heroId)) {
                    pickValue -= 30.0
                }
            }
            
            // 阵容平衡
            val currentComp = analyzeComposition(session.bluePicks)
            val balanceScore = calculateBalanceScore(currentComp, hero)
            pickValue += balanceScore
            
            scoredHeroes.add(hero to pickValue)
        }
        
        return scoredHeroes.maxByOrNull { it.second }?.first
            ?: candidateHeroes.random()
    }
    
    // 阵容分析
    private fun analyzeComposition(picks: List<PickedHero>): CompositionAnalysis {
        val heroes = picks.mapNotNull { HeroManager.getHeroById(it.heroId) }
        
        return CompositionAnalysis(
            totalDamage = heroes.sumOf { it.strength.damage },
            totalTankiness = heroes.sumOf { it.strength.tankiness },
            totalControl = heroes.sumOf { it.strength.control },
            totalMobility = heroes.sumOf { it.strength.mobility }
        )
    }
    
    // 平衡性评分
    private fun calculateBalanceScore(
        currentComp: CompositionAnalysis,
        newHero: MobaHero
    ): Double {
        var score = 0.0
        
        // 缺少前排时，坦克英雄加分
        if (currentComp.totalTankiness < 150 && newHero.strength.tankiness > 70) {
            score += 40.0
        }
        
        // 缺少伤害时，输出英雄加分
        if (currentComp.totalDamage < 200 && newHero.strength.damage > 80) {
            score += 30.0
        }
        
        // 缺少控制时，控制英雄加分
        if (currentComp.totalControl < 150 && newHero.strength.control > 60) {
            score += 25.0
        }
        
        return score
    }
}

data class CompositionAnalysis(
    val totalDamage: Int,
    val totalTankiness: Int,
    val totalControl: Int,
    val totalMobility: Int
)
```

### 3.3 高级BP策略

```kotlin
class AdvancedBPStrategy : BPStrategy {
    // 考虑战术体系
    enum class TacticalSystem(val priority: Map<HeroType, Int>) {
        PROTECT_ADC(mapOf(
            HeroType.TANK to 3,
            HeroType.SUPPORT to 3,
            HeroType.MARKSMAN to 5
        )),
        ENGAGE_COMP(mapOf(
            HeroType.TANK to 5,
            HeroType.FIGHTER to 4,
            HeroType.ASSASSIN to 3
        )),
        POKE_COMP(mapOf(
            HeroType.MAGE to 5,
            HeroType.MARKSMAN to 4,
            HeroType.SUPPORT to 3
        )),
        SPLIT_PUSH(mapOf(
            HeroType.FIGHTER to 5,
            HeroType.ASSASSIN to 4
        ))
    }
    
    // 根据队伍风格选择战术体系
    private fun selectTacticalSystem(team: Team): TacticalSystem {
        val avgMechanics = team.players.map { it.attributes.mechanics }.average()
        val avgTeamwork = team.players.map { it.attributes.teamwork }.average()
        
        return when {
            avgTeamwork > avgMechanics -> TacticalSystem.PROTECT_ADC
            avgMechanics > 80 -> TacticalSystem.SPLIT_PUSH
            else -> TacticalSystem.ENGAGE_COMP
        }
    }
    
    override fun selectPick(
        session: BPSession,
        availableHeroes: List<MobaHero>,
        position: HeroPosition,
        enemyTeam: Team,
        myTeam: Team
    ): MobaHero {
        val system = selectTacticalSystem(myTeam)
        
        // 根据战术体系调整英雄类型权重
        // ... 实现细节
        
        return availableHeroes.first()  // 简化实现
    }
}
```

---

## 四、阵容评分系统

### 4.1 阵容质量评估

```kotlin
data class TeamComposition(
    val heroes: List<MobaHero>,
    val players: List<EsportsPlayer>,
    val scores: CompositionScores
)

data class CompositionScores(
    val damage: Int,        // 伤害能力（0-100）
    val tankiness: Int,     // 坦度（0-100）
    val control: Int,       // 控制能力（0-100）
    val mobility: Int,      // 机动性（0-100）
    val synergy: Int,       // 协同性（0-100）
    val overall: Int        // 综合评分（0-100）
)

object CompositionAnalyzer {
    fun analyzeComposition(
        heroes: List<MobaHero>,
        players: List<EsportsPlayer>
    ): TeamComposition {
        val scores = calculateScores(heroes, players)
        return TeamComposition(heroes, players, scores)
    }
    
    private fun calculateScores(
        heroes: List<MobaHero>,
        players: List<EsportsPlayer>
    ): CompositionScores {
        // 基础属性评分
        val damage = heroes.sumOf { it.strength.damage } / 5
        val tankiness = heroes.sumOf { it.strength.tankiness } / 5
        val control = heroes.sumOf { it.strength.control } / 5
        val mobility = heroes.sumOf { it.strength.mobility } / 5
        
        // 协同性评分
        val synergy = calculateSynergy(heroes)
        
        // 选手熟练度影响
        val proficiencyBonus = calculateProficiencyBonus(heroes, players)
        
        // 综合评分
        val overall = ((damage + tankiness + control + mobility + synergy) / 5.0 * 
                      (1.0 + proficiencyBonus * 0.2)).toInt().coerceIn(0, 100)
        
        return CompositionScores(
            damage = damage.coerceIn(0, 100),
            tankiness = tankiness.coerceIn(0, 100),
            control = control.coerceIn(0, 100),
            mobility = mobility.coerceIn(0, 100),
            synergy = synergy.coerceIn(0, 100),
            overall = overall
        )
    }
    
    private fun calculateSynergy(heroes: List<MobaHero>): Int {
        var synergy = 50  // 基础50分
        
        // 位置完整性（5个不同位置）
        if (heroes.map { it.position }.distinct().size == 5) {
            synergy += 15
        }
        
        // 类型平衡
        val types = heroes.map { it.type }
        val hasTank = types.contains(HeroType.TANK)
        val hasDamage = types.any { it == HeroType.MAGE || it == HeroType.MARKSMAN }
        val hasControl = types.any { it == HeroType.SUPPORT || it == HeroType.TANK }
        
        if (hasTank && hasDamage && hasControl) {
            synergy += 20
        }
        
        // 克制关系协同
        var counterSynergy = 0
        heroes.forEach { hero1 ->
            heroes.forEach { hero2 ->
                if (hero1.id != hero2.id && hero1.counters.any { it in hero2.counters }) {
                    counterSynergy += 5
                }
            }
        }
        synergy += counterSynergy.coerceAtMost(15)
        
        return synergy
    }
    
    private fun calculateProficiencyBonus(
        heroes: List<MobaHero>,
        players: List<EsportsPlayer>
    ): Double {
        var totalProficiency = 0.0
        var count = 0
        
        heroes.forEach { hero ->
            players.forEach { player ->
                val mastery = player.heroPool.find { it.heroId == hero.id }
                if (mastery != null) {
                    totalProficiency += mastery.proficiency
                    count++
                }
            }
        }
        
        return if (count > 0) totalProficiency / count / 100.0 else 0.5
    }
}
```

### 4.2 阵容类型识别

```kotlin
enum class CompositionType(
    val displayName: String,
    val description: String
) {
    PROTECT_ADC(
        "四保一",
        "围绕ADC核心，提供保护和控制"
    ),
    ENGAGE(
        "突进阵容",
        "多个突进英雄，快速开团"
    ),
    POKE(
        "拉扯阵容",
        "远程消耗，风筝对手"
    ),
    SPLIT_PUSH(
        "分推阵容",
        "单带能力强，牵制对手"
    ),
    TEAMFIGHT(
        "团战阵容",
        "大招配合，团战能力强"
    ),
    PICK(
        "抓单阵容",
        "单点控制，快速秒人"
    ),
    BALANCED(
        "均衡阵容",
        "没有明显短板"
    )
}

object CompositionClassifier {
    fun classifyComposition(comp: TeamComposition): CompositionType {
        val scores = comp.scores
        val heroes = comp.heroes
        
        // 分析特征
        val hasStrongADC = heroes.any { 
            it.type == HeroType.MARKSMAN && it.strength.damage > 85 
        }
        val tankCount = heroes.count { it.type == HeroType.TANK }
        val supportCount = heroes.count { it.type == HeroType.SUPPORT }
        val assassinCount = heroes.count { it.type == HeroType.ASSASSIN }
        
        return when {
            hasStrongADC && (tankCount + supportCount >= 3) -> 
                CompositionType.PROTECT_ADC
            
            scores.mobility > 75 && assassinCount >= 2 -> 
                CompositionType.ENGAGE
            
            scores.damage > 80 && scores.mobility > 70 -> 
                CompositionType.POKE
            
            heroes.any { it.type == HeroType.FIGHTER && it.strength.damage > 80 } -> 
                CompositionType.SPLIT_PUSH
            
            scores.control > 75 && scores.synergy > 75 -> 
                CompositionType.TEAMFIGHT
            
            scores.control > 80 && assassinCount >= 1 -> 
                CompositionType.PICK
            
            else -> CompositionType.BALANCED
        }
    }
}
```

---

## 五、BP管理器

```kotlin
object BPManager {
    fun startBPSession(match: Match): BPSession {
        return BPSession(
            matchId = match.id,
            blueTeam = match.blueTeam,
            redTeam = match.redTeam
        )
    }
    
    fun executeAIBP(
        session: BPSession,
        strategy: BPStrategy = StandardBPStrategy()
    ): BPSession {
        val sequence = standardBPSequence
        var currentSession = session
        
        sequence.forEach { phase ->
            val availableHeroes = getAvailableHeroes(currentSession)
            
            when (phase.action) {
                BPAction.BAN -> {
                    val team = if (phase.side == TeamSide.BLUE) 
                        currentSession.blueTeam else currentSession.redTeam
                    val enemyTeam = if (phase.side == TeamSide.BLUE) 
                        currentSession.redTeam else currentSession.blueTeam
                    
                    val bannedHero = strategy.selectBan(
                        currentSession, availableHeroes, enemyTeam, team
                    )
                    
                    if (phase.side == TeamSide.BLUE) {
                        currentSession.blueBans.add(bannedHero.id)
                    } else {
                        currentSession.redBans.add(bannedHero.id)
                    }
                }
                
                BPAction.PICK -> {
                    val team = if (phase.side == TeamSide.BLUE) 
                        currentSession.blueTeam else currentSession.redTeam
                    val enemyTeam = if (phase.side == TeamSide.BLUE) 
                        currentSession.redTeam else currentSession.blueTeam
                    
                    val position = determineNextPosition(currentSession, phase.side)
                    val player = team.players.find { it.position == position }!!
                    
                    val pickedHeroObj = strategy.selectPick(
                        currentSession, availableHeroes, position, enemyTeam, team
                    )
                    
                    val pickedHero = PickedHero(
                        heroId = pickedHeroObj.id,
                        playerId = player.id,
                        position = position,
                        proficiency = player.heroPool.find { 
                            it.heroId == pickedHeroObj.id 
                        }?.proficiency ?: 50
                    )
                    
                    if (phase.side == TeamSide.BLUE) {
                        currentSession.bluePicks.add(pickedHero)
                    } else {
                        currentSession.redPicks.add(pickedHero)
                    }
                }
            }
            
            currentSession = currentSession.copy(
                currentPhase = currentSession.currentPhase + 1
            )
        }
        
        return currentSession.copy(isCompleted = true)
    }
    
    private fun getAvailableHeroes(session: BPSession): List<MobaHero> {
        val bannedIds = session.blueBans + session.redBans
        val pickedIds = session.bluePicks.map { it.heroId } + 
                       session.redPicks.map { it.heroId }
        val unavailableIds = bannedIds + pickedIds
        
        return HeroManager.getAllHeroes().filter { it.id !in unavailableIds }
    }
    
    private fun determineNextPosition(
        session: BPSession,
        side: TeamSide
    ): HeroPosition {
        val picks = if (side == TeamSide.BLUE) 
            session.bluePicks else session.redPicks
        
        val pickedPositions = picks.map { it.position }.toSet()
        val allPositions = HeroPosition.values().toList()
        
        return allPositions.first { it !in pickedPositions }
    }
}
```

---

## 六、BP界面设计

### 6.1 BP进行中界面

```
┌─────────────────────────────────────────┐
│  ⚔️ BP阶段 (第8步/16步)                  │
├─────────────────────────────────────────┤
│  蓝方 BAN：                              │
│  [🔒铁山] [🔒暗影刃] [🔒时空法师]        │
│                                         │
│  红方 BAN：                              │
│  [🔒寒冰射手] [🔒狂野猎手] [🔒烈焰法师]  │
├─────────────────────────────────────────┤
│  蓝方阵容：                              │
│  ┌─────┐  ┌─────┐                      │
│  │ TOP │  │ JUG │                      │
│  │巨石 │  │     │                      │
│  └─────┘  └─────┘                      │
│                                         │
│  红方阵容：                              │
│  ┌─────┐  ┌─────┐                      │
│  │ TOP │  │ JUG │                      │
│  │剑圣 │  │巨龙 │                      │
│  └─────┘  └─────┘                      │
├─────────────────────────────────────────┤
│  当前操作：红方选择打野位 ⏳ 25秒        │
│                                         │
│  [等待AI选择...]                         │
└─────────────────────────────────────────┘
```

### 6.2 BP完成界面

```
┌─────────────────────────────────────────┐
│  ✅ BP完成！                             │
├─────────────────────────────────────────┤
│  蓝方阵容评分：82/100 ⭐⭐⭐⭐           │
│  类型：突进阵容                          │
│  优势：机动性强、开团能力                │
│  劣势：缺少后排保护                      │
│                                         │
│  红方阵容评分：78/100 ⭐⭐⭐             │
│  类型：均衡阵容                          │
│  优势：全面发展、无明显短板              │
│  劣势：缺少核心Carry点                   │
│                                         │
│  预测胜率：蓝方 55% vs 红方 45%         │
│                                         │
│  [开始比赛]                              │
└─────────────────────────────────────────┘
```

---

## 七、测试用例

- [ ] BP流程：验证16步完整执行
- [ ] BAN策略：验证招牌英雄被Ban
- [ ] PICK策略：验证选手熟练度影响
- [ ] 阵容评分：验证评分合理性
- [ ] 克制关系：验证Counter影响BP选择
- [ ] 玩家BP：验证手动选择功能
