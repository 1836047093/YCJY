package com.example.yjcy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.*
import com.example.yjcy.service.JobPostingService

/**
 * 面试对话框
 * 玩家可以选择自己面试或让人事部门面试
 */
@Composable
fun InterviewDialog(
    applicant: JobApplicant,
    jobPosting: JobPosting,
    onDismiss: () -> Unit,
    onInterviewComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jobPostingService = remember { JobPostingService.getInstance() }
    var showInterviewTypeSelection by remember { mutableStateOf(true) }
    var selectedInterviewType by remember { mutableStateOf<InterviewType?>(null) }
    var interviewResult by remember { mutableStateOf<InterviewResult?>(null) }
    var playerDecision by remember { mutableStateOf<Boolean?>(null) }
    var playerNotes by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
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
                        text = "💼 面试候选人",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 候选人信息卡片
                CandidateInfoCard(candidate = applicant.candidate)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                if (showInterviewTypeSelection) {
                    // 面试类型选择
                    InterviewTypeSelection(
                        onPlayerInterviewClick = {
                            selectedInterviewType = InterviewType.PLAYER
                            showInterviewTypeSelection = false
                        },
                        onHRInterviewClick = {
                            selectedInterviewType = InterviewType.HR
                            showInterviewTypeSelection = false
                            
                            // 立即执行人事面试
                            val result = jobPostingService.conductHRInterview(
                                jobPosting.id,
                                applicant.id
                            )
                            interviewResult = result
                        }
                    )
                } else {
                    when (selectedInterviewType) {
                        InterviewType.PLAYER -> {
                            // 玩家面试界面
                            if (playerDecision == null) {
                                PlayerInterviewSection(
                                    candidate = applicant.candidate,
                                    notes = playerNotes,
                                    onNotesChange = { playerNotes = it },
                                    onAccept = {
                                        playerDecision = true
                                        val result = jobPostingService.conductPlayerInterview(
                                            jobPosting.id,
                                            applicant.id,
                                            decision = true,
                                            notes = playerNotes.ifBlank { "通过玩家面试" }
                                        )
                                        interviewResult = result
                                    },
                                    onReject = {
                                        playerDecision = false
                                        val result = jobPostingService.conductPlayerInterview(
                                            jobPosting.id,
                                            applicant.id,
                                            decision = false,
                                            notes = playerNotes.ifBlank { "未通过玩家面试" }
                                        )
                                        interviewResult = result
                                    }
                                )
                            }
                        }
                        else -> {}
                    }
                    
                    // 显示面试结果
                    interviewResult?.let { result ->
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        InterviewResultCard(result = result)
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // 完成按钮
                        Button(
                            onClick = {
                                onInterviewComplete()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "完成",
                                style = MaterialTheme.typography.labelLarge,
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
 * 候选人信息卡片（简化版）
 */
@Composable
private fun CandidateInfoCard(candidate: TalentCandidate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column {
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = candidate.position,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⭐ Lv.${candidate.getMaxSkillLevel()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "💰 ¥${candidate.expectedSalary}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 面试类型选择
 */
@Composable
private fun InterviewTypeSelection(
    onPlayerInterviewClick: () -> Unit,
    onHRInterviewClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "请选择面试方式",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // 玩家面试选项
        Card(
            onClick = onPlayerInterviewClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👨‍💼", fontSize = 24.sp)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "玩家亲自面试",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "你将亲自决定是否录用该候选人",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 人事面试选项
        Card(
            onClick = onHRInterviewClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👥", fontSize = 24.sp)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "人事部门面试",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "由人事部门自动评估候选人资质",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * 玩家面试区域
 */
@Composable
private fun PlayerInterviewSection(
    candidate: TalentCandidate,
    notes: String,
    onNotesChange: (String) -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "候选人技能详情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // 技能展示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillRow("💻 开发", candidate.skillDevelopment)
                SkillRow("📋 设计", candidate.skillDesign)
                SkillRow("🎨 美术", candidate.skillArt)
                SkillRow("🎵 音乐", candidate.skillMusic)
                SkillRow("📞 服务", candidate.skillService)
            }
        }
        
        // 备注输入
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("面试备注（可选）") },
            placeholder = { Text("输入对该候选人的评价...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )
        
        // 决策按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "拒绝",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "通过",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 技能行组件
 */
@Composable
private fun SkillRow(label: String, level: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < level) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                )
            }
        }
    }
}

/**
 * 面试结果卡片
 */
@Composable
private fun InterviewResultCard(result: InterviewResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.passed) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "面试结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (result.passed) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = if (result.passed) "✅ 通过" else "❌ 未通过",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (result.passed) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "面试评分",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${result.score} 分",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            result.score >= 80 -> MaterialTheme.colorScheme.primary
                            result.score >= 60 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "面试方式",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (result.interviewType) {
                            InterviewType.PLAYER -> "玩家面试"
                            InterviewType.HR -> "人事面试"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (result.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "备注",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

