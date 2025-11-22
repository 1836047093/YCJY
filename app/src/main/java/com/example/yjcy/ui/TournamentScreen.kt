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
    
    val ongoingGames = games.filter { 
        it.currentTournament != null && 
        it.currentTournament.status != TournamentStatus.COMPLETED 
    }
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
                    games = games,
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
 * 可举办赛事标签页 - 显示所有赛事类型
 */
@Composable
fun EligibleGamesTab(
    games: List<Game>,
    revenueDataMap: Map<String, GameRevenue>,
    currentDate: GameDate,
    money: Long,
    onHostTournament: (String, TournamentType) -> Unit
) {
    var selectedTournamentType by remember { mutableStateOf<TournamentType?>(null) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 显示所有赛事类型
        items(TournamentType.values().toList()) { tournamentType ->
            // 找到符合这个赛事类型条件的游戏
            val eligibleGamesForType = games.filter { game ->
                val revenueData = revenueDataMap[game.id]
                val eligibility = TournamentManager.canHostTournamentType(
                    game, revenueData, tournamentType, currentDate, money
                )
                eligibility is TournamentEligibility.Eligible
            }
            
            TournamentTypeCard(
                tournamentType = tournamentType,
                eligibleGames = eligibleGamesForType,
                totalGames = games.size,
                money = money,
                onClick = {
                    if (eligibleGamesForType.isNotEmpty()) {
                        selectedTournamentType = tournamentType
                    }
                }
            )
        }
    }
    
    // 游戏选择对话框
    selectedTournamentType?.let { type ->
        val eligibleGamesForType = games.filter { game ->
            val revenueData = revenueDataMap[game.id]
            val eligibility = TournamentManager.canHostTournamentType(
                game, revenueData, type, currentDate, money
            )
            eligibility is TournamentEligibility.Eligible
        }
        
        TournamentGameSelectionDialog(
            tournamentType = type,
            games = eligibleGamesForType,
            revenueDataMap = revenueDataMap,
            currentDate = currentDate,
            money = money,
            onDismiss = { selectedTournamentType = null },
            onSelectGame = { game ->
                // 不直接举办，而是显示确认对话框
                selectedGame = game
            }
        )
    }
    
    // 赛事确认对话框
    if (selectedGame != null && selectedTournamentType != null) {
        TournamentConfirmDialog(
            game = selectedGame!!,
            tournamentType = selectedTournamentType!!,
            revenueData = revenueDataMap[selectedGame!!.id],
            currentDate = currentDate,
            money = money,
            onDismiss = { 
                selectedGame = null
            },
            onConfirm = {
                onHostTournament(selectedGame!!.id, selectedTournamentType!!)
                selectedGame = null
                selectedTournamentType = null
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
@OptIn(ExperimentalMaterial3Api::class)
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
    var showDetailDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDetailDialog = true },
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
            
            // 当前阶段显示（使用新的阶段系统）
            val currentStage = tournament.getCurrentStage()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when (currentStage) {
                            TournamentStage.PREPARATION -> Color(0xFFE3F2FD)
                            TournamentStage.GROUP_STAGE -> Color(0xFFFFF3E0)
                            TournamentStage.KNOCKOUT -> Color(0xFFFFEBEE)
                            TournamentStage.SEMIFINALS -> Color(0xFFFCE4EC)
                            TournamentStage.FINALS -> Color(0xFFFFF9C4)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentStage.icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = currentStage.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = currentStage.description,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            val progress = if (tournament.status == TournamentStatus.PREPARING) {
                tournament.currentDay.toFloat() / tournament.preparationDays.toFloat()
            } else {
                tournament.currentDay.toFloat() / tournament.type.duration.toFloat()
            }
            
            Column {
                Text(
                    text = tournament.getStageProgressText(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = when (currentStage) {
                        TournamentStage.PREPARATION -> Color(0xFF2196F3)
                        TournamentStage.GROUP_STAGE -> Color(0xFFFF9800)
                        TournamentStage.KNOCKOUT -> Color(0xFFF44336)
                        TournamentStage.SEMIFINALS -> Color(0xFFE91E63)
                        TournamentStage.FINALS -> Color(0xFFFFEB3B)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 实时数据（仅在正式比赛时显示）
            if (tournament.status == TournamentStatus.ONGOING) {
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
            } else if (tournament.status == TournamentStatus.PREPARING) {
                Text(
                    text = "🔧 正在筹备中，点击查看详情...",
                    fontSize = 13.sp,
                    color = Color(0xFF2196F3),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
    
    // 赛事详情对话框
    if (showDetailDialog) {
        TournamentDetailDialog(
            tournament = tournament,
            onDismiss = { showDetailDialog = false }
        )
    }
}

/**
 * 赛事详情对话框
 */
@Composable
fun TournamentDetailDialog(
    tournament: EsportsTournament,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${tournament.type.icon} ${tournament.type.displayName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = tournament.gameName,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 筹备期显示战队和赞助商
                if (tournament.status == TournamentStatus.PREPARING) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📋 筹备进度",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "第 ${tournament.currentDay} 天 / 共 ${tournament.preparationDays} 天",
                                    fontSize = 14.sp
                                )
                                LinearProgressIndicator(
                                    progress = (tournament.currentDay.toFloat() / tournament.preparationDays.toFloat()).coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                    
                    // 参赛战队
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚔️ 参赛战队 (${tournament.participatingTeams.size}支)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                tournament.participatingTeams.forEachIndexed { index, team ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Text(
                                            text = team,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 赞助商
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💰 赞助商 (${tournament.sponsors.size}家)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                tournament.sponsors.forEachIndexed { index, sponsor ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🏢",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = sponsor,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 正式比赛期显示比赛信息
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🏆 比赛进行中",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "第 ${tournament.currentDay} 天 / 共 ${tournament.type.duration} 天",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "当前阶段: ${tournament.getCurrentStage().displayName}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                    
                    // 参赛战队（比赛期也显示）
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚔️ 参赛战队",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                tournament.participatingTeams.take(8).forEachIndexed { index, team ->
                                    Text(
                                        text = "• $team",
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                                if (tournament.participatingTeams.size > 8) {
                                    Text(
                                        text = "... 等共${tournament.participatingTeams.size}支战队",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 投入信息
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💵 投入成本",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatMoney(tournament.investment),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E63)
                            )
                        }
                    }
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

/**
 * 赛事类型卡片 - 显示赛事详情（现代化设计）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentTypeCard(
    tournamentType: TournamentType,
    eligibleGames: List<Game>,
    totalGames: Int,
    money: Long,
    onClick: () -> Unit
) {
    val isEligible = eligibleGames.isNotEmpty()
    val canAfford = money >= tournamentType.baseCost
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isEligible) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .graphicsLayer {
                shadowElevation = if (isEligible) 12f else 4f
            }
    ) {
        // 左侧装饰条
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(140.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = if (isEligible) {
                            listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF2196F3)
                            )
                        } else {
                            listOf(
                                Color(0xFF666666),
                                Color(0xFF999999)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                )
        )
        
        // 主内容区域 - 使用渐变背景
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = if (isEligible) {
                            listOf(
                                Color(0xFF1E3A8A).copy(alpha = 0.85f),
                                Color(0xFF1E40AF).copy(alpha = 0.75f)
                            )
                        } else {
                            listOf(
                                Color(0xFF374151).copy(alpha = 0.6f),
                                Color(0xFF4B5563).copy(alpha = 0.5f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 图标带光晕效果
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF).copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tournamentType.icon,
                            fontSize = 32.sp
                        )
                    }
                    
                    Column {
                        Text(
                            text = tournamentType.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${tournamentType.duration}天赛事",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // 状态标签
                Box(
                    modifier = Modifier
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = if (isEligible) {
                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                } else {
                                    listOf(Color(0xFF6B7280), Color(0xFF4B5563))
                                }
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (isEligible) "✓ 可举办 (${eligibleGames.size})" else "✗ 暂不可用",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 赛事信息网格 - 使用卡片式布局（3列）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 成本信息
                InfoCardItem(
                    label = "成本",
                    value = formatMoney(tournamentType.baseCost),
                    icon = "💰",
                    color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                
                // 奖金池
                InfoCardItem(
                    label = "奖金",
                    value = formatMoney(tournamentType.prizePool),
                    icon = "🏆",
                    color = Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )
                
                // 所需活跃
                InfoCardItem(
                    label = "活跃",
                    value = formatPlayerCount(tournamentType.minActivePlayers),
                    icon = "👥",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 预期收益 - 使用渐变背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E40AF).copy(alpha = 0.4f),
                                Color(0xFF7C3AED).copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📈 预期收益",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "粉丝 +${(tournamentType.fansGrowthMin * 100).toInt()}-${(tournamentType.fansGrowthMax * 100).toInt()}%  •  活跃 +${(tournamentType.playersGrowthMin * 100).toInt()}-${(tournamentType.playersGrowthMax * 100).toInt()}%  •  兴趣 +${tournamentType.interestBonus.toInt()}%",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
            }
            
            // 参赛条件提示
            if (!isEligible && totalGames > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B).copy(alpha = 0.25f),
                                    Color(0xFFEF4444).copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = "⚠️ 需要：竞技类网游 • 评分≥8.0 • 活跃≥${formatPlayerCount(tournamentType.minActivePlayers)}",
                        fontSize = 12.sp,
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 信息卡片项 - 现代化小卡片
 */
@Composable
fun InfoCardItem(
    label: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * 信息项
 */
@Composable
fun RowScope.InfoItem(
    label: String,
    value: String,
    icon: String,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 游戏选择对话框（现代化设计）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentGameSelectionDialog(
    tournamentType: TournamentType,
    games: List<Game>,
    revenueDataMap: Map<String, GameRevenue>,
    currentDate: GameDate,
    money: Long,
    onDismiss: () -> Unit,
    onSelectGame: (Game) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // 图标光晕效果
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tournamentType.icon, fontSize = 32.sp)
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "举办${tournamentType.displayName}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "选择参赛游戏",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(games) { game ->
                    val revenueData = revenueDataMap[game.id]
                    val activePlayers = revenueData?.getActivePlayers() ?: 0
                    
                    GameSelectionCard(
                        game = game,
                        activePlayers = activePlayers,
                        onClick = { onSelectGame(game) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF94A3B8)
                )
            ) {
                Text("取消", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    )
}

/**
 * 游戏选择卡片 - 现代化设计
 */
@Composable
fun GameSelectionCard(
    game: Game,
    activePlayers: Long,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // 左侧装饰条
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(100.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3B82F6),
                            Color(0xFF8B5CF6)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                )
        )
        
        // 主内容区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF334155).copy(alpha = 0.8f),
                            Color(0xFF475569).copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                )
                .padding(14.dp)
        ) {
            // 游戏名称和箭头
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = game.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    // 评分
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 14.sp
                        )
                        Text(
                            text = game.rating?.let { String.format("%.1f", it) } ?: "未评分",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
                
                // 右箭头图标
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // 活跃玩家和主题标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 活跃玩家
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1E40AF).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(text = "👥", fontSize = 14.sp)
                    Text(
                        text = formatPlayerCount(activePlayers),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA)
                    )
                }
                
                // 主题标签
                Box(
                    modifier = Modifier
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.3f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = game.theme.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA78BFA)
                    )
                }
            }
        }
    }
}


/**
 * 赛事确认对话框 - 显示详细信息并确认举办
 */
@Composable
fun TournamentConfirmDialog(
    game: Game,
    tournamentType: TournamentType,
    revenueData: GameRevenue?,
    currentDate: GameDate,
    money: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val activePlayers = revenueData?.getActivePlayers() ?: 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = tournamentType.icon,
                        fontSize = 36.sp
                    )
                    Column {
                        Text(
                            text = "确认举办赛事",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = tournamentType.displayName,
                            fontSize = 14.sp,
                            color = Color(0xFF60A5FA)
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                // 游戏信息
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF334155).copy(alpha = 0.8f),
                                        Color(0xFF475569).copy(alpha = 0.7f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🎮 参赛游戏",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = game.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "⭐ ${game.rating?.let { String.format("%.1f", it) } ?: "未评分"}",
                                    fontSize = 14.sp,
                                    color = Color(0xFFFBBF24)
                                )
                                Text(
                                    text = "👥 ${formatPlayerCount(activePlayers)}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF60A5FA)
                                )
                            }
                        }
                    }
                }
                
                // 赛事规则
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E40AF).copy(alpha = 0.4f),
                                        Color(0xFF7C3AED).copy(alpha = 0.3f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "📋 赛事规则",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            
                            TournamentInfoRow("⏱️ 赛事周期", "${tournamentType.duration}天")
                            TournamentInfoRow("💰 举办成本", formatMoney(tournamentType.baseCost))
                            TournamentInfoRow("🏆 奖金池", formatMoney(tournamentType.prizePool))
                            TournamentInfoRow("👥 最低活跃", formatPlayerCount(tournamentType.minActivePlayers))
                        }
                    }
                }
                
                // 预期收益
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF10B981).copy(alpha = 0.3f),
                                        Color(0xFF059669).copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "📈 预期收益",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            
                            TournamentInfoRow(
                                "粉丝增长", 
                                "+${(tournamentType.fansGrowthMin * 100).toInt()}-${(tournamentType.fansGrowthMax * 100).toInt()}%"
                            )
                            TournamentInfoRow(
                                "活跃增长", 
                                "+${(tournamentType.playersGrowthMin * 100).toInt()}-${(tournamentType.playersGrowthMax * 100).toInt()}%"
                            )
                            TournamentInfoRow(
                                "兴趣提升", 
                                "+${tournamentType.interestBonus.toInt()}%"
                            )
                        }
                    }
                }
                
                // 资金检查
                item {
                    val canAfford = money >= tournamentType.baseCost
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (canAfford) 
                                    Color(0xFF10B981).copy(alpha = 0.2f)
                                else 
                                    Color(0xFFEF4444).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (canAfford) "✓" else "✗",
                                fontSize = 20.sp,
                                color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Column {
                                Text(
                                    text = "当前资金: ${formatMoney(money)}",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (canAfford) "资金充足" else "资金不足",
                                    fontSize = 12.sp,
                                    color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = money >= tournamentType.baseCost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFF6B7280)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "确认举办",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF94A3B8)
                )
            ) {
                Text(
                    text = "取消",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

/**
 * 赛事信息行组件
 */
@Composable
fun TournamentInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
