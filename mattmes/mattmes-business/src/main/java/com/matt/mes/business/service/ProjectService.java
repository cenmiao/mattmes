package com.matt.mes.business.service;

import com.matt.mes.business.dto.ProjectAddRequest;
import com.matt.mes.business.dto.ProjectEditRequest;
import com.matt.mes.business.dto.ProjectPageResult;
import com.matt.mes.business.dto.ProjectQueryRequest;
import com.matt.mes.business.dto.ProjectResponse;
import com.matt.mes.business.dto.ProjectSimpleResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 分页查询项目
     */
    ProjectPageResult<ProjectResponse> page(ProjectQueryRequest request);

    /**
     * 新增项目
     */
    Long add(ProjectAddRequest request);

    /**
     * 编辑项目
     */
    Long edit(ProjectEditRequest request);

    /**
     * 删除项目
     */
    void delete(Long id);

    /**
     * 批量删除项目
     */
    void batchDelete(List<Long> ids);

    /**
     * 更新项目启用状态
     */
    void updateStatus(Long id, Integer enable);

    /**
     * 导出项目数据
     */
    void export(ProjectQueryRequest request, HttpServletResponse response);

    /**
     * 获取启用项目列表(用于下拉框)
     */
    List<ProjectSimpleResponse> listEnabled();
}