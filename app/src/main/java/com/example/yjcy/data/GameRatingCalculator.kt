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
 * 2. 技能评分：采用递减收益曲线（1-5.5分）
 * 3. 团队协作加成：多职位配合（0-1.5分）
 * 4. 复杂度惩罚：已移除，多平台和网游不再扣分
 * 5. 平衡性加成：员工技能均衡度（0-0.5分）
 * 6. 精英团队加成：高级员工比例（0-0.5分）
 * 
 * 最终评分 = 基础分 + 技能评分 + 团队协作 + 平衡性 + 精英加成
 * 理论最高分：10.0分（5职位满配，全员5级）
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
        val skillScore = skillContributions.sumOf { it.contribution.toDouble() }.toFloat().coerceAtMost(5.8f)
        
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
        
        // 输出详细评分日志
        android.util.Log.d("GameRatingCalculator", "═══════════════════════════════════════")
        android.util.Log.d("GameRatingCalculator", "📊 游戏评分计算: ${game.name}")
        android.util.Log.d("GameRatingCalculator", "═══════════════════════════════════════")
        android.util.Log.d("GameRatingCalculator", "【员工统计】")
        android.util.Log.d("GameRatingCalculator", "  总分配员工数: ${game.assignedEmployees.size}")
        android.util.Log.d("GameRatingCalculator", "  开发员工数: ${developmentEmployees.size} (已排除客服)")
        
        // 统计各岗位人数
        val positionCounts = developmentEmployees.groupBy { it.position }.mapValues { it.value.size }
        android.util.Log.d("GameRatingCalculator", "  岗位分布: ${positionCounts.entries.joinToString(", ") { "${it.key}×${it.value}" }}")
        
        // 统计技能等级分布
        val skillLevelCounts = developmentEmployees.groupBy { it.getSpecialtySkillLevel() }.mapValues { it.value.size }
        android.util.Log.d("GameRatingCalculator", "  技能等级分布: ${skillLevelCounts.entries.sortedByDescending { it.key }.joinToString(", ") { "Lv${it.key}×${it.value}" }}")
        
        android.util.Log.d("GameRatingCalculator", "")
        android.util.Log.d("GameRatingCalculator", "【评分详情】")
        android.util.Log.d("GameRatingCalculator", "  基础分: $BASE_SCORE")
        
        // 技能评分详情
        val rawSkillScore = skillContributions.sumOf { it.contribution.toDouble() }.toFloat()
        android.util.Log.d("GameRatingCalculator", "  技能评分: $rawSkillScore (原始) → $skillScore (封顶5.8)")
        skillContributions.forEach { contribution ->
            android.util.Log.d("GameRatingCalculator", "    - ${contribution.employeeName}(${contribution.skillType} Lv${contribution.skillLevel}): +${contribution.contribution}")
        }
        
        // 团队协作详情
        val uniquePositions = developmentEmployees.map { it.position }.toSet().size
        android.util.Log.d("GameRatingCalculator", "  团队协作: +$teamworkBonus (${uniquePositions}个不同职位)")
        
        // 平衡性详情
        if (developmentEmployees.size > 1) {
            val skillLevels = developmentEmployees.map { it.getSpecialtySkillLevel() }
            val avg = skillLevels.average()
            val variance = skillLevels.map { (it - avg) * (it - avg) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            android.util.Log.d("GameRatingCalculator", "  平衡性加成: +$balanceBonus (标准差=${String.format("%.2f", stdDev)}, 标准差≤1.0时+0.5)")
        } else {
            android.util.Log.d("GameRatingCalculator", "  平衡性加成: +$balanceBonus (员工数≤1，无加成)")
        }
        
        // 精英团队详情
        val eliteCount = developmentEmployees.count { it.getSpecialtySkillLevel() >= 4 }
        val eliteRatio = if (developmentEmployees.isNotEmpty()) eliteCount.toFloat() / developmentEmployees.size else 0f
        android.util.Log.d("GameRatingCalculator", "  精英团队加成: +$eliteBonus (${eliteCount}/${developmentEmployees.size}=${(eliteRatio*100).toInt()}%≥4级)")
        
        android.util.Log.d("GameRatingCalculator", "")
        android.util.Log.d("GameRatingCalculator", "【最终评分】")
        android.util.Log.d("GameRatingCalculator", "  $BASE_SCORE + $skillScore + $teamworkBonus - $complexityPenalty + $balanceBonus + $eliteBonus = $rawScore")
        android.util.Log.d("GameRatingCalculator", "  最终得分: $finalScore / $MAX_SCORE")
        android.util.Log.d("GameRatingCalculator", "═══════════════════════════════════════")
        
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
     * 计算单个技能等级的贡献（优化后更容易拿高分）
     * 技能范围：1-5级
     * 
     * 1级: 0.35  2级: 0.60  3级: 0.75  4级: 0.85  5级: 0.95
     * 
     * 设计理念：
     * - 提升高级员工的贡献值
     * - 满配置（24人×5级）确保能拿10分
     * - 技能评分：24×0.95=22.8分 → 封顶到5.8分
     * 
     * 顶配配置：4个开发岗各6人（程序员、策划师、美术师、音效师），全员5级
     * 技能评分：24×0.95=22.8分 → 封顶到5.8分
     */
    private fun calculateSkillContribution(skillLevel: Int): Float {
        return when (skillLevel) {
            1 -> 0.35f
            2 -> 0.60f
            3 -> 0.75f
            4 -> 0.85f
            5 -> 0.95f
            else -> 0f
        }
    }
    
    /**
     * 计算团队协作加成（多职位配合）
     * 激励玩家招募不同职位的员工而不是单一职位
     * 
     * 1个职位: 0分
     * 2个职位: 0.4分
     * 3个职位: 0.8分
     * 4个职位: 1.7分（满配加成 - 4个开发岗满配，提升到1.7）
     * 5个职位: 1.7分（保留兼容性）
     */
    private fun calculateTeamworkBonus(employees: List<Employee>): Float {
        val uniquePositions = employees.map { it.position }.toSet().size
        return when (uniquePositions) {
            1 -> 0f
            2 -> 0.4f
            3 -> 0.8f
            4 -> 1.7f
            5 -> 1.7f
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
        // 过滤掉客服，客服不参与开发评分
        val developmentEmployees = assignedEmployees.filter { it.position != "客服" }
        
        if (developmentEmployees.isEmpty()) return BASE_SCORE
        
        // 技能评分
        val skillScore = developmentEmployees.sumOf { employee ->
            calculateSkillContribution(employee.getSpecialtySkillLevel()).toDouble()
        }.toFloat().coerceAtMost(5.8f)
        
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