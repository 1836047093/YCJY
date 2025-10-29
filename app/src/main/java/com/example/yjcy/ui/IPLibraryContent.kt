package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.GameIP

/**
 * IP筛选类型
 */
enum class IPFilterType(val displayName: String) {
    ALL("全部"),
    POPULAR("知名IP"),
    COMMON("普通IP"),
    NICHE("小众IP")
}

/**
 * IP库内容组件
 */
@Composable
fun IPLibraryContent(
    ownedIPs: List<GameIP> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(IPFilterType.ALL) }
    
    // 根据筛选条件过滤IP列表
    val filteredIPs = remember(ownedIPs, selectedFilter) {
        when (selectedFilter) {
            IPFilterType.ALL -> ownedIPs
            IPFilterType.POPULAR -> ownedIPs.filter { it.originalRating >= 7.5f }
            IPFilterType.COMMON -> ownedIPs.filter { it.originalRating >= 6.5f && it.originalRating < 7.5f }
            IPFilterType.NICHE -> ownedIPs.filter { it.originalRating < 6.5f }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
                    )
                )
            )
    ) {
        // 顶部标题和筛选器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📚 IP库",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // IP筛选下拉菜单
            IPFilterDropdown(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }
        
        // IP统计信息
        if (ownedIPs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IPStatItem(
                    label = "IP总数",
                    value = "${ownedIPs.size}",
                    icon = "📚"
                )
                IPStatItem(
                    label = "知名IP",
                    value = "${ownedIPs.count { it.originalRating >= 7.5f }}",
                    icon = "⭐"
                )
                IPStatItem(
                    label = "平均评分",
                    value = String.format("%.1f", ownedIPs.map { it.originalRating }.average()),
                    icon = "⭐"
                )
            }
        }
        
        // IP列表
        if (filteredIPs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (ownedIPs.isEmpty()) "暂无IP" else "暂无匹配的IP",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (ownedIPs.isEmpty()) 
                            "收购竞争对手公司后，获得的IP将在此显示" 
                        else 
                            "请尝试选择其他筛选条件",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredIPs) { ip ->
                    IPCard(ip = ip)
                }
            }
        }
    }
}

/**
 * IP筛选下拉菜单
 */
@Composable
fun IPFilterDropdown(
    selectedFilter: IPFilterType,
    onFilterSelected: (IPFilterType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Card(
            modifier = Modifier.clickable { expanded = true },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4F46E5).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedFilter.displayName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "展开下拉菜单",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                Color(0xFF1E1B4B),
                RoundedCornerShape(8.dp)
            )
        ) {
            IPFilterType.values().forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = filter.displayName,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (selectedFilter == filter) Color(0xFF4F46E5).copy(alpha = 0.3f) else Color.Transparent
                    )
                )
            }
        }
    }
}

/**
 * IP统计项
 */
@Composable
fun IPStatItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

/**
 * IP卡片组件
 */
@Composable
fun IPCard(ip: GameIP) {
    val bonusPercent = (ip.calculateIPBonus() * 100).toInt()
    val ipLevel = ip.getIPLevel()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // IP名称和等级
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ip.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // IP等级标签
            Box(
                modifier = Modifier
                    .background(
                        getIPLevelColor(ip.originalRating),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = ipLevel,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // IP详细信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 原游戏评分
            IPInfoItem(
                label = "原评分",
                value = String.format("%.1f", ip.originalRating),
                icon = "⭐"
            )
            
            // 销量加成
            IPInfoItem(
                label = "销量加成",
                value = "+$bonusPercent%",
                icon = "📈"
            )
            
            // 收购时间
            IPInfoItem(
                label = "收购时间",
                value = "${ip.acquiredYear}年${ip.acquiredMonth}月",
                icon = "📅"
            )
        }
        
        Divider(
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // 原公司、主题、平台等信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UIInfoChip(
                text = "来自: ${ip.originalCompany}",
                icon = "🏢"
            )
            UIInfoChip(
                text = ip.theme.displayName,
                icon = ip.theme.icon
            )
            if (ip.platforms.isNotEmpty()) {
                UIInfoChip(
                    text = ip.platforms.first().displayName,
                    icon = ip.platforms.first().icon
                )
            }
        }
    }
}

/**
 * IP信息项
 */
@Composable
fun IPInfoItem(
    label: String,
    value: String,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 14.sp
        )
        Column {
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 信息标签
 */
@Composable
fun UIInfoChip(
    text: String,
    icon: String
) {
    Box(
        modifier = Modifier
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 10.sp
            )
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 根据IP评分获取等级颜色
 */
fun getIPLevelColor(rating: Float): Color {
    return when {
        rating >= 7.5f -> Color(0xFF4CAF50) // 绿色 - 知名IP
        rating >= 6.5f -> Color(0xFF9E9E9E) // 灰色 - 普通IP
        else -> Color(0xFF757575) // 深灰色 - 小众IP
    }
}

