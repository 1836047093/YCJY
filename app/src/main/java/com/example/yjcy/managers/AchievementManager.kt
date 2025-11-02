package com.example.yjcy.managers

import com.example.yjcy.data.*
import com.example.yjcy.ui.BusinessModel

/**
 * 成就管理器
 * 负责检测和解锁成就
 */
object AchievementManager {
    
    /**
     * 检查并解锁成就
     * @return 新解锁的成就列表
     */
    fun checkAndUnlockAchievements(
        saveData: SaveData,
        revenueData: Map<String, GameRevenue>
    ): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        val unlockedIds = saveData.unlockedAchievements.map { it.achievementId }.toSet()
        
        // 检查每个成就
        for (achievement in Achievements.ALL_ACHIEVEMENTS) {
            // 跳过已解锁的成就
            if (achievement.id in unlockedIds) continue
            
            // 检查是否达成
            if (isAchievementUnlocked(achievement, saveData, revenueData)) {
                newlyUnlocked.add(achievement)
            }
        }
        
        return newlyUnlocked
    }
    
    /**
     * 检查单个成就是否达成
     */
    private fun isAchievementUnlocked(
        achievement: Achievement,
        saveData: SaveData,
        revenueData: Map<String, GameRevenue>
    ): Boolean {
        return when (achievement.category) {
            AchievementCategory.COMPANY -> checkCompanyAchievement(achievement, saveData)
            AchievementCategory.SINGLE_GAME -> checkSingleGameAchievement(achievement, saveData, revenueData)
            AchievementCategory.ONLINE_GAME -> checkOnlineGameAchievement(achievement, saveData, revenueData)
            AchievementCategory.EMPLOYEE -> checkEmployeeAchievement(achievement, saveData)
        }
    }
    
    /**
     * 检查公司成长类成就
     */
    private fun checkCompanyAchievement(achievement: Achievement, saveData: SaveData): Boolean {
        return when (achievement.id) {
            "company_start" -> true // 创办公司自动解锁
            else -> saveData.money >= achievement.targetValue
        }
    }
    
    /**
     * 检查单机游戏销量类成就
     */
    private fun checkSingleGameAchievement(
        achievement: Achievement,
        saveData: SaveData,
        revenueData: Map<String, GameRevenue>
    ): Boolean {
        // 找出所有单机游戏的最高销量（只统计已发售的游戏）
        val maxSingleGameSales = saveData.games
            .filter { 
                it.businessModel == BusinessModel.SINGLE_PLAYER && 
                it.releaseStatus == GameReleaseStatus.RELEASED  // 只统计真正发售的游戏
            }
            .mapNotNull { game ->
                revenueData[game.id]?.getTotalSales()
            }
            .maxOrNull() ?: 0L
        
        return maxSingleGameSales >= achievement.targetValue
    }
    
    /**
     * 检查网游活跃类成就
     */
    private fun checkOnlineGameAchievement(
        achievement: Achievement,
        saveData: SaveData,
        revenueData: Map<String, GameRevenue>
    ): Boolean {
        // 计算所有网游的总活跃玩家数（只统计已发售的游戏）
        val totalActivePlayers = saveData.games
            .filter { 
                it.businessModel == BusinessModel.ONLINE_GAME && 
                it.releaseStatus == GameReleaseStatus.RELEASED  // 只统计真正发售的游戏
            }
            .mapNotNull { game ->
                revenueData[game.id]?.getActivePlayers()
            }
            .sum()
        
        return totalActivePlayers >= achievement.targetValue
    }
    
    /**
     * 检查员工成长类成就
     */
    private fun checkEmployeeAchievement(achievement: Achievement, saveData: SaveData): Boolean {
        return saveData.allEmployees.size.toLong() >= achievement.targetValue
    }
    
    /**
     * 解锁成就
     * @return 更新后的已解锁成就列表
     */
    fun unlockAchievement(
        currentUnlocked: List<UnlockedAchievement>,
        achievement: Achievement
    ): List<UnlockedAchievement> {
        // 检查是否已经解锁
        if (currentUnlocked.any { it.achievementId == achievement.id }) {
            return currentUnlocked
        }
        
        // 添加新解锁的成就
        return currentUnlocked + UnlockedAchievement(
            achievementId = achievement.id,
            unlockTime = System.currentTimeMillis()
        )
    }
    
    /**
     * 获取成就进度
     * @return 进度百分比（0-100）
     */
    fun getAchievementProgress(
        achievement: Achievement,
        saveData: SaveData,
        revenueData: Map<String, GameRevenue>
    ): Float {
        val currentValue = when (achievement.category) {
            AchievementCategory.COMPANY -> saveData.money
            AchievementCategory.SINGLE_GAME -> {
                saveData.games
                    .filter { 
                        it.businessModel == BusinessModel.SINGLE_PLAYER && 
                        it.releaseStatus == GameReleaseStatus.RELEASED  // 只统计已发售的游戏
                    }
                    .mapNotNull { game -> revenueData[game.id]?.getTotalSales() }
                    .maxOrNull() ?: 0L
            }
            AchievementCategory.ONLINE_GAME -> {
                saveData.games
                    .filter { 
                        it.businessModel == BusinessModel.ONLINE_GAME && 
                        it.releaseStatus == GameReleaseStatus.RELEASED  // 只统计已发售的游戏
                    }
                    .mapNotNull { game -> revenueData[game.id]?.getActivePlayers() }
                    .sum()
            }
            AchievementCategory.EMPLOYEE -> saveData.allEmployees.size.toLong()
        }
        
        if (achievement.targetValue == 0L) return 100f
        return (currentValue.toFloat() / achievement.targetValue * 100f).coerceIn(0f, 100f)
    }
    
    /**
     * 格式化成就目标值
     */
    fun formatTargetValue(achievement: Achievement): String {
        return when (achievement.category) {
            AchievementCategory.COMPANY -> {
                val value = achievement.targetValue
                when {
                    value >= 100_000_000 -> "${value / 100_000_000}亿"
                    value >= 10_000_000 -> "${value / 10_000_000}千万"
                    value >= 1_000_000 -> "${value / 1_000_000}百万"
                    else -> "${value / 10_000}万"
                }
            }
            AchievementCategory.SINGLE_GAME -> "${achievement.targetValue / 10_000}万"
            AchievementCategory.ONLINE_GAME -> "${achievement.targetValue / 10_000}万"
            AchievementCategory.EMPLOYEE -> "${achievement.targetValue}人"
        }
    }
}
