package com.example.yjcy.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.yjcy.data.DevelopmentPhase
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
    refreshTrigger: Int = 0,  // 新增：用于触发UI刷新的参数
    onSwitchToCurrentProjects: (() -> Unit)? = null,
    onReleaseGame: ((Game) -> Unit)? = null,  // 新增：发售游戏回调
    onAbandonGame: ((Game) -> Unit)? = null,  // 新增：废弃游戏回调
    onPurchaseServer: ((Game, com.example.yjcy.data.ServerType) -> Unit)? = null,  // 新增：购买服务器回调
    showDataOverview: Boolean = true,  // 新增：是否显示数据概览（正在更新标签页设为false）
    money: Long = 0L,  // 新增：资金
    onMoneyUpdate: (Long) -> Unit = {},  // 新增：资金更新回调
    currentYear: Int = 1,  // 新增：当前年份
    currentMonth: Int = 1,  // 新增：当前月份
    currentDay: Int = 1,  // 新增：当前日期
    onPauseGame: (() -> Unit)? = null,  // 暂停游戏的回调
    onResumeGame: (() -> Unit)? = null, // 恢复游戏的回调
    isSupporterUnlocked: Boolean = false, // 是否解锁支持者功能
    onShowFeatureLockedDialog: () -> Unit = {}, // 显示功能解锁对话框的回调
    onShowAutoUpdateInfoDialog: (Game) -> Unit = {} // 显示自动更新提示对话框的回调
) {
    var showRevenueDialog by remember { mutableStateOf(false) }
    var showPlayerInterestInfoDialog by remember { mutableStateOf(false) }
    
    // 检查游戏是否已发售（只有RELEASED状态才算真正发售）
    val isReleased = game.releaseStatus == GameReleaseStatus.RELEASED
    
    // 检查游戏是否已下架
    val isRemoved = game.releaseStatus == GameReleaseStatus.REMOVED_FROM_MARKET
    
    // 检查是否准备发售（包含READY_FOR_RELEASE和RATED状态）
    // RATED状态表示已评分但未发售，需要玩家手动点击"发售"按钮
    val isReadyForRelease = game.releaseStatus == GameReleaseStatus.READY_FOR_RELEASE || 
                           game.releaseStatus == GameReleaseStatus.RATED
    
    // 检查是否正在开发中（未完成、未发售也未下架）
    val isDeveloping = !isReleased && !isRemoved && !isReadyForRelease
    
    // 当 refreshTrigger 改变时，强制重新获取收益数据（确保实时更新）
    val gameRevenue by remember(game.id, refreshTrigger) {
        derivedStateOf { 
            if (isReleased || isRemoved) RevenueManager.getGameRevenue(game.id) else null
        }
    }
    
    // 检查是否有进行中的更新任务
    val updateTask = gameRevenue?.updateTask
    val hasActiveUpdateTask = updateTask != null && updateTask.progressPoints < updateTask.requiredPoints
    
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
            Column(modifier = Modifier.fillMaxWidth()) {
                // GVA获奖图标（如果有，显示在游戏名字上方）
                if (game.awards.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        game.awards.take(3).forEach { award ->
                            Text(
                                text = award.icon,
                                fontSize = 16.sp
                            )
                        }
                        if (game.awards.size > 3) {
                            Text(
                                text = "+${game.awards.size - 3}",
                                fontSize = 10.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // 游戏名字和状态标签在同一行
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = game.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 项目状态指示器
                    if (isDeveloping) {
                        // 开发中的游戏状态
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (game.assignedEmployees.isNotEmpty()) 
                                    Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (game.assignedEmployees.isNotEmpty()) 
                                        Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (game.assignedEmployees.isNotEmpty()) 
                                        Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (game.assignedEmployees.isNotEmpty()) "进行中" else "待分配",
                                    color = if (game.assignedEmployees.isNotEmpty()) 
                                        Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (isReleased) {
                        // 已发售的游戏显示版本号
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "📦",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "版本V${String.format("%.1f", game.version)}",
                                    color = Color(0xFF8B5CF6),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // 显示更新状态
                        if (hasActiveUpdateTask) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "🔄",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "正在更新",
                                        color = Color(0xFF3B82F6),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 游戏信息标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 平台显示优化：多个平台时只显示第一个+数量
                    val platformText = if (game.platforms.size <= 1) {
                        game.platforms.joinToString(", ") { it.displayName }
                    } else {
                        "${game.platforms.first().displayName}+${game.platforms.size - 1}"
                    }
                    
                    Text(
                        text = "${game.theme.displayName} • $platformText",
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
                    
                    // 宣传指数显示
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "宣传指数：${(game.promotionIndex * 100).toInt()}%",
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 开发阶段要求说明（仅开发中的游戏显示）
            if (isDeveloping) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = game.currentPhase.icon,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "当前阶段：${game.currentPhase.displayName}",
                                color = Color(0xFF3B82F6),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = game.currentPhase.description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 所需职位
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "所需职位：",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            game.currentPhase.requiredPositions.forEach { position ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF10B981).copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = position,
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 更新内容列表（仅在正在更新标签页显示）
            if (hasActiveUpdateTask && !showDataOverview) {
                updateTask?.let { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🔄 更新内容",
                                    color = Color(0xFF3B82F6),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                task.features.forEach { feature ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "•",
                                                color = Color(0xFF3B82F6),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = feature,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            // 已分配员工信息（开发中始终显示，更新中仅在正在更新标签页显示）
            if (isDeveloping || (hasActiveUpdateTask && !showDataOverview)) {
                if (game.assignedEmployees.isNotEmpty()) {
                    // 已分配员工
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
                } else {
                    // 未分配员工 - 显示提示
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (hasActiveUpdateTask) "未分配员工 - 请点击下方按钮分配员工进行更新工作" else "未分配员工 - 请点击下方按钮分配员工进行开发",
                            color = Color(0xFFEF4444).copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 进度显示：开发进度或更新进度（开发中或更新中都显示）
            if ((isDeveloping && game.assignedEmployees.isNotEmpty()) || (hasActiveUpdateTask && !showDataOverview)) {
                // 进度显示：开发进度或更新进度
                val actualProgress = if (hasActiveUpdateTask) {
                    // 更新任务进度
                    updateTask?.let { it.progressPoints.toFloat() / it.requiredPoints.toFloat() } ?: 0f
                } else {
                    // 开发进度
                    game.developmentProgress
                }
                
                // 添加进度动画
                val animatedProgress by animateFloatAsState(
                    targetValue = actualProgress,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "progress_animation"
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (hasActiveUpdateTask) "更新进度" else "开发进度",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            // 更新任务特征显示
                            if (hasActiveUpdateTask) {
                                updateTask?.let { task ->
                                    Text(
                                        text = "· ${task.features.size}项内容",
                                        color = Color(0xFF3B82F6),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // 百分比标签样式
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasActiveUpdateTask) 
                                    Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                color = if (hasActiveUpdateTask) Color(0xFF3B82F6) else Color(0xFF10B981),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 自定义进度条样式
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        // 渐变进度条
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = if (hasActiveUpdateTask) listOf(
                                            Color(0xFF3B82F6),
                                            Color(0xFF2563EB),
                                            Color(0xFF1D4ED8)
                                        ) else listOf(
                                            Color(0xFF10B981),
                                            Color(0xFF059669),
                                            Color(0xFF047857)
                                        )
                                    )
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 收益信息显示区域（根据 showDataOverview 参数控制，已下架的游戏不显示）
            if (showDataOverview && !isRemoved) {
                gameRevenue?.let { revenue ->
                    // 使用derivedStateOf确保实时更新统计数据
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
                                text = if (game.businessModel == BusinessModel.ONLINE_GAME) "💰 数据概览" else "💰 收益概览",
                                color = Color(0xFF10B981),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 收益统计行
                            if (game.businessModel == BusinessModel.ONLINE_GAME) {
                            // 网络游戏：显示4列（总注册、总活跃、当前状态、玩家兴趣）
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 总注册
                                Column {
                                    Text(
                                        text = "总注册",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${formatMoneyWithDecimals(statistics.totalSales.toDouble())}",
                                        color = Color(0xFF10B981),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // 总活跃（带趋势箭头）
                                Column {
                                    Text(
                                        text = "总活跃",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    
                                    val activePlayers = RevenueManager.getActivePlayers(game.id)
                                    val playerInterest = revenue.playerInterest
                                    
                                    // 根据兴趣值确定趋势
                                    val trendIcon = when {
                                        playerInterest >= 70.0 -> "" // 正常，不显示箭头
                                        playerInterest >= 50.0 -> "↘" // 小幅下降
                                        else -> "↓" // 大幅下降
                                    }
                                    
                                    val trendColor = when {
                                        playerInterest >= 70.0 -> Color.Green
                                        playerInterest >= 50.0 -> Color(0xFFFFA500) // 橙色
                                        else -> Color.Red
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when {
                                                activePlayers >= 1_000_000 -> "${activePlayers / 1_000_000}M"
                                                activePlayers >= 1_000 -> "${activePlayers / 1_000}K"
                                                else -> "$activePlayers"
                                            },
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        if (trendIcon.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            // 添加动画效果
                                            val infiniteTransition = rememberInfiniteTransition(label = "trend_animation")
                                            val animatedOffset by infiniteTransition.animateFloat(
                                                initialValue = 0f,
                                                targetValue = if (playerInterest < 50.0) 6f else 3f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(
                                                        durationMillis = if (playerInterest < 50.0) 600 else 1000,
                                                        easing = FastOutSlowInEasing
                                                    ),
                                                    repeatMode = RepeatMode.Reverse
                                                ),
                                                label = "trend_offset"
                                            )
                                            
                                            Box(
                                                modifier = Modifier.offset(y = animatedOffset.dp)
                                            ) {
                                                Text(
                                                    text = trendIcon,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = trendColor
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // 当前状态（运营/已下架）
                                Column {
                                    Text(
                                        text = "当前状态",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (revenue.isActive) "运营" else "已下架",
                                        color = if (revenue.isActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // 玩家兴趣（新增指标）
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "玩家兴趣",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { showPlayerInterestInfoDialog = true },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.HelpOutline,
                                                contentDescription = "玩家兴趣说明",
                                                tint = Color.White.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    val interestPercentage = String.format("%.0f%%", revenue.playerInterest)
                                    val interestColor = when {
                                        revenue.playerInterest >= 70.0 -> Color(0xFF10B981) // 绿色
                                        revenue.playerInterest >= 50.0 -> Color(0xFFFFA500) // 橙色
                                        else -> Color(0xFFEF4444) // 红色
                                    }
                                    
                                    Text(
                                        text = interestPercentage,
                                        color = interestColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // 单机游戏：显示3列（总收益、总销量、在售状态）
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
            }
            }
            
            // 按钮区域
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 有更新任务的游戏
                if (hasActiveUpdateTask) {
                    // 分配员工按钮（仅在正在更新标签页显示，不在数据概览模式显示）
                    if (!showDataOverview) {
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
                                "一键分配员工" else "重新分配员工",
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            onPauseGame = onPauseGame,
                            onResumeGame = onResumeGame
                        )
                    }
                    
                    // 如果已发售，显示收益报告和社区按钮（在数据概览模式也显示）
                    if (isReleased) {
                        var showCommunityDialog by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 收益报告按钮
                            Button(
                                onClick = { showRevenueDialog = true },
                                modifier = Modifier.weight(1f),
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "收益报告",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // 游戏社区按钮
                            Button(
                                onClick = { showCommunityDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B82F6)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🎮",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "游戏社区",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // 游戏社区对话框
                        if (showCommunityDialog) {
                            GameCommunityDialog(
                                game = game,
                                onDismiss = { showCommunityDialog = false },
                                onCommentLike = { updateIndex, commentId ->
                                    // 处理评论点赞
                                    val updatedHistory = (game.updateHistory ?: emptyList()).toMutableList()
                                    if (updateIndex in updatedHistory.indices) {
                                        val update = updatedHistory[updateIndex]
                                        val updatedComments = update.comments.map { comment ->
                                            if (comment.id == commentId && !comment.isLikedByUser) {
                                                comment.copy(
                                                    likes = comment.likes + 1,
                                                    isLikedByUser = true
                                                )
                                            } else {
                                                comment
                                            }
                                        }
                                        updatedHistory[updateIndex] = update.copy(comments = updatedComments)
                                        onGameUpdate(game.copy(
                                            updateHistory = updatedHistory,
                                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                                        ))
                                    }
                                }
                            )
                        }
                    }
                } else if (isReleased || isRemoved) {
                    // 已发售或已下架的游戏（无更新任务）：显示收益按钮和游戏社区按钮
                    // 如果有更新历史，显示并排按钮；否则只显示收益按钮
                    // 注意：已下架的游戏如果没有收益数据，则不显示任何按钮（无法重新上架）
                    if (isRemoved && gameRevenue == null) {
                        // 已下架但没有收益数据，不显示任何按钮（游戏已永久下架）
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        // 有收益数据或已发售的游戏，显示收益报告和游戏社区按钮
                        // 如果有更新历史，显示并排按钮；否则只显示收益按钮
                        if (!game.updateHistory.isNullOrEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 收益报告按钮
                                Button(
                                    onClick = { showRevenueDialog = true },
                                    modifier = Modifier.weight(1f),
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
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "收益报告",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // 游戏社区按钮
                                var showCommunityDialog by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showCommunityDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🎮",
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "游戏社区",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // 游戏社区对话框
                                if (showCommunityDialog) {
                                    GameCommunityDialog(
                                        game = game,
                                        onDismiss = { showCommunityDialog = false },
                                        onCommentLike = { updateIndex, commentId ->
                                            // 处理评论点赞
                                            val updatedHistory = (game.updateHistory ?: emptyList()).toMutableList()
                                            if (updateIndex in updatedHistory.indices) {
                                                val update = updatedHistory[updateIndex]
                                                val updatedComments = update.comments.map { comment ->
                                                    if (comment.id == commentId && !comment.isLikedByUser) {
                                                        comment.copy(
                                                            likes = comment.likes + 1,
                                                            isLikedByUser = true
                                                        )
                                                    } else {
                                                        comment
                                                    }
                                                }
                                                updatedHistory[updateIndex] = update.copy(comments = updatedComments)
                                                onGameUpdate(game.copy(
                                            updateHistory = updatedHistory,
                                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                                        ))
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            // 没有更新历史，显示收益报告和社区按钮
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 收益报告按钮
                                Button(
                                    onClick = { showRevenueDialog = true },
                                    modifier = Modifier.weight(1f),
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
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "收益报告",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // 游戏社区按钮
                                var showCommunityDialog by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showCommunityDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🎮",
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "游戏社区",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // 游戏社区对话框
                                if (showCommunityDialog) {
                                    GameCommunityDialog(
                                        game = game,
                                        onDismiss = { showCommunityDialog = false },
                                        onCommentLike = { updateIndex, commentId ->
                                            // 处理评论点赞
                                            val updatedHistory = (game.updateHistory ?: emptyList()).toMutableList()
                                            if (updateIndex in updatedHistory.indices) {
                                                val update = updatedHistory[updateIndex]
                                                val updatedComments = update.comments.map { comment ->
                                                    if (comment.id == commentId && !comment.isLikedByUser) {
                                                        comment.copy(
                                                            likes = comment.likes + 1,
                                                            isLikedByUser = true
                                                        )
                                                    } else {
                                                        comment
                                                    }
                                                }
                                                updatedHistory[updateIndex] = update.copy(comments = updatedComments)
                                                onGameUpdate(game.copy(
                                            updateHistory = updatedHistory,
                                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                                        ))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (isReadyForRelease) {
                    // 游戏开发完成，准备发售 - 显示发售和废弃两个按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 废弃项目按钮
                        OutlinedButton(
                            onClick = { onAbandonGame?.invoke(game) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🗑️ 废弃",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // 发售游戏按钮
                        Button(
                            onClick = { onReleaseGame?.invoke(game) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "发售",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (isDeveloping) {
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
                            "一键分配员工" else "重新分配员工",
                        currentYear = currentYear,
                        currentMonth = currentMonth,
                        currentDay = currentDay,
                        onPauseGame = onPauseGame,
                        onResumeGame = onResumeGame
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
                game = game,
                onDismiss = { showRevenueDialog = false },
                onRemoveFromMarket = { gameId ->
                    // 处理下架游戏逻辑
                    RevenueManager.removeGameFromMarket(gameId)
                    // 更新游戏状态为下架
                    val updatedGame = game.copy(
                        releaseStatus = GameReleaseStatus.REMOVED_FROM_MARKET,
                        allDevelopmentEmployees = game.allDevelopmentEmployees
                    )
                    onGameUpdate(updatedGame)
                    showRevenueDialog = false
                },
                onStartUpdate = {
                    // 关闭收益弹窗，回到项目卡片界面，便于分配员工
                    showRevenueDialog = false
                    // 不再自动跳转到"当前项目"列表
                },
                onMonetizationUpdate = { updatedItems ->
                    // 更新游戏的付费内容配置
                    val updatedGame = game.copy(
                        monetizationItems = updatedItems,
                        allDevelopmentEmployees = game.allDevelopmentEmployees
                    )
                    onGameUpdate(updatedGame)
                    // 同步更新 RevenueManager 中的游戏信息
                    RevenueManager.updateGameInfo(
                        game.id,
                        game.businessModel,
                        updatedItems
                    )
                },
                onPurchaseServer = { serverType ->
                    // 购买服务器
                    onPurchaseServer?.invoke(game, serverType)
                },
                onAutoUpdateToggle = { enabled ->
                    // 更新自动更新开关状态
                    val updatedGame = game.copy(
                        autoUpdate = enabled,
                        allDevelopmentEmployees = game.allDevelopmentEmployees
                    )
                    onGameUpdate(updatedGame)
                },
                onPriceChange = { newPrice ->
                    // 更新游戏价格
                    val updatedGame = game.copy(
                        releasePrice = newPrice.toFloat(),
                        allDevelopmentEmployees = game.allDevelopmentEmployees
                    )
                    onGameUpdate(updatedGame)
                },
                businessModel = game.businessModel,
                money = money,
                onMoneyUpdate = onMoneyUpdate,
                isSupporterUnlocked = isSupporterUnlocked,
                onShowFeatureLockedDialog = onShowFeatureLockedDialog,
                onShowAutoUpdateInfoDialog = { onShowAutoUpdateInfoDialog(game) }
            )
        }
    }
    
    // 显示玩家兴趣说明对话框
    if (showPlayerInterestInfoDialog) {
        PlayerInterestInfoDialog(
            onDismiss = { showPlayerInterestInfoDialog = false }
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
    modifier: Modifier = Modifier,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    onPauseGame: (() -> Unit)? = null,
    onResumeGame: (() -> Unit)? = null
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
                text = "🚀 批量智能分配所有项目",
                currentYear = currentYear,
                currentMonth = currentMonth,
                currentDay = currentDay,
                onPauseGame = onPauseGame,
                onResumeGame = onResumeGame
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

/**
 * 游戏宣传对话框
 */
@Composable
fun GamePromotionDialog(
    game: Game,
    money: Long,
    onDismiss: () -> Unit,
    onPromote: (Int) -> Unit // 宣传投入金额
) {
    var investmentAmount by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(0) } // 0=小规模, 1=中等规模, 2=大规模
    
    // 宣传选项配置（费用已大幅上调）
    val promotionOptions = listOf(
        PromotionOption("小规模宣传", 100000, 0.2f, "社交媒体、论坛推广"),
        PromotionOption("中等规模宣传", 300000, 0.4f, "游戏展会、媒体评测"),
        PromotionOption("大规模宣传", 1000000, 0.8f, "电视广告、网络推广、明星代言")
    )
    
    // 当前宣传指数
    val currentIndex = (game.promotionIndex * 100).toInt()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Column {
                Text(
                    text = "📢 游戏宣传",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.name,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 当前宣传指数显示
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "当前宣传指数",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$currentIndex%",
                                color = Color(0xFFF59E0B),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 进度条
                        LinearProgressIndicator(
                            progress = { game.promotionIndex },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = Color(0xFFF59E0B),
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
                
                // 说明文字
                Text(
                    text = "提升宣传指数可以增加游戏的首发销量或注册数",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                
                // 宣传选项
                promotionOptions.forEachIndexed { index, option ->
                    val canAfford = money >= option.cost
                    val newIndex = minOf(100, currentIndex + (option.indexIncrease * 100).toInt())
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { 
                            if (canAfford && currentIndex < 100) {
                                selectedOption = index
                                investmentAmount = option.cost
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                currentIndex >= 100 -> Color.Gray.copy(alpha = 0.2f)
                                selectedOption == index -> Color(0xFFF59E0B).copy(alpha = 0.3f)
                                canAfford -> Color.White.copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.2f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedOption == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentIndex >= 100 || !canAfford) Color.Gray else Color.White
                                    )
                                    Text(
                                        text = option.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "¥${formatMoneyWithDecimals(option.cost.toDouble())}",
                                        fontSize = 13.sp,
                                        color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "+${(option.indexIncrease * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                            
                            // 显示预期结果
                            if (selectedOption == index && currentIndex < 100) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "预期指数：",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "$currentIndex% → $newIndex%",
                                        fontSize = 12.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 效果说明
                if (currentIndex < 100) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "宣传指数达到100%时效果最佳！\n单机游戏：提升首发销量\n网络游戏：提升首发总注册",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "宣传指数已达到100%，无需继续投入！",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onPromote(investmentAmount)
                    onDismiss()
                },
                enabled = investmentAmount > 0 && money >= investmentAmount && currentIndex < 100,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("确认宣传", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White)
            }
        }
    )
}

/**
 * 宣传选项数据类
 */
data class PromotionOption(
    val name: String,
    val cost: Int,
    val indexIncrease: Float, // 增加的宣传指数（0-1）
    val description: String
)

/**
 * 玩家兴趣说明对话框
 */
@Composable
fun PlayerInterestInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "玩家兴趣值系统说明",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. 初始状态
                SectionTitle("📊 初始状态")
                InfoText("• 游戏刚上线时：兴趣值 = 100%")
                InfoText("• 生命周期进度 = 0%（基于上线天数/365天）")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 2. 自然衰减机制
                SectionTitle("⏱️ 自然衰减机制（每90天）")
                InfoText("每隔 90天（3个月）自动衰减一次，衰减率根据生命周期进度决定：")
                Spacer(modifier = Modifier.height(8.dp))
                
                DecayStageCard("🌱 成长期", "0-30% (0-109天)", "-8%", "轻微衰减", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(4.dp))
                DecayStageCard("🌿 成熟期", "30-70% (110-255天)", "-15%", "正常衰减", Color(0xFF3B82F6))
                Spacer(modifier = Modifier.height(4.dp))
                DecayStageCard("🍂 衰退期", "70-90% (256-328天)", "-25%", "加速衰减", Color(0xFFFFA500))
                Spacer(modifier = Modifier.height(4.dp))
                DecayStageCard("⚰️ 末期", "90%+ (329天+)", "-35%", "快速衰减", Color(0xFFEF4444))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 3. 更新游戏恢复机制
                SectionTitle("🔄 更新游戏恢复机制")
                WarningText("⚠️ 到了末期，无论做什么都无法恢复兴趣值！")
                Spacer(modifier = Modifier.height(8.dp))
                
                RecoveryStageCard("🌱 成长期", "0-30%", "+25%", "恢复效果最好", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(4.dp))
                RecoveryStageCard("🌿 成熟期", "30-70%", "+15%", "恢复效果一般", Color(0xFF3B82F6))
                Spacer(modifier = Modifier.height(4.dp))
                RecoveryStageCard("🍂 衰退期", "70-90%", "+8%", "恢复效果较差", Color(0xFFFFA500))
                Spacer(modifier = Modifier.height(4.dp))
                RecoveryStageCard("⚰️ 末期", "90%+", "0%", "无法恢复", Color(0xFFEF4444))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 4. 兴趣值影响
                SectionTitle("📉 兴趣值对游戏的影响")
                InfoText("兴趣值会影响活跃玩家数和新玩家增长率：")
                Spacer(modifier = Modifier.height(8.dp))
                
                ImpactCard("≥ 70%", "活跃玩家 100%", "新玩家增长 +15%", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(4.dp))
                ImpactCard("50-70%", "活跃玩家 70%", "新玩家增长 -15%", Color(0xFF3B82F6))
                Spacer(modifier = Modifier.height(4.dp))
                ImpactCard("30-50%", "活跃玩家 40%", "新玩家增长 -30%", Color(0xFFFFA500))
                Spacer(modifier = Modifier.height(4.dp))
                ImpactCard("< 30%", "活跃玩家 20%", "新玩家增长 -50%", Color(0xFFEF4444))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 5. 策略建议
                SectionTitle("💡 策略建议")
                StrategyText("0-109天（成长期）", "积极更新，恢复+25%", Color(0xFF10B981))
                StrategyText("110-255天（成熟期）", "定期更新，恢复+15%", Color(0xFF3B82F6))
                StrategyText("256-328天（衰退期）", "最后冲刺，恢复+8%", Color(0xFFFFA500))
                StrategyText("329天后（末期）", "💀 放弃挣扎，准备新游戏", Color(0xFFEF4444))
                
                Spacer(modifier = Modifier.height(8.dp))
                WarningText("⚠️ 关键节点：第329天后（约11个月），游戏进入末期，兴趣值持续衰减至0%，无法挽回！")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                Text("我知道了", color = Color.White)
            }
        }
    )
}

// 辅助组件
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun WarningText(text: String) {
    Text(
        text = text,
        color = Color(0xFFFFA500),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp
    )
}

@Composable
private fun DecayStageCard(
    stage: String,
    progress: String,
    decay: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stage,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = progress,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
        Text(
            text = decay,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecoveryStageCard(
    stage: String,
    progress: String,
    recovery: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stage,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = progress,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
        Text(
            text = recovery,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ImpactCard(
    range: String,
    activeEffect: String,
    growthEffect: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = range,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(3f)
        ) {
            Text(
                text = activeEffect,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
            Text(
                text = growthEffect,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StrategyText(period: String, strategy: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $period：",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1.5f),
            maxLines = 1
        )
        Text(
            text = strategy,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1.5f),
            maxLines = 1
        )
    }
}