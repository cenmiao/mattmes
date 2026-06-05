package com.matt.mes.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.business.dto.ProcessAddRequest;
import com.matt.mes.business.dto.ProcessEditRequest;
import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;
import com.matt.mes.business.entity.MesProcess;
import com.matt.mes.business.enums.ProcessType;
import com.matt.mes.business.mapper.ProcessMapper;
import com.matt.mes.business.service.ProcessService;
import com.matt.mes.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

    @Override
    public Long add(ProcessAddRequest request) {
        // 1. 校验必填项
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(400, "工序编码不能为空");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(400, "工序名称不能为空");
        }
        if (request.getProcessType() == null || request.getProcessType().trim().isEmpty()) {
            throw new BusinessException(400, "工序类型不能为空");
        }

        // 2. 校验字段长度
        if (request.getCode().length() > 50) {
            throw new BusinessException(400, "工序编码长度不能超过50个字符");
        }
        if (request.getName().length() > 100) {
            throw new BusinessException(400, "工序名称长度不能超过100个字符");
        }

        // 3. 校验编码格式（仅允许字母、数字、下划线、中划线）
        Pattern codePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
        if (!codePattern.matcher(request.getCode()).matches()) {
            throw new BusinessException(400, "工序编码只能包含字母、数字、下划线和中划线");
        }

        // 4. 校验编码唯一性
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MesProcess::getCode, request.getCode());
        Long count = processMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(400, "工序编码已存在");
        }

        // 5. 创建工序实体
        MesProcess process = new MesProcess();
        process.setCode(request.getCode());
        process.setName(request.getName());
        process.setProcessType(request.getProcessType());
        process.setDescription(request.getDescription());
        process.setEnable(request.getEnable() != null ? request.getEnable() : 1);
        process.setRemark(request.getRemark());

        // 6. 保存工序
        processMapper.insert(process);

        return process.getId();
    }

    @Override
    public Long edit(ProcessEditRequest request) {
        // 1. 校验工序是否存在
        MesProcess process = processMapper.selectById(request.getId());
        if (process == null) {
            throw new BusinessException(400, "工序不存在");
        }

        // 2. 校验必填项
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(400, "工序名称不能为空");
        }
        if (request.getProcessType() == null || request.getProcessType().trim().isEmpty()) {
            throw new BusinessException(400, "工序类型不能为空");
        }

        // 3. 校验字段长度
        if (request.getName().length() > 100) {
            throw new BusinessException(400, "工序名称长度不能超过100个字符");
        }

        // 4. 校验工序类型有效性
        boolean validProcessType = false;
        for (ProcessType type : ProcessType.values()) {
            if (type.getCode().equals(request.getProcessType())) {
                validProcessType = true;
                break;
            }
        }
        if (!validProcessType) {
            throw new BusinessException(400, "工序类型无效");
        }

        // 5. 更新工序信息（编码不可修改）
        process.setName(request.getName());
        process.setProcessType(request.getProcessType());
        process.setDescription(request.getDescription());
        process.setEnable(request.getEnable());
        process.setRemark(request.getRemark());

        // 6. 保存更新
        processMapper.updateById(process);

        return process.getId();
    }

    @Override
    public Long updateStatus(Long id, Integer enable) {
        // 1. 校验工序是否存在
        MesProcess process = processMapper.selectById(id);
        if (process == null) {
            throw new BusinessException(400, "工序不存在");
        }

        // 2. 校验状态值有效性
        if (enable == null || (enable != 0 && enable != 1)) {
            throw new BusinessException(400, "启用状态值无效");
        }

        // 3. 更新状态
        process.setEnable(enable);
        processMapper.updateById(process);

        return process.getId();
    }

    @Override
    public Long delete(Long id) {
        // 1. 校验工序是否存在
        MesProcess process = processMapper.selectById(id);
        if (process == null) {
            throw new BusinessException(400, "工序不存在");
        }

        // 2. 执行逻辑删除(MyBatis-Plus会自动设置deleted字段为1)
        int rows = processMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(500, "删除工序失败");
        }

        return id;
    }

    @Override
    public List<Long> batchDelete(List<Long> ids) {
        // 1. 校验参数
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "工序ID列表不能为空");
        }

        // 2. 校验所有工序是否存在
        for (Long id : ids) {
            MesProcess process = processMapper.selectById(id);
            if (process == null) {
                throw new BusinessException(400, "工序不存在");
            }
        }

        // 3. 执行批量逻辑删除
        int rows = processMapper.deleteBatchIds(ids);
        if (rows == 0) {
            throw new BusinessException(500, "批量删除工序失败");
        }

        return ids;
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
