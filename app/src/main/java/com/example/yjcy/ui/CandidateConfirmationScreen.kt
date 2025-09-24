package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.*
import kotlin.math.roundToInt

/**
 * 候选人确认界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateConfirmationScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    hrManager: HRManager = remember { HRManager() }
) {
    val recruitmentTasks by hrManager.recruitmentTasks.collectAsState()
    val recruitmentConfigs by hrManager.recruitmentConfigs.collectAsState()
    
    val currentTask = recruitmentTasks.find { it.id == taskId.toInt() }
    val currentConfig = currentTask?.let { task ->
        recruitmentConfigs.find { it.id == task.configId }
    }
    
    var selectedCandidateId by remember { mutableStateOf<Int?>(null) }
    var showBatchActions by remember { mutableStateOf(false) }
    var selectedCandidates by remember { mutableStateOf(setOf<Int>()) }
    
    if (currentTask == null || currentConfig == null) {
        // 任务不存在或已完成
        EmptyTaskScreen(onNavigateBack = onNavigateBack)
        return
    }
    
    val pendingCandidates = currentTask.currentCandidates.filter { 
        it.status == CandidateTaskStatus.PENDING 
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 顶部栏
        ConfirmationTopBar(
            config = currentConfig,
            task = currentTask,
            pendingCount = pendingCandidates.size,
            onNavigateBack = onNavigateBack,
            showBatchActions = showBatchActions,
            onToggleBatchActions = { showBatchActions = !showBatchActions },
            selectedCount = selectedCandidates.size
        )
        
        if (pendingCandidates.isEmpty()) {
            // 无待确认候选人
            EmptyCandidatesContent(
                task = currentTask,
                config = currentConfig,
                onRefresh = {
                    // 触发新的候选人搜索
                    hrManager.refreshCandidatesForTask(taskId.toInt())
                }
            )
        } else {
            // 候选人列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(pendingCandidates) { candidateTask ->
                    CandidateConfirmationCard(
                        candidateTask = candidateTask,
                        config = currentConfig,
                        isSelected = selectedCandidates.contains(candidateTask.candidate.id),
                        showSelection = showBatchActions,
                        onSelect = {
                            selectedCandidates = if (selectedCandidates.contains(candidateTask.candidate.id)) {
                                selectedCandidates - candidateTask.candidate.id
                            } else {
                                selectedCandidates + candidateTask.candidate.id
                            }
                        },
                        onClick = {
                            if (!showBatchActions) {
                                selectedCandidateId = candidateTask.candidate.id
                            }
                        },
                        onHire = {
                            hrManager.hireCandidate(taskId.toInt(), candidateTask.candidate.id)
                        },
                        onReject = {
                            hrManager.rejectCandidate(taskId.toInt(), candidateTask.candidate.id, "批量拒绝")
                        }
                    )
                }
            }
        }
        
        // 批量操作底部栏
        if (showBatchActions && selectedCandidates.isNotEmpty()) {
            BatchActionsBottomBar(
                selectedCount = selectedCandidates.size,
                onHireAll = {
                    selectedCandidates.forEach { candidateId ->
                        hrManager.hireCandidate(taskId.toInt(), candidateId)
                    }
                    selectedCandidates = emptySet()
                    showBatchActions = false
                },
                onRejectAll = {
                    selectedCandidates.forEach { candidateId ->
                        hrManager.rejectCandidate(taskId.toInt(), candidateId, "批量拒绝")
                    }
                    selectedCandidates = emptySet()
                    showBatchActions = false
                },
                onCancel = {
                    selectedCandidates = emptySet()
                    showBatchActions = false
                }
            )
        }
    }
    
    // 候选人详情对话框
    selectedCandidateId?.let { candidateId ->
        val candidateTask = pendingCandidates.find { it.candidate.id == candidateId }
        candidateTask?.let {
            CandidateDetailDialog(
                candidateTask = it,
                config = currentConfig,
                onDismiss = { selectedCandidateId = null },
                onHire = {
                    hrManager.hireCandidate(taskId.toInt(), candidateId)
                    selectedCandidateId = null
                },
                onReject = {
                    hrManager.rejectCandidate(taskId.toInt(), candidateId, "手动拒绝")
                    selectedCandidateId = null
                }
            )
        }
    }
}

/**
 * 确认界面顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationTopBar(
    config: RecruitmentConfig,
    task: RecruitmentTask,
    pendingCount: Int,
    onNavigateBack: () -> Unit,
    showBatchActions: Boolean,
    onToggleBatchActions: () -> Unit,
    selectedCount: Int
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = if (showBatchActions && selectedCount > 0) {
                        "已选择 $selectedCount 人"
                    } else {
                        "${config.positionType} - 候选人确认"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (!showBatchActions) {
                    Text(
                        text = "待确认: $pendingCount 人 | 已招聘: ${task.hiredCount}/${task.targetCount} 人",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = {
            if (pendingCount > 1) {
                IconButton(onClick = onToggleBatchActions) {
                    Icon(
                        imageVector = if (showBatchActions) Icons.Default.Close else Icons.Default.Checklist,
                        contentDescription = if (showBatchActions) "取消批量操作" else "批量操作"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

/**
 * 候选人确认卡片
 */
@Composable
fun CandidateConfirmationCard(
    candidateTask: TaskCandidate,
    config: RecruitmentConfig,
    isSelected: Boolean,
    showSelection: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit,
    onHire: () -> Unit,
    onReject: () -> Unit
) {
    val candidate = candidateTask.candidate
    val matchScore = candidateTask.matchScore
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (showSelection) onSelect() else onClick() 
            }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        Color(0xFF2196F3),
                        RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF3F9FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 候选人基本信息
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showSelection) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onSelect() },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        
                        Text(
                            text = candidate.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // 匹配度标签
                        Surface(
                            color = when {
                                matchScore >= 0.9f -> Color(0xFF4CAF50)
                                matchScore >= 0.7f -> Color(0xFF2196F3)
                                matchScore >= 0.5f -> Color(0xFFFF9800)
                                else -> Color(0xFFE57373)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${(matchScore * 100).roundToInt()}%",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${22 + candidate.experienceYears}岁 | ${candidate.experienceYears}年经验 | ${candidate.expectedSalary}万/年",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                // 可用状态
                Surface(
                    color = when (candidate.availabilityStatus) {
                        AvailabilityStatus.AVAILABLE -> Color(0xFF4CAF50)
                        AvailabilityStatus.INTERVIEWING -> Color(0xFFFF9800)
                        AvailabilityStatus.HIRED -> Color(0xFFE57373)
                        else -> Color(0xFF9E9E9E)
                    },
                    shape = CircleShape
                ) {
                    Text(
                        text = when (candidate.availabilityStatus) {
                            AvailabilityStatus.AVAILABLE -> "可用"
                            AvailabilityStatus.INTERVIEWING -> "面试中"
                            AvailabilityStatus.HIRED -> "已雇佣"
                            else -> "未知"
                        },
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 技能标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${candidate.getSpecialtySkillType()} Lv.${candidate.getSpecialtySkillLevel()}",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Surface(
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = candidate.educationLevel,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 评分指标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreIndicator(
                    label = "技能等级",
                    score = candidate.getSpecialtySkillLevel(),
                    maxScore = 10,
                    color = Color(0xFF2196F3)
                )
                
                ScoreIndicator(
                    label = "综合评分",
                    score = candidate.getOverallRating().toInt(),
                    maxScore = 10,
                    color = Color(0xFF4CAF50)
                )
                
                ScoreIndicator(
                    label = "招聘概率",
                    score = (candidate.calculateRecruitmentProbability() * 10).roundToInt(),
                    maxScore = 10,
                    color = Color(0xFFFF9800)
                )
            }
            
            if (!showSelection) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE57373)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "拒绝",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("拒绝")
                    }
                    
                    Button(
                        onClick = onHire,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "录用",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("录用")
                    }
                }
            }
        }
    }
}

/**
 * 评分指标
 */
@Composable
fun ScoreIndicator(
    label: String,
    score: Int,
    maxScore: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$score/$maxScore",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

/**
 * 批量操作底部栏
 */
@Composable
fun BatchActionsBottomBar(
    selectedCount: Int,
    onHireAll: () -> Unit,
    onRejectAll: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
            
            OutlinedButton(
                onClick = onRejectAll,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE57373)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "全部拒绝",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("全部拒绝")
            }
            
            Button(
                onClick = onHireAll,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "全部录用",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("全部录用")
            }
        }
    }
}

/**
 * 空任务界面
 */
@Composable
fun EmptyTaskScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "任务不存在",
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "任务不存在或已完成",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Text("返回")
        }
    }
}

/**
 * 无候选人内容
 */
@Composable
fun EmptyCandidatesContent(
    task: RecruitmentTask,
    config: RecruitmentConfig,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonSearch,
            contentDescription = "无候选人",
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "暂无待确认的候选人",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        
        Text(
            text = "系统正在为您寻找合适的${config.positionType}候选人",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "刷新",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("立即搜索")
        }
    }
}

/**
 * 候选人详情对话框
 */
@Composable
fun CandidateDetailDialog(
    candidateTask: TaskCandidate,
    config: RecruitmentConfig,
    onDismiss: () -> Unit,
    onHire: () -> Unit,
    onReject: () -> Unit
) {
    val candidate = candidateTask.candidate
    val matchScore = candidateTask.matchScore
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "候选人详情",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 候选人信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = candidate.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        
                        Text(
                            text = "${22 + candidate.experienceYears}岁 | ${candidate.experienceYears}年经验",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        
                        Text(
                            text = "期望薪资: ${candidate.expectedSalary}万/年",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    
                    // 匹配度
                    Surface(
                        color = when {
                            matchScore >= 0.9f -> Color(0xFF4CAF50)
                            matchScore >= 0.7f -> Color(0xFF2196F3)
                            matchScore >= 0.5f -> Color(0xFFFF9800)
                            else -> Color(0xFFE57373)
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${(matchScore * 100).roundToInt()}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "匹配度",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 技能列表
                Text(
                    text = "技能专长",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${candidate.getSpecialtySkillType()} Lv.${candidate.getSpecialtySkillLevel()}",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    Surface(
                        color = Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = candidate.educationLevel,
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 详细评分
                Text(
                    text = "能力评估",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreIndicator(
                        label = "技能等级",
                        score = candidate.getSpecialtySkillLevel(),
                        maxScore = 10,
                        color = Color(0xFF2196F3)
                    )
                    
                    ScoreIndicator(
                        label = "综合评分",
                        score = candidate.getOverallRating().toInt(),
                        maxScore = 10,
                        color = Color(0xFF4CAF50)
                    )
                    
                    ScoreIndicator(
                        label = "招聘概率",
                        score = (candidate.calculateRecruitmentProbability() * 10).roundToInt(),
                        maxScore = 10,
                        color = Color(0xFFFF9800)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE57373)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "拒绝",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拒绝")
                    }
                    
                    Button(
                        onClick = onHire,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "录用",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("录用")
                    }
                }
            }
        }
    }
}