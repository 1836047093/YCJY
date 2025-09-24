package com.example.yjcy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.remember
import kotlin.random.Random
import kotlin.math.sin
import kotlin.math.cos
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalConfiguration
import com.example.yjcy.ui.theme.YjcyTheme

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.google.gson.Gson
import com.example.yjcy.ui.RecruitmentCenter
import com.example.yjcy.data.CandidateManager
import com.example.yjcy.data.Candidate
import com.example.yjcy.ui.EmployeeManagementEnhanced
import com.example.yjcy.ui.HRCenterEmployeeManagement
import com.example.yjcy.ui.HRCenterScreen
import com.example.yjcy.ui.ProjectManagementWrapper
import com.example.yjcy.ui.RecruitmentConfigScreen
import com.example.yjcy.ui.CandidateConfirmationScreen
import com.example.yjcy.ui.RecruitmentHistoryScreen
import com.example.yjcy.data.Employee
import com.example.yjcy.data.Founder
import com.example.yjcy.data.Game
import com.example.yjcy.data.SaveData
import com.example.yjcy.ui.GameTheme
import com.example.yjcy.ui.Platform
import com.example.yjcy.ui.BusinessModel
import com.example.yjcy.data.Competitor
import com.example.yjcy.data.GameThemeTrend
import com.example.yjcy.data.TrendDirection
import com.example.yjcy.data.FounderProfession
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat



// 资金格式化函数
fun formatMoney(amount: Long): String {
    return when {
        amount >= 1_000_000_000_000L -> "${amount / 1_000_000_000_000L}T"
        amount >= 1_000_000_000L -> "${amount / 1_000_000_000L}B"
        amount >= 1_000_000L -> "${amount / 1_000_000L}M"
        amount >= 1_000L -> "${amount / 1_000L}K"
        else -> amount.toString()
    }
}

// 全局变量存储当前加载的存档数据
var currentLoadedSaveData: SaveData? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 增强全屏显示设置
        // enableFullScreenDisplay()  // 临时注释掉以解决闪退问题
        
        enableEdgeToEdge()
        setContent {
            YjcyTheme {
                val navController = rememberNavController()
                
                // SharedPreferences for privacy policy agreement
                val sharedPreferences = getSharedPreferences("privacy_settings", Context.MODE_PRIVATE)
                var showPrivacyDialog by remember { mutableStateOf(!sharedPreferences.getBoolean("privacy_agreed", false)) }
                
                // Privacy Policy Dialog
                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(
                        onAgree = {
                            sharedPreferences.edit().putBoolean("privacy_agreed", true).apply()
                            showPrivacyDialog = false
                        }
                    )
                }
                
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
                        LeaderboardScreen(navController)
                    }
                    composable("in_game_settings") {
                        InGameSettingsScreen(navController)
                    }
                    composable("recruitment_config") {
                        RecruitmentConfigScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("candidate_confirmation/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                        CandidateConfirmationScreen(
                            taskId = taskId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("recruitment_history") {
                        RecruitmentHistoryScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
    
    // 增强全屏显示方法
    private fun enableFullScreenDisplay() {
        // 设置窗口兼容性
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用 WindowInsetsController
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 7-10 兼容实现
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // 设置刘海屏适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = 
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

}

@Composable
fun InGameSettingsScreen(navController: NavController) {
    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    var gameSpeed by remember { mutableStateOf(1f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = "游戏设置",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // 保存游戏按钮
            GameMenuButton(
                text = "保存游戏",
                onClick = {
                    // TODO: 实现保存游戏逻辑
                }
            )
            
            // 音效开关
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "音效",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            // 音乐开关
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "音乐",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = musicEnabled,
                        onCheckedChange = { musicEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            // 游戏速度设置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "游戏速度: ${gameSpeed.toInt()}x",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = gameSpeed,
                        onValueChange = { gameSpeed = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CAF50),
                            activeTrackColor = Color(0xFF4CAF50),
                            inactiveTrackColor = Color.Gray
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 返回游戏按钮
            GameMenuButton(
                text = "返回游戏",
                onClick = {
                    navController.popBackStack()
                }
            )
            
            // 返回主菜单按钮
            GameMenuButton(
                text = "返回主菜单",
                onClick = {
                    navController.navigate("main_menu") {
                        popUpTo("main_menu") { inclusive = true }
                    }
                }
            )
        }
    }
    }

@Composable
fun MainMenuScreen(navController: androidx.navigation.NavController) {
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
                    text = "🏆 排行榜",
                    onClick = { navController.navigate("leaderboard") }
                )
                

                
                GameMenuButton(
                    text = "⚙️ 设置",
                    onClick = { navController.navigate("settings") }
                )
            }
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
fun GameSetupScreen(navController: androidx.navigation.NavController) {
    var companyName by remember { mutableStateOf("") }
    var founderName by remember { mutableStateOf("") }
    var selectedLogo by remember { mutableStateOf("🎮") }
    var selectedProfession by remember { mutableStateOf<FounderProfession?>(null) }
    var isCompanyNameValid by remember { mutableStateOf(true) }
    
    val logoOptions = listOf("🎮", "🏢", "💼", "🚀", "⭐", "🎯")
    
    // 验证公司名称（仅限5个字符和数字）
    fun validateCompanyName(name: String): Boolean {
        return name.length <= 5 && name.all { it.isLetterOrDigit() }
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
                    items(FounderProfession.values()) { profession ->
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
    navController: androidx.navigation.NavController,
    initialCompanyName: String = "我的游戏公司",
    initialFounderName: String = "创始人",
    selectedLogo: String = "🎮",
    initialFounderProfession: String = "PROGRAMMER",
    saveData: SaveData? = null
) {
    // 游戏状态数据 - 如果有存档数据则使用存档数据，否则使用默认值
    var money by remember { mutableStateOf(saveData?.money ?: 1000000L) }
    var fans by remember { mutableStateOf(saveData?.fans ?: 0) }
    var currentYear by remember { mutableStateOf(saveData?.currentYear ?: 1) }
    var currentMonth by remember { mutableStateOf(saveData?.currentMonth ?: 1) }
    var currentDay by remember { mutableStateOf(saveData?.currentDay ?: 1) }
    var gameSpeed by remember { mutableStateOf(1) }
    var selectedTab by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf(saveData?.companyName ?: initialCompanyName) }
    var founderName by remember { mutableStateOf(saveData?.founderName ?: initialFounderName) }
    var founderProfession by remember { mutableStateOf(saveData?.founderProfession ?: try { FounderProfession.valueOf(initialFounderProfession) } catch (e: IllegalArgumentException) { FounderProfession.PROGRAMMER }) }
    var games by remember { mutableStateOf(saveData?.games ?: emptyList<Game>()) }
    var showRecruitmentCenter by remember { mutableStateOf(false) }
    var showHRCenter by remember { mutableStateOf(false) }
    
    // 消息状态
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    
    // 员工状态管理 - 提升到GameScreen级别
    val allEmployees = remember { mutableStateListOf<Employee>() }
    
    // 候选人管理器 - 提升到GameScreen级别以保持状态
    val candidateManager = remember { CandidateManager() }
    
    // 创建创始人对象
    val founder = remember(founderName, founderProfession) {
        Founder(name = founderName, profession = founderProfession)
    }
    
    // 初始化员工列表 - 将创始人转换为员工并添加到列表开头
    LaunchedEffect(founder) {
        if (allEmployees.isEmpty() || allEmployees.firstOrNull()?.name != founder.name) {
            allEmployees.clear()
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
                }
            )
            allEmployees.add(founderAsEmployee)
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
                    
                    // 基础进度增长：每天0.1%，根据员工技能调整
                    val baseProgress = 0.001f // 0.1%
                    val skillMultiplier = (totalSkillPoints / 25f).coerceAtLeast(0.1f)
                    val progressIncrease = baseProgress * skillMultiplier
                    
                    val newProgress = (game.developmentProgress + progressIncrease).coerceAtMost(1.0f)
                    val isCompleted = newProgress >= 1.0f
                    
                    game.copy(
                        developmentProgress = newProgress,
                        isCompleted = isCompleted
                    )
                } else {
                    game
                }
            }
        }
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
                navController = navController,
                money = money,
                fans = fans,
                year = currentYear,
                month = currentMonth,
                day = currentDay,
                gameSpeed = gameSpeed,
                onSpeedChange = { gameSpeed = it },
                isPaused = isPaused,
                onPauseToggle = { isPaused = !isPaused },
                onSettingsClick = { navController.navigate("in_game_settings") },
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
                if (showRecruitmentCenter && selectedTab == 1) {
                    // 显示招聘中心界面
                    RecruitmentCenterContent(
                        candidateManager = candidateManager,
                        onBack = { showRecruitmentCenter = false },
                        onHireCandidate = { candidate, candidateManager ->
                            // 检查职位人数限制（每个职位最多5人）
                            val currentPositionCount = allEmployees.count { it.position == candidate.position }
                            if (currentPositionCount >= 5) {
                                // 显示职位人数已满消息
                                messageText = "${candidate.position}职位已达到招聘上限（5人）！无法招聘${candidate.name}"
                                showMessage = true
                            } else if (money >= candidate.recruitmentCost) {
                                // 根据招聘成功率判断是否成功
                                val random = kotlin.random.Random.nextFloat()
                                val hireSuccessRate = candidate.getHireSuccessRate()
                                
                                if (random <= hireSuccessRate) {
                                     // 招聘成功
                                     // 扣除招聘费用
                                     money -= candidate.recruitmentCost
                                     
                                     // 更新候选人状态为已雇佣
                                     candidateManager.updateCandidateStatus(candidate.id, com.example.yjcy.data.AvailabilityStatus.HIRED)
                                     
                                     // 将候选人转换为员工并添加到员工列表
                                    val newEmployee = Employee(
                                        id = candidate.id,
                                        name = candidate.name,
                                        position = candidate.position,
                                        skillDevelopment = candidate.programmingSkill,
                                        skillDesign = candidate.designSkill,
                                        skillArt = candidate.planningSkill,
                                        skillMusic = candidate.soundSkill,
                                        skillService = candidate.customerServiceSkill,
                                        salary = candidate.expectedSalary
                                    )
                                    
                                    // 确保创始人员工（id=0）始终保持在列表第一位
                                    val founderEmployee = allEmployees.find { it.id == 0 }
                                    allEmployees.add(newEmployee)
                                    if (founderEmployee != null && allEmployees.firstOrNull()?.id != 0) {
                                        allEmployees.remove(founderEmployee)
                                        allEmployees.add(0, founderEmployee)
                                    }
                                } else {
                                    // 招聘失败，扣除一半费用
                                    money -= candidate.recruitmentCost / 2
                                    // 显示招聘失败消息
                                    messageText = "招聘${candidate.name}失败！扣除一半招聘费用：¥${candidate.recruitmentCost / 2}"
                                    showMessage = true
                                }
                            } else {
                                // 显示资金不足消息
                                messageText = "资金不足！招聘${candidate.name}需要¥${candidate.recruitmentCost}，当前资金：¥${money}"
                                showMessage = true
                            }
                        }
                    )
                } else if (showHRCenter && selectedTab == 1) {
                    // 显示人事中心界面
                    HRCenterScreen(
                        onNavigateToConfig = { navController.navigate("recruitment_config") },
                        onNavigateToConfirmation = { navController.navigate("candidate_confirmation/0") },
                        onNavigateToHistory = { navController.navigate("recruitment_history") },
                        onNavigateBack = { showHRCenter = false }
                    )
                } else {
                    when (selectedTab) {
                        0 -> CompanyOverviewContent(
                        companyName = companyName,
                        founder = founder,
                        allEmployees = allEmployees
                    )
                        1 -> HRCenterEmployeeManagement(
                            employees = allEmployees,
                            onTrainEmployee = { employee, skillType ->
                                // 创始人不能被培训（技能已经是满级）
                                if (employee.id != 0) {
                                    // 执行培训逻辑
                                    val index = allEmployees.indexOfFirst { it.id == employee.id }
                                    if (index != -1) {
                                        val updatedEmployee = when (skillType) {
                                            "开发" -> employee.copy(skillDevelopment = minOf(100, employee.skillDevelopment + 10))
                                            "设计" -> employee.copy(skillDesign = minOf(100, employee.skillDesign + 10))
                                            "美工" -> employee.copy(skillArt = minOf(100, employee.skillArt + 10))
                                            "音乐" -> employee.copy(skillMusic = minOf(100, employee.skillMusic + 10))
                                            "服务" -> employee.copy(skillService = minOf(100, employee.skillService + 10))
                                            else -> employee
                                        }
                                        allEmployees[index] = updatedEmployee
                                    }
                                }
                            },
                            onDismissEmployee = { employee ->
                                // 创始人不能被解雇
                                if (employee.id != 0) {
                                    allEmployees.removeAll { it.id == employee.id }
                                }
                            },
                            onNavigateToHRCenter = { showHRCenter = true }
                        )
                        2 -> ProjectManagementWrapper(
                            games = games,
                            onGamesUpdate = { updatedGames -> games = updatedGames },
                            founder = founder,
                            allEmployees = allEmployees
                        )
                        3 -> MarketAnalysisContent()
                        4 -> InGameSettingsContent(
                            navController = navController,
                            money = money,
                            fans = fans,
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            currentDay = currentDay,
                            companyName = companyName,
                            founderName = founderName,
                            games = games
                        )
                        // 其他标签页内容可以在这里添加
                    }
                }
            }
            
            // 底部导航栏 - 使用优化版本（字体加粗+黑色）
            EnhancedBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
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
    }
}

@Composable
fun TopInfoBar(
    navController: androidx.navigation.NavController,
    money: Long,
    fans: Int,
    year: Int,
    month: Int,
    day: Int,
    gameSpeed: Int,
    onSpeedChange: (Int) -> Unit,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onSettingsClick: () -> Unit,
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
                Text(
                    text = "¥${formatMoney(money)}",
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
                        text = "👥",
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
fun PauseButton(
    isPaused: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = if (isPaused) 
                    Color(0xFFDC2626).copy(alpha = 0.8f) 
                else 
                    Color(0xFF16A34A).copy(alpha = 0.8f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isPaused) "▶" else "⏸",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SpeedButton(
    speed: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = if (isSelected) 
                    Color(0xFF6366F1).copy(alpha = 0.9f) 
                else 
                    Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) 
                    Color(0xFF6366F1) 
                else 
                    Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = speed.toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CompanyOverviewContent(
    companyName: String = "我的游戏公司",
    founder: Founder? = null,
    allEmployees: List<Employee> = emptyList()
) {
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
                .padding(16.dp)
        ) {
            Text(
                text = "🏢 公司概览",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 公司基本信息
            CompanyInfoCard(
                title = "公司信息",
                items = listOf(
                    "公司名称" to companyName,
                    "成立时间" to "第1年1月1日"
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
                        "技能等级" to "${founder.profession.specialtySkill} Lv.5"
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 员工信息
            val employeesByProfession = allEmployees.groupBy { it.position }
            CompanyInfoCard(
                title = "团队状况",
                items = listOf(
                    "员工总数" to "${allEmployees.size}人",
                    "程序员" to "${employeesByProfession["程序员"]?.size ?: 0}人",
                    "美术师" to "${employeesByProfession["美术师"]?.size ?: 0}人",
                    "策划师" to "${employeesByProfession["策划师"]?.size ?: 0}人",
                    "客服" to "${employeesByProfession["客服"]?.size ?: 0}人",
                    "音效师" to "${employeesByProfession["音效师"]?.size ?: 0}人"
                )
            )
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

@Composable
fun EmployeeManagementContent(
    onNavigateToRecruitment: () -> Unit = {},
    founder: Founder? = null,
    allEmployees: MutableList<Employee> = mutableListOf()
) {

    
    // 使用增强版员工管理界面
    EmployeeManagementEnhanced(
        employees = allEmployees,
        onTrainEmployee = { employee, skillType ->
            // 创始人不能被培训（技能已经是满级）
            if (employee.id != 0) {
                // 执行培训逻辑
                val index = allEmployees.indexOfFirst { it.id == employee.id }
                if (index != -1) {
                    val updatedEmployee = when (skillType) {
                        "开发" -> employee.copy(skillDevelopment = minOf(100, employee.skillDevelopment + 10))
                        "设计" -> employee.copy(skillDesign = minOf(100, employee.skillDesign + 10))
                        "美工" -> employee.copy(skillArt = minOf(100, employee.skillArt + 10))
                        "音乐" -> employee.copy(skillMusic = minOf(100, employee.skillMusic + 10))
                        "服务" -> employee.copy(skillService = minOf(100, employee.skillService + 10))
                        else -> employee
                    }
                    allEmployees[index] = updatedEmployee
                }
            }
        },
        onDismissEmployee = { employee ->
            // 创始人不能被解雇
            if (employee.id != 0) {
                allEmployees.removeAll { it.id == employee.id }
            }
        },
        onNavigateToRecruitment = onNavigateToRecruitment
    )
}



@Composable
fun EmployeeStatsCard(employees: List<Employee>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "团队概况",
                color = Color(0xFFF59E0B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总员工数",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${employees.size}人",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "月薪总支出",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "¥${formatMoney(employees.sumOf { it.salary }.toLong())}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "平均技能",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    val avgSkill = if (employees.isNotEmpty()) {
                        employees.map { it.getSpecialtySkillLevel().toDouble() }.average()
                    } else 0.0
                    Text(
                        text = String.format("%.1f级", avgSkill),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeList(
    employees: List<Employee>,
    onTrainEmployee: (Employee, String) -> Unit,
    onDismissEmployee: (Employee) -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(employees) { employee ->
            EmployeeCard(
                employee = employee,
                onTrainEmployee = onTrainEmployee,
                onDismissEmployee = onDismissEmployee
            )
        }
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onTrainEmployee: (Employee, String) -> Unit,
    onDismissEmployee: (Employee) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 员工基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getPositionIcon(employee.position),
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = employee.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = employee.position,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = "¥${formatMoney(employee.salary.toLong())}/月",
                    color = Color(0xFFF59E0B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 技能等级显示
            Column {
                Text(
                    text = "专业技能",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // 只显示员工的专属技能
                val specialtySkillType = employee.getSpecialtySkillType()
                val specialtySkillLevel = employee.getSpecialtySkillLevel()
                
                SkillLevelRow(
                    skillName = specialtySkillType,
                    level = specialtySkillLevel,
                    onTrainClick = { onTrainEmployee(employee, specialtySkillType) }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Card(
                    modifier = Modifier.clickable { onDismissEmployee(employee) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDC2626).copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "解雇",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SkillLevelRow(
    skillName: String,
    level: Int,
    onTrainClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = skillName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )
            
            // 技能等级星星显示
            Row {
                repeat(5) { index ->
                    Text(
                        text = if (index < level) "⭐" else "☆",
                        color = if (index < level) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Lv.$level",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (level < 5) {
            Card(
                modifier = Modifier.clickable { onTrainClick() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16A34A).copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "培训",
                    color = Color(0xFF16A34A),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

fun getPositionIcon(position: String): String {
    return when (position) {
        "程序员" -> "💻"
        "策划师" -> "📋"
        "美术师" -> "🎨"
        "音效师" -> "🎵"
        "客服" -> "📞"
        else -> "👤"
    }
}

@Composable
fun TrainingConfirmDialog(
    employee: Employee,
    skillType: String,
    onConfirm: (Employee, String) -> Unit,
    onDismiss: () -> Unit
) {
    // 只允许培训员工的专属技能
    val currentLevel = employee.getSpecialtySkillLevel()
    
    val trainingCost = currentLevel * 1000 // 培训费用随等级增加
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "员工培训确认",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "确定要为 ${employee.name} 进行${skillType}技能培训吗？",
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "当前等级：Lv.$currentLevel",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "培训后等级：Lv.${minOf(5, currentLevel + 1)}",
                    color = Color(0xFF16A34A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "培训费用：¥${formatMoney(trainingCost.toLong())}",
                    color = Color(0xFFF59E0B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Card(
                modifier = Modifier.clickable { onConfirm(employee, skillType) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16A34A).copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "确认培训",
                    color = Color(0xFF16A34A),
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
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun DismissConfirmDialog(
    employee: Employee,
    onConfirm: (Employee) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "解雇员工确认",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "确定要解雇 ${employee.name} 吗？",
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "职位：${employee.position}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "月薪：¥${formatMoney(employee.salary.toLong())}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ 此操作不可撤销！",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Card(
                modifier = Modifier.clickable { onConfirm(employee) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDC2626).copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "确认解雇",
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
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
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
            BottomNavItem(
                icon = "🏢",
                label = "公司概览",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            
            BottomNavItem(
                icon = "👥",
                label = "员工管理",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            
            BottomNavItem(
                icon = "🎯",
                label = "招聘中心",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            
            BottomNavItem(
                icon = "🎮",
                label = "项目管理",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) }
            )
            
            BottomNavItem(
                icon = "📊",
                label = "市场分析",
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) }
            )
            
            BottomNavItem(
                icon = "⚙️",
                label = "设置",
                isSelected = selectedTab == 5,
                onClick = { onTabSelected(5) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "textColor"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .scale(scale)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier
                .background(
                    brush = if (isSelected) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.8f),
                                Color(0xFF8B5CF6).copy(alpha = 0.6f),
                                Color(0xFF4C1D95).copy(alpha = 0.4f)
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
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    brush = if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// 优化版本的底部导航栏组件 - 字体加粗+黑色
@Composable
fun EnhancedBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
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
                onClick = { onTabSelected(1) }
            )
            
            EnhancedBottomNavItem(
                icon = "🎮",
                label = "项目管理",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            
            EnhancedBottomNavItem(
                icon = "📊",
                label = "市场分析",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) }
            )
            
            EnhancedBottomNavItem(
                icon = "⚙️",
                label = "设置",
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
    onClick: () -> Unit
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
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Black, // 选中时为白色，未选中时为黑色
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold // 设置为加粗
        )
    }
}

@Composable
fun ContinueScreen(navController: androidx.navigation.NavController) {
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
fun SettingsScreen(navController: androidx.navigation.NavController) {
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

@Composable
fun LeaderboardScreen(navController: androidx.navigation.NavController) {
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
                text = "🏆 排行榜",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            GameMenuButton(
                text = "返回主菜单",
                onClick = { navController.popBackStack() }
            )
        }
    }
}







// 现代化色彩系统
object ModernColorSystem {
    // 主要渐变背景
    val primaryGradient = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )
    
    val secondaryGradient = listOf(
        Color(0xFF4facfe),
        Color(0xFF00f2fe)
    )
    
    val accentGradient = listOf(
        Color(0xFFfa709a),
        Color(0xFFfee140)
    )
    
    // 毛玻璃效果颜色
    val glassBackground = Color.White.copy(alpha = 0.1f)
    val glassStroke = Color.White.copy(alpha = 0.2f)
    val glassShadow = Color.Black.copy(alpha = 0.1f)
    
    // 文本颜色
    val primaryText = Color.White
    val secondaryText = Color.White.copy(alpha = 0.8f)
    val accentText = Color.White.copy(alpha = 0.9f)
    
    // 状态颜色
    val successColor = Color(0xFF10B981)
    val warningColor = Color(0xFFF59E0B)
    val errorColor = Color(0xFFEF4444)
    val infoColor = Color(0xFF3B82F6)
    
    // 趋势颜色
    val trendUpColor = Color(0xFF10B981)
    val trendDownColor = Color(0xFFEF4444)
    val trendStableColor = Color(0xFF6B7280)
}

// 现代化动画系统
object ModernAnimationSystem {
    // 基础动画时长
    const val FAST_ANIMATION = 200
    const val NORMAL_ANIMATION = 300
    const val SLOW_ANIMATION = 500
    
    // 缓动函数
    val fastOutSlowIn = FastOutSlowInEasing
    val linearOutSlowIn = LinearOutSlowInEasing
    val fastOutLinearIn = FastOutLinearInEasing
    
    // 常用动画规格
    val fadeInOut = tween<Float>(NORMAL_ANIMATION, easing = fastOutSlowIn)
    val slideInOut = tween<IntOffset>(NORMAL_ANIMATION, easing = fastOutSlowIn)
    val scaleInOut = tween<Float>(FAST_ANIMATION, easing = fastOutSlowIn)
    val colorTransition = tween<Color>(NORMAL_ANIMATION, easing = linearOutSlowIn)
}

// 毛玻璃效果组件
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = ModernAnimationSystem.scaleInOut
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = ModernAnimationSystem.fadeInOut
    )
    
    Card(
        modifier = modifier
            .scale(animatedScale)
            .alpha(animatedAlpha)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = ModernColorSystem.glassBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = ModernColorSystem.glassStroke
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// 数据芯片组件
@Composable
fun DataChip(
    text: String,
    icon: String? = null,
    color: Color = ModernColorSystem.infoColor,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = 12.sp
                )
            }
            Text(
                text = text,
                fontSize = 12.sp,
                color = ModernColorSystem.primaryText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 趋势指示器组件
@Composable
fun TrendIndicator(
    direction: TrendDirection,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (direction) {
        TrendDirection.UP -> "📈" to ModernColorSystem.trendUpColor
        TrendDirection.DOWN -> "📉" to ModernColorSystem.trendDownColor
        TrendDirection.STABLE -> "➡️" to ModernColorSystem.trendStableColor
    }
    
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
        }
    }
}

// 响应式布局系统
object ResponsiveLayoutSystem {
    // 屏幕断点
    const val COMPACT_WIDTH = 600
    const val MEDIUM_WIDTH = 840
    const val EXPANDED_WIDTH = 1200
    
    // 布局配置
    data class LayoutConfig(
        val isCompact: Boolean,
        val isMedium: Boolean,
        val isExpanded: Boolean,
        val columns: Int,
        val cardSpacing: Int,
        val contentPadding: Int,
        val itemSpacing: Int,
        val cardPadding: Int,
        val titleFontSize: Int
    )
}

@Composable
fun getLayoutConfig(): ResponsiveLayoutSystem.LayoutConfig {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < ResponsiveLayoutSystem.COMPACT_WIDTH -> ResponsiveLayoutSystem.LayoutConfig(
            isCompact = true,
            isMedium = false,
            isExpanded = false,
            columns = 1,
            cardSpacing = 8,
            contentPadding = 12,
            itemSpacing = 8,
            cardPadding = 12,
            titleFontSize = 16
        )
        screenWidth < ResponsiveLayoutSystem.MEDIUM_WIDTH -> ResponsiveLayoutSystem.LayoutConfig(
            isCompact = false,
            isMedium = true,
            isExpanded = false,
            columns = 2,
            cardSpacing = 12,
            contentPadding = 16,
            itemSpacing = 12,
            cardPadding = 16,
            titleFontSize = 18
        )
        else -> ResponsiveLayoutSystem.LayoutConfig(
            isCompact = false,
            isMedium = false,
            isExpanded = true,
            columns = 3,
            cardSpacing = 16,
            contentPadding = 20,
            itemSpacing = 16,
            cardPadding = 20,
            titleFontSize = 20
        )
    }
}

// 竞争对手数据生成器
class CompetitorDataGenerator {
    private val companyNames = listOf(
        "星辰游戏", "梦想工作室", "创新互娱", 
        "未来科技", "极光工作室"
    )
    private val icons = listOf("🎮", "🌟", "🚀", "💎", "⚡")
    
    fun generateCompetitors(): List<Competitor> {
        return companyNames.mapIndexed { index, name ->
            Competitor(
                id = "comp_$index",
                name = name,
                icon = icons[index],
                annualRevenue = Random.nextLong(1000, 50000),
                fanCount = Random.nextInt(10, 1000),
                marketValue = Random.nextLong(5000, 200000)
            )
        }
    }
    
    // 动态更新现有竞争对手数据（模拟市场波动）
    fun updateCompetitorData(current: Competitor): Competitor {
        val revenueChange = Random.nextFloat() * 0.2f - 0.1f // ±10%变化
        val fanChange = Random.nextFloat() * 0.15f - 0.075f // ±7.5%变化
        val valueChange = Random.nextFloat() * 0.25f - 0.125f // ±12.5%变化
        
        return current.copy(
            annualRevenue = (current.annualRevenue * (1 + revenueChange)).toLong().coerceAtLeast(500),
            fanCount = (current.fanCount * (1 + fanChange)).toInt().coerceAtLeast(5),
            marketValue = (current.marketValue * (1 + valueChange)).toLong().coerceAtLeast(1000)
        )
    }
}

// 游戏主题趋势生成器
class GameThemeTrendGenerator {
    private val themes = listOf(
        "动作" to "⚔️",
        "角色扮演" to "🧙",
        "策略" to "🏰",
        "模拟" to "🏗️",
        "休闲" to "🎯",
        "竞技" to "🏆"
    )
    
    fun generateGameThemeTrends(): List<GameThemeTrend> {
        return themes.map { (theme, icon) ->
            GameThemeTrend(
                theme = theme,
                icon = icon,
                hotIndex = Random.nextFloat() * 100,
                marketShare = Random.nextFloat() * 30,
                trend = TrendDirection.values().random()
            )
        }.sortedByDescending { it.hotIndex }
    }
    
    // 动态更新主题趋势（模拟市场变化）
    fun updateThemeTrend(current: GameThemeTrend): GameThemeTrend {
        val hotIndexChange = Random.nextFloat() * 20f - 10f // ±10点变化
        val shareChange = Random.nextFloat() * 5f - 2.5f // ±2.5%变化
        
        return current.copy(
            hotIndex = (current.hotIndex + hotIndexChange).coerceIn(0f, 100f),
            marketShare = (current.marketShare + shareChange).coerceIn(0f, 50f),
            trend = when {
                hotIndexChange > 3f -> TrendDirection.UP
                hotIndexChange < -3f -> TrendDirection.DOWN
                else -> TrendDirection.STABLE
            }
        )
    }
}

// 存档管理类
class SaveManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_saves", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveGame(slotIndex: Int, saveData: SaveData) {
        val json = gson.toJson(saveData)
        sharedPreferences.edit()
            .putString("save_slot_$slotIndex", json)
            .apply()
    }
    
    fun loadGame(slotIndex: Int): SaveData? {
        val json = sharedPreferences.getString("save_slot_$slotIndex", null)
        return if (json != null) {
            try {
                gson.fromJson(json, SaveData::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    fun deleteSave(slotIndex: Int) {
        sharedPreferences.edit()
            .remove("save_slot_$slotIndex")
            .apply()
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
fun ProjectManagementContent(
    games: List<Game> = emptyList(),
    onGamesUpdate: (List<Game>) -> Unit = {}
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
        Text(
            text = "当前项目",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(games) { game ->
                    GameProjectCard(game = game)
                }
            }
        }
    }
    
    // 游戏开发流程对话框
    if (showGameDevelopmentDialog) {
        GameDevelopmentDialog(
            onDismiss = { showGameDevelopmentDialog = false },
            onGameCreated = { newGame ->
                onGamesUpdate(games + newGame)
                showGameDevelopmentDialog = false
            }
        )
    }
}

@Composable
fun GameProjectCard(game: Game) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = game.theme.icon,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "主题: ${game.theme.displayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Text(
                text = "平台: ${game.platforms.joinToString(", ") { it.displayName }}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Text(
                text = "商业模式: ${game.businessModel.displayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Text(
                        text = "开发进度",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${(game.developmentProgress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = game.developmentProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF10B981),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun GameDevelopmentDialog(
    onDismiss: () -> Unit,
    onGameCreated: (Game) -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var gameName by remember { mutableStateOf("") }
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
                                0 -> if (gameName.isNotBlank()) currentStep = 1
                                1 -> if (selectedTheme != null) currentStep = 2
                                2 -> if (selectedPlatforms.isNotEmpty() && selectedBusinessModel != null) currentStep = 3
                                3 -> {
                                    // 创建游戏
                                    val newGame = Game(
                                        id = java.util.UUID.randomUUID().toString(),
                                        name = gameName,
                                        theme = selectedTheme!!,
                                        platforms = selectedPlatforms.toList(),
                                        businessModel = selectedBusinessModel!!
                                    )
                                    onGameCreated(newGame)
                                }
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
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text(
                            text = when (currentStep) {
                                3 -> "开始开发"
                                else -> "下一步"
                            },
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
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
    Column {
        Text(
            text = "选择游戏主题：",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(GameTheme.values()) { theme ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeSelected(theme) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedTheme == theme) {
                            Color(0xFF10B981).copy(alpha = 0.3f)
                        } else {
                            Color.White.copy(alpha = 0.1f)
                        }
                    ),
                    border = if (selectedTheme == theme) {
                        androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFF10B981)
                        )
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = theme.icon,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = theme.displayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
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
            text = "选择发布平台：",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(Platform.values()) { platform ->
                Card(
                    modifier = Modifier.clickable { onPlatformToggle(platform) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPlatforms.contains(platform)) {
                            Color(0xFF10B981).copy(alpha = 0.3f)
                        } else {
                            Color.White.copy(alpha = 0.1f)
                        }
                    ),
                    border = if (selectedPlatforms.contains(platform)) {
                        androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFF10B981)
                        )
                    } else null
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = platform.icon,
                            fontSize = 20.sp
                        )
                        Text(
                            text = platform.displayName,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        
        Text(
            text = "选择商业模式：",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(BusinessModel.values()) { model ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBusinessModelSelected(model) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedBusinessModel == model) {
                            Color(0xFF10B981).copy(alpha = 0.3f)
                        } else {
                            Color.White.copy(alpha = 0.1f)
                        }
                    ),
                    border = if (selectedBusinessModel == model) {
                        androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFF10B981)
                        )
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = model.icon,
                            fontSize = 16.sp
                        )
                        Text(
                            text = model.displayName,
                            color = Color.White,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
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
            text = "确认游戏信息：",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "游戏名称：",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = gameName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "游戏主题：",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Row {
                        Text(
                            text = theme?.icon ?: "",
                            fontSize = 14.sp
                        )
                        Text(
                            text = theme?.displayName ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "发布平台：",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = platforms.joinToString(", ") { it.displayName },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "商业模式：",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Row {
                        Text(
                            text = businessModel?.icon ?: "",
                            fontSize = 14.sp
                        )
                        Text(
                            text = businessModel?.displayName ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "点击开始开发将创建新项目并开始开发流程。",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MarketAnalysisContent() {
    val competitorGenerator = remember { CompetitorDataGenerator() }
    val trendGenerator = remember { GameThemeTrendGenerator() }
    val layoutConfig = getLayoutConfig()
    
    var competitors by remember { mutableStateOf(competitorGenerator.generateCompetitors()) }
    var themeTrends by remember { mutableStateOf(trendGenerator.generateGameThemeTrends()) }
    
    // 数据更新动画状态
    var isUpdating by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isUpdating) 0.7f else 1f,
        animationSpec = ModernAnimationSystem.fadeInOut
    )
    
    // 渐变背景动画
    val infiniteTransition = rememberInfiniteTransition()
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // 启动自动更新
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L) // 30秒间隔
            
            isUpdating = true
            delay(300) // 动画时间
            
            // 增量更新竞争对手数据
            competitors = competitors.map { competitor ->
                competitorGenerator.updateCompetitorData(competitor)
            }
            
            // 增量更新主题趋势
            themeTrends = themeTrends.map { trend ->
                trendGenerator.updateThemeTrend(trend)
            }.sortedByDescending { it.hotIndex }
            
            isUpdating = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = ModernColorSystem.primaryGradient,
                    start = Offset(0f, gradientOffset * 1000f),
                    end = Offset(1000f, (1f - gradientOffset) * 1000f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(layoutConfig.contentPadding.dp)
                .alpha(animatedAlpha)
                .verticalScroll(rememberScrollState())
        ) {
            // 现代化标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = ModernColorSystem.glassBackground,
                    border = BorderStroke(1.dp, ModernColorSystem.glassStroke)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 24.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "市场分析",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernColorSystem.primaryText
                    )
                    Text(
                        text = "实时市场数据与趋势分析",
                        fontSize = 14.sp,
                        color = ModernColorSystem.secondaryText
                    )
                }
            }
            
            // 竞争对手分析区域
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = layoutConfig.cardSpacing.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏢",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "竞争对手分析",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernColorSystem.primaryText
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    DataChip(
                        text = "${competitors.size} 家公司",
                        icon = "🏭",
                        color = ModernColorSystem.infoColor
                    )
                }
                
                competitors.forEach { competitor ->
                    CompetitorCard(competitor = competitor)
                    Spacer(modifier = Modifier.height(layoutConfig.itemSpacing.dp))
                }
            }
            
            // 游戏主题趋势区域
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "游戏主题趋势",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernColorSystem.primaryText
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    DataChip(
                        text = "${themeTrends.size} 个主题",
                        icon = "🎯",
                        color = ModernColorSystem.accentGradient[0]
                    )
                }
                
                themeTrends.forEach { trend ->
                    GameThemeTrendCard(trend = trend)
                    Spacer(modifier = Modifier.height(layoutConfig.itemSpacing.dp))
                }
            }
        }
    }
}

@Composable
fun CompetitorCard(competitor: Competitor) {
    val layoutConfig = getLayoutConfig()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val animatedElevation by animateFloatAsState(
        targetValue = if (isHovered) 12f else 6f,
        animationSpec = ModernAnimationSystem.fadeInOut
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = ModernAnimationSystem.scaleInOut
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .hoverable(interactionSource),
        colors = CardDefaults.cardColors(
            containerColor = ModernColorSystem.glassBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = ModernColorSystem.glassStroke
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(layoutConfig.cardPadding.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 公司图标背景
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = ModernColorSystem.secondaryGradient[0].copy(alpha = 0.2f),
                border = BorderStroke(1.dp, ModernColorSystem.secondaryGradient[0].copy(alpha = 0.3f))
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = competitor.icon,
                        fontSize = 28.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = competitor.name,
                    fontSize = layoutConfig.titleFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernColorSystem.primaryText
                )
                
                // 数据芯片行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataChip(
                        text = "${formatMoney(competitor.annualRevenue)}万",
                        icon = "💰",
                        color = ModernColorSystem.successColor,
                        modifier = Modifier.weight(1f)
                    )
                    DataChip(
                        text = "${competitor.fanCount}万",
                        icon = "👥",
                        color = ModernColorSystem.infoColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 市值显示
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ModernColorSystem.accentGradient[1].copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, ModernColorSystem.accentGradient[1].copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📈",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "市值: ${formatMoney(competitor.marketValue)}万",
                            fontSize = 14.sp,
                            color = ModernColorSystem.primaryText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameThemeTrendCard(trend: GameThemeTrend) {
    val layoutConfig = getLayoutConfig()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val animatedElevation by animateFloatAsState(
        targetValue = if (isHovered) 10f else 4f,
        animationSpec = ModernAnimationSystem.fadeInOut
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.01f else 1f,
        animationSpec = ModernAnimationSystem.scaleInOut
    )
    
    // 根据趋势方向选择颜色
    val trendColor = when (trend.trend) {
        TrendDirection.UP -> ModernColorSystem.successColor
        TrendDirection.DOWN -> ModernColorSystem.errorColor
        TrendDirection.STABLE -> ModernColorSystem.warningColor
    }
    
    val trendGradient = when (trend.trend) {
        TrendDirection.UP -> listOf(
            ModernColorSystem.successColor.copy(alpha = 0.2f),
            ModernColorSystem.successColor.copy(alpha = 0.1f)
        )
        TrendDirection.DOWN -> listOf(
            ModernColorSystem.errorColor.copy(alpha = 0.2f),
            ModernColorSystem.errorColor.copy(alpha = 0.1f)
        )
        TrendDirection.STABLE -> listOf(
            ModernColorSystem.warningColor.copy(alpha = 0.2f),
            ModernColorSystem.warningColor.copy(alpha = 0.1f)
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .hoverable(interactionSource),
        colors = CardDefaults.cardColors(
            containerColor = ModernColorSystem.glassBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = trendColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = trendGradient
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(layoutConfig.cardPadding.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 主题图标背景
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = trendColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, trendColor.copy(alpha = 0.4f))
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trend.icon,
                            fontSize = 26.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trend.theme,
                            fontSize = layoutConfig.titleFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernColorSystem.primaryText
                        )
                        
                        TrendIndicator(
                            direction = trend.trend
                        )
                    }
                    
                    // 数据指标行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 热度指标
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = ModernColorSystem.errorColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ModernColorSystem.errorColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = String.format("%.1f", trend.hotIndex),
                                    fontSize = 13.sp,
                                    color = ModernColorSystem.primaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // 占有率指标
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = ModernColorSystem.infoColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ModernColorSystem.infoColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "📊",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${String.format("%.1f", trend.marketShare)}%",
                                    fontSize = 13.sp,
                                    color = ModernColorSystem.primaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
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
    games: List<Game> = emptyList()
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
                                    val saveData = SaveData(
                                        companyName = companyName,
                                        founderName = founderName,
                                        money = money,
                                        fans = fans,
                                        currentYear = currentYear,
                                        currentMonth = currentMonth,
                                        currentDay = currentDay,
                                        games = games,
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
fun RecruitmentCenterContent(
    candidateManager: CandidateManager,
    onBack: () -> Unit = {},
    onHireCandidate: (Candidate, CandidateManager) -> Unit = { _, _ -> }
) {
    val candidates = candidateManager.candidates
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 返回按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "招聘中心",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 招聘中心内容
        RecruitmentCenter(
            candidates = candidates,
            onHireCandidate = { candidate ->
                // 调用传入的回调函数，处理招聘逻辑
                onHireCandidate(candidate, candidateManager)
            },
            onRefreshCandidates = {
                // 计算当前可用候选人数量，如果少于5个则生成足够的候选人使总数达到5个
                val currentAvailableCount = candidateManager.getAvailableCandidatesCount()
                val maxCandidates = 5
                val needToGenerate = maxOf(0, maxCandidates - currentAvailableCount)
                repeat(needToGenerate) {
                    candidateManager.addCandidate(candidateManager.generateRandomCandidate())
                }
            },
            currentMoney = 100000 // TODO: 从游戏状态获取实际资金
        )
    }
}

@Composable
fun PrivacyPolicyDialog(onAgree: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) }
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
                    
                    ClickableText(
                        text = introText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF374151)),
                        modifier = Modifier.padding(bottom = 12.dp),
                        onClick = { offset ->
                            introText.getStringAnnotations(tag = "user_agreement", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                    context.startActivity(intent)
                                }
                            introText.getStringAnnotations(tag = "privacy_policy", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                    context.startActivity(intent)
                                }
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
                        text = "我们不会收集与游戏无关或强制用户开启的个人信息。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151)
                    )
                }
                
                // 复选框区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF10B981),
                            uncheckedColor = Color(0xFF9CA3AF)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 带链接的复选框文本
                    val checkboxText = buildAnnotatedString {
                        append("我已阅读并同意")
                        pushStringAnnotation(tag = "user_agreement", annotation = "https://share.note.youdao.com/s/FUdL4QRe")
                        withStyle(style = SpanStyle(color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)) {
                            append("《用户协议》")
                        }
                        pop()
                        append("与")
                        pushStringAnnotation(tag = "privacy_policy", annotation = "https://share.note.youdao.com/s/KjmsBvUB")
                        withStyle(style = SpanStyle(color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)) {
                            append("《隐私政策》")
                        }
                        pop()
                    }
                    
                    ClickableText(
                        text = checkboxText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF374151)),
                        onClick = { offset ->
                            checkboxText.getStringAnnotations(tag = "user_agreement", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                    context.startActivity(intent)
                                }
                            checkboxText.getStringAnnotations(tag = "privacy_policy", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                    context.startActivity(intent)
                                }
                        }
                    )
                }
                
                // 确认按钮
                Button(
                    onClick = onAgree,
                    enabled = isChecked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isChecked) Color(0xFF10B981) else Color(0xFF9CA3AF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "进入游戏",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// 优化版速度按钮组件 - 32dp尺寸，更大的点击区域
@Composable
fun EnhancedSpeedButton(
    speed: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp) // 48dp最小点击区域
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp) // 32dp视觉尺寸
                .background(
                    color = if (isSelected) Color(0xFF10B981) else Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(6.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFF10B981) else Color(0xFF6B7280),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(2.dp), // 新增：内边距确保文本不贴边
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${speed}x",
                color = if (isSelected) Color.White else Color(0xFF374151),
                fontSize = 14.sp, // 14sp字体
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1 // 限制单行
            )
        }
    }
}

// 优化版暂停按钮组件 - 32dp尺寸，更大的点击区域
@Composable
fun EnhancedPauseButton(
    isPaused: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp) // 48dp最小点击区域
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp) // 32dp视觉尺寸
                .background(
                    color = if (isPaused) Color(0xFFEF4444) else Color(0xFF10B981),
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPaused) "▶" else "⏸",
                color = Color.White,
                fontSize = 14.sp, // 14sp字体
                fontWeight = FontWeight.Medium
            )
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