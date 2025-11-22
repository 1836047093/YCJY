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
    SOUND_ENGINEER("音效师", "🎧", "音乐"),
    CUSTOMER_SERVICE("客服", "📞", "服务")
}

// MOBA英雄位置枚举
enum class HeroPosition(val displayName: String) {
    TOP("上单"),
    JUNGLE("打野"),
    MID("中单"),
    ADC("ADC"),
    SUPPORT("辅助")
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

// 游戏开发阶段枚举
enum class DevelopmentPhase(
    val displayName: String,
    val icon: String,
    val description: String,
    val requiredPositions: List<String>, // 必需的职位
    val recommendedCount: Int, // 推荐人数
    val minCount: Int // 最少人数
) {
    DESIGN(
        displayName = "需求文档",
        icon = "📋",
        description = "策划师编写游戏需求文档和设计方案",
        requiredPositions = listOf("策划师"),
        recommendedCount = 2,
        minCount = 1
    ),
    ART_SOUND(
        displayName = "美术音效",
        icon = "🎨",
        description = "美术师和音效师制作游戏资源",
        requiredPositions = listOf("美术师", "音效师"),
        recommendedCount = 2,
        minCount = 1
    ),
    PROGRAMMING(
        displayName = "程序实现",
        icon = "💻",
        description = "程序员实现游戏功能和逻辑",
        requiredPositions = listOf("程序员"),
        recommendedCount = 2,
        minCount = 1
    );
    
    /**
     * 检查员工列表是否满足当前阶段的最低要求
     */
    fun checkRequirements(employees: List<Employee>): Boolean {
        val requiredPositionEmployees = employees.filter { it.position in requiredPositions }
        return requiredPositionEmployees.size >= minCount
    }
    
    /**
     * 获取当前阶段的有效员工（职位匹配的员工）
     */
    fun getValidEmployees(employees: List<Employee>): List<Employee> {
        return employees.filter { it.position in requiredPositions }
    }
    
    /**
     * 计算阶段进度增长速度（基于有效员工的技能）
     */
    fun calculateProgressSpeed(employees: List<Employee>): Float {
        val validEmployees = getValidEmployees(employees)
        if (validEmployees.isEmpty()) return 0f
        
        // 计算每个员工的技能效率
        val employeeEfficiencies = validEmployees.map { employee ->
            // 获取员工在当前阶段的技能等级
            val skillLevel = when (this) {
                DESIGN -> employee.skillDesign
                ART_SOUND -> maxOf(employee.skillArt, employee.skillMusic)
                PROGRAMMING -> employee.skillDevelopment
            }
            
            // 技能倍率：1级=0.04x, 2级=0.005x, 3级=0.006x, 4级=0.007x, 5级=0.08x
            when {
                skillLevel >= 5 -> 1.0f   // 5级：大幅提升
                skillLevel >= 4 -> 0.8f   // 4级：提升
                skillLevel >= 3 -> 0.6f   // 3级：提升
                skillLevel >= 2 -> 0.4f   // 2级：提升
                else -> 0.2f              // 1级：提升
            }
        }
        
        // 计算平均效率
        val avgEfficiency = employeeEfficiencies.average().toFloat()
        
        // 基础进度：每天5%（提升开发速度）
        val baseProgress = 0.05f
        
        // 人数倍率：每人+0.5倍率，最高10人封顶6.0倍
        // 1人=1.0x, 2人=1.5x, 3人=2.0x, 4人=2.5x, 5人=3.0x, 6人=3.5x, ..., 10人=5.5x
        val countMultiplier = (1.0f + (validEmployees.size - 1) * 0.5f).coerceAtMost(6.0f)
        
        // 总进度 = 基础进度 × 平均效率 × 人数倍率
        return baseProgress * avgEfficiency * countMultiplier
    }
    
    /**
     * 获取下一个开发阶段
     */
    fun getNextPhase(): DevelopmentPhase? {
        return when (this) {
            DESIGN -> ART_SOUND
            ART_SOUND -> PROGRAMMING
            PROGRAMMING -> null // 最后阶段，返回null表示开发完成
        }
    }
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
    val loyalty: Int = 100, // 忠诚度（0-100）
    val requestedSalary: Int? = null, // 员工要求的薪资（null表示未提出要求）
    val lastSalaryRequestYear: Int? = null, // 上次提出涨薪要求的年份
    val lastSalaryRequestMonth: Int? = null, // 上次提出涨薪要求的月份
    val salaryRequestCount: Int = 0, // 涨薪请求次数（最多3次）
    val isFounder: Boolean = false,
    val hireYear: Int = 1,  // 入职年份
    val hireMonth: Int = 1, // 入职月份
    val hireDay: Int = 1    // 入职日期
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
     * 清理非专属技能，确保只保留岗位对应的专属技能
     * 用于修复旧存档中的错误数据
     */
    fun cleanNonSpecialtySkills(): Employee {
        return when (getSpecialtySkillType()) {
            "开发" -> this.copy(skillDesign = 0, skillArt = 0, skillMusic = 0, skillService = 0)
            "设计" -> this.copy(skillDevelopment = 0, skillArt = 0, skillMusic = 0, skillService = 0)
            "美工" -> this.copy(skillDevelopment = 0, skillDesign = 0, skillMusic = 0, skillService = 0)
            "音乐" -> this.copy(skillDevelopment = 0, skillDesign = 0, skillArt = 0, skillService = 0)
            "服务" -> this.copy(skillDevelopment = 0, skillDesign = 0, skillArt = 0, skillMusic = 0)
            else -> this // 未知岗位保持不变
        }
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
    
    /**
     * 计算员工工作的总月数
     * @param currentYear 当前游戏年份
     * @param currentMonth 当前游戏月份
     * @param currentDay 当前游戏日期
     * @return 工作的总月数（向上取整）
     */
    fun calculateWorkMonths(currentYear: Int, currentMonth: Int, currentDay: Int): Int {
        // 计算年份差
        val yearDiff = currentYear - hireYear
        // 计算月份差
        val monthDiff = currentMonth - hireMonth
        // 计算总月数
        var totalMonths = yearDiff * 12 + monthDiff
        
        // 如果当前日期大于等于入职日期，说明已经满一个月了，需要+1
        if (currentDay >= hireDay) {
            totalMonths++
        }
        
        // 确保至少1个月
        return maxOf(1, totalMonths)
    }
    
    /**
     * 计算解雇赔偿金额（2N+1公式）
     * N = 工作年数（向上取整）
     * 赔偿金额 = 月薪 × (2N + 1)
     * 
     * @param currentYear 当前游戏年份
     * @param currentMonth 当前游戏月份
     * @param currentDay 当前游戏日期
     * @return 赔偿金额
     */
    fun calculateSeverancePay(currentYear: Int, currentMonth: Int, currentDay: Int): Int {
        val workMonths = calculateWorkMonths(currentYear, currentMonth, currentDay)
        // 计算工作年数（向上取整）
        val workYears = (workMonths + 11) / 12  // 向上取整：(月数 + 11) / 12
        // 赔偿月数 = 2N + 1
        val compensationMonths = 2 * workYears + 1
        // 赔偿金额 = 月薪 × 赔偿月数
        return salary * compensationMonths
    }
    
    /**
     * 计算员工工作年数（用于判断是否满1年）
     * @param currentYear 当前游戏年份
     * @param currentMonth 当前游戏月份
     * @param currentDay 当前游戏日期
     * @return 工作年数（向下取整）
     * 
     * 注意：如果当前日期小于入职日期在同一年，说明还没满1年
     * 例如：入职第1年1月1日，当前第1年12月31日，还没满1年
     *      入职第1年1月1日，当前第2年1月1日，刚好满1年
     */
    fun calculateWorkYears(currentYear: Int, currentMonth: Int, currentDay: Int): Int {
        // 计算年份差
        val yearDiff = currentYear - hireYear
        
        // 如果年份差为0，说明还没满1年
        if (yearDiff == 0) return 0
        
        // 如果年份差大于等于1，还需要检查月份和日期
        if (yearDiff >= 1) {
            // 如果当前月份小于入职月份，说明还没满1年
            if (currentMonth < hireMonth) return yearDiff - 1
            // 如果当前月份等于入职月份，需要检查日期
            if (currentMonth == hireMonth) {
                // 如果当前日期小于入职日期，说明还没满1年
                if (currentDay < hireDay) return yearDiff - 1
            }
        }
        
        return yearDiff
    }
    
    /**
     * 根据技能等级计算对应的薪资标准
     * 公式：技能等级 × 10,000元
     * Lv.1: ¥10,000
     * Lv.2: ¥20,000
     * Lv.3: ¥30,000
     * Lv.4: ¥40,000
     * Lv.5: ¥50,000
     */
    fun calculateSalaryBySkillLevel(): Int {
        val skillLevel = getSpecialtySkillLevel().coerceIn(1, 5)
        return skillLevel * 10000
    }
    
    /**
     * 计算员工期望的薪资（基于技能等级和涨薪次数）
     * 公式：基础薪资 × (1 + 技能等级 × 0.05 + 涨薪次数 × 0.03)
     * 首次涨薪：技能等级加成5%每级 + 3%基础涨幅
     * 第二次涨薪：技能等级加成5%每级 + 6%基础涨幅
     * 第三次涨薪：技能等级加成5%每级 + 9%基础涨幅
     * 例如：3级员工首次涨薪 = 基础薪资 × (1 + 3×0.05 + 1×0.03) = 118%
     * 例如：5级员工第三次涨薪 = 基础薪资 × (1 + 5×0.05 + 3×0.03) = 134%
     */
    fun calculateExpectedSalary(baseSalary: Int): Int {
        val skillLevel = getSpecialtySkillLevel()
        val skillBonus = skillLevel * 0.05 // 技能加成：5%每级（进一步降低）
        val requestBonus = salaryRequestCount * 0.03 // 涨薪次数加成：3%每次（进一步降低）
        val multiplier = 1.0 + skillBonus + requestBonus
        return (baseSalary * multiplier).toInt()
    }
    
    /**
     * 检查员工是否应该提出涨薪要求
     * 规则：
     * - 首次涨薪：入职1年后
     * - 第二次涨薪：入职3年后
     * - 第三次涨薪：入职5年后
     * - 最多只能涨薪3次
     */
    fun shouldRequestSalaryIncrease(currentYear: Int, currentMonth: Int, currentDay: Int): Boolean {
        if (isFounder) return false // 创始人不提要求
        
        // 如果已经涨薪3次，不能再涨薪
        if (salaryRequestCount >= 3) return false
        
        // 如果已经提出过要求但还未处理，不再提出新要求
        if (requestedSalary != null) return false
        
        val workYears = calculateWorkYears(currentYear, currentMonth, currentDay)
        
        // 根据涨薪次数确定需要的入职年数
        val requiredYears = when (salaryRequestCount) {
            0 -> 1 // 首次：入职1年后
            1 -> 3 // 第二次：入职3年后
            2 -> 5 // 第三次：入职5年后
            else -> return false // 超过3次，不允许
        }
        
        if (workYears < requiredYears) return false // 未达到要求的年数
        
        // 如果从未提出过要求，检查是否刚好到了要求的入职日期
        if (lastSalaryRequestYear == null) {
            val targetYear = hireYear + requiredYears
            if (currentYear == targetYear && currentMonth == hireMonth && currentDay == hireDay) {
                return true
            }
            // 兼容旧数据：如果已经超过了目标年份，在入职日期也可以触发
            if (currentYear > targetYear && currentMonth == hireMonth && currentDay == hireDay) {
                return true
            }
            return false
        }
        
        // 如果已经提出过要求，需要检查是否到了下次涨薪的时间
        // 注意：这里需要根据涨薪次数来判断，而不是距离上次请求的时间
        val targetYear = hireYear + requiredYears
        if (currentYear == targetYear && currentMonth == hireMonth && currentDay == hireDay) {
            return true
        }
        
        // 兼容旧数据：如果已经超过了目标年份，在入职日期也可以触发
        if (currentYear > targetYear && currentMonth == hireMonth && currentDay == hireDay) {
            return true
        }
        
        return false
    }
    
    /**
     * 检查员工忠诚度是否过低（低于30）
     */
    fun isLoyaltyLow(): Boolean {
        return loyalty < 30
    }
    
    /**
     * 获取忠诚度百分比
     * @return 忠诚度百分比（0-100）
     */
    fun getLoyaltyPercentage(): Int {
        return loyalty
    }
}

// 创始人数据类
data class Founder(
    val name: String,
    val profession: FounderProfession,
    val skillLevel: Int = SkillConstants.FOUNDER_SKILL_LEVEL // 使用常量定义
) {
    fun toEmployee(
        hireYear: Int = 1,
        hireMonth: Int = 1,
        hireDay: Int = 1
    ): Employee {
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
            loyalty = 100, // 创始人忠诚度100
            isFounder = true,
            hireYear = hireYear,
            hireMonth = hireMonth,
            hireDay = hireDay
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
    val assignedEmployees: List<Employee> = emptyList(), // 已分配的员工列表（当前阶段）
    val allDevelopmentEmployees: List<Employee> = emptyList(), // 所有参与开发的员工列表（所有阶段累积）
    val monetizationItems: List<MonetizationItem> = emptyList(), // 付费内容列表（仅网络游戏）
    val developmentCost: Long = 0L, // 新增：开发成本（用于废弃时返还80%）
    val serverInfo: GameServerInfo? = null, // 新增：服务器信息（仅网络游戏）
    val promotionIndex: Float = 0f, // 新增：宣传指数（0-1，表示0%-100%）
    val autoUpdate: Boolean = false, // 新增：自动更新开关（开启后更新完成会自动发布）
    val autoPromotion: Boolean = false, // 新增：自动宣传开关（开启后宣传指数低于阈值时自动宣传）
    val version: Float = 1.0f, // 新增：游戏版本号，每次更新+0.1
    val currentPhase: DevelopmentPhase = DevelopmentPhase.DESIGN, // 当前开发阶段
    val phaseProgress: Float = 0f, // 当前阶段进度（0-1）
    val updateHistory: List<GameUpdate>? = emptyList(), // 游戏更新历史记录，可空类型以兼容旧存档
    val currentTournament: EsportsTournament? = null, // 当前进行中的赛事
    val lastTournamentDate: GameDate? = null, // 上次举办赛事的日期
    val tournamentHistory: List<EsportsTournament>? = emptyList(), // 赛事历史记录（最近5场），可空类型以兼容旧存档
    val awards: List<GVAAward> = emptyList(), // 获得的GVA奖项列表
    val releaseYear: Int? = null, // 发售年份
    val releaseMonth: Int? = null, // 发售月份
    val releaseDay: Int? = null, // 发售日期
    val fromIP: GameIP? = null // 使用的IP（null表示原创游戏）
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

// 媒体评测相关数据类
data class MediaReview(
    val mediaName: String, // 媒体名称
    val rating: Float, // 该媒体给出的评分 (0-10)
    val comment: String // 评价内容
)

// 游戏评分相关数据类
data class GameRating(
    val gameId: String,
    val finalScore: Float, // 最终评分 (0-10)
    val baseScore: Float = GameRatingCalculator.BASE_SCORE, // 基础分
    val skillBonus: Float, // 技能加成
    val skillContributions: List<SkillContribution>, // 技能贡献详情
    val mediaReviews: List<MediaReview> = emptyList(), // 媒体评测列表
    val calculatedAt: Long = System.currentTimeMillis()
)

data class SkillContribution(
    val employeeId: Int,
    val employeeName: String,
    val skillType: String, // 主要技能类型
    val skillLevel: Int, // 技能等级
    val contribution: Float // 对评分的贡献值
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

// 客诉严重程度枚举
enum class ComplaintSeverity(val displayName: String, val workload: Int, val dailyFanLoss: Int, val overdueThreshold: Int) {
    LOW("低", 80, 10, 15),       // 80工作量，每天扣10粉丝，15天超时（给玩家充足的发现和处理时间）
    MEDIUM("中", 200, 25, 12),   // 200工作量，每天扣25粉丝，12天超时（考虑多任务并行和调度时间）
    HIGH("高", 350, 50, 8)       // 350工作量，每天扣50粉丝，8天超时（给玩家时间培养高级客服）
}

// 客诉类型枚举
enum class ComplaintType(val displayName: String, val icon: String) {
    BUG("游戏Bug", "🐛"),
    BALANCE("平衡性问题", "⚖️"),
    CONTENT("内容不满意", "📝"),
    SERVER("服务器问题", "🖥️"),
    PAYMENT("付费争议", "💰"),
    OTHER("其他问题", "❓")
}

// 客诉状态枚举
enum class ComplaintStatus {
    PENDING,    // 待处理
    IN_PROGRESS, // 处理中
    COMPLETED,   // 已完成
    OVERDUE      // 已超时
}

// 客诉数据类
data class Complaint(
    val id: String,               // 客诉ID
    val gameId: String,           // 关联的游戏ID
    val gameName: String,         // 游戏名称
    val type: ComplaintType,      // 客诉类型
    val severity: ComplaintSeverity, // 严重程度
    val workload: Int,            // 需要处理的工作量
    val currentProgress: Int = 0, // 当前处理进度
    val assignedEmployeeId: Int? = null, // 分配的客服ID
    val status: ComplaintStatus = ComplaintStatus.PENDING, // 状态
    val createdYear: Int,         // 生成年份
    val createdMonth: Int,        // 生成月份
    val createdDay: Int,          // 生成日期
    val completedYear: Int? = null, // 完成年份（可选，用于统计本月完成）
    val completedMonth: Int? = null, // 完成月份（可选，用于统计本月完成）
    val completedDay: Int? = null   // 完成日期（可选，用于统计本月完成）
) {
    /**
     * 计算客诉存在天数
     */
    fun calculateExistingDays(currentYear: Int, currentMonth: Int, currentDay: Int): Int {
        val yearDiff = currentYear - createdYear
        val monthDiff = currentMonth - createdMonth
        val dayDiff = currentDay - createdDay
        return yearDiff * 360 + monthDiff * 30 + dayDiff // 简化计算：每月30天
    }
    
    /**
     * 判断是否已超时
     */
    fun isOverdue(currentYear: Int, currentMonth: Int, currentDay: Int): Boolean {
        val existingDays = calculateExistingDays(currentYear, currentMonth, currentDay)
        return existingDays > severity.overdueThreshold
    }
    
    /**
     * 计算当前应扣除的粉丝数
     */
    fun calculateFanLoss(currentYear: Int, currentMonth: Int, currentDay: Int): Long {
        if (!isOverdue(currentYear, currentMonth, currentDay)) return 0L
        val overdueDays = calculateExistingDays(currentYear, currentMonth, currentDay) - severity.overdueThreshold
        return overdueDays * severity.dailyFanLoss.toLong()
    }
    
    /**
     * 获取处理进度百分比
     */
    fun getProgressPercentage(): Int {
        return ((currentProgress.toFloat() / workload) * 100).toInt()
    }
    
    /**
     * 判断是否已完成
     */
    fun isCompleted(): Boolean {
        return currentProgress >= workload
    }
}

// 游戏日期数据类
data class GameDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<GameDate> {
    override fun toString(): String {
        return "${year}年${month}月${day}日"
    }
    
    override fun compareTo(other: GameDate): Int {
        return when {
            year != other.year -> year.compareTo(other.year)
            month != other.month -> month.compareTo(other.month)
            else -> day.compareTo(other.day)
        }
    }
}

// 玩家评论数据类
data class PlayerComment(
    val id: String = java.util.UUID.randomUUID().toString(),  // 唯一ID
    val playerName: String,  // 玩家昵称
    val content: String,  // 评论内容
    var likes: Int,  // 点赞数
    var isLikedByUser: Boolean = false  // 玩家是否点赞过
)

// 游戏更新记录数据类
data class GameUpdate(
    val updateNumber: Int,  // 第几次更新（1, 2, 3...）
    val version: Float = 1.0f,  // 更新后的游戏版本号（如 1.1, 1.2, 1.3...），默认值用于兼容旧数据
    val updateDate: GameDate,  // 更新日期
    val updateContent: List<String>,  // 更新内容列表（如："新皮肤", "新道具"）
    val announcement: String,  // 更新公告（玩家输入的，或默认的）
    val comments: List<PlayerComment> = emptyList()  // 玩家评论
) {
    /**
     * 获取显示的版本号
     * 兼容旧数据：如果version是默认值，根据updateNumber计算版本号
     */
    fun getDisplayVersion(): Float {
        return if (version <= 1.0f && updateNumber > 0) {
            // 旧数据：根据更新次数计算版本号（初始版本1.0，每次更新+0.1）
            // 第1次更新 → 1.1，第2次更新 → 1.2，第n次更新 → 1.0 + n * 0.1
            1.0f + updateNumber * 0.1f
        } else {
            version
        }
    }
}

// 游戏IP数据类（收购竞争对手后获得的IP）
data class GameIP(
    val id: String,  // IP唯一ID
    val name: String,  // IP名称（原游戏名）
    val originalCompany: String,  // 原公司名称
    val theme: GameTheme,  // 游戏主题
    val originalRating: Float,  // 原游戏评分（影响IP知名度）
    val acquiredYear: Int,  // 收购年份
    val acquiredMonth: Int,  // 收购月份
    val platforms: List<Platform> = emptyList(),  // 原游戏平台（参考信息）
    val businessModel: BusinessModel = BusinessModel.SINGLE_PLAYER  // 原游戏类型（参考信息）
) {
    /**
     * 计算IP知名度加成
     * 基于原游戏评分：评分越高，知名度越高，销量加成越大
     * 加成范围：10%-35%
     */
    fun calculateIPBonus(): Float {
        return when {
            originalRating >= 7.5f -> 0.35f  // 7.5分以上：+35%销量（知名IP）
            originalRating >= 6.5f -> 0.20f  // 6.5-7.5分：+20%销量（普通IP）
            else -> 0.10f  // 6.5分以下：+10%销量（小众IP，保底加成）
        }
    }
    
    /**
     * 获取IP等级描述
     */
    fun getIPLevel(): String {
        return when {
            originalRating >= 7.5f -> "知名IP"
            originalRating >= 6.5f -> "普通IP"
            else -> "小众IP"
        }
    }
}

/**
 * 贷款数据类
 */
data class Loan(
    val id: String = java.util.UUID.randomUUID().toString(), // 贷款唯一ID
    val amount: Long, // 贷款金额
    val interestRate: Double, // 年利率（如0.08表示8%）
    val totalMonths: Int, // 总还款月数
    val remainingMonths: Int, // 剩余还款月数
    val monthlyPayment: Long, // 月还款额（本金+利息）
    val startDate: GameDate, // 贷款开始日期
    val loanType: LoanType // 贷款类型
) {
    /**
     * 计算贷款总利息
     */
    fun getTotalInterest(): Long {
        return monthlyPayment * totalMonths - amount
    }
    
    /**
     * 计算已还金额
     */
    fun getPaidAmount(): Long {
        return monthlyPayment * (totalMonths - remainingMonths)
    }
    
    /**
     * 计算剩余本金
     */
    fun getRemainingPrincipal(): Long {
        return monthlyPayment * remainingMonths
    }
}

/**
 * 贷款类型枚举
 */
enum class LoanType(
    val typeName: String,
    val maxAmount: Long, // 最大贷款金额
    val interestRate: Double, // 年利率
    val maxMonths: Int, // 最长还款月数
    val minMonths: Int, // 最短还款月数
    val description: String
) {
    SHORT_TERM("短期贷款", 5000000L, 0.06, 12, 6, "6-12个月，年利率6%，快速周转"),
    MEDIUM_TERM("中期贷款", 20000000L, 0.08, 36, 12, "12-36个月，年利率8%，适合扩张"),
    LONG_TERM("长期贷款", 50000000L, 0.10, 60, 24, "24-60个月，年利率10%，大额投资");
    
    /**
     * 计算月利率
     */
    fun getMonthlyInterestRate(): Double {
        return interestRate / 12
    }
    
    /**
     * 计算月还款额（等额本息）
     * 公式：月还款额 = 贷款本金 × [月利率 × (1 + 月利率)^还款月数] / [(1 + 月利率)^还款月数 - 1]
     */
    fun calculateMonthlyPayment(amount: Long, months: Int): Long {
        val monthlyRate = getMonthlyInterestRate()
        if (monthlyRate == 0.0) return amount / months
        
        val temp = Math.pow(1 + monthlyRate, months.toDouble())
        val monthlyPayment = amount * (monthlyRate * temp) / (temp - 1)
        return monthlyPayment.toLong()
    }
}

/**
 * 战队Logo配置类
 */
data class TeamLogoConfig(
    val backgroundColor1: Long = 0xFF1565C0, // 深蓝
    val backgroundColor2: Long = 0xFF0D47A1, // 更深蓝
    val borderColor1: Long = 0xFFFFD700,     // 金色
    val borderColor2: Long = 0xFFFFA000,     // 橙金
    val iconColor: Long = 0xFFFFFFFF,        // 白色
    val teamName: String = "YJCY",          // 主标题
    val subText: String = ""               // 副标题
)

// 存档数据类
data class SaveData(
    val companyName: String = "我的游戏公司",
    val companyLogo: String = "🎮", // 公司LOGO
    val teamLogo: TeamLogoConfig = TeamLogoConfig(), // 战队Logo配置
    val founderName: String = "创始人",
    val founderProfession: FounderProfession? = null, // 新增字段,向后兼容
    val money: Long = 3000000L,
    val fans: Long = 0L,
    val currentYear: Int = 1,
    val currentMonth: Int = 1,
    val currentDay: Int = 1,
    val allEmployees: List<Employee> = emptyList(),
    val games: List<Game> = emptyList(),
    val competitors: List<CompetitorCompany> = emptyList(), // 竞争对手公司列表
    val competitorNews: List<CompetitorNews> = emptyList(), // 竞争对手动态新闻（最近30条）
    val serverData: Map<String, GameServerInfo> = emptyMap(), // 服务器数据（所有游戏的服务器信息）
    val revenueData: Map<String, GameRevenue> = emptyMap(), // 收益数据（所有已发售游戏的收益信息）
    val jobPostings: List<JobPosting> = emptyList(), // 招聘岗位列表
    val complaints: List<Complaint> = emptyList(), // 客诉列表
    val loans: List<Loan> = emptyList(), // 贷款列表
    val autoProcessComplaints: Boolean = false, // 新增：自动处理客诉开关（默认关闭）
    val autoPromotionThreshold: Float = 0.5f, // 新增：自动宣传阈值（0-1，表示0%-100%，低于此值自动宣传）
    val autoApproveSalaryIncrease: Boolean = false, // 新增：自动审批员工涨薪开关（默认关闭）
    val unlockedAchievements: List<UnlockedAchievement> = emptyList(), // 新增：已解锁的成就列表
    val completedTutorials: Set<String> = emptySet(), // 新增：已完成的教程ID集合（使用String存储以便序列化）
    val skipTutorial: Boolean = false, // 新增：是否跳过所有教程（默认不跳过）
    val companyReputation: com.example.yjcy.data.CompanyReputation = com.example.yjcy.data.CompanyReputation(), // GVA：公司声望系统
    val gvaHistory: List<com.example.yjcy.data.AwardNomination> = emptyList(), // GVA：历史获奖记录（最近10年）
    val currentYearNominations: List<com.example.yjcy.data.AwardNomination> = emptyList(), // GVA：当年提名（12月15日生成）
    val gvaAnnouncedDate: GameDate? = null, // GVA：最近一次颁奖日期
    val ownedIPs: List<GameIP> = emptyList(), // 拥有的游戏IP列表（收购竞争对手后获得）
    val subsidiaries: List<Subsidiary> = emptyList(), // 子公司列表（收购竞争对手后转为子公司）
    // MOBA电竞系统
    val mobaHeroes: List<com.example.yjcy.data.esports.MobaHero> = emptyList(), // MOBA英雄池（100个英雄）
    val esportsPlayers: List<com.example.yjcy.data.esports.EsportsPlayer> = emptyList(), // 所有电竞选手
    val myTeamPlayers: List<String> = emptyList(), // 我的战队选手ID列表
    val activeTournaments: List<com.example.yjcy.data.esports.Tournament> = emptyList(), // 进行中的赛事
    val tournamentHistory: List<com.example.yjcy.data.esports.TournamentRecord> = emptyList(), // 赛事历史
    val gmModeEnabled: Boolean = false, // GM模式开关（通过兑换码激活）
    val usedRedeemCodes: Set<String> = emptySet(), // 已使用的兑换码列表
    val isSupporterUnlocked: Boolean = false, // 支持者功能是否解锁（通过SUPPORTER兑换码解锁）
    val esportsTeamUnlocked: Boolean = false, // 战队管理功能是否解锁（需要1亿资金解锁）
    val autoSaveEnabled: Boolean = false, // 自动存档开关（默认关闭）
    val autoSaveInterval: Int = 5, // 自动存档间隔（天，默认5天）
    val lastAutoSaveDay: Int = 0, // 上次自动存档时的游戏天数（用于计算是否到达存档间隔）
    val saveTime: Long = System.currentTimeMillis(),
    val version: String = "1.0.0" // 存档版本号（创建时会被覆盖为当前版本）
)