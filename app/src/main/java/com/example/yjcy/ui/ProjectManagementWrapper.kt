package com.example.yjcy.ui

import com.example.yjcy.data.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.yjcy.ui.GameTheme



/**
 * 项目管理包装器组件
 * 提供员工分配功能的项目管理界面
 */
@Composable
fun ProjectManagementWrapper(
    games: List<Game> = emptyList(),
    onGamesUpdate: (List<Game>) -> Unit = {},
    founder: Founder? = null,
    allEmployees: List<Employee> = emptyList()
) {
    // 直接使用员工分配功能的项目管理
    EnhancedProjectManagementContent(
                    founder = founder,
                    availableEmployees = allEmployees,
                    games = games,
                    onGamesUpdate = { updatedGames ->
                        onGamesUpdate(updatedGames)
                    }
    )
}