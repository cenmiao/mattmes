package com.matt.mes.business.service;

import com.matt.mes.business.dto.ProcessAddRequest;
import com.matt.mes.business.dto.ProcessEditRequest;
import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

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

    /**
     * 删除工序
     * @param id 工序ID
     * @return 被删除的工序ID
     */
    Long delete(Long id);

    /**
     * 批量删除工序
     * @param ids 工序ID列表
     * @return 被删除的工序ID列表
     */
    List<Long> batchDelete(List<Long> ids);

    /**
     * 导出工序数据为CSV
     * @param request 查询条件
     * @param response HTTP响应
     */
    void export(ProcessQueryRequest request, HttpServletResponse response);
}