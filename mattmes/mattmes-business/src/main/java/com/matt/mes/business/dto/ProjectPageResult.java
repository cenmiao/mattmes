package com.matt.mes.business.dto;

import lombok.Data;
import java.util.List;

/**
 * 项目分页结果
 */
@Data
public class ProjectPageResult<T> {
    /** 数据列表 */
    private List<T> list;
    /** 总数 */
    private Long total;
    /** 当前页码 */
    private Integer pageNum;
    /** 每页数量 */
    private Integer pageSize;

    public ProjectPageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}