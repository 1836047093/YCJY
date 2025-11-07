# Windows Server 2022 快速部署指南

## 📋 你的服务器信息

- **公网IP**: 8.138.186.224
- **私网IP**: 172.28.169.85
- **操作系统**: Windows Server 2022
- **配置**: 2核2GB

## 🚀 快速部署步骤（10分钟）

### 第一步：安装Node.js（2分钟）

1. **下载Node.js**：
   - 访问：https://nodejs.org/zh-cn/download/
   - 下载 **Windows Installer (.msi) 64位**，版本选择 **18.x LTS**

2. **安装**：
   - 双击安装程序
   - 全部使用默认选项，点击"下一步"直到完成

3. **验证安装**（打开PowerShell）：
```powershell
node -v
npm -v
```
应该显示版本号，如 `v18.17.0` 和 `9.6.7`

### 第二步：安装MySQL（3分钟）

1. **下载MySQL**：
   - 访问：https://dev.mysql.com/downloads/installer/
   - 下载 **MySQL Installer for Windows**（推荐选择 `mysql-installer-web-community`）

2. **安装**：
   - 运行安装程序
   - 选择 **"Server only"** 或 **"Developer Default"**
   - 设置root密码（**记住这个密码！**）
   - 完成安装

3. **启动MySQL服务**：
   - 打开"服务"管理器（Win+R，输入 `services.msc`）
   - 找到 `MySQL80` 服务，右键 → 启动
   - 或使用PowerShell：
```powershell
Start-Service MySQL80
```

### 第三步：配置数据库（3分钟）

1. **打开MySQL命令行**：
```powershell
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
.\mysql.exe -u root -p
# 输入root密码
```

2. **执行SQL脚本**：
   将 `数据库初始化脚本.sql` 文件内容复制到MySQL命令行执行，或：

```sql
CREATE DATABASE redeem_codes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE redeem_codes;

CREATE TABLE redeem_codes (
    code VARCHAR(50) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    is_valid TINYINT(1) DEFAULT 1,
    max_uses INT DEFAULT 1,
    used_count INT DEFAULT 0,
    used_by_user_id VARCHAR(100) DEFAULT NULL,
    used_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_redeem_codes (
    user_id VARCHAR(100) PRIMARY KEY,
    used_codes TEXT,
    gm_mode_unlocked TINYINT(1) DEFAULT 0,
    supporter_unlocked TINYINT(1) DEFAULT 0,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建API用户（替换密码）
CREATE USER 'redeem_api'@'localhost' IDENTIFIED BY 'RedeemAPI2025!';
GRANT ALL PRIVILEGES ON redeem_codes.* TO 'redeem_api'@'localhost';
FLUSH PRIVILEGES;
```

3. **初始化兑换码数据**：
   使用Python脚本（见下方）或手动插入前几个测试

### 第四步：部署API服务（2分钟）

1. **创建项目目录**：
```powershell
mkdir C:\redeem-api
cd C:\redeem-api
```

2. **创建文件**：
   - 将 `server.js` 文件复制到 `C:\redeem-api\`
   - 创建 `package.json`：
```powershell
@"
{
  "name": "redeem-api",
  "version": "1.0.0",
  "main": "server.js",
  "dependencies": {
    "express": "^4.18.2",
    "mysql2": "^3.6.0",
    "cors": "^2.8.5",
    "express-rate-limit": "^6.8.1",
    "dotenv": "^16.3.1"
  }
}
"@ | Out-File -FilePath package.json -Encoding utf8
```

3. **创建.env文件**：
```powershell
@"
DB_HOST=localhost
DB_USER=redeem_api
DB_PASSWORD=RedeemAPI2025!
DB_NAME=redeem_codes
PORT=3000
"@ | Out-File -FilePath .env -Encoding utf8
```
**重要：修改密码为你在MySQL中设置的密码！**

4. **安装依赖**：
```powershell
npm install
```

5. **安装PM2（进程管理器）**：
```powershell
npm install -g pm2
npm install -g pm2-windows-startup
pm2-startup install
```

6. **启动服务**：
```powershell
pm2 start server.js --name redeem-api
pm2 save
```

### 第五步：配置防火墙

```powershell
# 允许3000端口
New-NetFirewallRule -DisplayName "Redeem API" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow
```

### 第六步：配置阿里云安全组

1. 登录阿里云控制台
2. 进入ECS实例 → 安全组
3. 添加入站规则：
   - 端口：3000
   - 协议：TCP
   - 授权对象：0.0.0.0/0

### 第七步：测试API

```powershell
# 本地测试
curl http://localhost:3000/health

# 公网测试（从你的电脑）
curl http://8.138.186.224:3000/health
```

## 📝 初始化兑换码数据的Python脚本

创建文件 `init_codes.py`：

```python
import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='your_root_password',  # 替换为实际root密码
    database='redeem_codes'
)
cursor = conn.cursor()

# 插入支持者兑换码
codes = [(f"SUPPORTER{i:03d}", 'supporter') for i in range(1, 151)]
codes.append(('PROGM', 'gm'))

sql = "INSERT INTO redeem_codes (code, type) VALUES (%s, %s)"
cursor.executemany(sql, codes)
conn.commit()

print(f"已插入 {len(codes)} 个兑换码")
cursor.close()
conn.close()
```

运行：
```powershell
pip install mysql-connector-python
python init_codes.py
```

## 📱 Android客户端配置

修改 `DomesticRedeemCodeManager.kt`：

```kotlin
private const val BASE_URL = "http://8.138.186.224:3000/api/v1/redeem"
```

## ✅ 完成检查清单

- [ ] Node.js已安装并可以运行
- [ ] MySQL已安装并运行
- [ ] 数据库已创建并初始化
- [ ] 151个兑换码已插入数据库
- [ ] API服务已启动（pm2 list可以看到）
- [ ] 防火墙已开放3000端口
- [ ] 阿里云安全组已配置
- [ ] 可以访问 http://8.138.186.224:3000/health
- [ ] Android客户端BASE_URL已配置

## 🔍 常见问题

**Q: pm2启动失败？**
```powershell
# 查看日志
pm2 logs redeem-api
# 检查.env文件中的密码是否正确
```

**Q: 无法连接数据库？**
```powershell
# 检查MySQL服务是否运行
Get-Service MySQL80
# 测试连接
mysql -u redeem_api -p
```

**Q: 外网无法访问？**
- 检查Windows防火墙
- 检查阿里云安全组规则
- 检查PM2服务是否运行：`pm2 list`

## 🎯 下一步

部署完成后，告诉我，我帮你：
1. 测试API是否正常工作
2. 修改Android客户端代码
3. 配置HTTPS（可选）

