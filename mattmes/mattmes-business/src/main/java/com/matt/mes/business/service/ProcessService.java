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

    /**
     * 更新工序启用状态
     * @param id 工序ID
     * @param enable 启用状态：1=启用，0=禁用
     * @return 工序ID
     */
    Long updateStatus(Long id, Integer enable);
}
