# Firebase 兑换码快速测试步骤

## 🚀 5分钟快速测试

### 第1步：确认配置（1分钟）

✅ **检查清单：**
- [ ] `google-services.json` 已在 `app/` 目录
- [ ] 项目已同步 Gradle（Sync Now）
- [ ] Firestore Database 已创建

### 第2步：配置 Firestore 规则（1分钟）

1. 打开 [Firebase Console](https://console.firebase.google.com/)
2. 选择项目 → **Firestore Database** → **规则**
3. 复制 `firestore.rules` 的内容并粘贴
4. 点击 **发布**

**当前规则（测试模式）：**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;  // 允许所有访问
    }
  }
}
```

⚠️ **注意：** 当前是测试模式，**不需要** Firebase Authentication。

### 第3步：运行应用并测试（3分钟）

#### 3.1 启动应用

1. 运行应用
2. 同意隐私政策
3. 登录 TapTap 账号（获取 userId）

#### 3.2 测试兑换码

1. 进入游戏主界面
2. 找到兑换码输入框
3. 输入测试兑换码：
   - `SUPPORTER001` - 支持者兑换码
   - `PROGM` - GM模式兑换码

#### 3.3 验证结果

**在应用中：**
- ✅ 显示"兑换成功"提示
- ✅ 功能已解锁（支持者功能/GM模式）

**在 Firebase Console 中：**
1. 进入 **Firestore Database**
2. 查看 `user_redeem_codes` 集合
3. 应该能看到新文档（文档ID为 TapTap userId）

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

### 第4步：查看日志（可选）

在 Android Studio 的 Logcat 中过滤：
```
tag:FirebaseRedeemCodeManager
```

**成功日志示例：**
```
FirebaseRedeemCodeManager: 标记兑换码成功: userId=xxx, code=SUPPORTER001, type=supporter
```

## ✅ 测试成功标志

- [ ] 兑换码可以正常使用
- [ ] Firestore 中有数据记录
- [ ] 功能正确解锁
- [ ] 重复使用会被拒绝

## ❌ 如果测试失败

### 问题1：兑换码使用失败

**检查：**
1. Firestore 规则是否已发布
2. 网络连接是否正常
3. `google-services.json` 是否正确

**查看日志：**
```
tag:FirebaseRedeemCodeManager
```

### 问题2：数据未出现在 Firestore

**检查：**
1. Firestore Database 是否已创建
2. 规则是否允许写入
3. 网络连接是否正常

**解决方法：**
- 检查 Firestore 规则
- 检查网络连接
- 查看 Logcat 错误日志

## 📝 测试用例

### 用例1：支持者兑换码
- **输入：** `SUPPORTER001`
- **预期：** 兑换成功，支持者功能解锁

### 用例2：GM模式兑换码
- **输入：** `PROGM`
- **预期：** 兑换成功，GM模式解锁

### 用例3：重复使用
- **输入：** 已使用过的兑换码
- **预期：** 提示已使用，不会重复标记

### 用例4：无效兑换码
- **输入：** `INVALID123`
- **预期：** 提示兑换码无效

## 🎯 下一步

测试成功后，可以：
1. 测试跨设备同步（使用同一 TapTap 账号）
2. 测试数据迁移（从本地迁移到云端）
3. 查看详细测试指南：`Firebase兑换码功能测试指南.md`

## 📚 相关文档

- `Firebase兑换码功能测试指南.md` - 详细测试指南
- `Firebase兑换码快速开始指南.md` - 快速开始指南
- `Firebase兑换码系统实现说明.md` - 实现文档

