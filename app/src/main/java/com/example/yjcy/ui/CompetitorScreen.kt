package com.example.yjcy.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.CompetitorCompany
import com.example.yjcy.data.CompetitorGame
import com.example.yjcy.data.CompetitorNews
import com.example.yjcy.data.GameIP
import com.example.yjcy.data.NewsType
import com.example.yjcy.data.SaveData
import com.example.yjcy.data.GameReleaseStatus
import com.example.yjcy.utils.formatMoneyWithDecimals
import com.example.yjcy.utils.formatMoney

/**
 * 排行榜类型枚举
 */
enum class LeaderboardType(
    val displayName: String,
    val icon: String
) {
    MARKET_VALUE("市值排行榜", "💰"),
    FANS("粉丝排行榜", "❤️"),
    ONLINE_GAME("热门网游排行", "🎮"),
    SINGLE_PLAYER("畅销单机排行", "📦")
}

/**
 * 竞争对手界面
 */
@Composable
fun CompetitorContent(
    saveData: SaveData,
    @Suppress("UNUSED_PARAMETER") gameSpeed: Int = 1,
    onAcquisitionSuccess: (CompetitorCompany, Long, Long, Long, List<GameIP>) -> Unit = { _, _, _, _, _ -> },
    onAIWin: (CompetitorCompany, CompetitorCompany, Long) -> Unit = { _, _, _ -> } // AI获胜回调
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📊 排行榜", "📰 新闻", "🏢 对手")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF0D47A1)
                    )
                )
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        // 标题栏
        Text(
            text = "🎯 竞争对手",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 标签栏
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .padding(bottom = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.background(
                        if (selectedTab == index) Color.White.copy(alpha = 0.2f) else Color.Transparent
                    )
                )
            }
        }
        
        // 内容区域
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> LeaderboardContent(saveData)
                1 -> NewsContent(saveData)
                2 -> CompetitorsListContent(saveData, onAcquisitionSuccess, onAIWin)
            }
        }
    }
}

/**
 * 排行榜内容
 */
@Composable
fun LeaderboardContent(saveData: SaveData) {
    // 排行榜类型
    var selectedLeaderboard by remember { mutableStateOf(LeaderboardType.MARKET_VALUE) }
    var expanded by remember { mutableStateOf(false) }
    
    // 实时更新的排行榜数据状态
    var liveLeaderboardItems by remember { mutableStateOf<List<LeaderboardItem>>(emptyList()) }
    
    // 计算所有竞争对手网游的总收入总和，用于检测数据变化
    val competitorTotalRevenue = remember(saveData.competitors) {
        saveData.competitors.sumOf { competitor ->
            competitor.games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
                .sumOf { it.totalRevenue.toLong() }
        }
    }
    
    // 跟踪竞争对手数量变化，确保收购后排行榜立即更新
    val competitorsCount = remember(saveData.competitors) {
        saveData.competitors.size
    }
    
    // 网游排行榜数据（响应 competitors 变化）
    val onlineGameItems = remember(saveData.competitors, saveData.games) {
        getTopOnlineGames(saveData)
    }
    
    // 定时更新机制：每3秒更新一次网游排行榜数据
    // 当排行榜类型、竞争对手总收入、玩家游戏数量、竞争对手数量发生变化时，立即刷新一次
    LaunchedEffect(selectedLeaderboard, competitorTotalRevenue, saveData.games.size, competitorsCount) {
        // 立即更新一次网游排行榜
        if (selectedLeaderboard == LeaderboardType.ONLINE_GAME) {
            liveLeaderboardItems = getTopOnlineGamesWithFluctuation(saveData)
        }
        while (true) {
            if (selectedLeaderboard == LeaderboardType.ONLINE_GAME) {
                liveLeaderboardItems = getTopOnlineGamesWithFluctuation(saveData)
            }
            kotlinx.coroutines.delay(3000L) // 每3秒更新一次
        }
    }
    
    // 使用 remember 确保排行榜数据在 competitors 变化时重新计算
    val marketValueItems = remember(saveData.competitors, saveData.companyName, saveData.fans) {
        getTopCompaniesByMarketValue(saveData)
    }
    
    val fansItems = remember(saveData.competitors, saveData.companyName, saveData.fans) {
        getTopCompaniesByFans(saveData)
    }
    
    val singlePlayerItems = remember(saveData.competitors, saveData.games) {
        getTopSinglePlayerGames(saveData)
    }
    
    // 显示选中的排行榜
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
        item(key = "leaderboard_${selectedLeaderboard}_${competitorsCount}") {
            when (selectedLeaderboard) {
                LeaderboardType.MARKET_VALUE -> {
                    LeaderboardCard(
                        title = "市值排行榜",
                        icon = "💰",
                        topColor = Color(0xFFFFD700),
                        items = marketValueItems,
                        leaderboardType = LeaderboardType.MARKET_VALUE,
                        selectedLeaderboard = selectedLeaderboard,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        onLeaderboardSelected = { selectedLeaderboard = it }
                    )
                }
                LeaderboardType.FANS -> {
                    LeaderboardCard(
                        title = "粉丝排行榜",
                        icon = "❤️",
                        topColor = Color(0xFFFF6B6B),
                        items = fansItems,
                        leaderboardType = LeaderboardType.FANS,
                        selectedLeaderboard = selectedLeaderboard,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        onLeaderboardSelected = { selectedLeaderboard = it }
                    )
                }
                LeaderboardType.ONLINE_GAME -> {
                    LeaderboardCard(
                        title = "热门网游排行",
                        icon = "🎮",
                        topColor = Color(0xFF4ECDC4),
                        items = liveLeaderboardItems.ifEmpty { onlineGameItems },
                        leaderboardType = LeaderboardType.ONLINE_GAME,
                        selectedLeaderboard = selectedLeaderboard,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        onLeaderboardSelected = { selectedLeaderboard = it }
                    )
                }
                LeaderboardType.SINGLE_PLAYER -> {
                    LeaderboardCard(
                        title = "畅销单机排行",
                        icon = "📦",
                        topColor = Color(0xFF95E1D3),
                        items = singlePlayerItems,
                        leaderboardType = LeaderboardType.SINGLE_PLAYER,
                        selectedLeaderboard = selectedLeaderboard,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        onLeaderboardSelected = { selectedLeaderboard = it }
                    )
                }
            }
        }
    }
}

/**
 * 排行榜卡片
 */
@Composable
fun LeaderboardCard(
    title: String,
    icon: String,
    topColor: Color,
    items: List<LeaderboardItem>,
    leaderboardType: LeaderboardType,
    selectedLeaderboard: LeaderboardType,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLeaderboardSelected: (LeaderboardType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        // 标题和下拉选择器
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            // 左侧：图标和标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = icon,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            
            // 右侧：下拉选择器
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onExpandedChange(true) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = selectedLeaderboard.icon,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = selectedLeaderboard.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (expanded) "▲" else "▼",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier
                        .background(Color(0xFF1E1E2E))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    LeaderboardType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = type.icon,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = type.displayName,
                                        color = Color.White
                                    )
                                }
                            },
                            onClick = {
                                onLeaderboardSelected(type)
                                onExpandedChange(false)
                            },
                            modifier = Modifier.background(
                                if (selectedLeaderboard == type) Color.White.copy(alpha = 0.1f) else Color.Transparent
                            )
                        )
                    }
                }
            }
        }
        
        // 排行榜列表
        items.forEachIndexed { index, item ->
            LeaderboardItemRow(
                rank = index + 1,
                item = item,
                topColor = topColor,
                isTop = index < 3,
                isPlayer = item.isPlayer,
                leaderboardType = leaderboardType
            )
            if (index < items.size - 1) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)
                )
            }
        }
        
        if (items.isEmpty()) {
            Text(
                text = "暂无数据",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * 排行榜项目行
 */
@Composable
fun LeaderboardItemRow(
    rank: Int,
    item: LeaderboardItem,
    @Suppress("UNUSED_PARAMETER") topColor: Color,
    isTop: Boolean,
    isPlayer: Boolean = false,
    leaderboardType: LeaderboardType
) {
    // 为前3名设计专属的超炫酷图标组合
    val rankIconData = when (rank) {
        1 -> when (leaderboardType) {
            LeaderboardType.MARKET_VALUE -> Triple("💎", "✨", listOf(Color(0xFFFFD700), Color(0xFFFFEB3B), Color(0xFFFFC107)))
            LeaderboardType.FANS -> Triple("❤️", "💕", listOf(Color(0xFFFF1744), Color(0xFFFF4081), Color(0xFFFF80AB)))
            LeaderboardType.ONLINE_GAME -> Triple("🔥", "⚡", listOf(Color(0xFFFF5722), Color(0xFFFF6F00), Color(0xFFFFD54F)))
            LeaderboardType.SINGLE_PLAYER -> Triple("👑", "💎", listOf(Color(0xFFFFD700), Color(0xFFFFEB3B), Color(0xFFFFF59D)))
        }
        2 -> when (leaderboardType) {
            LeaderboardType.MARKET_VALUE -> Triple("💰", "💸", listOf(Color(0xFFC0C0C0), Color(0xFFE0E0E0), Color(0xFFBDBDBD)))
            LeaderboardType.FANS -> Triple("💖", "💗", listOf(Color(0xFFFF4081), Color(0xFFFF80AB), Color(0xFFF48FB1)))
            LeaderboardType.ONLINE_GAME -> Triple("⚡", "🌟", listOf(Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFFD54F)))
            LeaderboardType.SINGLE_PLAYER -> Triple("🎮", "🕹️", listOf(Color(0xFF5C6BC0), Color(0xFF7E57C2), Color(0xFF9575CD))) // 游戏手柄+摇杆，紫蓝色系
        }
        3 -> when (leaderboardType) {
            LeaderboardType.MARKET_VALUE -> Triple("💵", "💴", listOf(Color(0xFFFFA726), Color(0xFFFFB74D), Color(0xFFFFCC80))) // 使用更亮的橙金色，提高可见度
            LeaderboardType.FANS -> Triple("💕", "💝", listOf(Color(0xFFF06292), Color(0xFFF48FB1), Color(0xFFF8BBD0)))
            LeaderboardType.ONLINE_GAME -> Triple("⭐", "✨", listOf(Color(0xFF00BCD4), Color(0xFF26C6DA), Color(0xFF4DD0E1)))
            LeaderboardType.SINGLE_PLAYER -> Triple("🏆", "⭐", listOf(Color(0xFFFF6F00), Color(0xFFFF8A65), Color(0xFFFFAB91))) // 奖杯+星星，橙红色系
        }
        else -> Triple("", "", emptyList())
    }
    
    val (@Suppress("UNUSED_VARIABLE") mainIcon, @Suppress("UNUSED_VARIABLE") particleIcon, gradientColors) = rankIconData
    
    // 静态图标 - 使用第一个渐变色作为主色
    val primaryColor = if (gradientColors.isNotEmpty()) gradientColors[0] else Color.Gray
    
    // ========== 环绕流光动画系统 ==========
    val infiniteTransition = rememberInfiniteTransition(label = "rank_animation_$rank")
    
    // 四边流光扫描动画
    val borderLightAngle = if (isTop) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = when (rank) {
                        1 -> 2000  // 第1名最快
                        2 -> 2500
                        3 -> 3000
                        else -> 3000
                    },
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "border_light"
        ).value
    } else 0f
    
    // 粒子内核脉冲呼吸
    val glowPulse = if (isTop) {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = when (rank) {
                        1 -> 1500
                        2 -> 1800
                        3 -> 2100
                        else -> 1500
                    },
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_pulse"
        ).value
    } else 0.5f
    
    // 环绕粒子旋转（反方向）
    val particleAngle = if (isTop) {
        infiniteTransition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = when (rank) {
                        1 -> 3500
                        2 -> 4000
                        3 -> 4500
                        else -> 4000
                    },
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_rotate"
        ).value
    } else 0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (isPlayer) {
                    // 玩家专属：简洁的绿色光带
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50).copy(alpha = 0.4f),
                                Color(0xFF4CAF50).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    drawRect(
                        color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                        size = androidx.compose.ui.geometry.Size(4f, size.height)
                    )
                } else if (isTop) {
                    // ========== 多彩流光扫描特效系统 ==========
                    
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    
                    // 根据排名定义彩虹色系
                    val rainbowColors = when (rank) {
                        1 -> listOf(  // 🥇金色彩虹
                            Color(0xFFFFD700),  // 金色
                            Color(0xFFFF69B4),  // 粉色
                            Color(0xFF00CED1),  // 青色
                            Color(0xFF7B68EE),  // 紫色
                            Color(0xFFFF6347),  // 橙红
                            Color(0xFFFFD700)   // 金色（循环）
                        )
                        2 -> listOf(  // 🥈银蓝彩虹
                            Color(0xFFC0C0C0),  // 银色
                            Color(0xFF4169E1),  // 皇家蓝
                            Color(0xFF00BFFF),  // 深天蓝
                            Color(0xFF9370DB),  // 紫罗兰
                            Color(0xFF87CEEB),  // 天蓝
                            Color(0xFFC0C0C0)   // 银色（循环）
                        )
                        3 -> listOf(  // 🥉铜橙彩虹
                            Color(0xFFCD7F32),  // 铜色
                            Color(0xFFFF4500),  // 橙红
                            Color(0xFFFF8C00),  // 暗橙
                            Color(0xFFFFD700),  // 金色
                            Color(0xFFFF6347),  // 番茄红
                            Color(0xFFCD7F32)   // 铜色（循环）
                        )
                        else -> gradientColors + gradientColors.first()
                    }
                    
                    // ========== 1. 动态背景层 ==========
                    
                    // 背景脉冲呼吸光晕
                    val bgColor1 = rainbowColors[0]
                    val bgColor2 = rainbowColors[1]
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                bgColor1.copy(alpha = 0.15f * glowPulse),
                                bgColor2.copy(alpha = 0.12f * glowPulse),
                                bgColor1.copy(alpha = 0.08f * glowPulse),
                                Color.Transparent
                            )
                        )
                    )
                    
                    // 背景流动波纹（从左到右）
                    val waveProgress = (borderLightAngle / 360f)
                    for (i in 0..2) {
                        val xOffset = ((waveProgress + i * 0.33f) % 1f) * size.width
                        val waveWidth = size.width * 0.4f
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    rainbowColors[i % rainbowColors.size].copy(alpha = 0.08f),
                                    rainbowColors[(i + 1) % rainbowColors.size].copy(alpha = 0.12f),
                                    rainbowColors[i % rainbowColors.size].copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startX = xOffset - waveWidth / 2,
                                endX = xOffset + waveWidth / 2
                            ),
                            topLeft = androidx.compose.ui.geometry.Offset(xOffset - waveWidth / 2, 0f),
                            size = androidx.compose.ui.geometry.Size(waveWidth, size.height)
                        )
                    }
                    
                    // 背景光点粒子（随机飘动）
                    val sparkleCount = when (rank) {
                        1 -> 12
                        2 -> 8
                        3 -> 6
                        else -> 5
                    }
                    
                    for (i in 0 until sparkleCount) {
                        val angle = (particleAngle * 0.5f + i * (360f / sparkleCount)) % 360f
                        val x = (kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() + 1f) / 2f * size.width
                        val y = (kotlin.math.cos(Math.toRadians((angle * 1.3f).toDouble())).toFloat() + 1f) / 2f * size.height
                        
                        val sparkleSize = when (rank) {
                            1 -> 3f
                            2 -> 2.5f
                            else -> 2f
                        }
                        
                        val colorIndex = i % rainbowColors.size
                        drawCircle(
                            color = rainbowColors[colorIndex].copy(alpha = 0.4f * glowPulse),
                            radius = sparkleSize,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                    
                    // ========== 2. 四边流光扫描层 ==========
                    
                    // 流光条数随排名变化
                    val streamCount = when (rank) {
                        1 -> 3  // 第1名：3条流光同时扫描
                        2 -> 2  // 第2名：2条流光
                        3 -> 1  // 第3名：1条流光
                        else -> 1
                    }
                    
                    // 流光宽度随排名变化
                    val streamWidth = when (rank) {
                        1 -> 0.25f  // 第1名最宽
                        2 -> 0.20f
                        3 -> 0.15f
                        else -> 0.15f
                    }
                    
                    // 多彩流光扫描
                    val borderProgress = borderLightAngle / 360f
                    val perimeter = 2 * (size.width + size.height)
                    
                    // 绘制多条流光
                    for (streamIndex in 0 until streamCount) {
                        val offset = (streamIndex * (1f / streamCount))
                        val currentPos = perimeter * ((borderProgress + offset) % 1f)
                        val lightLength = perimeter * streamWidth
                        
                        // 当前流光使用的颜色索引（随位置变化）
                        val colorPhase = ((borderProgress + offset) * rainbowColors.size).toInt() % rainbowColors.size
                        val color1 = rainbowColors[colorPhase]
                        val color2 = rainbowColors[(colorPhase + 1) % rainbowColors.size]
                        val color3 = rainbowColors[(colorPhase + 2) % rainbowColors.size]
                        
                        fun drawBorderLight(startPos: Float, length: Float) {
                            var remainingLength = length
                            var currentPos = startPos % perimeter
                            
                            // 持续绘制直到长度用完
                            while (remainingLength > 0.1f) {
                                currentPos = currentPos % perimeter  // 循环处理
                                
                                // 计算当前在哪条边以及该边的剩余长度
                                when {
                                    // 顶边（0 -> width）
                                    currentPos < size.width -> {
                                        val x = currentPos
                                        val edgeRemaining = size.width - x
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color3.copy(alpha = 0.5f * progress),
                                                    Color.Transparent
                                                ),
                                                startX = x,
                                                endX = x + drawLength
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                                            size = androidx.compose.ui.geometry.Size(drawLength, 6f)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                    
                                    // 右边（width -> width+height）
                                    currentPos < size.width + size.height -> {
                                        val y = currentPos - size.width
                                        val edgeRemaining = size.height - y
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color3.copy(alpha = 0.5f * progress),
                                                    Color.Transparent
                                                ),
                                                startY = y,
                                                endY = y + drawLength
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(size.width - 6f, y),
                                            size = androidx.compose.ui.geometry.Size(6f, drawLength)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                    
                                    // 底边（width+height -> 2*width+height，从右到左）
                                    currentPos < 2 * size.width + size.height -> {
                                        val traveled = currentPos - size.width - size.height
                                        val edgeRemaining = size.width - traveled
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val x = size.width - traveled
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    color3.copy(alpha = 0.5f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    Color.Transparent
                                                ),
                                                startX = x - drawLength,
                                                endX = x
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(x - drawLength, size.height - 6f),
                                            size = androidx.compose.ui.geometry.Size(drawLength, 6f)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                    
                                    // 左边（2*width+height -> perimeter，从下到上）
                                    else -> {
                                        val traveled = currentPos - 2 * size.width - size.height
                                        val edgeRemaining = size.height - traveled
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val y = size.height - traveled
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    color3.copy(alpha = 0.5f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    Color.Transparent
                                                ),
                                                startY = y - drawLength,
                                                endY = y
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, y - drawLength),
                                            size = androidx.compose.ui.geometry.Size(6f, drawLength)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                }
                            }
                        }
                        
                        drawBorderLight(currentPos, lightLength)
                    }
                    
                    // ========== 3. 环绕彩色粒子层 ==========
                    
                    val particleCount = when (rank) {
                        1 -> 6
                        2 -> 5
                        3 -> 4
                        else -> 4
                    }
                    
                    val particleSize = when (rank) {
                        1 -> 35f
                        2 -> 30f
                        3 -> 25f
                        else -> 25f
                    }
                    
                    for (i in 0 until particleCount) {
                        val angle = Math.toRadians((particleAngle + i * (360f / particleCount)).toDouble())
                        val radiusX = size.width * 0.45f
                        val radiusY = size.height * 0.42f
                        
                        val x = centerX + kotlin.math.cos(angle).toFloat() * radiusX
                        val y = centerY + kotlin.math.sin(angle).toFloat() * radiusY
                        
                        val colorIndex = i % (rainbowColors.size - 1)
                        val color = rainbowColors[colorIndex]
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.8f),
                                    color.copy(alpha = 0.7f),
                                    color.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                radius = particleSize * 1.2f
                            ),
                            radius = particleSize,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 排名数字和特效
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTop) {
                // 前3名 - 强力发光徽章
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // 外层强光晕（大范围）
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.5f * glowPulse),
                                        primaryColor.copy(alpha = 0.3f * glowPulse),
                                        primaryColor.copy(alpha = 0.1f * glowPulse),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    // 核心徽章
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(
                                elevation = (6.dp.value + glowPulse * 6f).dp,
                                shape = CircleShape,
                                ambientColor = primaryColor,
                                spotColor = primaryColor
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.95f),
                                        primaryColor.copy(alpha = 0.75f),
                                        primaryColor.copy(alpha = 0.6f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = (2.5f + glowPulse * 0.5f).dp,
                                brush = Brush.sweepGradient(
                                    colors = gradientColors.map { 
                                        it.copy(alpha = 0.7f + glowPulse * 0.3f) 
                                    } + Color.White.copy(alpha = glowPulse)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    
                    // 顶部装饰图标
                    Text(
                        text = when (rank) {
                            1 -> "👑"
                            2 -> "⭐"
                            3 -> "🏆"
                            else -> ""
                        },
                        fontSize = 18.sp,
                        modifier = Modifier.offset(y = (-30).dp)
                    )
                }
            } else {
                // 第4-5名
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(2.dp, CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 内容区域（完全垂直布局）
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 第一行：游戏名字
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlayer) {
                    Text(
                        text = "👤 ",
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = item.mainText,
                    color = Color.White,
                    fontWeight = if (isTop) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = if (isTop) 19.sp else 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (isTop && !isPlayer) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "✨", fontSize = 14.sp)
                }
            }
            
            // 第二行：公司名字
            if (item.subText.isNotEmpty()) {
                Text(
                    text = item.subText,
                    color = if (isTop) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f),
                    fontSize = if (isTop) 15.sp else 14.sp,
                    fontWeight = if (isTop) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 第三行：活跃玩家/销量
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTop) {
                    Text(
                        text = when (leaderboardType) {
                            LeaderboardType.MARKET_VALUE, LeaderboardType.FANS -> "📈"
                            LeaderboardType.ONLINE_GAME -> "🎮"
                            LeaderboardType.SINGLE_PLAYER -> "📦"
                        },
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = item.value,
                    color = if (isTop) primaryColor else Color(0xFFFFD700),
                    fontWeight = if (isTop) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (isTop) 15.sp else 14.sp
                )
            }
            
            // 第四行：总收入
            if (item.extraInfo.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTop) {
                        Text(
                            text = "💰",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = item.extraInfo,
                        color = if (isTop) primaryColor.copy(alpha = 0.9f) else Color(0xFFFF6B6B),
                        fontWeight = if (isTop) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = if (isTop) 15.sp else 14.sp
                    )
                }
            }
        }
    }
}

/**
 * 排行榜项目数据类
 */
data class LeaderboardItem(
    val mainText: String,
    val subText: String,
    val value: String,
    val extraInfo: String = "", // 额外信息（如总收入）
    val isPlayer: Boolean = false // 标记是否为玩家公司
)

/**
 * 计算玩家公司市值
 */
fun calculatePlayerMarketValue(saveData: SaveData): Long {
    val releasedGamesCount = saveData.games.count { 
        it.releaseStatus == GameReleaseStatus.RELEASED || 
        it.releaseStatus == GameReleaseStatus.RATED 
    }
    
    // 修复：如果资金为负数（溢出或过度负债），使用0计算基础市值
    val baseMoney = if (saveData.money < 0) {
        android.util.Log.w("CompetitorScreen", "⚠️ 计算市值时检测到资金为负数(${saveData.money})，使用0计算基础市值")
        0L
    } else {
        saveData.money
    }
    
    // 修复：确保各项计算不会溢出
    val fansValue = try {
        saveData.fans * 10L
    } catch (e: Exception) {
        android.util.Log.w("CompetitorScreen", "⚠️ 粉丝价值计算溢出，使用0")
        0L
    }
    
    val gamesValue = releasedGamesCount * 100000L
    val employeesValue = saveData.allEmployees.size * 50000L
    
    // 累加所有值，并确保结果不为负数
    val result = baseMoney + fansValue + gamesValue + employeesValue
    
    // 如果结果为负数（溢出），返回0
    return if (result < 0) {
        android.util.Log.w("CompetitorScreen", "⚠️ 市值计算结果为负数($result)，返回0")
        0L
    } else {
        result
    }
}

/**
 * 获取市值最高的公司（前5）
 */
fun getTopCompaniesByMarketValue(saveData: SaveData): List<LeaderboardItem> {
    // 包含玩家公司（使用真实市值计算）
    val playerMarketValue = calculatePlayerMarketValue(saveData)
    val allCompanies = mutableListOf<Pair<String, Long>>()
    allCompanies.add(Pair(saveData.companyName, playerMarketValue))
    saveData.competitors.forEach { competitor ->
        allCompanies.add(Pair(competitor.name, competitor.marketValue))
    }
    
    return allCompanies
        .sortedByDescending { it.second }
        .take(5)
        .map { (name, value) ->
            LeaderboardItem(
                mainText = name,
                subText = "",
                value = formatMoney(value),
                isPlayer = name == saveData.companyName
            )
        }
}

/**
 * 获取粉丝最多的公司（前5）
 */
fun getTopCompaniesByFans(saveData: SaveData): List<LeaderboardItem> {
    val allCompanies = mutableListOf<Pair<String, Long>>()
    allCompanies.add(Pair(saveData.companyName, saveData.fans))
    saveData.competitors.forEach { competitor ->
        allCompanies.add(Pair(competitor.name, competitor.fans))
    }
    
    return allCompanies
        .sortedByDescending { it.second }
        .take(5)
        .map { (name, fans) ->
            LeaderboardItem(
                mainText = name,
                subText = "",
                value = formatMoneyWithDecimals(fans.toDouble()),
                isPlayer = name == saveData.companyName
            )
        }
}

/**
 * 获取活跃玩家最多的网游（前5）
 */
fun getTopOnlineGames(saveData: SaveData): List<LeaderboardItem> {
    // 使用四元组存储：游戏名、公司名、活跃玩家数、总收入
    val allOnlineGames = mutableListOf<Tuple4<String, String, Long, Double>>()
    
    // 玩家的网游（包含已发售和已评分的游戏）
    saveData.games.filter { 
        it.businessModel == BusinessModel.ONLINE_GAME && 
        (it.releaseStatus == GameReleaseStatus.RELEASED || 
         it.releaseStatus == GameReleaseStatus.RATED)
    }.forEach { game ->
            // 从RevenueManager获取活跃玩家数（考虑兴趣值影响）
            val activePlayers = com.example.yjcy.data.RevenueManager.getActivePlayers(game.id)
            // 获取总收入
            val gameRevenue = com.example.yjcy.data.RevenueManager.getGameRevenue(game.id)
            val totalRevenue = gameRevenue?.let {
                val stats = com.example.yjcy.data.RevenueManager.calculateStatistics(it)
                stats.totalRevenue
            } ?: 0.0
            
            allOnlineGames.add(
                Tuple4(
                    game.name,
                    saveData.companyName,
                    activePlayers,
                    totalRevenue
                )
            )
        }
    
    // 竞争对手的网游（使用真实累计收入）
    saveData.competitors.forEach { competitor ->
        competitor.games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
            .forEach { game ->
                allOnlineGames.add(
                    Tuple4(
                        game.name,
                        competitor.name,
                        game.activePlayers,
                        game.totalRevenue
                    )
                )
            }
    }
    
    return allOnlineGames
        .sortedByDescending { it.third }
        .take(5)
        .map {
            LeaderboardItem(
                mainText = it.first,
                subText = it.second,
                value = "活跃玩家：${formatMoneyWithDecimals(it.third.toDouble())}",
                extraInfo = "总收入：${formatMoneyWithDecimals(it.fourth)}",
                isPlayer = it.second == saveData.companyName
            )
        }
}

/**
 * 四元组数据类
 */
data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * 获取带有实时波动的网游排行榜（用于动态显示）
 * 活跃玩家数和总收入会在±1-3%范围内波动
 */
fun getTopOnlineGamesWithFluctuation(saveData: SaveData): List<LeaderboardItem> {
    // 使用四元组存储：游戏名、公司名、活跃玩家数、总收入
    val allOnlineGames = mutableListOf<Tuple4<String, String, Long, Double>>()
    
    // 玩家的网游（包含已发售和已评分的游戏）
    saveData.games.filter { 
        it.businessModel == BusinessModel.ONLINE_GAME && 
        (it.releaseStatus == GameReleaseStatus.RELEASED || 
         it.releaseStatus == GameReleaseStatus.RATED)
    }.forEach { game ->
            // 从RevenueManager获取活跃玩家数（考虑兴趣值影响）
            val basePlayers = com.example.yjcy.data.RevenueManager.getActivePlayers(game.id)
            // 添加±1-3%的随机波动
            val fluctuation = kotlin.random.Random.nextDouble(-0.03, 0.03)
            val activePlayers = (basePlayers * (1 + fluctuation)).toLong().coerceAtLeast(0L)
            
            // 获取总收入（累计值，不应该波动）
            val gameRevenue = com.example.yjcy.data.RevenueManager.getGameRevenue(game.id)
            val totalRevenue = gameRevenue?.let {
                val stats = com.example.yjcy.data.RevenueManager.calculateStatistics(it)
                stats.totalRevenue
            } ?: 0.0
            
            allOnlineGames.add(
                Tuple4(
                    game.name,
                    saveData.companyName,
                    activePlayers,
                    totalRevenue
                )
            )
        }
    
    // 竞争对手的网游（使用真实累计收入）
    saveData.competitors.forEach { competitor ->
        competitor.games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
            .forEach { game ->
                // 添加±1-3%的随机波动（仅活跃玩家数）
                val fluctuation = kotlin.random.Random.nextDouble(-0.03, 0.03)
                val activePlayers = (game.activePlayers * (1 + fluctuation)).toLong().coerceAtLeast(0L)
                
                allOnlineGames.add(
                    Tuple4<String, String, Long, Double>(
                        game.name,
                        competitor.name,
                        activePlayers,
                        game.totalRevenue // 使用真实累计收入
                    )
                )
            }
    }
    
    return allOnlineGames
        .sortedByDescending { it.third }
        .take(5)
        .map {
            LeaderboardItem(
                mainText = it.first,
                subText = it.second,
                value = "活跃玩家：${formatMoneyWithDecimals(it.third.toDouble())}",
                extraInfo = "总收入：${formatMoneyWithDecimals(it.fourth)}",
                isPlayer = it.second == saveData.companyName
            )
        }
}

/**
 * 获取销量最高的单机游戏（前5）
 */
fun getTopSinglePlayerGames(saveData: SaveData): List<LeaderboardItem> {
    val allSinglePlayerGames = mutableListOf<Triple<String, String, Long>>()
    
    // 玩家的单机游戏（包含已发售和已评分的游戏）
    saveData.games.filter { 
        it.businessModel == BusinessModel.SINGLE_PLAYER && 
        (it.releaseStatus == GameReleaseStatus.RELEASED || 
         it.releaseStatus == GameReleaseStatus.RATED)
    }.forEach { game ->
        // 从RevenueManager获取真实销量
        val gameRevenue = com.example.yjcy.data.RevenueManager.getGameRevenue(game.id)
        val totalSales = gameRevenue?.getTotalSales() ?: 0L
        allSinglePlayerGames.add(
            Triple(
                game.name,
                saveData.companyName,
                totalSales
            )
        )
    }
    
    // 竞争对手的单机游戏
    saveData.competitors.forEach { competitor ->
        competitor.games.filter { it.businessModel == BusinessModel.SINGLE_PLAYER }
            .forEach { game ->
                allSinglePlayerGames.add(
                    Triple(
                        game.name,
                        competitor.name,
                        game.salesCount
                    )
                )
            }
    }
    
    return allSinglePlayerGames
        .sortedByDescending { it.third }
        .take(5)
        .map { (gameName, companyName, sales) ->
            LeaderboardItem(
                mainText = gameName,
                subText = companyName,
                value = "总销量：${formatMoneyWithDecimals(sales.toDouble())}",
                isPlayer = companyName == saveData.companyName
            )
        }
}

/**
 * 动态新闻内容
 */
@Composable
fun NewsContent(saveData: SaveData) {
    val sortedNews = saveData.competitorNews
        .sortedWith(compareByDescending<CompetitorNews> { it.year }
            .thenByDescending { it.month }
            .thenByDescending { it.day })
        .take(5) // 最多显示5条
    
    if (sortedNews.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无新闻",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            items(sortedNews) { news ->
                NewsCard(news)
            }
        }
    }
}

/**
 * 新闻卡片
 */
@Composable
fun NewsCard(news: CompetitorNews) {
    val backgroundColor = when (news.type) {
        NewsType.NEW_GAME_RELEASE -> Color(0xFF4ECDC4)
        NewsType.PLAYER_MILESTONE -> Color(0xFFFFD93D)
        NewsType.SALES_MILESTONE -> Color(0xFF95E1D3)
        NewsType.RATING_ACHIEVEMENT -> Color(0xFFFF6B6B)
        NewsType.COMPANY_MILESTONE -> Color(0xFF6BCB77)
        NewsType.MARKET_VALUE_CHANGE -> Color(0xFFFFA500)
        NewsType.GAME_UPDATE -> Color(0xFF64B5F6)  // 蓝色（游戏更新）
    }
    
    val typeIcon = when (news.type) {
        NewsType.NEW_GAME_RELEASE -> "🎮"
        NewsType.PLAYER_MILESTONE -> "👥"
        NewsType.SALES_MILESTONE -> "📦"
        NewsType.RATING_ACHIEVEMENT -> "⭐"
        NewsType.COMPANY_MILESTONE -> "🏆"
        NewsType.MARKET_VALUE_CHANGE -> "💰"
        NewsType.GAME_UPDATE -> "🔄"  // 更新图标
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = typeIcon,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = news.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 内容
            Text(
                text = news.content,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            
            // 时间戳
            Text(
                text = "第${news.year}年 ${news.month}月${news.day}日",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * 竞争对手列表内容
 */
@Composable
fun CompetitorsListContent(
    saveData: SaveData,
    onAcquisitionSuccess: (CompetitorCompany, Long, Long, Long, List<GameIP>) -> Unit = { _, _, _, _, _ -> },
    onAIWin: (CompetitorCompany, CompetitorCompany, Long) -> Unit = { _, _, _ -> } // AI获胜回调
) {
    var selectedCompetitor by remember { mutableStateOf<CompetitorCompany?>(null) }
    var showPlayerDetail by remember { mutableStateOf(false) }
    
    // 创建玩家公司数据（用于显示在列表中）
    val playerMarketValue = calculatePlayerMarketValue(saveData)
    val playerCompanyData = CompetitorCompany(
        id = -1, // 使用-1标识玩家公司
        name = saveData.companyName,
        logo = "🎮", // 可以根据实际情况调整
        marketValue = playerMarketValue,
        fans = saveData.fans,
        games = emptyList(), // 稍后在详情中显示真实游戏
        yearsFounded = saveData.currentYear,
        reputation = 0f
    )
    
    // 合并玩家公司和竞争对手，按市值排序
    val allCompanies = (listOf(playerCompanyData) + saveData.competitors)
        .sortedByDescending { it.marketValue }
    
    if (allCompanies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            itemsIndexed(allCompanies) { index, company ->
                val isPlayer = company.id == -1
                CompetitorCard(
                    rank = index + 1,
                    competitor = company,
                    onClick = { 
                        if (isPlayer) {
                            showPlayerDetail = true
                        } else {
                            selectedCompetitor = company
                        }
                    },
                    isPlayer = isPlayer,
                    playerGameCount = if (isPlayer) saveData.games.size else 0
                )
            }
        }
    }
    
    // 竞争对手详情对话框
    if (selectedCompetitor != null) {
        CompetitorDetailDialog(
            competitor = selectedCompetitor!!,
            onDismiss = { selectedCompetitor = null },
            saveData = saveData,
            onAcquisitionSuccess = { company: CompetitorCompany, price: Long, marketValueGain: Long, fansGain: Long, ips: List<GameIP> ->
                // 收购成功后关闭对话框，并触发外层回调
                selectedCompetitor = null
                onAcquisitionSuccess(company, price, marketValueGain, fansGain, ips)
            },
            onAIWin = { acquirer, acquired, price ->
                // AI获胜后关闭对话框，并触发外层回调
                selectedCompetitor = null
                onAIWin(acquirer, acquired, price)
            }
        )
    }
    
    // 玩家公司详情对话框（显示真实游戏列表）
    if (showPlayerDetail) {
        PlayerCompanyDetailDialog(
            saveData = saveData,
            onDismiss = { showPlayerDetail = false }
        )
    }
}

/**
 * 竞争对手卡片
 */
@Composable
fun CompetitorCard(
    rank: Int,
    competitor: CompetitorCompany,
    onClick: () -> Unit,
    isPlayer: Boolean = false,
    playerGameCount: Int = 0 // 玩家公司的游戏数量（仅当isPlayer为true时使用）
) {
    val isTop3 = rank <= 3 && !isPlayer
    
    // 动态颜色：根据排名生成不同的渐变色
    val rankColors = when {
        rank == 1 -> listOf(Color(0xFFFFD700), Color(0xFFFFEB3B), Color(0xFFFFC107)) // 第1名金色
        rank == 2 -> listOf(Color(0xFFC0C0C0), Color(0xFFE0E0E0), Color(0xFFBDBDBD)) // 第2名银色
        rank == 3 -> listOf(Color(0xFFFFA726), Color(0xFFFFB74D), Color(0xFFFFCC80)) // 第3名橙金色
        rank <= 6 -> listOf(Color(0xFF667eea), Color(0xFF764ba2)) // 4-6名紫色
        else -> listOf(Color(0xFF4A5568), Color(0xFF2D3748)) // 其他灰色
    }
    
    val primaryColor = rankColors[0]
    
    // 前3名的动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "competitor_card_$rank")
    
    // 主光环旋转
    val mainLightAngle = if (isTop3) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = when (rank) {
                        1 -> 3000
                        2 -> 3500
                        3 -> 4000
                        else -> 3000
                    },
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "main_light_angle"
        ).value
    } else 0f
    
    // 粒子强度
    val particleIntensity = if (isTop3) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particle_intensity"
        ).value
    } else 1f
    
    // 脉冲动画
    val pulseScale = if (isTop3) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = when (rank) {
                        1 -> 2000
                        2 -> 2300
                        3 -> 2600
                        else -> 2000
                    },
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        ).value
    } else 1f
    
    // 光晕强度
    val glowAlpha = if (isTop3) {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1800,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        ).value
    } else 0.6f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer {
                // 脉冲缩放和阴影
                if (isTop3) {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    shadowElevation = glowAlpha * 10f
                } else if (isPlayer) {
                    shadowElevation = 8f
                }
            }
            .drawBehind {
                if (isPlayer) {
                    // 玩家专属：简洁的绿色光带
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50).copy(alpha = 0.4f),
                                Color(0xFF4CAF50).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    drawRect(
                        color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                        size = androidx.compose.ui.geometry.Size(4f, size.height)
                    )
                } else if (isTop3) {
                    // ========== 多彩流光扫描特效系统 ==========
                    
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    
                    // 根据排名定义彩虹色系
                    val rainbowColors = when (rank) {
                        1 -> listOf(  // 🥇金色彩虹
                            Color(0xFFFFD700), Color(0xFFFF69B4), Color(0xFF00CED1),
                            Color(0xFF7B68EE), Color(0xFFFF6347), Color(0xFFFFD700)
                        )
                        2 -> listOf(  // 🥈银蓝彩虹
                            Color(0xFFC0C0C0), Color(0xFF4169E1), Color(0xFF00BFFF),
                            Color(0xFF9370DB), Color(0xFF87CEEB), Color(0xFFC0C0C0)
                        )
                        3 -> listOf(  // 🥉铜橙彩虹
                            Color(0xFFCD7F32), Color(0xFFFF4500), Color(0xFFFF8C00),
                            Color(0xFFFFD700), Color(0xFFFF6347), Color(0xFFCD7F32)
                        )
                        else -> rankColors + rankColors.first()
                    }
                    
                    // ========== 1. 动态背景层 ==========
                    
                    // 背景脉冲呼吸光晕
                    val bgColor1 = rainbowColors[0]
                    val bgColor2 = rainbowColors[1]
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                bgColor1.copy(alpha = 0.15f * glowAlpha),
                                bgColor2.copy(alpha = 0.12f * glowAlpha),
                                bgColor1.copy(alpha = 0.08f * glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
                    
                    // 背景流动波纹（从左到右）
                    val waveProgress = (mainLightAngle / 360f)
                    for (i in 0..2) {
                        val xOffset = ((waveProgress + i * 0.33f) % 1f) * size.width
                        val waveWidth = size.width * 0.4f
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    rainbowColors[i % rainbowColors.size].copy(alpha = 0.08f),
                                    rainbowColors[(i + 1) % rainbowColors.size].copy(alpha = 0.12f),
                                    rainbowColors[i % rainbowColors.size].copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startX = xOffset - waveWidth / 2,
                                endX = xOffset + waveWidth / 2
                            ),
                            topLeft = androidx.compose.ui.geometry.Offset(xOffset - waveWidth / 2, 0f),
                            size = androidx.compose.ui.geometry.Size(waveWidth, size.height)
                        )
                    }
                    
                    // 背景光点粒子（随机飘动）
                    val sparkleCount = when (rank) {
                        1 -> 12
                        2 -> 8
                        3 -> 6
                        else -> 5
                    }
                    
                    for (i in 0 until sparkleCount) {
                        val angle = (mainLightAngle * 0.5f + i * (360f / sparkleCount)) % 360f
                        val x = (kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() + 1f) / 2f * size.width
                        val y = (kotlin.math.cos(Math.toRadians((angle * 1.3f).toDouble())).toFloat() + 1f) / 2f * size.height
                        
                        val sparkleSize = when (rank) {
                            1 -> 3f
                            2 -> 2.5f
                            else -> 2f
                        }
                        
                        val colorIndex = i % rainbowColors.size
                        drawCircle(
                            color = rainbowColors[colorIndex].copy(alpha = 0.4f * glowAlpha),
                            radius = sparkleSize,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                    
                    // ========== 2. 四边流光扫描层 ==========
                    
                    val streamCount = when (rank) {
                        1 -> 3  // 第1名：3条流光
                        2 -> 2  // 第2名：2条流光
                        3 -> 1  // 第3名：1条流光
                        else -> 1
                    }
                    
                    val streamWidth = when (rank) {
                        1 -> 0.25f
                        2 -> 0.20f
                        3 -> 0.15f
                        else -> 0.15f
                    }
                    
                    val borderProgress = mainLightAngle / 360f
                    val perimeter = 2 * (size.width + size.height)
                    
                    for (streamIndex in 0 until streamCount) {
                        val offset = (streamIndex * (1f / streamCount))
                        val currentPos = perimeter * ((borderProgress + offset) % 1f)
                        val lightLength = perimeter * streamWidth
                        
                        val colorPhase = ((borderProgress + offset) * rainbowColors.size).toInt() % rainbowColors.size
                        val color1 = rainbowColors[colorPhase]
                        val color2 = rainbowColors[(colorPhase + 1) % rainbowColors.size]
                        val color3 = rainbowColors[(colorPhase + 2) % rainbowColors.size]
                        
                        fun drawBorderLight(startPos: Float, length: Float) {
                            var remainingLength = length
                            var currentPos = startPos % perimeter
                            
                            while (remainingLength > 0.1f) {
                                currentPos = currentPos % perimeter
                                
                                when {
                                    currentPos < size.width -> {
                                        val x = currentPos
                                        val edgeRemaining = size.width - x
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color3.copy(alpha = 0.5f * progress),
                                                    Color.Transparent
                                                ),
                                                startX = x,
                                                endX = x + drawLength
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                                            size = androidx.compose.ui.geometry.Size(drawLength, 6f)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                    
                                    currentPos < size.width + size.height -> {
                                        val y = currentPos - size.width
                                        val edgeRemaining = size.height - y
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color3.copy(alpha = 0.5f * progress),
                                                    Color.Transparent
                                                ),
                                                startY = y,
                                                endY = y + drawLength
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(size.width - 6f, y),
                                            size = androidx.compose.ui.geometry.Size(6f, drawLength)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                    
                                    currentPos < 2 * size.width + size.height -> {
                                        val traveled = currentPos - size.width - size.height
                                        val edgeRemaining = size.width - traveled
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val x = size.width - traveled
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    color3.copy(alpha = 0.5f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    Color.Transparent
                                                ),
                                                startX = x - drawLength,
                                                endX = x
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(x - drawLength, size.height - 6f),
                                            size = androidx.compose.ui.geometry.Size(drawLength, 6f)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                    
                                    else -> {
                                        val traveled = currentPos - 2 * size.width - size.height
                                        val edgeRemaining = size.height - traveled
                                        val drawLength = kotlin.math.min(remainingLength, edgeRemaining)
                                        val y = size.height - traveled
                                        val progress = remainingLength / length
                                        
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    color3.copy(alpha = 0.5f * progress),
                                                    color2.copy(alpha = 0.7f * progress),
                                                    color1.copy(alpha = 0.85f * progress),
                                                    Color.White.copy(alpha = 0.9f * progress),
                                                    Color.Transparent
                                                ),
                                                startY = y - drawLength,
                                                endY = y
                                            ),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, y - drawLength),
                                            size = androidx.compose.ui.geometry.Size(6f, drawLength)
                                        )
                                        currentPos += drawLength
                                        remainingLength -= drawLength
                                    }
                                }
                            }
                        }
                        
                        drawBorderLight(currentPos, lightLength)
                    }
                    
                    // ========== 3. 环绕彩色粒子层 ==========
                    
                    val particleCount = when (rank) {
                        1 -> 6
                        2 -> 5
                        3 -> 4
                        else -> 4
                    }
                    
                    val particleSize = when (rank) {
                        1 -> 35f
                        2 -> 30f
                        3 -> 25f
                        else -> 25f
                    }
                    
                    for (i in 0 until particleCount) {
                        val angle = Math.toRadians((mainLightAngle + i * (360f / particleCount)).toDouble())
                        val radiusX = size.width * 0.45f
                        val radiusY = size.height * 0.42f
                        
                        val x = centerX + kotlin.math.cos(angle).toFloat() * radiusX
                        val y = centerY + kotlin.math.sin(angle).toFloat() * radiusY
                        
                        val colorIndex = i % (rainbowColors.size - 1)
                        val color = rainbowColors[colorIndex]
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.8f),
                                    color.copy(alpha = 0.7f),
                                    color.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                radius = particleSize * 1.2f
                            ),
                            radius = particleSize,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                } else {
                    // 普通竞争对手：淡淡的渐变光晕
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = size.width * 0.6f
                        )
                    )
                    // 左侧装饰条（根据排名颜色）
                    drawRect(
                        brush = Brush.verticalGradient(colors = rankColors.map { it.copy(alpha = 0.6f) }),
                        size = androidx.compose.ui.geometry.Size(3f, size.height)
                    )
                }
            }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // 排名徽章（前3名增强版）
            Box(
                modifier = Modifier.width(if (isTop3) 72.dp else 56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTop3) {
                    // 前3名 - 超炫光环徽章
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // 外层光环
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .graphicsLayer {
                                    alpha = glowAlpha * 0.6f
                                }
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.3f),
                                            primaryColor.copy(alpha = 0.15f),
                                            primaryColor.copy(alpha = 0.05f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // 中层光环
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .graphicsLayer {
                                    alpha = glowAlpha * 0.8f
                                }
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = rankColors.map { it.copy(alpha = 0.4f) } + listOf(Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // 核心徽章
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    ambientColor = primaryColor,
                                    spotColor = primaryColor
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.9f),
                                            primaryColor.copy(alpha = 0.6f),
                                            primaryColor.copy(alpha = 0.3f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.8f),
                                            primaryColor.copy(alpha = 0.6f),
                                            Color.White.copy(alpha = 0.8f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$rank",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.graphicsLayer {
                                    shadowElevation = 12f
                                }
                            )
                        }
                        
                        // 顶部装饰图标
                        Text(
                            text = when (rank) {
                                1 -> "👑"
                                2 -> "⭐"
                                3 -> "🏆"
                                else -> ""
                            },
                            fontSize = 18.sp,
                            modifier = Modifier
                                .offset(y = (-28).dp)
                                .graphicsLayer {
                                    alpha = particleIntensity
                                    scaleX = 0.8f + particleIntensity * 0.4f
                                    scaleY = 0.8f + particleIntensity * 0.4f
                                }
                        )
                    }
                } else {
                    // 普通排名徽章
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = rankColors + listOf(rankColors.last().copy(alpha = 0.3f))
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Logo
            Text(
                text = competitor.logo,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // 公司信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 公司名
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPlayer) {
                        Text(
                            text = "👤 ",
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = competitor.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    // 前3名添加闪光特效
                    if (isTop3 && !isPlayer) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✨",
                            fontSize = 14.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = particleIntensity
                                scaleX = 0.8f + particleIntensity * 0.4f
                                scaleY = 0.8f + particleIntensity * 0.4f
                            }
                        )
                    }
                }
                
                // 成立年份和游戏数
                Text(
                    text = "成立${competitor.yearsFounded}年 | ${if (isPlayer) playerGameCount else competitor.games.size}款游戏",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                
                // 市值和粉丝
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💰${formatMoney(competitor.marketValue)}",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "❤️${formatMoneyWithDecimals(competitor.fans.toDouble())}",
                        color = Color(0xFFFF6B6B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            
            // 查看详情图标
            Text(
                text = "▶",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 20.sp
            )
        }
}

/**
 * 竞争对手详情对话框
 */
@Composable
fun CompetitorDetailDialog(
    competitor: CompetitorCompany,
    onDismiss: () -> Unit,
    saveData: SaveData,
    gameSpeed: Int = 1,
    onAcquisitionSuccess: (CompetitorCompany, Long, Long, Long, List<GameIP>) -> Unit = { _, _, _, _, _ -> },
    onAIWin: (CompetitorCompany, CompetitorCompany, Long) -> Unit = { _, _, _ -> } // AI获胜回调
) {
    var showAcquisitionDialog by remember { mutableStateOf(false) }
    val playerMarketValue = calculatePlayerMarketValue(saveData)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = competitor.logo,
                        fontSize = 36.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = competitor.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "成立第${competitor.yearsFounded}年",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                
                // 公司统计
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = "💰",
                        label = "市值",
                        value = formatMoney(competitor.marketValue)
                    )
                    StatItem(
                        icon = "❤️",
                        label = "粉丝",
                        value = formatMoneyWithDecimals(competitor.fans.toDouble())
                    )
                    StatItem(
                        icon = "🎮",
                        label = "游戏",
                        value = "${competitor.games.size}"
                    )
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                
                // 游戏列表
                Text(
                    text = "游戏作品",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(competitor.games) { game ->
                        CompetitorGameCard(game)
                    }
                }
                
                // 按钮区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 收购按钮
                    Button(
                        onClick = { showAcquisitionDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B6B)
                        )
                    ) {
                        Text("💰 收购", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    // 关闭按钮
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        )
                    ) {
                        Text("关闭", color = Color.White)
                    }
                }
            }
        }
    }
    
    // 收购对话框
    if (showAcquisitionDialog) {
        AcquisitionDialog(
            targetCompany = competitor,
            saveData = saveData,
            playerMarketValue = playerMarketValue,
            gameSpeed = gameSpeed,
            onDismiss = { showAcquisitionDialog = false },
            onSuccess = { finalPrice: Long, marketValueGain: Long, fansGain: Long, inheritedIPs: List<GameIP> ->
                showAcquisitionDialog = false
                onDismiss()
                onAcquisitionSuccess(competitor, finalPrice, marketValueGain, fansGain, inheritedIPs)
            },
            onAIWin = { acquirer: CompetitorCompany, acquired: CompetitorCompany, price: Long ->
                showAcquisitionDialog = false
                onDismiss()
                onAIWin(acquirer, acquired, price)
            }
        )
    }
}

/**
 * 统计项目
 */
@Composable
fun StatItem(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

/**
 * 玩家公司详情对话框
 */
@Composable
fun PlayerCompanyDetailDialog(
    saveData: SaveData,
    onDismiss: () -> Unit
) {
    val playerMarketValue = calculatePlayerMarketValue(saveData)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 36.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "👤 ",
                                fontSize = 20.sp
                            )
                            Text(
                                text = saveData.companyName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Text(
                            text = "成立第${saveData.currentYear}年",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                
                // 公司统计
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = "💰",
                        label = "市值",
                        value = formatMoney(playerMarketValue)
                    )
                    StatItem(
                        icon = "❤️",
                        label = "粉丝",
                        value = formatMoneyWithDecimals(saveData.fans.toDouble())
                    )
                    StatItem(
                        icon = "🎮",
                        label = "游戏",
                        value = "${saveData.games.size}"
                    )
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                
                // 游戏列表
                Text(
                    text = "游戏作品",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (saveData.games.isEmpty()) {
                    Text(
                        text = "还没有发布游戏",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(saveData.games) { game ->
                            PlayerGameCard(game)
                        }
                    }
                }
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("关闭", color = Color.White)
                }
            }
        }
    }
}

/**
 * 玩家游戏卡片
 */
@Composable
fun PlayerGameCard(game: com.example.yjcy.data.Game) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = game.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = when (game.businessModel) {
                        BusinessModel.SINGLE_PLAYER -> "单机"
                        BusinessModel.ONLINE_GAME -> "网游"
                    },
                    color = when (game.businessModel) {
                        BusinessModel.SINGLE_PLAYER -> Color(0xFF95E1D3)
                        BusinessModel.ONLINE_GAME -> Color(0xFF4ECDC4)
                    },
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(
                            color = when (game.businessModel) {
                                BusinessModel.SINGLE_PLAYER -> Color(0xFF95E1D3).copy(alpha = 0.2f)
                                BusinessModel.ONLINE_GAME -> Color(0xFF4ECDC4).copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            
            Text(
                text = "${game.theme.displayName} | ${game.platforms.joinToString(", ") { it.displayName }}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 竞争对手游戏卡片
 */
@Composable
fun CompetitorGameCard(game: CompetitorGame) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "⭐${String.format(Locale.getDefault(), "%.1f", game.rating)}",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp
                )
            }
            
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = game.theme.displayName,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = when (game.businessModel) {
                        BusinessModel.ONLINE_GAME -> "网游"
                        BusinessModel.SINGLE_PLAYER -> "单机"
                    },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            // 显示玩家数或销量
            if (game.businessModel == BusinessModel.ONLINE_GAME && game.activePlayers > 0) {
                Text(
                    text = "👥 活跃玩家: ${formatMoneyWithDecimals(game.activePlayers.toDouble())}",
                    color = Color(0xFF4ECDC4),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (game.businessModel == BusinessModel.SINGLE_PLAYER && game.salesCount > 0) {
                Text(
                    text = "📦 销量: ${formatMoneyWithDecimals(game.salesCount.toDouble())}",
                    color = Color(0xFF95E1D3),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * 收购竞价对话框
 */
@Composable
fun AcquisitionDialog(
    targetCompany: CompetitorCompany,
    saveData: SaveData,
    playerMarketValue: Long,
    @Suppress("UNUSED_PARAMETER") gameSpeed: Int = 1,
    onDismiss: () -> Unit,
    onSuccess: (Long, Long, Long, List<GameIP>) -> Unit,
    onAIWin: (CompetitorCompany, CompetitorCompany, Long) -> Unit = { _, _, _ -> } // AI获胜回调：(收购方, 被收购方, 价格)
) {
    // 检查资格
    val eligibilityStatus = remember {
        com.example.yjcy.data.CompetitorManager.checkAcquisitionEligibility(
            playerMarketValue = playerMarketValue,
            playerMoney = saveData.money,
            targetCompany = targetCompany,
            isTargetPlayer = false
        )
    }
    
    // 竞价状态
    var biddingPhase by remember { mutableStateOf("checking") } // checking, bidding, finished
    var currentPrice by remember { mutableLongStateOf(0L) }
    var currentLeader by remember { mutableStateOf("") }
    var biddingHistory by remember { mutableStateOf(listOf<com.example.yjcy.data.AcquisitionBid>()) }
    var biddingCompetitors by remember { mutableStateOf(listOf<CompetitorCompany>()) }
    var canPlayerBid by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    
    // 玩家加价触发器
    var triggerAIBidding by remember { mutableIntStateOf(0) }
    
    // 倒计时状态（秒）
    var countdown by remember { mutableIntStateOf(0) }
    
    // AI竞价处理中标志（防止并发执行）
    var isProcessingAIBidding by remember { mutableStateOf(false) }
    
    // 收购成功结果状态（用于无竞争对手时的可靠回调）
    var pendingSuccessResult by remember { mutableStateOf<Triple<Long, Long, Pair<Long, List<GameIP>>>?>(null) }
    var hasTriggeredSuccessCallback by remember { mutableStateOf(false) }
    
    // 玩家加价函数
    fun playerRaiseBid() {
        val increaseRate = 0.1
        val newPrice = (currentPrice * (1 + increaseRate)).toLong()
        
        if (saveData.money >= newPrice) {
            currentPrice = newPrice
            currentLeader = saveData.companyName
            canPlayerBid = true
            
            biddingHistory = biddingHistory + com.example.yjcy.data.AcquisitionBid(
                bidderId = -1,
                bidderName = saveData.companyName,
                amount = newPrice
            )
            
            // 触发下一轮AI竞价
            triggerAIBidding++
        }
    }
    
    // 初始化竞价和处理AI轮次
    LaunchedEffect(eligibilityStatus, triggerAIBidding) {
        // AI竞价处理函数（改用循环替代递归）
        suspend fun processAIRound() {
            // 防止并发执行
            if (isProcessingAIBidding) {
                return
            }
            isProcessingAIBidding = true
            
            try {
                // 使用while循环替代递归，避免栈溢出和状态混乱
                while (true) {
                    val (hasAIBid, newPrice, aiCompany) = com.example.yjcy.data.CompetitorManager.processAIBidding(
                        currentPrice = currentPrice,
                        targetCompany = targetCompany,
                        biddingCompetitors = biddingCompetitors
                    )
                    
                    if (!hasAIBid || aiCompany == null) {
                        // 竞价结束
                        break
                    }
                    
                    // AI出价
                    currentPrice = newPrice
                    currentLeader = aiCompany.name
                    canPlayerBid = saveData.money >= newPrice
                    
                    biddingHistory = biddingHistory + com.example.yjcy.data.AcquisitionBid(
                        bidderId = aiCompany.id,
                        bidderName = aiCompany.name,
                        amount = newPrice
                    )
                    
                    // 倒计时5秒后继续下一轮
                    for (i in 5 downTo 1) {
                        countdown = i
                        kotlinx.coroutines.delay(1000L)
                    }
                    countdown = 0
                }
                
                // 竞价结束，处理结果
                biddingPhase = "finished"
                
                if (currentLeader == saveData.companyName) {
                    // 玩家获胜
                    val (marketValueGain, fansGain, inheritedIPs) = 
                        com.example.yjcy.data.CompetitorManager.completeAcquisition(
                            targetCompany = targetCompany,
                            finalPrice = currentPrice,
                            acquiredYear = saveData.currentYear,
                            acquiredMonth = saveData.currentMonth
                        )
                    
                    resultMessage = "🎉 收购成功！\n\n" +
                        "以 ${formatMoney(currentPrice)} 成功收购 ${targetCompany.name}\n\n" +
                        "收益：\n" +
                        "• 市值增加：${formatMoney(marketValueGain)}\n" +
                        "• 粉丝增加：${formatMoneyWithDecimals(fansGain.toDouble())}\n" +
                        "• 获得IP：${inheritedIPs.size}个"
                    
                    showResult = true
                    
                    kotlinx.coroutines.delay(1000)
                    onSuccess(currentPrice, marketValueGain, fansGain, inheritedIPs)
                } else {
                    // AI获胜 - 触发AI收购逻辑
                    val winnerCompany = biddingCompetitors.find { it.name == currentLeader }
                    if (winnerCompany != null) {
                        // 调用AI获胜回调，传递收购方、被收购方、价格
                        onAIWin(winnerCompany, targetCompany, currentPrice)
                    }
                    
                    resultMessage = "😞 收购失败\n\n" +
                        "$currentLeader 以 ${formatMoney(currentPrice)} 的价格\n" +
                        "成功收购了 ${targetCompany.name}"
                    
                    showResult = true
                }
            } finally {
                // 确保标志被重置
                isProcessingAIBidding = false
            }
        }
        
        // 初始化竞价
        if (eligibilityStatus == com.example.yjcy.data.AcquisitionStatus.ELIGIBLE && triggerAIBidding == 0) {
            kotlinx.coroutines.delay(500)
            
            // 发起收购
            val (basePrice, competitors) = com.example.yjcy.data.CompetitorManager.initiateAcquisition(
                targetCompany = targetCompany,
                allCompetitors = saveData.competitors,
                playerMarketValue = playerMarketValue
            )
            
            currentPrice = basePrice
            biddingCompetitors = competitors
            currentLeader = saveData.companyName
            
            biddingHistory = listOf(
                com.example.yjcy.data.AcquisitionBid(
                    bidderId = -1,
                    bidderName = saveData.companyName,
                    amount = basePrice
                )
            )
            
            biddingPhase = "bidding"
            
            // 如果有竞争对手，开始AI竞价（初始延迟2秒）
            if (competitors.isNotEmpty()) {
                kotlinx.coroutines.delay(2000L)
                processAIRound()
            } else {
                // 没有竞争对手，玩家直接获胜
                kotlinx.coroutines.delay(1000L)
                biddingPhase = "finished"
                
                val (marketValueGain, fansGain, inheritedIPs) = 
                    com.example.yjcy.data.CompetitorManager.completeAcquisition(
                        targetCompany = targetCompany,
                        finalPrice = currentPrice,
                        acquiredYear = saveData.currentYear,
                        acquiredMonth = saveData.currentMonth
                    )
                
                resultMessage = "🎉 收购成功！\n\n" +
                    "以 ${formatMoney(currentPrice)} 成功收购 ${targetCompany.name}\n" +
                    "（无竞争对手参与竞价）\n\n" +
                    "收益：\n" +
                    "• 市值增加：${formatMoney(marketValueGain)}\n" +
                    "• 粉丝增加：${formatMoneyWithDecimals(fansGain.toDouble())}\n" +
                    "• 获得IP：${inheritedIPs.size}个"
                
                showResult = true
                
                // 保存收购成功结果，在单独的LaunchedEffect中触发回调
                // 避免LaunchedEffect被取消导致回调未执行
                pendingSuccessResult = Triple(currentPrice, marketValueGain, Pair(fansGain, inheritedIPs))
            }
        }
        
        // 玩家加价后触发AI竞价（固定5秒延迟，并显示倒计时）
        // 但如果AI竞价已在处理中，则跳过以避免并发
        if (triggerAIBidding > 0 && biddingPhase == "bidding" && !isProcessingAIBidding) {
            countdown = 5
            for (i in 5 downTo 1) {
                countdown = i
                kotlinx.coroutines.delay(1000L)
            }
            countdown = 0
            processAIRound()
        }
    }
    
    // 单独处理无竞争对手时的收购成功回调
    LaunchedEffect(pendingSuccessResult, hasTriggeredSuccessCallback) {
        if (pendingSuccessResult != null && !hasTriggeredSuccessCallback) {
            val result = pendingSuccessResult!!
            val (finalPrice, marketValueGain, fansAndIPs) = result
            val (fansGain, inheritedIPs) = fansAndIPs
            
            // 延迟一小段时间，确保UI状态已更新
            kotlinx.coroutines.delay(500)
            
            // 标记已触发，避免重复触发
            hasTriggeredSuccessCallback = true
            
            // 触发收购成功回调
            onSuccess(finalPrice, marketValueGain, fansGain, inheritedIPs)
        }
    }
    
    Dialog(onDismissRequest = { if (biddingPhase == "finished") onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "💰",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "收购 ${targetCompany.name}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "目标市值: ${formatMoney(targetCompany.marketValue)}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    
                    // 倒计时显示（竞价进行中且有倒计时时）
                    if (biddingPhase == "bidding" && countdown > 0) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0xFFFF6B6B).copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFFFF6B6B),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = countdown.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                    
                    // 关闭按钮
                    if (biddingPhase == "finished" || biddingPhase == "checking") {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = "✕",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                
                // 内容区域
                when {
                    eligibilityStatus != com.example.yjcy.data.AcquisitionStatus.ELIGIBLE -> {
                        // 显示资格不符信息
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "❌",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = when (eligibilityStatus) {
                                    com.example.yjcy.data.AcquisitionStatus.INSUFFICIENT_MARKET_VALUE -> 
                                        "市值不足\n需要: ${formatMoney((targetCompany.marketValue * 1.5).toLong())}\n当前: ${formatMoney(playerMarketValue)}"
                                    com.example.yjcy.data.AcquisitionStatus.INSUFFICIENT_FUNDS -> 
                                        "资金不足\n需要: ${formatMoney((targetCompany.marketValue * 1.2).toLong())}\n当前: ${formatMoney(saveData.money)}"
                                    else -> "无法收购"
                                },
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667eea)
                            )
                        ) {
                            Text("关闭", color = Color.White)
                        }
                    }
                    showResult -> {
                        // 显示结果
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentLeader == saveData.companyName) "🎉" else "😞",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = resultMessage,
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667eea)
                            )
                        ) {
                            Text("关闭", color = Color.White)
                        }
                    }
                    else -> {
                        // 竞价进行中
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 当前出价信息
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF667eea).copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "当前最高出价",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = formatMoneyWithDecimals(currentPrice.toDouble()),
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "领先者：",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = currentLeader,
                                            color = if (currentLeader == saveData.companyName) 
                                                Color(0xFF4CAF50) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            
                            // 竞价历史
                            if (biddingCompetitors.isNotEmpty()) {
                                Text(
                                    text = "竞价记录",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(biddingHistory.reversed()) { bid ->
                                        BidHistoryItem(bid, saveData.companyName)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            
                            // 操作按钮
                            if (biddingPhase == "bidding") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 放弃按钮
                                    Button(
                                        onClick = {
                                            biddingPhase = "finished"
                                            resultMessage = "您已放弃收购\n${currentLeader} 将收购 ${targetCompany.name}"
                                            showResult = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF666666)
                                        )
                                    ) {
                                        Text("放弃", color = Color.White)
                                    }
                                    
                                    // 加价按钮
                                    val nextBid = (currentPrice * 1.1).toLong()
                                    Button(
                                        onClick = { playerRaiseBid() },
                                        modifier = Modifier.weight(1f),
                                        enabled = canPlayerBid && saveData.money >= nextBid &&
                                                currentLeader != saveData.companyName,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF6B6B)
                                        )
                                    ) {
                                        Text(
                                            text = if (currentLeader == saveData.companyName) {
                                                "等待中..."
                                            } else if (saveData.money >= nextBid) {
                                                "加价至 ${formatMoneyWithDecimals(nextBid.toDouble())}"
                                            } else {
                                                "资金不足"
                                            },
                                            color = Color.White,
                                            fontSize = 12.sp
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
}

/**
 * 竞价历史记录项
 */
@Composable
fun BidHistoryItem(bid: com.example.yjcy.data.AcquisitionBid, playerName: String) {
    val isPlayer = bid.bidderName == playerName
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isPlayer) Color(0xFF4CAF50).copy(alpha = 0.2f) 
                        else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isPlayer) "👤" else "🏢",
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Text(
            text = bid.bidderName,
            color = if (isPlayer) Color(0xFF4CAF50) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = formatMoneyWithDecimals(bid.amount.toDouble()),
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
