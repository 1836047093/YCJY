package com.example.yjcy.ui.taptap

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yjcy.taptap.TapComplianceManager
import com.example.yjcy.taptap.TapDBManager
import com.example.yjcy.taptap.TapLoginManager
import com.taptap.sdk.login.TapTapAccount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * TapTap登录界面ViewModel
 */
class TapLoginViewModel : ViewModel() {
    
    var loginState by mutableStateOf<LoginState>(LoginState.NotLoggedIn)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var complianceInfo by mutableStateOf<ComplianceInfo?>(null)
        private set
    
    sealed class LoginState {
        object NotLoggedIn : LoginState()
        data class LoggedIn(val account: TapTapAccount) : LoginState()
    }
    
    /**
     * 合规认证信息
     */
    data class ComplianceInfo(
        val ageRange: Int, // 年龄段，-1表示未知
        val remainingTime: Int // 剩余时长（秒）
    )
    
    init {
        // 延迟检查登录状态，避免在SDK初始化前调用TapSDK功能
        // checkLoginState()
    }
    
    /**
     * 检查登录状态
     */
    fun checkLoginState() {
        try {
            val account = TapLoginManager.getCurrentAccount()
            loginState = if (account != null) {
                LoginState.LoggedIn(account)
            } else {
                LoginState.NotLoggedIn
            }
        } catch (e: Exception) {
            android.util.Log.w("TapLoginViewModel", "检查登录状态失败（SDK可能未初始化）: ${e.message}")
            loginState = LoginState.NotLoggedIn
        }
    }
    
    /**
     * 执行登录
     */
    fun login(activity: Activity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                android.util.Log.d("TapLoginScreen", "========== 开始登录流程 ==========")
                when (val result = TapLoginManager.loginWithBasicProfile(activity)) {
                    is TapLoginManager.LoginResult.Success -> {
                        android.util.Log.d("TapLoginScreen", "✅ 登录成功回调收到")
                        android.util.Log.d("TapLoginScreen", "账户信息: name=${result.account.name}, unionId=${result.account.unionId}, openId=${result.account.openId}")
                        
                        loginState = LoginState.LoggedIn(result.account)
                        
                        // 登录成功后设置TapDB账号ID
                        val unionId = result.account.unionId
                        android.util.Log.d("TapLoginScreen", "unionId检查: ${if (unionId.isNullOrEmpty()) "为空或null" else "有值，长度=${unionId.length}"}")
                        
                        if (!unionId.isNullOrEmpty()) {
                            android.util.Log.d("TapLoginScreen", "准备调用TapDBManager.setUser，unionId=$unionId")
                            TapDBManager.setUser(activity, unionId)
                            android.util.Log.d("TapLoginScreen", "TapDBManager.setUser调用完成")
                        } else {
                            android.util.Log.w("TapLoginScreen", "⚠️ unionId为空，跳过TapDB设置账号")
                        }
                        
                        // 登录成功后自动触发合规认证
                        if (!unionId.isNullOrEmpty()) {
                            android.util.Log.d("TapLoginScreen", "登录成功，准备启动合规认证: $unionId")
                            // 直接调用合规认证，由合规管理器处理可能的初始化问题
                            TapComplianceManager.startup(activity, unionId)
                            onResult(true, "登录成功！")
                            
                            // 等待一小段时间后刷新合规信息
                            delay(2000)
                            refreshComplianceInfo()
                        } else {
                            onResult(true, "登录成功！")
                        }
                        android.util.Log.d("TapLoginScreen", "========== 登录流程完成 ==========")
                    }
                    is TapLoginManager.LoginResult.Error -> {
                        android.util.Log.e("TapLoginScreen", "❌ 登录失败: ${result.exception.message}")
                        onResult(false, "登录失败: ${result.exception.message}")
                    }
                    TapLoginManager.LoginResult.Cancelled -> {
                        android.util.Log.d("TapLoginScreen", "用户取消登录")
                        onResult(false, "用户取消登录")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TapLoginScreen", "登录异常: ${e.message}", e)
                onResult(false, "登录异常: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * 登出
     */
    fun logout(context: android.content.Context, onResult: () -> Unit) {
        // 退出合规认证
        TapComplianceManager.exit()
        
        // 清除TapDB账号ID（需要在登出TapTap之前调用）
        TapDBManager.clearUser(context)
        
        // 登出 TapTap
        TapLoginManager.logout()
        
        // 清空状态
        loginState = LoginState.NotLoggedIn
        complianceInfo = null
        
        onResult()
    }
    
    /**
     * 刷新合规认证信息
     */
    fun refreshComplianceInfo() {
        val ageRange = TapComplianceManager.getAgeRange()
        val remainingTime = TapComplianceManager.getRemainingTime()
        complianceInfo = ComplianceInfo(ageRange, remainingTime)
    }
}

/**
 * TapTap登录界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TapLoginScreen(
    modifier: Modifier = Modifier,
    viewModel: TapLoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var showSnackbar by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示Snackbar
    LaunchedEffect(showSnackbar) {
        showSnackbar?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            showSnackbar = null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TapTap 登录") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 登录状态显示
                when (val state = viewModel.loginState) {
                    is TapLoginViewModel.LoginState.NotLoggedIn -> {
                        NotLoggedInContent(
                            isLoading = viewModel.isLoading,
                            onLoginClick = {
                                activity?.let { act ->
                                    viewModel.login(act) { success, message ->
                                        showSnackbar = message
                                    }
                                } ?: run {
                                    showSnackbar = "无法获取Activity"
                                }
                            }
                        )
                    }
                    is TapLoginViewModel.LoginState.LoggedIn -> {
                        LoggedInContent(
                            account = state.account,
                            complianceInfo = viewModel.complianceInfo,
                            onLogoutClick = {
                                viewModel.logout(context) {
                                    showSnackbar = "已登出"
                                }
                            },
                            onRefreshCompliance = {
                                viewModel.refreshComplianceInfo()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 未登录状态的内容
 */
@Composable
private fun NotLoggedInContent(
    isLoading: Boolean,
    onLoginClick: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = null,
        modifier = Modifier.size(120.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "欢迎使用 TapTap 登录",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "使用 TapTap 账号快速登录",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(48.dp))
    
    Button(
        onClick = onLoginClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Login,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "使用 TapTap 登录",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // 权限说明
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "登录后将获得以下信息：",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• TapTap 昵称\n• TapTap 头像\n• 唯一标识ID",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 已登录状态的内容
 */
@Composable
private fun LoggedInContent(
    account: TapTapAccount,
    complianceInfo: TapLoginViewModel.ComplianceInfo?,
    onLogoutClick: () -> Unit,
    onRefreshCompliance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "登录成功！",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 用户信息
            UserInfoRow(label = "昵称", value = account.name ?: "未知")
            Spacer(modifier = Modifier.height(8.dp))
            UserInfoRow(label = "Union ID", value = account.unionId ?: "未知")
            Spacer(modifier = Modifier.height(8.dp))
            UserInfoRow(label = "Open ID", value = account.openId ?: "未知")
            
            if (!account.avatar.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                UserInfoRow(label = "头像", value = account.avatar ?: "")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // 合规认证信息卡片
    complianceInfo?.let { info ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 合规认证信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    TextButton(onClick = onRefreshCompliance) {
                        Text("刷新")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 年龄段信息
                ComplianceInfoRow(
                    label = "年龄段",
                    value = if (info.ageRange >= 0) {
                        "${info.ageRange}岁以上"
                    } else {
                        "未知（未完成实名认证）"
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 剩余时长信息
                ComplianceInfoRow(
                    label = "剩余时长",
                    value = formatRemainingTime(info.remainingTime)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    OutlinedButton(
        onClick = onLogoutClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "登出",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * 合规认证信息行
 */
@Composable
private fun ComplianceInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * 格式化剩余时长
 */
private fun formatRemainingTime(seconds: Int): String {
    if (seconds <= 0) return "已用完或无限制"
    
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    
    return when {
        hours > 0 -> "${hours}小时${minutes}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "${seconds}秒"
    }
}

/**
 * 用户信息行
 */
@Composable
private fun UserInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
