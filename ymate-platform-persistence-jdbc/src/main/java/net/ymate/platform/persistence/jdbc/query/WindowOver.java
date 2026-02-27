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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.LambdaUtils.SFunction;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 窗口函数OVER子句
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
public class WindowOver extends Query<WindowOver> {

    private final StringBuilder partitionByBuilder;

    private final OrderBy orderBy;

    private String windowFrame;

    private final Params params;

    public static WindowOver create() {
        return create(JDBC.get());
    }

    public static WindowOver create(IDatabase owner) {
        return new WindowOver(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static WindowOver create(IDatabase owner, String dataSourceName) {
        return new WindowOver(owner, dataSourceName);
    }

    public static WindowOver create(Query<?> query) {
        return new WindowOver(query.owner(), query.dataSourceName());
    }

    public WindowOver(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
        partitionByBuilder = new StringBuilder();
        orderBy = OrderBy.create(owner, dataSourceName);
        params = Params.create();
    }

    /**
     * 设置分区字段
     *
     * @param field 分区字段
     * @return 当前WindowOver实例
     */
    public WindowOver partitionBy(String field) {
        return partitionBy(null, field);
    }

    /**
     * 设置分区字段（带前缀）
     *
     * @param prefix 前缀
     * @param fields 分区字段
     * @return 当前WindowOver实例
     */
    public WindowOver partitionBy(String prefix, String... fields) {
        if (ArrayUtils.isNotEmpty(fields)) {
            for (String field : fields) {
                if (StringUtils.isNotBlank(field)) {
                    if (partitionByBuilder.length() > 0) {
                        partitionByBuilder.append(LINE_END_FLAG);
                    }
                    if (StringUtils.isNotBlank(prefix)) {
                        partitionByBuilder.append(prefix).append(".");
                    }
                    partitionByBuilder.append(field);
                }
            }
        }
        return this;
    }

    /**
     * 通过Lambda表达式设置分区字段
     *
     * @param columns Lambda表达式
     * @param <T>     实体类型
     * @param <R>     返回值类型
     * @return 当前WindowOver实例
     */
    @SafeVarargs
    public final <T, R> WindowOver partitionBy(SFunction<T, R>... columns) {
        return partitionBy(null, columns);
    }

    /**
     * 通过Lambda表达式设置分区字段（带前缀）
     *
     * @param prefix  前缀
     * @param columns Lambda表达式
     * @param <T>     实体类型
     * @param <R>     返回值类型
     * @return 当前WindowOver实例
     */
    @SafeVarargs
    public final <T, R> WindowOver partitionBy(String prefix, SFunction<T, R>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            for (SFunction<T, R> column : columns) {
                String columnName = getColumnName(column);
                if (StringUtils.isNotBlank(columnName)) {
                    if (partitionByBuilder.length() > 0) {
                        partitionByBuilder.append(LINE_END_FLAG);
                    }
                    if (StringUtils.isNotBlank(prefix)) {
                        partitionByBuilder.append(prefix).append(".");
                    }
                    partitionByBuilder.append(columnName);
                }
            }
        }
        return this;
    }

    /**
     * 通过Fields对象设置分区字段
     *
     * @param fields Fields对象
     * @return 当前WindowOver实例
     */
    public WindowOver partitionBy(Fields fields) {
        return partitionBy(null, fields);
    }

    /**
     * 通过Fields对象设置分区字段（带前缀）
     *
     * @param prefix 前缀
     * @param fields Fields对象
     * @return 当前WindowOver实例
     */
    public WindowOver partitionBy(String prefix, Fields fields) {
        if (fields != null && !fields.isEmpty()) {
            for (String field : fields.fields()) {
                if (StringUtils.isNotBlank(field)) {
                    if (partitionByBuilder.length() > 0) {
                        partitionByBuilder.append(LINE_END_FLAG);
                    }
                    if (StringUtils.isNotBlank(prefix)) {
                        partitionByBuilder.append(prefix).append(".");
                    }
                    partitionByBuilder.append(field);
                }
            }
        }
        return this;
    }

    /**
     * 设置排序
     *
     * @param orderBy OrderBy对象
     * @return 当前WindowOver实例
     */
    public WindowOver orderBy(OrderBy orderBy) {
        if (orderBy != null) {
            this.orderBy.orderBy(orderBy);
            params.add(orderBy.params());
        }
        return this;
    }

    /**
     * 添加升序排序
     *
     * @param field 字段名
     * @return 当前WindowOver实例
     */
    public WindowOver orderByAsc(String field) {
        orderBy.asc(null, field, false);
        return this;
    }

    /**
     * 添加升序排序（带前缀）
     *
     * @param prefix 前缀
     * @param field  字段名
     * @return 当前WindowOver实例
     */
    public WindowOver orderByAsc(String prefix, String field) {
        orderBy.asc(prefix, field, false);
        return this;
    }

    /**
     * 通过Lambda表达式添加升序排序
     *
     * @param column Lambda表达式
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前WindowOver实例
     */
    public <T, R> WindowOver orderByAsc(SFunction<T, R> column) {
        orderBy.asc(column);
        return this;
    }

    /**
     * 通过Lambda表达式添加升序排序（带前缀）
     *
     * @param prefix 前缀
     * @param column Lambda表达式
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前WindowOver实例
     */
    public <T, R> WindowOver orderByAsc(String prefix, SFunction<T, R> column) {
        orderBy.asc(prefix, column);
        return this;
    }

    /**
     * 添加降序排序
     *
     * @param field 字段名
     * @return 当前WindowOver实例
     */
    public WindowOver orderByDesc(String field) {
        orderBy.desc(null, field, false);
        return this;
    }

    /**
     * 添加降序排序（带前缀）
     *
     * @param prefix 前缀
     * @param field  字段名
     * @return 当前WindowOver实例
     */
    public WindowOver orderByDesc(String prefix, String field) {
        orderBy.desc(prefix, field, false);
        return this;
    }

    /**
     * 通过Lambda表达式添加降序排序
     *
     * @param column Lambda表达式
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前WindowOver实例
     */
    public <T, R> WindowOver orderByDesc(SFunction<T, R> column) {
        orderBy.desc(column);
        return this;
    }

    /**
     * 通过Lambda表达式添加降序排序（带前缀）
     *
     * @param prefix 前缀
     * @param column Lambda表达式
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前WindowOver实例
     */
    public <T, R> WindowOver orderByDesc(String prefix, SFunction<T, R> column) {
        orderBy.desc(prefix, column);
        return this;
    }

    /**
     * 设置窗口框架（ROWS BETWEEN）
     *
     * @param frame 窗口框架字符串，如 "ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW"
     * @return 当前WindowOver实例
     */
    public WindowOver rowsBetween(String frame) {
        this.windowFrame = "ROWS " + frame;
        return this;
    }

    /**
     * 设置窗口框架（RANGE BETWEEN）
     *
     * @param frame 窗口框架字符串，如 "RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW"
     * @return 当前WindowOver实例
     */
    public WindowOver rangeBetween(String frame) {
        this.windowFrame = "RANGE " + frame;
        return this;
    }

    /**
     * 设置窗口框架（GROUPS BETWEEN）
     *
     * @param frame 窗口框架字符串，如 "GROUPS BETWEEN 1 PRECEDING AND 1 FOLLOWING"
     * @return 当前WindowOver实例
     */
    public WindowOver groupsBetween(String frame) {
        this.windowFrame = "GROUPS " + frame;
        return this;
    }

    /**
     * 窗口框架：从分区开始到当前行
     *
     * @return 当前WindowOver实例
     */
    public WindowOver rowsUnboundedPreceding() {
        return rowsBetween("BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW");
    }

    /**
     * 窗口框架：从当前行到分区结束
     *
     * @return 当前WindowOver实例
     */
    public WindowOver rowsUnboundedFollowing() {
        return rowsBetween("BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING");
    }

    /**
     * 窗口框架：整个分区
     *
     * @return 当前WindowOver实例
     */
    public WindowOver rowsBetweenUnbounded() {
        return rowsBetween("BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING");
    }

    /**
     * 窗口框架：当前行
     *
     * @return 当前WindowOver实例
     */
    public WindowOver rowsCurrentRow() {
        return rowsBetween("BETWEEN CURRENT ROW AND CURRENT ROW");
    }

    /**
     * 窗口框架：从当前行往前n行到当前行
     *
     * @param n 行数
     * @return 当前WindowOver实例
     */
    public WindowOver rowsPreceding(int n) {
        return rowsBetween("BETWEEN " + n + " PRECEDING AND CURRENT ROW");
    }

    /**
     * 窗口框架：从当前行到往后n行
     *
     * @param n 行数
     * @return 当前WindowOver实例
     */
    public WindowOver rowsFollowing(int n) {
        return rowsBetween("BETWEEN CURRENT ROW AND " + n + " FOLLOWING");
    }

    /**
     * 窗口框架：从往前n行到往后n行
     *
     * @param preceding 往前行数
     * @param following 往后行数
     * @return 当前WindowOver实例
     */
    public WindowOver rowsBetween(int preceding, int following) {
        return rowsBetween("BETWEEN " + preceding + " PRECEDING AND " + following + " FOLLOWING");
    }

    public Params params() {
        return params;
    }

    public WindowOver param(Object param) {
        params.add(param);
        return this;
    }

    public WindowOver param(Params params) {
        this.params.add(params);
        return this;
    }

    public boolean isEmpty() {
        return partitionByBuilder.length() == 0 && orderBy.isEmpty() && StringUtils.isBlank(windowFrame);
    }

    public String toSQL() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("${over}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        if (variables.contains("over")) {
            StringBuilder overBuilder = new StringBuilder("OVER (");
            boolean hasContent = false;

            if (partitionByBuilder.length() > 0) {
                overBuilder.append("PARTITION BY ").append(partitionByBuilder);
                hasContent = true;
            }

            if (!orderBy.isEmpty()) {
                if (hasContent) {
                    overBuilder.append(" ");
                }
                overBuilder.append(orderBy.toSQL());
                hasContent = true;
            }

            if (StringUtils.isNotBlank(windowFrame)) {
                if (hasContent) {
                    overBuilder.append(" ");
                }
                overBuilder.append(windowFrame);
            }

            overBuilder.append(")");
            expression.set("over", overBuilder.toString());
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }

    @Override
    public String toString() {
        return toSQL();
    }
}
