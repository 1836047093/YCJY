package com.example.yjcy.taptap

import android.app.Activity
import android.util.Log
import com.taptap.sdk.kit.internal.callback.TapTapCallback
import com.taptap.sdk.kit.internal.exception.TapTapException
import com.taptap.sdk.login.Scopes
import com.taptap.sdk.login.TapTapAccount
import com.taptap.sdk.login.TapTapLogin
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * TapTap登录管理器
 * 封装TapTap登录相关操作
 */
object TapLoginManager {
    
    private const val TAG = "TapLoginManager"
    
    /**
     * 登录结果
     */
    sealed class LoginResult {
        data class Success(val account: TapTapAccount) : LoginResult()
        data class Error(val exception: TapTapException) : LoginResult()
        object Cancelled : LoginResult()
    }
    
    /**
     * 使用基础权限登录
     * 包含用户基本信息：昵称、头像
     */
    suspend fun loginWithBasicProfile(activity: Activity): LoginResult {
        return loginWithScopes(activity, arrayOf(Scopes.SCOPE_PUBLIC_PROFILE))
    }
    
    /**
     * 使用指定权限登录
     * @param scopes 权限列表
     * - Scopes.SCOPE_PUBLIC_PROFILE: 获得 TapTap 基本信息（昵称、头像）
     * - Scopes.SCOPE_USER_FRIENDS: 获得访问 TapTap 好友相关数据的权限
     * - Scopes.SCOPE_BASIC_INFO: 获得 TapTap 更详细的基本信息，包括性别、地区等
     */
    suspend fun loginWithScopes(activity: Activity, scopes: Array<String>): LoginResult {
        return suspendCancellableCoroutine { continuation ->
            TapTapLogin.loginWithScopes(
                activity,
                scopes,
                object : TapTapCallback<TapTapAccount> {
                    override fun onSuccess(result: TapTapAccount) {
                        Log.d(TAG, "登录成功: unionId=${result.unionId}, name=${result.name}")
                        continuation.resume(LoginResult.Success(result))
                    }
                    
                    override fun onCancel() {
                        Log.d(TAG, "用户取消登录")
                        continuation.resume(LoginResult.Cancelled)
                    }
                    
                    override fun onFail(exception: TapTapException) {
                        Log.e(TAG, "登录失败: ${exception.message}", exception)
                        continuation.resume(LoginResult.Error(exception))
                    }
                }
            )
        }
    }
    
    /**
     * 获取当前登录的账号信息
     * @return 当前登录的账号，未登录则返回null
     */
    fun getCurrentAccount(): TapTapAccount? {
        return try {
            TapTapLogin.getCurrentTapAccount()
        } catch (e: Exception) {
            Log.e(TAG, "获取当前账号失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return getCurrentAccount() != null
    }
    
    /**
     * 登出
     */
    fun logout() {
        try {
            TapTapLogin.logout()
            Log.d(TAG, "登出成功")
        } catch (e: Exception) {
            Log.e(TAG, "登出失败: ${e.message}", e)
        }
    }
    
    /**
     * 获取用户信息摘要（用于显示）
     */
    fun getUserDisplayInfo(): String {
        val account = getCurrentAccount()
        return if (account != null) {
            "用户: ${account.name}\nID: ${account.unionId}"
        } else {
            "未登录"
        }
    }
}
