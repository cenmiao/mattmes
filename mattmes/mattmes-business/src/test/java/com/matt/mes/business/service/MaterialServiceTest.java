package com.matt.mes.business.service;

import com.matt.mes.business.config.TestConfig;
import com.matt.mes.business.dto.*;
import com.matt.mes.business.entity.MesMaterial;
import com.matt.mes.business.entity.MesProject;
import com.matt.mes.business.mapper.MaterialMapper;
import com.matt.mes.business.mapper.ProjectMapper;
import com.matt.mes.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MaterialService集成测试
 * 测试料号业务行为
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private ProjectMapper projectMapper;

    private Long testProjectId;

    @BeforeEach
    void setUp() {
        // 准备测试项目
        MesProject project = new MesProject();
        project.setCode("TEST-PROJECT-001");
        project.setName("测试项目");
        project.setEnable(1);
        projectMapper.insert(project);
        testProjectId = project.getId();

        // 准备测试料号
        insertTestMaterial("MAT-001", "料号1", testProjectId, 1);
        insertTestMaterial("MAT-002", "料号2", testProjectId, 1);
        insertTestMaterial("MAT-003", "料号3", testProjectId, 0);
    }

    // ========== 分页查询测试 ==========

    @Test
    @DisplayName("无条件查询返回所有料号")
    void shouldReturnAllMaterialsWithoutCondition() {
        MaterialQueryRequest request = new MaterialQueryRequest();
        request.setPageNum(1);
        request.setPageSize(100);

        MaterialPageResult<MaterialResponse> result = materialService.page(request);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 3);
        assertTrue(result.getList().size() >= 3);
    }

    @Test
    @DisplayName("可以根据编码模糊查询")
    void shouldQueryByCodeFuzzy() {
        MaterialQueryRequest request = new MaterialQueryRequest();
        request.setCode("MAT-00");

        MaterialPageResult<MaterialResponse> result = materialService.page(request);

        assertEquals(3, result.getTotal());
        assertTrue(result.getList().stream().allMatch(m -> m.getCode().contains("MAT-00")));
    }

    @Test
    @DisplayName("可以根据名称模糊查询")
    void shouldQueryByNameFuzzy() {
        MaterialQueryRequest request = new MaterialQueryRequest();
        request.setName("料号");

        MaterialPageResult<MaterialResponse> result = materialService.page(request);

        assertTrue(result.getTotal() >= 3);
        assertTrue(result.getList().stream().allMatch(m -> m.getName().contains("料号")));
    }

    @Test
    @DisplayName("可以根据项目ID查询")
    void shouldQueryByProjectId() {
        MaterialQueryRequest request = new MaterialQueryRequest();
        request.setProjectId(testProjectId);

        MaterialPageResult<MaterialResponse> result = materialService.page(request);

        assertTrue(result.getTotal() >= 3);
        assertTrue(result.getList().stream().allMatch(m -> m.getProjectId().equals(testProjectId)));
    }

    @Test
    @DisplayName("可以根据启用状态查询")
    void shouldQueryByEnableStatus() {
        MaterialQueryRequest request = new MaterialQueryRequest();
        request.setEnable(1);

        MaterialPageResult<MaterialResponse> result = materialService.page(request);

        assertTrue(result.getTotal() >= 2);
        assertTrue(result.getList().stream().allMatch(m -> m.getEnable() == 1));
    }

    // ========== 新增料号测试 ==========

    @Test
    @DisplayName("新增料号成功返回料号ID")
    void shouldAddMaterialSuccessfully() {
        MaterialAddRequest request = new MaterialAddRequest();
        request.setCode("MAT-NEW-001");
        request.setName("新料号");
        request.setProjectId(testProjectId);
        request.setColor("红色");
        request.setSize("XL");

        Long materialId = materialService.add(request);

        assertNotNull(materialId);

        MesMaterial material = materialMapper.selectById(materialId);
        assertNotNull(material);
        assertEquals("MAT-NEW-001", material.getMaterialCode());
        assertEquals("新料号", material.getMaterialName());
        assertEquals(testProjectId, material.getProjectId());
        assertEquals("红色", material.getColor());
        assertEquals("XL", material.getSize());
        assertEquals(1, material.getEnable());
    }

    @Test
    @DisplayName("编码重复时新增料号应抛出业务异常")
    void shouldThrowWhenCodeDuplicate() {
        MaterialAddRequest request = new MaterialAddRequest();
        request.setCode("MAT-001"); // 已存在
        request.setName("新料号");
        request.setProjectId(testProjectId);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.add(request);
        });
        assertTrue(exception.getMessage().contains("料号编码已存在"));
    }

    @Test
    @DisplayName("项目不存在时新增料号应抛出业务异常")
    void shouldThrowWhenProjectNotFound() {
        MaterialAddRequest request = new MaterialAddRequest();
        request.setCode("MAT-NEW-002");
        request.setName("新料号");
        request.setProjectId(99999L); // 不存在的项目

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.add(request);
        });
        assertTrue(exception.getMessage().contains("所选项目不存在或已禁用"));
    }

    @Test
    @DisplayName("项目已禁用时新增料号应抛出业务异常")
    void shouldThrowWhenProjectDisabled() {
        // 创建已禁用的项目
        MesProject disabledProject = new MesProject();
        disabledProject.setCode("DISABLED-PROJECT");
        disabledProject.setName("已禁用项目");
        disabledProject.setEnable(0);
        projectMapper.insert(disabledProject);

        MaterialAddRequest request = new MaterialAddRequest();
        request.setCode("MAT-NEW-003");
        request.setName("新料号");
        request.setProjectId(disabledProject.getId());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.add(request);
        });
        assertTrue(exception.getMessage().contains("所选项目不存在或已禁用"));
    }

    @Test
    @DisplayName("编码格式不正确时新增料号应抛出业务异常")
    void shouldThrowWhenCodeFormatInvalid() {
        MaterialAddRequest request = new MaterialAddRequest();
        request.setCode("MAT@001"); // 包含非法字符
        request.setName("新料号");
        request.setProjectId(testProjectId);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.add(request);
        });
        assertTrue(exception.getMessage().contains("编码只能包含字母、数字、下划线和中划线"));
    }

    // ========== 编辑料号测试 ==========

    @Test
    @DisplayName("编辑料号成功返回料号ID")
    void shouldEditMaterialSuccessfully() {
        // 先插入一条料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-EDIT-001");
        material.setMaterialName("原始料号");
        material.setProjectId(testProjectId);
        material.setEnable(1);
        materialMapper.insert(material);

        MaterialEditRequest request = new MaterialEditRequest();
        request.setId(material.getId());
        request.setName("更新后的料号");
        request.setColor("蓝色");
        request.setSize("M");

        Long resultId = materialService.edit(request);

        assertNotNull(resultId);
        assertEquals(material.getId(), resultId);

        MesMaterial updated = materialMapper.selectById(material.getId());
        assertEquals("MAT-EDIT-001", updated.getMaterialCode()); // 编码不变
        assertEquals(testProjectId, updated.getProjectId()); // 项目ID不变
        assertEquals("更新后的料号", updated.getMaterialName());
        assertEquals("蓝色", updated.getColor());
        assertEquals("M", updated.getSize());
    }

    @Test
    @DisplayName("料号不存在时编辑应抛出业务异常")
    void shouldThrowWhenMaterialNotFound() {
        MaterialEditRequest request = new MaterialEditRequest();
        request.setId(99999L);
        request.setName("测试料号");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.edit(request);
        });
        assertTrue(exception.getMessage().contains("料号不存在"));
    }

    /**
     * 辅助方法:插入测试料号
     */
    private void insertTestMaterial(String code, String name, Long projectId, Integer enable) {
        MesMaterial material = new MesMaterial();
        material.setMaterialCode(code);
        material.setMaterialName(name);
        material.setProjectId(projectId);
        material.setEnable(enable);
        materialMapper.insert(material);
    }
}
