package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme

/**
 * GVA游戏大奖数据结构
 */

/**
 * 奖项分类
 */
enum class AwardCategory {
    THEME,      // 主题类奖项
    GENERAL,    // 综合类奖项
    SPECIAL     // 特殊成就奖项
}

/**
 * GVA奖项枚举
 */
enum class GVAAward(
    val displayName: String,
    val englishName: String,
    val icon: String,
    val category: AwardCategory,
    val theme: GameTheme? = null // 主题类奖项专用
) {
    // 主题类奖项（12个）
    BEST_ACTION("最佳动作游戏", "Best Action Game", "⚔️", AwardCategory.THEME, GameTheme.ACTION),
    BEST_ADVENTURE("最佳冒险游戏", "Best Adventure Game", "🗺️", AwardCategory.THEME, GameTheme.ADVENTURE),
    BEST_RPG("最佳角色扮演游戏", "Best RPG", "🧙", AwardCategory.THEME, GameTheme.RPG),
    BEST_STRATEGY("最佳策略游戏", "Best Strategy Game", "♟️", AwardCategory.THEME, GameTheme.STRATEGY),
    BEST_SIMULATION("最佳模拟游戏", "Best Simulation Game", "🏗️", AwardCategory.THEME, GameTheme.SIMULATION),
    BEST_PUZZLE("最佳益智游戏", "Best Puzzle Game", "🧩", AwardCategory.THEME, GameTheme.PUZZLE),
    BEST_RACING("最佳竞速游戏", "Best Racing Game", "🏎️", AwardCategory.THEME, GameTheme.RACING),
    BEST_SPORTS("最佳体育游戏", "Best Sports Game", "⚽", AwardCategory.THEME, GameTheme.SPORTS),
    BEST_HORROR("最佳恐怖游戏", "Best Horror Game", "👻", AwardCategory.THEME, GameTheme.HORROR),
    BEST_CASUAL("最佳休闲游戏", "Best Casual Game", "🎲", AwardCategory.THEME, GameTheme.CASUAL),
    BEST_SHOOTER("最佳射击游戏", "Best Shooter Game", "🔫", AwardCategory.THEME, GameTheme.SHOOTER),
    BEST_MOBA("最佳MOBA游戏", "Best MOBA Game", "🎮", AwardCategory.THEME, GameTheme.MOBA),
    
    // 综合类奖项（4个）
    GAME_OF_YEAR("年度游戏", "Game of the Year", "🏆", AwardCategory.GENERAL),
    BEST_INDIE("最佳独立游戏", "Best Indie Game", "💎", AwardCategory.GENERAL),
    PLAYERS_CHOICE("最受玩家喜爱", "Players' Choice", "❤️", AwardCategory.GENERAL),
    BEST_ONLINE("最佳网络游戏", "Best Online Game", "🌐", AwardCategory.GENERAL),
    
    // 特殊成就奖项（5个）
    INNOVATION("创新先锋奖", "Innovation Award", "💡", AwardCategory.SPECIAL),
    PERFECT_QUALITY("完美品质奖", "Perfect Quality Award", "⭐", AwardCategory.SPECIAL),
    COMMERCIAL_MIRACLE("商业奇迹奖", "Commercial Miracle", "💰", AwardCategory.SPECIAL),
    EVERGREEN("长青树奖", "Evergreen Award", "🌲", AwardCategory.SPECIAL),
    CULTURAL_IMPACT("文化影响力奖", "Cultural Impact Award", "🎭", AwardCategory.SPECIAL);
    
    /**
     * 获取奖励配置
     */
    fun getReward(): AwardReward {
        return when (this) {
            // 主题类奖项
            BEST_ACTION, BEST_ADVENTURE, BEST_RPG, BEST_STRATEGY,
            BEST_SIMULATION, BEST_PUZZLE, BEST_RACING, BEST_SPORTS,
            BEST_HORROR, BEST_CASUAL, BEST_SHOOTER, BEST_MOBA ->
                AwardReward(cashPrize = 100000, fansGain = 5000, reputationGain = 50)
            
            // 综合类奖项
            GAME_OF_YEAR -> AwardReward(cashPrize = 500000, fansGain = 20000, reputationGain = 200)
            BEST_INDIE -> AwardReward(cashPrize = 200000, fansGain = 10000, reputationGain = 80)
            PLAYERS_CHOICE -> AwardReward(cashPrize = 150000, fansGain = 15000, reputationGain = 100)
            BEST_ONLINE -> AwardReward(cashPrize = 300000, fansGain = 12000, reputationGain = 120)
            
            // 特殊成就奖项
            INNOVATION -> AwardReward(cashPrize = 150000, fansGain = 8000, reputationGain = 100)
            PERFECT_QUALITY -> AwardReward(cashPrize = 200000, fansGain = 10000, reputationGain = 150)
            COMMERCIAL_MIRACLE -> AwardReward(cashPrize = 300000, fansGain = 15000, reputationGain = 100)
            EVERGREEN -> AwardReward(cashPrize = 250000, fansGain = 12000, reputationGain = 120)
            CULTURAL_IMPACT -> AwardReward(cashPrize = 180000, fansGain = 18000, reputationGain = 90)
        }
    }
}

/**
 * 提名信息
 */
data class NomineeInfo(
    val gameId: String,
    val gameName: String,
    val companyId: Int, // -1表示玩家
    val companyName: String,
    val rating: Float,
    val popularityScore: Float,
    val totalScore: Float,
    val isPlayerGame: Boolean,
    val releaseDate: String // 发售日期（如"12月20日"）
)

/**
 * 奖项提名记录
 */
data class AwardNomination(
    val year: Int,
    val award: GVAAward,
    val nominees: List<NomineeInfo>, // 前3名提名
    val winner: NomineeInfo? = null, // 获奖者（12月31日前为null）
    val isFinal: Boolean = false // 是否为最终结果
)

/**
 * 奖励信息
 */
data class AwardReward(
    val cashPrize: Int,      // 奖金
    val fansGain: Long,       // 粉丝增长
    val reputationGain: Int  // 声望增长
)

/**
 * 获奖/提名记录
 */
data class AwardRecord(
    val year: Int,
    val award: GVAAward,
    val gameId: String,
    val gameName: String,
    val isWinner: Boolean, // true=获奖，false=仅提名
    val rewards: AwardReward,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 声望等级
 */
enum class ReputationLevel(
    val displayName: String,
    val minPoints: Int,
    val recruitBonus: Float,   // 招聘吸引力加成
    val fansBonus: Float,      // 粉丝增长加成
    val salesBonus: Float      // 初始销量加成
) {
    UNKNOWN("无名小厂", 0, 0f, 0f, 0f),
    EMERGING("新兴工作室", 100, 0.05f, 0f, 0f),
    WELL_KNOWN("知名厂商", 300, 0.10f, 0.10f, 0f),
    TOP_TIER("一线大厂", 600, 0.15f, 0.20f, 0.10f),
    LEGENDARY("业界传奇", 1000, 0.25f, 0.30f, 0.20f);
    
    companion object {
        fun fromPoints(points: Int): ReputationLevel {
            return values().reversed().firstOrNull { points >= it.minPoints } ?: UNKNOWN
        }
    }
}

/**
 * 公司声望
 */
data class CompanyReputation(
    val reputationPoints: Int = 0, // 声望值
    val awardHistory: List<AwardRecord> = emptyList(), // 获奖历史
    val nominationHistory: List<AwardRecord> = emptyList() // 提名历史（未获奖）
) {
    /**
     * 获取当前声望等级
     */
    fun getLevel(): ReputationLevel {
        return ReputationLevel.fromPoints(reputationPoints)
    }
    
    /**
     * 获取距离下一等级的进度（0-1）
     */
    fun getProgressToNextLevel(): Float {
        val currentLevel = getLevel()
        val nextLevel = ReputationLevel.values().getOrNull(currentLevel.ordinal + 1) ?: return 1f
        
        val currentMin = currentLevel.minPoints
        val nextMin = nextLevel.minPoints
        val progress = (reputationPoints - currentMin).toFloat() / (nextMin - currentMin)
        
        return progress.coerceIn(0f, 1f)
    }
    
    /**
     * 添加声望
     */
    fun addReputation(points: Int): CompanyReputation {
        return copy(reputationPoints = reputationPoints + points)
    }
    
    /**
     * 添加获奖记录
     */
    fun addAwardRecord(record: AwardRecord): CompanyReputation {
        return if (record.isWinner) {
            copy(awardHistory = awardHistory + record)
        } else {
            copy(nominationHistory = nominationHistory + record)
        }
    }
}
