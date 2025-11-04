package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.utils.formatMoneyWithDecimals

/**
 * 宣传中心页面内容
 */
@Composable
fun PromotionCenterContent(
    games: List<Game>,
    money: Long,
    fans: Long,
    onMoneyUpdate: (Long) -> Unit,
    onFansUpdate: (Long) -> Unit,
    onGamesUpdate: (List<Game>) -> Unit,
    autoPromotionThreshold: Float = 0.5f,
    isSupporterUnlocked: Boolean = false, // 是否解锁支持者功能
    onShowFeatureLockedDialog: () -> Unit = {} // 显示功能解锁对话框的回调
) {
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var showPromotionDialog by remember { mutableStateOf(false) }
    
    // 筛选可宣传的游戏（开发中、准备发售、已上线等）
    val releasedGames = remember(games) {
        games.filter { 
            it.releaseStatus == GameReleaseStatus.DEVELOPMENT ||
            it.releaseStatus == GameReleaseStatus.READY_FOR_RELEASE ||
            it.releaseStatus == GameReleaseStatus.PRICE_SETTING ||
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
                            value = formatMoneyWithDecimals(fans.toDouble()),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                            text = "暂无营销",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
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
        // 获取最新的游戏数据（支持实时更新）
        val currentGame = remember(games, selectedGame) {
            games.find { it.id == selectedGame!!.id } ?: selectedGame!!
        }
        
        PromotionTypeDialog(
            game = currentGame,
            money = money,
            onDismiss = { 
                showPromotionDialog = false
                selectedGame = null
            },
            onPromote = { promotionType ->
                onMoneyUpdate(money - promotionType.cost)
                onFansUpdate(fans + promotionType.fansGain)
                
                // 更新游戏的宣传指数
                val updatedGames = games.map { game ->
                    if (game.id == selectedGame!!.id) {
                        val newPromotionIndex = (game.promotionIndex + promotionType.promotionIndexGain).coerceAtMost(1.0f)
                        game.copy(
                            promotionIndex = newPromotionIndex,
                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                        )
                    } else {
                        game
                    }
                }
                onGamesUpdate(updatedGames)
                
                // 不再关闭对话框，允许连续宣传
                // showPromotionDialog = false
                // selectedGame = null
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
    val fansGain: Long,
    val promotionIndexGain: Float, // 宣传指数增益（0-1之间）
    val icon: String
) {
    SOCIAL_MEDIA(
        displayName = "社交媒体推广",
        description = "在各大社交平台发布游戏内容",
        cost = 30000L,
        fansGain = 300L,
        promotionIndexGain = 0.05f,
        icon = "📱"
    ),
    VIDEO_AD(
        displayName = "视频广告",
        description = "制作精美的游戏宣传视频",
        cost = 200000L,
        fansGain = 2500L,
        promotionIndexGain = 0.12f,
        icon = "🎬"
    ),
    GAME_EXPO(
        displayName = "游戏展会",
        description = "参加游戏展会展示作品",
        cost = 500000L,
        fansGain = 7000L,
        promotionIndexGain = 0.20f,
        icon = "🎪"
    ),
    TV_COMMERCIAL(
        displayName = "电视广告",
        description = "在电视黄金时段投放广告",
        cost = 1000000L,
        fansGain = 16000L,
        promotionIndexGain = 0.30f,
        icon = "📺"
    ),
    CELEBRITY_ENDORSEMENT(
        displayName = "名人代言",
        description = "邀请知名人士为游戏代言",
        cost = 3000000L,
        fansGain = 50000L,
        promotionIndexGain = 0.45f,
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
    fans: Long,
    autoPromotionThreshold: Float = 0.5f,
    onDismiss: () -> Unit,
    onMoneyUpdate: (Long) -> Unit,
    onFansUpdate: (Long) -> Unit,
    onGamesUpdate: (List<Game>) -> Unit,
    onAutoPromotionThresholdUpdate: (Float) -> Unit = {},
    isSupporterUnlocked: Boolean = false, // 是否解锁支持者功能
    onShowFeatureLockedDialog: () -> Unit = {} // 显示功能解锁对话框的回调
) {
    var selectedGameIds by remember { mutableStateOf(emptySet<String>()) }
    var showBatchPromotionDialog by remember { mutableStateOf(false) }
    var showAutoPromotionSettings by remember { mutableStateOf(false) }
    var localThreshold by remember { mutableStateOf(autoPromotionThreshold) }
    // 自动宣传设置对话框中选中的游戏ID
    var autoPromotionSelectedGameIds by remember { mutableStateOf(emptySet<String>()) }
    
    // 筛选可宣传的游戏（开发中、准备发售、已上线等）
    val releasedGames = remember(games) {
        games.filter { 
            it.releaseStatus == GameReleaseStatus.DEVELOPMENT ||
            it.releaseStatus == GameReleaseStatus.READY_FOR_RELEASE ||
            it.releaseStatus == GameReleaseStatus.PRICE_SETTING ||
            it.releaseStatus == GameReleaseStatus.RELEASED || 
            it.releaseStatus == GameReleaseStatus.RATED
        }
    }
    
    // 是否全选
    val isAllSelected = selectedGameIds.size == releasedGames.size && releasedGames.isNotEmpty()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📢 宣传中心",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "选择要宣传的游戏（已选${selectedGameIds.size}个）",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // 全选/反选按钮
                    if (releasedGames.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                selectedGameIds = if (isAllSelected) {
                                    emptySet()
                                } else {
                                    releasedGames.map { it.id }.toSet()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF10B981)
                            )
                        ) {
                            Icon(
                                imageVector = if (isAllSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAllSelected) "反选" else "全选",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
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
                            text = "暂无营销",
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
                            val isSelected = selectedGameIds.contains(game.id)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            // 点击卡片切换选中状态
                                            selectedGameIds = if (isSelected) {
                                                selectedGameIds - game.id
                                            } else {
                                                selectedGameIds + game.id
                                            }
                                        },
                                        // 移除长按功能，改为在自动宣传设置界面选择
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        Color(0xFF10B981).copy(alpha = 0.2f)
                                    } else {
                                        Color.White.copy(alpha = 0.1f)
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
                                    // 复选框
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            selectedGameIds = if (isSelected) {
                                                selectedGameIds - game.id
                                            } else {
                                                selectedGameIds + game.id
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF10B981),
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                    
                                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = game.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            // 显示自动宣传状态
                                            if (game.autoPromotion) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF10B981).copy(alpha = 0.3f)
                                                ) {
                                                    Text(
                                                        text = "🤖 自动",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFF10B981),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
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
                                            // 显示宣传指数
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFEAB308).copy(alpha = 0.3f)
                                            ) {
                                                Text(
                                                    text = "宣传${(game.promotionIndex * 100).toInt()}%",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFEAB308),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 自动宣传设置按钮
                    TextButton(
                        onClick = { 
                            if (!isSupporterUnlocked) {
                                onShowFeatureLockedDialog()
                            } else {
                                // 初始化已开启自动宣传的游戏ID列表
                                autoPromotionSelectedGameIds = releasedGames.filter { it.autoPromotion }.map { it.id }.toSet()
                                showAutoPromotionSettings = true
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⚙️ 自动宣传设置", fontSize = 12.sp)
                            if (!isSupporterUnlocked) {
                                Text("🔒", fontSize = 10.sp)
                            }
                        }
                    }
                    
                    // 一键宣传按钮
                    if (selectedGameIds.isNotEmpty()) {
                        Button(
                            onClick = {
                                showBatchPromotionDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("一键宣传(${selectedGameIds.size})", color = Color.White)
                        }
                    }
                    
                    TextButton(onClick = onDismiss) {
                        Text("关闭", color = Color.White)
                    }
                }
            }
        }
    )
    
    // 自动宣传设置对话框
    if (showAutoPromotionSettings) {
        AlertDialog(
            onDismissRequest = { showAutoPromotionSettings = false },
            containerColor = Color(0xFF1F2937),
            title = {
                Text(
                    text = "⚙️ 自动宣传设置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "选择要开启自动宣传的游戏，设置宣传指数阈值。当游戏宣传指数低于阈值时，系统会自动进行宣传。",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                    
                    // 阈值设置
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "宣传指数阈值：${(localThreshold * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = localThreshold,
                            onValueChange = { localThreshold = it },
                            valueRange = 0f..1f,
                            steps = 19, // 0%, 5%, 10%, ..., 100%
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0%", fontSize = 10.sp, color = Color.Gray)
                            Text("100%", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    
                    // 游戏选择列表
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "选择游戏（已选${autoPromotionSelectedGameIds.size}个）：",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            TextButton(
                                onClick = {
                                    if (autoPromotionSelectedGameIds.size == releasedGames.size) {
                                        // 全部取消
                                        autoPromotionSelectedGameIds = emptySet()
                                    } else {
                                        // 全选
                                        autoPromotionSelectedGameIds = releasedGames.map { it.id }.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    text = if (autoPromotionSelectedGameIds.size == releasedGames.size) "取消全选" else "全选",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 游戏列表（可滚动）
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(releasedGames) { game ->
                                val isSelected = autoPromotionSelectedGameIds.contains(game.id)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            autoPromotionSelectedGameIds = if (isSelected) {
                                                autoPromotionSelectedGameIds - game.id
                                            } else {
                                                autoPromotionSelectedGameIds + game.id
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            Color(0xFF10B981).copy(alpha = 0.2f)
                                        } else {
                                            Color.White.copy(alpha = 0.05f)
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = game.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "🎮",
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = when (game.businessModel) {
                                                        BusinessModel.ONLINE_GAME -> "网游"
                                                        BusinessModel.SINGLE_PLAYER -> "单机"
                                                    },
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                                game.gameRating?.let { rating ->
                                                    Text(
                                                        text = "⭐ ${String.format("%.1f", rating.finalScore)}",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFEAB308).copy(alpha = 0.3f)
                                                ) {
                                                    Text(
                                                        text = "宣传${(game.promotionIndex * 100).toInt()}%",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFFEAB308),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 更新阈值
                        onAutoPromotionThresholdUpdate(localThreshold)
                        
                        // 更新选中游戏的自动宣传状态
                        val updatedGames = games.map { game ->
                            if (autoPromotionSelectedGameIds.contains(game.id)) {
                                // 选中的游戏开启自动宣传
                                game.copy(
                                    autoPromotion = true,
                                    allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                                )
                            } else {
                                // 未选中的游戏关闭自动宣传
                                game.copy(
                                    autoPromotion = false,
                                    allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                                )
                            }
                        }
                        onGamesUpdate(updatedGames)
                        
                        showAutoPromotionSettings = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text("保存", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    localThreshold = autoPromotionThreshold // 重置为原值
                    // 重置选中的游戏列表（使用games而不是releasedGames，因为releasedGames在lambda中可能不可访问）
                    autoPromotionSelectedGameIds = games.filter { 
                        it.autoPromotion && (
                            it.releaseStatus == GameReleaseStatus.DEVELOPMENT ||
                            it.releaseStatus == GameReleaseStatus.READY_FOR_RELEASE ||
                            it.releaseStatus == GameReleaseStatus.PRICE_SETTING ||
                            it.releaseStatus == GameReleaseStatus.RELEASED ||
                            it.releaseStatus == GameReleaseStatus.RATED
                        )
                    }.map { it.id }.toSet()
                    showAutoPromotionSettings = false 
                }) {
                    Text("取消", color = Color.White)
                }
            }
        )
    }
    
    // 单个游戏宣传类型选择对话框（已移除，使用批量宣传）
    
    // 批量宣传对话框
    if (showBatchPromotionDialog && selectedGameIds.isNotEmpty()) {
        BatchPromotionTypeDialog(
            selectedGameIds = selectedGameIds,
            games = games,
            money = money,
            onDismiss = { 
                showBatchPromotionDialog = false
            },
            onPromote = { promotionType ->
                val selectedGamesCount = selectedGameIds.size
                val totalCost = promotionType.cost * selectedGamesCount
                val totalFansGain = promotionType.fansGain * selectedGamesCount
                
                // 检查是否有足够的钱
                if (money >= totalCost) {
                    onMoneyUpdate(money - totalCost)
                    onFansUpdate(fans + totalFansGain)
                    
                    // 更新所有选中游戏的宣传指数
                    val updatedGames = games.map { game ->
                        if (selectedGameIds.contains(game.id)) {
                            val newPromotionIndex = (game.promotionIndex + promotionType.promotionIndexGain).coerceAtMost(1.0f)
                            game.copy(
                            promotionIndex = newPromotionIndex,
                            allDevelopmentEmployees = game.allDevelopmentEmployees ?: emptyList()
                        )
                        } else {
                            game
                        }
                    }
                    onGamesUpdate(updatedGames)
                    
                    // 不关闭对话框，允许连续宣传
                    // selectedGameIds = emptySet()
                    // showBatchPromotionDialog = false
                }
            }
        )
    }
}

/**
 * 批量宣传对话框
 */
@Composable
fun BatchPromotionTypeDialog(
    selectedGameIds: Set<String>,
    games: List<Game>,
    money: Long,
    onDismiss: () -> Unit,
    onPromote: (PromotionType) -> Unit
) {
    var selectedType by remember { mutableStateOf(PromotionType.SOCIAL_MEDIA) }
    
    val selectedGames = remember(selectedGameIds, games) {
        games.filter { selectedGameIds.contains(it.id) }
    }
    
    val selectedGamesCount = selectedGames.size
    val totalCost = selectedType.cost.toLong() * selectedGamesCount.toLong()
    val canAfford = money >= totalCost
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Column {
                Text(
                    text = "📢 批量宣传",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "将对 $selectedGamesCount 个游戏进行宣传",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 选中的游戏列表预览
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📋 选中的游戏：",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedGames.take(3).forEach { game ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${game.name}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEAB308).copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "宣传指数：${(game.promotionIndex * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        color = Color(0xFFEAB308),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        if (selectedGames.size > 3) {
                            Text(
                                text = "... 及其他 ${selectedGames.size - 3} 个游戏",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // 宣传类型选择
                Text(
                    text = "选择宣传类型：",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                LazyColumn(
                    modifier = Modifier.height(250.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PromotionType.values().toList()) { promotionType ->
                        val typeTotalCost = promotionType.cost.toLong() * selectedGamesCount.toLong()
                        val typeCanAfford = money >= typeTotalCost
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { if (typeCanAfford) selectedType = promotionType },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    selectedType == promotionType -> Color(0xFF10B981).copy(alpha = 0.3f)
                                    typeCanAfford -> Color.White.copy(alpha = 0.1f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = promotionType.icon,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = promotionType.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedType == promotionType) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.White
                                        )
                                        Text(
                                            text = promotionType.description,
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "总费用：¥${formatMoneyWithDecimals(typeTotalCost.toDouble())}",
                                        fontSize = 10.sp,
                                        color = if (typeCanAfford) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "总粉丝：+${formatMoneyWithDecimals((promotionType.fansGain * selectedGamesCount).toDouble())}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF3B82F6),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "指数：+${(promotionType.promotionIndexGain * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        color = Color(0xFFEAB308),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (!canAfford) {
                    Text(
                        text = "资金不足（还需 ¥${formatMoneyWithDecimals((totalCost - money).toDouble())}）",
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Button(
                    onClick = { 
                        if (canAfford) {
                            onPromote(selectedType)
                        }
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text("开始批量宣传", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White)
            }
        }
    )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = game.name,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEAB308).copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "宣传指数：${(game.promotionIndex * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color(0xFFEAB308),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
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
                                Text(
                                    text = "宣传指数：+${(promotionType.promotionIndexGain * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    color = Color(0xFFEAB308),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isMaxPromotion = game.promotionIndex >= 1.0f
            val canPromote = money >= selectedType.cost && !isMaxPromotion
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isMaxPromotion) {
                    Text(
                        text = "宣传指数已达最大值",
                        fontSize = 11.sp,
                        color = Color(0xFFEAB308),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Button(
                    onClick = { 
                        onPromote(selectedType)
                    },
                    enabled = canPromote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text("开始宣传", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White)
            }
        }
    )
}
