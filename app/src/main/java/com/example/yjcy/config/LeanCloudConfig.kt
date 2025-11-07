package com.example.yjcy.config

import android.content.Context
import android.util.Log
import cn.leancloud.LCCloud
import cn.leancloud.LCObject
import cn.leancloud.LeanCloud
import cn.leancloud.LCLogger

/**
 * LeanCloud配置类（SDK 8.2.28）
 * 用于初始化LeanCloud SDK和管理兑换码数据
 */
object LeanCloudConfig {
    private const val TAG = "LeanCloudConfig"
    
    // LeanCloud应用凭证
    private const val APP_ID = "iYTyrfx7JvXpsxyHySexdbCO-gzGzoHsz"
    private const val APP_KEY = "ZPSfzxh6H6PxrkSQ1Vr8xlBK"
    private const val SERVER_URL = "https://iytyrfx7.lc-cn-n1-shared.com"  // 华北节点
    // 注意：MasterKey 仅用于服务器端，客户端不使用
    
    // 是否使用国内节点（华北、华东节点）
    private const val USE_CN_NODE = true
    
    // LeanCloud数据表名（与管理后台保持一致）
    const val TABLE_REDEEM_CODES = "RedeemCode"  // 兑换码表（注意：不带s）
    const val TABLE_USER_REDEEM = "UserRedeemRecords"  // 用户兑换记录表
    
    /**
     * 初始化LeanCloud SDK
     * 必须在Application.onCreate()中调用
     */
    fun initialize(context: Context) {
        try {
            Log.d(TAG, "========== 开始初始化 LeanCloud SDK 8.2.28 ==========")
            
            // 检查配置是否已设置
            if (APP_ID == "YOUR_APP_ID" || APP_KEY == "YOUR_APP_KEY") {
                Log.e(TAG, "❌ LeanCloud配置未完成！")
                Log.e(TAG, "请修改 LeanCloudConfig.kt 中的 APP_ID 和 APP_KEY")
                Log.e(TAG, "访问 https://console.leancloud.cn/ 创建应用并获取凭证")
                return
            }
            
            // 开启日志（生产环境建议关闭）
            LeanCloud.setLogLevel(LCLogger.Level.DEBUG)
            
            // 初始化SDK（8.2.28版本）
            if (USE_CN_NODE) {
                // 使用中国节点（华北/华东）
                LeanCloud.initialize(context, APP_ID, APP_KEY, SERVER_URL)
                Log.d(TAG, "使用中国节点: $SERVER_URL")
            } else {
                // 国际版或自定义服务器
                if (SERVER_URL != "https://YOUR_SERVER_URL.api.lncldglobal.com") {
                    LeanCloud.initialize(context, APP_ID, APP_KEY, SERVER_URL)
                    Log.d(TAG, "使用自定义服务器: $SERVER_URL")
                } else {
                    LeanCloud.initialize(context, APP_ID, APP_KEY)
                    Log.d(TAG, "使用默认配置")
                }
            }
            
            Log.d(TAG, "✅ LeanCloud初始化成功")
            Log.d(TAG, "App ID: $APP_ID")
            Log.d(TAG, "节点类型: ${if (USE_CN_NODE) "中国节点" else "国际节点/自定义"}")
            Log.d(TAG, "数据表配置:")
            Log.d(TAG, "  - 兑换码表: $TABLE_REDEEM_CODES")
            Log.d(TAG, "  - 用户记录表: $TABLE_USER_REDEEM")
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
            LCObject("Test")
            true
        } catch (e: Exception) {
            Log.w(TAG, "LeanCloud未初始化或初始化失败", e)
            false
        }
    }
}

