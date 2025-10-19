package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.yjcy.utils.formatMoneyWithDecimals
import androidx.compose.material.icons.filled.FilterList
import com.example.yjcy.data.ServerInstance

/**
 * 服务器详情（带所属游戏信息）
 */
data class ServerDetail(
    val server: ServerInstance,
    val gameName: String,
    val gameId: String
)

/**
 * 服务器筛选类型
 */
enum class ServerFilter {
    ALL,           // 全部
    PUBLIC_POOL,   // 公共池
    ACTIVE,        // 运行中
    INACTIVE,      // 已停用
    BASIC,         // 星尘-D型
    INTERMEDIATE,  // 星尘-C型
    ADVANCED,      // 星尘-B型
    CLOUD          // 星尘-A型
}

/**
 * 服务器管理页面内容
 */
@Composable
fun ServerManagementContent(
    games: List<Game>,
    money: Long,
    onPurchaseServer: (ServerType) -> Unit, // 购买服务器到公共池
    onMoneyUpdate: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(ServerFilter.ALL) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showQuickPurchaseDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) } // 用于强制刷新UI
    
    // 定期刷新数据（每3秒）
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            refreshTrigger++
        }
    }
    
    // 收集所有已租用的服务器
    val allServers = remember(games, refreshTrigger) {
        val serverList = mutableListOf<ServerDetail>()
        
        // 1. 公共池服务器
        val publicPoolInfo = RevenueManager.getGameServerInfo("SERVER_PUBLIC_POOL")
        publicPoolInfo.servers.forEach { server ->
            serverList.add(
                ServerDetail(
                    server = server,
                    gameName = "公共池",
                    gameId = "SERVER_PUBLIC_POOL"
                )
            )
        }
        
        // 2. 各游戏的服务器
        games.forEach { game ->
            if (game.businessModel == BusinessModel.ONLINE_GAME) {
                val gameServerInfo = RevenueManager.getGameServerInfo(game.id)
                gameServerInfo.servers.forEach { server ->
                    serverList.add(
                        ServerDetail(
                            server = server,
                            gameName = game.name,
                            gameId = game.id
                        )
                    )
                }
            }
        }
        
        serverList
    }
    
    // 根据筛选条件过滤服务器
    val filteredServers = remember(allServers, selectedFilter) {
        when (selectedFilter) {
            ServerFilter.ALL -> allServers
            ServerFilter.PUBLIC_POOL -> allServers.filter { it.gameId == "SERVER_PUBLIC_POOL" }
            ServerFilter.ACTIVE -> allServers.filter { it.server.isActive }
            ServerFilter.INACTIVE -> allServers.filter { !it.server.isActive }
            ServerFilter.BASIC -> allServers.filter { it.server.type == ServerType.BASIC }
            ServerFilter.INTERMEDIATE -> allServers.filter { it.server.type == ServerType.INTERMEDIATE }
            ServerFilter.ADVANCED -> allServers.filter { it.server.type == ServerType.ADVANCED }
            ServerFilter.CLOUD -> allServers.filter { it.server.type == ServerType.CLOUD }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),  // 深蓝色
                        Color(0xFF4A148C)   // 深紫色
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
                    containerColor = Color.White.copy(alpha = 0.1f)
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
                    
                    // 计算总容量和总活跃服务器数（从公共池读取）
                    val publicPoolId = "SERVER_PUBLIC_POOL"
                    
                    val totalCapacity = remember(games, refreshTrigger) {
                        RevenueManager.getGameServerInfo(publicPoolId).getTotalCapacity()
                    }
                    
                    // 计算总活跃玩家数（所有在线网游）
                    val totalActivePlayers = remember(games, refreshTrigger) {
                        var total = 0
                        games.filter { 
                            it.businessModel == BusinessModel.ONLINE_GAME && 
                            (it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED)
                        }.forEach { game ->
                            val revenue = RevenueManager.getGameRevenue(game.id)
                            if (revenue != null && revenue.isActive) {
                                val statistics = RevenueManager.calculateStatistics(revenue)
                                val totalSales = statistics.totalSales
                                val activePlayers = (totalSales * 0.4).toInt()
                                total += activePlayers
                            }
                        }
                        total
                    }
                    
                    // 格式化容量显示（K/M格式）
                    val formattedCapacity = remember(totalCapacity) {
                        val capacityInPeople = totalCapacity * 10000 // 万人转为人数
                        when {
                            capacityInPeople >= 1_000_000 -> "${capacityInPeople / 1_000_000}M"
                            capacityInPeople >= 1_000 -> "${capacityInPeople / 1_000}K"
                            else -> "$capacityInPeople"
                        }
                    }
                    
                    // 格式化总活跃数（K/M格式）
                    val formattedActivePlayers = remember(totalActivePlayers) {
                        when {
                            totalActivePlayers >= 1_000_000 -> "${totalActivePlayers / 1_000_000}M"
                            totalActivePlayers >= 1_000 -> "${totalActivePlayers / 1_000}K"
                            else -> "$totalActivePlayers"
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ServerOverviewItem(
                            label = "总容量",
                            value = formattedCapacity,
                            icon = "🖥️"
                        )
                        ServerOverviewItem(
                            label = "总活跃数",
                            value = formattedActivePlayers,
                            icon = "📊"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 快速购买按钮
                    Button(
                        onClick = { showQuickPurchaseDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "租用服务器",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 筛选按钮和服务器数量
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已租用服务器 (${filteredServers.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Button(
                    onClick = { showFilterDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "筛选",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getFilterDisplayName(selectedFilter),
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 服务器列表
            if (filteredServers.isEmpty()) {
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
                            text = if (allServers.isEmpty()) "暂未租用任何服务器" else "未找到符合条件的服务器",
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
                    items(filteredServers) { serverDetail ->
                        RentedServerCard(
                            serverDetail = serverDetail
                        )
                    }
                }
            }
        }
    }
    
    // 筛选对话框
    if (showFilterDialog) {
        ServerFilterDialog(
            selectedFilter = selectedFilter,
            onDismiss = { showFilterDialog = false },
            onFilterSelected = { filter ->
                selectedFilter = filter
                showFilterDialog = false
            }
        )
    }
    
    // 快速购买对话框
    if (showQuickPurchaseDialog) {
        QuickPurchaseDialog(
            money = money,
            onDismiss = { showQuickPurchaseDialog = false },
            onPurchase = { serverType ->
                onPurchaseServer(serverType)
                refreshTrigger++ // 触发刷新
                showQuickPurchaseDialog = false
            }
        )
    }
}

/**
 * 获取筛选条件的显示名称
 */
fun getFilterDisplayName(filter: ServerFilter): String {
    return when (filter) {
        ServerFilter.ALL -> "全部"
        ServerFilter.PUBLIC_POOL -> "公共池"
        ServerFilter.ACTIVE -> "运行中"
        ServerFilter.INACTIVE -> "已停用"
        ServerFilter.BASIC -> "D型"
        ServerFilter.INTERMEDIATE -> "C型"
        ServerFilter.ADVANCED -> "B型"
        ServerFilter.CLOUD -> "A型"
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
    game: Game
) {
    val gameRevenue = remember { RevenueManager.getGameRevenue(game.id) }
    
    // 获取商业模式显示文本
    val businessModelText = when (game.businessModel) {
        BusinessModel.ONLINE_GAME -> "网游"
        BusinessModel.SINGLE_PLAYER -> "单机"
    }
    
    // 计算总利润
    val totalProfit = remember(gameRevenue) {
        if (gameRevenue != null) {
            val statistics = RevenueManager.calculateStatistics(gameRevenue)
            statistics.totalRevenue
        } else {
            0.0
        }
    }
    
    // 格式化总利润为K/M格式
    val formattedProfit = remember(totalProfit) {
        when {
            totalProfit >= 1_000_000 -> String.format("%.1fM", totalProfit / 1_000_000)
            totalProfit >= 1_000 -> String.format("%.1fK", totalProfit / 1_000)
            else -> String.format("%.0f", totalProfit)
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
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
                        icon = "🎮",
                        text = businessModelText
                    )
                    ServerInfoChip(
                        icon = "💰",
                        text = "¥$formattedProfit"
                    )
                }
            }
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
    // 显示公共池的服务器信息（所有游戏共享）
    val publicPoolId = "SERVER_PUBLIC_POOL"
    val serverInfo = remember { RevenueManager.getGameServerInfo(publicPoolId) }
    
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
                            containerColor = Color.White.copy(alpha = 0.1f)
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
                
                // 租用服务器
                item {
                    Text(
                        text = "租用新服务器",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                items(ServerType.entries) { serverType ->
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
            containerColor = Color.White.copy(alpha = 0.1f)
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

/**
 * 快速购买对话框 - 服务器公共池
 */
@Composable
fun QuickPurchaseDialog(
    money: Long,
    onDismiss: () -> Unit,
    onPurchase: (ServerType) -> Unit
) {
    var selectedServerType by remember { mutableStateOf(ServerType.BASIC) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Text(
                text = "🖥️ 租用服务器",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServerType.entries.forEach { serverType ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedServerType = serverType },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedServerType == serverType) 
                                Color(0xFF10B981).copy(alpha = 0.3f) 
                            else 
                                Color.White.copy(alpha = 0.1f)
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
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedServerType == serverType) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
                                )
                                Text(
                                    text = "容量: ${serverType.capacity}万人",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "月费：¥${formatMoneyWithDecimals(serverType.cost.toDouble())}",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onPurchase(selectedServerType)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Text("开通", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White)
            }
        }
    )
}

/**
 * 已租用服务器卡片
 */
@Composable
fun RentedServerCard(
    serverDetail: ServerDetail
) {
    val server = serverDetail.server
    val isPublicPool = serverDetail.gameId == "SERVER_PUBLIC_POOL"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (server.isActive) 
                Color.White.copy(alpha = 0.15f)
            else 
                Color.Gray.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 第一行：服务器类型和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🖥️",
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = server.type.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "容量: ${server.type.capacity}万人",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (server.isActive) Color(0xFF10B981) else Color(0xFF6B7280)
                ) {
                    Text(
                        text = if (server.isActive) "运行中" else "已停用",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 租用日期
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "📅",
                    fontSize = 14.sp
                )
                Text(
                    text = "租用日期: ${server.purchaseYear}年${server.purchaseMonth}月${server.purchaseDay}日",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 月费
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "💰",
                    fontSize = 14.sp
                )
                Text(
                    text = "月费: ¥${formatMoneyWithDecimals(server.type.cost.toDouble())}",
                    fontSize = 13.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 服务器筛选对话框
 */
@Composable
fun ServerFilterDialog(
    selectedFilter: ServerFilter,
    onDismiss: () -> Unit,
    onFilterSelected: (ServerFilter) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Text(
                text = "🔍 筛选服务器",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 服务器类型筛选选项
                listOf(
                    ServerFilter.ALL to "全部服务器",
                    ServerFilter.BASIC to "星尘-D型服务器",
                    ServerFilter.INTERMEDIATE to "星尘-C型服务器",
                    ServerFilter.ADVANCED to "星尘-B型服务器",
                    ServerFilter.CLOUD to "星尘-A型服务器"
                ).forEach { (filter, label) ->
                    FilterOptionCard(
                        label = label,
                        isSelected = selectedFilter == filter,
                        onClick = { onFilterSelected(filter) }
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

/**
 * 筛选选项卡片
 */
@Composable
fun FilterOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF10B981).copy(alpha = 0.3f) 
            else 
                Color.White.copy(alpha = 0.1f)
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
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color.White
            )
            
            if (isSelected) {
                Text(
                    text = "✓",
                    fontSize = 16.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
