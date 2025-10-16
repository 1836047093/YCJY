package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.Platform
import com.example.yjcy.ui.BusinessModel
import kotlin.math.roundToInt

/**
 * 游戏价格推荐引擎
 * 基于游戏类型、平台、商业模式等因素计算建议价格
 */
object PriceRecommendationEngine {
    
    // 基础价格常量
    private const val BASE_PRICE = 50.0f // 基础价格50元
    private const val MIN_PRICE = 10.0f  // 最低价格10元
    private const val MAX_PRICE = 500.0f // 最高价格500元
    
    // 平台价格系数
    private val platformMultipliers = mapOf(
        Platform.PC to 1.0f,        // PC平台基准
        Platform.MOBILE to 0.6f,    // 手机平台较低
        Platform.CONSOLE to 1.5f,   // 主机平台较高
        Platform.WEB to 0.4f        // 网页平台最低
    )
    
    // 游戏主题受欢迎度系数
    private val themePopularity = mapOf(
        GameTheme.ACTION to 1.2f,      // 动作游戏较受欢迎
        GameTheme.ADVENTURE to 1.0f,   // 冒险游戏中等
        GameTheme.RPG to 1.3f,         // RPG游戏很受欢迎
        GameTheme.STRATEGY to 1.1f,    // 策略游戏较受欢迎
        GameTheme.SIMULATION to 0.9f,  // 模拟游戏稍低
        GameTheme.PUZZLE to 0.8f,      // 解谜游戏较低
        GameTheme.RACING to 1.0f,      // 竞速游戏中等
        GameTheme.SPORTS to 0.9f,      // 体育游戏稍低
        GameTheme.CASUAL to 0.8f,      // 休闲游戏较低
        GameTheme.HORROR to 1.0f,      // 恐怖游戏中等
        GameTheme.SHOOTER to 1.3f,     // 射击游戏很受欢迎
        GameTheme.MOBA to 1.4f         // MOBA游戏非常受欢迎
    )
    
    // 商业模式价格系数
    private val businessModelFactors = mapOf(
        BusinessModel.SINGLE_PLAYER to 1.0f,  // 单机游戏基准价格
        BusinessModel.ONLINE_GAME to 0.8f     // 网络游戏稍低价格
    )
    
    /**
     * 计算游戏的推荐价格
     */
    fun calculateRecommendedPrice(game: Game): PriceRecommendation {
        
        // 计算基础价格
        var calculatedPrice = BASE_PRICE
        
        // 应用平台系数
        val platformMultiplier = game.platforms.maxOfOrNull { platformMultipliers[it] ?: 1.0f } ?: 1.0f
        calculatedPrice *= platformMultiplier
        
        // 应用主题受欢迎度系数
        val themeMultiplier = themePopularity[game.theme] ?: 1.0f
        calculatedPrice *= themeMultiplier
        
        // 应用商业模式系数
        val businessModelMultiplier = businessModelFactors[game.businessModel] ?: 1.0f
        calculatedPrice *= businessModelMultiplier
        
        // 确保价格在合理范围内
        calculatedPrice = calculatedPrice.coerceIn(MIN_PRICE, MAX_PRICE)
        
        // 计算价格区间（±20%）
        val minPrice = (calculatedPrice * 0.8f).coerceAtLeast(MIN_PRICE)
        val maxPrice = (calculatedPrice * 1.2f).coerceAtMost(MAX_PRICE)
        
        // 生成市场分析
        val marketAnalysis = generateMarketAnalysis(game, calculatedPrice, platformMultiplier, themeMultiplier)
        
        return PriceRecommendation(
            gameId = game.id,
            recommendedPrice = calculatedPrice.roundToNearestFive(),
            priceRange = PriceRange(
                minPrice = minPrice.roundToNearestFive(),
                maxPrice = maxPrice.roundToNearestFive(),
                optimalPrice = calculatedPrice.roundToNearestFive()
            ),
            marketAnalysis = marketAnalysis,
            confidence = calculateConfidence(game)
        )
    }
    
    /**
     * 生成市场分析文本
     */
    private fun generateMarketAnalysis(
        game: Game,
        price: Float,
        platformMultiplier: Float,
        themeMultiplier: Float
    ): String {
        val analysis = StringBuilder()
        
        // 平台分析
        when {
            platformMultiplier > 1.2f -> analysis.append("主机平台用户付费意愿较高，")
            platformMultiplier < 0.7f -> analysis.append("移动/网页平台建议采用较低价格策略，")
            else -> analysis.append("PC平台价格适中，")
        }
        
        // 主题分析
        when {
            themeMultiplier > 1.1f -> analysis.append("该游戏类型市场需求旺盛，")
            themeMultiplier < 0.9f -> analysis.append("该游戏类型竞争激烈，建议谨慎定价，")
            else -> analysis.append("该游戏类型市场表现稳定，")
        }
        
        // 商业模式分析
        when (game.businessModel) {
            BusinessModel.SINGLE_PLAYER -> analysis.append("单机游戏适合一次性付费模式。")
            BusinessModel.ONLINE_GAME -> analysis.append("网络游戏可考虑多种变现方式。")
        }
        
        // 价格建议
        when {
            price >= 100f -> analysis.append(" 定价较高，需确保游戏品质。")
            price <= 30f -> analysis.append(" 定价亲民，有利于快速获得用户。")
            else -> analysis.append(" 定价合理，平衡收益与用户接受度。")
        }
        
        return analysis.toString()
    }
    
    /**
     * 计算推荐置信度
     */
    private fun calculateConfidence(game: Game): Float {
        var confidence = 0.8f
        
        // 多平台发布增加置信度
        if (game.platforms.size > 1) {
            confidence += 0.1f
        }
        
        // 热门主题增加置信度
        val themeMultiplier = themePopularity[game.theme] ?: 1.0f
        if (themeMultiplier > 1.1f) {
            confidence += 0.05f
        }
        
        // 确保置信度在合理范围内
        return confidence.coerceIn(0.5f, 1.0f)
    }
    
    /**
     * 将价格四舍五入到最近的5的倍数
     */
    private fun Float.roundToNearestFive(): Float {
        return (this / 5.0f).roundToInt() * 5.0f
    }
    
    /**
     * 获取市场因素配置
     */
    fun getMarketFactors(): MarketFactors {
        return MarketFactors(
            platformMultipliers = platformMultipliers,
            themePopularity = themePopularity,
            businessModelFactors = businessModelFactors
        )
    }
    
    /**
     * 计算网络游戏的付费内容推荐价格
     */
    fun calculateMonetizationRecommendation(game: Game): MonetizationRecommendation {
        // 基础道具价格
        var baseItemPrice = 6.0f
        
        // 应用平台系数
        val platformMultiplier = game.platforms.maxOfOrNull { platformMultipliers[it] ?: 1.0f } ?: 1.0f
        baseItemPrice *= platformMultiplier
        
        // 应用主题受欢迎度系数
        val themeMultiplier = themePopularity[game.theme] ?: 1.0f
        baseItemPrice *= themeMultiplier
        
        // 道具价格梯度
        val lowTier = (baseItemPrice * 0.5f).coerceAtLeast(1.0f).roundToNearestOne()
        val midTier = baseItemPrice.roundToNearestOne()
        val highTier = (baseItemPrice * 3.0f).roundToNearestFive()
        
        // VIP价格梯度
        val monthlyVip = (baseItemPrice * 4.0f).roundToNearestFive()
        val seasonalVip = (monthlyVip * 2.5f).roundToNearestFive()
        val yearlyVip = (monthlyVip * 8.0f).roundToNearestTen()
        
        // 生成市场分析
        val marketAnalysis = generateMonetizationAnalysis(game, platformMultiplier)
        
        return MonetizationRecommendation(
            gameId = game.id,
            itemPrices = ItemPriceRecommendation(
                lowTier = lowTier,
                midTier = midTier,
                highTier = highTier
            ),
            vipPrices = VipPriceRecommendation(
                monthly = monthlyVip,
                seasonal = seasonalVip,
                yearly = yearlyVip
            ),
            marketAnalysis = marketAnalysis,
            confidence = calculateConfidence(game)
        )
    }
    
    /**
     * 生成网络游戏付费内容市场分析
     */
    private fun generateMonetizationAnalysis(
        game: Game,
        platformMultiplier: Float
    ): String {
        val analysis = StringBuilder()
        
        // 平台分析
        when {
            platformMultiplier > 1.2f -> analysis.append("主机平台用户付费率较高，可采用中高价策略。")
            platformMultiplier < 0.7f -> analysis.append("移动/网页平台用户量大，建议小额高频付费。")
            else -> analysis.append("PC平台付费能力适中，平衡定价策略。")
        }
        
        // 主题分析
        when (game.theme) {
            GameTheme.ACTION -> analysis.append("动作游戏适合皮肤、特效等视觉类付费内容。")
            GameTheme.RPG -> analysis.append("RPG游戏适合装备、道具等成长类付费内容。")
            GameTheme.STRATEGY -> analysis.append("策略游戏适合加速、资源等便利类付费内容。")
            GameTheme.CASUAL -> analysis.append("休闲游戏适合去广告、解锁关卡等功能性付费。")
            GameTheme.SHOOTER -> analysis.append("射击游戏适合武器皮肤、战斗通行证等付费内容，战利品箱模式表现优异。")
            GameTheme.MOBA -> analysis.append("MOBA游戏适合英雄角色、皮肤等付费内容，赛季通行证模式极受欢迎。")
            else -> analysis.append("该类型游戏可多元化付费内容设计。")
        }
        
        analysis.append("建议同时提供道具和VIP订阅，满足不同玩家需求。")
        
        return analysis.toString()
    }
    
    /**
     * 将价格四舍五入到最近的1元
     */
    private fun Float.roundToNearestOne(): Float {
        return this.roundToInt().toFloat()
    }
    
    /**
     * 将价格四舍五入到最近的10元
     */
    private fun Float.roundToNearestTen(): Float {
        return (this / 10.0f).roundToInt() * 10.0f
    }
}