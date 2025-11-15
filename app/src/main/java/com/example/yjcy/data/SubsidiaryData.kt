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
 * 子公司应聘者状态
 */
enum class SubsidiaryApplicantStatus {
    PENDING,   // 待处理
    HIRED,     // 已雇佣
    REJECTED   // 已拒绝
}

/**
 * 子公司应聘者
 */
data class SubsidiaryApplicant(
    val id: String,                                    // 应聘者ID
    val name: String,                                  // 姓名
    val age: Int,                                      // 年龄
    val position: String,                              // 应聘职位
    val skills: Map<String, Int>,                     // 技能等级 (技能名 -> 等级)
    val expectedSalary: Int,                           // 期望薪资
    val applyDate: GameDate,                           // 应聘日期
    val status: SubsidiaryApplicantStatus = SubsidiaryApplicantStatus.PENDING // 状态
)

/**
 * 子公司招聘岗位
 */
data class SubsidiaryJobPosting(
    val id: String,                                    // 岗位ID
    val position: String,                              // 职位
    val requiredSkillLevel: Int,                       // 要求技能等级
    val salary: Int,                                   // 薪资
    val postDate: GameDate,                            // 发布日期
    val applicants: List<SubsidiaryApplicant> = emptyList(), // 应聘者列表
    val isActive: Boolean = true                       // 是否激活
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
    
    // 员工数据
    val employees: List<Employee> = emptyList(), // 真实员工列表
    val estimatedEmployeeCount: Int,          // 估算员工数（基于游戏数量，仅用于向后兼容）
    val monthlyWageCost: Long,                // 月度工资成本
    
    // 招聘数据
    val jobPostings: List<SubsidiaryJobPosting> = emptyList(), // 招聘岗位列表
    
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
        val gameEmployees = company.games.size * 5 // 每款游戏5人
        return gameEmployees.coerceAtLeast(10) // 最少10人
    }
    
    /**
     * 生成子公司员工列表（基于游戏数量和质量）
     */
    private fun generateSubsidiaryEmployees(
        company: CompetitorCompany,
        subsidiaryId: Int
    ): List<Employee> {
        val employees = mutableListOf<Employee>()
        val totalEmployees = estimateEmployeeCount(company)
        
        // 根据游戏数量和评分，生成不同职位的员工
        val avgRating = if (company.games.isNotEmpty()) {
            company.games.map { it.rating }.average().toFloat()
        } else 6.0f
        
        // 根据平均评分决定技能等级分布
        val skillLevel = when {
            avgRating >= 8.5f -> 4 // 高评分公司，员工技能4级
            avgRating >= 7.5f -> 3 // 中高评分公司，员工技能3级
            avgRating >= 6.5f -> 2 // 中等评分公司，员工技能2级
            else -> 1 // 低评分公司，员工技能1级
        }
        
        // 每个职位大约占20%
        val positionsCount = totalEmployees / 5
        
        val positions = listOf("程序员", "策划师", "美工", "音乐家", "客服")
        var employeeId = subsidiaryId * 1000 // 使用子公司ID作为员工ID前缀
        
        positions.forEach { position ->
            val count = if (position == "程序员") {
                // 程序员稍多一些
                positionsCount + (totalEmployees % 5)
            } else {
                positionsCount
            }
            
            repeat(count) {
                val name = generateEmployeeName()
                val salary = skillLevel * 10000 + kotlin.random.Random.nextInt(-2000, 2000)
                
                employees.add(
                    Employee(
                        id = employeeId++,
                        name = name,
                        position = position,
                        skillDevelopment = if (position == "程序员") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillDesign = if (position == "策划师") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillArt = if (position == "美工") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillMusic = if (position == "音乐家") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillService = if (position == "客服") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        salary = salary,
                        experience = skillLevel * 365,
                        motivation = kotlin.random.Random.nextInt(70, 100),
                        loyalty = kotlin.random.Random.nextInt(70, 100),
                        isFounder = false,
                        hireYear = 1,
                        hireMonth = 1,
                        hireDay = 1
                    )
                )
            }
        }
        
        return employees
    }
    
    /**
     * 生成员工名字
     */
    private fun generateEmployeeName(): String {
        val surnames = listOf("王", "李", "张", "刘", "陈", "杨", "黄", "赵", "吴", "周",
                              "徐", "孙", "马", "朱", "胡", "郭", "何", "高", "林", "罗")
        val givenNames = listOf("伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
                                "洋", "勇", "艳", "杰", "涛", "明", "超", "秀兰", "霞", "平")
        return "${surnames.random()}${givenNames.random()}"
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
        
        // 生成真实员工列表
        val employees = generateSubsidiaryEmployees(company, company.id)
        val actualWageCost = employees.sumOf { it.salary.toLong() }
        
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
            employees = employees, // 使用真实员工列表
            estimatedEmployeeCount = employees.size,
            monthlyWageCost = actualWageCost, // 使用真实工资总额
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
     * 推进游戏开发进度（使用真实员工技能，像玩家一样）
     * @return 更新后的开发中游戏列表
     */
    private fun updateDevelopingGames(
        developingGames: List<DevelopingGame>,
        employees: List<Employee>
    ): List<DevelopingGame> {
        return developingGames.map { game ->
            // 获取当前阶段所需的员工
            val requiredEmployees = when (game.currentPhase) {
                DevelopmentPhase.DESIGN -> employees.filter { it.position == "策划师" }
                DevelopmentPhase.ART_SOUND -> employees.filter { it.position == "美工" || it.position == "音乐家" }
                DevelopmentPhase.PROGRAMMING -> employees.filter { it.position == "程序员" }
            }
            
            if (requiredEmployees.isEmpty()) {
                // 没有合适的员工，进度不增加
                return@map game
            }
            
            // 基础进度：每月2%
            val baseProgress = 2f
            
            // 计算技能倍率（根据员工专属技能）
            val avgSkillLevel = when (game.currentPhase) {
                DevelopmentPhase.DESIGN -> requiredEmployees.map { it.skillDesign }.average()
                DevelopmentPhase.ART_SOUND -> requiredEmployees.map { 
                    maxOf(it.skillArt, it.skillMusic)
                }.average()
                DevelopmentPhase.PROGRAMMING -> requiredEmployees.map { it.skillDevelopment }.average()
            }
            
            val skillMultiplier = when {
                avgSkillLevel >= 4.5 -> 1.6f // 平均4-5级：1.6倍
                avgSkillLevel >= 3.5 -> 1.3f // 平均4级：1.3倍
                avgSkillLevel >= 2.5 -> 1.0f // 平均3级：1.0倍
                avgSkillLevel >= 1.5 -> 0.8f // 平均2级：0.8倍
                else -> 0.5f                  // 平均1级：0.5倍
            }
            
            // 人数倍率
            val countMultiplier = when (requiredEmployees.size) {
                1 -> 1.0f
                2 -> 1.3f
                3 -> 1.5f
                else -> 1.6f
            }
            
            // 总进度增加
            val progressIncrease = baseProgress * skillMultiplier * countMultiplier
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
     * 修复旧存档的子公司（为没有员工列表的子公司生成员工）
     */
    fun fixLegacySubsidiary(subsidiary: Subsidiary): Subsidiary {
        // 如果已经有员工列表，不需要修复
        if (subsidiary.employees.isNotEmpty()) {
            return subsidiary
        }
        
        // 生成员工列表
        val avgRating = if (subsidiary.games.isNotEmpty()) {
            subsidiary.games.map { it.rating }.average().toFloat()
        } else 6.0f
        
        val skillLevel = when {
            avgRating >= 8.5f -> 4
            avgRating >= 7.5f -> 3
            avgRating >= 6.5f -> 2
            else -> 1
        }
        
        val totalEmployees = subsidiary.estimatedEmployeeCount
        val positionsCount = totalEmployees / 5
        val positions = listOf("程序员", "策划师", "美工", "音乐家", "客服")
        var employeeId = subsidiary.id * 1000
        
        val employees = mutableListOf<Employee>()
        positions.forEach { position ->
            val count = if (position == "程序员") {
                positionsCount + (totalEmployees % 5)
            } else {
                positionsCount
            }
            
            repeat(count) {
                val name = generateEmployeeName()
                val salary = skillLevel * 10000 + kotlin.random.Random.nextInt(-2000, 2000)
                
                employees.add(
                    Employee(
                        id = employeeId++,
                        name = name,
                        position = position,
                        skillDevelopment = if (position == "程序员") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillDesign = if (position == "策划师") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillArt = if (position == "美工") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillMusic = if (position == "音乐家") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        skillService = if (position == "客服") skillLevel else skillLevel - 1.coerceAtLeast(1),
                        salary = salary,
                        experience = skillLevel * 365,
                        motivation = kotlin.random.Random.nextInt(70, 100),
                        loyalty = kotlin.random.Random.nextInt(70, 100),
                        isFounder = false,
                        hireYear = 1,
                        hireMonth = 1,
                        hireDay = 1
                    )
                )
            }
        }
        
        val actualWageCost = employees.sumOf { it.salary.toLong() }
        
        android.util.Log.d("SubsidiaryManager", 
            "修复旧存档子公司${subsidiary.name}：生成${employees.size}名员工，技能等级${skillLevel}"
        )
        
        return subsidiary.copy(
            employees = employees,
            monthlyWageCost = actualWageCost
        )
    }
    
    /**
     * 更新子公司月度数据
     */
    fun updateMonthlyData(subsidiary: Subsidiary, currentDate: GameDate): Subsidiary {
        if (subsidiary.status != SubsidiaryStatus.ACTIVE) {
            return subsidiary
        }
        
        // 🆕 1. 推进开发中游戏的进度（使用真实员工）
        var updatedDevelopingGames = updateDevelopingGames(
            subsidiary.developingGames,
            subsidiary.employees
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
            developingGames = updatedDevelopingGames, // 🆕 更新开发中游戏列表
            employees = subsidiary.employees // 确保employees不丢失
        )
    }
    
    /**
     * 为子公司招聘新员工
     */
    fun hireEmployee(
        subsidiary: Subsidiary,
        position: String,
        skillLevel: Int,
        salary: Int,
        currentDate: GameDate
    ): Employee {
        // 生成员工ID（使用子公司ID * 1000 + 当前员工数）
        val employeeId = subsidiary.id * 1000 + subsidiary.employees.size + 1
        
        // 生成员工名字
        val name = generateEmployeeName()
        
        // 根据职位设置技能
        return Employee(
            id = employeeId,
            name = name,
            position = position,
            skillDevelopment = if (position == "程序员") skillLevel else (skillLevel - 1).coerceAtLeast(1),
            skillDesign = if (position == "策划师") skillLevel else (skillLevel - 1).coerceAtLeast(1),
            skillArt = if (position == "美工") skillLevel else (skillLevel - 1).coerceAtLeast(1),
            skillMusic = if (position == "音乐家") skillLevel else (skillLevel - 1).coerceAtLeast(1),
            skillService = if (position == "客服") skillLevel else (skillLevel - 1).coerceAtLeast(1),
            salary = salary,
            experience = skillLevel * 365,
            motivation = kotlin.random.Random.nextInt(70, 100),
            loyalty = kotlin.random.Random.nextInt(70, 100),
            isFounder = false,
            hireYear = currentDate.year,
            hireMonth = currentDate.month,
            hireDay = currentDate.day
        )
    }
    
    /**
     * 发布招聘岗位
     */
    fun postJob(
        subsidiary: Subsidiary,
        position: String,
        requiredSkillLevel: Int,
        salary: Int,
        currentDate: GameDate
    ): Subsidiary {
        val jobId = "job_${subsidiary.id}_${System.currentTimeMillis()}"
        val newPosting = SubsidiaryJobPosting(
            id = jobId,
            position = position,
            requiredSkillLevel = requiredSkillLevel,
            salary = salary,
            postDate = currentDate,
            applicants = emptyList(),
            isActive = true
        )
        
        return subsidiary.copy(
            jobPostings = subsidiary.jobPostings + newPosting
        )
    }
    
    /**
     * 生成应聘者（每日调用）
     */
    fun generateApplicants(
        subsidiary: Subsidiary,
        currentDate: GameDate
    ): Subsidiary {
        val updatedPostings = subsidiary.jobPostings.map { posting ->
            if (!posting.isActive) return@map posting
            
            // 根据薪资计算每日应聘者数量
            val minSalary = posting.requiredSkillLevel * 10000
            val salaryRatio = posting.salary.toFloat() / minSalary
            
            val dailyApplicantCount = when {
                salaryRatio >= 1.5f -> kotlin.random.Random.nextInt(1, 3) // 高薪：1-2人/天
                salaryRatio >= 1.25f -> kotlin.random.Random.nextInt(0, 2) // 较高薪：0-1人/天
                salaryRatio >= 1.15f -> kotlin.random.Random.nextInt(0, 2) // 一般薪：0-1人/天
                else -> if (kotlin.random.Random.nextFloat() < 0.3f) 1 else 0 // 低薪：30%概率1人
            }
            
            if (dailyApplicantCount == 0) return@map posting
            
            // 生成应聘者
            val newApplicants = (0 until dailyApplicantCount).map {
                generateApplicant(posting, currentDate)
            }
            
            posting.copy(
                applicants = posting.applicants + newApplicants
            )
        }
        
        return subsidiary.copy(
            jobPostings = updatedPostings
        )
    }
    
    /**
     * 生成一个应聘者
     */
    private fun generateApplicant(
        posting: SubsidiaryJobPosting,
        currentDate: GameDate
    ): SubsidiaryApplicant {
        val name = generateEmployeeName()
        val age = kotlin.random.Random.nextInt(22, 45)
        
        // 技能等级（主技能在要求等级±1范围内）
        val mainSkillLevel = (posting.requiredSkillLevel - 1).coerceAtLeast(1) + kotlin.random.Random.nextInt(0, 3).coerceAtMost(5)
        val otherSkillLevel = kotlin.random.Random.nextInt(1, 4)
        
        val skills = when (posting.position) {
            "程序员" -> mapOf(
                "开发" to mainSkillLevel,
                "设计" to otherSkillLevel,
                "美工" to otherSkillLevel
            )
            "策划师" -> mapOf(
                "设计" to mainSkillLevel,
                "开发" to otherSkillLevel,
                "服务" to otherSkillLevel
            )
            "美工" -> mapOf(
                "美工" to mainSkillLevel,
                "设计" to otherSkillLevel,
                "音乐" to otherSkillLevel
            )
            "音乐家" -> mapOf(
                "音乐" to mainSkillLevel,
                "美工" to otherSkillLevel,
                "设计" to otherSkillLevel
            )
            "客服" -> mapOf(
                "服务" to mainSkillLevel,
                "设计" to otherSkillLevel,
                "开发" to otherSkillLevel
            )
            else -> emptyMap()
        }
        
        // 期望薪资在岗位薪资的90%-110%之间
        val expectedSalary = (posting.salary * (0.9 + kotlin.random.Random.nextDouble() * 0.2)).toInt()
        
        return SubsidiaryApplicant(
            id = "applicant_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt(1000, 9999)}",
            name = name,
            age = age,
            position = posting.position,
            skills = skills,
            expectedSalary = expectedSalary,
            applyDate = currentDate,
            status = SubsidiaryApplicantStatus.PENDING
        )
    }
    
    /**
     * 雇佣应聘者
     */
    fun hireApplicant(
        subsidiary: Subsidiary,
        jobPostingId: String,
        applicantId: String,
        currentDate: GameDate
    ): Subsidiary {
        val posting = subsidiary.jobPostings.find { it.id == jobPostingId } ?: return subsidiary
        val applicant = posting.applicants.find { it.id == applicantId } ?: return subsidiary
        
        if (applicant.status != SubsidiaryApplicantStatus.PENDING) {
            return subsidiary
        }
        
        // 创建员工
        val employeeId = subsidiary.id * 1000 + subsidiary.employees.size + 1
        val newEmployee = Employee(
            id = employeeId,
            name = applicant.name,
            position = applicant.position,
            skillDevelopment = applicant.skills["开发"] ?: 1,
            skillDesign = applicant.skills["设计"] ?: 1,
            skillArt = applicant.skills["美工"] ?: 1,
            skillMusic = applicant.skills["音乐"] ?: 1,
            skillService = applicant.skills["服务"] ?: 1,
            salary = posting.salary,
            experience = applicant.skills.values.maxOrNull()?.times(365) ?: 365,
            motivation = kotlin.random.Random.nextInt(70, 100),
            loyalty = kotlin.random.Random.nextInt(70, 100),
            isFounder = false,
            hireYear = currentDate.year,
            hireMonth = currentDate.month,
            hireDay = currentDate.day
        )
        
        // 更新应聘者状态
        val updatedPostings = subsidiary.jobPostings.map { p ->
            if (p.id == jobPostingId) {
                p.copy(
                    applicants = p.applicants.map { a ->
                        if (a.id == applicantId) {
                            a.copy(status = SubsidiaryApplicantStatus.HIRED)
                        } else a
                    }
                )
            } else p
        }
        
        return subsidiary.copy(
            employees = subsidiary.employees + newEmployee,
            monthlyWageCost = subsidiary.monthlyWageCost + posting.salary,
            jobPostings = updatedPostings
        )
    }
    
    /**
     * 关闭招聘岗位
     */
    fun closeJobPosting(
        subsidiary: Subsidiary,
        jobPostingId: String
    ): Subsidiary {
        val updatedPostings = subsidiary.jobPostings.map { posting ->
            if (posting.id == jobPostingId) {
                posting.copy(isActive = false)
            } else posting
        }
        
        return subsidiary.copy(
            jobPostings = updatedPostings
        )
    }
    
    /**
     * 删除招聘岗位
     */
    fun deleteJobPosting(
        subsidiary: Subsidiary,
        jobPostingId: String
    ): Subsidiary {
        return subsidiary.copy(
            jobPostings = subsidiary.jobPostings.filter { it.id != jobPostingId }
        )
    }
}
