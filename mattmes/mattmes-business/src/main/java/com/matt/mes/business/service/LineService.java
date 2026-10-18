package com.matt.mes.business.service;

import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LinePageResult;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.dto.LineResponse;

/**
 * 线体服务接口
 */
public interface LineService {

    /**
     * 分页查询线体
     */
    LinePageResult<LineResponse> page(LineQueryRequest request);

    /**
     * 新增线体
     *
     * @param request 新增请求
     * @return 新记录ID
     */
    Long add(LineAddRequest request);

    /**
     * 切换线体启用状态
     *
     * @param id     线体ID
     * @param enable 目标状态：1=启用，0=禁用
     * @return 该线体ID
     */
    Long updateStatus(Long id, Integer enable);
}
