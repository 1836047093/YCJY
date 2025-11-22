package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.EsportsTournament
import com.example.yjcy.data.TournamentSuccessLevel
import com.example.yjcy.ui.components.TournamentIcon

/**
 * 赛事完成结果对话框
 */
@Composable
fun TournamentResultDialog(
    tournament: EsportsTournament,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "🏆 赛事圆满结束！",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 赛事名称
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TournamentIcon(
                        tournamentType = tournament.type.displayName,
                        size = 24f
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tournament.type.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = tournament.gameName,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 随机事件
                if (tournament.randomEvent.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tournament.randomEvent,
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 冠军信息
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "冠军：${tournament.champion}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 收益明细
                Text(
                    text = "💰 收益明细",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RevenueRow("投入成本", -tournament.investment, isNegative = true)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        RevenueRow("赞助商收入", tournament.sponsorRevenue)
                        RevenueRow("转播权收入", tournament.broadcastRevenue)
                        if (tournament.ticketRevenue > 0) {
                            RevenueRow("门票收入", tournament.ticketRevenue)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        val netProfit = tournament.getNetProfit()
                        RevenueRow(
                            "净利润",
                            netProfit,
                            isTotal = true,
                            isNegative = netProfit < 0
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 长期效果
                Text(
                    text = "📈 长期效果",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        EffectRow("粉丝增长", "+${tournament.fansGained}", "👥")
                        EffectRow("活跃玩家", "+${tournament.playersGained}", "🎮")
                        EffectRow("兴趣值恢复", "+${tournament.interestBonus.toInt()}", "❤️")
                        EffectRow("声誉提升", "+${tournament.type.reputationBonus}", "⭐")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 确定按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "太棒了！",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RevenueRow(
    label: String,
    amount: Long,
    isTotal: Boolean = false,
    isNegative: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isTotal) 15.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) Color.Black else Color(0xFF666666)
        )
        Text(
            text = formatMoney(amount),
            fontSize = if (isTotal) 15.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isNegative -> Color.Red
                isTotal && amount >= 0 -> Color(0xFF4CAF50)
                isTotal -> Color.Red
                else -> Color(0xFF4CAF50)
            }
        )
    }
}

@Composable
private fun EffectRow(
    label: String,
    value: String,
    icon: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3)
        )
    }
}

private fun formatMoney(amount: Long): String {
    val absAmount = kotlin.math.abs(amount)
    val formatted = when {
        absAmount >= 10000000L -> "¥${absAmount / 10000000}千万"
        absAmount >= 1000000L -> "¥${absAmount / 1000000}百万"
        absAmount >= 10000L -> "¥${absAmount / 10000}万"
        else -> "¥$absAmount"
    }
    return if (amount < 0) "-$formatted" else formatted
}
