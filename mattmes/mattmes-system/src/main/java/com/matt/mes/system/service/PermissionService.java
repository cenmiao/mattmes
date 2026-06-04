package com.matt.mes.system.service;

import com.matt.mes.system.dto.PermissionCreateRequest;
import com.matt.mes.system.dto.PermissionResponse;
import com.matt.mes.system.dto.PermissionTreeResponse;
import com.matt.mes.system.dto.PermissionUpdateRequest;

import java.util.List;

/**
 * 权限管理服务接口
 */
public interface PermissionService {

    /**
     * 创建权限
     */
    Long createPermission(PermissionCreateRequest request);

    /**
     * 编辑权限
     */
    void updatePermission(Long id, PermissionUpdateRequest request);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 查询权限列表
     */
    List<PermissionResponse> getPermissionList();

    /**
     * 查询权限树形结构
     */
    List<PermissionTreeResponse> getPermissionTree();
}