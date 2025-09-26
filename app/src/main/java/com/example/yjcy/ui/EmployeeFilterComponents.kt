package com.example.yjcy.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt
import android.util.Log
import com.example.yjcy.data.EmployeeSortBy

@Composable
fun FilterPanel(
    selectedPosition: String?,
    onPositionChange: (String?) -> Unit,
    skillLevelRange: ClosedFloatingPointRange<Float>,
    onSkillLevelRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                
                TextButton(
                    onClick = onClearFilters
                ) {
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
                selectedPosition = selectedPosition,
                onPositionChange = onPositionChange
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



// 单选版本的职位筛选芯片（用于员工管理）
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PositionFilterChips(
    selectedPosition: String?,
    onPositionChange: (String?) -> Unit
) {
    val positions = listOf("程序员", "美术师", "策划师", "音效师", "客服")
    
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        positions.forEach { position ->
            val isSelected = position == selectedPosition
            
            FilterChip(
                onClick = {
                    Log.d("PositionFilterChips", "Clicked position: $position, isSelected: $isSelected")
                    if (isSelected) {
                        Log.d("PositionFilterChips", "Deselecting position: $position")
                        onPositionChange(null)
                    } else {
                        Log.d("PositionFilterChips", "Selecting position: $position")
                        onPositionChange(position)
                    }
                },
                label = {
                    Text(
                        text = position,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                },
                selected = isSelected,
                enabled = true,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.3f),
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = Color(0xFFF59E0B),
                    borderColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun FilterSortBar(
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    sortBy: EmployeeSortBy,
    sortAscending: Boolean,
    onSortChange: (EmployeeSortBy, Boolean) -> Unit,
    resultCount: Int,
    selectedPosition: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
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
            // 结果统计
            Text(
                text = "$resultCount/30",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            // 控制按钮组
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 筛选按钮
                ModernButton(
                    text = "筛选",
                    icon = if (showFilters) "🔽" else "🔼",
                    onClick = onToggleFilters,
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showFilters) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
                    )
                )
                
                // 排序控件
                SortDropdownMenu(
                    sortBy = sortBy,
                    sortAscending = sortAscending,
                    onSortChange = onSortChange
                )
            }
        }
    }
}

@Composable
fun SortDropdownMenu(
    sortBy: EmployeeSortBy,
    sortAscending: Boolean,
    onSortChange: (EmployeeSortBy, Boolean) -> Unit
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
                EmployeeSortBy.NAME to "姓名",
                EmployeeSortBy.POSITION to "职位",
                EmployeeSortBy.SALARY to "薪资",
                EmployeeSortBy.SKILL_LEVEL to "技能等级",
                EmployeeSortBy.HIRE_DATE to "入职时间"
            )
            
            sortOptions.forEach { (sortOption, label) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    if (totalPages <= 1) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
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
            // 上一页按钮
            IconButton(
                onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
                enabled = currentPage > 0
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "上一页",
                    tint = if (currentPage > 0) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            
            // 页码指示器
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val startPage = maxOf(0, currentPage - 2)
                val endPage = minOf(totalPages - 1, currentPage + 2)
                
                if (startPage > 0) {
                    PageIndicator(
                        page = 0,
                        isSelected = false,
                        onClick = { onPageChange(0) }
                    )
                    if (startPage > 1) {
                        Text(
                            text = "...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
                
                for (page in startPage..endPage) {
                    PageIndicator(
                        page = page,
                        isSelected = page == currentPage,
                        onClick = { onPageChange(page) }
                    )
                }
                
                if (endPage < totalPages - 1) {
                    if (endPage < totalPages - 2) {
                        Text(
                            text = "...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    PageIndicator(
                        page = totalPages - 1,
                        isSelected = false,
                        onClick = { onPageChange(totalPages - 1) }
                    )
                }
            }
            
            // 下一页按钮
            IconButton(
                onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages - 1
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "下一页",
                    tint = if (currentPage < totalPages - 1) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun PageIndicator(
    page: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isSelected) Color(0xFFF59E0B) else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = (page + 1).toString(),
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}