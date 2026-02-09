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
 * Quote类的单元测试，用于验证Markdown引用组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:41:26
 * @since 2.1.4
 */
public class QuoteTest {

    /**
     * 测试使用字符串创建引用功能
     */
    @Test
    public void testCreateWithString() {
        Quote quote = Quote.create("Test quote");
        assertNotNull(quote);
        assertEquals("> Test quote\n> \n", quote.toMarkdown());
    }

    /**
     * 测试使用IMarkdown对象创建引用功能
     */
    @Test
    public void testCreateWithIMarkdown() {
        Text text = Text.create("Test quote");
        Quote quote = Quote.create(text);
        assertNotNull(quote);
        assertEquals("> Test quote\n> \n", quote.toMarkdown());
    }

    /**
     * 测试追加字符串到引用功能
     */
    @Test
    public void testAppendString() {
        Quote quote = Quote.create("Line 1");
        quote.append("Line 2");
        String result = quote.toMarkdown();
        assertTrue(result.contains("> Line 1") || result.contains("> Line 1Line 2"));
    }

    /**
     * 测试追加IMarkdown对象到引用功能
     */
    @Test
    public void testAppendIMarkdown() {
        Quote quote = Quote.create("Line 1");
        Text text = Text.create("Line 2");
        quote.append(text);
        String result = quote.toMarkdown();
        assertTrue(result.contains("> Line 1") || result.contains("> Line 1Line 2"));
    }

    /**
     * 测试多次追加内容到引用功能
     */
    @Test
    public void testMultipleAppends() {
        Quote quote = Quote.create("Line 1")
                .append("Line 2")
                .append("Line 3");
        String result = quote.toMarkdown();
        assertTrue(result.contains("> Line 1") || result.contains("> Line 1Line 2Line 3"));
    }

    /**
     * 测试空引用功能
     */
    @Test
    public void testEmptyQuote() {
        Quote quote = Quote.create("");
        assertEquals("", quote.toMarkdown());
    }

    /**
     * 测试null引用功能
     */
    @Test
    public void testNullQuote() {
        Quote quote = Quote.create((String) null);
        assertEquals("", quote.toMarkdown());
    }

    /**
     * 测试空白引用功能
     */
    @Test
    public void testBlankQuote() {
        Quote quote = Quote.create("   ");
        assertEquals("", quote.toMarkdown());
    }

    /**
     * 测试追加空字符串到引用功能
     */
    @Test
    public void testAppendEmptyString() {
        Quote quote = Quote.create("Line 1");
        quote.append("");
        assertEquals("> Line 1\n> \n", quote.toMarkdown());
    }

    /**
     * 测试追加null字符串到引用功能
     */
    @Test
    public void testAppendNullString() {
        Quote quote = Quote.create("Line 1");
        quote.append((String) null);
        assertEquals("> Line 1\n> \n", quote.toMarkdown());
    }

    /**
     * 测试追加空白字符串到引用功能
     */
    @Test
    public void testAppendBlankString() {
        Quote quote = Quote.create("Line 1");
        quote.append("   ");
        assertEquals("> Line 1\n> \n", quote.toMarkdown());
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        Quote quote = Quote.create("Test");
        assertEquals("> Test\n> \n", quote.toString());
    }

    /**
     * 测试toMarkdown方法是否返回正确的markdown内容
     */
    @Test
    public void testToMarkdown() {
        Quote quote = Quote.create("Test");
        assertEquals("> Test\n> \n", quote.toMarkdown());
    }

    /**
     * 测试包含特殊字符的引用功能
     */
    @Test
    public void testQuoteWithSpecialCharacters() {
        Quote quote = Quote.create("Quote with @#$%^&*()");
        String result = quote.toMarkdown();
        assertTrue(result.contains("> Quote with @#$%^&*()"));
    }

    /**
     * 测试包含Unicode字符的引用功能
     */
    @Test
    public void testQuoteWithUnicode() {
        Quote quote = Quote.create("中文引用");
        assertEquals("> 中文引用\n> \n", quote.toMarkdown());
    }

    /**
     * 测试包含数字的引用功能
     */
    @Test
    public void testQuoteWithNumbers() {
        Quote quote = Quote.create("Quote with 12345");
        String result = quote.toMarkdown();
        assertTrue(result.contains("> Quote with 12345"));
    }

    /**
     * 测试单个字符的引用功能
     */
    @Test
    public void testQuoteWithSingleCharacter() {
        Quote quote = Quote.create("A");
        assertEquals("> A\n> \n", quote.toMarkdown());
    }

    /**
     * 测试包含非常长内容的引用功能
     */
    @Test
    public void testQuoteWithVeryLongContent() {
        String longContent = "This is a very long quote that should be handled correctly by the Quote class in the markdown library. It contains multiple words and sentences to test how the class handles large amounts of text.";
        Quote quote = Quote.create(longContent);
        String result = quote.toMarkdown();
        assertTrue(result.contains("> " + longContent));
    }

    /**
     * 测试内容包含空白字符的引用功能
     */
    @Test
    public void testQuoteWithWhitespaceInContent() {
        Quote quote = Quote.create("   Content with leading and trailing whitespace   ");
        String result = quote.toMarkdown();
        assertFalse(result.contains("   Content"));
        assertFalse(result.contains("whitespace   "));
        assertTrue(result.contains("> Content with leading and trailing whitespace"));
    }

    /**
     * 测试空内容引用的创建和追加功能
     */
    @Test
    public void testQuoteWithEmptyBoth() {
        Quote quote = Quote.create("");
        quote.append("");
        assertEquals("", quote.toMarkdown());
    }

    /**
     * 测试null内容引用的创建和追加功能
     */
    @Test
    public void testQuoteWithNullBoth() {
        Quote quote = Quote.create((String) null);
        quote.append((String) null);
        assertEquals("", quote.toMarkdown());
    }

    /**
     * 测试引用的构建器模式功能
     */
    @Test
    public void testQuoteBuilderPattern() {
        Quote quote = Quote.create("Start")
                .append("Middle")
                .append("End");
        String result = quote.toMarkdown();
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    /**
     * 测试追加相同内容到引用的功能
     */
    @Test
    public void testQuoteWithSameContentAppended() {
        String content = "Same content";
        Quote quote = Quote.create(content);
        quote.append(content);
        String result = quote.toMarkdown();
        assertNotNull(result);
    }

    /**
     * 测试引用输出格式是否符合标准
     */
    @Test
    public void testQuoteOutputFormat() {
        Quote quote = Quote.create("Test");
        String result = quote.toMarkdown();
        assertTrue(result.startsWith("> "));
        assertTrue(result.endsWith("> \n"));
    }
}
