package com.example.yjcy.taptap

import android.app.Activity
import android.util.Log
import com.taptap.sdk.update.TapTapUpdate
import com.taptap.sdk.update.TapTapUpdateCallback

/**
 * TapTap更新唤起管理器
 * 封装TapTap更新唤起相关操作
 * 
 * 官方文档：https://developer.taptap.cn/docs/sdk/update/guide/
 */
object TapUpdateManager {
    
    private const val TAG = "TapUpdateManager"
    
    /**
     * 检查强制更新
     * 适合：无自有版本管理系统的游戏，特别是单机游戏
     * 
     * SDK会自动检查开发者中心配置的版本信息：
     * 1. 如果当前版本 < 开发者中心配置的版本，会弹出更新窗口
     * 2. 用户点击更新后，会唤起TapTap客户端进行更新
     * 3. 如果未安装TapTap客户端，会询问用户是否使用TapTap更新
     * 
     * 使用场景：
     * - 应用启动时检查更新
     * - 用户手动点击"检查更新"按钮
     * 
     * 重要提示：
     * - SDK通过比较本地APK的versionCode和TapTap开发者中心的versionCode来判断是否需要更新
     * - 只有当TapTap上的versionCode大于本地versionCode时，才会弹出更新提示
     * - 如果版本相同或TapTap版本更低，不会有任何提示
     */
    fun checkForceUpdate() {
        Log.d(TAG, "====================================")
        Log.d(TAG, "开始检查TapTap更新...")
        Log.d(TAG, "注意：只有当TapTap开发者中心的versionCode > 本地versionCode时才会提示更新")
        Log.d(TAG, "====================================")
        try {
            TapTapUpdate.checkForceUpdate()
            Log.d(TAG, "更新检查已触发，等待SDK响应...")
        } catch (e: Exception) {
            Log.e(TAG, "检查更新失败：${e.message}", e)
            Log.e(TAG, "可能原因：")
            Log.e(TAG, "1. TapSDK未正确初始化")
            Log.e(TAG, "2. 网络连接问题")
            Log.e(TAG, "3. TapTap开发者中心未正确配置")
        }
    }
    
    /**
     * 游戏自行判断更新
     * 适合：自有版本管理系统，期望更灵活触发、展示更新的网游
     * 
     * @param activity 当前Activity实例
     * @param onCancel 用户取消更新的回调（可选）
     * 
     * 注意：
     * - 游戏需要自行维护版本校验逻辑
     * - 确定需要更新时调用此方法
     * - SDK只负责唤起TapTap客户端进行更新
     * 
     * 使用场景：
     * - 游戏有自己的版本管理服务器
     * - 需要更灵活的更新策略（如可跳过、强制更新等）
     */
    fun updateGame(activity: Activity, onCancel: (() -> Unit)? = null) {
        Log.d(TAG, "唤起游戏更新")
        try {
            TapTapUpdate.updateGame(
                activity,
                object : TapTapUpdateCallback {
                    override fun onCancel() {
                        Log.d(TAG, "用户取消更新")
                        onCancel?.invoke()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "唤起更新失败", e)
        }
    }
    
    /**
     * 检查并提示更新（简化版）
     * 结合了检查和唤起更新的功能
     * 
     * @param activity 当前Activity实例
     * @param onCancel 用户取消更新的回调（可选）
     * 
     * 如果游戏使用"开发者中心配置更新"方式，建议使用 checkForceUpdate()
     * 如果游戏有自己的版本管理，建议使用 updateGame()
     */
    fun checkAndUpdate(activity: Activity, onCancel: (() -> Unit)? = null) {
        Log.d(TAG, "检查并更新游戏")
        updateGame(activity, onCancel)
    }
}
