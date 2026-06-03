package com.matt.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.system.dto.*;
import com.matt.mes.system.entity.SysRole;
import com.matt.mes.system.entity.SysUser;
import com.matt.mes.system.entity.SysUserRole;
import com.matt.mes.system.mapper.SysRoleMapper;
import com.matt.mes.system.mapper.SysUserMapper;
import com.matt.mes.system.mapper.SysUserRoleMapper;
import com.matt.mes.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Reset@123";
    private static final String SUPER_ADMIN_USER_NO = "admin";

    @Override
    @Transactional
    public Long createUser(UserCreateRequest request) {
        // 1. 验证工号唯一性（包括已删除的用户，因为唯一键约束仍生效）
        long existingCount = userMapper.countByUserNoIncludingDeleted(request.getUserNo());
        if (existingCount > 0) {
            throw new BusinessException(400, "工号已存在");
        }

        // 2. 加密密码
        String password = request.getPassword() != null ? request.getPassword() : DEFAULT_PASSWORD;
        String encodedPassword = passwordEncoder.encode(password);

        // 3. 创建用户
        SysUser user = new SysUser();
        user.setUserNo(request.getUserNo());
        user.setName(request.getName());
        user.setPassword(encodedPassword);
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setPasswordUpdateTime(null); // 首次登录强制修改
        user.setLoginErrorCount(0);

        userMapper.insert(user);

        // 4. 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 只允许修改姓名、手机号、邮箱（工号不可修改）
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());

        userMapper.updateById(user);

        // 如果传入了 roleIds，则同时更新角色
        if (request.getRoleIds() != null) {
            // 删除旧的角色关联
            userRoleMapper.delete(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, id)
            );

            // 插入新的角色关联
            if (!request.getRoleIds().isEmpty()) {
                for (Long roleId : request.getRoleIds()) {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(id);
                    userRole.setRoleId(roleId);
                    userRoleMapper.insert(userRole);
                }
            }
        }
    }

    @Override
    @Transactional
    public void assignRoles(Long id, AssignRolesRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 删除旧的角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id)
        );

        // 插入新的角色关联
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(id);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional
    public String resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 重置为默认密码
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        user.setPassword(encodedPassword);
        user.setPasswordUpdateTime(null); // 重置为null，首次登录强制修改

        userMapper.updateById(user);

        // 返回明文密码
        return DEFAULT_PASSWORD;
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 检查是否为超级管理员
        if (SUPER_ADMIN_USER_NO.equals(user.getUserNo())) {
            throw new BusinessException(400, "超级管理员不可禁用");
        }

        user.setStatus(0);
        user.setDisableReason("ADMIN_MANUAL");
        user.setCurrentToken(null); // 清空Token，强制登出

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void enableUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        user.setStatus(1);
        user.setDisableReason(null);

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 检查是否为超级管理员
        if (SUPER_ADMIN_USER_NO.equals(user.getUserNo())) {
            throw new BusinessException(400, "超级管理员不可删除");
        }

        // MyBatis-Plus逻辑删除（@TableLogic已配置）
        userMapper.deleteById(id);
    }

    @Override
    public PageResult<UserResponse> getUserList(UserQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 工号模糊搜索
        if (request.getUserNo() != null && !request.getUserNo().isEmpty()) {
            queryWrapper.like(SysUser::getUserNo, request.getUserNo());
        }

        // 姓名模糊搜索
        if (request.getName() != null && !request.getName().isEmpty()) {
            queryWrapper.like(SysUser::getName, request.getName());
        }

        // 状态精确筛选
        if (request.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, request.getStatus());
        }

        // 角色筛选
        if (request.getRoleId() != null) {
            List<SysUserRole> userRoles = userRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getRoleId, request.getRoleId())
            );
            List<Long> userIds = userRoles.stream()
                    .map(SysUserRole::getUserId)
                    .collect(Collectors.toList());
            if (userIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L, request.getPageNum(), request.getPageSize());
            }
            queryWrapper.in(SysUser::getId, userIds);
        }

        // 2. 分页查询
        Page<SysUser> page = new Page<>(request.getPageNum(), request.getPageSize());
        userMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应DTO
        List<UserResponse> userResponses = page.getRecords().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return new PageResult<>(userResponses, page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private UserResponse convertToUserResponse(SysUser user) {
        // 查询用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
        );

        List<UserResponse.RoleInfo> roles = Collections.emptyList();
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            List<SysRole> roleList = roleMapper.selectBatchIds(roleIds);
            roles = roleList.stream()
                    .map(role -> UserResponse.RoleInfo.builder()
                            .id(role.getId())
                            .roleName(role.getRoleName())
                            .roleCode(role.getRoleCode())
                            .build())
                    .collect(Collectors.toList());
        }

        return UserResponse.builder()
                .id(user.getId())
                .userNo(user.getUserNo())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .status(user.getStatus())
                .disableReason(user.getDisableReason())
                .lastLoginTime(user.getLastLoginTime())
                .roles(roles)
                .build();
    }

    @Override
    public UserResponse getUserDetail(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        return convertToUserResponse(user);
    }
}
