package com.example.yjcy



import android.os.Bundle
import android.util.Log
import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*




import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment



import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape






import com.example.yjcy.ui.theme.YjcyTheme

import com.example.yjcy.data.SaveData
import com.example.yjcy.data.Game





import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset



import com.example.yjcy.data.Founder
import com.google.gson.Gson
import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import kotlin.math.sin
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import kotlin.random.Random

import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.widget.Toast
import androidx.compose.ui.window.Dialog
import java.util.Locale
import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.hoverable
import androidx.compose.animation.core.LinearEasing
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.alpha
import com.example.yjcy.data.Employee
import com.example.yjcy.data.FounderProfession


import androidx.compose.ui.draw.clip
import com.example.yjcy.ui.ProjectManagementWrapper
import com.example.yjcy.ui.EmployeeManagementContent
import dagger.hilt.android.AndroidEntryPoint








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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "MainActivity onCreate 开始")
        
        // 增强全屏显示设置
        // enableFullScreenDisplay()  // 临时注释掉以解决闪退问题
        
        enableEdgeToEdge()
        setContent {
            YjcyTheme {
                val navController = rememberNavController()
                
                // SharedPreferences for privacy policy agreement
                val sharedPreferences = getSharedPreferences("privacy_settings", MODE_PRIVATE)
                var showPrivacyDialog by remember { mutableStateOf(!sharedPreferences.getBoolean("privacy_agreed", false)) }
                
                // Privacy Policy Dialog
                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(
                        onAgree = {
                            sharedPreferences.edit().apply {
                                putBoolean("privacy_agreed", true)
                                apply()
                            }
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
    // 游戏状态数据 - 如果有存档数据则使用存档数据，否则使用默认值
    var money by remember { mutableLongStateOf(saveData?.money ?: 1000000L) }
    var fans by remember { mutableIntStateOf(saveData?.fans ?: 0) }
    var currentYear by remember { mutableIntStateOf(saveData?.currentYear ?: 1) }
    var currentMonth by remember { mutableIntStateOf(saveData?.currentMonth ?: 1) }
    var currentDay by remember { mutableIntStateOf(saveData?.currentDay ?: 1) }
    var gameSpeed by remember { mutableIntStateOf(1) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf(saveData?.companyName ?: initialCompanyName) }
    var founderName by remember { mutableStateOf(saveData?.founderName ?: initialFounderName) }
    var founderProfession by remember { mutableStateOf(saveData?.founderProfession ?: try { FounderProfession.valueOf(initialFounderProfession) } catch (_: IllegalArgumentException) { FounderProfession.PROGRAMMER }) }
    var games by remember { mutableStateOf(saveData?.games ?: emptyList()) }
    
    // 消息状态
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    
    // 员工状态管理 - 提升到GameScreen级别
    val allEmployees = remember { mutableStateListOf<Employee>() }
    
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
    
    // 员工管理回调函数
    val onTrainEmployee: (Employee, String) -> Unit = { employee, skillType ->
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
    }
    
    val onDismissEmployee: (Employee) -> Unit = { employee ->
        // 创始人不能被解雇
        if (employee.id != 0) {
            allEmployees.removeAll { it.id == employee.id }
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
                            allEmployees = allEmployees
                        )
                        1 -> EmployeeManagementContent(
                            allEmployees = allEmployees,
                            onEmployeesUpdate = { updatedEmployees -> 
                                allEmployees.clear()
                                allEmployees.addAll(updatedEmployees)
                            },
                            money = money,
                            onMoneyUpdate = { updatedMoney -> money = updatedMoney }
                        )
                        2 -> ProjectManagementWrapper(
                            games = games,
                            onGamesUpdate = { updatedGames -> games = updatedGames },
                            founder = founder,
                            allEmployees = allEmployees
                        )
                        3 -> InGameSettingsContent(
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
                icon = "⚙️",
                label = "设置",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) }
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
    
    // 文本颜色
    val primaryText = Color.White
    val secondaryText = Color.White.copy(alpha = 0.8f)
    
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
    
    // 缓动函数
    val fastOutSlowIn = FastOutSlowInEasing
    
    // 常用动画规格
    val fadeInOut = tween<Float>(NORMAL_ANIMATION, easing = fastOutSlowIn)
    val scaleInOut = tween<Float>(FAST_ANIMATION, easing = fastOutSlowIn)
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
    modifier: Modifier = Modifier,
    icon: String? = null,
    color: Color = ModernColorSystem.infoColor
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



// 响应式布局系统
object ResponsiveLayoutSystem {
    // 屏幕断点
    
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
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> ResponsiveLayoutSystem.LayoutConfig(
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
        WindowWidthSizeClass.Medium -> ResponsiveLayoutSystem.LayoutConfig(
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
        WindowWidthSizeClass.Expanded -> ResponsiveLayoutSystem.LayoutConfig(
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





// 存档管理类
class SaveManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_saves", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveGame(slotIndex: Int, saveData: SaveData) {
        val json = gson.toJson(saveData)
        sharedPreferences.edit().apply {
            putString("save_slot_$slotIndex", json)
            apply()
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
        sharedPreferences.edit().apply {
            remove("save_slot_$slotIndex")
            apply()
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
                    
                    Text(
                        text = checkboxText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF374151)),
                        modifier = Modifier.clickable {
                            // Handle privacy policy link
                            val intent = Intent(Intent.ACTION_VIEW, "https://share.note.youdao.com/s/KjmsBvUB".toUri())
                            context.startActivity(intent)
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