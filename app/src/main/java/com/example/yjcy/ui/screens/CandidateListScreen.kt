package com.example.yjcy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yjcy.data.Candidate
import com.example.yjcy.data.CandidateStatus
import com.example.yjcy.ui.recruitment.RecruitmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListScreen(
    recruitmentId: String,
    onNavigateBack: () -> Unit,
    onCandidateClick: (String) -> Unit,
    viewModel: RecruitmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val candidates by viewModel.getCandidatesForRecruitment(recruitmentId).collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF3B82F6)
                    )
                )
            )
    ) {
        // 顶部导航栏
        TopAppBar(
            title = {
                Text(
                    text = "候选人列表",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        // 候选人统计信息
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总候选人",
                    value = candidates.size.toString(),
                    color = Color(0xFF3B82F6)
                )
                StatItem(
                    label = "待处理",
                    value = candidates.count { it.status == CandidateStatus.PENDING }.toString(),
                    color = Color(0xFFF59E0B)
                )
                StatItem(
                    label = "已聘用",
                    value = candidates.count { it.status == CandidateStatus.HIRED }.toString(),
                    color = Color(0xFF059669)
                )
                StatItem(
                    label = "已拒绝",
                    value = candidates.count { it.status == CandidateStatus.REJECTED }.toString(),
                    color = Color(0xFFDC2626)
                )
            }
        }
        
        // 候选人列表
        if (candidates.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "暂无候选人",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "等待招聘算法生成候选人",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(candidates) { candidate ->
                    CandidateCard(
                        candidate = candidate,
                        onClick = { onCandidateClick(candidate.id) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun CandidateCard(
    candidate: Candidate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 候选人基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 头像
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (candidate.position) {
                                    "程序员" -> Color(0xFF3B82F6)
                                    "策划师" -> Color(0xFFEC4899)
                                    "美术师" -> Color(0xFFEC4899)
                                    "音效师" -> Color(0xFFF59E0B)
                                    "测试员" -> Color(0xFF059669)
                                    else -> Color(0xFF6B7280)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = candidate.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = candidate.position,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                
                // 状态标签
                StatusChip(status = candidate.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 技能等级和薪资期望
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "技能等级 ${candidate.skillLevel}",
                        fontSize = 14.sp,
                        color = Color(0xFF374151)
                    )
                }
                
                Text(
                    text = "期望薪资 ¥${candidate.expectedSalary}/月",
                    fontSize = 14.sp,
                    color = Color(0xFF059669),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 工作经验
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = candidate.getExperienceDescription(),
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
            
            // 特殊能力（如果有）
            if (candidate.specialAbilities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "特殊能力: ${candidate.specialAbilities}",
                    fontSize = 12.sp,
                    color = Color(0xFF7C3AED),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: CandidateStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        CandidateStatus.PENDING -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFFD97706),
            "待处理"
        )
        CandidateStatus.HIRED -> Triple(
            Color(0xFFD1FAE5),
            Color(0xFF059669),
            "已聘用"
        )
        CandidateStatus.REJECTED -> Triple(
            Color(0xFFFEE2E2),
            Color(0xFFDC2626),
            "已拒绝"
        )
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}