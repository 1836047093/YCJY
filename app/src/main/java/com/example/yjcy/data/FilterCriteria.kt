package com.example.yjcy.data

/**
 * 人才市场筛选条件数据类
 * 用于存储用户设置的筛选条件
 */
data class FilterCriteria(
    val selectedPosition: String? = null,
    val minSkillLevel: Int = SkillConstants.MIN_SKILL_LEVEL,
    val maxSkillLevel: Int = SkillConstants.MAX_SKILL_LEVEL,
    val minSalary: Int = 3000,
    val maxSalary: Int = 50000
) {
    companion object {
        /**
         * 默认筛选条件
         */
        fun default() = FilterCriteria()
        
        /**
         * 获取所有可选职位
         */
        fun getAvailablePositions(): List<String> {
            return listOf("程序员", "策划师", "美术师", "音效师", "客服")
        }
        
        /**
         * 获取技能等级选项
         */
        fun getSkillLevelOptions(): List<Pair<String, IntRange>> {
            return listOf(
                "全部" to (SkillConstants.MIN_SKILL_LEVEL..SkillConstants.MAX_SKILL_LEVEL),
                "初级 (1-2级)" to (1..2),
                "中级 (3级)" to (3..3),
                "高级 (4级)" to (4..4),
                "专家级 (5级)" to (5..5)
            )
        }
        
        /**
         * 薪资范围常量
         */
        const val MIN_SALARY_RANGE = 3000
        const val MAX_SALARY_RANGE = 50000
        const val SALARY_STEP = 1000
    }
    
    /**
     * 验证筛选条件是否有效
     */
    fun isValid(): Boolean {
        return minSkillLevel <= maxSkillLevel &&
                minSalary <= maxSalary &&
                minSkillLevel in SkillConstants.SKILL_LEVEL_RANGE &&
                maxSkillLevel in SkillConstants.SKILL_LEVEL_RANGE &&
                minSalary >= MIN_SALARY_RANGE &&
                maxSalary <= MAX_SALARY_RANGE
    }
    
    /**
     * 获取技能等级范围描述
     */
    fun getSkillLevelDescription(): String {
        return when {
            minSkillLevel == SkillConstants.MIN_SKILL_LEVEL && maxSkillLevel == SkillConstants.MAX_SKILL_LEVEL -> "全部等级"
            minSkillLevel == maxSkillLevel -> {
                when (minSkillLevel) {
                    1, 2 -> "初级 (${minSkillLevel}级)"
                    3 -> "中级 (3级)"
                    4 -> "高级 (4级)"
                    5 -> "专家级 (5级)"
                    else -> "${minSkillLevel}级"
                }
            }
            else -> "${minSkillLevel}-${maxSkillLevel}级"
        }
    }
    
    /**
     * 获取薪资范围描述
     */
    fun getSalaryRangeDescription(): String {
        return "¥${minSalary} - ¥${maxSalary}"
    }
    
    /**
     * 获取职位描述
     */
    fun getPositionDescription(): String {
        return selectedPosition ?: "全部职位"
    }
    
    /**
     * 重置为默认条件
     */
    fun reset(): FilterCriteria {
        return default()
    }
    
    /**
     * 检查是否为默认条件
     */
    fun isDefault(): Boolean {
        return this == default()
    }
}