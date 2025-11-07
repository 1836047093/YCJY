# 超简单部署脚本 - 只需3步！
# 以管理员身份运行PowerShell，然后执行：.\simple-deploy.ps1

Write-Host "========================================" -ForegroundColor Green
Write-Host "超简单部署 - 只需3步！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 第1步：创建项目
Write-Host "[1/3] 创建项目..." -ForegroundColor Yellow
$dir = "C:\redeem-api"
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
Set-Location $dir

# 创建package.json
@'
{
  "name": "redeem-api",
  "version": "1.0.0",
  "main": "server.js",
  "dependencies": {
    "express": "^4.18.2",
    "mysql2": "^3.6.0",
    "cors": "^2.8.5",
    "dotenv": "^16.3.1"
  }
}
'@ | Out-File -FilePath "package.json" -Encoding utf8

# 创建server.js（简化版，使用SQLite代替MySQL）
@'
const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

const app = express();
app.use(cors());
app.use(express.json());

const dbFile = path.join(__dirname, 'redeem_codes.json');
let db = { codes: {}, users: {} };

// 加载数据库
if (fs.existsSync(dbFile)) {
  db = JSON.parse(fs.readFileSync(dbFile, 'utf8'));
} else {
  // 初始化兑换码
  for (let i = 1; i <= 150; i++) {
    db.codes['SUPPORTER' + String(i).padStart(3, '0')] = { type: 'supporter', used: false, usedBy: null };
  }
  db.codes['PROGM'] = { type: 'gm', used: false, usedBy: null };
  fs.writeFileSync(dbFile, JSON.stringify(db, null, 2));
}

function saveDb() {
  fs.writeFileSync(dbFile, JSON.stringify(db, null, 2));
}

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.get('/api/v1/redeem/code/:code', (req, res) => {
  const code = req.params.code.toUpperCase();
  const codeData = db.codes[code];
  if (!codeData) {
    return res.status(404).json({ success: false, message: '兑换码不存在' });
  }
  res.json({
    code: code,
    type: codeData.type,
    isValid: true,
    isUsed: codeData.used,
    usedByUserId: codeData.usedBy
  });
});

app.get('/api/v1/redeem/user/:userId', (req, res) => {
  const userId = req.params.userId;
  const userData = db.users[userId] || { usedCodes: [], gmModeUnlocked: false, supporterUnlocked: false };
  res.json({
    userId: userId,
    usedCodes: userData.usedCodes || [],
    gmModeUnlocked: userData.gmModeUnlocked || false,
    supporterUnlocked: userData.supporterUnlocked || false
  });
});

app.get('/api/v1/redeem/check', (req, res) => {
  const { userId, code } = req.query;
  const userData = db.users[userId];
  const isUsed = userData && userData.usedCodes && userData.usedCodes.includes(code.toUpperCase());
  res.json({ isUsed: isUsed || false });
});

app.post('/api/v1/redeem/use', (req, res) => {
  const { userId, code } = req.body;
  if (!userId || !code) {
    return res.status(400).json({ success: false, message: '参数不完整' });
  }
  
  const codeUpper = code.toUpperCase();
  const codeData = db.codes[codeUpper];
  
  if (!codeData) {
    return res.status(404).json({ success: false, message: '兑换码不存在' });
  }
  
  if (codeData.used && codeData.usedBy !== userId) {
    return res.status(400).json({ success: false, message: '兑换码已被其他用户使用' });
  }
  
  if (!db.users[userId]) {
    db.users[userId] = { usedCodes: [], gmModeUnlocked: false, supporterUnlocked: false };
  }
  
  if (db.users[userId].usedCodes.includes(codeUpper)) {
    return res.json({ success: true, message: '兑换码已使用过' });
  }
  
  // 标记为已使用
  codeData.used = true;
  codeData.usedBy = userId;
  db.users[userId].usedCodes.push(codeUpper);
  
  if (codeData.type === 'gm') {
    db.users[userId].gmModeUnlocked = true;
  }
  if (codeData.type === 'supporter') {
    db.users[userId].supporterUnlocked = true;
  }
  
  saveDb();
  res.json({ success: true, message: '兑换成功' });
});

const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`兑换码API运行在端口 ${PORT}`);
});
'@ | Out-File -FilePath "server.js" -Encoding utf8

Write-Host "  项目文件已创建" -ForegroundColor Green

# 第2步：安装依赖
Write-Host "[2/3] 安装依赖（可能需要1-2分钟）..." -ForegroundColor Yellow
npm install --silent
if ($LASTEXITCODE -ne 0) {
    Write-Host "  安装失败，请检查网络" -ForegroundColor Red
    exit 1
}
Write-Host "  依赖安装完成" -ForegroundColor Green

# 第3步：启动服务
Write-Host "[3/3] 启动服务..." -ForegroundColor Yellow
npm install -g pm2 --silent
pm2 delete redeem-api 2>&1 | Out-Null
pm2 start server.js --name redeem-api
pm2 save

# 配置防火墙
New-NetFirewallRule -DisplayName "Redeem API" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow -ErrorAction SilentlyContinue | Out-Null

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "API地址: http://8.138.186.224:3000/api/v1/redeem" -ForegroundColor Cyan
Write-Host "测试: curl http://localhost:3000/health" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步：" -ForegroundColor Yellow
Write-Host "  1. 配置阿里云安全组，开放3000端口" -ForegroundColor White
Write-Host "  2. 修改Android客户端BASE_URL" -ForegroundColor White
Write-Host ""

