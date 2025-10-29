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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
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
import com.example.yjcy.utils.formatMoneyWithDecimals
import com.example.yjcy.data.GameReleaseStatus
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
    gameSpeed: Int = 1,
    onAcquisitionSuccess: (CompetitorCompany, Long, Long, Int, List<GameIP>) -> Unit = { _, _, _, _, _ -> },
    onAIWin: (CompetitorCompany, CompetitorCompany, Long) -> Unit = { _, _, _ -> } // AI获胜回调
) {
    var selectedTab by remember { mutableStateOf(0) }
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
            .padding(16.dp)
    ) {
        // 标题
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
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 下拉选择器
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.12f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedLeaderboard.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = selectedLeaderboard.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (expanded) "▲" else "▼",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
                                selectedLeaderboard = type
                                expanded = false
                            },
                            modifier = Modifier.background(
                                if (selectedLeaderboard == type) Color.White.copy(alpha = 0.1f) else Color.Transparent
                            )
                        )
                    }
                }
            }
        }
        
        // 显示选中的排行榜
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "leaderboard_${selectedLeaderboard}_${competitorsCount}") {
                when (selectedLeaderboard) {
                    LeaderboardType.MARKET_VALUE -> {
                        LeaderboardCard(
                            title = "市值排行榜",
                            icon = "💰",
                            topColor = Color(0xFFFFD700),
                            items = marketValueItems,
                            leaderboardType = LeaderboardType.MARKET_VALUE
                        )
                    }
                    LeaderboardType.FANS -> {
                        LeaderboardCard(
                            title = "粉丝排行榜",
                            icon = "❤️",
                            topColor = Color(0xFFFF6B6B),
                            items = fansItems,
                            leaderboardType = LeaderboardType.FANS
                        )
                    }
                    LeaderboardType.ONLINE_GAME -> {
                        LeaderboardCard(
                            title = "热门网游排行",
                            icon = "🎮",
                            topColor = Color(0xFF4ECDC4),
                            items = liveLeaderboardItems.ifEmpty { onlineGameItems },
                            leaderboardType = LeaderboardType.ONLINE_GAME
                        )
                    }
                    LeaderboardType.SINGLE_PLAYER -> {
                        LeaderboardCard(
                            title = "畅销单机排行",
                            icon = "📦",
                            topColor = Color(0xFF95E1D3),
                            items = singlePlayerItems,
                            leaderboardType = LeaderboardType.SINGLE_PLAYER
                        )
                    }
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
    leaderboardType: LeaderboardType
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            if (items.isEmpty()) {
                Text(
                    text = "暂无数据",
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
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
    topColor: Color,
    isTop: Boolean,
    isPlayer: Boolean = false,
    leaderboardType: LeaderboardType
) {
    // 为前3名设计专属的超炫酷图标组合
    val rankIconData = when {
        rank == 1 -> when (leaderboardType) {
            LeaderboardType.MARKET_VALUE -> Triple("💎", "✨", listOf(Color(0xFFFFD700), Color(0xFFFFEB3B), Color(0xFFFFC107)))
            LeaderboardType.FANS -> Triple("❤️", "💕", listOf(Color(0xFFFF1744), Color(0xFFFF4081), Color(0xFFFF80AB)))
            LeaderboardType.ONLINE_GAME -> Triple("🔥", "⚡", listOf(Color(0xFFFF5722), Color(0xFFFF6F00), Color(0xFFFFD54F)))
            LeaderboardType.SINGLE_PLAYER -> Triple("👑", "💎", listOf(Color(0xFFFFD700), Color(0xFFFFEB3B), Color(0xFFFFF59D)))
        }
        rank == 2 -> when (leaderboardType) {
            LeaderboardType.MARKET_VALUE -> Triple("💰", "💸", listOf(Color(0xFFC0C0C0), Color(0xFFE0E0E0), Color(0xFFBDBDBD)))
            LeaderboardType.FANS -> Triple("💖", "💗", listOf(Color(0xFFFF4081), Color(0xFFFF80AB), Color(0xFFF48FB1)))
            LeaderboardType.ONLINE_GAME -> Triple("⚡", "🌟", listOf(Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFFD54F)))
            LeaderboardType.SINGLE_PLAYER -> Triple("🎮", "🕹️", listOf(Color(0xFF5C6BC0), Color(0xFF7E57C2), Color(0xFF9575CD))) // 游戏手柄+摇杆，紫蓝色系
        }
        rank == 3 -> when (leaderboardType) {
            LeaderboardType.MARKET_VALUE -> Triple("💵", "💴", listOf(Color(0xFFCD7F32), Color(0xFFD4A574), Color(0xFFE6C9A8)))
            LeaderboardType.FANS -> Triple("💕", "💝", listOf(Color(0xFFF06292), Color(0xFFF48FB1), Color(0xFFF8BBD0)))
            LeaderboardType.ONLINE_GAME -> Triple("⭐", "✨", listOf(Color(0xFF00BCD4), Color(0xFF26C6DA), Color(0xFF4DD0E1)))
            LeaderboardType.SINGLE_PLAYER -> Triple("🏆", "⭐", listOf(Color(0xFFFF6F00), Color(0xFFFF8A65), Color(0xFFFFAB91))) // 奖杯+星星，橙红色系
        }
        else -> Triple("", "", emptyList())
    }
    
    val (mainIcon, particleIcon, gradientColors) = rankIconData
    
    // 创建超强视觉冲击的动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "rank_animation_$rank")
    
    // 彩虹渐变色循环动画 - 超强视觉冲击
    val colorProgress = if (isTop) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "color_animation"
        ).value
    } else 0f
    
    // 强烈闪光效果 - 快速闪烁
    val flashAlpha = if (isTop) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = when (rank) {
                        1 -> 1000
                        2 -> 1200
                        3 -> 1400
                        else -> 1000
                    }
                    0f at 0
                    1f at 100 using FastOutSlowInEasing
                    0.3f at 200
                    1f at 300 using FastOutSlowInEasing
                    0f at durationMillis
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "flash_animation"
        ).value
    } else 0f
    
    // 冲击波扩散效果
    val shockwaveScale = if (isTop) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shockwave_animation"
        ).value
    } else 1f
    
    // 计算当前渐变色
    val currentGradientColor = if (gradientColors.isNotEmpty()) {
        val colorIndex = (colorProgress * gradientColors.size).toInt() % gradientColors.size
        val nextColorIndex = (colorIndex + 1) % gradientColors.size
        val fraction = (colorProgress * gradientColors.size) % 1f
        
        androidx.compose.ui.graphics.lerp(
            gradientColors[colorIndex],
            gradientColors[nextColorIndex],
            fraction
        )
    } else Color.Gray
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    isPlayer -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                    isTop -> currentGradientColor.copy(alpha = 0.15f) // 使用渐变色作为背景
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isPlayer) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else if (isTop) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 超炫酷的动态图标区域（前3名）
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTop && mainIcon.isNotEmpty()) {
                // 冲击波扩散层（爆炸效果）
                Box(
                    modifier = Modifier
                        .size((40 * shockwaveScale).dp)
                        .graphicsLayer {
                            alpha = (1f - (shockwaveScale - 0.5f) / 2f).coerceIn(0f, 1f) * 0.6f
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    currentGradientColor.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // 强烈闪光层
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .graphicsLayer {
                            alpha = flashAlpha * 0.9f
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    currentGradientColor,
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // 粒子层 - 围绕主图标的装饰粒子
                if (particleIcon.isNotEmpty()) {
                    // 4个粒子环绕在四个方向
                    val particlePositions = listOf(
                        Pair(20.dp, 0.dp),    // 右
                        Pair(-20.dp, 0.dp),   // 左
                        Pair(0.dp, -20.dp),   // 上
                        Pair(0.dp, 20.dp)     // 下
                    )
                    
                    particlePositions.forEach { (xOffset, yOffset) ->
                        Text(
                            text = particleIcon,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .offset(x = xOffset, y = yOffset)
                                .graphicsLayer {
                                    alpha = flashAlpha * 0.8f
                                }
                        )
                    }
                }
                
                // 主图标 - 超大尺寸
                Text(
                    text = mainIcon,
                    fontSize = 40.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            // 强烈的阴影效果
                            shadowElevation = 16f
                            // 轻微缩放（保持图标稳定可见）
                            scaleX = 1f + flashAlpha * 0.1f
                            scaleY = 1f + flashAlpha * 0.1f
                        }
                )
            } else {
                // 第4-5名显示普通数字
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Gray,
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 公司/游戏信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlayer) {
                    Text(
                        text = "👤 ",
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = item.mainText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (item.subText.isNotEmpty()) {
                Text(
                    text = item.subText,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 数值（右侧显示区域）
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = item.value,
                color = if (isTop) currentGradientColor else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            if (item.extraInfo.isNotEmpty()) {
                Text(
                    text = item.extraInfo,
                    color = if (isTop) currentGradientColor.copy(alpha = 0.9f) else Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
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
    return saveData.money + // 基础市值：当前资金
        (saveData.fans * 10L) + // 粉丝价值：每个粉丝值10元
        (releasedGamesCount * 100000L) + // 游戏价值：每个已发售游戏增加10万市值
        (saveData.allEmployees.size * 50000L) // 员工价值：每个员工增加5万市值
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
    val allCompanies = mutableListOf<Pair<String, Int>>()
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
        (it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RELEASED || 
         it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RATED)
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
                Tuple4<String, String, Long, Double>(
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
                    Tuple4<String, String, Long, Double>(
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
        (it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RELEASED || 
         it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RATED)
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
                Tuple4<String, String, Long, Double>(
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
        (it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RELEASED || 
         it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RATED)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
    }
    
    val typeIcon = when (news.type) {
        NewsType.NEW_GAME_RELEASE -> "🎮"
        NewsType.PLAYER_MILESTONE -> "👥"
        NewsType.SALES_MILESTONE -> "📦"
        NewsType.RATING_ACHIEVEMENT -> "⭐"
        NewsType.COMPANY_MILESTONE -> "🏆"
        NewsType.MARKET_VALUE_CHANGE -> "💰"
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
                .padding(12.dp)
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
    onAcquisitionSuccess: (CompetitorCompany, Long, Long, Int, List<GameIP>) -> Unit = { _, _, _, _, _ -> },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    isPlayer = isPlayer
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
            onAcquisitionSuccess = { company: CompetitorCompany, price: Long, marketValueGain: Long, fansGain: Int, ips: List<GameIP> ->
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
    isPlayer: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .then(
                if (isPlayer) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayer) {
                Color(0xFF4CAF50).copy(alpha = 0.2f)
            } else {
                Color.White.copy(alpha = 0.12f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 排名
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
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
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPlayer) {
                        Text(
                            text = "👤 ",
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = competitor.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "成立${competitor.yearsFounded}年 | ${competitor.games.size}款游戏",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "💰${formatMoney(competitor.marketValue)}",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "❤️${formatMoneyWithDecimals(competitor.fans.toDouble())}",
                        color = Color(0xFFFF6B6B),
                        fontSize = 11.sp
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
    onAcquisitionSuccess: (CompetitorCompany, Long, Long, Int, List<GameIP>) -> Unit = { _, _, _, _, _ -> },
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
            onSuccess = { finalPrice: Long, marketValueGain: Long, fansGain: Int, inheritedIPs: List<GameIP> ->
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
                .padding(12.dp)
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
                .padding(12.dp)
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
                    text = "⭐${String.format("%.1f", game.rating)}",
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
    gameSpeed: Int = 1,
    onDismiss: () -> Unit,
    onSuccess: (Long, Long, Int, List<GameIP>) -> Unit,
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
    var currentPrice by remember { mutableStateOf(0L) }
    var currentLeader by remember { mutableStateOf("") }
    var biddingHistory by remember { mutableStateOf(listOf<com.example.yjcy.data.AcquisitionBid>()) }
    var biddingCompetitors by remember { mutableStateOf(listOf<CompetitorCompany>()) }
    var canPlayerBid by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    
    // 玩家加价触发器
    var triggerAIBidding by remember { mutableStateOf(0) }
    
    // 倒计时状态（秒）
    var countdown by remember { mutableStateOf(0) }
    
    // AI竞价处理中标志（防止并发执行）
    var isProcessingAIBidding by remember { mutableStateOf(false) }
    
    // 收购成功结果状态（用于无竞争对手时的可靠回调）
    var pendingSuccessResult by remember { mutableStateOf<Triple<Long, Long, Pair<Int, List<GameIP>>>?>(null) }
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
                        "${currentLeader} 以 ${formatMoney(currentPrice)} 的价格\n" +
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
