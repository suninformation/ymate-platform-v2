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
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Markdown表格组件，用于生成表格格式的内容。
 * <p>
 * 支持自定义表头、对齐方式、多行数据，通过静态工厂方法创建实例，支持链式调用。
 * 设计目的是提供一种类型安全的方式来构建Markdown表格，
 * 使用场景包括数据展示、对比表格、统计信息等。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2020/2/9 15:34
 */
public final class Table implements IMarkdown {

    /**
     * 表头列表，存储表格的列标题和对齐方式
     */
    private final List<Header> headers = new ArrayList<>();

    /**
     * 行列表，存储表格的数据行
     */
    private final List<Row> rows = new ArrayList<>();

    /**
     * 创建表格实例
     *
     * @return Table实例，用于进一步操作或生成Markdown
     */
    public static Table create() {
        return new Table();
    }

    /**
     * 私有构造方法，防止直接实例化，通过create()静态方法获取实例
     */
    private Table() {
    }

    /**
     * 添加表头，使用默认对齐方式
     *
     * @param title 表头标题，实现了IMarkdown接口
     * @return 当前Table实例，支持链式调用
     */
    public Table addHeader(IMarkdown title) {
        return addHeader(title.toMarkdown());
    }

    /**
     * 添加指定对齐方式的表头
     *
     * @param title 表头标题，实现了IMarkdown接口
     * @param align 对齐方式，如左对齐、右对齐、居中对齐
     * @return 当前Table实例，支持链式调用
     */
    public Table addHeader(IMarkdown title, Align align) {
        return addHeader(title.toMarkdown(), align);
    }

    /**
     * 添加表头，使用默认对齐方式
     *
     * @param title 表头标题文本
     * @return 当前Table实例，支持链式调用
     */
    public Table addHeader(String title) {
        headers.add(new Header(title));
        return this;
    }

    /**
     * 添加指定对齐方式的表头
     *
     * @param title 表头标题文本
     * @param align 对齐方式，如左对齐、右对齐、居中对齐
     * @return 当前Table实例，支持链式调用
     */
    public Table addHeader(String title, Align align) {
        headers.add(new Header(title, align));
        return this;
    }

    /**
     * 添加一行数据
     *
     * @return Row实例，用于添加列数据
     */
    public Row addRow() {
        Row row = new Row(this);
        rows.add(row);
        return row;
    }

    /**
     * 将表格转换为Markdown格式字符串
     * <p>
     * 生成标准的Markdown表格格式，包括表头、对齐线和数据行
     * </p>
     *
     * @return Markdown格式的表格字符串
     */
    @Override
    public String toMarkdown() {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder headerDef = new StringBuilder();
        StringBuilder rowsData = new StringBuilder();
        for (Header header : headers) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("|");
                headerDef.append("|");
            }
            // 直接使用已处理的标题
            stringBuilder.append(header.title).append("|");
            switch (header.align) {
                case LEFT:
                    headerDef.append(":---").append("|");
                    break;
                case RIGHT:
                    headerDef.append("---:").append("|");
                    break;
                case CENTER:
                    headerDef.append(":---:").append("|");
                    break;
                default:
                    headerDef.append("---").append("|");
            }
        }
        if (headerDef.length() > 0) {
            stringBuilder.append(PARAGRAPH_SEPARATOR).append(headerDef).append(PARAGRAPH_SEPARATOR);
        }
        rows.forEach(row -> {
            rowsData.append("|");
            row.getColumns().forEach(column -> {
                // 列内容已经在addColumn方法中处理过，直接使用
                rowsData.append(column).append("|");
            });
            rowsData.append(PARAGRAPH_SEPARATOR);
        });
        return stringBuilder.append(rowsData).toString();
    }

    /**
     * 获取表格的Markdown格式字符串表示
     *
     * @return Markdown格式的表格字符串，与toMarkdown()方法等价
     */
    @Override
    public String toString() {
        return toMarkdown();
    }

    /**
     * 表头内部类，用于存储表头标题和对齐方式
     */
    private static final class Header {

        /**
         * 表头标题，已处理特殊字符和换行符
         */
        String title;

        /**
         * 对齐方式
         */
        Align align;

        /**
         * 构造方法，使用默认对齐方式创建表头
         *
         * @param title 表头标题文本
         */
        Header(String title) {
            this(title, null);
        }

        /**
         * 构造方法，创建指定对齐方式的表头
         *
         * @param title 表头标题文本
         * @param align 对齐方式
         */
        Header(String title, Align align) {
            // 处理标题中的特殊字符和换行符
            String processedTitle = StringUtils.trimToEmpty(title);
            processedTitle = Strings.CS.replace(processedTitle, "|", "\\|");
            processedTitle = StringUtils.replaceEach(processedTitle, new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{"<br/>", "<br/>", "<br/>", TAB});
            this.title = processedTitle;
            this.align = align != null ? align : Align.NORMAL;
        }
    }

    /**
     * 表格行内部类，用于存储一行数据
     */
    public static final class Row {

        /**
         * 所属表格实例
         */
        private final Table table;

        /**
         * 列数据列表
         */
        private final List<String> columns = new ArrayList<>();

        /**
         * 构造方法，创建行实例
         *
         * @param table 所属表格实例
         */
        Row(Table table) {
            this.table = table;
        }

        /**
         * 添加列数据
         *
         * @param content 列文本内容，会处理特殊字符和换行符
         * @return 当前Row实例，支持链式调用
         */
        public Row addColumn(String content) {
            columns.add(StringUtils.replaceEach(content, new String[]{"|", "\r\n", "\r", "\n", "\t"}, new String[]{"\\|", "<br/>", "<br/>", "<br/>", TAB}));
            return this;
        }

        /**
         * 添加Markdown组件作为列数据
         *
         * @param content 列内容，实现了IMarkdown接口
         * @return 当前Row实例，支持链式调用
         */
        public Row addColumn(IMarkdown content) {
            return addColumn(content.toMarkdown());
        }

        /**
         * 获取不可修改的列数据列表
         *
         * @return 列数据列表的不可修改视图
         */
        public List<String> getColumns() {
            return Collections.unmodifiableList(columns);
        }

        /**
         * 完成当前行的添加，返回所属表格实例
         *
         * @return 所属表格实例，用于继续添加行或生成Markdown
         */
        public Table build() {
            return table;
        }
    }

    /**
     * 对齐方式枚举，定义表格列的对齐方式
     */
    public enum Align {

        /**
         * 默认对齐方式，通常为左对齐
         */
        NORMAL,

        /**
         * 左对齐
         */
        LEFT,

        /**
         * 右对齐
         */
        RIGHT,

        /**
         * 居中对齐
         */
        CENTER
    }
}
