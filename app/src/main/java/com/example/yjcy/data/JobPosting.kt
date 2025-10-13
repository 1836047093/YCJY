package com.example.yjcy.data

import java.util.*

/**
 * 岗位发布数据类
 * 用于玩家发布招聘岗位
 */
data class JobPosting(
    val id: String,
    val position: String, // 岗位类型
    val requiredSkillType: String, // 要求的专属技能类型
    val minSkillLevel: Int, // 最低技能等级
    val minSalary: Int, // 薪资范围最小值
    val maxSalary: Int, // 薪资范围最大值
    val postedDate: Date, // 发布日期
    val status: JobPostingStatus = JobPostingStatus.ACTIVE, // 岗位状态
    val applicants: List<JobApplicant> = emptyList() // 应聘者列表
) {
    /**
     * 获取岗位描述
     */
    fun getDescription(): String {
        return "招聘 $position - $requiredSkillType Lv.$minSkillLevel - ¥${minSalary}-${maxSalary}"
    }
    
    /**
     * 检查候选人是否符合岗位要求
     */
    fun matchesRequirements(candidate: TalentCandidate): Boolean {
        // 检查职位是否匹配
        val positionMatch = candidate.position == position
        
        // 检查技能等级是否达标
        val skillLevel = when (requiredSkillType) {
            "开发" -> candidate.skillDevelopment
            "设计" -> candidate.skillDesign
            "美工" -> candidate.skillArt
            "音乐" -> candidate.skillMusic
            "服务" -> candidate.skillService
            else -> 0
        }
        val skillMatch = skillLevel >= minSkillLevel
        
        // 检查期望薪资是否在范围内
        val salaryMatch = candidate.expectedSalary in minSalary..maxSalary
        
        return positionMatch && skillMatch && salaryMatch
    }
    
    /**
     * 获取待处理的应聘者数量
     */
    fun getPendingApplicantsCount(): Int {
        return applicants.count { it.status == ApplicantStatus.PENDING }
    }
    
    /**
     * 获取已通过的应聘者数量
     */
    fun getAcceptedApplicantsCount(): Int {
        return applicants.count { it.status == ApplicantStatus.ACCEPTED }
    }
}

/**
 * 岗位发布状态
 */
enum class JobPostingStatus {
    ACTIVE,     // 活跃中
    PAUSED,     // 已暂停
    CLOSED,     // 已关闭
    FILLED      // 已满员
}

/**
 * 应聘者数据类
 */
data class JobApplicant(
    val id: String,
    val candidate: TalentCandidate, // 候选人信息
    val applicationDate: Date, // 应聘日期
    val status: ApplicantStatus = ApplicantStatus.PENDING, // 应聘状态
    val interviewScore: Int? = null, // 面试评分（如果已面试）
    val interviewNotes: String? = null // 面试备注
) {
    /**
     * 获取匹配度（基于技能等级）
     */
    fun getMatchScore(): Float {
        val maxSkill = candidate.getMaxSkillLevel()
        return maxSkill / 5.0f
    }
}

/**
 * 应聘者状态
 */
enum class ApplicantStatus {
    PENDING,    // 待处理
    REVIEWING,  // 审核中
    INTERVIEWING, // 面试中
    ACCEPTED,   // 已通过
    REJECTED,   // 已拒绝
    HIRED       // 已雇佣
}

/**
 * 面试类型
 */
enum class InterviewType {
    PLAYER,     // 玩家面试
    HR          // 人事面试
}

/**
 * 面试结果
 */
data class InterviewResult(
    val applicantId: String,
    val interviewType: InterviewType,
    val score: Int, // 1-100
    val passed: Boolean,
    val notes: String,
    val interviewDate: Date = Date()
) {
    /**
     * 获取评级
     */
    fun getRating(): String {
        return when (score) {
            in 90..100 -> "优秀"
            in 80..89 -> "良好"
            in 70..79 -> "中等"
            in 60..69 -> "及格"
            else -> "不合格"
        }
    }
}

