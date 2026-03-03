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
 * Code类的单元测试，用于验证Markdown代码块组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:39:36
 * @since 2.1.4
 */
public class CodeTest {

    /**
     * 测试使用字符串创建内联代码
     */
    @Test
    public void testCreateWithString() {
        Code code = Code.create("code");
        assertNotNull(code);
        String result = code.toMarkdown();
        System.out.println("testCreateWithString: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试使用IMarkdown对象创建内联代码
     */
    @Test
    public void testCreateWithIMarkdown() {
        Text text = Text.create("code");
        Code code = Code.create(text);
        assertNotNull(code);
        String result = code.toMarkdown();
        System.out.println("testCreateWithIMarkdown: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试使用字符串和语言创建代码块
     */
    @Test
    public void testCreateWithStringAndLanguage() {
        Code code = Code.create("code", "java");
        assertNotNull(code);
        String result = code.toMarkdown();
        System.out.println("testCreateWithStringAndLanguage: " + result);
        assertEquals("```java\ncode\n```\n", result);
    }

    /**
     * 测试使用IMarkdown对象和语言创建代码块
     */
    @Test
    public void testCreateWithIMarkdownAndLanguage() {
        Text text = Text.create("code");
        Code code = Code.create(text, "python");
        assertNotNull(code);
        String result = code.toMarkdown();
        System.out.println("testCreateWithIMarkdownAndLanguage: " + result);
        assertEquals("```python\ncode\n```\n", result);
    }

    /**
     * 测试空字符串代码
     */
    @Test
    public void testEmptyCode() {
        Code code = Code.create("");
        String result = code.toMarkdown();
        System.out.println("testEmptyCode: " + result);
        assertEquals("", result);
    }

    /**
     * 测试null代码
     */
    @Test
    public void testNullCode() {
        Code code = Code.create((String) null);
        String result = code.toMarkdown();
        System.out.println("testNullCode: " + result);
        assertEquals("", result);
    }

    /**
     * 测试内联代码格式
     */
    @Test
    public void testInlineCode() {
        Code code = Code.create("inline code");
        String result = code.toMarkdown();
        System.out.println("testInlineCode: " + result);
        assertEquals("`inline code`", result);
    }

    /**
     * 测试包含换行符的代码块
     */
    @Test
    public void testCodeBlockWithNewline() {
        Code code = Code.create("line1\nline2");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithNewline: " + result);
        assertEquals("```\nline1\nline2\n```\n", result);
    }

    /**
     * 测试包含多个换行符的代码块
     */
    @Test
    public void testCodeBlockWithMultipleNewlines() {
        Code code = Code.create("line1\nline2\nline3");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithMultipleNewlines: " + result);
        assertTrue(result.startsWith("```"));
        assertTrue(result.endsWith("```\n"));
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
        assertTrue(result.contains("line3"));
    }

    /**
     * 测试带语言指定的代码块
     */
    @Test
    public void testCodeBlockWithLanguage() {
        Code code = Code.create("public class Test {}", "java");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithLanguage: " + result);
        assertEquals("```java\npublic class Test {}\n```\n", result);
    }

    /**
     * 测试空语言代码块
     */
    @Test
    public void testCodeBlockWithEmptyLanguage() {
        Code code = Code.create("code", "");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithEmptyLanguage: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试null语言代码
     */
    @Test
    public void testCodeBlockWithNullLanguage() {
        Code code = Code.create("code", null);
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithNullLanguage: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试空白语言代码块
     */
    @Test
    public void testCodeBlockWithWhitespaceLanguage() {
        Code code = Code.create("code", "  ");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithWhitespaceLanguage: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试追加字符串到代码
     */
    @Test
    public void testAppendString() {
        Code code = Code.create("line1");
        code.append("line2");
        String result = code.toMarkdown();
        System.out.println("testAppendString: " + result);
        assertEquals("`line1line2`", result);
    }

    /**
     * 测试多次追加操作
     */
    @Test
    public void testMultipleAppends() {
        Code code = Code.create("line1");
        code.append("line2").append("line3");
        String result = code.toMarkdown();
        System.out.println("testMultipleAppends: " + result);
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
        assertTrue(result.contains("line3"));
    }

    /**
     * 测试包含特殊字符的代码
     */
    @Test
    public void testCodeWithSpecialCharacters() {
        Code code = Code.create("code @#$%^&*()");
        String result = code.toMarkdown();
        System.out.println("testCodeWithSpecialCharacters: " + result);
        assertEquals("`code @#$%^&*()`", result);
    }

    /**
     * 测试包含反引号的代码
     */
    @Test
    public void testCodeWithBackticks() {
        Code code = Code.create("code `with` backticks");
        String result = code.toMarkdown();
        System.out.println("testCodeWithBackticks: " + result);
        assertEquals("`code `with` backticks`", result);
    }

    /**
     * 测试包含Unicode字符的代码
     */
    @Test
    public void testCodeWithUnicode() {
        Code code = Code.create("中文代码");
        String result = code.toMarkdown();
        System.out.println("testCodeWithUnicode: " + result);
        assertEquals("`中文代码`", result);
    }

    /**
     * 测试包含数字的代码
     */
    @Test
    public void testCodeWithNumbers() {
        Code code = Code.create("12345");
        String result = code.toMarkdown();
        System.out.println("testCodeWithNumbers: " + result);
        assertEquals("`12345`", result);
    }

    /**
     * 测试包含制表符的代码
     */
    @Test
    public void testCodeWithTabs() {
        Code code = Code.create("code\twith\ttabs");
        String result = code.toMarkdown();
        System.out.println("testCodeWithTabs: " + result);
        assertEquals("`code\twith\ttabs`", result);
    }

    /**
     * 测试包含回车符的代码
     */
    @Test
    public void testCodeWithCarriageReturn() {
        Code code = Code.create("line1\rline2");
        String result = code.toMarkdown();
        System.out.println("testCodeWithCarriageReturn: " + result);
        assertTrue(result.startsWith("`") && result.endsWith("`"));
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
    }

    /**
     * 测试包含CRLF的代码
     */
    @Test
    public void testCodeWithCRLF() {
        Code code = Code.create("line1\r\nline2");
        String result = code.toMarkdown();
        System.out.println("testCodeWithCRLF: " + result);
        assertEquals("```\nline1\r\nline2\n```\n", result);
    }

    /**
     * 测试toString方法
     */
    @Test
    public void testToString() {
        Code code = Code.create("code");
        String result = code.toString();
        System.out.println("testToString: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试toMarkdown方法
     */
    @Test
    public void testToMarkdown() {
        Code code = Code.create("code");
        String result = code.toMarkdown();
        System.out.println("testToMarkdown: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试代码构建器模式
     */
    @Test
    public void testCodeBuilderPattern() {
        Code code = Code.create("line1")
                .append("line2")
                .append("line3");
        String result = code.toMarkdown();
        System.out.println("testCodeBuilderPattern: " + result);
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
        assertTrue(result.contains("line3"));
    }

    /**
     * 测试带语言和换行的代码块
     */
    @Test
    public void testCodeBlockWithLanguageAndNewline() {
        Code code = Code.create("line1\nline2", "java");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithLanguageAndNewline: " + result);
        assertTrue(result.startsWith("```java"));
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
        assertTrue(result.endsWith("```\n"));
    }

    /**
     * 测试多行带语言的代码块
     */
    @Test
    public void testCodeBlockWithMultipleLinesAndLanguage() {
        Code code = Code.create("public class Test {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}", "java");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithMultipleLinesAndLanguage: " + result);
        assertTrue(result.startsWith("```java"));
        assertTrue(result.contains("public class Test"));
        assertTrue(result.contains("System.out.println"));
        assertTrue(result.endsWith("```\n"));
    }

    /**
     * 测试带前后空白的代码
     */
    @Test
    public void testCodeWithLeadingWhitespace() {
        Code code = Code.create("  code  ");
        String result = code.toMarkdown();
        System.out.println("testCodeWithLeadingWhitespace: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试带前导空白的代码块
     */
    @Test
    public void testCodeBlockWithLeadingWhitespace() {
        Code code = Code.create("  line1\n  line2", "java");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithLeadingWhitespace: " + result);
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
    }

    /**
     * 测试包含空行的代码
     */
    @Test
    public void testCodeWithEmptyLines() {
        Code code = Code.create("line1\n\nline3");
        String result = code.toMarkdown();
        System.out.println("testCodeWithEmptyLines: " + result);
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line3"));
    }

    /**
     * 测试只有换行符的代码
     */
    @Test
    public void testCodeWithOnlyNewline() {
        Code code = Code.create("\n");
        String result = code.toMarkdown();
        System.out.println("testCodeWithOnlyNewline: " + result);
        assertEquals("", result);
    }

    /**
     * 测试带尾随换行符的代码
     */
    @Test
    public void testCodeWithTrailingNewline() {
        Code code = Code.create("code\n");
        String result = code.toMarkdown();
        System.out.println("testCodeWithTrailingNewline: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试带前导换行符的代码
     */
    @Test
    public void testCodeWithLeadingNewline() {
        Code code = Code.create("\ncode");
        String result = code.toMarkdown();
        System.out.println("testCodeWithLeadingNewline: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试代码块语言的大小写敏感性
     */
    @Test
    public void testCodeBlockLanguageCase() {
        Code code1 = Code.create("code", "Java");
        Code code2 = Code.create("code", "JAVA");
        Code code3 = Code.create("code", "java");

        String result1 = code1.toMarkdown();
        String result2 = code2.toMarkdown();
        String result3 = code3.toMarkdown();
        System.out.println("testCodeBlockLanguageCase - code1: " + result1);
        System.out.println("testCodeBlockLanguageCase - code2: " + result2);
        System.out.println("testCodeBlockLanguageCase - code3: " + result3);
        assertTrue(result1.contains("```Java"));
        assertTrue(result2.contains("```JAVA"));
        assertTrue(result3.contains("```java"));
    }

    /**
     * 测试特殊语言代码块
     */
    @Test
    public void testCodeBlockWithSpecialLanguage() {
        Code code = Code.create("code", "c++");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithSpecialLanguage: " + result);
        assertEquals("```c++\ncode\n```\n", result);
    }

    /**
     * 测试带连字符的语言代码块
     */
    @Test
    public void testCodeBlockWithHyphenatedLanguage() {
        Code code = Code.create("code", "java-script");
        String result = code.toMarkdown();
        System.out.println("testCodeBlockWithHyphenatedLanguage: " + result);
        assertEquals("```java-script\ncode\n```\n", result);
    }
}
