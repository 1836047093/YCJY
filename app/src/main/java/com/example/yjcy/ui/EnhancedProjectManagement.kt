package com.example.yjcy.ui

import com.example.yjcy.data.*
import com.example.yjcy.utils.formatMoneyWithDecimals
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
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
import androidx.compose.ui.window.Dialog
import kotlin.math.PI
import kotlin.math.sin

// 项目显示类型枚举
enum class ProjectDisplayType(val displayName: String) {
    DEVELOPING("正在开发"),
    UPDATING("正在更新"),
    RELEASED("已发售"),
    REMOVED("已下架"),
    IP_LIBRARY("IP库")
}

// 为 ProjectDisplayType 创建自定义 Saver
val ProjectDisplayTypeSaver = Saver<ProjectDisplayType, String>(
    save = { it.name },
    restore = { name -> ProjectDisplayType.valueOf(name) }
)

@Composable
fun ProjectTypeDropdown(
    selectedType: ProjectDisplayType,
    onTypeSelected: (ProjectDisplayType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Card(
            modifier = Modifier
                .clickable { expanded = true },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4F46E5).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedType.displayName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "展开下拉菜单",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                Color(0xFF1E1B4B),
                RoundedCornerShape(8.dp)
            )
        ) {
            ProjectDisplayType.values().forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = type.displayName,
                            color = if (type == selectedType) Color(0xFF60A5FA) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (type == selectedType) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (type == selectedType) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color.Transparent
                    )
                )
            }
        }
    }
}


/**
 * 增强版项目管理内容组件，集成员工分配功能
 */
@Composable
fun EnhancedProjectManagementContent(
    games: List<Game> = emptyList(),
    onGamesUpdate: (List<Game>) -> Unit = {},
    founder: Founder? = null,
    availableEmployees: List<Employee> = founder?.let { listOf(it.toEmployee()) } ?: getDefaultEmployees(),
    refreshTrigger: Int = 0,  // 新增：用于触发UI刷新
    onSwitchToCurrentProjects: (() -> Unit)? = null,
    onReleaseGame: ((Game) -> Unit)? = null,  // 新增：发售游戏回调
    onAbandonGame: ((Game) -> Unit)? = null,  // 新增：废弃游戏回调
    selectedProjectType: ProjectDisplayType = ProjectDisplayType.DEVELOPING,  // 外部控制的标签页状态
    onProjectTypeChange: (ProjectDisplayType) -> Unit = {},  // 标签页变化回调
    money: Long = 0L,  // 新增：资金
    fans: Long = 0L,  // 新增：粉丝数
    onMoneyUpdate: (Long) -> Unit = {},  // 新增：资金更新回调
    onFansUpdate: (Long) -> Unit = {},  // 新增：粉丝更新回调
    complaints: List<Complaint> = emptyList(),  // 新增：客诉列表
    onComplaintsUpdate: (List<Complaint>) -> Unit = {},  // 新增：客诉更新回调
    autoProcessComplaints: Boolean = false,  // 新增：自动处理客诉开关
    onAutoProcessToggle: (Boolean) -> Unit = {},  // 新增：自动处理开关回调
    autoPromotionThreshold: Float = 0.5f,  // 新增：自动宣传阈值
    onAutoPromotionThresholdUpdate: (Float) -> Unit = {},  // 新增：自动宣传阈值更新回调
    currentYear: Int = 1,  // 新增：当前年份
    currentMonth: Int = 1,  // 新增：当前月份
    currentDay: Int = 1,  // 新增：当前日期
    currentMinuteOfDay: Int = 0,  // 新增：当天内的分钟数（0-1439）
    ownedIPs: List<com.example.yjcy.data.GameIP> = emptyList(),  // 新增：拥有的IP列表
    onPauseGame: (() -> Unit)? = null,  // 暂停游戏的回调
    onResumeGame: (() -> Unit)? = null // 恢复游戏的回调
) {
    var showGameDevelopmentDialog by remember { mutableStateOf(false) }
    var showPromotionCenterDialog by remember { mutableStateOf(false) }
    var showCustomerServiceDialog by remember { mutableStateOf(false) }
    
    // 根据选择的项目类型过滤游戏列表
    val filteredGames = remember(games, selectedProjectType, refreshTrigger) {
        when (selectedProjectType) {
            ProjectDisplayType.DEVELOPING -> games.filter { game ->
                // 开发中、准备发售、价格设置中的游戏
                game.releaseStatus in listOf(
                    GameReleaseStatus.DEVELOPMENT,
                    GameReleaseStatus.READY_FOR_RELEASE,
                    GameReleaseStatus.PRICE_SETTING
                )
            }
            ProjectDisplayType.UPDATING -> games.filter { game ->
                // 已发售但有进行中更新任务的游戏
                val isReleased = game.releaseStatus in listOf(
                    GameReleaseStatus.RELEASED,
                    GameReleaseStatus.RATED
                )
                if (isReleased) {
                    val gameRevenue = RevenueManager.getGameRevenue(game.id)
                    val updateTask = gameRevenue?.updateTask
                    updateTask != null && updateTask.progressPoints < updateTask.requiredPoints
                } else {
                    false
                }
            }
            ProjectDisplayType.RELEASED -> games.filter { game ->
                // 所有已发售的游戏，包括正在更新的游戏
                game.releaseStatus in listOf(
                    GameReleaseStatus.RELEASED,
                    GameReleaseStatus.RATED
                )
            }
            ProjectDisplayType.REMOVED -> games.filter {
                it.releaseStatus == GameReleaseStatus.REMOVED_FROM_MARKET
            }
            ProjectDisplayType.IP_LIBRARY -> emptyList() // IP库不显示游戏列表
        }
    }
    
    // 计算待处理的客诉数量
    val pendingComplaintsCount = remember(complaints) {
        complaints.count { it.status == ComplaintStatus.PENDING }
    }
    
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
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // 客服中心和宣传中心按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 客服中心按钮
            BadgeBox(
                modifier = Modifier.weight(1f),
                showBadge = pendingComplaintsCount > 0,
                badgeCount = null  // 只显示红点，不显示数字
            ) {
                Button(
                    onClick = { showCustomerServiceDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "📞",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "客服中心",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 宣传中心按钮
            Button(
                onClick = { showPromotionCenterDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEA580C).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "📢",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "宣传中心",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
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
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 项目列表标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedProjectType.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 项目类型下拉选择框
                ProjectTypeDropdown(
                    selectedType = selectedProjectType,
                    onTypeSelected = onProjectTypeChange
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // IP库页面
        if (selectedProjectType == ProjectDisplayType.IP_LIBRARY) {
            IPLibraryContent(
                ownedIPs = ownedIPs,
                modifier = Modifier.fillMaxSize()
            )
        } else if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📝",
                        fontSize = 48.sp
                    )
                    Text(
                        text = when (selectedProjectType) {
                            ProjectDisplayType.DEVELOPING -> "暂无正在开发的游戏"
                            ProjectDisplayType.UPDATING -> "暂无正在更新的游戏"
                            ProjectDisplayType.RELEASED -> "暂无已发售的游戏"
                            ProjectDisplayType.REMOVED -> "暂无已下架的游戏"
                            ProjectDisplayType.IP_LIBRARY -> "" // 不会显示，因为有IP库组件
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = when (selectedProjectType) {
                            ProjectDisplayType.DEVELOPING -> "点击上方按钮开始开发新游戏"
                            ProjectDisplayType.UPDATING -> "已发售游戏开始更新后将在此显示"
                            ProjectDisplayType.RELEASED -> "完成游戏开发并发售后将在此显示"
                            ProjectDisplayType.REMOVED -> "下架的游戏将在此显示"
                            ProjectDisplayType.IP_LIBRARY -> "" // 不会显示，因为有IP库组件
                        },
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredGames) { game ->
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
                        },
                        onGameUpdate = { updatedGame ->
                            // 通用游戏更新回调，支持下架等操作
                            val updatedGames = games.map { existingGame ->
                                if (existingGame.id == updatedGame.id) {
                                    updatedGame
                                } else {
                                    existingGame
                                }
                            }
                            onGamesUpdate(updatedGames)
                        },
                        refreshTrigger = refreshTrigger,
                        onSwitchToCurrentProjects = {
                            onProjectTypeChange(ProjectDisplayType.UPDATING)
                            onSwitchToCurrentProjects?.invoke()
                        },
                        onReleaseGame = onReleaseGame,
                        onAbandonGame = onAbandonGame,
                        showDataOverview = selectedProjectType != ProjectDisplayType.UPDATING,  // 正在更新标签页不显示数据概览
                        money = money,
                        onMoneyUpdate = onMoneyUpdate,
                        currentYear = currentYear,
                        currentMonth = currentMonth,
                        currentDay = currentDay,
                        currentMinuteOfDay = currentMinuteOfDay,
                        onPauseGame = onPauseGame,
                        onResumeGame = onResumeGame
                    )
                }
            }
        }
    }
    
    // 游戏开发流程对话框
    // 监听对话框打开/关闭，控制游戏暂停
    LaunchedEffect(showGameDevelopmentDialog) {
        if (showGameDevelopmentDialog) {
            // 打开对话框时暂停游戏
            onPauseGame?.invoke()
        } else {
            // 关闭对话框时恢复游戏
            onResumeGame?.invoke()
        }
    }
    
    if (showGameDevelopmentDialog) {
        SuperEnhancedGameDevelopmentDialog(
            money = money,
            ownedIPs = ownedIPs,
            onDismiss = { showGameDevelopmentDialog = false },
            onGameCreated = { newGame ->
                // 扣除开发费用
                onMoneyUpdate(money - newGame.developmentCost)
                onGamesUpdate(games + newGame)
                showGameDevelopmentDialog = false
            }
        )
    }
    
    // 宣传中心对话框
    if (showPromotionCenterDialog) {
        PromotionCenterDialog(
            games = games,
            money = money,
            fans = fans,
            autoPromotionThreshold = autoPromotionThreshold,
            onDismiss = { showPromotionCenterDialog = false },
            onMoneyUpdate = onMoneyUpdate,
            onFansUpdate = onFansUpdate,
            onGamesUpdate = onGamesUpdate,
            onAutoPromotionThresholdUpdate = onAutoPromotionThresholdUpdate
        )
    }
    
    // 客服中心对话框
    if (showCustomerServiceDialog) {
        CustomerServiceDialog(
            complaints = complaints,
            employees = availableEmployees,
            fans = fans,
            currentYear = currentYear,
            currentMonth = currentMonth,
            currentDay = currentDay,
            autoProcessEnabled = autoProcessComplaints,
            onAutoProcessToggle = onAutoProcessToggle,
            onDismiss = { showCustomerServiceDialog = false },
            onComplaintsUpdate = onComplaintsUpdate
        )
    }
}

/**
 * 客服中心对话框（居中显示，类似宣传中心）
 */
@Composable
fun CustomerServiceDialog(
    complaints: List<Complaint>,
    employees: List<Employee>,
    fans: Long,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    autoProcessEnabled: Boolean,
    onAutoProcessToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onComplaintsUpdate: (List<Complaint>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📞 客服中心",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            CustomerServiceContent(
                complaints = complaints,
                employees = employees,
                fans = fans,
                currentYear = currentYear,
                currentMonth = currentMonth,
                currentDay = currentDay,
                autoProcessEnabled = autoProcessEnabled,
                onAutoProcessToggle = onAutoProcessToggle,
                onComplaintsUpdate = onComplaintsUpdate
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = Color.White)
            }
        }
    )
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
        items(GameTheme.entries.toList()) { theme ->
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
    money: Long,
    ownedIPs: List<com.example.yjcy.data.GameIP> = emptyList(),
    onDismiss: () -> Unit,
    onGameCreated: (Game) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var gameName by remember { mutableStateOf("") }
    var isGameNameValid by remember { mutableStateOf(true) }
    var gameNameError by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf<GameTheme?>(null) }
    var selectedPlatforms by remember { mutableStateOf(setOf<Platform>()) }
    var selectedBusinessModel by remember { mutableStateOf<BusinessModel?>(null) }
    var monetizationItems by remember { mutableStateOf<List<com.example.yjcy.data.MonetizationItem>>(emptyList()) }
    var selectedIP by remember { mutableStateOf<com.example.yjcy.data.GameIP?>(null) }
    var showStrategyDialog by remember { mutableStateOf(false) }
    
    // 计算总步骤数：基础步骤 + (有IP时+1步) + (网游时+1步)
    val hasIPStep = ownedIPs.isNotEmpty()
    val hasMonetizationStep = selectedBusinessModel == BusinessModel.ONLINE_GAME
    val totalSteps = 2 + (if (hasIPStep) 1 else 0) + 1 + (if (hasMonetizationStep) 1 else 0)
    // 步骤0：主题和名称
    // 步骤1（可选）：IP选择（如果有IP）
    // 步骤N：平台和商业模式
    // 步骤N+1（可选）：付费内容（仅网游）
    // 步骤最后：确认
    
    // 步骤索引计算
    val ipStepIndex = if (hasIPStep) 1 else -1
    val platformStepIndex = if (hasIPStep) 2 else 1
    val monetizationStepIndex = if (hasMonetizationStep) platformStepIndex + 1 else -1
    val confirmStepIndex = platformStepIndex + (if (hasMonetizationStep) 2 else 1)
    val isLastStep = currentStep >= totalSteps - 1
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "开发新游戏",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // 10分攻略问号按钮
                    TextButton(
                        onClick = { showStrategyDialog = true },
                        modifier = Modifier.size(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "❓",
                            fontSize = 20.sp
                        )
                    }
                }
                
                // 步骤指示器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(totalSteps) { index ->
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
                when {
                    currentStep == 0 -> ThemeAndNameInputStep(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it },
                        gameName = gameName,
                        onGameNameChange = { newValue ->
                            gameName = newValue
                            when {
                                newValue.isEmpty() -> {
                                    isGameNameValid = true
                                    gameNameError = ""
                                }
                                newValue.length > 20 -> {
                                    isGameNameValid = false
                                    gameNameError = "游戏名最多20个字符"
                                }
                                com.example.yjcy.utils.SensitiveWordFilter.containsSensitiveGameName(newValue) -> {
                                    isGameNameValid = false
                                    gameNameError = "存在敏感词汇，请换个游戏名"
                                }
                                else -> {
                                    isGameNameValid = true
                                    gameNameError = ""
                                }
                            }
                        },
                        isGameNameValid = isGameNameValid,
                        gameNameError = gameNameError
                    )
                    currentStep == ipStepIndex -> {
                        // IP选择步骤（如果有IP可用）
                        IPSelectionStep(
                            ownedIPs = ownedIPs,
                            selectedIP = selectedIP,
                            onIPSelected = { selectedIP = it }
                        )
                    }
                    currentStep == platformStepIndex -> PlatformAndBusinessModelStep(
                        selectedPlatforms = selectedPlatforms,
                        selectedBusinessModel = selectedBusinessModel,
                        onPlatformToggle = { platform ->
                            selectedPlatforms = if (selectedPlatforms.contains(platform)) {
                                selectedPlatforms - platform
                            } else {
                                selectedPlatforms + platform
                            }
                        },
                        onBusinessModelSelected = { selectedBusinessModel = it },
                        money = money
                    )
                    currentStep == monetizationStepIndex -> {
                        // 网络游戏的付费内容选择
                        MonetizationSelectionStep(
                            selectedTheme = selectedTheme,
                            monetizationItems = monetizationItems,
                            onMonetizationItemsChange = { monetizationItems = it }
                        )
                    }
                    else -> {
                        // 确认步骤（单机游戏在平台选择后直接到这里，网游在付费内容后到这里）
                        GameConfirmationStepWithIP(
                            gameName = gameName,
                            theme = selectedTheme,
                            platforms = selectedPlatforms.toList(),
                            businessModel = selectedBusinessModel,
                            selectedIP = selectedIP
                        )
                    }
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
                        if (isLastStep) {
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
                                    assignedEmployees = emptyList(),
                                    monetizationItems = monetizationItems,
                                    fromIP = selectedIP
                                ).let { game ->
                                    // 计算平台开发费用
                                    val totalPlatformCost = selectedPlatforms.sumOf { it.developmentCost.toLong() }
                                    game.copy(developmentCost = totalPlatformCost)
                                }
                                onGameCreated(newGame)
                            }
                        } else {
                            currentStep++
                        }
                    },
                    enabled = when {
                        currentStep == 0 -> gameName.isNotBlank() && isGameNameValid && selectedTheme != null
                        currentStep == ipStepIndex -> true  // IP选择是可选的
                        currentStep == platformStepIndex -> selectedPlatforms.isNotEmpty() && selectedBusinessModel != null
                        currentStep == monetizationStepIndex -> true  // 付费内容是可选的
                        else -> {
                            // 最后一步（确认）：检查资金是否足够
                            val totalCost = selectedPlatforms.sumOf { it.developmentCost.toLong() }
                            money >= totalCost
                        }
                    }
                ) {
                    val totalCost = selectedPlatforms.sumOf { it.developmentCost.toLong() }
                    val canAfford = money >= totalCost
                    val buttonText = if (isLastStep) {
                        if (canAfford) "创建游戏" else "资金不足"
                    } else {
                        "下一步"
                    }
                    
                    Text(
                        text = buttonText,
                        color = if (when {
                            currentStep == 0 -> gameName.isNotBlank() && isGameNameValid && selectedTheme != null
                            currentStep == ipStepIndex -> true
                            currentStep == platformStepIndex -> selectedPlatforms.isNotEmpty() && selectedBusinessModel != null
                            currentStep == monetizationStepIndex -> true
                            else -> canAfford
                        }) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    )
    
    // 10分攻略对话框
    if (showStrategyDialog) {
        GameTenPointStrategyDialog(
            onDismiss = { showStrategyDialog = false }
        )
    }
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
    money: Long,
    onDismiss: () -> Unit,
    onGameCreated: (Game) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var gameName by remember { mutableStateOf("") }
    var isGameNameValid by remember { mutableStateOf(true) }
    var gameNameError by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf<GameTheme?>(null) }
    var selectedPlatforms by remember { mutableStateOf(setOf<Platform>()) }
    var selectedBusinessModel by remember { mutableStateOf<BusinessModel?>(null) }
    
    Dialog(
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
                        0 -> "🎨 游戏主题和名称"
                        1 -> "📱 选择平台和商业模式"
                        2 -> "✅ 确认开发"
                        else -> "开发新游戏"
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 内容
                when (currentStep) {
                    0 -> ThemeAndNameInputStep(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it },
                        gameName = gameName,
                        onGameNameChange = { newValue ->
                            gameName = newValue
                            when {
                                newValue.isEmpty() -> {
                                    isGameNameValid = true
                                    gameNameError = ""
                                }
                                newValue.length > 20 -> {
                                    isGameNameValid = false
                                    gameNameError = "游戏名最多20个字符"
                                }
                                com.example.yjcy.utils.SensitiveWordFilter.containsSensitiveGameName(newValue) -> {
                                    isGameNameValid = false
                                    gameNameError = "存在敏感词汇，请换个游戏名"
                                }
                                else -> {
                                    isGameNameValid = true
                                    gameNameError = ""
                                }
                            }
                        },
                        isGameNameValid = isGameNameValid,
                        gameNameError = gameNameError
                    )
                    1 -> PlatformAndBusinessModelStep(
                        selectedPlatforms = selectedPlatforms,
                        selectedBusinessModel = selectedBusinessModel,
                        onPlatformToggle = { platform ->
                            selectedPlatforms = if (selectedPlatforms.contains(platform)) {
                                selectedPlatforms - platform
                            } else {
                                selectedPlatforms + platform
                            }
                        },
                        onBusinessModelSelected = { selectedBusinessModel = it },
                        money = money
                    )
                    2 -> GameConfirmationStep(
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
                            border = BorderStroke(
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
                                2 -> {
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
                                        ).let { game ->
                                            // 计算平台开发费用
                                            val totalPlatformCost = selectedPlatforms.sumOf { it.developmentCost.toLong() }
                                            game.copy(developmentCost = totalPlatformCost)
                                        }
                                        onGameCreated(newGame)
                                    }
                                }
                                else -> currentStep++
                            }
                        },
                        enabled = when (currentStep) {
                            0 -> gameName.isNotBlank() && isGameNameValid && selectedTheme != null
                            1 -> selectedPlatforms.isNotEmpty() && selectedBusinessModel != null
                            2 -> true
                            else -> false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (currentStep == 2) "创建游戏" else "下一步",
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
    onGameNameChange: (String) -> Unit,
    isGameNameValid: Boolean = true,
    gameNameError: String = "",
    selectedTheme: GameTheme? = null
) {
    Column {
        Text(
            text = "请输入游戏名称：",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = gameName,
                onValueChange = onGameNameChange,
                isError = !isGameNameValid,
                placeholder = {
                    Text(
                        text = "例如：超级冒险",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isGameNameValid) Color(0xFF10B981) else Color.Red,
                    unfocusedBorderColor = if (isGameNameValid) Color.White.copy(alpha = 0.3f) else Color.Red,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF10B981),
                    errorBorderColor = Color.Red
                ),
                modifier = Modifier.weight(1f)
            )
            
            // 一键生成游戏名按钮
            Button(
                onClick = {
                    if (selectedTheme != null) {
                        val generatedName = com.example.yjcy.utils.GameNameGenerator.generateGameName(selectedTheme)
                        onGameNameChange(generatedName)
                    }
                },
                enabled = selectedTheme != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6),
                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text(
                    text = "✨",
                    fontSize = 18.sp
                )
            }
        }
        
        if (selectedTheme == null) {
            Text(
                text = "💡 请先选择游戏主题后可使用一键生成",
                color = Color(0xFFFBBF24),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        if (!isGameNameValid && gameNameError.isNotEmpty()) {
            Text(
                text = gameNameError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 组合的主题选择和游戏名输入步骤
 */
@Composable
fun ThemeAndNameInputStep(
    selectedTheme: GameTheme?,
    onThemeSelected: (GameTheme) -> Unit,
    gameName: String,
    onGameNameChange: (String) -> Unit,
    isGameNameValid: Boolean = true,
    gameNameError: String = ""
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 主题选择部分
        Column {
            Text(
                text = "选择游戏主题：",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            EnhancedGameThemeSelectionStep(
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected
            )
        }
        
        // 游戏名输入部分
        Column {
            Text(
                text = "请输入游戏名称：",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = gameName,
                    onValueChange = onGameNameChange,
                    isError = !isGameNameValid,
                    placeholder = {
                        Text(
                            text = "例如：超级冒险",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isGameNameValid) Color(0xFF10B981) else Color.Red,
                        unfocusedBorderColor = if (isGameNameValid) Color.White.copy(alpha = 0.3f) else Color.Red,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF10B981),
                        errorBorderColor = Color.Red
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                // 一键生成游戏名按钮
                Button(
                    onClick = {
                        if (selectedTheme != null) {
                            val generatedName = com.example.yjcy.utils.GameNameGenerator.generateGameName(selectedTheme)
                            onGameNameChange(generatedName)
                        }
                    },
                    enabled = selectedTheme != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6),
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(
                        text = "✨",
                        fontSize = 18.sp
                    )
                }
            }
            
            if (selectedTheme == null) {
                Text(
                    text = "💡 请先选择游戏主题后可使用一键生成",
                    color = Color(0xFFFBBF24),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (!isGameNameValid && gameNameError.isNotEmpty()) {
                Text(
                    text = gameNameError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
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
        items(GameTheme.entries.toList()) { theme ->
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatMoneyWithDecimals(platform.developmentCost.toDouble()),
                color = Color(0xFFF59E0B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
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
        items(Platform.entries.toList()) { platform ->
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
    onDismiss: () -> Unit,
    money: Long = 0L  // 新增：玩家资金
) {
    var showInsufficientFundsDialog by remember { mutableStateOf(false) }
    val totalCost = selectedPlatforms.sumOf { it.developmentCost }
    val allPlatforms = Platform.entries.toSet()
    val isAllSelected = selectedPlatforms.size == allPlatforms.size
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "选择发布平台",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // 全选按钮（现代化设计）
                Surface(
                    modifier = Modifier
                        .clickable {
                            if (isAllSelected) {
                                // 取消全选
                                allPlatforms.forEach { platform ->
                                    if (selectedPlatforms.contains(platform)) {
                                        onPlatformToggle(platform)
                                    }
                                }
                            } else {
                                // 全选
                                allPlatforms.forEach { platform ->
                                    if (!selectedPlatforms.contains(platform)) {
                                        onPlatformToggle(platform)
                                    }
                                }
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAllSelected) 
                        Color(0xFFEF4444).copy(alpha = 0.15f)
                    else 
                        Color(0xFFEF4444).copy(alpha = 0.1f),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = Color(0xFFEF4444).copy(alpha = if (isAllSelected) 1f else 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAllSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isAllSelected) "取消全选" else "全选",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
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
                onClick = {
                    // 检查资金是否足够
                    if (selectedPlatforms.isNotEmpty() && totalCost > money) {
                        showInsufficientFundsDialog = true
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(
                    text = "确定",
                    color = Color(0xFF10B981)
                )
            }
        }
    )
    
    // 资金不足提示对话框
    if (showInsufficientFundsDialog) {
        AlertDialog(
            onDismissRequest = { showInsufficientFundsDialog = false },
            containerColor = Color(0xFF1F2937),
            title = {
                Text(
                    text = "⚠️ 资金不足",
                    color = Color(0xFFEF4444),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "所选平台的开发费用超出了您的资金！",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "总开发费用: ${formatMoneyWithDecimals(totalCost.toDouble())}",
                        color = Color(0xFFF59E0B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "当前资金: ${formatMoneyWithDecimals(money.toDouble())}",
                        color = Color(0xFF10B981),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "还差: ${formatMoneyWithDecimals((totalCost - money).toDouble())}",
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInsufficientFundsDialog = false }
                ) {
                    Text(
                        text = "知道了",
                        color = Color(0xFF10B981)
                    )
                }
            }
        )
    }
}

@Composable
fun PlatformAndBusinessModelStep(
    selectedPlatforms: Set<Platform>,
    selectedBusinessModel: BusinessModel?,
    onPlatformToggle: (Platform) -> Unit,
    onBusinessModelSelected: (BusinessModel) -> Unit,
    money: Long = 0L  // 新增：玩家资金
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
                onDismiss = { showPlatformDialog = false },
                money = money  // 传递资金参数
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
        
        listOf(BusinessModel.SINGLE_PLAYER, BusinessModel.ONLINE_GAME).forEach { model ->
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
                
                val developmentCost = platforms.sumOf { it.developmentCost }
                Text(
                    text = "开发费用: ${formatMoneyWithDecimals(developmentCost.toDouble())}",
                    color = Color(0xFFF59E0B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * IP选择步骤 - 可选择使用已有IP或原创游戏
 */
@Composable
fun IPSelectionStep(
    ownedIPs: List<com.example.yjcy.data.GameIP>,
    selectedIP: com.example.yjcy.data.GameIP?,
    onIPSelected: (com.example.yjcy.data.GameIP?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "选择游戏IP (可选):",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "使用已有IP可以获得销量加成",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 原创选项
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIPSelected(null) },
            colors = CardDefaults.cardColors(
                containerColor = if (selectedIP == null) 
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
                    text = "✨",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "原创游戏",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "不使用IP，全新原创作品",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                if (selectedIP == null) {
                    Text(
                        text = "✓",
                        color = Color(0xFF10B981),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 拥有的IP列表
        ownedIPs.forEach { ip ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIPSelected(ip) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedIP?.id == ip.id) 
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
                        text = "🎯",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ip.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "来自: ${ip.originalCompany}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            // IP等级
                            Text(
                                text = ip.getIPLevel(),
                                color = when {
                                    ip.originalRating >= 7.5f -> Color(0xFF4CAF50) // 绿色 - 知名IP
                                    ip.originalRating >= 6.5f -> Color(0xFF9E9E9E) // 灰色 - 普通IP
                                    else -> Color(0xFF757575) // 深灰色 - 小众IP
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // 评分
                            Text(
                                text = "评分${String.format("%.1f", ip.originalRating)}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            // 加成
                            Text(
                                text = "+${(ip.calculateIPBonus() * 100).toInt()}%销量",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (selectedIP?.id == ip.id) {
                        Text(
                            text = "✓",
                            color = Color(0xFF10B981),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * 游戏确认步骤 - 显示所有选择的信息，包括IP
 */
@Composable
fun GameConfirmationStepWithIP(
    gameName: String,
    theme: GameTheme?,
    platforms: List<Platform>,
    businessModel: BusinessModel?,
    selectedIP: com.example.yjcy.data.GameIP?
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
                
                // IP信息
                if (selectedIP != null) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "使用IP: ${selectedIP.name}",
                        color = Color(0xFF10B981),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "IP等级: ${selectedIP.getIPLevel()}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "销量加成: +${(selectedIP.calculateIPBonus() * 100).toInt()}%",
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "原创游戏 (不使用IP)",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
                
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                val developmentCost = platforms.sumOf { it.developmentCost }
                Text(
                    text = "开发费用: ${formatMoneyWithDecimals(developmentCost.toDouble())}",
                    color = Color(0xFFF59E0B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 10分游戏开发攻略对话框
 */
@Composable
fun GameTenPointStrategyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Text(
                text = "🎯 10分游戏攻略",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 详细步骤说明
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🚀 达成步骤",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 步骤1
                            StrategyDetailItem(
                                number = "1",
                                title = "规划游戏类型与平台",
                                detail = "任何游戏类型（单机或网游）和任何平台组合都不会影响评分。选择适合你团队配置和开发资源的类型即可，评分主要取决于团队技能和配置。"
                            )
                            
                            // 步骤2
                            StrategyDetailItem(
                                number = "2",
                                title = "招募4个不同职位的员工",
                                detail = "必须包含：程序员（开发技能）、策划师（设计技能）、美术师（美术技能）、音效师（音效技能）。凑齐这4个职位可以获得团队协作加成（+1.2分）。"
                            )
                            
                            // 步骤3
                            StrategyDetailItem(
                                number = "3",
                                title = "培养所有员工到5级",
                                detail = "通过分配员工到项目中工作来提升技能等级。当4个员工都达到5级时，技能总分可达3.4分（每个5级员工贡献约0.85分）。同时培养更多员工可以接近技能评分上限4.5分。"
                            )
                            
                            // 步骤4
                            StrategyDetailItem(
                                number = "4",
                                title = "选择匹配的游戏主题",
                                detail = "开发时选择与核心职位（程序员、策划师、美术师）技能匹配的主题。如果这3个核心职位的平均等级高，可以获得最高+1.0分的主题匹配加成。"
                            )
                            
                            // 步骤5
                            StrategyDetailItem(
                                number = "5",
                                title = "保持团队技能平衡",
                                detail = "确保4个员工的技能等级差距不要太大。如果技能等级较为均衡，可以获得最高+0.5分的平衡性加成。"
                            )
                            
                            // 步骤6
                            StrategyDetailItem(
                                number = "6",
                                title = "培养精英团队",
                                detail = "确保至少80%的员工（即4个员工中至少3个）达到4级以上，可以获得最高+0.5分的精英加成。如果全员5级，则100%满足条件。"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "知道了",
                    color = Color(0xFF10B981),
                    fontSize = 14.sp
                )
            }
        }
    )
}

/**
 * 详细攻略条目组件
 */
@Composable
private fun StrategyDetailItem(
    number: String,
    title: String,
    detail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 步骤编号
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detail,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}