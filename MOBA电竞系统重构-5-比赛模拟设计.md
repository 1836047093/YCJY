# MOBA电竞系统 - 比赛模拟设计

## 一、比赛数据结构

```kotlin
data class Match(
    val id: String,
    val tournamentId: String,
    val blueTeam: Team,
    val redTeam: Team,
    val bpSession: BPSession?,
    val result: MatchResult?,
    val format: MatchFormat  // BO1/BO3/BO5/BO7
)

data class MatchResult(
    val winner: Team,
    val loser: Team,
    val gameResults: List<GameResult>,  // 每一局的结果
    val mvp: EsportsPlayer,
    val highlights: List<String>,       // 精彩回放文字
    val duration: Int                   // 总时长（分钟）
)

data class GameResult(
    val gameNumber: Int,
    val winner: TeamSide,
    val duration: Int,          // 单局时长（分钟）
    val blueTeamStats: TeamGameStats,
    val redTeamStats: TeamGameStats,
    val playerStats: List<PlayerGameStats>
)

data class TeamGameStats(
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val towers: Int,           // 推塔数
    val dragons: Int,          // 小龙数
    val barons: Int,           // 大龙数
    val totalGold: Int,        // 总经济
    val totalDamage: Long      // 总伤害
) {
    fun kda(): Double = if (deaths > 0) (kills + assists).toDouble() / deaths else 99.9
}

data class PlayerGameStats(
    val playerId: String,
    val heroId: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val goldEarned: Int,
    val damageDealt: Long,
    val damageTaken: Long,
    val cs: Int,              // 补刀数
    val mvpScore: Double      // MVP评分
) {
    fun kda(): Double = if (deaths > 0) (kills + assists).toDouble() / deaths else 99.9
}
```

---

## 二、胜率计算系统

### 2.1 胜率影响因素

```kotlin
object WinRateCalculator {
    // 权重分配
    private const val PLAYER_ATTRIBUTES_WEIGHT = 0.40  // 选手属性
    private const val COMPOSITION_WEIGHT = 0.25        // 阵容质量
    private const val HERO_PROFICIENCY_WEIGHT = 0.20   // 英雄熟练度
    private const val COUNTER_WEIGHT = 0.10            // 克制关系
    private const val LUCK_WEIGHT = 0.05               // 随机波动
    
    fun calculateWinProbability(
        blueTeam: Team,
        redTeam: Team,
        blueComp: TeamComposition,
        redComp: TeamComposition
    ): Double {
        // 1. 选手属性对比（40%）
        val attributeScore = calculateAttributeScore(blueTeam, redTeam)
        
        // 2. 阵容质量对比（25%）
        val compositionScore = calculateCompositionScore(blueComp, redComp)
        
        // 3. 英雄熟练度对比（20%）
        val proficiencyScore = calculateProficiencyScore(blueComp, redComp)
        
        // 4. 克制关系（10%）
        val counterScore = calculateCounterScore(blueComp, redComp)
        
        // 5. 随机波动（5%）
        val luckScore = Random.nextDouble(-0.05, 0.05)
        
        // 综合计算
        val totalScore = attributeScore * PLAYER_ATTRIBUTES_WEIGHT +
                        compositionScore * COMPOSITION_WEIGHT +
                        proficiencyScore * HERO_PROFICIENCY_WEIGHT +
                        counterScore * COUNTER_WEIGHT +
                        luckScore
        
        // 转换为胜率（sigmoid函数）
        return sigmoid(totalScore)
    }
    
    // 选手属性评分（-1.0 到 1.0）
    private fun calculateAttributeScore(
        blueTeam: Team,
        redTeam: Team
    ): Double {
        val blueAvg = blueTeam.players.map { 
            it.attributes.overallRating() 
        }.average()
        val redAvg = redTeam.players.map { 
            it.attributes.overallRating() 
        }.average()
        
        // 差值转换为评分
        val diff = blueAvg - redAvg
        return (diff / 50.0).coerceIn(-1.0, 1.0)
    }
    
    // 阵容质量评分
    private fun calculateCompositionScore(
        blueComp: TeamComposition,
        redComp: TeamComposition
    ): Double {
        val bluScore = blueComp.scores.overall
        val redScore = redComp.scores.overall
        
        val diff = blueScore - redScore
        return (diff / 50.0).coerceIn(-1.0, 1.0)
    }
    
    // 英雄熟练度评分
    private fun calculateProficiencyScore(
        blueComp: TeamComposition,
        redComp: TeamComposition
    ): Double {
        val blueAvg = blueComp.players.map { player ->
            val hero = blueComp.heroes.find { 
                it.position == player.position 
            }
            player.heroPool.find { it.heroId == hero?.id }?.proficiency ?: 50
        }.average()
        
        val redAvg = redComp.players.map { player ->
            val hero = redComp.heroes.find { 
                it.position == player.position 
            }
            player.heroPool.find { it.heroId == hero?.id }?.proficiency ?: 50
        }.average()
        
        val diff = blueAvg - redAvg
        return (diff / 50.0).coerceIn(-1.0, 1.0)
    }
    
    // 克制关系评分
    private fun calculateCounterScore(
        blueComp: TeamComposition,
        redComp: TeamComposition
    ): Double {
        var blueCounters = 0
        var redCounters = 0
        
        blueComp.heroes.forEach { blueHero ->
            redComp.heroes.forEach { redHero ->
                if (blueHero.counters.contains(redHero.id)) {
                    blueCounters++
                }
                if (redHero.counters.contains(blueHero.id)) {
                    redCounters++
                }
            }
        }
        
        val counterDiff = blueCounters - redCounters
        return (counterDiff / 5.0).coerceIn(-1.0, 1.0)
    }
    
    // Sigmoid函数（将评分转换为概率）
    private fun sigmoid(x: Double): Double {
        return 1.0 / (1.0 + exp(-x * 5.0))
    }
}
```

### 2.2 状态影响因素

```kotlin
object StatusModifier {
    fun applyStatusModifiers(
        baseWinRate: Double,
        team: Team
    ): Double {
        var modifiedRate = baseWinRate
        
        // 体力影响
        val avgStamina = team.players.map { it.stamina }.average()
        if (avgStamina < 30) {
            modifiedRate *= 0.8  // -20%
        } else if (avgStamina < 50) {
            modifiedRate *= 0.9  // -10%
        }
        
        // 士气影响
        val avgMorale = team.players.map { it.morale }.average()
        when {
            avgMorale >= 80 -> modifiedRate *= 1.1  // +10%
            avgMorale <= 40 -> modifiedRate *= 0.9  // -10%
        }
        
        // 伤病影响
        val injuredCount = team.players.count { it.injury != null }
        if (injuredCount > 0) {
            modifiedRate *= (1.0 - injuredCount * 0.05)  // 每个伤员-5%
        }
        
        return modifiedRate.coerceIn(0.1, 0.9)
    }
}
```

---

## 三、比赛模拟器

### 3.1 核心模拟逻辑

```kotlin
object MatchSimulator {
    fun simulateMatch(match: Match): MatchResult {
        val gameResults = mutableListOf<GameResult>()
        var blueWins = 0
        var redWins = 0
        val maxWins = when (match.format) {
            MatchFormat.BO1 -> 1
            MatchFormat.BO3 -> 2
            MatchFormat.BO5 -> 3
            MatchFormat.BO7 -> 4
        }
        
        var gameNumber = 1
        while (blueWins < maxWins && redWins < maxWins) {
            val gameResult = simulateGame(
                gameNumber,
                match.blueTeam,
                match.redTeam,
                match.bpSession!!
            )
            gameResults.add(gameResult)
            
            when (gameResult.winner) {
                TeamSide.BLUE -> blueWins++
                TeamSide.RED -> redWins++
            }
            
            gameNumber++
        }
        
        val winner = if (blueWins > redWins) match.blueTeam else match.redTeam
        val loser = if (blueWins > redWins) match.redTeam else match.blueTeam
        
        // 选择MVP
        val mvp = selectMVP(gameResults, winner)
        
        // 生成精彩回放
        val highlights = generateHighlights(gameResults)
        
        return MatchResult(
            winner = winner,
            loser = loser,
            gameResults = gameResults,
            mvp = mvp,
            highlights = highlights,
            duration = gameResults.sumOf { it.duration }
        )
    }
    
    private fun simulateGame(
        gameNumber: Int,
        blueTeam: Team,
        redTeam: Team,
        bpSession: BPSession
    ): GameResult {
        // 获取阵容
        val blueComp = CompositionAnalyzer.analyzeComposition(
            bpSession.bluePicks.mapNotNull { HeroManager.getHeroById(it.heroId) },
            blueTeam.players
        )
        val redComp = CompositionAnalyzer.analyzeComposition(
            bpSession.redPicks.mapNotNull { HeroManager.getHeroById(it.heroId) },
            redTeam.players
        )
        
        // 计算胜率
        var blueWinRate = WinRateCalculator.calculateWinProbability(
            blueTeam, redTeam, blueComp, redComp
        )
        
        // 应用状态修正
        blueWinRate = StatusModifier.applyStatusModifiers(blueWinRate, blueTeam)
        
        // 决定胜负
        val blueWins = Random.nextDouble() < blueWinRate
        val winner = if (blueWins) TeamSide.BLUE else TeamSide.RED
        
        // 生成比赛时长（25-45分钟）
        val duration = Random.nextInt(25, 46)
        
        // 生成团队数据
        val (blueStats, redStats) = generateTeamStats(
            blueTeam, redTeam, blueWins, duration
        )
        
        // 生成选手数据
        val playerStats = generatePlayerStats(
            bpSession, blueStats, redStats, duration
        )
        
        return GameResult(
            gameNumber = gameNumber,
            winner = winner,
            duration = duration,
            blueTeamStats = blueStats,
            redTeamStats = redStats,
            playerStats = playerStats
        )
    }
}
```

### 3.2 数据生成

```kotlin
object DataGenerator {
    fun generateTeamStats(
        blueTeam: Team,
        redTeam: Team,
        blueWins: Boolean,
        duration: Int
    ): Pair<TeamGameStats, TeamGameStats> {
        // 基础击杀数（根据时长）
        val baseKills = duration / 2  // 约12-22次击杀
        
        val blueKills: Int
        val redKills: Int
        
        if (blueWins) {
            blueKills = baseKills + Random.nextInt(5, 15)
            redKills = baseKills - Random.nextInt(3, 10)
        } else {
            redKills = baseKills + Random.nextInt(5, 15)
            blueKills = baseKills - Random.nextInt(3, 10)
        }
        
        // 推塔数
        val (blueTowers, redTowers) = if (blueWins) {
            (Random.nextInt(8, 12) to Random.nextInt(2, 6))
        } else {
            (Random.nextInt(2, 6) to Random.nextInt(8, 12))
        }
        
        // 资源控制
        val (blueDragons, redDragons) = if (blueWins) {
            (Random.nextInt(2, 4) to Random.nextInt(0, 2))
        } else {
            (Random.nextInt(0, 2) to Random.nextInt(2, 4))
        }
        
        val (blueBarons, redBarons) = if (blueWins) {
            (Random.nextInt(1, 3) to Random.nextInt(0, 1))
        } else {
            (Random.nextInt(0, 1) to Random.nextInt(1, 3))
        }
        
        // 经济（基于时长和推塔）
        val blueGold = duration * 1000 + blueTowers * 500
        val redGold = duration * 900 + redTowers * 500
        
        return Pair(
            TeamGameStats(
                kills = blueKills,
                deaths = redKills,
                assists = blueKills * 2,
                towers = blueTowers,
                dragons = blueDragons,
                barons = blueBarons,
                totalGold = blueGold,
                totalDamage = blueGold * 50L
            ),
            TeamGameStats(
                kills = redKills,
                deaths = blueKills,
                assists = redKills * 2,
                towers = redTowers,
                dragons = redDragons,
                barons = redBarons,
                totalGold = redGold,
                totalDamage = redGold * 50L
            )
        )
    }
    
    fun generatePlayerStats(
        bpSession: BPSession,
        blueStats: TeamGameStats,
        redStats: TeamGameStats,
        duration: Int
    ): List<PlayerGameStats> {
        val allStats = mutableListOf<PlayerGameStats>()
        
        // 蓝方选手数据
        bpSession.bluePicks.forEach { pick ->
            val stats = generateIndividualStats(
                pick, blueStats, duration, true
            )
            allStats.add(stats)
        }
        
        // 红方选手数据
        bpSession.redPicks.forEach { pick ->
            val stats = generateIndividualStats(
                pick, redStats, duration, false
            )
            allStats.add(stats)
        }
        
        return allStats
    }
    
    private fun generateIndividualStats(
        pick: PickedHero,
        teamStats: TeamGameStats,
        duration: Int,
        isBlue: Boolean
    ): PlayerGameStats {
        val hero = HeroManager.getHeroById(pick.heroId)!!
        
        // 根据英雄位置分配数据
        val (killShare, assistShare) = when (pick.position) {
            HeroPosition.TOP -> (0.15 to 0.15)
            HeroPosition.JUNGLE -> (0.25 to 0.30)
            HeroPosition.MID -> (0.30 to 0.25)
            HeroPosition.ADC -> (0.25 to 0.15)
            HeroPosition.SUPPORT -> (0.05 to 0.40)
        }
        
        val kills = (teamStats.kills * killShare).toInt() + Random.nextInt(-2, 3)
        val assists = (teamStats.assists * assistShare).toInt() + Random.nextInt(-3, 4)
        val deaths = (teamStats.deaths / 5.0).toInt() + Random.nextInt(0, 3)
        
        // 经济分配
        val goldShare = when (pick.position) {
            HeroPosition.ADC -> 0.25
            HeroPosition.MID -> 0.23
            HeroPosition.TOP -> 0.20
            HeroPosition.JUNGLE -> 0.18
            HeroPosition.SUPPORT -> 0.14
        }
        val goldEarned = (teamStats.totalGold * goldShare).toInt()
        
        // 伤害分配（基于英雄类型）
        val damageShare = when (hero.type) {
            HeroType.MARKSMAN -> 0.30
            HeroType.MAGE -> 0.28
            HeroType.ASSASSIN -> 0.25
            HeroType.FIGHTER -> 0.20
            HeroType.TANK -> 0.12
            HeroType.SUPPORT -> 0.10
        }
        val damageDealt = (teamStats.totalDamage * damageShare).toLong()
        
        // 承受伤害（坦克承受更多）
        val tankShare = when (hero.type) {
            HeroType.TANK -> 0.35
            HeroType.FIGHTER -> 0.25
            HeroType.SUPPORT -> 0.20
            HeroType.ASSASSIN -> 0.15
            else -> 0.10
        }
        val damageTaken = (teamStats.totalDamage * tankShare * 0.8).toLong()
        
        // 补刀数
        val cs = when (pick.position) {
            HeroPosition.ADC -> duration * 7
            HeroPosition.MID -> duration * 6
            HeroPosition.TOP -> duration * 5
            HeroPosition.JUNGLE -> duration * 4
            HeroPosition.SUPPORT -> duration * 1
        } + Random.nextInt(-20, 21)
        
        // MVP评分
        val mvpScore = calculateMVPScore(
            kills, deaths, assists, goldEarned, damageDealt, damageTaken
        )
        
        return PlayerGameStats(
            playerId = pick.playerId,
            heroId = pick.heroId,
            kills = kills.coerceAtLeast(0),
            deaths = deaths.coerceAtLeast(0),
            assists = assists.coerceAtLeast(0),
            goldEarned = goldEarned,
            damageDealt = damageDealt,
            damageTaken = damageTaken,
            cs = cs.coerceAtLeast(0),
            mvpScore = mvpScore
        )
    }
    
    private fun calculateMVPScore(
        kills: Int,
        deaths: Int,
        assists: Int,
        gold: Int,
        damage: Long,
        damageTaken: Long
    ): Double {
        val kda = if (deaths > 0) (kills + assists * 0.5) / deaths else 10.0
        val goldScore = gold / 1000.0
        val damageScore = damage / 10000.0
        val tankScore = damageTaken / 15000.0
        
        return kda * 20 + goldScore * 0.5 + damageScore * 0.3 + tankScore * 0.2
    }
}
```

---

## 四、MVP评选

```kotlin
object MVPSelector {
    fun selectMVP(
        gameResults: List<GameResult>,
        winningTeam: Team
    ): EsportsPlayer {
        // 收集所有获胜方选手的数据
        val winningPlayerStats = gameResults.flatMap { game ->
            game.playerStats.filter { stat ->
                winningTeam.players.any { it.id == stat.playerId }
            }
        }
        
        // 按选手ID分组，计算总分
        val playerScores = winningPlayerStats
            .groupBy { it.playerId }
            .mapValues { (_, stats) ->
                stats.sumOf { it.mvpScore }
            }
        
        // 找到得分最高的选手
        val mvpId = playerScores.maxByOrNull { it.value }?.key
            ?: winningTeam.players.first().id
        
        return winningTeam.players.find { it.id == mvpId }!!
    }
}
```

---

## 五、精彩回放生成

```kotlin
object HighlightGenerator {
    fun generateHighlights(gameResults: List<GameResult>): List<String> {
        val highlights = mutableListOf<String>()
        
        gameResults.forEachIndexed { index, game ->
            val gameNum = index + 1
            
            // 找到表现最好的选手
            val bestPlayer = game.playerStats.maxByOrNull { it.mvpScore }
            if (bestPlayer != null) {
                val hero = HeroManager.getHeroById(bestPlayer.heroId)!!
                highlights.add(
                    "第${gameNum}局：${hero.name}拿下${bestPlayer.kills}/${bestPlayer.deaths}/${bestPlayer.assists}的完美数据！"
                )
            }
            
            // 团队击杀
            val blueKills = game.blueTeamStats.kills
            val redKills = game.redTeamStats.kills
            if (blueKills > 30 || redKills > 30) {
                highlights.add(
                    "第${gameNum}局：激烈的团战！双方总击杀达到${blueKills + redKills}次！"
                )
            }
            
            // 大龙争夺
            val totalBarons = game.blueTeamStats.barons + game.redTeamStats.barons
            if (totalBarons >= 2) {
                highlights.add(
                    "第${gameNum}局：双方围绕大龙展开激烈争夺，大龙被击杀${totalBarons}次！"
                )
            }
            
            // 五杀
            val pentakill = game.playerStats.find { it.kills >= 5 }
            if (pentakill != null) {
                val hero = HeroManager.getHeroById(pentakill.heroId)!!
                highlights.add(
                    "第${gameNum}局：${hero.name}完成五杀！势不可挡！"
                )
            }
        }
        
        return highlights.take(5)  // 最多5条
    }
}
```

---

## 六、比赛界面设计

### 6.1 比赛进行中

```
┌─────────────────────────────────────────┐
│  ⚔️ 比赛进行中... (第1局/BO3)            │
├─────────────────────────────────────────┤
│  🔵 蓝方：龙腾战队                       │
│  ━━━━━━━━━━━━━━ 65% ━━━━━━            │
│  🔴 红方：凤凰战队                       │
│  ━━━━━━━━ 35% ━━━━━━━━━━━━━           │
├─────────────────────────────────────────┤
│  战况播报：                              │
│  [12:34] 蓝方击杀第一条小龙！           │
│  [15:22] 红方推掉蓝方上路一塔！         │
│  [18:45] 蓝方中单击杀红方ADC！          │
│  [22:10] 双方在大龙坑爆发团战！         │
│                                         │
│  [等待比赛结果...]                       │
└─────────────────────────────────────────┘
```

### 6.2 比赛结果

```
┌─────────────────────────────────────────┐
│  🏆 比赛结束！                           │
│  蓝方 2:1 红方                           │
├─────────────────────────────────────────┤
│  第1局 (32分钟)                          │
│  🔵 蓝方 胜利 18-12                      │
│  击杀: 18 | 推塔: 9 | 小龙: 3 | 大龙: 2 │
│                                         │
│  第2局 (28分钟)                          │
│  🔴 红方 胜利 20-10                      │
│  击杀: 20 | 推塔: 10 | 小龙: 4 | 大龙: 1│
│                                         │
│  第3局 (35分钟)                          │
│  🔵 蓝方 胜利 22-15                      │
│  击杀: 22 | 推塔: 11 | 小龙: 3 | 大龙: 2│
├─────────────────────────────────────────┤
│  👑 MVP：李四 (蓝方打野)                 │
│  场均 KDA: 6/2/10 = 8.0                 │
│  场均伤害: 25,800                        │
├─────────────────────────────────────────┤
│  精彩回放：                              │
│  • 第1局：巨龙之魂拿下7/1/12的完美数据！ │
│  • 第3局：双方总击杀达到37次！           │
│                                         │
│  [查看详细数据] [继续赛程]               │
└─────────────────────────────────────────┘
```

---

## 七、赛后更新

```kotlin
object PostMatchUpdater {
    fun updateAfterMatch(match: Match, result: MatchResult) {
        // 更新选手数据
        updatePlayerStats(match, result)
        
        // 更新体力和士气
        updatePlayerCondition(match, result)
        
        // 获得经验
        grantExperience(match, result)
        
        // 更新英雄熟练度
        updateHeroMastery(match, result)
        
        // 更新团队数据
        updateTeamStats(match, result)
    }
    
    private fun updatePlayerStats(match: Match, result: MatchResult) {
        result.gameResults.forEach { game ->
            game.playerStats.forEach { stats ->
                val player = findPlayer(match, stats.playerId)
                if (player != null) {
                    // 更新生涯数据
                    player.careerStats = player.careerStats.copy(
                        totalMatches = player.careerStats.totalMatches + 1,
                        wins = player.careerStats.wins + 
                               if (isWinner(player, result)) 1 else 0
                    )
                }
            }
        }
    }
    
    private fun updatePlayerCondition(match: Match, result: MatchResult) {
        val allPlayers = match.blueTeam.players + match.redTeam.players
        
        allPlayers.forEach { player ->
            // 消耗体力
            player.stamina = (player.stamina - 10).coerceAtLeast(0)
            
            // 更新士气
            if (isWinner(player, result)) {
                player.morale = (player.morale + 5).coerceAtMost(100)
            } else {
                player.morale = (player.morale - 3).coerceAtLeast(0)
            }
        }
    }
}
```

---

## 八、测试用例

- [ ] 胜率计算：验证各因素权重正确
- [ ] 数据生成：验证数据合理性（KDA、经济等）
- [ ] MVP评选：验证高表现选手当选
- [ ] BO3/BO5：验证赛制正确执行
- [ ] 状态影响：验证体力/士气影响胜率
