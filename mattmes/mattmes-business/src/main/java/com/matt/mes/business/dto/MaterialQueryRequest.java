package com.matt.mes.business.dto;

import lombok.Data;

/**
 * 料号查询请求
 */
@Data
public class MaterialQueryRequest {
    /** 料号编码 */
    private String code;
    /** 料号名称 */
    private String name;
    /** 所属项目ID */
    private Long projectId;
    /** 启用状态 */
    private Integer enable;
    /** 页码 */
    private Integer pageNum = 1;
    /** 每页数量 */
    private Integer pageSize = 10;
}