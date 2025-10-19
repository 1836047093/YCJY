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
            
            // 先检查人数是否足够
            val checkResult = checkEmployeeAvailability(availableForProject)
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
     * 检查可用员工是否满足开发需求
     * 每个游戏需要：5名程序员、5名策划师、5名美术师、5名音效师
     */
    private fun checkEmployeeAvailability(availableEmployees: List<Employee>): EmployeeCheckResult {
        // 过滤掉客服，客服不参与开发
        val developmentEmployees = availableEmployees.filter { it.position != "客服" }
        
        // 统计各岗位人数
        val positionCounts = developmentEmployees.groupBy { it.position }.mapValues { it.value.size }
        
        val requiredPositions = mapOf(
            "程序员" to 5,
            "策划师" to 5,
            "美术师" to 5,
            "音效师" to 5
        )
        
        val shortages = mutableListOf<String>()
        for ((position, required) in requiredPositions) {
            val available = positionCounts[position] ?: 0
            if (available < required) {
                shortages.add("${position}需要${required}人，当前只有${available}人")
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
     * 每个游戏需要20名员工：5名程序、5名策划、5名美术、5名音效
     * 客服不参与开发
     * @param project 项目信息（当前版本统一所有游戏类型的岗位需求，该参数保留用于将来可能的扩展）
     */
    fun assignBestEmployeesToProject(
        @Suppress("UNUSED_PARAMETER") project: Game,
        availableEmployees: List<Employee>
    ): ProjectAssignmentResult {
        val assignedEmployees = mutableListOf<Employee>()
        val reasons = mutableListOf<String>()
        
        // 过滤掉客服，客服不参与开发
        val developmentEmployees = availableEmployees.filter { it.position != "客服" }
        
        // 定义每个岗位需要的人数
        val requiredPositions = mapOf(
            "程序员" to 5,
            "策划师" to 5,
            "美术师" to 5,
            "音效师" to 5
        )
        
        // 为每个岗位分配指定数量的员工
        for ((position, count) in requiredPositions) {
            val positionEmployees = developmentEmployees
                .filter { it.position == position && it.id !in assignedEmployees.map { emp -> emp.id } }
                .sortedByDescending { it.getSpecialtySkillLevel() }  // 按专业技能从高到低排序
                .take(count)  // 取前N名
            
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