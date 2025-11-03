# RATED状态游戏错误产生收益Bug修复说明

## Bug描述

发现严重Bug：`RATED`状态的游戏会产生收益！

**RATED状态含义：**
- 游戏开发完成，已获得评分
- **但尚未发售**，不应该产生任何收益
- 需要玩家手动点击"发售"按钮后才开始销售

**错误行为：**
- RATED状态的游戏被包含在每日收益计算中
- 导致未发售的游戏也在赚钱
- 玩家可能在不知情的情况下解锁销量成就

## Bug位置

### 位置1：每日收益计算（主要Bug）
**文件：** `MainActivity.kt` - 第2340-2344行

**Bug代码：**
```kotlin
// 每天更新已发售游戏的收益
val releasedGames = games.filter { 
    it.releaseStatus == GameReleaseStatus.RELEASED || 
    it.releaseStatus == GameReleaseStatus.RATED  // ❌ Bug！RATED不应该产生收益
}
```

### 位置2：游戏更新任务进度
**文件：** `MainActivity.kt` - 第3155行

**Bug代码：**
```kotlin
games.filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
    .forEach { releasedGame ->
        // 推进更新任务进度
    }
```

### 位置3：读档时初始化收益数据
**文件：** `MainActivity.kt` - 第2078-2080行

**Bug代码：**
```kotlin
saveData.games
    .filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
    .forEach { releasedGame ->
        // 初始化收益数据
    }
```

### 位置4：调整低评分游戏历史销量
**文件：** `MainActivity.kt` - 第2145-2146行

**Bug代码：**
```kotlin
saveData.games
    .filter { it.releaseStatus == GameReleaseStatus.RELEASED || it.releaseStatus == GameReleaseStatus.RATED }
    .forEach { game ->
        // 调整销量
    }
```

## 游戏状态说明

| 状态 | 含义 | 应该产生收益？ | 正确性 |
|------|------|---------------|--------|
| DEVELOPMENT | 开发中 | ❌ | ✓ |
| READY_FOR_RELEASE | 开发完成待发售 | ❌ | ✓ |
| **RATED** | **已评分但未发售** | **❌** | **✗ Bug** |
| RELEASED | 已发售 | ✅ | ✓ |
| REMOVED_FROM_MARKET | 已下架 | ❌ | ✓ |

## 修复方案

### 修复1：每日收益计算
```kotlin
// 修复前
val releasedGames = games.filter { 
    it.releaseStatus == GameReleaseStatus.RELEASED || 
    it.releaseStatus == GameReleaseStatus.RATED  // ❌
}

// 修复后
val releasedGames = games.filter { 
    it.releaseStatus == GameReleaseStatus.RELEASED  // ✓ 只有RELEASED
}
```

### 修复2：更新任务进度
```kotlin
// 修复后
games.filter { it.releaseStatus == GameReleaseStatus.RELEASED }
    .forEach { releasedGame -> ... }
```

### 修复3：收益数据初始化
```kotlin
// 修复后
saveData.games
    .filter { it.releaseStatus == GameReleaseStatus.RELEASED }
    .forEach { releasedGame -> ... }
```

### 修复4：销量调整
```kotlin
// 修复后
saveData.games
    .filter { it.releaseStatus == GameReleaseStatus.RELEASED }
    .forEach { game -> ... }
```

## Bug影响范围

### 受影响的场景

1. **继承游戏（旧存档）**
   - 旧版本继承的游戏状态可能是RATED
   - 这些游戏会错误地产生收益
   - 用户可能在不知情中获得大量资金

2. **自己开发的游戏**
   - 如果玩家开发完游戏但没点"发售"
   - 游戏状态是RATED
   - 也会错误地产生收益

### 错误的后果

- ❌ 资金异常增长
- ❌ 销量成就被错误解锁
- ❌ 游戏平衡性被破坏
- ❌ 玩家体验混乱

## 修复效果

### 修复前
```
RATED状态游戏 → 产生收益 ❌
RELEASED状态游戏 → 产生收益 ✓
```

### 修复后
```
RATED状态游戏 → 不产生收益 ✓
RELEASED状态游戏 → 产生收益 ✓
```

## 用户需要做什么

### 场景1：如果有RATED状态的游戏

1. 进入"项目管理"
2. 查看"已发售"标签
3. 如果有游戏显示"发售"按钮而不是"收益报告"
4. 这些游戏是RATED状态，修复后将不再产生收益
5. 点击"发售"按钮后才会开始赚钱

### 场景2：如果使用旧存档

旧存档中的继承游戏可能已经是RELEASED状态：
- 这些游戏会继续产生收益（正常）
- 不受此修复影响

### 场景3：想验证修复

1. 重新编译运行游戏
2. 等待到月初（X月1日）
3. 查看Logcat日志：`💰 发售中的游戏`
4. 确认只有真正发售的游戏在产生收益

## 正确的游戏流程

### 自己开发的游戏
```
开发中(DEVELOPMENT)
    ↓ 开发完成
准备发售(READY_FOR_RELEASE)
    ↓ 点击"发售"
已发售(RELEASED) → 开始产生收益 ✓
```

### 继承的游戏（新版本）
```
收购竞争对手
    ↓
继承游戏(RATED状态)
    ↓ 已评分但未发售
    ↓ 不产生收益 ✓
点击"发售"按钮
    ↓
已发售(RELEASED) → 开始产生收益 ✓
```

## 测试建议

1. **测试RATED游戏不产生收益**
   - 开发一个游戏到完成（READY_FOR_RELEASE）
   - 不点击发售按钮
   - 观察资金是否增长（应该不增长）

2. **测试RELEASED游戏产生收益**
   - 点击"发售"按钮
   - 游戏状态变为RELEASED
   - 观察资金增长（应该增长）

3. **测试继承游戏**
   - 收购竞争对手
   - 继承的游戏应该是RATED状态
   - 点击"发售"后才开始赚钱

## 修改文件

- `app/src/main/java/com/example/yjcy/MainActivity.kt`
  - 第2341行：每日收益计算
  - 第3155行：更新任务进度
  - 第2080行：收益数据初始化
  - 第2146行：销量调整

## 版本信息
- **修复日期**: 2025-11-02
- **Bug严重级别**: 高（影响游戏平衡）
- **修复内容**: 移除RATED状态游戏的收益计算



