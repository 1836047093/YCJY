package com.example.yjcy.data

import kotlin.math.*
import kotlin.random.Random

/**
 * 招聘算法核心类
 * 负责根据招聘条件生成候选人，实现薪资影响招聘质量的核心逻辑
 */
object RecruitmentAlgorithm {
    
    // 算法配置常量
    private const val BASE_GENERATION_RATE = 0.3f // 基础生成概率
    private const val MAX_DAILY_CANDIDATES = 3 // 每日最大候选人数
    private const val SALARY_INFLUENCE_FACTOR = 0.0001f // 薪资影响因子
    private const val SKILL_VARIANCE_RANGE = 2 // 技能等级变化范围
    
    // 薪资等级划分
    private val SALARY_TIERS = mapOf(
        "VERY_LOW" to 0..4999,
        "LOW" to 5000..7999,
        "MEDIUM" to 8000..11999,
        "HIGH" to 12000..17999,
        "VERY_HIGH" to 18000..Int.MAX_VALUE
    )
    
    /**
     * 为招聘任务生成候选人
     * 这是算法的主入口点
     */
    fun generateCandidates(recruitment: Recruitment): List<Candidate> {
        val candidates = mutableListOf<Candidate>()
        
        // 计算今日候选人生成数量
        val candidateCount = calculateDailyCandidateCount(recruitment)
        
        repeat(candidateCount) {
            val candidate = generateSingleCandidate(recruitment)
            if (candidate != null) {
                candidates.add(candidate)
            }
        }
        
        return candidates
    }
    
    /**
     * 计算每日候选人生成数量
     * 基于招聘条件和随机因素
     */
    private fun calculateDailyCandidateCount(recruitment: Recruitment): Int {
        val baseCount = Random.nextInt(0, MAX_DAILY_CANDIDATES + 1)
        val salaryBonus = calculateSalaryBonus(recruitment.salary)
        val positionEnum = recruitment.position
        val positionFactor = getPositionPopularity(positionEnum)
        
        val adjustedCount = (baseCount * (1 + salaryBonus) * positionFactor).roundToInt()
        return adjustedCount.coerceIn(0, MAX_DAILY_CANDIDATES)
    }
    
    /**
     * 生成单个候选人
     */
    private fun generateSingleCandidate(recruitment: Recruitment): Candidate? {
        // 计算生成概率
        val generationProbability = calculateGenerationProbability(recruitment)
        
        if (Random.nextFloat() > generationProbability) {
            return null
        }
        
        return CandidateGenerator.generateCandidate(
            recruitmentId = recruitment.id,
            position = recruitment.position.displayName,
            targetSkillLevel = recruitment.skillLevel,
            salaryBudget = recruitment.salary
        )
    }
    
    /**
     * 计算候选人生成概率
     * 核心算法：薪资越高，生成概率越大，但有上限
     */
    private fun calculateGenerationProbability(recruitment: Recruitment): Float {
        val salaryTier = getSalaryTier(recruitment.salary)
        val skillDemand = recruitment.skillLevel
        
        // 基础概率根据薪资等级调整
        val baseProbability = when (salaryTier) {
            "VERY_HIGH" -> 0.8f
            "HIGH" -> 0.6f
            "MEDIUM" -> 0.4f
            "LOW" -> 0.25f
            "VERY_LOW" -> 0.15f
            else -> 0.2f
        }
        
        // 技能要求调整（要求越高，概率越低）
        val skillAdjustment = when (skillDemand) {
            5 -> -0.2f
            4 -> -0.1f
            3 -> 0f
            2 -> 0.05f
            1 -> 0.1f
            else -> 0f
        }
        
        // 职位热门度调整
        val positionEnum = recruitment.position
        val positionAdjustment = getPositionPopularity(positionEnum) - 1f
        
        return (baseProbability + skillAdjustment + positionAdjustment * 0.1f)
            .coerceIn(0.05f, 0.9f)
    }
    
    /**
     * 计算候选人技能等级
     * 薪资越高，生成高技能候选人的概率越大
     */
    fun calculateCandidateSkillLevel(recruitment: Recruitment): Int {
        val targetSkill = recruitment.skillLevel
        val salaryInfluence = calculateSalaryInfluenceOnSkill(recruitment.salary)
        
        // 基于正态分布生成技能等级
        val skillVariance = when (getSalaryTier(recruitment.salary)) {
            "VERY_HIGH" -> 0.8 // 高薪资，技能更集中在高端
            "HIGH" -> 1.0
            "MEDIUM" -> 1.2
            "LOW" -> 1.5
            "VERY_LOW" -> 2.0 // 低薪资，技能分布更分散
            else -> 1.5
        }
        
        // 使用正态分布生成技能等级
        val randomSkill = Random.nextGaussian() * skillVariance + targetSkill + salaryInfluence
        
        return randomSkill.roundToInt().coerceIn(1, 5)
    }
    
    /**
     * 计算薪资对技能的影响
     */
    private fun calculateSalaryInfluenceOnSkill(salary: Int): Double {
        return when (getSalaryTier(salary)) {
            "VERY_HIGH" -> 1.0 // +1技能等级倾向
            "HIGH" -> 0.5
            "MEDIUM" -> 0.0
            "LOW" -> -0.5
            "VERY_LOW" -> -1.0 // -1技能等级倾向
            else -> 0.0
        }
    }
    
    /**
     * 生成候选人的具体技能分布
     */
    fun generateSkillDistribution(
        position: RecruitmentPosition,
        overallSkillLevel: Int,
        salary: Int
    ): SkillDistribution {
        val skills = mutableMapOf<String, Int>()
        val totalPoints = overallSkillLevel * 20 // 每个技能等级对应20点技能点
        
        // 根据职位确定主要技能
        val primarySkills = getPrimarySkillsForPosition(position)
        val secondarySkills = getSecondarySkillsForPosition(position)
        
        // 分配技能点
        var remainingPoints = totalPoints
        
        // 优先分配主要技能（60%的点数）
        val primaryPoints = (totalPoints * 0.6).toInt()
        primarySkills.forEach { skill ->
            val points = (primaryPoints / primarySkills.size) + Random.nextInt(-5, 6)
            skills[skill] = points.coerceIn(1, 100)
            remainingPoints -= skills[skill]!!
        }
        
        // 分配次要技能（40%的点数）
        secondarySkills.forEach { skill ->
            val points = (remainingPoints / secondarySkills.size) + Random.nextInt(-3, 4)
            skills[skill] = points.coerceIn(1, 80)
        }
        
        return SkillDistribution(
            programmingSkill = skills["programming"] ?: Random.nextInt(20, 60),
            designSkill = skills["design"] ?: Random.nextInt(20, 60),
            planningSkill = skills["planning"] ?: Random.nextInt(20, 60),
            marketingSkill = skills["marketing"] ?: Random.nextInt(20, 60),
            researchSkill = skills["research"] ?: Random.nextInt(20, 60)
        )
    }
    
    /**
     * 生成候选人期望薪资
     * 基于技能等级和市场行情
     */
    fun calculateExpectedSalary(
        skillLevel: Int,
        position: RecruitmentPosition,
        recruitmentSalary: Int
    ): Int {
        // 基础薪资期望
        val baseSalary = when (skillLevel) {
            5 -> 15000
            4 -> 12000
            3 -> 8000
            2 -> 5000
            1 -> 3000
            else -> 5000
        }
        
        // 职位薪资调整
        val positionMultiplier = when (position) {
            RecruitmentPosition.PROGRAMMER -> 1.2
            RecruitmentPosition.DESIGNER -> 1.0
            RecruitmentPosition.ARTIST -> 1.1
            RecruitmentPosition.SOUND_ENGINEER -> 0.9
            RecruitmentPosition.TESTER -> 1.15
        }
        
        // 市场行情调整（基于招聘薪资）
        val marketAdjustment = if (recruitmentSalary > baseSalary) {
            1.0 + (recruitmentSalary - baseSalary) * 0.0001
        } else {
            1.0
        }
        
        // 随机波动（±20%）
        val randomFactor = Random.nextDouble(0.8, 1.2)
        
        val expectedSalary = (baseSalary * positionMultiplier * marketAdjustment * randomFactor).toInt()
        
        return expectedSalary.coerceIn(3000, 25000)
    }
    
    /**
     * 生成候选人工作经验
     */
    fun generateExperience(skillLevel: Int): Int {
        val baseExperience = when (skillLevel) {
            5 -> Random.nextInt(5, 12)
            4 -> Random.nextInt(3, 8)
            3 -> Random.nextInt(1, 5)
            2 -> Random.nextInt(0, 3)
            1 -> Random.nextInt(0, 2)
            else -> Random.nextInt(0, 3)
        }
        
        return baseExperience.coerceIn(0, 15)
    }
    
    /**
     * 生成候选人特殊能力
     */
    fun generateSpecialAbilities(
        position: RecruitmentPosition,
        skillLevel: Int
    ): List<String> {
        val abilities = mutableListOf<String>()
        val abilityPool = getAbilityPoolForPosition(position)
        
        // 技能等级越高，特殊能力越多
        val abilityCount = when (skillLevel) {
            5 -> Random.nextInt(2, 4)
            4 -> Random.nextInt(1, 3)
            3 -> Random.nextInt(1, 2)
            2 -> Random.nextInt(0, 2)
            1 -> Random.nextInt(0, 1)
            else -> 0
        }
        
        repeat(abilityCount) {
            val ability = abilityPool.randomOrNull()
            if (ability != null && !abilities.contains(ability)) {
                abilities.add(ability)
            }
        }
        
        return abilities
    }
    
    // ==================== 辅助方法 ====================
    
    private fun getSalaryTier(salary: Int): String {
        return SALARY_TIERS.entries.find { salary in it.value }?.key ?: "MEDIUM"
    }
    
    private fun calculateSalaryBonus(salary: Int): Float {
        return (salary * SALARY_INFLUENCE_FACTOR).coerceAtMost(0.5f)
    }
    
    private fun getPositionPopularity(position: RecruitmentPosition): Float {
        return when (position) {
            RecruitmentPosition.PROGRAMMER -> 1.2f // 程序员需求高
            RecruitmentPosition.DESIGNER -> 1.0f
            RecruitmentPosition.ARTIST -> 0.9f
            RecruitmentPosition.SOUND_ENGINEER -> 0.8f
            RecruitmentPosition.TESTER -> 0.7f // 测试员相对稀少
        }
    }
    
    private fun getPrimarySkillsForPosition(position: RecruitmentPosition): List<String> {
        return when (position) {
            RecruitmentPosition.PROGRAMMER -> listOf("programming")
            RecruitmentPosition.DESIGNER -> listOf("design")
            RecruitmentPosition.ARTIST -> listOf("art")
            RecruitmentPosition.SOUND_ENGINEER -> listOf("music")
            RecruitmentPosition.TESTER -> listOf("service")
        }
    }
    
    private fun getSecondarySkillsForPosition(position: RecruitmentPosition): List<String> {
        return when (position) {
            RecruitmentPosition.PROGRAMMER -> listOf("design", "art", "service")
            RecruitmentPosition.DESIGNER -> listOf("programming", "art", "music")
            RecruitmentPosition.ARTIST -> listOf("design", "music", "programming")
            RecruitmentPosition.SOUND_ENGINEER -> listOf("art", "design", "programming")
            RecruitmentPosition.TESTER -> listOf("programming", "design", "art")
        }
    }
    
    private fun getAbilityPoolForPosition(position: RecruitmentPosition): List<String> {
        return when (position) {
            RecruitmentPosition.PROGRAMMER -> listOf(
                "全栈开发", "算法优化", "系统架构", "代码重构", "性能调优",
                "开源贡献", "技术领导", "跨平台开发", "数据库设计", "API设计"
            )
            RecruitmentPosition.DESIGNER -> listOf(
                "UI/UX设计", "品牌设计", "动画制作", "原型设计", "用户研究",
                "视觉传达", "交互设计", "设计系统", "创意思维", "色彩搭配"
            )
            RecruitmentPosition.ARTIST -> listOf(
                "角色设计", "场景绘制", "概念设计", "插画创作", "动画制作",
                "美术风格", "色彩理论", "构图技巧", "数字绘画", "创意表达"
            )
            RecruitmentPosition.SOUND_ENGINEER -> listOf(
                "音效制作", "背景音乐", "音频编辑", "声音设计", "混音技术",
                "音乐创作", "录音技术", "音频优化", "声学处理", "创意音效"
            )
            RecruitmentPosition.TESTER -> listOf(
                "功能测试", "性能测试", "用户体验", "Bug发现", "质量保证",
                "自动化测试", "回归测试", "兼容性测试", "安全测试", "测试文档"
            )
        }
    }
}

/**
 * 技能分布数据类
 */
data class SkillDistribution(
    val programmingSkill: Int,
    val designSkill: Int,
    val planningSkill: Int,
    val marketingSkill: Int,
    val researchSkill: Int
) {
    val totalSkillPoints: Int
        get() = programmingSkill + designSkill + planningSkill + marketingSkill + researchSkill
    
    val averageSkillLevel: Double
        get() = totalSkillPoints / 5.0
}

/**
 * 扩展函数：生成正态分布随机数
 */
fun Random.nextGaussian(): Double {
    var u = 0.0
    var v = 0.0
    while (u == 0.0) u = nextDouble() // Converting [0,1) to (0,1)
    while (v == 0.0) v = nextDouble()
    return sqrt(-2.0 * ln(u)) * cos(2.0 * PI * v)
}