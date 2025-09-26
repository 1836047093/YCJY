package com.example.yjcy.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.yjcy.data.*

/**
 * 招聘系统数据库
 * 包含招聘任务和候选人相关的所有数据表
 */
@Database(
    entities = [
        Recruitment::class,
        Candidate::class
    ],
    version = 2,  // 增加版本号以支持新的招聘功能
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RecruitmentDatabase : RoomDatabase() {
    
    // DAO接口
    abstract fun recruitmentDao(): RecruitmentDao
    abstract fun candidateDao(): CandidateDao
    abstract fun recruitmentStatsDao(): RecruitmentStatsDao
    
    companion object {
        const val DATABASE_NAME = "recruitment_database"
        
        /**
         * 数据库迁移：从版本1到版本2
         * 添加招聘相关的表
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建招聘任务表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS recruitments (
                        id TEXT PRIMARY KEY NOT NULL,
                        position TEXT NOT NULL,
                        skillLevel INTEGER NOT NULL,
                        salary INTEGER NOT NULL,
                        duration TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // 创建候选人表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS candidates (
                        id TEXT PRIMARY KEY NOT NULL,
                        recruitmentId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        position TEXT NOT NULL,
                        skillLevel INTEGER NOT NULL,
                        expectedSalary INTEGER NOT NULL,
                        programmingSkill INTEGER NOT NULL,
                        designSkill INTEGER NOT NULL,
                        planningSkill INTEGER NOT NULL,
                        marketingSkill INTEGER NOT NULL,
                        researchSkill INTEGER NOT NULL,
                        experienceYears INTEGER NOT NULL,
                        specialAbilities TEXT NOT NULL,
                        generatedTime INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(recruitmentId) REFERENCES recruitments(id) ON DELETE CASCADE
                    )
                """)
                
                // 创建索引以提高查询性能
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recruitments_status ON recruitments(status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recruitments_endTime ON recruitments(endTime)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_candidates_recruitmentId ON candidates(recruitmentId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_candidates_status ON candidates(status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_candidates_skillLevel ON candidates(skillLevel)")
            }
        }
        
        /**
         * 预填充数据库的回调
         */
        val DATABASE_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 数据库创建时的初始化操作
                // 可以在这里插入一些初始数据
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // 数据库打开时的操作
                // 可以在这里进行数据清理或更新
            }
        }
    }
}

/**
 * Room类型转换器
 * 用于将复杂类型转换为Room可以存储的基本类型
 */
class Converters {
    
    @TypeConverter
    fun fromRecruitmentStatus(status: RecruitmentStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toRecruitmentStatus(status: String): RecruitmentStatus {
        return RecruitmentStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromCandidateStatus(status: CandidateStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toCandidateStatus(status: String): CandidateStatus {
        return CandidateStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromRecruitmentPosition(position: RecruitmentPosition): String {
        return position.name
    }
    
    @TypeConverter
    fun toRecruitmentPosition(position: String): RecruitmentPosition {
        return RecruitmentPosition.valueOf(position)
    }
    
    @TypeConverter
    fun fromRecruitmentDuration(duration: RecruitmentDuration): String {
        return duration.name
    }
    
    @TypeConverter
    fun toRecruitmentDuration(duration: String): RecruitmentDuration {
        return RecruitmentDuration.valueOf(duration)
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}

/**
 * 数据库构建器扩展函数
 * 简化数据库实例的创建
 */
fun RoomDatabase.Builder<RecruitmentDatabase>.addMigrations(): RoomDatabase.Builder<RecruitmentDatabase> {
    return this.addMigrations(RecruitmentDatabase.MIGRATION_1_2)
}

fun RoomDatabase.Builder<RecruitmentDatabase>.addCallback(): RoomDatabase.Builder<RecruitmentDatabase> {
    return this.addCallback(RecruitmentDatabase.DATABASE_CALLBACK)
}

/**
 * 数据库操作扩展函数
 */
suspend fun RecruitmentDatabase.cleanupOldData() {
    val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
    
    // 清理30天前的已完成招聘任务
    recruitmentDao().deleteCompletedRecruitmentsBefore(thirtyDaysAgo)
    
    // 清理30天前的已拒绝候选人
    candidateDao().deleteRejectedCandidatesBefore(thirtyDaysAgo)
}

/**
 * 数据库健康检查
 */
suspend fun RecruitmentDatabase.performHealthCheck(): DatabaseHealthStatus {
    return try {
        val stats = recruitmentStatsDao().getRecruitmentStats()
        val expiredRecruitments = recruitmentDao().getExpiredRecruitments()
        
        DatabaseHealthStatus(
            isHealthy = true,
            totalRecruitments = stats.totalRecruitments,
            totalCandidates = stats.totalCandidates,
            expiredRecruitmentsCount = expiredRecruitments.size,
            lastCheckTime = System.currentTimeMillis()
        )
    } catch (e: Exception) {
        DatabaseHealthStatus(
            isHealthy = false,
            error = e.message,
            lastCheckTime = System.currentTimeMillis()
        )
    }
}

/**
 * 数据库健康状态
 */
data class DatabaseHealthStatus(
    val isHealthy: Boolean,
    val totalRecruitments: Int = 0,
    val totalCandidates: Int = 0,
    val expiredRecruitmentsCount: Int = 0,
    val error: String? = null,
    val lastCheckTime: Long
)