package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.BusinessModel
import kotlin.math.min
import kotlin.random.Random

/**
 * 游戏评分计算器 - 10分制评分系统
 *
 * 评分维度（总分10.0）：
 * 1. 基础分：2.0分（固定）
 * 2. 技能评分：3.0分（根据员工数量和技能等级）
 *    - 需要20-25个5级员工才能拿满3.0分
 *    - 每个员工贡献：5级=0.15, 4级=0.10, 3级=0.06, 2级=0.03, 1级=0.01
 * 3. 岗位配置深度：2.5分（每个岗位的人员数量）
 *    - 每个岗位需要5人才能满分
 *    - 单职位满分0.5 × 5职位 = 2.5分
 * 4. 团队协作加成：1.5分（职位多样性）
 *    - 5个不同职位满分
 * 5. 平衡性加成：0.5分（技能均衡度）
 * 6. 精英团队加成：0.5分（≥4级员工比例）
 *
 * 最终评分 = 基础分 + 技能评分 + 岗位配置 + 团队协作 + 平衡性 + 精英加成
 * 评分范围：2.0 - 10.0分
 *
 * 示例：
 * - 4人（每岗位1人，全5级）：2.0 + 0.6 + 0.8 + 1.2 + 0.5 + 0.5 = 5.6分 ✅
 * - 10人（每岗位2人，全5级）：2.0 + 1.5 + 1.0 + 1.2 + 0.5 + 0.5 = 6.7分
 * - 20人（每岗位4人，全5级）：2.0 + 3.0 + 2.0 + 1.5 + 0.5 + 0.5 = 9.5分
 * - 25人（每岗位5人，全5级）：2.0 + 3.0 + 2.5 + 1.5 + 0.5 + 0.5 = 10.0分（满分）✅
 */
object GameRatingCalculator {
    const val BASE_SCORE = 2.0f
    const val MAX_SCORE = 10.0f
    private const val MAX_SKILL_SCORE = 3.0f  // 技能评分最高3.0分
    private const val MAX_DEPTH_SCORE = 2.5f  // 岗位配置深度最高2.5分
    private const val MAX_TEAMWORK_SCORE = 1.5f  // 团队协作最高1.5分
    private const val MAX_BALANCE_SCORE = 0.5f  // 平衡性最高0.5分
    private const val MAX_ELITE_SCORE = 0.5f  // 精英团队最高0.5分
    
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
        
        // 1. 计算技能评分（基于人数和等级）
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
        
        // 2. 计算岗位配置深度（每个岗位的人员数量）
        val depthScore = calculatePositionDepthScore(developmentEmployees)
        
        // 3. 计算团队协作加成（职位多样性）
        val teamworkBonus = calculateTeamworkBonus(developmentEmployees)
        
        // 4. 计算平衡性加成
        val balanceBonus = calculateBalanceBonus(developmentEmployees)
        
        // 5. 计算精英团队加成
        val eliteBonus = calculateEliteBonus(developmentEmployees)
        
        // 计算最终评分
        val rawScore = BASE_SCORE + skillScore + depthScore + teamworkBonus + balanceBonus + eliteBonus
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
            android.util.Log.d("GameRatingCalculator", "  技能评分: $rawSkillScore (原始) -> $skillScore (封顶$MAX_SKILL_SCORE)")
            
            android.util.Log.d("GameRatingCalculator", ">>> 开始遍历员工贡献 数量=${skillContributions.size}")
            for (i in skillContributions.indices) {
                val contribution = skillContributions[i]
                android.util.Log.d("GameRatingCalculator", "    - ${contribution.employeeName}(${contribution.skillType} Lv${contribution.skillLevel}): +${contribution.contribution}")
            }
            
            // 岗位配置深度详情
            android.util.Log.d("GameRatingCalculator", ">>> 输出岗位配置深度")
            val depthDetails = positionCounts.entries.joinToString(", ") { "${it.key}:${it.value}人" }
            android.util.Log.d("GameRatingCalculator", "  岗位配置深度: +$depthScore (最高$MAX_DEPTH_SCORE) - $depthDetails")
            
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
            android.util.Log.d("GameRatingCalculator", "  基础分: $BASE_SCORE")
            android.util.Log.d("GameRatingCalculator", "  技能评分: +$skillScore (最高$MAX_SKILL_SCORE)")
            android.util.Log.d("GameRatingCalculator", "  岗位配置深度: +$depthScore (最高$MAX_DEPTH_SCORE)")
            android.util.Log.d("GameRatingCalculator", "  团队协作: +$teamworkBonus (最高$MAX_TEAMWORK_SCORE)")
            android.util.Log.d("GameRatingCalculator", "  平衡性加成: +$balanceBonus (最高$MAX_BALANCE_SCORE)")
            android.util.Log.d("GameRatingCalculator", "  精英团队加成: +$eliteBonus (最高$MAX_ELITE_SCORE)")
            android.util.Log.d("GameRatingCalculator", "  计算: $BASE_SCORE + $skillScore + $depthScore + $teamworkBonus + $balanceBonus + $eliteBonus = $rawScore")
            android.util.Log.d("GameRatingCalculator", "  最终得分: $finalScore / $MAX_SCORE")
            android.util.Log.d("GameRatingCalculator", "  距离满分还差: ${MAX_SCORE - finalScore} 分")
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
     * 计算单个技能等级的贡献
     * 技能范围：1-5级
     *
     * 1级: 0.01  2级: 0.03  3级: 0.06  4级: 0.10  5级: 0.15
     *
     * 设计理念：
     * - 需要20个5级员工（0.15×20=3.0）才能达到技能评分满分
     * - 鼓励招募更多高级员工
     */
    private fun calculateSkillContribution(skillLevel: Int): Float {
        return when (skillLevel) {
            1 -> 0.01f
            2 -> 0.03f
            3 -> 0.06f
            4 -> 0.10f
            5 -> 0.15f
            else -> 0f
        }
    }
    
    /**
     * 计算岗位配置深度（每个岗位的人员数量）
     * 每个岗位需要5人才能满分0.5分
     * 最多5个岗位 × 0.5 = 2.5分
     *
     * 计算公式：min(岗位人数, 5) / 5 × 0.5
     */
    private fun calculatePositionDepthScore(employees: List<Employee>): Float {
        if (employees.isEmpty()) return 0f
        
        // 按岗位分组统计人数
        val positionCounts = employees.groupBy { it.position }.mapValues { it.value.size }
        
        // 每个岗位的贡献：min(人数, 5) / 5 × 0.5
        val totalScore = positionCounts.values.sumOf { count ->
            (minOf(count, 5).toFloat() / 5f * 0.5f).toDouble()
        }.toFloat()
        
        return totalScore.coerceAtMost(MAX_DEPTH_SCORE)
    }
    
    /**
     * 计算团队协作加成（多职位配合）
     * 激励玩家招募不同职位的员工而不是单一职位
     *
     * 1个职位: 0分
     * 2个职位: 0.3分
     * 3个职位: 0.7分
     * 4个职位: 1.2分
     * 5个职位: 1.5分（满分）
     */
    private fun calculateTeamworkBonus(employees: List<Employee>): Float {
        val uniquePositions = employees.map { it.position }.toSet().size
        return when (uniquePositions) {
            1 -> 0f
            2 -> 0.3f
            3 -> 0.7f
            4 -> 1.2f
            else -> 1.5f  // 5个及以上职位满分
        }
    }
    
    
    /**
     * 计算平衡性加成（10分制）
     * 奖励技能分布均衡的团队，避免极端情况
     *
     * 计算方法：
     * - 计算所有员工技能等级的标准差
     * - 标准差越小，团队越均衡
     *
     * 标准差加成：
     * - 标准差≤1.0: +0.5分
     * - 标准差≤2.0: +0.3分
     * - 标准差≤3.0: +0.1分
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
     * 计算精英团队加成（10分制）
     * 奖励拥有多个高级员工（≥4级）的团队
     *
     * 高级员工（4-5级）比例加成：
     * - 20%以上: +0.10分
     * - 40%以上: +0.20分
     * - 60%以上: +0.30分
     * - 80%以上: +0.40分
     * - 100%: +0.50分
     */
    private fun calculateEliteBonus(employees: List<Employee>): Float {
        if (employees.isEmpty()) return 0f
        
        val eliteCount = employees.count { it.getSpecialtySkillLevel() >= 4 }
        val eliteRatio = eliteCount.toFloat() / employees.size
        
        return when {
            eliteRatio >= 1.0f -> 0.50f
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
     * @param assignedEmployees 分配的员工列表
     * @param game 游戏对象（用于兼容性，不再使用）
     * @return Float 预计的最终评分（仅供参考）
     */
    fun previewRating(assignedEmployees: List<Employee>, game: Game): Float {
        // 过滤掉客服，客服不参与开发评分
        val developmentEmployees = assignedEmployees.filter { it.position != "客服" }
        
        if (developmentEmployees.isEmpty()) return BASE_SCORE
        
        // 技能评分
        val skillScore = developmentEmployees.sumOf { employee ->
            calculateSkillContribution(employee.getSpecialtySkillLevel()).toDouble()
        }.toFloat().coerceAtMost(MAX_SKILL_SCORE)
        
        // 岗位配置深度
        val depthScore = calculatePositionDepthScore(developmentEmployees)
        
        // 团队协作加成
        val teamworkBonus = calculateTeamworkBonus(developmentEmployees)
        
        // 平衡性加成
        val balanceBonus = calculateBalanceBonus(developmentEmployees)
        
        // 精英团队加成
        val eliteBonus = calculateEliteBonus(developmentEmployees)
        
        return (BASE_SCORE + skillScore + depthScore + teamworkBonus + balanceBonus + eliteBonus).coerceAtMost(MAX_SCORE)
    }
}