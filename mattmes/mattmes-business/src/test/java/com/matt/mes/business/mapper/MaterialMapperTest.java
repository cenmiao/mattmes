package com.matt.mes.business.mapper;

import com.matt.mes.business.config.TestConfig;
import com.matt.mes.business.entity.MesMaterial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MaterialMapper集成测试
 * 测试料号数据访问行为
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class MaterialMapperTest {

    @Autowired
    private MaterialMapper materialMapper;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        insertTestMaterial("MAT-001", "料号1", 1L);
        insertTestMaterial("MAT-002", "料号2", 1L);
        insertTestMaterial("MAT-003", "料号3", 1L);
    }

    @Test
    @DisplayName("统计编码数量(包括已删除记录)")
    void shouldCountByCodeIncludeDeleted() {
        // 1. 先插入一条料号
        MesMaterial material = new MesMaterial();
        material.setMaterialCode("MAT-TEST-001");
        material.setMaterialName("测试料号");
        material.setProjectId(1L);
        material.setEnable(1);
        materialMapper.insert(material);

        // 2. 统计编码数量(应包含刚插入的记录)
        Long count = materialMapper.countByCodeIncludeDeleted("MAT-TEST-001");
        assertEquals(1, count);

        // 3. 逻辑删除该料号
        materialMapper.deleteById(material.getId());

        // 4. 再次统计编码数量(仍应包含已删除的记录)
        Long countAfterDelete = materialMapper.countByCodeIncludeDeleted("MAT-TEST-001");
        assertEquals(1, countAfterDelete); // 包括已删除记录

        // 5. 统计不存在的编码
        Long countNonExistent = materialMapper.countByCodeIncludeDeleted("MAT-NON-EXISTENT");
        assertEquals(0, countNonExistent);
    }

    /**
     * 辅助方法:插入测试料号
     */
    private void insertTestMaterial(String code, String name, Long projectId) {
        MesMaterial material = new MesMaterial();
        material.setMaterialCode(code);
        material.setMaterialName(name);
        material.setProjectId(projectId);
        material.setEnable(1);
        materialMapper.insert(material);
    }
}
