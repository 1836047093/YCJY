package com.example.yjcy.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * ⚠️ 标准化Text组件 - 防止换行和溢出问题 ⚠️
 * 
 * 使用规范：
 * 1. 所有短文本（标签、标题、数值）必须使用 SingleLineText
 * 2. 描述性长文本使用 MultiLineText 并指定 maxLines
 * 3. 禁止直接使用 Text() 而不设置 maxLines 和 overflow
 */

/**
 * 单行文本组件 - 自动截断，不换行
 * 
 * 适用场景：
 * - 标签："可用员工"、"总收入"
 * - 标题："游戏名称"、"公司名称"
 * - 数值："¥1000"、"1000人"
 * - 状态："开发中"、"已发售"
 */
@Composable
fun SingleLineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = 1,  // ⚠️ 强制单行
        overflow = TextOverflow.Ellipsis  // ⚠️ 超出显示...
    )
}

/**
 * 多行文本组件 - 限制最大行数，超出截断
 * 
 * 适用场景：
 * - 游戏描述
 * - 新闻内容
 * - 详细说明
 * - 提示信息
 */
@Composable
fun MultiLineText(
    text: String,
    maxLines: Int = 3,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,  // ⚠️ 限制行数
        overflow = TextOverflow.Ellipsis  // ⚠️ 超出显示...
    )
}

/**
 * 卡片标题文本 - 单行，加粗
 */
@Composable
fun CardTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 16.sp
) {
    SingleLineText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold
    )
}

/**
 * 数值显示文本 - 单行，居中
 */
@Composable
fun ValueText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 14.sp
) {
    SingleLineText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        textAlign = TextAlign.Center
    )
}

/**
 * 标签文本 - 单行，小号字体
 */
@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.7f),
    fontSize: TextUnit = 12.sp
) {
    SingleLineText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize
    )
}
