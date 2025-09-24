package com.example.yjcy.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * 招聘历史界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitmentHistoryScreen(
    onNavigateBack: () -> Unit = {},
    hrManager: HRManager = remember { HRManager() }
) {
    val recruitmentTasks by hrManager.recruitmentTasks.collectAsState()
    val recruitmentConfigs by hrManager.recruitmentConfigs.collectAsState()
    
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var selectedTaskId by remember { mutableStateOf<Int?>(null) }
    var showStatsDialog by remember { mutableStateOf(false) }
    
    val filteredTasks = when (selectedFilter) {
        HistoryFilter.ALL -> recruitmentTasks
        HistoryFilter.COMPLETED -> recruitmentTasks.filter { it.status == RecruitmentTaskStatus.COMPLETED }
        HistoryFilter.ACTIVE -> recruitmentTasks.filter { it.status == RecruitmentTaskStatus.ACTIVE }
        HistoryFilter.PAUSED -> recruitmentTasks.filter { it.status == RecruitmentTaskStatus.PAUSED }
    }
    
    val stats = calculateRecruitmentStats(recruitmentTasks)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 顶部栏
        HistoryTopBar(
            onNavigateBack = onNavigateBack,
            onShowStats = { showStatsDialog = true }
        )
        
        // 统计卡片
        StatsOverviewCard(
            stats = stats,
            onClick = { showStatsDialog = true }
        )
        
        // 筛选器
        FilterRow(
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            taskCounts = mapOf(
                HistoryFilter.ALL to recruitmentTasks.size,
                HistoryFilter.COMPLETED to recruitmentTasks.count { it.status == RecruitmentTaskStatus.COMPLETED },
                HistoryFilter.ACTIVE to recruitmentTasks.count { it.status == RecruitmentTaskStatus.ACTIVE },
                HistoryFilter.PAUSED to recruitmentTasks.count { it.status == RecruitmentTaskStatus.PAUSED }
            )
        )
        
        // 任务列表
        if (filteredTasks.isEmpty()) {
            EmptyHistoryContent(filter = selectedFilter)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredTasks.sortedByDescending { it.updatedAt }) { task ->
                    val config = recruitmentConfigs.find { it.id == task.configId }
                    if (config != null) {
                        RecruitmentTaskHistoryCard(
                            task = task,
                            config = config,
                            onClick = { selectedTaskId = task.id }
                        )
                    }
                }
            }
        }
    }
    
    // 任务详情对话框
    selectedTaskId?.let { taskId ->
        val task = recruitmentTasks.find { it.id == taskId }
        val config = task?.let { recruitmentConfigs.find { it.id == task.configId } }
        if (task != null && config != null) {
            TaskDetailDialog(
                task = task,
                config = config,
                onDismiss = { selectedTaskId = null }
            )
        }
    }
    
    // 统计详情对话框
    if (showStatsDialog) {
        StatsDetailDialog(
            stats = stats,
            tasks = recruitmentTasks,
            configs = recruitmentConfigs,
            onDismiss = { showStatsDialog = false }
        )
    }
}

/**
 * 历史筛选器枚举
 */
enum class HistoryFilter {
    ALL, COMPLETED, ACTIVE, PAUSED
}

/**
 * 招聘统计数据
 */
data class RecruitmentStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val activeTasks: Int,
    val pausedTasks: Int,
    val totalHired: Int,
    val totalRejected: Int,
    val totalBudgetUsed: Int,
    val averageHiringCost: Int,
    val successRate: Float,
    val averageTaskDuration: Long
)

/**
 * 计算招聘统计数据
 */
fun calculateRecruitmentStats(tasks: List<RecruitmentTask>): RecruitmentStats {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.status == RecruitmentTaskStatus.COMPLETED }
    val activeTasks = tasks.count { it.status == RecruitmentTaskStatus.ACTIVE }
    val pausedTasks = tasks.count { it.status == RecruitmentTaskStatus.PAUSED }
    
    val totalHired = tasks.sumOf { it.hiredCount }
    val totalRejected = tasks.sumOf { it.rejectedCandidates.size }
    val totalBudgetUsed = tasks.sumOf { it.totalBudgetUsed }
    
    val averageHiringCost = if (totalHired > 0) totalBudgetUsed / totalHired else 0
    val successRate = if (totalHired + totalRejected > 0) {
        totalHired.toFloat() / (totalHired + totalRejected)
    } else 0f
    
    val completedTasksWithDuration = tasks.filter { 
        it.status == RecruitmentTaskStatus.COMPLETED && it.completedAt != null 
    }
    val averageTaskDuration = if (completedTasksWithDuration.isNotEmpty()) {
        completedTasksWithDuration.map { 
            it.completedAt!! - it.createdAt 
        }.average().toLong()
    } else 0L
    
    return RecruitmentStats(
        totalTasks = totalTasks,
        completedTasks = completedTasks,
        activeTasks = activeTasks,
        pausedTasks = pausedTasks,
        totalHired = totalHired,
        totalRejected = totalRejected,
        totalBudgetUsed = totalBudgetUsed,
        averageHiringCost = averageHiringCost,
        successRate = successRate,
        averageTaskDuration = averageTaskDuration
    )
}

/**
 * 历史界面顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopBar(
    onNavigateBack: () -> Unit,
    onShowStats: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "招聘历史",
                fontWeight = FontWeight.Bold
            )
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
            IconButton(onClick = onShowStats) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "详细统计"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

/**
 * 统计概览卡片
 */
@Composable
fun StatsOverviewCard(
    stats: RecruitmentStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "招聘统计概览",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "查看详情",
                    tint = Color(0xFF666666)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsItem(
                    label = "总任务",
                    value = stats.totalTasks.toString(),
                    color = Color(0xFF2196F3)
                )
                
                StatsItem(
                    label = "已完成",
                    value = stats.completedTasks.toString(),
                    color = Color(0xFF4CAF50)
                )
                
                StatsItem(
                    label = "已招聘",
                    value = stats.totalHired.toString(),
                    color = Color(0xFFFF9800)
                )
                
                StatsItem(
                    label = "成功率",
                    value = "${(stats.successRate * 100).roundToInt()}%",
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
fun StatsItem(
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
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

/**
 * 筛选器行
 */
@Composable
fun FilterRow(
    selectedFilter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit,
    taskCounts: Map<HistoryFilter, Int>
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HistoryFilter.values()) { filter ->
            val isSelected = selectedFilter == filter
            val count = taskCounts[filter] ?: 0
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        text = "${getFilterLabel(filter)} ($count)",
                        fontSize = 14.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2196F3),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

/**
 * 获取筛选器标签
 */
fun getFilterLabel(filter: HistoryFilter): String {
    return when (filter) {
        HistoryFilter.ALL -> "全部"
        HistoryFilter.COMPLETED -> "已完成"
        HistoryFilter.ACTIVE -> "进行中"
        HistoryFilter.PAUSED -> "已暂停"
    }
}

/**
 * 招聘任务历史卡片
 */
@Composable
fun RecruitmentTaskHistoryCard(
    task: RecruitmentTask,
    config: RecruitmentConfig,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.positionType,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    
                    Text(
                        text = "创建时间: ${dateFormat.format(Date(task.createdAt))}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    
                    if (task.completedAt != null) {
                        Text(
                            text = "完成时间: ${dateFormat.format(Date(task.completedAt))}",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                // 状态标签
                Surface(
                    color = when (task.status) {
                        RecruitmentTaskStatus.ACTIVE -> Color(0xFF4CAF50)
                        RecruitmentTaskStatus.COMPLETED -> Color(0xFF2196F3)
                        RecruitmentTaskStatus.PAUSED -> Color(0xFFFF9800)
                        RecruitmentTaskStatus.CANCELLED -> Color(0xFF9E9E9E)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (task.status) {
                            RecruitmentTaskStatus.ACTIVE -> "进行中"
                            RecruitmentTaskStatus.COMPLETED -> "已完成"
                            RecruitmentTaskStatus.PAUSED -> "已暂停"
                            RecruitmentTaskStatus.CANCELLED -> "已取消"
                        },
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            val progress = if (task.targetCount > 0) {
                task.hiredCount.toFloat() / task.targetCount
            } else 0f
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "招聘进度",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    
                    Text(
                        text = "${task.hiredCount}/${task.targetCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        progress >= 1f -> Color(0xFF4CAF50)
                        progress >= 0.7f -> Color(0xFF2196F3)
                        progress >= 0.3f -> Color(0xFFFF9800)
                        else -> Color(0xFFE57373)
                    },
                    trackColor = Color(0xFFE0E0E0)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TaskStatsItem(
                    label = "待确认",
                    value = task.getPendingCandidatesCount().toString(),
                    color = Color(0xFFFF9800)
                )
                
                TaskStatsItem(
                    label = "已拒绝",
                    value = task.rejectedCandidates.size.toString(),
                    color = Color(0xFFE57373)
                )
                
                TaskStatsItem(
                    label = "预算使用",
                    value = "${task.totalBudgetUsed}万",
                    color = Color(0xFF9C27B0)
                )
                
                TaskStatsItem(
                    label = "平均成本",
                    value = if (task.hiredCount > 0) {
                        "${task.getAverageHiringCost()}万"
                    } else {
                        "--"
                    },
                    color = Color(0xFF607D8B)
                )
            }
        }
    }
}

/**
 * 任务统计项
 */
@Composable
fun TaskStatsItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
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
 * 空历史内容
 */
@Composable
fun EmptyHistoryContent(
    filter: HistoryFilter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (filter) {
                HistoryFilter.ALL -> Icons.Default.History
                HistoryFilter.COMPLETED -> Icons.Default.CheckCircle
                HistoryFilter.ACTIVE -> Icons.Default.PlayCircle
                HistoryFilter.PAUSED -> Icons.Default.PauseCircle
            },
            contentDescription = "无数据",
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when (filter) {
                HistoryFilter.ALL -> "暂无招聘记录"
                HistoryFilter.COMPLETED -> "暂无已完成的任务"
                HistoryFilter.ACTIVE -> "暂无进行中的任务"
                HistoryFilter.PAUSED -> "暂无已暂停的任务"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        
        Text(
            text = when (filter) {
                HistoryFilter.ALL -> "创建招聘配置后，系统将开始自动招聘"
                HistoryFilter.COMPLETED -> "完成的招聘任务将在这里显示"
                HistoryFilter.ACTIVE -> "正在进行的招聘任务将在这里显示"
                HistoryFilter.PAUSED -> "暂停的招聘任务将在这里显示"
            },
            fontSize = 14.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 任务详情对话框
 */
@Composable
fun TaskDetailDialog(
    task: RecruitmentTask,
    config: RecruitmentConfig,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
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
                        text = "任务详情",
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
                
                // 基本信息
                DetailSection(
                    title = "基本信息",
                    items = listOf(
                        "职位类型" to config.positionType,
                        "目标人数" to "${task.targetCount}人",
                        "任务状态" to when (task.status) {
                            RecruitmentTaskStatus.ACTIVE -> "进行中"
                            RecruitmentTaskStatus.COMPLETED -> "已完成"
                            RecruitmentTaskStatus.PAUSED -> "已暂停"
                            RecruitmentTaskStatus.CANCELLED -> "已取消"
                        },
                        "创建时间" to dateFormat.format(Date(task.createdAt)),
                        "更新时间" to dateFormat.format(Date(task.updatedAt))
                    )
                )
                
                if (task.completedAt != null) {
                    DetailItem(
                        label = "完成时间",
                        value = dateFormat.format(Date(task.completedAt))
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 招聘统计
                DetailSection(
                    title = "招聘统计",
                    items = listOf(
                        "已招聘" to "${task.hiredCount}人",
                        "待确认" to "${task.getPendingCandidatesCount()}人",
                        "已拒绝" to "${task.rejectedCandidates.size}人",
                        "自动批准" to "${task.getAutoApprovedCount()}人",
                        "招聘进度" to "${((task.hiredCount.toFloat() / task.targetCount) * 100).roundToInt()}%"
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 预算信息
                DetailSection(
                    title = "预算信息",
                    items = listOf(
                        "总预算使用" to "${task.totalBudgetUsed}万",
                        "平均招聘成本" to if (task.hiredCount > 0) {
                            "${task.getAverageHiringCost()}万/人"
                        } else {
                            "--"
                        },
                        "最高薪资限制" to "${config.maxSalary}万/年"
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 配置信息
                DetailSection(
                    title = "配置要求",
                    items = listOf(
                        "最低技能等级" to "${config.minSkillLevel}级",
                        "最低年龄" to "${config.minAge}岁",
                        "最高经验" to "${config.maxExperience}年",
                        "优先级" to when (config.priority) {
                            RecruitmentPriority.LOW -> "低"
                            RecruitmentPriority.NORMAL -> "普通"
                            RecruitmentPriority.HIGH -> "高"
                            RecruitmentPriority.URGENT -> "紧急"
                        },
                        "自动批准" to if (config.autoApprove) {
                            "启用 (≥${(config.autoApproveThreshold * 100).toInt()}%)"
                        } else {
                            "禁用"
                        }
                    )
                )
                
                if (config.specialRequirements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailItem(
                        label = "特殊要求",
                        value = config.specialRequirements
                    )
                }
            }
        }
    }
}

/**
 * 详情区块
 */
@Composable
fun DetailSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF333333)
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    items.forEach { (label, value) ->
        DetailItem(label = label, value = value)
    }
}

/**
 * 详情项
 */
@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 统计详情对话框
 */
@Composable
fun StatsDetailDialog(
    stats: RecruitmentStats,
    tasks: List<RecruitmentTask>,
    configs: List<RecruitmentConfig>,
    onDismiss: () -> Unit
) {
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
                        text = "详细统计",
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
                
                // 任务统计
                DetailSection(
                    title = "任务统计",
                    items = listOf(
                        "总任务数" to stats.totalTasks.toString(),
                        "已完成" to stats.completedTasks.toString(),
                        "进行中" to stats.activeTasks.toString(),
                        "已暂停" to stats.pausedTasks.toString(),
                        "完成率" to "${if (stats.totalTasks > 0) (stats.completedTasks * 100 / stats.totalTasks) else 0}%"
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 招聘统计
                DetailSection(
                    title = "招聘统计",
                    items = listOf(
                        "总招聘人数" to stats.totalHired.toString(),
                        "总拒绝人数" to stats.totalRejected.toString(),
                        "招聘成功率" to "${(stats.successRate * 100).roundToInt()}%",
                        "平均招聘成本" to "${stats.averageHiringCost}万/人"
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 预算统计
                DetailSection(
                    title = "预算统计",
                    items = listOf(
                        "总预算使用" to "${stats.totalBudgetUsed}万",
                        "活跃配置数" to configs.count { it.isActive }.toString(),
                        "平均任务时长" to if (stats.averageTaskDuration > 0) {
                            "${stats.averageTaskDuration / (1000 * 60 * 60 * 24)}天"
                        } else {
                            "--"
                        }
                    )
                )
            }
        }
    }
}