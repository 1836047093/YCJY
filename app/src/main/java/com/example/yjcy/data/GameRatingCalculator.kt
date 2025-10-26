package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.BusinessModel
import kotlin.math.min
import kotlin.random.Random

/**
 * 游戏评分计算器 - 渐进式评分系统
 * 
 * 评分维度：
 * 1. 基础分：2.0分（降低起点）
 * 2. 技能评分：采用递减收益曲线（1-4.5分）
 * 3. 团队协作加成：多职位配合（0-1.5分）
 * 4. 主题匹配加成：技能与主题适配（0-1分）
 * 5. 复杂度惩罚：多平台/网游降低评分（-0-1.5分）
 * 6. 平衡性加成：员工技能均衡度（0-0.5分）
 * 7. 精英团队加成：高级员工比例（0-0.5分）
 * 
 * 最终评分 = 基础分 + 技能评分 + 团队协作 + 主题匹配 - 复杂度惩罚 + 平衡性 + 精英加成
 * 理论最高分：10.0分（单平台单机，5职位满配，全员5级，完美主题匹配）
 */
object GameRatingCalculator {
    const val BASE_SCORE = 2.0f
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
     * 计算游戏评分 - 多维度评分系统
     * @param game 游戏对象，包含分配的员工信息
     * @return GameRating 包含最终评分和详细计算信息
     */
    fun calculateRating(game: Game): GameRating {
        // 1. 计算技能评分（递减收益）
        val skillContributions = game.assignedEmployees.map { employee ->
            val primarySkill = employee.getSpecialtySkillType()
            val skillLevel = employee.getSpecialtySkillLevel()
            val contribution = calculateSkillContribution(skillLevel)
            
            SkillContribution(
                employeeId = employee.id,
                employeeName = employee.name,
                skillType = primarySkill,
                skillLevel = skillLevel,
                contribution = contribution
            )
        }
        val skillScore = skillContributions.sumOf { it.contribution.toDouble() }.toFloat().coerceAtMost(4.5f)
        
        // 2. 计算团队协作加成（多职位配合）
        val teamworkBonus = calculateTeamworkBonus(game.assignedEmployees)
        
        // 3. 计算主题匹配加成
        val themeBonus = calculateThemeBonus(game.theme, game.assignedEmployees)
        
        // 4. 计算复杂度惩罚
        val complexityPenalty = calculateComplexityPenalty(game)
        
        // 5. 计算平衡性加成
        val balanceBonus = calculateBalanceBonus(game.assignedEmployees)
        
        // 6. 计算精英团队加成
        val eliteBonus = calculateEliteBonus(game.assignedEmployees)
        
        // 计算最终评分
        val rawScore = BASE_SCORE + skillScore + teamworkBonus + themeBonus - complexityPenalty + balanceBonus + eliteBonus
        val finalScore = rawScore.coerceIn(0f, MAX_SCORE)
        
        // 生成5家媒体的评测
        val mediaReviews = generateMediaReviews(finalScore)
        
        return GameRating(
            gameId = game.id,
            finalScore = finalScore,
            baseScore = BASE_SCORE,
            skillBonus = skillScore,
            skillContributions = skillContributions,
            mediaReviews = mediaReviews
        )
    }
    
    /**
     * 计算单个技能等级的贡献（递减收益曲线）
     * 技能范围：1-5级
     * 
     * 1级: 0.30  2级: 0.50  3级: 0.65  4级: 0.75  5级: 0.85
     * 
     * 设计理念：
     * - 低级技能收益较低，鼓励培养高级员工
     * - 高级技能收益递减，但5级仍有明显优势
     * - 5级员工对评分贡献0.85分，5个5级员工=4.25分（封顶4.5）
     * - 递增幅度：1→2(+0.20) > 2→3(+0.15) > 3→4(+0.10) > 4→5(+0.10)
     */
    private fun calculateSkillContribution(skillLevel: Int): Float {
        return when (skillLevel) {
            1 -> 0.30f
            2 -> 0.50f
            3 -> 0.65f
            4 -> 0.75f
            5 -> 0.85f
            else -> 0f
        }
    }
    
    /**
     * 计算团队协作加成（多职位配合）
     * 激励玩家招募不同职位的员工而不是单一职位
     * 
     * 1个职位: 0分
     * 2个职位: 0.3分
     * 3个职位: 0.7分
     * 4个职位: 1.2分
     * 5个职位: 1.5分（满配加成）
     */
    private fun calculateTeamworkBonus(employees: List<Employee>): Float {
        val uniquePositions = employees.map { it.position }.toSet().size
        return when (uniquePositions) {
            1 -> 0f
            2 -> 0.3f
            3 -> 0.7f
            4 -> 1.2f
            5 -> 1.5f
            else -> 0f
        }
    }
    
    /**
     * 计算主题匹配加成
     * 不同游戏主题需要不同的核心技能，符合主题的高级员工有额外加成
     * 
     * 核心技能定义：
     * - 动作/射击/体育/竞速：开发（程序员）
     * - RPG/冒险/策略/恐怖：设计（策划师）
     * - 休闲：美工、音乐
     * - 模拟/解谜：设计、开发
     * - MOBA：开发、设计（需要强技术和强策划）
     * 
     * 加成计算：核心职位的平均技能等级 × 0.1（最高1.0分）
     */
    private fun calculateThemeBonus(theme: GameTheme, employees: List<Employee>): Float {
        val corePositions = when (theme) {
            GameTheme.ACTION, GameTheme.SHOOTER, GameTheme.SPORTS, GameTheme.RACING -> listOf("程序员")
            GameTheme.RPG, GameTheme.ADVENTURE, GameTheme.STRATEGY, GameTheme.HORROR -> listOf("策划师")
            GameTheme.CASUAL -> listOf("美工", "音乐家")
            GameTheme.SIMULATION, GameTheme.PUZZLE -> listOf("策划师", "程序员")
            GameTheme.MOBA -> listOf("程序员", "策划师")
        }
        
        val coreEmployees = employees.filter { it.position in corePositions }
        if (coreEmployees.isEmpty()) return 0f
        
        val avgSkillLevel = coreEmployees.map { it.getSpecialtySkillLevel() }.average().toFloat()
        return (avgSkillLevel * 0.1f).coerceAtMost(1.0f)
    }
    
    /**
     * 计算复杂度惩罚
     * 更复杂的项目更难获得高评分
     * 
     * - 多平台：每增加1个平台 -0.2分（2个平台-0.2，3个-0.4，4个-0.6）
     * - 网络游戏：额外 -0.5分（技术难度更高）
     * 
     * 最大惩罚：-1.5分（4平台网游）
     */
    private fun calculateComplexityPenalty(game: Game): Float {
        var penalty = 0f
        
        // 多平台惩罚
        val platformCount = game.platforms.size
        if (platformCount > 1) {
            penalty += (platformCount - 1) * 0.2f
        }
        
        // 网络游戏惩罚
        if (game.businessModel == BusinessModel.ONLINE_GAME) {
            penalty += 0.5f
        }
        
        return penalty.coerceAtMost(1.5f)
    }
    
    /**
     * 计算平衡性加成
     * 奖励技能分布均衡的团队，避免极端情况
     * 
     * 计算方法：
     * - 计算所有员工技能等级的标准差
     * - 标准差越小，团队越均衡
     * - 标准差≤1.0: +0.5分
     * - 标准差≤2.0: +0.3分
     * - 标准差≤3.0: +0.1分
     * - 标准差>3.0: 0分
     */
    private fun calculateBalanceBonus(employees: List<Employee>): Float {
        if (employees.size <= 1) return 0f
        
        val skillLevels = employees.map { it.getSpecialtySkillLevel() }
        val avg = skillLevels.average()
        val variance = skillLevels.map { (it - avg) * (it - avg) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        
        return when {
            stdDev <= 1.0 -> 0.5f
            stdDev <= 2.0 -> 0.3f
            stdDev <= 3.0 -> 0.1f
            else -> 0f
        }
    }
    
    /**
     * 计算精英团队加成
     * 奖励拥有多个高级员工（≥4级）的团队
     * 
     * 高级员工（4-5级）比例加成：
     * - 20%以上: +0.1分
     * - 40%以上: +0.2分
     * - 60%以上: +0.3分
     * - 80%以上: +0.4分
     * - 100%: +0.5分
     */
    private fun calculateEliteBonus(employees: List<Employee>): Float {
        if (employees.isEmpty()) return 0f
        
        val eliteCount = employees.count { it.getSpecialtySkillLevel() >= 4 }
        val eliteRatio = eliteCount.toFloat() / employees.size
        
        return when {
            eliteRatio >= 1.0f -> 0.5f
            eliteRatio >= 0.8f -> 0.4f
            eliteRatio >= 0.6f -> 0.3f
            eliteRatio >= 0.4f -> 0.2f
            eliteRatio >= 0.2f -> 0.1f
            else -> 0f
        }
    }
    
    /**
     * 计算单个员工的技能贡献（用于预览）
     * @param employee 员工对象
     * @return Float 该员工对评分的贡献值
     */
    fun calculateEmployeeContribution(employee: Employee): Float {
        return calculateSkillContribution(employee.getSpecialtySkillLevel())
    }
    
    /**
     * 预览评分计算结果（不保存）- 简化版本
     * 注意：这是简化预览，实际评分还会考虑游戏主题、平台等因素
     * @param assignedEmployees 分配的员工列表
     * @return Float 预计的最终评分（仅供参考）
     */
    fun previewRating(assignedEmployees: List<Employee>): Float {
        if (assignedEmployees.isEmpty()) return BASE_SCORE
        
        // 技能评分
        val skillScore = assignedEmployees.sumOf { employee ->
            calculateSkillContribution(employee.getSpecialtySkillLevel()).toDouble()
        }.toFloat().coerceAtMost(4.5f)
        
        // 团队协作加成
        val teamworkBonus = calculateTeamworkBonus(assignedEmployees)
        
        // 平衡性加成
        val balanceBonus = calculateBalanceBonus(assignedEmployees)
        
        // 精英团队加成
        val eliteBonus = calculateEliteBonus(assignedEmployees)
        
        val estimated = BASE_SCORE + skillScore + teamworkBonus + balanceBonus + eliteBonus
        return estimated.coerceIn(0f, MAX_SCORE)
    }
}