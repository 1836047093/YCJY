#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# 在MainActivity.kt的第5436行后插入loans参数

file_path = r'd:\AI\YCJY\app\src\main\java\com\example\yjcy\MainActivity.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 在第5436行（索引5435）后插入新行
# 第5436行是: "                            complaints = complaints,"
# 第5437行是: "                            autoProcessComplaints = autoProcessComplaints,"
# 我们需要在这两行之间插入: "                            loans = loans,"

insert_line = '                            loans = loans,\n'
lines.insert(5436, insert_line)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print("成功在第5436行后插入loans参数")
