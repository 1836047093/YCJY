package com.example.yjcy.data

/**
 * 教程步骤ID枚举
 */
enum class TutorialId {
    // 公司概览相关（0-9）
    COMPANY_OVERVIEW_INTRO,          // 公司概览介绍
    SECRETARY_CHAT,                   // 秘书聊天功能
    FINANCIAL_STATUS,                 // 财务状况
    ACHIEVEMENT_SYSTEM,               // 成就系统
    
    // 员工管理相关（10-19）
    EMPLOYEE_MANAGEMENT_INTRO,        // 员工管理介绍
    EMPLOYEE_SKILLS,                  // 员工技能和等级
    EMPLOYEE_POSITIONS,               // 员工职位类型
    TALENT_MARKET,                    // 人才市场
    JOB_POSTING,                      // 岗位发布
    EMPLOYEE_TRAINING,                // 员工培训
    EMPLOYEE_FIRE,                    // 员工解雇
    
    // 项目管理相关（20-29）
    PROJECT_MANAGEMENT_INTRO,         // 项目管理介绍
    CREATE_GAME,                      // 创建游戏项目
    DEVELOPMENT_PHASES,               // 三阶段开发流程
    ASSIGN_EMPLOYEES,                 // 分配员工
    GAME_RELEASE,                     // 游戏发售
    GAME_UPDATE,                      // 游戏更新
    MONETIZATION,                     // 付费内容设置
    PROMOTION_CENTER,                 // 宣传中心
    CUSTOMER_SERVICE,                 // 客服中心
    GAME_COMMUNITY,                   // 游戏社区
    
    // 竞争对手相关（30-34）
    COMPETITOR_INTRO,                 // 竞争对手介绍
    COMPETITOR_RANKING,               // 排行榜
    COMPETITOR_NEWS,                  // 动态新闻
    COMPETITOR_ACQUISITION,           // 收购功能
    
    // 电竞赛事相关（35-39）
    TOURNAMENT_INTRO,                 // 电竞赛事介绍
    GVA_CONFERENCE_INTRO,             // GVA大会介绍
    
    // 服务器管理相关（40-44）
    SERVER_MANAGEMENT_INTRO,          // 服务器管理介绍
    SERVER_PURCHASE,                  // 购买服务器
    SERVER_ALLOCATION,                // 服务器分配
    
    // 游戏机制相关（45-49）
    TIME_SYSTEM,                      // 时间系统
    REVENUE_SYSTEM,                   // 收益系统
    FAN_SYSTEM,                       // 粉丝系统
    RATING_SYSTEM                     // 评分系统
}

/**
 * 教程步骤数据类
 */
data class TutorialStep(
    val id: TutorialId,
    val title: String,
    val content: String,
    val nextTutorial: TutorialId? = null  // 下一个教程（可选）
)
