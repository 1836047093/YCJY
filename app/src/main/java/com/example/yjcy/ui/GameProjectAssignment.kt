package com.example.yjcy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.*
import com.example.yjcy.ui.OneClickAssignmentButton

// 游戏数据类
data class Game(
    val id: String,
    val name: String,
    val theme: GameTheme,
    val platforms: List<Platform>,
    val businessModel: BusinessModel,
    val developmentProgress: Float = 0f,
    val isCompleted: Boolean = false,
    val revenue: Long = 0L,
    val assignedEmployees: List<Employee> = emptyList() // 新增：已分配的员工列表
)

enum class GameTheme(val displayName: String, val icon: String) {
    ACTION("动作", "⚔️"),
    ADVENTURE("冒险", "🗺️"),
    RPG("角色扮演", "🧙"),
    STRATEGY("策略", "♟️"),
    SIMULATION("模拟", "🏗️"),
    PUZZLE("解谜", "🧩"),
    RACING("竞速", "🏎️"),
    SPORTS("体育", "⚽")
}

enum class Platform(val displayName: String, val icon: String) {
    PC("PC", "💻"),
    MOBILE("手机", "📱"),
    CONSOLE("主机", "🎮"),
    WEB("网页", "🌐")
}

enum class BusinessModel(val displayName: String, val icon: String) {
    SINGLE_PLAYER("单机游戏", "🎮"),
    ONLINE_GAME("网络游戏", "🌐")
}

// 员工数据类
data class Employee(
    val id: Int,
    val name: String,
    val position: String,
    val skillDevelopment: Int,
    val skillDesign: Int,
    val skillArt: Int,
    val skillMusic: Int,
    val skillService: Int,
    val salary: Int,
    val isAssigned: Boolean = false // 新增：是否已分配
) {
    // 获取员工的专属技能类型
    fun getSpecialtySkillType(): String {
        return when (position) {
            "程序员" -> "开发"
            "策划师" -> "设计"
            "美术师" -> "美工"
            "音效师" -> "音乐"
            "客服" -> "服务"
            else -> "通用"
        }
    }
    
    // 获取员工的总技能点数
    fun getTotalSkillPoints(): Int {
        return skillDevelopment + skillDesign + skillArt + skillMusic + skillService
    }
    
    // 获取员工的主要技能值
    fun getPrimarySkillValue(): Int {
        return when (position) {
            "程序员" -> skillDevelopment
            "策划师" -> skillDesign
            "美术师" -> skillArt
            "音效师" -> skillMusic
            "客服" -> skillService
            else -> getTotalSkillPoints() / 5
        }
    }
    
    // 获取员工的专业技能等级
    fun getSpecialtySkillLevel(): Int {
        return getPrimarySkillValue()
    }
}

/**
 * 增强版游戏项目卡片，包含员工分配功能
 */
@Composable
fun EnhancedGameProjectCard(
    game: Game,
    availableEmployees: List<Employee> = emptyList(),
    onEmployeeAssigned: (Game, List<Employee>) -> Unit = { _, _ -> }
) {
    var showAssignmentDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 项目基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = game.theme.icon,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "主题: ${game.theme.displayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Text(
                text = "平台: ${game.platforms.joinToString(", ") { it.displayName }}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Text(
                text = "商业模式: ${game.businessModel.displayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 已分配员工信息
            if (game.assignedEmployees.isNotEmpty()) {
                Text(
                    text = "已分配员工 (${game.assignedEmployees.size}人):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    game.assignedEmployees.take(3).forEach { employee ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF10B981).copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${employee.name}(${employee.position})",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    if (game.assignedEmployees.size > 3) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+${game.assignedEmployees.size - 3}",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Text(
                        text = "开发进度",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${(game.developmentProgress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = game.developmentProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF10B981),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 一键分配员工按钮
            OneClickAssignmentButton(
                onClick = { showAssignmentDialog = true },
                modifier = Modifier.fillMaxWidth(),
                text = if (game.assignedEmployees.isEmpty()) "👥 分配员工" else "👥 重新分配员工"
            )
        }
    }
    
    // 员工分配对话框
    if (showAssignmentDialog) {
        EmployeeAssignmentDialog(
            game = game,
            availableEmployees = availableEmployees,
            onDismiss = { showAssignmentDialog = false },
            onAssignEmployees = { selectedEmployees ->
                onEmployeeAssigned(game, selectedEmployees)
                showAssignmentDialog = false
            }
        )
    }
}

/**
 * 员工分配对话框
 */
@Composable
fun EmployeeAssignmentDialog(
    game: Game,
    availableEmployees: List<Employee>,
    onDismiss: () -> Unit,
    onAssignEmployees: (List<Employee>) -> Unit
) {
    var selectedEmployees by remember { mutableStateOf(game.assignedEmployees.toSet()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "👥 为 ${game.name} 分配员工",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                

                
                // 可用员工列表
                Text(
                    text = "可用员工 (${availableEmployees.size}人):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableEmployees) { employee ->
                        EmployeeSelectionCard(
                            employee = employee,
                            isSelected = selectedEmployees.contains(employee),
                            onSelectionChanged = { isSelected ->
                                selectedEmployees = if (isSelected) {
                                    selectedEmployees + employee
                                } else {
                                    selectedEmployees - employee
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 选中员工统计
                if (selectedEmployees.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "已选择 ${selectedEmployees.size} 名员工",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            val totalCost = selectedEmployees.sumOf { it.salary }
                            Text(
                                text = "总成本: ¥$totalCost/月",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // 一键分配功能
                            val assignmentService = EnhancedAssignmentService()
                            val result = assignmentService.assignBestEmployeesToProject(
                                game,
                                availableEmployees
                            )
                            selectedEmployees = result.assignedEmployees.toSet()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        )
                    ) {
                        Text("一键分配")
                    }
                    
                    Button(
                        onClick = {
                            onAssignEmployees(selectedEmployees.toList())
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White
                        )
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

/**
 * 员工选择卡片
 */
@Composable
fun EmployeeSelectionCard(
    employee: Employee,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF3B82F6).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF3B82F6)) else null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = employee.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = employee.position,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "主技能: ${employee.getPrimarySkillValue()}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    
                    Text(
                        text = "薪资: ¥${employee.salary}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            
            if (isSelected) {
                Text(
                    text = "✓",
                    color = Color(0xFF10B981),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}