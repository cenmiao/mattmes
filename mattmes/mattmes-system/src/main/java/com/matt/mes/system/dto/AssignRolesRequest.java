package com.matt.mes.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 分配角色请求DTO
 */
@Data
public class AssignRolesRequest {

    /** 角色ID列表 */
    private List<Long> roleIds;
}
