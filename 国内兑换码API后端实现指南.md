# 国内兑换码API后端实现指南

## 📋 问题

国内用户不翻墙无法访问Firebase后端，需要国内可访问的解决方案。

## ✅ 解决方案：自建HTTP API后端

### 架构设计

```
客户端 (Android App)
    ↓ HTTP/HTTPS
国内服务器 (阿里云/腾讯云/华为云)
    ↓
数据库 (MySQL/PostgreSQL)
```

### 1. 服务器部署

**推荐平台：**
- 阿里云ECS（最便宜，约50元/月）
- 腾讯云CVM
- 华为云ECS
- 其他国内云服务商

**配置要求：**
- CPU: 1核
- 内存: 1GB
- 带宽: 1Mbps
- 系统: Ubuntu/CentOS

### 2. 后端API实现

**技术栈选择：**

#### 方案A：Node.js + Express（推荐，最简单）

```javascript
// server.js
const express = require('express');
const mysql = require('mysql2/promise');
const app = express();

app.use(express.json());

// 数据库连接
const pool = mysql.createPool({
  host: 'localhost',
  user: 'root',
  password: 'your_password',
  database: 'redeem_codes',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

// 查询兑换码
app.get('/api/v1/redeem/code/:code', async (req, res) => {
  try {
    const code = req.params.code.toUpperCase();
    const [rows] = await pool.execute(
      'SELECT * FROM redeem_codes WHERE code = ?',
      [code]
    );
    
    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: '兑换码不存在' });
    }
    
    const redeemCode = rows[0];
    res.json({
      code: redeemCode.code,
      type: redeemCode.type,
      isValid: redeemCode.is_valid === 1,
      isUsed: redeemCode.used_count > 0,
      usedByUserId: redeemCode.used_by_user_id
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  }
});

// 查询用户兑换码列表
app.get('/api/v1/redeem/user/:userId', async (req, res) => {
  try {
    const userId = req.params.userId;
    const [rows] = await pool.execute(
      'SELECT * FROM user_redeem_codes WHERE user_id = ?',
      [userId]
    );
    
    if (rows.length === 0) {
      return res.json({
        userId: userId,
        usedCodes: [],
        gmModeUnlocked: false,
        supporterUnlocked: false
      });
    }
    
    const userData = rows[0];
    const usedCodes = JSON.parse(userData.used_codes || '[]');
    
    res.json({
      userId: userData.user_id,
      usedCodes: usedCodes,
      gmModeUnlocked: userData.gm_mode_unlocked === 1,
      supporterUnlocked: userData.supporter_unlocked === 1
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  }
});

// 检查兑换码是否已使用
app.get('/api/v1/redeem/check', async (req, res) => {
  try {
    const { userId, code } = req.query;
    const codeUpper = code.toUpperCase();
    
    // 查询用户是否已使用
    const [userRows] = await pool.execute(
      'SELECT used_codes FROM user_redeem_codes WHERE user_id = ?',
      [userId]
    );
    
    if (userRows.length > 0) {
      const usedCodes = JSON.parse(userRows[0].used_codes || '[]');
      const isUsed = usedCodes.includes(codeUpper);
      return res.json({ isUsed });
    }
    
    res.json({ isUsed: false });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  }
});

// 使用兑换码
app.post('/api/v1/redeem/use', async (req, res) => {
  try {
    const { userId, code } = req.body;
    const codeUpper = code.toUpperCase();
    
    // 开始事务
    const connection = await pool.getConnection();
    await connection.beginTransaction();
    
    try {
      // 1. 检查兑换码是否存在且有效
      const [codeRows] = await connection.execute(
        'SELECT * FROM redeem_codes WHERE code = ?',
        [codeUpper]
      );
      
      if (codeRows.length === 0) {
        await connection.rollback();
        return res.status(404).json({ success: false, message: '兑换码不存在' });
      }
      
      const redeemCode = codeRows[0];
      
      // 2. 检查是否已被其他用户使用（全局唯一）
      if (redeemCode.used_count > 0 && redeemCode.used_by_user_id !== userId) {
        await connection.rollback();
        return res.status(400).json({ success: false, message: '兑换码已被其他用户使用' });
      }
      
      // 3. 检查用户是否已使用过
      const [userRows] = await connection.execute(
        'SELECT * FROM user_redeem_codes WHERE user_id = ?',
        [userId]
      );
      
      let usedCodes = [];
      if (userRows.length > 0) {
        usedCodes = JSON.parse(userRows[0].used_codes || '[]');
        if (usedCodes.includes(codeUpper)) {
          await connection.rollback();
          return res.json({ success: true, message: '兑换码已使用过' });
        }
      }
      
      // 4. 更新兑换码状态
      await connection.execute(
        'UPDATE redeem_codes SET used_count = 1, used_by_user_id = ?, used_at = NOW() WHERE code = ?',
        [userId, codeUpper]
      );
      
      // 5. 更新用户兑换码记录
      usedCodes.push(codeUpper);
      const codeType = redeemCode.type;
      const isGM = codeType === 'gm';
      const isSupporter = codeType === 'supporter';
      
      if (userRows.length > 0) {
        // 更新现有记录
        await connection.execute(
          `UPDATE user_redeem_codes 
           SET used_codes = ?, 
               gm_mode_unlocked = CASE WHEN ? THEN 1 ELSE gm_mode_unlocked END,
               supporter_unlocked = CASE WHEN ? THEN 1 ELSE supporter_unlocked END,
               last_updated = NOW()
           WHERE user_id = ?`,
          [JSON.stringify(usedCodes), isGM, isSupporter, userId]
        );
      } else {
        // 创建新记录
        await connection.execute(
          'INSERT INTO user_redeem_codes (user_id, used_codes, gm_mode_unlocked, supporter_unlocked) VALUES (?, ?, ?, ?)',
          [userId, JSON.stringify(usedCodes), isGM ? 1 : 0, isSupporter ? 1 : 0]
        );
      }
      
      await connection.commit();
      res.json({ success: true, message: '兑换成功' });
    } catch (error) {
      await connection.rollback();
      throw error;
    } finally {
      connection.release();
    }
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`服务器运行在端口 ${PORT}`);
});
```

#### 方案B：Python + Flask

```python
from flask import Flask, request, jsonify
import mysql.connector
from mysql.connector import pooling
import json

app = Flask(__name__)

# 数据库连接池
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'your_password',
    'database': 'redeem_codes',
    'pool_name': 'mypool',
    'pool_size': 10
}

pool = mysql.connector.pooling.MySQLConnectionPool(**db_config)

@app.route('/api/v1/redeem/code/<code>', methods=['GET'])
def get_redeem_code(code):
    # 实现查询兑换码逻辑
    pass

@app.route('/api/v1/redeem/user/<userId>', methods=['GET'])
def get_user_redeem_codes(userId):
    # 实现查询用户兑换码逻辑
    pass

@app.route('/api/v1/redeem/check', methods=['GET'])
def check_code_used():
    # 实现检查兑换码逻辑
    pass

@app.route('/api/v1/redeem/use', methods=['POST'])
def use_redeem_code():
    # 实现使用兑换码逻辑
    pass

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)
```

### 3. 数据库表结构

```sql
-- 兑换码表
CREATE TABLE redeem_codes (
    code VARCHAR(50) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    is_valid TINYINT(1) DEFAULT 1,
    max_uses INT DEFAULT 1,
    used_count INT DEFAULT 0,
    used_by_user_id VARCHAR(100),
    used_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户兑换码表
CREATE TABLE user_redeem_codes (
    user_id VARCHAR(100) PRIMARY KEY,
    used_codes TEXT,
    gm_mode_unlocked TINYINT(1) DEFAULT 0,
    supporter_unlocked TINYINT(1) DEFAULT 0,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始化支持者兑换码
INSERT INTO redeem_codes (code, type) VALUES
('SUPPORTER001', 'supporter'),
('SUPPORTER002', 'supporter'),
-- ... 其他150个
('SUPPORTER150', 'supporter'),
('PROGM', 'gm');
```

### 4. 部署步骤

1. **购买服务器**
   - 选择国内云服务商
   - 选择最低配置即可（约50元/月）

2. **安装环境**
   ```bash
   # Node.js方案
   curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
   sudo apt-get install -y nodejs
   npm install express mysql2
   
   # Python方案
   sudo apt-get install python3 python3-pip
   pip3 install flask mysql-connector-python
   ```

3. **安装数据库**
   ```bash
   sudo apt-get install mysql-server
   # 创建数据库和表
   ```

4. **配置Nginx反向代理**
   ```nginx
   server {
       listen 80;
       server_name your-api-domain.com;
       
       location / {
           proxy_pass http://localhost:3000;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

5. **配置HTTPS（可选但推荐）**
   ```bash
   sudo apt-get install certbot python3-certbot-nginx
   sudo certbot --nginx -d your-api-domain.com
   ```

6. **启动服务**
   ```bash
   # 使用PM2管理Node.js进程
   npm install -g pm2
   pm2 start server.js
   pm2 save
   pm2 startup
   ```

### 5. 客户端配置

修改 `DomesticRedeemCodeManager.kt` 中的 `BASE_URL`：

```kotlin
private const val BASE_URL = "https://your-api-domain.com/api/v1/redeem"
```

### 6. 成本估算

- **服务器**：50元/月（最低配置）
- **域名**：10元/年（可选）
- **SSL证书**：免费（Let's Encrypt）
- **总成本**：约50元/月

### 7. 安全建议

1. **API密钥认证**（可选）
   ```javascript
   // 添加API密钥验证中间件
   app.use((req, res, next) => {
     const apiKey = req.headers['x-api-key'];
     if (apiKey !== process.env.API_KEY) {
       return res.status(401).json({ success: false, message: '未授权' });
     }
     next();
   });
   ```

2. **限流**（防止滥用）
   ```javascript
   const rateLimit = require('express-rate-limit');
   const limiter = rateLimit({
     windowMs: 15 * 60 * 1000, // 15分钟
     max: 100 // 限制100次请求
   });
   app.use('/api/v1/redeem', limiter);
   ```

3. **HTTPS**：必须使用HTTPS加密传输

4. **SQL注入防护**：使用参数化查询（已实现）

## 📝 总结

这个方案的优势：
- ✅ 国内访问速度快（<100ms）
- ✅ 完全可控
- ✅ 成本低（约50元/月）
- ✅ 数据安全
- ✅ 易于扩展

需要帮助实现后端代码吗？

