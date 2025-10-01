package com.example.yjcy.data

import kotlin.math.min

/**
 * 游戏评分计算器
 * 实现游戏评分算法：基础分5分 + 技能等级/2
 */
object GameRatingCalculator {
    const val BASE_SCORE = 5.0f
    const val SKILL_DIVISOR = 2.0f
    const val MAX_SCORE = 10.0f
    
    /**
     * 计算游戏评分
     * @param game 游戏对象，包含分配的员工信息
     * @return GameRating 包含最终评分和详细计算信息
     */
    fun calculateRating(game: Game): GameRating {
        val skillContributions = game.assignedEmployees.map { employee ->
            val primarySkill = employee.getSpecialtySkillType()
            val skillLevel = employee.getSpecialtySkillLevel()
            
            SkillContribution(
                employeeId = employee.id,
                employeeName = employee.name,
                skillType = primarySkill,
                skillLevel = skillLevel,
                contribution = skillLevel / SKILL_DIVISOR
            )
        }
        
        val skillBonus = skillContributions.sumOf { it.contribution.toDouble() }.toFloat()
        val finalScore = min(BASE_SCORE + skillBonus, MAX_SCORE)
        
        return GameRating(
            gameId = game.id,
            finalScore = finalScore,
            baseScore = BASE_SCORE,
            skillBonus = skillBonus,
            skillContributions = skillContributions
        )
    }
    
    /**
     * 计算单个员工的技能贡献
     * @param employee 员工对象
     * @return Float 该员工对评分的贡献值
     */
    fun calculateEmployeeContribution(employee: Employee): Float {
        return employee.getSpecialtySkillLevel() / SKILL_DIVISOR
    }
    
    /**
     * 预览评分计算结果（不保存）
     * @param assignedEmployees 分配的员工列表
     * @return Float 预计的最终评分
     */
    fun previewRating(assignedEmployees: List<Employee>): Float {
        val totalSkillBonus = assignedEmployees.sumOf { employee ->
            (employee.getSpecialtySkillLevel() / SKILL_DIVISOR).toDouble()
        }.toFloat()
        
        return min(BASE_SCORE + totalSkillBonus, MAX_SCORE)
    }
}