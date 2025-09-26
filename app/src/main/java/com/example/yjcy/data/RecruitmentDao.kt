package com.example.yjcy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 招聘任务数据访问对象
 */
@Dao
interface RecruitmentDao {
    
    /**
     * 插入招聘任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecruitment(recruitment: Recruitment): Long
    
    /**
     * 更新招聘任务
     */
    @Update
    suspend fun updateRecruitment(recruitment: Recruitment)
    
    /**
     * 删除招聘任务
     */
    @Delete
    suspend fun deleteRecruitment(recruitment: Recruitment)
    
    /**
     * 根据ID获取招聘任务
     */
    @Query("SELECT * FROM recruitments WHERE id = :id")
    suspend fun getRecruitmentById(id: String): Recruitment?
    
    /**
     * 获取所有招聘任务
     */
    @Query("SELECT * FROM recruitments ORDER BY createdAt DESC")
    fun getAllRecruitments(): Flow<List<Recruitment>>
    
    /**
     * 获取活跃的招聘任务
     */
    @Query("SELECT * FROM recruitments WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveRecruitments(): Flow<List<Recruitment>>
    
    /**
     * 获取已过期的招聘任务
     */
    @Query("SELECT * FROM recruitments WHERE endTime < :currentTime AND status = 'ACTIVE'")
    suspend fun getExpiredRecruitments(currentTime: Long = System.currentTimeMillis()): List<Recruitment>
    
    /**
     * 更新招聘任务状态
     */
    @Query("UPDATE recruitments SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRecruitmentStatus(id: String, status: RecruitmentStatus, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 获取指定状态的招聘任务数量
     */
    @Query("SELECT COUNT(*) FROM recruitments WHERE status = :status")
    suspend fun getRecruitmentCountByStatus(status: RecruitmentStatus): Int
    
    /**
     * 删除已完成的招聘任务（清理数据）
     */
    @Query("DELETE FROM recruitments WHERE status = 'COMPLETED' AND updatedAt < :beforeTime")
    suspend fun deleteCompletedRecruitmentsBefore(beforeTime: Long)
}

/**
 * 候选人数据访问对象
 */
@Dao
interface CandidateDao {
    
    /**
     * 插入候选人
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: Candidate): Long
    
    /**
     * 批量插入候选人
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<Candidate>)
    
    /**
     * 更新候选人
     */
    @Update
    suspend fun updateCandidate(candidate: Candidate)
    
    /**
     * 删除候选人
     */
    @Delete
    suspend fun deleteCandidate(candidate: Candidate)
    
    /**
     * 根据ID获取候选人
     */
    @Query("SELECT * FROM candidates WHERE id = :id")
    suspend fun getCandidateById(id: String): Candidate?
    
    /**
     * 获取指定招聘任务的所有候选人
     */
    @Query("SELECT * FROM candidates WHERE recruitmentId = :recruitmentId ORDER BY generatedTime DESC")
    fun getCandidatesByRecruitmentId(recruitmentId: String): Flow<List<Candidate>>
    
    /**
     * 获取指定招聘任务的待处理候选人
     */
    @Query("SELECT * FROM candidates WHERE recruitmentId = :recruitmentId AND status = 'PENDING' ORDER BY skillLevel DESC, generatedTime DESC")
    fun getPendingCandidatesByRecruitmentId(recruitmentId: String): Flow<List<Candidate>>
    
    /**
     * 获取所有待处理的候选人
     */
    @Query("SELECT * FROM candidates WHERE status = 'PENDING' ORDER BY generatedTime DESC")
    fun getAllPendingCandidates(): Flow<List<Candidate>>
    
    /**
     * 获取已聘用的候选人
     */
    @Query("SELECT * FROM candidates WHERE status = 'HIRED' ORDER BY generatedTime DESC")
    fun getHiredCandidates(): Flow<List<Candidate>>
    
    /**
     * 更新候选人状态
     */
    @Query("UPDATE candidates SET status = :status WHERE id = :id")
    suspend fun updateCandidateStatus(id: String, status: CandidateStatus)
    
    /**
     * 获取指定招聘任务的候选人数量
     */
    @Query("SELECT COUNT(*) FROM candidates WHERE recruitmentId = :recruitmentId")
    suspend fun getCandidateCountByRecruitmentId(recruitmentId: String): Int
    
    /**
     * 获取指定状态的候选人数量
     */
    @Query("SELECT COUNT(*) FROM candidates WHERE status = :status")
    suspend fun getCandidateCountByStatus(status: CandidateStatus): Int
    
    /**
     * 获取指定招聘任务和状态的候选人数量
     */
    @Query("SELECT COUNT(*) FROM candidates WHERE recruitmentId = :recruitmentId AND status = :status")
    suspend fun getCandidateCountByRecruitmentIdAndStatus(recruitmentId: String, status: CandidateStatus): Int
    
    /**
     * 删除指定招聘任务的所有候选人
     */
    @Query("DELETE FROM candidates WHERE recruitmentId = :recruitmentId")
    suspend fun deleteCandidatesByRecruitmentId(recruitmentId: String)
    
    /**
     * 删除已拒绝的候选人（清理数据）
     */
    @Query("DELETE FROM candidates WHERE status = 'REJECTED' AND generatedTime < :beforeTime")
    suspend fun deleteRejectedCandidatesBefore(beforeTime: Long)
    
    /**
     * 根据技能等级筛选候选人
     */
    @Query("SELECT * FROM candidates WHERE recruitmentId = :recruitmentId AND skillLevel >= :minSkillLevel AND status = 'PENDING' ORDER BY skillLevel DESC")
    fun getCandidatesBySkillLevel(recruitmentId: String, minSkillLevel: Int): Flow<List<Candidate>>
    
    /**
     * 根据期望薪资范围筛选候选人
     */
    @Query("SELECT * FROM candidates WHERE recruitmentId = :recruitmentId AND expectedSalary BETWEEN :minSalary AND :maxSalary AND status = 'PENDING' ORDER BY skillLevel DESC")
    fun getCandidatesBySalaryRange(recruitmentId: String, minSalary: Int, maxSalary: Int): Flow<List<Candidate>>
}

/**
 * 招聘任务和候选人的关联查询
 */
data class RecruitmentWithCandidates(
    @Embedded val recruitment: Recruitment,
    @Relation(
        parentColumn = "id",
        entityColumn = "recruitmentId"
    )
    val candidates: List<Candidate>
)

/**
 * 招聘统计数据
 */
data class RecruitmentStats(
    val totalRecruitments: Int,
    val activeRecruitments: Int,
    val totalCandidates: Int,
    val pendingCandidates: Int,
    val hiredCandidates: Int,
    val rejectedCandidates: Int,
    val averageRecruitmentDays: Double = 0.0,
    val successRate: Double = 0.0,
    val monthlyHires: Int = 0
)

/**
 * 扩展DAO，提供复杂查询
 */
@Dao
interface RecruitmentStatsDao {
    
    /**
     * 获取招聘统计数据
     */
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM recruitments) as totalRecruitments,
            (SELECT COUNT(*) FROM recruitments WHERE status = 'ACTIVE') as activeRecruitments,
            (SELECT COUNT(*) FROM candidates) as totalCandidates,
            (SELECT COUNT(*) FROM candidates WHERE status = 'PENDING') as pendingCandidates,
            (SELECT COUNT(*) FROM candidates WHERE status = 'HIRED') as hiredCandidates,
            (SELECT COUNT(*) FROM candidates WHERE status = 'REJECTED') as rejectedCandidates,
            COALESCE(
                (SELECT AVG((endTime - createdAt) / (1000 * 60 * 60 * 24.0)) 
                 FROM recruitments 
                 WHERE status = 'COMPLETED'), 0.0
            ) as averageRecruitmentDays,
            COALESCE(
                CASE 
                    WHEN (SELECT COUNT(*) FROM recruitments WHERE status IN ('COMPLETED', 'CANCELLED')) > 0
                    THEN CAST((SELECT COUNT(*) FROM recruitments WHERE status = 'COMPLETED') AS REAL) / 
                         (SELECT COUNT(*) FROM recruitments WHERE status IN ('COMPLETED', 'CANCELLED'))
                    ELSE 0.0
                END, 0.0
            ) as successRate,
            (SELECT COUNT(*) FROM candidates 
             WHERE status = 'HIRED' 
             AND generatedTime >= strftime('%s', 'now', 'start of month') * 1000
            ) as monthlyHires
    """)
    suspend fun getRecruitmentStats(): RecruitmentStats
    
    /**
     * 获取招聘任务及其候选人
     */
    @Transaction
    @Query("SELECT * FROM recruitments WHERE id = :recruitmentId")
    suspend fun getRecruitmentWithCandidates(recruitmentId: String): RecruitmentWithCandidates?
    
    /**
     * 获取所有招聘任务及其候选人
     */
    @Transaction
    @Query("SELECT * FROM recruitments ORDER BY createdAt DESC")
    fun getAllRecruitmentsWithCandidates(): Flow<List<RecruitmentWithCandidates>>
}