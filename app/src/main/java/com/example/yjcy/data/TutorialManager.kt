package com.example.yjcy.data

/**
 * 教程管理器 - 统一管理所有教程内容和进度
 */
object TutorialManager {
    
    // 合并所有教程内容
    private val allTutorials by lazy {
        TutorialContentPart1.tutorials + 
        TutorialContentPart2.tutorials + 
        TutorialContentPart3.tutorials
    }
    
    /**
     * 获取指定ID的教程
     */
    fun getTutorial(id: TutorialId): TutorialStep? {
        return allTutorials[id]
    }
    
    /**
     * 检查教程是否已完成
     */
    fun isTutorialCompleted(id: TutorialId, completedTutorials: Set<TutorialId>): Boolean {
        return completedTutorials.contains(id)
    }
    
    /**
     * 检查是否应该显示教程
     * @param id 教程ID
     * @param completedTutorials 已完成的教程集合
     * @param skipTutorial 是否跳过所有教程
     * @return true表示应该显示
     */
    fun shouldShowTutorial(
        id: TutorialId,
        completedTutorials: Set<TutorialId>,
        skipTutorial: Boolean
    ): Boolean {
        // 如果用户选择跳过教程，则不显示
        if (skipTutorial) return false
        
        // 如果该教程已完成，则不显示
        if (isTutorialCompleted(id, completedTutorials)) return false
        
        return true
    }
    
    /**
     * 标记教程为已完成
     */
    fun markTutorialCompleted(
        id: TutorialId,
        completedTutorials: MutableSet<TutorialId>
    ) {
        completedTutorials.add(id)
    }
    
    /**
     * 获取所有教程ID列表
     */
    fun getAllTutorialIds(): List<TutorialId> {
        return TutorialId.values().toList()
    }
    
    /**
     * 获取教程总数
     */
    fun getTotalTutorialCount(): Int {
        return TutorialId.values().size
    }
    
    /**
     * 获取已完成教程数量
     */
    fun getCompletedTutorialCount(completedTutorials: Set<TutorialId>): Int {
        return completedTutorials.size
    }
    
    /**
     * 获取教程完成进度百分比
     */
    fun getTutorialProgress(completedTutorials: Set<TutorialId>): Float {
        val total = getTotalTutorialCount()
        if (total == 0) return 0f
        return (getCompletedTutorialCount(completedTutorials).toFloat() / total) * 100f
    }
    
    /**
     * 重置所有教程进度
     */
    fun resetAllTutorials(completedTutorials: MutableSet<TutorialId>) {
        completedTutorials.clear()
    }
    
    /**
     * 获取指定分类的教程列表
     */
    fun getTutorialsByCategory(category: TutorialCategory): List<TutorialId> {
        return when (category) {
            TutorialCategory.COMPANY_OVERVIEW -> listOf(
                TutorialId.COMPANY_OVERVIEW_INTRO,
                TutorialId.SECRETARY_CHAT,
                TutorialId.FINANCIAL_STATUS,
                TutorialId.ACHIEVEMENT_SYSTEM
            )
            TutorialCategory.EMPLOYEE_MANAGEMENT -> listOf(
                TutorialId.EMPLOYEE_MANAGEMENT_INTRO,
                TutorialId.EMPLOYEE_SKILLS,
                TutorialId.EMPLOYEE_POSITIONS,
                TutorialId.TALENT_MARKET,
                TutorialId.JOB_POSTING,
                TutorialId.EMPLOYEE_TRAINING,
                TutorialId.EMPLOYEE_FIRE
            )
            TutorialCategory.PROJECT_MANAGEMENT -> listOf(
                TutorialId.PROJECT_MANAGEMENT_INTRO,
                TutorialId.CREATE_GAME,
                TutorialId.DEVELOPMENT_PHASES,
                TutorialId.ASSIGN_EMPLOYEES,
                TutorialId.GAME_RELEASE,
                TutorialId.GAME_UPDATE,
                TutorialId.MONETIZATION,
                TutorialId.PROMOTION_CENTER,
                TutorialId.CUSTOMER_SERVICE,
                TutorialId.GAME_COMMUNITY
            )
            TutorialCategory.COMPETITOR -> listOf(
                TutorialId.COMPETITOR_INTRO,
                TutorialId.COMPETITOR_RANKING,
                TutorialId.COMPETITOR_NEWS,
                TutorialId.COMPETITOR_ACQUISITION
            )
            TutorialCategory.SERVER_MANAGEMENT -> listOf(
                TutorialId.SERVER_MANAGEMENT_INTRO,
                TutorialId.SERVER_PURCHASE,
                TutorialId.SERVER_ALLOCATION
            )
            TutorialCategory.GAME_MECHANICS -> listOf(
                TutorialId.TIME_SYSTEM,
                TutorialId.REVENUE_SYSTEM,
                TutorialId.FAN_SYSTEM,
                TutorialId.RATING_SYSTEM
            )
        }
    }
}

/**
 * 教程分类
 */
enum class TutorialCategory {
    COMPANY_OVERVIEW,      // 公司概览
    EMPLOYEE_MANAGEMENT,   // 员工管理
    PROJECT_MANAGEMENT,    // 项目管理
    COMPETITOR,            // 竞争对手
    SERVER_MANAGEMENT,     // 服务器管理
    GAME_MECHANICS         // 游戏机制
}
