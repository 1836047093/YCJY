package com.example.yjcy.data

import kotlin.math.min
import kotlin.random.Random

/**
 * 游戏评分计算器
 * 实现游戏评分算法：基础分5分 + 技能等级/2
 */
object GameRatingCalculator {
    const val BASE_SCORE = 5.0f
    const val SKILL_DIVISOR = 2.0f
    const val MAX_SCORE = 10.0f
    
    // 媒体列表
    private val mediaOutlets = listOf(
        "游火社",
        "夜猫游戏局",
        "漫游中枢",
        "电光快报",
        "幻游矩阵"
    )
    
    // 评价词库 - 按分数段分类
    private val excellentComments = listOf(
        "这是一款令人惊艳的游戏，从画面到玩法都堪称完美",
        "开发团队展现了卓越的创造力和技术实力",
        "游戏体验极其出色，强烈推荐给所有玩家",
        "这款游戏重新定义了该类型游戏的标准",
        "无论从哪个角度看，这都是一款神作级的游戏"
    )
    
    private val goodComments = listOf(
        "整体表现优秀，虽有小瑕疵但不影响体验",
        "开发团队的用心程度让人印象深刻",
        "游戏品质上乘，值得一玩",
        "在同类游戏中属于上乘之作",
        "各方面都很均衡，是款高品质的游戏"
    )
    
    private val averageComments = listOf(
        "中规中矩的作品，有亮点也有不足",
        "游戏有一定可玩性，但还有提升空间",
        "整体表现平平，适合休闲娱乐",
        "游戏有潜力，但执行上还需要打磨",
        "能够满足基本需求，但缺乏创新"
    )
    
    private val poorComments = listOf(
        "游戏存在明显的设计问题",
        "各方面的表现都略显不足",
        "需要更多的优化和改进",
        "玩法较为单调，缺乏吸引力",
        "遗憾的是游戏未能达到预期"
    )
    
    /**
     * 生成媒体评测
     * @param baseScore 游戏的基础评分
     * @return 5家媒体的评测列表
     */
    private fun generateMediaReviews(baseScore: Float): List<MediaReview> {
        return mediaOutlets.map { mediaName ->
            // 在基础分上下浮动0-1分
            val variance = Random.nextFloat() * 2f - 1f // -1 到 +1
            val mediaRating = (baseScore + variance).coerceIn(0f, 10f)
            
            // 根据评分选择评价内容
            val comment = when {
                mediaRating >= 8.0f -> excellentComments.random()
                mediaRating >= 6.5f -> goodComments.random()
                mediaRating >= 5.0f -> averageComments.random()
                else -> poorComments.random()
            }
            
            MediaReview(
                mediaName = mediaName,
                rating = mediaRating,
                comment = comment
            )
        }
    }
    
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
        
        // 生成5家媒体的评测
        val mediaReviews = generateMediaReviews(finalScore)
        
        return GameRating(
            gameId = game.id,
            finalScore = finalScore,
            baseScore = BASE_SCORE,
            skillBonus = skillBonus,
            skillContributions = skillContributions,
            mediaReviews = mediaReviews
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