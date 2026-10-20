package com.matt.mes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matt.mes.business.dto.LineAddRequest;
import com.matt.mes.business.dto.LineEditRequest;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.entity.MesLine;
import com.matt.mes.business.mapper.LineMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LineController 集成测试（HTTP 层）
 *
 * <p>通过 MockMvc 直接打 /api/line/** 接口，覆盖 Controller → Service → Mapper → 建表 SQL 的全部行为。
 * 依赖真实 MySQL，测试结束后回滚。</p>
 *
 * <p>{@code lineMapper} 声明为 {@link SpyBean}：默认在测试结束后重置，某个用例打的 stub
 * 不会泄漏到其他用例；未被 stub 的方法仍走真实 DB。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
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

    @Test
    @DisplayName("并发窗口绕过前置查重后撞上唯一键：接口返回 400「线体编码已存在」而非 500")
    void shouldTranslateUniqueKeyConflictOnInsertToBadRequest() throws Exception {
        // 场景：两个请求同时通过前置查重，本请求在 insert 时撞上数据库唯一键。
        // 用 SpyBean 让前置查重「查不到」，迫使流程走到 insert，再由 insert 抛唯一键冲突；
        // 请求仍走 mockMvc 全链路，异常由生产代码 GlobalExceptionHandler 映射，测试不代劳。
        doReturn(0L).when(lineMapper).countByCodeIncludeDeleted("LINE-RACE-001");
        doThrow(new DuplicateKeyException("Duplicate entry for key 'uk_line_code'"))
                .when(lineMapper).insert(any(MesLine.class));

        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-RACE-001");
        request.setName("并发撞键线体");

        mockMvc.perform(post("/api/line/add")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("线体编码已存在"));
    }

    @Test
    @DisplayName("insert 抛出唯一键之外的异常不被吞掉，仍按系统异常（500）返回")
    void shouldNotSwallowNonDuplicateKeyExceptionOnInsert() throws Exception {
        doReturn(0L).when(lineMapper).countByCodeIncludeDeleted("LINE-RACE-002");
        doThrow(new IllegalStateException("db down")).when(lineMapper).insert(any(MesLine.class));

        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-RACE-002");
        request.setName("其他异常线体");

        // 非 DuplicateKeyException 必须落到兜底的系统异常分支，不能被误译成「线体编码已存在」
        mockMvc.perform(post("/api/line/add")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("系统异常，请稍后重试"));
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

    // ========== 编辑成功 ==========

    @Test
    @DisplayName("PUT /api/line/edit 编辑名称、描述与状态成功并返回被编辑的ID")
    void shouldEditLineSuccessfully() throws Exception {
        Long id = insertTestLine("LINE-EDIT-001", "旧名称", 1, 0);

        LineEditRequest request = new LineEditRequest();
        request.setId(id);
        request.setName("新名称");
        request.setDescription("新的描述");
        request.setEnable(0);

        mockMvc.perform(put("/api/line/edit")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(id));

        // 列表查询可见更新后的名称、描述与状态
        LineQueryRequest query = new LineQueryRequest();
        query.setCode("LINE-EDIT-001");

        mockMvc.perform(post("/api/line/list")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].name").value("新名称"))
                .andExpect(jsonPath("$.data.list[0].description").value("新的描述"))
                .andExpect(jsonPath("$.data.list[0].enable").value(0));
    }

    // ========== 编码不可修改 ==========

    @Test
    @DisplayName("编辑请求体携带编码字段时不会被持久化")
    void shouldIgnoreCodeWhenEditing() throws Exception {
        Long id = insertTestLine("LINE-KEEP-CODE", "原始名称", 1, 0);

        // 以原始 JSON 方式传入 code，验证该字段被忽略
        String body = "{\"id\":" + id + ",\"code\":\"LINE-HACKED\",\"name\":\"改名后\",\"enable\":1}";

        mockMvc.perform(put("/api/line/edit")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MesLine updated = lineMapper.selectById(id);
        org.junit.jupiter.api.Assertions.assertEquals("LINE-KEEP-CODE", updated.getLineCode());
        org.junit.jupiter.api.Assertions.assertEquals("改名后", updated.getLineName());
    }

    // ========== 不变更 ID 校验 ==========

    @Test
    @DisplayName("编辑不存在的 ID 返回 400 与明确提示，不抛系统异常")
    void shouldRejectEditOfMissingId() throws Exception {
        LineEditRequest request = new LineEditRequest();
        request.setId(-1L);
        request.setName("不存在的线体");
        request.setEnable(1);

        mockMvc.perform(put("/api/line/edit")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.message").value("线体不存在"));
    }

    // ========== 状态切换 ==========

    @Test
    @DisplayName("PUT /api/line/status/{id} 目标状态为 1 时置为启用并返回该ID")
    void shouldEnableLine() throws Exception {
        Long lineId = insertTestLine("LINE-STATUS-ENABLE", "待启用线体", 0, 0);

        mockMvc.perform(put("/api/line/status/{id}", lineId)
                        .header("Authorization", AUTH)
                        .param("enable", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(lineId));

        org.junit.jupiter.api.Assertions.assertEquals(1, lineMapper.selectById(lineId).getEnable());
    }

    @Test
    @DisplayName("PUT /api/line/status/{id} 目标状态为 0 时置为禁用并返回该ID")
    void shouldDisableLine() throws Exception {
        Long lineId = insertTestLine("LINE-STATUS-DISABLE", "待禁用线体", 1, 0);

        mockMvc.perform(put("/api/line/status/{id}", lineId)
                        .header("Authorization", AUTH)
                        .param("enable", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(lineId));

        org.junit.jupiter.api.Assertions.assertEquals(0, lineMapper.selectById(lineId).getEnable());
    }

    @Test
    @DisplayName("PUT /api/line/status/{id} 目标状态为 0/1 之外的值返回 400")
    void shouldRejectInvalidStatusValue() throws Exception {
        Long lineId = insertTestLine("LINE-STATUS-BAD", "非法状态线体", 1, 0);

        mockMvc.perform(put("/api/line/status/{id}", lineId)
                        .header("Authorization", AUTH)
                        .param("enable", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("启用状态值无效"));

        // 校验失败不得修改原状态
        org.junit.jupiter.api.Assertions.assertEquals(1, lineMapper.selectById(lineId).getEnable());
    }

    @Test
    @DisplayName("PUT /api/line/status/{id} 对不存在的 ID 返回 400")
    void shouldRejectStatusChangeForMissingId() throws Exception {
        mockMvc.perform(put("/api/line/status/{id}", 99999999L)
                        .header("Authorization", AUTH)
                        .param("enable", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("线体不存在"));
    }

    @Test
    @DisplayName("编辑时 ID 缺失返回 400 而非系统异常")
    void shouldRejectEditWithoutId() throws Exception {
        LineEditRequest request = new LineEditRequest();
        request.setName("缺少ID");
        request.setEnable(1);

        mockMvc.perform(put("/api/line/edit")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("线体ID不能为空"));
    }

    // ========== 编辑校验 ==========

    @Test
    @DisplayName("编辑时名称为空返回 400")
    void shouldRejectBlankNameWhenEditing() throws Exception {
        Long id = insertTestLine("LINE-EDIT-NO-NAME", "原名称", 1, 0);

        LineEditRequest request = new LineEditRequest();
        request.setId(id);
        request.setName("");

        expectEditRejected(request, "线体名称不能为空");
    }

    @Test
    @DisplayName("编辑时名称为纯空白返回 400")
    void shouldRejectWhitespaceNameWhenEditing() throws Exception {
        Long id = insertTestLine("LINE-EDIT-BLANK-NAME", "原名称", 1, 0);

        LineEditRequest request = new LineEditRequest();
        request.setId(id);
        request.setName("  \t ");

        expectEditRejected(request, "线体名称不能为空");
    }

    @Test
    @DisplayName("编辑时名称长度超过 100 返回 400")
    void shouldRejectTooLongNameWhenEditing() throws Exception {
        Long id = insertTestLine("LINE-EDIT-LONG-NAME", "原名称", 1, 0);

        LineEditRequest request = new LineEditRequest();
        request.setId(id);
        request.setName("名".repeat(101));

        expectEditRejected(request, "线体名称长度不能超过100个字符");
    }

    @Test
    @DisplayName("编辑时状态值非法返回 400")
    void shouldRejectInvalidEnableWhenEditing() throws Exception {
        Long id = insertTestLine("LINE-EDIT-BAD-ENABLE", "原名称", 1, 0);

        LineEditRequest request = new LineEditRequest();
        request.setId(id);
        request.setName("合法名称");
        request.setEnable(2);

        expectEditRejected(request, "启用状态值无效");
    }

    @Test
    @DisplayName("编辑校验失败时原记录保持不变")
    void shouldKeepRecordUnchangedWhenEditRejected() throws Exception {
        Long id = insertTestLine("LINE-EDIT-UNCHANGED", "原名称", 1, 0);

        LineEditRequest request = new LineEditRequest();
        request.setId(id);
        request.setName("");

        expectEditRejected(request, "线体名称不能为空");

        MesLine unchanged = lineMapper.selectById(id);
        org.junit.jupiter.api.Assertions.assertEquals("原名称", unchanged.getLineName());
        org.junit.jupiter.api.Assertions.assertEquals(1, unchanged.getEnable());
    }

    // ========== 单条删除 ==========

    @Test
    @DisplayName("DELETE /api/line/delete/{id} 返回被删除的 ID，且列表查询不再返回该记录")
    void shouldDeleteLineAndHideFromList() throws Exception {
        Long id = insertTestLineReturningId("LINE-DEL-001", "待删除线体");

        // 删除前列表可见
        mockMvc.perform(post("/api/line/list")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"LINE-DEL-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)));

        mockMvc.perform(delete("/api/line/delete/{id}", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(id));

        // 删除后列表查不到
        mockMvc.perform(post("/api/line/list")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"LINE-DEL-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(0)))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("删除后数据仍在库中（逻辑删除，deleted=1）")
    void shouldLogicallyDeleteRecord() throws Exception {
        Long id = insertTestLineReturningId("LINE-DEL-LOGIC", "逻辑删除线体");

        mockMvc.perform(delete("/api/line/delete/{id}", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(id));

        // selectById 受 @TableLogic 过滤，看原始行需绕开；用查重计数（包含已删除）证明数据仍在
        org.junit.jupiter.api.Assertions.assertEquals(1L, lineMapper.countByCodeIncludeDeleted("LINE-DEL-LOGIC"));
    }

    @Test
    @DisplayName("删除不存在的 ID 返回 400")
    void shouldRejectDeleteOfMissingId() throws Exception {
        mockMvc.perform(delete("/api/line/delete/{id}", 99999999L)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("线体不存在"));
    }

    @Test
    @DisplayName("重复删除同一条记录第二次返回 400")
    void shouldRejectSecondDeleteOfSameId() throws Exception {
        Long id = insertTestLineReturningId("LINE-DEL-TWICE", "重复删除线体");

        mockMvc.perform(delete("/api/line/delete/{id}", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/line/delete/{id}", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("线体不存在"));
    }

    @Test
    @DisplayName("删除不受任何关联关系阻止：无关联数据时直接成功")
    void shouldDeleteWithoutRelationChecks() throws Exception {
        Long id = insertTestLineReturningId("LINE-DEL-FREE", "无关联线体");

        mockMvc.perform(delete("/api/line/delete/{id}", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(id));
    }

    @Test
    @DisplayName("删除后同编码仍占用唯一索引：再用该编码新增返回「线体编码已存在」")
    void shouldKeepCodeReservedAfterDelete() throws Exception {
        Long id = insertTestLineReturningId("LINE-DEL-REUSE", "删除后复用编码");

        mockMvc.perform(delete("/api/line/delete/{id}", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        LineAddRequest request = new LineAddRequest();
        request.setCode("LINE-DEL-REUSE");
        request.setName("复用已删除编码");

        expectAddRejected(request, "线体编码已存在");
    }

    // ========== 批量删除 ==========

    @Test
    @DisplayName("DELETE /api/line/batchDelete 批量逻辑删除后列表与总数同步刷新")
    void shouldBatchDeleteAndRefreshListAndTotal() throws Exception {
        Long id1 = insertTestLineReturningId("LINE-BATCH-001", "批量一线体");
        Long id2 = insertTestLineReturningId("LINE-BATCH-002", "批量二线体");
        insertTestLineReturningId("LINE-BATCH-KEEP", "批量保留线体");

        // 删除前总数
        mockMvc.perform(post("/api/line/list")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"LINE-BATCH-\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3));

        mockMvc.perform(delete("/api/line/batchDelete")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Arrays.asList(id1, id2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data", containsInAnyOrder(id1.intValue(), id2.intValue())));

        // 列表与总数同步刷新
        mockMvc.perform(post("/api/line/list")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"LINE-BATCH-\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].code").value("LINE-BATCH-KEEP"));
    }

    @Test
    @DisplayName("批量删除传入空数组返回 400")
    void shouldRejectEmptyBatchDelete() throws Exception {
        mockMvc.perform(delete("/api/line/batchDelete")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("删除ID列表不能为空"));
    }

    @Test
    @DisplayName("批量删除中存在不存在 ID 时返回 400 且不产生部分删除")
    void shouldRejectBatchDeleteWithMissingIdWithoutPartialDelete() throws Exception {
        Long existingId = insertTestLineReturningId("LINE-BATCH-PARTIAL", "部分删除线体");

        mockMvc.perform(delete("/api/line/batchDelete")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Arrays.asList(existingId, 99999999L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("线体不存在"));

        // 合法的那一条仍未被删除
        mockMvc.perform(post("/api/line/list")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"LINE-BATCH-PARTIAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)));
    }

    // ========== 导出 CSV ==========

    @Test
    @DisplayName("GET /api/line/export 响应体以 UTF-8 BOM 开头，表头为 7 列")
    void shouldExportCsvWithBomAndSevenColumnHeader() throws Exception {
        insertTestLine("LINE-EXP-BOM", "导出线体", 1, 0);

        byte[] body = mockMvc.perform(get("/api/line/export")
                        .header("Authorization", AUTH)
                        .param("code", "LINE-EXP-BOM"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        // 前三个字节必须是 UTF-8 BOM (EF BB BF)
        org.junit.jupiter.api.Assertions.assertTrue(body.length > 3);
        org.junit.jupiter.api.Assertions.assertEquals((byte) 0xEF, body[0]);
        org.junit.jupiter.api.Assertions.assertEquals((byte) 0xBB, body[1]);
        org.junit.jupiter.api.Assertions.assertEquals((byte) 0xBF, body[2]);

        String csv = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        String header = csv.substring(1).split("\r\n", -1)[0];
        org.junit.jupiter.api.Assertions.assertEquals(
                "ID,线体编码,线体名称,描述,启用状态,创建人,创建时间", header);
    }

    @Test
    @DisplayName("GET /api/line/export 返回 text/csv 内容类型与 attachment 文件名")
    void shouldExportWithCsvContentTypeAndAttachmentFileName() throws Exception {
        mockMvc.perform(get("/api/line/export")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Type", containsString("UTF-8")))
                .andExpect(header().string("Content-Disposition", startsWith("attachment;")));
    }

    @Test
    @DisplayName("GET /api/line/export 描述含逗号、双引号或换行时正确转义，解析后仍为 7 列")
    void shouldEscapeSpecialCharactersInExportedDescription() throws Exception {
        String description = "含逗号,含\"双引号\"，含换行\n第二行";
        MesLine line = new MesLine();
        line.setLineCode("LINE-EXP-ESC");
        line.setLineName("转义线体");
        line.setDescription(description);
        line.setEnable(0);
        lineMapper.insert(line);

        byte[] body = mockMvc.perform(get("/api/line/export")
                        .header("Authorization", AUTH)
                        .param("code", "LINE-EXP-ESC"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        String csv = new String(body, java.nio.charset.StandardCharsets.UTF_8).substring(1);
        // 原始文本中描述须整体被双引号包裹，内部双引号成对转义
        org.junit.jupiter.api.Assertions.assertTrue(
                csv.contains("\"含逗号,含\"\"双引号\"\"，含换行\n第二行\""));

        // 逐字符解析后列数仍为 7，且描述原样还原
        java.util.List<java.util.List<String>> records = parseCsv(csv);
        org.junit.jupiter.api.Assertions.assertEquals(2, records.size());
        java.util.List<String> row = records.get(1);
        org.junit.jupiter.api.Assertions.assertEquals(7, row.size());
        org.junit.jupiter.api.Assertions.assertEquals(description, row.get(3));
        org.junit.jupiter.api.Assertions.assertEquals("0", row.get(4));
    }

    @Test
    @DisplayName("GET /api/line/export 导出全部匹配记录，不受分页参数影响")
    void shouldExportAllMatchingRecordsIgnoringPaging() throws Exception {
        for (int i = 1; i <= 11; i++) {
            insertTestLine(String.format("LINE-EXP-ALL-%02d", i), "全量线体" + i, 1, 0);
        }

        byte[] body = mockMvc.perform(get("/api/line/export")
                        .header("Authorization", AUTH)
                        .param("code", "LINE-EXP-ALL-")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        String csv = new String(body, java.nio.charset.StandardCharsets.UTF_8).substring(1);
        org.junit.jupiter.api.Assertions.assertEquals(12, csv.split("\r\n").length);
    }

    // ========== 辅助方法 ==========

    /**
     * 按 RFC 4180 解析 CSV（支持引号包裹的逗号与换行）
     */
    private java.util.List<java.util.List<String>> parseCsv(String csv) {
        java.util.List<java.util.List<String>> records = new java.util.ArrayList<>();
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < csv.length(); i++) {
            char c = csv.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (c == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                fields.add(field.toString());
                field.setLength(0);
                records.add(fields);
                fields = new java.util.ArrayList<>();
                i++;
            } else if (c == '\n') {
                fields.add(field.toString());
                field.setLength(0);
                records.add(fields);
                fields = new java.util.ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (field.length() > 0 || !fields.isEmpty()) {
            fields.add(field.toString());
            records.add(fields);
        }
        return records;
    }


    /**
     * 断言编辑请求被业务校验拒绝：HTTP 200 + JSON code 400 + 指定提示
     */
    private void expectEditRejected(LineEditRequest request, String expectedMessage) throws Exception {
        mockMvc.perform(put("/api/line/edit")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

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
     * 辅助方法：插入测试线体并返回生成的主键
     */
    private Long insertTestLineReturningId(String code, String name) {
        MesLine line = new MesLine();
        line.setLineCode(code);
        line.setLineName(name);
        line.setEnable(1);
        line.setDeleted(0);
        lineMapper.insert(line);
        return line.getId();
    }

    /**
     * 辅助方法：插入测试线体，返回新记录ID
     */
    private Long insertTestLine(String code, String name, Integer enable, Integer deleted) {
        MesLine line = new MesLine();
        line.setLineCode(code);
        line.setLineName(name);
        line.setEnable(enable);
        line.setDeleted(deleted);
        lineMapper.insert(line);
        return line.getId();
    }
}
