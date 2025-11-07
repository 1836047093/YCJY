# Windows Server 兑换码初始化脚本
# 使用方法: python init_codes.py

import mysql.connector
import sys

def init_redeem_codes():
    print("==========================================")
    print("兑换码数据库初始化脚本")
    print("==========================================")
    
    # 获取数据库连接信息
    root_password = input("请输入MySQL root密码: ")
    
    try:
        # 连接数据库
        print("\n[1/3] 连接MySQL数据库...")
        conn = mysql.connector.connect(
            host='localhost',
            user='root',
            password=root_password,
            database='redeem_codes'
        )
        cursor = conn.cursor()
        print("✅ 数据库连接成功")
        
        # 检查是否已有数据
        print("\n[2/3] 检查现有数据...")
        cursor.execute("SELECT COUNT(*) FROM redeem_codes")
        count = cursor.fetchone()[0]
        
        if count > 0:
            print(f"⚠️  数据库中已有 {count} 个兑换码")
            choice = input("是否清空并重新初始化？(y/n): ")
            if choice.lower() == 'y':
                cursor.execute("DELETE FROM redeem_codes")
                conn.commit()
                print("✅ 已清空旧数据")
            else:
                print("跳过初始化")
                cursor.close()
                conn.close()
                return
        
        # 插入兑换码
        print("\n[3/3] 插入兑换码数据...")
        codes = []
        
        # 支持者兑换码 SUPPORTER001-150
        for i in range(1, 151):
            code = f"SUPPORTER{i:03d}"
            codes.append((code, 'supporter'))
        
        # GM兑换码
        codes.append(('PROGM', 'gm'))
        
        # 批量插入
        sql = "INSERT INTO redeem_codes (code, type) VALUES (%s, %s)"
        cursor.executemany(sql, codes)
        conn.commit()
        
        print(f"✅ 成功插入 {len(codes)} 个兑换码")
        print(f"   - 支持者兑换码: 150个 (SUPPORTER001-150)")
        print(f"   - GM兑换码: 1个 (PROGM)")
        
        # 验证数据
        cursor.execute("SELECT COUNT(*) FROM redeem_codes WHERE type = 'supporter'")
        supporter_count = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM redeem_codes WHERE type = 'gm'")
        gm_count = cursor.fetchone()[0]
        
        print(f"\n验证结果:")
        print(f"   - 支持者兑换码: {supporter_count}个")
        print(f"   - GM兑换码: {gm_count}个")
        
        cursor.close()
        conn.close()
        
        print("\n==========================================")
        print("初始化完成！")
        print("==========================================")
        
    except mysql.connector.Error as err:
        print(f"\n❌ MySQL错误: {err}")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ 错误: {e}")
        sys.exit(1)

if __name__ == "__main__":
    init_redeem_codes()

