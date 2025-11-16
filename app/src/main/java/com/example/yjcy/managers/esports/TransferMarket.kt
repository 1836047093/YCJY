package com.example.yjcy.managers.esports

import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.EsportsPlayer
import com.example.yjcy.data.esports.PlayerRarity
import kotlin.random.Random

/**
 * 转会市场
 * 管理选手的买卖和自由市场
 */
object TransferMarket {
    
    /**
     * 转会列表
     */
    data class Transfer(
        val player: EsportsPlayer,
        val listingDate: Int,  // 挂牌日期
        val askingPrice: Long,  // 要价
        val isNegotiable: Boolean = true  // 是否可议价
    )
    
    private val _listings = mutableListOf<Transfer>()
    val listings: List<Transfer> get() = _listings
    
    /**
     * 挂牌选手
     */
    fun listPlayer(
        player: EsportsPlayer,
        currentDay: Int,
        askingPrice: Long? = null
    ): Boolean {
        // 检查是否已挂牌
        if (_listings.any { it.player.id == player.id }) {
            android.util.Log.w("TransferMarket", "${player.name}已在转会市场")
            return false
        }
        
        val price = askingPrice ?: calculatePlayerValue(player)
        
        val transfer = Transfer(
            player = player,
            listingDate = currentDay,
            askingPrice = price
        )
        
        _listings.add(transfer)
        
        android.util.Log.d("TransferMarket", 
            "${player.name}挂牌，要价${price / 10000}万")
        
        return true
    }
    
    /**
     * 计算选手身价
     */
    fun calculatePlayerValue(player: EsportsPlayer): Long {
        // 基础身价（基于品质）
        val baseValue = when (player.rarity) {
            PlayerRarity.C -> 50_000L
            PlayerRarity.B -> 200_000L
            PlayerRarity.A -> 500_000L
            PlayerRarity.S -> 2_000_000L
            PlayerRarity.SSR -> 10_000_000L
        }
        
        // 综合能力加成
        val overallRating = player.attributes.overallRating()
        val abilityMultiplier = (overallRating / 50.0)  // 50分为基准
        
        // 年龄影响
        val ageMultiplier = when (player.age) {
            in 16..20 -> 1.5  // 新秀溢价
            in 21..24 -> 1.2  // 巅峰期
            in 25..26 -> 1.0  // 正常
            in 27..28 -> 0.8  // 开始贬值
            else -> 0.5       // 老将
        }
        
        // 英雄池影响
        val heroPoolBonus = player.heroPool.size * 5000L
        
        // 成就影响
        val achievementBonus = player.careerStats.mvpCount * 100_000L
        
        val totalValue = (baseValue * abilityMultiplier * ageMultiplier).toLong() +
                        heroPoolBonus + achievementBonus
        
        return totalValue.coerceAtLeast(10_000L)
    }
    
    /**
     * 购买选手
     */
    fun buyPlayer(
        transfer: Transfer,
        offerPrice: Long,
        buyer: String = "player"
    ): Pair<Boolean, String> {
        // 检查报价
        if (offerPrice < transfer.askingPrice * 0.8) {
            return false to "报价过低，至少需要${(transfer.askingPrice * 0.8 / 10000).toLong()}万"
        }
        
        // 议价成功率
        val successChance = when {
            offerPrice >= transfer.askingPrice -> 1.0  // 满足要价
            offerPrice >= transfer.askingPrice * 0.95 -> 0.9  // 95%以上
            offerPrice >= transfer.askingPrice * 0.9 -> 0.7   // 90%以上
            else -> 0.5  // 80-90%
        }
        
        if (Random.nextDouble() > successChance && transfer.isNegotiable) {
            return false to "转会失败，对方拒绝了报价"
        }
        
        // 转会成功
        _listings.remove(transfer)
        
        android.util.Log.d("TransferMarket", 
            "$buyer 以${offerPrice / 10000}万签下${transfer.player.name}")
        
        return true to "转会成功！"
    }
    
    /**
     * 取消挂牌
     */
    fun cancelListing(playerId: String): Boolean {
        val transfer = _listings.find { it.player.id == playerId }
        return if (transfer != null) {
            _listings.remove(transfer)
            android.util.Log.d("TransferMarket", "取消挂牌: ${transfer.player.name}")
            true
        } else {
            false
        }
    }
    
    /**
     * 每日更新转会市场
     */
    fun updateDaily(currentDay: Int) {
        // 清理超过30天的挂牌
        val expired = _listings.filter { currentDay - it.listingDate > 30 }
        expired.forEach {
            _listings.remove(it)
            android.util.Log.d("TransferMarket", "${it.player.name}挂牌过期")
        }
        
        // AI战队可能挂牌或购买选手（简化实现）
    }
    
    /**
     * 按品质筛选
     */
    fun filterByRarity(rarity: PlayerRarity): List<Transfer> {
        return _listings.filter { it.player.rarity == rarity }
    }
    
    /**
     * 按位置筛选
     */
    fun filterByPosition(position: HeroPosition): List<Transfer> {
        return _listings.filter { it.player.position == position }
    }
    
    /**
     * 按位置名称筛选（UI辅助函数）
     */
    fun filterByPositionName(positionName: String): List<Transfer> {
        val position = when(positionName) {
            "上单" -> HeroPosition.TOP
            "打野" -> HeroPosition.JUNGLE
            "中单" -> HeroPosition.MID
            "ADC" -> HeroPosition.ADC
            "辅助" -> HeroPosition.SUPPORT
            else -> return listings
        }
        return filterByPosition(position)
    }
    
    /**
     * 获取所有位置名称列表
     */
    fun getAllPositionNames(): List<String> {
        return listOf("上单", "打野", "中单", "ADC", "辅助")
    }
    
    /**
     * 按价格排序
     */
    fun sortByPrice(ascending: Boolean = true): List<Transfer> {
        return if (ascending) {
            _listings.sortedBy { it.askingPrice }
        } else {
            _listings.sortedByDescending { it.askingPrice }
        }
    }
    
    /**
     * 按能力排序
     */
    fun sortByAbility(ascending: Boolean = false): List<Transfer> {
        return if (ascending) {
            _listings.sortedBy { it.player.attributes.overallRating() }
        } else {
            _listings.sortedByDescending { it.player.attributes.overallRating() }
        }
    }
    
    /**
     * 推荐选手（基于需求）
     */
    fun recommendPlayers(
        position: HeroPosition,
        maxBudget: Long
    ): List<Transfer> {
        return _listings
            .filter { it.player.position == position }
            .filter { it.askingPrice <= maxBudget }
            .sortedByDescending { 
                it.player.attributes.overallRating() / (it.askingPrice / 1000.0) 
            }  // 性价比排序
            .take(5)
    }
    
    /**
     * 生成自由市场（随机选手）
     */
    fun generateFreeAgents(count: Int, currentDay: Int) {
        repeat(count) {
            val player = PlayerManager.recruitPlayer()
            val price = calculatePlayerValue(player) * Random.nextDouble(0.8, 1.2).toLong()
            
            val transfer = Transfer(
                player = player,
                listingDate = currentDay,
                askingPrice = price,
                isNegotiable = true
            )
            
            _listings.add(transfer)
        }
        
        android.util.Log.d("TransferMarket", "生成${count}名自由球员")
    }
    
    /**
     * 清空市场（新赛季）
     */
    fun clearMarket() {
        _listings.clear()
        android.util.Log.d("TransferMarket", "转会市场已清空")
    }
    
    /**
     * 获取市场统计
     */
    fun getMarketStats(): Map<String, Any> {
        return mapOf(
            "total" to _listings.size,
            "avgPrice" to if (_listings.isEmpty()) 0L 
                         else _listings.map { it.askingPrice }.average().toLong(),
            "highestPrice" to (_listings.maxByOrNull { it.askingPrice }?.askingPrice ?: 0L),
            "lowestPrice" to (_listings.minByOrNull { it.askingPrice }?.askingPrice ?: 0L)
        )
    }
}
