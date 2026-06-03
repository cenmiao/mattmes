package com.matt.mes.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.system.dto.*;
import com.matt.mes.system.entity.SysRole;
import com.matt.mes.system.entity.SysUser;
import com.matt.mes.system.entity.SysUserRole;
import com.matt.mes.system.mapper.SysRoleMapper;
import com.matt.mes.system.mapper.SysUserMapper;
import com.matt.mes.system.mapper.SysUserRoleMapper;
import com.matt.mes.system.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService单元测试
 */
class UserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserService userService;

    private static final String DEFAULT_PASSWORD = "Reset@123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userMapper, roleMapper, userRoleMapper, passwordEncoder);
    }

    // ==================== 创建用户测试 ====================

    @Test
    @DisplayName("创建用户成功")
    void createUser_success() {
        // Given: 创建用户请求
        UserCreateRequest request = new UserCreateRequest();
        request.setUserNo("test001");
        request.setName("测试用户");
        request.setPassword("Test@123");
        request.setPhone("13800138000");
        request.setEmail("test@example.com");
        request.setRoleIds(Arrays.asList(1L, 2L));

        when(userMapper.selectOne(any())).thenReturn(null); // 工号不存在
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$encodedPassword");
        when(userMapper.insert(any())).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(100L); // 模拟MyBatis-Plus回填ID
            return 1;
        });
        when(roleMapper.selectBatchIds(any())).thenReturn(Arrays.asList(
                createRole(1L, "管理员", "ADMIN"),
                createRole(2L, "操作员", "OPERATOR")
        ));
        when(userRoleMapper.insert(any())).thenReturn(1);

        // When: 创建用户
        Long userId = userService.createUser(request);

        // Then: 返回用户ID，验证插入逻辑
        assertNotNull(userId);
        assertEquals(100L, userId);

        // 验证密码加密
        verify(passwordEncoder).encode("Test@123");

        // 验证用户插入（passwordUpdateTime为null）
        verify(userMapper).insert(argThat(user -> {
            SysUser u = (SysUser) user;
            return u.getUserNo().equals("test001")
                    && u.getName().equals("测试用户")
                    && u.getPasswordUpdateTime() == null // 首次登录强制修改
                    && u.getStatus() == 1;
        }));

        // 验证角色关联插入
        verify(userRoleMapper, times(2)).insert(any());
    }

    @Test
    @DisplayName("创建用户-使用默认密码")
    void createUser_withDefaultPassword() {
        // Given: 不指定密码
        UserCreateRequest request = new UserCreateRequest();
        request.setUserNo("test002");
        request.setName("测试用户2");
        request.setRoleIds(Collections.emptyList());

        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn("$2a$10$encodedDefaultPassword");
        when(userMapper.insert(any())).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(101L); // 模拟MyBatis-Plus回填ID
            return 1;
        });

        // When: 创建用户
        Long userId = userService.createUser(request);

        // Then: 使用默认密码
        assertNotNull(userId);
        assertEquals(101L, userId);
        verify(passwordEncoder).encode(DEFAULT_PASSWORD);
    }

    @Test
    @DisplayName("创建用户-工号已存在")
    void createUser_userNoAlreadyExists() {
        // Given: 工号已存在
        UserCreateRequest request = new UserCreateRequest();
        request.setUserNo("admin");
        request.setName("管理员");

        when(userMapper.countByUserNoIncludingDeleted("admin")).thenReturn(1L);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(request)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已存在"));

        // 验证未执行插入
        verify(userMapper, never()).insert(any());
    }

    // ==================== 查询用户测试 ====================

    @Test
    @DisplayName("查询用户列表-无筛选条件")
    void getUserList_noFilter() {
        // Given: 无筛选条件
        UserQueryRequest request = new UserQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        List<SysUser> users = Arrays.asList(
                createUser(1L, "admin", "管理员"),
                createUser(2L, "test001", "测试用户")
        );

        when(userMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<SysUser> page = invocation.getArgument(0);
            page.setRecords(users);
            page.setTotal(2);
            return page;
        });
        when(userRoleMapper.selectList(any())).thenReturn(Collections.emptyList());

        // When: 查询用户列表
        PageResult<UserResponse> result = userService.getUserList(request);

        // Then: 返回用户列表
        assertNotNull(result);
        assertEquals(2, result.getList().size());
        assertEquals(2L, result.getTotal());
    }

    @Test
    @DisplayName("查询用户详情-成功")
    void getUserDetail_success() {
        // Given: 用户ID
        Long userId = 1L;
        SysUser user = createUser(1L, "admin", "管理员");
        user.setPhone("13800138000");
        user.setEmail("admin@example.com");
        user.setLastLoginTime(LocalDateTime.now().minusDays(1));

        when(userMapper.selectById(userId)).thenReturn(user);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.emptyList());

        // When: 查询用户详情
        UserResponse response = userService.getUserDetail(userId);

        // Then: 返回用户详情
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("admin", response.getUserNo());
        assertEquals("管理员", response.getName());
        assertEquals("13800138000", response.getPhone());
        assertEquals("admin@example.com", response.getEmail());
    }

    @Test
    @DisplayName("查询用户详情-用户不存在")
    void getUserDetail_userNotFound() {
        // Given: 不存在的用户ID
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.getUserDetail(userId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 辅助方法 ====================

    private SysUser createUser(Long id, String userNo, String name) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUserNo(userNo);
        user.setName(name);
        user.setStatus(1);
        return user;
    }

    private SysRole createRole(Long id, String roleName, String roleCode) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setStatus(1);
        return role;
    }

    // ==================== 编辑用户测试 ====================

    @Test
    @DisplayName("编辑用户成功")
    void updateUser_success() {
        // Given: 存在的用户
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("新名字");
        request.setPhone("13900139000");
        request.setEmail("new@example.com");

        SysUser user = createUser(1L, "test001", "旧名字");
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 编辑用户
        userService.updateUser(userId, request);

        // Then: 验证更新了用户信息（工号不可修改）
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getName().equals("新名字")
                    && updated.getPhone().equals("13900139000")
                    && updated.getEmail().equals("new@example.com")
                    && updated.getUserNo().equals("test001"); // 工号未变
        }));
    }

    @Test
    @DisplayName("编辑用户-同时更新角色")
    void updateUser_withRoleIds() {
        // Given: 存在的用户，需要更新角色
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("新名字");
        request.setRoleIds(Arrays.asList(1L, 2L));

        SysUser user = createUser(1L, "test001", "旧名字");
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);
        when(userRoleMapper.delete(any())).thenReturn(1); // 删除旧角色
        when(userRoleMapper.insert(any())).thenReturn(1); // 插入新角色

        // When: 编辑用户并更新角色
        userService.updateUser(userId, request);

        // Then: 验证更新了用户信息，同时更新了角色
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getName().equals("新名字");
        }));
        // 验证角色更新：先删除旧角色，再插入新角色
        verify(userRoleMapper).delete(any());
        verify(userRoleMapper, times(2)).insert(any());
    }

    @Test
    @DisplayName("编辑用户-角色为空列表时清空角色")
    void updateUser_withEmptyRoleIds() {
        // Given: 存在的用户，需要清空角色
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("新名字");
        request.setRoleIds(Collections.emptyList());

        SysUser user = createUser(1L, "test001", "旧名字");
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);
        when(userRoleMapper.delete(any())).thenReturn(1); // 删除旧角色

        // When: 编辑用户并清空角色
        userService.updateUser(userId, request);

        // Then: 验证更新了用户信息，同时清空了角色
        verify(userMapper).updateById(any());
        // 验证角色清空：只删除，不插入
        verify(userRoleMapper).delete(any());
        verify(userRoleMapper, never()).insert(any());
    }

    @Test
    @DisplayName("编辑用户-不传角色时不更新角色")
    void updateUser_withoutRoleIds() {
        // Given: 存在的用户，不更新角色
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("新名字");
        // roleIds 为 null，表示不更新角色

        SysUser user = createUser(1L, "test001", "旧名字");
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 编辑用户但不更新角色
        userService.updateUser(userId, request);

        // Then: 验证更新了用户信息，但不更新角色
        verify(userMapper).updateById(any());
        // 验证未修改角色
        verify(userRoleMapper, never()).delete(any());
        verify(userRoleMapper, never()).insert(any());
    }

    @Test
    @DisplayName("编辑用户-用户不存在")
    void updateUser_userNotFound() {
        // Given: 不存在的用户
        Long userId = 999L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("新名字");

        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateUser(userId, request)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 分配角色测试 ====================

    @Test
    @DisplayName("分配角色成功")
    void assignRoles_success() {
        // Given: 存在的用户
        Long userId = 1L;
        AssignRolesRequest request = new AssignRolesRequest();
        request.setRoleIds(Arrays.asList(1L, 2L));

        SysUser user = createUser(1L, "test001", "测试用户");
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userRoleMapper.delete(any())).thenReturn(1); // 删除旧角色
        when(userRoleMapper.insert(any())).thenReturn(1); // 插入新角色

        // When: 分配角色
        userService.assignRoles(userId, request);

        // Then: 验证先删除旧角色，再插入新角色
        verify(userRoleMapper).delete(any());
        verify(userRoleMapper, times(2)).insert(any());
    }

    // ==================== 重置密码测试 ====================

    @Test
    @DisplayName("重置密码成功")
    void resetPassword_success() {
        // Given: 存在的用户
        Long userId = 1L;
        SysUser user = createUser(1L, "test001", "测试用户");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn("$2a$10$newEncodedPassword");
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 重置密码
        String newPassword = userService.resetPassword(userId);

        // Then: 返回明文密码，后端BCrypt存储
        assertEquals(DEFAULT_PASSWORD, newPassword);
        verify(passwordEncoder).encode(DEFAULT_PASSWORD);
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getPassword().equals("$2a$10$newEncodedPassword");
        }));
    }

    // ==================== 禁用用户测试 ====================

    @Test
    @DisplayName("禁用用户成功")
    void disableUser_success() {
        // Given: 普通用户
        Long userId = 2L;
        SysUser user = createUser(2L, "test001", "测试用户");
        user.setCurrentToken("existing-token");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 禁用用户
        userService.disableUser(userId);

        // Then: 验证设置了禁用状态和清空Token
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getStatus() == 0
                    && updated.getDisableReason().equals("ADMIN_MANUAL")
                    && updated.getCurrentToken() == null; // 清空Token，强制登出
        }));
    }

    @Test
    @DisplayName("禁用用户-超级管理员不可禁用")
    void disableUser_cannotDisableSuperAdmin() {
        // Given: 超级管理员
        Long userId = 1L;
        SysUser user = createUser(1L, "admin", "超级管理员");

        when(userMapper.selectById(userId)).thenReturn(user);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.disableUser(userId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("超级管理员"));

        // 验证未执行更新
        verify(userMapper, never()).updateById(any());
    }

    // ==================== 启用用户测试 ====================

    @Test
    @DisplayName("启用用户成功")
    void enableUser_success() {
        // Given: 被禁用的用户
        Long userId = 2L;
        SysUser user = createUser(2L, "test001", "测试用户");
        user.setStatus(0);
        user.setDisableReason("AUTO_INACTIVE");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 启用用户
        userService.enableUser(userId);

        // Then: 验证启用了用户并清空禁用原因
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getStatus() == 1
                    && updated.getDisableReason() == null;
        }));
    }

    // ==================== 删除用户测试 ====================

    @Test
    @DisplayName("删除用户成功")
    void deleteUser_success() {
        // Given: 普通用户
        Long userId = 2L;
        SysUser user = createUser(2L, "test001", "测试用户");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.deleteById(userId)).thenReturn(1);

        // When: 删除用户
        userService.deleteUser(userId);

        // Then: 验证删除了用户（逻辑删除）
        verify(userMapper).deleteById(userId);
    }

    @Test
    @DisplayName("删除用户-超级管理员不可删除")
    void deleteUser_cannotDeleteSuperAdmin() {
        // Given: 超级管理员
        Long userId = 1L;
        SysUser user = createUser(1L, "admin", "超级管理员");

        when(userMapper.selectById(userId)).thenReturn(user);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("超级管理员"));

        // 验证未执行删除
        verify(userMapper, never()).deleteById(any(Long.class));
    }
}