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

import org.apache.commons.lang3.StringUtils;

/**
 * Markdown图片组件，用于生成图片格式的内容。
 * <p>
 * 支持创建带替代文本、缩放比例的图片，通过静态工厂方法创建实例。
 * 设计目的是提供一种类型安全的方式来构建Markdown图片，
 * 使用场景包括文档中的图片嵌入、截图展示、产品图片等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:52
 */
public final class Image implements IMarkdown {

    /**
     * 图片替代文本，当图片无法显示时显示
     */
    private final String alt;

    /**
     * 图片URL地址
     */
    private final String url;

    /**
     * 图片缩放比例，范围0-200，0表示不缩放
     */
    private final int zoom;

    /**
     * 创建图片，使用默认设置
     *
     * @param url 图片URL地址
     * @return Image实例，用于生成Markdown
     */
    public static Image create(String url) {
        return new Image(url);
    }

    /**
     * 创建带替代文本的图片
     *
     * @param alt 图片替代文本，当图片无法显示时显示
     * @param url 图片URL地址
     * @return Image实例，用于生成Markdown
     */
    public static Image create(String alt, String url) {
        return new Image(alt, url);
    }

    /**
     * 创建带替代文本和缩放比例的图片
     *
     * @param alt  图片替代文本，当图片无法显示时显示
     * @param url  图片URL地址
     * @param zoom 图片缩放比例，范围0-200，超出范围会自动调整
     * @return Image实例，用于生成Markdown
     */
    public static Image create(String alt, String url, int zoom) {
        return new Image(alt, url, zoom);
    }

    /**
     * 私有构造方法，创建图片
     *
     * @param url 图片URL地址
     */
    private Image(String url) {
        this(null, url, 0);
    }

    /**
     * 私有构造方法，创建带替代文本的图片
     *
     * @param alt 图片替代文本
     * @param url 图片URL地址
     */
    private Image(String alt, String url) {
        this(alt, url, 0);
    }

    /**
     * 私有构造方法，创建带替代文本和缩放比例的图片
     *
     * @param alt  图片替代文本，会被修剪首尾空白
     * @param url  图片URL地址，会被修剪首尾空白
     * @param zoom 图片缩放比例，范围0-200，超出范围会自动调整
     */
    private Image(String alt, String url, int zoom) {
        this.alt = StringUtils.trimToEmpty(alt);
        this.url = StringUtils.trimToEmpty(url);
        this.zoom = zoom < 0 ? 0 : Math.min(zoom, 200);
    }

    /**
     * 将图片转换为Markdown格式字符串
     * <p>
     * 根据不同条件生成不同格式：
     * - 如果URL为空，则返回空字符串
     * - 如果缩放比例为0，使用标准Markdown图片语法：![alt](url)
     * - 如果缩放比例不为0，使用HTML img标签，并添加缩放样式
     * </p>
     *
     * @return Markdown格式的图片字符串
     */
    @Override
    public String toMarkdown() {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        if (zoom == 0) {
            return String.format("![%s](%s)", alt, url);
        }
        return String.format("<img src=\"%s\" alt=\"%s\" style=\"zoom:%d%%;\" />", url, alt, zoom);
    }

    /**
     * 获取图片的Markdown格式字符串表示
     *
     * @return Markdown格式的图片字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }
}
