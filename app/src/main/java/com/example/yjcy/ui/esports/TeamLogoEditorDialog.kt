package com.example.yjcy.ui.esports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yjcy.data.TeamLogoConfig
import com.example.yjcy.ui.components.SingleLineText

/**
 * 队徽编辑器对话框
 */
@Composable
fun TeamLogoEditorDialog(
    currentConfig: TeamLogoConfig,
    onDismiss: () -> Unit,
    onSave: (TeamLogoConfig) -> Unit
) {
    var config by remember { mutableStateOf(currentConfig) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 颜色, 1: 文字

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SingleLineText(
                    text = "🎨 定制动态队徽",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 预览区域
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DynamicTeamLogo(
                        config = config,
                        modifier = Modifier.size(140.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 选项卡
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF2A2A3E),
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("配色方案") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("队名文字") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 编辑区域
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> ColorEditor(config) { config = it }
                        1 -> TextEditor(config) { config = it }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = { onSave(config) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorEditor(
    config: TeamLogoConfig,
    onUpdate: (TeamLogoConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 预设配色方案
        Text("预设方案", color = Color.Gray, fontSize = 14.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PredefinedSchemes) { scheme ->
                ColorSchemeItem(scheme, config) {
                    onUpdate(config.copy(
                        backgroundColor1 = scheme.bg1,
                        backgroundColor2 = scheme.bg2,
                        borderColor1 = scheme.border1,
                        borderColor2 = scheme.border2
                    ))
                }
            }
        }
        
        // 详细颜色调整（简化版，只显示颜色块）
        Text("主色调", color = Color.Gray, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorPickerCircle(Color(config.backgroundColor1)) { /* 暂不支持高级取色 */ }
            ColorPickerCircle(Color(config.backgroundColor2))
        }
        
        Text("边框色", color = Color.Gray, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorPickerCircle(Color(config.borderColor1))
            ColorPickerCircle(Color(config.borderColor2))
        }
    }
}

@Composable
private fun TextEditor(
    config: TeamLogoConfig,
    onUpdate: (TeamLogoConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = config.teamName,
            onValueChange = { if (it.length <= 4) onUpdate(config.copy(teamName = it.uppercase())) },
            label = { Text("主队名 (最多4字)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = config.subText,
            onValueChange = { if (it.length <= 8) onUpdate(config.copy(subText = it.uppercase())) },
            label = { Text("副标题 (最多8字)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray
            )
        )
        
        Text("💡 提示：所有文字将自动转换为大写", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun ColorSchemeItem(
    scheme: LogoColorScheme,
    currentConfig: TeamLogoConfig,
    onClick: () -> Unit
) {
    val isSelected = scheme.bg1 == currentConfig.backgroundColor1 && scheme.bg2 == currentConfig.backgroundColor2
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color(scheme.bg1), Color(scheme.bg2))
                ),
                CircleShape
            )
            .border(
                if (isSelected) 3.dp else 1.dp,
                if (isSelected) Color.White else Color.Gray,
                CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ColorPickerCircle(color: Color, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick)
    )
}

data class LogoColorScheme(
    val bg1: Long,
    val bg2: Long,
    val border1: Long,
    val border2: Long
)

private val PredefinedSchemes = listOf(
    // 经典蓝金
    LogoColorScheme(0xFF1565C0, 0xFF0D47A1, 0xFFFFD700, 0xFFFFA000),
    // 烈焰红
    LogoColorScheme(0xFFC62828, 0xFF8E0000, 0xFFFFD700, 0xFFFF5722),
    // 森林绿
    LogoColorScheme(0xFF2E7D32, 0xFF1B5E20, 0xFF81C784, 0xFF4CAF50),
    // 皇家紫
    LogoColorScheme(0xFF6A1B9A, 0xFF4A148C, 0xFFE1BEE7, 0xFFBA68C8),
    // 暗夜黑
    LogoColorScheme(0xFF212121, 0xFF000000, 0xFFFFFFFF, 0xFFBDBDBD),
    // 赛博粉
    LogoColorScheme(0xFFC2185B, 0xFF880E4F, 0xFF00BCD4, 0xFF00E5FF)
)
