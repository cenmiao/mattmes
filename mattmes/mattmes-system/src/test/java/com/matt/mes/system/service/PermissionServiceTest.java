package com.matt.mes.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.system.dto.PermissionCreateRequest;
import com.matt.mes.system.dto.PermissionResponse;
import com.matt.mes.system.dto.PermissionTreeResponse;
import com.matt.mes.system.dto.PermissionUpdateRequest;
import com.matt.mes.system.entity.SysPermission;
import com.matt.mes.system.mapper.SysPermissionMapper;
import com.matt.mes.system.mapper.SysRolePermissionMapper;
import com.matt.mes.system.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PermissionService 单元测试
 */
class PermissionServiceTest {

    @Mock
    private SysPermissionMapper permissionMapper;

    @Mock
    private SysRolePermissionMapper rolePermissionMapper;

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        permissionService = new PermissionServiceImpl(permissionMapper, rolePermissionMapper);
    }

    // ==================== 周期 1：创建模块级权限 ====================

    @Test
    @DisplayName("创建模块级权限成功")
    void createModulePermission_success() {
        // Given: 创建模块级权限请求（parentId=null）
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setPermissionName("用户管理");
        request.setPermissionCode("user");
        request.setParentId(null);
        request.setSortOrder(1);
        request.setDescription("用户管理模块");

        when(permissionMapper.selectOne(any())).thenReturn(null);
        when(permissionMapper.insert(any())).thenAnswer(invocation -> {
            SysPermission permission = invocation.getArgument(0);
            permission.setId(1L);
            return 1;
        });

        // When: 创建权限
        Long permissionId = permissionService.createPermission(request);

        // Then: 返回权限ID，验证插入逻辑
        assertNotNull(permissionId);
        assertEquals(1L, permissionId);

        verify(permissionMapper).insert(argThat(p -> {
            SysPermission perm = (SysPermission) p;
            return perm.getPermissionName().equals("用户管理")
                && perm.getPermissionCode().equals("user")
                && perm.getPermissionType() == 1
                && perm.getParentId() == null
                && perm.getSortOrder() == 1;
        }));
    }

    // ==================== 周期 2：创建按钮级权限 ====================

    @Test
    @DisplayName("创建按钮级权限成功")
    void createButtonPermission_success() {
        // Given: 创建按钮级权限请求（parentId有值）
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setPermissionName("新增用户");
        request.setPermissionCode("user:add");
        request.setParentId(1L);
        request.setSortOrder(1);
        request.setDescription("新增用户按钮");

        when(permissionMapper.selectOne(any())).thenReturn(null);
        when(permissionMapper.insert(any())).thenAnswer(invocation -> {
            SysPermission permission = invocation.getArgument(0);
            permission.setId(2L);
            return 1;
        });

        // When: 创建权限
        Long permissionId = permissionService.createPermission(request);

        // Then: 返回权限ID，验证permissionType=2（按钮级）
        assertNotNull(permissionId);

        verify(permissionMapper).insert(argThat(p -> {
            SysPermission perm = (SysPermission) p;
            return perm.getPermissionType() == 2
                && perm.getParentId().equals(1L);
        }));
    }

    // ==================== 周期 3：权限编码重复校验 ====================

    @Test
    @DisplayName("创建权限-编码已存在")
    void createPermission_codeDuplicate() {
        // Given: 已存在的权限编码
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setPermissionName("新权限");
        request.setPermissionCode("user");

        SysPermission existingPermission = new SysPermission();
        existingPermission.setId(1L);
        existingPermission.setPermissionName("用户管理");
        existingPermission.setPermissionCode("user");

        when(permissionMapper.selectOne(any())).thenReturn(existingPermission);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> permissionService.createPermission(request)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已存在"));

        verify(permissionMapper, never()).insert(any());
    }

    // ==================== 周期 4：编辑权限 ====================

    @Test
    @DisplayName("编辑权限成功")
    void updatePermission_success() {
        // Given: 存在的权限
        Long permissionId = 1L;
        PermissionUpdateRequest request = new PermissionUpdateRequest();
        request.setPermissionName("新名称");
        request.setSortOrder(2);
        request.setDescription("新描述");

        SysPermission permission = new SysPermission();
        permission.setId(1L);
        permission.setPermissionName("旧名称");
        permission.setPermissionCode("user");
        permission.setPermissionType(1);
        permission.setParentId(null);

        when(permissionMapper.selectById(permissionId)).thenReturn(permission);
        when(permissionMapper.updateById(any())).thenReturn(1);

        // When: 编辑权限
        permissionService.updatePermission(permissionId, request);

        // Then: 验证只更新了名称、描述、排序号，编码不变
        verify(permissionMapper).updateById(argThat(p -> {
            SysPermission updated = (SysPermission) p;
            return updated.getPermissionName().equals("新名称")
                && updated.getSortOrder().equals(2)
                && updated.getDescription().equals("新描述")
                && updated.getPermissionCode().equals("user");
        }));
    }

    // ==================== 周期 5：编辑权限-不存在 ====================

    @Test
    @DisplayName("编辑权限-权限不存在")
    void updatePermission_notFound() {
        // Given: 不存在的权限
        Long permissionId = 999L;
        PermissionUpdateRequest request = new PermissionUpdateRequest();
        request.setPermissionName("新名称");

        when(permissionMapper.selectById(permissionId)).thenReturn(null);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> permissionService.updatePermission(permissionId, request)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 周期 6：删除按钮级权限 ====================

    @Test
    @DisplayName("删除按钮级权限成功")
    void deletePermission_buttonLevel() {
        // Given: 按钮级权限
        Long permissionId = 2L;
        SysPermission permission = new SysPermission();
        permission.setId(2L);
        permission.setPermissionName("新增用户");
        permission.setPermissionCode("user:add");
        permission.setPermissionType(2);
        permission.setParentId(1L);

        when(permissionMapper.selectById(permissionId)).thenReturn(permission);
        when(rolePermissionMapper.delete(any())).thenReturn(3);
        when(permissionMapper.deleteById(permissionId)).thenReturn(1);

        // When: 删除权限
        permissionService.deletePermission(permissionId);

        // Then: 验证清理了角色关联并删除了权限
        verify(rolePermissionMapper).delete(any());
        verify(permissionMapper).deleteById(permissionId);
    }

    // ==================== 周期 7：删除模块级权限（级联删除子权限） ====================

    @Test
    @DisplayName("删除模块级权限-级联删除子权限")
    void deletePermission_moduleLevel() {
        // Given: 模块级权限，有子权限
        Long moduleId = 1L;
        SysPermission modulePermission = new SysPermission();
        modulePermission.setId(1L);
        modulePermission.setPermissionName("用户管理");
        modulePermission.setPermissionCode("user");
        modulePermission.setPermissionType(1);
        modulePermission.setParentId(null);

        List<SysPermission> childPermissions = Arrays.asList(
            createPermission(2L, "新增用户", "user:add", 2, 1L),
            createPermission(3L, "编辑用户", "user:edit", 2, 1L)
        );

        when(permissionMapper.selectById(moduleId)).thenReturn(modulePermission);
        when(permissionMapper.selectList(any())).thenReturn(childPermissions);
        when(rolePermissionMapper.delete(any())).thenReturn(1);
        when(permissionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);
        when(permissionMapper.deleteById(moduleId)).thenReturn(1);

        // When: 删除模块级权限
        permissionService.deletePermission(moduleId);

        // Then: 验证级联删除
        // 子权限角色关联(2次) + 模块权限角色关联(1次) = 3次
        verify(rolePermissionMapper, times(3)).delete(any());
        // 删除子权限(1次delete(wrapper)) + 删除模块权限(1次deleteById)
        verify(permissionMapper).delete(any(LambdaQueryWrapper.class));
        verify(permissionMapper).deleteById(moduleId);
    }

    // ==================== 周期 8：删除权限-不存在 ====================

    @Test
    @DisplayName("删除权限-权限不存在")
    void deletePermission_notFound() {
        // Given: 不存在的权限
        Long permissionId = 999L;
        when(permissionMapper.selectById(permissionId)).thenReturn(null);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> permissionService.deletePermission(permissionId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 周期 9：查询权限列表 ====================

    @Test
    @DisplayName("查询权限列表-成功")
    void getPermissionList_success() {
        // Given: 权限数据
        List<SysPermission> permissions = Arrays.asList(
            createPermission(1L, "用户管理", "user", 1, null),
            createPermission(2L, "新增用户", "user:add", 2, 1L),
            createPermission(3L, "角色管理", "role", 1, null)
        );

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(permissions);

        // When: 查询权限列表
        List<PermissionResponse> result = permissionService.getPermissionList();

        // Then: 返回权限列表
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("用户管理", result.get(0).getPermissionName());
        assertEquals("user", result.get(0).getPermissionCode());
    }

    // ==================== 周期 10：查询权限树 ====================

    @Test
    @DisplayName("查询权限树-成功")
    void getPermissionTree_success() {
        // Given: 模块级权限和按钮级权限
        List<SysPermission> modulePermissions = Arrays.asList(
            createPermission(1L, "用户管理", "user", 1, null)
        );

        List<SysPermission> buttonPermissions = Arrays.asList(
            createPermission(2L, "新增用户", "user:add", 2, 1L),
            createPermission(3L, "编辑用户", "user:edit", 2, 1L)
        );

        // 第一次调用返回模块级权限，后续调用返回按钮级权限
        when(permissionMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(modulePermissions)
            .thenReturn(buttonPermissions);

        // When: 查询权限树
        List<PermissionTreeResponse> result = permissionService.getPermissionTree();

        // Then: 返回树形结构
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("用户管理", result.get(0).getPermissionName());
        assertEquals(2, result.get(0).getChildren().size());
        assertEquals("新增用户", result.get(0).getChildren().get(0).getPermissionName());
    }

    private SysPermission createPermission(Long id, String name, String code, Integer type, Long parentId) {
        SysPermission p = new SysPermission();
        p.setId(id);
        p.setPermissionName(name);
        p.setPermissionCode(code);
        p.setPermissionType(type);
        p.setParentId(parentId);
        return p;
    }
}
