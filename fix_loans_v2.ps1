# 正确地在MainActivity.kt中插入loans参数

$file_path = 'd:\AI\YCJY\app\src\main\java\com\example\yjcy\MainActivity.kt'

$content = Get-Content $file_path -Raw

# 使用正则表达式替换：在complaints = complaints,之后插入loans = loans,
# 但只在InGameSettingsContent调用处（即后面跟着autoProcessComplaints的地方）
$pattern = '(complaints = complaints,)(\r?\n\s+)(autoProcessComplaints = autoProcessComplaints,)'
$replacement = '$1$2loans = loans,$2$3'

$newContent = $content -replace $pattern, $replacement

$newContent | Set-Content $file_path -Encoding UTF8 -NoNewline

Write-Host "修改完成"
