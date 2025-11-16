package com.example.yjcy.managers.esports

import com.example.yjcy.data.esports.*
import kotlin.math.exp
import kotlin.random.Random

/**
 * 胜率计算器
 * 基于多个因素计算比赛胜率
 */
object WinRateCalculator {
    
    // 权重分配
    private const val PLAYER_ATTRIBUTES_WEIGHT = 0.40  // 选手属性 40%
    private const val COMPOSITION_WEIGHT = 0.25        // 阵容质量 25%
    private const val HERO_PROFICIENCY_WEIGHT = 0.20   // 英雄熟练度 20%
    private const val COUNTER_WEIGHT = 0.10            // 克制关系 10%
    private const val LUCK_WEIGHT = 0.05               // 随机波动 5%
    
    /**
     * 计算蓝方胜率
     * @return 蓝方胜率（0.0-1.0）
     */
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
    
    /**
     * 选手属性评分（-1.0 到 1.0）
     */
    private fun calculateAttributeScore(blueTeam: Team, redTeam: Team): Double {
        val blueAvg = blueTeam.players.map { it.attributes.overallRating() }.average()
        val redAvg = redTeam.players.map { it.attributes.overallRating() }.average()
        
        val diff = blueAvg - redAvg
        return (diff / 50.0).coerceIn(-1.0, 1.0)
    }
    
    /**
     * 阵容质量评分
     */
    private fun calculateCompositionScore(
        blueComp: TeamComposition,
        redComp: TeamComposition
    ): Double {
        val blueScore = blueComp.scores.overall
        val redScore = redComp.scores.overall
        
        val diff = blueScore - redScore
        return (diff / 50.0).coerceIn(-1.0, 1.0)
    }
    
    /**
     * 英雄熟练度评分
     */
    private fun calculateProficiencyScore(
        blueComp: TeamComposition,
        redComp: TeamComposition
    ): Double {
        val blueAvg = calculateTeamProficiency(blueComp)
        val redAvg = calculateTeamProficiency(redComp)
        
        val diff = blueAvg - redAvg
        return (diff / 50.0).coerceIn(-1.0, 1.0)
    }
    
    private fun calculateTeamProficiency(comp: TeamComposition): Double {
        return comp.players.mapIndexed { index, player ->
            val hero = comp.heroes.getOrNull(index)
            player.heroPool.find { it.heroId == hero?.id }?.proficiency ?: 50
        }.average()
    }
    
    /**
     * 克制关系评分
     */
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
    
    /**
     * Sigmoid函数（将评分转换为概率）
     */
    private fun sigmoid(x: Double): Double {
        return 1.0 / (1.0 + exp(-x * 5.0))
    }
    
    /**
     * 应用状态修正（体力、士气、伤病）
     */
    fun applyStatusModifiers(baseWinRate: Double, team: Team): Double {
        var modifiedRate = baseWinRate
        
        // 体力影响
        val avgStamina = team.players.map { it.stamina }.average()
        when {
            avgStamina < 30 -> modifiedRate *= 0.8  // -20%
            avgStamina < 50 -> modifiedRate *= 0.9  // -10%
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
        
        // 状态影响（平均状态值）
        val avgForm = team.players.map { it.form }.average()
        val formModifier = (avgForm - 80) / 200.0  // 80为基准，±20影响±10%
        modifiedRate *= (1.0 + formModifier)
        
        return modifiedRate.coerceIn(0.1, 0.9)
    }
}
