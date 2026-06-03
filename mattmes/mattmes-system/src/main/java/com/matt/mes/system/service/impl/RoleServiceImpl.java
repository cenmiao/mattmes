package com.matt.mes.system.service.impl;

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
import com.matt.mes.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;

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
        role.setStatus(1); // 默认启用

        roleMapper.insert(role);
        return role.getId();
    }

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

    @Override
    public void disableRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(400, "角色不存在");
        }
        role.setStatus(0);
        roleMapper.updateById(role);
    }

    @Override
    public void enableRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(400, "角色不存在");
        }
        role.setStatus(1);
        roleMapper.updateById(role);
    }

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

    @Override
    public PageResult<RoleResponse> getRoleList(RoleQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();

        // 角色名称模糊搜索
        if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
            queryWrapper.like(SysRole::getRoleName, request.getRoleName());
        }

        // 状态精确筛选
        if (request.getStatus() != null) {
            queryWrapper.eq(SysRole::getStatus, request.getStatus());
        }

        // 分页查询
        Page<SysRole> page = new Page<>(request.getPageNum(), request.getPageSize());
        roleMapper.selectPage(page, queryWrapper);

        // 转换为响应DTO
        List<RoleResponse> roleResponses = page.getRecords().stream()
            .map(this::convertToRoleResponse)
            .collect(Collectors.toList());

        return new PageResult<>(roleResponses, page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public RoleResponse getRoleDetail(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(400, "角色不存在");
        }
        return convertToRoleResponse(role);
    }

    @Override
    public List<RoleSimpleResponse> getAllEnabledRoles() {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getStatus, 1); // 只查询启用状态
        queryWrapper.orderByAsc(SysRole::getId); // 按ID升序

        List<SysRole> roles = roleMapper.selectList(queryWrapper);
        return roles.stream()
            .map(role -> RoleSimpleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .build())
            .collect(Collectors.toList());
    }

    private RoleResponse convertToRoleResponse(SysRole role) {
        // 查询用户数量
        long userCount = userRoleMapper.selectCount(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, role.getId())
        );

        return RoleResponse.builder()
            .id(role.getId())
            .roleName(role.getRoleName())
            .roleCode(role.getRoleCode())
            .description(role.getDescription())
            .status(role.getStatus())
            .userCount((int) userCount)
            .createTime(role.getCreateTime())
            .permissions(Collections.emptyList())
            .build();
    }
}