# 权限管理模块代码审核报告

**审核日期**: 2026-06-03
**审核范围**: 当前分支相对于main的改动
**审核模式**: max effort (5 angles × 8 candidates → 1-vote verify → sweep)
**审核结果**: 发现 7 个确认bug

---

## 一、审核概述

本次审核针对权限管理模块的CRUD实现，包括：
- `PermissionServiceImpl.java` - 服务实现层
- `PermissionController.java` - REST接口层
- `PermissionCreateRequest.java` - 创建请求DTO
- `PermissionUpdateRequest.java` - 更新请求DTO
- `PermissionResponse.java` - 响应DTO
- `PermissionTreeResponse.java` - 树形响应DTO

审核采用5个独立视角进行缺陷扫描，并通过验证器确认每个发现。

---

## 二、确认Bug列表

### Bug 1: 创建按钮级权限时未验证parentId是否存在（严重）

**文件**: `mattmes/mattmes-system/src/main/java/com/matt/mes/system/service/impl/PermissionServiceImpl.java`
**行号**: 52-56

**问题描述**:
创建按钮级权限时，代码仅根据`parentId`是否为null来判断权限类型，未验证`parentId`对应的父权限是否真实存在。

**触发场景**:
```
请求: {parentId: 999, permissionCode: "test:btn"}
结果: 创建了 parentId=999 的权限，但父权限不存在
影响: 权限树查询时该按钮不会出现在任何模块下，形成孤儿数据
```

**代码片段**:
```java
// 3. 根据parentId判断权限类型
if (request.getParentId() == null) {
    permission.setPermissionType(1); // 模块级
} else {
    permission.setPermissionType(2); // 按钮级
    // 缺失: 未验证 parentId 是否存在
}
```

**修复建议**:
```java
if (request.getParentId() != null) {
    SysPermission parent = permissionMapper.selectById(request.getParentId());
    if (parent == null) {
        throw new BusinessException(400, "父权限不存在");
    }
    if (parent.getPermissionType() != 1) {
        throw new BusinessException(400, "父权限必须是模块级权限");
    }
    permission.setPermissionType(2);
}
```

---

### Bug 2: 更新权限时null字段覆盖原有值（严重）

**文件**: `mattmes/mattmes-system/src/main/java/com/matt/mes/system/service/impl/PermissionServiceImpl.java`
**行号**: 71-75

**问题描述**:
`updatePermission`方法直接将request字段设置到entity，当request字段为null时，MyBatis-Plus的`updateById`会将数据库中的非null值更新为null。

**触发场景**:
```
请求: {} (空请求体)
结果: permissionName、sortOrder、description 均被设为 null
影响: 数据库UPDATE将这三个字段全部更新为NULL，原有数据丢失
```

**代码片段**:
```java
permission.setPermissionName(request.getPermissionName());
permission.setSortOrder(request.getSortOrder());
permission.setDescription(request.getDescription());
permissionMapper.updateById(permission); // null值会覆盖原有数据
```

**修复建议**:
```java
// 方案1: 仅更新非null字段
if (request.getPermissionName() != null) {
    permission.setPermissionName(request.getPermissionName());
}
if (request.getSortOrder() != null) {
    permission.setSortOrder(request.getSortOrder());
}
if (request.getDescription() != null) {
    permission.setDescription(request.getDescription());
}

// 方案2: 使用MyBatis-Plus的UpdateWrapper选择性更新
```

---

### Bug 3: DTO缺少验证注解，Controller未使用@Valid（严重）

**文件**: `mattmes/mattmes-system/src/main/java/com/matt/mes/system/dto/PermissionCreateRequest.java`
**行号**: 类定义

**问题描述**:
`PermissionCreateRequest`没有JSR-303验证注解，`PermissionController`未使用`@Valid`注解，导致null字段直接传递到Service层。

**触发场景**:
```
请求: {permissionName: "测试", permissionCode: null}
结果: MyBatis-Plus查询条件 eq(permissionCode, null) 生成 WHERE permission_code IS NULL
影响: 唯一性检查失效，或抛出SQL异常
```

**代码片段**:
```java
// PermissionCreateRequest.java - 缺少验证
@Data
public class PermissionCreateRequest {
    private String permissionName;  // 无 @NotBlank
    private String permissionCode;  // 无 @NotBlank
    // ...
}

// PermissionController.java - 缺少 @Valid
@PostMapping
public Result<Long> createPermission(@RequestBody PermissionCreateRequest request) {
    // 未验证 request 字段
}
```

**修复建议**:
```java
@Data
public class PermissionCreateRequest {
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    @Pattern(regexp = "^[a-z]+(:[a-z]+)?$", message = "权限编码格式不正确")
    private String permissionCode;
    // ...
}

// Controller
@PostMapping
public Result<Long> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
    // ...
}
```

---

### Bug 4: getPermissionTree存在N+1查询问题（中等）

**文件**: `mattmes/mattmes-system/src/main/java/com/matt/mes/system/service/impl/PermissionServiceImpl.java`
**行号**: 130-155

**问题描述**:
在`getPermissionTree`方法中，对每个模块级权限单独查询子权限，造成N+1查询性能问题。

**触发场景**:
```
数据: 数据库有100个模块级权限
结果: 执行1次模块查询 + 100次子权限查询 = 101次SQL
影响: 性能严重下降，数据库连接占用增加
```

**代码片段**:
```java
return modulePermissions.stream()
    .map(module -> {
        // 每个模块单独查询子权限 - N+1问题
        List<SysPermission> buttonPermissions = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getParentId, module.getId())
        );
        // ...
    })
    .collect(Collectors.toList());
```

**修复建议**:
```java
// 一次性查询所有权限，在内存中组装
List<SysPermission> allPermissions = permissionMapper.selectList(
    new LambdaQueryWrapper<SysPermission>()
        .orderByAsc(SysPermission::getSortOrder)
);

// 按 parentId 分组
Map<Long, List<SysPermission>> childrenMap = allPermissions.stream()
    .filter(p -> p.getParentId() != null)
    .collect(Collectors.groupingBy(SysPermission::getParentId));

// 构建树形结构
return allPermissions.stream()
    .filter(p -> p.getPermissionType() == 1)
    .map(module -> buildTreeResponse(module, childrenMap))
    .collect(Collectors.toList());
```

---

### Bug 5: parentId语义冲突（中等）

**文件**: `mattmes/mattmes-system/src/main/java/com/matt/mes/system/service/impl/PermissionServiceImpl.java`
**行号**: 52

**问题描述**:
代码用`parentId == null`判断模块级权限，但实体注释规定`parent_id = 0`表示顶级，存在语义冲突。

**触发场景**:
```
场景: 开发者按实体注释传 parentId=0 表示顶级权限
结果: 代码判断 parentId != null，设置 permissionType=2（按钮级）
影响: 错误分类为按钮级权限，违反业务规则
```

**代码片段**:
```java
// 实体注释
/** 父权限ID（0表示顶级） */
private Long parentId;

// 服务实现 - 用 null 判断
if (request.getParentId() == null) {
    permission.setPermissionType(1); // 模块级
}
```

**修复建议**:
```java
// 统一语义：将0视为null
Long effectiveParentId = request.getParentId();
if (effectiveParentId != null && effectiveParentId == 0) {
    effectiveParentId = null;
}

if (effectiveParentId == null) {
    permission.setPermissionType(1);
    permission.setParentId(0L); // 保持数据库一致性
} else {
    // 验证父权限存在
    permission.setPermissionType(2);
    permission.setParentId(effectiveParentId);
}
```

---

### Bug 6: 批量删除性能问题（中等）

**文件**: `mattmes/mattmes-system/src/main/java/com/matt/mes/system/service/impl/PermissionServiceImpl.java`
**行号**: 91-96

**问题描述**:
删除模块级权限的子权限角色关联时，在for循环中逐条执行delete，当子权限数量大时性能差。

**触发场景**:
```
数据: 模块有100个子权限
结果: 执行100次独立的 rolePermissionMapper.delete
影响: 高并发时可能耗尽数据库连接池
```

**代码片段**:
```java
for (SysPermission child : childPermissions) {
    rolePermissionMapper.delete(
        new LambdaQueryWrapper<SysRolePermission>()
            .eq(SysRolePermission::getPermissionId, child.getId())
    );
}
```

**修复建议**:
```java
// 批量收集子权限ID
List<Long> childIds = childPermissions.stream()
    .map(SysPermission::getId)
    .collect(Collectors.toList());

// 一次性删除所有子权限的角色关联
rolePermissionMapper.delete(
    new LambdaQueryWrapper<SysRolePermission>()
        .in(SysRolePermission::getPermissionId, childIds)
);
```

---

### Bug 7: Integer类型比较使用==而非equals（轻微）

**文件**: `mattmes/mattmes-system/src/main/java/com/matt/mes/system/service/impl/PermissionServiceImpl.java`
**行号**: 80

**问题描述**:
`permissionType`是`Integer`类型，使用`==`比较可能因缓存问题导致判断失败。当前值1和2在缓存范围内，暂不影响功能，但不符合最佳实践。

**触发场景**:
```
场景: 若未来permissionType值超出-128~127范围
结果: == 比较失败，始终返回false
影响: 模块权限无法正确识别，删除逻辑执行错误分支
```

**代码片段**:
```java
if (permission.getPermissionType() == 1) { // Integer 比较
    // ...
}
```

**修复建议**:
```java
// 使用 equals 比较
if (Integer.valueOf(1).equals(permission.getPermissionType())) {
    // ...
}

// 或使用常量
private static final int PERMISSION_TYPE_MODULE = 1;
private static final int PERMISSION_TYPE_BUTTON = 2;

if (permission.getPermissionType() != null
    && permission.getPermissionType() == PERMISSION_TYPE_MODULE) {
    // ...
}
```

---

## 三、Bug严重程度统计

| 严重程度 | 数量 | Bug编号 |
|---------|------|--------|
| 🔴 严重 | 3 | #1, #2, #3 |
| 🟡 中等 | 3 | #4, #5, #6 |
| 🟢 轻微 | 1 | #7 |

---

## 四、修复优先级建议

**高优先级（建议立即修复）**:
1. Bug #1 - 孤儿权限问题（数据完整性）
2. Bug #2 - null覆盖问题（数据丢失）
3. Bug #3 - 缺少验证（安全/数据完整性）

**中优先级（建议近期修复）**:
4. Bug #5 - parentId语义冲突（业务逻辑错误）
5. Bug #4 - N+1查询问题（性能）
6. Bug #6 - 批量删除性能（性能）

**低优先级（可后续优化）**:
7. Bug #7 - Integer比较（代码质量）

---

## 五、审核方法论

本次审核采用以下方法论：

1. **Angle A - 逐行扫描**: 检查条件反转、空指针、异常吞没等
2. **Angle B - 删除行为审计**: 检查缺失的约束和验证
3. **Angle C - 跨文件追踪**: 检查调用约定破坏
4. **Angle D - 语言陷阱**: 检查Java/Spring/MyBatis-Plus特有pitfalls
5. **Angle E - 包装器正确性**: 检查wrapper/proxy模式（本次不适用）

每个候选bug经过独立验证器确认，确保发现的bug真实有效。

---

**审核人**: Claude Code (max effort mode)
**报告生成时间**: 2026-06-03
