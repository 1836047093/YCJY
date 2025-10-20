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
    val fans: Int, // 粉丝数
    val games: List<CompetitorGame> = emptyList(), // 游戏列表
    val yearsFounded: Int = 0, // 成立年数
    val reputation: Float = 50f // 声誉 (0-100)
) {
    /**
     * 计算公司的总活跃玩家数（所有网游的活跃玩家之和）
     */
    fun getTotalActivePlayers(): Int {
        return games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
            .sumOf { it.activePlayers }
    }
    
    /**
     * 计算公司的总销量（所有单机游戏的销量之和）
     */
    fun getTotalSales(): Int {
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
    val activePlayers: Int = 0, // 活跃玩家数（网游）
    val salesCount: Int = 0, // 销量（单机）
    val releaseYear: Int = 1, // 发售年份
    val releaseMonth: Int = 1, // 发售月份
    val totalRevenue: Double = 0.0, // 累计总收入
    val monetizationRevenue: Double = 0.0 // 累计付费内容收入（仅网游）
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
    MARKET_VALUE_CHANGE    // 市值变化
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
    
    // 游戏名称前缀
    private val gameNamePrefixes = listOf(
        "传奇", "英雄", "王者", "战争", "征服", "冒险", "幻想", "永恒",
        "命运", "荣耀", "天堂", "地狱", "黑暗", "光明", "神话", "史诗"
    )
    
    // 游戏名称后缀
    private val gameNameSuffixes = listOf(
        "之战", "物语", "世界", "传说", "纪元", "奇迹", "帝国", "联盟",
        "争霸", "对决", "狂潮", "风暴", "觉醒", "重生", "起源", "终结"
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
            
            // 生成公司基础数据（数值相对保守，避免过于夸张）
            val yearsFounded = Random.nextInt(1, 6) // 1-5年
            val baseMarketValue = when (yearsFounded) {
                1 -> Random.nextLong(500000L, 2000000L)      // 50万-200万
                2 -> Random.nextLong(1000000L, 5000000L)     // 100万-500万
                3 -> Random.nextLong(2000000L, 10000000L)    // 200万-1000万
                4 -> Random.nextLong(5000000L, 20000000L)    // 500万-2000万
                else -> Random.nextLong(10000000L, 50000000L) // 1000万-5000万
            }
            
            val baseFans = when (yearsFounded) {
                1 -> Random.nextInt(1000, 10000)       // 1K-10K
                2 -> Random.nextInt(5000, 50000)       // 5K-50K
                3 -> Random.nextInt(20000, 100000)     // 20K-100K
                4 -> Random.nextInt(50000, 300000)     // 50K-300K
                else -> Random.nextInt(100000, 800000) // 100K-800K
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
            // 生成唯一的游戏名称
            var gameName: String
            do {
                val prefix = gameNamePrefixes.random()
                val suffix = gameNameSuffixes.random()
                gameName = "$prefix$suffix"
            } while (usedGameNames.contains(gameName))
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
            
            // 游戏评分 (5.0-9.5)
            val rating = Random.nextInt(50, 96) / 10f
            
            // 根据游戏年龄和评分生成合理的玩家数/销量
            val monthsSinceRelease = (currentYear - actualReleaseYear) * 12 + (currentMonth - actualReleaseMonth)
            
            val (activePlayers, salesCount, initialRevenue, initialMonetizationRevenue) = when (businessModel) {
                BusinessModel.ONLINE_GAME -> {
                    // 网游活跃玩家：基于评分和时间
                    val baseActivePlayers = ((rating - 5) * 10000).toInt() // 评分影响基数
                    val timeMultiplier = when {
                        monthsSinceRelease <= 6 -> Random.nextDouble(0.8, 1.5)   // 新游戏
                        monthsSinceRelease <= 12 -> Random.nextDouble(0.6, 1.2)  // 半年-1年
                        monthsSinceRelease <= 24 -> Random.nextDouble(0.3, 0.8)  // 1-2年
                        else -> Random.nextDouble(0.1, 0.5)                       // 2年以上
                    }
                    val activePlayers = (baseActivePlayers * timeMultiplier).toInt().coerceIn(500, 500000)
                    
                    // 使用付费内容系统计算累计收入
                    val monthlyMonetizationRevenue = calculateCompetitorMonetizationRevenue(activePlayers, theme)
                    val totalMonetizationRevenue = monthlyMonetizationRevenue * monthsSinceRelease.coerceAtLeast(1)
                    
                    // 注册收入为0（免费网游）
                    val totalRevenue = totalMonetizationRevenue
                    
                    Quadruple(activePlayers, 0, totalRevenue, totalMonetizationRevenue)
                }
                BusinessModel.SINGLE_PLAYER -> {
                    // 单机游戏销量：基于评分和时间累计
                    val baseSales = ((rating - 5) * 5000).toInt()
                    val timeSales = monthsSinceRelease * Random.nextInt(100, 500)
                    val totalSales = (baseSales + timeSales).coerceIn(1000, 1000000)
                    
                    // 单机收入 = 销量 × 单价（假设50元）
                    val totalRevenue = totalSales * 50.0
                    
                    Quadruple(0, totalSales, totalRevenue, 0.0)
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
                    monetizationRevenue = initialMonetizationRevenue
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
            val fansGrowth = (company.fans * Random.nextDouble(0.02, 0.10)).toInt()
            val newFans = company.fans + fansGrowth
            
            // 更新游戏数据
            val updatedGames = mutableListOf<CompetitorGame>()
            for (game in company.games) {
                when (game.businessModel) {
                    BusinessModel.ONLINE_GAME -> {
                        // 网游活跃玩家数变化 (±10%-30%)
                        val playerChange = (game.activePlayers * Random.nextDouble(-0.10, 0.30)).toInt()
                        val newActivePlayers = (game.activePlayers + playerChange).coerceAtLeast(100)
                        
                        // 使用付费内容系统计算本月收入
                        val monthlyMonetizationRevenue = calculateCompetitorMonetizationRevenue(newActivePlayers, game.theme)
                        val newMonetizationRevenue = game.monetizationRevenue + monthlyMonetizationRevenue
                        val newTotalRevenue = newMonetizationRevenue // 网游总收入=付费内容收入
                        
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
                            monetizationRevenue = newMonetizationRevenue
                        ))
                    }
                    BusinessModel.SINGLE_PLAYER -> {
                        // 单机游戏持续销售 (+100-1000)
                        val salesGrowth = Random.nextInt(100, 1000)
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
                            totalRevenue = newTotalRevenue
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
    private fun shouldGenerateMilestoneNews(oldPlayers: Int, newPlayers: Int): Boolean {
        val milestones = listOf(10000, 50000, 100000, 200000, 300000, 500000, 1000000)
        return milestones.any { milestone ->
            oldPlayers < milestone && newPlayers >= milestone
        }
    }
    
    /**
     * 检查是否应生成销量里程碑新闻
     */
    private fun shouldGenerateSalesMilestoneNews(oldSales: Int, newSales: Int): Boolean {
        val milestones = listOf(10000, 50000, 100000, 200000, 500000, 1000000)
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
        playerCount: Int,
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
        salesCount: Int,
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
        val prefix = gameNamePrefixes.random()
        val suffix = gameNameSuffixes.random()
        val gameName = "$prefix$suffix"
        
        val theme = GameTheme.entries.random()
        val platformCount = Random.nextInt(1, 4)
        val platforms = Platform.entries.shuffled().take(platformCount)
        val businessModel = BusinessModel.entries.random()
        val rating = Random.nextInt(60, 90) / 10f
        
        val (activePlayers, salesCount, initialRevenue, initialMonetizationRevenue) = when (businessModel) {
            BusinessModel.ONLINE_GAME -> {
                val players = Random.nextInt(1000, 20000)
                // 使用付费内容系统计算首月收入
                val monetizationRevenue = calculateCompetitorMonetizationRevenue(players, theme)
                Quadruple(players, 0, monetizationRevenue, monetizationRevenue)
            }
            BusinessModel.SINGLE_PLAYER -> {
                val sales = Random.nextInt(2000, 10000)
                // 首月收入 = 销量 × 单价(50元)
                val revenue = sales * 50.0
                Quadruple(0, sales, revenue, 0.0)
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
            monetizationRevenue = initialMonetizationRevenue
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
     */
    private fun calculateCompetitorMonetizationRevenue(activePlayers: Int, theme: GameTheme): Double {
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
}
