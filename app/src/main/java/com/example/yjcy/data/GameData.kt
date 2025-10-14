package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.Platform
import com.example.yjcy.ui.BusinessModel

// 员工排序枚举
enum class EmployeeSortBy {
    NAME,        // 按姓名排序
    POSITION,    // 按职位排序
    SALARY,      // 按薪资排序
    SKILL_LEVEL, // 按技能等级排序
    HIRE_DATE    // 按入职时间排序
}

// 创始人职业枚举
enum class FounderProfession(val displayName: String, val icon: String, val specialtySkill: String) {
    PROGRAMMER("程序员", "💻", "开发"),
    DESIGNER("策划师", "📋", "设计"),
    ARTIST("美术师", "🎨", "美工"),
    SOUND_ENGINEER("音效师", "🎵", "音乐"),
    CUSTOMER_SERVICE("客服", "📞", "服务")
}

// 游戏发售状态枚举
enum class GameReleaseStatus {
    DEVELOPMENT,      // 开发中
    READY_FOR_RELEASE, // 准备发售
    PRICE_SETTING,    // 价格设置中
    RELEASED,         // 已发售
    RATED,           // 已评分
    REMOVED_FROM_MARKET // 已下架
}

// 员工数据类
data class Employee(
    val id: Int,
    val name: String,
    val position: String,
    val skillDevelopment: Int = 0,
    val skillDesign: Int = 0,
    val skillArt: Int = 0,
    val skillMusic: Int = 0,
    val skillService: Int = 0,
    val salary: Int = 0,
    val experience: Int = 0,
    val motivation: Int = 100,
    val isFounder: Boolean = false
) {
    /**
     * 获取员工的专属技能类型
     */
    fun getSpecialtySkillType(): String {
        return when (position) {
            "程序员" -> "开发"
            "策划师" -> "设计"
            "美术师" -> "美工"
            "音效师" -> "音乐"
            "客服" -> "服务"
            else -> "通用"
        }
    }
    
    /**
     * 获取员工的专属技能等级
     */
    fun getSpecialtySkillLevel(): Int {
        return when (getSpecialtySkillType()) {
            "开发" -> skillDevelopment
            "设计" -> skillDesign
            "美工" -> skillArt
            "音乐" -> skillMusic
            "服务" -> skillService
            else -> maxOf(skillDevelopment, skillDesign, skillArt, skillMusic, skillService)
        }
    }
    
    /**
     * 获取员工的主要技能值
     */
    fun getPrimarySkillValue(): Int {
        return getSpecialtySkillLevel()
    }
    
    /**
     * 获取员工的总技能点数
     */
    fun getTotalSkillPoints(): Int {
        return skillDevelopment + skillDesign + skillArt + skillMusic + skillService
    }
    
    /**
     * 检查员工是否具备指定技能
     */
    fun hasSkill(skillType: String, minLevel: Int = 1): Boolean {
        val skillValue = when (skillType) {
            "开发" -> skillDevelopment
            "设计" -> skillDesign
            "美工" -> skillArt
            "音乐" -> skillMusic
            "服务" -> skillService
            else -> 0
        }
        return skillValue >= minLevel
    }
    
    /**
     * 获取指定技能的等级
     */
    fun getSkillLevel(skillType: String): Int {
        return when (skillType) {
            "开发" -> skillDevelopment
            "设计" -> skillDesign
            "美工" -> skillArt
            "音乐" -> skillMusic
            "服务" -> skillService
            else -> 0
        }
    }
}

// 创始人数据类
data class Founder(
    val name: String,
    val profession: FounderProfession,
    val skillLevel: Int = SkillConstants.FOUNDER_SKILL_LEVEL // 使用常量定义
) {
    fun toEmployee(): Employee {
        return Employee(
            id = 0, // 特殊ID标识创始人
            name = name,
            position = profession.displayName,
            skillDevelopment = if (profession.specialtySkill == "开发") SkillConstants.FOUNDER_SKILL_LEVEL else 0,
            skillDesign = if (profession.specialtySkill == "设计") SkillConstants.FOUNDER_SKILL_LEVEL else 0,
            skillArt = if (profession.specialtySkill == "美工") SkillConstants.FOUNDER_SKILL_LEVEL else 0,
            skillMusic = if (profession.specialtySkill == "音乐") SkillConstants.FOUNDER_SKILL_LEVEL else 0,
            skillService = if (profession.specialtySkill == "服务") SkillConstants.FOUNDER_SKILL_LEVEL else 0,
            salary = 0, // 创始人无薪资
            isFounder = true
        )
    }
}

// 游戏相关数据类
data class Game(
    val id: String,
    val name: String,
    val theme: GameTheme,
    val platforms: List<Platform>,
    val businessModel: BusinessModel,
    val developmentProgress: Float = 0f,
    val isCompleted: Boolean = false,
    val releaseStatus: GameReleaseStatus = GameReleaseStatus.DEVELOPMENT, // 新增：发售状态
    val releasePrice: Float? = null, // 新增：发售价格
    val revenue: Long = 0L,
    val rating: Float? = null, // 游戏评分
    val gameRating: GameRating? = null, // 新增：详细评分信息
    val assignedEmployees: List<Employee> = emptyList(), // 已分配的员工列表
    val monetizationItems: List<MonetizationItem> = emptyList(), // 付费内容列表（仅网络游戏）
    val developmentCost: Long = 0L // 新增：开发成本（用于废弃时返还80%）
) {
    /**
     * 计算游戏开发成本
     * 基于主题、平台数量、商业模式计算
     */
    fun calculateDevelopmentCost(): Long {
        // 基础成本
        var cost = 50000L
        
        // 平台数量影响成本
        cost += platforms.size * 20000L
        
        // 商业模式影响成本
        cost += when (businessModel) {
            BusinessModel.SINGLE_PLAYER -> 30000L
            BusinessModel.ONLINE_GAME -> 80000L
        }
        
        return cost
    }
}

// 游戏评分相关数据类
data class GameRating(
    val gameId: String,
    val finalScore: Float, // 最终评分 (0-10)
    val baseScore: Float = 5.0f, // 基础分
    val skillBonus: Float, // 技能加成
    val skillContributions: List<SkillContribution>, // 技能贡献详情
    val calculatedAt: Long = System.currentTimeMillis()
)

data class SkillContribution(
    val employeeId: Int,
    val employeeName: String,
    val skillType: String, // 主要技能类型
    val skillLevel: Int, // 技能等级
    val contribution: Float // 对评分的贡献值 (skillLevel / 2)
)

// 价格推荐相关数据类
data class PriceRecommendation(
    val gameId: String,
    val recommendedPrice: Float, // 建议价格
    val priceRange: PriceRange, // 价格区间
    val marketAnalysis: String, // 市场分析
    val confidence: Float = 0.8f // 推荐置信度
)

data class PriceRange(
    val minPrice: Float, // 最低建议价格
    val maxPrice: Float, // 最高建议价格
    val optimalPrice: Float // 最优价格
)

// 市场因素数据类
data class MarketFactors(
    val platformMultipliers: Map<Platform, Float>, // 平台价格系数
    val themePopularity: Map<GameTheme, Float>, // 主题受欢迎度
    val businessModelFactors: Map<BusinessModel, Float> // 商业模式因素
)

// 付费内容推荐数据类（网络游戏专用）
data class MonetizationRecommendation(
    val gameId: String,
    val itemPrices: ItemPriceRecommendation, // 道具价格建议
    val vipPrices: VipPriceRecommendation, // VIP价格建议
    val marketAnalysis: String, // 市场分析
    val confidence: Float = 0.8f // 推荐置信度
)

data class ItemPriceRecommendation(
    val lowTier: Float, // 低档道具价格 (如小额消费道具)
    val midTier: Float, // 中档道具价格 (如礼包、皮肤)
    val highTier: Float // 高档道具价格 (如稀有道具)
)

data class VipPriceRecommendation(
    val monthly: Float, // 月卡价格
    val seasonal: Float, // 季卡价格
    val yearly: Float // 年卡价格
)

// 存档数据类
data class SaveData(
    val companyName: String = "我的游戏公司",
    val founderName: String = "创始人",
    val founderProfession: FounderProfession? = null, // 新增字段，向后兼容
    val money: Long = 1000000L,
    val fans: Int = 0,
    val currentYear: Int = 1,
    val currentMonth: Int = 1,
    val currentDay: Int = 1,
    val allEmployees: List<Employee> = emptyList(),
    val games: List<Game> = emptyList(),
    val saveTime: Long = System.currentTimeMillis()
)