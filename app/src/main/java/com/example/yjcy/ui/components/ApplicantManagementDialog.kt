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
 * 显示某个岗位的所有应聘者，支持直接雇佣
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantManagementDialog(
    jobPosting: JobPosting,
    saveData: SaveData,
    onDismiss: () -> Unit,
    onApplicantHired: (TalentCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    // 员工人数上限
    val maxEmployees = 30
    val currentEmployeeCount = saveData.allEmployees.size
    val isEmployeeFull = currentEmployeeCount >= maxEmployees
    val jobPostingService = remember { JobPostingService.getInstance() }
    var currentJobPosting by remember { mutableStateOf(jobPosting) }
    
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
                            label = "待雇佣",
                            value = currentJobPosting.getPendingApplicantsCount().toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = "✅",
                            label = "已雇佣",
                            value = currentJobPosting.applicants.count { it.status == ApplicantStatus.HIRED }.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = "👥",
                            label = "总计",
                            value = currentJobPosting.applicants.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 各职位当前人数
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💼 各职位当前人数",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 统计各职位人数
                            val positionCounts = mapOf(
                                "程序员" to saveData.allEmployees.count { it.position == "程序员" },
                                "策划师" to saveData.allEmployees.count { it.position == "策划师" },
                                "美术师" to saveData.allEmployees.count { it.position == "美术师" },
                                "音效师" to saveData.allEmployees.count { it.position == "音效师" },
                                "客服" to saveData.allEmployees.count { it.position == "客服" }
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                positionCounts.forEach { (position, count) ->
                                    PositionCountChip(
                                        position = position,
                                        count = count,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 员工数量信息卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEmployeeFull) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = if (isEmployeeFull) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "当前员工总数",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isEmployeeFull) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$currentEmployeeCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEmployeeFull) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = "/ $maxEmployees",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // 员工已满提示
                    if (isEmployeeFull) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "⚠️ 员工人数已达上限（${maxEmployees}人），无法继续雇佣！",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 应聘者列表（过滤掉已雇佣的）
                    val pendingApplicants = currentJobPosting.applicants.filter { it.status != ApplicantStatus.HIRED }
                    
                    if (pendingApplicants.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (currentJobPosting.applicants.isEmpty()) {
                                        "🔍 暂无应聘者"
                                    } else {
                                        "✅ 所有应聘者已处理完毕"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (currentJobPosting.applicants.isEmpty()) {
                                        "随着时间推进，会有人才来应聘"
                                    } else {
                                        "已成功雇佣的应聘者不再显示"
                                    },
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
                                items = pendingApplicants,
                                key = { it.id }
                            ) { applicant ->
                                ApplicantCard(
                                    applicant = applicant,
                                    isEmployeeFull = isEmployeeFull,
                                    onHireClick = {
                                        // 检查员工人数是否已满
                                        if (isEmployeeFull) {
                                            // 不执行雇佣操作
                                            return@ApplicantCard
                                        }
                                        
                                        // 先将应聘者标记为已通过，然后再雇佣
                                        jobPostingService.updateApplicantStatus(
                                            currentJobPosting.id,
                                            applicant.id,
                                            ApplicantStatus.ACCEPTED
                                        )
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
    isEmployeeFull: Boolean = false,
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
                ApplicantStatus.HIRED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ApplicantStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
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
                        ApplicantStatus.HIRED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ApplicantStatus.REJECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (applicant.status) {
                            ApplicantStatus.HIRED -> "已雇佣"
                            ApplicantStatus.REJECTED -> "已拒绝"
                            else -> "待雇佣"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (applicant.status) {
                            ApplicantStatus.HIRED -> MaterialTheme.colorScheme.primary
                            ApplicantStatus.REJECTED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (applicant.status) {
                    ApplicantStatus.PENDING, ApplicantStatus.REVIEWING, ApplicantStatus.ACCEPTED -> {
                        Button(
                            onClick = onHireClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isEmployeeFull,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = if (isEmployeeFull) Icons.Default.Block else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEmployeeFull) "已满员" else "雇佣")
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

/**
 * 信息芯片组件 - 优化设计
 */
@Composable
private fun InfoChip(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 18.sp
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 职位人数芯片组件
 */
@Composable
private fun PositionCountChip(
    position: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val icon = when (position) {
        "程序员" -> ""
        "策划师" -> ""
        "美术师" -> ""
        "音效师" -> ""
        "客服" -> ""
        else -> ""
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = position,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
