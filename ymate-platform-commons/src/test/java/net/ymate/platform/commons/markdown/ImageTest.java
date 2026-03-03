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
 * Image类的单元测试，用于验证Markdown图片组件的各种功能。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-09 01:40:48
 * @since 2.1.4
 */
public class ImageTest {

    /**
     * 测试仅使用URL创建图片
     */
    @Test
    public void testCreateWithUrlOnly() {
        Image image = Image.create("https://example.com/image.jpg");
        assertNotNull(image);
        String result = image.toMarkdown();
        System.out.println("testCreateWithUrlOnly: " + result);
        assertEquals("![](https://example.com/image.jpg)", result);
    }

    /**
     * 测试使用alt文本和URL创建图片
     */
    @Test
    public void testCreateWithAltAndUrl() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg");
        assertNotNull(image);
        String result = image.toMarkdown();
        System.out.println("testCreateWithAltAndUrl: " + result);
        assertEquals("![Alt text](https://example.com/image.jpg)", result);
    }

    /**
     * 测试使用alt文本、URL和缩放比例创建图片
     */
    @Test
    public void testCreateWithAltUrlAndZoom() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 50);
        assertNotNull(image);
        String result = image.toMarkdown();
        System.out.println("testCreateWithAltUrlAndZoom: " + result);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:50%;\" />", result);
    }

    /**
     * 测试空URL的情况
     */
    @Test
    public void testEmptyUrl() {
        Image image = Image.create("Alt text", "");
        String result = image.toMarkdown();
        System.out.println("testEmptyUrl: " + result);
        assertEquals("", result);
    }

    /**
     * 测试null URL的情况
     */
    @Test
    public void testNullUrl() {
        Image image = Image.create("Alt text", (String) null);
        String result = image.toMarkdown();
        System.out.println("testNullUrl: " + result);
        assertEquals("", result);
    }

    /**
     * 测试空白URL的情况
     */
    @Test
    public void testBlankUrl() {
        Image image = Image.create("Alt text", "   ");
        String result = image.toMarkdown();
        System.out.println("testBlankUrl: " + result);
        assertEquals("", result);
    }

    /**
     * 测试空alt文本的情况
     */
    @Test
    public void testEmptyAlt() {
        Image image = Image.create("", "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testEmptyAlt: " + result);
        assertEquals("![](https://example.com/image.jpg)", result);
    }

    /**
     * 测试null alt文本的情况
     */
    @Test
    public void testNullAlt() {
        Image image = Image.create((String) null, "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testNullAlt: " + result);
        assertEquals("![](https://example.com/image.jpg)", result);
    }

    /**
     * 测试空白alt文本的情况
     */
    @Test
    public void testBlankAlt() {
        Image image = Image.create("   ", "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testBlankAlt: " + result);
        assertEquals("![](https://example.com/image.jpg)", result);
    }

    /**
     * 测试alt文本中包含空白字符的情况
     */
    @Test
    public void testWhitespaceInAlt() {
        Image image = Image.create("  Alt text  ", "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testWhitespaceInAlt: " + result);
        assertEquals("![Alt text](https://example.com/image.jpg)", result);
    }

    /**
     * 测试URL中包含空白字符的情况
     */
    @Test
    public void testWhitespaceInUrl() {
        Image image = Image.create("Alt text", "  https://example.com/image.jpg  ");
        String result = image.toMarkdown();
        System.out.println("testWhitespaceInUrl: " + result);
        assertEquals("![Alt text](https://example.com/image.jpg)", result);
    }

    /**
     * 测试缩放比例为0的情况
     */
    @Test
    public void testZoomZero() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 0);
        String result = image.toMarkdown();
        System.out.println("testZoomZero: " + result);
        assertEquals("![Alt text](https://example.com/image.jpg)", result);
    }

    /**
     * 测试负缩放比例的情况
     */
    @Test
    public void testZoomNegative() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", -10);
        String result = image.toMarkdown();
        System.out.println("testZoomNegative: " + result);
        assertEquals("![Alt text](https://example.com/image.jpg)", result);
    }

    /**
     * 测试最大缩放比例的情况
     */
    @Test
    public void testZoomMax() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 200);
        String result = image.toMarkdown();
        System.out.println("testZoomMax: " + result);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:200%;\" />", result);
    }

    /**
     * 测试超过最大缩放比例的情况
     */
    @Test
    public void testZoomOverMax() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 250);
        String result = image.toMarkdown();
        System.out.println("testZoomOverMax: " + result);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:200%;\" />", result);
    }

    /**
     * 测试缩放比例为50%的情况
     */
    @Test
    public void testZoomFifty() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 50);
        String result = image.toMarkdown();
        System.out.println("testZoomFifty: " + result);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:50%;\" />", result);
    }

    /**
     * 测试缩放比例为100%的情况
     */
    @Test
    public void testZoomHundred() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 100);
        String result = image.toMarkdown();
        System.out.println("testZoomHundred: " + result);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:100%;\" />", result);
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg");
        String result = image.toString();
        System.out.println("testToString: " + result);
        assertEquals("![Alt text](https://example.com/image.jpg)", result);
    }

    /**
     * 测试toMarkdown方法是否返回正确的markdown内容
     */
    @Test
    public void testToMarkdown() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testToMarkdown: " + result);
        assertEquals("![Alt text](https://example.com/image.jpg)", result);
    }

    /**
     * 测试包含特殊字符的图片
     */
    @Test
    public void testImageWithSpecialCharacters() {
        Image image = Image.create("Alt @#$%^&*()", "https://example.com/image.jpg?query=123");
        String result = image.toMarkdown();
        System.out.println("testImageWithSpecialCharacters: " + result);
        assertEquals("![Alt @#$%^&*()](https://example.com/image.jpg?query=123)", result);
    }

    /**
     * 测试包含Unicode字符的图片
     */
    @Test
    public void testImageWithUnicode() {
        Image image = Image.create("中文图片", "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testImageWithUnicode: " + result);
        assertEquals("![中文图片](https://example.com/image.jpg)", result);
    }

    /**
     * 测试alt文本和URL都为空的情况
     */
    @Test
    public void testImageWithEmptyBoth() {
        Image image = Image.create("", "");
        String result = image.toMarkdown();
        System.out.println("testImageWithEmptyBoth: " + result);
        assertEquals("", result);
    }

    /**
     * 测试alt文本和URL都为null的情况
     */
    @Test
    public void testImageWithNullBoth() {
        Image image = Image.create((String) null, (String) null);
        String result = image.toMarkdown();
        System.out.println("testImageWithNullBoth: " + result);
        assertEquals("", result);
    }

    /**
     * 测试空alt文本和缩放比例的情况
     */
    @Test
    public void testImageWithZoomAndEmptyAlt() {
        Image image = Image.create("", "https://example.com/image.jpg", 50);
        String result = image.toMarkdown();
        System.out.println("testImageWithZoomAndEmptyAlt: " + result);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"\" style=\"zoom:50%;\" />", result);
    }

    /**
     * 测试null alt文本和缩放比例的情况
     */
    @Test
    public void testImageWithZoomAndNullAlt() {
        Image image = Image.create((String) null, "https://example.com/image.jpg", 50);
        String result = image.toMarkdown();
        System.out.println("testImageWithZoomAndNullAlt: " + result);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"\" style=\"zoom:50%;\" />", result);
    }

    /**
     * 测试长URL的情况
     */
    @Test
    public void testImageWithLongUrl() {
        String longUrl = "https://example.com/path/to/image.jpg?query1=value1&query2=value2&query3=value3#fragment";
        Image image = Image.create("Long URL Image", longUrl);
        String result = image.toMarkdown();
        System.out.println("testImageWithLongUrl: " + result);
        assertEquals("![Long URL Image](" + longUrl + ")", result);
    }

    /**
     * 测试alt文本和URL相同的情况
     */
    @Test
    public void testImageWithAltSameAsUrl() {
        String url = "https://example.com/image.jpg";
        Image image = Image.create(url, url);
        String result = image.toMarkdown();
        System.out.println("testImageWithAltSameAsUrl: " + result);
        assertEquals("!" + "[" + url + "]" + "(" + url + ")", result);
    }

    /**
     * 测试单字符alt文本的情况
     */
    @Test
    public void testImageWithSingleCharacterAlt() {
        Image image = Image.create("A", "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testImageWithSingleCharacterAlt: " + result);
        assertEquals("![A](https://example.com/image.jpg)", result);
    }

    /**
     * 测试超长alt文本的情况
     */
    @Test
    public void testImageWithVeryLongAlt() {
        String longAlt = "This is a very long alt text that should be handled correctly by the Image class in the markdown library";
        Image image = Image.create(longAlt, "https://example.com/image.jpg");
        String result = image.toMarkdown();
        System.out.println("testImageWithVeryLongAlt: " + result);
        assertEquals("![" + longAlt + "](https://example.com/image.jpg)", result);
    }

    /**
     * 测试缩放格式的情况
     */
    @Test
    public void testImageWithZoomFormatting() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 75);
        String result = image.toMarkdown();
        System.out.println("testImageWithZoomFormatting: " + result);
        assertTrue(result.contains("zoom:75%;"));
        assertTrue(result.startsWith("<img"));
        assertTrue(result.endsWith(" />"));
    }
}
