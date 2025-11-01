package com.example.yjcy.utils

import java.util.Calendar
import java.util.Date

/**
 * 资金格式化函数
 * 支持正数和负数的 K/M/B/T 单位显示，带小数位
 */
fun formatMoney(amount: Long): String {
    val absAmount = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    
    return when {
        absAmount >= 1_000_000_000_000L -> {
            val value = absAmount / 1_000_000_000_000.0
            "$sign${String.format("%.2f", value)}T"
        }
        absAmount >= 1_000_000_000L -> {
            val value = absAmount / 1_000_000_000.0
            "$sign${String.format("%.2f", value)}B"
        }
        absAmount >= 1_000_000L -> {
            val value = absAmount / 1_000_000.0
            "$sign${String.format("%.2f", value)}M"
        }
        absAmount >= 1_000L -> {
            val value = absAmount / 1_000.0
            "$sign${String.format("%.2f", value)}K"
        }
        else -> amount.toString()
    }
}

/**
 * 增强版资金格式化函数，支持保留两位小数
 * 支持正数和负数的 K/M/B/T 单位显示
 */
fun formatMoneyWithDecimals(amount: Double): String {
    val absAmount = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    
    return when {
        absAmount >= 1_000_000_000_000.0 -> String.format("%s%.2fT", sign, absAmount / 1_000_000_000_000.0)
        absAmount >= 1_000_000_000.0 -> String.format("%s%.2fB", sign, absAmount / 1_000_000_000.0)
        absAmount >= 1_000_000.0 -> String.format("%s%.2fM", sign, absAmount / 1_000_000.0)
        absAmount >= 1_000.0 -> String.format("%s%.2fK", sign, absAmount / 1_000.0)
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

/**
 * 计算游戏内日期的星期几
 * 以第1年1月1日为基准（假设是星期一），计算当前日期是星期几
 * 
 * @param year 游戏年份
 * @param month 游戏月份
 * @param day 游戏日期
 * @return 星期几（1=星期一，2=星期二，...，7=星期日）
 */
fun calculateWeekday(year: Int, month: Int, day: Int): Int {
    // 计算从第1年1月1日到当前日期的总天数
    // 假设每月30天，12月有31天
    var totalDays = 0
    
    // 计算年份差的天数
    // 每年361天（11个月×30天 + 12月31天）
    if (year > 1) {
        totalDays += (year - 1) * 361 // 前几年的天数
    }
    
    // 计算当前年份内的天数
    if (month > 1) {
        totalDays += (month - 1) * 30 // 前几个月的天数（每月30天）
    }
    
    // 加上当前月的天数（减1是因为第1天是第0天）
    totalDays += day - 1
    
    // 第1年1月1日是星期一（1），所以加上偏移量
    // (totalDays % 7) 得到0-6的值，然后+1得到1-7（星期一到星期日）
    return (totalDays % 7) + 1
}

/**
 * 获取星期几的中文显示
 * 
 * @param weekday 星期几（1=星期一，2=星期二，...，7=星期日）
 * @return 中文星期几
 */
fun getWeekdayName(weekday: Int): String {
    return when (weekday) {
        1 -> "星期一"
        2 -> "星期二"
        3 -> "星期三"
        4 -> "星期四"
        5 -> "星期五"
        6 -> "星期六"
        7 -> "星期日"
        else -> "未知"
    }
}

/**
 * 根据游戏内日期和当天分钟数计算显示时间（HH:mm格式）
 * 
 * @param minuteOfDay 当天内的分钟数（0-1439，0表示00:00，1439表示23:59）
 * @return 时间字符串（HH:mm格式）
 */
fun calculateGameTime(minuteOfDay: Int): String {
    val hour = minuteOfDay / 60 // 计算小时数（0-23）
    val minute = minuteOfDay % 60 // 计算分钟数（0-59）
    return String.format("%02d:%02d", hour, minute)
}