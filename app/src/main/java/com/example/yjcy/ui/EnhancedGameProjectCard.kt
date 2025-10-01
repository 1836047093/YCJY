package com.example.yjcy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.EnhancedAssignmentResult
import com.example.yjcy.data.Game
import com.example.yjcy.data.Employee
import com.example.yjcy.data.GameReleaseStatus
import com.example.yjcy.data.RevenueManager
import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.utils.formatMoneyWithDecimals

/**
 * 增强版游戏项目卡片
 * 集成了新的智能分配功能，不修改原有代码
 */
@Composable
fun EnhancedGameProjectCard(
    game: Game,
    availableEmployees: List<Employee>,
    onEmployeeAssigned: (Game, List<Employee>) -> Unit,
    onGameUpdate: (Game) -> Unit = {},
    modifier: Modifier = Modifier,
    refreshTrigger: Int = 0  // 新增：用于触发UI刷新的参数
) {
    var showRevenueDialog by remember { mutableStateOf(false) }
    
    // 检查游戏是否已发售
    val isReleased = game.releaseStatus == GameReleaseStatus.RELEASED || game.releaseStatus == GameReleaseStatus.RATED
    
    // 当 refreshTrigger 改变时，强制重新获取收益数据
    val gameRevenue by remember(game.id, refreshTrigger) {
        derivedStateOf { 
            if (isReleased) RevenueManager.getGameRevenue(game.id) else null
        }
    }
    
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
                                    BusinessModel.ONLINE_GAME -> Color(0xFF10B981)
                                }.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = game.businessModel.displayName,
                                color = when (game.businessModel) {
                                    BusinessModel.SINGLE_PLAYER -> Color(0xFF8B5CF6)
                                    BusinessModel.ONLINE_GAME -> Color(0xFF10B981)
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
            
            // 已分配员工信息（仅对开发中的游戏显示）
            if (!isReleased && game.assignedEmployees.isNotEmpty()) {
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
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
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
                            text = "${employee.getSpecialtySkillType()}技能：${employee.getSpecialtySkillLevel()}级",
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
                
                // 开发进度 - 与实际逻辑保持一致（仅对开发中的游戏显示）
                // 计算员工技能总和
                val totalSkillPoints = game.assignedEmployees.sumOf { employee ->
                    employee.skillDevelopment + employee.skillDesign +
                    employee.skillArt + employee.skillMusic + employee.skillService
                }

                // 基础进度增长：每天3%，根据员工技能调整
                val skillMultiplier = (totalSkillPoints / 25f).coerceAtLeast(0.1f)
                val actualProgress = game.developmentProgress

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
                            text = "${(actualProgress * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LinearProgressIndicator(
                        progress = { actualProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFF10B981),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    
                    // 显示技能效率提示
                    if (game.assignedEmployees.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "技能效率: ${skillMultiplier.toInt()}x (技能点: $totalSkillPoints)",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 收益信息显示区域（仅对已发售游戏）
            gameRevenue?.let { revenue ->
                val statistics = remember(revenue) {
                    RevenueManager.calculateStatistics(revenue)
                }
                
                // 收益概览卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "💰 收益概览",
                            color = Color(0xFF10B981),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 收益统计行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "总收益",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "¥${formatMoneyWithDecimals(statistics.totalRevenue)}",
                                    color = Color(0xFF10B981),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "总销量",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${formatMoneyWithDecimals(statistics.totalSales.toDouble())}份",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "在售状态",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (revenue.isActive) "在售" else "已下架",
                                    color = if (revenue.isActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 按钮区域
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 如果游戏已发售，显示收益按钮
                if (isReleased) {
                    Button(
                        onClick = { showRevenueDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📊 查看详细收益报告",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // 开发中的游戏显示分配按钮
                    // 智能分配按钮（新功能）
                    EnhancedOneClickAssignmentButton(
                        projects = listOf(game),
                        employees = availableEmployees,
                        onAssignmentComplete = { result ->
                            // 处理智能分配结果
                            result.assignments.forEach { (projectId, employees) ->
                                if (projectId == game.id) {
                                    onEmployeeAssigned(game, employees)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        text = if (game.assignedEmployees.isEmpty()) 
                            "一键分配员工" else "重新分配员工"
                    )
                }
            }
        }
    }
    
    // 显示收益对话框
    if (showRevenueDialog) {
        gameRevenue?.let { revenue ->
            GameRevenueDialog(
                gameRevenue = revenue,
                onDismiss = { showRevenueDialog = false },
                onRemoveFromMarket = { gameId ->
                    // 处理下架游戏逻辑
                    RevenueManager.removeGameFromMarket(gameId)
                    // 更新游戏状态为下架
                    val updatedGame = game.copy(
                        releaseStatus = GameReleaseStatus.REMOVED_FROM_MARKET
                    )
                    onGameUpdate(updatedGame)
                    showRevenueDialog = false
                },
                onRelistGame = { gameId ->
                    // 处理重新上架游戏逻辑
                    RevenueManager.relistGame(gameId)
                    // 更新游戏状态为重新上架
                    val updatedGame = game.copy(
                        releaseStatus = GameReleaseStatus.RELEASED
                    )
                    onGameUpdate(updatedGame)
                    showRevenueDialog = false
                }
            )
        }
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
                    icon = Icons.AutoMirrored.Filled.Assignment,
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