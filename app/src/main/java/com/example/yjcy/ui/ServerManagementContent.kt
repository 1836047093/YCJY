package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.Game
import com.example.yjcy.data.GameReleaseStatus
import com.example.yjcy.data.RevenueManager
import com.example.yjcy.data.ServerType
import com.example.yjcy.formatMoneyWithDecimals

/**
 * 服务器管理页面内容
 */
@Composable
fun ServerManagementContent(
    games: List<Game>,
    money: Long,
    onPurchaseServer: (String, ServerType) -> Unit, // gameId, serverType
    onMoneyUpdate: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    
    // 筛选已上线的网络游戏
    val onlineGames = remember(games) {
        games.filter { 
            it.businessModel == BusinessModel.ONLINE_GAME && 
            (it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED)
        }
    }
    
    // 根据搜索关键词过滤
    val filteredGames = remember(onlineGames, searchQuery) {
        if (searchQuery.isEmpty()) {
            onlineGames
        } else {
            onlineGames.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F2937),
                        Color(0xFF111827)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题和总览
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF374151)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🖥️ 服务器管理中心",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ServerOverviewItem(
                            label = "网游数量",
                            value = "${onlineGames.size}",
                            icon = "🎮"
                        )
                        ServerOverviewItem(
                            label = "资金",
                            value = "¥${formatMoneyWithDecimals(money.toDouble())}",
                            icon = "💰"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索游戏...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 游戏列表
            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (onlineGames.isEmpty()) "暂无已上线的网络游戏" else "未找到相关游戏",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGames) { game ->
                        ServerGameCard(
                            game = game,
                            onClick = { selectedGame = game }
                        )
                    }
                }
            }
        }
    }
    
    // 服务器管理对话框
    if (selectedGame != null) {
        ServerManagementDialog(
            game = selectedGame!!,
            money = money,
            onDismiss = { selectedGame = null },
            onPurchaseServer = { serverType ->
                onPurchaseServer(selectedGame!!.id, serverType)
                onMoneyUpdate(money - serverType.cost)
                selectedGame = null
            }
        )
    }
}

@Composable
fun ServerOverviewItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ServerGameCard(
    game: Game,
    onClick: () -> Unit
) {
    val serverInfo = remember { RevenueManager.getGameServerInfo(game.id) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF374151)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ServerInfoChip(
                        icon = "🖥️",
                        text = "${serverInfo.getActiveServerCount()}台"
                    )
                    ServerInfoChip(
                        icon = "📊",
                        text = "${serverInfo.getTotalCapacity()}万人"
                    )
                    ServerInfoChip(
                        icon = "💰",
                        text = "¥${formatMoneyWithDecimals(serverInfo.getTotalCost().toDouble())}"
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "查看详情",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun ServerInfoChip(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 14.sp
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ServerManagementDialog(
    game: Game,
    money: Long,
    onDismiss: () -> Unit,
    onPurchaseServer: (ServerType) -> Unit
) {
    val serverInfo = remember { RevenueManager.getGameServerInfo(game.id) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Column {
                Text(
                    text = "🖥️ 服务器管理",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = game.name,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 服务器概览
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF374151)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "当前状况",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ServerMgmtStatItem(
                                    label = "服务器",
                                    value = "${serverInfo.getActiveServerCount()}台",
                                    icon = "🖥️"
                                )
                                ServerMgmtStatItem(
                                    label = "容量",
                                    value = "${serverInfo.getTotalCapacity()}万",
                                    icon = "📊"
                                )
                            }
                        }
                    }
                }
                
                // 已有服务器列表
                if (serverInfo.servers.isNotEmpty()) {
                    item {
                        Text(
                            text = "已购买的服务器",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    items(serverInfo.servers) { server ->
                        ServerMgmtItemCard(server = server)
                    }
                }
                
                // 购买服务器
                item {
                    Text(
                        text = "购买新服务器",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                items(ServerType.values().toList()) { serverType ->
                    PurchaseServerCard(
                        serverType = serverType,
                        money = money,
                        onPurchase = { onPurchaseServer(serverType) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = Color.White)
            }
        }
    )
}

@Composable
fun ServerMgmtStatItem(
    label: String,
    value: String,
    icon: String
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ServerMgmtItemCard(server: com.example.yjcy.data.ServerInstance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (server.isActive) 
                Color(0xFF10B981).copy(alpha = 0.2f)
            else 
                Color(0xFF6B7280).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
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
                    text = server.type.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "容量: ${server.type.capacity}万人",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (server.isActive) Color(0xFF10B981) else Color(0xFF6B7280)
            ) {
                Text(
                    text = if (server.isActive) "运行中" else "已停用",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PurchaseServerCard(
    serverType: ServerType,
    money: Long,
    onPurchase: () -> Unit
) {
    val canAfford = money >= serverType.cost
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF374151)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = serverType.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = serverType.description,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "费用: ¥${formatMoneyWithDecimals(serverType.cost.toDouble())}",
                    fontSize = 12.sp,
                    color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Button(
                onClick = onPurchase,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "购买",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}
