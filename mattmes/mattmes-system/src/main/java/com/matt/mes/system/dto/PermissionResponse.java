package com.matt.mes.system.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限响应 DTO
 */
@Data
@Builder
public class PermissionResponse {

    /** 权限ID */
    private Long id;

    /** 权限名称 */
    private String permissionName;

    /** 权限编码 */
    private String permissionCode;

    /** 权限类型：1=模块，2=按钮 */
    private Integer permissionType;

    /** 父权限ID */
    private Long parentId;

    /** 排序号 */
    private Integer sortOrder;

    /** 权限描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;
}