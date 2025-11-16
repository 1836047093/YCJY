package com.example.yjcy.managers.esports

import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.*
import kotlin.random.Random

/**
 * BP管理器
 * 管理Ban/Pick流程
 */
object BPManager {
    
    // 标准BO5 BP流程（16步）
    private val standardBPSequence = listOf(
        BPPhase(1, BPAction.BAN, TeamSide.BLUE),
        BPPhase(2, BPAction.BAN, TeamSide.RED),
        BPPhase(3, BPAction.BAN, TeamSide.BLUE),
        BPPhase(4, BPAction.BAN, TeamSide.RED),
        BPPhase(5, BPAction.BAN, TeamSide.BLUE),
        BPPhase(6, BPAction.BAN, TeamSide.RED),
        BPPhase(7, BPAction.PICK, TeamSide.BLUE),
        BPPhase(8, BPAction.PICK, TeamSide.RED),
        BPPhase(9, BPAction.PICK, TeamSide.RED),
        BPPhase(10, BPAction.PICK, TeamSide.BLUE),
        BPPhase(11, BPAction.PICK, TeamSide.RED),
        BPPhase(12, BPAction.PICK, TeamSide.BLUE),
        BPPhase(13, BPAction.PICK, TeamSide.BLUE),
        BPPhase(14, BPAction.PICK, TeamSide.RED),
        BPPhase(15, BPAction.PICK, TeamSide.RED),
        BPPhase(16, BPAction.PICK, TeamSide.BLUE)
    )
    
    /**
     * 开始BP流程
     */
    fun startBPSession(match: Match): BPSession {
        return BPSession(
            matchId = match.id,
            blueTeam = match.blueTeam,
            redTeam = match.redTeam
        )
    }
    
    /**
     * 执行AI自动BP
     */
    fun executeAIBP(session: BPSession): BPSession {
        var currentSession = session
        
        standardBPSequence.forEach { phase ->
            val availableHeroes = getAvailableHeroes(currentSession)
            
            when (phase.action) {
                BPAction.BAN -> {
                    val bannedHero = selectBan(currentSession, phase.side, availableHeroes)
                    if (bannedHero != null) {
                        if (phase.side == TeamSide.BLUE) {
                            currentSession.blueBans.add(bannedHero.id)
                        } else {
                            currentSession.redBans.add(bannedHero.id)
                        }
                    }
                }
                
                BPAction.PICK -> {
                    val pickedHero = selectPick(currentSession, phase.side, availableHeroes)
                    if (pickedHero != null) {
                        val team = if (phase.side == TeamSide.BLUE) 
                            currentSession.blueTeam else currentSession.redTeam
                        val position = determineNextPosition(currentSession, phase.side)
                        val player = team.players.find { it.position == position }
                        
                        if (player != null) {
                            val picked = PickedHero(
                                heroId = pickedHero.id,
                                playerId = player.id,
                                position = position,
                                proficiency = player.heroPool.find { 
                                    it.heroId == pickedHero.id 
                                }?.proficiency ?: 50
                            )
                            
                            if (phase.side == TeamSide.BLUE) {
                                currentSession.bluePicks.add(picked)
                            } else {
                                currentSession.redPicks.add(picked)
                            }
                        }
                    }
                }
            }
            
            currentSession = currentSession.copy(
                currentPhase = currentSession.currentPhase + 1
            )
        }
        
        return currentSession.copy(isCompleted = true)
    }
    
    /**
     * 选择要Ban的英雄
     */
    private fun selectBan(
        session: BPSession,
        side: TeamSide,
        availableHeroes: List<MobaHero>
    ): MobaHero? {
        val team = if (side == TeamSide.BLUE) session.blueTeam else session.redTeam
        val enemyTeam = if (side == TeamSide.BLUE) session.redTeam else session.blueTeam
        
        val targetHeroes = mutableListOf<Pair<MobaHero, Double>>()
        
        availableHeroes.forEach { hero ->
            var banValue = 0.0
            
            // 1. 检查是否是对方招牌英雄
            enemyTeam.players.forEach { player ->
                val mastery = player.heroPool.find { it.heroId == hero.id }
                if (mastery != null && mastery.proficiency > 85) {
                    banValue += 100.0
                }
            }
            
            // 2. 版本强度（禁用率高）
            if (hero.banRate > 20.0) {
                banValue += hero.banRate * 2
            }
            
            // 3. 克制关系（克制我方已选英雄）
            val myPicks = if (side == TeamSide.BLUE) session.bluePicks else session.redPicks
            myPicks.forEach { pick ->
                if (hero.counters.contains(pick.heroId)) {
                    banValue += 30.0
                }
            }
            
            if (banValue > 0) {
                targetHeroes.add(hero to banValue)
            }
        }
        
        // 返回价值最高的，如果没有则随机
        return targetHeroes.maxByOrNull { it.second }?.first
            ?: availableHeroes.randomOrNull()
    }
    
    /**
     * 选择要Pick的英雄
     */
    private fun selectPick(
        session: BPSession,
        side: TeamSide,
        availableHeroes: List<MobaHero>
    ): MobaHero? {
        val team = if (side == TeamSide.BLUE) session.blueTeam else session.redTeam
        val position = determineNextPosition(session, side)
        val player = team.players.find { it.position == position } ?: return null
        
        val candidateHeroes = availableHeroes.filter { it.position == position }
        val scoredHeroes = mutableListOf<Pair<MobaHero, Double>>()
        
        candidateHeroes.forEach { hero ->
            var pickValue = 0.0
            
            // 1. 选手熟练度
            val mastery = player.heroPool.find { it.heroId == hero.id }
            if (mastery != null) {
                pickValue += mastery.proficiency * 1.5
            }
            
            // 2. 克制关系
            val enemyPicks = if (side == TeamSide.BLUE) session.redPicks else session.bluePicks
            enemyPicks.forEach { enemyPick ->
                if (hero.counters.contains(enemyPick.heroId)) {
                    pickValue += 50.0
                }
                if (hero.counteredBy.contains(enemyPick.heroId)) {
                    pickValue -= 30.0
                }
            }
            
            // 3. 阵容平衡
            val myPicks = if (side == TeamSide.BLUE) session.bluePicks else session.redPicks
            val balanceScore = calculateBalanceScore(myPicks, hero)
            pickValue += balanceScore
            
            scoredHeroes.add(hero to pickValue)
        }
        
        return scoredHeroes.maxByOrNull { it.second }?.first
            ?: candidateHeroes.randomOrNull()
    }
    
    /**
     * 计算阵容平衡性评分
     */
    private fun calculateBalanceScore(
        currentPicks: List<PickedHero>,
        newHero: MobaHero
    ): Double {
        val currentHeroes = currentPicks.mapNotNull { 
            HeroManager.getHeroById(it.heroId) 
        }
        
        var score = 0.0
        
        val totalTankiness = currentHeroes.sumOf { it.strength.tankiness }
        val totalDamage = currentHeroes.sumOf { it.strength.damage }
        val totalControl = currentHeroes.sumOf { it.strength.control }
        
        // 缺少前排时，坦克英雄加分
        if (totalTankiness < 150 && newHero.strength.tankiness > 70) {
            score += 40.0
        }
        
        // 缺少伤害时，输出英雄加分
        if (totalDamage < 200 && newHero.strength.damage > 80) {
            score += 30.0
        }
        
        // 缺少控制时，控制英雄加分
        if (totalControl < 150 && newHero.strength.control > 60) {
            score += 25.0
        }
        
        return score
    }
    
    /**
     * 获取可用英雄（未被Ban/Pick）
     */
    private fun getAvailableHeroes(session: BPSession): List<MobaHero> {
        val bannedIds = session.blueBans + session.redBans
        val pickedIds = session.bluePicks.map { it.heroId } + 
                       session.redPicks.map { it.heroId }
        val unavailableIds = bannedIds + pickedIds
        
        return HeroManager.getAllHeroes().filter { it.id !in unavailableIds }
    }
    
    /**
     * 确定下一个要选择的位置
     */
    private fun determineNextPosition(
        session: BPSession,
        side: TeamSide
    ): HeroPosition {
        val picks = if (side == TeamSide.BLUE) 
            session.bluePicks else session.redPicks
        
        val pickedPositions = mutableSetOf<HeroPosition>()
        picks.forEach { pickedPositions.add(it.position) }
        val allPositions = HeroPosition.values().toList()
        
        return allPositions.first { it !in pickedPositions }
    }
}
