#!/bin/bash
# 阿里云服务器一键部署脚本
# 使用方法: bash deploy.sh

set -e

echo "=========================================="
echo "兑换码API一键部署脚本"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}请使用root用户运行此脚本${NC}"
    exit 1
fi

# 1. 更新系统
echo -e "${GREEN}[1/8] 更新系统...${NC}"
apt update && apt upgrade -y

# 2. 安装Node.js
echo -e "${GREEN}[2/8] 安装Node.js...${NC}"
if ! command -v node &> /dev/null; then
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
else
    echo -e "${YELLOW}Node.js已安装，跳过${NC}"
fi

# 验证Node.js安装
node -v
npm -v

# 3. 安装MySQL
echo -e "${GREEN}[3/8] 安装MySQL...${NC}"
if ! command -v mysql &> /dev/null; then
    apt-get install -y mysql-server
    systemctl start mysql
    systemctl enable mysql
else
    echo -e "${YELLOW}MySQL已安装，跳过${NC}"
fi

# 4. 配置MySQL
echo -e "${GREEN}[4/8] 配置MySQL数据库...${NC}"
read -p "请输入MySQL root密码（如果已设置，直接回车）: " MYSQL_ROOT_PASSWORD
read -p "请输入API数据库用户密码: " API_DB_PASSWORD

if [ -z "$API_DB_PASSWORD" ]; then
    echo -e "${RED}API数据库密码不能为空${NC}"
    exit 1
fi

# 创建数据库和表
mysql -u root ${MYSQL_ROOT_PASSWORD:+-p$MYSQL_ROOT_PASSWORD} <<EOF
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

CREATE USER IF NOT EXISTS 'redeem_api'@'localhost' IDENTIFIED BY '$API_DB_PASSWORD';
GRANT ALL PRIVILEGES ON redeem_codes.* TO 'redeem_api'@'localhost';
FLUSH PRIVILEGES;
EOF

# 初始化兑换码数据（使用Python生成）
echo -e "${GREEN}[5/8] 初始化兑换码数据...${NC}"
python3 <<PYTHON_SCRIPT
import mysql.connector

try:
    conn = mysql.connector.connect(
        host='localhost',
        user='root',
        password='${MYSQL_ROOT_PASSWORD}',
        database='redeem_codes'
    )
    cursor = conn.cursor()
    
    # 检查是否已有数据
    cursor.execute("SELECT COUNT(*) FROM redeem_codes")
    count = cursor.fetchone()[0]
    
    if count == 0:
        # 插入支持者兑换码
        codes = [(f"SUPPORTER{i:03d}", 'supporter') for i in range(1, 151)]
        codes.append(('PROGM', 'gm'))
        
        sql = "INSERT INTO redeem_codes (code, type) VALUES (%s, %s)"
        cursor.executemany(sql, codes)
        conn.commit()
        print(f"已插入 {len(codes)} 个兑换码")
    else:
        print(f"数据库中已有 {count} 个兑换码，跳过初始化")
    
    cursor.close()
    conn.close()
except mysql.connector.Error as err:
    print(f"MySQL错误: {err}")
PYTHON_SCRIPT

# 5. 创建项目目录
echo -e "${GREEN}[6/8] 创建项目目录...${NC}"
PROJECT_DIR="/opt/redeem-api"
mkdir -p $PROJECT_DIR
cd $PROJECT_DIR

# 6. 创建package.json
cat > package.json <<EOF
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
EOF

# 7. 创建server.js（简化版，完整版见部署指南）
cat > server.js <<'SERVER_EOF'
const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const rateLimit = require('express-rate-limit');

const app = express();
app.use(cors());
app.use(express.json());

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: { success: false, message: '请求过于频繁' }
});
app.use('/api/v1/redeem', limiter);

const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'redeem_api',
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME || 'redeem_codes',
  waitForConnections: true,
  connectionLimit: 10
});

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.get('/api/v1/redeem/code/:code', async (req, res) => {
  try {
    const code = req.params.code.toUpperCase();
    const [rows] = await pool.execute('SELECT * FROM redeem_codes WHERE code = ?', [code]);
    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: '兑换码不存在' });
    }
    const r = rows[0];
    res.json({
      code: r.code,
      type: r.type,
      isValid: r.is_valid === 1,
      isUsed: r.used_count > 0,
      usedByUserId: r.used_by_user_id
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  }
});

app.get('/api/v1/redeem/user/:userId', async (req, res) => {
  try {
    const userId = req.params.userId;
    const [rows] = await pool.execute('SELECT * FROM user_redeem_codes WHERE user_id = ?', [userId]);
    if (rows.length === 0) {
      return res.json({ userId, usedCodes: [], gmModeUnlocked: false, supporterUnlocked: false });
    }
    const u = rows[0];
    let codes = [];
    try { codes = JSON.parse(u.used_codes || '[]'); } catch (e) {}
    res.json({
      userId: u.user_id,
      usedCodes: codes,
      gmModeUnlocked: u.gm_mode_unlocked === 1,
      supporterUnlocked: u.supporter_unlocked === 1
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  }
});

app.get('/api/v1/redeem/check', async (req, res) => {
  try {
    const { userId, code } = req.query;
    if (!userId || !code) {
      return res.status(400).json({ success: false, message: '参数不完整' });
    }
    const [rows] = await pool.execute('SELECT used_codes FROM user_redeem_codes WHERE user_id = ?', [userId]);
    if (rows.length > 0) {
      let codes = [];
      try { codes = JSON.parse(rows[0].used_codes || '[]'); } catch (e) {}
      return res.json({ isUsed: codes.includes(code.toUpperCase()) });
    }
    res.json({ isUsed: false });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  }
});

app.post('/api/v1/redeem/use', async (req, res) => {
  const conn = await pool.getConnection();
  try {
    const { userId, code } = req.body;
    if (!userId || !code) {
      return res.status(400).json({ success: false, message: '参数不完整' });
    }
    const codeUpper = code.toUpperCase();
    await conn.beginTransaction();
    
    const [codeRows] = await conn.execute('SELECT * FROM redeem_codes WHERE code = ? FOR UPDATE', [codeUpper]);
    if (codeRows.length === 0) {
      await conn.rollback();
      return res.status(404).json({ success: false, message: '兑换码不存在' });
    }
    const rc = codeRows[0];
    if (rc.is_valid !== 1) {
      await conn.rollback();
      return res.status(400).json({ success: false, message: '兑换码无效' });
    }
    if (rc.used_count > 0 && rc.used_by_user_id !== userId) {
      await conn.rollback();
      return res.status(400).json({ success: false, message: '兑换码已被其他用户使用' });
    }
    
    const [userRows] = await conn.execute('SELECT * FROM user_redeem_codes WHERE user_id = ? FOR UPDATE', [userId]);
    let codes = [];
    if (userRows.length > 0) {
      try { codes = JSON.parse(userRows[0].used_codes || '[]'); } catch (e) {}
      if (codes.includes(codeUpper)) {
        await conn.rollback();
        return res.json({ success: true, message: '兑换码已使用过' });
      }
    }
    
    await conn.execute('UPDATE redeem_codes SET used_count = 1, used_by_user_id = ?, used_at = NOW() WHERE code = ?', [userId, codeUpper]);
    codes.push(codeUpper);
    const isGM = rc.type === 'gm';
    const isSupporter = rc.type === 'supporter';
    
    if (userRows.length > 0) {
      await conn.execute('UPDATE user_redeem_codes SET used_codes = ?, gm_mode_unlocked = CASE WHEN ? = 1 THEN 1 ELSE gm_mode_unlocked END, supporter_unlocked = CASE WHEN ? = 1 THEN 1 ELSE supporter_unlocked END, last_updated = NOW() WHERE user_id = ?', [JSON.stringify(codes), isGM ? 1 : 0, isSupporter ? 1 : 0, userId]);
    } else {
      await conn.execute('INSERT INTO user_redeem_codes (user_id, used_codes, gm_mode_unlocked, supporter_unlocked) VALUES (?, ?, ?, ?)', [userId, JSON.stringify(codes), isGM ? 1 : 0, isSupporter ? 1 : 0]);
    }
    
    await conn.commit();
    res.json({ success: true, message: '兑换成功' });
  } catch (error) {
    await conn.rollback();
    console.error(error);
    res.status(500).json({ success: false, message: '服务器错误' });
  } finally {
    conn.release();
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`兑换码API服务运行在端口 ${PORT}`);
});
SERVER_EOF

# 8. 创建.env文件
cat > .env <<EOF
DB_HOST=localhost
DB_USER=redeem_api
DB_PASSWORD=$API_DB_PASSWORD
DB_NAME=redeem_codes
PORT=3000
EOF

# 9. 安装依赖
echo -e "${GREEN}[7/8] 安装Node.js依赖...${NC}"
npm install

# 10. 安装PM2
echo -e "${GREEN}[8/8] 安装PM2进程管理器...${NC}"
npm install -g pm2

# 11. 启动服务
echo -e "${GREEN}启动服务...${NC}"
pm2 start server.js --name redeem-api --update-env
pm2 save
pm2 startup

# 12. 安装Nginx
echo -e "${GREEN}安装Nginx...${NC}"
apt-get install -y nginx

# 获取服务器IP
SERVER_IP=$(curl -s ifconfig.me || curl -s ipinfo.io/ip)

# 配置Nginx
cat > /etc/nginx/sites-available/redeem-api <<EOF
server {
    listen 80;
    server_name _;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

ln -sf /etc/nginx/sites-available/redeem-api /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl restart nginx
systemctl enable nginx

# 13. 配置防火墙
echo -e "${GREEN}配置防火墙...${NC}"
ufw --force enable
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp

# 完成
echo ""
echo -e "${GREEN}=========================================="
echo "部署完成！"
echo "==========================================${NC}"
echo ""
echo "服务器IP: $SERVER_IP"
echo "API地址: http://$SERVER_IP/api/v1/redeem"
echo "健康检查: http://$SERVER_IP/health"
echo ""
echo "测试命令:"
echo "  curl http://$SERVER_IP/health"
echo "  curl http://$SERVER_IP/api/v1/redeem/code/SUPPORTER001"
echo ""
echo "查看日志:"
echo "  pm2 logs redeem-api"
echo ""
echo "重启服务:"
echo "  pm2 restart redeem-api"
echo ""
echo -e "${YELLOW}请在Android客户端中配置:${NC}"
echo "  BASE_URL = \"http://$SERVER_IP/api/v1/redeem\""
echo ""



