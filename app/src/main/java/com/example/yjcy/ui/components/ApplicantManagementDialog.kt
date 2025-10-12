package com.example.yjcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.*
import com.example.yjcy.service.JobPostingService

/**
 * 应聘者管理对话框
 * 显示某个岗位的所有应聘者，支持面试操作
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantManagementDialog(
    jobPosting: JobPosting,
    onDismiss: () -> Unit,
    onApplicantHired: (TalentCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    val jobPostingService = remember { JobPostingService.getInstance() }
    var currentJobPosting by remember { mutableStateOf(jobPosting) }
    var selectedApplicant by remember { mutableStateOf<JobApplicant?>(null) }
    var showInterviewDialog by remember { mutableStateOf(false) }
    
    // 定期更新岗位信息
    LaunchedEffect(jobPosting.id) {
        currentJobPosting = jobPostingService.getJobPosting(jobPosting.id) ?: jobPosting
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
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "应聘者管理",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentJobPosting.getDescription(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 统计信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = "📋",
                            label = "待处理",
                            value = currentJobPosting.getPendingApplicantsCount().toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = "✅",
                            label = "已通过",
                            value = currentJobPosting.getAcceptedApplicantsCount().toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = "👥",
                            label = "总计",
                            value = currentJobPosting.applicants.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 应聘者列表
                    if (currentJobPosting.applicants.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🔍 暂无应聘者",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "随着时间推进，会有人才来应聘",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = currentJobPosting.applicants,
                                key = { it.id }
                            ) { applicant ->
                                ApplicantCard(
                                    applicant = applicant,
                                    onInterviewClick = {
                                        selectedApplicant = applicant
                                        showInterviewDialog = true
                                    },
                                    onHireClick = {
                                        val candidate = jobPostingService.hireApplicant(
                                            currentJobPosting.id,
                                            applicant.id
                                        )
                                        if (candidate != null) {
                                            onApplicantHired(candidate)
                                            currentJobPosting = jobPostingService.getJobPosting(currentJobPosting.id) ?: currentJobPosting
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 面试对话框
    if (showInterviewDialog && selectedApplicant != null) {
        InterviewDialog(
            applicant = selectedApplicant!!,
            jobPosting = currentJobPosting,
            onDismiss = { 
                showInterviewDialog = false
                selectedApplicant = null
            },
            onInterviewComplete = { 
                currentJobPosting = jobPostingService.getJobPosting(currentJobPosting.id) ?: currentJobPosting
                showInterviewDialog = false
                selectedApplicant = null
            }
        )
    }
}

/**
 * 统计卡片
 */
@Composable
private fun StatCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 应聘者卡片
 */
@Composable
private fun ApplicantCard(
    applicant: JobApplicant,
    onInterviewClick: () -> Unit,
    onHireClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val candidate = applicant.candidate
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (applicant.status) {
                ApplicantStatus.ACCEPTED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ApplicantStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ApplicantStatus.HIRED -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 候选人基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 头像
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = candidate.position,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 状态标签
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (applicant.status) {
                        ApplicantStatus.PENDING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ApplicantStatus.REVIEWING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        ApplicantStatus.INTERVIEWING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ApplicantStatus.ACCEPTED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ApplicantStatus.REJECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        ApplicantStatus.HIRED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    }
                ) {
                    Text(
                        text = when (applicant.status) {
                            ApplicantStatus.PENDING -> "待处理"
                            ApplicantStatus.REVIEWING -> "审核中"
                            ApplicantStatus.INTERVIEWING -> "面试中"
                            ApplicantStatus.ACCEPTED -> "已通过"
                            ApplicantStatus.REJECTED -> "已拒绝"
                            ApplicantStatus.HIRED -> "已雇佣"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (applicant.status) {
                            ApplicantStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
                            ApplicantStatus.REJECTED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 技能和薪资信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(
                    label = "最高技能",
                    value = "Lv.${candidate.getMaxSkillLevel()}",
                    icon = "⭐",
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    label = "期望薪资",
                    value = "¥${candidate.expectedSalary}",
                    icon = "💰",
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    label = "工作经验",
                    value = "${candidate.experience}年",
                    icon = "💼",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 面试结果（如果已面试）
            applicant.interviewScore?.let { score ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "面试评分",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$score 分",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    score >= 80 -> MaterialTheme.colorScheme.primary
                                    score >= 60 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                        
                        applicant.interviewNotes?.let { notes ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (applicant.status) {
                    ApplicantStatus.PENDING, ApplicantStatus.REVIEWING -> {
                        Button(
                            onClick = onInterviewClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("面试")
                        }
                    }
                    ApplicantStatus.ACCEPTED -> {
                        Button(
                            onClick = onHireClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("雇佣")
                        }
                    }
                    ApplicantStatus.REJECTED -> {
                        Text(
                            text = "已拒绝此应聘者",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    ApplicantStatus.HIRED -> {
                        Text(
                            text = "✅ 已成功雇佣",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

