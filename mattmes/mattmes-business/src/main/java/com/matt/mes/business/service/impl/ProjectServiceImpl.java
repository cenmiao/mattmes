package com.matt.mes.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.business.dto.*;
import com.matt.mes.business.entity.MesProject;
import com.matt.mes.business.mapper.ProjectMapper;
import com.matt.mes.business.service.ProjectService;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 项目服务实现
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    /** 项目编码格式正则:仅允许字母、数字、下划线、中划线 */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    /** 项目编码最大长度 */
    private static final int CODE_MAX_LENGTH = 50;

    /** 项目名称最大长度 */
    private static final int NAME_MAX_LENGTH = 100;

    @Override
    public ProjectPageResult<ProjectResponse> page(ProjectQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<MesProject> queryWrapper = new LambdaQueryWrapper<>();

        // 编码模糊查询
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            queryWrapper.like(MesProject::getCode, request.getCode());
        }

        // 名称模糊查询
        if (request.getName() != null && !request.getName().isEmpty()) {
            queryWrapper.like(MesProject::getName, request.getName());
        }

        // 启用状态精确查询
        if (request.getEnable() != null) {
            queryWrapper.eq(MesProject::getEnable, request.getEnable());
        }

        // 按ID降序排序
        queryWrapper.orderByDesc(MesProject::getId);

        // 2. 执行分页查询
        Page<MesProject> page = new Page<>(request.getPageNum(), request.getPageSize());
        projectMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应DTO
        List<ProjectResponse> projectResponses = page.getRecords().stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());

        return new ProjectPageResult<>(projectResponses, page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public Long add(ProjectAddRequest request) {
        // 1. 校验必填项
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(400, "编码不能为空");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(400, "名称不能为空");
        }

        // 2. 校验字段长度
        if (request.getCode().length() > CODE_MAX_LENGTH) {
            throw new BusinessException(400, "编码长度不能超过" + CODE_MAX_LENGTH + "个字符");
        }
        if (request.getName().length() > NAME_MAX_LENGTH) {
            throw new BusinessException(400, "名称长度不能超过" + NAME_MAX_LENGTH + "个字符");
        }

        // 3. 校验编码格式（仅允许字母、数字、下划线、中划线）
        if (!CODE_PATTERN.matcher(request.getCode()).matches()) {
            throw new BusinessException(400, "编码只能包含字母、数字、下划线和中划线");
        }

        // 4. 校验编码唯一性（包括已删除的记录，避免唯一键冲突）
        Long count = projectMapper.countByCodeIncludeDeleted(request.getCode());
        if (count > 0) {
            throw new BusinessException(400, "项目编码已存在");
        }

        // 5. 创建项目实体
        MesProject project = new MesProject();
        project.setCode(request.getCode());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setEnable(1); // 默认启用
        project.setRemark(request.getRemark());

        // 6. 保存项目
        projectMapper.insert(project);

        return project.getId();
    }

    @Override
    public Long edit(ProjectEditRequest request) {
        // 1. 校验项目是否存在
        MesProject project = projectMapper.selectById(request.getId());
        if (project == null) {
            throw new BusinessException(400, "项目不存在");
        }

        // 2. 校验必填项
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(400, "名称不能为空");
        }

        // 3. 校验字段长度
        if (request.getName().length() > NAME_MAX_LENGTH) {
            throw new BusinessException(400, "名称长度不能超过" + NAME_MAX_LENGTH + "个字符");
        }

        // 4. 更新项目信息（编码不可修改）
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setRemark(request.getRemark());

        // 5. 保存更新
        projectMapper.updateById(project);

        return project.getId();
    }

    @Override
    public void delete(Long id) {
        // 1. 校验项目是否存在
        MesProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(400, "项目不存在");
        }

        // 2. TODO: 校验项目下是否有料号（待料号模块完成后实现）
        // 当前暂时不校验料号关联

        // 3. 执行逻辑删除(MyBatis-Plus会自动设置deleted字段为1)
        projectMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        // 1. 校验参数
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "项目ID列表不能为空");
        }

        // 2. TODO: 校验料号关联并统计成功/跳过数量（待料号模块完成后实现）
        // 当前暂时直接删除所有项目
        for (Long id : ids) {
            MesProject project = projectMapper.selectById(id);
            if (project == null) {
                throw new BusinessException(400, "项目不存在");
            }
            projectMapper.deleteById(id);
        }
    }

    @Override
    public void updateStatus(Long id, Integer enable) {
        // 1. 校验项目是否存在
        MesProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(400, "项目不存在");
        }

        // 2. 校验状态值有效性
        if (enable == null || (enable != 0 && enable != 1)) {
            throw new BusinessException(400, "启用状态值无效");
        }

        // 3. 更新状态
        project.setEnable(enable);
        projectMapper.updateById(project);
    }

    @Override
    public void export(ProjectQueryRequest request, HttpServletResponse response) {
        try {
            // 1. 构建查询条件（复用 page 的查询逻辑）
            LambdaQueryWrapper<MesProject> queryWrapper = buildQueryWrapper(request);
            queryWrapper.orderByDesc(MesProject::getId);

            // 2. 查询所有符合条件的数据（不分页）
            List<MesProject> projectList = projectMapper.selectList(queryWrapper);

            // 3. 设置响应头
            String fileName = generateExportFileName();
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 4. 写入CSV内容
            StringBuilder csv = new StringBuilder();
            // 写入表头
            csv.append("ID,项目编码,项目名称,项目描述,启用状态,备注,创建人,创建时间\n");

            // 写入数据行
            for (MesProject project : projectList) {
                csv.append(project.getId()).append(",");
                csv.append(escapeCsvField(project.getCode())).append(",");
                csv.append(escapeCsvField(project.getName())).append(",");
                csv.append(escapeCsvField(project.getDescription())).append(",");
                csv.append(project.getEnable()).append(",");
                csv.append(escapeCsvField(project.getRemark())).append(",");
                csv.append(escapeCsvField(project.getCreatedBy())).append(",");
                csv.append(project.getCreateTime() != null ? project.getCreateTime().toString() : "");
                csv.append("\n");
            }

            response.getWriter().write(csv.toString());
        } catch (IOException e) {
            throw new BusinessException(500, "导出文件失败");
        }
    }

    @Override
    public List<ProjectSimpleResponse> listEnabled() {
        // 1. 构建查询条件:只查询启用状态的项目
        LambdaQueryWrapper<MesProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MesProject::getEnable, 1);
        queryWrapper.orderByAsc(MesProject::getCode);

        // 2. 执行查询
        List<MesProject> projects = projectMapper.selectList(queryWrapper);

        // 3. 转换为响应DTO
        return projects.stream()
                .map(this::convertToProjectSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应DTO
     */
    private ProjectResponse convertToProjectResponse(MesProject project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setCode(project.getCode());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setEnable(project.getEnable());
        response.setRemark(project.getRemark());
        response.setCreatedBy(project.getCreatedBy());
        response.setCreateTime(project.getCreateTime());
        response.setUpdatedBy(project.getUpdatedBy());
        response.setUpdateTime(project.getUpdateTime());
        return response;
    }

    /**
     * 转换为简单响应DTO
     */
    private ProjectSimpleResponse convertToProjectSimpleResponse(MesProject project) {
        ProjectSimpleResponse response = new ProjectSimpleResponse();
        response.setId(project.getId());
        response.setCode(project.getCode());
        response.setName(project.getName());
        response.setEnable(project.getEnable());
        return response;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<MesProject> buildQueryWrapper(ProjectQueryRequest request) {
        LambdaQueryWrapper<MesProject> queryWrapper = new LambdaQueryWrapper<>();

        // 编码模糊查询
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            queryWrapper.like(MesProject::getCode, request.getCode());
        }

        // 名称模糊查询
        if (request.getName() != null && !request.getName().isEmpty()) {
            queryWrapper.like(MesProject::getName, request.getName());
        }

        // 启用状态精确查询
        if (request.getEnable() != null) {
            queryWrapper.eq(MesProject::getEnable, request.getEnable());
        }

        return queryWrapper;
    }

    /**
     * 生成导出文件名
     */
    private String generateExportFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "项目列表_" + timestamp + ".csv";
    }

    /**
     * 转义CSV字段（处理逗号、引号、换行符）
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // 如果字段包含逗号、引号或换行符，需要用引号包裹并转义内部引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}