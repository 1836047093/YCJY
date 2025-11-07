# LeanCloud 兑换码配置清单

## ⚡ 快速解决依赖下载慢的问题

### Maven仓库配置（必须！）

在 `settings.gradle.kts` 中添加 LeanCloud 官方 Maven 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        // LeanCloud Maven仓库（国内服务器，速度快）⭐
        maven { url = uri("https://mvn.leancloud.cn/nexus/content/repositories/public") }
        
        // 其他仓库...
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
        google()
    }
}
```

**为什么需要？**
- LeanCloud 依赖默认从 Maven Central（国外）下载，国内很慢
- 添加 LeanCloud 官方仓库后，从国内服务器下载，速度快得多

✅ **此配置已添加，如果下载卡住请重新同步项目！**

---

## ✅ 配置检查清单

### 1. LeanCloud 控制台配置

- [ ] 访问 https://console.leancloud.cn/ 并登录
- [ ] 创建应用（或使用现有应用）
- [ ] 获取应用凭证：
  - [ ] App ID
  - [ ] App Key
  - [ ] 服务器地址（可选）

### 2. 代码配置

#### 2.1 配置 LeanCloudConfig.kt

打开文件：`app/src/main/java/com/example/yjcy/config/LeanCloudConfig.kt`

替换以下内容：

```kotlin
private const val APP_ID = "your_app_id_here"        // 替换成你的
private const val APP_KEY = "your_app_key_here"      // 替换成你的
private const val USE_CN_NODE = true                 // 国内用户保持 true
```

#### 2.2 确认依赖已添加

检查 `app/build.gradle.kts` 中是否包含：

```kotlin
implementation(libs.leancloud.storage)
```

检查 `gradle/libs.versions.toml` 中是否包含：

```kotlin
[versions]
leancloud = "9.0.0"

[libraries]
leancloud-storage = { group = "cn.leancloud", name = "storage-android", version.ref = "leancloud" }
```

✅ 这些依赖已经存在，无需修改。

### 3. LeanCloud 控制台数据表配置

#### 3.1 创建数据表（如果管理后台未创建）

1. 进入 **数据存储 → 结构化数据**
2. 点击 **创建 Class**
3. 创建以下两个表：

**表 1: RedeemCode** ⭐ **首次绑定机制**
- Class 名称：`RedeemCode`（不带s）
- 权限：限制性（下一步设置）
- **必需字段**：
  - `code`（String）：兑换码，必填，唯一
  - `type`（String）：类型，必填，可选值：`gm`、`supporter`、`special`
  - `batchId`（String）：批次ID，可选
  - **`usedBy`（String）**：绑定的用户ID，首次使用时自动设置
  - **`isUsed`（Boolean）**：是否已被使用/绑定，默认值 `false`

**表 2: UserRedeemRecords** ⚠️ **必须创建此表，否则兑换功能无法使用！**
- Class 名称：`UserRedeemRecords`（注意大小写）
- 权限：限制性（下一步设置）
- **必需字段**：
  - `userId`（String）：用户ID（TapTap unionId），必填
  - `code`（String）：兑换码，必填
  - `type`（String）：类型，必填
  - `redeemedAt`（Date）：兑换时间，自动生成
  
⚠️ **常见错误**：如果不创建此表，会出现 `Class or object doesn't exists` 错误，导致兑换码验证成功但无法保存使用记录！

#### 3.2 设置表权限（重要！）

**RedeemCode 表权限：**

| 操作 | 权限 |
|------|------|
| find | ✅ 所有用户 |
| get | ✅ 所有用户 |
| create | ❌ 无（仅管理后台） |
| update | ❌ 无 |
| delete | ❌ 无 |

**UserRedeemRecords 表权限：**

| 操作 | 权限 |
|------|------|
| find | ✅ 所有用户 |
| get | ✅ 所有用户 |
| create | ✅ 所有用户 |
| update | ❌ 无 |
| delete | ❌ 无 |

#### 3.3 添加索引（可选，提升性能）

**RedeemCode 表索引：**
- 字段：`code`，类型：升序

**UserRedeemRecords 表索引：**
- 字段：`userId`，类型：升序
- 字段：`code`，类型：升序
- 复合索引：`userId + code`

#### 3.4 添加测试兑换码数据 ⭐

在 LeanCloud 控制台中手动添加测试兑换码：

1. 进入 **数据存储 → 结构化数据 → RedeemCode 表**
2. 点击 **添加行**
3. 填写以下字段：

**示例 1：GM 兑换码**
```
code: D9KV-SZJ6-F9ZD
type: gm
batchId: (可选，留空或填 test_batch_001)
```

**示例 2：支持者兑换码**
```
code: SUPPORTER-2025-001
type: supporter
batchId: (可选)
```

**示例 3：特殊兑换码**
```
code: WELCOME2025
type: special
batchId: (可选)
```

⚠️ **重要提醒**：
- `code` 字段必须填写（兑换码）
- `type` 字段必须填写（`gm`、`supporter` 或 `special`）
- **如果 `type` 字段为空，兑换会失败！**

### 4. 集成到 MainActivity

在 `MainActivity.kt` 中添加兑换码处理逻辑。

#### 4.1 添加导入

```kotlin
import com.example.yjcy.utils.LeanCloudRedeemCodeManager
```

#### 4.2 添加同步逻辑（LaunchedEffect）

在 `GameScreen` 的 `LaunchedEffect(userId)` 中添加：

```kotlin
LaunchedEffect(userId) {
    if (userId != null) {
        // 同步LeanCloud解锁状态
        val gmUnlocked = LeanCloudRedeemCodeManager.isGMUnlocked(userId)
        val supporterUnlocked = LeanCloudRedeemCodeManager.isSupporterUnlocked(userId)
        
        isGMUnlocked = gmUnlocked
        isSupporterUnlocked = supporterUnlocked
        
        Log.d("MainActivity", "LeanCloud同步: GM=$gmUnlocked, 支持者=$supporterUnlocked")
    }
}
```

#### 4.3 修改兑换按钮逻辑

找到现有的兑换按钮 `onClick` 逻辑，替换为：

```kotlin
Button(
    onClick = {
        val codeUpper = redeemCode.uppercase().trim()
        val currentUserId = userId
        
        if (currentUserId.isNullOrBlank()) {
            showRedeemError = true
            return@Button
        }
        
        if (codeUpper.isBlank()) {
            showRedeemError = true
            return@Button
        }
        
        coroutineScope.launch {
            when (val result = LeanCloudRedeemCodeManager.redeemCode(currentUserId, codeUpper)) {
                is LeanCloudRedeemCodeManager.RedeemResult.Success -> {
                    when (result.type) {
                        "gm" -> {
                            isGMUnlocked = true
                            redeemSuccessMessage = "🎮 GM功能已解锁！"
                        }
                        "supporter" -> {
                            isSupporterUnlocked = true
                            redeemSuccessMessage = "💎 支持者功能已解锁！"
                        }
                    }
                    showRedeemSuccessDialog = true
                    redeemCode = ""
                }
                LeanCloudRedeemCodeManager.RedeemResult.CodeNotFound -> {
                    showRedeemError = true
                }
                LeanCloudRedeemCodeManager.RedeemResult.AlreadyUsed -> {
                    showRedeemError = true
                }
                LeanCloudRedeemCodeManager.RedeemResult.RecordFailed -> {
                    showRedeemError = true
                }
                LeanCloudRedeemCodeManager.RedeemResult.NetworkError -> {
                    showRedeemError = true
                }
            }
        }
    }
) {
    Text("兑换")
}
```

### 5. 测试验证

#### 5.1 编译项目

```bash
./gradlew clean build
```

检查是否有编译错误。

#### 5.2 运行游戏

1. 启动游戏
2. 查看 Logcat 日志，搜索 "LeanCloud"
3. 确认初始化成功：`✅ LeanCloud初始化成功`

#### 5.3 测试兑换流程

1. 在管理后台生成测试兑换码
2. 在游戏中登录 TapTap 账号
3. 输入兑换码并点击兑换
4. 验证以下场景：
   - ✅ 正确码：兑换成功
   - ✅ 重复兑换：提示已使用
   - ✅ 错误码：提示不存在
   - ✅ 重启游戏：解锁状态保持

#### 5.4 检查数据

1. 打开 LeanCloud 控制台
2. 进入 **UserRedeemRecords** 表
3. 确认兑换记录已创建

## 🔍 故障排查

### 问题 1: 初始化失败

**日志**：`❌ LeanCloud初始化失败`

**解决**：
1. 检查 APP_ID 和 APP_KEY 是否正确
2. 检查网络连接
3. 国内用户确保 `USE_CN_NODE = true`

### 问题 2: 兑换时提示网络错误

**日志**：`❌ 验证兑换码失败`

**解决**：
1. 检查 LeanCloud 是否初始化成功
2. 检查表权限设置
3. 检查数据表是否存在
4. 查看详细错误日志

### 问题 3: 找不到 LeanCloud 类

**错误**：`Unresolved reference: LCObject`

**解决**：
1. 同步 Gradle：File → Sync Project with Gradle Files
2. 清理重建：Build → Clean Project → Rebuild Project
3. 确认依赖版本正确

### 问题 4: 兑换成功但重启后失效

**原因**：未添加同步逻辑

**解决**：在 `LaunchedEffect(userId)` 中添加解锁状态同步代码（见 4.2）

## 📝 配置完成检查

完成以上所有步骤后，请确认：

- [x] LeanCloud SDK 依赖已添加
- [x] LeanCloudConfig.kt 已配置凭证
- [x] YjcyApplication.kt 已初始化 LeanCloud
- [x] LeanCloud 控制台已创建数据表
- [x] 数据表权限已正确设置
- [x] MainActivity.kt 已添加兑换逻辑
- [x] 已测试兑换流程
- [x] 已验证数据记录

## 🎉 配置完成！

现在你的游戏已成功接入 LeanCloud 兑换码系统，可以开始生成和分发兑换码了！

## 📚 相关文档

- [LeanCloud兑换码接入指南.md](./LeanCloud兑换码接入指南.md) - 详细的API文档和使用示例
- [LeanCloud官方文档](https://docs.leancloud.cn/) - LeanCloud完整文档

## 💡 提示

- 生产环境建议关闭 LeanCloud 日志：`AppConfiguration.setLogLevel(AppConfiguration.LogLevel.OFF)`
- 定期备份 LeanCloud 数据
- 监控 LeanCloud 用量，避免超出免费额度

## 🔐 首次绑定机制说明

**什么是首次绑定？**

兑换码在首次使用时会绑定到使用者的账号，只有该账号可以无限次使用。

**工作流程：**

1. **用户A首次使用兑换码** `5FYT-FHY8-MV8M`：
   - 检查 `RedeemCode` 表，发现 `usedBy` 为空
   - 绑定到用户A（更新 `usedBy = userId_A`, `isUsed = true`）
   - 解锁功能 ✅

2. **用户A换设备/重装游戏再次使用**：
   - 检查 `RedeemCode` 表，发现 `usedBy = userId_A`（匹配）
   - 直接解锁功能 ✅（换设备也能用）

3. **用户B尝试使用同一兑换码**：
   - 检查 `RedeemCode` 表，发现 `usedBy = userId_A`（不匹配）
   - 提示"该兑换码已被其他用户使用" ❌

**优势：**

- ✅ 防止兑换码被多人共享
- ✅ 用户换设备/重装游戏不影响使用
- ✅ 数据存储在云端，永久有效
- ✅ 每个兑换码只能绑定一个用户

**数据库字段：**

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| code | String | 兑换码 | - |
| type | String | 类型（gm/supporter） | - |
| **usedBy** | **String** | **绑定的用户ID** | **空** |
| **isUsed** | **Boolean** | **是否已绑定** | **false** |
