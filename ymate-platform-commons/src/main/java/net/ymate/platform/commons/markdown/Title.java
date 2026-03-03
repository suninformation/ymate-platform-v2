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
 * Markdown标题组件，用于生成不同级别的标题。
 * <p>
 * 支持1-6级标题，通过静态工厂方法创建实例，支持链式调用追加内容。
 * 设计目的是提供一种类型安全的方式来构建Markdown标题，
 * 使用场景包括文档标题、章节标题、小标题等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 12:21
 */
public final class Title implements IMarkdown {

    /**
     * 标题内容构建器，用于存储和构建标题文本
     */
    public final StringBuilder title = new StringBuilder();

    /**
     * 标题级别，范围1-6
     */
    private final int level;

    /**
     * 创建一级标题
     *
     * @param title 标题内容，实现了IMarkdown接口
     * @return Title实例，用于进一步操作或生成Markdown
     */
    public static Title create(IMarkdown title) {
        return new Title(title);
    }

    /**
     * 创建一级标题
     *
     * @param title 标题文本内容
     * @return Title实例，用于进一步操作或生成Markdown
     */
    public static Title create(String title) {
        return new Title(title);
    }

    /**
     * 创建指定级别的标题
     *
     * @param title 标题内容，实现了IMarkdown接口
     * @param level 标题级别，范围1-6，超出范围会自动调整
     * @return Title实例，用于进一步操作或生成Markdown
     */
    public static Title create(IMarkdown title, int level) {
        return new Title(title, level);
    }

    /**
     * 创建指定级别的标题
     *
     * @param title 标题文本内容
     * @param level 标题级别，范围1-6，超出范围会自动调整
     * @return Title实例，用于进一步操作或生成Markdown
     */
    public static Title create(String title, int level) {
        return new Title(title, level);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建一级标题
     *
     * @param title 标题内容，实现了IMarkdown接口
     */
    private Title(IMarkdown title) {
        this(title.toMarkdown());
    }

    /**
     * 私有构造方法，创建一级标题
     *
     * @param title 标题文本内容
     */
    private Title(String title) {
        this(title, 1);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建指定级别的标题
     *
     * @param title 标题内容，实现了IMarkdown接口
     * @param level 标题级别，范围1-6，超出范围会自动调整
     */
    private Title(IMarkdown title, int level) {
        this(title.toMarkdown(), level);
    }

    /**
     * 私有构造方法，创建指定级别的标题
     *
     * @param title 标题文本内容
     * @param level 标题级别，范围1-6，超出范围会自动调整
     */
    private Title(String title, int level) {
        append(title);
        this.level = level <= 0 ? 1 : Math.min(level, 6);
    }

    /**
     * 追加Markdown组件内容到标题
     *
     * @param content 要追加的内容，实现了IMarkdown接口
     * @return 当前Title实例，支持链式调用
     */
    public Title append(IMarkdown content) {
        return append(content.toMarkdown());
    }

    /**
     * 追加文本内容到标题
     *
     * @param content 要追加的文本内容，非空内容才会被追加
     * @return 当前Title实例，支持链式调用
     */
    public Title append(String content) {
        if (StringUtils.isNotBlank(content)) {
            if (this.title.length() > 0) {
                this.title.append(" ");
            }
            this.title.append(StringUtils.trimToEmpty(content));
        }
        return this;
    }

    /**
     * 将标题转换为Markdown格式字符串
     *
     * @return Markdown格式的标题字符串，若标题内容为空则返回空字符串
     */
    @Override
    public String toMarkdown() {
        if (title.length() == 0) {
            return StringUtils.EMPTY;
        }
        return String.format("%s %s", StringUtils.repeat('#', level), StringUtils.replaceEach(title.toString(), new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{StringUtils.SPACE, StringUtils.EMPTY, StringUtils.SPACE, TAB}));
    }

    /**
     * 获取标题的Markdown格式字符串表示
     *
     * @return Markdown格式的标题字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }

    /**
     * 获取标题的原始文本内容（未转换为Markdown格式）
     *
     * @return 标题的原始文本内容
     */
    public String getTitle() {
        return title.toString();
    }
}
