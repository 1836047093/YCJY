package com.example.yjcy.service

import com.example.yjcy.data.*
import java.util.*
import kotlin.random.Random

/**
 * 岗位发布服务
 * 处理岗位发布、应聘者生成和管理
 */
class JobPostingService {
    
    companion object {
        // 单例实例
        private var instance: JobPostingService? = null
        
        fun getInstance(): JobPostingService {
            if (instance == null) {
                instance = JobPostingService()
            }
            return instance!!
        }
        
        // 职位到技能类型的映射
        private val POSITION_TO_SKILL_MAP = mapOf(
            "程序员" to "开发",
            "策划师" to "设计",
            "美术师" to "美工",
            "音效师" to "音乐",
            "客服" to "服务"
        )
        
        // 可用职位列表
        val AVAILABLE_POSITIONS = listOf("程序员", "策划师", "美术师", "音效师", "客服")
    }
    
    // 存储所有岗位发布
    private val jobPostings = mutableMapOf<String, JobPosting>()
    
    // 人才市场服务实例（用于生成候选人）
    private val talentMarketService = TalentMarketService()
    
    /**
     * 创建新的岗位发布
     */
    fun createJobPosting(
        position: String,
        minSkillLevel: Int,
        minSalary: Int,
        maxSalary: Int
    ): JobPosting {
        val id = "job_${UUID.randomUUID()}"
        val requiredSkillType = POSITION_TO_SKILL_MAP[position] ?: "开发"
        
        val jobPosting = JobPosting(
            id = id,
            position = position,
            requiredSkillType = requiredSkillType,
            minSkillLevel = minSkillLevel,
            minSalary = minSalary,
            maxSalary = maxSalary,
            postedDate = Date()
        )
        
        jobPostings[id] = jobPosting
        return jobPosting
    }
    
    /**
     * 获取所有活跃的岗位发布
     */
    fun getActiveJobPostings(): List<JobPosting> {
        return jobPostings.values.filter { it.status == JobPostingStatus.ACTIVE }
    }
    
    /**
     * 获取所有岗位发布
     */
    fun getAllJobPostings(): List<JobPosting> {
        return jobPostings.values.toList()
    }
    
    /**
     * 获取指定岗位发布
     */
    fun getJobPosting(jobId: String): JobPosting? {
        return jobPostings[jobId]
    }
    
    /**
     * 更新岗位发布
     */
    fun updateJobPosting(jobPosting: JobPosting) {
        jobPostings[jobPosting.id] = jobPosting
    }
    
    /**
     * 删除岗位发布
     */
    fun deleteJobPosting(jobId: String): Boolean {
        return jobPostings.remove(jobId) != null
    }
    
    /**
     * 关闭岗位发布
     */
    fun closeJobPosting(jobId: String): Boolean {
        val job = jobPostings[jobId] ?: return false
        jobPostings[jobId] = job.copy(status = JobPostingStatus.CLOSED)
        return true
    }
    
    /**
     * 暂停岗位发布
     */
    fun pauseJobPosting(jobId: String): Boolean {
        val job = jobPostings[jobId] ?: return false
        jobPostings[jobId] = job.copy(status = JobPostingStatus.PAUSED)
        return true
    }
    
    /**
     * 恢复岗位发布
     */
    fun resumeJobPosting(jobId: String): Boolean {
        val job = jobPostings[jobId] ?: return false
        jobPostings[jobId] = job.copy(status = JobPostingStatus.ACTIVE)
        return true
    }
    
    /**
     * 为活跃岗位生成新的应聘者
     * 每个岗位根据时间推进和岗位吸引力生成0-3个应聘者
     * @param existingEmployeeNames 现有员工名字集合，用于避免重复
     */
    fun generateApplicantsForActiveJobs(daysElapsed: Int = 1, existingEmployeeNames: Set<String> = emptySet()) {
        val activeJobs = getActiveJobPostings()
        
        // 收集所有已存在的名字（包括现有员工和所有岗位的应聘者）
        val allUsedNames = mutableSetOf<String>()
        allUsedNames.addAll(existingEmployeeNames)
        activeJobs.forEach { job ->
            allUsedNames.addAll(job.applicants.map { it.candidate.name })
        }
        
        activeJobs.forEach { job ->
            // 计算该岗位的吸引力（基于薪资范围）
            val attractiveness = calculateJobAttractiveness(job)
            
            // 根据吸引力和经过的天数计算应聘者数量
            val applicantCount = calculateApplicantCount(attractiveness, daysElapsed)
            
            if (applicantCount > 0) {
                // 生成符合岗位要求的应聘者
                val newApplicants = generateApplicantsForJob(job, applicantCount, allUsedNames)
                
                // 更新岗位的应聘者列表
                val updatedJob = job.copy(
                    applicants = job.applicants + newApplicants
                )
                updateJobPosting(updatedJob)
            }
        }
    }
    
    /**
     * 计算岗位吸引力（0.0-1.0）
     * 基于薪资与技能等级最低标准的关系
     * 
     * 规则（已调整为精确梯度）：
     * - 刚好100%（1.0万）：0.15（极少应聘者）
     * - 高于5-10%（1.05-1.1万）：0.3-0.4（少量应聘者）
     * - 高于15%（1.15万）：0.5（一般数量）
     * - 高于25%（1.25万）：0.7（较多应聘者）
     * - 高于50%+（1.5万+）：0.9-1.0（大量应聘者）
     */
    private fun calculateJobAttractiveness(job: JobPosting): Float {
        // 计算该技能等级的最低薪资标准
        val minSalaryRequired = job.minSkillLevel * 10000
        
        // 使用岗位薪资（现在minSalary和maxSalary相同）
        val salary = job.minSalary
        
        // 计算薪资与最低标准的比率
        val salaryRatio = salary.toFloat() / minSalaryRequired.toFloat()
        
        // 基于薪资比率计算吸引力（精确梯度设计）
        val attractiveness = when {
            // 远高于标准（150%+）：0.9-1.0（大量应聘者）
            salaryRatio >= 1.5f -> {
                // 1.5 -> 0.9, 2.0 -> 1.0, 2.5+ -> 1.0
                val bonus = (salaryRatio - 1.5f) * 0.2f
                (0.9f + bonus).coerceAtMost(1.0f)
            }
            // 高于标准25-50%（125-150%）：0.7-0.85（较多应聘者）
            salaryRatio >= 1.25f -> {
                // 线性插值：1.25 -> 0.7, 1.5 -> 0.85
                val progress = (salaryRatio - 1.25f) / 0.25f
                0.7f + progress * 0.15f
            }
            // 高于标准15-25%（115-125%）：0.5-0.7（一般数量）
            salaryRatio >= 1.15f -> {
                // 线性插值：1.15 -> 0.5, 1.25 -> 0.7
                val progress = (salaryRatio - 1.15f) / 0.1f
                0.5f + progress * 0.2f
            }
            // 高于标准5-15%（105-115%）：0.3-0.5（少量应聘者）
            salaryRatio >= 1.05f -> {
                // 线性插值：1.05 -> 0.3, 1.15 -> 0.5
                val progress = (salaryRatio - 1.05f) / 0.1f
                0.3f + progress * 0.2f
            }
            // 刚好达标准或略高（100-105%）：0.15-0.3（极少应聘者）
            salaryRatio >= 1.0f -> {
                // 线性插值：1.0 -> 0.15, 1.05 -> 0.3
                val progress = (salaryRatio - 1.0f) / 0.05f
                0.15f + progress * 0.15f
            }
            // 理论上不应该出现（UI已限制），但保险起见
            else -> 0.05f
        }
        
        return attractiveness.coerceIn(0.05f, 1.0f)
    }
    
    /**
     * 计算应聘者数量
     */
    private fun calculateApplicantCount(attractiveness: Float, daysElapsed: Int): Int {
        // 基础概率
        val baseProbability = attractiveness * daysElapsed
        
        // 随机生成0-3个应聘者
        return when {
            Random.nextFloat() < baseProbability -> Random.nextInt(1, 4)
            else -> 0
        }
    }
    
    /**
     * 为指定岗位生成应聘者
     * @param existingNames 已使用的名字集合（可变集合，会自动添加新生成的名字）
     */
    private fun generateApplicantsForJob(job: JobPosting, count: Int, existingNames: MutableSet<String>): List<JobApplicant> {
        val applicants = mutableListOf<JobApplicant>()
        
        repeat(count) {
            // 生成符合岗位要求的候选人
            val candidate = generateCandidateForJob(job, existingNames)
            
            val applicant = JobApplicant(
                id = "applicant_${UUID.randomUUID()}",
                candidate = candidate,
                applicationDate = Date()
            )
            
            applicants.add(applicant)
        }
        
        return applicants
    }
    
    /**
     * 为指定岗位生成符合要求的候选人
     * @param existingNames 已使用的名字集合，用于避免重复
     */
    private fun generateCandidateForJob(job: JobPosting, existingNames: Set<String>): TalentCandidate {
        // 使用TalentMarketService生成候选人，但调整参数以符合岗位要求
        val candidate = talentMarketService.generateCandidateForPosition(
            position = job.position,
            minSkillLevel = job.minSkillLevel,
            salaryRange = job.minSalary..job.maxSalary,
            existingEmployeeNames = existingNames
        )
        
        return candidate
    }
    
    /**
     * 添加应聘者到岗位
     */
    fun addApplicantToJob(jobId: String, applicant: JobApplicant): Boolean {
        val job = jobPostings[jobId] ?: return false
        val updatedJob = job.copy(applicants = job.applicants + applicant)
        jobPostings[jobId] = updatedJob
        return true
    }
    
    /**
     * 更新应聘者状态
     */
    fun updateApplicantStatus(
        jobId: String,
        applicantId: String,
        newStatus: ApplicantStatus
    ): Boolean {
        val job = jobPostings[jobId] ?: return false
        
        val updatedApplicants = job.applicants.map { applicant ->
            if (applicant.id == applicantId) {
                applicant.copy(status = newStatus)
            } else {
                applicant
            }
        }
        
        jobPostings[jobId] = job.copy(applicants = updatedApplicants)
        return true
    }
    
    /**
     * 玩家面试
     * 玩家需要做出选择，直接决定是否录用
     */
    fun conductPlayerInterview(
        jobId: String,
        applicantId: String,
        decision: Boolean,
        notes: String = ""
    ): InterviewResult? {
        val job = jobPostings[jobId] ?: return null
        // 检查应聘者是否存在
        if (job.applicants.none { it.id == applicantId }) return null
        
        // 玩家直接决定，评分基于候选人技能
        val score = if (decision) {
            Random.nextInt(70, 100)
        } else {
            Random.nextInt(30, 70)
        }
        
        val result = InterviewResult(
            applicantId = applicantId,
            interviewType = InterviewType.PLAYER,
            score = score,
            passed = decision,
            notes = notes
        )
        
        // 更新应聘者状态
        val updatedApplicants = job.applicants.map { app ->
            if (app.id == applicantId) {
                app.copy(
                    status = if (decision) ApplicantStatus.ACCEPTED else ApplicantStatus.REJECTED,
                    interviewScore = score,
                    interviewNotes = notes
                )
            } else {
                app
            }
        }
        
        jobPostings[jobId] = job.copy(applicants = updatedApplicants)
        
        return result
    }
    
    /**
     * 人事部门面试
     * 自动评估候选人，基于技能、经验和岗位匹配度
     */
    fun conductHRInterview(
        jobId: String,
        applicantId: String
    ): InterviewResult? {
        val job = jobPostings[jobId] ?: return null
        val applicant = job.applicants.find { it.id == applicantId } ?: return null
        val candidate = applicant.candidate
        
        // 计算综合评分
        var score = 0
        
        // 1. 技能匹配度（40分）
        val requiredSkillLevel = when (job.requiredSkillType) {
            "开发" -> candidate.skillDevelopment
            "设计" -> candidate.skillDesign
            "美工" -> candidate.skillArt
            "音乐" -> candidate.skillMusic
            "服务" -> candidate.skillService
            else -> 0
        }
        val skillScore = (requiredSkillLevel / 5.0f * 40).toInt()
        score += skillScore
        
        // 2. 经验加分（30分）
        val experienceScore = (candidate.experience.coerceAtMost(30) / 30.0f * 30).toInt()
        score += experienceScore
        
        // 3. 薪资合理性（20分）
        val avgSalary = (job.minSalary + job.maxSalary) / 2
        val salaryDiff = Math.abs(candidate.expectedSalary - avgSalary).toFloat()
        val salaryScore = ((1.0f - salaryDiff / avgSalary.coerceAtLeast(1)) * 20).toInt().coerceAtLeast(0)
        score += salaryScore
        
        // 4. 随机因素（10分）- 模拟面试表现
        score += Random.nextInt(0, 11)
        
        // 确保分数在0-100范围内
        score = score.coerceIn(0, 100)
        
        // 判断是否通过（60分及格）
        val passed = score >= 60
        
        val notes = when {
            score >= 80 -> "优秀候选人，强烈推荐录用"
            score >= 70 -> "合格候选人，建议录用"
            score >= 60 -> "基本符合要求，可以考虑"
            else -> "未达到录用标准"
        }
        
        val result = InterviewResult(
            applicantId = applicantId,
            interviewType = InterviewType.HR,
            score = score,
            passed = passed,
            notes = notes
        )
        
        // 更新应聘者状态
        val updatedApplicants = job.applicants.map { app ->
            if (app.id == applicantId) {
                app.copy(
                    status = if (passed) ApplicantStatus.ACCEPTED else ApplicantStatus.REJECTED,
                    interviewScore = score,
                    interviewNotes = notes
                )
            } else {
                app
            }
        }
        
        jobPostings[jobId] = job.copy(applicants = updatedApplicants)
        
        return result
    }
    
    /**
     * 雇佣通过面试的应聘者
     */
    fun hireApplicant(jobId: String, applicantId: String): TalentCandidate? {
        val job = jobPostings[jobId] ?: return null
        val applicant = job.applicants.find { 
            it.id == applicantId && it.status == ApplicantStatus.ACCEPTED 
        } ?: return null
        
        // 更新应聘者状态为已雇佣
        val updatedApplicants = job.applicants.map { app ->
            if (app.id == applicantId) {
                app.copy(status = ApplicantStatus.HIRED)
            } else {
                app
            }
        }
        
        jobPostings[jobId] = job.copy(applicants = updatedApplicants)
        
        return applicant.candidate
    }
    
    /**
     * 获取所有待处理的应聘者数量
     * 只统计活跃岗位（ACTIVE状态）的待处理应聘者
     */
    fun getTotalPendingApplicants(): Int {
        return jobPostings.values
            .filter { it.status == JobPostingStatus.ACTIVE }
            .sumOf { it.getPendingApplicantsCount() }
    }
    
    /**
     * 清除所有数据（用于测试或加载新存档前）
     */
    fun clearAllData() {
        jobPostings.clear()
    }
    
    /**
     * 从存档加载招聘岗位数据
     * 在加载存档时调用，恢复招聘岗位状态
     */
    fun loadFromSave(jobPostingsList: List<JobPosting>) {
        jobPostings.clear()
        jobPostingsList.forEach { posting ->
            jobPostings[posting.id] = posting
        }
    }
    
    /**
     * 获取所有招聘岗位数据（用于保存存档）
     */
    fun getAllJobPostingsForSave(): List<JobPosting> {
        return jobPostings.values.toList()
    }
}

