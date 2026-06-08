package com.matt.mes.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.business.dto.*;
import com.matt.mes.business.entity.MesMaterial;
import com.matt.mes.business.entity.MesProject;
import com.matt.mes.business.mapper.MaterialMapper;
import com.matt.mes.business.mapper.ProjectMapper;
import com.matt.mes.business.service.MaterialService;
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
 * 料号服务实现
 */
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialMapper materialMapper;
    private final ProjectMapper projectMapper;

    /** 料号编码格式正则:仅允许字母、数字、下划线、中划线 */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    /** 料号编码最大长度 */
    private static final int CODE_MAX_LENGTH = 50;

    /** 料号名称最大长度 */
    private static final int NAME_MAX_LENGTH = 100;

    @Override
    public MaterialPageResult<MaterialResponse> page(MaterialQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<MesMaterial> queryWrapper = new LambdaQueryWrapper<>();

        // 编码模糊查询
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            queryWrapper.like(MesMaterial::getMaterialCode, request.getCode());
        }

        // 名称模糊查询
        if (request.getName() != null && !request.getName().isEmpty()) {
            queryWrapper.like(MesMaterial::getMaterialName, request.getName());
        }

        // 项目ID精确查询
        if (request.getProjectId() != null) {
            queryWrapper.eq(MesMaterial::getProjectId, request.getProjectId());
        }

        // 启用状态精确查询
        if (request.getEnable() != null) {
            queryWrapper.eq(MesMaterial::getEnable, request.getEnable());
        }

        // 按ID降序排序
        queryWrapper.orderByDesc(MesMaterial::getId);

        // 2. 执行分页查询
        Page<MesMaterial> page = new Page<>(request.getPageNum(), request.getPageSize());
        materialMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应DTO
        List<MaterialResponse> materialResponses = page.getRecords().stream()
                .map(this::convertToMaterialResponse)
                .collect(Collectors.toList());

        return new MaterialPageResult<>(materialResponses, page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public MaterialResponse getById(Long id) {
        // 1. 校验料号是否存在
        MesMaterial material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(400, "料号不存在");
        }

        // 2. 转换为响应DTO
        return convertToMaterialResponse(material);
    }

    @Override
    public Long add(MaterialAddRequest request) {
        // 1. 校验必填项
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(400, "编码不能为空");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(400, "名称不能为空");
        }
        if (request.getProjectId() == null) {
            throw new BusinessException(400, "所属项目不能为空");
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
        Long count = materialMapper.countByCodeIncludeDeleted(request.getCode());
        if (count > 0) {
            throw new BusinessException(400, "料号编码已存在");
        }

        // 5. 校验项目存在性和启用状态
        MesProject project = projectMapper.selectById(request.getProjectId());
        if (project == null || project.getEnable() != 1) {
            throw new BusinessException(400, "所选项目不存在或已禁用");
        }

        // 6. 创建料号实体
        MesMaterial material = new MesMaterial();
        material.setMaterialCode(request.getCode());
        material.setMaterialName(request.getName());
        material.setProjectId(request.getProjectId());
        material.setColor(request.getColor());
        material.setSize(request.getSize());
        material.setSpec1(request.getSpec1());
        material.setSpec2(request.getSpec2());
        material.setSpec3(request.getSpec3());
        material.setDescription(request.getDescription());
        material.setRemark(request.getRemark());
        material.setEnable(1); // 默认启用

        // 7. 保存料号
        materialMapper.insert(material);

        return material.getId();
    }

    @Override
    public Long edit(MaterialEditRequest request) {
        // 1. 校验料号是否存在
        MesMaterial material = materialMapper.selectById(request.getId());
        if (material == null) {
            throw new BusinessException(400, "料号不存在");
        }

        // 2. 校验必填项
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(400, "名称不能为空");
        }

        // 3. 校验字段长度
        if (request.getName().length() > NAME_MAX_LENGTH) {
            throw new BusinessException(400, "名称长度不能超过" + NAME_MAX_LENGTH + "个字符");
        }

        // 4. 更新料号信息（编码和项目ID不可修改）
        material.setMaterialName(request.getName());
        material.setColor(request.getColor());
        material.setSize(request.getSize());
        material.setSpec1(request.getSpec1());
        material.setSpec2(request.getSpec2());
        material.setSpec3(request.getSpec3());
        material.setDescription(request.getDescription());
        material.setRemark(request.getRemark());

        // 5. 保存更新
        materialMapper.updateById(material);

        return material.getId();
    }

    @Override
    public void delete(Long id) {
        // 1. 校验料号是否存在
        MesMaterial material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(400, "料号不存在");
        }

        // 2. 删除保护检查：若已绑定路由则禁止删除
        if (material.getRouteId() != null) {
            throw new BusinessException(400, "该料号已绑定路由，无法删除");
        }

        // 3. 执行逻辑删除（MyBatis-Plus 会自动设置 deleted 字段为 1）
        materialMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        // 1. 校验参数
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "料号ID列表不能为空");
        }

        // 2. 逐个检查并删除
        int successCount = 0;
        int skipCount = 0;

        for (Long id : ids) {
            MesMaterial material = materialMapper.selectById(id);
            if (material == null) {
                // 料号不存在，跳过
                skipCount++;
                continue;
            }

            if (material.getRouteId() != null) {
                // 已绑定路由，跳过
                skipCount++;
                continue;
            }

            // 执行删除
            materialMapper.deleteById(id);
            successCount++;
        }

        // 3. 返回结果统计
        if (skipCount > 0) {
            throw new BusinessException(400,
                String.format("成功删除%d条，跳过%d条（已绑定路由或有工单）", successCount, skipCount));
        }
    }

    @Override
    public void updateStatus(Long id, Integer enable) {
        // 1. 校验料号是否存在
        MesMaterial material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(400, "料号不存在");
        }

        // 2. 校验状态值有效性
        if (enable == null || (enable != 0 && enable != 1)) {
            throw new BusinessException(400, "启用状态值无效");
        }

        // 3. 更新状态
        material.setEnable(enable);
        materialMapper.updateById(material);
    }

    @Override
    public void export(MaterialQueryRequest request, HttpServletResponse response) {
        try {
            // 1. 构建查询条件（复用 page 的查询逻辑）
            LambdaQueryWrapper<MesMaterial> queryWrapper = buildQueryWrapper(request);
            queryWrapper.orderByDesc(MesMaterial::getId);

            // 2. 查询所有符合条件的数据（不分页）
            List<MesMaterial> materialList = materialMapper.selectList(queryWrapper);

            // 3. 设置响应头
            String fileName = generateExportFileName();
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 4. 写入CSV内容
            StringBuilder csv = new StringBuilder();
            // 写入表头
            csv.append("ID,编码,名称,所属项目,绑定路由,颜色,尺码,规格1,规格2,规格3,描述,备注,状态,创建人,创建时间\n");

            // 写入数据行
            for (MesMaterial material : materialList) {
                csv.append(material.getId()).append(",");
                csv.append(escapeCsvField(material.getMaterialCode())).append(",");
                csv.append(escapeCsvField(material.getMaterialName())).append(",");
                csv.append(escapeCsvField(getProjectName(material.getProjectId()))).append(",");
                csv.append(escapeCsvField(getRouteName(material.getRouteId()))).append(",");
                csv.append(escapeCsvField(material.getColor())).append(",");
                csv.append(escapeCsvField(material.getSize())).append(",");
                csv.append(escapeCsvField(material.getSpec1())).append(",");
                csv.append(escapeCsvField(material.getSpec2())).append(",");
                csv.append(escapeCsvField(material.getSpec3())).append(",");
                csv.append(escapeCsvField(material.getDescription())).append(",");
                csv.append(escapeCsvField(material.getRemark())).append(",");
                csv.append(material.getEnable()).append(",");
                csv.append(escapeCsvField(material.getCreatedBy())).append(",");
                csv.append(material.getCreateTime() != null ? material.getCreateTime().toString() : "");
                csv.append("\n");
            }

            response.getWriter().write(csv.toString());
        } catch (IOException e) {
            throw new BusinessException(500, "导出文件失败");
        }
    }

    @Override
    public List<MaterialSimpleResponse> listByProjectId(Long projectId) {
        // 1. 构建查询条件：指定项目且启用状态
        LambdaQueryWrapper<MesMaterial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MesMaterial::getProjectId, projectId);
        queryWrapper.eq(MesMaterial::getEnable, 1);
        queryWrapper.orderByAsc(MesMaterial::getMaterialCode);

        // 2. 执行查询
        List<MesMaterial> materials = materialMapper.selectList(queryWrapper);

        // 3. 转换为响应DTO
        return materials.stream()
                .map(this::convertToMaterialSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为简单响应DTO
     */
    private MaterialSimpleResponse convertToMaterialSimpleResponse(MesMaterial material) {
        MaterialSimpleResponse response = new MaterialSimpleResponse();
        response.setId(material.getId());
        response.setCode(material.getMaterialCode());
        response.setName(material.getMaterialName());
        return response;
    }

    /**
     * 转换为响应DTO
     */
    private MaterialResponse convertToMaterialResponse(MesMaterial material) {
        MaterialResponse response = new MaterialResponse();
        response.setId(material.getId());
        response.setCode(material.getMaterialCode());
        response.setName(material.getMaterialName());
        response.setProjectId(material.getProjectId());

        // 查询项目名称
        if (material.getProjectId() != null) {
            MesProject project = projectMapper.selectById(material.getProjectId());
            if (project != null) {
                response.setProjectName(project.getName());
            }
        }

        response.setColor(material.getColor());
        response.setSize(material.getSize());
        response.setSpec1(material.getSpec1());
        response.setSpec2(material.getSpec2());
        response.setSpec3(material.getSpec3());
        response.setDescription(material.getDescription());
        response.setRemark(material.getRemark());
        response.setEnable(material.getEnable());
        response.setCreatedBy(material.getCreatedBy());
        response.setCreateTime(material.getCreateTime());
        response.setUpdatedBy(material.getUpdatedBy());
        response.setUpdateTime(material.getUpdateTime());
        return response;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<MesMaterial> buildQueryWrapper(MaterialQueryRequest request) {
        LambdaQueryWrapper<MesMaterial> queryWrapper = new LambdaQueryWrapper<>();

        // 编码模糊查询
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            queryWrapper.like(MesMaterial::getMaterialCode, request.getCode());
        }

        // 名称模糊查询
        if (request.getName() != null && !request.getName().isEmpty()) {
            queryWrapper.like(MesMaterial::getMaterialName, request.getName());
        }

        // 项目ID精确查询
        if (request.getProjectId() != null) {
            queryWrapper.eq(MesMaterial::getProjectId, request.getProjectId());
        }

        // 启用状态精确查询
        if (request.getEnable() != null) {
            queryWrapper.eq(MesMaterial::getEnable, request.getEnable());
        }

        return queryWrapper;
    }

    /**
     * 生成导出文件名
     */
    private String generateExportFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "料号列表_" + timestamp + ".csv";
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

    /**
     * 获取项目名称
     */
    private String getProjectName(Long projectId) {
        if (projectId == null) {
            return "";
        }
        MesProject project = projectMapper.selectById(projectId);
        return project != null ? project.getName() : "";
    }

    /**
     * 获取路由名称
     */
    private String getRouteName(Long routeId) {
        if (routeId == null) {
            return "";
        }
        // 路由模块暂未实现，返回占位符
        return "已绑定";
    }
}