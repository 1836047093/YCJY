package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.Game
import com.example.yjcy.data.GameReleaseStatus
import com.example.yjcy.formatMoneyWithDecimals

/**
 * 宣传中心页面内容
 */
@Composable
fun PromotionCenterContent(
    games: List<Game>,
    money: Long,
    fans: Int,
    onMoneyUpdate: (Long) -> Unit,
    onFansUpdate: (Int) -> Unit
) {
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var showPromotionDialog by remember { mutableStateOf(false) }
    
    // 筛选已上线的游戏（包括单机和网游）
    val releasedGames = remember(games) {
        games.filter { 
            it.releaseStatus == GameReleaseStatus.RELEASED || 
            it.releaseStatus == GameReleaseStatus.RATED
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),  // 深蓝色
                        Color(0xFF4A148C)   // 深紫色
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题和总览
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📢 宣传中心",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PromotionOverviewItem(
                            label = "当前粉丝",
                            value = "${fans / 1000}K",
                            icon = "👥"
                        )
                        PromotionOverviewItem(
                            label = "可宣传游戏",
                            value = "${releasedGames.size}",
                            icon = "🎮"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "通过宣传可以增加游戏的曝光度和粉丝数量",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 游戏列表
            if (releasedGames.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无已发售的游戏",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(releasedGames) { game ->
                        PromotionGameCard(
                            game = game,
                            onPromote = {
                                selectedGame = game
                                showPromotionDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 宣传对话框
    if (showPromotionDialog && selectedGame != null) {
        PromotionTypeDialog(
            game = selectedGame!!,
            money = money,
            onDismiss = { 
                showPromotionDialog = false
                selectedGame = null
            },
            onPromote = { promotionType ->
                onMoneyUpdate(money - promotionType.cost)
                onFansUpdate(fans + promotionType.fansGain)
                
                showPromotionDialog = false
                selectedGame = null
            }
        )
    }
}

@Composable
fun PromotionOverviewItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun PromotionGameCard(
    game: Game,
    onPromote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PromotionInfoChip(
                        icon = "🎮",
                        text = when (game.businessModel) {
                            BusinessModel.ONLINE_GAME -> "网游"
                            BusinessModel.SINGLE_PLAYER -> "单机"
                        }
                    )
                    game.gameRating?.let { rating ->
                        PromotionInfoChip(
                            icon = "⭐",
                            text = String.format("%.1f", rating.finalScore)
                        )
                    }
                }
            }
            
            Button(
                onClick = onPromote,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "宣传",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PromotionInfoChip(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 14.sp
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * 宣传类型
 */
enum class PromotionType(
    val displayName: String,
    val description: String,
    val cost: Long,
    val fansGain: Int,
    val icon: String
) {
    SOCIAL_MEDIA(
        displayName = "社交媒体推广",
        description = "在各大社交平台发布游戏内容",
        cost = 10000L,
        fansGain = 500,
        icon = "📱"
    ),
    VIDEO_AD(
        displayName = "视频广告",
        description = "制作精美的游戏宣传视频",
        cost = 50000L,
        fansGain = 2000,
        icon = "🎬"
    ),
    GAME_EXPO(
        displayName = "游戏展会",
        description = "参加游戏展会展示作品",
        cost = 100000L,
        fansGain = 5000,
        icon = "🎪"
    ),
    TV_COMMERCIAL(
        displayName = "电视广告",
        description = "在电视黄金时段投放广告",
        cost = 500000L,
        fansGain = 20000,
        icon = "📺"
    ),
    CELEBRITY_ENDORSEMENT(
        displayName = "名人代言",
        description = "邀请知名人士为游戏代言",
        cost = 1000000L,
        fansGain = 50000,
        icon = "⭐"
    )
}

/**
 * 宣传中心对话框（从项目管理打开的版本）
 */
@Composable
fun PromotionCenterDialog(
    games: List<Game>,
    money: Long,
    fans: Int,
    onDismiss: () -> Unit,
    onMoneyUpdate: (Long) -> Unit,
    onFansUpdate: (Int) -> Unit
) {
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var showPromotionTypeDialog by remember { mutableStateOf(false) }
    
    // 筛选已上线的游戏
    val releasedGames = remember(games) {
        games.filter { 
            it.releaseStatus == GameReleaseStatus.RELEASED || 
            it.releaseStatus == GameReleaseStatus.RATED
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Column {
                Text(
                    text = "📢 宣传中心",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "选择要宣传的游戏",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                if (releasedGames.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无已发售的游戏",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(releasedGames) { game ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    selectedGame = game
                                    showPromotionTypeDialog = true
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = game.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = when (game.businessModel) {
                                                    BusinessModel.ONLINE_GAME -> "🎮 网游"
                                                    BusinessModel.SINGLE_PLAYER -> "🎮 单机"
                                                },
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            game.gameRating?.let { rating ->
                                                Text(
                                                    text = "⭐ ${String.format("%.1f", rating.finalScore)}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                    
                                    Text(
                                        text = "📢",
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = Color.White)
            }
        }
    )
    
    // 宣传类型选择对话框
    if (showPromotionTypeDialog && selectedGame != null) {
        PromotionTypeDialog(
            game = selectedGame!!,
            money = money,
            onDismiss = { 
                showPromotionTypeDialog = false
                selectedGame = null
            },
            onPromote = { promotionType ->
                onMoneyUpdate(money - promotionType.cost)
                onFansUpdate(fans + promotionType.fansGain)
                
                showPromotionTypeDialog = false
                selectedGame = null
                onDismiss()
            }
        )
    }
}

@Composable
fun PromotionTypeDialog(
    game: Game,
    money: Long,
    onDismiss: () -> Unit,
    onPromote: (PromotionType) -> Unit
) {
    var selectedType by remember { mutableStateOf(PromotionType.SOCIAL_MEDIA) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Column {
                Text(
                    text = "📢 宣传游戏",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = game.name,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PromotionType.values().toList()) { promotionType ->
                    val canAfford = money >= promotionType.cost
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { if (canAfford) selectedType = promotionType },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                selectedType == promotionType -> Color(0xFF10B981).copy(alpha = 0.3f)
                                canAfford -> Color.White.copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.2f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = promotionType.icon,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = promotionType.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedType == promotionType) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                    Text(
                                        text = promotionType.description,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "费用：¥${formatMoneyWithDecimals(promotionType.cost.toDouble())}",
                                    fontSize = 11.sp,
                                    color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "粉丝：+${promotionType.fansGain}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onPromote(selectedType)
                },
                enabled = money >= selectedType.cost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("开始宣传", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White)
            }
        }
    )
}
