package com.example.yjcy.data

import androidx.compose.runtime.Immutable

/**
 * 招聘配置数据模型
 * 用于存储玩家设置的招聘条件
 */
@Immutable
data class RecruitmentConfig(
    val id: Int = 0,
    val positionType: String, // 职位类型：程序员、美术师、策划师等
    val minSkillLevel: Int = 1, // 最低技能等级要求
    val maxSkillLevel: Int = 10, // 最高技能等级要求
    val minSalary: Int, // 最低薪资预算
    val maxSalary: Int, // 最高薪资预算
    val minAge: Int = 18, // 最低年龄要求
    val maxAge: Int = 65, // 最高年龄要求
    val minExperience: Int = 0, // 最低工作经验要求（年）
    val maxExperience: Int = 20, // 最高工作经验要求（年）
    val specialRequirements: String = "", // 特殊技能要求
    val targetCount: Int = 1, // 目标招聘人数
    val isActive: Boolean = true, // 是否激活此配置
    val priority: RecruitmentPriority = RecruitmentPriority.NORMAL, // 招聘优先级
    val autoApprove: Boolean = false, // 是否自动批准高匹配度候选人
    val autoApproveThreshold: Float = 0.9f, // 自动批准的匹配度阈值
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 验证配置是否有效
     */
    fun isValid(): Boolean {
        return positionType.isNotBlank() &&
                minSkillLevel in 1..10 &&
                maxSkillLevel in 1..10 &&
                minSkillLevel <= maxSkillLevel &&
                minSalary > 0 &&
                maxSalary > 0 &&
                minSalary <= maxSalary &&
                minAge in 18..65 &&
                maxAge in 18..65 &&
                minAge <= maxAge &&
                targetCount > 0
    }
    
    /**
     * 获取薪资范围描述
     */
    fun getSalaryRangeDescription(): String {
        return "¥${minSalary} - ¥${maxSalary}"
    }
    
    /**
     * 获取技能等级范围描述
     */
    fun getSkillLevelRangeDescription(): String {
        return "${minSkillLevel}级 - ${maxSkillLevel}级"
    }
    
    /**
     * 获取年龄范围描述
     */
    fun getAgeRangeDescription(): String {
        return "${minAge}岁 - ${maxAge}岁"
    }
    
    /**
     * 获取工作经验范围描述
     */
    fun getExperienceRangeDescription(): String {
        return "${minExperience}年 - ${maxExperience}年"
    }
    
    /**
     * 检查候选人是否符合基本条件
     */
    fun matchesBasicRequirements(candidate: Candidate): Boolean {
        val candidateSkillLevel = candidate.getSpecialtySkillLevel()
        val candidateAge = calculateAgeFromExperience(candidate.experienceYears)
        
        return candidate.position == positionType &&
                candidateSkillLevel in minSkillLevel..maxSkillLevel &&
                candidate.expectedSalary in minSalary..maxSalary &&
                candidateAge in minAge..maxAge &&
                candidate.experienceYears in minExperience..maxExperience
    }
    
    /**
     * 根据工作经验估算年龄（简化计算）
     */
    private fun calculateAgeFromExperience(experienceYears: Int): Int {
        return 22 + experienceYears // 假设22岁开始工作
    }
}

/**
 * 招聘优先级枚举
 */
enum class RecruitmentPriority {
    LOW,     // 低优先级
    NORMAL,  // 普通优先级
    HIGH,    // 高优先级
    URGENT   // 紧急优先级
}

/**
 * 预定义的招聘配置模板
 */
object RecruitmentConfigTemplates {
    
    /**
     * 初级程序员配置
     */
    fun juniorProgrammer(minSalary: Int = 5000, maxSalary: Int = 8000): RecruitmentConfig {
        return RecruitmentConfig(
            positionType = "程序员",
            minSkillLevel = 1,
            maxSkillLevel = 3,
            minSalary = minSalary,
            maxSalary = maxSalary,
            minExperience = 0,
            maxExperience = 2,
            specialRequirements = "熟悉基础编程语言"
        )
    }
    
    /**
     * 高级程序员配置
     */
    fun seniorProgrammer(minSalary: Int = 12000, maxSalary: Int = 20000): RecruitmentConfig {
        return RecruitmentConfig(
            positionType = "程序员",
            minSkillLevel = 7,
            maxSkillLevel = 10,
            minSalary = minSalary,
            maxSalary = maxSalary,
            minExperience = 5,
            maxExperience = 15,
            specialRequirements = "精通多种编程语言，有架构设计经验"
        )
    }
    
    /**
     * 美术师配置
     */
    fun artist(minSalary: Int = 6000, maxSalary: Int = 12000): RecruitmentConfig {
        return RecruitmentConfig(
            positionType = "美术师",
            minSkillLevel = 3,
            maxSkillLevel = 8,
            minSalary = minSalary,
            maxSalary = maxSalary,
            minExperience = 1,
            maxExperience = 10,
            specialRequirements = "熟练使用设计软件"
        )
    }
    
    /**
     * 策划师配置
     */
    fun planner(minSalary: Int = 5500, maxSalary: Int = 11000): RecruitmentConfig {
        return RecruitmentConfig(
            positionType = "策划师",
            minSkillLevel = 2,
            maxSkillLevel = 7,
            minSalary = minSalary,
            maxSalary = maxSalary,
            minExperience = 0,
            maxExperience = 8,
            specialRequirements = "有游戏策划经验"
        )
    }
    
    /**
     * 获取所有预定义模板
     */
    fun getAllTemplates(): List<RecruitmentConfig> {
        return listOf(
            juniorProgrammer(),
            seniorProgrammer(),
            artist(),
            planner()
        )
    }
}