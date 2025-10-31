package com.example.yjcy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.Employee

/**
 * 涨薪请求对话框
 */
@Composable
fun SalaryRequestDialog(
    employee: Employee,
    currentMoney: Long,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val requestedSalary = employee.requestedSalary ?: employee.salary
    val salaryIncrease = requestedSalary - employee.salary
    val canAfford = currentMoney >= salaryIncrease
    
    Dialog(onDismissRequest = onReject) {
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "涨薪请求",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "涨薪请求",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "${employee.name} 提出涨薪要求",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "当前薪资: ¥${employee.salary}/月",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "要求薪资: ¥$requestedSalary/月",
                    fontSize = 14.sp,
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "增加: +¥$salaryIncrease/月",
                    fontSize = 14.sp,
                    color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (!canAfford) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "警告",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "资金不足，拒绝涨薪会影响员工忠诚度！",
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
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "拒绝",
                            color = Color.White
                        )
                    }
                    
                    Button(
                        onClick = onAccept,
                        enabled = canAfford,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = "同意",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

