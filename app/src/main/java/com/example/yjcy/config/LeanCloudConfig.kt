package com.example.yjcy.config

import android.content.Context
import android.util.Log
import cn.leancloud.AVOSCloud
import cn.leancloud.AVObject

/**
 * LeanCloud配置类
 * 用于初始化LeanCloud SDK
 */
object LeanCloudConfig {
    private const val TAG = "LeanCloudConfig"
    
    // TODO: 请在LeanCloud控制台获取以下信息并替换
    // 访问 https://console.leancloud.cn/ 创建应用后获取
    private const val APP_ID = "YOUR_APP_ID"  // 替换为你的App ID
    private const val APP_KEY = "YOUR_APP_KEY"  // 替换为你的App Key
    private const val SERVER_URL = "https://YOUR_SERVER_URL.cn.leancloud.cn"  // 替换为你的服务器地址（可选）
    
    /**
     * 初始化LeanCloud SDK
     * 必须在Application.onCreate()中调用
     */
    fun initialize(context: Context) {
        try {
            Log.d(TAG, "========== 开始初始化 LeanCloud ==========")
            
            // 检查配置是否已设置
            if (APP_ID == "YOUR_APP_ID" || APP_KEY == "YOUR_APP_KEY") {
                Log.e(TAG, "❌ LeanCloud配置未完成！")
                Log.e(TAG, "请修改 LeanCloudConfig.kt 中的 APP_ID 和 APP_KEY")
                Log.e(TAG, "访问 https://console.leancloud.cn/ 创建应用并获取凭证")
                return
            }
            
            // 初始化LeanCloud
            AVOSCloud.initialize(
                context,
                APP_ID,
                APP_KEY,
                SERVER_URL.takeIf { it != "https://YOUR_SERVER_URL.cn.leancloud.cn" }
            )
            
            // 设置日志级别（开发时开启，发布时关闭）
            AVOSCloud.setLogLevel(AVOSCloud.LOG_LEVEL_VERBOSE)
            
            Log.d(TAG, "✅ LeanCloud初始化成功")
            Log.d(TAG, "App ID: $APP_ID")
            Log.d(TAG, "Server URL: ${SERVER_URL.takeIf { it != "https://YOUR_SERVER_URL.cn.leancloud.cn" } ?: "默认"}")
            Log.d(TAG, "========== LeanCloud初始化完成 ==========")
        } catch (e: Exception) {
            Log.e(TAG, "❌ LeanCloud初始化失败", e)
            e.printStackTrace()
        }
    }
    
    /**
     * 检查LeanCloud是否已初始化
     */
    fun isInitialized(): Boolean {
        return try {
            // 尝试创建一个测试对象来验证SDK是否已初始化
            AVObject("Test")
            true
        } catch (e: Exception) {
            false
        }
    }
}

