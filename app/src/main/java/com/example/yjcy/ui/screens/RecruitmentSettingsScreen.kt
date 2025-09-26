package com.example.yjcy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.yjcy.data.RecruitmentDuration
import com.example.yjcy.data.RecruitmentPosition
import com.example.yjcy.ui.recruitment.RecruitmentSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitmentSettingsScreen(
    onNavigateBack: () -> Unit,
    onStartRecruitment: (position: RecruitmentPosition, skillLevel: Int, salary: Int, duration: RecruitmentDuration) -> Unit,
    viewModel: RecruitmentSettingsViewModel = viewModel()
) {
    val uiState by viewModel.settingsState.collectAsState()
    
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
                    text = "招聘设置",
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
        
        // 设置内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 职位选择
            SettingsCard(
                title = "招聘职位",
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(RecruitmentPosition.values().toList()) { position ->
                            FilterChip(
                                onClick = { viewModel.updatePosition(position) },
                                label = {
                                    Text(
                                        text = when (position) {
                                            RecruitmentPosition.PROGRAMMER -> "程序员"
                                            RecruitmentPosition.DESIGNER -> "策划师"
                                            RecruitmentPosition.ARTIST -> "美术师"
                                            RecruitmentPosition.SOUND_ENGINEER -> "音效师"
                                            RecruitmentPosition.TESTER -> "测试员"
                                        }
                                    )
                                },
                                selected = uiState.selectedPosition == position,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            )
            
            // 技能等级选择
            SettingsCard(
                title = "技能等级要求",
                content = {
                    Column {
                        Text(
                            text = "等级 ${uiState.selectedSkillLevel}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E40AF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = uiState.selectedSkillLevel.toFloat(),
                            onValueChange = { viewModel.updateSkillLevel(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1", fontSize = 12.sp, color = Color.Gray)
                            Text("10", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            )
            
            // 月薪设置
            SettingsCard(
                title = "月薪预算",
                content = {
                    Column {
                        Text(
                            text = "¥${uiState.selectedSalary}/月",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF059669)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = uiState.selectedSalary.toFloat(),
                            onValueChange = { viewModel.updateSalary(it.toInt()) },
                            valueRange = 3000f..20000f,
                            steps = 33,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF059669),
                                activeTrackColor = Color(0xFF059669)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("¥3,000", fontSize = 12.sp, color = Color.Gray)
                            Text("¥20,000", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            )
            
            // 招聘天数选择
            SettingsCard(
                title = "招聘周期",
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(RecruitmentDuration.values().toList()) { duration ->
                            FilterChip(
                                onClick = { viewModel.updateDuration(duration) },
                                label = {
                                    Text(
                                        text = when (duration) {
                                            RecruitmentDuration.SHORT -> "30天"
                                            RecruitmentDuration.MEDIUM -> "60天"
                                            RecruitmentDuration.LONG -> "90天"
                                        }
                                    )
                                },
                                selected = uiState.selectedDuration == duration,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDC2626),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            )
            
            // 预估信息
            SettingsCard(
                title = "招聘预估",
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(
                            label = "预计候选人数量",
                            value = "${uiState.estimatedCandidates}人"
                        )
                        InfoRow(
                            label = "总预算",
                            value = "¥${uiState.totalBudget}"
                        )
                        InfoRow(
                            label = "招聘成功率",
                            value = "${uiState.successRate}%"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 开始招聘按钮
            Button(
                onClick = {
                    val position = uiState.selectedPosition
                    val duration = uiState.selectedDuration
                    if (position != null && duration != null) {
                        onStartRecruitment(
                            position,
                            uiState.selectedSkillLevel,
                            uiState.selectedSalary,
                            duration
                        )
                    }
                },
                enabled = uiState.isComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF059669)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "开始招聘",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937)
        )
    }
}