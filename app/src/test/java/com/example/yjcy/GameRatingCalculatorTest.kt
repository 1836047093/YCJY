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
            skillDesign = 3,
            skillArt = 2,
            skillMusic = 1,
            skillService = 4
        )
        
        val employee2 = Employee(
            id = 2,
            name = "李四",
            position = "策划师",
            skillDevelopment = 3,
            skillDesign = 5,
            skillArt = 4,
            skillMusic = 2,
            skillService = 1
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
        assertEquals("基础分应该是5.0", 5.0f, rating.baseScore, 0.01f)
        
        // 计算预期的技能加成
        // 员工1：程序员，专业技能开发5级，贡献 5/2 = 2.5
        // 员工2：策划师，专业技能设计5级，贡献 5/2 = 2.5
        // 总技能加成：2.5 + 2.5 = 5.0
        val expectedSkillBonus = 2.5f + 2.5f
        assertEquals("技能加成应该是5.0", expectedSkillBonus, rating.skillBonus, 0.01f)
        
        // 最终评分：5 + 5 = 10.0
        val expectedFinalScore = 5.0f + expectedSkillBonus
        assertEquals("最终评分应该是10.0", expectedFinalScore, rating.finalScore, 0.01f)
        
        // 验证技能贡献列表
        assertEquals("应该有2个员工的技能贡献", 2, rating.skillContributions.size)
        
        val contribution1 = rating.skillContributions.find { it.employeeId == 1 }
        assertNotNull("员工1的贡献不应为空", contribution1)
        assertEquals("员工1的贡献应该是2.5", 2.5f, contribution1?.contribution ?: 0f, 0.01f)
        
        val contribution2 = rating.skillContributions.find { it.employeeId == 2 }
        assertNotNull("员工2的贡献不应为空", contribution2)
        assertEquals("员工2的贡献应该是2.5", 2.5f, contribution2?.contribution ?: 0f, 0.01f)
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
        
        // 基础分5 + 技能加成5/2 = 7.5
        assertEquals("单员工评分应该是7.5", 7.5f, rating.finalScore, 0.01f)
        assertEquals("技能加成应该是2.5", 2.5f, rating.skillBonus, 0.01f)
    }
    
    @Test
    fun testMaxScoreLimit() {
        // 测试评分上限
        val superEmployee = Employee(
            id = 1,
            name = "超级员工",
            position = "程序员",
            skillDevelopment = 10,
            skillDesign = 10,
            skillArt = 10,
            skillMusic = 10,
            skillService = 10
        )
        
        val game = Game(
            id = "1",
            name = "超级游戏",
            theme = GameTheme.ACTION,
            platforms = listOf(Platform.PC),
            businessModel = BusinessModel.SINGLE_PLAYER,
            assignedEmployees = listOf(superEmployee),
            developmentProgress = 1.0f,
            isCompleted = true
        )
        
        val rating = GameRatingCalculator.calculateRating(game)
        
        // 评分不应超过10分
        assertTrue("评分不应超过10分", rating.finalScore <= 10.0f)
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
        
        // 只有基础分5分
        assertEquals("无员工游戏评分应该是5.0", 5.0f, rating.finalScore, 0.01f)
        assertEquals("技能加成应该是0", 0.0f, rating.skillBonus, 0.01f)
        assertTrue("技能贡献列表应该为空", rating.skillContributions.isEmpty())
    }
}