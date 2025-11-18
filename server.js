const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const app = express();

// 中间件
app.use(cors());
app.use(express.json());

// 限流：每15分钟最多100次请求
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: { success: false, message: '请求过于频繁，请稍后再试' }
});
app.use('/api/v1/redeem', limiter);

// 数据库连接池
const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'redeem_api',
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME || 'redeem_codes',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  enableKeepAlive: true,
  keepAliveInitialDelay: 0
});

// 健康检查
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// 查询兑换码
app.get('/api/v1/redeem/code/:code', async (req, res) => {
  try {
    const code = req.params.code.toUpperCase();
    const [rows] = await pool.execute(
      'SELECT * FROM redeem_codes WHERE code = ?',
      [code]
    );
    
    if (rows.length === 0) {
      return res.status(404).json({ 
        success: false, 
        message: '兑换码不存在' 
      });
    }
    
    const redeemCode = rows[0];
    res.json({
      code: redeemCode.code,
      type: redeemCode.type,
      isValid: redeemCode.is_valid === 1,
      isUsed: redeemCode.used_count > 0,
      usedByUserId: redeemCode.used_by_user_id
    });
  } catch (error) {
    console.error('查询兑换码错误:', error);
    res.status(500).json({ 
      success: false, 
      message: '服务器错误' 
    });
  }
});

// 查询用户兑换码列表
app.get('/api/v1/redeem/user/:userId', async (req, res) => {
  try {
    const userId = req.params.userId;
    const [rows] = await pool.execute(
      'SELECT * FROM user_redeem_codes WHERE user_id = ?',
      [userId]
    );
    
    if (rows.length === 0) {
      return res.json({
        userId: userId,
        usedCodes: [],
        gmModeUnlocked: false,
        supporterUnlocked: false
      });
    }
    
    const userData = rows[0];
    let usedCodes = [];
    try {
      usedCodes = JSON.parse(userData.used_codes || '[]');
    } catch (e) {
      usedCodes = [];
    }
    
    res.json({
      userId: userData.user_id,
      usedCodes: usedCodes,
      gmModeUnlocked: userData.gm_mode_unlocked === 1,
      supporterUnlocked: userData.supporter_unlocked === 1
    });
  } catch (error) {
    console.error('查询用户兑换码错误:', error);
    res.status(500).json({ 
      success: false, 
      message: '服务器错误' 
    });
  }
});

// 检查兑换码是否已使用
app.get('/api/v1/redeem/check', async (req, res) => {
  try {
    const { userId, code } = req.query;
    if (!userId || !code) {
      return res.status(400).json({ 
        success: false, 
        message: '参数不完整' 
      });
    }
    
    const codeUpper = code.toUpperCase();
    
    // 查询用户是否已使用
    const [userRows] = await pool.execute(
      'SELECT used_codes FROM user_redeem_codes WHERE user_id = ?',
      [userId]
    );
    
    if (userRows.length > 0) {
      let usedCodes = [];
      try {
        usedCodes = JSON.parse(userRows[0].used_codes || '[]');
      } catch (e) {
        usedCodes = [];
      }
      const isUsed = usedCodes.includes(codeUpper);
      return res.json({ isUsed });
    }
    
    res.json({ isUsed: false });
  } catch (error) {
    console.error('检查兑换码错误:', error);
    res.status(500).json({ 
      success: false, 
      message: '服务器错误' 
    });
  }
});

// 使用兑换码
app.post('/api/v1/redeem/use', async (req, res) => {
  const connection = await pool.getConnection();
  
  try {
    const { userId, code } = req.body;
    
    if (!userId || !code) {
      return res.status(400).json({ 
        success: false, 
        message: '参数不完整' 
      });
    }
    
    const codeUpper = code.toUpperCase();
    
    // 开始事务
    await connection.beginTransaction();
    
    try {
      // 1. 检查兑换码是否存在且有效
      const [codeRows] = await connection.execute(
        'SELECT * FROM redeem_codes WHERE code = ? FOR UPDATE',
        [codeUpper]
      );
      
      if (codeRows.length === 0) {
        await connection.rollback();
        return res.status(404).json({ 
          success: false, 
          message: '兑换码不存在' 
        });
      }
      
      const redeemCode = codeRows[0];
      
      if (redeemCode.is_valid !== 1) {
        await connection.rollback();
        return res.status(400).json({ 
          success: false, 
          message: '兑换码无效' 
        });
      }
      
      // 2. 检查是否已被其他用户使用（全局唯一）
      if (redeemCode.used_count > 0 && redeemCode.used_by_user_id !== userId) {
        await connection.rollback();
        return res.status(400).json({ 
          success: false, 
          message: '兑换码已被其他用户使用' 
        });
      }
      
      // 3. 检查用户是否已使用过
      const [userRows] = await connection.execute(
        'SELECT * FROM user_redeem_codes WHERE user_id = ? FOR UPDATE',
        [userId]
      );
      
      let usedCodes = [];
      if (userRows.length > 0) {
        try {
          usedCodes = JSON.parse(userRows[0].used_codes || '[]');
        } catch (e) {
          usedCodes = [];
        }
        if (usedCodes.includes(codeUpper)) {
          await connection.rollback();
          return res.json({ 
            success: true, 
            message: '兑换码已使用过' 
          });
        }
      }
      
      // 4. 更新兑换码状态
      await connection.execute(
        'UPDATE redeem_codes SET used_count = 1, used_by_user_id = ?, used_at = NOW() WHERE code = ?',
        [userId, codeUpper]
      );
      
      // 5. 更新用户兑换码记录
      usedCodes.push(codeUpper);
      const codeType = redeemCode.type;
      const isGM = codeType === 'gm';
      const isSupporter = codeType === 'supporter';
      
      if (userRows.length > 0) {
        // 更新现有记录
        await connection.execute(
          `UPDATE user_redeem_codes 
           SET used_codes = ?, 
               gm_mode_unlocked = CASE WHEN ? = 1 THEN 1 ELSE gm_mode_unlocked END,
               supporter_unlocked = CASE WHEN ? = 1 THEN 1 ELSE supporter_unlocked END,
               last_updated = NOW()
           WHERE user_id = ?`,
          [JSON.stringify(usedCodes), isGM ? 1 : 0, isSupporter ? 1 : 0, userId]
        );
      } else {
        // 创建新记录
        await connection.execute(
          'INSERT INTO user_redeem_codes (user_id, used_codes, gm_mode_unlocked, supporter_unlocked) VALUES (?, ?, ?, ?)',
          [userId, JSON.stringify(usedCodes), isGM ? 1 : 0, isSupporter ? 1 : 0]
        );
      }
      
      await connection.commit();
      res.json({ 
        success: true, 
        message: '兑换成功' 
      });
    } catch (error) {
      await connection.rollback();
      throw error;
    }
  } catch (error) {
    console.error('使用兑换码错误:', error);
    res.status(500).json({ 
      success: false, 
      message: '服务器错误' 
    });
  } finally {
    connection.release();
  }
});

// 错误处理
app.use((err, req, res, next) => {
  console.error('未处理的错误:', err);
  res.status(500).json({ 
    success: false, 
    message: '服务器内部错误' 
  });
});

// 启动服务器
const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`兑换码API服务运行在端口 ${PORT}`);
  console.log(`健康检查: http://localhost:${PORT}/health`);
  console.log(`API地址: http://localhost:${PORT}/api/v1/redeem`);
});



