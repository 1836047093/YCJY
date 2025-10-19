# TapTap更新唤起功能接入说明

## 📋 功能概述

TapTap更新唤起功能允许游戏通过TapTap客户端进行版本更新，提供便捷的一键更新体验。

**官方文档**：https://developer.taptap.cn/docs/sdk/update/guide/

## ✅ 已完成的接入工作

### 1. SDK依赖配置

#### libs.versions.toml
```toml
[versions]
tapSdk = "4.8.2"

[libraries]
tap-update = { group = "com.taptap.sdk", name = "tap-update", version.ref = "tapSdk" }
```

#### build.gradle.kts
```kotlin
dependencies {
    implementation(libs.tap.core)
    implementation(libs.tap.update)
}
```

### 2. AndroidManifest.xml权限配置

添加了必要的权限和queries配置：

```xml
<!-- 安装包权限 -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<!-- 查询TapTap客户端 -->
<queries>
    <package android:name="com.taptap" />
    <package android:name="com.taptap.global" />
</queries>
```

**重要说明**：
- `REQUEST_INSTALL_PACKAGES`：允许应用请求安装包
- `<queries>`：Android 11+ 需要声明要查询的应用包名

### 3. TapUpdateManager管理类

创建了 `TapUpdateManager.kt` 封装更新相关功能：

**文件路径**：`app/src/main/java/com/example/yjcy/taptap/TapUpdateManager.kt`

#### 主要方法：

##### (1) checkForceUpdate() - 开发者中心配置更新
```kotlin
TapUpdateManager.checkForceUpdate()
```
- **适用场景**：无自有版本管理系统的游戏，特别是单机游戏
- **工作原理**：SDK自动检查开发者中心配置的版本，发现新版本时弹窗提示更新
- **优点**：简单易用，无需自己维护版本号

##### (2) updateGame() - 游戏自行判断更新
```kotlin
TapUpdateManager.updateGame(activity) {
    // 用户取消更新的回调
}
```
- **适用场景**：有自有版本管理系统的网游
- **工作原理**：游戏自己判断是否需要更新，决定时机调用
- **优点**：更灵活，可自定义更新策略

##### (3) checkAndUpdate() - 简化版
```kotlin
TapUpdateManager.checkAndUpdate(activity) {
    // 用户取消更新的回调
}
```
- **适用场景**：快速接入，无需区分更新方式
- **工作原理**：内部调用 updateGame()

### 4. 启动时自动检查更新

在应用启动时自动检查更新，无需用户手动操作：

**文件**：`MainActivity.kt` - `onCreate`函数

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 如果用户已同意隐私政策，则初始化SDK并检查更新
    if (hasAgreedPrivacy) {
        (application as? YjcyApplication)?.initTapSDKIfNeeded()
        // 启动时自动检查更新
        TapUpdateManager.checkForceUpdate()
    }
    
    // ... 其他代码
}
```

## 🔧 使用方式

### 当前实现：启动时自动检查更新 ✅

应用启动时自动检查更新，如果有新版本会自动弹窗提示：

```kotlin
// MainActivity.onCreate() 中已实现
if (hasAgreedPrivacy) {
    (application as? YjcyApplication)?.initTapSDKIfNeeded()
    TapUpdateManager.checkForceUpdate()
}
```

**优点**：
- 用户打开游戏即可检测到新版本
- 无需手动操作，体验流畅
- 符合TapTap SDK最佳实践

### 可选方式：游戏内手动检查

如需在游戏设置界面添加手动检查更新功能：

```kotlin
Button(onClick = {
    TapUpdateManager.checkAndUpdate(activity) {
        // 用户取消更新
    }
}) {
    Text("检查更新")
}
```

## 📝 开发者中心配置

### 1. 上传APK
1. 登录 [TapTap开发者中心](https://developer.taptap.cn/)
2. 进入 **商店 > 游戏资料 > 商店资料**
3. 上传APK文件
4. 设置发布状态：
   - **立即上线**：公开发布
   - **敬请期待** / **预约**：仅用于测试更新功能

### 2. 配置版本信息
- 确保上传的APK版本号高于当前版本
- SDK会自动比较版本号，提示用户更新

## 🧪 测试流程

### 测试准备
1. 在TapTap开发者中心上传新版本APK
2. 设置版本号高于当前安装版本
3. 通过审核（或使用测试状态）

### 测试步骤
1. 安装旧版本APK到测试设备
2. 启动游戏，进入主菜单
3. 点击"🔄 检查更新"按钮
4. 应该会弹出更新提示窗口

### 预期行为
- **已安装TapTap**：直接唤起TapTap客户端，跳转到游戏详情页进行更新
- **未安装TapTap**：弹窗询问用户是否使用TapTap更新
  - 点击"确定"：引导用户下载安装TapTap
  - 点击"取消"：触发 onCancel 回调

## ⚠️ 重要注意事项

### 1. Activity重建问题
**问题**：屏幕旋转、配置修改时Activity重建，会导致更新UI失效

**解决方案**：在 `AndroidManifest.xml` 的 `<activity>` 标签中添加：
```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|keyboardHidden"
    ... >
```

**已处理**：本项目已在 `AndroidManifest.xml` 中配置。

### 2. 版本号管理
- 务必确保开发者中心的版本号 > 当前安装版本
- 使用语义化版本号（如 1.0.0 -> 1.1.0）
- versionCode 必须递增

### 3. 签名一致性
- 更新包的签名必须与当前安装包一致
- 否则Android会拒绝安装更新

### 4. 网络权限
- 确保应用有网络权限（已在 AndroidManifest.xml 中配置）
- 更新检查需要联网

## 📦 核心文件清单

| 文件 | 说明 |
|------|------|
| `gradle/libs.versions.toml` | SDK版本配置 |
| `app/build.gradle.kts` | 依赖引入 |
| `app/src/main/AndroidManifest.xml` | 权限配置 |
| `app/src/main/java/com/example/yjcy/taptap/TapUpdateManager.kt` | 更新管理器 |
| `app/src/main/java/com/example/yjcy/MainActivity.kt` | UI集成（主菜单） |

## 🎯 后续优化建议

1. **版本更新日志**：在更新提示中展示更新内容
2. **强制更新**：对于重要版本，可实现强制更新逻辑
3. **下载进度**：显示更新包下载进度（需自定义UI）
4. **更新策略**：
   - WiFi下自动更新
   - 移动网络提示用户
   - 静默下载，提示安装

## 🔗 相关资源

- [TapTap更新唤起官方文档](https://developer.taptap.cn/docs/sdk/update/guide/)
- [TapTap开发者中心](https://developer.taptap.cn/)
- [TapSDK下载页](https://developer.taptap.cn/docs/tap-download/)

## ✨ 总结

TapTap更新唤起功能已成功接入，主要特点：

1. ✅ **自动检测**：应用启动时自动检查更新，无需用户操作
2. ✅ **用户友好**：利用TapTap客户端，更新体验流畅
3. ✅ **灵活配置**：支持开发者中心配置和自定义版本管理
4. ✅ **完整封装**：TapUpdateManager封装所有API，方便调用

当前实现为**启动时自动检查更新**，符合TapTap SDK最佳实践，用户打开游戏即可第一时间发现新版本。
