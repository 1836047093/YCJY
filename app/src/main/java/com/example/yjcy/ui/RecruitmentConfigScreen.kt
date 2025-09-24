package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import com.example.yjcy.data.*

/**
 * 招聘条件配置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitmentConfigScreen(
    onNavigateBack: () -> Unit = {},
    hrManager: HRManager = remember { HRManager() }
) {
    val recruitmentConfigs by hrManager.recruitmentConfigs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<RecruitmentConfig?>(null) }

    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 顶部栏
        ConfigScreenTopBar(
            onNavigateBack = onNavigateBack,
            onAddConfig = { showAddDialog = true }
        )
        
        // 配置列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recruitmentConfigs) { config ->
                RecruitmentConfigCard(
                    config = config,
                    onEdit = { editingConfig = config },
                    onToggleActive = {
                        hrManager.updateRecruitmentConfig(
                            config.id,
                            config.copy(isActive = !config.isActive)
                        )
                    },
                    onDelete = {
                        hrManager.removeRecruitmentConfig(config.id)
                    }
                )
            }
            
            if (recruitmentConfigs.isEmpty()) {
                item {
                    EmptyConfigCard(
                        onAddConfig = { showAddDialog = true }
                    )
                }
            }
        }
    }
    
    // 添加配置对话框
    if (showAddDialog) {
        ConfigEditDialog(
            config = null,
            onDismiss = { showAddDialog = false },
            onSave = { config ->
                hrManager.addRecruitmentConfig(config)
                showAddDialog = false
            }
        )
    }
    
    // 编辑配置对话框
    editingConfig?.let { config ->
        ConfigEditDialog(
            config = config,
            onDismiss = { editingConfig = null },
            onSave = { updatedConfig ->
                hrManager.updateRecruitmentConfig(config.id, updatedConfig)
                editingConfig = null
            }
        )
    }
    

}

/**
 * 配置界面顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreenTopBar(
    onNavigateBack: () -> Unit,
    onAddConfig: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "招聘配置",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = {
            IconButton(onClick = onAddConfig) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "创建招聘配置"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

/**
 * 招聘配置卡片
 */
@Composable
fun RecruitmentConfigCard(
    config: RecruitmentConfig,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (config.isActive) Color.White else Color(0xFFF0F0F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.positionType,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (config.isActive) Color(0xFF333333) else Color(0xFF666666)
                    )
                    
                    Text(
                        text = config.specialRequirements,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        maxLines = 2
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = config.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                    
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = Color(0xFF666666)
                        )
                    }
                    
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = Color(0xFFE57373)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 配置详情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ConfigDetailItem(
                    label = "目标人数",
                    value = config.targetCount.toString(),
                    color = Color(0xFF2196F3)
                )
                
                ConfigDetailItem(
                    label = "技能等级",
                    value = "≥${config.minSkillLevel}",
                    color = Color(0xFF4CAF50)
                )
                
                ConfigDetailItem(
                    label = "最高薪资",
                    value = "${config.maxSalary}万",
                    color = Color(0xFFFF9800)
                )
                
                ConfigDetailItem(
                    label = "优先级",
                    value = when (config.priority) {
                        RecruitmentPriority.LOW -> "低"
                        RecruitmentPriority.NORMAL -> "中"
                        RecruitmentPriority.HIGH -> "高"
                        RecruitmentPriority.URGENT -> "急"
                    },
                    color = when (config.priority) {
                        RecruitmentPriority.LOW -> Color(0xFF757575)
                        RecruitmentPriority.NORMAL -> Color(0xFF2196F3)
                        RecruitmentPriority.HIGH -> Color(0xFFFF9800)
                        RecruitmentPriority.URGENT -> Color(0xFFE53935)
                    }
                )
            }
            
            if (config.autoApprove) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoMode,
                        contentDescription = "自动批准",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "自动批准 (匹配度≥${(config.autoApproveThreshold * 100).toInt()}%)",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除配置 \"${config.positionType}\" 吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 配置详情项
 */
@Composable
fun ConfigDetailItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

/**
 * 空配置卡片
 */
@Composable
fun EmptyConfigCard(
    onAddConfig: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "无配置",
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "暂无招聘配置",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
            
            Text(
                text = "创建配置后，系统将自动为您寻找合适的候选人",
                fontSize = 14.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddConfig,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建招聘配置")
            }
        }
    }
}

/**
 * 简化的配置编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigEditDialog(
    config: RecruitmentConfig?,
    onDismiss: () -> Unit,
    onSave: (RecruitmentConfig) -> Unit
) {
    var positionType by remember { mutableStateOf(config?.positionType ?: "程序员") }
    var skillLevel by remember { mutableStateOf(config?.minSkillLevel?.toFloat() ?: 5f) }
    var monthlySalary by remember { mutableStateOf(config?.maxSalary?.toString() ?: "8") }
    var expanded by remember { mutableStateOf(false) }
    
    val positionOptions = listOf("程序员", "美术师", "策划师", "音效师")
    
    val isValid = positionType.isNotBlank() && 
                  monthlySalary.toIntOrNull() != null &&
                  monthlySalary.toIntOrNull()!! > 0
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (config == null) "创建招聘配置" else "编辑招聘配置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 职位类型下拉选择
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = positionType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("职位类型") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        positionOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    positionType = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 技能等级滑块
                Column {
                    Text(
                        text = "技能等级: ${skillLevel.toInt()}级",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = skillLevel,
                        onValueChange = { skillLevel = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF2196F3),
                            activeTrackColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1级",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "10级",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 月薪输入
                OutlinedTextField(
                    value = monthlySalary,
                    onValueChange = { monthlySalary = it },
                    label = { Text("月薪(万元)") },
                    placeholder = { Text("如：8") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "薪资",
                            tint = Color(0xFF666666)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            val salaryValue = monthlySalary.toIntOrNull() ?: 8
                            val newConfig = RecruitmentConfig(
                                id = config?.id ?: 0,
                                positionType = positionType,
                                minSkillLevel = skillLevel.toInt(),
                                maxSkillLevel = skillLevel.toInt(),
                                minSalary = (salaryValue * 0.8 * 1000).toInt(), // 最低薪资为设定值的80%
                                maxSalary = salaryValue * 1000, // 转换为元
                                minAge = 22,
                                maxAge = 45,
                                minExperience = 0,
                                maxExperience = 10,
                                specialRequirements = "",
                                targetCount = 1,
                                isActive = config?.isActive ?: true,
                                priority = RecruitmentPriority.NORMAL,
                                autoApprove = false,
                                autoApproveThreshold = 0.8f,
                                createdAt = config?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(newConfig)
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text(if (config == null) "创建" else "保存")
                    }
                }
            }
        }
    }
}