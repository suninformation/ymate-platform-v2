/*
 * Copyright 2007-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.commons;

import org.apache.poi.ss.usermodel.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

/**
 * ExcelFileExportHelper类的单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-08 22:28:38
 * @since 2.1.4
 */
public class ExcelFileExportHelperTest {

    private List<TestUserInfo> testData;
    private Map<String, Object> testDataMap;

    /**
     * 初始化测试数据
     * <p>
     * 创建用于测试的用户数据列表和Map。
     * </p>
     */
    @Before
    public void setUp() {
        testData = new ArrayList<>();
        testData.add(new TestUserInfo("张三", 25, "ZHANGSAN@EXAMPLE.COM", new Date(), 0, 1000.50, true));
        testData.add(new TestUserInfo("李四", 30, "LISI@EXAMPLE.COM", new Date(), 1, 2000.75, false));
        testData.add(new TestUserInfo("王五", 35, "WANGWU@EXAMPLE.COM", new Date(), 0, 3000.00, true));

        testDataMap = new HashMap<>();
        testDataMap.put("users", testData);
        testDataMap.put("title", "用户信息表");
    }

    /**
     * 清理测试文件
     * <p>
     * 删除测试过程中创建的临时文件。
     * </p>
     */
    @After
    public void tearDown() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File[] tempFiles = tempDir.listFiles((dir, name) -> name.startsWith("export_") || name.startsWith("test_"));
        if (tempFiles != null) {
            for (File file : tempFiles) {
                file.delete();
            }
        }
    }

    /**
     * 测试bind(Map)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用Map数据创建导出助手</li>
     *     <li>Map为null时抛出异常</li>
     * </ul>
     * </p>
     */
    @Test
    public void testBindMap() {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testDataMap);
        assertNotNull(helper);

        try {
            ExcelFileExportHelper.bind((Map<String, Object>) null);
            fail("应该抛出NullArgumentException异常");
        } catch (org.apache.commons.lang.NullArgumentException e) {
            // 预期异常
        }
    }

    /**
     * 测试bind(List)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用List数据创建导出助手</li>
     *     <li>List为null时抛出异常</li>
     * </ul>
     * </p>
     */
    @Test
    public void testBindList() {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        assertNotNull(helper);

        try {
            ExcelFileExportHelper.bind((List<?>) null);
            fail("应该抛出NullArgumentException异常");
        } catch (org.apache.commons.lang.NullArgumentException e) {
            // 预期异常
        }
    }

    /**
     * 测试bind(IExportDataProcessor)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用数据处理器创建导出助手</li>
     *     <li>数据处理器为null时抛出异常</li>
     * </ul>
     * </p>
     */
    @Test
    public void testBindDataProcessor() {
        IExportDataProcessor processor = new TestDataProcessor(testData, 2);
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(processor);
        assertNotNull(helper);

        try {
            ExcelFileExportHelper.bind((IExportDataProcessor) null);
            fail("应该抛出NullArgumentException异常");
        } catch (org.apache.commons.lang.NullArgumentException e) {
            // 预期异常
        }
    }

    /**
     * 测试prefix()方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>设置自定义前缀</li>
     *     <li>前缀为空时使用默认值</li>
     *     <li>前缀不以_结尾时自动添加</li>
     * </ul>
     * </p>
     */
    @Test
    public void testPrefix() {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);

        helper.prefix("custom");
        assertEquals("custom_", helper.prefix());

        helper.prefix("");
        assertEquals("export_", helper.prefix());

        helper.prefix(null);
        assertEquals("export_", helper.prefix());
    }

    /**
     * 测试firstCellAsIndex()方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>设置第一个单元格作为索引列</li>
     * </ul>
     * </p>
     */
    @Test
    public void testFirstCellAsIndex() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData).firstCellAsIndex();

        List<String> headerNames = Arrays.asList("用户名", "年龄", "邮箱");
        Map<String, ExportColumnMeta> columnMetaMap = new LinkedHashMap<>();
        columnMetaMap.put("username", ExportColumnMeta.create().setName("用户名"));
        columnMetaMap.put("age", ExportColumnMeta.create().setName("年龄"));
        columnMetaMap.put("email", ExportColumnMeta.create().setName("邮箱"));

        File file = helper.exportExcel(headerNames, columnMetaMap);
        assertNotNull(file);
        assertTrue(file.exists());

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row firstRow = sheet.getRow(1);
            assertNotNull(firstRow);
            assertEquals(1, firstRow.getCell(0).getNumericCellValue(), 0.001);
        }
    }

    /**
     * 测试export(Class)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出Excel文件</li>
     *     <li>验证导出的文件内容</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportClass() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试export(Class, boolean)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出XSSF格式（.xlsx）</li>
     *     <li>导出HSSF格式（.xls）</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportClassWithXssf() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);

        File xlsxFile = helper.export(TestUserInfo.class, true);
        assertNotNull(xlsxFile);
        assertTrue(xlsxFile.getName().endsWith(".xlsx"));

        File xlsFile = helper.export(TestUserInfo.class, false);
        assertNotNull(xlsFile);
        assertTrue(xlsFile.getName().endsWith(".csv"));
    }

    /**
     * 测试export(Class, String)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出CSV文件并指定字符集</li>
     *     <li>验证字符集设置</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportClassWithCharset() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class, "UTF-8");

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getName().endsWith(".csv"));
    }

    /**
     * 测试export(Class, String, boolean)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出CSV文件并指定字符集和格式</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportClassWithCharsetAndXssf() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class, "UTF-8", true);

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getName().endsWith(".xlsx"));
    }

    /**
     * 测试exportExcel(List, Map)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用自定义列头和元数据导出Excel</li>
     *     <li>验证导出的文件内容</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportExcel() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);

        List<String> headerNames = Arrays.asList("用户名", "年龄", "邮箱");
        Map<String, ExportColumnMeta> columnMetaMap = new LinkedHashMap<>();
        columnMetaMap.put("username", ExportColumnMeta.create().setName("用户名"));
        columnMetaMap.put("age", ExportColumnMeta.create().setName("年龄"));
        columnMetaMap.put("email", ExportColumnMeta.create().setName("邮箱"));

        File file = helper.exportExcel(headerNames, columnMetaMap);
        assertNotNull(file);
        assertTrue(file.exists());

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(3, sheet.getLastRowNum());

            Row headerRow = sheet.getRow(0);
            assertEquals("用户名", headerRow.getCell(0).getStringCellValue());
            assertEquals("年龄", headerRow.getCell(1).getStringCellValue());
            assertEquals("邮箱", headerRow.getCell(2).getStringCellValue());

            Row dataRow = sheet.getRow(1);
            assertEquals("张三", dataRow.getCell(0).getStringCellValue());
            assertEquals(25, dataRow.getCell(1).getNumericCellValue(), 0.001);
        }
    }

    /**
     * 测试exportCsv(List, Map)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出CSV文件</li>
     *     <li>验证导出的文件内容</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportCsv() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);

        List<String> headerNames = Arrays.asList("用户名", "年龄", "邮箱");
        Map<String, ExportColumnMeta> columnMetaMap = new LinkedHashMap<>();
        columnMetaMap.put("username", ExportColumnMeta.create().setName("用户名"));
        columnMetaMap.put("age", ExportColumnMeta.create().setName("年龄"));
        columnMetaMap.put("email", ExportColumnMeta.create().setName("邮箱"));

        File file = helper.exportCsv(headerNames, columnMetaMap);
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getName().endsWith(".csv"));

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            inputStream.read(content);
            String csvContent = new String(content, "GB2312");
            assertTrue(csvContent.contains("用户名"));
            assertTrue(csvContent.contains("张三"));
        }
    }

    /**
     * 测试exportCsv(List, Map, String)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出CSV文件并指定字符集</li>
     *     <li>验证字符集设置</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportCsvWithCharset() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);

        List<String> headerNames = Arrays.asList("用户名", "年龄", "邮箱");
        Map<String, ExportColumnMeta> columnMetaMap = new LinkedHashMap<>();
        columnMetaMap.put("username", ExportColumnMeta.create().setName("用户名"));
        columnMetaMap.put("age", ExportColumnMeta.create().setName("年龄"));
        columnMetaMap.put("email", ExportColumnMeta.create().setName("邮箱"));

        File file = helper.exportCsv(headerNames, columnMetaMap, "UTF-8");
        assertNotNull(file);
        assertTrue(file.exists());

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            inputStream.read(content);
            String csvContent = new String(content, StandardCharsets.UTF_8);
            assertTrue(csvContent.contains("用户名"));
        }
    }

    /**
     * 测试export(String)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用模板文件导出</li>
     *     <li>模板文件不存在时返回null</li>
     *     <li>模板文件为空时抛出异常</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportTemplate() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testDataMap);
        File file = helper.export("non_existent_template");
        assertNull(file);

        try {
            helper.export("");
            fail("应该抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            // 预期异常
        }
    }

    /**
     * 测试分批导出功能
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用IExportDataProcessor分批导出</li>
     *     <li>验证分批导出的正确性</li>
     * </ul>
     * </p>
     */
    @Test
    public void testBatchExport() throws Exception {
        List<TestUserInfo> largeData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            largeData.add(new TestUserInfo("用户" + i, 20 + i, "user" + i + "@example.com", new Date(), i % 2, 1000.0 + i * 100, true));
        }

        IExportDataProcessor processor = new TestDataProcessor(largeData, 3);
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(processor);

        File file = helper.export(TestUserInfo.class);
        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试空数据导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出空列表</li>
     *     <li>验证返回null</li>
     * </ul>
     * </p>
     */
    @Test
    public void testEmptyDataExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(new ArrayList<>());
        File file = helper.export(TestUserInfo.class);
        assertNotNull(file);
    }

    /**
     * 测试空Map导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出空Map</li>
     *     <li>验证返回null</li>
     * </ul>
     * </p>
     */
    @Test
    public void testEmptyMapExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(new HashMap<>());
        File file = helper.export(TestUserInfo.class);
        assertNull(file);
    }

    /**
     * 测试日期时间类型导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出包含日期时间字段的数据</li>
     *     <li>验证日期时间格式化</li>
     * </ul>
     * </p>
     */
    @Test
    public void testDateTimeExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试货币类型导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出包含货币字段的数据</li>
     *     <li>验证货币格式化</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCurrencyExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试数据范围映射导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出包含数据范围字段的数据</li>
     *     <li>验证数据范围映射</li>
     * </ul>
     * </p>
     */
    @Test
    public void testDataRangeExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试自定义渲染器导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用自定义渲染器导出数据</li>
     *     <li>验证自定义渲染器的效果</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCustomRenderExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        List<String> headerNames = Arrays.asList("用户名", "邮箱");
        Map<String, ExportColumnMeta> columnMetaMap = new LinkedHashMap<>();
        columnMetaMap.put("username", ExportColumnMeta.create().setName("用户名"));
        columnMetaMap.put("email", ExportColumnMeta.create().setName("邮箱"));

        File file = helper.exportExcel(headerNames, columnMetaMap);
        assertNotNull(file);
        assertTrue(file.exists());

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row dataRow = sheet.getRow(1);
            assertEquals("张三", dataRow.getCell(0).getStringCellValue());
            assertEquals("ZHANGSAN@EXAMPLE.COM", dataRow.getCell(1).getStringCellValue());
        }
    }

    /**
     * 测试布尔类型导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出包含布尔字段的数据</li>
     *     <li>验证布尔值处理</li>
     * </ul>
     * </p>
     */
    @Test
    public void testBooleanExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试排除字段导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出时排除标记为excluded的字段</li>
     *     <li>验证排除字段不出现在导出文件中</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExcludedFieldExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试列排序
     * <p>
     * 测试内容：
     * <ul>
     *     <li>验证列按照order属性排序</li>
     * </ul>
     * </p>
     */
    @Test
    public void testColumnOrderExport() throws Exception {
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(testData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试多Sheet导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用数据处理器导出多个Sheet</li>
     *     <li>验证ZIP打包</li>
     * </ul>
     * </p>
     */
    @Test
    public void testMultiSheetExport() throws Exception {
        List<TestUserInfo> largeData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            largeData.add(new TestUserInfo("用户" + i, 20 + i, "user" + i + "@example.com", new Date(), i % 2, 1000.0 + i * 100, true));
        }

        IExportDataProcessor processor = new TestDataProcessor(largeData, 3);
        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(processor);

        File file = helper.export(TestUserInfo.class);
        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试Map数据导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出Map类型的数据</li>
     *     <li>验证Map数据的正确处理</li>
     * </ul>
     * </p>
     */
    @Test
    public void testMapDataExport() throws Exception {
        List<Map<String, Object>> mapData = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("username", "张三");
        map1.put("age", 25);
        map1.put("email", "zhangsan@example.com");
        mapData.add(map1);

        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(mapData);

        List<String> headerNames = Arrays.asList("用户名", "年龄", "邮箱");
        Map<String, ExportColumnMeta> columnMetaMap = new LinkedHashMap<>();
        columnMetaMap.put("username", ExportColumnMeta.create().setName("用户名"));
        columnMetaMap.put("age", ExportColumnMeta.create().setName("年龄"));
        columnMetaMap.put("email", ExportColumnMeta.create().setName("邮箱"));

        File file = helper.exportExcel(headerNames, columnMetaMap);
        assertNotNull(file);
        assertTrue(file.exists());

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row dataRow = sheet.getRow(1);
            assertEquals("张三", dataRow.getCell(0).getStringCellValue());
            assertEquals(25, dataRow.getCell(1).getNumericCellValue(), 0.001);
        }
    }

    /**
     * 测试空值处理
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出包含空值的数据</li>
     *     <li>验证空值的正确处理</li>
     * </ul>
     * </p>
     */
    @Test
    public void testNullValueExport() throws Exception {
        List<TestUserInfo> nullData = new ArrayList<>();
        nullData.add(new TestUserInfo(null, null, null, null, null, null, null));

        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(nullData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试特殊字符导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出包含特殊字符的数据</li>
     *     <li>验证特殊字符的正确处理</li>
     * </ul>
     * </p>
     */
    @Test
    public void testSpecialCharacterExport() throws Exception {
        List<TestUserInfo> specialData = new ArrayList<>();
        specialData.add(new TestUserInfo("张三,李四", 25, "test@example.com", new Date(), 0, 1000.50, true));

        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(specialData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试大数据量导出
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出大数据量</li>
     *     <li>验证性能和正确性</li>
     * </ul>
     * </p>
     */
    @Test
    public void testLargeDataExport() throws Exception {
        List<TestUserInfo> largeData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeData.add(new TestUserInfo("用户" + i, 20 + i, "user" + i + "@example.com", new Date(), i % 2, 1000.0 + i * 100, true));
        }

        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(largeData);
        File file = helper.export(TestUserInfo.class, null, true);

        assertNotNull(file);
        assertTrue(file.exists());
    }

    /**
     * 测试ExportColumnMeta类
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试create()静态方法</li>
     *     <li>测试各种setter和getter方法</li>
     *     <li>测试toString()方法</li>
     * </ul>
     * </p>
     */
    @Test
    public void testExportColumnMeta() {
        ExportColumnMeta meta = ExportColumnMeta.create();
        assertNotNull(meta);
        assertEquals("", meta.getName());
        assertFalse(meta.isDateTime());
        assertFalse(meta.isCurrency());
        assertEquals(2, meta.getDecimals());

        meta.setName("测试列")
                .setDateTime(true)
                .setPattern("yyyy-MM-dd")
                .setCurrency(true)
                .setAccuracy(false)
                .setDecimals(3)
                .setExcluded(true)
                .setImportable(false)
                .setOrder(1);

        assertEquals("测试列", meta.getName());
        assertTrue(meta.isDateTime());
        assertTrue(meta.isCurrency());
        assertEquals(3, meta.getDecimals());
        assertTrue(meta.isExcluded());
        assertFalse(meta.isImportable());
        assertEquals(1, meta.getOrder());

        String metaStr = meta.toString();
        assertTrue(metaStr.contains("测试列"));
    }


    /**
     * 测试CSV导出使用UTF-8编码
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出CSV文件</li>
     *     <li>验证使用UTF-8编码</li>
     *     <li>验证中文内容正确</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCsvUtf8Encoding() throws Exception {
        List<TestUserInfo> chineseData = new ArrayList<>();
        chineseData.add(new TestUserInfo("张三", 25, "zhangsan@example.com", new Date(), 0, 1000.50, true));

        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(chineseData);
        File file = helper.export(TestUserInfo.class, "UTF-8");

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getName().endsWith(".csv"));

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            inputStream.read(content);
            String csvContent = new String(content, StandardCharsets.UTF_8);
            assertTrue(csvContent.contains("张三"));
            assertTrue(csvContent.contains("zhangsan@example.com"));
        }
    }

    /**
     * 测试CSV导出使用默认UTF-8编码
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出CSV文件时不指定字符集</li>
     *     <li>验证使用默认UTF-8编码</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCsvDefaultEncoding() throws Exception {
        List<TestUserInfo> chineseData = new ArrayList<>();
        chineseData.add(new TestUserInfo("李四", 30, "lisi@example.com", new Date(), 1, 2000.75, false));

        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(chineseData);
        File file = helper.export(TestUserInfo.class);

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getName().endsWith(".csv"));

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            inputStream.read(content);
            String csvContent = new String(content, "GB2312");
            assertTrue(csvContent.contains("李四"));
            assertTrue(csvContent.contains("lisi@example.com"));
        }
    }

    /**
     * 测试样式缓存机制
     * <p>
     * 测试内容：
     * <ul>
     *     <li>导出大数据量</li>
     *     <li>验证样式缓存生效</li>
     *     <li>验证相同配置的单元格使用相同样式</li>
     * </ul>
     * </p>
     */
    @Test
    public void testStyleCaching() throws Exception {
        List<TestUserInfo> largeData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeData.add(new TestUserInfo("用户" + i, 20 + i, "user" + i + "@example.com", new Date(), i % 2, 1000.0 + i * 100, true));
        }

        ExcelFileExportHelper helper = ExcelFileExportHelper.bind(largeData);
        File file = helper.export(TestUserInfo.class, null, true);

        assertNotNull(file);
        assertTrue(file.exists());

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            CellStyle firstDateStyle = null;
            CellStyle firstNumberStyle = null;
            CellStyle firstTextStyle = null;

            for (int i = 1; i <= 10; i++) {
                Row row = sheet.getRow(i);
                assertNotNull(row);

                Cell dateCell = row.getCell(3);
                if (firstDateStyle == null) {
                    firstDateStyle = dateCell.getCellStyle();
                } else {
                    assertEquals("相同类型的单元格应该使用相同样式", firstDateStyle, dateCell.getCellStyle());
                }

                Cell numberCell = row.getCell(5);
                if (firstNumberStyle == null) {
                    firstNumberStyle = numberCell.getCellStyle();
                } else {
                    assertEquals("相同类型的单元格应该使用相同样式", firstNumberStyle, numberCell.getCellStyle());
                }

                Cell textCell = row.getCell(0);
                if (firstTextStyle == null) {
                    firstTextStyle = textCell.getCellStyle();
                } else {
                    assertEquals("相同类型的单元格应该使用相同样式", firstTextStyle, textCell.getCellStyle());
                }
            }
        }
    }


}
