#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查Kotlin文件的括号匹配"""

def check_brackets(filename, start_line=2247, end_line=5882):
    """检查指定行范围内的括号匹配"""
    with open(filename, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    stack = []
    bracket_map = {')': '(', '}': '{', ']': '['}
    open_brackets = set('({[')
    close_brackets = set(')}]')
    
    print(f"检查 {filename} 第{start_line}-{end_line}行的括号匹配...")
    print("=" * 80)
    
    for i in range(start_line - 1, min(end_line, len(lines))):
        line = lines[i]
        line_num = i + 1
        
        # 跳过字符串内容（简化处理）
        in_string = False
        in_comment = False
        j = 0
        while j < len(line):
            char = line[j]
            
            # 检查注释
            if j < len(line) - 1 and line[j:j+2] == '//':
                in_comment = True
                break
            
            # 检查字符串
            if char == '"' and (j == 0 or line[j-1] != '\\'):
                in_string = not in_string
            
            # 只处理非字符串、非注释中的括号
            if not in_string and not in_comment:
                if char in open_brackets:
                    stack.append((char, line_num, len(stack)))
                elif char in close_brackets:
                    if not stack:
                        print(f"❌ 第{line_num}行: 多余的闭合括号 '{char}'")
                        print(f"   {line.rstrip()}")
                    elif bracket_map[char] != stack[-1][0]:
                        print(f"❌ 第{line_num}行: 括号不匹配")
                        print(f"   期望: {stack[-1][0]}, 实际: {char}")
                        print(f"   {line.rstrip()}")
                    else:
                        stack.pop()
            
            j += 1
    
    print("\n" + "=" * 80)
    if stack:
        print(f"⚠️  发现 {len(stack)} 个未闭合的括号:")
        for bracket, line_num, depth in stack[-10:]:  # 只显示最后10个
            print(f"   第{line_num}行: '{bracket}' (嵌套深度: {depth})")
    else:
        print("✅ 括号匹配正确!")
    
    return len(stack)

if __name__ == '__main__':
    unclosed = check_brackets(r'd:\AI\YCJY\app\src\main\java\com\example\yjcy\MainActivity.kt', 2247, 5882)
    print(f"\n未闭合括号总数: {unclosed}")
