package com.example.yjcy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Display
import android.view.WindowManager
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.util.Log
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
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.view.Choreographer
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
import androidx.navigation.NavController
import java.util.Calendar
import java.text.SimpleDateFormat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yjcy.data.Employee
import com.example.yjcy.data.Founder
import com.example.yjcy.data.FounderProfession
import com.example.yjcy.data.Game
import com.example.yjcy.data.GameRatingCalculator
import com.example.yjcy.data.GameReleaseStatus
import com.example.yjcy.data.GameRevenue
import com.example.yjcy.data.RevenueManager
import com.example.yjcy.data.SaveData
import com.example.yjcy.data.DevelopmentPhase
import com.example.yjcy.data.GameDate
import com.example.yjcy.ui.BadgeBox
import com.example.yjcy.ui.EmployeeManagementContent
import com.example.yjcy.ui.GameRatingDialog
import com.example.yjcy.ui.GameReleaseDialog
import com.example.yjcy.ui.ProjectManagementWrapper
import com.example.yjcy.ui.ProjectDisplayType
import com.example.yjcy.ui.ServerManagementContent
import com.example.yjcy.ui.TournamentScreen
import com.example.yjcy.ui.TournamentResultDialog
import com.example.yjcy.ui.theme.YjcyTheme
import com.example.yjcy.utils.formatMoney
import com.example.yjcy.utils.formatMoneyWithDecimals
import com.example.yjcy.utils.calculateWeekday
import com.example.yjcy.utils.getWeekdayName
import com.example.yjcy.utils.calculateGameTime
import com.example.yjcy.service.JobPostingService
import com.example.yjcy.service.CustomerServiceManager
import com.example.yjcy.data.getUpdateContentName
import com.example.yjcy.data.getRecommendedPrice
import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.data.CompetitorCompany
import com.example.yjcy.data.CompetitorNews
import com.example.yjcy.data.CompetitorManager
import com.example.yjcy.data.GameIP
import com.example.yjcy.data.Complaint
import com.example.yjcy.data.ComplaintStatus
import com.example.yjcy.data.Achievement
import com.example.yjcy.data.Achievements
import com.example.yjcy.data.AchievementCategory
import com.example.yjcy.data.UnlockedAchievement
import com.example.yjcy.managers.AchievementManager
import com.example.yjcy.ui.AchievementPopupQueue
import com.example.yjcy.ui.CompetitorContent
import com.example.yjcy.ui.calculatePlayerMarketValue
import com.example.yjcy.ui.SecretaryChatScreen
import com.example.yjcy.ui.SecretaryChatDialog
import com.example.yjcy.data.ChatMessage
import com.example.yjcy.data.MessageSender
import com.example.yjcy.ui.GVAScreen
import com.example.yjcy.ui.GVAAwardDialog
import com.example.yjcy.ui.SalaryRequestDialog
import com.example.yjcy.ui.YearEndBonusDialog
import com.example.yjcy.ui.YearEndStatistics
import com.example.yjcy.data.GVAManager
import com.example.yjcy.data.CompanyReputation
import com.example.yjcy.data.AwardRecord
import com.example.yjcy.data.AwardReward
import com.example.yjcy.data.AwardNomination
import com.example.yjcy.data.SecretaryReplyManager
import com.example.yjcy.ui.rememberTutorialState
import com.example.yjcy.ui.TutorialDialog
import com.example.yjcy.ui.TutorialTrigger
import com.example.yjcy.data.TutorialId
import com.example.yjcy.data.EsportsTournament
import com.example.yjcy.data.TournamentStatus
import com.example.yjcy.data.TournamentManager
import com.example.yjcy.data.MonetizationConfig
import com.example.yjcy.data.MonetizationItem
import com.example.yjcy.data.GameUpdate
import com.example.yjcy.utils.CommentGenerator
import com.example.yjcy.utils.SensitiveWordFilter
import com.example.yjcy.utils.SignatureHelper
import com.example.yjcy.data.NewsType
import com.example.yjcy.taptap.TapUpdateManager
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random
import kotlinx.coroutines.delay
import com.example.yjcy.taptap.TapLoginManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yjcy.ui.taptap.TapLoginViewModel




// 全局变量存储当前加载的存档数据
var currentLoadedSaveData: SaveData? = null

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
            (application as? YjcyApplication)?.initTapSDKIfNeeded()
            
            // 延迟500ms后检查更新，确保SDK完全初始化
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("MainActivity", "开始检查TapTap更新...")
                TapUpdateManager.checkForceUpdate()
            }, 500)
        } else {
            Log.d("MainActivity", "用户未同意隐私政策，等待用户同意后再初始化SDK")
        }
        
        setContent {
            YjcyTheme {
                val navController = rememberNavController()
                
                // 使用外部已创建的sharedPreferences
                var showPrivacyDialog by remember { mutableStateOf(!hasAgreedPrivacy) }
                
                // TapTap登录状态检查（Activity重启后会重新检查）
                var isTapTapLoggedIn by remember { mutableStateOf(TapLoginManager.isLoggedIn()) }
                
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
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
                        bestRefreshRate = refreshRate
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
        // 背景粒子动画
        ParticleBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo展示
            Text(
                text = "🎮 游创纪元",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "打造你的游戏帝国",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // TapTap登录卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎮 TapTap 登录",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "请先登录TapTap账号",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 登录按钮
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
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
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
                                fontWeight = FontWeight.SemiBold,
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
    @Suppress("SpellCheckingInspection") dontShowToday: Boolean,
    onDontShowTodayChange: (Boolean) -> Unit
) {
    // 一键加群功能
    fun joinQQGroup() {
        try {
            // QQ群号
            @Suppress("SpellCheckingInspection")
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
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 12f else 8f,
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
                spotColor = if (isPressed) item.gradient.first().copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = {
            isPressed = true
            coroutineScope.launch {
                delay(150)
                isPressed = false
            }
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

// 现代化的游戏背景
@Composable
fun ModernGameBackground() {
    // 网格背景效果
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_offset"
    )
    
    val density = LocalDensity.current
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // 绘制网格线
        val gridSize = with(density) { 50.dp.toPx() }
        val offsetX = gridOffset % gridSize
        
        // 垂直线
        var x = offsetX
        while (x < size.width) {
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSize
        }
        
        // 水平线
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }
        
        // 添加一些装饰性的圆形
        val circles = listOf(
            Offset(size.width * 0.1f, size.height * 0.2f) to 100f,
            Offset(size.width * 0.9f, size.height * 0.3f) to 150f,
            Offset(size.width * 0.15f, size.height * 0.8f) to 80f,
            Offset(size.width * 0.85f, size.height * 0.7f) to 120f
        )
        
        circles.forEach { (center, radius) ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3B82F6).copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
    
    // 粒子效果（保留原有的粒子效果但优化）
    ParticleBackground()
}

@Composable
fun ParticleBackground() {
    // 减少粒子数量，降低性能消耗
    val particles = remember {
        List(8) { // 从20个减少到8个
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 2f, // 稍微减小粒子大小
                speed = Random.nextFloat() * 0.015f + 0.01f, // 稍微减慢速度
                alpha = Random.nextFloat() * 0.4f + 0.15f // 降低透明度范围
            )
        }
    }
    
    // 使用更长的动画时间，减少更新频率
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000) // 从10秒增加到15秒，减少更新频率
        ),
        label = "particle_animation"
    )
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // 直接在Canvas中绘制，移除sin计算以提升性能
        particles.forEach { particle ->
            val currentY = (particle.y + animationProgress * particle.speed) % 1f
            // 移除sin函数计算，使用简单的线性移动
            val currentX = particle.x
            
            drawCircle(
                color = Color.White.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(
                    x = currentX * size.width,
                    y = currentY * size.height
                )
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

@Composable
fun GameMenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
    var money by remember { mutableLongStateOf(saveData?.money ?: 3000000L) }
    var fans by remember { mutableLongStateOf(saveData?.fans ?: 0L) }
    var currentYear by remember { mutableIntStateOf(saveData?.currentYear ?: 1) }
    var currentMonth by remember { mutableIntStateOf(saveData?.currentMonth ?: 1) }
    var currentDay by remember { mutableIntStateOf(saveData?.currentDay ?: 1) }
    // 当天内的分钟数（0-1439，一天1440分钟）
    var currentMinuteOfDay by remember { mutableIntStateOf(saveData?.currentMinuteOfDay ?: 0) }
    var gameSpeed by remember { mutableIntStateOf(3) }  // 默认3倍速
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var showTournamentMenu by remember { mutableStateOf(false) }
    var tournamentInitialTab by remember { mutableIntStateOf(0) }
    
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
    
    // 客诉数据状态
    var complaints by remember { mutableStateOf(saveData?.complaints ?: emptyList()) }
    var autoProcessComplaints by remember { mutableStateOf(saveData?.autoProcessComplaints ?: false) }
    var autoPromotionThreshold by remember { mutableFloatStateOf(saveData?.autoPromotionThreshold ?: 0.5f) }
    
    // GM模式状态
    var gmModeEnabled by remember { mutableStateOf(saveData?.gmModeEnabled ?: false) }
    
    // 自动存档设置
    var autoSaveEnabled by remember { mutableStateOf(saveData?.autoSaveEnabled ?: false) }
    var autoSaveInterval by remember { mutableIntStateOf(saveData?.autoSaveInterval ?: 5) } // 自动存档间隔（分钟）
    var lastAutoSaveMinute by remember { mutableIntStateOf(saveData?.lastAutoSaveMinute ?: 0) } // 上次自动存档的分钟数
    
    // 已使用的兑换码状态
    var usedRedeemCodes by remember { mutableStateOf(saveData?.usedRedeemCodes ?: emptySet()) }
    
    // GVA颁奖对话框状态
    var showGVAAwardDialog by remember { mutableStateOf(false) }
    var gvaAwardYear by remember { mutableIntStateOf(1) }
    var gvaAwardNominations by remember { mutableStateOf(emptyList<AwardNomination>()) }
    var gvaPlayerWonCount by remember { mutableIntStateOf(0) }
    var gvaPlayerTotalReward by remember { mutableLongStateOf(0L) }
    var gvaPlayerFansGain by remember { mutableLongStateOf(0L) }
    
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
            
            // 为已发售但没有收益数据的游戏初始化数据（向后兼容旧存档）
            saveData.games
                .filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
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
                        RevenueManager.updateGameInfo(
                            releasedGame.id,
                            releasedGame.businessModel,
                            releasedGame.monetizationItems
                        )
                        // 初始化游戏IP信息（用于销量加成）
                        RevenueManager.updateGameIP(releasedGame.id, releasedGame.fromIP)
                    } else {
                        // 收益数据存在，更新游戏信息（商业模式和付费内容）
                        RevenueManager.updateGameInfo(
                            releasedGame.id,
                            releasedGame.businessModel,
                            releasedGame.monetizationItems
                        )
                        // 更新游戏IP信息（用于销量加成）
                        RevenueManager.updateGameIP(releasedGame.id, releasedGame.fromIP)
                    }
                }
            
            // 调整低评分游戏的历史销量（旧存档兼容）- 必须在游戏信息设置之后
            saveData.games
                .filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
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
                    game.copy(monetizationItems = monetizationItems)
                } else {
                    game
                }
            }
            if (needUpdateGames) {
                games = updatedGames
                Log.d("GameScreen", "【实例 $instanceId】✓ 已更新子公司网游的付费内容")
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
            Log.d("GameScreen", "【实例 $instanceId】===== 读档数据恢复完成 =====")
        } else {
            // ===== 新游戏：清空旧数据 =====
            Log.d("GameScreen", "【实例 $instanceId】===== 新游戏模式：清空旧数据 =====")
            RevenueManager.clearAllData()
            jobPostingService.clearAllData()
            Log.d("GameScreen", "【实例 $instanceId】✓ 清空招聘岗位数据")
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
    
    // 初始化竞争对手（只执行一次）
    LaunchedEffect(Unit) {
        if (competitors.isEmpty()) {
            // 生成初始竞争对手（新游戏或继承后的存档都会触发）
            competitors = CompetitorManager.generateInitialCompetitors(
                companyName, 
                currentYear, 
                currentMonth
            )
            Log.d("MainActivity", "初始化竞争对手：生成${competitors.size}家竞争公司")
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
            hasCheckedInitialAchievements = true
            
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
    
    
    // 时间推进系统
    LaunchedEffect(gameSpeed, isPaused) {
        while (!isPaused) {
            delay(when (gameSpeed) {
                1 -> 100L // 慢速：0.1秒1分钟（1440分钟需要144秒=2.4分钟）
                2 -> 50L // 中速：0.05秒1分钟（1440分钟需要72秒=1.2分钟）
                3 -> 20L // 快速：0.02秒1分钟（1440分钟需要28.8秒=0.48分钟，比原来快约1.65倍）
                else -> 50L
            })
            
            // 更新时间：每0.1秒（1倍速）推进1分钟
            currentMinuteOfDay++
            
            // 每分钟更新已发售游戏的销量（实时更新）
            games.filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
                .forEach { releasedGame ->
                    // 更新游戏信息（商业模式和付费内容）
                    RevenueManager.updateGameInfo(
                        releasedGame.id,
                        releasedGame.businessModel,
                        releasedGame.monetizationItems
                    )
                    
                    // 传入游戏评分、粉丝数和当前时间，实时更新销量
                    val gameRating = releasedGame.gameRating?.finalScore
                    val reputationLevel = companyReputation.getLevel()
                    val minuteRevenue = RevenueManager.addMinuteRevenueForGame(
                        gameId = releasedGame.id,
                        gameRating = gameRating,
                        fanCount = fans,
                        currentYear = currentYear,
                        currentMonth = currentMonth,
                        currentDay = currentDay,
                        currentMinuteOfDay = currentMinuteOfDay,
                        reputationBonus = reputationLevel.salesBonus
                    )
                    money += minuteRevenue.toLong()
                }
            
            // 自动存档检查（如果启用了自动存档）
            if (autoSaveEnabled) {
                try {
                    // 计算从上次存档到现在经过的分钟数
                    val minutesSinceLastSave = if (lastAutoSaveMinute == 0) {
                        // 首次运行，使用当前分钟数
                        currentMinuteOfDay
                    } else {
                        // 计算经过的分钟数（考虑跨天情况）
                        if (currentMinuteOfDay >= lastAutoSaveMinute) {
                            currentMinuteOfDay - lastAutoSaveMinute
                        } else {
                            // 跨天了，加上1440分钟
                            (1440 - lastAutoSaveMinute) + currentMinuteOfDay
                        }
                    }
                    
                    // 如果达到存档间隔，执行自动存档（完全异步，不阻塞时间循环）
                    if (minutesSinceLastSave >= autoSaveInterval) {
                        // 立即更新上次存档时间标记，避免重复触发（在主线程立即更新）
                        lastAutoSaveMinute = currentMinuteOfDay
                        
                        // 在LaunchedEffect内部可以直接使用launch，因为LaunchedEffect本身就是协程作用域
                        launch {
                            try {
                                // 所有数据导出操作都在后台线程执行，不阻塞主循环
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
                                    currentMinuteOfDay = currentMinuteOfDay,
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
                                    unlockedAchievements = unlockedAchievements,
                                    completedTutorials = tutorialState.getCompletedTutorialsForSave(),
                                    skipTutorial = tutorialState.skipTutorial.value,
                                    companyReputation = companyReputation,
                                    gvaHistory = gvaHistory,
                                    currentYearNominations = currentYearNominations,
                                    gvaAnnouncedDate = gvaAnnouncedDate,
                                    ownedIPs = ownedIPs,
                                    gmModeEnabled = gmModeEnabled,
                                    usedRedeemCodes = usedRedeemCodes,
                                    autoSaveEnabled = autoSaveEnabled,
                                    autoSaveInterval = autoSaveInterval,
                                    lastAutoSaveMinute = currentMinuteOfDay,
                                    saveTime = System.currentTimeMillis(),
                                    version = BuildConfig.VERSION_NAME
                                )
                                
                                val result = saveManager.saveGameAsync(1, saveData)
                                if (result.success) {
                                    Log.d("MainActivity", "💾 自动存档成功（存档位1，间隔${autoSaveInterval}分钟）")
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
            
            // 当分钟数达到1440（一天24小时）时，推进日期
            if (currentMinuteOfDay >= 1440) {
                currentMinuteOfDay = 0 // 重置为0:00
                
                // 更新日期
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
                
                // 每日检查：扣除到期服务器的月费（按购买日期每30天计费）
                Log.d("MainActivity", "准备调用服务器扣费检查... 当前日期: ${currentYear}年${currentMonth}月${currentDay}日")
                val moneyBefore = money
                val serverBillingCost = RevenueManager.checkAndBillServers(
                    currentYear = currentYear,
                    currentMonth = currentMonth,
                    currentDay = currentDay
                )
                Log.d("MainActivity", "服务器扣费检查完成，返回金额: ¥$serverBillingCost")
                if (serverBillingCost > 0) {
                    money -= serverBillingCost
                    Log.d("MainActivity", "💰 服务器计费: -¥$serverBillingCost (扣费前:¥$moneyBefore -> 扣费后:¥$money)")
                }
                
                // 每日检查：员工忠诚度变化（如果薪资低于期望薪资，忠诚度会逐渐降低）
                try {
                    val updatedEmployees2 = allEmployees.map { employee ->
                        if (!employee.isFounder && employee.requestedSalary == null) {
                            // 计算员工期望的薪资
                            val expectedSalary = employee.calculateExpectedSalary(employee.salary)
                            if (employee.salary < expectedSalary) {
                                // 薪资低于期望，每月降低1点忠诚度（每天约0.033点）
                                val loyaltyLoss = if (currentDay == 1) 1 else 0 // 每月1日降低1点
                                employee.copy(loyalty = (employee.loyalty - loyaltyLoss).coerceAtLeast(0))
                            } else {
                                // 薪资满足期望，每月恢复1点忠诚度（每天约0.033点）
                                val loyaltyGain = if (currentDay == 1) 1 else 0 // 每月1日恢复1点
                                employee.copy(loyalty = (employee.loyalty + loyaltyGain).coerceAtMost(100))
                            }
                        } else {
                            employee
                        }
                    }
                    allEmployees.clear()
                    allEmployees.addAll(updatedEmployees2)
                } catch (e: Exception) {
                    Log.e("MainActivity", "更新员工忠诚度失败", e)
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
                        // 基于已发售游戏数量和平均评分计算粉丝增长
                        val avgRating = releasedGames.mapNotNull { it.gameRating?.finalScore }.average().toFloat()
                        val gameCountMultiplier = 1.0 + (releasedGames.size * 0.1) // 每个游戏增加10%增长率
                        
                        val baseFansGrowth = when {
                            avgRating >= 8.0f -> (fans * 0.025).toLong() // 2.5%增长（高评分）（原5%）
                            avgRating >= 6.0f -> (fans * 0.015).toLong() // 1.5%增长（中等评分）（原3%）
                            else -> (fans * 0.005).toLong() // 0.5%增长（低评分）（原1%）
                        }
                        
                        // 应用声望加成
                        val reputationLevel = companyReputation.getLevel()
                        val reputationBonus = reputationLevel.fansBonus
                        val reputationMultiplier = 1.0 + reputationBonus
                        
                        val totalFansGrowth = (baseFansGrowth * gameCountMultiplier * reputationMultiplier).toLong().coerceAtLeast(100L)
                        fans = (fans + totalFansGrowth).coerceAtLeast(0L)
                        
                        Log.d("MainActivity", "月结算粉丝增长: +$totalFansGrowth (游戏数:${releasedGames.size}, 平均评分:$avgRating, 声望加成:+${(reputationBonus*100).toInt()}%, 当前粉丝:$fans)")
                    }
                    
                    // 月结算：宣传指数衰减
                    games = games.map { game ->
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
                            
                            // 日志输出衰减信息
                            if (game.promotionIndex != newPromotionIndex) {
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
                            
                            game.copy(promotionIndex = newPromotionIndex)
                        } else {
                            game
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
                            money -= totalCost
                            fans += selectedPromotionType.fansGain * gamesNeedingPromotion.size
                            
                            // 更新所有需要宣传的游戏的宣传指数
                            games = games.map { game ->
                                if (gamesNeedingPromotion.any { it.id == game.id }) {
                                    val newPromotionIndex = (game.promotionIndex + selectedPromotionType.promotionIndexGain).coerceAtMost(1.0f)
                                    game.copy(promotionIndex = newPromotionIndex)
                                } else {
                                    game
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
                    
                    // 月结算：生成客诉
                    val newComplaints = CustomerServiceManager.generateMonthlyComplaints(
                        games,
                        currentYear,
                        currentMonth,
                        currentDay
                    )
                    if (newComplaints.isNotEmpty()) {
                        complaints = complaints + newComplaints
                        Log.d("MainActivity", "月结算：生成${newComplaints.size}个新客诉")
                    }
                    
                    // 月结算：清理旧客诉
                    complaints = CustomerServiceManager.cleanupOldComplaints(complaints)
                    
                    // 更新上次月结算时间
                    lastSettlementYear = currentYear
                    lastSettlementMonth = currentMonth
                    
                    // 月结算：扣除员工工资
                    val totalSalaryCost = allEmployees.sumOf { it.salary }
                    if (totalSalaryCost > 0) {
                        money -= totalSalaryCost
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
                            money -= totalCost
                            fans += selectedPromotionType.fansGain * gamesNeedingPromotion.size
                            
                            // 更新所有需要宣传的游戏的宣传指数
                            games = games.map { game ->
                                if (gamesNeedingPromotion.any { it.id == game.id }) {
                                    val newPromotionIndex = (game.promotionIndex + selectedPromotionType.promotionIndexGain).coerceAtMost(1.0f)
                                    game.copy(promotionIndex = newPromotionIndex)
                                } else {
                                    game
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
                money += totalCashReward
                fans += totalFansReward
                companyReputation = companyReputation.addReputation(totalReputationGain)
                
                // 更新获奖游戏的awards字段
                games = games.map { game ->
                    if (game.id in winnerGameIds) {
                        val wonAwards = finalNominations
                            .filter { it.winner?.gameId == game.id }
                            .map { it.award }
                        game.copy(awards = (game.awards + wonAwards).distinct())
                    } else {
                        game
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
                // 计算年度统计数据
                val gamesReleasedThisYear = games.count { game ->
                    game.releaseYear == currentYear && 
                    (game.releaseStatus == GameReleaseStatus.RELEASED || 
                     game.releaseStatus == GameReleaseStatus.RATED)
                }
                
                // 计算年度总收入（从RevenueManager获取，统计所有已发售游戏在当年的收入）
                val totalRevenue = RevenueManager.exportRevenueData()
                    .values
                    .flatMap { revenue ->
                        revenue.dailySalesList.filter { dailySales ->
                            // 直接从recordDate中提取游戏内年份
                            // recordDate是用游戏内时间创建的，所以其中的YEAR字段就是游戏内年份
                            val recordCalendar = java.util.Calendar.getInstance()
                            recordCalendar.time = dailySales.date
                            val recordGameYear = recordCalendar.get(java.util.Calendar.YEAR)
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
                
                val totalDevelopmentCost = games
                    .filter { it.releaseYear == currentYear }
                    .sumOf { it.developmentCost }
                
                val totalExpenses = totalSalary + totalServerCost + totalDevelopmentCost
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
                    
                    // 显示涨薪请求对话框（保存当前的涨薪次数）
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
                            }
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
            
            // 更新游戏开发进度（分阶段系统）
            // 计算当前星期几和时间
            val currentWeekday = com.example.yjcy.utils.calculateWeekday(currentYear, currentMonth, currentDay)
            val currentHour = currentMinuteOfDay / 60
            val currentMinute = currentMinuteOfDay % 60
            
            games = games.map { game ->
                if (!game.isCompleted && game.assignedEmployees.isNotEmpty()) {
                    val currentPhase = game.currentPhase
                    
                    // 检查当前阶段是否有足够的员工
                    if (!currentPhase.checkRequirements(game.assignedEmployees)) {
                        // 没有满足要求的员工，进度不增长
                        return@map game
                    }
                    
                    // 计算当前阶段的进度增长
                    val phaseProgressIncrease = currentPhase.calculateProgressSpeed(game.assignedEmployees)
                    val newPhaseProgress = (game.phaseProgress + phaseProgressIncrease).coerceAtMost(1.0f)
                    
                    // 检查当前阶段是否完成
                    if (newPhaseProgress >= 1.0f) {
                        // 当前阶段完成，进入下一阶段
                        val nextPhase = currentPhase.getNextPhase()
                        
                        if (nextPhase != null) {
                            // 进入下一阶段
                            val updatedGame = game.copy(
                                currentPhase = nextPhase,
                                phaseProgress = 0f,
                                developmentProgress = when (nextPhase) {
                                    DevelopmentPhase.DESIGN -> 0f // 不应该发生
                                    DevelopmentPhase.ART_SOUND -> 0.33f // 需求文档完成
                                    DevelopmentPhase.PROGRAMMING -> 0.66f // 美术音效完成
                                },
                                assignedEmployees = emptyList() // 清空员工，让玩家重新分配
                            )
                            updatedGame
                        } else {
                            // 所有阶段完成，游戏开发完成
                            val gameRating = GameRatingCalculator.calculateRating(game)
                            val completedGame = game.copy(
                                developmentProgress = 1.0f,
                                phaseProgress = 1.0f,
                                isCompleted = true,
                                rating = gameRating.finalScore,
                                gameRating = gameRating,
                                releaseStatus = GameReleaseStatus.READY_FOR_RELEASE,
                                assignedEmployees = emptyList()
                            )
                            
                            // 先显示评分对话框
                            pendingRatingGame = completedGame
                            showRatingDialog = true
                            
                            completedGame
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
                        
                        // 更新assignedEmployees中的员工信息（同步体力值），保留所有已分配的员工（包括休息中的）
                        val updatedAssignedEmployees = game.assignedEmployees.map { assignedEmployee ->
                            allEmployees.find { it.id == assignedEmployee.id } ?: assignedEmployee
                        }
                        
                        game.copy(
                            phaseProgress = newPhaseProgress,
                            developmentProgress = newTotalProgress,
                            isCompleted = false,
                            assignedEmployees = updatedAssignedEmployees
                        )
                    }
                } else {
                    game
                }
            }
            
            // 注意：已发售游戏的收益现在在每分钟更新中实时计算，这里不再重复计算
            // 每天结束时只推进更新任务进度
            games.filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
                .forEach { releasedGame ->
                    // 更新游戏信息（商业模式和付费内容）
                    RevenueManager.updateGameInfo(
                        releasedGame.id,
                        releasedGame.businessModel,
                        releasedGame.monetizationItems
                    )
                    
                    // 收益已经在每分钟更新中实时计算，这里不再重复计算

                    // 在推进进度前先获取更新任务信息（因为完成后会被清除）
                    val completedTask = RevenueManager.getGameRevenue(releasedGame.id)?.updateTask
                    
                    // 若存在更新任务，根据已分配员工数量和技能等级推进进度
                    var employeesForUpdate = releasedGame.assignedEmployees
                    if (employeesForUpdate.isNotEmpty()) {
                        // 更新assignedEmployees中的员工信息
                        val updatedAssignedEmployees = employeesForUpdate.map { assignedEmployee ->
                            allEmployees.find { it.id == assignedEmployee.id } ?: assignedEmployee
                        }
                        
                        // 更新游戏中的assignedEmployees
                        games = games.map { game ->
                            if (game.id == releasedGame.id) {
                                game.copy(assignedEmployees = updatedAssignedEmployees)
                            } else {
                                game
                            }
                        }
                        
                        // 使用更新后的员工列表计算进度
                        employeesForUpdate = updatedAssignedEmployees
                    }
                    
                    val employeePoints = RevenueManager.calculateUpdateProgressPoints(employeesForUpdate)
                    val updateJustCompleted = RevenueManager.progressUpdateTask(releasedGame.id, employeePoints)
                    
                    // 如果更新刚刚完成，版本号+0.1
                    if (updateJustCompleted) {
                        // 使用之前保存的任务信息
                        
                        // 创建游戏更新记录
                        val newUpdateHistory = if (completedTask != null) {
                            val updateNumber = (releasedGame.updateHistory ?: emptyList()).size + 1
                            val updateDate = GameDate(currentYear, currentMonth, currentDay)
                            
                            // 生成玩家评论
                            val comments = CommentGenerator.generateComments(
                                updateContent = completedTask.features,
                                commentCount = Random.nextInt(5, 11)
                            )
                            
                            // 创建更新记录
                            val gameUpdate = GameUpdate(
                                updateNumber = updateNumber,
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
                        games = games.map { if (it.id == updatedGame.id) updatedGame else it }
                        
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
            
            // 自动处理模式：自动分配待处理的客诉
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
            
            // 每日处理客诉
            val (updatedComplaints, _) = CustomerServiceManager.processDailyComplaints(
                complaints,
                allEmployees
            )
            complaints = updatedComplaints
            
            // 计算超时客诉造成的粉丝损失
            val fanLoss: Long = CustomerServiceManager.calculateOverdueFanLoss(
                complaints,
                currentYear,
                currentMonth,
                currentDay
            )
            if (fanLoss > 0) {
                fans = (fans - fanLoss).coerceAtLeast(0L)
                Log.d("MainActivity", "客诉超时：粉丝流失 -$fanLoss，当前粉丝: $fans")
            }
            
            // 每日更新赛事
            games = games.map { game ->
                val tournament = game.currentTournament
                if (tournament != null && tournament.status != TournamentStatus.COMPLETED) {
                    val updatedTournament = TournamentManager.updateTournament(
                        tournament,
                        GameDate(currentYear, currentMonth, currentDay)
                    )
                    
                    // 检查是否刚完成
                    if (updatedTournament.status == TournamentStatus.COMPLETED && 
                        tournament.status != TournamentStatus.COMPLETED) {
                        // 结算赛事
                        val revenueData = RevenueManager.getGameRevenue(game.id)
                        if (revenueData != null) {
                            // 确定成功等级
                            val successLevel = TournamentManager.determineTournamentSuccess(
                                updatedTournament, game, 50f // TODO: 使用公司声誉
                            )
                            
                            // 计算收益
                            val revenue = TournamentManager.calculateTournamentRevenue(
                                updatedTournament, game, revenueData, successLevel
                            )
                            
                            // 应用效果
                            val (fansGained, playersGained, interestBonus) = TournamentManager.applyTournamentEffects(
                                updatedTournament, game, revenueData, fans, successLevel
                            )
                            
                            // 生成随机事件
                            val (eventDesc, _) = TournamentManager.generateRandomEvent()
                            
                            // 更新数据
                            money += revenue.totalRevenue
                            fans += fansGained
                            
                            // 更新收益数据的兴趣值（直接修改，RevenueManager会自动保存）
                            // Note: 这里简化处理，实际兴趣值会在月结算时自动衰减
                            
                            // 保存历史
                            val completedTournament = updatedTournament.copy(
                                sponsorRevenue = revenue.sponsorRevenue,
                                broadcastRevenue = revenue.broadcastRevenue,
                                ticketRevenue = revenue.ticketRevenue,
                                successLevel = successLevel,
                                fansGained = fansGained,
                                playersGained = playersGained,
                                interestBonus = interestBonus,
                                randomEvent = eventDesc
                            )
                            
                            val history = ((game.tournamentHistory ?: emptyList()) + completedTournament).takeLast(5)
                            
                            Log.d("MainActivity", "🏆 赛事完成: ${game.name} - ${updatedTournament.type.displayName}, 收益: ${formatMoney(revenue.totalRevenue)}, 粉丝+$fansGained")
                            
                            // 显示赛事完成弹窗
                            tournamentResult = completedTournament
                            showTournamentResultDialog = true
                            
                            game.copy(
                                currentTournament = null,
                                tournamentHistory = history
                            )
                        } else {
                            game.copy(currentTournament = updatedTournament)
                        }
                    } else {
                        game.copy(currentTournament = updatedTournament)
                    }
                } else {
                    game
                }
            }
            
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
        // FPS监测（左上角）
        FpsMonitor(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        
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
                minuteOfDay = currentMinuteOfDay,
                gameSpeed = gameSpeed,
                onSpeedChange = { gameSpeed = it },
                isPaused = isPaused,
                onPauseToggle = { isPaused = !isPaused },
                companyName = companyName,
                selectedLogo = selectedLogo,
                onSettingsClick = { showSettings = true }
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
                            onSecretaryChatClick = { showSecretaryChat = true }
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
                            currentMinuteOfDay = currentMinuteOfDay,
                            jobPostingRefreshTrigger = jobPostingRefreshTrigger,
                            onPauseGame = { isPaused = true },
                            onResumeGame = { isPaused = false }
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
                            currentMinuteOfDay = currentMinuteOfDay,
                            ownedIPs = ownedIPs,
                            onPauseGame = { isPaused = true },
                            onResumeGame = { isPaused = false }
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
                                ownedIPs = ownedIPs // 传递拥有的IP列表
                            ),
                            gameSpeed = gameSpeed,
                            onAcquisitionSuccess = { acquiredCompany: CompetitorCompany, finalPrice: Long, _: Long, fansGain: Long, inheritedIPs: List<GameIP> ->
                                // 扣除收购费用
                                money -= finalPrice
                                
                                // 增加粉丝
                                fans += fansGain
                                
                                // 移除被收购的公司
                                competitors = competitors.filter { it.id != acquiredCompany.id }
                                
                                // 将获得的IP添加到玩家的IP库
                                ownedIPs = ownedIPs + inheritedIPs
                                
                                Log.d("MainActivity", "收购成功：获得${inheritedIPs.size}个IP")
                                inheritedIPs.forEach { ip: GameIP ->
                                    Log.d("MainActivity", "  - IP: ${ip.name} (${ip.getIPLevel()}, 评分${ip.originalRating}, 加成${(ip.calculateIPBonus() * 100).toInt()}%)")
                                }
                                
                                // 生成收购新闻
                                competitorNews = (listOf(
                                    CompetitorNews(
                                        id = "news_${System.currentTimeMillis()}_${Random.nextInt()}",
                                        title = "${companyName}成功收购${acquiredCompany.name}！",
                                        content = "${companyName}以${formatMoney(finalPrice)}的价格成功收购了${acquiredCompany.name}，" +
                                                "获得了${inheritedIPs.size}个游戏IP，市值大幅增长。这是游戏行业的重大并购事件。",
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
                                    money -= tournament.investment
                                    
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
                    onTabSelected = { selectedTab = it },
                    pendingApplicantsCount = pendingApplicantsCount,
                    pendingAssignmentCount = pendingAssignmentCount,
                    onTournamentClick = { showTournamentMenu = true }
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
                            RevenueManager.updateGameInfo(
                                releasedGame.id,
                                releasedGame.businessModel,
                                releasedGame.monetizationItems
                            )
                            
                            // 更新游戏IP信息（用于销量加成）
                            RevenueManager.updateGameIP(releasedGame.id, releasedGame.fromIP)
                            
                            releasedGame
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
                            money += refund
                            
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
                            currentMinuteOfDay = currentMinuteOfDay,
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
                            unlockedAchievements = unlockedAchievements,
                            completedTutorials = tutorialState.getCompletedTutorialsForSave(),
                            skipTutorial = tutorialState.skipTutorial.value,
                            companyReputation = companyReputation,
                            gvaHistory = gvaHistory,
                            currentYearNominations = currentYearNominations,
                            gvaAnnouncedDate = gvaAnnouncedDate,
                            ownedIPs = ownedIPs, // 传递拥有的IP列表
                            gmModeEnabled = gmModeEnabled,
                            onGMToggle = { enabled -> gmModeEnabled = enabled },
                            autoSaveEnabled = autoSaveEnabled,
                            autoSaveInterval = autoSaveInterval,
                            lastAutoSaveMinute = lastAutoSaveMinute,
                            onAutoSaveEnabledToggle = { enabled -> autoSaveEnabled = enabled },
                            onAutoSaveIntervalChange = { interval -> autoSaveInterval = interval },
                            usedRedeemCodes = usedRedeemCodes,
                            onUsedRedeemCodesUpdate = { updatedCodes -> usedRedeemCodes = updatedCodes },
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
                                money += 10000000L
                            },
                            onCreateTopEmployees = {
                                // 创建各个职位6名5级专属技能员工
                                val existingNames = allEmployees.map { it.name }.toSet().toMutableSet()
                                val maxId = allEmployees.maxOfOrNull { it.id } ?: 0
                                val newEmployees = mutableListOf<Employee>()
                                
                                // 职位列表
                                val positions = listOf("程序员", "策划师", "美术师", "音效师", "客服")
                                
                                // 为每个职位创建6名5级专属技能员工
                                for (position in positions) {
                                    repeat(6) {
                                        val employeeName = com.example.yjcy.service.TalentMarketService.generateUniqueName(existingNames)
                                        existingNames.add(employeeName)
                                        
                                        // 根据职位设置专属技能为5级，其他技能为0
                                        val newEmployee = when (position) {
                                            "程序员" -> Employee(
                                                id = maxId + newEmployees.size + 1,
                                                name = employeeName,
                                                position = position,
                                                skillDevelopment = 5,
                                                skillDesign = 0,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = 15000, // 5级技能对应薪资
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
                                                skillDesign = 5,
                                                skillArt = 0,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = 15000,
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
                                                skillArt = 5,
                                                skillMusic = 0,
                                                skillService = 0,
                                                salary = 15000,
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
                                                skillMusic = 5,
                                                skillService = 0,
                                                salary = 15000,
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
                                                skillService = 5,
                                                salary = 15000,
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
                                                salary = 15000,
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
                                
                                // 添加新员工到列表
                                allEmployees.addAll(newEmployees)
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
            // 重新计算年度统计数据（确保数据最新）
            val gamesReleasedThisYear = games.count { game ->
                game.releaseYear == currentYear && 
                (game.releaseStatus == GameReleaseStatus.RELEASED || 
                 game.releaseStatus == GameReleaseStatus.RATED)
            }
            
            // 计算年度总收入（从RevenueManager获取，统计所有已发售游戏在当年的收入）
            val totalRevenue = RevenueManager.exportRevenueData()
                .values
                .flatMap { revenue ->
                    revenue.dailySalesList.filter { dailySales ->
                        // 直接从recordDate中提取游戏内年份
                        // recordDate是用游戏内时间创建的，所以其中的YEAR字段就是游戏内年份
                        val recordCalendar = java.util.Calendar.getInstance()
                        recordCalendar.time = dailySales.date
                        val recordGameYear = recordCalendar.get(java.util.Calendar.YEAR)
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
            
            val totalDevelopmentCost = games
                .filter { it.releaseYear == currentYear }
                .sumOf { it.developmentCost }
            
            val totalExpenses = totalSalary + totalServerCost + totalDevelopmentCost
            val netProfit = totalRevenue - totalExpenses
            
            val yearEndStatistics = YearEndStatistics(
                year = currentYear,
                gamesReleased = gamesReleasedThisYear,
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
                    money -= bonusAmount
                    
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
    minuteOfDay: Int, // 当天内的分钟数（0-1439）
    gameSpeed: Int,
    onSpeedChange: (Int) -> Unit,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    @Suppress("UNUSED_PARAMETER") companyName: String = "我的游戏公司", // 保留用于未来功能
    @Suppress("UNUSED_PARAMETER") selectedLogo: String = "🎮", // 保留用于未来功能
    onSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左边区域：资金和粉丝（垂直排列）
            Column(
                modifier = Modifier.weight(1f),
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
            
            // 中间区域：日期和游戏速度
            Column(
                modifier = Modifier.weight(1.5f),
                horizontalAlignment = Alignment.Start
            ) {
                // 日期和游戏速度下拉选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 日期列
                    Column {
                        // 日期
                        Text(
                            text = "第${year}年${month}月${day}日",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        // 星期几和时间
                        val weekday = calculateWeekday(year, month, day)
                        val gameTime = calculateGameTime(minuteOfDay)
                        Text(
                            text = "${getWeekdayName(weekday)}丨$gameTime",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    
                    // 游戏速度下拉选择
                    GameSpeedDropdown(
                        currentSpeed = gameSpeed,
                        isPaused = isPaused,
                        onSpeedChange = onSpeedChange,
                        onPauseToggle = onPauseToggle
                    )
                }
            }
            
            // 右边区域：设置按钮
            Box(
                modifier = Modifier.weight(1f),
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
    onSecretaryChatClick: () -> Unit = {}
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
            
            val financialData = remember(games.size, currentYear, currentMonth, currentDay, selectedFinancialYear, allEmployees.size) {
                derivedStateOf {
                    Log.d("MainActivity", "===== 财务状况计算开始 =====")
                    Log.d("MainActivity", "查询年份: 第${selectedFinancialYear}年")
                    
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
                                val recordCalendar = java.util.Calendar.getInstance()
                                recordCalendar.time = dailySales.date
                                val recordGameYear = recordCalendar.get(java.util.Calendar.YEAR)
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
                                    val recordCalendar = java.util.Calendar.getInstance()
                                    recordCalendar.time = dailySales.date
                                    val recordGameYear = recordCalendar.get(java.util.Calendar.YEAR)
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
                                    val recordCalendar = java.util.Calendar.getInstance()
                                    recordCalendar.time = dailySales.date
                                    val recordGameYear = recordCalendar.get(java.util.Calendar.YEAR)
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
                                    val recordCalendar = java.util.Calendar.getInstance()
                                    recordCalendar.time = dailySales.date
                                    val recordGameYear = recordCalendar.get(java.util.Calendar.YEAR)
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
                                    val recordCalendar = java.util.Calendar.getInstance()
                                    recordCalendar.time = dailySales.date
                                    val recordGameYear = recordCalendar.get(java.util.Calendar.YEAR)
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
    onTournamentClick: () -> Unit = {} // 赛事按钮点击事件
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
                showBadge = pendingApplicantsCount > 0,
                badgeCount = pendingApplicantsCount
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
                onClick = { onTabSelected(3) }
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
    showBadge: Boolean = false,
    @Suppress("UNUSED_PARAMETER") badgeCount: Int = 0
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
                        isUnlocked = achievement.id in unlockedIds,
                        progress = AchievementManager.getAchievementProgress(achievement, saveData, revenueData)
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
    isUnlocked: Boolean,
    @Suppress("UNUSED_PARAMETER") progress: Float // 保留用于未来显示进度功能
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
                    version = game.version
                )
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
                
                // 招聘系统
                jobPostings = saveData.jobPostings,
                
                // 服务器和收益数据
                serverData = saveData.serverData,
                revenueData = saveData.revenueData,
                
                // 创始人职业（可空）
                founderProfession = saveData.founderProfession
            )
            
            Log.d("SaveManager", "修复完成：游戏${fixedGames.size}个，员工${fixedSaveData.allEmployees.size}人")
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
        // 1. 清理收益数据：每个游戏只保留最近365天的每日销售数据
        val cleanedRevenueData = saveData.revenueData.mapValues { (gameId, revenue) ->
            if (revenue.dailySalesList.size > MAX_DAILY_SALES_DAYS) {
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
    currentMinuteOfDay: Int = 0, // 当天内的分钟数（0-1439）
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
    unlockedAchievements: List<UnlockedAchievement> = emptyList(),
    completedTutorials: Set<String> = emptySet(), // 新增：教程进度
    skipTutorial: Boolean = false, // 新增：跳过教程状态
    companyReputation: CompanyReputation = CompanyReputation(), // GVA：公司声望
    gvaHistory: List<AwardNomination> = emptyList(), // GVA：历史记录
    currentYearNominations: List<AwardNomination> = emptyList(), // GVA：当年提名
    gvaAnnouncedDate: GameDate? = null, // GVA：颁奖日期
    ownedIPs: List<GameIP> = emptyList(), // 拥有的游戏IP列表（收购竞争对手后获得）
    gmModeEnabled: Boolean = false, // GM模式是否开启
    onGMToggle: (Boolean) -> Unit = {}, // GM模式切换回调
    autoSaveEnabled: Boolean = false, // 自动存档开关
    autoSaveInterval: Int = 5, // 自动存档间隔（分钟）
    lastAutoSaveMinute: Int = 0, // 上次自动存档的分钟数
    onAutoSaveEnabledToggle: (Boolean) -> Unit = {}, // 自动存档开关切换回调
    onAutoSaveIntervalChange: (Int) -> Unit = {}, // 自动存档间隔修改回调
    onMaxEmployees: () -> Unit = {}, // 一键满配员工回调
    onAddMoney: () -> Unit = {}, // 一键加钱回调
    onCreateTopEmployees: () -> Unit = {}, // 创建各职位6名5级专属技能员工回调
    onMoneyUpdate: (Long) -> Unit = {}, // 资金更新回调
    usedRedeemCodes: Set<String> = emptySet(), // 已使用的兑换码列表
    onUsedRedeemCodesUpdate: (Set<String>) -> Unit = {} // 已使用兑换码更新回调
) {
    val context = LocalContext.current
    val saveManager = remember { SaveManager(context) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingSaveSlots by remember { mutableStateOf(false) }
    var saveSlots by remember { mutableStateOf(emptyMap<Int, SaveData?>()) }
    val coroutineScope = rememberCoroutineScope()
    
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
                    modifier = Modifier.fillMaxWidth(),
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
                                text = "开启后每隔${autoSaveInterval}分钟自动保存到存档位1",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Switch(
                        checked = autoSaveEnabled,
                        onCheckedChange = { onAutoSaveEnabledToggle(it) },
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
                                text = "${autoSaveInterval}分钟",
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
        
        // 兑换码区域
        if (!gmModeEnabled) {
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        placeholder = { Text("请输入兑换码", color = Color.White.copy(alpha = 0.4f)) },
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
                        @Suppress("SpellCheckingInspection")
                        Text(
                            text = if (redeemCode.uppercase() == "YCJY2025" && usedRedeemCodes.contains("YCJY2025")) {
                                "❌ 该兑换码已在本存档中使用过，每个存档仅限使用1次"
                            } else {
                                "❌ 兑换码错误，请重新输入"
                            },
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                    }
                    
                    Button(
                        onClick = {
                            val codeUpper = redeemCode.uppercase()
                            @Suppress("SpellCheckingInspection")
                            when {
                                codeUpper == "PROGM" -> {
                                    onGMToggle(true)
                                    redeemCode = ""
                                    redeemSuccessMessage = "GM工具箱已激活！"
                                    showRedeemSuccessDialog = true
                                }
                                codeUpper == "YCJY2025" -> {
                                    // 检查兑换码是否已使用
                                    if (usedRedeemCodes.contains("YCJY2025")) {
                                        showRedeemError = true
                                    } else {
                                        // 兑换码：YCJY2025，获得5M资金
                                        val rewardAmount = 5000000L // 5M = 500万
                                        onMoneyUpdate(money + rewardAmount)
                                        // 标记兑换码为已使用
                                        onUsedRedeemCodesUpdate(usedRedeemCodes + "YCJY2025")
                                        redeemCode = ""
                                        redeemSuccessMessage = "兑换成功！获得 ${formatMoney(rewardAmount)}"
                                        showRedeemSuccessDialog = true
                                    }
                                }
                                else -> {
                                    showRedeemError = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp)
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
        }
        
        // GM工具箱
        if (gmModeEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF6B6B).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🛠️ GM工具箱",
                        color = Color(0xFFFF6B6B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "GM模式已激活",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    
                    // 一键满配员工
                    Button(
                        onClick = onMaxEmployees,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(8.dp)
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
                        shape = RoundedCornerShape(8.dp)
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
                    
                    // 创建各职位6名5级专属技能员工
                    Button(
                        onClick = onCreateTopEmployees,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp)
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
                                currentMinuteOfDay = currentMinuteOfDay,
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
                                unlockedAchievements = unlockedAchievements, // 保存已解锁成就
                                completedTutorials = completedTutorials, // 保存已完成教程
                                skipTutorial = skipTutorial, // 保存跳过教程状态
                                companyReputation = companyReputation, // 保存公司声望
                                gvaHistory = gvaHistory, // 保存GVA历史记录
                                currentYearNominations = currentYearNominations, // 保存当年提名
                                gvaAnnouncedDate = gvaAnnouncedDate, // 保存颁奖日期
                                ownedIPs = ownedIPs, // 保存拥有的IP列表（收购竞争对手后获得）
                                gmModeEnabled = gmModeEnabled, // 保存GM模式状态
                                usedRedeemCodes = usedRedeemCodes, // 保存已使用的兑换码列表
                                autoSaveEnabled = autoSaveEnabled, // 保存自动存档开关
                                autoSaveInterval = autoSaveInterval, // 保存自动存档间隔
                                lastAutoSaveMinute = lastAutoSaveMinute, // 保存上次自动存档时间
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
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
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
                                            unlockedAchievements = unlockedAchievements, // 保存已解锁成就
                                            completedTutorials = completedTutorials, // 保存已完成教程
                                            skipTutorial = skipTutorial, // 保存跳过教程状态
                                            companyReputation = companyReputation, // 保存公司声望
                                            gvaHistory = gvaHistory, // 保存GVA历史记录
                                            currentYearNominations = currentYearNominations, // 保存当年提名
                                            gvaAnnouncedDate = gvaAnnouncedDate, // 保存颁奖日期
                                            ownedIPs = ownedIPs, // 保存拥有的IP列表（收购竞争对手后获得）
                                            gmModeEnabled = gmModeEnabled, // 保存GM模式状态
                                            usedRedeemCodes = usedRedeemCodes, // 保存已使用的兑换码列表
                                            autoSaveEnabled = autoSaveEnabled, // 保存自动存档开关
                                            autoSaveInterval = autoSaveInterval, // 保存自动存档间隔
                                            lastAutoSaveMinute = lastAutoSaveMinute, // 保存上次自动存档时间
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
                                Text(
                                    text = "存档位 $slotNumber",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                
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
                                        text = "空存档",
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
    onPauseToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        // 下拉按钮 - 现代化设计
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .height(32.dp)
                .widthIn(min = 80.dp, max = 120.dp)
                .wrapContentWidth()
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
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
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
                overflow = TextOverflow.Ellipsis
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
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${speed}x",
                            color = if (currentSpeed == speed && !isPaused) Color(0xFF10B981) else Color(0xFFE5E7EB),
                            fontSize = 14.sp,
                            fontWeight = if (currentSpeed == speed && !isPaused) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    onClick = {
                        // 如果当前是暂停状态，先取消暂停
                        if (isPaused) {
                            onPauseToggle()
                        }
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
 * FPS监测组件
 * 显示当前帧率，用于性能监控
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
        
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        Choreographer.getInstance().postFrameCallback(frameCallback)
        
        // 每秒计算一次FPS
        val updateJob = coroutineScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastTime
                if (elapsed > 0) {
                    val calculatedFps = ((frameCount * 1000L) / elapsed).toInt().coerceIn(0, 144)
                    fps = calculatedFps
                    frameCount = 0
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
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "FPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "$fps",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    fps >= 110 -> Color(0xFF10B981) // 绿色：性能优秀（接近120fps）
                    fps >= 55 -> Color(0xFF3B82F6) // 蓝色：性能良好（60fps左右）
                    fps >= 30 -> Color(0xFFF59E0B) // 黄色：性能一般
                    else -> Color(0xFFEF4444) // 红色：性能较差
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YjcyTheme {
        // MainMenuScreen() - 需要NavController参数
    }
}