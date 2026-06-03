package com.matt.mes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matt.mes.system.dto.PermissionCreateRequest;
import com.matt.mes.system.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * PermissionController 单元测试
 *
 * 使用纯单元测试方式，不加载 Spring 上下文
 */
@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionController permissionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ==================== 周期 1：创建权限 ====================

    @Test
    @DisplayName("创建权限成功")
    void createPermission_success() {
        // Given: 创建权限请求
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setPermissionName("用户管理");
        request.setPermissionCode("user");
        request.setParentId(null);
        request.setSortOrder(1);
        request.setDescription("用户管理模块");

        when(permissionService.createPermission(any())).thenReturn(1L);

        // When: 调用 Controller
        var result = permissionController.createPermission(request);

        // Then: 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData());
    }

    // ==================== 周期 2：编辑权限 ====================

    @Test
    @DisplayName("编辑权限成功")
    void updatePermission_success() {
        // Given: 编辑权限请求
        Long permissionId = 1L;
        var request = new com.matt.mes.system.dto.PermissionUpdateRequest();
        request.setPermissionName("新名称");
        request.setSortOrder(2);
        request.setDescription("新描述");

        // When: 调用 Controller
        var result = permissionController.updatePermission(permissionId, request);

        // Then: 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== 周期 3：删除权限 ====================

    @Test
    @DisplayName("删除权限成功")
    void deletePermission_success() {
        // Given: 权限ID
        Long permissionId = 1L;

        // When: 调用 Controller
        var result = permissionController.deletePermission(permissionId);

        // Then: 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== 周期 4：查询权限列表 ====================

    @Test
    @DisplayName("查询权限列表成功")
    void getPermissionList_success() {
        // Given: Mock 返回数据
        var permission1 = com.matt.mes.system.dto.PermissionResponse.builder()
            .id(1L)
            .permissionName("用户管理")
            .permissionCode("user")
            .permissionType(1)
            .build();
        var permission2 = com.matt.mes.system.dto.PermissionResponse.builder()
            .id(2L)
            .permissionName("新增用户")
            .permissionCode("user:add")
            .permissionType(2)
            .parentId(1L)
            .build();

        when(permissionService.getPermissionList()).thenReturn(java.util.List.of(permission1, permission2));

        // When: 调用 Controller
        var result = permissionController.getPermissionList();

        // Then: 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    // ==================== 周期 5：查询权限树形结构 ====================

    @Test
    @DisplayName("查询权限树形结构成功")
    void getPermissionTree_success() {
        // Given: Mock 返回数据
        var childPermission = com.matt.mes.system.dto.PermissionTreeResponse.builder()
            .id(2L)
            .permissionName("新增用户")
            .permissionCode("user:add")
            .permissionType(2)
            .children(java.util.Collections.emptyList())
            .build();

        var modulePermission = com.matt.mes.system.dto.PermissionTreeResponse.builder()
            .id(1L)
            .permissionName("用户管理")
            .permissionCode("user")
            .permissionType(1)
            .children(java.util.List.of(childPermission))
            .build();

        when(permissionService.getPermissionTree()).thenReturn(java.util.List.of(modulePermission));

        // When: 调用 Controller
        var result = permissionController.getPermissionTree();

        // Then: 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getData().get(0).getChildren().size());
    }
}
