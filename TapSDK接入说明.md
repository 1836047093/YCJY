# TapSDK 接入说明

## ✅ 已完成的配置

### 1. 依赖配置

**gradle/libs.versions.toml**
```toml
[versions]
tapSdk = "4.8.2"

[libraries]
tap-core = { group = "com.taptap.sdk", name = "tap-core", version.ref = "tapSdk" }
```

**app/build.gradle.kts**
```kotlin
dependencies {
    implementation(libs.tap.core)
}
```

**settings.gradle.kts**
```kotlin
repositories {
    maven { url = uri("https://nexus.tapsvc.com/repository/releases/") }
}
```

### 2. SDK初始化

已在 `YjcyApplication.kt` 中初始化：
- **Client ID**: qmbx71v2tbmp4nsbmj
- **Client Token**: ObngFI5E4L2gfn4sI6ghhXjGvsUSrsyshttEr93s
- **区域**: 国内（CN）
- **日志**: Debug模式开启，Release关闭

### 3. 配置文件

创建了 `TapSDKConfig.kt` 统一管理配置信息：
```kotlin
object TapSDKConfig {
    const val CLIENT_ID = "qmbx71v2tbmp4nsbmj"
    const val CLIENT_TOKEN = "ObngFI5E4L2gfn4sI6ghhXjGvsUSrsyshttEr93s"
    const val SERVER_SECRET = "FTiojsHBkvJb4Pjhq8KXGN48oR2Xc7BJ"
}
```

## 📋 后续接入功能模块

根据需要接入以下功能模块：

### TapTap 登录
```kotlin
// 添加依赖
implementation("com.taptap.sdk:tap-login:4.8.2")

// 使用登录
TapTapLogin.registerLoginCallback { result ->
    when {
        result.isSuccess -> {
            // 登录成功
            val profile = result.data
        }
        result.isCancel -> {
            // 用户取消
        }
        else -> {
            // 登录失败
        }
    }
}
TapTapLogin.startTapLogin()
```

### 内嵌动态
```kotlin
// 添加依赖
implementation("com.taptap.sdk:tap-moment:4.8.2")

// 打开动态页面
TapTapMoment.open()
```

### 数据分析
```kotlin
// 添加依赖
implementation("com.taptap.sdk:tap-analytics:4.8.2")

// 自定义事件跟踪
TapTapAnalytics.trackEvent("event_name", mapOf(
    "param1" to "value1",
    "param2" to "value2"
))
```

### 成就系统
```kotlin
// 添加依赖
implementation("com.taptap.sdk:tap-achievement:4.8.2")

// 获取成就列表
TapTapAchievement.fetchAllAchievementList()
```

### 排行榜
```kotlin
// 添加依赖
implementation("com.taptap.sdk:tap-leaderboard:4.8.2")

// 提交分数
TapTapLeaderboard.submitScore("leaderboard_id", score)
```

## 🔧 环境要求

- ✅ Android 5.0（API level 21）或更高版本
- ✅ Kotlin 1.7.21 或更高版本（当前：2.2.20）
- ✅ Gradle 6.1.1+（当前：8.13.0）
- ✅ AGP 4.0.1+（当前：8.13.0）

## 📝 混淆配置

如果项目开启了代码混淆，需要在 `proguard-rules.pro` 中添加：

```proguard
# TapSDK
-keep class com.taptap.** { *; }
-keep interface com.taptap.** { *; }
```

## 🔗 相关链接

- [TapSDK 官方文档](https://developer.taptap.cn/docs/sdk/access/quickstart/)
- [TapTap 开发者中心](https://developer.taptap.cn/)
- [SDK 下载](https://developer.taptap.cn/docs/tap-download/)

## ⚠️ 注意事项

1. **Server Secret** 仅用于服务器端API调用，客户端请勿使用
2. 正式发布前请将 `enableLog` 设置为 `false`
3. 确保在 `AndroidManifest.xml` 中已添加网络权限（已添加）
4. 版本更新时注意查看 [更新日志](https://developer.taptap.cn/docs/sdk/update/release-note/)
