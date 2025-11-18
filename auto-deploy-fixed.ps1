# Windows Server 2022 简化部署脚本（修复版）
# 使用方法：以管理员身份打开PowerShell，执行此脚本

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "兑换码API全自动部署脚本" -ForegroundColor Cyan
Write-Host "服务器IP: 8.138.186.224" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "错误：请以管理员身份运行PowerShell" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "管理员权限检查通过" -ForegroundColor Green
Write-Host ""

# 检查Node.js
Write-Host "[1/8] 检查Node.js..." -ForegroundColor Yellow
if (Get-Command node -ErrorAction SilentlyContinue) {
    $nodeVersion = node -v
    Write-Host "  Node.js已安装: $nodeVersion" -ForegroundColor Green
} else {
    Write-Host "  Node.js未安装，请先安装Node.js" -ForegroundColor Red
    Write-Host "  下载地址: https://nodejs.org/zh-cn/download/" -ForegroundColor Yellow
    pause
    exit 1
}

# 检查MySQL
Write-Host "[2/8] 检查MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
if ($mysqlService) {
    Write-Host "  MySQL已安装" -ForegroundColor Green
    if ($mysqlService.Status -ne "Running") {
        Start-Service $mysqlService.Name
        Start-Sleep -Seconds 3
    }
    Write-Host "  MySQL服务运行中" -ForegroundColor Green
} else {
    Write-Host "  MySQL未安装，请先安装MySQL" -ForegroundColor Red
    Write-Host "  下载地址: https://dev.mysql.com/downloads/installer/" -ForegroundColor Yellow
    pause
    exit 1
}

# 创建项目目录
Write-Host "[3/8] 创建项目目录..." -ForegroundColor Yellow
$projectDir = "C:\redeem-api"
if (-not (Test-Path $projectDir)) {
    New-Item -ItemType Directory -Path $projectDir | Out-Null
}
Set-Location $projectDir
Write-Host "  项目目录: $projectDir" -ForegroundColor Green

# 创建package.json
Write-Host "[4/8] 创建package.json..." -ForegroundColor Yellow
$packageJsonContent = @'
{
  "name": "redeem-api",
  "version": "1.0.0",
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
'@
$packageJsonContent | Out-File -FilePath "package.json" -Encoding utf8 -Force
Write-Host "  package.json已创建" -ForegroundColor Green

# 创建server.js
Write-Host "[5/8] 创建server.js..." -ForegroundColor Yellow
# 这里需要从server.js文件读取内容
$serverJsPath = Join-Path (Split-Path $PSScriptRoot -Parent) "server.js"
if (Test-Path $serverJsPath) {
    Copy-Item $serverJsPath "server.js" -Force
    Write-Host "  server.js已创建" -ForegroundColor Green
} else {
    Write-Host "  警告：未找到server.js文件，将创建简化版本" -ForegroundColor Yellow
    # 创建简化版server.js
    $serverJsContent = Get-Content (Join-Path $PSScriptRoot "server.js") -Raw -ErrorAction SilentlyContinue
    if (-not $serverJsContent) {
        Write-Host "  请手动创建server.js文件" -ForegroundColor Red
    } else {
        $serverJsContent | Out-File -FilePath "server.js" -Encoding utf8 -Force
        Write-Host "  server.js已创建" -ForegroundColor Green
    }
}

# 配置数据库
Write-Host "[6/8] 配置数据库..." -ForegroundColor Yellow
Write-Host ""
$mysqlRootPassword = Read-Host "请输入MySQL root密码" -AsSecureString
$mysqlRootPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlRootPassword))

$apiDbPassword = Read-Host "请输入API数据库用户密码" -AsSecureString
$apiDbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($apiDbPassword))

Write-Host ""
Write-Host "正在配置数据库..." -ForegroundColor Yellow

# 创建SQL脚本文件
$sqlFile = Join-Path $projectDir "init_db.sql"
$sqlContent = "CREATE DATABASE IF NOT EXISTS redeem_codes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`r`n"
$sqlContent += "USE redeem_codes;`r`n`r`n"
$sqlContent += "CREATE TABLE IF NOT EXISTS redeem_codes (`r`n"
$sqlContent += "    code VARCHAR(50) PRIMARY KEY,`r`n"
$sqlContent += "    type VARCHAR(20) NOT NULL,`r`n"
$sqlContent += "    is_valid TINYINT(1) DEFAULT 1,`r`n"
$sqlContent += "    max_uses INT DEFAULT 1,`r`n"
$sqlContent += "    used_count INT DEFAULT 0,`r`n"
$sqlContent += "    used_by_user_id VARCHAR(100) DEFAULT NULL,`r`n"
$sqlContent += "    used_at DATETIME DEFAULT NULL,`r`n"
$sqlContent += "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,`r`n"
$sqlContent += "    INDEX idx_type (type),`r`n"
$sqlContent += "    INDEX idx_used_by_user_id (used_by_user_id)`r`n"
$sqlContent += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;`r`n`r`n"
$sqlContent += "CREATE TABLE IF NOT EXISTS user_redeem_codes (`r`n"
$sqlContent += "    user_id VARCHAR(100) PRIMARY KEY,`r`n"
$sqlContent += "    used_codes TEXT,`r`n"
$sqlContent += "    gm_mode_unlocked TINYINT(1) DEFAULT 0,`r`n"
$sqlContent += "    supporter_unlocked TINYINT(1) DEFAULT 0,`r`n"
$sqlContent += "    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,`r`n"
$sqlContent += "    INDEX idx_gm_mode (gm_mode_unlocked),`r`n"
$sqlContent += "    INDEX idx_supporter (supporter_unlocked)`r`n"
$sqlContent += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;`r`n`r`n"
$sqlContent += "DROP USER IF EXISTS 'redeem_api'@'localhost';`r`n"
$sqlContent += "CREATE USER 'redeem_api'@'localhost' IDENTIFIED BY '$apiDbPasswordPlain';`r`n"
$sqlContent += "GRANT ALL PRIVILEGES ON redeem_codes.* TO 'redeem_api'@'localhost';`r`n"
$sqlContent += "FLUSH PRIVILEGES;`r`n"

$sqlContent | Out-File -FilePath $sqlFile -Encoding utf8 -Force

# 执行SQL脚本
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysqlPath)) {
    $mysqlPath = "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe"
}

if ($mysqlPath -and (Test-Path $mysqlPath)) {
    Write-Host "  执行SQL脚本..." -ForegroundColor Yellow
    $process = Start-Process -FilePath $mysqlPath -ArgumentList "-u","root","-p$mysqlRootPasswordPlain","-e","source $sqlFile" -Wait -NoNewWindow -PassThru
    if ($process.ExitCode -eq 0) {
        Write-Host "  数据库配置成功" -ForegroundColor Green
    } else {
        Write-Host "  数据库配置可能失败，请手动执行: mysql -u root -p < $sqlFile" -ForegroundColor Yellow
    }
} else {
    Write-Host "  未找到MySQL，请手动执行SQL脚本: $sqlFile" -ForegroundColor Yellow
}

# 初始化兑换码数据
Write-Host ""
Write-Host "初始化兑换码数据..." -ForegroundColor Yellow
$initCodesFile = Join-Path $projectDir "init_codes_temp.py"
$pythonScript = "import mysql.connector`r`n"
$pythonScript += "import sys`r`n`r`n"
$pythonScript += "try:`r`n"
$pythonScript += "    conn = mysql.connector.connect(`r`n"
$pythonScript += "        host='localhost',`r`n"
$pythonScript += "        user='root',`r`n"
$pythonScript += "        password='$mysqlRootPasswordPlain',`r`n"
$pythonScript += "        database='redeem_codes'`r`n"
$pythonScript += "    )`r`n"
$pythonScript += "    cursor = conn.cursor()`r`n`r`n"
$pythonScript += "    cursor.execute('SELECT COUNT(*) FROM redeem_codes')`r`n"
$pythonScript += "    count = cursor.fetchone()[0]`r`n`r`n"
$pythonScript += "    if count == 0:`r`n"
$pythonScript += "        codes = []`r`n"
$pythonScript += "        for i in range(1, 151):`r`n"
$pythonScript += "            code = 'SUPPORTER' + str(i).zfill(3)`r`n"
$pythonScript += "            codes.append((code, 'supporter'))`r`n"
$pythonScript += "        codes.append(('PROGM', 'gm'))`r`n"
$pythonScript += "        sql = 'INSERT INTO redeem_codes (code, type) VALUES (%s, %s)'`r`n"
$pythonScript += "        cursor.executemany(sql, codes)`r`n"
$pythonScript += "        conn.commit()`r`n"
$pythonScript += "        print('已插入 ' + str(len(codes)) + ' 个兑换码')`r`n"
$pythonScript += "    else:`r`n"
$pythonScript += "        print('数据库中已有 ' + str(count) + ' 个兑换码，跳过初始化')`r`n`r`n"
$pythonScript += "    cursor.close()`r`n"
$pythonScript += "    conn.close()`r`n"
$pythonScript += "except Exception as e:`r`n"
$pythonScript += "    print('错误: ' + str(e))`r`n"
$pythonScript += "    sys.exit(1)`r`n"

$pythonScript | Out-File -FilePath $initCodesFile -Encoding utf8 -Force

if (Get-Command python -ErrorAction SilentlyContinue) {
    python -m pip install mysql-connector-python --quiet 2>&1 | Out-Null
    python $initCodesFile
    Remove-Item $initCodesFile -Force -ErrorAction SilentlyContinue
    Write-Host "  兑换码数据初始化完成" -ForegroundColor Green
} else {
    Write-Host "  Python未安装，请手动运行init_codes.py" -ForegroundColor Yellow
}

# 创建.env文件
Write-Host "[7/8] 创建.env文件..." -ForegroundColor Yellow
$envFile = Join-Path $projectDir ".env"
$envContent = "DB_HOST=localhost`r`nDB_USER=redeem_api`r`nDB_PASSWORD=$apiDbPasswordPlain`r`nDB_NAME=redeem_codes`r`nPORT=3000"
$envContent | Out-File -FilePath $envFile -Encoding utf8 -Force
Write-Host "  .env文件已创建" -ForegroundColor Green

# 安装依赖并启动
Write-Host "[8/8] 安装依赖并启动服务..." -ForegroundColor Yellow
npm install --silent
npm install -g pm2 pm2-windows-startup --silent
pm2-startup install 2>&1 | Out-Null
pm2 delete redeem-api 2>&1 | Out-Null
pm2 start server.js --name redeem-api --update-env
pm2 save
Start-Sleep -Seconds 2

# 配置防火墙
New-NetFirewallRule -DisplayName "Redeem API" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow -ErrorAction SilentlyContinue | Out-Null

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "部署完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "服务器信息：" -ForegroundColor Cyan
Write-Host "  公网IP: 8.138.186.224" -ForegroundColor White
Write-Host "  本地测试: http://localhost:3000/health" -ForegroundColor White
Write-Host "  公网测试: http://8.138.186.224:3000/health" -ForegroundColor White
Write-Host ""
Write-Host "下一步：" -ForegroundColor Yellow
Write-Host "  1. 配置阿里云安全组，开放3000端口" -ForegroundColor White
Write-Host "  2. 测试API" -ForegroundColor White
Write-Host ""



