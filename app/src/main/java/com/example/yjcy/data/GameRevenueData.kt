package com.example.yjcy.data

import java.util.*
import java.text.SimpleDateFormat
import kotlin.random.Random

/**
 * 每日销量数据类
 */
data class DailySales(
    val date: Date,
    val sales: Int,
    val revenue: Double
)

/**
 * 销量数据类，用于图表显示
 */
data class SalesData(
    val label: String,
    val value: Int,
    val revenue: Double
)

/**
 * 收益统计数据类
 */
data class RevenueStatistics(
    val totalRevenue: Double,
    val totalSales: Int,
    val averageDailyRevenue: Double,
    val averageDailySales: Int,
    val peakDailySales: Int,
    val peakDailyRevenue: Double,
    val daysOnMarket: Int,
    val revenueGrowthRate: Double
)

/**
 * 游戏收益主数据类
 */
data class GameRevenue(
    val gameId: String,
    val gameName: String,
    val releaseDate: Date,
    val releasePrice: Double,
    val isActive: Boolean = true,
    val dailySalesList: List<DailySales> = emptyList(),
    val statistics: RevenueStatistics? = null
) {
    /**
     * 获取总收益
     */
    fun getTotalRevenue(): Double {
        return dailySalesList.sumOf { it.revenue }
    }
    
    /**
     * 获取总销量
     */
    fun getTotalSales(): Int {
        return dailySalesList.sumOf { it.sales }
    }
    
    /**
     * 获取平均每日收益
     */
    fun getAverageDailyRevenue(): Double {
        return if (dailySalesList.isNotEmpty()) {
            getTotalRevenue() / dailySalesList.size
        } else 0.0
    }
    
    /**
     * 获取平均每日销量
     */
    fun getAverageDailySales(): Int {
        return if (dailySalesList.isNotEmpty()) {
            getTotalSales() / dailySalesList.size
        } else 0
    }
    
    /**
     * 获取在售天数
     */
    fun getDaysOnMarket(): Int {
        return dailySalesList.size
    }
}

/**
 * 收益管理器单例类
 */
object RevenueManager {
    private val gameRevenueMap = mutableMapOf<String, GameRevenue>()
    
    /**
     * 为游戏生成模拟收益数据
     */
    fun generateRevenueData(gameId: String, gameName: String, releasePrice: Double, daysOnMarket: Int = 30): GameRevenue {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysOnMarket)
        val releaseDate = calendar.time
        val dailySalesList = mutableListOf<DailySales>()
        
        // 生成模拟的每日销量数据
        for (i in 0 until daysOnMarket) {
            calendar.time = releaseDate
            calendar.add(Calendar.DAY_OF_YEAR, i)
            val date = calendar.time
            
            // 模拟销量衰减：首日最高，然后逐渐下降，偶有波动
            val baseSales = when {
                i == 0 -> Random.nextInt(800, 1200) // 首日销量
                i <= 7 -> Random.nextInt(300, 600) // 首周
                i <= 14 -> Random.nextInt(150, 350) // 第二周
                else -> Random.nextInt(50, 200) // 后续
            }
            
            // 添加随机波动
            val fluctuation = Random.nextDouble(0.7, 1.3)
            val actualSales = (baseSales * fluctuation).toInt()
            
            val dailyRevenue = actualSales * releasePrice
            
            dailySalesList.add(
                DailySales(
                    date = date,
                    sales = actualSales,
                    revenue = dailyRevenue
                )
            )
        }
        
        val gameRevenue = GameRevenue(
            gameId = gameId,
            gameName = gameName,
            releaseDate = releaseDate,
            releasePrice = releasePrice,
            dailySalesList = dailySalesList
        )
        
        gameRevenueMap[gameId] = gameRevenue
        return gameRevenue
    }
    
    /**
     * 获取游戏收益数据
     */
    fun getGameRevenue(gameId: String): GameRevenue? {
        return gameRevenueMap[gameId]
    }
    
    /**
     * 计算收益统计
     */
    fun calculateStatistics(gameRevenue: GameRevenue): RevenueStatistics {
        val dailySales = gameRevenue.dailySalesList
        
        if (dailySales.isEmpty()) {
            return RevenueStatistics(
                totalRevenue = 0.0,
                totalSales = 0,
                averageDailyRevenue = 0.0,
                averageDailySales = 0,
                peakDailySales = 0,
                peakDailyRevenue = 0.0,
                daysOnMarket = 0,
                revenueGrowthRate = 0.0
            )
        }
        
        val totalRevenue = dailySales.sumOf { it.revenue }
        val totalSales = dailySales.sumOf { it.sales }
        val averageDailyRevenue = totalRevenue / dailySales.size
        val averageDailySales = totalSales / dailySales.size
        val peakDailySales = dailySales.maxOf { it.sales }
        val peakDailyRevenue = dailySales.maxOf { it.revenue }
        val daysOnMarket = dailySales.size
        
        // 计算收益增长率（最近7天vs前7天）
        val revenueGrowthRate = if (dailySales.size >= 14) {
            val recent7Days = dailySales.takeLast(7).sumOf { it.revenue }
            val previous7Days = dailySales.drop(dailySales.size - 14).take(7).sumOf { it.revenue }
            if (previous7Days > 0) {
                ((recent7Days - previous7Days) / previous7Days) * 100
            } else 0.0
        } else 0.0
        
        return RevenueStatistics(
            totalRevenue = totalRevenue,
            totalSales = totalSales,
            averageDailyRevenue = averageDailyRevenue,
            averageDailySales = averageDailySales,
            peakDailySales = peakDailySales,
            peakDailyRevenue = peakDailyRevenue,
            daysOnMarket = daysOnMarket,
            revenueGrowthRate = revenueGrowthRate
        )
    }
    
    /**
     * 下架游戏
     */
    fun removeGameFromMarket(gameId: String): Boolean {
        val gameRevenue = gameRevenueMap[gameId]
        return if (gameRevenue != null) {
            gameRevenueMap[gameId] = gameRevenue.copy(isActive = false)
            true
        } else {
            false
        }
    }
    
    /**
     * 重新上架游戏
     */
    fun relistGame(gameId: String): Boolean {
        val gameRevenue = gameRevenueMap[gameId]
        return if (gameRevenue != null) {
            gameRevenueMap[gameId] = gameRevenue.copy(isActive = true)
            true
        } else {
            false
        }
    }
    
    /**
     * 获取所有游戏收益数据
     */
    fun getAllGameRevenues(): List<GameRevenue> {
        return gameRevenueMap.values.toList()
    }
    
    /**
     * 获取活跃游戏收益数据
     */
    fun getActiveGameRevenues(): List<GameRevenue> {
        return gameRevenueMap.values.filter { it.isActive }
    }
    
    /**
     * 获取已下架游戏收益数据
     */
    fun getInactiveGameRevenues(): List<GameRevenue> {
        return gameRevenueMap.values.filter { !it.isActive }
    }
    
    /**
     * 获取用于图表显示的销量数据
     */
    fun getSalesDataForChart(gameRevenue: GameRevenue, days: Int = 7): List<SalesData> {
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        return gameRevenue.dailySalesList.takeLast(days).map { dailySales ->
            SalesData(
                label = dateFormat.format(dailySales.date),
                value = dailySales.sales,
                revenue = dailySales.revenue
            )
        }
    }
    
    /**
     * 根据游戏评分更新收益数据
     */
    fun updateRevenueBasedOnRating(gameId: String, rating: Float) {
        val gameRevenue = gameRevenueMap[gameId] ?: return
        
        // 根据评分调整收益倍数
        val multiplier = when {
            rating >= 9.0f -> 1.5f
            rating >= 7.0f -> 1.2f
            rating >= 5.0f -> 1.0f
            else -> 0.7f
        }
        
        // 更新每日销量数据，应用评分倍数
        val updatedDailySales = gameRevenue.dailySalesList.map { dailySales ->
            val adjustedSales = (dailySales.sales * multiplier).toInt()
            val adjustedRevenue = adjustedSales * gameRevenue.releasePrice
            
            dailySales.copy(
                sales = adjustedSales,
                revenue = adjustedRevenue
            )
        }
        
        // 更新游戏收益数据
        gameRevenueMap[gameId] = gameRevenue.copy(
            dailySalesList = updatedDailySales
        )
    }
    
    /**
     * 为已发售游戏添加新的一天收益数据
     */
    fun addDailyRevenueForGame(gameId: String): Double {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0.0
        
        // 如果游戏已下架，则不产生收益
        if (!gameRevenue.isActive) return 0.0
        
        // 获取最新的销量数据作为参考
        val latestSales = gameRevenue.dailySalesList.lastOrNull()
        
        // 如果是第一天（没有历史数据），初始化首日销量
        if (latestSales == null) {
            val calendar = Calendar.getInstance()
            val firstDayDate = gameRevenue.releaseDate
            
            // 首日销量：根据价格调整
            val baseSales = when {
                gameRevenue.releasePrice <= 30.0 -> Random.nextInt(800, 1200) // 低价游戏
                gameRevenue.releasePrice <= 100.0 -> Random.nextInt(500, 800) // 中价游戏
                else -> Random.nextInt(200, 500) // 高价游戏
            }
            val firstDayRevenue = baseSales * gameRevenue.releasePrice
            
            val firstDaySales = DailySales(
                date = firstDayDate,
                sales = baseSales,
                revenue = firstDayRevenue
            )
            
            // 更新游戏收益数据
            gameRevenueMap[gameId] = gameRevenue.copy(
                dailySalesList = listOf(firstDaySales)
            )
            
            return firstDayRevenue
        }
        
        // 生成新的一天的销量数据（基于前一天的数据，但略有波动）
        val calendar = Calendar.getInstance()
        calendar.time = latestSales.date
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val newDate = calendar.time
        
        // 模拟销量衰减：随着时间推移，销量逐渐下降，但有随机波动
        val decayFactor = 0.98 // 每天衰减2%
        val fluctuation = Random.nextDouble(0.8, 1.2) // 随机波动
        val newSales = (latestSales.sales * decayFactor * fluctuation).toInt().coerceAtLeast(1)
        val newRevenue = newSales * gameRevenue.releasePrice
        
        // 创建新的每日销量数据
        val newDailySales = DailySales(
            date = newDate,
            sales = newSales,
            revenue = newRevenue
        )
        
        // 更新游戏收益数据
        val updatedDailySalesList = gameRevenue.dailySalesList + newDailySales
        gameRevenueMap[gameId] = gameRevenue.copy(
            dailySalesList = updatedDailySalesList
        )
        
        return newRevenue
    }
    
    /**
     * 清除所有收益数据（用于测试）
     */
    fun clearAllData() {
        gameRevenueMap.clear()
    }
}