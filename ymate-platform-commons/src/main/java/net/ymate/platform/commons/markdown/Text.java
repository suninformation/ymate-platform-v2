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
 * Markdown文本组件，用于生成不同样式的文本内容。
 * <p>
 * 支持多种文本样式，包括普通、粗体、斜体、下划线和删除线，
 * 通过静态工厂方法创建实例，支持链式调用追加内容。
 * 设计目的是提供一种类型安全的方式来构建不同样式的Markdown文本，
 * 使用场景包括正文文本、强调文本、特殊标记文本等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 12:35
 */
public final class Text implements IMarkdown {

    /**
     * 文本内容构建器，用于存储和构建文本内容
     */
    private final StringBuilder content = new StringBuilder();

    /**
     * 文本样式，定义了文本的显示效果
     */
    private final Style style;

    /**
     * 创建普通样式文本
     *
     * @param content 文本内容，实现了IMarkdown接口
     * @return Text实例，用于进一步操作或生成Markdown
     */
    public static Text create(IMarkdown content) {
        return new Text(content);
    }

    /**
     * 创建指定样式的文本
     *
     * @param content 文本内容，实现了IMarkdown接口
     * @param style   文本样式，如粗体、斜体、下划线等
     * @return Text实例，用于进一步操作或生成Markdown
     */
    public static Text create(IMarkdown content, Style style) {
        return new Text(content, style);
    }

    /**
     * 创建普通样式文本
     *
     * @param content 文本内容
     * @return Text实例，用于进一步操作或生成Markdown
     */
    public static Text create(String content) {
        return new Text(content);
    }

    /**
     * 创建指定样式的文本
     *
     * @param content 文本内容
     * @param style   文本样式，如粗体、斜体、下划线等
     * @return Text实例，用于进一步操作或生成Markdown
     */
    public static Text create(String content, Style style) {
        return new Text(content, style);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建普通样式文本
     *
     * @param content 文本内容，实现了IMarkdown接口
     */
    private Text(IMarkdown content) {
        this(content.toMarkdown(), null);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建指定样式的文本
     *
     * @param content 文本内容，实现了IMarkdown接口
     * @param style   文本样式，如粗体、斜体、下划线等
     */
    private Text(IMarkdown content, Style style) {
        this(content.toMarkdown(), style);
    }

    /**
     * 私有构造方法，创建普通样式文本
     *
     * @param content 文本内容
     */
    private Text(String content) {
        this(content, null);
    }

    /**
     * 私有构造方法，创建指定样式的文本
     *
     * @param content 文本内容
     * @param style   文本样式，如粗体、斜体、下划线等
     */
    private Text(String content, Style style) {
        append(content);
        this.style = style == null ? Style.NORMAL : style;
    }

    /**
     * 追加Markdown组件内容到文本
     *
     * @param content 要追加的内容，实现了IMarkdown接口
     * @return 当前Text实例，支持链式调用
     */
    public Text append(IMarkdown content) {
        String markdown = content.toMarkdown();
        if (StringUtils.isNotBlank(markdown)) {
            if (this.content.length() > 0) {
                this.content.append(StringUtils.SPACE);
            }
            this.content.append(markdown);
        }
        return this;
    }

    /**
     * 追加文本内容到文本
     *
     * @param content 要追加的文本内容，非空内容才会被追加
     * @return 当前Text实例，支持链式调用
     */
    public Text append(String content) {
        if (StringUtils.isNotBlank(content)) {
            if (this.content.length() > 0) {
                this.content.append(StringUtils.SPACE);
            }
            this.content.append(StringUtils.trimToEmpty(content));
        }
        return this;
    }

    /**
     * 将文本转换为Markdown格式字符串
     *
     * @return Markdown格式的文本字符串，根据样式应用不同的Markdown语法
     */
    @Override
    public String toMarkdown() {
        switch (style) {
            case BOLD:
                return toString("**%s**");
            case ITALIC:
                return toString("*%s*");
            case UNDERLINE:
                return toString("<u>%s</u>");
            case STRIKEOUT:
                return toString("~~%s~~");
            default:
                return content.toString();
        }
    }

    /**
     * 根据指定格式将文本内容转换为字符串
     *
     * @param prefix 格式化前缀，包含%s占位符
     * @return 格式化后的字符串
     */
    private String toString(String prefix) {
        if (content.length() == 0) {
            return StringUtils.EMPTY;
        }
        String[] lines = StringUtils.split(content.toString(), P);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                result.append(P);
            }
            result.append(String.format(prefix, lines[i]));
        }
        return result.toString();
    }

    /**
     * 获取文本的Markdown格式字符串表示
     *
     * @return Markdown格式的文本字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }

    /**
     * 文本样式枚举，定义了支持的文本显示效果
     */
    public enum Style {
        /**
         * 正常样式，无特殊格式
         */
        NORMAL,

        /**
         * 粗体样式，使用**包裹文本
         */
        BOLD,

        /**
         * 斜体样式，使用*包裹文本
         */
        ITALIC,

        /**
         * 下划线样式，使用<u>标签包裹文本
         */
        UNDERLINE,

        /**
         * 删除线样式，使用~~包裹文本
         */
        STRIKEOUT
    }
}
