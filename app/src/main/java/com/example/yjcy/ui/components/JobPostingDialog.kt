package com.example.yjcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    var minSalary by remember { mutableIntStateOf(3000) }
    var maxSalary by remember { mutableIntStateOf(10000) }
    var showPositionDropdown by remember { mutableStateOf(false) }
    
    val positions = FilterCriteria.getAvailablePositions()
    
    // 验证表单
    val isValid = selectedPosition != null && 
                  minSkillLevel in 1..5 && 
                  minSalary > 0 && 
                  maxSalary > minSalary
    
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
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ),
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "发布招聘岗位",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 职位选择
                Text(
                    text = "岗位类型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
                            .menuAnchor(),
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { level ->
                        FilterChip(
                            selected = minSkillLevel == level,
                            onClick = { minSkillLevel = level },
                            label = { Text("Lv.$level") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 薪资范围
                Text(
                    text = "薪资范围",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 最低薪资
                Text(
                    text = "最低薪资: ¥$minSalary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Slider(
                    value = minSalary.toFloat(),
                    onValueChange = { 
                        minSalary = it.toInt()
                        // 确保最高薪资大于最低薪资
                        if (maxSalary <= minSalary) {
                            maxSalary = minSalary + 1000
                        }
                    },
                    valueRange = 3000f..50000f,
                    steps = 46,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 最高薪资
                Text(
                    text = "最高薪资: ¥$maxSalary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Slider(
                    value = maxSalary.toFloat(),
                    onValueChange = { 
                        maxSalary = it.toInt()
                        // 确保最低薪资小于最高薪资
                        if (minSalary >= maxSalary) {
                            minSalary = maxSalary - 1000
                        }
                    },
                    valueRange = 3000f..50000f,
                    steps = 46,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 岗位预览
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "岗位预览",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = selectedPosition ?: "未选择岗位",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "要求: 专属技能 Lv.$minSkillLevel 以上",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "薪资: ¥${minSalary} - ¥${maxSalary}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
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
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // 发布按钮
                    Button(
                        onClick = {
                            if (selectedPosition != null) {
                                jobPostingService.createJobPosting(
                                    position = selectedPosition!!,
                                    minSkillLevel = minSkillLevel,
                                    minSalary = minSalary,
                                    maxSalary = maxSalary
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

