package com.matt.mes.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.business.dto.LinePageResult;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.dto.LineResponse;
import com.matt.mes.business.entity.MesLine;
import com.matt.mes.business.mapper.LineMapper;
import com.matt.mes.business.service.LineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 线体服务实现
 */
@Service
@RequiredArgsConstructor
public class LineServiceImpl implements LineService {

    private final LineMapper lineMapper;

    @Override
    public LinePageResult<LineResponse> page(LineQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<MesLine> queryWrapper = new LambdaQueryWrapper<>();

        // 编码模糊查询
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            queryWrapper.like(MesLine::getLineCode, request.getCode());
        }

        // 名称模糊查询
        if (request.getName() != null && !request.getName().isEmpty()) {
            queryWrapper.like(MesLine::getLineName, request.getName());
        }

        // 启用状态精确查询
        if (request.getEnable() != null) {
            queryWrapper.eq(MesLine::getEnable, request.getEnable());
        }

        // 按 ID 降序排序
        queryWrapper.orderByDesc(MesLine::getId);

        // 2. 执行分页查询
        Page<MesLine> page = new Page<>(request.getPageNum(), request.getPageSize());
        lineMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应DTO
        List<LineResponse> lineResponses = page.getRecords().stream()
                .map(this::convertToLineResponse)
                .collect(Collectors.toList());

        return new LinePageResult<>(lineResponses, page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    /**
     * 转换为响应DTO
     */
    private LineResponse convertToLineResponse(MesLine line) {
        LineResponse response = new LineResponse();
        response.setId(line.getId());
        response.setCode(line.getLineCode());
        response.setName(line.getLineName());
        response.setDescription(line.getDescription());
        response.setEnable(line.getEnable());
        response.setCreatedBy(line.getCreatedBy());
        response.setCreateTime(line.getCreateTime());
        response.setUpdatedBy(line.getUpdatedBy());
        response.setUpdateTime(line.getUpdateTime());
        return response;
    }
}
