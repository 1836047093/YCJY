package com.example.yjcy.service

import com.example.yjcy.data.*
import com.example.yjcy.ui.BusinessModel
import kotlin.random.Random

/**
 * 客服中心管理器
 * 负责客诉的生成、分配、处理和统计
 */
object CustomerServiceManager {
    
    /**
     * 每月为已发售游戏生成客诉
     * @param games 所有游戏列表
     * @param currentYear 当前年份
     * @param currentMonth 当前月份
     * @param currentDay 当前日期
     * @return 新生成的客诉列表
     */
    fun generateMonthlyComplaints(
        games: List<Game>,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): List<Complaint> {
        val newComplaints = mutableListOf<Complaint>()
        
        // 只对已发售的游戏生成客诉
        val releasedGames = games.filter { 
            it.releaseStatus == GameReleaseStatus.RELEASED || 
            it.releaseStatus == GameReleaseStatus.RATED 
        }
        
        releasedGames.forEach { game ->
            // 根据游戏类型确定生成概率
            val generationProbability = when (game.businessModel) {
                BusinessModel.SINGLE_PLAYER -> 0.30 // 单机30%
                BusinessModel.ONLINE_GAME -> 0.50    // 网游50%
            }
            
            // 判断是否生成客诉
            if (Random.nextFloat() < generationProbability) {
                // 每个游戏每月最多生成1-2个客诉
                val complaintCount = Random.nextInt(1, 3)
                
                repeat(complaintCount) {
                    val complaint = generateComplaint(game, currentYear, currentMonth, currentDay)
                    newComplaints.add(complaint)
                }
            }
        }
        
        return newComplaints
    }
    
    /**
     * 生成单个客诉
     */
    private fun generateComplaint(
        game: Game,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Complaint {
        // 确定客诉类型
        val type = selectComplaintType(game)
        
        // 确定严重程度（低50%，中35%，高15%）
        val severity = when (Random.nextInt(100)) {
            in 0..49 -> ComplaintSeverity.LOW
            in 50..84 -> ComplaintSeverity.MEDIUM
            else -> ComplaintSeverity.HIGH
        }
        
        return Complaint(
            id = "${game.id}_${currentYear}_${currentMonth}_${currentDay}_${Random.nextInt(1000)}",
            gameId = game.id,
            gameName = game.name,
            type = type,
            severity = severity,
            workload = severity.workload,
            currentProgress = 0,
            assignedEmployeeId = null,
            status = ComplaintStatus.PENDING,
            createdYear = currentYear,
            createdMonth = currentMonth,
            createdDay = currentDay
        )
    }
    
    /**
     * 根据游戏类型选择客诉类型
     */
    private fun selectComplaintType(game: Game): ComplaintType {
        return when (game.businessModel) {
            BusinessModel.ONLINE_GAME -> {
                // 网游有服务器和付费相关问题
                when (Random.nextInt(100)) {
                    in 0..24 -> ComplaintType.BUG
                    in 25..44 -> ComplaintType.BALANCE
                    in 45..64 -> ComplaintType.CONTENT
                    in 65..79 -> ComplaintType.SERVER
                    in 80..89 -> ComplaintType.PAYMENT
                    else -> ComplaintType.OTHER
                }
            }
            BusinessModel.SINGLE_PLAYER -> {
                // 单机游戏没有服务器和付费问题
                when (Random.nextInt(100)) {
                    in 0..34 -> ComplaintType.BUG
                    in 35..64 -> ComplaintType.BALANCE
                    in 65..94 -> ComplaintType.CONTENT
                    else -> ComplaintType.OTHER
                }
            }
        }
    }
    
    /**
     * 分配客服到客诉
     */
    fun assignEmployee(
        complaint: Complaint,
        employee: Employee
    ): Complaint {
        return complaint.copy(
            assignedEmployeeId = employee.id,
            status = ComplaintStatus.IN_PROGRESS
        )
    }
    
    /**
     * 取消分配
     */
    fun unassignEmployee(complaint: Complaint): Complaint {
        return complaint.copy(
            assignedEmployeeId = null,
            status = if (complaint.currentProgress > 0) ComplaintStatus.IN_PROGRESS else ComplaintStatus.PENDING
        )
    }
    
    /**
     * 每天处理客诉（在日结算时调用）
     * @param complaints 所有客诉列表
     * @param employees 所有员工列表
     * @return 更新后的客诉列表和处理完成的客诉列表
     */
    fun processDailyComplaints(
        complaints: List<Complaint>,
        employees: List<Employee>
    ): Pair<List<Complaint>, List<Complaint>> {
        val updatedComplaints = mutableListOf<Complaint>()
        val completedComplaints = mutableListOf<Complaint>()
        
        complaints.forEach { complaint ->
            if (complaint.status == ComplaintStatus.COMPLETED) {
                // 已完成的客诉保持原样
                updatedComplaints.add(complaint)
            } else if (complaint.assignedEmployeeId != null) {
                // 有分配客服的客诉，进行处理
                val employee = employees.find { it.id == complaint.assignedEmployeeId }
                if (employee != null) {
                    val dailyProgress = calculateDailyProgress(employee)
                    val newProgress = complaint.currentProgress + dailyProgress
                    
                    if (newProgress >= complaint.workload) {
                        // 处理完成
                        val completedComplaint = complaint.copy(
                            currentProgress = complaint.workload,
                            status = ComplaintStatus.COMPLETED
                        )
                        updatedComplaints.add(completedComplaint)
                        completedComplaints.add(completedComplaint)
                    } else {
                        // 继续处理中
                        updatedComplaints.add(complaint.copy(currentProgress = newProgress))
                    }
                } else {
                    // 员工不存在，取消分配
                    updatedComplaints.add(unassignEmployee(complaint))
                }
            } else {
                // 未分配的客诉保持原样
                updatedComplaints.add(complaint)
            }
        }
        
        return Pair(updatedComplaints, completedComplaints)
    }
    
    /**
     * 计算客服每天的处理量
     * 基础处理量60 × 技能加成
     * 
     * 实际处理量：
     * - 1级：60/天（1.3天完成低等客诉）
     * - 2级：78/天（2.6天完成中等客诉）
     * - 3级：102/天（3.4天完成高等客诉）
     * - 4级：132/天（2.7天完成高等客诉）
     * - 5级：168/天（2.1天完成高等客诉）
     */
    private fun calculateDailyProgress(employee: Employee): Int {
        val baseProgress = 60 // 从50提升到60，提高20%处理能力
        val skillLevel = employee.skillService
        
        // 技能加成倍率
        val multiplier = when (skillLevel) {
            1 -> 1.0
            2 -> 1.3
            3 -> 1.7
            4 -> 2.2
            5 -> 2.8
            else -> 1.0
        }
        
        return (baseProgress * multiplier).toInt()
    }
    
    /**
     * 计算超时客诉造成的粉丝损失
     */
    fun calculateOverdueFanLoss(
        complaints: List<Complaint>,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Int {
        var totalLoss = 0
        
        complaints.forEach { complaint ->
            if (complaint.status != ComplaintStatus.COMPLETED) {
                if (complaint.isOverdue(currentYear, currentMonth, currentDay)) {
                    val existingDays = complaint.calculateExistingDays(currentYear, currentMonth, currentDay)
                    val overdueDays = existingDays - complaint.severity.overdueThreshold
                    if (overdueDays > 0) {
                        totalLoss += complaint.severity.dailyFanLoss
                    }
                }
            }
        }
        
        return totalLoss
    }
    
    /**
     * 清理已完成的旧客诉（保留最近30条）
     */
    fun cleanupOldComplaints(complaints: List<Complaint>): List<Complaint> {
        val activeComplaints = complaints.filter { it.status != ComplaintStatus.COMPLETED }
        val completedComplaints = complaints.filter { it.status == ComplaintStatus.COMPLETED }
            .takeLast(30) // 只保留最近30条已完成的客诉
        
        return activeComplaints + completedComplaints
    }
    
    /**
     * 获取客诉统计信息
     */
    fun getComplaintStatistics(
        complaints: List<Complaint>,
        currentYear: Int,
        currentMonth: Int
    ): ComplaintStatistics {
        val currentMonthComplaints = complaints.filter { 
            it.createdYear == currentYear && it.createdMonth == currentMonth 
        }
        
        val pendingCount = complaints.count { it.status == ComplaintStatus.PENDING }
        val inProgressCount = complaints.count { it.status == ComplaintStatus.IN_PROGRESS }
        val completedThisMonth = currentMonthComplaints.count { it.status == ComplaintStatus.COMPLETED }
        val newThisMonth = currentMonthComplaints.size
        
        return ComplaintStatistics(
            totalPending = pendingCount,
            totalInProgress = inProgressCount,
            completedThisMonth = completedThisMonth,
            newThisMonth = newThisMonth
        )
    }
    
    /**
     * 获取客服员工列表（岗位为"客服"的员工）
     * 只有岗位是客服的员工才能处理客诉
     */
    fun getAvailableCustomerService(employees: List<Employee>): List<Employee> {
        return employees.filter { it.position == "客服" }
            .sortedByDescending { it.skillService }
    }
    
    /**
     * 一键智能分配：自动为所有待处理客诉分配合适的客服
     * 分配策略：
     * 1. 优先处理高严重度客诉
     * 2. 高严重度客诉分配给技能高的客服
     * 3. 平衡每个客服的工作量
     * 
     * @param complaints 所有客诉列表
     * @param employees 所有员工列表
     * @return 更新后的客诉列表和分配数量
     */
    fun autoAssignComplaints(
        complaints: List<Complaint>,
        employees: List<Employee>
    ): Pair<List<Complaint>, Int> {
        // 获取可用客服
        val availableCustomerService = getAvailableCustomerService(employees)
        if (availableCustomerService.isEmpty()) {
            return Pair(complaints, 0)
        }
        
        // 获取待分配的客诉（未分配的客诉）
        val unassignedComplaints = complaints.filter { 
            it.status == ComplaintStatus.PENDING && it.assignedEmployeeId == null 
        }.sortedWith(
            compareByDescending<Complaint> { it.severity } // 优先分配高严重度
                .thenBy { it.createdYear }
                .thenBy { it.createdMonth }
                .thenBy { it.createdDay }
        )
        
        if (unassignedComplaints.isEmpty()) {
            return Pair(complaints, 0)
        }
        
        // 计算每个客服当前的工作量
        val employeeWorkload = mutableMapOf<Int, Int>()
        availableCustomerService.forEach { employee ->
            val currentWorkload = complaints
                .filter { it.assignedEmployeeId == employee.id && it.status != ComplaintStatus.COMPLETED }
                .sumOf { it.workload - it.currentProgress }
            employeeWorkload[employee.id] = currentWorkload
        }
        
        // 开始分配
        val updatedComplaints = complaints.toMutableList()
        var assignedCount = 0
        
        unassignedComplaints.forEach { complaint ->
            // 根据客诉严重度选择合适的客服
            val suitableEmployee = when (complaint.severity) {
                ComplaintSeverity.HIGH -> {
                    // 高严重度：优先选技能最高且工作量不是太多的客服
                    availableCustomerService
                        .filter { employeeWorkload[it.id]!! < 1000 } // 工作量<1000
                        .maxByOrNull { it.skillService }
                        ?: availableCustomerService.minByOrNull { employeeWorkload[it.id]!! }
                }
                ComplaintSeverity.MEDIUM -> {
                    // 中等严重度：选工作量最少的中等技能客服
                    availableCustomerService
                        .filter { it.skillService >= 2 } // 至少2级技能
                        .minByOrNull { employeeWorkload[it.id]!! }
                        ?: availableCustomerService.minByOrNull { employeeWorkload[it.id]!! }
                }
                ComplaintSeverity.LOW -> {
                    // 低严重度：选工作量最少的客服
                    availableCustomerService.minByOrNull { employeeWorkload[it.id]!! }
                }
            }
            
            // 执行分配
            suitableEmployee?.let { employee ->
                val updatedComplaint = assignEmployee(complaint, employee)
                val index = updatedComplaints.indexOfFirst { it.id == complaint.id }
                if (index >= 0) {
                    updatedComplaints[index] = updatedComplaint
                    employeeWorkload[employee.id] = employeeWorkload[employee.id]!! + complaint.workload
                    assignedCount++
                }
            }
        }
        
        return Pair(updatedComplaints, assignedCount)
    }
}

/**
 * 客诉统计信息
 */
data class ComplaintStatistics(
    val totalPending: Int,        // 待处理数量
    val totalInProgress: Int,     // 处理中数量
    val completedThisMonth: Int,  // 本月已完成数量
    val newThisMonth: Int         // 本月新增数量
)
