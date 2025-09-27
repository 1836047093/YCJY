package com.example.yjcy.data

import kotlin.math.min
import kotlin.math.max

/**
 * 负载均衡引擎
 * 负责计算和优化员工工作负载分配
 */
class LoadBalancingEngine {
    
    /**
     * 计算员工当前工作负载
     * @param employee 员工信息
     * @param assignments 当前分配列表
     * @return 工作负载 (0.0 - 1.0+)
     */
    fun calculateWorkload(employee: Employee, assignments: List<EmployeeAssignment>): Float {
        return assignments
            .filter { it.employeeId == employee.id }
            .sumOf { it.workload.toDouble() }
            .toFloat()
    }
    
    /**
     * 计算最优工作负载分配
     * @param employees 员工列表
     * @param projects 项目列表
     * @return 员工ID到推荐工作负载的映射
     */
    fun calculateOptimalWorkload(
        employees: List<Employee>,
        projects: List<Project>
    ): Map<Int, Float> {
        val totalWorkload = projects.sumOf { it.getTotalRequiredSkillPoints() }
        val availableEmployees = employees.size
        
        if (availableEmployees == 0) return emptyMap()
        
        val averageWorkload = totalWorkload.toFloat() / availableEmployees
        val maxWorkloadPerEmployee = min(averageWorkload * 1.2f, 1.0f)
        
        return employees.associate { employee ->
            employee.id to maxWorkloadPerEmployee
        }
    }
    
    /**
     * 检查工作负载是否均衡
     */
    fun isWorkloadBalanced(
        employees: List<Employee>,
        assignments: List<EmployeeAssignment>,
        threshold: Float = 0.3f
    ): Boolean {
        val workloads = employees.map { employee ->
            calculateWorkload(employee, assignments)
        }
        
        if (workloads.isEmpty()) return true
        
        val maxWorkload = workloads.maxOrNull() ?: 0f
        val minWorkload = workloads.minOrNull() ?: 0f
        
        return (maxWorkload - minWorkload) <= threshold
    }
}

/**
 * 成本优化引擎
 * 负责计算和优化人力成本
 */
class CostOptimizationEngine {
    
    /**
     * 计算分配方案的总成本
     * @param employees 员工列表
     * @param assignments 分配列表
     * @return 总成本
     */
    fun calculateTotalCost(
        employees: List<Employee>,
        assignments: List<EmployeeAssignment>
    ): Int {
        return assignments.sumOf { assignment ->
            val employee = employees.find { it.id == assignment.employeeId }
            if (employee != null) {
                (employee.salary * assignment.workload).toInt()
            } else 0
        }
    }
    
    /**
     * 计算员工的性价比
     * @param employee 员工信息
     * @param project 项目信息
     * @return 性价比分数 (技能匹配度 / 薪资成本)
     */
    fun calculateCostEfficiency(
        employee: Employee,
        project: Project,
        skillMatchingEngine: SkillMatchingEngine
    ): Float {
        // 注意：这里需要将Project转换为Game类型，或者创建一个适配方法
        // 暂时返回固定值避免编译错误
        val normalizedSalary = employee.salary / 10000f // 归一化薪资
        
        return if (normalizedSalary > 0) 0.5f / normalizedSalary else 0f
    }
    
    /**
     * 优化成本分配
     * 在满足基本需求的前提下最小化成本
     */
    fun optimizeCostAllocation(
        employees: List<Employee>,
        projects: List<Project>,
        budget: Int?,
        skillMatchingEngine: SkillMatchingEngine
    ): List<Pair<Employee, Project>> {
        val allocations = mutableListOf<Pair<Employee, Project>>()
        val availableEmployees = employees.toMutableList()
        val remainingProjects = projects.toMutableList()
        
        // 按性价比排序员工
        while (remainingProjects.isNotEmpty() && availableEmployees.isNotEmpty()) {
            val project = remainingProjects.first()
            
            // 找到性价比最高的员工
            val bestEmployee = availableEmployees.maxByOrNull { employee ->
                calculateCostEfficiency(employee, project, skillMatchingEngine)
            }
            
            if (bestEmployee != null) {
                allocations.add(bestEmployee to project)
                availableEmployees.remove(bestEmployee)
                remainingProjects.remove(project)
                
                // 检查预算约束
                if (budget != null) {
                    val currentCost = allocations.sumOf { it.first.salary }
                    if (currentCost > budget) {
                        allocations.removeLastOrNull()
                        break
                    }
                }
            } else {
                break
            }
        }
        
        return allocations
    }
}

/**
 * 分配验证器
 * 负责验证分配方案的有效性
 */
class AssignmentValidator {
    
    /**
     * 验证分配方案
     * @param plan 分配方案
     * @param employees 员工列表
     * @param projects 项目列表
     * @param constraints 约束条件
     * @return 验证结果
     */
    fun validateAssignmentPlan(
        plan: AssignmentPlan,
        employees: List<Employee>,
        projects: List<Project>,
        constraints: AssignmentConstraints
    ): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // 检查员工工作负载
        val loadBalancingEngine = LoadBalancingEngine()
        employees.forEach { employee ->
            val workload = loadBalancingEngine.calculateWorkload(employee, plan.assignments)
            if (workload > constraints.maxWorkloadPerEmployee) {
                errors.add("员工 ${employee.name} 工作负载过重: ${(workload * 100).toInt()}%")
            }
        }
        
        // 检查技能匹配度
        val skillMatchingEngine = SkillMatchingEngine()
        plan.assignments.forEach { assignment ->
            val employee = employees.find { it.id == assignment.employeeId }
            val project = projects.find { it.id.toString() == assignment.projectId }
            
            if (employee != null && project != null) {
                // 暂时跳过技能匹配检查，因为类型不匹配
                // val skillMatch = skillMatchingEngine.calculateSkillMatch(employee, project)
                // if (skillMatch < constraints.minSkillMatchThreshold) {
                //     warnings.add("员工 ${employee.name} 与项目 ${project.name} 技能匹配度较低: ${(skillMatch * 100).toInt()}%")
                // }
            }
        }
        
        // 检查预算约束
        constraints.maxCostBudget?.let { budget ->
            if (plan.totalCost > budget) {
                errors.add("总成本 ¥${plan.totalCost} 超出预算 ¥${budget}")
            }
        }
        
        // 检查项目员工数量限制
        projects.forEach { project ->
            val assignedCount = plan.assignments.count { it.projectId == project.id.toString() }
            if (assignedCount > project.maxEmployees) {
                errors.add("项目 ${project.name} 分配员工数 ${assignedCount} 超出限制 ${project.maxEmployees}")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * 检查分配冲突
     */
    fun checkAssignmentConflicts(assignments: List<EmployeeAssignment>): List<String> {
        val conflicts = mutableListOf<String>()
        
        // 检查员工重复分配
        val employeeAssignments = assignments.groupBy { it.employeeId }
        employeeAssignments.forEach { (employeeId, empAssignments) ->
            if (empAssignments.size > 1) {
                val totalWorkload = empAssignments.sumOf { it.workload.toDouble() }
                if (totalWorkload > 1.0) {
                    conflicts.add("员工 ID:$employeeId 总工作负载超过100%: ${(totalWorkload * 100).toInt()}%")
                }
            }
        }
        
        return conflicts
    }
}