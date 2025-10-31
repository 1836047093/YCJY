package com.example.yjcy.data

import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.ui.Platform
import kotlin.math.min

/**
 * GVA游戏大奖管理器
 * 负责提名生成、评分计算、奖励发放等核心逻辑
 */
object GVAManager {
    
    /**
     * 生成初步提名（12月15日调用）
     * @param year 当前年份
     * @param playerGames 玩家的游戏列表
     * @param playerCompanyName 玩家公司名称
     * @param playerFans 玩家粉丝数
     * @param competitorCompanies AI竞争对手公司列表
     * @param revenueData 收益数据（用于获取销量和玩家数）
     * @return 初步提名列表
     */
    fun generatePreliminaryNominations(
        year: Int,
        playerGames: List<Game>,
        playerCompanyName: String,
        playerFans: Long,
        competitorCompanies: List<CompetitorCompany>,
        revenueData: Map<String, GameRevenue>
    ): List<AwardNomination> {
        // 筛选符合条件的游戏（1月1日-12月14日发售）
        val eligibleGames = filterEligibleGames(
            playerGames = playerGames,
            playerCompanyName = playerCompanyName,
            playerFans = playerFans,
            competitorCompanies = competitorCompanies,
            revenueData = revenueData,
            startDate = GameDate(year, 1, 1),
            endDate = GameDate(year, 12, 14)
        )
        
        // 为每个奖项生成提名
        return GVAAward.values().map { award ->
            generateNominationForAward(
                award = award,
                eligibleGames = eligibleGames,
                isFinal = false,
                currentYear = year
            )
        }
    }
    
    /**
     * 生成最终结果（12月31日调用）
     * @return 最终提名列表（包含获奖者）
     */
    fun generateFinalNominations(
        year: Int,
        playerGames: List<Game>,
        playerCompanyName: String,
        playerFans: Long,
        competitorCompanies: List<CompetitorCompany>,
        revenueData: Map<String, GameRevenue>
    ): List<AwardNomination> {
        // 筛选全年游戏（1月1日-12月31日）
        val eligibleGames = filterEligibleGames(
            playerGames = playerGames,
            playerCompanyName = playerCompanyName,
            playerFans = playerFans,
            competitorCompanies = competitorCompanies,
            revenueData = revenueData,
            startDate = GameDate(year, 1, 1),
            endDate = GameDate(year, 12, 31)
        )
        
        // 为每个奖项生成最终提名和获奖者
        return GVAAward.values().map { award ->
            generateNominationForAward(
                award = award,
                eligibleGames = eligibleGames,
                isFinal = true,
                currentYear = year
            )
        }
    }
    
    /**
     * 筛选符合条件的游戏
     */
    private fun filterEligibleGames(
        playerGames: List<Game>,
        playerCompanyName: String,
        playerFans: Long,
        competitorCompanies: List<CompetitorCompany>,
        revenueData: Map<String, GameRevenue>,
        startDate: GameDate,
        endDate: GameDate
    ): List<EligibleGame> {
        val result = mutableListOf<EligibleGame>()
        
        // 添加玩家游戏
        playerGames.forEach { game ->
            if (isGameEligible(game, startDate, endDate)) {
                val revenue = revenueData[game.id]
                // 计算运营天数（从发售到评选日期）
                val daysOnMarket = calculateDaysOnMarket(
                    game.releaseYear ?: 0,
                    game.releaseMonth ?: 0,
                    game.releaseDay ?: 0,
                    endDate.year,
                    endDate.month,
                    endDate.day
                )
                result.add(
                    EligibleGame(
                        gameId = game.id,
                        gameName = game.name,
                        companyId = -1, // 玩家公司
                        companyName = playerCompanyName,
                        theme = game.theme,
                        platforms = game.platforms,
                        businessModel = game.businessModel,
                        rating = game.rating ?: 0f,
                        totalSales = revenue?.getTotalSales() ?: 0L,
                        activePlayers = revenue?.getActivePlayers() ?: 0L,
                        companyFans = playerFans,
                        isPlayerGame = true,
                        releaseYear = game.releaseYear ?: 0,
                        releaseMonth = game.releaseMonth ?: 0,
                        releaseDay = game.releaseDay ?: 0,
                        teamSize = game.assignedEmployees.size,
                        developmentCost = game.developmentCost,
                        daysOnMarket = daysOnMarket
                    )
                )
            }
        }
        
        // 添加AI竞争对手游戏
        competitorCompanies.forEach { company ->
            company.games.forEach { game ->
                if (isCompetitorGameEligible(game, startDate, endDate)) {
                    // 计算AI游戏运营天数
                    val daysOnMarket = calculateDaysOnMarket(
                        game.releaseYear,
                        game.releaseMonth,
                        1, // AI游戏默认1号
                        endDate.year,
                        endDate.month,
                        endDate.day
                    )
                    result.add(
                        EligibleGame(
                            gameId = game.id,
                            gameName = game.name,
                            companyId = company.id,
                            companyName = company.name,
                            theme = game.theme,
                            platforms = game.platforms,
                            businessModel = game.businessModel,
                            rating = game.rating,
                            totalSales = game.salesCount,
                            activePlayers = game.activePlayers,
                            companyFans = company.fans,
                            isPlayerGame = false,
                            releaseYear = game.releaseYear,
                            releaseMonth = game.releaseMonth,
                            releaseDay = 1, // AI游戏默认1号
                            teamSize = 5, // AI游戏默认5人团队
                            developmentCost = 100000L, // AI游戏默认成本
                            daysOnMarket = daysOnMarket
                        )
                    )
                }
            }
        }
        
        return result
    }
    
    /**
     * 检查玩家游戏是否符合参赛条件
     */
    private fun isGameEligible(game: Game, startDate: GameDate, endDate: GameDate): Boolean {
        // 必须已发售并有评分
        if (game.rating == null || game.rating < 6.0f) return false
        if (game.releaseStatus != GameReleaseStatus.RELEASED && 
            game.releaseStatus != GameReleaseStatus.RATED) return false
        
        // 必须有发售日期
        val releaseYear = game.releaseYear ?: return false
        val releaseMonth = game.releaseMonth ?: return false
        val releaseDay = game.releaseDay ?: return false
        
        val releaseDate = GameDate(releaseYear, releaseMonth, releaseDay)
        return releaseDate >= startDate && releaseDate <= endDate
    }
    
    /**
     * 检查AI游戏是否符合参赛条件
     */
    private fun isCompetitorGameEligible(game: CompetitorGame, startDate: GameDate, endDate: GameDate): Boolean {
        // 必须有足够高的评分
        if (game.rating < 6.0f) return false
        
        // 检查发售日期（AI游戏默认每月1号）
        val releaseDate = GameDate(game.releaseYear, game.releaseMonth, 1)
        return releaseDate >= startDate && releaseDate <= endDate
    }
    
    /**
     * 为单个奖项生成提名
     */
    private fun generateNominationForAward(
        award: GVAAward,
        eligibleGames: List<EligibleGame>,
        isFinal: Boolean,
        currentYear: Int
    ): AwardNomination {
        // 筛选符合该奖项的游戏
        val candidates = filterCandidatesForAward(award, eligibleGames)
        
        // 计算得分并排序
        val scored = candidates.map { game ->
            NomineeInfo(
                gameId = game.gameId,
                gameName = game.gameName,
                companyId = game.companyId,
                companyName = game.companyName,
                rating = game.rating,
                popularityScore = calculatePopularityScore(game),
                totalScore = calculateAwardScore(game, award),
                isPlayerGame = game.isPlayerGame,
                releaseDate = "${game.releaseMonth}月${game.releaseDay}日"
            )
        }.sortedByDescending { it.totalScore }
        
        // 取前3名作为提名
        val nominees = scored.take(3)
        
        // 如果是最终结果，第一名为获奖者
        val winner = if (isFinal && nominees.isNotEmpty()) nominees.first() else null
        
        return AwardNomination(
            year = currentYear, // 使用当前年份而不是游戏发售年份
            award = award,
            nominees = nominees,
            winner = winner,
            isFinal = isFinal
        )
    }
    
    /**
     * 筛选符合奖项的候选游戏
     */
    private fun filterCandidatesForAward(award: GVAAward, games: List<EligibleGame>): List<EligibleGame> {
        return when (award.category) {
            // 主题类奖项：只评选对应主题
            AwardCategory.THEME -> {
                games.filter { it.theme == award.theme }
            }
            
            // 综合类奖项
            AwardCategory.GENERAL -> {
                when (award) {
                    GVAAward.GAME_OF_YEAR -> games // 所有游戏
                    GVAAward.BEST_INDIE -> games.filter { 
                        it.businessModel == BusinessModel.SINGLE_PLAYER &&
                        it.platforms.size == 1 // 单平台
                    }
                    GVAAward.PLAYERS_CHOICE -> games // 所有游戏
                    GVAAward.BEST_ONLINE -> games.filter { 
                        it.businessModel == BusinessModel.ONLINE_GAME 
                    }
                    else -> games
                }
            }
            
            // 特殊成就奖项
            AwardCategory.SPECIAL -> {
                when (award) {
                    GVAAward.INNOVATION -> games.filter { 
                        it.rating >= 8.5f && it.teamSize <= 3 
                    }
                    GVAAward.PERFECT_QUALITY -> games.filter { 
                        it.rating >= 9.0f 
                    }
                    GVAAward.COMMERCIAL_MIRACLE -> games.filter {
                        (it.businessModel == BusinessModel.SINGLE_PLAYER && it.totalSales >= 1000000L) ||
                        (it.businessModel == BusinessModel.ONLINE_GAME && it.activePlayers >= 500000L)
                    }
                    GVAAward.EVERGREEN -> {
                        // 长青树奖：运营超过2年（730天）且仍保持高质量
                        games.filter {
                            it.daysOnMarket >= 730 &&  // 至少运营2年
                            it.rating >= 8.0f &&  // 保持高评分
                            (it.businessModel == BusinessModel.ONLINE_GAME && it.activePlayers >= 10000L ||  // 网游仍有活跃玩家
                             it.businessModel == BusinessModel.SINGLE_PLAYER && it.totalSales >= 50000L)  // 单机销量可观
                        }
                    }
                    GVAAward.CULTURAL_IMPACT -> games.filter { 
                        it.companyFans >= 500000 
                    }
                    else -> games
                }
            }
        }
    }
    
    /**
     * 计算人气分
     */
    private fun calculatePopularityScore(game: EligibleGame): Float {
        return when (game.businessModel) {
            BusinessModel.SINGLE_PLAYER -> min(game.totalSales.toFloat() / 10000f, 10f)
            BusinessModel.ONLINE_GAME -> min(game.activePlayers.toFloat() / 50000f, 10f)
        }
    }
    
    /**
     * 计算奖项综合得分
     */
    private fun calculateAwardScore(game: EligibleGame, award: GVAAward): Float {
        val rating = game.rating
        val popularityScore = calculatePopularityScore(game)
        
        return when (award) {
            // 主题类奖项：70%评分 + 30%人气
            in GVAAward.values().filter { it.category == AwardCategory.THEME } -> {
                rating * 0.7f + popularityScore * 0.3f
            }
            
            // 年度游戏：80%评分 + 20%人气
            GVAAward.GAME_OF_YEAR -> {
                rating * 0.8f + popularityScore * 0.2f
            }
            
            // 最佳独立游戏：60%评分 + 20%人气 + 20%创新
            GVAAward.BEST_INDIE -> {
                val innovationScore = calculateInnovationScore(game)
                rating * 0.6f + popularityScore * 0.2f + innovationScore * 0.2f
            }
            
            // 最受玩家喜爱：纯人气
            GVAAward.PLAYERS_CHOICE -> {
                val fansScore = game.companyFans.toFloat() / 1000f
                val extraPopularity = when (game.businessModel) {
                    BusinessModel.SINGLE_PLAYER -> game.totalSales.toFloat() / 5000f
                    BusinessModel.ONLINE_GAME -> game.activePlayers.toFloat() / 10000f
                }
                fansScore + extraPopularity * 2f
            }
            
            // 最佳网络游戏：60%评分 + 30%活跃度 + 10%收入
            GVAAward.BEST_ONLINE -> {
                val activityScore = min(game.activePlayers.toFloat() / 50000f, 10f)
                // 收入分暂时用活跃玩家估算
                val revenueScore = min(game.activePlayers.toFloat() / 100000f, 10f)
                rating * 0.6f + activityScore * 0.3f + revenueScore * 0.1f
            }
            
            // 特殊成就奖项：直接使用评分
            else -> rating
        }
    }
    
    /**
     * 计算创新分（最佳独立游戏用）
     */
    private fun calculateInnovationScore(game: EligibleGame): Float {
        var score = 0f
        
        // 团队规模小
        if (game.teamSize <= 2) score += 2f
        else if (game.teamSize == 3) score += 1f
        
        // 开发成本低
        if (game.developmentCost < 100000L) score += 1f
        
        // 高评分
        if (game.rating >= 8.5f) score += 2f
        
        return score
    }
    
    /**
     * 计算游戏运营天数
     */
    private fun calculateDaysOnMarket(
        releaseYear: Int,
        releaseMonth: Int,
        releaseDay: Int,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Int {
        if (releaseYear == 0) return 0
        
        val yearDiff = currentYear - releaseYear
        val monthDiff = currentMonth - releaseMonth
        val dayDiff = currentDay - releaseDay
        
        // 简化计算：每年365天，每月30天
        return yearDiff * 365 + monthDiff * 30 + dayDiff
    }
    
    /**
     * 发放奖励给玩家
     * @return 修改后的SaveData
     */
    fun grantAwardsToPlayer(
        saveData: SaveData,
        finalNominations: List<AwardNomination>
    ): SaveData {
        var newMoney = saveData.money
        var newFans = saveData.fans
        var newReputation = saveData.companyReputation
        val newGames = saveData.games.toMutableList()
        
        // 遍历所有奖项，发放给玩家获奖的游戏
        finalNominations.forEach { nomination ->
            val winner = nomination.winner
            if (winner != null && winner.isPlayerGame) {
                val reward = nomination.award.getReward()
                
                // 发放奖金和粉丝
                newMoney += reward.cashPrize
                newFans += reward.fansGain
                
                // 增加声望
                newReputation = newReputation.addReputation(reward.reputationGain)
                
                // 记录获奖历史
                val record = AwardRecord(
                    year = nomination.year,
                    award = nomination.award,
                    gameId = winner.gameId,
                    gameName = winner.gameName,
                    isWinner = true,
                    rewards = reward
                )
                newReputation = newReputation.addAwardRecord(record)
                
                // 为游戏添加奖项标记
                val gameIndex = newGames.indexOfFirst { it.id == winner.gameId }
                if (gameIndex >= 0) {
                    val game = newGames[gameIndex]
                    newGames[gameIndex] = game.copy(
                        awards = game.awards + nomination.award
                    )
                }
            }
            
            // 处理提名奖励（未获奖但进入前3）
            nomination.nominees.forEach { nominee ->
                if (nominee.isPlayerGame && nominee.gameId != winner?.gameId) {
                    val reward = nomination.award.getReward()
                    val nominationReward = AwardReward(
                        cashPrize = (reward.cashPrize * 0.2f).toInt(),
                        fansGain = (reward.fansGain * 0.2f).toLong(),
                        reputationGain = 10
                    )
                    
                    newMoney += nominationReward.cashPrize
                    newFans += nominationReward.fansGain
                    newReputation = newReputation.addReputation(nominationReward.reputationGain)
                    
                    // 记录提名历史
                    val record = AwardRecord(
                        year = nomination.year,
                        award = nomination.award,
                        gameId = nominee.gameId,
                        gameName = nominee.gameName,
                        isWinner = false,
                        rewards = nominationReward
                    )
                    newReputation = newReputation.addAwardRecord(record)
                }
            }
        }
        
        return saveData.copy(
            money = newMoney,
            fans = newFans,
            companyReputation = newReputation,
            games = newGames
        )
    }
}

/**
 * 符合条件的游戏（内部数据结构）
 */
private data class EligibleGame(
    val gameId: String,
    val gameName: String,
    val companyId: Int,
    val companyName: String,
    val theme: com.example.yjcy.ui.GameTheme,
    val platforms: List<Platform>,
    val businessModel: BusinessModel,
    val rating: Float,
    val totalSales: Long,
    val activePlayers: Long,
    val companyFans: Long,
    val isPlayerGame: Boolean,
    val releaseYear: Int,
    val releaseMonth: Int,
    val releaseDay: Int,
    val teamSize: Int,
    val developmentCost: Long,
    val daysOnMarket: Int = 0  // 运营天数
)
