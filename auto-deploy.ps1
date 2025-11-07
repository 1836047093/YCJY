# Windows Server 2022 全自动部署脚本
# 使用方法：以管理员身份打开PowerShell，执行此脚本
# 注意：此脚本会自动完成大部分部署工作，但需要你输入MySQL root密码

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "兑换码API全自动部署脚本" -ForegroundColor Cyan
Write-Host "服务器IP: 8.138.186.224" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "❌ 错误：请以管理员身份运行PowerShell" -ForegroundColor Red
    Write-Host "   右键点击PowerShell → 以管理员身份运行" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "✅ 管理员权限检查通过" -ForegroundColor Green
Write-Host ""

# ========== 第一步：检查并安装Node.js ==========
Write-Host "[步骤 1/8] 检查Node.js..." -ForegroundColor Yellow
if (Get-Command node -ErrorAction SilentlyContinue) {
    $nodeVersion = node -v
    Write-Host "  ✅ Node.js已安装: $nodeVersion" -ForegroundColor Green
} else {
    Write-Host "  ❌ Node.js未安装" -ForegroundColor Red
    Write-Host ""
    Write-Host "  请手动安装Node.js：" -ForegroundColor Yellow
    Write-Host "  1. 访问: https://nodejs.org/zh-cn/download/" -ForegroundColor White
    Write-Host "  2. 下载 Windows Installer (.msi) 64位，版本18.x LTS" -ForegroundColor White
    Write-Host "  3. 运行安装程序，全部使用默认选项" -ForegroundColor White
    Write-Host "  4. 安装完成后，重新运行此脚本" -ForegroundColor White
    Write-Host ""
    $continue = Read-Host "是否已安装Node.js？(y/n)"
    if ($continue -ne "y") {
        exit 1
    }
    # 刷新环境变量
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

# ========== 第二步：检查并安装MySQL ==========
Write-Host "[步骤 2/8] 检查MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
if ($mysqlService) {
    Write-Host "  ✅ MySQL已安装" -ForegroundColor Green
    if ($mysqlService.Status -ne "Running") {
        Write-Host "  启动MySQL服务..." -ForegroundColor Yellow
        Start-Service $mysqlService.Name
        Start-Sleep -Seconds 3
    }
    Write-Host "  ✅ MySQL服务运行中" -ForegroundColor Green
} else {
    Write-Host "  ❌ MySQL未安装" -ForegroundColor Red
    Write-Host ""
    Write-Host "  请手动安装MySQL：" -ForegroundColor Yellow
    Write-Host "  1. 访问: https://dev.mysql.com/downloads/installer/" -ForegroundColor White
    Write-Host "  2. 下载 MySQL Installer for Windows" -ForegroundColor White
    Write-Host "  3. 选择 'Server only' 安装" -ForegroundColor White
    Write-Host "  4. 设置root密码（记住这个密码！）" -ForegroundColor White
    Write-Host "  5. 安装完成后，重新运行此脚本" -ForegroundColor White
    Write-Host ""
    $continue = Read-Host "是否已安装MySQL？(y/n)"
    if ($continue -ne "y") {
        exit 1
    }
}

# ========== 第三步：创建项目目录 ==========
Write-Host "[步骤 3/8] 创建项目目录..." -ForegroundColor Yellow
$projectDir = "C:\redeem-api"
if (-not (Test-Path $projectDir)) {
    New-Item -ItemType Directory -Path $projectDir | Out-Null
    Write-Host "  ✅ 创建目录: $projectDir" -ForegroundColor Green
} else {
    Write-Host "  ✅ 目录已存在: $projectDir" -ForegroundColor Green
}
Set-Location $projectDir

# ========== 第四步：创建package.json ==========
Write-Host "[步骤 4/8] 创建package.json..." -ForegroundColor Yellow
$packageJson = @"
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
    "express-rate-limit": "^6.8.1",
    "dotenv": "^16.3.1"
  }
}
"@
$packageJson | Out-File -FilePath "package.json" -Encoding utf8 -Force
Write-Host "  ✅ package.json已创建" -ForegroundColor Green

# ========== 第五步：创建server.js ==========
Write-Host "[步骤 5/8] 创建server.js..." -ForegroundColor Yellow
$serverJs = @'
const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: { success: false, message: '请求过于频繁，请稍后再试' }
});
app.use('/api/v1/redeem', limiter);

const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'redeem_api',
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME || 'redeem_codes',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  enableKeepAlive: true,
  keepAliveInitialDelay: 0
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
    console.error('查询兑换码错误:', error);
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
    console.error('查询用户兑换码错误:', error);
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
    console.error('检查兑换码错误:', error);
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
    console.error('使用兑换码错误:', error);
    res.status(500).json({ success: false, message: '服务器错误' });
  } finally {
    conn.release();
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`兑换码API服务运行在端口 ${PORT}`);
  console.log(`健康检查: http://localhost:${PORT}/health`);
  console.log(`API地址: http://localhost:${PORT}/api/v1/redeem`);
});
'@
$serverJs | Out-File -FilePath "server.js" -Encoding utf8 -Force
Write-Host "  ✅ server.js已创建" -ForegroundColor Green

# ========== 第六步：配置数据库 ==========
Write-Host "[步骤 6/8] 配置数据库..." -ForegroundColor Yellow
Write-Host ""
Write-Host "需要配置MySQL数据库，请输入以下信息：" -ForegroundColor Cyan
$mysqlRootPassword = Read-Host "MySQL root密码" -AsSecureString
$mysqlRootPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlRootPassword))

$apiDbPassword = Read-Host "API数据库用户密码（将创建新用户）" -AsSecureString
$apiDbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($apiDbPassword))

Write-Host ""
Write-Host "正在配置数据库..." -ForegroundColor Yellow

# 创建SQL脚本
$sqlScript = @"
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

DROP USER IF EXISTS `redeem_api`@`localhost`;
CREATE USER `redeem_api`@`localhost` IDENTIFIED BY '$apiDbPasswordPlain';
GRANT ALL PRIVILEGES ON redeem_codes.* TO `redeem_api`@`localhost`;
FLUSH PRIVILEGES;
"@

$sqlFile = Join-Path $projectDir "init_db.sql"
$sqlScript | Out-File -FilePath $sqlFile -Encoding utf8 -Force

# 执行SQL脚本
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysqlPath)) {
    # 尝试其他可能的路径
    $possiblePaths = @(
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\mysql\bin\mysql.exe"
    )
    $mysqlPath = $possiblePaths | Where-Object { Test-Path $_ } | Select-Object -First 1
}

if ($mysqlPath) {
    # 使用临时文件执行SQL
    Write-Host "  执行SQL脚本..." -ForegroundColor Yellow
    $tempSqlFile = Join-Path $env:TEMP "init_db_temp.sql"
    Copy-Item $sqlFile $tempSqlFile -Force
    
    # 使用mysql命令行执行（通过文件）
    $mysqlArgs = @(
        "-u", "root",
        "-p$mysqlRootPasswordPlain",
        "-e", "source $sqlFile"
    )
    
    try {
        $process = Start-Process -FilePath $mysqlPath -ArgumentList $mysqlArgs -Wait -NoNewWindow -PassThru -RedirectStandardError "$env:TEMP\mysql_error.txt"
        
        if ($process.ExitCode -eq 0) {
            Write-Host "  ✅ 数据库配置成功" -ForegroundColor Green
        } else {
            $errorContent = Get-Content "$env:TEMP\mysql_error.txt" -ErrorAction SilentlyContinue
            Write-Host "  ⚠️  数据库配置可能失败，请手动执行SQL脚本" -ForegroundColor Yellow
            Write-Host "     SQL脚本位置: $sqlFile" -ForegroundColor Yellow
            if ($errorContent) {
                Write-Host "     错误信息: $errorContent" -ForegroundColor Red
            }
        }
        Remove-Item "$env:TEMP\mysql_error.txt" -ErrorAction SilentlyContinue
    } catch {
        Write-Host "  ⚠️  数据库配置失败，请手动执行SQL脚本" -ForegroundColor Yellow
        Write-Host "     SQL脚本位置: $sqlFile" -ForegroundColor Yellow
        Write-Host "     错误: $_" -ForegroundColor Red
    }
} else {
    Write-Host "  ⚠️  未找到MySQL，请手动执行SQL脚本" -ForegroundColor Yellow
    Write-Host "     SQL脚本位置: $sqlFile" -ForegroundColor Yellow
    Write-Host "     执行命令: mysql -u root -p < $sqlFile" -ForegroundColor Yellow
}

# 初始化兑换码数据
Write-Host ""
Write-Host "正在初始化兑换码数据..." -ForegroundColor Yellow
$initCodesScript = @'
import mysql.connector
import sys

try:
    conn = mysql.connector.connect(
        host='localhost',
        user='root',
        password='{0}',
        database='redeem_codes'
    )
    cursor = conn.cursor()
    
    cursor.execute("SELECT COUNT(*) FROM redeem_codes")
    count = cursor.fetchone()[0]
    
    if count == 0:
        codes = []
        for i in range(1, 151):
            code = "SUPPORTER" + str(i).zfill(3)
            codes.append((code, 'supporter'))
        codes.append(('PROGM', 'gm'))
        sql = "INSERT INTO redeem_codes (code, type) VALUES (%s, %s)"
        cursor.executemany(sql, codes)
        conn.commit()
        print("已插入 " + str(len(codes)) + " 个兑换码")
    else:
        print("数据库中已有 " + str(count) + " 个兑换码，跳过初始化")
    
    cursor.close()
    conn.close()
except Exception as e:
    print("错误: " + str(e))
    sys.exit(1)
'@ -f $mysqlRootPasswordPlain

$initCodesFile = Join-Path $projectDir "init_codes_temp.py"
$initCodesScript | Out-File -FilePath $initCodesFile -Encoding utf8 -Force

# 检查Python
if (Get-Command python -ErrorAction SilentlyContinue) {
    Write-Host "  安装Python MySQL驱动..." -ForegroundColor Yellow
    python -m pip install mysql-connector-python --quiet 2>&1 | Out-Null
    Write-Host "  初始化兑换码数据..." -ForegroundColor Yellow
    python $initCodesFile
    Remove-Item $initCodesFile -Force -ErrorAction SilentlyContinue
    Write-Host "  ✅ 兑换码数据初始化完成" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Python未安装，请手动初始化兑换码数据" -ForegroundColor Yellow
    Write-Host "     使用 init_codes.py 脚本" -ForegroundColor Yellow
}

# ========== 第七步：创建.env文件 ==========
Write-Host "[步骤 7/8] 创建.env文件..." -ForegroundColor Yellow
$envFile = Join-Path $projectDir ".env"
$envContent = "DB_HOST=localhost`r`nDB_USER=redeem_api`r`nDB_PASSWORD=$apiDbPasswordPlain`r`nDB_NAME=redeem_codes`r`nPORT=3000"
$envContent | Out-File -FilePath $envFile -Encoding utf8 -Force
Write-Host "  ✅ .env文件已创建" -ForegroundColor Green

# ========== 第八步：安装依赖并启动服务 ==========
Write-Host "[步骤 8/8] 安装依赖并启动服务..." -ForegroundColor Yellow
Write-Host "  安装Node.js依赖包（这可能需要几分钟）..." -ForegroundColor Yellow
npm install --silent
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ 依赖安装完成" -ForegroundColor Green
} else {
    Write-Host "  ❌ 依赖安装失败" -ForegroundColor Red
    exit 1
}

# 安装PM2
Write-Host "  安装PM2进程管理器..." -ForegroundColor Yellow
npm install -g pm2 pm2-windows-startup --silent
pm2-startup install 2>&1 | Out-Null

# 停止旧服务（如果存在）
pm2 delete redeem-api 2>&1 | Out-Null

# 启动服务
Write-Host "  启动API服务..." -ForegroundColor Yellow
pm2 start server.js --name redeem-api --update-env
pm2 save
Start-Sleep -Seconds 2

# 检查服务状态
$pm2Status = pm2 list 2>&1
if ($pm2Status -match "redeem-api.*online") {
    Write-Host "  ✅ API服务启动成功" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  服务可能未正常启动，请检查日志: pm2 logs redeem-api" -ForegroundColor Yellow
}

# ========== 配置防火墙 ==========
Write-Host ""
Write-Host "配置防火墙..." -ForegroundColor Yellow
$firewallRule = Get-NetFirewallRule -DisplayName "Redeem API" -ErrorAction SilentlyContinue
if (-not $firewallRule) {
    New-NetFirewallRule -DisplayName "Redeem API" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow | Out-Null
    Write-Host "  ✅ 防火墙规则已添加" -ForegroundColor Green
} else {
    Write-Host "  ✅ 防火墙规则已存在" -ForegroundColor Green
}

# ========== 完成 ==========
Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "✅ 部署完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "服务器信息：" -ForegroundColor Cyan
Write-Host "  公网IP: 8.138.186.224" -ForegroundColor White
Write-Host "  本地测试: http://localhost:3000/health" -ForegroundColor White
Write-Host "  公网测试: http://8.138.186.224:3000/health" -ForegroundColor White
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "  1. 配置阿里云安全组，开放3000端口" -ForegroundColor White
Write-Host "  2. 测试API: curl http://8.138.186.224:3000/health" -ForegroundColor White
Write-Host "  3. 修改Android客户端BASE_URL为: http://8.138.186.224:3000/api/v1/redeem" -ForegroundColor White
Write-Host ""
Write-Host "常用命令：" -ForegroundColor Cyan
Write-Host "  查看日志: pm2 logs redeem-api" -ForegroundColor White
Write-Host "  重启服务: pm2 restart redeem-api" -ForegroundColor White
Write-Host "  停止服务: pm2 stop redeem-api" -ForegroundColor White
Write-Host "  查看状态: pm2 list" -ForegroundColor White
Write-Host ""

# 测试API
Write-Host "正在测试API..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000/health" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ API测试成功！" -ForegroundColor Green
        Write-Host "  响应: $($response.Content)" -ForegroundColor White
    }
} catch {
    Write-Host "  ⚠️  API测试失败，请检查日志: pm2 logs redeem-api" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "部署完成！如有问题，请查看日志或联系我。" -ForegroundColor Green
Write-Host ""

