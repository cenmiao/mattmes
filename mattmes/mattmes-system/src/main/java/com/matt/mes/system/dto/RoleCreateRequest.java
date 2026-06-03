package com.matt.mes.system.dto;

import lombok.Data;

/**
 * 创建角色请求 DTO
 */
@Data
public class RoleCreateRequest {

    /** 角色名称 */
    private String roleName;

    /** 角色编码（唯一） */
    private String roleCode;

    /** 角色描述 */
    private String description;
}