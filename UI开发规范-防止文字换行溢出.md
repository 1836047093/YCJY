# UI开发规范 - 防止文字换行和溢出问题

## ⚠️ 核心原则：永绝后患

**所有Text组件必须明确指定 `maxLines` 和 `overflow` 属性！**

---

## 📋 强制性规范

### 1. 优先使用标准化组件

✅ **推荐做法**：使用 `StandardizedText.kt` 中的组件

```kotlin
// ✅ 正确：使用 SingleLineText
SingleLineText(
    text = "👥 可用员工: ${count}人",
    fontSize = 12.sp
)

// ✅ 正确：使用 MultiLineText
MultiLineText(
    text = "游戏描述...",
    maxLines = 3
)

// ✅ 正确：使用 CardTitleText
CardTitleText(
    text = "游戏标题"
)
```

❌ **禁止做法**：直接使用 Text() 不设置属性

```kotlin
// ❌ 错误：会导致换行和溢出
Text(
    text = "可用员工: ${count}人",
    fontSize = 12.sp
)
```

---

### 2. 特殊情况：必须使用原生Text时

如果必须使用原生 `Text()` 组件，**必须**添加以下属性：

```kotlin
Text(
    text = "文字内容",
    maxLines = 1,  // ⚠️ 必须指定
    overflow = TextOverflow.Ellipsis,  // ⚠️ 必须指定
    modifier = Modifier...
)
```

---

## 🎯 标准化组件使用指南

### SingleLineText - 单行文本（强制不换行）

**适用场景**：
- 标签："可用员工"、"总收入"、"市值"
- 标题："游戏名称"、"公司名称"
- 数值："¥1,000,000"、"10,000人"
- 状态："开发中"、"已发售"、"进行中"
- 按钮文字："确定"、"取消"、"开始宣传"

**示例**：
```kotlin
// 统计卡片
Card {
    SingleLineText(
        text = "👥 可用员工: ${count}人",
        fontSize = 12.sp,
        color = Color.White
    )
}

// 游戏名称
SingleLineText(
    text = game.name,
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold
)
```

---

### MultiLineText - 多行文本（限制最大行数）

**适用场景**：
- 游戏描述
- 新闻内容
- 详细说明
- 提示信息
- 评论内容

**示例**：
```kotlin
// 游戏描述（最多3行）
MultiLineText(
    text = game.description,
    maxLines = 3,
    fontSize = 14.sp
)

// 新闻内容（最多5行）
MultiLineText(
    text = news.content,
    maxLines = 5,
    fontSize = 13.sp
)
```

---

### CardTitleText - 卡片标题

**适用场景**：
- 卡片标题
- 对话框标题
- 区域标题

**示例**：
```kotlin
Card {
    Column {
        CardTitleText(text = "游戏信息")
        // 其他内容...
    }
}
```

---

### ValueText - 数值显示

**适用场景**：
- 居中显示的数值
- 统计数据
- 金额显示

**示例**：
```kotlin
Column(horizontalAlignment = Alignment.CenterHorizontally) {
    LabelText(text = "总收入")
    ValueText(
        text = formatMoney(revenue),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}
```

---

### LabelText - 标签文本

**适用场景**：
- 字段标签
- 提示文字（半透明）
- 次要信息

**示例**：
```kotlin
Column {
    LabelText(text = "游戏类型")
    SingleLineText(text = game.type)
}
```

---

## 🔍 常见问题场景和解决方案

### 场景1：标签 + 数值

```kotlin
// ❌ 错误：可能换行
Row {
    Text("可用员工: ")
    Text("${count}人")
}

// ✅ 正确：合并为单行
SingleLineText(
    text = "可用员工: ${count}人"
)

// ✅ 正确：分开但都单行
Row {
    LabelText("可用员工:")
    Spacer(Modifier.width(4.dp))
    ValueText("${count}人")
}
```

---

### 场景2：长标题

```kotlin
// ❌ 错误：标题可能换行
Text(
    text = "这是一个非常非常长的游戏标题名称",
    fontSize = 16.sp
)

// ✅ 正确：自动截断
SingleLineText(
    text = "这是一个非常非常长的游戏标题名称",
    fontSize = 16.sp
)
// 显示效果："这是一个非常非常长的游戏..."
```

---

### 场景3：PrimaryTabRow标签

```kotlin
// ❌ 错误：标签可能换行或截断
PrimaryTabRow(selectedTabIndex = selectedTab) {
    Tab(
        selected = selectedTab == 0,
        onClick = { onTabSelected(0) },
        text = { Text("动态新闻") }  // ❌ 可能截断
    )
}

// ✅ 正确：使用简短文字 + 单行限制
PrimaryTabRow(selectedTabIndex = selectedTab) {
    Tab(
        selected = selectedTab == 0,
        onClick = { onTabSelected(0) },
        text = { SingleLineText("新闻") }  // ✅ 简短且单行
    )
}
```

---

### 场景4：Compose布局中的Text

```kotlin
// ❌ 错误：Column中使用fillMaxSize
Column {
    Text("标题")
    Box(Modifier.fillMaxSize()) {  // ❌ 会占据所有空间
        Text("内容")
    }
}

// ✅ 正确：使用weight(1f)
Column {
    CardTitleText("标题")
    Box(Modifier.weight(1f)) {  // ✅ 占据剩余空间
        MultiLineText("内容", maxLines = 3)
    }
}
```

---

## 📝 代码审查清单

在提交代码前，检查以下项目：

- [ ] 所有短文本（标签、标题、数值）使用了 `SingleLineText`
- [ ] 所有长文本（描述、说明）使用了 `MultiLineText` 并指定 `maxLines`
- [ ] 没有裸露的 `Text()` 组件（除非明确添加了 `maxLines` 和 `overflow`）
- [ ] Tab标签文字简短（≤4个汉字）且使用 `SingleLineText`
- [ ] Column中的LazyColumn使用了 `weight(1f)` 而不是 `fillMaxSize()`
- [ ] 所有对话框内容使用了 `heightIn()` 约束

---

## 🚨 已知问题和修复记录

### 已修复问题：

1. **竞争对手界面标签截断** (2024-01-23)
   - 问题：PrimaryTabRow标签文字过长
   - 修复：简化标签文字 + 添加 `maxLines=1`

2. **宣传中心空状态占满屏幕** (2024-01-23)
   - 问题：Box使用 `fillMaxSize()` 在 Column 中
   - 修复：改用 `weight(1f)`

3. **可用员工文字换行** (当前)
   - 问题：Text组件缺少 `maxLines` 和 `overflow`
   - 修复：添加 `maxLines=1` 和 `overflow=TextOverflow.Ellipsis`

---

## 🎓 最佳实践总结

1. **默认使用标准化组件**：`SingleLineText`、`MultiLineText`
2. **标签要简短**：Tab标签、按钮文字控制在4个汉字以内
3. **明确指定行数**：所有Text必须有 `maxLines`
4. **避免fillMaxSize**：Column中优先使用 `weight(1f)`
5. **测试不同分辨率**：确保在小屏幕上也能正常显示

---

## 📚 参考资料

- `StandardizedText.kt` - 标准化组件实现
- `UI自适应分辨率规范.md` - 整体布局规范
- Jetpack Compose官方文档：[Text](https://developer.android.com/jetpack/compose/text)

---

**更新日期**：2024-01-23  
**维护者**：Cascade  
**版本**：1.0
