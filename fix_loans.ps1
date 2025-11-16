# 在MainActivity.kt的第5436行后插入loans参数

$file_path = 'd:\AI\YCJY\app\src\main\java\com\example\yjcy\MainActivity.kt'

$lines = Get-Content $file_path

# 在第5436行（索引5435）后插入新行
$newLines = @()
for ($i = 0; $i -lt $lines.Count; $i++) {
    $newLines += $lines[$i]
    if ($i -eq 5435) {
        $newLines += '                            loans = loans,'
    }
}

$newLines | Set-Content $file_path -Encoding UTF8

Write-Host "成功在第5436行后插入loans参数"
