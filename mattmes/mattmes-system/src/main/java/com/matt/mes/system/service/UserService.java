package com.matt.mes.system.service;

import com.matt.mes.system.dto.*;

/**
 * 用户管理服务接口
 */
public interface UserService {

    /**
     * 创建用户
     */
    Long createUser(UserCreateRequest request);

    /**
     * 编辑用户
     */
    void updateUser(Long id, UserUpdateRequest request);

    /**
     * 分配角色
     */
    void assignRoles(Long id, AssignRolesRequest request);

    /**
     * 重置密码
     */
    String resetPassword(Long id);

    /**
     * 禁用用户
     */
    void disableUser(Long id);

    /**
     * 启用用户
     */
    void enableUser(Long id);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 查询用户列表
     */
    PageResult<UserResponse> getUserList(UserQueryRequest request);

    /**
     * 查询用户详情
     */
    UserResponse getUserDetail(Long id);
}
