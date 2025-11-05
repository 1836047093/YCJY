package com.example.yjcy.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.GameRevenue
import com.example.yjcy.data.RevenueManager
import com.example.yjcy.data.SalesData
import com.example.yjcy.data.Game
import com.example.yjcy.data.MonetizationItem
import com.example.yjcy.data.ServerType
import com.example.yjcy.data.GameServerInfo
import com.example.yjcy.data.getUpdateContentName
import com.example.yjcy.utils.formatMoneyWithDecimals
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun GameRevenueDialog(
    gameRevenue: GameRevenue,
    game: Game,
    onDismiss: () -> Unit,
    onRemoveFromMarket: (String) -> Unit,
    onStartUpdate: (String) -> Unit = {},
    onMonetizationUpdate: (List<MonetizationItem>) -> Unit = {},
    onPurchaseServer: (ServerType) -> Unit = {},
    onAutoUpdateToggle: (Boolean) -> Unit = {},
    onPriceChange: (Double) -> Unit = {},
    businessModel: BusinessModel,
    money: Long = 0L,  // 新增：资金
    onMoneyUpdate: (Long) -> Unit = {},  // 新增：资金更新回调
    isSupporterUnlocked: Boolean = false, // 是否解锁支持者功能
    onShowFeatureLockedDialog: () -> Unit = {}, // 显示功能解锁对话框的回调
    onShowAutoUpdateInfoDialog: (Game) -> Unit = {} // 显示自动更新提示对话框的回调
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showPaymentSettingsDialog by remember { mutableStateOf(false) }
    var showServerManagementDialog by remember { mutableStateOf(false) }
    var showChangePriceDialog by remember { mutableStateOf(false) }
    val statistics = RevenueManager.calculateStatistics(gameRevenue)
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B) // 深灰蓝色护眼背景
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "收入报告",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = gameRevenue.gameName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row {
                        // 游戏状态指示器
                        StatusIndicator(isActive = gameRevenue.isActive, businessModel = businessModel)
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 移除核心数据卡片
                    
                    // 详细统计信息
                    item {
                        DetailedStatisticsCard(statistics = statistics, gameRevenue = gameRevenue, game = game)
                    }
                    
                    // 游戏信息
                    item {
                        GameInfoCard(gameRevenue = gameRevenue, businessModel = businessModel)
                    }
                    
                    // 操作按钮
                    item {
                        ActionButtonsCard(
                            gameRevenue = gameRevenue,
                            game = game,
                            onRemoveFromMarket = { showConfirmDialog = true },
                            onShowUpdateDialog = { showUpdateDialog = true },
                            onShowPaymentSettings = { showPaymentSettingsDialog = true },
                            onShowServerManagement = { showServerManagementDialog = true },
                            onShowChangePrice = { showChangePriceDialog = true },
                            onAutoUpdateToggle = onAutoUpdateToggle,
                            businessModel = businessModel,
                            isSupporterUnlocked = isSupporterUnlocked,
                            onShowFeatureLockedDialog = onShowFeatureLockedDialog,
                            onShowAutoUpdateInfoDialog = { onShowAutoUpdateInfoDialog(game) }
                        )
                    }
                }
            }
        }
    }
    
    // 确认对话框
    if (showConfirmDialog) {
        ConfirmRemovalDialog(
            gameName = gameRevenue.gameName,
            onConfirm = {
                onRemoveFromMarket(gameRevenue.gameId)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    if (showUpdateDialog) {
        UpdateFeatureDialog(
            game = game,
            money = money,
            onDismiss = { showUpdateDialog = false },
            onConfirm = { features, announcement ->
                // 计算更新费用
                val updateCost = RevenueManager.calculateUpdateCost(gameRevenue.gameId)
                
                // 检查资金是否足够
                if (money >= updateCost) {
                    // 扣除更新费用
                    onMoneyUpdate(money - updateCost.toLong())
                    // 创建更新任务（暂存更新内容和公告，等更新完成后再添加到updateHistory）
                    RevenueManager.createUpdateTask(gameRevenue.gameId, features, announcement)
                    showUpdateDialog = false
                    // 通知外层开始更新（例如关闭此弹窗，回到项目界面以分配员工）
                    onStartUpdate(gameRevenue.gameId)
                }
            }
        )
    }
    
    // 付费设置对话框
    if (showPaymentSettingsDialog) {
        PaymentSettingsDialog(
            game = game,
            onDismiss = { showPaymentSettingsDialog = false },
            onConfirm = { updatedItems ->
                onMonetizationUpdate(updatedItems)
                showPaymentSettingsDialog = false
            }
        )
    }
    
    // 服务器管理对话框
    if (showServerManagementDialog) {
        ServerManagementDialog(
            game = game,
            onDismiss = { showServerManagementDialog = false },
            onPurchaseServer = { serverType ->
                onPurchaseServer(serverType)
                showServerManagementDialog = false
            }
        )
    }
    
    // 更改价格对话框（仅单机游戏）
    if (showChangePriceDialog && businessModel == BusinessModel.SINGLE_PLAYER) {
        ChangePriceDialog(
            gameName = gameRevenue.gameName,
            currentPrice = gameRevenue.releasePrice,
            onDismiss = { showChangePriceDialog = false },
            onConfirm = { newPrice ->
                // 更新价格
                RevenueManager.updateGamePrice(gameRevenue.gameId, newPrice)
                onPriceChange(newPrice)
                showChangePriceDialog = false
                // 关闭对话框并刷新
                onDismiss()
            }
        )
    }
}

@Composable
fun StatusIndicator(isActive: Boolean, businessModel: BusinessModel = BusinessModel.SINGLE_PLAYER) {
    val color = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val text = if (isActive) {
        if (businessModel == BusinessModel.ONLINE_GAME) "运营中" else "在售"
    } else {
        "已下架"
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun UpdateFeatureDialog(
    game: Game,
    money: Long = 0L,
    onDismiss: () -> Unit,
    onConfirm: (List<String>, String) -> Unit
) {
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    // 根据游戏的付费内容或主题生成更新选项
    val options = remember(game) {
        if (game.businessModel == BusinessModel.ONLINE_GAME) {
            // 网络游戏：使用已启用的付费内容
            game.monetizationItems
                .filter { it.isEnabled }
                .map { it.type.getUpdateContentName() }
                .distinct()
        } else {
            // 单机游戏：根据游戏主题获取推荐的付费内容类型作为更新内容
            val recommendedItems = com.example.yjcy.data.MonetizationConfig.getRecommendedItems(game.theme)
            recommendedItems.map { it.getUpdateContentName() }
        }
    }
    
    val selected = remember { mutableStateListOf<String>() }
    val allSelected = selected.size == options.size && options.isNotEmpty()
    
    // 计算更新费用
    val updateCost = remember(game.id) {
        RevenueManager.calculateUpdateCost(game.id)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择更新内容", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 全选/反选按钮
                if (options.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (allSelected) "反选" else "全选",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selected.clear()
                                    selected.addAll(options)
                                } else {
                                    selected.clear()
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                
                // 更新内容选项
                if (options.isEmpty()) {
                    Text(
                        text = if (game.businessModel == BusinessModel.ONLINE_GAME) {
                            "暂无可用的更新内容\n请先在付费设置中启用付费内容"
                        } else {
                            "暂无可用的更新内容"
                        },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    options.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = item)
                            Checkbox(
                                checked = selected.contains(item),
                                onCheckedChange = { checked ->
                                    if (checked) selected.add(item) else selected.remove(item)
                                }
                            )
                        }
                    }
                }
                
                // 显示更新费用
                if (options.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "更新费用：",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatMoneyWithDecimals(updateCost),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canAfford = money >= updateCost
            TextButton(
                onClick = { showAnnouncementDialog = true },
                enabled = selected.isNotEmpty() && canAfford
            ) {
                Text(
                    text = if (!canAfford && selected.isNotEmpty()) {
                        "资金不足"
                    } else {
                        "开始更新"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        shape = RoundedCornerShape(16.dp)
    )
    
    // 更新公告输入对话框
    if (showAnnouncementDialog) {
        AnnouncementInputDialog(
            updateContent = selected.toList(),
            onDismiss = { showAnnouncementDialog = false },
            onConfirm = { announcement ->
                showAnnouncementDialog = false
                onDismiss() // 关闭更新内容选择对话框
                onConfirm(selected.toList(), announcement)
            }
        )
    }
}

@Composable
fun CoreStatisticsCard(statistics: com.example.yjcy.data.RevenueStatistics, gameRevenue: GameRevenue) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "核心数据",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    icon = Icons.Default.AttachMoney,
                    label = "总收益",
                    value = currencyFormat.format(statistics.totalRevenue),
                    color = Color(0xFF4CAF50)
                )
                
                StatisticItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "总销量",
                    value = "${statistics.totalSales}份",
                    color = Color(0xFF2196F3)
                )
                
                StatisticItem(
                    icon = Icons.Default.CalendarToday,
                    label = "在售天数",
                    value = "${statistics.daysOnMarket}天",
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SalesTrendChart(chartData: List<SalesData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = "趋势",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "最近7天销量趋势",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chartData.isNotEmpty()) {
                SimpleBarChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无销量数据",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<SalesData>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1
    val barColor = MaterialTheme.colorScheme.primary
    
    Column(modifier = modifier) {
        // 图表区域
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val barWidth = size.width / data.size * 0.7f
            val spacing = size.width / data.size * 0.3f
            
            data.forEachIndexed { index, salesData ->
                val barHeight = (salesData.value.toFloat() / maxValue) * size.height * 0.8f
                val x = index * (barWidth + spacing) + spacing / 2
                val y = size.height - barHeight
                
                // 绘制柱状图
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
                
                // 绘制数值标签 (简化版本，移除原生Canvas调用)
                // 注意：这里简化了文本绘制，实际项目中可以使用Text组件替代
            }
        }
        
        // 日期标签
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(data) { salesData ->
                Text(
                    text = salesData.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun DetailedStatisticsCard(statistics: com.example.yjcy.data.RevenueStatistics, gameRevenue: GameRevenue, game: Game) {
    // 移除 currencyFormat，使用自定义格式化函数
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3748) // 深灰色卡片背景
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "详细统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8) // 浅蓝灰色
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailStatRow("总收入", "¥${formatMoneyWithDecimals(statistics.totalRevenue)}")
                
                // 如果是网络游戏，显示付费内容
                if (game.businessModel == BusinessModel.ONLINE_GAME) {
                    // 获取所有已开启的付费内容
                    val enabledItems = game.monetizationItems.filter { it.isEnabled }
                    
                    if (enabledItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "付费内容",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 为每个已开启的付费内容显示收入
                        enabledItems.forEach { item ->
                            // 查找该付费内容的收入记录
                            val revenue = gameRevenue.monetizationRevenues.find { 
                                it.itemType == item.type.displayName 
                            }
                            
                            val revenueText = if (revenue != null) {
                                "¥${formatMoneyWithDecimals(revenue.totalRevenue)}"
                            } else {
                                "¥0.00"
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.type.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // 显示付费内容价格
                                    Text(
                                        text = if (item.price != null) "¥${formatMoneyWithDecimals(item.price.toDouble())}" else "未设置",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                Text(
                                    text = revenueText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
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
fun DetailStatRow(
    label: String, 
    value: String, 
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
fun GameInfoCard(gameRevenue: GameRevenue, businessModel: BusinessModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3748) // 深灰色卡片背景
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "游戏信息",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8) // 浅蓝灰色
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailStatRow("游戏名称", gameRevenue.gameName)
                // 网络游戏显示上线日期，单机游戏显示发售价格
                if (businessModel == BusinessModel.ONLINE_GAME) {
                    DetailStatRow(
                        "上线日期",
                        "第${gameRevenue.releaseYear}年${gameRevenue.releaseMonth}月${gameRevenue.releaseDay}日"
                    )
                } else {
                    DetailStatRow("发售价格", NumberFormat.getCurrencyInstance(Locale.getDefault()).format(gameRevenue.releasePrice))
                    // 改为显示游戏内日期
                    DetailStatRow(
                        "发售日期",
                        "第${gameRevenue.releaseYear}年${gameRevenue.releaseMonth}月${gameRevenue.releaseDay}日"
                    )
                }
                // 网络游戏显示"运营中"，单机游戏显示"在售中"
                DetailStatRow(
                    "当前状态",
                    if (gameRevenue.isActive) {
                        if (businessModel == BusinessModel.ONLINE_GAME) "运营中" else "在售中"
                    } else {
                        "已下架"
                    }
                )
            }
        }
    }
}

@Composable
fun ActionButtonsCard(
    gameRevenue: GameRevenue,
    game: Game,
    onRemoveFromMarket: () -> Unit,
    onShowUpdateDialog: () -> Unit,
    onShowPaymentSettings: () -> Unit = {},
    onShowServerManagement: () -> Unit = {},
    onShowChangePrice: () -> Unit = {},
    onAutoUpdateToggle: (Boolean) -> Unit = {},
    businessModel: BusinessModel,
    isSupporterUnlocked: Boolean = false,
    onShowFeatureLockedDialog: () -> Unit = {},
    onShowAutoUpdateInfoDialog: () -> Unit = {} // 显示自动更新提示对话框的回调
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3748) // 深灰色卡片背景
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "游戏管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444) // 红色
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (gameRevenue.isActive) {
                // 游戏更新按钮（集成自动更新开关）
                Button(
                    onClick = onShowUpdateDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：图标和"游戏更新"文字
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "游戏更新",
                                modifier = Modifier.size(20.dp)
                            )
                            Text("游戏更新", fontWeight = FontWeight.Medium)
                        }
                        
                        // 右侧：自动更新开关（带文字）
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (game.autoUpdate) 
                                Color(0xFF10B981).copy(alpha = 0.2f) 
                            else 
                                Color(0xFFEF4444).copy(alpha = 0.2f),
                            border = BorderStroke(
                                1.dp, 
                                if (game.autoUpdate) Color(0xFF10B981) else Color(0xFFEF4444)
                            ),
                            modifier = Modifier.clickable {
                                if (!isSupporterUnlocked) {
                                    onShowFeatureLockedDialog()
                                } else {
                                    onShowAutoUpdateInfoDialog()
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = "自动更新",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (game.autoUpdate) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                if (!isSupporterUnlocked) {
                                    Text(
                                        text = "🔒",
                                        fontSize = 10.sp
                                    )
                                }
                                Switch(
                                    checked = game.autoUpdate,
                                    onCheckedChange = { enabled ->
                                        if (!isSupporterUnlocked) {
                                            onShowFeatureLockedDialog()
                                        } else {
                                            onShowAutoUpdateInfoDialog()
                                        }
                                    },
                                    enabled = isSupporterUnlocked,
                                    modifier = Modifier.scale(0.8f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF10B981),
                                        checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f),
                                        uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                                        uncheckedTrackColor = Color(0xFFEF4444).copy(alpha = 0.4f),
                                        uncheckedBorderColor = Color(0xFFEF4444).copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
                
                // 新增：付费设置按钮（仅对网络游戏显示）
                if (businessModel == BusinessModel.ONLINE_GAME) {
                    Button(
                        onClick = onShowPaymentSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "付费设置",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("付费设置", fontWeight = FontWeight.Medium)
                    }
                }
                
                // 新增：更改价格按钮（仅对单机游戏显示）
                if (businessModel == BusinessModel.SINGLE_PLAYER) {
                    Button(
                        onClick = onShowChangePrice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "更改价格",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("更改价格", fontWeight = FontWeight.Medium)
                    }
                }
                
                Button(
                    onClick = onRemoveFromMarket,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.RemoveCircle,
                        contentDescription = "下架游戏",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("下架游戏", fontWeight = FontWeight.Medium)
                }
            } else {
                // 已下架的游戏，不显示任何操作按钮（无法重新上架）
                Text(
                    text = "游戏已下架",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ConfirmRemovalDialog(
    gameName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "确认下架游戏",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("确定要下架游戏《$gameName》吗？下架后将停止销售，且无法重新上架。")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确认下架", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ChangePriceDialog(
    gameName: String,
    currentPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var priceText by remember { mutableStateOf(currentPrice.toString()) }
    var priceError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "更改价格",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("游戏：《$gameName》")
                Text("当前价格：¥${formatMoneyWithDecimals(currentPrice)}")
                
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        priceText = newValue
                        priceError = false
                    },
                    label = { Text("新价格") },
                    placeholder = { Text("请输入价格（例如：50.0）") },
                    singleLine = true,
                    isError = priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("¥") }
                )
                
                if (priceError) {
                    Text(
                        text = "请输入有效的价格（大于0）",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newPrice = priceText.toDoubleOrNull()
                    if (newPrice != null && newPrice > 0) {
                        onConfirm(newPrice)
                    } else {
                        priceError = true
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("确认", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PaymentSettingsDialog(
    game: Game,
    onDismiss: () -> Unit,
    onConfirm: (List<MonetizationItem>) -> Unit
) {
    var editedItems by remember { mutableStateOf(game.monetizationItems) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💰 付费设置",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (editedItems.isEmpty()) {
                    // 未选择任何付费内容
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "😔",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "未选择任何付费内容",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "在创建游戏时，您可以选择付费内容类型",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // 付费内容列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(editedItems.size) { index ->
                            MonetizationItemEditCard(
                                item = editedItems[index],
                                onItemChange = { updatedItem ->
                                    editedItems = editedItems.toMutableList().apply {
                                        set(index, updatedItem)
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = { onConfirm(editedItems) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
fun MonetizationItemEditCard(
    item: MonetizationItem,
    onItemChange: (MonetizationItem) -> Unit
) {
    var priceInput by remember { mutableStateOf(item.price?.toString() ?: "") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.type.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { newInput ->
                        priceInput = newInput
                        val price = newInput.toFloatOrNull()
                        if (price != null && price >= 6f && price <= 648f) {
                            onItemChange(item.copy(price = price, isEnabled = true))
                        } else if (newInput.isEmpty()) {
                            onItemChange(item.copy(price = null, isEnabled = true))
                        }
                    },
                    label = { Text("价格 (元)") },
                    placeholder = { Text("输入价格或留空稍后设置") },
                    leadingIcon = {
                        Text(
                            text = "¥",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceInput.isNotEmpty() && (priceInput.toFloatOrNull() == null || priceInput.toFloatOrNull()!! < 6f || priceInput.toFloatOrNull()!! > 648f),
                    supportingText = {
                        if (priceInput.isNotEmpty()) {
                            val price = priceInput.toFloatOrNull()
                            when {
                                price == null -> Text(
                                    text = "请输入有效的价格",
                                    color = MaterialTheme.colorScheme.error
                                )
                                price < 6f -> Text(
                                    text = "最低价格为 ¥6",
                                    color = MaterialTheme.colorScheme.error
                                )
                                price > 648f -> Text(
                                    text = "最高价格为 ¥648",
                                    color = MaterialTheme.colorScheme.error
                                )
                                else -> Text(
                                    text = "价格有效",
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        } else {
                            Text(
                                text = "留空表示稍后设置 | 价格范围：¥6 - ¥648",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                
                // 快捷价格选择
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "快捷选择",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(6, 18, 30, 68).forEach { price ->
                            QuickPriceChip(
                                price = price,
                                onClick = {
                                    priceInput = price.toString()
                                    onItemChange(item.copy(price = price.toFloat(), isEnabled = true))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(128, 198, 328, 648).forEach { price ->
                            QuickPriceChip(
                                price = price,
                                onClick = {
                                    priceInput = price.toString()
                                    onItemChange(item.copy(price = price.toFloat(), isEnabled = true))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerManagementDialog(
    game: Game,
    onDismiss: () -> Unit,
    onPurchaseServer: (ServerType) -> Unit
) {
    // 显示公共池的服务器信息（所有游戏共享）
    val publicPoolId = "SERVER_PUBLIC_POOL"
    val serverInfo = remember { RevenueManager.getGameServerInfo(publicPoolId) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🖥️ 服务器管理",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 服务器概览卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "当前服务器状况",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ServerStatItem(
                                label = "总容量",
                                value = "${serverInfo.getTotalCapacity()}万人",
                                icon = "📊"
                            )
                            ServerStatItem(
                                label = "服务器数",
                                value = "${serverInfo.getActiveServerCount()}台",
                                icon = "🖥️"
                            )
                            ServerStatItem(
                                label = "总投入",
                                value = "¥${formatMoneyWithDecimals(serverInfo.getTotalCost().toDouble())}",
                                icon = "💰"
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 已有服务器列表
                if (serverInfo.servers.isNotEmpty()) {
                    Text(
                        text = "已购买的服务器",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(serverInfo.servers.size) { index ->
                            val server = serverInfo.servers[index]
                            ServerItemCard(server = server)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 购买服务器区域
                Text(
                    text = "购买新服务器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (serverInfo.servers.isEmpty()) 1f else 0.5f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ServerType.values().size) { index ->
                        val serverType = ServerType.values()[index]
                        PurchaseServerCard(
                            serverType = serverType,
                            onPurchase = { onPurchaseServer(serverType) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
fun ServerStatItem(label: String, value: String, icon: String) {
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ServerItemCard(server: com.example.yjcy.data.ServerInstance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (server.isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                    text = server.type.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "容量: ${server.type.capacity}万人",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "购买时间: 第${server.purchaseYear}年${server.purchaseMonth}月${server.purchaseDay}日",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (server.isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (server.isActive) "运行中" else "已停用",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PurchaseServerCard(
    serverType: ServerType,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
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
                    text = serverType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = serverType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "容量: ${serverType.capacity}万",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "费用: ¥${formatMoneyWithDecimals(serverType.cost.toDouble())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Button(
                onClick = onPurchase,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("购买", fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
 * 快捷价格选择按钮（用于付费设置对话框）
 */
@Composable
fun QuickPriceChip(
    price: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                text = "¥$price",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * 更新公告输入对话框
 * 玩家可以自定义更新公告，或使用默认公告
 */
@Composable
fun AnnouncementInputDialog(
    updateContent: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var announcement by remember { mutableStateOf("") }
    val defaultAnnouncement = remember(updateContent) {
        com.example.yjcy.utils.CommentGenerator.generateDefaultAnnouncement(updateContent)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "📢 编写更新公告",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 显示更新内容
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "本次更新内容：",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        updateContent.forEach { content ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = content,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                
                // 公告输入框
                OutlinedTextField(
                    value = announcement,
                    onValueChange = { announcement = it },
                    label = { Text("更新公告（选填）") },
                    placeholder = { Text("输入更新公告，或点击【使用默认】...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 180.dp),
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                
                // 默认公告预览
                if (announcement.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = "💡 默认公告预览：",
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = defaultAnnouncement,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            // 统一的确认按钮，根据输入状态显示不同文字
            TextButton(
                onClick = {
                    val finalAnnouncement = if (announcement.isNotEmpty()) {
                        announcement
                    } else {
                        defaultAnnouncement
                    }
                    onConfirm(finalAnnouncement)
                }
            ) {
                Text(if (announcement.isNotEmpty()) "确认发布" else "使用默认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}