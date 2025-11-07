package com.example.yjcy

import android.app.Application
import android.content.Intent
import android.util.Log
import com.taptap.sdk.compliance.TapTapCompliance
import com.taptap.sdk.compliance.TapTapComplianceCallback
import com.taptap.sdk.compliance.constants.ComplianceMessage
import com.taptap.sdk.compliance.option.TapTapComplianceOptions
import com.taptap.sdk.core.TapTapRegion
import com.taptap.sdk.core.TapTapSdk
import com.taptap.sdk.core.TapTapSdkOptions
import com.example.yjcy.taptap.TapDBManager
import com.example.yjcy.taptap.TapLoginManager
import com.example.yjcy.utils.RedeemCodeManager
import com.example.yjcy.utils.FirebaseRedeemCodeManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YjcyApplication : Application() {
    
    companion object {
        private const val TAG = "YjcyApplication"
        
        // 标记SDK是否已初始化
        @Volatile
        private var isSdkInitialized = false
        
        /**
         * 检查SDK是否已初始化
         */
        fun isSdkInitialized(): Boolean {
            return isSdkInitialized
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化兑换码管理器（不需要等待隐私政策）
        RedeemCodeManager.initialize(this)
        
        // 初始化Firebase兑换码缓存（优化国内网络延迟）
        FirebaseRedeemCodeManager.initializeCache(this)
        
        // 注意：不在这里初始化TapSDK！
        // 必须等待用户同意隐私政策后，才能初始化SDK
        // 由MainActivity在用户同意隐私政策后调用 initTapSDKIfNeeded()
        
        Log.d(TAG, "Application启动完成，等待隐私政策同意后初始化SDK")
    }
    
    /**
     * 延迟初始化TapSDK
     * 必须在用户同意隐私政策后调用
     */
    fun initTapSDKIfNeeded() {
        synchronized(this) {
            if (isSdkInitialized) {
                Log.d(TAG, "TapSDK已初始化，跳过重复初始化")
                return
            }
            
            // 初始化 TapSDK
            initTapSDK()
            
            // 注册合规认证回调
            registerComplianceCallback()
            
            // TapSDK.init()可能是异步的，延迟标记为已初始化
            // 确保SDK真正初始化完成后再允许调用其他功能
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isSdkInitialized = true
                Log.d(TAG, "✅ TapSDK延迟初始化完成（用户已同意隐私政策），已标记为已初始化")
            }, 1000) // 延迟1秒，确保SDK完全初始化
            
            Log.d(TAG, "TapSDK初始化调用完成，等待SDK完全初始化...")
        }
    }
    
    private fun initTapSDK() {
        try {
            Log.d(TAG, "========== 开始初始化 TapSDK ==========")
            Log.d(TAG, "Client ID: ${TapSDKConfig.CLIENT_ID}")
            Log.d(TAG, "Region: CN")
            
            val tapSdkOptions = TapTapSdkOptions(
                TapSDKConfig.CLIENT_ID, // Client ID
                TapSDKConfig.CLIENT_TOKEN, // Client Token
                TapTapRegion.CN // 国内区域
            ).apply {
                enableLog = true // 开启日志（正式发布时改为false）
            }
            
            // 合规认证配置
            val complianceOptions = TapTapComplianceOptions(
                showSwitchAccount = true, // 显示切换账号按钮
                useAgeRange = true // 获取真实年龄段信息
            )
            
            // 初始化 TapSDK（包含合规认证）
            Log.d(TAG, "正在调用 TapTapSdk.init()...")
            TapTapSdk.init(this, tapSdkOptions, complianceOptions)
            Log.d(TAG, "✅ TapSDK.init() 调用完成")
            
            Log.d(TAG, "✅ TapSDK初始化完成（包含合规认证）")
            
            // 初始化 TapDB（如果SDK支持）
            initTapDB()
            
            Log.d(TAG, "========== TapSDK初始化全部完成 ==========")
        } catch (e: Exception) {
            Log.e(TAG, "❌ TapSDK初始化失败: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    /**
     * 初始化TapDB
     * TapSDK 4.8.3版本中，TapDB可能通过ServiceManager架构自动初始化
     * 如果TapSDK.init()已调用，TapDB应该已经自动初始化，这里只做验证
     */
    private fun initTapDB() {
        try {
            Log.d(TAG, "========== 开始初始化TapDB ==========")
            Log.d(TAG, "AppID: ${TapSDKConfig.TAPDB_APP_ID}")
            Log.d(TAG, "Channel: ${TapSDKConfig.TAPDB_CHANNEL}")
            Log.d(TAG, "Version: ${TapSDKConfig.TAPDB_GAME_VERSION}")
            Log.d(TAG, "注意：TapSDK 4.8.3中，TapDB可能已通过TapSDK.init()自动初始化")
            
            // 尝试通过ServiceManager获取TapDB服务来验证是否已初始化
            try {
                val serviceManagerClass = Class.forName("com.taptap.sdk.servicemanager.TapServiceManager")
                val getServiceMethod = serviceManagerClass.getMethod("getService", Class::class.java)
                val tapDBAPIClass = Class.forName("com.taptap.sdk.tapdbapi.TapDBAPI")
                val tapDBService = getServiceMethod.invoke(null, tapDBAPIClass)
                
                if (tapDBService != null) {
                    Log.d(TAG, "✅ TapDB服务已通过ServiceManager初始化（TapSDK自动初始化）")
                    // 如果API支持手动设置参数，可以在这里设置
                    try {
                        val initMethod = tapDBAPIClass.getMethod("init", android.content.Context::class.java, String::class.java, String::class.java, String::class.java)
                        initMethod.invoke(tapDBService, this, TapSDKConfig.TAPDB_APP_ID, TapSDKConfig.TAPDB_CHANNEL, TapSDKConfig.TAPDB_GAME_VERSION)
                        Log.d(TAG, "✅ TapDB参数设置成功")
                        return
                    } catch (e: NoSuchMethodException) {
                        Log.d(TAG, "TapDBAPI不支持手动init方法，可能已自动配置")
                    }
                    return
                }
            } catch (e: ClassNotFoundException) {
                Log.d(TAG, "未找到ServiceManager或TapDBAPI，尝试直接访问TapDB类...")
            } catch (e: Exception) {
                Log.d(TAG, "通过ServiceManager获取TapDB失败: ${e.message}，尝试直接访问...")
            }
            
            // 如果ServiceManager方式失败，尝试直接访问TapDB类进行初始化
            val possiblePackages = listOf(
                "com.taptap.sdk.tapdbapi.TapDB",   // tap-db-api中的TapDB（最优先）
                "com.taptap.sdk.tapdb.TapDB",      // tap-db中的TapDB
                "com.taptap.sdk.tap.db.TapDB",     // 可能的包名变体
                "com.taptap.sdk.TapDB",            // 官方文档示例中的包名
                "com.tds.tapdb.sdk.TapDB",         // 备用包名
                "com.tds.tapdb.TapDB",             // 备用包名
                "com.tapsdk.tapdb.TapDB"           // 备用包名
            )
            
            for (packageName in possiblePackages) {
                try {
                    Log.d(TAG, "尝试包名: ${packageName}")
                    val tapDBClass = Class.forName(packageName)
                    
                    // 方式1: init(appId, channel, version, isCN) - 官方文档示例
                    try {
                        val initMethod = tapDBClass.getMethod("init", String::class.java, String::class.java, String::class.java, Boolean::class.java)
                        initMethod.invoke(null, TapSDKConfig.TAPDB_APP_ID, TapSDKConfig.TAPDB_CHANNEL, TapSDKConfig.TAPDB_GAME_VERSION, true)
                        Log.d(TAG, "✅ TapDB初始化成功 (使用${packageName}.init(appId, channel, version, isCN))")
                        return
                    } catch (e: NoSuchMethodException) {
                        Log.d(TAG, "${packageName} 没有init(appId, channel, version, isCN)方法")
                    }
                    
                    // 方式2: init(context, appId, channel, version) - 4参数带context
                    try {
                        val initMethod = tapDBClass.getMethod("init", android.content.Context::class.java, String::class.java, String::class.java, String::class.java)
                        initMethod.invoke(null, this, TapSDKConfig.TAPDB_APP_ID, TapSDKConfig.TAPDB_CHANNEL, TapSDKConfig.TAPDB_GAME_VERSION)
                        Log.d(TAG, "✅ TapDB初始化成功 (使用${packageName}.init(context, appId, channel, version))")
                        return
                    } catch (e: NoSuchMethodException) {
                        Log.d(TAG, "${packageName} 没有init(context, appId, channel, version)方法")
                    }
                    
                    // 方式3: init(context, appId) - 2参数
                    try {
                        val initMethod = tapDBClass.getMethod("init", android.content.Context::class.java, String::class.java)
                        initMethod.invoke(null, this, TapSDKConfig.TAPDB_APP_ID)
                        Log.d(TAG, "✅ TapDB初始化成功 (使用${packageName}.init(context, appId))")
                        return
                    } catch (e: NoSuchMethodException) {
                        Log.d(TAG, "${packageName} 没有init(context, appId)方法")
                    }
                    
                    // 如果类存在但没有找到init方法，可能已自动初始化
                    Log.d(TAG, "${packageName} 类存在，但未找到init方法，可能已通过TapSDK自动初始化")
                    Log.d(TAG, "✅ TapDB类存在，跳过手动初始化")
                    return
                    
                } catch (e: ClassNotFoundException) {
                    Log.d(TAG, "未找到${packageName}，继续尝试...")
                } catch (e: Exception) {
                    Log.w(TAG, "初始化${packageName}时出错: ${e.message}")
                }
            }
            
            Log.w(TAG, "⚠️ 未找到TapDB类")
            Log.w(TAG, "请确认：")
            Log.w(TAG, "1. app/build.gradle.kts中已添加 implementation(libs.tap.db)")
            Log.w(TAG, "2. 已同步Gradle项目")
            Log.w(TAG, "3. TapDB SDK版本与TapSDK版本匹配")
            Log.w(TAG, "4. TapSDK.init()已成功调用（TapDB可能已自动初始化）")
        } catch (e: Exception) {
            Log.e(TAG, "TapDB初始化异常: ${e.message}", e)
        } finally {
            Log.d(TAG, "========== TapDB初始化完成 ==========")
        }
    }
    
    /**
     * 注册合规认证全局回调
     */
    private fun registerComplianceCallback() {
        TapTapCompliance.registerComplianceCallback(
            callback = object : TapTapComplianceCallback {
                override fun onComplianceResult(code: Int, extra: Map<String, Any>?) {
                    when (code) {
                        ComplianceMessage.LOGIN_SUCCESS -> {
                            Log.d(TAG, "合规认证：登录成功")
                        }
                        ComplianceMessage.EXITED -> {
                            Log.d(TAG, "合规认证：用户退出")
                            // 清除TapDB账号ID
                            TapDBManager.clearUser(this@YjcyApplication)
                            // 退出当前登录
                            TapLoginManager.logout()
                            // 发送广播通知退出登录
                            sendLogoutBroadcast()
                        }
                        ComplianceMessage.SWITCH_ACCOUNT -> {
                            Log.d(TAG, "合规认证：切换账号")
                            // 清除TapDB账号ID
                            TapDBManager.clearUser(this@YjcyApplication)
                            // 退出当前登录
                            TapLoginManager.logout()
                            // 发送广播通知切换账号
                            sendLogoutBroadcast()
                        }
                        ComplianceMessage.PERIOD_RESTRICT -> {
                            Log.d(TAG, "合规认证：时段限制（未成年人22:00-8:00禁止游戏）")
                        }
                        ComplianceMessage.DURATION_LIMIT -> {
                            Log.d(TAG, "合规认证：时长限制")
                        }
                        ComplianceMessage.AGE_LIMIT -> {
                            Log.d(TAG, "合规认证：年龄限制")
                        }
                        ComplianceMessage.INVALID_CLIENT_OR_NETWORK_ERROR -> {
                            Log.e(TAG, "合规认证：客户端错误或网络错误")
                        }
                        ComplianceMessage.REAL_NAME_STOP -> {
                            Log.d(TAG, "合规认证：实名认证已停止")
                        }
                        else -> {
                            Log.d(TAG, "合规认证：未知回调 code=$code")
                        }
                    }
                }
            }
        )
        Log.d(TAG, "合规认证回调注册完成")
    }
    
    /**
     * 发送退出登录广播
     * 通知MainActivity返回登录界面
     */
    private fun sendLogoutBroadcast() {
        try {
            // 延迟200ms确保logout完成
            android.os.Handler(mainLooper).postDelayed({
                // 创建重启MainActivity的Intent
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.let {
                    // 清除所有Activity栈并重新启动
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(it)
                    Log.d(TAG, "已重启应用")
                }
            }, 200)
        } catch (e: Exception) {
            Log.e(TAG, "重启应用失败: ${e.message}", e)
        }
    }
}