# Supabase 接入说明

## 📋 第一步：获取 Supabase 配置信息

### 1. 进入项目设置
1. 登录 Supabase 控制台：https://app.supabase.com
2. 选择你的项目
3. 点击左侧菜单底部的 **"Project Settings"**（项目设置 ⚙️）

### 2. 获取配置信息
在 **"Project Settings"** 页面中：

#### **Project URL**
- 位置：通常在页面顶部或 "General" 部分
- 格式：`https://xxxxx.supabase.co`
- 示例：`https://abcdefghijklmnop.supabase.co`

#### **API Keys**
- 位置：点击左侧菜单中的 **"API"** 或直接在设置页面找到 "API" 部分
- 需要获取两个密钥：

**1. anon public key（匿名公共密钥）**
- 用途：客户端访问，公开使用
- 位置：在 "Project API keys" 区域
- 标签：显示为 "anon" 或 "public"
- 这个密钥**可以暴露在客户端代码中**（但建议放在配置文件中）

**2. service_role key（服务角色密钥）**
- 用途：服务器端高权限操作（可选）
- 位置：在 "Project API keys" 区域
- 标签：显示为 "service_role"
- ⚠️ **警告**：这个密钥有完全权限，**绝对不能暴露在客户端代码中**
- 如果只做云存档功能，可能不需要这个

### 3. 记录配置信息
请将以下信息提供给我（我会帮你安全地配置）：

```
Project URL: https://xxxxx.supabase.co
Anon Public Key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Service Role Key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...（可选）
```

---

## 📋 第二步：创建数据库表结构

接入云存档功能需要创建以下数据表：

### 表1：users（用户表）
```sql
-- 用户表（Supabase Auth会自动创建，我们可能需要扩展）
-- 如果需要额外字段，可以创建 user_profiles 表
```

### 表2：game_saves（游戏存档表）
```sql
CREATE TABLE game_saves (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  slot_index INTEGER NOT NULL CHECK (slot_index >= 1 AND slot_index <= 3),
  save_data JSONB NOT NULL,
  save_name TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  version INTEGER DEFAULT 1,
  UNIQUE(user_id, slot_index)
);

-- 创建索引
CREATE INDEX idx_game_saves_user_id ON game_saves(user_id);
CREATE INDEX idx_game_saves_user_slot ON game_saves(user_id, slot_index);
```

### 表3：user_statistics（用户统计表，可选）
```sql
CREATE TABLE user_statistics (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE UNIQUE,
  total_money BIGINT DEFAULT 0,
  total_fans BIGINT DEFAULT 0,
  games_created INTEGER DEFAULT 0,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## 📋 第三步：配置 Row Level Security (RLS)

为了数据安全，需要启用 RLS 策略：

```sql
-- 启用 RLS
ALTER TABLE game_saves ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_statistics ENABLE ROW LEVEL SECURITY;

-- 策略：用户只能访问自己的数据
CREATE POLICY "Users can view own saves"
  ON game_saves FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own saves"
  ON game_saves FOR INSERT
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own saves"
  ON game_saves FOR UPDATE
  USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own saves"
  ON game_saves FOR DELETE
  USING (auth.uid() = user_id);

-- 用户统计表的策略
CREATE POLICY "Users can view own statistics"
  ON user_statistics FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Users can update own statistics"
  ON user_statistics FOR UPDATE
  USING (auth.uid() = user_id);
```

---

## 📋 第四步：功能规划

### 核心功能
1. ✅ **用户认证**
   - 邮箱/密码注册登录
   - 匿名登录（游客模式）
   - 登录状态保持

2. ✅ **云存档同步**
   - 上传存档到云端
   - 从云端下载存档
   - 自动同步（可选）
   - 冲突解决策略

3. ✅ **多设备支持**
   - 同一账号多设备数据同步
   - 最后保存时间显示

### 可选功能
- 排行榜同步
- 成就数据云端备份
- 游戏设置同步

---

## 🔒 安全注意事项

1. **API 密钥管理**
   - `anon public key` 可以放在客户端，但建议使用 `local.properties` 或 `BuildConfig`
   - `service_role key` **绝对不能**放在客户端代码中

2. **数据加密**
   - 存档数据在传输时使用 HTTPS（Supabase 自动提供）
   - 敏感数据可以考虑客户端加密后再上传

3. **用户隐私**
   - 使用 RLS 确保用户只能访问自己的数据
   - 遵循 GDPR 等隐私法规

---

## 📝 下一步

请提供：
1. ✅ Project URL
2. ✅ Anon Public Key
3. ✅ Service Role Key（如果需要服务器端功能）

收到后我会：
1. 添加 Supabase SDK 依赖
2. 创建配置类
3. 实现云存档服务
4. 集成到现有 SaveManager
5. 添加用户认证 UI





