package com.example.yjcy.data

import kotlin.random.Random

/**
 * 候选人生成器
 * 根据招聘配置自动生成符合条件的候选人
 */
class CandidateGenerator {
    
    companion object {
        // 姓名库
        private val FIRST_NAMES = listOf(
            "张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴",
            "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"
        )
        
        private val LAST_NAMES = listOf(
            "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
            "洋", "勇", "艳", "杰", "涛", "明", "超", "秀兰", "霞", "平",
            "刚", "桂英", "建华", "文", "华", "金凤", "素华", "春梅", "海燕", "雪梅"
        )
        
        // 技能库
        private val PROGRAMMER_SKILLS = mapOf(
            "Java" to (1..10),
            "Python" to (1..10),
            "JavaScript" to (1..10),
            "C++" to (1..10),
            "Go" to (1..10),
            "Kotlin" to (1..10),
            "Swift" to (1..10),
            "React" to (1..10),
            "Vue" to (1..10),
            "Android" to (1..10),
            "iOS" to (1..10),
            "数据库" to (1..10),
            "算法" to (1..10)
        )
        
        private val ARTIST_SKILLS = mapOf(
            "Photoshop" to (1..10),
            "3D Max" to (1..10),
            "Maya" to (1..10),
            "Blender" to (1..10),
            "Illustrator" to (1..10),
            "After Effects" to (1..10),
            "Premiere" to (1..10),
            "Substance" to (1..10),
            "ZBrush" to (1..10),
            "概念设计" to (1..10),
            "角色设计" to (1..10),
            "场景设计" to (1..10)
        )
        
        private val PLANNER_SKILLS = mapOf(
            "游戏设计" to (1..10),
            "关卡设计" to (1..10),
            "数值策划" to (1..10),
            "系统策划" to (1..10),
            "剧情策划" to (1..10),
            "用户体验" to (1..10),
            "数据分析" to (1..10),
            "项目管理" to (1..10),
            "文档撰写" to (1..10),
            "市场分析" to (1..10)
        )
        
        // 特殊技能描述
        private val SPECIAL_SKILLS = listOf(
            "团队协作能力强", "学习能力突出", "抗压能力强", "创新思维活跃",
            "沟通能力优秀", "责任心强", "执行力强", "适应能力强",
            "领导能力", "问题解决能力强", "细心认真", "时间管理能力强"
        )
    }
    
    /**
     * 根据招聘配置生成候选人列表
     */
    fun generateCandidates(
        config: RecruitmentConfig,
        count: Int = 5,
        existingCandidateIds: Set<Int> = emptySet()
    ): List<Candidate> {
        val candidates = mutableListOf<Candidate>()
        var attempts = 0
        val maxAttempts = count * 3 // 防止无限循环
        
        while (candidates.size < count && attempts < maxAttempts) {
            attempts++
            val candidate = generateSingleCandidate(config, existingCandidateIds)
            
            // 检查是否已存在相同ID的候选人
            if (candidate.id !in existingCandidateIds && 
                candidates.none { it.id == candidate.id }) {
                candidates.add(candidate)
            }
        }
        
        return candidates.sortedByDescending { calculateMatchScore(it, config) }
    }
    
    /**
     * 生成单个候选人
     */
    private fun generateSingleCandidate(
        config: RecruitmentConfig,
        existingIds: Set<Int>
    ): Candidate {
        val random = Random.Default
        
        // 生成唯一ID
        var candidateId: Int
        do {
            candidateId = random.nextInt(10000, 99999)
        } while (candidateId in existingIds)
        
        // 生成姓名
        val firstName = FIRST_NAMES.random()
        val lastName = LAST_NAMES.random()
        val name = "$firstName$lastName"
        
        // 根据配置生成属性
        val skillLevel = generateSkillLevel(config, random)
        val experience = generateExperience(config, skillLevel, random)
        val age = generateAge(config, experience, random)
        val salary = generateSalary(config, skillLevel, experience, random)
        
        // 生成技能
        val skills = generateSkills(config.positionType, skillLevel, random)
        
        // 生成特殊技能
        val specialSkills = generateSpecialSkills(random)
        
        // 生成可用性状态
        val availability = generateAvailability(random)
        
        return Candidate(
            id = candidateId,
            name = name,
            position = config.positionType,
            programmingSkill = if (config.positionType == "程序员") skillLevel else random.nextInt(1, 6),
            designSkill = if (config.positionType == "美术师") skillLevel else random.nextInt(1, 6),
            planningSkill = if (config.positionType == "策划师") skillLevel else random.nextInt(1, 6),
            soundSkill = if (config.positionType == "音效师") skillLevel else random.nextInt(1, 6),
            customerServiceSkill = if (config.positionType == "客服") skillLevel else random.nextInt(1, 6),
            experienceYears = experience,
            expectedSalary = salary,
            availabilityStatus = availability,
            recruitmentCost = salary / 10 + random.nextInt(500, 2000)
        )
    }
    
    /**
     * 生成技能等级
     */
    private fun generateSkillLevel(config: RecruitmentConfig, random: Random): Int {
        // 70% 概率生成配置范围内的技能等级
        return if (random.nextFloat() < 0.7f) {
            random.nextInt(config.minSkillLevel, config.maxSkillLevel + 1)
        } else {
            // 30% 概率生成稍微偏离范围的技能等级
            val deviation = random.nextInt(-2, 3)
            (config.minSkillLevel + (config.maxSkillLevel - config.minSkillLevel) / 2 + deviation)
                .coerceIn(1, 10)
        }
    }
    
    /**
     * 生成工作经验
     */
    private fun generateExperience(config: RecruitmentConfig, skillLevel: Int, random: Random): Int {
        // 基于技能等级生成合理的工作经验
        val baseExperience = when (skillLevel) {
            in 1..2 -> random.nextInt(0, 2)
            in 3..4 -> random.nextInt(1, 4)
            in 5..6 -> random.nextInt(2, 6)
            in 7..8 -> random.nextInt(4, 10)
            else -> random.nextInt(6, 15)
        }
        
        return baseExperience.coerceIn(config.minExperience, config.maxExperience)
    }
    
    /**
     * 生成年龄
     */
    private fun generateAge(config: RecruitmentConfig, experience: Int, random: Random): Int {
        // 基于工作经验生成合理年龄
        val baseAge = 22 + experience + random.nextInt(-2, 5)
        return baseAge.coerceIn(config.minAge, config.maxAge)
    }
    
    /**
     * 生成期望薪资
     */
    private fun generateSalary(
        config: RecruitmentConfig,
        skillLevel: Int,
        experience: Int,
        random: Random
    ): Int {
        // 基于技能等级和经验生成薪资
        val baseSalary = config.minSalary + 
            (config.maxSalary - config.minSalary) * (skillLevel + experience) / 20
        
        val variation = random.nextInt(-1000, 2000)
        return (baseSalary + variation).coerceIn(config.minSalary, config.maxSalary)
    }
    
    /**
     * 生成技能列表
     */
    private fun generateSkills(position: String, targetLevel: Int, random: Random): Map<String, Int> {
        val skillPool = when (position) {
            "程序员" -> PROGRAMMER_SKILLS
            "美术师" -> ARTIST_SKILLS
            "策划师" -> PLANNER_SKILLS
            else -> PROGRAMMER_SKILLS
        }
        
        val skills = mutableMapOf<String, Int>()
        val skillCount = random.nextInt(3, 7) // 每个候选人有3-6个技能
        
        val selectedSkills = skillPool.keys.shuffled(random).take(skillCount)
        
        selectedSkills.forEach { skillName ->
            // 主要技能接近目标等级，其他技能有所差异
            val isMainSkill = skills.size < 2
            val level = if (isMainSkill) {
                (targetLevel + random.nextInt(-1, 2)).coerceIn(1, 10)
            } else {
                random.nextInt(1, targetLevel + 2).coerceIn(1, 10)
            }
            skills[skillName] = level
        }
        
        return skills
    }
    
    /**
     * 生成特殊技能
     */
    private fun generateSpecialSkills(random: Random): String {
        val skillCount = random.nextInt(1, 4)
        return SPECIAL_SKILLS.shuffled(random).take(skillCount).joinToString("、")
    }
    
    /**
     * 生成可用性状态
     */
    private fun generateAvailability(random: Random): AvailabilityStatus {
        return when (random.nextInt(0, 100)) {
            in 0..79 -> AvailabilityStatus.AVAILABLE // 80% 可用
            in 80..94 -> AvailabilityStatus.INTERVIEWING // 15% 面试中
            else -> AvailabilityStatus.HIRED // 5% 已被雇佣
        }
    }
    
    /**
     * 计算候选人与配置的匹配度
     */
    private fun calculateMatchScore(candidate: Candidate, config: RecruitmentConfig): Float {
        var score = 0f
        var totalWeight = 0f
        
        // 职位匹配 (权重: 30%)
        if (candidate.position == config.positionType) {
            score += 30f
        }
        totalWeight += 30f
        
        // 技能等级匹配 (权重: 25%)
        val candidateSkillLevel = candidate.getSpecialtySkillLevel()
        if (candidateSkillLevel in config.minSkillLevel..config.maxSkillLevel) {
            score += 25f
        } else {
            // 部分匹配
            val deviation = minOf(
                kotlin.math.abs(candidateSkillLevel - config.minSkillLevel),
                kotlin.math.abs(candidateSkillLevel - config.maxSkillLevel)
            )
            score += maxOf(0f, 25f - deviation * 5f)
        }
        totalWeight += 25f
        
        // 薪资匹配 (权重: 20%)
        if (candidate.expectedSalary in config.minSalary..config.maxSalary) {
            score += 20f
        } else {
            // 薪资偏差惩罚
            val deviation = if (candidate.expectedSalary < config.minSalary) {
                config.minSalary - candidate.expectedSalary
            } else {
                candidate.expectedSalary - config.maxSalary
            }
            val penalty = (deviation.toFloat() / config.maxSalary) * 20f
            score += maxOf(0f, 20f - penalty)
        }
        totalWeight += 20f
        
        // 工作经验匹配 (权重: 15%)
        if (candidate.experienceYears in config.minExperience..config.maxExperience) {
            score += 15f
        } else {
            val deviation = minOf(
                kotlin.math.abs(candidate.experienceYears - config.minExperience),
                kotlin.math.abs(candidate.experienceYears - config.maxExperience)
            )
            score += maxOf(0f, 15f - deviation * 2f)
        }
        totalWeight += 15f
        
        // 可用性加分 (权重: 10%)
        when (candidate.availabilityStatus) {
            AvailabilityStatus.AVAILABLE -> score += 10f
            AvailabilityStatus.INTERVIEWING -> score += 5f
            AvailabilityStatus.HIRED -> score += 0f
        }
        totalWeight += 10f
        
        return (score / totalWeight).coerceIn(0f, 1f)
    }
    
    /**
     * 生成高质量候选人（用于紧急招聘）
     */
    fun generateHighQualityCandidates(
        config: RecruitmentConfig,
        count: Int = 3
    ): List<Candidate> {
        return generateCandidates(config, count).map { candidate ->
            // 提升技能等级
            val enhancedProgrammingSkill = minOf(10, candidate.programmingSkill + kotlin.random.Random.nextInt(1, 3))
            val enhancedDesignSkill = minOf(10, candidate.designSkill + kotlin.random.Random.nextInt(1, 3))
            val enhancedPlanningSkill = minOf(10, candidate.planningSkill + kotlin.random.Random.nextInt(1, 3))
            val enhancedSoundSkill = minOf(10, candidate.soundSkill + kotlin.random.Random.nextInt(1, 3))
            val enhancedCustomerServiceSkill = minOf(10, candidate.customerServiceSkill + kotlin.random.Random.nextInt(1, 3))
            
            candidate.copy(
                 programmingSkill = enhancedProgrammingSkill,
                 designSkill = enhancedDesignSkill,
                 planningSkill = enhancedPlanningSkill,
                 soundSkill = enhancedSoundSkill,
                 customerServiceSkill = enhancedCustomerServiceSkill,
                 availabilityStatus = AvailabilityStatus.AVAILABLE,
                 experienceYears = candidate.experienceYears + kotlin.random.Random.nextInt(1, 3)
             )
         }
    }
         }