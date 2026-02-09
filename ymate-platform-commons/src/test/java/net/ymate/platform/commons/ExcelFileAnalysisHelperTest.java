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

import java.io.*;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ExcelFileAnalysisHelper类的单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-08 22:28:38
 * @since 2.1.4
 */
public class ExcelFileAnalysisHelperTest {

    private File testExcelFile;
    private File testXlsFile;
    private File testXlsxFile;

    /**
     * 创建测试用的Excel文件
     * <p>
     * 创建包含测试数据的Excel文件，用于测试导入功能。
     * </p>
     */
    @Before
    public void setUp() throws IOException {
        testExcelFile = File.createTempFile("test", ".xlsx");
        testXlsFile = File.createTempFile("test", ".xls");
        testXlsxFile = File.createTempFile("test", ".xlsx");

        createTestExcelFile(testExcelFile, false);
        createTestExcelFile(testXlsFile, true);
        createTestExcelFile(testXlsxFile, false);
    }

    /**
     * 清理测试文件
     * <p>
     * 删除测试过程中创建的临时文件。
     * </p>
     */
    @After
    public void tearDown() {
        if (testExcelFile != null && testExcelFile.exists()) {
            testExcelFile.delete();
        }
        if (testXlsFile != null && testXlsFile.exists()) {
            testXlsFile.delete();
        }
        if (testXlsxFile != null && testXlsxFile.exists()) {
            testXlsxFile.delete();
        }
    }

    /**
     * 创建测试用的Excel文件
     *
     * @param file  要创建的文件
     * @param isXls 是否为.xls格式
     */
    private void createTestExcelFile(File file, boolean isXls) throws IOException {
        try (Workbook workbook = isXls ? WorkbookFactory.create(false) : WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户名");
            headerRow.createCell(1).setCellValue("年龄");
            headerRow.createCell(2).setCellValue("邮箱");
            headerRow.createCell(3).setCellValue("创建时间");
            headerRow.createCell(4).setCellValue("性别");
            headerRow.createCell(5).setCellValue("金额");
            headerRow.createCell(6).setCellValue("是否激活");

            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("张三");
            dataRow1.createCell(1).setCellValue(25);
            dataRow1.createCell(2).setCellValue("zhangsan@example.com");
            dataRow1.createCell(3).setCellValue("2023-01-01 12:00:00");
            dataRow1.createCell(4).setCellValue(0);
            dataRow1.createCell(5).setCellValue(1000.50);
            dataRow1.createCell(6).setCellValue(true);

            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("李四");
            dataRow2.createCell(1).setCellValue(30);
            dataRow2.createCell(2).setCellValue("lisi@example.com");
            dataRow2.createCell(3).setCellValue("2023-01-02 13:00:00");
            dataRow2.createCell(4).setCellValue(1);
            dataRow2.createCell(5).setCellValue(2000.75);
            dataRow2.createCell(6).setCellValue(false);

            Sheet sheet2 = workbook.createSheet("Sheet2");
            Row headerRow2 = sheet2.createRow(0);
            headerRow2.createCell(0).setCellValue("产品名称");
            headerRow2.createCell(1).setCellValue("价格");

            Row dataRow3 = sheet2.createRow(1);
            dataRow3.createCell(0).setCellValue("产品A");
            dataRow3.createCell(1).setCellValue(100.00);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }

    /**
     * 测试bind(File)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>从.xlsx文件绑定</li>
     *     <li>从.xls文件绑定</li>
     *     <li>文件不存在时抛出IOException</li>
     * </ul>
     * </p>
     */
    @Test
    public void testBindFile() throws IOException {
        ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile);
        assertNotNull(helper);
        assertEquals(2, helper.getSheetNames().length);

        ExcelFileAnalysisHelper helper2 = ExcelFileAnalysisHelper.bind(testXlsFile);
        assertNotNull(helper2);
        assertEquals(2, helper2.getSheetNames().length);

        File nonExistentFile = new File("non_existent_file.xlsx");
        try {
            ExcelFileAnalysisHelper.bind(nonExistentFile);
            fail("应该抛出IOException异常");
        } catch (IOException e) {
            // 预期异常
        }
    }

    /**
     * 测试bind(InputStream)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>从InputStream绑定</li>
     *     <li>流为null时抛出异常</li>
     * </ul>
     * </p>
     */
    @Test
    public void testBindInputStream() throws IOException {
        try (FileInputStream inputStream = new FileInputStream(testXlsxFile)) {
            ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(inputStream);
            assertNotNull(helper);
            assertEquals(2, helper.getSheetNames().length);
        }

        try {
            ExcelFileAnalysisHelper.bind((InputStream) null);
            fail("应该抛出异常");
        } catch (Exception e) {
            // 预期异常
        }
    }

    /**
     * 测试getSheetNames()方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>获取所有Sheet名称</li>
     *     <li>验证Sheet名称的正确性</li>
     * </ul>
     * </p>
     */
    @Test
    public void testGetSheetNames() throws IOException {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            String[] sheetNames = helper.getSheetNames();
            assertNotNull(sheetNames);
            assertEquals(2, sheetNames.length);
            assertEquals("Sheet1", sheetNames[0]);
            assertEquals("Sheet2", sheetNames[1]);
        }
    }

    /**
     * 测试openSheet(int)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>打开指定索引的Sheet并返回原始数据</li>
     *     <li>验证返回的数据结构</li>
     *     <li>索引越界时抛出异常</li>
     * </ul>
     * </p>
     */
    @Test
    public void testOpenSheetByIndex() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            List<Object[]> data = helper.openSheet(0);
            assertNotNull(data);
            assertEquals(2, data.size());

            Object[] firstRow = data.get(0);
            assertNotNull(firstRow);
            assertEquals(7, firstRow.length);
            assertEquals("用户名", ((Object[]) firstRow[0])[0]);
            assertEquals("张三", ((Object[]) firstRow[0])[1]);

            try {
                helper.openSheet(10);
                fail("应该抛出异常");
            } catch (Exception e) {
                // 预期异常
            }
        }
    }

    /**
     * 测试openSheet(int, ISheetHandler)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用自定义处理器打开指定索引的Sheet</li>
     *     <li>验证处理器返回的数据</li>
     * </ul>
     * </p>
     */
    @Test
    public void testOpenSheetByIndexWithHandler() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            List<Object[]> data = helper.openSheet(0, new ISheetHandler.Default());
            assertNotNull(data);
            assertEquals(2, data.size());
        }
    }

    /**
     * 测试openSheet(String)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>打开指定名称的Sheet并返回原始数据</li>
     *     <li>验证返回的数据结构</li>
     *     <li>Sheet名称不存在时抛出异常</li>
     * </ul>
     * </p>
     */
    @Test
    public void testOpenSheetByName() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            List<Object[]> data = helper.openSheet("Sheet1");
            assertNotNull(data);
            assertEquals(2, data.size());

            try {
                helper.openSheet("NonExistentSheet");
                fail("应该抛出异常");
            } catch (Exception e) {
                // 预期异常
            }
        }
    }

    /**
     * 测试openSheet(String, ISheetHandler)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用自定义处理器打开指定名称的Sheet</li>
     *     <li>验证处理器返回的数据</li>
     * </ul>
     * </p>
     */
    @Test
    public void testOpenSheetByNameWithHandler() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            List<Object[]> data = helper.openSheet("Sheet1", new ISheetHandler.Default());
            assertNotNull(data);
            assertEquals(2, data.size());
        }
    }

    /**
     * 测试openSheet(int, Class)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>打开指定索引的Sheet并转换为指定类型</li>
     *     <li>验证映射后的Bean对象</li>
     * </ul>
     * </p>
     */
    @Test
    public void testOpenSheetByIndexWithClass() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            List<TestUserInfo> users = helper.openSheet(0, TestUserInfo.class);
            assertNotNull(users);
            assertEquals(2, users.size());

            TestUserInfo user1 = users.get(0);
            assertEquals("张三", user1.getUsername());
            assertEquals(Integer.valueOf(25), user1.getAge());
            assertEquals("zhangsan@example.com", user1.getEmail());
            assertEquals(Integer.valueOf(0), user1.getGender());
            assertTrue(user1.getActive());

            TestUserInfo user2 = users.get(1);
            assertEquals("李四", user2.getUsername());
            assertEquals(Integer.valueOf(30), user2.getAge());
            assertEquals("lisi@example.com", user2.getEmail());
            assertEquals(Integer.valueOf(1), user2.getGender());
            assertFalse(user2.getActive());
        }
    }

    /**
     * 测试openSheet(String, Class)方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>打开指定名称的Sheet并转换为指定类型</li>
     *     <li>验证映射后的Bean对象</li>
     * </ul>
     * </p>
     */
    @Test
    public void testOpenSheetByNameWithClass() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            List<TestUserInfo> users = helper.openSheet("Sheet1", TestUserInfo.class);
            assertNotNull(users);
            assertEquals(2, users.size());

            TestUserInfo user1 = users.get(0);
            assertEquals("张三", user1.getUsername());
        }
    }

    /**
     * 测试close()方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>手动关闭分析助手</li>
     *     <li>使用try-with-resources自动关闭</li>
     * </ul>
     * </p>
     */
    @Test
    public void testClose() throws IOException {
        ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile);
        assertNotNull(helper);
        helper.close();

        try (ExcelFileAnalysisHelper helper2 = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            assertNotNull(helper2);
        }
    }

    /**
     * 测试空Sheet处理
     * <p>
     * 测试内容：
     * <ul>
     *     <li>处理空Sheet</li>
     *     <li>验证返回空列表</li>
     * </ul>
     * </p>
     */
    @Test
    public void testEmptySheet() throws Exception {
        File emptyFile = File.createTempFile("empty", ".xlsx");
        try (Workbook workbook = WorkbookFactory.create(true)) {
            workbook.createSheet("EmptySheet");
            try (FileOutputStream outputStream = new FileOutputStream(emptyFile)) {
                workbook.write(outputStream);
            }
        }

        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(emptyFile)) {
            List<Object[]> data = helper.openSheet(0);
            assertNotNull(data);
            assertTrue(data.isEmpty());
        }

        emptyFile.delete();
    }

    /**
     * 测试只有表头的Sheet
     * <p>
     * 测试内容：
     * <ul>
     *     <li>处理只有表头的Sheet</li>
     *     <li>验证返回空列表</li>
     * </ul>
     * </p>
     */
    @Test
    public void testHeaderOnlySheet() throws Exception {
        File headerOnlyFile = File.createTempFile("header_only", ".xlsx");
        try (Workbook workbook = WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet("HeaderOnly");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户名");
            headerRow.createCell(1).setCellValue("年龄");
            try (FileOutputStream outputStream = new FileOutputStream(headerOnlyFile)) {
                workbook.write(outputStream);
            }
        }

        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(headerOnlyFile)) {
            List<Object[]> data = helper.openSheet(0);
            assertNotNull(data);
            assertTrue(data.isEmpty());
        }

        headerOnlyFile.delete();
    }

    /**
     * 测试ISheetHandler.Abstract的配置方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试firstRowNum()方法</li>
     *     <li>测试lastRowNum()方法</li>
     *     <li>测试firstCellNum()方法</li>
     *     <li>测试lastCellNum()方法</li>
     *     <li>测试decimalPattern()方法</li>
     * </ul>
     * </p>
     */
    @Test
    public void testSheetHandlerConfiguration() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            ISheetHandler.Default handler = new ISheetHandler.Default();
            handler.firstRowNum(1)
                    .lastRowNum(3)
                    .firstCellNum(0)
                    .lastCellNum(3)
                    .decimalPattern("##.##");

            List<Object[]> data = helper.openSheet(0, handler);
            assertNotNull(data);
            assertEquals(1, data.size());
        }
    }

    /**
     * 测试ISheetHandler.Bean的反射构造方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用反射构造Bean处理器</li>
     *     <li>验证反射构造的正确性</li>
     * </ul>
     * </p>
     */
    @Test
    public void testSheetHandlerBeanReflectionConstructor() throws Exception {
        class TestBeanHandler extends ISheetHandler.Bean<TestUserInfo> {
        }

        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            List<TestUserInfo> users = helper.openSheet(0, new TestBeanHandler());
            assertNotNull(users);
            assertEquals(2, users.size());
        }
    }

    /**
     * 测试ISheetHandler.CellMeta类
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试CellMeta的构造方法</li>
     *     <li>测试getName()方法</li>
     *     <li>测试getCellIndex()方法</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCellMeta() {
        ISheetHandler.CellMeta cellMeta = new ISheetHandler.CellMeta("用户名", 0);
        assertEquals("用户名", cellMeta.getName());
        assertEquals(0, cellMeta.getCellIndex());
    }

    /**
     * 测试数据类型解析
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试字符串类型解析</li>
     *     <li>测试数字类型解析</li>
     *     <li>测试日期类型解析</li>
     *     <li>测试布尔类型解析</li>
     *     <li>测试公式类型解析</li>
     * </ul>
     * </p>
     */
    @Test
    public void testDataTypeParsing() throws Exception {
        File dataTypeFile = File.createTempFile("datatype", ".xlsx");
        try (Workbook workbook = WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet("DataType");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("字符串");
            headerRow.createCell(1).setCellValue("数字");
            headerRow.createCell(2).setCellValue("日期");
            headerRow.createCell(3).setCellValue("布尔");
            headerRow.createCell(4).setCellValue("公式");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("测试文本");
            dataRow.createCell(1).setCellValue(123.45);
            Cell dateCell = dataRow.createCell(2);
            dateCell.setCellValue(new Date());
            dataRow.createCell(3).setCellValue(true);
            Cell formulaCell = dataRow.createCell(4);
            formulaCell.setCellFormula("A1");

            try (FileOutputStream outputStream = new FileOutputStream(dataTypeFile)) {
                workbook.write(outputStream);
            }
        }

        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(dataTypeFile)) {
            List<Object[]> data = helper.openSheet(0);
            assertNotNull(data);
            assertEquals(1, data.size());

            Object[] row = data.get(0);
            assertEquals("测试文本", ((Object[]) row[0])[1]);
            assertEquals("123.45", ((Object[]) row[1])[1]);
            assertNotNull(((Object[]) row[2])[1]);
            assertEquals(true, ((Object[]) row[3])[1]);
            assertEquals("A1", ((Object[]) row[4])[1]);
        }

        dataTypeFile.delete();
    }

    /**
     * 测试多Sheet处理
     * <p>
     * 测试内容：
     * <ul>
     *     <li>处理包含多个Sheet的Excel文件</li>
     *     <li>验证每个Sheet的数据正确性</li>
     * </ul>
     * </p>
     */
    @Test
    public void testMultipleSheets() throws Exception {
        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(testXlsxFile)) {
            String[] sheetNames = helper.getSheetNames();
            assertEquals(2, sheetNames.length);

            List<Object[]> data1 = helper.openSheet(0);
            assertEquals(2, data1.size());

            List<Object[]> data2 = helper.openSheet(1);
            assertEquals(1, data2.size());
        }
    }

    /**
     * 测试空单元格处理
     * <p>
     * 测试内容：
     * <ul>
     *     <li>处理包含空单元格的行</li>
     *     <li>验证空单元格的处理逻辑</li>
     * </ul>
     * </p>
     */
    @Test
    public void testEmptyCellHandling() throws Exception {
        File emptyCellFile = File.createTempFile("emptycell", ".xlsx");
        try (Workbook workbook = WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet("EmptyCell");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("字段1");
            headerRow.createCell(1).setCellValue("字段2");
            headerRow.createCell(2).setCellValue("字段3");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("值1");
            dataRow.createCell(2).setCellValue("值3");

            try (FileOutputStream outputStream = new FileOutputStream(emptyCellFile)) {
                workbook.write(outputStream);
            }
        }

        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(emptyCellFile)) {
            List<Object[]> data = helper.openSheet(0);
            assertNotNull(data);
            assertEquals(1, data.size());
        }

        emptyCellFile.delete();
    }

    /**
     * 测试大数据量处理
     * <p>
     * 测试内容：
     * <ul>
     *     <li>创建包含大量数据的Excel文件</li>
     *     <li>验证数据处理的正确性</li>
     * </ul>
     * </p>
     */
    @Test
    public void testLargeData() throws Exception {
        File largeFile = File.createTempFile("large", ".xlsx");
        try (Workbook workbook = WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet("LargeData");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户名");
            headerRow.createCell(1).setCellValue("年龄");

            for (int i = 0; i < 250; i++) {
                Row dataRow = sheet.createRow(i + 1);
                dataRow.createCell(0).setCellValue("用户" + i);
                dataRow.createCell(1).setCellValue(20 + i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(largeFile)) {
                workbook.write(outputStream);
            }
        }

        try (ExcelFileAnalysisHelper helper = ExcelFileAnalysisHelper.bind(largeFile)) {
            List<TestUserInfo> users = helper.openSheet(0, TestUserInfo.class);
            assertEquals(250, users.size());
        }

        largeFile.delete();
    }
}
