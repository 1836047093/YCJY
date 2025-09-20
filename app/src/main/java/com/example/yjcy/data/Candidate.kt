package com.example.yjcy.data

import androidx.compose.runtime.Immutable

@Immutable
data class Candidate(
    val id: Int = 0,
    val name: String,
    val position: String,
    val programmingSkill: Int = 1,
    val designSkill: Int = 1,
    val planningSkill: Int = 1,
    val soundSkill: Int = 1,
    val customerServiceSkill: Int = 1,
    val expectedSalary: Int,
    val experienceYears: Int = 0,
    val educationLevel: String = "本科",
    val availabilityStatus: AvailabilityStatus = AvailabilityStatus.AVAILABLE,
    val recruitmentCost: Int,
    val successRate: Float = 0.7f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取候选人的专属技能类型
     */
    fun getSpecialtySkillType(): String {
        return when (position) {
            "程序员" -> "开发"
            "美术师" -> "美工"
            "策划师" -> "策划"
            "音效师" -> "音效"
            "客服" -> "客服"
            else -> "综合"
        }
    }
    
    /**
     * 获取候选人的专属技能等级
     */
    fun getSpecialtySkillLevel(): Int {
        return when (position) {
            "程序员" -> programmingSkill
            "美术师" -> designSkill
            "策划师" -> planningSkill
            "音效师" -> soundSkill
            "客服" -> customerServiceSkill
            else -> maxOf(programmingSkill, designSkill, planningSkill, soundSkill, customerServiceSkill)
        }
    }
    
    /**
     * 获取候选人的综合评分
     */
    fun getOverallRating(): Float {
        val skillAverage = (programmingSkill + designSkill + planningSkill + soundSkill + customerServiceSkill) / 5.0f
        val experienceBonus = minOf(experienceYears * 0.1f, 1.0f)
        val educationBonus = when (educationLevel) {
            "博士" -> 0.3f
            "硕士" -> 0.2f
            "本科" -> 0.1f
            else -> 0.0f
        }
        return skillAverage + experienceBonus + educationBonus
    }
    
    /**
     * 计算招聘成功概率
     */
    fun calculateRecruitmentProbability(companyReputation: Float = 0.7f): Float {
        val baseRate = successRate
        val reputationBonus = companyReputation * 0.2f
        val salaryFactor = if (expectedSalary <= 8000) 0.1f else -0.1f
        return minOf(1.0f, maxOf(0.1f, baseRate + reputationBonus + salaryFactor))
    }
    
    /**
     * 获取平均技能等级
     */
    fun getAverageSkillLevel(): Float {
        return (programmingSkill + designSkill + planningSkill + soundSkill + customerServiceSkill) / 5.0f
    }
    
    /**
     * 获取招聘成功率
     */
    fun getHireSuccessRate(): Float {
        return successRate
    }
    
    /**
     * 获取综合评分（用于显示）
     */
    fun getOverallScore(): Float {
        return getOverallRating()
    }
}

enum class AvailabilityStatus {
    AVAILABLE,    // 可招聘
    INTERVIEWING, // 面试中
    HIRED        // 已被雇佣
}

/**
 * 候选人筛选条件
 */
data class CandidateFilter(
    val searchQuery: String = "",
    val positions: List<String> = emptyList(),
    val minSalary: Int? = null,
    val maxSalary: Int? = null,
    val minExperience: Int? = null,
    val maxExperience: Int? = null,
    val educationLevels: List<String> = emptyList(),
    val availabilityStatus: AvailabilityStatus? = null,
    val minSkillLevel: Int? = null
)

/**
 * 候选人排序方式
 */
enum class CandidateSortBy {
    NAME,           // 按姓名排序
    SALARY,         // 按期望薪资排序
    EXPERIENCE,     // 按工作经验排序
    SKILL_LEVEL,    // 按技能等级排序
    SUCCESS_RATE,   // 按招聘成功率排序
    RECRUITMENT_COST, // 按招聘成本排序
    CREATED_AT      // 按创建时间排序
}