package com.example.yjcy.ui.esports

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.esports.*
import com.example.yjcy.ui.components.SingleLineText

/**
 * 赛事详情界面
 * 包含：报名、参赛战队、赛程表、积分榜
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournament: Tournament,
    playerTeam: Team?,  // 玩家的战队
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onViewMatch: (ScheduledMatch) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📋 概况", "👥 战队", "📅 赛程", "📊 积分榜")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
    ) {
        // 顶部栏
        TopAppBar(
            title = {
                Column {
                    SingleLineText(
                        text = "${tournament.tier.emoji} ${tournament.tier.displayName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    SingleLineText(
                        text = tournament.getCurrentPhaseDescription(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0f3460)
            )
        )
        
        // 标签页
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF16213e),
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { SingleLineText(text = title, fontSize = 14.sp) }
                )
            }
        }
        
        // 内容区
        when (selectedTab) {
            0 -> TournamentOverviewTab(tournament, playerTeam, onRegister)
            1 -> RegisteredTeamsTab(tournament)
            2 -> TournamentScheduleTab(tournament, onViewMatch)
            3 -> StandingsTab(tournament)
        }
    }
}

/**
 * 概况Tab
 */
@Composable
fun TournamentOverviewTab(
    tournament: Tournament,
    playerTeam: Team?,
    onRegister: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 赛事信息卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SingleLineText(
                        text = "📋 赛事信息",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    TournamentInfoRow("赛事等级", tournament.tier.displayName)
                    TournamentInfoRow("报名费", "¥${formatMoney(tournament.tier.entryFee)}")
                    TournamentInfoRow("奖金池", "¥${formatMoney(tournament.prizePool)}")
                    TournamentInfoRow("赛程", "${tournament.tier.duration}天")
                    TournamentInfoRow("已报名", "${tournament.registeredTeams.size}/16队")
                    
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    
                    SingleLineText(
                        text = "🏆 赛制说明",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    SingleLineText(
                        text = "• 报名阶段：${tournament.tier.registrationDays}天",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    SingleLineText(
                        text = "• 小组赛：${tournament.tier.groupStageDays}天（4组单循环BO1）",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    SingleLineText(
                        text = "• 淘汰赛：${tournament.tier.playoffDays}天（单败淘汰BO3/BO5）",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
        
        // 报名状态卡片
        item {
            if (tournament.status == Tournament.TournamentStatus.REGISTRATION) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (tournament.isPlayerParticipating()) 
                            Color(0xFF4CAF50).copy(alpha = 0.2f) 
                        else Color(0xFF2196F3).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (tournament.isPlayerParticipating()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                                SingleLineText(
                                    text = "已报名参赛",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            SingleLineText(
                                text = "战队：${playerTeam?.name ?: "未知"}",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        } else {
                            SingleLineText(
                                text = "⏰ 报名倒计时",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            SingleLineText(
                                text = "还剩 ${tournament.tier.registrationDays - tournament.currentDay} 天",
                                fontSize = 14.sp,
                                color = Color.LightGray
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = onRegister,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                ),
                                enabled = playerTeam != null && playerTeam.players.size >= 5
                            ) {
                                SingleLineText(
                                    text = if (playerTeam == null) "请先组建战队" 
                                          else if (playerTeam.players.size < 5) "战队人数不足(需5人)"
                                          else "立即报名",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SingleLineText(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        SingleLineText(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 参赛战队Tab
 */
@Composable
fun RegisteredTeamsTab(tournament: Tournament) {
    if (tournament.registeredTeams.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SingleLineText(text = "👥", fontSize = 48.sp)
                SingleLineText(
                    text = "暂无战队报名",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tournament.registeredTeams) { team ->
            TeamCard(team, isPlayerTeam = team.id == tournament.playerTeamId)
        }
    }
}

@Composable
fun TeamCard(team: Team, isPlayerTeam: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayerTeam) Color(0xFF4CAF50).copy(alpha = 0.2f) 
                            else Color(0xFF16213e)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(
                    text = team.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isPlayerTeam) {
                    Text(
                        text = "我的战队",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            SingleLineText(
                text = "队员：${team.players.size}人",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            // 显示选手位置
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                team.players.take(5).forEach { player ->
                    Text(
                        text = player.positionDisplayName,
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color(0xFF2196F3).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 赛程表Tab
 */
@Composable
fun TournamentScheduleTab(
    tournament: Tournament,
    onViewMatch: (ScheduledMatch) -> Unit
) {
    if (tournament.schedule.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SingleLineText(text = "📅", fontSize = 48.sp)
                SingleLineText(
                    text = "赛程尚未生成",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                SingleLineText(
                    text = "等待报名结束后生成",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 按阶段分组显示
        val groupedMatches = tournament.schedule.groupBy { it.phase }
        
        groupedMatches.forEach { (phase, matches) ->
            item {
                SingleLineText(
                    text = "━━━ $phase ━━━",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(matches) { match ->
                MatchCard(match, tournament.playerTeamId, onViewMatch)
            }
        }
    }
}

@Composable
fun MatchCard(
    match: ScheduledMatch,
    playerTeamId: String?,
    onViewMatch: (ScheduledMatch) -> Unit
) {
    val isPlayerMatch = match.blueTeam.id == playerTeamId || match.redTeam.id == playerTeamId
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onViewMatch(match) },
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayerMatch) Color(0xFF2196F3).copy(alpha = 0.2f)
                            else Color(0xFF16213e)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 比赛状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (match.status) {
                        ScheduledMatch.MatchStatus.SCHEDULED -> "⏰ 未开始"
                        ScheduledMatch.MatchStatus.LIVE -> "🔴 进行中"
                        ScheduledMatch.MatchStatus.COMPLETED -> "✅ 已完成"
                    },
                    fontSize = 12.sp,
                    color = when (match.status) {
                        ScheduledMatch.MatchStatus.SCHEDULED -> Color.Gray
                        ScheduledMatch.MatchStatus.LIVE -> Color.Red
                        ScheduledMatch.MatchStatus.COMPLETED -> Color(0xFF4CAF50)
                    },
                    modifier = Modifier
                        .background(
                            when (match.status) {
                                ScheduledMatch.MatchStatus.SCHEDULED -> Color.Gray.copy(alpha = 0.2f)
                                ScheduledMatch.MatchStatus.LIVE -> Color.Red.copy(alpha = 0.2f)
                                ScheduledMatch.MatchStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                SingleLineText(
                    text = match.format.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            // 对阵双方
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 蓝方
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    SingleLineText(
                        text = match.blueTeam.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (match.blueTeam.id == playerTeamId) Color(0xFF4CAF50) else Color.White
                    )
                }
                
                // VS
                SingleLineText(
                    text = "VS",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // 红方
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    SingleLineText(
                        text = match.redTeam.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (match.redTeam.id == playerTeamId) Color(0xFF4CAF50) else Color.White
                    )
                }
            }
            
            // 比赛结果
            match.result?.let { result ->
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SingleLineText(
                        text = "获胜：${result.winner.name}",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                    SingleLineText(
                        text = "MVP：${result.mvp.name}",
                        fontSize = 14.sp,
                        color = Color(0xFFFFD700)
                    )
                }
            }
        }
    }
}

/**
 * 积分榜Tab
 */
@Composable
fun StandingsTab(tournament: Tournament) {
    if (tournament.groupStandings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SingleLineText(text = "📊", fontSize = 48.sp)
                SingleLineText(
                    text = "积分榜尚未生成",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        tournament.groupStandings.forEach { (groupName, standings) ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SingleLineText(
                            text = "$groupName 组",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                        
                        // 表头
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SingleLineText("排名", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.5f))
                            SingleLineText("战队", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(2f))
                            SingleLineText("胜", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.5f))
                            SingleLineText("负", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.5f))
                            SingleLineText("积分", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.7f))
                        }
                        
                        // 积分榜
                        standings.sortedByDescending { it.points }.forEachIndexed { index, standing ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SingleLineText(
                                    text = "${index + 1}",
                                    fontSize = 14.sp,
                                    color = if (index < 2) Color(0xFF4CAF50) else Color.White,
                                    fontWeight = if (index < 2) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(0.5f)
                                )
                                SingleLineText(
                                    text = standing.team.name,
                                    fontSize = 14.sp,
                                    color = if (standing.team.id == tournament.playerTeamId) Color(0xFF4CAF50) else Color.White,
                                    modifier = Modifier.weight(2f)
                                )
                                SingleLineText(
                                    text = "${standing.wins}",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(0.5f)
                                )
                                SingleLineText(
                                    text = "${standing.losses}",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(0.5f)
                                )
                                SingleLineText(
                                    text = "${standing.points}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF2196F3),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.7f)
                                )
                            }
                        }
                        
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                        SingleLineText(
                            text = "前2名晋级淘汰赛",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

fun formatMoney(amount: Long): String {
    return when {
        amount >= 100_000_000 -> "%.1f亿".format(amount / 100_000_000.0)
        amount >= 10_000 -> "%.1f万".format(amount / 10_000.0)
        else -> amount.toString()
    }
}
