package com.matt.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.system.dto.PermissionCreateRequest;
import com.matt.mes.system.dto.PermissionResponse;
import com.matt.mes.system.dto.PermissionTreeResponse;
import com.matt.mes.system.dto.PermissionUpdateRequest;
import com.matt.mes.system.entity.SysPermission;
import com.matt.mes.system.entity.SysRolePermission;
import com.matt.mes.system.mapper.SysPermissionMapper;
import com.matt.mes.system.mapper.SysRolePermissionMapper;
import com.matt.mes.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    @Override
    public Long createPermission(PermissionCreateRequest request) {
        // 1. 检查编码唯一性
        SysPermission existingPermission = permissionMapper.selectOne(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermissionCode, request.getPermissionCode())
        );
        if (existingPermission != null) {
            throw new BusinessException(400, "权限编码已存在");
        }

        // 2. 创建权限
        SysPermission permission = new SysPermission();
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionCode(request.getPermissionCode());
        permission.setParentId(request.getParentId());
        permission.setSortOrder(request.getSortOrder());
        permission.setDescription(request.getDescription());

        // 3. 根据parentId判断权限类型
        if (request.getParentId() == null) {
            permission.setPermissionType(1); // 模块级
        } else {
            permission.setPermissionType(2); // 按钮级
        }

        permissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    public void updatePermission(Long id, PermissionUpdateRequest request) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(400, "权限不存在");
        }

        // 只允许修改名称、描述、排序号
        // permissionCode、permissionType、parentId 不可修改
        permission.setPermissionName(request.getPermissionName());
        permission.setSortOrder(request.getSortOrder());
        permission.setDescription(request.getDescription());
        permissionMapper.updateById(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(400, "权限不存在");
        }

        // 如果是模块级权限（permissionType=1），需要级联删除子权限
        if (permission.getPermissionType() == 1) {
            // 查询所有子权限
            List<SysPermission> childPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getParentId, id)
            );

            // 删除子权限的角色关联
            for (SysPermission child : childPermissions) {
                rolePermissionMapper.delete(
                    new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getPermissionId, child.getId())
                );
            }

            // 删除子权限
            permissionMapper.delete(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getParentId, id)
            );
        }

        // 删除权限的角色关联
        rolePermissionMapper.delete(
            new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id)
        );

        // 删除权限本身
        permissionMapper.deleteById(id);
    }

    @Override
    public List<PermissionResponse> getPermissionList() {
        List<SysPermission> permissions = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSortOrder)
        );

        return permissions.stream()
            .map(this::convertToPermissionResponse)
            .collect(Collectors.toList());
    }

    private PermissionResponse convertToPermissionResponse(SysPermission permission) {
        return PermissionResponse.builder()
            .id(permission.getId())
            .permissionName(permission.getPermissionName())
            .permissionCode(permission.getPermissionCode())
            .permissionType(permission.getPermissionType())
            .parentId(permission.getParentId())
            .sortOrder(permission.getSortOrder())
            .description(permission.getDescription())
            .createTime(permission.getCreateTime())
            .build();
    }

    @Override
    public List<PermissionTreeResponse> getPermissionTree() {
        // 查询所有模块级权限（permissionType=1）
        List<SysPermission> modulePermissions = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermissionType, 1)
                .orderByAsc(SysPermission::getSortOrder)
        );

        // 为每个模块查询其子权限（按钮级）
        return modulePermissions.stream()
            .map(module -> {
                // 查询该模块下的所有按钮级权限
                List<SysPermission> buttonPermissions = permissionMapper.selectList(
                    new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getParentId, module.getId())
                        .orderByAsc(SysPermission::getSortOrder)
                );

                List<PermissionTreeResponse> children = buttonPermissions.stream()
                    .map(button -> PermissionTreeResponse.builder()
                        .id(button.getId())
                        .permissionName(button.getPermissionName())
                        .permissionCode(button.getPermissionCode())
                        .permissionType(button.getPermissionType())
                        .children(Collections.emptyList())
                        .build())
                    .collect(Collectors.toList());

                return PermissionTreeResponse.builder()
                    .id(module.getId())
                    .permissionName(module.getPermissionName())
                    .permissionCode(module.getPermissionCode())
                    .permissionType(module.getPermissionType())
                    .children(children)
                    .build();
            })
            .collect(Collectors.toList());
    }
}