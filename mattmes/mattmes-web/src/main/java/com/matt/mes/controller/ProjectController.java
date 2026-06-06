package com.matt.mes.controller;

import com.matt.mes.business.dto.*;
import com.matt.mes.business.service.ProjectService;
import com.matt.mes.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 项目管理Controller
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 分页查询项目
     */
    @PostMapping("/list")
    public Result<ProjectPageResult<ProjectResponse>> list(@RequestBody ProjectQueryRequest request) {
        ProjectPageResult<ProjectResponse> result = projectService.page(request);
        return Result.success(result);
    }

    /**
     * 新增项目
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody ProjectAddRequest request) {
        Long id = projectService.add(request);
        return Result.success("新增项目成功", id);
    }

    /**
     * 编辑项目
     */
    @PutMapping("/edit")
    public Result<Long> edit(@RequestBody ProjectEditRequest request) {
        Long id = projectService.edit(request);
        return Result.success("编辑项目成功", id);
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除项目
     */
    @DeleteMapping("/batchDelete")
    public Result<Void> batchDelete(@RequestBody java.util.List<Long> ids) {
        projectService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 更新项目启用状态
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer enable) {
        projectService.updateStatus(id, enable);
        return Result.success();
    }

    /**
     * 导出项目数据
     */
    @GetMapping("/export")
    public void export(ProjectQueryRequest request, HttpServletResponse response) {
        projectService.export(request, response);
    }
}