# 国内兑换码API后端方案

## 📋 问题

国内用户不翻墙无法访问Firebase后端，需要国内可访问的解决方案。

## ✅ 解决方案

### 方案1：自建HTTP API后端（推荐）

**架构：**
- 后端API部署在国内服务器（阿里云/腾讯云/华为云等）
- 使用简单的HTTP REST API
- 数据库使用MySQL/PostgreSQL（部署在国内）
- 客户端使用OkHttp/Retrofit调用API

**优势：**
- ✅ 国内访问速度快（<100ms）
- ✅ 完全可控
- ✅ 成本低（小型服务器即可）
- ✅ 数据安全

### 方案2：使用国内BaaS服务

**可选服务：**
- LeanCloud（已移除，但可重新考虑）
- Bmob
- 野狗（Wilddog）
- 环信

**优势：**
- ✅ 无需自建服务器
- ✅ 快速接入
- ✅ 国内访问稳定

## 🚀 推荐实现：自建HTTP API

### 后端API设计

**基础URL：**
```
https://your-api-domain.com/api/v1/redeem
```

**接口列表：**

1. **查询兑换码**
   ```
   GET /redeem/code/{code}
   Response: {
     "code": "SUPPORTER001",
     "type": "supporter",
     "isValid": true,
     "isUsed": false,
     "usedByUserId": null
   }
   ```

2. **查询用户兑换码列表**
   ```
   GET /redeem/user/{userId}
   Response: {
     "userId": "xxx",
     "usedCodes": ["SUPPORTER001", "PROGM"],
     "gmModeUnlocked": true,
     "supporterUnlocked": true
   }
   ```

3. **使用兑换码**
   ```
   POST /redeem/use
   Body: {
     "userId": "xxx",
     "code": "SUPPORTER001"
   }
   Response: {
     "success": true,
     "message": "兑换成功"
   }
   ```

4. **检查兑换码是否已使用**
   ```
   GET /redeem/check?userId={userId}&code={code}
   Response: {
     "isUsed": true
   }
   ```

### 客户端实现

使用OkHttp或Retrofit调用API，示例代码见下方。

