package com.example.yjcy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.data.FilterCriteria
import com.example.yjcy.data.SaveData
import com.example.yjcy.service.TalentMarketService
import com.example.yjcy.service.RecruitmentService

/**
 * 人才市场弹出式对话框组件
 * 重构版：优化了UI布局和用户体验
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalentMarketDialog(
    saveData: SaveData,
    onDismiss: () -> Unit,
    onRecruitCandidate: (TalentCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    // 服务实例
    val talentMarketService = remember { TalentMarketService() }
    val recruitmentService = remember { RecruitmentService() }
    
    // 状态管理
    var candidates by remember { mutableStateOf(talentMarketService.generateCandidates()) }
    var filteredCandidates by remember { mutableStateOf(candidates) }
    var filterCriteria by remember { mutableStateOf(FilterCriteria.default()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCandidate by remember { mutableStateOf<TalentCandidate?>(null) }
    var showRefreshAnimation by remember { mutableStateOf(false) }
    
    // 应用筛选条件
    LaunchedEffect(candidates, filterCriteria) {
        filteredCandidates = talentMarketService.filterCandidates(candidates, filterCriteria)
    }
    
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
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部标题栏 - 使用渐变背景
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "人才市场",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 刷新按钮
                            IconButton(
                                onClick = {
                                    isLoading = true
                                    showRefreshAnimation = true
                                    candidates = talentMarketService.refreshCandidates()
                                    isLoading = false
                                    showRefreshAnimation = false
                                },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新候选人列表",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // 关闭按钮
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 资金显示卡片 - 优化设计
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "可用资金",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "¥${String.format("%,d", saveData.money)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 筛选区域
                    FilterSection(
                        filterCriteria = filterCriteria,
                        onFilterChange = { newCriteria ->
                            filterCriteria = newCriteria
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 候选人统计信息 - 优化设计
                    val stats = remember(filteredCandidates) {
                        talentMarketService.getCandidateStats(filteredCandidates)
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "候选人统计",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "共 ${stats.totalCount} 人",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatsItem(
                                    label = "平均薪资",
                                    value = "¥${stats.averageSalary}",
                                    modifier = Modifier.weight(1f)
                                )
                                Divider(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                StatsItem(
                                    label = "平均技能",
                                    value = "${String.format("%.1f", stats.averageSkillLevel)}级",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 候选人列表
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "正在加载候选人...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (filteredCandidates.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "😕 没有找到符合条件的候选人",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "请调整筛选条件或刷新候选人列表",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = filteredCandidates,
                                key = { it.id }
                            ) { candidate ->
                                CandidateCard(
                                    candidate = candidate,
                                    onRecruitClick = { selectedCandidate = candidate },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 招聘确认弹窗
    selectedCandidate?.let { candidate ->
        RecruitmentDialog(
            candidate = candidate,
            currentFunds = saveData.money.toInt(),
            currentEmployeeCount = saveData.allEmployees.size,
            maxEmployeeCount = recruitmentService.getMaxEmployeeCount(),
            onConfirm = { 
                onRecruitCandidate(candidate)
                selectedCandidate = null
                // 从候选人列表中移除已招聘的候选人
                candidates = candidates.filter { it.id != candidate.id }
            },
            onDismiss = { selectedCandidate = null }
        )
    }
}

/**
 * 统计信息项组件
 */
@Composable
private fun StatsItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}