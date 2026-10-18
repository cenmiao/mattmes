package com.matt.mes.controller;

import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LinePageResult;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.dto.LineResponse;
import com.matt.mes.business.service.LineService;
import com.matt.mes.common.result.Result;
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
}
