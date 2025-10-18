package com.example.yjcy.utils

/**
 * 资金格式化函数
 * 支持正数和负数的 K/M/B/T 单位显示
 */
fun formatMoney(amount: Long): String {
    val absAmount = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    
    return when {
        absAmount >= 1_000_000_000_000L -> "$sign${absAmount / 1_000_000_000_000L}T"
        absAmount >= 1_000_000_000L -> "$sign${absAmount / 1_000_000_000L}B"
        absAmount >= 1_000_000L -> "$sign${absAmount / 1_000_000L}M"
        absAmount >= 1_000L -> "$sign${absAmount / 1_000L}K"
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