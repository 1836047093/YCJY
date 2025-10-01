package com.example.yjcy.data

import kotlin.random.Random

/**
 * 人才候选人数据类
 * 用于人才市场功能，表示可招聘的候选人信息
 */
data class TalentCandidate(
    val id: String,
    val name: String,
    val position: String,
    val skillDevelopment: Int,
    val skillDesign: Int,
    val skillArt: Int,
    val skillMusic: Int,
    val skillService: Int,
    val expectedSalary: Int,
    val experience: Int,
    val avatar: String? = null
) {
    /**
     * 计算总技能点数
     */
    fun getTotalSkill(): Int = skillDevelopment + skillDesign + skillArt + skillMusic + skillService
    
    /**
     * 获取最高技能等级
     */
    fun getMaxSkillLevel(): Int = maxOf(skillDevelopment, skillDesign, skillArt, skillMusic, skillService)
    
    /**
     * 获取技能等级分类
     */
    fun getSkillLevel(): String = when (getMaxSkillLevel()) {
        in 1..2 -> "初级"
        3 -> "中级"
        4 -> "高级"
        5 -> "专家级"
        else -> "未知"
    }
    
    /**
     * 获取技能等级分类（使用SkillConstants）
     */
    fun getSkillCategory(): SkillConstants.SkillCategory? {
        return SkillConstants.getSkillCategory(getMaxSkillLevel())
    }
    
    /**
     * 转换为Employee对象
     */
    fun toEmployee(newId: Int): Employee {
        return Employee(
            id = newId,
            name = name,
            position = position,
            skillDevelopment = skillDevelopment,
            skillDesign = skillDesign,
            skillArt = skillArt,
            skillMusic = skillMusic,
            skillService = skillService,
            salary = expectedSalary,
            experience = experience,
            motivation = Random.nextInt(80, 101), // 新员工动机较高
            isFounder = false
        )
    }
    
    /**
     * 获取专业技能等级（根据职位）
     */
    fun getProfessionalSkillLevel(): Int {
        return when (position) {
            "程序员" -> skillDevelopment
            "策划师" -> skillDesign
            "美术师" -> skillArt
            "音效师" -> skillMusic
            "客服" -> skillService
            else -> getMaxSkillLevel()
        }
    }
    
    /**
     * 检查是否符合筛选条件
     */
    fun matchesFilter(criteria: FilterCriteria): Boolean {
        // 职位筛选
        val positionMatch = criteria.selectedPosition?.let { 
            position == it 
        } ?: true
        
        // 技能等级筛选（基于最高技能等级）
        val maxSkill = getMaxSkillLevel()
        val skillMatch = maxSkill in criteria.minSkillLevel..criteria.maxSkillLevel
        
        // 薪资筛选
        val salaryMatch = expectedSalary in criteria.minSalary..criteria.maxSalary
        
        return positionMatch && skillMatch && salaryMatch
    }
}