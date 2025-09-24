package com.example.yjcy.data

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

/**
 * 匹配度计算器
 * 提供精确的候选人与招聘配置匹配度计算
 */
class MatchScoreCalculator {
    
    companion object {
        // 权重配置
        private const val POSITION_WEIGHT = 0.30f      // 职位匹配权重
        private const val SKILL_LEVEL_WEIGHT = 0.25f   // 技能等级权重
        private const val SALARY_WEIGHT = 0.20f        // 薪资匹配权重
        private const val EXPERIENCE_WEIGHT = 0.15f    // 工作经验权重
        private const val AVAILABILITY_WEIGHT = 0.10f  // 可用性权重
        
        // 加分项权重
        private const val SPECIAL_SKILLS_BONUS = 0.05f // 特殊技能加分
        private const val SKILL_DIVERSITY_BONUS = 0.03f // 技能多样性加分
        private const val RECENT_ACTIVITY_BONUS = 0.02f // 最近活跃加分
    }
    
    /**
     * 计算候选人与招聘配置的匹配度
     * @param candidate 候选人
     * @param config 招聘配置
     * @return 匹配度分数 (0.0 - 1.0)
     */
    fun calculateMatchScore(candidate: Candidate, config: RecruitmentConfig): Float {
        var totalScore = 0f
        
        // 1. 职位匹配度
        totalScore += calculatePositionMatch(candidate, config) * POSITION_WEIGHT
        
        // 2. 技能等级匹配度
        totalScore += calculateSkillLevelMatch(candidate, config) * SKILL_LEVEL_WEIGHT
        
        // 3. 薪资匹配度
        totalScore += calculateSalaryMatch(candidate, config) * SALARY_WEIGHT
        
        // 4. 工作经验匹配度
        totalScore += calculateExperienceMatch(candidate, config) * EXPERIENCE_WEIGHT
        
        // 5. 可用性匹配度
        totalScore += calculateAvailabilityMatch(candidate) * AVAILABILITY_WEIGHT
        
        // 6. 加分项
        totalScore += calculateBonusScore(candidate, config)
        
        return totalScore.coerceIn(0f, 1f)
    }
    
    /**
     * 计算职位匹配度
     */
    private fun calculatePositionMatch(candidate: Candidate, config: RecruitmentConfig): Float {
        return if (candidate.position == config.positionType) 1f else 0f
    }
    
    /**
     * 计算技能等级匹配度
     */
    private fun calculateSkillLevelMatch(candidate: Candidate, config: RecruitmentConfig): Float {
        val candidateSkillLevel = candidate.getSpecialtySkillLevel()
        
        return when {
            candidateSkillLevel in config.minSkillLevel..config.maxSkillLevel -> {
                // 完全匹配，根据在范围内的位置给分
                val rangeSize = config.maxSkillLevel - config.minSkillLevel + 1
                val optimalLevel = (config.minSkillLevel + config.maxSkillLevel) / 2f
                val deviation = abs(candidateSkillLevel - optimalLevel)
                1f - (deviation / rangeSize) * 0.2f // 最多扣除20%
            }
            candidateSkillLevel < config.minSkillLevel -> {
                // 技能不足
                val deficit = config.minSkillLevel - candidateSkillLevel
                maxOf(0f, 1f - deficit * 0.15f) // 每级差距扣15%
            }
            else -> {
                // 技能过高（可能要求更高薪资）
                val excess = candidateSkillLevel - config.maxSkillLevel
                maxOf(0.6f, 1f - excess * 0.1f) // 每级差距扣10%，最低60%
            }
        }
    }
    
    /**
     * 计算薪资匹配度
     */
    private fun calculateSalaryMatch(candidate: Candidate, config: RecruitmentConfig): Float {
        val candidateSalary = candidate.expectedSalary
        
        return when {
            candidateSalary in config.minSalary..config.maxSalary -> {
                // 在预算范围内，越接近最低预算分数越高
                val range = config.maxSalary - config.minSalary
                val position = candidateSalary - config.minSalary
                1f - (position.toFloat() / range) * 0.3f // 最多扣除30%
            }
            candidateSalary < config.minSalary -> {
                // 期望薪资过低（可能技能不足或有其他问题）
                val deficit = config.minSalary - candidateSalary
                val deficitRatio = deficit.toFloat() / config.minSalary
                maxOf(0.3f, 1f - deficitRatio * 0.5f)
            }
            else -> {
                // 期望薪资过高
                val excess = candidateSalary - config.maxSalary
                val excessRatio = excess.toFloat() / config.maxSalary
                maxOf(0f, 1f - excessRatio * 0.8f)
            }
        }
    }
    
    /**
     * 计算工作经验匹配度
     */
    private fun calculateExperienceMatch(candidate: Candidate, config: RecruitmentConfig): Float {
        val candidateExp = candidate.experienceYears
        
        return when {
            candidateExp in config.minExperience..config.maxExperience -> {
                // 在要求范围内
                val range = config.maxExperience - config.minExperience + 1
                val optimalExp = (config.minExperience + config.maxExperience) / 2f
                val deviation = abs(candidateExp - optimalExp)
                1f - (deviation / range) * 0.25f
            }
            candidateExp < config.minExperience -> {
                // 经验不足
                val deficit = config.minExperience - candidateExp
                maxOf(0.2f, 1f - deficit * 0.2f)
            }
            else -> {
                // 经验过多（可能过度资格）
                val excess = candidateExp - config.maxExperience
                maxOf(0.7f, 1f - excess * 0.1f)
            }
        }
    }
    
    /**
     * 计算可用性匹配度
     */
    private fun calculateAvailabilityMatch(candidate: Candidate): Float {
        return when (candidate.availabilityStatus) {
            AvailabilityStatus.AVAILABLE -> 1f
            AvailabilityStatus.INTERVIEWING -> 0.6f
            AvailabilityStatus.HIRED -> 0.2f
        }
    }
    
    /**
     * 计算加分项
     */
    private fun calculateBonusScore(candidate: Candidate, config: RecruitmentConfig): Float {
        var bonus = 0f
        
        // 特殊技能加分
        bonus += calculateSpecialSkillsBonus(candidate, config)
        
        // 技能多样性加分
        bonus += calculateSkillDiversityBonus(candidate)
        
        // 最近活跃加分
        bonus += calculateRecentActivityBonus(candidate)
        
        return bonus
    }
    
    /**
     * 计算特殊技能加分
     */
    private fun calculateSpecialSkillsBonus(candidate: Candidate, config: RecruitmentConfig): Float {
        // 由于Candidate类没有specialSkills属性，暂时返回0
        return 0f
    }
    
    /**
     * 计算技能多样性加分
     */
    private fun calculateSkillDiversityBonus(candidate: Candidate): Float {
        // 基于候选人的各项技能计算多样性
        val skills = listOf(
            candidate.programmingSkill,
            candidate.designSkill,
            candidate.planningSkill,
            candidate.soundSkill,
            candidate.customerServiceSkill
        )
        
        val skillCount = skills.count { it > 0 }
        val averageSkillLevel = skills.average().toFloat()
        
        // 技能数量和平均水平的综合评分
        val diversityScore = when {
            skillCount >= 5 && averageSkillLevel >= 6f -> 1f
            skillCount >= 4 && averageSkillLevel >= 5f -> 0.8f
            skillCount >= 3 && averageSkillLevel >= 4f -> 0.6f
            skillCount >= 2 && averageSkillLevel >= 3f -> 0.4f
            else -> 0.2f
        }
        
        return diversityScore * SKILL_DIVERSITY_BONUS
    }
    
    /**
     * 计算最近活跃加分
     */
    private fun calculateRecentActivityBonus(candidate: Candidate): Float {
        // 基于候选人的创建时间计算活跃度
        val daysSinceCreated = (System.currentTimeMillis() - candidate.createdAt) / (24 * 60 * 60 * 1000)
        
        return when {
            daysSinceCreated <= 1 -> RECENT_ACTIVITY_BONUS
            daysSinceCreated <= 3 -> RECENT_ACTIVITY_BONUS * 0.8f
            daysSinceCreated <= 7 -> RECENT_ACTIVITY_BONUS * 0.6f
            daysSinceCreated <= 14 -> RECENT_ACTIVITY_BONUS * 0.4f
            daysSinceCreated <= 30 -> RECENT_ACTIVITY_BONUS * 0.2f
            else -> 0f
        }
    }
    
    /**
     * 批量计算匹配度
     */
    fun calculateBatchMatchScores(
        candidates: List<Candidate>,
        config: RecruitmentConfig
    ): List<Pair<Candidate, Float>> {
        return candidates.map { candidate ->
            candidate to calculateMatchScore(candidate, config)
        }.sortedByDescending { it.second }
    }
    
    /**
     * 获取匹配度详细分析
     */
    fun getDetailedMatchAnalysis(
        candidate: Candidate,
        config: RecruitmentConfig
    ): MatchAnalysis {
        val positionScore = calculatePositionMatch(candidate, config)
        val skillLevelScore = calculateSkillLevelMatch(candidate, config)
        val salaryScore = calculateSalaryMatch(candidate, config)
        val experienceScore = calculateExperienceMatch(candidate, config)
        val availabilityScore = calculateAvailabilityMatch(candidate)
        val bonusScore = calculateBonusScore(candidate, config)
        
        val totalScore = positionScore * POSITION_WEIGHT +
                skillLevelScore * SKILL_LEVEL_WEIGHT +
                salaryScore * SALARY_WEIGHT +
                experienceScore * EXPERIENCE_WEIGHT +
                availabilityScore * AVAILABILITY_WEIGHT +
                bonusScore
        
        return MatchAnalysis(
            totalScore = totalScore.coerceIn(0f, 1f),
            positionMatch = positionScore,
            skillLevelMatch = skillLevelScore,
            salaryMatch = salaryScore,
            experienceMatch = experienceScore,
            availabilityMatch = availabilityScore,
            bonusScore = bonusScore,
            recommendation = generateRecommendation(totalScore, candidate, config)
        )
    }
    
    /**
     * 生成推荐建议
     */
    private fun generateRecommendation(
        score: Float,
        candidate: Candidate,
        config: RecruitmentConfig
    ): String {
        return when {
            score >= 0.9f -> "强烈推荐：候选人完全符合要求，建议立即雇佣"
            score >= 0.8f -> "推荐：候选人基本符合要求，可以考虑雇佣"
            score >= 0.7f -> "一般：候选人部分符合要求，需要进一步评估"
            score >= 0.6f -> "谨慎：候选人勉强符合要求，建议谨慎考虑"
            else -> "不推荐：候选人不符合基本要求，不建议雇佣"
        }
    }
}

/**
 * 匹配度详细分析结果
 */
data class MatchAnalysis(
    val totalScore: Float,
    val positionMatch: Float,
    val skillLevelMatch: Float,
    val salaryMatch: Float,
    val experienceMatch: Float,
    val availabilityMatch: Float,
    val bonusScore: Float,
    val recommendation: String
) {
    /**
     * 获取总分百分比
     */
    fun getTotalPercentage(): Int {
        return (totalScore * 100).toInt()
    }
    
    /**
     * 获取匹配等级
     */
    fun getMatchGrade(): String {
        return when {
            totalScore >= 0.9f -> "S"
            totalScore >= 0.8f -> "A"
            totalScore >= 0.7f -> "B"
            totalScore >= 0.6f -> "C"
            else -> "D"
        }
    }
    
    /**
     * 获取最强项
     */
    fun getStrongestAspect(): String {
        val scores = mapOf(
            "职位匹配" to positionMatch,
            "技能等级" to skillLevelMatch,
            "薪资匹配" to salaryMatch,
            "工作经验" to experienceMatch,
            "可用性" to availabilityMatch
        )
        return scores.maxByOrNull { it.value }?.key ?: "无"
    }
    
    /**
     * 获取最弱项
     */
    fun getWeakestAspect(): String {
        val scores = mapOf(
            "职位匹配" to positionMatch,
            "技能等级" to skillLevelMatch,
            "薪资匹配" to salaryMatch,
            "工作经验" to experienceMatch,
            "可用性" to availabilityMatch
        )
        return scores.minByOrNull { it.value }?.key ?: "无"
    }
    
    /**
     * 比较操作符
     */
    operator fun compareTo(other: MatchAnalysis): Int {
        return totalScore.compareTo(other.totalScore)
    }
}