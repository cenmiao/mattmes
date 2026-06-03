package com.matt.mes.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 分配权限请求 DTO
 */
@Data
public class AssignPermissionsRequest {

    /** 权限ID列表 */
    private List<Long> permissionIds;
}