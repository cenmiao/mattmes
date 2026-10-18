package com.matt.mes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.entity.MesLine;
import com.matt.mes.business.mapper.LineMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LineController 集成测试（HTTP 层）
 *
 * <p>通过 MockMvc 直接打 /api/line/** 接口，覆盖 Controller → Service → Mapper → 建表 SQL 的全部行为。
 * 依赖真实 MySQL，测试结束后回滚。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LineMapper lineMapper;

    /** 永久测试 token */
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyTm8iOiJhZG1pbiIsInVzZXJJZCI6MSwiaWF0IjoxNzgwNDg2OTEyLCJleHAiOjE4MTIwMjI5MTJ9.be7sM94QmpqKrkr3iYWMRkROzaKyb-LGZNF3SW93VPzSzjEEjJy06zbOCeOjsTGK";

    private static final String AUTH = "Bearer " + TEST_TOKEN;

    // ========== 新增成功 ==========

    @Test
    @DisplayName("POST /api/line/add 新增成功并返回新记录ID")
    void shouldAddLineSuccessfully() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-ADD-001");
        request.setName("一号线体");
        request.setDescription("总装一段");
        request.setEnable(0);

        mockMvc.perform(post("/api/line/add")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());

        // 新增成功后列表查询可见该记录
        LineQueryRequest query = new LineQueryRequest();
        query.setCode("LINE-ADD-001");

        mockMvc.perform(post("/api/line/list")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].code").value("LINE-ADD-001"))
                .andExpect(jsonPath("$.data.list[0].name").value("一号线体"))
                .andExpect(jsonPath("$.data.list[0].description").value("总装一段"))
                .andExpect(jsonPath("$.data.list[0].enable").value(0));
    }

    @Test
    @DisplayName("未传状态时默认为启用")
    void shouldDefaultEnableToEnabled() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-DEFAULT-ENABLE");
        request.setName("默认状态线体");

        String response = mockMvc.perform(post("/api/line/add")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("data").asLong();
        MesLine saved = lineMapper.selectById(id);
        org.junit.jupiter.api.Assertions.assertNotNull(saved);
        org.junit.jupiter.api.Assertions.assertEquals(1, saved.getEnable());
    }

    // ========== 必填校验 ==========

    @Test
    @DisplayName("编码为空返回 400")
    void shouldRejectBlankCode() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("");
        request.setName("有线无码");

        expectAddRejected(request, "线体编码不能为空");
    }

    @Test
    @DisplayName("编码为纯空白返回 400")
    void shouldRejectWhitespaceCode() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("   ");
        request.setName("有线无码");

        expectAddRejected(request, "线体编码不能为空");
    }

    @Test
    @DisplayName("编码缺失返回 400")
    void shouldRejectMissingCode() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setName("有线无码");

        expectAddRejected(request, "线体编码不能为空");
    }

    @Test
    @DisplayName("名称为空返回 400")
    void shouldRejectBlankName() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-NO-NAME");
        request.setName("");

        expectAddRejected(request, "线体名称不能为空");
    }

    @Test
    @DisplayName("名称为纯空白返回 400")
    void shouldRejectWhitespaceName() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-NO-NAME");
        request.setName("  \t ");

        expectAddRejected(request, "线体名称不能为空");
    }

    @Test
    @DisplayName("名称缺失返回 400")
    void shouldRejectMissingName() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-NO-NAME");

        expectAddRejected(request, "线体名称不能为空");
    }

    // ========== 长度校验 ==========

    @Test
    @DisplayName("编码长度超过 50 返回 400")
    void shouldRejectTooLongCode() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("A".repeat(51));
        request.setName("超长编码线体");

        expectAddRejected(request, "线体编码长度不能超过50个字符");
    }

    @Test
    @DisplayName("编码长度恰好 50 允许新增")
    void shouldAcceptCodeWithExactly50Chars() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("A".repeat(50));
        request.setName("边界编码线体");

        mockMvc.perform(post("/api/line/add")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("名称长度超过 100 返回 400")
    void shouldRejectTooLongName() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-LONG-NAME");
        request.setName("名".repeat(101));

        expectAddRejected(request, "线体名称长度不能超过100个字符");
    }

    // ========== 编码格式校验 ==========

    @Test
    @DisplayName("编码含非法字符返回 400")
    void shouldRejectInvalidCodeFormat() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE 001#");
        request.setName("格式非法线体");

        expectAddRejected(request, "线体编码只能包含字母、数字、下划线和中划线");
    }

    @Test
    @DisplayName("编码含中文返回 400")
    void shouldRejectChineseCode() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("线体001");
        request.setName("中文编码线体");

        expectAddRejected(request, "线体编码只能包含字母、数字、下划线和中划线");
    }

    @Test
    @DisplayName("编码含字母数字下划线中划线允许新增")
    void shouldAcceptCodeWithAllowedCharacters() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("Line_01-A");
        request.setName("合法字符线体");

        mockMvc.perform(post("/api/line/add")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ========== 唯一性校验 ==========

    @Test
    @DisplayName("编码与未删除记录重复返回 400 并提示线体编码已存在")
    void shouldRejectDuplicateCodeOfActiveRecord() throws Exception {
        insertTestLine("LINE-DUP-001", "已存在线体", 1, 0);

        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-DUP-001");
        request.setName("重复编码线体");

        expectAddRejected(request, "线体编码已存在");
    }

    @Test
    @DisplayName("编码与已删除记录重复同样返回 400，不落入数据库唯一键异常")
    void shouldRejectDuplicateCodeOfDeletedRecord() throws Exception {
        // 准备：已逻辑删除的同编码记录
        insertTestLine("LINE-DUP-DELETED", "已删除线体", 1, 1);

        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-DUP-DELETED");
        request.setName("复用已删除编码");

        expectAddRejected(request, "线体编码已存在");
    }

    // ========== 状态值校验 ==========

    @Test
    @DisplayName("状态值非法返回 400")
    void shouldRejectInvalidEnableValue() throws Exception {
        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-BAD-ENABLE");
        request.setName("非法状态线体");
        request.setEnable(2);

        expectAddRejected(request, "启用状态值无效");
    }

    // ========== 辅助方法 ==========

    /**
     * 断言新增请求被业务校验拒绝：HTTP 200 + JSON code 400 + 指定提示
     */
    private void expectAddRejected(LineAddRequest request, String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/line/add")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    /**
     * 辅助方法：插入测试线体
     */
    private void insertTestLine(String code, String name, Integer enable, Integer deleted) {
        MesLine line = new MesLine();
        line.setLineCode(code);
        line.setLineName(name);
        line.setEnable(enable);
        line.setDeleted(deleted);
        lineMapper.insert(line);
    }
}
