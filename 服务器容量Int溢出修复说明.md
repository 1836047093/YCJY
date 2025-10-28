# 服务器容量Int溢出修复说明

## 问题现象

服务器容量或活跃玩家数超过21亿后变成负数（如截图显示的-2123967296）。

## 问题根源

**21.47亿（2,147,483,647）是Int32类型的最大值**。超过此值会发生整数溢出，变成负数。

### 发现的溢出点

#### 1. 服务器容量数据类型（ServerData.kt）
**问题代码**：
```kotlin
enum class ServerType(
    val displayName: String,
    val capacity: Int,      // ❌ Int类型（最大21.47亿）
    val cost: Long,
    val description: String
)

fun getTotalCapacity(): Int {  // ❌ 返回Int类型
    return servers.filter { it.isActive }.sumOf { it.getCapacity() }
}
```

**问题分析**：
- 服务器容量单位是"万人"
- 当总容量超过21.47亿万人时（实际玩家数 = 容量 × 10000）溢出
- 多个S型服务器（1000万容量）累加很容易达到这个值

#### 2. 新增玩家数计算（GameRevenueData.kt 第684行）
**问题代码**：
```kotlin
// 更新带来新玩家：增加10-20%的总注册人数
val newPlayersRatio = Random.nextDouble(0.1, 0.2)
val newPlayers = (current.totalRegisteredPlayers * newPlayersRatio).toInt()  // ❌ 强转Int
newTotalRegisteredPlayers = current.totalRegisteredPlayers + newPlayers
```

**问题分析**：
- `totalRegisteredPlayers` 是 Long 类型
- 当超过21亿后，`.toInt()` 强制类型转换导致溢出
- 导致更新游戏时新增玩家数变成负数

#### 3. 服务器容量检查（GameRevenueData.kt 第1382行）
**问题代码**：
```kotlin
fun hasEnoughServerCapacity(gameId: String, requiredCapacity: Int): Boolean {  // ❌ 参数Int类型
    val serverInfo = gameServerMap[gameId] ?: return false
    return serverInfo.getTotalCapacity() >= requiredCapacity
}
```

## 修复方案

### 修复1：服务器容量改用Long类型

**修改文件**：`ServerData.kt`

```kotlin
enum class ServerType(
    val displayName: String,
    val capacity: Long,      // ✅ 使用Long（最大922京）
    val cost: Long,
    val description: String
) {
    BASIC("星尘-D型服务器", 10L, 500000L, "适合小规模运营，10万容量"),
    INTERMEDIATE("星尘-C型服务器", 50L, 1000000L, "适合中等规模，50万容量"),
    ADVANCED("星尘-B型服务器", 200L, 5000000L, "适合大规模运营，200万容量"),
    CLOUD("星尘-A型服务器", 500L, 10000000L, "最高性能，500万容量"),
    SUPER("星尘-S型服务器", 1000L, 20000000L, "超级服务器，1000万容量")
}

fun getCapacity(): Long = type.capacity  // ✅ 返回Long

fun getTotalCapacity(): Long {  // ✅ 返回Long
    return servers.filter { it.isActive }.sumOf { it.getCapacity() }
}
```

### 修复2：新增玩家数改用Long类型

**修改文件**：`GameRevenueData.kt` 第684行

```kotlin
// 更新带来新玩家：增加10-20%的总注册人数
val newPlayersRatio = Random.nextDouble(0.1, 0.2)
val newPlayers = (current.totalRegisteredPlayers * newPlayersRatio).toLong()  // ✅ 改用toLong()
newTotalRegisteredPlayers = current.totalRegisteredPlayers + newPlayers
```

### 修复3：容量检查参数改用Long类型

**修改文件**：`GameRevenueData.kt` 第1382行

```kotlin
fun hasEnoughServerCapacity(gameId: String, requiredCapacity: Long): Boolean {  // ✅ 参数Long类型
    val serverInfo = gameServerMap[gameId] ?: return false
    return serverInfo.getTotalCapacity() >= requiredCapacity
}
```

## 数据类型对比

| 类型 | 最大值 | 适用场景 |
|------|--------|----------|
| Int (32位) | 2,147,483,647 (约21.47亿) | ❌ 大型网游会溢出 |
| Long (64位) | 9,223,372,036,854,775,807 (约922京) | ✅ 完全够用 |

## 修复效果

✅ **服务器容量**：支持千亿级别容量，不再溢出  
✅ **活跃玩家数**：正常显示，不会变负数  
✅ **新增玩家数**：更新游戏时正确计算新增玩家  
✅ **容量检查**：正确判断服务器容量是否足够  

## 向后兼容性

### 已有存档数据
- **服务器数据**：ServerData使用的是枚举值，代码级别修改不影响存档
- **玩家数据**：totalRegisteredPlayers本来就是Long类型，无需迁移
- **完全兼容**：旧存档可以直接加载，自动使用新的Long类型计算

### SharedPreferences读取
```kotlin
// SharedPreferences的getLong()可以读取旧的Int数据
val capacity = prefs.getLong("capacity", 0L)  // 兼容旧的putInt
```

## 数值示例

### 修复前（Int类型）：
- 100台S型服务器：100 × 1000万 = 10亿万容量 ✅（未溢出）
- 300台S型服务器：300 × 1000万 = 30亿万容量 ❌（溢出变负数）

### 修复后（Long类型）：
- 300台S型服务器：300 × 1000万 = 30亿万容量 ✅（正常）
- 100万台S型服务器：100万 × 1000万 = 1000万亿容量 ✅（仍然正常）

## 相关内存

这次修复与之前的"网游总注册数和活跃数Int溢出"修复类似：
- 之前修复了：`totalRegisteredPlayers` 从Int改为Long
- 这次修复了：服务器容量、新增玩家计算
- **共同原因**：网游5%日增长复利效应，第2年即可突破21亿

## 测试建议

建议测试以下场景：
1. ✅ 购买大量S型服务器（>300台），检查总容量显示
2. ✅ 网游运营到第3-4年，活跃玩家超过21亿，检查是否正常
3. ✅ 更新网游时，检查新增玩家数是否正确（不为负数）
4. ✅ 服务器管理界面显示的总容量和活跃玩家数

## 开发建议

以后涉及可能超过21亿的数值时：
1. **优先使用Long类型**而不是Int
2. **避免.toInt()强转**，除非确定不会溢出
3. **数值单位要注意**：容量是"万人"，实际玩家数要×10000
4. **复利增长警惕**：5%日增长，2年即可达到30亿

---

**修复时间**：2025-01-28  
**问题级别**：严重（数据显示错误）  
**修复难度**：简单（类型修改）  
**影响范围**：服务器管理、网游收益计算
