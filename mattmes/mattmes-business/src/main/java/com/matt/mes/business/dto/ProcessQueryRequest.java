package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 工序查询请求DTO
 */
@Data
public class ProcessQueryRequest {

    /** 工序编码(模糊搜索) */
    private String code;

    /** 工序名称(模糊搜索) */
    private String name;

    /** 工序类型(精确筛选) */
    private String processType;

    /** 启用状态(精确筛选) */
    private Integer enable;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
