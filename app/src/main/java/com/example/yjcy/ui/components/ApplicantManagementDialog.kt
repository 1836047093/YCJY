package com.example.yjcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.*
import com.example.yjcy.service.JobPostingService
import com.example.yjcy.service.RecruitmentService
import com.example.yjcy.utils.formatMoney

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
    // 获取服务实例
    val recruitmentService = remember { RecruitmentService.getInstance() }
    val jobPostingService = remember { JobPostingService.getInstance() }
    
    // 员工人数上限（使用 RecruitmentService 的统一方法）
    val maxEmployees = recruitmentService.getMaxEmployeeCount()
    
    // 响应式计算当前员工数量，确保雇佣后实时更新
    // 修复：添加 saveData.allEmployees 作为 remember 的 key
    val currentEmployeeCount = remember(saveData.allEmployees) {
        saveData.allEmployees.size
    }
    
    // 响应式计算是否员工已满
    // 修复：添加 saveData.allEmployees 作为 remember 的 key
    val isEmployeeFull = remember(saveData.allEmployees) {
        saveData.allEmployees.size >= maxEmployees
    }
    
    var currentJobPosting by remember(jobPosting.id) { mutableStateOf(jobPosting) }
    var showHireSuccessDialog by remember { mutableStateOf(false) }
    var hiredEmployeeName by remember { mutableStateOf("") }
    
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
        Box(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 现代化标题栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.15f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.1f),
                                    Color(0xFFEC4899).copy(alpha = 0.15f)
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "应聘者管理",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentJobPosting.getDescription(),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // 现代化统计信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        StatCard(
                            icon = "📋",
                            label = "待雇佣",
                            value = currentJobPosting.getPendingApplicantsCount().toString(),
                            gradientColors = listOf(
                                Color(0xFF3B82F6).copy(alpha = 0.2f),
                                Color(0xFF60A5FA).copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = "✅",
                            label = "已雇佣",
                            value = currentJobPosting.applicants.count { it.status == ApplicantStatus.HIRED }.toString(),
                            gradientColors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.2f),
                                Color(0xFF34D399).copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = "👥",
                            label = "总计",
                            value = currentJobPosting.applicants.size.toString(),
                            gradientColors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.2f),
                                Color(0xFFA78BFA).copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 各职位当前人数 - 横向排列（类似统计卡片）
                    // 修复：添加 saveData.allEmployees 作为 remember 的 key，确保雇佣员工后实时更新人数
                    val positionCounts = remember(saveData.allEmployees) {
                        mapOf(
                            "程序员" to saveData.allEmployees.count { it.position == "程序员" },
                            "策划师" to saveData.allEmployees.count { it.position == "策划师" },
                            "美术师" to saveData.allEmployees.count { it.position == "美术师" },
                            "音效师" to saveData.allEmployees.count { it.position == "音效师" },
                            "客服" to saveData.allEmployees.count { it.position == "客服" }
                        )
                    }
                    var selectedPositionDialog by remember { mutableStateOf<String?>(null) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        positionCounts.forEach { (position, count) ->
                            PositionCountChip(
                                position = position,
                                count = count,
                                onClick = {
                                    selectedPositionDialog = position
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // 岗位信息对话框
                    selectedPositionDialog?.let { position ->
                        PositionInfoDialog(
                            position = position,
                            count = positionCounts[position] ?: 0,
                            onDismiss = { selectedPositionDialog = null }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 员工数量信息 - 现代化设计
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E293B).copy(alpha = 0.8f),
                                        Color(0xFF334155).copy(alpha = 0.6f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = if (isEmployeeFull) {
                                    Color(0xFFEF4444)
                                } else {
                                    Color(0xFF60A5FA)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "当前员工总数",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$currentEmployeeCount",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEmployeeFull) {
                                    Color(0xFFEF4444)
                                } else {
                                    Color(0xFF60A5FA)
                                }
                            )
                            Text(
                                text = "/ $maxEmployees",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // 员工已满提示 - 现代化设计
                    if (isEmployeeFull) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "员工人数已达上限（${maxEmployees}人），无法继续雇佣！",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFEF4444)
                            )
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
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentJobPosting.applicants.isEmpty()) {
                                        "随着时间推进，会有人才来应聘"
                                    } else {
                                        "已成功雇佣的应聘者不再显示"
                                    },
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
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
                                        
                                        try {
                                            android.util.Log.d("ApplicantManagement", "开始雇佣流程: ${applicant.candidate.name}")
                                            
                                            // 先将应聘者标记为已通过
                                            val updateSuccess = jobPostingService.updateApplicantStatus(
                                                currentJobPosting.id,
                                                applicant.id,
                                                ApplicantStatus.ACCEPTED
                                            )
                                            
                                            if (!updateSuccess) {
                                                android.util.Log.w("ApplicantManagement", "更新状态失败，取消雇佣")
                                                return@ApplicantCard
                                            }
                                            
                                            // 从 JobPostingService 获取候选人（用于标记为已雇佣）
                                            val candidate = jobPostingService.hireApplicant(
                                                currentJobPosting.id,
                                                applicant.id
                                            )
                                            
                                            if (candidate != null) {
                                                // 验证候选人数据
                                                if (candidate.name.isBlank()) {
                                                    android.util.Log.e("ApplicantManagement", "候选人姓名为空")
                                                    return@ApplicantCard
                                                }
                                                
                                                // 更新本地状态
                                                hiredEmployeeName = candidate.name
                                                
                                                // 调用回调，传递候选人（上层组件会使用 RecruitmentService 处理雇佣）
                                                android.util.Log.d("ApplicantManagement", "准备调用 onApplicantHired 回调")
                                                try {
                                                    onApplicantHired(candidate)
                                                    
                                                    // 更新当前岗位信息
                                                    try {
                                                        currentJobPosting = jobPostingService.getJobPosting(currentJobPosting.id) ?: currentJobPosting
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("ApplicantManagement", "更新岗位信息失败", e)
                                                    }
                                                    
                                                    // 显示成功对话框
                                                    showHireSuccessDialog = true
                                                } catch (e: Exception) {
                                                    android.util.Log.e("ApplicantManagement", "调用回调时发生异常", e)
                                                    e.printStackTrace()
                                                }
                                            } else {
                                                android.util.Log.w("ApplicantManagement", "hireApplicant返回null，无法雇佣")
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("ApplicantManagement", "雇佣过程中发生异常", e)
                                            e.printStackTrace()
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
    
    // 成功雇佣提示对话框
    if (showHireSuccessDialog && hiredEmployeeName.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { 
                android.util.Log.d("ApplicantManagement", "关闭成功对话框")
                showHireSuccessDialog = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "雇佣成功",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "已成功雇佣 $hiredEmployeeName！\n\n员工已加入您的团队，可以在员工管理页面查看。",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        android.util.Log.d("ApplicantManagement", "点击确定按钮")
                        showHireSuccessDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("确定")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
    }

/**
 * 现代化统计卡片
 */
@Composable
private fun StatCard(
    icon: String,
    label: String,
    value: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(colors = gradientColors),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label：",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )
    }
}

/**
 * 现代化应聘者卡片
 */
@Composable
private fun ApplicantCard(
    applicant: JobApplicant,
    isEmployeeFull: Boolean = false,
    onHireClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val candidate = applicant.candidate
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = 0.8f),
                        Color(0xFF334155).copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp)
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
                // 现代化头像
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.3f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = candidate.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = candidate.position,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 现代化状态标签
            Text(
                text = when (applicant.status) {
                    ApplicantStatus.HIRED -> "已雇佣"
                    ApplicantStatus.REJECTED -> "已拒绝"
                    else -> "待雇佣"
                },
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = when (applicant.status) {
                                ApplicantStatus.HIRED -> listOf(
                                    Color(0xFF10B981).copy(alpha = 0.25f),
                                    Color(0xFF34D399).copy(alpha = 0.15f)
                                )
                                ApplicantStatus.REJECTED -> listOf(
                                    Color(0xFFEF4444).copy(alpha = 0.25f),
                                    Color(0xFFF87171).copy(alpha = 0.15f)
                                )
                                else -> listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.25f),
                                    Color(0xFF60A5FA).copy(alpha = 0.15f)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 技能和薪资信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InfoChip(
                label = "最高技能",
                value = "Lv.${candidate.getMaxSkillLevel()}",
                icon = "⭐",
                modifier = Modifier.weight(1f, fill = true)
            )
            InfoChip(
                label = "期望薪资",
                value = "¥${formatMoney(candidate.expectedSalary.toLong())}",
                icon = "💰",
                modifier = Modifier.weight(1f, fill = true)
            )
            InfoChip(
                label = "工作经验",
                value = "${candidate.experience}年",
                icon = "💼",
                modifier = Modifier.weight(1f, fill = true)
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
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isEmployeeFull,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEmployeeFull) {
                                Color(0xFF475569)
                            } else {
                                Color(0xFF3B82F6)
                            },
                            disabledContainerColor = Color(0xFF475569),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = if (isEmployeeFull) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEmployeeFull) "已满员" else "雇佣",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                ApplicantStatus.REJECTED -> {
                    Text(
                        text = "已拒绝此应聘者",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center
                    )
                }
                ApplicantStatus.HIRED -> {
                    Text(
                        text = "✅ 已成功雇佣",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {}
            }
        }
    }
}

/**
 * 现代化信息芯片组件
 */
@Composable
private fun InfoChip(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = 0.6f),
                        Color(0xFF334155).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
        Column(
            modifier = Modifier.weight(1f, fill = true)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                minLines = 1
            )
        }
    }
}

/**
 * 现代化职位人数芯片组件
 */
@Composable
private fun PositionCountChip(
    position: String,
    count: Int,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val icon = when (position) {
        "程序员" -> "💻"
        "策划师" -> "📝"
        "美术师" -> "🎨"
        "音效师" -> "🎵"
        "客服" -> "💬"
        else -> "👤"
    }
    
    val gradientColors = if (count > 0) {
        listOf(
            Color(0xFF3B82F6).copy(alpha = 0.2f),
            Color(0xFF60A5FA).copy(alpha = 0.1f)
        )
    } else {
        listOf(
            Color(0xFF1E293B).copy(alpha = 0.4f),
            Color(0xFF334155).copy(alpha = 0.2f)
        )
    }
    
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                brush = Brush.verticalGradient(colors = gradientColors),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )
    }
}

/**
 * 岗位信息对话框
 */
@Composable
private fun PositionInfoDialog(
    position: String,
    count: Int,
    onDismiss: () -> Unit
) {
    val icon = when (position) {
        "程序员" -> "💻"
        "策划师" -> "📝"
        "美术师" -> "🎨"
        "音效师" -> "🎵"
        "客服" -> "💬"
        else -> "👤"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                Text(
                    text = position,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "当前人数",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = count.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                Text("确定", color = Color.White)
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
