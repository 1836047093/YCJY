package com.example.yjcy.data

import androidx.room.*
import java.util.UUID
import kotlin.random.Random

/**
 * 候选人实体类
 */
@Entity(
    tableName = "candidates",
    foreignKeys = [
        ForeignKey(
            entity = Recruitment::class,
            parentColumns = ["id"],
            childColumns = ["recruitmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recruitmentId"]),
        Index(value = ["status"]),
        Index(value = ["skillLevel"])
    ]
)
data class Candidate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** 关联的招聘任务ID */
    val recruitmentId: String,
    
    /** 候选人姓名 */
    val name: String,
    
    /** 应聘职位 */
    val position: String,
    
    /** 技能等级 (1-5) */
    val skillLevel: Int,
    
    /** 期望薪资 */
    val expectedSalary: Int,
    
    /** 开发技能 */
    val skillDevelopment: Int = 0,
    
    /** 设计技能 */
    val skillDesign: Int = 0,
    
    /** 美工技能 */
    val skillArt: Int = 0,
    
    /** 音乐技能 */
    val skillMusic: Int = 0,
    
    /** 服务技能 */
    val skillService: Int = 0,
    
    /** 工作经验年数 */
    val experienceYears: Int = 0,
    
    /** 特殊能力描述 */
    val specialAbilities: String = "",
    
    /** 生成时间 */
    val generatedTime: Long = System.currentTimeMillis(),
    
    /** 候选人状态 */
    val status: CandidateStatus = CandidateStatus.PENDING,
    
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取候选人的专属技能类型
     */
    fun getSpecialtySkillType(): String {
        return when (position) {
            "程序员" -> "开发"
            "策划师" -> "设计"
            "美术师" -> "美工"
            "音效师" -> "音乐"
            "测试员" -> "服务"
            else -> "通用"
        }
    }
    
    /**
     * 获取候选人的专属技能等级
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
     * 获取候选人的总技能点数
     */
    fun getTotalSkillPoints(): Int {
        return skillDevelopment + skillDesign + skillArt + skillMusic + skillService
    }
    
    /**
     * 转换为员工对象
     */
    fun toEmployee(): Employee {
        return Employee(
            id = Random.nextInt(10000, 99999), // 生成新的员工ID
            name = name,
            position = position,
            skillDevelopment = skillDevelopment,
            skillDesign = skillDesign,
            skillArt = skillArt,
            skillMusic = skillMusic,
            skillService = skillService,
            salary = expectedSalary,
            experience = experienceYears,
            motivation = Random.nextInt(80, 100), // 随机生成初始动机
            isFounder = false
        )
    }
    
    /**
     * 获取技能星级显示
     */
    fun getSkillStars(): String {
        val stars = "★".repeat(skillLevel) + "☆".repeat(5 - skillLevel)
        return stars
    }
    
    /**
     * 获取经验等级描述
     */
    fun getExperienceDescription(): String {
        return when (experienceYears) {
            0 -> "应届毕业生"
            in 1..2 -> "初级"
            in 3..5 -> "中级"
            in 6..10 -> "高级"
            else -> "资深"
        }
    }
}

/**
 * 候选人状态枚举
 */
enum class CandidateStatus {
    PENDING,  // 待处理
    HIRED,    // 已聘用
    REJECTED  // 已拒绝
}

/**
 * 候选人生成器
 */
object CandidateGenerator {
    
    private val firstNames = listOf(
        "张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"
    )
    
    private val lastNames = listOf(
        "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
        "洋", "勇", "艳", "杰", "涛", "明", "超", "秀兰", "霞", "平",
        "刚", "桂英", "建华", "文", "华", "金凤", "素华", "春梅", "海燕", "雪梅"
    )
    
    private val specialAbilitiesList = listOf(
        "快速学习新技术", "团队协作能力强", "抗压能力优秀", "创新思维活跃",
        "沟通表达能力佳", "项目管理经验丰富", "多语言开发经验", "用户体验敏感度高",
        "数据分析能力强", "问题解决能力突出", "自我驱动力强", "跨部门协调能力好"
    )
    
    /**
     * 生成随机候选人
     */
    fun generateCandidate(
        recruitmentId: String,
        position: String,
        targetSkillLevel: Int,
        salaryBudget: Int
    ): Candidate {
        val name = generateRandomName()
        val skillLevel = generateSkillLevel(targetSkillLevel, salaryBudget)
        val expectedSalary = generateExpectedSalary(skillLevel, salaryBudget)
        val experienceYears = generateExperienceYears(skillLevel)
        val specialAbilities = generateSpecialAbilities()
        
        // 根据职位和技能等级生成具体技能值
        val skills = generateSkillValues(position, skillLevel)
        
        return Candidate(
            recruitmentId = recruitmentId,
            name = name,
            position = position,
            skillLevel = skillLevel,
            expectedSalary = expectedSalary,
            skillDevelopment = skills["development"] ?: 0,
            skillDesign = skills["design"] ?: 0,
            skillArt = skills["art"] ?: 0,
            skillMusic = skills["music"] ?: 0,
            skillService = skills["service"] ?: 0,
            experienceYears = experienceYears,
            specialAbilities = specialAbilities
        )
    }
    
    private fun generateRandomName(): String {
        val firstName = firstNames.random()
        val lastName = lastNames.random()
        return firstName + lastName
    }
    
    private fun generateSkillLevel(targetLevel: Int, salaryBudget: Int): Int {
        // 薪资影响技能等级的概率
        val salaryFactor = when {
            salaryBudget >= 15000 -> 0.8f // 高薪资，80%概率获得目标等级或更高
            salaryBudget >= 10000 -> 0.6f // 中等薪资，60%概率
            salaryBudget >= 5000 -> 0.4f  // 低薪资，40%概率
            else -> 0.2f                   // 极低薪资，20%概率
        }
        
        return if (Random.nextFloat() < salaryFactor) {
            // 获得目标等级或更高
            Random.nextInt(targetLevel, 6).coerceAtMost(5)
        } else {
            // 获得较低等级
            Random.nextInt(1, targetLevel + 1)
        }
    }
    
    private fun generateExpectedSalary(skillLevel: Int, salaryBudget: Int): Int {
        val baseSalary = when (skillLevel) {
            1 -> Random.nextInt(3000, 5000)
            2 -> Random.nextInt(5000, 8000)
            3 -> Random.nextInt(8000, 12000)
            4 -> Random.nextInt(12000, 18000)
            5 -> Random.nextInt(18000, 25000)
            else -> Random.nextInt(3000, 5000)
        }
        
        // 在基础薪资基础上增加一些随机性
        val variation = (baseSalary * 0.2).toInt()
        return baseSalary + Random.nextInt(-variation, variation + 1)
    }
    
    private fun generateExperienceYears(skillLevel: Int): Int {
        return when (skillLevel) {
            1 -> Random.nextInt(0, 2)
            2 -> Random.nextInt(1, 3)
            3 -> Random.nextInt(2, 6)
            4 -> Random.nextInt(4, 8)
            5 -> Random.nextInt(6, 12)
            else -> 0
        }
    }
    
    private fun generateSpecialAbilities(): String {
        val numAbilities = Random.nextInt(1, 4) // 1-3个特殊能力
        return specialAbilitiesList.shuffled().take(numAbilities).joinToString(", ")
    }
    
    private fun generateSkillValues(position: String, skillLevel: Int): Map<String, Int> {
        val skills = mutableMapOf(
            "development" to 0,
            "design" to 0,
            "art" to 0,
            "music" to 0,
            "service" to 0
        )
        
        // 设置主要技能
        val primarySkill = when (position) {
            "程序员" -> "development"
            "策划师" -> "design"
            "美术师" -> "art"
            "音效师" -> "music"
            "测试员" -> "service"
            else -> "development"
        }
        
        skills[primarySkill] = skillLevel
        
        // 随机分配其他技能点
        val remainingSkills = skills.keys.filter { it != primarySkill }
        remainingSkills.forEach { skill ->
            skills[skill] = Random.nextInt(0, (skillLevel * 0.6).toInt() + 1)
        }
        
        return skills
    }
}