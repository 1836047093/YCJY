package com.example.yjcy.ui

import com.example.yjcy.data.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin


/**
 * 增强版项目管理内容组件，集成员工分配功能
 */
@Composable
fun EnhancedProjectManagementContent(
    games: List<Game> = emptyList(),
    onGamesUpdate: (List<Game>) -> Unit = {},
    founder: Founder? = null,
    availableEmployees: List<Employee> = founder?.let { listOf(it.toEmployee()) } ?: getDefaultEmployees()
) {
    var showGameDevelopmentDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
                    )
                )
            )
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
                    text = "👥 可用员工: ${availableEmployees.size}人",
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
                        availableEmployees = availableEmployees,
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
        SuperEnhancedGameDevelopmentDialog(
            onDismiss = { showGameDevelopmentDialog = false },
            onGameCreated = { newGame ->
                onGamesUpdate(games + newGame)
                showGameDevelopmentDialog = false
            }
        )
    }
}

// 新增的增强版主题选择组件
@Composable
fun EnhancedGameThemeSelectionStep(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ThemeSelectionBox(
        selectedTheme = selectedTheme,
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        EnhancedThemeSelectionDialog(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun EnhancedThemeSelectionDialog(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedTheme by remember { mutableStateOf(selectedTheme) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Text(
                text = "选择游戏主题",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            AnimatedThemeGrid(
                selectedTheme = tempSelectedTheme,
                onThemeSelected = { theme ->
                    tempSelectedTheme = theme
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "取消",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    tempSelectedTheme?.let { onThemeSelected(it) }
                    onDismiss()
                },
                enabled = tempSelectedTheme != null
            ) {
                Text(
                    text = "确定",
                    color = if (tempSelectedTheme != null) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    )
}

@Composable
fun AnimatedThemeGrid(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(GameTheme.values().toList()) { theme ->
            AnimatedThemeCard(
                theme = theme,
                isSelected = selectedTheme == theme,
                onClick = { onThemeSelected(theme) }
            )
        }
    }
}

/**
 * 超级增强版游戏开发对话框 - 使用增强版主题选择组件
 * 与EnhancedGameDevelopmentDialog完全相同，但在主题选择步骤中使用EnhancedGameThemeSelectionStep
 */
@Composable
fun SuperEnhancedGameDevelopmentDialog(
    onDismiss: () -> Unit,
    onGameCreated: (Game) -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var gameName by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf<GameTheme?>(null) }
    var selectedPlatforms by remember { mutableStateOf(setOf<Platform>()) }
    var selectedBusinessModel by remember { mutableStateOf<BusinessModel?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Column {
                Text(
                    text = "开发新游戏",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // 步骤指示器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(
                                    color = if (index <= currentStep) Color(0xFF10B981) else Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 步骤标题已删除
                
                // 步骤内容
                when (currentStep) {
                    0 -> GameNameInputStep(
                        gameName = gameName,
                        onGameNameChange = { gameName = it }
                    )
                    1 -> EnhancedGameThemeSelectionStep(
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
                    // 按钮将在confirmButton和dismissButton中处理
                }
            }
        },
        dismissButton = {
            if (currentStep > 0) {
                TextButton(
                    onClick = { currentStep-- }
                ) {
                    Text(
                        text = "上一步",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "取消",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                TextButton(
                    onClick = {
                        when (currentStep) {
                            3 -> {
                                // 创建游戏
                                if (gameName.isNotBlank() && selectedTheme != null && 
                                    selectedPlatforms.isNotEmpty() && selectedBusinessModel != null) {
                                    val newGame = Game(
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
                    }
                ) {
                    Text(
                        text = if (currentStep == 3) "创建游戏" else "下一步",
                        color = if (when (currentStep) {
                            0 -> gameName.isNotBlank()
                            1 -> selectedTheme != null
                            2 -> selectedPlatforms.isNotEmpty() && selectedBusinessModel != null
                            3 -> true
                            else -> false
                        }) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    )
}

@Composable
fun AnimatedThemeCard(
    theme: GameTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "borderAlpha"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.3f else 0.1f,
        animationSpec = tween(300),
        label = "backgroundAlpha"
    )
    
    // 只有选中状态下才有微动画效果
    val iconScale = if (isSelected) {
        val infiniteTransition = rememberInfiniteTransition(label = "iconAnimation")
        val iconPulse by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "iconPulse"
        )
        1f + (sin(iconPulse) * 0.03f)
    } else {
        1f
    }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF10B981).copy(alpha = backgroundAlpha) else Color.White.copy(alpha = backgroundAlpha)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF10B981).copy(alpha = borderAlpha)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = theme.icon,
                fontSize = 28.sp,
                modifier = Modifier.scale(iconScale)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = theme.displayName,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * 增强版游戏开发对话框
 */
@Composable
fun EnhancedGameDevelopmentDialog(
    onDismiss: () -> Unit,
    onGameCreated: (Game) -> Unit
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
                                        val newGame = Game(
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
    return emptyList()
}

@Composable
fun GameNameInputStep(
    gameName: String,
    onGameNameChange: (String) -> Unit
) {
    Column {
        Text(
            text = "请输入游戏名称：",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        OutlinedTextField(
            value = gameName,
            onValueChange = onGameNameChange,
            placeholder = {
                Text(
                    text = "例如：超级冒险",
                    color = Color.White.copy(alpha = 0.5f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF10B981)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GameThemeSelectionStep(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ThemeSelectionBox(
        selectedTheme = selectedTheme,
        onClick = { showDialog = true }
    )
    
    if (showDialog) {
        ThemeSelectionDialog(
            selectedTheme = selectedTheme,
            onThemeSelected = { theme ->
                onThemeSelected(theme)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ThemeSelectionBox(
    selectedTheme: GameTheme?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedTheme != null) {
                    Text(
                        text = selectedTheme.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = selectedTheme.displayName,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "请选择游戏主题",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            Text(
                text = "▼",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Text(
                text = "选择游戏主题",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            ThemeGrid(
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "取消",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ThemeGrid(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(GameTheme.values().toList()) { theme ->
            ThemeCard(
                theme = theme,
                isSelected = selectedTheme == theme,
                onClick = { onThemeSelected(theme) }
            )
        }
    }
}

@Composable
fun ThemeCard(
    theme: GameTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF10B981).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF10B981)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = theme.icon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = theme.displayName,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun PlatformSelectionBox(
    selectedPlatforms: Set<Platform>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedPlatforms.isNotEmpty()) {
                    // 显示前3个平台的图标
                    selectedPlatforms.take(3).forEach { platform ->
                        Text(
                            text = platform.icon,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    if (selectedPlatforms.size > 3) {
                        Text(
                            text = "+${selectedPlatforms.size - 3}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Text(
                        text = "已选择 ${selectedPlatforms.size} 个平台",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Text(
                        text = "请选择发布平台",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            Text(
                text = "▼",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PlatformCard(
    platform: Platform,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF10B981).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF10B981)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = platform.icon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = platform.displayName,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun PlatformGrid(
    selectedPlatforms: Set<Platform>,
    onPlatformToggle: (Platform) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(Platform.values().toList()) { platform ->
            PlatformCard(
                platform = platform,
                isSelected = selectedPlatforms.contains(platform),
                onClick = { onPlatformToggle(platform) }
            )
        }
    }
}

@Composable
fun PlatformSelectionDialog(
    selectedPlatforms: Set<Platform>,
    onPlatformToggle: (Platform) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Text(
                text = "选择发布平台",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            PlatformGrid(
                selectedPlatforms = selectedPlatforms,
                onPlatformToggle = onPlatformToggle
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "取消",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "确定",
                    color = Color(0xFF10B981)
                )
            }
        }
    )
}

@Composable
fun PlatformAndBusinessModelStep(
    selectedPlatforms: Set<Platform>,
    selectedBusinessModel: BusinessModel?,
    onPlatformToggle: (Platform) -> Unit,
    onBusinessModelSelected: (BusinessModel) -> Unit
) {
    var showPlatformDialog by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "选择平台:",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        PlatformSelectionBox(
            selectedPlatforms = selectedPlatforms,
            onClick = { showPlatformDialog = true }
        )
        
        if (showPlatformDialog) {
            PlatformSelectionDialog(
                selectedPlatforms = selectedPlatforms,
                onPlatformToggle = onPlatformToggle,
                onDismiss = { showPlatformDialog = false }
            )
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