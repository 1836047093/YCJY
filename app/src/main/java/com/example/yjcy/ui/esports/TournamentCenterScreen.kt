package com.example.yjcy.ui.esports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.esports.*
import com.example.yjcy.managers.esports.PlayerManager
import com.example.yjcy.managers.esports.TournamentManager

/**
 * 赛事中心界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentCenterScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showRegisterDialog by remember { mutableStateOf<Tournament?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🏆 赛事中心",
                        fontWeight = FontWeight.Bold
                    ) 
                },
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
            // Tab栏
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A2E),
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("可用赛事") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("我的赛程") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("赛事历史") }
                )
            }
            
            // 内容区域
            when (selectedTab) {
                0 -> AvailableTournamentsTab(
                    onRegister = { tournament ->
                        showRegisterDialog = tournament
                    }
                )
                1 -> MyScheduleTab()
                2 -> TournamentHistoryTab()
            }
        }
    }
    
    // 报名对话框
    showRegisterDialog?.let { tournament ->
        RegisterDialog(
            tournament = tournament,
            onDismiss = { showRegisterDialog = null },
            onConfirm = {
                // 执行报名
                val myTeam = createMyTeam()
                val success = TournamentManager.registerTeam(
                    tournament.id,
                    myTeam,
                    tournament.tier.entryFee
                )
                showRegisterDialog = null
                
                // TODO: 显示结果提示
            }
        )
    }
}

/**
 * 可用赛事Tab
 */
@Composable
fun AvailableTournamentsTab(
    onRegister: (Tournament) -> Unit
) {
    val tournaments = TournamentManager.activeTournaments
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 显示三种赛事
        item {
            TournamentTierCard(
                tier = TournamentTier.CITY_CUP,
                tournaments = tournaments.filter { it.tier == TournamentTier.CITY_CUP },
                onRegister = onRegister
            )
        }
        
        item {
            TournamentTierCard(
                tier = TournamentTier.CHAMPIONSHIP,
                tournaments = tournaments.filter { it.tier == TournamentTier.CHAMPIONSHIP },
                onRegister = onRegister
            )
        }
        
        item {
            TournamentTierCard(
                tier = TournamentTier.WORLDS,
                tournaments = tournaments.filter { it.tier == TournamentTier.WORLDS },
                onRegister = onRegister
            )
        }
    }
}

/**
 * 赛事层级卡片
 */
@Composable
fun TournamentTierCard(
    tier: TournamentTier,
    tournaments: List<Tournament>,
    onRegister: (Tournament) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tier.emoji,
                    fontSize = 28.sp
                )
                Column {
                    Text(
                        text = tier.displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "奖金池: ¥${tier.minPrizePool / 10000}万",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            
            // 赛事信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("报名费", "¥${tier.entryFee / 10000}万")
                InfoItem("赛程", "${tier.duration}天")
                InfoItem("声望", "+${tier.prestigeReward}")
            }
            
            // 当前赛事或创建按钮
            if (tournaments.isNotEmpty()) {
                val tournament = tournaments.first()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (tournament.status) {
                                Tournament.TournamentStatus.REGISTRATION -> "📝 报名中"
                                Tournament.TournamentStatus.IN_PROGRESS -> "🎮 进行中"
                                else -> "✅ 已完成"
                            },
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "${tournament.registeredTeams.size}支队伍",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                    }
                    
                    if (tournament.status == Tournament.TournamentStatus.REGISTRATION) {
                        Button(
                            onClick = { onRegister(tournament) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("立即报名")
                        }
                    } else {
                        Button(
                            onClick = { /* TODO: 查看详情 */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            )
                        ) {
                            Text("查看详情")
                        }
                    }
                }
            } else {
                Text(
                    text = "暂无进行中的赛事",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
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
 * 我的赛程Tab
 */
@Composable
fun MyScheduleTab() {
    // TODO: 显示我的战队参加的比赛
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "暂无赛程",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                "报名参赛后将显示比赛赛程",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}

/**
 * 赛事历史Tab
 */
@Composable
fun TournamentHistoryTab() {
    val history = TournamentManager.history
    
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "暂无历史记录",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Text(
                    "完成比赛后将显示历史成绩",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { record ->
                HistoryCard(record = record)
            }
        }
    }
}

@Composable
fun HistoryCard(record: TournamentRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.tier.emoji,
                        fontSize = 20.sp
                    )
                    Text(
                        text = record.tier.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Text(
                    text = "${record.year}年 ${record.season?.displayName ?: ""}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = getPlacementText(record.placement),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = getPlacementColor(record.placement)
                )
                Text(
                    text = "¥${record.prizeMoney / 10000}万",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

fun getPlacementText(placement: Int): String {
    return when (placement) {
        1 -> "🏆 冠军"
        2 -> "🥈 亚军"
        3 -> "🥉 季军"
        4 -> "第4名"
        else -> "第${placement}名"
    }
}

fun getPlacementColor(placement: Int): Color {
    return when (placement) {
        1 -> Color(0xFFFFD700)  // 金色
        2 -> Color(0xFFC0C0C0)  // 银色
        3 -> Color(0xFFCD7F32)  // 铜色
        else -> Color.White
    }
}

/**
 * 报名对话框
 */
@Composable
fun RegisterDialog(
    tournament: Tournament,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val myTeam = createMyTeam()
    val canRegister = myTeam.players.size >= 5
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tournament.tier.emoji)
                Text("报名 ${tournament.tier.displayName}")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 赛事信息
                Text("赛事信息", fontWeight = FontWeight.Bold)
                InfoRow("报名费", "¥${tournament.tier.entryFee / 10000}万")
                InfoRow("奖金池", "¥${tournament.tier.minPrizePool / 10000}万")
                InfoRow("赛程", "${tournament.tier.duration}天")
                
                Divider()
                
                // 队伍状态
                Text("队伍状态", fontWeight = FontWeight.Bold)
                InfoRow("队伍人数", "${myTeam.players.size}/5")
                
                if (!canRegister) {
                    Text(
                        "❌ 队伍人数不足，至少需要5名选手",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = canRegister
            ) {
                Text("确认报名")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * 创建我的战队（临时）
 */
fun createMyTeam(): Team {
    return Team(
        id = "my_team",
        name = "我的战队",
        players = PlayerManager.myTeam,
        tournamentHistory = emptyList()
    )
}
