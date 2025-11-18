package com.example.yjcy.managers.esports

import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.*
import java.util.Date
import kotlin.random.Random

/**
 * 选手管理器
 */
object PlayerManager {
    private var _players = mutableListOf<EsportsPlayer>()
    val players: List<EsportsPlayer> get() = _players
    
    private var _myTeam = mutableListOf<EsportsPlayer>()
    val myTeam: List<EsportsPlayer> get() = _myTeam
    
    // 中文姓氏和名字
    private val firstNames = listOf(
        "李", "王", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"
    )
    
    private val lastNames = listOf(
        "伟", "强", "明", "华", "鹏", "军", "杰", "峰", "磊", "涛",
        "浩", "博", "宇", "辉", "刚", "健", "斌", "勇", "超", "俊"
    )
    
    /**
     * 初始化选手系统
     */
    fun initialize(savedPlayers: List<EsportsPlayer>?, myTeamIds: List<String>?) {
        _players = savedPlayers?.toMutableList() ?: mutableListOf()
        _myTeam = myTeamIds?.mapNotNull { id ->
            _players.find { it.id == id }
        }?.toMutableList() ?: mutableListOf()
        
        android.util.Log.d("PlayerManager", "初始化完成，选手数: ${_players.size}, 我的战队: ${_myTeam.size}")
    }
    
    /**
     * 招募选手（青训营）
     */
    fun recruitPlayer(rarity: PlayerRarity? = null, position: HeroPosition? = null): EsportsPlayer {
        val actualRarity = rarity ?: rollRarity()
        val player = generatePlayer(actualRarity, position)
        _players.add(player)
        android.util.Log.d("PlayerManager", "招募选手: ${player.name} (${actualRarity.displayName}) - ${position?.name ?: "随机位置"}")
        return player
    }
    
    /**
     * 随机品质（基于概率）
     */
    private fun rollRarity(): PlayerRarity {
        val roll = Random.nextDouble()
        return when {
            roll < 0.001 -> PlayerRarity.SSR  // 0.1%
            roll < 0.01 -> PlayerRarity.S     // 0.9%
            roll < 0.05 -> PlayerRarity.A     // 4%
            roll < 0.20 -> PlayerRarity.B     // 15%
            else -> PlayerRarity.C            // 80%
        }
    }
    
    /**
     * 生成选手
     */
    private fun generatePlayer(
        rarity: PlayerRarity,
        position: HeroPosition? = null
    ): EsportsPlayer {
        val actualPosition = position ?: HeroPosition.values().random()
        val attributes = generateAttributes(rarity)
        val heroPool = generateHeroPool(actualPosition, rarity)
        
        return EsportsPlayer(
            id = "player_${System.currentTimeMillis()}_${Random.nextInt(9999)}",
            name = "${firstNames.random()}${lastNames.random()}",
            rarity = rarity,
            position = actualPosition,
            age = Random.nextInt(16, 22),
            nationality = "中国",
            attributes = attributes,
            heroPool = heroPool,
            championHeroes = heroPool.take(minOf(3, heroPool.size)).map { it.heroId },
            careerStats = CareerStats(0, 0, 3.0, 0, mutableListOf(), 1500),
            contract = generateContract(rarity),
            form = Random.nextInt(70, 100),
            morale = 80,
            stamina = 100,
            injury = null,
            personality = PlayerPersonality.values().random(),
            achievements = mutableListOf()
        )
    }
    
    /**
     * 生成属性
     */
    private fun generateAttributes(rarity: PlayerRarity): PlayerAttributes {
        val base = Random.nextInt(
            rarity.baseAttributeRange.first,
            rarity.baseAttributeRange.last + 1
        )
        
        return PlayerAttributes(
            mechanics = (base + Random.nextInt(-5, 6)).coerceIn(1, 100),
            awareness = (base + Random.nextInt(-5, 6)).coerceIn(1, 100),
            teamwork = (base + Random.nextInt(-5, 6)).coerceIn(1, 100),
            mentality = (base + Random.nextInt(-5, 6)).coerceIn(1, 100),
            heroMastery = (base + Random.nextInt(-5, 6)).coerceIn(1, 100)
        )
    }
    
    /**
     * 生成英雄池
     */
    private fun generateHeroPool(
        position: HeroPosition,
        rarity: PlayerRarity
    ): MutableList<HeroMastery> {
        val positionHeroes = HeroManager.getHeroesByPosition(position)
        val poolSize = when (rarity) {
            PlayerRarity.C -> Random.nextInt(3, 6)
            PlayerRarity.B -> Random.nextInt(4, 8)
            PlayerRarity.A -> Random.nextInt(6, 10)
            PlayerRarity.S -> Random.nextInt(8, 13)
            PlayerRarity.SSR -> Random.nextInt(10, 16)
        }
        
        return positionHeroes.shuffled().take(poolSize.coerceAtMost(positionHeroes.size)).map { hero ->
            val baseProficiency = when (rarity) {
                PlayerRarity.C -> Random.nextInt(20, 40)
                PlayerRarity.B -> Random.nextInt(30, 50)
                PlayerRarity.A -> Random.nextInt(40, 60)
                PlayerRarity.S -> Random.nextInt(50, 70)
                PlayerRarity.SSR -> Random.nextInt(60, 80)
            }
            HeroMastery(
                heroId = hero.id,
                proficiency = baseProficiency,
                gamesPlayed = 0,
                winRate = 0.5
            )
        }.toMutableList()
    }
    
    /**
     * 生成合同
     */
    private fun generateContract(rarity: PlayerRarity): PlayerContract {
        return PlayerContract(
            startDate = Date(),
            endDate = Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000 * 2),
            monthlySalary = rarity.monthlySalary,
            buyoutClause = rarity.monthlySalary * 24,
            bonusClause = ContractBonus(
                championshipBonus = rarity.monthlySalary * 10,
                mvpBonus = rarity.monthlySalary * 3,
                performanceBonus = rarity.monthlySalary * 2
            )
        )
    }
    
    /**
     * 签约选手
     */
    fun signPlayer(playerId: String): Boolean {
        val player = _players.find { it.id == playerId } ?: return false
        if (_myTeam.size >= 7) {
            android.util.Log.w("PlayerManager", "战队已满（7人）")
            return false
        }
        _myTeam.add(player)
        android.util.Log.d("PlayerManager", "签约选手: ${player.name}")
        return true
    }
    
    /**
     * 解约选手
     */
    fun releasePlayer(playerId: String): Boolean {
        val player = _myTeam.find { it.id == playerId } ?: return false
        _myTeam.remove(player)
        android.util.Log.d("PlayerManager", "解约选手: ${player.name}")
        return true
    }
    
    /**
     * 获取指定位置的选手
     */
    fun getPlayersByPosition(position: HeroPosition): List<EsportsPlayer> {
        return _myTeam.filter { it.position == position }
    }
    
    /**
     * 更新选手状态（每日）
     */
    fun updateDailyStatus() {
        _myTeam.forEach { player ->
            // 恢复体力
            player.stamina = (player.stamina + 5).coerceAtMost(100)
            
            // 伤病恢复
            player.injury?.let { injury ->
                // 这里简化处理，实际应该有恢复天数计数
                if (Random.nextDouble() < 0.1) {  // 10%概率恢复
                    player.injury = null
                    android.util.Log.d("PlayerManager", "${player.name} 伤病已恢复")
                }
            }
        }
    }
}
