package com.matt.mes.controller;

import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LineEditRequest;
import com.matt.mes.business.dto.LinePageResult;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.dto.LineResponse;
import com.matt.mes.business.service.LineService;
import com.matt.mes.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
