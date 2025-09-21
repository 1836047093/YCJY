package com.example.yjcy.data

/**
 * 员工数据类 - data包版本
 * 用于数据处理和业务逻辑
 */
data class Employee(
    val id: Int,
    val name: String,
    val position: String,
    val skillDevelopment: Int,
    val skillDesign: Int,
    val skillArt: Int,
    val skillMusic: Int,
    val skillService: Int,
    val salary: Int
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
     * 获取员工的主要技能值
     */
    fun getPrimarySkillValue(): Int {
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

/**
 * 将ui包的Employee转换为data包的Employee
 */
fun com.example.yjcy.ui.Employee.toDataEmployee(): Employee {
    return Employee(
        id = this.id,
        name = this.name,
        position = this.position,
        skillDevelopment = this.skillDevelopment,
        skillDesign = this.skillDesign,
        skillArt = this.skillArt,
        skillMusic = this.skillMusic,
        skillService = this.skillService,
        salary = this.salary
    )
}

/**
 * 将data包的Employee转换为ui包的Employee
 */
fun Employee.toUiEmployee(): com.example.yjcy.ui.Employee {
    return com.example.yjcy.ui.Employee(
        id = this.id,
        name = this.name,
        position = this.position,
        skillDevelopment = this.skillDevelopment,
        skillDesign = this.skillDesign,
        skillArt = this.skillArt,
        skillMusic = this.skillMusic,
        skillService = this.skillService,
        salary = this.salary
    )
}