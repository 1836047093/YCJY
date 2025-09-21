package com.example.yjcy.data

/**
 * 项目优先级枚举
 */
enum class ProjectPriority {
    LOW,        // 低优先级
    MEDIUM,     // 中等优先级
    HIGH,       // 高优先级
    CRITICAL    // 紧急优先级
}

/**
 * 项目状态枚举
 */
enum class ProjectStatus {
    PLANNING,       // 规划中
    IN_PROGRESS,    // 进行中
    COMPLETED,      // 已完成
    CANCELLED       // 已取消
}

/**
 * 项目数据模型
 * 定义游戏开发项目的基本信息和需求
 */
data class Project(
    val id: Int,
    val name: String,
    val description: String,
    val requiredSkills: Map<String, Int>, // 技能类型 -> 所需等级
    val maxEmployees: Int = 5,
    val requiredEmployees: Int = 1, // 项目所需的员工数量
    val currentEmployees: List<Int> = emptyList(), // 员工ID列表
    val priority: ProjectPriority = ProjectPriority.MEDIUM,
    val deadline: Long? = null, // 截止时间戳
    val budget: Int? = null, // 项目预算
    val status: ProjectStatus = ProjectStatus.PLANNING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取项目所需的总技能点数
     */
    fun getTotalRequiredSkillPoints(): Int {
        return requiredSkills.values.sum()
    }
    
    /**
     * 检查项目是否需要特定技能
     */
    fun requiresSkill(skillType: String): Boolean {
        return requiredSkills.containsKey(skillType)
    }
}