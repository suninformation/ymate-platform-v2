/*
 * Copyright 2007-2020 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/2/9 15:34
 */
public class Table implements IMarkdown {

    private List<Header> headers = new ArrayList<>();

    private List<Row> rows = new ArrayList<>();

    public static Table create() {
        return new Table();
    }

    private Table() {
    }

    public Table addHeader(IMarkdown title) {
        return addHeader(title.toMarkdown());
    }

    public Table addHeader(IMarkdown title, Align align) {
        return addHeader(title.toMarkdown(), align);
    }

    public Table addHeader(String title) {
        headers.add(new Header(title));
        return this;
    }

    public Table addHeader(String title, Align align) {
        headers.add(new Header(title, align));
        return this;
    }

    public Row addRow() {
        Row row = new Row(this);
        rows.add(row);
        return row;
    }

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
            stringBuilder.append(P).append(headerDef).append(P);
        }
        rows.forEach(row -> {
            rowsData.append("|");
            row.getColumns().forEach(column -> rowsData.append(column).append("|"));
            rowsData.append(P);
        });
        return stringBuilder.append(rowsData).toString();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }

    static class Header {

        String title;

        Align align;

        Header(String title) {
            this(title, null);
        }

        Header(String title, Align align) {
            this.title = StringUtils.trimToEmpty(title);
            this.align = align != null ? align : Align.NORMAL;
        }
    }

    public static class Row {

        private final Table table;

        private final List<String> columns = new ArrayList<>();

        Row(Table table) {
            this.table = table;
        }

        public Row addColumn(String content) {
            columns.add(StringUtils.replaceEach(content, new String[]{"|", "\r\n", "\r", "\n", "\t"}, new String[]{"\\|", "<br>", StringUtils.EMPTY, "<br>", TAB}));
            return this;
        }

        public Row addColumn(IMarkdown content) {
            return addColumn(content.toMarkdown());
        }

        public List<String> getColumns() {
            return Collections.unmodifiableList(columns);
        }

        public Table build() {
            return table;
        }
    }

    public enum Align {

        /**
         * 默认
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
         * 居中
         */
        CENTER
    }
}
