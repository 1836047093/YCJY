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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.Employee
import com.example.yjcy.data.TalentCandidate
import com.example.yjcy.ui.components.NewTalentMarketDialog
import kotlin.random.Random

@Composable
fun EmployeeManagementContent(
    allEmployees: List<Employee>,
    onEmployeesUpdate: (List<Employee>) -> Unit,
    money: Long,
    onMoneyUpdate: (Long) -> Unit,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    @Suppress("UNUSED_PARAMETER") onNavigateToTalentMarket: () -> Unit = {},
    jobPostingRefreshTrigger: Int = 0 // 用于触发应聘者数据刷新
) {
    var showTrainingDialog by remember { mutableStateOf(false) }
    var showFireDialog by remember { mutableStateOf(false) }
    var showTalentMarketDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var filterType by remember { mutableStateOf("全部") }
    val listState = rememberLazyListState()
    
    
    // 计算总薪资 - 使用remember缓存，避免每次重组都计算
    val totalSalary by remember(allEmployees) {
        derivedStateOf {
            allEmployees.sumOf { it.salary }
        }
    }
    
    // 过滤员工列表 - 使用 derivedStateOf 以正确响应 mutableStateListOf 的变化
    val filteredEmployees by remember(allEmployees, filterType) {
        derivedStateOf {
            if (filterType == "全部") {
                allEmployees
            } else {
                allEmployees.filter { employee ->
                    employee.position == filterType
                }
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
        
        // 员工统计信息 - 卡片设计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmployeeStatItem(
                value = "${allEmployees.size}/30",
                label = "总员工数",
                icon = Icons.Default.People,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            
            EmployeeStatItem(
                value = "¥$totalSalary",
                label = "月薪总额",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }
        
        // 筛选和操作按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                onClick = { 
                    showTalentMarketDialog = true
                },
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
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(
                    items = filteredEmployees,
                    key = { it.id } // 添加key，提升性能
                ) { employee ->
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
        val currentEmployee = selectedEmployee!!
        // 保存员工ID，避免闭包问题
        val employeeId = currentEmployee.id
        val employeeName = currentEmployee.name
        
        EnhancedTrainingDialog(
            employee = currentEmployee,
            money = money,
            onConfirm = { trainingCost ->
                try {
                    android.util.Log.d("EmployeeManagement", "培训确认: 员工ID=$employeeId, 费用=$trainingCost, 当前资金=$money")
                    
                    // 再次检查员工是否存在
                    val employeeToTrain = allEmployees.find { it.id == employeeId }
                    if (employeeToTrain == null) {
                        android.util.Log.w("EmployeeManagement", "培训时员工不存在: ID=$employeeId, 名称=$employeeName")
                        showTrainingDialog = false
                        selectedEmployee = null
                    } else {
                        // 检查资金是否足够
                        if (money < trainingCost) {
                            android.util.Log.w("EmployeeManagement", "培训资金不足: 需要 $trainingCost，当前 $money")
                            showTrainingDialog = false
                            selectedEmployee = null
                        } else {
                            // 固定提升1级技能
                            val skillBoost = 1
                            val updatedEmployees = try {
                                allEmployees.map { emp ->
                                    if (emp.id == employeeId) {
                                        // 只提升专属技能
                                        try {
                                            val skillType = emp.getSpecialtySkillType()
                                            when (skillType) {
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
                                                else -> {
                                                    android.util.Log.w("EmployeeManagement", "未知技能类型: $skillType")
                                                    emp
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("EmployeeManagement", "提升技能时异常: ${e.message}", e)
                                            emp
                                        }
                                    } else emp
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("EmployeeManagement", "映射员工列表时异常: ${e.message}", e)
                                allEmployees.toList() // 返回原列表
                            }
                            
                            // 更新员工列表和资金
                            try {
                                onEmployeesUpdate(updatedEmployees)
                                onMoneyUpdate(money - trainingCost)
                                android.util.Log.d("EmployeeManagement", "培训成功: 员工 $employeeName 技能已提升")
                            } catch (e: Exception) {
                                android.util.Log.e("EmployeeManagement", "更新状态时异常: ${e.message}", e)
                                e.printStackTrace()
                            }
                            
                            // 关闭对话框
                            showTrainingDialog = false
                            selectedEmployee = null
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EmployeeManagement", "培训员工时发生未捕获异常", e)
                    e.printStackTrace()
                    // 确保对话框关闭
                    try {
                        showTrainingDialog = false
                        selectedEmployee = null
                    } catch (e2: Exception) {
                        android.util.Log.e("EmployeeManagement", "关闭对话框时异常", e2)
                    }
                }
            },
            onDismiss = {
                try {
                    showTrainingDialog = false
                    selectedEmployee = null
                } catch (e: Exception) {
                    android.util.Log.e("EmployeeManagement", "关闭培训对话框时异常", e)
                }
            }
        )
    }
    
    // 解雇对话框
    if (showFireDialog && selectedEmployee != null) {
        EnhancedFireDialog(
            employee = selectedEmployee!!,
            currentYear = currentYear,
            currentMonth = currentMonth,
            currentDay = currentDay,
            currentMoney = money,
            onConfirm = {
                // 计算赔偿金额
                val severancePay = selectedEmployee!!.calculateSeverancePay(currentYear, currentMonth, currentDay)
                
                // 扣除赔偿金额
                onMoneyUpdate(money - severancePay)
                
                // 移除员工
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
    
    // 人才市场对话框（岗位发布系统版本）
    if (showTalentMarketDialog) {
        val currentSaveData = remember(money, allEmployees, currentYear, currentMonth, currentDay) {
            com.example.yjcy.data.SaveData(
                money = money,
                allEmployees = allEmployees.toList(),
                currentYear = currentYear,
                currentMonth = currentMonth,
                currentDay = currentDay
            )
        }
        
        NewTalentMarketDialog(
            saveData = currentSaveData,
            onDismiss = { showTalentMarketDialog = false },
            onRecruitCandidate = { candidate ->
                try {
                    // 检查员工数量限制
                    if (allEmployees.size >= 30) {
                        android.util.Log.w("EmployeeManagement", "员工数量已达上限")
                        return@NewTalentMarketDialog
                    }
                    
                    // 计算招聘费用（基础费用 + 技能加成）
                    val baseFee = 5000L
                    val skillBonus = candidate.getMaxSkillLevel() * 2000L
                    val recruitmentFee = baseFee + skillBonus
                    
                    // 检查资金是否足够
                    if (money < recruitmentFee) {
                        android.util.Log.w("EmployeeManagement", "资金不足")
                        return@NewTalentMarketDialog
                    }
                    
                    // 生成新员工ID
                    val maxId = allEmployees.maxOfOrNull { it.id } ?: 0
                    val newId = maxOf(1, maxId + 1)
                    
                    // 将候选人转换为员工
                    val newEmployee = candidate.toEmployee(
                        newId = newId,
                        hireYear = currentYear,
                        hireMonth = currentMonth,
                        hireDay = currentDay
                    )
                    
                    // 更新员工列表和资金
                    val updatedEmployees = allEmployees + newEmployee
                    onEmployeesUpdate(updatedEmployees)
                    onMoneyUpdate(money - recruitmentFee)
                    
                    android.util.Log.d("EmployeeManagement", "成功招聘 ${candidate.name}，花费 ¥$recruitmentFee")
                } catch (e: Exception) {
                    android.util.Log.e("EmployeeManagement", "招聘员工时发生异常", e)
                    e.printStackTrace()
                }
            },
            jobPostingRefreshTrigger = jobPostingRefreshTrigger
        )
    }
}


@Composable
fun EmployeeStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.25f),
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
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
    // 缓存计算结果，避免重复计算
    // 使用employee的具体属性作为key，确保属性变化时重新计算
    val specialtySkillType = remember(employee.id, employee.position, employee.skillDevelopment, employee.skillDesign, employee.skillArt, employee.skillMusic, employee.skillService) { 
        employee.getSpecialtySkillType() 
    }
    val specialtySkillLevel = remember(employee.id, employee.skillDevelopment, employee.skillDesign, employee.skillArt, employee.skillMusic, employee.skillService) { 
        employee.getSpecialtySkillLevel() 
    }
    val stamina = remember(employee.id, employee.stamina) { 
        employee.getStaminaPercentage() 
    }
    val loyalty = remember(employee.id, employee.loyalty, employee.isFounder) { 
        if (!employee.isFounder) employee.getLoyaltyPercentage() else 0 
    }
    
    val staminaColor = remember(stamina) {
        when {
            stamina >= 70 -> Color(0xFF10B981)
            stamina >= 30 -> Color(0xFFF59E0B)
            else -> Color(0xFFEF4444)
        }
    }
    
    val loyaltyColor = remember(loyalty) {
        when {
            loyalty >= 70 -> Color(0xFF10B981)
            loyalty >= 30 -> Color(0xFFF59E0B)
            else -> Color(0xFFEF4444)
        }
    }
    
    val (grade, gradeColor) = remember(specialtySkillLevel) {
        when {
            specialtySkillLevel >= 5 -> "S" to Color(0xFF10B981)
            specialtySkillLevel >= 4 -> "A" to Color(0xFF3B82F6)
            specialtySkillLevel >= 3 -> "B" to Color(0xFFF59E0B)
            else -> "C" to Color(0xFFEF4444)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .padding(vertical = 16.dp, horizontal = 12.dp)
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
                            text = "${specialtySkillType}技能：${specialtySkillLevel}级",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // 技能等级星星
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < specialtySkillLevel) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = if (index < specialtySkillLevel) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 体力值快速指示器
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "体力值",
                            tint = staminaColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "体力：$stamina%",
                            fontSize = 11.sp,
                            color = staminaColor.copy(alpha = 0.9f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 忠诚度快速指示器
                    if (!employee.isFounder) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "忠诚度",
                                tint = loyaltyColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "忠诚：$loyalty%",
                                fontSize = 11.sp,
                                color = loyaltyColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // 员工等级徽章（基于专属技能等级）- 简化设计
                    Text(
                        text = grade,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor,
                        modifier = Modifier
                            .background(
                                color = gradeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "¥${employee.salary}/月",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            // 操作按钮（创始人不显示培训和解雇按钮，5级员工不显示培训按钮）
            if (!employee.isFounder) {
                Spacer(modifier = Modifier.height(12.dp))
                
                val canTrain = specialtySkillLevel < 5
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canTrain) {
                        ModernButton(
                            text = "培训",
                            icon = Icons.Default.School,
                            onClick = onTrainClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6)
                            )
                        )
                    }
                    
                    ModernButton(
                        text = "解雇",
                        icon = Icons.Default.PersonRemove,
                        onClick = onFireClick,
                        modifier = if (canTrain) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.fillMaxWidth()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    )
                }
            }
        
        // 底部分隔线
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun StaminaBar(
    stamina: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "体力",
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
                    .fillMaxWidth(stamina / 100f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = when {
                                stamina >= 70 -> listOf(Color(0xFF10B981), Color(0xFF34D399)) // 绿色：体力充足
                                stamina >= 30 -> listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)) // 黄色：体力中等
                                else -> listOf(Color(0xFFEF4444), Color(0xFFF87171)) // 红色：体力不足
                            }
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
        
        Text(
            text = "$stamina%",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                stamina >= 70 -> Color(0xFF10B981)
                stamina >= 30 -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            },
            modifier = Modifier.padding(start = 8.dp)
        )
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
    /**
     * 计算培训费用（梯度费用）
     * 根据员工当前技能等级计算：
     * - 1级：月薪 × 1.5（基础培训）
     * - 2级：月薪 × 2.0（进阶培训）
     * - 3级：月薪 × 2.5（高级培训）
     * - 4级：月薪 × 3.0（专家培训）
     * - 5级：无法培训（已达最高等级）
     */
    // 安全检查：确保员工数据有效
    val isValidEmployee = try {
        employee.name.isNotBlank()
    } catch (e: Exception) {
        android.util.Log.e("EnhancedTrainingDialog", "员工数据无效", e)
        false
    }
    
    // 如果员工数据无效，显示错误对话框
    if (!isValidEmployee) {
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
                    Text(
                        text = "错误",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "员工数据无效，无法进行培训",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
        return
    }
    
    val safeEmployee = employee
    val currentSkillLevel = try {
        safeEmployee.getSpecialtySkillLevel().coerceIn(0, 5)
    } catch (e: Exception) {
        android.util.Log.e("EnhancedTrainingDialog", "获取技能等级失败", e)
        0
    }
    
    val safeSalary = safeEmployee.salary.coerceAtLeast(0)
    val trainingCost = try {
        when {
            currentSkillLevel >= 5 -> 0L // 已达最高等级，无法培训
            currentSkillLevel == 4 -> (safeSalary * 3.0).toLong().coerceAtLeast(0L) // 4级→5级：3倍月薪
            currentSkillLevel == 3 -> (safeSalary * 2.5).toLong().coerceAtLeast(0L) // 3级→4级：2.5倍月薪
            currentSkillLevel == 2 -> (safeSalary * 2.0).toLong().coerceAtLeast(0L) // 2级→3级：2倍月薪
            else -> (safeSalary * 1.5).toLong().coerceAtLeast(0L) // 1级→2级：1.5倍月薪
        }
    } catch (e: Exception) {
        android.util.Log.e("EnhancedTrainingDialog", "计算培训费用失败", e)
        0L
    }
    
    val specialtySkillType = try {
        safeEmployee.getSpecialtySkillType()
    } catch (e: Exception) {
        android.util.Log.e("EnhancedTrainingDialog", "获取技能类型失败", e)
        "通用"
    }
    
    val canTrain = currentSkillLevel < 5 // 未达最高等级才能培训
    val canAfford = money >= trainingCost
    val canProceed = canTrain && canAfford
    
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
                    text = "为 ${safeEmployee.name} 提供培训",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "当前${specialtySkillType}技能: ${currentSkillLevel}级",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                if (canTrain) {
                    Text(
                        text = "培训后等级: ${currentSkillLevel + 1}级",
                        fontSize = 14.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Text(
                    text = if (canTrain) "培训费用: ¥$trainingCost" else "已达最高等级，无法培训",
                    fontSize = 14.sp,
                    color = when {
                        !canTrain -> Color(0xFFEF4444)
                        !canAfford -> Color(0xFFEF4444)
                        else -> Color.White
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (!canTrain) {
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
                            text = "员工已达到最高技能等级！",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                } else if (!canAfford) {
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
                        enabled = canProceed,
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
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    currentMoney: Long,
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
                
                // 计算赔偿信息
                val workMonths = employee.calculateWorkMonths(currentYear, currentMonth, currentDay)
                val workYears = (workMonths + 11) / 12
                val severancePay = employee.calculateSeverancePay(currentYear, currentMonth, currentDay)
                val compensationMonths = 2 * workYears + 1
                
                // 赔偿信息卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF374151)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💰 解雇赔偿详情",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "月薪：",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "¥${employee.salary}",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "工作时长：",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${workYears}年${workMonths % 12}个月 (${workMonths}个月)",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "赔偿公式：",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "2N+1 = ${compensationMonths}个月工资",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                        
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "赔偿金额：",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "¥$severancePay",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
                
                // 资金不足警告
                if (currentMoney < severancePay) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "资金不足",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "资金不足，解雇后将负债！",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
                
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