# LeanCloud 兑换码接入指南

## 概述

本指南介绍如何在游戏中接入LeanCloud兑换码系统，实现与管理后台生成的兑换码对接。

## 架构说明

```
游戏管理后台（生成兑换码）
    ↓
LeanCloud 云数据库（存储兑换码和使用记录）
    ↓
游戏客户端（验证和使用兑换码）
```

## 数据表结构

### 1. RedeemCodes 表（兑换码信息）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | String | 兑换码（唯一） |
| type | String | 类型：gm/supporter |
| batchId | String | 批次ID（可选） |
| createdAt | Date | 创建时间（自动） |

### 2. UserRedeemRecords 表（用户兑换记录）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | String | 用户ID（TapTap unionId） |
| code | String | 兑换码 |
| type | String | 类型：gm/supporter |
| redeemedAt | Date | 兑换时间 |

## 配置步骤

### 步骤 1：配置 LeanCloud

1. 访问 [LeanCloud 控制台](https://console.leancloud.cn/)
2. 创建应用（如果还没有）
3. 获取应用凭证：
   - App ID
   - App Key
   - 服务器地址（可选，国内节点可留空）

4. 打开 `app/src/main/java/com/example/yjcy/config/LeanCloudConfig.kt`

5. 替换配置信息：

```kotlin
private const val APP_ID = "your_app_id_here"
private const val APP_KEY = "your_app_key_here"
private const val SERVER_URL = "https://your_server.api.lncldglobal.com"  // 国际版
// 或者使用国内节点
private const val USE_CN_NODE = true  // 使用华北/华东节点
```

### 步骤 2：在 LeanCloud 控制台创建数据表

1. 进入 **数据存储 → 结构化数据**
2. 创建 `RedeemCodes` 表（如果管理后台已创建，跳过此步）
3. 创建 `UserRedeemRecords` 表

### 步骤 3：设置权限（重要！）

在 LeanCloud 控制台为每个表设置权限：

#### RedeemCodes 表权限：
- **查询（find）**：所有用户
- **新增（create）**：仅管理员（由管理后台创建）
- **修改（update）**：禁止
- **删除（delete）**：仅管理员

#### UserRedeemRecords 表权限：
- **查询（find）**：所有用户（仅能查询自己的记录）
- **新增（create）**：所有用户
- **修改（update）**：禁止
- **删除（delete）**：仅管理员

## 在游戏中使用

### 方式 1：使用完整兑换流程（推荐）

```kotlin
import com.example.yjcy.utils.LeanCloudRedeemCodeManager
import kotlinx.coroutines.launch

// 在 Composable 或 Activity 中
val coroutineScope = rememberCoroutineScope()

Button(
    onClick = {
        val code = redeemCodeInput.uppercase()
        val userId = TapLoginManager.getCurrentAccount()?.unionId
        
        if (userId.isNullOrBlank()) {
            // 提示用户需要登录
            return@Button
        }
        
        coroutineScope.launch {
            when (val result = LeanCloudRedeemCodeManager.redeemCode(userId, code)) {
                is LeanCloudRedeemCodeManager.RedeemResult.Success -> {
                    // 兑换成功！
                    when (result.type) {
                        "gm" -> {
                            // 解锁GM功能
                            isGMUnlocked = true
                            showSuccessMessage = "🎮 GM功能已解锁！"
                        }
                        "supporter" -> {
                            // 解锁支持者功能
                            isSupporterUnlocked = true
                            showSuccessMessage = "💎 支持者功能已解锁！"
                        }
                    }
                }
                LeanCloudRedeemCodeManager.RedeemResult.CodeNotFound -> {
                    showErrorMessage = "❌ 兑换码不存在或无效"
                }
                LeanCloudRedeemCodeManager.RedeemResult.AlreadyUsed -> {
                    showErrorMessage = "❌ 您已使用过该兑换码"
                }
                LeanCloudRedeemCodeManager.RedeemResult.RecordFailed -> {
                    showErrorMessage = "❌ 记录兑换失败，请重试"
                }
                LeanCloudRedeemCodeManager.RedeemResult.NetworkError -> {
                    showErrorMessage = "❌ 网络错误，请检查网络连接"
                }
            }
        }
    }
) {
    Text("兑换")
}
```

### 方式 2：分步验证（灵活控制）

```kotlin
// 1. 验证兑换码是否存在
val codeData = LeanCloudRedeemCodeManager.validateRedeemCode(code)
if (codeData == null) {
    // 兑换码不存在
    return
}

// 2. 检查是否已使用
val hasUsed = LeanCloudRedeemCodeManager.hasUserUsedCode(userId, code)
if (hasUsed) {
    // 已使用过
    return
}

// 3. 执行游戏逻辑（解锁功能、发放奖励等）
when (codeData.type) {
    "gm" -> unlockGMFeature()
    "supporter" -> unlockSupporterFeature()
}

// 4. 记录使用
val recorded = LeanCloudRedeemCodeManager.recordUserRedeem(userId, code, codeData.type)
if (!recorded) {
    // 记录失败
}
```

### 方式 3：启动时同步解锁状态

```kotlin
// 在 LaunchedEffect 中检查解锁状态
LaunchedEffect(userId) {
    if (userId != null) {
        // 检查GM功能解锁状态
        val gmUnlocked = LeanCloudRedeemCodeManager.isGMUnlocked(userId)
        isGMUnlocked = gmUnlocked
        
        // 检查支持者功能解锁状态
        val supporterUnlocked = LeanCloudRedeemCodeManager.isSupporterUnlocked(userId)
        isSupporterUnlocked = supporterUnlocked
        
        Log.d("RedeemCode", "GM解锁: $gmUnlocked, 支持者解锁: $supporterUnlocked")
    }
}
```

## 完整集成示例（MainActivity）

在你的 `MainActivity.kt` 中添加兑换码处理逻辑：

```kotlin
// 在 GameScreen Composable 中
LaunchedEffect(userId) {
    if (userId != null) {
        // 同步LeanCloud的解锁状态
        val gmUnlocked = LeanCloudRedeemCodeManager.isGMUnlocked(userId)
        val supporterUnlocked = LeanCloudRedeemCodeManager.isSupporterUnlocked(userId)
        
        isGMUnlocked = gmUnlocked
        isSupporterUnlocked = supporterUnlocked
        
        Log.d("MainActivity", "LeanCloud同步: GM=$gmUnlocked, 支持者=$supporterUnlocked")
    }
}

// 在兑换按钮的 onClick 中
Button(
    onClick = {
        val codeUpper = redeemCode.uppercase().trim()
        val currentUserId = userId
        
        if (currentUserId.isNullOrBlank()) {
            showRedeemError = true
            redeemErrorMessage = "❌ 请先登录"
            return@Button
        }
        
        if (codeUpper.isBlank()) {
            showRedeemError = true
            redeemErrorMessage = "❌ 请输入兑换码"
            return@Button
        }
        
        // 使用协程处理异步操作
        coroutineScope.launch {
            try {
                Log.d("RedeemCode", "开始兑换: $codeUpper")
                
                when (val result = LeanCloudRedeemCodeManager.redeemCode(currentUserId, codeUpper)) {
                    is LeanCloudRedeemCodeManager.RedeemResult.Success -> {
                        // 兑换成功
                        when (result.type) {
                            "gm" -> {
                                isGMUnlocked = true
                                redeemSuccessMessage = "🎮 GM功能已解锁！"
                            }
                            "supporter" -> {
                                isSupporterUnlocked = true
                                redeemSuccessMessage = "💎 支持者功能已解锁！"
                            }
                            else -> {
                                redeemSuccessMessage = "✅ 兑换成功！"
                            }
                        }
                        showRedeemSuccessDialog = true
                        redeemCode = ""
                    }
                    
                    LeanCloudRedeemCodeManager.RedeemResult.CodeNotFound -> {
                        showRedeemError = true
                        redeemErrorMessage = "❌ 兑换码不存在或无效"
                    }
                    
                    LeanCloudRedeemCodeManager.RedeemResult.AlreadyUsed -> {
                        showRedeemError = true
                        redeemErrorMessage = "❌ 您已使用过该兑换码"
                    }
                    
                    LeanCloudRedeemCodeManager.RedeemResult.RecordFailed -> {
                        showRedeemError = true
                        redeemErrorMessage = "❌ 记录兑换失败，请重试"
                    }
                    
                    LeanCloudRedeemCodeManager.RedeemResult.NetworkError -> {
                        showRedeemError = true
                        redeemErrorMessage = "❌ 网络错误，请检查网络连接"
                    }
                }
                
            } catch (e: Exception) {
                Log.e("RedeemCode", "兑换异常", e)
                showRedeemError = true
                redeemErrorMessage = "❌ 兑换失败：${e.message}"
            }
        }
    }
) {
    Text("兑换")
}
```

## 测试步骤

### 1. 在管理后台生成测试兑换码

在你的游戏管理后台生成几个测试兑换码：
- GM兑换码 x 2
- 支持者兑换码 x 2

### 2. 检查 LeanCloud 数据

1. 打开 LeanCloud 控制台
2. 进入 **数据存储 → 结构化数据 → RedeemCodes**
3. 确认兑换码已生成

### 3. 在游戏中测试

1. 启动游戏并登录 TapTap 账号
2. 进入兑换码界面
3. 输入测试兑换码
4. 验证兑换流程：
   - ✅ 首次兑换：成功，功能解锁
   - ✅ 重复兑换：提示已使用
   - ✅ 错误码：提示不存在
   - ✅ 重启游戏：解锁状态保持

### 4. 检查兑换记录

1. 打开 LeanCloud 控制台
2. 进入 **UserRedeemRecords** 表
3. 确认兑换记录已创建
4. 验证字段：userId、code、type、redeemedAt

## API 参考

### LeanCloudRedeemCodeManager 方法

#### 完整兑换流程（推荐）

```kotlin
suspend fun redeemCode(userId: String, code: String): RedeemResult
```

一步完成验证、检查、记录的完整流程。

#### 分步方法

```kotlin
// 验证兑换码是否存在
suspend fun validateRedeemCode(code: String): RedeemCodeData?

// 检查用户是否已使用
suspend fun hasUserUsedCode(userId: String, code: String): Boolean

// 记录使用
suspend fun recordUserRedeem(userId: String, code: String, type: String): Boolean

// 检查GM解锁状态
suspend fun isGMUnlocked(userId: String): Boolean

// 检查支持者解锁状态
suspend fun isSupporterUnlocked(userId: String): Boolean

// 获取用户所有兑换记录
suspend fun getUserRedeemRecords(userId: String): List<UserRedeemRecord>
```

## 网络优化

- 所有请求都有 **10秒超时**保护
- 使用 `withContext(Dispatchers.IO)` 避免阻塞主线程
- 错误时自动降级处理

## 安全建议

1. **权限控制**：在 LeanCloud 控制台严格设置表权限
2. **数据验证**：兑换前验证用户登录状态
3. **防重放**：使用 UserRedeemRecords 表防止重复兑换
4. **日志记录**：所有操作都有详细日志，便于追踪问题

## 常见问题

### Q1: 兑换时提示"网络错误"？

**A**: 检查以下几点：
1. 确认已配置正确的 APP_ID 和 APP_KEY
2. 检查设备网络连接
3. 查看 Logcat 日志获取详细错误信息
4. 国内用户确保 `USE_CN_NODE = true`

### Q2: 兑换成功但重启后失效？

**A**: 需要在启动时同步解锁状态：

```kotlin
LaunchedEffect(userId) {
    if (userId != null) {
        isGMUnlocked = LeanCloudRedeemCodeManager.isGMUnlocked(userId)
        isSupporterUnlocked = LeanCloudRedeemCodeManager.isSupporterUnlocked(userId)
    }
}
```

### Q3: 如何查看兑换记录？

**A**: 使用 `getUserRedeemRecords` 方法：

```kotlin
val records = LeanCloudRedeemCodeManager.getUserRedeemRecords(userId)
records.forEach { record ->
    Log.d("RedeemRecord", "兑换码: ${record.code}, 类型: ${record.type}, 时间: ${record.redeemedAt}")
}
```

### Q4: 支持离线兑换吗？

**A**: 不支持。兑换码验证需要联网访问 LeanCloud 云端数据，确保防作弊和跨设备同步。

## 与 Firebase 兑换码的对比

| 特性 | LeanCloud | Firebase |
|------|-----------|----------|
| 管理后台 | ✅ 独立管理后台 | ❌ 需自行实现 |
| 国内访问 | ✅ 稳定快速 | ⚠️ 需代理或延迟优化 |
| 数据结构 | ✅ 灵活的关系型数据 | ✅ NoSQL文档型 |
| 实时同步 | ✅ 支持 | ✅ 支持 |
| 成本 | 💰 按请求计费 | 💰 按流量计费 |

## 下一步

1. ✅ 配置 LeanCloud 凭证
2. ✅ 在管理后台创建数据表
3. ✅ 设置表权限
4. ✅ 在游戏中集成兑换逻辑
5. ✅ 生成测试兑换码并测试
6. ✅ 发布正式兑换码给玩家

## 相关文件

- 配置文件：`app/src/main/java/com/example/yjcy/config/LeanCloudConfig.kt`
- 管理器：`app/src/main/java/com/example/yjcy/utils/LeanCloudRedeemCodeManager.kt`
- 应用初始化：`app/src/main/java/com/example/yjcy/YjcyApplication.kt`

## 技术支持

- LeanCloud 文档：https://docs.leancloud.cn/
- LeanCloud SDK 版本：9.0.0
- 问题反馈：请在项目 Issues 中提交
