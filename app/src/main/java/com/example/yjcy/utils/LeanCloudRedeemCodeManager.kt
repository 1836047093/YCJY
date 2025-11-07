package com.example.yjcy.utils

import android.content.Context
import android.util.Log
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.example.yjcy.config.LeanCloudConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

/**
 * LeanCloud 兑换码管理器
 * 使用 LeanCloud 云端存储兑换码使用记录，实现跨设备同步和防作弊
 * 
 * 数据结构（与管理后台保持一致）：
 * 
 * RedeemCode表（兑换码信息，注意：不带s）：
 * - code: String（兑换码，唯一）
 * - type: String（类型：gm/supporter）
 * - batchId: String（批次ID，可选）
 * - usedBy: String（绑定的用户ID，首次使用时设置）
 * - isUsed: Boolean（是否已被使用/绑定，默认false）
 * - createdAt: Date（创建时间）
 * 
 * UserRedeemRecords表（用户兑换记录）：
 * - userId: String（用户ID，TapTap unionId）
 * - code: String（兑换码）
 * - type: String（类型：gm/supporter）
 * - redeemedAt: Date（兑换时间）
 */
object LeanCloudRedeemCodeManager {
    
    private const val TAG = "LeanCloudRedeemCode"
    
    // 网络超时配置
    private const val NETWORK_TIMEOUT_MS = 10000L // 10秒超时
    
    /**
     * 兑换码数据模型
     */
    data class RedeemCodeData(
        val code: String,
        val type: String,  // "gm", "supporter"
        val batchId: String? = null,
        val usedBy: String? = null,  // 绑定的用户ID
        val isUsed: Boolean = false,  // 是否已被使用
        val createdAt: Date? = null,
        val objectId: String? = null  // LeanCloud对象ID，用于更新
    )
    
    /**
     * 用户兑换记录数据模型
     */
    data class UserRedeemRecord(
        val userId: String,
        val code: String,
        val type: String,
        val redeemedAt: Date
    )
    
    /**
     * 验证兑换码是否存在且有效
     * @param code 兑换码
     * @return RedeemCodeData 如果兑换码有效，返回兑换码数据；否则返回null
     */
    suspend fun validateRedeemCode(code: String): RedeemCodeData? = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "开始验证兑换码: $code")
                
                // 查询兑换码表
                val query = LCQuery<LCObject>(LeanCloudConfig.TABLE_REDEEM_CODES)
                query.whereEqualTo("code", code)
                
                val results = query.find()
                
                if (results.isEmpty()) {
                    Log.w(TAG, "兑换码不存在: $code")
                    return@withTimeoutOrNull null
                }
                
                val redeemCodeObj = results[0]
                val typeValue = redeemCodeObj.getString("type")
                
                // 检查 type 字段是否存在且不为空
                if (typeValue.isNullOrBlank()) {
                    Log.e(TAG, "❌ 兑换码缺少 type 字段: $code")
                    Log.e(TAG, "请在 LeanCloud 控制台中为该兑换码添加 type 字段（gm/supporter/special）")
                    return@withTimeoutOrNull null
                }
                
                val codeData = RedeemCodeData(
                    code = redeemCodeObj.getString("code") ?: code,
                    type = typeValue,
                    batchId = redeemCodeObj.getString("batchId"),
                    usedBy = redeemCodeObj.getString("usedBy"),
                    isUsed = redeemCodeObj.getBoolean("isUsed"),
                    createdAt = redeemCodeObj.createdAt,
                    objectId = redeemCodeObj.objectId
                )
                
                Log.d(TAG, "✅ 兑换码验证成功: $code, 类型: ${codeData.type}")
                return@withTimeoutOrNull codeData
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 验证兑换码失败: $code", e)
                null
            }
        }
    }
    
    /**
     * 检查兑换码归属（首次绑定机制）
     * @param code 兑换码
     * @param userId 用户ID（TapTap unionId）
     * @return CodeOwnership 归属状态
     */
    suspend fun checkCodeOwnership(code: String, userId: String): CodeOwnership = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "检查兑换码归属: code=$code, userId=$userId")
                
                val codeData = validateRedeemCode(code)
                if (codeData == null) {
                    Log.w(TAG, "兑换码不存在")
                    return@withTimeoutOrNull CodeOwnership.NotFound
                }
                
                val usedBy = codeData.usedBy
                val isUsed = codeData.isUsed
                
                when {
                    usedBy.isNullOrBlank() && !isUsed -> {
                        // 未绑定，首次使用
                        Log.d(TAG, "✅ 兑换码未绑定，可以使用")
                        CodeOwnership.Available
                    }
                    usedBy == userId -> {
                        // 已绑定到当前用户
                        Log.d(TAG, "✅ 兑换码已绑定到当前用户，可以使用")
                        CodeOwnership.OwnedByCurrentUser
                    }
                    else -> {
                        // 已被其他用户绑定
                        Log.w(TAG, "❌ 兑换码已被其他用户绑定")
                        CodeOwnership.OwnedByOthers(usedBy ?: "未知用户")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 检查兑换码归属失败", e)
                CodeOwnership.Error
            }
        } ?: CodeOwnership.Error
    }
    
    /**
     * 绑定兑换码到用户（首次使用时调用）
     * @param code 兑换码
     * @param userId 用户ID
     * @return Boolean true-绑定成功，false-绑定失败
     */
    suspend fun bindCodeToUser(code: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "开始绑定兑换码: code=$code, userId=$userId")
                
                // 获取兑换码对象
                val query = LCQuery<LCObject>(LeanCloudConfig.TABLE_REDEEM_CODES)
                query.whereEqualTo("code", code)
                val results = query.find()
                
                if (results.isEmpty()) {
                    Log.e(TAG, "❌ 兑换码不存在，无法绑定")
                    return@withTimeoutOrNull false
                }
                
                val redeemCodeObj = results[0]
                
                // 更新 usedBy 和 isUsed 字段
                redeemCodeObj.put("usedBy", userId)
                redeemCodeObj.put("isUsed", true)
                redeemCodeObj.save()
                
                Log.d(TAG, "✅ 兑换码绑定成功")
                return@withTimeoutOrNull true
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 绑定兑换码失败", e)
                false
            }
        } ?: false
    }
    
    /**
     * 检查用户是否已使用过该兑换码（保留用于向后兼容）
     * @param userId 用户ID（TapTap unionId）
     * @param code 兑换码
     * @return Boolean? true-已使用，false-未使用，null-查询出错
     */
    suspend fun hasUserUsedCode(userId: String, code: String): Boolean? = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "检查用户是否已使用兑换码: userId=$userId, code=$code")
                
                val query = LCQuery<LCObject>(LeanCloudConfig.TABLE_USER_REDEEM)
                query.whereEqualTo("userId", userId)
                query.whereEqualTo("code", code)
                
                val count = query.count()
                val hasUsed = count > 0
                
                Log.d(TAG, "用户兑换码使用状态: $hasUsed")
                return@withTimeoutOrNull hasUsed
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 检查兑换码使用状态失败", e)
                // 返回null表示查询出错（可能是表不存在）
                null
            }
        } // 超时时返回null
    }
    
    /**
     * 记录用户使用兑换码
     * @param userId 用户ID（TapTap unionId）
     * @param code 兑换码
     * @param type 兑换码类型
     * @return Boolean true-记录成功，false-记录失败
     */
    suspend fun recordUserRedeem(userId: String, code: String, type: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "开始记录用户兑换: userId=$userId, code=$code, type=$type")
                
                // 创建兑换记录
                val record = LCObject(LeanCloudConfig.TABLE_USER_REDEEM)
                record.put("userId", userId)
                record.put("code", code)
                record.put("type", type)
                record.put("redeemedAt", Date())
                
                // 保存到云端
                record.save()
                
                Log.d(TAG, "✅ 用户兑换记录保存成功")
                return@withTimeoutOrNull true
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 记录用户兑换失败", e)
                false
            }
        } ?: false
    }
    
    /**
     * 获取用户所有已使用的兑换码
     * @param userId 用户ID（TapTap unionId）
     * @return List<UserRedeemRecord> 用户兑换记录列表
     */
    suspend fun getUserRedeemRecords(userId: String): List<UserRedeemRecord> = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "获取用户兑换记录: userId=$userId")
                
                val query = LCQuery<LCObject>(LeanCloudConfig.TABLE_USER_REDEEM)
                query.whereEqualTo("userId", userId)
                query.orderByDescending("redeemedAt")
                query.limit(100) // 最多获取100条记录
                
                val results = query.find()
                val records = results.map { obj ->
                    UserRedeemRecord(
                        userId = obj.getString("userId") ?: userId,
                        code = obj.getString("code") ?: "",
                        type = obj.getString("type") ?: "",
                        redeemedAt = obj.getDate("redeemedAt") ?: obj.createdAt
                    )
                }
                
                Log.d(TAG, "✅ 获取到 ${records.size} 条兑换记录")
                return@withTimeoutOrNull records
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 获取用户兑换记录失败", e)
                emptyList()
            }
        } ?: emptyList()
    }
    
    /**
     * 检查用户是否解锁GM功能
     * @param userId 用户ID（TapTap unionId）
     * @return Boolean true-已解锁，false-未解锁
     */
    suspend fun isGMUnlocked(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "检查用户GM功能解锁状态: userId=$userId")
                
                val query = LCQuery<LCObject>(LeanCloudConfig.TABLE_USER_REDEEM)
                query.whereEqualTo("userId", userId)
                query.whereEqualTo("type", "gm")
                
                val count = query.count()
                val isUnlocked = count > 0
                
                Log.d(TAG, "用户GM功能解锁状态: $isUnlocked")
                return@withTimeoutOrNull isUnlocked
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 检查GM功能解锁状态失败", e)
                false
            }
        } ?: false
    }
    
    /**
     * 检查用户是否解锁支持者功能
     * @param userId 用户ID（TapTap unionId）
     * @return Boolean true-已解锁，false-未解锁
     */
    suspend fun isSupporterUnlocked(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
            try {
                Log.d(TAG, "检查用户支持者功能解锁状态: userId=$userId")
                
                val query = LCQuery<LCObject>(LeanCloudConfig.TABLE_USER_REDEEM)
                query.whereEqualTo("userId", userId)
                query.whereEqualTo("type", "supporter")
                
                val count = query.count()
                val isUnlocked = count > 0
                
                Log.d(TAG, "用户支持者功能解锁状态: $isUnlocked")
                return@withTimeoutOrNull isUnlocked
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 检查支持者功能解锁状态失败", e)
                false
            }
        } ?: false
    }
    
    /**
     * 完整的兑换码兑换流程（首次绑定机制）
     * 1. 验证兑换码是否存在
     * 2. 检查兑换码归属（未绑定/当前用户/其他用户）
     * 3. 首次使用时绑定到当前用户
     * 4. 记录使用记录（可选，用于历史追踪）
     * 
     * @param userId 用户ID（TapTap unionId）
     * @param code 兑换码
     * @return RedeemResult 兑换结果
     */
    suspend fun redeemCode(userId: String, code: String): RedeemResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "========== 开始兑换码兑换流程（首次绑定机制） ==========")
            Log.d(TAG, "用户ID: $userId")
            Log.d(TAG, "兑换码: $code")
            
            // 1. 验证兑换码是否存在
            val codeData = validateRedeemCode(code)
            if (codeData == null) {
                Log.w(TAG, "❌ 兑换码不存在或无效")
                return@withContext RedeemResult.CodeNotFound
            }
            
            // 2. 检查兑换码归属
            val ownership = checkCodeOwnership(code, userId)
            when (ownership) {
                is CodeOwnership.NotFound -> {
                    Log.w(TAG, "❌ 兑换码不存在")
                    return@withContext RedeemResult.CodeNotFound
                }
                is CodeOwnership.OwnedByOthers -> {
                    Log.w(TAG, "❌ 兑换码已被其他用户使用")
                    return@withContext RedeemResult.UsedByOthers
                }
                is CodeOwnership.Error -> {
                    Log.e(TAG, "❌ 检查兑换码归属失败")
                    return@withContext RedeemResult.NetworkError
                }
                is CodeOwnership.Available -> {
                    // 3. 首次使用，绑定到当前用户
                    Log.d(TAG, "首次使用，绑定兑换码到用户")
                    val bound = bindCodeToUser(code, userId)
                    if (!bound) {
                        Log.e(TAG, "❌ 绑定兑换码失败")
                        return@withContext RedeemResult.RecordFailed
                    }
                }
                is CodeOwnership.OwnedByCurrentUser -> {
                    // 已绑定到当前用户，可以继续使用
                    Log.d(TAG, "兑换码已绑定到当前用户，继续使用")
                }
            }
            
            // 4. 记录使用记录（可选，用于历史追踪）
            val recorded = recordUserRedeem(userId, code, codeData.type)
            if (!recorded) {
                Log.w(TAG, "⚠️ 记录兑换历史失败（但兑换成功）")
            }
            
            Log.d(TAG, "✅ 兑换码兑换成功！类型: ${codeData.type}")
            Log.d(TAG, "========== 兑换码兑换流程完成 ==========")
            
            return@withContext RedeemResult.Success(codeData.type)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 兑换码兑换流程失败", e)
            return@withContext RedeemResult.NetworkError
        }
    }
    
    /**
     * 兑换码归属状态
     */
    sealed class CodeOwnership {
        object Available : CodeOwnership()  // 未绑定，可用
        object OwnedByCurrentUser : CodeOwnership()  // 已绑定到当前用户
        data class OwnedByOthers(val ownerId: String) : CodeOwnership()  // 已被其他用户绑定
        object NotFound : CodeOwnership()  // 兑换码不存在
        object Error : CodeOwnership()  // 查询出错
    }
    
    /**
     * 兑换结果
     */
    sealed class RedeemResult {
        data class Success(val type: String) : RedeemResult()  // 兑换成功
        object CodeNotFound : RedeemResult()  // 兑换码不存在
        object AlreadyUsed : RedeemResult()  // 已使用过（保留用于向后兼容）
        object UsedByOthers : RedeemResult()  // 已被其他用户使用（新增）
        object RecordFailed : RedeemResult()  // 记录失败
        object NetworkError : RedeemResult()  // 网络错误
    }
}
