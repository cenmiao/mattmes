package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 线体新增请求DTO
 */
@Data
public class LineAddRequest {

    /** 线体编码（必填，全局唯一，创建后不可修改） */
    private String code;

    /** 线体名称（必填） */
    private String name;

    /** 描述 */
    private String description;

    /** 启用状态：1=启用，0=禁用，新增时为空默认1 */
    private Integer enable;
}
