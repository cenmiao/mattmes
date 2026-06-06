package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 项目编辑请求
 */
@Data
public class ProjectEditRequest {
    /** 项目ID */
    private Long id;
    /** 项目名称 */
    private String name;
    /** 项目描述 */
    private String description;
    /** 备注 */
    private String remark;
}