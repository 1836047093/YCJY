package com.example.yjcy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yjcy.data.FilterCriteria
import com.example.yjcy.data.SkillConstants

/**
 * 筛选区域组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    filterCriteria: FilterCriteria,
    onFilterChange: (FilterCriteria) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 筛选标题和展开/收起按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = false,
                        onClick = { isExpanded = !isExpanded }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "筛选",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "筛选条件",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开"
                    )
                }
            }
            
            // 当前筛选条件摘要
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        append(filterCriteria.getPositionDescription())
                        append(" • ")
                        append(filterCriteria.getSkillLevelDescription())
                        append(" • ")
                        append(filterCriteria.getSalaryRangeDescription())
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 展开的筛选选项
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 职位筛选
                PositionFilter(
                    selectedPosition = filterCriteria.selectedPosition,
                    onPositionChange = { position ->
                        onFilterChange(filterCriteria.copy(selectedPosition = position))
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 技能等级筛选
                SkillLevelFilter(
                    minSkillLevel = filterCriteria.minSkillLevel,
                    maxSkillLevel = filterCriteria.maxSkillLevel,
                    onSkillLevelChange = { min, max ->
                        onFilterChange(filterCriteria.copy(minSkillLevel = min, maxSkillLevel = max))
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 薪资范围筛选
                SalaryRangeFilter(
                    minSalary = filterCriteria.minSalary,
                    maxSalary = filterCriteria.maxSalary,
                    onSalaryRangeChange = { min, max ->
                        onFilterChange(filterCriteria.copy(minSalary = min, maxSalary = max))
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 重置按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onFilterChange(FilterCriteria.default()) }
                    ) {
                        Text("重置筛选")
                    }
                }
            }
        }
    }
}

/**
 * 职位筛选组件
 */
@Composable
private fun PositionFilter(
    selectedPosition: String?,
    onPositionChange: (String?) -> Unit
) {
    Column {
        Text(
            text = "职位",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val positions = listOf(null) + FilterCriteria.getAvailablePositions()
        val positionNames = listOf("全部职位") + FilterCriteria.getAvailablePositions()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            positions.forEachIndexed { index, position ->
                FilterChip(
                    selected = selectedPosition == position,
                    onClick = { onPositionChange(position) },
                    label = { Text(positionNames[index]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 技能等级筛选组件
 */
@Composable
private fun SkillLevelFilter(
    minSkillLevel: Int,
    maxSkillLevel: Int,
    onSkillLevelChange: (Int, Int) -> Unit
) {
    Column {
        Text(
            text = "技能等级",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val skillLevelOptions = FilterCriteria.getSkillLevelOptions()
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            skillLevelOptions.forEach { (label, range) ->
                val isSelected = minSkillLevel == range.first && maxSkillLevel == range.last
                
                FilterChip(
                    selected = isSelected,
                    onClick = { 
                        onSkillLevelChange(range.first, range.last)
                    },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 薪资范围筛选组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalaryRangeFilter(
    minSalary: Int,
    maxSalary: Int,
    onSalaryRangeChange: (Int, Int) -> Unit
) {
    Column {
        Text(
            text = "薪资范围",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // 显示当前薪资范围
        Text(
            text = "¥${minSalary} - ¥${maxSalary}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 薪资范围滑块
        RangeSlider(
            value = minSalary.toFloat()..maxSalary.toFloat(),
            onValueChange = { range ->
                onSalaryRangeChange(
                    range.start.toInt(),
                    range.endInclusive.toInt()
                )
            },
            valueRange = FilterCriteria.MIN_SALARY_RANGE.toFloat()..FilterCriteria.MAX_SALARY_RANGE.toFloat(),
            steps = (FilterCriteria.MAX_SALARY_RANGE - FilterCriteria.MIN_SALARY_RANGE) / FilterCriteria.SALARY_STEP - 1
        )
        
        // 薪资范围标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "¥${FilterCriteria.MIN_SALARY_RANGE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¥${FilterCriteria.MAX_SALARY_RANGE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}