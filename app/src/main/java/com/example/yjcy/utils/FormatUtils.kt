package com.example.yjcy.utils

import java.util.Calendar
import java.util.Date

/**
 * 资金格式化函数
 * 支持正数和负数的 万/亿/兆 单位显示，带小数位
 */
fun formatMoney(amount: Long): String {
    val absAmount = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    
    return when {
        absAmount >= 1_000_000_000_000L -> {
            val value = absAmount / 1_000_000_000_000.0
            "$sign${String.format("%.2f", value)}兆"
        }
        absAmount >= 100_000_000L -> {
            val value = absAmount / 100_000_000.0
            "$sign${String.format("%.2f", value)}亿"
        }
        absAmount >= 10_000L -> {
            val value = absAmount / 10_000.0
            "$sign${String.format("%.2f", value)}万"
        }
        else -> amount.toString()
    }
}

/**
 * 增强版资金格式化函数，支持保留两位小数
 * 支持正数和负数的 万/亿/兆 单位显示
 */
fun formatMoneyWithDecimals(amount: Double): String {
    val absAmount = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    
    return when {
        absAmount >= 1_000_000_000_000.0 -> String.format("%s%.2f兆", sign, absAmount / 1_000_000_000_000.0)
        absAmount >= 100_000_000.0 -> String.format("%s%.2f亿", sign, absAmount / 100_000_000.0)
        absAmount >= 10_000.0 -> String.format("%s%.2f万", sign, absAmount / 10_000.0)
        else -> String.format("%.2f", amount)
    }
}

/**
 * 根据游戏上线日期和记录日期，计算该记录属于游戏的第几年
 * 
 * @param releaseYear 游戏上线年份
 * @param releaseMonth 游戏上线月份
 * @param releaseDay 游戏上线日期
 * @param recordDate 记录的日期
 * @return 游戏内的年份（1表示第1年，2表示第2年，以此类推）
 */
fun calculateGameYear(
    releaseYear: Int,
    releaseMonth: Int,
    releaseDay: Int,
    recordDate: Date
): Int {
    // 创建游戏上线日期的Calendar
    val releaseCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, releaseYear)
        set(Calendar.MONTH, releaseMonth - 1) // Calendar的月份从0开始
        set(Calendar.DAY_OF_MONTH, releaseDay)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    // 创建记录日期的Calendar
    val recordCalendar = Calendar.getInstance().apply {
        time = recordDate
    }
    
    // 计算从上线日期到记录日期经过的天数
    val releaseTimeMillis = releaseCalendar.timeInMillis
    val recordTimeMillis = recordCalendar.timeInMillis
    val daysPassed = ((recordTimeMillis - releaseTimeMillis) / (1000 * 60 * 60 * 24)).toInt()
    
    // 计算游戏内年份（每365天为一年）
    val gameYear = (daysPassed / 365) + 1
    
    return gameYear.coerceAtLeast(1) // 确保至少返回1（第1年）
}
