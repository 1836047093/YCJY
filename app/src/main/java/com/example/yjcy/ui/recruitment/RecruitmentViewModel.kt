package com.example.yjcy.ui.recruitment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yjcy.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 招聘系统ViewModel
 * 负责招聘相关的UI状态管理和业务逻辑
 */
@HiltViewModel
class RecruitmentViewModel @Inject constructor(
    private val recruitmentRepository: RecruitmentRepository
) : ViewModel() {
    
    // ==================== UI状态 ====================
    
    private val _uiState = MutableStateFlow(RecruitmentUiState())
    val uiState: StateFlow<RecruitmentUiState> = _uiState.asStateFlow()
    
    private val _recruitmentOverview = MutableStateFlow<RecruitmentOverview?>(null)
    val recruitmentOverview: StateFlow<RecruitmentOverview?> = _recruitmentOverview.asStateFlow()
    
    private val _recruitmentStats = MutableStateFlow<RecruitmentStats?>(null)
    val recruitmentStats: StateFlow<RecruitmentStats?> = _recruitmentStats.asStateFlow()
    
    // ==================== 数据流 ====================
    
    val activeRecruitments: StateFlow<List<Recruitment>> = recruitmentRepository
        .getActiveRecruitments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val allRecruitments: StateFlow<List<Recruitment>> = recruitmentRepository
        .getAllRecruitments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val pendingCandidates: StateFlow<List<Candidate>> = recruitmentRepository
        .getAllPendingCandidates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadRecruitmentOverview()
        loadRecruitmentStats()
        checkExpiredRecruitments()
    }
    
    // ==================== 招聘任务操作 ====================
    
    /**
     * 创建新的招聘任务
     */
    fun createRecruitment(
        position: RecruitmentPosition,
        skillLevel: Int,
        salary: Int,
        duration: RecruitmentDuration
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            recruitmentRepository.createRecruitment(position, skillLevel, salary, duration)
                .onSuccess { recruitmentId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "招聘任务创建成功"
                    )
                    loadRecruitmentOverview()
                    loadRecruitmentStats()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "创建招聘任务失败"
                    )
                }
        }
    }
    
    /**
     * 取消招聘任务
     */
    fun cancelRecruitment(recruitmentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            recruitmentRepository.cancelRecruitment(recruitmentId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "招聘任务已取消"
                    )
                    loadRecruitmentOverview()
                    loadRecruitmentStats()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "取消招聘任务失败"
                    )
                }
        }
    }
    
    /**
     * 为招聘任务生成候选人
     */
    fun generateCandidates(recruitmentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            recruitmentRepository.generateCandidatesForRecruitment(recruitmentId)
                .onSuccess { candidates ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = if (candidates.isNotEmpty()) {
                            "生成了 ${candidates.size} 个候选人"
                        } else {
                            "暂时没有合适的候选人"
                        }
                    )
                    loadRecruitmentOverview()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "生成候选人失败"
                    )
                }
        }
    }
    
    // ==================== 候选人操作 ====================
    
    /**
     * 聘用候选人
     */
    fun hireCandidate(candidateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            recruitmentRepository.hireCandidate(candidateId)
                .onSuccess { employee ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "成功聘用 ${employee.name}"
                    )
                    loadRecruitmentOverview()
                    loadRecruitmentStats()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "聘用候选人失败"
                    )
                }
        }
    }
    
    /**
     * 拒绝候选人
     */
    fun rejectCandidate(candidateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            recruitmentRepository.rejectCandidate(candidateId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "已拒绝该候选人"
                    )
                    loadRecruitmentOverview()
                    loadRecruitmentStats()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "拒绝候选人失败"
                    )
                }
        }
    }
    
    /**
     * 获取指定招聘任务的候选人
     */
    fun getCandidatesForRecruitment(recruitmentId: String): StateFlow<List<Candidate>> {
        return recruitmentRepository.getCandidatesByRecruitmentId(recruitmentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
    
    /**
     * 获取指定招聘任务的待处理候选人
     */
    fun getPendingCandidatesForRecruitment(recruitmentId: String): StateFlow<List<Candidate>> {
        return recruitmentRepository.getPendingCandidatesByRecruitmentId(recruitmentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
    
    // ==================== 数据加载 ====================
    
    /**
     * 加载招聘概览数据
     */
    private fun loadRecruitmentOverview() {
        viewModelScope.launch {
            recruitmentRepository.getRecruitmentOverview()
                .collect { overview ->
                    _recruitmentOverview.value = overview
                }
        }
    }
    
    /**
     * 加载招聘统计数据
     */
    private fun loadRecruitmentStats() {
        viewModelScope.launch {
            recruitmentRepository.getRecruitmentStats()
                .onSuccess { stats ->
                    _recruitmentStats.value = stats
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "加载统计数据失败: ${error.message}"
                    )
                }
        }
    }
    
    /**
     * 检查并更新过期的招聘任务
     */
    private fun checkExpiredRecruitments() {
        viewModelScope.launch {
            recruitmentRepository.updateExpiredRecruitments()
                .onSuccess { expiredCount ->
                    if (expiredCount > 0) {
                        _uiState.value = _uiState.value.copy(
                            message = "已更新 $expiredCount 个过期的招聘任务"
                        )
                        loadRecruitmentOverview()
                        loadRecruitmentStats()
                    }
                }
        }
    }
    
    /**
     * 刷新数据
     */
    fun refreshData() {
        loadRecruitmentOverview()
        loadRecruitmentStats()
        checkExpiredRecruitments()
    }
    
    /**
     * 清理过期数据
     */
    fun cleanupExpiredData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            recruitmentRepository.cleanupExpiredData()
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = result.message
                    )
                    refreshData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "数据清理失败"
                    )
                }
        }
    }
    
    // ==================== UI状态管理 ====================
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    /**
     * 设置加载状态
     */
    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }
}

/**
 * 招聘UI状态
 */
data class RecruitmentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

/**
 * 招聘设置ViewModel
 * 专门处理招聘设置页面的状态
 */
@HiltViewModel
class RecruitmentSettingsViewModel @Inject constructor(
    private val recruitmentRepository: RecruitmentRepository
) : ViewModel() {
    
    private val _settingsState = MutableStateFlow(RecruitmentSettingsState())
    val settingsState: StateFlow<RecruitmentSettingsState> = _settingsState.asStateFlow()
    
    /**
     * 更新选择的职位
     */
    fun updatePosition(position: RecruitmentPosition) {
        _settingsState.value = _settingsState.value.copy(selectedPosition = position)
    }
    
    /**
     * 更新技能等级
     */
    fun updateSkillLevel(skillLevel: Int) {
        _settingsState.value = _settingsState.value.copy(selectedSkillLevel = skillLevel)
    }
    
    /**
     * 更新薪资
     */
    fun updateSalary(salary: Int) {
        _settingsState.value = _settingsState.value.copy(selectedSalary = salary)
    }
    
    /**
     * 更新招聘天数
     */
    fun updateDuration(duration: RecruitmentDuration) {
        _settingsState.value = _settingsState.value.copy(selectedDuration = duration)
    }
    
    /**
     * 验证设置是否有效
     */
    fun validateSettings(): Boolean {
        val state = _settingsState.value
        return state.selectedPosition != null &&
                state.selectedSkillLevel in 1..5 &&
                state.selectedSalary > 0 &&
                state.selectedDuration != null
    }
    
    /**
     * 重置设置
     */
    fun resetSettings() {
        _settingsState.value = RecruitmentSettingsState()
    }
    
    /**
     * 获取当前设置
     */
    fun getCurrentSettings(): RecruitmentSettingsState {
        return _settingsState.value
    }
}

/**
 * 招聘设置状态
 */
data class RecruitmentSettingsState(
    val selectedPosition: RecruitmentPosition? = null,
    val selectedSkillLevel: Int = 3,
    val selectedSalary: Int = 8000,
    val selectedDuration: RecruitmentDuration? = null,
    val isValid: Boolean = false
) {
    val isComplete: Boolean
        get() = selectedPosition != null && selectedDuration != null
    
    // 计算预估候选人数量
    val estimatedCandidates: Int
        get() = when (selectedDuration) {
            RecruitmentDuration.SHORT -> 3 + (selectedSkillLevel / 2)
            RecruitmentDuration.MEDIUM -> 5 + selectedSkillLevel
            RecruitmentDuration.LONG -> 8 + (selectedSkillLevel * 2)
            null -> 0
        }
    
    // 计算总预算
    val totalBudget: Int
        get() = when (selectedDuration) {
            RecruitmentDuration.SHORT -> selectedSalary * 1
            RecruitmentDuration.MEDIUM -> selectedSalary * 2
            RecruitmentDuration.LONG -> selectedSalary * 3
            null -> 0
        }
    
    // 计算招聘成功率
    val successRate: Int
        get() = when {
            selectedPosition == null || selectedDuration == null -> 0
            selectedSalary >= 15000 -> 85 + (selectedSkillLevel * 2)
            selectedSalary >= 10000 -> 70 + selectedSkillLevel
            selectedSalary >= 6000 -> 55 + selectedSkillLevel
            else -> 40 + selectedSkillLevel
        }.coerceIn(0, 95)
}

/**
 * 候选人详情ViewModel
 * 专门处理候选人详情页面的状态
 */
@HiltViewModel
class CandidateDetailViewModel @Inject constructor(
    private val recruitmentRepository: RecruitmentRepository
) : ViewModel() {
    
    private val _candidateState = MutableStateFlow<CandidateDetailState>(CandidateDetailState.Loading)
    val candidateState: StateFlow<CandidateDetailState> = _candidateState.asStateFlow()
    
    /**
     * 加载候选人详情
     */
    fun loadCandidate(candidateId: String) {
        viewModelScope.launch {
            _candidateState.value = CandidateDetailState.Loading
            
            try {
                val candidate = recruitmentRepository.getCandidateById(candidateId)
                if (candidate != null) {
                    _candidateState.value = CandidateDetailState.Success(candidate)
                } else {
                    _candidateState.value = CandidateDetailState.Error("候选人不存在")
                }
            } catch (e: Exception) {
                _candidateState.value = CandidateDetailState.Error(e.message ?: "加载候选人信息失败")
            }
        }
    }
}

/**
 * 候选人详情状态
 */
sealed class CandidateDetailState {
    object Loading : CandidateDetailState()
    data class Success(val candidate: Candidate) : CandidateDetailState()
    data class Error(val message: String) : CandidateDetailState()
}