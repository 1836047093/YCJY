package com.example.yjcy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalTextStyle
import com.example.yjcy.data.ChatMessage
import com.example.yjcy.data.MessageSender
import com.example.yjcy.data.SecretaryReplyManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 秘书聊天对话框（类似宣传中心样式）
 * @param messages 聊天记录列表（从外部传入，对话框关闭后不会丢失）
 * @param onMessagesChange 更新聊天记录的回调
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun SecretaryChatDialog(
    messages: List<ChatMessage>,
    onMessagesChange: (List<ChatMessage>) -> Unit,
    onDismiss: () -> Unit
) {
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
    
    // 小型对话框样式
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D3748),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
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
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👩‍💼",
                            fontSize = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // 秘书标题和状态
                    Column {
                        Text(
                            text = "秘书",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "在线",
                                fontSize = 11.sp,
                                color = Color(0xFFB0B0B0)
                            )
                        }
                    }
                }
                
                // 右上角关闭按钮
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                // 聊天消息列表
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                        ) {
                            CompactChatMessageItem(message = message)
                        }
                    }
                    
                    // 输入中提示
                    if (isTyping) {
                        item {
                            CompactTypingIndicator()
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 分割线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 输入框区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 100.dp),
                        placeholder = {
                            Text(
                                text = "输入框",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            unfocusedBorderColor = Color(0xFF4B5563),
                            focusedContainerColor = Color(0xFF374151),
                            unfocusedContainerColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF667eea)
                        ),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                    
                    // 发送按钮
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val messageContent = inputText.trim()
                                
                                // 检查敏感词
                                if (SecretaryReplyManager.containsSensitiveWords(messageContent)) {
                                    // 如果包含敏感词，直接显示警告消息
                                    val warningMessage = ChatMessage(
                                        sender = MessageSender.SECRETARY,
                                        content = "⚠️ 检测到敏感词，请不要讨论政治相关话题哦，老板！"
                                    )
                                    onMessagesChange(messages + warningMessage)
                                    inputText = "" // 清空输入框
                                    return@IconButton
                                }
                                
                                // 添加玩家消息
                                val playerMessage = ChatMessage(
                                    sender = MessageSender.PLAYER,
                                    content = messageContent
                                )
                                onMessagesChange(messages + playerMessage)
                                
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
                                    onMessagesChange(messages + secretaryMessage)
                                }
                            }
                        },
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
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { }
    )
}

/**
 * 紧凑型聊天消息项
 */
@Composable
fun CompactChatMessageItem(message: ChatMessage) {
    val isSecretary = message.sender == MessageSender.SECRETARY
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSecretary) Arrangement.Start else Arrangement.End
    ) {
        if (isSecretary) {
            // 秘书头像
            Box(
                modifier = Modifier
                    .size(28.dp)
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
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(6.dp))
        }
        
        // 消息气泡
        Column(
            horizontalAlignment = if (isSecretary) Alignment.Start else Alignment.End,
            modifier = Modifier.widthIn(max = 220.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isSecretary) 4.dp else 16.dp,
                    topEnd = if (isSecretary) 16.dp else 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isSecretary) Color(0xFF374151) else Color(0xFF667eea),
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            
            Text(
                text = message.getFormattedTime(),
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(start = 4.dp, top = 3.dp, end = 4.dp)
            )
        }
        
        if (!isSecretary) {
            Spacer(modifier = Modifier.width(6.dp))
            
            // 玩家头像
            Box(
                modifier = Modifier
                    .size(28.dp)
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
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 紧凑型打字中指示器
 */
@Composable
fun CompactTypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
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
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF374151),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(3) { index ->
                    TypingDot(initialDelay = index * 200)
                }
            }
        }
    }
}
