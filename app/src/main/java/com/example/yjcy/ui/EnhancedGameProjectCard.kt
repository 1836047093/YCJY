package com.example.yjcy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.EnhancedAssignmentResult

/**
 * 增强版游戏项目卡片
 * 集成了新的智能分配功能，不修改原有代码
 */
@Composable
fun EnhancedGameProjectCard(
    game: Game,
    availableEmployees: List<Employee>,
    allProjects: List<Game>,
    onEmployeeAssigned: (Game, List<Employee>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOriginalDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 项目标题和基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${game.theme.displayName} • ${game.platforms.joinToString(", ") { it.displayName }}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (game.businessModel) {
                                    BusinessModel.SINGLE_PLAYER -> Color(0xFF8B5CF6)
                                    BusinessModel.ONLINE_GAME -> Color(0xFF3B82F6)
                                }.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = game.businessModel.displayName,
                                color = when (game.businessModel) {
                                    BusinessModel.SINGLE_PLAYER -> Color(0xFF8B5CF6)
                                    BusinessModel.ONLINE_GAME -> Color(0xFF3B82F6)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // 项目状态指示器
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (game.assignedEmployees.isNotEmpty()) 
                            Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (game.assignedEmployees.isNotEmpty()) 
                                Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (game.assignedEmployees.isNotEmpty()) 
                                Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (game.assignedEmployees.isNotEmpty()) "进行中" else "待分配",
                            color = if (game.assignedEmployees.isNotEmpty()) 
                                Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 已分配员工信息
            if (game.assignedEmployees.isNotEmpty()) {
                Text(
                    text = "已分配员工 (${game.assignedEmployees.size}人):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                game.assignedEmployees.take(3).forEach { employee ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${employee.name} (${employee.position})",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "技能: ${employee.getPrimarySkillValue()}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
                
                if (game.assignedEmployees.size > 3) {
                    Text(
                        text = "还有 ${game.assignedEmployees.size - 3} 名员工...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 开发进度
                val progress = (game.assignedEmployees.size * 0.2f).coerceAtMost(1.0f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "开发进度",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFF10B981),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 分配按钮区域 - 新增智能分配选项
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 智能分配按钮（新功能）
                EnhancedOneClickAssignmentButton(
                    projects = listOf(game),
                    employees = availableEmployees,
                    onAssignmentComplete = { result ->
                        // 处理智能分配结果
                        result.assignments.forEach { (projectName, employees) ->
                            if (projectName == game.name) {
                                onEmployeeAssigned(game, employees)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = if (game.assignedEmployees.isEmpty()) 
                        "🎯 智能分配最佳员工" else "🎯 重新智能分配"
                )
                
                // 原有的手动分配按钮
                OutlinedButton(
                    onClick = { showOriginalDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (game.assignedEmployees.isEmpty()) 
                            "手动选择员工" else "手动重新分配",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
    
    // 原有的员工分配对话框
    if (showOriginalDialog) {
        EmployeeAssignmentDialog(
            game = game,
            availableEmployees = availableEmployees,
            onDismiss = { showOriginalDialog = false },
            onAssignEmployees = { selectedEmployees ->
                onEmployeeAssigned(game, selectedEmployees)
                showOriginalDialog = false
            }
        )
    }
}

/**
 * 批量智能分配组件
 * 为多个项目同时进行智能分配
 */
@Composable
fun BatchEnhancedAssignmentCard(
    projects: List<Game>,
    availableEmployees: List<Employee>,
    onBatchAssignmentComplete: (EnhancedAssignmentResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "批量智能分配",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "为所有项目一键分配最佳员工",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "待分配项目",
                    value = projects.count { it.assignedEmployees.isEmpty() }.toString(),
                    icon = Icons.Default.Assignment,
                    color = Color(0xFFF59E0B)
                )
                
                StatCard(
                    title = "可用员工",
                    value = availableEmployees.size.toString(),
                    icon = Icons.Default.Group,
                    color = Color(0xFF3B82F6)
                )
                
                StatCard(
                    title = "总项目数",
                    value = projects.size.toString(),
                    icon = Icons.Default.Folder,
                    color = Color(0xFF10B981)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 批量分配按钮
            EnhancedOneClickAssignmentButton(
                projects = projects,
                employees = availableEmployees,
                onAssignmentComplete = onBatchAssignmentComplete,
                modifier = Modifier.fillMaxWidth(),
                text = "🚀 批量智能分配所有项目"
            )
        }
    }
}

/**
 * 统计卡片组件
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}