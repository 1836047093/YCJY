# TapTap登录接入说明

## 📋 接入概述

已成功按照 [TapTap 开发者文档](https://developer.taptap.cn/docs/sdk/taptap-login/guide/) 接入TapTap登录功能。

## 🎯 接入内容

### 1. SDK依赖 ✅

**文件：`gradle/libs.versions.toml`**
- 添加了 `tap-login` SDK依赖（版本：4.8.2）

**文件：`app/build.gradle.kts`**
- 已引入 `implementation(libs.tap.login)`

### 2. 核心功能类 ✅

**文件：`app/src/main/java/com/example/yjcy/taptap/TapLoginManager.kt`**

封装的登录管理器，提供以下功能：

- **`loginWithBasicProfile(activity)`**  
  使用基础权限登录（昵称、头像）
  
- **`loginWithScopes(activity, scopes)`**  
  使用指定权限登录，支持的权限：
  - `Scopes.SCOPE_PUBLIC_PROFILE`：基本信息（昵称、头像）
  - `Scopes.SCOPE_USER_FRIENDS`：好友相关数据
  - `Scopes.SCOPE_BASIC_INFO`：详细信息（性别、地区等）

- **`getCurrentAccount()`**  
  获取当前登录账号信息

- **`isLoggedIn()`**  
  检查是否已登录

- **`logout()`**  
  登出

- **`getUserDisplayInfo()`**  
  获取用户显示信息

### 3. UI界面 ✅

**文件：`app/src/main/java/com/example/yjcy/ui/taptap/TapLoginScreen.kt`**

完整的登录界面，包含：

- **未登录状态**：显示登录按钮和权限说明
- **已登录状态**：显示用户信息（昵称、Union ID、Open ID、头像）
- **加载状态**：登录过程中显示加载动画
- **错误处理**：登录失败、用户取消等情况的处理

### 4. 导航集成 ✅

**文件：`app/src/main/java/com/example/yjcy/MainActivity.kt`**

- 在导航图中添加了 `taptap_login` 路由
- 在主菜单中添加了"🎮 TapTap登录"按钮

## 📱 使用方法

### 启动应用后

1. 在主菜单点击 **"🎮 TapTap登录"**
2. 进入登录界面，点击 **"使用 TapTap 登录"**
3. 系统会跳转到TapTap客户端或网页进行授权
4. 授权成功后返回应用，显示用户信息
5. 可以点击 **"登出"** 退出登录

### 在代码中使用

```kotlin
// 检查登录状态
if (TapLoginManager.isLoggedIn()) {
    val account = TapLoginManager.getCurrentAccount()
    Log.d("TapLogin", "当前用户: ${account?.name}")
}

// 执行登录（在Composable中）
val activity = LocalContext.current as? Activity
activity?.let {
    viewModelScope.launch {
        when (val result = TapLoginManager.loginWithBasicProfile(it)) {
            is TapLoginManager.LoginResult.Success -> {
                // 登录成功
                val account = result.account
            }
            is TapLoginManager.LoginResult.Error -> {
                // 登录失败
            }
            TapLoginManager.LoginResult.Cancelled -> {
                // 用户取消
            }
        }
    }
}

// 登出
TapLoginManager.logout()
```

## 🔧 配置信息

**TapSDK配置文件：`app/src/main/java/com/example/yjcy/TapSDKConfig.kt`**

```kotlin
Client ID: qmbx71v2tbmp4nsbmj
Client Token: ObngFI5E4L2gfn4sI6ghhXjGvsUSrsyshttEr93s
区域: 中国大陆 (TapTapRegion.CN)
```

SDK已在 `YjcyApplication.onCreate()` 中完成初始化。

## 📝 获取的用户信息

登录成功后可获取：

- **unionId**：用户的唯一标识（跨应用）
- **openId**：用户在当前应用的唯一标识
- **name**：用户昵称
- **avatar**：用户头像URL

## ⚠️ 注意事项

1. **网络权限**：已在 `AndroidManifest.xml` 中配置 `INTERNET` 权限
2. **TapSDK初始化**：在使用登录功能前，SDK已在Application中初始化
3. **Activity Context**：登录功能需要Activity实例，确保在正确的上下文中调用
4. **协程使用**：登录方法是挂起函数，需在协程中调用

## 🚀 后续扩展

可以基于登录功能实现：

- 用户数据云存储
- 好友系统
- 排行榜
- 成就系统
- 社区互动

## 📚 参考文档

- [TapTap登录开发指南](https://developer.taptap.cn/docs/sdk/taptap-login/guide/)
- [TapSDK快速开始](https://developer.taptap.cn/docs/sdk/access/quickstart/)
