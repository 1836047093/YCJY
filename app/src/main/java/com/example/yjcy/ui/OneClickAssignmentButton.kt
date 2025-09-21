package com.example.yjcy.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 一键分配按钮组件
 * 支持多种状态显示和动画效果
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneClickAssignmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    text: String = "一键智能分配",
    loadingText: String = "分配中...",
    successText: String = "分配完成",
    errorText: String = "分配失败"
) {
    val buttonState = when {
        isError -> ButtonState.Error
        isSuccess -> ButtonState.Success
        isLoading -> ButtonState.Loading
        else -> ButtonState.Normal
    }
    
    val buttonColors = getButtonColors(buttonState)
    val buttonText = getButtonText(buttonState, text, loadingText, successText, errorText)
    val buttonIcon = getButtonIcon(buttonState)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled && buttonState == ButtonState.Normal) 8.dp else 4.dp
        )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = buttonColors
                    )
                ),
            enabled = enabled && buttonState != ButtonState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            AnimatedContent(
                targetState = buttonState,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "button_content"
            ) { state ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 图标动画
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        if (state == ButtonState.Loading) {
                            LoadingIndicator()
                        } else {
                            Icon(
                                imageVector = buttonIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 文本动画
                    AnimatedContent(
                        targetState = buttonText,
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() with
                                    slideOutVertically { height -> -height } + fadeOut()
                        },
                        label = "button_text"
                    ) { text ->
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
    
    // 自动重置成功/错误状态
    LaunchedEffect(isSuccess, isError) {
        if (isSuccess || isError) {
            delay(2000) // 2秒后自动重置
        }
    }
}

/**
 * 加载指示器组件
 */
@Composable
private fun LoadingIndicator() {
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            rotation += 360f
            delay(1000)
        }
    }
    
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer(rotationZ = rotation)
    )
}

/**
 * 按钮状态枚举
 */
private enum class ButtonState {
    Normal, Loading, Success, Error
}

/**
 * 获取按钮颜色
 */
private fun getButtonColors(state: ButtonState): List<Color> {
    return when (state) {
        ButtonState.Normal -> listOf(
            Color(0xFF6366F1), // Indigo-500
            Color(0xFF8B5CF6)  // Violet-500
        )
        ButtonState.Loading -> listOf(
            Color(0xFF3B82F6), // Blue-500
            Color(0xFF1D4ED8)  // Blue-700
        )
        ButtonState.Success -> listOf(
            Color(0xFF10B981), // Emerald-500
            Color(0xFF059669)  // Emerald-600
        )
        ButtonState.Error -> listOf(
            Color(0xFFEF4444), // Red-500
            Color(0xFFDC2626)  // Red-600
        )
    }
}

/**
 * 获取按钮文本
 */
private fun getButtonText(
    state: ButtonState,
    normalText: String,
    loadingText: String,
    successText: String,
    errorText: String
): String {
    return when (state) {
        ButtonState.Normal -> normalText
        ButtonState.Loading -> loadingText
        ButtonState.Success -> successText
        ButtonState.Error -> errorText
    }
}

/**
 * 获取按钮图标
 */
private fun getButtonIcon(state: ButtonState): ImageVector {
    return when (state) {
        ButtonState.Normal -> Icons.Default.Check
        ButtonState.Loading -> Icons.Default.Check
        ButtonState.Success -> Icons.Default.Check
        ButtonState.Error -> Icons.Default.Check
    }
}

/**
 * 紧凑版一键分配按钮
 * 适用于空间受限的场景
 */
@Composable
fun CompactOneClickAssignmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    val buttonState = when {
        isError -> ButtonState.Error
        isSuccess -> ButtonState.Success
        isLoading -> ButtonState.Loading
        else -> ButtonState.Normal
    }
    
    val buttonColors = getButtonColors(buttonState)
    val buttonIcon = getButtonIcon(buttonState)
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = if (enabled && buttonState == ButtonState.Normal) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = buttonColors
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = buttonState,
                transitionSpec = {
                    scaleIn() + fadeIn() with scaleOut() + fadeOut()
                },
                label = "compact_button_content"
            ) { state ->
                if (state == ButtonState.Loading) {
                    LoadingIndicator()
                } else {
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = when (state) {
                            ButtonState.Normal -> "一键智能分配"
                            ButtonState.Success -> "分配完成"
                            ButtonState.Error -> "分配失败"
                            else -> null
                        },
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 带提示的一键分配按钮
 * 包含工具提示和帮助信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneClickAssignmentButtonWithTooltip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    tooltipText: String = "根据员工技能和项目需求智能分配"
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(
                    text = tooltipText,
                    fontSize = 12.sp
                )
            }
        },
        state = rememberTooltipState()
    ) {
        OneClickAssignmentButton(
            onClick = onClick,
            modifier = modifier,
            isLoading = isLoading,
            isSuccess = isSuccess,
            isError = isError,
            enabled = enabled
        )
    }
}