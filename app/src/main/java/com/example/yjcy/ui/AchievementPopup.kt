package com.example.yjcy.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.example.yjcy.data.Achievement
import kotlinx.coroutines.delay

/**
 * 成就解锁弹窗
 * 带有动画效果的居中弹窗
 */
@Composable
fun AchievementUnlockPopup(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    // 动画状态
    var visible by remember { mutableStateOf(false) }
    var iconScale by remember { mutableFloatStateOf(0f) }
    
    // 启动动画
    LaunchedEffect(Unit) {
        visible = true
        // 图标缩放动画
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { value, _ ->
            iconScale = value
        }
        
        // 移除自动关闭，玩家必须点击才能关闭
    }
    
    // 弹窗内容动画
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200)
        )
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .wrapContentHeight()
                        .clickable { onDismiss() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 渐变背景标题区域
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFFFA500)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🏆",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "成就解锁",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 成就图标（带缩放动画）
                        Text(
                            text = achievement.icon,
                            fontSize = 72.sp,
                            modifier = Modifier.scale(iconScale)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 成就名称
                        Text(
                            text = achievement.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 成就描述
                        Text(
                            text = achievement.description,
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 类别标签
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = getCategoryColor(achievement.category).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = achievement.category.icon,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = achievement.category.displayName,
                                    fontSize = 14.sp,
                                    color = getCategoryColor(achievement.category),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // 闪烁的提示文字
                        BlinkingText(
                            text = "点击任意位置关闭",
                            color = Color(0xFF999999),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取类别颜色
 */
private fun getCategoryColor(category: com.example.yjcy.data.AchievementCategory): Color {
    return when (category) {
        com.example.yjcy.data.AchievementCategory.COMPANY -> Color(0xFF4CAF50)
        com.example.yjcy.data.AchievementCategory.SINGLE_GAME -> Color(0xFF2196F3)
        com.example.yjcy.data.AchievementCategory.ONLINE_GAME -> Color(0xFF9C27B0)
        com.example.yjcy.data.AchievementCategory.EMPLOYEE -> Color(0xFFFF9800)
    }
}

/**
 * 闪烁的文字
 */
@Composable
fun BlinkingText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Text(
        text = text,
        fontSize = fontSize,
        color = color.copy(alpha = alpha),
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * 多个成就连续解锁时的队列管理
 */
@Composable
fun AchievementPopupQueue(
    achievements: List<Achievement>,
    onAllDismissed: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    
    if (currentIndex < achievements.size) {
        AchievementUnlockPopup(
            achievement = achievements[currentIndex],
            onDismiss = {
                currentIndex++
                if (currentIndex >= achievements.size) {
                    onAllDismissed()
                }
            }
        )
    }
}
