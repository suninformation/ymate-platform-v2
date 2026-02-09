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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * ConsoleTableBuilder测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:04:48
 * @since 2.1.4
 */
public class ConsoleTableBuilderTest {

    @Test
    public void testCreate() {
        // 测试静态创建方法
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(3);
        Assert.assertNotNull(builder);
    }

    @Test
    public void testBasicTable() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(3);
        // 添加表头
        builder.addRow().addColumn("Name").addColumn("Age").addColumn("City");
        // 添加数据行
        builder.addRow().addColumn("John").addColumn("30").addColumn("New York");
        builder.addRow().addColumn("Alice").addColumn("25").addColumn("London");
        // 生成表格字符串
        String tableString = builder.toString();
        Assert.assertNotNull(tableString);
        Assert.assertTrue(tableString.contains("Name"));
        Assert.assertTrue(tableString.contains("Age"));
        Assert.assertTrue(tableString.contains("City"));
        Assert.assertTrue(tableString.contains("John"));
        Assert.assertTrue(tableString.contains("Alice"));
    }

    @Test
    public void testMarkdownFormat() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(3).markdown();
        // 添加表头
        builder.addRow().addColumn("Product").addColumn("Price").addColumn("Stock");
        // 添加数据行
        builder.addRow().addColumn("Apple").addColumn("$1.99").addColumn("100");
        builder.addRow().addColumn("Banana").addColumn("$0.99").addColumn("200");
        // 生成Markdown表格
        String markdownString = builder.toString();
        Assert.assertNotNull(markdownString);
        Assert.assertTrue(markdownString.contains("| Product | Price | Stock |"));
        Assert.assertTrue(markdownString.contains("|---------|-------|-------|"));
        Assert.assertTrue(markdownString.contains("Apple"));
        Assert.assertTrue(markdownString.contains("$1.99"));
        Assert.assertTrue(markdownString.contains("100"));
        Assert.assertTrue(markdownString.contains("Banana"));
        Assert.assertTrue(markdownString.contains("$0.99"));
        Assert.assertTrue(markdownString.contains("200"));
    }

    @Test
    public void testCSVFormat() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(3).csv();
        // 添加表头
        builder.addRow().addColumn("ID").addColumn("Name").addColumn("Email");
        // 添加数据行
        builder.addRow().addColumn("1").addColumn("Tom").addColumn("tom@example.com");
        builder.addRow().addColumn("2").addColumn("Jerry").addColumn("jerry@example.com");
        // 生成CSV字符串
        String csvString = builder.toString();
        Assert.assertNotNull(csvString);
        Assert.assertTrue(csvString.contains("ID,Name,Email"));
        Assert.assertTrue(csvString.contains("1,Tom,tom@example.com"));
        Assert.assertTrue(csvString.contains("2,Jerry,jerry@example.com"));
    }

    @Test
    public void testCSVFormatWithSpecialCharacters() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(3).csv();
        // 添加包含特殊字符的数据
        builder.addRow().addColumn("ID").addColumn("Text").addColumn("Notes");
        builder.addRow().addColumn("1").addColumn("Hello, World").addColumn("Contains comma");
        builder.addRow().addColumn("2").addColumn("Line1\nLine2").addColumn("Contains newline");
        builder.addRow().addColumn("3").addColumn("Quote \"test\"").addColumn("Contains quote");
        // 生成CSV字符串
        String csvString = builder.toString();
        Assert.assertNotNull(csvString);
        // 验证特殊字符被正确处理
        Assert.assertTrue(csvString.contains("1,\"Hello, World\",Contains comma"));
        Assert.assertTrue(csvString.contains("2,\"Line1\nLine2\",Contains newline"));
        Assert.assertTrue(csvString.contains("3,\"Quote \"\"test\"\"\",Contains quote"));
    }

    @Test
    public void testSeparateLine() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2).separateLine();
        // 添加行
        builder.addRow().addColumn("Key").addColumn("Value");
        builder.addRow().addColumn("user").addColumn("admin");
        builder.addRow().addColumn("pass").addColumn("secret");
        // 生成带分隔线的表格
        String tableString = builder.toString();
        Assert.assertNotNull(tableString);
        // 验证分隔线存在
        Assert.assertTrue(tableString.contains("+"));
        Assert.assertTrue(tableString.contains("-"));
    }

    @Test
    public void testEscape() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2).escape();
        // 添加包含特殊字符的数据
        builder.addRow().addColumn("Text").addColumn("Value");
        builder.addRow().addColumn("Line1\nLine2").addColumn("Tab\tTest");
        // 生成转义后的表格
        String escapedString = builder.toString();
        Assert.assertNotNull(escapedString);
        // 验证特殊字符被转义
        Assert.assertTrue(escapedString.contains("[\\n]"));
        Assert.assertTrue(escapedString.contains("[\\t]"));
    }

    @Test
    public void testWriteToOutputStream() throws IOException {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 添加数据
        builder.addRow().addColumn("Name").addColumn("Value");
        builder.addRow().addColumn("Test").addColumn("123");
        // 写入输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        builder.writeTo(outputStream);
        String outputString = outputStream.toString(StandardCharsets.UTF_8.name());
        Assert.assertNotNull(outputString);
        Assert.assertTrue(outputString.contains("Name"));
        Assert.assertTrue(outputString.contains("Value"));
        Assert.assertTrue(outputString.contains("Test"));
        Assert.assertTrue(outputString.contains("123"));
    }

    @Test
    public void testWriteToOutputStreamWithCharset() throws IOException {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 添加数据
        builder.addRow().addColumn("中文").addColumn("测试");
        // 写入输出流（指定编码）
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        builder.writeTo(outputStream, StandardCharsets.UTF_8);
        String outputString = outputStream.toString(StandardCharsets.UTF_8.name());
        Assert.assertNotNull(outputString);
        Assert.assertTrue(outputString.contains("中文"));
        Assert.assertTrue(outputString.contains("测试"));
    }

    @Test
    public void testWriteToOutputStreamWithCharsetName() throws IOException {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 添加数据
        builder.addRow().addColumn("Header").addColumn("Data");
        // 写入输出流（指定编码名称）
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        builder.writeTo(outputStream, "UTF-8");
        String outputString = outputStream.toString("UTF-8");
        Assert.assertNotNull(outputString);
        Assert.assertTrue(outputString.contains("Header"));
        Assert.assertTrue(outputString.contains("Data"));
    }

    @Test
    public void testRowAndColumnMethods() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 测试链式调用
        ConsoleTableBuilder.Row row = builder.addRow();
        Assert.assertNotNull(row);
        // 添加列并返回行对象
        ConsoleTableBuilder.Row sameRow = row.addColumn("Column1").addColumn("Column2");
        Assert.assertEquals(row, sameRow);
        // 测试获取构建器
        ConsoleTableBuilder retrievedBuilder = row.builder();
        Assert.assertEquals(builder, retrievedBuilder);
        // 测试获取列数
        Assert.assertEquals(2, row.getColumns().size());
        // 测试获取列长度
        Assert.assertEquals(7, row.getColumnLength(0)); // "Column1" 的长度
        Assert.assertEquals(7, row.getColumnLength(1)); // "Column2" 的长度
        Assert.assertEquals(0, row.getColumnLength(2)); // 不存在的列
    }

    @Test
    public void testEmptyTable() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 生成空表格
        String emptyTableString = builder.toString();
        Assert.assertNotNull(emptyTableString);
    }

    @Test
    public void testColumnWithNullContent() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 添加包含null内容的列
        builder.addRow().addColumn(null).addColumn("NotNull");
        String tableString = builder.toString();
        Assert.assertNotNull(tableString);
        Assert.assertTrue(tableString.contains("NULL"));
        Assert.assertTrue(tableString.contains("NotNull"));
    }

    @Test
    public void testGetRows() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 添加多行
        builder.addRow().addColumn("Row1-Col1").addColumn("Row1-Col2");
        builder.addRow().addColumn("Row2-Col1").addColumn("Row2-Col2");
        // 获取行列表
        Assert.assertEquals(2, builder.getRows().size());
    }

    @Test
    public void testGetColumnLengths() {
        ConsoleTableBuilder builder = ConsoleTableBuilder.create(2);
        // 添加不同长度的列
        builder.addRow().addColumn("Short").addColumn("Longer Text");
        builder.addRow().addColumn("Medium Length").addColumn("Short");
        // 获取列长度数组
        int[] columnLengths = builder.getColumnLengths();
        Assert.assertEquals(2, columnLengths.length);
        // 验证每列的最大长度
        Assert.assertEquals(13, columnLengths[0]); // "Medium Length" 的长度
        Assert.assertEquals(11, columnLengths[1]); // "Longer Text" 的长度
    }
}
