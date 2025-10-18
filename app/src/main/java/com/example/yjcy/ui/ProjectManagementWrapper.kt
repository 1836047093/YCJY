package com.example.yjcy.ui

import com.example.yjcy.data.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    allEmployees: List<Employee> = emptyList(),
    refreshTrigger: Int = 0,  // 新增：用于触发UI刷新
    onReleaseGame: ((Game) -> Unit)? = null,  // 新增：发售游戏回调
    onAbandonGame: ((Game) -> Unit)? = null,  // 新增：废弃游戏回调
    selectedProjectType: ProjectDisplayType = ProjectDisplayType.DEVELOPING,  // 外部传入的状态
    onProjectTypeChange: (ProjectDisplayType) -> Unit = {},  // 状态变化回调
    money: Long = 0L,  // 新增：资金
    fans: Int = 0,  // 新增：粉丝数
    onMoneyUpdate: (Long) -> Unit = {},  // 新增：资金更新回调
    onFansUpdate: (Int) -> Unit = {}  // 新增：粉丝更新回调
) {
    // 移除本地状态管理，改为由外部（MainActivity）管理
    // 这样可以确保切换标签时状态不会丢失
    
    // 直接使用员工分配功能的项目管理
    EnhancedProjectManagementContent(
                    founder = founder,
                    availableEmployees = allEmployees,
                    games = games,
                    onGamesUpdate = { updatedGames ->
                        onGamesUpdate(updatedGames)
                    },
                    refreshTrigger = refreshTrigger,
                    onSwitchToCurrentProjects = {
                        // 当从"已发售"点击更新时，自动切换到"正在更新"标签页
                        onProjectTypeChange(ProjectDisplayType.UPDATING)
                    },
                    onReleaseGame = onReleaseGame,
                    onAbandonGame = onAbandonGame,
                    selectedProjectType = selectedProjectType,
                    onProjectTypeChange = onProjectTypeChange,
                    money = money,
                    fans = fans,
                    onMoneyUpdate = onMoneyUpdate,
                    onFansUpdate = onFansUpdate
    )
}
