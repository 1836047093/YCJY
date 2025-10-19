# TapTap更新唤起问题排查指南

## 🔍 问题描述

手机安装的是V1.0版本，TapTap上传的是V1.5版本，但进入游戏后没有提示更新。

## ❓ 问题原因

### 核心原理
TapTap更新唤起功能通过比较**versionCode**来判断是否需要更新：
- ✅ **TapTap的versionCode > 手机的versionCode** → 弹出更新提示
- ❌ **TapTap的versionCode ≤ 手机的versionCode** → 不提示更新

⚠️ **注意**：SDK比较的是`versionCode`（数字），而不是`versionName`（字符串）

### 常见原因

#### 1. 版本号配置问题 ⭐⭐⭐⭐⭐
**最常见的原因！**

当前代码配置（`app/build.gradle.kts`）：
```kotlin
versionCode = 2
versionName = "1.5"
```

**问题分析**：
- 如果你手机上安装的APK也是`versionCode = 2`
- 而TapTap开发者中心上传的APK也是`versionCode = 2`
- 那么SDK会认为版本相同，不会提示更新

**解决方案**：
1. **检查手机上的APK版本号**
   - 设置 → 应用 → Yjcy → 查看版本信息
   - 或使用adb命令：`adb shell dumpsys package com.example.yjcy | grep versionCode`

2. **确保TapTap上的版本号更高**
   - 登录TapTap开发者中心
   - 查看已上传APK的versionCode
   - **必须确保TapTap的versionCode > 手机的versionCode**

3. **如何测试更新功能**：
   ```kotlin
   // 方案1：降低本地versionCode（用于测试）
   versionCode = 1  // 改为比TapTap低的版本号
   versionName = "1.0"
   
   // 方案2：在TapTap上传更高版本（正式方案）
   versionCode = 3  // 改为比手机高的版本号
   versionName = "1.6"
   ```

#### 2. SDK初始化时机问题 ⭐⭐⭐⭐
**已修复！**

之前的代码在SDK初始化后立即调用更新检查，可能SDK还没完全准备好。

**修复内容**：
```kotlin
// 修复前：立即检查更新
(application as? YjcyApplication)?.initTapSDKIfNeeded()
TapUpdateManager.checkForceUpdate() // ❌ SDK可能还没准备好

// 修复后：延迟500ms
(application as? YjcyApplication)?.initTapSDKIfNeeded()
android.os.Handler(mainLooper).postDelayed({
    TapUpdateManager.checkForceUpdate() // ✅ 确保SDK已初始化
}, 500)
```

#### 3. TapTap开发者中心配置问题 ⭐⭐⭐
检查开发者中心配置：
- 是否已上传新版本APK
- APK是否已通过审核
- 发布状态是否正确（立即上线/预约/敬请期待）

#### 4. 网络连接问题 ⭐⭐
- 确保手机网络正常
- 检查SDK是否能正常访问TapTap服务器

#### 5. TapSDK配置问题 ⭐
- Client ID是否正确
- Client Token是否正确
- 是否选择了正确的区域（CN/海外）

## 🛠️ 排查步骤

### 第1步：查看日志 🔍

启动游戏后，查看Logcat日志，搜索以下关键词：

#### 关键日志1：SDK初始化
```
TAG: YjcyApplication
期待的日志：
- "TapSDK延迟初始化完成（用户已同意隐私政策）"
```

#### 关键日志2：更新检查
```
TAG: TapUpdateManager
期待的日志：
- "===================================="
- "开始检查TapTap更新..."
- "注意：只有当TapTap开发者中心的versionCode > 本地versionCode时才会提示更新"
- "更新检查已触发，等待SDK响应..."
```

#### 关键日志3：错误信息
如果有错误，会显示：
```
TAG: TapUpdateManager
错误日志：
- "检查更新失败：[错误信息]"
- 可能原因：
  1. TapSDK未正确初始化
  2. 网络连接问题
  3. TapTap开发者中心未正确配置
```

### 第2步：验证版本号 📱

#### 方法1：通过代码查看
在`MainActivity.kt`的`onCreate`中添加临时日志：
```kotlin
// 临时调试代码
val packageInfo = packageManager.getPackageInfo(packageName, 0)
Log.d("MainActivity", "本地APK信息：")
Log.d("MainActivity", "  versionCode: ${packageInfo.versionCode}")
Log.d("MainActivity", "  versionName: ${packageInfo.versionName}")
```

#### 方法2：通过adb查看
```bash
adb shell dumpsys package com.example.yjcy | grep versionCode
adb shell dumpsys package com.example.yjcy | grep versionName
```

#### 方法3：通过设置查看
设置 → 应用 → Yjcy → 查看版本信息

### 第3步：确认TapTap配置 ☁️

1. 登录 [TapTap开发者中心](https://developer.taptap.cn/)
2. 进入 **商店 > 游戏资料 > 商店资料**
3. 查看已上传的APK信息：
   - versionCode是多少？
   - versionName是什么？
   - 审核状态如何？
   - 发布状态是什么？

### 第4步：测试更新功能 🧪

#### 测试方案1：降低本地版本（推荐）
最简单的测试方法！

1. 修改`app/build.gradle.kts`：
   ```kotlin
   versionCode = 1  // 改为比TapTap低的版本号
   versionName = "0.9"
   ```

2. 重新编译并安装到手机
   ```bash
   ./gradlew assembleDebug
   adb install -r app/debug/app-arm64-v8a-debug.apk
   ```

3. 启动游戏，应该会弹出更新提示

#### 测试方案2：上传新版本到TapTap
1. 修改`app/build.gradle.kts`：
   ```kotlin
   versionCode = 3  // 改为比手机高的版本号
   versionName = "1.6"
   ```

2. 编译release版本
   ```bash
   ./gradlew assembleRelease
   ```

3. 上传到TapTap开发者中心

4. 等待审核通过

5. 启动游戏测试

## ✅ 成功标志

更新功能正常工作时，应该看到：

### 1. 日志输出正常
```
TapUpdateManager: ====================================
TapUpdateManager: 开始检查TapTap更新...
TapUpdateManager: 注意：只有当TapTap开发者中心的versionCode > 本地versionCode时才会提示更新
TapUpdateManager: ====================================
TapUpdateManager: 更新检查已触发，等待SDK响应...
```

### 2. 弹出更新对话框
- 显示新版本信息
- 提示用户更新
- 点击后跳转到TapTap客户端

### 3. 两种可能的情况

#### 情况1：已安装TapTap客户端
- 直接唤起TapTap
- 跳转到游戏详情页
- 显示"更新"按钮

#### 情况2：未安装TapTap客户端
- 弹窗询问是否使用TapTap更新
- 点击"确定"引导下载TapTap
- 点击"取消"不更新

## 📋 完整检查清单

使用此清单逐项排查：

- [ ] 手机上APK的versionCode是多少？______
- [ ] TapTap上APK的versionCode是多少？______
- [ ] TapTap的versionCode是否大于手机的？ 是/否
- [ ] 日志中是否显示"TapSDK延迟初始化完成"？ 是/否
- [ ] 日志中是否显示"开始检查TapTap更新"？ 是/否
- [ ] 日志中是否有错误信息？ 是/否
- [ ] 手机网络是否正常？ 是/否
- [ ] TapTap开发者中心是否已上传新版本？ 是/否
- [ ] APK是否已通过审核？ 是/否
- [ ] Client ID配置是否正确？ 是/否

## 🎯 快速解决方案

### 如果你只是想快速测试更新功能：

**最简单的方法**：降低本地版本号

1. 打开`app/build.gradle.kts`
2. 修改版本号：
   ```kotlin
   versionCode = 1  // 改为1
   versionName = "0.9"  // 改为0.9
   ```
3. 重新编译安装
4. 启动游戏
5. 应该会立即弹出更新提示（前提是TapTap上有versionCode=2的版本）

## 📞 仍然无法解决？

如果按照以上步骤仍然无法解决，请提供以下信息：

1. **完整的Logcat日志**（从启动到进入主菜单）
   - 过滤TAG: `YjcyApplication`, `TapUpdateManager`, `MainActivity`

2. **版本号信息**
   - 手机APK的versionCode和versionName
   - TapTap上传APK的versionCode和versionName

3. **TapTap开发者中心配置截图**
   - 商店资料页面
   - APK上传状态

4. **网络状况**
   - 是否能正常访问TapTap网站
   - 是否使用了代理/VPN

## 🔗 相关文档

- [TapTap更新唤起官方文档](https://developer.taptap.cn/docs/sdk/update/guide/)
- [TapTap更新唤起接入说明.md](./TapTap更新唤起接入说明.md)
- [TapTap开发者中心](https://developer.taptap.cn/)

## 📝 修复记录

- **2025-01-XX**: 修复SDK初始化时机问题，添加500ms延迟
- **2025-01-XX**: 优化TapUpdateManager日志输出
- **2025-01-XX**: 创建本排查指南
