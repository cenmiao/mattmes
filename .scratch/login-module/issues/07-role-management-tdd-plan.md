# 角色管理 TDD 开发计划（红绿重构循环）

## TDD 核心流程

每个开发周期严格遵循：

```
┌─────────────────────────────────────────────────────────────┐
│  RED（红）                                                   │
│  ─────                                                       │
│  1. 编写一个失败的测试                                        │
│  2. 测试描述期望的行为（不是实现细节）                         │
│  3. 运行测试 → 确认失败                                       │
├─────────────────────────────────────────────────────────────┤
│  GREEN（绿）                                                  │
│  ───────                                                      │
│  1. 编写最小代码使测试通过                                    │
│  2. 不添加额外功能，只满足当前测试                             │
│  3. 运行测试 → 确认通过                                       │
├─────────────────────────────────────────────────────────────┤
│  REFACTOR（重构）                                             │
│  ───────────                                                  │
│  1. 检查代码是否有重复、可优化的地方                           │
│  2. 重构后重新运行测试 → 确认仍然通过                          │
│  3. 不改变行为，只改善结构                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 周期 1：创建角色 - 成功路径

### RED（编写失败测试）

**测试代码**：
```java
@Test
@DisplayName("创建角色成功")
void createRole_success() {
    // Given: 创建角色请求
    RoleCreateRequest request = new RoleCreateRequest();
    request.setRoleName("测试角色");
    request.setRoleCode("TEST_ROLE");
    request.setDescription("这是一个测试角色");

    when(roleMapper.selectOne(any())).thenReturn(null); // 编码不存在
    when(roleMapper.insert(any())).thenAnswer(invocation -> {
        SysRole role = invocation.getArgument(0);
        role.setId(1L);
        return 1;
    });

    // When: 创建角色
    Long roleId = roleService.createRole(request);

    // Then: 返回角色ID
    assertNotNull(roleId);
    assertEquals(1L, roleId);
    
    // 验证插入的角色状态为启用
    verify(roleMapper).insert(argThat(role -> {
        SysRole r = (SysRole) role;
        return r.getRoleName().equals("测试角色")
            && r.getRoleCode().equals("TEST_ROLE")
            && r.getStatus() == 1;
    }));
}
```

**预期结果**：编译失败（RoleService、RoleCreateRequest 不存在）

**运行命令**：
```bash
mvn test -Dtest=RoleServiceTest#createRole_success -pl mattmes-system
```

---

### GREEN（最小实现）

**步骤 1**：创建 DTO
```java
// RoleCreateRequest.java
@Data
public class RoleCreateRequest {
    private String roleName;
    private String roleCode;
    private String description;
}
```

**步骤 2**：创建服务接口
```java
// RoleService.java
public interface RoleService {
    Long createRole(RoleCreateRequest request);
}
```

**步骤 3**：创建服务实现（最小代码）
```java
// RoleServiceImpl.java
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final SysRoleMapper roleMapper;
    
    @Override
    public Long createRole(RoleCreateRequest request) {
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(1);
        roleMapper.insert(role);
        return role.getId();
    }
}
```

**步骤 4**：创建测试类骨架
```java
// RoleServiceTest.java
class RoleServiceTest {
    
    @Mock
    private SysRoleMapper roleMapper;
    
    private RoleService roleService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleService = new RoleServiceImpl(roleMapper);
    }
    
    // 测试方法...
}
```

**运行命令**：
```bash
mvn test -Dtest=RoleServiceTest#createRole_success -pl mattmes-system
```

**预期结果**：测试通过 ✅

---

### REFACTOR（检查重构）

**检查点**：
- [ ] 是否有重复代码？→ 当前只有一个方法，无需重构
- [ ] 是否需要提取常量？→ SUPER_ADMIN 常量暂不需要
- [ ] 测试是否验证行为而非实现？→ ✅ 验证了返回值和插入状态

**结论**：本轮无需重构

---

## 周期 2：创建角色 - 编码重复校验

### RED（编写失败测试）

**测试代码**：
```java
@Test
@DisplayName("创建角色-编码已存在")
void createRole_roleCodeAlreadyExists() {
    // Given: 已存在的角色编码
    RoleCreateRequest request = new RoleCreateRequest();
    request.setRoleName("新角色");
    request.setRoleCode("EXISTING_CODE");

    SysRole existingRole = new SysRole();
    existingRole.setId(1L);
    existingRole.setRoleCode("EXISTING_CODE");

    when(roleMapper.selectOne(any())).thenReturn(existingRole);

    // When & Then: 抛出异常
    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> roleService.createRole(request)
    );

    assertEquals(400, exception.getCode());
    assertTrue(exception.getMessage().contains("已存在"));

    // 验证未执行插入
    verify(roleMapper, never()).insert(any());
}
```

**预期结果**：测试失败（当前实现不检查唯一性，会直接插入）

**运行命令**：
```bash
mvn test -Dtest=RoleServiceTest#createRole_roleCodeAlreadyExists -pl mattmes-system
```

---

### GREEN（最小修改）

**修改 RoleServiceImpl.createRole**：
```java
@Override
public Long createRole(RoleCreateRequest request) {
    // 1. 检查编码唯一性
    SysRole existingRole = roleMapper.selectOne(
        new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleCode, request.getRoleCode())
    );
    if (existingRole != null) {
        throw new BusinessException(400, "角色编码已存在");
    }
    
    // 2. 创建角色
    SysRole role = new SysRole();
    role.setRoleName(request.getRoleName());
    role.setRoleCode(request.getRoleCode());
    role.setDescription(request.getDescription());
    role.setStatus(1);
    roleMapper.insert(role);
    return role.getId();
}
```

**运行命令**：
```bash
mvn test -Dtest=RoleServiceTest -pl mattmes-system
```

**预期结果**：所有测试通过 ✅

---

### REFACTOR（检查重构）

**检查点**：
- [ ] 唯一性检查逻辑是否需要提取？→ 目前足够简单，暂不提取
- [ ] 错误消息是否需要统一？→ 后续可考虑 ResultCode 扩展

**结论**：本轮无需重构

---

## 周期 3：编辑角色

### RED

**测试 1**：
```java
@Test
@DisplayName("编辑角色成功")
void updateRole_success() {
    // Given: 存在的角色
    Long roleId = 1L;
    RoleUpdateRequest request = new RoleUpdateRequest();
    request.setRoleName("新名称");
    request.setDescription("新描述");

    SysRole role = createRole(1L, "旧名称", "OLD_CODE");
    when(roleMapper.selectById(roleId)).thenReturn(role);
    when(roleMapper.updateById(any())).thenReturn(1);

    // When: 编辑角色
    roleService.updateRole(roleId, request);

    // Then: 验证只更新了名称和描述，编码不变
    verify(roleMapper).updateById(argThat(r -> {
        SysRole updated = (SysRole) r;
        return updated.getRoleName().equals("新名称")
            && updated.getDescription().equals("新描述")
            && updated.getRoleCode().equals("OLD_CODE"); // 编码不变
    }));
}

@Test
@DisplayName("编辑角色-角色不存在")
void updateRole_roleNotFound() {
    // Given: 不存在的角色
    Long roleId = 999L;
    RoleUpdateRequest request = new RoleUpdateRequest();
    request.setRoleName("新名称");

    when(roleMapper.selectById(roleId)).thenReturn(null);

    // When & Then: 抛出异常
    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> roleService.updateRole(roleId, request)
    );

    assertEquals(400, exception.getCode());
    assertTrue(exception.getMessage().contains("不存在"));
}
```

---

### GREEN

**新增 DTO**：
```java
@Data
public class RoleUpdateRequest {
    private String roleName;
    private String description;
}
```

**扩展接口**：
```java
void updateRole(Long id, RoleUpdateRequest request);
```

**最小实现**：
```java
@Override
public void updateRole(Long id, RoleUpdateRequest request) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
        throw new BusinessException(400, "角色不存在");
    }
    
    // 只允许修改名称和描述，编码不可修改
    role.setRoleName(request.getRoleName());
    role.setDescription(request.getDescription());
    roleMapper.updateById(role);
}
```

---

### REFACTOR

**检查点**：
- [ ] createRole 和 updateRole 都有"不存在"检查 → 暂不提取，逻辑简单
- [ ] 辅助方法 createRole() 可复用 → ✅ 在测试类中已有

---

## 周期 4：分配权限

### RED

**测试代码**：
```java
@Test
@DisplayName("分配权限成功")
void assignPermissions_success() {
    // Given: 存在的角色
    Long roleId = 1L;
    AssignPermissionsRequest request = new AssignPermissionsRequest();
    request.setPermissionIds(Arrays.asList(1L, 2L, 3L));

    SysRole role = createRole(1L, "测试角色", "TEST");
    when(roleMapper.selectById(roleId)).thenReturn(role);
    when(rolePermissionMapper.delete(any())).thenReturn(5); // 删除旧关联
    when(rolePermissionMapper.insert(any())).thenReturn(1);

    // When: 分配权限
    roleService.assignPermissions(roleId, request);

    // Then: 验证先删除旧关联，再插入新关联
    verify(rolePermissionMapper).delete(any());
    verify(rolePermissionMapper, times(3)).insert(any());
}
```

---

### GREEN

**新增 DTO**：
```java
@Data
public class AssignPermissionsRequest {
    private List<Long> permissionIds;
}
```

**扩展接口和实现**：
```java
void assignPermissions(Long id, AssignPermissionsRequest request);

@Override
@Transactional
public void assignPermissions(Long id, AssignPermissionsRequest request) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
        throw new BusinessException(400, "角色不存在");
    }
    
    // 删除旧的权限关联
    rolePermissionMapper.delete(
        new LambdaQueryWrapper<SysRolePermission>()
            .eq(SysRolePermission::getRoleId, id)
    );
    
    // 插入新的权限关联
    if (request.getPermissionIds() != null) {
        for (Long permissionId : request.getPermissionIds()) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(id);
            rp.setPermissionId(permissionId);
            rolePermissionMapper.insert(rp);
        }
    }
}
```

**测试类需要新增 Mock**：
```java
@Mock
private SysRolePermissionMapper rolePermissionMapper;
```

---

### REFACTOR

**检查点**：
- [ ] 分配权限逻辑与 UserService.assignRoles 类似 → 可考虑提取通用方法，但暂不急

---

## 周期 5：禁用角色

### RED

```java
@Test
@DisplayName("禁用角色成功")
void disableRole_success() {
    Long roleId = 1L;
    SysRole role = createRole(1L, "测试", "TEST");
    role.setStatus(1);

    when(roleMapper.selectById(roleId)).thenReturn(role);
    when(roleMapper.updateById(any())).thenReturn(1);

    roleService.disableRole(roleId);

    verify(roleMapper).updateById(argThat(r -> 
        ((SysRole) r).getStatus() == 0
    ));
}
```

---

### GREEN

```java
void disableRole(Long id);

@Override
public void disableRole(Long id) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
        throw new BusinessException(400, "角色不存在");
    }
    role.setStatus(0);
    roleMapper.updateById(role);
}
```

---

## 周期 6：启用角色

### RED → GREEN

```java
@Test
@DisplayName("启用角色成功")
void enableRole_success() {
    Long roleId = 1L;
    SysRole role = createRole(1L, "测试", "TEST");
    role.setStatus(0);

    when(roleMapper.selectById(roleId)).thenReturn(role);
    when(roleMapper.updateById(any())).thenReturn(1);

    roleService.enableRole(roleId);

    verify(roleMapper).updateById(argThat(r -> 
        ((SysRole) r).getStatus() == 1
    ));
}

// 实现
@Override
public void enableRole(Long id) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
        throw new BusinessException(400, "角色不存在");
    }
    role.setStatus(1);
    roleMapper.updateById(role);
}
```

---

### REFACTOR

**检查点**：
- [ ] disableRole 和 enableRole 逻辑相似 → 可合并为 setStatus 方法？→ 暂不合并，语义更清晰

---

## 周期 7：删除角色 - 成功路径

### RED

```java
@Test
@DisplayName("删除角色成功")
void deleteRole_success() {
    Long roleId = 2L;
    SysRole role = createRole(2L, "普通角色", "NORMAL_ROLE");

    when(roleMapper.selectById(roleId)).thenReturn(role);
    when(userRoleMapper.delete(any())).thenReturn(3); // 删除用户关联
    when(rolePermissionMapper.delete(any())).thenReturn(5); // 删除权限关联
    when(roleMapper.deleteById(roleId)).thenReturn(1);

    roleService.deleteRole(roleId);

    // 验证清理顺序
    InOrder inOrder = inOrder(userRoleMapper, rolePermissionMapper, roleMapper);
    inOrder.verify(userRoleMapper).delete(any());
    inOrder.verify(rolePermissionMapper).delete(any());
    inOrder.verify(roleMapper).deleteById(roleId);
}
```

**新增 Mock**：
```java
@Mock
private SysUserRoleMapper userRoleMapper;
```

---

### GREEN

```java
void deleteRole(Long id);

@Override
@Transactional
public void deleteRole(Long id) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
        throw new BusinessException(400, "角色不存在");
    }
    
    // 清理用户角色关联
    userRoleMapper.delete(
        new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, id)
    );
    
    // 清理角色权限关联
    rolePermissionMapper.delete(
        new LambdaQueryWrapper<SysRolePermission>()
            .eq(SysRolePermission::getRoleId, id)
    );
    
    // 删除角色
    roleMapper.deleteById(id);
}
```

---

## 周期 8：删除角色 - 超级管理员保护

### RED

```java
@Test
@DisplayName("删除角色-超级管理员不可删除")
void deleteRole_cannotDeleteSuperAdmin() {
    Long roleId = 1L;
    SysRole role = createRole(1L, "超级管理员", "SUPER_ADMIN");

    when(roleMapper.selectById(roleId)).thenReturn(role);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> roleService.deleteRole(roleId)
    );

    assertEquals(400, exception.getCode());
    assertTrue(exception.getMessage().contains("超级管理员"));

    // 验证未执行删除
    verify(userRoleMapper, never()).delete(any());
    verify(rolePermissionMapper, never()).delete(any());
    verify(roleMapper, never()).deleteById(any());
}
```

**预期结果**：测试失败（当前实现不检查 SUPER_ADMIN）

---

### GREEN

```java
private static final String SUPER_ADMIN_CODE = "SUPER_ADMIN";

@Override
@Transactional
public void deleteRole(Long id) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
        throw new BusinessException(400, "角色不存在");
    }
    
    // 超级管理员不可删除
    if (SUPER_ADMIN_CODE.equals(role.getRoleCode())) {
        throw new BusinessException(400, "超级管理员角色不可删除");
    }
    
    // ... 清理逻辑
}
```

---

## 周期 9：查询角色列表

### RED → GREEN → REFACTOR

```java
@Test
@DisplayName("查询角色列表-无筛选")
void getRoleList_noFilter() {
    RoleQueryRequest request = new RoleQueryRequest();
    request.setPageNum(1);
    request.setPageSize(10);

    List<SysRole> roles = Arrays.asList(
        createRole(1L, "管理员", "ADMIN"),
        createRole(2L, "操作员", "OPERATOR")
    );

    when(roleMapper.selectPage(any(), any())).thenAnswer(invocation -> {
        Page<SysRole> page = invocation.getArgument(0);
        page.setRecords(roles);
        page.setTotal(2);
        return page;
    });
    when(userRoleMapper.selectList(any())).thenReturn(Collections.emptyList());

    PageResult<RoleResponse> result = roleService.getRoleList(request);

    assertEquals(2, result.getList().size());
    assertEquals(2L, result.getTotal());
}
```

**需要新增 DTO**：RoleQueryRequest, RoleResponse

---

## 周期 10：查询角色详情

### RED → GREEN

```java
@Test
@DisplayName("查询角色详情-成功")
void getRoleDetail_success() {
    Long roleId = 1L;
    SysRole role = createRole(1L, "管理员", "ADMIN");
    role.setDescription("管理员角色");

    when(roleMapper.selectById(roleId)).thenReturn(role);
    when(rolePermissionMapper.selectList(any())).thenReturn(Collections.emptyList());

    RoleResponse response = roleService.getRoleDetail(roleId);

    assertEquals(1L, response.getId());
    assertEquals("管理员", response.getRoleName());
    assertEquals("ADMIN", response.getRoleCode());
}
```

---

## 周期 11：REST 接口层

### 最后实现 Controller（可选集成测试）

```java
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    
    @PostMapping
    public Result<Long> createRole(@RequestBody RoleCreateRequest request) {
        return Result.success("创建角色成功", roleService.createRole(request));
    }
    
    // ... 其他接口
}
```

---

## 执行命令汇总

每个周期执行：

```bash
# RED：运行测试，确认失败
mvn test -Dtest=RoleServiceTest#<methodName> -pl mattmes-system

# GREEN：实现后运行测试，确认通过
mvn test -Dtest=RoleServiceTest -pl mattmes-system

# REFACTOR：重构后再次运行测试，确认通过
mvn test -Dtest=RoleServiceTest -pl mattmes-system
```

---

## 总结：11 个 TDD 周期

| 周期 | RED（测试） | GREEN（实现） | REFACTOR |
|------|-------------|---------------|----------|
| 1 | createRole_success | RoleService + createRole 逻辑 | 无需重构 |
| 2 | createRole_roleCodeAlreadyExists | 唯一性检查 | 无需重构 |
| 3 | updateRole_success/notFound | updateRole 方法 | 无需重构 |
| 4 | assignPermissions_success | assignPermissions 方法 | 可提取通用模式 |
| 5 | disableRole_success | disableRole 方法 | 无需重构 |
| 6 | enableRole_success | enableRole 方法 | 可合并为 setStatus |
| 7 | deleteRole_success | deleteRole + 关联清理 | 无需重构 |
| 8 | deleteRole_cannotDeleteSuperAdmin | SUPER_ADMIN 保护 | 无需重构 |
| 9 | getRoleList 测试 | getRoleList + DTO | 无需重构 |
| 10 | getRoleDetail 测试 | getRoleDetail 方法 | 无需重构 |
| 11 | Controller（可选） | RoleController | 无需重构 |