package com.matt.mes.controller;

import com.matt.mes.common.result.Result;
import com.matt.mes.system.dto.*;
import com.matt.mes.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理Controller
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public Result<Long> createUser(@RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        return Result.success("创建用户成功", userId);
    }

    /**
     * 编辑用户
     */
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return Result.success("编辑用户成功");
    }

    /**
     * 分配角色
     */
    @PutMapping("/{id}/roles")
    public Result<?> assignRoles(@PathVariable Long id, @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request);
        return Result.success("分配角色成功");
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        return Result.success("重置密码成功", newPassword);
    }

    /**
     * 禁用用户
     */
    @PutMapping("/{id}/disable")
    public Result<?> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success("禁用用户成功");
    }

    /**
     * 启用用户
     */
    @PutMapping("/{id}/enable")
    public Result<?> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return Result.success("启用用户成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除用户成功");
    }

    /**
     * 查询用户列表
     */
    @GetMapping
    public Result<PageResult<UserResponse>> getUserList(UserQueryRequest request) {
        PageResult<UserResponse> result = userService.getUserList(request);
        return Result.success(result);
    }

    /**
     * 查询用户详情
     */
    @GetMapping("/{id}")
    public Result<UserResponse> getUserDetail(@PathVariable Long id) {
        UserResponse response = userService.getUserDetail(id);
        return Result.success(response);
    }
}
