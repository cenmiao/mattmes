package com.matt.mes.system.service;

import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.system.dto.ChangePasswordRequest;
import com.matt.mes.system.entity.SysUser;
import com.matt.mes.system.mapper.SysUserMapper;
import com.matt.mes.system.service.impl.ChangePasswordServiceImpl;
import com.matt.mes.common.token.TokenStorage;
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

class ChangePasswordServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private TokenStorage tokenStorage;

    private ChangePasswordService changePasswordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        changePasswordService = new ChangePasswordServiceImpl(userMapper, passwordEncoder, tokenStorage);
    }

    @Test
    @DisplayName("用户主动修改密码成功")
    void changePassword_success() {
        // Given: 有效的修改请求
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldPass@123");
        request.setNewPassword("NewPass@123");
        request.setConfirmPassword("NewPass@123");
        request.setIsFirstLogin(false);

        SysUser user = new SysUser();
        user.setId(userId);
        user.setUserNo("admin");
        user.setPassword("$2a$10$oldEncodedPassword");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("OldPass@123", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("NewPass@123", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("$2a$10$newEncodedPassword");
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 执行修改
        String result = changePasswordService.changePassword(request, userId);

        // Then: 返回成功消息
        assertEquals("密码修改成功，请重新登录", result);

        // 验证更新了用户信息
        verify(userMapper).updateById(argThat(u -> {
            SysUser updated = (SysUser) u;
            return updated.getPassword().equals("$2a$10$newEncodedPassword")
                    && updated.getPasswordUpdateTime() != null
                    && updated.getLoginErrorCount() == 0;
        }));

        // 验证使Token失效
        verify(tokenStorage).invalidateToken(userId);
    }

    @Test
    @DisplayName("首次登录修改密码成功（无需旧密码）")
    void changePassword_firstLoginSuccess() {
        // Given: 首次登录修改请求
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("NewPass@123");
        request.setConfirmPassword("NewPass@123");
        request.setIsFirstLogin(true);

        SysUser user = new SysUser();
        user.setId(userId);
        user.setUserNo("admin");
        user.setPassword("$2a$10$oldEncodedPassword");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("$2a$10$newEncodedPassword");
        when(userMapper.updateById(any())).thenReturn(1);

        // When: 执行修改
        String result = changePasswordService.changePassword(request, userId);

        // Then: 返回成功消息
        assertEquals("密码修改成功，请重新登录", result);

        // 验证没有验证旧密码
        verify(passwordEncoder, never()).matches(any(), any());

        // 验证更新了用户信息
        verify(userMapper).updateById(any());
        verify(tokenStorage).invalidateToken(userId);
    }

    @Test
    @DisplayName("旧密码错误")
    void changePassword_wrongOldPassword() {
        // Given: 旧密码错误的请求
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("WrongPass@123");
        request.setNewPassword("NewPass@123");
        request.setConfirmPassword("NewPass@123");
        request.setIsFirstLogin(false);

        SysUser user = new SysUser();
        user.setId(userId);
        user.setUserNo("admin");
        user.setPassword("$2a$10$oldEncodedPassword");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("WrongPass@123", user.getPassword())).thenReturn(false);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> changePasswordService.changePassword(request, userId)
        );

        assertEquals(400, exception.getCode());
        assertEquals("旧密码错误", exception.getMessage());

        // 验证没有更新用户
        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("新密码与旧密码相同")
    void changePassword_sameAsOldPassword() {
        // Given: 新旧密码相同的请求
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("SamePass@123");
        request.setNewPassword("SamePass@123");
        request.setConfirmPassword("SamePass@123");
        request.setIsFirstLogin(false);

        SysUser user = new SysUser();
        user.setId(userId);
        user.setUserNo("admin");
        user.setPassword("$2a$10$encodedPassword");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("SamePass@123", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("SamePass@123", user.getPassword())).thenReturn(true);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> changePasswordService.changePassword(request, userId)
        );

        assertEquals(400, exception.getCode());
        assertEquals("新密码不能与旧密码相同", exception.getMessage());
    }

    @Test
    @DisplayName("两次密码不一致")
    void changePassword_passwordMismatch() {
        // Given: 新密码和确认密码不一致
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldPass@123");
        request.setNewPassword("NewPass@123");
        request.setConfirmPassword("DifferentPass@123");
        request.setIsFirstLogin(false);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> changePasswordService.changePassword(request, userId)
        );

        assertEquals(400, exception.getCode());
        assertEquals("两次密码不一致", exception.getMessage());
    }

    @Test
    @DisplayName("密码强度不足")
    void changePassword_weakPassword() {
        // Given: 密码强度不足的请求
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldPass@123");
        request.setNewPassword("weak");
        request.setConfirmPassword("weak");
        request.setIsFirstLogin(false);

        // When & Then: 抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> changePasswordService.changePassword(request, userId)
        );

        assertEquals(400, exception.getCode());
        // 应该返回具体的强度不足原因
        assertTrue(exception.getMessage().contains("密码长度至少8位"));
    }
}
