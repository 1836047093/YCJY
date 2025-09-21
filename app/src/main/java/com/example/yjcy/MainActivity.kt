package com.example.yjcy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
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
import com.example.yjcy.ui.theme.YjcyTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.google.gson.Gson
import com.example.yjcy.ui.RecruitmentCenter
import com.example.yjcy.data.CandidateManager
import com.example.yjcy.ui.EmployeeManagementEnhanced
import com.example.yjcy.ui.ProjectManagementWrapper
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



// 全局变量存储当前加载的存档数据
var currentLoadedSaveData: SaveData? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        
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
                    composable("game/{companyName}/{founderName}/{selectedLogo}") { backStackEntry ->
                        val companyName = backStackEntry.arguments?.getString("companyName") ?: "我的游戏公司"
                        val founderName = backStackEntry.arguments?.getString("founderName") ?: "创始人"
                        val selectedLogo = backStackEntry.arguments?.getString("selectedLogo") ?: "🎮"
                        GameScreen(navController, companyName, founderName, selectedLogo, currentLoadedSaveData)
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
                }
            }
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
    // 创建渐变背景
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E3A8A), // 深蓝色
            Color(0xFF7C3AED)  // 紫色
        )
    )
    
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
            .background(gradientBrush)
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
                        Color(0xFF1E3A8A),
                        Color(0xFF7C3AED)
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
                        if (companyName.isNotEmpty() && founderName.isNotEmpty() && isCompanyNameValid) {
                            navController.navigate("game/$companyName/$founderName/$selectedLogo")
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
    saveData: SaveData? = null
) {
    // 游戏状态数据 - 如果有存档数据则使用存档数据，否则使用默认值
    var money by remember { mutableStateOf(saveData?.money ?: 10000L) }
    var fans by remember { mutableStateOf(saveData?.fans ?: 0) }
    var currentYear by remember { mutableStateOf(saveData?.currentYear ?: 1) }
    var currentMonth by remember { mutableStateOf(saveData?.currentMonth ?: 1) }
    var currentDay by remember { mutableStateOf(saveData?.currentDay ?: 1) }
    var gameSpeed by remember { mutableStateOf(1) }
    var selectedTab by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf(saveData?.companyName ?: initialCompanyName) }
    var founderName by remember { mutableStateOf(saveData?.founderName ?: initialFounderName) }
    var games by remember { mutableStateOf(saveData?.games ?: emptyList<Game>()) }
    var showRecruitmentCenter by remember { mutableStateOf(false) }
    
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
                        onBack = { showRecruitmentCenter = false }
                    )
                } else {
                    when (selectedTab) {
                        0 -> CompanyOverviewContent(companyName = companyName)
                        1 -> EmployeeManagementContent(
                            onNavigateToRecruitment = { showRecruitmentCenter = true }
                        )
                        2 -> ProjectManagementWrapper(
                            games = games,
                            onGamesUpdate = { updatedGames -> games = updatedGames }
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
            modifier = Modifier.weight(1.2f),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    text = "¥${String.format("%,d", money)}",
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
                        text = String.format("%,d", fans),
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
fun CompanyOverviewContent(companyName: String = "我的游戏公司") {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 公司基本信息
            CompanyInfoCard(
                title = "公司信息",
                items = listOf(
                    "公司名称" to companyName,
                    "公司等级" to "Lv.1",
                    "声誉值" to "0",
                    "成立时间" to "第1年1月1日"
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 员工信息
            CompanyInfoCard(
                title = "团队状况",
                items = listOf(
                    "员工总数" to "3人",
                    "程序员" to "1人",
                    "美术师" to "1人",
                    "策划师" to "1人"
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 项目信息
            CompanyInfoCard(
                title = "项目状况",
                items = listOf(
                    "进行中项目" to "0个",
                    "已完成项目" to "0个",
                    "总收益" to "¥0",
                    "平均评分" to "暂无"
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
                text = title,
                color = Color(0xFFF59E0B), // 橙色强调色
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
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
    onNavigateToRecruitment: () -> Unit = {}
) {
    // 员工数据状态
    val employees = remember {
        mutableStateListOf(
            Employee(1, "张程序", "程序员", 3, 1, 1, 1, 1, 8000),
            Employee(2, "李美术", "美术师", 1, 1, 3, 1, 1, 7000),
            Employee(3, "王策划", "策划师", 1, 3, 1, 1, 1, 6000),
            Employee(4, "赵音效", "音效师", 1, 1, 1, 3, 1, 6500),
            Employee(5, "钱客服", "客服", 1, 1, 1, 1, 3, 5000)
        )
    }
    
    // 使用增强版员工管理界面
    EmployeeManagementEnhanced(
        employees = employees,
        onTrainEmployee = { employee, skillType ->
            // 执行培训逻辑
            val index = employees.indexOfFirst { it.id == employee.id }
            if (index != -1) {
                val updatedEmployee = when (skillType) {
                    "开发" -> employee.copy(skillDevelopment = minOf(5, employee.skillDevelopment + 1))
                    "策划" -> employee.copy(skillDesign = minOf(5, employee.skillDesign + 1))
                    "美术" -> employee.copy(skillArt = minOf(5, employee.skillArt + 1))
                    "音效" -> employee.copy(skillMusic = minOf(5, employee.skillMusic + 1))
                    "客服" -> employee.copy(skillService = minOf(5, employee.skillService + 1))
                    else -> employee
                }
                employees[index] = updatedEmployee
            }
        },
        onDismissEmployee = { employee ->
            employees.removeAll { it.id == employee.id }
        },
        onNavigateToRecruitment = onNavigateToRecruitment
    )
}

// 员工数据类
data class Employee(
    val id: Int,
    val name: String,
    val position: String,
    val skillDevelopment: Int,
    val skillDesign: Int,
    val skillArt: Int,
    val skillMusic: Int,
    val skillService: Int,
    val salary: Int
) {
    // 获取员工的专属技能类型
    fun getSpecialtySkillType(): String {
        return when (position) {
            "程序员" -> "开发"
            "策划师" -> "设计"
            "美术师" -> "美工"
            "音效师" -> "音乐"
            "客服" -> "服务"
            else -> "开发"
        }
    }
    
    // 获取员工的专属技能等级
    fun getSpecialtySkillLevel(): Int {
        return when (position) {
            "程序员" -> skillDevelopment
            "策划师" -> skillDesign
            "美术师" -> skillArt
            "音效师" -> skillMusic
            "客服" -> skillService
            else -> skillDevelopment
        }
    }
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
                        text = "¥${String.format("%,d", employees.sumOf { it.salary })}",
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
                    text = "¥${String.format("%,d", employee.salary)}/月",
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
                    text = "培训费用：¥${String.format("%,d", trainingCost)}",
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
                    text = "月薪：¥${String.format("%,d", employee.salary)}",
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
                        navController.navigate("game/${saveData.companyName}/${saveData.founderName}/🎮")
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
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
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
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
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

// 游戏相关数据类
data class Game(
    val id: String,
    val name: String,
    val theme: GameTheme,
    val platforms: List<Platform>,
    val businessModel: BusinessModel,
    val developmentProgress: Float = 0f,
    val isCompleted: Boolean = false,
    val revenue: Long = 0L
)

enum class GameTheme(val displayName: String, val icon: String) {
    ACTION("动作", "⚔️"),
    ADVENTURE("冒险", "🗺️"),
    RPG("角色扮演", "🧙"),
    STRATEGY("策略", "♟️"),
    SIMULATION("模拟", "🏗️"),
    PUZZLE("益智", "🧩"),
    RACING("竞速", "🏎️"),
    SPORTS("体育", "⚽"),
    HORROR("恐怖", "👻"),
    CASUAL("休闲", "🎲")
}

enum class Platform(val displayName: String, val icon: String) {
    PC("PC", "💻"),
    MOBILE("手机", "📱"),
    CONSOLE("主机", "🎮"),
    WEB("网页", "🌐")
}

enum class BusinessModel(val displayName: String, val icon: String) {
    SINGLE_PLAYER("单机游戏", "🎮"),
    ONLINE_GAME("网络游戏", "🌐")
}

// 存档数据类
data class SaveData(
    val companyName: String = "我的游戏公司",
    val founderName: String = "创始人",
    val money: Long = 10000L,
    val fans: Int = 0,
    val currentYear: Int = 1,
    val currentMonth: Int = 1,
    val currentDay: Int = 1,
    val saveTime: Long = System.currentTimeMillis(),
    val games: List<Game> = emptyList()
)

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
            text = "请输入您要开发的游戏名称：",
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📊 市场分析",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
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
                Text(
                    text = "市场趋势分析功能即将推出",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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
                                        text = "资金: ¥${existingSave.money} | 粉丝: ${existingSave.fans}",
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
fun RecruitmentCenterContent(onBack: () -> Unit = {}) {
    val candidateManager = remember { CandidateManager() }
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
                // TODO: 实现招聘逻辑，将候选人转换为员工
                candidateManager.updateCandidateStatus(candidate.id, com.example.yjcy.data.AvailabilityStatus.HIRED)
            },
            onRefreshCandidates = {
                // 生成新的候选人
                repeat(3) {
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

// 游戏速度下拉选择组件
@Composable
fun GameSpeedDropdown(
    currentSpeed: Int,
    isPaused: Boolean,
    onSpeedChange: (Int) -> Unit,
    onPauseToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 游戏速度下拉选择
        Box {
            // 下拉按钮
            Row(
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡",
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (isPaused) "暂停" else "${currentSpeed}x",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (expanded) "▲" else "▼",
                    color = Color.White,
                    fontSize = 8.sp
                )
            }
            
            // 下拉菜单
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(
                    color = Color(0xFF1F2937),
                    shape = RoundedCornerShape(6.dp)
                )
            ) {
                // 暂停选项
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏸",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "暂停",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    },
                    onClick = {
                        onPauseToggle()
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (isPaused) Color(0xFF374151) else Color.Transparent
                    )
                )
                
                // 速度选项
                listOf(1, 2, 3).forEach { speed ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${speed}x速度",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        onClick = {
                            onSpeedChange(speed)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (currentSpeed == speed && !isPaused) Color(0xFF374151) else Color.Transparent
                        )
                    )
                }
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