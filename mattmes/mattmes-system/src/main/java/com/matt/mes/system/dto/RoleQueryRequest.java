package com.matt.mes.system.dto;

import lombok.Data;

/**
 * 查询角色请求 DTO
 */
@Data
public class RoleQueryRequest {

    /** 角色名称（模糊搜索） */
    private String roleName;

    /** 状态：1=启用，0=禁用 */
    private Integer status;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页数量 */
    private Integer pageSize = 10;
}