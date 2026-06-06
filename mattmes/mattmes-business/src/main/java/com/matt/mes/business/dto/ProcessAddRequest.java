package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 工序新增请求DTO
 */
@Data
public class ProcessAddRequest {

    /** 工序编码（唯一） */
    private String code;

    /** 工序名称 */
    private String name;

    /** 工序类型(INSPECTION/ASSEMBLY/PACKAGING/OTHER) */
    private String processType;

    /** 工序描述 */
    private String description;

    /** 启用状态：1=启用，0=禁用，新增时默认1 */
    private Integer enable;

    /** 备注 */
    private String remark;
}