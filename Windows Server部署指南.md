# Windows Server 2022 部署兑换码API指南

## 📋 服务器信息

- **公网IP**: 8.138.186.224
- **私网IP**: 172.28.169.85
- **操作系统**: Windows Server 2022
- **配置**: 2核2GB

## 🚀 方案1：Windows Server部署（当前系统）

### 第一步：安装必要软件

#### 1.1 安装Node.js

1. 下载Node.js 18.x LTS版本：
   - 访问：https://nodejs.org/zh-cn/download/
   - 下载 Windows Installer (.msi) 64位版本

2. 运行安装程序，全部使用默认选项

3. 验证安装（打开PowerShell）：
```powershell
node -v
npm -v
```

#### 1.2 安装MySQL

1. 下载MySQL 8.0：
   - 访问：https://dev.mysql.com/downloads/installer/
   - 下载 MySQL Installer for Windows

2. 运行安装程序：
   - 选择 "Developer Default" 或 "Server only"
   - 设置root密码（记住这个密码！）
   - 完成安装

3. 启动MySQL服务：
```powershell
# 在服务管理器中启动MySQL服务，或使用：
net start MySQL80
```

#### 1.3 安装Git（可选，用于下载代码）

下载：https://git-scm.com/download/win

### 第二步：配置数据库

#### 2.1 打开MySQL命令行

```powershell
# 找到MySQL安装目录，通常在：
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
.\mysql.exe -u root -p
# 输入root密码
```

#### 2.2 执行数据库初始化脚本

将 `数据库初始化脚本.sql` 文件上传到服务器，然后执行：

```sql
-- 在MySQL命令行中执行
source C:\path\to\数据库初始化脚本.sql
```

或者手动执行：

```sql
CREATE DATABASE IF NOT EXISTS redeem_codes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE redeem_codes;

CREATE TABLE IF NOT EXISTS redeem_codes (
    code VARCHAR(50) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    is_valid TINYINT(1) DEFAULT 1,
    max_uses INT DEFAULT 1,
    used_count INT DEFAULT 0,
    used_by_user_id VARCHAR(100) DEFAULT NULL,
    used_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_used_by_user_id (used_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_redeem_codes (
    user_id VARCHAR(100) PRIMARY KEY,
    used_codes TEXT,
    gm_mode_unlocked TINYINT(1) DEFAULT 0,
    supporter_unlocked TINYINT(1) DEFAULT 0,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_gm_mode (gm_mode_unlocked),
    INDEX idx_supporter (supporter_unlocked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建API用户（替换密码）
CREATE USER IF NOT EXISTS 'redeem_api'@'localhost' IDENTIFIED BY 'your_secure_password_here';
GRANT ALL PRIVILEGES ON redeem_codes.* TO 'redeem_api'@'localhost';
FLUSH PRIVILEGES;

-- 插入兑换码（使用Python脚本生成，见下方）
```

#### 2.3 初始化兑换码数据

创建Python脚本 `init_codes.py`：

```python
import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='your_root_password',  # 替换为实际密码
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

安装Python MySQL驱动并运行：
```powershell
pip install mysql-connector-python
python init_codes.py
```

### 第三步：部署API服务

#### 3.1 创建项目目录

```powershell
mkdir C:\redeem-api
cd C:\redeem-api
```

#### 3.2 创建package.json

```powershell
@"
{
  "name": "redeem-api",
  "version": "1.0.0",
  "description": "兑换码API服务",
  "main": "server.js",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.18.2",
    "mysql2": "^3.6.0",
    "cors": "^2.8.5",
    "express-rate-limit": "^6.8.1"
  }
}
"@ | Out-File -FilePath package.json -Encoding utf8
```

#### 3.3 创建server.js

将 `阿里云服务器部署指南.md` 中的 server.js 内容复制到 `C:\redeem-api\server.js`

#### 3.4 创建.env文件

```powershell
@"
DB_HOST=localhost
DB_USER=redeem_api
DB_PASSWORD=your_secure_password_here
DB_NAME=redeem_codes
PORT=3000
"@ | Out-File -FilePath .env -Encoding utf8
```

**重要：修改.env文件中的密码为实际密码！**

#### 3.5 安装依赖

```powershell
npm install
```

#### 3.6 安装PM2（进程管理器）

```powershell
npm install -g pm2
npm install -g pm2-windows-startup
pm2-startup install
```

#### 3.7 启动服务

```powershell
pm2 start server.js --name redeem-api
pm2 save
```

### 第四步：配置防火墙

```powershell
# 允许3000端口（如果直接访问）
New-NetFirewallRule -DisplayName "Redeem API" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow

# 或者配置IIS反向代理（推荐）
```

### 第五步：配置IIS反向代理（推荐）

#### 5.1 安装IIS和URL Rewrite

1. 打开"服务器管理器" → "添加角色和功能"
2. 安装IIS（Internet Information Services）
3. 下载并安装URL Rewrite模块：
   - https://www.iis.net/downloads/microsoft/url-rewrite

#### 5.2 配置反向代理

1. 打开IIS管理器
2. 创建新网站或使用默认网站
3. 添加URL重写规则，将请求转发到 `http://localhost:3000`

### 第六步：测试API

```powershell
# 测试健康检查
curl http://localhost:3000/health

# 测试查询兑换码
curl http://localhost:3000/api/v1/redeem/code/SUPPORTER001

# 测试公网访问
curl http://8.138.186.224:3000/health
```

## 🔄 方案2：重装为Linux系统（推荐）

Windows Server部署相对复杂，建议重装为Ubuntu系统：

### 重装步骤：

1. **在阿里云控制台操作**：
   - 进入ECS实例详情页
   - 点击"更多" → "云盘和镜像" → "更换操作系统"
   - 选择"Ubuntu 22.04 LTS"或"CentOS 7"
   - 确认重装（会清除数据）

2. **重装后使用Linux部署脚本**：
   - 使用 `deploy.sh` 一键部署
   - 或按照 `阿里云服务器部署指南.md` 手动部署

### Linux部署的优势：

- ✅ 部署更简单（一键脚本）
- ✅ 资源占用更少
- ✅ 更适合运行Node.js服务
- ✅ 社区支持更好

## 📱 客户端配置

无论使用哪种方案，Android客户端配置相同：

修改 `DomesticRedeemCodeManager.kt`：

```kotlin
private const val BASE_URL = "http://8.138.186.224/api/v1/redeem"
// 如果配置了IIS/Nginx，端口可能是80，则：
// private const val BASE_URL = "http://8.138.186.224/api/v1/redeem"
```

## ⚠️ 注意事项

1. **Windows防火墙**：确保开放3000端口（或80端口如果使用IIS）
2. **阿里云安全组**：在阿里云控制台配置安全组规则，开放80和443端口
3. **数据库密码**：使用强密码，不要使用默认密码
4. **HTTPS**：生产环境建议配置HTTPS（使用Let's Encrypt或阿里云SSL证书）

## 🎯 推荐操作

**建议重装为Ubuntu系统**，然后使用Linux部署脚本，这样：
- 部署时间：5-10分钟（vs Windows需要30分钟+）
- 维护更简单
- 性能更好

需要我帮你：
1. 创建Windows Server的完整部署脚本？
2. 还是帮你准备Linux重装后的部署步骤？

