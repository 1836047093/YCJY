// 这是在 MainActivity.kt 中集成 Firebase 兑换码系统的示例代码
// 复制相关部分到您的 MainActivity.kt 中

// ============= 1. 导入必要的包 =============
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.yjcy.utils.FirebaseRedeemCodeManager
import kotlinx.coroutines.tasks.await

// ============= 2. 在 MainActivity 类中添加 =============
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // Firebase Auth 实例
    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化 Firebase 匿名认证
        lifecycleScope.launch {
            initFirebaseAuth()
        }
        
        // ... 其他代码
    }
    
    /**
     * 初始化 Firebase 匿名认证
     */
    private suspend fun initFirebaseAuth() {
        try {
            if (firebaseAuth.currentUser == null) {
                val result = firebaseAuth.signInAnonymously().await()
                Log.d("Firebase", "匿名登录成功: ${result.user?.uid}")
            } else {
                Log.d("Firebase", "已登录: ${firebaseAuth.currentUser?.uid}")
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Firebase 认证失败", e)
        }
    }
}

// ============= 3. 在 GameScreen 函数中集成 =============
@Composable
fun GameScreen(
    // ... 现有参数
) {
    // 获取 TapTap 用户 ID
    val tapTapAccount = TapLoginManager.getCurrentAccount()
    val userId = tapTapAccount?.unionId ?: tapTapAccount?.openId
    
    // 协程作用域
    val coroutineScope = rememberCoroutineScope()
    
    // 兑换码相关状态
    var redeemCode by remember { mutableStateOf("") }
    var showRedeemDialog by remember { mutableStateOf(false) }
    var showRedeemSuccessDialog by remember { mutableStateOf(false) }
    var showRedeemError by remember { mutableStateOf(false) }
    var redeemSuccessMessage by remember { mutableStateOf("") }
    var isCheckingCode by remember { mutableStateOf(false) }
    
    // ============= 4. 首次登录时迁移本地数据 =============
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                // 从本地获取兑换码
                val localCodes = RedeemCodeManager.getUserUsedCodes(userId)
                
                if (localCodes.isNotEmpty()) {
                    // 迁移到云端
                    val migrated = FirebaseRedeemCodeManager.migrateFromLocal(userId, localCodes)
                    if (migrated) {
                        Log.d("RedeemCode", "本地数据已迁移到云端")
                    }
                }
                
                // 同步云端状态
                val gmUnlocked = FirebaseRedeemCodeManager.isGMModeUnlocked(userId)
                if (gmUnlocked && !gmModeEnabled) {
                    onGMToggle(true)
                    Log.d("RedeemCode", "从云端恢复 GM 模式")
                }
                
            } catch (e: Exception) {
                Log.e("RedeemCode", "数据同步失败", e)
            }
        }
    }
    
    // ============= 5. 兑换码验证逻辑 =============
    
    /**
     * 处理兑换码输入
     */
    fun handleRedeemCode() {
        if (userId.isNullOrBlank()) {
            showRedeemError = true
            return
        }
        
        val codeUpper = redeemCode.trim().uppercase()
        
        if (codeUpper.isBlank()) {
            showRedeemError = true
            return
        }
        
        isCheckingCode = true
        
        coroutineScope.launch {
            try {
                // === 处理支持者兑换码 ===
                if (FirebaseRedeemCodeManager.isValidSupporterCode(codeUpper)) {
                    // 检查是否已使用
                    val isUsed = FirebaseRedeemCodeManager.isCodeUsedByUser(userId, codeUpper)
                    
                    if (isUsed) {
                        Log.d("RedeemCode", "✅ 兑换码已绑定到当前用户（云端）")
                        redeemSuccessMessage = "✅ 兑换成功！已解锁所有支持者功能"
                        showRedeemSuccessDialog = true
                    } else {
                        // 标记为已使用（云端）
                        val success = FirebaseRedeemCodeManager.markCodeAsUsed(
                            userId = userId,
                            code = codeUpper,
                            codeType = "supporter"
                        )
                        
                        if (success) {
                            Log.d("RedeemCode", "✅ 兑换成功（云端）")
                            
                            // 同时更新本地（向后兼容）
                            onUsedRedeemCodesUpdate(usedRedeemCodes + codeUpper)
                            RedeemCodeManager.markCodeAsUsed(userId, codeUpper)
                            
                            redeemCode = ""
                            redeemSuccessMessage = "✅ 兑换成功！已解锁所有支持者功能\n（已同步到云端）"
                            showRedeemSuccessDialog = true
                        } else {
                            Log.e("RedeemCode", "❌ 云端保存失败")
                            showRedeemError = true
                        }
                    }
                    
                    isCheckingCode = false
                    return@launch
                }
                
                // === 处理 GM 模式兑换码 ===
                if (codeUpper == "PROGM") {
                    // 检查是否已使用
                    val isUsed = FirebaseRedeemCodeManager.isCodeUsedByUser(userId, codeUpper)
                    
                    if (isUsed) {
                        // 账号已使用过，自动启用 GM 模式
                        if (!gmModeEnabled) {
                            onGMToggle(true)
                        }
                        redeemCode = ""
                        redeemSuccessMessage = "GM工具箱已激活！（账号已解锁，自动启用）"
                        showRedeemSuccessDialog = true
                    } else {
                        // 标记为已使用（云端）
                        val success = FirebaseRedeemCodeManager.markCodeAsUsed(
                            userId = userId,
                            code = codeUpper,
                            codeType = "gm"
                        )
                        
                        if (success) {
                            // 同时更新本地
                            RedeemCodeManager.markCodeAsUsed(userId, codeUpper)
                            onGMToggle(true)
                            
                            redeemCode = ""
                            redeemSuccessMessage = "GM工具箱已激活！\n（已同步到云端）"
                            showRedeemSuccessDialog = true
                        } else {
                            showRedeemError = true
                        }
                    }
                    
                    isCheckingCode = false
                    return@launch
                }
                
                // 无效的兑换码
                Log.w("RedeemCode", "❌ 兑换码无效")
                showRedeemError = true
                
            } catch (e: Exception) {
                Log.e("RedeemCode", "兑换码验证失败", e)
                showRedeemError = true
            } finally {
                isCheckingCode = false
            }
        }
    }
    
    // ============= 6. 兑换码输入对话框 =============
    if (showRedeemDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRedeemDialog = false
                redeemCode = ""
            },
            title = { Text("输入兑换码") },
            text = {
                Column {
                    OutlinedTextField(
                        value = redeemCode,
                        onValueChange = { redeemCode = it.uppercase() },
                        label = { Text("兑换码") },
                        singleLine = true,
                        enabled = !isCheckingCode
                    )
                    
                    if (isCheckingCode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "正在验证兑换码...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    if (userId.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ 请先登录 TapTap 账号",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { handleRedeemCode() },
                    enabled = !isCheckingCode && !userId.isNullOrBlank()
                ) {
                    Text(if (isCheckingCode) "验证中..." else "兑换")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRedeemDialog = false
                        redeemCode = ""
                    },
                    enabled = !isCheckingCode
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // ============= 7. 兑换成功对话框 =============
    if (showRedeemSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRedeemSuccessDialog = false },
            title = { Text("兑换成功") },
            text = { 
                Column {
                    Text(redeemSuccessMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💾 数据已保存到云端，可在任意设备使用",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showRedeemSuccessDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
    
    // ============= 8. 兑换失败对话框 =============
    if (showRedeemError) {
        AlertDialog(
            onDismissRequest = { showRedeemError = false },
            title = { Text("兑换失败") },
            text = { 
                Text("兑换码无效或网络错误，请检查后重试")
            },
            confirmButton = {
                Button(onClick = { showRedeemError = false }) {
                    Text("确定")
                }
            }
        )
    }
    
    // ============= 9. 在设置菜单中添加兑换码按钮 =============
    // 在您的设置界面中添加这个按钮：
    /*
    Button(
        onClick = { showRedeemDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_redeem), // 需要添加图标
            contentDescription = "兑换码"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("输入兑换码")
        
        // 如果已解锁支持者功能，显示标记
        if (FirebaseRedeemCodeManager.isSupporterFeatureUnlocked(userId, usedRedeemCodes)) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("✓", color = Color.Green)
        }
    }
    */
}

// ============= 10. 检查支持者功能的辅助函数 =============
@Composable
fun rememberSupporterFeatureUnlocked(userId: String?): Boolean {
    var isUnlocked by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        if (userId != null) {
            isUnlocked = FirebaseRedeemCodeManager.hasUsedSupporterCode(userId)
        }
    }
    
    return isUnlocked
}

// 使用示例：
/*
val isSupporterUnlocked = rememberSupporterFeatureUnlocked(userId)

if (isSupporterUnlocked) {
    // 显示支持者专属功能
    Button(onClick = { /* 3倍速 */ }) {
        Text("3X速度")
    }
}
*/

