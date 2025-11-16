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
import com.example.yjcy.data.esports.PlayerRarity
import com.example.yjcy.managers.esports.HeroManager
import com.example.yjcy.managers.esports.PlayerManager

/**
 * MOBA电竞系统测试界面
 * 用于验证100个英雄和招募系统
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsportsTestScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var testResults by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧪 MOBA系统测试") },
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
                    text = { Text("英雄测试") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("招募测试") }
                )
            }
            
            when (selectedTab) {
                0 -> HeroTestTab()
                1 -> RecruitmentTestTab(
                    onTestComplete = { results ->
                        testResults = results
                    }
                )
            }
        }
    }
}

/**
 * 英雄测试Tab
 */
@Composable
fun HeroTestTab() {
    val heroes = HeroManager.getAllHeroes()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 统计卡片
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
                    "📊 英雄池统计",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("总英雄数", color = Color.LightGray)
                    Text(
                        "${heroes.size}",
                        color = if (heroes.size == 100) Color(0xFF4CAF50) else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                HeroPosition.values().forEach { position ->
                    val count = HeroManager.getHeroesByPosition(position).size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val posName = when(position) {
                            HeroPosition.TOP -> "上单"
                            HeroPosition.JUNGLE -> "打野"
                            HeroPosition.MID -> "中单"
                            HeroPosition.ADC -> "ADC"
                            HeroPosition.SUPPORT -> "辅助"
                        }
                        Text("$posName", color = Color.LightGray, fontSize = 14.sp)
                        Text(
                            "$count",
                            color = if (count == 20) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        
        // 英雄列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(heroes) { hero ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A3E)
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
                                "${hero.title} | ${hero.positionDisplayName}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            hero.type.displayName,
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 招募测试Tab
 */
@Composable
fun RecruitmentTestTab(
    onTestComplete: (Map<String, Any>) -> Unit
) {
    var testCount by remember { mutableStateOf(100) }
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<Map<PlayerRarity, Int>>(emptyMap()) }
    
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
                    "🎲 招募概率测试",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "多次招募测试，验证概率分布是否正确",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
        }
        
        // 测试次数选择
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
                Text("测试次数: $testCount", color = Color.White)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { testCount = 100 },
                        enabled = !isRunning
                    ) {
                        Text("100次")
                    }
                    Button(
                        onClick = { testCount = 500 },
                        enabled = !isRunning
                    ) {
                        Text("500次")
                    }
                    Button(
                        onClick = { testCount = 1000 },
                        enabled = !isRunning
                    ) {
                        Text("1000次")
                    }
                }
            }
        }
        
        // 测试结果
        if (results.isNotEmpty()) {
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
                        "📊 测试结果",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    PlayerRarity.values().forEach { rarity ->
                        val count = results[rarity] ?: 0
                        val percentage = (count.toDouble() / testCount * 100)
                        val expected = rarity.probability * 100
                        val diff = percentage - expected
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${rarity.emoji} ${rarity.displayName}",
                                    color = rarity.color,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$count 次 (${String.format("%.2f", percentage)}%)",
                                    color = Color.White
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "预期: ${String.format("%.1f", expected)}%",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "偏差: ${String.format("%+.2f", diff)}%",
                                    fontSize = 12.sp,
                                    color = if (kotlin.math.abs(diff) < 2.0) 
                                        Color(0xFF4CAF50) else Color.Yellow
                                )
                            }
                        }
                        
                        Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 开始测试按钮
        Button(
            onClick = {
                isRunning = true
                val testResults = mutableMapOf<PlayerRarity, Int>()
                
                repeat(testCount) {
                    val player = PlayerManager.recruitPlayer()
                    testResults[player.rarity] = testResults.getOrDefault(player.rarity, 0) + 1
                }
                
                results = testResults
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
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    "🚀 开始测试",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
