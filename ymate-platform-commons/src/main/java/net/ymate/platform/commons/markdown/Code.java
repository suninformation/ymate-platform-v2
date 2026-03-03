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
 * Markdown代码块组件，用于生成代码块或行内代码。
 * <p>
 * 支持单行代码（使用反引号包裹）和多行代码块（使用三个反引号包裹），
 * 支持指定编程语言用于语法高亮，支持链式调用追加内容。
 * 设计目的是提供一种类型安全的方式来构建Markdown代码表示，
 * 使用场景包括代码示例、命令行输出、配置文件展示等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:16
 */
public final class Code implements IMarkdown {

    /**
     * 代码内容构建器，用于存储和构建代码文本
     */
    private final StringBuilder code = new StringBuilder();

    /**
     * 代码语言，用于语法高亮，为空字符串表示不指定语言
     */
    private final String language;

    /**
     * 记录是否明确指定了语言（即使是空字符串）
     */
    private final boolean hasExplicitLanguage;

    /**
     * 创建代码块，使用行内代码还是代码块由内容自动决定
     *
     * @param code 代码内容，实现了IMarkdown接口
     * @return Code实例，用于进一步操作或生成Markdown
     */
    public static Code create(IMarkdown code) {
        return new Code(code);
    }

    /**
     * 创建指定语言的代码块
     *
     * @param code     代码内容，实现了IMarkdown接口
     * @param language 代码语言，用于语法高亮
     * @return Code实例，用于进一步操作或生成Markdown
     */
    public static Code create(IMarkdown code, String language) {
        return new Code(code, language);
    }

    /**
     * 创建代码块，使用行内代码还是代码块由内容自动决定
     *
     * @param code 代码文本内容
     * @return Code实例，用于进一步操作或生成Markdown
     */
    public static Code create(String code) {
        return new Code(code);
    }

    /**
     * 创建指定语言的代码块
     *
     * @param code     代码文本内容
     * @param language 代码语言，用于语法高亮
     * @return Code实例，用于进一步操作或生成Markdown
     */
    public static Code create(String code, String language) {
        return new Code(code, language);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建代码块
     *
     * @param code 代码内容，实现了IMarkdown接口
     */
    private Code(IMarkdown code) {
        this(code.toMarkdown(), null);
    }

    /**
     * 私有构造方法，通过IMarkdown对象创建指定语言的代码块
     *
     * @param code     代码内容，实现了IMarkdown接口
     * @param language 代码语言，用于语法高亮
     */
    private Code(IMarkdown code, String language) {
        this(code.toMarkdown(), language);
    }

    /**
     * 私有构造方法，创建代码块
     *
     * @param code 代码文本内容
     */
    private Code(String code) {
        this(code, null);
    }

    /**
     * 私有构造方法，创建指定语言的代码块
     *
     * @param code     代码文本内容，null会被转换为空字符串
     * @param language 代码语言，用于语法高亮，null表示不明确指定语言
     */
    private Code(String code, String language) {
        this.code.append(code == null ? StringUtils.EMPTY : code);
        // 保留原始语言（包括空白），但只在非null时设置
        this.language = language == null ? StringUtils.EMPTY : language;
        // 如果语言参数不是null，则表示明确指定了语言
        this.hasExplicitLanguage = language != null;
    }

    /**
     * 追加代码内容
     *
     * @param code 要追加的代码内容，null会被转换为空字符串
     * @return 当前Code实例，支持链式调用
     */
    public Code append(String code) {
        this.code.append(code == null ? StringUtils.EMPTY : code);
        return this;
    }

    /**
     * 将代码转换为Markdown格式字符串
     * <p>
     * 自动判断使用行内代码还是代码块：
     * - 当明确指定了语言、包含换行符或回车符、或内容为空时，使用代码块（```）
     * - 否则使用行内代码（`）
     * </p>
     *
     * @return Markdown格式的代码字符串，若代码内容为空则返回空字符串
     */
    @Override
    public String toMarkdown() {
        if (code.length() == 0) {
            return StringUtils.EMPTY;
        }
        String codeContent = code.toString();
        String trimmedContent = codeContent.trim();
        if (StringUtils.isBlank(trimmedContent)) {
            return StringUtils.EMPTY;
        }
        // 检查是否包含实际的换行符（LF）在内容中间
        boolean hasMiddleNewline = codeContent.trim().contains(StringUtils.LF);
        boolean hasNonBlankLanguage = hasExplicitLanguage && StringUtils.isNotBlank(language.trim());
        // 当明确指定了非空白语言或内容中间包含换行符时，使用代码块
        if (hasNonBlankLanguage || hasMiddleNewline) {
            // 处理换行符，确保格式正确
            String normalizedContent = codeContent;
            // 处理末尾换行，只保留一个
            while (normalizedContent.endsWith(StringUtils.LF)) {
                normalizedContent = normalizedContent.substring(0, normalizedContent.length() - 1);
            }
            return String.format("```%s\n%s\n```\n", language, normalizedContent);
        }
        // 行内代码使用修剪后的内容，将回车符替换为空格
        String inlineContent = trimmedContent.replace(StringUtils.CR, StringUtils.SPACE);
        return String.format("`%s`", inlineContent);
    }

    /**
     * 获取代码的Markdown格式字符串表示
     *
     * @return Markdown格式的代码字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }
}
