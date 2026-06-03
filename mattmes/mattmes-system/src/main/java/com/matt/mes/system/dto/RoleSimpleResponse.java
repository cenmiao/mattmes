package com.matt.mes.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色简化响应DTO（用于下拉选项）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSimpleResponse {

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;
}
