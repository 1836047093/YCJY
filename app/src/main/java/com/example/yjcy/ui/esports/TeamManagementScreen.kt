package com.example.yjcy.ui.esports

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
            }
            
            // 内容区域
            when (selectedTab) {
                0 -> TeamRosterTab()
                1 -> RecruitmentTab(
                    onRecruit = { showRecruitDialog = true }
                )
                2 -> AllPlayersTab()
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
        
        // 招募按钮
        Button(
            onClick = onRecruit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            SingleLineText(
                "🎯 招募选手",
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
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(allPlayers) { player ->
            PlayerCard(
                player = player,
                isMyTeam = PlayerManager.myTeam.contains(player)
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
    isMyTeam: Boolean
) {
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
    // 执行招募
    val player = remember { PlayerManager.recruitPlayer() }
    
    // 动画状态
    var animationPhase by remember { mutableIntStateOf(0) }
    // 0: 初始旋转动画
    // 1: 显示品质光效
    // 2: 显示完整信息
    
    LaunchedEffect(Unit) {
        delay(1500) // 旋转动画持续1.5秒
        animationPhase = 1
        delay(800) // 光效持续0.8秒
        animationPhase = 2
    }
    
    Dialog(
        onDismissRequest = { if (animationPhase == 2) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            when (animationPhase) {
                0 -> SpinningCardAnimation()
                1 -> RarityRevealAnimation(player)
                2 -> PlayerDetailCard(player, onDismiss)
            }
        }
    }
}

/**
 * 旋转卡片动画
 */
@Composable
fun SpinningCardAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .size(200.dp)
            .rotate(rotation)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2196F3),
                        Color(0xFF9C27B0)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "?",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 品质揭示动画
 */
@Composable
fun RarityRevealAnimation(player: EsportsPlayer) {
    // 根据品质选择不同的动画效果
    val colors = when (player.rarity.displayName) {
        "SSR" -> listOf(Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFFFF5722))
        "S" -> listOf(Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFF673AB7))
        "A" -> listOf(Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4))
        "B" -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFF009688))
        else -> listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E), Color(0xFF757575))
    }
    
    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
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
        // 外圈旋转光环（SSR和S品质特有）
        if (player.rarity.displayName in listOf("SSR", "S")) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .rotate(rotation)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = colors + colors[0]
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .alpha(0.3f)
            )
        }
        
        // 中间脉冲圆
        Box(
            modifier = Modifier
                .size(250.dp * scale)
                .background(
                    brush = Brush.radialGradient(
                        colors = colors + Color.Transparent
                    ),
                    shape = RoundedCornerShape(50)
                )
                .alpha(0.6f)
        )
        
        // 品质文字
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = player.rarity.emoji,
                fontSize = 100.sp,
                modifier = Modifier.scale(scale)
            )
            Text(
                text = player.rarity.displayName,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = player.rarity.color,
                modifier = Modifier.scale(scale)
            )
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
            
            Divider(color = Color.White.copy(alpha = 0.1f))
            
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
                InfoRow("位置", player.positionDisplayName)
                InfoRow("年龄", "${player.age}岁")
                InfoRow("综合评分", player.attributes.overallRating().toString())
                
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
fun InfoRow(label: String, value: String) {
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
