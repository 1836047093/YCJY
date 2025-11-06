# Firebase 兑换码功能测试指南

## 📋 测试前准备

### 1. 确认 Firebase 配置已完成

✅ **检查项：**
- [ ] `google-services.json` 已放置在 `app/` 目录下
- [ ] `build.gradle.kts` 中已添加 Firebase 依赖
- [ ] 项目已同步 Gradle

### 2. 配置 Firestore 安全规则

当前 `firestore.rules` 已设置为**测试模式**（允许所有访问），可以直接测试。

**在 Firebase Console 中配置：**

1. 打开 [Firebase Console](https://console.firebase.google.com/)
2. 选择您的项目
3. 进入 **Firestore Database**
4. 点击 **规则** 标签
5. 复制 `firestore.rules` 文件的内容并粘贴
6. 点击 **发布**

**当前测试模式规则：**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;  // 允许所有访问（仅测试）
    }
  }
}
```

⚠️ **注意：** 测试模式允许所有访问，无需 Firebase Authentication。生产环境请使用严格规则。

### 3. （可选）启用 Firebase Authentication

如果需要测试认证功能：

1. 在 Firebase Console 中进入 **Authentication**
2. 点击 **登录方法** 标签
3. 启用 **匿名登录**（用于开发测试）

## 🧪 测试步骤

### 测试 1：基本功能测试

#### 1.1 启动应用并登录 TapTap

1. 运行应用
2. 同意隐私政策
3. 登录 TapTap 账号（获取 userId）

#### 1.2 测试兑换码输入

1. 进入游戏主界面
2. 找到兑换码输入框（通常在设置或主菜单中）
3. 输入测试兑换码：
   - **支持者兑换码：** `SUPPORTER001` 到 `SUPPORTER150`
   - **GM模式兑换码：** `PROGM`
   - **特殊兑换码：** `YCJY2025`（如果已实现）

#### 1.3 验证兑换结果

**预期结果：**
- ✅ 兑换码验证成功
- ✅ 显示成功提示
- ✅ 功能已解锁（如支持者功能、GM模式等）

**检查日志：**
在 Android Studio 的 Logcat 中过滤以下标签：
- `FirebaseRedeemCodeManager` - 兑换码操作日志
- `Firebase` - Firebase 相关日志
- `RedeemCode` - 兑换码相关日志

**成功日志示例：**
```
FirebaseRedeemCodeManager: 标记兑换码成功: userId=xxx, code=SUPPORTER001, type=supporter
```

### 测试 2：云端数据验证

#### 2.1 在 Firebase Console 中查看数据

1. 打开 [Firebase Console](https://console.firebase.google.com/)
2. 选择您的项目
3. 进入 **Firestore Database**
4. 查看 `user_redeem_codes` 集合

**预期数据结构：**
```json
{
  "userId": "tap_union_id_xxx",
  "usedCodes": ["SUPPORTER001"],
  "gmModeUnlocked": false,
  "supporterUnlocked": true,
  "lastUpdated": "2025-01-07T10:30:00Z"
}
```

#### 2.2 验证数据同步

1. 使用同一个 TapTap 账号在不同设备上登录
2. 检查兑换码状态是否同步

**预期结果：**
- ✅ 已使用的兑换码状态同步
- ✅ 解锁的功能状态同步

### 测试 3：重复兑换码测试

#### 3.1 尝试重复使用同一兑换码

1. 使用已使用过的兑换码再次兑换
2. 观察应用行为

**预期结果：**
- ✅ 提示兑换码已使用
- ✅ 不会重复标记
- ✅ 不会重复解锁功能

**检查日志：**
```
FirebaseRedeemCodeManager: 检查兑换码: userId=xxx, code=SUPPORTER001, isUsed=true
```

### 测试 4：数据迁移测试

#### 4.1 测试本地数据迁移到云端

1. 清除应用数据（模拟新安装）
2. 使用本地存档（包含已使用的兑换码）
3. 登录 TapTap 账号
4. 检查数据是否自动迁移

**预期结果：**
- ✅ 本地兑换码数据自动迁移到云端
- ✅ 迁移后功能状态正确

**检查日志：**
```
FirebaseRedeemCodeManager: 本地数据迁移成功: userId=xxx, codes=2个
```

### 测试 5：网络异常测试

#### 5.1 测试离线情况

1. 关闭设备网络
2. 尝试使用兑换码

**预期结果：**
- ⚠️ Firestore 支持离线缓存
- ⚠️ 如果之前已同步，离线时仍可读取
- ⚠️ 离线时写入会失败，但会缓存，网络恢复后自动同步

#### 5.2 测试网络恢复

1. 恢复网络连接
2. 检查数据是否自动同步

**预期结果：**
- ✅ 离线时的操作自动同步到云端

## 📊 测试检查清单

### 功能测试

- [ ] 支持者兑换码（SUPPORTER001-150）可以正常使用
- [ ] GM模式兑换码（PROGM）可以正常使用
- [ ] 特殊兑换码（如YCJY2025）可以正常使用
- [ ] 重复使用同一兑换码会被拒绝
- [ ] 兑换成功后功能正确解锁
- [ ] 兑换成功后显示正确的提示信息

### 云端同步测试

- [ ] 兑换码使用记录保存到 Firestore
- [ ] 数据在 Firebase Console 中可见
- [ ] 同一账号在不同设备上状态同步
- [ ] 本地数据成功迁移到云端

### 错误处理测试

- [ ] 无效兑换码被正确拒绝
- [ ] 网络错误时有适当的提示
- [ ] 用户ID为空时正确处理
- [ ] 异常情况不会导致崩溃

## 🔍 调试技巧

### 1. 查看 Logcat 日志

**过滤标签：**
```
tag:FirebaseRedeemCodeManager
tag:Firebase
tag:RedeemCode
```

**关键日志：**
- `标记兑换码成功` - 兑换码使用成功
- `检查兑换码` - 检查兑换码使用状态
- `数据迁移成功` - 本地数据迁移成功
- `标记兑换码失败` - 兑换码使用失败（需要检查）

### 2. 在 Firebase Console 中查看数据

**路径：** Firestore Database → `user_redeem_codes` 集合

**检查项：**
- 文档ID是否为 TapTap userId
- `usedCodes` 数组是否包含已使用的兑换码
- `gmModeUnlocked` 和 `supporterUnlocked` 状态是否正确
- `lastUpdated` 时间戳是否更新

### 3. 测试代码示例

如果需要手动测试，可以在 MainActivity 中添加测试代码：

```kotlin
// 测试兑换码功能
lifecycleScope.launch {
    val userId = TapLoginManager.getCurrentAccount()?.unionId 
        ?: TapLoginManager.getCurrentAccount()?.openId
    
    if (userId != null) {
        // 测试检查兑换码
        val isUsed = FirebaseRedeemCodeManager.isCodeUsedByUser(userId, "SUPPORTER001")
        Log.d("Test", "兑换码SUPPORTER001已使用: $isUsed")
        
        // 测试标记兑换码
        val success = FirebaseRedeemCodeManager.markCodeAsUsed(
            userId = userId,
            code = "SUPPORTER001",
            codeType = "supporter"
        )
        Log.d("Test", "标记兑换码结果: $success")
        
        // 测试获取用户兑换码列表
        val usedCodes = FirebaseRedeemCodeManager.getUserUsedCodes(userId)
        Log.d("Test", "用户已使用的兑换码: $usedCodes")
        
        // 测试检查GM模式
        val gmUnlocked = FirebaseRedeemCodeManager.isGMModeUnlocked(userId)
        Log.d("Test", "GM模式已解锁: $gmUnlocked")
        
        // 测试检查支持者功能
        val supporterUnlocked = FirebaseRedeemCodeManager.hasUsedSupporterCode(userId)
        Log.d("Test", "支持者功能已解锁: $supporterUnlocked")
    }
}
```

## ⚠️ 常见问题排查

### 问题 1：兑换码使用失败

**可能原因：**
1. Firestore 安全规则未配置
2. 网络连接问题
3. Firebase 项目配置错误

**解决方法：**
1. 检查 Firestore 安全规则是否已发布
2. 检查网络连接
3. 检查 `google-services.json` 是否正确
4. 查看 Logcat 中的错误日志

### 问题 2：数据未同步到云端

**可能原因：**
1. Firestore 安全规则限制
2. 网络问题
3. Firebase Authentication 未初始化（如果使用认证规则）

**解决方法：**
1. 确认 Firestore 规则允许写入
2. 检查网络连接
3. 如果使用认证规则，确保 Firebase Auth 已初始化

### 问题 3：兑换码验证失败

**可能原因：**
1. 兑换码格式错误
2. 兑换码不在有效范围内
3. 代码逻辑问题

**解决方法：**
1. 检查兑换码格式（支持者兑换码：SUPPORTER001-150）
2. 查看 `isValidSupporterCode()` 方法
3. 检查 Logcat 日志

### 问题 4：跨设备不同步

**可能原因：**
1. 使用了不同的 TapTap 账号
2. userId 不一致
3. 数据未正确保存

**解决方法：**
1. 确认使用相同的 TapTap 账号
2. 检查 userId 是否一致（unionId 或 openId）
3. 在 Firebase Console 中检查数据

## 🎯 测试用例示例

### 用例 1：首次使用支持者兑换码

**步骤：**
1. 登录 TapTap 账号（userId: `test_user_123`）
2. 输入兑换码：`SUPPORTER001`
3. 点击兑换

**预期结果：**
- ✅ 兑换成功
- ✅ 支持者功能解锁
- ✅ Firestore 中创建文档：`user_redeem_codes/test_user_123`
- ✅ 文档包含：`usedCodes: ["SUPPORTER001"]`, `supporterUnlocked: true`

### 用例 2：重复使用兑换码

**步骤：**
1. 使用已使用过的兑换码：`SUPPORTER001`
2. 再次尝试兑换

**预期结果：**
- ✅ 提示兑换码已使用
- ✅ 不会重复标记
- ✅ Firestore 数据不变

### 用例 3：使用多个兑换码

**步骤：**
1. 使用 `SUPPORTER001`
2. 使用 `SUPPORTER002`
3. 使用 `PROGM`

**预期结果：**
- ✅ 所有兑换码都成功使用
- ✅ Firestore 文档包含：`usedCodes: ["SUPPORTER001", "SUPPORTER002", "PROGM"]`
- ✅ `supporterUnlocked: true`, `gmModeUnlocked: true`

### 用例 4：跨设备同步

**步骤：**
1. 在设备A上使用 `SUPPORTER001`
2. 在设备B上使用相同 TapTap 账号登录
3. 检查支持者功能状态

**预期结果：**
- ✅ 设备B上支持者功能已解锁
- ✅ 无需重新兑换

## 📝 测试报告模板

```
测试日期：2025-01-07
测试人员：xxx
测试环境：Android Studio / 真机

测试结果：
✅ 基本功能测试：通过
✅ 云端同步测试：通过
✅ 重复兑换码测试：通过
✅ 数据迁移测试：通过
⚠️ 网络异常测试：部分通过（离线写入会失败，但会缓存）

发现的问题：
1. 无

建议：
1. 生产环境需要启用 Firebase Authentication
2. 需要设置严格的 Firestore 安全规则
```

## 🚀 下一步

测试完成后，建议：

1. **启用 Firebase Authentication**（生产环境）
2. **设置严格的 Firestore 安全规则**（生产环境）
3. **添加错误处理和重试机制**
4. **添加用户友好的错误提示**
5. **监控 Firestore 使用量**（避免超出免费额度）

## 📚 相关文档

- `Firebase兑换码快速开始指南.md` - 快速开始指南
- `Firebase兑换码系统实现说明.md` - 详细实现文档
- `Firebase兑换码集成示例.kt` - 代码示例
- `firestore.rules` - Firestore 安全规则

