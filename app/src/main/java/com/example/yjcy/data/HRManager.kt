package com.example.yjcy.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 人事管理器
 * 负责自动化招聘流程的核心逻辑
 */
class HRManager {
    
    private val candidateGenerator = CandidateGenerator()
    private val matchScoreCalculator = MatchScoreCalculator()
    
    // 招聘配置列表
    private val _recruitmentConfigs = MutableStateFlow<List<RecruitmentConfig>>(emptyList())
    val recruitmentConfigs: StateFlow<List<RecruitmentConfig>> = _recruitmentConfigs.asStateFlow()
    
    // 招聘任务列表
    private val _recruitmentTasks = MutableStateFlow<List<RecruitmentTask>>(emptyList())
    val recruitmentTasks: StateFlow<List<RecruitmentTask>> = _recruitmentTasks.asStateFlow()
    
    // 待确认候选人列表
    private val _pendingCandidates = MutableStateFlow<List<TaskCandidate>>(emptyList())
    val pendingCandidates: StateFlow<List<TaskCandidate>> = _pendingCandidates.asStateFlow()
    
    // 招聘统计信息
    private val _recruitmentStats = MutableStateFlow(RecruitmentTaskStats())
    val recruitmentStats: StateFlow<RecruitmentTaskStats> = _recruitmentStats.asStateFlow()
    
    // 系统状态
    private val _isAutoRecruitmentEnabled = MutableStateFlow(true)
    val isAutoRecruitmentEnabled: StateFlow<Boolean> = _isAutoRecruitmentEnabled.asStateFlow()
    
    // 已雇佣候选人ID集合（避免重复生成）
    private val hiredCandidateIds = mutableSetOf<Int>()
    
    // 协程作用域
    private val hrScope = CoroutineScope(Dispatchers.Default)
    
    init {
        // 启动自动招聘循环
        startAutoRecruitmentLoop()
    }
    
    /**
     * 添加招聘配置
     */
    fun addRecruitmentConfig(config: RecruitmentConfig): Boolean {
        if (!config.isValid()) {
            return false
        }
        
        val newConfig = config.copy(
            id = generateConfigId(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        _recruitmentConfigs.value = _recruitmentConfigs.value + newConfig
        
        // 如果配置是激活状态，创建对应的招聘任务
        if (newConfig.isActive) {
            createRecruitmentTask(newConfig)
        }
        
        return true
    }
    
    /**
     * 更新招聘配置
     */
    fun updateRecruitmentConfig(configId: Int, updatedConfig: RecruitmentConfig): Boolean {
        if (!updatedConfig.isValid()) {
            return false
        }
        
        val configs = _recruitmentConfigs.value.toMutableList()
        val index = configs.indexOfFirst { it.id == configId }
        
        if (index == -1) {
            return false
        }
        
        val newConfig = updatedConfig.copy(
            id = configId,
            updatedAt = System.currentTimeMillis()
        )
        
        configs[index] = newConfig
        _recruitmentConfigs.value = configs
        
        // 更新对应的招聘任务
        updateRecruitmentTaskForConfig(newConfig)
        
        return true
    }
    
    /**
     * 删除招聘配置
     */
    fun removeRecruitmentConfig(configId: Int): Boolean {
        val configs = _recruitmentConfigs.value.toMutableList()
        val removed = configs.removeAll { it.id == configId }
        
        if (removed) {
            _recruitmentConfigs.value = configs
            // 取消对应的招聘任务
            cancelRecruitmentTaskForConfig(configId)
        }
        
        return removed
    }
    
    /**
     * 创建招聘任务
     */
    private fun createRecruitmentTask(config: RecruitmentConfig) {
        val task = RecruitmentTask(
            id = generateTaskId(),
            configId = config.id,
            targetCount = config.targetCount,
            status = RecruitmentTaskStatus.ACTIVE,
            nextSearchTime = System.currentTimeMillis() + 5000L // 5秒后开始搜索
        )
        
        _recruitmentTasks.value = _recruitmentTasks.value + task
    }
    
    /**
     * 更新招聘任务配置
     */
    private fun updateRecruitmentTaskForConfig(config: RecruitmentConfig) {
        val tasks = _recruitmentTasks.value.toMutableList()
        val taskIndex = tasks.indexOfFirst { it.configId == config.id }
        
        if (taskIndex != -1) {
            val task = tasks[taskIndex]
            val updatedTask = task.copy(
                targetCount = config.targetCount,
                status = if (config.isActive) RecruitmentTaskStatus.ACTIVE else RecruitmentTaskStatus.PAUSED,
                updatedAt = System.currentTimeMillis()
            )
            tasks[taskIndex] = updatedTask
            _recruitmentTasks.value = tasks
        }
    }
    
    /**
     * 取消招聘任务
     */
    private fun cancelRecruitmentTaskForConfig(configId: Int) {
        val tasks = _recruitmentTasks.value.toMutableList()
        val taskIndex = tasks.indexOfFirst { it.configId == configId }
        
        if (taskIndex != -1) {
            val task = tasks[taskIndex]
            val cancelledTask = task.copy(
                status = RecruitmentTaskStatus.CANCELLED,
                updatedAt = System.currentTimeMillis()
            )
            tasks[taskIndex] = cancelledTask
            _recruitmentTasks.value = tasks
        }
    }
    
    /**
     * 启动自动招聘循环
     */
    private fun startAutoRecruitmentLoop() {
        hrScope.launch {
            while (true) {
                if (_isAutoRecruitmentEnabled.value) {
                    processAutoRecruitment()
                }
                delay(10000L) // 每10秒检查一次
            }
        }
    }
    
    /**
     * 处理自动招聘
     */
    private suspend fun processAutoRecruitment() {
        val activeTasks = _recruitmentTasks.value.filter { 
            it.status == RecruitmentTaskStatus.ACTIVE && !it.isCompleted()
        }
        
        activeTasks.forEach { task ->
            if (task.needsNewCandidates()) {
                searchCandidatesForTask(task)
            }
            
            // 处理自动批准
            processAutoApproval(task)
        }
        
        // 更新统计信息
        updateRecruitmentStats()
    }
    
    /**
     * 为任务搜索候选人
     */
    private suspend fun searchCandidatesForTask(task: RecruitmentTask) {
        val config = _recruitmentConfigs.value.find { it.id == task.configId } ?: return
        
        // 生成候选人
        val candidates = if (config.priority == RecruitmentPriority.URGENT) {
            candidateGenerator.generateHighQualityCandidates(config, 5)
        } else {
            candidateGenerator.generateCandidates(config, 3, hiredCandidateIds)
        }
        
        // 计算匹配度
        val matchScores = candidates.map { candidate ->
            matchScoreCalculator.calculateMatchScore(candidate, config)
        }
        
        // 更新任务
        val updatedTask = task.addCandidates(candidates, matchScores)
        updateTask(updatedTask)
        
        // 更新待确认候选人列表
        updatePendingCandidates()
    }
    
    /**
     * 处理自动批准
     */
    private suspend fun processAutoApproval(task: RecruitmentTask) {
        val config = _recruitmentConfigs.value.find { it.id == task.configId } ?: return
        
        if (!config.autoApprove) return
        
        val candidatesToAutoApprove = task.currentCandidates.filter { taskCandidate ->
            taskCandidate.status == CandidateTaskStatus.PENDING &&
            taskCandidate.matchScore >= config.autoApproveThreshold
        }
        
        candidatesToAutoApprove.forEach { taskCandidate ->
            hireCandidateInternal(task.id, taskCandidate.candidate.id, true)
        }
    }
    
    /**
     * 手动刷新任务的候选人搜索
     */
    fun refreshCandidatesForTask(taskId: Int) {
        hrScope.launch {
            val task = _recruitmentTasks.value.find { it.id == taskId }
            if (task != null) {
                searchCandidatesForTask(task)
            }
        }
    }

    /**
     * 雇佣候选人
     */
    fun hireCandidate(taskId: Int, candidateId: Int): Boolean {
        return hireCandidateInternal(taskId, candidateId, false)
    }
    
    /**
     * 内部雇佣候选人方法
     */
    private fun hireCandidateInternal(taskId: Int, candidateId: Int, isAutoApproved: Boolean): Boolean {
        val tasks = _recruitmentTasks.value.toMutableList()
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        
        if (taskIndex == -1) return false
        
        val task = tasks[taskIndex]
        val updatedTask = task.hireCandidate(candidateId, isAutoApproved)
        
        if (updatedTask != task) {
            tasks[taskIndex] = updatedTask
            _recruitmentTasks.value = tasks
            
            // 添加到已雇佣ID集合
            hiredCandidateIds.add(candidateId)
            
            // 更新待确认候选人列表
            updatePendingCandidates()
            
            return true
        }
        
        return false
    }
    
    /**
     * 拒绝候选人
     */
    fun rejectCandidate(taskId: Int, candidateId: Int, reason: String = ""): Boolean {
        val tasks = _recruitmentTasks.value.toMutableList()
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        
        if (taskIndex == -1) return false
        
        val task = tasks[taskIndex]
        val updatedTask = task.rejectCandidate(candidateId, reason)
        
        if (updatedTask != task) {
            tasks[taskIndex] = updatedTask
            _recruitmentTasks.value = tasks
            
            // 更新待确认候选人列表
            updatePendingCandidates()
            
            return true
        }
        
        return false
    }
    
    /**
     * 暂停/恢复招聘任务
     */
    fun toggleTaskStatus(taskId: Int): Boolean {
        val tasks = _recruitmentTasks.value.toMutableList()
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        
        if (taskIndex == -1) return false
        
        val task = tasks[taskIndex]
        val newStatus = when (task.status) {
            RecruitmentTaskStatus.ACTIVE -> RecruitmentTaskStatus.PAUSED
            RecruitmentTaskStatus.PAUSED -> RecruitmentTaskStatus.ACTIVE
            else -> task.status
        }
        
        val updatedTask = task.copy(
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        
        tasks[taskIndex] = updatedTask
        _recruitmentTasks.value = tasks
        
        return true
    }
    
    /**
     * 启用/禁用自动招聘
     */
    fun toggleAutoRecruitment() {
        _isAutoRecruitmentEnabled.value = !_isAutoRecruitmentEnabled.value
    }
    
    /**
     * 更新任务
     */
    private fun updateTask(updatedTask: RecruitmentTask) {
        val tasks = _recruitmentTasks.value.toMutableList()
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        
        if (index != -1) {
            tasks[index] = updatedTask
            _recruitmentTasks.value = tasks
        }
    }
    
    /**
     * 更新待确认候选人列表
     */
    private fun updatePendingCandidates() {
        val allPendingCandidates = _recruitmentTasks.value
            .filter { it.status == RecruitmentTaskStatus.ACTIVE }
            .flatMap { task ->
                task.currentCandidates.filter { it.status == CandidateTaskStatus.PENDING }
                    .map { it.copy() } // 创建副本以避免引用问题
            }
            .sortedByDescending { it.matchScore }
        
        _pendingCandidates.value = allPendingCandidates
    }
    
    /**
     * 更新招聘统计信息
     */
    private fun updateRecruitmentStats() {
        val tasks = _recruitmentTasks.value
        val totalTasks = tasks.size
        val activeTasks = tasks.count { it.status == RecruitmentTaskStatus.ACTIVE }
        val completedTasks = tasks.count { it.status == RecruitmentTaskStatus.COMPLETED }
        val totalHired = tasks.sumOf { it.hiredCount }
        val totalBudgetUsed = tasks.sumOf { it.totalBudgetUsed }
        
        val allCandidates = tasks.flatMap { it.hiredCandidates + it.rejectedCandidates }
        val averageMatchScore = if (allCandidates.isNotEmpty()) {
            allCandidates.map { it.matchScore }.average().toFloat()
        } else {
            0f
        }
        
        val completedTasksWithTime = tasks.filter { it.completedAt != null }
        val averageHiringTime = if (completedTasksWithTime.isNotEmpty()) {
            completedTasksWithTime.map { it.completedAt!! - it.createdAt }.average().toLong()
        } else {
            0L
        }
        
        val successRate = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks.toFloat()) * 100f
        } else {
            0f
        }
        
        _recruitmentStats.value = RecruitmentTaskStats(
            totalTasks = totalTasks,
            activeTasks = activeTasks,
            completedTasks = completedTasks,
            totalHired = totalHired,
            totalBudgetUsed = totalBudgetUsed,
            averageMatchScore = averageMatchScore,
            averageHiringTime = averageHiringTime,
            successRate = successRate
        )
    }
    
    /**
     * 获取任务详细信息
     */
    fun getTaskDetails(taskId: Int): RecruitmentTask? {
        return _recruitmentTasks.value.find { it.id == taskId }
    }
    
    /**
     * 获取配置详细信息
     */
    fun getConfigDetails(configId: Int): RecruitmentConfig? {
        return _recruitmentConfigs.value.find { it.id == configId }
    }
    
    /**
     * 获取候选人匹配度分析
     */
    fun getCandidateMatchAnalysis(candidateId: Int): MatchAnalysis? {
        val allCandidates = _recruitmentTasks.value.flatMap { task ->
            task.currentCandidates.map { taskCandidate ->
                Triple(taskCandidate.candidate, task.configId, taskCandidate.matchScore)
            }
        }
        
        val candidateInfo = allCandidates.find { it.first.id == candidateId } ?: return null
        val config = _recruitmentConfigs.value.find { it.id == candidateInfo.second } ?: return null
        
        return matchScoreCalculator.getDetailedMatchAnalysis(candidateInfo.first, config)
    }
    
    /**
     * 生成配置ID
     */
    private fun generateConfigId(): Int {
        val existingIds = _recruitmentConfigs.value.map { it.id }.toSet()
        var newId = 1
        while (newId in existingIds) {
            newId++
        }
        return newId
    }
    
    /**
     * 生成任务ID
     */
    private fun generateTaskId(): Int {
        val existingIds = _recruitmentTasks.value.map { it.id }.toSet()
        var newId = 1
        while (newId in existingIds) {
            newId++
        }
        return newId
    }
    
    /**
     * 清理已完成的任务（可选）
     */
    fun cleanupCompletedTasks(olderThanDays: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        val tasks = _recruitmentTasks.value.filter { task ->
            !(task.status == RecruitmentTaskStatus.COMPLETED && 
              task.completedAt != null && 
              task.completedAt < cutoffTime)
        }
        _recruitmentTasks.value = tasks
    }
}