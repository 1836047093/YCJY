package com.example.yjcy.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.yjcy.data.RevenueManager
import com.example.yjcy.data.RevenueStatistics
import com.example.yjcy.data.SaveData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * 保存结果数据类
 */
data class SaveResult(
    val success: Boolean,
    val originalSizeKB: Double = 0.0,
    val compressedSizeKB: Double = 0.0,
    val errorMessage: String? = null
)

/**
 * 存档管理类（异步版本，支持数据清理和压缩）
 */
class SaveManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_saves", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .serializeNulls() // 确保null值也被序列化，去除PrettyPrinting以减小体积
        // setLenient()已弃用，移除以消除警告
        .create()
    
    companion object {
        private const val MAX_DAILY_SALES_DAYS = 180 // 每个游戏最多保留180天的每日数据（降低以减小存档大小）
        private const val MAX_COMPETITOR_NEWS = 30 // 最多保留30条竞争对手新闻（降低以减小存档大小）
        private const val MAX_GAMES_PER_COMPETITOR = 10 // 每个竞争对手/子公司最多保留10个游戏（防止无限增长）
        private const val MAX_TOURNAMENT_HISTORY = 3 // 每个游戏最多保留3场赛事历史
        private const val MAX_GVA_HISTORY_YEARS = 5 // GVA历史最多保留5年
        private const val WARNING_SIZE_MB = 5.0 // 存档大小警告阈值（MB）
    }
    
    /**
     * 修复旧存档数据，确保所有新增字段都有正确的默认值
     * 这是为了兼容旧版本存档，避免因缺失字段导致闪退
     */
    private fun fixLegacySaveData(saveData: SaveData): SaveData {
        try {
            Log.d("SaveManager", "开始修复旧存档数据，版本: ${saveData.version}")
            
            // 修复游戏数据：确保所有可空字段和新增字段都有正确的默认值
            val fixedGames = saveData.games.map { game ->
                game.copy(
                    // 赛事相关字段（可空）
                    currentTournament = game.currentTournament,
                    lastTournamentDate = game.lastTournamentDate,
                    tournamentHistory = game.tournamentHistory,
                    
                    // 更新历史（可空）
                    updateHistory = game.updateHistory,
                    
                    // GVA奖项（可能缺失）
                    awards = game.awards,
                    
                    // 付费内容（网游必需）
                    monetizationItems = game.monetizationItems,
                    
                    // 其他可能缺失的字段
                    developmentCost = game.developmentCost,
                    promotionIndex = game.promotionIndex,
                    autoUpdate = game.autoUpdate,
                    autoPromotion = game.autoPromotion,
                    version = game.version,
                    
                    // 分阶段开发累积员工（新增字段）
                    allDevelopmentEmployees = game.allDevelopmentEmployees
                )
            }
            
            // 调试：修复前的子公司数据
            Log.d("SaveManager", "🔍 修复前：子公司数量=${saveData.subsidiaries.size}")
            saveData.subsidiaries.forEachIndexed { index, sub ->
                Log.d("SaveManager", "  修复前子公司[$index]: ${sub.name}, ID=${sub.id}")
            }
            
            // 修复SaveData级别的字段
            val fixedSaveData = saveData.copy(
                games = fixedGames,
                
                // 教程和成就系统（可空）
                completedTutorials = saveData.completedTutorials,
                unlockedAchievements = saveData.unlockedAchievements,
                skipTutorial = saveData.skipTutorial,
                
                // 客服中心
                autoProcessComplaints = saveData.autoProcessComplaints,
                complaints = saveData.complaints,
                
                // 自动宣传设置
                autoPromotionThreshold = saveData.autoPromotionThreshold,
                
                // GVA系统（可能缺失）
                companyReputation = saveData.companyReputation,
                gvaHistory = saveData.gvaHistory,
                currentYearNominations = saveData.currentYearNominations,
                gvaAnnouncedDate = saveData.gvaAnnouncedDate,
                
                // 竞争对手系统
                competitors = saveData.competitors,
                competitorNews = saveData.competitorNews,
                
                // 收购系统（子公司和IP）
                ownedIPs = saveData.ownedIPs,
                subsidiaries = saveData.subsidiaries,
                
                // 招聘系统
                jobPostings = saveData.jobPostings,
                
                // 服务器和收益数据
                serverData = saveData.serverData,
                revenueData = saveData.revenueData,
                
                // 创始人职业（可空）
                founderProfession = saveData.founderProfession,
                
                // 兑换码和支持者功能
                usedRedeemCodes = saveData.usedRedeemCodes,
                // 如果旧存档中已使用SUPPORTER兑换码，则自动设置解锁状态
                isSupporterUnlocked = saveData.isSupporterUnlocked || saveData.usedRedeemCodes.contains("SUPPORTER"),
                
                // 自动存档设置
                autoSaveEnabled = saveData.autoSaveEnabled,
                autoSaveInterval = saveData.autoSaveInterval,
                lastAutoSaveDay = saveData.lastAutoSaveDay,
                
                // MOBA电竞系统
                mobaHeroes = saveData.mobaHeroes,
                esportsPlayers = saveData.esportsPlayers,
                myTeamPlayers = saveData.myTeamPlayers,
                activeTournaments = saveData.activeTournaments,
                tournamentHistory = saveData.tournamentHistory,
                
                // GM模式
                gmModeEnabled = saveData.gmModeEnabled
            )
            
            // 调试：修复后的子公司数据
            Log.d("SaveManager", "🔍 修复后：子公司数量=${fixedSaveData.subsidiaries.size}")
            fixedSaveData.subsidiaries.forEachIndexed { index, sub ->
                Log.d("SaveManager", "  修复后子公司[$index]: ${sub.name}, ID=${sub.id}")
            }
            
            Log.d("SaveManager", "修复完成：游戏${fixedGames.size}个，员工${fixedSaveData.allEmployees.size}人，子公司${fixedSaveData.subsidiaries.size}个")
            return fixedSaveData
            
        } catch (e: Exception) {
            Log.e("SaveManager", "修复存档数据时出错，返回原始数据", e)
            e.printStackTrace()
            return saveData
        }
    }
    
    /**
     * 清理存档数据，移除过旧的历史数据以减小体积
     */
    private fun cleanSaveData(saveData: SaveData): SaveData {
        Log.d("SaveManager", "===== 开始清理存档数据 =====")
        
        // 1. 清理收益数据：每个游戏只保留最近180天的每日销售数据
        val cleanedRevenueData = saveData.revenueData.mapValues { (_, revenue) ->
            // 记录清理前的数据
            if (revenue.totalRegisteredPlayers > 0) {
                Log.d("SaveManager", "清理前 - 游戏 ${revenue.gameName}: 总注册=${revenue.totalRegisteredPlayers}")
            }
            
            val cleaned = if (revenue.dailySalesList.size > MAX_DAILY_SALES_DAYS) {
                val recentDailySales = revenue.dailySalesList.takeLast(MAX_DAILY_SALES_DAYS)
                val totalSales = revenue.dailySalesList.sumOf { it.sales }
                val totalRevenue = revenue.dailySalesList.sumOf { it.revenue }
                
                Log.d("SaveManager", "游戏 ${revenue.gameName} 清理前: ${revenue.dailySalesList.size}天, 清理后: ${recentDailySales.size}天")
                Log.d("SaveManager", "  保存总销量: $totalSales, 总收益: $totalRevenue")
                
                // 更新统计数据以保留总计信息
                // 修复：如果statistics为null，创建新的statistics对象
                val updatedStatistics = if (revenue.statistics != null) {
                    revenue.statistics.copy(
                        totalSales = totalSales,
                        totalRevenue = totalRevenue
                    )
                } else {
                    // 创建新的statistics对象，保存全部历史数据
                    RevenueStatistics(
                        totalRevenue = totalRevenue,
                        totalSales = totalSales,
                        averageDailyRevenue = if (revenue.dailySalesList.isNotEmpty()) totalRevenue / revenue.dailySalesList.size else 0.0,
                        averageDailySales = if (revenue.dailySalesList.isNotEmpty()) totalSales / revenue.dailySalesList.size else 0L,
                        peakDailySales = revenue.dailySalesList.maxOfOrNull { it.sales } ?: 0L,
                        peakDailyRevenue = revenue.dailySalesList.maxOfOrNull { it.revenue } ?: 0.0,
                        daysOnMarket = revenue.dailySalesList.size,
                        revenueGrowthRate = 0.0
                    )
                }
                
                // 修复：显式保留所有关键字段，防止数据丢失
                revenue.copy(
                    dailySalesList = recentDailySales,
                    statistics = updatedStatistics,
                    // 显式保留网游特有字段
                    playerInterest = revenue.playerInterest,
                    totalRegisteredPlayers = revenue.totalRegisteredPlayers,
                    lifecycleProgress = revenue.lifecycleProgress,
                    daysSinceLaunch = revenue.daysSinceLaunch,
                    lastInterestDecayDay = revenue.lastInterestDecayDay,
                    monetizationRevenues = revenue.monetizationRevenues,
                    updateCount = revenue.updateCount,
                    cumulativeSalesMultiplier = revenue.cumulativeSalesMultiplier,
                    updateTask = revenue.updateTask
                )
            } else {
                revenue
            }
            
            // 记录清理后的数据
            if (cleaned.totalRegisteredPlayers > 0) {
                Log.d("SaveManager", "清理后 - 游戏 ${cleaned.gameName}: 总注册=${cleaned.totalRegisteredPlayers}")
            } else if (revenue.totalRegisteredPlayers > 0) {
                Log.e("SaveManager", "⚠️⚠️⚠️ 清理数据时丢失了 totalRegisteredPlayers！游戏=${revenue.gameName}")
            }
            
            cleaned
        }
        
        // 2. 清理竞争对手新闻：只保留最近30条
        val cleanedCompetitorNews = if (saveData.competitorNews.size > MAX_COMPETITOR_NEWS) {
            Log.d("SaveManager", "清理竞争对手新闻: ${saveData.competitorNews.size}条 → ${MAX_COMPETITOR_NEWS}条")
            saveData.competitorNews.takeLast(MAX_COMPETITOR_NEWS)
        } else {
            saveData.competitorNews
        }
        
        // 3. 清理竞争对手游戏：每个公司最多保留10个最新游戏
        val cleanedCompetitors = saveData.competitors.map { competitor ->
            if (competitor.games.size > MAX_GAMES_PER_COMPETITOR) {
                Log.d("SaveManager", "清理竞争对手 ${competitor.name} 的游戏: ${competitor.games.size}个 → ${MAX_GAMES_PER_COMPETITOR}个")
                // 按发售日期排序，保留最新的游戏
                val sortedGames = competitor.games.sortedByDescending { game ->
                    game.releaseYear * 12 + game.releaseMonth
                }.take(MAX_GAMES_PER_COMPETITOR)
                competitor.copy(games = sortedGames)
            } else {
                competitor
            }
        }
        
        // 4. 清理子公司游戏：每个子公司最多保留10个最新游戏
        val cleanedSubsidiaries = saveData.subsidiaries.map { subsidiary ->
            if (subsidiary.games.size > MAX_GAMES_PER_COMPETITOR) {
                Log.d("SaveManager", "清理子公司 ${subsidiary.name} 的游戏: ${subsidiary.games.size}个 → ${MAX_GAMES_PER_COMPETITOR}个")
                val sortedGames = subsidiary.games.sortedByDescending { game ->
                    game.releaseYear * 12 + game.releaseMonth
                }.take(MAX_GAMES_PER_COMPETITOR)
                subsidiary.copy(
                    games = sortedGames,
                    employees = subsidiary.employees ?: emptyList() // 确保不为null
                )
            } else {
                subsidiary
            }
        }
        
        // 5. 清理玩家游戏的赛事历史：每个游戏最多保留3场赛事
        val cleanedGames = saveData.games.map { game ->
            val tournamentHistory = game.tournamentHistory
            if (tournamentHistory != null && tournamentHistory.size > MAX_TOURNAMENT_HISTORY) {
                Log.d("SaveManager", "清理游戏 ${game.name} 的赛事历史: ${tournamentHistory.size}场 → ${MAX_TOURNAMENT_HISTORY}场")
                game.copy(tournamentHistory = tournamentHistory.takeLast(MAX_TOURNAMENT_HISTORY))
            } else {
                game
            }
        }
        
        // 6. 清理GVA历史：只保留最近5年
        val currentYear = saveData.currentYear
        val cleanedGvaHistory = if (saveData.gvaHistory.isNotEmpty()) {
            val filtered = saveData.gvaHistory.filter { nomination ->
                currentYear - nomination.year <= MAX_GVA_HISTORY_YEARS
            }
            if (filtered.size < saveData.gvaHistory.size) {
                Log.d("SaveManager", "清理GVA历史: ${saveData.gvaHistory.size}条 → ${filtered.size}条")
            }
            filtered
        } else {
            saveData.gvaHistory
        }
        
        Log.d("SaveManager", "数据清理完成: 收益数据=${cleanedRevenueData.size}个游戏, 竞争对手新闻=${cleanedCompetitorNews.size}条, 竞争对手=${cleanedCompetitors.size}家, 子公司=${cleanedSubsidiaries.size}家")
        
        return saveData.copy(
            revenueData = cleanedRevenueData,
            competitorNews = cleanedCompetitorNews,
            competitors = cleanedCompetitors,
            subsidiaries = cleanedSubsidiaries,
            games = cleanedGames,
            gvaHistory = cleanedGvaHistory
        )
    }
    
    /**
     * 压缩字符串（GZIP）
     */
    private fun compressString(input: String): ByteArray {
        val bos = java.io.ByteArrayOutputStream()
        java.util.zip.GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(input) }
        return bos.toByteArray()
    }
    
    /**
     * 解压字符串（GZIP）
     */
    private fun decompressString(compressed: ByteArray): String {
        val bis = java.io.ByteArrayInputStream(compressed)
        return java.util.zip.GZIPInputStream(bis).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
    
    // 异步保存游戏（带数据清理和压缩）
    suspend fun saveGameAsync(slotIndex: Int, saveData: SaveData): SaveResult = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            
            // 0. 强制保存RevenueManager的pending数据（性能优化：避免丢失数据）
            RevenueManager.forceSave()
            
            // 1. 清理数据
            val cleanedData = cleanSaveData(saveData)
            
            // 2. 序列化为JSON
            val json = gson.toJson(cleanedData)
            val jsonSizeKB = json.length / 1024.0
            val jsonSizeMB = jsonSizeKB / 1024.0
            
            Log.d("SaveManager", "JSON大小: ${String.format(Locale.US, "%.2f", jsonSizeKB)} KB (${String.format(Locale.US, "%.2f", jsonSizeMB)} MB)")
            
            // 检查存档大小是否过大
            if (jsonSizeMB > WARNING_SIZE_MB) {
                Log.w("SaveManager", "⚠️ 警告：存档数据过大！JSON=${String.format(Locale.US, "%.2f", jsonSizeMB)}MB，可能导致保存失败")
                Log.w("SaveManager", "  - 游戏数量: ${cleanedData.games.size}")
                Log.w("SaveManager", "  - 员工数量: ${cleanedData.allEmployees.size}")
                Log.w("SaveManager", "  - 竞争对手: ${cleanedData.competitors.size}家，共${cleanedData.competitors.sumOf { it.games.size }}个游戏")
                Log.w("SaveManager", "  - 子公司: ${cleanedData.subsidiaries.size}家，共${cleanedData.subsidiaries.sumOf { it.games.size }}个游戏")
                Log.w("SaveManager", "  - 收益记录: ${cleanedData.revenueData.size}个游戏")
            }
            
            // 3. GZIP压缩
            val compressed = compressString(json)
            val compressedSizeKB = compressed.size / 1024.0
            val compressedSizeMB = compressedSizeKB / 1024.0
            val compressionRatio = (1 - compressedSizeKB / jsonSizeKB) * 100
            
            Log.d("SaveManager", "压缩后大小: ${String.format(Locale.US, "%.2f", compressedSizeKB)} KB (${String.format(Locale.US, "%.2f", compressedSizeMB)} MB), 压缩率: ${String.format(Locale.US, "%.1f", compressionRatio)}%")
            
            // 检查压缩后大小是否仍然过大
            if (compressedSizeMB > WARNING_SIZE_MB) {
                Log.e("SaveManager", "❌ 错误：压缩后存档仍然过大！${String.format(Locale.US, "%.2f", compressedSizeMB)}MB，可能超出SharedPreferences限制")
            }
            
            // 4. Base64编码后存储（因为SharedPreferences只能存字符串）
            val base64Encoded = android.util.Base64.encodeToString(compressed, android.util.Base64.DEFAULT)
            
            // 5. 保存到SharedPreferences
            sharedPreferences.edit {
                putString("save_slot_${slotIndex}_compressed", base64Encoded)
                putBoolean("save_slot_${slotIndex}_is_compressed", true)
                // 移除旧的未压缩版本（如果存在）
                remove("save_slot_$slotIndex")
            }
            
            val duration = System.currentTimeMillis() - startTime
            Log.d("SaveManager", "保存游戏到存档位 $slotIndex 完成，耗时: ${duration}ms")
            Log.d("SaveManager", "游戏数量: ${saveData.games.size}, 收益记录: ${saveData.revenueData.size}个游戏")
            
            SaveResult(
                success = true,
                originalSizeKB = jsonSizeKB,
                compressedSizeKB = compressedSizeKB,
                errorMessage = null
            )
        } catch (e: OutOfMemoryError) {
            Log.e("SaveManager", "保存游戏失败: 内存不足", e)
            e.printStackTrace()
            SaveResult(
                success = false,
                errorMessage = "内存不足！存档数据过大（游戏: ${saveData.games.size}, 竞争对手: ${saveData.competitors.sumOf { it.games.size }}, 子公司: ${saveData.subsidiaries.sumOf { it.games.size }}）。建议关闭部分功能或重新开档。"
            )
        } catch (e: android.os.TransactionTooLargeException) {
            Log.e("SaveManager", "保存游戏失败: 事务过大", e)
            e.printStackTrace()
            SaveResult(
                success = false,
                errorMessage = "存档数据过大，超出系统限制！建议重新开档或联系开发者。"
            )
        } catch (e: Exception) {
            Log.e("SaveManager", "保存游戏失败", e)
            e.printStackTrace()
            SaveResult(
                success = false,
                errorMessage = "保存失败: ${e.javaClass.simpleName} - ${e.message ?: "未知错误"}"
            )
        }
    }
    
    // 异步加载游戏（支持压缩和未压缩格式）
    suspend fun loadGameAsync(slotIndex: Int): SaveData? = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val isCompressed = sharedPreferences.getBoolean("save_slot_${slotIndex}_is_compressed", false)
            
            val json = if (isCompressed) {
                // 加载压缩格式
                val base64Encoded = sharedPreferences.getString("save_slot_${slotIndex}_compressed", null)
                if (base64Encoded != null) {
                    val compressed = android.util.Base64.decode(base64Encoded, android.util.Base64.DEFAULT)
                    decompressString(compressed)
                } else {
                    null
                }
            } else {
                // 加载旧的未压缩格式
                sharedPreferences.getString("save_slot_$slotIndex", null)
            }
            
            return@withContext if (json != null) {
                try {
                    val loadedData = gson.fromJson(json, SaveData::class.java)
                    // 修复旧存档数据，确保兼容性
                    val fixedData = fixLegacySaveData(loadedData)
                    val duration = System.currentTimeMillis() - startTime
                    Log.d("SaveManager", "从存档位 $slotIndex 加载游戏完成（${if (isCompressed) "压缩" else "未压缩"}），耗时: ${duration}ms, 游戏数量: ${fixedData.games.size}")
                    fixedData
                } catch (e: Exception) {
                    Log.e("SaveManager", "解析存档失败", e)
                    Log.e("SaveManager", "错误详情: ${e.message}")
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SaveManager", "加载存档失败", e)
            null
        }
    }
    
    // 异步删除存档
    suspend fun deleteSaveAsync(slotIndex: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            remove("save_slot_$slotIndex")
            remove("save_slot_${slotIndex}_compressed")
            remove("save_slot_${slotIndex}_is_compressed")
        }
    }
    
    // 异步加载所有存档
    suspend fun getAllSavesAsync(): Map<Int, SaveData?> = withContext(Dispatchers.IO) {
        mapOf(
            1 to loadGameAsync(1),
            2 to loadGameAsync(2),
            3 to loadGameAsync(3)
        )
    }
    
    // 同步方法（保留用于兼容）
    @Deprecated("使用异步版本 saveGameAsync")
    fun saveGame(slotIndex: Int, saveData: SaveData) {
        val json = gson.toJson(saveData)
        sharedPreferences.edit {
            putString("save_slot_$slotIndex", json)
        }
    }
    
    @Deprecated("使用异步版本 loadGameAsync")
    fun loadGame(slotIndex: Int): SaveData? {
        val json = sharedPreferences.getString("save_slot_$slotIndex", null)
        return if (json != null) {
            try {
                gson.fromJson(json, SaveData::class.java)
            } catch (e: Exception) {
                Log.e("SaveManager", "加载存档失败", e)
                null
            }
        } else {
            null
        }
    }
}
