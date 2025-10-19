@echo off
echo ========================================
echo Maestro 自动安装脚本
echo ========================================
echo.

echo [1/4] 下载 Maestro...
powershell -Command "& {$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri 'https://github.com/mobile-dev-inc/maestro/releases/latest/download/maestro.zip' -OutFile '%TEMP%\maestro.zip'}"
if errorlevel 1 (
    echo 下载失败！
    pause
    exit /b 1
)
echo 下载完成！

echo.
echo [2/4] 解压 Maestro...
powershell -Command "& {Expand-Archive -Path '%TEMP%\maestro.zip' -DestinationPath '%USERPROFILE%\.maestro' -Force}"
if errorlevel 1 (
    echo 解压失败！
    pause
    exit /b 1
)
echo 解压完成！

echo.
echo [3/4] 添加到环境变量...
setx PATH "%PATH%;%USERPROFILE%\.maestro\maestro\bin"
echo 环境变量已更新！

echo.
echo [4/4] 清理临时文件...
del "%TEMP%\maestro.zip"

echo.
echo ========================================
echo ✅ Maestro 安装成功！
echo ========================================
echo.
echo 重要提示：
echo 1. 请关闭当前终端窗口
echo 2. 重新打开一个新的终端
echo 3. 运行：maestro --version
echo.
pause
