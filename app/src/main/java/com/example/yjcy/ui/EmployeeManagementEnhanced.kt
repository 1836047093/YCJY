package com.example.yjcy.ui

import com.example.yjcy.Employee
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

// 员工筛选条件
data class EmployeeFilter(
    val searchQuery: String = "",
    val positions: List<String> = emptyList(),
    val minSalary: Int? = null,
    val maxSalary: Int? = null,
    val minSkillLevel: Int? = null,
    val maxSkillLevel: Int? = null
)

// 员工排序方式
enum class EmployeeSortBy {
    NAME,           // 按姓名排序
    POSITION,       // 按职位排序
    SALARY,         // 按薪资排序
    SKILL_LEVEL,    // 按技能等级排序
    HIRE_DATE       // 按入职时间排序
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementEnhanced(
    employees: List<Employee>,
    onTrainEmployee: (Employee, String) -> Unit,
    onDismissEmployee: (Employee) -> Unit,
    onNavigateToRecruitment: () -> Unit
) {

    
    var searchQuery by remember { mutableStateOf("") }
    var selectedPositions by remember { mutableStateOf(emptyList<String>()) }
    var salaryRange by remember { mutableStateOf(0f..20000f) }
    var skillLevelRange by remember { mutableStateOf(1f..5f) }
    var sortBy by remember { mutableStateOf(EmployeeSortBy.NAME) }
    var sortAscending by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }
    

    
    val itemsPerPage = 8
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // 应用筛选和排序
    val filteredEmployees = remember(employees, searchQuery, selectedPositions, salaryRange, skillLevelRange) {
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
            if (selectedPositions.isNotEmpty() && employee.position !in selectedPositions) {
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
                brush = Brush.verticalGradient(
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
                    text = "招聘中心",
                    icon = "🎯",
                    onClick = onNavigateToRecruitment,
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
            resultCount = filteredEmployees.size
        )
        
        // 筛选面板
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FilterPanel(
                selectedPositions = selectedPositions,
                onPositionsChange = { selectedPositions = it },
                salaryRange = salaryRange,
                onSalaryRangeChange = { salaryRange = it },
                skillLevelRange = skillLevelRange,
                onSkillLevelRangeChange = { skillLevelRange = it },
                onClearFilters = {
                    selectedPositions = emptyList()
                    salaryRange = 0f..20000f
                    skillLevelRange = 1f..100f
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 员工列表
        if (paginatedEmployees.isEmpty()) {
            EmptyStateCard(
                message = if (searchQuery.isNotEmpty() || selectedPositions.isNotEmpty()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "搜索",
                tint = Color.White.copy(alpha = 0.7f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") }
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "清除",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFFF59E0B),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = Color(0xFFF59E0B)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FilterSortBar(
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    sortBy: EmployeeSortBy,
    sortAscending: Boolean,
    onSortChange: (EmployeeSortBy, Boolean) -> Unit,
    resultCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 筛选按钮
        ModernButton(
            text = "筛选",
            icon = if (showFilters) "🔽" else "🔼",
            onClick = onToggleFilters,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showFilters) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
            )
        )
        
        // 结果计数
        Text(
            text = "共 $resultCount 名员工",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
        
        // 排序按钮
        SortDropdownMenu(
            sortBy = sortBy,
            sortAscending = sortAscending,
            onSortChange = onSortChange
        )
    }
}

// ModernButton已在ModernComponents.kt中定义，此处删除重复定义

@Composable
fun EmptyStateCard(message: String) {
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
                text = "📭",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}