package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.GameDate
import com.example.yjcy.data.Loan
import com.example.yjcy.data.LoanType
import com.example.yjcy.utils.formatMoney

@Composable
fun LoanDialog(
    currentMoney: Long,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    existingLoans: List<Loan>,
    onDismiss: () -> Unit,
    onApplyLoan: (Loan) -> Unit
) {
    var selectedLoanType by remember { mutableStateOf<LoanType?>(null) }
    var loanAmount by remember { mutableLongStateOf(1000000L) } // 默认100万
    var loanMonths by remember { mutableIntStateOf(12) } // 默认12个月
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .background(
                    color = Color(0xFF1F2937),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 标题
                Text(
                    text = "💰 银行贷款",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 当前资金和贷款总额
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "当前资金",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = formatMoney(currentMoney),
                            color = if (currentMoney >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "贷款总额",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        val totalLoanAmount = existingLoans.sumOf { it.amount }
                        Text(
                            text = formatMoney(totalLoanAmount),
                            color = if (totalLoanAmount > 0) Color(0xFFEF4444) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 贷款类型选择
                Text(
                    text = "选择贷款类型",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 贷款类型卡片
                LoanType.entries.forEach { loanType ->
                    val isSelected = selectedLoanType == loanType
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { 
                                selectedLoanType = loanType 
                                loanAmount = (loanType.maxAmount / 2).coerceAtLeast(1000000L)
                                loanMonths = (loanType.maxMonths + loanType.minMonths) / 2
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = loanType.typeName,
                                    color = if (isSelected) Color(0xFF60A5FA) else Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "年利率 ${(loanType.interestRate * 100).toInt()}%",
                                    color = if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = loanType.description,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "最大金额：${formatMoney(loanType.maxAmount)} | 期限：${loanType.minMonths}-${loanType.maxMonths}个月",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                // 贷款参数设置（仅在选中类型后显示）
                if (selectedLoanType != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 贷款金额滑块
                    Text(
                        text = "贷款金额：${formatMoney(loanAmount)}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = loanAmount.toFloat(),
                        onValueChange = { loanAmount = it.toLong() },
                        valueRange = 100000f..selectedLoanType!!.maxAmount.toFloat(),
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF3B82F6),
                            activeTrackColor = Color(0xFF3B82F6),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 还款期限滑块
                    Text(
                        text = "还款期限：${loanMonths}个月",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = loanMonths.toFloat(),
                        onValueChange = { loanMonths = it.toInt() },
                        valueRange = selectedLoanType!!.minMonths.toFloat()..selectedLoanType!!.maxMonths.toFloat(),
                        steps = selectedLoanType!!.maxMonths - selectedLoanType!!.minMonths - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF3B82F6),
                            activeTrackColor = Color(0xFF3B82F6),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 还款信息预览
                    val monthlyPayment = selectedLoanType!!.calculateMonthlyPayment(loanAmount, loanMonths)
                    val totalPayment = monthlyPayment * loanMonths
                    val totalInterest = totalPayment - loanAmount
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "📋 还款计划预览",
                                color = Color(0xFF60A5FA),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LoanDetailRow("月还款额", formatMoney(monthlyPayment))
                            LoanDetailRow("还款总额", formatMoney(totalPayment))
                            LoanDetailRow("总利息", formatMoney(totalInterest), Color(0xFFFBBF24))
                            LoanDetailRow("月利率", "${String.format("%.2f", selectedLoanType!!.getMonthlyInterestRate() * 100)}%")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 申请贷款按钮
                    Button(
                        onClick = {
                            if (selectedLoanType != null) {
                                val loan = Loan(
                                    amount = loanAmount,
                                    interestRate = selectedLoanType!!.interestRate,
                                    totalMonths = loanMonths,
                                    remainingMonths = loanMonths,
                                    monthlyPayment = selectedLoanType!!.calculateMonthlyPayment(loanAmount, loanMonths),
                                    startDate = GameDate(currentYear, currentMonth, currentDay),
                                    loanType = selectedLoanType!!
                                )
                                onApplyLoan(loan)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = selectedLoanType != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "申请贷款",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanDetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
