package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 项目新增请求
 */
@Data
public class ProjectAddRequest {
    /** 项目编码 */
    private String code;
    /** 项目名称 */
    private String name;
    /** 项目描述 */
    private String description;
    /** 备注 */
    private String remark;
}