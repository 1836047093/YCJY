package com.example.yjcy.data

import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.Platform

/**
 * 子公司运营状态
 */
enum class SubsidiaryStatus {
    ACTIVE,      // 运营中
    SUSPENDED,   // 暂停营业
    LIQUIDATED   // 已清算
}

/**
 * 子公司开发偏好
 */
enum class DevelopmentPreference {
    SINGLE_PLAYER_ONLY,  // 只开发单机游戏
    ONLINE_GAME_ONLY,    // 只开发网游
    BOTH                 // 都开发
}

/**
 * 游戏更新策略
 */
enum class GameUpdateStrategy {
    AGGRESSIVE,  // 激进（频繁更新）
    MODERATE,    // 适中（定期更新）
    CONSERVATIVE // 保守（很少更新）
}

/**
 * 子公司正在开发的游戏
 */
data class DevelopingGame(
    val id: String,                           // 游戏ID
    val name: String,                         // 游戏名称
    val theme: GameTheme,                     // 游戏主题
    val platforms: List<Platform>,            // 平台
    val businessModel: BusinessModel,         // 商业模式
    val currentPhase: DevelopmentPhase = DevelopmentPhase.DESIGN, // 当前开发阶段
    val phaseProgress: Float = 0f,            // 当前阶段进度 (0-100)
    val startDate: GameDate,                  // 开始开发日期
    val estimatedRating: Float = 0f           // 预估评分（基于员工技能）
)

/**
 * 网游付费内容价格配置（5个付费内容）
 */
data class OnlineGamePricing(
    val price1: Int? = null,        // 第1个付费内容价格（null表示使用默认）
    val price2: Int? = null,        // 第2个付费内容价格（null表示使用默认）
    val price3: Int? = null,        // 第3个付费内容价格（null表示使用默认）
    val price4: Int? = null,        // 第4个付费内容价格（null表示使用默认）
    val price5: Int? = null         // 第5个付费内容价格（null表示使用默认）
)

/**
 * 子公司游戏管理配置
 */
data class SubsidiaryGameConfig(
    val gameId: String,                           // 游戏ID
    val customPrice: Int? = null,                 // 自定义价格（单机游戏）（null表示使用默认价格）
    val onlineGamePricing: OnlineGamePricing? = null, // 网游付费内容价格配置
    val updateStrategy: GameUpdateStrategy = GameUpdateStrategy.MODERATE // 更新策略
)

/**
 * 子公司数据类（基于收购的竞争对手公司）
 */
data class Subsidiary(
    val id: Int,                              // 原竞争对手ID
    val name: String,                         // 公司名称
    val logo: String,                         // 公司LOGO
    val acquisitionPrice: Long,               // 收购价格
    val acquisitionDate: GameDate,            // 收购日期
    
    // 财务数据
    val marketValue: Long,                    // 当前市值
    val cashBalance: Long = 0L,               // 当前资金（现金余额）
    val monthlyRevenue: Long = 0L,            // 月度收入
    val monthlyExpense: Long = 0L,            // 月度支出
    val totalRevenue: Long = 0L,              // 累计总收入（收购后）
    
    // 游戏数据
    val games: List<CompetitorGame>,          // 已发售的游戏
    val developingGames: List<DevelopingGame> = emptyList(), // 正在开发的游戏
    
    // 员工数据（根据游戏反推）
    val estimatedEmployeeCount: Int,          // 估算员工数（基于游戏数量）
    val monthlyWageCost: Long,                // 月度工资成本
    
    // 管理设置
    val profitSharingRate: Float = 0.5f,      // 利润分成比例（总公司抽成）
    val autoManagement: Boolean = true,       // 自动管理（默认开启）
    val status: SubsidiaryStatus = SubsidiaryStatus.ACTIVE,
    
    // 游戏管理
    val developmentPreference: DevelopmentPreference = DevelopmentPreference.BOTH, // 开发偏好
    val gameConfigs: Map<String, SubsidiaryGameConfig> = emptyMap() // 各游戏的管理配置
) {
    /**
     * 计算月度利润
     */
    fun getMonthlyProfit(): Long {
        return monthlyRevenue - monthlyExpense
    }
    
    /**
     * 计算上缴总公司的利润
     */
    fun getProfitShare(): Long {
        val profit = getMonthlyProfit()
        return if (profit > 0) {
            (profit * profitSharingRate).toLong()
        } else {
            0L
        }
    }
    
    /**
     * 计算投资回报率（ROI）
     */
    fun getROI(): Float {
        if (acquisitionPrice == 0L) return 0f
        return ((totalRevenue - acquisitionPrice).toFloat() / acquisitionPrice) * 100f
    }
    
    /**
     * 获取网游数量
     */
    fun getOnlineGameCount(): Int {
        return games.count { it.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME }
    }
    
    /**
     * 获取单机游戏数量
     */
    fun getSinglePlayerGameCount(): Int {
        return games.count { it.businessModel == com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER }
    }
    
    /**
     * 计算总活跃玩家数（所有网游）
     */
    fun getTotalActivePlayers(): Long {
        return games.filter { it.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME }
            .sumOf { it.activePlayers }
    }
    
    /**
     * 计算总销量（所有单机游戏）
     */
    fun getTotalSales(): Long {
        return games.filter { it.businessModel == com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER }
            .sumOf { it.salesCount }
    }
}

/**
 * 子公司管理器
 */
object SubsidiaryManager {
    
    /**
     * 估算员工数量（基于游戏数量）
     */
    fun estimateEmployeeCount(company: CompetitorCompany): Int {
        val baseEmployees = 5 // 基础管理人员
        val gameEmployees = company.games.size * 5 // 每款游戏5人
        return baseEmployees + gameEmployees
    }
    
    /**
     * 估算月度工资成本
     */
    fun estimateWageCost(company: CompetitorCompany): Long {
        val employeeCount = estimateEmployeeCount(company)
        val avgSalary = 15000L // 平均月薪1.5万
        return employeeCount * avgSalary
    }
    
    /**
     * 创建子公司（从被收购的竞争对手）
     */
    fun createSubsidiary(
        company: CompetitorCompany,
        acquisitionPrice: Long,
        acquisitionDate: GameDate
    ): Subsidiary {
        // 初始资金：市值的10%作为启动资金
        val initialCash = (company.marketValue * 0.1).toLong().coerceAtLeast(500000L) // 最低50万
        
        return Subsidiary(
            id = company.id,
            name = company.name,
            logo = company.logo,
            acquisitionPrice = acquisitionPrice,
            acquisitionDate = acquisitionDate,
            marketValue = company.marketValue,
            cashBalance = initialCash, // 设置初始资金
            monthlyRevenue = 0L,
            monthlyExpense = 0L,
            totalRevenue = 0L,
            games = company.games,
            estimatedEmployeeCount = estimateEmployeeCount(company),
            monthlyWageCost = estimateWageCost(company),
            profitSharingRate = 0.5f,
            autoManagement = true,
            status = SubsidiaryStatus.ACTIVE
        )
    }
    
    /**
     * 计算子公司月度收入
     */
    fun calculateMonthlyIncome(subsidiary: Subsidiary): Long {
        var totalIncome = 0L
        
        subsidiary.games.forEach { game ->
            when (game.businessModel) {
                com.example.yjcy.ui.BusinessModel.ONLINE_GAME -> {
                    // 🔧 修复：使用完整的付费内容系统计算收入（5个付费内容，总付费率约3.5%）
                    // 而不是简化公式（0.5%付费率）
                    val monthlyRevenue = CompetitorManager.calculateCompetitorMonetizationRevenue(
                        game.activePlayers, 
                        game.theme
                    ).toLong()
                    totalIncome += monthlyRevenue
                }
                com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER -> {
                    // 单机：持续销量（已发售游戏逐月衰减2%）
                    // 估算月销量 = 总销量 * 0.01 * 50元
                    val monthlySales = (game.salesCount * 0.01 * 50).toLong()
                    totalIncome += monthlySales
                }
            }
        }
        
        return totalIncome
    }
    
    /**
     * 计算服务器成本（网游）- 使用与玩家相同的服务器租用逻辑
     */
    fun calculateServerCost(games: List<CompetitorGame>): Long {
        val onlineGames = games.filter { it.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME }
        var totalServerCost = 0L
        
        onlineGames.forEach { game ->
            val activePlayers = game.activePlayers.coerceAtLeast(10000L) // 最少按1万玩家计算
            
            // 估算需要的服务器（优先使用性价比高的服务器）
            // ADVANCED: 200万容量, 500万/月, 性价比最高 (0.25万/万人)
            // INTERMEDIATE: 50万容量, 300万/月
            // BASIC: 10万容量, 100万/月
            
            var remainingPlayers = activePlayers
            var serverCost = 0L
            
            // 1. 优先使用ADVANCED服务器（200万容量）
            val advancedCount = (remainingPlayers / 2000000L).toInt()
            if (advancedCount > 0) {
                serverCost += advancedCount * 5000000L
                remainingPlayers -= advancedCount * 2000000L
            }
            
            // 2. 剩余使用INTERMEDIATE服务器（50万容量）
            val intermediateCount = (remainingPlayers / 500000L).toInt()
            if (intermediateCount > 0) {
                serverCost += intermediateCount * 3000000L
                remainingPlayers -= intermediateCount * 500000L
            }
            
            // 3. 最后使用BASIC服务器（10万容量）补足
            if (remainingPlayers > 0) {
                val basicCount = ((remainingPlayers + 99999L) / 100000L).toInt() // 向上取整
                serverCost += basicCount * 1000000L
            }
            
            totalServerCost += serverCost
        }
        
        return totalServerCost
    }
    
    /**
     * 计算其他成本（运营成本）
     */
    fun calculateOtherCosts(subsidiary: Subsidiary): Long {
        // 基础运营成本：每款游戏1万元/月
        return subsidiary.games.size * 10000L
    }
    
    /**
     * 推进游戏开发进度
     * @return 更新后的开发中游戏列表
     */
    private fun updateDevelopingGames(
        developingGames: List<DevelopingGame>,
        employeeCount: Int
    ): List<DevelopingGame> {
        return developingGames.map { game ->
            // 基础进度：每月2%
            val baseProgress = 2f
            
            // 员工数量加成（每5名员工+0.5%，最多+3%）
            val employeeBonus = (employeeCount / 5 * 0.5f).coerceAtMost(3f)
            
            // 总进度增加
            val progressIncrease = baseProgress + employeeBonus
            val newProgress = (game.phaseProgress + progressIncrease).coerceAtMost(100f)
            
            // 检查当前阶段是否完成
            if (newProgress >= 100f) {
                // 进入下一阶段
                when (game.currentPhase) {
                    DevelopmentPhase.DESIGN -> {
                        // 进入美术音效阶段
                        game.copy(
                            currentPhase = DevelopmentPhase.ART_SOUND,
                            phaseProgress = 0f
                        )
                    }
                    DevelopmentPhase.ART_SOUND -> {
                        // 进入程序实现阶段
                        game.copy(
                            currentPhase = DevelopmentPhase.PROGRAMMING,
                            phaseProgress = 0f
                        )
                    }
                    DevelopmentPhase.PROGRAMMING -> {
                        // 保持在100%，等待被移除
                        game.copy(phaseProgress = 100f)
                    }
                }
            } else {
                // 更新当前阶段进度
                game.copy(phaseProgress = newProgress)
            }
        }
    }
    
    /**
     * 检查并移除已完成的游戏
     * @return Pair(剩余的开发中游戏, 完成的游戏列表)
     */
    private fun extractCompletedGames(
        developingGames: List<DevelopingGame>,
        currentDate: GameDate
    ): Pair<List<DevelopingGame>, List<CompetitorGame>> {
        val completed = mutableListOf<CompetitorGame>()
        val remaining = mutableListOf<DevelopingGame>()
        
        developingGames.forEach { game ->
            if (game.currentPhase == DevelopmentPhase.PROGRAMMING && game.phaseProgress >= 100f) {
                // 游戏完成，创建 CompetitorGame
                val finalRating = if (game.estimatedRating > 0) {
                    game.estimatedRating
                } else {
                    // 基于随机生成评分（6.0-8.5）
                    (kotlin.random.Random.nextDouble(6.0, 8.5)).toFloat()
                }
                
                // 计算初始玩家数/销量
                val (initialPlayers, initialSales) = when (game.businessModel) {
                    BusinessModel.ONLINE_GAME -> {
                        // 网游：初始注册数 = 评分 * 2000-5000
                        val registered = (finalRating * kotlin.random.Random.nextInt(2000, 5000)).toLong()
                        Pair(registered, 0L)
                    }
                    BusinessModel.SINGLE_PLAYER -> {
                        // 单机：初始销量 = 评分 * 500-1500
                        val sales = (finalRating * kotlin.random.Random.nextInt(500, 1500)).toLong()
                        Pair(0L, sales)
                    }
                }
                
                completed.add(
                    CompetitorGame(
                        id = game.id,
                        name = game.name,
                        companyId = 0, // 子公司ID，待填充
                        companyName = "", // 子公司名称，待填充
                        theme = game.theme,
                        platforms = game.platforms,
                        businessModel = game.businessModel,
                        rating = finalRating,
                        activePlayers = if (game.businessModel == BusinessModel.ONLINE_GAME) {
                            (initialPlayers * 0.4).toLong()
                        } else 0L,
                        salesCount = initialSales,
                        releaseYear = currentDate.year,
                        releaseMonth = currentDate.month,
                        totalRevenue = 0.0,
                        monetizationRevenue = 0.0,
                        totalRegisteredPlayers = initialPlayers,
                        playerInterest = 100.0,
                        lifecycleProgress = 0.0,
                        daysSinceLaunch = 0,
                        lastInterestDecayDay = 0
                    )
                )
            } else {
                remaining.add(game)
            }
        }
        
        return Pair(remaining, completed)
    }
    
    /**
     * 尝试开始新游戏开发
     * @return 新开发的游戏（可能为null）
     */
    private fun tryStartNewGame(
        subsidiary: Subsidiary,
        currentDate: GameDate
    ): DevelopingGame? {
        // 开发概率：
        // - 少于3个开发中：30%概率
        // - 3-5个开发中：15%概率
        // - 5个以上：5%概率
        val probability = when {
            subsidiary.developingGames.size < 3 -> 0.30
            subsidiary.developingGames.size < 5 -> 0.15
            else -> 0.05
        }
        
        if (kotlin.random.Random.nextDouble() > probability) {
            return null
        }
        
        // 根据开发偏好决定游戏类型
        val businessModel = when (subsidiary.developmentPreference) {
            DevelopmentPreference.SINGLE_PLAYER_ONLY -> BusinessModel.SINGLE_PLAYER
            DevelopmentPreference.ONLINE_GAME_ONLY -> BusinessModel.ONLINE_GAME
            DevelopmentPreference.BOTH -> {
                if (kotlin.random.Random.nextBoolean()) {
                    BusinessModel.SINGLE_PLAYER
                } else {
                    BusinessModel.ONLINE_GAME
                }
            }
        }
        
        // 随机选择主题
        val theme = com.example.yjcy.ui.GameTheme.entries.random()
        
        // 随机选择1-3个平台
        val allPlatforms = com.example.yjcy.ui.Platform.entries
        val platformCount = kotlin.random.Random.nextInt(1, 4)
        val platforms = allPlatforms.shuffled().take(platformCount)
        
        // 生成游戏名称
        val gameName = generateGameName(theme)
        
        // 预估评分（基于员工数量）
        val estimatedRating = when {
            subsidiary.estimatedEmployeeCount >= 30 -> kotlin.random.Random.nextFloat() * 1.5f + 7.5f // 7.5-9.0
            subsidiary.estimatedEmployeeCount >= 20 -> kotlin.random.Random.nextFloat() * 1.0f + 7.0f // 7.0-8.0
            else -> kotlin.random.Random.nextFloat() * 1.0f + 6.0f // 6.0-7.0
        }.coerceIn(6.0f, 9.5f)
        
        return DevelopingGame(
            id = "sub_${subsidiary.id}_${System.currentTimeMillis()}",
            name = gameName,
            theme = theme,
            platforms = platforms,
            businessModel = businessModel,
            currentPhase = DevelopmentPhase.DESIGN,
            phaseProgress = 0f,
            startDate = currentDate,
            estimatedRating = estimatedRating
        )
    }
    
    /**
     * 生成游戏名称
     */
    private fun generateGameName(theme: com.example.yjcy.ui.GameTheme): String {
        val prefixes = listOf("超级", "终极", "王者", "传奇", "无敌", "梦幻", "狂野", "疯狂", "史诗", "极限")
        val suffixes = listOf("之路", "传说", "战记", "物语", "奇遇", "冒险", "征途", "荣耀", "纪元", "世界")
        
        val prefix = if (kotlin.random.Random.nextBoolean()) prefixes.random() else ""
        val suffix = if (kotlin.random.Random.nextBoolean()) suffixes.random() else ""
        
        return "$prefix${theme.displayName}$suffix".trim()
    }
    
    /**
     * 更新子公司月度数据
     */
    fun updateMonthlyData(subsidiary: Subsidiary, currentDate: GameDate): Subsidiary {
        if (subsidiary.status != SubsidiaryStatus.ACTIVE) {
            return subsidiary
        }
        
        // 🆕 1. 推进开发中游戏的进度
        var updatedDevelopingGames = updateDevelopingGames(
            subsidiary.developingGames,
            subsidiary.estimatedEmployeeCount
        )
        
        // 🆕 2. 提取已完成的游戏
        val (remainingDev, completedGames) = extractCompletedGames(
            updatedDevelopingGames,
            currentDate
        )
        updatedDevelopingGames = remainingDev
        
        // 🆕 3. 将完成的游戏添加到已发售列表（填充公司信息）
        val newlyReleasedGames = completedGames.map { game ->
            game.copy(
                companyId = subsidiary.id,
                companyName = subsidiary.name
            )
        }
        
        // 🆕 4. 尝试开始新游戏开发
        val newGame = tryStartNewGame(subsidiary, currentDate)
        if (newGame != null) {
            updatedDevelopingGames = updatedDevelopingGames + newGame
            android.util.Log.d("SubsidiaryManager", 
                "子公司${subsidiary.name}开始开发新游戏《${newGame.name}》（${newGame.theme.displayName}）"
            )
        }
        
        // 计算本月收入
        val monthlyIncome = calculateMonthlyIncome(subsidiary)
        
        // 计算本月支出
        val monthlyExpense = subsidiary.monthlyWageCost + 
                            calculateServerCost(subsidiary.games) +
                            calculateOtherCosts(subsidiary)
        
        // 🆕 更新游戏数据，使用与玩家相同的系统
        val updatedGames = subsidiary.games.map { game ->
            when (game.businessModel) {
                com.example.yjcy.ui.BusinessModel.ONLINE_GAME -> {
                    // 🆕 使用与玩家相同的兴趣值系统（与竞争对手逻辑完全一致）
                    
                    // 1. 更新上线天数（每月30天）
                    val newDaysSinceLaunch = game.daysSinceLaunch + 30
                    
                    // 2. 计算生命周期进度
                    val totalLifecycleDays = 365
                    val newLifecycleProgress = ((newDaysSinceLaunch.toDouble() / totalLifecycleDays) * 100.0).coerceIn(0.0, 100.0)
                    
                    // 3. 检查是否需要衰减兴趣值（每90天衰减一次）
                    val currentDecayInterval = newDaysSinceLaunch / 90
                    val lastDecayInterval = game.lastInterestDecayDay / 90
                    val shouldDecay = currentDecayInterval > lastDecayInterval
                    
                    var newPlayerInterest = game.playerInterest
                    var newLastDecayDay = game.lastInterestDecayDay
                    
                    if (shouldDecay) {
                        val decayRate = when {
                            newLifecycleProgress < 30.0 -> 8.0
                            newLifecycleProgress < 70.0 -> 15.0
                            newLifecycleProgress < 90.0 -> 25.0
                            else -> 35.0
                        }
                        newPlayerInterest = (game.playerInterest - decayRate).coerceIn(0.0, 100.0)
                        newLastDecayDay = newDaysSinceLaunch
                    }
                    
                    // 4. 计算注册数增长（固定每日新增 + 倍率，避免复利爆炸）
                    // 基础每日新增（根据评分）
                    val baseDailyNew = when {
                        game.rating >= 9.0f -> 8000L   // 神作：每日8K
                        game.rating >= 8.5f -> 5000L   // 优秀：每日5K
                        game.rating >= 8.0f -> 3000L   // 良好：每日3K
                        game.rating >= 7.0f -> 1500L   // 一般：每日1.5K
                        else -> 800L                   // 及格：每日800
                    }
                    
                    // 兴趣值倍率（注册数增长用）
                    val registrationInterestMultiplier = when {
                        newPlayerInterest >= 80.0 -> 1.3
                        newPlayerInterest >= 70.0 -> 1.0
                        newPlayerInterest >= 50.0 -> 0.8
                        newPlayerInterest >= 30.0 -> 0.6
                        else -> 0.5
                    }
                    
                    // 生命周期倍率（后期自然衰减）
                    val lifecycleMultiplier = when {
                        newLifecycleProgress < 30.0 -> 1.2   // 成长期：加速
                        newLifecycleProgress < 70.0 -> 1.0   // 成熟期：正常
                        newLifecycleProgress < 90.0 -> 0.6   // 衰退期：减速
                        else -> 0.3                          // 末期：大幅减速
                    }
                    
                    // 月度新增 = 每日新增 × 30天 × 倍率
                    val monthlyNewRegistrations = (baseDailyNew * 30 * registrationInterestMultiplier * lifecycleMultiplier).toLong()
                    val newTotalRegistered = game.totalRegisteredPlayers + monthlyNewRegistrations
                    
                    // 5. 计算活跃玩家数
                    val activeMultiplier = when {
                        newPlayerInterest >= 70.0 -> 1.0
                        newPlayerInterest >= 50.0 -> 0.7
                        newPlayerInterest >= 30.0 -> 0.4
                        else -> 0.2
                    }
                    val newActivePlayers = (newTotalRegistered * 0.4 * activeMultiplier).toLong().coerceAtLeast(100L)
                    
                    // 6. 计算本月收入
                    val monthlyMonetizationRevenue = CompetitorManager.calculateCompetitorMonetizationRevenue(
                        newActivePlayers, 
                        game.theme
                    )
                    
                    // 🆕 7. 检查是否需要更新游戏来恢复兴趣值
                    var finalPlayerInterest = newPlayerInterest
                    
                    // 当兴趣值低于50%时，有概率更新游戏
                    if (newPlayerInterest < 50.0 && newLifecycleProgress < 90.0) {
                        // 更新概率：兴趣值越低，概率越高
                        val updateProbability = when {
                            newPlayerInterest < 30.0 -> 0.30  // 30%概率
                            newPlayerInterest < 40.0 -> 0.20  // 20%概率
                            else -> 0.10                      // 10%概率
                        }
                        
                        if (kotlin.random.Random.nextDouble() < updateProbability) {
                            // 恢复兴趣值（与玩家系统相同）
                            val recoveryAmount = when {
                                newLifecycleProgress < 30.0 -> 25.0  // 成长期：恢复25%
                                newLifecycleProgress < 70.0 -> 15.0  // 成熟期：恢复15%
                                else -> 8.0                          // 衰退期：恢复8%
                            }
                            finalPlayerInterest = (newPlayerInterest + recoveryAmount).coerceIn(0.0, 100.0)
                            
                            android.util.Log.d("SubsidiaryManager", 
                                "子公司${subsidiary.name}更新游戏《${game.name}》，" +
                                "兴趣值从${newPlayerInterest.toInt()}%恢复到${finalPlayerInterest.toInt()}%"
                            )
                        }
                    }
                    
                    game.copy(
                        activePlayers = newActivePlayers,
                        totalRevenue = game.totalRevenue + monthlyMonetizationRevenue,
                        monetizationRevenue = game.monetizationRevenue + monthlyMonetizationRevenue,
                        // 🆕 更新兴趣值系统字段
                        totalRegisteredPlayers = newTotalRegistered,
                        playerInterest = finalPlayerInterest,  // 使用可能已恢复的兴趣值
                        lifecycleProgress = newLifecycleProgress,
                        daysSinceLaunch = newDaysSinceLaunch,
                        lastInterestDecayDay = newLastDecayDay
                    )
                }
                com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER -> {
                    // 🆕 使用与竞争对手相同的复杂销量增长逻辑
                    val baseGrowthRate = when {
                        game.rating >= 9.0f -> kotlin.random.Random.nextDouble(1.2, 2.5)
                        game.rating >= 8.5f -> kotlin.random.Random.nextDouble(0.9, 1.8)
                        game.rating >= 8.0f -> kotlin.random.Random.nextDouble(0.6, 1.2)
                        game.rating >= 7.5f -> kotlin.random.Random.nextDouble(0.4, 0.9)
                        game.rating >= 7.0f -> kotlin.random.Random.nextDouble(0.3, 0.6)
                        else -> kotlin.random.Random.nextDouble(0.15, 0.4)
                    }
                    
                    val proportionalGrowth = (game.salesCount * baseGrowthRate / 100.0).toLong()
                    
                    val minGrowth = when {
                        game.rating >= 9.0f -> kotlin.random.Random.nextInt(5000, 12000)
                        game.rating >= 8.5f -> kotlin.random.Random.nextInt(3000, 8000)
                        game.rating >= 8.0f -> kotlin.random.Random.nextInt(2000, 5000)
                        game.rating >= 7.0f -> kotlin.random.Random.nextInt(1000, 3000)
                        else -> kotlin.random.Random.nextInt(300, 1200)
                    }.toLong()
                    
                    val maxGrowth = (game.salesCount * 0.08).toLong()
                    val salesGrowth = maxOf(proportionalGrowth, minGrowth).coerceAtMost(maxGrowth)
                    
                    val newSales = game.salesCount + salesGrowth
                    val monthlySalesRevenue = (salesGrowth * 50).toDouble()
                    
                    game.copy(
                        salesCount = newSales,
                        totalRevenue = game.totalRevenue + monthlySalesRevenue
                    )
                }
            }
        }
        
        // 🆕 5. 合并新发售的游戏到已发售列表
        val finalGames = updatedGames + newlyReleasedGames
        
        // 更新资金余额：本月利润 = 收入 - 支出
        val monthlyProfit = monthlyIncome - monthlyExpense
        val newCashBalance = (subsidiary.cashBalance + monthlyProfit).coerceAtLeast(0L) // 资金不能为负数
        
        // 🆕 动态更新市值（与玩家公司使用相同逻辑）
        val releasedGamesCount = finalGames.size // 包含新发售的游戏
        val baseMoney = if (newCashBalance < 0) 0L else newCashBalance
        val gamesValue = releasedGamesCount * 100000L
        val employeesValue = subsidiary.estimatedEmployeeCount * 50000L
        val newMarketValue = baseMoney + gamesValue + employeesValue
        
        // 🆕 记录完成的游戏
        if (completedGames.isNotEmpty()) {
            android.util.Log.d("SubsidiaryManager",
                "子公司${subsidiary.name}完成${completedGames.size}款游戏开发：" +
                completedGames.joinToString(", ") { "《${it.name}》(${it.rating}分)" }
            )
        }
        
        return subsidiary.copy(
            monthlyRevenue = monthlyIncome,
            monthlyExpense = monthlyExpense,
            cashBalance = newCashBalance, // 更新资金余额
            marketValue = newMarketValue, // 🆕 更新市值
            totalRevenue = subsidiary.totalRevenue + monthlyIncome,
            games = finalGames, // 🆕 包含新发售的游戏
            developingGames = updatedDevelopingGames // 🆕 更新开发中游戏列表
        )
    }
}
