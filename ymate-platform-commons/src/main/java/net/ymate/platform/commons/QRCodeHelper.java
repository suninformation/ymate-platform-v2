/*
 * Copyright 2007-2019 the original author or authors.
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 *
 * @author 刘镇 (suninformation@163.com) on 15/1/2 下午4:21
 */
public class QRCodeHelper {

    private static final int BLACK = 0xFF000000;

    private static final int WHITE = 0xFFFFFFFF;

    private static final String DEFAULT_FORMAT = "png";

    private final BitMatrix bitMatrix;

    private BufferedImage logoImage;

    private int logoImageSize;

    private int borderWidth;

    private Color borderColor;

    private Color backgroundColor;

    /**
     * 二维码的图片格式
     */
    private String format;

    private QRCodeHelper(BitMatrix matrix) {
        bitMatrix = matrix;
    }

    /**
     * @param content      二维码内容字符串
     * @param characterSet 使用的字符编码集，默认UTF-8
     * @param width        二维码图片宽度
     * @param height       二维码图片高度
     * @param margin       二维码图片边距，默认3
     * @param level        二维码容错级别
     * @return 创建二维码工具类实例对象
     * @throws WriterException 可能产生异常
     */
    public static QRCodeHelper create(String content, String characterSet, int width, int height, int margin, ErrorCorrectionLevel level) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>(3);
        //内容所使用编码
        hints.put(EncodeHintType.CHARACTER_SET, StringUtils.defaultIfEmpty(characterSet, "UTF-8"));
        hints.put(EncodeHintType.MARGIN, margin <= 0 ? 3 : margin);
        //设置QR二维码的纠错级别（H为最高级别）
        if (level != null) {
            hints.put(EncodeHintType.ERROR_CORRECTION, level);
        }
        //生成二维码
        return new QRCodeHelper(new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints));
    }

    public static QRCodeHelper create(String content, int width, int height, int margin, ErrorCorrectionLevel level) throws WriterException {
        return create(content, null, width, height, margin, level);
    }

    /**
     * @param content 二维码内容字符串
     * @param width   二维码图片宽度
     * @param height  二维码图片高度
     * @param level   二维码容错级别
     * @return 创建二维码工具类实例对象
     * @throws WriterException 可能产生的异常
     */
    public static QRCodeHelper create(String content, int width, int height, ErrorCorrectionLevel level) throws WriterException {
        return create(content, null, width, height, 0, level);
    }

    /**
     * @param content 二维码内容字符串
     * @param width   二维码图片宽度
     * @param height  二维码图片高度
     * @return 创建二维码工具类实例对象
     * @throws WriterException 可能产生的异常
     */
    public static QRCodeHelper create(String content, int width, int height) throws WriterException {
        return create(content, null, width, height, 0, null);
    }

    /**
     * @param format 图片格式
     * @return 设置二维码图片格式，默认PNG
     */
    public QRCodeHelper setFormat(String format) {
        this.format = format;
        return this;
    }

    public QRCodeHelper setLogo(InputStream logoInputStream, int logoImageSize, int borderWidth, Color borderColor, Color backgroundColor) throws IOException {
        return setLogo(ImageIO.read(logoInputStream), logoImageSize, borderWidth, borderColor, backgroundColor);
    }

    public QRCodeHelper setLogo(File logoFile, int logoImageSize, int borderWidth, Color borderColor, Color backgroundColor) throws IOException {
        return setLogo(ImageIO.read(logoFile), logoImageSize, borderWidth, borderColor, backgroundColor);
    }

    public QRCodeHelper setLogo(URL logoUrl, int logoImageSize, int borderWidth, Color borderColor, Color backgroundColor) throws IOException {
        return setLogo(ImageIO.read(logoUrl), logoImageSize, borderWidth, borderColor, backgroundColor);
    }

    public QRCodeHelper setLogo(ImageInputStream logoImageInputStream, int logoImageSize, int borderWidth, Color borderColor, Color backgroundColor) throws IOException {
        return setLogo(ImageIO.read(logoImageInputStream), logoImageSize, borderWidth, borderColor, backgroundColor);
    }

    public QRCodeHelper setLogo(BufferedImage logoImage, int logoImageSize, int borderWidth, Color borderColor, Color backgroundColor) {
        this.logoImage = logoImage;
        if (this.logoImage != null) {
            this.logoImageSize = logoImageSize <= 0 ? 5 : logoImageSize;
            this.borderWidth = borderWidth > 0 ? borderWidth : 2;
            this.borderColor = borderColor;
            this.backgroundColor = backgroundColor;
        }
        return this;
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < bitMatrix.getWidth(); x++) {
            for (int y = 0; y < bitMatrix.getHeight(); y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? BLACK : WHITE);
            }
        }
        //
        if (logoImage != null) {
            Graphics2D g = image.createGraphics();
            //
            int logoWidth = image.getWidth() / logoImageSize;
            int logoHeight = image.getHeight() / logoImageSize;
            // 按比例缩放LOGO
            if (logoImage.getHeight() >= logoWidth) {
                logoHeight = (int) Math.round((logoImage.getHeight() * logoWidth * 1.0 / logoImage.getWidth()));
            } else {
                logoWidth = (int) Math.round((logoImage.getWidth() * logoHeight * 1.0 / logoImage.getHeight()));
            }
            BufferedImage resizeLogoImage = new BufferedImage(logoWidth, logoHeight, logoImage.getType());
            resizeLogoImage.getGraphics().drawImage(logoImage, 0, 0, logoWidth, logoHeight, null);
            //
            int x = (image.getWidth() - logoWidth) / 2;
            int y = (image.getHeight() - logoHeight) / 2;
            //
            if (backgroundColor != null) {
                Color originColor = g.getColor();
                g.setColor(backgroundColor);
                g.fillRect(x, y, logoWidth, logoHeight);
                g.setColor(originColor);
            }
            g.drawImage(resizeLogoImage, x, y, logoWidth, logoHeight, null);
            if (borderColor != null) {
                g.setStroke(new BasicStroke(borderWidth));
                g.setColor(borderColor);
                g.drawRect(x, y, logoWidth, logoHeight);
            }
            g.dispose();
        }
        //
        return image;
    }

    public String getFormat() {
        return StringUtils.defaultIfEmpty(format, DEFAULT_FORMAT);
    }

    /**
     * 输出二维码图片到文件
     *
     * @param file 目标文件对象
     * @throws IOException 可能产生的异常
     */
    public void writeToFile(File file) throws IOException {
        BufferedImage image = toBufferedImage();
        String imageFormat = getFormat();
        if (!ImageIO.write(image, imageFormat, file)) {
            throw new IOException(String.format("Could not write an image of format %s to %s", imageFormat, file));
        }
    }

    /**
     * 输出二维码图片到输出流
     *
     * @param stream 目标输出流对象
     * @throws IOException 可能产生的异常
     */
    public void writeToStream(OutputStream stream) throws IOException {
        BufferedImage image = toBufferedImage();
        String imageFormat = getFormat();
        if (!ImageIO.write(image, imageFormat, stream)) {
            throw new IOException(String.format("Could not write an image of format %s", imageFormat));
        }
    }

}
