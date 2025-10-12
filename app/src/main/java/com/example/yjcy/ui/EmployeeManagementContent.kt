package com.example.yjcy.ui

import com.example.yjcy.data.SkillConstants

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.Employee
import com.example.yjcy.ui.components.TalentMarketDialog
import kotlin.random.Random

@Composable
fun EmployeeManagementContent(
    allEmployees: List<Employee>,
    onEmployeesUpdate: (List<Employee>) -> Unit,
    money: Long,
    onMoneyUpdate: (Long) -> Unit,
    onNavigateToTalentMarket: () -> Unit = {}
) {
    var showTrainingDialog by remember { mutableStateOf(false) }
    var showFireDialog by remember { mutableStateOf(false) }
    var showTalentMarketDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var filterType by remember { mutableStateOf("全部") }
    val listState = rememberLazyListState()
    
    // 过滤员工列表
    val filteredEmployees = remember(allEmployees, filterType) {
        allEmployees.filter { employee ->
            when (filterType) {
                "程序员" -> employee.position == "程序员"
                "策划师" -> employee.position == "策划师"
                "美术师" -> employee.position == "美术师"
                "音效师" -> employee.position == "音效师"
                "客服" -> employee.position == "客服"
                else -> true
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E).copy(alpha = 0.1f),
                        Color(0xFF4A148C).copy(alpha = 0.1f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "👥 员工管理",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 员工统计信息卡片
        EmployeeStatsCard(allEmployees = allEmployees)
        
        // 筛选和操作按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 筛选下拉菜单
            FilterDropdown(
                currentFilter = filterType,
                onFilterChange = { filterType = it },
                modifier = Modifier.weight(1f)
            )
            
            // 人才市场入口按钮
            ModernButton(
                text = "人才市场",
                icon = Icons.Default.PersonAdd,
                onClick = { showTalentMarketDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            )
        }
        
        // 员工列表
        if (filteredEmployees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = "无员工",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无员工",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "点击人才市场按钮招聘员工",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEmployees) { employee ->
                    EnhancedEmployeeCard(
                        employee = employee,
                        onTrainClick = {
                            selectedEmployee = employee
                            showTrainingDialog = true
                        },
                        onFireClick = {
                            selectedEmployee = employee
                            showFireDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // 培训对话框
    if (showTrainingDialog && selectedEmployee != null) {
        EnhancedTrainingDialog(
            employee = selectedEmployee!!,
            money = money,
            onConfirm = { trainingCost ->
                val updatedEmployees = allEmployees.map { emp ->
                    if (emp.id == selectedEmployee!!.id) {
                        val skillBoost = Random.nextInt(1, 3)
                        // 只提升专属技能
                        when (emp.getSpecialtySkillType()) {
                            "开发" -> emp.copy(
                                skillDevelopment = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillDevelopment + skillBoost)
                            )
                            "设计" -> emp.copy(
                                skillDesign = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillDesign + skillBoost)
                            )
                            "美工" -> emp.copy(
                                skillArt = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillArt + skillBoost)
                            )
                            "音乐" -> emp.copy(
                                skillMusic = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillMusic + skillBoost)
                            )
                            "服务" -> emp.copy(
                                skillService = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillService + skillBoost)
                            )
                            else -> emp
                        }
                    } else emp
                }
                onEmployeesUpdate(updatedEmployees)
                onMoneyUpdate(money - trainingCost)
                showTrainingDialog = false
                selectedEmployee = null
            },
            onDismiss = {
                showTrainingDialog = false
                selectedEmployee = null
            }
        )
    }
    
    // 解雇对话框
    if (showFireDialog && selectedEmployee != null) {
        EnhancedFireDialog(
            employee = selectedEmployee!!,
            onConfirm = {
                val updatedEmployees = allEmployees.filter { it.id != selectedEmployee!!.id }
                onEmployeesUpdate(updatedEmployees)
                showFireDialog = false
                selectedEmployee = null
            },
            onDismiss = {
                showFireDialog = false
                selectedEmployee = null
            }
        )
    }
    
    // 人才市场弹出式对话框
    if (showTalentMarketDialog) {
        TalentMarketDialog(
            saveData = com.example.yjcy.data.SaveData(
                money = money,
                allEmployees = allEmployees
            ),
            onDismiss = { showTalentMarketDialog = false },
            onRecruitCandidate = { candidate ->
                // 招聘候选人的逻辑
                val newEmployee = Employee(
                    id = (allEmployees.maxOfOrNull { it.id } ?: 0) + 1,
                    name = candidate.name,
                    position = candidate.position,
                    skillDevelopment = candidate.skillDevelopment,
                    skillDesign = candidate.skillDesign,
                    skillArt = candidate.skillArt,
                    skillMusic = candidate.skillMusic,
                    skillService = candidate.skillService,
                    salary = candidate.expectedSalary,
                    experience = candidate.experience
                )
                
                // 添加新员工到列表
                val updatedEmployees = allEmployees.toMutableList()
                updatedEmployees.add(newEmployee)
                onEmployeesUpdate(updatedEmployees)
                
                // 扣除招聘费用
                val recruitmentCost = candidate.expectedSalary * 2
                onMoneyUpdate(money - recruitmentCost)
                
                showTalentMarketDialog = false
            }
        )
    }
}

@Composable
fun EmployeeStatsCard(allEmployees: List<Employee>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EmployeeStatItem(
                value = "${allEmployees.size}/30",
                label = "总员工数",
                icon = Icons.Default.People,
                color = Color(0xFF3B82F6)
            )
            
            EmployeeStatItem(
                value = "¥${allEmployees.sumOf { it.salary }}",
                label = "月薪总额",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun EmployeeStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun FilterDropdown(
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("全部", "程序员", "策划师", "美术师", "音效师", "客服")
    
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "筛选",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currentFilter,
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "展开",
                modifier = Modifier.size(18.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    color = Color(0xFF1F2937),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            filterOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = Color.White
                        )
                    },
                    onClick = {
                        onFilterChange(option)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EnhancedEmployeeCard(
    employee: Employee,
    onTrainClick: () -> Unit,
    onFireClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = employee.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = employee.position,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // 技能等级指示器
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${employee.getSpecialtySkillType()}技能：${employee.getSpecialtySkillLevel()}级",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // 技能等级星星
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < employee.getSpecialtySkillLevel()) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = if (index < employee.getSpecialtySkillLevel()) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // 员工等级徽章（基于专属技能等级）
                    val specialtyLevel = employee.getSpecialtySkillLevel()
                    val (grade, gradeColor) = when {
                        specialtyLevel >= 5 -> "S" to Color(0xFF10B981)
                        specialtyLevel >= 4 -> "A" to Color(0xFF3B82F6)
                        specialtyLevel >= 3 -> "B" to Color(0xFFF59E0B)
                        else -> "C" to Color(0xFFEF4444)
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = gradeColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = grade,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "¥${employee.salary}/月",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            // 展开的详细信息
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 技能详情
                    Text(
                        text = "专属技能",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // 只显示该岗位的专属技能
                    val specialtySkillType = employee.getSpecialtySkillType()
                    val specialtySkillLevel = employee.getSpecialtySkillLevel()
                    
                    EnhancedSkillBar(
                        skillName = specialtySkillType,
                        skillLevel = specialtySkillLevel,
                        maxLevel = SkillConstants.MAX_SKILL_LEVEL
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernButton(
                            text = "培训",
                            icon = Icons.Default.School,
                            onClick = onTrainClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6)
                            )
                        )
                        
                        ModernButton(
                            text = "解雇",
                            icon = Icons.Default.PersonRemove,
                            onClick = onFireClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSkillBar(
    skillName: String,
    skillLevel: Int,
    maxLevel: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = skillName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.width(40.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(skillLevel.toFloat() / maxLevel)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF8BC34A)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
        
        Text(
            text = "$skillLevel/$maxLevel",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun EnhancedTrainingDialog(
    employee: Employee,
    money: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val trainingCost = employee.salary.toLong() * 2
    val canAfford = money >= trainingCost
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "培训",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "员工培训",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "为 ${employee.name} 提供培训",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "当前${employee.getSpecialtySkillType()}技能: ${employee.getSpecialtySkillLevel()}级",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "培训费用: ¥$trainingCost",
                    fontSize = 14.sp,
                    color = if (canAfford) Color.White else Color(0xFFEF4444),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (!canAfford) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "警告",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "资金不足！",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "取消",
                            color = Color.White
                        )
                    }
                    
                    Button(
                        onClick = { onConfirm(trainingCost) },
                        enabled = canAfford,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = "确认培训",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedFireDialog(
    employee: Employee,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = "解雇",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "解雇员工",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "确定要解雇 ${employee.name} 吗？",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "警告",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "此操作无法撤销！",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "取消",
                            color = Color.White
                        )
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text(
                            text = "确认解雇",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}