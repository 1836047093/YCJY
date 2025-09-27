package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.Platform
import com.example.yjcy.ui.BusinessModel

// 员工排序枚举
enum class EmployeeSortBy {
    NAME,        // 按姓名排序
    POSITION,    // 按职位排序
    SALARY,      // 按薪资排序
    SKILL_LEVEL, // 按技能等级排序
    HIRE_DATE    // 按入职时间排序
}

// 创始人职业枚举
enum class FounderProfession(val displayName: String, val icon: String, val specialtySkill: String) {
    PROGRAMMER("程序员", "💻", "开发"),
    DESIGNER("策划师", "📋", "设计"),
    ARTIST("美术师", "🎨", "美工"),
    SOUND_ENGINEER("音效师", "🎵", "音乐"),
    CUSTOMER_SERVICE("客服", "📞", "服务")
}

// 员工数据类
data class Employee(
    val id: Int,
    val name: String,
    val position: String,
    val skillDevelopment: Int = 0,
    val skillDesign: Int = 0,
    val skillArt: Int = 0,
    val skillMusic: Int = 0,
    val skillService: Int = 0,
    val salary: Int = 0,
    val experience: Int = 0,
    val motivation: Int = 100,
    val isFounder: Boolean = false
) {
    /**
     * 获取员工的专属技能类型
     */
    fun getSpecialtySkillType(): String {
        return when (position) {
            "程序员" -> "开发"
            "策划师" -> "设计"
            "美术师" -> "美工"
            "音效师" -> "音乐"
            "客服" -> "服务"
            else -> "通用"
        }
    }
    
    /**
     * 获取员工的专属技能等级
     */
    fun getSpecialtySkillLevel(): Int {
        return when (getSpecialtySkillType()) {
            "开发" -> skillDevelopment
            "设计" -> skillDesign
            "美工" -> skillArt
            "音乐" -> skillMusic
            "服务" -> skillService
            else -> maxOf(skillDevelopment, skillDesign, skillArt, skillMusic, skillService)
        }
    }
    
    /**
     * 获取员工的主要技能值
     */
    fun getPrimarySkillValue(): Int {
        return getSpecialtySkillLevel()
    }
    
    /**
     * 获取员工的总技能点数
     */
    fun getTotalSkillPoints(): Int {
        return skillDevelopment + skillDesign + skillArt + skillMusic + skillService
    }
    
    /**
     * 检查员工是否具备指定技能
     */
    fun hasSkill(skillType: String, minLevel: Int = 1): Boolean {
        val skillValue = when (skillType) {
            "开发" -> skillDevelopment
            "设计" -> skillDesign
            "美工" -> skillArt
            "音乐" -> skillMusic
            "服务" -> skillService
            else -> 0
        }
        return skillValue >= minLevel
    }
    
    /**
     * 获取指定技能的等级
     */
    fun getSkillLevel(skillType: String): Int {
        return when (skillType) {
            "开发" -> skillDevelopment
            "设计" -> skillDesign
            "美工" -> skillArt
            "音乐" -> skillMusic
            "服务" -> skillService
            else -> 0
        }
    }
}

// 创始人数据类
data class Founder(
    val name: String,
    val profession: FounderProfession,
    val skillLevel: Int = 5 // 固定为5级
) {
    fun toEmployee(): Employee {
        return Employee(
            id = 0, // 特殊ID标识创始人
            name = name,
            position = profession.displayName,
            skillDevelopment = if (profession.specialtySkill == "开发") 5 else 0,
            skillDesign = if (profession.specialtySkill == "设计") 5 else 0,
            skillArt = if (profession.specialtySkill == "美工") 5 else 0,
            skillMusic = if (profession.specialtySkill == "音乐") 5 else 0,
            skillService = if (profession.specialtySkill == "服务") 5 else 0,
            salary = 0, // 创始人无薪资
            isFounder = true
        )
    }
}

// 游戏相关数据类
data class Game(
    val id: String,
    val name: String,
    val theme: GameTheme,
    val platforms: List<Platform>,
    val businessModel: BusinessModel,
    val developmentProgress: Float = 0f,
    val isCompleted: Boolean = false,
    val revenue: Long = 0L,
    val assignedEmployees: List<Employee> = emptyList() // 新增：已分配的员工列表
)

// 存档数据类
data class SaveData(
    val companyName: String = "我的游戏公司",
    val founderName: String = "创始人",
    val founderProfession: FounderProfession? = null, // 新增字段，向后兼容
    val money: Long = 1000000L,
    val fans: Int = 0,
    val currentYear: Int = 1,
    val currentMonth: Int = 1,
    val currentDay: Int = 1,
    val allEmployees: List<Employee> = emptyList(),
    val games: List<Game> = emptyList(),
    val saveTime: Long = System.currentTimeMillis()
)