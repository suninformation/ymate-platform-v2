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
 * ParagraphList类的单元测试，用于验证Markdown列表组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:42:16
 * @since 2.1.4
 */
public class ParagraphListTest {

    /**
     * 测试创建无序列表功能
     */
    @Test
    public void testCreateUnorderedList() {
        ParagraphList list = ParagraphList.create();
        assertNotNull(list);
    }

    /**
     * 测试创建有序列表功能
     */
    @Test
    public void testCreateOrderedList() {
        ParagraphList list = ParagraphList.create(true);
        assertNotNull(list);
    }

    /**
     * 测试添加单个列表项功能
     */
    @Test
    public void testAddSingleItem() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1");
        String result = list.toMarkdown();
        assertTrue(result.contains("- Item 1"));
    }

    /**
     * 测试添加多个列表项功能
     */
    @Test
    public void testAddMultipleItems() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addItem("Item 2")
                .addItem("Item 3");
        String result = list.toMarkdown();
        assertTrue(result.contains("- Item 1"));
        assertTrue(result.contains("- Item 2"));
        assertTrue(result.contains("- Item 3"));
    }

    /**
     * 测试使用数组添加多个列表项功能
     */
    @Test
    public void testAddItemsArray() {
        ParagraphList list = ParagraphList.create()
                .addItems("Item 1", "Item 2", "Item 3");
        String result = list.toMarkdown();
        assertTrue(result.contains("- Item 1") || result.contains("1. Item 1"));
    }

    /**
     * 测试添加空列表项功能
     */
    @Test
    public void testAddEmptyItem() {
        ParagraphList list = ParagraphList.create()
                .addItem("");
        String result = list.toMarkdown();
        assertEquals("", result);
    }

    /**
     * 测试添加null列表项功能
     */
    @Test
    public void testAddNullItem() {
        ParagraphList list = ParagraphList.create()
                .addItem(null);
        String result = list.toMarkdown();
        assertEquals("", result);
    }

    /**
     * 测试添加空白列表项功能
     */
    @Test
    public void testAddBlankItem() {
        ParagraphList list = ParagraphList.create()
                .addItem("   ");
        String result = list.toMarkdown();
        assertEquals("", result);
    }

    /**
     * 测试有序列表功能
     */
    @Test
    public void testOrderedList() {
        ParagraphList list = ParagraphList.create(true)
                .addItem("Item 1")
                .addItem("Item 2");
        String result = list.toMarkdown();
        assertTrue(result.contains("1. Item 1"));
        assertTrue(result.contains("2. Item 2"));
    }

    /**
     * 测试无序列表功能
     */
    @Test
    public void testUnorderedList() {
        ParagraphList list = ParagraphList.create(false)
                .addItem("Item 1")
                .addItem("Item 2");
        String result = list.toMarkdown();
        assertTrue(result.contains("- Item 1"));
        assertTrue(result.contains("- Item 2"));
    }

    /**
     * 测试添加子列表项功能
     */
    @Test
    public void testAddSubItem() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addSubItem("Subitem 1");
        String result = list.toMarkdown();
        assertTrue(result.contains("    "));
    }

    /**
     * 测试添加带有指定顺序的子列表项功能
     */
    @Test
    public void testAddSubItemWithOrder() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addSubItem(ParagraphList.create(true).addItem("Subitem 1"));
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试添加子列表项数组功能
     */
    @Test
    public void testAddSubItemsArray() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addSubItems("Subitem 1", "Subitem 2");
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试添加嵌套子列表项功能
     */
    @Test
    public void testAddNestedSubItems() {
        ParagraphList list = ParagraphList.create()
                .addItem("Level 1")
                .addSubItem(ParagraphList.create()
                        .addItem("Level 2")
                        .addSubItem(ParagraphList.create()
                                .addItem("Level 3")));
        String result = list.toMarkdown();
        assertNotNull(result);
        assertTrue(result.contains("    "));
    }

    /**
     * 测试添加列表项主体内容功能
     */
    @Test
    public void testAddBody() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addBody("Body content");
        String result = list.toMarkdown();
        assertTrue(result.contains("Body content"));
    }

    /**
     * 测试添加IMarkdown对象作为列表项主体内容功能
     */
    @Test
    public void testAddBodyWithIMarkdown() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addBody(Text.create("Markdown body"));
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试混合内容类型的列表功能
     */
    @Test
    public void testMixedContent() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addBody("Body text")
                .addSubItem("Subitem 1")
                .addItem("Item 2");
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试空列表的情况
     */
    @Test
    public void testEmptyList() {
        ParagraphList list = ParagraphList.create();
        assertEquals("", list.toMarkdown());
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        ParagraphList list = ParagraphList.create()
                .addItem("Test item");
        String toStringResult = list.toString();
        String toMarkdownResult = list.toMarkdown();
        assertEquals(toMarkdownResult, toStringResult);
    }

    /**
     * 测试添加null数组作为列表项功能
     */
    @Test
    public void testAddItemsArrayWithNulls() {
        ParagraphList list = ParagraphList.create()
                .addItems((String[]) null);
        assertEquals("", list.toMarkdown());
    }

    /**
     * 测试添加包含空字符串和null的数组作为列表项功能
     */
    @Test
    public void testAddItemsArrayWithEmptyStrings() {
        ParagraphList list = ParagraphList.create()
                .addItems("", "   ", null, "Item 1");
        String result = list.toMarkdown();
        assertTrue(result.contains("Item 1"));
    }

    /**
     * 测试包含特殊字符的列表项功能
     */
    @Test
    public void testListWithSpecialCharacters() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item with @#$%^&*()")
                .addItem("Item with \t tab")
                .addItem("Item with \n newline");
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试包含Unicode字符的列表项功能
     */
    @Test
    public void testListWithUnicode() {
        ParagraphList list = ParagraphList.create()
                .addItem("中文项目 1")
                .addItem("中文项目 2");
        String result = list.toMarkdown();
        assertTrue(result.contains("中文项目 1"));
    }

    /**
     * 测试构建器模式创建列表功能
     */
    @Test
    public void testBuilderPattern() {
        ParagraphList list = ParagraphList.create(true)
                .addItem("Item 1")
                .addItem("Item 2")
                .addSubItems("Subitem 1", "Subitem 2")
                .addBody("Final body");
        String result = list.toMarkdown();
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    /**
     * 测试将ParagraphList作为子列表项功能
     */
    @Test
    public void testParagraphListAsSubItem() {
        ParagraphList subList = ParagraphList.create()
                .addItem("Subitem 1")
                .addItem("Subitem 2");
        ParagraphList mainList = ParagraphList.create()
                .addItem("Main item")
                .addSubItem(subList);
        String result = mainList.toMarkdown();
        assertNotNull(result);
        assertTrue(result.contains("    "));
    }

    /**
     * 测试列表项主体内容包含段落功能
     */
    @Test
    public void testBodyContentWithParagraphs() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item 1")
                .addBody("First paragraph\n\nSecond paragraph");
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试多次添加相同列表项功能
     */
    @Test
    public void testAddSameItemMultipleTimes() {
        ParagraphList list = ParagraphList.create()
                .addItem("Same item")
                .addItem("Same item")
                .addItem("Same item");
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试创建大型列表功能
     */
    @Test
    public void testLargeList() {
        ParagraphList list = ParagraphList.create();
        for (int i = 1; i <= 10; i++) {
            list.addItem("Item " + i);
        }
        String result = list.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试列表项中包含换行符功能
     */
    @Test
    public void testListItemWithNewlines() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item with\nnewline");
        String result = list.toMarkdown();
        assertTrue(result.contains("Item with newline"));
    }

    /**
     * 测试列表项中包含制表符功能
     */
    @Test
    public void testListItemWithTabs() {
        ParagraphList list = ParagraphList.create()
                .addItem("Item with\ttab");
        String result = list.toMarkdown();
        assertTrue(result.contains("Item with\ttab") || result.contains("Item with    tab"));
    }
}
