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
package net.ymate.platform.commons;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * QRCodeHelper测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:15:29
 * @since 2.1.4
 */
public class QRCodeHelperTest {

    private static final String TEST_CONTENT = "https://www.example.com";
    private static final int TEST_WIDTH = 200;
    private static final int TEST_HEIGHT = 200;

    @Test
    public void testCreateWithFullParameters() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, "UTF-8", TEST_WIDTH, TEST_HEIGHT, 2, ErrorCorrectionLevel.H);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testCreateWithContentAndSize() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testCreateWithContentSizeAndLevel() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT, ErrorCorrectionLevel.M);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testCreateWithContentSizeMarginAndLevel() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT, 1, ErrorCorrectionLevel.L);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testSetFormat() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Test setFormat with PNG
        QRCodeHelper result = helper.setFormat("png");
        Assert.assertSame(helper, result);
        Assert.assertEquals("png", helper.getFormat());

        // Test setFormat with JPG
        result = helper.setFormat("jpg");
        Assert.assertSame(helper, result);
        Assert.assertEquals("jpg", helper.getFormat());

        // Test setFormat with empty string (should default to PNG)
        result = helper.setFormat("");
        Assert.assertSame(helper, result);
        Assert.assertEquals("png", helper.getFormat());
    }

    @Test
    public void testGetFormat() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Test default format
        Assert.assertEquals("png", helper.getFormat());

        // Test custom format
        helper.setFormat("jpg");
        Assert.assertEquals("jpg", helper.getFormat());
    }

    @Test
    public void testToBufferedImage() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Test converting to BufferedImage
        BufferedImage image = helper.toBufferedImage();
        Assert.assertNotNull(image);
        Assert.assertEquals(TEST_WIDTH, image.getWidth());
        Assert.assertEquals(TEST_HEIGHT, image.getHeight());
    }

    @Test
    public void testWriteToStream() throws WriterException, IOException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Test writing to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        helper.writeToStream(outputStream);

        // Verify output stream has data
        byte[] imageData = outputStream.toByteArray();
        Assert.assertNotNull(imageData);
        Assert.assertTrue(imageData.length > 0);

        // Clean up
        outputStream.close();
    }

    @Test
    public void testWriteToFile() throws WriterException, IOException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Create temporary file
        File tempFile = File.createTempFile("qrcode_test", ".png");

        try {
            // Test writing to file
            helper.writeToFile(tempFile);

            // Verify file exists and has content
            Assert.assertTrue(tempFile.exists());
            Assert.assertTrue(tempFile.length() > 0);

            // Verify file is a valid image
            BufferedImage image = ImageIO.read(tempFile);
            Assert.assertNotNull(image);
        } finally {
            // Clean up
            tempFile.delete();
        }
    }

    @Test
    public void testWriteToFileWithCustomFormat() throws WriterException, IOException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT)
                .setFormat("jpg");

        // Create temporary file
        File tempFile = File.createTempFile("qrcode_test", ".jpg");

        try {
            // Test writing to file with JPG format
            helper.writeToFile(tempFile);

            // Verify file exists and has content
            Assert.assertTrue(tempFile.exists());
            Assert.assertTrue(tempFile.length() > 0);
        } finally {
            // Clean up
            tempFile.delete();
        }
    }

    @Test
    public void testSetLogoWithBufferedImage() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Create a simple logo image
        BufferedImage logo = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = logo.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 50, 50);
        g.setColor(Color.BLACK);
        g.drawString("Test", 10, 25);
        g.dispose();

        // Test setLogo
        QRCodeHelper result = helper.setLogo(logo, 5, 2, Color.RED, Color.WHITE);
        Assert.assertSame(helper, result);

        // Test toBufferedImage with logo
        BufferedImage qrCodeImage = helper.toBufferedImage();
        Assert.assertNotNull(qrCodeImage);
        Assert.assertEquals(TEST_WIDTH, qrCodeImage.getWidth());
        Assert.assertEquals(TEST_HEIGHT, qrCodeImage.getHeight());
    }

    @Test
    public void testToBufferedImageWithLogo() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Create a simple logo image
        BufferedImage logo = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = logo.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 30, 30);
        g.dispose();

        // Set logo with border and background
        helper.setLogo(logo, 10, 1, Color.BLACK, Color.YELLOW);

        // Test toBufferedImage
        BufferedImage image = helper.toBufferedImage();
        Assert.assertNotNull(image);
        Assert.assertEquals(TEST_WIDTH, image.getWidth());
        Assert.assertEquals(TEST_HEIGHT, image.getHeight());
    }

    @Test
    public void testToBufferedImageWithZeroLogoSize() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Create a simple logo image
        BufferedImage logo = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

        // Set logo with zero size (should use default size of 5)
        helper.setLogo(logo, 0, 1, Color.BLACK, Color.WHITE);

        // Test toBufferedImage
        BufferedImage image = helper.toBufferedImage();
        Assert.assertNotNull(image);
    }

    @Test
    public void testToBufferedImageWithNegativeLogoSize() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Create a simple logo image
        BufferedImage logo = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

        // Set logo with negative size (should use default size of 5)
        helper.setLogo(logo, -1, 1, Color.BLACK, Color.WHITE);

        // Test toBufferedImage
        BufferedImage image = helper.toBufferedImage();
        Assert.assertNotNull(image);
    }

    @Test
    public void testToBufferedImageWithZeroBorderWidth() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Create a simple logo image
        BufferedImage logo = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

        // Set logo with zero border width (should use default width of 2)
        helper.setLogo(logo, 5, 0, Color.BLACK, Color.WHITE);

        // Test toBufferedImage
        BufferedImage image = helper.toBufferedImage();
        Assert.assertNotNull(image);
    }

    @Test
    public void testToBufferedImageWithNullColors() throws WriterException {
        QRCodeHelper helper = QRCodeHelper.create(TEST_CONTENT, TEST_WIDTH, TEST_HEIGHT);

        // Create a simple logo image
        BufferedImage logo = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

        // Set logo with null colors
        helper.setLogo(logo, 5, 2, null, null);

        // Test toBufferedImage
        BufferedImage image = helper.toBufferedImage();
        Assert.assertNotNull(image);
    }
}
