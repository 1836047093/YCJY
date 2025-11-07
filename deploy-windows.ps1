# Windows Server 一键部署脚本（PowerShell）

## 使用方法

1. 以管理员身份打开PowerShell
2. 执行以下脚本

```powershell
# 设置执行策略（首次运行需要）
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 下载并执行部署脚本
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/your-repo/deploy-windows.ps1" -OutFile "deploy.ps1"
.\deploy.ps1
```

## 完整部署脚本

```powershell
# Windows Server 兑换码API部署脚本
# 需要管理员权限运行

Write-Host "==========================================" -ForegroundColor Green
Write-Host "兑换码API Windows Server部署脚本" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "错误：请以管理员身份运行此脚本" -ForegroundColor Red
    exit 1
}

# 1. 检查并安装Node.js
Write-Host "[1/6] 检查Node.js..." -ForegroundColor Yellow
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host "Node.js未安装，请手动安装：" -ForegroundColor Yellow
    Write-Host "1. 访问 https://nodejs.org/" -ForegroundColor Yellow
    Write-Host "2. 下载并安装Node.js 18.x LTS版本" -ForegroundColor Yellow
    Write-Host "3. 安装完成后重新运行此脚本" -ForegroundColor Yellow
    exit 1
} else {
    $nodeVersion = node -v
    Write-Host "Node.js已安装: $nodeVersion" -ForegroundColor Green
}

# 2. 检查并安装MySQL
Write-Host "[2/6] 检查MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
if (-not $mysqlService) {
    Write-Host "MySQL未安装，请手动安装：" -ForegroundColor Yellow
    Write-Host "1. 访问 https://dev.mysql.com/downloads/installer/" -ForegroundColor Yellow
    Write-Host "2. 下载MySQL Installer for Windows" -ForegroundColor Yellow
    Write-Host "3. 安装MySQL Server 8.0" -ForegroundColor Yellow
    Write-Host "4. 安装完成后重新运行此脚本" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "MySQL已安装" -ForegroundColor Green
    # 启动MySQL服务
    Start-Service $mysqlService.Name
    Write-Host "MySQL服务已启动" -ForegroundColor Green
}

# 3. 创建项目目录
Write-Host "[3/6] 创建项目目录..." -ForegroundColor Yellow
$projectDir = "C:\redeem-api"
if (-not (Test-Path $projectDir)) {
    New-Item -ItemType Directory -Path $projectDir | Out-Null
}
Set-Location $projectDir
Write-Host "项目目录: $projectDir" -ForegroundColor Green

# 4. 创建package.json
Write-Host "[4/6] 创建package.json..." -ForegroundColor Yellow
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
"@ | Out-File -FilePath "package.json" -Encoding utf8

# 5. 创建server.js（简化版）
Write-Host "[5/6] 创建server.js..." -ForegroundColor Yellow
# 这里需要从完整指南中复制server.js内容
Write-Host "请手动创建server.js文件，内容见部署指南" -ForegroundColor Yellow

# 6. 创建.env文件
Write-Host "[6/6] 创建.env文件..." -ForegroundColor Yellow
$dbPassword = Read-Host "请输入MySQL API用户密码"
@"
DB_HOST=localhost
DB_USER=redeem_api
DB_PASSWORD=$dbPassword
DB_NAME=redeem_codes
PORT=3000
"@ | Out-File -FilePath ".env" -Encoding utf8

# 7. 安装依赖
Write-Host "安装Node.js依赖..." -ForegroundColor Yellow
npm install

# 8. 安装PM2
Write-Host "安装PM2..." -ForegroundColor Yellow
npm install -g pm2
npm install -g pm2-windows-startup
pm2-startup install

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "部署完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 配置MySQL数据库（见部署指南）" -ForegroundColor Yellow
Write-Host "2. 创建server.js文件（见部署指南）" -ForegroundColor Yellow
Write-Host "3. 启动服务: pm2 start server.js --name redeem-api" -ForegroundColor Yellow
Write-Host "4. 配置防火墙开放3000端口" -ForegroundColor Yellow
Write-Host ""
```

