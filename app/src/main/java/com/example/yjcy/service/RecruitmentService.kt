package com.example.yjcy.service

import android.util.Log
import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.data.Employee
import com.example.yjcy.data.SaveData
import kotlin.math.max
import kotlin.math.min

/**
 * 招聘服务类
 * 负责处理招聘逻辑、费用计算和员工添加
 * 使用单例模式确保一致性
 */
class RecruitmentService {
    
    companion object {
        @Volatile
        private var instance: RecruitmentService? = null
        
        fun getInstance(): RecruitmentService {
            return instance ?: synchronized(this) {
                instance ?: RecruitmentService().also { instance = it }
            }
        }
        
        // 招聘费用基础倍数
        private const val RECRUITMENT_FEE_MULTIPLIER = 1.5
        
        // 最低招聘费用
        private const val MIN_RECRUITMENT_FEE = 2000
        
        // 最高招聘费用
        private const val MAX_RECRUITMENT_FEE = 30000
        
        // 最大员工数量限制
        private const val MAX_EMPLOYEE_COUNT = 30
        
        // 技能等级费用系数
        private val SKILL_LEVEL_MULTIPLIER = mapOf(
            1 to 0.8, 2 to 1.0, 3 to 1.3, 4 to 1.8, 5 to 2.5
        )
        
        private const val TAG = "RecruitmentService"
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
     * 返回招聘结果，包含更新后的员工列表和资金
     */
    fun recruitCandidate(
        candidate: TalentCandidate,
        currentEmployees: List<Employee>,
        currentMoney: Long,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): RecruitmentResult {
        Log.d(TAG, "开始雇佣流程: ${candidate.name}, 职位: ${candidate.position}")
        
        // 验证候选人数据
        val validationResult = validateCandidate(candidate)
        if (!validationResult.isValid) {
            Log.e(TAG, "候选人验证失败: ${validationResult.message}")
            return RecruitmentResult(
                success = false,
                message = validationResult.message,
                employee = null,
                cost = 0
            )
        }
        
        // 计算招聘费用
        val recruitmentFee = calculateRecruitmentFee(candidate)
        Log.d(TAG, "招聘费用: ¥$recruitmentFee")
        
        // 检查资金是否足够
        if (currentMoney < recruitmentFee) {
            Log.w(TAG, "资金不足: 需要 ¥$recruitmentFee，当前只有 ¥$currentMoney")
            return RecruitmentResult(
                success = false,
                message = "资金不足，需要 ¥${recruitmentFee}，当前只有 ¥${currentMoney}",
                employee = null,
                cost = recruitmentFee
            )
        }
        
        // 检查员工数量限制
        if (currentEmployees.size >= MAX_EMPLOYEE_COUNT) {
            Log.w(TAG, "员工数量已达上限: ${currentEmployees.size}/$MAX_EMPLOYEE_COUNT")
            return RecruitmentResult(
                success = false,
                message = "员工数量已达上限（${currentEmployees.size}/$MAX_EMPLOYEE_COUNT），无法继续招聘",
                employee = null,
                cost = recruitmentFee
            )
        }
        
        try {
            // 生成新员工ID
            val newEmployeeId = generateNewEmployeeId(currentEmployees)
            Log.d(TAG, "生成新员工ID: $newEmployeeId")
            
            // 转换候选人为员工
            val newEmployee = candidate.toEmployee(
                newEmployeeId,
                currentYear,
                currentMonth,
                currentDay
            )
            
            // 验证员工对象
            if (newEmployee.id <= 0 || newEmployee.name.isBlank()) {
                Log.e(TAG, "创建的员工对象无效: ID=${newEmployee.id}, name=${newEmployee.name}")
                return RecruitmentResult(
                    success = false,
                    message = "创建员工对象失败，数据无效",
                    employee = null,
                    cost = recruitmentFee
                )
            }
            
            Log.d(TAG, "成功创建员工: ${newEmployee.name}, ID=${newEmployee.id}")
            
            // 计算更新后的数据
            val updatedEmployees = currentEmployees + newEmployee
            val updatedMoney = currentMoney - recruitmentFee
            
            return RecruitmentResult(
                success = true,
                message = "成功招聘 ${candidate.name}，花费 ¥${recruitmentFee}",
                employee = newEmployee,
                cost = recruitmentFee,
                updatedEmployees = updatedEmployees,
                updatedMoney = updatedMoney
            )
        } catch (e: Exception) {
            Log.e(TAG, "雇佣过程中发生异常", e)
            e.printStackTrace()
            return RecruitmentResult(
                success = false,
                message = "雇佣失败: ${e.message ?: "未知错误"}",
                employee = null,
                cost = recruitmentFee
            )
        }
    }
    
    /**
     * 验证候选人数据的有效性
     */
    private fun validateCandidate(candidate: TalentCandidate): ValidationResult {
        if (candidate.name.isBlank()) {
            return ValidationResult(false, "候选人姓名不能为空")
        }
        
        if (candidate.position.isBlank()) {
            return ValidationResult(false, "候选人职位不能为空")
        }
        
        if (candidate.expectedSalary <= 0) {
            return ValidationResult(false, "候选人期望薪资无效")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * 生成新的员工ID
     * 确保ID唯一且为正数
     */
    private fun generateNewEmployeeId(employees: List<Employee>): Int {
        return if (employees.isEmpty()) {
            1
        } else {
            val maxId = employees.maxOfOrNull { it.id } ?: 0
            maxOf(1, maxId + 1)
        }
    }
    
    /**
     * 获取最大员工数量限制
     */
    fun getMaxEmployeeCount(): Int {
        return MAX_EMPLOYEE_COUNT
    }
    
    /**
     * 检查是否可以雇佣更多员工
     */
    fun canHireMore(currentEmployeeCount: Int): Boolean {
        return currentEmployeeCount < MAX_EMPLOYEE_COUNT
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
    val cost: Int,
    val updatedEmployees: List<Employee>? = null,
    val updatedMoney: Long? = null
)

/**
 * 验证结果数据类
 */
private data class ValidationResult(
    val isValid: Boolean,
    val message: String
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