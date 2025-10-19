# TapTap合规认证接入完成说明

## 接入版本
- **TapSDK版本**: 4.8.2
- **依赖模块**: tap-compliance

## 核心功能
根据TapTap官方文档，已成功接入以下功能：
1. ✅ 实名认证和防沉迷
2. ✅ 年龄段获取
3. ✅ 剩余游戏时长获取
4. ✅ 充值额度限制检查
5. ✅ 充值金额上报
6. ✅ 时段限制（未成年人22:00-8:00）
7. ✅ 时长限制
8. ✅ 年龄限制

## 核心文件

### 1. TapComplianceManager.kt
**路径**: `app/src/main/java/com/example/yjcy/taptap/TapComplianceManager.kt`

**功能**：
- 封装TapTap合规认证API
- 提供合规认证启动/退出
- 充值限制检查和上报
- 获取玩家年龄段和剩余时长

**主要方法**：
```kotlin
// 开始合规认证（登录后自动调用）
TapComplianceManager.startup(activity, userId)

// 退出合规认证
TapComplianceManager.exit()

// 检查充值限制（金额单位：分，100元=10000分）
val result = TapComplianceManager.checkPaymentLimit(activity, amount)

// 上报充值金额
TapComplianceManager.submitPayment(amount)

// 获取玩家年龄段（-1表示未知）
val ageRange = TapComplianceManager.getAgeRange()

// 获取剩余时长（秒）
val remainingTime = TapComplianceManager.getRemainingTime()
```

### 2. YjcyApplication.kt
**路径**: `app/src/main/java/com/example/yjcy/YjcyApplication.kt`

**功能**：
- 初始化TapSDK时配置合规认证选项
- 注册全局合规认证回调
- 处理各种认证事件

**配置项**：
```kotlin
val complianceOptions = TapTapComplianceOptions(
    showSwitchAccount = true,  // 显示切换账号按钮
    useAgeRange = true          // 获取真实年龄段信息
)
```

**回调事件**：
- `LOGIN_SUCCESS`: 认证成功
- `EXITED`: 用户退出认证
- `SWITCH_ACCOUNT`: 用户切换账号
- `PERIOD_RESTRICT`: 时段限制（未成年人22:00-8:00禁止游戏）
- `DURATION_LIMIT`: 时长限制
- `AGE_LIMIT`: 年龄限制
- `INVALID_CLIENT_OR_NETWORK_ERROR`: 客户端错误或网络错误
- `REAL_NAME_STOP`: 实名认证已停止

### 3. TapLoginScreen.kt
**路径**: `app/src/main/java/com/example/yjcy/ui/taptap/TapLoginScreen.kt`

**功能**：
- 登录成功后自动触发合规认证
- 显示合规认证信息（年龄段、剩余时长）
- 提供刷新合规信息按钮
- 登出时自动退出合规认证

**UI展示**：
- 登录成功后显示用户信息卡片
- 显示合规认证信息卡片（年龄段和剩余时长）
- 提供刷新按钮实时更新合规信息

## 使用流程

### 标准流程
1. **用户登录**: 在TapLoginScreen点击"使用TapTap登录"
2. **自动认证**: 登录成功后，系统自动调用 `TapComplianceManager.startup()`
3. **弹窗显示**: TapSDK自动弹出实名认证/防沉迷界面
4. **获取信息**: 认证完成后，自动获取年龄段和剩余时长
5. **显示状态**: UI显示合规认证信息

### 充值流程
```kotlin
// 1. 检查充值限制
val amount = 10000 // 100元 = 10000分
when (val result = TapComplianceManager.checkPaymentLimit(activity, amount)) {
    is PaymentCheckResult.Allowed -> {
        // 允许充值，继续支付流程
        // 支付成功后上报
        TapComplianceManager.submitPayment(amount)
    }
    is PaymentCheckResult.Restricted -> {
        // 充值受限，SDK会自动弹窗提示
    }
    is PaymentCheckResult.Error -> {
        // 检查失败，可以重试
    }
    PaymentCheckResult.Cancelled -> {
        // 用户取消
    }
}
```

## 重要注意事项

### 1. 用户ID要求
- 使用TapTap的 `unionId` 作为用户唯一标识
- 字符串长度不大于160
- 只能包含：数字、大小写字母、下划线(_)、短横(-)、加号(+)、正斜线(/)、等号(=)、英文句号(.)、英文逗号(,)、英文冒号(:)

### 2. 充值金额单位
- **单位为分**，不是元
- 例如：100元 = 10000分

### 3. 年龄段返回-1的原因
- 用户未完成实名认证
- 初始化时 `useAgeRange` 设置为 `false`
- 游戏无版号且在TapPlay中运行
- 用户首次进入游戏时未开启获取年龄段权限

### 4. 开发者中心配置
- 需要在TapTap开发者中心开通合规认证服务
- 选择"已有版号"或"暂无版号"方案
- 配置适龄限制（可选）

## 测试建议
1. 测试正常登录流程，验证合规认证弹窗
2. 测试不同年龄段账号的限制情况
3. 测试充值限制检查功能
4. 测试时段限制（未成年人22:00-8:00）
5. 测试时长限制触发情况
6. 测试切换账号功能

## Lint警告说明
代码中存在一些非关键的lint警告（deprecated icons等），这些不影响功能使用，可以后续优化。

## 依赖变更
在 `app/build.gradle.kts` 中已添加：
```kotlin
implementation(libs.tap.compliance)
```

在 `gradle/libs.versions.toml` 中已包含：
```toml
tap-compliance = { group = "com.taptap.sdk", name = "tap-compliance", version.ref = "tapSdk" }
```

## 参考文档
- TapTap官方文档: https://developer.taptap.cn/docs/sdk/anti-addiction/guide/
- TapSDK版本: 4.8.2
