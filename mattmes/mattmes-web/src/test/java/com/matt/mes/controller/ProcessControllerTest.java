package com.matt.mes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
}
