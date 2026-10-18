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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /** UTF-8 BOM：让 Excel 以 UTF-8 解析导出文件，避免中文乱码 */
    private static final String UTF8_BOM = "\uFEFF";

    private final LineMapper lineMapper;

    @Override
    public LinePageResult<LineResponse> page(LineQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<MesLine> queryWrapper = buildQueryWrapper(request);

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
     * 构建线体查询条件（列表与导出共用，保证两者所见数据一致）
     */
    private LambdaQueryWrapper<MesLine> buildQueryWrapper(LineQueryRequest request) {
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

        return queryWrapper;
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

    @Override
    public void export(LineQueryRequest request, HttpServletResponse response) {
        // 1. 查询全部符合条件的数据（不分页，导出的是当前筛选下的全量记录）
        List<MesLine> lineList = lineMapper.selectList(buildQueryWrapper(request));

        // 2. 写入响应头
        try {
            String fileName = generateExportFileName();
            response.setContentType("text/csv;charset=UTF-8");
            // URLEncoder 以表单编码处理空格（+），在 Content-Disposition 中须还原为 %20
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            response.setCharacterEncoding("UTF-8");

            // 3. 写入 BOM + 表头 + 数据行
            //    BOM 必须写在最前面，否则 Excel 以本地编码解析，中文单元格会乱码
            StringBuilder csv = new StringBuilder();
            csv.append(UTF8_BOM);
            csv.append("ID,线体编码,线体名称,描述,启用状态,创建人,创建时间\r\n");

            for (MesLine line : lineList) {
                csv.append(line.getId()).append(',');
                csv.append(escapeCsvField(line.getLineCode())).append(',');
                csv.append(escapeCsvField(line.getLineName())).append(',');
                csv.append(escapeCsvField(line.getDescription())).append(',');
                // 状态列导出原始值 1/0，不转换为中文，便于离线过滤与统计
                csv.append(line.getEnable()).append(',');
                csv.append(escapeCsvField(line.getCreatedBy())).append(',');
                csv.append(line.getCreateTime() != null ? line.getCreateTime().toString() : "");
                csv.append("\r\n");
            }

            // 4. 按 UTF-8 字节写出（显式指定字符集，避免容器默认编码干扰 BOM 与中文）
            byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
            response.setContentLength(bytes.length);
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new BusinessException(500, "导出文件失败");
        }
    }

    /**
     * 生成导出文件名：线体数据_yyyyMMdd_HHmmss.csv
     */
    private String generateExportFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "线体数据_" + timestamp + ".csv";
    }

    /**
     * 转义 CSV 字段：含逗号、双引号、换行或回车时整体加引号包裹，内部双引号成对转义
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
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
