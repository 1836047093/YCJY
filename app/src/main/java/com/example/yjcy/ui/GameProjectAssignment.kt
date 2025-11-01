package com.example.yjcy.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.*


// Game类已在MainActivity.kt中定义，避免重复定义

// GameTheme, Platform, BusinessModel 枚举已在MainActivity.kt中定义，避免重复定义

// Employee数据类已在data/Employee.kt和MainActivity.kt中定义，避免重复定义

/**
 * 增强版游戏项目卡片，包含员工分配功能
 */
@Composable
fun EnhancedGameProjectCard(
    game: Game,
    availableEmployees: List<Employee> = emptyList(),
    onEmployeeAssigned: (Game, List<Employee>) -> Unit = { _, _ -> },
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    currentMinuteOfDay: Int = 0, // 当天内的分钟数（0-1439）
    onPauseGame: (() -> Unit)? = null,
    onResumeGame: (() -> Unit)? = null
) {
    var showAssignmentDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 项目基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = game.theme.icon,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "主题: ${game.theme.displayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Text(
                text = "平台: ${game.platforms.joinToString(", ") { it.displayName }}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Text(
                text = "商业模式: ${game.businessModel.displayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 已分配员工信息
            if (game.assignedEmployees.isNotEmpty()) {
                Text(
                    text = "已分配员工 (${game.assignedEmployees.size}人):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    game.assignedEmployees.take(3).forEach { employee ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF10B981).copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${employee.name}(${employee.position})",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    if (game.assignedEmployees.size > 3) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+${game.assignedEmployees.size - 3}",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Text(
                        text = "开发进度",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${(game.developmentProgress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { game.developmentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF10B981),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 一键分配员工按钮
            Button(
                onClick = { showAssignmentDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (game.assignedEmployees.isEmpty()) "👥 分配员工" else "👥 重新分配员工",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // 员工分配对话框
    if (showAssignmentDialog) {
        EmployeeAssignmentDialog(
            game = game,
            availableEmployees = availableEmployees,
            onDismiss = { showAssignmentDialog = false },
            onAssignEmployees = { selectedEmployees ->
                onEmployeeAssigned(game, selectedEmployees)
                showAssignmentDialog = false
            },
            currentYear = currentYear,
            currentMonth = currentMonth,
            currentDay = currentDay,
            currentMinuteOfDay = currentMinuteOfDay,
            onPauseGame = onPauseGame,
            onResumeGame = onResumeGame
        )
    }
}

/**
 * 员工分配对话框
 */
@Composable
fun EmployeeAssignmentDialog(
    game: Game,
    availableEmployees: List<Employee>,
    onDismiss: () -> Unit,
    onAssignEmployees: (List<Employee>) -> Unit,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    currentMinuteOfDay: Int = 0, // 当天内的分钟数（0-1439）
    onPauseGame: (() -> Unit)? = null,
    onResumeGame: (() -> Unit)? = null
) {
    // 过滤掉客服，客服不参与开发
    // 并且只显示符合当前阶段要求的职位
    val developmentEmployees = remember(availableEmployees, game.currentPhase) {
        availableEmployees.filter { 
            it.position != "客服" && 
            it.position in game.currentPhase.requiredPositions 
        }
    }
    
    // 计算当前星期几和时间
    val currentWeekday = remember(currentYear, currentMonth, currentDay) {
        com.example.yjcy.utils.calculateWeekday(currentYear, currentMonth, currentDay)
    }
    val currentHour = remember(currentMinuteOfDay) { currentMinuteOfDay / 60 }
    val currentMinute = remember(currentMinuteOfDay) { currentMinuteOfDay % 60 }
    
    // 检查员工是否在工作时间内
    val employeesWorkingStatus = remember(developmentEmployees, currentWeekday, currentHour, currentMinute) {
        developmentEmployees.associateWith { employee ->
            try {
                employee.isWorking(currentWeekday, currentHour, currentMinute)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    val workingEmployeesCount = employeesWorkingStatus.values.count { it }
    val restingEmployeesCount = developmentEmployees.size - workingEmployeesCount
    
    // 监听对话框打开/关闭，控制游戏暂停
    DisposableEffect(Unit) {
        onPauseGame?.invoke()
        onDispose {
            onResumeGame?.invoke()
        }
    }
    
    var selectedEmployees by remember { mutableStateOf(game.assignedEmployees.toSet()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "👥 为 ${game.name} 分配员工",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 员工工作状态提示
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
                                    "⚠️ 当前非工作时间" 
                                else "✅ 当前工作时间",
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
                                text = "💡 提示：休息中的员工也可以分配，将在工作时间开始后自动开始工作",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                
                // 可用员工列表（排除客服）
                Text(
                    text = "可用开发人员 (${developmentEmployees.size}人):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(developmentEmployees) { employee ->
                        val isWorking = employeesWorkingStatus[employee] ?: false
                        EmployeeSelectionCard(
                            employee = employee,
                            isSelected = selectedEmployees.contains(employee),
                            isWorking = isWorking,
                            onSelectionChanged = { isSelected ->
                                selectedEmployees = if (isSelected) {
                                    selectedEmployees + employee
                                } else {
                                    selectedEmployees - employee
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 选中员工统计
                if (selectedEmployees.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "已选择 ${selectedEmployees.size} 名员工",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            val totalCost = selectedEmployees.sumOf { it.salary }
                            Text(
                                text = "总成本: ¥$totalCost/月",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // 一键分配功能：使用当前阶段推荐人数，优先选择技能高的员工
                            val recommendedCount = game.currentPhase.recommendedCount
                            val bestEmployees = developmentEmployees
                                .sortedByDescending { employee ->
                                    // 根据当前阶段选择对应的技能
                                    when (game.currentPhase) {
                                        DevelopmentPhase.DESIGN -> employee.skillDesign
                                        DevelopmentPhase.ART_SOUND -> maxOf(employee.skillArt, employee.skillMusic)
                                        DevelopmentPhase.PROGRAMMING -> employee.skillDevelopment
                                    }
                                }
                                .take(recommendedCount)
                            selectedEmployees = bestEmployees.toSet()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        )
                    ) {
                        Text("一键分配")
                    }
                    
                    Button(
                        onClick = {
                            onAssignEmployees(selectedEmployees.toList())
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White
                        )
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

/**
 * 员工选择卡片
 */
@Composable
fun EmployeeSelectionCard(
    employee: Employee,
    isSelected: Boolean,
    isWorking: Boolean = true, // 是否在工作时间内
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF3B82F6).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF3B82F6)) else null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = employee.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = employee.position,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${employee.getSpecialtySkillType()}技能：${employee.getSpecialtySkillLevel()}级",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    
                    Text(
                        text = "薪资: ¥${employee.salary}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    
                    // 工作状态指示
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isWorking) Icons.Default.Business else Icons.Default.Home,
                            contentDescription = if (isWorking) "工作中" else "休息中",
                            tint = if (isWorking) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isWorking) "工作中" else "休息中",
                            color = if (isWorking) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            
            if (isSelected) {
                Text(
                    text = "✓",
                    color = Color(0xFF10B981),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}