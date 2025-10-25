# 游戏社区更新记录Bug修复说明

## 问题描述
用户更新游戏后，游戏社区对话框显示"暂无更新记录"，没有显示刚刚完成的更新内容和玩家评论。

## 问题原因

### 根本原因：时序问题
1. **MainActivity.kt（第1657行）**：调用 `RevenueManager.progressUpdateTask()` 推进更新任务进度
2. **GameRevenueData.kt（第711行）**：`progressUpdateTask()` 在任务完成时调用 `applyGameUpdate()`
3. **GameRevenueData.kt（第672行）**：`applyGameUpdate()` 将 `updateTask` 设置为 `null`（清除任务）
4. **MainActivity.kt（第1662行）**：尝试获取 `completedTask`，但此时 `updateTask` 已经被清除，返回 `null`
5. **MainActivity.kt（第1665行）**：由于 `completedTask == null`，不会创建更新记录，导致 `updateHistory` 没有添加新记录

### 调用链
```
MainActivity.progressUpdateTask(gameId, points)
    └─> progressUpdateTask() 完成时调用
        └─> applyGameUpdate(gameId)
            └─> updateTask = null  // ← 清除任务
MainActivity.getGameRevenue(gameId)?.updateTask  // ← 返回null
```

## 修复方案

### 修改位置
`MainActivity.kt` 第1655-1664行

### 修改内容
**修改前**：
```kotlin
// 若存在更新任务，根据已分配员工数量推进进度
val employeePoints = (releasedGame.assignedEmployees.size * 20).coerceAtLeast(10)
val updateJustCompleted = RevenueManager.progressUpdateTask(releasedGame.id, employeePoints)

// 如果更新刚刚完成，版本号+0.1
if (updateJustCompleted) {
    // 获取刚刚完成的更新任务信息（❌ 此时已经被清除，返回null）
    val completedTask = RevenueManager.getGameRevenue(releasedGame.id)?.updateTask
```

**修改后**：
```kotlin
// 在推进进度前先获取更新任务信息（因为完成后会被清除）
val completedTask = RevenueManager.getGameRevenue(releasedGame.id)?.updateTask

// 若存在更新任务，根据已分配员工数量推进进度
val employeePoints = (releasedGame.assignedEmployees.size * 20).coerceAtLeast(10)
val updateJustCompleted = RevenueManager.progressUpdateTask(releasedGame.id, employeePoints)

// 如果更新刚刚完成，版本号+0.1
if (updateJustCompleted) {
    // 使用之前保存的任务信息（✅ 在清除前已保存）
```

## 修复效果

### 修复后的工作流程
1. **更新前保存**：在调用 `progressUpdateTask` 之前，先获取并保存 `updateTask` 信息
2. **任务完成**：`progressUpdateTask` 完成任务并清除 `updateTask`
3. **创建记录**：使用之前保存的任务信息创建 `GameUpdate` 记录
4. **添加到历史**：将新记录添加到 `game.updateHistory`
5. **社区显示**：游戏社区对话框正确显示更新内容和玩家评论

### 用户体验
- ✅ 游戏更新完成后，游戏社区立即显示更新内容
- ✅ 显示更新版本号（V1.1、V1.2等）
- ✅ 显示更新日期
- ✅ 显示更新内容列表
- ✅ 显示更新公告
- ✅ 显示5-10条随机生成的玩家评论

## 相关文件
- `MainActivity.kt`：日结算逻辑，更新记录创建
- `GameRevenueData.kt`：更新任务管理，任务完成处理
- `GameCommunityDialog.kt`：社区UI，显示更新历史
- `GameData.kt`：游戏数据模型，updateHistory 字段

## 向后兼容
此修复完全向后兼容，旧存档不受影响。修复后新完成的更新会正确显示在社区中。

## 测试建议
1. 创建一个游戏并发售
2. 发起一次游戏更新
3. 分配员工完成更新（进度达到100%）
4. 打开游戏社区，应该能看到更新记录
5. 检查更新内容、公告、玩家评论是否正确显示
