package com.example.yjcy.ui.esports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.*
import com.example.yjcy.managers.esports.PlayerManager
import com.example.yjcy.managers.esports.TournamentManager
import kotlin.random.Random

/**
 * 赛事系统测试界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentTestScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var testLog by remember { mutableStateOf<List<String>>(emptyList()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧪 赛事系统测试") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E)
                )
            )
        },
        containerColor = Color(0xFF0F0F1E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A2E)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("快速测试") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("测试日志") }
                )
            }
            
            when (selectedTab) {
                0 -> QuickTestTab(
                    onLogUpdate = { log ->
                        testLog = testLog + log
                    }
                )
                1 -> TestLogTab(logs = testLog)
            }
        }
    }
}

/**
 * 快速测试Tab
 */
@Composable
fun QuickTestTab(
    onLogUpdate: (String) -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 测试说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🎯 赛事系统测试",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "测试创建赛事、队伍报名、比赛模拟、奖励发放",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
        }
        
        // 当前状态
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "📊 当前状态",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                val activeTournaments = TournamentManager.activeTournaments
                val history = TournamentManager.history
                
                StatusRow("进行中赛事", "${activeTournaments.size}")
                StatusRow("历史记录", "${history.size}")
                StatusRow("我的战队人数", "${PlayerManager.myTeam.size}")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 测试按钮
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    isRunning = true
                    testCityCup(onLogUpdate)
                    isRunning = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    "🏙️ 测试城市杯（完整流程）",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = {
                    isRunning = true
                    testCreateAndRegister(onLogUpdate)
                    isRunning = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    "📝 测试创建和报名",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = {
                    isRunning = true
                    testSimulateMatch(onLogUpdate)
                    isRunning = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Text(
                    "⚔️ 测试比赛模拟",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (isRunning) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}

/**
 * 测试日志Tab
 */
@Composable
fun TestLogTab(logs: List<String>) {
    if (logs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "暂无测试日志",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E)
                    )
                ) {
                    Text(
                        text = log,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 测试城市杯完整流程
 */
fun testCityCup(onLog: (String) -> Unit) {
    onLog("━━━━━ 开始测试城市杯 ━━━━━")
    
    // 1. 创建赛事
    onLog("📝 创建城市杯...")
    val tournament = TournamentManager.createTournament(
        TournamentTier.CITY_CUP,
        2024,
        TournamentSeason.SPRING
    )
    onLog("✅ 赛事创建成功: ${tournament.id}")
    
    // 2. 生成16支队伍并报名
    onLog("👥 生成16支队伍...")
    repeat(16) { i ->
        val team = generateTestTeam("测试战队${i + 1}")
        val success = TournamentManager.registerTeam(
            tournament.id,
            team,
            tournament.tier.entryFee
        )
        if (success) {
            onLog("✅ ${team.name} 报名成功")
        }
    }
    onLog("✅ 共${tournament.registeredTeams.size}支队伍报名")
    
    // 3. 开始赛事
    onLog("🚀 开始赛事...")
    val started = TournamentManager.startTournament(tournament.id)
    if (started) {
        onLog("✅ 赛事开始，共${tournament.schedule.size}场比赛")
    }
    
    // 4. 模拟前10场比赛
    onLog("⚔️ 模拟比赛...")
    repeat(10) { i ->
        val result = TournamentManager.progressTournament(tournament.id)
        if (result != null) {
            onLog("✅ 第${i + 1}场: ${result.winner.name} 战胜 ${result.loser.name}")
        }
    }
    
    onLog("━━━━━ 测试完成 ━━━━━")
}

/**
 * 测试创建和报名
 */
fun testCreateAndRegister(onLog: (String) -> Unit) {
    onLog("━━━━━ 测试创建和报名 ━━━━━")
    
    // 创建三种赛事
    val cityCup = TournamentManager.createTournament(
        TournamentTier.CITY_CUP,
        2024,
        TournamentSeason.SPRING
    )
    onLog("✅ 创建城市杯: ${cityCup.id}")
    
    val championship = TournamentManager.createTournament(
        TournamentTier.CHAMPIONSHIP,
        2024,
        TournamentSeason.SUMMER
    )
    onLog("✅ 创建锦标赛: ${championship.id}")
    
    val worlds = TournamentManager.createTournament(
        TournamentTier.WORLDS,
        2024,
        null
    )
    onLog("✅ 创建全球总决赛: ${worlds.id}")
    
    // 测试报名
    val team = generateTestTeam("我的战队")
    
    val cityCupSuccess = TournamentManager.registerTeam(
        cityCup.id,
        team,
        cityCup.tier.entryFee
    )
    onLog(if (cityCupSuccess) "✅ 城市杯报名成功" else "❌ 城市杯报名失败")
    
    val champSuccess = TournamentManager.registerTeam(
        championship.id,
        team,
        championship.tier.entryFee
    )
    onLog(if (champSuccess) "❌ 锦标赛应该失败（需要城市杯资格）" else "✅ 正确：需要前置资格")
    
    onLog("━━━━━ 测试完成 ━━━━━")
}

/**
 * 测试比赛模拟
 */
fun testSimulateMatch(onLog: (String) -> Unit) {
    onLog("━━━━━ 测试比赛模拟 ━━━━━")
    
    // 创建两支队伍
    val team1 = generateTestTeam("蓝方战队")
    val team2 = generateTestTeam("红方战队")
    
    onLog("👥 创建队伍")
    onLog("  蓝方: ${team1.name} (${team1.players.size}人)")
    onLog("  红方: ${team2.name} (${team2.players.size}人)")
    
    // 创建比赛
    val match = Match(
        id = "test_match",
        tournamentId = "test",
        blueTeam = team1,
        redTeam = team2,
        bpSession = null,
        result = null,
        format = MatchFormat.BO3
    )
    
    onLog("⚔️ 开始BO3比赛...")
    
    // 模拟比赛
    val result = com.example.yjcy.managers.esports.MatchSimulator.simulateMatch(match)
    
    onLog("━━━ 比赛结果 ━━━")
    onLog("🏆 获胜方: ${result.winner.name}")
    onLog("📊 比分: ${result.gameResults.count { it.winner == TeamSide.BLUE }}:${result.gameResults.count { it.winner == TeamSide.RED }}")
    onLog("👑 MVP: ${result.mvp.name}")
    onLog("⏱️ 总时长: ${result.duration}分钟")
    
    result.highlights.forEach { highlight ->
        onLog("⭐ $highlight")
    }
    
    onLog("━━━━━ 测试完成 ━━━━━")
}

/**
 * 生成测试队伍
 */
fun generateTestTeam(name: String): Team {
    val players = mutableListOf<EsportsPlayer>()
    
    // 生成5个选手（每个位置1个）
    val positions = listOf(
        HeroPosition.TOP,
        HeroPosition.JUNGLE,
        HeroPosition.MID,
        HeroPosition.ADC,
        HeroPosition.SUPPORT
    )
    
    positions.forEach { position ->
        val player = PlayerManager.recruitPlayer()
        // 修改位置
        val modifiedPlayer = player.copy(position = position)
        players.add(modifiedPlayer)
    }
    
    return Team(
        id = "team_${Random.nextInt(10000)}",
        name = name,
        players = players,
        tournamentHistory = emptyList()
    )
}
