package com.matt.mes.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 状态：1=启用，0=禁用 */
    private Integer status;

    /** 用户数量 */
    private Integer userCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 权限列表 */
    private List<PermissionInfo> permissions;

    /**
     * 权限信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionInfo {
        private Long id;
        private String permissionName;
        private String permissionCode;
    }
}