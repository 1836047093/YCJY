package com.example.yjcy.service

import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.data.FilterCriteria
import com.example.yjcy.data.SkillConstants
import kotlin.random.Random

/**
 * 人才市场服务类
 * 负责生成候选人数据和提供筛选功能
 */
class TalentMarketService {
    
    companion object {
        // 候选人姓名池
        private val CANDIDATE_NAMES = listOf(
            "张伟", "李娜", "王强", "刘敏", "陈杰", "杨丽", "赵磊", "孙静", "周涛", "吴萍",
            "郑浩", "王芳", "李明", "张丽", "刘伟", "陈静", "杨强", "赵敏", "孙杰", "周萍",
            "吴涛", "郑丽", "王浩", "李静", "张强", "刘丽", "陈伟", "杨敏", "赵杰", "孙萍"
        )
        
        // 职位列表
        private val POSITIONS = listOf("程序员", "策划师", "美术师", "音效师", "客服")
        
        // 薪资基础值（根据技能等级）
        private val SALARY_BASE = mapOf(
            1 to 3000, 2 to 4000, 3 to 6000, 4 to 9000, 5 to 15000
        )
    }
    
    /**
     * 生成随机候选人列表
     */
    fun generateCandidates(count: Int = 20): List<TalentCandidate> {
        return (1..count).map { generateRandomCandidate(it.toString()) }
    }
    
    /**
     * 生成单个随机候选人
     */
    private fun generateRandomCandidate(id: String): TalentCandidate {
        val position = POSITIONS.random()
        val skills = generateSkillsForPosition(position)
        val maxSkill = skills.maxOf { it }
        val baseSalary = SALARY_BASE[maxSkill] ?: 3000
        val salary = baseSalary + Random.nextInt(-500, 1000)
        
        return TalentCandidate(
            id = id,
            name = CANDIDATE_NAMES.random(),
            position = position,
            skillDevelopment = skills[0],
            skillDesign = skills[1],
            skillArt = skills[2],
            skillMusic = skills[3],
            skillService = skills[4],
            expectedSalary = salary,
            experience = Random.nextInt(0, 60)
        )
    }
    
    /**
     * 根据职位生成合适的技能分布
     * 职位专属技能系统：每个职位只有自己对应职位的专属技能，其他技能为0或1级
     */
    private fun generateSkillsForPosition(position: String): List<Int> {
        // 初始化所有技能为0或1级（非专业技能）
        val skills = MutableList(5) { Random.nextInt(0, 2) } // 非专业技能0-1级
        
        // 职位专属技能系统：每个职位只有自己对应的专属技能
        when (position) {
            "程序员" -> {
                // 程序员只有开发技能(skillDevelopment)高等级，其他技能保持低等级
                skills[0] = Random.nextInt(3, 6) // 开发技能3-5级（专属技能）
                // 其他技能保持0-1级
            }
            "策划师" -> {
                // 策划师只有策划技能(skillDesign)高等级，其他技能保持低等级
                skills[1] = Random.nextInt(3, 6) // 策划技能3-5级（专属技能）
                // 其他技能保持0-1级
            }
            "美术师" -> {
                // 美术师只有美术技能(skillArt)高等级，其他技能保持低等级
                skills[2] = Random.nextInt(3, 6) // 美术技能3-5级（专属技能）
                // 其他技能保持0-1级
            }
            "音效师" -> {
                // 音效师只有音效技能(skillMusic)高等级，其他技能保持低等级
                skills[3] = Random.nextInt(3, 6) // 音效技能3-5级（专属技能）
                // 其他技能保持0-1级
            }
            "客服" -> {
                // 客服只有客服技能(skillService)高等级，其他技能保持低等级
                skills[4] = Random.nextInt(3, 6) // 客服技能3-5级（专属技能）
                // 其他技能保持0-1级
            }
        }
        
        // 确保技能等级在有效范围内
        return skills.map { SkillConstants.clampSkillLevel(it) }
    }
    
    /**
     * 根据筛选条件过滤候选人
     */
    fun filterCandidates(
        candidates: List<TalentCandidate>,
        criteria: FilterCriteria
    ): List<TalentCandidate> {
        return candidates.filter { candidate ->
            candidate.matchesFilter(criteria)
        }
    }
    
    /**
     * 按技能等级排序候选人
     */
    fun sortCandidatesBySkill(
        candidates: List<TalentCandidate>,
        ascending: Boolean = false
    ): List<TalentCandidate> {
        return if (ascending) {
            candidates.sortedBy { it.getMaxSkillLevel() }
        } else {
            candidates.sortedByDescending { it.getMaxSkillLevel() }
        }
    }
    
    /**
     * 按薪资排序候选人
     */
    fun sortCandidatesBySalary(
        candidates: List<TalentCandidate>,
        ascending: Boolean = true
    ): List<TalentCandidate> {
        return if (ascending) {
            candidates.sortedBy { it.expectedSalary }
        } else {
            candidates.sortedByDescending { it.expectedSalary }
        }
    }
    
    /**
     * 获取候选人统计信息
     */
    fun getCandidateStats(candidates: List<TalentCandidate>): CandidateStats {
        if (candidates.isEmpty()) {
            return CandidateStats(0, 0, 0.0, 0, emptyMap())
        }
        
        val avgSalary = candidates.map { it.expectedSalary }.average().toInt()
        val avgSkill = candidates.map { it.getMaxSkillLevel() }.average()
        val avgExperience = candidates.map { it.experience }.average().toInt()
        val positionCounts = candidates.groupingBy { it.position }.eachCount()
        
        return CandidateStats(
            totalCount = candidates.size,
            averageSalary = avgSalary,
            averageSkillLevel = avgSkill,
            averageExperience = avgExperience,
            positionDistribution = positionCounts
        )
    }
    
    /**
     * 刷新候选人列表（重新生成）
     */
    fun refreshCandidates(count: Int = 20): List<TalentCandidate> {
        return generateCandidates(count)
    }
    
    /**
     * 为特定岗位生成候选人
     * 用于岗位发布系统
     */
    fun generateCandidateForPosition(
        position: String,
        minSkillLevel: Int = 1,
        salaryRange: IntRange = 3000..50000
    ): TalentCandidate {
        val skills = generateSkillsForPosition(position, minSkillLevel)
        val maxSkill = skills.maxOf { it }
        
        // 根据技能等级和薪资范围生成合理的薪资期望
        val baseSalary = SALARY_BASE[maxSkill] ?: 3000
        val targetSalary = (salaryRange.first + salaryRange.last) / 2
        val salary = (baseSalary * 0.7 + targetSalary * 0.3).toInt()
            .coerceIn(salaryRange.first, salaryRange.last)
        
        // 添加一些随机波动
        val finalSalary = salary + Random.nextInt(-500, 1000)
        
        return TalentCandidate(
            id = "candidate_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            name = CANDIDATE_NAMES.random(),
            position = position,
            skillDevelopment = skills[0],
            skillDesign = skills[1],
            skillArt = skills[2],
            skillMusic = skills[3],
            skillService = skills[4],
            expectedSalary = finalSalary.coerceIn(salaryRange.first, salaryRange.last),
            experience = Random.nextInt(0, 60)
        )
    }
    
    /**
     * 为特定岗位生成技能分布（带最低技能等级要求）
     */
    private fun generateSkillsForPosition(position: String, minSkillLevel: Int): List<Int> {
        // 初始化所有技能为0或1级
        val skills = MutableList(5) { Random.nextInt(0, 2) }
        
        // 根据职位设置专属技能，确保达到最低等级要求
        when (position) {
            "程序员" -> {
                skills[0] = Random.nextInt(minSkillLevel, 6).coerceAtMost(5)
            }
            "策划师" -> {
                skills[1] = Random.nextInt(minSkillLevel, 6).coerceAtMost(5)
            }
            "美术师" -> {
                skills[2] = Random.nextInt(minSkillLevel, 6).coerceAtMost(5)
            }
            "音效师" -> {
                skills[3] = Random.nextInt(minSkillLevel, 6).coerceAtMost(5)
            }
            "客服" -> {
                skills[4] = Random.nextInt(minSkillLevel, 6).coerceAtMost(5)
            }
        }
        
        // 确保技能等级在有效范围内
        return skills.map { SkillConstants.clampSkillLevel(it) }
    }
}

/**
 * 候选人统计信息数据类
 */
data class CandidateStats(
    val totalCount: Int,
    val averageSalary: Int,
    val averageSkillLevel: Double,
    val averageExperience: Int,
    val positionDistribution: Map<String, Int>
)