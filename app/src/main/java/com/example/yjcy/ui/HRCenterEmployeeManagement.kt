package com.example.yjcy.ui

import com.example.yjcy.data.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

import com.example.yjcy.ui.theme.YjcyTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusManager
import kotlin.math.min
import kotlin.math.max
import com.example.yjcy.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRCenterEmployeeManagement(
    employees: List<Employee>,
    onTrainEmployee: (Employee, String) -> Unit,
    onDismissEmployee: (Employee) -> Unit,
    onNavigateToHRCenter: () -> Unit
) {

    
    var searchQuery by remember { mutableStateOf("") }
    var selectedPosition by remember { mutableStateOf<String?>(null) }
    var salaryRange by remember { mutableStateOf(0f..20000f) }
    var skillLevelRange by remember { mutableStateOf(1f..5f) }
    var sortBy by remember { mutableStateOf(EmployeeSortBy.NAME) }
    var sortAscending by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }
    

    
    val itemsPerPage = 8
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // 应用筛选和排序
    val filteredEmployees = remember(employees, searchQuery, selectedPosition, salaryRange, skillLevelRange) {
        employees.filter { employee ->
            // 搜索查询
            if (searchQuery.isNotEmpty()) {
                val query = searchQuery.lowercase()
                if (!employee.name.lowercase().contains(query) && 
                    !employee.position.lowercase().contains(query)) {
                    return@filter false
                }
            }
            
            // 职位筛选
            if (selectedPosition != null && employee.position != selectedPosition) {
                return@filter false
            }
            
            // 薪资范围筛选
            if (employee.salary < salaryRange.start || employee.salary > salaryRange.endInclusive) {
                return@filter false
            }
            
            // 技能等级筛选
            val skillLevel = employee.getSpecialtySkillLevel()
            if (skillLevel < skillLevelRange.start || skillLevel > skillLevelRange.endInclusive) {
                return@filter false
            }
            
            true
        }
    }
    
    val sortedEmployees = remember(filteredEmployees, sortBy, sortAscending) {
        val sorted = when (sortBy) {
            EmployeeSortBy.NAME -> filteredEmployees.sortedBy { it.name }
            EmployeeSortBy.POSITION -> filteredEmployees.sortedBy { it.position }
            EmployeeSortBy.SALARY -> filteredEmployees.sortedBy { it.salary }
            EmployeeSortBy.SKILL_LEVEL -> filteredEmployees.sortedBy { it.getSpecialtySkillLevel() }
            EmployeeSortBy.HIRE_DATE -> filteredEmployees.sortedBy { it.id } // 假设ID代表入职顺序
        }
        if (sortAscending) sorted else sorted.reversed()
    }
    
    val totalPages = (sortedEmployees.size + itemsPerPage - 1) / itemsPerPage
    val paginatedEmployees = remember(sortedEmployees, currentPage) {
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, sortedEmployees.size)
        if (startIndex < sortedEmployees.size) {
            sortedEmployees.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
    
    // 当筛选结果改变时重置到第一页
    LaunchedEffect(filteredEmployees.size) {
        currentPage = 0
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // 标题和操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👥 员工管理",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                
                ModernButton(
                    text = "人事中心",
                    icon = "🏢",
                    onClick = onNavigateToHRCenter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A).copy(alpha = 0.2f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 搜索栏
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { keyboardController?.hide() },
            placeholder = "搜索员工姓名或职位..."
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 筛选和排序控制栏
        FilterSortBar(
            showFilters = showFilters,
            onToggleFilters = { showFilters = !showFilters },
            sortBy = sortBy,
            sortAscending = sortAscending,
            onSortChange = { newSortBy, ascending ->
                sortBy = newSortBy
                sortAscending = ascending
            },
            resultCount = filteredEmployees.size,
            selectedPosition = selectedPosition
        )
        
        // 筛选面板
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FilterPanel(
                selectedPosition = selectedPosition,
                onPositionChange = { selectedPosition = it },
                salaryRange = salaryRange,
                onSalaryRangeChange = { salaryRange = it },
                skillLevelRange = skillLevelRange,
                onSkillLevelRangeChange = { skillLevelRange = it },
                onClearFilters = {
                    selectedPosition = null
                    salaryRange = 0f..20000f
                    skillLevelRange = 1f..100f
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 员工列表
        if (paginatedEmployees.isEmpty()) {
            EmptyStateCard(
                message = if (searchQuery.isNotEmpty() || selectedPosition != null) {
                    "未找到符合条件的员工"
                } else {
                    "暂无员工数据"
                }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(paginatedEmployees) { employee ->
                    ModernEmployeeCard(
                        employee = employee,
                        onTrainEmployee = onTrainEmployee,
                        onDismissEmployee = onDismissEmployee
                    )
                }
            }
        }
        
        // 分页控制
        if (totalPages > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            PaginationBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onPageChange = { currentPage = it }
            )
        }
    }
}