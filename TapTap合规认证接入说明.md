# TapTap 合规认证接入说明

## 📋 概述

已成功接入TapTap合规认证SDK（防沉迷、实名认证），版本：**4.8.2**

## 🎯 核心功能

### 1. 实名认证
- 验证玩家身份信息
- 符合国家防沉迷政策要求

### 2. 防沉迷系统
- **时段限制**：限制未成年人游戏时段
- **时长限制**：限制每日游戏时长
- **年龄段识别**：区分未成年/成年玩家

### 3. 充值管理
- **充值限制检查**：检查玩家充值额度
- **充值金额上报**：向SDK上报充值记录

### 4. 适龄限制
- 在开发者中心配置年龄要求（如12+、16+、18+）
- SDK自动检查玩家是否满足年龄要求

## 📦 已添加的依赖

### libs.versions.toml
```toml
[versions]
tapSdk = "4.8.2"
kotlinxSerialization = "1.4.1"

[libraries]
tap-compliance = { group = "com.taptap.sdk", name = "tap-compliance", version.ref = "tapSdk" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
```

### app/build.gradle.kts
```kotlin
implementation(libs.tap.compliance)
implementation(libs.kotlinx.serialization.json)
```

## 🔧 核心文件

### 1. YjcyApplication.kt
- 初始化TapSDK时添加合规认证配置
- 配置选项：
  - `showSwitchAccount = true`：显示切换账号按钮
  - `useAgeRange = false`：不需要获取真实年龄段（使用范围即可）

### 2. TapComplianceManager.kt
合规认证管理器，封装所有SDK调用：

**主要方法**：
- `registerCallback(callback)`：注册合规认证回调
- `startup(activity, userIdentifier)`：开始认证
- `startupWithCurrentAccount(activity)`：使用当前TapTap账号认证
- `exit()`：退出认证
- `checkPayLimit(activity, amount, callback)`：检查充值限制
- `submitPayResult(amount, callback)`：上报充值金额
- `getAgeRange()`：获取玩家年龄段（0=未成年，1=成年，-1=未知）
- `getRemainingTime()`：获取剩余游戏时长（秒）

**回调事件**：
- `LOGIN_SUCCESS`：认证成功
- `EXITED`：退出认证
- `SWITCH_ACCOUNT`：需要切换账号
- `PERIOD_RESTRICT`：时段限制
- `DURATION_LIMIT`：时长限制
- `AGE_LIMIT`：年龄限制
- `INVALID_CLIENT_OR_NETWORK_ERROR`：错误
- `REAL_NAME_STOP`：实名认证停止

### 3. TapComplianceScreen.kt
完整的合规认证UI界面：

**功能模块**：
- 状态卡片：显示当前认证状态
- 玩家信息：显示年龄段和剩余时长
- 操作按钮：
  - 检查登录状态
  - 开始认证
  - 退出认证
  - 设置适龄限制
  - 检查充值限制
- 功能说明区域

### 4. MainActivity.kt
- 添加路由：`taptap_compliance`
- 主菜单添加入口："🛡️ 合规认证"

## 🚀 使用方式

### 方式一：通过主菜单
1. 启动游戏
2. 在主菜单点击"🛡️ 合规认证"
3. 进入合规认证界面

### 方式二：代码调用

```kotlin
// 1. 注册回调
TapComplianceManager.registerCallback(object : TapComplianceManager.ComplianceCallback {
    override fun onLoginSuccess(extra: Map<String, Any>?) {
        // 认证成功，可以进入游戏
    }
    
    override fun onExited(extra: Map<String, Any>?) {
        // 已退出认证
    }
    
    override fun onDurationLimit(extra: Map<String, Any>?) {
        // 游戏时长已达限制
    }
    
    // ... 其他回调
})

// 2. 开始认证（使用当前TapTap登录账号）
TapComplianceManager.startupWithCurrentAccount(activity)

// 3. 检查充值限制（金额单位：分）
TapComplianceManager.checkPayLimit(activity, 10000) { canPay, errorMsg ->
    if (canPay) {
        // 可以充值
    } else {
        // 充值受限
    }
}

// 4. 上报充值成功（金额单位：分）
TapComplianceManager.submitPayResult(10000) { success, errorMsg ->
    if (success) {
        // 上报成功
    }
}

// 5. 获取玩家信息
val ageRange = TapComplianceManager.getAgeRange()  // 0=未成年, 1=成年, -1=未知
val remainingTime = TapComplianceManager.getRemainingTime()  // 剩余时长（秒）

// 6. 退出认证
TapComplianceManager.exit()
```

## ⚠️ 重要注意事项

### 1. 前置要求
- **必须先接入TapTap登录**：合规认证依赖TapTap登录模块
- 使用`unionId`或`openId`作为用户唯一标识

### 2. 适龄限制配置
- 适龄限制（如12+、16+、18+）需要在**TapTap开发者中心后台**配置
- SDK不提供动态设置接口
- 配置路径：开发者中心 > 游戏服务 > 合规认证

### 3. 充值金额单位
- SDK要求使用**分**作为单位
- 例如：100元 = 10000分

### 4. 测试环境说明
- 测试环境下可能无法获取真实的防沉迷数据
- 需要在开发者中心开通服务后才能正常使用

### 5. 回调处理
- 收到`LOGIN_SUCCESS`后才能正常进入游戏
- 收到`PERIOD_RESTRICT`或`DURATION_LIMIT`时需要限制玩家游戏
- 收到`AGE_LIMIT`时说明玩家不满足年龄要求

## 📚 API文档

详细文档：https://developer.taptap.cn/docs/sdk/anti-addiction/guide/

## ✅ 接入检查清单

- [x] 添加SDK依赖
- [x] 初始化配置
- [x] 创建管理器类
- [x] 创建UI界面
- [x] 添加导航路由
- [x] 添加主菜单入口
- [ ] 在开发者中心开通服务
- [ ] 配置适龄限制
- [ ] 集成到游戏主流程
- [ ] 充值流程集成
- [ ] 正式环境测试

## 🎮 集成到游戏流程（建议）

### 1. 游戏启动流程
```
1. TapTap登录
2. 开始合规认证
3. 等待认证回调
4. 收到LOGIN_SUCCESS → 进入游戏
5. 收到限制回调 → 显示提示并限制游戏
```

### 2. 充值流程
```
1. 用户点击充值
2. checkPayLimit检查限制
3. 如果可以充值 → 进行支付
4. 支付成功 → submitPayResult上报
```

### 3. 游戏运行中
```
1. 定期调用getRemainingTime检查剩余时长
2. 时长不足时提示玩家
3. 时长耗尽时收到DURATION_LIMIT回调
```

## 🔍 调试建议

### 查看日志
使用TAG `TapComplianceManager` 查看合规认证相关日志：
```
adb logcat | grep TapComplianceManager
```

### 常见问题

**Q: 为什么getAgeRange返回-1？**
A: 可能原因：
- 用户未完成实名认证
- useAgeRange配置为false
- 游戏无版号且在TapPlay运行

**Q: 充值限制检查失败？**
A: 确保：
- 用户已完成实名认证
- 金额单位使用分（不是元）
- Activity参数正确传入

**Q: 认证回调没有触发？**
A: 检查：
- 是否已注册回调
- TapSDK是否正确初始化
- 网络连接是否正常

## 📝 更新日志

### 2025-01-XX
- ✅ 初次接入TapTap合规认证SDK 4.8.2
- ✅ 创建TapComplianceManager管理器
- ✅ 创建TapComplianceScreen UI界面
- ✅ 添加主菜单入口
- ✅ 完成基础功能集成

---

**接入完成日期**：2025年1月
**SDK版本**：4.8.2
**接入状态**：✅ 基础功能已完成，待正式环境测试
