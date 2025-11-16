package com.example.yjcy.managers.esports

import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.*
import kotlin.random.Random

/**
 * 数据生成器
 * 生成比赛数据（KDA、经济等）
 */
object DataGenerator {
    
    /**
     * 生成团队数据
     */
    fun generateTeamStats(
        blueTeam: Team,
        redTeam: Team,
        blueWins: Boolean,
        duration: Int
    ): Pair<TeamGameStats, TeamGameStats> {
        // 基础击杀数（根据时长）
        val baseKills = duration / 2
        
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
                kills = blueKills.coerceAtLeast(1),
                deaths = redKills.coerceAtLeast(1),
                assists = (blueKills * 2).coerceAtLeast(1),
                towers = blueTowers,
                dragons = blueDragons,
                barons = blueBarons,
                totalGold = blueGold,
                totalDamage = blueGold * 50L
            ),
            TeamGameStats(
                kills = redKills.coerceAtLeast(1),
                deaths = blueKills.coerceAtLeast(1),
                assists = (redKills * 2).coerceAtLeast(1),
                towers = redTowers,
                dragons = redDragons,
                barons = redBarons,
                totalGold = redGold,
                totalDamage = redGold * 50L
            )
        )
    }
    
    /**
     * 生成选手数据
     */
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
    
    /**
     * 生成个人数据
     */
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
            else -> (0.20 to 0.20)
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
            else -> 0.20
        }
        val goldEarned = (teamStats.totalGold * goldShare).toInt()
        
        // 伤害分配
        val damageShare = when (hero.type) {
            HeroType.MARKSMAN -> 0.30
            HeroType.MAGE -> 0.28
            HeroType.ASSASSIN -> 0.25
            HeroType.FIGHTER -> 0.20
            HeroType.TANK -> 0.12
            HeroType.SUPPORT -> 0.10
        }
        val damageDealt = (teamStats.totalDamage * damageShare).toLong()
        
        // 承受伤害
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
            else -> duration * 4
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
    
    /**
     * 计算MVP评分
     */
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
