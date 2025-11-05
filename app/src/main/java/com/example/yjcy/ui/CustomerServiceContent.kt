package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.*
import com.example.yjcy.service.CustomerServiceManager
import com.example.yjcy.utils.formatMoneyWithDecimals

/**
 * 客服中心页面内容（对话框版本）
 */
@Composable
fun CustomerServiceContent(
    complaints: List<Complaint>,
    employees: List<Employee>,
    fans: Long,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    autoProcessEnabled: Boolean,
    onAutoProcessToggle: (Boolean) -> Unit,
    onComplaintsUpdate: (List<Complaint>) -> Unit,
    isSupporterUnlocked: Boolean = false, // 是否解锁支持者功能
    onShowFeatureLockedDialog: () -> Unit = {}, // 显示功能解锁对话框的回调
    onShowAutoProcessInfoDialog: () -> Unit = {} // 显示自动处理提示对话框的回调
) {
    var selectedComplaint by remember { mutableStateOf<Complaint?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }
    
    // 获取统计信息
    // 修复：使用key确保每次complaints变化时都重新计算（实时更新）
    // 使用complaints.size和complaints的hashCode作为key，确保任何变化都能检测到
    val statistics = remember(complaints.size, complaints.hashCode(), currentYear, currentMonth) {
        val stats = CustomerServiceManager.getComplaintStatistics(complaints, currentYear, currentMonth)
        android.util.Log.d("CustomerServiceContent", "📊 重新计算统计: 本月完成=${stats.completedThisMonth}, 总客诉=${complaints.size}")
        stats
    }
    
    // 获取可用客服列表
    val customerServiceEmployees = remember(employees) {
        CustomerServiceManager.getAvailableCustomerService(employees)
    }
    
    // 按状态分类客诉
    // 修复：使用size和hashCode作为key，确保变化时重新计算
    val activeComplaints = remember(complaints.size, complaints.hashCode()) {
        complaints.filter { it.status != ComplaintStatus.COMPLETED }
            .sortedWith(
                compareByDescending<Complaint> { it.severity }
                    .thenBy { it.status }
                    .thenBy { it.createdYear }
                    .thenBy { it.createdMonth }
                    .thenBy { it.createdDay }
            )
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 统计信息（紧凑版）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                label = "待处理",
                value = "${statistics.totalPending}",
                icon = "⏳",
                color = Color(0xFFFFA726)
            )
            StatisticItem(
                label = "处理中",
                value = "${statistics.totalInProgress}",
                icon = "🔧",
                color = Color(0xFF42A5F5)
            )
            StatisticItem(
                label = "本月完成",
                value = "${statistics.completedThisMonth}",
                icon = "✅",
                color = Color(0xFF66BB6A)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 客服人员数量和提示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡 及时处理客诉可避免粉丝流失",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "客服: ${customerServiceEmployees.size}人",
                fontSize = 12.sp,
                color = if (customerServiceEmployees.isEmpty()) Color.Red else Color(0xFF66BB6A),
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 自动处理开关
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (autoProcessEnabled) Color(0xFF10B981).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(8.dp)
                )
                .clickable { 
                    if (!isSupporterUnlocked) {
                        onShowFeatureLockedDialog()
                    } else {
                        // 直接切换开关状态
                        onAutoProcessToggle(!autoProcessEnabled)
                    }
                }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (autoProcessEnabled) "🤖" else "⏸️",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "自动处理",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (autoProcessEnabled) Color(0xFF10B981) else Color.White
                    )
                    Text(
                        text = if (autoProcessEnabled) "已开启：自动分配和处理客诉" else "已关闭：需手动操作",
                        fontSize = 11.sp,
                        color = if (autoProcessEnabled) Color(0xFF10B981).copy(alpha = 0.8f) else Color.Gray
                    )
                }
                if (!isSupporterUnlocked) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔒",
                        fontSize = 12.sp
                    )
                }
            }
            Switch(
                checked = autoProcessEnabled,
                onCheckedChange = { enabled ->
                    // 直接切换开关状态
                    if (!isSupporterUnlocked) {
                        onShowFeatureLockedDialog()
                    } else {
                        onAutoProcessToggle(enabled)
                    }
                },
                enabled = isSupporterUnlocked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF10B981),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 客诉列表（固定高度）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            if (activeComplaints.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "暂无客诉，做得不错！",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeComplaints) { complaint ->
                        CompactComplaintCard(
                            complaint = complaint,
                            employees = employees,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            onAssign = {
                                selectedComplaint = complaint
                                showAssignDialog = true
                            },
                            onUnassign = {
                                val updatedComplaint = CustomerServiceManager.unassignEmployee(complaint)
                                onComplaintsUpdate(
                                    complaints.map { if (it.id == complaint.id) updatedComplaint else it }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 分配客服对话框
    if (showAssignDialog && selectedComplaint != null) {
        AssignEmployeeDialog(
            complaint = selectedComplaint!!,
            employees = customerServiceEmployees,
            onDismiss = {
                showAssignDialog = false
                selectedComplaint = null
            },
            onConfirm = { employee ->
                val updatedComplaint = CustomerServiceManager.assignEmployee(selectedComplaint!!, employee)
                onComplaintsUpdate(
                    complaints.map { if (it.id == selectedComplaint!!.id) updatedComplaint else it }
                )
                showAssignDialog = false
                selectedComplaint = null
            }
        )
    }
}

/**
 * 统计项组件（紧凑版）
 */
@Composable
fun StatisticItem(
    label: String,
    value: String,
    icon: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

/**
 * 紧凑版客诉卡片组件
 */
@Composable
fun CompactComplaintCard(
    complaint: Complaint,
    employees: List<Employee>,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    onAssign: () -> Unit,
    onUnassign: () -> Unit
) {
    // 使用remember缓存计算结果，避免每次重组都重新计算
    val assignedEmployee = remember(complaint.assignedEmployeeId, employees.size) {
        employees.find { it.id == complaint.assignedEmployeeId }
    }
    val isOverdue = remember(complaint, currentYear, currentMonth, currentDay) {
        complaint.isOverdue(currentYear, currentMonth, currentDay)
    }
    val existingDays = remember(complaint, currentYear, currentMonth, currentDay) {
        complaint.calculateExistingDays(currentYear, currentMonth, currentDay)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> Color(0x33F44336)
                complaint.status == ComplaintStatus.IN_PROGRESS -> Color(0xFF1F2937)
                else -> Color(0xFF111827)
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 第一行：游戏名 + 严重程度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = complaint.type.icon,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = complaint.gameName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = complaint.type.displayName,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Surface(
                    color = when (complaint.severity) {
                        ComplaintSeverity.LOW -> Color(0xFF66BB6A)
                        ComplaintSeverity.MEDIUM -> Color(0xFFFFA726)
                        ComplaintSeverity.HIGH -> Color(0xFFF44336)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = complaint.severity.displayName,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { complaint.currentProgress.toFloat() / complaint.workload },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                    color = when (complaint.severity) {
                        ComplaintSeverity.LOW -> Color(0xFF66BB6A)
                        ComplaintSeverity.MEDIUM -> Color(0xFFFFA726)
                        ComplaintSeverity.HIGH -> Color(0xFFF44336)
                    },
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${complaint.getProgressPercentage()}%",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 底部信息和按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (isOverdue) {
                        Text(
                            text = "⚠️ 已超时${existingDays - complaint.severity.overdueThreshold}天",
                            fontSize = 10.sp,
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "${existingDays}天/${complaint.severity.overdueThreshold}天",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    
                    if (assignedEmployee != null) {
                        Text(
                            text = "👤 ${assignedEmployee.name} (Lv.${assignedEmployee.skillService})",
                            fontSize = 10.sp,
                            color = Color(0xFF42A5F5)
                        )
                    }
                }
                
                if (assignedEmployee == null) {
                    Button(
                        onClick = onAssign,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF42A5F5)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("分配", fontSize = 11.sp)
                    }
                } else {
                    TextButton(
                        onClick = onUnassign,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("取消", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

/**
 * 客诉卡片组件（原版，保留以兼容其他地方可能的调用）
 */
@Composable
fun ComplaintCard(
    complaint: Complaint,
    employees: List<Employee>,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    onAssign: () -> Unit,
    onUnassign: () -> Unit
) {
    val assignedEmployee = employees.find { it.id == complaint.assignedEmployeeId }
    val isOverdue = complaint.isOverdue(currentYear, currentMonth, currentDay)
    val existingDays = complaint.calculateExistingDays(currentYear, currentMonth, currentDay)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> Color(0x33F44336) // 红色背景（超时）
                complaint.status == ComplaintStatus.IN_PROGRESS -> Color.White.copy(alpha = 0.1f)
                else -> Color.White.copy(alpha = 0.05f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 第一行：游戏名 + 类型 + 严重程度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = complaint.type.icon,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = complaint.gameName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = complaint.type.displayName,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // 严重程度标签
                Surface(
                    color = when (complaint.severity) {
                        ComplaintSeverity.LOW -> Color(0xFF66BB6A)
                        ComplaintSeverity.MEDIUM -> Color(0xFFFFA726)
                        ComplaintSeverity.HIGH -> Color(0xFFF44336)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${complaint.severity.displayName}严重",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "处理进度",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${complaint.getProgressPercentage()}% (${complaint.currentProgress}/${complaint.workload})",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { complaint.currentProgress.toFloat() / complaint.workload },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when (complaint.severity) {
                        ComplaintSeverity.LOW -> Color(0xFF66BB6A)
                        ComplaintSeverity.MEDIUM -> Color(0xFFFFA726)
                        ComplaintSeverity.HIGH -> Color(0xFFF44336)
                    },
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 第三行：状态信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // 存在天数和超时状态
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isOverdue) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "超时",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "已超时${existingDays - complaint.severity.overdueThreshold}天",
                                fontSize = 12.sp,
                                color = Color(0xFFF44336),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "已存在${existingDays}天 (${complaint.severity.overdueThreshold}天内需完成)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // 分配的客服
                    if (assignedEmployee != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "客服",
                                tint = Color(0xFF42A5F5),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${assignedEmployee.name} (Lv.${assignedEmployee.skillService})",
                                fontSize = 12.sp,
                                color = Color(0xFF42A5F5)
                            )
                        }
                    }
                }
                
                // 操作按钮
                if (assignedEmployee == null) {
                    Button(
                        onClick = onAssign,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF42A5F5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("分配客服", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onUnassign,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("取消分配", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * 分配客服对话框
 */
@Composable
fun AssignEmployeeDialog(
    complaint: Complaint,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onConfirm: (Employee) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "分配客服处理客诉",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // 客诉信息
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "${complaint.type.icon} ${complaint.gameName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${complaint.type.displayName} - ${complaint.severity.displayName}严重",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "需要处理${complaint.workload}工作量",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 客服列表
                if (employees.isEmpty()) {
                    Text(
                        text = "❌ 没有可用的客服人员\n请先招聘或培训具有服务技能的员工",
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "选择客服人员：",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(employees) { employee ->
                            EmployeeSelectionCard(
                                employee = employee,
                                complaint = complaint,
                                onSelect = { onConfirm(employee) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 员工选择卡片
 */
@Composable
fun EmployeeSelectionCard(
    employee: Employee,
    complaint: Complaint,
    onSelect: () -> Unit
) {
    // 计算预计完成天数
    val dailyProgress = when (employee.skillService) {
        1 -> 50
        2 -> 65
        3 -> 85
        4 -> 110
        5 -> 140
        else -> 50
    }
    val estimatedDays = (complaint.workload - complaint.currentProgress + dailyProgress - 1) / dailyProgress
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
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
                    text = employee.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "服务技能: Lv.${employee.skillService}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "处理速度: ${dailyProgress}/天",
                    fontSize = 12.sp,
                    color = Color(0xFF42A5F5)
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "预计${estimatedDays}天",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF66BB6A)
                )
                Text(
                    text = "完成",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
