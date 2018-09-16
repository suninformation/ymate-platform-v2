/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.core.support;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制台表格构建工具
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/21 上午12:42
 * @version 1.0
 */
public class ConsoleTableBuilder {

    private static final int __margin = 1;

    private final List<Row> rows = new ArrayList<Row>();

    private final int column;

    private boolean __separateLine;

    private boolean __escape;

    /**
     * 输出格式：table|markdown|csv
     */
    private String __format;

    public static ConsoleTableBuilder create(int column) {
        return new ConsoleTableBuilder(column);
    }

    public ConsoleTableBuilder(int column) {
        this.column = column;
    }

    public ConsoleTableBuilder markdown() {
        __format = "markdown";
        return this;
    }

    public ConsoleTableBuilder csv() {
        __format = "csv";
        return this;
    }

    public ConsoleTableBuilder separateLine() {
        __separateLine = true;
        return this;
    }

    public ConsoleTableBuilder escape() {
        __escape = true;
        return this;
    }

    public Row addRow() {
        Row _row = new Row(this, column);
        this.rows.add(_row);
        return _row;
    }

    public int[] getColumnLengths() {
        int[] _lengths = new int[column];
        for (int _idx = 0; _idx < column; _idx++) {
            int _length = 0;
            for (Row _row : this.rows) {
                int _len = _row.getColumnLength(_idx);
                if (_length < _len) {
                    _length = _len;
                }
            }
            _lengths[_idx] = _length;
        }
        return _lengths;
    }

    private String __printStr(char c, int len) {
        StringBuilder _sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            _sb.append(c);
        }
        return _sb.toString();
    }

    private String __printHeader(int[] columnLengths) {
        StringBuilder _sb = new StringBuilder("+");
        for (int _idx = 0; _idx < columnLengths.length; _idx++) {
            _sb.append(__printStr('-', columnLengths[_idx] + __margin * 2)).append('+');
            if (_idx == columnLengths.length - 1) {
                _sb.append('\n');
            }
        }
        //
        return _sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder _sb = new StringBuilder();
        //
        boolean _markdownFmt = StringUtils.equals(__format, "markdown");
        boolean _csvFmt = StringUtils.equals(__format, "csv");
        //
        if (_csvFmt) {
            for (Row _row : rows) {
                for (int _columnIdx = 0; _columnIdx < this.column; _columnIdx++) {
                    Column _column = _row.getColumns().get(_columnIdx);
                    _sb.append(_column.getContent());
                    if (_columnIdx < this.column - 1) {
                        _sb.append(',');
                    }
                }
                _sb.append("\n");
            }
        } else {
            int[] _columnLengths;
            if (_markdownFmt) {
                _columnLengths = new int[0];
            } else {
                _columnLengths = this.getColumnLengths();
            }
            //
            if (!_markdownFmt) {
                _sb.append(__printHeader(_columnLengths));
            }
            //
            int _rowIdx = 0;
            for (Row _row : rows) {
                for (int _columnIdx = 0; _columnIdx < this.column; _columnIdx++) {
                    String _content = "";
                    int _length = 0;
                    if (_columnIdx < _row.getColumns().size()) {
                        Column _column = _row.getColumns().get(_columnIdx);
                        _content = _column.getContent();
                        _length = _column.getLength();
                    }
                    _sb.append('|');
                    //
                    if (!_markdownFmt) {
                        _sb.append(__printStr(' ', __margin)).append(_content).append(__printStr(' ', _columnLengths[_columnIdx] - _length + __margin));
                    } else {
                        _sb.append(_content);
                    }
                }
                _sb.append("|\n");
                if (!_markdownFmt) {
                    if (__separateLine || _rowIdx == 0 || _rowIdx == rows.size() - 1) {
                        _sb.append(__printHeader(_columnLengths));
                    }
                } else if (_rowIdx <= 0) {
                    _sb.append("|");
                    for (int _idx = 0; _idx < column; _idx++) {
                        _sb.append(__printStr('-', 3)).append("|");
                    }
                    _sb.append("\n");
                }
                _rowIdx++;
            }
        }
        //
        return _sb.toString();
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
            this.columns = new ArrayList<Column>(column);
        }

        public Row addColumn(String content) {
            boolean _csv = StringUtils.equals(builder.__format, "csv");
            boolean _markdown = StringUtils.equals(builder.__format, "markdown");
            if (!_csv && builder.__escape) {
                if (_markdown) {
                    content = StringUtils.replaceEach(content, new String[]{"_", "|", "\r\n", "\r", "\n", "\t"}, new String[]{"\\_", "\\|", "<br>", "", "<br>", ""});
                } else {
                    content = StringUtils.replaceEach(content, new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{"[\\r][\\n]", "[\\r]", "[\\n]", "[\\t]"});
                }
            }
            if (_csv) {
                if (StringUtils.contains(content, '"')) {
                    content = StringUtils.replace(content, "\"", "\"\"");
                }
                if (StringUtils.contains(content, ',')) {
                    content = "\"" + content + "\"";
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
            Column _column = this.columns.get(column);
            return _column.getLength();
        }
    }
}
