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
 * 游戏更新任务
 */
data class GameUpdateTask(
    val features: List<String>,
    val requiredPoints: Int,
    val progressPoints: Int = 0
)

/**
 * 付费内容收入数据
 */
data class MonetizationRevenue(
    val itemType: String,
    val totalRevenue: Double
)

/**
 * 游戏收益主数据类
 */
data class GameRevenue(
    val gameId: String,
    val gameName: String,
    val releaseDate: Date,
    val releasePrice: Double,
    // 新增：游戏内发售日期（与系统日期区分）
    val releaseYear: Int = 1,
    val releaseMonth: Int = 1,
    val releaseDay: Int = 1,
    val isActive: Boolean = true,
    val dailySalesList: List<DailySales> = emptyList(),
    val statistics: RevenueStatistics? = null,
    val updateTask: GameUpdateTask? = null,
    // 新增：更新次数与销量累计倍数（每次更新+5%）
    val updateCount: Int = 0,
    val cumulativeSalesMultiplier: Double = 1.0,
    // 新增：付费内容收入列表（仅网络游戏）
    val monetizationRevenues: List<MonetizationRevenue> = emptyList(),
    // 新增：玩家兴趣值系统（仅网络游戏）
    val playerInterest: Double = 100.0, // 玩家兴趣值 0-100
    val totalRegisteredPlayers: Int = 0, // 总注册人数
    val lifecycleProgress: Double = 0.0, // 生命周期进度 0-100%
    val daysSinceLaunch: Int = 0, // 上线天数
    val lastInterestDecayDay: Int = 0 // 上次兴趣值衰减的天数
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
    // 存储游戏商业模式和付费内容信息
    private val gameInfoMap = mutableMapOf<String, Pair<com.example.yjcy.ui.BusinessModel, List<MonetizationItem>>>()
    // 存储游戏服务器信息
    private val gameServerMap = mutableMapOf<String, GameServerInfo>()
    
    // SharedPreferences用于持久化
    private var sharedPreferences: android.content.SharedPreferences? = null
    
    /**
     * 初始化RevenueManager，传入Context以支持持久化
     */
    fun initialize(context: android.content.Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("revenue_manager", android.content.Context.MODE_PRIVATE)
            loadServerData()
            loadRevenueData()
        }
    }
    
    /**
     * 保存服务器数据到SharedPreferences
     */
    private fun saveServerData() {
        val prefs = sharedPreferences ?: return
        val editor = prefs.edit()
        
        // 将gameServerMap序列化为JSON字符串
        val json = serializeServerMap(gameServerMap)
        editor.putString("server_map", json)
        editor.apply()
    }
    
    /**
     * 从SharedPreferences加载服务器数据
     */
    private fun loadServerData() {
        val prefs = sharedPreferences ?: return
        val json = prefs.getString("server_map", null) ?: return
        
        // 反序列化JSON字符串为Map
        val loadedMap = deserializeServerMap(json)
        gameServerMap.clear()
        gameServerMap.putAll(loadedMap)
    }
    
    /**
     * 序列化服务器Map为JSON字符串
     */
    private fun serializeServerMap(map: Map<String, GameServerInfo>): String {
        val sb = StringBuilder()
        sb.append("{")
        map.entries.forEachIndexed { index, entry ->
            if (index > 0) sb.append(",")
            sb.append("\"${entry.key}\":")
            sb.append(serializeGameServerInfo(entry.value))
        }
        sb.append("}")
        return sb.toString()
    }
    
    /**
     * 序列化GameServerInfo为JSON
     */
    private fun serializeGameServerInfo(info: GameServerInfo): String {
        val sb = StringBuilder()
        sb.append("{\"gameId\":\"${info.gameId}\",\"servers\":[")
        info.servers.forEachIndexed { index, server ->
            if (index > 0) sb.append(",")
            sb.append("{")
            sb.append("\"id\":\"${server.id}\",")
            sb.append("\"type\":\"${server.type.name}\",")
            sb.append("\"purchaseYear\":${server.purchaseYear},")
            sb.append("\"purchaseMonth\":${server.purchaseMonth},")
            sb.append("\"purchaseDay\":${server.purchaseDay},")
            sb.append("\"isActive\":${server.isActive}")
            sb.append("}")
        }
        sb.append("]}")
        return sb.toString()
    }
    
    /**
     * 反序列化JSON字符串为服务器Map
     */
    private fun deserializeServerMap(json: String): Map<String, GameServerInfo> {
        val map = mutableMapOf<String, GameServerInfo>()
        try {
            // 简单的JSON解析（生产环境建议使用Gson或Kotlinx.serialization）
            val jsonObj = json.trim().removeSurrounding("{", "}")
            if (jsonObj.isEmpty()) return map
            
            // 手动解析JSON（简化版）
            var currentPos = 0
            while (currentPos < jsonObj.length) {
                // 找到gameId
                val keyStart = jsonObj.indexOf("\"", currentPos) + 1
                val keyEnd = jsonObj.indexOf("\"", keyStart)
                if (keyStart <= 0 || keyEnd < 0) break
                
                val gameId = jsonObj.substring(keyStart, keyEnd)
                
                // 找到对应的GameServerInfo对象
                val objStart = jsonObj.indexOf("{", keyEnd)
                var braceCount = 1
                var objEnd = objStart + 1
                while (braceCount > 0 && objEnd < jsonObj.length) {
                    when (jsonObj[objEnd]) {
                        '{' -> braceCount++
                        '}' -> braceCount--
                    }
                    objEnd++
                }
                
                val infoJson = jsonObj.substring(objStart, objEnd)
                val info = deserializeGameServerInfo(gameId, infoJson)
                map[gameId] = info
                
                currentPos = objEnd
            }
        } catch (e: Exception) {
            android.util.Log.e("RevenueManager", "Failed to deserialize server data", e)
        }
        return map
    }
    
    /**
     * 反序列化GameServerInfo
     */
    private fun deserializeGameServerInfo(gameId: String, json: String): GameServerInfo {
        val servers = mutableListOf<ServerInstance>()
        
        try {
            // 提取servers数组
            val serversStart = json.indexOf("[")
            val serversEnd = json.lastIndexOf("]")
            if (serversStart >= 0 && serversEnd > serversStart) {
                val serversJson = json.substring(serversStart + 1, serversEnd)
                
                // 解析每个server对象
                var pos = 0
                while (pos < serversJson.length) {
                    val objStart = serversJson.indexOf("{", pos)
                    if (objStart < 0) break
                    
                    val objEnd = serversJson.indexOf("}", objStart) + 1
                    val serverJson = serversJson.substring(objStart, objEnd)
                    
                    // 提取字段
                    val id = extractStringField(serverJson, "id")
                    val typeName = extractStringField(serverJson, "type")
                    val purchaseYear = extractIntField(serverJson, "purchaseYear")
                    val purchaseMonth = extractIntField(serverJson, "purchaseMonth")
                    val purchaseDay = extractIntField(serverJson, "purchaseDay")
                    val isActive = extractBooleanField(serverJson, "isActive")
                    
                    val serverType = ServerType.valueOf(typeName)
                    servers.add(
                        ServerInstance(
                            id = id,
                            type = serverType,
                            purchaseYear = purchaseYear,
                            purchaseMonth = purchaseMonth,
                            purchaseDay = purchaseDay,
                            isActive = isActive
                        )
                    )
                    
                    pos = objEnd
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RevenueManager", "Failed to deserialize GameServerInfo", e)
        }
        
        return GameServerInfo(gameId = gameId, servers = servers)
    }
    
    private fun extractStringField(json: String, fieldName: String): String {
        val pattern = "\"$fieldName\":\"([^\"]*)\""
        val regex = Regex(pattern)
        val match = regex.find(json)
        return match?.groupValues?.get(1) ?: ""
    }
    
    private fun extractIntField(json: String, fieldName: String): Int {
        val pattern = "\"$fieldName\":(\\d+)"
        val regex = Regex(pattern)
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
    
    private fun extractBooleanField(json: String, fieldName: String): Boolean {
        val pattern = "\"$fieldName\":(true|false)"
        val regex = Regex(pattern)
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toBoolean() ?: true
    }
    
    /**
     * 保存收益数据到SharedPreferences
     */
    private fun saveRevenueData() {
        val prefs = sharedPreferences ?: return
        val editor = prefs.edit()
        
        // 保存游戏收益数据（只保存关键字段）
        val revenueCount = gameRevenueMap.size
        editor.putInt("revenue_count", revenueCount)
        
        gameRevenueMap.entries.forEachIndexed { index, entry ->
            val revenue = entry.value
            editor.putString("revenue_${index}_id", revenue.gameId)
            editor.putString("revenue_${index}_name", revenue.gameName)
            editor.putFloat("revenue_${index}_price", revenue.releasePrice.toFloat())
            editor.putBoolean("revenue_${index}_active", revenue.isActive)
            editor.putInt("revenue_${index}_update_count", revenue.updateCount)
            editor.putFloat("revenue_${index}_multiplier", revenue.cumulativeSalesMultiplier.toFloat())
            
            // 保存每日销量数据（只保存最近30天）
            val recentDailySales = revenue.dailySalesList.takeLast(30)
            editor.putInt("revenue_${index}_days", recentDailySales.size)
            recentDailySales.forEachIndexed { dayIndex, dailySales ->
                editor.putLong("revenue_${index}_day${dayIndex}_time", dailySales.date.time)
                editor.putInt("revenue_${index}_day${dayIndex}_sales", dailySales.sales)
                editor.putFloat("revenue_${index}_day${dayIndex}_revenue", dailySales.revenue.toFloat())
            }
        }
        
        editor.apply()
    }
    
    /**
     * 从SharedPreferences加载收益数据
     */
    private fun loadRevenueData() {
        val prefs = sharedPreferences ?: return
        
        try {
            val revenueCount = prefs.getInt("revenue_count", 0)
            if (revenueCount == 0) return
            
            gameRevenueMap.clear()
            
            for (i in 0 until revenueCount) {
                val gameId = prefs.getString("revenue_${i}_id", null) ?: continue
                val gameName = prefs.getString("revenue_${i}_name", "") ?: ""
                val releasePrice = prefs.getFloat("revenue_${i}_price", 0f).toDouble()
                val isActive = prefs.getBoolean("revenue_${i}_active", true)
                val updateCount = prefs.getInt("revenue_${i}_update_count", 0)
                val multiplier = prefs.getFloat("revenue_${i}_multiplier", 1.0f).toDouble()
                
                // 加载每日销量数据
                val daysCount = prefs.getInt("revenue_${i}_days", 0)
                val dailySalesList = mutableListOf<DailySales>()
                for (dayIndex in 0 until daysCount) {
                    val time = prefs.getLong("revenue_${i}_day${dayIndex}_time", 0)
                    val sales = prefs.getInt("revenue_${i}_day${dayIndex}_sales", 0)
                    val revenue = prefs.getFloat("revenue_${i}_day${dayIndex}_revenue", 0f).toDouble()
                    
                    dailySalesList.add(
                        DailySales(
                            date = Date(time),
                            sales = sales,
                            revenue = revenue
                        )
                    )
                }
                
                val gameRevenue = GameRevenue(
                    gameId = gameId,
                    gameName = gameName,
                    releaseDate = if (dailySalesList.isNotEmpty()) dailySalesList.first().date else Date(),
                    releasePrice = releasePrice,
                    isActive = isActive,
                    dailySalesList = dailySalesList,
                    updateCount = updateCount,
                    cumulativeSalesMultiplier = multiplier
                )
                
                gameRevenueMap[gameId] = gameRevenue
            }
        } catch (e: Exception) {
            android.util.Log.e("RevenueManager", "Failed to load revenue data", e)
        }
    }
    
    /**
     * 为游戏生成模拟收益数据
     */
    fun generateRevenueData(
        gameId: String,
        gameName: String,
        releasePrice: Double,
        daysOnMarket: Int = 30,
        releaseYear: Int = 1,
        releaseMonth: Int = 1,
        releaseDay: Int = 1
    ): GameRevenue {
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
            releaseYear = releaseYear,
            releaseMonth = releaseMonth,
            releaseDay = releaseDay,
            dailySalesList = dailySalesList
        )
        
        gameRevenueMap[gameId] = gameRevenue
        saveRevenueData()
        return gameRevenue
    }

    /**
     * 计算下一次更新成本（随更新次数递增）
     * 规则：基础价为首发价的20%，每次更新成本在上次基础上×(1 + 0.15)
     * 也可理解为 base * (1.15^updateCount)
     */
    fun calculateUpdateCost(gameId: String): Double {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0.0
        // 基础价固定为 50,000，后续每次在此基础上递增
        val base = 50_000.0
        val factor = Math.pow(1.15, gameRevenue.updateCount.toDouble())
        return base * factor
    }

    /**
     * 应用一次游戏更新：销量整体提高5%，记录更新次数，并插入当日“更新热度”提升
     * 网络游戏：恢复玩家兴趣值，增加总注册人数
     */
    fun applyGameUpdate(gameId: String): Boolean {
        val current = gameRevenueMap[gameId] ?: return false
        val newMultiplier = current.cumulativeSalesMultiplier * 1.05

        // 将“即时应用”改为：若存在更新任务且未完成，则不生效；完成时批量应用
        if (current.updateTask != null && current.updateTask.progressPoints < current.updateTask.requiredPoints) {
            return false
        }

        // 批量提升历史销量
        val ratio = newMultiplier / current.cumulativeSalesMultiplier
        val adjustedDaily = current.dailySalesList.map { d ->
            val newSales = (d.sales * ratio).toInt().coerceAtLeast(1)
            val newRevenue = newSales * current.releasePrice
            d.copy(sales = newSales, revenue = newRevenue)
        }

        // 检查是否为网络游戏，恢复玩家兴趣值
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        var newPlayerInterest = current.playerInterest
        var newTotalRegisteredPlayers = current.totalRegisteredPlayers
        
        if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            // 恢复玩家兴趣值（根据生命周期阶段）
            newPlayerInterest = recoverInterestAfterUpdate(current.playerInterest, current.lifecycleProgress)
            
            // 更新带来新玩家：增加10-20%的总注册人数
            val newPlayersRatio = Random.nextDouble(0.1, 0.2)
            val newPlayers = (current.totalRegisteredPlayers * newPlayersRatio).toInt()
            newTotalRegisteredPlayers = current.totalRegisteredPlayers + newPlayers
        }

        gameRevenueMap[gameId] = current.copy(
            dailySalesList = adjustedDaily,
            updateTask = null,
            updateCount = current.updateCount + 1,
            cumulativeSalesMultiplier = newMultiplier,
            playerInterest = newPlayerInterest,
            totalRegisteredPlayers = newTotalRegisteredPlayers
        )
        saveRevenueData()
        return true
    }

    /**
     * 创建一条更新任务
     */
    fun createUpdateTask(gameId: String, features: List<String>): GameUpdateTask? {
        val current = gameRevenueMap[gameId] ?: return null
        if (current.updateTask != null) return current.updateTask
        // 简单估算：每个特性 100 进度点
        val required = (features.size * 100).coerceAtLeast(100)
        val task = GameUpdateTask(features = features, requiredPoints = required)
        gameRevenueMap[gameId] = current.copy(updateTask = task)
        saveRevenueData()
        return task
    }

    /**
     * 由外部（例如员工开发）推进更新任务的进度
     */
    fun progressUpdateTask(gameId: String, points: Int): Boolean {
        val current = gameRevenueMap[gameId] ?: return false
        val task = current.updateTask ?: return false
        val newPoints = (task.progressPoints + points).coerceAtMost(task.requiredPoints)
        val newTask = task.copy(progressPoints = newPoints)
        gameRevenueMap[gameId] = current.copy(updateTask = newTask)
        saveRevenueData()
        // 任务完成时应用收益侧更新
        if (newPoints >= task.requiredPoints) {
            applyGameUpdate(gameId)
        }
        return true
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
        
        // 基础销售收入
        val baseSalesRevenue = dailySales.sumOf { it.revenue }
        
        // 付费内容总收入（网络游戏）
        val monetizationTotalRevenue = gameRevenue.monetizationRevenues.sumOf { it.totalRevenue }
        
        // 总收入 = 游戏销售收入 + 付费内容收入
        val totalRevenue = baseSalesRevenue + monetizationTotalRevenue
        
        val totalSales = dailySales.sumOf { it.sales }
        
        // 平均每日收入也要包含付费内容
        val averageDailyRevenue = totalRevenue / dailySales.size
        val averageDailySales = totalSales / dailySales.size
        val peakDailySales = dailySales.maxOf { it.sales }
        
        // 单日最高收入：游戏销售 + 当日付费内容收入（简化处理，使用日均值）
        val dailyMonetizationAvg = if (dailySales.isNotEmpty()) {
            monetizationTotalRevenue / dailySales.size
        } else 0.0
        val peakDailyRevenue = dailySales.maxOf { it.revenue } + dailyMonetizationAvg
        
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
            saveRevenueData()
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
            saveRevenueData()
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
        saveRevenueData()
    }
    
    /**
     * 更新游戏信息（商业模式和付费内容）
     */
    fun updateGameInfo(gameId: String, businessModel: com.example.yjcy.ui.BusinessModel, monetizationItems: List<MonetizationItem>) {
        gameInfoMap[gameId] = Pair(businessModel, monetizationItems)
    }
    
    /**
     * 为已发售游戏添加新的一天收益数据
     */
    fun addDailyRevenueForGame(gameId: String): Double {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0.0
        
        // 如果游戏已下架，则不产生收益
        if (!gameRevenue.isActive) return 0.0
        
        // 检查是否为网络游戏
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        // 如果是网络游戏，更新兴趣值和生命周期
        if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            updateOnlineGameInterest(gameId)
        }
        
        // 获取最新的销量数据作为参考
        val latestSales = gameRevenue.dailySalesList.lastOrNull()
        
        // 如果是第一天（没有历史数据），初始化首日销量
        if (latestSales == null) {
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
            
            // 网络游戏：初始化总注册人数
            val totalRegistered = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
                baseSales
            } else {
                gameRevenue.totalRegisteredPlayers
            }
            
            // 更新游戏收益数据
            gameRevenueMap[gameId] = gameRevenue.copy(
                dailySalesList = listOf(firstDaySales),
                totalRegisteredPlayers = totalRegistered
            )
            saveRevenueData()
            
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
        
        // 获取付费内容配置
        val monetizationItems = gameInfoMap[gameId]?.second ?: emptyList()
        
        var dailyMonetizationRevenue = 0.0 // 当日付费内容收益
        val monetizationRevenues = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            // 计算当天的付费内容收益
            val dailyRevenues = calculateMonetizationRevenues(newSales, monetizationItems)
            dailyMonetizationRevenue = dailyRevenues.sumOf { it.totalRevenue }
            
            // 将当天的收益累加到之前的累计收益上
            val existingRevenues = gameRevenue.monetizationRevenues
            if (existingRevenues.isEmpty()) {
                // 第一天，直接使用当天收益
                dailyRevenues
            } else {
                // 后续天数，累加收益
                dailyRevenues.map { daily ->
                    val existing = existingRevenues.find { it.itemType == daily.itemType }
                    if (existing != null) {
                        // 累加：旧的累计收益 + 当天收益
                        daily.copy(totalRevenue = existing.totalRevenue + daily.totalRevenue)
                    } else {
                        // 新增的付费内容
                        daily
                    }
                }
            }
        } else {
            gameRevenue.monetizationRevenues
        }
        
        // 使用当日付费内容收益（不是累计值）
        val monetizationTotalRevenue = dailyMonetizationRevenue
        
        gameRevenueMap[gameId] = gameRevenue.copy(
            dailySalesList = updatedDailySalesList,
            monetizationRevenues = monetizationRevenues
        )
        saveRevenueData()
        
        // 返回总收益（销售收益 + 付费内容收益）
        return newRevenue + monetizationTotalRevenue
    }
    
    /**
     * 计算网络游戏的付费内容收益
     * @param activePlayers 当日活跃玩家数
     * @param monetizationItems 游戏的付费内容列表
     * @return 付费内容收益列表
     */
    private fun calculateMonetizationRevenues(
        activePlayers: Int,
        monetizationItems: List<MonetizationItem>
    ): List<MonetizationRevenue> {
        // 只计算已启用且设置了价格的付费内容
        val enabledItems = monetizationItems.filter { it.isEnabled && it.price != null && it.price > 0 }
        
        if (enabledItems.isEmpty()) return emptyList()
        
        return enabledItems.map { item ->
            // 根据付费内容类型设置不同的付费率
            val purchaseRate = when (item.type.displayName) {
                "皮肤与外观" -> 0.05  // 5%的活跃玩家会购买皮肤
                "成长加速道具" -> 0.08  // 8%会购买加速道具
                "稀有装备" -> 0.03  // 3%会购买稀有装备
                "赛季通行证" -> 0.15  // 15%会购买赛季通行证
                "强力角色" -> 0.04  // 4%会购买角色
                "VIP会员" -> 0.10  // 10%会购买VIP
                "抽卡系统" -> 0.20  // 20%会参与抽卡
                "扩展包" -> 0.06  // 6%会购买扩展包
                "资源包" -> 0.12  // 12%会购买资源包
                "战斗通行证" -> 0.18  // 18%会购买战斗通行证
                "宠物与坐骑" -> 0.07  // 7%会购买宠物
                "便利工具" -> 0.09  // 9%会购买工具
                "专属剧情" -> 0.05  // 5%会购买剧情
                "建造蓝图" -> 0.08  // 8%会购买蓝图
                "社交道具" -> 0.06  // 6%会购买社交道具
                else -> 0.05  // 默认5%
            }
            
            // 计算购买人数（带随机波动）
            val basebuyers = (activePlayers * purchaseRate).toInt()
            val fluctuation = Random.nextDouble(0.7, 1.3)
            val actualBuyers = (basebuyers * fluctuation).toInt().coerceAtLeast(0)
            
            // 计算收益
            val revenue = actualBuyers * (item.price ?: 0f)
            
            MonetizationRevenue(
                itemType = item.type.displayName,
                totalRevenue = revenue.toDouble()
            )
        }
    }
    
    /**
     * 清除所有收益数据（用于新游戏）
     */
    fun clearAllData() {
        gameRevenueMap.clear()
        gameInfoMap.clear()
        gameServerMap.clear()
        
        // 清除持久化数据
        val prefs = sharedPreferences ?: return
        prefs.edit().clear().apply()
    }
    
    // ========== 服务器管理功能 ==========
    
    /**
     * 获取游戏的服务器信息
     */
    fun getGameServerInfo(gameId: String): GameServerInfo {
        return gameServerMap.getOrPut(gameId) {
            GameServerInfo(gameId = gameId)
        }
    }
    
    /**
     * 为游戏购买服务器
     */
    fun purchaseServer(
        gameId: String,
        serverType: ServerType,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): ServerInstance {
        val serverInfo = getGameServerInfo(gameId)
        val newServer = ServerInstance(
            id = "${gameId}_server_${System.currentTimeMillis()}",
            type = serverType,
            purchaseYear = currentYear,
            purchaseMonth = currentMonth,
            purchaseDay = currentDay
        )
        
        val updatedServers = serverInfo.servers + newServer
        gameServerMap[gameId] = serverInfo.copy(servers = updatedServers)
        saveServerData()
        
        return newServer
    }
    
    /**
     * 为游戏添加服务器（purchaseServer的别名）
     */
    fun addServerToGame(
        gameId: String,
        serverType: ServerType,
        purchaseYear: Int,
        purchaseMonth: Int,
        purchaseDay: Int
    ) {
        purchaseServer(gameId, serverType, purchaseYear, purchaseMonth, purchaseDay)
        saveServerData()
    }
    
    /**
     * 停用服务器
     */
    fun deactivateServer(gameId: String, serverId: String): Boolean {
        val serverInfo = gameServerMap[gameId] ?: return false
        val updatedServers = serverInfo.servers.map { server ->
            if (server.id == serverId) {
                server.copy(isActive = false)
            } else {
                server
            }
        }
        gameServerMap[gameId] = serverInfo.copy(servers = updatedServers)
        saveServerData()
        return true
    }
    
    /**
     * 激活服务器
     */
    fun activateServer(gameId: String, serverId: String): Boolean {
        val serverInfo = gameServerMap[gameId] ?: return false
        val updatedServers = serverInfo.servers.map { server ->
            if (server.id == serverId) {
                server.copy(isActive = true)
            } else {
                server
            }
        }
        gameServerMap[gameId] = serverInfo.copy(servers = updatedServers)
        saveServerData()
        return true
    }
    
    /**
     * 检查服务器容量是否足够
     */
    fun hasEnoughServerCapacity(gameId: String, requiredCapacity: Int): Boolean {
        val serverInfo = gameServerMap[gameId] ?: return false
        return serverInfo.getTotalCapacity() >= requiredCapacity
    }
    
    /**
     * 更新游戏服务器信息
     */
    fun updateGameServerInfo(gameId: String, serverInfo: GameServerInfo) {
        gameServerMap[gameId] = serverInfo
        saveServerData()
    }
    
    /**
     * 计算所有服务器的总月费
     */
    fun calculateTotalMonthlyServerCost(): Long {
        var totalCost = 0L
        gameServerMap.entries.forEach { (gameId, serverInfo) ->
            // 排除公共池，避免重复计费（公共池的服务器已经分配到各个游戏中）
            if (gameId != "SERVER_PUBLIC_POOL") {
                serverInfo.servers.forEach { server ->
                    if (server.isActive) {
                        totalCost += server.type.cost
                    }
                }
            }
        }
        return totalCost
    }
    
    /**
     * 计算网络游戏的生命周期进度 (0-100%)
     * 基于上线天数和预设的生命周期总天数
     */
    fun calculateLifecycleProgress(daysSinceLaunch: Int): Double {
        // 定义网络游戏的标准生命周期为365天（1年）
        val totalLifecycleDays = 365
        val progress = (daysSinceLaunch.toDouble() / totalLifecycleDays) * 100.0
        return progress.coerceIn(0.0, 100.0)
    }
    
    /**
     * 计算玩家兴趣值衰减
     * @param currentInterest 当前兴趣值
     * @param lifecycleProgress 生命周期进度 (0-100%)
     * @return 衰减后的兴趣值
     */
    fun calculateInterestDecay(currentInterest: Double, lifecycleProgress: Double): Double {
        // 根据生命周期阶段确定衰减率
        val decayRate = when {
            lifecycleProgress < 30.0 -> 0.3  // 成长期：每天衰减0.3%
            lifecycleProgress < 70.0 -> 0.5  // 成熟期：每天衰减0.5%
            lifecycleProgress < 90.0 -> 1.0  // 衰退期：每天衰减1.0%
            else -> 1.5                      // 末期：每天衰减1.5%
        }
        
        val newInterest = currentInterest - decayRate
        return newInterest.coerceIn(0.0, 100.0)
    }
    
    /**
     * 更新游戏后恢复玩家兴趣值
     * @param currentInterest 当前兴趣值
     * @param lifecycleProgress 生命周期进度
     * @return 新的兴趣值
     */
    fun recoverInterestAfterUpdate(currentInterest: Double, lifecycleProgress: Double): Double {
        // 根据生命周期阶段确定恢复量
        val recoveryAmount = when {
            lifecycleProgress < 30.0 -> 25.0  // 成长期：恢复25%
            lifecycleProgress < 70.0 -> 15.0  // 成熟期：恢复15%
            lifecycleProgress < 90.0 -> 8.0   // 衰退期：恢复8%
            else -> 0.0                       // 末期：无法恢复
        }
        
        val newInterest = currentInterest + recoveryAmount
        return newInterest.coerceIn(0.0, 100.0)
    }
    
    /**
     * 根据兴趣值计算活跃玩家倍数
     * @param playerInterest 玩家兴趣值 (0-100)
     * @return 活跃玩家倍数 (0.0-1.0)
     */
    fun calculateActivePlayerMultiplier(playerInterest: Double): Double {
        return when {
            playerInterest >= 70.0 -> 1.0         // 正常状态
            playerInterest >= 50.0 -> 0.7         // 小幅下降
            playerInterest >= 30.0 -> 0.4         // 大幅下降
            else -> 0.2                           // 严重下降
        }
    }
    
    /**
     * 更新网络游戏的玩家兴趣值和生命周期
     * 每天调用一次
     */
    fun updateOnlineGameInterest(gameId: String) {
        val gameRevenue = gameRevenueMap[gameId] ?: return
        
        // 只处理网络游戏
        val (businessModel, _) = gameInfoMap[gameId] ?: return
        if (businessModel != com.example.yjcy.ui.BusinessModel.ONLINE_GAME) return
        
        // 增加上线天数
        val newDaysSinceLaunch = gameRevenue.daysSinceLaunch + 1
        
        // 计算生命周期进度
        val newLifecycleProgress = calculateLifecycleProgress(newDaysSinceLaunch)
        
        // 每天衰减兴趣值（避免重复衰减）
        val shouldDecay = gameRevenue.lastInterestDecayDay < newDaysSinceLaunch
        val newInterest = if (shouldDecay) {
            calculateInterestDecay(gameRevenue.playerInterest, newLifecycleProgress)
        } else {
            gameRevenue.playerInterest
        }
        
        // 更新游戏数据
        gameRevenueMap[gameId] = gameRevenue.copy(
            daysSinceLaunch = newDaysSinceLaunch,
            lifecycleProgress = newLifecycleProgress,
            playerInterest = newInterest,
            lastInterestDecayDay = newDaysSinceLaunch
        )
        saveRevenueData()
    }
    
    /**
     * 获取当前活跃玩家数（考虑兴趣值影响）
     */
    fun getActivePlayers(gameId: String): Int {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0
        
        // 检查是否为网络游戏
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        if (businessModel != com.example.yjcy.ui.BusinessModel.ONLINE_GAME) return 0
        
        // 基础活跃玩家 = 总销量 * 40%（作为注册玩家基数）
        val statistics = calculateStatistics(gameRevenue)
        val baseActivePlayers = (statistics.totalSales * 0.4).toInt()
        
        // 根据兴趣值调整活跃玩家数
        val interestMultiplier = calculateActivePlayerMultiplier(gameRevenue.playerInterest)
        
        return (baseActivePlayers * interestMultiplier).toInt()
    }
    
}