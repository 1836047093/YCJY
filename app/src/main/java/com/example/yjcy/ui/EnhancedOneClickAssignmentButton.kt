package com.example.yjcy.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import com.example.yjcy.data.EnhancedAssignmentService
import com.example.yjcy.data.EnhancedAssignmentResult
import com.example.yjcy.data.SkillMatchingEngine
import com.example.yjcy.data.RankingStrategy
import com.example.yjcy.data.Employee
import com.example.yjcy.data.Game

/**
 * 增强版一键分配最佳员工按钮
 * 集成新的智能分配算法
 */
@Composable
fun EnhancedOneClickAssignmentButton(
    projects: List<Game>,
    employees: List<Employee>,
    onAssignmentComplete: (EnhancedAssignmentResult) -> Unit,
    modifier: Modifier = Modifier,
    text: String = "智能一键分配",
    enabled: Boolean = true,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    currentMinuteOfDay: Int = 0, // 当天内的分钟数（0-1439）
    onPauseGame: (() -> Unit)? = null,
    onResumeGame: (() -> Unit)? = null
) {
    var buttonState by remember { mutableStateOf(EnhancedButtonState.NORMAL) }
    var showResultDialog by remember { mutableStateOf(false) }
    var assignmentResult by remember { mutableStateOf<EnhancedAssignmentResult?>(null) }
    
    val context = LocalContext.current
    val assignmentService = remember { EnhancedAssignmentService() }
    val coroutineScope = rememberCoroutineScope()
    
    // 监听对话框打开/关闭，控制游戏暂停
    LaunchedEffect(showResultDialog) {
        if (showResultDialog) {
            // 打开结果对话框时暂停游戏
            onPauseGame?.invoke()
        } else {
            // 关闭结果对话框时恢复游戏
            onResumeGame?.invoke()
        }
    }
    
    // 自动重置状态
    LaunchedEffect(buttonState) {
        if (buttonState == EnhancedButtonState.SUCCESS || buttonState == EnhancedButtonState.ERROR) {
            delay(2000)
            buttonState = EnhancedButtonState.NORMAL
        }
    }
    
    // 执行分配（包含暂停功能）
    suspend fun performAssignment() {
        try {
            // 开始分配时暂停游戏
            onPauseGame?.invoke()
            
            buttonState = EnhancedButtonState.LOADING
            
            // 模拟分析过程
            delay(500)
            
            // 执行智能分配
            val result = assignmentService.assignBestEmployeesToProjects(
                projects = projects,
                availableEmployees = employees
            )
            
            assignmentResult = result
            buttonState = EnhancedButtonState.SUCCESS
            
            // 显示结果对话框（保持暂停状态）
            delay(500)
            showResultDialog = true
            
        } catch (e: Exception) {
            buttonState = EnhancedButtonState.ERROR
            // 出错时恢复游戏
            onResumeGame?.invoke()
        }
    }
    
    Box(modifier = modifier) {
        Button(
            onClick = {
                if (buttonState == EnhancedButtonState.NORMAL && enabled) {
                    coroutineScope.launch {
                        performAssignment()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled && buttonState != EnhancedButtonState.LOADING,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = getEnhancedButtonGradient(buttonState),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = buttonState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with
                                fadeOut(animationSpec = tween(300))
                    }
                ) { state ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when (state) {
                            EnhancedButtonState.LOADING -> {
                                EnhancedLoadingIndicator()
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "智能分析中...",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            EnhancedButtonState.SUCCESS -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "分配完成",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            EnhancedButtonState.ERROR -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "分配失败",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = text,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 显示分配结果对话框
    if (showResultDialog && assignmentResult != null) {
        EnhancedAssignmentResultDialog(
            assignmentResult = assignmentResult!!,
            projectNames = projects.associate { it.id to it.name },
            games = projects,
            onDismiss = {
                showResultDialog = false
                buttonState = EnhancedButtonState.NORMAL
            },
            onConfirm = {
                showResultDialog = false
                onAssignmentComplete(assignmentResult!!)
                buttonState = EnhancedButtonState.NORMAL
            },
            currentYear = currentYear,
            currentMonth = currentMonth,
            currentDay = currentDay,
            currentMinuteOfDay = currentMinuteOfDay,
            onPauseGame = onPauseGame,
            onResumeGame = onResumeGame
        )
    }
}

/**
 * 增强版加载指示器
 */
@Composable
private fun EnhancedLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
    )
}

/**
 * 获取增强按钮渐变色
 */
private fun getEnhancedButtonGradient(state: EnhancedButtonState): Brush {
    return when (state) {
        EnhancedButtonState.NORMAL -> Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF6366F1), // Indigo
                Color(0xFF8B5CF6)  // Purple
            )
        )
        EnhancedButtonState.LOADING -> Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF3B82F6), // Blue
                Color(0xFF1D4ED8)  // Blue-700
            )
        )
        EnhancedButtonState.SUCCESS -> Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF10B981), // Emerald
                Color(0xFF059669)  // Emerald-600
            )
        )
        EnhancedButtonState.ERROR -> Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFEF4444), // Red
                Color(0xFFDC2626)  // Red-600
            )
        )
    }
}

/**
 * 增强按钮状态枚举
 */
enum class EnhancedButtonState {
    NORMAL,   // 正常状态
    LOADING,  // 加载中
    SUCCESS,  // 成功
    ERROR     // 错误
}

/**
 * 紧凑版增强一键分配按钮
 */
@Composable
fun CompactEnhancedAssignmentButton(
    projects: List<Game>,
    availableEmployees: List<Employee>,
    onAssignmentComplete: (EnhancedAssignmentResult) -> Unit,
    modifier: Modifier = Modifier,
    currentYear: Int = 1,
    currentMonth: Int = 1,
    currentDay: Int = 1,
    currentMinuteOfDay: Int = 0, // 当天内的分钟数（0-1439）
    onPauseGame: (() -> Unit)? = null,
    onResumeGame: (() -> Unit)? = null
) {
    var buttonState by remember { mutableStateOf(EnhancedButtonState.NORMAL) }
    var assignmentResult by remember { mutableStateOf<EnhancedAssignmentResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    
    val assignmentService = remember { EnhancedAssignmentService() }
    val coroutineScope = rememberCoroutineScope()
    
    // 监听对话框打开/关闭，控制游戏暂停
    LaunchedEffect(showResultDialog) {
        if (showResultDialog) {
            onPauseGame?.invoke()
        } else {
            onResumeGame?.invoke()
        }
    }
    
    // 执行分配（包含暂停功能）
    suspend fun performAssignment() {
        try {
            // 开始分配时暂停游戏
            onPauseGame?.invoke()
            
            buttonState = EnhancedButtonState.LOADING
            delay(500)
            
            val result = assignmentService.assignBestEmployeesToProjects(
                projects = projects,
                availableEmployees = availableEmployees
            )
            
            assignmentResult = result
            buttonState = EnhancedButtonState.SUCCESS
            delay(500)
            showResultDialog = true
            
        } catch (e: Exception) {
            buttonState = EnhancedButtonState.ERROR
            // 出错时恢复游戏
            onResumeGame?.invoke()
        }
    }
    
    IconButton(
        onClick = {
            if (buttonState == EnhancedButtonState.NORMAL) {
                kotlinx.coroutines.GlobalScope.launch {
                    performAssignment()
                }
            }
        },
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = getEnhancedButtonGradient(buttonState)
            )
    ) {
        when (buttonState) {
            EnhancedButtonState.LOADING -> {
                EnhancedLoadingIndicator()
            }
            EnhancedButtonState.SUCCESS -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            EnhancedButtonState.ERROR -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
    
    // 显示分配结果对话框
    if (showResultDialog && assignmentResult != null) {
        EnhancedAssignmentResultDialog(
            assignmentResult = assignmentResult!!,
            projectNames = projects.associate { it.id to it.name },
            games = projects,
            onDismiss = {
                showResultDialog = false
                buttonState = EnhancedButtonState.NORMAL
            },
            onConfirm = {
                showResultDialog = false
                onAssignmentComplete(assignmentResult!!)
                buttonState = EnhancedButtonState.NORMAL
            },
            currentYear = currentYear,
            currentMonth = currentMonth,
            currentDay = currentDay,
            currentMinuteOfDay = currentMinuteOfDay,
            onPauseGame = onPauseGame,
            onResumeGame = onResumeGame
        )
    }
}