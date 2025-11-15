package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.TopInfoBar
import com.example.yjcy.data.Subsidiary
import com.example.yjcy.data.SubsidiaryStatus
import com.example.yjcy.data.DevelopmentPreference
import com.example.yjcy.data.GameUpdateStrategy
import com.example.yjcy.data.SubsidiaryGameConfig
import com.example.yjcy.data.OnlineGamePricing
import com.example.yjcy.data.CompetitorGame
import com.example.yjcy.data.DevelopingGame
import com.example.yjcy.data.getRecommendedPrice
import com.example.yjcy.data.DevelopmentPhase
import com.example.yjcy.data.Employee
import com.example.yjcy.data.GameDate
import com.example.yjcy.data.SubsidiaryManager
import com.example.yjcy.data.SubsidiaryJobPosting
import com.example.yjcy.data.SubsidiaryApplicant
import com.example.yjcy.data.SubsidiaryApplicantStatus
import com.example.yjcy.ui.components.SingleLineText
import com.example.yjcy.ui.components.MultiLineText
import com.example.yjcy.utils.formatMoney

/**
 * 子公司管理主界面（全屏布局，参考GVA样式）
 */
@Composable
fun SubsidiaryManagementScreen(
    subsidiaries: List<Subsidiary>,
    onSubsidiaryUpdate: (Subsidiary) -> Unit = {},
    onDismiss: () -> Unit = {},
    // TopInfoBar参数
    money: Long = 0,
    fans: Long = 0,
    year: Int = 1,
    month: Int = 1,
    day: Int = 1,
    gameSpeed: Int = 1,
    onSpeedChange: (Int) -> Unit = {},
    onPauseToggle: () -> Unit = {},
    isPaused: Boolean = false,
    onSettingsClick: () -> Unit = {},
    isSupporterUnlocked: Boolean = false,
    onShowFeatureLockedDialog: () -> Unit = {}
) {
    var selectedSubsidiary by remember { mutableStateOf<Subsidiary?>(null) }
    var showManagementDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF7C3AED)
                    )
                )
            )
    ) {
        // 顶部状态栏
        TopInfoBar(
            money = money,
            fans = fans,
            year = year,
            month = month,
            day = day,
            gameSpeed = gameSpeed,
            onSpeedChange = onSpeedChange,
            onPauseToggle = onPauseToggle,
            isPaused = isPaused,
            onSettingsClick = onSettingsClick,
            isSupporterUnlocked = isSupporterUnlocked,
            onShowFeatureLockedDialog = onShowFeatureLockedDialog
        )
        
        // 主内容区（深色背景）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a2e))
        ) {
            // 顶部标题栏
            SubsidiaryTopBar(onBack = onDismiss)
        
        // 标签页
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF16213e),
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { SingleLineText(text = "子公司列表", fontSize = 14.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { SingleLineText(text = "财务概览", fontSize = 14.sp) }
            )
        }
        
        // 内容区域
        when (selectedTab) {
            0 -> SubsidiaryListTab(
                subsidiaries = subsidiaries,
                onSubsidiaryUpdate = onSubsidiaryUpdate,
                year = year,
                month = month,
                day = day
            )
            1 -> FinancialOverviewTab(subsidiaries = subsidiaries)
        }
        }
    }
    
    // 子公司管理对话框
    if (showManagementDialog && selectedSubsidiary != null) {
        SubsidiaryManagementDialog(
            subsidiary = selectedSubsidiary!!,
            onDismiss = { 
                showManagementDialog = false
                selectedSubsidiary = null
            },
            onSubsidiaryUpdate = onSubsidiaryUpdate
        )
    }
}

/**
 * 顶部标题栏
 */
@Composable
private fun SubsidiaryTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0f3460))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            SingleLineText(text = "← 返回", color = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        SingleLineText(
            text = "🏭 子公司管理",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}

/**
 * 子公司列表标签页
 */
@Composable
private fun SubsidiaryListTab(
    subsidiaries: List<Subsidiary>,
    onSubsidiaryUpdate: (Subsidiary) -> Unit,
    year: Int = 1,
    month: Int = 1,
    day: Int = 1
) {
    var selectedSubsidiary by remember { mutableStateOf<Subsidiary?>(null) }
    var showGameManagement by remember { mutableStateOf(false) }
    var showDevConfig by remember { mutableStateOf(false) }
    var showDevelopingGames by remember { mutableStateOf(false) }
    var showEmployeeManagement by remember { mutableStateOf(false) }
    
    if (subsidiaries.isEmpty()) {
        // 空状态
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SingleLineText(
                    text = "🏢",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                SingleLineText(
                    text = "暂无子公司",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                MultiLineText(
                    text = "收购竞争对手后将出现在这里",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subsidiaries) { subsidiary ->
                SubsidiaryCard(
                    subsidiary = subsidiary,
                    onGameManagementClick = {
                        selectedSubsidiary = subsidiary
                        showGameManagement = true
                    },
                    onDevConfigClick = {
                        selectedSubsidiary = subsidiary
                        showDevConfig = true
                    },
                    onDevelopingGamesClick = {
                        selectedSubsidiary = subsidiary
                        showDevelopingGames = true
                    },
                    onEmployeeManagementClick = {
                        selectedSubsidiary = subsidiary
                        showEmployeeManagement = true
                    }
                )
            }
        }
    }
    
    // 游戏管理对话框
    if (showGameManagement && selectedSubsidiary != null) {
        GameManagementOnlyDialog(
            subsidiary = selectedSubsidiary!!,
            onDismiss = { showGameManagement = false },
            onSubsidiaryUpdate = { updated ->
                onSubsidiaryUpdate(updated)
                showGameManagement = false
            }
        )
    }
    
    // 开发配置对话框
    if (showDevConfig && selectedSubsidiary != null) {
        DevConfigOnlyDialog(
            subsidiary = selectedSubsidiary!!,
            onDismiss = { showDevConfig = false },
            onSubsidiaryUpdate = { updated ->
                onSubsidiaryUpdate(updated)
                showDevConfig = false
            }
        )
    }
    
    // 正在开发对话框
    if (showDevelopingGames && selectedSubsidiary != null) {
        DevelopingGamesDialog(
            subsidiary = selectedSubsidiary!!,
            onDismiss = { showDevelopingGames = false }
        )
    }
    
    // 员工管理对话框
    if (showEmployeeManagement && selectedSubsidiary != null) {
        EmployeeManagementDialog(
            subsidiary = selectedSubsidiary!!,
            onDismiss = { showEmployeeManagement = false },
            onSubsidiaryUpdate = { updated ->
                onSubsidiaryUpdate(updated)
            },
            currentDate = GameDate(year, month, day)
        )
    }
}

/**
 * 财务概览标签页
 */
@Composable
private fun FinancialOverviewTab(subsidiaries: List<Subsidiary>) {
    var selectedSubsidiaryForView by remember { mutableStateOf<Subsidiary?>(null) }
    var showSubsidiarySelector by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 统计卡片
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "子公司总数",
                    value = "${subsidiaries.size}家",
                    modifier = Modifier.weight(1f)
                )
                val totalProfit = subsidiaries.sumOf { it.getProfitShare() }
                StatCard(
                    title = "月度分成",
                    value = formatMoney(totalProfit),
                    modifier = Modifier.weight(1f),
                    valueColor = if (totalProfit >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            }
        }
        
        item {
            // 收入详情卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16213e)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 标题行（带下拉选择器）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleLineText(
                            text = "📊 收入详情",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        // 下拉选择按钮
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .clickable { showSubsidiarySelector = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SingleLineText(
                                text = selectedSubsidiaryForView?.name ?: "全部",
                                fontSize = 13.sp,
                                color = Color(0xFF60A5FA)
                            )
                            SingleLineText(
                                text = "▼",
                                fontSize = 10.sp,
                                color = Color(0xFF60A5FA)
                            )
                        }
                    }
                    
                    // 根据选择显示数据
                    val displaySubsidiaries = selectedSubsidiaryForView?.let { listOf(it) } ?: subsidiaries
                    val totalRevenue = displaySubsidiaries.sumOf { it.monthlyRevenue }
                    val totalExpense = displaySubsidiaries.sumOf { it.monthlyExpense }
                    val totalProfit = displaySubsidiaries.sumOf { it.getMonthlyProfit() }
                    val totalProfitShare = displaySubsidiaries.sumOf { it.getProfitShare() }
                    
                    InfoRow("总月度收入", formatMoney(totalRevenue))
                    InfoRow("总月度支出", formatMoney(totalExpense))
                    InfoRow("总月度利润", formatMoney(totalProfit))
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SingleLineText(
                            text = "总利润分成",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        SingleLineText(
                            text = formatMoney(totalProfitShare),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
        
        item {
            // 市值统计卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16213e)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SingleLineText(
                        text = "💼 市值统计",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val totalAcquisitionPrice = subsidiaries.sumOf { it.acquisitionPrice }
                    val totalMarketValue = subsidiaries.sumOf { it.marketValue }
                    val totalAppreciation = totalMarketValue - totalAcquisitionPrice
                    val appreciationRate = if (totalAcquisitionPrice > 0) {
                        (totalAppreciation.toDouble() / totalAcquisitionPrice * 100).toInt()
                    } else 0
                    
                    InfoRow("总收购价格", formatMoney(totalAcquisitionPrice))
                    InfoRow("总当前市值", formatMoney(totalMarketValue))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SingleLineText(
                            text = "总增值",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        SingleLineText(
                            text = "${formatMoney(totalAppreciation)} (${if (appreciationRate >= 0) "+" else ""}${appreciationRate}%)",
                            color = if (totalAppreciation >= 0) Color(0xFF4CAF50) else Color(0xFFE57373),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
    
    // 子公司选择对话框
    if (showSubsidiarySelector) {
        Dialog(onDismissRequest = { showSubsidiarySelector = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1a1a2e)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 标题
                    SingleLineText(
                        text = "选择查看子公司",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 子公司列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "全部"选项
                        item {
                            SubsidiarySelectorItem(
                                name = "全部子公司",
                                isSelected = selectedSubsidiaryForView == null,
                                onClick = {
                                    selectedSubsidiaryForView = null
                                    showSubsidiarySelector = false
                                }
                            )
                        }
                        
                        // 各个子公司选项
                        items(subsidiaries) { subsidiary ->
                            SubsidiarySelectorItem(
                                name = subsidiary.name,
                                isSelected = selectedSubsidiaryForView?.id == subsidiary.id,
                                onClick = {
                                    selectedSubsidiaryForView = subsidiary
                                    showSubsidiarySelector = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 统计卡片
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SingleLineText(
                text = title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            SingleLineText(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

/**
 * 子公司卡片
 */
@Composable
private fun SubsidiaryCard(
    subsidiary: Subsidiary,
    onGameManagementClick: () -> Unit,
    onDevConfigClick: () -> Unit,
    onDevelopingGamesClick: () -> Unit,
    onEmployeeManagementClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部：logo和名称
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subsidiary.logo,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subsidiary.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "收购于 ${subsidiary.acquisitionDate.year}年${subsidiary.acquisitionDate.month}月",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                // 状态标签
                val (statusText, statusColor) = when (subsidiary.status) {
                    SubsidiaryStatus.ACTIVE -> "运营中" to Color(0xFF4CAF50)
                    SubsidiaryStatus.SUSPENDED -> "暂停" to Color(0xFFFFA726)
                    SubsidiaryStatus.LIQUIDATED -> "已清算" to Color(0xFFE57373)
                }
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = statusColor,
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 财务信息（并列显示）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "资金", value = formatMoney(subsidiary.cashBalance), valueColor = Color(0xFF64B5F6))
                InfoItem(label = "市值", value = formatMoney(subsidiary.marketValue))
                InfoItem(label = "月收入", value = formatMoney(subsidiary.monthlyRevenue))
                InfoItem(label = "月支出", value = formatMoney(subsidiary.monthlyExpense))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 利润分成
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 利润分成 (${(subsidiary.profitSharingRate * 100).toInt()}%)",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = formatMoney(subsidiary.getProfitShare()),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 管理按钮（两行）
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 第一行：游戏管理和开发配置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onGameManagementClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF64B5F6)
                        )
                    ) {
                        SingleLineText(text = "🎮 游戏管理", fontSize = 13.sp)
                    }
                    
                    OutlinedButton(
                        onClick = onDevConfigClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        )
                    ) {
                        SingleLineText(text = "⚙️ 开发配置", fontSize = 13.sp)
                    }
                }
                
                // 第二行：项目管理 和 员工管理
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDevelopingGamesClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFA726)
                        )
                    ) {
                        val devCount = subsidiary.developingGames.size
                        SingleLineText(
                            text = if (devCount > 0) "📋 项目管理 ($devCount)" else "📋 项目管理",
                            fontSize = 13.sp
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onEmployeeManagementClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF9C27B0)
                        )
                    ) {
                        SingleLineText(text = "👥 员工管理", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * 信息项
 */
@Composable
private fun InfoItem(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * 子公司菜单对话框（从底部弹出）
 */
@Composable
fun SubsidiaryMenuDialog(
    subsidiaries: List<Subsidiary>,
    onDismiss: () -> Unit,
    onSubsidiaryClick: (Subsidiary) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp) // 限制最大高度
                .background(
                    color = Color(0xFF1a1a2e),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(vertical = 16.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            // 标题
            Text(
                text = "🏭 子公司管理",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 子公司列表（可滚动）
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(subsidiaries) { subsidiary ->
                    SubsidiaryMenuItem(
                        subsidiary = subsidiary,
                        onClick = { onSubsidiaryClick(subsidiary) }
                    )
                }
            }
        }
    }
}

/**
 * 子公司菜单项
 */
@Composable
fun SubsidiaryMenuItem(
    subsidiary: Subsidiary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subsidiary.logo,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subsidiary.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                val profit = subsidiary.getMonthlyProfit()
                val profitColor = if (profit >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
                Text(
                    text = "月度利润: ${formatMoney(profit)} | ${subsidiary.games.size}款游戏",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            // 状态标签
            val (statusText, statusColor) = when (subsidiary.status) {
                SubsidiaryStatus.ACTIVE -> "运营中" to Color(0xFF4CAF50)
                SubsidiaryStatus.SUSPENDED -> "暂停" to Color(0xFFFFA726)
                SubsidiaryStatus.LIQUIDATED -> "已清算" to Color(0xFFE57373)
            }
            Text(
                text = statusText,
                fontSize = 11.sp,
                color = statusColor,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * 子公司管理对话框（游戏管理 + 开发配置）
 */
@Composable
fun SubsidiaryManagementDialog(
    subsidiary: Subsidiary,
    onDismiss: () -> Unit,
    onSubsidiaryUpdate: (Subsidiary) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1a1a2e)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subsidiary.logo,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        SingleLineText(
                            text = subsidiary.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Text(text = "✖", color = Color.White, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 标签页
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF16213e),
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { SingleLineText(text = "游戏管理", fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { SingleLineText(text = "开发配置", fontSize = 13.sp) }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 内容区域
                when (selectedTab) {
                    0 -> GameManagementTab(
                        subsidiary = subsidiary,
                        onSubsidiaryUpdate = onSubsidiaryUpdate
                    )
                    1 -> DevelopmentConfigTab(
                        subsidiary = subsidiary,
                        onSubsidiaryUpdate = onSubsidiaryUpdate
                    )
                }
            }
        }
    }
}

/**
 * 游戏管理标签页
 */
@Composable
private fun GameManagementTab(
    subsidiary: Subsidiary,
    onSubsidiaryUpdate: (Subsidiary) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (subsidiary.games.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SingleLineText(
                        text = "暂无游戏",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(subsidiary.games) { game ->
                GameManagementCard(
                    game = game,
                    subsidiary = subsidiary,
                    onSubsidiaryUpdate = onSubsidiaryUpdate
                )
            }
        }
    }
}

/**
 * 游戏管理卡片
 */
@Composable
private fun GameManagementCard(
    game: CompetitorGame,
    subsidiary: Subsidiary,
    onSubsidiaryUpdate: (Subsidiary) -> Unit
) {
    var showPriceDialog by remember { mutableStateOf(false) }
    var showStrategyDialog by remember { mutableStateOf(false) }
    
    val config = subsidiary.gameConfigs[game.id]
    val currentPrice = config?.customPrice
    val currentStrategy = config?.updateStrategy ?: GameUpdateStrategy.MODERATE
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213e)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 游戏名称和类型
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SingleLineText(
                        text = game.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleLineText(
                            text = when (game.businessModel) {
                                com.example.yjcy.ui.BusinessModel.ONLINE_GAME -> "网游"
                                com.example.yjcy.ui.BusinessModel.SINGLE_PLAYER -> "单机"
                            },
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        // 游戏主题
                        Text(
                            text = game.theme.displayName,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // 评分
                SingleLineText(
                    text = "%.1f".format(game.rating),
                    fontSize = 12.sp,
                    color = Color(0xFFFFD700)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 游戏数据展示
            val isOnlineGame = game.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (isOnlineGame) {
                    // 网游：显示活跃人数和总收入
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SingleLineText(
                            text = "活跃玩家",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        val activePlayersText = when {
                            game.activePlayers >= 100000000 -> "%.1f亿".format(game.activePlayers / 100000000.0)
                            game.activePlayers >= 10000 -> "%.1f万".format(game.activePlayers / 10000.0)
                            else -> "${game.activePlayers}"
                        }
                        SingleLineText(
                            text = activePlayersText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64B5F6)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SingleLineText(
                            text = "总收入",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        SingleLineText(
                            text = formatMoney(game.totalRevenue.toLong()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    // 单机：显示总销量
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SingleLineText(
                            text = "总销量",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        val salesText = when {
                            game.salesCount >= 100000000 -> "%.1f亿份".format(game.salesCount / 100000000.0)
                            game.salesCount >= 10000 -> "%.1f万份".format(game.salesCount / 10000.0)
                            else -> "${game.salesCount}份"
                        }
                        SingleLineText(
                            text = salesText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SingleLineText(
                            text = "总收入",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        SingleLineText(
                            text = formatMoney(game.totalRevenue.toLong()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 管理按钮
            
            if (isOnlineGame) {
                // 网游：显示价格设置和更新策略按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPriceDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SingleLineText(text = "价格设置", fontSize = 11.sp)
                            val hasCustomPrice = config?.onlineGamePricing != null
                            SingleLineText(
                                text = if (hasCustomPrice) "已自定义" else "使用默认",
                                fontSize = 10.sp,
                                color = if (hasCustomPrice) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { showStrategyDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SingleLineText(text = "更新", fontSize = 11.sp)
                            SingleLineText(
                                text = when (currentStrategy) {
                                    GameUpdateStrategy.AGGRESSIVE -> "激进"
                                    GameUpdateStrategy.MODERATE -> "适中"
                                    GameUpdateStrategy.CONSERVATIVE -> "保守"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF64B5F6)
                            )
                        }
                    }
                }
            } else {
                // 单机：显示价格和更新策略
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPriceDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SingleLineText(text = "价格", fontSize = 11.sp)
                            SingleLineText(
                                text = currentPrice?.let { "¥$it" } ?: "默认",
                                fontSize = 10.sp,
                                color = if (currentPrice != null) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { showStrategyDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SingleLineText(text = "更新", fontSize = 11.sp)
                            SingleLineText(
                                text = when (currentStrategy) {
                                    GameUpdateStrategy.AGGRESSIVE -> "激进"
                                    GameUpdateStrategy.MODERATE -> "适中"
                                    GameUpdateStrategy.CONSERVATIVE -> "保守"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF64B5F6)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 价格设置对话框
    if (showPriceDialog) {
        PriceSettingDialog(
            game = game,
            currentPrice = currentPrice,
            currentOnlinePricing = config?.onlineGamePricing,
            onDismiss = { showPriceDialog = false },
            onConfirm = { newPrice ->
                // 单机游戏价格更新
                val newConfig = SubsidiaryGameConfig(
                    gameId = game.id,
                    customPrice = newPrice,
                    onlineGamePricing = null,
                    updateStrategy = currentStrategy
                )
                val updatedConfigs = subsidiary.gameConfigs.toMutableMap()
                updatedConfigs[game.id] = newConfig
                onSubsidiaryUpdate(subsidiary.copy(
                    gameConfigs = updatedConfigs,
                    developingGames = subsidiary.developingGames,
                    employees = subsidiary.employees
                ))
                showPriceDialog = false
            },
            onConfirmOnline = { newOnlinePricing ->
                // 网游付费内容价格更新
                val newConfig = SubsidiaryGameConfig(
                    gameId = game.id,
                    customPrice = null,
                    onlineGamePricing = newOnlinePricing,
                    updateStrategy = currentStrategy
                )
                val updatedConfigs = subsidiary.gameConfigs.toMutableMap()
                updatedConfigs[game.id] = newConfig
                onSubsidiaryUpdate(subsidiary.copy(
                    gameConfigs = updatedConfigs,
                    developingGames = subsidiary.developingGames,
                    employees = subsidiary.employees
                ))
                showPriceDialog = false
            }
        )
    }
    
    // 更新策略对话框
    if (showStrategyDialog) {
        UpdateStrategyDialog(
            game = game,
            currentStrategy = currentStrategy,
            onDismiss = { showStrategyDialog = false },
            onConfirm = { newStrategy ->
                val newConfig = SubsidiaryGameConfig(
                    gameId = game.id,
                    customPrice = currentPrice,
                    onlineGamePricing = config?.onlineGamePricing,
                    updateStrategy = newStrategy
                )
                val updatedConfigs = subsidiary.gameConfigs.toMutableMap()
                updatedConfigs[game.id] = newConfig
                onSubsidiaryUpdate(subsidiary.copy(
                    gameConfigs = updatedConfigs,
                    developingGames = subsidiary.developingGames,
                    employees = subsidiary.employees
                ))
                showStrategyDialog = false
            }
        )
    }
}

/**
 * 开发配置标签页
 */
@Composable
private fun DevelopmentConfigTab(
    subsidiary: Subsidiary,
    onSubsidiaryUpdate: (Subsidiary) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 开发偏好选择
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213e)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SingleLineText(
                    text = "🎯 开发偏好",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                MultiLineText(
                    text = "设置子公司开发新游戏时的类型偏好",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // 三个选项
                DevelopmentPreferenceOption(
                    label = "只开发单机游戏",
                    description = "专注于单机市场",
                    isSelected = subsidiary.developmentPreference == DevelopmentPreference.SINGLE_PLAYER_ONLY,
                    onClick = {
                        onSubsidiaryUpdate(subsidiary.copy(
                            developmentPreference = DevelopmentPreference.SINGLE_PLAYER_ONLY,
                            developingGames = subsidiary.developingGames,
                            employees = subsidiary.employees
                        ))
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DevelopmentPreferenceOption(
                    label = "只开发网游",
                    description = "专注于网游市场",
                    isSelected = subsidiary.developmentPreference == DevelopmentPreference.ONLINE_GAME_ONLY,
                    onClick = {
                        onSubsidiaryUpdate(subsidiary.copy(
                            developmentPreference = DevelopmentPreference.ONLINE_GAME_ONLY,
                            developingGames = subsidiary.developingGames,
                            employees = subsidiary.employees
                        ))
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DevelopmentPreferenceOption(
                    label = "两种都开发",
                    description = "灵活应对市场需求",
                    isSelected = subsidiary.developmentPreference == DevelopmentPreference.BOTH,
                    onClick = {
                        onSubsidiaryUpdate(subsidiary.copy(
                            developmentPreference = DevelopmentPreference.BOTH,
                            developingGames = subsidiary.developingGames,
                            employees = subsidiary.employees
                        ))
                    }
                )
            }
        }
    }
}

/**
 * 开发偏好选项
 */
@Composable
private fun DevelopmentPreferenceOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 选择指示器
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) Color(0xFF4CAF50)
                    else Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(text = "✓", color = Color.White, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            SingleLineText(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            MultiLineText(
                text = description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

/**
 * 价格设置区域组件（用于网游付费内容）
 */
@Composable
private fun PricingSection(
    title: String,
    price: Int,
    onPriceChange: (Int) -> Unit,
    useDefault: Boolean,
    onUseDefaultChange: (Boolean) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUseDefaultChange(!useDefault) }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = useDefault,
                onCheckedChange = onUseDefaultChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50),
                    uncheckedColor = Color(0xFF888888),
                    checkmarkColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            SingleLineText(text = "使用默认$title", fontSize = 12.sp, color = Color.White)
        }
        
        if (!useDefault) {
            Spacer(modifier = Modifier.height(4.dp))
            SingleLineText(
                text = "$title: ¥$price", 
                fontSize = 12.sp, 
                color = Color(0xFF64B5F6)
            )
            Slider(
                value = price.toFloat(),
                onValueChange = { onPriceChange(it.toInt()) },
                valueRange = priceRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF64B5F6),
                    activeTrackColor = Color(0xFF64B5F6),
                    inactiveTrackColor = Color(0xFF555555)
                )
            )
        }
    }
}

/**
 * 价格设置对话框
 */
@Composable
private fun PriceSettingDialog(
    game: CompetitorGame,
    currentPrice: Int?,
    currentOnlinePricing: OnlineGamePricing?,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit,
    onConfirmOnline: (OnlineGamePricing?) -> Unit = {}
) {
    val isOnlineGame = game.businessModel == com.example.yjcy.ui.BusinessModel.ONLINE_GAME
    
    // 单机游戏状态
    var price by remember { mutableIntStateOf(currentPrice ?: 50) }
    var useDefault by remember { mutableStateOf(currentPrice == null) }
    
    // 网游付费内容状态（5个付费内容）
    var price1 by remember { mutableIntStateOf(currentOnlinePricing?.price1 ?: 30) }
    var price2 by remember { mutableIntStateOf(currentOnlinePricing?.price2 ?: 15) }
    var price3 by remember { mutableIntStateOf(currentOnlinePricing?.price3 ?: 5) }
    var price4 by remember { mutableIntStateOf(currentOnlinePricing?.price4 ?: 50) }
    var price5 by remember { mutableIntStateOf(currentOnlinePricing?.price5 ?: 20) }
    var useDefault1 by remember { mutableStateOf(currentOnlinePricing?.price1 == null) }
    var useDefault2 by remember { mutableStateOf(currentOnlinePricing?.price2 == null) }
    var useDefault3 by remember { mutableStateOf(currentOnlinePricing?.price3 == null) }
    var useDefault4 by remember { mutableStateOf(currentOnlinePricing?.price4 == null) }
    var useDefault5 by remember { mutableStateOf(currentOnlinePricing?.price5 == null) }
    
    val dialogTitle = if (isOnlineGame) "设置付费内容价格" else "设置游戏价格"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1a1a2e),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            SingleLineText(text = dialogTitle, fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SingleLineText(text = game.name, fontSize = 13.sp, color = Color(0xFFAAAAAA))
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isOnlineGame) {
                    // 网游：根据主题显示对应的5个付费内容（全部可自定义价格）
                    val themeItems = com.example.yjcy.data.MonetizationConfig.getRecommendedItems(game.theme)
                    
                    // 提示信息
                    MultiLineText(
                        text = "💡 所有付费内容均可自定义价格，也可选择使用默认价格",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA726),
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    themeItems.forEachIndexed { index, item ->
                        // 所有5个付费内容都可自定义价格
                        val (currentPrice, currentUseDefault, priceChangeFn, useDefaultChangeFn, currentPriceRange) = when (index) {
                            0 -> PriceConfig(price1, useDefault1, { p: Int -> price1 = p }, { u: Boolean -> useDefault1 = u }, 10f..200f)
                            1 -> PriceConfig(price2, useDefault2, { p: Int -> price2 = p }, { u: Boolean -> useDefault2 = u }, 5f..150f)
                            2 -> PriceConfig(price3, useDefault3, { p: Int -> price3 = p }, { u: Boolean -> useDefault3 = u }, 1f..100f)
                            3 -> PriceConfig(price4, useDefault4, { p: Int -> price4 = p }, { u: Boolean -> useDefault4 = u }, 10f..200f)
                            else -> PriceConfig(price5, useDefault5, { p: Int -> price5 = p }, { u: Boolean -> useDefault5 = u }, 10f..200f)
                        }
                        
                        PricingSection(
                            title = item.displayName,
                            price = currentPrice,
                            onPriceChange = priceChangeFn,
                            useDefault = currentUseDefault,
                            onUseDefaultChange = useDefaultChangeFn,
                            priceRange = currentPriceRange
                        )
                        
                        if (index < themeItems.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                } else {
                    // 单机游戏：单一价格
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { useDefault = !useDefault }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useDefault,
                            onCheckedChange = { useDefault = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50),
                                uncheckedColor = Color(0xFF888888),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SingleLineText(text = "使用默认价格", fontSize = 13.sp, color = Color.White)
                    }
                    
                    if (!useDefault) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SingleLineText(
                            text = "游戏价格: ¥$price", 
                            fontSize = 13.sp, 
                            color = Color(0xFF64B5F6)
                        )
                        Slider(
                            value = price.toFloat(),
                            onValueChange = { price = it.toInt() },
                            valueRange = 10f..300f,
                            steps = 28,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF64B5F6),
                                activeTrackColor = Color(0xFF64B5F6),
                                inactiveTrackColor = Color(0xFF555555)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (isOnlineGame) {
                    // 网游：返回付费内容价格配置（5个）
                    val allDefault = useDefault1 && useDefault2 && useDefault3 && useDefault4 && useDefault5
                    if (allDefault) {
                        onConfirmOnline(null)
                    } else {
                        onConfirmOnline(
                            OnlineGamePricing(
                                price1 = if (useDefault1) null else price1,
                                price2 = if (useDefault2) null else price2,
                                price3 = if (useDefault3) null else price3,
                                price4 = if (useDefault4) null else price4,
                                price5 = if (useDefault5) null else price5
                            )
                        )
                    }
                } else {
                    // 单机：返回游戏价格
                    onConfirm(if (useDefault) null else price)
                }
            }) {
                Text("确定", color = Color(0xFF4CAF50))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFFAAAAAA))
            }
        }
    )
}

/**
 * 更新策略对话框
 */
@Composable
private fun UpdateStrategyDialog(
    game: CompetitorGame,
    currentStrategy: GameUpdateStrategy,
    onDismiss: () -> Unit,
    onConfirm: (GameUpdateStrategy) -> Unit
) {
    var selectedStrategy by remember { mutableStateOf(currentStrategy) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1a1a2e),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            SingleLineText(text = "更新策略", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column {
                SingleLineText(text = game.name, fontSize = 13.sp, color = Color(0xFFAAAAAA))
                Spacer(modifier = Modifier.height(12.dp))
                
                // 激进
                StrategyOption(
                    label = "激进（频繁更新）",
                    description = "每1-2月更新一次",
                    isSelected = selectedStrategy == GameUpdateStrategy.AGGRESSIVE,
                    onClick = { selectedStrategy = GameUpdateStrategy.AGGRESSIVE }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 适中
                StrategyOption(
                    label = "适中（定期更新）",
                    description = "每3-4月更新一次",
                    isSelected = selectedStrategy == GameUpdateStrategy.MODERATE,
                    onClick = { selectedStrategy = GameUpdateStrategy.MODERATE }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 保守
                StrategyOption(
                    label = "保守（很少更新）",
                    description = "每6月或更久更新一次",
                    isSelected = selectedStrategy == GameUpdateStrategy.CONSERVATIVE,
                    onClick = { selectedStrategy = GameUpdateStrategy.CONSERVATIVE }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedStrategy) }) {
                Text("确定", color = Color(0xFF4CAF50))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFFAAAAAA))
            }
        }
    )
}

/**
 * 策略选项
 */
@Composable
private fun StrategyOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF64B5F6).copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF64B5F6),
                unselectedColor = Color(0xFF888888)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            SingleLineText(text = label, fontSize = 13.sp, color = Color.White)
            MultiLineText(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFFAAAAAA),
                maxLines = 1
            )
        }
    }
}

/**
 * 游戏管理独立对话框
 */
@Composable
private fun GameManagementOnlyDialog(
    subsidiary: Subsidiary,
    onDismiss: () -> Unit,
    onSubsidiaryUpdate: (Subsidiary) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1a1a2e)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subsidiary.logo,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        SingleLineText(
                            text = "${subsidiary.name} - 游戏管理",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Text(text = "✖", color = Color.White, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 游戏管理内容
                GameManagementTab(
                    subsidiary = subsidiary,
                    onSubsidiaryUpdate = onSubsidiaryUpdate
                )
            }
        }
    }
}

/**
 * 开发配置独立对话框
 */
@Composable
private fun DevConfigOnlyDialog(
    subsidiary: Subsidiary,
    onDismiss: () -> Unit,
    onSubsidiaryUpdate: (Subsidiary) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1a1a2e)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subsidiary.logo,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        SingleLineText(
                            text = "${subsidiary.name} - 开发配置",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Text(text = "✖", color = Color.White, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 开发配置内容
                DevelopmentConfigTab(
                    subsidiary = subsidiary,
                    onSubsidiaryUpdate = onSubsidiaryUpdate
                )
            }
        }
    }
}

/**
 * 信息行组件
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SingleLineText(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        SingleLineText(
            text = value,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

/**
 * 价格配置辅助类
 */
private data class PriceConfig(
    val price: Int,
    val useDefault: Boolean,
    val onPriceChange: (Int) -> Unit,
    val onUseDefaultChange: (Boolean) -> Unit,
    val priceRange: ClosedFloatingPointRange<Float>
)

/**
 * 多行文本组件
 */
@Composable
private fun MultiLineText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    maxLines: Int = Int.MAX_VALUE
) {
    androidx.compose.material3.Text(
        text = text,
        fontSize = fontSize,
        color = color,
        maxLines = maxLines,
        modifier = modifier
    )
}

/**
 * 子公司选择器列表项
 */
@Composable
private fun SubsidiarySelectorItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.3f)
                else Color(0xFF16213e)
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SingleLineText(
            text = name,
            fontSize = 15.sp,
            color = if (isSelected) Color(0xFF60A5FA) else Color.White
        )
        
        if (isSelected) {
            SingleLineText(
                text = "✓",
                fontSize = 16.sp,
                color = Color(0xFF60A5FA),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 正在开发对话框
 */
@Composable
private fun DevelopingGamesDialog(
    subsidiary: Subsidiary,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ 正在开发",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Text(text = "✕", fontSize = 20.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
                
                Text(
                    text = subsidiary.name,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 开发中游戏列表
                if (subsidiary.developingGames.isEmpty()) {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎮",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "暂无开发中的游戏",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(subsidiary.developingGames) { game ->
                            DevelopingGameCard(game = game)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    )
                ) {
                    Text("关闭", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

/**
 * 开发中游戏卡片
 */
@Composable
private fun DevelopingGameCard(game: DevelopingGame) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D44)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 游戏名称和主题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = game.theme.displayName,
                            fontSize = 12.sp,
                            color = Color(0xFF60A5FA),
                            modifier = Modifier
                                .background(Color(0xFF60A5FA).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = when (game.businessModel) {
                                BusinessModel.SINGLE_PLAYER -> "单机"
                                BusinessModel.ONLINE_GAME -> "网游"
                                else -> "未知"
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // 预估评分
                if (game.estimatedRating > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "预估评分",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = String.format("%.1f", game.estimatedRating),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 开发阶段
            val phaseText = when (game.currentPhase) {
                DevelopmentPhase.DESIGN -> "📋 需求文档"
                DevelopmentPhase.ART_SOUND -> "🎨 美术音效"
                DevelopmentPhase.PROGRAMMING -> "💻 程序实现"
            }
            
            val phaseColor = when (game.currentPhase) {
                DevelopmentPhase.DESIGN -> Color(0xFF8B5CF6)
                DevelopmentPhase.ART_SOUND -> Color(0xFFF59E0B)
                DevelopmentPhase.PROGRAMMING -> Color(0xFF10B981)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phaseText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = phaseColor
                )
                Text(
                    text = "${game.phaseProgress.toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(game.phaseProgress / 100f)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(phaseColor, phaseColor.copy(alpha = 0.8f))
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 平台和开始日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 平台标签
                Text(
                    text = game.platforms.joinToString(", ") { it.displayName },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "开始于 ${game.startDate.year}/${game.startDate.month}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 员工管理对话框
 * 显示子公司员工概况和统计信息，支持招聘和解雇
 */
@Composable
private fun EmployeeManagementDialog(
    subsidiary: Subsidiary,
    onDismiss: () -> Unit,
    onSubsidiaryUpdate: (Subsidiary) -> Unit = {},
    currentDate: GameDate = GameDate(1, 1, 1)
) {
    var showHireDialog by remember { mutableStateOf(false) }
    var showCandidateDialog by remember { mutableStateOf(false) }
    var showFireDialog by remember { mutableStateOf(false) }
    var selectedEmployeeToFire by remember { mutableStateOf<Employee?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleLineText(
                        text = "👥 员工管理",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                    IconButton(onClick = onDismiss) {
                        SingleLineText(text = "✕", fontSize = 20.sp, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 公司名称
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = subsidiary.logo, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    SingleLineText(
                        text = subsidiary.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 内容区域（可滚动）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 员工总数卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2C2C3E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            SingleLineText(
                                text = "📊 员工概况",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9C27B0),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    SingleLineText(
                                        text = "总员工数",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SingleLineText(
                                        text = "${subsidiary.estimatedEmployeeCount}人",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64B5F6)
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    SingleLineText(
                                        text = "月度工资",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SingleLineText(
                                        text = formatMoney(subsidiary.monthlyWageCost),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFA726)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 员工构成估算
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2C2C3E)
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                                SingleLineText(
                                    text = "👔 团队详情",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9C27B0)
                                )
                                
                                // 招聘和候选人按钮
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 招聘按钮
                                    Button(
                                        onClick = { showHireDialog = true },
                                        enabled = subsidiary.employees.size < 30,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50),
                                            disabledContainerColor = Color.Gray
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SingleLineText(text = "+", fontSize = 14.sp, color = Color.White)
                                            SingleLineText(text = "招聘", fontSize = 13.sp, color = Color.White)
                                        }
                                    }
                                    
                                    // 候选人按钮
                                    OutlinedButton(
                                        onClick = { showCandidateDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF64B5F6)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SingleLineText(text = "👥", fontSize = 14.sp)
                                            SingleLineText(text = "候选人", fontSize = 13.sp, color = Color(0xFF64B5F6))
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 统计各职位人数和技能等级
                            val employeesByPosition = subsidiary.employees.groupBy { it.position }
                            
                            // 显示各职位信息
                            listOf(
                                "程序员" to Color(0xFF3B82F6),
                                "策划师" to Color(0xFF10B981),
                                "美工" to Color(0xFFF59E0B),
                                "音乐家" to Color(0xFF8B5CF6),
                                "客服" to Color(0xFFEC4899)
                            ).forEach { (position, color) ->
                                val employees = employeesByPosition[position] ?: emptyList()
                                if (employees.isNotEmpty()) {
                                    val avgSkill = when (position) {
                                        "程序员" -> employees.map { it.skillDevelopment }.average()
                                        "策划师" -> employees.map { it.skillDesign }.average()
                                        "美工" -> employees.map { it.skillArt }.average()
                                        "音乐家" -> employees.map { it.skillMusic }.average()
                                        "客服" -> employees.map { it.skillService }.average()
                                        else -> 0.0
                                    }
                                    EmployeePositionRow(
                                        position = position,
                                        count = employees.size,
                                        avgSkillLevel = avgSkill,
                                        color = color,
                                        employees = employees,
                                        onEmployeeClick = { employee ->
                                            selectedEmployeeToFire = employee
                                            showFireDialog = true
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 说明文字
                            MultiLineText(
                                text = "💡 显示各岗位人数和专属技能平均等级\n• 程序员：开发技能\n• 策划师：设计技能\n• 美工：美术技能\n• 音乐家：音乐技能\n• 客服：服务技能",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 6
                            )
                        }
                    }
                    
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    SingleLineText(text = "关闭", fontSize = 15.sp)
                }
            }
        }
    }
    
    // 招聘对话框 - 发布岗位
    if (showHireDialog) {
        SubsidiaryHireDialog(
            subsidiary = subsidiary,
            onDismiss = { showHireDialog = false },
            onConfirm = { position, skillLevel, salary ->
                // 发布招聘岗位
                val updatedSubsidiary = SubsidiaryManager.postJob(
                    subsidiary = subsidiary,
                    position = position,
                    requiredSkillLevel = skillLevel,
                    salary = salary,
                    currentDate = currentDate
                )
                onSubsidiaryUpdate(updatedSubsidiary)
                showHireDialog = false
            }
        )
    }
    
    // 候选人列表对话框 - 显示已发布岗位的应聘者
    if (showCandidateDialog) {
        SubsidiaryApplicantDialog(
            subsidiary = subsidiary,
            currentDate = currentDate,
            onDismiss = { showCandidateDialog = false },
            onHireApplicant = { jobPostingId, applicantId ->
                val updatedSubsidiary = SubsidiaryManager.hireApplicant(
                    subsidiary = subsidiary,
                    jobPostingId = jobPostingId,
                    applicantId = applicantId,
                    currentDate = currentDate
                )
                onSubsidiaryUpdate(updatedSubsidiary)
            },
            onClosePosting = { jobPostingId ->
                val updatedSubsidiary = SubsidiaryManager.closeJobPosting(
                    subsidiary = subsidiary,
                    jobPostingId = jobPostingId
                )
                onSubsidiaryUpdate(updatedSubsidiary)
            }
        )
    }
    
    // 解雇对话框
    if (showFireDialog && selectedEmployeeToFire != null) {
        SubsidiaryFireDialog(
            employee = selectedEmployeeToFire!!,
            subsidiary = subsidiary,
            currentDate = currentDate,
            onDismiss = {
                showFireDialog = false
                selectedEmployeeToFire = null
            },
            onConfirm = {
                // 计算补偿金
                val severancePay = selectedEmployeeToFire!!.calculateSeverancePay(
                    currentDate.year,
                    currentDate.month,
                    currentDate.day
                )
                
                // 从子公司资金扣除补偿金
                val updatedSubsidiary = subsidiary.copy(
                    employees = subsidiary.employees.filter { it.id != selectedEmployeeToFire!!.id },
                    monthlyWageCost = subsidiary.monthlyWageCost - selectedEmployeeToFire!!.salary,
                    cashBalance = subsidiary.cashBalance - severancePay
                )
                onSubsidiaryUpdate(updatedSubsidiary)
                showFireDialog = false
                selectedEmployeeToFire = null
            }
        )
    }
}

/**
 * 员工职位行（带技能等级，支持点击查看员工列表）
 */
@Composable
private fun EmployeePositionRow(
    position: String,
    count: Int,
    avgSkillLevel: Double,
    color: Color,
    employees: List<Employee> = emptyList(),
    onEmployeeClick: (Employee) -> Unit = {}
) {
    var showEmployeeList by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .clickable { if (employees.isNotEmpty()) showEmployeeList = true }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：职位名称
        SingleLineText(
            text = position,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        
        // 右侧：人数和技能等级
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 技能等级
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(
                    text = "⭐",
                    fontSize = 12.sp
                )
                SingleLineText(
                    text = String.format("%.1f级", avgSkillLevel),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
            
            // 人数
            SingleLineText(
                text = "${count}人",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
    
    // 员工列表对话框
    if (showEmployeeList) {
        Dialog(onDismissRequest = { showEmployeeList = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
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
                        SingleLineText(
                            text = "$position 列表",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        IconButton(onClick = { showEmployeeList = false }) {
                            SingleLineText(text = "✕", fontSize = 18.sp, color = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(employees) { employee ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showEmployeeList = false
                                        onEmployeeClick(employee)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2C2C3E)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        SingleLineText(
                                            text = employee.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        val skillLevel = when (position) {
                                            "程序员" -> employee.skillDevelopment
                                            "策划师" -> employee.skillDesign
                                            "美工" -> employee.skillArt
                                            "音乐家" -> employee.skillMusic
                                            "客服" -> employee.skillService
                                            else -> 0
                                        }
                                        SingleLineText(
                                            text = "技能: $skillLevel 级 | 月薪: ¥${employee.salary}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    SingleLineText(
                                        text = "解雇 →",
                                        fontSize = 12.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 水平滚动选择器组件
 */
@Composable
private fun HorizontalScrollSelector(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(items) { item ->
            val isSelected = item == selectedItem
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF66BB6A)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        }
                    )
                    .clickable { onItemSelected(item) }
                    .then(
                        if (isSelected) {
                            Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                SingleLineText(
                    text = item,
                    fontSize = if (isSelected) 16.sp else 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 子公司招聘对话框 - 简单版本
 */
@Composable
private fun SubsidiaryHireDialog(
    subsidiary: Subsidiary,
    onDismiss: () -> Unit,
    onConfirm: (position: String, skillLevel: Int, salary: Int) -> Unit
) {
    var selectedPosition by remember { mutableStateOf("程序员") }
    var skillLevel by remember { mutableIntStateOf(1) }
    var salary by remember { mutableIntStateOf(10000) }
    
    // 计算最低薪资
    val minSalary = skillLevel * 10000
    
    // 确保薪资不低于最低薪资
    LaunchedEffect(skillLevel) {
        if (salary < minSalary) {
            salary = minSalary
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1a1a2e),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            SingleLineText(
                text = "招聘员工",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 公司名称
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = subsidiary.logo, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SingleLineText(text = subsidiary.name, fontSize = 14.sp, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 员工上限提示
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (subsidiary.employees.size >= 30) Color(0xFFEF4444).copy(alpha = 0.2f)
                            else Color(0xFF4CAF50).copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SingleLineText(
                        text = "当前员工数",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    SingleLineText(
                        text = "${subsidiary.employees.size} / 30",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (subsidiary.employees.size >= 30) Color(0xFFEF4444) else Color(0xFF4CAF50)
                    )
                }
                
                // 职位选择
                SingleLineText(text = "选择职位", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                HorizontalScrollSelector(
                    items = listOf("程序员", "策划师", "美工", "音乐家", "客服"),
                    selectedItem = selectedPosition,
                    onItemSelected = { selectedPosition = it }
                )
                
                // 技能等级选择
                SingleLineText(text = "技能等级", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                HorizontalScrollSelector(
                    items = (1..5).map { "${it}级" },
                    selectedItem = "${skillLevel}级",
                    onItemSelected = { 
                        skillLevel = it.replace("级", "").toInt()
                    }
                )
                
                // 薪资设置
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SingleLineText(text = "月薪", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        SingleLineText(
                            text = "¥$salary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    SingleLineText(
                        text = "最低标准: ¥$minSalary",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Slider(
                        value = salary.toFloat(),
                        onValueChange = { salary = it.toInt() },
                        valueRange = minSalary.toFloat()..60000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CAF50),
                            activeTrackColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedPosition, skillLevel, salary) },
                enabled = subsidiary.employees.size < 30
            ) {
                SingleLineText(text = "确认招聘", color = Color(0xFF4CAF50))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                SingleLineText(text = "取消", color = Color.Gray)
            }
        }
    )
}

/**
 * 子公司应聘者管理对话框（显示已发布岗位的应聘者）
 */
@Composable
private fun SubsidiaryApplicantDialog(
    subsidiary: Subsidiary,
    currentDate: GameDate,
    onDismiss: () -> Unit,
    onHireApplicant: (jobPostingId: String, applicantId: String) -> Unit,
    onClosePosting: (jobPostingId: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1a1a2e)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = subsidiary.logo, fontSize = 28.sp)
                        Column {
                            SingleLineText(
                                text = "${subsidiary.name} - 应聘者",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            SingleLineText(
                                text = "员工: ${subsidiary.employees.size} / 30 | 岗位: ${subsidiary.jobPostings.count { it.isActive }}",
                                fontSize = 13.sp,
                                color = Color(0xFF64B5F6)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Text(text = "✕", fontSize = 20.sp, color = Color.White)
                    }
                }
                
                // 内容区
                if (subsidiary.jobPostings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "📋", fontSize = 64.sp)
                            SingleLineText(
                                text = "还没有发布招聘岗位",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            SingleLineText(
                                text = "点击左侧\"+招聘\"按钮发布岗位",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(subsidiary.jobPostings.filter { it.isActive }) { posting ->
                            SubsidiaryJobPostingCard(
                                posting = posting,
                                subsidiary = subsidiary,
                                onHireApplicant = { applicantId ->
                                    onHireApplicant(posting.id, applicantId)
                                },
                                onClosePosting = {
                                    onClosePosting(posting.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 子公司岗位卡片（显示岗位信息和应聘者）
 */
@Composable
private fun SubsidiaryJobPostingCard(
    posting: SubsidiaryJobPosting,
    subsidiary: Subsidiary,
    onHireApplicant: (applicantId: String) -> Unit,
    onClosePosting: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val pendingApplicants = posting.applicants.filter { it.status == SubsidiaryApplicantStatus.PENDING }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C3E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 岗位头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 职位图标
                    val icon = when (posting.position) {
                        "程序员" -> "💻"
                        "策划师" -> "📋"
                        "美工" -> "🎨"
                        "音乐家" -> "🎵"
                        "客服" -> "💬"
                        else -> "💼"
                    }
                    Text(text = icon, fontSize = 32.sp)
                    
                    Column {
                        SingleLineText(
                            text = posting.position,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        SingleLineText(
                            text = "Lv.${posting.requiredSkillLevel} | ¥${String.format("%,d", posting.salary)}/月",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 应聘者数量
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (pendingApplicants.isNotEmpty()) Color(0xFF4CAF50).copy(alpha = 0.2f)
                               else Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "👥", fontSize = 14.sp)
                            SingleLineText(
                                text = "${pendingApplicants.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingApplicants.isNotEmpty()) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                    
                    // 关闭岗位按钮
                    IconButton(
                        onClick = onClosePosting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(text = "✕", fontSize = 16.sp, color = Color(0xFFEF4444))
                    }
                }
            }
            
            // 应聘者列表
            if (pendingApplicants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                
                pendingApplicants.forEach { applicant ->
                    SubsidiaryApplicantCard(
                        applicant = applicant,
                        isEmployeeFull = subsidiary.employees.size >= 30,
                        onHireClick = {
                            if (subsidiary.employees.size < 30) {
                                onHireApplicant(applicant.id)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SingleLineText(
                        text = "🔍 暂无应聘者，请耐心等待",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * 子公司应聘者卡片
 */
@Composable
private fun SubsidiaryApplicantCard(
    applicant: SubsidiaryApplicant,
    isEmployeeFull: Boolean,
    onHireClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SingleLineText(
                        text = applicant.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SingleLineText(
                        text = "${applicant.age}岁",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                Button(
                    onClick = onHireClick,
                    enabled = !isEmployeeFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    SingleLineText(
                        text = if (isEmployeeFull) "已满员" else "雇佣",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 技能列表 - 紧凑显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                applicant.skills.forEach { (skillName, level) ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SingleLineText(
                                text = skillName,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            SingleLineText(
                                text = "Lv.$level",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 期望薪资
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(
                    text = "期望薪资: ¥${String.format("%,d", applicant.expectedSalary)}/月",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 子公司解雇员工对话框
 */
@Composable
private fun SubsidiaryFireDialog(
    employee: Employee,
    subsidiary: Subsidiary,
    currentDate: GameDate,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // 计算补偿金
    val workMonths = employee.calculateWorkMonths(currentDate.year, currentDate.month, currentDate.day)
    val workYears = (workMonths + 11) / 12
    val severancePay = employee.calculateSeverancePay(currentDate.year, currentDate.month, currentDate.day)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1a1a2e),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            SingleLineText(
                text = "确认解雇",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 公司名称
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = subsidiary.logo, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SingleLineText(text = subsidiary.name, fontSize = 14.sp, color = Color.White)
                }
                
                // 员工信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C2C3E)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SingleLineText(
                            text = employee.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        InfoRow("职位", employee.position)
                        InfoRow("月薪", "¥${employee.salary}")
                        InfoRow("工作时长", "$workMonths 个月 ($workYears 年)")
                    }
                }
                
                // 补偿金信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SingleLineText(
                            text = "💰 解雇补偿金",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        SingleLineText(
                            text = "公式: 月薪 × (2N + 1)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SingleLineText(
                                text = "需支付",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            SingleLineText(
                                text = "¥$severancePay",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
                
                // 资金不足警告
                if (subsidiary.cashBalance < severancePay) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleLineText(text = "⚠️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        MultiLineText(
                            text = "子公司资金不足！当前资金: ¥${formatMoney(subsidiary.cashBalance)}",
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 2
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = subsidiary.cashBalance >= severancePay
            ) {
                SingleLineText(
                    text = "确认解雇",
                    color = if (subsidiary.cashBalance >= severancePay) Color(0xFFEF4444) else Color.Gray
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                SingleLineText(text = "取消", color = Color.Gray)
            }
        }
    )
}
