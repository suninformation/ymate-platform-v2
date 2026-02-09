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
package net.ymate.platform.commons.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * MimeTypeUtils 单元测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 01:39:36
 * @since 2.1.4
 */
public class MimeTypeUtilsTest {

    @BeforeClass
    public static void setUp() {
    }

    /**
     * 测试通过文件扩展名获取 MIME 类型 - 正常情况
     */
    @Test
    public void testGetFileMimeTypeByExtName() {
        // 测试已知扩展名
        Assert.assertEquals("text/plain", MimeTypeUtils.getFileMimeType("txt"));
        Assert.assertEquals("text/html", MimeTypeUtils.getFileMimeType("html"));
        Assert.assertEquals("text/css", MimeTypeUtils.getFileMimeType("css"));
        Assert.assertEquals("text/javascript", MimeTypeUtils.getFileMimeType("js"));
        Assert.assertEquals("application/json", MimeTypeUtils.getFileMimeType("json"));
        Assert.assertEquals("image/jpeg", MimeTypeUtils.getFileMimeType("jpg"));
        Assert.assertEquals("image/png", MimeTypeUtils.getFileMimeType("png"));
        Assert.assertEquals("application/pdf", MimeTypeUtils.getFileMimeType("pdf"));
    }

    /**
     * 测试通过带点的文件扩展名获取 MIME 类型
     */
    @Test
    public void testGetFileMimeTypeByExtNameWithDot() {
        Assert.assertEquals("text/plain", MimeTypeUtils.getFileMimeType(".txt"));
        Assert.assertEquals("text/html", MimeTypeUtils.getFileMimeType(".html"));
    }

    /**
     * 测试通过文件扩展名获取 MIME 类型 - 大小写不敏感
     */
    @Test
    public void testGetFileMimeTypeByExtNameCaseInsensitive() {
        Assert.assertEquals("text/plain", MimeTypeUtils.getFileMimeType("TXT"));
        Assert.assertEquals("text/plain", MimeTypeUtils.getFileMimeType("Txt"));
        Assert.assertEquals("image/jpeg", MimeTypeUtils.getFileMimeType("JPG"));
        Assert.assertEquals("image/jpeg", MimeTypeUtils.getFileMimeType("Jpg"));
    }

    /**
     * 测试通过不存在的文件扩展名获取 MIME 类型
     */
    @Test
    public void testGetFileMimeTypeByNonExistentExtName() {
        Assert.assertNull(MimeTypeUtils.getFileMimeType("nonexistentextension"));
        Assert.assertNull(MimeTypeUtils.getFileMimeType(".nonexistentextension"));
    }

    /**
     * 测试通过 null 或空字符串获取 MIME 类型
     */
    @Test
    public void testGetFileMimeTypeByNullOrEmptyExtName() {
        // 明确调用 String 参数版本的方法
        String nullStr = null;
        Assert.assertNull(MimeTypeUtils.getFileMimeType(nullStr));
        Assert.assertNull(MimeTypeUtils.getFileMimeType(""));
        Assert.assertNull(MimeTypeUtils.getFileMimeType("   "));
    }

    /**
     * 测试通过文件对象获取 MIME 类型
     */
    @Test
    public void testGetFileMimeTypeByFile() throws IOException {
        // 创建临时文件进行测试
        File txtFile = File.createTempFile("test", ".txt");
        try {
            Assert.assertEquals("text/plain", MimeTypeUtils.getFileMimeType(txtFile));
        } finally {
            txtFile.delete();
        }

        File htmlFile = File.createTempFile("test", ".html");
        try {
            Assert.assertEquals("text/html", MimeTypeUtils.getFileMimeType(htmlFile));
        } finally {
            htmlFile.delete();
        }

        // 测试无扩展名的文件
        File noExtFile = File.createTempFile("test", "");
        try {
            Assert.assertNull(MimeTypeUtils.getFileMimeType(noExtFile));
        } finally {
            noExtFile.delete();
        }
    }

    /**
     * 测试通过 null 文件对象获取 MIME 类型
     */
    @Test
    public void testGetFileMimeTypeByNullFile() {
        Assert.assertNull(MimeTypeUtils.getFileMimeType((File) null));
    }

    /**
     * 测试通过 MIME 类型获取文件扩展名 - 正常情况
     */
    @Test
    public void testGetFileExtNameByMimeType() {
        // 测试已知 MIME 类型
        Assert.assertEquals("txt", MimeTypeUtils.getFileExtName("text/plain"));
        Assert.assertEquals("html", MimeTypeUtils.getFileExtName("text/html"));
        Assert.assertEquals("css", MimeTypeUtils.getFileExtName("text/css"));
        Assert.assertEquals("js", MimeTypeUtils.getFileExtName("text/javascript"));
        Assert.assertEquals("json", MimeTypeUtils.getFileExtName("application/json"));
        Assert.assertEquals("jpeg", MimeTypeUtils.getFileExtName("image/jpeg"));
        Assert.assertEquals("png", MimeTypeUtils.getFileExtName("image/png"));
        Assert.assertEquals("pdf", MimeTypeUtils.getFileExtName("application/pdf"));
    }

    /**
     * 测试通过 MIME 类型获取文件扩展名 - 大小写不敏感
     */
    @Test
    public void testGetFileExtNameByMimeTypeCaseInsensitive() {
        Assert.assertEquals("txt", MimeTypeUtils.getFileExtName("TEXT/PLAIN"));
        Assert.assertEquals("txt", MimeTypeUtils.getFileExtName("Text/Plain"));
        Assert.assertEquals("jpeg", MimeTypeUtils.getFileExtName("IMAGE/JPEG"));
        Assert.assertEquals("jpeg", MimeTypeUtils.getFileExtName("Image/Jpeg"));
    }

    /**
     * 测试通过不存在的 MIME 类型获取文件扩展名
     */
    @Test
    public void testGetFileExtNameByNonExistentMimeType() {
        Assert.assertNull(MimeTypeUtils.getFileExtName("application/nonexistent"));
        Assert.assertNull(MimeTypeUtils.getFileExtName("image/nonexistent"));
    }

    /**
     * 测试通过 null 或空字符串获取文件扩展名
     */
    @Test
    public void testGetFileExtNameByNullOrEmptyMimeType() {
        Assert.assertNull(MimeTypeUtils.getFileExtName(null));
        Assert.assertNull(MimeTypeUtils.getFileExtName(""));
        Assert.assertNull(MimeTypeUtils.getFileExtName("   "));
    }

    /**
     * 测试 MIME 类型和文件扩展名的双向映射关系
     */
    @Test
    public void testBidirectionalMapping() {
        // 测试常见的双向映射关系
        String txtExt = "txt";
        String txtMime = MimeTypeUtils.getFileMimeType(txtExt);
        Assert.assertNotNull(txtMime);
        Assert.assertEquals(txtExt, MimeTypeUtils.getFileExtName(txtMime));

        String htmlExt = "html";
        String htmlMime = MimeTypeUtils.getFileMimeType(htmlExt);
        Assert.assertNotNull(htmlMime);
        Assert.assertEquals(htmlExt, MimeTypeUtils.getFileExtName(htmlMime));

        String cssExt = "css";
        String cssMime = MimeTypeUtils.getFileMimeType(cssExt);
        Assert.assertNotNull(cssMime);
        Assert.assertEquals(cssExt, MimeTypeUtils.getFileExtName(cssMime));

        String jsExt = "js";
        String jsMime = MimeTypeUtils.getFileMimeType(jsExt);
        Assert.assertNotNull(jsMime);
        Assert.assertEquals(jsExt, MimeTypeUtils.getFileExtName(jsMime));

        String jsonExt = "json";
        String jsonMime = MimeTypeUtils.getFileMimeType(jsonExt);
        Assert.assertNotNull(jsonMime);
        Assert.assertEquals(jsonExt, MimeTypeUtils.getFileExtName(jsonMime));
    }
}
