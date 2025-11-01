package com.example.yjcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.*
import com.example.yjcy.service.JobPostingService
import com.example.yjcy.utils.formatMoney
import java.util.Locale

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
    onPauseGame: (() -> Unit)? = null, // 暂停游戏的回调
    onResumeGame: (() -> Unit)? = null // 恢复游戏的回调
) {
    val jobPostingService = remember { JobPostingService.getInstance() }
    
    // 状态管理
    var jobPostings by remember { mutableStateOf(jobPostingService.getAllJobPostings().filter { it.status != JobPostingStatus.CLOSED }) }
    var showJobPostingDialog by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<JobPosting?>(null) }
    var showApplicantDialog by remember { mutableStateOf(false) }
    
    // 监听对话框打开/关闭，控制游戏暂停
    DisposableEffect(Unit) {
        onPauseGame?.invoke() // 对话框打开时暂停游戏
        onDispose {
            onResumeGame?.invoke() // 对话框关闭时恢复游戏
        }
    }
    
    // 监听刷新触发器，实时更新岗位数据
    LaunchedEffect(jobPostingRefreshTrigger) {
        jobPostings = jobPostingService.getAllJobPostings().filter { it.status != JobPostingStatus.CLOSED }
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
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B) // 深色背景
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部标题栏 - 渐变背景
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6).copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.People,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "人才市场",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 发布岗位按钮 - 现代化样式
                            FilledTonalButton(
                                onClick = { showJobPostingDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "发布岗位",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("发布", fontWeight = FontWeight.Bold)
                            }
                            
                            // 关闭按钮
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .background(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "关闭",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // 资金显示卡片 - 现代化设计
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF10B981).copy(alpha = 0.3f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "可用资金",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "¥${formatMoney(saveData.money)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 岗位列表标题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "招聘岗位 (${jobPostings.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 岗位列表
                    if (jobPostings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WorkOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFF60A5FA)
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "还没有发布岗位",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "点击上方\"发布\"按钮开始招聘",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = jobPostings,
                                key = { it.id }
                            ) { job ->
                                ModernJobPostingCard(
                                    jobPosting = job,
                                    onClick = {
                                        selectedJob = job
                                        showApplicantDialog = true
                                    },
                                    onCloseClick = {
                                        jobPostingService.closeJobPosting(job.id)
                                        jobPostings = jobPostingService.getAllJobPostings().filter { it.status != JobPostingStatus.CLOSED }
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
            onDismiss = { 
                showJobPostingDialog = false 
            },
            onPostingCreated = {
                showJobPostingDialog = false
                jobPostings = jobPostingService.getAllJobPostings().filter { it.status != JobPostingStatus.CLOSED }
            }
        )
    }
    
    // 应聘者管理对话框
    if (showApplicantDialog && selectedJob != null) {
        ApplicantManagementDialog(
            jobPosting = selectedJob!!,
            saveData = saveData,  // 直接使用最新的 saveData
            onDismiss = { 
                showApplicantDialog = false
                selectedJob = null
                jobPostings = jobPostingService.getAllJobPostings().filter { it.status != JobPostingStatus.CLOSED }
            },
            onApplicantHired = { candidate ->
                try {
                    // 验证候选人数据
                    if (candidate.name.isBlank()) {
                        android.util.Log.e("NewTalentMarket", "候选人姓名为空")
                        return@ApplicantManagementDialog
                    }
                    
                    // 雇佣员工
                    android.util.Log.d("NewTalentMarket", "准备调用 onRecruitCandidate 回调")
                    onRecruitCandidate(candidate)
                    android.util.Log.d("NewTalentMarket", "onRecruitCandidate 回调执行完成")
                    
                    // 刷新岗位列表
                    android.util.Log.d("NewTalentMarket", "准备刷新岗位列表")
                    try {
                        jobPostings = jobPostingService.getAllJobPostings().filter { it.status != JobPostingStatus.CLOSED }
                        android.util.Log.d("NewTalentMarket", "岗位列表刷新完成: ${jobPostings.size} 个岗位")
                    } catch (e: Exception) {
                        android.util.Log.e("NewTalentMarket", "刷新岗位列表失败", e)
                    }
                    
                    android.util.Log.d("NewTalentMarket", "成功处理雇佣回调: ${candidate.name}")
                } catch (e: Exception) {
                    // 捕获所有异常，防止崩溃
                    android.util.Log.e("NewTalentMarket", "处理雇佣回调时发生异常", e)
                    e.printStackTrace()
                }
            }
        )
    }
}

/**
 * 现代化岗位卡片 - 全新设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernJobPostingCard(
    jobPosting: JobPosting,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 岗位图标映射
    val positionIcons = mapOf(
        "程序员" to "💻",
        "策划师" to "📋",
        "美术师" to "🎨",
        "音效师" to "🎵",
        "客服" to "💬"
    )
    
    val positionColor = when (jobPosting.position) {
        "程序员" -> Color(0xFF3B82F6)
        "策划师" -> Color(0xFF10B981)
        "美术师" -> Color(0xFFF59E0B)
        "音效师" -> Color(0xFF8B5CF6)
        "客服" -> Color(0xFFEC4899)
        else -> Color(0xFF6B7280)
    }
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            positionColor.copy(alpha = if (jobPosting.status == JobPostingStatus.ACTIVE) 0.3f else 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 顶部：岗位信息和图标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 岗位图标
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        positionColor.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = positionIcons[jobPosting.position] ?: "💼",
                            fontSize = 24.sp
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = jobPosting.position,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            // 状态标签
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (jobPosting.status) {
                                    JobPostingStatus.ACTIVE -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    JobPostingStatus.PAUSED -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    JobPostingStatus.CLOSED -> Color(0xFF6B7280).copy(alpha = 0.2f)
                                    JobPostingStatus.FILLED -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = when (jobPosting.status) {
                                        JobPostingStatus.ACTIVE -> "招聘中"
                                        JobPostingStatus.PAUSED -> "已暂停"
                                        JobPostingStatus.CLOSED -> "已关闭"
                                        JobPostingStatus.FILLED -> "已满员"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (jobPosting.status) {
                                        JobPostingStatus.ACTIVE -> Color(0xFF10B981)
                                        JobPostingStatus.PAUSED -> Color(0xFFF59E0B)
                                        JobPostingStatus.CLOSED -> Color(0xFF6B7280)
                                        JobPostingStatus.FILLED -> Color(0xFF3B82F6)
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${jobPosting.requiredSkillType} Lv.${jobPosting.minSkillLevel}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                
                if (jobPosting.status == JobPostingStatus.ACTIVE) {
                    IconButton(
                        onClick = { onCloseClick() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "关闭岗位",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 薪资和应聘者信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 薪资卡片
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "💰 薪资",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${formatMoney(jobPosting.minSalary.toLong())}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                // 应聘者卡片
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "👥 应聘者",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${jobPosting.applicants.size} 人",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (jobPosting.getPendingApplicantsCount() > 0) {
                                Color(0xFF10B981)
                            } else {
                                Color.White
                            }
                        )
                    }
                }
            }
            
            // 待处理提示
            if (jobPosting.getPendingApplicantsCount() > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "有 ${jobPosting.getPendingApplicantsCount()} 位应聘者待处理",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}


