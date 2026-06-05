package com.matt.mes.business.service;

import com.matt.mes.business.config.TestConfig;
import com.matt.mes.business.dto.ProcessAddRequest;
import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;
import com.matt.mes.business.entity.MesProcess;
import com.matt.mes.business.mapper.ProcessMapper;
import com.matt.mes.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProcessService集成测试
 * 测试工序查询业务行为
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class ProcessServiceTest {

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessMapper processMapper;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        insertTestProcess("ASM-001", "组装工序A", "ASSEMBLY", 1);
        insertTestProcess("ASM-002", "组装工序B", "ASSEMBLY", 1);
        insertTestProcess("ASM-003", "组装工序C", "ASSEMBLY", 0);
        insertTestProcess("INS-001", "外观检测", "INSPECTION", 1);
        insertTestProcess("INS-002", "功能检测", "INSPECTION", 1);
        insertTestProcess("PKG-001", "包装工序", "PACKAGING", 1);
    }

    @Test
    @DisplayName("无条件查询返回所有工序")
    void shouldReturnAllProcessesWhenNoCondition() {
        // 准备请求:无条件查询
        ProcessQueryRequest request = new ProcessQueryRequest();

        // 执行查询
        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(6, result.getTotal());
        assertEquals(6, result.getList().size());
    }

    @Test
    @DisplayName("返回正确格式的分页结果")
    void shouldReturnCorrectPageResultFormat() {
        // 准备请求:第1页,每页3条
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setPageNum(1);
        request.setPageSize(3);

        // 执行查询
        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        // 验证分页格式
        assertNotNull(result);
        assertNotNull(result.getList());
        assertNotNull(result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(3, result.getPageSize());
    }

    @Test
    @DisplayName("正确处理分页参数")
    void shouldHandlePagingParametersCorrectly() {
        // 测试第1页
        ProcessQueryRequest request1 = new ProcessQueryRequest();
        request1.setPageNum(1);
        request1.setPageSize(2);

        ProcessPageResult<ProcessResponse> result1 = processService.queryList(request1);
        assertEquals(2, result1.getList().size());
        assertEquals(6, result1.getTotal());

        // 测试第2页
        ProcessQueryRequest request2 = new ProcessQueryRequest();
        request2.setPageNum(2);
        request2.setPageSize(2);

        ProcessPageResult<ProcessResponse> result2 = processService.queryList(request2);
        assertEquals(2, result2.getList().size());
        assertEquals(6, result2.getTotal());

        // 测试第3页(最后一页)
        ProcessQueryRequest request3 = new ProcessQueryRequest();
        request3.setPageNum(3);
        request3.setPageSize(2);

        ProcessPageResult<ProcessResponse> result3 = processService.queryList(request3);
        assertEquals(2, result3.getList().size());
        assertEquals(6, result3.getTotal());
    }

    @Test
    @DisplayName("查询结果包含正确的字段值")
    void shouldContainCorrectFieldValuesInResult() {
        // 准备请求:查询编码为ASM-001的工序
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setCode("ASM-001");

        // 执行查询
        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        // 验证字段值
        assertEquals(1, result.getList().size());
        ProcessResponse response = result.getList().get(0);

        assertEquals("ASM-001", response.getCode());
        assertEquals("组装工序A", response.getName());
        assertEquals("ASSEMBLY", response.getProcessType());
        assertEquals(1, response.getEnable());
        assertNotNull(response.getId());
        assertNotNull(response.getCreateTime());
    }

    @Test
    @DisplayName("可以根据编码模糊查询")
    void shouldQueryByCodeFuzzy() {
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setCode("ASM");

        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        assertEquals(3, result.getTotal());
        assertTrue(result.getList().stream().allMatch(p -> p.getCode().contains("ASM")));
    }

    @Test
    @DisplayName("可以根据名称模糊查询")
    void shouldQueryByNameFuzzy() {
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setName("检测");

        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        assertEquals(2, result.getTotal());
        assertTrue(result.getList().stream().allMatch(p -> p.getName().contains("检测")));
    }

    @Test
    @DisplayName("可以根据工序类型精确查询")
    void shouldQueryByProcessTypeExact() {
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setProcessType("INSPECTION");

        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        assertEquals(2, result.getTotal());
        assertTrue(result.getList().stream().allMatch(p -> "INSPECTION".equals(p.getProcessType())));
    }

    @Test
    @DisplayName("可以根据启用状态精确查询")
    void shouldQueryByEnableStatusExact() {
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setEnable(1);

        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        assertEquals(5, result.getTotal());
        assertTrue(result.getList().stream().allMatch(p -> p.getEnable() == 1));
    }

    @Test
    @DisplayName("可以组合多个条件查询")
    void shouldQueryWithMultipleConditions() {
        // 组合查询:编码包含ASM + 类型=ASSEMBLY + 状态=1
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setCode("ASM");
        request.setProcessType("ASSEMBLY");
        request.setEnable(1);

        ProcessPageResult<ProcessResponse> result = processService.queryList(request);

        assertEquals(2, result.getTotal());
        assertTrue(result.getList().stream().allMatch(p ->
            p.getCode().contains("ASM") &&
            "ASSEMBLY".equals(p.getProcessType()) &&
            p.getEnable() == 1
        ));
    }

    /**
     * 辅助方法:插入测试工序
     */
    private void insertTestProcess(String code, String name, String processType, Integer enable) {
        MesProcess process = new MesProcess();
        process.setCode(code);
        process.setName(name);
        process.setProcessType(processType);
        process.setEnable(enable);
        processMapper.insert(process);
    }

    // ========== 新增工序测试 ==========

    @Test
    @DisplayName("编码重复时新增工序应抛出业务异常")
    void shouldThrowWhenCodeDuplicate() {
        // 准备:先插入一条编码为DUP-001的工序（使用不与setUp冲突的编码）
        insertTestProcess("DUP-001", "测试工序", "ASSEMBLY", 1);

        // 构建新增请求:使用相同编码
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("DUP-001");
        request.setName("新组装工序");
        request.setProcessType("ASSEMBLY");

        // 验证:应抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            processService.add(request);
        });
        assertTrue(exception.getMessage().contains("编码已存在"));
    }

    @Test
    @DisplayName("编码为空时新增工序应抛出业务异常")
    void shouldThrowWhenCodeEmpty() {
        // 构建新增请求:编码为空
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode(null);
        request.setName("测试工序");
        request.setProcessType("ASSEMBLY");

        // 验证:应抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            processService.add(request);
        });
        assertTrue(exception.getMessage().contains("编码不能为空"));
    }

    @Test
    @DisplayName("名称为空时新增工序应抛出业务异常")
    void shouldThrowWhenNameEmpty() {
        // 构建新增请求:名称为空
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("NEW-001");
        request.setName(null);
        request.setProcessType("ASSEMBLY");

        // 验证:应抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            processService.add(request);
        });
        assertTrue(exception.getMessage().contains("名称不能为空"));
    }

    @Test
    @DisplayName("工序类型为空时新增工序应抛出业务异常")
    void shouldThrowWhenProcessTypeEmpty() {
        // 构建新增请求:工序类型为空
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("NEW-001");
        request.setName("测试工序");
        request.setProcessType(null);

        // 验证:应抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            processService.add(request);
        });
        assertTrue(exception.getMessage().contains("工序类型不能为空"));
    }

    @Test
    @DisplayName("编码超过50字符时新增工序应抛出业务异常")
    void shouldThrowWhenCodeTooLong() {
        // 构建新增请求:编码超过50字符
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("A".repeat(51));  // 51个字符
        request.setName("测试工序");
        request.setProcessType("ASSEMBLY");

        // 验证:应抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            processService.add(request);
        });
        assertTrue(exception.getMessage().contains("编码长度不能超过50个字符"));
    }

    @Test
    @DisplayName("名称超过100字符时新增工序应抛出业务异常")
    void shouldThrowWhenNameTooLong() {
        // 构建新增请求:名称超过100字符
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("NEW-001");
        request.setName("A".repeat(101));  // 101个字符
        request.setProcessType("ASSEMBLY");

        // 验证:应抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            processService.add(request);
        });
        assertTrue(exception.getMessage().contains("名称长度不能超过100个字符"));
    }

    @Test
    @DisplayName("编码格式不正确时新增工序应抛出业务异常")
    void shouldThrowWhenCodeFormatInvalid() {
        // 构建新增请求:编码包含非法字符（仅允许字母、数字、下划线、中划线）
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("CODE@001");  // 包含@
        request.setName("测试工序");
        request.setProcessType("ASSEMBLY");

        // 验证:应抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            processService.add(request);
        });
        assertTrue(exception.getMessage().contains("编码只能包含字母、数字、下划线和中划线"));
    }
}
