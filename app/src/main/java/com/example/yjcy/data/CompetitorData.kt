package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.Platform
import com.example.yjcy.ui.BusinessModel
import kotlin.random.Random

/**
 * 辅助数据类：四元组
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * 竞争对手公司数据类
 */
data class CompetitorCompany(
    val id: Int,
    val name: String,
    val logo: String,
    val marketValue: Long, // 市值
    val fans: Long, // 粉丝数
    val games: List<CompetitorGame> = emptyList(), // 游戏列表
    val yearsFounded: Int = 0, // 成立年数
    val reputation: Float = 50f // 声誉 (0-100)
) {
    /**
     * 计算公司的总活跃玩家数（所有网游的活跃玩家之和）
     */
    fun getTotalActivePlayers(): Long {
        return games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
            .sumOf { it.activePlayers }
    }
    
    /**
     * 计算公司的总销量（所有单机游戏的销量之和）
     */
    fun getTotalSales(): Long {
        return games.filter { it.businessModel == BusinessModel.SINGLE_PLAYER }
            .sumOf { it.salesCount }
    }
    
    /**
     * 计算公司所有游戏的总收入（累计）
     */
    fun getTotalRevenue(): Double {
        return games.sumOf { it.totalRevenue }
    }
    
    /**
     * 计算公司所有游戏的平均评分
     */
    fun getAverageRating(): Float {
        if (games.isEmpty()) return 5.0f
        return games.map { it.rating }.average().toFloat()
    }
}

/**
 * 竞争对手游戏数据类
 */
data class CompetitorGame(
    val id: String,
    val name: String,
    val companyId: Int,
    val companyName: String,
    val theme: GameTheme,
    val platforms: List<Platform>,
    val businessModel: BusinessModel,
    val rating: Float, // 游戏评分 (0-10)
    val activePlayers: Long = 0, // 活跃玩家数（网游）
    val salesCount: Long = 0, // 销量（单机）
    val releaseYear: Int = 1, // 发售年份
    val releaseMonth: Int = 1, // 发售月份
    val totalRevenue: Double = 0.0, // 累计总收入
    val monetizationRevenue: Double = 0.0, // 累计付费内容收入（仅网游）
    val currentTournament: EsportsTournament? = null, // 当前进行中的赛事
    val lastTournamentDate: GameDate? = null, // 上次举办赛事的日期
    val tournamentHistory: List<EsportsTournament>? = emptyList(), // 赛事历史记录
    val allDevelopmentEmployees: List<Employee> = emptyList(), // 所有参与开发的员工（兼容性字段，竞争对手不使用）
    // 🆕 网游兴趣值系统（与玩家系统对齐）
    val totalRegisteredPlayers: Long = 0, // 总注册人数（仅网游）
    val playerInterest: Double = 100.0, // 玩家兴趣值 0-100（仅网游）
    val lifecycleProgress: Double = 0.0, // 生命周期进度 0-100%（仅网游）
    val daysSinceLaunch: Int = 0, // 上线天数（仅网游）
    val lastInterestDecayDay: Int = 0 // 上次兴趣值衰减的天数（仅网游）
)

/**
 * 动态新闻数据类
 */
data class CompetitorNews(
    val id: String,
    val title: String,
    val content: String,
    val type: NewsType,
    val companyId: Int,
    val companyName: String,
    val gameId: String? = null,
    val gameName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val year: Int,
    val month: Int,
    val day: Int
)

/**
 * 新闻类型枚举
 */
enum class NewsType {
    NEW_GAME_RELEASE,      // 新游戏发售
    PLAYER_MILESTONE,      // 玩家数里程碑
    SALES_MILESTONE,       // 销量里程碑
    RATING_ACHIEVEMENT,    // 评分成就
    COMPANY_MILESTONE,     // 公司里程碑
    MARKET_VALUE_CHANGE,   // 市值变化
    GAME_UPDATE            // 🆕 游戏更新
}

/**
 * 收购状态枚举
 */
enum class AcquisitionStatus {
    ELIGIBLE,              // 符合收购条件
    INSUFFICIENT_MARKET_VALUE, // 市值不足
    INSUFFICIENT_FUNDS,    // 资金不足
    CANNOT_ACQUIRE_SELF    // 无法收购自己
}

/**
 * 竞价出价记录
 */
data class AcquisitionBid(
    val bidderId: Int,           // 出价者ID（-1表示玩家）
    val bidderName: String,      // 出价者名称
    val amount: Long,            // 出价金额
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 收购结果密封类
 */
sealed class AcquisitionResult {
    data class Success(
        val acquiredCompany: CompetitorCompany,
        val finalPrice: Long,
        val inheritedIPs: List<GameIP>,  // 改为继承IP而不是游戏
        val marketValueGain: Long,
        val fansGain: Long
    ) : AcquisitionResult()
    
    data class Failed(
        val reason: String,
        val winnerName: String,
        val finalPrice: Long
    ) : AcquisitionResult()
    
    data class Cancelled(val reason: String) : AcquisitionResult()
}

/**
 * 竞争对手管理器
 * 负责生成和管理竞争对手公司及其游戏
 */
object CompetitorManager {
    // 公司名称库（9个竞争对手）
    private val companyNames = listOf(
        "光源互娱", "棱镜游戏", "梦境互娱", "星尘枢纽", "微光火花",
        "泰坦互娱", "乱线工坊", "璀璨游戏", "神盾动力"
    )
    
    // 游戏名称前缀（避免真实游戏名称）
    private val gameNamePrefixes = listOf(
        "虚空", "星辰", "银河", "时空", "龙翼", "凤羽", "雷霆", "烈焰",
        "冰霜", "暗影", "圣光", "魔法", "机甲", "异界", "混沌", "苍穹",
        "深渊", "幻影", "极地", "炽天", "末日", "创世", "轮回", "涅槃"
    )
    
    // 游戏名称后缀（避免真实游戏名称）
    private val gameNameSuffixes = listOf(
        "纪元", "编年史", "传说", "秘境", "冒险", "挑战", "试炼", "远征",
        "征途", "战纪", "乱斗", "狂想", "交响曲", "协奏曲", "狂潮", "浩劫",
        "崛起", "黎明", "余晖", "探索", "维度", "界限", "碎片", "遗迹"
    )
    
    // 真实游戏名称黑名单（防止生成真实游戏名称）
    private val gameNameBlacklist = setOf(
        "英雄联盟", "王者荣耀", "和平精英", "原神", "崩坏三", "明日方舟",
        "阴阳师", "梦幻西游", "大话西游", "倩女幽魂", "天涯明月刀", "剑网三",
        "逆水寒", "永劫无间", "黑神话", "战神", "刺客信条", "使命召唤",
        "绝地求生", "堡垒之夜", "英雄无敌", "魔兽世界", "炉石传说", "守望先锋"
    )
    
    /**
     * 生成初始的竞争对手公司列表
     */
    fun generateInitialCompetitors(playerCompanyName: String, currentYear: Int, currentMonth: Int): List<CompetitorCompany> {
        val competitors = mutableListOf<CompetitorCompany>()
        val usedNames = mutableSetOf(playerCompanyName)
        
        for (i in 1..9) {
            // 确保公司名称不重复
            var companyName: String
            do {
                companyName = companyNames.random()
            } while (usedNames.contains(companyName))
            usedNames.add(companyName)
            
            // 生成公司基础数据（大幅提高初始市值，确保所有竞争对手都有足够高的市值）
            val yearsFounded = Random.nextInt(1, 6) // 1-5年
            val baseMarketValue = when (yearsFounded) {
                1 -> Random.nextLong(5000000L, 15000000L)      // 500万-1500万（大幅提高）
                2 -> Random.nextLong(10000000L, 30000000L)     // 1000万-3000万（大幅提高）
                3 -> Random.nextLong(20000000L, 60000000L)    // 2000万-6000万（大幅提高）
                4 -> Random.nextLong(40000000L, 100000000L)    // 4000万-1亿（大幅提高）
                else -> Random.nextLong(80000000L, 200000000L) // 8000万-2亿（大幅提高）
            }
            
            val baseFans = when (yearsFounded) {
                1 -> Random.nextLong(1000L, 10000L)       // 1K-10K
                2 -> Random.nextLong(5000L, 50000L)       // 5K-50K
                3 -> Random.nextLong(20000L, 100000L)     // 20K-100K
                4 -> Random.nextLong(50000L, 300000L)     // 50K-300K
                else -> Random.nextLong(100000L, 800000L) // 100K-800K
            }
            
            val reputation = Random.nextInt(40, 85).toFloat()
            
            // 生成公司的游戏（1-3个）
            val gameCount = Random.nextInt(1, 4)
            val games = generateCompanyGames(i, companyName, gameCount, yearsFounded, currentYear, currentMonth)
            
            competitors.add(
                CompetitorCompany(
                    id = i,
                    name = companyName,
                    logo = getCompanyLogo(companyName),
                    marketValue = baseMarketValue,
                    fans = baseFans,
                    games = games,
                    yearsFounded = yearsFounded,
                    reputation = reputation
                )
            )
        }
        
        return competitors
    }
    
    /**
     * 生成公司的游戏列表
     */
    private fun generateCompanyGames(
        companyId: Int,
        companyName: String,
        gameCount: Int,
        yearsFounded: Int,
        currentYear: Int,
        currentMonth: Int
    ): List<CompetitorGame> {
        val games = mutableListOf<CompetitorGame>()
        val usedGameNames = mutableSetOf<String>()
        
        for (j in 1..gameCount) {
            // 生成唯一的游戏名称（避免真实游戏名称）
            var gameName: String
            do {
                val prefix = gameNamePrefixes.random()
                val suffix = gameNameSuffixes.random()
                gameName = "$prefix$suffix"
            } while (usedGameNames.contains(gameName) || gameNameBlacklist.contains(gameName))
            usedGameNames.add(gameName)
            
            val theme = GameTheme.entries.random()
            val platformCount = Random.nextInt(1, 4)
            val platforms = Platform.entries.shuffled().take(platformCount)
            val businessModel = BusinessModel.entries.random()
            
            // 游戏发售时间（在公司成立期间的随机时间）
            val gameAgeInMonths = Random.nextInt(1, yearsFounded * 12 + 1)
            val releaseYear = currentYear - (gameAgeInMonths / 12)
            val releaseMonth = currentMonth - (gameAgeInMonths % 12)
            
            val actualReleaseYear = if (releaseMonth <= 0) releaseYear - 1 else releaseYear
            val actualReleaseMonth = if (releaseMonth <= 0) releaseMonth + 12 else releaseMonth
            
            // 游戏评分 (6.5-10.0，大幅提高竞争对手实力，增加10分游戏比例)
            // 使用加权随机，让高评分游戏更容易出现，特别是10分游戏
            val rating = when (Random.nextInt(1, 101)) {
                in 1..8 -> 10.0f                                // 8%概率：10.0分（满分神作）
                in 9..20 -> Random.nextInt(95, 100) / 10f      // 12%概率：9.5-9.9分（接近满分）
                in 21..35 -> Random.nextInt(90, 95) / 10f      // 15%概率：9.0-9.5分（高评分）
                in 36..55 -> Random.nextInt(85, 90) / 10f     // 20%概率：8.5-9.0分（中高评分）
                in 56..75 -> Random.nextInt(75, 85) / 10f     // 20%概率：7.5-8.5分（中等偏上）
                in 76..90 -> Random.nextInt(70, 75) / 10f     // 15%概率：7.0-7.5分（中等评分）
                else -> Random.nextInt(65, 70) / 10f          // 10%概率：6.5-7.0分（中低评分）
            }
            
            // 根据游戏年龄和评分生成合理的玩家数/销量
            val monthsSinceRelease = (currentYear - actualReleaseYear) * 12 + (currentMonth - actualReleaseMonth)
            
            // 🆕 网游兴趣值系统初始化（用于历史游戏）
            var initialDaysSinceLaunch = 0
            var initialLifecycleProgress = 0.0
            var initialPlayerInterest = 100.0
            var initialTotalRegistered = 0L
            var initialLastDecayDay = 0
            
            val (activePlayers, salesCount, initialRevenue, initialMonetizationRevenue) = when (businessModel) {
                BusinessModel.ONLINE_GAME -> {
                    // 🆕 计算上线天数（简化：每月30天）
                    initialDaysSinceLaunch = monthsSinceRelease * 30
                    
                    // 🆕 计算生命周期进度
                    val totalLifecycleDays = 365
                    initialLifecycleProgress = ((initialDaysSinceLaunch.toDouble() / totalLifecycleDays) * 100.0).coerceIn(0.0, 100.0)
                    
                    // 🆕 模拟历史兴趣值衰减（每90天衰减一次）
                    initialPlayerInterest = 100.0
                    val decayCount = initialDaysSinceLaunch / 90
                    for (i in 0 until decayCount) {
                        val dayAtInterval = (i + 1) * 90
                        val progressAtInterval = ((dayAtInterval.toDouble() / totalLifecycleDays) * 100.0).coerceIn(0.0, 100.0)
                        val decayRate = when {
                            progressAtInterval < 30.0 -> 8.0   // 成长期：衰减8%
                            progressAtInterval < 70.0 -> 15.0  // 成熟期：衰减15%
                            progressAtInterval < 90.0 -> 25.0  // 衰退期：衰减25%
                            else -> 35.0                       // 末期：衰减35%
                        }
                        initialPlayerInterest = (initialPlayerInterest - decayRate).coerceIn(0.0, 100.0)
                    }
                    initialLastDecayDay = decayCount * 90
                    
                    // 网游活跃玩家：大幅提高基础活跃玩家数，增强竞争力（给玩家更大压力）
                    val baseActivePlayers = when {
                        rating >= 9.0f -> Random.nextLong(200000L, 500000L)  // 9.0+分：20万-50万（大幅提高）
                        rating >= 8.5f -> Random.nextLong(120000L, 300000L)   // 8.5-9.0分：12万-30万（大幅提高）
                        rating >= 8.0f -> Random.nextLong(80000L, 200000L)   // 8.0-8.5分：8万-20万（大幅提高）
                        rating >= 7.0f -> Random.nextLong(40000L, 100000L)   // 7.0-8.0分：4万-10万（提高）
                        rating >= 6.5f -> Random.nextLong(20000L, 50000L)     // 6.5-7.0分：2万-5万（提高）
                        else -> Random.nextLong(10000L, 25000L)              // 6.5分以下：1万-2.5万（提高）
                    }
                    
                    // 时间倍率（发售越久，倍率越高）
                    val timeMultiplier = when {
                        monthsSinceRelease <= 6 -> Random.nextDouble(0.8, 1.2)   // 新游戏：80%-120%
                        monthsSinceRelease <= 12 -> Random.nextDouble(1.0, 1.5)  // 半年-1年：100%-150%
                        monthsSinceRelease <= 24 -> Random.nextDouble(1.5, 2.5)  // 1-2年：150%-250%
                        else -> Random.nextDouble(2.0, 3.5)                      // 2年以上：200%-350%
                    }
                    val activePlayers = ((baseActivePlayers * timeMultiplier).toLong()).coerceIn(500L, 2000000L)
                    
                    // 🆕 根据活跃玩家数和兴趣值反推总注册数
                    // 活跃玩家 = 总注册数 × 40% × 兴趣倍率
                    val interestMultiplier = when {
                        initialPlayerInterest >= 70.0 -> 1.0
                        initialPlayerInterest >= 50.0 -> 0.7
                        initialPlayerInterest >= 30.0 -> 0.4
                        else -> 0.2
                    }
                    initialTotalRegistered = if (interestMultiplier > 0) {
                        (activePlayers / (0.4 * interestMultiplier)).toLong()
                    } else {
                        (activePlayers * 5).toLong() // 兜底
                    }
                    
                    // 使用付费内容系统计算累计收入
                    val monthlyMonetizationRevenue = calculateCompetitorMonetizationRevenue(activePlayers, theme)
                    val totalMonetizationRevenue = monthlyMonetizationRevenue * monthsSinceRelease.coerceAtLeast(1)
                    
                    // 注册收入为0（免费网游）
                    val totalRevenue = totalMonetizationRevenue
                    
                    Quadruple(activePlayers, 0L, totalRevenue, totalMonetizationRevenue)
                }
                BusinessModel.SINGLE_PLAYER -> {
                    // 单机游戏销量：大幅提高基础销量，增强竞争压力
                    val ratingBase = when {
                        rating >= 9.0f -> Random.nextLong(200000L, 500000L)  // 9.0+分：20万-50万（大幅增强）
                        rating >= 8.5f -> Random.nextLong(120000L, 300000L)  // 8.5-9.0分：12万-30万（大幅增强）
                        rating >= 8.0f -> Random.nextLong(80000L, 200000L)   // 8.0-8.5分：8万-20万（大幅增强）
                        rating >= 7.5f -> Random.nextLong(50000L, 120000L)   // 7.5-8.0分：5万-12万（增强）
                        rating >= 7.0f -> Random.nextLong(30000L, 80000L)    // 7.0-7.5分：3万-8万（增强）
                        rating >= 6.5f -> Random.nextLong(15000L, 40000L)    // 6.5-7.0分：1.5万-4万
                        else -> Random.nextLong(8000L, 20000L)               // 6.5分以下：8千-2万
                    }
                    
                    // 时间累积销量（发售越久销量越高，增速更快）
                    val timeMultiplier = when {
                        monthsSinceRelease <= 12 -> Random.nextDouble(1.0, 2.0)   // 1年内：100%-200%
                        monthsSinceRelease <= 24 -> Random.nextDouble(2.0, 4.0)   // 1-2年：200%-400%
                        monthsSinceRelease <= 36 -> Random.nextDouble(4.0, 7.0)   // 2-3年：400%-700%
                        monthsSinceRelease <= 48 -> Random.nextDouble(6.0, 10.0)  // 3-4年：600%-1000%
                        else -> Random.nextDouble(8.0, 12.0)                      // 4年以上：800%-1200%
                    }
                    
                    // 计算总销量：基础销量 × 时间倍率，大幅提高上限
                    val totalSales = ((ratingBase * timeMultiplier).toLong()).coerceIn(1000L, 6000000L)
                    
                    // 单机收入 = 销量 × 单价（假设50元）
                    val totalRevenue = totalSales * 50.0
                    
                    Quadruple(0L, totalSales, totalRevenue, 0.0)
                }
            }
            
            games.add(
                CompetitorGame(
                    id = "comp_${companyId}_game_$j",
                    name = gameName,
                    companyId = companyId,
                    companyName = companyName,
                    theme = theme,
                    platforms = platforms,
                    businessModel = businessModel,
                    rating = rating,
                    activePlayers = activePlayers,
                    salesCount = salesCount,
                    releaseYear = actualReleaseYear,
                    releaseMonth = actualReleaseMonth,
                    totalRevenue = initialRevenue,
                    monetizationRevenue = initialMonetizationRevenue,
                    // 🆕 兴趣值系统初始化（仅网游有效）
                    totalRegisteredPlayers = initialTotalRegistered,
                    playerInterest = initialPlayerInterest,
                    lifecycleProgress = initialLifecycleProgress,
                    daysSinceLaunch = initialDaysSinceLaunch,
                    lastInterestDecayDay = initialLastDecayDay
                )
            )
        }
        
        return games
    }
    
    /**
     * 更新竞争对手数据（每月调用）
     * 模拟竞争对手的发展，并生成新闻事件
     */
    fun updateCompetitors(
        competitors: List<CompetitorCompany>,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Pair<List<CompetitorCompany>, List<CompetitorNews>> {
        val updatedCompetitors = mutableListOf<CompetitorCompany>()
        val newsList = mutableListOf<CompetitorNews>()
        
        for (company in competitors) {
            // 更新市值：基于总收入、平均评分和成立年数动态计算
            // 市值 = 总收入 × (1 + 评分系数 + 年数系数 + 游戏数量系数)
            val totalRevenue = company.getTotalRevenue()
            val avgRating = company.getAverageRating()
            
            // 评分系数：评分越高，市盈率越高 (0-1之间)
            val ratingMultiplier = ((avgRating - 5.0f) / 5.0f).coerceIn(0f, 1f).toDouble()
            
            // 年数系数：成立时间越长，品牌价值越高 (0-0.5之间)
            val yearsMultiplier = (company.yearsFounded.toDouble() / 10.0).coerceIn(0.0, 0.5)
            
            // 游戏数量系数：游戏越多，公司价值越高 (0-0.3之间)
            val gameCountMultiplier = (company.games.size.toDouble() / 10.0).coerceIn(0.0, 0.3)
            
            // 计算市值倍数 (1.0-2.8之间)
            val marketValueMultiplier = 1.0 + ratingMultiplier + yearsMultiplier + gameCountMultiplier
            
            // 新市值 = 总收入 × 市值倍数，最低10万
            val newMarketValue = (totalRevenue * marketValueMultiplier).toLong().coerceAtLeast(100000L)
            
            // 更新粉丝数 (+2%-10%)
            val fansGrowth = (company.fans * Random.nextDouble(0.02, 0.10)).toLong()
            val newFans = company.fans + fansGrowth
            
            // 更新游戏数据
            val updatedGames = mutableListOf<CompetitorGame>()
            for (game in company.games) {
                when (game.businessModel) {
                    BusinessModel.ONLINE_GAME -> {
                        // 🆕 使用与玩家相同的兴趣值系统
                        
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
                            // 根据生命周期阶段确定衰减率
                            val decayRate = when {
                                newLifecycleProgress < 30.0 -> 8.0   // 成长期：衰减8%
                                newLifecycleProgress < 70.0 -> 15.0  // 成熟期：衰减15%
                                newLifecycleProgress < 90.0 -> 25.0  // 衰退期：衰减25%
                                else -> 35.0                         // 末期：衰减35%
                            }
                            newPlayerInterest = (game.playerInterest - decayRate).coerceIn(0.0, 100.0)
                            newLastDecayDay = newDaysSinceLaunch
                        }
                        
                        // 4. 计算注册数增长（每日）× 30天
                        var newTotalRegistered = game.totalRegisteredPlayers
                        for (day in 1..30) {
                            // 基础增长率（根据评分）
                            val baseGrowthRate = when {
                                game.rating >= 8.5f -> 0.05   // 8.5分以上：5%增长
                                game.rating >= 8.0f -> 0.04   // 8.0-8.5分：4%增长
                                game.rating >= 7.0f -> 0.03   // 7.0-8.0分：3%增长
                                else -> 0.02                  // 7.0分以下：2%增长
                            }
                            
                            // 兴趣值影响
                            val interestMultiplier = when {
                                newPlayerInterest >= 80.0 -> 1.15
                                newPlayerInterest >= 70.0 -> 1.0
                                newPlayerInterest >= 50.0 -> 0.85
                                newPlayerInterest >= 30.0 -> 0.7
                                else -> 0.5
                            }
                            
                            // 计算当日新增注册
                            val dailyRegistrations = (newTotalRegistered * baseGrowthRate * interestMultiplier * 0.01).toLong().coerceAtLeast(10L)
                            newTotalRegistered += dailyRegistrations
                        }
                        
                        // 5. 计算活跃玩家数（总注册数 × 40% × 兴趣倍率）
                        val interestMultiplier = when {
                            newPlayerInterest >= 70.0 -> 1.0
                            newPlayerInterest >= 50.0 -> 0.7
                            newPlayerInterest >= 30.0 -> 0.4
                            else -> 0.2
                        }
                        val newActivePlayers = (newTotalRegistered * 0.4 * interestMultiplier).toLong().coerceAtLeast(100L)
                        
                        // 6. 计算本月收入
                        val monthlyMonetizationRevenue = calculateCompetitorMonetizationRevenue(newActivePlayers, game.theme)
                        val newMonetizationRevenue = game.monetizationRevenue + monthlyMonetizationRevenue
                        val newTotalRevenue = newMonetizationRevenue
                        
                        // 🆕 7. 检查是否需要更新游戏来恢复兴趣值
                        var finalPlayerInterest = newPlayerInterest
                        var updatedGame = false
                        
                        // 当兴趣值低于50%时，有概率更新游戏
                        if (newPlayerInterest < 50.0 && newLifecycleProgress < 90.0) {
                            // 更新概率：兴趣值越低，概率越高
                            val updateProbability = when {
                                newPlayerInterest < 30.0 -> 0.30  // 30%概率
                                newPlayerInterest < 40.0 -> 0.20  // 20%概率
                                else -> 0.10                      // 10%概率
                            }
                            
                            if (Random.nextDouble() < updateProbability) {
                                // 恢复兴趣值（与玩家系统相同）
                                val recoveryAmount = when {
                                    newLifecycleProgress < 30.0 -> 25.0  // 成长期：恢复25%
                                    newLifecycleProgress < 70.0 -> 15.0  // 成熟期：恢复15%
                                    else -> 8.0                          // 衰退期：恢复8%
                                }
                                finalPlayerInterest = (newPlayerInterest + recoveryAmount).coerceIn(0.0, 100.0)
                                updatedGame = true
                                
                                // 生成更新游戏新闻
                                newsList.add(
                                    CompetitorNews(
                                        id = "news_update_${System.currentTimeMillis()}_${Random.nextInt()}",
                                        title = "${company.name}更新《${game.name}》，玩家好评！",
                                        content = "${company.name}为旗下网游《${game.name}》推出重大更新，" +
                                                "新增内容受到玩家好评，兴趣值提升${recoveryAmount.toInt()}%！",
                                        type = NewsType.GAME_UPDATE,
                                        companyId = company.id,
                                        companyName = company.name,
                                        gameId = game.id,
                                        gameName = game.name,
                                        year = currentYear,
                                        month = currentMonth,
                                        day = currentDay
                                    )
                                )
                            }
                        }
                        
                        // 检查是否达到里程碑
                        if (shouldGenerateMilestoneNews(game.activePlayers, newActivePlayers)) {
                            newsList.add(
                                generatePlayerMilestoneNews(
                                    company, game, newActivePlayers,
                                    currentYear, currentMonth, currentDay
                                )
                            )
                        }
                        
                        updatedGames.add(game.copy(
                            activePlayers = newActivePlayers,
                            totalRevenue = newTotalRevenue,
                            monetizationRevenue = newMonetizationRevenue,
                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList(),
                            // 🆕 更新兴趣值系统字段
                            totalRegisteredPlayers = newTotalRegistered,
                            playerInterest = finalPlayerInterest,  // 使用可能已恢复的兴趣值
                            lifecycleProgress = newLifecycleProgress,
                            daysSinceLaunch = newDaysSinceLaunch,
                            lastInterestDecayDay = newLastDecayDay
                        ))
                    }
                    BusinessModel.SINGLE_PLAYER -> {
                        // 单机游戏持续销售：根据评分和当前销量动态计算增长（增强版）
                        // 基础增长：评分越高，增长越快
                        val baseGrowthRate = when {
                            game.rating >= 9.0f -> Random.nextDouble(1.2, 2.5)   // 9.0+分：1.2%-2.5%（大幅增强）
                            game.rating >= 8.5f -> Random.nextDouble(0.9, 1.8)   // 8.5-9.0分：0.9%-1.8%（大幅增强）
                            game.rating >= 8.0f -> Random.nextDouble(0.6, 1.2)   // 8.0-8.5分：0.6%-1.2%（增强）
                            game.rating >= 7.5f -> Random.nextDouble(0.4, 0.9)   // 7.5-8.0分：0.4%-0.9%（增强）
                            game.rating >= 7.0f -> Random.nextDouble(0.3, 0.6)   // 7.0-7.5分：0.3%-0.6%
                            else -> Random.nextDouble(0.15, 0.4)                 // 7.0分以下：0.15%-0.4%
                        }
                        
                        // 计算增长：当前销量 × 增长比例
                        val proportionalGrowth = (game.salesCount * baseGrowthRate / 100.0).toLong()
                        
                        // 保底增长（防止销量太低的游戏增长过慢）- 大幅提高
                        val minGrowth = when {
                            game.rating >= 9.0f -> Random.nextInt(5000, 12000)   // 高评分：5000-12000/月（大幅增强）
                            game.rating >= 8.5f -> Random.nextInt(3000, 8000)    // 中高评分：3000-8000/月（大幅增强）
                            game.rating >= 8.0f -> Random.nextInt(2000, 5000)    // 中等评分：2000-5000/月（增强）
                            game.rating >= 7.0f -> Random.nextInt(1000, 3000)    // 中低评分：1000-3000/月（增强）
                            else -> Random.nextInt(300, 1200)                    // 低评分：300-1200/月
                        }.toLong()
                        
                        // 取两者中的较大值，但不超过当前销量的8%（提高上限，允许更快增长）
                        val maxGrowth = (game.salesCount * 0.08).toLong()
                        val salesGrowth = maxOf(proportionalGrowth, minGrowth).coerceAtMost(maxGrowth)
                        
                        val newSalesCount = game.salesCount + salesGrowth
                        
                        // 累加单机收入：新销量 × 单价(50元)
                        val additionalRevenue = salesGrowth * 50.0
                        val newTotalRevenue = game.totalRevenue + additionalRevenue
                        
                        // 检查是否达到销量里程碑
                        if (shouldGenerateSalesMilestoneNews(game.salesCount, newSalesCount)) {
                            newsList.add(
                                generateSalesMilestoneNews(
                                    company, game, newSalesCount,
                                    currentYear, currentMonth, currentDay
                                )
                            )
                        }
                        
                        updatedGames.add(game.copy(
                            salesCount = newSalesCount,
                            totalRevenue = newTotalRevenue,
                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                        ))
                    }
                }
            }
            
            // 小概率发布新游戏 (5%)
            if (Random.nextDouble() < 0.05) {
                val newGame = generateNewGame(company, currentYear, currentMonth)
                updatedGames.add(newGame)
                newsList.add(
                    generateNewGameNews(
                        company, newGame,
                        currentYear, currentMonth, currentDay
                    )
                )
            }
            
            // AI举办电竞赛事并更新游戏进度
            val (gamesAfterTournament, tournamentNews) = updateCompetitorTournaments(
                updatedGames,
                company,
                currentYear, 
                currentMonth, 
                currentDay
            )
            updatedGames.clear()
            updatedGames.addAll(gamesAfterTournament)
            if (tournamentNews != null) {
                newsList.add(tournamentNews)
            }
            
            updatedCompetitors.add(
                company.copy(
                    marketValue = newMarketValue,
                    fans = newFans,
                    games = updatedGames,
                    yearsFounded = company.yearsFounded // 年份在年度更新时增加
                )
            )
        }
        
        return Pair(updatedCompetitors, newsList)
    }
    
    /**
     * 检查是否应生成玩家数里程碑新闻
     */
    private fun shouldGenerateMilestoneNews(oldPlayers: Long, newPlayers: Long): Boolean {
        val milestones = listOf(10000L, 50000L, 100000L, 200000L, 300000L, 500000L, 1000000L)
        return milestones.any { milestone ->
            oldPlayers < milestone && newPlayers >= milestone
        }
    }
    
    /**
     * 检查是否应生成销量里程碑新闻
     */
    private fun shouldGenerateSalesMilestoneNews(oldSales: Long, newSales: Long): Boolean {
        val milestones = listOf(10000L, 50000L, 100000L, 200000L, 500000L, 1000000L)
        return milestones.any { milestone ->
            oldSales < milestone && newSales >= milestone
        }
    }
    
    /**
     * 生成玩家数里程碑新闻
     */
    private fun generatePlayerMilestoneNews(
        company: CompetitorCompany,
        game: CompetitorGame,
        playerCount: Long,
        year: Int,
        month: Int,
        day: Int
    ): CompetitorNews {
        val milestone = when {
            playerCount >= 1000000 -> "100万"
            playerCount >= 500000 -> "50万"
            playerCount >= 300000 -> "30万"
            playerCount >= 200000 -> "20万"
            playerCount >= 100000 -> "10万"
            playerCount >= 50000 -> "5万"
            else -> "1万"
        }
        
        return CompetitorNews(
            id = "news_${System.currentTimeMillis()}_${Random.nextInt()}",
            title = "${company.name}的《${game.name}》突破${milestone}活跃玩家！",
            content = "${company.name}旗下网游《${game.name}》活跃玩家数突破${milestone}大关，成为近期市场上的一匹黑马。",
            type = NewsType.PLAYER_MILESTONE,
            companyId = company.id,
            companyName = company.name,
            gameId = game.id,
            gameName = game.name,
            year = year,
            month = month,
            day = day
        )
    }
    
    /**
     * 生成销量里程碑新闻
     */
    private fun generateSalesMilestoneNews(
        company: CompetitorCompany,
        game: CompetitorGame,
        salesCount: Long,
        year: Int,
        month: Int,
        day: Int
    ): CompetitorNews {
        val milestone = when {
            salesCount >= 1000000 -> "100万"
            salesCount >= 500000 -> "50万"
            salesCount >= 200000 -> "20万"
            salesCount >= 100000 -> "10万"
            salesCount >= 50000 -> "5万"
            else -> "1万"
        }
        
        return CompetitorNews(
            id = "news_${System.currentTimeMillis()}_${Random.nextInt()}",
            title = "${company.name}的《${game.name}》销量突破${milestone}！",
            content = "${company.name}推出的单机大作《${game.name}》销量突破${milestone}份，获得玩家一致好评。",
            type = NewsType.SALES_MILESTONE,
            companyId = company.id,
            companyName = company.name,
            gameId = game.id,
            gameName = game.name,
            year = year,
            month = month,
            day = day
        )
    }
    
    /**
     * 生成新游戏
     */
    private fun generateNewGame(company: CompetitorCompany, year: Int, month: Int): CompetitorGame {
        // 生成游戏名称（避免真实游戏名称和已有游戏重名）
        var gameName: String
        val usedNames = company.games.map { it.name }.toSet()
        do {
            val prefix = gameNamePrefixes.random()
            val suffix = gameNameSuffixes.random()
            gameName = "$prefix$suffix"
        } while (usedNames.contains(gameName) || gameNameBlacklist.contains(gameName))
        
        val theme = GameTheme.entries.random()
        val platformCount = Random.nextInt(1, 4)
        val platforms = Platform.entries.shuffled().take(platformCount)
        val businessModel = BusinessModel.entries.random()
        // 新游戏评分：提高至7.0-10.0分，大幅增加10分和接近满分游戏比例
        val rating = when (Random.nextInt(1, 101)) {
            in 1..10 -> 10.0f                                // 10%概率：10.0分（满分神作）
            in 11..25 -> Random.nextInt(95, 100) / 10f       // 15%概率：9.5-9.9分（接近满分）
            in 26..45 -> Random.nextInt(90, 95) / 10f       // 20%概率：9.0-9.5分（高评分）
            in 46..65 -> Random.nextInt(85, 90) / 10f      // 20%概率：8.5-9.0分（中高评分）
            in 66..80 -> Random.nextInt(80, 85) / 10f      // 15%概率：8.0-8.5分（中等偏上）
            in 81..92 -> Random.nextInt(75, 80) / 10f     // 12%概率：7.5-8.0分（中等评分）
            else -> Random.nextInt(70, 75) / 10f           // 8%概率：7.0-7.5分（中低评分）
        }
        
        // 🆕 网游兴趣值系统初始化
        var initialTotalRegistered = 0L
        var initialPlayerInterest = 100.0
        var initialLifecycleProgress = 0.0
        var initialDaysSinceLaunch = 0
        
        val (activePlayers, salesCount, initialRevenue, initialMonetizationRevenue) = when (businessModel) {
            BusinessModel.ONLINE_GAME -> {
                // 新发售游戏的初始活跃玩家数：大幅提高首发活跃玩家数（增强竞争力）
                val players = when {
                    rating >= 9.0f -> Random.nextInt(80000, 150000).toLong()    // 9.0+分：8万-15万首发（大幅提高）
                    rating >= 8.5f -> Random.nextInt(50000, 100000).toLong()     // 8.5-9.0分：5万-10万首发（大幅提高）
                    rating >= 8.0f -> Random.nextInt(30000, 70000).toLong()     // 8.0-8.5分：3万-7万首发（大幅提高）
                    rating >= 7.0f -> Random.nextInt(20000, 50000).toLong()      // 7.0-8.0分：2万-5万首发（提高）
                    else -> Random.nextInt(10000, 30000).toLong()                 // 7.0分以下：1万-3万首发（提高）
                }
                
                // 🆕 根据活跃玩家数反推总注册数
                // 活跃玩家 = 总注册数 × 40% × 兴趣倍率
                // 初始兴趣值100%，兴趣倍率1.0
                initialTotalRegistered = (players / 0.4).toLong()
                initialPlayerInterest = 100.0
                initialLifecycleProgress = 0.0
                initialDaysSinceLaunch = 0
                
                // 使用付费内容系统计算首月收入
                val monetizationRevenue = calculateCompetitorMonetizationRevenue(players, theme)
                Quadruple(players, 0L, monetizationRevenue, monetizationRevenue)
            }
            BusinessModel.SINGLE_PLAYER -> {
                // 新发售游戏的初始销量：大幅提高首发销量，增强竞争压力
                val sales = when {
                    rating >= 9.0f -> Random.nextInt(150000, 350000).toLong()   // 9.0+分：15万-35万首发（超强）
                    rating >= 8.5f -> Random.nextInt(80000, 200000).toLong()    // 8.5-9.0分：8万-20万首发（超强）
                    rating >= 8.0f -> Random.nextInt(50000, 120000).toLong()    // 8.0-8.5分：5万-12万首发（大幅增强）
                    rating >= 7.5f -> Random.nextInt(30000, 80000).toLong()     // 7.5-8.0分：3万-8万首发（增强）
                    rating >= 7.0f -> Random.nextInt(18000, 50000).toLong()     // 7.0-7.5分：1.8万-5万首发（增强）
                    rating >= 6.5f -> Random.nextInt(10000, 30000).toLong()     // 6.5-7.0分：1万-3万首发
                    else -> Random.nextInt(5000, 18000).toLong()                // 6.5分以下：5千-1.8万首发
                }
                // 首月收入 = 销量 × 单价(50元)
                val revenue = sales * 50.0
                Quadruple(0L, sales, revenue, 0.0)
            }
        }
        
        return CompetitorGame(
            id = "comp_${company.id}_game_${System.currentTimeMillis()}",
            name = gameName,
            companyId = company.id,
            companyName = company.name,
            theme = theme,
            platforms = platforms,
            businessModel = businessModel,
            rating = rating,
            activePlayers = activePlayers,
            salesCount = salesCount,
            releaseYear = year,
            releaseMonth = month,
            totalRevenue = initialRevenue,
            monetizationRevenue = initialMonetizationRevenue,
            // 🆕 兴趣值系统初始化（仅网游有效）
            totalRegisteredPlayers = initialTotalRegistered,
            playerInterest = initialPlayerInterest,
            lifecycleProgress = initialLifecycleProgress,
            daysSinceLaunch = initialDaysSinceLaunch,
            lastInterestDecayDay = 0
        )
    }
    
    /**
     * 生成新游戏发售新闻
     */
    private fun generateNewGameNews(
        company: CompetitorCompany,
        game: CompetitorGame,
        year: Int,
        month: Int,
        day: Int
    ): CompetitorNews {
        val gameType = if (game.businessModel == BusinessModel.ONLINE_GAME) "网游" else "单机游戏"
        return CompetitorNews(
            id = "news_${System.currentTimeMillis()}_${Random.nextInt()}",
            title = "${company.name}发售新作《${game.name}》！",
            content = "${company.name}正式推出全新${gameType}《${game.name}》，主题为${game.theme.displayName}，登陆${game.platforms.joinToString("、") { it.displayName }}平台。",
            type = NewsType.NEW_GAME_RELEASE,
            companyId = company.id,
            companyName = company.name,
            gameId = game.id,
            gameName = game.name,
            year = year,
            month = month,
            day = day
        )
    }
    
    /**
     * 计算竞争对手网游的付费内容月收入
     * 根据游戏主题配置5个付费内容，使用更激进的付费率和价格
     * 
     * 公开此函数供SubsidiaryManager使用
     */
    fun calculateCompetitorMonetizationRevenue(activePlayers: Long, theme: GameTheme): Double {
        var totalRevenue = 0.0
        
        // 根据游戏主题获取推荐的5个付费内容类型
        val recommendedItems = MonetizationConfig.getRecommendedItems(theme)
        
        // 为每个付费内容类型设置付费率和价格（更激进的数值）
        for (itemType in recommendedItems) {
            // 根据付费内容类型设置不同的付费率（提高到原来的10-20倍）
            val purchaseRate = when (itemType.displayName) {
                "皮肤与外观", "角色皮肤", "英雄皮肤", "武器皮肤", "赛车皮肤", "球队皮肤", "皮肤套装" -> 0.005  // 0.5%
                "成长加速道具", "训练加速券", "科技加速券", "时间加速道具" -> 0.008  // 0.8%
                "稀有装备", "特殊武器包", "战术背包" -> 0.003  // 0.3%
                "赛季通行证", "战斗通行证" -> 0.015  // 1.5%
                "强力角色", "新英雄", "新人物", "新角色" -> 0.004  // 0.4%
                "VIP会员" -> 0.01  // 1.0%
                "抽卡系统", "球员卡包" -> 0.02  // 2.0%（最激进）
                "扩展包", "DLC内容", "限定剧情章节" -> 0.006  // 0.6%
                "资源包", "道具组合包", "资源包" -> 0.012  // 1.2%
                "高级兵种包", "高级单位", "高级载具" -> 0.005  // 0.5%
                else -> 0.005  // 默认0.5%
            }
            
            // 根据付费内容类型设置价格范围（更高的价格）
            val prices = when (itemType.displayName) {
                "皮肤与外观", "角色皮肤", "英雄皮肤", "武器皮肤", "赛车皮肤", "球队皮肤" -> listOf(30f, 68f, 98f, 198f)
                "成长加速道具", "训练加速券", "科技加速券" -> listOf(18f, 30f, 68f)
                "稀有装备", "特殊武器包" -> listOf(68f, 98f, 198f)
                "赛季通行证", "战斗通行证" -> listOf(68f, 98f, 128f, 198f)
                "强力角色", "新英雄", "新人物" -> listOf(98f, 198f, 328f)
                "VIP会员" -> listOf(30f, 68f, 98f, 198f)
                "抽卡系统", "球员卡包" -> listOf(6f, 30f, 68f, 328f, 648f)  // 抽卡有低价和高价档位
                "扩展包", "DLC内容", "限定剧情章节" -> listOf(68f, 98f, 128f)
                "资源包", "道具组合包" -> listOf(30f, 68f, 98f)
                "高级兵种包", "高级单位", "高级载具" -> listOf(98f, 198f, 328f)
                else -> listOf(30f, 68f, 98f)  // 默认价格
            }
            
            // 随机选择一个价格档位（偏向高价）
            val price = if (Random.nextDouble() < 0.3) {
                prices.last()  // 30%概率选择最高价
            } else {
                prices.random()  // 70%概率随机选择
            }
            
            // 计算购买人数（带随机波动）
            val baseBuyers = (activePlayers * purchaseRate).toInt()
            val fluctuation = Random.nextDouble(0.8, 1.2)  // 减少波动范围，更稳定
            val actualBuyers = (baseBuyers * fluctuation).toInt().coerceAtLeast(1)
            
            // 计算收益
            val revenue = actualBuyers * price
            totalRevenue += revenue
        }
        
        return totalRevenue
    }
    
    /**
     * 获取公司专属Logo
     * 每个竞争对手都有独特的创意图标
     */
    private fun getCompanyLogo(companyName: String): String {
        return when (companyName) {
            "光源互娱" -> "💡"  // 灯泡，象征光源与创意
            "棱镜游戏" -> "🔷"  // 菱形，象征棱镜的多面折射与视觉效果
            "梦境互娱" -> "🌙"  // 月亮，象征梦境与幻想
            "星尘枢纽" -> "⭐"  // 星星，象征星尘与宇宙枢纽
            "微光火花" -> "✨"  // 闪光，象征微光与灵感火花
            "泰坦互娱" -> "⚡"  // 闪电，象征泰坦的强大力量
            "乱线工坊" -> "🧵"  // 线，象征工坊的精细编织与创作
            "璀璨游戏" -> "💎"  // 钻石，象征璀璨夺目的品质
            "神盾动力" -> "🛡️" // 盾牌，象征神盾的防护与力量
            else -> "🎮"        // 默认游戏图标
        }
    }
    
    /**
     * 检查玩家是否有资格收购目标公司
     */
    fun checkAcquisitionEligibility(
        playerMarketValue: Long,
        playerMoney: Long,
        targetCompany: CompetitorCompany,
        isTargetPlayer: Boolean = false
    ): AcquisitionStatus {
        // 无法收购自己
        if (isTargetPlayer) {
            return AcquisitionStatus.CANNOT_ACQUIRE_SELF
        }
        
        // 资金要求：玩家资金 ≥ 目标市值 × 1.2倍（最低收购价）
        val minimumBidPrice = (targetCompany.marketValue * 1.2).toLong()
        if (playerMoney < minimumBidPrice) {
            return AcquisitionStatus.INSUFFICIENT_FUNDS
        }
        
        return AcquisitionStatus.ELIGIBLE
    }
    
    /**
     * 发起收购（返回初始出价和竞价对手列表）
     */
    fun initiateAcquisition(
        targetCompany: CompetitorCompany,
        allCompetitors: List<CompetitorCompany>,
        playerMarketValue: Long
    ): Pair<Long, List<CompetitorCompany>> {
        // 基础收购价 = 目标市值 × 1.2倍
        val basePrice = (targetCompany.marketValue * 1.2).toLong()
        
        // 30-50%概率触发竞价
        val shouldTriggerBidding = Random.nextDouble() < Random.nextDouble(0.3, 0.5)
        
        if (!shouldTriggerBidding) {
            return Pair(basePrice, emptyList())
        }
        
        // 筛选有资格的竞争对手
        // 条件：市值 > 目标市值 × 1.3倍 且 不是目标公司本身
        val eligibleCompetitors = allCompetitors.filter { competitor ->
            competitor.id != targetCompany.id &&
            competitor.marketValue > (targetCompany.marketValue * 1.3).toLong()
        }
        
        // 随机选择1-2个竞争对手参与竞价
        val biddingCompetitors = if (eligibleCompetitors.isNotEmpty()) {
            val count = Random.nextInt(1, minOf(3, eligibleCompetitors.size + 1))
            eligibleCompetitors.shuffled().take(count)
        } else {
            emptyList()
        }
        
        return Pair(basePrice, biddingCompetitors)
    }
    
    /**
     * 处理AI竞价轮次
     * @return Triple(是否有AI继续出价, 新的出价金额, 出价的AI公司)
     */
    fun processAIBidding(
        currentPrice: Long,
        targetCompany: CompetitorCompany,
        biddingCompetitors: List<CompetitorCompany>
    ): Triple<Boolean, Long, CompetitorCompany?> {
        // 筛选仍有能力出价的AI
        val capableCompetitors = biddingCompetitors.filter { competitor ->
            // AI最高出价 = min(自身市值 × 0.7, 目标市值 × 2.5)
            val maxBid = minOf(
                (competitor.marketValue * 0.7).toLong(),
                (targetCompany.marketValue * 2.5).toLong()
            )
            
            // 当前价格必须低于AI的最高出价
            currentPrice < maxBid
        }
        
        if (capableCompetitors.isEmpty()) {
            return Triple(false, currentPrice, null)
        }
        
        // 选择一个AI出价（市值越高，越容易出价）
        val totalMarketValue = capableCompetitors.sumOf { it.marketValue }
        val randomValue = Random.nextLong(0, totalMarketValue)
        var accumulatedValue = 0L
        var selectedCompetitor: CompetitorCompany? = null
        
        for (competitor in capableCompetitors.sortedByDescending { it.marketValue }) {
            accumulatedValue += competitor.marketValue
            if (randomValue < accumulatedValue) {
                selectedCompetitor = competitor
                break
            }
        }
        
        selectedCompetitor = selectedCompetitor ?: capableCompetitors.first()
        
        // AI决定是否继续出价（60-80%概率）
        val willBid = Random.nextDouble() < Random.nextDouble(0.6, 0.8)
        if (!willBid) {
            return Triple(false, currentPrice, null)
        }
        
        // AI出价：当前价格 × (1 + 5%-15%)
        val increaseRate = Random.nextDouble(0.05, 0.15)
        val newPrice = (currentPrice * (1 + increaseRate)).toLong()
        
        // 确保不超过AI的最高出价
        val maxBid = minOf(
            (selectedCompetitor.marketValue * 0.7).toLong(),
            (targetCompany.marketValue * 2.5).toLong()
        )
        val finalPrice = minOf(newPrice, maxBid)
        
        return Triple(true, finalPrice, selectedCompetitor)
    }
    
    /**
     * 完成收购，返回收购结果（改为返回IP列表而不是游戏列表）
     */
    fun completeAcquisition(
        targetCompany: CompetitorCompany,
        finalPrice: Long,
        acquiredYear: Int,
        acquiredMonth: Int
    ): Triple<Long, Long, List<GameIP>> {
        // 市值增加：目标市值 × 60%
        val marketValueGain = (targetCompany.marketValue * 0.6).toLong()
        
        // 粉丝增加：目标粉丝 × 40%
        val fansGain = (targetCompany.fans * 0.4).toLong()
        
        // 将游戏转换为IP（所有游戏都转换为IP，不是继承游戏）
        val inheritedIPs = targetCompany.games.map { game ->
            GameIP(
                id = "ip_${game.id}",
                name = game.name,
                originalCompany = targetCompany.name,
                theme = game.theme,
                originalRating = game.rating,
                acquiredYear = acquiredYear,
                acquiredMonth = acquiredMonth,
                platforms = game.platforms,
                businessModel = game.businessModel
            )
        }
        
        return Triple(marketValueGain, fansGain, inheritedIPs)
    }
    
    /**
     * 更新竞争对手的赛事进度并尝试举办新赛事
     */
    private fun updateCompetitorTournaments(
        games: List<CompetitorGame>,
        company: CompetitorCompany,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Pair<List<CompetitorGame>, CompetitorNews?> {
        val updatedGames = mutableListOf<CompetitorGame>()
        var tournamentNews: CompetitorNews? = null
        
        for (game in games) {
            var updatedGame = game
            
            // 更新进行中的赛事
            game.currentTournament?.let { tournament ->
                if (tournament.status == TournamentStatus.ONGOING) {
                    val newDay = tournament.currentDay + 1
                    
                    if (newDay >= tournament.type.duration) {
                        // 赛事结束，设置为完成状态
                        val completedTournament = tournament.copy(
                            status = TournamentStatus.COMPLETED,
                            currentDay = tournament.type.duration
                        )
                        
                        // 添加到历史记录
                        val history = game.tournamentHistory?.toMutableList() ?: mutableListOf()
                        history.add(0, completedTournament)
                        if (history.size > 5) {
                            history.removeAt(history.size - 1)
                        }
                        
                        updatedGame = game.copy(
                            currentTournament = null,
                            tournamentHistory = history,
                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                        )
                    } else {
                        // 继续进行
                        updatedGame = game.copy(
                            currentTournament = tournament.copy(currentDay = newDay),
                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                        )
                    }
                }
            }
            
            updatedGames.add(updatedGame)
        }
        
        // 尝试举办新赛事
        val newsAndGame = tryHostTournamentForCompetitor(
            company, 
            updatedGames,
            currentYear, 
            currentMonth, 
            currentDay
        )
        
        if (newsAndGame != null) {
            val (selectedGameId, selectedTournament, news) = newsAndGame
            // 更新对应游戏的赛事信息
            val finalGames = updatedGames.map { game ->
                if (game.id == selectedGameId) {
                    game.copy(
                        currentTournament = selectedTournament,
                        lastTournamentDate = GameDate(currentYear, currentMonth, currentDay),
                        allDevelopmentEmployees = game.allDevelopmentEmployees
                    )
                } else {
                    game
                }
            }
            return Pair(finalGames, news)
        }
        
        return Pair(updatedGames, null)
    }
    
    /**
     * AI竞争对手尝试举办赛事
     * 每月调用，5-10%概率举办
     * @return Triple(游戏ID, 赛事对象, 新闻) 或 null
     */
    private fun tryHostTournamentForCompetitor(
        company: CompetitorCompany,
        games: List<CompetitorGame>,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Triple<String, EsportsTournament, CompetitorNews>? {
        // 筛选可以举办赛事的游戏（竞技类网游，评分≥8.0，活跃玩家≥10万，且没有进行中的赛事）
        val eligibleGames = games.filter { game ->
            // 必须是竞技类游戏
            val isCompetitive = game.theme in listOf(
                GameTheme.MOBA,
                GameTheme.SHOOTER,
                GameTheme.SPORTS,
                GameTheme.RACING,
                GameTheme.STRATEGY
            )
            // 必须是网络游戏
            val isOnline = game.businessModel == BusinessModel.ONLINE_GAME
            // 评分≥8.0
            val goodRating = game.rating >= 8.0f
            // 活跃玩家≥10万
            val enoughPlayers = game.activePlayers >= 100000L
            // 没有进行中的赛事
            val noOngoingTournament = game.currentTournament == null
            
            isCompetitive && isOnline && goodRating && enoughPlayers && noOngoingTournament
        }
        
        if (eligibleGames.isEmpty()) {
            return null
        }
        
        // 基础概率：5%
        var probability = 0.05
        
        // 活跃玩家 > 50万：+3%
        if (eligibleGames.any { it.activePlayers > 500000L }) {
            probability += 0.03
        }
        
        // 平均评分 > 8.0：+2%
        val avgRating = eligibleGames.map { it.rating }.average()
        if (avgRating > 8.0) {
            probability += 0.02
        }
        
        // 公司市值 > 5000万：+2%
        if (company.marketValue > 50000000L) {
            probability += 0.02
        }
        
        // 随机判断是否举办
        if (Random.nextDouble() > probability) {
            return null
        }
        
        // 选择活跃玩家最多的游戏
        val selectedGame = eligibleGames.maxByOrNull { it.activePlayers } ?: return null
        
        // 根据活跃玩家数决定赛事规模
        val tournamentType = when {
            selectedGame.activePlayers >= 500000L -> TournamentType.WORLD_FINALS
            selectedGame.activePlayers >= 200000L -> TournamentType.INTERNATIONAL
            selectedGame.activePlayers >= 50000L -> TournamentType.NATIONAL
            else -> TournamentType.REGIONAL
        }
        
        // 创建赛事对象
        val tournament = EsportsTournament(
            id = "comp_tournament_${System.currentTimeMillis()}_${Random.nextInt()}",
            gameId = selectedGame.id,
            gameName = selectedGame.name,
            type = tournamentType,
            status = TournamentStatus.ONGOING,
            startYear = currentYear,
            startMonth = currentMonth,
            startDay = currentDay,
            currentDay = 1,
            investment = tournamentType.baseCost,
            champion = ""
        )
        
        // 生成赛事新闻
        val news = CompetitorNews(
            id = "news_tournament_${System.currentTimeMillis()}_${Random.nextInt()}",
            title = "${company.name}为《${selectedGame.name}》举办${tournamentType.displayName}！",
            content = "${company.name}宣布将为旗下热门游戏《${selectedGame.name}》举办${tournamentType.displayName}，" +
                    "投入${formatTournamentCost(tournamentType.baseCost)}，预计吸引数十万玩家观看。" +
                    "这是该公司首次举办如此规模的电竞赛事，展现了其在电竞领域的雄心。",
            type = NewsType.COMPANY_MILESTONE,
            companyId = company.id,
            companyName = company.name,
            gameId = selectedGame.id,
            gameName = selectedGame.name,
            year = currentYear,
            month = currentMonth,
            day = currentDay
        )
        
        return Triple(selectedGame.id, tournament, news)
    }
    
    /**
     * 格式化赛事成本
     */
    private fun formatTournamentCost(cost: Long): String {
        return when {
            cost >= 10000000L -> "${cost / 10000000}千万元"
            cost >= 1000000L -> "${cost / 1000000}百万元"
            cost >= 10000L -> "${cost / 10000}万元"
            else -> "${cost}元"
        }
    }
    
    /**
     * 修复旧存档中的竞争对手/子公司游戏数据（向后兼容）
     * 为缺失兴趣值系统字段的网游设置合理的初始值
     */
    fun fixLegacyCompetitorGames(
        companies: List<CompetitorCompany>,
        currentYear: Int,
        currentMonth: Int
    ): List<CompetitorCompany> {
        return companies.map { company ->
            val fixedGames = company.games.map { game ->
                // 只处理网游，且totalRegisteredPlayers为0（表示旧存档）
                if (game.businessModel == BusinessModel.ONLINE_GAME && game.totalRegisteredPlayers == 0L) {
                    // 计算上线天数
                    val monthsSinceRelease = (currentYear - game.releaseYear) * 12 + (currentMonth - game.releaseMonth)
                    val daysSinceLaunch = monthsSinceRelease * 30
                    
                    // 计算生命周期进度
                    val totalLifecycleDays = 365
                    val lifecycleProgress = ((daysSinceLaunch.toDouble() / totalLifecycleDays) * 100.0).coerceIn(0.0, 100.0)
                    
                    // 模拟历史兴趣值衰减
                    var playerInterest = 100.0
                    val decayCount = daysSinceLaunch / 90
                    for (i in 0 until decayCount) {
                        val dayAtInterval = (i + 1) * 90
                        val progressAtInterval = ((dayAtInterval.toDouble() / totalLifecycleDays) * 100.0).coerceIn(0.0, 100.0)
                        val decayRate = when {
                            progressAtInterval < 30.0 -> 8.0
                            progressAtInterval < 70.0 -> 15.0
                            progressAtInterval < 90.0 -> 25.0
                            else -> 35.0
                        }
                        playerInterest = (playerInterest - decayRate).coerceIn(0.0, 100.0)
                    }
                    
                    // 根据当前活跃玩家数和兴趣值反推总注册数
                    val interestMultiplier = when {
                        playerInterest >= 70.0 -> 1.0
                        playerInterest >= 50.0 -> 0.7
                        playerInterest >= 30.0 -> 0.4
                        else -> 0.2
                    }
                    val totalRegistered = if (interestMultiplier > 0) {
                        (game.activePlayers / (0.4 * interestMultiplier)).toLong()
                    } else {
                        (game.activePlayers * 5).toLong()
                    }
                    
                    android.util.Log.d("CompetitorManager", 
                        "修复旧存档游戏 ${game.name}：" +
                        "天数=$daysSinceLaunch, 生命周期=${lifecycleProgress.toInt()}%, " +
                        "兴趣值=${playerInterest.toInt()}%, 注册数=$totalRegistered, 活跃=${game.activePlayers}"
                    )
                    
                    game.copy(
                        totalRegisteredPlayers = totalRegistered,
                        playerInterest = playerInterest,
                        lifecycleProgress = lifecycleProgress,
                        daysSinceLaunch = daysSinceLaunch,
                        lastInterestDecayDay = decayCount * 90
                    )
                } else {
                    game
                }
            }
            
            company.copy(games = fixedGames)
        }
    }
}
