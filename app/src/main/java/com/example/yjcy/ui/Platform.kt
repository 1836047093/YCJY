package com.example.yjcy.ui

enum class Platform(val displayName: String, val icon: String, val developmentCost: Int) {
    WEB("网页", "🌐", 50000),
    PC("PC", "💻", 100000),
    MOBILE("手机", "📱", 300000),
    CONSOLE("主机", "🎮", 500000)
}