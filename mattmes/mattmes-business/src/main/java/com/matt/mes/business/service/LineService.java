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
}
