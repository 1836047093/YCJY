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
        val usedEmployees = mutableSetOf<Int>()
        
        // 按项目优先级排序（这里简单按名称排序，实际可根据业务需求调整）
        val sortedProjects = projects.sortedBy { it.name }
        
        for (project in sortedProjects) {
            val projectAssignment = assignBestEmployeesToProject(
                project,
                availableEmployees.filter { it.id !in usedEmployees }
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
            unassignedEmployees = availableEmployees.filter { it.id !in usedEmployees }
        )
    }
    
    /**
     * 为单个项目分配最佳员工
     */
    fun assignBestEmployeesToProject(
        project: Game,
        availableEmployees: List<Employee>
    ): ProjectAssignmentResult {
        val requiredSkills = getProjectRequiredSkills(project)
        val assignedEmployees = mutableListOf<Employee>()
        val reasons = mutableListOf<String>()
        
        // 为每个技能类型分配最佳员工
        for (skillType in requiredSkills) {
            val bestEmployee = findBestEmployeeForSkill(
                skillType,
                availableEmployees.filter { it.id !in assignedEmployees.map { emp -> emp.id } }
            )
            
            if (bestEmployee != null) {
                assignedEmployees.add(bestEmployee)
                val reason = generateAssignmentReason(bestEmployee, skillType, project)
                reasons.add(reason)
            }
        }
        
        // 如果还需要更多员工，按综合技能分配
        val maxEmployeesPerProject = 4
        while (assignedEmployees.size < maxEmployeesPerProject) {
            val remainingEmployees = availableEmployees.filter { 
                it.id !in assignedEmployees.map { emp -> emp.id }
            }
            
            if (remainingEmployees.isEmpty()) break
            
            val bestGeneralEmployee = findBestGeneralEmployee(remainingEmployees)
            if (bestGeneralEmployee != null) {
                assignedEmployees.add(bestGeneralEmployee)
                reasons.add("分配 ${bestGeneralEmployee.name} 作为综合技能支持，总技能点数: ${bestGeneralEmployee.getTotalSkillPoints()}")
            } else {
                break
            }
        }
        
        return ProjectAssignmentResult(
            assignedEmployees = assignedEmployees,
            reasons = reasons
        )
    }
    
    /**
     * 根据项目类型确定所需技能
     */
    private fun getProjectRequiredSkills(project: Game): List<String> {
        return when (project.theme.name) {
            "ACTION" -> listOf("开发", "美工", "音乐")
            "ADVENTURE" -> listOf("开发", "设计", "美工")
            "PUZZLE" -> listOf("开发", "设计")
            "STRATEGY" -> listOf("开发", "设计", "美工")
            "SIMULATION" -> listOf("开发", "设计")
            "SPORTS" -> listOf("开发", "美工", "音乐")
            "RACING" -> listOf("开发", "美工", "音乐")
            "FIGHTING" -> listOf("开发", "美工", "音乐")
            "SHOOTING" -> listOf("开发", "美工", "音乐")
            "HORROR" -> listOf("开发", "美工", "音乐", "设计")
            else -> listOf("开发", "设计", "美工")
        }
    }
    
    /**
     * 为特定技能找到最佳员工
     */
    private fun findBestEmployeeForSkill(
        skillType: String,
        availableEmployees: List<Employee>
    ): Employee? {
        if (availableEmployees.isEmpty()) return null
        
        return availableEmployees
            .filter { employee ->
                // 优先选择专业对口的员工
                when (skillType) {
                    "开发" -> employee.position == "程序员"
                    "设计" -> employee.position == "策划师"
                    "美工" -> employee.position == "美术师"
                    "音乐" -> employee.position == "音效师"
                    "服务" -> employee.position == "客服"
                    else -> true
                }
            }
            .ifEmpty { availableEmployees } // 如果没有专业对口的，从所有员工中选择
            .maxByOrNull { employee ->
                val skillLevel = getEmployeeSkillLevel(employee, skillType)
                val totalSkill = employee.getTotalSkillPoints()
                // 综合评分：专业技能权重70%，总技能权重30%
                skillLevel * 0.7 + (totalSkill / 25.0) * 0.3
            }
    }
    
    /**
     * 找到综合技能最佳的员工
     */
    private fun findBestGeneralEmployee(availableEmployees: List<Employee>): Employee? {
        if (availableEmployees.isEmpty()) return null
        
        return availableEmployees.maxByOrNull { employee ->
            val totalSkill = employee.getTotalSkillPoints()
            val primarySkill = employee.getPrimarySkillValue()
            // 综合评分：总技能50%，主要技能50%
            totalSkill * 0.5 + primarySkill * 0.5
        }
    }
    
    /**
     * 获取员工指定技能的等级
     */
    private fun getEmployeeSkillLevel(employee: Employee, skillType: String): Int {
        return when (skillType) {
            "开发" -> employee.skillDevelopment
            "设计" -> employee.skillDesign
            "美工" -> employee.skillArt
            "音乐" -> employee.skillMusic
            "服务" -> employee.skillService
            else -> employee.getTotalSkillPoints() / 5
        }
    }
    
    /**
     * 生成分配理由
     */
    private fun generateAssignmentReason(
        employee: Employee,
        skillType: String,
        project: Game
    ): String {
        val skillLevel = getEmployeeSkillLevel(employee, skillType)
        val isSpecialist = employee.getSpecialtySkillType() == skillType
        
        return buildString {
            append("分配 ${employee.name}(${employee.position}) 负责${skillType}工作")
            if (isSpecialist) {
                append("，专业对口")
            }
            append("，${skillType}技能等级: $skillLevel")
            append("，总技能点数: ${employee.getTotalSkillPoints()}")
        }
    }
}

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
    val unassignedEmployees: List<Employee>
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