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
     * 每天为已发售游戏生成客诉（实时生成）
     * @param games 所有游戏列表
     * @param currentYear 当前年份
     * @param currentMonth 当前月份
     * @param currentDay 当前日期
     * @return 新生成的客诉列表
     */
    fun generateDailyComplaints(
        games: List<Game>,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): List<Complaint> {
        val newComplaints = mutableListOf<Complaint>()
        
        // 只对已发售的游戏生成客诉（RELEASED状态才算真正发售）
        val releasedGames = games.filter { 
            it.releaseStatus == GameReleaseStatus.RELEASED
        }
        
        if (releasedGames.isEmpty()) {
            return emptyList()
        }
        
        // 根据已发售游戏数量动态调整概率，避免游戏多时客诉过多
        // 游戏越多，单游戏生成概率越低
        val gameCount = releasedGames.size
        val probabilityMultiplier = when {
            gameCount <= 5 -> 0.8f      // 5款以内：降低20%
            gameCount <= 10 -> 0.5f     // 6-10款：降低50%
            gameCount <= 20 -> 0.3f     // 11-20款：降低70%
            else -> 0.2f                // 20款以上：降低80%
        }
        
        // 大幅降低基础概率：单机0.5%，网游1%（考虑到多游戏时的累积效应）
        releasedGames.forEach { game ->
            val baseProbability = when (game.businessModel) {
                BusinessModel.SINGLE_PLAYER -> 0.005f  // 单机每天0.5%（从1%降至0.5%，再降低50%）
                BusinessModel.ONLINE_GAME -> 0.01f      // 网游每天1%（从2.5%降至1%，再降低60%）
            }
            
            // 应用动态调整后的概率
            val dailyProbability = baseProbability * probabilityMultiplier
            
            // 判断是否生成客诉
            if (Random.nextFloat() < dailyProbability) {
                // 每天最多生成1个客诉
                val complaint = generateComplaint(game, currentYear, currentMonth, currentDay)
                newComplaints.add(complaint)
                android.util.Log.d("CustomerServiceManager", "每日生成客诉: ${game.name} (${game.businessModel}), 调整后概率=${dailyProbability}")
            }
        }
        
        // 额外限制：每天最多生成1个客诉，避免多游戏时客诉爆炸
        val limitedComplaints = newComplaints.shuffled().take(1)
        
        if (limitedComplaints.isNotEmpty()) {
            android.util.Log.d("CustomerServiceManager", "每日生成 ${limitedComplaints.size} 个新客诉（已发售游戏数=${gameCount}，概率倍率=${probabilityMultiplier}）")
        }
        
        return limitedComplaints
    }
    
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
        
        // 只对已发售的游戏生成客诉（RELEASED状态才算真正发售）
        val releasedGames = games.filter { 
            it.releaseStatus == GameReleaseStatus.RELEASED
        }
        
        android.util.Log.d("CustomerServiceManager", "客诉生成检查: 总游戏数=${games.size}, 已发售游戏数=${releasedGames.size}")
        
        if (releasedGames.isEmpty()) {
            android.util.Log.d("CustomerServiceManager", "没有已发售的游戏，跳过客诉生成")
            return emptyList()
        }
        
        releasedGames.forEach { game ->
            // 根据游戏类型确定生成概率
            val generationProbability = when (game.businessModel) {
                BusinessModel.SINGLE_PLAYER -> 0.70 // 单机70%
                BusinessModel.ONLINE_GAME -> 0.90    // 网游90%
            }
            
            android.util.Log.d("CustomerServiceManager", "检查游戏: ${game.name} (${game.businessModel}), 生成概率=${generationProbability}")
            
            // 判断是否生成客诉
            if (Random.nextFloat() < generationProbability) {
                // 每个游戏每月最多生成1-2个客诉
                val complaintCount = Random.nextInt(1, 3)
                
                android.util.Log.d("CustomerServiceManager", "游戏 ${game.name} 生成 ${complaintCount} 个客诉")
                
                repeat(complaintCount) {
                    val complaint = generateComplaint(game, currentYear, currentMonth, currentDay)
                    newComplaints.add(complaint)
                }
            } else {
                android.util.Log.d("CustomerServiceManager", "游戏 ${game.name} 未生成客诉（随机未命中）")
            }
        }
        
        android.util.Log.d("CustomerServiceManager", "本次共生成 ${newComplaints.size} 个客诉")
        
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
     * @param currentYear 当前年份（用于记录完成时间）
     * @param currentMonth 当前月份（用于记录完成时间）
     * @param currentDay 当前日期（用于记录完成时间）
     * @return 更新后的客诉列表和处理完成的客诉列表
     */
    fun processDailyComplaints(
        complaints: List<Complaint>,
        employees: List<Employee>,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
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
                        // 处理完成 - 记录完成时间
                        val completedComplaint = complaint.copy(
                            currentProgress = complaint.workload,
                            status = ComplaintStatus.COMPLETED,
                            completedYear = currentYear,
                            completedMonth = currentMonth,
                            completedDay = currentDay
                        )
                        updatedComplaints.add(completedComplaint)
                        completedComplaints.add(completedComplaint)
                        android.util.Log.d("CustomerServiceManager", "✅ 客诉完成: ${complaint.gameName} - ${complaint.type.displayName}, 完成时间: ${currentYear}年${currentMonth}月${currentDay}日")
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
     * 优化：只遍历活动客诉，避免遍历已完成的客诉
     */
    fun calculateOverdueFanLoss(
        complaints: List<Complaint>,
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int
    ): Long {
        var totalLoss = 0L
        
        // 只遍历活动客诉（未完成的），避免遍历已完成的客诉
        complaints.filter { it.status != ComplaintStatus.COMPLETED }.forEach { complaint ->
            if (complaint.isOverdue(currentYear, currentMonth, currentDay)) {
                // 每天只扣除一次损失（避免重复计算）
                totalLoss += complaint.severity.dailyFanLoss.toLong()
            }
        }
        
        return totalLoss
    }
    
    /**
     * 清理已完成的旧客诉（保留最近30条）
     * 同时限制活动客诉数量上限（最多50个），超出部分按创建时间最早优先清理
     * 修复：确保不会删除本月完成的客诉，至少保留所有本月完成的客诉
     */
    fun cleanupOldComplaints(
        complaints: List<Complaint>,
        currentYear: Int,
        currentMonth: Int
    ): List<Complaint> {
        val activeComplaints = complaints.filter { it.status != ComplaintStatus.COMPLETED }
        val completedComplaints = complaints.filter { it.status == ComplaintStatus.COMPLETED }
        
        // 分离本月完成的客诉和其他已完成的客诉
        val thisMonthCompleted = completedComplaints.filter { complaint ->
            // 本月完成的客诉（有完成时间或创建时间在本月）
            (complaint.completedYear == currentYear && complaint.completedMonth == currentMonth) ||
            (complaint.completedYear == null && complaint.completedMonth == null &&
             complaint.createdYear == currentYear && complaint.createdMonth == currentMonth)
        }
        
        val otherCompleted = completedComplaints.filter { complaint ->
            // 其他已完成的客诉
            !((complaint.completedYear == currentYear && complaint.completedMonth == currentMonth) ||
              (complaint.completedYear == null && complaint.completedMonth == null &&
               complaint.createdYear == currentYear && complaint.createdMonth == currentMonth))
        }
        
        // 对其他已完成的客诉按完成时间排序，保留最近30条
        val keptOtherCompleted = otherCompleted.sortedWith(
            compareByDescending<Complaint> { complaint ->
                val year = complaint.completedYear ?: complaint.createdYear
                val month = complaint.completedMonth ?: complaint.createdMonth
                val day = complaint.completedDay ?: complaint.createdDay
                "${year}${month.toString().padStart(2, '0')}${day.toString().padStart(2, '0')}"
            }
        ).takeLast(30)
        
        // 限制活动客诉数量上限：最多50个，超出部分按创建时间最早优先清理
        val limitedActiveComplaints = if (activeComplaints.size > 50) {
            activeComplaints.sortedWith(
                compareBy<Complaint> { it.createdYear }
                    .thenBy { it.createdMonth }
                    .thenBy { it.createdDay }
            ).takeLast(50) // 保留最新的50个
        } else {
            activeComplaints
        }
        
        // 返回：活动客诉 + 本月完成的客诉（全部保留）+ 其他已完成的客诉（最多30条）
        return limitedActiveComplaints + thisMonthCompleted + keptOtherCompleted
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
        
        // 修复：统计所有在本月完成的客诉（不管是什么时候创建的）
        // 对于旧存档中已完成的客诉（没有完成时间），使用创建时间作为完成时间（向后兼容）
        val completedThisMonth = complaints.count { complaint ->
            complaint.status == ComplaintStatus.COMPLETED && (
                // 新客诉：有完成时间字段
                (complaint.completedYear == currentYear && complaint.completedMonth == currentMonth) ||
                // 旧客诉：没有完成时间字段，使用创建时间判断（向后兼容）
                (complaint.completedYear == null && complaint.completedMonth == null &&
                 complaint.createdYear == currentYear && complaint.createdMonth == currentMonth)
            )
        }
        
        // 调试日志：显示统计详情
        val completedWithTime = complaints.count { it.status == ComplaintStatus.COMPLETED && it.completedYear == currentYear && it.completedMonth == currentMonth }
        val completedWithoutTime = complaints.count { it.status == ComplaintStatus.COMPLETED && it.completedYear == null && it.completedMonth == null && it.createdYear == currentYear && it.createdMonth == currentMonth }
        android.util.Log.d("CustomerServiceManager", "📊 统计本月完成: 总客诉数=${complaints.size}, 已完成=${complaints.count { it.status == ComplaintStatus.COMPLETED }}, 本月完成=${completedThisMonth} (有完成时间=${completedWithTime}, 无完成时间=${completedWithoutTime}), 当前年月=${currentYear}年${currentMonth}月")
        
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
