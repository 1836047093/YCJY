package com.example.yjcy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.*
import com.example.yjcy.service.JobPostingService
import com.example.yjcy.service.RecruitmentService
import com.example.yjcy.ui.BadgeBox

/**
 * 人才市场对话框 - 岗位发布系统版本
 * 玩家可以发布岗位，查看应聘者，进行面试和雇佣
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTalentMarketDialog(
    saveData: SaveData,
    onDismiss: () -> Unit,
    onRecruitCandidate: (TalentCandidate) -> Unit,
    modifier: Modifier = Modifier,
    jobPostingRefreshTrigger: Int = 0, // 用于触发应聘者数据刷新
    onNavigateToEmployeeManagement: () -> Unit = {} // 导航到员工管理
) {
    val jobPostingService = remember { JobPostingService.getInstance() }
    val recruitmentService = remember { RecruitmentService() }
    
    // 状态管理
    var jobPostings by remember { mutableStateOf(jobPostingService.getAllJobPostings()) }
    var showJobPostingDialog by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<JobPosting?>(null) }
    var showApplicantDialog by remember { mutableStateOf(false) }
    var pendingApplicantsCount by remember { mutableStateOf(jobPostingService.getTotalPendingApplicants()) }
    
    // 监听刷新触发器，实时更新岗位数据
    LaunchedEffect(jobPostingRefreshTrigger) {
        jobPostings = jobPostingService.getAllJobPostings()
        pendingApplicantsCount = jobPostingService.getTotalPendingApplicants()
    }
    
    // 当岗位列表更新时，同步更新待处理应聘者数量
    LaunchedEffect(jobPostings) {
        pendingApplicantsCount = jobPostingService.getTotalPendingApplicants()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部标题栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "人才市场",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "发布岗位，招聘人才",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 发布岗位按钮（不显示红点）
                            IconButton(
                                onClick = { showJobPostingDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "发布岗位",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // 关闭按钮
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 资金显示卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "可用资金",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "¥${String.format("%,d", saveData.money)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 统计信息
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = "活跃岗位",
                                value = jobPostingService.getActiveJobPostings().size.toString(),
                                icon = "📋"
                            )
                            Divider(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            StatItem(
                                label = "待处理应聘",
                                value = pendingApplicantsCount.toString(),
                                icon = "👥"
                            )
                            Divider(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            StatItem(
                                label = "总岗位",
                                value = jobPostings.size.toString(),
                                icon = "📊"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 岗位列表
                    if (jobPostings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "还没有发布岗位",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "点击右上角 + 按钮发布招聘岗位",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(
                                    onClick = { showJobPostingDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("发布岗位")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = jobPostings,
                                key = { it.id }
                            ) { job ->
                                JobPostingCard(
                                    jobPosting = job,
                                    onClick = {
                                        selectedJob = job
                                        showApplicantDialog = true
                                    },
                                    onCloseClick = {
                                        jobPostingService.closeJobPosting(job.id)
                                        jobPostings = jobPostingService.getAllJobPostings()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 岗位发布对话框
    if (showJobPostingDialog) {
        JobPostingDialog(
            onDismiss = { showJobPostingDialog = false },
            onPostingCreated = {
                showJobPostingDialog = false
                jobPostings = jobPostingService.getAllJobPostings()
            }
        )
    }
    
    // 应聘者管理对话框
    if (showApplicantDialog && selectedJob != null) {
        ApplicantManagementDialog(
            jobPosting = selectedJob!!,
            onDismiss = { 
                showApplicantDialog = false
                selectedJob = null
                jobPostings = jobPostingService.getAllJobPostings()
                pendingApplicantsCount = jobPostingService.getTotalPendingApplicants()
            },
            onApplicantHired = { candidate ->
                // 雇佣员工
                onRecruitCandidate(candidate)
                // 刷新岗位列表
                jobPostings = jobPostingService.getAllJobPostings()
                pendingApplicantsCount = jobPostingService.getTotalPendingApplicants()
            }
        )
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 岗位发布卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobPostingCard(
    jobPosting: JobPosting,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (jobPosting.status) {
                JobPostingStatus.ACTIVE -> MaterialTheme.colorScheme.surface
                JobPostingStatus.PAUSED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                JobPostingStatus.CLOSED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                JobPostingStatus.FILLED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部：岗位信息和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = jobPosting.position,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (jobPosting.status) {
                                JobPostingStatus.ACTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                JobPostingStatus.PAUSED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                JobPostingStatus.CLOSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                JobPostingStatus.FILLED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = when (jobPosting.status) {
                                    JobPostingStatus.ACTIVE -> "招聘中"
                                    JobPostingStatus.PAUSED -> "已暂停"
                                    JobPostingStatus.CLOSED -> "已关闭"
                                    JobPostingStatus.FILLED -> "已满员"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${jobPosting.requiredSkillType} Lv.${jobPosting.minSkillLevel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (jobPosting.status == JobPostingStatus.ACTIVE) {
                    IconButton(
                        onClick = { onCloseClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭岗位",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 薪资和应聘者信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 薪资范围
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💰", fontSize = 16.sp)
                        Column {
                            Text(
                                text = "薪资待遇",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "¥${String.format("%,d", jobPosting.minSalary)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // 应聘者数量
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👥", fontSize = 16.sp)
                        Column {
                            Text(
                                text = "应聘者",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${jobPosting.applicants.size} 人",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (jobPosting.getPendingApplicantsCount() > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
            
            // 待处理提示
            if (jobPosting.getPendingApplicantsCount() > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "有 ${jobPosting.getPendingApplicantsCount()} 位应聘者待处理",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

