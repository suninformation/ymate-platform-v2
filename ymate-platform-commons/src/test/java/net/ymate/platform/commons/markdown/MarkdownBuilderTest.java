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
 * MarkdownBuilder类的单元测试，用于验证Markdown构建器的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:37:58
 * @since 2.1.4
 */
public class MarkdownBuilderTest {

    /**
     * 测试创建MarkdownBuilder实例
     */
    @Test
    public void testCreate() {
        MarkdownBuilder builder = MarkdownBuilder.create();
        assertNotNull(builder);
        assertEquals(0, builder.length());
    }

    /**
     * 测试换行功能
     */
    @Test
    public void testBr() {
        MarkdownBuilder builder = MarkdownBuilder.create().br();
        String result = builder.toMarkdown();
        System.out.println("testBr: " + result);
        assertEquals("\n", result);
    }

    /**
     * 测试段落分隔功能
     */
    @Test
    public void testP() {
        MarkdownBuilder builder = MarkdownBuilder.create().p();
        String result = builder.toMarkdown();
        System.out.println("testP: " + result);
        assertEquals("\n\n", result);
    }

    /**
     * 测试带有重复次数的段落分隔功能
     */
    @Test
    public void testPWithRepeat() {
        MarkdownBuilder builder = MarkdownBuilder.create().p(3);
        String result = builder.toMarkdown();
        System.out.println("testPWithRepeat: " + result);
        assertEquals("\n\n\n", result);
    }

    /**
     * 测试重复次数为0的段落分隔功能
     */
    @Test
    public void testPWithZeroRepeat() {
        MarkdownBuilder builder = MarkdownBuilder.create().p(0);
        String result = builder.toMarkdown();
        System.out.println("testPWithZeroRepeat: " + result);
        assertEquals("\n", result);
    }

    /**
     * 测试重复次数为负数的段落分隔功能
     */
    @Test
    public void testPWithNegativeRepeat() {
        MarkdownBuilder builder = MarkdownBuilder.create().p(-1);
        String result = builder.toMarkdown();
        System.out.println("testPWithNegativeRepeat: " + result);
        assertEquals("\n", result);
    }

    /**
     * 测试水平分隔线功能
     */
    @Test
    public void testHr() {
        MarkdownBuilder builder = MarkdownBuilder.create().hr();
        String result = builder.toMarkdown();
        System.out.println("testHr: " + result);
        assertEquals("------\n\n\n", result);
    }

    /**
     * 测试空格功能
     */
    @Test
    public void testSpace() {
        MarkdownBuilder builder = MarkdownBuilder.create().space();
        String result = builder.toMarkdown();
        System.out.println("testSpace: " + result);
        assertEquals(" ", result);
    }

    /**
     * 测试带有重复次数的空格功能
     */
    @Test
    public void testSpaceWithRepeat() {
        MarkdownBuilder builder = MarkdownBuilder.create().space(3);
        String result = builder.toMarkdown();
        System.out.println("testSpaceWithRepeat: " + result);
        assertEquals("   ", result);
    }

    /**
     * 测试重复次数为0的空格功能
     */
    @Test
    public void testSpaceWithZeroRepeat() {
        MarkdownBuilder builder = MarkdownBuilder.create().space(0);
        String result = builder.toMarkdown();
        System.out.println("testSpaceWithZeroRepeat: " + result);
        assertEquals(" ", result);
    }

    /**
     * 测试制表符功能
     */
    @Test
    public void testTab() {
        MarkdownBuilder builder = MarkdownBuilder.create().tab();
        String result = builder.toMarkdown();
        System.out.println("testTab: " + result);
        assertEquals("    ", result);
    }

    /**
     * 测试获取内容长度功能
     */
    @Test
    public void testLength() {
        MarkdownBuilder builder = MarkdownBuilder.create().append("test");
        assertEquals(4, builder.length());
    }

    /**
     * 测试追加IMarkdown对象功能
     */
    @Test
    public void testAppendIMarkdown() {
        MarkdownBuilder builder = MarkdownBuilder.create();
        Title title = Title.create("Test Title");
        builder.append(title);
        String result = builder.toMarkdown();
        System.out.println("testAppendIMarkdown: " + result);
        assertEquals("# Test Title", result);
    }

    /**
     * 测试追加字符串功能
     */
    @Test
    public void testAppendString() {
        MarkdownBuilder builder = MarkdownBuilder.create().append("Hello");
        String result = builder.toMarkdown();
        System.out.println("testAppendString: " + result);
        assertEquals("Hello", result);
    }

    /**
     * 测试使用字符串创建标题功能
     */
    @Test
    public void testTitleWithString() {
        MarkdownBuilder builder = MarkdownBuilder.create().title("Test");
        String result = builder.toMarkdown();
        System.out.println("testTitleWithString: " + result);
        assertEquals("# Test", result);
    }

    /**
     * 测试使用IMarkdown对象创建标题功能
     */
    @Test
    public void testTitleWithIMarkdown() {
        MarkdownBuilder builder = MarkdownBuilder.create();
        Text text = Text.create("Test Title");
        builder.title(text);
        String result = builder.toMarkdown();
        System.out.println("testTitleWithIMarkdown: " + result);
        assertEquals("# Test Title", result);
    }

    /**
     * 测试创建指定级别的标题功能
     */
    @Test
    public void testTitleWithLevel() {
        MarkdownBuilder builder = MarkdownBuilder.create().title("Test", 3);
        String result = builder.toMarkdown();
        System.out.println("testTitleWithLevel: " + result);
        assertEquals("### Test", result);
    }

    /**
     * 测试使用字符串创建文本功能
     */
    @Test
    public void testTextWithString() {
        MarkdownBuilder builder = MarkdownBuilder.create().text("Hello");
        String result = builder.toMarkdown();
        System.out.println("testTextWithString: " + result);
        assertEquals("Hello", result);
    }

    /**
     * 测试使用指定样式创建文本功能
     */
    @Test
    public void testTextWithStyle() {
        MarkdownBuilder builder = MarkdownBuilder.create().text("Bold", Text.Style.BOLD);
        String result = builder.toMarkdown();
        System.out.println("testTextWithStyle: " + result);
        assertEquals("**Bold**", result);
    }

    /**
     * 测试使用字符串创建引用功能
     */
    @Test
    public void testQuoteWithString() {
        MarkdownBuilder builder = MarkdownBuilder.create().quote("Quote text");
        String result = builder.toMarkdown();
        System.out.println("testQuoteWithString: " + result);
        assertEquals("> Quote text\n> \n", result);
    }

    /**
     * 测试使用IMarkdown对象创建引用功能
     */
    @Test
    public void testQuoteWithIMarkdown() {
        MarkdownBuilder builder = MarkdownBuilder.create();
        Text text = Text.create("Quote text");
        builder.quote(text);
        String result = builder.toMarkdown();
        System.out.println("testQuoteWithIMarkdown: " + result);
        assertEquals("> Quote text\n> \n", result);
    }

    /**
     * 测试创建链接功能
     */
    @Test
    public void testLink() {
        MarkdownBuilder builder = MarkdownBuilder.create().link("GitHub", "https://github.com");
        String result = builder.toMarkdown();
        System.out.println("testLink: " + result);
        assertEquals("[GitHub](https://github.com)", result);
    }

    /**
     * 测试使用IMarkdown对象创建链接功能
     */
    @Test
    public void testLinkWithIMarkdown() {
        MarkdownBuilder builder = MarkdownBuilder.create();
        Text text = Text.create("GitHub");
        builder.link(text, "https://github.com");
        String result = builder.toMarkdown();
        System.out.println("testLinkWithIMarkdown: " + result);
        assertEquals("[GitHub](https://github.com)", result);
    }

    /**
     * 测试仅使用URL创建图片功能
     */
    @Test
    public void testImage() {
        MarkdownBuilder builder = MarkdownBuilder.create().image("https://example.com/image.png");
        String result = builder.toMarkdown();
        System.out.println("testImage: " + result);
        assertEquals("![](https://example.com/image.png)", result);
    }

    /**
     * 测试使用alt文本和URL创建图片功能
     */
    @Test
    public void testImageWithAlt() {
        MarkdownBuilder builder = MarkdownBuilder.create().image("Alt text", "https://example.com/image.png");
        String result = builder.toMarkdown();
        System.out.println("testImageWithAlt: " + result);
        assertEquals("![Alt text](https://example.com/image.png)", result);
    }

    /**
     * 测试使用alt文本、URL和缩放比例创建图片功能
     */
    @Test
    public void testImageWithZoom() {
        MarkdownBuilder builder = MarkdownBuilder.create().image("Alt", "https://example.com/image.png", 50);
        String result = builder.toMarkdown();
        System.out.println("testImageWithZoom: " + result);
        assertEquals("<img src=\"https://example.com/image.png\" alt=\"Alt\" style=\"zoom:50%;\" />", result);
    }

    /**
     * 测试创建内联代码功能
     */
    @Test
    public void testCode() {
        MarkdownBuilder builder = MarkdownBuilder.create().code("code");
        String result = builder.toMarkdown();
        System.out.println("testCode: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试使用指定语言创建代码块功能
     */
    @Test
    public void testCodeWithLanguage() {
        MarkdownBuilder builder = MarkdownBuilder.create().code("code", "java");
        String result = builder.toMarkdown();
        System.out.println("testCodeWithLanguage: " + result);
        assertEquals("```java\ncode\n```\n", result);
    }

    /**
     * 测试使用IMarkdown对象创建代码功能
     */
    @Test
    public void testCodeWithIMarkdown() {
        MarkdownBuilder builder = MarkdownBuilder.create();
        Text text = Text.create("code");
        builder.code(text);
        String result = builder.toMarkdown();
        System.out.println("testCodeWithIMarkdown: " + result);
        assertEquals("`code`", result);
    }

    /**
     * 测试方法链式调用功能
     */
    @Test
    public void testChaining() {
        String result = MarkdownBuilder.create()
                .title("Title")
                .p()
                .text("Content")
                .toMarkdown();
        System.out.println("testChaining: " + result);
        assertEquals("# Title\n\nContent", result);
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        MarkdownBuilder builder = MarkdownBuilder.create().append("test");
        String result = builder.toString();
        System.out.println("testToString: " + result);
        assertEquals("test", result);
    }

    /**
     * 测试toMarkdown方法是否返回正确的markdown内容
     */
    @Test
    public void testToMarkdown() {
        MarkdownBuilder builder = MarkdownBuilder.create().append("test");
        String result = builder.toMarkdown();
        System.out.println("testToMarkdown: " + result);
        assertEquals("test", result);
    }

    /**
     * 测试空构建器的情况
     */
    @Test
    public void testEmptyBuilder() {
        MarkdownBuilder builder = MarkdownBuilder.create();
        String result = builder.toMarkdown();
        System.out.println("testEmptyBuilder: " + result);
        assertEquals("", result);
    }

    /**
     * 测试多种操作组合使用功能
     */
    @Test
    public void testMultipleOperations() {
        String result = MarkdownBuilder.create()
                .title("Main Title", 1)
                .p()
                .text("Paragraph 1")
                .p()
                .text("Paragraph 2", Text.Style.BOLD)
                .p()
                .quote("A quote")
                .p()
                .code("System.out.println(\"Hello\");", "java")
                .toMarkdown();
        System.out.println("testMultipleOperations: " + result);
        assertTrue(result.contains("# Main Title"));
        assertTrue(result.contains("Paragraph 1"));
        assertTrue(result.contains("**Paragraph 2**"));
        assertTrue(result.contains("> A quote"));
        assertTrue(result.contains("```java"));
    }
}
