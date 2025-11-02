# 成就系统Bug修复说明

## Bug描述

成就系统存在严重Bug：**未发售的游戏（RATED状态）也会被计入成就统计**，导致玩家在游戏未发售时就能解锁销量成就。

## Bug位置

### 位置1：单机游戏销量成就检测
**文件：** `AchievementManager.kt` - 第72-77行

**Bug代码：**
```kotlin
private fun checkSingleGameAchievement(...): Boolean {
    val maxSingleGameSales = saveData.games
        .filter { it.businessModel == BusinessModel.SINGLE_PLAYER }  // ❌ 没有过滤发售状态
        .mapNotNull { game -> revenueData[game.id]?.getTotalSales() }
        .maxOrNull() ?: 0L
    
    return maxSingleGameSales >= achievement.targetValue
}
```

**问题：**
- 只过滤了商业模式（单机游戏）
- 没有检查游戏是否真的发售（RELEASED状态）
- RATED状态的游戏如果有收益数据，也会被统计

### 位置2：网游活跃成就检测
**文件：** `AchievementManager.kt` - 第91-96行

**Bug代码：**
```kotlin
private fun checkOnlineGameAchievement(...): Boolean {
    val totalActivePlayers = saveData.games
        .filter { it.businessModel == BusinessModel.ONLINE_GAME }  // ❌ 没有过滤发售状态
        .mapNotNull { game -> revenueData[game.id]?.getActivePlayers() }
        .sum()
    
    return totalActivePlayers >= achievement.targetValue
}
```

### 位置3：成就进度计算
**文件：** `AchievementManager.kt` - 第146-156行

**Bug代码：**
```kotlin
fun getAchievementProgress(...): Float {
    val currentValue = when (achievement.category) {
        AchievementCategory.SINGLE_GAME -> {
            saveData.games
                .filter { it.businessModel == BusinessModel.SINGLE_PLAYER }  // ❌
                ...
        }
        AchievementCategory.ONLINE_GAME -> {
            saveData.games
                .filter { it.businessModel == BusinessModel.ONLINE_GAME }  // ❌
                ...
        }
    }
}
```

## 修复方案

### 修复1：单机游戏成就检测
```kotlin
// 修复后
private fun checkSingleGameAchievement(...): Boolean {
    val maxSingleGameSales = saveData.games
        .filter { 
            it.businessModel == BusinessModel.SINGLE_PLAYER && 
            it.releaseStatus == GameReleaseStatus.RELEASED  // ✓ 只统计已发售
        }
        .mapNotNull { game -> revenueData[game.id]?.getTotalSales() }
        .maxOrNull() ?: 0L
    
    return maxSingleGameSales >= achievement.targetValue
}
```

### 修复2：网游活跃成就检测
```kotlin
// 修复后
private fun checkOnlineGameAchievement(...): Boolean {
    val totalActivePlayers = saveData.games
        .filter { 
            it.businessModel == BusinessModel.ONLINE_GAME && 
            it.releaseStatus == GameReleaseStatus.RELEASED  // ✓ 只统计已发售
        }
        .mapNotNull { game -> revenueData[game.id]?.getActivePlayers() }
        .sum()
    
    return totalActivePlayers >= achievement.targetValue
}
```

### 修复3：成就进度计算
```kotlin
// 修复后
AchievementCategory.SINGLE_GAME -> {
    saveData.games
        .filter { 
            it.businessModel == BusinessModel.SINGLE_PLAYER && 
            it.releaseStatus == GameReleaseStatus.RELEASED  // ✓
        }
        ...
}
AchievementCategory.ONLINE_GAME -> {
    saveData.games
        .filter { 
            it.businessModel == BusinessModel.ONLINE_GAME && 
            it.releaseStatus == GameReleaseStatus.RELEASED  // ✓
        }
        ...
}
```

## Bug复现条件

### 场景1：RATED状态游戏有收益数据
1. 旧存档中的继承游戏可能有收益数据
2. 但游戏状态是RATED（未发售）
3. 成就系统错误地统计了这些数据
4. 玩家在游戏未发售时解锁成就 ❌

### 场景2：手动修改存档
1. 如果存档数据异常
2. 游戏有收益但状态不是RELEASED
3. 也会触发此Bug

## 修复效果

### 修复前
```
RATED状态游戏 + 有收益数据 → 被计入成就统计 ❌
↓
错误解锁成就"百万奇迹"、"全民热玩"等
```

### 修复后
```
RATED状态游戏 → 不计入成就统计 ✓
RELEASED状态游戏 → 计入成就统计 ✓
↓
只有真正发售的游戏才能解锁成就
```

## 相关成就

受影响的成就：

### 单机游戏销量类
- 🎯 百万奇迹（100万销量）
- 🔥 爆款制造机（300万销量）
- 🌍 **全民热玩（500万销量）** ← 您解锁的这个
- ⭐ 传奇制作人（1000万销量）

### 网游活跃类
- 🔧 服务器开始冒烟（10万活跃）
- 🚀 热度爆棚（30万活跃）
- 🎊 国服爆满（50万活跃）
- 🏆 虚拟世界的王者（100万活跃）

## 对用户的影响

### 如果您的成就是错误解锁的

**好消息：**
- 已解锁的成就不会被撤销
- 您可以保留这些成就

**坏消息：**
- 这些成就可能不是通过正常途径解锁的
- 如果想重新挑战，需要开新档

### 如果成就是正常解锁的

如果您确实有游戏达到500万销量：
- 成就解锁是正常的
- 不受此修复影响

## 验证方法

### 检查游戏销量
1. 进入"项目管理" → "已发售"
2. 点击游戏卡片的"收益报告"
3. 查看总销量

**如果总销量 < 500万：**
- 成就是Bug导致的错误解锁

**如果总销量 ≥ 500万：**
- 成就是正常解锁的

## 修改文件

- `app/src/main/java/com/example/yjcy/managers/AchievementManager.kt`
  - checkSingleGameAchievement：添加发售状态过滤
  - checkOnlineGameAchievement：添加发售状态过滤
  - getAchievementProgress：添加发售状态过滤

## 配合修复

此修复与以下修复配合使用：
1. ✅ RATED状态游戏不产生收益（MainActivity.kt）
2. ✅ RATED状态游戏不计入成就（AchievementManager.kt）
3. ✅ 只有RELEASED状态才是真正发售

## 版本信息
- **修复日期**: 2025-11-02
- **Bug严重级别**: 高
- **修复内容**: 成就统计只包含RELEASED状态的游戏


