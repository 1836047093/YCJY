package com.example.yjcy.ui

import androidx.compose.foundation.clickable
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

/**
 * 增强版项目管理内容组件，集成员工分配功能
 */
@Composable
fun EnhancedProjectManagementContent(
    games: List<com.example.yjcy.ui.Game> = emptyList(),
    onGamesUpdate: (List<com.example.yjcy.ui.Game>) -> Unit = {},
    availableEmployees: List<Employee> = getDefaultEmployees()
) {
    var showGameDevelopmentDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "🎮 项目管理",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 开发新游戏按钮
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showGameDevelopmentDialog = true },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF10B981).copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "➕",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "开发新游戏",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 当前项目列表
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "当前项目",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 可用员工统计
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3B82F6).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "👥 可用员工: ${availableEmployees.filter { !it.isAssigned }.size}人",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (games.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无进行中的项目",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "点击上方按钮开始开发新游戏",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(games) { game ->
                    EnhancedGameProjectCard(
                        game = game,
                        availableEmployees = availableEmployees.filter { !it.isAssigned },
                        onEmployeeAssigned = { updatedGame, selectedEmployees ->
                            // 更新游戏的员工分配
                            val updatedGames = games.map { existingGame ->
                                if (existingGame.id == updatedGame.id) {
                                    existingGame.copy(assignedEmployees = selectedEmployees)
                                } else {
                                    existingGame
                                }
                            }
                            onGamesUpdate(updatedGames)
                        }
                    )
                }
            }
        }
    }
    
    // 游戏开发流程对话框
    if (showGameDevelopmentDialog) {
        EnhancedGameDevelopmentDialog(
            onDismiss = { showGameDevelopmentDialog = false },
            onGameCreated = { newGame ->
                onGamesUpdate(games + newGame)
                showGameDevelopmentDialog = false
            }
        )
    }
}

/**
 * 增强版游戏开发对话框
 */
@Composable
fun EnhancedGameDevelopmentDialog(
    onDismiss: () -> Unit,
    onGameCreated: (com.example.yjcy.ui.Game) -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var gameName by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf<GameTheme?>(null) }
    var selectedPlatforms by remember { mutableStateOf(setOf<Platform>()) }
    var selectedBusinessModel by remember { mutableStateOf<BusinessModel?>(null) }
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 400.dp)
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
            ) {
                // 标题
                Text(
                    text = when (currentStep) {
                        0 -> "🎮 输入游戏名称"
                        1 -> "🎨 选择游戏主题"
                        2 -> "📱 选择平台和商业模式"
                        3 -> "✅ 确认开发"
                        else -> "开发新游戏"
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 内容
                when (currentStep) {
                    0 -> GameNameInputStep(
                        gameName = gameName,
                        onGameNameChange = { gameName = it }
                    )
                    1 -> GameThemeSelectionStep(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it }
                    )
                    2 -> PlatformAndBusinessModelStep(
                        selectedPlatforms = selectedPlatforms,
                        selectedBusinessModel = selectedBusinessModel,
                        onPlatformToggle = { platform ->
                            selectedPlatforms = if (selectedPlatforms.contains(platform)) {
                                selectedPlatforms - platform
                            } else {
                                selectedPlatforms + platform
                            }
                        },
                        onBusinessModelSelected = { selectedBusinessModel = it }
                    )
                    3 -> GameConfirmationStep(
                        gameName = gameName,
                        theme = selectedTheme,
                        platforms = selectedPlatforms.toList(),
                        businessModel = selectedBusinessModel
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "上一步",
                                color = Color.White
                            )
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "取消",
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            when (currentStep) {
                                3 -> {
                                    // 创建游戏
                                    if (gameName.isNotBlank() && selectedTheme != null && 
                                        selectedPlatforms.isNotEmpty() && selectedBusinessModel != null) {
                                        val newGame = com.example.yjcy.ui.Game(
                                            id = java.util.UUID.randomUUID().toString(),
                                            name = gameName,
                                            theme = selectedTheme!!,
                                            platforms = selectedPlatforms.toList(),
                                            businessModel = selectedBusinessModel!!,
                                            developmentProgress = 0f,
                                            isCompleted = false,
                                            revenue = 0L,
                                            assignedEmployees = emptyList()
                                        )
                                        onGameCreated(newGame)
                                    }
                                }
                                else -> currentStep++
                            }
                        },
                        enabled = when (currentStep) {
                            0 -> gameName.isNotBlank()
                            1 -> selectedTheme != null
                            2 -> selectedPlatforms.isNotEmpty() && selectedBusinessModel != null
                            3 -> true
                            else -> false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (currentStep == 3) "创建游戏" else "下一步",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取默认员工数据
 */
fun getDefaultEmployees(): List<Employee> {
    return listOf(
        Employee(1, "张程序", "程序员", 3, 1, 1, 1, 1, 8000, false),
        Employee(2, "李美术", "美术师", 1, 1, 3, 1, 1, 7000, false),
        Employee(3, "王策划", "策划师", 1, 3, 1, 1, 1, 6500, false),
        Employee(4, "赵音效", "音效师", 1, 1, 1, 3, 1, 6000, false),
        Employee(5, "陈客服", "客服", 1, 1, 1, 1, 3, 5000, false),
        Employee(6, "刘全能", "程序员", 2, 2, 2, 2, 2, 9000, false),
        Employee(7, "孙设计", "美术师", 1, 2, 4, 1, 1, 7500, false),
        Employee(8, "周创意", "策划师", 1, 4, 1, 2, 1, 7200, false)
    )
}

// 游戏开发步骤组件（简化版本，实际应该从原文件导入）
@Composable
fun GameNameInputStep(
    gameName: String,
    onGameNameChange: (String) -> Unit
) {
    OutlinedTextField(
        value = gameName,
        onValueChange = onGameNameChange,
        label = { Text("游戏名称", color = Color.White.copy(alpha = 0.7f)) },
        placeholder = { Text("请输入游戏名称", color = Color.White.copy(alpha = 0.5f)) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF10B981),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun GameThemeSelectionStep(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(GameTheme.values().toList()) { theme ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(theme) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTheme == theme) 
                        Color(0xFF10B981).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = theme.icon,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = theme.displayName,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlatformAndBusinessModelStep(
    selectedPlatforms: Set<Platform>,
    selectedBusinessModel: BusinessModel?,
    onPlatformToggle: (Platform) -> Unit,
    onBusinessModelSelected: (BusinessModel) -> Unit
) {
    Column {
        Text(
            text = "选择平台:",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Platform.values().forEach { platform ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlatformToggle(platform) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPlatforms.contains(platform)) 
                        Color(0xFF3B82F6).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = platform.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = platform.displayName,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "选择商业模式:",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        BusinessModel.values().forEach { model ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBusinessModelSelected(model) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedBusinessModel == model) 
                        Color(0xFF10B981).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = model.displayName,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun GameConfirmationStep(
    gameName: String,
    theme: GameTheme?,
    platforms: List<Platform>,
    businessModel: BusinessModel?
) {
    Column {
        Text(
            text = "确认游戏信息:",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "游戏名称: $gameName",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "主题: ${theme?.displayName ?: "未选择"}",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "平台: ${platforms.joinToString(", ") { it.displayName }}",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "商业模式: ${businessModel?.displayName ?: "未选择"}",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}