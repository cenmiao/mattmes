package com.matt.mes.business.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.matt.mes.business.config.TestConfig;
import com.matt.mes.business.entity.MesProcess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProcessMapper集成测试
 * 测试工序数据访问行为
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class ProcessMapperTest {

    @Autowired
    private ProcessMapper processMapper;

    @AfterEach
    void tearDown() {
        // 清理测试数据
        processMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("可以插入工序数据")
    void canInsertProcess() {
        // 准备测试数据
        MesProcess process = new MesProcess();
        process.setCode("TEST-001");
        process.setName("测试工序");
        process.setProcessType("INSPECTION");
        process.setDescription("这是一个测试工序");
        process.setEnable(1);
        process.setRemark("测试备注");

        // 执行插入
        int result = processMapper.insert(process);

        // 验证插入成功
        assertEquals(1, result);
        assertNotNull(process.getId());
    }

    @Test
    @DisplayName("可以查询工序列表并分页")
    void canQueryProcessListWithPaging() {
        // 准备测试数据:插入3条工序
        for (int i = 1; i <= 3; i++) {
            MesProcess process = new MesProcess();
            process.setCode("PROC-00" + i);
            process.setName("工序" + i);
            process.setProcessType("INSPECTION");
            process.setEnable(1);
            processMapper.insert(process);
        }

        // 执行分页查询:第1页,每页2条
        Page<MesProcess> page = new Page<>(1, 2);
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(MesProcess::getId);

        Page<MesProcess> result = processMapper.selectPage(page, queryWrapper);

        // 验证分页结果
        assertEquals(2, result.getRecords().size());
        assertEquals(3, result.getTotal());
        assertEquals(1, result.getCurrent());
        assertEquals(2, result.getSize());
    }

    @Test
    @DisplayName("可以根据编码模糊查询")
    void canQueryByCodeFuzzy() {
        // 准备测试数据
        insertTestProcess("ASM-001", "组装工序1", "ASSEMBLY");
        insertTestProcess("ASM-002", "组装工序2", "ASSEMBLY");
        insertTestProcess("INS-001", "检测工序1", "INSPECTION");

        // 执行编码模糊查询
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MesProcess::getCode, "ASM");

        List<MesProcess> results = processMapper.selectList(queryWrapper);

        // 验证查询结果
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getCode().contains("ASM")));
    }

    @Test
    @DisplayName("可以根据名称模糊查询")
    void canQueryByNameFuzzy() {
        // 准备测试数据
        insertTestProcess("P001", "外观检测工序", "INSPECTION");
        insertTestProcess("P002", "功能检测工序", "INSPECTION");
        insertTestProcess("P003", "组装工序", "ASSEMBLY");

        // 执行名称模糊查询
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MesProcess::getName, "检测");

        List<MesProcess> results = processMapper.selectList(queryWrapper);

        // 验证查询结果
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getName().contains("检测")));
    }

    @Test
    @DisplayName("可以根据工序类型精确查询")
    void canQueryByProcessTypeExact() {
        // 准备测试数据
        insertTestProcess("P001", "检测工序", "INSPECTION");
        insertTestProcess("P002", "组装工序", "ASSEMBLY");
        insertTestProcess("P003", "包装工序", "PACKAGING");

        // 执行类型精确查询
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MesProcess::getProcessType, "ASSEMBLY");

        List<MesProcess> results = processMapper.selectList(queryWrapper);

        // 验证查询结果
        assertEquals(1, results.size());
        assertEquals("ASSEMBLY", results.get(0).getProcessType());
    }

    @Test
    @DisplayName("可以根据启用状态精确查询")
    void canQueryByEnableStatusExact() {
        // 准备测试数据
        insertTestProcess("P001", "启用工序", "INSPECTION", 1);
        insertTestProcess("P002", "禁用工序", "ASSEMBLY", 0);

        // 执行状态精确查询
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MesProcess::getEnable, 1);

        List<MesProcess> results = processMapper.selectList(queryWrapper);

        // 验证查询结果
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getEnable());
    }

    @Test
    @DisplayName("可以组合多个条件查询")
    void canQueryWithMultipleConditions() {
        // 准备测试数据
        insertTestProcess("ASM-001", "组装工序A", "ASSEMBLY", 1);
        insertTestProcess("ASM-002", "组装工序B", "ASSEMBLY", 1);
        insertTestProcess("ASM-003", "组装工序C", "ASSEMBLY", 0);
        insertTestProcess("INS-001", "检测工序", "INSPECTION", 1);

        // 执行组合查询:编码包含ASM + 类型=ASSEMBLY + 状态=1
        LambdaQueryWrapper<MesProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MesProcess::getCode, "ASM")
                   .eq(MesProcess::getProcessType, "ASSEMBLY")
                   .eq(MesProcess::getEnable, 1);

        List<MesProcess> results = processMapper.selectList(queryWrapper);

        // 验证查询结果
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p ->
            p.getCode().contains("ASM") &&
            "ASSEMBLY".equals(p.getProcessType()) &&
            p.getEnable() == 1
        ));
    }

    /**
     * 辅助方法:插入测试工序
     */
    private void insertTestProcess(String code, String name, String processType) {
        insertTestProcess(code, name, processType, 1);
    }

    /**
     * 辅助方法:插入测试工序(带状态)
     */
    private void insertTestProcess(String code, String name, String processType, Integer enable) {
        MesProcess process = new MesProcess();
        process.setCode(code);
        process.setName(name);
        process.setProcessType(processType);
        process.setEnable(enable);
        processMapper.insert(process);
    }
}
