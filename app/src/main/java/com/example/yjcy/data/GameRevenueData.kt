package com.example.yjcy.data

import java.util.*
import java.text.SimpleDateFormat
import kotlin.random.Random

/**
 * 每日销量数据类
 */
data class DailySales(
    val date: Date,
    val sales: Long,
    val revenue: Double
)

/**
 * 销量数据类，用于图表显示
 */
data class SalesData(
    val label: String,
    val value: Long,
    val revenue: Double
)

/**
 * 收益统计数据类
 */
data class RevenueStatistics(
    val totalRevenue: Double,
    val totalSales: Long,
    val averageDailyRevenue: Double,
    val averageDailySales: Long,
    val peakDailySales: Long,
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
    val progressPoints: Int = 0,
    val announcement: String = "" // 更新公告
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
    val totalRegisteredPlayers: Long = 0, // 总注册人数
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
    fun getTotalSales(): Long {
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
    fun getAverageDailySales(): Long {
        return if (dailySalesList.isNotEmpty()) {
            getTotalSales() / dailySalesList.size
        } else 0L
    }
    
    /**
     * 获取在售天数
     */
    fun getDaysOnMarket(): Int {
        return dailySalesList.size
    }
    
    /**
     * 获取当前活跃玩家数（仅网络游戏）
     */
    fun getActivePlayers(): Long {
        // 基础活跃玩家 = 总注册人数 * 40%
        val baseActivePlayers = (totalRegisteredPlayers * 0.4).toLong()
        
        // 根据兴趣值计算倍数
        val interestMultiplier = when {
            playerInterest >= 70.0 -> 1.0         // 正常状态
            playerInterest >= 50.0 -> 0.7         // 小幅下降
            playerInterest >= 30.0 -> 0.4         // 大幅下降
            else -> 0.2                           // 严重下降
        }
        
        return (baseActivePlayers * interestMultiplier).toLong()
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
        
        // 数据迁移：强制保存一次，确保lastBilling字段被持久化
        saveServerData()
        android.util.Log.d("RevenueManager", "服务器数据迁移完成，lastBilling字段已保存")
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
            sb.append("\"isActive\":${server.isActive},")
            sb.append("\"lastBillingYear\":${server.lastBillingYear},")
            sb.append("\"lastBillingMonth\":${server.lastBillingMonth},")
            sb.append("\"lastBillingDay\":${server.lastBillingDay}")
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
                    
                    // 提取lastBilling字段（兼容旧存档，如果不存在则使用purchase日期）
                    val lastBillingYear = extractIntFieldOrDefault(serverJson, "lastBillingYear", purchaseYear)
                    val lastBillingMonth = extractIntFieldOrDefault(serverJson, "lastBillingMonth", purchaseMonth)
                    val lastBillingDay = extractIntFieldOrDefault(serverJson, "lastBillingDay", purchaseDay)
                    
                    val serverType = ServerType.valueOf(typeName)
                    servers.add(
                        ServerInstance(
                            id = id,
                            type = serverType,
                            purchaseYear = purchaseYear,
                            purchaseMonth = purchaseMonth,
                            purchaseDay = purchaseDay,
                            isActive = isActive,
                            lastBillingYear = lastBillingYear,
                            lastBillingMonth = lastBillingMonth,
                            lastBillingDay = lastBillingDay
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
    
    private fun extractIntFieldOrDefault(json: String, fieldName: String, defaultValue: Int): Int {
        val pattern = "\"$fieldName\":(\\d+)"
        val regex = Regex(pattern)
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: defaultValue
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
            
            // 保存网游特有字段
            editor.putInt("revenue_${index}_release_year", revenue.releaseYear)
            editor.putInt("revenue_${index}_release_month", revenue.releaseMonth)
            editor.putInt("revenue_${index}_release_day", revenue.releaseDay)
            editor.putFloat("revenue_${index}_player_interest", revenue.playerInterest.toFloat())
            editor.putLong("revenue_${index}_total_registered", revenue.totalRegisteredPlayers)
            editor.putFloat("revenue_${index}_lifecycle_progress", revenue.lifecycleProgress.toFloat())
            editor.putInt("revenue_${index}_days_since_launch", revenue.daysSinceLaunch)
            editor.putInt("revenue_${index}_last_decay_day", revenue.lastInterestDecayDay)
            
            // 保存付费内容收益
            editor.putInt("revenue_${index}_monetization_count", revenue.monetizationRevenues.size)
            revenue.monetizationRevenues.forEachIndexed { monIndex, monRevenue ->
                editor.putString("revenue_${index}_mon${monIndex}_type", monRevenue.itemType)
                editor.putFloat("revenue_${index}_mon${monIndex}_revenue", monRevenue.totalRevenue.toFloat())
            }
            
            // 保存更新任务
            revenue.updateTask?.let { task ->
                editor.putBoolean("revenue_${index}_has_update_task", true)
                editor.putInt("revenue_${index}_task_required", task.requiredPoints)
                editor.putInt("revenue_${index}_task_progress", task.progressPoints)
                editor.putInt("revenue_${index}_task_feature_count", task.features.size)
                task.features.forEachIndexed { featureIndex, feature ->
                    editor.putString("revenue_${index}_task_feature${featureIndex}", feature)
                }
            } ?: editor.putBoolean("revenue_${index}_has_update_task", false)
            
            // 保存每日销量数据（只保存最近30天）
            val recentDailySales = revenue.dailySalesList.takeLast(30)
            editor.putInt("revenue_${index}_daily_sales_count", recentDailySales.size)
            recentDailySales.forEachIndexed { dayIndex, dailySales ->
                editor.putLong("revenue_${index}_daily_sales_${dayIndex}_date", dailySales.date.time)
                editor.putLong("revenue_${index}_daily_sales_${dayIndex}_sales", dailySales.sales)
                editor.putFloat("revenue_${index}_daily_sales_${dayIndex}_revenue", dailySales.revenue.toFloat())
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
                
                // 加载网游特有字段
                val releaseYear = prefs.getInt("revenue_${i}_release_year", 1)
                val releaseMonth = prefs.getInt("revenue_${i}_release_month", 1)
                val releaseDay = prefs.getInt("revenue_${i}_release_day", 1)
                val playerInterest = prefs.getFloat("revenue_${i}_player_interest", 100.0f).toDouble()
                val totalRegisteredPlayers = prefs.getLong("revenue_${i}_total_registered", 0L)
                val lifecycleProgress = prefs.getFloat("revenue_${i}_lifecycle_progress", 0.0f).toDouble()
                val daysSinceLaunch = prefs.getInt("revenue_${i}_days_since_launch", 0)
                val lastInterestDecayDay = prefs.getInt("revenue_${i}_last_decay_day", 0)
                
                // 加载付费内容收益
                val monetizationCount = prefs.getInt("revenue_${i}_monetization_count", 0)
                val monetizationRevenues = mutableListOf<MonetizationRevenue>()
                for (monIndex in 0 until monetizationCount) {
                    val itemType = prefs.getString("revenue_${i}_mon${monIndex}_type", "") ?: ""
                    val monRevenue = prefs.getFloat("revenue_${i}_mon${monIndex}_revenue", 0f).toDouble()
                    monetizationRevenues.add(MonetizationRevenue(itemType, monRevenue))
                }
                
                // 加载更新任务
                val hasUpdateTask = prefs.getBoolean("revenue_${i}_has_update_task", false)
                val updateTask = if (hasUpdateTask) {
                    val requiredPoints = prefs.getInt("revenue_${i}_task_required", 100)
                    val progressPoints = prefs.getInt("revenue_${i}_task_progress", 0)
                    val featureCount = prefs.getInt("revenue_${i}_task_feature_count", 0)
                    val features = mutableListOf<String>()
                    for (featureIndex in 0 until featureCount) {
                        val feature = prefs.getString("revenue_${i}_task_feature${featureIndex}", "") ?: ""
                        if (feature.isNotEmpty()) features.add(feature)
                    }
                    GameUpdateTask(features, requiredPoints, progressPoints)
                } else null
                
                // 加载每日销量数据
                val dailySalesCount = prefs.getInt("revenue_${i}_daily_sales_count", 0)
                val dailySalesList = mutableListOf<DailySales>()
                for (dayIndex in 0 until dailySalesCount) {
                    val date = Date(prefs.getLong("revenue_${i}_daily_sales_${dayIndex}_date", 0))
                    val sales = prefs.getLong("revenue_${i}_daily_sales_${dayIndex}_sales", 0L)
                    val revenue = prefs.getFloat("revenue_${i}_daily_sales_${dayIndex}_revenue", 0f).toDouble()
                    dailySalesList.add(DailySales(date, sales, revenue))
                }
                
                val gameRevenue = GameRevenue(
                    gameId = gameId,
                    gameName = gameName,
                    releaseDate = if (dailySalesList.isNotEmpty()) dailySalesList.first().date else Date(),
                    releasePrice = releasePrice,
                    releaseYear = releaseYear,
                    releaseMonth = releaseMonth,
                    releaseDay = releaseDay,
                    isActive = isActive,
                    dailySalesList = dailySalesList,
                    updateCount = updateCount,
                    cumulativeSalesMultiplier = multiplier,
                    monetizationRevenues = monetizationRevenues,
                    playerInterest = playerInterest,
                    totalRegisteredPlayers = totalRegisteredPlayers,
                    lifecycleProgress = lifecycleProgress,
                    daysSinceLaunch = daysSinceLaunch,
                    lastInterestDecayDay = lastInterestDecayDay,
                    updateTask = updateTask
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
        releaseDay: Int = 1,
        promotionIndex: Float = 0f  // 新增：宣传指数（0-1）
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
                i == 0 -> {
                    // 首日销量/注册，应用宣传指数提升
                    val baseValue = Random.nextInt(800, 1200)
                    // 宣传指数100%时，首发销量/注册提升25%（已下调，原50%）
                    // 低于100%也有提升，但效果没有100%多
                    val promotionBonus = 1f + (promotionIndex * 0.25f)
                    (baseValue * promotionBonus).toInt()
                }
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
                    sales = actualSales.toLong(),
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
     * 计算下一次更新成本（随更新次数递增，带成本上限）
     * 
     * **单机游戏**（DLC/资料片性质）：
     * - 基础成本：80,000元
     * - 递增系数：1.25（每次+25%）
     * - 成本上限：300,000元（第7次达到）
     * - 价格梯度：80K → 100K → 125K → 156K → 195K → 244K → 300K（上限）
     * 
     * **网络游戏**（持续运营性质）：
     * - 基础成本：40,000元
     * - 递增系数：1.25（每次+25%）
     * - 成本上限：150,000元（第7次达到）
     * - 价格梯度：40K → 50K → 62.5K → 78K → 97.5K → 122K → 150K（上限）
     * 
     * **设计理念**：
     * - 单机游戏基础成本是网游的2倍，符合DLC性质
     * - 设置成本上限防止后期成本过高导致游戏失衡
     * - 第7次更新后达到上限，保持固定成本
     * - 鼓励玩家持续维护游戏，但不会无限膨胀
     */
    fun calculateUpdateCost(gameId: String): Double {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0.0
        
        // 获取游戏商业模式
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        // 根据商业模式设置不同的基础成本和上限
        val (base, maxCost) = when (businessModel) {
            com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER -> Pair(80_000.0, 300_000.0)  // 单机：基础80K，上限300K
            com.example.yjcy.ui.BusinessModel.ONLINE_GAME -> Pair(40_000.0, 150_000.0)     // 网游：基础40K，上限150K
        }
        
        // 使用1.25的递增系数（每次+25%），但不超过上限
        val factor = Math.pow(1.25, gameRevenue.updateCount.toDouble())
        val calculatedCost = base * factor
        
        // 返回计算成本和上限中的较小值
        return calculatedCost.coerceAtMost(maxCost)
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
            val newSales = (d.sales * ratio).toLong().coerceAtLeast(1L)
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
    fun createUpdateTask(gameId: String, features: List<String>, announcement: String = ""): GameUpdateTask? {
        val current = gameRevenueMap[gameId] ?: return null
        if (current.updateTask != null) return current.updateTask
        // 简单估算：每个特性 100 进度点
        val required = (features.size * 100).coerceAtLeast(100)
        val task = GameUpdateTask(features = features, requiredPoints = required, announcement = announcement)
        gameRevenueMap[gameId] = current.copy(updateTask = task)
        saveRevenueData()
        return task
    }

    /**
     * 由外部（例如员工开发）推进更新任务的进度
     * @return 返回任务是否刚刚完成（从未完成到完成的状态转换）
     */
    fun progressUpdateTask(gameId: String, points: Int): Boolean {
        val current = gameRevenueMap[gameId] ?: return false
        val task = current.updateTask ?: return false
        val wasIncomplete = task.progressPoints < task.requiredPoints
        val newPoints = (task.progressPoints + points).coerceAtMost(task.requiredPoints)
        val newTask = task.copy(progressPoints = newPoints)
        gameRevenueMap[gameId] = current.copy(updateTask = newTask)
        saveRevenueData()
        // 任务完成时应用收益侧更新
        val isJustCompleted = wasIncomplete && newPoints >= task.requiredPoints
        if (isJustCompleted) {
            applyGameUpdate(gameId)
        }
        return isJustCompleted
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
     * 计算评分倍率（更陡峭的曲线，让低评分游戏销量大幅下降）
     */
    private fun calculateRatingMultiplier(rating: Float): Float {
        return when {
            rating >= 9.0f -> 1.5f  // 杰作（9-10分）
            rating >= 7.0f -> {
                // 优秀（7-9分）：线性插值 1.0 到 1.5
                1.0f + ((rating - 7.0f) / 2.0f) * 0.5f
            }
            rating >= 5.0f -> {
                // 及格（5-7分）：线性插值 0.6 到 1.0
                0.6f + ((rating - 5.0f) / 2.0f) * 0.4f
            }
            rating >= 3.0f -> {
                // 差评（3-5分）：线性插值 0.2 到 0.6
                0.2f + ((rating - 3.0f) / 2.0f) * 0.4f
            }
            else -> {
                // 烂作（0-3分）：线性插值 0.05 到 0.2
                0.05f + (rating / 3.0f) * 0.15f
            }
        }
    }
    
    /**
     * 根据游戏评分更新收益数据
     */
    fun updateRevenueBasedOnRating(gameId: String, rating: Float) {
        val gameRevenue = gameRevenueMap[gameId] ?: return
        
        // 使用新的评分倍率函数
        val multiplier = calculateRatingMultiplier(rating)
        
        // 更新每日销量数据，应用评分倍数
        val updatedDailySales = gameRevenue.dailySalesList.map { dailySales ->
            val adjustedSales = (dailySales.sales * multiplier).toLong()
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
     * 更新游戏价格（不覆盖历史数据）
     */
    fun updateGamePrice(gameId: String, newPrice: Double) {
        val existingRevenue = gameRevenueMap[gameId] ?: return
        gameRevenueMap[gameId] = existingRevenue.copy(releasePrice = newPrice)
        saveRevenueData()
    }
    
    /**
     * 为已发售游戏添加新的一天收益数据
     * @param gameId 游戏ID
     * @param gameRating 游戏评分（0-10），用于计算注册数加成
     * @param fanCount 公司粉丝数，用于计算注册数加成
     * @param currentYear 当前游戏内年份
     * @param currentMonth 当前游戏内月份
     * @param currentDay 当前游戏内日期
     */
    fun addDailyRevenueForGame(
        gameId: String, 
        gameRating: Float? = null, 
        fanCount: Int = 0,
        currentYear: Int = 1,
        currentMonth: Int = 1,
        currentDay: Int = 1,
        reputationBonus: Float = 0f  // 新增：声望带来的初期销量加成（0-0.2）
    ): Double {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0.0
        var currentGameRevenue = gameRevenue
        
        // 如果游戏已下架，则不产生收益
        if (!currentGameRevenue.isActive) return 0.0
        
        // 检查是否为网络游戏
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        // 如果是网络游戏，更新兴趣值和生命周期
        if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            // 修复旧存档：如果daysSinceLaunch为0，根据上线日期计算实际天数
            if (currentGameRevenue.daysSinceLaunch == 0) {
                val actualDays = calculateDaysSinceLaunch(
                    currentGameRevenue.releaseYear,
                    currentGameRevenue.releaseMonth,
                    currentGameRevenue.releaseDay,
                    currentYear,
                    currentMonth,
                    currentDay
                )
                // 初始化天数和兴趣值
                // lastInterestDecayDay 设置为最近一次衰减的天数（90天的倍数）
                val lastDecayDay = (actualDays / 90) * 90
                val initialInterest = calculateInitialInterest(actualDays)
                
                println("=== 旧存档修复 ===")
                println("游戏ID: $gameId")
                println("上线日期: ${currentGameRevenue.releaseYear}年${currentGameRevenue.releaseMonth}月${currentGameRevenue.releaseDay}日")
                println("当前日期: ${currentYear}年${currentMonth}月${currentDay}日")
                println("实际天数: $actualDays 天")
                println("衰减周期: ${actualDays / 90}")
                println("计算后兴趣值: $initialInterest%")
                println("lastDecayDay: $lastDecayDay")
                
                gameRevenueMap[gameId] = currentGameRevenue.copy(
                    daysSinceLaunch = actualDays,
                    lifecycleProgress = calculateLifecycleProgress(actualDays),
                    playerInterest = initialInterest,
                    lastInterestDecayDay = lastDecayDay
                )
                currentGameRevenue = gameRevenueMap[gameId] ?: currentGameRevenue
                saveRevenueData()
            }
            updateOnlineGameInterest(gameId)
            currentGameRevenue = gameRevenueMap[gameId] ?: currentGameRevenue
        }
        
        // 获取最新的销量数据作为参考
        val latestSales = currentGameRevenue.dailySalesList.lastOrNull()
        
        // 如果是第一天（没有历史数据），初始化首日销量
        if (latestSales == null) {
            val firstDayDate = currentGameRevenue.releaseDate
            
            // 首日销量/注册：根据商业模式调整
            val baseSales = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
                // 网游：免费游戏，首日注册人数较多
                val baseRegistrations = Random.nextInt(1000, 2000)
                // 根据游戏评分添加加成
                val withRatingBonus = applyRatingBonus(baseRegistrations, gameRating)
                // 根据粉丝数量添加加成
                val withFansBonus = applyFansBonus(withRatingBonus, fanCount)
                // 根据声望添加加成（初期销量提升）
                (withFansBonus * (1f + reputationBonus)).toInt()
            } else {
                // 单机：根据价格调整首日销量（已下调基础值）
                val baseSalesForPrice = when {
                    currentGameRevenue.releasePrice <= 30.0 -> Random.nextInt(500, 800) // 低价游戏（原800-1200）
                    currentGameRevenue.releasePrice <= 100.0 -> Random.nextInt(300, 500) // 中价游戏（原500-800）
                    else -> Random.nextInt(100, 300) // 高价游戏（原200-500）
                }
                // 应用评分倍率（单机游戏销量受评分影响很大）
                val ratingMultiplier = if (gameRating != null) calculateRatingMultiplier(gameRating) else 1.0f
                val withRatingMultiplier = (baseSalesForPrice * ratingMultiplier).toInt()
                // 单机游戏也应用粉丝加成（效果低于网游）
                val withFansBonus = applyFansBonusForSinglePlayer(withRatingMultiplier, fanCount)
                // 根据声望添加加成（初期销量提升）
                (withFansBonus * (1f + reputationBonus)).toInt()
            }
            
            val firstDayRevenue = baseSales * currentGameRevenue.releasePrice
            
            val firstDaySales = DailySales(
                date = firstDayDate,
                sales = baseSales.toLong(),
                revenue = firstDayRevenue
            )
            
            // 网络游戏：初始化总注册人数并计算付费内容收益
            val totalRegistered = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
                baseSales.toLong()
            } else {
                currentGameRevenue.totalRegisteredPlayers
            }
            
            // 计算首日付费内容收益（仅网游）
            val monetizationItems = gameInfoMap[gameId]?.second ?: emptyList()
            var firstDayMonetizationRevenue = 0.0
            val initialMonetizationRevenues = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
                // 首日活跃玩家 = 注册人数 * 40%（新游戏兴趣值默认100%）
                val firstDayActivePlayers = (baseSales * 0.4).toLong()
                val dailyRevenues = calculateMonetizationRevenues(firstDayActivePlayers, monetizationItems)
                firstDayMonetizationRevenue = dailyRevenues.sumOf { it.totalRevenue }
                dailyRevenues
            } else {
                emptyList()
            }
            
            // 更新游戏收益数据
            gameRevenueMap[gameId] = currentGameRevenue.copy(
                dailySalesList = listOf(firstDaySales),
                totalRegisteredPlayers = totalRegistered,
                monetizationRevenues = initialMonetizationRevenues
            )
            saveRevenueData()
            
            // 网游返回总收益（销售收益 + 付费内容收益）
            return firstDayRevenue + firstDayMonetizationRevenue
        }
        
        // 生成新的一天的销量数据（基于前一天的数据，但略有波动）
        val calendar = Calendar.getInstance()
        calendar.time = latestSales.date
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val newDate = calendar.time
        
        // 计算新增销量/注册
        val newSales = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            // 网游：基于玩家兴趣值计算新增注册
            val baseNewRegistrations = (latestSales.sales * 0.05).toInt() // 基础5%增长
            val interestMultiplier = (currentGameRevenue.playerInterest / 100.0) // 兴趣值影响
            val fluctuation = Random.nextDouble(0.8, 1.5) // 随机波动
            val registrationsBeforeBonus = (baseNewRegistrations * interestMultiplier * fluctuation).toInt().coerceAtLeast(10)
            // 根据游戏评分添加加成
            val withRatingBonus = applyRatingBonus(registrationsBeforeBonus, gameRating)
            // 根据粉丝数量添加加成
            applyFansBonus(withRatingBonus, fanCount)
        } else {
            // 单机：销量衰减模式
            val decayFactor = 0.98 // 每天衰减2%
            val fluctuation = Random.nextDouble(0.8, 1.2) // 随机波动
            (latestSales.sales * decayFactor * fluctuation).toInt().coerceAtLeast(1)
        }
        
        val newRevenue = newSales * currentGameRevenue.releasePrice
        
        // 创建新的每日销量数据
        val newDailySales = DailySales(
            date = newDate,
            sales = newSales.toLong(),
            revenue = newRevenue
        )
        
        // 更新游戏收益数据
        val updatedDailySalesList = currentGameRevenue.dailySalesList + newDailySales
        
        // 获取付费内容配置
        val monetizationItems = gameInfoMap[gameId]?.second ?: emptyList()
        
        var dailyMonetizationRevenue = 0.0 // 当日付费内容收益
        val monetizationRevenues = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            // 计算当天的付费内容收益 - 使用活跃玩家数（总注册*40%*兴趣值影响）
            val totalRegistered = currentGameRevenue.totalRegisteredPlayers + newSales
            val baseActivePlayers = (totalRegistered * 0.4).toLong()
            val interestMultiplier = calculateActivePlayerMultiplier(currentGameRevenue.playerInterest)
            val actualActivePlayers = (baseActivePlayers * interestMultiplier).toLong()
            
            val dailyRevenues = calculateMonetizationRevenues(actualActivePlayers, monetizationItems)
            dailyMonetizationRevenue = dailyRevenues.sumOf { it.totalRevenue }
            
            // 将当天的收益累加到之前的累计收益上
            val existingRevenues = currentGameRevenue.monetizationRevenues
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
        
        // 网游：更新总注册人数（累加每日新增）
        val newTotalRegisteredPlayers = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            currentGameRevenue.totalRegisteredPlayers + newSales
        } else {
            currentGameRevenue.totalRegisteredPlayers
        }
        
        gameRevenueMap[gameId] = currentGameRevenue.copy(
            dailySalesList = updatedDailySalesList,
            monetizationRevenues = monetizationRevenues,
            totalRegisteredPlayers = newTotalRegisteredPlayers
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
        activePlayers: Long,
        monetizationItems: List<MonetizationItem>
    ): List<MonetizationRevenue> {
        // 只计算已启用且设置了价格的付费内容
        val enabledItems = monetizationItems.filter { it.isEnabled && it.price != null && it.price > 0 }
        
        if (enabledItems.isEmpty()) return emptyList()
        
        return enabledItems.map { item ->
            // 根据付费内容类型设置不同的付费率（调整到0.5%-2.0%，更合理的付费率）
            val purchaseRate = when (item.type.displayName) {
                "皮肤与外观" -> 0.005  // 0.5%的活跃玩家会购买皮肤
                "成长加速道具" -> 0.008  // 0.8%会购买加速道具
                "稀有装备" -> 0.003  // 0.3%会购买稀有装备
                "赛季通行证" -> 0.015  // 1.5%会购买赛季通行证
                "强力角色" -> 0.004  // 0.4%会购买角色
                "VIP会员" -> 0.01  // 1.0%会购买VIP
                "抽卡系统" -> 0.02  // 2.0%会参与抽卡
                "扩展包" -> 0.006  // 0.6%会购买扩展包
                "资源包" -> 0.012  // 1.2%会购买资源包
                "战斗通行证" -> 0.018  // 1.8%会购买战斗通行证
                "宠物与坐骑" -> 0.007  // 0.7%会购买宠物
                "便利工具" -> 0.009  // 0.9%会购买工具
                "专属剧情" -> 0.005  // 0.5%会购买剧情
                "建造蓝图" -> 0.008  // 0.8%会购买蓝图
                "社交道具" -> 0.006  // 0.6%会购买社交道具
                else -> 0.005  // 默认0.5%
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
        android.util.Log.d("RevenueManager", "===== 清除所有数据 =====")
        android.util.Log.d("RevenueManager", "清除前gameServerMap大小: ${gameServerMap.size}")
        
        gameRevenueMap.clear()
        gameInfoMap.clear()
        gameServerMap.clear()
        
        android.util.Log.d("RevenueManager", "清除后gameServerMap大小: ${gameServerMap.size}")
        
        // 清除持久化数据
        val prefs = sharedPreferences ?: return
        prefs.edit().clear().apply()
        
        android.util.Log.d("RevenueManager", "===== SharedPreferences已清除 =====")
    }
    
    // ========== 服务器管理功能 ==========
    
    /**
     * 获取游戏的服务器信息
     */
    fun getGameServerInfo(gameId: String): GameServerInfo {
        val info = gameServerMap[gameId]
        if (info == null) {
            // 如果找不到，创建新的空信息（但记录日志）
            android.util.Log.d("RevenueManager", "⚠ getGameServerInfo: gameId=$gameId 不存在，创建新的空信息")
            android.util.Log.d("RevenueManager", "当前gameServerMap中的gameId: ${gameServerMap.keys.joinToString()}")
            val newInfo = GameServerInfo(gameId = gameId)
            gameServerMap[gameId] = newInfo
            return newInfo
        }
        return info
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
     * 计算所有服务器的总月费（已弃用，改用checkAndBillServers）
     */
    @Deprecated("使用checkAndBillServers代替")
    fun calculateTotalMonthlyServerCost(): Long {
        var totalCost = 0L
        gameServerMap.entries.forEach { (gameId, serverInfo) ->
            serverInfo.servers.forEach { server ->
                if (server.isActive) {
                    totalCost += server.type.cost
                }
            }
        }
        return totalCost
    }
    
    /**
     * 检查并扣除到期服务器的月费
     * 每个服务器按购买日期独立计费，每30天扣一次费
     * @return 本次扣费总金额
     */
    fun checkAndBillServers(
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Long {
        android.util.Log.d("ServerBilling", "========== 开始检查服务器扣费 ==========")
        android.util.Log.d("ServerBilling", "当前日期: ${currentYear}年${currentMonth}月${currentDay}日")
        android.util.Log.d("ServerBilling", "服务器总数: ${gameServerMap.size}, 详情: ${gameServerMap.keys}")
        
        var totalBillingCost = 0L
        val updatedGameServerMap = mutableMapOf<String, GameServerInfo>()
        
        gameServerMap.entries.forEach { (gameId, serverInfo) ->
            android.util.Log.d("ServerBilling", "检查游戏/池: $gameId, 服务器数量: ${serverInfo.servers.size}")
            
            val updatedServers = serverInfo.servers.map { server ->
                if (server.isActive) {
                    // 计算距离上次扣费的天数
                    val daysSinceLastBilling = calculateDaysSinceLaunch(
                        releaseYear = server.lastBillingYear,
                        releaseMonth = server.lastBillingMonth,
                        releaseDay = server.lastBillingDay,
                        currentYear = currentYear,
                        currentMonth = currentMonth,
                        currentDay = currentDay
                    )
                    
                    android.util.Log.d("ServerBilling", 
                        "服务器${server.id} - 类型:${server.type.displayName}, " +
                        "上次扣费:${server.lastBillingYear}年${server.lastBillingMonth}月${server.lastBillingDay}日, " +
                        "当前:${currentYear}年${currentMonth}月${currentDay}日, " +
                        "距离天数:${daysSinceLastBilling}天(包含租用当天=${daysSinceLastBilling + 1}天), 月费:¥${server.type.cost}"
                    )
                    
                    // 如果已经过了29天（即第30天，包含租用当天），扣费
                    // 例如：1月3日租用，2月2日扣费（间隔29天，包含租用日共30天）
                    if (daysSinceLastBilling >= 29) {
                        totalBillingCost += server.type.cost
                        android.util.Log.d("ServerBilling", 
                            "✓ 扣费成功！服务器${server.id} - ¥${server.type.cost}, 累计:¥${totalBillingCost}"
                        )
                        // 更新扣费日期
                        server.copy(
                            lastBillingYear = currentYear,
                            lastBillingMonth = currentMonth,
                            lastBillingDay = currentDay
                        )
                    } else {
                        android.util.Log.d("ServerBilling", 
                            "○ 未到扣费周期，服务器${server.id} - 还需${29 - daysSinceLastBilling}天"
                        )
                        server
                    }
                } else {
                    android.util.Log.d("ServerBilling", "服务器${server.id} - 已停用，跳过")
                    server
                }
            }
            updatedGameServerMap[gameId] = serverInfo.copy(servers = updatedServers)
        }
        
        // 更新服务器数据
        gameServerMap.clear()
        gameServerMap.putAll(updatedGameServerMap)
        if (totalBillingCost > 0) {
            saveServerData()
        }
        
        android.util.Log.d("ServerBilling", "========== 扣费检查完成 ==========")
        android.util.Log.d("ServerBilling", "本次扣费总额: ¥$totalBillingCost")
        
        return totalBillingCost
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
     * 计算玩家兴趣值衰减（每3个月衰减一次）
     * @param currentInterest 当前兴趣值
     * @param lifecycleProgress 生命周期进度 (0-100%)
     * @return 衰减后的兴趣值
     */
    fun calculateInterestDecay(currentInterest: Double, lifecycleProgress: Double): Double {
        // 根据生命周期阶段确定衰减率（每90天衰减一次）
        val decayRate = when {
            lifecycleProgress < 30.0 -> 8.0   // 成长期：每3个月衰减8%
            lifecycleProgress < 70.0 -> 15.0  // 成熟期：每3个月衰减15%
            lifecycleProgress < 90.0 -> 25.0  // 衰退期：每3个月衰减25%
            else -> 35.0                      // 末期：每3个月衰减35%
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
     * 计算游戏上线天数
     * @param releaseYear 上线年份
     * @param releaseMonth 上线月份
     * @param releaseDay 上线日期
     * @param currentYear 当前年份
     * @param currentMonth 当前月份
     * @param currentDay 当前日期
     * @return 上线天数
     */
    public fun calculateDaysSinceLaunch(
        releaseYear: Int,
        releaseMonth: Int,
        releaseDay: Int,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Int {
        // 简化计算：每年365天，每月30天
        val yearDays = (currentYear - releaseYear) * 365
        val monthDays = (currentMonth - releaseMonth) * 30
        val dayDiff = currentDay - releaseDay
        
        val totalDays = yearDays + monthDays + dayDiff
        return totalDays.coerceAtLeast(0)
    }
    
    /**
     * 根据实际上线天数计算初始兴趣值（考虑自然衰减）
     * @param daysSinceLaunch 实际上线天数
     * @return 当前应有的兴趣值
     */
    private fun calculateInitialInterest(daysSinceLaunch: Int): Double {
        // 从100%开始，模拟每90天（3个月）的自然衰减
        var interest = 100.0
        val decayIntervals = daysSinceLaunch / 90  // 计算经历了多少个90天周期
        
        println("--- 计算初始兴趣值 ---")
        println("总天数: $daysSinceLaunch")
        println("衰减次数: $decayIntervals")
        
        for (interval in 1..decayIntervals) {
            val dayAtInterval = interval * 90
            val progress = calculateLifecycleProgress(dayAtInterval)
            val beforeDecay = interest
            interest = calculateInterestDecay(interest, progress)
            println("第${interval}次衰减（第${dayAtInterval}天）：${beforeDecay}% -> ${interest}%")
        }
        
        println("最终兴趣值: $interest%")
        return interest.coerceIn(0.0, 100.0)
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
        
        // 每隔90天（3个月）衰减一次兴趣值
        // 当跨越90天周期时触发衰减（不需要等到整数倍的那一天）
        val currentDecayInterval = newDaysSinceLaunch / 90
        val lastDecayInterval = gameRevenue.lastInterestDecayDay / 90
        val shouldDecay = currentDecayInterval > lastDecayInterval
        
        println("=== 每日兴趣值更新 ===")
        println("游戏ID: $gameId")
        println("上线天数: $newDaysSinceLaunch")
        println("当前衰减周期: $currentDecayInterval")
        println("上次衰减周期: $lastDecayInterval")
        println("是否触发衰减: $shouldDecay")
        println("当前兴趣值: ${gameRevenue.playerInterest}%")
        
        val newInterest = if (shouldDecay) {
            val decayed = calculateInterestDecay(gameRevenue.playerInterest, newLifecycleProgress)
            println("衰减后兴趣值: $decayed%")
            decayed
        } else {
            gameRevenue.playerInterest
        }
        
        // 更新游戏数据
        gameRevenueMap[gameId] = gameRevenue.copy(
            daysSinceLaunch = newDaysSinceLaunch,
            lifecycleProgress = newLifecycleProgress,
            playerInterest = newInterest,
            lastInterestDecayDay = if (shouldDecay) newDaysSinceLaunch else gameRevenue.lastInterestDecayDay
        )
        saveRevenueData()
    }
    
    /**
     * 根据游戏评分计算注册数加成
     * @param baseRegistrations 基础注册数
     * @param gameRating 游戏评分（0-10）
     * @return 应用加成后的注册数
     */
    private fun applyRatingBonus(baseRegistrations: Int, gameRating: Float?): Int {
        if (gameRating == null) return baseRegistrations
        
        return when {
            gameRating >= 9.5f -> {
                // 评分 >= 9.5（满分神作）：10倍（+900%）
                (baseRegistrations * 10.0).toInt()
            }
            gameRating >= 9.0f -> {
                // 评分 9.0-9.5（神作）：6倍（+500%）
                (baseRegistrations * 6.0).toInt()
            }
            gameRating >= 8.0f -> {
                // 评分 8.0-9.0（佳作）：3倍（+200%）
                (baseRegistrations * 3.0).toInt()
            }
            gameRating >= 7.0f -> {
                // 评分 7.0-8.0（优秀）：1.8倍（+80%）
                (baseRegistrations * 1.8).toInt()
            }
            gameRating >= 6.0f -> {
                // 评分 6.0-7.0（良好）：1.3倍（+30%）
                (baseRegistrations * 1.3).toInt()
            }
            gameRating >= 5.0f -> {
                // 评分 5.0-6.0（及格）：无加成
                baseRegistrations
            }
            else -> {
                // 评分 < 5.0（差评）：惩罚-30%
                (baseRegistrations * 0.7).toInt()
            }
        }
    }
    
    /**
     * 根据粉丝数量计算注册数加成（网游专用，已下调数值）
     * @param baseRegistrations 基础注册数
     * @param fanCount 粉丝数量
     * @return 应用加成后的注册数
     */
    private fun applyFansBonus(baseRegistrations: Int, fanCount: Int): Int {
        if (fanCount <= 0) return baseRegistrations
        
        // 计算粉丝加成百分比（分段递减，已下调）
        val bonusPercent = when {
            fanCount <= 10000 -> {
                // 0-10K 粉丝：每1K粉丝 +1%（最多 +10%）（原+2%/+20%）
                (fanCount / 1000) * 1.0
            }
            fanCount <= 50000 -> {
                // 10K-50K 粉丝：前10K给10%，后续每1K粉丝 +0.5%（最多额外 +20%）（原+1%/+40%）
                val base = 10.0
                val extra = ((fanCount - 10000) / 1000) * 0.5
                base + extra
            }
            fanCount <= 100000 -> {
                // 50K-100K 粉丝：前50K给30%，后续每1K粉丝 +0.3%（最多额外 +15%）（原+0.5%/+25%）
                val base = 30.0
                val extra = ((fanCount - 50000) / 1000) * 0.3
                base + extra
            }
            else -> {
                // 100K+ 粉丝：封顶在 +50%（原+85%）
                50.0
            }
        }
        
        val multiplier = 1.0 + (bonusPercent / 100.0)
        return (baseRegistrations * multiplier).toInt()
    }
    
    /**
     * 根据粉丝数量计算单机游戏销量加成（效果低于网游）
     * @param baseSales 基础销量
     * @param fanCount 粉丝数量
     * @return 应用加成后的销量
     */
    private fun applyFansBonusForSinglePlayer(baseSales: Int, fanCount: Int): Int {
        if (fanCount <= 0) return baseSales
        
        // 计算粉丝加成百分比（单机游戏加成较低，封顶30%）
        val bonusPercent = when {
            fanCount <= 10000 -> {
                // 0-10K 粉丝：每1K粉丝 +0.5%（最多 +5%）
                (fanCount / 1000) * 0.5
            }
            fanCount <= 50000 -> {
                // 10K-50K 粉丝：前10K给5%，后续每1K粉丝 +0.3%（最多额外 +12%）
                val base = 5.0
                val extra = ((fanCount - 10000) / 1000) * 0.3
                base + extra
            }
            fanCount <= 100000 -> {
                // 50K-100K 粉丝：前50K给17%，后续每1K粉丝 +0.2%（最多额外 +10%）
                val base = 17.0
                val extra = ((fanCount - 50000) / 1000) * 0.2
                base + extra
            }
            else -> {
                // 100K+ 粉丝：封顶在 +30%
                30.0
            }
        }
        
        val multiplier = 1.0 + (bonusPercent / 100.0)
        return (baseSales * multiplier).toInt()
    }
    
    /**
     * 获取当前活跃玩家数（考虑兴趣值影响）
     */
    fun getActivePlayers(gameId: String): Long {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0L
        
        // 检查是否为网络游戏
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        if (businessModel != com.example.yjcy.ui.BusinessModel.ONLINE_GAME) return 0L
        
        // 基础活跃玩家 = 总注册人数 * 40%（注册玩家中40%保持活跃）
        val baseActivePlayers = (gameRevenue.totalRegisteredPlayers * 0.4).toLong()
        
        // 根据兴趣值调整活跃玩家数
        val interestMultiplier = calculateActivePlayerMultiplier(gameRevenue.playerInterest)
        
        return (baseActivePlayers * interestMultiplier).toLong()
    }
    
    /**
     * 导出所有服务器数据（用于存档）
     */
    fun exportServerData(): Map<String, GameServerInfo> {
        return gameServerMap.toMap()
    }
    
    /**
     * 导入服务器数据（用于读档）
     * 注意：这会覆盖当前所有服务器数据，并同步到SharedPreferences
     */
    fun importServerData(serverData: Map<String, GameServerInfo>) {
        android.util.Log.d("RevenueManager", "===== 开始导入服务器数据 =====")
        android.util.Log.d("RevenueManager", "导入前gameServerMap大小: ${gameServerMap.size}")
        
        gameServerMap.clear()
        gameServerMap.putAll(serverData)
        
        android.util.Log.d("RevenueManager", "导入后gameServerMap大小: ${gameServerMap.size}")
        android.util.Log.d("RevenueManager", "导入服务器数据: ${serverData.size} 个游戏")
        serverData.forEach { (gameId, info) ->
            android.util.Log.d("RevenueManager", "  - 游戏 $gameId: ${info.servers.size} 台服务器")
            info.servers.forEach { server ->
                android.util.Log.d("RevenueManager", "    * ${server.type.displayName}, 购买于 ${server.purchaseYear}年${server.purchaseMonth}月${server.purchaseDay}日")
            }
        }
        
        // 立即同步到SharedPreferences，确保数据持久化
        saveServerData()
        android.util.Log.d("RevenueManager", "===== 服务器数据已同步到SharedPreferences =====")
        
        // 验证数据已正确保存
        logCurrentServerState()
    }
    
    /**
     * 调试：记录当前服务器状态
     */
    fun logCurrentServerState() {
        android.util.Log.d("RevenueManager", "========== 当前服务器状态 ==========")
        android.util.Log.d("RevenueManager", "gameServerMap总大小: ${gameServerMap.size}")
        gameServerMap.forEach { (gameId, info) ->
            android.util.Log.d("RevenueManager", "  [$gameId]: ${info.servers.size} 台服务器")
            info.servers.forEach { server ->
                android.util.Log.d("RevenueManager", "    - ${server.type.displayName} (${server.id})")
            }
        }
        android.util.Log.d("RevenueManager", "====================================")
    }
    
    /**
     * 导出所有收益数据（用于存档）
     */
    fun exportRevenueData(): Map<String, GameRevenue> {
        return gameRevenueMap.toMap()
    }
    
    /**
     * 导入收益数据（用于读档）
     * 注意：这会覆盖当前所有收益数据，并同步到SharedPreferences
     */
    fun importRevenueData(revenueData: Map<String, GameRevenue>) {
        android.util.Log.d("RevenueManager", "===== 开始导入收益数据 =====")
        android.util.Log.d("RevenueManager", "导入前gameRevenueMap大小: ${gameRevenueMap.size}")
        
        gameRevenueMap.clear()
        gameRevenueMap.putAll(revenueData)
        
        android.util.Log.d("RevenueManager", "导入后gameRevenueMap大小: ${gameRevenueMap.size}")
        android.util.Log.d("RevenueManager", "导入收益数据: ${revenueData.size} 个游戏")
        revenueData.forEach { (gameId, revenue) ->
            android.util.Log.d("RevenueManager", "  - 游戏 $gameId (${revenue.gameName})")
            android.util.Log.d("RevenueManager", "    * 总收益: ${revenue.getTotalRevenue()}")
            android.util.Log.d("RevenueManager", "    * 销售天数: ${revenue.dailySalesList.size}")
            android.util.Log.d("RevenueManager", "    * 更新次数: ${revenue.updateCount}")
            if (revenue.totalRegisteredPlayers > 0) {
                android.util.Log.d("RevenueManager", "    * 总注册: ${revenue.totalRegisteredPlayers}, 兴趣值: ${revenue.playerInterest}%")
            }
        }
        
        // 立即同步到SharedPreferences，确保数据持久化
        saveRevenueData()
        android.util.Log.d("RevenueManager", "===== 收益数据已同步到SharedPreferences =====")
    }
    
}