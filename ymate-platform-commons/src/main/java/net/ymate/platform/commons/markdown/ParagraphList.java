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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Markdown列表组件，用于生成有序列表或无序列表。
 * <p>
 * 支持嵌套列表、添加正文内容，通过静态工厂方法创建实例，支持链式调用。
 * 设计目的是提供一种类型安全的方式来构建Markdown列表，
 * 使用场景包括文档中的项目列表、步骤说明、层级结构展示等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 14:35
 */
public final class ParagraphList implements IMarkdown {

    /**
     * 列表项集合，支持字符串和ParagraphList（用于嵌套列表）
     */
    private final List<Serializable> items = new ArrayList<>();

    /**
     * 是否为有序列表，true表示有序列表，false表示无序列表
     */
    private boolean order;

    /**
     * 创建无序列表
     *
     * @return ParagraphList实例，用于进一步操作或生成Markdown
     */
    public static ParagraphList create() {
        return new ParagraphList();
    }

    /**
     * 创建指定类型的列表
     *
     * @param order true表示有序列表，false表示无序列表
     * @return ParagraphList实例，用于进一步操作或生成Markdown
     */
    public static ParagraphList create(boolean order) {
        return new ParagraphList(order);
    }

    /**
     * 私有构造方法，创建无序列表
     */
    private ParagraphList() {
    }

    /**
     * 私有构造方法，创建指定类型的列表
     *
     * @param order true表示有序列表，false表示无序列表
     */
    private ParagraphList(boolean order) {
        this.order = order;
    }

    /**
     * 添加单个列表项
     *
     * @param item 列表项文本内容，非空内容才会被添加
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addItem(String item) {
        if (StringUtils.isNotBlank(item)) {
            items.add(item);
        }
        return this;
    }

    /**
     * 添加多个列表项
     *
     * @param items 列表项数组，null会被忽略
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addItems(String... items) {
        if (items != null) {
            return addItems(Arrays.asList(items));
        }
        return this;
    }

    /**
     * 添加列表项集合
     *
     * @param items 列表项集合，null或空集合会被忽略
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addItems(List<String> items) {
        if (items != null && !items.isEmpty()) {
            items.stream().filter(StringUtils::isNotBlank).forEachOrdered(this.items::add);
        }
        return this;
    }

    /**
     * 添加子列表
     *
     * @param subItem 子列表实例
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addSubItem(ParagraphList subItem) {
        this.items.add(subItem);
        return this;
    }

    /**
     * 添加单个子列表项，继承当前列表类型
     *
     * @param subItem 子列表项文本内容
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addSubItem(String subItem) {
        items.add(new ParagraphList(order).addItem(subItem));
        return this;
    }

    /**
     * 添加单个指定类型的子列表项
     *
     * @param subItem  子列表项文本内容
     * @param subOrder true表示有序子列表，false表示无序子列表
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addSubItem(String subItem, boolean subOrder) {
        items.add(new ParagraphList(subOrder).addItem(subItem));
        return this;
    }

    /**
     * 添加多个子列表项，继承当前列表类型
     *
     * @param subItems 子列表项数组
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addSubItems(String... subItems) {
        this.items.add(new ParagraphList(order).addItems(subItems));
        return this;
    }

    /**
     * 添加子列表项集合，继承当前列表类型
     *
     * @param subItems 子列表项集合
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addSubItems(List<String> subItems) {
        this.items.add(new ParagraphList(order).addItems(subItems));
        return this;
    }

    /**
     * 添加多个指定类型的子列表项
     *
     * @param subOrder true表示有序子列表，false表示无序子列表
     * @param subItems 子列表项数组
     * @return 当前ParagraphList实例，支持链式调用
     * @since 2.1.4
     */
    public ParagraphList addSubItems(boolean subOrder, String... subItems) {
        this.items.add(new ParagraphList(subOrder).addItems(subItems));
        return this;
    }

    /**
     * 添加指定类型的子列表项集合
     *
     * @param subOrder true表示有序子列表，false表示无序子列表
     * @param subItems 子列表项集合
     * @return 当前ParagraphList实例，支持链式调用
     * @since 2.1.4
     */
    public ParagraphList addSubItems(boolean subOrder, List<String> subItems) {
        this.items.add(new ParagraphList(subOrder).addItems(subItems));
        return this;
    }

    /**
     * 添加Markdown组件作为列表项内容
     *
     * @param body Markdown组件实例
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addBody(IMarkdown body) {
        return addBody(body.toMarkdown());
    }

    /**
     * 添加文本作为列表项内容
     *
     * @param body 文本内容
     * @return 当前ParagraphList实例，支持链式调用
     */
    public ParagraphList addBody(String body) {
        this.items.add(new Body(body));
        return this;
    }

    /**
     * 将列表转换为Markdown格式字符串
     * <p>
     * 根据列表类型生成不同格式：
     * - 有序列表：使用数字. 作为前缀
     * - 无序列表：使用- 作为前缀
     * - 嵌套列表：添加制表符缩进
     * - 正文内容：添加空行分隔
     * </p>
     *
     * @return Markdown格式的列表字符串
     */
    @Override
    public String toMarkdown() {
        StringBuilder stringBuilder = new StringBuilder();
        int idx = 1;
        for (Object object : items) {
            if (object instanceof String) {
                stringBuilder.append(order ? String.format("%d. ", idx) : "- ").append(StringUtils.replaceEach((String) object, new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{StringUtils.SPACE, StringUtils.EMPTY, StringUtils.SPACE, "\t"})).append(PARAGRAPH_SEPARATOR);
            } else if (object instanceof ParagraphList) {
                // 为子列表添加缩进，确保每一行都有制表符
                String subListContent = ((ParagraphList) object).toMarkdown();
                if (StringUtils.isNotBlank(subListContent)) {
                    String[] subListLines = StringUtils.split(subListContent, PARAGRAPH_SEPARATOR);
                    if (subListLines != null) {
                        for (String line : subListLines) {
                            if (StringUtils.isNotBlank(line)) {
                                stringBuilder.append("    ").append(line).append(PARAGRAPH_SEPARATOR);
                            }
                        }
                    }
                }
            } else if (object instanceof Body) {
                stringBuilder.append(PARAGRAPH_SEPARATOR).append(((Body) object).body).append(PARAGRAPH_SEPARATOR);
            }
            idx++;
        }
        return stringBuilder.toString();
    }

    /**
     * 获取列表的Markdown格式字符串表示
     *
     * @return Markdown格式的列表字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }

    /**
     * 列表正文内容包装类，用于区分普通列表项和正文内容
     */
    private static class Body implements Serializable {

        /**
         * 正文内容
         */
        String body;

        /**
         * 构造方法，创建正文内容实例
         *
         * @param body 正文文本内容
         */
        Body(String body) {
            this.body = body;
        }
    }
}
