package com.matt.mes.system.dto;

import lombok.Data;

/**
 * 编辑权限请求 DTO
 */
@Data
public class PermissionUpdateRequest {

    /** 权限名称 */
    private String permissionName;

    /** 排序号 */
    private Integer sortOrder;

    /** 权限描述 */
    private String description;
}