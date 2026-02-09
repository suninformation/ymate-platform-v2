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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

/**
 * FileUtils测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 10:00:00
 * @since 2.1.4
 */
public class FileUtilsTest {

    /**
     * 测试常量
     */
    @Test
    public void testConstants() {
        Assert.assertEquals(":", FileUtils.SEPARATOR_CHAR);
        Assert.assertEquals(".", FileUtils.POINT_CHAR);
        Assert.assertEquals("jar", FileUtils.PROTOCOL_JAR);
        Assert.assertEquals("file", FileUtils.PROTOCOL_FILE);
        Assert.assertEquals("class", FileUtils.FILE_CLASS);
        Assert.assertEquals(".class", FileUtils.FILE_SUFFIX_CLASS);
        Assert.assertEquals("xml", FileUtils.FILE_SUFFIX_XML);
        Assert.assertEquals("properties", FileUtils.FILE_SUFFIX_PROPERTIES);
        Assert.assertEquals("json", FileUtils.FILE_SUFFIX_JSON);
    }

    /**
     * 测试getExtName方法（字符串参数）
     */
    @Test
    public void testGetExtNameString() {
        // 测试正常文件名
        Assert.assertEquals("txt", FileUtils.getExtName("test.txt"));
        Assert.assertEquals("java", FileUtils.getExtName("Test.java"));
        Assert.assertEquals("xml", FileUtils.getExtName("config.xml"));

        // 测试带路径的文件名
        Assert.assertEquals("txt", FileUtils.getExtName("path/to/test.txt"));
        Assert.assertEquals("java", FileUtils.getExtName("C:\\path\\to\\Test.java"));

        // 测试以点开头的文件名
        Assert.assertEquals("txt", FileUtils.getExtName(".test.txt"));
        Assert.assertEquals("", FileUtils.getExtName(".test"));

        // 测试没有扩展名的文件名
        Assert.assertEquals("", FileUtils.getExtName("test"));
        Assert.assertEquals("", FileUtils.getExtName("path/to/test"));

        // 测试空字符串和null
        Assert.assertEquals("", FileUtils.getExtName(""));
        Assert.assertEquals("", FileUtils.getExtName((String) null));
    }

    /**
     * 测试getExtName方法（File参数）
     */
    @Test
    public void testGetExtNameFile() {
        // 测试正常文件
        File file = new File("test.txt");
        Assert.assertEquals("txt", FileUtils.getExtName(file));

        // 测试null
        Assert.assertEquals("", FileUtils.getExtName((File) null));
    }

    /**
     * 测试getHash方法
     */
    @Test
    public void testGetHash() {
        try {
            // 创建临时测试文件
            File testFile = File.createTempFile("test", ".txt");
            testFile.deleteOnExit();

            // 写入测试内容
            Files.write(testFile.toPath(), "test content".getBytes());

            // 测试MD5
            String md5Hash = FileUtils.getHash(testFile);
            Assert.assertNotNull(md5Hash);
            Assert.assertEquals(32, md5Hash.length());

            // 测试SHA1
            String sha1Hash = FileUtils.getHash(testFile, true);
            Assert.assertNotNull(sha1Hash);
            Assert.assertEquals(40, sha1Hash.length());

            // 测试不同内容的文件
            Files.write(testFile.toPath(), "different content".getBytes());
            String newMd5Hash = FileUtils.getHash(testFile);
            Assert.assertNotNull(newMd5Hash);
            Assert.assertNotEquals(md5Hash, newMd5Hash);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试toFile方法
     */
    @Test
    public void testToFile() {
        try {
            // 测试null URL
            Assert.assertNull(FileUtils.toFile(null));

            // 测试文件URL
            File testFile = File.createTempFile("test", ".txt");
            testFile.deleteOnExit();
            URL fileUrl = testFile.toURI().toURL();
            File convertedFile = FileUtils.toFile(fileUrl);
            Assert.assertNotNull(convertedFile);
            Assert.assertEquals(testFile.getPath(), convertedFile.getPath());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试toZip方法
     */
    @Test
    public void testToZip() {
        try {
            // 创建临时测试文件
            File testFile1 = File.createTempFile("test1", ".txt");
            testFile1.deleteOnExit();
            Files.write(testFile1.toPath(), "test content 1".getBytes());

            File testFile2 = File.createTempFile("test2", ".txt");
            testFile2.deleteOnExit();
            Files.write(testFile2.toPath(), "test content 2".getBytes());

            // 测试基本压缩
            File zipFile = FileUtils.toZip("test", testFile1, testFile2);
            Assert.assertNotNull(zipFile);
            Assert.assertTrue(zipFile.exists());
            Assert.assertTrue(zipFile.length() > 0);
            zipFile.deleteOnExit();

            // 测试带重命名选项的压缩
            zipFile = FileUtils.toZip("test", true, testFile1, testFile2);
            Assert.assertNotNull(zipFile);
            Assert.assertTrue(zipFile.exists());
            Assert.assertTrue(zipFile.length() > 0);
            zipFile.deleteOnExit();

            // 测试带自定义条目名称的压缩
            String[] entryNames = {"file1.txt", "file2.txt"};
            zipFile = FileUtils.toZip("test", false, entryNames, testFile1, testFile2);
            Assert.assertNotNull(zipFile);
            Assert.assertTrue(zipFile.exists());
            Assert.assertTrue(zipFile.length() > 0);
            zipFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试toZip方法的异常情况
     */
    @Test
    public void testToZipException() {
        try {
            // 测试空文件数组
            FileUtils.toZip("test");
            Assert.fail("Expected NullArgumentException");
        } catch (Exception e) {
            // 预期异常
        }
    }

    /**
     * 测试writeTo方法（文件到文件）
     */
    @Test
    public void testWriteToFileToFile() {
        try {
            // 创建源文件
            File sourceFile = File.createTempFile("source", ".txt");
            sourceFile.deleteOnExit();
            Files.write(sourceFile.toPath(), "test content".getBytes());

            // 创建目标文件
            File destFile = File.createTempFile("dest", ".txt");
            destFile.deleteOnExit();

            // 测试复制
            FileUtils.writeTo(sourceFile, destFile);
            Assert.assertTrue(destFile.exists());
            Assert.assertTrue(destFile.length() > 0);
            String content = new String(Files.readAllBytes(destFile.toPath()));
            Assert.assertEquals("test content", content);

            // 测试转移选项
            File destFile2 = File.createTempFile("dest2", ".txt");
            destFile2.deleteOnExit();
            FileUtils.writeTo(sourceFile, destFile2, true);
            Assert.assertTrue(destFile2.exists());
            Assert.assertTrue(destFile2.length() > 0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试writeTo方法（输入流到文件）
     */
    @Test
    public void testWriteToInputStreamToFile() {
        try {
            // 创建输入流
            String testContent = "test content from input stream";
            InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());

            // 创建目标文件
            File destFile = File.createTempFile("dest", ".txt");
            destFile.deleteOnExit();

            // 测试复制
            FileUtils.writeTo(inputStream, destFile);
            Assert.assertTrue(destFile.exists());
            Assert.assertTrue(destFile.length() > 0);
            String content = new String(Files.readAllBytes(destFile.toPath()));
            Assert.assertEquals(testContent, content);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试createTempFile方法
     */
    @Test
    public void testCreateTempFile() {
        try {
            // 测试基本创建
            File tempFile = FileUtils.createTempFile("test", "file.txt");
            Assert.assertNotNull(tempFile);
            Assert.assertTrue(tempFile.exists());
            tempFile.deleteOnExit();

            // 测试带索引创建
            tempFile = FileUtils.createTempFile("test", "file.txt", 1);
            Assert.assertNotNull(tempFile);
            Assert.assertTrue(tempFile.exists());
            tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试createEmptyFile方法
     */
    @Test
    public void testCreateEmptyFile() {
        try {
            // 创建临时文件路径
            String tempDir = System.getProperty("java.io.tmpdir");
            File newFile = new File(tempDir, "test_empty_file_" + UUID.randomUUID() + ".txt");

            // 确保文件不存在
            if (newFile.exists()) {
                newFile.delete();
            }

            // 测试创建空文件
            boolean created = FileUtils.createEmptyFile(newFile);
            Assert.assertTrue(created);
            Assert.assertTrue(newFile.exists());
            Assert.assertTrue(newFile.isFile());
            Assert.assertEquals(0, newFile.length());

            // 测试文件已存在的情况
            created = FileUtils.createEmptyFile(newFile);
            Assert.assertTrue(created);
            Assert.assertTrue(newFile.exists());

            // 清理
            newFile.delete();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试createFileIfNotExists方法
     */
    @Test
    public void testCreateFileIfNotExists() {
        try {
            // 创建临时文件路径
            String tempDir = System.getProperty("java.io.tmpdir");
            File newFile = new File(tempDir, "test_file_" + UUID.randomUUID() + ".txt");

            // 确保文件不存在
            if (newFile.exists()) {
                newFile.delete();
            }

            // 测试创建带内容的文件
            String testContent = "test content";
            InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
            boolean created = FileUtils.createFileIfNotExists(newFile, inputStream);
            Assert.assertTrue(created);
            Assert.assertTrue(newFile.exists());
            Assert.assertTrue(newFile.isFile());
            Assert.assertTrue(newFile.length() > 0);
            String content = new String(Files.readAllBytes(newFile.toPath()));
            Assert.assertEquals(testContent, content);

            // 测试文件已存在的情况
            created = FileUtils.createFileIfNotExists(newFile, null);
            Assert.assertTrue(created);
            Assert.assertTrue(newFile.exists());

            // 清理
            newFile.delete();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 测试loadFileAsStream方法
     */
    @Test
    public void testLoadFileAsStream() {
        try {
            // 创建临时测试文件
            File testFile = File.createTempFile("test", ".txt");
            testFile.deleteOnExit();
            Files.write(testFile.toPath(), "test content".getBytes());

            // 测试加载存在的文件
            InputStream inputStream = FileUtils.loadFileAsStream(testFile.getPath(), "non/existent/path.txt");
            Assert.assertNotNull(inputStream);
            String content = new String(IOUtils.toByteArray(inputStream));
            Assert.assertEquals("test content", content);
            inputStream.close();

            // 测试加载不存在的文件
            inputStream = FileUtils.loadFileAsStream("non/existent/path.txt");
            Assert.assertNull(inputStream);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
