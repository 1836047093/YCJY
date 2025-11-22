// 战队管理解锁功能 - 快速集成示例
// 将以下代码添加到 GameScreen 中

// ============================================
// 1. 在 GameScreen 的状态变量部分添加
// ============================================

@Composable
fun GameScreen(
    navController: NavController,
    companyName: String,
    founderName: String,
    selectedLogo: String,
    founderProfession: String,
    loadedSaveData: SaveData? = null
) {
    val context = LocalContext.current
    
    // 初始化存档数据
    var saveData by remember { 
        mutableStateOf(
            loadedSaveData ?: SaveData(
                companyName = companyName,
                companyLogo = selectedLogo,
                founderName = founderName,
                founderProfession = FounderProfession.valueOf(founderProfession),
                money = 3000000L,
                // ... 其他初始化字段
                esportsTeamUnlocked = false // 默认未解锁
            )
        )
    }
    
    // ... 其他状态变量
    
    // ============================================
    // 2. 在导航到战队管理的地方（例如按钮点击）
    // ============================================
    
    // 示例：在某个菜单或按钮中
    Button(
        onClick = {
            // 导航到战队管理
            showTeamManagement = true
        }
    ) {
        Text("⚽ 战队管理")
    }
    
    // ============================================
    // 3. 显示战队管理界面
    // ============================================
    
    if (showTeamManagement) {
        TeamManagementScreen(
            onNavigateBack = { 
                showTeamManagement = false 
            },
            teamLogoConfig = saveData.teamLogo,
            onUpdateTeamLogo = { newConfig ->
                saveData = saveData.copy(teamLogo = newConfig)
            },
            // TopInfoBar 参数
            money = saveData.money,
            fans = saveData.fans,
            year = saveData.currentYear,
            month = saveData.currentMonth,
            day = saveData.currentDay,
            gameSpeed = gameSpeed,
            onSpeedChange = { newSpeed -> 
                gameSpeed = newSpeed 
            },
            onPauseToggle = { 
                isPaused = !isPaused 
            },
            isPaused = isPaused,
            onSettingsClick = { 
                showInGameSettings = true 
            },
            isSupporterUnlocked = saveData.isSupporterUnlocked,
            onShowFeatureLockedDialog = {
                showFeatureLockedDialog = true
            },
            // 战队解锁相关参数（新增）
            isTeamUnlocked = saveData.esportsTeamUnlocked,
            onUnlockTeam = { teamName, logoConfig ->
                val unlockCost = 100_000_000L // 1亿
                
                // 检查资金是否足够
                if (saveData.money >= unlockCost) {
                    // 扣除资金
                    saveData = saveData.copy(
                        money = saveData.money - unlockCost,
                        esportsTeamUnlocked = true,
                        teamLogo = logoConfig.copy(teamName = teamName)
                    )
                    
                    // 记录资金流水（如果有资金流水系统）
                    moneyLogs = moneyLogs + MoneyLog(
                        date = GameDate(
                            saveData.currentYear,
                            saveData.currentMonth,
                            saveData.currentDay
                        ),
                        amount = -unlockCost,
                        reason = "解锁战队管理",
                        category = "战队"
                    )
                    
                    // 显示成功提示
                    Toast.makeText(
                        context,
                        "✅ 战队管理已解锁！欢迎来到电竞世界！",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // 可选：触发成就
                    // achievementManager.unlock("UNLOCK_ESPORTS")
                } else {
                    // 资金不足（理论上不会到这里，因为对话框已经检查过）
                    Toast.makeText(
                        context,
                        "⚠️ 资金不足，无法解锁战队管理",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

// ============================================
// 4. 完整的集成示例（包含所有必要的状态）
// ============================================

@Composable
fun GameScreenWithTeamManagement(
    navController: NavController,
    companyName: String,
    founderName: String,
    selectedLogo: String,
    founderProfession: String,
    loadedSaveData: SaveData? = null
) {
    val context = LocalContext.current
    
    // 存档数据
    var saveData by remember { 
        mutableStateOf(
            loadedSaveData ?: SaveData(
                companyName = companyName,
                companyLogo = selectedLogo,
                founderName = founderName,
                founderProfession = FounderProfession.valueOf(founderProfession),
                esportsTeamUnlocked = false
            )
        )
    }
    
    // UI状态
    var showTeamManagement by remember { mutableStateOf(false) }
    var gameSpeed by remember { mutableIntStateOf(1) }
    var isPaused by remember { mutableStateOf(false) }
    var showInGameSettings by remember { mutableStateOf(false) }
    var showFeatureLockedDialog by remember { mutableStateOf(false) }
    
    // 资金流水（如果有）
    var moneyLogs by remember { mutableStateOf(emptyList<MoneyLog>()) }
    
    // 主界面
    Box(modifier = Modifier.fillMaxSize()) {
        // ... 游戏主界面内容
        
        // 战队管理按钮（示例）
        Button(
            onClick = { showTeamManagement = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("⚽ 战队管理")
        }
    }
    
    // 战队管理界面
    if (showTeamManagement) {
        TeamManagementScreen(
            onNavigateBack = { showTeamManagement = false },
            teamLogoConfig = saveData.teamLogo,
            onUpdateTeamLogo = { newConfig ->
                saveData = saveData.copy(teamLogo = newConfig)
            },
            money = saveData.money,
            fans = saveData.fans,
            year = saveData.currentYear,
            month = saveData.currentMonth,
            day = saveData.currentDay,
            gameSpeed = gameSpeed,
            onSpeedChange = { gameSpeed = it },
            onPauseToggle = { isPaused = !isPaused },
            isPaused = isPaused,
            onSettingsClick = { showInGameSettings = true },
            isSupporterUnlocked = saveData.isSupporterUnlocked,
            onShowFeatureLockedDialog = { showFeatureLockedDialog = true },
            isTeamUnlocked = saveData.esportsTeamUnlocked,
            onUnlockTeam = { teamName, logoConfig ->
                val unlockCost = 100_000_000L
                if (saveData.money >= unlockCost) {
                    saveData = saveData.copy(
                        money = saveData.money - unlockCost,
                        esportsTeamUnlocked = true,
                        teamLogo = logoConfig.copy(teamName = teamName)
                    )
                    
                    moneyLogs = moneyLogs + MoneyLog(
                        date = GameDate(
                            saveData.currentYear,
                            saveData.currentMonth,
                            saveData.currentDay
                        ),
                        amount = -unlockCost,
                        reason = "解锁战队管理",
                        category = "战队"
                    )
                    
                    Toast.makeText(
                        context,
                        "✅ 战队管理已解锁！",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
}

// ============================================
// 5. MoneyLog 数据类（如果还没有）
// ============================================

data class MoneyLog(
    val date: GameDate,
    val amount: Long,
    val reason: String,
    val category: String
)
