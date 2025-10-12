package com.example.yjcy.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.data.SkillConstants

/**
 * 候选人卡片组件
 * 展示候选人的基本信息、技能等级和招聘按钮
 * 重构版：优化了视觉效果和信息展示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateCard(
    candidate: TalentCandidate,
    onRecruitClick: (TalentCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        label = "card elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 候选人基本信息行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：头像和基本信息
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 头像 - 优化设计
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "候选人头像",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    // 姓名和职位
                    Column {
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = candidate.position,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // 右侧：技能等级标签 - 优化设计
                SkillLevelBadge(
                    skillLevel = candidate.getMaxSkillLevel(),
                    skillCategory = candidate.getSkillCategory()
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // 技能详情 - 优化显示
            SkillsSection(candidate = candidate)
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // 经验和期望薪资 - 优化设计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoChip(
                    label = "工作经验",
                    value = "${candidate.experience}年",
                    icon = "💼",
                    modifier = Modifier.weight(1f)
                )
                
                InfoChip(
                    label = "期望薪资",
                    value = "¥${candidate.expectedSalary}",
                    icon = "💰",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 招聘按钮 - 优化设计
            Button(
                onClick = { onRecruitClick(candidate) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "📩 立即招聘",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * 技能等级徽章 - 优化设计
 */
@Composable
private fun SkillLevelBadge(
    skillLevel: Int,
    skillCategory: SkillConstants.SkillCategory?
) {
    val (backgroundColor, textColor) = when (skillCategory) {
        SkillConstants.SkillCategory.JUNIOR -> 
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f) to MaterialTheme.colorScheme.secondary
        SkillConstants.SkillCategory.INTERMEDIATE -> 
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f) to MaterialTheme.colorScheme.tertiary
        SkillConstants.SkillCategory.SENIOR -> 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) to MaterialTheme.colorScheme.primary
        SkillConstants.SkillCategory.EXPERT -> 
            Color(0xFFFF6B35).copy(alpha = 0.25f) to Color(0xFFFF6B35)
        null -> 
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = SkillConstants.getSkillCategoryDisplayName(skillCategory),
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 技能详情区域 - 优化设计
 */
@Composable
private fun SkillsSection(candidate: TalentCandidate) {
    Column {
        Text(
            text = "技能属性",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 第一行技能
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillItem(
                    label = "开发",
                    level = candidate.skillDevelopment,
                    emoji = "💻",
                    modifier = Modifier.weight(1f)
                )
                SkillItem(
                    label = "设计",
                    level = candidate.skillDesign,
                    emoji = "📋",
                    modifier = Modifier.weight(1f)
                )
                SkillItem(
                    label = "美术",
                    level = candidate.skillArt,
                    emoji = "🎨",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 第二行技能
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillItem(
                    label = "音乐",
                    level = candidate.skillMusic,
                    emoji = "🎵",
                    modifier = Modifier.weight(1f)
                )
                SkillItem(
                    label = "服务",
                    level = candidate.skillService,
                    emoji = "📞",
                    modifier = Modifier.weight(1f)
                )
                // 占位空间保持对齐
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * 单个技能项 - 优化设计
 */
@Composable
private fun SkillItem(
    label: String,
    level: Int,
    emoji: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        level >= 4 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        level >= 3 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        level >= 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    
    val textColor = when {
        level >= 4 -> MaterialTheme.colorScheme.primary
        level >= 3 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Lv.$level",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 信息芯片组件 - 优化设计
 */
@Composable
private fun InfoChip(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 18.sp
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        }
    }
}