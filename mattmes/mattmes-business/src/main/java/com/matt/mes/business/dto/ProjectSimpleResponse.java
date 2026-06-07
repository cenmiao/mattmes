package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 项目简单响应(用于下拉框等场景)
 */
@Data
public class ProjectSimpleResponse {
    /** 项目ID */
    private Long id;
    /** 项目编码 */
    private String code;
    /** 项目名称 */
    private String name;
    /** 启用状态 */
    private Integer enable;
}
