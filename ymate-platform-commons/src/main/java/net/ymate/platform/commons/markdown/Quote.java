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
 * Markdown引用块组件，用于生成引用格式的文本内容。
 * <p>
 * 支持通过静态工厂方法创建实例，支持链式调用追加内容。
 * 设计目的是提供一种类型安全的方式来构建Markdown引用块，
 * 使用场景包括引用他人观点、引用外部资源、强调重要内容等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:31
 */
public final class Quote implements IMarkdown {

    /**
     * 引用内容构建器，用于存储和构建引用文本
     */
    private final StringBuilder content = new StringBuilder();

    /**
     * 创建引用块
     *
     * @param content 引用内容，实现了IMarkdown接口
     * @return Quote实例，用于进一步操作或生成Markdown
     */
    public static Quote create(IMarkdown content) {
        return new Quote(content);
    }

    /**
     * 创建引用块
     *
     * @param content 引用文本内容
     * @return Quote实例，用于进一步操作或生成Markdown
     */
    public static Quote create(String content) {
        return new Quote(content);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建引用块
     *
     * @param content 引用内容，实现了IMarkdown接口
     */
    private Quote(IMarkdown content) {
        this(content.toMarkdown());
    }

    /**
     * 私有构造方法，创建引用块
     *
     * @param content 引用文本内容
     */
    private Quote(String content) {
        append(content);
    }

    /**
     * 追加Markdown组件内容到引用
     *
     * @param content 要追加的内容，实现了IMarkdown接口
     * @return 当前Quote实例，支持链式调用
     */
    public Quote append(IMarkdown content) {
        return append(content.toMarkdown());
    }

    /**
     * 追加文本内容到引用
     *
     * @param content 要追加的文本内容，会被修剪首尾空白后追加
     * @return 当前Quote实例，支持链式调用
     */
    public Quote append(String content) {
        this.content.append(StringUtils.trimToEmpty(content));
        return this;
    }

    /**
     * 将引用转换为Markdown格式字符串
     *
     * @return Markdown格式的引用字符串，每行以"> "开头，末尾有一个空引用行
     */
    @Override
    public String toMarkdown() {
        if (content.length() == 0) {
            return StringUtils.EMPTY;
        }
        String contentStr = content.toString();
        String[] lines = StringUtils.split(contentStr, PARAGRAPH_SEPARATOR);
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append("> ").append(line).append(PARAGRAPH_SEPARATOR);
        }
        // 确保引用末尾有一个单独的"> "行，然后跟着一个换行符，与测试预期一致
        result.append("> ").append(PARAGRAPH_SEPARATOR);
        return result.toString();
    }

    /**
     * 获取引用的Markdown格式字符串表示
     *
     * @return Markdown格式的引用字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }
}
