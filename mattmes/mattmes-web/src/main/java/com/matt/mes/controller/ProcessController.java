package com.matt.mes.controller;

import com.matt.mes.business.dto.ProcessAddRequest;
import com.matt.mes.business.dto.ProcessEditRequest;
import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;
import com.matt.mes.business.service.ProcessService;
import com.matt.mes.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 工序管理Controller
 */
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    /**
     * 查询工序列表
     */
    @PostMapping("/list")
    public Result<ProcessPageResult<ProcessResponse>> list(@RequestBody ProcessQueryRequest request) {
        ProcessPageResult<ProcessResponse> result = processService.queryList(request);
        return Result.success(result);
    }

    /**
     * 新增工序
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody ProcessAddRequest request) {
        Long id = processService.add(request);
        return Result.success("新增工序成功", id);
    }

    /**
     * 编辑工序
     */
    @PutMapping("/edit")
    public Result<Long> edit(@RequestBody ProcessEditRequest request) {
        Long id = processService.edit(request);
        return Result.success("编辑工序成功", id);
    }

    /**
     * 更新工序启用状态
     */
    @PutMapping("/status/{id}")
    public Result<Long> updateStatus(@PathVariable Long id, @RequestParam Integer enable) {
        Long processId = processService.updateStatus(id, enable);
        return Result.success("状态更新成功", processId);
    }

    /**
     * 导出工序数据
     */
    @GetMapping("/export")
    public void export(ProcessQueryRequest request, HttpServletResponse response) {
        processService.export(request, response);
    }
}