package com.example.yjcy.ui

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

/**
 * 项目管理包装器组件
 * 提供员工分配功能的项目管理界面
 */
@Composable
fun ProjectManagementWrapper(
    games: List<com.example.yjcy.Game> = emptyList(),
    onGamesUpdate: (List<com.example.yjcy.Game>) -> Unit = {},
    founder: com.example.yjcy.Founder? = null
) {
    // 直接使用员工分配功能的项目管理
    EnhancedProjectManagementContent(
                    founder = founder,
                    games = games.map { originalGame ->
                        // 将原始Game转换为增强版Game
                        Game(
                            id = originalGame.id,
                            name = originalGame.name,
                            theme = when(originalGame.theme) {
                                com.example.yjcy.GameTheme.ACTION -> GameTheme.ACTION
                                com.example.yjcy.GameTheme.ADVENTURE -> GameTheme.ADVENTURE
                                com.example.yjcy.GameTheme.RPG -> GameTheme.RPG
                                com.example.yjcy.GameTheme.STRATEGY -> GameTheme.STRATEGY
                                com.example.yjcy.GameTheme.SIMULATION -> GameTheme.SIMULATION
                                com.example.yjcy.GameTheme.PUZZLE -> GameTheme.PUZZLE
                                com.example.yjcy.GameTheme.RACING -> GameTheme.RACING
                                com.example.yjcy.GameTheme.SPORTS -> GameTheme.SPORTS
                                com.example.yjcy.GameTheme.HORROR -> GameTheme.ACTION // 映射到最接近的类型
                                com.example.yjcy.GameTheme.CASUAL -> GameTheme.PUZZLE // 映射到最接近的类型
                            },
                            platforms = originalGame.platforms.map { platform ->
                                when(platform) {
                                    com.example.yjcy.Platform.PC -> Platform.PC
                                    com.example.yjcy.Platform.MOBILE -> Platform.MOBILE
                                    com.example.yjcy.Platform.CONSOLE -> Platform.CONSOLE
                                    com.example.yjcy.Platform.WEB -> Platform.WEB
                                }
                            },
                            businessModel = when(originalGame.businessModel) {
                                com.example.yjcy.BusinessModel.SINGLE_PLAYER -> BusinessModel.SINGLE_PLAYER
                                com.example.yjcy.BusinessModel.ONLINE_GAME -> BusinessModel.ONLINE_GAME
                            },
                            developmentProgress = originalGame.developmentProgress,
                            isCompleted = originalGame.isCompleted,
                            revenue = originalGame.revenue,
                            assignedEmployees = emptyList() // 初始为空，可以通过分配功能添加
                        )
                    },
                    onGamesUpdate = { enhancedGames ->
                        // 将增强版Game转换回原始Game
                        val originalGames = enhancedGames.map { enhancedGame ->
                            com.example.yjcy.Game(
                                id = enhancedGame.id,
                                name = enhancedGame.name,
                                theme = when(enhancedGame.theme) {
                                    GameTheme.ACTION -> com.example.yjcy.GameTheme.ACTION
                                    GameTheme.ADVENTURE -> com.example.yjcy.GameTheme.ADVENTURE
                                    GameTheme.RPG -> com.example.yjcy.GameTheme.RPG
                                    GameTheme.STRATEGY -> com.example.yjcy.GameTheme.STRATEGY
                                    GameTheme.SIMULATION -> com.example.yjcy.GameTheme.SIMULATION
                                    GameTheme.PUZZLE -> com.example.yjcy.GameTheme.PUZZLE
                                    GameTheme.RACING -> com.example.yjcy.GameTheme.RACING
                                    GameTheme.SPORTS -> com.example.yjcy.GameTheme.SPORTS
                                },
                                platforms = enhancedGame.platforms.map { platform ->
                                    when(platform) {
                                        Platform.PC -> com.example.yjcy.Platform.PC
                                        Platform.MOBILE -> com.example.yjcy.Platform.MOBILE
                                        Platform.CONSOLE -> com.example.yjcy.Platform.CONSOLE
                                        Platform.WEB -> com.example.yjcy.Platform.WEB
                                    }
                                },
                                businessModel = when(enhancedGame.businessModel) {
                                    BusinessModel.SINGLE_PLAYER -> com.example.yjcy.BusinessModel.SINGLE_PLAYER
                                    BusinessModel.ONLINE_GAME -> com.example.yjcy.BusinessModel.ONLINE_GAME
                                },
                                developmentProgress = enhancedGame.developmentProgress,
                                isCompleted = enhancedGame.isCompleted,
                                revenue = enhancedGame.revenue
                            )
                        }
                        onGamesUpdate(originalGames)
                    }
    )
}