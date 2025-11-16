package com.example.yjcy.data

import java.util.*
import java.text.SimpleDateFormat
import kotlin.random.Random
import kotlin.math.roundToLong

// 性能优化：调试日志开关（正式环境应设为false）
private const val ENABLE_VERBOSE_LOGS = false

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
    val releaseMinuteOfDay: Int = 0, // 新增：发售时的分钟数（0-1439，0表示00:00，1439表示23:59）
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
     * 修复：优先使用statistics中保存的总收益，防止因清理dailySalesList导致历史数据丢失
     */
    fun getTotalRevenue(): Double {
        // 如果statistics存在且有总收益数据，优先使用（包含全部历史）
        return statistics?.totalRevenue ?: dailySalesList.sumOf { it.revenue }
    }
    
    /**
     * 获取总销量
     * 修复：优先使用statistics中保存的总销量，防止因清理dailySalesList导致历史数据丢失
     */
    fun getTotalSales(): Long {
        // 如果statistics存在且有总销量数据，优先使用（包含全部历史）
        return statistics?.totalSales ?: dailySalesList.sumOf { it.sales }
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
        // 修复：如果总注册人数为负数（溢出），返回0
        if (totalRegisteredPlayers < 0) {
            return 0L
        }
        
        // 基础活跃玩家 = 总注册人数 * 40%
        val baseActivePlayers = (totalRegisteredPlayers * 0.4).toLong()
        
        // 根据兴趣值计算倍数
        val interestMultiplier = when {
            playerInterest >= 70.0 -> 1.0         // 正常状态
            playerInterest >= 50.0 -> 0.7         // 小幅下降
            playerInterest >= 30.0 -> 0.4         // 大幅下降
            else -> 0.2                           // 严重下降
        }
        
        val result = (baseActivePlayers * interestMultiplier).toLong()
        // 确保结果不为负数
        return result.coerceAtLeast(0L)
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
    // 存储游戏IP信息（用于销量加成计算）
    private val gameIPMap = mutableMapOf<String, GameIP>()
    
    // SharedPreferences用于持久化
    private var sharedPreferences: android.content.SharedPreferences? = null
    
    // 性能优化：延迟批量保存机制
    private var isDirty = false  // 数据是否有变更
    private var lastSaveTime = 0L  // 上次保存时间戳
    private const val SAVE_INTERVAL_MS = 3000L  // 最小保存间隔：3秒
    private val saveHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var pendingSaveRunnable: Runnable? = null
    
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
     * 标记数据已变更，延迟保存（性能优化）
     * 使用延迟批量保存机制，避免频繁写入SharedPreferences导致UI卡顿
     */
    private fun saveRevenueData() {
        isDirty = true
        
        // 取消之前pending的保存任务
        pendingSaveRunnable?.let { saveHandler.removeCallbacks(it) }
        
        // 计算距离上次保存的时间
        val now = System.currentTimeMillis()
        val timeSinceLastSave = now - lastSaveTime
        
        // 如果距离上次保存不足SAVE_INTERVAL_MS，延迟保存
        val delay = if (timeSinceLastSave < SAVE_INTERVAL_MS) {
            SAVE_INTERVAL_MS - timeSinceLastSave
        } else {
            0L  // 立即保存
        }
        
        // 创建延迟保存任务
        pendingSaveRunnable = Runnable {
            if (isDirty) {
                performSave()
            }
        }
        saveHandler.postDelayed(pendingSaveRunnable!!, delay)
    }
    
    /**
     * 强制立即保存（用于应用退出等关键时刻）
     */
    fun forceSave() {
        // 取消pending的保存任务
        pendingSaveRunnable?.let { saveHandler.removeCallbacks(it) }
        
        if (isDirty) {
            performSave()
        }
    }
    
    /**
     * 实际执行保存到SharedPreferences的函数
     */
    private fun performSave() {
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
            editor.putInt("revenue_${index}_release_minute", revenue.releaseMinuteOfDay) // 新增：保存发售时的分钟数
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
        
        // 更新保存时间和状态
        lastSaveTime = System.currentTimeMillis()
        isDirty = false
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
                val releaseMinuteOfDay = prefs.getInt("revenue_${i}_release_minute", 0) // 新增：发售时的分钟数，默认0（00:00）
                val playerInterest = prefs.getFloat("revenue_${i}_player_interest", 100.0f).toDouble()
                val totalRegisteredPlayersRaw = prefs.getLong("revenue_${i}_total_registered", 0L)
                // 修复：如果读取到的值为负数（溢出），重置为0
                val totalRegisteredPlayers = if (totalRegisteredPlayersRaw < 0) {
                    android.util.Log.w("RevenueManager", "⚠️ 读取存档时发现totalRegisteredPlayers为负数($totalRegisteredPlayersRaw)，重置为0")
                    0L
                } else {
                    totalRegisteredPlayersRaw
                }
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
                    releaseMinuteOfDay = releaseMinuteOfDay,
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
        // 使用游戏内时间创建发售日期
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, releaseYear)
        calendar.set(Calendar.MONTH, releaseMonth - 1) // Calendar月份从0开始
        calendar.set(Calendar.DAY_OF_MONTH, releaseDay)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val releaseDate = calendar.time
        val dailySalesList = mutableListOf<DailySales>()
        
        // 生成模拟的每日销量数据
        for (i in 0 until daysOnMarket) {
            // 为每一天创建游戏内日期
            val dayCalendar = Calendar.getInstance()
            dayCalendar.time = releaseDate
            dayCalendar.add(Calendar.DAY_OF_YEAR, i)
            val date = dayCalendar.time
            
            // 模拟销量衰减：首日最高，然后逐渐下降，偶有波动
            val baseSales = when {
                i == 0 -> {
                    // 首日销量/注册，应用宣传指数提升
                    val baseValue = Random.nextInt(800, 1200)
                    // 宣传指数100%时，首发销量/注册提升10%（已大幅下调，原25%，最初50%）
                    // 低于100%也有提升，但效果没有100%多
                    val promotionBonus = 1f + (promotionIndex * 0.1f)
                    val withPromotionBonus = (baseValue * promotionBonus).toInt()
                    // 应用IP加成（如果有）
                    val ipBonus = getIPBonus(gameId)
                    if (ipBonus > 0f) {
                        (withPromotionBonus * (1f + ipBonus)).toInt()
                    } else {
                        withPromotionBonus
                    }
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
     * 计算下一次更新成本（随更新次数递增，有上限）
     * 
     * **单机游戏**（DLC/资料片性质）：
     * - 基础成本：3,000,000元
     * - 递增系数：1.25（每次+25%）
     * - **成本上限：500,000,000元（5亿）**
     * - 价格梯度：300万 → 375万 → 469万 → 586万 → 732万 → 916万 → 1145万 → ... → 5亿（上限）
     * 
     * **网络游戏**（持续运营性质）：
     * - 基础成本：5,000,000元
     * - 递增系数：1.25（每次+25%）
     * - **成本上限：500,000,000元（5亿）**
     * - 价格梯度：500万 → 625万 → 781万 → 977万 → 1221万 → 1526万 → 1907万 → ... → 5亿（上限）
     * 
     * **成本上限设计理念**：
     * 1. 高基础成本使更新成为重大投资决策
     * 2. 设置5亿上限防止成本无限膨胀
     * 3. 鼓励玩家在早期阶段多更新，后期需要权衡收益与成本
     * 4. 达到上限后保持固定成本，提供可预测的长期运营成本
     */
    fun calculateUpdateCost(gameId: String): Double {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0.0
        
        // 获取游戏商业模式
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        // 根据商业模式设置不同的基础成本，统一上限为5亿
        val base = when (businessModel) {
            com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER -> 3_000_000.0  // 单机：基础300万
            com.example.yjcy.ui.BusinessModel.ONLINE_GAME -> 5_000_000.0    // 网游：基础500万
        }
        val maxCost = 500_000_000.0  // 统一上限：5亿
        
        // 使用1.25的递增系数（每次+25%），但不超过上限
        val factor = Math.pow(1.25, gameRevenue.updateCount.toDouble())
        val cost = base * factor
        return cost.coerceAtMost(maxCost)
    }

    /**
     * 应用一次游戏更新：销量整体提高1-3%（随机），记录更新次数，并插入当日"更新热度"提升
     * 网络游戏：恢复玩家兴趣值，增加总注册人数
     */
    fun applyGameUpdate(gameId: String): Boolean {
        val current = gameRevenueMap[gameId] ?: return false
        // 随机提升1-3%
        val salesIncreaseRatio = Random.nextDouble(0.01, 0.03)
        val newMultiplier = current.cumulativeSalesMultiplier * (1.0 + salesIncreaseRatio)

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
            
            // 更新带来新玩家：增加1-3%的总注册人数（随机）
            val newPlayersRatio = Random.nextDouble(0.01, 0.03)
            val newPlayers = (current.totalRegisteredPlayers * newPlayersRatio).toLong()
            newTotalRegisteredPlayers = safeAddRegisteredPlayers(current.totalRegisteredPlayers, newPlayers)
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
        // 每个特性需要 150 进度点（提高难度，延长更新时间）
        val required = (features.size * 150).coerceAtLeast(150)
        val task = GameUpdateTask(features = features, requiredPoints = required, announcement = announcement)
        gameRevenueMap[gameId] = current.copy(updateTask = task)
        saveRevenueData()
        return task
    }

    /**
     * 计算更新任务的进度点（考虑员工技能等级）
     * 游戏更新主要需要开发技能，但也需要设计、美工、音乐等技能
     * @param employees 已分配的员工列表
     * @return 每日进度点
     */
    fun calculateUpdateProgressPoints(employees: List<com.example.yjcy.data.Employee>): Int {
        // 未分配员工时，进度条不增长
        if (employees.isEmpty()) return 0
        
        // 计算每个员工的技能效率（与游戏开发进度保持一致）
        val employeeEfficiencies = employees.map { employee ->
            // 更新任务主要依赖开发技能（60%），其次是设计（20%）、美工（15%）、音乐（5%）
            val weightedSkill = (employee.skillDevelopment * 0.6f + 
                                employee.skillDesign * 0.2f + 
                                employee.skillArt * 0.15f + 
                                employee.skillMusic * 0.05f)
            
            // 技能倍率：与游戏开发进度保持一致（1级=0.2x, 2级=0.4x, 3级=0.6x, 4级=0.8x, 5级=1.0x）
            when {
                weightedSkill >= 5f -> 1.0f   // 5级：大幅提升
                weightedSkill >= 4f -> 0.8f   // 4级：提升
                weightedSkill >= 3f -> 0.6f   // 3级：提升
                weightedSkill >= 2f -> 0.4f   // 2级：提升
                else -> 0.2f                  // 1级：提升
            }
        }
        
        // 计算平均效率
        val avgEfficiency = employeeEfficiencies.average().toFloat()
        
        // 基础进度：每人每天8点（降低更新速度）
        val basePointsPerEmployee = 8
        
        // 人数倍率：与游戏开发进度保持一致（每人+0.5倍率，最高10人封顶6.0倍）
        // 1人=1.0x, 2人=1.5x, 3人=2.0x, 4人=2.5x, 5人=3.0x, 6人=3.5x, ..., 10人=5.5x
        val countMultiplier = (1.0f + (employees.size - 1) * 0.5f).coerceAtMost(6.0f)
        
        // 计算总进度点 = 基础点数 × 平均效率 × 人数倍率
        // 确保至少产生1点进度，避免进度完全停滞
        val totalPoints = (employees.size * basePointsPerEmployee * avgEfficiency * countMultiplier).toInt().coerceAtLeast(1)
        
        return totalPoints
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
     * 低评分游戏销量惩罚大幅加强，确保烂作几乎卖不出去
     */
    private fun calculateRatingMultiplier(rating: Float): Float {
        return when {
            rating >= 9.0f -> 1.3f  // 杰作（9-10分）：1.3倍（原1.8倍）
            rating >= 8.0f -> {
                // 优秀（8-9分）：线性插值 1.15 到 1.3（原1.3-1.8）
                1.15f + ((rating - 8.0f) / 1.0f) * 0.15f
            }
            rating >= 7.0f -> {
                // 良好（7-8分）：线性插值 1.0 到 1.15（原1.0-1.3）
                1.0f + ((rating - 7.0f) / 1.0f) * 0.15f
            }
            rating >= 5.0f -> {
                // 及格（5-7分）：线性插值 0.4 到 1.0（保持不变）
                0.4f + ((rating - 5.0f) / 2.0f) * 0.6f
            }
            rating >= 3.0f -> {
                // 差评（3-5分）：线性插值 0.1 到 0.4（保持不变）
                0.1f + ((rating - 3.0f) / 2.0f) * 0.3f
            }
            else -> {
                // 烂作（0-3分）：线性插值 0.01 到 0.1（保持不变）
                0.01f + (rating / 3.0f) * 0.09f
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
     * 调整低评分游戏的历史销量（用于旧存档兼容）
     * 对于评分<3分的游戏，重新计算历史销量并应用新的限制规则
     * 采用激进策略：设置总销量上限，按衰减模式重新分配每日销量
     * @param gameId 游戏ID
     * @param rating 游戏评分
     * @param releasePrice 发售价格
     * @return 是否进行了调整
     */
    fun adjustLowRatingGameSales(gameId: String, rating: Float, releasePrice: Double): Boolean {
        val gameRevenue = gameRevenueMap[gameId] ?: run {
            android.util.Log.w("RevenueManager", "调整失败：游戏 $gameId 没有收益数据")
            return false
        }
        
        // 只处理低评分游戏（<3分）
        if (rating >= 3.0f) {
            android.util.Log.d("RevenueManager", "跳过：游戏 $gameId 评分 ${rating} >= 3.0")
            return false
        }
        
        // 检查是否为单机游戏（只有单机游戏需要调整销量上限）
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        if (businessModel != com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER) {
            android.util.Log.d("RevenueManager", "跳过：游戏 $gameId 是网络游戏，不需要调整")
            return false // 网络游戏使用注册数，不需要调整
        }
        
        // 设置低评分游戏的严格销量上限（固定总销量上限，不管卖了多少天）
        val (maxDailySales, maxTotalSales) = when {
            rating < 1.0f -> 5L to 50L   // 0-1分：每天最多5份，总销量最多50份
            rating < 2.0f -> 20L to 200L  // 1-2分：每天最多20份，总销量最多200份
            else -> 50L to 500L            // 2-3分：每天最多50份，总销量最多500份
        }
        
        val currentTotalSales = gameRevenue.dailySalesList.sumOf { it.sales }
        android.util.Log.d("RevenueManager", "开始调整游戏 $gameId (${rating}分): 当前总销量=$currentTotalSales, 上限=$maxTotalSales, 天数=${gameRevenue.dailySalesList.size}")
        
        // 强制重新分配所有低评分游戏的销量，确保总销量不超过上限（无论当前是否超过）
        // 这样可以确保旧存档立即生效
            // 重新计算首日销量（基于评分倍率）
            val newMultiplier = calculateRatingMultiplier(rating)
            val baseSalesForPrice = when {
                releasePrice <= 30.0 -> 500 // 低价游戏基础值
                releasePrice <= 100.0 -> 300 // 中价游戏基础值
                else -> 100 // 高价游戏基础值
            }
            val firstDaySales = minOf(
                (baseSalesForPrice * newMultiplier).toInt(),
                maxDailySales.toInt()
            )
            
            // 按衰减模式重新生成每日销量（衰减因子0.98），确保总销量不超过上限
            val dayCount = gameRevenue.dailySalesList.size
            val updatedDailySales = mutableListOf<DailySales>()
            var remainingTotal = maxTotalSales
            var currentDaySales = firstDaySales.toLong()
            
            gameRevenue.dailySalesList.forEachIndexed { index, originalDailySales ->
                if (remainingTotal <= 0) {
                    // 剩余销量已用完，后续都是0
                    updatedDailySales.add(originalDailySales.copy(
                        sales = 0L,
                        revenue = 0.0
                    ))
                } else {
                    // 计算当日销量（考虑衰减）
                    val decayFactor = 0.98
                    val fluctuation = 0.9 + (index % 5) * 0.1 // 简单的波动模拟
                    val daySales = (currentDaySales * decayFactor * fluctuation).toInt().coerceAtLeast(1)
                    
                    // 最后一天，将剩余销量全部分配（确保总销量精确等于上限）
                    val finalDaySales = if (index == gameRevenue.dailySalesList.size - 1) {
                        remainingTotal // 最后一天，分配所有剩余销量
                    } else {
                        minOf(daySales.toLong(), maxDailySales, remainingTotal)
                    }
                    
                    updatedDailySales.add(originalDailySales.copy(
                        sales = finalDaySales,
                        revenue = finalDaySales * releasePrice
                    ))
                    
                    currentDaySales = finalDaySales
                    remainingTotal -= finalDaySales
                }
            }
            
            // 验证：确保总销量不超过上限
            val actualTotal = updatedDailySales.sumOf { it.sales }
            val finalDailySales = if (actualTotal > maxTotalSales) {
                // 如果还是超过了（理论上不应该发生），强制截断
                val ratio = maxTotalSales.toDouble() / actualTotal
                updatedDailySales.map { dailySales ->
                    val correctedSales = (dailySales.sales * ratio).toLong()
                    dailySales.copy(
                        sales = correctedSales,
                        revenue = correctedSales * releasePrice
                    )
                }
            } else {
                updatedDailySales
            }
            
            // 更新游戏收益数据
            gameRevenueMap[gameId] = gameRevenue.copy(
                dailySalesList = finalDailySales
            )
            saveRevenueData()
            
            android.util.Log.d("RevenueManager", "调整低评分游戏 $gameId (${rating}分): 原总销量=$currentTotalSales, 新总销量=${finalDailySales.sumOf { it.sales }}, 上限=$maxTotalSales (重新分配)")
            
            return true
    }
    
    /**
     * 更新游戏信息（商业模式和付费内容）
     */
    fun updateGameInfo(gameId: String, businessModel: com.example.yjcy.ui.BusinessModel, monetizationItems: List<MonetizationItem>) {
        gameInfoMap[gameId] = Pair(businessModel, monetizationItems)
    }
    
    /**
     * 更新游戏IP信息（用于销量加成）
     */
    fun updateGameIP(gameId: String, gameIP: GameIP?) {
        if (gameIP != null) {
            gameIPMap[gameId] = gameIP
        } else {
            gameIPMap.remove(gameId)
        }
    }
    
    /**
     * 获取游戏的IP加成（如果游戏使用了IP）
     */
    fun getIPBonus(gameId: String): Float {
        return gameIPMap[gameId]?.calculateIPBonus() ?: 0f
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
        fanCount: Long = 0L,
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
            // 使用游戏内时间创建 Date 对象，而不是系统时间
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, currentGameRevenue.releaseYear)
            calendar.set(Calendar.MONTH, currentGameRevenue.releaseMonth - 1) // Calendar月份从0开始
            calendar.set(Calendar.DAY_OF_MONTH, currentGameRevenue.releaseDay)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val firstDayDate = calendar.time
            
            // 首日销量/注册：根据商业模式调整
            val baseSales = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
                // 网游：首日注册数根据评分调整，10分游戏大幅提升初始玩家数
                val baseRegistrations = when {
                    gameRating != null && gameRating >= 10.0f -> Random.nextInt(3000, 5000)  // 10.0分：3000-5000人（满分神作）
                    gameRating != null && gameRating >= 9.5f -> Random.nextInt(2500, 4000)   // 9.5-10.0分：2500-4000人（接近满分）
                    gameRating != null && gameRating >= 9.0f -> Random.nextInt(2000, 3500)   // 9.0-9.5分：2000-3500人（神作）
                    gameRating != null && gameRating >= 8.5f -> Random.nextInt(1500, 2500)  // 8.5-9.0分：1500-2500人
                    gameRating != null && gameRating >= 8.0f -> Random.nextInt(1200, 2000)  // 8.0-8.5分：1200-2000人
                    gameRating != null && gameRating >= 7.0f -> Random.nextInt(800, 1500)   // 7.0-8.0分：800-1500人
                    else -> Random.nextInt(500, 1000)                                       // 7.0分以下：500-1000人
                }
                // 根据游戏评分添加加成
                val withRatingBonus = applyRatingBonus(baseRegistrations, gameRating)
                // 根据粉丝数量添加加成
                val withFansBonus = applyFansBonus(withRatingBonus, fanCount)
                // 根据IP添加加成
                val ipBonus = getIPBonus(gameId)
                val withIPBonus = if (ipBonus > 0f) {
                    (withFansBonus * (1f + ipBonus)).toInt()
                } else {
                    withFansBonus
                }
                // 根据声望添加加成（初期注册提升）
                (withIPBonus * (1f + reputationBonus)).toInt()
            } else {
                // 单机：根据价格和评分调整首日销量（高评分游戏首日销量更高）
                val baseSalesForPrice = when {
                    currentGameRevenue.releasePrice <= 30.0 -> {
                        // 低价游戏：根据评分调整基础值
                        when {
                            gameRating != null && gameRating >= 9.0f -> Random.nextInt(10000, 20000) // 9分以上：首日10000-20000份
                            gameRating != null && gameRating >= 8.0f -> Random.nextInt(5000, 10000) // 8-9分：首日5000-10000份
                            gameRating != null && gameRating >= 7.0f -> Random.nextInt(2000, 5000)  // 7-8分：首日2000-5000份
                            gameRating != null && gameRating >= 6.0f -> Random.nextInt(800, 2000) // 6-7分：首日800-2000份
                            else -> Random.nextInt(200, 800) // 其他：首日200-800份
                        }
                    }
                    currentGameRevenue.releasePrice <= 100.0 -> {
                        // 中价游戏：根据评分调整基础值
                        when {
                            gameRating != null && gameRating >= 9.0f -> Random.nextInt(5000, 10000) // 9分以上：首日5000-10000份
                            gameRating != null && gameRating >= 8.0f -> Random.nextInt(2500, 5000) // 8-9分：首日2500-5000份
                            gameRating != null && gameRating >= 7.0f -> Random.nextInt(1000, 2500) // 7-8分：首日1000-2500份
                            gameRating != null && gameRating >= 6.0f -> Random.nextInt(400, 1000) // 6-7分：首日400-1000份
                            else -> Random.nextInt(100, 400) // 其他：首日100-400份
                        }
                    }
                    else -> {
                        // 高价游戏（>100元）：根据评分和价格调整基础值（价格越高销量越少，已大幅下调）
                        // 价格影响系数：大幅增强价格惩罚
                        val priceFactor = when {
                            currentGameRevenue.releasePrice <= 150.0 -> 0.6  // 100-150元：0.6倍（原0.8）
                            currentGameRevenue.releasePrice <= 200.0 -> 0.35 // 150-200元：0.35倍（原0.5）
                            currentGameRevenue.releasePrice <= 300.0 -> 0.2  // 200-300元：0.2倍（原0.3）
                            currentGameRevenue.releasePrice <= 500.0 -> 0.1  // 300-500元：0.1倍（原0.15）
                            else -> 0.05 // 500元以上：0.05倍（原0.08）
                        }
                        
                        // 大幅降低基础销量范围（高价格下即使高分销量也很低）
                        val baseRange = when {
                            gameRating != null && gameRating >= 9.0f -> Random.nextInt(80, 150)  // 9分以上：首日80-150份（原150-300）
                            gameRating != null && gameRating >= 8.0f -> Random.nextInt(60, 120) // 8-9分：首日60-120份（原100-200）
                            gameRating != null && gameRating >= 7.0f -> Random.nextInt(40, 80)  // 7-8分：首日40-80份（原80-150）
                            gameRating != null && gameRating >= 6.0f -> Random.nextInt(30, 60) // 6-7分：首日30-60份（原50-100）
                            else -> Random.nextInt(15, 30) // 其他：首日15-30份（原20-50）
                        }
                        (baseRange * priceFactor).toInt()
                    }
                }
                // 应用评分倍率（单机游戏销量受评分影响很大）
                // 但对于高价格游戏，评分倍率的效果会被价格限制
                val ratingMultiplier = if (gameRating != null) calculateRatingMultiplier(gameRating) else 1.0f
                // 高价格游戏：评分倍率效果受到价格惩罚限制（即使高分也不会大幅增加销量）
                val effectiveRatingMultiplier = if (currentGameRevenue.releasePrice > 100.0) {
                    // 价格越高，评分倍率的效果越弱
                    val pricePenaltyOnRating = when {
                        currentGameRevenue.releasePrice <= 200.0 -> 0.8f  // 100-200元：评分倍率效果打8折
                        currentGameRevenue.releasePrice <= 300.0 -> 0.6f  // 200-300元：评分倍率效果打6折
                        currentGameRevenue.releasePrice <= 500.0 -> 0.4f  // 300-500元：评分倍率效果打4折
                        else -> 0.3f // 500元以上：评分倍率效果打3折
                    }
                    // 基础倍率1.0，加上受限的评分加成
                    1.0f + (ratingMultiplier - 1.0f) * pricePenaltyOnRating
                } else {
                    ratingMultiplier
                }
                var withRatingMultiplier = (baseSalesForPrice * effectiveRatingMultiplier).toInt()
                
                // 设置保底销量（即使评分很低，每天至少卖几份，更符合现实）
                // 只对已评分的游戏应用保底，未评分游戏保持原始计算值
                if (gameRating != null) {
                    val minSalesForRating = when {
                        gameRating >= 6.0f -> 0 // 6分以上不设保底
                        gameRating >= 5.0f -> 3 // 5-6分：至少3份/天
                        gameRating >= 4.0f -> 5 // 4-5分：至少5份/天
                        gameRating >= 3.0f -> 8 // 3-4分：至少8份/天
                        else -> 10 // 3分以下：至少10份/天（保底更高，因为基础值已经很低）
                    }
                    withRatingMultiplier = maxOf(withRatingMultiplier, minSalesForRating)
                }
                
                // 对于低评分游戏（<3分），大幅限制其他加成效果，避免烂作还能大卖
                val isLowRating = gameRating != null && gameRating < 3.0f
                
                // 单机游戏也应用粉丝加成（效果低于网游），低评分游戏受限
                val singlePlayerSales = if (isLowRating) {
                    // 低评分游戏：直接应用粉丝加成，但限制效果（最多只有正常效果的10%）
                    val baseForFansCalc = 100
                    val fansBonusBase = applyFansBonusForSinglePlayer(baseForFansCalc, fanCount)
                    val normalMultiplier = fansBonusBase / baseForFansCalc.toFloat()
                    val reducedMultiplier = 1.0f + (normalMultiplier - 1.0f) * 0.1f // 只保留10%的加成效果
                    val withFansBonus = (withRatingMultiplier * minOf(1.1f, reducedMultiplier)).toInt()
                    
                    // 后续IP和声望加成继续应用（但会有上限限制）
                    val ipBonus = getIPBonus(gameId)
                    val ipBonusMultiplier = if (ipBonus > 0f) {
                        minOf(1.05f, 1.0f + ipBonus * 0.1f) // 最多5%
                    } else {
                        1.0f
                    }
                    val withIPBonus = (withFansBonus * ipBonusMultiplier).toInt()
                    
                    val reputationMultiplier = minOf(1.03f, 1.0f + reputationBonus * 0.15f) // 最多3%
                    val finalSales = (withIPBonus * reputationMultiplier).toInt()
                    
                    // 设置绝对上限
                    val maxSalesForLowRating = when {
                        gameRating!! < 1.0f -> 5
                        gameRating < 2.0f -> 20
                        else -> 50
                    }
                    minOf(finalSales, maxSalesForLowRating)
                } else {
                    // 正常评分游戏：正常应用所有加成
                    val withFansBonus = applyFansBonusForSinglePlayer(withRatingMultiplier, fanCount)
                    val ipBonus = getIPBonus(gameId)
                    val withIPBonus = if (ipBonus > 0f) {
                        (withFansBonus * (1f + ipBonus)).toInt()
                    } else {
                        withFansBonus
                    }
                    (withIPBonus * (1f + reputationBonus)).toInt()
                }
                singlePlayerSales
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
        // 使用游戏内时间创建当天的 Date 对象
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth - 1) // Calendar月份从0开始
        calendar.set(Calendar.DAY_OF_MONTH, currentDay)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val newDate = calendar.time
        
        // 检查当天是否已经计算过销量（防止游戏速度快时重复计算）
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDateStr = dateFormat.format(newDate)
        val alreadyCalculated = currentGameRevenue.dailySalesList.any { dailySales ->
            dateFormat.format(dailySales.date) == currentDateStr
        }
        
        if (alreadyCalculated) {
            // 当天已经计算过，返回0避免重复累加资金
            // 收益已经在第一次计算时累加到资金中了
            return 0.0
        }
        
        // 计算新增销量/注册
        val newSales = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            // 网游：根据评分动态调整基础增长率，10分游戏增长更快
            val baseGrowthRate = when {
                gameRating != null && gameRating >= 10.0f -> 0.08  // 10.0分：8%增长（满分神作，快速传播）
                gameRating != null && gameRating >= 9.5f -> 0.07   // 9.5-10.0分：7%增长（接近满分）
                gameRating != null && gameRating >= 9.0f -> 0.06  // 9.0-9.5分：6%增长（神作）
                gameRating != null && gameRating >= 8.5f -> 0.05   // 8.5-9.0分：5%增长
                gameRating != null && gameRating >= 8.0f -> 0.04   // 8.0-8.5分：4%增长
                gameRating != null && gameRating >= 7.0f -> 0.03   // 7.0-8.0分：3%增长
                else -> 0.02                                       // 7.0分以下：2%增长
            }
            
            // 基础新增注册：前一天注册数 × 增长率
            val baseNewRegistrations = (latestSales.sales * baseGrowthRate).toInt()
            
            // 兴趣值影响（高兴趣值增加增长，低兴趣值降低增长）
            val interestMultiplier = when {
                currentGameRevenue.playerInterest >= 80.0 -> 1.15  // 高兴趣：+15%（原+20%）
                currentGameRevenue.playerInterest >= 70.0 -> 1.0    // 正常
                currentGameRevenue.playerInterest >= 50.0 -> 0.85   // 下降：-15%（原-20%）
                currentGameRevenue.playerInterest >= 30.0 -> 0.7    // 大幅下降：-30%（原-40%）
                else -> 0.5                                        // 严重下降：-50%（原-60%）
            }
            
            // 随机波动（缩小波动范围，让增长更稳定）
            val fluctuation = Random.nextDouble(0.9, 1.2) // 缩小波动范围（原0.9-1.3）
            
            // 计算基础增长（不再应用评分加成，避免重复加成）
            val registrationsBeforeBonus = (baseNewRegistrations * interestMultiplier * fluctuation).toInt().coerceAtLeast(10)
            
            // 仅根据粉丝数量添加加成（不再应用评分加成，避免重复加成）
            val withFansBonus = applyFansBonus(registrationsBeforeBonus, fanCount)
            
            // 设置每日新增上限（防止数值爆炸，10分游戏上限更高）
            val maxDailyNewRegistrations = when {
                gameRating != null && gameRating >= 10.0f -> 10000  // 10.0分：每天最多新增10000人（满分神作）
                gameRating != null && gameRating >= 9.5f -> 8000    // 9.5-10.0分：每天最多新增8000人
                gameRating != null && gameRating >= 9.0f -> 6000    // 9.0-9.5分：每天最多新增6000人
                gameRating != null && gameRating >= 8.0f -> 4000    // 8.0-9.0分：每天最多新增4000人
                gameRating != null && gameRating >= 7.0f -> 2500    // 7.0-8.0分：每天最多新增2500人
                else -> 1500                                        // 其他：每天最多新增1500人
            }
            
            minOf(withFansBonus, maxDailyNewRegistrations)
        } else {
            // 单机：销量衰减模式
            // 对于低评分游戏，检查总销量上限（大幅提高上限，避免过早停售）
            val isLowRating = gameRating != null && gameRating < 3.0f
            if (isLowRating) {
                // 计算总销量上限（大幅提高）
                val maxTotalSales = when {
                    gameRating!! < 1.0f -> 500L    // 0-1分：总销量最多500份（原50）
                    gameRating < 2.0f -> 2000L   // 1-2分：总销量最多2000份（原200）
                    else -> 5000L                // 2-3分：总销量最多5000份（原500）
                }
                
                // 检查当前总销量是否已达到上限
                val currentTotalSales = currentGameRevenue.dailySalesList.sumOf { it.sales }
                if (currentTotalSales >= maxTotalSales) {
                    // 已达到上限，不再产生销量
                    return 0.0
                }
                
                // 计算单日销量上限
                val maxDailySales = when {
                    gameRating < 1.0f -> 5L    // 0-1分：每天最多5份
                    gameRating < 2.0f -> 20L   // 1-2分：每天最多20份
                    else -> 50L                // 2-3分：每天最多50份
                }
                
                // 销量衰减模式
                val decayFactor = 0.98 // 每天衰减2%
                val fluctuation = Random.nextDouble(0.8, 1.2) // 随机波动
                val baseNewSales = (latestSales.sales * decayFactor * fluctuation).toInt().coerceAtLeast(1)
                
                // 应用单日上限和总销量上限
                val remainingQuota = maxTotalSales - currentTotalSales
                minOf(baseNewSales.toLong(), maxDailySales, remainingQuota).toInt()
            } else {
                // 正常评分游戏：销量逐渐衰减（单机游戏通常首日最高，之后逐渐下降）
                // 根据评分计算衰减率（高评分游戏衰减更慢，但已加快衰减速度）
                val decayFactor = when {
                    gameRating != null && gameRating >= 9.0f -> 0.95f // 9.0+分：每天衰减5%（原2%，加快衰减）
                    gameRating != null && gameRating >= 8.0f -> 0.94f // 8.0-9.0分：每天衰减6%（原4%，加快衰减）
                    gameRating != null && gameRating >= 7.0f -> 0.92f // 7.0-8.0分：每天衰减8%（原6%，加快衰减）
                    gameRating != null && gameRating >= 6.0f -> 0.90f // 6.0-7.0分：每天衰减10%（原8%，加快衰减）
                    gameRating != null && gameRating >= 5.0f -> 0.88f // 5.0-6.0分：每天衰减12%（原10%，加快衰减）
                    gameRating != null && gameRating >= 4.0f -> 0.85f // 4.0-5.0分：每天衰减15%（原12%，加快衰减）
                    else -> 0.80f // 4.0分以下：每天衰减20%（原15%，加快衰减）
                }
                val fluctuation = Random.nextDouble(0.9, 1.1) // 随机波动
                var newSales = (latestSales.sales * decayFactor * fluctuation).toInt().coerceAtLeast(1)
                
                // 设置保底销量（即使评分很低，每天至少卖几份）
                // 只对已评分的游戏应用保底
                if (gameRating != null) {
                    val minDailySales = when {
                        gameRating >= 6.0f -> 0 // 6分以上不设保底
                        gameRating >= 5.0f -> 3 // 5-6分：至少3份/天
                        gameRating >= 4.0f -> 5 // 4-5分：至少5份/天
                        gameRating >= 3.0f -> 8 // 3-4分：至少8份/天
                        else -> 10 // 3分以下：至少10份/天
                    }
                    newSales = maxOf(newSales, minDailySales)
                }
                
                // 设置合理上限（防止销量过高）
                val maxDailySales = when {
                    gameRating != null && gameRating >= 9.0f -> 5000 // 高评分：每天最多5000份（原50000）
                    gameRating != null && gameRating >= 8.0f -> 3000  // 中高评分：每天最多3000份（原30000）
                    gameRating != null && gameRating >= 7.0f -> 1500  // 中等评分：每天最多1500份（原10000）
                    else -> 500 // 低评分：每天最多500份（原2000）
                }
                minOf(newSales, maxDailySales)
            }
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
            safeAddRegisteredPlayers(currentGameRevenue.totalRegisteredPlayers, newSales.toLong())
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
     * 为已发售游戏添加每分钟的销量增量（实时更新）
     * 将一天的销量分散到每分钟更新，实现实时销量增长
     * @param gameId 游戏ID
     * @param gameRating 游戏评分（0-10）
     * @param fanCount 公司粉丝数
     * @param currentYear 当前游戏内年份
     * @param currentMonth 当前游戏内月份
     * @param currentDay 当前游戏内日期
     * @param currentMinuteOfDay 当前游戏内分钟数（0-1439）
     * @param reputationBonus 声望加成
     * @return 本分钟的收益增量
     */
    fun addMinuteRevenueForGame(
        gameId: String,
        gameRating: Float? = null,
        fanCount: Long = 0L,
        currentYear: Int = 1,
        currentMonth: Int = 1,
        currentDay: Int = 1,
        currentMinuteOfDay: Int = 0,
        reputationBonus: Float = 0f
    ): Double {
        val gameRevenue = gameRevenueMap[gameId] ?: return 0.0
        var currentGameRevenue = gameRevenue
        
        // 如果游戏已下架，则不产生收益
        if (!currentGameRevenue.isActive) return 0.0
        
        // 检查是否为网络游戏
        val (businessModel, _) = gameInfoMap[gameId] 
            ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
        
        // 使用游戏内时间创建当天的 Date 对象
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, currentDay)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayDate = calendar.time
        
        // 查找或创建今天的销量记录
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDateStr = dateFormat.format(todayDate)
        val todaySales = currentGameRevenue.dailySalesList.find { dailySales ->
            dateFormat.format(dailySales.date) == todayDateStr
        }
        
        // 关键修复：如果今天是发售日且releaseMinuteOfDay还是0（未设置），记录当前发售时间
        val isReleaseDay = (currentYear == currentGameRevenue.releaseYear && 
                           currentMonth == currentGameRevenue.releaseMonth && 
                           currentDay == currentGameRevenue.releaseDay)
        if (isReleaseDay && currentGameRevenue.releaseMinuteOfDay == 0 && todaySales == null) {
            // 首次创建今天的销量记录，记录发售时间
            gameRevenueMap[gameId] = currentGameRevenue.copy(
                releaseMinuteOfDay = currentMinuteOfDay
            )
            currentGameRevenue = gameRevenueMap[gameId] ?: return 0.0
            saveRevenueData()
        }
        
        // 如果今天还没有销量记录，先初始化首日销量记录（初始为0，等待实时更新）
        val baseTodaySales = if (todaySales == null) {
            // 今天是第一天，需要计算首日预期总销量
            // 但销量记录应该初始化为0，然后通过addMinuteRevenueForGame实时更新
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, currentYear)
            calendar.set(Calendar.MONTH, currentMonth - 1)
            calendar.set(Calendar.DAY_OF_MONTH, currentDay)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val firstDayDate = calendar.time
            
            // 创建初始销量记录（销量为0，等待实时更新）
            val initialSales = DailySales(
                date = firstDayDate,
                sales = 0L,
                revenue = 0.0
            )
            
            // 添加到列表中
            val updatedDailySalesList = currentGameRevenue.dailySalesList + initialSales
            gameRevenueMap[gameId] = currentGameRevenue.copy(
                dailySalesList = updatedDailySalesList
            )
            saveRevenueData()
            currentGameRevenue = gameRevenueMap[gameId] ?: return 0.0
            initialSales
        } else {
            todaySales
        }
        
        // 计算一天的预期总销量（基于昨天销量和增长率）
        // 关键修复：第一天使用addDailyRevenueForGame计算的预期总销量，而不是当前销量
        val yesterdaySales = if (currentGameRevenue.dailySalesList.size >= 2) {
            // 有至少两天的记录，获取昨天的销量（倒数第二条）
            currentGameRevenue.dailySalesList[currentGameRevenue.dailySalesList.size - 2].sales
        } else {
            // 今天是第一天，需要计算首日预期总销量
            // 调用addDailyRevenueForGame的逻辑来计算首日预期销量（但不更新dailySalesList）
            val (businessModelForCalc, _) = gameInfoMap[gameId] 
                ?: (com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER to emptyList())
            
            if (businessModelForCalc == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
                // 网游：首日注册数根据评分调整，10分游戏大幅提升
                val baseRegistrations = when {
                    gameRating != null && gameRating >= 10.0f -> Random.nextInt(3000, 5000)  // 10.0分：3000-5000人（满分神作）
                    gameRating != null && gameRating >= 9.5f -> Random.nextInt(2500, 4000)   // 9.5-10.0分：2500-4000人（接近满分）
                    gameRating != null && gameRating >= 9.0f -> Random.nextInt(2000, 3500)   // 9.0-9.5分：2000-3500人（神作）
                    gameRating != null && gameRating >= 8.5f -> Random.nextInt(1500, 2500)  // 8.5-9.0分：1500-2500人
                    gameRating != null && gameRating >= 8.0f -> Random.nextInt(1200, 2000)   // 8.0-8.5分：1200-2000人
                    gameRating != null && gameRating >= 7.0f -> Random.nextInt(800, 1500)    // 7.0-8.0分：800-1500人
                    else -> Random.nextInt(500, 1000)                                       // 7.0分以下：500-1000人
                }
                val withRatingBonus = applyRatingBonus(baseRegistrations, gameRating)
                val withFansBonus = applyFansBonus(withRatingBonus, fanCount)
                val ipBonus = getIPBonus(gameId)
                val withIPBonus = if (ipBonus > 0f) {
                    (withFansBonus * (1f + ipBonus)).toInt()
                } else {
                    withFansBonus
                }
                (withIPBonus * (1f + reputationBonus)).toInt().toLong()
            } else {
                // 单机：根据价格和评分调整首日销量（高评分游戏首日销量更高）
                val baseSalesForPrice = when {
                    currentGameRevenue.releasePrice <= 30.0 -> {
                        // 低价游戏：根据评分调整基础值
                        when {
                            gameRating != null && gameRating >= 9.0f -> Random.nextInt(10000, 20000) // 9分以上：首日10000-20000份
                            gameRating != null && gameRating >= 8.0f -> Random.nextInt(5000, 10000) // 8-9分：首日5000-10000份
                            gameRating != null && gameRating >= 7.0f -> Random.nextInt(2000, 5000)  // 7-8分：首日2000-5000份
                            gameRating != null && gameRating >= 6.0f -> Random.nextInt(800, 2000) // 6-7分：首日800-2000份
                            else -> Random.nextInt(200, 800) // 其他：首日200-800份
                        }
                    }
                    currentGameRevenue.releasePrice <= 100.0 -> {
                        // 中价游戏：根据评分调整基础值
                        when {
                            gameRating != null && gameRating >= 9.0f -> Random.nextInt(5000, 10000) // 9分以上：首日5000-10000份
                            gameRating != null && gameRating >= 8.0f -> Random.nextInt(2500, 5000) // 8-9分：首日2500-5000份
                            gameRating != null && gameRating >= 7.0f -> Random.nextInt(1000, 2500) // 7-8分：首日1000-2500份
                            gameRating != null && gameRating >= 6.0f -> Random.nextInt(400, 1000) // 6-7分：首日400-1000份
                            else -> Random.nextInt(100, 400) // 其他：首日100-400份
                        }
                    }
                    else -> {
                        // 高价游戏（>100元）：根据评分和价格调整基础值（价格越高销量越少，已大幅下调）
                        // 价格影响系数：大幅增强价格惩罚
                        val priceFactor = when {
                            currentGameRevenue.releasePrice <= 150.0 -> 0.6  // 100-150元：0.6倍（原0.8）
                            currentGameRevenue.releasePrice <= 200.0 -> 0.35 // 150-200元：0.35倍（原0.5）
                            currentGameRevenue.releasePrice <= 300.0 -> 0.2  // 200-300元：0.2倍（原0.3）
                            currentGameRevenue.releasePrice <= 500.0 -> 0.1  // 300-500元：0.1倍（原0.15）
                            else -> 0.05 // 500元以上：0.05倍（原0.08）
                        }
                        
                        // 大幅降低基础销量范围（高价格下即使高分销量也很低）
                        val baseRange = when {
                            gameRating != null && gameRating >= 9.0f -> Random.nextInt(80, 150)  // 9分以上：首日80-150份（原150-300）
                            gameRating != null && gameRating >= 8.0f -> Random.nextInt(60, 120) // 8-9分：首日60-120份（原100-200）
                            gameRating != null && gameRating >= 7.0f -> Random.nextInt(40, 80)  // 7-8分：首日40-80份（原80-150）
                            gameRating != null && gameRating >= 6.0f -> Random.nextInt(30, 60) // 6-7分：首日30-60份（原50-100）
                            else -> Random.nextInt(15, 30) // 其他：首日15-30份（原20-50）
                        }
                        (baseRange * priceFactor).toInt()
                    }
                }
                // 应用评分倍率（单机游戏销量受评分影响很大）
                // 但对于高价格游戏，评分倍率的效果会被价格限制
                val ratingMultiplier = if (gameRating != null) calculateRatingMultiplier(gameRating) else 1.0f
                // 高价格游戏：评分倍率效果受到价格惩罚限制（即使高分也不会大幅增加销量）
                val effectiveRatingMultiplier = if (currentGameRevenue.releasePrice > 100.0) {
                    // 价格越高，评分倍率的效果越弱
                    val pricePenaltyOnRating = when {
                        currentGameRevenue.releasePrice <= 200.0 -> 0.8f  // 100-200元：评分倍率效果打8折
                        currentGameRevenue.releasePrice <= 300.0 -> 0.6f  // 200-300元：评分倍率效果打6折
                        currentGameRevenue.releasePrice <= 500.0 -> 0.4f  // 300-500元：评分倍率效果打4折
                        else -> 0.3f // 500元以上：评分倍率效果打3折
                    }
                    // 基础倍率1.0，加上受限的评分加成
                    1.0f + (ratingMultiplier - 1.0f) * pricePenaltyOnRating
                } else {
                    ratingMultiplier
                }
                var withRatingMultiplier = (baseSalesForPrice * effectiveRatingMultiplier).toInt()
                
                if (gameRating != null) {
                    val minSalesForRating = when {
                        gameRating >= 6.0f -> 0
                        gameRating >= 5.0f -> 3
                        gameRating >= 4.0f -> 5
                        gameRating >= 3.0f -> 8
                        else -> 10
                    }
                    withRatingMultiplier = maxOf(withRatingMultiplier, minSalesForRating)
                }
                
                val isLowRating = gameRating != null && gameRating < 3.0f
                val singlePlayerSales = if (isLowRating) {
                    val baseForFansCalc = 100
                    val fansBonusBase = applyFansBonusForSinglePlayer(baseForFansCalc, fanCount)
                    val normalMultiplier = fansBonusBase / baseForFansCalc.toFloat()
                    val reducedMultiplier = 1.0f + (normalMultiplier - 1.0f) * 0.1f
                    val withFansBonus = (withRatingMultiplier * minOf(1.1f, reducedMultiplier)).toInt()
                    val ipBonus = getIPBonus(gameId)
                    val ipBonusMultiplier = if (ipBonus > 0f) {
                        minOf(1.05f, 1.0f + ipBonus * 0.1f)
                    } else {
                        1.0f
                    }
                    val withIPBonus = (withFansBonus * ipBonusMultiplier).toInt()
                    val reputationMultiplier = minOf(1.03f, 1.0f + reputationBonus * 0.15f)
                    val finalSales = (withIPBonus * reputationMultiplier).toInt()
                    val maxSalesForLowRating = when {
                        gameRating!! < 1.0f -> 5
                        gameRating < 2.0f -> 20
                        else -> 50
                    }
                    minOf(finalSales, maxSalesForLowRating).toLong()
                } else {
                    val withFansBonus = applyFansBonusForSinglePlayer(withRatingMultiplier, fanCount)
                    val ipBonus = getIPBonus(gameId)
                    val withIPBonus = if (ipBonus > 0f) {
                        (withFansBonus * (1f + ipBonus)).toInt()
                    } else {
                        withFansBonus
                    }
                    (withIPBonus * (1f + reputationBonus)).toInt().toLong()
                }
                singlePlayerSales
            }
        }
        
        val expectedDailySales = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            // 网游：基于注册数增长率计算
            val baseGrowthRate = when {
                gameRating != null && gameRating >= 9.0f -> 0.12
                gameRating != null && gameRating >= 8.5f -> 0.10
                gameRating != null && gameRating >= 8.0f -> 0.08
                gameRating != null && gameRating >= 7.0f -> 0.06
                else -> 0.05
            }
            (yesterdaySales * (1.0 + baseGrowthRate)).toLong()
        } else {
            // 单机：基于增长率计算
            val growthRate = when {
                gameRating != null && gameRating >= 9.0f -> 1.15f
                gameRating != null && gameRating >= 8.0f -> 1.10f
                gameRating != null && gameRating >= 7.0f -> 1.05f
                gameRating != null && gameRating >= 6.0f -> 1.02f
                gameRating != null && gameRating >= 5.0f -> 1.01f
                gameRating != null && gameRating >= 4.0f -> 1.005f
                else -> 1.0f
            }
            (yesterdaySales * growthRate).toLong()
        }
        
        // 计算每分钟的销量增量（将一天的销量分散到1440分钟）
        // 关键修复：如果今天是发售日，从发售时间开始计算进度；否则从00:00开始
        val minutesPerDay = 1440
        // 注意：isReleaseDay 已在上面声明，这里复用
        
        // 关键修复：处理发售日的进度计算
        // 对于旧存档，如果releaseMinuteOfDay==0，表示00:00发售
        val releaseMinute = if (isReleaseDay && currentGameRevenue.releaseMinuteOfDay == 0 && todaySales != null) {
            // 旧存档：发售日且已经有销量记录，但releaseMinuteOfDay未设置，假设00:00发售
            0
        } else {
            currentGameRevenue.releaseMinuteOfDay
        }
        
        val progressOfDay = if (isReleaseDay && currentMinuteOfDay >= releaseMinute) {
            // 今天是发售日，且当前时间已过发售时间：从发售时间到当天结束计算进度
            val minutesSinceRelease = currentMinuteOfDay - releaseMinute
            val minutesUntilEndOfDay = minutesPerDay - releaseMinute
            if (minutesUntilEndOfDay > 0) {
                (minutesSinceRelease.toDouble() / minutesUntilEndOfDay).coerceIn(0.0, 1.0)
            } else {
                1.0 // 如果发售时间已经是23:59，进度直接为1
            }
        } else if (isReleaseDay && currentMinuteOfDay < releaseMinute) {
            // 今天是发售日，但还没到发售时间：进度为0
            0.0
        } else {
            // 不是发售日：从00:00开始计算进度
            (currentMinuteOfDay.toDouble() / minutesPerDay).coerceIn(0.0, 1.0)
        }
        
        // 计算当前应该达到的累计销量（按时间进度平滑增长，使用Double保证精度）
        val targetSalesAtThisMinute = expectedDailySales * progressOfDay
        
        // 计算本分钟的增量（目标销量 - 当前销量）
        val currentSales = baseTodaySales.sales.toDouble()
        
        // 关键修复：允许小数增量累积更新，而不是等到整数才更新
        // 使用累加机制：每次至少更新0.01的增量，累积到一定程度再更新整数部分
        val salesIncrement = targetSalesAtThisMinute - currentSales
        
        // 如果目标销量没有增长，直接返回（避免重复更新）
        if (salesIncrement <= 0.0) {
            return 0.0
        }
        
        // 关键修复：限制单次更新的最大增量，防止暂停恢复时一次性结算大量收益
        // 如果增量过大（超过单分钟应该有的增量），说明可能是暂停恢复导致的跳变
        // 单分钟最大增量 = 每日销量 / 1440分钟，超过这个值说明时间跳变了
        val maxSingleMinuteIncrement = expectedDailySales / 1440.0
        val actualSalesIncrement = if (salesIncrement > maxSingleMinuteIncrement * 2) {
            // 如果增量超过2分钟的预期增量，说明时间跳变了，只更新单分钟增量
            // 严格限制：最多只允许2分钟的增量，防止暂停恢复时一次性结算大量收益
            android.util.Log.w("RevenueManager", "⚠️ 检测到销量增量异常（暂停恢复？）：增量=${String.format("%.2f", salesIncrement)}，每日销量=${expectedDailySales}，单分钟增量=${String.format("%.2f", maxSingleMinuteIncrement)}，限制为单分钟增量")
            maxSingleMinuteIncrement
        } else {
            salesIncrement
        }
        
        // 更新今天的销量记录：使用四舍五入，但确保只要有增长就更新
        // 关键修复：只要目标值大于当前值，就更新（即使增量很小）
        val newSales = (currentSales + actualSalesIncrement).roundToLong()
        
        // 关键修复：放宽更新条件，允许更频繁的更新
        // 只要目标值的小数部分>=0.01就更新，或者整数部分有增长就更新
        val shouldUpdate = if (actualSalesIncrement >= 0.01 || newSales > baseTodaySales.sales) {
            // 更新到目标值的整数部分（四舍五入），或者至少+1
            maxOf(baseTodaySales.sales + 1, newSales)
        } else {
            // 增量太小且整数部分没变，暂时不更新
            baseTodaySales.sales
        }
        
        // 只有当需要更新时才继续
        if (shouldUpdate <= baseTodaySales.sales) {
            return 0.0
        }
        
        val updatedTodaySales = baseTodaySales.copy(
            sales = shouldUpdate,
            revenue = shouldUpdate * currentGameRevenue.releasePrice
        )
        
        // 更新游戏收益数据
        val updatedDailySalesList = currentGameRevenue.dailySalesList.map { dailySales ->
            if (dateFormat.format(dailySales.date) == todayDateStr) {
                updatedTodaySales
            } else {
                dailySales
            }
        }
        
        // 如果是新记录，添加到列表
        val finalDailySalesList = if (updatedDailySalesList.any { dateFormat.format(it.date) == todayDateStr }) {
            updatedDailySalesList
        } else {
            updatedDailySalesList + updatedTodaySales
        }
        
        // 更新网游总注册人数
        // 注意：对于网游，销量增量实际是注册数增量
        val actualSalesIncrementForUpdate = (shouldUpdate - baseTodaySales.sales).toDouble()
        val newTotalRegisteredPlayers: Long = if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            val increment = actualSalesIncrementForUpdate.toLong()
            safeAddRegisteredPlayers(currentGameRevenue.totalRegisteredPlayers, increment)
        } else {
            currentGameRevenue.totalRegisteredPlayers
        }
        
        // 计算付费内容收益（仅网游）
        var minuteMonetizationRevenue = 0.0
        val monetizationItems = gameInfoMap[gameId]?.second ?: emptyList()
        if (businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME) {
            val totalRegistered = newTotalRegisteredPlayers.toDouble()
            val baseActivePlayers = (totalRegistered * 0.4).toLong()
            val interestMultiplier = calculateActivePlayerMultiplier(currentGameRevenue.playerInterest)
            val actualActivePlayers = (baseActivePlayers * interestMultiplier).toLong()
            
            // 添加调试日志
            android.util.Log.d("RevenueManager", "===== 付费内容收益计算 =====")
            android.util.Log.d("RevenueManager", "游戏ID: $gameId")
            android.util.Log.d("RevenueManager", "总注册: $totalRegistered, 基础活跃: $baseActivePlayers, 兴趣值倍率: $interestMultiplier, 实际活跃: $actualActivePlayers")
            android.util.Log.d("RevenueManager", "付费内容数量: ${monetizationItems.size}, 已启用: ${monetizationItems.filter { it.isEnabled && it.price != null && it.price > 0 }.size}")
            
            val minuteRevenues = calculateMonetizationRevenues(actualActivePlayers, monetizationItems)
            val dailyRevenue = minuteRevenues.sumOf { it.totalRevenue }
            minuteMonetizationRevenue = dailyRevenue / minutesPerDay // 每分钟的付费内容收益
            
            android.util.Log.d("RevenueManager", "每日付费内容收益: ¥$dailyRevenue, 每分钟收益: ¥$minuteMonetizationRevenue")
            if (minuteRevenues.isNotEmpty()) {
                minuteRevenues.forEach { revenue ->
                    android.util.Log.d("RevenueManager", "  - ${revenue.itemType}: ¥${revenue.totalRevenue}")
                }
            } else {
                android.util.Log.w("RevenueManager", "⚠️ 没有付费内容收益！")
            }
            
            // 修复：只在每天开始时累加一次付费内容收益到 monetizationRevenues
            // 检查今天是否已经累加过付费内容收益（通过检查是否有今天的dailySales记录）
            val todayDateStr = dateFormat.format(todayDate)
            val existingRevenues = currentGameRevenue.monetizationRevenues
            val updatedMonetizationRevenues = if (minuteRevenues.isNotEmpty() && 
                currentGameRevenue.dailySalesList.any { dateFormat.format(it.date) == todayDateStr && it.sales > 0 }) {
                // 只有在今天有销量记录时才累加（确保只累加一次）
                // 检查是否今天已经累加过：通过比较 monetizationRevenues 的最后更新时间
                // 简化处理：每分钟都累加每分钟收益（每天收益 / 1440）
                minuteRevenues.map { minuteRevenue ->
                    val existing = existingRevenues.find { it.itemType == minuteRevenue.itemType }
                    if (existing != null) {
                        // 累加：旧的累计收益 + 每分钟收益（每天收益 / 1440）
                        val minuteIncrement = minuteRevenue.totalRevenue / minutesPerDay
                        MonetizationRevenue(
                            itemType = minuteRevenue.itemType,
                            totalRevenue = existing.totalRevenue + minuteIncrement
                        )
                    } else {
                        // 新增的付费内容，第一次累加每分钟收益
                        MonetizationRevenue(
                            itemType = minuteRevenue.itemType,
                            totalRevenue = minuteRevenue.totalRevenue / minutesPerDay
                        )
                    }
                }
            } else {
                existingRevenues
            }
            
            // 更新游戏收益数据，包含更新后的付费内容收益
            gameRevenueMap[gameId] = currentGameRevenue.copy(
                dailySalesList = finalDailySalesList,
                totalRegisteredPlayers = newTotalRegisteredPlayers,
                monetizationRevenues = updatedMonetizationRevenues
            )
        } else {
            // 非网游，直接更新
            gameRevenueMap[gameId] = currentGameRevenue.copy(
                dailySalesList = finalDailySalesList,
                totalRegisteredPlayers = newTotalRegisteredPlayers
            )
        }
        saveRevenueData()
        
        // 返回本分钟的收益增量（使用实际的销量增量）
        val revenueIncrement = actualSalesIncrementForUpdate * currentGameRevenue.releasePrice
        return revenueIncrement + minuteMonetizationRevenue
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
        
        if (ENABLE_VERBOSE_LOGS) {
        android.util.Log.d("RevenueManager", "计算付费内容收益 - 活跃玩家: $activePlayers, 已启用付费内容数: ${enabledItems.size}")
        }
        
        if (enabledItems.isEmpty()) {
            if (ENABLE_VERBOSE_LOGS) {
            android.util.Log.w("RevenueManager", "⚠️ 没有已启用且设置了价格的付费内容！")
            }
            return emptyList()
        }
        
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
            
            if (ENABLE_VERBOSE_LOGS) {
            android.util.Log.d("RevenueManager", "  ${item.type.displayName}: 价格=¥${item.price}, 付费率=${purchaseRate * 100}%, 基础购买人数=$basebuyers, 实际购买人数=$actualBuyers, 收益=¥$revenue")
            }
            
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
     * 删除服务器（退租）
     */
    fun removeServerFromGame(gameId: String, serverId: String): Boolean {
        val serverInfo = gameServerMap[gameId] ?: return false
        val updatedServers = serverInfo.servers.filter { it.id != serverId }
        gameServerMap[gameId] = serverInfo.copy(servers = updatedServers)
        saveServerData()
        return true
    }
    
    /**
     * 检查服务器容量是否足够
     */
    fun hasEnoughServerCapacity(gameId: String, requiredCapacity: Long): Boolean {
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
        if (ENABLE_VERBOSE_LOGS) {
        android.util.Log.d("ServerBilling", "========== 开始检查服务器扣费 ==========")
        android.util.Log.d("ServerBilling", "当前日期: ${currentYear}年${currentMonth}月${currentDay}日")
        android.util.Log.d("ServerBilling", "服务器总数: ${gameServerMap.size}, 详情: ${gameServerMap.keys}")
        }
        
        var totalBillingCost = 0L
        val updatedGameServerMap = mutableMapOf<String, GameServerInfo>()
        
        gameServerMap.entries.forEach { (gameId, serverInfo) ->
            if (ENABLE_VERBOSE_LOGS) {
            android.util.Log.d("ServerBilling", "检查游戏/池: $gameId, 服务器数量: ${serverInfo.servers.size}")
            }
            
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
                    
                    if (ENABLE_VERBOSE_LOGS) {
                    android.util.Log.d("ServerBilling", 
                        "服务器${server.id} - 类型:${server.type.displayName}, " +
                        "上次扣费:${server.lastBillingYear}年${server.lastBillingMonth}月${server.lastBillingDay}日, " +
                        "当前:${currentYear}年${currentMonth}月${currentDay}日, " +
                        "距离天数:${daysSinceLastBilling}天(包含租用当天=${daysSinceLastBilling + 1}天), 月费:¥${server.type.cost}"
                    )
                    }
                    
                    // 如果已经过了29天（即第30天，包含租用当天），扣费
                    // 例如：1月3日租用，2月2日扣费（间隔29天，包含租用日共30天）
                    if (daysSinceLastBilling >= 29) {
                        totalBillingCost += server.type.cost
                        if (ENABLE_VERBOSE_LOGS) {
                        android.util.Log.d("ServerBilling", 
                            "✓ 扣费成功！服务器${server.id} - ¥${server.type.cost}, 累计:¥${totalBillingCost}"
                        )
                        }
                        // 更新扣费日期
                        server.copy(
                            lastBillingYear = currentYear,
                            lastBillingMonth = currentMonth,
                            lastBillingDay = currentDay
                        )
                    } else {
                        if (ENABLE_VERBOSE_LOGS) {
                        android.util.Log.d("ServerBilling", 
                            "○ 未到扣费周期，服务器${server.id} - 还需${29 - daysSinceLastBilling}天"
                        )
                        }
                        server
                    }
                } else {
                    if (ENABLE_VERBOSE_LOGS) {
                    android.util.Log.d("ServerBilling", "服务器${server.id} - 已停用，跳过")
                    }
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
        
        if (ENABLE_VERBOSE_LOGS) {
        android.util.Log.d("ServerBilling", "========== 扣费检查完成 ==========")
        android.util.Log.d("ServerBilling", "本次扣费总额: ¥$totalBillingCost")
        } else if (totalBillingCost > 0) {
            // 只在实际扣费时输出简要日志
            android.util.Log.d("ServerBilling", "服务器扣费: ¥$totalBillingCost")
        }
        
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
     * 安全地累加总注册人数，防止溢出
     * @param current 当前总注册人数
     * @param increment 增量
     * @return 累加后的值（已处理溢出和负数）
     */
    fun safeAddRegisteredPlayers(current: Long, increment: Long): Long {
        // 如果当前值为负数，说明已经溢出，重置为0
        if (current < 0) {
            android.util.Log.w("RevenueManager", "⚠️ 检测到totalRegisteredPlayers为负数($current)，重置为0")
            return increment.coerceAtLeast(0L)
        }
        
        // 如果增量为负数，说明计算错误，返回当前值
        if (increment < 0) {
            android.util.Log.w("RevenueManager", "⚠️ 检测到注册人数增量为负数($increment)，忽略此次更新")
            return current
        }
        
        // 检查是否会溢出（Long.MAX_VALUE = 9,223,372,036,854,775,807）
        // 限制最大值为 Long.MAX_VALUE / 2，避免接近溢出
        val maxValue = Long.MAX_VALUE / 2
        
        return when {
            current >= maxValue -> {
                android.util.Log.w("RevenueManager", "⚠️ 总注册人数已达到上限($current)，不再累加")
                current
            }
            current + increment > maxValue -> {
                android.util.Log.w("RevenueManager", "⚠️ 累加后会超过上限($current + $increment)，限制为上限")
                maxValue
            }
            else -> current + increment
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
        
        if (ENABLE_VERBOSE_LOGS) {
        println("=== 每日兴趣值更新 ===")
        println("游戏ID: $gameId")
        println("上线天数: $newDaysSinceLaunch")
        println("当前衰减周期: $currentDecayInterval")
        println("上次衰减周期: $lastDecayInterval")
        println("是否触发衰减: $shouldDecay")
        println("当前兴趣值: ${gameRevenue.playerInterest}%")
        }
        
        val newInterest = if (shouldDecay) {
            val decayed = calculateInterestDecay(gameRevenue.playerInterest, newLifecycleProgress)
            if (ENABLE_VERBOSE_LOGS) {
            println("衰减后兴趣值: $decayed%")
            }
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
     * 更新游戏收益数据（赛事完成后调用）
     * @param gameId 游戏ID
     * @param newInterest 新的兴趣值
     * @param newTotalRegistered 新的总注册人数
     */
    fun updateGameRevenueAfterTournament(
        gameId: String,
        newInterest: Double,
        newTotalRegistered: Long
    ) {
        val current = gameRevenueMap[gameId] ?: return
        
        gameRevenueMap[gameId] = current.copy(
            playerInterest = newInterest.coerceIn(0.0, 100.0),
            totalRegisteredPlayers = newTotalRegistered
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
            gameRating >= 10.0f -> {
                // 评分 = 10.0（满分神作）：3.0倍（+200%）
                (baseRegistrations * 3.0).toInt()
            }
            gameRating >= 9.5f -> {
                // 评分 >= 9.5（接近满分）：2.5倍（+150%）
                (baseRegistrations * 2.5).toInt()
            }
            gameRating >= 9.0f -> {
                // 评分 9.0-9.5（神作）：2.0倍（+100%）
                (baseRegistrations * 2.0).toInt()
            }
            gameRating >= 8.0f -> {
                // 评分 8.0-9.0（佳作）：1.5倍（+50%）（原3倍）
                (baseRegistrations * 1.5).toInt()
            }
            gameRating >= 7.0f -> {
                // 评分 7.0-8.0（优秀）：1.3倍（+30%）（原1.8倍）
                (baseRegistrations * 1.3).toInt()
            }
            gameRating >= 6.0f -> {
                // 评分 6.0-7.0（良好）：1.15倍（+15%）（原1.3倍）
                (baseRegistrations * 1.15).toInt()
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
    private fun applyFansBonus(baseRegistrations: Int, fanCount: Long): Int {
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
    private fun applyFansBonusForSinglePlayer(baseSales: Int, fanCount: Long): Int {
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
        
        // 修复：如果总注册人数为负数（溢出），返回0
        if (gameRevenue.totalRegisteredPlayers < 0) {
            android.util.Log.w("RevenueManager", "⚠️ getActivePlayers: totalRegisteredPlayers为负数(${gameRevenue.totalRegisteredPlayers})，返回0")
            return 0L
        }
        
        // 基础活跃玩家 = 总注册人数 * 40%（注册玩家中40%保持活跃）
        val baseActivePlayers = (gameRevenue.totalRegisteredPlayers * 0.4).toLong()
        
        // 根据兴趣值调整活跃玩家数
        val interestMultiplier = calculateActivePlayerMultiplier(gameRevenue.playerInterest)
        
        val result = (baseActivePlayers * interestMultiplier).toLong()
        // 确保结果不为负数
        return result.coerceAtLeast(0L)
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
        android.util.Log.d("RevenueManager", "===== 导出收益数据（用于存档） =====")
        android.util.Log.d("RevenueManager", "导出游戏数量: ${gameRevenueMap.size}")
        gameRevenueMap.forEach { (gameId, revenue) ->
            android.util.Log.d("RevenueManager", "  - 游戏 $gameId (${revenue.gameName})")
            if (revenue.totalRegisteredPlayers > 0) {
                android.util.Log.d("RevenueManager", "    总注册: ${revenue.totalRegisteredPlayers}")
            }
        }
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
        
        // 🔧 使用 forceSave() 而不是 saveRevenueData()，确保数据立即写入 SharedPreferences
        // 否则延迟保存可能导致数据丢失（玩家在数据写入前退出游戏）
        forceSave()
        android.util.Log.d("RevenueManager", "===== 收益数据已立即同步到SharedPreferences =====")
    }
    
    /**
     * 数据迁移：修复旧存档中使用系统时间的 DailySales 日期
     * 
     * 问题：旧版本代码使用系统时间创建 DailySales.date，导致与 releaseYear/releaseMonth/releaseDay（游戏内时间）无法匹配
     * 解决：根据 dailySalesList 的索引和 releaseYear/releaseMonth/releaseDay，重建每条记录的游戏内日期
     */
    fun migrateDateToGameTime() {
        android.util.Log.d("RevenueManager", "===== 开始数据迁移：修复DailySales日期 =====")
        
        var migratedCount = 0
        gameRevenueMap.forEach { (gameId, revenue) ->
            if (revenue.dailySalesList.isEmpty()) return@forEach
            
            // 检查是否需要迁移：如果首日销量的date年份与releaseYear相同，说明已经使用游戏内时间，无需迁移
            val firstDate = revenue.dailySalesList.first().date
            val firstCalendar = Calendar.getInstance()
            firstCalendar.time = firstDate
            val firstYear = firstCalendar.get(Calendar.YEAR)
            
            if (firstYear == revenue.releaseYear) {
                // 已经是游戏内时间，无需迁移
                android.util.Log.d("RevenueManager", "游戏 ${revenue.gameName} (${gameId}) 已使用游戏内时间，跳过")
                return@forEach
            }
            
            // 需要迁移：根据索引重建日期
            android.util.Log.d("RevenueManager", "游戏 ${revenue.gameName} (${gameId}) 需要迁移，共 ${revenue.dailySalesList.size} 条记录")
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, revenue.releaseYear)
            calendar.set(Calendar.MONTH, revenue.releaseMonth - 1) // Calendar月份从0开始
            calendar.set(Calendar.DAY_OF_MONTH, revenue.releaseDay)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val migratedDailySales = revenue.dailySalesList.mapIndexed { index, dailySales ->
                // 为每一天创建游戏内日期
                val dayCalendar = Calendar.getInstance()
                dayCalendar.time = calendar.time
                dayCalendar.add(Calendar.DAY_OF_YEAR, index)
                val newDate = dayCalendar.time
                
                // 创建新的 DailySales，保留销量和收益，只修改日期
                dailySales.copy(date = newDate)
            }
            
            // 更新 releaseDate 为游戏内时间
            val newReleaseDate = calendar.time
            
            // 更新 GameRevenue
            gameRevenueMap[gameId] = revenue.copy(
                releaseDate = newReleaseDate,
                dailySalesList = migratedDailySales
            )
            
            migratedCount++
            android.util.Log.d("RevenueManager", "  ✓ 已迁移：首日从 ${firstDate} 改为 ${newReleaseDate}")
        }
        
        if (migratedCount > 0) {
            // 保存迁移后的数据
            saveRevenueData()
            android.util.Log.d("RevenueManager", "===== 数据迁移完成，共迁移 $migratedCount 个游戏 =====")
        } else {
            android.util.Log.d("RevenueManager", "===== 无需迁移，所有游戏已使用游戏内时间 =====")
        }
    }
    
}