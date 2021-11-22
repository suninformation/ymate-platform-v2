/*
 * Copyright 2007-2021 the original author or authors.
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

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/11/21 12:58 下午
 * @since 2.1.0
 */
public class ImageUtils {

    private static final Log LOG = LogFactory.getLog(ImageUtils.class);

    /**
     * 替换原图片里面的二维码
     *
     * @param originImage 原图
     * @param qrImage     要替换的二维码
     * @param deviate     定位点与起始点的差值
     * @return 替换后的图片
     * @throws NotFoundException 识别二维码失败
     */
    public static BufferedImage replaceQrCode(BufferedImage originImage, BufferedImage qrImage, int deviate) throws NotFoundException {

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(originImage)));

        Map<DecodeHintType, Object> hints = new HashMap<>(1);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        Result[] results = new QRCodeMultiReader().decodeMultiple(binaryBitmap, hints);
        if (results != null && results.length > 0) {
            for (Result result : results) {
                // 定位点的坐标，按照左下、左上、右上顺序
                ResultPoint[] resultPoint = result.getResultPoints();
                float x1 = resultPoint[0].getX();
                float y1 = resultPoint[0].getY();
                float x2 = resultPoint[1].getX();
                float y2 = resultPoint[1].getY();

                // 定位点与起始点的差值
                if (deviate == 0) {
                    deviate = 36;
                }
                // 计算二维码图片边长
                final int length = (int) Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2) + Math.abs(y1 - y2) * Math.abs(y1 - y2)) + 2 * deviate;
                // 根据二维码定位坐标计算起始坐标
                int x = Math.round(x2) - deviate;
                int y = Math.round(y2) - deviate;
                // 替换二维码图案
                Graphics2D graphics = originImage.createGraphics();
                //
                BufferedImage resizedQrImage = new BufferedImage(length, length, qrImage.getType());
                Graphics gc = resizedQrImage.getGraphics();
                gc.setColor(Color.WHITE);
                gc.drawImage(qrImage.getScaledInstance(length, length, Image.SCALE_SMOOTH), 0, 0, null);
                resizedQrImage.flush();
                gc.dispose();
                //
                graphics.drawImage(resizedQrImage, x, y, length, length, null);
                originImage.flush();
                graphics.dispose();
            }
        }
        return originImage;
    }

    public static BufferedImage replaceQrCode(BufferedImage originImage, BufferedImage qrImage) throws NotFoundException {
        return replaceQrCode(originImage, qrImage, 0);
    }

    /**
     * 计算海明距离（即相似度差异值，一般值小于5为同一张图片）
     *
     * @param dHash1 dHash值1
     * @param dHash2 dHash值2
     * @return 返回海明距离值
     */
    public static long hammingDistance(String dHash1, String dHash2) {
        int distance = 0;
        if (dHash1 == null || dHash2 == null || dHash1.length() != dHash2.length()) {
            distance = -1;
        } else {
            for (int i = 0; i < dHash1.length(); i++) {
                if (dHash1.charAt(i) != dHash2.charAt(i)) {
                    distance++;
                }
            }
        }
        return distance;
    }

    /**
     * @param srcImage 源图片
     * @return 计算图片文件dHash值
     */
    public static String dHash(BufferedImage srcImage) {
        // 转换为 9*8 像素
        BufferedImage buffImg = new BufferedImage(9, 8, BufferedImage.TYPE_INT_RGB);
        buffImg.getGraphics().drawImage(srcImage.getScaledInstance(9, 8, Image.SCALE_SMOOTH), 0, 0, null);
        int width = buffImg.getWidth();
        int height = buffImg.getHeight();
        int[][] grayPix = new int[width][height];
        StringBuilder figure = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 图片灰度化
                int rgb = buffImg.getRGB(x, y);
                int r = rgb >> 16 & 0xff;
                int g = rgb >> 8 & 0xff;
                int b = rgb & 0xff;
                int gray = (r * 30 + g * 59 + b * 11) / 100;
                grayPix[x][y] = gray;
                // 开始计算 dHash
                if (x != 0) {
                    long bit = grayPix[x - 1][y] > grayPix[x][y] ? 1 : 0;
                    figure.append(bit);
                }
            }
        }
        return figure.toString();
    }

    /**
     * 重置图片大小
     *
     * @param source  源图片
     * @param width   宽度
     * @param height  高度
     * @param quality 质量
     * @param format  输出文件格式
     * @return 返回Thumbnails.Builder对象实例
     */
    public static Thumbnails.Builder<BufferedImage> resize(BufferedImage source, int width, int height, float quality, String format) {
        Thumbnails.Builder<BufferedImage> thumbBuilder = Thumbnails.of(source);
        if (width <= 0 || height <= 0) {
            if (width > 0) {
                thumbBuilder.width(width);
            } else if (height > 0) {
                thumbBuilder.height(height);
            }
        } else {
            thumbBuilder.size(width, height).keepAspectRatio(false);
        }
        if (quality > 0 && quality < 1) {
            thumbBuilder.outputQuality(quality);
        }
        if (StringUtils.isNotBlank(format)) {
            thumbBuilder.outputFormat(format);
        }
        return thumbBuilder;
    }

    public static Thumbnails.Builder<BufferedImage> resize(BufferedImage source, int width, int height, float quality) {
        return resize(source, width, height, quality, null);
    }

    /**
     * 重置图片大小并将重置后的文件写入目标文件
     *
     * @param source  源图片
     * @param dist    目标文件
     * @param width   宽度
     * @param height  高度
     * @param quality 质量
     * @return 返回true表示重置成功
     */
    public static boolean resize(BufferedImage source, File dist, int width, int height, float quality) {
        try {
            resize(source, width, height, quality, null).toFile(dist);
            return true;
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return false;
    }

    public static Thumbnails.Builder<BufferedImage> resize(BufferedImage source, float scale, float quality, String format) {
        if (scale <= 0 || scale > 1) {
            scale = 1f;
        }
        Thumbnails.Builder<BufferedImage> thumbBuilder = Thumbnails.of(source).scale(scale);
        if (quality > 0 && quality < 1) {
            thumbBuilder.outputQuality(quality);
        }
        if (StringUtils.isNotBlank(format)) {
            thumbBuilder.outputFormat(format);
        }
        return thumbBuilder;
    }

    public static Thumbnails.Builder<BufferedImage> resize(BufferedImage source, float scale, float quality) {
        return resize(source, scale, quality, null);
    }

    public static boolean resize(BufferedImage source, File dist, float scale, float quality) {
        try {
            resize(source, scale, quality, null).toFile(dist);
            return true;
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return false;
    }
}
