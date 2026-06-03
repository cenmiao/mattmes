# 0006-密码加密与Token格式规范

## 状态

已接受

## 背景

系统需要安全地存储用户密码,并使用Token进行身份认证。需要确定密码加密算法和Token格式。

## 决策

### 1. 密码加密算法

**选择**: BCrypt

**实现**:
```java
// Spring Security BCryptPasswordEncoder
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// 加密
String encodedPassword = passwordEncoder.encode(rawPassword);

// 验证
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

### 2. JWT Token格式

**临时Token(用户登录生成)**:
```json
{
  "userId": 1,
  "userNo": "admin",
  "exp": 1717056000,
  "iat": 1717034400
}
```

**永久Token(管理员手动生成)**:
- 格式: UUID (如 `550e8400-e29b-41d4-a716-446655440000`)
- 存储: `sys_permanent_token` 表
- 用途: 外部系统API调用认证

### 3. JWT签名配置

**配置文件**:
```yaml
jwt:
  secret: your-secret-key-at-least-256-bits-long
  expiration: 21600000  # 6小时(毫秒)
```

**密钥管理**:
- 开发环境: 配置文件明文
- 生产环境: 环境变量或密钥管理服务

## 理由

### 为什么选择BCrypt
1. **Spring Security默认支持**: 无需额外依赖
2. **自动加盐**: 每次加密生成不同哈希值,防止彩虹表攻击
3. **可配置强度**: 默认强度10,可根据需要调整
4. **行业标准**: 广泛使用,经过充分验证

### 为什么JWT不包含角色权限
1. **权限可能变化**: 管理员调整权限后,需立即生效
2. **Token长度**: 包含权限列表会增加Token体积
3. **安全性**: 减少Token泄露时的信息暴露

### 为什么永久Token用UUID而非JWT
1. **无需过期时间**: 永久Token长期有效
2. **便于管理**: 数据库可直接查询和管理
3. **安全性**: 泄露后可在数据库删除

## 影响

### 依赖影响
- 后端需要添加 `spring-boot-starter-security` 依赖(仅用于BCrypt)
- 后端需要添加 `jjwt` 依赖(JWT生成和解析)

### 数据库影响
- `sys_user.password` 字段存储BCrypt哈希值(60字符)
- `sys_permanent_token.token` 字段存储UUID(36字符)

### 接口影响
- 登录接口: 密码验证使用BCrypt
- 修改密码接口: 新密码BCrypt加密后存储
- Token验证拦截器: 解析JWT验证有效期
