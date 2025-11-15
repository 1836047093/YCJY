package com.example.yjcy.ui.composables

import android.view.Choreographer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * FPS 监控组件（简化版）
 */
@Composable
fun FpsMonitor(modifier: Modifier = Modifier) {
    var fps by remember { mutableIntStateOf(0) }
    var lastTs by remember { mutableLongStateOf(System.nanoTime()) }
    var frames by remember { mutableIntStateOf(0) }
    
    DisposableEffect(Unit) {
        val cb = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frames++
                val elapsed = frameTimeNanos - lastTs
                if (elapsed >= 1_000_000_000L) {
                    fps = frames
                    frames = 0
                    lastTs = frameTimeNanos
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(cb)
        onDispose { Choreographer.getInstance().removeFrameCallback(cb) }
    }
    
    Box(
        modifier = modifier
            .width(42.dp)
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$fps",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                fps >= 110 -> Color(0xFF10B981) // 绿色：性能优秀
                fps >= 55 -> Color(0xFF3B82F6) // 蓝色：性能良好
                fps >= 30 -> Color(0xFFF59E0B) // 黄色：性能一般
                else -> Color(0xFFEF4444) // 红色：性能较差
            },
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 隐私政策对话框
 */
@Composable
fun PrivacyPolicyDialog(onAgree: () -> Unit, onReject: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("隐私政策") },
        text = { Text("为确保合规，请同意隐私政策以继续使用。") },
        confirmButton = { TextButton(onClick = onAgree) { Text("同意") } },
        dismissButton = { TextButton(onClick = onReject) { Text("拒绝") } }
    )
}
