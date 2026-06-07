package com.matt.mes.business.service;

import com.matt.mes.business.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 料号服务接口
 */
public interface MaterialService {

    /**
     * 分页查询料号
     */
    MaterialPageResult<MaterialResponse> page(MaterialQueryRequest request);

    /**
     * 获取料号详情
     */
    MaterialResponse getById(Long id);

    /**
     * 新增料号
     */
    Long add(MaterialAddRequest request);

    /**
     * 编辑料号
     */
    Long edit(MaterialEditRequest request);

    /**
     * 删除料号
     */
    void delete(Long id);

    /**
     * 批量删除料号
     */
    void batchDelete(List<Long> ids);

    /**
     * 更新料号启用状态
     */
    void updateStatus(Long id, Integer enable);

    /**
     * 导出料号数据
     */
    void export(MaterialQueryRequest request, HttpServletResponse response);

    /**
     * 按项目查询料号列表
     */
    List<MaterialSimpleResponse> listByProjectId(Long projectId);
}