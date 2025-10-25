package com.example.yjcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.FilterCriteria
import com.example.yjcy.service.JobPostingService

/**
 * 岗位发布对话框
 * 允许玩家创建新的招聘岗位
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostingDialog(
    onDismiss: () -> Unit,
    onPostingCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jobPostingService = remember { JobPostingService.getInstance() }
    
    // 表单状态
    var selectedPosition by remember { mutableStateOf<String?>(null) }
    var minSkillLevel by remember { mutableIntStateOf(1) }
    var salary by remember { mutableIntStateOf(10000) }
    var showPositionDropdown by remember { mutableStateOf(false) }
    
    val positions = FilterCriteria.getAvailablePositions()
    
    // 根据技能等级计算最低薪资标准（硬性要求）
    val minSalaryRequired = minSkillLevel * 10000
    
    // 计算薪资与最低标准的比率
    val salaryRatio = salary.toFloat() / minSalaryRequired.toFloat()
    
    // 验证表单
    val isValid = selectedPosition != null && 
                  minSkillLevel in 1..5 && 
                  salary >= minSalaryRequired
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4A7BB7) // 蓝色背景
            )
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // 标题栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF3A6BA5), // 深蓝色
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "发布招聘岗位",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 职位选择
                Text(
                    text = "岗位类型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = showPositionDropdown,
                    onExpandedChange = { showPositionDropdown = !showPositionDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedPosition ?: "请选择岗位",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPositionDropdown) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showPositionDropdown,
                        onDismissRequest = { showPositionDropdown = false }
                    ) {
                        positions.forEach { position ->
                            DropdownMenuItem(
                                text = { Text(position) },
                                onClick = {
                                    selectedPosition = position
                                    showPositionDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 技能等级要求
                Text(
                    text = "最低技能等级: Lv.$minSkillLevel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 使用自定义按钮确保所有分辨率下都能并排显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..5).forEach { level ->
                        val isSelected = minSkillLevel == level
                        OutlinedButton(
                            onClick = { 
                                minSkillLevel = level
                                // 自动调整薪资到最低标准
                                val newMinSalary = level * 10000
                                if (salary < newMinSalary) {
                                    salary = newMinSalary
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) Color(0xFF3A6BA5) else Color.Transparent,
                                contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                brush = if (isSelected) {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF74C0FC), Color(0xFF4A7BB7))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f))
                                    )
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Lv.$level",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 薪资设置
                Text(
                    text = "薪资待遇",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 薪资建议提示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f) // 半透明白色
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    salaryRatio >= 1.25f -> "🎉"
                                    salaryRatio >= 1.15f -> "👍"
                                    salaryRatio >= 1.05f -> "💡"
                                    else -> "⚠️"
                                },
                                fontSize = 16.sp
                            )
                            Column {
                                Text(
                                    text = "Lv.$minSkillLevel 最低薪资：¥${String.format("%,d", minSalaryRequired)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "当前薪资：¥${String.format("%,d", salary)} (${String.format("%.0f%%", salaryRatio * 100)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        when {
                            salaryRatio >= 1.5f -> {
                                Text(
                                    text = "💎 远高于标准（+${String.format("%.0f%%", (salaryRatio - 1) * 100)}）- 大量应聘者！",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF51CF66) // 绿色
                                )
                            }
                            salaryRatio >= 1.25f -> {
                                Text(
                                    text = "✨ 高于标准25%+ - 较多应聘者",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF51CF66) // 绿色
                                )
                            }
                            salaryRatio >= 1.15f -> {
                                Text(
                                    text = "👌 高于标准15%+ - 一般数量应聘者",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF74C0FC) // 蓝色
                                )
                            }
                            salaryRatio >= 1.05f -> {
                                Text(
                                    text = "📝 略高于标准 - 少量应聘者",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            else -> {
                                Text(
                                    text = "⚠️ 刚达标准 - 极少应聘者，建议提高薪资！",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFD93D) // 黄色警告
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 薪资设置
                Text(
                    text = "薪资: ¥${String.format("%,d", salary)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                
                Slider(
                    value = salary.toFloat(),
                    onValueChange = { 
                        // 确保薪资不低于最低标准
                        salary = it.toInt().coerceAtLeast(minSalaryRequired)
                    },
                    valueRange = minSalaryRequired.toFloat()..60000f,
                    steps = ((60000 - minSalaryRequired) / 1000).coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "取消",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 发布按钮
                    Button(
                        onClick = {
                            if (selectedPosition != null) {
                                jobPostingService.createJobPosting(
                                    position = selectedPosition!!,
                                    minSkillLevel = minSkillLevel,
                                    minSalary = salary,
                                    maxSalary = salary
                                )
                                onPostingCreated()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "发布岗位",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

