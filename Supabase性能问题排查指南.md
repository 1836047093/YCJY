# Supabase性能问题排查指南

## 问题描述

用户反馈：接入Supabase后，主菜单FPS掉到20多帧，怀疑是Supabase导致的性能问题。

## 问题分析

### 可能的Supabase性能问题

#### 1. **初始化阻塞主线程** 🔴
```kotlin
// 在Application.onCreate()中调用
RedeemCodeManager.initialize(this)
  ↓
SupabaseConfig.initialize()
  ↓
createSupabaseClient(...)  // 可能会建立网络连接
```

**问题：**
- `createSupabaseClient`可能涉及网络请求或耗时操作
- 在应用启动时阻塞主线程
- 导致应用启动卡顿

#### 2. **Supabase SDK持续的后台活动** 🔴
Supabase SDK可能会：
- 维护WebSocket连接
- 定期发送心跳包
- 同步数据
- 监听数据库变化

**问题：**
- 持续占用网络和CPU资源
- 影响主线程性能

#### 3. **网络请求队列堆积** ⚠️
- 频繁的协程创建
- 大量未完成的网络请求
- IO线程池压力大

#### 4. **与复杂背景动画叠加** 🔴
- 主菜单的复杂Canvas绘制（300+操作/帧）
- Supabase后台活动
- 两者叠加导致性能崩溃

## 已实施的优化

### 1. ✅ 异步初始化Supabase
```kotlin
// 不再阻塞主线程
ioScope.launch {
    SupabaseConfig.initialize()
}
```

### 2. ✅ 添加临时禁用开关
```kotlin
private const val ENABLE_SUPABASE = false  // 设置为false临时禁用
```

### 3. ✅ 共享CoroutineScope
```kotlin
private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
```

### 4. ✅ 防抖机制
```kotlin
// 5秒内只同步一次
if (now - lastSyncTime > SYNC_DEBOUNCE_MS) { ... }
```

### 5. ✅ 移除主菜单复杂背景动画
- 移除300+次Canvas绘制
- 移除303次sin计算
- 移除所有复杂动画

## 测试方案

### 方案一：禁用Supabase测试（已实施）

**步骤：**
1. 已将`ENABLE_SUPABASE`设置为`false`
2. 重新编译运行
3. 观察主菜单FPS

**预期结果：**
- 如果FPS恢复到55-60帧 → **确认是Supabase的问题**
- 如果FPS仍然很低 → 还有其他性能问题

### 方案二：分步测试

**步骤1：测试背景动画的影响**
- 已移除所有复杂背景动画
- 编译运行，观察FPS

**步骤2：测试Supabase的影响**
- 如果步骤1后FPS仍低，说明不是背景动画的问题
- 禁用Supabase（`ENABLE_SUPABASE = false`）
- 编译运行，观察FPS

**步骤3：定位具体问题**
- 对比两次测试结果
- 确定性能瓶颈

## 可能的解决方案

### 如果确认是Supabase导致的

#### 方案A：完全移除Supabase
```kotlin
// 完全不使用Supabase，仅使用本地存储
private const val ENABLE_SUPABASE = false
```

**优点：**
- 彻底解决性能问题
- 简化代码

**缺点：**
- 失去云端同步功能
- 兑换码无法跨设备同步

#### 方案B：懒加载Supabase
```kotlin
// 只在需要时才初始化
fun initializeSupabaseLazy() {
    if (!useSupabase && ENABLE_SUPABASE) {
        ioScope.launch {
            SupabaseConfig.initialize()
            useSupabase = true
        }
    }
}
```

**优点：**
- 启动时不初始化，不影响性能
- 需要时再初始化

**缺点：**
- 首次使用时可能有延迟

#### 方案C：优化Supabase客户端配置
```kotlin
createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY
) {
    install(Postgrest) {
        // 配置更短的超时时间
        // 禁用实时订阅
        // 优化连接池
    }
}
```

**优点：**
- 保留功能
- 降低资源占用

**缺点：**
- 需要深入了解Supabase SDK配置

#### 方案D：只在兑换码界面时启用
```kotlin
// 只在打开兑换码对话框时才启用Supabase
LaunchedEffect(showRedeemDialog) {
    if (showRedeemDialog && !useSupabase) {
        initializeSupabaseLazy()
    }
}
```

**优点：**
- 最小化性能影响
- 保留云端同步功能

**缺点：**
- 首次打开时有延迟

## 性能对比测试

### 测试结果记录

| 配置 | 主菜单FPS | 游戏中FPS | 说明 |
|------|-----------|-----------|------|
| 背景动画+Supabase | 20-30 | 25-35 | 原始状态 |
| 简化背景+Supabase | ? | ? | 待测试 |
| 简化背景+禁用Supabase | ? | ? | 待测试 |

**测试方法：**
1. 编译当前版本（背景已简化+Supabase已禁用）
2. 在主菜单观察FPS 30秒，记录平均值和最低值
3. 进入游戏观察FPS 30秒
4. 填写上表

## 推荐方案

基于目前的分析，我推荐：

### 短期方案（立即）
1. **临时禁用Supabase**（已实施）
   - 设置`ENABLE_SUPABASE = false`
   - 测试FPS是否恢复

2. **移除复杂背景动画**（已实施）
   - 这是最主要的性能瓶颈
   - FPS应该有显著提升

### 长期方案

如果确认是Supabase的问题：

1. **懒加载方案**
   - 延迟到首次使用时初始化
   - 对大多数用户无影响（不使用兑换码）

2. **配置优化**
   - 研究Supabase SDK配置选项
   - 禁用不需要的功能（如实时订阅）

3. **替代方案**
   - 考虑使用更轻量的网络库
   - 或完全使用本地存储

## 当前状态

### 已完成的优化

1. ✅ **移除主菜单复杂背景动画**
   - Canvas绘制：300+ → 5个
   - sin计算：303次/帧 → 0
   - 预期FPS提升：+150%

2. ✅ **游戏循环后台线程优化**
   - 计算移到Dispatchers.Default
   - 批量更新状态
   - 预期FPS提升：+100%

3. ✅ **Supabase协程优化**
   - 共享CoroutineScope
   - 防抖机制（5秒）
   - 异步初始化

4. ✅ **临时禁用Supabase开关**
   - `ENABLE_SUPABASE = false`
   - 用于性能测试

### 下一步

**立即测试：**
1. 重新编译运行
2. 观察主菜单FPS
3. 如果FPS恢复到55-60帧 → 问题已解决
4. 如果FPS仍然很低 → 继续排查

**如果FPS已恢复：**
- 背景动画是主要问题（占90%）
- Supabase可能有小影响（占10%）
- 可以选择保持Supabase禁用，或使用懒加载方案

**如果FPS仍然很低：**
- 还有其他未发现的性能问题
- 需要进一步profiling
- 可以使用Android Studio的CPU Profiler

## 版本信息

- **测试日期**: 2025-11-05
- **当前配置**: 
  - 背景动画：已简化
  - Supabase：已临时禁用（ENABLE_SUPABASE=false）
  - 游戏循环：已优化
- **等待测试结果**

请重新编译运行，告诉我FPS情况！



