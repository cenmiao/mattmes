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
        // TODO: 待实现
        throw new BusinessException(500, "功能开发中");
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
        // TODO: 待实现
        throw new BusinessException(500, "功能开发中");
    }

    @Override
    public void batchDelete(List<Long> ids) {
        // TODO: 待实现
        throw new BusinessException(500, "功能开发中");
    }

    @Override
    public void updateStatus(Long id, Integer enable) {
        // TODO: 待实现
        throw new BusinessException(500, "功能开发中");
    }

    @Override
    public void export(MaterialQueryRequest request, HttpServletResponse response) {
        // TODO: 待实现
        throw new BusinessException(500, "功能开发中");
    }

    @Override
    public List<MaterialSimpleResponse> listByProjectId(Long projectId) {
        // TODO: 待实现
        throw new BusinessException(500, "功能开发中");
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
}