package com.example.yjcy.taptap

import android.content.Context
import android.util.Log

/**
 * TapDB管理器
 * 封装TapDB数据分析相关操作
 */
object TapDBManager {
    
    private const val TAG = "TapDBManager"
    
    /**
     * 设置账号ID
     * 在用户登录成功后调用，会将账号ID关联到后续的所有事件
     * 
     * @param context 上下文
     * @param userId 用户唯一标识符，长度不超过256个字符
     */
    fun setUser(context: Context, userId: String) {
        try {
            Log.d(TAG, "========== 开始设置TapDB账号ID ==========")
            Log.d(TAG, "userId长度: ${userId.length}")
            Log.d(TAG, "userId内容: $userId")
            
            if (userId.isBlank()) {
                Log.w(TAG, "❌ 设置账号ID失败：userId为空")
                return
            }
            
            // 验证长度不超过256个字符
            if (userId.length > 256) {
                Log.w(TAG, "❌ 设置账号ID失败：userId长度超过256个字符 (${userId.length})")
                return
            }
            
            // TapSDK 4.8.3使用ServiceManager架构，TapDB可能通过ServiceManager获取
            // 尝试通过ServiceManager获取TapDB服务
            try {
                val serviceManagerClass = Class.forName("com.taptap.sdk.servicemanager.TapServiceManager")
                val getServiceMethod = serviceManagerClass.getMethod("getService", Class::class.java)
                
                // 尝试获取TapDB API接口
                val tapDBAPIClass = Class.forName("com.taptap.sdk.tapdbapi.TapDBAPI")
                val tapDBService = getServiceMethod.invoke(null, tapDBAPIClass)
                
                if (tapDBService != null) {
                    // 尝试调用setUser方法
                    try {
                        val setUserMethod = tapDBAPIClass.getMethod("setUser", String::class.java)
                        setUserMethod.invoke(tapDBService, userId)
                        Log.d(TAG, "✅ 设置账号ID成功 (通过ServiceManager获取TapDBAPI): ${userId}")
                        return
                    } catch (e: NoSuchMethodException) {
                        try {
                            val setUserMethod = tapDBAPIClass.getMethod("setUser", Context::class.java, String::class.java)
                            setUserMethod.invoke(tapDBService, context, userId)
                            Log.d(TAG, "✅ 设置账号ID成功 (通过ServiceManager获取TapDBAPI，双参数): ${userId}")
                            return
                        } catch (e2: Exception) {
                            Log.d(TAG, "TapDBAPI.setUser方法调用失败: ${e2.message}")
                        }
                    }
                }
            } catch (e: ClassNotFoundException) {
                Log.d(TAG, "未找到ServiceManager或TapDBAPI，尝试直接访问TapDB类...")
            } catch (e: Exception) {
                Log.d(TAG, "通过ServiceManager获取TapDB失败: ${e.message}，尝试直接访问...")
            }
            
            // 如果ServiceManager方式失败，尝试直接访问TapDB类
            // 根据Maven Central：https://central.sonatype.com/artifact/com.taptap.sdk/tap-core/4.8.3
            // tap-core 4.8.3已经包含了tap-db依赖（com.taptap.sdk:tap-db:4.8.3）
            // tap-db依赖于tap-db-api，TapDB类可能在tap-db-api中
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
                    
                    // 尝试单参数setUser(userId) - 根据官方文档
                    try {
                        val setUserMethod = tapDBClass.getMethod("setUser", String::class.java)
                        setUserMethod.invoke(null, userId)
                        Log.d(TAG, "✅ 设置账号ID成功 (使用${packageName}.setUser): ${userId}")
                        return
                    } catch (e: NoSuchMethodException) {
                        // 尝试双参数setUser(context, userId)
                        try {
                            val setUserMethod = tapDBClass.getMethod("setUser", Context::class.java, String::class.java)
                            setUserMethod.invoke(null, context, userId)
                            Log.d(TAG, "✅ 设置账号ID成功 (使用${packageName}.setUser(context, userId)): ${userId}")
                            return
                        } catch (e2: NoSuchMethodException) {
                            Log.d(TAG, "${packageName} 没有找到setUser方法")
                        }
                    }
                } catch (e: ClassNotFoundException) {
                    Log.d(TAG, "未找到${packageName}，继续尝试...")
                } catch (e: Exception) {
                    Log.w(TAG, "调用${packageName}.setUser失败: ${e.message}")
                }
            }
            
            Log.e(TAG, "❌ 无法找到TapDB类")
            Log.e(TAG, "请确认：")
            Log.e(TAG, "1. app/build.gradle.kts中已添加 implementation(libs.tap.db)")
            Log.e(TAG, "2. 已同步Gradle项目")
            Log.e(TAG, "3. TapDB SDK版本与TapSDK版本匹配")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 设置账号ID异常: ${e.message}", e)
            e.printStackTrace()
        } finally {
            Log.d(TAG, "========== 设置TapDB账号ID完成 ==========")
        }
    }
    
    /**
     * 清除账号ID
     * 在用户登出时调用，清除当前SDK中保存的账号ID
     * 调用后，后续上报的事件将不再携带账号ID
     * 
     * @param context 上下文
     */
    fun clearUser(context: Context) {
        try {
            Log.d(TAG, "========== 开始清除TapDB账号ID ==========")
            
            // 尝试通过ServiceManager获取TapDB服务
            try {
                val serviceManagerClass = Class.forName("com.taptap.sdk.servicemanager.TapServiceManager")
                val getServiceMethod = serviceManagerClass.getMethod("getService", Class::class.java)
                val tapDBAPIClass = Class.forName("com.taptap.sdk.tapdbapi.TapDBAPI")
                val tapDBService = getServiceMethod.invoke(null, tapDBAPIClass)
                
                if (tapDBService != null) {
                    val clearUserMethod = tapDBAPIClass.getMethod("clearUser")
                    clearUserMethod.invoke(tapDBService)
                    Log.d(TAG, "✅ 清除账号ID成功 (通过ServiceManager获取TapDBAPI)")
                    return
                }
            } catch (e: Exception) {
                Log.d(TAG, "通过ServiceManager获取TapDB失败: ${e.message}，尝试直接访问...")
            }
            
            // 如果ServiceManager方式失败，尝试直接访问TapDB类
            val possiblePackages = listOf(
                "com.taptap.sdk.tapdbapi.TapDB",   // tap-db-api中的TapDB（最优先）
                "com.taptap.sdk.tapdb.TapDB",      // tap-db中的TapDB
                "com.taptap.sdk.TapDB",
                "com.tds.tapdb.sdk.TapDB",
                "com.tds.tapdb.TapDB",
                "com.tapsdk.tapdb.TapDB"
            )
            
            for (packageName in possiblePackages) {
                try {
                    val tapDBClass = Class.forName(packageName)
                    val clearUserMethod = tapDBClass.getMethod("clearUser")
                    clearUserMethod.invoke(null)
                    Log.d(TAG, "✅ 清除账号ID成功 (使用${packageName})")
                    return
                } catch (e: ClassNotFoundException) {
                    // 继续尝试下一个
                } catch (e: NoSuchMethodException) {
                    Log.w(TAG, "${packageName} 没有clearUser方法")
                } catch (e: Exception) {
                    Log.w(TAG, "调用${packageName}.clearUser失败: ${e.message}")
                }
            }
            
            Log.e(TAG, "❌ 无法找到TapDB类")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 清除账号ID异常: ${e.message}", e)
        } finally {
            Log.d(TAG, "========== 清除TapDB账号ID完成 ==========")
        }
    }
    
    /**
     * 上报自定义事件
     * 
     * @param context 上下文
     * @param eventName 事件名称
     * @param properties 事件属性（可选）
     */
    fun trackEvent(context: Context, eventName: String, properties: Map<String, Any>? = null) {
        try {
            if (eventName.isBlank()) {
                Log.w(TAG, "上报事件失败：事件名称为空")
                return
            }
            
            // 尝试通过ServiceManager获取TapDB服务
            try {
                val serviceManagerClass = Class.forName("com.taptap.sdk.servicemanager.TapServiceManager")
                val getServiceMethod = serviceManagerClass.getMethod("getService", Class::class.java)
                val tapDBAPIClass = Class.forName("com.taptap.sdk.tapdbapi.TapDBAPI")
                val tapDBService = getServiceMethod.invoke(null, tapDBAPIClass)
                
                if (tapDBService != null) {
                    if (properties != null && properties.isNotEmpty()) {
                        val trackEventMethod = tapDBAPIClass.getMethod("trackEvent", String::class.java, Map::class.java)
                        trackEventMethod.invoke(tapDBService, eventName, properties)
                    } else {
                        val trackEventMethod = tapDBAPIClass.getMethod("trackEvent", String::class.java)
                        trackEventMethod.invoke(tapDBService, eventName)
                    }
                    Log.d(TAG, "上报事件成功: ${eventName} (通过ServiceManager获取TapDBAPI)")
                    return
                }
            } catch (e: Exception) {
                Log.d(TAG, "通过ServiceManager获取TapDB失败: ${e.message}，尝试直接访问...")
            }
            
            // 如果ServiceManager方式失败，尝试直接访问TapDB类
            val possiblePackages = listOf(
                "com.taptap.sdk.tapdbapi.TapDB",   // tap-db-api中的TapDB（最优先）
                "com.taptap.sdk.tapdb.TapDB",      // tap-db中的TapDB
                "com.taptap.sdk.TapDB",
                "com.tds.tapdb.sdk.TapDB",
                "com.tds.tapdb.TapDB",
                "com.tapsdk.tapdb.TapDB"
            )
            
            for (packageName in possiblePackages) {
                try {
                    val tapDBClass = Class.forName(packageName)
                    if (properties != null && properties.isNotEmpty()) {
                        val trackEventMethod = tapDBClass.getMethod("trackEvent", String::class.java, Map::class.java)
                        trackEventMethod.invoke(null, eventName, properties)
                    } else {
                        val trackEventMethod = tapDBClass.getMethod("trackEvent", String::class.java)
                        trackEventMethod.invoke(null, eventName)
                    }
                    Log.d(TAG, "上报事件成功: ${eventName} (使用${packageName})")
                    return
                } catch (e: ClassNotFoundException) {
                    // 继续尝试下一个
                } catch (e: Exception) {
                    Log.w(TAG, "调用${packageName}.trackEvent失败: ${e.message}")
                }
            }
            
            Log.e(TAG, "❌ 无法找到TapDB类上报事件")
        } catch (e: Exception) {
            Log.e(TAG, "上报事件异常: ${e.message}", e)
        }
    }
}

