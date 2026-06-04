package com.matt.mes.controller;

import com.matt.mes.common.result.Result;
import com.matt.mes.system.dto.PermissionCreateRequest;
import com.matt.mes.system.dto.PermissionResponse;
import com.matt.mes.system.dto.PermissionTreeResponse;
import com.matt.mes.system.dto.PermissionUpdateRequest;
import com.matt.mes.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理 Controller
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 创建权限
     */
    @PostMapping
    public Result<Long> createPermission(@RequestBody PermissionCreateRequest request) {
        Long permissionId = permissionService.createPermission(request);
        return Result.success("创建权限成功", permissionId);
    }

    /**
     * 编辑权限
     */
    @PutMapping("/{id}")
    public Result<?> updatePermission(@PathVariable Long id, @RequestBody PermissionUpdateRequest request) {
        permissionService.updatePermission(id, request);
        return Result.success("编辑权限成功");
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public Result<?> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success("删除权限成功");
    }

    /**
     * 查询权限列表
     */
    @GetMapping
    public Result<List<PermissionResponse>> getPermissionList() {
        List<PermissionResponse> list = permissionService.getPermissionList();
        return Result.success(list);
    }

    /**
     * 查询权限树形结构
     */
    @GetMapping("/tree")
    public Result<List<PermissionTreeResponse>> getPermissionTree() {
        List<PermissionTreeResponse> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }
}