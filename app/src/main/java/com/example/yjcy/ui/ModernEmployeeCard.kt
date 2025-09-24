package com.example.yjcy.ui

import com.example.yjcy.data.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernEmployeeCard(
    employee: Employee,
    onTrainEmployee: (Employee, String) -> Unit,
    onDismissEmployee: (Employee) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showTrainingDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF667eea).copy(alpha = 0.1f),
                            Color(0xFF764ba2).copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                // 员工基本信息
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
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "¥${employee.salary}/月",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                        
                        // 技能等级指示器
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(5) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = if (index < employee.getSpecialtySkillLevel()) {
                                                Color(0xFFFFD700)
                                            } else {
                                                Color.Gray.copy(alpha = 0.3f)
                                            },
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                            Text(
                                text = "Lv.${employee.getSpecialtySkillLevel()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // 技能详情
                        Text(
                            text = "技能详情",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val skills: List<Pair<String, Int>> = when (employee.position) {
                            "程序员" -> listOf("开发" to employee.skillDevelopment)
                            "策划师" -> listOf("设计" to employee.skillDesign)
                            "美术师" -> listOf("美工" to employee.skillArt)
                            "音效师" -> listOf("音效" to employee.skillMusic)
                            "客服" -> listOf("客服" to employee.skillService)
                            else -> listOf("开发" to employee.skillDevelopment)
                        }
                        
                        skills.forEach { skill ->
                            val skillName = skill.first
                            val skillLevel = skill.second
                            SkillBar(
                                skillName = skillName,
                                skillLevel = skillLevel,
                                maxLevel = 5
                            )
                        }
                        
                        // 操作按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModernButton(
                                text = "培训",
                                icon = "⭐",
                                onClick = { showTrainingDialog = true },
                                enabled = true,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                )
                            )
                            
                            ModernButton(
                                text = "解雇",
                                icon = "🗑️",
                                onClick = { onDismissEmployee(employee) },
                                enabled = true,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF44336)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 培训对话框
    if (showTrainingDialog) {
        TrainingDialog(
            employee = employee,
            onDismiss = { showTrainingDialog = false },
            onConfirm = { skillType ->
                onTrainEmployee(employee, skillType)
                showTrainingDialog = false
            }
        )
    }
}

@Composable
fun SkillBar(
    skillName: String,
    skillLevel: Int,
    maxLevel: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                .height(6.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(3.dp)
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
                        shape = RoundedCornerShape(3.dp)
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
fun TrainingDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedSkill by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "培训 ${employee.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "选择要培训的技能类型：",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                val skills: List<Pair<String, Int>> = when (employee.position) {
                    "程序员" -> listOf("开发" to employee.skillDevelopment)
                    "策划师" -> listOf("设计" to employee.skillDesign)
                    "美术师" -> listOf("美工" to employee.skillArt)
                    "音效师" -> listOf("音效" to employee.skillMusic)
                    "客服" -> listOf("客服" to employee.skillService)
                    else -> listOf("开发" to employee.skillDevelopment)
                }
                
                skills.forEach { skill ->
                    val skillName = skill.first
                    val currentLevel = skill.second
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSkill = skillName }
                            .background(
                                color = if (selectedSkill == skillName) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSkill == skillName,
                            onClick = { selectedSkill = skillName }
                        )
                        
                        Column(
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = skillName,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "当前等级: $currentLevel",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedSkill) },
                enabled = selectedSkill.isNotEmpty()
            ) {
                Text("确认培训")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ModernButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = colors,
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}