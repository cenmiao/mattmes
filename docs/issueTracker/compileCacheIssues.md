# 编译缓存问题记录

---

**问题编号**: ISS-002
**发现日期**: 2026-06-06
**问题级别**: 严重

---

## 问题现象

### 错误信息
调用批量删除工序接口 `DELETE /api/process/batchDelete` 时返回500错误:
```json
{
    "code": 500,
    "message": "系统异常，请稍后重试",
    "data": null
}
```

### 后端日志
```
java.lang.NoSuchMethodError: 'java.util.List com.matt.mes.business.service.ProcessService.batchDelete(java.util.List)'
```

### 影响范围
- 所有新添加的接口方法都可能出现此问题
- 特别是批量操作、新增功能等
- 影响前后端联调测试

---

## 根本原因

### 1. Spring Boot Maven插件缓存问题
- `mvn spring-boot:run` 在某些情况下使用缓存的编译结果
- 即使源代码已修改，运行时仍使用旧的class文件
- 导致方法签名不匹配的NoSuchMethodError

### 2. IDE文件锁定
- VSCode Java Language Server (PID 7448) 锁定了 `mattmes-business/target/classes` 目录
- 导致 `mvn clean` 无法删除旧的编译文件
- 错误信息: `Failed to delete ... Device or resource busy`

### 3. 编译产物验证
- 使用 `javap` 查看 `.class` 文件，方法确实存在且签名正确
- 说明编译本身成功，但运行环境使用了缓存

---

## 解决方案

### 标准解决流程

#### 步骤1: 停止所有Java进程
```bash
# 查找所有Java进程（包括IDE进程）
tasklist | findstr java.exe

# 强制终止所有Java进程
taskkill //F //PID <每个Java进程的PID>

# 特别注意: VSCode Java Language Server必须停止
# 否则会锁定编译文件目录
```

#### 步骤2: 重新打包项目
```bash
# 尝试清理并打包（推荐）
mvn -f mattmes/pom.xml clean package -DskipTests

# 如果clean失败（文件被锁定），跳过clean直接打包
mvn -f mattmes/pom.xml package -DskipTests
```

#### 步骤3: 使用jar包启动
```bash
# 使用java -jar运行打包好的jar文件
java -jar mattmes/mattmes-web/target/mattmes-web-1.0.0-SNAPSHOT.jar

# ❌ 禁止使用 mvn spring-boot:run
# mvn spring-boot:run 可能使用缓存，导致问题复发
```

#### 步骤4: 验证接口
```bash
# 使用curl或Postman验证接口功能
curl -X DELETE http://localhost:8080/api/process/batchDelete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "[1494, 1495, 1496]"

# 验证成功标志
# {"code":200,"message":"批量删除工序成功","data":[1494,1495,1496]}
```

---

## 修复的文件

### 优化技能文件
- `.claude/skills/build-and-run/skill.md` - 更新启动流程，禁止使用 `mvn spring-boot:run`

### 关键修改点
1. 强制要求使用 `mvn package` + `java -jar`
2. 添加停止所有Java进程的步骤
3. 说明文件锁定问题的处理方法
4. 新增"最佳实践"和"常见问题"章节

---

## 验证结果

### 编译验证
```bash
# 查看编译后的接口方法
javap -p mattmes/mattmes-business/target/classes/com/matt/mes/business/service/ProcessService.class

# 输出包含正确的方法签名
public abstract java.util.List<java.lang.Long> batchDelete(java.util.List<java.lang.Long>);
```

### 运行验证
- ✅ 后端启动成功（端口8080）
- ✅ 接口调用成功，返回200状态码
- ✅ 批量删除功能正常工作
- ✅ 数据库记录正确删除

### 对比验证

| 启动方式 | 结果 |
|---------|------|
| `mvn spring-boot:run` | ❌ NoSuchMethodError |
| `java -jar xxx.jar` | ✅ 正常工作 |

---

## 影响范围

### 影响的功能
- 工序管理模块：批量删除、新增、编辑、状态切换等
- 所有后端新增接口方法
- 前后端联调测试流程

### 影响的用户
- 开发人员：无法验证新功能
- 测试人员：接口测试失败
- 用户：功能无法使用

---

## 经验教训

### 核心原则
**永远不要在生产或重要功能验证时使用 `mvn spring-boot:run`**

### 最佳实践

#### 开发阶段
- 快速迭代可以使用IDE的热部署功能
- 但验证重要功能前必须完全重启

#### 功能验证流程
1. 停止所有Java进程（包括IDE）
2. `mvn package -DskipTests` 打包
3. `java -jar` 运行jar包
4. 使用curl验证接口
5. 在前端测试完整流程

#### 避免的错误
- ❌ 使用 `mvn spring-boot:run` 启动
- ❌ 使用 `mvn compile` 而不是 `mvn package`
- ❌ 不停止旧进程直接启动
- ❌ 忽略文件锁定错误

### 问题预防措施

#### 在build-and-run技能中已实施
1. ✅ 强制要求停止所有Java进程
2. ✅ 强制使用 `mvn package` 打包
3. ✅ 强制使用 `java -jar` 运行
4. ✅ 添加文件锁定问题处理方案
5. ✅ 新增最佳实践说明

#### 未来改进建议
- 考虑添加自动化脚本检测缓存问题
- 在打包前自动检测IDE进程并发送警告
- 建立CI/CD流程时必须使用jar包部署

---

## 相关链接

- [build-and-run技能文件](.claude/skills/build-and-run/skill.md)
- [问题索引](index.md)

---

## 历史记录

| 日期 | 操作 | 结果 |
|------|------|------|
| 2026-06-06 07:20 | 发现问题 | 批量删除接口500错误 |
| 2026-06-06 07:26 | 尝试重新编译 | clean失败，文件锁定 |
| 2026-06-06 07:29 | 停止Java进程 | 文件仍被锁定 |
| 2026-06-06 07:31 | 跳过clean打包 | package成功 |
| 2026-06-06 07:31 | jar包启动 | 接口验证成功 |
| 2026-06-06 07:35 | 优化技能文件 | 预防措施已实施 |

---