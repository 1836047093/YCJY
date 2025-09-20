package com.example.yjcy.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.yjcy.data.Candidate
import com.example.yjcy.data.AvailabilityStatus
import kotlin.math.roundToInt

@Composable
fun CandidateCard(
    candidate: Candidate,
    onHire: () -> Unit,
    canAfford: Boolean
) {
    var showDetails by remember { mutableStateOf(false) }
    var showHireDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when (candidate.availabilityStatus) {
                AvailabilityStatus.AVAILABLE -> Color(0xFF16A34A).copy(alpha = 0.3f)
                AvailabilityStatus.INTERVIEWING -> Color(0xFFF59E0B).copy(alpha = 0.3f)
                AvailabilityStatus.HIRED -> Color(0xFFEF4444).copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 候选人基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getPositionIcon(candidate.position),
                            fontSize = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = candidate.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = candidate.position,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 状态标签
                    StatusChip(status = candidate.availabilityStatus)
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "¥${candidate.expectedSalary}/月",
                        color = Color(0xFFF59E0B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 综合评分
                    ScoreIndicator(
                        score = candidate.getOverallScore(),
                        label = "综合评分"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 技能预览
            SkillPreview(candidate = candidate)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 招聘成功率和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "成功率: ${(candidate.getHireSuccessRate() * 100).roundToInt()}%",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 详情按钮
                    ModernButton(
                        text = if (showDetails) "收起" else "详情",
                        icon = if (showDetails) "🔽" else "🔼",
                        onClick = { showDetails = !showDetails },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                    
                    // 招聘按钮
                    if (candidate.availabilityStatus == AvailabilityStatus.AVAILABLE) {
                        ModernButton(
                            text = "招聘",
                            icon = "💼",
                            onClick = { showHireDialog = true },
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) Color(0xFF16A34A).copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
            
            // 详细信息展开区域
            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CandidateDetailedInfo(candidate = candidate)
                }
            }
        }
    }
    
    // 招聘确认对话框
    if (showHireDialog) {
        HireConfirmDialog(
            candidate = candidate,
            onConfirm = {
                onHire()
                showHireDialog = false
            },
            onDismiss = { showHireDialog = false }
        )
    }
}

@Composable
fun StatusChip(status: AvailabilityStatus) {
    val statusInfo = when (status) {
        AvailabilityStatus.AVAILABLE -> Triple("可招聘", Color(0xFF16A34A), "✅")
        AvailabilityStatus.INTERVIEWING -> Triple("面试中", Color(0xFFF59E0B), "⏳")
        AvailabilityStatus.HIRED -> Triple("已雇佣", Color(0xFFEF4444), "❌")
    }
    val text = statusInfo.first
    val color = statusInfo.second
    val icon = statusInfo.third
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 10.sp
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ScoreIndicator(
    score: Float,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%.1f", score),
            color = when {
                score >= 4.0f -> Color(0xFF16A34A)
                score >= 3.0f -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun SkillPreview(candidate: Candidate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkillMiniIndicator(
            skillName = "开发",
            level = candidate.programmingSkill,
            modifier = Modifier.weight(1f)
        )
        
        SkillMiniIndicator(
            skillName = "美术",
            level = candidate.designSkill,
            modifier = Modifier.weight(1f)
        )
        
        SkillMiniIndicator(
            skillName = "策划",
            level = candidate.designSkill,
            modifier = Modifier.weight(1f)
        )
        
        SkillMiniIndicator(
            skillName = "音效",
            level = candidate.soundSkill,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SkillMiniIndicator(
    skillName: String,
    level: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = skillName,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (index < level) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }
        
        Text(
            text = "Lv.$level",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 8.sp
        )
    }
}

@Composable
fun CandidateDetailedInfo(candidate: Candidate) {
    Column {
        Text(
            text = "📊 详细技能",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 详细技能展示
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailedSkillRow("程序开发", candidate.programmingSkill)
            DetailedSkillRow("美术设计", candidate.designSkill)
            DetailedSkillRow("游戏策划", candidate.designSkill)
            DetailedSkillRow("音效制作", candidate.soundSkill)
            DetailedSkillRow("客户服务", candidate.customerServiceSkill)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 专属技能
        val specialtyType = candidate.getSpecialtySkillType()
        if (specialtyType.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐",
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "专长: $specialtyType (Lv.${candidate.getSpecialtySkillLevel()})",
                    color = Color(0xFFF59E0B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 其他信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                icon = "🎯",
                label = "平均技能",
                value = "Lv.${String.format("%.1f", candidate.getAverageSkillLevel())}"
            )
            
            InfoItem(
                icon = "💰",
                label = "期望薪资",
                value = "¥${candidate.expectedSalary}"
            )
            
            InfoItem(
                icon = "📈",
                label = "成功率",
                value = "${(candidate.getHireSuccessRate() * 100).roundToInt()}%"
            )
        }
    }
}

@Composable
fun DetailedSkillRow(
    skillName: String,
    level: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = skillName,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.width(60.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index < level) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Lv.$level",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
        
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HireConfirmDialog(
    candidate: Candidate,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2937),
        title = {
            Text(
                text = "💼 确认招聘",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "确定要招聘 ${candidate.name} 吗？",
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "职位:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = candidate.position,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "月薪:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "¥${candidate.expectedSalary}",
                                color = Color(0xFFF59E0B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "成功率:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${(candidate.getHireSuccessRate() * 100).roundToInt()}%",
                                color = Color(0xFF16A34A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "⚠️ 招聘可能失败，失败时仍需支付部分费用",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            ModernButton(
                text = "确认招聘",
                icon = "✅",
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A).copy(alpha = 0.3f)
                )
            )
        },
        dismissButton = {
            ModernButton(
                text = "取消",
                icon = "❌",
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    )
}