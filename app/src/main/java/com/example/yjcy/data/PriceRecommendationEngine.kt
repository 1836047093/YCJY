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
        GameTheme.HORROR to 1.0f       // 恐怖游戏中等
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
}