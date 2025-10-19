package com.example.yjcy.service

import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.data.Employee
import com.example.yjcy.data.SaveData
import kotlin.math.max
import kotlin.math.min

/**
 * 招聘服务类
 * 负责处理招聘逻辑、费用计算和员工添加
 */
class RecruitmentService {
    
    companion object {
        // 招聘费用基础倍数
        private const val RECRUITMENT_FEE_MULTIPLIER = 1.5
        
        // 最低招聘费用
        private const val MIN_RECRUITMENT_FEE = 2000
        
        // 最高招聘费用
        private const val MAX_RECRUITMENT_FEE = 30000
        
        // 技能等级费用系数
        private val SKILL_LEVEL_MULTIPLIER = mapOf(
            1 to 0.8, 2 to 1.0, 3 to 1.3, 4 to 1.8, 5 to 2.5
        )
    }
    
    /**
     * 计算招聘费用
     * 基于候选人的期望薪资和技能等级
     */
    fun calculateRecruitmentFee(candidate: TalentCandidate): Int {
        val baseFee = candidate.expectedSalary * RECRUITMENT_FEE_MULTIPLIER
        val skillMultiplier = SKILL_LEVEL_MULTIPLIER[candidate.getMaxSkillLevel()] ?: 1.0
        val finalFee = (baseFee * skillMultiplier).toInt()
        
        return max(MIN_RECRUITMENT_FEE, min(finalFee, MAX_RECRUITMENT_FEE))
    }
    
    /**
     * 检查是否有足够资金招聘
     */
    fun canAffordRecruitment(candidate: TalentCandidate, currentMoney: Long): Boolean {
        val recruitmentFee = calculateRecruitmentFee(candidate)
        return currentMoney >= recruitmentFee
    }
    
    /**
     * 执行招聘操作
     * 返回招聘结果
     */
    fun recruitCandidate(
        candidate: TalentCandidate,
        saveData: SaveData,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): RecruitmentResult {
        val recruitmentFee = calculateRecruitmentFee(candidate)
        
        // 检查资金是否足够
        if (saveData.money < recruitmentFee) {
            return RecruitmentResult(
                success = false,
                message = "资金不足，需要 ¥${recruitmentFee}，当前只有 ¥${saveData.money}",
                employee = null,
                cost = recruitmentFee
            )
        }
        
        // 检查员工数量限制
        if (saveData.allEmployees.size >= getMaxEmployeeCount(saveData)) {
            return RecruitmentResult(
                success = false,
                message = "员工数量已达上限，无法招聘更多员工",
                employee = null,
                cost = recruitmentFee
            )
        }
        
        // 生成新员工ID
        val newEmployeeId = generateNewEmployeeId(saveData.allEmployees)
        
        // 转换候选人为员工
        val newEmployee = candidate.toEmployee(
            newEmployeeId,
            currentYear,
            currentMonth,
            currentDay
        )
        
        // 添加员工到游戏数据
        val updatedEmployees = saveData.allEmployees + newEmployee
        val updatedMoney = saveData.money - recruitmentFee
        
        // 更新游戏数据（这里假设有更新方法，实际实现可能需要调整）
        // gameData.copy(employees = updatedEmployees, money = updatedMoney)
        
        return RecruitmentResult(
            success = true,
            message = "成功招聘 ${candidate.name}，花费 ¥${recruitmentFee}",
            employee = newEmployee,
            cost = recruitmentFee
        )
    }
    
    /**
     * 生成新的员工ID
     */
    private fun generateNewEmployeeId(employees: List<Employee>): Int {
        return if (employees.isEmpty()) {
            1
        } else {
            employees.maxOf { it.id } + 1
        }
    }
    
    /**
     * 获取最大员工数量限制
     * 可以根据公司等级或其他因素调整
     */
    private fun getMaxEmployeeCount(saveData: SaveData): Int {
        // 基础员工数量限制
        var maxCount = 10
        
        // 根据公司资金调整限制（示例逻辑）
        when {
            saveData.money >= 100000L -> maxCount = 20
            saveData.money >= 50000L -> maxCount = 15
            saveData.money >= 20000L -> maxCount = 12
        }
        
        return maxCount
    }
    
    /**
     * 获取最大员工数量限制（公共方法）
     */
    fun getMaxEmployeeCount(): Int {
        // 默认员工数量限制
        return 30
    }
    
    /**
     * 获取招聘费用详细信息
     */
    fun getRecruitmentFeeDetails(candidate: TalentCandidate): RecruitmentFeeDetails {
        val baseFee = candidate.expectedSalary * RECRUITMENT_FEE_MULTIPLIER
        val skillMultiplier = SKILL_LEVEL_MULTIPLIER[candidate.getMaxSkillLevel()] ?: 1.0
        val skillBonus = (baseFee * (skillMultiplier - 1.0)).toInt()
        val totalFee = calculateRecruitmentFee(candidate)
        
        return RecruitmentFeeDetails(
            baseFee = baseFee.toInt(),
            skillBonus = skillBonus,
            totalFee = totalFee,
            skillLevel = candidate.getMaxSkillLevel(),
            skillMultiplier = skillMultiplier
        )
    }
    
    /**
     * 预估招聘成功率（可选功能）
     */
    fun estimateRecruitmentSuccessRate(candidate: TalentCandidate, companyReputation: Int = 50): Double {
        // 基础成功率
        var successRate = 0.8
        
        // 根据技能等级调整
        when (candidate.getMaxSkillLevel()) {
            5 -> successRate *= 0.6 // 专家级人才更难招聘
            4 -> successRate *= 0.7
            3 -> successRate *= 0.8
            2 -> successRate *= 0.9
            1 -> successRate *= 0.95
        }
        
        // 根据公司声誉调整
        val reputationBonus = (companyReputation - 50) * 0.002
        successRate += reputationBonus
        
        return max(0.1, min(0.95, successRate))
    }
}

/**
 * 招聘结果数据类
 */
data class RecruitmentResult(
    val success: Boolean,
    val message: String,
    val employee: Employee?,
    val cost: Int
)

/**
 * 招聘费用详细信息数据类
 */
data class RecruitmentFeeDetails(
    val baseFee: Int,
    val skillBonus: Int,
    val totalFee: Int,
    val skillLevel: Int,
    val skillMultiplier: Double
) {
    fun getFormattedDetails(): String {
        return buildString {
            appendLine("基础费用: ¥${baseFee}")
            if (skillBonus > 0) {
                appendLine("技能加成: ¥${skillBonus} (${skillLevel}级技能 x${String.format("%.1f", skillMultiplier)})")
            }
            appendLine("总费用: ¥${totalFee}")
        }
    }
}