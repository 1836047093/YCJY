package com.example.yjcy.managers.esports

import com.example.yjcy.data.esports.EsportsPlayer
import com.example.yjcy.data.esports.InjuryStatus
import kotlin.random.Random

/**
 * 状态管理器
 * 管理选手的体力、士气、伤病等状态
 */
object StatusManager {
    
    /**
     * 每日更新所有选手状态
     */
    fun updateDailyStatus(players: List<EsportsPlayer>) {
        players.forEach { player ->
            // 1. 恢复体力
            recoverStamina(player)
            
            // 2. 恢复士气
            updateMorale(player)
            
            // 3. 更新状态值
            updateForm(player)
            
            // 4. 处理伤病
            updateInjury(player)
            
            // 5. 年龄影响
            applyAgeEffect(player)
        }
    }
    
    /**
     * 恢复体力
     */
    private fun recoverStamina(player: EsportsPlayer) {
        if (player.stamina < 100) {
            // 基础恢复
            var recovery = 10
            
            // 年龄影响
            if (player.age > 25) {
                recovery -= (player.age - 25) / 2  // 年龄越大恢复越慢
            }
            
            // 伤病影响
            if (player.injury != null) {
                recovery /= 2
            }
            
            player.stamina = (player.stamina + recovery).coerceAtMost(100)
        }
    }
    
    /**
     * 更新士气
     */
    private fun updateMorale(player: EsportsPlayer) {
        // 士气自然恢复到80
        if (player.morale < 80) {
            player.morale = (player.morale + 2).coerceAtMost(80)
        } else if (player.morale > 80) {
            player.morale = (player.morale - 1).coerceAtLeast(80)
        }
    }
    
    /**
     * 更新状态值
     */
    private fun updateForm(player: EsportsPlayer) {
        // 状态会波动
        val change = Random.nextInt(-3, 4)
        player.form = (player.form + change).coerceIn(50, 100)
    }
    
    /**
     * 更新伤病
     */
    private fun updateInjury(player: EsportsPlayer) {
        player.injury?.let { injury ->
            // 恢复天数+1
            val updatedInjury = injury.copy(
                recoveryDays = injury.recoveryDays - 1
            )
            
            if (updatedInjury.recoveryDays <= 0) {
                // 伤病痊愈
                player.injury = null
                android.util.Log.d("StatusManager", "${player.name}伤病痊愈")
            } else {
                player.injury = updatedInjury
            }
        }
    }
    
    /**
     * 年龄影响
     */
    private fun applyAgeEffect(player: EsportsPlayer) {
        // 巅峰期：20-24岁
        // 衰退期：25岁+
        if (player.age >= 28) {
            // 属性下降（极小概率）
            if (Random.nextDouble() < 0.01) {  // 1%概率
                val attr = Random.nextInt(4)
                when (attr) {
                    0 -> player.attributes.mechanics = 
                        (player.attributes.mechanics - 1).coerceAtLeast(1)
                    1 -> player.attributes.awareness = 
                        (player.attributes.awareness - 1).coerceAtLeast(1)
                    2 -> player.attributes.teamwork = 
                        (player.attributes.teamwork - 1).coerceAtLeast(1)
                    3 -> player.attributes.mentality = 
                        (player.attributes.mentality - 1).coerceAtLeast(1)
                }
            }
        }
    }
    
    /**
     * 比赛后更新（消耗）
     */
    fun afterMatch(player: EsportsPlayer, won: Boolean, isMVP: Boolean) {
        // 消耗体力
        player.stamina = (player.stamina - 15).coerceAtLeast(0)
        
        // 更新士气
        if (won) {
            player.morale = (player.morale + 5).coerceAtMost(100)
        } else {
            player.morale = (player.morale - 5).coerceAtLeast(0)
        }
        
        // MVP额外士气
        if (isMVP) {
            player.morale = (player.morale + 5).coerceAtMost(100)
        }
        
        // 伤病风险（低体力高风险）
        if (player.stamina < 30) {
            val injuryChance = (30 - player.stamina) / 100.0  // 最高30%
            if (Random.nextDouble() < injuryChance) {
                causeInjury(player)
            }
        }
    }
    
    /**
     * 训练后更新
     */
    fun afterTraining(player: EsportsPlayer) {
        // 消耗体力
        player.stamina = (player.stamina - 10).coerceAtLeast(0)
        
        // 轻微提升士气
        player.morale = (player.morale + 1).coerceAtMost(100)
    }
    
    /**
     * 造成伤病
     */
    private fun causeInjury(player: EsportsPlayer) {
        if (player.injury != null) return  // 已有伤病
        
        val severity = when (Random.nextInt(100)) {
            in 0..59 -> InjuryStatus.InjurySeverity.MINOR    // 60% 轻伤
            in 60..89 -> InjuryStatus.InjurySeverity.MODERATE // 30% 中伤
            else -> InjuryStatus.InjurySeverity.SEVERE        // 10% 重伤
        }
        
        val recoveryDays = when (severity) {
            InjuryStatus.InjurySeverity.MINOR -> Random.nextInt(3, 8)
            InjuryStatus.InjurySeverity.MODERATE -> Random.nextInt(7, 15)
            InjuryStatus.InjurySeverity.SEVERE -> Random.nextInt(14, 31)
        }
        
        player.injury = InjuryStatus(
            severity = severity,
            recoveryDays = recoveryDays,
            affectedAttribute = null  // 简化实现
        )
        
        android.util.Log.d("StatusManager", 
            "${player.name}受伤(${severity.displayName})，需要${recoveryDays}天恢复")
    }
    
    /**
     * 强制休息（快速恢复）
     */
    fun rest(player: EsportsPlayer, days: Int) {
        repeat(days) {
            player.stamina = (player.stamina + 20).coerceAtMost(100)
            player.morale = (player.morale + 5).coerceAtMost(100)
        }
        android.util.Log.d("StatusManager", "${player.name}休息${days}天")
    }
    
    /**
     * 治疗伤病（加速恢复）
     */
    fun treatInjury(player: EsportsPlayer): Boolean {
        val injury = player.injury ?: return false
        
        // 减少恢复天数
        val reducedDays = (injury.recoveryDays * 0.5).toInt()
        player.injury = injury.copy(recoveryDays = reducedDays.coerceAtLeast(1))
        
        android.util.Log.d("StatusManager", 
            "${player.name}接受治疗，恢复时间减少至${reducedDays}天")
        
        return true
    }
    
    /**
     * 激励选手（提升士气）
     */
    fun motivate(player: EsportsPlayer, amount: Int) {
        player.morale = (player.morale + amount).coerceAtMost(100)
        android.util.Log.d("StatusManager", "${player.name}士气提升${amount}")
    }
    
    /**
     * 检查选手是否可以比赛
     */
    fun canPlay(player: EsportsPlayer): Boolean {
        // 有伤病不能上场
        if (player.injury != null) {
            android.util.Log.w("StatusManager", "${player.name}有伤病，无法比赛")
            return false
        }
        
        // 体力过低不建议上场
        if (player.stamina < 20) {
            android.util.Log.w("StatusManager", "${player.name}体力过低，不建议比赛")
            return false
        }
        
        return true
    }
    
    /**
     * 获取选手状态评级
     */
    fun getStatusRating(player: EsportsPlayer): String {
        val avgStatus = (player.stamina + player.morale + player.form) / 3.0
        return when {
            avgStatus >= 90 -> "🟢 极佳"
            avgStatus >= 75 -> "🔵 良好"
            avgStatus >= 60 -> "🟡 一般"
            avgStatus >= 40 -> "🟠 较差"
            else -> "🔴 糟糕"
        }
    }
    
    /**
     * 批量更新战队状态
     */
    fun updateTeamStatus(players: List<EsportsPlayer>) {
        updateDailyStatus(players)
    }
}
