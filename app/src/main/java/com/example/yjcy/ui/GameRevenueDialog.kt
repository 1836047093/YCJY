package com.example.yjcy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.yjcy.utils.formatMoneyWithDecimals
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GameRevenueDialog(
    gameRevenue: GameRevenue,
    onDismiss: () -> Unit,
    onRemoveFromMarket: (String) -> Unit,
    onRelistGame: (String) -> Unit,
    onStartUpdate: (String) -> Unit = {}
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
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
                            text = "收入报告",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = gameRevenue.gameName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row {
                        // 游戏状态指示器
                        StatusIndicator(isActive = gameRevenue.isActive)
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                        DetailedStatisticsCard(statistics = statistics)
                    }
                    
                    // 游戏信息
                    item {
                        GameInfoCard(gameRevenue = gameRevenue)
                    }
                    
                    // 操作按钮
                    item {
                        ActionButtonsCard(
                            gameRevenue = gameRevenue,
                            onRemoveFromMarket = { showConfirmDialog = true },
                            onRelistGame = { onRelistGame(gameRevenue.gameId) },
                            onShowUpdateDialog = { showUpdateDialog = true }
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
            onDismiss = { showUpdateDialog = false },
            onConfirm = { features ->
                RevenueManager.createUpdateTask(gameRevenue.gameId, features)
                showUpdateDialog = false
                // 通知外层开始更新（例如关闭此弹窗，回到项目界面以分配员工）
                onStartUpdate(gameRevenue.gameId)
            }
        )
    }
}

@Composable
fun StatusIndicator(isActive: Boolean) {
    val color = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val text = if (isActive) "在售" else "已下架"
    
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
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val options = listOf("新人物", "新地图", "新坐骑", "新活动")
    val selected = remember { mutableStateListOf<String>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择更新内容", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toList()) }) {
                Text("开始更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        shape = RoundedCornerShape(16.dp)
    )
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
fun DetailedStatisticsCard(statistics: com.example.yjcy.data.RevenueStatistics) {
    // 移除 currencyFormat，使用自定义格式化函数
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
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
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailStatRow("总收入", "¥${formatMoneyWithDecimals(statistics.totalRevenue)}")
                DetailStatRow("总销量", "${formatMoneyWithDecimals(statistics.totalSales.toDouble())}份")
                DetailStatRow("单日最高销量", "${formatMoneyWithDecimals(statistics.peakDailySales.toDouble())}份")
                DetailStatRow("单日最高收入", "¥${formatMoneyWithDecimals(statistics.peakDailyRevenue)}")
            }
        }
    }
}

@Composable
fun DetailStatRow(
    label: String, 
    value: String, 
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
fun GameInfoCard(gameRevenue: GameRevenue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
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
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailStatRow("游戏名称", gameRevenue.gameName)
                DetailStatRow("发售价格", NumberFormat.getCurrencyInstance(Locale.getDefault()).format(gameRevenue.releasePrice))
                // 改为显示游戏内日期
                DetailStatRow(
                    "发售日期",
                    "第${gameRevenue.releaseYear}年${gameRevenue.releaseMonth}月${gameRevenue.releaseDay}日"
                )
                DetailStatRow("当前状态", if (gameRevenue.isActive) "在售中" else "已下架")
            }
        }
    }
}

@Composable
fun ActionButtonsCard(
    gameRevenue: GameRevenue,
    onRemoveFromMarket: () -> Unit,
    onRelistGame: () -> Unit,
    onShowUpdateDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
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
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (gameRevenue.isActive) {
                // 新增：游戏更新按钮（先展示成本与次数）
                val updateCost = remember(gameRevenue.updateCount) {
                    RevenueManager.calculateUpdateCost(gameRevenue.gameId)
                }
                Button(
                    onClick = onShowUpdateDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "游戏更新",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("游戏更新（+5%销量） - 费用 ¥${String.format("%.2f", updateCost)} / 已更新 ${gameRevenue.updateCount} 次", fontWeight = FontWeight.Medium)
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
                Button(
                    onClick = onRelistGame,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.RestoreFromTrash,
                        contentDescription = "重新上架",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重新上架", fontWeight = FontWeight.Medium)
                }
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
            Text("确定要下架游戏《$gameName》吗？下架后将停止销售，但可以重新上架。")
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