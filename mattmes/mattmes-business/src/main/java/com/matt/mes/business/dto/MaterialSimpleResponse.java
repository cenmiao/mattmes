package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 料号简单响应对象（下拉列表用）
 */
@Data
public class MaterialSimpleResponse {
    /** 料号ID */
    private Long id;
    /** 料号编码 */
    private String code;
    /** 料号名称 */
    private String name;
}