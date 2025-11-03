package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.BusinessModel
import kotlin.math.min
import kotlin.random.Random

/**
 * 游戏评分计算器 - 10分制评分系统
 *
 * 评分维度：
 * 1. 基础分：2.0分
 * 2. 技能评分：根据员工技能等级（最高6.0分）
 * 3. 团队协作加成：多职位配合（0-0.8分）
 * 4. 复杂度惩罚：已移除，多平台和网游不再扣分
 * 5. 平衡性加成：员工技能均衡度（0-0.6分）
 * 6. 精英团队加成：高级员工比例（0-0.6分）
 *
 * 最终评分 = 基础分 + 技能评分 + 团队协作 + 平衡性 + 精英加成
 * 评分范围：2.0 - 10.0分
 *
 * 示例（24人满配）：
 * - 24个1级：约 2.0 + 1.2 + 0.8 + 0.12 = 4.12分
 * - 24个3级：约 2.0 + 3.6 + 0.8 + 0.36 + 0.30 = 7.06分
 * - 24个5级：2.0 + 6.0 + 0.8 + 0.6 + 0.6 = 10.0分（满分）
 */
object GameRatingCalculator {
    const val BASE_SCORE = 2.0f
    const val MAX_SCORE = 10.0f
    private const val MAX_SKILL_SCORE = 6.0f
    
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
            // 在基础分上下浮动0-0.5分
            val variance = Random.nextFloat() * 1f - 0.5f // -0.5 到 +0.5
            val mediaRating = (baseScore + variance).coerceIn(1.0f, 10.0f)
            
            // 根据评分选择评价内容（1-5分制）
            val comment = when {
                mediaRating >= 4.5f -> excellentComments.random()
                mediaRating >= 3.5f -> goodComments.random()
                mediaRating >= 2.5f -> averageComments.random()
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
        // 过滤掉客服，客服不参与开发评分
        val developmentEmployees = game.assignedEmployees.filter { it.position != "客服" }
        
        // 1. 计算技能评分（递减收益）
        val skillContributions = developmentEmployees.map { employee ->
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
        val skillScore = skillContributions.sumOf { it.contribution.toDouble() }.toFloat().coerceAtMost(MAX_SKILL_SCORE)
        
        // 2. 计算团队协作加成（多职位配合）- 只计算开发岗位
        val teamworkBonus = calculateTeamworkBonus(developmentEmployees)
        
        // 3. 计算复杂度惩罚
        val complexityPenalty = calculateComplexityPenalty(game)
        
        // 4. 计算平衡性加成
        val balanceBonus = calculateBalanceBonus(developmentEmployees)
        
        // 5. 计算精英团队加成
        val eliteBonus = calculateEliteBonus(developmentEmployees)
        
        // 计算最终评分
        val rawScore = BASE_SCORE + skillScore + teamworkBonus - complexityPenalty + balanceBonus + eliteBonus
        val finalScore = rawScore.coerceAtMost(MAX_SCORE)
        
        // 输出详细评分日志 - 使用try-catch保护，确保日志错误不影响游戏
        android.util.Log.d("GameRatingCalculator", ">>> 开始输出评分日志")
        try {
            val gameName = game.name ?: "未命名"
            android.util.Log.d("GameRatingCalculator", "===========================================")
            android.util.Log.d("GameRatingCalculator", "游戏评分计算: $gameName")
            android.util.Log.d("GameRatingCalculator", "===========================================")
            android.util.Log.d("GameRatingCalculator", "[员工统计]")
            
            val totalEmployees = game.assignedEmployees.size
            val devEmployees = developmentEmployees.size
            android.util.Log.d("GameRatingCalculator", "  总分配员工数: $totalEmployees")
            android.util.Log.d("GameRatingCalculator", "  开发员工数: $devEmployees (已排除客服)")
            
            // 统计各岗位人数
            android.util.Log.d("GameRatingCalculator", ">>> 开始统计岗位")
            val positionCounts = developmentEmployees.groupBy { it.position }.mapValues { it.value.size }
            val positionCountsStr = if (positionCounts.isEmpty()) "无" else positionCounts.entries.joinToString(", ") { "${it.key}x${it.value}" }
            android.util.Log.d("GameRatingCalculator", "  岗位分布: $positionCountsStr")
            
            // 统计技能等级分布
            android.util.Log.d("GameRatingCalculator", ">>> 开始统计技能等级")
            val skillLevelCounts = developmentEmployees.groupBy { it.getSpecialtySkillLevel() }.mapValues { it.value.size }
            val skillLevelCountsStr = if (skillLevelCounts.isEmpty()) "无" else skillLevelCounts.entries.sortedByDescending { it.key }.joinToString(", ") { "Lv${it.key}x${it.value}" }
            android.util.Log.d("GameRatingCalculator", "  技能等级分布: $skillLevelCountsStr")
            
            android.util.Log.d("GameRatingCalculator", "")
            android.util.Log.d("GameRatingCalculator", "[评分详情]")
            android.util.Log.d("GameRatingCalculator", "  基础分: $BASE_SCORE")
            
            // 技能评分详情
            android.util.Log.d("GameRatingCalculator", ">>> 开始输出技能评分")
            val rawSkillScore = skillContributions.sumOf { it.contribution.toDouble() }.toFloat()
            android.util.Log.d("GameRatingCalculator", "  技能评分: $rawSkillScore (原始) -> $skillScore (封顶6.0)")
            
            android.util.Log.d("GameRatingCalculator", ">>> 开始遍历员工贡献 数量=${skillContributions.size}")
            for (i in skillContributions.indices) {
                val contribution = skillContributions[i]
                android.util.Log.d("GameRatingCalculator", "    - ${contribution.employeeName}(${contribution.skillType} Lv${contribution.skillLevel}): +${contribution.contribution}")
            }
            
            // 团队协作详情
            android.util.Log.d("GameRatingCalculator", ">>> 输出团队协作")
            val uniquePositions = developmentEmployees.map { it.position }.toSet().size
            android.util.Log.d("GameRatingCalculator", "  团队协作: +$teamworkBonus (${uniquePositions}个不同职位)")
            
            // 平衡性详情
            android.util.Log.d("GameRatingCalculator", ">>> 输出平衡性")
            if (developmentEmployees.size > 1) {
                val skillLevels = developmentEmployees.map { it.getSpecialtySkillLevel() }
                val avg = skillLevels.average()
                val variance = skillLevels.map { (it - avg) * (it - avg) }.average()
                val stdDev = kotlin.math.sqrt(variance)
                val stdDevStr = String.format("%.2f", stdDev)
                android.util.Log.d("GameRatingCalculator", "  平衡性加成: +$balanceBonus (标准差=$stdDevStr)")
            } else {
                android.util.Log.d("GameRatingCalculator", "  平衡性加成: +$balanceBonus (员工数<=1)")
            }
            
            // 精英团队详情
            android.util.Log.d("GameRatingCalculator", ">>> 输出精英团队")
            val eliteCount = developmentEmployees.count { it.getSpecialtySkillLevel() >= 4 }
            val eliteRatio = if (developmentEmployees.isNotEmpty()) eliteCount.toFloat() / developmentEmployees.size else 0f
            val elitePercent = (eliteRatio * 100).toInt()
            android.util.Log.d("GameRatingCalculator", "  精英团队加成: +$eliteBonus (${eliteCount}/${developmentEmployees.size}=${elitePercent}% >=4级)")
            
            android.util.Log.d("GameRatingCalculator", "")
            android.util.Log.d("GameRatingCalculator", "[最终评分]")
            android.util.Log.d("GameRatingCalculator", "  计算: $BASE_SCORE + $skillScore + $teamworkBonus - $complexityPenalty + $balanceBonus + $eliteBonus = $rawScore")
            android.util.Log.d("GameRatingCalculator", "  最终得分: $finalScore / $MAX_SCORE")
            android.util.Log.d("GameRatingCalculator", "===========================================")
            android.util.Log.d("GameRatingCalculator", ">>> 日志输出完成")
        } catch (e: Exception) {
            android.util.Log.e("GameRatingCalculator", "!!! 日志输出异常: ${e.message}", e)
        }
        android.util.Log.d("GameRatingCalculator", ">>> try-catch块已退出")
        
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
     * 计算单个技能等级的贡献（10分制）
     * 技能范围：1-5级
     *
     * 1级: 0.05  2级: 0.10  3级: 0.15  4级: 0.20  5级: 0.25
     *
     * 设计理念：
     * - 技能评分部分最高 6.0 分（24人×0.25 → 封顶）
     * - 保持递减收益，鼓励培养高等级员工
     */
    private fun calculateSkillContribution(skillLevel: Int): Float {
        return when (skillLevel) {
            1 -> 0.05f
            2 -> 0.10f
            3 -> 0.15f
            4 -> 0.20f
            5 -> 0.25f
            else -> 0f
        }
    }
    
    /**
     * 计算团队协作加成（多职位配合）
     * 激励玩家招募不同职位的员工而不是单一职位
     *
     * 1个职位: 0分
     * 2个职位: 0.4分
     * 3个职位: 0.6分
     * 4个职位: 0.8分
     * 5个职位: 0.8分（保留兼容性）
     */
    private fun calculateTeamworkBonus(employees: List<Employee>): Float {
        val uniquePositions = employees.map { it.position }.toSet().size
        return when (uniquePositions) {
            1 -> 0f
            2 -> 0.4f
            3 -> 0.6f
            4 -> 0.8f
            5 -> 0.8f
            else -> 0f
        }
    }
    
    /**
     * 计算复杂度惩罚
     * 多平台和网游不再扣分
     */
    private fun calculateComplexityPenalty(game: Game): Float {
        // 已移除多平台和网游的扣分逻辑
        return 0f
    }
    
    /**
     * 计算平衡性加成（10分制）
     * 奖励技能分布均衡的团队，避免极端情况
     *
     * 计算方法：
     * - 计算所有员工技能等级的标准差
     * - 标准差越小，团队越均衡
     * - 根据平均技能等级调整加成系数，避免低级员工占便宜
     *
     * 标准差加成：
     * - 标准差≤1.0: +0.6分
     * - 标准差≤2.0: +0.4分
     * - 标准差≤3.0: +0.2分
     *
     * 平均等级系数：
     * - 平均≥4级: 100%
     * - 平均≥3级: 70%
     * - 平均≥2级: 40%
     * - 平均<2级: 20%
     */
    private fun calculateBalanceBonus(employees: List<Employee>): Float {
        if (employees.size <= 1) return 0f
        
        val skillLevels = employees.map { it.getSpecialtySkillLevel() }
        val avg = skillLevels.average()
        val variance = skillLevels.map { (it - avg) * (it - avg) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        
        // 基础平衡性加成
        val baseBonus = when {
            stdDev <= 1.0 -> 0.6f
            stdDev <= 2.0 -> 0.4f
            stdDev <= 3.0 -> 0.2f
            else -> 0f
        }
        
        // 根据平均技能等级调整系数
        val avgFactor = when {
            avg >= 4.0 -> 1.0f
            avg >= 3.0 -> 0.7f
            avg >= 2.0 -> 0.4f
            else -> 0.2f
        }
        
        return baseBonus * avgFactor
    }
    
    /**
     * 计算精英团队加成（10分制）
     * 奖励拥有多个高级员工（≥4级）的团队
     *
     * 高级员工（4-5级）比例加成：
     * - 20%以上: +0.10分
     * - 40%以上: +0.20分
     * - 60%以上: +0.30分
     * - 80%以上: +0.40分
     * - 100%: +0.60分
     */
    private fun calculateEliteBonus(employees: List<Employee>): Float {
        if (employees.isEmpty()) return 0f
        
        val eliteCount = employees.count { it.getSpecialtySkillLevel() >= 4 }
        val eliteRatio = eliteCount.toFloat() / employees.size
        
        return when {
            eliteRatio >= 1.0f -> 0.60f
            eliteRatio >= 0.8f -> 0.40f
            eliteRatio >= 0.6f -> 0.30f
            eliteRatio >= 0.4f -> 0.20f
            eliteRatio >= 0.2f -> 0.10f
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
        // 过滤掉客服，客服不参与开发评分
        val developmentEmployees = assignedEmployees.filter { it.position != "客服" }
        
        if (developmentEmployees.isEmpty()) return BASE_SCORE
        
        // 技能评分
        val skillScore = developmentEmployees.sumOf { employee ->
            calculateSkillContribution(employee.getSpecialtySkillLevel()).toDouble()
        }.toFloat().coerceAtMost(MAX_SKILL_SCORE)
        
        // 团队协作加成
        val teamworkBonus = calculateTeamworkBonus(developmentEmployees)
        
        // 平衡性加成
        val balanceBonus = calculateBalanceBonus(developmentEmployees)
        
        // 精英团队加成
        val eliteBonus = calculateEliteBonus(developmentEmployees)
        
        val estimated = BASE_SCORE + skillScore + teamworkBonus + balanceBonus + eliteBonus
        return estimated.coerceIn(0f, MAX_SCORE)
    }
}