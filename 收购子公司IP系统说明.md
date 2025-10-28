# 收购子公司IP系统说明

## 概述

收购竞争对手公司后，现在**只继承IP而不继承游戏**。玩家可以在开发新游戏时使用这些IP，获得销量加成。

---

## 核心设计

### 1. IP数据结构 (GameIP)

**位置**: `GameData.kt`

```kotlin
data class GameIP(
    val id: String,                          // IP唯一ID
    val name: String,                        // IP名称（原游戏名）
    val originalCompany: String,             // 原公司名称
    val theme: GameTheme,                    // 游戏主题
    val originalRating: Float,               // 原游戏评分（影响IP知名度）
    val acquiredYear: Int,                   // 收购年份
    val acquiredMonth: Int,                  // 收购月份
    val platforms: List<Platform>,           // 原游戏平台（参考信息）
    val businessModel: BusinessModel         // 原游戏类型（参考信息）
)
```

### 2. IP知名度加成系统

IP的销量加成**基于原游戏评分**：

| 原游戏评分 | IP等级 | 销量加成 |
|-----------|-------|---------|
| ≥9.0分 | 传奇IP | +50% |
| 8.5-9.0分 | 顶级IP | +40% |
| 8.0-8.5分 | 优质IP | +30% |
| 7.5-8.0分 | 知名IP | +25% |
| 7.0-7.5分 | 热门IP | +20% |
| 6.5-7.0分 | 普通IP | +15% |
| <6.5分 | 小众IP | +10%（保底）|

**核心方法**:
- `calculateIPBonus()`: 计算销量加成（返回0.1-0.5）
- `getIPLevel()`: 获取IP等级描述

---

## 收购流程变更

### 旧系统（已废弃）
- 收购后继承1-2款热门游戏
- 游戏直接成为玩家的游戏
- 需要初始化收益数据

### 新系统（当前）
- 收购后获得**所有游戏的IP**
- 将`CompetitorGame`转换为`GameIP`
- 保存到`SaveData.ownedIPs`列表

### 核心修改

**CompetitorData.kt - completeAcquisition()**
```kotlin
fun completeAcquisition(
    targetCompany: CompetitorCompany,
    finalPrice: Long,
    acquiredYear: Int,
    acquiredMonth: Int
): Triple<Long, Int, List<GameIP>> {
    // 将所有游戏转换为IP
    val inheritedIPs = targetCompany.games.map { game ->
        GameIP(
            id = "ip_${game.id}",
            name = game.name,
            originalCompany = targetCompany.name,
            theme = game.theme,
            originalRating = game.rating,
            acquiredYear = acquiredYear,
            acquiredMonth = acquiredMonth,
            platforms = game.platforms,
            businessModel = game.businessModel
        )
    }
    
    return Triple(marketValueGain, fansGain, inheritedIPs)
}
```

**MainActivity.kt - 收购成功回调**
```kotlin
onAcquisitionSuccess = { acquiredCompany, finalPrice, marketValueGain, fansGain, inheritedIPs ->
    // 扣除收购费用
    money -= finalPrice
    
    // 增加粉丝
    fans += fansGain
    
    // 移除被收购的公司
    competitors = competitors.filter { it.id != acquiredCompany.id }
    
    // 将获得的IP添加到玩家的IP库
    ownedIPs = ownedIPs + inheritedIPs
    
    Log.d("MainActivity", "收购成功：获得${inheritedIPs.size}个IP")
    inheritedIPs.forEach { ip ->
        Log.d("MainActivity", "  - IP: ${ip.name} (${ip.getIPLevel()}, 评分${ip.originalRating}, 加成${(ip.calculateIPBonus() * 100).toInt()}%)")
    }
}
```

---

## 数据存储

### SaveData新增字段
```kotlin
data class SaveData(
    // ... 其他字段 ...
    val ownedIPs: List<GameIP> = emptyList()  // 拥有的游戏IP列表
)
```

### 状态管理
**MainActivity.kt**:
```kotlin
var ownedIPs by remember { mutableStateOf(saveData?.ownedIPs ?: emptyList()) }
```

---

## AI竞争对手收购

AI之间收购时：
- **不获得IP**（简化逻辑）
- 只增加市值和粉丝
- 避免游戏内容过度膨胀

```kotlin
onAIWin = { acquirer, acquired, price ->
    // AI收购不获得IP，只增加市值和粉丝
    val (marketValueGain, fansGain, _) = CompetitorManager.completeAcquisition(...)
    
    competitors = competitors.map { company ->
        if (company.id == acquirer.id) {
            company.copy(
                marketValue = company.marketValue + marketValueGain,
                fans = company.fans + fansGain
            )
        } else {
            company
        }
    }
}
```

---

## UI变更

### 收购成功对话框
**旧文字**: "继承游戏：X款"  
**新文字**: "获得IP：X个"

### 新闻文字
**旧**: "继承了X款热门游戏"  
**新**: "获得了X个游戏IP"

---

## 待实现功能

### 1. 创建游戏时选择IP ⏳
**目标**: 在创建游戏界面添加IP选择下拉框
- 显示玩家拥有的所有IP
- 可选择使用某个IP或原创
- 选中IP后显示加成预览

### 2. IP销量加成计算 ⏳
**目标**: 在GameRevenueData.kt中实现加成逻辑

**单机游戏**:
```kotlin
// 基础销量
val baseSales = calculateBaseSales(...)

// 应用IP加成
val ipBonus = game.fromIP?.calculateIPBonus() ?: 0f
val finalSales = (baseSales * (1 + ipBonus)).toLong()
```

**网络游戏**:
```kotlin
// 基础注册数
val baseRegistrations = calculateBaseRegistrations(...)

// 应用IP加成
val ipBonus = game.fromIP?.calculateIPBonus() ?: 0f
val finalRegistrations = (baseRegistrations * (1 + ipBonus)).toLong()
```

---

## 数值平衡

### IP加成设计理念
1. **保底加成10%**: 即使低评分IP也有价值
2. **最高加成50%**: 传奇IP可大幅提升销量
3. **梯度合理**: 每0.5分一个档次
4. **鼓励收购**: 高质量IP值得付出高昂收购代价

### 收购成本与收益
假设收购一家拥有3款游戏的公司：
- **收购价格**: 目标市值 × 1.2-2.5倍
- **获得IP**: 3个（含原游戏评分）
- **长期收益**: 
  - 每款使用IP的游戏 +10%-50% 销量
  - 可重复使用多次
  - 持续产生价值

---

## 文件修改列表

### 已修改
1. ✅ `GameData.kt`: 添加GameIP数据类、Game.fromIP字段、SaveData.ownedIPs字段
2. ✅ `CompetitorData.kt`: 修改completeAcquisition()返回List<GameIP>
3. ✅ `MainActivity.kt`: 
   - 添加GameIP import
   - 添加ownedIPs状态管理
   - 修改收购成功回调
   - 修改AI收购逻辑
4. ✅ `CompetitorScreen.kt`:
   - 添加GameIP import
   - 修改所有回调类型
   - 更新UI文字

### 待修改
1. ⏳ 创建游戏界面（添加IP选择功能）
2. ⏳ `GameRevenueData.kt`（添加IP销量加成计算）

---

## 向后兼容

- **旧存档**: `ownedIPs`默认为空列表
- **数据结构**: 所有新字段都有默认值
- **存档系统**: Gson自动序列化新字段
- **无需迁移**: 旧存档可直接加载使用

---

## 设计优势

### 1. 策略深度增加
- 收购不再是简单的资产获取
- 需要考虑IP质量和长期价值
- 选择开发原创还是使用IP

### 2. 数值更合理
- 不再直接继承运营中的游戏
- 避免收入数据突然暴涨
- IP加成可控且可预测

### 3. 游戏性提升
- IP可重复使用，价值更高
- 鼓励玩家收购高质量公司
- 增加游戏续作和系列化玩法

### 4. 代码更简洁
- 不需要处理复杂的游戏继承逻辑
- 不需要初始化收益数据
- 减少存档数据膨胀

---

## 使用示例

### 场景1: 收购高评分IP
1. 玩家发现竞争对手有一款9.2分的动作游戏
2. 花费2000万收购该公司
3. 获得该IP（传奇IP，+50%加成）
4. 开发续作时使用该IP
5. 销量从原本1000份提升到1500份
6. 长期收益远超收购成本

### 场景2: 收购低成本IP
1. 收购小公司，获得6.5分IP（+15%加成）
2. 收购成本较低（100万左右）
3. 开发多款游戏时使用该IP
4. 虽然单次加成不高，但可重复使用
5. 性价比不错

---

## 后续优化方向

### 1. IP衰减机制
- IP随时间逐渐失去热度
- 收购后X年内加成逐渐降低
- 鼓励及时使用IP

### 2. IP续作加成
- 使用同一IP的续作有额外加成
- 建立游戏系列品牌

### 3. IP组合效果
- 拥有某主题的多个IP可触发组合效果
- 如：拥有3个动作IP → 动作游戏 +5%额外加成

### 4. IP展示界面
- 独立的"IP库"界面
- 展示所有拥有的IP及其详情
- 按主题、评分、获得时间排序

---

## 常见问题

### Q: 收购后立即使用IP会怎样？
A: 正常获得加成，没有冷却期。

### Q: 一个IP可以使用多次吗？
A: 可以，IP不会消耗。

### Q: AI收购会获得IP吗？
A: 不会，AI收购只增加市值和粉丝。

### Q: 旧存档的收购游戏会怎样？
A: 已经继承的游戏保持不变，新的收购会获得IP。

### Q: IP的加成会叠加吗？
A: 每个游戏只能使用一个IP，不叠加。

---

## 总结

收购子公司IP系统是对原收购系统的重大优化：
- ✅ 更符合真实商业逻辑
- ✅ 增加策略深度和可玩性
- ✅ 数值更合理，避免膨胀
- ✅ 代码更简洁，易于维护
- ✅ 完全向后兼容

下一步需要实现创建游戏时的IP选择功能和销量加成计算，完整系统即可投入使用。
