package com.matt.mes.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总数 */
    private Long total;

    /** 页码 */
    private Integer pageNum;

    /** 每页大小 */
    private Integer pageSize;
}
