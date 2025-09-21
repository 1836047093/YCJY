package com.example.yjcy.data

/**
 * 分配策略枚举
 */
enum class AssignmentStrategy {
    SKILL_PRIORITY,     // 技能优先策略
    LOAD_BALANCE,       // 负载均衡策略
    COST_OPTIMIZATION,  // 成本优化策略
    MIXED              // 混合策略
}

/**
 * 员工状态枚举
 */
enum class EmployeeStatus {
    AVAILABLE,   // 空闲
    BUSY,        // 忙碌
    OVERLOADED   // 超负荷
}

/**
 * 员工分配数据模型
 * 表示员工与项目的分配关系
 */
data class EmployeeAssignment(
    val employeeId: Int,
    val projectId: String?,
    val assignmentDate: Long = System.currentTimeMillis(),
    val workload: Float = 0.0f, // 工作负载 0.0 - 1.0
    val efficiency: Float = 0.0f, // 预期效率
    val skillMatchScore: Float = 0.0f // 技能匹配分数 0.0 - 1.0
) {
    /**
     * 检查分配是否有效
     */
    fun isValid(): Boolean {
        return workload in 0.0f..1.0f && efficiency >= 0.0f
    }
    
    /**
     * 获取工作负载百分比
     */
    fun getWorkloadPercentage(): Int {
        return (workload * 100).toInt()
    }
}

/**
 * 分配方案数据模型
 * 表示一个完整的员工分配方案
 */
data class AssignmentPlan(
    val id: String,
    val name: String,
    val assignments: List<EmployeeAssignment>,
    val strategy: AssignmentStrategy,
    val createdAt: Long = System.currentTimeMillis(),
    val expectedEfficiency: Float = 0.0f,
    val totalCost: Int = 0,
    val totalScore: Float = 0.0f, // 总分数 0.0 - 1.0
    val isTemplate: Boolean = false,
    val isExecuted: Boolean = false
) {
    /**
     * 获取分配的员工总数
     */
    fun getTotalAssignedEmployees(): Int {
        return assignments.distinctBy { it.employeeId }.size
    }
    
    /**
     * 获取涉及的项目总数
     */
    fun getTotalProjects(): Int {
        return assignments.mapNotNull { it.projectId }.distinct().size
    }
    
    /**
     * 获取平均工作负载
     */
    fun getAverageWorkload(): Float {
        return if (assignments.isNotEmpty()) {
            assignments.map { it.workload }.average().toFloat()
        } else 0.0f
    }
    
    /**
     * 获取特定员工的分配信息
     */
    fun getAssignmentForEmployee(employeeId: Int): EmployeeAssignment? {
        return assignments.find { it.employeeId == employeeId }
    }
    
    /**
     * 获取特定项目的所有分配
     */
    fun getAssignmentsForProject(projectId: String): List<EmployeeAssignment> {
        return assignments.filter { it.projectId == projectId }
    }
    
    /**
     * 检查方案是否可以执行
     */
    fun canExecute(): Boolean {
        return !isExecuted && assignments.all { it.isValid() }
    }
}

/**
 * 分配约束条件
 */
data class AssignmentConstraints(
    val maxWorkloadPerEmployee: Float = 1.0f,
    val minSkillMatchThreshold: Float = 0.3f,
    val maxCostBudget: Int? = null,
    val prioritizeHighPriorityProjects: Boolean = true,
    val allowCrossSkillAssignment: Boolean = true,
    val maxEmployeesPerProject: Int? = null
)

/**
 * 验证结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    fun hasErrors(): Boolean = errors.isNotEmpty()
    fun hasWarnings(): Boolean = warnings.isNotEmpty()
}

/**
 * 分配模板
 */
data class AssignmentTemplate(
    val id: Int,
    val name: String,
    val description: String,
    val planId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false,
    val usageCount: Int = 0
)