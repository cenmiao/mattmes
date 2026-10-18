package com.matt.mes.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LineEditRequest;
import com.matt.mes.business.dto.LinePageResult;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.dto.LineResponse;
import com.matt.mes.business.entity.MesLine;
import com.matt.mes.business.mapper.LineMapper;
import com.matt.mes.business.service.LineService;
import com.matt.mes.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 线体服务实现
 */
@Service
@RequiredArgsConstructor
public class LineServiceImpl implements LineService {

    /** 编码允许的字符：字母、数字、下划线、中划线 */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    /** 编码最大长度 */
    private static final int CODE_MAX_LENGTH = 50;

    /** 名称最大长度 */
    private static final int NAME_MAX_LENGTH = 100;

    /** 描述最大长度 */
    private static final int DESCRIPTION_MAX_LENGTH = 500;

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

    @Override
    public Long add(LineAddRequest request) {
        // 1. 编码必填（空或纯空白均不允许）
        String code = request.getCode() == null ? null : request.getCode().trim();
        if (code == null || code.isEmpty()) {
            throw new BusinessException(400, "线体编码不能为空");
        }

        // 2. 名称必填（空或纯空白均不允许）
        String name = request.getName() == null ? null : request.getName().trim();
        if (name == null || name.isEmpty()) {
            throw new BusinessException(400, "线体名称不能为空");
        }

        // 3. 长度校验
        if (code.length() > CODE_MAX_LENGTH) {
            throw new BusinessException(400, "线体编码长度不能超过50个字符");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(400, "线体名称长度不能超过100个字符");
        }

        // 4. 编码格式校验
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BusinessException(400, "线体编码只能包含字母、数字、下划线和中划线");
        }

        // 5. 描述长度校验
        if (request.getDescription() != null && request.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException(400, "描述长度不能超过500个字符");
        }

        // 6. 状态值校验
        Integer enable = request.getEnable() == null ? 1 : request.getEnable();
        if (enable != 0 && enable != 1) {
            throw new BusinessException(400, "启用状态值无效");
        }

        // 7. 编码唯一性校验（连同已删除记录一起统计，避免数据库唯一键冲突）
        Long count = lineMapper.countByCodeIncludeDeleted(code);
        if (count != null && count > 0) {
            throw new BusinessException(400, "线体编码已存在");
        }

        // 8. 保存
        MesLine line = new MesLine();
        line.setLineCode(code);
        line.setLineName(name);
        line.setDescription(request.getDescription());
        line.setEnable(enable);
        lineMapper.insert(line);

        return line.getId();
    }

    @Override
    public Long edit(LineEditRequest request) {
        // 1. 校验 ID 是否存在，不存在时返回业务错误而非系统异常
        if (request.getId() == null) {
            throw new BusinessException(400, "线体ID不能为空");
        }
        MesLine line = lineMapper.selectById(request.getId());
        if (line == null) {
            throw new BusinessException(400, "线体不存在");
        }

        // 2. 名称必填（空或纯空白均不允许），与新增规则一致
        String name = request.getName() == null ? null : request.getName().trim();
        if (name == null || name.isEmpty()) {
            throw new BusinessException(400, "线体名称不能为空");
        }

        // 3. 长度校验
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(400, "线体名称长度不能超过100个字符");
        }

        // 4. 描述长度校验
        if (request.getDescription() != null && request.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException(400, "描述长度不能超过500个字符");
        }

        // 5. 状态值校验（未传则保留原状态）
        Integer enable = request.getEnable() == null ? line.getEnable() : request.getEnable();
        if (enable != 0 && enable != 1) {
            throw new BusinessException(400, "启用状态值无效");
        }

        // 6. 只更新可变字段：编码 lineCode 不在编辑请求中，保持原值不变
        line.setLineName(name);
        line.setDescription(request.getDescription());
        line.setEnable(enable);
        lineMapper.updateById(line);

        return line.getId();
    }

    @Override
    public Long updateStatus(Long id, Integer enable) {
        // 1. 状态值校验：只接受 1（启用）与 0（禁用）
        if (enable == null || (enable != 0 && enable != 1)) {
            throw new BusinessException(400, "启用状态值无效");
        }

        // 2. 校验线体是否存在
        MesLine line = lineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException(400, "线体不存在");
        }

        // 3. 切换状态
        line.setEnable(enable);
        lineMapper.updateById(line);

        return line.getId();
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
