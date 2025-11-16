# MOBA电竞系统 - 选手系统设计

## 一、选手品质等级

### 1.1 品质定义

```kotlin
enum class PlayerRarity(
    val displayName: String,
    val color: Color,
    val emoji: String,
    val baseAttributeRange: IntRange,  // 基础属性范围
    val growthPotential: IntRange,     // 成长潜力（每年+1-10点）
    val signCost: Long,                // 签约费用
    val monthlySalary: Long,           // 月薪
    val probability: Double            // 青训营概率
) {
    C("C级", Color(0xFFBDBDBD), "⚪", 55..65, 1..3, 50_000, 10_000, 0.80),
    B("B级", Color(0xFF4CAF50), "🟢", 65..75, 3..5, 200_000, 30_000, 0.15),
    A("A级", Color(0xFF2196F3), "🔵", 75..85, 5..7, 800_000, 80_000, 0.04),
    S("S级", Color(0xFF9C27B0), "🟣", 85..92, 7..9, 3_000_000, 200_000, 0.009),
    SSR("SSR级", Color(0xFFFF9800), "🟠", 92..98, 9..10, 10_000_000, 500_000, 0.001)
}
```

### 1.2 品质特点对比

| 品质 | 基础属性 | 成长 | 签约费 | 月薪 | 特点 |
|------|----------|------|--------|------|------|
| **C级** | 55-65 | 1-3/年 | 5万 | 1万 | 新手选手，性价比高 |
| **B级** | 65-75 | 3-5/年 | 20万 | 3万 | 可培养，中期主力 |
| **A级** | 75-85 | 5-7/年 | 80万 | 8万 | 即战力，成长空间大 |
| **S级** | 85-92 | 7-9/年 | 300万 | 20万 | 顶尖选手，稀有 |
| **SSR级** | 92-98 | 9-10/年 | 1000万 | 50万 | 传奇选手，极稀有 |

---

## 二、选手属性系统

### 2.1 数据结构

```kotlin
data class EsportsPlayer(
    val id: String,
    val name: String,
    val rarity: PlayerRarity,
    val position: HeroPosition,
    val age: Int,                        // 年龄（16-30）
    val nationality: String,             // 国籍
    val attributes: PlayerAttributes,
    val heroPool: MutableList<HeroMastery>, // 英雄池
    val championHeroes: List<String>,    // 招牌英雄（最多5个）
    val careerStats: CareerStats,
    val contract: PlayerContract,
    val form: Int,                       // 状态（0-100）
    val morale: Int,                     // 士气（0-100）
    val stamina: Int,                    // 体力（0-100）
    val injury: InjuryStatus?,           // 伤病状态
    val personality: PlayerPersonality,   // 性格
    val achievements: List<Achievement>   // 成就
)

data class PlayerAttributes(
    val mechanics: Int,      // 操作（1-100）
    val awareness: Int,      // 意识（1-100）
    val teamwork: Int,       // 团队配合（1-100）
    val mentality: Int,      // 心态（1-100）
    val heroMastery: Int     // 英雄熟练度（1-100）
) {
    // 综合实力
    fun overallRating(): Int {
        return (mechanics * 0.3 + awareness * 0.25 + teamwork * 0.2 + 
                mentality * 0.15 + heroMastery * 0.1).toInt()
    }
}

data class HeroMastery(
    val heroId: String,
    val proficiency: Int,    // 熟练度（0-100）
    val gamesPlayed: Int,    // 使用场次
    val winRate: Double      // 胜率
)

data class CareerStats(
    val totalMatches: Int,
    val wins: Int,
    val kda: Double,         // KDA比率
    val mvpCount: Int,       // MVP次数
    val championships: List<ChampionshipRecord>,
    val peakElo: Int         // 历史最高分
) {
    fun winRate(): Double = if (totalMatches > 0) wins.toDouble() / totalMatches else 0.0
}

data class ChampionshipRecord(
    val tournamentId: String,
    val tournamentName: String,
    val year: Int,
    val placement: Int       // 名次
)

data class PlayerContract(
    val startDate: Date,
    val endDate: Date,
    val monthlySalary: Long,
    val buyoutClause: Long,  // 违约金
    val bonusClause: ContractBonus // 奖金条款
)

data class ContractBonus(
    val championshipBonus: Long,  // 冠军奖金
    val mvpBonus: Long,           // MVP奖金
    val performanceBonus: Long    // 表现奖金
)

enum class InjuryStatus(val displayName: String, val recoveryDays: Int) {
    MINOR("轻伤", 7),
    MODERATE("中度受伤", 30),
    SEVERE("重伤", 90)
}

enum class PlayerPersonality(val displayName: String) {
    AGGRESSIVE("激进型"),    // 攻击性强，容易失误
    STEADY("稳健型"),        // 稳定发挥
    CLUTCH("关键先生"),      // 关键时刻表现突出
    TEAM_PLAYER("团队型"),   // 团队配合好
    CARRY("核心型")          // 个人能力强，需要资源
}

enum class Achievement(val displayName: String, val emoji: String) {
    ROOKIE_OF_YEAR("年度新秀", "🌟"),
    MVP("MVP", "👑"),
    WORLD_CHAMPION("世界冠军", "🏆"),
    PENTAKILL_MASTER("五杀大师", "⚔️"),
    LEGENDARY_PLAYER("传奇选手", "✨")
}
```

---

## 三、选手获取方式

### 3.1 青训营招募

**费用**：每次招募10万元

**概率分布**：
```kotlin
object PlayerRecruitment {
    fun recruitFromAcademy(): EsportsPlayer {
        val roll = Random.nextDouble()
        val rarity = when {
            roll < 0.001 -> PlayerRarity.SSR  // 0.1%
            roll < 0.01 -> PlayerRarity.S     // 0.9%
            roll < 0.05 -> PlayerRarity.A     // 4%
            roll < 0.20 -> PlayerRarity.B     // 15%
            else -> PlayerRarity.C            // 80%
        }
        return generatePlayer(rarity)
    }
}
```

**生成规则**：
- 年龄：16-18岁（青训营）
- 属性：品质范围内随机
- 英雄池：2-5个英雄，熟练度20-40
- 无职业经验

### 3.2 转会市场

**条件**：其他俱乐部挂牌的选手

**费用**：
- 转会费：选手当前身价（基于属性和成就）
- 签约费：品质对应的签约费
- 总费用 = 转会费 + 签约费

**身价计算**：
```kotlin
fun calculateTransferFee(player: EsportsPlayer): Long {
    val baseValue = when (player.rarity) {
        PlayerRarity.C -> 100_000L
        PlayerRarity.B -> 500_000L
        PlayerRarity.A -> 2_000_000L
        PlayerRarity.S -> 8_000_000L
        PlayerRarity.SSR -> 30_000_000L
    }
    
    // 年龄系数（黄金年龄20-24岁）
    val ageFactor = when (player.age) {
        in 16..19 -> 0.8  // 潜力股
        in 20..24 -> 1.2  // 黄金年龄
        in 25..27 -> 1.0  // 成熟期
        in 28..30 -> 0.6  // 下滑期
        else -> 0.3       // 老将
    }
    
    // 成就系数
    val achievementFactor = 1.0 + (player.achievements.size * 0.2)
    
    // 综合实力系数
    val overallFactor = player.attributes.overallRating() / 100.0
    
    return (baseValue * ageFactor * achievementFactor * overallFactor).toLong()
}
```

### 3.3 自由市场

**条件**：合同到期的选手

**费用**：只需支付签约费（无转会费）

**出现时间**：每年1月和7月刷新

---

## 四、选手培养系统

### 4.1 训练系统

```kotlin
enum class TrainingType(
    val displayName: String,
    val targetAttribute: String,
    val costPerDay: Long,
    val improvement: Int        // 每天提升
) {
    MECHANICS("操作训练", "mechanics", 5000, 1),
    AWARENESS("意识训练", "awareness", 5000, 1),
    TEAMWORK("团队训练", "teamwork", 3000, 1),
    MENTALITY("心理训练", "mentality", 3000, 1),
    HERO_PRACTICE("英雄练习", "heroMastery", 2000, 2)
}

object PlayerTraining {
    fun trainPlayer(
        player: EsportsPlayer,
        type: TrainingType,
        days: Int
    ): EsportsPlayer {
        val maxImprovement = player.rarity.growthPotential.last
        val actualImprovement = (type.improvement * days)
            .coerceAtMost(maxImprovement)
        
        // 更新属性
        val newAttributes = when (type.targetAttribute) {
            "mechanics" -> player.attributes.copy(
                mechanics = (player.attributes.mechanics + actualImprovement).coerceAtMost(100)
            )
            "awareness" -> player.attributes.copy(
                awareness = (player.attributes.awareness + actualImprovement).coerceAtMost(100)
            )
            // ... 其他属性
            else -> player.attributes
        }
        
        // 消耗体力
        val newStamina = (player.stamina - days * 5).coerceAtLeast(0)
        
        return player.copy(
            attributes = newAttributes,
            stamina = newStamina
        )
    }
}
```

### 4.2 比赛经验

**获得经验**：
- 参加比赛：+1-3点属性（随机）
- 获胜：额外+1点
- 获得MVP：额外+2点

**英雄熟练度**：
```kotlin
fun gainHeroExperience(
    player: EsportsPlayer,
    heroId: String,
    won: Boolean
): EsportsPlayer {
    val mastery = player.heroPool.find { it.heroId == heroId }
    if (mastery != null) {
        val newProficiency = (mastery.proficiency + if (won) 2 else 1)
            .coerceAtMost(100)
        val newGamesPlayed = mastery.gamesPlayed + 1
        val newWinRate = if (won) {
            ((mastery.winRate * mastery.gamesPlayed) + 1) / newGamesPlayed
        } else {
            (mastery.winRate * mastery.gamesPlayed) / newGamesPlayed
        }
        
        val updatedMastery = mastery.copy(
            proficiency = newProficiency,
            gamesPlayed = newGamesPlayed,
            winRate = newWinRate
        )
        
        val newHeroPool = player.heroPool.toMutableList()
        val index = newHeroPool.indexOfFirst { it.heroId == heroId }
        newHeroPool[index] = updatedMastery
        
        return player.copy(heroPool = newHeroPool)
    }
    return player
}
```

### 4.3 状态管理

**体力系统**：
- 每场比赛消耗10点体力
- 每天自动恢复5点体力
- 体力<30时表现下降20%
- 体力<10时强制休息

**士气系统**：
- 获胜：+5士气
- 失败：-3士气
- 连胜：额外+2士气/场
- 连败：额外-2士气/场
- 士气影响比赛表现（±10%）

**伤病系统**：
```kotlin
fun checkInjury(player: EsportsPlayer): EsportsPlayer {
    // 体力低于20时，10%概率受伤
    if (player.stamina < 20 && Random.nextDouble() < 0.1) {
        val severity = when (Random.nextInt(100)) {
            in 0..69 -> InjuryStatus.MINOR      // 70%
            in 70..94 -> InjuryStatus.MODERATE  // 25%
            else -> InjuryStatus.SEVERE         // 5%
        }
        return player.copy(injury = severity)
    }
    return player
}
```

### 4.4 成长曲线

```
属性成长示例（C级选手，基础60，成长2/年）

年龄   属性   说明
16    60     初始
17    62     +2
18    64     +2
19    66     +2（进入职业）
20    68     +2
21    70     +2
22    72     +2（巅峰期开始）
23    74     +2
24    76     +2
25    78     +2（巅峰期结束）
26    78     +0（稳定期）
27    78     +0
28    77     -1（下滑期）
29    75     -2
30    73     -2
```

**年龄影响**：
- 16-21岁：快速成长期
- 22-25岁：巅峰期（属性最高）
- 26-27岁：稳定期（不涨不跌）
- 28-30岁：下滑期（逐年-1到-2）
- 30岁+：急速下滑（-3到-5）
- 35岁：强制退役

---

## 五、选手管理界面

### 5.1 战队阵容

```
┌─────────────────────────────────────────┐
│  🏆 我的战队                             │
├─────────────────────────────────────────┤
│  首发阵容：                              │
│  ┌─────┐  ┌─────┐  ┌─────┐             │
│  │ TOP │  │ JUG │  │ MID │             │
│  │🟢张三│  │🔵李四│  │🟣王五│            │
│  │ 75  │  │ 82  │  │ 88  │             │
│  │ C级 │  │ A级 │  │ S级 │             │
│  └─────┘  └─────┘  └─────┘             │
│                                         │
│  ┌─────┐  ┌─────┐                      │
│  │ ADC │  │ SUP │                      │
│  │🔵赵六│  │🟢钱七│                     │
│  │ 80  │  │ 72  │                      │
│  │ A级 │  │ B级 │                      │
│  └─────┘  └─────┘                      │
│                                         │
│  替补席：                                │
│  ┌─────┐  ┌─────┐                      │
│  │🟢孙八│  │⚪周九│                     │
│  │ 70  │  │ 62  │                      │
│  └─────┘  └─────┘                      │
│                                         │
│  月薪总计：¥43万                         │
└─────────────────────────────────────────┘
```

### 5.2 选手详情

```
┌─────────────────────────────────────────┐
│  🟣 李四 (S级) - 打野位                  │
│  年龄：23岁 | 国籍：中国                 │
├─────────────────────────────────────────┤
│  综合实力：82/100 ⭐⭐⭐⭐               │
│                                         │
│  属性：                                 │
│  操作  ███████████░ 85                  │
│  意识  ████████████ 88                  │
│  团队  ████████░░░░ 78                  │
│  心态  ████████░░░░ 80                  │
│  熟练  ███████░░░░░ 75                  │
│                                         │
│  状态：                                 │
│  体力 ███████░░░ 70% | 士气 ████████ 85%│
│  状态 ████████░░ 82% | 伤病 ✅无       │
│                                         │
│  招牌英雄：                              │
│  🦁狂野猎手(95) 🗡️暗夜刺客(88) 🐉巨龙(82)│
│                                         │
│  生涯数据：                              │
│  总场次：125 | 胜率：58.4%              │
│  KDA：4.2 | MVP：18次                   │
│  冠军：2次（城市杯×2）                   │
│                                         │
│  合同：                                 │
│  2023.1 - 2026.1 (剩余18个月)          │
│  月薪：¥20万 | 违约金：¥800万           │
│                                         │
│  [训练] [续约] [挂牌转会]               │
└─────────────────────────────────────────┘
```

### 5.3 招募界面

```
┌─────────────────────────────────────────┐
│  🎯 青训营招募                           │
│  费用：¥10万/次                          │
├─────────────────────────────────────────┤
│  概率：                                 │
│  SSR(🟠) 0.1%  ████░░░░░░░░░░░░░░░░    │
│  S(🟣)   0.9%  ████░░░░░░░░░░░░░░░░    │
│  A(🔵)   4.0%  ████░░░░░░░░░░░░░░░░    │
│  B(🟢)  15.0%  ████████░░░░░░░░░░░░    │
│  C(⚪)  80.0%  ████████████████████    │
│                                         │
│  [招募一次] [招募十次 ¥90万]            │
├─────────────────────────────────────────┤
│  最近招募：                              │
│  🟢刘十 (B级) 中单 72分                 │
│  ⚪周十一 (C级) 辅助 58分                │
│  🟢赵十二 (B级) 上单 68分                │
│                                         │
│  [查看详情] [签约] [放弃]               │
└─────────────────────────────────────────┘
```

---

## 六、数据持久化

```kotlin
data class SaveData(
    // ... 现有字段
    
    // 选手系统
    val allPlayers: List<EsportsPlayer> = emptyList(),
    val myTeamPlayers: List<String> = emptyList(),  // 选手ID列表
    val freeAgents: List<String> = emptyList(),     // 自由市场选手ID
    val transferMarket: List<TransferListing> = emptyList()
)

data class TransferListing(
    val playerId: String,
    val clubName: String,
    val askingPrice: Long,
    val listingDate: Date
)
```

---

## 七、AI战队选手生成

```kotlin
object AITeamGenerator {
    // 为AI战队生成合理的选手
    fun generateTeamForAI(teamStrength: TeamStrength): List<EsportsPlayer> {
        val rarities = when (teamStrength) {
            TeamStrength.WEAK -> listOf(
                PlayerRarity.C, PlayerRarity.C, PlayerRarity.C,
                PlayerRarity.B, PlayerRarity.C
            )
            TeamStrength.MEDIUM -> listOf(
                PlayerRarity.B, PlayerRarity.B, PlayerRarity.A,
                PlayerRarity.B, PlayerRarity.B
            )
            TeamStrength.STRONG -> listOf(
                PlayerRarity.A, PlayerRarity.A, PlayerRarity.S,
                PlayerRarity.A, PlayerRarity.A
            )
            TeamStrength.LEGENDARY -> listOf(
                PlayerRarity.S, PlayerRarity.S, PlayerRarity.SSR,
                PlayerRarity.S, PlayerRarity.S
            )
        }
        
        return rarities.mapIndexed { index, rarity ->
            val position = HeroPosition.values()[index]
            generatePlayer(rarity, position)
        }
    }
}

enum class TeamStrength {
    WEAK,        // 弱队（城市杯）
    MEDIUM,      // 中等（锦标赛）
    STRONG,      // 强队（锦标赛冠军）
    LEGENDARY    // 传奇（全球总决赛）
}
```

---

## 八、测试用例

- [ ] 选手生成：验证品质概率分布
- [ ] 属性成长：验证年龄影响
- [ ] 训练系统：验证属性提升
- [ ] 状态管理：验证体力/士气/伤病
- [ ] 合同系统：验证签约/续约/转会
- [ ] UI展示：验证所有界面正常显示
