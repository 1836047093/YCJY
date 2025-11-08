package com.example.yjcy.data

/**
 * 子公司运营状态
 */
enum class SubsidiaryStatus {
    ACTIVE,      // 运营中
    SUSPENDED,   // 暂停营业
    LIQUIDATED   // 已清算
}

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
    val monthlyRevenue: Long = 0L,            // 月度收入
    val monthlyExpense: Long = 0L,            // 月度支出
    val totalRevenue: Long = 0L,              // 累计总收入（收购后）
    
    // 游戏数据
    val games: List<CompetitorGame>,          // 继承的游戏（含开发中和已发售）
    
    // 员工数据（根据游戏反推）
    val estimatedEmployeeCount: Int,          // 估算员工数（基于游戏数量）
    val monthlyWageCost: Long,                // 月度工资成本
    
    // 管理设置
    val profitSharingRate: Float = 0.5f,      // 利润分成比例（总公司抽成）
    val autoManagement: Boolean = true,       // 自动管理（默认开启）
    val status: SubsidiaryStatus = SubsidiaryStatus.ACTIVE
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
        return Subsidiary(
            id = company.id,
            name = company.name,
            logo = company.logo,
            acquisitionPrice = acquisitionPrice,
            acquisitionDate = acquisitionDate,
            marketValue = company.marketValue,
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
                    // 网游：基于活跃玩家和付费内容收入
                    // 付费率0.5%，ARPU 100元/月
                    val monthlyRevenue = (game.activePlayers * 0.005 * 100).toLong()
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
     * 计算服务器成本（网游）
     */
    fun calculateServerCost(games: List<CompetitorGame>): Long {
        val onlineGames = games.filter { it.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME }
        var serverCost = 0L
        
        onlineGames.forEach { game ->
            // 基于活跃玩家估算服务器数量
            val requiredServers = (game.activePlayers / 10000).toInt().coerceAtLeast(1)
            // S型服务器月租5000元/台
            serverCost += requiredServers * 5000L
        }
        
        return serverCost
    }
    
    /**
     * 计算其他成本（运营成本）
     */
    fun calculateOtherCosts(subsidiary: Subsidiary): Long {
        // 基础运营成本：每款游戏1万元/月
        return subsidiary.games.size * 10000L
    }
    
    /**
     * 更新子公司月度数据
     */
    fun updateMonthlyData(subsidiary: Subsidiary): Subsidiary {
        if (subsidiary.status != SubsidiaryStatus.ACTIVE) {
            return subsidiary
        }
        
        // 计算本月收入
        val monthlyIncome = calculateMonthlyIncome(subsidiary)
        
        // 计算本月支出
        val monthlyExpense = subsidiary.monthlyWageCost + 
                            calculateServerCost(subsidiary.games) +
                            calculateOtherCosts(subsidiary)
        
        // 更新游戏数据（网游玩家数衰减2%，单机销量增长）
        val updatedGames = subsidiary.games.map { game ->
            when (game.businessModel) {
                com.example.yjcy.ui.BusinessModel.ONLINE_GAME -> {
                    // 网游活跃玩家缓慢衰减
                    game.copy(activePlayers = (game.activePlayers * 0.98).toLong())
                }
                com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER -> {
                    // 单机游戏持续小量销售
                    val newSales = (game.salesCount * 0.01).toLong().coerceAtLeast(10L)
                    game.copy(salesCount = game.salesCount + newSales)
                }
            }
        }
        
        return subsidiary.copy(
            monthlyRevenue = monthlyIncome,
            monthlyExpense = monthlyExpense,
            totalRevenue = subsidiary.totalRevenue + monthlyIncome,
            games = updatedGames
        )
    }
}
