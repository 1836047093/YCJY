package com.example.yjcy.data

/**
 * 技能等级相关常量定义
 * 统一管理所有技能等级相关的数值，确保代码一致性
 */
object SkillConstants {
    
    /** 技能等级最小值 */
    const val MIN_SKILL_LEVEL = 1
    
    /** 技能等级最大值 */
    const val MAX_SKILL_LEVEL = 5
    
    /** 创始人默认技能等级 */
    const val FOUNDER_SKILL_LEVEL = MAX_SKILL_LEVEL
    
    /** 技能等级范围 */
    val SKILL_LEVEL_RANGE = MIN_SKILL_LEVEL..MAX_SKILL_LEVEL
    
    /**
     * 技能等级分类枚举
     * 用于人才市场等功能的技能等级筛选
     */
    enum class SkillCategory(val displayName: String, val levelRange: IntRange) {
        JUNIOR("初级", 1..2),
        INTERMEDIATE("中级", 3..3),
        SENIOR("高级", 4..4),
        EXPERT("专家级", 5..5)
    }
    
    /**
     * 根据技能等级获取对应的分类
     */
    fun getSkillCategory(level: Int): SkillCategory? {
        return SkillCategory.values().find { level in it.levelRange }
    }
    
    /**
     * 获取技能等级的显示名称
     */
    fun getSkillLevelDisplayName(level: Int): String {
        return when (level) {
            in 1..2 -> "初级 ($level 级)"
            3 -> "中级 (3 级)"
            4 -> "高级 (4 级)"
            5 -> "专家级 (5 级)"
            else -> "未知 ($level 级)"
        }
    }
    
    /**
     * 验证技能等级是否有效
     */
    fun isValidSkillLevel(level: Int): Boolean {
        return level in SKILL_LEVEL_RANGE
    }
    
    /**
     * 限制技能等级在有效范围内
     */
    fun clampSkillLevel(level: Int): Int {
        return level.coerceIn(MIN_SKILL_LEVEL, MAX_SKILL_LEVEL)
    }
    
    /**
     * 获取技能分类的显示名称
     */
    fun getSkillCategoryDisplayName(category: SkillCategory?): String {
        return category?.displayName ?: "未知"
    }
}