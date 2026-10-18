package com.matt.mes.business.service;

import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LineEditRequest;
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
     * 编辑线体（编码不可修改）
     *
     * @param request 编辑请求
     * @return 被编辑的记录ID
     */
    Long edit(LineEditRequest request);
}
