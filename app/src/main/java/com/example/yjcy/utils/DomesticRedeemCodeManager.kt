package com.example.yjcy.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * 国内兑换码API管理器
 * 使用HTTP API调用国内服务器，替代Firebase（国内无法访问）
 * 
 * 配置说明：
 * 1. 将 BASE_URL 修改为你的国内服务器地址
 * 2. 确保服务器部署在国内（阿里云/腾讯云/华为云等）
 * 3. 实现对应的后端API接口（见 国内兑换码API后端实现指南.md）
 */
object DomesticRedeemCodeManager {
    
    private const val TAG = "DomesticRedeemCodeManager"
    
    // TODO: 修改为你的国内服务器地址
    private const val BASE_URL = "https://your-api-domain.com/api/v1/redeem"
    
    // 网络超时配置
    private const val CONNECT_TIMEOUT_MS = 5000L // 5秒连接超时
    private const val READ_TIMEOUT_MS = 10000L   // 10秒读取超时
    
    private val gson = Gson()
    
    /**
     * 兑换码数据模型
     */
    data class RedeemCodeResponse(
        val code: String,
        val type: String,
        @SerializedName("isValid") val isValid: Boolean,
        @SerializedName("isUsed") val isUsed: Boolean,
        @SerializedName("usedByUserId") val usedByUserId: String? = null
    )
    
    /**
     * 用户兑换码数据模型
     */
    data class UserRedeemResponse(
        @SerializedName("userId") val userId: String,
        @SerializedName("usedCodes") val usedCodes: List<String>,
        @SerializedName("gmModeUnlocked") val gmModeUnlocked: Boolean,
        @SerializedName("supporterUnlocked") val supporterUnlocked: Boolean
    )
    
    /**
     * 检查兑换码响应模型
     */
    data class CheckCodeResponse(
        @SerializedName("isUsed") val isUsed: Boolean
    )
    
    /**
     * API响应基础模型
     */
    data class ApiResponse<T>(
        val success: Boolean,
        val data: T? = null,
        val message: String? = null
    )
    
    /**
     * 使用兑换码响应模型
     */
    data class UseCodeResponse(
        val success: Boolean,
        val message: String? = null
    )
    
    /**
     * 查询兑换码信息
     */
    suspend fun getRedeemCode(code: String): RedeemCodeResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/code/${code.uppercase()}")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.connectTimeout = CONNECT_TIMEOUT_MS.toInt()
                connection.readTimeout = READ_TIMEOUT_MS.toInt()
                connection.setRequestProperty("Content-Type", "application/json")
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "查询兑换码成功: code=$code, response=$response")
                    
                    return@withContext gson.fromJson(response, RedeemCodeResponse::class.java)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.w(TAG, "查询兑换码失败: code=$code, responseCode=$responseCode, error=$errorResponse")
                    null
                }
            } catch (e: IOException) {
                Log.e(TAG, "查询兑换码网络错误: code=$code", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "查询兑换码异常: code=$code", e)
                null
            }
        }
    }
    
    /**
     * 查询用户兑换码列表
     */
    suspend fun getUserRedeemCodes(userId: String): UserRedeemResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val encodedUserId = java.net.URLEncoder.encode(userId, "UTF-8")
                val url = URL("$BASE_URL/user/$encodedUserId")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.connectTimeout = CONNECT_TIMEOUT_MS.toInt()
                connection.readTimeout = READ_TIMEOUT_MS.toInt()
                connection.setRequestProperty("Content-Type", "application/json")
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "查询用户兑换码成功: userId=$userId, response=$response")
                    
                    return@withContext gson.fromJson(response, UserRedeemResponse::class.java)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.w(TAG, "查询用户兑换码失败: userId=$userId, responseCode=$responseCode, error=$errorResponse")
                    null
                }
            } catch (e: IOException) {
                Log.e(TAG, "查询用户兑换码网络错误: userId=$userId", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "查询用户兑换码异常: userId=$userId", e)
                null
            }
        }
    }
    
    /**
     * 检查兑换码是否已使用
     */
    suspend fun isCodeUsedByUser(userId: String, code: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val encodedUserId = java.net.URLEncoder.encode(userId, "UTF-8")
                val encodedCode = java.net.URLEncoder.encode(code.uppercase(), "UTF-8")
                val url = URL("$BASE_URL/check?userId=$encodedUserId&code=$encodedCode")
                
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = CONNECT_TIMEOUT_MS.toInt()
                connection.readTimeout = READ_TIMEOUT_MS.toInt()
                connection.setRequestProperty("Content-Type", "application/json")
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "检查兑换码使用状态: userId=$userId, code=$code, response=$response")
                    
                    val checkResponse = gson.fromJson(response, CheckCodeResponse::class.java)
                    return@withContext checkResponse.isUsed
                } else {
                    Log.w(TAG, "检查兑换码失败: userId=$userId, code=$code, responseCode=$responseCode")
                    false
                }
            } catch (e: IOException) {
                Log.e(TAG, "检查兑换码网络错误: userId=$userId, code=$code", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "检查兑换码异常: userId=$userId, code=$code", e)
                false
            }
        }
    }
    
    /**
     * 使用兑换码
     */
    suspend fun useRedeemCode(userId: String, code: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/use")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.connectTimeout = CONNECT_TIMEOUT_MS.toInt()
                connection.readTimeout = READ_TIMEOUT_MS.toInt()
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                // 发送请求体
                val requestBody = gson.toJson(mapOf(
                    "userId" to userId,
                    "code" to code.uppercase()
                ))
                
                connection.outputStream.bufferedWriter().use { writer ->
                    writer.write(requestBody)
                }
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "使用兑换码成功: userId=$userId, code=$code, response=$response")
                    
                    val useResponse = gson.fromJson(response, UseCodeResponse::class.java)
                    return@withContext useResponse.success
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.w(TAG, "使用兑换码失败: userId=$userId, code=$code, responseCode=$responseCode, error=$errorResponse")
                    false
                }
            } catch (e: IOException) {
                Log.e(TAG, "使用兑换码网络错误: userId=$userId, code=$code", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "使用兑换码异常: userId=$userId, code=$code", e)
                false
            }
        }
    }
    
    /**
     * 检查用户是否已解锁GM模式
     */
    suspend fun isGMModeUnlocked(userId: String): Boolean {
        val userData = getUserRedeemCodes(userId)
        return userData?.gmModeUnlocked ?: false
    }
    
    /**
     * 检查用户是否已解锁支持者功能
     */
    suspend fun hasUsedSupporterCode(userId: String): Boolean {
        val userData = getUserRedeemCodes(userId)
        return userData?.supporterUnlocked ?: false
    }
    
    /**
     * 获取用户已使用的所有兑换码列表
     */
    suspend fun getUserUsedCodes(userId: String): Set<String> {
        val userData = getUserRedeemCodes(userId)
        return userData?.usedCodes?.toSet() ?: emptySet()
    }
}

