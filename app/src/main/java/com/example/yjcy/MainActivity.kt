package com.example.yjcy

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.yjcy.ui.BadgeBox
import com.example.yjcy.ui.EmployeeManagementContent
import com.example.yjcy.ui.GameRatingDialog
import com.example.yjcy.ui.GameReleaseDialog
import com.example.yjcy.ui.ProjectManagementWrapper
import com.example.yjcy.ui.ProjectDisplayType
import com.example.yjcy.ui.ServerManagementContent
import com.example.yjcy.ui.theme.YjcyTheme
import com.example.yjcy.utils.formatMoney
import com.example.yjcy.utils.formatMoneyWithDecimals
import com.example.yjcy.service.JobPostingService
import com.example.yjcy.service.CustomerServiceManager
import com.example.yjcy.data.getUpdateContentName
import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.data.CompetitorCompany
import com.example.yjcy.data.CompetitorNews
import com.example.yjcy.data.CompetitorManager
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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.math.sin
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
        com.example.yjcy.utils.SignatureHelper.logAppInfo(this)
        
        // 初始化RevenueManager以支持数据持久化
        RevenueManager.initialize(this)
        
        // 启动时检查并修复服务器扣费（针对旧存档的一次性迁移）
        Log.d("MainActivity", "检查服务器扣费状态...")
        
        // 先启用边到边显示
        enableEdgeToEdge()
        
        // 然后设置全屏显示和隐藏系统导航栏
        enableFullScreenDisplay()
        
        // 检查用户是否已同意隐私政策
        val sharedPreferences = getSharedPreferences("privacy_settings", MODE_PRIVATE)
        val hasAgreedPrivacy = sharedPreferences.getBoolean("privacy_agreed", false)
        
        // 如果用户已同意隐私政策，则初始化SDK并检查更新
        if (hasAgreedPrivacy) {
            (application as? YjcyApplication)?.initTapSDKIfNeeded()
            
            // 延迟500ms后检查更新，确保SDK完全初始化
            android.os.Handler(mainLooper).postDelayed({
                Log.d("MainActivity", "开始检查TapTap更新...")
                com.example.yjcy.taptap.TapUpdateManager.checkForceUpdate()
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
                        var allUnlockedAchievements by remember { mutableStateOf<List<UnlockedAchievement>>(emptyList()) }
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
    
    var showMessage by remember { mutableStateOf<String?>(null) }
    
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
    
    // 退出应用确认对话框状态
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Logo动画
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    // 拦截返回键，显示退出应用确认对话框
    BackHandler {
        showExitDialog = true
    }
    
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
        
        // 左上角版本号
        Text(
            text = "V${BuildConfig.VERSION_NAME}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo展示区域
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(logoScale)
            ) {
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
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 主要功能按钮组
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GameMenuButton(
                    text = "🚀 开始新游戏",
                    onClick = { navController.navigate("game_setup") }
                )
                
                GameMenuButton(
                    text = "📂 继续游戏",
                    onClick = { navController.navigate("continue") }
                )
                
                GameMenuButton(
                    text = "🏆 成就",
                    onClick = { navController.navigate("achievement") }
                )
                
                GameMenuButton(
                    text = "⚙️ 设置",
                    onClick = { navController.navigate("settings") }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 健康游戏忠告
            Card(
                modifier = Modifier
                    .width(320.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "抵制不良游戏，拒绝盗版游戏。\n注意自我保护，谨防受骗上当。\n适度游戏益脑，沉迷游戏伤身。\n合理安排时间，享受健康生活。",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
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
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "确定要退出游戏吗？",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            activity?.finish()
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
    }
}

@Composable
fun ParticleBackground() {
    val particles = remember {
        List(20) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.02f + 0.01f,
                alpha = Random.nextFloat() * 0.6f + 0.2f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000)
        ),
        label = "particle_animation"
    )
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        particles.forEach { particle ->
            val currentY = (particle.y + animationProgress * particle.speed) % 1f
            val currentX = particle.x + sin(animationProgress * 2 * Math.PI.toFloat() + particle.y * 10) * 0.1f
            
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
    var selectedProfession by remember { mutableStateOf<FounderProfession?>(null) }
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
                            com.example.yjcy.utils.SensitiveWordFilter.containsSensitiveCompanyName(newValue) -> {
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
    var money by remember { mutableLongStateOf(saveData?.money ?: 1000000L) }
    var fans by remember { mutableIntStateOf(saveData?.fans ?: 0) }
    var currentYear by remember { mutableIntStateOf(saveData?.currentYear ?: 1) }
    var currentMonth by remember { mutableIntStateOf(saveData?.currentMonth ?: 1) }
    var currentDay by remember { mutableIntStateOf(saveData?.currentDay ?: 1) }
    var gameSpeed by remember { mutableIntStateOf(1) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    
    // 上次月结算的年月（防止重复结算）
    var lastSettlementYear by remember { mutableIntStateOf(saveData?.currentYear ?: 1) }
    var lastSettlementMonth by remember { mutableIntStateOf(saveData?.currentMonth ?: 1) }
    
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
    var pendingReleaseGame by remember { mutableStateOf<Game?>(null) }
    var revenueRefreshTrigger by remember { mutableIntStateOf(0) } // 用于触发收益数据刷新
    var jobPostingRefreshTrigger by remember { mutableIntStateOf(0) } // 用于触发岗位应聘者数据刷新
    var pendingRatingGame by remember { mutableStateOf<Game?>(null) }
    
    // 废弃游戏相关状态
    var showAbandonDialog by remember { mutableStateOf(false) }
    var pendingAbandonGame by remember { mutableStateOf<Game?>(null) }
    
    // 退出游戏确认对话框状态
    var showExitDialog by remember { mutableStateOf(false) }
    
    // 破产对话框状态
    var showBankruptcyDialog by remember { mutableStateOf(false) }
    
    // 显示设置界面状态
    var showSettings by remember { mutableStateOf(false) }
    
    // 秘书聊天对话框状态
    var showSecretaryChat by remember { mutableStateOf(false) }
    
    // 员工状态管理 - 提升到GameScreen级别
    val allEmployees = remember { mutableStateListOf<Employee>() }
    
    // 竞争对手数据状态
    var competitors by remember { mutableStateOf(saveData?.competitors ?: emptyList()) }
    var competitorNews by remember { mutableStateOf(saveData?.competitorNews ?: emptyList()) }
    
    // 客诉数据状态
    var complaints by remember { mutableStateOf(saveData?.complaints ?: emptyList()) }
    var autoProcessComplaints by remember { mutableStateOf(saveData?.autoProcessComplaints ?: false) }
    
    // 成就系统状态
    var unlockedAchievements by remember { mutableStateOf(saveData?.unlockedAchievements ?: emptyList()) }
    var pendingAchievementsToShow by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var hasCheckedInitialAchievements by remember { mutableStateOf(false) }
    
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
                    } else {
                        // 收益数据存在，更新游戏信息（商业模式和付费内容）
                        RevenueManager.updateGameInfo(
                            releasedGame.id,
                            releasedGame.businessModel,
                            releasedGame.monetizationItems
                        )
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
        if (saveData == null && competitors.isEmpty()) {
            // 生成初始竞争对手
            competitors = CompetitorManager.generateInitialCompetitors(
                companyName, 
                currentYear, 
                currentMonth
            )
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
                1 -> 5000L // 慢速：5秒一天（2.5分钟/月，30分钟/年）- 最舒适的节奏
                2 -> 2500L // 中速：2.5秒一天（1.25分钟/月，15分钟/年）- 平衡速度
                3 -> 1000L // 快速：1秒一天（30秒/月，6分钟/年）- 快速推进
                else -> 2500L
            })
            
            // 更新日期
            currentDay++
            if (currentDay > 30) {
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
            
            if (currentDay == 1) {
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
                            avgRating >= 8.0f -> (fans * 0.025).toInt() // 2.5%增长（高评分）（原5%）
                            avgRating >= 6.0f -> (fans * 0.015).toInt() // 1.5%增长（中等评分）（原3%）
                            else -> (fans * 0.005).toInt() // 0.5%增长（低评分）（原1%）
                        }
                        
                        val totalFansGrowth = (baseFansGrowth * gameCountMultiplier).toInt().coerceAtLeast(100)
                        fans = (fans + totalFansGrowth).coerceAtLeast(0)
                        
                        Log.d("MainActivity", "月结算粉丝增长: +$totalFansGrowth (游戏数:${releasedGames.size}, 平均评分:$avgRating, 当前粉丝:$fans)")
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
                
                // 检查是否破产（负债达到50万）
                if (money <= -500000L) {
                    isPaused = true
                    showBankruptcyDialog = true
                    Log.d("MainActivity", "公司破产：当前资金 ¥$money")
                }
            }
            
            // 更新游戏开发进度（分阶段系统）
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
                            
                            // 触发发售价格设置对话框
                            pendingReleaseGame = completedGame
                            showReleaseDialog = true
                            
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
                        
                        game.copy(
                            phaseProgress = newPhaseProgress,
                            developmentProgress = newTotalProgress,
                            isCompleted = false
                        )
                    }
                } else {
                    game
                }
            }
            
            // 为已发售的游戏添加每日收益，并推进更新任务
            games.filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
                .forEach { releasedGame ->
                    // 更新游戏信息（商业模式和付费内容）
                    RevenueManager.updateGameInfo(
                        releasedGame.id,
                        releasedGame.businessModel,
                        releasedGame.monetizationItems
                    )
                    
                    // 传入游戏评分、粉丝数和当前日期，影响网络游戏的注册数和兴趣值衰减
                    val gameRating = releasedGame.gameRating?.finalScore
                    val dailyRevenue = RevenueManager.addDailyRevenueForGame(
                        gameId = releasedGame.id, 
                        gameRating = gameRating, 
                        fanCount = fans,
                        currentYear = currentYear,
                        currentMonth = currentMonth,
                        currentDay = currentDay
                    )
                    money += dailyRevenue.toLong()

                    // 在推进进度前先获取更新任务信息（因为完成后会被清除）
                    val completedTask = RevenueManager.getGameRevenue(releasedGame.id)?.updateTask
                    
                    // 若存在更新任务，根据已分配员工数量推进进度
                    val employeePoints = (releasedGame.assignedEmployees.size * 20).coerceAtLeast(10)
                    val updateJustCompleted = RevenueManager.progressUpdateTask(releasedGame.id, employeePoints)
                    
                    // 如果更新刚刚完成，版本号+0.1
                    if (updateJustCompleted) {
                        // 使用之前保存的任务信息
                        
                        // 创建游戏更新记录
                        val newUpdateHistory = if (completedTask != null) {
                            val updateNumber = releasedGame.updateHistory.size + 1
                            val updateDate = com.example.yjcy.data.GameDate(currentYear, currentMonth, currentDay)
                            
                            // 生成玩家评论
                            val comments = com.example.yjcy.utils.CommentGenerator.generateComments(
                                updateContent = completedTask.features,
                                commentCount = kotlin.random.Random.nextInt(5, 11)
                            )
                            
                            // 创建更新记录
                            val gameUpdate = com.example.yjcy.data.GameUpdate(
                                updateNumber = updateNumber,
                                updateDate = updateDate,
                                updateContent = completedTask.features,
                                announcement = completedTask.announcement,
                                comments = comments
                            )
                            
                            releasedGame.updateHistory + gameUpdate
                        } else {
                            releasedGame.updateHistory
                        }
                        
                        val updatedGame = releasedGame.copy(
                            version = releasedGame.version + 0.1f,
                            assignedEmployees = emptyList(), // 清空分配的员工
                            updateHistory = newUpdateHistory // 添加更新记录
                        )
                        games = games.map { if (it.id == updatedGame.id) updatedGame else it }
                        
                        // 如果开启了自动更新，自动创建下一次更新任务
                        if (releasedGame.autoUpdate) {
                            println("【自动更新】游戏《${releasedGame.name}》的更新已自动发布！版本升级至 V${String.format(Locale.getDefault(), "%.1f", updatedGame.version)}")
                            
                            // 根据游戏类型生成更新选项
                            val autoUpdateFeatures = if (releasedGame.businessModel == BusinessModel.ONLINE_GAME) {
                                // 网络游戏：使用已启用的付费内容
                                releasedGame.monetizationItems
                                    .filter { it.isEnabled }
                                    .map { it.type.getUpdateContentName() }
                                    .distinct()
                            } else {
                                // 单机游戏：根据游戏主题获取推荐的付费内容类型作为更新内容
                                val recommendedItems = com.example.yjcy.data.MonetizationConfig.getRecommendedItems(releasedGame.theme)
                                recommendedItems.map { it.getUpdateContentName() }
                            }
                            
                            // 如果有可用的更新内容，自动创建新的更新任务
                            if (autoUpdateFeatures.isNotEmpty()) {
                                // 自动更新使用默认公告
                                val autoAnnouncement = com.example.yjcy.utils.CommentGenerator.generateDefaultAnnouncement(autoUpdateFeatures)
                                RevenueManager.createUpdateTask(releasedGame.id, autoUpdateFeatures, autoAnnouncement)
                                println("【自动更新】已自动创建下一次更新任务，共${autoUpdateFeatures.size}项内容")
                            }
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
            val (updatedComplaints, completedComplaints) = CustomerServiceManager.processDailyComplaints(
                complaints,
                allEmployees
            )
            complaints = updatedComplaints
            
            // 计算超时客诉造成的粉丝损失
            val fanLoss: Int = CustomerServiceManager.calculateOverdueFanLoss(
                complaints,
                currentYear,
                currentMonth,
                currentDay
            )
            if (fanLoss > 0) {
                fans = (fans - fanLoss).coerceAtLeast(0)
                Log.d("MainActivity", "客诉超时：粉丝流失 -$fanLoss，当前粉丝: $fans")
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
                onSettingsClick = { showSettings = true }
            )
            
            // 主要内容区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
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
                                allEmployees.clear()
                                allEmployees.addAll(updatedEmployees)
                            },
                            money = money,
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney },
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            jobPostingRefreshTrigger = jobPostingRefreshTrigger
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
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay
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
                                revenueData = RevenueManager.exportRevenueData()
                            ),
                            onAcquisitionSuccess = { acquiredCompany, finalPrice, marketValueGain, fansGain, inheritedGames ->
                                // 扣除收购费用
                                money -= finalPrice
                                
                                // 增加粉丝
                                fans += fansGain
                                
                                // 移除被收购的公司
                                competitors = competitors.filter { it.id != acquiredCompany.id }
                                
                                // 继承游戏（转换为玩家的游戏）
                                val inheritedPlayerGames = inheritedGames.map { competitorGame ->
                                    Game(
                                        id = "inherited_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt()}",
                                        name = competitorGame.name,
                                        theme = competitorGame.theme,
                                        platforms = competitorGame.platforms,
                                        businessModel = competitorGame.businessModel,
                                        assignedEmployees = emptyList(),
                                        releaseStatus = GameReleaseStatus.RELEASED,
                                        developmentProgress = 100f,
                                        isCompleted = true,
                                        currentPhase = DevelopmentPhase.PROGRAMMING,
                                        phaseProgress = 100f,
                                        rating = competitorGame.rating,
                                        releasePrice = 50f,
                                        promotionIndex = 0f,
                                        version = 1.0f,
                                        monetizationItems = emptyList()
                                    )
                                }
                                games = games + inheritedPlayerGames
                                
                                // 为继承的网游初始化收益数据
                                // 注意：RevenueManager会在首次调用addDailyRevenueForGame时自动创建GameRevenue
                                // 这里只需要将继承的游戏添加到列表中即可
                                
                                // 生成收购新闻
                                competitorNews = (listOf(
                                    com.example.yjcy.data.CompetitorNews(
                                        id = "news_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt()}",
                                        title = "${companyName}成功收购${acquiredCompany.name}！",
                                        content = "${companyName}以${com.example.yjcy.utils.formatMoney(finalPrice)}的价格成功收购了${acquiredCompany.name}，" +
                                                "继承了${inheritedGames.size}款热门游戏，市值大幅增长。这是游戏行业的重大并购事件。",
                                        type = com.example.yjcy.data.NewsType.COMPANY_MILESTONE,
                                        companyId = -1,
                                        companyName = companyName,
                                        year = currentYear,
                                        month = currentMonth,
                                        day = currentDay
                                    )
                                ) + competitorNews).take(30)
                            }
                        )
                        4 -> ServerManagementContent(
                            games = games,
                            money = money,
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
                            },
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney }
                        )
                    }
            }
            
            // 底部导航栏 - 使用优化版本（字体加粗+黑色）
            EnhancedBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                pendingApplicantsCount = pendingApplicantsCount,
                pendingAssignmentCount = pendingAssignmentCount
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
                                releasePrice = price
                            )
                            
                            // 为已发售游戏初始化收益数据（空数据，等待日常循环累加）
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
                            // 初始化游戏信息（商业模式和付费内容）
                            RevenueManager.updateGameInfo(
                                releasedGame.id,
                                releasedGame.businessModel,
                                releasedGame.monetizationItems
                            )
                            
                            releasedGame
                        } else {
                            existingGame
                        }
                    }
                    
                    // 关闭发售对话框，显示评分对话框
                    showReleaseDialog = false
                    pendingRatingGame = pendingReleaseGame
                    pendingReleaseGame = null
                    showRatingDialog = true
                }
            )
        }
        
        // 游戏评分展示对话框
        if (showRatingDialog && pendingRatingGame != null) {
            GameRatingDialog(
                gameRating = pendingRatingGame!!.gameRating!!,
                gameName = pendingRatingGame!!.name,
                onDismiss = {
                    // 评分对话框关闭时，更新游戏状态为已评分
                    games = games.map { existingGame ->
                        if (existingGame.id == pendingRatingGame!!.id) {
                            val ratedGame = existingGame.copy(
                                releaseStatus = GameReleaseStatus.RATED
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
                            (8000..20000).random()
                        }
                        finalRating >= 8.0f -> {
                            // 评分>=8：优秀作品（3000-10000）
                            (3000..10000).random()
                        }
                        finalRating >= 6.5f -> {
                            // 评分>=6.5：中等偏上（1000-4000）
                            (1000..4000).random()
                        }
                        finalRating >= 5.0f -> {
                            // 评分>=5：及格水平（500-2000）
                            (500..2000).random()
                        }
                        else -> {
                            // 评分<5：口碑崩塌（-3000到-1000）
                            (-3000..-1000).random()
                        }
                    }
                    fans = (fans + fansChange).coerceAtLeast(0) // 粉丝数不能为负
                    
                    Log.d("MainActivity", "游戏发布-评分: $finalRating, 粉丝变化: $fansChange, 当前粉丝: $fans")
                    
                    // 自动切换到"已发售"界面，方便玩家查看新发售的游戏
                    selectedProjectType = ProjectDisplayType.RELEASED
                    
                    showRatingDialog = false
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
                            unlockedAchievements = unlockedAchievements
                        )
                    }
                }
            }
        }
        
        // 秘书聊天对话框
        if (showSecretaryChat) {
            SecretaryChatDialog(
                onDismiss = { showSecretaryChat = false }
            )
        }
        
        // 成就解锁弹窗
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
    fans: Int,
    year: Int,
    month: Int,
    day: Int,
    gameSpeed: Int,
    onSpeedChange: (Int) -> Unit,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    companyName: String = "我的游戏公司",
    selectedLogo: String = "🎮",
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
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 粉丝
                Row(
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
                        fontWeight = FontWeight.Bold
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 日期
                    Text(
                        text = "第${year}年${month}月${day}日",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
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
    fans: Int = 0,
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
            
            val releasedGames = games.filter { 
                it.releaseStatus == GameReleaseStatus.RELEASED || 
                it.releaseStatus == GameReleaseStatus.RATED 
            }
            
            // 根据选择的年份筛选收入数据
            // 计算单机游戏该年收入
            var singlePlayerRevenue = 0.0
            releasedGames.filter { it.businessModel == BusinessModel.SINGLE_PLAYER }.forEach { game ->
                val revenue = RevenueManager.getGameRevenue(game.id)
                if (revenue != null && revenue.dailySalesList.isNotEmpty()) {
                    // 筛选该年份的数据（基于索引：前365天是第1年，365-730天是第2年，以此类推）
                    val startIndex = (selectedFinancialYear - 1) * 365
                    val endIndex = selectedFinancialYear * 365
                    val yearRevenue = revenue.dailySalesList
                        .filterIndexed { index, _ -> index in startIndex until endIndex }
                        .sumOf { it.revenue }
                    singlePlayerRevenue += yearRevenue
                }
            }
            
            // 计算网游该年收入
            var onlineGameRevenue = 0.0
            releasedGames.filter { it.businessModel == BusinessModel.ONLINE_GAME }.forEach { game ->
                val revenue = RevenueManager.getGameRevenue(game.id)
                if (revenue != null && revenue.dailySalesList.isNotEmpty()) {
                    // 筛选该年份的数据（基于索引：前365天是第1年，365-730天是第2年，以此类推）
                    val startIndex = (selectedFinancialYear - 1) * 365
                    val endIndex = selectedFinancialYear * 365
                    val yearRevenue = revenue.dailySalesList
                        .filterIndexed { index, _ -> index in startIndex until endIndex }
                        .sumOf { it.revenue }
                    onlineGameRevenue += yearRevenue
                }
            }
            
            // 计算该年总收入
            val yearTotalRevenue = singlePlayerRevenue + onlineGameRevenue
            
            CompanyInfoCardWithYearSelector(
                title = "财务状况",
                currentYear = currentYear,
                selectedYear = selectedFinancialYear,
                onYearChange = { selectedFinancialYear = it },
                items = listOf(
                    "单机收入" to "¥${formatMoneyWithDecimals(singlePlayerRevenue)}",
                    "网游收入" to "¥${formatMoneyWithDecimals(onlineGameRevenue)}",
                    "总收入" to "¥${formatMoneyWithDecimals(yearTotalRevenue)}"
                )
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
    items: List<Pair<String, String>>
) {
    var expanded by remember { mutableStateOf(false) }
    
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
            // 标题行（包含年份选择器）
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
                
                // 年份选择下拉框
                Box {
                    OutlinedButton(
                        onClick = { expanded = !expanded },
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
                            text = if (expanded) "▲" else "▼",
                            fontSize = 10.sp
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color(0xFF1F2937))
                            .widthIn(min = 120.dp)
                    ) {
                        // 从第1年到当前年份
                        (1..currentYear).reversed().forEach { year ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "第${year}年",
                                        color = if (year == selectedYear) Color(0xFFF59E0B) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onYearChange(year)
                                    expanded = false
                                },
                                modifier = Modifier.background(
                                    if (year == selectedYear) Color.White.copy(alpha = 0.1f) else Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
            
            // 财务数据列表
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

// 优化版本的底部导航栏组件 - 字体加粗+黑色
@Composable
fun EnhancedBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    pendingApplicantsCount: Int = 0, // 待处理应聘者数量
    pendingAssignmentCount: Int = 0 // 待分配项目数量
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
                icon = "🖥️",
                label = "服务器",
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) }
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
    var saves by remember { mutableStateOf<Map<Int, SaveData?>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var saveToDelete by remember { mutableStateOf<Pair<Int, SaveData?>?>(null) }
    var showVersionWarningDialog by remember { mutableStateOf(false) }
    var saveToLoad by remember { mutableStateOf<Pair<Int, SaveData>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // 当前游戏版本（自动从BuildConfig读取）
    val currentVersion = BuildConfig.VERSION_NAME
    
    // 异步加载存档
    LaunchedEffect(Unit) {
        isLoading = true
        saves = saveManager.getAllSavesAsync()
        isLoading = false
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
                            // 检查存档版本号
                            val saveVersion = saveData.version
                            if (compareVersion(saveVersion, currentVersion) < 0) {
                                // 存档版本低于当前版本，显示警告对话框
                                saveToLoad = Pair(slotIndex, saveData)
                                showVersionWarningDialog = true
                            } else {
                                // 版本号正常，直接加载
                                currentLoadedSaveData = saveData
                                Toast.makeText(context, "加载存档 $slotIndex", Toast.LENGTH_SHORT).show()
                                navController.navigate("game/${saveData.companyName}/${saveData.founderName}/${saveData.companyLogo}/${saveData.founderProfession?.name ?: "PROGRAMMER"}")
                            }
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
        
        // 版本号警告对话框
        if (showVersionWarningDialog && saveToLoad != null) {
            VersionWarningDialog(
                slotIndex = saveToLoad!!.first,
                saveData = saveToLoad!!.second,
                currentVersion = currentVersion,
                onConfirm = {
                    // 用户确认后继续加载
                    currentLoadedSaveData = saveToLoad!!.second
                    Toast.makeText(context, "加载存档 ${saveToLoad!!.first}", Toast.LENGTH_SHORT).show()
                    navController.navigate("game/${saveToLoad!!.second.companyName}/${saveToLoad!!.second.founderName}/${saveToLoad!!.second.companyLogo}/${saveToLoad!!.second.founderProfession?.name ?: "PROGRAMMER"}")
                    showVersionWarningDialog = false
                    saveToLoad = null
                },
                onDismiss = {
                    showVersionWarningDialog = false
                    saveToLoad = null
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
    progress: Float
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
    val context = LocalContext.current
    
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

/**
 * 比较两个版本号
 * @return 如果version1 < version2返回负数，相等返回0，大于返回正数
 */
fun compareVersion(version1: String, version2: String): Int {
    val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
    val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
    
    val maxLength = maxOf(v1Parts.size, v2Parts.size)
    for (i in 0 until maxLength) {
        val v1 = v1Parts.getOrNull(i) ?: 0
        val v2 = v2Parts.getOrNull(i) ?: 0
        if (v1 != v2) {
            return v1 - v2
        }
    }
    return 0
}

@Composable
fun VersionWarningDialog(
    slotIndex: Int,
    saveData: SaveData,
    currentVersion: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 24.sp
                )
                Text(
                    text = "存档版本警告",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "存档版本: ${saveData.version}",
                            color = Color(0xFFFBBF24),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "当前版本: $currentVersion",
                            color = Color(0xFF10B981),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Text(
                    text = "当前存档版本低于最新版，可能会出现以下问题：",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "•",
                            color = Color(0xFFFBBF24),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "旧存档游戏数据可能受到影响",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "•",
                            color = Color(0xFFFBBF24),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "无法体验新版本的玩法或功能",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "建议开启新档游玩以获得最佳体验",
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Card(
                modifier = Modifier.clickable { onConfirm() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFBBF24).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "仍然加载",
                    color = Color(0xFFFBBF24),
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
                ),
                shape = RoundedCornerShape(8.dp)
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

// 存档管理类（异步版本，支持数据清理和压缩）
class SaveManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_saves", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .serializeNulls() // 确保null值也被序列化，去除PrettyPrinting以减小体积
        .create()
    
    companion object {
        private const val MAX_DAILY_SALES_DAYS = 365 // 每个游戏最多保留365天的每日数据
        private const val MAX_COMPETITOR_NEWS = 50 // 最多保留50条竞争对手新闻
        private const val MAX_JSON_SIZE_MB = 5 // 警告阈值：5MB
    }
    
    /**
     * 清理存档数据，移除过旧的历史数据以减小体积
     */
    private fun cleanSaveData(saveData: SaveData): SaveData {
        // 1. 清理收益数据：每个游戏只保留最近365天的每日销售数据
        val cleanedRevenueData = saveData.revenueData.mapValues { (gameId, revenue) ->
            if (revenue.dailySalesList.size > MAX_DAILY_SALES_DAYS) {
                val totalSales = revenue.dailySalesList.sumOf { it.sales }
                val totalRevenue = revenue.dailySalesList.sumOf { it.revenue }
                val recentDailySales = revenue.dailySalesList.takeLast(MAX_DAILY_SALES_DAYS)
                
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
            
            Log.d("SaveManager", "JSON大小: ${String.format("%.2f", jsonSizeKB)} KB (${String.format("%.2f", jsonSizeMB)} MB)")
            
            // 3. GZIP压缩
            val compressed = compressString(json)
            val compressedSizeKB = compressed.size / 1024.0
            val compressionRatio = (1 - compressedSizeKB / jsonSizeKB) * 100
            
            Log.d("SaveManager", "压缩后大小: ${String.format("%.2f", compressedSizeKB)} KB, 压缩率: ${String.format("%.1f", compressionRatio)}%")
            
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
                    val duration = System.currentTimeMillis() - startTime
                    Log.d("SaveManager", "从存档位 $slotIndex 加载游戏完成（${if (isCompressed) "压缩" else "未压缩"}），耗时: ${duration}ms, 游戏数量: ${loadedData.games.size}")
                    loadedData
                } catch (e: Exception) {
                    Log.e("SaveManager", "解析存档失败", e)
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
    fans: Int = 0,
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
    unlockedAchievements: List<UnlockedAchievement> = emptyList()
) {
    val context = LocalContext.current
    val saveManager = remember { SaveManager(context) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var shouldReturnToMenuAfterSave by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingSaveSlots by remember { mutableStateOf(false) }
    var saveSlots by remember { mutableStateOf<Map<Int, SaveData?>>(emptyMap()) }
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
                // 异步加载存档列表
                coroutineScope.launch {
                    saveSlots = saveManager.getAllSavesAsync()
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
    }
    
    // 保存游戏对话框
    if (showSaveDialog) {
        var showOverwriteConfirmDialog by remember { mutableStateOf(false) }
        var selectedSlotNumber by remember { mutableIntStateOf(0) }
        var selectedExistingSave by remember { mutableStateOf<SaveData?>(null) }
        
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
                                unlockedAchievements = unlockedAchievements, // 保存已解锁成就
                                saveTime = System.currentTimeMillis(),
                                version = BuildConfig.VERSION_NAME // 使用当前游戏版本号
                            )
                            val slotToSave = selectedSlotNumber
                            isSaving = true
                            coroutineScope.launch {
                                val result = saveManager.saveGameAsync(slotToSave, saveData)
                                withContext(Dispatchers.Main) {
                                    isSaving = false
                                    if (result.success) {
                                        val compressionRatio = if (result.originalSizeKB > 0) {
                                            (1 - result.compressedSizeKB / result.originalSizeKB) * 100
                                        } else 0.0
                                        val message = "游戏已保存！\n压缩前: ${String.format("%.1f", result.originalSizeKB)} KB\n压缩后: ${String.format("%.1f", result.compressedSizeKB)} KB\n压缩率: ${String.format("%.1f", compressionRatio)}%"
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        showSaveDialog = false
                                        showOverwriteConfirmDialog = false
                                        // 如果需要在保存后返回主菜单
                                        if (shouldReturnToMenuAfterSave) {
                                            shouldReturnToMenuAfterSave = false
                                            navController.navigate("main_menu")
                                        }
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
                                            unlockedAchievements = unlockedAchievements, // 保存已解锁成就
                                            saveTime = System.currentTimeMillis(),
                                            version = BuildConfig.VERSION_NAME // 使用当前游戏版本号
                                        )
                                        isSaving = true
                                        coroutineScope.launch {
                                            val result = saveManager.saveGameAsync(slotNumber, saveData)
                                            withContext(Dispatchers.Main) {
                                                isSaving = false
                                                if (result.success) {
                                                    val compressionRatio = if (result.originalSizeKB > 0) {
                                                        (1 - result.compressedSizeKB / result.originalSizeKB) * 100
                                                    } else 0.0
                                                    val message = "游戏已保存！\n压缩前: ${String.format("%.1f", result.originalSizeKB)} KB\n压缩后: ${String.format("%.1f", result.compressedSizeKB)} KB\n压缩率: ${String.format("%.1f", compressionRatio)}%"
                                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                    showSaveDialog = false
                                                    // 如果需要在保存后返回主菜单
                                                    if (shouldReturnToMenuAfterSave) {
                                                        shouldReturnToMenuAfterSave = false
                                                        navController.navigate("main_menu")
                                                    }
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
                    text = "当前游戏进度尚未保存，是否要先保存游戏再返回主菜单？",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Row {
                    // 保存并返回按钮
                    TextButton(
                        onClick = {
                            showExitConfirmDialog = false
                            shouldReturnToMenuAfterSave = true
                            showSaveDialog = true
                        }
                    ) {
                        Text(
                            text = "保存并返回",
                            color = Color(0xFF10B981)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 直接返回按钮
                    TextButton(
                        onClick = {
                            showExitConfirmDialog = false
                            navController.navigate("main_menu")
                        }
                    ) {
                        Text(
                            text = "直接返回",
                            color = Color(0xFFEF4444)
                        )
                    }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YjcyTheme {
        // MainMenuScreen() - 需要NavController参数
    }
}