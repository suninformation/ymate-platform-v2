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
package net.ymate.platform.commons.markdown;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Table类的单元测试，用于验证Markdown表格组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 02:22:55
 * @since 2.1.4
 */
public class TableTest {

    /**
     * 测试创建表格功能
     */
    @Test
    public void testCreateTable() {
        Table table = Table.create();
        assertNotNull(table);
    }

    /**
     * 测试添加表头功能
     */
    @Test
    public void testAddHeader() {
        Table table = Table.create()
                .addHeader("Column 1");
        String result = table.toMarkdown();
        System.out.println("testAddHeader: " + result);
        assertTrue(result.contains("|Column 1|"));
    }

    /**
     * 测试添加带对齐方式的表头功能
     */
    @Test
    public void testAddHeadersWithAlignment() {
        Table table = Table.create()
                .addHeader("Left", Table.Align.LEFT)
                .addHeader("Center", Table.Align.CENTER)
                .addHeader("Right", Table.Align.RIGHT)
                .addHeader("Default");
        String result = table.toMarkdown();
        System.out.println("testAddHeadersWithAlignment: " + result);
        assertTrue(result.contains(":---|"));
        assertTrue(result.contains(":---:|"));
        assertTrue(result.contains("---:|"));
        assertTrue(result.contains("---|"));
    }

    /**
     * 测试使用IMarkdown对象添加表头功能
     */
    @Test
    public void testAddHeaderWithIMarkdown() {
        Table table = Table.create()
                .addHeader(Text.create("Markdown Header"));
        String result = table.toMarkdown();
        System.out.println("testAddHeaderWithIMarkdown: " + result);
        assertTrue(result.contains("|Markdown Header|"));
    }

    /**
     * 测试使用IMarkdown对象和对齐方式添加表头功能
     */
    @Test
    public void testAddHeaderWithIMarkdownAndAlignment() {
        Table table = Table.create()
                .addHeader(Text.create("Aligned Header"), Table.Align.CENTER);
        String result = table.toMarkdown();
        System.out.println("testAddHeaderWithIMarkdownAndAlignment: " + result);
        assertNotNull(result);
    }

    /**
     * 测试添加行和列功能
     */
    @Test
    public void testAddRow() {
        Table table = Table.create()
                .addHeader("Column 1")
                .addRow()
                .addColumn("Row 1")
                .build();
        String result = table.toMarkdown();
        System.out.println("testAddRow: " + result);
        assertTrue(result.contains("|Row 1|"));
    }

    /**
     * 测试添加多行功能
     */
    @Test
    public void testAddMultipleRows() {
        Table table = Table.create()
                .addHeader("Column 1")
                .addHeader("Column 2")
                .addRow()
                .addColumn("Row 1 Col 1")
                .addColumn("Row 1 Col 2")
                .build()
                .addRow()
                .addColumn("Row 2 Col 1")
                .addColumn("Row 2 Col 2")
                .build();
        String result = table.toMarkdown();
        System.out.println("testAddMultipleRows: " + result);
        assertTrue(result.contains("|Row 1 Col 1|Row 1 Col 2|"));
        assertTrue(result.contains("|Row 2 Col 1|Row 2 Col 2|"));
    }

    /**
     * 测试添加IMarkdown对象作为列内容功能
     */
    @Test
    public void testAddColumnWithIMarkdown() {
        Table table = Table.create()
                .addHeader("Column 1")
                .addRow()
                .addColumn(Text.create("Markdown Content"))
                .build();
        String result = table.toMarkdown();
        System.out.println("testAddColumnWithIMarkdown: " + result);
        assertTrue(result.contains("|Markdown Content|"));
    }

    /**
     * 测试空表格功能
     */
    @Test
    public void testEmptyTable() {
        Table table = Table.create();
        String result = table.toMarkdown();
        System.out.println("testEmptyTable: " + result);
        assertEquals("", result);
    }

    /**
     * 测试只有表头的表格功能
     */
    @Test
    public void testTableWithOnlyHeaders() {
        Table table = Table.create()
                .addHeader("Column 1")
                .addHeader("Column 2");
        String result = table.toMarkdown();
        System.out.println("testTableWithOnlyHeaders: " + result);
        assertNotNull(result);
    }

    /**
     * 测试行与列数量不同的表格功能
     */
    @Test
    public void testTableWithDifferentColumnCounts() {
        Table table = Table.create()
                .addHeader("Column 1")
                .addHeader("Column 2")
                .addRow()
                .addColumn("Only Col 1")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithDifferentColumnCounts: " + result);
        assertNotNull(result);
    }

    /**
     * 测试包含特殊字符的表格功能
     */
    @Test
    public void testTableWithSpecialCharacters() {
        Table table = Table.create()
                .addHeader("Column|1")
                .addRow()
                .addColumn("Content\nwith|newlines	and tabs")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithSpecialCharacters: " + result);
        assertTrue(result.contains("Column\\|1"));
        assertTrue(result.contains("Content<br/>with\\|newlines"));
    }

    /**
     * 测试包含Unicode字符的表格功能
     */
    @Test
    public void testTableWithUnicode() {
        Table table = Table.create()
                .addHeader("中文列")
                .addRow()
                .addColumn("中文内容")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithUnicode: " + result);
        assertTrue(result.contains("|中文列|"));
        assertTrue(result.contains("|中文内容|"));
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        Table table = Table.create()
                .addHeader("Column 1")
                .addRow()
                .addColumn("Data")
                .build();
        String toStringResult = table.toString();
        String toMarkdownResult = table.toMarkdown();
        System.out.println("testToString: " + toStringResult);
        assertEquals(toMarkdownResult, toStringResult);
    }

    /**
     * 测试行构建方法是否返回原始表格对象
     */
    @Test
    public void testRowBuildMethod() {
        Table table = Table.create()
                .addHeader("Column 1");
        Table.Row row = table.addRow();
        Table resultTable = row.addColumn("Data").build();
        assertSame(table, resultTable);
    }

    /**
     * 测试获取行中列的数量功能
     */
    @Test
    public void testRowGetColumns() {
        Table table = Table.create()
                .addHeader("Column 1");
        Table.Row row = table.addRow();
        row.addColumn("Data 1")
                .addColumn("Data 2");
        assertEquals(2, row.getColumns().size());
    }

    /**
     * 测试添加空字符串表头功能
     */
    @Test
    public void testHeaderWithEmptyString() {
        Table table = Table.create()
                .addHeader("");
        String result = table.toMarkdown();
        System.out.println("testHeaderWithEmptyString: " + result);
        assertTrue(result.contains("||"));
    }

    /**
     * 测试添加空字符串列内容功能
     */
    @Test
    public void testColumnWithEmptyString() {
        Table table = Table.create()
                .addHeader("Column 1")
                .addRow()
                .addColumn("")
                .build();
        String result = table.toMarkdown();
        System.out.println("testColumnWithEmptyString: " + result);
        assertTrue(result.contains("||"));
    }

    /**
     * 测试表格构建器模式功能
     */
    @Test
    public void testTableBuilderPattern() {
        Table table = Table.create()
                .addHeader("Name", Table.Align.LEFT)
                .addHeader("Value", Table.Align.RIGHT)
                .addRow()
                .addColumn("Item 1")
                .addColumn("100")
                .build()
                .addRow()
                .addColumn("Item 2")
                .addColumn("200")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableBuilderPattern: " + result);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    /**
     * 测试单列表格功能
     */
    @Test
    public void testTableWithSingleColumn() {
        Table table = Table.create()
                .addHeader("Single Column")
                .addRow()
                .addColumn("Row 1")
                .build()
                .addRow()
                .addColumn("Row 2")
                .build()
                .addRow()
                .addColumn("Row 3")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithSingleColumn: " + result);
        assertTrue(result.contains("|Row 1|"));
        assertTrue(result.contains("|Row 2|"));
        assertTrue(result.contains("|Row 3|"));
    }

    /**
     * 测试多列表格功能
     */
    @Test
    public void testTableWithManyColumns() {
        Table table = Table.create()
                .addHeader("Col 1")
                .addHeader("Col 2")
                .addHeader("Col 3")
                .addHeader("Col 4")
                .addHeader("Col 5")
                .addRow()
                .addColumn("1")
                .addColumn("2")
                .addColumn("3")
                .addColumn("4")
                .addColumn("5")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithManyColumns: " + result);
        assertNotNull(result);
    }

    /**
     * 测试混合对齐方式的表格功能
     */
    @Test
    public void testTableWithMixedAlignment() {
        Table table = Table.create()
                .addHeader("Left Aligned", Table.Align.LEFT)
                .addHeader("Center Aligned", Table.Align.CENTER)
                .addHeader("Right Aligned", Table.Align.RIGHT)
                .addHeader("Default")
                .addRow()
                .addColumn("Left")
                .addColumn("Center")
                .addColumn("Right")
                .addColumn("Default")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithMixedAlignment: " + result);
        assertNotNull(result);
    }

    /**
     * 测试获取列列表是否为不可修改集合
     */
    @Test
    public void testRowGetColumnsIsUnmodifiable() {
        Table table = Table.create()
                .addHeader("Column 1");
        Table.Row row = table.addRow();
        row.addColumn("Data");
        try {
            row.getColumns().add("Should not be allowed");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    /**
     * 测试包含换行符的表格内容功能
     */
    @Test
    public void testTableWithNewlinesInContent() {
        Table table = Table.create()
                .addHeader("Column with\nNewlines")
                .addRow()
                .addColumn("Content\nwith\nmultiple\nlines")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithNewlinesInContent: " + result);
        assertTrue(result.contains("Column with<br/>Newlines"));
        assertTrue(result.contains("Content<br/>with<br/>multiple<br/>lines"));
    }

    /**
     * 测试包含制表符的表格内容功能
     */
    @Test
    public void testTableWithTabsInContent() {
        Table table = Table.create()
                .addHeader("Column with\ttab")
                .addRow()
                .addColumn("Content\twith\ttabs")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithTabsInContent: " + result);
        assertNotNull(result);
    }

    /**
     * 测试包含CRLF换行符的表格内容功能
     */
    @Test
    public void testTableWithCRLFInContent() {
        Table table = Table.create()
                .addHeader("Column")
                .addRow()
                .addColumn("Content\r\nwith\r\nCRLF")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithCRLFInContent: " + result);
        assertTrue(result.contains("Content<br/>with<br/>CRLF"));
    }

    /**
     * 测试包含管道符的表格内容功能
     */
    @Test
    public void testTableWithPipeInContent() {
        Table table = Table.create()
                .addHeader("Column")
                .addRow()
                .addColumn("Content with | pipe")
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithPipeInContent: " + result);
        assertTrue(result.contains("Content with \\| pipe"));
    }

    /**
     * 测试不同顺序添加表头功能
     */
    @Test
    public void testAddHeadersInDifferentOrders() {
        Table table1 = Table.create()
                .addHeader("Header 1", Table.Align.LEFT)
                .addHeader("Header 2");

        Table table2 = Table.create()
                .addHeader("Header A")
                .addHeader("Header B", Table.Align.RIGHT);

        String result1 = table1.toMarkdown();
        String result2 = table2.toMarkdown();

        System.out.println("testAddHeadersInDifferentOrders (table1): " + result1);
        System.out.println("testAddHeadersInDifferentOrders (table2): " + result2);
        assertNotNull(result1);
        assertNotNull(result2);
    }

    /**
     * 测试多个单元格包含相同内容的表格功能
     */
    @Test
    public void testTableWithSameContentInMultipleCells() {
        String sameContent = "Same Content";
        Table table = Table.create()
                .addHeader("Column 1")
                .addHeader("Column 2")
                .addHeader("Column 3")
                .addRow()
                .addColumn(sameContent)
                .addColumn(sameContent)
                .addColumn(sameContent)
                .build();
        String result = table.toMarkdown();
        System.out.println("testTableWithSameContentInMultipleCells: " + result);
        assertNotNull(result);
    }
}
