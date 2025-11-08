package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.Subsidiary
import com.example.yjcy.data.SubsidiaryStatus
import com.example.yjcy.utils.formatMoney

/**
 * 子公司菜单对话框（从底部弹出）
 */
@Composable
fun SubsidiaryMenuDialog(
    subsidiaries: List<Subsidiary>,
    onDismiss: () -> Unit,
    onSubsidiaryClick: (Subsidiary) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp) // 限制最大高度
                .background(
                    color = Color(0xFF1a1a2e),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(vertical = 16.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            // 标题
            Text(
                text = "🏭 子公司管理",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 子公司列表（可滚动）
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(subsidiaries) { subsidiary ->
                    SubsidiaryMenuItem(
                        subsidiary = subsidiary,
                        onClick = { onSubsidiaryClick(subsidiary) }
                    )
                }
            }
        }
    }
}

/**
 * 子公司菜单项
 */
@Composable
fun SubsidiaryMenuItem(
    subsidiary: Subsidiary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subsidiary.logo,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subsidiary.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                val profit = subsidiary.getMonthlyProfit()
                val profitColor = if (profit >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
                Text(
                    text = "月度利润: ${formatMoney(profit)} | ${subsidiary.games.size}款游戏",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            // 状态标签
            val (statusText, statusColor) = when (subsidiary.status) {
                SubsidiaryStatus.ACTIVE -> "运营中" to Color(0xFF4CAF50)
                SubsidiaryStatus.SUSPENDED -> "暂停" to Color(0xFFFFA726)
                SubsidiaryStatus.LIQUIDATED -> "已清算" to Color(0xFFE57373)
            }
            Text(
                text = statusText,
                fontSize = 11.sp,
                color = statusColor,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * 子公司详情对话框（简化版）
 */
@Composable
fun SubsidiaryDetailDialog(
    subsidiary: Subsidiary,
    onDismiss: () -> Unit,
    onProfitSharingChange: (Float) -> Unit = {},
    onAutoManagementToggle: (Boolean) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1a1a2e)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subsidiary.logo,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = subsidiary.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "收购于 ${subsidiary.acquisitionDate.year}年${subsidiary.acquisitionDate.month}月",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Text(text = "✖", color = Color.White, fontSize = 18.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 财务信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 财务概览",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        InfoRow("收购价格", formatMoney(subsidiary.acquisitionPrice))
                        InfoRow("当前市值", formatMoney(subsidiary.marketValue))
                        InfoRow("月度收入", formatMoney(subsidiary.monthlyRevenue))
                        InfoRow("月度支出", formatMoney(subsidiary.monthlyExpense))
                        
                        val profit = subsidiary.getMonthlyProfit()
                        val profitColor = if (profit >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "月度利润",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatMoney(profit),
                                color = profitColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        
                        InfoRow("利润分成 (${(subsidiary.profitSharingRate * 100).toInt()}%)", 
                               formatMoney(subsidiary.getProfitShare()))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 游戏信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎮 游戏概览",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        InfoRow("游戏总数", "${subsidiary.games.size}款")
                        InfoRow("网游数量", "${subsidiary.getOnlineGameCount()}款")
                        InfoRow("单机数量", "${subsidiary.getSinglePlayerGameCount()}款")
                        InfoRow("总活跃玩家", "${subsidiary.getTotalActivePlayers()}人")
                        InfoRow("总销量", "${subsidiary.getTotalSales()}份")
                        InfoRow("估算员工", "${subsidiary.estimatedEmployeeCount}人")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("关闭", color = Color.White)
                }
            }
        }
    }
}

/**
 * 信息行组件
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
