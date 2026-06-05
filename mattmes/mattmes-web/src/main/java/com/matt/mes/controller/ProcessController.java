package com.matt.mes.controller;

import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;
import com.matt.mes.business.service.ProcessService;
import com.matt.mes.common.result.Result;
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
}