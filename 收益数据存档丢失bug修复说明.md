# 收益数据存档丢失Bug修复说明

## 问题描述

**严重Bug**：切换存档时会导致游戏收益数据完全丢失。

### 问题场景
1. 玩家在存档1发售游戏A，运营10天，获得收益10万元
2. 保存存档1
3. 切换到存档2游玩
4. 再次读取存档1 → **游戏A的所有收益数据丢失！**

### 受影响的数据

**单机游戏**：
- ❌ dailySalesList（每日销量记录）
- ❌ 总收益、总销量
- ❌ 更新任务进度
- ❌ 更新次数

**网络游戏**：
- ✓ 服务器数据（已保存，不受影响）
- ❌ dailySalesList（每日注册人数记录）
- ❌ monetizationRevenues（付费内容累计收益）
- ❌ playerInterest（玩家兴趣值）
- ❌ totalRegisteredPlayers（总注册人数）
- ❌ 更新任务进度

## 问题根源

### 1. SaveData缺少收益数据字段
存档只保存了服务器数据，没有保存收益数据：
```kotlin
// 修复前
data class SaveData(
    ...
    val serverData: Map<String, GameServerInfo> = emptyMap(), // ✓ 只有服务器
    // ✗ 缺少收益数据
)
```

### 2. RevenueManager使用全局SharedPreferences
- `RevenueManager.gameRevenueMap` 是全局单例
- 切换存档时调用 `clearAllData()` 清空所有数据
- 读取存档时无法恢复之前的收益数据

### 3. 读档逻辑的补偿措施失效
当读档时发现游戏没有收益数据，会用 `daysOnMarket=0` 重新生成空数据，导致历史数据丢失。

## 修复方案

### 1. SaveData添加收益数据字段（GameData.kt）
```kotlin
data class SaveData(
    ...
    val serverData: Map<String, GameServerInfo> = emptyMap(),
    val revenueData: Map<String, GameRevenue> = emptyMap(), // ✓ 新增
)
```

### 2. RevenueManager添加导出/导入函数（GameRevenueData.kt）
```kotlin
/**
 * 导出所有收益数据（用于存档）
 */
fun exportRevenueData(): Map<String, GameRevenue> {
    return gameRevenueMap.toMap()
}

/**
 * 导入收益数据（用于读档）
 */
fun importRevenueData(revenueData: Map<String, GameRevenue>) {
    gameRevenueMap.clear()
    gameRevenueMap.putAll(revenueData)
    saveRevenueData() // 同步到SharedPreferences
}
```

### 3. 存档时导出收益数据（MainActivity.kt，3处修改）
```kotlin
val saveData = SaveData(
    ...
    serverData = RevenueManager.exportServerData(),
    revenueData = RevenueManager.exportRevenueData() // ✓ 导出收益数据
)
```

### 4. 读档时导入收益数据（MainActivity.kt）
```kotlin
// 恢复服务器数据
if (saveData.serverData.isNotEmpty()) {
    RevenueManager.importServerData(saveData.serverData)
}

// 恢复收益数据
if (saveData.revenueData.isNotEmpty()) {
    RevenueManager.importRevenueData(saveData.revenueData) // ✓ 导入收益数据
} else {
    // 旧存档兼容：为已发售游戏初始化空数据
}
```

## 修改文件列表

1. **GameData.kt**：SaveData添加revenueData字段
2. **GameRevenueData.kt**：RevenueManager添加exportRevenueData()和importRevenueData()函数
3. **MainActivity.kt**：
   - 3处存档点添加revenueData导出
   - 读档逻辑添加revenueData导入

## 向后兼容性

✓ **旧存档完全兼容**：
- 旧存档的revenueData字段默认为空Map
- 读档时检测到空Map会为已发售游戏初始化空数据
- 不会影响正常游玩，只是旧存档的历史收益数据无法恢复

## 验证方法

### 测试场景1：切换存档
1. 存档1：发售单机游戏A，运营5天，总收益5万
2. 保存存档1
3. 开始新游戏，发售游戏B
4. 读取存档1 → **验证游戏A的收益数据完整（5天记录，5万收益）**

### 测试场景2：网游数据保存
1. 存档2：发售网游C，运营10天，总注册1万，兴趣值80%
2. 保存存档2
3. 开始新游戏
4. 读取存档2 → **验证网游C的数据完整（注册数、兴趣值、付费收益）**

### 测试场景3：多存档切换
1. 存档1保存游戏A数据
2. 存档2保存游戏B数据
3. 存档1 → 存档2 → 存档1 → 存档2
4. **验证两个存档的收益数据互不影响**

## 修复后效果

✅ **所有收益数据正常保存和恢复**：
- 单机游戏：每日销量、总收益、更新记录
- 网游：注册数、兴趣值、付费内容收益、服务器数据
- 多存档独立保存，互不影响
- 切换存档不再丢失数据

## 技术细节

### 数据流程
```
存档时：
gameRevenueMap → exportRevenueData() → SaveData.revenueData → JSON → SharedPreferences

读档时：
SharedPreferences → JSON → SaveData.revenueData → importRevenueData() → gameRevenueMap
```

### 日志监控
修复后添加了详细日志，方便排查问题：
```
[RevenueManager] ===== 开始导出收益数据 =====
[RevenueManager] 导出收益数据: 3 个游戏
[RevenueManager]   - 游戏 xxx (游戏名)
[RevenueManager]     * 总收益: 100000
[RevenueManager]     * 销售天数: 10
```

## 注意事项

1. **旧存档会丢失历史数据**：修复前保存的存档无法恢复历史收益数据
2. **建议玩家重新存档**：修复后第一次保存时会将当前数据完整保存
3. **存档文件变大**：收益数据包含详细的每日记录，存档体积会增加

---

**修复完成时间**：2025-01-20
**影响版本**：≤ 1.8.1
**修复版本**：1.8.2+
