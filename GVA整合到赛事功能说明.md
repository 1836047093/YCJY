# GVA整合到赛事功能说明

**修改日期**: 2024-01-26

## 功能说明

将GVA大会功能整合到电竞赛事中心，作为赛事页面的第4个标签页，简化底部导航栏。

### 用户体验

用户点击底部导航栏的"🏆 赛事"后，可以看到4个标签页：
1. **🏆 可举办** - 查看符合条件的游戏并举办赛事
2. **⏳ 进行中** - 查看正在进行的赛事
3. **📊 历史记录** - 查看过往赛事记录
4. **🏅 GVA大会** - 年度游戏行业盛会（新增）

## 修改内容

### 1. TournamentScreen.kt

**修改位置**: 第27-38行，第104-128行

**主要变更**:
- 为 `TournamentScreen` 添加 `saveData: SaveData` 参数
- 标签页列表从3个增加到4个：`listOf("🏆 可举办", "⏳ 进行中", "📊 历史记录", "🏅 GVA大会")`
- 在 `when (selectedTab)` 中添加第4个分支（索引3）显示 `GVAScreen`

**代码示例**:
```kotlin
@Composable
fun TournamentScreen(
    games: List<Game>,
    revenueDataMap: Map<String, GameRevenue>,
    currentDate: GameDate,
    money: Long,
    fans: Int,
    saveData: SaveData,  // 新增参数
    onHostTournament: (String, TournamentType) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🏆 可举办", "⏳ 进行中", "📊 历史记录", "🏅 GVA大会")  // 新增第4个标签
    
    // ... 内容区域
    when (selectedTab) {
        0 -> EligibleGamesTab(...)
        1 -> OngoingTournamentsTab(...)
        2 -> TournamentHistoryTab(...)
        3 -> GVAScreen(  // 新增
            saveData = saveData,
            onBack = { /* 已在赛事页面，无需返回 */ }
        )
    }
}
```

### 2. MainActivity.kt

#### 2.1 TournamentScreen调用（第2327-2347行）

为 `TournamentScreen` 传递完整的 `saveData` 参数：

```kotlin
4 -> TournamentScreen(
    games = games,
    revenueDataMap = RevenueManager.exportRevenueData(),
    currentDate = GameDate(currentYear, currentMonth, currentDay),
    money = money,
    fans = fans,
    saveData = SaveData(  // 新增
        money = money,
        fans = fans,
        allEmployees = allEmployees.toList(),
        games = games,
        currentYear = currentYear,
        currentMonth = currentMonth,
        currentDay = currentDay,
        competitors = competitors,
        competitorNews = competitorNews,
        companyReputation = companyReputation,
        gvaHistory = gvaHistory,
        currentYearNominations = currentYearNominations,
        gvaAnnouncedDate = gvaAnnouncedDate
    ),
    onHostTournament = { gameId, type -> ... }
)
```

#### 2.2 移除独立的GVA页面（第2378-2393行）

删除了 `6 -> GVAScreen(...)` 分支，因为GVA已整合到赛事页面中。

#### 2.3 底部导航栏（第3707-3712行）

移除了独立的GVA导航项：

```kotlin
// ❌ 删除
EnhancedBottomNavItem(
    icon = "🏅",
    label = "GVA",
    isSelected = selectedTab == 6,
    onClick = { onTabSelected(6) }
)
```

#### 2.4 教程触发器（第2934-2939行）

移除了GVA的独立教程触发器：

```kotlin
// ❌ 删除
TutorialTrigger(
    tutorialId = TutorialId.GVA_CONFERENCE_INTRO,
    tutorialState = tutorialState,
    enabled = selectedTab == 6
)
```

**保留**: 赛事教程触发器 `enabled = selectedTab == 4` 继续生效，涵盖整个赛事中心（包括GVA）。

### 3. TutorialContentPart3.kt（第112-160行）

更新赛事教程内容，整合GVA说明：

```kotlin
TutorialId.TOURNAMENT_INTRO to TutorialStep(
    id = TutorialId.TOURNAMENT_INTRO,
    title = "🏆 电竞赛事中心",
    content = """...
    
📊 主要功能：
• 🏆 可举办：查看符合条件的游戏
• ⏳ 进行中：查看正在进行的赛事
• 📊 历史记录：查看过往赛事
• 🏅 GVA大会：年度游戏行业盛会  // 新增

...

🏅 GVA大会：  // 新增板块
• 每年12月举办年度评选
• 多个奖项类别供争夺
• 获奖可大幅提升品牌价值
• 查看本年度提名和历史记录

💡 提示：定期举办赛事+争取GVA奖项=顶尖游戏公司！"""
)
```

## 底部导航栏调整

### 修改前（7个标签页）
- 公司概览 (0)
- 员工管理 (1)
- 项目管理 (2)
- 竞争对手 (3)
- 赛事 (4)
- 服务器 (5)
- **GVA (6)** ❌ 独立标签页

### 修改后（6个标签页）
- 公司概览 (0)
- 员工管理 (1)
- 项目管理 (2)
- 竞争对手 (3)
- **赛事 (4)** ✅ 包含GVA子标签页
- 服务器 (5)

## 导航路径

### 访问赛事功能
1. 点击底部导航栏"🏆 赛事"
2. 选择标签页：可举办/进行中/历史记录

### 访问GVA大会
1. 点击底部导航栏"🏆 赛事"
2. 点击顶部标签"🏅 GVA大会"
3. 查看本年度提名/历史记录/声望

## 优势

### 1. **简化导航**
- 底部导航栏从7个减少到6个标签
- 更符合移动端设计规范
- 减少标签拥挤问题

### 2. **逻辑关联**
- 赛事和GVA都是竞技相关活动
- GVA是年度赛事的升级版
- 整合后更符合用户心智模型

### 3. **空间优化**
- 为未来新功能预留底部导航空间
- 避免底部导航栏过度拥挤
- 提升整体UI美观度

### 4. **功能层级**
- 主功能：赛事（底部导航栏）
- 子功能：可举办、进行中、历史记录、GVA（顶部标签页）
- 层级清晰，符合信息架构原则

## 修改文件列表

1. **app/src/main/java/com/example/yjcy/ui/TournamentScreen.kt**
   - 添加 `saveData` 参数
   - 添加"🏅 GVA大会"标签页
   - 在内容区域添加 `GVAScreen` 显示

2. **app/src/main/java/com/example/yjcy/MainActivity.kt**
   - 为 `TournamentScreen` 传递 `saveData`
   - 移除独立的 `GVAScreen` 页面（索引6）
   - 移除底部导航栏的GVA选项
   - 移除GVA教程触发器

3. **app/src/main/java/com/example/yjcy/data/TutorialContentPart3.kt**
   - 更新赛事教程内容，整合GVA说明

## 向后兼容性

✅ **完全兼容**
- 不影响存档数据结构
- GVA功能逻辑完全保留
- 仅改变UI导航方式
- 无需更新存档版本号

## 测试验证

### 测试步骤
1. 启动游戏
2. 点击底部导航栏"🏆 赛事"
3. 查看4个标签页是否正常显示
4. 点击"🏅 GVA大会"标签页
5. 验证GVA功能正常工作（提名、历史、声望）
6. 确认其他3个赛事标签页功能正常

### 预期结果
- ✅ 底部导航栏只显示6个标签
- ✅ 赛事页面显示4个子标签页
- ✅ GVA功能完全正常
- ✅ 赛事功能完全正常
- ✅ 导航逻辑清晰流畅

## 总结

成功将GVA大会整合到电竞赛事中心，优化了导航结构，简化了底部导航栏，提升了用户体验。GVA作为年度游戏评选活动，与日常电竞赛事形成完美互补，整合后更符合用户的使用习惯和心智模型。
