# 游戏项目员工分配功能使用说明

## 功能概述

本功能为游戏开发项目管理系统添加了智能员工分配功能，允许用户为每个游戏项目分配合适的员工，提高开发效率。

## 新增文件

### 1. GameProjectAssignment.kt
- **位置**: `app/src/main/java/com/example/yjcy/ui/GameProjectAssignment.kt`
- **功能**: 包含增强版的游戏项目卡片和员工分配对话框
- **主要组件**:
  - `EnhancedGameProjectCard`: 带员工分配功能的项目卡片
  - `EmployeeAssignmentDialog`: 员工分配对话框
  - `EmployeeSelectionCard`: 员工选择卡片
  - `performSkillBasedAssignment`: 智能分配算法

### 2. EnhancedProjectManagement.kt
- **位置**: `app/src/main/java/com/example/yjcy/ui/EnhancedProjectManagement.kt`
- **功能**: 完整的增强版项目管理界面
- **主要组件**:
  - `EnhancedProjectManagementContent`: 主要管理界面
  - `EnhancedGameDevelopmentDialog`: 增强版游戏创建对话框
  - 员工数据管理和状态跟踪

### 3. ProjectManagementWrapper.kt
- **位置**: `app/src/main/java/com/example/yjcy/ui/ProjectManagementWrapper.kt`
- **功能**: 包装器组件，提供模式切换功能
- **特点**: 可以在原始模式和增强模式之间切换

## 使用方法

### 方法一：直接替换（推荐）

在 `MainActivity.kt` 的 `GameScreen` 函数中，找到以下代码：

```kotlin
2 -> ProjectManagementContent(
    games = games,
    onGamesUpdate = { updatedGames -> games = updatedGames }
)
```

替换为：

```kotlin
2 -> EnhancedProjectManagementContent(
    games = games.map { originalGame ->
        com.example.yjcy.ui.Game(
            id = originalGame.id,
            name = originalGame.name,
            theme = originalGame.theme,
            platforms = originalGame.platforms,
            businessModel = originalGame.businessModel,
            developmentProgress = originalGame.developmentProgress,
            isCompleted = originalGame.isCompleted,
            revenue = originalGame.revenue,
            assignedEmployees = emptyList()
        )
    },
    onGamesUpdate = { enhancedGames ->
        val originalGames = enhancedGames.map { enhancedGame ->
            com.example.yjcy.Game(
                id = enhancedGame.id,
                name = enhancedGame.name,
                theme = enhancedGame.theme,
                platforms = enhancedGame.platforms,
                businessModel = enhancedGame.businessModel,
                developmentProgress = enhancedGame.developmentProgress,
                isCompleted = enhancedGame.isCompleted,
                revenue = enhancedGame.revenue
            )
        }
        games = originalGames
    }
)
```

### 方法二：使用包装器（保持兼容性）

在 `MainActivity.kt` 的 `GameScreen` 函数中，替换为：

```kotlin
2 -> ProjectManagementWrapper(
    games = games,
    onGamesUpdate = { updatedGames -> games = updatedGames }
)
```

这种方法提供了一个切换开关，用户可以在原始模式和增强模式之间切换。

## 功能特性

### 1. 智能员工分配
- **技能匹配**: 根据员工技能和项目需求进行智能匹配
- **分配策略**: 支持多种分配策略（技能优先、成本优先、平衡分配）
- **实时预览**: 分配前可预览分配结果和预期效果

### 2. 员工管理
- **可用性检查**: 自动检查员工是否已被分配到其他项目
- **技能展示**: 清晰显示每个员工的技能等级
- **成本计算**: 显示分配成本和预期收益

### 3. 项目增强
- **分配状态**: 项目卡片显示已分配员工信息
- **进度影响**: 员工分配影响项目开发进度
- **团队协作**: 支持多人团队协作开发

### 4. 用户体验
- **一键分配**: 快速智能分配最佳员工组合
- **手动选择**: 支持手动选择特定员工
- **状态反馈**: 实时显示分配状态和结果
- **撤销功能**: 支持撤销员工分配

## 数据结构

### 增强版 Game 数据类
```kotlin
data class Game(
    val id: String,
    val name: String,
    val theme: GameTheme,
    val platforms: List<Platform>,
    val businessModel: BusinessModel,
    val developmentProgress: Float,
    val isCompleted: Boolean,
    val revenue: Long,
    val assignedEmployees: List<Employee> = emptyList() // 新增字段
)
```

### Employee 数据类
```kotlin
data class Employee(
    val id: Int,
    val name: String,
    val position: String,
    val programmingSkill: Int,
    val designSkill: Int,
    val artSkill: Int,
    val soundSkill: Int,
    val marketingSkill: Int,
    val salary: Int,
    val isAssigned: Boolean = false
)
```

## 分配算法

### 技能匹配算法
```kotlin
fun performSkillBasedAssignment(
    availableEmployees: List<Employee>,
    maxEmployees: Int = 3
): List<Employee>
```

算法特点：
- 计算每个员工的综合技能分数
- 优先选择技能互补的员工组合
- 考虑成本效益比
- 避免技能重复浪费

## 注意事项

1. **兼容性**: 新功能完全兼容现有代码，不会影响原有功能
2. **性能**: 员工分配算法经过优化，支持大量员工数据
3. **扩展性**: 代码结构支持未来功能扩展
4. **数据持久化**: 分配信息需要集成到现有的存档系统中

## 未来扩展

- 员工技能成长系统
- 项目协作效率计算
- 员工满意度系统
- 高级分配策略
- 项目风险评估

## 技术支持

如有问题或建议，请查看代码注释或联系开发团队。