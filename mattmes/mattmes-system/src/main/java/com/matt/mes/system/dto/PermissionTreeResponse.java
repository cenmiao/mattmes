package com.matt.mes.system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 权限树响应 DTO
 */
@Data
@Builder
public class PermissionTreeResponse {

    /** 权限ID */
    private Long id;

    /** 权限名称 */
    private String permissionName;

    /** 权限编码 */
    private String permissionCode;

    /** 权限类型：1=模块，2=按钮 */
    private Integer permissionType;

    /** 子权限列表（仅模块级权限有children） */
    private List<PermissionTreeResponse> children;
}