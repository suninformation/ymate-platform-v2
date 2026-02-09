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
        assertEquals("![](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试使用alt文本和URL创建图片
     */
    @Test
    public void testCreateWithAltAndUrl() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg");
        assertNotNull(image);
        assertEquals("![Alt text](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试使用alt文本、URL和缩放比例创建图片
     */
    @Test
    public void testCreateWithAltUrlAndZoom() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 50);
        assertNotNull(image);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:50%;\" />", image.toMarkdown());
    }

    /**
     * 测试空URL的情况
     */
    @Test
    public void testEmptyUrl() {
        Image image = Image.create("Alt text", "");
        assertEquals("", image.toMarkdown());
    }

    /**
     * 测试null URL的情况
     */
    @Test
    public void testNullUrl() {
        Image image = Image.create("Alt text", (String) null);
        assertEquals("", image.toMarkdown());
    }

    /**
     * 测试空白URL的情况
     */
    @Test
    public void testBlankUrl() {
        Image image = Image.create("Alt text", "   ");
        assertEquals("", image.toMarkdown());
    }

    /**
     * 测试空alt文本的情况
     */
    @Test
    public void testEmptyAlt() {
        Image image = Image.create("", "https://example.com/image.jpg");
        assertEquals("![](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试null alt文本的情况
     */
    @Test
    public void testNullAlt() {
        Image image = Image.create((String) null, "https://example.com/image.jpg");
        assertEquals("![](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试空白alt文本的情况
     */
    @Test
    public void testBlankAlt() {
        Image image = Image.create("   ", "https://example.com/image.jpg");
        assertEquals("![](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试alt文本中包含空白字符的情况
     */
    @Test
    public void testWhitespaceInAlt() {
        Image image = Image.create("  Alt text  ", "https://example.com/image.jpg");
        assertEquals("![Alt text](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试URL中包含空白字符的情况
     */
    @Test
    public void testWhitespaceInUrl() {
        Image image = Image.create("Alt text", "  https://example.com/image.jpg  ");
        assertEquals("![Alt text](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试缩放比例为0的情况
     */
    @Test
    public void testZoomZero() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 0);
        assertEquals("![Alt text](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试负缩放比例的情况
     */
    @Test
    public void testZoomNegative() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", -10);
        assertEquals("![Alt text](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试最大缩放比例的情况
     */
    @Test
    public void testZoomMax() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 200);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:200%;\" />", image.toMarkdown());
    }

    /**
     * 测试超过最大缩放比例的情况
     */
    @Test
    public void testZoomOverMax() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 250);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:200%;\" />", image.toMarkdown());
    }

    /**
     * 测试缩放比例为50%的情况
     */
    @Test
    public void testZoomFifty() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 50);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:50%;\" />", image.toMarkdown());
    }

    /**
     * 测试缩放比例为100%的情况
     */
    @Test
    public void testZoomHundred() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 100);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"Alt text\" style=\"zoom:100%;\" />", image.toMarkdown());
    }

    /**
     * 测试toString方法是否返回正确的markdown内容
     */
    @Test
    public void testToString() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg");
        assertEquals("![Alt text](https://example.com/image.jpg)", image.toString());
    }

    /**
     * 测试toMarkdown方法是否返回正确的markdown内容
     */
    @Test
    public void testToMarkdown() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg");
        assertEquals("![Alt text](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试包含特殊字符的图片
     */
    @Test
    public void testImageWithSpecialCharacters() {
        Image image = Image.create("Alt @#$%^&*()", "https://example.com/image.jpg?query=123");
        assertEquals("![Alt @#$%^&*()](https://example.com/image.jpg?query=123)", image.toMarkdown());
    }

    /**
     * 测试包含Unicode字符的图片
     */
    @Test
    public void testImageWithUnicode() {
        Image image = Image.create("中文图片", "https://example.com/image.jpg");
        assertEquals("![中文图片](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试alt文本和URL都为空的情况
     */
    @Test
    public void testImageWithEmptyBoth() {
        Image image = Image.create("", "");
        assertEquals("", image.toMarkdown());
    }

    /**
     * 测试alt文本和URL都为null的情况
     */
    @Test
    public void testImageWithNullBoth() {
        Image image = Image.create((String) null, (String) null);
        assertEquals("", image.toMarkdown());
    }

    /**
     * 测试空alt文本和缩放比例的情况
     */
    @Test
    public void testImageWithZoomAndEmptyAlt() {
        Image image = Image.create("", "https://example.com/image.jpg", 50);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"\" style=\"zoom:50%;\" />", image.toMarkdown());
    }

    /**
     * 测试null alt文本和缩放比例的情况
     */
    @Test
    public void testImageWithZoomAndNullAlt() {
        Image image = Image.create((String) null, "https://example.com/image.jpg", 50);
        assertEquals("<img src=\"https://example.com/image.jpg\" alt=\"\" style=\"zoom:50%;\" />", image.toMarkdown());
    }

    /**
     * 测试长URL的情况
     */
    @Test
    public void testImageWithLongUrl() {
        String longUrl = "https://example.com/path/to/image.jpg?query1=value1&query2=value2&query3=value3#fragment";
        Image image = Image.create("Long URL Image", longUrl);
        assertEquals("![Long URL Image](" + longUrl + ")", image.toMarkdown());
    }

    /**
     * 测试alt文本和URL相同的情况
     */
    @Test
    public void testImageWithAltSameAsUrl() {
        String url = "https://example.com/image.jpg";
        Image image = Image.create(url, url);
        assertEquals("!" + "[" + url + "]" + "(" + url + ")", image.toMarkdown());
    }

    /**
     * 测试单字符alt文本的情况
     */
    @Test
    public void testImageWithSingleCharacterAlt() {
        Image image = Image.create("A", "https://example.com/image.jpg");
        assertEquals("![A](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试超长alt文本的情况
     */
    @Test
    public void testImageWithVeryLongAlt() {
        String longAlt = "This is a very long alt text that should be handled correctly by the Image class in the markdown library";
        Image image = Image.create(longAlt, "https://example.com/image.jpg");
        assertEquals("![" + longAlt + "](https://example.com/image.jpg)", image.toMarkdown());
    }

    /**
     * 测试缩放格式的情况
     */
    @Test
    public void testImageWithZoomFormatting() {
        Image image = Image.create("Alt text", "https://example.com/image.jpg", 75);
        String result = image.toMarkdown();
        assertTrue(result.contains("zoom:75%;"));
        assertTrue(result.startsWith("<img"));
        assertTrue(result.endsWith(" />"));
    }
}
