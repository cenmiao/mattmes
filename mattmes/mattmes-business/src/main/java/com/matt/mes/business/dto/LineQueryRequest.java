package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 线体查询请求
 */
@Data
public class LineQueryRequest {
    /** 线体编码（模糊查询） */
    private String code;
    /** 线体名称（模糊查询） */
    private String name;
    /** 启用状态（精确查询） */
    private Integer enable;
    /** 页码 */
    private Integer pageNum = 1;
    /** 每页数量 */
    private Integer pageSize = 10;
}
