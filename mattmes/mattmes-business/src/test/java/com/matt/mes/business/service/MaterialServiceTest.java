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

import java.util.Arrays;
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

    // ========== 获取料号详情测试 ==========

    @Test
    @DisplayName("获取料号详情成功")
    void shouldGetMaterialByIdSuccessfully() {
        // 先插入一条料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-GET-001");
        material.setMaterialName("测试料号");
        material.setProjectId(testProjectId);
        material.setEnable(1);
        materialMapper.insert(material);

        // 执行查询
        MaterialResponse response = materialService.getById(material.getId());

        // 验证结果
        assertNotNull(response);
        assertEquals(material.getId(), response.getId());
        assertEquals("MAT-GET-001", response.getCode());
        assertEquals("测试料号", response.getName());
        assertEquals(testProjectId, response.getProjectId());
        assertEquals(1, response.getEnable());
    }

    @Test
    @DisplayName("料号不存在时获取详情应抛出业务异常")
    void shouldThrowWhenGetByIdNotFound() {
        // 尝试获取不存在的料号
        Long nonExistentId = 99999L;

        // 验证：应抛出 BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.getById(nonExistentId);
        });
        assertTrue(exception.getMessage().contains("料号不存在"));
    }

    // ========== 按项目查询料号列表测试 ==========

    @Test
    @DisplayName("按项目查询料号列表成功")
    void shouldListByProjectIdSuccessfully() {
        // 执行查询
        List<MaterialSimpleResponse> result = materialService.listByProjectId(testProjectId);

        // 验证返回列表不为空
        assertNotNull(result);

        // 验证返回的料号都属于指定项目
        assertTrue(result.stream().allMatch(m -> m.getId() != null));
        assertTrue(result.stream().anyMatch(m -> "MAT-001".equals(m.getCode())));
        assertTrue(result.stream().anyMatch(m -> "MAT-002".equals(m.getCode())));
    }

    @Test
    @DisplayName("按项目查询只返回启用的料号")
    void shouldListOnlyEnabledMaterialsByProjectId() {
        // 执行查询
        List<MaterialSimpleResponse> result = materialService.listByProjectId(testProjectId);

        // MAT-003 是禁用状态（enable=0），不应出现在列表中
        assertTrue(result.stream().noneMatch(m -> "MAT-003".equals(m.getCode())));
    }

    // ========== 删除料号测试 ==========

    @Test
    @DisplayName("删除无绑定的料号成功")
    void shouldDeleteMaterialSuccessfully() {
        // 先插入一条无绑定的料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-DEL-001");
        material.setMaterialName("待删除料号");
        material.setProjectId(testProjectId);
        material.setEnable(1);
        material.setRouteId(null); // 无路由绑定
        materialMapper.insert(material);

        // 执行删除
        materialService.delete(material.getId());

        // 验证料号已被逻辑删除
        MesMaterial deletedMaterial = materialMapper.selectById(material.getId());
        assertNull(deletedMaterial);
    }

    @Test
    @DisplayName("料号不存在时删除应抛出业务异常")
    void shouldThrowWhenDeleteMaterialNotFound() {
        // 尝试删除不存在的料号
        Long nonExistentId = 99999L;

        // 验证：应抛出 BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.delete(nonExistentId);
        });
        assertTrue(exception.getMessage().contains("料号不存在"));
    }

    @Test
    @DisplayName("删除已绑定路由的料号应抛出业务异常")
    void shouldThrowWhenDeleteMaterialWithRoute() {
        // 先插入一条已绑定路由的料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-ROUTE-001");
        material.setMaterialName("已绑定路由料号");
        material.setProjectId(testProjectId);
        material.setEnable(1);
        material.setRouteId(100L); // 已绑定路由
        materialMapper.insert(material);

        // 验证：应抛出 BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.delete(material.getId());
        });
        assertTrue(exception.getMessage().contains("该料号已绑定路由，无法删除"));
    }

    // ========== 批量删除料号测试 ==========

    @Test
    @DisplayName("批量删除无绑定的料号成功")
    void shouldBatchDeleteMaterialsSuccessfully() {
        // 先插入多条无绑定的料号
        MesMaterial material1 = new MesMaterial();
        material1.setMaterialCode("MAT-BATCH-001");
        material1.setMaterialName("待删除料号1");
        material1.setProjectId(testProjectId);
        material1.setEnable(1);
        materialMapper.insert(material1);

        MesMaterial material2 = new MesMaterial();
        material2.setMaterialCode("MAT-BATCH-002");
        material2.setMaterialName("待删除料号2");
        material2.setProjectId(testProjectId);
        material2.setEnable(1);
        materialMapper.insert(material2);

        List<Long> ids = Arrays.asList(material1.getId(), material2.getId());

        // 执行批量删除
        materialService.batchDelete(ids);

        // 验证料号已被逻辑删除
        assertNull(materialMapper.selectById(material1.getId()));
        assertNull(materialMapper.selectById(material2.getId()));
    }

    @Test
    @DisplayName("批量删除混合场景应返回正确的成功和跳过数量")
    void shouldBatchDeleteMixedMaterials() {
        // 准备：插入可删除和不可删除的料号
        MesMaterial material1 = new MesMaterial();
        material1.setMaterialCode("MAT-MIX-001");
        material1.setMaterialName("可删除料号1");
        material1.setProjectId(testProjectId);
        material1.setEnable(1);
        materialMapper.insert(material1);

        MesMaterial material2 = new MesMaterial();
        material2.setMaterialCode("MAT-MIX-002");
        material2.setMaterialName("已绑定路由料号");
        material2.setProjectId(testProjectId);
        material2.setEnable(1);
        material2.setRouteId(200L); // 已绑定路由
        materialMapper.insert(material2);

        MesMaterial material3 = new MesMaterial();
        material3.setMaterialCode("MAT-MIX-003");
        material3.setMaterialName("可删除料号2");
        material3.setProjectId(testProjectId);
        material3.setEnable(1);
        materialMapper.insert(material3);

        List<Long> ids = Arrays.asList(material1.getId(), material2.getId(), material3.getId());

        // 执行批量删除，应抛出包含统计信息的异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.batchDelete(ids);
        });

        // 验证异常消息包含成功和跳过数量
        assertTrue(exception.getMessage().contains("成功删除2条"));
        assertTrue(exception.getMessage().contains("跳过1条"));

        // 验证：可删除的已删除，不可删除的保留
        assertNull(materialMapper.selectById(material1.getId()));
        assertNotNull(materialMapper.selectById(material2.getId())); // 已绑定路由，保留
        assertNull(materialMapper.selectById(material3.getId()));
    }

    @Test
    @DisplayName("批量删除空列表应抛出业务异常")
    void shouldThrowWhenBatchDeleteEmptyList() {
        // 验证：应抛出 BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.batchDelete(Arrays.asList());
        });
        assertTrue(exception.getMessage().contains("料号ID列表不能为空"));
    }

    // ========== 状态切换测试 ==========

    @Test
    @DisplayName("启用料号成功")
    void shouldEnableMaterialSuccessfully() {
        // 先插入一条禁用的料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-STATUS-001");
        material.setMaterialName("待启用料号");
        material.setProjectId(testProjectId);
        material.setEnable(0);
        materialMapper.insert(material);

        // 执行启用
        materialService.updateStatus(material.getId(), 1);

        // 验证状态已更新
        MesMaterial updated = materialMapper.selectById(material.getId());
        assertEquals(1, updated.getEnable());
    }

    @Test
    @DisplayName("禁用料号成功")
    void shouldDisableMaterialSuccessfully() {
        // 先插入一条启用的料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-STATUS-002");
        material.setMaterialName("待禁用料号");
        material.setProjectId(testProjectId);
        material.setEnable(1);
        materialMapper.insert(material);

        // 执行禁用
        materialService.updateStatus(material.getId(), 0);

        // 验证状态已更新
        MesMaterial updated = materialMapper.selectById(material.getId());
        assertEquals(0, updated.getEnable());
    }

    @Test
    @DisplayName("料号不存在时状态切换应抛出业务异常")
    void shouldThrowWhenUpdateStatusMaterialNotFound() {
        // 尝试切换不存在的料号状态
        Long nonExistentId = 99999L;

        // 验证：应抛出 BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.updateStatus(nonExistentId, 1);
        });
        assertTrue(exception.getMessage().contains("料号不存在"));
    }

    @Test
    @DisplayName("状态值无效时状态切换应抛出业务异常")
    void shouldThrowWhenUpdateStatusInvalidValue() {
        // 先插入一条料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-STATUS-003");
        material.setMaterialName("测试料号");
        material.setProjectId(testProjectId);
        material.setEnable(1);
        materialMapper.insert(material);

        // 验证：无效状态值应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            materialService.updateStatus(material.getId(), 2);
        });
        assertTrue(exception.getMessage().contains("启用状态值无效"));
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
