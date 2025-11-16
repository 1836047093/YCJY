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
import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.EsportsPlayer
import com.example.yjcy.managers.esports.HeroManager
import com.example.yjcy.managers.esports.PlayerManager

/**
 * 战队管理主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showRecruitDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "⚽ 战队管理",
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
                    text = { Text("战队阵容") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("青训营") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("全部选手") }
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
 * 战队阵容Tab
 */
@Composable
fun TeamRosterTab() {
    val myTeam = PlayerManager.myTeam
    
    if (myTeam.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "暂无战队成员",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Text(
                    "前往青训营招募选手",
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
                        Text(
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
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🎓 青训营招募",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
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
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
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
            )
        ) {
            Text(
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
        Text(
            text = rarity,
            fontSize = 14.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
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
            containerColor = Color(0xFF1A1A2E)
        )
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
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            // 位置和年龄
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "位置: ${player.positionDisplayName}",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
                Text(
                    text = "年龄: ${player.age}岁",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
            
            // 属性
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AttributeBar("操作", player.attributes.mechanics)
                AttributeBar("意识", player.attributes.awareness)
                AttributeBar("团队", player.attributes.teamwork)
                AttributeBar("心态", player.attributes.mentality)
            }
            
            // 英雄池
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "英雄池:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${player.heroPool.size}个英雄",
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3)
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
 * 招募结果对话框
 */
@Composable
fun RecruitResultDialog(
    onDismiss: () -> Unit
) {
    // 执行招募
    val player = remember { PlayerManager.recruitPlayer() }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🎉 招募成功！")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${player.rarity.emoji} ${player.name}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = player.rarity.color
                )
                Text("品质: ${player.rarity.displayName}")
                Text("位置: ${player.positionDisplayName}")
                Text("年龄: ${player.age}岁")
                Text("综合评分: ${player.attributes.overallRating()}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
