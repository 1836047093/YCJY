package com.example.yjcy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 年度统计数据
 */
data class YearEndStatistics(
    val year: Int,
    val gamesReleased: Int, // 发售的游戏数量
    val totalRevenue: Long, // 总收入
    val netProfit: Long, // 净利润（总收入 - 总支出）
    val totalEmployees: Int // 员工总数
)

/**
 * 年终奖对话框
 */
@Composable
fun YearEndBonusDialog(
    statistics: YearEndStatistics,
    currentMoney: Long,
    employeeCount: Int,
    averageSalary: Int, // 员工平均薪资
    onDistributeBonus: (bonusAmount: Long) -> Unit,
    onSkip: () -> Unit
) {
    // 年终奖建议金额：净利润的10-20%，或每人1-2个月薪资
    val suggestedBonusPerEmployee = if (statistics.netProfit > 0) {
        // 有利润：每人1-2个月薪资（根据净利润调整）
        val profitRatio = (statistics.netProfit.toFloat() / (averageSalary.toLong() * employeeCount * 12)).coerceIn(0f, 2f)
        (averageSalary.toLong() * (1.0 + profitRatio * 0.5)).toLong().coerceAtLeast(averageSalary.toLong())
    } else {
        // 无利润：每人0.5个月薪资
        (averageSalary * 0.5).toLong()
    }
    
    val totalSuggestedBonus = suggestedBonusPerEmployee * employeeCount
    val canAfford = currentMoney >= totalSuggestedBonus
    
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = "年终奖",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${statistics.year}年度总结",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "感谢您一年来的努力！",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 年度统计数据卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF374151)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(
                            label = "发售游戏",
                            value = "${statistics.gamesReleased}款",
                            icon = Icons.Default.Games
                        )
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        
                        StatRow(
                            label = "总收入",
                            value = "¥${statistics.totalRevenue}",
                            icon = Icons.Default.AccountBalanceWallet,
                            valueColor = Color(0xFF10B981)
                        )
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        
                        StatRow(
                            label = "净利润",
                            value = "¥${statistics.netProfit}",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            valueColor = if (statistics.netProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
                
                // 年终奖建议
                Text(
                    text = "年终奖建议",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Text(
                    text = "建议为每位员工发放 ¥${suggestedBonusPerEmployee}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                
                Text(
                    text = "总计：¥$totalSuggestedBonus（${employeeCount}位员工）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                if (!canAfford) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "警告",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "资金不足，跳过年终奖会影响员工忠诚度！",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "跳过",
                            color = Color.White
                        )
                    }
                    
                    Button(
                        onClick = { onDistributeBonus(totalSuggestedBonus) },
                        enabled = canAfford,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = "发放年终奖",
                            color = Color.Black,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

