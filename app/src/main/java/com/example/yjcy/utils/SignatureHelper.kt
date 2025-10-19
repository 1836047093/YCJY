package com.example.yjcy.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import java.security.MessageDigest

/**
 * 签名工具类
 * 用于获取应用的MD5签名，用于TapTap SDK配置
 */
object SignatureHelper {
    
    /**
     * 获取应用的MD5签名
     * @return MD5签名字符串，例如：a08ec1ffd029837a1767e2ae0a26bfb6
     */
    fun getAppSignature(context: Context): String {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            if (signatures == null || signatures.isEmpty()) {
                return "无法获取签名"
            }
            
            return getSignatureMD5(signatures[0])
        } catch (e: Exception) {
            Log.e("SignatureHelper", "获取签名失败", e)
            return "获取失败: ${e.message}"
        }
    }
    
    /**
     * 计算签名的MD5值
     */
    private fun getSignatureMD5(signature: Signature): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(signature.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 获取包名和签名信息（用于日志输出）
     */
    fun logAppInfo(context: Context) {
        val packageName = context.packageName
        val signature = getAppSignature(context)
        Log.i("SignatureHelper", "包名: $packageName")
        Log.i("SignatureHelper", "签名: $signature")
        Log.i("SignatureHelper", "===== 复制以下信息到TapTap开发者后台 =====")
        Log.i("SignatureHelper", "包名: $packageName")
        Log.i("SignatureHelper", "签名: $signature")
    }
}
