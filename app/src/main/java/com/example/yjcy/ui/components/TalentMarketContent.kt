package com.example.yjcy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.data.FilterCriteria
import com.example.yjcy.data.SaveData
import com.example.yjcy.service.TalentMarketService
import com.example.yjcy.service.RecruitmentService

/**
 * 人才市场主界面组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalentMarketContent(
    saveData: SaveData,
    onBack: () -> Unit,
    onRecruitCandidate: (TalentCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    // 服务实例
    val talentMarketService = remember { TalentMarketService() }
    val recruitmentService = remember { RecruitmentService() }
    
    // 获取现有员工名字集合，确保候选人名字唯一
    val existingEmployeeNames = remember { saveData.allEmployees.map { it.name }.toSet() }
    
    // 状态管理
    var candidates by remember { mutableStateOf(talentMarketService.generateCandidates(existingEmployeeNames = existingEmployeeNames)) }
    var filteredCandidates by remember { mutableStateOf(candidates) }
    var filterCriteria by remember { mutableStateOf(FilterCriteria.default()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCandidate by remember { mutableStateOf<TalentCandidate?>(null) }
    
    // 应用筛选条件
    LaunchedEffect(candidates, filterCriteria) {
        filteredCandidates = talentMarketService.filterCandidates(candidates, filterCriteria)
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = "人才市场",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        isLoading = true
                        // 收集现有员工名字和当前候选人名字，确保刷新后不重复
                        val allUsedNames = saveData.allEmployees.map { it.name }.toSet() + 
                                          candidates.map { it.name }.toSet()
                        candidates = talentMarketService.refreshCandidates(existingEmployeeNames = allUsedNames)
                        isLoading = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新候选人"
                    )
                }
            }
        )
        
        // 资金显示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "可用资金",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "¥${saveData.money}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 筛选区域
        FilterSection(
            filterCriteria = filterCriteria,
            onFilterChange = { newCriteria ->
                filterCriteria = newCriteria
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // 候选人统计信息
        val stats = remember(filteredCandidates) {
            talentMarketService.getCandidateStats(filteredCandidates)
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "候选人统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("总数: ${stats.totalCount}")
                    Text("平均薪资: ¥${stats.averageSalary}")
                    Text("平均技能: ${String.format("%.1f", stats.averageSkillLevel)}级")
                }
            }
        }
        
        // 候选人列表
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredCandidates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "没有找到符合条件的候选人",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCandidates) { candidate ->
                    CandidateCard(
                        candidate = candidate,
                        onRecruitClick = { selectedCandidate = candidate }
                    )
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