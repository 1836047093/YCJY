package com.example.yjcy.data

/**
 * 成就类别
 */
enum class AchievementCategory(val displayName: String, val icon: String) {
    COMPANY("公司成长", "💼"),
    SINGLE_GAME("单机游戏销量", "🎮"),
    ONLINE_GAME("网游活跃", "🌐"),
    EMPLOYEE("员工成长", "👨‍💼")
}

/**
 * 成就数据类
 */
data class Achievement(
    val id: String,                     // 成就唯一ID
    val name: String,                   // 成就名称
    val description: String,            // 成就描述
    val category: AchievementCategory,  // 成就类别
    val icon: String,                   // 成就图标
    val targetValue: Long = 0L,         // 目标数值（金额、销量等）
    val rewardDescription: String = ""  // 奖励描述（预留）
)

/**
 * 已解锁的成就
 */
data class UnlockedAchievement(
    val achievementId: String,  // 成就ID
    val unlockTime: Long        // 解锁时间戳
)

/**
 * 成就定义
 */
object Achievements {
    
    // ==================== 💼 公司成长类 ====================
    val COMPANY_START = Achievement(
        id = "company_start",
        name = "从零开始的游戏梦",
        description = "创办游戏公司",
        category = AchievementCategory.COMPANY,
        icon = "🌱",
        targetValue = 0L
    )
    
    val COMPANY_5M = Achievement(
        id = "company_5m",
        name = "能养活自己了",
        description = "资金达到500万",
        category = AchievementCategory.COMPANY,
        icon = "💰",
        targetValue = 5_000_000L
    )
    
    val COMPANY_15M = Achievement(
        id = "company_15m",
        name = "小有名气",
        description = "资金达到1500万",
        category = AchievementCategory.COMPANY,
        icon = "📈",
        targetValue = 15_000_000L
    )
    
    val COMPANY_30M = Achievement(
        id = "company_30m",
        name = "投资人来敲门",
        description = "资金达到3000万",
        category = AchievementCategory.COMPANY,
        icon = "🤝",
        targetValue = 30_000_000L
    )
    
    val COMPANY_50M = Achievement(
        id = "company_50m",
        name = "游戏圈新贵",
        description = "资金达到5000万",
        category = AchievementCategory.COMPANY,
        icon = "🌟",
        targetValue = 50_000_000L
    )
    
    val COMPANY_100M = Achievement(
        id = "company_100m",
        name = "资本的味道",
        description = "资金达到1亿",
        category = AchievementCategory.COMPANY,
        icon = "💎",
        targetValue = 100_000_000L
    )
    
    val COMPANY_500M = Achievement(
        id = "company_500m",
        name = "行业巨头",
        description = "资金达到5亿",
        category = AchievementCategory.COMPANY,
        icon = "🏢",
        targetValue = 500_000_000L
    )
    
    val COMPANY_1B = Achievement(
        id = "company_1b",
        name = "游戏帝国",
        description = "资金达到10亿",
        category = AchievementCategory.COMPANY,
        icon = "👑",
        targetValue = 1_000_000_000L
    )
    
    // ==================== 🎮 单机游戏销量类 ====================
    val SINGLE_1M = Achievement(
        id = "single_1m",
        name = "百万奇迹",
        description = "单款游戏销量突破100万",
        category = AchievementCategory.SINGLE_GAME,
        icon = "🎯",
        targetValue = 1_000_000L
    )
    
    val SINGLE_3M = Achievement(
        id = "single_3m",
        name = "爆款制造机",
        description = "单款游戏销量突破300万",
        category = AchievementCategory.SINGLE_GAME,
        icon = "🔥",
        targetValue = 3_000_000L
    )
    
    val SINGLE_5M = Achievement(
        id = "single_5m",
        name = "全民热玩",
        description = "单款游戏销量突破500万",
        category = AchievementCategory.SINGLE_GAME,
        icon = "🌍",
        targetValue = 5_000_000L
    )
    
    val SINGLE_10M = Achievement(
        id = "single_10m",
        name = "传奇制作人",
        description = "单款游戏销量突破1000万",
        category = AchievementCategory.SINGLE_GAME,
        icon = "⭐",
        targetValue = 10_000_000L
    )
    
    // ==================== 🌐 网游活跃类 ====================
    val ONLINE_100K = Achievement(
        id = "online_100k",
        name = "服务器开始冒烟",
        description = "网游总活跃突破10万",
        category = AchievementCategory.ONLINE_GAME,
        icon = "🔧",
        targetValue = 100_000L
    )
    
    val ONLINE_300K = Achievement(
        id = "online_300k",
        name = "热度爆棚",
        description = "网游总活跃突破30万",
        category = AchievementCategory.ONLINE_GAME,
        icon = "🚀",
        targetValue = 300_000L
    )
    
    val ONLINE_500K = Achievement(
        id = "online_500k",
        name = "国服爆满",
        description = "网游总活跃突破50万",
        category = AchievementCategory.ONLINE_GAME,
        icon = "🎊",
        targetValue = 500_000L
    )
    
    val ONLINE_1M = Achievement(
        id = "online_1m",
        name = "虚拟世界的王者",
        description = "网游总活跃突破100万",
        category = AchievementCategory.ONLINE_GAME,
        icon = "🏆",
        targetValue = 1_000_000L
    )
    
    // ==================== 👨‍💼 员工成长类 ====================
    val EMPLOYEE_10 = Achievement(
        id = "employee_10",
        name = "小团队，大梦想",
        description = "员工总数达到10人",
        category = AchievementCategory.EMPLOYEE,
        icon = "👥",
        targetValue = 10L
    )
    
    val EMPLOYEE_20 = Achievement(
        id = "employee_20",
        name = "初具规模",
        description = "员工总数达到20人",
        category = AchievementCategory.EMPLOYEE,
        icon = "👫",
        targetValue = 20L
    )
    
    val EMPLOYEE_30 = Achievement(
        id = "employee_30",
        name = "中型工作室",
        description = "员工总数达到30人",
        category = AchievementCategory.EMPLOYEE,
        icon = "👨‍👩‍👧‍👦",
        targetValue = 30L
    )
    
    // ==================== 所有成就列表 ====================
    val ALL_ACHIEVEMENTS = listOf(
        // 公司成长类
        COMPANY_START,
        COMPANY_5M,
        COMPANY_15M,
        COMPANY_30M,
        COMPANY_50M,
        COMPANY_100M,
        COMPANY_500M,
        COMPANY_1B,
        
        // 单机游戏销量类
        SINGLE_1M,
        SINGLE_3M,
        SINGLE_5M,
        SINGLE_10M,
        
        // 网游活跃类
        ONLINE_100K,
        ONLINE_300K,
        ONLINE_500K,
        ONLINE_1M,
        
        // 员工成长类
        EMPLOYEE_10,
        EMPLOYEE_20,
        EMPLOYEE_30
    )
    
    /**
     * 根据类别获取成就列表
     */
    fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        return ALL_ACHIEVEMENTS.filter { it.category == category }
    }
    
    /**
     * 根据ID获取成就
     */
    fun getAchievementById(id: String): Achievement? {
        return ALL_ACHIEVEMENTS.find { it.id == id }
    }
}
