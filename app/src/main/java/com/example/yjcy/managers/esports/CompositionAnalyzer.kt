package com.example.yjcy.managers.esports

import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.*

/**
 * 阵容分析器
 * 分析队伍阵容的质量和类型
 */
object CompositionAnalyzer {
    
    /**
     * 分析阵容
     */
    fun analyzeComposition(
        heroes: List<MobaHero>,
        players: List<EsportsPlayer>
    ): TeamComposition {
        val scores = calculateScores(heroes, players)
        return TeamComposition(heroes, players, scores)
    }
    
    /**
     * 计算阵容评分
     */
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
    
    /**
     * 计算协同性
     */
    private fun calculateSynergy(heroes: List<MobaHero>): Int {
        var synergy = 50  // 基础50分
        
        // 位置完整性（5个不同位置）
        val positionSet = mutableSetOf<HeroPosition>()
        heroes.forEach { positionSet.add(it.position) }
        if (positionSet.size == 5) {
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
    
    /**
     * 计算熟练度加成
     */
    private fun calculateProficiencyBonus(
        heroes: List<MobaHero>,
        players: List<EsportsPlayer>
    ): Double {
        var totalProficiency = 0.0
        var count = 0
        
        heroes.forEachIndexed { index, hero ->
            val player = players.getOrNull(index)
            val mastery = player?.heroPool?.find { it.heroId == hero.id }
            if (mastery != null) {
                totalProficiency += mastery.proficiency
                count++
            }
        }
        
        return if (count > 0) totalProficiency / count / 100.0 else 0.5
    }
    
    /**
     * 识别阵容类型
     */
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
