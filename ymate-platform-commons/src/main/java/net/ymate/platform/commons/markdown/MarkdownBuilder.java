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
 * Markdown文档构建器，用于链式构建完整的Markdown文档。
 * <p>
 * 提供了一系列便捷方法来添加各种Markdown组件，如标题、文本、引用、链接、图片、代码块等，
 * 支持链式调用，简化Markdown文档的构建过程。
 * 设计目的是提供一种流畅的API来生成结构化的Markdown文档，
 * 使用场景包括动态生成博客文章、技术文档、API文档等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 15:29
 */
public final class MarkdownBuilder implements IMarkdown {

    /**
     * 内部使用的字符串构建器，用于存储生成的Markdown内容
     */
    private final StringBuilder stringBuilder = new StringBuilder();

    /**
     * 创建一个新的MarkdownBuilder实例
     *
     * @return MarkdownBuilder实例，用于链式构建Markdown文档
     */
    public static MarkdownBuilder create() {
        return new MarkdownBuilder();
    }

    /**
     * 私有构造方法，防止直接实例化，通过create()静态方法获取实例
     */
    private MarkdownBuilder() {
    }

    /**
     * 添加换行符
     *
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder br() {
        stringBuilder.append(P);
        return this;
    }

    /**
     * 添加一个段落分隔（两个换行符）
     *
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder p() {
        stringBuilder.append(P).append(P);
        return this;
    }

    /**
     * 添加指定数量的换行符
     *
     * @param repeat 换行符重复次数，最小值为1
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder p(int repeat) {
        stringBuilder.append(StringUtils.repeat(P, Math.max(repeat, 1)));
        return this;
    }

    /**
     * 添加水平分隔线，并在前后各添加一个换行符
     *
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder hr() {
        stringBuilder.append(HR).append(P).append(P);
        return this;
    }

    /**
     * 添加一个空格
     *
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder space() {
        stringBuilder.append(StringUtils.SPACE);
        return this;
    }

    /**
     * 添加指定数量的空格
     *
     * @param repeat 空格重复次数，最小值为1
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder space(int repeat) {
        stringBuilder.append(StringUtils.repeat(StringUtils.SPACE, Math.max(repeat, 1)));
        return this;
    }

    /**
     * 添加一个制表符（4个空格）
     *
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder tab() {
        stringBuilder.append(TAB);
        return this;
    }

    /**
     * 获取当前构建的Markdown内容长度
     *
     * @return 已构建内容的字符长度
     */
    public int length() {
        return stringBuilder.length();
    }

    /**
     * 追加一个Markdown组件的内容
     *
     * @param markdown 实现了IMarkdown接口的Markdown组件
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder append(IMarkdown markdown) {
        stringBuilder.append(markdown.toMarkdown());
        return this;
    }

    /**
     * 追加一个字符串内容
     *
     * @param content 要追加的字符串内容
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder append(String content) {
        stringBuilder.append(content);
        return this;
    }

    // -----

    /**
     * 添加一级标题
     *
     * @param title 标题内容，实现了IMarkdown接口
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder title(IMarkdown title) {
        stringBuilder.append(Title.create(title));
        return this;
    }

    /**
     * 添加一级标题
     *
     * @param title 标题文本内容
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder title(String title) {
        stringBuilder.append(Title.create(title));
        return this;
    }

    /**
     * 添加指定级别的标题
     *
     * @param title 标题内容，实现了IMarkdown接口
     * @param level 标题级别，范围1-6，超出范围会自动调整
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder title(IMarkdown title, int level) {
        stringBuilder.append(Title.create(title, level));
        return this;
    }

    /**
     * 添加指定级别的标题
     *
     * @param title 标题文本内容
     * @param level 标题级别，范围1-6，超出范围会自动调整
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder title(String title, int level) {
        stringBuilder.append(Title.create(title, level));
        return this;
    }

    // -----

    /**
     * 添加普通样式文本
     *
     * @param text 文本内容，实现了IMarkdown接口
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder text(IMarkdown text) {
        stringBuilder.append(Text.create(text));
        return this;
    }

    /**
     * 添加普通样式文本
     *
     * @param text 文本内容
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder text(String text) {
        stringBuilder.append(Text.create(text));
        return this;
    }

    /**
     * 添加指定样式的文本
     *
     * @param text  文本内容，实现了IMarkdown接口
     * @param style 文本样式，如粗体、斜体、下划线等
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder text(IMarkdown text, Text.Style style) {
        stringBuilder.append(Text.create(text, style));
        return this;
    }

    /**
     * 添加指定样式的文本
     *
     * @param text  文本内容
     * @param style 文本样式，如粗体、斜体、下划线等
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder text(String text, Text.Style style) {
        stringBuilder.append(Text.create(text, style));
        return this;
    }

    // -----

    /**
     * 添加引用块
     *
     * @param quote 引用内容，实现了IMarkdown接口
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder quote(IMarkdown quote) {
        stringBuilder.append(Quote.create(quote));
        return this;
    }

    /**
     * 添加引用块
     *
     * @param quote 引用文本内容
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder quote(String quote) {
        stringBuilder.append(Quote.create(quote));
        return this;
    }

    // -----

    /**
     * 添加链接
     *
     * @param name 链接显示文本，实现了IMarkdown接口
     * @param url  链接URL地址
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder link(IMarkdown name, String url) {
        stringBuilder.append(Link.create(name, url));
        return this;
    }

    /**
     * 添加链接
     *
     * @param name 链接显示文本
     * @param url  链接URL地址
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder link(String name, String url) {
        stringBuilder.append(Link.create(name, url));
        return this;
    }

    // -----

    /**
     * 添加图片
     *
     * @param url 图片URL地址
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder image(String url) {
        stringBuilder.append(Image.create(url));
        return this;
    }

    /**
     * 添加带有替代文本的图片
     *
     * @param alt 图片替代文本，当图片无法显示时显示
     * @param url 图片URL地址
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder image(String alt, String url) {
        stringBuilder.append(Image.create(alt, url));
        return this;
    }

    /**
     * 添加带有替代文本和缩放比例的图片
     *
     * @param alt  图片替代文本，当图片无法显示时显示
     * @param url  图片URL地址
     * @param zoom 图片缩放比例，范围0-200，0表示不缩放
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder image(String alt, String url, int zoom) {
        stringBuilder.append(Image.create(alt, url, zoom));
        return this;
    }

    // -----

    /**
     * 添加代码块
     *
     * @param code 代码内容，实现了IMarkdown接口
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder code(IMarkdown code) {
        stringBuilder.append(Code.create(code));
        return this;
    }

    /**
     * 添加代码块
     *
     * @param code 代码文本内容
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder code(String code) {
        stringBuilder.append(Code.create(code));
        return this;
    }

    /**
     * 添加指定语言的代码块
     *
     * @param code     代码内容，实现了IMarkdown接口
     * @param language 代码语言，用于语法高亮
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder code(IMarkdown code, String language) {
        stringBuilder.append(Code.create(code, language));
        return this;
    }

    /**
     * 添加指定语言的代码块
     *
     * @param code     代码文本内容
     * @param language 代码语言，用于语法高亮
     * @return 当前MarkdownBuilder实例，支持链式调用
     */
    public MarkdownBuilder code(String code, String language) {
        stringBuilder.append(Code.create(code, language));
        return this;
    }

    // -----

    /**
     * 将当前构建的内容转换为完整的Markdown格式字符串
     *
     * @return Markdown格式的字符串，包含所有添加的组件内容
     */
    @Override
    public String toMarkdown() {
        return stringBuilder.toString();
    }

    /**
     * 获取当前构建的Markdown内容字符串表示
     *
     * @return Markdown格式的字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }
}
