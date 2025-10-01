package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.Game
import com.example.yjcy.data.PriceRecommendation
import com.example.yjcy.data.PriceRecommendationEngine
import kotlin.math.roundToInt

/**
 * 游戏发售价格设置对话框
 */
@Composable
fun GameReleaseDialog(
    game: Game,
    onDismiss: () -> Unit,
    onConfirmRelease: (Float) -> Unit
) {
    var userInputPrice by remember { mutableStateOf("") }
    var isValidPrice by remember { mutableStateOf(false) }
    val priceRecommendation = remember { PriceRecommendationEngine.calculateRecommendedPrice(game) }
    
    // 验证价格输入
    LaunchedEffect(userInputPrice) {
        val price = userInputPrice.toFloatOrNull()
        isValidPrice = price != null && price >= 0f && price <= 1000f
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎮 游戏发售",
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
                
                // 游戏信息卡片
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
                            text = game.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(
                                text = "主题: ${game.theme.displayName}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "平台: ${game.platforms.joinToString { it.displayName }}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "商业模式: ${game.businessModel.displayName}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 市场建议卡片
                if (game.businessModel == BusinessModel.SINGLE_PLAYER || game.businessModel == BusinessModel.ONLINE_GAME) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEF3C7)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "市场建议",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "💡 市场建议",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 推荐价格
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "推荐价格:",
                                    fontSize = 14.sp,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "¥${priceRecommendation.recommendedPrice.roundToInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 价格区间
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "价格区间:",
                                    fontSize = 14.sp,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "¥${priceRecommendation.priceRange.minPrice.roundToInt()} - ¥${priceRecommendation.priceRange.maxPrice.roundToInt()}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 市场分析
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "分析",
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = priceRecommendation.marketAnalysis,
                                        fontSize = 13.sp,
                                        color = Color(0xFF374151),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // 价格输入区域
                if (game.businessModel == BusinessModel.SINGLE_PLAYER || game.businessModel == BusinessModel.ONLINE_GAME) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💰 设置发售价格",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = userInputPrice,
                            onValueChange = { userInputPrice = it },
                            label = { Text("价格 (元)") },
                            placeholder = { Text("请输入发售价格") },
                            leadingIcon = {
                                Text(
                                    text = "¥",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = userInputPrice.isNotEmpty() && !isValidPrice,
                            supportingText = {
                                if (userInputPrice.isNotEmpty() && !isValidPrice) {
                                    Text(
                                        text = "请输入有效的价格 (0-1000元)",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        text = "建议价格: ¥${priceRecommendation.recommendedPrice.roundToInt()}",
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 快速选择价格按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                priceRecommendation.priceRange.minPrice,
                                priceRecommendation.recommendedPrice,
                                priceRecommendation.priceRange.maxPrice
                            ).forEach { price ->
                                OutlinedButton(
                                    onClick = { userInputPrice = price.roundToInt().toString() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "¥${price.roundToInt()}",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // 确认发售按钮
                val finalPrice = userInputPrice.toFloatOrNull() ?: 0f
                
                val canRelease = isValidPrice && userInputPrice.isNotEmpty()
                
                Button(
                    onClick = { onConfirmRelease(finalPrice) },
                    enabled = canRelease,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text(
                        text = "🚀 确认发售 (¥${finalPrice.roundToInt()})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 取消按钮
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "取消",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}