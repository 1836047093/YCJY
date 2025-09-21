package com.example.yjcy.data

import com.example.yjcy.Employee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * 分配管理服务 - 负责协调各种分配算法引擎，提供统一的分配接口
 */
class AssignmentService {
    private val skillMatchingEngine = SkillMatchingEngine()
    private val loadBalancingEngine = LoadBalancingEngine()
    private val costOptimizationEngine = CostOptimizationEngine()
    private val assignmentValidator = AssignmentValidator()
    
    // 当前分配状态
    private val _assignmentState = MutableStateFlow<AssignmentState>(AssignmentState.Idle)
    val assignmentState: StateFlow<AssignmentState> = _assignmentState.asStateFlow()
    
    // 分配历史记录
    private val _assignmentHistory = MutableStateFlow<List<AssignmentPlan>>(emptyList())
    val assignmentHistory: StateFlow<List<AssignmentPlan>> = _assignmentHistory.asStateFlow()
    
    /**
     * 执行一键智能分配
     * @param projects 待分配的项目列表
     * @param employees 可用员工列表
     * @param strategy 分配策略
     * @return 分配计划
     */
    suspend fun executeOneClickAssignment(
        projects: List<Project>,
        employees: List<Employee>,
        strategy: AssignmentStrategy = AssignmentStrategy.MIXED
    ): AssignmentResult {
        try {
            _assignmentState.value = AssignmentState.Processing
            
            // 1. 验证输入数据
            val inputValidation = validateAssignmentInput(projects, employees)
            if (!inputValidation.isValid) {
                val errorMsg = inputValidation.errors.joinToString(", ")
                _assignmentState.value = AssignmentState.Error(errorMsg)
                return AssignmentResult.failure(errorMsg)
            }
            
            // 2. 根据策略执行分配
            val assignmentPlan = when (strategy) {
                AssignmentStrategy.SKILL_PRIORITY -> executeSkillPriorityAssignment(projects, employees)
                AssignmentStrategy.LOAD_BALANCE -> executeLoadBalancedAssignment(projects, employees)
                AssignmentStrategy.COST_OPTIMIZATION -> executeCostOptimizedAssignment(projects, employees)
                AssignmentStrategy.MIXED -> executeBalancedAssignment(projects, employees)
            }
            
            // 3. 验证分配结果
            val validationResult = assignmentValidator.validateAssignmentPlan(
                assignmentPlan, 
                employees, 
                projects, 
                AssignmentConstraints()
            )
            if (!validationResult.isValid) {
                _assignmentState.value = AssignmentState.Error("分配计划验证失败: ${validationResult.errors.joinToString(", ")}")
                return AssignmentResult.failure("分配计划验证失败")
            }
            
            // 4. 保存分配历史
            addToHistory(assignmentPlan)
            
            _assignmentState.value = AssignmentState.Success(assignmentPlan)
            return AssignmentResult.success(assignmentPlan)
            
        } catch (e: Exception) {
            val errorMessage = "分配过程中发生错误: ${e.message}"
            _assignmentState.value = AssignmentState.Error(errorMessage)
            return AssignmentResult.failure(errorMessage)
        }
    }
    
    /**
     * 技能优先分配策略
     */
    private suspend fun executeSkillPriorityAssignment(
        projects: List<Project>,
        employees: List<Employee>
    ): AssignmentPlan {
        // 使用技能匹配引擎进行最佳匹配
        val assignments = mutableListOf<EmployeeAssignment>()
        val availableEmployees = employees.toMutableList()
        
        projects.forEach { project ->
            val bestEmployee = availableEmployees.maxByOrNull { employee ->
                skillMatchingEngine.calculateSkillMatch(employee, project)
            }
            
            if (bestEmployee != null) {
                assignments.add(
                    EmployeeAssignment(
                        employeeId = bestEmployee.id,
                        projectId = project.id.toString(),
                        assignmentDate = System.currentTimeMillis(),
                        workload = 0.8f, // 临时设置
                        efficiency = 0.9f, // 临时设置
                        skillMatchScore = skillMatchingEngine.calculateSkillMatch(bestEmployee, project)
                    )
                )
                availableEmployees.remove(bestEmployee)
            }
        }
        
        return AssignmentPlan(
            id = UUID.randomUUID().toString(),
            name = "技能优先分配方案",
            assignments = assignments,
            strategy = AssignmentStrategy.SKILL_PRIORITY,
            createdAt = System.currentTimeMillis(),
            totalScore = calculatePlanScore(assignments, projects, employees).toFloat()
        )
    }
    
    /**
     * 负载均衡分配策略
     */
    private suspend fun executeLoadBalancedAssignment(
        projects: List<Project>,
        employees: List<Employee>
    ): AssignmentPlan {
        // 临时实现负载均衡分配
        val assignments = mutableListOf<EmployeeAssignment>()
        val availableEmployees = employees.toMutableList()
        
        projects.forEach { project ->
            val requiredCount = minOf(project.requiredEmployees, availableEmployees.size)
            repeat(requiredCount) {
                if (availableEmployees.isNotEmpty()) {
                    val employee = availableEmployees.removeAt(0)
                    assignments.add(
                        EmployeeAssignment(
                            employeeId = employee.id,
                            projectId = project.id.toString(),
                            assignmentDate = System.currentTimeMillis(),
                            workload = 0.7f,
                            efficiency = 0.8f,
                            skillMatchScore = skillMatchingEngine.calculateSkillMatch(employee, project)
                        )
                    )
                }
            }
        }
        
        return AssignmentPlan(
            id = UUID.randomUUID().toString(),
            name = "负载均衡分配方案",
            assignments = assignments,
            strategy = AssignmentStrategy.LOAD_BALANCE,
            createdAt = System.currentTimeMillis(),
            totalScore = calculatePlanScore(assignments, projects, employees).toFloat()
        )
    }
    
    /**
     * 成本优化分配策略
     */
    private suspend fun executeCostOptimizedAssignment(
        projects: List<Project>,
        employees: List<Employee>
    ): AssignmentPlan {
        // 临时实现成本优化分配
        val assignments = mutableListOf<EmployeeAssignment>()
        val availableEmployees = employees.toMutableList()
        
        projects.forEach { project ->
            val requiredCount = minOf(project.requiredEmployees, availableEmployees.size)
            repeat(requiredCount) {
                if (availableEmployees.isNotEmpty()) {
                    val employee = availableEmployees.removeAt(0)
                    assignments.add(
                        EmployeeAssignment(
                            employeeId = employee.id,
                            projectId = project.id.toString(),
                            assignmentDate = System.currentTimeMillis(),
                            workload = 0.6f,
                            efficiency = 0.7f,
                            skillMatchScore = skillMatchingEngine.calculateSkillMatch(employee, project)
                        )
                    )
                }
            }
        }
        
        return AssignmentPlan(
            id = UUID.randomUUID().toString(),
            name = "成本优化分配方案",
            assignments = assignments,
            strategy = AssignmentStrategy.COST_OPTIMIZATION,
            createdAt = System.currentTimeMillis(),
            totalScore = calculatePlanScore(assignments, projects, employees).toFloat()
        )
    }
    
    /**
     * 平衡分配策略（综合考虑技能、负载和成本）
     */
    private suspend fun executeBalancedAssignment(
        projects: List<Project>,
        employees: List<Employee>
    ): AssignmentPlan {
        // 获取各种策略的分配结果
        val skillPlan = executeSkillPriorityAssignment(projects, employees)
        val loadPlan = executeLoadBalancedAssignment(projects, employees)
        val costPlan = executeCostOptimizedAssignment(projects, employees)
        
        // 选择综合得分最高的方案
        val bestPlan: AssignmentPlan = listOf(skillPlan, loadPlan, costPlan)
            .maxByOrNull { it.totalScore } ?: skillPlan
        
        return bestPlan.copy(
            id = UUID.randomUUID().toString(),
            name = "平衡分配方案",
            strategy = AssignmentStrategy.MIXED,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 验证分配输入数据
     */
    private fun validateAssignmentInput(
        projects: List<Project>,
        employees: List<Employee>
    ): ValidationResult {
        if (projects.isEmpty()) {
            return ValidationResult(false, listOf("项目列表不能为空"))
        }
        
        if (employees.isEmpty()) {
            return ValidationResult(false, listOf("员工列表不能为空"))
        }
        
        val totalRequiredEmployees = projects.sumOf { it.requiredEmployees }
        if (totalRequiredEmployees > employees.size) {
            return ValidationResult(
                false, 
                listOf("可用员工数量(${employees.size})不足以满足项目需求(${totalRequiredEmployees})")
            )
        }
        
        return ValidationResult(true)
    }
    
    /**
     * 计算分配计划总分
     */
    private fun calculatePlanScore(
        assignments: List<EmployeeAssignment>,
        projects: List<Project>,
        employees: List<Employee>
    ): Double {
        if (assignments.isEmpty()) return 0.0
        
        val skillScore = assignments.sumOf { it.skillMatchScore.toDouble() } / assignments.size
        val loadScore = 0.8 // 临时设置默认负载平衡分数
        val costScore = 0.7 // 临时设置默认成本效率分数
        
        // 综合评分（权重可调整）
        return skillScore * 0.4 + loadScore * 0.3 + costScore * 0.3
    }
    
    /**
     * 添加到分配历史
     */
    private fun addToHistory(plan: AssignmentPlan) {
        val currentHistory = _assignmentHistory.value.toMutableList()
        currentHistory.add(0, plan) // 添加到列表开头
        
        // 保持历史记录数量限制
        if (currentHistory.size > 50) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _assignmentHistory.value = currentHistory
    }
    
    /**
     * 获取分配建议
     */
    fun getAssignmentSuggestions(
        project: Project,
        employees: List<Employee>
    ): List<AssignmentSuggestion> {
        return employees.take(5)
            .map { employee: Employee ->
                AssignmentSuggestion(
                    employee = employee,
                    matchScore = skillMatchingEngine.calculateSkillMatch(employee, project).toDouble(),
                    reasons = generateAssignmentReasons(employee, project)
                )
            }
    }
    
    /**
     * 生成分配建议原因
     */
    private fun generateAssignmentReasons(employee: Employee, project: Project): List<String> {
        val reasons = mutableListOf<String>()
        
        val skillMatch = skillMatchingEngine.calculateSkillMatch(employee, project).toDouble()
        if (skillMatch > 0.8) {
            reasons.add("技能高度匹配(${(skillMatch * 100).toInt()}%)")
        }
        
        if (employee.getSpecialtySkillType() in project.requiredSkills) {
            reasons.add("拥有项目所需的专业技能")
        }
        
        val currentLoad = 0.5 // 临时设置默认工作负载
        if (currentLoad < 0.7) {
            reasons.add("当前工作负载较轻(${(currentLoad * 100).toInt()}%)")
        }
        
        return reasons
    }
    
    /**
     * 清除分配历史
     */
    fun clearAssignmentHistory() {
        _assignmentHistory.value = emptyList()
    }
    
    /**
     * 重置分配状态
     */
    fun resetAssignmentState() {
        _assignmentState.value = AssignmentState.Idle
    }
}

/**
 * 分配状态
 */
sealed class AssignmentState {
    object Idle : AssignmentState()
    object Processing : AssignmentState()
    data class Success(val plan: AssignmentPlan) : AssignmentState()
    data class Error(val message: String) : AssignmentState()
}

/**
 * 分配结果
 */
data class AssignmentResult(
    val isSuccess: Boolean,
    val plan: AssignmentPlan?,
    val errorMessage: String?
) {
    companion object {
        fun success(plan: AssignmentPlan) = AssignmentResult(true, plan, null)
        fun failure(message: String) = AssignmentResult(false, null, message)
    }
}



/**
 * 分配建议
 */
data class AssignmentSuggestion(
    val employee: Employee,
    val matchScore: Double,
    val reasons: List<String>
)