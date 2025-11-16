package com.example.yjcy.ui.esports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.yjcy.data.esports.PlayerRarity
import com.example.yjcy.managers.esports.TransferMarket

// 位置名称常量
private val ALL_POSITIONS = listOf("上单", "打野", "中单", "ADC", "辅助")

/**
 * 转会市场界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMarketScreen(
    onNavigateBack: () -> Unit,
    playerBalance: Long  // 玩家资金
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPosition by remember { mutableStateOf<String?>(null) }
    var selectedRarity by remember { mutableStateOf<PlayerRarity?>(null) }
    var showBuyDialog by remember { mutableStateOf<TransferMarket.Transfer?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💼 转会市场") },
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
            // 市场统计
            MarketStatsCard(playerBalance)
            
            // Tab栏
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A2E)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("全部") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("按位置") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("按品质") }
                )
            }
            
            // 筛选器
            if (selectedTab == 1) {
                PositionFilter(
                    selected = selectedPosition,
                    onSelect = { selectedPosition = it }
                )
            } else if (selectedTab == 2) {
                RarityFilter(
                    selected = selectedRarity,
                    onSelect = { selectedRarity = it }
                )
            }
            
            // 选手列表
            val listings = when (selectedTab) {
                0 -> TransferMarket.sortByPrice()
                1 -> selectedPosition?.let { TransferMarket.filterByPositionName(it) } 
                    ?: TransferMarket.listings
                2 -> selectedRarity?.let { TransferMarket.filterByRarity(it) } 
                    ?: TransferMarket.listings
                else -> emptyList()
            }
            
            TransferListings(
                listings = listings,
                onBuy = { showBuyDialog = it }
            )
        }
    }
    
    // 购买对话框
    showBuyDialog?.let { transfer ->
        BuyPlayerDialog(
            transfer = transfer,
            playerBalance = playerBalance,
            onDismiss = { showBuyDialog = null },
            onConfirm = { offer ->
                val (success, message) = TransferMarket.buyPlayer(transfer, offer)
                // TODO: 显示结果
                showBuyDialog = null
            }
        )
    }
}

@Composable
fun MarketStatsCard(playerBalance: Long) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val stats = TransferMarket.getMarketStats()
            
            StatColumn("市场选手", "${stats["total"]}")
            StatColumn("平均价格", "¥${(stats["avgPrice"] as Long) / 10000}万")
            StatColumn("我的资金", "¥${playerBalance / 10000}万")
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun PositionFilter(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("全部") }
            )
        }
        
        ALL_POSITIONS.forEach { position ->
            item {
                FilterChip(
                    selected = selected == position,
                    onClick = { onSelect(position) },
                    label = { Text(position) }
                )
            }
        }
    }
}

@Composable
fun RarityFilter(
    selected: PlayerRarity?,
    onSelect: (PlayerRarity?) -> Unit
) {
    val rarities = remember { PlayerRarity.values().toList() }
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("全部") }
            )
        }
        
        items(rarities) { rarity ->
            FilterChip(
                selected = selected == rarity,
                onClick = { onSelect(rarity) },
                label = { 
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(rarity.emoji)
                        Text(rarity.displayName)
                    }
                }
            )
        }
    }
}

@Composable
fun TransferListings(
    listings: List<TransferMarket.Transfer>,
    onBuy: (TransferMarket.Transfer) -> Unit
) {
    if (listings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "暂无选手",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    "转会市场目前没有可用选手",
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
            items(listings) { transfer ->
                TransferCard(
                    transfer = transfer,
                    onBuy = { onBuy(transfer) }
                )
            }
        }
    }
}

@Composable
fun TransferCard(
    transfer: TransferMarket.Transfer,
    onBuy: () -> Unit
) {
    val player = transfer.player
    
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
            // 选手基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(player.rarity.emoji, fontSize = 24.sp)
                    Column {
                        Text(
                            player.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = player.rarity.color
                        )
                        Text(
                            "${player.positionDisplayName} | ${player.age}岁",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Text(
                    "综合${player.attributes.overallRating()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            // 属性预览
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniAttribute("操作", player.attributes.mechanics)
                MiniAttribute("意识", player.attributes.awareness)
                MiniAttribute("团队", player.attributes.teamwork)
                MiniAttribute("心态", player.attributes.mentality)
            }
            
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            
            // 价格和按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("要价", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "¥${transfer.askingPrice / 10000}万",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
                
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("报价购买")
                }
            }
        }
    }
}

@Composable
fun MiniAttribute(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(
            "$value",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun BuyPlayerDialog(
    transfer: TransferMarket.Transfer,
    playerBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val player = transfer.player
    var offerAmount by remember { mutableStateOf(transfer.askingPrice) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(player.rarity.emoji)
                Text("购买 ${player.name}")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 选手信息
                Text("位置: ${player.positionDisplayName}")
                Text("年龄: ${player.age}岁")
                Text("综合: ${player.attributes.overallRating()}")
                
                Divider()
                
                // 价格信息
                Text("要价: ¥${transfer.askingPrice / 10000}万")
                Text("我的资金: ¥${playerBalance / 10000}万")
                
                // 报价输入
                Text("报价金额（万元）:")
                Slider(
                    value = offerAmount.toFloat(),
                    onValueChange = { offerAmount = it.toLong() },
                    valueRange = (transfer.askingPrice * 0.8).toFloat()..(transfer.askingPrice * 1.2).toFloat()
                )
                Text(
                    "¥${offerAmount / 10000}万",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                
                // 成功率提示
                val successRate = when {
                    offerAmount >= transfer.askingPrice -> "100%"
                    offerAmount >= transfer.askingPrice * 0.95 -> "90%"
                    offerAmount >= transfer.askingPrice * 0.9 -> "70%"
                    else -> "50%"
                }
                Text(
                    "预计成功率: $successRate",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(offerAmount) },
                enabled = offerAmount <= playerBalance
            ) {
                Text("确认报价")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
