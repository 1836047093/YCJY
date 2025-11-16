package com.example.yjcy.managers.esports

import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.EsportsPlayer
import com.example.yjcy.data.esports.HeroMastery
import kotlin.random.Random

/**
 * 训练管理器
 * 管理选手的属性训练和英雄熟练度提升
 */
object TrainingManager {
    
    /**
     * 训练类型
     */
    enum class TrainingType(
        val displayName: String,
        val description: String,
        val cost: Long,
        val duration: Int,  // 天数
        val emoji: String
    ) {
        MECHANICS("操作训练", "提升选手的微操和反应速度", 50_000, 7, "🎮"),
        AWARENESS("意识训练", "提升选手的大局观和决策能力", 50_000, 7, "🧠"),
        TEAMWORK("团队训练", "提升选手的配合和沟通能力", 50_000, 7, "🤝"),
        MENTALITY("心态训练", "提升选手的抗压和稳定性", 50_000, 7, "💪"),
        HERO_MASTERY("英雄训练", "提升选手对特定英雄的熟练度", 30_000, 3, "⚔️"),
        COMPREHENSIVE("综合训练", "全面提升选手各项能力", 200_000, 14, "🌟")
    }
    
    /**
     * 训练记录
     */
    data class TrainingSession(
        val playerId: String,
        val type: TrainingType,
        val startDay: Int,
        val endDay: Int,
        val targetHeroId: String? = null  // 仅英雄训练需要
    )
    
    private val _activeSessions = mutableMapOf<String, TrainingSession>()
    val activeSessions: Map<String, TrainingSession> get() = _activeSessions
    
    /**
     * 开始训练
     */
    fun startTraining(
        player: EsportsPlayer,
        type: TrainingType,
        currentDay: Int,
        targetHeroId: String? = null
    ): Boolean {
        // 检查是否已在训练中
        if (_activeSessions.containsKey(player.id)) {
            android.util.Log.w("TrainingManager", "${player.name}已在训练中")
            return false
        }
        
        // 检查体力
        if (player.stamina < 50) {
            android.util.Log.w("TrainingManager", "${player.name}体力不足")
            return false
        }
        
        // 英雄训练需要指定英雄
        if (type == TrainingType.HERO_MASTERY && targetHeroId == null) {
            android.util.Log.w("TrainingManager", "英雄训练需要指定英雄")
            return false
        }
        
        // 创建训练记录
        val session = TrainingSession(
            playerId = player.id,
            type = type,
            startDay = currentDay,
            endDay = currentDay + type.duration,
            targetHeroId = targetHeroId
        )
        
        _activeSessions[player.id] = session
        
        android.util.Log.d("TrainingManager", 
            "${player.name}开始${type.displayName}，预计${type.duration}天完成")
        
        return true
    }
    
    /**
     * 每日更新训练进度
     */
    fun updateDailyProgress(currentDay: Int) {
        val completed = mutableListOf<String>()
        
        _activeSessions.forEach { (playerId, session) ->
            if (currentDay >= session.endDay) {
                // 训练完成
                completed.add(playerId)
                
                // 应用训练效果
                val player = PlayerManager.players.find { it.id == playerId }
                if (player != null) {
                    applyTrainingEffect(player, session)
                    android.util.Log.d("TrainingManager", 
                        "${player.name}完成${session.type.displayName}")
                }
            }
        }
        
        // 移除已完成的训练
        completed.forEach { _activeSessions.remove(it) }
    }
    
    /**
     * 应用训练效果
     */
    private fun applyTrainingEffect(player: EsportsPlayer, session: TrainingSession) {
        when (session.type) {
            TrainingType.MECHANICS -> {
                val gain = calculateGain(player.attributes.mechanics)
                player.attributes.mechanics = (player.attributes.mechanics + gain)
                    .coerceIn(1, 100)
                android.util.Log.d("TrainingManager", "操作 +$gain")
            }
            
            TrainingType.AWARENESS -> {
                val gain = calculateGain(player.attributes.awareness)
                player.attributes.awareness = (player.attributes.awareness + gain)
                    .coerceIn(1, 100)
                android.util.Log.d("TrainingManager", "意识 +$gain")
            }
            
            TrainingType.TEAMWORK -> {
                val gain = calculateGain(player.attributes.teamwork)
                player.attributes.teamwork = (player.attributes.teamwork + gain)
                    .coerceIn(1, 100)
                android.util.Log.d("TrainingManager", "团队 +$gain")
            }
            
            TrainingType.MENTALITY -> {
                val gain = calculateGain(player.attributes.mentality)
                player.attributes.mentality = (player.attributes.mentality + gain)
                    .coerceIn(1, 100)
                android.util.Log.d("TrainingManager", "心态 +$gain")
            }
            
            TrainingType.HERO_MASTERY -> {
                session.targetHeroId?.let { heroId ->
                    val mastery = player.heroPool.find { it.heroId == heroId }
                    if (mastery != null) {
                        val gain = calculateHeroGain(mastery.proficiency)
                        mastery.proficiency = (mastery.proficiency + gain).coerceIn(0, 100)
                        android.util.Log.d("TrainingManager", "英雄熟练度 +$gain")
                    }
                }
            }
            
            TrainingType.COMPREHENSIVE -> {
                // 全属性小幅提升
                val gain = Random.nextInt(1, 4)
                player.attributes.mechanics = (player.attributes.mechanics + gain)
                    .coerceIn(1, 100)
                player.attributes.awareness = (player.attributes.awareness + gain)
                    .coerceIn(1, 100)
                player.attributes.teamwork = (player.attributes.teamwork + gain)
                    .coerceIn(1, 100)
                player.attributes.mentality = (player.attributes.mentality + gain)
                    .coerceIn(1, 100)
                android.util.Log.d("TrainingManager", "全属性 +$gain")
            }
        }
        
        // 训练消耗体力
        player.stamina = (player.stamina - 20).coerceAtLeast(0)
    }
    
    /**
     * 计算属性增长（递减收益）
     */
    private fun calculateGain(currentValue: Int): Int {
        return when {
            currentValue < 50 -> Random.nextInt(3, 6)   // 低属性：3-5
            currentValue < 70 -> Random.nextInt(2, 5)   // 中属性：2-4
            currentValue < 85 -> Random.nextInt(1, 3)   // 高属性：1-2
            else -> if (Random.nextDouble() < 0.3) 1 else 0  // 顶尖：30%概率+1
        }
    }
    
    /**
     * 计算英雄熟练度增长
     */
    private fun calculateHeroGain(currentProficiency: Int): Int {
        return when {
            currentProficiency < 50 -> Random.nextInt(8, 13)   // 8-12
            currentProficiency < 70 -> Random.nextInt(5, 9)    // 5-8
            currentProficiency < 85 -> Random.nextInt(3, 6)    // 3-5
            else -> Random.nextInt(1, 3)                        // 1-2
        }
    }
    
    /**
     * 取消训练
     */
    fun cancelTraining(playerId: String): Boolean {
        return if (_activeSessions.remove(playerId) != null) {
            android.util.Log.d("TrainingManager", "取消训练: $playerId")
            true
        } else {
            false
        }
    }
    
    /**
     * 获取选手训练状态
     */
    fun getTrainingStatus(playerId: String): TrainingSession? {
        return _activeSessions[playerId]
    }
    
    /**
     * 检查选手是否在训练
     */
    fun isTraining(playerId: String): Boolean {
        return _activeSessions.containsKey(playerId)
    }
    
    /**
     * 批量训练（整个战队）
     */
    fun startTeamTraining(
        players: List<EsportsPlayer>,
        type: TrainingType,
        currentDay: Int
    ): Int {
        var successCount = 0
        players.forEach { player ->
            if (startTraining(player, type, currentDay)) {
                successCount++
            }
        }
        return successCount
    }
}
