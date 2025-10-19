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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.yjcy.data.RevenueManager
import com.example.yjcy.data.SaveData
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
import com.example.yjcy.data.getUpdateContentName
import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.data.CompetitorCompany
import com.example.yjcy.data.CompetitorNews
import com.example.yjcy.data.CompetitorManager
import com.example.yjcy.ui.CompetitorContent
import com.example.yjcy.ui.calculatePlayerMarketValue
import com.google.gson.Gson
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
        
        // 先启用边到边显示
        enableEdgeToEdge()
        
        // 然后设置全屏显示和隐藏系统导航栏
        enableFullScreenDisplay()
        
        // 检查用户是否已同意隐私政策
        val sharedPreferences = getSharedPreferences("privacy_settings", MODE_PRIVATE)
        val hasAgreedPrivacy = sharedPreferences.getBoolean("privacy_agreed", false)
        
        // 如果用户已同意隐私政策，则初始化SDK
        if (hasAgreedPrivacy) {
            (application as? YjcyApplication)?.initTapSDKIfNeeded()
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
                        GameScreen(navController, companyName, founderName, selectedLogo, founderProfession, currentLoadedSaveData)
                        // 清除存档数据，避免影响下次新游戏
                        currentLoadedSaveData = null
                    }
                    composable("continue") {
                        ContinueScreen(navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController)
                    }
                    composable("leaderboard") {
                    }
                    composable("in_game_settings") {
                        InGameSettingsScreen(navController)
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )
    
    // 橙色渐变背景
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFF59E0B), // 橙色
            Color(0xFFEA580C)  // 深橙色
        )
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .width(280.dp)
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
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
                        if (newValue.length <= 5 && newValue.all { it.isLetterOrDigit() }) {
                            companyName = newValue
                            isCompanyNameValid = true
                        } else {
                            isCompanyNameValid = false
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
                if (!isCompanyNameValid) {
                    Text(
                        text = "只能输入最多5个字符和数字",
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
    
    // 项目管理界面的显示类型状态（使用 remember 保持在内存中）
    var selectedProjectType by remember { mutableStateOf(ProjectDisplayType.DEVELOPING) }
    var companyName by remember { mutableStateOf(saveData?.companyName ?: initialCompanyName) }
    var founderName by remember { mutableStateOf(saveData?.founderName ?: initialFounderName) }
    var founderProfession by remember { mutableStateOf(saveData?.founderProfession ?: try { FounderProfession.valueOf(initialFounderProfession) } catch (_: IllegalArgumentException) { FounderProfession.PROGRAMMER }) }
    var games by remember { mutableStateOf(saveData?.games ?: emptyList()) }
    
    // 消息状态
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    
    // 如果是新游戏，清除RevenueManager的旧数据
    LaunchedEffect(saveData) {
        if (saveData == null) {
            RevenueManager.clearAllData()
        }
    }
    
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
    
    // 员工状态管理 - 提升到GameScreen级别
    val allEmployees = remember { mutableStateListOf<Employee>() }
    
    // 竞争对手数据状态
    var competitors by remember { mutableStateOf(saveData?.competitors ?: emptyList()) }
    var competitorNews by remember { mutableStateOf(saveData?.competitorNews ?: emptyList()) }
    
    // 获取待处理的应聘者数量
    val jobPostingService = remember { JobPostingService.getInstance() }
    var pendingApplicantsCount by remember { mutableIntStateOf(0) }
    
    // 监听岗位变化，更新待处理应聘者数量
    LaunchedEffect(jobPostingRefreshTrigger) {
        pendingApplicantsCount = jobPostingService.getTotalPendingApplicants()
    }
    
    // 创建创始人对象
    val founder = remember(founderName, founderProfession) {
        Founder(name = founderName, profession = founderProfession)
    }

    // 如果是从存档进入，初始化已发售游戏的收益数据（避免收益概览为空、按钮无响应）
    LaunchedEffect(saveData) {
        if (saveData != null) {
            saveData.games
                .filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
                .forEach { releasedGame ->
                    val exists = RevenueManager.getGameRevenue(releasedGame.id)
                    if (exists == null) {
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
                    }
                }
            // 触发一次UI刷新以显示已初始化的收益
            revenueRefreshTrigger++
        }
    }
    
    // 初始化员工列表 - 从存档加载或创建创始人员工
    LaunchedEffect(founder, saveData) {
        if (allEmployees.isEmpty()) {
            if (saveData != null && saveData.allEmployees.isNotEmpty()) {
                // 从存档加载员工数据
                allEmployees.addAll(saveData.allEmployees)
            } else {
                // 新游戏：将创始人转换为员工
                val founderAsEmployee = Employee(
                    id = 0,
                    name = founder.name,
                    position = when (founder.profession) {
                        FounderProfession.PROGRAMMER -> "程序员"
                        FounderProfession.DESIGNER -> "策划师"
                        FounderProfession.ARTIST -> "美术师"
                        FounderProfession.SOUND_ENGINEER -> "音效师"
                        FounderProfession.CUSTOMER_SERVICE -> "客服"
                    },
                    salary = 0,
                    skillDevelopment = when (founder.profession) {
                        FounderProfession.PROGRAMMER -> 5
                        FounderProfession.DESIGNER -> 2
                        FounderProfession.ARTIST -> 1
                        FounderProfession.SOUND_ENGINEER -> 1
                        FounderProfession.CUSTOMER_SERVICE -> 1
                    },
                    skillDesign = when (founder.profession) {
                        FounderProfession.PROGRAMMER -> 2
                        FounderProfession.DESIGNER -> 5
                        FounderProfession.ARTIST -> 2
                        FounderProfession.SOUND_ENGINEER -> 1
                        FounderProfession.CUSTOMER_SERVICE -> 1
                    },
                    skillArt = when (founder.profession) {
                        FounderProfession.PROGRAMMER -> 1
                        FounderProfession.DESIGNER -> 2
                        FounderProfession.ARTIST -> 5
                        FounderProfession.SOUND_ENGINEER -> 2
                        FounderProfession.CUSTOMER_SERVICE -> 1
                    },
                    skillMusic = when (founder.profession) {
                        FounderProfession.PROGRAMMER -> 1
                        FounderProfession.DESIGNER -> 1
                        FounderProfession.ARTIST -> 2
                        FounderProfession.SOUND_ENGINEER -> 5
                        FounderProfession.CUSTOMER_SERVICE -> 1
                    },
                    skillService = when (founder.profession) {
                        FounderProfession.PROGRAMMER -> 2
                        FounderProfession.DESIGNER -> 2
                        FounderProfession.ARTIST -> 1
                        FounderProfession.SOUND_ENGINEER -> 1
                        FounderProfession.CUSTOMER_SERVICE -> 5
                    },
                    isFounder = true
                )
                allEmployees.add(founderAsEmployee)
            }
        }
    }
    
    // 初始化竞争对手（仅新游戏）
    LaunchedEffect(saveData) {
        if (saveData == null && competitors.isEmpty()) {
            // 生成初始竞争对手
            competitors = CompetitorManager.generateInitialCompetitors(
                companyName, 
                currentYear, 
                currentMonth
            )
        }
    }
    
    
    // 时间推进系统
    LaunchedEffect(gameSpeed, isPaused) {
        while (!isPaused) {
            delay(when (gameSpeed) {
                1 -> 2000L // 慢速：2秒一天
                2 -> 1000L // 中速：1秒一天
                3 -> 500L  // 快速：0.5秒一天
                else -> 1000L
            })
            
            // 更新日期
            currentDay++
            if (currentDay > 30) {
                currentDay = 1
                currentMonth++
                
                // 月结算：扣除服务器月费
                val monthlyServerCost = RevenueManager.calculateTotalMonthlyServerCost()
                money -= monthlyServerCost
                
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
                
                if (currentMonth > 12) {
                    currentMonth = 1
                    currentYear++
                }
            }
            
            // 更新游戏开发进度
            games = games.map { game ->
                if (!game.isCompleted && game.assignedEmployees.isNotEmpty()) {
                    // 计算员工技能总和
                    val totalSkillPoints = game.assignedEmployees.sumOf { employee ->
                        employee.skillDevelopment + employee.skillDesign + 
                        employee.skillArt + employee.skillMusic + employee.skillService
                    }
                    
                    // 基础进度增长：每天3%，根据员工技能调整
                    val baseProgress = 0.03f // 3%
                    val skillMultiplier = (totalSkillPoints / 25f).coerceAtLeast(0.1f)
                    val progressIncrease = baseProgress * skillMultiplier
                    
                    val newProgress = (game.developmentProgress + progressIncrease).coerceAtMost(1.0f)
                    val isCompleted = newProgress >= 1.0f
                    
                    // 如果游戏刚完成，触发发售流程
                    val updatedGame = if (isCompleted) {
                        val gameRating = GameRatingCalculator.calculateRating(game)
                        val completedGame = game.copy(
                            developmentProgress = newProgress,
                            isCompleted = true,
                            rating = gameRating.finalScore,
                            gameRating = gameRating,
                            releaseStatus = GameReleaseStatus.READY_FOR_RELEASE
                        )
                        
                        // 触发发售价格设置对话框
                        pendingReleaseGame = completedGame
                        showReleaseDialog = true
                        
                        completedGame
                    } else {
                        game.copy(
                            developmentProgress = newProgress,
                            isCompleted = false
                        )
                    }
                    
                    updatedGame
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

                    // 若存在更新任务，根据已分配员工数量推进进度
                    val employeePoints = (releasedGame.assignedEmployees.size * 20).coerceAtLeast(10)
                    val updateJustCompleted = RevenueManager.progressUpdateTask(releasedGame.id, employeePoints)
                    
                    // 如果更新刚刚完成，版本号+0.1
                    if (updateJustCompleted) {
                        val updatedGame = releasedGame.copy(
                            version = releasedGame.version + 0.1f,
                            assignedEmployees = emptyList() // 清空分配的员工
                        )
                        games = games.map { if (it.id == updatedGame.id) updatedGame else it }
                        
                        // 如果开启了自动更新，自动创建下一次更新任务
                        if (releasedGame.autoUpdate) {
                            println("【自动更新】游戏《${releasedGame.name}》的更新已自动发布！版本升级至 V${String.format(Locale.getDefault(), "%.1f", updatedGame.version)}")
                            
                            // 根据游戏已启用的付费内容生成更新选项
                            val autoUpdateFeatures = releasedGame.monetizationItems
                                .filter { it.isEnabled }
                                .map { it.type.getUpdateContentName() }
                                .distinct()
                            
                            // 如果有可用的更新内容，自动创建新的更新任务
                            if (autoUpdateFeatures.isNotEmpty()) {
                                RevenueManager.createUpdateTask(releasedGame.id, autoUpdateFeatures)
                                println("【自动更新】已自动创建下一次更新任务，共${autoUpdateFeatures.size}项内容")
                            }
                        }
                    }
                }
            
            // 为活跃岗位生成应聘者
            JobPostingService.getInstance().generateApplicantsForActiveJobs(1)
            
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
                selectedLogo = selectedLogo
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
                            founder = founder,
                            allEmployees = allEmployees,
                            games = games,
                            money = money,
                            fans = fans,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            competitors = competitors,
                            competitorNews = competitorNews
                        )
                        1 -> EmployeeManagementContent(
                            allEmployees = allEmployees,
                            onEmployeesUpdate = { updatedEmployees -> 
                                allEmployees.clear()
                                allEmployees.addAll(updatedEmployees)
                            },
                            money = money,
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney },
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
                            onFansUpdate = { updatedFans -> fans = updatedFans }
                        )
                        3 -> CompetitorContent(
                            saveData = SaveData(
                                companyName = companyName,
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
                                competitorNews = competitorNews
                            )
                        )
                        4 -> ServerManagementContent(
                            games = games,
                            money = money,
                            onPurchaseServer = { serverType ->
                                // 购买服务器到公共池
                                if (money >= serverType.cost) {
                                    // 扣除费用
                                    money -= serverType.cost
                                    
                                    // 使用固定的公共池ID存储服务器
                                    val publicPoolId = "SERVER_PUBLIC_POOL"
                                    RevenueManager.addServerToGame(
                                        gameId = publicPoolId,
                                        serverType = serverType,
                                        purchaseYear = currentYear,
                                        purchaseMonth = currentMonth,
                                        purchaseDay = currentDay
                                    )
                                    
                                    // 同时为所有现有网游添加服务器
                                    val onlineGames = games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
                                    onlineGames.forEach { game ->
                                        RevenueManager.addServerToGame(
                                            gameId = game.id,
                                            serverType = serverType,
                                            purchaseYear = currentYear,
                                            purchaseMonth = currentMonth,
                                            purchaseDay = currentDay
                                        )
                                    }
                                    
                                    // 更新所有网游的服务器信息
                                    games = games.map { game ->
                                        if (game.businessModel == BusinessModel.ONLINE_GAME) {
                                            game.copy(serverInfo = RevenueManager.getGameServerInfo(game.id))
                                        } else {
                                            game
                                        }
                                    }
                                }
                            },
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney }
                        )
                        5 -> InGameSettingsContent(
                            navController = navController,
                            money = money,
                            fans = fans,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            companyName = companyName,
                            founderName = founderName,
                            founderProfession = founderProfession,
                            games = games,
                            allEmployees = allEmployees,
                            competitors = competitors,
                            competitorNews = competitorNews
                        )
                        // 其他标签页内容可以在这里添加
                    }
            }
            
            // 底部导航栏 - 使用优化版本（字体加粗+黑色）
            EnhancedBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                pendingApplicantsCount = pendingApplicantsCount
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
    selectedLogo: String = "🎮"
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
            // 左边区域：公司LOGO和名字（垂直排列）
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // 公司LOGO在上
                Text(
                    text = selectedLogo,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                // 公司名字在下
                Text(
                    text = companyName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
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
            
            // 右边区域：资金、粉丝和设置按钮
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
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
                
                // 粉丝和设置按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                            text = formatMoney(fans.toLong()),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyOverviewContent(
    companyName: String = "我的游戏公司",
    founder: Founder? = null,
    allEmployees: List<Employee> = emptyList(),
    games: List<Game> = emptyList(),
    money: Long = 0L,
    fans: Int = 0,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    competitors: List<CompetitorCompany> = emptyList(),
    competitorNews: List<CompetitorNews> = emptyList()
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
                                .clickable { showSecretaryBubble = !showSecretaryBubble }
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
            
            // 财务状况
            val releasedGames = games.filter { 
                it.releaseStatus == GameReleaseStatus.RELEASED || 
                it.releaseStatus == GameReleaseStatus.RATED 
            }
            
            // 计算单机游戏总收入
            var singlePlayerRevenue = 0.0
            releasedGames.filter { it.businessModel == BusinessModel.SINGLE_PLAYER }.forEach { game ->
                val revenue = RevenueManager.getGameRevenue(game.id)
                if (revenue != null) {
                    val statistics = RevenueManager.calculateStatistics(revenue)
                    singlePlayerRevenue += statistics.totalRevenue
                }
            }
            
            // 计算网游总收入
            var onlineGameRevenue = 0.0
            releasedGames.filter { it.businessModel == BusinessModel.ONLINE_GAME }.forEach { game ->
                val revenue = RevenueManager.getGameRevenue(game.id)
                if (revenue != null) {
                    val statistics = RevenueManager.calculateStatistics(revenue)
                    onlineGameRevenue += statistics.totalRevenue
                }
            }
            
            // 计算上个月总收入（简化：取最近30天的收入）
            var lastMonthRevenue = 0.0
            releasedGames.forEach { game ->
                val revenue = RevenueManager.getGameRevenue(game.id)
                if (revenue != null && revenue.dailySalesList.isNotEmpty()) {
                    val recentDays = revenue.dailySalesList.takeLast(30)
                    lastMonthRevenue += recentDays.sumOf { it.revenue }
                }
            }
            
            // 计算去年总收入（简化：取最近365天的收入）
            var lastYearRevenue = 0.0
            releasedGames.forEach { game ->
                val revenue = RevenueManager.getGameRevenue(game.id)
                if (revenue != null && revenue.dailySalesList.isNotEmpty()) {
                    val recentDays = revenue.dailySalesList.takeLast(365)
                    lastYearRevenue += recentDays.sumOf { it.revenue }
                }
            }
            
            CompanyInfoCard(
                title = "财务状况",
                items = listOf(
                    "单机收入" to "¥${formatMoneyWithDecimals(singlePlayerRevenue)}",
                    "网游收入" to "¥${formatMoneyWithDecimals(onlineGameRevenue)}",
                    "上个月总收入" to "¥${formatMoneyWithDecimals(lastMonthRevenue)}",
                    "去年总收入" to "¥${formatMoneyWithDecimals(lastYearRevenue)}"
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
    items: List<Pair<String, String>>
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
            Text(
                text = title,
                color = Color(0xFFF59E0B), // 橙色强调色
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
    pendingApplicantsCount: Int = 0 // 待处理应聘者数量
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
                onClick = { onTabSelected(2) }
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
            
            EnhancedBottomNavItem(
                icon = "⚙️",
                label = "设置",
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
    var saves by remember { mutableStateOf(saveManager.getAllSaves()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var saveToDelete by remember { mutableStateOf<Pair<Int, SaveData?>?>(null) }
    
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
            
            // 存档位列表
            for (slotIndex in 1..3) {
                SaveSlotCard(
                    slotIndex = slotIndex,
                    saveData = saves[slotIndex],
                    onLoadSave = { saveData ->
                        // 设置全局存档数据，以便GameScreen可以使用
                        currentLoadedSaveData = saveData
                        Toast.makeText(context, "加载存档 $slotIndex", Toast.LENGTH_SHORT).show()
                        navController.navigate("game/${saveData.companyName}/${saveData.founderName}/🎮/${saveData.founderProfession?.name ?: "PROGRAMMER"}")
                    },
                    onDeleteSave = {
                        saveToDelete = Pair(slotIndex, saves[slotIndex])
                        showDeleteConfirmDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
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
                    saveManager.deleteSave(saveToDelete!!.first)
                    saves = saveManager.getAllSaves()
                    Toast.makeText(context, "删除存档 ${saveToDelete!!.first}", Toast.LENGTH_SHORT).show()
                    showDeleteConfirmDialog = false
                    saveToDelete = null
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
            .height(140.dp)
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
                    
                    Text(
                        text = "公司: ${saveData.companyName}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "资金: ¥${saveData.money} | 粉丝: ${saveData.fans}",
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
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4facfe),
                        Color(0xFF00f2fe)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚙️ 设置",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            GameMenuButton(
                text = "保存游戏",
                onClick = {
                    Toast.makeText(context, "游戏已保存", Toast.LENGTH_SHORT).show()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GameMenuButton(
                text = "语言选项（英语）",
                onClick = {
                    Toast.makeText(context, "语言已切换为英语", Toast.LENGTH_SHORT).show()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GameMenuButton(
                text = "返回主菜单",
                onClick = { navController.popBackStack() }
            )
        }
    }
}





// 存档管理类
class SaveManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_saves", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveGame(slotIndex: Int, saveData: SaveData) {
        val json = gson.toJson(saveData)
        sharedPreferences.edit {
            putString("save_slot_$slotIndex", json)
        }
    }
    
    fun loadGame(slotIndex: Int): SaveData? {
        val json = sharedPreferences.getString("save_slot_$slotIndex", null)
        return if (json != null) {
            try {
                gson.fromJson(json, SaveData::class.java)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    fun deleteSave(slotIndex: Int) {
        sharedPreferences.edit {
            remove("save_slot_$slotIndex")
        }
    }
    
    fun getAllSaves(): Map<Int, SaveData?> {
        return mapOf(
            1 to loadGame(1),
            2 to loadGame(2),
            3 to loadGame(3)
        )
    }
}





















@Composable
fun InGameSettingsContent(
    navController: NavController,
    money: Long = 10000L,
    fans: Int = 0,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    companyName: String = "我的游戏公司",
    founderName: String = "创始人",
    founderProfession: FounderProfession = FounderProfession.PROGRAMMER,
    games: List<Game> = emptyList(),
    allEmployees: List<Employee> = emptyList(),
    competitors: List<CompetitorCompany> = emptyList(),
    competitorNews: List<CompetitorNews> = emptyList()
) {
    val context = LocalContext.current
    val saveManager = remember { SaveManager(context) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var shouldReturnToMenuAfterSave by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⚙️ 游戏设置",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // 保存游戏按钮
        Button(
            onClick = {
                showSaveDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                            text = "资金: ¥${formatMoney(selectedExistingSave!!.money)} | 粉丝: ${formatMoney(selectedExistingSave!!.fans.toLong())}",
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
                                saveTime = System.currentTimeMillis()
                            )
                            saveManager.saveGame(selectedSlotNumber, saveData)
                            // 显示保存成功提示（在重置前使用selectedSlotNumber）
                            Toast.makeText(context, "游戏已保存到存档位 $selectedSlotNumber", Toast.LENGTH_SHORT).show()
                            showSaveDialog = false
                            showOverwriteConfirmDialog = false
                            selectedSlotNumber = 0
                            selectedExistingSave = null
                            // 如果需要在保存后返回主菜单
                            if (shouldReturnToMenuAfterSave) {
                                shouldReturnToMenuAfterSave = false
                                navController.navigate("main_menu")
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
                    
                    repeat(3) { index ->
                        val slotNumber = index + 1
                        val existingSave = saveManager.loadGame(slotNumber)
                        
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
                                            saveTime = System.currentTimeMillis()
                                        )
                                        saveManager.saveGame(slotNumber, saveData)
                                        showSaveDialog = false
                                        // 显示保存成功提示
                                        Toast.makeText(context, "游戏已保存到存档位 $slotNumber", Toast.LENGTH_SHORT).show()
                                        // 如果需要在保存后返回主菜单
                                        if (shouldReturnToMenuAfterSave) {
                                            shouldReturnToMenuAfterSave = false
                                            navController.navigate("main_menu")
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
                                        text = "资金: ¥${formatMoney(existingSave.money)} | 粉丝: ${formatMoney(existingSave.fans.toLong())}",
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