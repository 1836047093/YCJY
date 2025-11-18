package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import kotlin.random.Random

/**
 * 赛事类型枚举
 */
enum class TournamentType(
    val displayName: String,
    val icon: String,
    val baseCost: Long,
    val duration: Int, // 天数
    val prizePool: Long,
    val minActivePlayers: Long,
    val cooldownDays: Int, // 冷却天数
    val sponsorRateMin: Double, // 赞助商收入最小比例
    val sponsorRateMax: Double, // 赞助商收入最大比例
    val broadcastRevenue: Long, // 转播权收入
    val fansGrowthMin: Double, // 粉丝增长最小比例
    val fansGrowthMax: Double, // 粉丝增长最大比例
    val playersGrowthMin: Double, // 活跃玩家增长最小比例
    val playersGrowthMax: Double, // 活跃玩家增长最大比例
    val interestBonus: Double, // 兴趣值恢复
    val reputationBonus: Int // 声誉提升
) {
    REGIONAL(
        displayName = "城市杯",
        icon = "🥉",
        baseCost = 500000L,
        duration = 3,
        prizePool = 100000L,
        minActivePlayers = 10000L,
        cooldownDays = 30,
        sponsorRateMin = 0.30,
        sponsorRateMax = 0.50,
        broadcastRevenue = 0L,
        fansGrowthMin = 0.05,
        fansGrowthMax = 0.10,
        playersGrowthMin = 0.03,
        playersGrowthMax = 0.08,
        interestBonus = 5.0,
        reputationBonus = 1
    ),
    NATIONAL(
        displayName = "全国锦标赛",
        icon = "🥈",
        baseCost = 2000000L,
        duration = 7,
        prizePool = 500000L,
        minActivePlayers = 50000L,
        cooldownDays = 90,
        sponsorRateMin = 0.40,
        sponsorRateMax = 0.60,
        broadcastRevenue = 750000L,
        fansGrowthMin = 0.10,
        fansGrowthMax = 0.20,
        playersGrowthMin = 0.08,
        playersGrowthMax = 0.15,
        interestBonus = 10.0,
        reputationBonus = 3
    ),
    WORLD_FINALS(
        displayName = "全球总决赛",
        icon = "💎",
        baseCost = 30000000L,
        duration = 30,
        prizePool = 10000000L,
        minActivePlayers = 500000L,
        cooldownDays = 365,
        sponsorRateMin = 0.80,
        sponsorRateMax = 1.00,
        broadcastRevenue = 12000000L,
        fansGrowthMin = 0.30,
        fansGrowthMax = 0.50,
        playersGrowthMin = 0.25,
        playersGrowthMax = 0.40,
        interestBonus = 30.0,
        reputationBonus = 15
    )
}

/**
 * 赛事状态枚举
 */
enum class TournamentStatus {
    PREPARING,  // 筹备中
    ONGOING,    // 进行中
    COMPLETED   // 已完成
}

/**
 * 赛事成功等级
 */
enum class TournamentSuccessLevel(
    val displayName: String,
    val revenueMultiplier: Double, // 收益倍率
    val effectMultiplier: Double // 效果倍率
) {
    GREAT_SUCCESS("大成功", 1.3, 1.5),
    SUCCESS("成功", 1.0, 1.0),
    AVERAGE("一般", 0.8, 0.7),
    FAILURE("失败", 0.5, 0.5)
}

/**
 * 赛事资格检查结果
 */
sealed class TournamentEligibility {
    object Eligible : TournamentEligibility()
    data class NotEligible(val reason: String) : TournamentEligibility()
}

/**
 * 赛事收益数据
 */
data class TournamentRevenue(
    val sponsorRevenue: Long,
    val broadcastRevenue: Long,
    val ticketRevenue: Long,
    val totalRevenue: Long,
    val netProfit: Long // 净利润（总收益 - 成本）
)

/**
 * 电竞赛事数据类
 */
data class EsportsTournament(
    val id: String,
    val gameId: String,
    val gameName: String,
    val type: TournamentType,
    val status: TournamentStatus,
    val startYear: Int,
    val startMonth: Int,
    val startDay: Int,
    val currentDay: Int = 0, // 当前进行到第几天
    val investment: Long, // 投入成本
    val sponsorRevenue: Long = 0, // 赞助商收入
    val ticketRevenue: Long = 0, // 门票收入
    val broadcastRevenue: Long = 0, // 转播权收入
    val viewerCount: Long = 0, // 观看人数
    val successLevel: TournamentSuccessLevel = TournamentSuccessLevel.SUCCESS,
    val fansGained: Long = 0L, // 获得的粉丝数
    val playersGained: Long = 0, // 获得的活跃玩家数
    val interestBonus: Double = 0.0, // 兴趣值恢复
    val reputationGained: Int = 0, // 声誉提升
    val champion: String = "", // 冠军战队名称
    val randomEvent: String = "" // 随机事件描述
) {
    /**
     * 获取总收益
     */
    fun getTotalRevenue(): Long {
        return sponsorRevenue + ticketRevenue + broadcastRevenue
    }
    
    /**
     * 获取净利润
     */
    fun getNetProfit(): Long {
        return getTotalRevenue() - investment
    }
}

/**
 * 赛事管理器
 */
object TournamentManager {
    
    /**
     * 检查游戏是否可以举办赛事（基本条件）
     */
    fun canHostTournament(game: Game, revenueData: GameRevenue?): Boolean {
        // 必须是竞技类游戏
        if (!isCompetitiveGame(game.theme)) return false
        
        // 必须是网络游戏
        if (game.businessModel != com.example.yjcy.ui.BusinessModel.ONLINE_GAME) return false
        
        // 必须已发售
        if (game.releaseStatus != GameReleaseStatus.RELEASED && 
            game.releaseStatus != GameReleaseStatus.RATED) return false
        
        // 评分必须 >= 8.0
        if (game.rating != null && game.rating < 8.0f) return false
        
        // 活跃玩家必须 >= 100,000
        val activePlayers = revenueData?.getActivePlayers() ?: 0
        if (activePlayers < 100000L) return false
        
        return true
    }
    
    /**
     * 检查特定赛事类型是否可以举办
     */
    fun canHostTournamentType(
        game: Game,
        revenueData: GameRevenue?,
        type: TournamentType,
        currentDate: GameDate,
        money: Long
    ): TournamentEligibility {
        // 检查基本条件
        if (!canHostTournament(game, revenueData)) {
            return TournamentEligibility.NotEligible("游戏不满足基本条件")
        }
        
        // 检查活跃玩家数
        val activePlayers = revenueData?.getActivePlayers() ?: 0
        if (activePlayers < type.minActivePlayers) {
            return TournamentEligibility.NotEligible("需要 ${formatNumber(type.minActivePlayers)} 活跃玩家")
        }
        
        // 检查资金
        if (money < type.baseCost) {
            return TournamentEligibility.NotEligible("资金不足")
        }
        
        // 检查冷却时间
        if (game.lastTournamentDate != null) {
            val daysPassed = calculateDaysBetween(game.lastTournamentDate, currentDate)
            if (daysPassed < type.cooldownDays) {
                val remainingDays = type.cooldownDays - daysPassed
                return TournamentEligibility.NotEligible("冷却中: 还需 $remainingDays 天")
            }
        }
        
        // 检查是否有正在进行的赛事
        if (game.currentTournament != null && game.currentTournament.status != TournamentStatus.COMPLETED) {
            return TournamentEligibility.NotEligible("已有赛事进行中")
        }
        
        return TournamentEligibility.Eligible
    }
    
    /**
     * 创建赛事
     */
    fun createTournament(
        game: Game,
        type: TournamentType,
        currentDate: GameDate
    ): EsportsTournament {
        val championTeam = generateChampionTeam()
        
        return EsportsTournament(
            id = "tournament_${System.currentTimeMillis()}_${Random.nextInt()}",
            gameId = game.id,
            gameName = game.name,
            type = type,
            status = TournamentStatus.PREPARING,
            startYear = currentDate.year,
            startMonth = currentDate.month,
            startDay = currentDate.day,
            investment = type.baseCost,
            champion = championTeam
        )
    }
    
    /**
     * 更新赛事进度（每日调用）
     */
    fun updateTournament(
        tournament: EsportsTournament,
        currentDate: GameDate
    ): EsportsTournament {
        if (tournament.status == TournamentStatus.COMPLETED) {
            return tournament
        }
        
        val daysPassed = calculateDaysBetween(
            GameDate(tournament.startYear, tournament.startMonth, tournament.startDay),
            currentDate
        )
        
        // 筹备期：开始日期后30天正式开始
        if (daysPassed < 30) {
            return tournament.copy(status = TournamentStatus.PREPARING)
        }
        
        // 计算赛事进行天数
        val tournamentDay = daysPassed - 30 + 1
        
        // 赛事进行中
        if (tournamentDay <= tournament.type.duration) {
            return tournament.copy(
                status = TournamentStatus.ONGOING,
                currentDay = tournamentDay
            )
        }
        
        // 赛事已完成（首次完成时才结算）
        if (tournament.status != TournamentStatus.COMPLETED) {
            return tournament.copy(
                status = TournamentStatus.COMPLETED,
                currentDay = tournament.type.duration
            )
        }
        
        return tournament
    }
    
    /**
     * 生成赛事成功等级
     */
    fun determineTournamentSuccess(
        tournament: EsportsTournament,
        game: Game,
        companyReputation: Float
    ): TournamentSuccessLevel {
        var successScore = 50.0 // 基础分50
        
        // 游戏评分影响 (0-20分)
        if (game.rating != null) {
            successScore += (game.rating - 5.0) * 4.0
        }
        
        // 公司声誉影响 (0-15分)
        successScore += (companyReputation - 50) * 0.3
        
        // 投入成本影响 (0-10分)
        val costBonus = when (tournament.type) {
            TournamentType.REGIONAL -> 2.0
            TournamentType.NATIONAL -> 5.0
            TournamentType.WORLD_FINALS -> 10.0
        }
        successScore += costBonus
        
        // 随机因素 (0-5分)
        successScore += Random.nextDouble(0.0, 5.0)
        
        return when {
            successScore >= 75.0 -> TournamentSuccessLevel.GREAT_SUCCESS
            successScore >= 50.0 -> TournamentSuccessLevel.SUCCESS
            successScore >= 30.0 -> TournamentSuccessLevel.AVERAGE
            else -> TournamentSuccessLevel.FAILURE
        }
    }
    
    /**
     * 计算赛事收益
     */
    fun calculateTournamentRevenue(
        tournament: EsportsTournament,
        game: Game,
        revenueData: GameRevenue
    ): TournamentRevenue {
        val type = tournament.type
        val activePlayers = revenueData.getActivePlayers()
        
        // 赞助商收入
        val baseSponsorRate = (type.sponsorRateMin + type.sponsorRateMax) / 2.0
        val ratingMultiplier = (game.rating ?: 6.0f) / 10.0
        val playerMultiplier = 1.0 + kotlin.math.log10(activePlayers.toDouble() / 10000.0) * 0.1
        val sponsorRevenue = (tournament.investment * baseSponsorRate * ratingMultiplier * 
                             playerMultiplier).toLong()
        
        // 转播权收入
        val broadcastRevenue = (type.broadcastRevenue * 
                               (1.0 + activePlayers / 1000000.0 * 0.2)).toLong()
        
        // 门票收入（仅全球赛）
        val ticketRevenue = if (type == TournamentType.WORLD_FINALS) {
            val normalTickets = (activePlayers * 0.0001).toLong()
            val vipTickets = (activePlayers * 0.00001).toLong()
            val normalPrice = Random.nextInt(200, 500)
            val vipPrice = Random.nextInt(1000, 3000)
            (normalTickets * normalPrice + vipTickets * vipPrice).toLong()
        } else {
            0L
        }
        
        val totalRevenue = sponsorRevenue + broadcastRevenue + ticketRevenue
        val netProfit = totalRevenue - tournament.investment
        
        return TournamentRevenue(
            sponsorRevenue = sponsorRevenue,
            broadcastRevenue = broadcastRevenue,
            ticketRevenue = ticketRevenue,
            totalRevenue = totalRevenue,
            netProfit = netProfit
        )
    }
    
    /**
     * 计算粉丝增长递减系数
     * 粉丝越多，增长比例越低（符合真实传播规律）
     */
    private fun calculateFansDecayFactor(currentFans: Long): Double {
        return when {
            currentFans < 1_000_000L -> 1.0      // 0-100万：完整比例
            currentFans < 10_000_000L -> 0.6     // 100万-1000万：60%
            currentFans < 100_000_000L -> 0.4    // 1000万-1亿：40%
            currentFans < 1_000_000_000L -> 0.2  // 1亿-10亿：20%
            currentFans < 10_000_000_000L -> 0.08 // 10亿-100亿：8%
            else -> 0.02                          // 100亿以上：2%
        }
    }
    
    /**
     * 获取赛事类型的粉丝增长上限
     */
    private fun getTournamentFansCapByType(type: TournamentType): Long {
        return when (type) {
            TournamentType.REGIONAL -> 300_000L        // 城市杯：30万
            TournamentType.NATIONAL -> 1_000_000L      // 全国锦标赛：100万
            TournamentType.WORLD_FINALS -> 20_000_000L  // 全球总决赛：2000万
        }
    }
    
    /**
     * 应用赛事效果到游戏
     */
    fun applyTournamentEffects(
        tournament: EsportsTournament,
        game: Game,
        revenueData: GameRevenue,
        currentFans: Long
    ): Triple<Long, Long, Double> {
        val type = tournament.type
        
        // 粉丝增长（应用递减系数和上限）
        val fansGrowthRate = (type.fansGrowthMin + type.fansGrowthMax) / 2.0
        val decayFactor = calculateFansDecayFactor(currentFans)
        val randomFactor = 1.0 + Random.nextDouble(-0.1, 0.1)
        val calculatedFansGain = (currentFans * fansGrowthRate * decayFactor * randomFactor).toLong()
        val fansCap = getTournamentFansCapByType(type)
        val fansGained = calculatedFansGain.coerceAtMost(fansCap)
        
        // 活跃玩家增长
        val activePlayers = revenueData.getActivePlayers()
        val playersGrowthRate = (type.playersGrowthMin + type.playersGrowthMax) / 2.0
        val playersGained = (activePlayers * playersGrowthRate * randomFactor).toLong()
        
        // 兴趣值恢复
        val interestBonus = type.interestBonus
        
        return Triple(fansGained, playersGained, interestBonus)
    }
    
    /**
     * 生成随机事件
     */
    fun generateRandomEvent(): Pair<String, Double> {
        val roll = Random.nextDouble()
        
        return when {
            // 15%概率正面事件
            roll < 0.15 -> {
                val events = listOf(
                    "🌟 明星选手诞生" to 1.2,
                    "📺 病毒式传播" to 1.5,
                    "💰 额外赞助" to 1.3,
                    "🎁 官方支持" to 1.25
                )
                events.random()
            }
            // 5%概率负面事件
            roll < 0.20 -> {
                val events = listOf(
                    "🐛 服务器崩溃" to 0.7,
                    "⚠️ 作弊丑闻" to 0.75,
                    "😴 观众反响平淡" to 0.8,
                    "🌧️ 技术故障" to 0.85
                )
                events.random()
            }
            // 80%概率正常
            else -> "" to 1.0
        }
    }
    
    /**
     * 判断游戏类型是否适合电竞
     */
    private fun isCompetitiveGame(theme: GameTheme): Boolean {
        return theme in listOf(
            GameTheme.MOBA,
            GameTheme.SHOOTER,
            GameTheme.SPORTS,
            GameTheme.RACING,
            GameTheme.STRATEGY
        )
    }
    
    /**
     * 生成冠军战队名称
     */
    private fun generateChampionTeam(): String {
        val prefixes = listOf("龙之", "凤凰", "狂暴", "闪电", "幻影", "钢铁", "星辰", "烈焰", "寒冰", "雷霆")
        val suffixes = listOf("战队", "俱乐部", "电竞", "联盟", "军团", "公会")
        return prefixes.random() + suffixes.random()
    }
    
    /**
     * 计算两个日期之间的天数
     */
    private fun calculateDaysBetween(from: GameDate, to: GameDate): Int {
        val yearDiff = to.year - from.year
        val monthDiff = to.month - from.month
        val dayDiff = to.day - from.day
        return yearDiff * 360 + monthDiff * 30 + dayDiff
    }
    
    /**
     * 格式化数字
     */
    private fun formatNumber(number: Long): String {
        return when {
            number >= 10000 -> "${number / 10000}万"
            number >= 1000 -> "${number / 1000}K"
            else -> number.toString()
        }
    }
}
