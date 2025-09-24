package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yjcy.data.*


/**
 * 人事中心主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRCenterScreen(
    onNavigateToConfig: () -> Unit = {},
    onNavigateToConfirmation: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    hrManager: HRManager = remember { HRManager() }
) {
    val recruitmentConfigs by hrManager.recruitmentConfigs.collectAsState()
    val recruitmentTasks by hrManager.recruitmentTasks.collectAsState()
    val pendingCandidates by hrManager.pendingCandidates.collectAsState()
    val recruitmentStats by hrManager.recruitmentStats.collectAsState()
    val isAutoRecruitmentEnabled by hrManager.isAutoRecruitmentEnabled.collectAsState()
    
    var showTaskDetails by remember { mutableStateOf<RecruitmentTask?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // 标题栏
        HRCenterHeader(
            onNavigateBack = onNavigateBack
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 快速操作卡片
        QuickActionCards(
            pendingCount = pendingCandidates.size,
            activeTasksCount = recruitmentTasks.count { it.status == RecruitmentTaskStatus.ACTIVE },
            onNavigateToConfig = onNavigateToConfig,
            onNavigateToConfirmation = onNavigateToConfirmation,
            onNavigateToHistory = onNavigateToHistory
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 招聘任务列表
        Text(
            text = "招聘任务",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recruitmentTasks.filter { it.status != RecruitmentTaskStatus.CANCELLED }) { task ->
                RecruitmentTaskCard(
                    task = task,
                    config = recruitmentConfigs.find { it.id == task.configId },
                    onTaskClick = { showTaskDetails = task },
                    onToggleStatus = { hrManager.toggleTaskStatus(task.id) }
                )
            }
            
            if (recruitmentTasks.isEmpty()) {
                item {
                    EmptyTasksCard(onNavigateToConfig = onNavigateToConfig)
                }
            }
        }
    }
    
    // 任务详情对话框
    showTaskDetails?.let { task ->
        TaskDetailsDialog(
            task = task,
            config = recruitmentConfigs.find { it.id == task.configId },
            onDismiss = { showTaskDetails = null },
            onHireCandidate = { candidateId ->
                hrManager.hireCandidate(task.id, candidateId)
            },
            onRejectCandidate = { candidateId, reason ->
                hrManager.rejectCandidate(task.id, candidateId, reason)
            }
        )
    }
}

/**
 * 人事中心标题栏
 */
@Composable
fun HRCenterHeader(
    onNavigateBack: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "人事中心",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 统计项目
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 快速操作卡片
 */
@Composable
fun QuickActionCards(
    pendingCount: Int,
    activeTasksCount: Int,
    onNavigateToConfig: () -> Unit,
    onNavigateToConfirmation: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            modifier = Modifier.weight(1f),
            title = "配置招聘",
            subtitle = "${activeTasksCount}个活跃任务",
            icon = Icons.Default.Settings,
            color = Color(0xFF2196F3),
            onClick = onNavigateToConfig
        )
        
        QuickActionCard(
            modifier = Modifier.weight(1f),
            title = "待确认",
            subtitle = "${pendingCount}个候选人",
            icon = Icons.Default.Person,
            color = Color(0xFFFF9800),
            onClick = onNavigateToConfirmation,
            showBadge = pendingCount > 0
        )
        
        QuickActionCard(
            modifier = Modifier.weight(1f),
            title = "招聘历史",
            subtitle = "查看记录",
            icon = Icons.Default.History,
            color = Color(0xFF4CAF50),
            onClick = onNavigateToHistory
        )
    }
}

/**
 * 快速操作卡片
 */
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    if (showBadge) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    Color.Red,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .align(Alignment.TopEnd)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 招聘任务卡片
 */
@Composable
fun RecruitmentTaskCard(
    task: RecruitmentTask,
    config: RecruitmentConfig?,
    onTaskClick: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config?.positionType ?: "未知职位",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    
                    Text(
                        text = "目标: ${task.hiredCount}/${task.targetCount} | 进度: ${task.getProgressPercentage().toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskStatusChip(status = task.status)
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (task.status == RecruitmentTaskStatus.ACTIVE) 
                                Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "切换状态",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = task.getProgressPercentage() / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    task.getProgressPercentage() >= 100f -> Color(0xFF4CAF50)
                    task.getProgressPercentage() >= 50f -> Color(0xFFFF9800)
                    else -> Color(0xFF2196F3)
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 候选人信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "待确认: ${task.getPendingCandidatesCount()}",
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800)
                )
                
                Text(
                    text = "已用预算: ${task.totalBudgetUsed}万",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 任务状态标签
 */
@Composable
fun TaskStatusChip(status: RecruitmentTaskStatus) {
    val (text, color) = when (status) {
        RecruitmentTaskStatus.ACTIVE -> "进行中" to Color(0xFF4CAF50)
        RecruitmentTaskStatus.PAUSED -> "已暂停" to Color(0xFFFF9800)
        RecruitmentTaskStatus.COMPLETED -> "已完成" to Color(0xFF2196F3)
        RecruitmentTaskStatus.CANCELLED -> "已取消" to Color(0xFF757575)
    }
    
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 空任务卡片
 */
@Composable
fun EmptyTasksCard(onNavigateToConfig: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = "无任务",
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "暂无招聘任务",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "点击下方按钮创建第一个招聘配置",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToConfig,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "创建招聘配置",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 任务详情对话框
 */
@Composable
fun TaskDetailsDialog(
    task: RecruitmentTask,
    config: RecruitmentConfig?,
    onDismiss: () -> Unit,
    onHireCandidate: (Int) -> Unit,
    onRejectCandidate: (Int, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "任务详情 - ${config?.positionType ?: "未知职位"}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                item {
                    Column {
                        Text(
                            text = "基本信息",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("状态: ${task.status}", fontSize = 12.sp)
                        Text("进度: ${task.hiredCount}/${task.targetCount}", fontSize = 12.sp)
                        Text("预算: ${task.totalBudgetUsed}万", fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "待确认候选人 (${task.getPendingCandidatesCount()})",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                items(task.currentCandidates.filter { it.status == CandidateTaskStatus.PENDING }) { taskCandidate ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = taskCandidate.candidate.name,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "匹配度: ${(taskCandidate.matchScore * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "薪资: ${taskCandidate.candidate.expectedSalary}万",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row {
                                Button(
                                    onClick = { onHireCandidate(taskCandidate.candidate.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    )
                                ) {
                                    Text("雇佣", fontSize = 12.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                OutlinedButton(
                                    onClick = { onRejectCandidate(taskCandidate.candidate.id, "手动拒绝") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("拒绝", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
            Text(
                text = "关闭",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        }
    )
}