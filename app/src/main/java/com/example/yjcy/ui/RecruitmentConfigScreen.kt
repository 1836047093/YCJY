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
    var showTemplateDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 顶部栏
        ConfigScreenTopBar(
            onNavigateBack = onNavigateBack,
            onAddConfig = { showAddDialog = true },
            onShowTemplates = { showTemplateDialog = true }
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
                        onAddConfig = { showAddDialog = true },
                        onShowTemplates = { showTemplateDialog = true }
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
    
    // 模板选择对话框
    if (showTemplateDialog) {
        TemplateSelectionDialog(
            onDismiss = { showTemplateDialog = false },
            onSelectTemplate = { template ->
                hrManager.addRecruitmentConfig(template)
                showTemplateDialog = false
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
    onAddConfig: () -> Unit,
    onShowTemplates: () -> Unit
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
            IconButton(onClick = onShowTemplates) {
                Icon(
                    imageVector = Icons.Default.LibraryBooks,
                    contentDescription = "模板"
                )
            }
            IconButton(onClick = onAddConfig) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加配置"
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
    onAddConfig: () -> Unit,
    onShowTemplates: () -> Unit
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
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onShowTemplates,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryBooks,
                        contentDescription = "模板",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("使用模板")
                }
                
                Button(
                    onClick = onAddConfig,
                    modifier = Modifier.weight(1f),
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
                    Text("自定义配置")
                }
            }
        }
    }
}

/**
 * 配置编辑对话框
 */
@Composable
fun ConfigEditDialog(
    config: RecruitmentConfig?,
    onDismiss: () -> Unit,
    onSave: (RecruitmentConfig) -> Unit
) {
    var positionType by remember { mutableStateOf(config?.positionType ?: "") }
    var minSkillLevel by remember { mutableStateOf(config?.minSkillLevel?.toString() ?: "1") }
    var maxSalary by remember { mutableStateOf(config?.maxSalary?.toString() ?: "10") }
    var minAge by remember { mutableStateOf(config?.minAge?.toString() ?: "22") }
    var maxExperience by remember { mutableStateOf(config?.maxExperience?.toString() ?: "10") }
    var targetCount by remember { mutableStateOf(config?.targetCount?.toString() ?: "1") }
    var priority by remember { mutableStateOf(config?.priority ?: RecruitmentPriority.NORMAL) }
    var autoApprove by remember { mutableStateOf(config?.autoApprove ?: false) }
    var autoApproveThreshold by remember { mutableStateOf((config?.autoApproveThreshold?.times(100))?.toInt()?.toString() ?: "80") }
    var specialRequirements by remember { mutableStateOf(config?.specialRequirements ?: "") }
    
    val isValid = positionType.isNotBlank() && 
                  minSkillLevel.toIntOrNull() != null &&
                  maxSalary.toIntOrNull() != null &&
                  targetCount.toIntOrNull() != null
    
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 职位类型
                OutlinedTextField(
                    value = positionType,
                    onValueChange = { positionType = it },
                    label = { Text("职位类型") },
                    placeholder = { Text("如：程序员、美术、策划") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 技能等级和薪资
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minSkillLevel,
                        onValueChange = { minSkillLevel = it },
                        label = { Text("最低技能等级") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = maxSalary,
                        onValueChange = { maxSalary = it },
                        label = { Text("最高薪资(万)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 年龄和经验
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minAge,
                        onValueChange = { minAge = it },
                        label = { Text("最低年龄") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = maxExperience,
                        onValueChange = { maxExperience = it },
                        label = { Text("最高经验(年)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 目标人数
                OutlinedTextField(
                    value = targetCount,
                    onValueChange = { targetCount = it },
                    label = { Text("目标招聘人数") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 优先级选择
                Text(
                    text = "优先级",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecruitmentPriority.values().forEach { priorityOption ->
                        FilterChip(
                            selected = priority == priorityOption,
                            onClick = { priority = priorityOption },
                            label = {
                                Text(
                                    text = when (priorityOption) {
                                        RecruitmentPriority.LOW -> "低"
                                        RecruitmentPriority.NORMAL -> "普通"
                                        RecruitmentPriority.HIGH -> "高"
                                        RecruitmentPriority.URGENT -> "紧急"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 自动批准设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "启用自动批准",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    
                    Switch(
                        checked = autoApprove,
                        onCheckedChange = { autoApprove = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                }
                
                if (autoApprove) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = autoApproveThreshold,
                        onValueChange = { autoApproveThreshold = it },
                        label = { Text("自动批准阈值(%)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 特殊要求
                OutlinedTextField(
                    value = specialRequirements,
                    onValueChange = { specialRequirements = it },
                    label = { Text("特殊技能要求") },
                    placeholder = { Text("如：Unity, Photoshop (用逗号分隔)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
                            val newConfig = RecruitmentConfig(
                                id = config?.id ?: 0,
                                positionType = positionType,
                                minSkillLevel = minSkillLevel.toIntOrNull() ?: 1,
                                minSalary = 3000,
                                maxSalary = maxSalary.toIntOrNull() ?: 10,
                                minAge = minAge.toIntOrNull() ?: 22,
                                maxExperience = maxExperience.toIntOrNull() ?: 10,
                                specialRequirements = specialRequirements,
                                targetCount = targetCount.toIntOrNull() ?: 1,
                                isActive = config?.isActive ?: true,
                                priority = priority,
                                autoApprove = autoApprove,
                                autoApproveThreshold = (autoApproveThreshold.toIntOrNull() ?: 80) / 100f,
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

/**
 * 模板选择对话框
 */
@Composable
fun TemplateSelectionDialog(
    onDismiss: () -> Unit,
    onSelectTemplate: (RecruitmentConfig) -> Unit
) {
    val templates = RecruitmentConfigTemplates.getAllTemplates()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "选择配置模板",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(templates) { template ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTemplate(template) },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = template.positionType,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                                
                                Text(
                                    text = template.specialRequirements,
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                }
            }
        }
    }
}