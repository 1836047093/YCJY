package com.example.yjcy.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 兑换码管理器
 * 基于用户ID管理兑换码的使用记录，确保每个用户只能使用一次兑换码
 */
object RedeemCodeManager {
    
    private const val TAG = "RedeemCodeManager"
    private const val PREFS_NAME = "redeem_codes"
    private const val KEY_PREFIX = "user_redeem_codes_" // 格式: user_redeem_codes_{userId}
    
    private var sharedPreferences: SharedPreferences? = null
    
    /**
     * 初始化管理器
     */
    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d(TAG, "RedeemCodeManager 初始化完成")
        }
    }
    
    /**
     * 检查用户是否已解锁GM模式（通过PROGM兑换码）
     * @param userId 用户ID（TapTap unionId 或 openId）
     * @return true表示已解锁，false表示未解锁
     */
    fun isGMModeUnlocked(userId: String?): Boolean {
        return isCodeUsedByUser(userId, "PROGM")
    }
    
    /**
     * 检查用户是否已使用过指定的兑换码
     * @param userId 用户ID（TapTap unionId 或 openId）
     * @param code 兑换码
     * @return true表示已使用过，false表示未使用
     */
    fun isCodeUsedByUser(userId: String?, code: String): Boolean {
        if (userId.isNullOrBlank()) {
            Log.w(TAG, "用户ID为空，无法检查兑换码使用记录")
            return false // 未登录用户默认返回false，允许使用（但实际应该要求登录）
        }
        
        val prefs = sharedPreferences ?: run {
            Log.e(TAG, "SharedPreferences未初始化，请先调用initialize()")
            return false
        }
        
        val key = "$KEY_PREFIX$userId"
        val usedCodesJson = prefs.getString(key, null)
        
        if (usedCodesJson.isNullOrBlank()) {
            return false // 该用户还没有使用过任何兑换码
        }
        
        try {
            // 解析JSON字符串为Set<String>
            val usedCodes = usedCodesJson.split(",").toSet()
            val isUsed = usedCodes.contains(code.uppercase())
            
            Log.d(TAG, "检查兑换码: userId=$userId, code=$code, isUsed=$isUsed")
            return isUsed
        } catch (e: Exception) {
            Log.e(TAG, "解析兑换码记录失败", e)
            return false
        }
    }
    
    /**
     * 标记兑换码为已使用
     * @param userId 用户ID（TapTap unionId 或 openId）
     * @param code 兑换码
     * @return true表示成功标记，false表示失败
     */
    fun markCodeAsUsed(userId: String?, code: String): Boolean {
        if (userId.isNullOrBlank()) {
            Log.e(TAG, "用户ID为空，无法标记兑换码")
            return false
        }
        
        val prefs = sharedPreferences ?: run {
            Log.e(TAG, "SharedPreferences未初始化，请先调用initialize()")
            return false
        }
        
        val key = "$KEY_PREFIX$userId"
        val usedCodesJson = prefs.getString(key, null)
        
        val usedCodes = if (usedCodesJson.isNullOrBlank()) {
            mutableSetOf<String>()
        } else {
            usedCodesJson.split(",").toMutableSet()
        }
        
        // 添加新兑换码（转换为大写）
        usedCodes.add(code.uppercase())
        
        // 保存回SharedPreferences
        val newJson = usedCodes.joinToString(",")
        val success = prefs.edit().putString(key, newJson).commit()
        
        if (success) {
            Log.d(TAG, "标记兑换码成功: userId=$userId, code=$code")
        } else {
            Log.e(TAG, "标记兑换码失败: userId=$userId, code=$code")
        }
        
        return success
    }
    
    /**
     * 获取用户已使用的所有兑换码列表
     * @param userId 用户ID
     * @return 已使用的兑换码集合
     */
    fun getUserUsedCodes(userId: String?): Set<String> {
        if (userId.isNullOrBlank()) {
            return emptySet()
        }
        
        val prefs = sharedPreferences ?: return emptySet()
        val key = "$KEY_PREFIX$userId"
        val usedCodesJson = prefs.getString(key, null)
        
        return if (usedCodesJson.isNullOrBlank()) {
            emptySet()
        } else {
            try {
                usedCodesJson.split(",").toSet()
            } catch (e: Exception) {
                Log.e(TAG, "解析兑换码列表失败", e)
                emptySet()
            }
        }
    }
    
    /**
     * 清除用户的所有兑换码记录（用于测试或重置）
     * @param userId 用户ID
     */
    fun clearUserCodes(userId: String?) {
        if (userId.isNullOrBlank()) {
            return
        }
        
        val prefs = sharedPreferences ?: return
        val key = "$KEY_PREFIX$userId"
        prefs.edit().remove(key).apply()
        Log.d(TAG, "已清除用户兑换码记录: userId=$userId")
    }
}

