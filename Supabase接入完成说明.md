# Supabase 兑换码管理接入完成说明

## ✅ 已完成的工作

### 1. 添加Supabase依赖
- ✅ 在 `gradle/libs.versions.toml` 中添加了 Supabase PostgREST 依赖
- ✅ 在 `app/build.gradle.kts` 中添加了依赖引用

### 2. 创建配置文件
- ✅ `app/src/main/java/com/example/yjcy/config/SupabaseConfig.kt`
  - Supabase客户端配置类
  - Project URL已配置：`https://otfeirasxpshwjesbyvu.supabase.co`
  - ⚠️ **需要配置Anon Public Key**（见下方说明）

### 3. 创建Supabase服务类
- ✅ `app/src/main/java/com/example/yjcy/data/SupabaseRedeemCodeService.kt`
  - 兑换码查询、标记、获取列表等功能
  - 完整的错误处理和日志记录

### 4. 修改RedeemCodeManager
- ✅ 优先使用Supabase云端存储
- ✅ 失败时自动回退到本地SharedPreferences
- ✅ 向后兼容，不影响现有功能

### 5. 创建数据库表结构SQL脚本
- ✅ `Supabase兑换码表结构.sql`
  - 包含完整的表结构、索引、RLS策略

---

## 📋 接下来需要做的步骤

### 步骤1：获取Anon Public Key

1. 登录 Supabase 控制台：https://app.supabase.com
2. 选择项目
3. 点击左侧菜单底部的 **"Project Settings"**（项目设置 ⚙️）
4. 点击 **"API"** 菜单
5. 在 **"Project API keys"** 区域找到 **"anon public"** 密钥
6. 复制这个密钥

### 步骤2：配置Anon Public Key

打开文件：`app/src/main/java/com/example/yjcy/config/SupabaseConfig.kt`

找到这一行：
```kotlin
private const val SUPABASE_ANON_KEY = "YOUR_ANON_PUBLIC_KEY_HERE"
```

替换为你的Anon Public Key：
```kotlin
private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // 你的实际密钥
```

### 步骤3：创建数据库表

1. 在Supabase控制台，点击左侧菜单的 **"SQL Editor"**
2. 打开文件 `Supabase兑换码表结构.sql`
3. 复制所有SQL代码
4. 粘贴到SQL Editor中
5. 点击 **"Run"** 执行

这会创建：
- `redeem_code_records` 表
- 必要的索引
- Row Level Security (RLS) 策略

### 步骤4：测试

1. 同步Gradle项目（Sync Project）
2. 运行应用
3. 使用兑换码功能
4. 检查日志，确认Supabase连接正常

---

## 🔍 功能说明

### 工作原理

1. **优先使用Supabase**
   - 如果Supabase配置正确且连接成功，所有兑换码记录会保存到云端
   - 支持多设备同步

2. **自动回退机制**
   - 如果Supabase连接失败（网络问题、配置错误等），自动使用本地存储
   - 确保功能始终可用

3. **双重备份**
   - 即使使用Supabase，也会在本地保存一份备份
   - 提高数据可靠性

### 数据表结构

```sql
redeem_code_records
├── id (UUID, 主键)
├── user_id (TEXT, 用户ID - TapTap unionId/openId)
├── code (TEXT, 兑换码 - 已转换为大写)
├── used_at (TIMESTAMP, 使用时间)
└── UNIQUE(user_id, code) - 确保每个用户每个兑换码只能用一次
```

### API接口

- `isCodeUsedByUser(userId, code)` - 检查兑换码是否已使用
- `markCodeAsUsed(userId, code)` - 标记兑换码为已使用
- `getUserUsedCodes(userId)` - 获取用户所有已使用的兑换码
- `hasUsedSupporterCode(userId)` - 检查是否使用过支持者兑换码

---

## 🐛 常见问题

### Q: 如果Supabase未配置，会怎样？
A: 系统会自动使用本地SharedPreferences存储，功能完全正常。

### Q: 如何查看Supabase中的数据？
A: 在Supabase控制台 → Table Editor → `redeem_code_records` 表

### Q: 网络失败时会怎样？
A: 自动回退到本地存储，不影响用户体验。

### Q: 如何迁移现有本地数据到Supabase？
A: 暂不支持自动迁移。用户首次使用兑换码时会自动同步到云端。

---

## 📝 注意事项

1. **Anon Public Key安全性**
   - Anon Key是公开的，可以放在客户端代码中
   - 已通过RLS策略限制，用户只能访问自己的数据

2. **性能考虑**
   - 查询使用同步方式（runBlocking），可能阻塞主线程
   - 建议后续优化为异步方式

3. **数据一致性**
   - 本地和云端数据可能不同步（如果网络失败时使用本地存储）
   - 优先信任云端数据

---

## 🎯 完成检查清单

- [ ] 获取Anon Public Key
- [ ] 配置SupabaseConfig.kt中的密钥
- [ ] 在Supabase中执行SQL脚本创建表
- [ ] 同步Gradle项目
- [ ] 测试兑换码功能
- [ ] 检查Supabase控制台中的数据

完成以上步骤后，Supabase兑换码管理功能就完全可以使用了！


