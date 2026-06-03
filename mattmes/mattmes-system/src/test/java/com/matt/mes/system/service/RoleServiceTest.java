package com.matt.mes.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.system.dto.AssignPermissionsRequest;
import com.matt.mes.system.dto.PageResult;
import com.matt.mes.system.dto.RoleCreateRequest;
import com.matt.mes.system.dto.RoleQueryRequest;
import com.matt.mes.system.dto.RoleResponse;
import com.matt.mes.system.dto.RoleSimpleResponse;
import com.matt.mes.system.dto.RoleUpdateRequest;
import com.matt.mes.system.entity.SysRole;
import com.matt.mes.system.entity.SysRolePermission;
import com.matt.mes.system.entity.SysUserRole;
import com.matt.mes.system.mapper.SysRoleMapper;
import com.matt.mes.system.mapper.SysRolePermissionMapper;
import com.matt.mes.system.mapper.SysUserRoleMapper;
import com.matt.mes.system.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RoleService 单元测试
 *
 * TDD 周期 1：创建角色 - 成功路径
 */
class RoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysRolePermissionMapper rolePermissionMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleService = new RoleServiceImpl(roleMapper, rolePermissionMapper, userRoleMapper);
    }

    // ==================== 周期 1：创建角色 - 成功路径 ====================

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
            role.setId(1L); // 模拟 MyBatis-Plus 回填 ID
            return 1;
        });

        // When: 创建角色
        Long roleId = roleService.createRole(request);

        // Then: 返回角色 ID，验证插入逻辑
        assertNotNull(roleId);
        assertEquals(1L, roleId);

        // 验证插入的角色状态为启用
        verify(roleMapper).insert(argThat(role -> {
            SysRole r = (SysRole) role;
            return r.getRoleName().equals("测试角色")
                && r.getRoleCode().equals("TEST_ROLE")
                && r.getDescription().equals("这是一个测试角色")
                && r.getStatus() == 1;
        }));
    }

    // ==================== 周期 2：创建角色 - 编码重复校验 ====================

    @Test
    @DisplayName("创建角色-编码已存在")
    void createRole_roleCodeAlreadyExists() {
        // Given: 已存在的角色编码
        RoleCreateRequest request = new RoleCreateRequest();
        request.setRoleName("新角色");
        request.setRoleCode("EXISTING_CODE");

        SysRole existingRole = createRole(1L, "已存在角色", "EXISTING_CODE");

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

    // ==================== 周期 3：编辑角色 ====================

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

    // ==================== 周期 4：分配权限 ====================

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

    @Test
    @DisplayName("分配权限-角色不存在")
    void assignPermissions_roleNotFound() {
        // Given: 不存在的角色
        Long roleId = 999L;
        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(Arrays.asList(1L, 2L));

        when(roleMapper.selectById(roleId)).thenReturn(null);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> roleService.assignPermissions(roleId, request)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 周期 5：禁用角色 ====================

    @Test
    @DisplayName("禁用角色成功")
    void disableRole_success() {
        Long roleId = 1L;
        SysRole role = createRole(1L, "测试角色", "TEST");
        role.setStatus(1);

        when(roleMapper.selectById(roleId)).thenReturn(role);
        when(roleMapper.updateById(any())).thenReturn(1);

        roleService.disableRole(roleId);

        verify(roleMapper).updateById(argThat(r ->
            ((SysRole) r).getStatus() == 0
        ));
    }

    @Test
    @DisplayName("禁用角色-角色不存在")
    void disableRole_roleNotFound() {
        Long roleId = 999L;
        when(roleMapper.selectById(roleId)).thenReturn(null);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> roleService.disableRole(roleId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 周期 6：启用角色 ====================

    @Test
    @DisplayName("启用角色成功")
    void enableRole_success() {
        Long roleId = 1L;
        SysRole role = createRole(1L, "测试角色", "TEST");
        role.setStatus(0);

        when(roleMapper.selectById(roleId)).thenReturn(role);
        when(roleMapper.updateById(any())).thenReturn(1);

        roleService.enableRole(roleId);

        verify(roleMapper).updateById(argThat(r ->
            ((SysRole) r).getStatus() == 1
        ));
    }

    @Test
    @DisplayName("启用角色-角色不存在")
    void enableRole_roleNotFound() {
        Long roleId = 999L;
        when(roleMapper.selectById(roleId)).thenReturn(null);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> roleService.enableRole(roleId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 周期 7：删除角色 - 成功路径 ====================

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

    @Test
    @DisplayName("删除角色-角色不存在")
    void deleteRole_roleNotFound() {
        Long roleId = 999L;
        when(roleMapper.selectById(roleId)).thenReturn(null);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> roleService.deleteRole(roleId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 周期 8：删除角色 - 超级管理员保护 ====================

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
        verify(roleMapper, never()).deleteById(any(Long.class));
    }

    // ==================== 周期 9：查询角色列表 ====================

    @Test
    @DisplayName("查询角色列表-无筛选条件")
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

        assertNotNull(result);
        assertEquals(2, result.getList().size());
        assertEquals(2L, result.getTotal());
    }

    @Test
    @DisplayName("查询角色列表-带筛选条件")
    void getRoleList_withFilter() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setRoleName("管理");
        request.setStatus(1);
        request.setPageNum(1);
        request.setPageSize(10);

        List<SysRole> roles = Arrays.asList(
            createRole(1L, "管理员", "ADMIN")
        );

        when(roleMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<SysRole> page = invocation.getArgument(0);
            page.setRecords(roles);
            page.setTotal(1);
            return page;
        });
        when(userRoleMapper.selectList(any())).thenReturn(Collections.emptyList());

        PageResult<RoleResponse> result = roleService.getRoleList(request);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        assertEquals("管理员", result.getList().get(0).getRoleName());
    }

    // ==================== 周期 10：查询角色详情 ====================

    @Test
    @DisplayName("查询角色详情-成功")
    void getRoleDetail_success() {
        Long roleId = 1L;
        SysRole role = createRole(1L, "管理员", "ADMIN");
        role.setDescription("管理员角色");

        when(roleMapper.selectById(roleId)).thenReturn(role);
        when(rolePermissionMapper.selectList(any())).thenReturn(Collections.emptyList());

        RoleResponse response = roleService.getRoleDetail(roleId);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("管理员", response.getRoleName());
        assertEquals("ADMIN", response.getRoleCode());
    }

    @Test
    @DisplayName("查询角色详情-角色不存在")
    void getRoleDetail_roleNotFound() {
        Long roleId = 999L;
        when(roleMapper.selectById(roleId)).thenReturn(null);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> roleService.getRoleDetail(roleId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 周期 11：获取所有启用角色（用于下拉选项） ====================

    @Test
    @DisplayName("获取所有启用角色-成功")
    void getAllEnabledRoles_success() {
        // Given: 启用和禁用的角色混合
        SysRole enabledRole1 = createRole(1L, "管理员", "ADMIN");
        enabledRole1.setStatus(1);
        SysRole enabledRole2 = createRole(2L, "操作员", "OPERATOR");
        enabledRole2.setStatus(1);
        SysRole disabledRole = createRole(3L, "已禁用角色", "DISABLED");
        disabledRole.setStatus(0);

        when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            LambdaQueryWrapper<SysRole> wrapper = invocation.getArgument(0);
            // 模拟只返回启用状态的角色
            return Arrays.asList(enabledRole1, enabledRole2);
        });

        // When: 获取所有启用角色
        List<RoleSimpleResponse> result = roleService.getAllEnabledRoles();

        // Then: 只返回启用状态的角色
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("管理员", result.get(0).getRoleName());
        assertEquals("ADMIN", result.get(0).getRoleCode());
        assertEquals("操作员", result.get(1).getRoleName());
        assertEquals("OPERATOR", result.get(1).getRoleCode());

        // 验证查询条件只包含启用状态
        verify(roleMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取所有启用角色-无角色")
    void getAllEnabledRoles_noRoles() {
        // Given: 数据库中没有启用角色
        when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // When: 获取所有启用角色
        List<RoleSimpleResponse> result = roleService.getAllEnabledRoles();

        // Then: 返回空列表
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== 辅助方法 ====================

    private SysRole createRole(Long id, String roleName, String roleCode) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setStatus(1);
        return role;
    }
}