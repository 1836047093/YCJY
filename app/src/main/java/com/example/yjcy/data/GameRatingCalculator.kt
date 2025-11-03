package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.BusinessModel
import kotlin.math.min
import kotlin.random.Random

/**
 * 游戏评分计算器 - 10分制评分系统
 *
 * 评分维度：
 * 1. 基础分：2.0分（固定）
 * 2. 技能评分：根据员工技能等级（动态封顶）
 *    - 5级员工团队（平均≥4.8级）：最高4.0分 → 总分10分
 *    - 4级员工团队（平均≥3.8级）：最高2.5分 → 总分8-9分
 *    - 3级员工团队（平均≥2.8级）：最高1.5分 → 总分6-7分
 *    - 2级员工团队（平均≥1.8级）：最高0.3分 → 总分4-5分
 *    - 1级员工团队（平均<1.8级）：最高0.0分 → 总分1-3分
 * 3. 团队协作加成：多职位配合（根据等级调整）
 *    - 5-4级团队：保持原值（最高3.0分）
 *    - 3级团队：降低到85%（最高2.55分）
 *    - 2级团队：降低到40%（最高1.2分）
 *    - 1级团队：降低到15%（最高0.45分）
 * 4. 平衡性加成：员工技能均衡度（最高0.5分）
 * 5. 精英团队加成：高级员工比例（根据等级调整）
 *    - 4-5级团队：保持原值（最高0.5分）
 *    - 3级及以下：不给予精英加成（0分）
 *
 * 最终评分 = 基础分 + 技能评分 + 团队协作 + 平衡性 + 精英加成
 * 评分范围：1.0 - 10.0分
 *
 * 示例（25人满配，4个职位）：
 * - 25个5级员工：2.0 + 4.0 + 3.0 + 0.5 + 0.5 = 10.0分（满分）
 * - 25个4级员工：2.0 + 2.5 + 3.0 + 0.5 + 0.5 = 8.5分
 * - 25个3级员工：2.0 + 1.5 + 2.55 + 0.5 + 0 = 6.55分
 * - 25个2级员工：2.0 + 0.3 + 1.2 + 0.5 + 0 = 4.0分
 * - 25个1级员工：2.0 + 0.0 + 0.45 + 0.5 + 0 = 2.95分
 */
object GameRatingCalculator {
    const val BASE_SCORE = 2.0f
    const val MAX_SCORE = 10.0f
    // 根据平均技能等级设置不同的技能评分封顶值
    private const val MAX_SKILL_SCORE_LV5 = 4.0f  // 5级员工团队：封顶4.0分 → 总分10分
    private const val MAX_SKILL_SCORE_LV4 = 2.5f  // 4级员工团队：封顶2.5分 → 总分8.5分
    private const val MAX_SKILL_SCORE_LV3 = 1.5f  // 3级员工团队：封顶1.5分 → 总分约6.5-7分
    private const val MAX_SKILL_SCORE_LV2 = 0.3f  // 2级员工团队：封顶0.3分 → 总分约4.5-5分
    private const val MAX_SKILL_SCORE_LV1 = 0.0f  // 1级员工团队：封顶0.0分 → 总分约2-3分
    
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
        // 根据团队平均技能等级动态调整技能评分封顶值
        val avgSkillLevel = if (developmentEmployees.isNotEmpty()) {
            developmentEmployees.map { it.getSpecialtySkillLevel() }.average()
        } else {
            0.0
        }
        val maxSkillScore = when {
            avgSkillLevel >= 4.8 -> MAX_SKILL_SCORE_LV5  // 5级团队：封顶4.0 → 总分10分
            avgSkillLevel >= 3.8 -> MAX_SKILL_SCORE_LV4  // 4级团队：封顶2.5 → 总分8.5分
            avgSkillLevel >= 2.8 -> MAX_SKILL_SCORE_LV3  // 3级团队：封顶1.0 → 总分6.5-7分
            avgSkillLevel >= 1.8 -> MAX_SKILL_SCORE_LV2  // 2级团队：封顶0.3 → 总分4.5-5分
            else -> MAX_SKILL_SCORE_LV1                  // 1级团队：封顶0.0 → 总分2-3分
        }
        val skillScore = skillContributions.sumOf { it.contribution.toDouble() }.toFloat().coerceAtMost(maxSkillScore)
        
        // 2. 计算团队协作加成（多职位配合）- 根据平均等级调整
        val baseTeamworkBonus = calculateTeamworkBonus(developmentEmployees)
        val teamworkBonus = adjustTeamworkBonusByLevel(baseTeamworkBonus, avgSkillLevel)
        
        // 3. 计算平衡性加成
        val balanceBonus = calculateBalanceBonus(developmentEmployees)
        
        // 4. 计算精英团队加成 - 根据平均等级调整
        val baseEliteBonus = calculateEliteBonus(developmentEmployees)
        val eliteBonus = adjustEliteBonusByLevel(baseEliteBonus, avgSkillLevel)
        
        // 计算最终评分
        val rawScore = BASE_SCORE + skillScore + teamworkBonus + balanceBonus + eliteBonus
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
            android.util.Log.d("GameRatingCalculator", "  平均技能等级: ${String.format("%.2f", avgSkillLevel)}")
            android.util.Log.d("GameRatingCalculator", "  技能评分: $rawSkillScore (原始) -> $skillScore (封顶$maxSkillScore)")
            
            android.util.Log.d("GameRatingCalculator", ">>> 开始遍历员工贡献 数量=${skillContributions.size}")
            for (i in skillContributions.indices) {
                val contribution = skillContributions[i]
                android.util.Log.d("GameRatingCalculator", "    - ${contribution.employeeName}(${contribution.skillType} Lv${contribution.skillLevel}): +${contribution.contribution}")
            }
            
            // 团队协作详情
            android.util.Log.d("GameRatingCalculator", ">>> 输出团队协作")
            val uniquePositions = developmentEmployees.map { it.position }.toSet().size
            val baseTeamworkForLog = calculateTeamworkBonus(developmentEmployees)
            if (baseTeamworkForLog != teamworkBonus) {
                android.util.Log.d("GameRatingCalculator", "  团队协作: +$baseTeamworkForLog (基础) -> +$teamworkBonus (调整后, ${uniquePositions}个不同职位)")
            } else {
                android.util.Log.d("GameRatingCalculator", "  团队协作: +$teamworkBonus (${uniquePositions}个不同职位)")
            }
            
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
            val baseEliteForLog = calculateEliteBonus(developmentEmployees)
            if (baseEliteForLog != eliteBonus) {
                android.util.Log.d("GameRatingCalculator", "  精英团队加成: +$baseEliteForLog (基础) -> +$eliteBonus (调整后, ${eliteCount}/${developmentEmployees.size}=${elitePercent}% >=4级)")
            } else {
                android.util.Log.d("GameRatingCalculator", "  精英团队加成: +$eliteBonus (${eliteCount}/${developmentEmployees.size}=${elitePercent}% >=4级)")
            }
            
            android.util.Log.d("GameRatingCalculator", "")
            android.util.Log.d("GameRatingCalculator", "[最终评分]")
            android.util.Log.d("GameRatingCalculator", "  基础分: $BASE_SCORE")
            android.util.Log.d("GameRatingCalculator", "  技能评分: +$skillScore")
            android.util.Log.d("GameRatingCalculator", "  团队协作: +$teamworkBonus")
            android.util.Log.d("GameRatingCalculator", "  平衡性加成: +$balanceBonus")
            android.util.Log.d("GameRatingCalculator", "  精英团队加成: +$eliteBonus")
            android.util.Log.d("GameRatingCalculator", "  计算: $BASE_SCORE + $skillScore + $teamworkBonus + $balanceBonus + $eliteBonus = $rawScore")
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
     * 计算单个技能等级的贡献（10分制）
     * 技能范围：1-5级
     *
     * 1级: 0.30  2级: 0.50  3级: 0.65  4级: 0.75  5级: 0.85
     *
     * 设计理念：
     * - 技能评分部分最高 4.0 分（24人×0.85 → 封顶）
     * - 保持递减收益，鼓励培养高等级员工
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
     * 2个职位: 1.0分
     * 3个职位: 2.0分
     * 4个职位: 3.0分（满分）
     * 5个职位及以上: 3.0分（满分）
     */
    private fun calculateTeamworkBonus(employees: List<Employee>): Float {
        val uniquePositions = employees.map { it.position }.toSet().size
        return when (uniquePositions) {
            1 -> 0f
            2 -> 1.0f
            3 -> 2.0f
            4 -> 3.0f
            else -> 3.0f  // 4个及以上职位都满分
        }
    }
    
    /**
     * 计算主题匹配加成（最高1.0分）
     * 根据游戏主题和核心职位的技能等级计算加成
     * 
     * 核心职位定义：
     * - 动作/射击/体育/竞速：程序员
     * - RPG/冒险/策略/恐怖：策划师
     * - 休闲：美工、音乐家
     * - 模拟/解谜：策划师、程序员
     * - MOBA：策划师、程序员
     * 
     * 计算公式：核心职位的平均技能等级 × 0.1（上限1.0分）
     */
    private fun calculateThemeBonus(game: Game, employees: List<Employee>): Float {
        if (employees.isEmpty()) return 0f
        
        val theme = game.theme.name
        
        // 职位名称映射 - 统一职位名称
        fun normalizePosition(position: String): String {
            return when (position) {
                "策划师" -> "设计"
                "美术师" -> "美工"
                "音效师" -> "音乐"
                "程序员" -> "开发"
                else -> position
            }
        }
        
        val corePositions = when (theme) {
            "ACTION", "SHOOTER", "SPORTS", "RACING" -> listOf("开发")
            "RPG", "ADVENTURE", "STRATEGY", "HORROR" -> listOf("设计")
            "CASUAL" -> listOf("美工", "音乐")
            "SIMULATION", "PUZZLE" -> listOf("设计", "开发")
            "MOBA" -> listOf("设计", "开发")
            else -> listOf<String>()
        }
        
        if (corePositions.isEmpty()) return 0f
        
        // 获取核心职位的员工（使用标准化后的职位名称）
        val coreEmployees = employees.filter { normalizePosition(it.position) in corePositions }
        if (coreEmployees.isEmpty()) return 0f
        
        // 计算核心职位的平均技能等级
        val avgSkillLevel = coreEmployees.map { it.getSpecialtySkillLevel() }.average()
        
        // 主题匹配加成 = 平均技能等级 × 0.1，上限1.0分
        val bonus = (avgSkillLevel * 0.1f).toFloat().coerceAtMost(1.0f)
        
        return bonus
    }
    
    /**
     * 计算复杂度惩罚
     * 多平台和网游会扣分
     */
    private fun calculateComplexityPenalty(game: Game): Float {
        var penalty = 0f
        
        // 多平台惩罚：每增加1个平台-0.2分
        val platformCount = game.platforms.size
        if (platformCount > 1) {
            penalty += (platformCount - 1) * 0.2f
        }
        
        // 网络游戏惩罚：-0.5分
        if (game.businessModel == BusinessModel.ONLINE_GAME) {
            penalty += 0.5f
        }
        
        return penalty
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
     * 根据平均技能等级调整团队协作加成
     * 低等级团队降低团队协作加成，使总分控制在目标范围
     */
    private fun adjustTeamworkBonusByLevel(baseBonus: Float, avgSkillLevel: Double): Float {
        return when {
            avgSkillLevel >= 4.8 -> baseBonus  // 5级团队：保持原值（3.0）
            avgSkillLevel >= 3.8 -> baseBonus  // 4级团队：保持原值（3.0）
            avgSkillLevel >= 2.8 -> baseBonus * 0.85f  // 3级团队：降低到85%（2.55）
            avgSkillLevel >= 1.8 -> baseBonus * 0.4f   // 2级团队：降低到40%（1.2）
            else -> baseBonus * 0.15f                  // 1级团队：降低到15%（0.45）
        }
    }
    
    /**
     * 根据平均技能等级调整精英团队加成
     * 低等级团队不给予精英团队加成
     */
    private fun adjustEliteBonusByLevel(baseBonus: Float, avgSkillLevel: Double): Float {
        return when {
            avgSkillLevel >= 3.8 -> baseBonus  // 4-5级团队：保持原值
            else -> 0f                          // 3级及以下：不给予精英加成
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
        
        // 技能评分 - 根据平均等级动态调整封顶值
        val avgSkillLevel = if (developmentEmployees.isNotEmpty()) {
            developmentEmployees.map { it.getSpecialtySkillLevel() }.average()
        } else {
            0.0
        }
        val maxSkillScore = when {
            avgSkillLevel >= 4.8 -> MAX_SKILL_SCORE_LV5
            avgSkillLevel >= 3.8 -> MAX_SKILL_SCORE_LV4
            avgSkillLevel >= 2.8 -> MAX_SKILL_SCORE_LV3
            avgSkillLevel >= 1.8 -> MAX_SKILL_SCORE_LV2
            else -> MAX_SKILL_SCORE_LV1
        }
        val skillScore = developmentEmployees.sumOf { employee ->
            calculateSkillContribution(employee.getSpecialtySkillLevel()).toDouble()
        }.toFloat().coerceAtMost(maxSkillScore)
        
        // 团队协作加成 - 根据平均等级调整
        val baseTeamworkBonus = calculateTeamworkBonus(developmentEmployees)
        val teamworkBonus = adjustTeamworkBonusByLevel(baseTeamworkBonus, avgSkillLevel)
        
        // 平衡性加成
        val balanceBonus = calculateBalanceBonus(developmentEmployees)
        
        // 精英团队加成 - 根据平均等级调整
        val baseEliteBonus = calculateEliteBonus(developmentEmployees)
        val eliteBonus = adjustEliteBonusByLevel(baseEliteBonus, avgSkillLevel)
        
        val estimated = BASE_SCORE + skillScore + teamworkBonus + balanceBonus + eliteBonus
        return estimated.coerceIn(0f, MAX_SCORE)
    }
}