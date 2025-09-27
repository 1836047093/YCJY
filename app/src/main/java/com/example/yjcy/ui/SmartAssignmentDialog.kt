package com.example.yjcy.ui

import com.example.yjcy.data.Employee
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.*
import kotlinx.coroutines.delay

/**
 * 智能分配对话框
 * 提供分配策略选择、预览和确认功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAssignmentDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    projects: List<Project>,
    employees: List<Employee>,
    onAssignmentConfirmed: (AssignmentStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            SmartAssignmentContent(
                projects = projects,
                employees = employees,
                onDismiss = onDismiss,
                onAssignmentConfirmed = onAssignmentConfirmed,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun SmartAssignmentContent(
    projects: List<Project>,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onAssignmentConfirmed: (AssignmentStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStrategy by remember { mutableStateOf(AssignmentStrategy.MIXED) }
    var currentStep by remember { mutableStateOf(AssignmentStep.STRATEGY_SELECTION) }
    var previewPlan by remember { mutableStateOf<AssignmentPlan?>(null) }
    
    Card(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.9f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFE2E8F0)
                        )
                    )
                )
        ) {
            // 标题栏
            AssignmentDialogHeader(
                currentStep = currentStep,
                onDismiss = onDismiss,
                onBackClick = {
                    currentStep = when (currentStep) {
                        AssignmentStep.PREVIEW -> AssignmentStep.STRATEGY_SELECTION
                        else -> currentStep
                    }
                }
            )
            
            // 内容区域
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "dialog_content",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    AssignmentStep.STRATEGY_SELECTION -> {
                        StrategySelectionContent(
                            selectedStrategy = selectedStrategy,
                            onStrategySelected = { selectedStrategy = it },
                            projects = projects,
                            employees = employees,
                            onNextClick = {
                                currentStep = AssignmentStep.PREVIEW
                                // 这里可以预生成分配计划
                            }
                        )
                    }
                    AssignmentStep.PREVIEW -> {
                        AssignmentPreviewContent(
                            strategy = selectedStrategy,
                            _projects = projects,
                            _employees = employees,
                            _previewPlan = previewPlan,
                            onConfirmClick = {
                                onAssignmentConfirmed(selectedStrategy)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 对话框标题栏
 */
@Composable
private fun AssignmentDialogHeader(
    currentStep: AssignmentStep,
    onDismiss: () -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (currentStep == AssignmentStep.PREVIEW) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color(0xFF475569)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column {
                Text(
                    text = when (currentStep) {
                        AssignmentStep.STRATEGY_SELECTION -> "智能分配设置"
                        AssignmentStep.PREVIEW -> "分配预览"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = when (currentStep) {
                        AssignmentStep.STRATEGY_SELECTION -> "选择分配策略和参数"
                        AssignmentStep.PREVIEW -> "确认分配方案"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color(0xFF475569)
            )
        }
    }
}

/**
 * 策略选择内容
 */
@Composable
private fun StrategySelectionContent(
    selectedStrategy: AssignmentStrategy,
    onStrategySelected: (AssignmentStrategy) -> Unit,
    projects: List<Project>,
    employees: List<Employee>,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // 项目和员工概览
        AssignmentOverviewCard(
            projects = projects,
            employees = employees,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 策略选择
        Text(
            text = "选择分配策略",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(AssignmentStrategy.entries) { strategy ->
                StrategyCard(
                    strategy = strategy,
                    isSelected = strategy == selectedStrategy,
                    onClick = { onStrategySelected(strategy) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 下一步按钮
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366F1)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "预览分配方案",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 分配概览卡片
 */
@Composable
private fun AssignmentOverviewCard(
    projects: List<Project>,
    employees: List<Employee>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OverviewItem(
                icon = Icons.Default.Check,
                title = "项目数量",
                value = projects.size.toString(),
                color = Color(0xFF3B82F6)
            )
            
            OverviewItem(
                icon = Icons.Default.Check,
                title = "可用员工",
                value = employees.size.toString(),
                color = Color(0xFF10B981)
            )
            
            OverviewItem(
                icon = Icons.Default.Check,
                title = "需求人数",
                value = projects.sumOf { it.maxEmployees }.toString(),
                color = Color(0xFFF59E0B)
            )
        }
    }
}

/**
 * 概览项目
 */
@Composable
private fun OverviewItem(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )
    }
}

/**
 * 策略卡片
 */
@Composable
private fun StrategyCard(
    strategy: AssignmentStrategy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val strategyInfo = getStrategyInfo(strategy)
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF6366F1) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0F4FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = strategyInfo.color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = strategyInfo.icon,
                    contentDescription = null,
                    tint = strategyInfo.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strategyInfo.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = strategyInfo.description,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选择",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 分配预览内容
 */
@Composable
private fun AssignmentPreviewContent(
    strategy: AssignmentStrategy,
    @Suppress("UNUSED_PARAMETER") _projects: List<Project>,
    @Suppress("UNUSED_PARAMETER") _employees: List<Employee>,
    @Suppress("UNUSED_PARAMETER") _previewPlan: AssignmentPlan?,
    onConfirmClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // 策略信息
        val strategyInfo = getStrategyInfo(strategy)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = strategyInfo.color.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = strategyInfo.icon,
                    contentDescription = null,
                    tint = strategyInfo.color,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "选择的策略: ${strategyInfo.title}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = strategyInfo.description,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 预览内容
        Text(
            text = "分配预览",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 这里可以显示预览的分配结果
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(48.dp)
                )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "分配预览将在这里显示",
                        fontSize = 16.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "包括员工分配详情、技能匹配度等信息",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 确认按钮
        Button(
            onClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "确认分配",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 分配步骤枚举
 */
private enum class AssignmentStep {
    STRATEGY_SELECTION, PREVIEW
}

/**
 * 策略信息数据类
 */
private data class StrategyInfo(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

/**
 * 获取策略信息
 */
private fun getStrategyInfo(strategy: AssignmentStrategy): StrategyInfo {
    return when (strategy) {
        AssignmentStrategy.SKILL_PRIORITY -> StrategyInfo(
            title = "技能优先",
            description = "优先考虑员工技能与项目需求的匹配度，确保最佳技能配置",
            icon = Icons.Default.Check,
            color = Color(0xFF8B5CF6)
        )
        AssignmentStrategy.LOAD_BALANCE -> StrategyInfo(
            title = "负载均衡",
            description = "平衡分配工作负载，确保每个员工的工作量相对均匀",
            icon = Icons.Default.Check,
            color = Color(0xFF3B82F6)
        )
        AssignmentStrategy.COST_OPTIMIZATION -> StrategyInfo(
            title = "成本优化",
            description = "在满足项目需求的前提下，优化人力成本配置",
            icon = Icons.Default.Check,
            color = Color(0xFFF59E0B)
        )
        AssignmentStrategy.MIXED -> StrategyInfo(
            title = "综合平衡",
            description = "综合考虑技能匹配、负载均衡和成本优化，提供最佳整体方案",
            icon = Icons.Default.Check,
            color = Color(0xFF10B981)
        )
    }
}