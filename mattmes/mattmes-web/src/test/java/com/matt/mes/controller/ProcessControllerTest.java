package com.matt.mes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matt.mes.business.dto.ProcessAddRequest;
import com.matt.mes.business.dto.ProcessEditRequest;
import com.matt.mes.business.dto.ProcessPageResult;
import com.matt.mes.business.dto.ProcessQueryRequest;
import com.matt.mes.business.dto.ProcessResponse;
import com.matt.mes.business.entity.MesProcess;
import com.matt.mes.business.mapper.ProcessMapper;
import com.matt.mes.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProcessController集成测试
 * 测试工序查询接口行为
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessMapper processMapper;

    // 永久测试token
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyTm8iOiJhZG1pbiIsInVzZXJJZCI6MSwiaWF0IjoxNzgwNDg2OTEyLCJleHAiOjE4MTIwMjI5MTJ9.be7sM94QmpqKrkr3iYWMRkROzaKyb-LGZNF3SW93VPzSzjEEjJy06zbOCeOjsTGK";

    @BeforeEach
    void setUp() {
        // 准备测试数据
        insertTestProcess("ASM-001", "组装工序A", "ASSEMBLY", 1);
        insertTestProcess("ASM-002", "组装工序B", "ASSEMBLY", 1);
        insertTestProcess("INS-001", "检测工序", "INSPECTION", 1);
    }

    @Test
    @DisplayName("POST /api/process/list 接口可访问")
    void shouldAccessProcessListEndpoint() throws Exception {
        ProcessQueryRequest request = new ProcessQueryRequest();

        mockMvc.perform(post("/api/process/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("返回统一响应格式")
    void shouldReturnUnifiedResponseFormat() throws Exception {
        ProcessQueryRequest request = new ProcessQueryRequest();

        MvcResult result = mockMvc.perform(post("/api/process/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Response: " + responseBody);
    }

    @Test
    @DisplayName("正确接收查询参数")
    void shouldReceiveQueryParametersCorrectly() throws Exception {
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setCode("ASM");
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/api/process/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    @DisplayName("查询结果包含正确的字段")
    void shouldContainCorrectFieldsInResult() throws Exception {
        ProcessQueryRequest request = new ProcessQueryRequest();
        request.setCode("ASM-001");

        mockMvc.perform(post("/api/process/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].code").value("ASM-001"))
                .andExpect(jsonPath("$.data.list[0].name").value("组装工序A"))
                .andExpect(jsonPath("$.data.list[0].processType").value("ASSEMBLY"))
                .andExpect(jsonPath("$.data.list[0].enable").value(1));
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

    // ========== 新增工序接口测试 ==========

    @Test
    @DisplayName("POST /api/process/add 接口新增工序成功")
    void shouldAddProcessSuccessfully() throws Exception {
        // 构建新增请求
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("NEW-001");
        request.setName("新工序");
        request.setProcessType("ASSEMBLY");

        // 验证响应
        mockMvc.perform(post("/api/process/add")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("编码重复时返回业务错误")
    void shouldReturnErrorWhenCodeDuplicate() throws Exception {
        // 准备:已存在的工序
        insertTestProcess("DUP-002", "已存在工序", "ASSEMBLY", 1);

        // 构建新增请求:使用重复编码
        ProcessAddRequest request = new ProcessAddRequest();
        request.setCode("DUP-002");
        request.setName("新工序");
        request.setProcessType("ASSEMBLY");

        // 验证返回业务错误（HTTP 200，JSON code 400）
        mockMvc.perform(post("/api/process/add")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("工序编码已存在"));
    }

    // ========== 编辑工序接口测试 ==========

    @Test
    @DisplayName("PUT /api/process/edit 接口编辑工序成功")
    void shouldEditProcessSuccessfully() throws Exception {
        // 准备:先插入一条工序
        MesProcess process = new MesProcess();
        process.setCode("EDIT-001");
        process.setName("原始工序");
        process.setProcessType("ASSEMBLY");
        process.setEnable(1);
        processMapper.insert(process);

        // 构建编辑请求
        ProcessEditRequest request = new ProcessEditRequest();
        request.setId(process.getId());
        request.setName("更新后的工序");
        request.setProcessType("INSPECTION");
        request.setEnable(0);
        request.setDescription("更新后的描述");
        request.setRemark("更新后的备注");

        // 验证响应
        mockMvc.perform(put("/api/process/edit")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("工序不存在时编辑返回业务错误")
    void shouldReturnErrorWhenProcessNotFound() throws Exception {
        // 构建编辑请求:使用不存在的ID
        ProcessEditRequest request = new ProcessEditRequest();
        request.setId(99999L);
        request.setName("测试工序");
        request.setProcessType("ASSEMBLY");

        // 验证返回业务错误（HTTP 200，JSON code 400）
        mockMvc.perform(put("/api/process/edit")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("工序不存在"));
    }

    // ========== 删除工序接口测试 ==========

    @Test
    @DisplayName("DELETE /api/process/delete/{id} 接口删除工序成功")
    void shouldDeleteProcessSuccessfully() throws Exception {
        // 准备:先插入一条工序
        MesProcess process = new MesProcess();
        process.setCode("DEL-001");
        process.setName("待删除工序");
        process.setProcessType("ASSEMBLY");
        process.setEnable(1);
        processMapper.insert(process);

        // 验证响应
        mockMvc.perform(delete("/api/process/delete/{id}", process.getId())
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("工序不存在时删除返回业务错误")
    void shouldReturnErrorWhenDeleteProcessNotFound() throws Exception {
        // 验证返回业务错误（HTTP 200，JSON code 400）
        mockMvc.perform(delete("/api/process/delete/{id}", 99999L)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("工序不存在"));
    }

    // ========== 批量删除工序接口测试 ==========

    @Test
    @DisplayName("DELETE /api/process/batchDelete 接口批量删除工序成功")
    void shouldBatchDeleteProcessesSuccessfully() throws Exception {
        // 准备:插入多条工序
        MesProcess process1 = new MesProcess();
        process1.setCode("BATCH-DEL-001");
        process1.setName("待删除工序1");
        process1.setProcessType("ASSEMBLY");
        process1.setEnable(1);
        processMapper.insert(process1);

        MesProcess process2 = new MesProcess();
        process2.setCode("BATCH-DEL-002");
        process2.setName("待删除工序2");
        process2.setProcessType("ASSEMBLY");
        process2.setEnable(1);
        processMapper.insert(process2);

        String requestBody = "[" + process1.getId() + "," + process2.getId() + "]";

        // 验证响应
        mockMvc.perform(delete("/api/process/batchDelete")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("批量删除时部分工序不存在返回业务错误")
    void shouldReturnErrorWhenBatchDeletePartialNotFound() throws Exception {
        // 准备:插入一条工序
        MesProcess process = new MesProcess();
        process.setCode("BATCH-DEL-003");
        process.setName("存在的工序");
        process.setProcessType("ASSEMBLY");
        process.setEnable(1);
        processMapper.insert(process);

        // 构建请求:包含存在的ID和不存在的ID
        String requestBody = "[" + process.getId() + ",99999]";

        // 验证返回业务错误（HTTP 200，JSON code 400）
        mockMvc.perform(delete("/api/process/batchDelete")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("工序不存在"));
    }

    // ========== 导出工序接口测试 ==========

    @Test
    @DisplayName("GET /api/process/export 接口可访问")
    void shouldAccessExportEndpoint() throws Exception {
        mockMvc.perform(get("/api/process/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("导出接口返回正确的Content-Type")
    void shouldReturnCorrectContentType() throws Exception {
        mockMvc.perform(get("/api/process/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"));
    }

    @Test
    @DisplayName("导出接口返回正确的文件名格式")
    void shouldReturnCorrectFileNameFormat() throws Exception {
        mockMvc.perform(get("/api/process/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".csv")));
    }

    @Test
    @DisplayName("导出接口返回CSV内容包含表头")
    void shouldReturnCsvWithHeader() throws Exception {
        mockMvc.perform(get("/api/process/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("工序编码")));
    }
}