package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 料号编辑请求
 */
@Data
public class MaterialEditRequest {
    /** 料号ID */
    private Long id;
    /** 料号名称 */
    private String name;
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