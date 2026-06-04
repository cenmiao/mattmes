package com.matt.mes.system.dto;

import lombok.Data;

/**
 * 创建权限请求 DTO
 */
@Data
public class PermissionCreateRequest {

    /** 权限名称 */
    private String permissionName;

    /** 权限编码（唯一，如 user 或 user:add） */
    private String permissionCode;

    /** 父权限ID（null表示模块级权限） */
    private Long parentId;

    /** 排序号 */
    private Integer sortOrder;

    /** 权限描述 */
    private String description;
}