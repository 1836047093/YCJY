package com.example.yjcy.ui.esports

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.TopInfoBar
import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.EsportsPlayer
import com.example.yjcy.managers.esports.HeroManager
import com.example.yjcy.managers.esports.PlayerManager
import com.example.yjcy.ui.components.SingleLineText
import kotlinx.coroutines.delay

/**
 * 战队管理主界面（全屏布局，参考子公司管理样式）
 */
@Composable
fun TeamManagementScreen(
    onNavigateBack: () -> Unit,
    // TopInfoBar参数
    money: Long = 0,
    fans: Long = 0,
    year: Int = 1,
    month: Int = 1,
    day: Int = 1,
    gameSpeed: Int = 1,
    onSpeedChange: (Int) -> Unit = {},
    onPauseToggle: () -> Unit = {},
    isPaused: Boolean = false,
    onSettingsClick: () -> Unit = {},
    isSupporterUnlocked: Boolean = false,
    onShowFeatureLockedDialog: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRecruitDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF7C3AED)
                    )
                )
            )
    ) {
        // 顶部状态栏
        TopInfoBar(
            money = money,
            fans = fans,
            year = year,
            month = month,
            day = day,
            gameSpeed = gameSpeed,
            onSpeedChange = onSpeedChange,
            onPauseToggle = onPauseToggle,
            isPaused = isPaused,
            onSettingsClick = onSettingsClick,
            isSupporterUnlocked = isSupporterUnlocked,
            onShowFeatureLockedDialog = onShowFeatureLockedDialog
        )
        
        // 主内容区（深色背景）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a2e))
        ) {
            // 顶部标题栏
            TeamTopBar(onBack = onNavigateBack)
            
            // 标签页
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color(0xFF16213e),
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { SingleLineText(text = "战队阵容", fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { SingleLineText(text = "青训营", fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { SingleLineText(text = "全部选手", fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { SingleLineText(text = "英雄图鉴", fontSize = 14.sp) }
                )
            }
            
            // 内容区域
            when (selectedTab) {
                0 -> TeamRosterTab()
                1 -> RecruitmentTab(
                    onRecruit = { showRecruitDialog = true }
                )
                2 -> AllPlayersTab()
                3 -> HeroEncyclopediaTab()
            }
        }
    }
    
    // 招募对话框
    if (showRecruitDialog) {
        RecruitResultDialog(
            onDismiss = { showRecruitDialog = false }
        )
    }
}

/**
 * 顶部标题栏
 */
@Composable
private fun TeamTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0f3460))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            SingleLineText(text = "← 返回", color = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        SingleLineText(
            text = "⚽ 战队管理",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}

/**
 * 战队阵容Tab
 */
@Composable
fun TeamRosterTab() {
    val myTeam = PlayerManager.myTeam
    
    if (myTeam.isEmpty()) {
        // 空状态
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SingleLineText(
                    text = "⚽",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                SingleLineText(
                    text = "暂无战队成员",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleLineText(
                    text = "前往青训营招募选手",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 按位置分组显示
            HeroPosition.values().forEach { position ->
                val playersInPosition = myTeam.filter { it.position == position }
                if (playersInPosition.isNotEmpty()) {
                    item {
                        val posName = when(position) {
                            HeroPosition.TOP -> "上单"
                            HeroPosition.JUNGLE -> "打野"
                            HeroPosition.MID -> "中单"
                            HeroPosition.ADC -> "ADC"
                            HeroPosition.SUPPORT -> "辅助"
                        }
                        SingleLineText(
                            text = "━━━ $posName ━━━",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(playersInPosition) { player ->
                        PlayerCard(player = player, isMyTeam = true)
                    }
                }
            }
        }
    }
}

/**
 * 青训营Tab
 */
@Composable
fun RecruitmentTab(
    onRecruit: () -> Unit
) {
    val myTeamSize = PlayerManager.myTeam.size
    val isTeamFull = myTeamSize >= 7
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 说明卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213e)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SingleLineText(
                    "🎓 青训营招募",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                SingleLineText(
                    "从青训营招募新选手加入战队",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                SingleLineText(
                    "当前战队人数: $myTeamSize / 7",
                    fontSize = 14.sp,
                    color = if (isTeamFull) Color(0xFFFF5252) else Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 品质概率说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213e)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SingleLineText(
                    "📊 招募概率",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                RarityProbabilityRow("SSR", "0.1%", Color(0xFFFF9800))
                RarityProbabilityRow("S", "0.9%", Color(0xFF9C27B0))
                RarityProbabilityRow("A", "4%", Color(0xFF2196F3))
                RarityProbabilityRow("B", "15%", Color(0xFF4CAF50))
                RarityProbabilityRow("C", "80%", Color(0xFFBDBDBD))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 战队满员提示
        if (isTeamFull) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5252).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleLineText(
                        text = "⚠️",
                        fontSize = 24.sp
                    )
                    Column {
                        SingleLineText(
                            text = "战队已满员",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                        SingleLineText(
                            text = "请先解约部分选手再招募",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // 招募按钮
        Button(
            onClick = onRecruit,
            enabled = !isTeamFull,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            SingleLineText(
                if (isTeamFull) "战队已满（7/7）" else "🎯 招募选手",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RarityProbabilityRow(rarity: String, probability: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleLineText(
            text = rarity,
            fontSize = 14.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
        SingleLineText(
            text = probability,
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}

/**
 * 全部选手Tab
 */
@Composable
fun AllPlayersTab() {
    val allPlayers = PlayerManager.players
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(allPlayers) { player ->
            PlayerCard(
                player = player,
                isMyTeam = PlayerManager.myTeam.contains(player),
                onSignPlayer = {
                    if (PlayerManager.signPlayer(player.id)) {
                        refreshTrigger++
                    }
                },
                onReleasePlayer = {
                    if (PlayerManager.releasePlayer(player.id)) {
                        refreshTrigger++
                    }
                }
            )
        }
    }
}

/**
 * 选手卡片
 */
@Composable
fun PlayerCard(
    player: EsportsPlayer,
    isMyTeam: Boolean,
    onSignPlayer: (() -> Unit)? = null,
    onReleasePlayer: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, player.rarity.color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 顶部：姓名和品质
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = player.rarity.emoji,
                        fontSize = 20.sp
                    )
                    Text(
                        text = player.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = player.rarity.color
                    )
                }
                
                if (isMyTeam) {
                    Text(
                        text = "✓ 我的战队",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // 位置和年龄
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SingleLineText(
                    text = "位置: ${player.positionDisplayName}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                SingleLineText(
                    text = "年龄: ${player.age}岁",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 属性
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AttributeBar("操作", player.attributes.mechanics)
                AttributeBar("意识", player.attributes.awareness)
                AttributeBar("团队", player.attributes.teamwork)
                AttributeBar("心态", player.attributes.mentality)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 英雄池
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(
                    text = "英雄池:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                SingleLineText(
                    text = "${player.heroPool.size}个英雄",
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 签约/解约按钮（仅在全部选手Tab显示）
            if (onSignPlayer != null || onReleasePlayer != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isMyTeam && onReleasePlayer != null) {
                    // 解约按钮
                    OutlinedButton(
                        onClick = onReleasePlayer,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFF5252)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        SingleLineText(
                            text = "解约",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (!isMyTeam && onSignPlayer != null) {
                    // 签约按钮
                    val isTeamFull = PlayerManager.myTeam.size >= 7
                    Button(
                        onClick = onSignPlayer,
                        enabled = !isTeamFull,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        SingleLineText(
                            text = if (isTeamFull) "战队已满" else "签约",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttributeBar(name: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.width(40.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .background(Color(0xFF2A2A3E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                        )
                    )
            )
        }
        
        Text(
            text = value.toString(),
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.width(30.dp)
        )
    }
}

/**
 * 招募结果对话框（带抽奖动画）
 */
@Composable
fun RecruitResultDialog(
    onDismiss: () -> Unit
) {
    // 执行招募并自动签约
    val player = remember { 
        val newPlayer = PlayerManager.recruitPlayer()
        PlayerManager.signPlayer(newPlayer.id)  // 自动签约到战队
        newPlayer
    }
    
    // 动画状态
    var animationPhase by remember { mutableIntStateOf(0) }
    // 0: 光束聚集 (1.2秒)
    // 1: 品质爆发 (1秒)
    // 2: 卡片展示 (0.5秒)
    // 3: 详细信息
    
    LaunchedEffect(Unit) {
        delay(1200)
        animationPhase = 1
        delay(1000)
        animationPhase = 2
        delay(500)
        animationPhase = 3
    }
    
    Dialog(
        onDismissRequest = { if (animationPhase == 3) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when (animationPhase) {
                0 -> BeamGatheringAnimation()
                1 -> RarityBurstAnimation(player)
                2 -> CardRevealAnimation(player)
                3 -> PlayerDetailCard(player, onDismiss)
            }
        }
    }
}

/**
 * 光束聚集动画 - 第一阶段
 */
@Composable
fun BeamGatheringAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "gathering")
    
    // 光束聚集动画
    val gatherProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gather"
    )
    
    // 旋转
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        // 四周光束向中心聚集
        repeat(12) { index ->
            val angle = index * 30f
            val distance = 300.dp * (1 - gatherProgress)
            
            Box(
                modifier = Modifier
                    .offset(
                        x = distance * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                        y = distance * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                    )
                    .size(8.dp, 80.dp)
                    .rotate(angle)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.8f),
                                Color(0xFFFFEB3B).copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
        
        // 中心能量球
        Box(
            modifier = Modifier
                .size(150.dp * gatherProgress)
                .rotate(rotation)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFEB3B),
                            Color(0xFF2196F3),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
        
        // 内圈旋转光环
        Box(
            modifier = Modifier
                .size(120.dp * gatherProgress)
                .rotate(-rotation * 2)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

/**
 * 品质爆发动画 - 第二阶段
 */
@Composable
fun RarityBurstAnimation(player: EsportsPlayer) {
    val colors = when (player.rarity.displayName) {
        "SSR" -> listOf(Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFFFF5722))
        "S" -> listOf(Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFF673AB7))
        "A" -> listOf(Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4))
        "B" -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFF009688))
        else -> listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E), Color(0xFF757575))
    }
    
    var burstProgress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            burstProgress = value
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "burst")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        // 爆发冲击波（多层）
        repeat(3) { layer ->
            val layerDelay = layer * 0.2f
            val layerProgress = (burstProgress - layerDelay).coerceIn(0f, 1f)
            
            Box(
                modifier = Modifier
                    .size(400.dp * layerProgress)
                    .alpha((1 - layerProgress) * 0.6f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colors[layer % colors.size].copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
        
        // SSR/S 特有效果
        if (player.rarity.displayName in listOf("SSR", "S")) {
            // 外层旋转光环
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size((350 - index * 50).dp)
                        .rotate(rotation * (if (index % 2 == 0) 1f else -1.5f))
                        .alpha(burstProgress)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors[0],
                                    Color.Transparent,
                                    colors[1],
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
        
        // 粒子爆发（16个方向）
        repeat(16) { index ->
            val angle = index * 22.5f
            val distance = 200.dp * burstProgress
            val particleSize = if (player.rarity.displayName in listOf("SSR", "S")) 16.dp else 12.dp
            
            Box(
                modifier = Modifier
                    .offset(
                        x = distance * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                        y = distance * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                    )
                    .size(particleSize * (1 - burstProgress * 0.5f))
                    .alpha(1 - burstProgress)
                    .background(
                        color = colors[index % colors.size],
                        shape = RoundedCornerShape(50)
                    )
            )
        }
        
        // 中心光球
        Box(
            modifier = Modifier
                .size(180.dp * (0.5f + burstProgress * 0.5f))
                .scale(1f + (kotlin.math.sin(burstProgress * Math.PI * 4).toFloat() * 0.1f))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            colors[0],
                            colors[1],
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
        
        // 品质文字淡入
        if (burstProgress > 0.6f) {
            val textAlpha = ((burstProgress - 0.6f) / 0.4f).coerceIn(0f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = player.rarity.emoji,
                    fontSize = 120.sp,
                    modifier = Modifier
                        .alpha(textAlpha)
                        .scale(0.8f + textAlpha * 0.2f)
                )
                Text(
                    text = player.rarity.displayName,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .alpha(textAlpha)
                        .scale(0.8f + textAlpha * 0.2f)
                )
            }
        }
    }
}

/**
 * 卡片展示动画 - 第三阶段
 */
@Composable
fun CardRevealAnimation(player: EsportsPlayer) {
    val colors = when (player.rarity.displayName) {
        "SSR" -> listOf(Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFFFF5722))
        "S" -> listOf(Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFF673AB7))
        "A" -> listOf(Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4))
        "B" -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFF009688))
        else -> listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E), Color(0xFF757575))
    }
    
    var revealProgress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            revealProgress = value
        }
    }
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        // 背景光晕
        Box(
            modifier = Modifier
                .size(350.dp)
                .alpha(revealProgress * 0.3f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors[0],
                            colors[1],
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
        
        // 卡片
        Card(
            modifier = Modifier
                .width(280.dp)
                .height(400.dp)
                .scale(0.5f + revealProgress * 0.5f)
                .alpha(revealProgress),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors[0].copy(alpha = 0.3f),
                                Color(0xFF1E1E2E),
                                Color(0xFF1E1E2E)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    // 品质图标
                    Text(
                        text = player.rarity.emoji,
                        fontSize = 100.sp
                    )
                    
                    // 选手名字
                    Text(
                        text = player.name,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = player.rarity.color
                    )
                    
                    // 品质标签
                    Surface(
                        color = player.rarity.color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = player.rarity.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = player.rarity.color,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                    
                    // 位置
                    Text(
                        text = player.positionDisplayName,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * 选手详情卡片
 */
@Composable
fun PlayerDetailCard(player: EsportsPlayer, onDismiss: () -> Unit) {
    // 入场动画
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .scale(scale)
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = "🎉 招募成功！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            
            // 选手信息
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 品质和名字
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = player.rarity.emoji,
                        fontSize = 32.sp
                    )
                    Text(
                        text = player.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = player.rarity.color
                    )
                }
                
                // 品质标签
                Surface(
                    color = player.rarity.color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = player.rarity.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = player.rarity.color,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 基本信息
                PlayerInfoRow("位置", player.positionDisplayName)
                PlayerInfoRow("年龄", "${player.age}岁")
                PlayerInfoRow("综合评分", player.attributes.overallRating().toString())
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 属性条
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "选手属性",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    AttributeBar("操作", player.attributes.mechanics)
                    AttributeBar("意识", player.attributes.awareness)
                    AttributeBar("团队", player.attributes.teamwork)
                    AttributeBar("心态", player.attributes.mentality)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 确定按钮
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "确定",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 信息行
 */
@Composable
private fun PlayerInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 英雄图鉴Tab
 */
@Composable
fun HeroEncyclopediaTab() {
    val allHeroes = HeroManager.getAllHeroes()
    var selectedPosition by remember { mutableStateOf<HeroPosition?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 位置筛选
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213e)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SingleLineText(
                    "📖 英雄图鉴 (共${allHeroes.size}个英雄)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // 位置筛选按钮（可横向滚动）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPosition == null,
                        onClick = { selectedPosition = null },
                        label = { SingleLineText("全部", fontSize = 12.sp) }
                    )
                    HeroPosition.values().forEach { position ->
                        FilterChip(
                            selected = selectedPosition == position,
                            onClick = { selectedPosition = position },
                            label = { 
                                SingleLineText(
                                    when(position) {
                                        HeroPosition.TOP -> "上单"
                                        HeroPosition.JUNGLE -> "打野"
                                        HeroPosition.MID -> "中单"
                                        HeroPosition.ADC -> "ADC"
                                        HeroPosition.SUPPORT -> "辅助"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // 英雄列表
        val filteredHeroes = if (selectedPosition == null) {
            allHeroes
        } else {
            allHeroes.filter { it.position == selectedPosition }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredHeroes) { hero ->
                HeroCard(hero = hero)
            }
        }
    }
}

/**
 * 英雄卡片
 */
@Composable
fun HeroCard(hero: com.example.yjcy.data.esports.MobaHero) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 顶部：英雄名称和称号
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = hero.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = hero.title,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // 类型标签
                Surface(
                    color = getHeroTypeColor(hero.type).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = hero.type.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = getHeroTypeColor(hero.type),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            // 位置和难度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleLineText(
                        text = "位置:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    SingleLineText(
                        text = hero.positionDisplayName,
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleLineText(
                        text = "难度:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    SingleLineText(
                        text = "${"★".repeat(hero.difficulty)}${"☆".repeat(5 - hero.difficulty)}",
                        fontSize = 13.sp,
                        color = Color(0xFFFFD700)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 英雄强度属性
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SingleLineText(
                    text = "英雄强度",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                HeroStrengthBar("伤害", hero.strength.damage, Color(0xFFFF5252))
                HeroStrengthBar("坦度", hero.strength.tankiness, Color(0xFF4CAF50))
                HeroStrengthBar("机动", hero.strength.mobility, Color(0xFF2196F3))
                HeroStrengthBar("控制", hero.strength.control, Color(0xFF9C27B0))
                HeroStrengthBar("工具", hero.strength.utility, Color(0xFFFFEB3B))
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 胜率、选取率、禁用率
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HeroStatColumn("胜率", String.format("%.1f%%", hero.winRate), 
                    if (hero.winRate >= 52) Color(0xFF4CAF50) else if (hero.winRate <= 48) Color(0xFFFF5252) else Color.White)
                HeroStatColumn("选取率", String.format("%.1f%%", hero.pickRate), Color(0xFF2196F3))
                HeroStatColumn("禁用率", String.format("%.1f%%", hero.banRate), Color(0xFFFF9800))
            }
        }
    }
}

/**
 * 英雄强度条
 */
@Composable
fun HeroStrengthBar(name: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.width(40.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .background(Color(0xFF2A2A3E), RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.6f), color)
                        ),
                        shape = RoundedCornerShape(7.dp)
                    )
            )
        }
        
        Text(
            text = value.toString(),
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.width(30.dp)
        )
    }
}

/**
 * 英雄统计列
 */
@Composable
fun HeroStatColumn(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 获取英雄类型颜色
 */
fun getHeroTypeColor(type: com.example.yjcy.data.esports.HeroType): Color {
    return when(type) {
        com.example.yjcy.data.esports.HeroType.TANK -> Color(0xFF4CAF50)
        com.example.yjcy.data.esports.HeroType.FIGHTER -> Color(0xFFFF5722)
        com.example.yjcy.data.esports.HeroType.ASSASSIN -> Color(0xFF9C27B0)
        com.example.yjcy.data.esports.HeroType.MAGE -> Color(0xFF2196F3)
        com.example.yjcy.data.esports.HeroType.MARKSMAN -> Color(0xFFFF9800)
        com.example.yjcy.data.esports.HeroType.SUPPORT -> Color(0xFF00BCD4)
    }
}
