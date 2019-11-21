/*
 * Copyright 2007-2019 the original author or authors.
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
package net.ymate.platform.commons;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 控制台表格构建工具
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/21 上午12:42
 */
public class ConsoleTableBuilder {

    public static final String TYPE_CSV = "csv";

    public static final String TYPE_MARKDOWN = "markdown";

    private static final int MARGIN = 1;

    private final List<Row> rows = new ArrayList<>();

    private final int column;

    private boolean separateLine;

    private boolean escape;

    /**
     * 输出格式：table|markdown|csv
     */
    private String format;

    public static ConsoleTableBuilder create(int column) {
        return new ConsoleTableBuilder(column);
    }

    public ConsoleTableBuilder(int column) {
        this.column = column;
    }

    public ConsoleTableBuilder markdown() {
        format = TYPE_MARKDOWN;
        return this;
    }

    public ConsoleTableBuilder csv() {
        format = TYPE_CSV;
        return this;
    }

    public ConsoleTableBuilder separateLine() {
        separateLine = true;
        return this;
    }

    public ConsoleTableBuilder escape() {
        escape = true;
        return this;
    }

    public Row addRow() {
        Row row = new Row(this, column);
        this.rows.add(row);
        return row;
    }

    public int[] getColumnLengths() {
        int[] lengths = new int[column];
        for (int idx = 0; idx < column; idx++) {
            int length = 0;
            for (Row row : this.rows) {
                int len = row.getColumnLength(idx);
                if (length < len) {
                    length = len;
                }
            }
            lengths[idx] = length;
        }
        return lengths;
    }

    private String printStr(char c, int len) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    private String printHeader(int[] columnLengths) {
        StringBuilder stringBuilder = new StringBuilder("+");
        for (int idx = 0; idx < columnLengths.length; idx++) {
            stringBuilder.append(printStr('-', columnLengths[idx] + MARGIN * 2)).append('+');
            if (idx == columnLengths.length - 1) {
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        //
        boolean markdown = StringUtils.equals(format, TYPE_MARKDOWN);
        boolean csv = StringUtils.equals(format, TYPE_CSV);
        //
        if (csv) {
            rows.forEach(row -> {
                IntStream.range(0, this.column).forEachOrdered(columnIdx -> {
                    Column currColumn = row.getColumns().get(columnIdx);
                    stringBuilder.append(currColumn.getContent());
                    if (columnIdx < this.column - 1) {
                        stringBuilder.append(',');
                    }
                });
                stringBuilder.append("\n");
            });
        } else {
            int[] columnLengths;
            if (markdown) {
                columnLengths = new int[0];
            } else {
                columnLengths = this.getColumnLengths();
            }
            //
            if (!markdown) {
                stringBuilder.append(printHeader(columnLengths));
            }
            //
            int rowIdx = 0;
            for (Row row : rows) {
                for (int columnIdx = 0; columnIdx < this.column; columnIdx++) {
                    String content = "";
                    int length = 0;
                    if (columnIdx < row.getColumns().size()) {
                        Column currColumn = row.getColumns().get(columnIdx);
                        content = currColumn.getContent();
                        length = currColumn.getLength();
                    }
                    stringBuilder.append('|');
                    //
                    if (!markdown) {
                        stringBuilder.append(printStr(' ', MARGIN)).append(content).append(printStr(' ', columnLengths[columnIdx] - length + MARGIN));
                    } else {
                        stringBuilder.append(content);
                    }
                }
                stringBuilder.append("|\n");
                if (!markdown) {
                    if (separateLine || rowIdx == 0 || rowIdx == rows.size() - 1) {
                        stringBuilder.append(printHeader(columnLengths));
                    }
                } else if (rowIdx <= 0) {
                    stringBuilder.append("|");
                    for (int idx = 0; idx < column; idx++) {
                        stringBuilder.append(printStr('-', 3)).append("|");
                    }
                    stringBuilder.append("\n");
                }
                rowIdx++;
            }
        }
        //
        return stringBuilder.toString();
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(toString().getBytes())) {
            IOUtils.copyLarge(inputStream, outputStream);
        }
    }

    public static class Column {

        private final int length;

        private final String content;

        public Column(String content) {
            if (content == null) {
                content = "NULL";
            }
            this.content = content;
            this.length = content.getBytes().length;
        }

        public int getLength() {
            return this.length;
        }

        public String getContent() {
            return content;
        }
    }

    public static class Row {

        private final List<Column> columns;

        private final ConsoleTableBuilder builder;

        public Row(ConsoleTableBuilder builder, int column) {
            this.builder = builder;
            this.columns = new ArrayList<>(column);
        }

        public Row addColumn(String content) {
            boolean csv = StringUtils.equals(builder.format, TYPE_CSV);
            boolean markdown = StringUtils.equals(builder.format, TYPE_MARKDOWN);
            if (!csv && builder.escape) {
                if (markdown) {
                    content = StringUtils.replaceEach(content, new String[]{"_", "|", "\r\n", "\r", "\n", "\t"}, new String[]{"\\_", "\\|", "<br>", "", "<br>", "    "});
                } else {
                    content = StringUtils.replaceEach(content, new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{"[\\r][\\n]", "[\\r]", "[\\n]", "[\\t]"});
                }
            }
            if (csv) {
                if (StringUtils.contains(content, '"')) {
                    content = StringUtils.replace(content, "\"", "\"\"");
                }
                if (StringUtils.contains(content, ',')) {
                    content = String.format("\"%s\"", content);
                }
            }
            this.columns.add(new Column(content));
            return this;
        }

        public ConsoleTableBuilder builder() {
            return builder;
        }

        public List<Column> getColumns() {
            return this.columns;
        }

        public int getColumnLength(int column) {
            if (column >= this.columns.size()) {
                return 0;
            }
            return this.columns.get(column).getLength();
        }
    }
}
