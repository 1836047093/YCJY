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
    var showHelpDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
    ) {
        // 顶部标题栏
        GVATopBar(
            onBack = onBack,
            onHelp = { showHelpDialog = true }
        )
        
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
    
    // 功能介绍对话框
    if (showHelpDialog) {
        GVAHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

/**
 * 顶部标题栏
 */
@Composable
private fun GVATopBar(onBack: () -> Unit, onHelp: () -> Unit) {
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
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onHelp) {
            SingleLineText(
                text = "❓",
                fontSize = 20.sp,
                color = Color(0xFFFFD700)
            )
        }
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
            SingleLineText(
                text = if (winner.isPlayerGame) "你的公司" else winner.companyName,
                fontSize = 12.sp,
                color = if (winner.isPlayerGame) Color(0xFF4CAF50) else Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                SingleLineText(
                    text = "⭐开发质量 ${String.format("%.1f", winner.rating)} | ",
                    fontSize = 12.sp,
                    color = Color(0xFFFFD700)
                )
                SingleLineText(
                    text = "📊综合得分 ${String.format("%.1f", winner.totalScore)}",
                    fontSize = 12.sp,
                    color = Color(0xFF64B5F6)
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
            SingleLineText(
                text = if (nominee.isPlayerGame) "你的公司" else nominee.companyName,
                fontSize = 11.sp,
                color = if (nominee.isPlayerGame) Color(0xFF4CAF50) else Color.Gray
            )
            Row {
                SingleLineText(
                    text = "⭐开发质量 ${String.format("%.1f", nominee.rating)} | ",
                    fontSize = 11.sp,
                    color = Color(0xFFFFD700)
                )
                SingleLineText(
                    text = "📊综合得分 ${String.format("%.1f", nominee.totalScore)}",
                    fontSize = 11.sp,
                    color = Color(0xFF64B5F6)
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

/**
 * 功能介绍对话框
 */
@Composable
private fun GVAHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SingleLineText(
                    text = "🏆",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                SingleLineText(
                    text = "GVA游戏大奖 - 功能介绍",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 什么是GVA
                item {
                    HelpSection(
                        title = "📖 什么是GVA？",
                        content = "GVA（Game Video Awards）是游戏内年度评选系统，每年12月会评选出当年最优秀的游戏作品。你的游戏可以和AI竞争对手同台竞技，争夺21个不同奖项！"
                    )
                }
                
                // 参赛条件
                item {
                    HelpSection(
                        title = "🎯 自动参赛条件",
                        content = "✅ 游戏已发售\n✅ 评分≥6.0\n✅ 在当年1-12月发售\n\n符合条件的游戏会自动参与评选，无需手动报名！"
                    )
                }
                
                // 评选时间线
                item {
                    HelpSection(
                        title = "📅 评选时间线",
                        content = "• 12月15日：公布初步提名（前3名）\n• 12月16-30日：最后冲刺期\n• 12月31日：颁奖典礼！\n  - 确定获奖者（第1名）\n  - 自动发放奖励\n  - 弹出颁奖对话框"
                    )
                }
                
                // 奖项分类
                item {
                    HelpSection(
                        title = "🏅 21个奖项",
                        content = "🎮 综合类（4个）：\n• 🏆 年度游戏：¥50万\n• 💎 最佳独立：¥20万\n• ❤️ 最受玩家喜爱：¥15万\n• 🌐 最佳网游：¥30万\n\n🎯 主题类（12个）：\n每个游戏类型都有专属奖项\n奖励：¥10万 + 5千粉丝\n\n⭐ 特殊成就（5个）：\n• 💡 创新先锋（团队≤3人）\n• ⭐ 完美品质（评分≥9.0）\n• 💰 商业奇迹（百万销量）\n• 🌲 长青树（运营≥2年）\n• 🎭 文化影响力（50万粉丝）"
                    )
                }
                
                // 奖励说明
                item {
                    HelpSection(
                        title = "🎁 奖励发放",
                        content = "🏆 获奖（第1名）：\n• 100%奖金和粉丝\n• 游戏添加奖项徽章\n• 增加公司声望\n\n🥈 提名（第2-3名）：\n• 20%奖金和粉丝\n• 10点声望\n\n所有奖励自动发放！"
                    )
                }
                
                // 声望系统
                item {
                    HelpSection(
                        title = "👑 声望等级",
                        content = "声望提供永久加成：\n\n🏢 无名小厂（0点）\n🌱 新兴工作室（100点）：招聘+5%\n⭐ 知名厂商（300点）：招聘+10%，粉丝+10%\n🏆 一线大厂（600点）：招聘+15%，粉丝+20%，销量+10%\n👑 业界传奇（1000点）：招聘+25%，粉丝+30%，销量+20%"
                    )
                }
                
                // 获奖策略
                item {
                    HelpSection(
                        title = "💡 获奖策略",
                        content = "🌟 新手期：瞄准主题类奖项\n• 评分7.0-8.0\n• 专注单一类型\n\n💎 成长期：挑战最佳独立\n• 小团队（≤3人）\n• 评分≥8.5\n\n🏆 成熟期：冲击年度游戏\n• 满配团队\n• 评分9.0+\n• 大量宣传\n\n🎯 长期：特殊成就\n• 商业奇迹：百万销量\n• 长青树：运营2年+\n• 文化影响：50万粉丝"
                    )
                }
                
                // 底部提示
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f))
                    ) {
                        MultiLineText(
                            text = "💡 提示：一款游戏可以同时获得多个奖项，所有奖励会叠加发放！",
                            fontSize = 13.sp,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(12.dp),
                            maxLines = 3
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                SingleLineText(
                    text = "我知道了",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFF16213e),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

/**
 * 帮助章节
 */
@Composable
private fun HelpSection(title: String, content: String) {
    Column {
        SingleLineText(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        MultiLineText(
            text = content,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 50
        )
    }
}
