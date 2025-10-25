package com.example.yjcy.data
import kotlin.math.min
import kotlin.math.max

/**
 * 增强的员工分配服务
 * 基于技能等级高低进行最佳员工分配
 */
class EnhancedAssignmentService {
    
    /**
     * 一键分配最佳员工到项目
     * @param projects 待分配的项目列表
     * @param availableEmployees 可用员工列表
     * @return 分配结果
     */
    fun assignBestEmployeesToProjects(
        projects: List<Game>,
        availableEmployees: List<Employee>
    ): EnhancedAssignmentResult {
        val assignments = mutableMapOf<String, List<Employee>>()
        val assignmentReasons = mutableMapOf<String, List<String>>()
        val failedProjects = mutableMapOf<String, String>()
        val usedEmployees = mutableSetOf<Int>()
        
        // 按项目优先级排序（这里简单按名称排序，实际可根据业务需求调整）
        val sortedProjects = projects.sortedBy { it.name }
        
        for (project in sortedProjects) {
            val availableForProject = availableEmployees.filter { it.id !in usedEmployees }
            
            // 先检查人数是否足够（根据当前开发阶段）
            val checkResult = checkEmployeeAvailability(project, availableForProject)
            if (!checkResult.isEnough) {
                failedProjects[project.id] = checkResult.reason
                continue
            }
            
            val projectAssignment = assignBestEmployeesToProject(
                project,
                availableForProject
            )
            
            assignments[project.id] = projectAssignment.assignedEmployees
            assignmentReasons[project.id] = projectAssignment.reasons
            
            // 标记已使用的员工
            projectAssignment.assignedEmployees.forEach { employee ->
                usedEmployees.add(employee.id)
            }
        }
        
        return EnhancedAssignmentResult(
            assignments = assignments,
            reasons = assignmentReasons,
            totalAssignedEmployees = usedEmployees.size,
            unassignedEmployees = availableEmployees.filter { it.id !in usedEmployees },
            failedProjects = failedProjects
        )
    }
    
    /**
     * 检查可用员工是否满足当前阶段的开发需求
     * 根据游戏的开发阶段判断需要哪些职位
     */
    private fun checkEmployeeAvailability(game: Game, availableEmployees: List<Employee>): EmployeeCheckResult {
        // 过滤掉客服，客服不参与开发
        val developmentEmployees = availableEmployees.filter { it.position != "客服" }
        
        // 统计各岗位人数
        val positionCounts = developmentEmployees.groupBy { it.position }.mapValues { it.value.size }
        
        // 根据当前阶段获取需要的职位和人数
        val requiredPositions = game.currentPhase.requiredPositions
        val minCount = game.currentPhase.minCount // 使用阶段的最低人数要求
        
        val shortages = mutableListOf<String>()
        for (position in requiredPositions) {
            val available = positionCounts[position] ?: 0
            if (available < minCount) {
                shortages.add("${position}最少需要${minCount}人，当前只有${available}人")
            }
        }
        
        return if (shortages.isEmpty()) {
            EmployeeCheckResult(true, "")
        } else {
            EmployeeCheckResult(false, shortages.joinToString("；"))
        }
    }
    
    /**
     * 为单个项目分配最佳员工
     * 根据游戏的当前开发阶段分配对应职位的员工
     * 客服不参与开发
     * @param project 项目信息，用于获取当前阶段和所需职位
     */
    fun assignBestEmployeesToProject(
        project: Game,
        availableEmployees: List<Employee>
    ): ProjectAssignmentResult {
        val assignedEmployees = mutableListOf<Employee>()
        val reasons = mutableListOf<String>()
        
        // 过滤掉客服，客服不参与开发
        val developmentEmployees = availableEmployees.filter { it.position != "客服" }
        
        // 根据当前阶段获取需要的职位
        val requiredPositions = project.currentPhase.requiredPositions
        
        // 为每个岗位分配所有可用的员工（按技能从高到低排序）
        for (position in requiredPositions) {
            val positionEmployees = developmentEmployees
                .filter { it.position == position && it.id !in assignedEmployees.map { emp -> emp.id } }
                .sortedByDescending { it.getSpecialtySkillLevel() }  // 按专业技能从高到低排序
                // 不限制人数，分配所有可用的该职位员工以加速开发
            
            assignedEmployees.addAll(positionEmployees)
            
            positionEmployees.forEach { employee ->
                val reason = "分配 ${employee.name}(${employee.position}) - 专业技能等级: ${employee.getSpecialtySkillLevel()}, 总技能: ${employee.getTotalSkillPoints()}"
                reasons.add(reason)
            }
        }
        
        return ProjectAssignmentResult(
            assignedEmployees = assignedEmployees,
            reasons = reasons
        )
    }
}

/**
 * 员工数量检查结果
 */
data class EmployeeCheckResult(
    val isEnough: Boolean,
    val reason: String
)

/**
 * 项目分配结果
 */
data class ProjectAssignmentResult(
    val assignedEmployees: List<Employee>,
    val reasons: List<String>
)

/**
 * 增强分配结果
 */
data class EnhancedAssignmentResult(
    val assignments: Map<String, List<Employee>>,
    val reasons: Map<String, List<String>>,
    val totalAssignedEmployees: Int,
    val unassignedEmployees: List<Employee>,
    val failedProjects: Map<String, String> = emptyMap() // 无法分配的项目及原因
) {
    /**
     * 获取分配摘要
     */
    fun getSummary(): String {
        return "成功分配 $totalAssignedEmployees 名员工到 ${assignments.size} 个项目，" +
                "剩余 ${unassignedEmployees.size} 名员工未分配"
    }
    
    /**
     * 获取项目的分配详情
     */
    fun getProjectAssignmentDetails(projectId: String): String? {
        val employees = assignments[projectId] ?: return null
        val projectReasons = reasons[projectId] ?: emptyList()
        
        return buildString {
            appendLine("分配了 ${employees.size} 名员工:")
            employees.forEachIndexed { index, employee ->
                appendLine("${index + 1}. ${employee.name}(${employee.position})")
            }
            if (projectReasons.isNotEmpty()) {
                appendLine("\n分配理由:")
                projectReasons.forEachIndexed { index, reason ->
                    appendLine("${index + 1}. $reason")
                }
            }
        }
    }
}