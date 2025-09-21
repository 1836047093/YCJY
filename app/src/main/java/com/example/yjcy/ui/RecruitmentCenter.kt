package com.example.yjcy.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.yjcy.data.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitmentCenter(
    candidates: List<Candidate>,
    onHireCandidate: (Candidate) -> Unit,
    onRefreshCandidates: () -> Unit,
    currentMoney: Int
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPositions by remember { mutableStateOf(emptyList<String>()) }
    var salaryRange by remember { mutableStateOf(0f..20000f) }
    var skillLevelRange by remember { mutableStateOf(1f..5f) }
    var sortBy by remember { mutableStateOf(CandidateSortBy.NAME) }
    var sortAscending by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 8
    
    // 筛选和排序逻辑
    val filteredCandidates = remember(candidates, searchQuery, selectedPositions, salaryRange, skillLevelRange) {
        candidates.filter { candidate ->
            val matchesSearch = searchQuery.isEmpty() || 
                candidate.name.contains(searchQuery, ignoreCase = true) ||
                candidate.position.contains(searchQuery, ignoreCase = true)
            
            val matchesPosition = selectedPositions.isEmpty() || 
                candidate.position in selectedPositions
            
            val matchesSalary = candidate.expectedSalary in salaryRange.start.toInt()..salaryRange.endInclusive.toInt()
            
            val matchesSkillLevel = candidate.getSpecialtySkillLevel().toFloat() in skillLevelRange.start..skillLevelRange.endInclusive
            
            val isAvailable = candidate.availabilityStatus != AvailabilityStatus.HIRED
            
            matchesSearch && matchesPosition && matchesSalary && matchesSkillLevel && isAvailable
        }
    }
    
    val sortedCandidates = remember(filteredCandidates, sortBy, sortAscending) {
        when (sortBy) {
            CandidateSortBy.NAME -> if (sortAscending) filteredCandidates.sortedBy { it.name } else filteredCandidates.sortedByDescending { it.name }
            CandidateSortBy.SALARY -> if (sortAscending) filteredCandidates.sortedBy { it.expectedSalary } else filteredCandidates.sortedByDescending { it.expectedSalary }
            CandidateSortBy.EXPERIENCE -> if (sortAscending) filteredCandidates.sortedBy { it.experienceYears } else filteredCandidates.sortedByDescending { it.experienceYears }
            CandidateSortBy.SKILL_LEVEL -> if (sortAscending) filteredCandidates.sortedBy { it.getSpecialtySkillLevel() } else filteredCandidates.sortedByDescending { it.getSpecialtySkillLevel() }
            CandidateSortBy.SUCCESS_RATE -> if (sortAscending) filteredCandidates.sortedBy { it.successRate } else filteredCandidates.sortedByDescending { it.successRate }
            CandidateSortBy.RECRUITMENT_COST -> if (sortAscending) filteredCandidates.sortedBy { it.recruitmentCost } else filteredCandidates.sortedByDescending { it.recruitmentCost }
            CandidateSortBy.CREATED_AT -> if (sortAscending) filteredCandidates.sortedBy { it.createdAt } else filteredCandidates.sortedByDescending { it.createdAt }
        }
    }
    
    val totalPages = (sortedCandidates.size + itemsPerPage - 1) / itemsPerPage
    val paginatedCandidates = remember(sortedCandidates, currentPage) {
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, sortedCandidates.size)
        if (startIndex < sortedCandidates.size) {
            sortedCandidates.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
    
    // 重置页码当筛选结果改变时
    LaunchedEffect(filteredCandidates.size) {
        currentPage = 0
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // 标题栏
        RecruitmentHeader(
            totalCandidates = candidates.size,
            filteredCandidates = filteredCandidates.size,
            currentMoney = currentMoney,
            onRefresh = onRefreshCandidates
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 搜索和筛选栏
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            showFilters = showFilters,
            onToggleFilters = { showFilters = !showFilters },
            sortBy = sortBy,
            sortAscending = sortAscending,
            onSortChange = { newSortBy, ascending ->
                sortBy = newSortBy
                sortAscending = ascending
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 筛选面板
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CandidateFilterPanel(
                selectedPositions = selectedPositions,
                onPositionsChange = { selectedPositions = it },
                salaryRange = salaryRange,
                onSalaryRangeChange = { salaryRange = it },
                skillLevelRange = skillLevelRange,
                onSkillLevelRangeChange = { skillLevelRange = it },
                onClearFilters = {
                    selectedPositions = emptyList()
                    salaryRange = 0f..20000f
                    skillLevelRange = 1f..5f
                    searchQuery = ""
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 候选人列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(paginatedCandidates) { candidate ->
                CandidateCard(
                    candidate = candidate,
                    onHire = { onHireCandidate(candidate) },
                    canAfford = currentMoney >= candidate.expectedSalary
                )
            }
            
            if (paginatedCandidates.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 分页控制
        if (totalPages > 1) {
            PaginationBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onPageChange = { currentPage = it }
            )
        }
    }
}

@Composable
fun RecruitmentHeader(
    totalCandidates: Int,
    filteredCandidates: Int,
    currentMoney: Int,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🎯 招聘中心",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "显示 $filteredCandidates / $totalCandidates 位候选人",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                
                Text(
                    text = "💰 可用资金: ¥${currentMoney}",
                    color = Color(0xFFF59E0B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            ModernButton(
                text = "刷新",
                icon = "🔄",
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A).copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    sortBy: CandidateSortBy,
    sortAscending: Boolean,
    onSortChange: (CandidateSortBy, Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 搜索框
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "搜索候选人姓名或职位...",
            modifier = Modifier.weight(1f)
        )
        
        // 筛选按钮
        ModernButton(
            text = "筛选",
            icon = if (showFilters) "🔽" else "🔼",
            onClick = onToggleFilters,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showFilters) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
            )
        )
        
        // 排序菜单
        CandidateSortDropdown(
            sortBy = sortBy,
            sortAscending = sortAscending,
            onSortChange = onSortChange
        )
    }
}

@Composable
fun CandidateFilterPanel(
    selectedPositions: List<String>,
    onPositionsChange: (List<String>) -> Unit,
    salaryRange: ClosedFloatingPointRange<Float>,
    onSalaryRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    skillLevelRange: ClosedFloatingPointRange<Float>,
    onSkillLevelRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍 筛选条件",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "清除全部",
                        color = Color(0xFFF59E0B),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 职位筛选
            Text(
                text = "职位类型",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PositionFilterChips(
                selectedPositions = selectedPositions,
                onPositionsChange = onPositionsChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 期望薪资筛选
            Text(
                text = "期望薪资: ¥${salaryRange.start.roundToInt()} - ¥${salaryRange.endInclusive.roundToInt()}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            RangeSlider(
                value = salaryRange,
                onValueChange = onSalaryRangeChange,
                valueRange = 0f..20000f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF59E0B),
                    activeTrackColor = Color(0xFFF59E0B),
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 技能等级筛选
            Text(
                text = "技能等级: Lv.${skillLevelRange.start.roundToInt()} - Lv.${skillLevelRange.endInclusive.roundToInt()}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            RangeSlider(
                value = skillLevelRange,
                onValueChange = onSkillLevelRangeChange,
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF16A34A),
                    activeTrackColor = Color(0xFF16A34A),
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun CandidateSortDropdown(
    sortBy: CandidateSortBy,
    sortAscending: Boolean,
    onSortChange: (CandidateSortBy, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        ModernButton(
            text = "排序",
            icon = if (sortAscending) "⬆️" else "⬇️",
            onClick = { expanded = true },
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                color = Color(0xFF1F2937),
                shape = RoundedCornerShape(12.dp)
            )
        ) {
            val sortOptions = listOf(
                CandidateSortBy.NAME to "姓名",
                CandidateSortBy.SALARY to "期望薪资",
                CandidateSortBy.EXPERIENCE to "工作经验",
                CandidateSortBy.SKILL_LEVEL to "技能等级",
                CandidateSortBy.SUCCESS_RATE to "成功率",
                CandidateSortBy.RECRUITMENT_COST to "招聘成本",
                CandidateSortBy.CREATED_AT to "创建时间"
            )
            
            sortOptions.forEach { pair ->
                val sortOption = pair.first
                val label = pair.second
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            if (sortBy == sortOption) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (sortAscending) "↑" else "↓",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    onClick = {
                        val newAscending = if (sortBy == sortOption) !sortAscending else true
                        onSortChange(sortOption, newAscending)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(textColor = Color.White)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔍",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "没有找到符合条件的候选人",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "尝试调整筛选条件或刷新候选人列表",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}