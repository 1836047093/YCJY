package com.example.yjcy.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.CompetitorCompany
import com.example.yjcy.data.CompetitorGame
import com.example.yjcy.data.CompetitorNews
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
    saveData: SaveData
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📊 排行榜", "📰 动态新闻", "🏢 竞争对手")
    
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
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.background(
                        if (selectedTab == index) Color.White.copy(alpha = 0.2f) else Color.Transparent
                    )
                )
            }
        }
        
        // 内容区域
        when (selectedTab) {
            0 -> LeaderboardContent(saveData)
            1 -> NewsContent(saveData)
            2 -> CompetitorsListContent(saveData)
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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                when (selectedLeaderboard) {
                    LeaderboardType.MARKET_VALUE -> {
                        LeaderboardCard(
                            title = "市值排行榜",
                            icon = "💰",
                            topColor = Color(0xFFFFD700),
                            items = getTopCompaniesByMarketValue(saveData)
                        )
                    }
                    LeaderboardType.FANS -> {
                        LeaderboardCard(
                            title = "粉丝排行榜",
                            icon = "❤️",
                            topColor = Color(0xFFFF6B6B),
                            items = getTopCompaniesByFans(saveData)
                        )
                    }
                    LeaderboardType.ONLINE_GAME -> {
                        LeaderboardCard(
                            title = "热门网游排行",
                            icon = "🎮",
                            topColor = Color(0xFF4ECDC4),
                            items = getTopOnlineGames(saveData)
                        )
                    }
                    LeaderboardType.SINGLE_PLAYER -> {
                        LeaderboardCard(
                            title = "畅销单机排行",
                            icon = "📦",
                            topColor = Color(0xFF95E1D3),
                            items = getTopSinglePlayerGames(saveData)
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
    items: List<LeaderboardItem>
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
                    isPlayer = item.isPlayer
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
    isPlayer: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    isPlayer -> Color(0xFF4CAF50).copy(alpha = 0.3f) // 玩家公司用绿色高亮
                    isTop -> topColor.copy(alpha = 0.2f)
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
                } else {
                    Modifier
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 排名
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = if (isTop) {
                        Brush.radialGradient(
                            colors = listOf(
                                topColor,
                                topColor.copy(alpha = 0.6f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Gray,
                                Color.Gray.copy(alpha = 0.6f)
                            )
                        )
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
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
        
        // 数值
        Text(
            text = item.value,
            color = if (isTop) topColor else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/**
 * 排行榜项目数据类
 */
data class LeaderboardItem(
    val mainText: String,
    val subText: String,
    val value: String,
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
    val allOnlineGames = mutableListOf<Triple<String, String, Int>>()
    
    // 玩家的网游（包含已发售和已评分的游戏）
    saveData.games.filter { 
        it.businessModel == BusinessModel.ONLINE_GAME && 
        (it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RELEASED || 
         it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RATED)
    }.forEach { game ->
            // 从RevenueManager获取活跃玩家数（考虑兴趣值影响）
            val activePlayers = com.example.yjcy.data.RevenueManager.getActivePlayers(game.id)
            allOnlineGames.add(
                Triple(
                    game.name,
                    saveData.companyName,
                    activePlayers
                )
            )
        }
    
    // 竞争对手的网游
    saveData.competitors.forEach { competitor ->
        competitor.games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
            .forEach { game ->
                allOnlineGames.add(
                    Triple(
                        game.name,
                        competitor.name,
                        game.activePlayers
                    )
                )
            }
    }
    
    return allOnlineGames
        .sortedByDescending { it.third }
        .take(5)
        .map { (gameName, companyName, players) ->
            LeaderboardItem(
                mainText = gameName,
                subText = companyName,
                value = "活跃玩家：${players / 1000}K",
                isPlayer = companyName == saveData.companyName
            )
        }
}

/**
 * 获取销量最高的单机游戏（前5）
 */
fun getTopSinglePlayerGames(saveData: SaveData): List<LeaderboardItem> {
    val allSinglePlayerGames = mutableListOf<Triple<String, String, Int>>()
    
    // 玩家的单机游戏（包含已发售和已评分的游戏）
    saveData.games.filter { 
        it.businessModel == BusinessModel.SINGLE_PLAYER && 
        (it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RELEASED || 
         it.releaseStatus == com.example.yjcy.data.GameReleaseStatus.RATED)
    }.forEach { game ->
        // 从RevenueManager获取真实销量
        val gameRevenue = com.example.yjcy.data.RevenueManager.getGameRevenue(game.id)
        val totalSales = gameRevenue?.getTotalSales() ?: 0
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
                value = "总销量：${sales / 1000}K",
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
fun CompetitorsListContent(saveData: SaveData) {
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
            onDismiss = { selectedCompetitor = null }
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
    onDismiss: () -> Unit
) {
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
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
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
                    text = "👥 活跃玩家: ${game.activePlayers / 1000}K",
                    color = Color(0xFF4ECDC4),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (game.businessModel == BusinessModel.SINGLE_PLAYER && game.salesCount > 0) {
                Text(
                    text = "📦 销量: ${game.salesCount / 1000}K",
                    color = Color(0xFF95E1D3),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
