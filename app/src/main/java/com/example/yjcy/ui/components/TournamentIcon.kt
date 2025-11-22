package com.example.yjcy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 *炫酷动态赛事图标
 */
@Composable
fun TournamentIcon(
    tournamentType: String,
    modifier: Modifier = Modifier,
    size: Float = 60f
) {
    when (tournamentType) {
        "城市杯" -> BronzeTournamentIcon(modifier.size(size.dp), size)
        "全国锦标赛" -> SilverTournamentIcon(modifier.size(size.dp), size)
        "全球总决赛" -> DiamondTournamentIcon(modifier.size(size.dp), size)
        else -> DefaultTournamentIcon(modifier.size(size.dp), size)
    }
}

/**
 * 城市杯 - 青铜圆圈
 */
@Composable
fun BronzeTournamentIcon(
    modifier: Modifier = Modifier,
    size: Float
) {
    Canvas(modifier = modifier) {
        val centerX = size / 2
        val centerY = size / 2
        
        // 绘制简单的青铜色圆圈
        drawCircle(
            color = Color(0xFFCD7F32),
            radius = size * 0.4f,
            center = Offset(centerX, centerY),
            style = Stroke(width = size * 0.08f)
        )
        
        // 绘制内圈
        drawCircle(
            color = Color(0xFFB87333),
            radius = size * 0.3f,
            center = Offset(centerX, centerY),
            style = Stroke(width = size * 0.04f)
        )
    }
}

/**
 * 全国锦标赛 - 银色六边形
 */
@Composable
fun SilverTournamentIcon(
    modifier: Modifier = Modifier,
    size: Float
) {
    Canvas(modifier = modifier) {
        val centerX = size / 2
        val centerY = size / 2
        
        // 绘制简单的银色六边形
        val hexagonPath = Path().apply {
            val radius = size * 0.4f
            for (i in 0 until 6) {
                val angle = i * 60f * PI / 180f
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
            close()
        }
        
        drawPath(
            path = hexagonPath,
            color = Color(0xFFC0C0C0),
            style = Stroke(width = size * 0.06f)
        )
    }
}

/**
 * 全球总决赛 - 钻石形状
 */
@Composable
fun DiamondTournamentIcon(
    modifier: Modifier = Modifier,
    size: Float
) {
    Canvas(modifier = modifier) {
        val centerX = size / 2
        val centerY = size / 2
        
        // 绘制简单的钻石形状
        val diamondPath = Path().apply {
            moveTo(centerX, centerY - size * 0.4f)
            lineTo(centerX + size * 0.3f, centerY)
            lineTo(centerX, centerY + size * 0.4f)
            lineTo(centerX - size * 0.3f, centerY)
            close()
        }
        
        drawPath(
            path = diamondPath,
            color = Color(0xFF00BCD4),
            style = Stroke(width = size * 0.06f)
        )
    }
}

/**
 * 默认图标 - 简单圆圈
 */
@Composable
fun DefaultTournamentIcon(
    modifier: Modifier = Modifier,
    size: Float
) {
    Canvas(modifier = modifier) {
        val centerX = size / 2
        val centerY = size / 2
        
        // 绘制简单的灰色圆圈
        drawCircle(
            color = Color(0xFF6B7280),
            radius = size * 0.4f,
            center = Offset(centerX, centerY),
            style = Stroke(width = size * 0.08f)
        )
    }
}
