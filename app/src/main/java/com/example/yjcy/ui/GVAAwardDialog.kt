package com.example.yjcy.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.*
import com.example.yjcy.ui.components.SingleLineText
import com.example.yjcy.ui.components.MultiLineText
import kotlinx.coroutines.delay

/**
 * GVA游戏大奖颁奖典礼对话框
 * 
 * 12月31日颁奖时弹出，展示所有奖项获奖者
 * 包含视觉动效和玩家获奖高亮
 */
@Composable
fun GVAAwardDialog(
    year: Int,
    nominations: List<AwardNomination>,
    playerWonCount: Int,
    playerTotalReward: Long,
    playerFansGain: Int,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) }
    
    // 入场动画
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
        delay(300)
        showContent = true
    }
    
    // 背景缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // 背景透明度动画
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = alpha * 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .scale(scale),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1a1a2e)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 顶部标题区域（带渐变背景）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0f3460),
                                        Color(0xFF1a1a2e)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 动画奖杯图标
                            AnimatedTrophy(isVisible = showContent)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            AnimatedVisibility(
                                visible = showContent,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    SingleLineText(
                                        text = "🏆 GVA游戏大奖",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SingleLineText(
                                        text = "${year}年度获奖名单",
                                        fontSize = 18.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 玩家获奖统计卡片
                    if (playerWonCount > 0 && showContent) {
                        PlayerAwardSummary(
                            wonCount = playerWonCount,
                            totalReward = playerTotalReward,
                            fansGain = playerFansGain
                        )
                    }
                    
                    // 标签页
                    if (showContent) {
                        PrimaryScrollableTabRow(
                            selectedTabIndex = currentTab,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color(0xFF16213e),
                            contentColor = Color.White
                        ) {
                            Tab(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                text = { SingleLineText("综合类", fontSize = 14.sp) }
                            )
                            Tab(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                text = { SingleLineText("主题类", fontSize = 14.sp) }
                            )
                            Tab(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                text = { SingleLineText("特殊成就", fontSize = 14.sp) }
                            )
                        }
                    }
                    
                    // 奖项列表
                    if (showContent) {
                        val filteredNominations = when (currentTab) {
                            0 -> nominations.filter { it.award.category == AwardCategory.GENERAL }
                            1 -> nominations.filter { it.award.category == AwardCategory.THEME }
                            2 -> nominations.filter { it.award.category == AwardCategory.SPECIAL }
                            else -> emptyList()
                        }
                        
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredNominations) { nomination ->
                                if (nomination.winner != null) {
                                    AwardItemCard(nomination)
                                }
                            }
                        }
                    }
                    
                    // 底部按钮
                    if (showContent) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0f3460)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            SingleLineText(
                                text = "关闭",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 动画奖杯图标
 */
@Composable
private fun AnimatedTrophy(isVisible: Boolean) {
    var isAnimating by remember { mutableStateOf(false) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(200)
            isAnimating = true
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "trophy_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 0f else -180f,
        animationSpec = tween(600, easing = EaseOutBack),
        label = "trophy_rotation"
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        // 发光效果背景
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color(0xFFFFD700).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(40.dp)
                )
        )
        
        SingleLineText(
            text = "🏆",
            fontSize = 48.sp,
            modifier = Modifier.offset(y = rotation.dp)
        )
    }
}

/**
 * 玩家获奖统计卡片
 */
@Composable
private fun PlayerAwardSummary(
    wonCount: Int,
    totalReward: Long,
    fansGain: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(500)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SingleLineText(
                    text = "🎉 恭喜获得 $wonCount 个奖项！",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RewardItem(
                        icon = "💰",
                        label = "奖金",
                        value = formatMoney(totalReward),
                        color = Color(0xFFFFD700)
                    )
                    RewardItem(
                        icon = "👥",
                        label = "粉丝",
                        value = "+$fansGain",
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

/**
 * 奖励项
 */
@Composable
private fun RewardItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SingleLineText(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        SingleLineText(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        SingleLineText(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * 奖项卡片
 */
@Composable
private fun AwardItemCard(nomination: AwardNomination) {
    val winner = nomination.winner ?: return
    val isPlayerWon = winner.isPlayerGame
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isPlayerWon) {
                    Color(0xFF4CAF50).copy(alpha = 0.15f)
                } else {
                    Color(0xFF16213e)
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 奖项图标和名称
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SingleLineText(
                            text = nomination.award.icon,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SingleLineText(
                            text = nomination.award.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 获奖游戏
                    SingleLineText(
                        text = "《${winner.gameName}》",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlayerWon) Color(0xFF4CAF50) else Color.White
                    )
                    
                    // 公司名称
                    SingleLineText(
                        text = winner.companyName,
                        fontSize = 12.sp,
                        color = if (isPlayerWon) Color(0xFF4CAF50).copy(alpha = 0.8f) else Color.Gray
                    )
                    
                    // 评分
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SingleLineText(
                            text = "⭐开发质量 ${String.format("%.1f", winner.rating)}",
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SingleLineText(
                            text = "📊综合得分 ${String.format("%.1f", winner.totalScore)}",
                            fontSize = 11.sp,
                            color = Color(0xFF64B5F6)
                        )
                    }
                }
                
                // 玩家获奖标记
                if (isPlayerWon) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF4CAF50),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        SingleLineText(
                            text = "🏆",
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 格式化金额
 */
private fun formatMoney(amount: Long): String {
    return when {
        amount >= 1000000 -> "¥${String.format("%.1f", amount / 1000000.0)}M"
        amount >= 1000 -> "¥${String.format("%.1f", amount / 1000.0)}K"
        else -> "¥$amount"
    }
}
