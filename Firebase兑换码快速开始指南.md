# Firebase 兑换码系统 - 快速开始指南

## ✅ 当前状态

所有文件已创建完成：

1. ✅ `FirebaseRedeemCodeManager.kt` - 云端兑换码管理器
2. ✅ `Firebase兑换码系统实现说明.md` - 详细文档
3. ✅ `Firebase兑换码集成示例.kt` - 集成代码示例
4. ✅ `firestore.rules` - Firestore 安全规则

## 🚀 快速开始（3步完成）

### 第1步：配置 Firebase Console

1. 打开 [Firebase Console](https://console.firebase.google.com/)
2. 选择您的项目
3. 进入 **Firestore Database**
4. 点击 **规则** 标签
5. 复制 `firestore.rules` 的内容并粘贴
6. 点击 **发布**

### 第2步：启用 Firebase Authentication

1. 在 Firebase Console 中进入 **Authentication**
2. 点击 **登录方法** 标签
3. 启用 **匿名登录**（用于开发测试）

### 第3步：同步 Gradle 并测试

1. 在 Android Studio 中点击 **Sync Now**
2. 等待同步完成
3. 运行应用测试

## 📝 集成到 MainActivity

### 方法1：最简单 - 复制粘贴

打开 `Firebase兑换码集成示例.kt`，复制相关代码到您的 `MainActivity.kt`。

### 方法2：逐步集成

#### 1. 添加 Firebase Auth 初始化

在 `MainActivity.onCreate()` 中添加：

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    
    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化 Firebase 匿名认证
        lifecycleScope.launch {
            try {
                if (firebaseAuth.currentUser == null) {
                    firebaseAuth.signInAnonymously().await()
                    Log.d("Firebase", "匿名登录成功")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Firebase 认证失败", e)
            }
        }
        
        // ... 其他代码
    }
}
```

#### 2. 替换兑换码验证逻辑

在处理兑换码的地方，将：

```kotlin
// 旧代码
RedeemCodeManager.isCodeUsedByUser(userId, code)
RedeemCodeManager.markCodeAsUsed(userId, code)
```

替换为：

```kotlin
// 新代码（云端）
FirebaseRedeemCodeManager.isCodeUsedByUser(userId, code)
FirebaseRedeemCodeManager.markCodeAsUsed(userId, code, "supporter")
```

#### 3. 添加数据迁移

在用户登录后添加：

```kotlin
LaunchedEffect(userId) {
    if (userId != null) {
        // 迁移本地数据到云端
        val localCodes = RedeemCodeManager.getUserUsedCodes(userId)
        if (localCodes.isNotEmpty()) {
            FirebaseRedeemCodeManager.migrateFromLocal(userId, localCodes)
        }
    }
}
```

## 🧪 测试步骤

### 1. 测试匿名认证

```kotlin
// 在 MainActivity 中查看日志
lifecycleScope.launch {
    val user = Firebase.auth.currentUser
    Log.d("Test", "Firebase User: ${user?.uid}")
}
```

### 2. 测试兑换码使用

```kotlin
lifecycleScope.launch {
    val userId = "test_user_123"
    
    // 检查是否已使用
    val isUsed = FirebaseRedeemCodeManager.isCodeUsedByUser(userId, "SUPPORTER001")
    Log.d("Test", "兑换码已使用: $isUsed")
    
    // 标记为已使用
    val success = FirebaseRedeemCodeManager.markCodeAsUsed(
        userId = userId,
        code = "SUPPORTER001",
        codeType = "supporter"
    )
    Log.d("Test", "标记成功: $success")
}
```

### 3. 在 Firebase Console 中查看数据

1. 进入 Firestore Database
2. 查看 `user_redeem_codes` 集合
3. 应该能看到新增的文档

## 📊 查看效果

### 兑换码使用后的数据结构

```json
{
  "userId": "tap_union_id_12345",
  "usedCodes": ["PROGM", "SUPPORTER001"],
  "gmModeUnlocked": true,
  "supporterUnlocked": true,
  "lastUpdated": "2025-01-07T10:30:00Z"
}
```

## 🎯 核心功能测试清单

- [ ] Firebase 匿名登录成功
- [ ] 使用兑换码后数据保存到云端
- [ ] 跨设备登录时兑换码状态同步
- [ ] 本地数据成功迁移到云端
- [ ] GM 模式解锁状态云端同步
- [ ] 支持者功能解锁状态云端同步

## 🔧 常见问题

### Q1: Firebase 认证失败？

**A:** 确保在 Firebase Console 中启用了匿名登录：
1. Authentication → 登录方法
2. 启用"匿名"

### Q2: Firestore 权限被拒绝？

**A:** 检查安全规则：
1. Firestore Database → 规则
2. 确保规则中有 `allow read, write: if request.auth != null;`
3. 点击"发布"

### Q3: 数据没有同步？

**A:** 检查以下几点：
1. 网络连接是否正常
2. Firebase Auth 是否登录成功
3. 查看 Logcat 中的错误信息
4. 检查 Firestore 安全规则

### Q4: 如何查看详细日志？

**A:** 在 Android Studio 的 Logcat 中过滤：
- `Firebase` - Firebase 相关日志
- `RedeemCode` - 兑换码相关日志
- `Firestore` - Firestore 操作日志

## 💡 进阶功能

### 1. 离线支持

Firestore 默认支持离线缓存，无需额外配置。

### 2. 实时同步

```kotlin
// 监听兑换码变化
val db = FirebaseFirestore.getInstance()
db.collection("user_redeem_codes")
    .document(userId)
    .addSnapshotListener { snapshot, error ->
        if (snapshot != null && snapshot.exists()) {
            // 数据更新了
            val data = snapshot.toObject(UserRedeemData::class.java)
            // 更新 UI
        }
    }
```

### 3. 批量操作

```kotlin
// 批量验证兑换码
suspend fun checkMultipleCodes(userId: String, codes: List<String>) {
    codes.forEach { code ->
        val isUsed = FirebaseRedeemCodeManager.isCodeUsedByUser(userId, code)
        Log.d("Batch", "$code: ${if (isUsed) "已使用" else "未使用"}")
    }
}
```

## 📚 相关文档

- 📄 `Firebase兑换码系统实现说明.md` - 完整文档
- 💻 `Firebase兑换码集成示例.kt` - 代码示例
- 🔒 `firestore.rules` - 安全规则

## ✨ 优势总结

✅ **跨设备同步** - 同一账号在任意设备使用
✅ **防作弊** - 数据存储在云端，无法篡改
✅ **实时验证** - 确保兑换码全局唯一
✅ **数据备份** - 永不丢失
✅ **向后兼容** - 支持从本地迁移
✅ **离线支持** - Firestore 自动缓存

## 🎉 完成！

现在您的兑换码系统已经升级为云端同步方案，享受更安全、更强大的功能吧！

如有问题，请查看详细文档或检查 Firebase Console 的日志。

