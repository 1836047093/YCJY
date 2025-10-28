# GM工具箱功能说明

## 功能概述

已在游戏设置界面新增**兑换码系统**和**GM工具箱**功能，玩家可以通过输入特定兑换码解锁GM模式，使用强大的开发者工具。

---

## 兑换码

### 激活方式
1. 进入游戏内的**设置**界面
2. 在"兑换码"输入框输入：`progm`（不区分大小写）
3. 点击"兑换"按钮
4. GM工具箱立即激活

### 兑换码列表
| 兑换码 | 功能 | 状态 |
|-------|------|------|
| `progm` | 激活GM工具箱 | ✅ 已实现 |

---

## GM工具箱功能

### 1. 一键满配员工 👥
**功能说明**：
- 将所有员工的技能等级提升至**5级**（最高等级）
- 包含的技能：开发、设计、美工、音乐、服务

**使用场景**：
- 快速测试高配置团队的游戏开发
- 跳过漫长的员工培养过程
- 体验满级团队的游戏制作效果

**实现逻辑**：
```kotlin
val maxedEmployees = allEmployees.map { employee ->
    employee.copy(
        skillDevelopment = 5,  // 开发
        skillDesign = 5,       // 设计
        skillArt = 5,          // 美工
        skillMusic = 5,        // 音乐
        skillService = 5       // 服务
    )
}
allEmployees.clear()
allEmployees.addAll(maxedEmployees)
```

---

### 2. 一键加1000万 💰
**功能说明**：
- 立即获得**10,000,000元**游戏资金
- 可多次使用，无限制

**使用场景**：
- 快速积累资金进行大规模投资
- 测试高成本游戏项目
- 收购子公司、租用大量服务器等

**实现逻辑**：
```kotlin
money += 10000000L
```

---

## UI界面设计

### 兑换码界面（GM模式未激活）
```
🎁 兑换码
┌─────────────────────────┐
│ [输入框]                 │
│ 请输入兑换码             │
└─────────────────────────┘
❌ 兑换码错误，请重新输入 （仅错误时显示）
[兑换] 按钮（绿色）
```

### GM工具箱界面（GM模式已激活）
```
🛠️ GM工具箱
GM模式已激活

[👥 一键满配员工] 按钮（紫色）
[💰 一键加1000万] 按钮（金色）
```

---

## 数据持久化

### SaveData数据结构
```kotlin
data class SaveData(
    // ... 其他字段 ...
    val gmModeEnabled: Boolean = false // GM模式开关
)
```

### 存档保存
- GM模式状态会**自动保存**到存档文件
- 下次加载存档时，GM工具箱状态**自动恢复**
- 一旦激活GM模式，永久生效（除非删除存档）

---

## 技术实现

### 1. 数据层
**文件**：`GameData.kt`
- 在`SaveData`中添加`gmModeEnabled`字段

### 2. 状态管理
**文件**：`MainActivity.kt`
```kotlin
// GM模式状态
var gmModeEnabled by remember { mutableStateOf(saveData?.gmModeEnabled ?: false) }
```

### 3. UI组件
**文件**：`MainActivity.kt` - `InGameSettingsContent`函数
- 新增参数：
  - `gmModeEnabled: Boolean` - GM模式状态
  - `onGMToggle: (Boolean) -> Unit` - 切换GM模式回调
  - `onMaxEmployees: () -> Unit` - 一键满配员工回调
  - `onAddMoney: () -> Unit` - 一键加钱回调

### 4. 存档系统
**文件**：`MainActivity.kt`
- 两处保存位置都添加了`gmModeEnabled`字段保存
- 存档加载时自动恢复GM模式状态

---

## 修改文件清单

| 文件 | 修改内容 |
|------|---------|
| `GameData.kt` | 添加`gmModeEnabled`字段 |
| `MainActivity.kt` | 1. 添加GM模式状态管理<br>2. 添加GM回调函数<br>3. 修改存档保存逻辑<br>4. 实现兑换码UI<br>5. 实现GM工具箱UI |

---

## 使用指南

### 新游戏激活GM
1. 开始新游戏
2. 进入设置界面
3. 输入兑换码 `progm`
4. 使用GM工具箱功能

### 旧存档激活GM
1. 加载已有存档
2. 进入设置界面
3. 输入兑换码 `progm`
4. 保存游戏（GM状态会被保存）
5. 下次加载时GM工具箱自动可用

---

## 注意事项

⚠️ **重要提示**：

1. **单向激活**：
   - GM模式一旦激活，无法关闭
   - 如需关闭，需要删除存档重新开始

2. **存档标记**：
   - GM模式状态会保存在存档中
   - 建议为GM存档和正常存档使用不同的存档位

3. **游戏平衡**：
   - GM工具箱会破坏游戏平衡性
   - 建议仅用于测试和快速体验内容

4. **兼容性**：
   - 旧版本存档默认`gmModeEnabled = false`
   - 完全向后兼容

---

## 未来扩展

可以继续添加更多GM功能：
- 🎮 一键解锁所有成就
- 📅 时间跳转（跳到指定年月日）
- 🏆 一键获得GVA大奖
- 👔 一键生成各职位顶级员工
- 🎯 修改竞争对手数据
- 💻 一键满配服务器
- ⭐ 修改公司声望等级

---

## 版本历史

**v1.0.0** (2025-01-27)
- ✅ 实现兑换码系统
- ✅ 实现GM工具箱基础功能
- ✅ 一键满配员工
- ✅ 一键加1000万
- ✅ 数据持久化

---

## 开发者备注

### 兑换码管理
如需添加新的兑换码，修改 `InGameSettingsContent` 中的验证逻辑：
```kotlin
if (redeemCode.lowercase() == "progm") {
    // progm激活GM工具箱
    onGMToggle(true)
} else if (redeemCode.lowercase() == "newcode") {
    // 新兑换码功能
} else {
    showRedeemError = true
}
```

### GM功能扩展
在 `InGameSettingsContent` 的GM工具箱区域添加新按钮，并在调用处传入对应回调函数。
