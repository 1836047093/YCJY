package com.example.yjcy.taptap

import android.app.Activity
import android.util.Log
import com.taptap.sdk.compliance.TapTapCompliance
import com.taptap.sdk.compliance.bean.CheckPaymentResult
import com.taptap.sdk.kit.internal.callback.TapTapCallback
import com.taptap.sdk.kit.internal.exception.TapTapException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * TapTap合规认证管理器
 * 封装实名认证和防沉迷相关操作
 */
object TapComplianceManager {
    
    private const val TAG = "TapComplianceManager"
    
    /**
     * 开始合规认证
     * @param activity 当前Activity
     * @param userId 用户唯一标识（建议使用TapTap的unionId）
     */
    fun startup(activity: Activity, userId: String) {
        try {
            Log.d(TAG, "开始合规认证: userId=$userId")
            TapTapCompliance.startup(activity, userId)
        } catch (e: Exception) {
            Log.e(TAG, "开始合规认证失败: ${e.message}", e)
        }
    }
    
    /**
     * 退出合规认证
     */
    fun exit() {
        try {
            Log.d(TAG, "退出合规认证")
            TapTapCompliance.exit()
        } catch (e: Exception) {
            Log.e(TAG, "退出合规认证失败: ${e.message}", e)
        }
    }
    
    /**
     * 检查充值限制
     * @param activity 当前Activity
     * @param amount 充值金额（单位：分，100元=10000分）
     * @return 充值检查结果，true表示可以充值，false表示受限制
     */
    suspend fun checkPaymentLimit(activity: Activity, amount: Int): PaymentCheckResult {
        return suspendCancellableCoroutine { continuation ->
            TapTapCompliance.checkPaymentLimit(
                activity,
                amount,
                object : TapTapCallback<CheckPaymentResult> {
                    override fun onSuccess(result: CheckPaymentResult) {
                        val canPay = result.status
                        Log.d(TAG, "充值检查成功: amount=$amount, canPay=$canPay")
                        continuation.resume(
                            if (canPay) {
                                PaymentCheckResult.Allowed
                            } else {
                                PaymentCheckResult.Restricted("充值受限")
                            }
                        )
                    }
                    
                    override fun onFail(exception: TapTapException) {
                        Log.e(TAG, "充值检查失败: ${exception.message}", exception)
                        continuation.resume(
                            PaymentCheckResult.Error(exception.message ?: "未知错误")
                        )
                    }
                    
                    override fun onCancel() {
                        Log.d(TAG, "充值检查取消")
                        continuation.resume(PaymentCheckResult.Cancelled)
                    }
                }
            )
        }
    }
    
    /**
     * 上报充值金额
     * @param amount 充值金额（单位：分，100元=10000分）
     * @param orderId 订单ID（可选）
     */
    fun submitPayment(amount: Int, orderId: String? = null) {
        try {
            Log.d(TAG, "上报充值: amount=$amount, orderId=$orderId")
            TapTapCompliance.submitPayment(amount)
        } catch (e: Exception) {
            Log.e(TAG, "上报充值失败: ${e.message}", e)
        }
    }
    
    /**
     * 获取玩家年龄段
     * @return 年龄段下限，-1表示未知
     * 
     * 返回-1的可能原因：
     * 1. 用户未完成实名认证
     * 2. 初始化时useAgeRange设置为false
     * 3. 游戏无版号且在TapPlay中运行
     * 4. 用户首次进入游戏时未开启获取年龄段权限
     */
    fun getAgeRange(): Int {
        return try {
            val ageRange = TapTapCompliance.getAgeRange()
            Log.d(TAG, "获取年龄段: $ageRange")
            ageRange
        } catch (e: Exception) {
            Log.e(TAG, "获取年龄段失败: ${e.message}", e)
            -1
        }
    }
    
    /**
     * 获取剩余游戏时长
     * @return 剩余时长（单位：秒）
     */
    fun getRemainingTime(): Int {
        return try {
            val remainingTime = TapTapCompliance.getRemainingTime()
            Log.d(TAG, "获取剩余时长: ${remainingTime}秒")
            remainingTime
        } catch (e: Exception) {
            Log.e(TAG, "获取剩余时长失败: ${e.message}", e)
            0
        }
    }
    
    /**
     * 充值检查结果
     */
    sealed class PaymentCheckResult {
        /** 允许充值 */
        object Allowed : PaymentCheckResult()
        
        /** 充值受限 */
        data class Restricted(val reason: String) : PaymentCheckResult()
        
        /** 检查出错 */
        data class Error(val message: String) : PaymentCheckResult()
        
        /** 用户取消 */
        object Cancelled : PaymentCheckResult()
    }
}
