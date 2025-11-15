package com.example.yjcy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.yjcy.data.ChatMessage
import com.example.yjcy.data.MessageSender
import com.example.yjcy.data.SecretaryReplyManager
import com.example.yjcy.ui.composables.FpsMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 秘书聊天界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryChatScreen(navController: NavController) {
    var messages by remember { 
        mutableStateOf<List<ChatMessage>>(
            listOf(
                ChatMessage(
                    sender = MessageSender.SECRETARY,
                    content = SecretaryReplyManager.WELCOME_MESSAGE
                )
            )
        ) 
    }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 自动滚动到最新消息
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                delay(100) // 等待动画完成
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 秘书头像
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B9D),
                                                Color(0xFFC06C84)
                                            )
                                        )
                                    )
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👩‍💼",
                                    fontSize = 24.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // 秘书标题
                            Column {
                                Text(
                                    text = "秘书",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                // 在线状态指示
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "在线",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF667eea)
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                // 聊天消息列表
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                        ) {
                            ChatMessageItem(message = message)
                        }
                    }
                    
                    // 输入中提示
                    if (isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
                
                // 输入框区域
                ChatInputBar(
                    inputText = inputText,
                    onInputTextChange = { inputText = it },
                    onSendClick = {
                        if (inputText.isNotBlank()) {
                            val messageContent = inputText.trim()
                            
                            // 检查敏感词
                            if (SecretaryReplyManager.containsSensitiveWords(messageContent)) {
                                // 如果包含敏感词，直接显示警告消息
                                val warningMessage = ChatMessage(
                                    sender = MessageSender.SECRETARY,
                                    content = "⚠️ 检测到敏感词，请不要讨论政治相关话题哦，老板！"
                                )
                                messages = messages + warningMessage
                                inputText = "" // 清空输入框
                                return@ChatInputBar
                            }
                            
                            // 添加玩家消息
                            val playerMessage = ChatMessage(
                                sender = MessageSender.PLAYER,
                                content = messageContent
                            )
                            messages = messages + playerMessage
                            
                            // 清空输入框
                            inputText = ""
                            
                            // 模拟秘书打字延迟
                            coroutineScope.launch {
                                isTyping = true
                                delay(800 + (messageContent.length * 50L).coerceAtMost(2000))
                                isTyping = false
                                
                                // 生成秘书回复
                                val reply = SecretaryReplyManager.generateReply(messageContent)
                                val secretaryMessage = ChatMessage(
                                    sender = MessageSender.SECRETARY,
                                    content = reply
                                )
                                messages = messages + secretaryMessage
                            }
                        }
                    }
                )
            }
        }
        
        // FPS监测（左上角）
        FpsMonitor(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
    }
}

/**
 * 聊天消息项
 */
@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isSecretary = message.sender == MessageSender.SECRETARY
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSecretary) Arrangement.Start else Arrangement.End
    ) {
        if (isSecretary) {
            // 秘书头像
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B9D),
                                Color(0xFFC06C84)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👩‍💼",
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // 消息气泡
        Column(
            horizontalAlignment = if (isSecretary) Alignment.Start else Alignment.End,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // 消息内容
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isSecretary) 4.dp else 16.dp,
                    topEnd = if (isSecretary) 16.dp else 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isSecretary) Color.White else Color(0xFF667eea),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    color = if (isSecretary) Color(0xFF333333) else Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            
            // 时间戳
            Text(
                text = message.getFormattedTime(),
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp)
            )
        }
        
        if (!isSecretary) {
            Spacer(modifier = Modifier.width(8.dp))
            
            // 玩家头像
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF388E3C)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "😎",
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * 打字中指示器
 */
@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // 秘书头像
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF6B9D),
                            Color(0xFFC06C84)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👩‍💼",
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 打字中气泡
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    TypingDot(initialDelay = index * 200)
                }
            }
        }
    }
}

/**
 * 打字中动画点
 */
@Composable
fun TypingDot(initialDelay: Int) {
    var alpha by remember { mutableStateOf(0.3f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(initialDelay.toLong())
            alpha = 1f
            delay(400)
            alpha = 0.3f
            delay(200)
        }
    }
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = alpha))
    )
}

/**
 * 聊天输入栏
 */
@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 输入框
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp),
                placeholder = {
                    Text(
                        text = "输入框",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedContainerColor = Color(0xFFF8F8F8)
                ),
                singleLine = false,
                maxLines = 4
            )
            
            // 发送按钮
            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    ),
                enabled = inputText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = Color.White
                )
            }
        }
    }
}
