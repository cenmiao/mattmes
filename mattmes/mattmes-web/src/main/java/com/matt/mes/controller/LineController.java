package com.matt.mes.controller;

import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LineEditRequest;
import com.matt.mes.business.dto.LinePageResult;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.dto.LineResponse;
import com.matt.mes.business.service.LineService;
import com.matt.mes.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 线体管理Controller
 */
@RestController
@RequestMapping("/api/line")
@RequiredArgsConstructor
public class LineController {

    private final LineService lineService;

    /**
     * 分页查询线体
     */
    @PostMapping("/list")
    public Result<LinePageResult<LineResponse>> list(@RequestBody LineQueryRequest request) {
        LinePageResult<LineResponse> result = lineService.page(request);
        return Result.success(result);
    }

    /**
     * 新增线体
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody LineAddRequest request) {
        Long id = lineService.add(request);
        return Result.success("新增线体成功", id);
    }

    /**
     * 编辑线体（编码不可修改）
     */
    @PutMapping("/edit")
    public Result<Long> edit(@RequestBody LineEditRequest request) {
        Long id = lineService.edit(request);
        return Result.success("编辑线体成功", id);
    }

    /**
     * 切换线体启用状态
     */
    @PutMapping("/status/{id}")
    public Result<Long> updateStatus(@PathVariable Long id, @RequestParam Integer enable) {
        Long lineId = lineService.updateStatus(id, enable);
        return Result.success("状态更新成功", lineId);
    }

    /**
     * 单条删除线体（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Long> delete(@PathVariable Long id) {
        Long lineId = lineService.delete(id);
        return Result.success("删除线体成功", lineId);
    }

    /**
     * 批量删除线体（逻辑删除）
     */
    @DeleteMapping("/batchDelete")
    public Result<List<Long>> batchDelete(@RequestBody List<Long> ids) {
        List<Long> deletedIds = lineService.batchDelete(ids);
        return Result.success("批量删除线体成功", deletedIds);
    }

    /**
     * 导出线体数据为 CSV
     *
     * <p>查询条件与列表一致，一次性导出全部匹配记录；响应体以 UTF-8 BOM 开头。</p>
     */
    @GetMapping("/export")
    public void export(LineQueryRequest request, HttpServletResponse response) {
        lineService.export(request, response);
    }
}
