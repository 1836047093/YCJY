package com.example.yjcy.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 招聘系统Repository
 * 负责招聘相关的数据访问和业务逻辑
 */
@Singleton
class RecruitmentRepository @Inject constructor(
    private val recruitmentDao: RecruitmentDao,
    private val candidateDao: CandidateDao,
    private val recruitmentStatsDao: RecruitmentStatsDao
) {
    
    // ==================== 招聘任务相关 ====================
    
    /**
     * 创建新的招聘任务
     */
    suspend fun createRecruitment(
        position: RecruitmentPosition,
        skillLevel: Int,
        salary: Int,
        duration: RecruitmentDuration
    ): Result<String> {
        return try {
            val currentTime = System.currentTimeMillis()
            val endTime = currentTime + (duration.days * 24 * 60 * 60 * 1000L)
            val recruitment = Recruitment(
                position = position,
                skillLevel = skillLevel,
                salary = salary,
                duration = duration.days,
                startTime = currentTime,
                endTime = endTime
            )
            recruitmentDao.insertRecruitment(recruitment)
            Result.success(recruitment.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有招聘任务
     */
    fun getAllRecruitments(): Flow<List<Recruitment>> {
        return recruitmentDao.getAllRecruitments()
    }
    
    /**
     * 获取活跃的招聘任务
     */
    fun getActiveRecruitments(): Flow<List<Recruitment>> {
        return recruitmentDao.getActiveRecruitments()
    }
    
    /**
     * 根据ID获取招聘任务
     */
    suspend fun getRecruitmentById(id: String): Recruitment? {
        return recruitmentDao.getRecruitmentById(id)
    }
    
    /**
     * 更新招聘任务状态
     */
    suspend fun updateRecruitmentStatus(id: String, status: RecruitmentStatus): Result<Unit> {
        return try {
            recruitmentDao.updateRecruitmentStatus(id, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取消招聘任务
     */
    suspend fun cancelRecruitment(id: String): Result<Unit> {
        return try {
            recruitmentDao.updateRecruitmentStatus(id, RecruitmentStatus.CANCELLED)
            // 同时取消该招聘任务下的所有待处理候选人
            val candidates = candidateDao.getPendingCandidatesByRecruitmentId(id)
            candidates.collect { candidateList ->
                candidateList.forEach { candidate ->
                    candidateDao.updateCandidateStatus(candidate.id, CandidateStatus.REJECTED)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查并更新过期的招聘任务
     */
    suspend fun updateExpiredRecruitments(): Result<Int> {
        return try {
            val expiredRecruitments = recruitmentDao.getExpiredRecruitments()
            expiredRecruitments.forEach { recruitment ->
                recruitmentDao.updateRecruitmentStatus(recruitment.id, RecruitmentStatus.EXPIRED)
            }
            Result.success(expiredRecruitments.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== 候选人相关 ====================
    
    /**
     * 为招聘任务生成候选人
     */
    suspend fun generateCandidatesForRecruitment(recruitmentId: String): Result<List<Candidate>> {
        return try {
            val recruitment = recruitmentDao.getRecruitmentById(recruitmentId)
                ?: return Result.failure(Exception("招聘任务不存在"))
            
            if (recruitment.status != RecruitmentStatus.ACTIVE) {
                return Result.failure(Exception("招聘任务未激活"))
            }
            
            // 根据招聘算法生成候选人
            val candidates = RecruitmentAlgorithm.generateCandidates(recruitment)
            
            if (candidates.isNotEmpty()) {
                candidateDao.insertCandidates(candidates)
            }
            
            Result.success(candidates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取指定招聘任务的候选人
     */
    fun getCandidatesByRecruitmentId(recruitmentId: String): Flow<List<Candidate>> {
        return candidateDao.getCandidatesByRecruitmentId(recruitmentId)
    }
    
    /**
     * 获取指定招聘任务的待处理候选人
     */
    fun getPendingCandidatesByRecruitmentId(recruitmentId: String): Flow<List<Candidate>> {
        return candidateDao.getPendingCandidatesByRecruitmentId(recruitmentId)
    }
    
    /**
     * 获取所有待处理的候选人
     */
    fun getAllPendingCandidates(): Flow<List<Candidate>> {
        return candidateDao.getAllPendingCandidates()
    }
    
    /**
     * 根据ID获取候选人
     */
    suspend fun getCandidateById(id: String): Candidate? {
        return candidateDao.getCandidateById(id)
    }
    
    /**
     * 聘用候选人
     */
    suspend fun hireCandidate(candidateId: String): Result<Employee> {
        return try {
            val candidate = candidateDao.getCandidateById(candidateId)
                ?: return Result.failure(Exception("候选人不存在"))
            
            if (candidate.status != CandidateStatus.PENDING) {
                return Result.failure(Exception("候选人状态不正确"))
            }
            
            // 更新候选人状态为已聘用
            candidateDao.updateCandidateStatus(candidateId, CandidateStatus.HIRED)
            
            // 转换为员工对象
            val employee = candidate.toEmployee()
            
            // 这里应该调用员工Repository来添加新员工
            // employeeRepository.addEmployee(employee)
            
            Result.success(employee)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 拒绝候选人
     */
    suspend fun rejectCandidate(candidateId: String): Result<Unit> {
        return try {
            val candidate = candidateDao.getCandidateById(candidateId)
                ?: return Result.failure(Exception("候选人不存在"))
            
            if (candidate.status != CandidateStatus.PENDING) {
                return Result.failure(Exception("候选人状态不正确"))
            }
            
            candidateDao.updateCandidateStatus(candidateId, CandidateStatus.REJECTED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== 统计相关 ====================
    
    /**
     * 获取招聘统计数据
     */
    suspend fun getRecruitmentStats(): Result<RecruitmentStats> {
        return try {
            val stats = recruitmentStatsDao.getRecruitmentStats()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取招聘任务及其候选人
     */
    suspend fun getRecruitmentWithCandidates(recruitmentId: String): Result<RecruitmentWithCandidates?> {
        return try {
            val result = recruitmentStatsDao.getRecruitmentWithCandidates(recruitmentId)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有招聘任务及其候选人
     */
    fun getAllRecruitmentsWithCandidates(): Flow<List<RecruitmentWithCandidates>> {
        return recruitmentStatsDao.getAllRecruitmentsWithCandidates()
    }
    
    /**
     * 获取招聘概览数据
     */
    fun getRecruitmentOverview(): Flow<RecruitmentOverview> {
        return combine(
            getActiveRecruitments(),
            getAllPendingCandidates()
        ) { activeRecruitments, pendingCandidates ->
            RecruitmentOverview(
                activeRecruitmentsCount = activeRecruitments.size,
                pendingCandidatesCount = pendingCandidates.size,
                activeRecruitments = activeRecruitments.take(5), // 最近5个活跃招聘
                recentCandidates = pendingCandidates.take(10) // 最近10个候选人
            )
        }
    }
    
    // ==================== 数据清理 ====================
    
    /**
     * 清理过期数据
     */
    suspend fun cleanupExpiredData(): Result<CleanupResult> {
        return try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
            
            // 清理已完成的招聘任务
            recruitmentDao.deleteCompletedRecruitmentsBefore(thirtyDaysAgo)
            
            // 清理已拒绝的候选人
            candidateDao.deleteRejectedCandidatesBefore(thirtyDaysAgo)
            
            val result = CleanupResult(
                success = true,
                cleanupTime = System.currentTimeMillis(),
                message = "数据清理完成"
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 招聘概览数据
 */
data class RecruitmentOverview(
    val activeRecruitmentsCount: Int,
    val pendingCandidatesCount: Int,
    val activeRecruitments: List<Recruitment>,
    val recentCandidates: List<Candidate>
)

/**
 * 数据清理结果
 */
data class CleanupResult(
    val success: Boolean,
    val cleanupTime: Long,
    val message: String,
    val error: String? = null
)