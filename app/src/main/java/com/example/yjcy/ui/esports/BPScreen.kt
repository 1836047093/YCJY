package com.example.yjcy.ui.esports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.*
import com.example.yjcy.managers.esports.HeroManager
import com.example.yjcy.ui.components.SingleLineText

/**
 * BP（Ban/Pick）界面
 * 玩家进行英雄禁用和选择
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BPScreen(
    match: ScheduledMatch,
    playerTeam: Team,
    isBlue: Boolean,  // 玩家是蓝方还是红方
    onComplete: (BPSession) -> Unit,
    onBack: () -> Unit
) {
    var bpSession by remember {
        mutableStateOf(
            BPSession(
                matchId = match.id,
                blueTeam = match.blueTeam,
                redTeam = match.redTeam
            )
        )
    }
    
    var currentPhase by remember { mutableIntStateOf(0) }  // 0-5: Ban阶段, 6-10: Pick阶段
    var selectedHeroId by remember { mutableStateOf<String?>(null) }
    var selectedPosition by remember { mutableStateOf<HeroPosition?>(null) }
    
    // BP顺序：蓝Ban-红Ban-蓝Ban-红Ban-蓝Ban-红Ban -> 蓝Pick-红Pick-红Pick-蓝Pick-蓝Pick-红Pick-红Pick-蓝Pick-蓝Pick-红Pick
    val bpOrder = listOf(
        // Ban阶段
        BPPhase(0, BPAction.BAN, TeamSide.BLUE),
        BPPhase(1, BPAction.BAN, TeamSide.RED),
        BPPhase(2, BPAction.BAN, TeamSide.BLUE),
        BPPhase(3, BPAction.BAN, TeamSide.RED),
        BPPhase(4, BPAction.BAN, TeamSide.BLUE),
        BPPhase(5, BPAction.BAN, TeamSide.RED),
        // Pick阶段
        BPPhase(6, BPAction.PICK, TeamSide.BLUE),
        BPPhase(7, BPAction.PICK, TeamSide.RED),
        BPPhase(8, BPAction.PICK, TeamSide.RED),
        BPPhase(9, BPAction.PICK, TeamSide.BLUE),
        BPPhase(10, BPAction.PICK, TeamSide.BLUE),
        BPPhase(11, BPAction.PICK, TeamSide.RED),
        BPPhase(12, BPAction.PICK, TeamSide.RED),
        BPPhase(13, BPAction.PICK, TeamSide.BLUE),
        BPPhase(14, BPAction.PICK, TeamSide.BLUE),
        BPPhase(15, BPAction.PICK, TeamSide.RED)
    )
    
    val currentBPPhase = if (currentPhase < bpOrder.size) bpOrder[currentPhase] else null
    val isPlayerTurn = currentBPPhase?.side == (if (isBlue) TeamSide.BLUE else TeamSide.RED)
    
    // 获取所有英雄
    val allHeroes = HeroManager.getAllHeroes()
    
    // 获取已Ban和已Pick的英雄ID
    val bannedHeroIds = bpSession.blueBans + bpSession.redBans
    val pickedHeroIds = bpSession.bluePicks.map { it.heroId } + bpSession.redPicks.map { it.heroId }
    val unavailableHeroIds = bannedHeroIds + pickedHeroIds
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
    ) {
        // 顶部栏
        TopAppBar(
            title = {
                Column {
                    SingleLineText(
                        text = "⚔️ BP阶段",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    SingleLineText(
                        text = "${match.blueTeam.name} vs ${match.redTeam.name}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0f3460)
            )
        )
        
        // BP状态显示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 当前阶段
                if (currentBPPhase != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleLineText(
                            text = if (currentBPPhase.action == BPAction.BAN) "🚫 Ban阶段" else "✅ Pick阶段",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = if (isPlayerTurn) "你的回合" else "对手回合",
                            fontSize = 14.sp,
                            color = if (isPlayerTurn) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier
                                .background(
                                    if (isPlayerTurn) Color(0xFF4CAF50).copy(alpha = 0.2f) 
                                    else Color.Gray.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                
                // Ban位显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 蓝方Ban
                    Column(modifier = Modifier.weight(1f)) {
                        SingleLineText(
                            text = "蓝方Ban (${bpSession.blueBans.size}/3)",
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) { index ->
                                BanSlot(
                                    heroId = bpSession.blueBans.getOrNull(index),
                                    allHeroes = allHeroes
                                )
                            }
                        }
                    }
                    
                    // 红方Ban
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        SingleLineText(
                            text = "红方Ban (${bpSession.redBans.size}/3)",
                            fontSize = 12.sp,
                            color = Color(0xFFFF5252)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) { index ->
                                BanSlot(
                                    heroId = bpSession.redBans.getOrNull(index),
                                    allHeroes = allHeroes
                                )
                            }
                        }
                    }
                }
                
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                
                // Pick位显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 蓝方Pick
                    Column(modifier = Modifier.weight(1f)) {
                        SingleLineText(
                            text = "蓝方阵容 (${bpSession.bluePicks.size}/5)",
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            HeroPosition.values().forEach { position ->
                                PickSlot(
                                    position = position,
                                    pickedHero = bpSession.bluePicks.find { it.position == position },
                                    allHeroes = allHeroes
                                )
                            }
                        }
                    }
                    
                    // 红方Pick
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        SingleLineText(
                            text = "红方阵容 (${bpSession.redPicks.size}/5)",
                            fontSize = 12.sp,
                            color = Color(0xFFFF5252)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            HeroPosition.values().forEach { position ->
                                PickSlot(
                                    position = position,
                                    pickedHero = bpSession.redPicks.find { it.position == position },
                                    allHeroes = allHeroes
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 英雄选择区域
        if (isPlayerTurn && currentBPPhase != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // Pick阶段需要先选择位置
                if (currentBPPhase.action == BPAction.PICK) {
                    SingleLineText(
                        text = "1. 选择位置",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        HeroPosition.values().forEach { position ->
                            val alreadyPicked = if (isBlue) {
                                bpSession.bluePicks.any { it.position == position }
                            } else {
                                bpSession.redPicks.any { it.position == position }
                            }
                            
                            Button(
                                onClick = { selectedPosition = position },
                                enabled = !alreadyPicked,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPosition == position) Color(0xFF2196F3) else Color(0xFF16213e),
                                    disabledContainerColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                SingleLineText(
                                    text = when(position) {
                                        HeroPosition.TOP -> "上"
                                        HeroPosition.JUNGLE -> "野"
                                        HeroPosition.MID -> "中"
                                        HeroPosition.ADC -> "下"
                                        HeroPosition.SUPPORT -> "辅"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                
                SingleLineText(
                    text = if (currentBPPhase.action == BPAction.BAN) "选择要Ban的英雄" else "2. 选择英雄",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 英雄网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 筛选英雄
                    val availableHeroes = allHeroes.filter { hero ->
                        // 不能选择已Ban或已Pick的英雄
                        hero.id !in unavailableHeroIds &&
                        // Pick阶段需要匹配位置
                        (currentBPPhase.action == BPAction.BAN || selectedPosition == null || hero.position == selectedPosition) &&
                        // Pick阶段需要检查选手英雄池
                        (currentBPPhase.action == BPAction.BAN || selectedPosition == null || 
                         playerTeam.players.any { player -> 
                             player.position == selectedPosition && 
                             player.heroPool.any { it.heroId == hero.id && it.proficiency >= 30 }
                         })
                    }
                    
                    items(availableHeroes) { hero ->
                        HeroSelectCard(
                            hero = hero,
                            isSelected = selectedHeroId == hero.id,
                            onClick = { selectedHeroId = hero.id }
                        )
                    }
                }
            }
        } else {
            // 对手回合，显示等待
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                    SingleLineText(
                        text = "等待对手选择...",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // 确认按钮
        if (isPlayerTurn && currentBPPhase != null) {
            Button(
                onClick = {
                    if (selectedHeroId != null) {
                        // 执行Ban或Pick
                        if (currentBPPhase.action == BPAction.BAN) {
                            if (isBlue) {
                                bpSession.blueBans.add(selectedHeroId!!)
                            } else {
                                bpSession.redBans.add(selectedHeroId!!)
                            }
                        } else if (selectedPosition != null) {
                            // 找到对应位置的选手
                            val player = playerTeam.players.find { it.position == selectedPosition }
                            if (player != null) {
                                val proficiency = player.heroPool.find { it.heroId == selectedHeroId }?.proficiency ?: 50
                                val pickedHero = PickedHero(
                                    heroId = selectedHeroId!!,
                                    playerId = player.id,
                                    position = selectedPosition!!,
                                    proficiency = proficiency
                                )
                                if (isBlue) {
                                    bpSession.bluePicks.add(pickedHero)
                                } else {
                                    bpSession.redPicks.add(pickedHero)
                                }
                            }
                        }
                        
                        // 重置选择
                        selectedHeroId = null
                        selectedPosition = null
                        
                        // 进入下一阶段
                        currentPhase++
                        
                        // 如果是对手回合，自动选择
                        while (currentPhase < bpOrder.size && !isPlayerTurn) {
                            val phase = bpOrder[currentPhase]
                            // AI自动选择（简化实现）
                            val opponentTeam = if (isBlue) match.redTeam else match.blueTeam
                            val availableHeroes = allHeroes.filter { it.id !in unavailableHeroIds }
                            
                            if (phase.action == BPAction.BAN && availableHeroes.isNotEmpty()) {
                                val randomHero = availableHeroes.random()
                                if (phase.side == TeamSide.BLUE) {
                                    bpSession.blueBans.add(randomHero.id)
                                } else {
                                    bpSession.redBans.add(randomHero.id)
                                }
                            } else if (phase.action == BPAction.PICK) {
                                // 找到还没选的位置
                                val pickedPositions = if (phase.side == TeamSide.BLUE) {
                                    bpSession.bluePicks.map { it.position }
                                } else {
                                    bpSession.redPicks.map { it.position }
                                }
                                val remainingPositions = HeroPosition.values().filter { it !in pickedPositions }
                                
                                if (remainingPositions.isNotEmpty()) {
                                    val position = remainingPositions.first()
                                    val player = opponentTeam.players.find { it.position == position }
                                    val suitableHeroes = availableHeroes.filter { it.position == position }
                                    
                                    if (player != null && suitableHeroes.isNotEmpty()) {
                                        val randomHero = suitableHeroes.random()
                                        val proficiency = player.heroPool.find { it.heroId == randomHero.id }?.proficiency ?: 50
                                        val pickedHero = PickedHero(
                                            heroId = randomHero.id,
                                            playerId = player.id,
                                            position = position,
                                            proficiency = proficiency
                                        )
                                        if (phase.side == TeamSide.BLUE) {
                                            bpSession.bluePicks.add(pickedHero)
                                        } else {
                                            bpSession.redPicks.add(pickedHero)
                                        }
                                    }
                                }
                            }
                            
                            currentPhase++
                        }
                        
                        // BP完成
                        if (currentPhase >= bpOrder.size) {
                            bpSession.isCompleted = true
                            onComplete(bpSession)
                        }
                    }
                },
                enabled = selectedHeroId != null && (currentBPPhase.action == BPAction.BAN || selectedPosition != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                SingleLineText(
                    text = if (currentBPPhase.action == BPAction.BAN) "确认Ban" else "确认Pick",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BanSlot(heroId: String?, allHeroes: List<MobaHero>) {
    val hero = heroId?.let { id -> allHeroes.find { it.id == id } }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (hero != null) Color(0xFFFF5252).copy(alpha = 0.3f) else Color(0xFF2A2A3E),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (hero != null) {
            SingleLineText(
                text = hero.name.take(1),
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        } else {
            SingleLineText(
                text = "?",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PickSlot(position: HeroPosition, pickedHero: PickedHero?, allHeroes: List<MobaHero>) {
    val hero = pickedHero?.let { pick -> allHeroes.find { it.id == pick.heroId } }
    
    Row(
        modifier = Modifier
            .height(32.dp)
            .background(Color(0xFF2A2A3E), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SingleLineText(
            text = when(position) {
                HeroPosition.TOP -> "上"
                HeroPosition.JUNGLE -> "野"
                HeroPosition.MID -> "中"
                HeroPosition.ADC -> "下"
                HeroPosition.SUPPORT -> "辅"
            },
            fontSize = 12.sp,
            color = Color.Gray
        )
        
        if (hero != null) {
            SingleLineText(
                text = hero.name,
                fontSize = 12.sp,
                color = Color.White
            )
        } else {
            SingleLineText(
                text = "未选择",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun HeroSelectCard(hero: MobaHero, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color(0xFF16213e)
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2196F3)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SingleLineText(
                text = hero.name,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            SingleLineText(
                text = hero.title,
                fontSize = 10.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when(hero.position) {
                    HeroPosition.TOP -> "上"
                    HeroPosition.JUNGLE -> "野"
                    HeroPosition.MID -> "中"
                    HeroPosition.ADC -> "下"
                    HeroPosition.SUPPORT -> "辅"
                },
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0xFF2196F3).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
