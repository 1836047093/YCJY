package com.example.yjcy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
 * 重构版：优化了交互和视觉效果
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "筛选条件",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 显示是否使用了筛选
                    if (!filterCriteria.isDefault()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "已筛选",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
            }
            
            // 当前筛选条件摘要
            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
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
            }
            
            // 展开的筛选选项
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 职位筛选
                    PositionFilter(
                        selectedPosition = filterCriteria.selectedPosition,
                        onPositionChange = { position ->
                            onFilterChange(filterCriteria.copy(selectedPosition = position))
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 技能等级筛选
                    SkillLevelFilter(
                        minSkillLevel = filterCriteria.minSkillLevel,
                        maxSkillLevel = filterCriteria.maxSkillLevel,
                        onSkillLevelChange = { min, max ->
                            onFilterChange(filterCriteria.copy(minSkillLevel = min, maxSkillLevel = max))
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
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
                            onClick = { onFilterChange(FilterCriteria.default()) },
                            enabled = !filterCriteria.isDefault()
                        ) {
                            Text("🔄 重置筛选")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 职位筛选组件 - 优化设计
 */
@Composable
private fun PositionFilter(
    selectedPosition: String?,
    onPositionChange: (String?) -> Unit
) {
    Column {
        Text(
            text = "职位类型",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        val positions = listOf(null) + FilterCriteria.getAvailablePositions()
        val positionNames = listOf("全部") + FilterCriteria.getAvailablePositions()
        val positionEmojis = listOf("🏢", "💻", "📋", "🎨", "🎵", "📞")
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 第一行：全部、程序员、策划师
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..2).forEach { index ->
                    FilterChip(
                        selected = selectedPosition == positions[index],
                        onClick = { onPositionChange(positions[index]) },
                        label = { 
                            Text(
                                text = "${positionEmojis[index]} ${positionNames[index]}"
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // 第二行：美术师、音效师、客服
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (3..5).forEach { index ->
                    FilterChip(
                        selected = selectedPosition == positions[index],
                        onClick = { onPositionChange(positions[index]) },
                        label = { 
                            Text(
                                text = "${positionEmojis[index]} ${positionNames[index]}"
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 技能等级筛选组件 - 优化设计
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
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        val skillLevelOptions = FilterCriteria.getSkillLevelOptions()
        val skillEmojis = listOf("🌟", "⭐", "🌟", "⭐", "✨")
        
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            skillLevelOptions.forEachIndexed { index, (label, range) ->
                val isSelected = minSkillLevel == range.first && maxSkillLevel == range.last
                
                FilterChip(
                    selected = isSelected,
                    onClick = { 
                        onSkillLevelChange(range.first, range.last)
                    },
                    label = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = skillEmojis[index])
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = label)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 薪资范围筛选组件 - 优化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalaryRangeFilter(
    minSalary: Int,
    maxSalary: Int,
    onSalaryRangeChange: (Int, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "薪资范围",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "¥${minSalary} - ¥${maxSalary}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 薪资范围滑块
        RangeSlider(
            value = minSalary.toFloat()..maxSalary.toFloat(),
            onValueChange = { range ->
                // 确保步长为1000的倍数
                val roundedMin = (range.start / 1000).toInt() * 1000
                val roundedMax = (range.endInclusive / 1000).toInt() * 1000
                if (roundedMin != minSalary || roundedMax != maxSalary) {
                    onSalaryRangeChange(roundedMin, roundedMax)
                }
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