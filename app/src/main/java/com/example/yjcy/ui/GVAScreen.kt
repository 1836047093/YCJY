package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.*
import com.example.yjcy.ui.components.SingleLineText
import com.example.yjcy.ui.components.MultiLineText

/**
 * GVA游戏大奖主界面
 */
@Composable
fun GVAScreen(
    saveData: SaveData,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
    ) {
        // 顶部标题栏
        GVATopBar(onBack = onBack)
        
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
                text = { SingleLineText(text = "本年度提名", fontSize = 14.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { SingleLineText(text = "历史记录", fontSize = 14.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { SingleLineText(text = "声望", fontSize = 14.sp) }
            )
        }
        
        // 内容区域
        when (selectedTab) {
            0 -> CurrentNominationsTab(saveData)
            1 -> HistoryTab(saveData)
            2 -> ReputationTab(saveData)
        }
    }
}

/**
 * 顶部标题栏
 */
@Composable
private fun GVATopBar(onBack: () -> Unit) {
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
            text = "🏆 GVA游戏大奖",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )
    }
}

/**
 * 当年提名标签页
 */
@Composable
private fun CurrentNominationsTab(saveData: SaveData) {
    val nominations = saveData.currentYearNominations
    
    if (nominations.isEmpty()) {
        // 空状态
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SingleLineText(
                    text = "📅",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                SingleLineText(
                    text = "暂无提名",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                MultiLineText(
                    text = "每年12月15日生成初步提名\n12月31日公布最终获奖名单",
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
            // 根据分类分组显示
            val themeAwards = nominations.filter { it.award.category == AwardCategory.THEME }
            val generalAwards = nominations.filter { it.award.category == AwardCategory.GENERAL }
            val specialAwards = nominations.filter { it.award.category == AwardCategory.SPECIAL }
            
            if (generalAwards.isNotEmpty()) {
                item {
                    SingleLineText(
                        text = "🏆 综合类奖项",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
                items(generalAwards) { nomination ->
                    NominationCard(nomination)
                }
            }
            
            if (themeAwards.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleLineText(
                        text = "🎮 主题类奖项",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
                items(themeAwards) { nomination ->
                    NominationCard(nomination)
                }
            }
            
            if (specialAwards.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleLineText(
                        text = "⭐ 特殊成就奖项",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
                items(specialAwards) { nomination ->
                    NominationCard(nomination)
                }
            }
        }
    }
}

/**
 * 提名卡片
 */
@Composable
private fun NominationCard(nomination: AwardNomination) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 奖项名称
            Row(verticalAlignment = Alignment.CenterVertically) {
                SingleLineText(
                    text = nomination.award.icon,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                SingleLineText(
                    text = nomination.award.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 状态提示
            if (!nomination.isFinal) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA500).copy(alpha = 0.2f))
                ) {
                    MultiLineText(
                        text = "⚠️ 初步提名 - 最终结果12月31日揭晓",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA500),
                        modifier = Modifier.padding(8.dp),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 获奖者/提名列表
            if (nomination.isFinal && nomination.winner != null) {
                // 显示获奖者
                WinnerCard(nomination.winner)
                
                if (nomination.nominees.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleLineText(
                        text = "提名：",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    nomination.nominees.drop(1).forEach { nominee ->
                        NomineeItem(nominee, rank = "")
                    }
                }
            } else {
                // 显示暂定前三名
                if (nomination.nominees.isNotEmpty()) {
                    SingleLineText(
                        text = "暂定前三名：",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    nomination.nominees.forEachIndexed { index, nominee ->
                        val rank = when (index) {
                            0 -> "👑"
                            1 -> "🥈"
                            2 -> "🥉"
                            else -> ""
                        }
                        NomineeItem(nominee, rank)
                    }
                }
            }
        }
    }
}

/**
 * 获奖者卡片
 */
@Composable
private fun WinnerCard(winner: NomineeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SingleLineText(
                    text = "🏆",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                SingleLineText(
                    text = "获奖者",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SingleLineText(
                text = "《${winner.gameName}》",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row {
                SingleLineText(
                    text = "${if (winner.isPlayerGame) "你的公司" else winner.companyName} | ",
                    fontSize = 12.sp,
                    color = if (winner.isPlayerGame) Color(0xFF4CAF50) else Color.Gray
                )
                SingleLineText(
                    text = "评分：${String.format("%.1f", winner.rating)} | ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                SingleLineText(
                    text = "综合得分：${String.format("%.1f", winner.totalScore)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 提名项
 */
@Composable
private fun NomineeItem(nominee: NomineeInfo, rank: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rank.isNotEmpty()) {
            SingleLineText(
                text = rank,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            SingleLineText(
                text = "《${nominee.gameName}》",
                fontSize = 14.sp,
                color = Color.White
            )
            Row {
                SingleLineText(
                    text = "${if (nominee.isPlayerGame) "你的公司" else nominee.companyName} | ",
                    fontSize = 11.sp,
                    color = if (nominee.isPlayerGame) Color(0xFF4CAF50) else Color.Gray
                )
                SingleLineText(
                    text = "${String.format("%.1f", nominee.totalScore)}分",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 历史记录标签页
 */
@Composable
private fun HistoryTab(saveData: SaveData) {
    val history = saveData.gvaHistory
    
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SingleLineText(text = "🎖️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                SingleLineText(
                    text = "暂无历史记录",
                    fontSize = 16.sp,
                    color = Color.Gray
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
            items(history.sortedByDescending { it.year }) { nomination ->
                if (nomination.winner != null) {
                    HistoryCard(nomination)
                }
            }
        }
    }
}

/**
 * 历史记录卡片
 */
@Composable
private fun HistoryCard(nomination: AwardNomination) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SingleLineText(
                    text = "${nomination.year}年",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SingleLineText(
                        text = nomination.award.icon,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SingleLineText(
                        text = nomination.award.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                nomination.winner?.let { winner ->
                    Spacer(modifier = Modifier.height(4.dp))
                    SingleLineText(
                        text = "《${winner.gameName}》- ${winner.companyName}",
                        fontSize = 13.sp,
                        color = if (winner.isPlayerGame) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
            
            if (nomination.winner?.isPlayerGame == true) {
                SingleLineText(
                    text = "🏆",
                    fontSize = 32.sp
                )
            }
        }
    }
}

/**
 * 声望标签页
 */
@Composable
private fun ReputationTab(saveData: SaveData) {
    val reputation = saveData.companyReputation
    val level = reputation.getLevel()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 声望等级卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    SingleLineText(
                        text = "公司声望",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SingleLineText(
                            text = level.displayName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        SingleLineText(
                            text = "${reputation.reputationPoints} 点",
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 进度条
                    val progress = reputation.getProgressToNextLevel()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFFFFD700),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 效果说明
                    SingleLineText(
                        text = "当前效果：",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (level.recruitBonus > 0) {
                        SingleLineText(
                            text = "✓ 招聘吸引力 +${(level.recruitBonus * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    if (level.fansBonus > 0) {
                        SingleLineText(
                            text = "✓ 粉丝增长 +${(level.fansBonus * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    if (level.salesBonus > 0) {
                        SingleLineText(
                            text = "✓ 游戏初始销量 +${(level.salesBonus * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
        
        // 获奖统计
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SingleLineText(
                        text = "获奖统计",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "获奖",
                            value = reputation.awardHistory.size.toString(),
                            color = Color(0xFFFFD700)
                        )
                        StatItem(
                            label = "提名",
                            value = reputation.nominationHistory.size.toString(),
                            color = Color(0xFFC0C0C0)
                        )
                        StatItem(
                            label = "总计",
                            value = (reputation.awardHistory.size + reputation.nominationHistory.size).toString(),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SingleLineText(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        SingleLineText(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
