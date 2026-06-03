package com.matt.mes.system.service;

import com.matt.mes.system.dto.AssignPermissionsRequest;
import com.matt.mes.system.dto.PageResult;
import com.matt.mes.system.dto.RoleCreateRequest;
import com.matt.mes.system.dto.RoleQueryRequest;
import com.matt.mes.system.dto.RoleResponse;
import com.matt.mes.system.dto.RoleSimpleResponse;
import com.matt.mes.system.dto.RoleUpdateRequest;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 角色ID
     */
    Long createRole(RoleCreateRequest request);

    /**
     * 编辑角色
     *
     * @param id 角色ID
     * @param request 编辑请求
     */
    void updateRole(Long id, RoleUpdateRequest request);

    /**
     * 分配权限
     *
     * @param id 角色ID
     * @param request 分配权限请求
     */
    void assignPermissions(Long id, AssignPermissionsRequest request);

    /**
     * 禁用角色
     *
     * @param id 角色ID
     */
    void disableRole(Long id);

    /**
     * 启用角色
     *
     * @param id 角色ID
     */
    void enableRole(Long id);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 查询角色列表
     *
     * @param request 查询请求
     * @return 角色列表
     */
    PageResult<RoleResponse> getRoleList(RoleQueryRequest request);

    /**
     * 查询角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleResponse getRoleDetail(Long id);

    /**
     * 获取所有启用的角色（用于下拉选项）
     *
     * @return 启用状态的角色列表
     */
    List<RoleSimpleResponse> getAllEnabledRoles();
}