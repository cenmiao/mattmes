package com.matt.mes.system.dto;

import lombok.Data;

/**
 * 查询用户请求DTO
 */
@Data
public class UserQueryRequest {

    /** 工号（模糊搜索） */
    private String userNo;

    /** 姓名（模糊搜索） */
    private String name;

    /** 状态筛选 */
    private Integer status;

    /** 角色ID筛选 */
    private Long roleId;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
