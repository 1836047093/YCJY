package com.example.yjcy.managers.esports

import com.example.yjcy.data.esports.*
import kotlin.random.Random

/**
 * 比赛模拟器
 * 模拟完整的比赛流程
 */
object MatchSimulator {
    
    /**
     * 模拟完整比赛
     */
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
            // 每局进行BP
            val bpSession = BPManager.executeAIBP(
                BPManager.startBPSession(match)
            )
            
            // 模拟这一局
            val gameResult = simulateGame(
                gameNumber,
                match.blueTeam,
                match.redTeam,
                bpSession
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
    
    /**
     * 模拟单局游戏
     */
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
        blueWinRate = WinRateCalculator.applyStatusModifiers(blueWinRate, blueTeam)
        
        // 决定胜负
        val blueWins = Random.nextDouble() < blueWinRate
        val winner = if (blueWins) TeamSide.BLUE else TeamSide.RED
        
        // 生成比赛时长（25-45分钟）
        val duration = Random.nextInt(25, 46)
        
        // 生成团队数据
        val (blueStats, redStats) = DataGenerator.generateTeamStats(
            blueTeam, redTeam, blueWins, duration
        )
        
        // 生成选手数据
        val playerStats = DataGenerator.generatePlayerStats(
            bpSession, blueStats, redStats, duration
        )
        
        android.util.Log.d("MatchSimulator", 
            "第${gameNumber}局结束: ${if (blueWins) "蓝方" else "红方"}获胜 (${duration}分钟)")
        
        return GameResult(
            gameNumber = gameNumber,
            winner = winner,
            duration = duration,
            blueTeamStats = blueStats,
            redTeamStats = redStats,
            playerStats = playerStats
        )
    }
    
    /**
     * 选择MVP
     */
    private fun selectMVP(
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
    
    /**
     * 生成精彩回放
     */
    private fun generateHighlights(gameResults: List<GameResult>): List<String> {
        val highlights = mutableListOf<String>()
        
        gameResults.forEachIndexed { index, game ->
            val gameNum = index + 1
            
            // 找到表现最好的选手
            val bestPlayer = game.playerStats.maxByOrNull { it.mvpScore }
            if (bestPlayer != null) {
                val hero = HeroManager.getHeroById(bestPlayer.heroId)!!
                if (bestPlayer.kda() >= 5.0) {
                    highlights.add(
                        "第${gameNum}局：${hero.name}拿下${bestPlayer.kills}/${bestPlayer.deaths}/${bestPlayer.assists}的完美数据！"
                    )
                }
            }
            
            // 团队击杀
            val blueKills = game.blueTeamStats.kills
            val redKills = game.redTeamStats.kills
            if (blueKills + redKills > 40) {
                highlights.add(
                    "第${gameNum}局：激烈的团战！双方总击杀达到${blueKills + redKills}次！"
                )
            }
            
            // 大龙争夺
            val totalBarons = game.blueTeamStats.barons + game.redTeamStats.barons
            if (totalBarons >= 2) {
                highlights.add(
                    "第${gameNum}局：双方围绕大龙展开激烈争夺！"
                )
            }
            
            // 五杀（超过5个击杀视为五杀）
            val pentakill = game.playerStats.find { it.kills >= 5 }
            if (pentakill != null) {
                val hero = HeroManager.getHeroById(pentakill.heroId)!!
                highlights.add(
                    "第${gameNum}局：${hero.name}完成五杀！势不可挡！"
                )
            }
            
            // 快速结束
            if (game.duration < 30) {
                highlights.add(
                    "第${gameNum}局：${game.duration}分钟快速结束，碾压局！"
                )
            }
        }
        
        return highlights.take(5)  // 最多5条
    }
    
    /**
     * 赛后更新（体力、士气、经验）
     */
    fun updateAfterMatch(match: Match, result: MatchResult) {
        val allPlayers = match.blueTeam.players + match.redTeam.players
        
        allPlayers.forEach { player ->
            // 消耗体力
            player.stamina = (player.stamina - 10).coerceAtLeast(0)
            
            // 更新士气
            if (result.winner.players.contains(player)) {
                player.morale = (player.morale + 5).coerceAtMost(100)
            } else {
                player.morale = (player.morale - 3).coerceAtLeast(0)
            }
            
            // 更新生涯数据
            player.careerStats = player.careerStats.copy(
                totalMatches = player.careerStats.totalMatches + result.gameResults.size,
                wins = player.careerStats.wins + 
                       if (result.winner.players.contains(player)) result.gameResults.size else 0
            )
            
            // MVP奖励
            if (player.id == result.mvp.id) {
                player.morale = (player.morale + 10).coerceAtMost(100)
                player.careerStats = player.careerStats.copy(
                    mvpCount = player.careerStats.mvpCount + 1
                )
            }
        }
        
        android.util.Log.d("MatchSimulator", "比赛结束，更新选手状态完成")
    }
}
