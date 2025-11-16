# MOBA电竞系统 - 英雄系统设计

## 一、英雄数据结构

```kotlin
data class MobaHero(
    val id: String,              // 唯一ID
    val name: String,            // 英雄名称
    val title: String,           // 称号（如"德玛西亚之力"）
    val position: HeroPosition,  // 位置
    val type: HeroType,          // 类型
    val difficulty: Int,         // 难度（1-5）
    val strength: HeroStrength,  // 强度属性
    val counters: List<String>,  // 克制的英雄ID
    val counteredBy: List<String>, // 被克制的英雄ID
    val releaseDate: Date,       // 发布日期
    val version: String,         // 版本号
    val winRate: Double = 50.0,  // 胜率（动态）
    val pickRate: Double = 10.0, // 选取率（动态）
    val banRate: Double = 5.0    // 禁用率（动态）
)

enum class HeroPosition(val displayName: String) {
    TOP("上单"),
    JUNGLE("打野"),
    MID("中单"),
    ADC("下路"),
    SUPPORT("辅助")
}

enum class HeroType(val displayName: String) {
    TANK("坦克"),
    FIGHTER("战士"),
    ASSASSIN("刺客"),
    MAGE("法师"),
    MARKSMAN("射手"),
    SUPPORT("辅助")
}

data class HeroStrength(
    val damage: Int,        // 伤害（1-100）
    val tankiness: Int,     // 坦度（1-100）
    val mobility: Int,      // 机动性（1-100）
    val control: Int,       // 控制能力（1-100）
    val utility: Int        // 工具性（1-100）
)
```

---

## 二、初始20个英雄设计

### 上单（4个）

#### 1. 铁山·盖伦
```kotlin
MobaHero(
    id = "hero_001",
    name = "铁山",
    title = "德玛西亚之力",
    position = HeroPosition.TOP,
    type = HeroType.FIGHTER,
    difficulty = 1,
    strength = HeroStrength(
        damage = 70,
        tankiness = 80,
        mobility = 40,
        control = 30,
        utility = 50
    ),
    counters = listOf("hero_016"), // 克制射手
    counteredBy = listOf("hero_011") // 被法师克制
)
```

#### 2. 暗影刃
```kotlin
MobaHero(
    id = "hero_002",
    name = "暗影刃",
    title = "影之哀伤",
    position = HeroPosition.TOP,
    type = HeroType.ASSASSIN,
    difficulty = 4,
    strength = HeroStrength(
        damage = 90,
        tankiness = 30,
        mobility = 85,
        control = 20,
        utility = 40
    ),
    counters = listOf("hero_009", "hero_013"), // 克制法师和射手
    counteredBy = listOf("hero_003", "hero_017") // 被坦克和辅助克制
)
```

#### 3. 巨石守卫
```kotlin
MobaHero(
    id = "hero_003",
    name = "巨石守卫",
    title = "不朽石像",
    position = HeroPosition.TOP,
    type = HeroType.TANK,
    difficulty = 2,
    strength = HeroStrength(
        damage = 40,
        tankiness = 95,
        mobility = 30,
        control = 70,
        utility = 60
    ),
    counters = listOf("hero_002", "hero_006"), // 克制刺客
    counteredBy = listOf("hero_001") // 被战士克制
)
```

#### 4. 剑圣·易
```kotlin
MobaHero(
    id = "hero_004",
    name = "剑圣",
    title = "无极剑道",
    position = HeroPosition.TOP,
    type = HeroType.FIGHTER,
    difficulty = 3,
    strength = HeroStrength(
        damage = 85,
        tankiness = 50,
        mobility = 75,
        control = 15,
        utility = 45
    ),
    counters = listOf("hero_003"), // 克制坦克
    counteredBy = listOf("hero_017") // 被控制型辅助克制
)
```

---

### 打野（4个）

#### 5. 狂野猎手
```kotlin
MobaHero(
    id = "hero_005",
    name = "狂野猎手",
    title = "丛林之王",
    position = HeroPosition.JUNGLE,
    type = HeroType.FIGHTER,
    difficulty = 3,
    strength = HeroStrength(
        damage = 75,
        tankiness = 65,
        mobility = 70,
        control = 40,
        utility = 55
    ),
    counters = listOf("hero_013"), // 克制射手
    counteredBy = listOf("hero_012") // 被法师克制
)
```

#### 6. 暗夜刺客
```kotlin
MobaHero(
    id = "hero_006",
    name = "暗夜刺客",
    title = "隐秘杀手",
    position = HeroPosition.JUNGLE,
    type = HeroType.ASSASSIN,
    difficulty = 4,
    strength = HeroStrength(
        damage = 95,
        tankiness = 25,
        mobility = 90,
        control = 25,
        utility = 35
    ),
    counters = listOf("hero_009", "hero_014"), // 克制法师和射手
    counteredBy = listOf("hero_007", "hero_018") // 被坦克和辅助克制
)
```

#### 7. 巨龙之魂
```kotlin
MobaHero(
    id = "hero_007",
    name = "巨龙之魂",
    title = "远古巨兽",
    position = HeroPosition.JUNGLE,
    type = HeroType.TANK,
    difficulty = 2,
    strength = HeroStrength(
        damage = 50,
        tankiness = 90,
        mobility = 45,
        control = 75,
        utility = 65
    ),
    counters = listOf("hero_006"), // 克制刺客
    counteredBy = listOf("hero_010") // 被百分比伤害法师克制
)
```

#### 8. 疾风剑豪
```kotlin
MobaHero(
    id = "hero_008",
    name = "疾风剑豪",
    title = "御风而行",
    position = HeroPosition.JUNGLE,
    type = HeroType.FIGHTER,
    difficulty = 5,
    strength = HeroStrength(
        damage = 80,
        tankiness = 45,
        mobility = 85,
        control = 50,
        utility = 55
    ),
    counters = listOf("hero_009"), // 克制法师（风墙）
    counteredBy = listOf("hero_003") // 被坦克克制
)
```

---

### 中单（4个）

#### 9. 烈焰法师
```kotlin
MobaHero(
    id = "hero_009",
    name = "烈焰法师",
    title = "火焰之心",
    position = HeroPosition.MID,
    type = HeroType.MAGE,
    difficulty = 2,
    strength = HeroStrength(
        damage = 90,
        tankiness = 30,
        mobility = 50,
        control = 40,
        utility = 45
    ),
    counters = listOf("hero_001", "hero_003"), // 克制战士和坦克
    counteredBy = listOf("hero_002", "hero_008") // 被刺客克制
)
```

#### 10. 冰霜女巫
```kotlin
MobaHero(
    id = "hero_010",
    name = "冰霜女巫",
    title = "寒冰之怒",
    position = HeroPosition.MID,
    type = HeroType.MAGE,
    difficulty = 3,
    strength = HeroStrength(
        damage = 85,
        tankiness = 35,
        mobility = 40,
        control = 80,
        utility = 60
    ),
    counters = listOf("hero_007"), // 克制坦克（百分比伤害）
    counteredBy = listOf("hero_006") // 被刺客克制
)
```

#### 11. 暗影法师
```kotlin
MobaHero(
    id = "hero_011",
    name = "暗影法师",
    title = "虚空之眼",
    position = HeroPosition.MID,
    type = HeroType.MAGE,
    difficulty = 4,
    strength = HeroStrength(
        damage = 95,
        tankiness = 25,
        mobility = 60,
        control = 35,
        utility = 50
    ),
    counters = listOf("hero_001"), // 克制战士
    counteredBy = listOf("hero_002") // 被刺客克制
)
```

#### 12. 时空法师
```kotlin
MobaHero(
    id = "hero_012",
    name = "时空法师",
    title = "时光守护",
    position = HeroPosition.MID,
    type = HeroType.MAGE,
    difficulty = 5,
    strength = HeroStrength(
        damage = 80,
        tankiness = 30,
        mobility = 70,
        control = 70,
        utility = 80
    ),
    counters = listOf("hero_005"), // 克制打野战士
    counteredBy = listOf("hero_006") // 被刺客克制
)
```

---

### ADC（4个）

#### 13. 寒冰射手
```kotlin
MobaHero(
    id = "hero_013",
    name = "寒冰射手",
    title = "弗雷尔卓德之心",
    position = HeroPosition.ADC,
    type = HeroType.MARKSMAN,
    difficulty = 1,
    strength = HeroStrength(
        damage = 85,
        tankiness = 30,
        mobility = 45,
        control = 60,
        utility = 55
    ),
    counters = listOf("hero_003", "hero_007"), // 克制坦克（持续输出）
    counteredBy = listOf("hero_002", "hero_006") // 被刺客克制
)
```

#### 14. 赏金猎人
```kotlin
MobaHero(
    id = "hero_014",
    name = "赏金猎人",
    title = "枪火玫瑰",
    position = HeroPosition.ADC,
    type = HeroType.MARKSMAN,
    difficulty = 2,
    strength = HeroStrength(
        damage = 90,
        tankiness = 25,
        mobility = 50,
        control = 30,
        utility = 50
    ),
    counters = listOf("hero_003"), // 克制坦克
    counteredBy = listOf("hero_006") // 被刺客克制
)
```

#### 15. 虚空射手
```kotlin
MobaHero(
    id = "hero_015",
    name = "虚空射手",
    title = "深渊之箭",
    position = HeroPosition.ADC,
    type = HeroType.MARKSMAN,
    difficulty = 3,
    strength = HeroStrength(
        damage = 95,
        tankiness = 20,
        mobility = 55,
        control = 25,
        utility = 45
    ),
    counters = listOf("hero_007"), // 克制坦克（百分比伤害）
    counteredBy = listOf("hero_002", "hero_006") // 被刺客克制
)
```

#### 16. 圣枪游侠
```kotlin
MobaHero(
    id = "hero_016",
    name = "圣枪游侠",
    title = "光辉之矛",
    position = HeroPosition.ADC,
    type = HeroType.MARKSMAN,
    difficulty = 4,
    strength = HeroStrength(
        damage = 90,
        tankiness = 25,
        mobility = 75,
        control = 30,
        utility = 50
    ),
    counters = listOf("hero_009"), // 克制法师（高机动）
    counteredBy = listOf("hero_001") // 被战士克制
)
```

---

### 辅助（4个）

#### 17. 光辉女神
```kotlin
MobaHero(
    id = "hero_017",
    name = "光辉女神",
    title = "曙光之盾",
    position = HeroPosition.SUPPORT,
    type = HeroType.SUPPORT,
    difficulty = 1,
    strength = HeroStrength(
        damage = 30,
        tankiness = 75,
        mobility = 40,
        control = 85,
        utility = 90
    ),
    counters = listOf("hero_004"), // 克制战士（硬控）
    counteredBy = listOf("hero_010") // 被控制型法师克制
)
```

#### 18. 风暴之怒
```kotlin
MobaHero(
    id = "hero_018",
    name = "风暴之怒",
    title = "疾风之护",
    position = HeroPosition.SUPPORT,
    type = HeroType.SUPPORT,
    difficulty = 2,
    strength = HeroStrength(
        damage = 25,
        tankiness = 40,
        mobility = 65,
        control = 60,
        utility = 95
    ),
    counters = listOf("hero_006"), // 克制刺客（保护）
    counteredBy = listOf("hero_005") // 被战士克制
)
```

#### 19. 星界游神
```kotlin
MobaHero(
    id = "hero_019",
    name = "星界游神",
    title = "铸星龙王",
    position = HeroPosition.SUPPORT,
    type = HeroType.SUPPORT,
    difficulty = 3,
    strength = HeroStrength(
        damage = 50,
        tankiness = 50,
        mobility = 80,
        control = 75,
        utility = 85
    ),
    counters = listOf("hero_008"), // 克制战士（游走）
    counteredBy = listOf("hero_011") // 被法师克制
)
```

#### 20. 深海泰坦
```kotlin
MobaHero(
    id = "hero_020",
    name = "深海泰坦",
    title = "海洋之怒",
    position = HeroPosition.SUPPORT,
    type = HeroType.SUPPORT,
    difficulty = 4,
    strength = HeroStrength(
        damage = 40,
        tankiness = 85,
        mobility = 35,
        control = 90,
        utility = 80
    ),
    counters = listOf("hero_002"), // 克制刺客（硬控）
    counteredBy = listOf("hero_012") // 被工具型法师克制
)
```

---

## 三、英雄扩展机制

### 3.1 新英雄解锁

**触发条件**：玩家更新MOBA游戏时

**概率**：
- 30%概率解锁1个新英雄
- 15%概率解锁2个新英雄
- 5%概率解锁3个新英雄

**设计原则**：
- 新英雄填补位置空缺
- 避免类型过度集中
- 保持克制关系平衡

### 3.2 英雄平衡调整

**触发条件**：游戏版本更新

**调整规则**：
- 胜率>55%：降低5-10点属性
- 胜率<45%：提升5-10点属性
- 禁用率>30%：降低关键属性
- 选取率<2%：提升关键属性

### 3.3 英雄重做

**触发条件**：
- 胜率持续低于45%超过3个版本
- 选取率低于1%

**重做内容**：
- 重新分配属性点
- 调整克制关系
- 更新难度等级

---

## 四、英雄管理器

```kotlin
object HeroManager {
    private val allHeroes = mutableListOf<MobaHero>()
    
    // 初始化20个基础英雄
    fun initializeHeroes(): List<MobaHero> {
        if (allHeroes.isEmpty()) {
            allHeroes.addAll(createInitialHeroes())
        }
        return allHeroes.toList()
    }
    
    // 根据位置获取英雄
    fun getHeroesByPosition(position: HeroPosition): List<MobaHero> {
        return allHeroes.filter { it.position == position }
    }
    
    // 根据类型获取英雄
    fun getHeroesByType(type: HeroType): List<MobaHero> {
        return allHeroes.filter { it.type == type }
    }
    
    // 解锁新英雄
    fun unlockNewHero(hero: MobaHero) {
        if (!allHeroes.any { it.id == hero.id }) {
            allHeroes.add(hero)
        }
    }
    
    // 获取克制关系
    fun getCounterHeroes(heroId: String): List<MobaHero> {
        val hero = allHeroes.find { it.id == heroId } ?: return emptyList()
        return allHeroes.filter { it.id in hero.counters }
    }
    
    // 调整英雄强度
    fun adjustHeroStrength(heroId: String, adjustment: HeroStrength) {
        val index = allHeroes.indexOfFirst { it.id == heroId }
        if (index != -1) {
            val hero = allHeroes[index]
            allHeroes[index] = hero.copy(
                strength = hero.strength.copy(
                    damage = (hero.strength.damage + adjustment.damage).coerceIn(1, 100),
                    tankiness = (hero.strength.tankiness + adjustment.tankiness).coerceIn(1, 100),
                    mobility = (hero.strength.mobility + adjustment.mobility).coerceIn(1, 100),
                    control = (hero.strength.control + adjustment.control).coerceIn(1, 100),
                    utility = (hero.strength.utility + adjustment.utility).coerceIn(1, 100)
                )
            )
        }
    }
    
    // 更新英雄统计数据
    fun updateHeroStats(heroId: String, winRate: Double, pickRate: Double, banRate: Double) {
        val index = allHeroes.indexOfFirst { it.id == heroId }
        if (index != -1) {
            allHeroes[index] = allHeroes[index].copy(
                winRate = winRate,
                pickRate = pickRate,
                banRate = banRate
            )
        }
    }
    
    private fun createInitialHeroes(): List<MobaHero> {
        // 返回上面定义的20个英雄
        return listOf(
            // ... 所有英雄实例
        )
    }
}
```

---

## 五、UI展示

### 5.1 英雄图鉴界面

```
┌─────────────────────────────────┐
│  🎮 英雄图鉴 (20/100)            │
├─────────────────────────────────┤
│  [全部] [上单] [打野] [中单]...  │
├─────────────────────────────────┤
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐       │
│  │铁山│ │暗影│ │巨石│ │剑圣│      │
│  │⭐⭐│ │⭐⭐⭐│ │⭐⭐│ │⭐⭐⭐│     │
│  │战士│ │刺客│ │坦克│ │战士│      │
│  └───┘ └───┘ └───┘ └───┘       │
│  胜率:52% 胜率:48% 胜率:51% ...  │
└─────────────────────────────────┘
```

### 5.2 英雄详情卡片

```
┌─────────────────────────────────┐
│  铁山 - 德玛西亚之力              │
│  ⭐⭐ 难度：简单                   │
├─────────────────────────────────┤
│  位置：上单 | 类型：战士           │
│                                 │
│  属性：                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│  伤害  ████████░░ 70            │
│  坦度  ████████░░ 80            │
│  机动  ████░░░░░░ 40            │
│  控制  ███░░░░░░░ 30            │
│  工具  █████░░░░░ 50            │
│                                 │
│  克制：射手 | 被克制：法师         │
│                                 │
│  数据统计：                      │
│  胜率: 52.3% | 选取率: 12.5%    │
│  禁用率: 3.2%                    │
└─────────────────────────────────┘
```

---

## 六、数据持久化

```kotlin
data class SaveData(
    // ... 现有字段
    
    // 英雄系统
    val mobaHeroes: List<MobaHero> = emptyList(),
    val unlockedHeroIds: List<String> = emptyList(),
    val heroVersion: String = "1.0.0"
)
```

---

## 七、测试用例

### 7.1 初始化测试
- [ ] 验证20个英雄全部生成
- [ ] 验证每个位置有4个英雄
- [ ] 验证克制关系正确

### 7.2 扩展测试
- [ ] 验证新英雄解锁不重复
- [ ] 验证属性调整在合理范围
- [ ] 验证重做机制触发

### 7.3 性能测试
- [ ] 验证英雄列表查询速度
- [ ] 验证克制关系计算速度
