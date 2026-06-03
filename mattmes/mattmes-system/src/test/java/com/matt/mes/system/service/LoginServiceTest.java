package com.matt.mes.system.service;

import com.matt.mes.system.dto.LoginRequest;
import com.matt.mes.system.dto.LoginResponse;
import com.matt.mes.system.entity.SysUser;
import com.matt.mes.system.mapper.SysUserMapper;
import com.matt.mes.system.mapper.SysUserRoleMapper;
import com.matt.mes.system.mapper.SysRoleMapper;
import com.matt.mes.system.mapper.SysRolePermissionMapper;
import com.matt.mes.system.mapper.SysPermissionMapper;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.system.service.impl.LoginServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysRolePermissionMapper rolePermissionMapper;

    @Mock
    private SysPermissionMapper permissionMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginService = new LoginServiceImpl(userMapper, userRoleMapper, roleMapper, rolePermissionMapper, permissionMapper, passwordEncoder);
    }

    @Test
    @DisplayName("正常登录成功")
    void login_success() {
        // Given: 存在的用户，正确的密码
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword"); // BCrypt加密后的密码
        user.setName("管理员");
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now().minusDays(10));

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Admin@123", user.getPassword())).thenReturn(true);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 调用登录
        LoginResponse response = loginService.login(request, "192.168.1.100");

        // Then: 返回Token和用户信息
        assertNotNull(response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("admin", response.getUserNo());
        assertEquals("管理员", response.getName());
        assertFalse(response.getNeedChangePassword());

        // 验证更新了登录信息
        verify(userMapper).updateById(any());
    }

    @Test
    @DisplayName("工号不存在")
    void login_userNotFound() {
        // Given: 工号不存在
        LoginRequest request = new LoginRequest();
        request.setUserNo("notexist");
        request.setPassword("password");

        when(userMapper.selectOne(any())).thenReturn(null);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loginService.login(request, "192.168.1.100")
        );

        assertEquals(400, exception.getCode());
        assertEquals("工号或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("密码错误（含剩余次数）")
    void login_wrongPassword() {
        // Given: 存在的用户，错误的密码，当前错误次数为0
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("wrongpassword");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setStatus(1);
        user.setLoginErrorCount(0);

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);
        when(userMapper.updateById(any())).thenReturn(1);

        // When & Then: 抛出异常并更新错误次数
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loginService.login(request, "192.168.1.100")
        );

        assertEquals(400, exception.getCode());
        // 验证更新了错误次数
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getLoginErrorCount() == 1;
        }));
    }

    @Test
    @DisplayName("错误5次锁定")
    void login_lockedAfter5Errors() {
        // Given: 存在的用户，错误的密码，当前错误次数为4
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("wrongpassword");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setStatus(1);
        user.setLoginErrorCount(4);

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);
        when(userMapper.updateById(any())).thenReturn(1);

        // When & Then: 抛出异常并设置锁定时间
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loginService.login(request, "192.168.1.100")
        );

        assertEquals(400, exception.getCode());
        // 验证设置了锁定时间（10分钟后）
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getLoginErrorCount() == 5 && updated.getLockTime() != null;
        }));
    }

    @Test
    @DisplayName("锁定期间登录（含剩余时间）")
    void login_lockedUser() {
        // Given: 存在的用户，已被锁定
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setStatus(1);
        user.setLoginErrorCount(5);
        user.setLockTime(LocalDateTime.now().plusMinutes(5)); // 还有5分钟锁定

        when(userMapper.selectOne(any())).thenReturn(user);

        // When & Then: 抛出锁定异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loginService.login(request, "192.168.1.100")
        );

        assertEquals(400, exception.getCode());
        // 应该返回剩余锁定时间
        assertTrue(exception.getMessage().contains("锁定"));
    }

    @Test
    @DisplayName("并发登录冲突")
    void login_concurrentLogin() {
        // Given: 存在的用户，已有有效Token
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");
        request.setForceLogin(false);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setName("管理员");
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now().minusDays(10));
        user.setCurrentToken("existing-valid-token");
        user.setTokenExpireTime(LocalDateTime.now().plusHours(1)); // Token未过期

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Admin@123", user.getPassword())).thenReturn(true);

        // When & Then: 抛出并发登录冲突异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loginService.login(request, "192.168.1.100")
        );

        assertEquals(409, exception.getCode());
        assertTrue(exception.getMessage().contains("其他设备登录"));
    }

    @Test
    @DisplayName("强制登录成功")
    void login_forceLoginSuccess() {
        // Given: 存在的用户，已有有效Token，强制登录
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");
        request.setForceLogin(true);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setName("管理员");
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now().minusDays(10));
        user.setCurrentToken("existing-valid-token");
        user.setTokenExpireTime(LocalDateTime.now().plusHours(1));

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Admin@123", user.getPassword())).thenReturn(true);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 强制登录
        LoginResponse response = loginService.login(request, "192.168.1.100");

        // Then: 成功登录，返回新Token
        assertNotNull(response.getToken());
        assertNotEquals("existing-valid-token", response.getToken());

        // 验证更新了Token
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getCurrentToken() != null
                    && !updated.getCurrentToken().equals("existing-valid-token");
        }));
    }

    @Test
    @DisplayName("被禁用账号拒绝登录")
    void login_disabledUser() {
        // Given: 存在的用户，已被禁用
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setName("管理员");
        user.setStatus(0); // 禁用状态
        user.setDisableReason("ADMIN_MANUAL");

        when(userMapper.selectOne(any())).thenReturn(user);

        // When & Then: 抛出禁用异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loginService.login(request, "192.168.1.100")
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已禁用"));
    }

    @Test
    @DisplayName("首次登录检测")
    void login_firstTimeLogin() {
        // Given: 存在的用户，passwordUpdateTime为null
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setName("管理员");
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setPasswordUpdateTime(null); // 首次登录

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Admin@123", user.getPassword())).thenReturn(true);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 登录
        LoginResponse response = loginService.login(request, "192.168.1.100");

        // Then: 返回需要修改密码标志
        assertTrue(response.getNeedChangePassword());
    }

    @Test
    @DisplayName("密码过期检测")
    void login_passwordExpired() {
        // Given: 存在的用户，passwordUpdateTime超过30天
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setName("管理员");
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now().minusDays(31)); // 31天前修改

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Admin@123", user.getPassword())).thenReturn(true);
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 登录
        LoginResponse response = loginService.login(request, "192.168.1.100");

        // Then: 返回需要修改密码标志
        assertTrue(response.getNeedChangePassword());
    }

    @Test
    @DisplayName("30天未登录禁用")
    void login_autoDisableAfter30Days() {
        // Given: 存在的用户，lastLoginTime超过30天
        LoginRequest request = new LoginRequest();
        request.setUserNo("admin");
        request.setPassword("Admin@123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");
        user.setName("管理员");
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now().minusDays(10));
        user.setLastLoginTime(LocalDateTime.now().minusDays(31)); // 31天前登录

        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        // When & Then: 抛出禁用异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loginService.login(request, "192.168.1.100")
        );

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已禁用"));

        // 验证更新了用户状态为禁用
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getStatus() == 0
                    && "AUTO_INACTIVE".equals(updated.getDisableReason());
        }));
    }
}
