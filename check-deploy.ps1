# Windows Server 一键部署检查脚本
# 使用方法: powershell -ExecutionPolicy Bypass -File check-deploy.ps1

Write-Host "==========================================" -ForegroundColor Green
Write-Host "部署状态检查脚本" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""

$allOk = $true

# 1. 检查Node.js
Write-Host "[1/6] 检查Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node -v 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ Node.js已安装: $nodeVersion" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Node.js未安装" -ForegroundColor Red
        $allOk = $false
    }
} catch {
    Write-Host "  ❌ Node.js未安装" -ForegroundColor Red
    $allOk = $false
}

# 2. 检查npm
Write-Host "[2/6] 检查npm..." -ForegroundColor Yellow
try {
    $npmVersion = npm -v 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ npm已安装: $npmVersion" -ForegroundColor Green
    } else {
        Write-Host "  ❌ npm未安装" -ForegroundColor Red
        $allOk = $false
    }
} catch {
    Write-Host "  ❌ npm未安装" -ForegroundColor Red
    $allOk = $false
}

# 3. 检查MySQL服务
Write-Host "[3/6] 检查MySQL服务..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
if ($mysqlService) {
    $status = $mysqlService.Status
    if ($status -eq "Running") {
        Write-Host "  ✅ MySQL服务运行中" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  MySQL服务已安装但未运行" -ForegroundColor Yellow
        Write-Host "     运行以下命令启动: Start-Service $($mysqlService.Name)" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ MySQL服务未安装" -ForegroundColor Red
    $allOk = $false
}

# 4. 检查项目目录
Write-Host "[4/6] 检查项目目录..." -ForegroundColor Yellow
$projectDir = "C:\redeem-api"
if (Test-Path $projectDir) {
    Write-Host "  ✅ 项目目录存在: $projectDir" -ForegroundColor Green
    
    # 检查必要文件
    $requiredFiles = @("package.json", "server.js", ".env")
    foreach ($file in $requiredFiles) {
        $filePath = Join-Path $projectDir $file
        if (Test-Path $filePath) {
            Write-Host "    ✅ $file 存在" -ForegroundColor Green
        } else {
            Write-Host "    ❌ $file 不存在" -ForegroundColor Red
            $allOk = $false
        }
    }
    
    # 检查node_modules
    $nodeModules = Join-Path $projectDir "node_modules"
    if (Test-Path $nodeModules) {
        Write-Host "    ✅ node_modules 已安装" -ForegroundColor Green
    } else {
        Write-Host "    ⚠️  node_modules 未安装，运行: cd $projectDir; npm install" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ 项目目录不存在: $projectDir" -ForegroundColor Red
    $allOk = $false
}

# 5. 检查PM2
Write-Host "[5/6] 检查PM2..." -ForegroundColor Yellow
try {
    $pm2Version = pm2 -v 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ PM2已安装: $pm2Version" -ForegroundColor Green
        
        # 检查服务是否运行
        $pm2List = pm2 list 2>&1
        if ($pm2List -match "redeem-api") {
            Write-Host "    ✅ redeem-api 服务正在运行" -ForegroundColor Green
        } else {
            Write-Host "    ⚠️  redeem-api 服务未运行，运行: pm2 start server.js --name redeem-api" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ❌ PM2未安装" -ForegroundColor Red
        Write-Host "     安装命令: npm install -g pm2 pm2-windows-startup" -ForegroundColor Yellow
        $allOk = $false
    }
} catch {
    Write-Host "  ❌ PM2未安装" -ForegroundColor Red
    $allOk = $false
}

# 6. 检查防火墙
Write-Host "[6/6] 检查防火墙规则..." -ForegroundColor Yellow
$firewallRule = Get-NetFirewallRule -DisplayName "Redeem API" -ErrorAction SilentlyContinue
if ($firewallRule) {
    Write-Host "  ✅ 防火墙规则已配置" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  防火墙规则未配置" -ForegroundColor Yellow
    Write-Host "     运行: New-NetFirewallRule -DisplayName 'Redeem API' -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
if ($allOk) {
    Write-Host "✅ 所有检查通过！" -ForegroundColor Green
} else {
    Write-Host "⚠️  部分检查未通过，请根据上述提示修复" -ForegroundColor Yellow
}
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "测试API:" -ForegroundColor Cyan
Write-Host "  curl http://localhost:3000/health" -ForegroundColor White
Write-Host "  curl http://8.138.186.224:3000/health" -ForegroundColor White
Write-Host ""



