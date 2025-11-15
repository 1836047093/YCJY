package com.example.yjcy.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 兑换码管理器
 * 基于用户ID管理兑换码的使用记录，确保每个用户只能使用一次兑换码
 * 使用本地SharedPreferences存储
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
            Log.d(TAG, "RedeemCodeManager 初始化完成（仅使用本地存储）")
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
        
        return try {
            // 解析JSON字符串为Set<String>
            val usedCodes = usedCodesJson.split(",").toSet()
            val isUsed = usedCodes.contains(code.uppercase())
            
            Log.d(TAG, "检查兑换码: userId=$userId, code=$code, isUsed=$isUsed")
            isUsed
        } catch (e: Exception) {
            Log.e(TAG, "解析兑换码记录失败", e)
            false
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
    
    /**
     * 检查用户是否已使用过任何支持者兑换码（SUPPORTER 开头）
     * @param userId 用户ID
     * @return true表示已使用过，false表示未使用
     */
    fun hasUsedSupporterCode(userId: String?): Boolean {
        if (userId.isNullOrBlank()) {
            return false
        }
        
        val usedCodes = getUserUsedCodes(userId)
        return usedCodes.any { it.startsWith("SUPPORTER", ignoreCase = true) }
    }
    
    /**
     * 检查存档中是否包含任何支持者兑换码（SUPPORTER 开头）
     * @param usedRedeemCodes 存档中已使用的兑换码集合
     * @return true表示包含，false表示不包含
     */
    fun hasSupporterCodeInSave(usedRedeemCodes: Set<String>): Boolean {
        return usedRedeemCodes.any { it.startsWith("SUPPORTER", ignoreCase = true) }
    }
    
    /**
     * 检查用户是否已解锁支持者功能
     * 支持者功能需要通过兑换码解锁
     * 支持以下格式的兑换码：
     * - SUPPORTER
     * - SUPPORTER001 到 SUPPORTER150
     * @param userId 用户ID
     * @param usedRedeemCodes 存档中已使用的兑换码集合（用于存档级别的解锁检查）
     * @return true表示已解锁，false表示未解锁
     */
    fun isSupporterFeatureUnlocked(userId: String?, usedRedeemCodes: Set<String>): Boolean {
        // 检查账号级别是否已使用过支持者兑换码
        if (hasUsedSupporterCode(userId)) {
            return true
        }
        
        // 检查存档级别是否包含支持者兑换码
        return hasSupporterCodeInSave(usedRedeemCodes)
    }
    
    /**
     * 验证兑换码是否是有效的支持者兑换码
     * @param code 兑换码（大写）
     * @return true表示是有效的支持者兑换码，false表示无效
     */
    fun isValidSupporterCode(code: String): Boolean {
        val upperCode = code.uppercase()
        
        // 检查是否是 SUPPORTER001 到 SUPPORTER150 格式
        if (upperCode.startsWith("SUPPORTER")) {
            val numberPart = upperCode.removePrefix("SUPPORTER")
            return try {
                val number = numberPart.toInt()
                number in 1..150
            } catch (e: NumberFormatException) {
                false
            }
        }
        
        return false
    }
    
    /**
     * 支持者功能类型枚举
     */
    enum class SupporterFeature {
        SPEED_2X_3X,           // 2X,3X游戏速度
        EXTRA_SAVE_SLOTS,      // 额外的游戏存档（第4、5个槽位）
        AUTO_SAVE,             // 游戏自动存档功能
        AUTO_APPROVE_SALARY,   // 薪资自动审批功能
        AUTO_UPDATE,           // 游戏自动更新功能
        AUTO_PROMOTION,        // 游戏自动宣传功能
        AUTO_COMPLAINT,        // 客诉自动处理功能
        BATCH_TRAINING,        // 员工一键培训功能
        SUBSIDIARY_MANAGEMENT  // 子公司管理功能（收购竞争对手）
    }
}
