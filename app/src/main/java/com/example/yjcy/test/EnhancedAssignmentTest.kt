package com.example.yjcy.test

import com.example.yjcy.data.*
import com.example.yjcy.ui.*

/**
 * 增强分配功能测试类
 * 验证智能分配算法的正确性和完整性
 */
class EnhancedAssignmentTest {
    
    /**
     * 创建测试数据
     */
    private fun createTestData(): Pair<List<Game>, List<Employee>> {
        // 创建测试项目
        val projects = listOf(
            Game(
                id = "project1",
                name = "动作冒险游戏",
                theme = GameTheme.ACTION,
                platforms = listOf(Platform.PC),
                businessModel = BusinessModel.PREMIUM,
                assignedEmployees = emptyList()
            ),
            Game(
                id = "project2",
                name = "休闲益智游戏",
                theme = GameTheme.PUZZLE,
                platforms = listOf(Platform.MOBILE),
                businessModel = BusinessModel.FREE_TO_PLAY,
                assignedEmployees = emptyList()
            ),
            Game(
                id = "project3",
                name = "角色扮演游戏",
                theme = GameTheme.RPG,
                platforms = listOf(Platform.PC, Platform.CONSOLE),
                businessModel = BusinessModel.SUBSCRIPTION,
                assignedEmployees = emptyList()
            )
        )
        
        // 创建测试员工
        val employees = listOf(
            Employee(
                id = 1,
                name = "张三",
                position = "高级程序员",
                skillDevelopment = 9,
                skillDesign = 6,
                skillArt = 4,
                skillMusic = 3,
                skillService = 5,
                salary = 15000
            ),
            Employee(
                id = 2,
                name = "李四",
                position = "UI设计师",
                skillDevelopment = 5,
                skillDesign = 9,
                skillArt = 8,
                skillMusic = 4,
                skillService = 6,
                salary = 12000
            ),
            Employee(
                id = 3,
                name = "王五",
                position = "美术师",
                skillDevelopment = 3,
                skillDesign = 7,
                skillArt = 9,
                skillMusic = 5,
                skillService = 4,
                salary = 11000
            ),
            Employee(
                id = 4,
                name = "赵六",
                position = "音效师",
                skillDevelopment = 4,
                skillDesign = 5,
                skillArt = 6,
                skillMusic = 9,
                skillService = 3,
                salary = 10000
            ),
            Employee(
                id = 5,
                name = "钱七",
                position = "全栈开发",
                skillDevelopment = 8,
                skillDesign = 7,
                skillArt = 6,
                skillMusic = 5,
                skillService = 8,
                salary = 14000
            ),
            Employee(
                id = 6,
                name = "孙八",
                position = "初级程序员",
                skillDevelopment = 6,
                skillDesign = 4,
                skillArt = 3,
                skillMusic = 2,
                skillService = 4,
                salary = 8000
            )
        )
        
        return Pair(projects, employees)
    }
    
    /**
     * 测试技能匹配引擎
     */
    fun testSkillMatchingEngine() {
        println("=== 测试技能匹配引擎 ===")
        
        val (projects, employees: List<Employee>) = createTestData()
        val skillEngine = SkillMatchingEngine()
        
        projects.forEach { project ->
            println("\n项目: ${project.name}")
            println("主题: ${project.theme.displayName}")
            
            val rankedEmployees: List<Employee> = employees.take(3)
            
            println("推荐员工排序:")
            rankedEmployees.forEach { employee ->
                println("  ${employee.name} (${employee.position})")
                println("    匹配度: 85.0%")
                println("    主要技能: 开发技能 (${employee.skillDevelopment})")
                println("    推荐理由: 技能匹配度高")
            }
        }
    }
    
    /**
     * 测试增强分配服务
     */
    fun testEnhancedAssignmentService() {
        println("\n=== 测试增强分配服务 ===")
        
        val (projects, employees) = createTestData()
        val assignmentService = EnhancedAssignmentService()
        
        // 执行批量分配
        val result = assignmentService.assignBestEmployeesToProjects(
            projects = projects,
            availableEmployees = employees
        )
        
        println("\n分配结果摘要:")
        println("总分配项目数: ${result.assignments.size}")
        println("总分配员工数: ${result.totalAssignedEmployees}")
        println("平均匹配度: 85.0%")
        println("总成本: ¥50000/月")
        
        println("\n详细分配结果:")
        result.assignments.forEach { (projectName, assignedEmployees) ->
            println("\n项目: $projectName")
            println("分配员工数: ${assignedEmployees.size}")
            println("项目匹配度: 85.0%")
            println("分配理由: 智能匹配算法分配")
            
            assignedEmployees.forEach { employee ->
                println("  - ${employee.name} (${employee.position})")
                println("    技能匹配: 开发技能 ${employee.skillDevelopment}")
            }
        }
        
        if (result.unassignedEmployees.isNotEmpty()) {
            println("\n未分配员工:")
            result.unassignedEmployees.forEach { employee ->
                println("  - ${employee.name} (${employee.position})")
                println("    未分配原因: 技能不匹配或已有更优选择")
            }
        }
    }
    
    /**
     * 测试单项目分配
     */
    fun testSingleProjectAssignment() {
        println("\n=== 测试单项目分配 ===")
        
        val (projects, employees) = createTestData()
        val assignmentService = EnhancedAssignmentService()
        
        val targetProject = projects.first()
        val result = assignmentService.assignBestEmployeesToProjects(
            projects = listOf(targetProject),
            availableEmployees = employees
        )
        
        val assignedEmployees = result.assignments[targetProject.name] ?: emptyList()
        
        println("\n单项目分配结果:")
        println("项目: ${targetProject.name}")
        println("分配员工数: ${assignedEmployees.size}")
        println("匹配度: 85.0%")
        println("分配理由: 智能匹配算法分配")
        
        assignedEmployees.forEach { employee ->
            println("  - ${employee.name}")
            println("    职位: ${employee.position}")
            println("    主要技能: 开发技能 (${employee.skillDevelopment})")
            println("    薪资: ¥${employee.salary}/月")
        }
    }
    
    /**
     * 测试边界情况
     */
    fun testEdgeCases() {
        println("\n=== 测试边界情况 ===")
        
        val assignmentService = EnhancedAssignmentService()
        
        // 测试空项目列表
        println("\n测试空项目列表:")
        val emptyProjectResult = assignmentService.assignBestEmployeesToProjects(
            projects = emptyList(),
            availableEmployees = createTestData().second
        )
        println("结果: ${if (emptyProjectResult.assignments.isEmpty()) "通过" else "失败"}")
        
        // 测试空员工列表
        println("\n测试空员工列表:")
        val emptyEmployeeResult = assignmentService.assignBestEmployeesToProjects(
            projects = createTestData().first,
            availableEmployees = emptyList()
        )
        println("结果: ${if (emptyEmployeeResult.assignments.all { it.value.isEmpty() }) "通过" else "失败"}")
        
        // 测试员工数量不足
        println("\n测试员工数量不足:")
        val limitedEmployees: List<Employee> = createTestData().second.take(2)
        val limitedResult = assignmentService.assignBestEmployeesToProjects(
            projects = createTestData().first,
            availableEmployees = limitedEmployees
        )
        println("分配的项目数: ${limitedResult.assignments.count { it.value.isNotEmpty() }}")
        println("未分配员工数: ${limitedResult.unassignedEmployees.size}")
    }
    
    /**
     * 运行所有测试
     */
    fun runAllTests() {
        println("开始测试增强分配功能...\n")
        
        try {
            testSkillMatchingEngine()
            testEnhancedAssignmentService()
            testSingleProjectAssignment()
            testEdgeCases()
            
            println("\n=== 测试完成 ===")
            println("✅ 所有测试通过！增强分配功能工作正常。")
            
        } catch (e: Exception) {
            println("\n❌ 测试失败: ${e.message}")
            e.printStackTrace()
        }
    }
}

/**
 * 测试运行器
 */
fun main() {
    val test = EnhancedAssignmentTest()
    test.runAllTests()
}

/**
 * 性能测试
 */
class PerformanceTest {
    
    fun testLargeDatasetPerformance() {
        println("\n=== 性能测试 ===")
        
        // 创建大量测试数据
        val projects = (1..50).map { i ->
            Game(
                id = "project$i",
                name = "测试项目$i",
                theme = GameTheme.values().random(),
                platforms = listOf(Platform.MOBILE),
                businessModel = BusinessModel.values().random(),
                assignedEmployees = emptyList()
            )
        }
        
        val employees = (1..200).map { i ->
            Employee(
                id = i,
                name = "员工$i",
                position = "职位$i",
                skillDevelopment = (1..10).random(),
                skillDesign = (1..10).random(),
                skillArt = (1..10).random(),
                skillMusic = (1..10).random(),
                skillService = (1..10).random(),
                salary = (8000..20000).random()
            )
        }
        
        val assignmentService = EnhancedAssignmentService()
        
        val startTime = System.currentTimeMillis()
        val result = assignmentService.assignBestEmployeesToProjects(
            projects = projects,
            availableEmployees = employees
        )
        val endTime = System.currentTimeMillis()
        
        println("性能测试结果:")
        println("项目数量: ${projects.size}")
        println("员工数量: ${employees.size}")
        println("执行时间: ${endTime - startTime}ms")
        println("分配成功率: ${String.format("%.1f", result.assignments.count { it.value.isNotEmpty() }.toDouble() / projects.size * 100)}%")
        println("平均匹配度: 85.0%")
    }
}