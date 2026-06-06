
---

## 前端API路径重复问题

**问题编号**: ISS-001
**发现日期**: 2026-06-04
**问题级别**: 中等

### 问题现象

请求工序列表接口时报错：
```
请求路径: http://localhost:3000/api/api/process/list
错误信息: {"code":500,"message":"系统异常，请稍后重试","data":null}
```

后端日志显示：
```
org.springframework.web.servlet.resource.NoResourceFoundException: No static resource api/api/process/list.
```

### 根本原因

前端 API 文件 `mattmes-ui/src/api/process.ts` 中的路径定义为 `/api/process/list`，而 Vite 代理配置会将 `/api` 开头的请求转发到后端，导致最终请求路径变成 `/api/api/process/list`（路径中出现了重复的 `api`）。

**配置分析：**

1. **Vite 代理配置** (`mattmes-ui/vite.config.ts`):
   ```javascript
   proxy: {
     '/api': {
       target: 'http://localhost:8080',
       changeOrigin: true
     }
   }
   ```
   - 作用：将前端 `/api` 开头的请求转发到后端 `http://localhost:8080`

2. **前端 API 调用** (`mattmes-ui/src/api/process.ts`):
   ```typescript
   return request<ProcessPageResult>({
     url: '/api/process/list',  // ❌ 错误：包含了 /api 前缀
     method: 'post',
     data: params
   })
   ```

3. **实际请求路径**:
   - 前端发送：`/api/process/list`
   - Vite 代理添加：`/api` + `/api/process/list` = `/api/api/process/list`
   - 导致路径重复

### 解决方案

修改前端 API 路径，去掉 `/api` 前缀，只保留相对路径：

**修改前：**
```typescript
url: '/api/process/list'
```

**修改后：**
```typescript
url: '/process/list'
```

**原理：**
- 前端请求 `/process/list`
- Vite 代理自动添加 `/api` 前缀：`/api` + `/process/list` = `/api/process/list`
- 后端 Controller 路径：`@RequestMapping("/api/process")` + `@PostMapping("/list")` = `/api/process/list`
- 路径正确匹配 ✅

### 修复的文件

1. `mattmes-ui/src/api/process.ts` - 第47行

### 验证结果

- ✅ 后端接口 `/api/process/list` 正常工作
- ✅ 前端路径修复，请求路径为正确的 `/api/process/list`
- ✅ 返回数据格式正确：
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "list": [],
      "total": 0,
      "pageNum": 1,
      "pageSize": 10
    }
  }
  ```

### 影响范围

- 影响模块：工序管理模块
- 影响功能：工序列表查询
- 影响用户：所有访问工序管理页面的用户

### 经验教训

**最佳实践：**

1. **前端 API 路径规范**：
   - 不要在 API 文件中硬编码 `/api` 前缀
   - 由 Vite 代理或环境变量统一管理 API 前缀
   - 保持 API 路径的相对性，便于环境切换

2. **开发环境配置**：
   - 开发环境：使用 Vite 代理转发请求
   - 生产环境：使用相对路径或环境变量配置 API 基础地址

3. **路径规范**：
   - 后端 Controller：使用完整路径 `/api/模块/操作`
   - 前端 API：使用相对路径 `/模块/操作`
   - 代理配置：统一添加 `/api` 前缀

4. **代码检查**：
   - 新增 API 时检查路径是否正确
   - 确保前端路径与后端 Controller 路径匹配
   - 注意代理配置对路径的影响


