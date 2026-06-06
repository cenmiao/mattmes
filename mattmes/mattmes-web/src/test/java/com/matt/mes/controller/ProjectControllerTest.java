package com.matt.mes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matt.mes.business.dto.*;
import com.matt.mes.business.mapper.ProjectMapper;
import com.matt.mes.business.entity.MesProject;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController集成测试
 * 测试项目管理接口行为
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectMapper projectMapper;

    // 永久测试token
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyTm8iOiJhZG1pbiIsInVzZXJJZCI6MSwiaWF0IjoxNzgwNDg2OTEyLCJleHAiOjE4MTIwMjI5MTJ9.be7sM94QmpqKrkr3iYWMRkROzaKyb-LGZNF3SW93VPzSzjEEjJy06zbOCeOjsTGK";

    @BeforeEach
    void setUp() {
        // 准备测试数据
        insertTestProject("IPHONE17", "iPhone 17系列", 1);
        insertTestProject("IPHONE18", "iPhone 18系列", 1);
        insertTestProject("MACBOOK2024", "MacBook Pro 2024系列", 1);
    }

    // ========== 分页查询接口测试 ==========

    @Test
    @DisplayName("POST /api/project/list 接口可访问")
    void shouldAccessProjectListEndpoint() throws Exception {
        ProjectQueryRequest request = new ProjectQueryRequest();

        mockMvc.perform(post("/api/project/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("返回统一响应格式")
    void shouldReturnUnifiedResponseFormat() throws Exception {
        ProjectQueryRequest request = new ProjectQueryRequest();

        MvcResult result = mockMvc.perform(post("/api/project/list")
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
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setCode("IPHONE");
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/api/project/list")
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
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setCode("IPHONE17");

        mockMvc.perform(post("/api/project/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].code").value("IPHONE17"))
                .andExpect(jsonPath("$.data.list[0].name").value("iPhone 17系列"))
                .andExpect(jsonPath("$.data.list[0].enable").value(1));
    }

    // ========== 新增项目接口测试 ==========

    @Test
    @DisplayName("POST /api/project/add 接口新增项目成功")
    void shouldAddProjectSuccessfully() throws Exception {
        // 构建新增请求
        ProjectAddRequest request = new ProjectAddRequest();
        request.setCode("NEW-PROJECT-001");
        request.setName("新项目");
        request.setDescription("新项目描述");
        request.setRemark("新项目备注");

        // 验证响应
        mockMvc.perform(post("/api/project/add")
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
        // 准备:已存在的项目
        insertTestProject("DUP-PROJECT", "已存在项目", 1);

        // 构建新增请求:使用重复编码
        ProjectAddRequest request = new ProjectAddRequest();
        request.setCode("DUP-PROJECT");
        request.setName("新项目");

        // 验证返回业务错误（HTTP 200，JSON code 400）
        mockMvc.perform(post("/api/project/add")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("项目编码已存在"));
    }

    @Test
    @DisplayName("编码为空时返回业务错误")
    void shouldReturnErrorWhenCodeEmpty() throws Exception {
        // 构建新增请求:编码为空
        ProjectAddRequest request = new ProjectAddRequest();
        request.setCode(null);
        request.setName("测试项目");

        // 验证返回业务错误
        mockMvc.perform(post("/api/project/add")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("编码不能为空"));
    }

    @Test
    @DisplayName("名称为空时返回业务错误")
    void shouldReturnErrorWhenNameEmpty() throws Exception {
        // 构建新增请求:名称为空
        ProjectAddRequest request = new ProjectAddRequest();
        request.setCode("TEST-PROJECT");
        request.setName(null);

        // 验证返回业务错误
        mockMvc.perform(post("/api/project/add")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("名称不能为空"));
    }

    // ========== 编辑项目接口测试 ==========

    @Test
    @DisplayName("PUT /api/project/edit 接口编辑项目成功")
    void shouldEditProjectSuccessfully() throws Exception {
        // 准备:先插入一条项目
        MesProject project = new MesProject();
        project.setCode("EDIT-TEST-001");
        project.setName("原始项目");
        project.setEnable(1);
        projectMapper.insert(project);

        // 构建编辑请求
        ProjectEditRequest request = new ProjectEditRequest();
        request.setId(project.getId());
        request.setName("更新后的项目");
        request.setDescription("更新后的描述");
        request.setRemark("更新后的备注");

        // 验证响应
        mockMvc.perform(put("/api/project/edit")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("项目不存在时编辑返回业务错误")
    void shouldReturnErrorWhenProjectNotFound() throws Exception {
        // 构建编辑请求:使用不存在的ID
        ProjectEditRequest request = new ProjectEditRequest();
        request.setId(99999L);
        request.setName("测试项目");

        // 验证返回业务错误
        mockMvc.perform(put("/api/project/edit")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }

    // ========== 删除项目接口测试 ==========

    @Test
    @DisplayName("DELETE /api/project/delete/{id} 接口删除项目成功")
    void shouldDeleteProjectSuccessfully() throws Exception {
        // 准备:先插入一条项目
        MesProject project = new MesProject();
        project.setCode("DEL-TEST-001");
        project.setName("待删除项目");
        project.setEnable(1);
        projectMapper.insert(project);

        // 验证响应
        mockMvc.perform(delete("/api/project/delete/{id}", project.getId())
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("项目不存在时删除返回业务错误")
    void shouldReturnErrorWhenDeleteProjectNotFound() throws Exception {
        // 验证返回业务错误
        mockMvc.perform(delete("/api/project/delete/{id}", 99999L)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }

    // ========== 批量删除项目接口测试 ==========

    @Test
    @DisplayName("DELETE /api/project/batchDelete 接口批量删除项目成功")
    void shouldBatchDeleteProjectsSuccessfully() throws Exception {
        // 准备:插入多条项目
        MesProject project1 = new MesProject();
        project1.setCode("BATCH-DEL-001");
        project1.setName("待删除项目1");
        project1.setEnable(1);
        projectMapper.insert(project1);

        MesProject project2 = new MesProject();
        project2.setCode("BATCH-DEL-002");
        project2.setName("待删除项目2");
        project2.setEnable(1);
        projectMapper.insert(project2);

        String requestBody = "[" + project1.getId() + "," + project2.getId() + "]";

        // 验证响应
        mockMvc.perform(delete("/api/project/batchDelete")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("批量删除时部分项目不存在返回业务错误")
    void shouldReturnErrorWhenBatchDeletePartialNotFound() throws Exception {
        // 准备:插入一条项目
        MesProject project = new MesProject();
        project.setCode("BATCH-DEL-003");
        project.setName("存在的项目");
        project.setEnable(1);
        projectMapper.insert(project);

        // 构建请求:包含存在的ID和不存在的ID
        String requestBody = "[" + project.getId() + ",99999]";

        // 验证返回业务错误
        mockMvc.perform(delete("/api/project/batchDelete")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }

    // ========== 状态切换接口测试 ==========

    @Test
    @DisplayName("PUT /api/project/status/{id} 接口更新状态成功")
    void shouldUpdateStatusSuccessfully() throws Exception {
        // 准备:先插入一条启用的项目
        MesProject project = new MesProject();
        project.setCode("STATUS-TEST-001");
        project.setName("待禁用项目");
        project.setEnable(1);
        projectMapper.insert(project);

        // 验证响应
        mockMvc.perform(put("/api/project/status/{id}", project.getId())
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("enable", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("项目不存在时更新状态返回业务错误")
    void shouldReturnErrorWhenUpdateStatusProjectNotFound() throws Exception {
        // 验证返回业务错误
        mockMvc.perform(put("/api/project/status/{id}", 99999L)
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("enable", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }

    // ========== 导出项目接口测试 ==========

    @Test
    @DisplayName("GET /api/project/export 接口可访问")
    void shouldAccessExportEndpoint() throws Exception {
        mockMvc.perform(get("/api/project/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("导出接口返回正确的Content-Type")
    void shouldReturnCorrectContentType() throws Exception {
        mockMvc.perform(get("/api/project/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"));
    }

    @Test
    @DisplayName("导出接口返回正确的文件名格式")
    void shouldReturnCorrectFileNameFormat() throws Exception {
        mockMvc.perform(get("/api/project/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".csv")));
    }

    @Test
    @DisplayName("导出接口返回CSV内容包含表头")
    void shouldReturnCsvWithHeader() throws Exception {
        mockMvc.perform(get("/api/project/export")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("项目编码")));
    }

    /**
     * 辅助方法:插入测试项目
     */
    private void insertTestProject(String code, String name, Integer enable) {
        MesProject project = new MesProject();
        project.setCode(code);
        project.setName(name);
        project.setEnable(enable);
        projectMapper.insert(project);
    }
}