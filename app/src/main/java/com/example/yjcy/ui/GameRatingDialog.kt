package com.example.yjcy.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.GameRating
import com.example.yjcy.data.SkillContribution
import kotlin.math.roundToInt

/**
 * 游戏评分展示对话框
 */
@Composable
fun GameRatingDialog(
    gameRating: GameRating,
    gameName: String,
    onDismiss: () -> Unit
) {
    // 动画状态
    var animationStarted by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "rating_animation"
    )
    
    LaunchedEffect(Unit) {
        animationStarted = true
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // 标题栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 游戏评分",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    // 游戏名称
                    Text(
                        text = gameName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    // 评分圆环和星级
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        // 圆环进度条
                        CircularProgressIndicator(
                            progress = (gameRating.finalScore / 10f) * animationProgress,
                            modifier = Modifier.size(180.dp),
                            strokeWidth = 12.dp,
                            color = getRatingColor(gameRating.finalScore),
                            trackColor = Color(0xFFE5E7EB)
                        )
                        
                        // 中心评分显示
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%.1f", gameRating.finalScore * animationProgress),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = getRatingColor(gameRating.finalScore)
                            )
                            Text(
                                text = "/ 10.0",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 星级显示
                            Row {
                                repeat(5) { index ->
                                    val starProgress = ((gameRating.finalScore / 2f) - index).coerceIn(0f, 1f)
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "星级",
                                        tint = if (starProgress * animationProgress > 0.5f) {
                                            Color(0xFFFBBF24)
                                        } else {
                                            Color(0xFFE5E7EB)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Text(
                                text = getRatingDescription(gameRating.finalScore),
                                fontSize = 14.sp,
                                color = getRatingColor(gameRating.finalScore),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                item {
                    // 评分构成详情
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "📊 评分构成",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 基础分
                            ScoreBreakdownItem(
                                label = "基础分",
                                score = gameRating.baseScore,
                                maxScore = 10f,
                                color = Color(0xFF6B7280),
                                animationProgress = animationProgress
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 技能加成
                            ScoreBreakdownItem(
                                label = "技能加成",
                                score = gameRating.skillBonus,
                                maxScore = 5f,
                                color = Color(0xFF10B981),
                                animationProgress = animationProgress
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Divider(color = Color(0xFFE5E7EB))
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 最终得分
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "最终得分",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = String.format("%.1f", gameRating.finalScore * animationProgress),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getRatingColor(gameRating.finalScore)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 关闭按钮
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A)
                        )
                    ) {
                        Text(
                            text = "✨ 太棒了！",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 评分构成项目组件
 */
@Composable
private fun ScoreBreakdownItem(
    label: String,
    score: Float,
    maxScore: Float,
    color: Color,
    animationProgress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
            Text(
                text = String.format("%.1f", score * animationProgress),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 进度条
        LinearProgressIndicator(
            progress = (score / maxScore) * animationProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFFE5E7EB)
        )
    }
}

/**
 * 员工贡献卡片组件
 */
@Composable
private fun EmployeeContributionCard(
    contribution: SkillContribution,
    animationProgress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 员工头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getSkillColor(contribution.skillType)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "员工",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 员工信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contribution.employeeName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = "${contribution.skillType} Lv.${contribution.skillLevel}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            // 贡献值
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "+${String.format("%.1f", contribution.contribution * animationProgress)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
                Text(
                    text = "贡献",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 获取评分对应的颜色
 */
private fun getRatingColor(rating: Float): Color {
    return when {
        rating >= 8.0f -> Color(0xFF10B981) // 绿色 - 优秀
        rating >= 6.0f -> Color(0xFFFBBF24) // 黄色 - 良好
        rating >= 4.0f -> Color(0xFFF59E0B) // 橙色 - 一般
        else -> Color(0xFFEF4444) // 红色 - 较差
    }
}

/**
 * 获取评分描述
 */
private fun getRatingDescription(rating: Float): String {
    return when {
        rating >= 9.0f -> "神作"
        rating >= 8.0f -> "优秀"
        rating >= 7.0f -> "良好"
        rating >= 6.0f -> "不错"
        rating >= 5.0f -> "一般"
        rating >= 4.0f -> "较差"
        else -> "糟糕"
    }
}

/**
 * 获取技能类型对应的颜色
 */
private fun getSkillColor(skillType: String): Color {
    return when (skillType) {
        "开发" -> Color(0xFF3B82F6) // 蓝色
        "设计" -> Color(0xFF8B5CF6) // 紫色
        "美工" -> Color(0xFFEC4899) // 粉色
        "音乐" -> Color(0xFF10B981) // 绿色
        "服务" -> Color(0xFFF59E0B) // 橙色
        else -> Color(0xFF6B7280) // 灰色
    }
}