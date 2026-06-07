package com.matt.mes.controller;

import com.matt.mes.business.dto.*;
import com.matt.mes.business.service.MaterialService;
import com.matt.mes.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 料号管理Controller
 */
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    /**
     * 分页查询料号
     */
    @PostMapping("/list")
    public Result<MaterialPageResult<MaterialResponse>> list(@RequestBody MaterialQueryRequest request) {
        MaterialPageResult<MaterialResponse> result = materialService.page(request);
        return Result.success(result);
    }

    /**
     * 获取料号详情
     */
    @GetMapping("/{id}")
    public Result<MaterialResponse> getById(@PathVariable Long id) {
        MaterialResponse result = materialService.getById(id);
        return Result.success(result);
    }

    /**
     * 新增料号
     */
    @PostMapping
    public Result<Long> add(@RequestBody MaterialAddRequest request) {
        Long id = materialService.add(request);
        return Result.success("新增料号成功", id);
    }

    /**
     * 编辑料号
     */
    @PutMapping("/{id}")
    public Result<Long> edit(@PathVariable Long id, @RequestBody MaterialEditRequest request) {
        request.setId(id);
        Long resultId = materialService.edit(request);
        return Result.success("编辑料号成功", resultId);
    }

    /**
     * 删除料号
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除料号
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        materialService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 更新料号启用状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer enable) {
        materialService.updateStatus(id, enable);
        return Result.success();
    }

    /**
     * 导出料号数据
     */
    @GetMapping("/export")
    public void export(MaterialQueryRequest request, HttpServletResponse response) {
        materialService.export(request, response);
    }

    /**
     * 按项目查询料号列表
     */
    @GetMapping("/list-by-project/{projectId}")
    public Result<List<MaterialSimpleResponse>> listByProjectId(@PathVariable Long projectId) {
        List<MaterialSimpleResponse> result = materialService.listByProjectId(projectId);
        return Result.success(result);
    }
}