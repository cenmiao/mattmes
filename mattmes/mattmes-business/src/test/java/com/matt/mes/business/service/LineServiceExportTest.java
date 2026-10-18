package com.matt.mes.business.service;

import com.matt.mes.business.config.TestConfig;
import com.matt.mes.business.dto.LineQueryRequest;
import com.matt.mes.business.entity.MesLine;
import com.matt.mes.business.mapper.LineMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LineService 导出 CSV 集成测试
 *
 * <p>覆盖服务层的导出行为：表头、BOM、状态原值时、筛选条件、CSV 转义与响应头。</p>
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class LineServiceExportTest {

    @Autowired
    private LineService lineService;

    @Autowired
    private LineMapper lineMapper;

    /** UTF-8 BOM 的字符串形式（EF BB BF） */
    private static final String BOM = "\uFEFF";

    private static final String EXPECTED_HEADER = "ID,线体编码,线体名称,描述,启用状态,创建人,创建时间";

    @Test
    @DisplayName("导出响应体以 UTF-8 BOM 开头")
    void shouldStartWithUtf8Bom() throws Exception {
        insertTestLine("EXP-BOM-01", "BOM线体", null, 1);

        String csv = exportToText(queryByCodePrefix("EXP-BOM-"));

        assertTrue(csv.startsWith(BOM), "CSV 应以 UTF-8 BOM 开头");
    }

    @Test
    @DisplayName("导出表头为 7 列：ID、线体编码、线体名称、描述、启用状态、创建人、创建时间")
    void shouldWriteSevenColumnHeader() throws Exception {
        insertTestLine("EXP-HEAD-01", "表头线体", null, 1);

        String csv = exportToText(queryByCodePrefix("EXP-HEAD-"));
        String firstLine = stripBom(csv).split("\r\n", -1)[0];

        assertEquals(EXPECTED_HEADER, firstLine);
        assertEquals(7, firstLine.split(",", -1).length);
    }

    @Test
    @DisplayName("导出内容类型为 text/csv 且字符集为 UTF-8")
    void shouldSetCsvContentType() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        lineService.export(queryByCodePrefix("EXP-NONE-"), response);

        assertTrue(response.getContentType().startsWith("text/csv"), "内容类型应为 text/csv");
        assertTrue(response.getContentType().toUpperCase().contains("UTF-8"), "字符集应为 UTF-8");
    }

    @Test
    @DisplayName("导出响应头含 attachment 形式的文件名且带时间戳")
    void shouldSetAttachmentFileNameWithTimestamp() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        lineService.export(queryByCodePrefix("EXP-NONE-"), response);

        String disposition = response.getHeader("Content-Disposition");
        assertNotNull(disposition, "应包含 Content-Disposition 响应头");
        assertTrue(disposition.startsWith("attachment;"), "应为 attachment 形式");
        assertTrue(disposition.contains(".csv"), "文件名应以 .csv 结尾");
        assertTrue(disposition.matches(".*\\d{8}_\\d{6}.*"), "文件名应带 yyyyMMdd_HHmmss 时间戳");
    }

    @Test
    @DisplayName("状态列导出原始值 1 或 0，不转换为中文")
    void shouldExportRawEnableValue() throws Exception {
        insertTestLine("EXP-RAW-01", "启用线体", null, 1);
        insertTestLine("EXP-RAW-02", "禁用线体", null, 0);

        String csv = stripBom(exportToText(queryByCodePrefix("EXP-RAW-")));
        List<String> lines = List.of(csv.split("\r\n"));

        String enabledRow = lines.stream().filter(l -> l.contains("EXP-RAW-01")).findFirst().orElseThrow();
        String disabledRow = lines.stream().filter(l -> l.contains("EXP-RAW-02")).findFirst().orElseThrow();

        assertEquals("1", splitCsvRecord(enabledRow).get(4));
        assertEquals("0", splitCsvRecord(disabledRow).get(4));
        assertFalse(csv.contains("启用线体,启用,"), "状态列不应输出中文");
    }

    @Test
    @DisplayName("描述含逗号、双引号或换行时按 CSV 规则包裹与转义，解析后列数一致")
    void shouldEscapeDescriptionWithSpecialCharacters() throws Exception {
        String trickyDescription = "含逗号,含双引号\"x\"，还含换行\n第二行";
        insertTestLine("EXP-ESC-01", "转义线体", trickyDescription, 1);

        String csv = stripBom(exportToText(queryByCodePrefix("EXP-ESC-")));

        // 解析 CSV 记录（按引号状态聚合物理行）
        List<List<String>> records = parseCsv(csv);
        assertEquals(2, records.size(), "应包含表头与一条数据");
        List<String> row = records.get(1);
        assertEquals(7, row.size(), "含特殊字符的描述解析后仍为 7 列");
        assertEquals("EXP-ESC-01", row.get(1));
        assertEquals(trickyDescription, row.get(3));
    }

    @Test
    @DisplayName("导出严格遵循筛选条件，只包含编码匹配的数据")
    void shouldRespectQueryFilters() throws Exception {
        insertTestLine("EXP-FILTER-ASM", "筛选线体A", null, 1);
        insertTestLine("EXP-FILTER-OTHER", "筛选线体B", null, 1);

        String csv = stripBom(exportToText(queryByCodePrefix("EXP-FILTER-ASM")));
        List<String> lines = List.of(csv.split("\r\n"));

        assertEquals(2, lines.size(), "表头 + 1 条匹配数据");
        assertTrue(lines.get(1).contains("EXP-FILTER-ASM"));
    }

    @Test
    @DisplayName("导出一次性包含全部匹配记录，不受列表分页限制")
    void shouldExportAllMatchingRecordsNotJustCurrentPage() throws Exception {
        for (int i = 1; i <= 12; i++) {
            insertTestLine(String.format("EXP-ALL-%02d", i), "全量线体" + i, null, 1);
        }

        LineQueryRequest request = queryByCodePrefix("EXP-ALL-");
        request.setPageNum(1);
        request.setPageSize(10);

        String csv = stripBom(exportToText(request));
        List<String> lines = List.of(csv.split("\r\n"));

        assertEquals(13, lines.size(), "12 条数据 + 1 行表头，导出不应受 pageSize 限制");
    }

    // ========== 辅助方法 ==========

    private LineQueryRequest queryByCodePrefix(String code) {
        LineQueryRequest request = new LineQueryRequest();
        request.setCode(code);
        return request;
    }

    private String exportToText(LineQueryRequest request) {
        MockHttpServletResponse response = new MockHttpServletResponse();
        lineService.export(request, response);
        return new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private String stripBom(String text) {
        return text.startsWith(BOM) ? text.substring(1) : text;
    }

    private Long insertTestLine(String code, String name, String description, Integer enable) {
        MesLine line = new MesLine();
        line.setLineCode(code);
        line.setLineName(name);
        line.setDescription(description);
        line.setEnable(enable);
        line.setCreatedBy("tester");
        line.setCreateTime(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        lineMapper.insert(line);
        return line.getId();
    }

    /**
     * 按 RFC 4180 解析整段 CSV（支持引号包裹的逗号与换行）
     */
    private List<List<String>> parseCsv(String csv) {
        List<List<String>> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
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
                fields = new ArrayList<>();
                i++;
            } else if (c == '\n') {
                fields.add(field.toString());
                field.setLength(0);
                records.add(fields);
                fields = new ArrayList<>();
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

    private List<String> splitCsvRecord(String record) {
        return parseCsv(record + "\r\n").get(0);
    }
}
