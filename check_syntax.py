#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys

def check_brackets(filename):
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            content = f.read()
        
        opens = content.count('{')
        closes = content.count('}')
        
        print(f"File: {filename}")
        print(f"{{ count: {opens}")
        print(f"}} count: {closes}")
        print(f"Difference: {opens - closes}")
        
        if opens != closes:
            print("\n⚠️ WARNING: Unmatched braces detected!")
            
            # Find approximate location
            stack = []
            lines = content.split('\n')
            for i, line in enumerate(lines, 1):
                for char in line:
                    if char == '{':
                        stack.append(i)
                    elif char == '}':
                        if stack:
                            stack.pop()
                        else:
                            print(f"Extra }} at line {i}")
            
            if stack:
                print(f"\nUnclosed {{ found around these lines:")
                for line_num in stack[-5:]:  # Show last 5
                    print(f"  Line {line_num}")
        else:
            print("\n✅ All braces matched!")
        
        return opens == closes
    
    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == '__main__':
    if len(sys.argv) > 1:
        check_brackets(sys.argv[1])
    else:
        check_brackets(r'd:\AI\YCJY\app\src\main\java\com\example\yjcy\MainActivity.kt')
