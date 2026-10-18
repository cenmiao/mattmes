package com.matt.mes.business.service;

import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LinePageResult;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.dto.LineResponse;

import java.util.List;

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
     * 单条逻辑删除线体
     *
     * @param id 线体ID
     * @return 被删除的ID
     */
    Long delete(Long id);

    /**
     * 批量逻辑删除线体
     *
     * @param ids 线体ID列表
     * @return 被删除的ID列表
     */
    List<Long> batchDelete(List<Long> ids);
}
