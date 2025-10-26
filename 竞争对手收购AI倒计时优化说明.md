# 竞争对手收购AI倒计时优化说明

## 修改日期
2025年1月26日

## 问题描述
用户反馈：竞价过程中AI出价速度随游戏速度变化，导致体验不一致。希望AI始终在**5秒后做出响应**，且右上角显示倒计时。

## 修改内容

### 1. AI出价延迟固定为5秒
**修改位置**：`CompetitorScreen.kt` - `AcquisitionDialog` 函数

**旧逻辑**：
```kotlin
val aiRoundDelay = when (gameSpeed) {
    1 -> 2000L    // 1x速度: 2秒
    2 -> 1000L    // 2x速度: 1秒
    3 -> 400L     // 3x速度: 0.4秒
    else -> 2000L
}
```

**新逻辑**：
```kotlin
// AI竞价延迟时间固定为5秒（不受游戏速度影响）
val aiRoundDelay = 5000L
```

### 2. 添加倒计时显示

#### 2.1 新增状态变量
```kotlin
// 倒计时状态（秒）
var countdown by remember { mutableStateOf(0) }
```

#### 2.2 AI出价过程中更新倒计时
```kotlin
// 继续下一轮（固定5秒延迟，并显示倒计时）
countdown = 5
for (i in 5 downTo 1) {
    countdown = i
    kotlinx.coroutines.delay(1000L)
}
countdown = 0
processAIRound()
```

#### 2.3 玩家加价后AI响应倒计时
```kotlin
// 玩家加价后触发AI竞价（固定5秒延迟，并显示倒计时）
if (triggerAIBidding > 0 && biddingPhase == "bidding") {
    countdown = 5
    for (i in 5 downTo 1) {
        countdown = i
        kotlinx.coroutines.delay(1000L)
    }
    countdown = 0
    processAIRound()
}
```

#### 2.4 UI显示（右上角倒计时）
```kotlin
// 倒计时显示（竞价进行中且有倒计时时）
if (biddingPhase == "bidding" && countdown > 0) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = Color(0xFFFF6B6B).copy(alpha = 0.3f),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = Color(0xFFFF6B6B),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = countdown.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}
```

## 功能特点

### ✅ 固定AI响应时间
- 不管游戏速度是1x、2x还是3x，AI都会在**5秒后**做出响应
- 给玩家充足时间思考下一步策略

### ✅ 实时倒计时显示
- 右上角显示红色圆圈倒计时（5→4→3→2→1）
- 玩家可清晰知道AI还有多久出价
- 倒计时结束后自动隐藏

### ✅ UI视觉设计
- 48x48dp圆形倒计时器
- 红色背景（0xFFFF6B6B，30%透明度）
- 2dp红色边框
- 白色粗体数字，20sp字号
- 位于对话框标题栏右侧

## 使用场景

### 场景1：初始AI出价
1. 玩家发起收购出价
2. 等待2秒（初始延迟）
3. AI第一次出价
4. 显示倒计时5秒→AI第二次出价（如果还要继续）

### 场景2：玩家加价后
1. 玩家点击"加价至XXX"
2. 立即显示倒计时5秒
3. 每秒递减：5→4→3→2→1
4. 倒计时结束，AI做出响应（出价或退出）

### 场景3：竞价结束
- 倒计时消失
- 显示收购结果（成功/失败）

## 技术细节

### 倒计时实现
使用 Kotlin 协程 + `LaunchedEffect` 实现倒计时：
- 外层LaunchedEffect监听 `triggerAIBidding` 变化
- 内层for循环每秒更新 `countdown` 状态
- UI自动响应状态变化实时显示

### 线程安全
- 所有倒计时操作在同一个LaunchedEffect协程中执行
- 不会出现多个倒计时并发的情况
- `countdown` 状态更新触发UI重组

## 向后兼容
✅ 完全兼容，不影响现有收购功能
✅ 只是调整了时间参数和添加了UI显示
✅ 旧存档正常运行

## 修改文件
- `CompetitorScreen.kt`：`AcquisitionDialog` 函数（约150行修改）

## 效果对比

### 修改前
| 游戏速度 | AI响应时间 | 倒计时显示 |
|---------|----------|----------|
| 1x      | 2秒      | ❌ 无     |
| 2x      | 1秒      | ❌ 无     |
| 3x      | 0.4秒    | ❌ 无     |

### 修改后
| 游戏速度 | AI响应时间 | 倒计时显示 |
|---------|----------|----------|
| 1x      | **5秒**  | ✅ 5→1   |
| 2x      | **5秒**  | ✅ 5→1   |
| 3x      | **5秒**  | ✅ 5→1   |

## 用户体验提升
✅ **策略深度**：玩家有充足时间评估是否继续加价
✅ **紧张感**：倒计时营造紧迫氛围
✅ **可预测性**：明确知道AI何时出价
✅ **公平性**：所有游戏速度下体验一致
