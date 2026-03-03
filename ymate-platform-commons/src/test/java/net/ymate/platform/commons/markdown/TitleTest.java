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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Title类的单元测试，用于验证Markdown标题组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:38:25
 * @since 2.1.4
 */
public class TitleTest {

    /**
     * 测试使用字符串创建标题功能
     */
    @Test
    public void testCreateWithString() {
        Title title = Title.create("Test Title");
        assertNotNull(title);
        String result = title.toMarkdown();
        System.out.println("testCreateWithString: " + result);
        assertEquals("# Test Title", result);
    }

    /**
     * 测试使用IMarkdown对象创建标题功能
     */
    @Test
    public void testCreateWithIMarkdown() {
        Text text = Text.create("Test Title");
        Title title = Title.create(text);
        assertNotNull(title);
        String result = title.toMarkdown();
        System.out.println("testCreateWithIMarkdown: " + result);
        assertEquals("# Test Title", result);
    }

    /**
     * 测试使用字符串和指定级别创建标题功能
     */
    @Test
    public void testCreateWithStringAndLevel() {
        Title title = Title.create("Test Title", 3);
        assertNotNull(title);
        String result = title.toMarkdown();
        System.out.println("testCreateWithStringAndLevel: " + result);
        assertEquals("### Test Title", result);
    }

    /**
     * 测试使用IMarkdown对象和指定级别创建标题功能
     */
    @Test
    public void testCreateWithIMarkdownAndLevel() {
        Text text = Text.create("Test Title");
        Title title = Title.create(text, 2);
        assertNotNull(title);
        String result = title.toMarkdown();
        System.out.println("testCreateWithIMarkdownAndLevel: " + result);
        assertEquals("## Test Title", result);
    }

    /**
     * 测试级别为1的标题功能
     */
    @Test
    public void testLevel1() {
        Title title = Title.create("Title", 1);
        String result = title.toMarkdown();
        System.out.println("testLevel1: " + result);
        assertEquals("# Title", result);
    }

    /**
     * 测试级别为2的标题功能
     */
    @Test
    public void testLevel2() {
        Title title = Title.create("Title", 2);
        String result = title.toMarkdown();
        System.out.println("testLevel2: " + result);
        assertEquals("## Title", result);
    }

    /**
     * 测试级别为3的标题功能
     */
    @Test
    public void testLevel3() {
        Title title = Title.create("Title", 3);
        String result = title.toMarkdown();
        System.out.println("testLevel3: " + result);
        assertEquals("### Title", result);
    }

    /**
     * 测试级别为4的标题功能
     */
    @Test
    public void testLevel4() {
        Title title = Title.create("Title", 4);
        String result = title.toMarkdown();
        System.out.println("testLevel4: " + result);
        assertEquals("#### Title", result);
    }

    /**
     * 测试级别为5的标题功能
     */
    @Test
    public void testLevel5() {
        Title title = Title.create("Title", 5);
        String result = title.toMarkdown();
        System.out.println("testLevel5: " + result);
        assertEquals("##### Title", result);
    }

    /**
     * 测试级别为6的标题功能
     */
    @Test
    public void testLevel6() {
        Title title = Title.create("Title", 6);
        String result = title.toMarkdown();
        System.out.println("testLevel6: " + result);
        assertEquals("###### Title", result);
    }

    /**
     * 测试级别为0的标题功能（应被视为级别1）
     */
    @Test
    public void testLevelZero() {
        Title title = Title.create("Title", 0);
        String result = title.toMarkdown();
        System.out.println("testLevelZero: " + result);
        assertEquals("# Title", result);
    }

    /**
     * 测试负级别标题功能（应被视为级别1）
     */
    @Test
    public void testNegativeLevel() {
        Title title = Title.create("Title", -5);
        String result = title.toMarkdown();
        System.out.println("testNegativeLevel: " + result);
        assertEquals("# Title", result);
    }

    /**
     * 测试大于6的级别标题功能（应被视为级别6）
     */
    @Test
    public void testLevelGreaterThan6() {
        Title title = Title.create("Title", 10);
        String result = title.toMarkdown();
        System.out.println("testLevelGreaterThan6: " + result);
        assertEquals("###### Title", result);
    }

    /**
     * 测试追加字符串到标题功能
     */
    @Test
    public void testAppendString() {
        Title title = Title.create("Hello");
        title.append(" World");
        String result = title.toMarkdown();
        System.out.println("testAppendString: " + result);
        assertEquals("# Hello World", result);
    }

    /**
     * 测试追加IMarkdown对象到标题功能
     */
    @Test
    public void testAppendIMarkdown() {
        Title title = Title.create("Hello");
        Text text = Text.create(" World");
        title.append(text);
        String result = title.toMarkdown();
        System.out.println("testAppendIMarkdown: " + result);
        assertEquals("# Hello World", result);
    }

    /**
     * 测试空标题功能
     */
    @Test
    public void testEmptyTitle() {
        Title title = Title.create("");
        String result = title.toMarkdown();
        System.out.println("testEmptyTitle: " + result);
        assertEquals("", result);
    }

    /**
     * 测试null标题功能
     */
    @Test
    public void testNullTitle() {
        Title title = Title.create((String) null);
        String result = title.toMarkdown();
        System.out.println("testNullTitle: " + result);
        assertEquals("", result);
    }

    /**
     * 测试包含换行符的标题功能
     */
    @Test
    public void testTitleWithNewlines() {
        Title title = Title.create("Line1\nLine2");
        String result = title.toMarkdown();
        System.out.println("testTitleWithNewlines: " + result);
        assertEquals("# Line1 Line2", result);
    }

    /**
     * 测试包含回车符的标题功能
     */
    @Test
    public void testTitleWithCarriageReturn() {
        Title title = Title.create("Line1\rLine2");
        String result = title.toMarkdown();
        System.out.println("testTitleWithCarriageReturn: " + result);
        assertEquals("# Line1Line2", result);
    }

    /**
     * 测试包含CRLF换行符的标题功能
     */
    @Test
    public void testTitleWithCRLF() {
        Title title = Title.create("Line1\r\nLine2");
        String result = title.toMarkdown();
        System.out.println("testTitleWithCRLF: " + result);
        assertEquals("# Line1 Line2", result);
    }

    /**
     * 测试包含制表符的标题功能
     */
    @Test
    public void testTitleWithTabs() {
        Title title = Title.create("Tab\tTest");
        String result = title.toMarkdown();
        System.out.println("testTitleWithTabs: " + result);
        assertEquals("# Tab    Test", result);
    }

    /**
     * 测试包含前后空白字符的标题功能
     */
    @Test
    public void testTitleWithWhitespace() {
        Title title = Title.create("  Whitespace  ");
        String result = title.toMarkdown();
        System.out.println("testTitleWithWhitespace: " + result);
        assertEquals("# Whitespace", result);
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        Title title = Title.create("Test");
        String result = title.toString();
        System.out.println("testToString: " + result);
        assertEquals("# Test", result);
    }

    /**
     * 测试toMarkdown方法是否返回正确的markdown内容
     */
    @Test
    public void testToMarkdown() {
        Title title = Title.create("Test");
        String result = title.toMarkdown();
        System.out.println("testToMarkdown: " + result);
        assertEquals("# Test", result);
    }

    /**
     * 测试获取标题文本内容功能
     */
    @Test
    public void testGetTitle() {
        Title title = Title.create("Test Title");
        assertEquals("Test Title", title.title.toString());
    }

    /**
     * 测试追加内容后获取标题文本功能
     */
    @Test
    public void testGetTitleAfterAppend() {
        Title title = Title.create("Hello");
        title.append(" World");
        assertEquals("Hello World", title.title.toString());
    }

    /**
     * 测试多次追加内容到标题功能
     */
    @Test
    public void testMultipleAppends() {
        Title title = Title.create("A");
        title.append(" B").append(" C").append(" D");
        String result = title.toMarkdown();
        System.out.println("testMultipleAppends: " + result);
        assertEquals("# A B C D", result);
    }

    /**
     * 测试包含特殊字符的标题功能
     */
    @Test
    public void testTitleWithSpecialCharacters() {
        Title title = Title.create("Title with *bold* and _italic_");
        String result = title.toMarkdown();
        System.out.println("testTitleWithSpecialCharacters: " + result);
        assertEquals("# Title with *bold* and _italic_", result);
    }

    /**
     * 测试包含数字的标题功能
     */
    @Test
    public void testTitleWithNumbers() {
        Title title = Title.create("12345");
        String result = title.toMarkdown();
        System.out.println("testTitleWithNumbers: " + result);
        assertEquals("# 12345", result);
    }

    /**
     * 测试包含Unicode字符的标题功能
     */
    @Test
    public void testTitleWithUnicode() {
        Title title = Title.create("中文标题");
        String result = title.toMarkdown();
        System.out.println("testTitleWithUnicode: " + result);
        assertEquals("# 中文标题", result);
    }

    /**
     * 测试标题构建器模式功能
     */
    @Test
    public void testTitleBuilderPattern() {
        Title title = Title.create("Start")
                .append(" Middle")
                .append(" End");
        String result = title.toMarkdown();
        System.out.println("testTitleBuilderPattern: " + result);
        assertEquals("# Start Middle End", result);
    }

    /**
     * 测试已过时的title字段功能
     */
    @Test
    public void testDeprecatedTitleField() {
        Title title = Title.create("Test");
        assertNotNull(title.title);
        assertEquals("Test", title.title.toString());
    }
}
