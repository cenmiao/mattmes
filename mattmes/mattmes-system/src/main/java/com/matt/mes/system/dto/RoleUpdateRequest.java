package com.matt.mes.system.dto;

import lombok.Data;

/**
 * 编辑角色请求 DTO
 */
@Data
public class RoleUpdateRequest {

    /** 角色名称 */
    private String roleName;

    /** 角色描述 */
    private String description;
}