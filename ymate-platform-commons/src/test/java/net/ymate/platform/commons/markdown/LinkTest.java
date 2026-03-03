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
 * Link类的单元测试，用于验证Markdown链接组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:40:05
 * @since 2.1.4
 */
public class LinkTest {

    /**
     * 测试仅使用URL创建链接
     */
    @Test
    public void testCreateWithUrlOnly() {
        Link link = Link.create("https://example.com");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testCreateWithUrlOnly: " + result);
        assertEquals("[https://example.com](https://example.com)", result);
    }

    /**
     * 测试使用名称和URL创建链接
     */
    @Test
    public void testCreateWithNameAndUrl() {
        Link link = Link.create("Example", "https://example.com");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testCreateWithNameAndUrl: " + result);
        assertEquals("[Example](https://example.com)", result);
    }

    /**
     * 测试使用IMarkdown对象作为名称创建链接
     */
    @Test
    public void testCreateWithIMarkdownName() {
        Text name = Text.create("Example");
        Link link = Link.create(name, "https://example.com");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testCreateWithIMarkdownName: " + result);
        assertEquals("[Example](https://example.com)", result);
    }

    /**
     * 测试空URL的情况
     */
    @Test
    public void testEmptyUrl() {
        Link link = Link.create("Example", "");
        String result = link.toMarkdown();
        System.out.println("testEmptyUrl: " + result);
        assertEquals("", result);
    }

    /**
     * 测试null URL的情况
     */
    @Test
    public void testNullUrl() {
        Link link = Link.create("Example", (String) null);
        String result = link.toMarkdown();
        System.out.println("testNullUrl: " + result);
        assertEquals("", result);
    }

    /**
     * 测试空白URL的情况
     */
    @Test
    public void testBlankUrl() {
        Link link = Link.create("Example", "   ");
        String result = link.toMarkdown();
        System.out.println("testBlankUrl: " + result);
        assertEquals("", result);
    }

    /**
     * 测试空名称的情况
     */
    @Test
    public void testEmptyName() {
        Link link = Link.create("", "https://example.com");
        String result = link.toMarkdown();
        System.out.println("testEmptyName: " + result);
        assertEquals("[https://example.com](https://example.com)", result);
    }

    /**
     * 测试null名称的情况
     */
    @Test
    public void testNullName() {
        Link link = Link.create((String) null, "https://example.com");
        String result = link.toMarkdown();
        System.out.println("testNullName: " + result);
        assertEquals("[https://example.com](https://example.com)", result);
    }

    /**
     * 测试空白名称的情况
     */
    @Test
    public void testBlankName() {
        Link link = Link.create("   ", "https://example.com");
        String result = link.toMarkdown();
        System.out.println("testBlankName: " + result);
        assertEquals("[https://example.com](https://example.com)", result);
    }

    /**
     * 测试名称中包含空白字符的情况
     */
    @Test
    public void testWhitespaceInName() {
        Link link = Link.create("  Example  ", "https://example.com");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testWhitespaceInName: " + result);
        assertEquals("[Example](https://example.com)", result);
    }

    /**
     * 测试URL中包含空白字符的情况
     */
    @Test
    public void testWhitespaceInUrl() {
        Link link = Link.create("Example", "  https://example.com  ");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testWhitespaceInUrl: " + result);
        assertEquals("[Example](https://example.com)", result);
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        Link link = Link.create("Example", "https://example.com");
        assertNotNull(link);
        String result = link.toString();
        System.out.println("testToString: " + result);
        assertEquals("[Example](https://example.com)", result);
    }

    /**
     * 测试toMarkdown方法是否返回正确的markdown内容
     */
    @Test
    public void testToMarkdown() {
        Link link = Link.create("Example", "https://example.com");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testToMarkdown: " + result);
        assertEquals("[Example](https://example.com)", result);
    }

    /**
     * 测试包含特殊字符的链接
     */
    @Test
    public void testLinkWithSpecialCharacters() {
        Link link = Link.create("Example @#$%^&*()", "https://example.com/path?query=123");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testLinkWithSpecialCharacters: " + result);
        assertEquals("[Example @#$%^&*()](https://example.com/path?query=123)", result);
    }

    /**
     * 测试包含Unicode字符的链接
     */
    @Test
    public void testLinkWithUnicode() {
        Link link = Link.create("中文链接", "https://example.com");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testLinkWithUnicode: " + result);
        assertEquals("[中文链接](https://example.com)", result);
    }

    /**
     * 测试名称和URL都为空的情况
     */
    @Test
    public void testLinkWithEmptyBoth() {
        Link link = Link.create("", "");
        String result = link.toMarkdown();
        System.out.println("testLinkWithEmptyBoth: " + result);
        assertEquals("", result);
    }

    /**
     * 测试名称和URL都为null的情况
     */
    @Test
    public void testLinkWithNullBoth() {
        Link link = Link.create((String) null, (String) null);
        String result = link.toMarkdown();
        System.out.println("testLinkWithNullBoth: " + result);
        assertEquals("", result);
    }

    /**
     * 测试名称和URL相同的情况
     */
    @Test
    public void testLinkWithNameSameAsUrl() {
        String url = "https://example.com";
        Link link = Link.create(url, url);
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testLinkWithNameSameAsUrl: " + result);
        assertEquals("[" + url + "]" + "(" + url + ")", result);
    }

    /**
     * 测试长URL的情况
     */
    @Test
    public void testLinkWithLongUrl() {
        String longUrl = "https://example.com/path/to/resource?query1=value1&query2=value2&query3=value3#fragment";
        Link link = Link.create("Long URL", longUrl);
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testLinkWithLongUrl: " + result);
        assertEquals("[Long URL](" + longUrl + ")", result);
    }

    /**
     * 测试名称中包含Markdown的情况
     */
    @Test
    public void testLinkWithMarkdownInName() {
        Text name = Text.create("Bold Example");
        Link link = Link.create(name, "https://example.com");
        assertNotNull(link);
        String result = link.toMarkdown();
        System.out.println("testLinkWithMarkdownInName: " + result);
        assertEquals("[Bold Example](https://example.com)", result);
    }
}
