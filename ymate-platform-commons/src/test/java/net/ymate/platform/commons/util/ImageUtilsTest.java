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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * ImageUtils 类的单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 01:15:00
 * @since 2.1.4
 */
public class ImageUtilsTest {

    private File tempDir;
    private BufferedImage originImage;
    private BufferedImage qrImage;
    private BufferedImage simpleImage;

    @Before
    public void setUp() {
        // 创建临时目录
        tempDir = new File(System.getProperty("java.io.tmpdir"), "ImageUtilsTest_" + System.currentTimeMillis());
        tempDir.mkdirs();
        // 创建测试用的图片
        originImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        qrImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        simpleImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);

        // 填充颜色
        Graphics2D g1 = originImage.createGraphics();
        g1.setColor(Color.WHITE);
        g1.fillRect(0, 0, 200, 200);
        g1.dispose();

        Graphics2D g2 = qrImage.createGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 100, 100);
        g2.dispose();

        Graphics2D g3 = simpleImage.createGraphics();
        g3.setColor(Color.GRAY);
        g3.fillRect(0, 0, 50, 50);
        g3.dispose();
    }

    @After
    public void tearDown() {
        // 释放资源
        originImage = null;
        qrImage = null;
        simpleImage = null;
        // 删除临时目录
        if (tempDir != null && tempDir.exists()) {
            deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * 测试替换二维码功能（正常情况）
     */
    @Test
    public void testReplaceQrCode() {
        // 由于没有实际的二维码，这里测试方法调用是否正常执行
        // 预期会抛出 NotFoundException，因为原图中没有二维码
        assertThrows(com.google.zxing.NotFoundException.class, () -> {
            ImageUtils.replaceQrCode(originImage, qrImage);
        });
    }

    /**
     * 测试替换二维码功能（带差值参数）
     */
    @Test
    public void testReplaceQrCodeWithDeviate() {
        // 预期会抛出 NotFoundException，因为原图中没有二维码
        assertThrows(com.google.zxing.NotFoundException.class, () -> {
            ImageUtils.replaceQrCode(originImage, qrImage, 20);
        });
    }

    /**
     * 测试空参数异常
     */
    @Test
    public void testReplaceQrCodeWithNullParams() {
        // 测试 originImage 为 null
        assertThrows(NullPointerException.class, () -> {
            ImageUtils.replaceQrCode(null, qrImage);
        });

        // 测试 qrImage 为 null
        assertThrows(NullPointerException.class, () -> {
            ImageUtils.replaceQrCode(originImage, null);
        });

        // 测试两个参数都为 null
        assertThrows(NullPointerException.class, () -> {
            ImageUtils.replaceQrCode(null, null);
        });
    }

    /**
     * 测试海明距离计算
     */
    @Test
    public void testHammingDistance() {
        String hash1 = "10101010";
        String hash2 = "10101010";
        String hash3 = "11110000";

        // 相同的哈希值，距离为 0
        assertEquals(0, ImageUtils.hammingDistance(hash1, hash2));

        // 不同的哈希值，距离应为 4
        assertEquals(4, ImageUtils.hammingDistance(hash1, hash3));

        // 部分不同，距离应为 4
        String hash4 = "10100101";
        assertEquals(4, ImageUtils.hammingDistance(hash1, hash4));
    }

    /**
     * 测试空参数的海明距离
     */
    @Test
    public void testHammingDistanceWithNullParams() {
        String hash = "10101010";

        // 测试一个参数为 null
        assertEquals(-1, ImageUtils.hammingDistance(null, hash));
        assertEquals(-1, ImageUtils.hammingDistance(hash, null));

        // 测试两个参数都为 null
        assertEquals(-1, ImageUtils.hammingDistance(null, null));
    }

    /**
     * 测试不同长度的海明距离
     */
    @Test
    public void testHammingDistanceWithDifferentLengths() {
        String hash1 = "101010";
        String hash2 = "10101010";

        // 长度不同，返回 -1
        assertEquals(-1, ImageUtils.hammingDistance(hash1, hash2));
    }

    /**
     * 测试 dHash 计算
     */
    @Test
    public void testDHash() {
        String dHash = ImageUtils.dHash(simpleImage);
        assertNotNull(dHash);
        // 9x8 像素的图片，dHash 长度应为 9*8 - 8 = 64 位
        assertEquals(64, dHash.length());

        // 相同图片的 dHash 应该相同
        String dHash2 = ImageUtils.dHash(simpleImage);
        assertEquals(dHash, dHash2);
    }

    /**
     * 测试空参数的 dHash
     */
    @Test
    public void testDHashWithNullParams() {
        assertThrows(NullPointerException.class, () -> {
            ImageUtils.dHash(null);
        });
    }

    /**
     * 测试按宽度和高度调整大小
     */
    @Test
    public void testResizeByWidthAndHeight() {
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder = ImageUtils.resize(simpleImage, 30, 30, 0.8f, null);
        assertNotNull(builder);

        // 测试生成的图片
        try {
            BufferedImage resized = builder.asBufferedImage();
            assertEquals(30, resized.getWidth());
            assertEquals(30, resized.getHeight());
        } catch (IOException e) {
            fail("调整大小失败: " + e.getMessage());
        }
    }

    /**
     * 测试只按宽度调整大小
     */
    @Test
    public void testResizeByWidthOnly() {
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder = ImageUtils.resize(simpleImage, 40, 0, 0.8f, null);
        assertNotNull(builder);

        try {
            BufferedImage resized = builder.asBufferedImage();
            assertEquals(40, resized.getWidth());
            // 高度应该按比例调整
            assertTrue(resized.getHeight() > 0);
        } catch (IOException e) {
            fail("调整大小失败: " + e.getMessage());
        }
    }

    /**
     * 测试只按高度调整大小
     */
    @Test
    public void testResizeByHeightOnly() {
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder = ImageUtils.resize(simpleImage, 0, 40, 0.8f, null);
        assertNotNull(builder);

        try {
            BufferedImage resized = builder.asBufferedImage();
            assertEquals(40, resized.getHeight());
            // 宽度应该按比例调整
            assertTrue(resized.getWidth() > 0);
        } catch (IOException e) {
            fail("调整大小失败: " + e.getMessage());
        }
    }

    /**
     * 测试无效的宽度和高度（都为0或负数）
     */
    @Test
    public void testResizeWithInvalidWidthHeight() {
        // 宽度和高度都为0
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder1 = ImageUtils.resize(simpleImage, 0, 0, 0.8f, null);
        assertNotNull(builder1);

        // 宽度和高度都为负数
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder2 = ImageUtils.resize(simpleImage, -10, -10, 0.8f, null);
        assertNotNull(builder2);
    }

    /**
     * 测试带质量参数的调整大小
     */
    @Test
    public void testResizeWithQuality() {
        // 正常质量参数（0.5）
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder1 = ImageUtils.resize(simpleImage, 30, 30, 0.5f, null);
        assertNotNull(builder1);

        // 无效质量参数（大于1.0）
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder2 = ImageUtils.resize(simpleImage, 30, 30, 1.5f, null);
        assertNotNull(builder2);

        // 无效质量参数（小于0）
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder3 = ImageUtils.resize(simpleImage, 30, 30, -0.5f, null);
        assertNotNull(builder3);
    }

    /**
     * 测试按比例调整大小
     */
    @Test
    public void testResizeByScale() {
        // 正常比例（0.5）
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder1 = ImageUtils.resize(simpleImage, 0.5f, 0.8f);
        assertNotNull(builder1);

        try {
            BufferedImage resized = builder1.asBufferedImage();
            // 50x50 的图片缩小到 0.5 倍，应该是 25x25
            assertEquals(25, resized.getWidth());
            assertEquals(25, resized.getHeight());
        } catch (IOException e) {
            fail("调整大小失败: " + e.getMessage());
        }
    }

    /**
     * 测试无效比例值
     */
    @Test
    public void testResizeByScaleWithInvalidValue() {
        // 比例大于1.0，应该使用默认值1.0
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder1 = ImageUtils.resize(simpleImage, 1.5f, 0.8f);
        assertNotNull(builder1);

        // 比例小于0，应该使用默认值1.0
        net.coobird.thumbnailator.Thumbnails.Builder<BufferedImage> builder2 = ImageUtils.resize(simpleImage, -0.5f, 0.8f);
        assertNotNull(builder2);
    }

    /**
     * 测试调整大小并写入文件
     */
    @Test
    public void testResizeToFile() {
        File outputFile = new File(tempDir, "resized.jpg");
        boolean result = ImageUtils.resize(simpleImage, outputFile, 30, 30, 0.8f);
        assertTrue(result);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * 测试按比例调整大小并写入文件
     */
    @Test
    public void testResizeToFileByScale() {
        File outputFile = new File(tempDir, "resized_by_scale.jpg");
        boolean result = ImageUtils.resize(simpleImage, outputFile, 0.5f, 0.8f);
        assertTrue(result);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * 测试空参数的文件写入
     */
    @Test
    public void testResizeToFileWithNullParams() {
        File outputFile = new File(tempDir, "null_test.jpg");

        // 源图片为null
        assertThrows(NullPointerException.class, () -> {
            ImageUtils.resize(null, outputFile, 30, 30, 0.8f);
        });

        // 目标文件为null
        assertThrows(NullPointerException.class, () -> {
            ImageUtils.resize(simpleImage, null, 30, 30, 0.8f);
        });
    }

    /**
     * 测试空参数的resize方法
     */
    @Test
    public void testResizeWithNullSource() {
        assertThrows(NullPointerException.class, () -> {
            ImageUtils.resize(null, 30, 30, 0.8f, null);
        });

        assertThrows(NullPointerException.class, () -> {
            ImageUtils.resize(null, 0.5f, 0.8f);
        });
    }
}
