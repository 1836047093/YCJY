package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.*

/**
 * 赛事中心主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentScreen(
    games: List<Game>,
    revenueDataMap: Map<String, GameRevenue>,
    currentDate: GameDate,
    money: Long,
    fans: Long,
    competitors: List<CompetitorCompany> = emptyList(),
    initialTab: Int = 0,
    onHostTournament: (String, TournamentType) -> Unit
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    val tabs = listOf("🏆 可举办", "⏳ 进行中", "📊 历史记录")
    
    // 筛选游戏
    val eligibleGames = games.filter { game ->
        val revenueData = revenueDataMap[game.id]
        TournamentManager.canHostTournament(game, revenueData)
    }
    
    val ongoingGames = games.filter { it.currentTournament?.status == TournamentStatus.ONGOING }
    val completedGames = games.filter { !it.tournamentHistory.isNullOrEmpty() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // 顶部标题
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🏆 电竞赛事中心",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "为竞技游戏举办电竞赛事，提升热度和收益",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // 标签页
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = Color.Gray,
                    text = {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // 内容区域
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> EligibleGamesTab(
                    games = eligibleGames,
                    revenueDataMap = revenueDataMap,
                    currentDate = currentDate,
                    money = money,
                    onHostTournament = onHostTournament
                )
                1 -> OngoingTournamentsTab(
                    games = ongoingGames,
                    revenueDataMap = revenueDataMap,
                    currentDate = currentDate,
                    competitors = competitors
                )
                2 -> TournamentHistoryTab(
                    games = completedGames
                )
            }
        }
    }
}

/**
 * 可举办赛事标签页
 */
@Composable
fun EligibleGamesTab(
    games: List<Game>,
    revenueDataMap: Map<String, GameRevenue>,
    currentDate: GameDate,
    money: Long,
    onHostTournament: (String, TournamentType) -> Unit
) {
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    
    if (games.isEmpty()) {
        EmptyStateView(
            icon = "🎮",
            title = "暂无可举办赛事的游戏",
            message = "需要：MOBA/射击/体育/竞速/策略类网游、评分≥8.0、活跃玩家≥10万"
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(games) { game ->
            TournamentGameCard(
                game = game,
                revenueData = revenueDataMap[game.id],
                currentDate = currentDate,
                money = money,
                onClick = { selectedGame = game }
            )
        }
    }
    
    // 赛事策划对话框
    selectedGame?.let { game ->
        TournamentPlanDialog(
            game = game,
            revenueData = revenueDataMap[game.id],
            currentDate = currentDate,
            money = money,
            onDismiss = { selectedGame = null },
            onConfirm = { type ->
                onHostTournament(game.id, type)
                selectedGame = null
            }
        )
    }
}

/**
 * 游戏卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentGameCard(
    game: Game,
    revenueData: GameRevenue?,
    currentDate: GameDate,
    money: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = game.theme.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "评分: ${game.rating?.let { "%.1f".format(it) } ?: "未知"}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "活跃: ${formatPlayerCount(revenueData?.getActivePlayers() ?: 0)}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
            
            // 冷却状态
            game.lastTournamentDate?.let { lastDate ->
                val daysSince = calculateDaysBetween(lastDate, currentDate)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "上次赛事: ${daysSince}天前",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

/**
 * 赛事策划对话框
 */
@Composable
fun TournamentPlanDialog(
    game: Game,
    revenueData: GameRevenue?,
    currentDate: GameDate,
    money: Long,
    onDismiss: () -> Unit,
    onConfirm: (TournamentType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🏆 举办电竞赛事 - ${game.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 游戏数据
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📊 游戏数据",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "评分: ${game.rating?.let { "%.1f".format(it) } ?: "未知"} | 活跃玩家: ${formatPlayerCount(revenueData?.getActivePlayers() ?: 0)}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                // 赛事选项
                items(TournamentType.entries.toList()) { type ->
                    TournamentTypeCard(
                        type = type,
                        game = game,
                        revenueData = revenueData,
                        currentDate = currentDate,
                        money = money,
                        onSelect = { onConfirm(type) }
                    )
                }
                
                // 提示
                item {
                    Text(
                        text = "💡 提示：赛事能大幅提升游戏热度和收益，但需要大量投入",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 赛事类型卡片
 */
@Composable
fun TournamentTypeCard(
    type: TournamentType,
    game: Game,
    revenueData: GameRevenue?,
    currentDate: GameDate,
    money: Long,
    onSelect: () -> Unit
) {
    val eligibility = TournamentManager.canHostTournamentType(
        game, revenueData, type, currentDate, money
    )
    
    val isEligible = eligibility is TournamentEligibility.Eligible
    val reason = if (eligibility is TournamentEligibility.NotEligible) eligibility.reason else ""
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isEligible) Modifier.alpha(0.5f) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isEligible) Color.White else Color(0xFFF5F5F5)
        ),
        border = if (isEligible) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${type.icon} ${type.displayName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "投入: ${formatMoney(type.baseCost)} | 周期: ${type.duration}天",
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "预计收益: ${formatMoney((type.baseCost * 0.7).toLong())}-${formatMoney((type.baseCost * 1.2).toLong())}",
                fontSize = 13.sp,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "效果: 粉丝+${(type.fansGrowthMin * 100).toInt()}-${(type.fansGrowthMax * 100).toInt()}%, 活跃+${(type.playersGrowthMin * 100).toInt()}-${(type.playersGrowthMax * 100).toInt()}%",
                fontSize = 13.sp,
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isEligible) {
                Button(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("举办赛事")
                }
            } else {
                Text(
                    text = "❌ $reason",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 进行中的赛事标签页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingTournamentsTab(
    games: List<Game>,
    revenueDataMap: Map<String, GameRevenue>,
    currentDate: GameDate,
    competitors: List<CompetitorCompany>
) {
    // 选择器状态：0=我的公司，1=竞争对手
    var selectedCompanyIndex by remember { mutableIntStateOf(0) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    
    // 构建选择列表：只有两个选项
    val companyOptions = listOf("我的公司", "竞争对手")
    
    // 根据选择获取对应的游戏和公司名称
    val (displayGames, competitorGameDataMap) = if (selectedCompanyIndex == 0) {
        // 显示玩家的游戏
        Pair(games, emptyMap<String, Pair<Long, String>>())
    } else {
        // 显示所有竞争对手的游戏（合并显示）
        val allCompetitorGames = mutableListOf<Game>()
        val competitorDataMap = mutableMapOf<String, Pair<Long, String>>()
        
        competitors.forEach { competitor ->
            competitor.games.filter { it.currentTournament != null }.forEach { compGame ->
                val game = Game(
                    id = compGame.id,
                    name = "${competitor.name} - ${compGame.name}", // 显示公司名+游戏名
                    theme = compGame.theme,
                    platforms = compGame.platforms,
                    businessModel = compGame.businessModel,
                    isCompleted = true,
                    releaseStatus = GameReleaseStatus.RELEASED,
                    rating = compGame.rating,
                    currentTournament = compGame.currentTournament
                )
                allCompetitorGames.add(game)
                // 保存活跃玩家数和公司名
                competitorDataMap[compGame.id] = Pair(compGame.activePlayers, competitor.name)
            }
        }
        Pair(allCompetitorGames, competitorDataMap)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 选择公司按钮
        OutlinedButton(
            onClick = { showSelectionDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFF90CAF9)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = companyOptions[selectedCompanyIndex],
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "选择公司",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
        
        // 赛事列表
        if (displayGames.isEmpty()) {
            EmptyStateView(
                icon = "⏳",
                title = "暂无进行中的赛事",
                message = if (selectedCompanyIndex == 0) "去可举办页面创建新赛事吧！" else "竞争对手暂无进行中的赛事"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayGames) { game ->
                    game.currentTournament?.let { tournament ->
                        // 对于竞争对手的游戏，使用原始游戏ID查找数据
                        val competitorData = if (selectedCompanyIndex != 0) competitorGameDataMap[game.id] else null
                        OngoingTournamentCard(
                            tournament = tournament,
                            game = game,
                            revenueData = if (selectedCompanyIndex == 0) revenueDataMap[game.id] else null,
                            currentDate = currentDate,
                            isCompetitor = selectedCompanyIndex != 0,
                            companyName = competitorData?.second,
                            competitorActivePlayers = competitorData?.first
                        )
                    }
                }
            }
        }
        
        // 选择公司对话框
        if (showSelectionDialog) {
            AlertDialog(
                onDismissRequest = { showSelectionDialog = false },
                title = {
                    Text(
                        text = "选择查看公司",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column {
                        companyOptions.forEachIndexed { index, name ->
                            Button(
                                onClick = {
                                    selectedCompanyIndex = index
                                    showSelectionDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedCompanyIndex == index) 
                                        Color(0xFF64B5F6) else Color(0xFF2D3748)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = name,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSelectionDialog = false }) {
                        Text("取消", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * 进行中的赛事卡片
 */
@Composable
fun OngoingTournamentCard(
    tournament: EsportsTournament,
    game: Game,
    revenueData: GameRevenue?,
    currentDate: GameDate,
    isCompetitor: Boolean = false,
    companyName: String? = null,
    competitorActivePlayers: Long? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${tournament.type.icon} ${tournament.type.displayName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tournament.gameName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // 显示公司名称（仅竞争对手）
                if (companyName != null) {
                    Text(
                        text = companyName,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            val progress = tournament.currentDay.toFloat() / tournament.type.duration.toFloat()
            Column {
                Text(
                    text = "📅 第${tournament.currentDay}天 / 共${tournament.type.duration}天",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 当前阶段
            val stage = when {
                progress < 0.25 -> "小组赛"
                progress < 0.50 -> "淘汰赛"
                progress < 0.75 -> "半决赛"
                else -> "决赛"
            }
            Text(
                text = "🏟️ 当前阶段: $stage",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 实时数据
            val activePlayers = if (isCompetitor && competitorActivePlayers != null) {
                competitorActivePlayers
            } else {
                revenueData?.getActivePlayers() ?: 0L
            }
            Text(
                text = "👥 预计观看: ${formatPlayerCount(activePlayers / 2)}人",
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "📈 热度指数: ${"★".repeat((progress * 5).toInt())}${"☆".repeat(5 - (progress * 5).toInt())}",
                fontSize = 13.sp,
                color = Color(0xFFFFC107)
            )
        }
    }
}

/**
 * 历史记录标签页
 */
@Composable
fun TournamentHistoryTab(
    games: List<Game>
) {
    if (games.isEmpty() || games.all { it.tournamentHistory.isNullOrEmpty() }) {
        EmptyStateView(
            icon = "📊",
            title = "暂无历史记录",
            message = "举办赛事后会显示在这里"
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        games.forEach { game ->
            items(game.tournamentHistory ?: emptyList()) { tournament ->
                TournamentHistoryCard(tournament = tournament)
            }
        }
    }
}

/**
 * 历史赛事卡片
 */
@Composable
fun TournamentHistoryCard(tournament: EsportsTournament) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${tournament.type.icon} ${tournament.type.displayName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tournament.successLevel.displayName,
                    fontSize = 12.sp,
                    color = when (tournament.successLevel) {
                        TournamentSuccessLevel.GREAT_SUCCESS -> Color(0xFF4CAF50)
                        TournamentSuccessLevel.SUCCESS -> Color(0xFF2196F3)
                        TournamentSuccessLevel.AVERAGE -> Color(0xFFFFC107)
                        TournamentSuccessLevel.FAILURE -> Color.Red
                    },
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Text(
                text = tournament.gameName,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "👑 冠军: ${tournament.champion}",
                fontSize = 13.sp
            )
            Text(
                text = "📅 时间: ${tournament.startYear}年${tournament.startMonth}月",
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val netProfit = tournament.getNetProfit()
            Text(
                text = "💰 净利润: ${formatMoney(netProfit)}",
                fontSize = 14.sp,
                color = if (netProfit >= 0) Color(0xFF4CAF50) else Color.Red,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "📈 粉丝+${tournament.fansGained} | 活跃+${tournament.playersGained}",
                fontSize = 13.sp,
                color = Color(0xFF2196F3)
            )
        }
    }
}

/**
 * 空状态视图
 */
@Composable
fun EmptyStateView(
    icon: String,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 辅助函数
private fun formatPlayerCount(count: Long): String {
    return when {
        count >= 10000 -> "${count / 10000}万"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}

private fun formatMoney(amount: Long): String {
    return when {
        amount >= 10000 -> "¥${amount / 10000}万"
        amount >= 1000 -> "¥${amount / 1000}K"
        else -> "¥$amount"
    }
}

private fun calculateDaysBetween(from: GameDate, to: GameDate): Int {
    val yearDiff = to.year - from.year
    val monthDiff = to.month - from.month
    val dayDiff = to.day - from.day
    return yearDiff * 360 + monthDiff * 30 + dayDiff
}

@Composable
fun Modifier.alpha(alpha: Float): Modifier = this.graphicsLayer(alpha = alpha)
