package com.example.yjcy.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Firebase 兑换码管理器
 * 使用 Firestore 云端存储兑换码使用记录，实现跨设备同步和防作弊
 */
object FirebaseRedeemCodeManager {
    
    private const val TAG = "FirebaseRedeemCodeManager"
    private const val COLLECTION_USER_CODES = "user_redeem_codes"
    private const val COLLECTION_CODES = "redeemCodes"  // 匹配管理后台的集合名
    
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    
    /**
     * 清理用户ID，移除Firestore不允许的字符
     * Firestore文档ID不能包含：/ \ ? # [ ]
     * @param userId 原始用户ID（可为null）
     * @return 清理后的用户ID（可用于Firestore文档ID），如果输入为null则返回空字符串
     */
    private fun sanitizeUserId(userId: String?): String {
        if (userId.isNullOrBlank()) {
            return ""
        }
        // 替换Firestore不允许的字符为下划线
        return userId
            .replace("/", "_")
            .replace("\\", "_")
            .replace("?", "_")
            .replace("#", "_")
            .replace("[", "_")
            .replace("]", "_")
    }
    
    /**
     * 用户兑换码数据模型
     */
    data class UserRedeemData(
        val userId: String = "",
        val usedCodes: List<String> = emptyList(),
        val gmModeUnlocked: Boolean = false,
        val supporterUnlocked: Boolean = false,
        val lastUpdated: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
    )
    
    /**
     * 兑换码奖励数据模型（匹配管理后台的数据结构）
     */
    data class RedeemCodeReward(
        val featureId: String = "",
        val featureName: String = "",
        val type: String = "",  // "supporter", "gm"
        val status: String = "unused"  // "unused", "used"
    )
    
    /**
     * 兑换码数据模型（用于全局兑换码状态跟踪）
     * 匹配管理后台的数据结构
     */
    data class RedeemCodeData(
        val code: String = "",
        val reward: RedeemCodeReward? = null,
        val batchId: String? = null,
        val createdAt: com.google.firebase.Timestamp? = null,
        // 兼容旧格式的字段
        val type: String = "",  // "gm", "supporter" (兼容旧数据)
        val isValid: Boolean = true,
        val maxUses: Int = 1,
        val usedCount: Int = 0,
        val usedByUserId: String? = null,
        val usedAt: com.google.firebase.Timestamp? = null
    )
    
    /**
     * 从Firestore查询兑换码（支持新格式和旧格式）
     * @param code 兑换码
     * @return 兑换码数据，如果不存在返回null
     */
    suspend fun getRedeemCodeFromFirestore(code: String): RedeemCodeData? {
        val upperCode = code.trim().uppercase()
        val originalCode = code.trim() // 保留原始格式（可能包含大小写和连字符）
        
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始查询兑换码: 原始='$code', 大写='$upperCode'")
                
                // 先尝试用code作为文档ID查询（兼容旧格式）
                val codeDoc = db.collection(COLLECTION_CODES)
                    .document(upperCode)
                    .get()
                    .await()
                
                if (codeDoc.exists()) {
                    val data = codeDoc.toObject(RedeemCodeData::class.java)
                    Log.d(TAG, "✅ 通过文档ID找到兑换码: code=$upperCode, reward=${data?.reward}, status=${data?.reward?.status}")
                    return@withContext data
                }
                
                // 如果文档ID不匹配，尝试用code字段查询（管理后台使用随机文档ID）
                // 先尝试大写格式
                var querySnapshot = db.collection(COLLECTION_CODES)
                    .whereEqualTo("code", upperCode)
                    .limit(1)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val data = doc.toObject(RedeemCodeData::class.java)
                    Log.d(TAG, "✅ 通过code字段找到兑换码(大写): code=$upperCode, docId=${doc.id}")
                    return@withContext data
                }
                
                // 再尝试原始格式（可能包含大小写和连字符）
                if (originalCode != upperCode) {
                    querySnapshot = db.collection(COLLECTION_CODES)
                        .whereEqualTo("code", originalCode)
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]
                        val data = doc.toObject(RedeemCodeData::class.java)
                        Log.d(TAG, "✅ 通过code字段找到兑换码(原始格式): code=$originalCode, docId=${doc.id}")
                        return@withContext data
                    }
                }
                
                // 调试：查询所有兑换码看看数据结构
                Log.w(TAG, "❌ 兑换码不存在，尝试查询集合中的所有文档...")
                val allDocs = db.collection(COLLECTION_CODES)
                    .limit(5)
                    .get()
                    .await()
                
                Log.d(TAG, "集合中共有 ${allDocs.size()} 个文档（仅显示前5个）")
                allDocs.documents.forEach { doc ->
                    val data = doc.toObject(RedeemCodeData::class.java)
                    Log.d(TAG, "  文档ID: ${doc.id}, code字段: ${data?.code}, reward: ${data?.reward}")
                }
                
                Log.d(TAG, "兑换码不存在: $upperCode (已尝试大写和原始格式)")
                null
            } catch (e: Exception) {
                Log.e(TAG, "查询兑换码失败", e)
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 检查兑换码是否已被任何用户使用（全局唯一验证）
     * @param code 兑换码
     * @return true表示已被使用，false表示未被使用
     */
    suspend fun isCodeUsedGlobally(code: String): Boolean {
        val redeemCodeData = getRedeemCodeFromFirestore(code)
        
        return when {
            redeemCodeData == null -> {
                Log.d(TAG, "兑换码不存在: $code")
                false
            }
            // 新格式：检查reward.status
            redeemCodeData.reward != null -> {
                val isUsed = redeemCodeData.reward.status == "used"
                Log.d(TAG, "全局检查兑换码(新格式): code=$code, status=${redeemCodeData.reward.status}, isUsed=$isUsed")
                isUsed
            }
            // 旧格式：检查usedCount
            else -> {
                val isUsed = (redeemCodeData.usedCount ?: 0) > 0
                Log.d(TAG, "全局检查兑换码(旧格式): code=$code, usedCount=${redeemCodeData.usedCount}, isUsed=$isUsed")
                isUsed
            }
        }
    }
    
    /**
     * 检查用户是否已使用过指定的兑换码
     * @param userId TapTap unionId 或 openId
     * @param code 兑换码
     * @return true表示已使用过，false表示未使用
     */
    suspend fun isCodeUsedByUser(userId: String?, code: String): Boolean {
        if (userId.isNullOrBlank()) {
            Log.w(TAG, "用户ID为空，无法检查兑换码使用记录")
            return false
        }
        
        return withContext(Dispatchers.IO) {
            try {
                // 清理用户ID，移除Firestore不允许的字符
                val sanitizedUserId = sanitizeUserId(userId)
                Log.d(TAG, "原始userId: $userId, 清理后: $sanitizedUserId")
                
                val document = db.collection(COLLECTION_USER_CODES)
                    .document(sanitizedUserId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val data = document.toObject(UserRedeemData::class.java)
                    val isUsed = data?.usedCodes?.contains(code.uppercase()) ?: false
                    Log.d(TAG, "检查兑换码: userId=$userId, code=$code, isUsed=$isUsed")
                    isUsed
                } else {
                    Log.d(TAG, "用户无兑换码记录: userId=$userId")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查兑换码失败", e)
                // 出错时返回false，允许用户尝试使用
                false
            }
        }
    }
    
    /**
     * 标记兑换码为已使用
     * @param userId TapTap unionId 或 openId
     * @param code 兑换码
     * @param codeType 兑换码类型（"gm", "supporter"），如果为null则从数据库读取
     * @return true表示成功标记，false表示失败
     */
    suspend fun markCodeAsUsed(
        userId: String?,
        code: String,
        codeType: String? = null
    ): Boolean {
        if (userId.isNullOrBlank()) {
            Log.e(TAG, "用户ID为空，无法标记兑换码")
            return false
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val upperCode = code.trim().uppercase()
                
                // 1. 先查询兑换码数据
                val existingCodeData = getRedeemCodeFromFirestore(upperCode)
                if (existingCodeData == null) {
                    Log.e(TAG, "兑换码不存在: $upperCode")
                    return@withContext false
                }
                
                // 检查是否已被其他用户使用
                val isUsed = when {
                    existingCodeData.reward != null -> existingCodeData.reward.status == "used"
                    else -> (existingCodeData.usedCount ?: 0) > 0
                }
                
                if (isUsed) {
                    val usedBy = existingCodeData.usedByUserId
                    if (usedBy != null && usedBy != userId) {
                        Log.w(TAG, "兑换码已被其他用户使用: code=$upperCode, usedBy=$usedBy, currentUser=$userId")
                        return@withContext false
                    }
                }
                
                // 确定兑换码类型
                val finalCodeType = codeType ?: existingCodeData.reward?.type ?: existingCodeData.type
                if (finalCodeType.isBlank()) {
                    Log.e(TAG, "无法确定兑换码类型: $upperCode")
                    return@withContext false
                }
                
                // 2. 使用事务确保原子性操作（所有读取必须在写入之前）
                // 注意：如果文档ID不是code本身，需要先查询找到文档ID
                val sanitizedUserId = sanitizeUserId(userId)
                val userDocRef = db.collection(COLLECTION_USER_CODES).document(sanitizedUserId)
                
                // 先查询找到正确的文档引用（因为文档ID可能是随机生成的）
                val codeQuerySnapshot = db.collection(COLLECTION_CODES)
                    .whereEqualTo("code", upperCode)
                    .limit(1)
                    .get()
                    .await()
                
                if (codeQuerySnapshot.isEmpty) {
                    // 如果查询不到，尝试用code作为文档ID
                    val codeDocRef = db.collection(COLLECTION_CODES).document(upperCode)
                    val codeDoc = codeDocRef.get().await()
                    if (!codeDoc.exists()) {
                        Log.e(TAG, "兑换码文档不存在: $upperCode")
                        return@withContext false
                    }
                    
                    // 使用code作为文档ID的情况
                    db.runTransaction { transaction ->
                        // 先执行所有读取操作
                        val codeSnapshot = transaction.get(codeDocRef)
                        val userSnapshot = transaction.get(userDocRef)
                        
                        // 检查兑换码状态
                        val codeData = if (codeSnapshot.exists()) {
                            codeSnapshot.toObject(RedeemCodeData::class.java)
                        } else {
                            null
                        }
                        
                        if (codeData == null) {
                            throw Exception("兑换码不存在")
                        }
                        
                        // 检查是否已被其他用户使用
                        val alreadyUsed = when {
                            codeData.reward != null -> codeData.reward.status == "used"
                            else -> (codeData.usedCount ?: 0) > 0
                        }
                        
                        if (alreadyUsed) {
                            val usedBy = codeData.usedByUserId
                            if (usedBy != null && usedBy != userId) {
                                throw Exception("兑换码已被其他用户使用")
                            }
                        }
                        
                        // 现在执行写入操作
                        val updatedReward = if (codeData.reward != null) {
                            codeData.reward.copy(status = "used")
                        } else {
                            null
                        }
                        
                        val updatedCodeData = RedeemCodeData(
                            code = upperCode,
                            reward = updatedReward,
                            batchId = codeData.batchId,
                            createdAt = codeData.createdAt ?: com.google.firebase.Timestamp.now(),
                            type = finalCodeType,
                            isValid = true,
                            maxUses = 1,
                            usedCount = 1,
                            usedByUserId = userId,
                            usedAt = com.google.firebase.Timestamp.now()
                        )
                        transaction.set(codeDocRef, updatedCodeData, SetOptions.merge())
                        
                        // 更新用户兑换码记录
                        val existingUserData = if (userSnapshot.exists()) {
                            userSnapshot.toObject(UserRedeemData::class.java)
                        } else {
                            null
                        }
                        
                        val usedCodes = (existingUserData?.usedCodes ?: emptyList()).toMutableList()
                        if (!usedCodes.contains(upperCode)) {
                            usedCodes.add(upperCode)
                        }
                        
                        val newUserData = UserRedeemData(
                            userId = userId ?: "", // userId可能为null，需要处理
                            usedCodes = usedCodes,
                            gmModeUnlocked = existingUserData?.gmModeUnlocked ?: (finalCodeType == "gm"),
                            supporterUnlocked = existingUserData?.supporterUnlocked ?: (finalCodeType == "supporter"),
                            lastUpdated = com.google.firebase.Timestamp.now()
                        )
                        transaction.set(userDocRef, newUserData, SetOptions.merge())
                    }.await()
                } else {
                    // 使用查询到的文档ID
                    val codeDocId = codeQuerySnapshot.documents[0].id
                    val codeDocRef = db.collection(COLLECTION_CODES).document(codeDocId)
                    
                    db.runTransaction { transaction ->
                        // 先执行所有读取操作
                        val codeSnapshot = transaction.get(codeDocRef)
                        val userSnapshot = transaction.get(userDocRef)
                        
                        // 检查兑换码状态
                        val codeData = if (codeSnapshot.exists()) {
                            codeSnapshot.toObject(RedeemCodeData::class.java)
                        } else {
                            null
                        }
                        
                        if (codeData == null) {
                            throw Exception("兑换码不存在")
                        }
                        
                        // 检查是否已被其他用户使用
                        val alreadyUsed = when {
                            codeData.reward != null -> codeData.reward.status == "used"
                            else -> (codeData.usedCount ?: 0) > 0
                        }
                        
                        if (alreadyUsed) {
                            val usedBy = codeData.usedByUserId
                            if (usedBy != null && usedBy != userId) {
                                throw Exception("兑换码已被其他用户使用")
                            }
                        }
                        
                        // 现在执行写入操作
                        // 更新兑换码状态（新格式）
                        val updatedReward = if (codeData.reward != null) {
                            codeData.reward.copy(status = "used")
                        } else {
                            null
                        }
                        
                        val updatedCodeData = RedeemCodeData(
                            code = upperCode,
                            reward = updatedReward,
                            batchId = codeData.batchId,
                            createdAt = codeData.createdAt ?: com.google.firebase.Timestamp.now(),
                            // 兼容旧格式
                            type = finalCodeType,
                            isValid = true,
                            maxUses = 1,
                            usedCount = 1,
                            usedByUserId = userId,
                            usedAt = com.google.firebase.Timestamp.now()
                        )
                        transaction.set(codeDocRef, updatedCodeData, SetOptions.merge())
                        
                        // 更新用户兑换码记录
                        val existingUserData = if (userSnapshot.exists()) {
                            userSnapshot.toObject(UserRedeemData::class.java)
                        } else {
                            null
                        }
                        
                        val usedCodes = (existingUserData?.usedCodes ?: emptyList()).toMutableList()
                        if (!usedCodes.contains(upperCode)) {
                            usedCodes.add(upperCode)
                        }
                        
                        val newUserData = UserRedeemData(
                            userId = userId ?: "", // userId可能为null，需要处理
                            usedCodes = usedCodes,
                            gmModeUnlocked = existingUserData?.gmModeUnlocked ?: (finalCodeType == "gm"),
                            supporterUnlocked = existingUserData?.supporterUnlocked ?: (finalCodeType == "supporter"),
                            lastUpdated = com.google.firebase.Timestamp.now()
                        )
                        transaction.set(userDocRef, newUserData, SetOptions.merge())
                    }.await()
                }
                
                Log.d(TAG, "标记兑换码成功（全局唯一）: userId=$userId, code=$code, type=$finalCodeType")
                true
            } catch (e: Exception) {
                Log.e(TAG, "标记兑换码失败", e)
                false
            }
        }
    }
    
    /**
     * 检查用户是否已解锁GM模式
     * @param userId TapTap unionId 或 openId
     * @return true表示已解锁，false表示未解锁
     */
    suspend fun isGMModeUnlocked(userId: String?): Boolean {
        return isCodeUsedByUser(userId, "PROGM")
    }
    
    /**
     * 获取用户已使用的所有兑换码列表
     * @param userId 用户ID
     * @return 已使用的兑换码集合
     */
    suspend fun getUserUsedCodes(userId: String?): Set<String> {
        if (userId.isNullOrBlank()) {
            return emptySet()
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val sanitizedUserId = sanitizeUserId(userId)
                val document = db.collection(COLLECTION_USER_CODES)
                    .document(sanitizedUserId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val data = document.toObject(UserRedeemData::class.java)
                    data?.usedCodes?.toSet() ?: emptySet()
                } else {
                    emptySet()
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取用户兑换码列表失败", e)
                emptySet()
            }
        }
    }
    
    /**
     * 检查用户是否已使用过任何支持者兑换码
     * @param userId 用户ID
     * @return true表示已使用过，false表示未使用
     */
    suspend fun hasUsedSupporterCode(userId: String?): Boolean {
        if (userId.isNullOrBlank()) {
            return false
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val sanitizedUserId = sanitizeUserId(userId)
                val document = db.collection(COLLECTION_USER_CODES)
                    .document(sanitizedUserId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val data = document.toObject(UserRedeemData::class.java)
                    data?.supporterUnlocked ?: false
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查支持者状态失败", e)
                false
            }
        }
    }
    
    /**
     * 验证兑换码是否存在且有效（从Firestore查询）
     * @param code 兑换码
     * @return 兑换码数据，如果不存在或无效返回null
     */
    suspend fun validateRedeemCode(code: String): RedeemCodeData? {
        val upperCode = code.trim().uppercase()
        Log.d(TAG, "验证兑换码: 原始输入='$code', 处理后='$upperCode'")
        
        val redeemCodeData = getRedeemCodeFromFirestore(upperCode)
        
        if (redeemCodeData == null) {
            Log.w(TAG, "兑换码不存在: $upperCode")
            return null
        }
        
        // 检查是否已被使用（redeemCodeData已经确认不为null）
        val isUsed = when {
            redeemCodeData.reward != null -> redeemCodeData.reward.status == "used"
            else -> (redeemCodeData.usedCount ?: 0) > 0
        }
        
        if (isUsed) {
            Log.w(TAG, "兑换码已被使用: $upperCode")
            return null
        }
        
        val codeType = redeemCodeData.reward?.type ?: redeemCodeData.type
        Log.d(TAG, "兑换码验证成功: code=$upperCode, type=$codeType")
        return redeemCodeData
    }
    
    /**
     * 验证兑换码是否是有效的支持者兑换码（兼容旧格式）
     * @param code 兑换码（会自动转换为大写并去除空格）
     * @return true表示是有效的支持者兑换码，false表示无效
     */
    suspend fun isValidSupporterCode(code: String): Boolean {
        val upperCode = code.trim().uppercase()
        
        // 先检查是否是旧格式 SUPPORTER001-150
        if (upperCode.startsWith("SUPPORTER")) {
            val numberPart = upperCode.removePrefix("SUPPORTER").trim()
            return try {
                val number = numberPart.toInt()
                val isValid = number in 1..150
                if (isValid) {
                    Log.d(TAG, "旧格式支持者兑换码验证成功: $upperCode")
                }
                isValid
            } catch (e: NumberFormatException) {
                false
            }
        }
        
        // 新格式：从Firestore查询
        val redeemCodeData = getRedeemCodeFromFirestore(upperCode)
        if (redeemCodeData == null) {
            Log.w(TAG, "兑换码验证失败: $upperCode (不存在)")
            return false
        }
        
        val isValid = (redeemCodeData.reward?.type == "supporter" || redeemCodeData.type == "supporter") &&
                     (redeemCodeData.reward?.status != "used" && (redeemCodeData.usedCount ?: 0) == 0)
        
        if (isValid) {
            Log.d(TAG, "新格式支持者兑换码验证成功: $upperCode")
        } else {
            Log.w(TAG, "兑换码验证失败: $upperCode (不存在或不是支持者兑换码)")
        }
        
        return isValid
    }
    
    /**
     * 检查用户是否已解锁支持者功能
     * @param userId 用户ID
     * @param usedRedeemCodes 存档中已使用的兑换码集合（用于向后兼容）
     * @return true表示已解锁，false表示未解锁
     */
    suspend fun isSupporterFeatureUnlocked(
        userId: String?,
        usedRedeemCodes: Set<String> = emptySet()
    ): Boolean {
        // 优先检查云端数据
        if (hasUsedSupporterCode(userId)) {
            return true
        }
        
        // 向后兼容：检查本地存档
        return usedRedeemCodes.any { it.startsWith("SUPPORTER", ignoreCase = true) }
    }
    
    /**
     * 从本地存档迁移到云端
     * @param userId 用户ID
     * @param localUsedCodes 本地存档中的兑换码列表
     */
    suspend fun migrateFromLocal(userId: String?, localUsedCodes: Set<String>): Boolean {
        if (userId.isNullOrBlank() || localUsedCodes.isEmpty()) {
            return false
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val sanitizedUserId = sanitizeUserId(userId)
                val docRef = db.collection(COLLECTION_USER_CODES).document(sanitizedUserId)
                
                // 检查是否已有云端数据
                val document = docRef.get().await()
                if (document.exists()) {
                    Log.d(TAG, "云端已有数据，跳过迁移")
                    return@withContext true
                }
                
                // 迁移数据
                val newData = UserRedeemData(
                    userId = userId ?: "", // 保存原始userId（如果为null则使用空字符串）
                    usedCodes = localUsedCodes.toList(),
                    gmModeUnlocked = localUsedCodes.contains("PROGM"),
                    supporterUnlocked = localUsedCodes.any { 
                        it.startsWith("SUPPORTER", ignoreCase = true) 
                    },
                    lastUpdated = com.google.firebase.Timestamp.now()
                )
                
                docRef.set(newData).await()
                Log.d(TAG, "本地数据迁移成功: userId=$userId, codes=${localUsedCodes.size}个")
                true
            } catch (e: Exception) {
                Log.e(TAG, "数据迁移失败", e)
                false
            }
        }
    }
    
    /**
     * 清除用户的所有兑换码记录（用于测试或重置）
     * @param userId 用户ID
     */
    suspend fun clearUserCodes(userId: String?): Boolean {
        if (userId.isNullOrBlank()) {
            return false
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val sanitizedUserId = sanitizeUserId(userId)
                db.collection(COLLECTION_USER_CODES)
                    .document(sanitizedUserId)
                    .delete()
                    .await()
                Log.d(TAG, "已清除用户兑换码记录: userId=$userId")
                true
            } catch (e: Exception) {
                Log.e(TAG, "清除用户兑换码记录失败", e)
                false
            }
        }
    }
    
    /**
     * 创建单个兑换码到Firestore（用于管理后台生成后同步）
     * @param code 兑换码
     * @param codeType 兑换码类型（"supporter", "gm", "special"等）
     * @param maxUses 最大使用次数（默认1，表示全局唯一）
     * @return 是否成功
     */
    suspend fun createRedeemCode(
        code: String,
        codeType: String = "supporter",
        maxUses: Int = 1
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val upperCode = code.trim().uppercase()
                
                val codeData = RedeemCodeData(
                    code = upperCode,
                    type = codeType,
                    isValid = true,
                    maxUses = maxUses,
                    usedCount = 0,
                    usedByUserId = null,
                    usedAt = null,
                    createdAt = com.google.firebase.Timestamp.now()
                )
                
                db.collection(COLLECTION_CODES)
                    .document(upperCode)
                    .set(codeData, SetOptions.merge())
                    .await()
                
                Log.d(TAG, "✅ 成功创建兑换码到Firestore: code=$upperCode, type=$codeType")
                true
            } catch (e: Exception) {
                Log.e(TAG, "创建兑换码失败", e)
                false
            }
        }
    }
    
    /**
     * 批量创建兑换码到Firestore（用于管理后台批量生成后同步）
     * @param codes 兑换码列表
     * @param codeType 兑换码类型
     * @param maxUses 最大使用次数
     * @return 成功创建的数量
     */
    suspend fun createRedeemCodesBatch(
        codes: List<String>,
        codeType: String = "supporter",
        maxUses: Int = 1
    ): Int {
        return withContext(Dispatchers.IO) {
            try {
                var successCount = 0
                val batch = db.batch()
                var batchCount = 0
                val maxBatchSize = 500
                
                codes.forEach { code ->
                    val upperCode = code.trim().uppercase()
                    val codeData = RedeemCodeData(
                        code = upperCode,
                        type = codeType,
                        isValid = true,
                        maxUses = maxUses,
                        usedCount = 0,
                        usedByUserId = null,
                        usedAt = null,
                        createdAt = com.google.firebase.Timestamp.now()
                    )
                    
                    val docRef = db.collection(COLLECTION_CODES).document(upperCode)
                    batch.set(docRef, codeData, SetOptions.merge())
                    batchCount++
                    
                    if (batchCount >= maxBatchSize) {
                        batch.commit().await()
                        successCount += batchCount
                        batchCount = 0
                        Log.d(TAG, "已同步 $successCount 个兑换码...")
                    }
                }
                
                if (batchCount > 0) {
                    batch.commit().await()
                    successCount += batchCount
                }
                
                Log.d(TAG, "✅ 成功批量创建 $successCount 个兑换码到Firestore")
                successCount
            } catch (e: Exception) {
                Log.e(TAG, "批量创建兑换码失败", e)
                0
            }
        }
    }
    
    /**
     * 批量初始化支持者兑换码到Firestore
     * 将 SUPPORTER001 到 SUPPORTER150 添加到 redeem_codes 集合
     * @return 成功初始化的数量
     */
    suspend fun initializeSupporterCodes(): Int {
        return withContext(Dispatchers.IO) {
            try {
                var successCount = 0
                val batch = db.batch()
                var batchCount = 0
                val maxBatchSize = 500 // Firestore批量操作限制
                
                // 生成 SUPPORTER001 到 SUPPORTER150
                for (i in 1..150) {
                    val code = "SUPPORTER${i.toString().padStart(3, '0')}" // SUPPORTER001, SUPPORTER002, ...
                    
                    val codeData = RedeemCodeData(
                        code = code,
                        type = "supporter",
                        isValid = true,
                        maxUses = 1, // 每个兑换码只能使用1次
                        usedCount = 0,
                        usedByUserId = null,
                        usedAt = null,
                        createdAt = com.google.firebase.Timestamp.now()
                    )
                    
                    val docRef = db.collection(COLLECTION_CODES).document(code)
                    batch.set(docRef, codeData, SetOptions.merge())
                    batchCount++
                    
                    // Firestore批量操作限制，每500个提交一次
                    if (batchCount >= maxBatchSize) {
                        batch.commit().await()
                        successCount += batchCount
                        batchCount = 0
                        Log.d(TAG, "已初始化 $successCount 个兑换码...")
                    }
                }
                
                // 提交剩余的
                if (batchCount > 0) {
                    batch.commit().await()
                    successCount += batchCount
                }
                
                Log.d(TAG, "✅ 成功初始化 $successCount 个支持者兑换码到Firestore")
                successCount
            } catch (e: Exception) {
                Log.e(TAG, "初始化兑换码失败", e)
                0
            }
        }
    }
    
    /**
     * 初始化GM模式兑换码到Firestore
     * @return 是否成功
     */
    suspend fun initializeGMCode(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val codeData = RedeemCodeData(
                    code = "PROGM",
                    type = "gm",
                    isValid = true,
                    maxUses = 1,
                    usedCount = 0,
                    usedByUserId = null,
                    usedAt = null,
                    createdAt = com.google.firebase.Timestamp.now()
                )
                
                db.collection(COLLECTION_CODES)
                    .document("PROGM")
                    .set(codeData, SetOptions.merge())
                    .await()
                
                Log.d(TAG, "✅ 成功初始化GM模式兑换码到Firestore")
                true
            } catch (e: Exception) {
                Log.e(TAG, "初始化GM兑换码失败", e)
                false
            }
        }
    }
    
    /**
     * 获取兑换码使用记录（用于查看哪些兑换码已被使用）
     * @param code 兑换码（可选，如果为null则获取所有兑换码）
     * @return 兑换码数据列表
     */
    suspend fun getRedeemCodeRecords(code: String? = null): List<RedeemCodeData> {
        return withContext(Dispatchers.IO) {
            try {
                val query = if (code != null) {
                    db.collection(COLLECTION_CODES)
                        .whereEqualTo("code", code.trim().uppercase())
                } else {
                    db.collection(COLLECTION_CODES)
                }
                
                val snapshot = query.get().await()
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RedeemCodeData::class.java)
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取兑换码记录失败", e)
                emptyList()
            }
        }
    }
    
    /**
     * 获取已使用的兑换码列表（用于查看哪些兑换码已被兑换）
     * @return 已使用的兑换码列表
     */
    suspend fun getUsedRedeemCodes(): List<RedeemCodeData> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = db.collection(COLLECTION_CODES)
                    .whereGreaterThan("usedCount", 0)
                    .get()
                    .await()
                
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RedeemCodeData::class.java)
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取已使用兑换码列表失败", e)
                emptyList()
            }
        }
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
        BATCH_TRAINING         // 员工一键培训功能
    }
}

