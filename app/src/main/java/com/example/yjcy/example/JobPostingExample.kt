package com.example.yjcy.example

import androidx.compose.runtime.*
import androidx.compose.material3.*
import com.example.yjcy.data.SaveData
import com.example.yjcy.ui.components.NewTalentMarketDialog
import com.example.yjcy.service.JobPostingService

/**
 * 岗位发布系统集成示例
 * 
 * 这个文件展示了如何在现有系统中集成新的岗位发布系统
 */

/**
 * 示例1: 在员工管理界面中使用
 */
@Composable
fun EmployeeManagementExample(saveData: SaveData) {
    var showTalentMarket by remember { mutableStateOf(false) }
    
    // 按钮触发打开人才市场
    Button(onClick = { showTalentMarket = true }) {
        Text("打开人才市场")
    }
    
    // 显示新的岗位发布对话框
    if (showTalentMarket) {
        NewTalentMarketDialog(
            saveData = saveData,
            onDismiss = { 
                showTalentMarket = false 
            },
            onRecruitCandidate = { candidate ->
                // 将候选人转换为员工并添加到员工列表
                val newEmployee = candidate.toEmployee(
                    newId = saveData.allEmployees.size + 1
                )
                
                // 扣除招聘费用（这里需要在调用处更新SaveData）
                // 注意：由于SaveData是不可变的，需要在外部处理更新
                // 这里仅作示例，实际使用需要通过ViewModel或State管理
                println("招聘成功: ${candidate.name}")
                println("提示：需要在外部更新SaveData（使用copy()方法）")
            }
        )
    }
}

/**
 * 示例2: 游戏时间推进时生成应聘者
 */
fun onGameDayPass(daysElapsed: Int = 1) {
    val jobPostingService = JobPostingService.getInstance()
    
    // 为所有活跃岗位生成应聘者
    jobPostingService.generateApplicantsForActiveJobs(daysElapsed)
    
    // 获取待处理的应聘者数量，可以显示通知
    val pendingCount = jobPostingService.getTotalPendingApplicants()
    if (pendingCount > 0) {
        println("有 $pendingCount 位应聘者待处理")
        // 这里可以显示游戏内通知
    }
}

/**
 * 示例3: 游戏存档时保存岗位数据
 */
fun saveGameData(saveData: SaveData): Map<String, Any> {
    val jobPostingService = JobPostingService.getInstance()
    
    return mapOf(
        "saveData" to saveData,
        "jobPostings" to jobPostingService.getAllJobPostings()
    )
}

/**
 * 示例4: 游戏读档时恢复岗位数据
 */
fun loadGameData(data: Map<String, Any>) {
    val jobPostingService = JobPostingService.getInstance()
    
    // 清除现有数据
    jobPostingService.clearAllData()
    
    // TODO: 从存档中恢复岗位数据
    // 需要在JobPostingService中添加导入方法
}

/**
 * 示例5: 快捷操作 - 批量处理应聘者
 */
fun autoProcessApplicants(
    jobId: String,
    acceptThreshold: Int = 70
) {
    val jobPostingService = JobPostingService.getInstance()
    val job = jobPostingService.getJobPosting(jobId) ?: return
    
    // 对所有待处理的应聘者进行人事面试
    job.applicants
        .filter { it.status == com.example.yjcy.data.ApplicantStatus.PENDING }
        .forEach { applicant ->
            jobPostingService.conductHRInterview(jobId, applicant.id)
        }
}

/**
 * 辅助函数：计算招聘费用
 */
private fun calculateRecruitmentCost(candidate: com.example.yjcy.data.TalentCandidate): Long {
    val baseFee = 5000L
    val skillBonus = candidate.getMaxSkillLevel() * 2000L
    return baseFee + skillBonus
}

/**
 * 示例6: 监控岗位状态
 */
fun monitorJobPostings(): JobPostingStatus {
    val jobPostingService = JobPostingService.getInstance()
    
    val activeJobs = jobPostingService.getActiveJobPostings()
    val totalApplicants = activeJobs.sumOf { it.applicants.size }
    val pendingApplicants = jobPostingService.getTotalPendingApplicants()
    
    return JobPostingStatus(
        activeJobCount = activeJobs.size,
        totalApplicants = totalApplicants,
        pendingApplicants = pendingApplicants
    )
}

data class JobPostingStatus(
    val activeJobCount: Int,
    val totalApplicants: Int,
    val pendingApplicants: Int
)

