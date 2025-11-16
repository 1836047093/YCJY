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
import com.example.yjcy.data.esports.EsportsPlayer
import com.example.yjcy.managers.esports.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 选手详情界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    player: EsportsPlayer,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showTrainingDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(player.name) },
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
            // 顶部信息卡片
            PlayerHeaderCard(player)
            
            // Tab栏
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A2E)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("属性") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("英雄池") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("生涯") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("合同") }
                )
            }
            
            // 内容
            when (selectedTab) {
                0 -> AttributesTab(player, onTraining = { showTrainingDialog = true })
                1 -> HeroPoolTab(player)
                2 -> CareerTab(player)
                3 -> ContractTab(player)
            }
        }
    }
    
    // 训练对话框
    if (showTrainingDialog) {
        TrainingDialog(
            player = player,
            onDismiss = { showTrainingDialog = false }
        )
    }
}

@Composable
fun PlayerHeaderCard(player: EsportsPlayer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 基本信息
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(player.rarity.emoji, fontSize = 24.sp)
                Column {
                    Text(
                        player.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = player.rarity.color
                    )
                    Text(
                        "${player.positionDisplayName} | ${player.age}岁",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            
            // 状态栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem("体力", player.stamina, Color(0xFF4CAF50))
                StatusItem("士气", player.morale, Color(0xFF2196F3))
                StatusItem("状态", player.form, Color(0xFFFF9800))
            }
            
            // 伤病提示
            player.injury?.let { injury ->
                Text(
                    "🏥 ${injury.severity.displayName} - 还需${injury.recoveryDays}天恢复",
                    fontSize = 12.sp,
                    color = Color.Red
                )
            }
            
            // 训练状态
            val trainingStatus = TrainingManager.getTrainingStatus(player.id)
            trainingStatus?.let { session ->
                Text(
                    "📚 ${session.type.displayName}中...",
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(
            "$value",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun AttributesTab(player: EsportsPlayer, onTraining: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                    Text(
                        "五维属性",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    AttributeBar("操作", player.attributes.mechanics)
                    AttributeBar("意识", player.attributes.awareness)
                    AttributeBar("团队", player.attributes.teamwork)
                    AttributeBar("心态", player.attributes.mentality)
                    AttributeBar("精通", player.attributes.heroMastery)
                    
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("综合评分", color = Color.Gray)
                        Text(
                            "${player.attributes.overallRating()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
        
        item {
            Button(
                onClick = onTraining,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                enabled = !TrainingManager.isTraining(player.id)
            ) {
                Text(
                    if (TrainingManager.isTraining(player.id)) "训练中..." else "开始训练",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun HeroPoolTab(player: EsportsPlayer) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "英雄池 (${player.heroPool.size}个英雄)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(player.heroPool.sortedByDescending { it.proficiency }) { mastery ->
            val hero = HeroManager.getHeroById(mastery.heroId)
            if (hero != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                hero.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${hero.type.displayName} | 难度${hero.difficulty}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${mastery.proficiency}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = getProficiencyColor(mastery.proficiency)
                            )
                            Text(
                                "${mastery.gamesPlayed}场",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getProficiencyColor(proficiency: Int): Color {
    return when {
        proficiency >= 85 -> Color(0xFFFF9800)  // 橙色
        proficiency >= 70 -> Color(0xFF9C27B0)  // 紫色
        proficiency >= 50 -> Color(0xFF2196F3)  // 蓝色
        else -> Color.Gray
    }
}

@Composable
fun CareerTab(player: EsportsPlayer) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                        "生涯数据",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    CareerStatRow("总场次", "${player.careerStats.totalMatches}")
                    CareerStatRow("胜场", "${player.careerStats.wins}")
                    CareerStatRow(
                        "胜率", 
                        "${(player.careerStats.winRate() * 100).toInt()}%"
                    )
                    CareerStatRow("MVP次数", "${player.careerStats.mvpCount}")
                    CareerStatRow("平均KDA", String.format("%.2f", player.careerStats.kda))
                }
            }
        }
    }
}

@Composable
fun CareerStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun ContractTab(player: EsportsPlayer) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                        "合同信息",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    ContractInfoRow(
                        "开始日期",
                        dateFormat.format(player.contract.startDate)
                    )
                    ContractInfoRow(
                        "结束日期",
                        dateFormat.format(player.contract.endDate)
                    )
                    ContractInfoRow(
                        "月薪",
                        "¥${player.contract.monthlySalary / 10000}万"
                    )
                    ContractInfoRow(
                        "违约金",
                        "¥${player.contract.buyoutClause / 10000}万"
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    
                    Text(
                        "奖金条款",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    ContractInfoRow(
                        "冠军奖金",
                        "¥${player.contract.bonusClause.championshipBonus / 10000}万"
                    )
                    ContractInfoRow(
                        "MVP奖金",
                        "¥${player.contract.bonusClause.mvpBonus / 10000}万"
                    )
                    ContractInfoRow(
                        "表现奖金",
                        "¥${player.contract.bonusClause.performanceBonus / 10000}万"
                    )
                }
            }
        }
        
        item {
            Button(
                onClick = { /* TODO: 续约 */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("续约合同", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ContractInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun TrainingDialog(
    player: EsportsPlayer,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择训练类型") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TrainingManager.TrainingType.values()) { type ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A3E)
                        ),
                        onClick = {
                            TrainingManager.startTraining(player, type, 1)
                            onDismiss()
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(type.emoji)
                                Text(
                                    type.displayName,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                type.description,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "费用: ¥${type.cost / 10000}万",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFF9800)
                                )
                                Text(
                                    "时长: ${type.duration}天",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
