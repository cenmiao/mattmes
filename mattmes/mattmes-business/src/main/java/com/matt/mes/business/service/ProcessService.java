package com.matt.mes.business.service;

import com.matt.mes.business.dto.ProcessAddRequest;
import com.matt.mes.business.dto.ProcessEditRequest;
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

    /**
     * 新增工序
     */
    Long add(ProcessAddRequest request);

    /**
     * 编辑工序
     */
    Long edit(ProcessEditRequest request);
}
