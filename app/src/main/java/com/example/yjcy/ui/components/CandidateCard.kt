package com.example.yjcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateCard(
    candidate: TalentCandidate,
    onRecruitClick: (TalentCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                    // 头像
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "候选人头像",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 姓名和职位
                    Column {
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = candidate.position,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // 右侧：技能等级标签
                SkillLevelBadge(
                    skillLevel = candidate.getMaxSkillLevel(),
                    skillCategory = candidate.getSkillCategory()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 技能详情
            SkillsSection(candidate = candidate)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 经验和期望薪资
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    label = "经验",
                    value = "${candidate.experience}年",
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                InfoChip(
                    label = "期望薪资",
                    value = "¥${candidate.expectedSalary}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 招聘按钮
            Button(
                onClick = { onRecruitClick(candidate) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "招聘",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * 技能等级徽章
 */
@Composable
private fun SkillLevelBadge(
    skillLevel: Int,
    skillCategory: SkillConstants.SkillCategory?
) {
    val (backgroundColor, textColor) = when (skillCategory) {
        SkillConstants.SkillCategory.JUNIOR -> 
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.secondary
        SkillConstants.SkillCategory.INTERMEDIATE -> 
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.tertiary
        SkillConstants.SkillCategory.SENIOR -> 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.primary
        SkillConstants.SkillCategory.EXPERT -> 
            Color(0xFFFF6B35).copy(alpha = 0.2f) to Color(0xFFFF6B35)
        null -> 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = SkillConstants.getSkillCategoryDisplayName(skillCategory),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 技能详情区域
 */
@Composable
private fun SkillsSection(candidate: TalentCandidate) {
    Column {
        Text(
            text = "技能详情",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkillItem(
                label = "开发",
                level = candidate.skillDevelopment,
                modifier = Modifier.weight(1f)
            )
            SkillItem(
                label = "设计",
                level = candidate.skillDesign,
                modifier = Modifier.weight(1f)
            )
            SkillItem(
                label = "美术",
                level = candidate.skillArt,
                modifier = Modifier.weight(1f)
            )
            SkillItem(
                label = "音乐",
                level = candidate.skillMusic,
                modifier = Modifier.weight(1f)
            )
            SkillItem(
                label = "服务",
                level = candidate.skillService,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 单个技能项
 */
@Composable
private fun SkillItem(
    label: String,
    level: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = level.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                level >= 4 -> MaterialTheme.colorScheme.primary
                level >= 3 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * 信息芯片组件
 */
@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}