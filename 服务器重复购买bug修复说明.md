# 服务器重复购买Bug修复说明

## 问题描述

用户反馈：购买1台D型服务器时，实际开通了2台服务器。

## 问题原因

在 `MainActivity.kt` 的购买服务器逻辑中，代码重复添加了服务器：

```kotlin
// 1. 添加到公共池
RevenueManager.addServerToGame(
    gameId = "SERVER_PUBLIC_POOL",
    serverType = serverType,
    purchaseYear = currentYear,
    purchaseMonth = currentMonth,
    purchaseDay = currentDay
)

// 2. 同时为所有现有网游也添加相同的服务器
val onlineGames = games.filter { it.businessModel == BusinessModel.ONLINE_GAME }
onlineGames.forEach { game ->
    RevenueManager.addServerToGame(
        gameId = game.id,
        serverType = serverType,
        purchaseYear = currentYear,
        purchaseMonth = currentMonth,
        purchaseDay = currentDay
    )
}
```

这导致如果用户有N个网游，购买1台服务器时实际添加了(N+1)台：
- 公共池：1台
- 每个网游：1台

## 解决方案

### 1. 修改购买逻辑（MainActivity.kt）

移除为每个网游重复添加服务器的代码，只添加到公共池：

```kotlin
onPurchaseServer = { serverType ->
    // 购买服务器到公共池（不立即扣费，按购买日期每30天扣费）
    val publicPoolId = "SERVER_PUBLIC_POOL"
    RevenueManager.addServerToGame(
        gameId = publicPoolId,
        serverType = serverType,
        purchaseYear = currentYear,
        purchaseMonth = currentMonth,
        purchaseDay = currentDay
    )
},
```

### 2. 修改服务器管理对话框（GameRevenueDialog.kt & ServerManagementContent.kt）

将 `ServerManagementDialog` 改为显示公共池的服务器信息，而不是游戏自己的服务器信息：

```kotlin
// 显示公共池的服务器信息（所有游戏共享）
val publicPoolId = "SERVER_PUBLIC_POOL"
val serverInfo = remember { RevenueManager.getGameServerInfo(publicPoolId) }
```

## 设计说明

修复后的设计逻辑：
- **公共池**：在"租用服务器"标签页购买服务器，统一存储在公共池中
- **游戏共享**：所有网络游戏共享公共池中的服务器
- **容量检查**：游戏上线时检查公共池是否有足够的服务器容量
- **费用扣除**：只对公共池中的服务器扣费，避免重复扣费

## 修改文件

1. `MainActivity.kt`：移除重复添加服务器的逻辑
2. `GameRevenueDialog.kt`：ServerManagementDialog显示公共池服务器
3. `ServerManagementContent.kt`：ServerManagementDialog显示公共池服务器

## 影响范围

- 修复后，购买N台服务器就只会添加N台服务器，不会重复
- 所有游戏的服务器管理对话框都显示公共池的服务器信息
- 服务器列表页面可能会显示游戏本身没有服务器，这是正常的（因为游戏使用公共池服务器）

## 测试建议

1. 购买1台D型服务器，检查是否只添加了1台
2. 创建多个网游，购买服务器，检查数量是否正确
3. 在游戏收益对话框中打开服务器管理，检查是否显示公共池服务器信息
4. 检查服务器扣费是否正常（不重复扣费）
