package com.example.yjcy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.Choreographer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yjcy.data.Achievement
import com.example.yjcy.data.AchievementCategory
import com.example.yjcy.data.Achievements
import com.example.yjcy.data.AwardNomination
import com.example.yjcy.data.AwardRecord
import com.example.yjcy.data.AwardReward
import com.example.yjcy.data.ChatMessage
import com.example.yjcy.data.CompanyReputation
import com.example.yjcy.data.CompetitorCompany
import com.example.yjcy.data.CompetitorManager
import com.example.yjcy.data.CompetitorNews
import com.example.yjcy.data.Subsidiary
import com.example.yjcy.data.SubsidiaryManager
import com.example.yjcy.data.Complaint
import com.example.yjcy.data.ComplaintStatus
import com.example.yjcy.data.DevelopmentPhase
import com.example.yjcy.data.Employee
import com.example.yjcy.data.EsportsTournament
import com.example.yjcy.data.Founder
import com.example.yjcy.data.FounderProfession
import com.example.yjcy.data.GVAManager
import com.example.yjcy.data.Game
import com.example.yjcy.data.GameDate
import com.example.yjcy.data.GameIP
import com.example.yjcy.data.GameRatingCalculator
import com.example.yjcy.data.GameReleaseStatus
import com.example.yjcy.data.GameRevenue
import com.example.yjcy.data.GameUpdate
import com.example.yjcy.data.MessageSender
import com.example.yjcy.data.MonetizationConfig
import com.example.yjcy.data.MonetizationItem
import com.example.yjcy.data.NewsType
import com.example.yjcy.data.RevenueManager
import com.example.yjcy.data.SaveData
import com.example.yjcy.data.SecretaryReplyManager
import com.example.yjcy.data.TournamentManager
import com.example.yjcy.data.TournamentStatus
import com.example.yjcy.data.TutorialId
import com.example.yjcy.data.UnlockedAchievement
import com.example.yjcy.data.getRecommendedPrice
import com.example.yjcy.data.getUpdateContentName
import com.example.yjcy.managers.AchievementManager
import com.example.yjcy.service.CustomerServiceManager
import com.example.yjcy.service.JobPostingService
import com.example.yjcy.taptap.TapLoginManager
import com.example.yjcy.taptap.TapUpdateManager
import com.example.yjcy.ui.AchievementPopupQueue
import com.example.yjcy.ui.BadgeBox
import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.ui.ChallengeCompleteDialog
import com.example.yjcy.ui.CompetitorContent
import com.example.yjcy.ui.EmployeeManagementContent
import com.example.yjcy.ui.GVAAwardDialog
import com.example.yjcy.ui.GVAScreen
import com.example.yjcy.ui.GameRatingDialog
import com.example.yjcy.ui.GameReleaseDialog
import com.example.yjcy.ui.ProjectDisplayType
import com.example.yjcy.ui.ProjectManagementWrapper
import com.example.yjcy.ui.SalaryRequestDialog
import com.example.yjcy.ui.SecretaryChatDialog
import com.example.yjcy.ui.SecretaryChatScreen
import com.example.yjcy.ui.ServerManagementContent
import com.example.yjcy.ui.SubsidiaryManagementScreen
import com.example.yjcy.ui.TournamentResultDialog
import com.example.yjcy.ui.TournamentScreen
import com.example.yjcy.ui.TutorialDialog
import com.example.yjcy.ui.TutorialTrigger
import com.example.yjcy.ui.YearEndBonusDialog
import com.example.yjcy.ui.YearEndStatistics
import com.example.yjcy.ui.calculatePlayerMarketValue
import com.example.yjcy.ui.rememberTutorialState
import com.example.yjcy.ui.taptap.TapLoginViewModel
import com.example.yjcy.ui.theme.YjcyTheme
import com.example.yjcy.utils.CommentGenerator
import com.example.yjcy.utils.LeanCloudRedeemCodeManager
import com.example.yjcy.utils.RedeemCodeManager
import com.example.yjcy.utils.SensitiveWordFilter
import com.example.yjcy.utils.SignatureHelper
import com.example.yjcy.utils.formatMoney
import com.example.yjcy.utils.formatMoneyWithDecimals
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random


// 性能优化：调试日志开关（正式环境应设为false）
private const val ENABLE_VERBOSE_GAME_LOGS = false

// FPS日志开关（设置为true后会在Logcat中输出FPS信息）
private const val ENABLE_FPS_LOG = true

// 全局变量存储当前加载的存档数据
var currentLoadedSaveData: SaveData? = null

/**
 * 安全地增加资金，防止溢出
 * @param current 当前资金
 * @param amount 增加金额（可为负数，表示减少）
 * @return 操作后的资金（已处理溢出）
 */
private fun safeAddMoney(current: Long, amount: Long): Long {
    // 如果当前值为负数且金额也为负数，可能导致异常，重置为0
    if (current < 0 && amount < 0) {
        Log.w("MainActivity", "⚠️ 检测到资金为负数($current)且继续减少($amount)，重置为0")
        return 0L
    }
    
    // 检查累加是否会溢出
    val maxValue = Long.MAX_VALUE / 2
    
    return when {
        // 当前值已达到上限，不允许再增加
        current >= maxValue && amount > 0 -> {
            Log.w("MainActivity", "⚠️ 资金已达到上限($current)，不再增加")
            current
        }
        // 累加后会超过上限
        current > 0 && amount > 0 && current + amount > maxValue -> {
            Log.w("MainActivity", "⚠️ 累加后会超过上限($current + $amount)，限制为上限")
            maxValue
        }
        // 正常累加
        else -> {
            val result = current + amount
            // 如果结果为负数且减少金额过大，限制为0（允许负债，但限制过度负债）
            if (result < -10_000_000_000L) { // 负债超过100亿时限制
                Log.w("MainActivity", "⚠️ 资金负债过大($result)，限制为-100亿")
                -10_000_000_000L
            } else {
                result
            }
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "MainActivity onCreate 开始")
        
        // 打印当前签名信息（用于TapTap SDK配置）
        SignatureHelper.logAppInfo(this)
        
        // 初始化RevenueManager以支持数据持久化
        RevenueManager.initialize(this)
        
        // 数据迁移：修复旧存档中使用系统时间的 DailySales 日期（针对旧存档的一次性迁移）
        RevenueManager.migrateDateToGameTime()
        
        // 启动时检查并修复服务器扣费（针对旧存档的一次性迁移）
        Log.d("MainActivity", "检查服务器扣费状态...")
        
        // 先启用边到边显示
        enableEdgeToEdge()
        
        // 设置120Hz高刷新率
        enableHighRefreshRate()
        
        // 然后设置全屏显示和隐藏系统导航栏
        enableFullScreenDisplay()
        
        // 检查用户是否已同意隐私政策
        val sharedPreferences = getSharedPreferences("privacy_settings", MODE_PRIVATE)
        val hasAgreedPrivacy = sharedPreferences.getBoolean("privacy_agreed", false)
        
        // 如果用户已同意隐私政策，则初始化SDK并检查更新
        if (hasAgreedPrivacy) {
            Log.d("MainActivity", "✅ 用户已同意隐私政策，开始初始化TapSDK")
            (application as? YjcyApplication)?.initTapSDKIfNeeded()
            
            // 延迟500ms后检查更新，确保SDK完全初始化
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("MainActivity", "开始检查TapTap更新...")
                TapUpdateManager.checkForceUpdate()
            }, 500)
        } else {
            Log.d("MainActivity", "⚠️ 用户未同意隐私政策，等待用户同意后再初始化SDK")
        }
        
        setContent {
            YjcyTheme {
                val navController = rememberNavController()
                
                // 使用外部已创建的sharedPreferences
                var showPrivacyDialog by remember { mutableStateOf(!hasAgreedPrivacy) }
                
                // TapTap登录状态检查（Activity重启后会重新检查）
                // 初始为false，用户同意隐私政策后再检查真实状态
                var isTapTapLoggedIn by remember { mutableStateOf(false) }
                
                // 检查登录状态：在隐私协议同意后且SDK初始化完成后
                LaunchedEffect(showPrivacyDialog) {
                    // 只有在隐私协议已同意时才执行
                    if (!showPrivacyDialog) {
                        // 等待SDK初始化完成
                        var retryCount = 0
                        while (retryCount < 20 && !YjcyApplication.isSdkInitialized()) {
                            delay(200) // 每200ms检查一次
                            retryCount++
                        }
                        
                        if (YjcyApplication.isSdkInitialized()) {
                            // SDK已初始化，再等待一小段时间确保完全就绪
                            delay(500)
                            
                            // 检查是否已经登录
                            try {
                                val account = TapLoginManager.getCurrentAccount()
                                if (account != null) {
                                    Log.d("MainActivity", "✅ 检测到已登录的账号: ${account.name}, unionId=${account.unionId}")
                                    isTapTapLoggedIn = true
                                } else {
                                    Log.d("MainActivity", "ℹ️ 未检测到已登录的账号，需要重新登录")
                                }
                            } catch (e: Exception) {
                                Log.w("MainActivity", "检查登录状态失败: ${e.message}")
                                // 如果检查失败，保持未登录状态，让用户重新登录
                            }
                        } else {
                            Log.w("MainActivity", "⚠️ SDK初始化超时，可能需要重新登录")
                        }
                    }
                }
                
                // Privacy Policy Dialog
                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(
                        onAgree = {
                            sharedPreferences.edit().apply {
                                putBoolean("privacy_agreed", true)
                                apply()
                            }
                            showPrivacyDialog = false
                            
                            // 用户同意隐私政策后，立即初始化TapSDK
                            (application as? YjcyApplication)?.initTapSDKIfNeeded()
                            
                            // 延迟更长时间，确保SDK完全初始化后再显示登录界面
                            // 避免合规认证时出现"当前应用还未初始化"的错误
                            Handler(Looper.getMainLooper()).postDelayed({
                                Log.d("MainActivity", "✅ SDK初始化延迟完成（1秒），准备检查登录状态")
                            }, 1000)
                        },
                        onReject = {
                            // 用户拒绝隐私政策，退出游戏
                            finish()
                        }
                    )
                }
                
                // 强制TapTap登录界面（隐私协议同意后且未登录时显示）
                if (!showPrivacyDialog && !isTapTapLoggedIn) {
                    ForcedTapLoginScreen(
                        onLoginSuccess = {
                            isTapTapLoggedIn = true
                        }
                    )
                }
                
                // 只有在隐私协议同意且TapTap登录后才显示导航
                if (!showPrivacyDialog && isTapTapLoggedIn) {
                    NavHost(
                        navController = navController,
                        startDestination = "main_menu"
                    ) {
                    composable("main_menu") {
                        MainMenuScreen(navController)
                    }
                    composable("game_setup") {
                        GameSetupScreen(navController)
                    }
                    composable("game/{companyName}/{founderName}/{selectedLogo}/{founderProfession}") { backStackEntry ->
                        val companyName = backStackEntry.arguments?.getString("companyName") ?: "我的游戏公司"
                        val founderName = backStackEntry.arguments?.getString("founderName") ?: "创始人"
                        val selectedLogo = backStackEntry.arguments?.getString("selectedLogo") ?: "🎮"
                        val founderProfession = backStackEntry.arguments?.getString("founderProfession") ?: "PROGRAMMER"
                        
                        // 保存当前存档数据的快照，避免被清空影响
                        val saveDataSnapshot = remember { currentLoadedSaveData }
                        
                        // 首次进入时清除全局存档变量
                        DisposableEffect(Unit) {
                            onDispose {
                                currentLoadedSaveData = null
                            }
                        }
                        
                        GameScreen(navController, companyName, founderName, selectedLogo, founderProfession, saveDataSnapshot)
                    }
                    composable("continue") {
                        ContinueScreen(navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController)
                    }
                    composable("achievement") {
                        // 从所有存档中加载已解锁的成就
                        val context = LocalContext.current
                        val saveManager = remember { SaveManager(context) }
                        var allUnlockedAchievements by remember { mutableStateOf(emptyList<UnlockedAchievement>()) }
                        var maxMoney by remember { mutableLongStateOf(0L) }
                        var isLoading by remember { mutableStateOf(true) }
                        
                        LaunchedEffect(Unit) {
                            // 异步加载所有存档
                            val saves = saveManager.getAllSavesAsync()
                            
                            // 合并所有存档的已解锁成就（去重）
                            val achievementMap = mutableMapOf<String, UnlockedAchievement>()
                            var highestMoney = 0L
                            
                            saves.values.filterNotNull().forEach { saveData ->
                                // 记录最高资金（用于计算进度）
                                if (saveData.money > highestMoney) {
                                    highestMoney = saveData.money
                                }
                                
                                // 合并成就（保留最早的解锁时间）
                                saveData.unlockedAchievements.forEach { achievement ->
                                    val existing = achievementMap[achievement.achievementId]
                                    if (existing == null || achievement.unlockTime < existing.unlockTime) {
                                        achievementMap[achievement.achievementId] = achievement
                                    }
                                }
                            }
                            
                            allUnlockedAchievements = achievementMap.values.toList()
                            maxMoney = highestMoney
                            isLoading = false
                        }
                        
                        if (isLoading) {
                            // 加载中显示
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF1A237E),
                                                Color(0xFF4A148C)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        } else {
                            // 创建临时SaveData用于显示成就（使用合并后的成就列表）
                            val tempSaveData = SaveData(
                                money = maxMoney,
                                unlockedAchievements = allUnlockedAchievements
                            )
                            AchievementScreen(
                                navController = navController,
                                saveData = tempSaveData,
                                revenueData = emptyMap()
                            )
                        }
                    }
                    composable("leaderboard") {
                    }
                    composable("in_game_settings") {
                        InGameSettingsScreen(navController)
                    }
                    composable("secretary_chat") {
                        SecretaryChatScreen(navController)
                    }
                    }
                }
            }
        }
    }
    
    /**
     * 启用120Hz高刷新率
     */
    private fun enableHighRefreshRate() {
        try {
            // minSdk是24，所以总是使用Display.Mode API
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            // 使用新的API替代过时的defaultDisplay
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay ?: return
            
            // 获取支持的刷新率模式列表
            val supportedModes = display.supportedModes
            
            if (supportedModes != null && supportedModes.isNotEmpty()) {
                // 优先查找120Hz或最接近120Hz的模式
                var bestMode: Display.Mode? = null
                var bestRefreshRate = 0f
                var closestTo120 = Float.MAX_VALUE
                
                for (mode in supportedModes) {
                    val refreshRate = mode.refreshRate
                    
                    // 优先选择120Hz
                    if (refreshRate == 120f) {
                        bestMode = mode
                        break
                    }
                    
                    // 如果没找到120Hz，选择最接近120Hz且不超过120Hz的
                    if (refreshRate <= 120f && refreshRate > bestRefreshRate) {
                        bestRefreshRate = refreshRate
                        bestMode = mode
                    }
                    
                    // 记录最接近120Hz的模式（可能超过120Hz）
                    val diff = abs(refreshRate - 120f)
                    if (diff < closestTo120) {
                        closestTo120 = diff
                        if (bestMode == null || bestRefreshRate < 60f) {
                            bestMode = mode
                            bestRefreshRate = refreshRate
                        }
                    }
                }
                
                if (bestMode != null) {
                    val layoutParams = window.attributes
                    layoutParams.preferredDisplayModeId = bestMode.modeId
                    window.attributes = layoutParams
                    Log.d("MainActivity", "✅ 已设置刷新率: ${bestMode.refreshRate}Hz (模式ID: ${bestMode.modeId})")
                    Log.d("MainActivity", "📊 支持的刷新率: ${supportedModes.map { it.refreshRate }.joinToString(", ")}Hz")
                } else {
                    Log.w("MainActivity", "⚠️ 未找到支持的刷新率模式")
                }
            } else {
                Log.w("MainActivity", "⚠️ 无法获取支持的刷新率模式列表")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "设置高刷新率失败: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    private fun enableFullScreenDisplay() {
        try {
            // 使用 WindowCompat API，与 enableEdgeToEdge 兼容
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.let { controller ->
                // 隐藏状态栏和导航栏
                controller.hide(WindowInsetsCompat.Type.systemBars())
                // 设置沉浸式模式，滑动时临时显示系统栏
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "设置全屏显示失败: ${e.message}", e)
        }
    }
}

/**
 * 强制TapTap登录界面
 * 玩家必须登录后才能进入游戏
 */
@Composable
fun ForcedTapLoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: TapLoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var showMessage by remember { mutableStateOf(null as String?) }
    
    // 性能优化：移除标题发光动画，使用静态值
    val titleGlow = 0.8f  // 固定值，不再使用动画
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C29),  // 深紫蓝色
                        Color(0xFF1A0A2E),  // 深紫色
                        Color(0xFF16213E),  // 深蓝色
                        Color(0xFF0F0C29)   // 回到深紫蓝色
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // 性能优化：移除背景动画
        // GameStyleBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo展示 - 带霓虹发光效果
            Text(
                text = "🎮 游创纪元",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF9B51E0).copy(alpha = titleGlow * 0.9f),
                        offset = Offset(0f, 0f),
                        blurRadius = 30f * titleGlow
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "打造你的游戏帝国",
                fontSize = 18.sp,
                color = Color(0xFFA0A0FF),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF667eea).copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 15f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // TapTap登录卡片 - 游戏风格半透明设计
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xCC1A1A2E)  // 深紫色半透明
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎮 TapTap 登录",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "请先登录TapTap账号",
                        fontSize = 16.sp,
                        color = Color(0xFFA0A0FF),  // 浅紫色
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 登录按钮 - 霓虹效果
                    Button(
                        onClick = {
                            activity?.let { act ->
                                viewModel.login(act) { success, message ->
                                    showMessage = message
                                    if (success) {
                                        onLoginSuccess()
                                    }
                                }
                            } ?: run {
                                showMessage = "无法获取Activity"
                            }
                        },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        )
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "🚀 使用 TapTap 登录",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // 自动隐藏消息（3秒后）
            LaunchedEffect(showMessage) {
                showMessage?.let {
                    delay(3000) // 3秒延迟
                    showMessage = null
                }
            }
            
            // 错误提示
            showMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("成功")) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                ) {
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun InGameSettingsScreen(navController: NavController) {
    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F2937),
                        Color(0xFF111827)
                    )
                )
            )
    ) {
        // FPS监测（左上角）
        FpsMonitor(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        
        // Settings content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "游戏设置",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Settings options
            SettingsOption(
                title = "音效",
                isEnabled = soundEnabled,
                onToggle = { soundEnabled = it }
            )
            
            SettingsOption(
                title = "音乐",
                isEnabled = musicEnabled,
                onToggle = { musicEnabled = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Text(
                    text = "返回游戏",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SettingsOption(
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF10B981),
                checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}
@Composable
fun MainMenuScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val saveManager = remember { SaveManager(context) }
    
    // 退出应用确认对话框状态
    var showExitDialog by remember { mutableStateOf(false) }
    
    // QQ群提示对话框状态
    var showQQGroupDialog by remember { mutableStateOf(false) }
    @Suppress("SpellCheckingInspection")
    var dontShowToday by remember { mutableStateOf(false) }
    var pendingNavigationRoute by remember { mutableStateOf<String?>(null) }
    
    // 检查今天是否已经显示过对话框
    fun shouldShowQQGroupDialog(): Boolean {
        val prefs = context.getSharedPreferences("qq_group_dialog", Context.MODE_PRIVATE)
        val lastDismissDate = prefs.getString("last_dismiss_date", null)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        return lastDismissDate != today
    }
    
    // 保存"今日不再弹出"的状态
    @Suppress("SpellCheckingInspection")
    fun saveDontShowToday(checked: Boolean) {
        if (checked) {
            val prefs = context.getSharedPreferences("qq_group_dialog", Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            prefs.edit {
                putString("last_dismiss_date", today)
            }
        }
    }
    
    // 处理导航（在显示对话框后）
    fun handleNavigation(route: String) {
        if (shouldShowQQGroupDialog()) {
            pendingNavigationRoute = route
            showQQGroupDialog = true
        } else {
            navController.navigate(route)
        }
    }
    
    // 加载存档数据（用于显示最近游戏）
    var recentSaves by remember { mutableStateOf(emptyMap<Int, SaveData?>()) }
    
    LaunchedEffect(Unit) {
        recentSaves = saveManager.getAllSavesAsync()
    }
    
    // Logo和标题动画
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    
    val logoGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_glow"
    )
    
    val titleOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_offset"
    )
    
    // 拦截返回键，显示退出应用确认对话框
    BackHandler {
        showExitDialog = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // 深蓝黑色
                        Color(0xFF1E293B), // 深灰蓝
                        Color(0xFF334155)  // 中灰蓝
                    ),
                    center = Offset(0f, 0f),
                    radius = 2000f
                )
            )
    ) {
        // 现代化的背景动画
        ModernGameBackground()
        
        // 左上角版本号和FPS监测
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            // FPS监测
            FpsMonitor()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 版本号
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "V${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        // 主要内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // 游戏Logo和标题区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 游戏图标（带发光效果）
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFF3B82F6).copy(alpha = logoGlow)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.8f),
                                    Color(0xFF1E40AF).copy(alpha = 0.6f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 64.sp,
                        modifier = Modifier.scale(1f + logoGlow * 0.1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 游戏标题（带动态效果）
                Text(
                    text = "游创纪元",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF3B82F6).copy(alpha = 0.8f),
                            offset = Offset(titleOffset, titleOffset),
                            blurRadius = 20f
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 副标题
                Text(
                    text = "GAME DEV SIMULATOR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA).copy(alpha = 0.9f),
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "打造你的游戏帝国",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 主要功能卡片网格
            val menuItems = listOf(
                MenuItem(
                    icon = "🚀",
                    title = "开始新游戏",
                    description = "创建新的游戏公司",
                    gradient = listOf(Color(0xFF3B82F6), Color(0xFF1E40AF)),
                    onClick = { handleNavigation("game_setup") }
                ),
                MenuItem(
                    icon = "📂",
                    title = "继续游戏",
                    description = "加载已保存的存档",
                    gradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
                    onClick = { handleNavigation("continue") },
                    badge = if (recentSaves.values.any { it != null }) "NEW" else null
                ),
                MenuItem(
                    icon = "🏆",
                    title = "成就系统",
                    description = "查看解锁的成就",
                    gradient = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                    onClick = { navController.navigate("achievement") }
                ),
                MenuItem(
                    icon = "⚙️",
                    title = "游戏设置",
                    description = "调整游戏参数",
                    gradient = listOf(Color(0xFF6B7280), Color(0xFF4B5563)),
                    onClick = { navController.navigate("settings") }
                )
            )
            
            // 响应式网格布局
            val columns = 2
            val rows = (menuItems.size + columns - 1) / columns
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(rows) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(columns) { colIndex ->
                            val index = rowIndex * columns + colIndex
                            if (index < menuItems.size) {
                                ModernMenuCard(
                                    item = menuItems[index],
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 健康游戏忠告（底部）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "抵制不良游戏，拒绝盗版游戏。\n注意自我保护，谨防受骗上当。\n适度游戏益脑，沉迷游戏伤身。\n合理安排时间，享受健康生活。",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }
        
        // 退出应用确认对话框
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = {
                    showExitDialog = false
                },
                title = {
                    Text(
                        text = "⚠️ 退出游戏",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = "确定要退出游戏吗？",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp),
                confirmButton = {
                    Button(
                        onClick = {
                            activity?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text("确认退出", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showExitDialog = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("取消")
                    }
                }
            )
        }
        
        // QQ群提示对话框
        if (showQQGroupDialog) {
            QQGroupDialog(
                context = context,
                onDismiss = {
                    showQQGroupDialog = false
                    dontShowToday = false
                    // 取消时清除待处理的导航
                    pendingNavigationRoute = null
                },
                onConfirm = {
                    saveDontShowToday(dontShowToday)
                    showQQGroupDialog = false
                    dontShowToday = false
                    // 执行待处理的导航
                    pendingNavigationRoute?.let { route ->
                        navController.navigate(route)
                        pendingNavigationRoute = null
                    }
                },
                dontShowToday = dontShowToday,
                onDontShowTodayChange = { dontShowToday = it }
            )
        }
    }
}

// QQ群提示对话框组件
@Composable
fun QQGroupDialog(
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dontShowToday: Boolean,
    onDontShowTodayChange: (Boolean) -> Unit
) {
    // 一键加群功能
    fun joinQQGroup() {
        try {
            // QQ群号
            val qqGroupNumber = "851082168"
            
            // 检查QQ是否安装（直接检查包名）
            fun isQQInstalled(): Boolean {
                return try {
                    @Suppress("SpellCheckingInspection")
                    context.packageManager.getPackageInfo("com.tencent.mobileqq", 0)
                    true
                } catch (_: Exception) {
                    false
                }
            }
            
            if (!isQQInstalled()) {
                Toast.makeText(context, "未检测到QQ应用，请先安装QQ后搜索群号：$qqGroupNumber", Toast.LENGTH_LONG).show()
                return
            }
            
            // 方式1: 尝试使用QQ的URL Scheme打开加群页面（推荐方式）
            val groupIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                @Suppress("SpellCheckingInspection")
                data = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$qqGroupNumber&card_type=group&source=external".toUri()
            }
            
            // 检查是否有应用可以处理这个Intent
            val resolveInfo = groupIntent.resolveActivity(context.packageManager)
            if (resolveInfo != null) {
                try {
                    context.startActivity(groupIntent)
                    return
                } catch (_: Exception) {
                    // 如果启动失败，尝试其他方式
                }
            }
            
            // 方式2: 尝试使用QQ的通用Scheme打开QQ应用
            val qqIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = "mqq://".toUri()
            }
            
            if (qqIntent.resolveActivity(context.packageManager) != null) {
                try {
                    context.startActivity(qqIntent)
                    Toast.makeText(context, "请搜索QQ群号：$qqGroupNumber", Toast.LENGTH_LONG).show()
                    return
                } catch (_: Exception) {
                    // 如果启动失败，使用包名直接启动
                }
            }
            
            // 方式3: 使用包名直接启动QQ
            try {
                @Suppress("SpellCheckingInspection")
                val packageIntent = context.packageManager.getLaunchIntentForPackage("com.tencent.mobileqq")
                if (packageIntent != null) {
                    context.startActivity(packageIntent)
                    Toast.makeText(context, "请搜索QQ群号：$qqGroupNumber", Toast.LENGTH_LONG).show()
                    return
                }
            } catch (_: Exception) {
                // 如果启动失败，提示用户
            }
            
            // 如果所有方式都失败，提示用户手动搜索
            Toast.makeText(context, "打开QQ失败，请手动搜索QQ群号：$qqGroupNumber", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(context, "打开QQ失败，请手动搜索QQ群号：851082168", Toast.LENGTH_LONG).show()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📢 加入QQ群",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "欢迎大家加群，可以和各路玩家分享攻略，交流心得，还能获得神秘投资兑换码",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )
                
                // QQ群号显示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "QQ群号",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "851082168",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = { joinQQGroup() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("一键加群", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = dontShowToday,
                        onCheckedChange = onDontShowTodayChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF3B82F6),
                            uncheckedColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "今日不再弹出",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.clickable { onDontShowTodayChange(!dontShowToday) }
                    )
                }
            }
        },
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(20.dp),
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("知道了", color = Color.White, fontSize = 15.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Text("取消", fontSize = 15.sp)
            }
        }
    )
}

// 菜单项数据类
data class MenuItem(
    val icon: String,
    val title: String,
    val description: String,
    val gradient: List<Color>,
    val onClick: () -> Unit,
    val badge: String? = null
)

// 现代化的菜单卡片组件
@Composable
fun ModernMenuCard(
    item: MenuItem,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = 8f,
        animationSpec = tween(200),
        label = "card_elevation"
    )
    
    Card(
        modifier = modifier
            .height(140.dp)
            .scale(scale)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = {
            item.onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = item.gradient.map { it.copy(alpha = 0.9f) }
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 图标和标题行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.icon,
                        fontSize = 32.sp
                    )
                    
                    // 徽章
                    item.badge?.let { badge ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // 标题和描述
                Column {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 现代化的游戏背景 - 性能优化版本（完全禁用动画）
@Composable
fun ModernGameBackground() {
    // 性能优化：完全移除所有Canvas绘制和动画，显著提升FPS
    // 背景使用静态渐变即可，无需额外绘制
}
// 性能优化：完全移除粒子背景动画
// @Composable
// fun ParticleBackground() {
//     // 已禁用以提升FPS性能
// }

data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color = Color.White
)

@Composable
fun GameMenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )
    
    // 橙色渐变背景，禁用时变灰
    val buttonGradient = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFF59E0B), // 橙色
                Color(0xFFEA580C)  // 深橙色
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Gray.copy(alpha = 0.5f),
                Color.Gray.copy(alpha = 0.4f)
            )
        )
    }
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .width(280.dp)
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = buttonGradient,
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun GameSetupScreen(navController: NavController) {
    var companyName by remember { mutableStateOf("") }
    var founderName by remember { mutableStateOf("") }
    var selectedLogo by remember { mutableStateOf("🎮") }
    var selectedProfession by remember { mutableStateOf(null as FounderProfession?) }
    var isCompanyNameValid by remember { mutableStateOf(true) }
    var companyNameError by remember { mutableStateOf("") }
    
    val logoOptions = listOf("🎮", "🏢", "💼", "🚀", "⭐", "🎯")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        // FPS监测（左上角）
        FpsMonitor(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🏢 创建游戏公司",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 公司名称输入
            Column {
                Text(
                    text = "公司名称（最多5个字符）",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { newValue ->
                        companyName = newValue
                        when {
                            newValue.isEmpty() -> {
                                isCompanyNameValid = true
                                companyNameError = ""
                            }
                            newValue.length > 5 -> {
                                isCompanyNameValid = false
                                companyNameError = "公司名最多5个字符"
                            }
                            !newValue.all { it.isLetterOrDigit() } -> {
                                isCompanyNameValid = false
                                companyNameError = "只能输入字符和数字"
                            }
                            SensitiveWordFilter.containsSensitiveCompanyName(newValue) -> {
                                isCompanyNameValid = false
                                companyNameError = "存在敏感词汇，请换个公司名"
                            }
                            else -> {
                                isCompanyNameValid = true
                                companyNameError = ""
                            }
                        }
                    },
                    isError = !isCompanyNameValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        errorBorderColor = Color.Red
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isCompanyNameValid && companyNameError.isNotEmpty()) {
                    Text(
                        text = companyNameError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // LOGO选择
            Column {
                Text(
                    text = "选择公司LOGO",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(logoOptions) { logo ->
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = if (selectedLogo == logo) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (selectedLogo == logo) Color.White else Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedLogo = logo },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = logo,
                                fontSize = 24.sp
                            )
                        }
                        }
                    }
                }
            
            // 创始人名字输入
            Column {
                Text(
                    text = "创始人姓名",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = founderName,
                    onValueChange = { founderName = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 创始人职业选择
            Column {
                Text(
                    text = "选择创始人职业",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(FounderProfession.entries.toList()) { profession: FounderProfession ->
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(80.dp)
                                .background(
                                    color = if (selectedProfession == profession) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (selectedProfession == profession) Color.White else Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedProfession = profession }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = profession.icon,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profession.displayName,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                selectedProfession?.let { profession ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "专属技能：${profession.specialtySkill}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 按钮组
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GameMenuButton(
                    text = "返回",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                )
                
                GameMenuButton(
                    text = "开始游戏",
                    onClick = {
                        if (companyName.isNotEmpty() && founderName.isNotEmpty() && selectedProfession != null && isCompanyNameValid) {
                            navController.navigate("game/$companyName/$founderName/$selectedLogo/${selectedProfession!!.name}")
                        }
                    },
                    enabled = companyName.isNotEmpty() && founderName.isNotEmpty() && selectedProfession != null && isCompanyNameValid,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
fun GameScreen(
    navController: NavController,
    initialCompanyName: String = "我的游戏公司",
    initialFounderName: String = "创始人",
    selectedLogo: String = "🎮",
    initialFounderProfession: String = "PROGRAMMER",
    saveData: SaveData? = null
) {
    // 调试：记录GameScreen创建
    val screenInstanceId = remember { System.currentTimeMillis() }
    Log.d("GameScreen", "🔵 GameScreen【实例 $screenInstanceId】被创建, saveData=${if (saveData != null) "非null(公司=${saveData.companyName})" else "null(新游戏)"}")
    
    // 获取 Activity 上下文，用于退出游戏
    val activity = LocalActivity.current!!
    
    // 游戏状态数据 - 如果有存档数据则使用存档数据，否则使用默认值
    // 修复：如果读取到的资金为负数（溢出），重置为默认值
    var money by remember { 
        mutableLongStateOf(
            if (saveData?.money != null && saveData.money < 0) {
                Log.w("MainActivity", "⚠️ 读取存档时发现资金为负数(${saveData.money})，重置为300万")
                3000000L
            } else {
                saveData?.money ?: 3000000L
            }
        ) 
    }
    var fans by remember { mutableLongStateOf(saveData?.fans ?: 0L) }
    var currentYear by remember { mutableIntStateOf(saveData?.currentYear ?: 1) }
    var currentMonth by remember { mutableIntStateOf(saveData?.currentMonth ?: 1) }
    var currentDay by remember { mutableIntStateOf(saveData?.currentDay ?: 1) }
    var gameSpeed by rememberSaveable { mutableIntStateOf(1) }  // 默认1倍速，使用rememberSaveable确保状态持久化
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isPaused by rememberSaveable { mutableStateOf(false) }  // 使用rememberSaveable确保暂停状态在切换标签时保持
    
    // 调试：记录状态变化
    LaunchedEffect(isPaused, gameSpeed, selectedTab) {
        Log.d("GameScreen", "📊 状态变化: isPaused=$isPaused, gameSpeed=$gameSpeed, selectedTab=$selectedTab")
    }
    var showTournamentMenu by remember { mutableStateOf(false) }
    var tournamentInitialTab by remember { mutableIntStateOf(0) }
    
    var showCompetitorMenu by remember { mutableStateOf(false) } // 竞争对手菜单（包含竞争对手和子公司）
    var showSubsidiaryManagement by remember { mutableStateOf(false) } // 子公司管理界面
    
    // 子公司资金不足对话框状态
    var showSubsidiaryBankruptDialog by remember { mutableStateOf(false) }
    var bankruptSubsidiary by remember { mutableStateOf<Subsidiary?>(null) }
    var injectionAmountInput by remember { mutableStateOf("") } // 注入金额输入
    
    // 上次月结算的年月（防止重复结算）
    var lastSettlementYear by remember { mutableIntStateOf(saveData?.currentYear ?: 1) }
    var lastSettlementMonth by remember { mutableIntStateOf(saveData?.currentMonth ?: 1) }
    
    // 上次自动宣传检查的日期（防止重复检查）
    var lastAutoPromotionCheckYear by remember { mutableIntStateOf(saveData?.currentYear ?: 1) }
    var lastAutoPromotionCheckMonth by remember { mutableIntStateOf(saveData?.currentMonth ?: 1) }
    var lastAutoPromotionCheckDay by remember { mutableIntStateOf(saveData?.currentDay ?: 1) }
    
    // 项目管理界面的显示类型状态（使用 remember 保持在内存中）
    var selectedProjectType by remember { mutableStateOf(ProjectDisplayType.DEVELOPING) }
    var companyName by remember { mutableStateOf(saveData?.companyName ?: initialCompanyName) }
    var founderName by remember { mutableStateOf(saveData?.founderName ?: initialFounderName) }
    var founderProfession by remember { mutableStateOf(saveData?.founderProfession ?: try { FounderProfession.valueOf(initialFounderProfession) } catch (_: IllegalArgumentException) { FounderProfession.PROGRAMMER }) }
    var games by remember { mutableStateOf(saveData?.games ?: emptyList()) }
    
    // 消息状态
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    
    // 游戏发售相关状态
    var showReleaseDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var pendingReleaseGame by remember { mutableStateOf(null as Game?) }
    var revenueRefreshTrigger by remember { mutableIntStateOf(0) } // 用于触发收益数据刷新
    var jobPostingRefreshTrigger by remember { mutableIntStateOf(0) } // 用于触发岗位应聘者数据刷新
    var pendingRatingGame by remember { mutableStateOf(null as Game?) }
    
    // 废弃游戏相关状态
    var showAbandonDialog by remember { mutableStateOf(false) }
    var pendingAbandonGame by remember { mutableStateOf(null as Game?) }
    
    // 退出游戏确认对话框状态
    var showExitDialog by remember { mutableStateOf(false) }
    
    // 破产对话框状态
    var showBankruptcyDialog by remember { mutableStateOf(false) }
    
    // 显示设置界面状态
    var showSettings by remember { mutableStateOf(false) }
    
    // 秘书聊天对话框状态
    var showSecretaryChat by remember { mutableStateOf(false) }
    
    // 秘书聊天记录状态（保存在GameScreen级别，对话框关闭后不会丢失）
    val chatMessages = remember { 
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.SECRETARY,
                content = SecretaryReplyManager.WELCOME_MESSAGE
            )
        )
    }
    
    // 员工状态管理 - 提升到GameScreen级别
    val allEmployees = remember { mutableStateListOf<Employee>() }
    
    // 获取协程作用域，用于在主线程安全更新
    val coroutineScope = rememberCoroutineScope()
    
    // 获取Context用于自动存档
    val context = LocalContext.current
    val saveManager = remember { SaveManager(context) }
    
    // 竞争对手数据状态
    var competitors by remember { mutableStateOf(saveData?.competitors ?: emptyList()) }
    var competitorNews by remember { mutableStateOf(saveData?.competitorNews ?: emptyList()) }
    var ownedIPs by remember { mutableStateOf(saveData?.ownedIPs ?: emptyList()) } // 拥有的游戏IP列表
    var subsidiaries by remember { mutableStateOf(saveData?.subsidiaries ?: emptyList()) } // 子公司列表（收购的竞争对手）
    
    // 客诉数据状态
    var complaints by remember { mutableStateOf(saveData?.complaints ?: emptyList()) }
    var autoProcessComplaints by remember { mutableStateOf(saveData?.autoProcessComplaints ?: false) }
    var autoPromotionThreshold by remember { mutableFloatStateOf(saveData?.autoPromotionThreshold ?: 0.5f) }
    
    // 自动审批涨薪状态
    var autoApproveSalaryIncrease by remember { mutableStateOf(saveData?.autoApproveSalaryIncrease ?: false) }
    
    // 获取当前登录的TapTap用户ID并检查账号是否已解锁GM模式
    val tapTapAccount = TapLoginManager.getCurrentAccount()
    val userId = tapTapAccount?.unionId ?: tapTapAccount?.openId
    
    // GM模式解锁状态（使用State存储异步结果）
    var isGMModeUnlockedByAccount by remember { mutableStateOf(false) }
    
    // GM模式状态（优先使用账号级别解锁状态，否则使用存档状态）
    var gmModeEnabled by remember { 
        mutableStateOf(saveData?.gmModeEnabled ?: false) 
    }
    
    // 异步检查GM模式解锁状态并迁移本地数据
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                // 检查云端GM模式状态
                isGMModeUnlockedByAccount = LeanCloudRedeemCodeManager.isGMUnlocked(userId)
                
                // 如果云端已解锁GM模式，自动启用
                if (isGMModeUnlockedByAccount && !gmModeEnabled) {
                    gmModeEnabled = true
                    Log.d("LeanCloud", "从云端恢复GM模式")
                }
            } catch (e: Exception) {
                Log.e("LeanCloud", "同步兑换码数据失败", e)
            }
        }
    }
    
    // 自动存档设置
    var autoSaveEnabled by remember { mutableStateOf(saveData?.autoSaveEnabled ?: false) }
    var autoSaveInterval by remember { mutableIntStateOf(saveData?.autoSaveInterval ?: 5) } // 自动存档间隔（天）
    var lastAutoSaveDay by remember { mutableIntStateOf(saveData?.lastAutoSaveDay ?: 0) } // 上次自动存档的游戏天数
    
    // 已使用的兑换码状态
    var usedRedeemCodes by remember { mutableStateOf(saveData?.usedRedeemCodes ?: emptySet()) }
    
    // 支持者功能解锁状态（使用State存储异步结果）
    var isSupporterUnlocked by remember { mutableStateOf(saveData?.isSupporterUnlocked ?: false) }
    
    // 异步检查支持者功能解锁状态
    LaunchedEffect(userId, usedRedeemCodes) {
        if (userId != null) {
            try {
                val unlocked = LeanCloudRedeemCodeManager.isSupporterUnlocked(userId)
                if (unlocked) {
                    isSupporterUnlocked = true
                    Log.d("LeanCloud", "支持者功能已解锁（云端）")
                }
            } catch (e: Exception) {
                Log.e("LeanCloud", "检查支持者功能失败", e)
            }
        }
    }
    
    // 功能解锁对话框状态
    var showFeatureLockedDialog by remember { mutableStateOf(false) }
    
    
    // GVA颁奖对话框状态
    var showGVAAwardDialog by remember { mutableStateOf(false) }
    var gvaAwardYear by remember { mutableIntStateOf(1) }
    var gvaAwardNominations by remember { mutableStateOf(emptyList<AwardNomination>()) }
    var gvaPlayerWonCount by remember { mutableIntStateOf(0) }
    var gvaPlayerTotalReward by remember { mutableLongStateOf(0L) }
    var gvaPlayerFansGain by remember { mutableLongStateOf(0L) }
    
    // 挑战完成对话框状态
    var showChallengeCompleteDialog by remember { mutableStateOf(false) }
    var totalAcquiredCompanies by remember { mutableIntStateOf(0) }
    
    // 赛事完成弹窗状态
    var showTournamentResultDialog by remember { mutableStateOf(false) }
    var tournamentResult by remember { mutableStateOf(null as EsportsTournament?) }
    
    // 成就系统状态
    var unlockedAchievements by remember { mutableStateOf(saveData?.unlockedAchievements ?: emptyList()) }
    var pendingAchievementsToShow by remember { mutableStateOf(emptyList<Achievement>()) }
    var hasCheckedInitialAchievements by remember { mutableStateOf(false) }
    
    // 教程系统状态
    val tutorialState = rememberTutorialState(
        completedTutorials = saveData?.completedTutorials ?: emptySet(),
        skipTutorial = saveData?.skipTutorial ?: false
    )
    
    // GVA游戏大奖系统状态  
    var companyReputation by remember(saveData) { 
        mutableStateOf(saveData?.companyReputation ?: CompanyReputation()) 
    }
    var gvaHistory by remember(saveData) { 
        mutableStateOf(saveData?.gvaHistory ?: emptyList()) 
    }
    var currentYearNominations by remember(saveData) { 
        mutableStateOf(saveData?.currentYearNominations ?: emptyList()) 
    }
    var gvaAnnouncedDate by remember(saveData) { 
        mutableStateOf(saveData?.gvaAnnouncedDate) 
    }
    
    // 员工忠诚度和年终奖系统状态
    var showSalaryRequestDialog by remember { mutableStateOf(false) }
    var salaryRequestEmployee by remember { mutableStateOf(null as Employee?) }
    var showYearEndBonusDialog by remember { mutableStateOf(false) }
    var lastYearEndBonusYear by remember { mutableIntStateOf(0) } // 上次年终奖年份，防止重复触发
    
    // 获取待处理的应聘者数量
    val jobPostingService = remember { JobPostingService.getInstance() }
    var pendingApplicantsCount by remember { mutableIntStateOf(0) }
    
    // 监听岗位变化，更新待处理应聘者数量
    LaunchedEffect(jobPostingRefreshTrigger) {
        pendingApplicantsCount = jobPostingService.getTotalPendingApplicants()
    }
    
    // 计算待分配的项目数量（正在开发中且未分配员工的项目）
    val pendingAssignmentCount by remember {
        derivedStateOf {
            games.count { game ->
                game.releaseStatus == GameReleaseStatus.DEVELOPMENT && game.assignedEmployees.isEmpty()
            }
        }
    }
    
    // 创建创始人对象
    val founder = remember(founderName, founderProfession) {
        Founder(name = founderName, profession = founderProfession)
    }

    // 初始化RevenueManager数据：新游戏清空，读档恢复（只执行一次）
    LaunchedEffect(Unit) {
        val instanceId = System.currentTimeMillis()
        Log.d("GameScreen", "【实例 $instanceId】LaunchedEffect(Unit) 开始执行, saveData=${if (saveData != null) "非null" else "null"}")
        
        if (saveData != null) {
            // ===== 读档：恢复数据 =====
            Log.d("GameScreen", "【实例 $instanceId】===== 读档模式：开始恢复数据 =====")
            
            // 恢复服务器数据
            if (saveData.serverData.isNotEmpty()) {
                RevenueManager.importServerData(saveData.serverData)
                Log.d("GameScreen", "【实例 $instanceId】✓ 从存档恢复服务器数据: ${saveData.serverData.size} 个游戏")
            } else {
                Log.d("GameScreen", "【实例 $instanceId】⚠ 存档中没有服务器数据")
            }
            
            // 恢复收益数据
            if (saveData.revenueData.isNotEmpty()) {
                RevenueManager.importRevenueData(saveData.revenueData)
                Log.d("GameScreen", "【实例 $instanceId】✓ 从存档恢复收益数据: ${saveData.revenueData.size} 个游戏")
            } else {
                Log.d("GameScreen", "【实例 $instanceId】⚠ 存档中没有收益数据（可能是旧存档）")
            }
            
            // 为已发售但没有收益数据的游戏初始化数据（向后兼容旧存档，只处理RELEASED状态）
            saveData.games
                .filter { it.releaseStatus == GameReleaseStatus.RELEASED }
                .forEach { releasedGame ->
                    val exists = RevenueManager.getGameRevenue(releasedGame.id)
                    if (exists == null) {
                        Log.d("GameScreen", "【实例 $instanceId】⚠ 游戏 ${releasedGame.name} 没有收益数据，初始化空数据")
                        val price = releasedGame.releasePrice?.toDouble() ?: 0.0
                        RevenueManager.generateRevenueData(
                            gameId = releasedGame.id,
                            gameName = releasedGame.name,
                            releasePrice = price,
                            daysOnMarket = 0,
                            releaseYear = currentYear,
                            releaseMonth = currentMonth,
                            releaseDay = currentDay,
                            promotionIndex = releasedGame.promotionIndex
                        )
                        // 初始化游戏信息（商业模式和付费内容）
                        // 修复：如果付费内容没有设置价格，自动使用推荐价格
                        val monetizationItemsWithPrices = if (releasedGame.businessModel == BusinessModel.ONLINE_GAME) {
                            releasedGame.monetizationItems.map { item ->
                                if (item.price == null || item.price <= 0) {
                                    // 使用推荐价格
                                    item.copy(price = item.type.getRecommendedPrice())
                                } else {
                                    item
                                }
                            }
                        } else {
                            releasedGame.monetizationItems
                        }
                        
                        RevenueManager.updateGameInfo(
                            releasedGame.id,
                            releasedGame.businessModel,
                            monetizationItemsWithPrices
                        )
                        // 初始化游戏IP信息（用于销量加成）
                        RevenueManager.updateGameIP(releasedGame.id, releasedGame.fromIP)
                    } else {
                        // 收益数据存在，更新游戏信息（商业模式和付费内容）
                        // 修复：如果付费内容没有设置价格，自动使用推荐价格
                        val monetizationItemsWithPrices = if (releasedGame.businessModel == BusinessModel.ONLINE_GAME) {
                            releasedGame.monetizationItems.map { item ->
                                if (item.price == null || item.price <= 0) {
                                    // 使用推荐价格
                                    item.copy(price = item.type.getRecommendedPrice())
                                } else {
                                    item
                                }
                            }
                        } else {
                            releasedGame.monetizationItems
                        }
                        
                        RevenueManager.updateGameInfo(
                            releasedGame.id,
                            releasedGame.businessModel,
                            monetizationItemsWithPrices
                        )
                        // 更新游戏IP信息（用于销量加成）
                        RevenueManager.updateGameIP(releasedGame.id, releasedGame.fromIP)
                    }
                }
            
            // 调整低评分游戏的历史销量（旧存档兼容）- 必须在游戏信息设置之后（只处理RELEASED状态）
            saveData.games
                .filter { it.releaseStatus == GameReleaseStatus.RELEASED }
                .forEach { game ->
                    val rating = game.rating
                    if (rating != null && rating < 3.0f && game.businessModel == BusinessModel.SINGLE_PLAYER) {
                        val releasePrice = game.releasePrice?.toDouble() ?: 0.0
                        val adjusted = RevenueManager.adjustLowRatingGameSales(game.id, rating, releasePrice)
                        if (adjusted) {
                            Log.d("GameScreen", "【实例 $instanceId】✓ 调整低评分游戏 ${game.name} (${rating}分) 的历史销量")
                        }
                    }
                }
            
            // 为旧存档中的子公司网游生成付费内容（向后兼容）
            var needUpdateGames = false
            val updatedGames = saveData.games.map { game ->
                if (game.id.startsWith("inherited_") && 
                    game.businessModel == BusinessModel.ONLINE_GAME &&
                    game.monetizationItems.isEmpty()) {
                    // 子公司网游没有付费内容，自动生成
                    needUpdateGames = true
                    val recommendedTypes = MonetizationConfig.getRecommendedItems(game.theme)
                    val monetizationItems = recommendedTypes.map { itemType ->
                        MonetizationItem(
                            type = itemType,
                            price = itemType.getRecommendedPrice(),
                            isEnabled = true
                        )
                    }
                    Log.d("GameScreen", "【实例 $instanceId】✓ 为旧存档子公司网游 ${game.name} 生成付费内容（${monetizationItems.size}个）")
                    game.copy(
                        monetizationItems = monetizationItems,
                        allDevelopmentEmployees = game.allDevelopmentEmployees
                    )
                } else {
                    game
                }
            }
            if (needUpdateGames) {
                games = updatedGames
                Log.d("GameScreen", "【实例 $instanceId】✓ 已更新子公司网游的付费内容")
                
                // 🔧 修复：同步更新RevenueManager中的付费内容信息
                updatedGames
                    .filter { it.id.startsWith("inherited_") && it.businessModel == BusinessModel.ONLINE_GAME }
                    .forEach { game ->
                        RevenueManager.updateGameInfo(
                            game.id,
                            game.businessModel,
                            game.monetizationItems
                        )
                        Log.d("GameScreen", "【实例 $instanceId】✓ 同步 ${game.name} 的付费内容到RevenueManager（${game.monetizationItems.size}个）")
                    }
            }
            
            // 恢复招聘岗位数据
            if (saveData.jobPostings.isNotEmpty()) {
                jobPostingService.loadFromSave(saveData.jobPostings)
                Log.d("GameScreen", "【实例 $instanceId】✓ 从存档恢复招聘岗位数据: ${saveData.jobPostings.size} 个岗位")
            } else {
                jobPostingService.clearAllData()
                Log.d("GameScreen", "【实例 $instanceId】⚠ 存档中没有招聘岗位数据，清空岗位")
            }
            
            // 触发一次UI刷新以显示已恢复的收益
            revenueRefreshTrigger++
            jobPostingRefreshTrigger++
            
            // 检查游戏速度：如果加载旧存档且未解锁2x/3x速度，自动重置为1x速度
            val isSupporterUnlockedForSpeedCheck = saveData.isSupporterUnlocked
            if (!isSupporterUnlockedForSpeedCheck && gameSpeed > 1) {
                gameSpeed = 1
                Log.d("GameScreen", "【实例 $instanceId】⚠ 旧存档未解锁2x/3x速度，已重置为1x速度")
            }
            
            // 🔍 调试：检查恢复后的收入数据
            val loadedRevenue = RevenueManager.exportRevenueData()
            Log.d("GameScreen", "【实例 $instanceId】===== 读档数据恢复完成 =====")
            Log.d("GameScreen", "🔍 收入数据检查:")
            Log.d("GameScreen", "  总条目数: ${loadedRevenue.size}")
            loadedRevenue.forEach { (gameId, revenue) ->
                val totalRevenue = revenue.dailySalesList.sumOf { it.revenue }
                Log.d("GameScreen", "  - ${revenue.gameName} (${if (gameId.startsWith("inherited_")) "继承" else "自研"}): 记录${revenue.dailySalesList.size}天, 总收入¥${totalRevenue.toLong()}")
            }
        } else {
            // ===== 新游戏：清空旧数据 =====
            Log.d("GameScreen", "【实例 $instanceId】===== 新游戏模式：清空旧数据 =====")
            RevenueManager.clearAllData()
            jobPostingService.clearAllData()
            Log.d("GameScreen", "【实例 $instanceId】✓ 清空招聘岗位数据")
            
            // 🔍 调试：确认清空后没有收入数据
            val afterClear = RevenueManager.exportRevenueData()
            if (afterClear.isNotEmpty()) {
                Log.e("GameScreen", "⚠️ 警告：清空后仍有${afterClear.size}条收入数据！")
                afterClear.forEach { (gameId, revenue) ->
                    Log.e("GameScreen", "  - ${revenue.gameName} (ID: ${gameId.take(20)}...)")
                }
            } else {
                Log.d("GameScreen", "✓ 确认收入数据已清空")
            }
        }
    }
    
    // 初始化员工列表 - 从存档加载或创建创始人员工（只执行一次）
    LaunchedEffect(Unit) {
        if (allEmployees.isEmpty()) {
            if (saveData != null && saveData.allEmployees.isNotEmpty()) {
                // 从存档加载员工数据
                allEmployees.addAll(saveData.allEmployees)
            } else {
                // 新游戏：将创始人转换为员工
                val founderAsEmployee = founder.toEmployee(
                    hireYear = 1,
                    hireMonth = 1,
                    hireDay = 1
                )
                allEmployees.add(founderAsEmployee)
            }
        }
    }
    
    // 初始化竞争对手（只执行一次，且只在存档中没有竞争对手时才生成）
    LaunchedEffect(saveData) {
        if (saveData == null) {
            // 新游戏：生成初始竞争对手
            if (competitors.isEmpty()) {
                competitors = CompetitorManager.generateInitialCompetitors(
                    companyName, 
                    currentYear, 
                    currentMonth
                )
                Log.d("MainActivity", "初始化竞争对手：生成${competitors.size}家竞争公司")
            }
        } else {
            // 读档：从存档中恢复竞争对手列表
            // 注意：即使存档中competitors为空（所有对手都被收购），也不应该重新生成
            competitors = saveData.competitors
            Log.d("MainActivity", "从存档恢复竞争对手：${competitors.size}家竞争公司")
            
            // 🆕 修复旧存档中的竞争对手数据（向后兼容）
            if (competitors.isNotEmpty()) {
                val fixedCompetitors = CompetitorManager.fixLegacyCompetitorGames(
                    competitors, currentYear, currentMonth
                )
                if (fixedCompetitors != competitors) {
                    competitors = fixedCompetitors
                    Log.d("MainActivity", "✅ 已修复旧存档的竞争对手游戏数据")
                }
            }
            
            // 🆕 修复旧存档中的子公司数据（向后兼容）
            if (subsidiaries.isNotEmpty()) {
                val fixedSubsidiaries = subsidiaries.map { subsidiary ->
                    val fixedGames = subsidiary.games.map { game ->
                        if (game.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME && 
                            game.totalRegisteredPlayers == 0L) {
                            val monthsSinceRelease = (currentYear - game.releaseYear) * 12 + (currentMonth - game.releaseMonth)
                            val daysSinceLaunch = monthsSinceRelease * 30
                            val totalLifecycleDays = 365
                            val lifecycleProgress = ((daysSinceLaunch.toDouble() / totalLifecycleDays) * 100.0).coerceIn(0.0, 100.0)
                            
                            var playerInterest = 100.0
                            val decayCount = daysSinceLaunch / 90
                            for (i in 0 until decayCount) {
                                val dayAtInterval = (i + 1) * 90
                                val progressAtInterval = ((dayAtInterval.toDouble() / totalLifecycleDays) * 100.0).coerceIn(0.0, 100.0)
                                val decayRate = when {
                                    progressAtInterval < 30.0 -> 8.0
                                    progressAtInterval < 70.0 -> 15.0
                                    progressAtInterval < 90.0 -> 25.0
                                    else -> 35.0
                                }
                                playerInterest = (playerInterest - decayRate).coerceIn(0.0, 100.0)
                            }
                            
                            val interestMultiplier = when {
                                playerInterest >= 70.0 -> 1.0
                                playerInterest >= 50.0 -> 0.7
                                playerInterest >= 30.0 -> 0.4
                                else -> 0.2
                            }
                            val totalRegistered = if (interestMultiplier > 0) {
                                (game.activePlayers / (0.4 * interestMultiplier)).toLong()
                            } else {
                                (game.activePlayers * 5).toLong()
                            }
                            
                            game.copy(
                                totalRegisteredPlayers = totalRegistered,
                                playerInterest = playerInterest,
                                lifecycleProgress = lifecycleProgress,
                                daysSinceLaunch = daysSinceLaunch,
                                lastInterestDecayDay = decayCount * 90
                            )
                        } else {
                            game
                        }
                    }
                    subsidiary.copy(games = fixedGames)
                }
                if (fixedSubsidiaries != subsidiaries) {
                    subsidiaries = fixedSubsidiaries
                    Log.d("MainActivity", "✅ 已修复旧存档的子公司游戏数据")
                }
            }
        }
    }
    
    // 🔧 GVA历史记录补偿机制（游戏加载时执行一次）
    LaunchedEffect(Unit) {
        // 检测条件：当年提名不为空 + 历史记录为空 + 提名已经是最终结果
        if (currentYearNominations.isNotEmpty() && 
            gvaHistory.isEmpty() && 
            currentYearNominations.any { it.isFinal }) {
            
            Log.d("MainActivity", "🔧 [启动时检测] GVA历史记录丢失，执行数据补偿...")
            
            // 将当年最终提名添加到历史记录
            gvaHistory = currentYearNominations
            
            Log.d("MainActivity", "✅ GVA历史记录补偿完成，恢复${gvaHistory.size}条记录（年份：${currentYearNominations.firstOrNull()?.year}）")
        }
    }
    
    // 游戏初始化：检查并解锁成就（新游戏或读档都检查一次）
    // 依赖 allEmployees.size 确保在员工初始化后执行
    LaunchedEffect(allEmployees.size) {
        // 只在员工初始化完成后检查一次（使用标志位防止重复检查）
        if (allEmployees.isNotEmpty() && !hasCheckedInitialAchievements) {
            // 创建当前存档数据快照
            val currentSaveData = SaveData(
                money = money,
                fans = fans,
                allEmployees = allEmployees.toList(),
                games = games,
                unlockedAchievements = unlockedAchievements
            )
            val revenueDataMap = RevenueManager.exportRevenueData()
            
            // 检查所有成就
            val newlyUnlocked = AchievementManager.checkAndUnlockAchievements(
                currentSaveData,
                revenueDataMap
            )
            
            if (newlyUnlocked.isNotEmpty()) {
                // 更新已解锁成就列表
                newlyUnlocked.forEach { achievement ->
                    unlockedAchievements = AchievementManager.unlockAchievement(
                        unlockedAchievements,
                        achievement
                    )
                }
                // 添加到待显示队列
                pendingAchievementsToShow = newlyUnlocked
                Log.d("MainActivity", "🏆 游戏初始化解锁${newlyUnlocked.size}个成就: ${newlyUnlocked.map { it.name }}")
            }
        }
    }
    // 时间推进系统 - 直接按天推进
    LaunchedEffect(gameSpeed) {
        val loopId = System.currentTimeMillis()
        Log.d("MainActivity", "▶️ 游戏循环启动 [循环ID=$loopId]: gameSpeed=$gameSpeed, 初始暂停状态=$isPaused")
        
        while (true) {
            // 如果暂停，就一直等待
            while (isPaused) {
                delay(100L)
            }
            
            // 执行游戏逻辑前再次确认未暂停
            if (isPaused) {
                Log.d("MainActivity", "⏸️ [循环ID=$loopId] 检测到暂停，跳过本次循环")
                continue
            }
            
            // 根据游戏速度延迟不同的时间后推进一天
            delay(when (gameSpeed) {
                1 -> 15000L // 慢速：15秒推进1天
                2 -> 2000L // 中速：2秒推进1天
                3 -> 1000L // 快速：1秒推进1天
                else -> 2000L
            })
            
            // delay 后立即检查暂停状态
            if (isPaused) {
                Log.d("MainActivity", "⏸️ [循环ID=$loopId] delay后检测到暂停，跳过本次循环")
                continue
            }
            
            // 推进日期
            currentDay++
            // 12月特殊处理：有31天（为了GVA颁奖典礼）
            val maxDaysInMonth = if (currentMonth == 12) 31 else 30
            if (currentDay > maxDaysInMonth) {
                currentDay = 1
                currentMonth++
                // 检查月份是否超过12，需要进入下一年
                if (currentMonth > 12) {
                    currentMonth = 1
                    currentYear++
                }
            }
            
            if (ENABLE_VERBOSE_GAME_LOGS) {
                Log.d("MainActivity", "📅 日期推进: ${currentYear}年${currentMonth}月${currentDay}日")
            }
            
            // 每天更新已发售游戏的收益（只有RELEASED状态才产生收益）
            val releasedGames = games.filter { 
                it.releaseStatus == GameReleaseStatus.RELEASED
            }
            
            // 🔍 调试：每月1日输出发售游戏详情
            if (currentDay == 1) {
                Log.d("MainActivity", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d("MainActivity", "📅 ${currentYear}年${currentMonth}月${currentDay}日 - 发售游戏检查")
                Log.d("MainActivity", "发售中游戏数量: ${releasedGames.size}")
                releasedGames.forEach { game ->
                    val isInherited = game.id.startsWith("inherited_")
                    Log.d("MainActivity", "  ${game.name}:")
                    Log.d("MainActivity", "    - 类型: ${if (isInherited) "继承游戏" else "自研游戏"}")
                    Log.d("MainActivity", "    - 商业模式: ${game.businessModel}")
                    Log.d("MainActivity", "    - 状态: ${game.releaseStatus}")
                    Log.d("MainActivity", "    - ID: ${game.id.take(30)}...")
                }
                if (releasedGames.isEmpty()) {
                    Log.d("MainActivity", "  ⚠️ 没有任何发售中的游戏！")
                }
                Log.d("MainActivity", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            }
            
            if (!isPaused && releasedGames.isNotEmpty()) {
                // 性能优化：在后台线程批量计算收益，减少主线程阻塞
                val totalRevenue = withContext(Dispatchers.Default) {
                    var total = 0.0
                    releasedGames.forEach { releasedGame ->
                        // 更新游戏信息（商业模式和付费内容）
                        RevenueManager.updateGameInfo(
                            releasedGame.id,
                            releasedGame.businessModel,
                            releasedGame.monetizationItems
                        )
                        
                        // 按天计算收益
                        val gameRating = releasedGame.gameRating?.finalScore
                        val reputationLevel = companyReputation.getLevel()
                        val dailyRevenue = RevenueManager.addDailyRevenueForGame(
                            gameId = releasedGame.id,
                            gameRating = gameRating,
                            fanCount = fans,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            reputationBonus = reputationLevel.salesBonus
                        )
                        
                        total += dailyRevenue
                        if (ENABLE_VERBOSE_GAME_LOGS) {
                            Log.d("MainActivity", "💰 每日收益: ${releasedGame.name} +¥${dailyRevenue.toLong()}")
                        }
                    }
                    total
                }
                // 在主线程一次性更新资金（减少状态更新次数）
                money = safeAddMoney(money, totalRevenue.toLong())
            }
            
            // 触发收益数据刷新
            revenueRefreshTrigger++
            
            // 每日检查：扣除到期服务器的月费（按购买日期每30天计费）
            if (ENABLE_VERBOSE_GAME_LOGS) {
                Log.d("MainActivity", "准备调用服务器扣费检查... 当前日期: ${currentYear}年${currentMonth}月${currentDay}日")
            }
            val moneyBefore = money
            val serverBillingCost = RevenueManager.checkAndBillServers(
                currentYear = currentYear,
                currentMonth = currentMonth,
                currentDay = currentDay
            )
            if (ENABLE_VERBOSE_GAME_LOGS) {
                Log.d("MainActivity", "服务器扣费检查完成，返回金额: ¥$serverBillingCost")
            }
            if (serverBillingCost > 0) {
                money = safeAddMoney(money, -serverBillingCost)
                if (ENABLE_VERBOSE_GAME_LOGS) {
                    Log.d("MainActivity", "💰 服务器计费: -¥$serverBillingCost (扣费前:¥$moneyBefore -> 扣费后:¥$money)")
                }
            }
            
            // 每日检查：员工忠诚度变化（如果薪资低于期望薪资，忠诚度会逐渐降低）
            // 性能优化：在后台线程计算，减少主线程阻塞
            if (currentDay == 1) { // 只在每月1日更新，减少计算频率
                try {
                    val updatedEmployees2 = withContext(Dispatchers.Default) {
                        allEmployees.map { employee ->
                            if (!employee.isFounder && employee.requestedSalary == null) {
                                // 计算员工期望的薪资
                                val expectedSalary = employee.calculateExpectedSalary(employee.salary)
                                if (employee.salary < expectedSalary) {
                                    // 薪资低于期望，每月降低1点忠诚度
                                    employee.copy(loyalty = (employee.loyalty - 1).coerceAtLeast(0))
                                } else {
                                    // 薪资满足期望，每月恢复1点忠诚度
                                    employee.copy(loyalty = (employee.loyalty + 1).coerceAtMost(100))
                                }
                            } else {
                                employee
                            }
                        }
                    }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees2)
                } catch (e: Exception) {
                    Log.e("MainActivity", "更新员工忠诚度失败", e)
                }
            }
            
            // 自动存档检查（按天计算）
            if (autoSaveEnabled) {
                try {
                    // 计算当前游戏总天数
                    val currentTotalDays = (currentYear - 1) * 360 + (currentMonth - 1) * 30 + currentDay
                    
                    // 检查是否达到存档间隔
                    if (lastAutoSaveDay == 0 || currentTotalDays - lastAutoSaveDay >= autoSaveInterval) {
                        lastAutoSaveDay = currentTotalDays
                        
                        // 异步执行存档
                        launch {
                            try {
                                // 调试：保存前的子公司数据
                                Log.d("MainActivity", "🔍 准备保存：子公司数量=${subsidiaries.size}")
                                subsidiaries.forEachIndexed { index, sub ->
                                    Log.d("MainActivity", "  子公司[$index]: ${sub.name}, ID=${sub.id}, 游戏数=${sub.games.size}")
                                }
                                
                                val saveData = SaveData(
                                    companyName = companyName,
                                    companyLogo = selectedLogo,
                                    founderName = founderName,
                                    founderProfession = founderProfession,
                                    money = money,
                                    fans = fans,
                                    currentYear = currentYear,
                                    currentMonth = currentMonth,
                                    currentDay = currentDay,
                                    allEmployees = allEmployees.toList(),
                                    games = games,
                                    competitors = competitors,
                                    competitorNews = competitorNews,
                                    serverData = RevenueManager.exportServerData(),
                                    revenueData = RevenueManager.exportRevenueData(),
                                    jobPostings = JobPostingService.getInstance().getAllJobPostingsForSave(),
                                    complaints = complaints,
                                    autoProcessComplaints = autoProcessComplaints,
                                    autoPromotionThreshold = autoPromotionThreshold,
                                    autoApproveSalaryIncrease = autoApproveSalaryIncrease,
                                    unlockedAchievements = unlockedAchievements,
                                    completedTutorials = tutorialState.getCompletedTutorialsForSave(),
                                    skipTutorial = tutorialState.skipTutorial.value,
                                    companyReputation = companyReputation,
                                    gvaHistory = gvaHistory,
                                    currentYearNominations = currentYearNominations,
                                    gvaAnnouncedDate = gvaAnnouncedDate,
                                    ownedIPs = ownedIPs,
                                    subsidiaries = subsidiaries,
                                    gmModeEnabled = gmModeEnabled,
                                    usedRedeemCodes = usedRedeemCodes,
                                    isSupporterUnlocked = isSupporterUnlocked,
                                    autoSaveEnabled = autoSaveEnabled,
                                    autoSaveInterval = autoSaveInterval,
                                    lastAutoSaveDay = lastAutoSaveDay,
                                    saveTime = System.currentTimeMillis(),
                                    version = BuildConfig.VERSION_NAME
                                )
                                
                                // 调试：SaveData对象中的子公司数据
                                Log.d("MainActivity", "🔍 SaveData对象：子公司数量=${saveData.subsidiaries.size}")
                                
                                val result = saveManager.saveGameAsync(1, saveData)
                                if (result.success) {
                                    Log.d("MainActivity", "💾 自动存档成功（存档位1，间隔${autoSaveInterval}天）")
                                } else {
                                    Log.w("MainActivity", "💾 自动存档失败: ${result.errorMessage}")
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "自动存档异常", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "自动存档检查失败", e)
                }
            }
            
            if (currentDay == 1) {
                // GVA新年清理：1月1日清空本年度提名，开始新一年的评选
                if (currentMonth == 1 && currentYearNominations.isNotEmpty()) {
                    Log.d("MainActivity", "🎊 GVA：新年开始，清空上一年的提名数据")
                    currentYearNominations = emptyList()
                }
                
                // 🔧 GVA历史记录补偿机制：修复旧版本bug导致的数据丢失
                // 检测条件：当年提名不为空 + 历史记录为空 + 提名已经是最终结果 + 不是当年1月（避免误判）
                if (currentYearNominations.isNotEmpty() && 
                    gvaHistory.isEmpty() && 
                    currentYearNominations.any { it.isFinal } &&
                    currentMonth != 1) {
                    
                    Log.d("MainActivity", "🔧 检测到GVA历史记录丢失，执行数据补偿...")
                    
                    // 将当年最终提名添加到历史记录
                    gvaHistory = currentYearNominations
                    
                    Log.d("MainActivity", "✅ GVA历史记录补偿完成，恢复${gvaHistory.size}条记录")
                }
                
                // 检查是否需要进行月结算（避免读档后重复结算）
                val needSettlement = (currentYear != lastSettlementYear || currentMonth != lastSettlementMonth)
                
                if (needSettlement) {
                    Log.d("MainActivity", "🗓️ 触发月结算: ${currentYear}年${currentMonth}月（上次结算: ${lastSettlementYear}年${lastSettlementMonth}月）")
                    
                    // 月结算：玩家公司粉丝自然增长
                    val releasedGames = games.filter { 
                        it.releaseStatus == GameReleaseStatus.RELEASED || 
                        it.releaseStatus == GameReleaseStatus.RATED 
                    }
                    if (releasedGames.isNotEmpty()) {
                        // 基于平均评分计算粉丝增长（已移除游戏数量加成）
                        val avgRating = releasedGames.mapNotNull { it.gameRating?.finalScore }.average().toFloat()
                        
                        val baseFansGrowth = when {
                            avgRating >= 8.0f -> (fans * 0.015).toLong() // 1.5%增长（高评分）（原2.5%）
                            avgRating >= 6.0f -> (fans * 0.01).toLong() // 1.0%增长（中等评分）（原1.5%）
                            else -> (fans * 0.003).toLong() // 0.3%增长（低评分）（原0.5%）
                        }
                        
                        // 应用声望加成
                        val reputationLevel = companyReputation.getLevel()
                        val reputationBonus = reputationLevel.fansBonus
                        val reputationMultiplier = 1.0 + reputationBonus
                        
                        val totalFansGrowth = (baseFansGrowth * reputationMultiplier).toLong().coerceAtLeast(100L)
                        fans = (fans + totalFansGrowth).coerceAtLeast(0L)
                        
                        Log.d("MainActivity", "月结算粉丝增长: +$totalFansGrowth (平均评分:$avgRating, 声望加成:+${(reputationBonus*100).toInt()}%, 当前粉丝:$fans)")
                    }
                    
                    // 月结算：宣传指数衰减 - 性能优化：在后台线程计算
                    games = withContext(Dispatchers.Default) {
                        games.map { game ->
                            if (game.promotionIndex > 0f) {
                                // 根据游戏状态确定衰减速度
                                val decayRate = when (game.releaseStatus) {
                                    GameReleaseStatus.DEVELOPMENT,
                                    GameReleaseStatus.READY_FOR_RELEASE,
                                    GameReleaseStatus.PRICE_SETTING -> 0.04f  // 开发中游戏：每月衰减4%
                                    GameReleaseStatus.RELEASED,
                                    GameReleaseStatus.RATED -> 0.10f  // 已发售游戏：每月衰减10%
                                    else -> 0f  // 已下架游戏不衰减
                                }
                                
                                val newPromotionIndex = (game.promotionIndex - decayRate).coerceAtLeast(0f)
                                
                                // 日志输出衰减信息（仅在详细模式）
                                if (ENABLE_VERBOSE_GAME_LOGS && game.promotionIndex != newPromotionIndex) {
                                    val statusText = when (game.releaseStatus) {
                                        GameReleaseStatus.DEVELOPMENT -> "开发中"
                                        GameReleaseStatus.READY_FOR_RELEASE -> "准备发售"
                                        GameReleaseStatus.PRICE_SETTING -> "价格设置中"
                                        GameReleaseStatus.RELEASED -> "已发售"
                                        GameReleaseStatus.RATED -> "已评分"
                                        else -> "其他"
                                    }
                                    Log.d("MainActivity", "宣传指数衰减: ${game.name} ($statusText) ${(game.promotionIndex * 100).toInt()}% -> ${(newPromotionIndex * 100).toInt()}% (衰减${(decayRate * 100).toInt()}%)")
                                }
                                
                                game.copy(
                                    promotionIndex = newPromotionIndex,
                                    allDevelopmentEmployees = game.allDevelopmentEmployees
                                )
                            } else {
                                game
                            }
                        }
                    }
                    
                    // 月结算：自动宣传（检查开启自动宣传的游戏，如果宣传指数低于阈值则自动宣传）
                    // 使用当前的阈值设置（从内存中的状态获取）
                    val gamesNeedingPromotion = games.filter { game ->
                        game.autoPromotion && 
                        game.promotionIndex < autoPromotionThreshold &&
                        (game.releaseStatus == GameReleaseStatus.DEVELOPMENT ||
                         game.releaseStatus == GameReleaseStatus.READY_FOR_RELEASE ||
                         game.releaseStatus == GameReleaseStatus.PRICE_SETTING ||
                         game.releaseStatus == GameReleaseStatus.RELEASED ||
                         game.releaseStatus == GameReleaseStatus.RATED)
                    }
                    
                    if (gamesNeedingPromotion.isNotEmpty()) {
                        // 根据资金选择最好的宣传方式
                        // 从最贵的开始尝试，选择能够负担得起的最好的宣传方式
                        val availablePromotionTypes = com.example.yjcy.ui.PromotionType.entries
                            .sortedByDescending { it.promotionIndexGain } // 按宣传指数增益降序排列
                        
                        val selectedPromotionType = availablePromotionTypes.firstOrNull { promotionType ->
                        val totalCost = promotionType.cost * gamesNeedingPromotion.size
                            money >= totalCost
                        } ?: com.example.yjcy.ui.PromotionType.SOCIAL_MEDIA // 如果都负担不起，至少尝试最便宜的
                        
                        val totalCost = selectedPromotionType.cost * gamesNeedingPromotion.size
                        
                        // 检查资金是否足够
                        if (money >= totalCost) {
                            money = safeAddMoney(money, -totalCost)
                            fans += selectedPromotionType.fansGain * gamesNeedingPromotion.size
                            
                            // 更新所有需要宣传的游戏的宣传指数 - 性能优化：使用Set提升查找效率
                            val promotionGameIds = gamesNeedingPromotion.map { it.id }.toSet()
                            games = withContext(Dispatchers.Default) {
                                games.map { game ->
                                    if (game.id in promotionGameIds) {
                                        val newPromotionIndex = (game.promotionIndex + selectedPromotionType.promotionIndexGain).coerceAtMost(1.0f)
                                        game.copy(
                                            promotionIndex = newPromotionIndex,
                                            allDevelopmentEmployees = game.allDevelopmentEmployees
                                        )
                                    } else {
                                        game
                                    }
                                }
                            }
                            
                            Log.d("MainActivity", "自动宣传: 为${gamesNeedingPromotion.size}个游戏进行了${selectedPromotionType.displayName}，总费用¥${totalCost}，宣传指数提升${(selectedPromotionType.promotionIndexGain * 100).toInt()}%")
                        } else {
                            Log.d("MainActivity", "自动宣传: 资金不足（需要¥${totalCost}，当前¥${money}），跳过自动宣传")
                        }
                    }
                    
                    // 月结算：更新竞争对手
                    val (updatedCompetitors, newNews) = CompetitorManager.updateCompetitors(
                        competitors,
                        currentYear,
                        currentMonth,
                        currentDay
                    )
                    competitors = updatedCompetitors
                    // 添加新闻，保持最近30条
                    competitorNews = (newNews + competitorNews).take(30)
                    
                    // 月结算：更新子公司
                    subsidiaries = subsidiaries.map { subsidiary ->
                        val updatedSubsidiary = SubsidiaryManager.updateMonthlyData(subsidiary)
                        
                        // 如果盈利，上缴利润分成
                        val profitShare = updatedSubsidiary.getProfitShare()
                        if (profitShare > 0) {
                            money = safeAddMoney(money, profitShare)
                            Log.d("MainActivity", "🏭 子公司[${subsidiary.name}]上缴利润: +¥${profitShare} (分成${(subsidiary.profitSharingRate * 100).toInt()}%)")
                        } else if (updatedSubsidiary.getMonthlyProfit() < 0) {
                            Log.d("MainActivity", "⚠️ 子公司[${subsidiary.name}]本月亏损: ¥${updatedSubsidiary.getMonthlyProfit()}")
                        }
                        
                        // 检查资金是否为0，如果是则弹出对话框
                        if (updatedSubsidiary.cashBalance <= 0 && !showSubsidiaryBankruptDialog) {
                            Log.d("MainActivity", "💸 子公司[${subsidiary.name}]资金不足！当前资金: ¥${updatedSubsidiary.cashBalance}")
                            bankruptSubsidiary = updatedSubsidiary
                            injectionAmountInput = "" // 清空输入框
                            showSubsidiaryBankruptDialog = true
                            // 暂停游戏，让玩家做出选择
                            isPaused = true
                        }
                        
                        updatedSubsidiary
                    }
                    
                    // 月结算：清理旧客诉（不再生成新客诉，只清理）
                    // 修复：传入当前年月，确保不会删除本月完成的客诉
                    complaints = CustomerServiceManager.cleanupOldComplaints(complaints, currentYear, currentMonth)
                    
                    // 更新上次月结算时间
                    lastSettlementYear = currentYear
                    lastSettlementMonth = currentMonth
                    
                    // 月结算：扣除员工工资
                    val totalSalaryCost: Long = allEmployees.sumOf { it.salary.toLong() }
                    if (totalSalaryCost > 0) {
                        money = safeAddMoney(money.toLong(), -totalSalaryCost)
                        Log.d("MainActivity", "💰 月结算工资扣除: -¥$totalSalaryCost (员工数:${allEmployees.size}, 扣费后:¥$money)")
                    }
                    
                    // 月结算：检查成就
                    val currentSaveData = SaveData(
                        money = money,
                        fans = fans,
                        allEmployees = allEmployees.toList(),
                        games = games,
                        unlockedAchievements = unlockedAchievements
                    )
                    val revenueDataMap = RevenueManager.exportRevenueData()
                    val newlyUnlocked = AchievementManager.checkAndUnlockAchievements(
                        currentSaveData,
                        revenueDataMap
                    )
                    
                    if (newlyUnlocked.isNotEmpty()) {
                        // 更新已解锁成就列表
                        newlyUnlocked.forEach { achievement ->
                            unlockedAchievements = AchievementManager.unlockAchievement(
                                unlockedAchievements,
                                achievement
                            )
                        }
                        // 添加到待显示队列
                        pendingAchievementsToShow = newlyUnlocked
                        Log.d("MainActivity", "🏆 解锁${newlyUnlocked.size}个新成就: ${newlyUnlocked.map { it.name }}")
                    }
                    
                    Log.d("MainActivity", "✅ 月结算完成: ${currentYear}年${currentMonth}月")
                } else {
                    Log.d("MainActivity", "⏭️ 跳过月结算（本月已结算）: ${currentYear}年${currentMonth}月")
                }
                
                // 每日检查：自动宣传（检查开启自动宣传的游戏，如果宣传指数低于阈值则自动宣传）
                // 每天检查一次，更及时地触发自动宣传
                val needDailyAutoPromotionCheck = (
                    currentYear != lastAutoPromotionCheckYear || 
                    currentMonth != lastAutoPromotionCheckMonth || 
                    currentDay != lastAutoPromotionCheckDay
                )
                
                if (needDailyAutoPromotionCheck) {
                    val gamesNeedingPromotion = games.filter { game ->
                        game.autoPromotion && 
                        game.promotionIndex < autoPromotionThreshold &&
                        (game.releaseStatus == GameReleaseStatus.DEVELOPMENT ||
                         game.releaseStatus == GameReleaseStatus.READY_FOR_RELEASE ||
                         game.releaseStatus == GameReleaseStatus.PRICE_SETTING ||
                         game.releaseStatus == GameReleaseStatus.RELEASED ||
                         game.releaseStatus == GameReleaseStatus.RATED)
                    }
                    
                    if (gamesNeedingPromotion.isNotEmpty()) {
                        // 根据资金选择最好的宣传方式
                        // 从最贵的开始尝试，选择能够负担得起的最好的宣传方式
                        val availablePromotionTypes = com.example.yjcy.ui.PromotionType.entries
                            .sortedByDescending { it.promotionIndexGain } // 按宣传指数增益降序排列
                        
                        val selectedPromotionType = availablePromotionTypes.firstOrNull { promotionType ->
                            val totalCost = promotionType.cost * gamesNeedingPromotion.size
                            money >= totalCost
                        } ?: com.example.yjcy.ui.PromotionType.SOCIAL_MEDIA // 如果都负担不起，至少尝试最便宜的
                        
                        val totalCost = selectedPromotionType.cost * gamesNeedingPromotion.size
                        
                        // 检查资金是否足够
                        if (money >= totalCost) {
                            money = safeAddMoney(money, -totalCost)
                            fans += selectedPromotionType.fansGain * gamesNeedingPromotion.size
                            
                            // 更新所有需要宣传的游戏的宣传指数 - 性能优化：使用Set提升查找效率
                            val promotionGameIds = gamesNeedingPromotion.map { it.id }.toSet()
                            games = withContext(Dispatchers.Default) {
                                games.map { game ->
                                    if (game.id in promotionGameIds) {
                                        val newPromotionIndex = (game.promotionIndex + selectedPromotionType.promotionIndexGain).coerceAtMost(1.0f)
                                        game.copy(
                                            promotionIndex = newPromotionIndex,
                                            allDevelopmentEmployees = game.allDevelopmentEmployees
                                        )
                                    } else {
                                        game
                                    }
                                }
                            }
                            
                            Log.d("MainActivity", "📢 每日自动宣传: 为${gamesNeedingPromotion.size}个游戏进行了${selectedPromotionType.displayName}，总费用¥${totalCost}，宣传指数提升${(selectedPromotionType.promotionIndexGain * 100).toInt()}%")
                        } else {
                            Log.d("MainActivity", "📢 每日自动宣传: 资金不足（需要¥${totalCost}，当前¥${money}），跳过自动宣传")
                        }
                        
                        // 更新上次检查日期
                        lastAutoPromotionCheckYear = currentYear
                        lastAutoPromotionCheckMonth = currentMonth
                        lastAutoPromotionCheckDay = currentDay
                    }
                }
            }
            
            // GVA评选逻辑：12月15日生成初步提名
            if (currentMonth == 12 && currentDay == 15) {
                Log.d("MainActivity", "🏆 GVA：生成${currentYear}年初步提名...")
                
                currentYearNominations = GVAManager.generatePreliminaryNominations(
                    year = currentYear,
                    playerGames = games,
                    playerCompanyName = companyName,
                    playerFans = fans,
                    competitorCompanies = competitors,
                    revenueData = RevenueManager.exportRevenueData()
                )
                
                Log.d("MainActivity", "🏆 GVA：生成${currentYearNominations.size}个提名")
                
                // 提示消息
                messageText = "🏆 GVA ${currentYear}年初步提名已公布！共${currentYearNominations.size}个奖项提名"
                showMessage = true
            }
            
            // GVA评选逻辑：12月31日公布最终获奖结果
            if (currentMonth == 12 && currentDay == 31) {
                Log.d("MainActivity", "🏆 GVA：公布${currentYear}年最终获奖结果...")
                
                // 生成最终提名（包含12月15-31日发售的游戏）
                val finalNominations = GVAManager.generateFinalNominations(
                    year = currentYear,
                    playerGames = games,
                    playerCompanyName = companyName,
                    playerFans = fans,
                    competitorCompanies = competitors,
                    revenueData = RevenueManager.exportRevenueData()
                )
                
                // 计算玩家获奖情况并统计奖励
                var totalCashReward = 0L
                var totalFansReward = 0L
                var totalReputationGain = 0
                
                val winnerGameIds = mutableSetOf<String>()
                
                finalNominations.forEach { nomination ->
                    val winner = nomination.winner
                    if (winner != null && winner.isPlayerGame) {
                        // 获奖奖励
                        val reward = nomination.award.getReward()
                        totalCashReward += reward.cashPrize
                        totalFansReward += reward.fansGain
                        totalReputationGain += reward.reputationGain
                        winnerGameIds.add(winner.gameId)
                        
                        // 记录获奖历史
                        val record = AwardRecord(
                            year = currentYear,
                            award = nomination.award,
                            gameId = winner.gameId,
                            gameName = winner.gameName,
                            isWinner = true,
                            rewards = reward
                        )
                        companyReputation = companyReputation.addAwardRecord(record)
                    }
                    
                    // 提名奖励（未获奖但进入前3）
                    nomination.nominees.forEach { nominee ->
                        if (nominee.isPlayerGame && nominee.gameId != winner?.gameId) {
                            val baseReward = nomination.award.getReward()
                            val nominationReward = AwardReward(
                                cashPrize = (baseReward.cashPrize * 0.2f).toInt(),
                                fansGain = (baseReward.fansGain * 0.2f).toLong(),
                                reputationGain = 10
                            )
                            
                            totalCashReward += nominationReward.cashPrize
                            totalFansReward += nominationReward.fansGain
                            totalReputationGain += nominationReward.reputationGain
                            
                            // 记录提名历史
                            val record = AwardRecord(
                                year = currentYear,
                                award = nomination.award,
                                gameId = nominee.gameId,
                                gameName = nominee.gameName,
                                isWinner = false,
                                rewards = nominationReward
                            )
                            companyReputation = companyReputation.addAwardRecord(record)
                        }
                    }
                }
                
                // 更新提名为最终结果
                currentYearNominations = finalNominations
                
                // 添加到历史记录（仅保留最近10年）
                gvaHistory = (finalNominations + gvaHistory).take(10 * 21) // 每年最多21个奖项
                
                // 应用奖励
                money = safeAddMoney(money, totalCashReward.toLong())
                fans += totalFansReward
                companyReputation = companyReputation.addReputation(totalReputationGain)
                
                // 更新获奖游戏的awards字段 - 性能优化：在后台线程计算
                games = withContext(Dispatchers.Default) {
                    games.map { game ->
                        if (game.id in winnerGameIds) {
                            val wonAwards = finalNominations
                                .filter { it.winner?.gameId == game.id }
                                .map { it.award }
                            game.copy(
                                awards = (game.awards + wonAwards).distinct(),
                                allDevelopmentEmployees = game.allDevelopmentEmployees
                            )
                        } else {
                            game
                        }
                    }
                }
                
                // 记录颁奖日期
                gvaAnnouncedDate = GameDate(currentYear, currentMonth, currentDay)
                
                val wonCount = winnerGameIds.size
                
                Log.d("MainActivity", "🏆 GVA：玩家获得${wonCount}个奖项，奖金${totalCashReward}，粉丝${totalFansReward}")
                
                // 设置颁奖对话框数据并显示
                gvaAwardYear = currentYear
                gvaAwardNominations = finalNominations
                gvaPlayerWonCount = wonCount
                gvaPlayerTotalReward = totalCashReward
                gvaPlayerFansGain = totalFansReward
                showGVAAwardDialog = true
                
                // 暂停游戏，让玩家查看颁奖结果
                isPaused = true
            }
            
            // 年终奖系统：12月31日触发年度总结和年终奖分发
            if (currentMonth == 12 && currentDay == 31 && currentYear != lastYearEndBonusYear) {
                // 计算年度统计数据 - 统计本年有收入的游戏数量（而非本年新发售的）
                val revenueData = RevenueManager.exportRevenueData()
                
                // 🔍 调试：输出所有收入数据
                Log.d("YearEnd", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d("YearEnd", "📊 年度总结数据调试 - ${currentYear}年")
                Log.d("YearEnd", "收入数据总条目数: ${revenueData.size}")
                revenueData.forEach { (gameId, revenue) ->
                    val recordsThisYear = revenue.dailySalesList.filter { dailySales ->
                        val recordCalendar = Calendar.getInstance()
                        recordCalendar.time = dailySales.date
                        val recordGameYear = recordCalendar.get(Calendar.YEAR)
                        recordGameYear == currentYear
                    }
                    val revenueThisYear = recordsThisYear.sumOf { it.revenue }
                    val isInherited = gameId.startsWith("inherited_")
                    Log.d("YearEnd", "  游戏: ${revenue.gameName} (ID=${gameId.take(20)}...)")
                    Log.d("YearEnd", "    类型: ${if (isInherited) "继承游戏" else "自研游戏"}")
                    Log.d("YearEnd", "    发售日期: ${revenue.releaseYear}年${revenue.releaseMonth}月${revenue.releaseDay}日")
                    Log.d("YearEnd", "    本年收入记录数: ${recordsThisYear.size}")
                    Log.d("YearEnd", "    本年总收入: ¥${revenueThisYear.toLong()}")
                }
                Log.d("YearEnd", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                
                val gamesReleasedThisYear = revenueData.values.count { revenue ->
                    // 检查该游戏在当年是否有收入记录
                    revenue.dailySalesList.any { dailySales ->
                        val recordCalendar = Calendar.getInstance()
                        recordCalendar.time = dailySales.date
                        val recordGameYear = recordCalendar.get(Calendar.YEAR)
                        recordGameYear == currentYear && dailySales.revenue > 0
                    }
                }
                
                // 计算年度总收入（从RevenueManager获取，统计所有已发售游戏在当年的收入）
                val totalRevenue = revenueData.values
                    .flatMap { revenue ->
                        revenue.dailySalesList.filter { dailySales ->
                            // 直接从recordDate中提取游戏内年份
                            // recordDate是用游戏内时间创建的，所以其中的YEAR字段就是游戏内年份
                            val recordCalendar = Calendar.getInstance()
                            recordCalendar.time = dailySales.date
                            val recordGameYear = recordCalendar.get(Calendar.YEAR)
                            recordGameYear == currentYear // 只统计当年的收入
                        }
                    }
                    .sumOf { it.revenue.toLong() } // 转换为Long
                
                // 计算年度总支出（员工薪资 + 服务器费用 + 开发成本）
                val totalSalary = allEmployees.sumOf { it.salary.toLong() } * 12L
                
                // 计算年度服务器费用（从服务器数据中获取）
                val totalServerCost = RevenueManager.exportRevenueData()
                    .values
                    .sumOf { revenue ->
                        // 获取该游戏的服务器信息
                        val serverInfo = RevenueManager.getGameServerInfo(revenue.gameId)
                        // 计算该游戏所有服务器的年度费用（12个月）
                        serverInfo.servers.filter { it.isActive }.sumOf { server ->
                            server.type.cost * 12L
                        }
                    }
                
                // 注意：开发费用已在创建游戏时扣除，不应在年度支出中重复计算
                val totalExpenses = totalSalary + totalServerCost
                val netProfit = totalRevenue - totalExpenses
                
                // 触发年终奖对话框（统计会在对话框内重新计算）
                showYearEndBonusDialog = true
                lastYearEndBonusYear = currentYear
                isPaused = true // 暂停游戏
                
                Log.d("MainActivity", "💰 年终奖：${currentYear}年总结 - 游戏${gamesReleasedThisYear}款，收入¥$totalRevenue，利润¥$netProfit")
            }
            
            // 每日检查：员工涨薪请求
            if (!showSalaryRequestDialog) {
                val employeeNeedingSalaryIncrease = allEmployees.firstOrNull { employee ->
                    employee.shouldRequestSalaryIncrease(currentYear, currentMonth, currentDay) &&
                    employee.requestedSalary == null
                }
                
                if (employeeNeedingSalaryIncrease != null) {
                    // 计算员工期望的薪资（基于技能等级）
                    val expectedSalary = employeeNeedingSalaryIncrease.calculateExpectedSalary(
                        employeeNeedingSalaryIncrease.salary
                    )
                    
                    // 更新员工的涨薪要求
                    val updatedEmployees = allEmployees.map { emp ->
                        if (emp.id == employeeNeedingSalaryIncrease.id) {
                            emp.copy(
                                requestedSalary = expectedSalary,
                                lastSalaryRequestYear = currentYear,
                                lastSalaryRequestMonth = currentMonth
                            )
                        } else {
                            emp
                        }
                    }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees)
                    
                    // 检查是否开启自动审批
                    if (autoApproveSalaryIncrease) {
                        // 自动审批：直接同意涨薪
                        val finalUpdatedEmployees = allEmployees.map { emp ->
                            if (emp.id == employeeNeedingSalaryIncrease.id) {
                                emp.copy(
                                    salary = expectedSalary,
                                    loyalty = (emp.loyalty + 10).coerceAtMost(100),
                                    requestedSalary = null,
                                    salaryRequestCount = emp.salaryRequestCount + 1
                                )
                            } else {
                                emp
                            }
                        }
                        allEmployees.clear()
                        allEmployees.addAll(finalUpdatedEmployees)
                        
                        Log.d("MainActivity", "✅ 自动审批：${employeeNeedingSalaryIncrease.name} (第${employeeNeedingSalaryIncrease.salaryRequestCount + 1}次涨薪) 薪资从¥${employeeNeedingSalaryIncrease.salary}涨到¥$expectedSalary")
                    } else {
                        // 手动审批：显示涨薪请求对话框（保存当前的涨薪次数）
                        salaryRequestEmployee = employeeNeedingSalaryIncrease.copy(
                            requestedSalary = expectedSalary,
                            lastSalaryRequestYear = currentYear,
                            lastSalaryRequestMonth = currentMonth,
                            salaryRequestCount = employeeNeedingSalaryIncrease.salaryRequestCount
                        )
                        showSalaryRequestDialog = true
                        isPaused = true // 暂停游戏
                        
                        Log.d("MainActivity", "💼 涨薪请求：${employeeNeedingSalaryIncrease.name} (第${employeeNeedingSalaryIncrease.salaryRequestCount + 1}次涨薪) 要求薪资从¥${employeeNeedingSalaryIncrease.salary}涨到¥$expectedSalary")
                    }
                }
            }
            
            // 每日检查：员工忠诚度过低触发离职和竞争对手争夺
            val employeesToRemove = mutableListOf<Employee>()
            allEmployees.forEach { employee ->
                if (employee.isLoyaltyLow() && !employee.isFounder) {
                    // 忠诚度过低，有概率离职或被竞争对手挖走
                    val leaveChance = Random.nextFloat()
                    if (leaveChance < 0.1f) { // 10%概率离职
                        employeesToRemove.add(employee)
                        
                        // 生成竞争对手挖角的新闻
                        val competitor = competitors.randomOrNull()
                        if (competitor != null) {
                            val news = CompetitorNews(
                                id = "competitor_${System.currentTimeMillis()}_${Random.nextInt()}",
                                title = "${competitor.name}挖走了${employee.name}",
                                content = "${employee.name}因对公司不满，被${competitor.name}以更高薪资挖走。",
                                type = NewsType.COMPANY_MILESTONE, // 使用公司里程碑类型
                                companyId = competitor.id,
                                companyName = competitor.name,
                                timestamp = System.currentTimeMillis(),
                                year = currentYear,
                                month = currentMonth,
                                day = currentDay
                            )
                            competitorNews = (competitorNews + news).takeLast(30)
                        }
                        
                        Log.d("MainActivity", "⚠️ 员工离职：${employee.name}因忠诚度过低（${employee.loyalty}）而离职")
                    }
                }
            }
            // 移除离职员工（使用安全的filter方式避免并发修改）
            if (employeesToRemove.isNotEmpty()) {
                try {
                    val employeeIdsToRemove = employeesToRemove.map { it.id }.toSet()
                    val updatedEmployees = allEmployees.filter { it.id !in employeeIdsToRemove }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees)
                    
                    // 同时从游戏中移除这些员工
                    games = games.map { game ->
                        game.copy(
                            assignedEmployees = game.assignedEmployees.filter { emp ->
                                emp.id !in employeeIdsToRemove
                            },
                            allDevelopmentEmployees = game.allDevelopmentEmployees
                        )
                    }
                    
                    Log.d("MainActivity", "成功移除${employeesToRemove.size}名离职员工")
                } catch (e: Exception) {
                    Log.e("MainActivity", "移除离职员工时发生异常", e)
                    e.printStackTrace()
                }
            }
            
            if (currentDay == 1) {
                // 检查是否破产（负债达到50万）
                if (money <= -500000L) {
                    isPaused = true
                    showBankruptcyDialog = true
                    Log.d("MainActivity", "公司破产：当前资金 ¥$money")
                }
            }
            
            // 更新游戏开发进度（分阶段系统）- 性能优化：在后台线程计算，减少主线程阻塞
            val updatedGames = withContext(Dispatchers.Default) {
                // 创建员工Map以提升查找效率（只需创建一次）
                val employeeMap = allEmployees.associateBy { it.id }
                
                games.map { game ->
                    if (!game.isCompleted && game.assignedEmployees.isNotEmpty()) {
                        val currentPhase = game.currentPhase
                        
                        // 检查当前阶段是否有足够的员工
                        if (!currentPhase.checkRequirements(game.assignedEmployees)) {
                            // 没有满足要求的员工，进度不增长
                            if (ENABLE_VERBOSE_GAME_LOGS) {
                                Log.w("MainActivity", "⚠️ 游戏${game.name}阶段${currentPhase.displayName}员工不足")
                            }
                            return@map game
                        }
                        
                        // 计算当前阶段的进度增长
                        val phaseProgressIncrease = currentPhase.calculateProgressSpeed(game.assignedEmployees)
                        val newPhaseProgress = (game.phaseProgress + phaseProgressIncrease).coerceAtMost(1.0f)
                        
                        // 优化：仅在详细日志模式或阶段完成时输出
                        if (ENABLE_VERBOSE_GAME_LOGS || newPhaseProgress >= 1.0f) {
                            Log.d("MainActivity", "📈 ${game.name}开发：阶段=${currentPhase.displayName}, 进度=${(newPhaseProgress * 100).toInt()}%, 员工=${game.assignedEmployees.size}人")
                        }
                        
                        // 检查当前阶段是否完成
                        if (newPhaseProgress >= 1.0f) {
                            // 当前阶段完成，进入下一阶段
                            val nextPhase = currentPhase.getNextPhase()
                            
                            // 累积当前阶段的员工到allDevelopmentEmployees（去重）
                            val updatedAllEmployees = (game.allDevelopmentEmployees + game.assignedEmployees)
                                .distinctBy { it.id } // 按ID去重，避免同一员工多次计入
                            
                            if (nextPhase != null) {
                                // 进入下一阶段
                                game.copy(
                                    currentPhase = nextPhase,
                                    phaseProgress = 0f,
                                    developmentProgress = when (nextPhase) {
                                        DevelopmentPhase.DESIGN -> 0f // 不应该发生
                                        DevelopmentPhase.ART_SOUND -> 0.33f // 需求文档完成
                                        DevelopmentPhase.PROGRAMMING -> 0.66f // 美术音效完成
                                    },
                                    assignedEmployees = emptyList(), // 清空当前阶段员工，让玩家重新分配
                                    allDevelopmentEmployees = updatedAllEmployees // 保存所有参与开发的员工
                                )
                            } else {
                                // 所有阶段完成，游戏开发完成
                                // 使用allDevelopmentEmployees计算评分
                                val gameWithAllEmployees = game.copy(
                                    assignedEmployees = updatedAllEmployees,
                                    allDevelopmentEmployees = updatedAllEmployees
                                )
                                val gameRating = GameRatingCalculator.calculateRating(gameWithAllEmployees)
                                game.copy(
                                    developmentProgress = 1.0f,
                                    phaseProgress = 1.0f,
                                    isCompleted = true,
                                    rating = gameRating.finalScore,
                                    gameRating = gameRating,
                                    releaseStatus = GameReleaseStatus.READY_FOR_RELEASE,
                                    assignedEmployees = emptyList(),
                                    allDevelopmentEmployees = updatedAllEmployees // 保存所有参与开发的员工
                                )
                            }
                        } else {
                            // 阶段未完成，更新阶段进度和总进度
                            val phaseWeight = 0.33f // 每个阶段占总进度的33%
                            val phaseBaseProgress = when (currentPhase) {
                                DevelopmentPhase.DESIGN -> 0f
                                DevelopmentPhase.ART_SOUND -> 0.33f
                                DevelopmentPhase.PROGRAMMING -> 0.66f
                            }
                            val newTotalProgress = phaseBaseProgress + (newPhaseProgress * phaseWeight)
                            
                            // 优化：使用已创建的Map提升查找效率
                            val updatedAssignedEmployees = game.assignedEmployees.map { assignedEmployee ->
                                employeeMap[assignedEmployee.id] ?: assignedEmployee
                            }
                            
                            game.copy(
                                phaseProgress = newPhaseProgress,
                                developmentProgress = newTotalProgress,
                                isCompleted = false,
                                assignedEmployees = updatedAssignedEmployees,
                                allDevelopmentEmployees = game.allDevelopmentEmployees // 兼容旧存档
                            )
                        }
                    } else {
                        game
                    }
                }
            }
            
            // 检查是否有游戏完成（需要在主线程更新状态）
            val completedGame = updatedGames.firstOrNull { it.isCompleted && !games.any { g -> g.id == it.id && g.isCompleted } }
            if (completedGame != null) {
                pendingRatingGame = completedGame
                showRatingDialog = true
            }
            
            // 一次性更新所有游戏（减少重组次数）
            games = updatedGames
            
            // 注意：已发售游戏的收益现在在每分钟更新中实时计算，这里不再重复计算
            // 每天结束时只推进更新任务进度（只有RELEASED状态）- 性能优化：批量处理，减少状态更新
            val releasedGamesForUpdate = games.filter { it.releaseStatus == GameReleaseStatus.RELEASED }
            if (releasedGamesForUpdate.isNotEmpty()) {
                // 性能优化：在后台线程批量处理所有更新任务
                val updatedGamesForTasks = withContext(Dispatchers.Default) {
                    // 创建员工Map以提升查找效率（只需创建一次）
                    val employeeMap = allEmployees.associateBy { it.id }
                    
                    releasedGamesForUpdate.map { releasedGame ->
                        // 更新游戏信息（商业模式和付费内容）
                        RevenueManager.updateGameInfo(
                            releasedGame.id,
                            releasedGame.businessModel,
                            releasedGame.monetizationItems
                        )
                        
                        // 在推进进度前先获取更新任务信息（因为完成后会被清除）
                        val completedTask = RevenueManager.getGameRevenue(releasedGame.id)?.updateTask
                        
                        // 若存在更新任务，根据已分配员工数量和技能等级推进进度
                        var employeesForUpdate = releasedGame.assignedEmployees
                        if (employeesForUpdate.isNotEmpty()) {
                            // 优化：使用已创建的Map提升查找效率
                            val updatedAssignedEmployees = employeesForUpdate.map { assignedEmployee ->
                                employeeMap[assignedEmployee.id] ?: assignedEmployee
                            }
                            
                            // 使用更新后的员工列表计算进度
                            employeesForUpdate = updatedAssignedEmployees
                        }
                        
                        val employeePoints = RevenueManager.calculateUpdateProgressPoints(employeesForUpdate)
                        val updateJustCompleted = RevenueManager.progressUpdateTask(releasedGame.id, employeePoints)
                        
                        // 返回需要更新的游戏数据
                        Triple(releasedGame, updateJustCompleted, completedTask)
                    }
                }
                
                // 在主线程批量更新游戏（减少重组次数）
                val updatedGamesMap = mutableMapOf<String, Game>()
                updatedGamesMap.putAll(games.associateBy { it.id })
                
                updatedGamesForTasks.forEach { (releasedGame, updateJustCompleted, completedTask) ->
                    
                    // 如果更新刚刚完成，版本号+0.1
                    if (updateJustCompleted) {
                        // 使用之前保存的任务信息
                        
                        // 创建游戏更新记录
                        val newUpdateHistory = if (completedTask != null) {
                            val updateNumber = (releasedGame.updateHistory ?: emptyList()).size + 1
                            val updateDate = GameDate(currentYear, currentMonth, currentDay)
                            val newVersion = releasedGame.version + 0.1f // 更新后的版本号
                            
                            // 生成玩家评论
                            val comments = CommentGenerator.generateComments(
                                updateContent = completedTask.features,
                                commentCount = Random.nextInt(5, 11)
                            )
                            
                            // 创建更新记录
                            val gameUpdate = GameUpdate(
                                updateNumber = updateNumber,
                                version = newVersion, // 保存更新后的版本号
                                updateDate = updateDate,
                                updateContent = completedTask.features,
                                announcement = completedTask.announcement,
                                comments = comments
                            )
                            
                            (releasedGame.updateHistory ?: emptyList()) + gameUpdate
                        } else {
                            releasedGame.updateHistory
                        }
                        
                        // 检查是否会自动创建下一个更新任务
                        var willCreateNewTask = false
                        if (releasedGame.autoUpdate) {
                            // 根据游戏类型生成更新选项
                            val autoUpdateFeatures = if (releasedGame.businessModel == BusinessModel.ONLINE_GAME) {
                                // 网络游戏：使用已启用的付费内容
                                releasedGame.monetizationItems
                                    .filter { it.isEnabled }
                                    .map { it.type.getUpdateContentName() }
                                    .distinct()
                            } else {
                                // 单机游戏：根据游戏主题获取推荐的付费内容类型作为更新内容
                                val recommendedItems = MonetizationConfig.getRecommendedItems(releasedGame.theme)
                                recommendedItems.map { it.getUpdateContentName() }
                            }
                            
                            // 如果有可用的更新内容，标记会创建新任务
                            willCreateNewTask = autoUpdateFeatures.isNotEmpty()
                        }
                        
                        // 如果会自动创建新任务，保留员工分配；否则清空员工分配
                        val updatedGame = releasedGame.copy(
                            version = releasedGame.version + 0.1f,
                            assignedEmployees = if (willCreateNewTask) releasedGame.assignedEmployees else emptyList(),
                            updateHistory = newUpdateHistory // 添加更新记录
                        )
                        updatedGamesMap[updatedGame.id] = updatedGame
                        
                        // 如果开启了自动更新，自动创建下一次更新任务
                        if (releasedGame.autoUpdate && willCreateNewTask) {
                            println("【自动更新】游戏《${releasedGame.name}》的更新已自动发布！版本升级至 V${String.format(Locale.getDefault(), "%.1f", updatedGame.version)}")
                            
                            // 根据游戏类型生成更新选项（重新计算，因为上面已经计算过了）
                            val autoUpdateFeatures = if (releasedGame.businessModel == BusinessModel.ONLINE_GAME) {
                                // 网络游戏：使用已启用的付费内容
                                releasedGame.monetizationItems
                                    .filter { it.isEnabled }
                                    .map { it.type.getUpdateContentName() }
                                    .distinct()
                            } else {
                                // 单机游戏：根据游戏主题获取推荐的付费内容类型作为更新内容
                                val recommendedItems = MonetizationConfig.getRecommendedItems(releasedGame.theme)
                                recommendedItems.map { it.getUpdateContentName() }
                            }
                            
                                // 自动更新使用默认公告
                                val autoAnnouncement = CommentGenerator.generateDefaultAnnouncement(autoUpdateFeatures)
                                RevenueManager.createUpdateTask(releasedGame.id, autoUpdateFeatures, autoAnnouncement)
                            println("【自动更新】已自动创建下一次更新任务，共${autoUpdateFeatures.size}项内容，员工将继续工作")
                        }
                    }
                }
                
                // 一次性更新所有游戏（减少重组次数）
                games = games.map { updatedGamesMap[it.id] ?: it }
            }
            
            // ===== 客诉处理流程 =====
            // 1. 先生成新客诉（实时生成）
            // 限制：如果活动客诉数量已达到上限（50个），则不再生成新客诉
            val activeComplaintCount = complaints.count { it.status != ComplaintStatus.COMPLETED }
            if (activeComplaintCount < 50) {
                val dailyNewComplaints = CustomerServiceManager.generateDailyComplaints(
                    games,
                    currentYear,
                    currentMonth,
                    currentDay
                )
                if (dailyNewComplaints.isNotEmpty()) {
                    complaints = complaints + dailyNewComplaints
                    Log.d("MainActivity", "每日生成 ${dailyNewComplaints.size} 个新客诉（当前活动客诉: ${activeComplaintCount + dailyNewComplaints.size}）")
                }
            } else {
                // 活动客诉数量已达上限，跳过生成
                if (activeComplaintCount >= 50 && activeComplaintCount % 10 == 0) {
                    // 每10个客诉记录一次日志，避免日志过多
                    Log.d("MainActivity", "⚠️ 活动客诉数量已达上限（${activeComplaintCount}个），暂停生成新客诉")
                }
            }
            
            // 2. 清理旧客诉并限制数量上限（每日清理一次，避免客诉累积过多）
            // 修复：传入当前年月，确保不会删除本月完成的客诉
            complaints = CustomerServiceManager.cleanupOldComplaints(complaints, currentYear, currentMonth)
            
            // 3. 自动处理模式：自动分配待处理的客诉（包括刚生成的新客诉）
            if (autoProcessComplaints) {
                val pendingCount = complaints.count { 
                    it.status == ComplaintStatus.PENDING && it.assignedEmployeeId == null 
                }
                if (pendingCount > 0) {
                    val (autoAssigned, assignedCount) = CustomerServiceManager.autoAssignComplaints(
                        complaints,
                        allEmployees
                    )
                    complaints = autoAssigned
                    if (assignedCount > 0) {
                        Log.d("MainActivity", "自动处理模式：自动分配 $assignedCount 个客诉")
                    }
                }
            }
            
            // 4. 每日处理客诉（传入当前日期以记录完成时间）
            val (updatedComplaints, _) = CustomerServiceManager.processDailyComplaints(
                complaints,
                allEmployees,
                currentYear,
                currentMonth,
                currentDay
            )
            complaints = updatedComplaints
            
            // 计算超时客诉造成的粉丝损失（优化：只遍历活动客诉）
            val fanLoss: Long = CustomerServiceManager.calculateOverdueFanLoss(
                complaints,
                currentYear,
                currentMonth,
                currentDay
            )
            if (fanLoss > 0) {
                fans = (fans - fanLoss).coerceAtLeast(0L)
                if (ENABLE_VERBOSE_GAME_LOGS) {
                    Log.d("MainActivity", "客诉超时：粉丝流失 -$fanLoss，当前粉丝: $fans")
                }
            }
            
            // 每日更新赛事 - 性能优化：在后台线程计算比赛进度，在主线程结算
            val tournamentUpdateResults = withContext(Dispatchers.Default) {
                games.map { game ->
                    val tournament = game.currentTournament
                    if (tournament != null && tournament.status != TournamentStatus.COMPLETED) {
                        val updatedTournament = TournamentManager.updateTournament(
                            tournament,
                            GameDate(currentYear, currentMonth, currentDay)
                        )
                        
                        // 检查是否刚完成
                        val isCompleted = updatedTournament.status == TournamentStatus.COMPLETED && 
                            tournament.status != TournamentStatus.COMPLETED
                        
                        // 返回更新结果：Pair(Triple(游戏, 更新后的赛事, 原赛事), 是否完成)
                        Pair(Triple(game, updatedTournament, tournament), isCompleted)
                    } else {
                        null
                    }
                }.filterNotNull()
            }
            
            // 在主线程批量结算完成的赛事
            val tournamentUpdatedGames = games.map { game ->
                val updateResult = tournamentUpdateResults.find { it.first.first.id == game.id }
                if (updateResult != null) {
                    val (triple, isCompleted) = updateResult
                    val (updatedGame, updatedTournament, _) = triple
                    if (isCompleted) {
                        // 结算完成的赛事
                        val revenueData = RevenueManager.getGameRevenue(updatedGame.id)
                        if (revenueData != null) {
                            val revenue = TournamentManager.calculateTournamentRevenue(
                                updatedTournament, updatedGame, revenueData
                            )
                            val (fansGained, playersGained, interestBonus) = TournamentManager.applyTournamentEffects(
                                updatedTournament, updatedGame, revenueData, fans
                            )
                            val (eventDesc, _) = TournamentManager.generateRandomEvent()
                            
                            money = safeAddMoney(money, revenue.totalRevenue.toLong())
                            fans += fansGained
                            
                            if (updatedGame.businessModel == BusinessModel.ONLINE_GAME) {
                                val currentRevenue = RevenueManager.getGameRevenue(updatedGame.id)
                                if (currentRevenue != null) {
                                    val newInterest = (currentRevenue.playerInterest + interestBonus).coerceIn(0.0, 100.0)
                                    val currentInterestMultiplier = when {
                                        currentRevenue.playerInterest >= 70.0 -> 1.0
                                        currentRevenue.playerInterest >= 50.0 -> 0.7
                                        currentRevenue.playerInterest >= 30.0 -> 0.4
                                        else -> 0.2
                                    }
                                    val newInterestMultiplier = when {
                                        newInterest >= 70.0 -> 1.0
                                        newInterest >= 50.0 -> 0.7
                                        newInterest >= 30.0 -> 0.4
                                        else -> 0.2
                                    }
                                    val currentActivePlayers = currentRevenue.totalRegisteredPlayers * 0.4 * currentInterestMultiplier
                                    val targetActivePlayers = currentActivePlayers + playersGained
                                    val registeredPlayersGained = if (newInterestMultiplier > 0) {
                                        ((targetActivePlayers / (0.4 * newInterestMultiplier)) - currentRevenue.totalRegisteredPlayers).toLong().coerceAtLeast(0L)
                                    } else {
                                        (playersGained * 2.5).toLong()
                                    }
                                    val newTotalRegistered = RevenueManager.safeAddRegisteredPlayers(
                                        currentRevenue.totalRegisteredPlayers,
                                        registeredPlayersGained
                                    )
                                    RevenueManager.updateGameRevenueAfterTournament(
                                        updatedGame.id,
                                        newInterest,
                                        newTotalRegistered
                                    )
                                }
                            }
                            
                            val completedTournament = updatedTournament.copy(
                                sponsorRevenue = revenue.sponsorRevenue,
                                broadcastRevenue = revenue.broadcastRevenue,
                                ticketRevenue = revenue.ticketRevenue,
                                fansGained = fansGained,
                                playersGained = playersGained,
                                interestBonus = interestBonus,
                                randomEvent = eventDesc
                            )
                            
                            val history = ((game.tournamentHistory ?: emptyList()) + completedTournament).takeLast(5)
                            
                            tournamentResult = completedTournament
                            showTournamentResultDialog = true
                            
                            game.copy(
                                currentTournament = null,
                                tournamentHistory = history,
                                allDevelopmentEmployees = game.allDevelopmentEmployees
                            )
                        } else {
                            game.copy(
                                currentTournament = updatedTournament,
                                allDevelopmentEmployees = game.allDevelopmentEmployees
                            )
                        }
                    } else {
                        // 更新进行中的赛事
                        game.copy(
                            currentTournament = updatedTournament,
                            allDevelopmentEmployees = game.allDevelopmentEmployees
                        )
                    }
                } else {
                    game
                }
            }
            
            games = tournamentUpdatedGames
            
            // 为活跃岗位生成应聘者（传入现有员工名字，确保应聘者名字唯一）
            val existingEmployeeNames = allEmployees.map { it.name }.toSet()
            JobPostingService.getInstance().generateApplicantsForActiveJobs(1, existingEmployeeNames)
            
            // 触发收益数据刷新
            revenueRefreshTrigger++
            
            // 触发岗位应聘者数据刷新
            jobPostingRefreshTrigger++
        }
    }
    
    // 拦截返回键，显示退出确认对话框
    BackHandler {
        showExitDialog = true
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // 深蓝色主色调
                        Color(0xFF7C3AED)  // 紫色渐变
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部信息流
            TopInfoBar(
                money = money,
                fans = fans,
                year = currentYear,
                month = currentMonth,
                day = currentDay,
                gameSpeed = gameSpeed,
                onSpeedChange = { gameSpeed = it },
                isPaused = isPaused,
                onPauseToggle = { isPaused = !isPaused },
                companyName = companyName,
                selectedLogo = selectedLogo,
                onSettingsClick = { showSettings = true },
                isSupporterUnlocked = isSupporterUnlocked,
                onShowFeatureLockedDialog = { showFeatureLockedDialog = true }
            )
            
            // 主要内容区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {

                    when (selectedTab) {
                        0 -> CompanyOverviewContent(
                            companyName = companyName,
                            selectedLogo = selectedLogo,
                            founder = founder,
                            allEmployees = allEmployees,
                            games = games,
                            money = money,
                            fans = fans,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            competitors = competitors,
                            competitorNews = competitorNews,
                            onSecretaryChatClick = { showSecretaryChat = true },
                            revenueRefreshTrigger = revenueRefreshTrigger // 传递收益刷新触发器
                        )
                        1 -> EmployeeManagementContent(
                            allEmployees = allEmployees,
                            onEmployeesUpdate = { updatedEmployees -> 
                                try {
                                    Log.d("MainActivity", "📞 onEmployeesUpdate回调: ${updatedEmployees.size} 个员工")
                                    Log.d("MainActivity", "📞 回调中员工名单: ${updatedEmployees.joinToString { it.name }}")
                                    Log.d("MainActivity", "📞 当前allEmployees大小: ${allEmployees.size}")
                                    
                                    // 使用协程确保在主线程执行，避免并发修改
                                    coroutineScope.launch(Dispatchers.Main) {
                                        try {
                                            Log.d("MainActivity", "🔄 开始更新员工列表")
                                            Log.d("MainActivity", "🔄 更新前: ${allEmployees.size} 个员工")
                                            
                                            // 批量更新（避免并发修改）
                                            val employeesList = updatedEmployees.toList()
                                            allEmployees.clear()
                                            allEmployees.addAll(employeesList)
                                            
                                            Log.d("MainActivity", "✅ 更新完成: ${allEmployees.size} 个员工")
                                            Log.d("MainActivity", "✅ 员工名单: ${allEmployees.joinToString { it.name }}")
                                        } catch (e: ConcurrentModificationException) {
                                            Log.e("MainActivity", "❌ 并发修改异常，重试更新", e)
                                            // 重试一次
                                            try {
                                                val employeesList = updatedEmployees.toList()
                                                allEmployees.clear()
                                                allEmployees.addAll(employeesList)
                                                Log.d("MainActivity", "✅ 重试更新成功")
                                            } catch (e2: Exception) {
                                                Log.e("MainActivity", "❌ 重试更新失败", e2)
                                                e2.printStackTrace()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "❌ 更新员工列表失败", e)
                                            e.printStackTrace()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "📞 onEmployeesUpdate回调时发生异常", e)
                                    e.printStackTrace()
                                }
                            },
                            money = money,
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney },
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            jobPostingRefreshTrigger = jobPostingRefreshTrigger,
                            onPauseGame = { isPaused = true },
                            onResumeGame = { isPaused = false },
                            isSupporterUnlocked = isSupporterUnlocked,
                            onShowFeatureLockedDialog = { showFeatureLockedDialog = true }
                        )
                        2 -> ProjectManagementWrapper(
                            games = games,
                            onGamesUpdate = { updatedGames -> games = updatedGames },
                            founder = founder,
                            allEmployees = allEmployees,
                            refreshTrigger = revenueRefreshTrigger,
                            onReleaseGame = { game ->
                                // 触发发售对话框
                                pendingReleaseGame = game
                                showReleaseDialog = true
                            },
                            onAbandonGame = { game ->
                                // 触发废弃确认对话框
                                pendingAbandonGame = game
                                showAbandonDialog = true
                            },
                            selectedProjectType = selectedProjectType,
                            onProjectTypeChange = { newType -> selectedProjectType = newType },
                            money = money,
                            fans = fans,
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney },
                            onFansUpdate = { updatedFans -> fans = updatedFans },
                            complaints = complaints,
                            onComplaintsUpdate = { updatedComplaints -> complaints = updatedComplaints },
                            autoProcessComplaints = autoProcessComplaints,
                            onAutoProcessToggle = { enabled -> autoProcessComplaints = enabled },
                            autoPromotionThreshold = autoPromotionThreshold,
                            onAutoPromotionThresholdUpdate = { threshold ->
                                // 更新本地状态中的阈值
                                autoPromotionThreshold = threshold
                            },
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            ownedIPs = ownedIPs,
                            onPauseGame = { isPaused = true },
                            onResumeGame = { isPaused = false },
                            isPaused = isPaused,
                            isSupporterUnlocked = isSupporterUnlocked,
                            onShowFeatureLockedDialog = { showFeatureLockedDialog = true },
                            onShowAutoProcessInfoDialog = { },
                            onShowAutoUpdateInfoDialog = { }
                        )
                        3 -> CompetitorContent(
                            saveData = SaveData(
                                companyName = companyName,
                                companyLogo = selectedLogo,
                                founderName = founderName,
                                founderProfession = founderProfession,
                                money = money,
                                fans = fans,
                                currentYear = currentYear,
                                currentMonth = currentMonth,
                                currentDay = currentDay,
                                allEmployees = allEmployees,
                                games = games,
                                competitors = competitors,
                                competitorNews = competitorNews,
                                serverData = RevenueManager.exportServerData(),
                                revenueData = RevenueManager.exportRevenueData(),
                                ownedIPs = ownedIPs, // 传递拥有的IP列表
                                subsidiaries = subsidiaries // 传递子公司列表
                            ),
                            gameSpeed = gameSpeed,
                            onAcquisitionSuccess = { acquiredCompany: CompetitorCompany, finalPrice: Long, _: Long, fansGain: Long, inheritedIPs: List<GameIP> ->
                                // 扣除收购费用
                                money = safeAddMoney(money, -finalPrice)
                                
                                // 增加粉丝
                                fans += fansGain
                                
                                // ✅ 将被收购公司转换为子公司
                                val newSubsidiary = SubsidiaryManager.createSubsidiary(
                                    company = acquiredCompany,
                                    acquisitionPrice = finalPrice,
                                    acquisitionDate = GameDate(currentYear, currentMonth, currentDay)
                                )
                                subsidiaries = subsidiaries + newSubsidiary
                                
                                // 移除被收购的公司
                                competitors = competitors.filter { it.id != acquiredCompany.id }
                                
                                // 将获得的IP添加到玩家的IP库
                                ownedIPs = ownedIPs + inheritedIPs
                                
                                // 统计收购公司数量
                                totalAcquiredCompanies++
                                
                                Log.d("MainActivity", "收购成功：${acquiredCompany.name}转为子公司")
                                Log.d("MainActivity", "  - 继承${acquiredCompany.games.size}款游戏")
                                Log.d("MainActivity", "  - 获得${inheritedIPs.size}个IP")
                                Log.d("MainActivity", "  - 估算员工${newSubsidiary.estimatedEmployeeCount}人")
                                inheritedIPs.forEach { ip: GameIP ->
                                    Log.d("MainActivity", "  - IP: ${ip.name} (${ip.getIPLevel()}, 评分${ip.originalRating}, 加成${(ip.calculateIPBonus() * 100).toInt()}%)")
                                }
                                
                                // 检查是否收购了所有竞争对手（9家全部收购）
                                if (competitors.isEmpty()) {
                                    Log.d("MainActivity", "🏆 挑战完成：已收购所有竞争对手！")
                                    showChallengeCompleteDialog = true
                                    isPaused = true
                                }
                                
                                // 生成收购新闻
                                competitorNews = (listOf(
                                    CompetitorNews(
                                        id = "news_${System.currentTimeMillis()}_${Random.nextInt()}",
                                        title = "${companyName}成功收购${acquiredCompany.name}！",
                                        content = "${companyName}以${formatMoney(finalPrice)}的价格成功收购了${acquiredCompany.name}，" +
                                                "该公司已转为子公司继续运营，拥有${acquiredCompany.games.size}款游戏。" +
                                                "同时获得了${inheritedIPs.size}个游戏IP。这是游戏行业的重大并购事件。",
                                        type = NewsType.COMPANY_MILESTONE,
                                        companyId = -1,
                                        companyName = companyName,
                                        year = currentYear,
                                        month = currentMonth,
                                        day = currentDay
                                    )
                                ) + competitorNews).take(30)
                            },
                            onAIWin = { acquirer: CompetitorCompany, acquired: CompetitorCompany, price: Long ->
                                // AI竞争对手收购成功
                                
                                // 1. 移除被收购的公司
                                competitors = competitors.filter { it.id != acquired.id }
                                
                                // 2. 更新收购方公司的数据（AI收购不获得IP，只增加市值和粉丝）
                                val (marketValueGain, fansGain, _) = 
                                    CompetitorManager.completeAcquisition(
                                        targetCompany = acquired,
                                        finalPrice = price,
                                        acquiredYear = currentYear,
                                        acquiredMonth = currentMonth
                                    )
                                
                                competitors = competitors.map { company ->
                                    if (company.id == acquirer.id) {
                                        // 更新收购方：只增加市值、粉丝（AI不继承游戏或IP）
                                        company.copy(
                                            marketValue = company.marketValue + marketValueGain,
                                            fans = company.fans + fansGain
                                        )
                                    } else {
                                        company
                                    }
                                }
                                
                                // 3. 生成收购新闻
                                competitorNews = (listOf(
                                    CompetitorNews(
                                        id = "news_${System.currentTimeMillis()}_${Random.nextInt()}",
                                        title = "${acquirer.name}成功收购${acquired.name}！",
                                        content = "${acquirer.name}以${formatMoney(price)}的价格成功收购了${acquired.name}。这是游戏行业的一次重大并购事件。",
                                        type = NewsType.COMPANY_MILESTONE,
                                        companyId = acquirer.id,
                                        companyName = acquirer.name,
                                        year = currentYear,
                                        month = currentMonth,
                                        day = currentDay
                                    )
                                ) + competitorNews).take(30)
                            }
                        )
                        4 -> TournamentScreen(
                            games = games,
                            revenueDataMap = RevenueManager.exportRevenueData(),
                            currentDate = GameDate(currentYear, currentMonth, currentDay),
                            money = money,
                            fans = fans,
                            competitors = competitors,
                            initialTab = tournamentInitialTab,
                            onHostTournament = { gameId, tournamentType ->
                                // 举办赛事
                                val game = games.find { it.id == gameId }
                                if (game != null) {
                                    val tournament = TournamentManager.createTournament(
                                        game, 
                                        tournamentType, 
                                        GameDate(currentYear, currentMonth, currentDay)
                                    )
                                    
                                    // 扣除资金
                                    money = safeAddMoney(money, -tournament.investment)
                                    
                                    // 更新游戏
                                    games = games.map { g ->
                                        if (g.id == gameId) {
                                            g.copy(
                                                currentTournament = tournament.copy(status = TournamentStatus.ONGOING),
                                                lastTournamentDate = GameDate(currentYear, currentMonth, currentDay)
                                            )
                                        } else {
                                            g
                                        }
                                    }
                                    
                                    messageText = "成功举办${tournament.type.displayName}，投入${formatMoney(tournament.investment)}"
                                    showMessage = true
                                }
                            }
                        )
                        5 -> ServerManagementContent(
                            games = games,
                            onPurchaseServer = { serverType ->
                                // 购买服务器到公共池（不立即扣费，按购买日期每30天扣费）
                                val publicPoolId = "SERVER_PUBLIC_POOL"
                                RevenueManager.addServerToGame(
                                    gameId = publicPoolId,
                                    serverType = serverType,
                                    purchaseYear = currentYear,
                                    purchaseMonth = currentMonth,
                                    purchaseDay = currentDay
                                )
                            }
                        )
                        6 -> GVAScreen(
                            saveData = SaveData(
                                money = money,
                                fans = fans,
                                allEmployees = allEmployees.toList(),
                                games = games,
                                currentYear = currentYear,
                                currentMonth = currentMonth,
                                currentDay = currentDay,
                                competitors = competitors,
                                competitorNews = competitorNews,
                                companyReputation = companyReputation,
                                gvaHistory = gvaHistory,
                                currentYearNominations = currentYearNominations,
                                gvaAnnouncedDate = gvaAnnouncedDate,
                                ownedIPs = ownedIPs // 传递拥有的IP列表
                            ),
                            onBack = { selectedTab = 0 }
                        )
                    }
            }
            
            // 底部导航栏 - 使用优化版本（字体加粗+黑色）
            // 在GVA界面时隐藏底部导航栏
            if (selectedTab != 6) {
                EnhancedBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { newTab ->
                        // 调试：记录标签切换
                        Log.d("GameScreen", "🔄 切换标签: $selectedTab -> $newTab, isPaused=$isPaused, gameSpeed=$gameSpeed")
                        selectedTab = newTab
                    },
                    pendingApplicantsCount = pendingApplicantsCount,
                    pendingAssignmentCount = pendingAssignmentCount,
                    onTournamentClick = { showTournamentMenu = true },
                    onCompetitorClick = { showCompetitorMenu = true }
                )
            }
        }
        
        // 赛事菜单
        if (showTournamentMenu) {
            TournamentMenuDialog(
                onDismiss = { showTournamentMenu = false },
                onTournamentManagement = {
                    tournamentInitialTab = 0
                    selectedTab = 4
                    showTournamentMenu = false
                },
                onGVAConference = {
                    selectedTab = 6
                    showTournamentMenu = false
                }
            )
        }
        
        // 竞争对手菜单
        if (showCompetitorMenu) {
            CompetitorMenuDialog(
                onDismiss = { showCompetitorMenu = false },
                onCompetitorManagement = {
                    selectedTab = 3
                    showCompetitorMenu = false
                },
                onSubsidiaryManagement = {
                    // 显示子公司管理界面
                    showSubsidiaryManagement = true
                    showCompetitorMenu = false
                }
            )
        }
        
        // 子公司管理界面
        if (showSubsidiaryManagement) {
            SubsidiaryManagementScreen(
                subsidiaries = subsidiaries,
                onSubsidiaryUpdate = { updatedSubsidiary ->
                    subsidiaries = subsidiaries.map { sub ->
                        if (sub.id == updatedSubsidiary.id) {
                            updatedSubsidiary
                        } else {
                            sub
                        }
                    }
                },
                onDismiss = {
                    showSubsidiaryManagement = false
                },
                // TopInfoBar参数
                money = money,
                fans = fans,
                year = currentYear,
                month = currentMonth,
                day = currentDay,
                gameSpeed = gameSpeed,
                onSpeedChange = { newSpeed -> gameSpeed = newSpeed },
                onPauseToggle = { isPaused = !isPaused },
                isPaused = isPaused,
                onSettingsClick = { showSettings = true },
                isSupporterUnlocked = isSupporterUnlocked,
                onShowFeatureLockedDialog = { showFeatureLockedDialog = true }
            )
        }
        
        // 消息弹窗
        if (showMessage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showMessage = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = messageText,
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showMessage = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            )
                        ) {
                            Text(
                                text = "确定",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // 游戏发售价格设置对话框
        if (showReleaseDialog && pendingReleaseGame != null) {
            GameReleaseDialog(
                game = pendingReleaseGame!!,
                onDismiss = {
                    showReleaseDialog = false
                    pendingReleaseGame = null
                },
                onConfirmRelease = { price ->
                    // 更新游戏状态为已发售
                    games = games.map { existingGame ->
                        if (existingGame.id == pendingReleaseGame!!.id) {
                            val releasedGame = existingGame.copy(
                                releaseStatus = GameReleaseStatus.RELEASED,
                                releasePrice = price,
                                releaseYear = currentYear,
                                releaseMonth = currentMonth,
                                releaseDay = currentDay
                            )
                            
                            // 检查是否是子公司继承的游戏（ID以"inherited_"开头）
                            val isInheritedGame = releasedGame.id.startsWith("inherited_")
                            
                            if (isInheritedGame) {
                                // 子公司游戏：收益数据已在收购时初始化，只需更新价格（不覆盖历史数据）
                                val existingRevenue = RevenueManager.getGameRevenue(releasedGame.id)
                                if (existingRevenue != null) {
                                    // 只更新发售价格，保留所有历史数据和发售日期
                                    RevenueManager.updateGamePrice(releasedGame.id, price.toDouble())
                                    Log.d("MainActivity", "✓ 子公司游戏 ${releasedGame.name} 手动发售，更新价格为¥${price}，保留历史数据（发售日期：${existingRevenue.releaseYear}年${existingRevenue.releaseMonth}月${existingRevenue.releaseDay}日）")
                                }
                            } else {
                                // 普通游戏：为已发售游戏初始化收益数据（空数据，等待日常循环累加）
                                RevenueManager.generateRevenueData(
                                    gameId = releasedGame.id,
                                    gameName = releasedGame.name,
                                    releasePrice = price.toDouble(),
                                    daysOnMarket = 0, // 初始化为空，让日常循环来累加收益
                                    releaseYear = currentYear,
                                    releaseMonth = currentMonth,
                                    releaseDay = currentDay,
                                    promotionIndex = releasedGame.promotionIndex
                                )
                            }
                            
                            // 更新游戏信息（商业模式和付费内容）
                            // 修复：如果付费内容没有设置价格，自动使用推荐价格
                            val monetizationItemsWithPrices = if (releasedGame.businessModel == BusinessModel.ONLINE_GAME) {
                                releasedGame.monetizationItems.map { item ->
                                    if (item.price == null || item.price <= 0) {
                                        // 使用推荐价格
                                        item.copy(price = item.type.getRecommendedPrice())
                                    } else {
                                        item
                                    }
                                }
                            } else {
                                releasedGame.monetizationItems
                            }
                            
                            RevenueManager.updateGameInfo(
                                releasedGame.id,
                                releasedGame.businessModel,
                                monetizationItemsWithPrices
                            )
                            
                            // 更新游戏IP信息（用于销量加成）
                            RevenueManager.updateGameIP(releasedGame.id, releasedGame.fromIP)
                            
                            // 同时更新游戏对象中的付费内容价格
                            releasedGame.copy(monetizationItems = monetizationItemsWithPrices)
                        } else {
                            existingGame
                        }
                    }
                    
                    // 关闭发售对话框
                    showReleaseDialog = false
                    pendingReleaseGame = null
                    
                    // 自动切换到"已发售"界面，方便玩家查看新发售的游戏
                    selectedProjectType = ProjectDisplayType.RELEASED
                }
            )
        }
        
        // 游戏评分展示对话框
        if (showRatingDialog && pendingRatingGame != null) {
            GameRatingDialog(
                gameRating = pendingRatingGame!!.gameRating!!,
                gameName = pendingRatingGame!!.name,
                onDismiss = {
                    // 评分对话框关闭时，只有当游戏还未发售时才更新状态为RATED
                    games = games.map { existingGame ->
                        if (existingGame.id == pendingRatingGame!!.id) {
                            // 检查游戏是否已经发售（RELEASED状态）
                            val currentStatus = existingGame.releaseStatus
                            val newStatus = if (currentStatus == GameReleaseStatus.RELEASED) {
                                // 已发售的游戏保持RELEASED状态，不要改回RATED
                                GameReleaseStatus.RELEASED
                            } else {
                                // 未发售的游戏设置为RATED（这种情况理论上不应该发生）
                                GameReleaseStatus.RATED
                            }
                            
                            val ratedGame = existingGame.copy(
                                releaseStatus = newStatus
                            )
                            
                            // 根据评分更新收益数据
                            RevenueManager.updateRevenueBasedOnRating(
                                gameId = ratedGame.id,
                                rating = ratedGame.gameRating?.finalScore ?: 5.0f
                            )
                            
                            ratedGame
                        } else {
                            existingGame
                        }
                    }
                    
                    // 根据游戏评分更新粉丝数（调整为更平衡的数值）
                    val finalRating = pendingRatingGame!!.gameRating?.finalScore ?: 5.0f
                    val fansChange = when {
                        finalRating >= 9.0f -> {
                            // 评分>=9：神作级别（8000-20000）
                            (8000..20000).random().toLong()
                        }
                        finalRating >= 8.0f -> {
                            // 评分>=8：优秀作品（3000-10000）
                            (3000..10000).random().toLong()
                        }
                        finalRating >= 6.5f -> {
                            // 评分>=6.5：中等偏上（1000-4000）
                            (1000..4000).random().toLong()
                        }
                        finalRating >= 5.0f -> {
                            // 评分>=5：及格水平（500-2000）
                            (500..2000).random().toLong()
                        }
                        else -> {
                            // 评分<5：口碑崩塌（-3000到-1000）
                            (-3000..-1000).random().toLong()
                        }
                    }
                    fans = (fans + fansChange).coerceAtLeast(0L) // 粉丝数不能为负
                    
                    Log.d("MainActivity", "游戏发布-评分: $finalRating, 粉丝变化: $fansChange, 当前粉丝: $fans")
                    
                    // 评分对话框关闭后，如果游戏还未发售，则显示发售对话框
                    val currentGame = games.find { it.id == pendingRatingGame!!.id }
                    val isGameReleased = currentGame?.releaseStatus == GameReleaseStatus.RELEASED
                    val isReadyForRelease = currentGame?.releaseStatus == GameReleaseStatus.READY_FOR_RELEASE || 
                                           currentGame?.releaseStatus == GameReleaseStatus.RATED
                    
                    showRatingDialog = false
                    
                    if (!isGameReleased && isReadyForRelease) {
                        // 游戏还未发售，显示发售对话框
                        pendingReleaseGame = pendingRatingGame
                        showReleaseDialog = true
                    }
                    
                    pendingRatingGame = null
                }
            )
        }
        
        // 废弃游戏确认对话框
        if (showAbandonDialog && pendingAbandonGame != null) {
            AlertDialog(
                onDismissRequest = {
                    showAbandonDialog = false
                    pendingAbandonGame = null
                },
                title = {
                    Text(
                        text = "⚠️ 确认废弃项目",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "确定要废弃项目「${pendingAbandonGame!!.name}」吗？",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 如果是开发中的游戏，显示不同的提示
                        if (pendingAbandonGame!!.releaseStatus == GameReleaseStatus.DEVELOPMENT) {
                            Text(
                                text = "游戏还在开发阶段，废弃不会产生任何费用",
                                fontSize = 13.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "开发成本：${formatMoney(pendingAbandonGame!!.developmentCost)}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "返还金额：${formatMoney((pendingAbandonGame!!.developmentCost * 0.8).toLong())} (80%)",
                                fontSize = 13.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // 返还80%开发费用
                            val refund = (pendingAbandonGame!!.developmentCost * 0.8).toLong()
                            money = safeAddMoney(money, refund)
                            
                            // 从游戏列表中移除
                            games = games.filter { it.id != pendingAbandonGame!!.id }
                            
                            // 显示提示消息
                            messageText = "已废弃项目「${pendingAbandonGame!!.name}」，返还 ${formatMoney(refund)}"
                            showMessage = true
                            
                            showAbandonDialog = false
                            pendingAbandonGame = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text("确认废弃")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showAbandonDialog = false
                            pendingAbandonGame = null
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
        
        // 退出游戏确认对话框
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = {
                    showExitDialog = false
                },
                title = {
                    Text(
                        text = "⚠️ 退出游戏",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "确定要退出游戏吗？\n\n未保存的进度将会丢失！",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // 退出应用
                            activity.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text("确认退出")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showExitDialog = false
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
        
        // 破产对话框
        if (showBankruptcyDialog) {
            AlertDialog(
                onDismissRequest = {
                    // 破产对话框不允许关闭，必须选择一个选项
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💸",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "公司破产",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "负债已达到 ¥50万！",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "当前资金：¥${formatMoney(money)}",
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "公司已无力继续经营...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showBankruptcyDialog = false
                                navController.navigate("continue")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("读取存档", fontSize = 16.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                showBankruptcyDialog = false
                                navController.navigate("game_setup") {
                                    popUpTo("game") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF10B981)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重新开始", fontSize = 16.sp)
                        }
                    }
                },
                dismissButton = null
            )
        }
        
        // 子公司资金不足对话框
        if (showSubsidiaryBankruptDialog && bankruptSubsidiary != null) {
            AlertDialog(
                onDismissRequest = { /* 不允许关闭，必须选择一个选项 */ },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💸",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "子公司资金不足",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "子公司「${bankruptSubsidiary!!.name}」资金已归零！",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "当前资金：¥${formatMoney(bankruptSubsidiary!!.cashBalance)}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "月度支出：¥${formatMoney(bankruptSubsidiary!!.monthlyExpense)}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "月度收入：¥${formatMoney(bankruptSubsidiary!!.monthlyRevenue)}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "您可以选择注入资金维持运营，或解散公司止损。",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 注入金额输入框
                        Text(
                            text = "注入金额（元）",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = injectionAmountInput,
                            onValueChange = { input ->
                                // 只允许输入数字
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    injectionAmountInput = input
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "请输入注入金额",
                                    color = Color.White.copy(alpha = 0.3f)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF10B981)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 显示玩家当前资金
                        Text(
                            text = "💰 您的可用资金：¥${formatMoney(money)}",
                            fontSize = 13.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 注入资金按钮
                        Button(
                            onClick = {
                                // 获取玩家输入的金额
                                val inputAmount = injectionAmountInput.toLongOrNull()
                                
                                if (inputAmount == null || inputAmount <= 0) {
                                    messageText = "请输入有效的注入金额"
                                    showMessage = true
                                } else if (money < inputAmount) {
                                    messageText = "资金不足！需要¥${formatMoney(inputAmount)}，当前仅有¥${formatMoney(money)}"
                                    showMessage = true
                                } else {
                                    // 扣除玩家资金
                                    money = safeAddMoney(money, -inputAmount)
                                    // 更新子公司资金
                                    subsidiaries = subsidiaries.map { sub ->
                                        if (sub.id == bankruptSubsidiary!!.id) {
                                            sub.copy(cashBalance = inputAmount)
                                        } else {
                                            sub
                                        }
                                    }
                                    messageText = "已向${bankruptSubsidiary!!.name}注入¥${formatMoney(inputAmount)}"
                                    showMessage = true
                                    Log.d("MainActivity", "💰 注入资金: ${bankruptSubsidiary!!.name} +¥${inputAmount}")
                                    
                                    // 清空输入框
                                    injectionAmountInput = ""
                                    showSubsidiaryBankruptDialog = false
                                    bankruptSubsidiary = null
                                    isPaused = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            enabled = injectionAmountInput.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "注入资金",
                                fontSize = 16.sp
                            )
                        }
                        
                        // 解散公司按钮
                        OutlinedButton(
                            onClick = {
                                // 移除子公司
                                subsidiaries = subsidiaries.filter { it.id != bankruptSubsidiary!!.id }
                                messageText = "${bankruptSubsidiary!!.name}已解散"
                                showMessage = true
                                Log.d("MainActivity", "🏭 解散子公司: ${bankruptSubsidiary!!.name}")
                                
                                // 清空输入框
                                injectionAmountInput = ""
                                showSubsidiaryBankruptDialog = false
                                bankruptSubsidiary = null
                                isPaused = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("解散公司", fontSize = 16.sp)
                        }
                    }
                },
                dismissButton = null,
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
        
        // 设置界面覆盖层
        if (showSettings) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable(enabled = false) {} // 阻止点击事件穿透
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 顶部栏（带返回按钮）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 返回按钮
                        IconButton(
                            onClick = { showSettings = false },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text(
                                text = "←",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "游戏设置",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 设置内容
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        InGameSettingsContent(
                            navController = navController,
                            money = money,
                            fans = fans,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            companyName = companyName,
                            selectedLogo = selectedLogo,
                            founderName = founderName,
                            founderProfession = founderProfession,
                            games = games,
                            allEmployees = allEmployees,
                            competitors = competitors,
                            competitorNews = competitorNews,
                            complaints = complaints,
                            autoProcessComplaints = autoProcessComplaints,
                            autoPromotionThreshold = autoPromotionThreshold,
                            autoApproveSalaryIncrease = autoApproveSalaryIncrease,
                            onAutoApproveSalaryToggle = { enabled -> autoApproveSalaryIncrease = enabled },
                            unlockedAchievements = unlockedAchievements,
                            completedTutorials = tutorialState.getCompletedTutorialsForSave(),
                            skipTutorial = tutorialState.skipTutorial.value,
                            companyReputation = companyReputation,
                            gvaHistory = gvaHistory,
                            currentYearNominations = currentYearNominations,
                            gvaAnnouncedDate = gvaAnnouncedDate,
                            ownedIPs = ownedIPs, // 传递拥有的IP列表
                            subsidiaries = subsidiaries, // 传递子公司列表
                            gmModeEnabled = gmModeEnabled,
                            onGMToggle = { enabled -> gmModeEnabled = enabled },
                            autoSaveEnabled = autoSaveEnabled,
                            autoSaveInterval = autoSaveInterval,
                            lastAutoSaveDay = lastAutoSaveDay,
                            onAutoSaveEnabledToggle = { enabled -> autoSaveEnabled = enabled },
                            onAutoSaveIntervalChange = { interval -> autoSaveInterval = interval },
                            usedRedeemCodes = usedRedeemCodes,
                            onUsedRedeemCodesUpdate = { updatedCodes -> usedRedeemCodes = updatedCodes },
                            isSupporterUnlocked = isSupporterUnlocked,
                            onShowFeatureLockedDialog = { showFeatureLockedDialog = true },
                            onShowAutoSaveInfoDialog = { },
                            onShowAutoApproveInfoDialog = { },
                            onMaxEmployees = {
                                // 一键将所有员工技能设置为5级
                                val maxedEmployees = allEmployees.map { employee ->
                                    employee.copy(
                                        skillDevelopment = 5,
                                        skillDesign = 5,
                                        skillArt = 5,
                                        skillMusic = 5,
                                        skillService = 5
                                    )
                                }
                                allEmployees.clear()
                                allEmployees.addAll(maxedEmployees)
                            },
                            onAddMoney = {
                                // 一键增加1000万
                                money = safeAddMoney(money, 10000000L)
                            },
                            onCreateTopEmployees = { skillLevel ->
                                // 智能调整模式：优先修改现有员工等级，不足时才新增
                                val existingNames = allEmployees.map { it.name }.toSet().toMutableSet()
                                val maxId = allEmployees.maxOfOrNull { it.id } ?: 0
                                val updatedEmployees = mutableListOf<Employee>()
                                val newEmployees = mutableListOf<Employee>()
                                
                                // 职位列表
                                val positions = listOf("程序员", "策划师", "美术师", "音效师", "客服")
                                
                                // 根据技能等级计算薪资（等级 × 10000 + 5000）
                                val baseSalary = skillLevel * 10000 + 5000
                                
                                // 为每个职位处理员工（优先修改现有，不足时新增）
                                for (position in positions) {
                                    // 找到该职位的现有员工（排除创始人）
                                    val existingForPosition = allEmployees.filter { 
                                        it.position == position && !it.isFounder 
                                    }.take(6)
                                    
                                    val existingCount = existingForPosition.size
                                    val needNewCount = 6 - existingCount
                                    
                                    // 修改现有员工的等级
                                    for (existingEmp in existingForPosition) {
                                        val updatedEmp = when (position) {
                                            "程序员" -> existingEmp.copy(
                                                skillDevelopment = skillLevel,
                                                skillDesign = 0,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = baseSalary
                                            )
                                            "策划师" -> existingEmp.copy(
                                                skillDevelopment = 0,
                                                skillDesign = skillLevel,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = baseSalary
                                            )
                                            "美术师" -> existingEmp.copy(
                                                skillDevelopment = 0,
                                                skillDesign = 0,
                                                skillArt = skillLevel,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = baseSalary
                                            )
                                            "音效师" -> existingEmp.copy(
                                                skillDevelopment = 0,
                                                skillDesign = 0,
                                                skillArt = 0,
                                                skillMusic = skillLevel,
                                                skillService = 0,
                                                salary = baseSalary
                                            )
                                            "客服" -> existingEmp.copy(
                                                skillDevelopment = 0,
                                                skillDesign = 0,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = skillLevel,
                                                salary = baseSalary
                                            )
                                            else -> existingEmp
                                        }
                                        updatedEmployees.add(updatedEmp)
                                    }
                                    
                                    // 如果数量不足6个，新增员工
                                    repeat(needNewCount) {
                                        val employeeName = com.example.yjcy.service.TalentMarketService.generateUniqueName(existingNames)
                                        existingNames.add(employeeName)

                                        
                                        // 根据职位设置专属技能为指定等级，其他技能为0
                                        val newEmployee = when (position) {
                                            "程序员" -> Employee(
                                                id = maxId + newEmployees.size + 1,
                                                name = employeeName,
                                                position = position,
                                                skillDevelopment = skillLevel,
                                                skillDesign = 0,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = baseSalary,
                                                experience = 100,
                                                motivation = 100,
                                                isFounder = false,
                                                hireYear = currentYear,
                                                hireMonth = currentMonth,
                                                hireDay = currentDay
                                            )
                                            "策划师" -> Employee(
                                                id = maxId + newEmployees.size + 1,
                                                name = employeeName,
                                                position = position,
                                                skillDevelopment = 0,
                                                skillDesign = skillLevel,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = baseSalary,
                                                experience = 100,
                                                motivation = 100,
                                                isFounder = false,
                                                hireYear = currentYear,
                                                hireMonth = currentMonth,
                                                hireDay = currentDay
                                            )
                                            "美术师" -> Employee(
                                                id = maxId + newEmployees.size + 1,
                                                name = employeeName,
                                                position = position,
                                                skillDevelopment = 0,
                                                skillDesign = 0,
                                                skillArt = skillLevel,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = baseSalary,
                                                experience = 100,
                                                motivation = 100,
                                                isFounder = false,
                                                hireYear = currentYear,
                                                hireMonth = currentMonth,
                                                hireDay = currentDay
                                            )
                                            "音效师" -> Employee(
                                                id = maxId + newEmployees.size + 1,
                                                name = employeeName,
                                                position = position,
                                                skillDevelopment = 0,
                                                skillDesign = 0,
                                                skillArt = 0,
                                                skillMusic = skillLevel,
                                                skillService = 0,
                                                salary = baseSalary,
                                                experience = 100,
                                                motivation = 100,
                                                isFounder = false,
                                                hireYear = currentYear,
                                                hireMonth = currentMonth,
                                                hireDay = currentDay
                                            )
                                            "客服" -> Employee(
                                                id = maxId + newEmployees.size + 1,
                                                name = employeeName,
                                                position = position,
                                                skillDevelopment = 0,
                                                skillDesign = 0,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = skillLevel,
                                                salary = baseSalary,
                                                experience = 100,
                                                motivation = 100,
                                                isFounder = false,
                                                hireYear = currentYear,
                                                hireMonth = currentMonth,
                                                hireDay = currentDay
                                            )
                                            else -> Employee(
                                                id = maxId + newEmployees.size + 1,
                                                name = employeeName,
                                                position = position,
                                                salary = baseSalary,
                                                experience = 100,
                                                motivation = 100,
                                                isFounder = false,
                                                hireYear = currentYear,
                                                hireMonth = currentMonth,
                                                hireDay = currentDay
                                            )
                                        }
                                        newEmployees.add(newEmployee)
                                    }
                                }
                                
                                // 收集被更新的员工ID
                                val updatedEmployeeIds = updatedEmployees.map { it.id }.toSet()
                                
                                // 合并员工列表：保留未被更新的员工 + 更新后的员工 + 新增的员工
                                val finalEmployees = allEmployees.filter { !updatedEmployeeIds.contains(it.id) } + 
                                                     updatedEmployees + 
                                                     newEmployees
                                
                                // 更新员工列表
                                allEmployees.clear()
                                allEmployees.addAll(finalEmployees)
                            },
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney }
                        )
                    }
                }
            }
        }
        
        // 秘书聊天对话框
        if (showSecretaryChat) {
            SecretaryChatDialog(
                messages = chatMessages,
                onMessagesChange = { newMessages ->
                    chatMessages.clear()
                    chatMessages.addAll(newMessages)
                },
                onDismiss = { showSecretaryChat = false }
            )
        }
        
        // 功能介绍对话框
        if (tutorialState.showTutorialDialog.value && tutorialState.currentTutorialId.value != null) {
            TutorialDialog(
                tutorialId = tutorialState.currentTutorialId.value!!,
                onDismiss = { tutorialState.dismissTutorial() },
                onComplete = { tutorialState.completeTutorial() }
            )
        }
        
        // 公司概览教程触发器
        TutorialTrigger(
            tutorialId = TutorialId.COMPANY_OVERVIEW_INTRO,
            tutorialState = tutorialState,
            enabled = selectedTab == 0 && saveData == null // 只在新游戏且进入公司概览时触发
        )
        
        // 员工管理教程触发器
        TutorialTrigger(
            tutorialId = TutorialId.EMPLOYEE_MANAGEMENT_INTRO,
            tutorialState = tutorialState,
            enabled = selectedTab == 1 // 进入员工管理时触发
        )
        
        // 项目管理教程触发器
        TutorialTrigger(
            tutorialId = TutorialId.PROJECT_MANAGEMENT_INTRO,
            tutorialState = tutorialState,
            enabled = selectedTab == 2 // 进入项目管理时触发
        )
        
        // 竞争对手教程触发器
        TutorialTrigger(
            tutorialId = TutorialId.COMPETITOR_INTRO,
            tutorialState = tutorialState,
            enabled = selectedTab == 3 // 进入竞争对手时触发
        )
        
        // 赛事教程触发器
        TutorialTrigger(
            tutorialId = TutorialId.TOURNAMENT_INTRO,
            tutorialState = tutorialState,
            enabled = selectedTab == 4 // 进入赛事时触发
        )
        
        // 服务器管理教程触发器
        TutorialTrigger(
            tutorialId = TutorialId.SERVER_MANAGEMENT_INTRO,
            tutorialState = tutorialState,
            enabled = selectedTab == 5 // 进入服务器管理时触发
        )
        
        // 赛事完成弹窗
        if (showTournamentResultDialog && tournamentResult != null) {
            TournamentResultDialog(
                tournament = tournamentResult!!,
                onDismiss = {
                    showTournamentResultDialog = false
                    tournamentResult = null
                }
            )
        }
        
        // GVA颁奖典礼对话框
        if (showGVAAwardDialog) {
            GVAAwardDialog(
                year = gvaAwardYear,
                nominations = gvaAwardNominations,
                playerWonCount = gvaPlayerWonCount,
                playerTotalReward = gvaPlayerTotalReward,
                playerFansGain = gvaPlayerFansGain,
                onDismiss = {
                    showGVAAwardDialog = false
                    isPaused = false // 关闭对话框后恢复游戏
                }
            )
        }
        
        // 挑战完成对话框
        if (showChallengeCompleteDialog) {
            ChallengeCompleteDialog(
                currentYear = currentYear,
                currentMonth = currentMonth,
                acquiredCompaniesCount = totalAcquiredCompanies,
                totalIPs = ownedIPs.size,
                onContinue = {
                    showChallengeCompleteDialog = false
                    isPaused = false
                },
                onNewGame = {
                    showChallengeCompleteDialog = false
                    navController.navigate("main_menu") {
                        popUpTo("main_menu") { inclusive = true }
                    }
                }
            )
        }
        
        // 涨薪请求对话框
        if (showSalaryRequestDialog && salaryRequestEmployee != null) {
            val employee = salaryRequestEmployee!!
            SalaryRequestDialog(
                employee = employee,
                currentMoney = money,
                onAccept = {
                    // 同意涨薪：更新员工薪资，提升忠诚度，增加涨薪次数
                    val updatedEmployees = allEmployees.map { emp ->
                        if (emp.id == employee.id) {
                            emp.copy(
                                salary = employee.requestedSalary!!,
                                requestedSalary = null,
                                lastSalaryRequestYear = currentYear,
                                lastSalaryRequestMonth = currentMonth,
                                salaryRequestCount = (emp.salaryRequestCount + 1).coerceAtMost(3), // 增加涨薪次数，最多3次
                                loyalty = (emp.loyalty + 10).coerceAtMost(100) // 提升10点忠诚度
                            )
                        } else {
                            emp
                        }
                    }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees)
                    
                    // 获取更新后的涨薪次数
                    val updatedEmployee = allEmployees.find { it.id == employee.id }
                    val updatedCount = updatedEmployee?.salaryRequestCount ?: (employee.salaryRequestCount + 1)
                    
                    showSalaryRequestDialog = false
                    salaryRequestEmployee = null
                    isPaused = false
                    
                    Log.d("MainActivity", "✅ 同意涨薪：${employee.name} (第${updatedCount}次涨薪) 薪资涨到¥${employee.requestedSalary}")
                },
                onReject = {
                    // 拒绝涨薪：降低忠诚度
                    val updatedEmployees = allEmployees.map { emp ->
                        if (emp.id == employee.id) {
                            emp.copy(
                                requestedSalary = null,
                                lastSalaryRequestYear = currentYear,
                                lastSalaryRequestMonth = currentMonth,
                                loyalty = (emp.loyalty - 15).coerceAtLeast(0) // 降低15点忠诚度
                            )
                        } else {
                            emp
                        }
                    }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees)
                    
                    showSalaryRequestDialog = false
                    salaryRequestEmployee = null
                    isPaused = false
                    
                    Log.d("MainActivity", "❌ 拒绝涨薪：${employee.name} 忠诚度降低")
                }
            )
        }
        
        // 年终奖对话框
        if (showYearEndBonusDialog) {
            // 重新计算年度统计数据（确保数据最新）- 统计本年有收入的游戏数量，并分别统计单机游戏和网络游戏
            val revenueDataForDialog = RevenueManager.exportRevenueData()
            val gamesReleasedThisYear = revenueDataForDialog.values.count { revenue ->
                // 检查该游戏在当年是否有收入记录
                revenue.dailySalesList.any { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                    recordGameYear == currentYear && dailySales.revenue > 0
                }
            }
            
            // 分别统计单机游戏和网络游戏数量
            val singlePlayerGames = revenueDataForDialog.values.count { revenue ->
                // 检查该游戏在当年是否有收入记录
                val hasRevenueThisYear = revenue.dailySalesList.any { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                    recordGameYear == currentYear && dailySales.revenue > 0
                }
                if (!hasRevenueThisYear) return@count false
                
                // 通过游戏ID找到对应的游戏，判断类型
                val game = games.find { it.id == revenue.gameId }
                game?.businessModel == com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER
            }
            
            val onlineGames = revenueDataForDialog.values.count { revenue ->
                // 检查该游戏在当年是否有收入记录
                val hasRevenueThisYear = revenue.dailySalesList.any { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                    recordGameYear == currentYear && dailySales.revenue > 0
                }
                if (!hasRevenueThisYear) return@count false
                
                // 通过游戏ID找到对应的游戏，判断类型
                val game = games.find { it.id == revenue.gameId }
                game?.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME
            }
            
            // 计算年度总收入（从RevenueManager获取，统计所有已发售游戏在当年的收入）
            val totalRevenue = revenueDataForDialog.values
                .flatMap { revenue ->
                    revenue.dailySalesList.filter { dailySales ->
                        // 直接从recordDate中提取游戏内年份
                        // recordDate是用游戏内时间创建的，所以其中的YEAR字段就是游戏内年份
                        val recordCalendar = Calendar.getInstance()
                        recordCalendar.time = dailySales.date
                        val recordGameYear = recordCalendar.get(Calendar.YEAR)
                        recordGameYear == currentYear // 只统计当年的收入
                    }
                }
                .sumOf { it.revenue.toLong() } // 转换为Long
            
            // 计算年度总支出（员工薪资 + 服务器费用 + 开发成本）
            val totalSalary = allEmployees.sumOf { it.salary.toLong() } * 12L
            
            // 计算年度服务器费用（从服务器数据中获取）
            val totalServerCost = RevenueManager.exportRevenueData()
                .values
                .sumOf { revenue ->
                    // 获取该游戏的服务器信息
                    val serverInfo = RevenueManager.getGameServerInfo(revenue.gameId)
                    // 计算该游戏所有服务器的年度费用（12个月）
                    serverInfo.servers.filter { it.isActive }.sumOf { server ->
                        server.type.cost * 12L
                    }
                }
            
            // 注意：开发费用已在创建游戏时扣除，不应在年度支出中重复计算
            val totalExpenses = totalSalary + totalServerCost
            val netProfit = totalRevenue - totalExpenses
            
            val yearEndStatistics = YearEndStatistics(
                year = currentYear,
                gamesReleased = gamesReleasedThisYear,
                singlePlayerGames = singlePlayerGames,
                onlineGames = onlineGames,
                totalRevenue = totalRevenue,
                netProfit = netProfit,
                totalEmployees = allEmployees.size
            )
            
            YearEndBonusDialog(
                statistics = yearEndStatistics,
                currentMoney = money,
                employeeCount = allEmployees.size,
                averageSalary = if (allEmployees.isNotEmpty()) {
                    allEmployees.map { it.salary }.average().toInt()
                } else {
                    5000 // 默认值
                },
                onDistributeBonus = { bonusAmount ->
                    // 分发年终奖：扣除资金，提升所有员工忠诚度
                    money = safeAddMoney(money, -bonusAmount)
                    
                    val updatedEmployees = allEmployees.map { emp ->
                        if (!emp.isFounder) {
                            emp.copy(
                                loyalty = (emp.loyalty + 20).coerceAtMost(100) // 提升20点忠诚度
                            )
                        } else {
                            emp
                        }
                    }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees)
                    
                    showYearEndBonusDialog = false
                    isPaused = false
                    
                    Log.d("MainActivity", "💰 发放年终奖：¥$bonusAmount，提升所有员工忠诚度")
                },
                onSkip = {
                    // 跳过年终奖：降低所有员工忠诚度
                    val updatedEmployees = allEmployees.map { emp ->
                        if (!emp.isFounder) {
                            emp.copy(
                                loyalty = (emp.loyalty - 10).coerceAtLeast(0) // 降低10点忠诚度
                            )
                        } else {
                            emp
                        }
                    }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees)
                    
                    showYearEndBonusDialog = false
                    isPaused = false
                    
                    Log.d("MainActivity", "⚠️ 跳过年终奖：所有员工忠诚度降低")
                }
            )
        }
        
        // 功能解锁对话框
        if (showFeatureLockedDialog) {
            FeatureLockedDialog(
                onDismiss = { showFeatureLockedDialog = false },
                onOpenAfdian = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://afdian.com/a/LTDHMNDH"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                    }
                    showFeatureLockedDialog = false
                }
            )
        }
        
        if (pendingAchievementsToShow.isNotEmpty()) {
            AchievementPopupQueue(
                achievements = pendingAchievementsToShow,
                onAllDismissed = {
                    pendingAchievementsToShow = emptyList()
                }
            )
        }
    }
}

@Composable
fun TopInfoBar(
    money: Long,
    fans: Long,
    year: Int,
    month: Int,
    day: Int,
    gameSpeed: Int,
    onSpeedChange: (Int) -> Unit,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    companyName: String = "我的游戏公司",
    selectedLogo: String = "🎮",
    onSettingsClick: () -> Unit = {},
    isSupporterUnlocked: Boolean = false,
    onShowFeatureLockedDialog: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A).copy(alpha = 0.95f), // 深蓝色，增加不透明度
                        Color(0xFF3B5BDB).copy(alpha = 0.90f), // 亮一点的蓝色
                        Color(0xFF1E3A8A).copy(alpha = 0.95f) // 回到深蓝色，创造渐变效果
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                ),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左边区域：资金和粉丝（垂直排列）
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // 资金
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // 金额采用滚动动画并保留两位小数
                    val animatedMoney = remember { Animatable(money.toFloat()) }
                    LaunchedEffect(money) {
                        animatedMoney.animateTo(
                            targetValue = money.toFloat(),
                            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        )
                    }
                    Text(
                        text = "¥${formatMoneyWithDecimals(animatedMoney.value.toDouble())}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 粉丝
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❤️",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatMoneyWithDecimals(fans.toDouble()),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // 中间区域：日期、游戏速度和FPS
            Row(
                modifier = Modifier.weight(2.2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 日期 - 固定宽度防止抖动，紧贴左边
                Text(
                    text = "第${year}年${month}月${day}日",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.width(84.dp)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                // 游戏速度下拉选择
                GameSpeedDropdown(
                    currentSpeed = gameSpeed,
                    isPaused = isPaused,
                    onSpeedChange = onSpeedChange,
                    onPauseToggle = onPauseToggle,
                    isSupporterUnlocked = isSupporterUnlocked,
                    onShowFeatureLockedDialog = onShowFeatureLockedDialog
                )
                
                // FPS监测 - 固定宽度防止抖动
                FpsMonitor(
                    modifier = Modifier
                )
            }
            
            // 右边区域：设置按钮
            Box(
                modifier = Modifier.weight(0.6f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSettingsClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "设置",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun CompanyOverviewContent(
    companyName: String = "我的游戏公司",
    selectedLogo: String = "🎮",
    founder: Founder? = null,
    allEmployees: List<Employee> = emptyList(),
    games: List<Game> = emptyList(),
    money: Long = 0L,
    fans: Long = 0L,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    competitors: List<CompetitorCompany> = emptyList(),
    competitorNews: List<CompetitorNews> = emptyList(),
    onSecretaryChatClick: () -> Unit = {},
    revenueRefreshTrigger: Int = 0 // 新增：收益刷新触发器
) {
    var showSecretaryBubble by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 标题栏与秘书头像
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏢 公司概览",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                // 秘书头像和气泡
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // 气泡对话框（带尾巴）
                    if (showSecretaryBubble) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .widthIn(max = 180.dp)
                        ) {
                            // 气泡主体
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "老板，您好！👋",
                                    color = Color(0xFF1F2937),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 气泡尾巴（三角形指示器，指向右侧头像）
                            Canvas(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 8.dp)
                                    .size(8.dp, 12.dp)
                            ) {
                                val trianglePath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, size.height / 2)
                                    lineTo(0f, size.height)
                                    close()
                                }
                                drawPath(
                                    path = trianglePath,
                                    color = Color(0xFFE5E7EB)
                                )
                            }
                        }
                    }
                    
                    // 秘书头像和标签
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .border(2.dp, Color(0xFFF59E0B), CircleShape)
                                .clickable { onSecretaryChatClick() }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👩‍💼",
                                fontSize = 24.sp
                            )
                        }
                        Text(
                            text = "秘书",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 计算公司市值（使用统一的市值计算函数）
            val currentSaveData = SaveData(
                companyName = companyName,
                companyLogo = selectedLogo,
                founderName = founder?.name ?: "",
                founderProfession = founder?.profession,
                money = money,
                fans = fans,
                currentYear = currentYear,
                currentMonth = currentMonth,
                currentDay = currentDay,
                allEmployees = allEmployees,
                games = games,
                competitors = competitors,
                competitorNews = competitorNews
            )
            val marketValue = calculatePlayerMarketValue(currentSaveData)
            
            // 公司基本信息
            CompanyInfoCard(
                title = "公司信息",
                logo = selectedLogo, // 玩家选择的公司LOGO
                items = listOf(
                    "公司名称" to companyName,
                    "成立时间" to "第1年1月1日",
                    "公司市值" to formatMoney(marketValue)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 创始人信息
            if (founder != null) {
                CompanyInfoCard(
                    title = "创始人信息",
                    items = listOf(
                        "姓名" to founder.name,
                        "职业" to "${founder.profession.icon} ${founder.profession.displayName}",
                        "专属技能" to founder.profession.specialtySkill,
                        "技能等级" to "${founder.profession.specialtySkill} Lv.${founder.toEmployee().getSpecialtySkillLevel()}"
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 财务状况（带年份选择）
            var selectedFinancialYear by remember { mutableIntStateOf(currentYear) }
            
            // 记录上次的游戏数和日期，用于日志
            LaunchedEffect(games.size, currentYear, currentMonth, currentDay) {
                Log.d("MainActivity", "财务状况：检测到数据变化（游戏数：${games.size}，日期：${currentYear}年${currentMonth}月${currentDay}日）")
            }
            
            // 使用 derivedStateOf 计算财务数据，确保依赖变化时自动更新
            data class FinancialDetails(
                val revenueDetails: Map<String, Double>, // 收入明细：单机收入、网游收入、子公司收入
                val expenseDetails: Map<String, Double>, // 支出明细：员工薪资、服务器费用等
                val totalRevenue: Double,
                val totalExpense: Double,
                val profit: Double
            )
            
            val financialData = remember(games.size, currentYear, currentMonth, currentDay, selectedFinancialYear, allEmployees.size, revenueRefreshTrigger) {
                derivedStateOf {
                    Log.d("MainActivity", "===== 财务状况计算开始 =====")
                    Log.d("MainActivity", "查询年份: 第${selectedFinancialYear}年")
                    Log.d("MainActivity", "revenueRefreshTrigger: $revenueRefreshTrigger")
                    
                    val releasedGames = games.filter { 
                        it.releaseStatus == GameReleaseStatus.RELEASED || 
                        it.releaseStatus == GameReleaseStatus.RATED 
                    }
                    Log.d("MainActivity", "已发售游戏数量: ${releasedGames.size}")
                    
                    // 收入明细计算
                    var singlePlayerRevenue = 0.0
                    var onlineGameRevenue = 0.0
                    var subsidiaryRevenue = 0.0
                    
                    // 单机收入（不包括子公司）
                    val singlePlayerGames = releasedGames.filter { 
                        it.businessModel == BusinessModel.SINGLE_PLAYER && !it.id.startsWith("inherited_")
                    }
                    singlePlayerGames.forEach { game ->
                        val revenue = RevenueManager.getGameRevenue(game.id)
                        if (revenue != null && revenue.dailySalesList.isNotEmpty()) {
                            val matchingRecords = revenue.dailySalesList.filter { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                                recordGameYear == selectedFinancialYear
                            }
                            singlePlayerRevenue += matchingRecords.sumOf { it.revenue }
                        }
                    }
                    
                    // 网游收入（不包括子公司）
                    val onlineGames = releasedGames.filter { 
                        it.businessModel == BusinessModel.ONLINE_GAME && !it.id.startsWith("inherited_")
                    }
                    onlineGames.forEach { game ->
                        val revenue = RevenueManager.getGameRevenue(game.id)
                        if (revenue != null) {
                            // 注册收入（从dailySalesList统计）
                            val registrationRevenue = if (revenue.dailySalesList.isNotEmpty()) {
                                val matchingRecords = revenue.dailySalesList.filter { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                                    recordGameYear == selectedFinancialYear
                                }
                                matchingRecords.sumOf { it.revenue }
                            } else {
                                0.0
                            }
                            
                            // 付费内容收益（网游主要收入来源）
                            // 需要根据该年份的dailySalesList来估算该年份的付费内容收益
                            // 由于付费内容收益是累计的，我们需要按日期范围来估算
                            val monetizationRevenue = if (revenue.monetizationRevenues.isNotEmpty() && revenue.dailySalesList.isNotEmpty()) {
                                // 计算该年份的天数
                                val matchingRecords = revenue.dailySalesList.filter { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                                    recordGameYear == selectedFinancialYear
                                }
                                val totalDays = matchingRecords.size
                                val allDays = revenue.dailySalesList.size
                                
                                // 按比例分配累计付费内容收益
                                if (allDays > 0) {
                                    val totalMonetizationRevenue = revenue.monetizationRevenues.sumOf { it.totalRevenue }
                                    totalMonetizationRevenue * (totalDays.toDouble() / allDays.toDouble())
                                } else {
                                    0.0
                                }
                            } else {
                                0.0
                            }
                            
                            onlineGameRevenue += registrationRevenue + monetizationRevenue
                        }
                    }
                    
                    // 子公司收入（包括单机和网游）
                    val subsidiaryGames = releasedGames.filter { it.id.startsWith("inherited_") }
                    subsidiaryGames.forEach { game ->
                        val revenue = RevenueManager.getGameRevenue(game.id)
                        if (revenue != null) {
                            // 注册/销量收入（从dailySalesList统计）
                            val baseRevenue = if (revenue.dailySalesList.isNotEmpty()) {
                                val matchingRecords = revenue.dailySalesList.filter { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                                    recordGameYear == selectedFinancialYear
                                }
                                matchingRecords.sumOf { it.revenue }
                            } else {
                                0.0
                            }
                            
                            // 如果是网游，还需要加上付费内容收益
                            val monetizationRevenue = if (game.businessModel == BusinessModel.ONLINE_GAME && 
                                revenue.monetizationRevenues.isNotEmpty() && revenue.dailySalesList.isNotEmpty()) {
                                // 计算该年份的天数
                                val matchingRecords = revenue.dailySalesList.filter { dailySales ->
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = dailySales.date
                    val recordGameYear = recordCalendar.get(Calendar.YEAR)
                                    recordGameYear == selectedFinancialYear
                                }
                                val totalDays = matchingRecords.size
                                val allDays = revenue.dailySalesList.size
                                
                                // 按比例分配累计付费内容收益
                                if (allDays > 0) {
                                    val totalMonetizationRevenue = revenue.monetizationRevenues.sumOf { it.totalRevenue }
                                    totalMonetizationRevenue * (totalDays.toDouble() / allDays.toDouble())
                                } else {
                                    0.0
                                }
                            } else {
                                0.0
                            }
                            
                            subsidiaryRevenue += baseRevenue + monetizationRevenue
                        }
                    }
                    
                    val yearTotalRevenue = singlePlayerRevenue + onlineGameRevenue + subsidiaryRevenue
                    
                    // 支出明细计算（估算：该年份的支出）
                    // 注意：这里显示的是估算的年度支出，而不是实际已扣除的金额
                    // 实际扣除：
                    // - 员工薪资：每月1日扣除（见2608-2613行）
                    // - 服务器费用：每30天扣除一次（见2403-2415行）
                    // 因此，如果查看的是当前年份，显示的是全年估算支出，但实际资金可能只扣除了部分月份的费用
                    
                    // 员工薪资：按12个月计算（假设该年全年都有这些员工）
                    val monthlySalary = allEmployees.sumOf { it.salary }
                    val yearlySalary = monthlySalary * 12L
                    
                    // 服务器费用：需要计算该年的服务器扣费总额（估算）
                    // 由于服务器扣费记录不在保存数据中，这里按当前服务器状态估算
                    val serverData = RevenueManager.exportServerData()
                    var yearlyServerCost = 0.0
                    serverData.forEach { (_, serverInfo) ->
                        serverInfo.servers.filter { it.isActive }.forEach { server ->
                            // 估算：该年有12个月，每30天扣费一次，大约12次
                            val monthlyCost = server.type.cost
                            yearlyServerCost += monthlyCost * 12.0
                        }
                    }
                    
                    val yearTotalExpense = yearlySalary + yearlyServerCost
                    val profit = yearTotalRevenue - yearTotalExpense
                    
                    val revenueDetails = mapOf(
                        "单机收入" to singlePlayerRevenue,
                        "网游收入" to onlineGameRevenue,
                        "子公司收入" to subsidiaryRevenue
                    )
                    
                    val expenseDetails = mapOf(
                        "员工薪资" to yearlySalary.toDouble(),
                        "服务器费用" to yearlyServerCost
                    )
                    
                    Log.d("MainActivity", "财务状况计算完成（第${selectedFinancialYear}年）：总收入¥${formatMoneyWithDecimals(yearTotalRevenue)} - 总支出¥${formatMoneyWithDecimals(yearTotalExpense)} = 利润¥${formatMoneyWithDecimals(profit)}")
                    
                    FinancialDetails(revenueDetails, expenseDetails, yearTotalRevenue, yearTotalExpense, profit)
                }
            }.value
            
            // 可展开的财务项组件
            var revenueExpanded by remember { mutableStateOf(false) }
            var expenseExpanded by remember { mutableStateOf(false) }
            
            CompanyInfoCardWithYearSelector(
                title = "财务状况",
                currentYear = currentYear,
                selectedYear = selectedFinancialYear,
                onYearChange = { selectedFinancialYear = it },
                content = {
                    // 总收入（可展开）
                    ExpandableFinancialItem(
                        label = "总收入",
                        value = financialData.totalRevenue,
                        details = financialData.revenueDetails,
                        isExpanded = revenueExpanded,
                        onExpandedChange = { revenueExpanded = it },
                        positiveColor = Color(0xFF10B981) // 绿色表示收入
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 总支出（可展开）
                    ExpandableFinancialItem(
                        label = "总支出",
                        value = financialData.totalExpense,
                        details = financialData.expenseDetails,
                        isExpanded = expenseExpanded,
                        onExpandedChange = { expenseExpanded = it },
                        positiveColor = Color(0xFFEF4444) // 红色表示支出
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 利润
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "利润",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "¥${formatMoneyWithDecimals(financialData.profit)}",
                            color = if (financialData.profit >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
            
            // 底部留白，避免内容被底部导航栏遮挡
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CompanyInfoCard(
    title: String,
    items: List<Pair<String, String>>,
    logo: String? = null // 可选的公司LOGO
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行（包含LOGO）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // 如果有LOGO，显示在左侧
                if (logo != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFF59E0B),
                                        Color(0xFFD97706)
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = logo,
                            fontSize = 24.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Text(
                    text = title,
                    color = Color(0xFFF59E0B), // 橙色强调色
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CompanyInfoCardWithYearSelector(
    title: String,
    currentYear: Int,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    items: List<Pair<String, String>> = emptyList(),
    onRefresh: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    var showYearDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行（包含年份选择器和刷新按钮）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFFF59E0B), // 橙色强调色
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // 右侧按钮组
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 刷新按钮（仅在有刷新回调时显示）
                    if (onRefresh != null) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = "🔄",
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    // 年份选择按钮
                    OutlinedButton(
                        onClick = { showYearDialog = true },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "第${selectedYear}年",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            // 年份选择对话框
            if (showYearDialog) {
                Dialog(onDismissRequest = { showYearDialog = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(
                                color = Color(0xFF1F2937),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "选择年份",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                items((1..currentYear).reversed().toList()) { year ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = if (year == selectedYear) 
                                                    Color(0xFFF59E0B).copy(alpha = 0.2f) 
                                                else 
                                                    Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                onYearChange(year)
                                                showYearDialog = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "第${year}年",
                                            color = if (year == selectedYear) Color(0xFFF59E0B) else Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showYearDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "关闭",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // 财务数据列表或自定义内容
            if (content != null) {
                Column {
                    content()
                }
            } else {
                items.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = value,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// 可展开财务项组件
@Composable
fun ExpandableFinancialItem(
    label: String,
    value: Double,
    details: Map<String, Double>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    positiveColor: Color
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) 
                        Icons.Default.KeyboardArrowUp 
                    else 
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "¥${formatMoneyWithDecimals(value)}",
                color = positiveColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 展开时显示明细
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            ) {
                details.forEach { (detailLabel, detailValue) ->
                    if (detailValue > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "  • $detailLabel",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "¥${formatMoneyWithDecimals(detailValue)}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
// 优化版本的底部导航栏组件 - 字体加粗+黑色
@Composable
fun EnhancedBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    pendingApplicantsCount: Int = 0, // 待处理应聘者数量
    pendingAssignmentCount: Int = 0, // 待分配项目数量
    onTournamentClick: () -> Unit = {}, // 赛事按钮点击事件
    onCompetitorClick: () -> Unit = {} // 竞争对手按钮点击事件
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedBottomNavItem(
                icon = "🏢",
                label = "公司概览",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            
            EnhancedBottomNavItem(
                icon = "👥",
                label = "员工管理",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                showBadge = pendingApplicantsCount > 0
            )
            
            EnhancedBottomNavItem(
                icon = "🎮",
                label = "项目管理",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                showBadge = pendingAssignmentCount > 0
            )
            
            EnhancedBottomNavItem(
                icon = "🎯",
                label = "竞争对手",
                isSelected = selectedTab == 3,
                onClick = onCompetitorClick // 点击显示菜单
            )
            
            EnhancedBottomNavItem(
                icon = "🏆",
                label = "赛事",
                isSelected = selectedTab == 4,
                onClick = onTournamentClick // 点击显示菜单
            )
            
            EnhancedBottomNavItem(
                icon = "🖥️",
                label = "服务器",
                isSelected = selectedTab == 5,
                onClick = { onTabSelected(5) }
            )
        }
    }
}

@Composable
fun EnhancedBottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .scale(scale)
    ) {
        // 使用BadgeBox包裹图标以显示红点
        BadgeBox(
            showBadge = showBadge,
            badgeCount = null  // 只显示红点，不显示数字
        ) {
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier
                    .background(
                        brush = if (isSelected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.2f)
                                ),
                                radius = 40f
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            )
        }
        Text(
            text = label,
            color = Color.White, // 统一使用白色
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold // 设置为加粗
        )
    }
}

@Composable
fun ContinueScreen(navController: NavController) {
    val context = LocalContext.current
    val saveManager = remember { SaveManager(context) }
    var saves by remember { mutableStateOf(emptyMap<Int, SaveData?>()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var saveToDelete by remember { mutableStateOf(null as Pair<Int, SaveData?>?) }
    var refreshKey by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    
    // 异步加载存档 - 使用refreshKey作为key，确保每次显示都重新加载
    LaunchedEffect(refreshKey) {
        isLoading = true
        saves = saveManager.getAllSavesAsync()
        isLoading = false
    }
    
    // 监听导航状态，当界面重新可见时刷新存档列表
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route == "continue") {
                refreshKey++
            }
        }
        navController.addOnDestinationChangedListener(listener)
        
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    
    Box(
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
    ) {
        // FPS监测（左上角）
        FpsMonitor(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📂 继续游戏",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
            )
            
            if (isLoading) {
                // 加载中指示器
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在加载存档...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // 存档位列表
                for (slotIndex in 1..3) {
                    SaveSlotCard(
                        slotIndex = slotIndex,
                        saveData = saves[slotIndex],
                        onLoadSave = { saveData ->
                            // 直接加载存档，不再进行版本检查
                            currentLoadedSaveData = saveData
                            Toast.makeText(context, "加载存档 $slotIndex", Toast.LENGTH_SHORT).show()
                            navController.navigate("game/${saveData.companyName}/${saveData.founderName}/${saveData.companyLogo}/${saveData.founderProfession?.name ?: "PROGRAMMER"}")
                        },
                        onDeleteSave = {
                            saveToDelete = Pair(slotIndex, saves[slotIndex])
                            showDeleteConfirmDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            GameMenuButton(
                text = "返回主菜单",
                onClick = { navController.popBackStack() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 删除存档确认对话框
        if (showDeleteConfirmDialog && saveToDelete != null) {
            DeleteSaveConfirmDialog(
                slotIndex = saveToDelete!!.first,
                saveData = saveToDelete!!.second,
                onConfirm = {
                    coroutineScope.launch {
                        // 先保存要删除的存档位编号
                        val slotToDelete = saveToDelete!!.first
                        
                        // 删除存档
                        saveManager.deleteSaveAsync(slotToDelete)
                        
                        // 重新加载所有存档
                        saves = saveManager.getAllSavesAsync()
                        
                        withContext(Dispatchers.Main) {
                            // 使用保存的变量而不是saveToDelete
                            Toast.makeText(context, "删除存档 $slotToDelete", Toast.LENGTH_SHORT).show()
                            
                            // 所有操作完成后再清空状态
                            showDeleteConfirmDialog = false
                            saveToDelete = null
                        }
                    }
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                    saveToDelete = null
                }
            )
        }
    }
}

@Composable
fun DeleteSaveConfirmDialog(
    slotIndex: Int,
    saveData: SaveData?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "删除存档",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "确定要删除存档 $slotIndex 吗？",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                if (saveData != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "公司: ${saveData.companyName}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "时间: ${saveData.currentYear}年${saveData.currentMonth}月${saveData.currentDay}日",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "此操作无法撤销！",
                    color = Color(0xFFEF4444),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Card(
                modifier = Modifier.clickable { onConfirm() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDC2626).copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "确认删除",
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Card(
                modifier = Modifier.clickable { onDismiss() },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "取消",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFF1F2937),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SaveSlotCard(
    slotIndex: Int,
    saveData: SaveData?,
    onLoadSave: (SaveData) -> Unit,
    onDeleteSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable {
                saveData?.let { onLoadSave(it) }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (saveData != null) {
                Color.White.copy(alpha = 0.15f)
            } else {
                Color.White.copy(alpha = 0.05f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (saveData != null) {
                // 有存档数据
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "存档 $slotIndex",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = onDeleteSave,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                text = "🗑️",
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = saveData.companyLogo,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "公司: ${saveData.companyName}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Text(
                        text = "资金: ¥${formatMoney(saveData.money)} | 粉丝: ${formatMoneyWithDecimals(saveData.fans.toDouble())}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "时间: ${saveData.currentYear}年${saveData.currentMonth}月${saveData.currentDay}日",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // 显示版本号
                    Text(
                        text = "版本: ${saveData.version}",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // 空存档位
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📁",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "存档位 $slotIndex",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "空",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementScreen(
    navController: NavController,
    saveData: SaveData,
    revenueData: Map<String, GameRevenue>
) {
    val unlockedIds = saveData.unlockedAchievements.map { it.achievementId }.toSet()
    val totalAchievements = Achievements.ALL_ACHIEVEMENTS.size
    val unlockedCount = unlockedIds.size
    
    var selectedCategory by remember { mutableStateOf(AchievementCategory.COMPANY) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        // FPS监测（左上角）
        FpsMonitor(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题和统计（并列显示）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 成就",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 统计卡片
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已解锁：",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$unlockedCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = " / $totalAchievements",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${(unlockedCount * 100 / totalAchievements)}%)",
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 类别标签页
            @Suppress("DEPRECATION")
            ScrollableTabRow(
                selectedTabIndex = AchievementCategory.entries.indexOf(selectedCategory),
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 16.dp
            ) {
                AchievementCategory.entries.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = "${category.icon} ${category.displayName}",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 成就列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val categoryAchievements = Achievements.getAchievementsByCategory(selectedCategory)
                
                items(categoryAchievements) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        isUnlocked = achievement.id in unlockedIds
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 返回按钮（居中）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                GameMenuButton(
                    text = "返回主菜单",
                    onClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                Color.White.copy(alpha = 0.95f)
            } else {
                Color.White.copy(alpha = 0.7f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 成就图标
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = if (isUnlocked) {
                            Color(0xFFFFD700).copy(alpha = 0.2f)
                        } else {
                            Color.Gray.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 32.sp,
                    modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 成就信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFF333333) else Color(0xFF999999)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = achievement.description,
                    fontSize = 13.sp,
                    color = if (isUnlocked) Color(0xFF666666) else Color(0xFF999999)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 完成状态标签
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFE0E0E0)
                    }
                )
            ) {
                Text(
                    text = if (isUnlocked) "已完成" else "未完成",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isUnlocked) Color.White else Color(0xFF999999),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        // FPS监测（左上角）
        FpsMonitor(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚙️ 设置",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            GameMenuButton(
                text = "返回主菜单",
                onClick = { 
                    navController.navigate("main_menu") {
                        popUpTo("main_menu") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
// 存档管理类（异步版本，支持数据清理和压缩）
class SaveManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_saves", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .serializeNulls() // 确保null值也被序列化，去除PrettyPrinting以减小体积
        // setLenient()已弃用，移除以消除警告
        .create()
    
    companion object {
        private const val MAX_DAILY_SALES_DAYS = 365 // 每个游戏最多保留365天的每日数据
        private const val MAX_COMPETITOR_NEWS = 50 // 最多保留50条竞争对手新闻
    }
    
    /**
     * 修复旧存档数据，确保所有新增字段都有正确的默认值
     * 这是为了兼容旧版本存档，避免因缺失字段导致闪退
     */
    private fun fixLegacySaveData(saveData: SaveData): SaveData {
        try {
            Log.d("SaveManager", "开始修复旧存档数据，版本: ${saveData.version}")
            
            // 修复游戏数据：确保所有可空字段和新增字段都有正确的默认值
            val fixedGames = saveData.games.map { game ->
                game.copy(
                    // 赛事相关字段（可空）
                    currentTournament = game.currentTournament,
                    lastTournamentDate = game.lastTournamentDate,
                    tournamentHistory = game.tournamentHistory,
                    
                    // 更新历史（可空）
                    updateHistory = game.updateHistory,
                    
                    // GVA奖项（可能缺失）
                    awards = game.awards,
                    
                    // 付费内容（网游必需）
                    monetizationItems = game.monetizationItems,
                    
                    // 其他可能缺失的字段
                    developmentCost = game.developmentCost,
                    promotionIndex = game.promotionIndex,
                    autoUpdate = game.autoUpdate,
                    autoPromotion = game.autoPromotion,
                    version = game.version,
                    
                    // 分阶段开发累积员工（新增字段）
                    allDevelopmentEmployees = game.allDevelopmentEmployees
                )
            }
            
            // 调试：修复前的子公司数据
            Log.d("SaveManager", "🔍 修复前：子公司数量=${saveData.subsidiaries.size}")
            saveData.subsidiaries.forEachIndexed { index, sub ->
                Log.d("SaveManager", "  修复前子公司[$index]: ${sub.name}, ID=${sub.id}")
            }
            
            // 修复SaveData级别的字段
            val fixedSaveData = saveData.copy(
                games = fixedGames,
                
                // 教程和成就系统（可空）
                completedTutorials = saveData.completedTutorials,
                unlockedAchievements = saveData.unlockedAchievements,
                skipTutorial = saveData.skipTutorial,
                
                // 客服中心
                autoProcessComplaints = saveData.autoProcessComplaints,
                complaints = saveData.complaints,
                
                // 自动宣传设置
                autoPromotionThreshold = saveData.autoPromotionThreshold,
                
                // GVA系统（可能缺失）
                companyReputation = saveData.companyReputation,
                gvaHistory = saveData.gvaHistory,
                currentYearNominations = saveData.currentYearNominations,
                gvaAnnouncedDate = saveData.gvaAnnouncedDate,
                
                // 竞争对手系统
                competitors = saveData.competitors,
                competitorNews = saveData.competitorNews,
                
                // 收购系统（子公司和IP）
                ownedIPs = saveData.ownedIPs,
                subsidiaries = saveData.subsidiaries,
                
                // 招聘系统
                jobPostings = saveData.jobPostings,
                
                // 服务器和收益数据
                serverData = saveData.serverData,
                revenueData = saveData.revenueData,
                
                // 创始人职业（可空）
                founderProfession = saveData.founderProfession,
                
                // 兑换码和支持者功能
                usedRedeemCodes = saveData.usedRedeemCodes,
                // 如果旧存档中已使用SUPPORTER兑换码，则自动设置解锁状态
                isSupporterUnlocked = saveData.isSupporterUnlocked || saveData.usedRedeemCodes.contains("SUPPORTER"),
                
                // 自动存档设置
                autoSaveEnabled = saveData.autoSaveEnabled,
                autoSaveInterval = saveData.autoSaveInterval,
                lastAutoSaveDay = saveData.lastAutoSaveDay
            )
            
            // 调试：修复后的子公司数据
            Log.d("SaveManager", "🔍 修复后：子公司数量=${fixedSaveData.subsidiaries.size}")
            fixedSaveData.subsidiaries.forEachIndexed { index, sub ->
                Log.d("SaveManager", "  修复后子公司[$index]: ${sub.name}, ID=${sub.id}")
            }
            
            Log.d("SaveManager", "修复完成：游戏${fixedGames.size}个，员工${fixedSaveData.allEmployees.size}人，子公司${fixedSaveData.subsidiaries.size}个")
            return fixedSaveData
            
        } catch (e: Exception) {
            Log.e("SaveManager", "修复存档数据时出错，返回原始数据", e)
            e.printStackTrace()
            return saveData
        }
    }
    
    /**
     * 清理存档数据，移除过旧的历史数据以减小体积
     */
    private fun cleanSaveData(saveData: SaveData): SaveData {
        Log.d("SaveManager", "===== 开始清理存档数据 =====")
        
        // 1. 清理收益数据：每个游戏只保留最近365天的每日销售数据
        val cleanedRevenueData = saveData.revenueData.mapValues { (gameId, revenue) ->
            // 记录清理前的数据
            if (revenue.totalRegisteredPlayers > 0) {
                Log.d("SaveManager", "清理前 - 游戏 ${revenue.gameName}: 总注册=${revenue.totalRegisteredPlayers}")
            }
            
            val cleaned = if (revenue.dailySalesList.size > MAX_DAILY_SALES_DAYS) {
                val recentDailySales = revenue.dailySalesList.takeLast(MAX_DAILY_SALES_DAYS)
                val totalSales = revenue.dailySalesList.sumOf { it.sales }
                val totalRevenue = revenue.dailySalesList.sumOf { it.revenue }
                
                Log.d("SaveManager", "游戏 ${revenue.gameName} 清理前: ${revenue.dailySalesList.size}天, 清理后: ${recentDailySales.size}天")
                
                // 更新统计数据以保留总计信息
                revenue.copy(
                    dailySalesList = recentDailySales,
                    statistics = revenue.statistics?.copy(
                        totalSales = totalSales,
                        totalRevenue = totalRevenue
                    )
                )
            } else {
                revenue
            }
            
            // 记录清理后的数据
            if (cleaned.totalRegisteredPlayers > 0) {
                Log.d("SaveManager", "清理后 - 游戏 ${cleaned.gameName}: 总注册=${cleaned.totalRegisteredPlayers}")
            } else if (revenue.totalRegisteredPlayers > 0) {
                Log.e("SaveManager", "⚠️⚠️⚠️ 清理数据时丢失了 totalRegisteredPlayers！游戏=${revenue.gameName}")
            }
            
            cleaned
        }
        
        // 2. 清理竞争对手新闻：只保留最近50条
        val cleanedCompetitorNews = if (saveData.competitorNews.size > MAX_COMPETITOR_NEWS) {
            saveData.competitorNews.takeLast(MAX_COMPETITOR_NEWS)
        } else {
            saveData.competitorNews
        }
        
        Log.d("SaveManager", "数据清理完成: 收益数据=${cleanedRevenueData.size}个游戏, 竞争对手新闻=${cleanedCompetitorNews.size}条")
        
        return saveData.copy(
            revenueData = cleanedRevenueData,
            competitorNews = cleanedCompetitorNews
        )
    }
    
    /**
     * 压缩字符串（GZIP）
     */
    private fun compressString(input: String): ByteArray {
        val bos = java.io.ByteArrayOutputStream()
        java.util.zip.GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(input) }
        return bos.toByteArray()
    }
    
    /**
     * 解压字符串（GZIP）
     */
    private fun decompressString(compressed: ByteArray): String {
        val bis = java.io.ByteArrayInputStream(compressed)
        return java.util.zip.GZIPInputStream(bis).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
    
    // 异步保存游戏（带数据清理和压缩）
    suspend fun saveGameAsync(slotIndex: Int, saveData: SaveData): SaveResult = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            
            // 0. 强制保存RevenueManager的pending数据（性能优化：避免丢失数据）
            RevenueManager.forceSave()
            
            // 1. 清理数据
            val cleanedData = cleanSaveData(saveData)
            
            // 2. 序列化为JSON
            val json = gson.toJson(cleanedData)
            val jsonSizeKB = json.length / 1024.0
            val jsonSizeMB = jsonSizeKB / 1024.0
            
            Log.d("SaveManager", "JSON大小: ${String.format(Locale.US, "%.2f", jsonSizeKB)} KB (${String.format(Locale.US, "%.2f", jsonSizeMB)} MB)")
            
            // 3. GZIP压缩
            val compressed = compressString(json)
            val compressedSizeKB = compressed.size / 1024.0
            val compressionRatio = (1 - compressedSizeKB / jsonSizeKB) * 100
            
            Log.d("SaveManager", "压缩后大小: ${String.format(Locale.US, "%.2f", compressedSizeKB)} KB, 压缩率: ${String.format(Locale.US, "%.1f", compressionRatio)}%")
            
            // 4. Base64编码后存储（因为SharedPreferences只能存字符串）
            val base64Encoded = android.util.Base64.encodeToString(compressed, android.util.Base64.DEFAULT)
            
            // 5. 保存到SharedPreferences
            sharedPreferences.edit {
                putString("save_slot_${slotIndex}_compressed", base64Encoded)
                putBoolean("save_slot_${slotIndex}_is_compressed", true)
                // 移除旧的未压缩版本（如果存在）
                remove("save_slot_$slotIndex")
            }
            
            val duration = System.currentTimeMillis() - startTime
            Log.d("SaveManager", "保存游戏到存档位 $slotIndex 完成，耗时: ${duration}ms")
            Log.d("SaveManager", "游戏数量: ${saveData.games.size}, 收益记录: ${saveData.revenueData.size}个游戏")
            
            SaveResult(
                success = true,
                originalSizeKB = jsonSizeKB,
                compressedSizeKB = compressedSizeKB,
                errorMessage = null
            )
        } catch (e: OutOfMemoryError) {
            Log.e("SaveManager", "保存游戏失败: 内存不足", e)
            SaveResult(
                success = false,
                errorMessage = "内存不足，存档数据过大。建议清理部分游戏数据。"
            )
        } catch (e: Exception) {
            Log.e("SaveManager", "保存游戏失败", e)
            SaveResult(
                success = false,
                errorMessage = "保存失败: ${e.message}"
            )
        }
    }
    
    // 异步加载游戏（支持压缩和未压缩格式）
    suspend fun loadGameAsync(slotIndex: Int): SaveData? = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val isCompressed = sharedPreferences.getBoolean("save_slot_${slotIndex}_is_compressed", false)
            
            val json = if (isCompressed) {
                // 加载压缩格式
                val base64Encoded = sharedPreferences.getString("save_slot_${slotIndex}_compressed", null)
                if (base64Encoded != null) {
                    val compressed = android.util.Base64.decode(base64Encoded, android.util.Base64.DEFAULT)
                    decompressString(compressed)
                } else {
                    null
                }
            } else {
                // 加载旧的未压缩格式
                sharedPreferences.getString("save_slot_$slotIndex", null)
            }
            
            return@withContext if (json != null) {
                try {
                    val loadedData = gson.fromJson(json, SaveData::class.java)
                    // 修复旧存档数据，确保兼容性
                    val fixedData = fixLegacySaveData(loadedData)
                    val duration = System.currentTimeMillis() - startTime
                    Log.d("SaveManager", "从存档位 $slotIndex 加载游戏完成（${if (isCompressed) "压缩" else "未压缩"}），耗时: ${duration}ms, 游戏数量: ${fixedData.games.size}")
                    fixedData
                } catch (e: Exception) {
                    Log.e("SaveManager", "解析存档失败", e)
                    Log.e("SaveManager", "错误详情: ${e.message}")
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SaveManager", "加载存档失败", e)
            null
        }
    }
    
    // 异步删除存档
    suspend fun deleteSaveAsync(slotIndex: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            remove("save_slot_$slotIndex")
            remove("save_slot_${slotIndex}_compressed")
            remove("save_slot_${slotIndex}_is_compressed")
        }
    }
    
    // 异步加载所有存档
    suspend fun getAllSavesAsync(): Map<Int, SaveData?> = withContext(Dispatchers.IO) {
        mapOf(
            1 to loadGameAsync(1),
            2 to loadGameAsync(2),
            3 to loadGameAsync(3)
        )
    }
    
    // 同步方法（保留用于兼容）
    @Deprecated("使用异步版本 saveGameAsync")
    fun saveGame(slotIndex: Int, saveData: SaveData) {
        val json = gson.toJson(saveData)
        sharedPreferences.edit {
            putString("save_slot_$slotIndex", json)
        }
    }
    
    @Deprecated("使用异步版本 loadGameAsync")
    fun loadGame(slotIndex: Int): SaveData? {
        val json = sharedPreferences.getString("save_slot_$slotIndex", null)
        return if (json != null) {
            try {
                gson.fromJson(json, SaveData::class.java)
            } catch (e: Exception) {
                Log.e("SaveManager", "加载存档失败", e)
                null
            }
        } else {
            null
        }
    }
}

// 保存结果数据类
data class SaveResult(
    val success: Boolean,
    val originalSizeKB: Double = 0.0,
    val compressedSizeKB: Double = 0.0,
    val errorMessage: String? = null
)
@Composable
fun InGameSettingsContent(
    navController: NavController,
    money: Long = 10000L,
    fans: Long = 0L,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    companyName: String = "我的游戏公司",
    selectedLogo: String = "🎮",
    founderName: String = "创始人",
    founderProfession: FounderProfession = FounderProfession.PROGRAMMER,
    games: List<Game> = emptyList(),
    allEmployees: List<Employee> = emptyList(),
    competitors: List<CompetitorCompany> = emptyList(),
    competitorNews: List<CompetitorNews> = emptyList(),
    complaints: List<Complaint> = emptyList(),
    autoProcessComplaints: Boolean = false,
    autoPromotionThreshold: Float = 0.5f, // 自动宣传阈值
    autoApproveSalaryIncrease: Boolean = false, // 自动审批员工涨薪开关
    onAutoApproveSalaryToggle: (Boolean) -> Unit = {}, // 自动审批涨薪开关切换回调
    unlockedAchievements: List<UnlockedAchievement> = emptyList(),
    completedTutorials: Set<String> = emptySet(), // 新增：教程进度
    skipTutorial: Boolean = false, // 新增：跳过教程状态
    companyReputation: CompanyReputation = CompanyReputation(), // GVA：公司声望
    gvaHistory: List<AwardNomination> = emptyList(), // GVA：历史记录
    currentYearNominations: List<AwardNomination> = emptyList(), // GVA：当年提名
    gvaAnnouncedDate: GameDate? = null, // GVA：颁奖日期
    ownedIPs: List<GameIP> = emptyList(), // 拥有的游戏IP列表（收购竞争对手后获得）
    subsidiaries: List<Subsidiary> = emptyList(), // 子公司列表（收购竞争对手后转为子公司）
    gmModeEnabled: Boolean = false, // GM模式是否开启
    onGMToggle: (Boolean) -> Unit = {}, // GM模式切换回调
    autoSaveEnabled: Boolean = false, // 自动存档开关
    autoSaveInterval: Int = 5, // 自动存档间隔（天）
    lastAutoSaveDay: Int = 0, // 上次自动存档的游戏天数
    onAutoSaveEnabledToggle: (Boolean) -> Unit = {}, // 自动存档开关切换回调
    onAutoSaveIntervalChange: (Int) -> Unit = {}, // 自动存档间隔修改回调
    onMaxEmployees: () -> Unit = {}, // 一键满配员工回调
    onAddMoney: () -> Unit = {}, // 一键加钱回调
    onCreateTopEmployees: (Int) -> Unit = {}, // 创建各职位6名指定等级专属技能员工回调（参数：技能等级1-5）
    onMoneyUpdate: (Long) -> Unit = {}, // 资金更新回调
    usedRedeemCodes: Set<String> = emptySet(), // 已使用的兑换码列表
    onUsedRedeemCodesUpdate: (Set<String>) -> Unit = {}, // 已使用兑换码更新回调
    isSupporterUnlocked: Boolean = false, // 是否解锁支持者功能
    onShowFeatureLockedDialog: () -> Unit = {}, // 显示功能解锁对话框的回调
    onShowAutoSaveInfoDialog: () -> Unit = {}, // 显示自动存档提示对话框的回调
    onShowAutoApproveInfoDialog: () -> Unit = {} // 显示自动审批提示对话框的回调
) {
    val context = LocalContext.current
    val saveManager = remember { SaveManager(context) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingSaveSlots by remember { mutableStateOf(false) }
    var saveSlots by remember { mutableStateOf(emptyMap<Int, SaveData?>()) }
    val coroutineScope = rememberCoroutineScope()
    
    // 获取当前登录的TapTap用户ID
    val tapTapAccount = TapLoginManager.getCurrentAccount()
    val userId = tapTapAccount?.unionId ?: tapTapAccount?.openId
    
    // 检查账号是否已解锁GM模式（账号级别，使用LeanCloud）
    var isGMModeUnlockedByAccount by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                isGMModeUnlockedByAccount = LeanCloudRedeemCodeManager.isGMUnlocked(userId)
            } catch (e: Exception) {
                Log.e("LeanCloud", "检查GM模式失败", e)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 保存游戏按钮
        Button(
            onClick = {
                isLoadingSaveSlots = true
                showSaveDialog = true
                Log.d("GameSave", "打开保存对话框，开始加载存档列表")
                // 异步加载存档列表
                coroutineScope.launch {
                    saveSlots = saveManager.getAllSavesAsync()
                    Log.d("GameSave", "存档列表加载完成: slot1=${saveSlots[1] != null}, slot2=${saveSlots[2] != null}, slot3=${saveSlots[3] != null}")
                    isLoadingSaveSlots = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💾",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "保存游戏",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 自动存档开关和设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isSupporterUnlocked) {
                                onShowFeatureLockedDialog()
                            } else {
                                onShowAutoSaveInfoDialog()
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💾",
                            fontSize = 18.sp
                        )
                        Column {
                            Text(
                                text = "自动存档",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "开启后每隔${autoSaveInterval}天自动保存到存档位1",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
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
                        checked = autoSaveEnabled,
                        onCheckedChange = { enabled ->
                            // 直接切换开关状态
                            if (!isSupporterUnlocked) {
                                onShowFeatureLockedDialog()
                            } else {
                                onAutoSaveEnabledToggle(enabled)
                            }
                        },
                        enabled = isSupporterUnlocked,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF3B82F6),
                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                }
                
                // 存档间隔设置（仅在开启时显示）
                if (autoSaveEnabled) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "存档间隔",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${autoSaveInterval}天",
                                color = Color(0xFF3B82F6),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = autoSaveInterval.toFloat(),
                            onValueChange = { onAutoSaveIntervalChange(it.toInt()) },
                            valueRange = 1f..30f,
                            steps = 29,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6),
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "1分钟",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "30分钟",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        
        // 自动审批涨薪开关
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isSupporterUnlocked) {
                            onShowFeatureLockedDialog()
                        } else {
                            onShowAutoApproveInfoDialog()
                        }
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💰",
                        fontSize = 18.sp
                    )
                    Column {
                        Text(
                            text = "自动审批",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (autoApproveSalaryIncrease) "已开启：自动同意员工涨薪请求" else "已关闭：需手动审批",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
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
                    checked = autoApproveSalaryIncrease,
                    onCheckedChange = { enabled ->
                        // 直接切换开关状态
                        if (!isSupporterUnlocked) {
                            onShowFeatureLockedDialog()
                        } else {
                            onAutoApproveSalaryToggle(enabled)
                        }
                    },
                    enabled = isSupporterUnlocked,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF3B82F6),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
        }
        
        // 返回主菜单按钮
        Button(
            onClick = {
                showExitConfirmDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏠",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "返回主菜单",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 兑换码区域（始终显示，GM模式激活后仍可使用其他兑换码）
        var redeemCode by remember { mutableStateOf("") }
        var showRedeemError by remember { mutableStateOf(false) }
        var showRedeemSuccessDialog by remember { mutableStateOf(false) }
        var redeemSuccessMessage by remember { mutableStateOf("") }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎁 兑换码",
                    color = Color(0xFFF59E0B),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = redeemCode,
                    onValueChange = { 
                        redeemCode = it
                        showRedeemError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            text = "请输入兑换码",
                            color = Color.White.copy(alpha = 0.4f)
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                
                if (showRedeemError) {
                    Text(
                        text = "❌ 兑换码错误，请重新输入",
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp
                    )
                }
                
                Button(
                    onClick = {
                        @Suppress("SpellCheckingInspection")
                        // 去除空格并转换为大写
                        val codeUpper = redeemCode.trim().uppercase()
                        
                        // 检查用户是否已登录
                        if (userId.isNullOrBlank()) {
                            Log.w("RedeemCode", "用户未登录，无法使用兑换码")
                            showRedeemError = true
                            return@Button
                        }
                        
                        // 检查兑换码是否为空
                        if (codeUpper.isBlank()) {
                            Log.w("RedeemCode", "兑换码为空")
                            showRedeemError = true
                            return@Button
                        }
                        
                        // 使用协程处理异步操作
                        coroutineScope.launch {
                            try {
                                Log.d("RedeemCode", "开始验证兑换码: $codeUpper")
                                
                                // 特殊处理：PROGM兑换码直接使用本地验证，不走LeanCloud
                                if (codeUpper == "PROGM") {
                                    Log.d("RedeemCode", "检测到PROGM兑换码，使用本地验证")
                                    
                                    // 检查是否已使用过（本地）
                                    val isUsedLocally = RedeemCodeManager.isCodeUsedByUser(userId, codeUpper)
                                    
                                    if (isUsedLocally) {
                                        // 已使用过，自动启用GM模式
                                        if (!gmModeEnabled) {
                                            onGMToggle(true)
                                        }
                                        redeemCode = ""
                                        redeemSuccessMessage = "✅ GM工具箱已激活！"
                                        showRedeemSuccessDialog = true
                                    } else {
                                        // 首次使用，标记为已使用并启用GM模式
                                        RedeemCodeManager.markCodeAsUsed(userId, codeUpper)
                                        onGMToggle(true)
                                        
                                        redeemCode = ""
                                        redeemSuccessMessage = "✅ GM工具箱已激活！"
                                        showRedeemSuccessDialog = true
                                    }
                                    return@launch
                                }
                                
                                // 其他兑换码：验证是否存在（从LeanCloud查询）
                                val redeemCodeData = LeanCloudRedeemCodeManager.validateRedeemCode(codeUpper)
                                
                                if (redeemCodeData == null) {
                                    Log.w("LeanCloud", "❌ 兑换码不存在或无效: $codeUpper")
                                    redeemSuccessMessage = "❌ 兑换失败：兑换码不存在或无效"
                                    showRedeemError = true
                                    return@launch
                                }
                                
                                // 确定兑换码类型（redeemCodeData已经确认不为null）
                                val codeType = redeemCodeData.type
                                if (codeType.isBlank()) {
                                    Log.e("LeanCloud", "❌ 无法确定兑换码类型: $codeUpper")
                                    redeemSuccessMessage = "❌ 兑换失败：兑换码类型无效"
                                    showRedeemError = true
                                    return@launch
                                }
                                
                                // 检查是否为支持者兑换码或GM兑换码
                                val isSupporterCode = codeType == "supporter"
                                val isGMCode = codeType == "gm"
                                
                                if (isSupporterCode) {
                                    Log.d("LeanCloud", "开始兑换支持者兑换码: $codeUpper")
                                    
                                    // 使用新的首次绑定机制
                                    val ownership = LeanCloudRedeemCodeManager.checkCodeOwnership(codeUpper, userId)
                                    when (ownership) {
                                        is LeanCloudRedeemCodeManager.CodeOwnership.Available -> {
                                            // 首次使用，绑定到当前用户
                                            val bound = LeanCloudRedeemCodeManager.bindCodeToUser(codeUpper, userId)
                                            if (bound) {
                                                // 绑定成功，记录使用历史
                                                LeanCloudRedeemCodeManager.recordUserRedeem(userId, codeUpper, codeType)
                                                // 同时更新本地（向后兼容）
                                                onUsedRedeemCodesUpdate(usedRedeemCodes + codeUpper)
                                                RedeemCodeManager.markCodeAsUsed(userId, codeUpper)
                                                
                                                redeemCode = ""
                                                redeemSuccessMessage = "✅ 兑换成功！已解锁所有支持者功能\n💾 兑换码已绑定到你的账号"
                                                showRedeemSuccessDialog = true
                                            } else {
                                                Log.e("LeanCloud", "❌ 绑定兑换码失败")
                                                redeemSuccessMessage = "❌ 兑换失败：绑定失败"
                                                showRedeemError = true
                                            }
                                        }
                                        is LeanCloudRedeemCodeManager.CodeOwnership.OwnedByCurrentUser -> {
                                            // 已绑定到当前用户，可以继续使用
                                            Log.d("LeanCloud", "✅ 兑换码已绑定到当前用户")
                                            redeemSuccessMessage = "✅ 兑换成功！已解锁所有支持者功能\n💾 换设备也可使用"
                                            showRedeemSuccessDialog = true
                                        }
                                        is LeanCloudRedeemCodeManager.CodeOwnership.OwnedByOthers -> {
                                            // 已被其他用户绑定
                                            Log.w("LeanCloud", "❌ 兑换码已被其他用户使用")
                                            redeemSuccessMessage = "❌ 兑换失败：该兑换码已被其他用户使用"
                                            showRedeemError = true
                                        }
                                        else -> {
                                            Log.e("LeanCloud", "❌ 检查兑换码归属失败")
                                            redeemSuccessMessage = "❌ 兑换失败：网络错误或表不存在"
                                            showRedeemError = true
                                        }
                                    }
                                    return@launch
                                } else if (isGMCode) {
                                    // GM兑换码处理逻辑保持不变
                                    return@launch
                                }
                                
                                // 处理其他兑换码
                                when (codeUpper) {
                                    "YCJY2025" -> {
                                        // 检查是否已使用过（云端 + 存档本地）
                                        val isUsedInCloud = LeanCloudRedeemCodeManager.hasUserUsedCode(userId, codeUpper)
                                        val isUsedInSave = usedRedeemCodes.contains(codeUpper)
                                        
                                        if (isUsedInCloud == true || isUsedInSave) {
                                            showRedeemError = true
                                        } else {
                                            // 记录使用（云端）
                                            LeanCloudRedeemCodeManager.recordUserRedeem(userId, codeUpper, "special")
                                            // 同时标记本地
                                            RedeemCodeManager.markCodeAsUsed(userId, codeUpper)
                                            
                                            // 兑换码：YCJY2025，获得5M资金
                                            val rewardAmount = 5000000L // 5M = 500万
                                            onMoneyUpdate(money + rewardAmount)
                                            // 标记兑换码为已使用（存档本地）
                                            onUsedRedeemCodesUpdate(usedRedeemCodes + codeUpper)
                                            redeemCode = ""
                                            redeemSuccessMessage = "兑换成功！获得 ${formatMoney(rewardAmount)}\n💾 数据已同步到云端"
                                            showRedeemSuccessDialog = true
                                        }
                                    }
                                    else -> {
                                        showRedeemError = true
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("LeanCloud", "兑换码处理失败", e)
                                showRedeemError = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("兑换", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // 兑换成功弹窗
        if (showRedeemSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showRedeemSuccessDialog = false },
                title = {
                    Text(
                        text = "✅ 兑换成功",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = redeemSuccessMessage,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(20.dp),
                confirmButton = {
                    Button(
                        onClick = { showRedeemSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text("知道了", color = Color.White, fontSize = 15.sp)
                    }
                }
            )
        }
        
        // GM工具箱（仅在GM模式激活时显示）
        if (gmModeEnabled) {
            var showSkillLevelDialog by remember { mutableStateOf(false) }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF6B6B).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🛠️ GM工具箱",
                        color = Color(0xFFFF6B6B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 一键满配员工
                    Button(
                        onClick = onMaxEmployees,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👥",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "一键满配员工",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // 一键加1000万
                    Button(
                        onClick = onAddMoney,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💰",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "一键加1000万",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // 创建各职位6名指定等级专属技能员工
                    Button(
                        onClick = { showSkillLevelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "各职位6名5级员工",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // 技能等级选择对话框
            if (showSkillLevelDialog) {
                Dialog(onDismissRequest = { showSkillLevelDialog = false }) {
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
                            Text(
                                text = "⭐ 选择员工技能等级",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "将创建各职位6名员工",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            
                            // 等级选择按钮
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (level in 1..5) {
                                    val levelColor = when (level) {
                                        5 -> Color(0xFFFF6B6B) // 红色 - 5级
                                        4 -> Color(0xFFF59E0B) // 橙色 - 4级
                                        3 -> Color(0xFF10B981) // 绿色 - 3级
                                        2 -> Color(0xFF3B82F6) // 蓝色 - 2级
                                        else -> Color(0xFF6B7280) // 灰色 - 1级
                                    }
                                    
                                    val levelLabel = when (level) {
                                        5 -> "★★★★★ 5级"
                                        4 -> "★★★★☆ 4级"
                                        3 -> "★★★☆☆ 3级"
                                        2 -> "★★☆☆☆ 2级"
                                        else -> "★☆☆☆☆ 1级"
                                    }
                                    
                                    Button(
                                        onClick = {
                                            showSkillLevelDialog = false
                                            onCreateTopEmployees(level)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = levelColor
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = levelLabel,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 取消按钮
                            OutlinedButton(
                                onClick = { showSkillLevelDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "取消",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    // 保存游戏对话框
    if (showSaveDialog) {
        var showOverwriteConfirmDialog by remember { mutableStateOf(false) }
        var selectedSlotNumber by remember { mutableIntStateOf(0) }
        var selectedExistingSave by remember { mutableStateOf(null as SaveData?) }
        
        // 覆盖确认对话框
        if (showOverwriteConfirmDialog && selectedExistingSave != null) {
            AlertDialog(
                onDismissRequest = { 
                    showOverwriteConfirmDialog = false
                    selectedSlotNumber = 0
                    selectedExistingSave = null
                },
                title = {
                    Text(
                        text = "覆盖存档确认",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "存档位 $selectedSlotNumber 已有存档数据，确定要覆盖吗？",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "公司: ${selectedExistingSave!!.companyName}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "时间: ${selectedExistingSave!!.currentYear}年${selectedExistingSave!!.currentMonth}月${selectedExistingSave!!.currentDay}日",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "资金: ¥${formatMoney(selectedExistingSave!!.money)} | 粉丝: ${formatMoneyWithDecimals(selectedExistingSave!!.fans.toDouble())}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "此操作将覆盖原有存档数据，无法撤销！",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val saveData = SaveData(
                                companyName = companyName,
                                companyLogo = selectedLogo,
                                founderName = founderName,
                                founderProfession = founderProfession,
                                money = money,
                                fans = fans,
                                currentYear = currentYear,
                                currentMonth = currentMonth,
                                currentDay = currentDay,
                                allEmployees = allEmployees,
                                games = games,
                                competitors = competitors,
                                competitorNews = competitorNews,
                                serverData = RevenueManager.exportServerData(), // 导出服务器数据
                                revenueData = RevenueManager.exportRevenueData(), // 导出收益数据
                                jobPostings = JobPostingService.getInstance().getAllJobPostingsForSave(), // 导出招聘岗位数据
                                complaints = complaints, // 保存客诉数据
                                autoProcessComplaints = autoProcessComplaints, // 保存自动处理开关状态
                                autoPromotionThreshold = autoPromotionThreshold, // 保存自动宣传阈值
                                autoApproveSalaryIncrease = autoApproveSalaryIncrease, // 保存自动审批涨薪开关
                                unlockedAchievements = unlockedAchievements, // 保存已解锁成就
                                completedTutorials = completedTutorials, // 保存已完成教程
                                skipTutorial = skipTutorial, // 保存跳过教程状态
                                companyReputation = companyReputation, // 保存公司声望
                                gvaHistory = gvaHistory, // 保存GVA历史记录
                                currentYearNominations = currentYearNominations, // 保存当年提名
                                gvaAnnouncedDate = gvaAnnouncedDate, // 保存颁奖日期
                                ownedIPs = ownedIPs, // 保存拥有的IP列表（收购竞争对手后获得）
                                subsidiaries = subsidiaries, // 🔧 保存子公司列表（收购竞争对手后转为子公司）
                                gmModeEnabled = gmModeEnabled, // 保存GM模式状态
                                usedRedeemCodes = usedRedeemCodes, // 保存已使用的兑换码列表
                                isSupporterUnlocked = isSupporterUnlocked, // 保存支持者功能解锁状态
                                autoSaveEnabled = autoSaveEnabled, // 保存自动存档开关
                                autoSaveInterval = autoSaveInterval, // 保存自动存档间隔
                                lastAutoSaveDay = lastAutoSaveDay, // 保存上次自动存档时间
                                saveTime = System.currentTimeMillis(),
                                version = BuildConfig.VERSION_NAME // 使用当前游戏版本号
                            )
                            val slotToSave = selectedSlotNumber
                            isSaving = true
                            Log.d("GameSave", "开始保存游戏到存档位 $slotToSave（覆盖模式）")
                            coroutineScope.launch {
                                val result = saveManager.saveGameAsync(slotToSave, saveData)
                                Log.d("GameSave", "保存结果: success=${result.success}, error=${result.errorMessage}")
                                withContext(Dispatchers.Main) {
                                    isSaving = false
                                    if (result.success) {
                                        val compressionRatio = if (result.originalSizeKB > 0) {
                                            (1 - result.compressedSizeKB / result.originalSizeKB) * 100
                                        } else 0.0
                                        val message = "游戏已保存！\n压缩前: ${String.format(Locale.US, "%.1f", result.originalSizeKB)} KB\n压缩后: ${String.format(Locale.US, "%.1f", result.compressedSizeKB)} KB\n压缩率: ${String.format(Locale.US, "%.1f", compressionRatio)}%"
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        // 重新加载存档列表以更新UI
                                        saveSlots = saveManager.getAllSavesAsync()
                                        showSaveDialog = false
                                        showOverwriteConfirmDialog = false
                                    } else {
                                        Toast.makeText(context, result.errorMessage ?: "保存失败，请重试", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "确认覆盖",
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showOverwriteConfirmDialog = false
                            selectedSlotNumber = 0
                            selectedExistingSave = null
                        }
                    ) {
                        Text(
                            text = "取消",
                            color = Color.White
                        )
                    }
                },
                containerColor = Color(0xFF1F2937),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
        
        // 主保存对话框
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "选择存档位",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "请选择要保存到的存档位：",
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (isLoadingSaveSlots) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else {
                        repeat(3) { index ->
                            val slotNumber = index + 1
                            val existingSave = saveSlots[slotNumber]
                            // 检查是否需要解锁（第2、3个槽位需要解锁）
                            val isLocked = (slotNumber == 2 || slotNumber == 3) && !isSupporterUnlocked
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (isLocked) {
                                        // 显示功能解锁对话框
                                        onShowFeatureLockedDialog()
                                        return@clickable
                                    }
                                    if (existingSave != null) {
                                        // 有存档，显示覆盖确认对话框
                                        selectedSlotNumber = slotNumber
                                        selectedExistingSave = existingSave
                                        showOverwriteConfirmDialog = true
                                    } else {
                                        // 空存档，直接保存
                                        val saveData = SaveData(
                                            companyName = companyName,
                                            companyLogo = selectedLogo,
                                            founderName = founderName,
                                            founderProfession = founderProfession,
                                            money = money,
                                            fans = fans,
                                            currentYear = currentYear,
                                            currentMonth = currentMonth,
                                            currentDay = currentDay,
                                            allEmployees = allEmployees,
                                            games = games,
                                            competitors = competitors,
                                            competitorNews = competitorNews,
                                            serverData = RevenueManager.exportServerData(), // 导出服务器数据
                                            revenueData = RevenueManager.exportRevenueData(), // 导出收益数据
                                            jobPostings = JobPostingService.getInstance().getAllJobPostingsForSave(), // 导出招聘岗位数据
                                            complaints = complaints, // 保存客诉数据
                                            autoProcessComplaints = autoProcessComplaints, // 保存自动处理开关状态
                                            autoPromotionThreshold = autoPromotionThreshold, // 保存自动宣传阈值
                                            autoApproveSalaryIncrease = autoApproveSalaryIncrease, // 保存自动审批涨薪开关
                                            unlockedAchievements = unlockedAchievements, // 保存已解锁成就
                                            completedTutorials = completedTutorials, // 保存已完成教程
                                            skipTutorial = skipTutorial, // 保存跳过教程状态
                                            companyReputation = companyReputation, // 保存公司声望
                                            gvaHistory = gvaHistory, // 保存GVA历史记录
                                            currentYearNominations = currentYearNominations, // 保存当年提名
                                            gvaAnnouncedDate = gvaAnnouncedDate, // 保存颁奖日期
                                            ownedIPs = ownedIPs, // 保存拥有的IP列表（收购竞争对手后获得）
                                            subsidiaries = subsidiaries, // 🔧 保存子公司列表（收购竞争对手后转为子公司）
                                            gmModeEnabled = gmModeEnabled, // 保存GM模式状态
                                            usedRedeemCodes = usedRedeemCodes, // 保存已使用的兑换码列表
                                            isSupporterUnlocked = isSupporterUnlocked, // 保存支持者功能解锁状态
                                            autoSaveEnabled = autoSaveEnabled, // 保存自动存档开关
                                            autoSaveInterval = autoSaveInterval, // 保存自动存档间隔
                                            lastAutoSaveDay = lastAutoSaveDay, // 保存上次自动存档时间
                                            saveTime = System.currentTimeMillis(),
                                            version = BuildConfig.VERSION_NAME // 使用当前游戏版本号
                                        )
                                        isSaving = true
                                        Log.d("GameSave", "开始保存游戏到存档位 $slotNumber（新存档）")
                                        coroutineScope.launch {
                                            val result = saveManager.saveGameAsync(slotNumber, saveData)
                                            Log.d("GameSave", "保存结果: success=${result.success}, error=${result.errorMessage}")
                                            withContext(Dispatchers.Main) {
                                                isSaving = false
                                                if (result.success) {
                                                    val compressionRatio = if (result.originalSizeKB > 0) {
                                                        (1 - result.compressedSizeKB / result.originalSizeKB) * 100
                                                    } else 0.0
                                                    val message = "游戏已保存！\n压缩前: ${String.format(Locale.US, "%.1f", result.originalSizeKB)} KB\n压缩后: ${String.format(Locale.US, "%.1f", result.compressedSizeKB)} KB\n压缩率: ${String.format(Locale.US, "%.1f", compressionRatio)}%"
                                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                    // 重新加载存档列表以更新UI
                                                    saveSlots = saveManager.getAllSavesAsync()
                                                    showSaveDialog = false
                                                } else {
                                                    Toast.makeText(context, result.errorMessage ?: "保存失败，请重试", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "存档位 $slotNumber",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isLocked) {
                                        Text(
                                            text = "🔒",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                if (existingSave != null) {
                                    Text(
                                        text = "${existingSave.companyName} - ${existingSave.currentYear}年${existingSave.currentMonth}月${existingSave.currentDay}日",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "资金: ¥${formatMoney(existingSave.money)} | 粉丝: ${formatMoneyWithDecimals(existingSave.fans.toDouble())}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Text(
                                        text = if (isLocked) "需要解锁" else "空存档",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSaveDialog = false }
                ) {
                    Text(
                        text = "取消",
                        color = Color.White
                    )
                }
            },
            containerColor = Color(0xFF1F2937),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
    
    // 返回主菜单确认对话框
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "返回主菜单",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "确定要返回主菜单吗？\n\n💡 提示：请记得使用「保存游戏」按钮保存进度",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        navController.navigate("main_menu")
                    }
                ) {
                    Text(
                        text = "返回",
                        color = Color(0xFFEF4444)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmDialog = false }
                ) {
                    Text(
                        text = "取消",
                        color = Color.White
                    )
                }
            },
            containerColor = Color(0xFF1F2937),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
    
    // 保存中的loading overlay
    if (isSaving) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在保存游戏...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "请稍候，不要关闭应用",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}



@Composable
fun PrivacyPolicyDialog(onAgree: () -> Unit, onReject: () -> Unit) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = { /* 不允许点击外部关闭 */ }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "个人信息保护指引",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 内容区域 - 可滚动
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    // 带链接的开头文本
                    val introText = buildAnnotatedString {
                        append("请您在使用本游戏前仔细阅读")
                        pushStringAnnotation(tag = "user_agreement", annotation = "https://share.note.youdao.com/s/FUdL4QRe")
                        withStyle(style = SpanStyle(color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)) {
                            append("《用户协议》")
                        }
                        pop()
                        append("和")
                        pushStringAnnotation(tag = "privacy_policy", annotation = "https://share.note.youdao.com/s/KjmsBvUB")
                        withStyle(style = SpanStyle(color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)) {
                            append("《隐私政策》")
                        }
                        pop()
                        append("条款。")
                    }
                    
                    Text(
                        text = introText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF374151)),
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .clickable {
                                // Handle user agreement link
                                val intent = Intent(Intent.ACTION_VIEW, "https://share.note.youdao.com/s/FUdL4QRe".toUri())
                                context.startActivity(intent)
                            }
                    )
                    
                    Text(
                        text = "为便于您了解我们如何收集、使用和保护您的个人信息，我们特别说明如下内容：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "在您使用本游戏服务的过程中：",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "• 设备信息收集：我们会收集您的Android ID等设备标识符，用于用户账号识别、登录认证、防作弊以及为您提供个性化服务。这些信息仅在您同意本隐私政策后才会收集；",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• 我们可能会申请存储权限，用于保存游戏数据；",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• 如果您需要语音聊天、视频或其他互动功能，我们可能会申请麦克风、摄像头权限；",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• 为了账号安全或活动奖励，我们可能会申请网络、位置等必要权限。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "上述权限均不会强制获取，且仅在您授权同意后才会启用相关功能。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "第三方SDK说明：",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                    )
                    
                    Text(
                        text = "• TapTap SDK：用于提供登录、实名认证和防沉迷服务，会在您同意本隐私政策后收集Android ID等设备信息，用于账号识别和合规认证。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "我们不会收集与游戏无关或强制用户开启的个人信息。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151)
                    )
                }
                
                // 按钮区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 拒绝按钮
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B7280),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "拒绝",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    // 同意按钮
                    Button(
                        onClick = onAgree,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "同意",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}



// 游戏速度下拉选项组件
@Composable
fun GameSpeedDropdown(
    currentSpeed: Int,
    isPaused: Boolean,
    onSpeedChange: (Int) -> Unit,
    onPauseToggle: () -> Unit,
    isSupporterUnlocked: Boolean = false, // 是否解锁支持者功能
    onShowFeatureLockedDialog: () -> Unit = {} // 显示功能解锁对话框的回调
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Box {
        // 下拉按钮 - 现代化设计，固定宽度避免抖动
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .height(32.dp)
                .widthIn(min = 58.dp, max = 58.dp) // 减少宽度，更紧凑
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF374151),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 1.dp,
                hoveredElevation = 3.dp
            )
        ) {
            Text(
                text = if (isPaused) "暂停" else "${currentSpeed}x",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 下拉菜单 - 现代化卡片设计
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    color = Color(0xFF1F2937),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF374151),
                    shape = RoundedCornerShape(10.dp)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(10.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .padding(vertical = 2.dp)
        ) {
            // 暂停选项 - 现代化样式
            DropdownMenuItem(
                text = {
                    Text(
                        text = "暂停",
                        color = if (isPaused) Color(0xFF10B981) else Color(0xFFE5E7EB),
                        fontSize = 14.sp,
                        fontWeight = if (isPaused) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                onClick = {
                    onPauseToggle()
                    expanded = false
                },
                modifier = Modifier
                    .background(
                        color = if (isPaused) Color(0xFF065F46).copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .animateContentSize()
            )
            
            // 速度选项 - 现代化样式
            listOf(1, 2, 3).forEach { speed ->
                val isLocked = speed > 1 && !isSupporterUnlocked
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (currentSpeed == speed && !isPaused) Color(0xFF10B981) else Color(0xFFE5E7EB),
                                fontSize = 14.sp,
                                fontWeight = if (currentSpeed == speed && !isPaused) FontWeight.SemiBold else FontWeight.Medium
                            )
                            if (isLocked) {
                                Text(
                                    text = "🔒",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    onClick = {
                        // 检查是否需要解锁
                        if (isLocked) {
                            expanded = false
                            onShowFeatureLockedDialog()
                            return@DropdownMenuItem
                        }
                        // 修复：如果当前是暂停状态，先取消暂停，然后切换速度
                        // 这样用户可以正常切换速度
                        if (isPaused) {
                            // 先取消暂停
                            onPauseToggle()
                        }
                        // 切换速度
                        onSpeedChange(speed)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(
                            color = if (currentSpeed == speed && !isPaused) Color(0xFF065F46).copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .animateContentSize()
                )
            }
        }
    }
}

/**
 * 赛事菜单对话框（从底部弹出）
 */
@Composable
fun TournamentMenuDialog(
    onDismiss: () -> Unit,
    onTournamentManagement: () -> Unit,
    onGVAConference: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1a1a2e),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(vertical = 16.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            // 标题
            Text(
                text = "🏆 赛事功能",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 赛事管理选项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTournamentManagement)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = "赛事管理",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "举办和管理游戏赛事",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 分隔线
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            // GVA大会选项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onGVAConference)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏅",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = "GVA大会",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "年度游戏行业盛会",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 竞争对手菜单对话框（从底部弹出）
 */
@Composable
fun CompetitorMenuDialog(
    onDismiss: () -> Unit,
    onCompetitorManagement: () -> Unit,
    onSubsidiaryManagement: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1a1a2e),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(vertical = 16.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            // 标题
            Text(
                text = "🎯 竞争对手功能",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 竞争对手管理选项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCompetitorManagement)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = "竞争对手",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "查看排行榜、新闻和收购对手",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 分隔线
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            // 子公司管理选项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSubsidiaryManagement)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏭",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = "子公司管理",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "管理已收购的子公司",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 功能未解锁对话框
 * 当玩家点击未解锁的功能时显示
 */
@Composable
fun FeatureLockedDialog(
    onDismiss: () -> Unit,
    onOpenAfdian: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "功能未解锁",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "该功能需通过爱发电赞助解锁，您的支持将直接用于游戏内容迭代与开发。",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 22.sp
            )
        },
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(20.dp),
        confirmButton = {
            Button(
                onClick = onOpenAfdian,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("去爱发电支持", color = Color.White, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("稍后再说", fontSize = 15.sp)
            }
        }
    )
}

/**
 * 分析FPS下降的可能原因
 */
private fun getPossibleCause(currentFps: Int, lastFps: Int, memoryPercent: Int, stutterPercent: Int): String {
    return buildString {
        if (currentFps < lastFps) {
            // FPS下降
            when {
                memoryPercent > 80 -> append("内存占用过高($memoryPercent%)，可能触发GC；")
                stutterPercent > 30 -> append("大量卡顿帧($stutterPercent%)，主线程可能被阻塞；")
                currentFps < 30 -> append("严重性能问题，可能是复杂的UI绘制或计算；")
                else -> append("性能下降，可能是后台任务增加；")
            }
        } else {
            // FPS提升
            append("性能恢复，卡顿原因已消除")
        }
    }
}

/**
 * FPS监测组件 - 增强版
 * 显示当前帧率，并记录详细的性能分析日志
 */
@Composable
fun FpsMonitor(
    modifier: Modifier = Modifier
) {
    var fps by remember { mutableIntStateOf(60) }
    val coroutineScope = rememberCoroutineScope()
    
    // 使用Choreographer监测真实帧率
    DisposableEffect(Unit) {
        var frameCount = 0
        var lastTime = System.currentTimeMillis()
        var lastFps = 60
        var frameTimings = mutableListOf<Long>()
        var lastFrameTime = System.nanoTime()
        
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                
                // 记录帧间隔时间
                val frameInterval = (frameTimeNanos - lastFrameTime) / 1_000_000 // 转换为毫秒
                frameTimings.add(frameInterval)
                lastFrameTime = frameTimeNanos
                
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        Choreographer.getInstance().postFrameCallback(frameCallback)
        
        // 每秒计算一次FPS并输出详细日志
        val updateJob = coroutineScope.launch {
            while (true) {
                delay(1000)
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastTime
                if (elapsed > 0) {
                    val calculatedFps = ((frameCount * 1000L) / elapsed).toInt().coerceIn(0, 144)
                    
                    // 输出详细的FPS日志
                    if (ENABLE_FPS_LOG) {
                        // 计算帧间隔统计数据
                        val avgFrameTime = if (frameTimings.isNotEmpty()) {
                            frameTimings.average()
                        } else {
                            0.0
                        }
                        val maxFrameTime = frameTimings.maxOrNull() ?: 0L
                        val minFrameTime = frameTimings.minOrNull() ?: 0L
                        
                        // 统计卡顿帧（超过33ms，即低于30fps）
                        val stutterFrames = frameTimings.count { it > 33 }
                        val stutterPercent = if (frameTimings.isNotEmpty()) {
                            (stutterFrames * 100.0 / frameTimings.size).toInt()
                        } else {
                            0
                        }
                        
                        // FPS变化检测
                        val fpsChange = calculatedFps - lastFps
                        val changeIndicator = when {
                            fpsChange > 10 -> "📈 大幅提升"
                            fpsChange > 5 -> "↗️ 提升"
                            fpsChange < -10 -> "📉 大幅下降"
                            fpsChange < -5 -> "↘️ 下降"
                            else -> "➡️ 稳定"
                        }
                        
                        // 获取内存信息
                        val runtime = Runtime.getRuntime()
                        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
                        val maxMemory = runtime.maxMemory() / 1024 / 1024
                        val memoryPercent = (usedMemory * 100 / maxMemory).toInt()
                        
                        // 基础日志
                        Log.d("FPSMonitor", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d("FPSMonitor", "⏱️ 当前FPS: $calculatedFps ($changeIndicator, 变化: ${if (fpsChange >= 0) "+" else ""}$fpsChange)")
                        Log.d("FPSMonitor", "📊 帧统计: 平均=${String.format("%.1f", avgFrameTime)}ms, 最大=${maxFrameTime}ms, 最小=${minFrameTime}ms")
                        Log.d("FPSMonitor", "⚠️ 卡顿帧: $stutterFrames/${frameTimings.size} ($stutterPercent%)")
                        Log.d("FPSMonitor", "💾 内存: ${usedMemory}MB/${maxMemory}MB ($memoryPercent%)")
                        
                        // FPS下降严重时，输出额外的诊断信息
                        if (calculatedFps < 40) {
                            Log.w("FPSMonitor", "🔴 性能警告: FPS低于40帧！")
                            
                            // 检查线程状态
                            val threadCount = Thread.activeCount()
                            Log.w("FPSMonitor", "🧵 活跃线程数: $threadCount")
                            
                            // 检查GC状态
                            if (memoryPercent > 80) {
                                Log.w("FPSMonitor", "⚠️ 内存占用过高 (${memoryPercent}%)，可能触发GC")
                            }
                            
                            // 帧时间分析
                            if (maxFrameTime > 100) {
                                Log.w("FPSMonitor", "⚠️ 检测到严重卡顿帧: ${maxFrameTime}ms (应小于16.7ms)")
                            }
                        }
                        
                        // FPS剧烈变化时输出警告
                        if (abs(fpsChange) > 15) {
                            Log.w("FPSMonitor", "⚡ FPS剧烈变化: $lastFps → $calculatedFps (${if (fpsChange > 0) "+" else ""}$fpsChange)")
                            Log.w("FPSMonitor", "可能原因: ${getPossibleCause(calculatedFps, lastFps, memoryPercent, stutterPercent)}")
                        }
                        
                        Log.d("FPSMonitor", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    }
                    
                    lastFps = calculatedFps
                    fps = calculatedFps
                    frameCount = 0
                    frameTimings.clear()
                    lastTime = currentTime
                }
            }
        }
        
        onDispose {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
            updateJob.cancel()
        }
    }
    
    Box(
        modifier = modifier
            .width(42.dp)
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$fps",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                fps >= 110 -> Color(0xFF10B981) // 绿色：性能优秀（接近120fps）
                fps >= 55 -> Color(0xFF3B82F6) // 蓝色：性能良好（60fps左右）
                fps >= 30 -> Color(0xFFF59E0B) // 黄色：性能一般
                else -> Color(0xFFEF4444) // 红色：性能较差
            },
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YjcyTheme {
        // MainMenuScreen() - 需要NavController参数
    }
}