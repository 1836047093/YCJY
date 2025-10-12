package com.example.yjcy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.data.SkillConstants
import com.example.yjcy.service.RecruitmentService
import com.example.yjcy.service.RecruitmentResult
import com.example.yjcy.service.RecruitmentFeeDetails

/**
 * 招聘确认对话框
 * 显示候选人信息、招聘费用详情和确认操作
 * 重构版：优化了视觉效果和用户体验
 */
@Composable
fun RecruitmentDialog(
    candidate: TalentCandidate,
    currentFunds: Int,
    currentEmployeeCount: Int,
    maxEmployeeCount: Int,
    onConfirm: (TalentCandidate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recruitmentService = remember { RecruitmentService() }
    val feeDetails = remember(candidate) {
        recruitmentService.getRecruitmentFeeDetails(candidate)
    }
    val canAfford = currentFunds >= feeDetails.totalFee
    val hasSpace = currentEmployeeCount < maxEmployeeCount
    val canRecruit = canAfford && hasSpace
    
    var isProcessing by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp)
            ) {
                // 标题 - 优化设计
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋 招聘确认",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 候选人信息 - 优化设计
                CandidateInfoSection(candidate = candidate)
                
                Spacer(modifier = Modifier.height(22.dp))
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
                
                Spacer(modifier = Modifier.height(22.dp))
                
                // 费用详情 - 优化设计
                FeeDetailsSection(
                    feeDetails = feeDetails,
                    currentFunds = currentFunds,
                    canAfford = canAfford
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 员工数量检查 - 优化设计
                EmployeeCountSection(
                    currentCount = currentEmployeeCount,
                    maxCount = maxEmployeeCount,
                    hasSpace = hasSpace
                )
                
                Spacer(modifier = Modifier.height(26.dp))
                
                // 操作按钮 - 优化设计
                ActionButtonsSection(
                    canRecruit = canRecruit,
                    isProcessing = isProcessing,
                    onConfirm = {
                        isProcessing = true
                        onConfirm(candidate)
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

/**
 * 候选人信息区域 - 优化设计
 */
@Composable
private fun CandidateInfoSection(candidate: TalentCandidate) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像 - 优化设计
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "候选人头像",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(18.dp))
            
            // 基本信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "💼",
                        fontSize = 14.sp
                    )
                    Text(
                        text = candidate.position,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${candidate.experience}年经验 • ${SkillConstants.getSkillCategoryDisplayName(candidate.getSkillCategory())}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 费用详情区域
 */
@Composable
private fun FeeDetailsSection(
    feeDetails: RecruitmentFeeDetails,
    currentFunds: Int,
    canAfford: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "费用详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 基础费用
            FeeItem(
                label = "基础招聘费",
                amount = feeDetails.baseFee
            )
            
            // 技能加成费用
            FeeItem(
                label = "技能加成费",
                amount = feeDetails.skillBonus
            )
            
            // 经验加成费用（暂时设为0）
            FeeItem(
                label = "经验加成费",
                amount = 0
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            // 总费用
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "总费用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "¥${feeDetails.totalFee}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 当前资金状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前资金",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¥$currentFunds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!canAfford) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "资金不足，还需要 ¥${feeDetails.totalFee - currentFunds}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 费用项目
 */
@Composable
private fun FeeItem(
    label: String,
    amount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "¥$amount",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 员工数量检查区域 - 优化设计
 */
@Composable
private fun EmployeeCountSection(
    currentCount: Int,
    maxCount: Int,
    hasSpace: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasSpace) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (hasSpace) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasSpace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasSpace) "✅ 员工名额充足" else "⚠️ 员工名额已满",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (hasSpace) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "当前员工: $currentCount / $maxCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasSpace) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 操作按钮区域 - 优化设计
 */
@Composable
private fun ActionButtonsSection(
    canRecruit: Boolean,
    isProcessing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 取消按钮
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            enabled = !isProcessing,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = "取消",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 确认按钮
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = canRecruit && !isProcessing,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            if (isProcessing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "招聘中...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "✅ 确认招聘",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}