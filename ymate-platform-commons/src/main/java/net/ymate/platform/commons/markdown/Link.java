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
 * Markdown链接组件，用于生成链接格式的文本内容。
 * <p>
 * 支持创建带显示文本或不带显示文本的链接，通过静态工厂方法创建实例。
 * 设计目的是提供一种类型安全的方式来构建Markdown链接，
 * 使用场景包括文档中的超链接、外部资源引用、导航链接等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:43
 */
public final class Link implements IMarkdown {

    /**
     * 链接显示文本，为空时使用URL作为显示文本
     */
    private final String name;

    /**
     * 链接目标URL地址
     */
    private final String url;

    /**
     * 创建链接，使用URL作为显示文本
     *
     * @param url 链接目标URL地址
     * @return Link实例，用于生成Markdown
     */
    public static Link create(String url) {
        return new Link((String) null, url);
    }

    /**
     * 创建链接，使用Markdown组件作为显示文本
     *
     * @param name 链接显示文本，实现了IMarkdown接口
     * @param url  链接目标URL地址
     * @return Link实例，用于生成Markdown
     */
    public static Link create(IMarkdown name, String url) {
        return new Link(name, url);
    }

    /**
     * 创建链接，使用文本作为显示文本
     *
     * @param name 链接显示文本
     * @param url  链接目标URL地址
     * @return Link实例，用于生成Markdown
     */
    public static Link create(String name, String url) {
        return new Link(name, url);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建链接
     *
     * @param name 链接显示文本，实现了IMarkdown接口
     * @param url  链接目标URL地址
     */
    private Link(IMarkdown name, String url) {
        this(name.toMarkdown(), url);
    }

    /**
     * 私有构造方法，创建链接
     *
     * @param name 链接显示文本，会被修剪首尾空白
     * @param url  链接目标URL地址，会被修剪首尾空白
     */
    private Link(String name, String url) {
        this.name = StringUtils.trimToEmpty(name);
        this.url = StringUtils.trimToEmpty(url);
    }

    /**
     * 将链接转换为Markdown格式字符串
     * <p>
     * 如果URL为空，则返回空字符串；
     * 如果显示文本为空，则使用URL作为显示文本。
     * </p>
     *
     * @return Markdown格式的链接字符串，格式为[name](url)
     */
    @Override
    public String toMarkdown() {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        return String.format("[%s](%s)", StringUtils.defaultIfBlank(name, url), url);
    }

    /**
     * 获取链接的Markdown格式字符串表示
     *
     * @return Markdown格式的链接字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }
}
