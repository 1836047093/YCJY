# Firebase 兑换码初始化工具

## 📋 功能说明

这个工具用于将兑换码批量同步到 Firebase Firestore，方便查看和管理兑换码使用记录。

## 🚀 使用方法

### 方法1：在代码中调用（推荐）

在应用启动时或需要时调用初始化函数：

```kotlin
import com.example.yjcy.utils.FirebaseRedeemCodeManager
import kotlinx.coroutines.launch

// 在 MainActivity 或 Application 中
lifecycleScope.launch {
    // 初始化支持者兑换码（SUPPORTER001-150）
    val count = FirebaseRedeemCodeManager.initializeSupporterCodes()
    Log.d("Init", "已初始化 $count 个支持者兑换码")
    
    // 初始化GM模式兑换码
    val gmSuccess = FirebaseRedeemCodeManager.initializeGMCode()
    Log.d("Init", "GM兑换码初始化: ${if (gmSuccess) "成功" else "失败"}")
}
```

### 方法2：创建测试按钮（开发阶段）

在设置界面添加一个测试按钮：

```kotlin
Button(
    onClick = {
        coroutineScope.launch {
            val count = FirebaseRedeemCodeManager.initializeSupporterCodes()
            Toast.makeText(context, "已初始化 $count 个兑换码", Toast.LENGTH_SHORT).show()
        }
    }
) {
    Text("初始化兑换码到Firebase")
}
```

## 📊 查看兑换码记录

### 在 Firebase Console 中查看

1. 打开 [Firebase Console](https://console.firebase.google.com/)
2. 选择项目 → **Firestore Database**
3. 查看 `redeem_codes` 集合
4. 每个兑换码一个文档，文档ID就是兑换码（如 `SUPPORTER149`）

### 在代码中查询

```kotlin
// 获取所有已使用的兑换码
lifecycleScope.launch {
    val usedCodes = FirebaseRedeemCodeManager.getUsedRedeemCodes()
    usedCodes.forEach { codeData ->
        Log.d("RedeemCode", "兑换码: ${codeData.code}, 使用者: ${codeData.usedByUserId}, 使用时间: ${codeData.usedAt}")
    }
}

// 获取特定兑换码的记录
lifecycleScope.launch {
    val records = FirebaseRedeemCodeManager.getRedeemCodeRecords("SUPPORTER149")
    records.forEach { codeData ->
        Log.d("RedeemCode", "兑换码: ${codeData.code}, 已使用: ${codeData.usedCount > 0}, 使用者: ${codeData.usedByUserId}")
    }
}
```

## 📐 Firestore 数据结构

### Collection: `redeem_codes`

每个兑换码一个文档：

```json
{
  "code": "SUPPORTER149",
  "type": "supporter",
  "isValid": true,
  "maxUses": 1,
  "usedCount": 1,
  "usedByUserId": "mT/ACJluBZGQowXTWMmnKg==",
  "usedAt": "2025-11-07T01:20:59Z",
  "createdAt": "2025-11-07T01:00:00Z"
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | String | 兑换码（文档ID） |
| `type` | String | 类型（"supporter", "gm"） |
| `isValid` | Boolean | 是否有效 |
| `maxUses` | Int | 最大使用次数（1=全局唯一） |
| `usedCount` | Int | 已使用次数 |
| `usedByUserId` | String | 使用该兑换码的用户ID |
| `usedAt` | Timestamp | 使用时间 |
| `createdAt` | Timestamp | 创建时间 |

## ✅ 初始化后的效果

初始化后，您可以在 Firebase Console 中：

1. **查看所有兑换码**：`redeem_codes` 集合中有 151 个文档（150个支持者 + 1个GM）
2. **查看使用状态**：
   - `usedCount = 0`：未使用
   - `usedCount = 1`：已使用
   - `usedByUserId`：显示使用者ID
   - `usedAt`：显示使用时间
3. **筛选已使用的兑换码**：在 Firebase Console 中使用筛选功能

## 🔍 查询示例

### 查询所有已使用的兑换码

在 Firebase Console 中：
1. 进入 `redeem_codes` 集合
2. 添加筛选条件：`usedCount` > `0`
3. 查看结果

### 查询特定用户的兑换码

在代码中：
```kotlin
lifecycleScope.launch {
    val allCodes = FirebaseRedeemCodeManager.getRedeemCodeRecords()
    val userCodes = allCodes.filter { it.usedByUserId == userId }
    // 显示该用户使用的所有兑换码
}
```

## ⚠️ 注意事项

1. **只初始化一次**：初始化函数使用 `SetOptions.merge()`，可以安全地多次调用，不会覆盖已有数据
2. **批量操作**：初始化150个兑换码会分批提交（每500个一批），可能需要几秒钟
3. **网络要求**：需要网络连接才能同步到Firebase
4. **权限要求**：确保 Firestore 规则允许写入 `redeem_codes` 集合

## 🎯 推荐流程

1. **首次部署**：在应用启动时自动初始化（只执行一次）
2. **查看记录**：在 Firebase Console 中查看兑换码使用情况
3. **数据分析**：使用 `getUsedRedeemCodes()` 获取统计数据

## 📝 示例代码

完整的初始化示例：

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化兑换码（只在首次启动时执行一次）
        lifecycleScope.launch {
            val sharedPrefs = getSharedPreferences("app_init", MODE_PRIVATE)
            val codesInitialized = sharedPrefs.getBoolean("codes_initialized", false)
            
            if (!codesInitialized) {
                Log.d("Init", "开始初始化兑换码...")
                val count = FirebaseRedeemCodeManager.initializeSupporterCodes()
                FirebaseRedeemCodeManager.initializeGMCode()
                
                if (count > 0) {
                    sharedPrefs.edit().putBoolean("codes_initialized", true).apply()
                    Log.d("Init", "✅ 兑换码初始化完成: $count 个")
                }
            }
        }
    }
}
```

