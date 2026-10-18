package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 线体编辑请求DTO
 *
 * <p>不含编码字段：线体编码是业务唯一标识，编辑时不可修改。</p>
 */
@Data
public class LineEditRequest {

    /** 线体ID（必填） */
    private Long id;

    /** 线体名称（必填） */
    private String name;

    /** 描述 */
    private String description;

    /** 启用状态：1=启用，0=禁用 */
    private Integer enable;
}
