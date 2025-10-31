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
import com.example.yjcy.ui.components.NewTalentMarketDialog
import com.example.yjcy.service.JobPostingService
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
    
    // 获取待处理的应聘者数量
    val jobPostingService = remember { JobPostingService.getInstance() }
    val pendingApplicantsCount = remember { mutableIntStateOf(jobPostingService.getTotalPendingApplicants()) }
    
    // 刷新应聘者数量 - 监听刷新触发器和对话框打开状态
    LaunchedEffect(showTalentMarketDialog, jobPostingRefreshTrigger) {
        pendingApplicantsCount.value = jobPostingService.getTotalPendingApplicants()
    }
    
    // 过滤员工列表 - 使用 derivedStateOf 以正确响应 mutableStateListOf 的变化
    val filteredEmployees by remember {
        derivedStateOf {
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
        
        // 员工统计信息 - 无卡片设计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
            
            // 人才市场入口按钮（带红点提示）
            BadgeBox(
                showBadge = pendingApplicantsCount.value > 0,
                badgeCount = null, // 只显示红点，不显示数字
                modifier = Modifier.weight(1f)
            ) {
                ModernButton(
                    text = "人才市场",
                    icon = Icons.Default.PersonAdd,
                    onClick = { 
                        showTalentMarketDialog = true
                        pendingApplicantsCount.value = jobPostingService.getTotalPendingApplicants()
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
                        // 固定提升1级技能
                        val skillBoost = 1
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
    
    // 新版人才市场（岗位发布系统）弹出式对话框
    if (showTalentMarketDialog) {
        // 动态创建 SaveData，确保始终使用最新数据
        // 不使用 remember 缓存，避免数据不一致
        val currentSaveData = com.example.yjcy.data.SaveData(
            money = money,
            allEmployees = allEmployees.toList() // 每次都获取最新的员工列表
        )
        
        NewTalentMarketDialog(
            saveData = currentSaveData,
            onDismiss = { showTalentMarketDialog = false },
            jobPostingRefreshTrigger = jobPostingRefreshTrigger,
            onRecruitCandidate = { candidate ->
                try {
                    android.util.Log.d("EmployeeManagement", "收到雇佣请求: ${candidate.name}, 职位: ${candidate.position}")
                    
                    // 检查候选人的必要字段
                    if (candidate.name.isBlank()) {
                        android.util.Log.e("EmployeeManagement", "候选人姓名为空，无法雇佣")
                        return@NewTalentMarketDialog
                    }
                    
                    if (candidate.position.isBlank()) {
                        android.util.Log.e("EmployeeManagement", "候选人职位为空，无法雇佣")
                        return@NewTalentMarketDialog
                    }
                    
                    // 检查员工数量限制
                    if (allEmployees.size >= 30) {
                        android.util.Log.w("EmployeeManagement", "员工数量已达上限（${allEmployees.size}/30），无法继续招聘")
                        return@NewTalentMarketDialog
                    }
                    
                    // 生成新员工ID - 优化：只遍历一次列表
                    val maxId = allEmployees.maxOfOrNull { it.id } ?: 0
                    val newId = maxOf(1, maxId + 1)
                    
                    // 创建员工对象（减少重复检查）
                    val newEmployee = try {
                        android.util.Log.d("EmployeeManagement", "开始创建员工对象: ID=$newId, 候选人=${candidate.name}")
                        
                        val emp = candidate.toEmployee(
                            newId = newId,
                            hireYear = currentYear,
                            hireMonth = currentMonth,
                            hireDay = currentDay
                        )
                        
                        android.util.Log.d("EmployeeManagement", "员工对象创建成功: ${emp.name}")
                        emp
                    } catch (e: Exception) {
                        android.util.Log.e("EmployeeManagement", "toEmployee转换失败", e)
                        e.printStackTrace()
                        return@NewTalentMarketDialog
                    }
                    
                    // 验证员工对象是否有效
                    if (newEmployee.id <= 0 || newEmployee.name.isBlank()) {
                        android.util.Log.e("EmployeeManagement", "创建的员工对象无效")
                        return@NewTalentMarketDialog
                    }
                    
                    // 创建新列表并添加员工（只遍历一次）
                    val updatedEmployees = ArrayList<Employee>(allEmployees.size + 1)
                    updatedEmployees.addAll(allEmployees)
                    updatedEmployees.add(newEmployee)
                    
                    // 更新员工列表
                    try {
                        onEmployeesUpdate(updatedEmployees)
                        android.util.Log.d("EmployeeManagement", "成功更新员工列表，当前员工数: ${updatedEmployees.size}")
                    } catch (e: Exception) {
                        android.util.Log.e("EmployeeManagement", "更新员工列表失败", e)
                        e.printStackTrace()
                        return@NewTalentMarketDialog
                    }
                    
                    // 扣除招聘费用
                    try {
                        val recruitmentCost = candidate.expectedSalary.toLong() * 2L
                        val newMoney = maxOf(0L, money - recruitmentCost)
                        onMoneyUpdate(newMoney)
                        android.util.Log.d("EmployeeManagement", "扣除招聘费用: ¥$recruitmentCost，剩余资金: ¥$newMoney")
                    } catch (e: Exception) {
                        android.util.Log.e("EmployeeManagement", "更新资金失败", e)
                        e.printStackTrace()
                    }
                    
                    // 不要立即关闭对话框，让用户可以继续招聘
                    // showTalentMarketDialog = false
                } catch (e: Exception) {
                    // 捕获所有异常，防止崩溃
                    android.util.Log.e("EmployeeManagement", "雇佣员工时发生未捕获的异常", e)
                    e.printStackTrace()
                }
            }
        )
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
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 体力值快速指示器
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stamina = employee.getStaminaPercentage()
                        val staminaColor = when {
                            stamina >= 70 -> Color(0xFF10B981)
                            stamina >= 30 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }
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
                            val loyalty = employee.getLoyaltyPercentage()
                            val loyaltyColor = when {
                                loyalty >= 70 -> Color(0xFF10B981)
                                loyalty >= 30 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
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
                    val specialtyLevel = employee.getSpecialtySkillLevel()
                    val (grade, gradeColor) = when {
                        specialtyLevel >= 5 -> "S" to Color(0xFF10B981)
                        specialtyLevel >= 4 -> "A" to Color(0xFF3B82F6)
                        specialtyLevel >= 3 -> "B" to Color(0xFFF59E0B)
                        else -> "C" to Color(0xFFEF4444)
                    }
                    
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
                
                val canTrain = employee.getSpecialtySkillLevel() < 5
                
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
                        modifier = Modifier.weight(if (canTrain) 1f else 0f).then(
                            if (!canTrain) Modifier.fillMaxWidth() else Modifier
                        ),
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
    val currentSkillLevel = employee.getSpecialtySkillLevel()
    val trainingCost = when {
        currentSkillLevel >= 5 -> 0L // 已达最高等级，无法培训
        currentSkillLevel == 4 -> (employee.salary * 3.0).toLong() // 4级→5级：3倍月薪
        currentSkillLevel == 3 -> (employee.salary * 2.5).toLong() // 3级→4级：2.5倍月薪
        currentSkillLevel == 2 -> (employee.salary * 2.0).toLong() // 2级→3级：2倍月薪
        else -> (employee.salary * 1.5).toLong() // 1级→2级：1.5倍月薪
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
                    text = "当前${employee.getSpecialtySkillType()}技能: ${employee.getSpecialtySkillLevel()}级",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                if (canTrain) {
                    Text(
                        text = "培训后等级: ${employee.getSpecialtySkillLevel() + 1}级",
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