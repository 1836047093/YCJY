# MOBA电竞系统 - 赛事系统设计

## 一、赛事层级体系

```kotlin
enum class TournamentTier(
    val displayName: String,
    val emoji: String,
    val entryFee: Long,
    val minPrizePool: Long,
    val duration: Int,          // 总天数
    val prestigeReward: Int,
    val requirements: TournamentRequirements
) {
    CITY_CUP(
        "城市杯", "🏙️",
        100_000,
        500_000,
        30,
        50,
        TournamentRequirements(
            minTeamSize = 5,
            minPlayerLevel = 0,
            prerequisite = null
        )
    ),
    
    CHAMPIONSHIP(
        "锦标赛", "🏆",
        500_000,
        5_000_000,
        60,
        200,
        TournamentRequirements(
            minTeamSize = 7,  // 5主力+2替补
            minPlayerLevel = 1,
            prerequisite = TournamentTier.CITY_CUP  // 需要参加过城市杯
        )
    ),
    
    WORLDS(
        "全球总决赛", "🌍",
        2_000_000,
        50_000_000,
        90,
        1000,
        TournamentRequirements(
            minTeamSize = 7,
            minPlayerLevel = 2,
            prerequisite = TournamentTier.CHAMPIONSHIP  // 需要锦标赛资格
        )
    )
}

data class TournamentRequirements(
    val minTeamSize: Int,
    val minPlayerLevel: Int,
    val prerequisite: TournamentTier?
)
```

---

## 二、城市杯（City Cup）

### 2.1 赛制概览

**定位**：入门级赛事，练兵场

**赛制**：小组赛 + 淘汰赛

**参赛队伍**：16支

**赛程**：30天

### 2.2 详细赛程

#### 第一阶段：小组赛（15天）

```kotlin
data class GroupStage(
    val groups: List<Group>,
    val format: MatchFormat = MatchFormat.BO1
) {
    data class Group(
        val name: String,      // A组、B组、C组、D组
        val teams: List<Team>, // 4支队伍
        val matches: List<Match>
    )
}

// 赛程安排
第1-3天：第一轮（A1 vs A2, A3 vs A4）
第4-6天：第二轮（A1 vs A3, A2 vs A4）
第7-9天：第三轮（A1 vs A4, A2 vs A3）
第10-12天：其他组比赛
第13-15天：加赛（如有积分相同）

// 积分规则
胜利 = 1分
失败 = 0分

// 晋级规则
每组前2名晋级（共8队）
如积分相同，比较净胜场
```

#### 第二阶段：淘汰赛（15天）

```
淘汰赛对阵（单败淘汰）：

1/4决赛（BO3）：
┌─────────┐
│ A1 vs B2 │ ──┐
└─────────┘   │ 胜者1
              ├────┐
┌─────────┐   │    │
│ B1 vs A2 │ ──┘    │
└─────────┘         │
                    ├──── 决赛胜者
┌─────────┐         │
│ C1 vs D2 │ ──┐    │
└─────────┘   │    │
              ├────┘
┌─────────┐   │ 胜者2
│ D1 vs C2 │ ──┘
└─────────┘

半决赛（BO5）：5-7天
决赛（BO5）：8-10天
三四名决赛（BO3）：11-15天
```

### 2.3 奖励分配

```kotlin
enum class CityCupPlacement(
    val displayName: String,
    val prizeMoney: Long,
    val prestige: Int,
    val championshipPoints: Int  // 锦标赛积分
) {
    CHAMPION("冠军", 250_000, 50, 100),
    RUNNER_UP("亚军", 100_000, 30, 70),
    THIRD_PLACE("季军", 50_000, 20, 50),
    FOURTH_PLACE("殿军", 30_000, 15, 40),
    QUARTER_FINALIST("8强", 20_000, 10, 30),
    GROUP_STAGE("小组赛", 0, 5, 10)
}
```

### 2.4 赛事流程

```kotlin
data class CityCupTournament(
    val id: String,
    val year: Int,
    val season: Season,
    val registeredTeams: MutableList<Team>,
    val currentPhase: TournamentPhase,
    val groupStage: GroupStage?,
    val playoffs: PlayoffBracket?,
    val schedule: List<ScheduledMatch>
) {
    enum class Season {
        SPRING,  // 春季（1-2月）
        SUMMER,  // 夏季（6-7月）
        AUTUMN,  // 秋季（9-10月）
        WINTER   // 冬季（12月）
    }
    
    enum class TournamentPhase {
        REGISTRATION,   // 报名阶段
        GROUP_STAGE,    // 小组赛
        PLAYOFFS,       // 淘汰赛
        COMPLETED       // 已完成
    }
}
```

---

## 三、锦标赛（Championship）

### 3.1 赛制概览

**定位**：职业级赛事，年度大赛

**赛制**：常规赛 + 季后赛

**参赛队伍**：12支（需要城市杯积分排名前12）

**赛程**：60天

### 3.2 详细赛程

#### 第一阶段：常规赛（40天）

```kotlin
data class RegularSeason(
    val teams: List<Team>,     // 12支队伍
    val format: MatchFormat = MatchFormat.BO3,
    val standings: MutableList<TeamStanding>
) {
    data class TeamStanding(
        val team: Team,
        val points: Int,       // 积分
        val wins: Int,         // 2:0或2:1胜场
        val losses: Int,       // 0:2或1:2负场
        val gameWins: Int,     // 小局胜场
        val gameLosses: Int    // 小局负场
    ) {
        fun winRate(): Double = 
            if (wins + losses > 0) wins.toDouble() / (wins + losses) else 0.0
    }
}

// 赛程安排
第1-40天：双循环BO3（每队22场比赛）
- 每队对其他11队各打2次
- 每场BO3平均2天（包括休息）

// 积分规则
2:0胜利 = 3分
2:1胜利 = 2分
1:2失败 = 1分
0:2失败 = 0分

// 排名规则
1. 积分高者排名靠前
2. 积分相同，比较胜场数
3. 胜场相同，比较小局胜负差
4. 仍相同，比较直接对战成绩
```

#### 第二阶段：季后赛（20天）

```
季后赛对阵（双败淘汰）：

上半区（1-4名）：
┌────────────┐
│ 1 vs 4(BO5)│ ──┐
└────────────┘   │
                 ├─── 胜者进决赛
┌────────────┐   │
│ 2 vs 3(BO5)│ ──┘
└────────────┘

下半区（5-8名）：
┌────────────┐
│ 5 vs 8(BO5)│ ──┐
└────────────┘   │
                 ├─── 胜者争季军
┌────────────┐   │
│ 6 vs 7(BO5)│ ──┘
└────────────┘

决赛（BO7）：第16-20天
```

### 3.3 奖励分配

```kotlin
enum class ChampionshipPlacement(
    val displayName: String,
    val prizeMoney: Long,
    val prestige: Int,
    val worldsQualification: Boolean  // 全球总决赛资格
) {
    CHAMPION("冠军", 2_500_000, 200, true),
    RUNNER_UP("亚军", 1_000_000, 150, true),
    THIRD_PLACE("季军", 500_000, 100, false),
    FOURTH_PLACE("殿军", 300_000, 80, false),
    FIFTH_TO_EIGHTH("5-8名", 100_000, 50, false),
    REGULAR_SEASON("常规赛", 0, 20, false)
}
```

### 3.4 全球总决赛资格系统

```kotlin
data class WorldsQualification(
    val directSeeds: List<Team>,      // 直接晋级（春夏冠军）
    val pointsRanking: List<Team>,    // 积分排名
    val gauntlet: GauntletTournament? // 冒泡赛
) {
    // 积分规则（累计春季+夏季）
    // 冠军：100分
    // 亚军：70分
    // 季军：50分
    // 4名：40分
    // 5-8名：20分
    
    // 晋级名额分配：
    // - 春季冠军：直接晋级
    // - 夏季冠军：直接晋级
    // - 积分排名3-5名：参加冒泡赛，胜者晋级
}

data class GauntletTournament(
    val teams: List<Team>,  // 3-5支队伍
    val format: MatchFormat = MatchFormat.BO5
) {
    // 冒泡赛规则：
    // 第5名 vs 第4名 → 胜者 vs 第3名
    // 最终胜者获得全球总决赛资格
}
```

---

## 四、全球总决赛（Worlds）

### 4.1 赛制概览

**定位**：世界级赛事，年度巅峰

**赛制**：入围赛 + 小组赛 + 淘汰赛

**参赛队伍**：16支（来自全球各赛区）

**赛程**：90天

### 4.2 详细赛程

#### 第一阶段：入围赛（20天）

```kotlin
data class PlayInStage(
    val teams: List<Team>,     // 8支队伍（非直邀）
    val format: MatchFormat = MatchFormat.BO5
) {
    // 双败淘汰制
    // 8支队伍 → 前4名晋级小组赛
}

// 赛程安排
第1-5天：第一轮（1v8, 2v7, 3v6, 4v5）
第6-10天：胜者组半决赛
第11-15天：败者组复活赛
第16-20天：最终决定4个晋级名额
```

#### 第二阶段：小组赛（30天）

```
16支队伍分为4组（A/B/C/D组）

赛程：
第1-15天：第一轮（组内单循环BO1）
第16-30天：第二轮（组内单循环BO1）

晋级规则：
每组前2名晋级淘汰赛（共8队）

积分规则：
胜利 = 1分
失败 = 0分
```

#### 第三阶段：淘汰赛（40天）

```
淘汰赛对阵（单败淘汰）：

1/4决赛（BO5）：第1-10天
├─ A1 vs B2
├─ B1 vs A2
├─ C1 vs D2
└─ D1 vs C2

半决赛（BO5）：第11-25天
├─ 胜者1 vs 胜者2
└─ 胜者3 vs 胜者4

决赛（BO7）：第26-40天
└─ 半决赛胜者 vs 半决赛胜者
```

### 4.3 奖励分配

```kotlin
enum class WorldsPlacement(
    val displayName: String,
    val prizeMoney: Long,
    val prestige: Int,
    val specialReward: String?
) {
    CHAMPION("世界冠军", 25_000_000, 1000, "永久'世界冠军'称号"),
    RUNNER_UP("亚军", 10_000_000, 700, null),
    THIRD_PLACE("季军", 5_000_000, 500, null),
    FOURTH_PLACE("殿军", 3_000_000, 400, null),
    QUARTER_FINALIST("8强", 2_000_000, 300, null),
    GROUP_STAGE("小组赛", 1_000_000, 200, null),
    PLAY_IN("入围赛", 500_000, 100, null)
}
```

### 4.4 世界冠军特殊奖励

```kotlin
data class WorldChampionRewards(
    val title: String = "世界冠军",
    val permanentBadge: Boolean = true,
    val skinRevenue: Long = 5_000_000,  // 冠军皮肤收入
    val brandValue: Double = 2.0,       // 品牌价值翻倍
    val sponsorshipBonus: Long = 10_000_000
)
```

---

## 五、赛事管理器

### 5.1 核心数据结构

```kotlin
data class Tournament(
    val id: String,
    val tier: TournamentTier,
    val year: Int,
    val season: CityCupTournament.Season?,
    val status: TournamentStatus,
    val registeredTeams: MutableList<Team>,
    val currentPhase: TournamentPhase,
    val schedule: MutableList<ScheduledMatch>,
    val results: MutableMap<String, MatchResult>,
    val prizePool: Long
) {
    enum class TournamentStatus {
        UPCOMING,      // 即将开始
        REGISTRATION,  // 报名中
        IN_PROGRESS,   // 进行中
        COMPLETED      // 已完成
    }
}

data class ScheduledMatch(
    val id: String,
    val date: Date,
    val blueTeam: Team,
    val redTeam: Team,
    val format: MatchFormat,
    val phase: String,     // "小组赛第1轮" "半决赛" 等
    val status: MatchStatus,
    val result: MatchResult?
) {
    enum class MatchStatus {
        SCHEDULED,   // 已安排
        LIVE,        // 进行中
        COMPLETED    // 已完成
    }
}

enum class MatchFormat(val displayName: String, val maxGames: Int) {
    BO1("BO1", 1),
    BO3("BO3", 3),
    BO5("BO5", 5),
    BO7("BO7", 7)
}
```

### 5.2 赛事管理器实现

```kotlin
object TournamentManager {
    private val activeTournaments = mutableMapOf<String, Tournament>()
    
    // 创建新赛事
    fun createTournament(
        tier: TournamentTier,
        year: Int,
        season: CityCupTournament.Season?
    ): Tournament {
        val tournament = Tournament(
            id = generateTournamentId(tier, year, season),
            tier = tier,
            year = year,
            season = season,
            status = TournamentStatus.REGISTRATION,
            registeredTeams = mutableListOf(),
            currentPhase = TournamentPhase.REGISTRATION,
            schedule = mutableListOf(),
            results = mutableMapOf(),
            prizePool = tier.minPrizePool
        )
        activeTournaments[tournament.id] = tournament
        return tournament
    }
    
    // 报名参赛
    fun registerTeam(tournamentId: String, team: Team): Boolean {
        val tournament = activeTournaments[tournamentId] ?: return false
        
        // 检查资格
        if (!checkEligibility(tournament, team)) {
            return false
        }
        
        // 扣除报名费
        if (!team.payEntryFee(tournament.tier.entryFee)) {
            return false
        }
        
        tournament.registeredTeams.add(team)
        return true
    }
    
    // 开始赛事
    fun startTournament(tournamentId: String) {
        val tournament = activeTournaments[tournamentId] ?: return
        
        when (tournament.tier) {
            TournamentTier.CITY_CUP -> {
                // 生成小组赛赛程
                generateCityCupSchedule(tournament)
            }
            TournamentTier.CHAMPIONSHIP -> {
                // 生成常规赛赛程
                generateChampionshipSchedule(tournament)
            }
            TournamentTier.WORLDS -> {
                // 生成入围赛赛程
                generateWorldsSchedule(tournament)
            }
        }
        
        tournament.status = TournamentStatus.IN_PROGRESS
    }
    
    // 推进赛程（每日结算调用）
    fun progressTournament(tournamentId: String, currentDate: Date) {
        val tournament = activeTournaments[tournamentId] ?: return
        
        // 找到今天要进行的比赛
        val todayMatches = tournament.schedule.filter { 
            it.date == currentDate && it.status == MatchStatus.SCHEDULED 
        }
        
        // 模拟比赛
        todayMatches.forEach { match ->
            val result = simulateMatch(match)
            match.status = MatchStatus.COMPLETED
            match.result = result
            tournament.results[match.id] = result
        }
        
        // 检查是否需要进入下一阶段
        checkPhaseTransition(tournament)
    }
    
    // 检查参赛资格
    private fun checkEligibility(tournament: Tournament, team: Team): Boolean {
        val requirements = tournament.tier.requirements
        
        // 检查队伍规模
        if (team.players.size < requirements.minTeamSize) {
            return false
        }
        
        // 检查前置赛事
        if (requirements.prerequisite != null) {
            val hasParticipated = team.tournamentHistory.any { 
                it.tier == requirements.prerequisite 
            }
            if (!hasParticipated) {
                return false
            }
        }
        
        return true
    }
    
    // 生成城市杯赛程
    private fun generateCityCupSchedule(tournament: Tournament) {
        val teams = tournament.registeredTeams.shuffled()
        
        // 分组
        val groups = teams.chunked(4).mapIndexed { index, teamList ->
            GroupStage.Group(
                name = ('A' + index).toString(),
                teams = teamList,
                matches = mutableListOf()
            )
        }
        
        // 生成小组赛对阵
        groups.forEach { group ->
            // 单循环BO1
            for (i in 0 until group.teams.size) {
                for (j in i + 1 until group.teams.size) {
                    val match = ScheduledMatch(
                        id = generateMatchId(),
                        date = calculateMatchDate(tournament, i, j),
                        blueTeam = group.teams[i],
                        redTeam = group.teams[j],
                        format = MatchFormat.BO1,
                        phase = "小组赛-${group.name}组",
                        status = MatchStatus.SCHEDULED,
                        result = null
                    )
                    tournament.schedule.add(match)
                }
            }
        }
    }
    
    // 生成锦标赛赛程（常规赛）
    private fun generateChampionshipSchedule(tournament: Tournament) {
        val teams = tournament.registeredTeams
        
        // 双循环BO3
        for (round in 1..2) {
            for (i in 0 until teams.size) {
                for (j in i + 1 until teams.size) {
                    val match = ScheduledMatch(
                        id = generateMatchId(),
                        date = calculateMatchDate(tournament, round, i, j),
                        blueTeam = teams[i],
                        redTeam = teams[j],
                        format = MatchFormat.BO3,
                        phase = "常规赛第${round}轮",
                        status = MatchStatus.SCHEDULED,
                        result = null
                    )
                    tournament.schedule.add(match)
                }
            }
        }
    }
    
    // 检查阶段转换
    private fun checkPhaseTransition(tournament: Tournament) {
        when (tournament.tier) {
            TournamentTier.CITY_CUP -> {
                if (tournament.currentPhase == TournamentPhase.GROUP_STAGE) {
                    val groupMatches = tournament.schedule.filter { 
                        it.phase.contains("小组赛") 
                    }
                    if (groupMatches.all { it.status == MatchStatus.COMPLETED }) {
                        // 进入淘汰赛
                        generatePlayoffBracket(tournament)
                        tournament.currentPhase = TournamentPhase.PLAYOFFS
                    }
                }
            }
            // ... 其他赛事类型
        }
    }
    
    // 发放奖励
    fun distributeRewards(tournamentId: String) {
        val tournament = activeTournaments[tournamentId] ?: return
        val finalStandings = calculateFinalStandings(tournament)
        
        finalStandings.forEachIndexed { index, team ->
            val placement = getPlacementReward(tournament.tier, index + 1)
            team.addPrizeMoney(placement.prizeMoney)
            team.addPrestige(placement.prestige)
            
            // 记录成就
            team.addTournamentRecord(
                TournamentRecord(
                    tournamentId = tournament.id,
                    tier = tournament.tier,
                    year = tournament.year,
                    placement = index + 1,
                    prizeMoney = placement.prizeMoney
                )
            )
        }
        
        tournament.status = TournamentStatus.COMPLETED
    }
}
```

---

## 六、赛事UI界面

### 6.1 赛事中心主界面

```
┌─────────────────────────────────────────┐
│  🏆 赛事中心                             │
├─────────────────────────────────────────┤
│  [可用赛事] [我的赛程] [赛事历史]        │
├─────────────────────────────────────────┤
│  可用赛事：                              │
│                                         │
│  🏙️ 城市杯·春季赛                       │
│  └ 报名中 (剩余5天)                      │
│  └ 报名费：¥10万 | 奖金池：¥50万        │
│  └ [查看详情] [立即报名]                 │
│                                         │
│  🏆 锦标赛·夏季赛                        │
│  └ 未开放（需要城市杯积分）              │
│                                         │
│  🌍 全球总决赛                           │
│  └ 未开放（需要锦标赛资格）              │
└─────────────────────────────────────────┘
```

### 6.2 赛程详情界面

```
┌─────────────────────────────────────────┐
│  🏙️ 城市杯·春季赛                       │
│  状态：小组赛进行中 (第7天/30天)         │
├─────────────────────────────────────────┤
│  我的战绩：2胜1负 (积分2)                │
│  小组排名：A组第2名                      │
│                                         │
│  近期赛程：                              │
│  ✅ 第1天 vs 雷霆战队 2:0 胜             │
│  ✅ 第3天 vs 风暴战队 2:1 胜             │
│  ❌ 第5天 vs 龙腾战队 1:2 负             │
│  📅 第8天 vs 星辰战队 (明天)             │
│                                         │
│  [查看完整赛程] [小组积分榜]             │
└─────────────────────────────────────────┘
```

---

## 七、数据持久化

```kotlin
data class SaveData(
    // ... 现有字段
    
    // 赛事系统
    val activeTournaments: List<Tournament> = emptyList(),
    val tournamentHistory: List<TournamentRecord> = emptyList(),
    val championshipPoints: Int = 0,
    val worldsQualified: Boolean = false
)

data class TournamentRecord(
    val tournamentId: String,
    val tier: TournamentTier,
    val year: Int,
    val season: CityCupTournament.Season?,
    val placement: Int,
    val prizeMoney: Long,
    val prestigeEarned: Int
)
```

---

## 八、删除的内容

### ❌ 世界冠军赛（WorldChampionship）

**删除原因**：
- 与全球总决赛（Worlds）功能重复
- 简化赛事体系，避免过于复杂
- 全球总决赛已经是最高级别赛事

**迁移方案**：
- 原世界冠军赛的定位由全球总决赛承担
- 保持三级赛事体系：城市杯 → 锦标赛 → 全球总决赛

---

## 九、测试用例

- [ ] 赛事创建：验证赛程生成正确
- [ ] 报名系统：验证资格检查和费用扣除
- [ ] 小组赛：验证积分计算和排名
- [ ] 淘汰赛：验证对阵生成
- [ ] 奖励发放：验证奖金和声望
- [ ] 全球总决赛资格：验证冒泡赛逻辑
