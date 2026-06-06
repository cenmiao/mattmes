package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 项目查询请求
 */
@Data
public class ProjectQueryRequest {
    /** 项目编码 */
    private String code;
    /** 项目名称 */
    private String name;
    /** 启用状态 */
    private Integer enable;
    /** 页码 */
    private Integer pageNum = 1;
    /** 每页数量 */
    private Integer pageSize = 10;
}