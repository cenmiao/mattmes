package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 料号新增请求
 */
@Data
public class MaterialAddRequest {
    /** 料号编码 */
    private String code;
    /** 料号名称 */
    private String name;
    /** 所属项目ID */
    private Long projectId;
    /** 颜色 */
    private String color;
    /** 尺码 */
    private String size;
    /** 通用规格1 */
    private String spec1;
    /** 通用规格2 */
    private String spec2;
    /** 通用规格3 */
    private String spec3;
    /** 描述 */
    private String description;
    /** 备注 */
    private String remark;
}