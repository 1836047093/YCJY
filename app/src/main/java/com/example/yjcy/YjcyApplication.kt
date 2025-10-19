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
import com.example.yjcy.taptap.TapLoginManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YjcyApplication : Application() {
    
    companion object {
        private const val TAG = "YjcyApplication"
        
        // 标记SDK是否已初始化
        @Volatile
        private var isSdkInitialized = false
    }
    
    override fun onCreate() {
        super.onCreate()
        
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
            
            isSdkInitialized = true
            Log.d(TAG, "TapSDK延迟初始化完成（用户已同意隐私政策）")
        }
    }
    
    private fun initTapSDK() {
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
        TapTapSdk.init(this, tapSdkOptions, complianceOptions)
        
        Log.d(TAG, "TapSDK初始化完成（包含合规认证）")
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
                            // 退出当前登录
                            TapLoginManager.logout()
                            // 发送广播通知退出登录
                            sendLogoutBroadcast()
                        }
                        ComplianceMessage.SWITCH_ACCOUNT -> {
                            Log.d(TAG, "合规认证：切换账号")
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