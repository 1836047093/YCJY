package com.example.yjcy.data
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 排序策略枚举
 */
enum class RankingStrategy {
    SKILL_LEVEL_PRIORITY,  // 技能等级优先
    MATCH_PRIORITY,        // 匹配度优先
    SPECIALTY_PRIORITY,    // 专业技能优先
    BALANCED              // 平衡策略
}

/**
 * 技能匹配结果
 */
data class SkillMatchResult(
    val employee: Employee,
    val project: Game,
    val overallMatchScore: Double,
    val skillMatches: Map<String, SkillMatchDetail>,
    val totalSkillPoints: Int,
    val specialtyBonus: Double
) {
    /**
     * 获取综合评分
     */
    fun getCompositeScore(): Double {
        return overallMatchScore * 100 * 0.4 + 
               totalSkillPoints * 0.3 + 
               specialtyBonus * 30
    }
    
    /**
     * 获取匹配摘要
     */
    fun getMatchSummary(): String {
        val matchPercentage = (overallMatchScore * 100).toInt()
        val specialtyText = if (specialtyBonus > 0) "专业对口" else "非专业"
        return "${employee.name}: 匹配度${matchPercentage}%, $specialtyText, 总技能${totalSkillPoints}点"
    }
}

/**
 * 单项技能匹配详情
 */
data class SkillMatchDetail(
    val employeeLevel: Int,
    val requiredLevel: Int,
    val matchScore: Double,
    val isSpecialty: Boolean
) {
    /**
     * 获取匹配状态
     */
    fun getMatchStatus(): String {
        return when {
            matchScore >= 1.0 -> "完全匹配"
            matchScore >= 0.8 -> "高度匹配"
            matchScore >= 0.6 -> "良好匹配"
            matchScore >= 0.4 -> "基本匹配"
            else -> "不匹配"
        }
    }
}

/**
 * 技能匹配引擎
 * 提供高级的员工技能匹配和排序算法
 */
class SkillMatchingEngine {
    
    /**
     * 计算员工与项目的技能匹配度
     * @param employee 员工
     * @param project 项目
     * @return 匹配度分数 (0.0 - 1.0)
     */
    fun calculateSkillMatch(employee: Employee, project: Game): SkillMatchResult {
        val requiredSkills = getProjectSkillRequirements(project)
        val employeeSkills = getEmployeeSkillVector(employee)
        
        var totalMatch = 0.0
        var maxPossibleMatch = 0.0
        val skillMatches = mutableMapOf<String, SkillMatchDetail>()
        
        for ((skillType, requiredLevel) in requiredSkills) {
            val employeeLevel = employeeSkills[skillType] ?: 0
            val matchScore = calculateSingleSkillMatch(employeeLevel, requiredLevel)
            
            skillMatches[skillType] = SkillMatchDetail(
                employeeLevel = employeeLevel,
                requiredLevel = requiredLevel,
                matchScore = matchScore,
                isSpecialty = employee.getSpecialtySkillType() == skillType
            )
            
            totalMatch += matchScore * requiredLevel
            maxPossibleMatch += requiredLevel * requiredLevel
        }
        
        val overallMatch = if (maxPossibleMatch > 0) totalMatch / maxPossibleMatch else 0.0
        
        return SkillMatchResult(
            employee = employee,
            project = project,
            overallMatchScore = overallMatch,
            skillMatches = skillMatches,
            totalSkillPoints = employee.getTotalSkillPoints(),
            specialtyBonus = calculateSpecialtyBonus(employee, requiredSkills.keys)
        )
    }
    
    /**
     * 为项目排序员工列表
     * @param employees 员工列表
     * @param project 项目
     * @param strategy 排序策略
     * @return 排序后的员工匹配结果列表
     */
    fun rankEmployeesForProject(
        employees: List<Employee>,
        project: Game,
        strategy: RankingStrategy = RankingStrategy.BALANCED
    ): List<SkillMatchResult> {
        val matchResults = employees.map { calculateSkillMatch(it, project) }
        
        return when (strategy) {
            RankingStrategy.SKILL_LEVEL_PRIORITY -> {
                matchResults.sortedWith { a, b ->
                    val scoreA = a.totalSkillPoints * 0.6 + a.overallMatchScore * 100 * 0.4
                    val scoreB = b.totalSkillPoints * 0.6 + b.overallMatchScore * 100 * 0.4
                    scoreB.compareTo(scoreA)
                }
            }
            RankingStrategy.MATCH_PRIORITY -> {
                matchResults.sortedWith { a, b ->
                    val scoreA = a.overallMatchScore * 100 * 0.7 + a.totalSkillPoints * 0.3
                    val scoreB = b.overallMatchScore * 100 * 0.7 + b.totalSkillPoints * 0.3
                    scoreB.compareTo(scoreA)
                }
            }
            RankingStrategy.SPECIALTY_PRIORITY -> {
                matchResults.sortedWith { a, b ->
                    val scoreA = a.specialtyBonus * 50 + a.overallMatchScore * 100 * 0.5 + a.totalSkillPoints * 0.3
                    val scoreB = b.specialtyBonus * 50 + b.overallMatchScore * 100 * 0.5 + b.totalSkillPoints * 0.3
                    scoreB.compareTo(scoreA)
                }
            }
            RankingStrategy.BALANCED -> {
                matchResults.sortedWith { a, b ->
                    val scoreA = a.overallMatchScore * 100 * 0.4 + a.totalSkillPoints * 0.3 + a.specialtyBonus * 30
                    val scoreB = b.overallMatchScore * 100 * 0.4 + b.totalSkillPoints * 0.3 + b.specialtyBonus * 30
                    scoreB.compareTo(scoreA)
                }
            }
        }
    }
    
    /**
     * 获取项目的技能需求
     */
    private fun getProjectSkillRequirements(project: Game): Map<String, Int> {
        return when (project.theme.name) {
            "ACTION" -> mapOf(
                "开发" to 5,
                "美工" to 4,
                "音乐" to 3,
                "设计" to 2
            )
            "ADVENTURE" -> mapOf(
                "开发" to 4,
                "设计" to 5,
                "美工" to 4,
                "音乐" to 2
            )
            "PUZZLE" -> mapOf(
                "开发" to 5,
                "设计" to 4,
                "美工" to 2,
                "音乐" to 1
            )
            "STRATEGY" -> mapOf(
                "开发" to 4,
                "设计" to 5,
                "美工" to 3,
                "音乐" to 2
            )
            "SIMULATION" -> mapOf(
                "开发" to 5,
                "设计" to 4,
                "美工" to 3,
                "音乐" to 2
            )
            "SPORTS" -> mapOf(
                "开发" to 4,
                "美工" to 4,
                "音乐" to 3,
                "设计" to 2
            )
            "RACING" -> mapOf(
                "开发" to 5,
                "美工" to 4,
                "音乐" to 4,
                "设计" to 2
            )
            "FIGHTING" -> mapOf(
                "开发" to 4,
                "美工" to 5,
                "音乐" to 4,
                "设计" to 2
            )
            "SHOOTING" -> mapOf(
                "开发" to 5,
                "美工" to 4,
                "音乐" to 3,
                "设计" to 2
            )
            "HORROR" -> mapOf(
                "开发" to 4,
                "美工" to 5,
                "音乐" to 5,
                "设计" to 4
            )
            else -> mapOf(
                "开发" to 4,
                "设计" to 3,
                "美工" to 3,
                "音乐" to 2
            )
        }
    }
    
    /**
     * 获取员工技能向量
     */
    private fun getEmployeeSkillVector(employee: Employee): Map<String, Int> {
        return mapOf(
            "开发" to employee.skillDevelopment,
            "设计" to employee.skillDesign,
            "美工" to employee.skillArt,
            "音乐" to employee.skillMusic,
            "服务" to employee.skillService
        )
    }
    
    /**
     * 计算单项技能匹配度
     */
    private fun calculateSingleSkillMatch(employeeLevel: Int, requiredLevel: Int): Double {
        return when {
            employeeLevel >= requiredLevel -> 1.0
            employeeLevel == 0 -> 0.0
            else -> (employeeLevel.toDouble() / requiredLevel).coerceAtMost(1.0)
        }
    }
    
    /**
     * 计算专业技能加成
     */
    private fun calculateSpecialtyBonus(employee: Employee, requiredSkills: Set<String>): Double {
        val specialtySkill = employee.getSpecialtySkillType()
        return if (specialtySkill in requiredSkills) {
            val specialtyLevel = when (specialtySkill) {
                "开发" -> employee.skillDevelopment
                "设计" -> employee.skillDesign
                "美工" -> employee.skillArt
                "音乐" -> employee.skillMusic
                "服务" -> employee.skillService
                else -> 0
            }
            specialtyLevel / 5.0 // 归一化到0-1
        } else {
            0.0
        }
    }
}