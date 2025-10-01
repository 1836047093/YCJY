package com.example.yjcy.data

/**
 * 收益计算器
 * 根据游戏评分调整游戏收益
 */
class RevenueCalculator {
    
    /**
     * 根据游戏评分计算调整后的收益
     * @param baseRevenue 基础收益
     * @param rating 游戏评分(0-10)
     * @return Long 调整后的收益
     */
    fun calculateAdjustedRevenue(baseRevenue: Long, rating: Float): Long {
        // 评分影响收益的倍数：
        // 9-10分: 1.5倍
        // 7-8.9分: 1.2倍
        // 5-6.9分: 1.0倍
        // 0-4.9分: 0.7倍
        val multiplier = when {
            rating >= 9.0f -> 1.5f
            rating >= 7.0f -> 1.2f
            rating >= 5.0f -> 1.0f
            else -> 0.7f
        }
        
        return (baseRevenue * multiplier).toLong()
    }
    
    /**
     * 获取评分对应的收益倍数
     * @param rating 游戏评分
     * @return Float 收益倍数
     */
    fun getRevenueMultiplier(rating: Float): Float {
        return when {
            rating >= 9.0f -> 1.5f
            rating >= 7.0f -> 1.2f
            rating >= 5.0f -> 1.0f
            else -> 0.7f
        }
    }
    
    /**
     * 获取评分等级描述
     * @param rating 游戏评分
     * @return String 评分等级描述
     */
    fun getRatingDescription(rating: Float): String {
        return when {
            rating >= 9.0f -> "杰作 (${String.format("%.1f", rating)}/10)"
            rating >= 7.0f -> "优秀 (${String.format("%.1f", rating)}/10)"
            rating >= 5.0f -> "良好 (${String.format("%.1f", rating)}/10)"
            else -> "一般 (${String.format("%.1f", rating)}/10)"
        }
    }
    
    /**
     * 预览收益调整效果
     * @param baseRevenue 基础收益
     * @param rating 游戏评分
     * @return RevenuePreview 收益预览信息
     */
    fun previewRevenueAdjustment(baseRevenue: Long, rating: Float): RevenuePreview {
        val adjustedRevenue = calculateAdjustedRevenue(baseRevenue, rating)
        val multiplier = getRevenueMultiplier(rating)
        val description = getRatingDescription(rating)
        
        return RevenuePreview(
            baseRevenue = baseRevenue,
            adjustedRevenue = adjustedRevenue,
            multiplier = multiplier,
            ratingDescription = description,
            bonusRevenue = adjustedRevenue - baseRevenue
        )
    }
}

/**
 * 收益预览数据类
 */
data class RevenuePreview(
    val baseRevenue: Long,      // 基础收益
    val adjustedRevenue: Long,  // 调整后收益
    val multiplier: Float,      // 收益倍数
    val ratingDescription: String, // 评分描述
    val bonusRevenue: Long      // 额外收益
)