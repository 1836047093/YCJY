package com.example.yjcy.ui.esports

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.TeamLogoConfig
import com.example.yjcy.ui.components.SingleLineText
import com.example.yjcy.utils.formatMoney

/**
 * 战队管理解锁对话框
 * 需要1亿资金解锁，解锁时设置战队名称和队徽
 */
@Composable
fun TeamUnlockDialog(
    currentMoney: Long,
    companyName: String = "", // 公司名称
    year: Int = 1, // 当前年份
    month: Int = 1, // 当前月份
    onDismiss: () -> Unit,
    onUnlock: (teamName: String, logoConfig: TeamLogoConfig) -> Unit
) {
    val unlockCost = 100_000_000L // 1亿
    val canAfford = currentMoney >= unlockCost
    
    var teamName by remember { mutableStateOf("") }
    var selectedScheme by remember { mutableIntStateOf(0) }
    var currentStep by remember { mutableIntStateOf(0) } // 0: 输入队名, 1: 选择队徽
    
    // 获取当前选中的配色方案
    val currentLogoConfig = remember(selectedScheme, teamName, companyName, year, month) {
        val scheme = PredefinedTeamSchemes[selectedScheme]
        // 格式化成立日期（例如：2025年1月）
        val foundedDate = "${year}年${month}月"
        
        TeamLogoConfig(
            backgroundColor1 = scheme.bg1,
            backgroundColor2 = scheme.bg2,
            borderColor1 = scheme.border1,
            borderColor2 = scheme.border2,
            iconColor = scheme.iconColor,
            teamName = teamName.uppercase(),
            subText = "",
            foundedDate = foundedDate,
            ownerCompany = companyName
        )
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 700.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    SingleLineText(
                        text = "⚽ 解锁战队管理",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 解锁费用
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleLineText(
                            text = "解锁费用:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        SingleLineText(
                            text = formatMoney(unlockCost),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) Color(0xFF4CAF50) else Color(0xFFFF5252)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleLineText(
                            text = "当前资金:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        SingleLineText(
                            text = formatMoney(currentMoney),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 步骤指示器
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicator(
                            stepNumber = 1,
                            stepName = "队名",
                            isActive = currentStep == 0,
                            isCompleted = currentStep > 0
                        )
                        
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(
                                    if (currentStep > 0) Color(0xFF4CAF50) else Color.Gray
                                )
                        )
                        
                        StepIndicator(
                            stepNumber = 2,
                            stepName = "队徽",
                            isActive = currentStep == 1,
                            isCompleted = false
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 内容区域
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (currentStep) {
                            0 -> TeamNameStep(
                                teamName = teamName,
                                onTeamNameChange = { if (it.length <= 4) teamName = it },
                                canAfford = canAfford
                            )
                            1 -> LogoSelectionStep(
                                selectedScheme = selectedScheme,
                                onSchemeSelected = { selectedScheme = it },
                                currentLogoConfig = currentLogoConfig
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 取消/返回按钮
                        Button(
                            onClick = {
                                if (currentStep == 0) {
                                    onDismiss()
                                } else {
                                    currentStep = 0
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            SingleLineText(
                                text = if (currentStep == 0) "取消" else "← 返回",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // 下一步/解锁按钮
                        Button(
                            onClick = {
                                if (currentStep == 0) {
                                    currentStep = 1
                                } else {
                                    onUnlock(teamName, currentLogoConfig)
                                }
                            },
                            enabled = if (currentStep == 0) {
                                canAfford && teamName.isNotBlank()
                            } else {
                                true
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                disabledContainerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            SingleLineText(
                                text = if (currentStep == 0) "下一步 →" else "✓ 解锁",
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

/**
 * 步骤指示器
 */
@Composable
private fun StepIndicator(
    stepNumber: Int,
    stepName: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = when {
                        isCompleted -> Color(0xFF4CAF50)
                        isActive -> Color(0xFF2196F3)
                        else -> Color.Gray
                    },
                    shape = CircleShape
                )
                .border(
                    width = if (isActive) 3.dp else 0.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            SingleLineText(
                text = if (isCompleted) "✓" else stepNumber.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        SingleLineText(
            text = stepName,
            fontSize = 12.sp,
            color = if (isActive) Color.White else Color.Gray
        )
    }
}

/**
 * 第一步：输入战队名称
 */
@Composable
private fun TeamNameStep(
    teamName: String,
    onTeamNameChange: (String) -> Unit,
    canAfford: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 提示信息
        if (!canAfford) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5252).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleLineText(
                        text = "⚠️",
                        fontSize = 24.sp
                    )
                    Column {
                        SingleLineText(
                            text = "资金不足",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                        SingleLineText(
                            text = "需要1亿资金才能解锁战队管理功能",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // 输入框
        OutlinedTextField(
            value = teamName,
            onValueChange = onTeamNameChange,
            label = { Text("战队名称 (最多4个字)") },
            placeholder = { Text("例如: YJCY") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF2A2A3E),
                unfocusedContainerColor = Color(0xFF2A2A3E),
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF4CAF50),
                unfocusedLabelColor = Color.Gray
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 提示文字
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(text = "💡", fontSize = 16.sp)
                SingleLineText(
                    text = "战队名称将自动转换为大写",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(text = "⚽", fontSize = 16.sp)
                SingleLineText(
                    text = "建议使用简短有力的名称",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(text = "🏆", fontSize = 16.sp)
                SingleLineText(
                    text = "解锁后可招募选手参加电竞赛事",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 第二步：选择队徽配色
 */
@Composable
private fun LogoSelectionStep(
    selectedScheme: Int,
    onSchemeSelected: (Int) -> Unit,
    currentLogoConfig: TeamLogoConfig
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 队徽预览
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DynamicTeamLogo(
                config = currentLogoConfig,
                modifier = Modifier.size(160.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 配色方案选择
        SingleLineText(
            text = "选择配色方案",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(PredefinedTeamSchemes.size) { index ->
                val scheme = PredefinedTeamSchemes[index]
                ColorSchemeCard(
                    scheme = scheme,
                    schemeName = when(index) {
                        0 -> "经典蓝金"
                        1 -> "烈焰红"
                        2 -> "森林绿"
                        3 -> "皇家紫"
                        4 -> "暗夜黑"
                        5 -> "赛博粉"
                        else -> "方案${index + 1}"
                    },
                    isSelected = selectedScheme == index,
                    onClick = { onSchemeSelected(index) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 提示
        SingleLineText(
            text = "💡 解锁后可在战队管理中进一步定制队徽",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * 配色方案卡片
 */
@Composable
private fun ColorSchemeCard(
    scheme: TeamLogoColorScheme,
    schemeName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color(scheme.bg1), Color(scheme.bg2))
                    ),
                    CircleShape
                )
                .border(
                    width = if (isSelected) 4.dp else 2.dp,
                    color = if (isSelected) Color(0xFF4CAF50) else Color.Gray,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                SingleLineText(
                    text = "✓",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        SingleLineText(
            text = schemeName,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

data class TeamLogoColorScheme(
    val bg1: Long,
    val bg2: Long,
    val border1: Long,
    val border2: Long,
    val iconColor: Long
)

private val PredefinedTeamSchemes = listOf(
    // 经典蓝金
    TeamLogoColorScheme(0xFF1565C0, 0xFF0D47A1, 0xFFFFD700, 0xFFFFA000, 0xFFFFFFFF),
    // 烈焰红
    TeamLogoColorScheme(0xFFC62828, 0xFF8E0000, 0xFFFFD700, 0xFFFF5722, 0xFFFFFFFF),
    // 森林绿
    TeamLogoColorScheme(0xFF2E7D32, 0xFF1B5E20, 0xFF81C784, 0xFF4CAF50, 0xFFFFFFFF),
    // 皇家紫
    TeamLogoColorScheme(0xFF6A1B9A, 0xFF4A148C, 0xFFE1BEE7, 0xFFBA68C8, 0xFFFFFFFF),
    // 暗夜黑
    TeamLogoColorScheme(0xFF212121, 0xFF000000, 0xFFFFFFFF, 0xFFBDBDBD, 0xFFFFD700),
    // 赛博粉
    TeamLogoColorScheme(0xFFC2185B, 0xFF880E4F, 0xFF00BCD4, 0xFF00E5FF, 0xFFFFFFFF)
)
