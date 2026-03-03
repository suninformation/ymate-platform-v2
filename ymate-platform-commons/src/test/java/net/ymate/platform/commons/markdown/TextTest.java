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
 * Text类的单元测试，用于验证Markdown文本组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:38:59
 * @since 2.1.4
 */
public class TextTest {

    /**
     * 测试使用字符串创建文本功能
     */
    @Test
    public void testCreateWithString() {
        Text text = Text.create("Hello");
        assertNotNull(text);
        String result = text.toMarkdown();
        System.out.println("testCreateWithString: " + result);
        assertEquals("Hello", result);
    }

    /**
     * 测试使用IMarkdown对象创建文本功能
     */
    @Test
    public void testCreateWithIMarkdown() {
        Title title = Title.create("Title");
        Text text = Text.create(title);
        assertNotNull(text);
        String result = text.toMarkdown();
        System.out.println("testCreateWithIMarkdown: " + result);
        assertEquals("# Title", result);
    }

    /**
     * 测试使用字符串和样式创建文本功能
     */
    @Test
    public void testCreateWithStringAndStyle() {
        Text text = Text.create("Bold", Text.Style.BOLD);
        assertNotNull(text);
        String result = text.toMarkdown();
        System.out.println("testCreateWithStringAndStyle: " + result);
        assertEquals("**Bold**", result);
    }

    /**
     * 测试使用IMarkdown对象和样式创建文本功能
     */
    @Test
    public void testCreateWithIMarkdownAndStyle() {
        Title title = Title.create("Title");
        Text text = Text.create(title, Text.Style.ITALIC);
        assertNotNull(text);
        String result = text.toMarkdown();
        System.out.println("testCreateWithIMarkdownAndStyle: " + result);
        assertEquals("*# Title*", result);
    }

    /**
     * 测试正常样式文本功能
     */
    @Test
    public void testNormalStyle() {
        Text text = Text.create("Normal", Text.Style.NORMAL);
        String result = text.toMarkdown();
        System.out.println("testNormalStyle: " + result);
        assertEquals("Normal", result);
    }

    /**
     * 测试粗体样式文本功能
     */
    @Test
    public void testBoldStyle() {
        Text text = Text.create("Bold", Text.Style.BOLD);
        String result = text.toMarkdown();
        System.out.println("testBoldStyle: " + result);
        assertEquals("**Bold**", result);
    }

    /**
     * 测试斜体样式文本功能
     */
    @Test
    public void testItalicStyle() {
        Text text = Text.create("Italic", Text.Style.ITALIC);
        String result = text.toMarkdown();
        System.out.println("testItalicStyle: " + result);
        assertEquals("*Italic*", result);
    }

    /**
     * 测试下划线样式文本功能
     */
    @Test
    public void testUnderlineStyle() {
        Text text = Text.create("Underline", Text.Style.UNDERLINE);
        String result = text.toMarkdown();
        System.out.println("testUnderlineStyle: " + result);
        assertEquals("<u>Underline</u>", result);
    }

    /**
     * 测试删除线样式文本功能
     */
    @Test
    public void testStrikeoutStyle() {
        Text text = Text.create("Strikeout", Text.Style.STRIKEOUT);
        String result = text.toMarkdown();
        System.out.println("testStrikeoutStyle: " + result);
        assertEquals("~~Strikeout~~", result);
    }

    /**
     * 测试空样式文本功能
     */
    @Test
    public void testNullStyle() {
        Text text = Text.create("Test", null);
        String result = text.toMarkdown();
        System.out.println("testNullStyle: " + result);
        assertEquals("Test", result);
    }

    /**
     * 测试空文本功能
     */
    @Test
    public void testEmptyText() {
        Text text = Text.create("");
        String result = text.toMarkdown();
        System.out.println("testEmptyText: " + result);
        assertEquals("", result);
    }

    /**
     * 测试null文本功能
     */
    @Test
    public void testNullText() {
        Text text = Text.create((String) null);
        String result = text.toMarkdown();
        System.out.println("testNullText: " + result);
        assertEquals("", result);
    }

    /**
     * 测试追加字符串到文本功能
     */
    @Test
    public void testAppendString() {
        Text text = Text.create("Hello");
        text.append(" World");
        String result = text.toMarkdown();
        System.out.println("testAppendString: " + result);
        assertEquals("Hello World", result);
    }

    /**
     * 测试追加IMarkdown对象到文本功能
     */
    @Test
    public void testAppendIMarkdown() {
        Text text = Text.create("Hello");
        Title title = Title.create(" World");
        text.append(title);
        String result = text.toMarkdown();
        System.out.println("testAppendIMarkdown: " + result);
        assertEquals("Hello # World", result);
    }

    /**
     * 测试多次追加内容到文本功能
     */
    @Test
    public void testMultipleAppends() {
        Text text = Text.create("A");
        text.append(" B").append(" C").append(" D");
        String result = text.toMarkdown();
        System.out.println("testMultipleAppends: " + result);
        assertEquals("A B C D", result);
    }

    /**
     * 测试包含换行符的文本功能
     */
    @Test
    public void testTextWithNewlines() {
        Text text = Text.create("Line1\nLine2\nLine3", Text.Style.BOLD);
        String result = text.toMarkdown();
        System.out.println("testTextWithNewlines: " + result);
        assertEquals("**Line1**\n**Line2**\n**Line3**", result);
    }

    /**
     * 测试包含空白字符的文本功能
     */
    @Test
    public void testTextWithWhitespace() {
        Text text = Text.create("  Whitespace  ");
        String result = text.toMarkdown();
        System.out.println("testTextWithWhitespace: " + result);
        assertEquals("Whitespace", result);
    }

    /**
     * 测试粗体样式文本包含换行符的功能
     */
    @Test
    public void testBoldTextWithNewlines() {
        Text text = Text.create("Line1\nLine2", Text.Style.BOLD);
        String result = text.toMarkdown();
        System.out.println("testBoldTextWithNewlines: " + result);
        assertTrue(result.contains("**Line1**"));
        assertTrue(result.contains("**Line2**"));
        assertTrue(result.contains("\n"));
    }

    /**
     * 测试斜体样式文本包含换行符的功能
     */
    @Test
    public void testItalicTextWithNewlines() {
        Text text = Text.create("Line1\nLine2", Text.Style.ITALIC);
        String result = text.toMarkdown();
        System.out.println("testItalicTextWithNewlines: " + result);
        assertTrue(result.contains("*Line1*"));
        assertTrue(result.contains("*Line2*"));
    }

    /**
     * 测试下划线样式文本包含换行符的功能
     */
    @Test
    public void testUnderlineTextWithNewlines() {
        Text text = Text.create("Line1\nLine2", Text.Style.UNDERLINE);
        String result = text.toMarkdown();
        System.out.println("testUnderlineTextWithNewlines: " + result);
        assertTrue(result.contains("<u>Line1</u>"));
        assertTrue(result.contains("<u>Line2</u>"));
    }

    /**
     * 测试删除线样式文本包含换行符的功能
     */
    @Test
    public void testStrikeoutTextWithNewlines() {
        Text text = Text.create("Line1\nLine2", Text.Style.STRIKEOUT);
        String result = text.toMarkdown();
        System.out.println("testStrikeoutTextWithNewlines: " + result);
        assertTrue(result.contains("~~Line1~~"));
        assertTrue(result.contains("~~Line2~~"));
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        Text text = Text.create("Test");
        String result = text.toString();
        System.out.println("testToString: " + result);
        assertEquals("Test", result);
    }

    /**
     * 测试toMarkdown方法是否返回正确的markdown内容
     */
    @Test
    public void testToMarkdown() {
        Text text = Text.create("Test");
        String result = text.toMarkdown();
        System.out.println("testToMarkdown: " + result);
        assertEquals("Test", result);
    }

    /**
     * 测试包含特殊字符的文本功能
     */
    @Test
    public void testTextWithSpecialCharacters() {
        Text text = Text.create("Special: @#$%^&*()");
        String result = text.toMarkdown();
        System.out.println("testTextWithSpecialCharacters: " + result);
        assertEquals("Special: @#$%^&*()", result);
    }

    /**
     * 测试包含Unicode字符的文本功能
     */
    @Test
    public void testTextWithUnicode() {
        Text text = Text.create("中文文本");
        String result = text.toMarkdown();
        System.out.println("testTextWithUnicode: " + result);
        assertEquals("中文文本", result);
    }

    /**
     * 测试包含数字的文本功能
     */
    @Test
    public void testTextWithNumbers() {
        Text text = Text.create("12345");
        String result = text.toMarkdown();
        System.out.println("testTextWithNumbers: " + result);
        assertEquals("12345", result);
    }

    /**
     * 测试文本构建器模式功能
     */
    @Test
    public void testTextBuilderPattern() {
        Text text = Text.create("Start")
                .append(" Middle")
                .append(" End");
        String result = text.toMarkdown();
        System.out.println("testTextBuilderPattern: " + result);
        assertEquals("Start Middle End", result);
    }

    /**
     * 测试带样式的文本构建器模式功能
     */
    @Test
    public void testStyledTextBuilderPattern() {
        Text text = Text.create("Start", Text.Style.BOLD)
                .append(" Middle")
                .append(" End");
        String result = text.toMarkdown();
        System.out.println("testStyledTextBuilderPattern: " + result);
        assertEquals("**Start Middle End**", result);
    }

    /**
     * 测试空的带样式文本功能
     */
    @Test
    public void testEmptyStyledText() {
        Text text = Text.create("", Text.Style.BOLD);
        String result = text.toMarkdown();
        System.out.println("testEmptyStyledText: " + result);
        assertEquals("", result);
    }

    /**
     * 测试混合内容文本功能
     */
    @Test
    public void testTextWithMixedContent() {
        Text text = Text.create("Line1\nLine2\nLine3", Text.Style.BOLD);
        String result = text.toMarkdown();
        System.out.println("testTextWithMixedContent: " + result);
        assertEquals(3, result.split("\\n").length);
    }

    /**
     * 测试单行文本功能
     */
    @Test
    public void testTextWithSingleLine() {
        Text text = Text.create("Single Line", Text.Style.BOLD);
        String result = text.toMarkdown();
        System.out.println("testTextWithSingleLine: " + result);
        assertEquals("**Single Line**", result);
    }

    /**
     * 测试包含末尾换行符的文本功能
     */
    @Test
    public void testTextWithTrailingNewline() {
        Text text = Text.create("Line1\n");
        String result = text.toMarkdown();
        System.out.println("testTextWithTrailingNewline: " + result);
        assertEquals("Line1", result);
    }

    /**
     * 测试包含开头换行符的文本功能
     */
    @Test
    public void testTextWithLeadingNewline() {
        Text text = Text.create("\nLine1");
        String result = text.toMarkdown();
        System.out.println("testTextWithLeadingNewline: " + result);
        assertEquals("Line1", result);
    }

    /**
     * 测试所有文本样式功能
     */
    @Test
    public void testAllStyles() {
        Text normal = Text.create("Normal");
        Text bold = Text.create("Bold", Text.Style.BOLD);
        Text italic = Text.create("Italic", Text.Style.ITALIC);
        Text underline = Text.create("Underline", Text.Style.UNDERLINE);
        Text strikeout = Text.create("Strikeout", Text.Style.STRIKEOUT);
        String normalResult = normal.toMarkdown();
        String boldResult = bold.toMarkdown();
        String italicResult = italic.toMarkdown();
        String underlineResult = underline.toMarkdown();
        String strikeoutResult = strikeout.toMarkdown();
        System.out.println("testAllStyles (normal): " + normalResult);
        System.out.println("testAllStyles (bold): " + boldResult);
        System.out.println("testAllStyles (italic): " + italicResult);
        System.out.println("testAllStyles (underline): " + underlineResult);
        System.out.println("testAllStyles (strikeout): " + strikeoutResult);
        assertEquals("Normal", normalResult);
        assertEquals("**Bold**", boldResult);
        assertEquals("*Italic*", italicResult);
        assertEquals("<u>Underline</u>", underlineResult);
        assertEquals("~~Strikeout~~", strikeoutResult);
    }
}
