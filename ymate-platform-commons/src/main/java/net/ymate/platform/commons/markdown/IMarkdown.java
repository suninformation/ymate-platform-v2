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

import java.io.Serializable;

/**
 * Markdown组件接口，定义了将对象转换为Markdown格式字符串的标准方法。
 * <p>
 * 所有Markdown组件类都实现此接口，用于统一生成Markdown格式内容。
 * 设计目的是提供一种类型安全的方式来构建和渲染Markdown文档，
 * 使用场景包括动态生成Markdown文档、博客系统、文档生成工具等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2018/8/27 下午4:17
 */
public interface IMarkdown extends Serializable {

    /**
     * 水平分隔线Markdown语法常量，格式为6个连字符加换行符
     */
    String HR = "------\n";

    /**
     * 制表符常量，用于缩进，包含4个空格
     */
    String TAB = "    ";

    /**
     * 换行符常量，使用系统默认换行符
     */
    String P = StringUtils.LF;

    /**
     * 段落分隔符常量，与换行符相同
     */
    String PARAGRAPH_SEPARATOR = P;

    /**
     * 将当前对象转换为Markdown格式字符串
     *
     * @return Markdown格式的字符串，若无法生成有效内容则返回空字符串
     */
    String toMarkdown();
}
