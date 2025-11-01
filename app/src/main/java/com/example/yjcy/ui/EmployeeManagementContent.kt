package com.example.yjcy.ui

import com.example.yjcy.data.SkillConstants

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.yjcy.data.JobPostingStatus
import com.example.yjcy.data.ApplicantStatus
import com.example.yjcy.data.WorkSchedule
import com.example.yjcy.ui.components.NewTalentMarketDialog
import com.example.yjcy.utils.formatMoney
import com.example.yjcy.service.JobPostingService

@Composable
fun EmployeeManagementContent(
    allEmployees: List<Employee>,
    onEmployeesUpdate: (List<Employee>) -> Unit,
    money: Long,
    onMoneyUpdate: (Long) -> Unit,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    currentMinuteOfDay: Int = 0, // 当天内的分钟数（0-1439）
    @Suppress("UNUSED_PARAMETER") onNavigateToTalentMarket: () -> Unit = {},
    jobPostingRefreshTrigger: Int = 0, // 用于触发应聘者数据刷新
    onPauseGame: (() -> Unit)? = null, // 暂停游戏的回调
    onResumeGame: (() -> Unit)? = null // 恢复游戏的回调
) {
    var showTrainingDialog by remember { mutableStateOf(false) }
    var showFireDialog by remember { mutableStateOf(false) }
    var showTalentMarketDialog by remember { mutableStateOf(false) }
    var showBatchTrainingDialog by remember { mutableStateOf(false) }
    var showWorkScheduleDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var filterType by remember { mutableStateOf("全部") }
    val listState = rememberLazyListState()
    
    // 计算当前星期几和时间
    val currentWeekday = remember(currentYear, currentMonth, currentDay) {
        com.example.yjcy.utils.calculateWeekday(currentYear, currentMonth, currentDay)
    }
    val currentHour = remember(currentMinuteOfDay) { currentMinuteOfDay / 60 }
    val currentMinute = remember(currentMinuteOfDay) { currentMinuteOfDay % 60 }
    
    // 检查是否有待处理的应聘者
    val jobPostingService = remember { JobPostingService.getInstance() }
    val hasPendingApplicants by remember(jobPostingRefreshTrigger) {
        derivedStateOf {
            jobPostingService.getAllJobPostings()
                .filter { it.status == JobPostingStatus.ACTIVE }
                .any { jobPosting ->
                    jobPosting.applicants.any { it.status == ApplicantStatus.PENDING }
                }
        }
    }
    
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
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👥 员工管理",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 一键培训按钮（现代化设计）
            Surface(
                modifier = Modifier
                    .height(36.dp)
                    .clickable { showBatchTrainingDialog = true },
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF2563EB)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "一键培训",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "一键培训",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
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
                value = "¥${formatMoney(totalSalary.toLong())}",
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
            
            // 工作时间设置按钮
            ModernButton(
                text = "工作时间",
                icon = Icons.Default.AccessTime,
                onClick = { showWorkScheduleDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                )
            )
            
            // 人才市场入口按钮（带红点提示）
            BadgeBox(
                showBadge = hasPendingApplicants,
                modifier = Modifier.weight(1f)
            ) {
                ModernButton(
                    text = "人才市场",
                    icon = Icons.Default.PersonAdd,
                    onClick = { 
                        showTalentMarketDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                )
            }
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
                        },
                        currentWeekday = currentWeekday,
                        currentHour = currentHour,
                        currentMinute = currentMinute
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
            onConfirm = { trainingCost: Long ->
                try {
                    android.util.Log.d("EmployeeManagement", "培训确认: 员工ID=$employeeId, 费用=$trainingCost, 当前资金=$money")
                    
                    // 再次检查员工是否存在
                    val employeeToTrain = allEmployees.find { it.id == employeeId }
                    if (employeeToTrain == null) {
                        android.util.Log.w("EmployeeManagement", "培训时员工不存在: ID=$employeeId, 名称=$employeeName")
                        showTrainingDialog = false
                        selectedEmployee = null
                        Unit
                    } else {
                        // 检查资金是否足够
                        if (money < trainingCost) {
                            android.util.Log.w("EmployeeManagement", "培训资金不足: 需要 $trainingCost，当前 $money")
                            showTrainingDialog = false
                            selectedEmployee = null
                            Unit
                        } else {
                            // 固定提升1级技能
                            val skillBoost = 1
                            val updatedEmployees = try {
                                allEmployees.map { emp ->
                                    if (emp.id == employeeId) {
                                        // 只提升专属技能
                                        try {
                                            when (val skillType = emp.getSpecialtySkillType()) {
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
                            Unit
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EmployeeManagement", "培训员工时发生未捕获异常", e)
                    e.printStackTrace()
                    // 确保对话框关闭
                    try {
                        showTrainingDialog = false
                        selectedEmployee = null
                        Unit
                    } catch (e2: Exception) {
                        android.util.Log.e("EmployeeManagement", "关闭对话框时异常", e2)
                    }
                }
            },
            onDismiss = {
                try {
                    showTrainingDialog = false
                    selectedEmployee = null
                    Unit
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
                Unit
            },
            onDismiss = {
                showFireDialog = false
                selectedEmployee = null
                Unit
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
            onDismiss = { showTalentMarketDialog = false; Unit },
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
    
    // 批量培训对话框
    if (showBatchTrainingDialog) {
        BatchTrainingDialog(
            employees = allEmployees,
            money = money,
            onConfirm = { totalCost: Long ->
                try {
                    val updatedEmployees = allEmployees.map { emp ->
                    val currentSkillLevel = try {
                        emp.getSpecialtySkillLevel().coerceIn(0, 5)
                    } catch (_: Exception) {
                        0
                    }
                        
                        if (currentSkillLevel < 5) {
                            // 提升专属技能1级
                            try {
                                when (emp.getSpecialtySkillType()) {
                                    "开发" -> emp.copy(
                                        skillDevelopment = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillDevelopment + 1)
                                    )
                                    "设计" -> emp.copy(
                                        skillDesign = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillDesign + 1)
                                    )
                                    "美工" -> emp.copy(
                                        skillArt = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillArt + 1)
                                    )
                                    "音乐" -> emp.copy(
                                        skillMusic = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillMusic + 1)
                                    )
                                    "服务" -> emp.copy(
                                        skillService = minOf(SkillConstants.MAX_SKILL_LEVEL, emp.skillService + 1)
                                    )
                                    else -> emp
                                }
                            } catch (_: Exception) {
                                emp
                            }
                        } else {
                            emp
                        }
                    }
                    
                    onEmployeesUpdate(updatedEmployees)
                    onMoneyUpdate(money - totalCost)
                    showBatchTrainingDialog = false
                    Unit
                } catch (e: Exception) {
                    android.util.Log.e("EmployeeManagement", "批量培训时发生异常", e)
                    showBatchTrainingDialog = false
                    Unit
                }
            },
            onDismiss = { showBatchTrainingDialog = false; Unit }
        )
    }
    
    // 工作时间设置对话框
    var showWorkScheduleSuccessDialog by remember { mutableStateOf(false) }
    
    // 监听对话框打开/关闭，控制游戏暂停
    LaunchedEffect(showWorkScheduleDialog, showWorkScheduleSuccessDialog) {
        when {
            // 工作时间设置对话框打开时暂停游戏
            showWorkScheduleDialog -> {
                onPauseGame?.invoke()
            }
            // 成功提示对话框打开时保持暂停
            showWorkScheduleSuccessDialog -> {
                // 保持暂停状态（不执行任何操作）
            }
            // 两个对话框都关闭时恢复游戏
            !showWorkScheduleDialog && !showWorkScheduleSuccessDialog -> {
                onResumeGame?.invoke()
            }
        }
    }
    
    if (showWorkScheduleDialog) {
        WorkScheduleDialog(
            employees = allEmployees,
            onConfirm = { updatedEmployees ->
                onEmployeesUpdate(updatedEmployees)
                showWorkScheduleDialog = false
                showWorkScheduleSuccessDialog = true
                Unit
            },
            onDismiss = { 
                showWorkScheduleDialog = false
                Unit
            },
            currentHour = currentHour,
            currentMinute = currentMinute
        )
    }
    
    // 工作时间设置成功提示对话框
    if (showWorkScheduleSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showWorkScheduleSuccessDialog = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "成功",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "设置成功",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Text(
                    text = "工作时间已成功应用到所有员工",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showWorkScheduleSuccessDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text("确定", color = Color.White)
                }
            },
            containerColor = Color(0xFF1F2937),
            titleContentColor = Color.White,
            textContentColor = Color.White
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
    onFireClick: () -> Unit,
    currentWeekday: Int = 1, // 当前星期几（1=周一，7=周日）
    currentHour: Int = 0, // 当前小时（0-23）
    currentMinute: Int = 0 // 当前分钟（0-59）
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
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 工作状态显示
                    val isWorking = remember(currentWeekday, currentHour, currentMinute, employee.workSchedule) {
                        employee.isWorking(currentWeekday, currentHour, currentMinute)
                    }
                    val workStatusColor = if (isWorking) Color(0xFF10B981) else Color(0xFF9CA3AF)
                    val workStatusText = if (isWorking) "工作中" else "休息中"
                    val workStatusIcon = if (isWorking) Icons.Default.Business else Icons.Default.Home
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = workStatusIcon,
                            contentDescription = workStatusText,
                            tint = workStatusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = workStatusText,
                            fontSize = 11.sp,
                            color = workStatusColor.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
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
                        text = "¥${formatMoney(employee.salary.toLong())}/月",
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

/**
 * 批量培训对话框
 */
@Composable
fun BatchTrainingDialog(
    employees: List<Employee>,
    money: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // 计算可培训员工列表和总费用
    val trainableEmployees = remember(employees) {
        employees.filter { emp ->
            try {
                val currentSkillLevel = emp.getSpecialtySkillLevel().coerceIn(0, 5)
                currentSkillLevel < 5
            } catch (_: Exception) {
                false
            }
        }
    }
    
    val totalCost = remember(trainableEmployees) {
        trainableEmployees.sumOf { emp ->
            try {
                val currentSkillLevel = emp.getSpecialtySkillLevel().coerceIn(0, 5)
                val safeSalary = emp.salary.coerceAtLeast(0)
                when {
                    currentSkillLevel >= 5 -> 0L
                    currentSkillLevel == 4 -> (safeSalary * 3.0).toLong()
                    currentSkillLevel == 3 -> (safeSalary * 2.5).toLong()
                    currentSkillLevel == 2 -> (safeSalary * 2.0).toLong()
                    else -> (safeSalary * 1.5).toLong()
                }
            } catch (_: Exception) {
                0L
            }
        }
    }
    
    val canAfford = money >= totalCost
    
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
                    contentDescription = "批量培训",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "一键培训",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "将为所有可培训的员工进行培训",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 统计信息
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "可培训员工数：",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${trainableEmployees.size}/${employees.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "总培训费用：",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "¥${formatMoney(totalCost)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "当前资金：",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "¥${formatMoney(money)}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                if (!canAfford) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "资金不足",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "资金不足，无法进行批量培训",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
                
                if (trainableEmployees.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "所有员工已达到最高技能等级",
                        fontSize = 12.sp,
                        color = Color(0xFFF59E0B)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                        onClick = { onConfirm(totalCost) },
                        modifier = Modifier.weight(1f),
                        enabled = canAfford && trainableEmployees.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
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
    
    val currentSkillLevel = try {
        employee.getSpecialtySkillLevel().coerceIn(0, 5)
    } catch (e: Exception) {
        android.util.Log.e("EnhancedTrainingDialog", "获取技能等级失败", e)
        0
    }
    
    val safeSalary = employee.salary.coerceAtLeast(0)
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
        employee.getSpecialtySkillType()
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
                    text = "为 ${employee.name} 提供培训",
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
                                text = "¥${formatMoney(employee.salary.toLong())}",
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

/**
 * 工作时间设置对话框
 */
@Composable
fun WorkScheduleDialog(
    employees: List<Employee>,
    onConfirm: (List<Employee>) -> Unit,
    onDismiss: () -> Unit,
    currentHour: Int = 0, // 当前游戏时间（小时）
    currentMinute: Int = 0 // 当前游戏时间（分钟）
) {
    // 从员工列表中获取现有工作时间设置（使用第一个员工的工作时间，因为所有员工应该使用相同的设置）
    val existingSchedule = remember(employees) {
        employees.firstOrNull()?.workSchedule ?: WorkSchedule()
    }
    
    // 初始化状态，使用默认值
    var workDays by remember { mutableStateOf(existingSchedule.workDays) }
    var startHour by remember { mutableIntStateOf(existingSchedule.startHour) }
    var endHour by remember { mutableIntStateOf(existingSchedule.endHour) }
    
    // 当员工列表变化时，更新状态以反映最新的工作时间设置
    LaunchedEffect(employees) {
        val schedule = employees.firstOrNull()?.workSchedule ?: WorkSchedule()
        workDays = schedule.workDays
        startHour = schedule.startHour
        endHour = schedule.endHour
    }
    
    // 分钟固定为0
    val startMinute = 0
    val endMinute = 0
    
    // 计算当前时间的总分钟数（用于比较）
    val currentTotalMinutes = currentHour * 60 + currentMinute
    
    // 辅助函数：判断设置的时间是否早于当前时间
    fun isTimeBeforeCurrent(hour: Int): Boolean {
        val totalMinutes = hour * 60
        return totalMinutes < currentTotalMinutes
    }
    
    // 获取时间的最小值（不能低于当前时间）
    fun getMinHour(hour: Int): Float {
        val totalMinutes = hour * 60
        return if (totalMinutes < currentTotalMinutes) {
            currentHour.toFloat()
        } else {
            0f
        }
    }
    
    // 确保初始值不低于当前时间
    LaunchedEffect(currentHour, currentMinute) {
        if (isTimeBeforeCurrent(startHour)) {
            startHour = currentHour
        }
        if (isTimeBeforeCurrent(endHour)) {
            endHour = maxOf(currentHour, startHour)
        }
    }
    
    // 验证所有时间设置是否有效
    val isValidTime = remember(startHour, endHour, currentHour, currentMinute) {
        val currentTotal = currentHour * 60 + currentMinute
        val startTotal = startHour * 60
        val endTotal = endHour * 60
        
        // 所有时间都不能早于当前时间
        val allAfterCurrent = startTotal >= currentTotal && 
                               endTotal >= currentTotal
        
        // 时间逻辑关系：下班 > 上班
        val logicalOrder = endTotal > startTotal
        
        allAfterCurrent && logicalOrder
    }
    
    val weekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⏰ 工作时间设置",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 工作日选择
                Text(
                    text = "工作日：",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // 格式化工作日范围显示和完整文本
                val formattedScheduleText = remember(workDays, startHour, endHour) {
                    val workDaysTextResult = when {
                        workDays.isEmpty() -> "未设置"
                        workDays.size == 7 -> "每天"
                        else -> {
                            val sortedDays = workDays.sorted()
                            val ranges = mutableListOf<String>()
                            var start = sortedDays[0]
                            var end = sortedDays[0]
                            
                            for (i in 1 until sortedDays.size) {
                                if (sortedDays[i] == end + 1) {
                                    end = sortedDays[i]
                                } else {
                                    if (start == end) {
                                        ranges.add(weekdayNames[start - 1])
                                    } else {
                                        ranges.add("${weekdayNames[start - 1]}-${weekdayNames[end - 1]}")
                                    }
                                    start = sortedDays[i]
                                    end = sortedDays[i]
                                }
                            }
                            if (start == end) {
                                ranges.add(weekdayNames[start - 1])
                            } else {
                                ranges.add("${weekdayNames[start - 1]}-${weekdayNames[end - 1]}")
                            }
                            ranges.joinToString("、")
                        }
                    }
                    val timeTextResult = "${startHour.toString().padStart(2, '0')}:00-${endHour.toString().padStart(2, '0')}:00"
                    workDaysTextResult + "丨" + timeTextResult
                }
                
                // 显示当前设置的工作日和上下班时间
                Text(
                    text = formattedScheduleText,
                    fontSize = 14.sp,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weekdayNames.forEachIndexed { index, name ->
                        val day = index + 1
                        val isSelected = day in workDays
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clickable {
                                    workDays = if (isSelected) {
                                        workDays - day
                                    } else {
                                        workDays + day
                                    }
                                }
                                .background(
                                    color = if (isSelected) 
                                        Color(0xFF3B82F6) else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                
                // 上班时间
                Text(
                    text = "上班时间：",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // 显示当前时间提示
                if (isTimeBeforeCurrent(startHour)) {
                    Text(
                        text = "⚠️ 时间不能早于当前时间（${currentHour.toString().padStart(2, '0')}:${currentMinute.toString().padStart(2, '0')}）",
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("时", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Slider(
                        value = startHour.toFloat(),
                        onValueChange = { 
                            val newHour = it.toInt()
                            if (newHour * 60 >= currentTotalMinutes) {
                                startHour = newHour
                            } else {
                                startHour = currentHour
                            }
                        },
                        valueRange = getMinHour(startHour)..23f,
                        steps = (23 - getMinHour(startHour).toInt()).coerceAtLeast(0),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${startHour.toString().padStart(2, '0')}:00", 
                        fontSize = 14.sp, color = Color.White, modifier = Modifier.width(50.dp))
                }
                
                // 下班时间
                Text(
                    text = "下班时间：",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // 显示当前时间提示
                if (isTimeBeforeCurrent(endHour)) {
                    Text(
                        text = "⚠️ 时间不能早于当前时间（${currentHour.toString().padStart(2, '0')}:${currentMinute.toString().padStart(2, '0')}）",
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("时", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Slider(
                        value = endHour.toFloat(),
                        onValueChange = { 
                            val newHour = it.toInt()
                            if (newHour * 60 >= currentTotalMinutes) {
                                endHour = newHour
                            } else {
                                endHour = maxOf(currentHour, startHour)
                            }
                        },
                        valueRange = getMinHour(endHour)..23f,
                        steps = (23 - getMinHour(endHour).toInt()).coerceAtLeast(0),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${endHour.toString().padStart(2, '0')}:00", 
                        fontSize = 14.sp, color = Color.White, modifier = Modifier.width(50.dp))
                }
                
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val workSchedule = WorkSchedule(
                        workDays = workDays,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute
                    )
                    val updatedEmployees = employees.map { it.copy(workSchedule = workSchedule) }
                    onConfirm(updatedEmployees)
                },
                enabled = isValidTime,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isValidTime) Color(0xFF3B82F6) else Color.Gray,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text("应用到所有员工", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White)
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}