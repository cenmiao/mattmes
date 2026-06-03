package com.matt.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.common.token.TokenStorage;
import com.matt.mes.common.utils.JwtUtils;
import com.matt.mes.system.dto.ConcurrentLoginInfo;
import com.matt.mes.system.dto.LoginRequest;
import com.matt.mes.system.dto.LoginResponse;
import com.matt.mes.system.entity.SysPermission;
import com.matt.mes.system.entity.SysRole;
import com.matt.mes.system.entity.SysRolePermission;
import com.matt.mes.system.entity.SysUser;
import com.matt.mes.system.entity.SysUserRole;
import com.matt.mes.system.mapper.SysPermissionMapper;
import com.matt.mes.system.mapper.SysRoleMapper;
import com.matt.mes.system.mapper.SysRolePermissionMapper;
import com.matt.mes.system.mapper.SysUserMapper;
import com.matt.mes.system.mapper.SysUserRoleMapper;
import com.matt.mes.system.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final int MAX_ERROR_COUNT = 5;
    private static final int LOCK_MINUTES = 10;
    private static final int PASSWORD_EXPIRE_DAYS = 30;
    private static final int AUTO_INACTIVE_DAYS = 30;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String loginIp) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserNo, request.getUserNo())
        );

        if (user == null) {
            throw new BusinessException(400, "工号或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            String reason = user.getDisableReason() != null ? user.getDisableReason() : "未知原因";
            throw new BusinessException(400, "账号已禁用，原因：" + reason);
        }

        // 检查30天未登录自动禁用
        if (user.getLastLoginTime() != null
                && user.getLastLoginTime().plusDays(AUTO_INACTIVE_DAYS).isBefore(LocalDateTime.now())) {
            user.setStatus(0);
            user.setDisableReason("AUTO_INACTIVE");
            userMapper.updateById(user);
            throw new BusinessException(400, "账号已禁用，原因：AUTO_INACTIVE");
        }

        // 检查是否被锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            long remainingMinutes = java.time.Duration.between(
                    LocalDateTime.now(), user.getLockTime()
            ).toMillis() / 1000 / 60;
            throw new BusinessException(400, "账号已锁定，请" + remainingMinutes + "分钟后再试");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 密码错误，更新错误次数
            int errorCount = (user.getLoginErrorCount() == null ? 0 : user.getLoginErrorCount()) + 1;
            user.setLoginErrorCount(errorCount);

            // 达到5次则锁定10分钟
            if (errorCount >= MAX_ERROR_COUNT) {
                user.setLockTime(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }

            userMapper.updateById(user);
            throw new BusinessException(400, "工号或密码错误");
        }

        // 3. 检查并发登录（非强制登录时检测）
        if (!Boolean.TRUE.equals(request.getForceLogin())) {
            if (user.getCurrentToken() != null && user.getTokenExpireTime() != null
                    && user.getTokenExpireTime().isAfter(LocalDateTime.now())) {
                // 已有有效Token，返回并发登录冲突
                throw new BusinessException(409, "账号已在其他设备登录，是否强制登录？");
            }
        }

        // 3. 生成Token
        String token = JwtUtils.generateToken(user.getId(), user.getUserNo());

        // 4. 更新用户登录信息
        user.setLoginErrorCount(0);
        user.setLockTime(null);
        user.setLastLoginTime(LocalDateTime.now());
        user.setCurrentToken(token);
        user.setTokenExpireTime(JwtUtils.getExpirationTime());
        userMapper.updateById(user);

        // 5. 检查是否需要修改密码
        boolean needChangePassword = false;
        if (user.getPasswordUpdateTime() == null) {
            // 首次登录
            needChangePassword = true;
        } else if (user.getPasswordUpdateTime().plusDays(PASSWORD_EXPIRE_DAYS).isBefore(LocalDateTime.now())) {
            // 密码过期（30天）
            needChangePassword = true;
        }

        // 6. 查询用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
        );

        List<LoginResponse.RoleInfo> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        boolean isSuperAdmin = false;

        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());

            List<SysRole> roleList = roleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>()
                            .in(SysRole::getId, roleIds)
                            .eq(SysRole::getStatus, 1)
            );

            roles = roleList.stream()
                    .map(r -> LoginResponse.RoleInfo.builder()
                            .roleId(r.getId())
                            .roleCode(r.getRoleCode())
                            .roleName(r.getRoleName())
                            .build())
                    .collect(Collectors.toList());

            isSuperAdmin = roleList.stream()
                    .anyMatch(r -> "SUPER_ADMIN".equals(r.getRoleCode()));

            // 超管不需要查权限，前端会自动放行
            if (!isSuperAdmin) {
                List<Long> activeRoleIds = roleList.stream()
                        .map(SysRole::getId)
                        .collect(Collectors.toList());

                List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>()
                                .in(SysRolePermission::getRoleId, activeRoleIds)
                );

                if (!rolePermissions.isEmpty()) {
                    List<Long> permissionIds = rolePermissions.stream()
                            .map(SysRolePermission::getPermissionId)
                            .distinct()
                            .collect(Collectors.toList());

                    List<SysPermission> permissionList = permissionMapper.selectList(
                            new LambdaQueryWrapper<SysPermission>()
                                    .in(SysPermission::getId, permissionIds)
                                    .eq(SysPermission::getPermissionType, 2)
                    );

                    permissions = permissionList.stream()
                            .map(SysPermission::getPermissionCode)
                            .collect(Collectors.toList());
                }
            }
        }

        // 7. 构建响应
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .userNo(user.getUserNo())
                .name(user.getName())
                .needChangePassword(needChangePassword)
                .permissions(permissions)
                .roles(roles)
                .build();
    }

    @Override
    public ConcurrentLoginInfo checkConcurrentLogin(LoginRequest request) {
        return null;
    }
}
