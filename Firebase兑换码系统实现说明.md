# Firebase 兑换码系统实现说明

## 🎯 功能概述

使用 Firebase Firestore 实现**云端兑换码管理系统**，与 TapTap 用户 ID 绑定。

### ✅ 核心功能

1. **云端存储** - 兑换码使用记录存储在 Firebase Firestore
2. **跨设备同步** - 同一账号在不同设备上共享兑换码状态
3. **防作弊** - 数据存储在云端，无法本地篡改
4. **账号绑定** - 与 TapTap unionId/openId 绑定
5. **实时验证** - 确保兑换码全局唯一使用
6. **向后兼容** - 支持从本地 SharedPreferences 迁移

## 📐 Firestore 数据结构

### Collection: `user_redeem_codes`

每个用户一个文档，Document ID 为 TapTap userId：

```kotlin
{
  "userId": "tap_union_id_12345",
  "usedCodes": ["PROGM", "SUPPORTER001", "SUPPORTER002"],
  "gmModeUnlocked": true,
  "supporterUnlocked": true,
  "lastUpdated": Timestamp(2025, 1, 7, 10, 30, 0)
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `userId` | String | TapTap unionId 或 openId |
| `usedCodes` | Array<String> | 已使用的兑换码列表（大写） |
| `gmModeUnlocked` | Boolean | 是否解锁 GM 模式 |
| `supporterUnlocked` | Boolean | 是否解锁支持者功能 |
| `lastUpdated` | Timestamp | 最后更新时间 |

## 🚀 使用方法

### 1. 初始化（自动）

Firebase 已在 `build.gradle.kts` 中配置，无需手动初始化。

### 2. 检查兑换码是否已使用

```kotlin
import com.example.yjcy.utils.FirebaseRedeemCodeManager
import kotlinx.coroutines.launch

// 在协程中调用
lifecycleScope.launch {
    val userId = getTapTapUserId() // 获取 TapTap 用户 ID
    val isUsed = FirebaseRedeemCodeManager.isCodeUsedByUser(userId, "SUPPORTER001")
    
    if (isUsed) {
        // 兑换码已使用
    } else {
        // 兑换码未使用
    }
}
```

### 3. 标记兑换码为已使用

```kotlin
lifecycleScope.launch {
    val userId = getTapTapUserId()
    val success = FirebaseRedeemCodeManager.markCodeAsUsed(
        userId = userId,
        code = "SUPPORTER001",
        codeType = "supporter"  // 或 "gm"
    )
    
    if (success) {
        // 标记成功
        Log.d("RedeemCode", "兑换码使用成功")
    } else {
        // 标记失败
        Log.e("RedeemCode", "兑换码使用失败")
    }
}
```

### 4. 检查 GM 模式是否解锁

```kotlin
lifecycleScope.launch {
    val userId = getTapTapUserId()
    val isUnlocked = FirebaseRedeemCodeManager.isGMModeUnlocked(userId)
    
    if (isUnlocked) {
        // GM 模式已解锁
    }
}
```

### 5. 检查支持者功能是否解锁

```kotlin
lifecycleScope.launch {
    val userId = getTapTapUserId()
    val isUnlocked = FirebaseRedeemCodeManager.isSupporterFeatureUnlocked(
        userId = userId,
        usedRedeemCodes = emptySet()  // 可选：传入本地存档的兑换码（向后兼容）
    )
    
    if (isUnlocked) {
        // 支持者功能已解锁
    }
}
```

### 6. 获取用户已使用的兑换码列表

```kotlin
lifecycleScope.launch {
    val userId = getTapTapUserId()
    val usedCodes = FirebaseRedeemCodeManager.getUserUsedCodes(userId)
    
    Log.d("RedeemCode", "已使用的兑换码: $usedCodes")
}
```

### 7. 从本地迁移到云端（首次登录时）

```kotlin
lifecycleScope.launch {
    val userId = getTapTapUserId()
    val localCodes = RedeemCodeManager.getUserUsedCodes(userId) // 从本地获取
    
    if (localCodes.isNotEmpty()) {
        val success = FirebaseRedeemCodeManager.migrateFromLocal(userId, localCodes)
        if (success) {
            Log.d("RedeemCode", "数据迁移成功")
        }
    }
}
```

## 🔄 与现有系统集成

### 在 MainActivity 中使用

```kotlin
// 在 GameScreen 函数中
val tapTapAccount = TapLoginManager.getCurrentAccount()
val userId = tapTapAccount?.unionId ?: tapTapAccount?.openId

// 检查云端兑换码状态
LaunchedEffect(userId) {
    if (userId != null) {
        // 首次登录时，迁移本地数据
        val localCodes = RedeemCodeManager.getUserUsedCodes(userId)
        FirebaseRedeemCodeManager.migrateFromLocal(userId, localCodes)
        
        // 检查 GM 模式
        val gmUnlocked = FirebaseRedeemCodeManager.isGMModeUnlocked(userId)
        if (gmUnlocked && !gmModeEnabled) {
            onGMToggle(true)
        }
        
        // 检查支持者功能
        val supporterUnlocked = FirebaseRedeemCodeManager.hasUsedSupporterCode(userId)
        // 根据需要更新 UI 状态
    }
}

// 使用兑换码时
Button(onClick = {
    coroutineScope.launch {
        val code = redeemCode.trim().uppercase()
        
        // 检查是否已使用
        val isUsed = FirebaseRedeemCodeManager.isCodeUsedByUser(userId, code)
        if (isUsed) {
            // 已使用过
            return@launch
        }
        
        // 验证兑换码
        if (FirebaseRedeemCodeManager.isValidSupporterCode(code)) {
            // 标记为已使用
            val success = FirebaseRedeemCodeManager.markCodeAsUsed(
                userId = userId,
                code = code,
                codeType = "supporter"
            )
            
            if (success) {
                // 兑换成功
                showSuccessDialog = true
            } else {
                // 兑换失败
                showErrorDialog = true
            }
        }
    }
}) {
    Text("兑换")
}
```

## 🔒 Firestore 安全规则

在 Firebase Console 中设置以下安全规则：

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // 用户兑换码记录
    match /user_redeem_codes/{userId} {
      // 只允许用户读写自己的记录
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // 或者如果使用 TapTap 自定义认证
      allow read, write: if request.auth != null && 
                            request.auth.token.tapUserId == userId;
    }
    
    // 兑换码验证（可选）
    match /redeem_codes/{code} {
      // 所有认证用户可读，仅管理员可写
      allow read: if request.auth != null;
      allow write: if false;  // 仅通过 Firebase Admin SDK 写入
    }
  }
}
```

### 注意：TapTap 认证集成

由于使用 TapTap 登录，需要通过以下方式之一认证 Firebase：

#### 方案 1：匿名认证（推荐用于开发测试）

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

lifecycleScope.launch {
    try {
        val result = Firebase.auth.signInAnonymously().await()
        Log.d("Firebase", "匿名登录成功: ${result.user?.uid}")
    } catch (e: Exception) {
        Log.e("Firebase", "匿名登录失败", e)
    }
}
```

#### 方案 2：自定义 Token（推荐用于生产环境）

1. 后端接收 TapTap token
2. 验证 TapTap token
3. 生成 Firebase 自定义 token
4. 客户端使用自定义 token 登录 Firebase

```kotlin
// 客户端代码
Firebase.auth.signInWithCustomToken(customToken).await()
```

#### 方案 3：测试模式（仅开发阶段）

Firestore 安全规则设置为：

```javascript
// ⚠️ 仅用于开发测试，生产环境禁用
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;  // 允许所有访问
    }
  }
}
```

## 📊 数据监控

### 查看数据

1. 登录 [Firebase Console](https://console.firebase.google.com/)
2. 选择项目
3. 进入 Firestore Database
4. 查看 `user_redeem_codes` 集合

### 统计分析

可以使用 Firebase Analytics 或直接查询 Firestore 统计：
- 兑换码使用数量
- GM 模式解锁用户数
- 支持者用户数

## 🔧 高级功能

### 1. 实时监听兑换码变化

```kotlin
val userId = getTapTapUserId()
val db = FirebaseFirestore.getInstance()

db.collection("user_redeem_codes")
    .document(userId)
    .addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.e("Firebase", "监听失败", error)
            return@addSnapshotListener
        }
        
        if (snapshot != null && snapshot.exists()) {
            val data = snapshot.toObject(FirebaseRedeemCodeManager.UserRedeemData::class.java)
            // 更新 UI
        }
    }
```

### 2. 批量验证兑换码

```kotlin
suspend fun validateMultipleCodes(userId: String, codes: List<String>): Map<String, Boolean> {
    return codes.associateWith { code ->
        !FirebaseRedeemCodeManager.isCodeUsedByUser(userId, code)
    }
}
```

## ✅ 迁移步骤

### 从本地 SharedPreferences 迁移到 Firebase

1. **保留现有 RedeemCodeManager**（向后兼容）
2. **首次登录时自动迁移**：
   ```kotlin
   // 在用户登录后
   val localCodes = RedeemCodeManager.getUserUsedCodes(userId)
   FirebaseRedeemCodeManager.migrateFromLocal(userId, localCodes)
   ```
3. **优先使用 Firebase 数据**：
   ```kotlin
   // 优先从云端读取
   val cloudCodes = FirebaseRedeemCodeManager.getUserUsedCodes(userId)
   
   // 如果云端为空，尝试本地
   if (cloudCodes.isEmpty()) {
       val localCodes = RedeemCodeManager.getUserUsedCodes(userId)
       if (localCodes.isNotEmpty()) {
           FirebaseRedeemCodeManager.migrateFromLocal(userId, localCodes)
       }
   }
   ```

## 🎯 优势总结

| 特性 | 本地存储 | Firebase |
|------|---------|----------|
| 跨设备同步 | ❌ | ✅ |
| 防作弊 | ❌ | ✅ |
| 数据备份 | ❌ | ✅ |
| 实时同步 | ❌ | ✅ |
| 数据分析 | ❌ | ✅ |
| 离线访问 | ✅ | ✅ (缓存) |
| 实现难度 | 简单 | 中等 |

## 📝 TODO

- [ ] 在 MainActivity 中集成 FirebaseRedeemCodeManager
- [ ] 实现 Firebase 匿名认证或自定义认证
- [ ] 设置 Firestore 安全规则
- [ ] 测试云端同步功能
- [ ] 实现数据迁移逻辑
- [ ] 添加错误处理和重试机制
- [ ] 添加离线缓存支持

## 🔗 相关资源

- [Firebase Firestore 文档](https://firebase.google.com/docs/firestore)
- [Firebase Authentication 文档](https://firebase.google.com/docs/auth)
- [Firestore 安全规则](https://firebase.google.com/docs/firestore/security/get-started)

