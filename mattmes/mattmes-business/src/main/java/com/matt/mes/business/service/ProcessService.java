package com.matt.mes.business.service;

import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;

/**
 * 工序服务接口
 */
public interface ProcessService {

    /**
     * 查询工序列表
     */
    ProcessPageResult<ProcessResponse> queryList(ProcessQueryRequest request);
}
