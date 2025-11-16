# 正确地在MainActivity.kt中插入loans参数

$file_path = 'd:\AI\YCJY\app\src\main\java\com\example\yjcy\MainActivity.kt'

$lines = Get-Content $file_path

# 先移除错误插入的行（第5437行的loans = loans,）
$newLines = @()
for ($i = 0; $i -lt $lines.Count; $i++) {
    # 跳过第5437行（索引5436）如果它是错误插入的loans行
    if ($i -eq 5436 -and $lines[$i] -match '^\s+loans = loans,\s*$' -and $lines[$i] -notmatch 'complaints') {
        Write-Host "移除错误插入的行: $($lines[$i])"
        continue
    }
    $newLines += $lines[$i]
}

# 现在在正确的位置插入loans参数
# 查找 "complaints = complaints," 后面的 "autoProcessComplaints = autoProcessComplaints,"
$finalLines = @()
for ($i = 0; $i -lt $newLines.Count; $i++) {
    $finalLines += $newLines[$i]
    # 如果当前行包含 "complaints = complaints," 且下一行包含 "autoProcessComplaints"
    if ($newLines[$i] -match 'complaints = complaints,' -and 
        $i+1 -lt $newLines.Count -and 
        $newLines[$i+1] -match 'autoProcessComplaints = autoProcessComplaints,') {
        # 在它们之间插入loans行，使用相同的缩进
        $indent = '                            '
        $finalLines += ($indent + 'loans = loans,')
        Write-Host "在第$($i+1)行后插入loans参数"
    }
}

$finalLines | Set-Content $file_path -Encoding UTF8

Write-Host "修改完成"
