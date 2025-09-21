package com.example.yjcy.ui

import com.example.yjcy.Employee
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.*
import kotlin.math.roundToInt

/**
 * 分配预览界面
 * 显示详细的分配结果和统计信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentPreviewScreen(
    assignmentPlan: AssignmentPlan,
    projects: List<Project>,
    employees: List<Employee>,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PreviewTab.OVERVIEW) }
    
    Column(
        modifier = modifier
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
        // 顶部栏
        PreviewTopBar(
            onBackClick = onBackClick,
            assignmentPlan = assignmentPlan
        )
        
        // 标签页
        PreviewTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        // 内容区域
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "preview_content",
            modifier = Modifier.weight(1f)
        ) { tab ->
            when (tab) {
                PreviewTab.OVERVIEW -> {
                    OverviewTabContent(
                        assignmentPlan = assignmentPlan,
                        projects = projects,
                        employees = employees
                    )
                }
                PreviewTab.ASSIGNMENTS -> {
                    AssignmentsTabContent(
                        assignmentPlan = assignmentPlan,
                        projects = projects,
                        employees = employees
                    )
                }
                PreviewTab.STATISTICS -> {
                    StatisticsTabContent(
                        assignmentPlan = assignmentPlan,
                        projects = projects,
                        employees = employees
                    )
                }
            }
        }
        
        // 底部操作栏
        PreviewBottomBar(
            onConfirmClick = onConfirmClick,
            onEditClick = onEditClick
        )
    }
}

/**
 * 预览顶部栏
 */
@Composable
private fun PreviewTopBar(
    onBackClick: () -> Unit,
    assignmentPlan: AssignmentPlan
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFF475569)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = "分配预览",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "策略: ${getStrategyDisplayName(assignmentPlan.strategy)}",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
        
        // 总分显示
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = getScoreColor(assignmentPlan.totalScore)
            )
        ) {
            Text(
                text = "${(assignmentPlan.totalScore * 100).roundToInt()}分",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * 预览标签页行
 */
@Composable
private fun PreviewTabRow(
    selectedTab: PreviewTab,
    onTabSelected: (PreviewTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PreviewTab.values().forEach { tab ->
            PreviewTabItem(
                tab = tab,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 预览标签项
 */
@Composable
private fun PreviewTabItem(
    tab: PreviewTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabInfo = getTabInfo(tab)
    
    Card(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF6366F1) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0F4FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tabInfo.icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF6366F1) else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = tabInfo.title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF6366F1) else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 概览标签内容
 */
@Composable
private fun OverviewTabContent(
    assignmentPlan: AssignmentPlan,
    projects: List<Project>,
    employees: List<Employee>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 分配摘要卡片
            AssignmentSummaryCard(
                assignmentPlan = assignmentPlan,
                projects = projects,
                employees = employees
            )
        }
        
        item {
            // 关键指标卡片
            KeyMetricsCard(
                assignmentPlan = assignmentPlan,
                projects = projects,
                employees = employees
            )
        }
        
        item {
            // 项目分配概览
            ProjectAssignmentOverview(
                assignmentPlan = assignmentPlan,
                projects = projects,
                employees = employees
            )
        }
    }
}

/**
 * 分配摘要卡片
 */
@Composable
private fun AssignmentSummaryCard(
    assignmentPlan: AssignmentPlan,
    projects: List<Project>,
    employees: List<Employee>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "分配摘要",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    title = "总项目数",
                    value = projects.size.toString(),
                    icon = Icons.Default.Info,
                    color = Color(0xFF3B82F6)
                )
                
                SummaryItem(
                    title = "分配员工",
                    value = assignmentPlan.assignments.distinctBy { it.employeeId }.size.toString(),
                    icon = Icons.Default.Person,
                    color = Color(0xFF10B981)
                )
                
                SummaryItem(
                    title = "总分配数",
                    value = assignmentPlan.assignments.size.toString(),
                    icon = Icons.Default.List,
                    color = Color(0xFFF59E0B)
                )
            }
        }
    }
}

/**
 * 摘要项
 */
@Composable
private fun SummaryItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    shape = CircleShape
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
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 关键指标卡片
 */
@Composable
private fun KeyMetricsCard(
    assignmentPlan: AssignmentPlan,
    projects: List<Project>,
    employees: List<Employee>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "关键指标",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 计算关键指标
            val avgSkillMatch = assignmentPlan.assignments.map { it.skillMatchScore }.average()
            val employeeUtilization = (assignmentPlan.assignments.distinctBy { it.employeeId }.size.toDouble() / employees.size) * 100
            val projectCoverage = (assignmentPlan.assignments.distinctBy { it.projectId }.size.toDouble() / projects.size) * 100
            
            MetricRow(
                title = "平均技能匹配度",
                value = "${(avgSkillMatch * 100).roundToInt()}%",
                progress = avgSkillMatch.toFloat(),
                color = Color(0xFF8B5CF6)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MetricRow(
                title = "员工利用率",
                value = "${employeeUtilization.roundToInt()}%",
                progress = (employeeUtilization / 100).toFloat(),
                color = Color(0xFF10B981)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MetricRow(
                title = "项目覆盖率",
                value = "${projectCoverage.roundToInt()}%",
                progress = (projectCoverage / 100).toFloat(),
                color = Color(0xFF3B82F6)
            )
        }
    }
}

/**
 * 指标行
 */
@Composable
private fun MetricRow(
    title: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
            
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

/**
 * 项目分配概览
 */
@Composable
private fun ProjectAssignmentOverview(
    assignmentPlan: AssignmentPlan,
    projects: List<Project>,
    employees: List<Employee>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "项目分配概览",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            projects.forEach { project ->
                val projectAssignments = assignmentPlan.assignments.filter { it.projectId == project.id.toString() }
                
                ProjectOverviewItem(
                    project = project,
                    assignedCount = projectAssignments.size,
                    employees = employees.filter { employee ->
                        projectAssignments.any { it.employeeId == employee.id }
                    }
                )
                
                if (project != projects.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 项目概览项
 */
@Composable
private fun ProjectOverviewItem(
    project: Project,
    assignedCount: Int,
    employees: List<Employee>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = project.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "需求: ${project.requiredEmployees}人 | 已分配: ${assignedCount}人",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
        
        // 分配状态指示器
        val isFullyAssigned = assignedCount >= project.requiredEmployees
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isFullyAssigned) Color(0xFF10B981) else Color(0xFFF59E0B),
                    shape = CircleShape
                )
        )
    }
}

/**
 * 分配标签内容
 */
@Composable
private fun AssignmentsTabContent(
    assignmentPlan: AssignmentPlan,
    projects: List<Project>,
    employees: List<Employee>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(assignmentPlan.assignments) { assignment ->
            val employee = employees.find { it.id == assignment.employeeId }
            val project = projects.find { it.id.toString() == assignment.projectId }
            
            if (employee != null && project != null) {
                AssignmentDetailCard(
                    assignment = assignment,
                    employee = employee,
                    project = project
                )
            }
        }
    }
}

/**
 * 分配详情卡片
 */
@Composable
private fun AssignmentDetailCard(
    assignment: EmployeeAssignment,
    employee: Employee,
    project: Project
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 员工头像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF6366F1).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(1),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 分配信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                
                Text(
                    text = "→ ${project.name}",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                
                Text(
                    text = "技能匹配: ${(assignment.skillMatchScore * 100).roundToInt()}%",
                    fontSize = 12.sp,
                    color = Color(0xFF10B981)
                )
            }
            
            // 匹配度指示器
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = getScoreColor(assignment.skillMatchScore).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(assignment.skillMatchScore * 100).roundToInt()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(assignment.skillMatchScore)
                )
            }
        }
    }
}

/**
 * 统计标签内容
 */
@Composable
private fun StatisticsTabContent(
    assignmentPlan: AssignmentPlan,
    projects: List<Project>,
    employees: List<Employee>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "详细统计信息",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "统计图表将在这里显示",
                        fontSize = 16.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 预览底部栏
 */
@Composable
private fun PreviewBottomBar(
    onConfirmClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF6366F1))
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "编辑",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6366F1)
            )
        }
        
        Button(
            onClick = onConfirmClick,
            modifier = Modifier
                .weight(2f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "确认分配",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * 预览标签枚举
 */
private enum class PreviewTab {
    OVERVIEW, ASSIGNMENTS, STATISTICS
}

/**
 * 标签信息数据类
 */
private data class TabInfo(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * 获取标签信息
 */
private fun getTabInfo(tab: PreviewTab): TabInfo {
    return when (tab) {
        PreviewTab.OVERVIEW -> TabInfo("概览", Icons.Default.Check)
        PreviewTab.ASSIGNMENTS -> TabInfo("分配", Icons.Default.Check)
        PreviewTab.STATISTICS -> TabInfo("统计", Icons.Default.Check)
    }
}

/**
 * 获取策略显示名称
 */
private fun getStrategyDisplayName(strategy: AssignmentStrategy): String {
    return when (strategy) {
        AssignmentStrategy.SKILL_PRIORITY -> "技能优先"
        AssignmentStrategy.LOAD_BALANCE -> "负载均衡"
        AssignmentStrategy.COST_OPTIMIZATION -> "成本优化"
        AssignmentStrategy.MIXED -> "综合平衡"
    }
}

/**
 * 获取分数颜色
 */
private fun getScoreColor(score: Float): Color {
    return when {
        score >= 0.8 -> Color(0xFF10B981) // 绿色
        score >= 0.6 -> Color(0xFFF59E0B) // 黄色
        else -> Color(0xFFEF4444) // 红色
    }
}