package com.matt.mes.controller;

import com.matt.mes.common.result.Result;
import com.matt.mes.system.dto.AssignPermissionsRequest;
import com.matt.mes.system.dto.PageResult;
import com.matt.mes.system.dto.RoleCreateRequest;
import com.matt.mes.system.dto.RoleQueryRequest;
import com.matt.mes.system.dto.RoleResponse;
import com.matt.mes.system.dto.RoleSimpleResponse;
import com.matt.mes.system.dto.RoleUpdateRequest;
import com.matt.mes.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 Controller
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    public Result<Long> createRole(@RequestBody RoleCreateRequest request) {
        Long roleId = roleService.createRole(request);
        return Result.success("创建角色成功", roleId);
    }

    /**
     * 编辑角色
     */
    @PutMapping("/{id}")
    public Result<?> updateRole(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        roleService.updateRole(id, request);
        return Result.success("编辑角色成功");
    }

    /**
     * 分配权限
     */
    @PutMapping("/{id}/permissions")
    public Result<?> assignPermissions(@PathVariable Long id, @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request);
        return Result.success("分配权限成功");
    }

    /**
     * 禁用角色
     */
    @PutMapping("/{id}/disable")
    public Result<?> disableRole(@PathVariable Long id) {
        roleService.disableRole(id);
        return Result.success("禁用角色成功");
    }

    /**
     * 启用角色
     */
    @PutMapping("/{id}/enable")
    public Result<?> enableRole(@PathVariable Long id) {
        roleService.enableRole(id);
        return Result.success("启用角色成功");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除角色成功");
    }

    /**
     * 查询角色列表
     */
    @GetMapping
    public Result<PageResult<RoleResponse>> getRoleList(RoleQueryRequest request) {
        PageResult<RoleResponse> result = roleService.getRoleList(request);
        return Result.success(result);
    }

    /**
     * 查询角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleResponse> getRoleDetail(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleDetail(id);
        return Result.success(response);
    }

    /**
     * 获取所有启用的角色（用于下拉选项）
     */
    @GetMapping("/all")
    public Result<List<RoleSimpleResponse>> getAllEnabledRoles() {
        List<RoleSimpleResponse> roles = roleService.getAllEnabledRoles();
        return Result.success(roles);
    }
}