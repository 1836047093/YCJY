# Maestro 安装脚本
$maestroUrl = "https://github.com/mobile-dev-inc/maestro/releases/latest/download/maestro.zip"
$tempZip = "$env:TEMP\maestro.zip"
$installDir = "$env:USERPROFILE\.maestro"

Write-Host "正在下载 Maestro..." -ForegroundColor Green
$ProgressPreference = 'SilentlyContinue'
Invoke-WebRequest -Uri $maestroUrl -OutFile $tempZip

Write-Host "正在解压..." -ForegroundColor Green
if (Test-Path $installDir) {
    Remove-Item $installDir -Recurse -Force
}
Expand-Archive -Path $tempZip -DestinationPath $installDir -Force

Write-Host "正在添加到环境变量..." -ForegroundColor Green
$maestroBin = Join-Path $installDir "maestro\bin"
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -notlike "*$maestroBin*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$maestroBin", "User")
    $env:Path += ";$maestroBin"
}

Write-Host "清理临时文件..." -ForegroundColor Green
Remove-Item $tempZip -Force

Write-Host "" -ForegroundColor Green
Write-Host "✅ Maestro 安装成功！" -ForegroundColor Green
Write-Host "请重新打开终端或运行以下命令来刷新环境变量：" -ForegroundColor Yellow
Write-Host '$env:Path = [System.Environment]::GetEnvironmentVariable("Path","User")' -ForegroundColor Cyan
Write-Host "" -ForegroundColor Green
Write-Host "验证安装：maestro --version" -ForegroundColor Cyan
