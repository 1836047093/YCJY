package com.example.yjcy.utils

/**
 * 资金格式化函数
 */
fun formatMoney(amount: Long): String {
    return when {
        amount >= 1_000_000_000_000L -> "${amount / 1_000_000_000_000L}T"
        amount >= 1_000_000_000L -> "${amount / 1_000_000_000L}B"
        amount >= 1_000_000L -> "${amount / 1_000_000L}M"
        amount >= 1_000L -> "${amount / 1_000L}K"
        else -> amount.toString()
    }
}

/**
 * 增强版资金格式化函数，支持保留两位小数
 */
fun formatMoneyWithDecimals(amount: Double): String {
    return when {
        amount >= 1_000_000_000_000.0 -> String.format("%.2fT", amount / 1_000_000_000_000.0)
        amount >= 1_000_000_000.0 -> String.format("%.2fB", amount / 1_000_000_000.0)
        amount >= 1_000_000.0 -> String.format("%.2fM", amount / 1_000_000.0)
        amount >= 1_000.0 -> String.format("%.2fK", amount / 1_000.0)
        else -> String.format("%.2f", amount)
    }
}