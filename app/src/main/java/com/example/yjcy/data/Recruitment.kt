package com.example.yjcy.data

import androidx.room.*
import java.util.UUID

/**
 * 招聘任务实体类
 */
@Entity(tableName = "recruitments")
data class Recruitment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** 招聘职位 */
    val position: RecruitmentPosition,
    
    /** 期望技能等级 (1-5) */
    val skillLevel: Int,
    
    /** 月薪预算 */
    val salary: Int,
    
    /** 招聘天数 (30/60/90) */
    val duration: Int,
    
    /** 开始时间 (时间戳) */
    val startTime: Long,
    
    /** 结束时间 (时间戳) */
    val endTime: Long,
    
    /** 招聘状态 */
    val status: RecruitmentStatus = RecruitmentStatus.ACTIVE,
    
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** 更新时间 */
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 检查招聘是否已过期
     */
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > endTime
    }
    
    /**
     * 获取剩余天数
     */
    fun getRemainingDays(): Int {
        val remainingTime = endTime - System.currentTimeMillis()
        return if (remainingTime > 0) {
            (remainingTime / (24 * 60 * 60 * 1000)).toInt()
        } else {
            0
        }
    }
    
    /**
     * 获取招聘进度百分比
     */
    fun getProgress(): Float {
        val totalTime = endTime - startTime
        val elapsedTime = System.currentTimeMillis() - startTime
        return if (totalTime > 0) {
            (elapsedTime.toFloat() / totalTime).coerceIn(0f, 1f)
        } else {
            1f
        }
    }
}

/**
 * 招聘状态枚举
 */
enum class RecruitmentStatus {
    ACTIVE,     // 进行中
    COMPLETED,  // 已完成
    CANCELLED,  // 已取消
    EXPIRED     // 已过期
}

/**
 * 招聘职位枚举
 */
enum class RecruitmentPosition(val displayName: String, val skillType: String) {
    PROGRAMMER("程序员", "开发"),
    DESIGNER("策划师", "设计"),
    ARTIST("美术师", "美工"),
    SOUND_ENGINEER("音效师", "音乐"),
    TESTER("测试员", "服务")
}

/**
 * 招聘周期枚举
 */
enum class RecruitmentDuration(val days: Int, val displayName: String) {
    SHORT(30, "30天"),
    MEDIUM(60, "60天"),
    LONG(90, "90天")
}