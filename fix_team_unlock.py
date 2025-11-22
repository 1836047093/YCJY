# -*- coding: utf-8 -*-
# 修复 MainActivity.kt 中的 onUnlockTeam 回调

file_path = r"app\src\main\java\com\example\yjcy\MainActivity.kt"

# 读取文件
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 要替换的旧代码
old_code = '''                        // 设置队徽
                        teamLogoConfig = logoConfig.copy(teamName = teamName)
                        // 显示成功消息
                        messageText = "✅ 成功解锁战队管理！\\n战队名称：${teamName}\\n扣除费用：¥1亿"
                        showMessage = true
                        android.util.Log.d("MainActivity", "战队管理解锁成功: 队名=${teamName}, 剩余资金=${money}")'''

# 新代码
new_code = '''                        // 设置队徽（包括成立时间和所属公司）
                        val foundedDate = "${currentYear}年${currentMonth}月"
                        teamLogoConfig = logoConfig.copy(
                            teamName = teamName,
                            foundedDate = foundedDate,
                            ownerCompany = companyName
                        )
                        // 显示成功消息
                        messageText = "✅ 成功解锁战队管理！\\n战队名称：${teamName}\\n成立时间：${foundedDate}\\n所属公司：${companyName}\\n扣除费用：¥1亿"
                        showMessage = true
                        android.util.Log.d("MainActivity", "战队管理解锁成功: 队名=${teamName}, 成立时间=${foundedDate}, 所属公司=${companyName}, 剩余资金=${money}")'''

# 执行替换
if old_code in content:
    new_content = content.replace(old_code, new_code)
    
    # 写回文件
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print("✅ 修改完成！")
else:
    print("❌ 未找到要替换的代码")
