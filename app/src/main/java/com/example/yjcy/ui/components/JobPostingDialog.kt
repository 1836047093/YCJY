package com.example.yjcy.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.FilterCriteria
import com.example.yjcy.service.JobPostingService
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToInt

/**
 * 岗位发布对话框 - 全新现代化设计
 * 采用分步骤流程，更直观的交互体验
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostingDialog(
    onDismiss: () -> Unit,
    onPostingCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jobPostingService = remember { JobPostingService.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    
    // 表单状态
    var selectedPosition by remember { mutableStateOf<String?>(null) }
    var skillLevel by remember { mutableIntStateOf(1) } // 专属技能等级（1-5级）
    var salary by remember { mutableIntStateOf(10000) }
    var currentStep by remember { mutableIntStateOf(1) } // 1: 选择岗位, 2: 设置薪资和技能等级
    
    val positions = FilterCriteria.getAvailablePositions()
    
    // 岗位信息映射（岗位 -> 图标、技能类型、最低技能等级、描述、颜色）
    val positionInfo = mapOf(
        "程序员" to PositionInfo("💻", "开发", 1, "编写游戏代码", Color(0xFF3B82F6)),
        "策划师" to PositionInfo("📋", "设计", 1, "设计游戏玩法", Color(0xFF10B981)),
        "美术师" to PositionInfo("🎨", "美工", 1, "制作游戏美术", Color(0xFFF59E0B)),
        "音效师" to PositionInfo("🎵", "音乐", 1, "创作游戏音乐", Color(0xFF8B5CF6)),
        "客服" to PositionInfo("💬", "服务", 1, "处理客户服务", Color(0xFFEC4899))
    )
    
    // 根据岗位获取对应的技能类型
    val skillType = positionInfo[selectedPosition]?.skillType ?: "开发"
    
    // 根据技能等级计算最低薪资标准（硬性要求）
    val minSalaryRequired = skillLevel * 10000
    
    // 如果选择的岗位改变了，重置技能等级和薪资
    LaunchedEffect(selectedPosition) {
        if (selectedPosition != null) {
            skillLevel = 1 // 重置为1级
            val newMinSalary = skillLevel * 10000
            if (salary < newMinSalary) {
                salary = newMinSalary
            }
        }
    }
    
    // 如果技能等级改变了，自动调整薪资到最低标准
    LaunchedEffect(skillLevel) {
        val newMinSalary = skillLevel * 10000
        if (salary < newMinSalary) {
            salary = newMinSalary
        }
    }
    
    // 计算薪资与最低标准的比率
    val salaryRatio = if (minSalaryRequired > 0) {
        salary.toFloat() / minSalaryRequired.toFloat()
    } else {
        1f
    }
    
    // 验证表单
    val isValid = selectedPosition != null && salary >= minSalaryRequired
    
    // 动画
    val positionCardScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B) // 深色背景
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部标题栏 - 渐变背景
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "发布招聘岗位",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentStep == 1) "第1步：选择岗位" else "第2步：设置技能等级和薪资",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // 步骤指示器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StepIndicator(step = 1, currentStep = currentStep, label = "选择岗位")
                    Spacer(modifier = Modifier.weight(1f))
                    StepIndicator(step = 2, currentStep = currentStep, label = "设置条件")
                }
                
                // 内容区域
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val scrollState = rememberScrollState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 步骤1：选择岗位
                        AnimatedVisibility(
                            visible = currentStep == 1,
                            enter = fadeIn() + slideInHorizontally(),
                            exit = fadeOut() + slideOutHorizontally()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "选择要招聘的岗位类型",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                
                                // 岗位卡片网格
                                positions.chunked(2).forEach { rowPositions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowPositions.forEach { position ->
                                            val info = positionInfo[position]
                                            val isSelected = selectedPosition == position
                                            
                                            PositionCard(
                                                position = position,
                                                info = info,
                                                isSelected = isSelected,
                                            onClick = {
                                                selectedPosition = position
                                                // 自动进入下一步
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.delay(300)
                                                    currentStep = 2
                                                }
                                            },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        // 如果一行只有一个岗位，添加空白占位
                                        if (rowPositions.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 步骤2：设置薪资
                        AnimatedVisibility(
                            visible = currentStep == 2,
                            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // 已选择的岗位信息
                                selectedPosition?.let { pos ->
                                    val info = positionInfo[pos]
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = info?.color?.copy(alpha = 0.2f) ?: Color.White.copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = info?.icon ?: "💼",
                                                fontSize = 32.sp
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = pos,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = info?.description ?: "",
                                                    fontSize = 12.sp,
                                                    color = Color.White.copy(alpha = 0.7f)
                                                )
                                            }
                                            TextButton(
                                                onClick = { currentStep = 1 }
                                            ) {
                                                Text("更改", color = Color.White.copy(alpha = 0.8f))
                                            }
                                        }
                                    }
                                }
                                
                                // 专属技能等级设置
                                Text(
                                    text = "设置专属技能等级",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${skillType}技能等级",
                                                    fontSize = 14.sp,
                                                    color = Color.White.copy(alpha = 0.7f)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Lv.$skillLevel",
                                                        fontSize = 32.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = selectedPosition?.let { positionInfo[it]?.color } ?: Color.White
                                                    )
                                                    Text(
                                                        text = when (skillLevel) {
                                                            5 -> "⭐ 专家级"
                                                            4 -> "⭐ 高级"
                                                            3 -> "⭐ 中级"
                                                            2 -> "⭐ 初级"
                                                            else -> "⭐ 入门"
                                                        },
                                                        fontSize = 14.sp,
                                                        color = Color.White.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // 技能等级滑块
                                        Slider(
                                            value = skillLevel.toFloat(),
                                            onValueChange = { 
                                                skillLevel = it.toInt().coerceIn(1, 5)
                                            },
                                            valueRange = 1f..5f,
                                            steps = 3, // 1, 2, 3, 4, 5
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = selectedPosition?.let { positionInfo[it]?.color } ?: Color.White,
                                                activeTrackColor = selectedPosition?.let { positionInfo[it]?.color } ?: Color.White,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                            )
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Lv.1 入门",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = "Lv.5 专家",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                                
                                Text(
                                    text = "设置薪资待遇",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                
                                // 薪资显示卡片
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "月薪",
                                                    fontSize = 14.sp,
                                                    color = Color.White.copy(alpha = 0.7f)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "¥${String.format("%,d", salary)}",
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = selectedPosition?.let { positionInfo[it]?.color } ?: Color.White
                                                )
                                            }
                                            
                                            // 薪资吸引力指示器
                                            AttractivenessIndicator(ratio = salaryRatio)
                                        }
                                        
                                        // 薪资滑块
                                        Slider(
                                            value = salary.toFloat(),
                                            onValueChange = { 
                                                salary = it.toInt().coerceAtLeast(minSalaryRequired)
                                            },
                                            valueRange = minSalaryRequired.toFloat()..60000f,
                                            steps = ((60000 - minSalaryRequired) / 1000).coerceAtLeast(0),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = selectedPosition?.let { positionInfo[it]?.color } ?: Color.White,
                                                activeTrackColor = selectedPosition?.let { positionInfo[it]?.color } ?: Color.White,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                            )
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "¥${String.format("%,d", minSalaryRequired)}",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = "¥60,000",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        
                                        // 薪资建议
                                        SalaryAdviceCard(ratio = salaryRatio, minSalary = minSalaryRequired, currentSalary = salary)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 底部操作按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep == 2) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("上一步", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Button(
                        onClick = {
                            if (currentStep == 1 && selectedPosition != null) {
                                currentStep = 2
                            } else if (currentStep == 2 && isValid) {
                                val position = selectedPosition!!
                                val skillInfo = positionInfo[position]
                                
                                jobPostingService.createJobPosting(
                                    position = position,
                                    minSkillLevel = skillLevel,
                                    minSalary = salary,
                                    maxSalary = salary
                                )
                                onPostingCreated()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = if (currentStep == 1) selectedPosition != null else isValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isValid || selectedPosition != null) {
                                selectedPosition?.let { positionInfo[it]?.color } ?: Color(0xFF10B981)
                            } else {
                                Color.Gray.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Text(
                            text = if (currentStep == 1) "下一步" else "发布岗位",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 岗位信息数据类
data class PositionInfo(
    val icon: String,
    val skillType: String,
    val minLevel: Int,
    val description: String,
    val color: Color
)

// 岗位卡片组件 - 现代化设计（无边框方框）
@Composable
fun PositionCard(
    position: String,
    info: PositionInfo?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = info?.color ?: Color(0xFF6B7280)
    
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 160.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                // 基础背景层 - 增强可见性
                color = if (isSelected) {
                    cardColor.copy(alpha = 0.12f)
                } else {
                    Color.White.copy(alpha = 0.15f)
                }
            )
            .background(
                brush = Brush.radialGradient(
                    colors = if (isSelected) {
                        listOf(
                            cardColor.copy(alpha = 0.3f),
                            cardColor.copy(alpha = 0.18f),
                            cardColor.copy(alpha = 0.08f)
                        )
                    } else {
                        listOf(
                            cardColor.copy(alpha = 0.12f),
                            cardColor.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    },
                    radius = 200f
                )
            )
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                cardColor.copy(alpha = 0.18f),
                                Color.Transparent,
                                cardColor.copy(alpha = 0.18f)
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = info?.icon ?: "💼",
                fontSize = 40.sp
            )
            Text(
                text = position,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = info?.description ?: "",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
        
        // 选中状态的柔和光晕效果
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                cardColor.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 150f
                        )
                    )
            )
        }
    }
}

// 步骤指示器
@Composable
fun StepIndicator(step: Int, currentStep: Int, label: String) {
    val isActive = step <= currentStep
    val isCurrent = step == currentStep
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "$step",
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (isCurrent) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// 吸引力指示器
@Composable
fun AttractivenessIndicator(ratio: Float) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = when {
                ratio >= 1.5f -> "💎 极高"
                ratio >= 1.25f -> "✨ 很高"
                ratio >= 1.15f -> "👍 较高"
                ratio >= 1.05f -> "💡 一般"
                else -> "⚠️ 较低"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                ratio >= 1.5f -> Color(0xFF51CF66)
                ratio >= 1.25f -> Color(0xFF10B981)
                ratio >= 1.15f -> Color(0xFF3B82F6)
                ratio >= 1.05f -> Color(0xFFF59E0B)
                else -> Color(0xFFFFD93D)
            }
        )
        Text(
            text = "${(ratio * 100).roundToInt()}%",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

// 薪资建议卡片
@Composable
fun SalaryAdviceCard(ratio: Float, minSalary: Int, currentSalary: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                ratio >= 1.5f -> Color(0xFF51CF66).copy(alpha = 0.2f)
                ratio >= 1.25f -> Color(0xFF10B981).copy(alpha = 0.2f)
                ratio >= 1.15f -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                ratio >= 1.05f -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                else -> Color(0xFFFFD93D).copy(alpha = 0.2f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = when {
                    ratio >= 1.5f -> "💎 远高于标准（+${((ratio - 1) * 100).roundToInt()}%）- 会吸引大量优秀应聘者！"
                    ratio >= 1.25f -> "✨ 高于标准25%+ - 会有较多应聘者"
                    ratio >= 1.15f -> "👌 高于标准15%+ - 一般数量的应聘者"
                    ratio >= 1.05f -> "📝 略高于标准 - 少量应聘者"
                    else -> "⚠️ 刚达最低标准 - 极少应聘者，建议提高薪资！"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    ratio >= 1.5f -> Color(0xFF51CF66)
                    ratio >= 1.25f -> Color(0xFF10B981)
                    ratio >= 1.15f -> Color(0xFF3B82F6)
                    ratio >= 1.05f -> Color(0xFFF59E0B)
                    else -> Color(0xFFFFD93D)
                }
            )
        }
    }
}
