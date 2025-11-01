package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
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
import com.example.yjcy.data.EnhancedAssignmentResult
import com.example.yjcy.data.SkillMatchResult
import com.example.yjcy.data.Employee
import com.example.yjcy.data.Game
import com.example.yjcy.data.DevelopmentPhase

/**
 * 增强分配结果对话框
 * 显示详细的分配结果和理由
 */
@Composable
fun EnhancedAssignmentResultDialog(
    assignmentResult: EnhancedAssignmentResult,
    projectNames: Map<String, String> = emptyMap(),
    games: List<Game> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    currentMinuteOfDay: Int = 0, // 当天内的分钟数（0-1439）
    onPauseGame: (() -> Unit)? = null,
    onResumeGame: (() -> Unit)? = null
) {
    // 监听对话框打开/关闭，控制游戏暂停
    DisposableEffect(Unit) {
        onPauseGame?.invoke()
        onDispose {
            onResumeGame?.invoke()
        }
    }
    
    // 计算当前星期几和时间
    val currentWeekday = remember(currentYear, currentMonth, currentDay) {
        com.example.yjcy.utils.calculateWeekday(currentYear, currentMonth, currentDay)
    }
    val currentHour = remember(currentMinuteOfDay) { currentMinuteOfDay / 60 }
    val currentMinute = remember(currentMinuteOfDay) { currentMinuteOfDay % 60 }
    
    // 添加调试日志，检查传入的时间参数
    LaunchedEffect(currentMinuteOfDay, currentYear, currentMonth, currentDay) {
        android.util.Log.d("AssignmentDialog", 
            "时间参数: year=$currentYear, month=$currentMonth, day=$currentDay, minuteOfDay=$currentMinuteOfDay, " +
            "weekday=$currentWeekday, hour=$currentHour, minute=$currentMinute")
    }
    
    // 检查所有分配员工的工作状态
    val allAssignedEmployees = remember(assignmentResult.assignments) {
        assignmentResult.assignments.values.flatten()
    }
    
    val employeesWorkingStatus = remember(allAssignedEmployees, currentWeekday, currentHour, currentMinute, currentMinuteOfDay) {
        // 再次计算，确保使用最新的时间
        val hour = currentMinuteOfDay / 60
        val minute = currentMinuteOfDay % 60
        
        android.util.Log.d("AssignmentDialog", 
            "开始检查${allAssignedEmployees.size}名员工的工作状态: " +
            "weekday=$currentWeekday(${com.example.yjcy.utils.getWeekdayName(currentWeekday)}), " +
            "hour=$hour, minute=$minute, minuteOfDay=$currentMinuteOfDay")
        
        allAssignedEmployees.associateWith { employee ->
            try {
                // 调试：检查工作时间和员工设置
                android.util.Log.d("AssignmentDialog", 
                    "检查员工${employee.name}: " +
                    "workDays=${employee.workSchedule.workDays}, " +
                    "startHour=${employee.workSchedule.startHour}:${employee.workSchedule.startMinute}, " +
                    "endHour=${employee.workSchedule.endHour}:${employee.workSchedule.endMinute}")
                
                val isWorking = employee.isWorking(currentWeekday, hour, minute)
                
                android.util.Log.d("AssignmentDialog", 
                    "员工${employee.name}工作状态: isWorking=$isWorking")
                
                isWorking
            } catch (e: Exception) {
                android.util.Log.e("AssignmentDialog", "检查员工工作时间失败: ${employee.name}", e)
                e.printStackTrace()
                false
            }
        }
    }
    
    val workingEmployeesCount = employeesWorkingStatus.values.count { it }
    val restingEmployeesCount = allAssignedEmployees.size - workingEmployeesCount
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A),
                                Color(0xFF3B82F6)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // 标题栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "分配完成",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 员工工作状态提示
                    if (allAssignedEmployees.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (restingEmployeesCount > 0) 
                                    Color(0xFFF59E0B).copy(alpha = 0.2f) 
                                else Color(0xFF10B981).copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (restingEmployeesCount > 0) 
                                            Color(0xFFF59E0B) 
                                        else Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (restingEmployeesCount > 0) 
                                            "⚠️ 部分员工在休息中" 
                                        else "✅ 所有员工都在工作时间内",
                                        color = if (restingEmployeesCount > 0) 
                                            Color(0xFFF59E0B) 
                                        else Color(0xFF10B981),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "工作时间：${workingEmployeesCount}人",
                                        color = Color(0xFF10B981),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "休息中：${restingEmployeesCount}人",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 13.sp
                                    )
                                }
                                if (restingEmployeesCount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "💡 提示：休息中的员工已分配，将在工作时间开始后自动开始工作",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 分配摘要
                    AssignmentSummaryCard(assignmentResult)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 详细分配结果
                    Text(
                        text = "分配详情",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(assignmentResult.assignments.entries.toList()) { (projectId, employees) ->
                            ProjectAssignmentCard(
                                projectId = projectId,
                                projectName = projectNames[projectId] ?: "项目 $projectId",
                                employees = employees,
                                reasons = assignmentResult.reasons[projectId] ?: emptyList()
                            )
                        }
                        
                        // 无法分配的项目
                        if (assignmentResult.failedProjects.isNotEmpty()) {
                            item {
                                FailedProjectsCard(
                                    failedProjects = assignmentResult.failedProjects,
                                    projectNames = projectNames,
                                    games = games
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 按钮
                    if (assignmentResult.assignments.isEmpty()) {
                        // 如果没有成功分配任何项目，只显示关闭按钮
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            )
                        ) {
                            Text(
                                text = "关闭",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // 有成功分配的项目，显示重新分配和确认分配按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.3f))
                                    )
                                )
                            ) {
                                Text("重新分配")
                            }
                            
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981)
                                )
                            ) {
                                Text(
                                    text = "确认分配",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
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
 * 分配摘要卡片
 */
@Composable
private fun AssignmentSummaryCard(result: EnhancedAssignmentResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    icon = Icons.Default.Person,
                    label = "已分配员工",
                    value = "${result.totalAssignedEmployees}人",
                    color = Color(0xFF10B981)
                )
                
                SummaryItem(
                    icon = Icons.Default.Star,
                    label = "分配项目",
                    value = "${result.assignments.size}个",
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}

/**
 * 摘要项目
 */
@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * 项目分配卡片
 */
@Composable
private fun ProjectAssignmentCard(
    projectId: String,
    projectName: String,
    employees: List<Employee>,
    reasons: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 项目标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = projectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "${employees.size}人",
                    fontSize = 14.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 员工列表
            employees.forEach { employee ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${employee.name}(${employee.position})",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${employee.getSpecialtySkillType()}技能：${employee.getSpecialtySkillLevel()}级",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 无法分配的项目卡片
 */
@Composable
private fun FailedProjectsCard(
    failedProjects: Map<String, String>,
    projectNames: Map<String, String>,
    games: List<Game>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "❌",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "无法开发",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            failedProjects.forEach { (projectId, reason) ->
                // 获取游戏的开发阶段
                val game = games.firstOrNull { it.id == projectId }
                val phase = game?.currentPhase
                
                // 解析reason字符串，根据阶段过滤显示
                val staffInfo = parseStaffRequirement(reason, phase)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = projectNames[projectId] ?: "项目 $projectId",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = staffInfo,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 无法分配的员工卡片
 */
@Composable
private fun UnassignedEmployeesCard(unassignedEmployees: List<Employee>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "无法分配的员工 (${unassignedEmployees.size}人)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            unassignedEmployees.forEach { employee ->
                Text(
                    text = "• ${employee.name}(${employee.position}) - ${employee.getSpecialtySkillType()}技能：${employee.getSpecialtySkillLevel()}级",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * 解析员工需求字符串，根据开发阶段过滤显示
 * 输入示例：“策划师最少需要1人，当前只有0人”
 * 输出示例：“还需要1名策划”（只显示当前阶段需要的职位）
 */
private fun parseStaffRequirement(reason: String, phase: DevelopmentPhase?): String {
    // 岗位映射（全称 -> 简称）
    val positionMap = mapOf(
        "程序员" to "程序",
        "策划师" to "策划",
        "美术师" to "美术",
        "音效师" to "音效"
    )
    
    // 获取当前阶段需要的职位
    val requiredPositions = phase?.requiredPositions ?: emptyList()
    
    // 解析reason字符串
    val staffData = mutableMapOf<String, Pair<Int, Int>>() // 岗位 -> (最少需要人数, 当前人数)
    
    reason.split("；").forEach { part ->
        // 匹配格式：“策划师最少需要1人，当前只有0人”
        val regex = """(\S+)最少需要(\d+)人，当前只有(\d+)人""".toRegex()
        val matchResult = regex.find(part.trim())
        
        if (matchResult != null) {
            val position = matchResult.groupValues[1]
            // 只解析当前阶段需要的职位
            if (requiredPositions.isEmpty() || position in requiredPositions) {
                val required = matchResult.groupValues[2].toInt()
                val current = matchResult.groupValues[3].toInt()
                staffData[position] = Pair(required, current)
            }
        }
    }
    
    // 计算总的当前人数
    val totalCurrent = staffData.values.sumOf { it.second }
    
    // 构建需求列表
    val needsList = mutableListOf<String>()
    staffData.forEach { (position, counts) ->
        val (required, current) = counts
        val needed = required - current
        if (needed > 0) {
            val shortName = positionMap[position] ?: position
            needsList.add("${needed}名${shortName}")
        }
    }
    
    // 构建最终字符串
    return if (totalCurrent > 0 && needsList.isNotEmpty()) {
        // 找出当前已有的岗位（假设只有一种岗位有人）
        val currentPosition = staffData.entries.firstOrNull { it.value.second > 0 }
        val currentShortName = currentPosition?.let { positionMap[it.key] ?: it.key } ?: "员工"
        val currentCount = currentPosition?.value?.second ?: 0
        
        "目前已有${currentShortName}${currentCount}名，还需要" + needsList.joinToString("，")
    } else if (needsList.isNotEmpty()) {
        "还需要" + needsList.joinToString("，")
    } else {
        "人员配置完整"
    }
}