package com.example.yjcy

import com.example.yjcy.data.*
import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.Platform
import com.example.yjcy.ui.BusinessModel
import org.junit.Test
import org.junit.Assert.*

class GameRatingCalculatorTest {

    @Test
    fun testGameRatingCalculation() {
        // 创建测试员工
        val employee1 = Employee(
            id = 1,
            name = "张三",
            position = "程序员",
            skillDevelopment = 5,
            skillDesign = 0,
            skillArt = 0,
            skillMusic = 0,
            skillService = 0
        )
        
        val employee2 = Employee(
            id = 2,
            name = "李四",
            position = "策划师",
            skillDevelopment = 0,
            skillDesign = 5,
            skillArt = 0,
            skillMusic = 0,
            skillService = 0
        )
        
        // 创建测试游戏
        val game = Game(
            id = "1",
            name = "测试游戏",
            theme = GameTheme.ACTION,
            platforms = listOf(Platform.PC),
            businessModel = BusinessModel.SINGLE_PLAYER,
            assignedEmployees = listOf(employee1, employee2),
            developmentProgress = 1.0f,
            isCompleted = true
        )
        
        // 计算评分
        val rating = GameRatingCalculator.calculateRating(game)
        
        // 验证评分结果
        assertNotNull("评分不应为空", rating)
        assertEquals("游戏ID应该匹配", "1", rating.gameId)
        assertEquals("基础分应该是2.0", 2.0f, rating.baseScore, 0.01f)
        
        // 计算预期的技能加成（当前系统递减收益，1-5级）
        // 员工1：程序员，专业技能开发5级，贡献 0.25
        // 员工2：策划师，专业技能设计5级，贡献 0.25
        // 总技能加成：0.50
        val expectedSkillBonus = 0.25f + 0.25f
        assertEquals("技能加成应该是0.50", expectedSkillBonus, rating.skillBonus, 0.01f)
        
        // 当前系统预期评分：
        // 基础分2.0 + 技能0.50 + 团队协作0.4(2职位) + 平衡性0.6(完全一致) + 精英0.6(全≥4级) = 4.10
        val expectedFinalScore = 4.10f
        assertEquals("最终评分应该约为4.1", expectedFinalScore, rating.finalScore, 0.1f)
        
        // 验证技能贡献列表
        assertEquals("应该有2个员工的技能贡献", 2, rating.skillContributions.size)
        
        val contribution1 = rating.skillContributions.find { it.employeeId == 1 }
        assertNotNull("员工1的贡献不应为空", contribution1)
        assertEquals("员工1的贡献应该是0.85", 0.85f, contribution1?.contribution ?: 0f, 0.01f)
        
        val contribution2 = rating.skillContributions.find { it.employeeId == 2 }
        assertNotNull("员工2的贡献不应为空", contribution2)
        assertEquals("员工2的贡献应该是0.85", 0.85f, contribution2?.contribution ?: 0f, 0.01f)
    }
    
    @Test
    fun testSingleEmployeeRating() {
        // 测试单个员工的情况
        val employee = Employee(
            id = 1,
            name = "独行侠",
            position = "程序员",
            skillDevelopment = 5,
            skillDesign = 0,
            skillArt = 0,
            skillMusic = 0,
            skillService = 0
        )
        
        val game = Game(
            id = "1",
            name = "独立游戏",
            theme = GameTheme.ACTION,
            platforms = listOf(Platform.PC),
            businessModel = BusinessModel.SINGLE_PLAYER,
            assignedEmployees = listOf(employee),
            developmentProgress = 1.0f,
            isCompleted = true
        )
        
        val rating = GameRatingCalculator.calculateRating(game)
        
        // 当前系统：基础分2.0 + 技能0.25 + 团队协作0(单职位) + 平衡性0 + 精英0.6(100%≥4级) = 2.85
        assertEquals("单员工评分应该约为2.85", 2.85f, rating.finalScore, 0.1f)
        assertEquals("技能加成应该是0.25", 0.25f, rating.skillBonus, 0.01f)
    }
    
    @Test
    fun testMaxScoreLimit() {
        // 测试接近满分的完美配置（5级上限）
        val employee1 = Employee(id = 1, name = "程序1", position = "程序员", skillDevelopment = 5, skillDesign = 0, skillArt = 0, skillMusic = 0, skillService = 0)
        val employee2 = Employee(id = 2, name = "策划1", position = "策划师", skillDevelopment = 0, skillDesign = 5, skillArt = 0, skillMusic = 0, skillService = 0)
        val employee3 = Employee(id = 3, name = "美工1", position = "美工", skillDevelopment = 0, skillDesign = 0, skillArt = 5, skillMusic = 0, skillService = 0)
        val employee4 = Employee(id = 4, name = "音乐1", position = "音乐家", skillDevelopment = 0, skillDesign = 0, skillArt = 0, skillMusic = 5, skillService = 0)
        val employee5 = Employee(id = 5, name = "客服1", position = "客服", skillDevelopment = 0, skillDesign = 0, skillArt = 0, skillMusic = 0, skillService = 5)
        
        val game = Game(
            id = "1",
            name = "完美游戏",
            theme = GameTheme.ACTION,
            platforms = listOf(Platform.PC),
            businessModel = BusinessModel.SINGLE_PLAYER,
            assignedEmployees = listOf(employee1, employee2, employee3, employee4, employee5),
            developmentProgress = 1.0f,
            isCompleted = true
        )
        
        val rating = GameRatingCalculator.calculateRating(game)
        
        // 评分不应超过10分
        assertTrue("评分不应超过10分", rating.finalScore <= 10.0f)
        // 完美配置（4个5级不同岗位，无客服）应具备明显优势
        assertEquals("满配四职能至少应达到5.0分", 5.0f, rating.finalScore, 0.1f)
    }
    
    @Test
    fun testNoEmployeesRating() {
        // 测试没有员工的情况
        val game = Game(
            id = "1",
            name = "空游戏",
            theme = GameTheme.ACTION,
            platforms = listOf(Platform.PC),
            businessModel = BusinessModel.SINGLE_PLAYER,
            assignedEmployees = emptyList(),
            developmentProgress = 1.0f,
            isCompleted = true
        )
        
        val rating = GameRatingCalculator.calculateRating(game)
        
        // 新系统只有基础分2.0分
        assertEquals("无员工游戏评分应该是2.0", 2.0f, rating.finalScore, 0.01f)
        assertEquals("技能加成应该是0", 0.0f, rating.skillBonus, 0.01f)
        assertTrue("技能贡献列表应该为空", rating.skillContributions.isEmpty())
    }
}