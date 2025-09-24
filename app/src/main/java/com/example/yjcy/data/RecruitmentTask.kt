package com.example.yjcy.data

import androidx.compose.runtime.Immutable

/**
 * 招聘任务数据模型
 * 用于跟踪每个招聘任务的状态和进度
 */
@Immutable
data class RecruitmentTask(
    val id: Int = 0,
    val configId: Int, // 关联的招聘配置ID
    val status: RecruitmentTaskStatus = RecruitmentTaskStatus.ACTIVE,
    val currentCandidates: List<TaskCandidate> = emptyList(), // 当前待确认的候选人
    val hiredCandidates: List<TaskCandidate> = emptyList(), // 已雇佣的候选人
    val rejectedCandidates: List<TaskCandidate> = emptyList(), // 已拒绝的候选人
    val targetCount: Int, // 目标招聘人数
    val hiredCount: Int = 0, // 已雇佣人数
    val lastSearchTime: Long = 0L, // 上次搜索候选人的时间
    val nextSearchTime: Long = 0L, // 下次搜索候选人的时间
    val searchInterval: Long = 24 * 60 * 60 * 1000L, // 搜索间隔（毫秒）
    val totalBudgetUsed: Int = 0, // 已使用的总预算
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null // 完成时间
) {
    /**
     * 检查任务是否已完成
     */
    fun isCompleted(): Boolean {
        return hiredCount >= targetCount || status == RecruitmentTaskStatus.COMPLETED
    }
    
    /**
     * 检查是否需要搜索新候选人
     */
    fun needsNewCandidates(): Boolean {
        return status == RecruitmentTaskStatus.ACTIVE &&
                !isCompleted() &&
                currentCandidates.isEmpty() &&
                System.currentTimeMillis() >= nextSearchTime
    }
    
    /**
     * 获取剩余需要招聘的人数
     */
    fun getRemainingCount(): Int {
        return maxOf(0, targetCount - hiredCount)
    }
    
    /**
     * 获取招聘进度百分比
     */
    fun getProgressPercentage(): Float {
        return if (targetCount > 0) {
            (hiredCount.toFloat() / targetCount.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    /**
     * 获取平均雇佣成本
     */
    fun getAverageHiringCost(): Int {
        return if (hiredCount > 0) {
            totalBudgetUsed / hiredCount
        } else {
            0
        }
    }
    
    /**
     * 获取待确认候选人数量
     */
    fun getPendingCandidatesCount(): Int {
        return currentCandidates.count { it.status == CandidateTaskStatus.PENDING }
    }
    
    /**
     * 获取自动批准的候选人数量
     */
    fun getAutoApprovedCount(): Int {
        return hiredCandidates.count { it.isAutoApproved }
    }
    
    /**
     * 添加新候选人
     */
    fun addCandidates(candidates: List<Candidate>, matchScores: List<Float>): RecruitmentTask {
        val taskCandidates = candidates.mapIndexed { index, candidate ->
            TaskCandidate(
                candidate = candidate,
                matchScore = matchScores.getOrElse(index) { 0f },
                status = CandidateTaskStatus.PENDING,
                addedAt = System.currentTimeMillis()
            )
        }
        
        return copy(
            currentCandidates = currentCandidates + taskCandidates,
            lastSearchTime = System.currentTimeMillis(),
            nextSearchTime = System.currentTimeMillis() + searchInterval,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 雇佣候选人
     */
    fun hireCandidate(candidateId: Int, isAutoApproved: Boolean = false): RecruitmentTask {
        val candidateToHire = currentCandidates.find { it.candidate.id == candidateId }
            ?: return this
        
        val updatedCandidate = candidateToHire.copy(
            status = CandidateTaskStatus.HIRED,
            isAutoApproved = isAutoApproved,
            processedAt = System.currentTimeMillis()
        )
        
        val newHiredCount = hiredCount + 1
        val newStatus = if (newHiredCount >= targetCount) {
            RecruitmentTaskStatus.COMPLETED
        } else {
            status
        }
        
        return copy(
            currentCandidates = currentCandidates.filter { it.candidate.id != candidateId },
            hiredCandidates = hiredCandidates + updatedCandidate,
            hiredCount = newHiredCount,
            totalBudgetUsed = totalBudgetUsed + candidateToHire.candidate.expectedSalary,
            status = newStatus,
            completedAt = if (newStatus == RecruitmentTaskStatus.COMPLETED) System.currentTimeMillis() else completedAt,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 拒绝候选人
     */
    fun rejectCandidate(candidateId: Int, reason: String = ""): RecruitmentTask {
        val candidateToReject = currentCandidates.find { it.candidate.id == candidateId }
            ?: return this
        
        val updatedCandidate = candidateToReject.copy(
            status = CandidateTaskStatus.REJECTED,
            rejectionReason = reason,
            processedAt = System.currentTimeMillis()
        )
        
        return copy(
            currentCandidates = currentCandidates.filter { it.candidate.id != candidateId },
            rejectedCandidates = rejectedCandidates + updatedCandidate,
            updatedAt = System.currentTimeMillis()
        )
    }
}

/**
 * 招聘任务状态枚举
 */
enum class RecruitmentTaskStatus {
    ACTIVE,     // 活跃中
    PAUSED,     // 已暂停
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}

/**
 * 任务候选人数据模型
 */
@Immutable
data class TaskCandidate(
    val candidate: Candidate,
    val matchScore: Float, // 匹配度分数 (0.0 - 1.0)
    val status: CandidateTaskStatus = CandidateTaskStatus.PENDING,
    val isAutoApproved: Boolean = false, // 是否自动批准
    val rejectionReason: String = "", // 拒绝原因
    val addedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null // 处理时间（雇佣或拒绝）
) {
    /**
     * 获取匹配度百分比
     */
    fun getMatchPercentage(): Int {
        return (matchScore * 100).toInt()
    }
    
    /**
     * 获取匹配度等级描述
     */
    fun getMatchGrade(): String {
        return when {
            matchScore >= 0.9f -> "优秀"
            matchScore >= 0.8f -> "良好"
            matchScore >= 0.7f -> "一般"
            matchScore >= 0.6f -> "较差"
            else -> "不匹配"
        }
    }
    
    /**
     * 检查是否推荐自动批准
     */
    fun isRecommendedForAutoApproval(): Boolean {
        return matchScore >= 0.85f
    }
    
    /**
     * 获取等待时间（小时）
     */
    fun getWaitingHours(): Long {
        return if (processedAt != null) {
            (processedAt - addedAt) / (1000 * 60 * 60)
        } else {
            (System.currentTimeMillis() - addedAt) / (1000 * 60 * 60)
        }
    }
}

/**
 * 候选人在任务中的状态
 */
enum class CandidateTaskStatus {
    PENDING,   // 待确认
    HIRED,     // 已雇佣
    REJECTED   // 已拒绝
}

/**
 * 招聘任务统计信息
 */
data class RecruitmentTaskStats(
    val totalTasks: Int = 0,
    val activeTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalHired: Int = 0,
    val totalBudgetUsed: Int = 0,
    val averageMatchScore: Float = 0f,
    val averageHiringTime: Long = 0L, // 平均招聘时间（毫秒）
    val successRate: Float = 0f // 成功率
) {
    /**
     * 获取完成率
     */
    fun getCompletionRate(): Float {
        return if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    /**
     * 获取平均雇佣成本
     */
    fun getAverageHiringCost(): Int {
        return if (totalHired > 0) {
            totalBudgetUsed / totalHired
        } else {
            0
        }
    }
}