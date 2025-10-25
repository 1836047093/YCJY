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
        // 候选人姓名池（扩展版本）
        private val CANDIDATE_NAMES = listOf(
            // 常见姓氏 + 常见名字组合
            "张伟", "李娜", "王强", "刘敏", "陈杰", "杨丽", "赵磊", "孙静", "周涛", "吴萍",
            "郑浩", "王芳", "李明", "张丽", "刘伟", "陈静", "杨强", "赵敏", "孙杰", "周萍",
            "吴涛", "郑丽", "王浩", "李静", "张强", "刘丽", "陈伟", "杨敏", "赵杰", "孙萍",
            "黄磊", "周杰", "徐娜", "朱伟", "林丽", "何强", "高静", "马明", "罗敏", "梁涛",
            "宋萍", "唐浩", "许芳", "韩杰", "冯丽", "曹磊", "彭敏", "曾强", "肖静", "田伟",
            "董娜", "袁明", "潘杰", "于丽", "蒋涛", "蔡萍", "余浩", "杜芳", "叶磊", "苏敏",
            "魏强", "卢静", "丁伟", "任娜", "沈明", "姚杰", "卫丽", "钱涛", "汤萍", "黎浩",
            "易芳", "常磊", "武敏", "乔强", "贺静", "赖伟", "龚娜", "文明", "庞杰", "樊丽",
            "兰涛", "殷萍", "施浩", "陶芳", "洪磊", "翟敏", "安强", "颜静", "倪伟", "严娜",
            "牛明", "温杰", "芦丽", "季涛", "俞萍", "章浩", "鲁芳", "韦磊", "薛敏", "雷强"
        )
        
        // 职位列表
        private val POSITIONS = listOf("程序员", "策划师", "美术师", "音效师", "客服")
        
        // 薪资基础值（根据技能等级）
        private val SALARY_BASE = mapOf(
            1 to 3000, 2 to 4000, 3 to 6000, 4 to 9000, 5 to 15000
        )
        
        /**
         * 生成唯一的员工名字
         * 如果名字已存在，则在后面加数字后缀（如"张伟2"、"张伟3"）
         */
        fun generateUniqueName(existingNames: Set<String>): String {
            // 先尝试从名字池中找一个未使用的名字
            val availableName = CANDIDATE_NAMES.firstOrNull { it !in existingNames }
            if (availableName != null) {
                return availableName
            }
            
            // 如果所有基础名字都被使用了，添加数字后缀
            val baseName = CANDIDATE_NAMES.random()
            var suffix = 2
            var uniqueName = "$baseName$suffix"
            
            while (uniqueName in existingNames) {
                suffix++
                uniqueName = "$baseName$suffix"
            }
            
            return uniqueName
        }
    }
    
    /**
     * 生成随机候选人列表
     * @param count 生成数量
     * @param existingEmployeeNames 现有员工名字集合，用于避免重复
     */
    fun generateCandidates(count: Int = 20, existingEmployeeNames: Set<String> = emptySet()): List<TalentCandidate> {
        val usedNames = mutableSetOf<String>()
        usedNames.addAll(existingEmployeeNames)
        
        return (1..count).map { 
            generateRandomCandidate(it.toString(), usedNames)
        }
    }
    
    /**
     * 生成单个随机候选人
     * @param id 候选人ID
     * @param usedNames 已使用的名字集合（可变集合，会自动添加新生成的名字）
     */
    private fun generateRandomCandidate(id: String, usedNames: MutableSet<String>): TalentCandidate {
        val position = POSITIONS.random()
        val skills = generateSkillsForPosition(position)
        val maxSkill = skills.maxOf { it }
        val baseSalary = SALARY_BASE[maxSkill] ?: 3000
        val salary = baseSalary + Random.nextInt(-500, 1000)
        
        // 根据技能等级生成合理的工作经验（符合现实）
        val experience = generateRealisticExperience(maxSkill)
        
        // 生成唯一名字
        val uniqueName = generateUniqueName(usedNames)
        usedNames.add(uniqueName)
        
        return TalentCandidate(
            id = id,
            name = uniqueName,
            position = position,
            skillDevelopment = skills[0],
            skillDesign = skills[1],
            skillArt = skills[2],
            skillMusic = skills[3],
            skillService = skills[4],
            expectedSalary = salary,
            experience = experience
        )
    }
    
    /**
     * 根据职位生成合适的技能分布
     * 职位专属技能系统：每个职位只有自己对应职位的专属技能，其他技能固定为0
     */
    private fun generateSkillsForPosition(position: String): List<Int> {
        // 初始化所有技能为0（非专业技能）
        val skills = MutableList(5) { 0 } // 非专业技能固定为0
        
        // 职位专属技能系统：每个职位只有自己对应的专属技能
        when (position) {
            "程序员" -> {
                // 程序员只有开发技能(skillDevelopment)高等级，其他技能为0
                skills[0] = Random.nextInt(3, 6) // 开发技能3-5级（专属技能）
            }
            "策划师" -> {
                // 策划师只有策划技能(skillDesign)高等级，其他技能为0
                skills[1] = Random.nextInt(3, 6) // 策划技能3-5级（专属技能）
            }
            "美术师" -> {
                // 美术师只有美术技能(skillArt)高等级，其他技能为0
                skills[2] = Random.nextInt(3, 6) // 美术技能3-5级（专属技能）
            }
            "音效师" -> {
                // 音效师只有音效技能(skillMusic)高等级，其他技能为0
                skills[3] = Random.nextInt(3, 6) // 音效技能3-5级（专属技能）
            }
            "客服" -> {
                // 客服只有客服技能(skillService)高等级，其他技能为0
                skills[4] = Random.nextInt(3, 6) // 客服技能3-5级（专属技能）
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
     * @param count 生成数量
     * @param existingEmployeeNames 现有员工名字集合，用于避免重复
     */
    fun refreshCandidates(count: Int = 20, existingEmployeeNames: Set<String> = emptySet()): List<TalentCandidate> {
        return generateCandidates(count, existingEmployeeNames)
    }
    
    /**
     * 为特定岗位生成候选人
     * 用于岗位发布系统
     * @param existingEmployeeNames 现有员工名字集合，用于避免重复
     */
    fun generateCandidateForPosition(
        position: String,
        minSkillLevel: Int = 1,
        salaryRange: IntRange = 3000..50000,
        existingEmployeeNames: Set<String> = emptySet()
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
        
        // 根据技能等级生成合理的工作经验
        val experience = generateRealisticExperience(maxSkill)
        
        // 生成唯一名字
        val uniqueName = generateUniqueName(existingEmployeeNames)
        
        return TalentCandidate(
            id = "candidate_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            name = uniqueName,
            position = position,
            skillDevelopment = skills[0],
            skillDesign = skills[1],
            skillArt = skills[2],
            skillMusic = skills[3],
            skillService = skills[4],
            expectedSalary = finalSalary.coerceIn(salaryRange.first, salaryRange.last),
            experience = experience
        )
    }
    
    /**
     * 为特定岗位生成技能分布（带最低技能等级要求）
     * 生成的专属技能等级刚好等于要求等级
     */
    private fun generateSkillsForPosition(position: String, minSkillLevel: Int): List<Int> {
        // 初始化所有技能为0（非专业技能）
        val skills = MutableList(5) { 0 }
        
        // 生成专属技能等级：100%刚好等于最低要求
        val skillLevel = minSkillLevel
        
        // 根据职位设置专属技能
        when (position) {
            "程序员" -> {
                skills[0] = skillLevel
            }
            "策划师" -> {
                skills[1] = skillLevel
            }
            "美术师" -> {
                skills[2] = skillLevel
            }
            "音效师" -> {
                skills[3] = skillLevel
            }
            "客服" -> {
                skills[4] = skillLevel
            }
        }
        
        // 确保技能等级在有效范围内
        return skills.map { SkillConstants.clampSkillLevel(it) }
    }
    
    /**
     * 根据技能等级生成合理的工作经验（符合现实）
     * Lv.1: 0-2年（应届生/刚入行）
     * Lv.2: 1-4年（略有经验）
     * Lv.3: 3-8年（有一定经验）
     * Lv.4: 6-15年（资深人士）
     * Lv.5: 10-25年（行业专家）
     */
    private fun generateRealisticExperience(skillLevel: Int): Int {
        return when (skillLevel) {
            1 -> Random.nextInt(0, 3)      // 0-2年
            2 -> Random.nextInt(1, 5)      // 1-4年
            3 -> Random.nextInt(3, 9)      // 3-8年
            4 -> Random.nextInt(6, 16)     // 6-15年
            5 -> Random.nextInt(10, 26)    // 10-25年
            else -> Random.nextInt(0, 3)   // 默认初级
        }
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