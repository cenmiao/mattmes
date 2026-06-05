package com.matt.mes.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;
import com.matt.mes.business.entity.MesProcess;
import com.matt.mes.business.mapper.ProcessMapper;
import com.matt.mes.business.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工序服务实现
 */
@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final ProcessMapper processMapper;

    @Override
    public ProcessPageResult<ProcessResponse> queryList(ProcessQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();

        // 编码模糊查询
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            queryWrapper.like(MesProcess::getCode, request.getCode());
        }

        // 名称模糊查询
        if (request.getName() != null && !request.getName().isEmpty()) {
            queryWrapper.like(MesProcess::getName, request.getName());
        }

        // 工序类型精确查询
        if (request.getProcessType() != null && !request.getProcessType().isEmpty()) {
            queryWrapper.eq(MesProcess::getProcessType, request.getProcessType());
        }

        // 启用状态精确查询
        if (request.getEnable() != null) {
            queryWrapper.eq(MesProcess::getEnable, request.getEnable());
        }

        // 按ID降序排序
        queryWrapper.orderByDesc(MesProcess::getId);

        // 2. 执行分页查询
        Page<MesProcess> page = new Page<>(request.getPageNum(), request.getPageSize());
        processMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应DTO
        List<ProcessResponse> processResponses = page.getRecords().stream()
                .map(this::convertToProcessResponse)
                .collect(Collectors.toList());

        return new ProcessPageResult<>(processResponses, page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    /**
     * 转换为响应DTO
     */
    private ProcessResponse convertToProcessResponse(MesProcess process) {
        return ProcessResponse.builder()
                .id(process.getId())
                .code(process.getCode())
                .name(process.getName())
                .processType(process.getProcessType())
                .description(process.getDescription())
                .enable(process.getEnable())
                .remark(process.getRemark())
                .createdBy(process.getCreatedBy())
                .createTime(process.getCreateTime())
                .updatedBy(process.getUpdatedBy())
                .updateTime(process.getUpdateTime())
                .build();
    }
}
