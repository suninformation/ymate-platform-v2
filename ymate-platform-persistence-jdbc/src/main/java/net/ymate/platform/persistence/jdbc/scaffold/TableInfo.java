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
package net.ymate.platform.persistence.jdbc.scaffold;

import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/19 下午3:34
 */
public final class TableInfo implements Serializable {

    private static final Log LOG = LogFactory.getLog(TableInfo.class);

    public static TableInfo create(IDatabaseConnectionHolder connectionHolder, Scaffold scaffold, String tableName, boolean view) throws Exception {
        DatabaseMetaData databaseMetaData = connectionHolder.getConnection().getMetaData();
        //
        String dbName = scaffold.getDbName();
        if (StringUtils.isBlank(dbName)) {
            dbName = connectionHolder.getConnection().getCatalog();
        }
        String dbUserName = StringUtils.defaultIfBlank(scaffold.getDbUserName(), connectionHolder.getDataSourceConfig().getUsername());
        if (Type.DATABASE.ORACLE.equalsIgnoreCase(connectionHolder.getDialect().getName())) {
            dbUserName = StringUtils.upperCase(dbUserName);
        }
        try (Statement statement = connectionHolder.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            List<String> primaryKeyNames = new ArrayList<>();
            if (!view) {
                try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(dbName, dbUserName, tableName)) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            String columnName = resultSet.getString(4);
                            if (!scaffold.isKeepCase()) {
                                primaryKeyNames.add(columnName.toLowerCase());
                            } else {
                                primaryKeyNames.add(columnName);
                            }
                        }
                    }
                    if (primaryKeyNames.isEmpty()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Database table '%s' no primary key set, ignored.", tableName));
                        }
                        return null;
                    }
                }
            }
            try (ResultSet resultSet = statement.executeQuery(connectionHolder.getDialect().buildPagedQuerySql(String.format("SELECT * FROM %s", connectionHolder.getDialect().wrapIdentifierQuote(tableName)), 1, 1))) {
                ResultSetMetaData tableMetaData = resultSet.getMetaData();
                Map<String, ColumnInfo> columns = new LinkedHashMap<>(tableMetaData.getColumnCount());
                for (int idx = 1; idx <= tableMetaData.getColumnCount(); idx++) {
                    try (ResultSet resultSetColumn = databaseMetaData.getColumns(dbName, dbUserName, tableName, tableMetaData.getColumnName(idx))) {
                        if (resultSetColumn.next()) {
                            // 提取字段定义及字段默认值
                            String name = tableMetaData.getColumnName(idx);
                            if (!scaffold.isKeepCase()) {
                                name = StringUtils.lowerCase(name);
                            }
                            String columnType;
                            switch (tableMetaData.getColumnType(idx)) {
                                case Types.BINARY:
                                case Types.VARBINARY:
                                case Types.LONGVARBINARY:
                                    columnType = "byte[]";
                                    break;
                                default:
                                    columnType = tableMetaData.getColumnClassName(idx);
                            }
                            ColumnInfo column = new ColumnInfo(scaffold, name,
                                    columnType,
                                    tableMetaData.isAutoIncrement(idx),
                                    primaryKeyNames.contains(name),
                                    tableMetaData.isSigned(idx),
                                    tableMetaData.getPrecision(idx),
                                    tableMetaData.getScale(idx),
                                    tableMetaData.isNullable(idx),
                                    scaffold.getReadonlyColumns().contains(name),
                                    resultSetColumn.getString("COLUMN_DEF"),
                                    resultSetColumn.getString("REMARKS"));
                            columns.put(name, column);
                        }
                    }
                }
                String comment = null;
                if (Type.DATABASE.MYSQL.equalsIgnoreCase(connectionHolder.getDialect().getName()) && StringUtils.isNotBlank(dbName)) {
                    try (ResultSet commentResultSet = statement.executeQuery(String.format("SELECT table_comment FROM information_schema.tables WHERE table_schema = '%s' and table_name = '%s'", dbName, tableName))) {
                        if (commentResultSet.next()) {
                            comment = commentResultSet.getString("table_comment");
                        }
                    } catch (Exception ignored) {
                    }
                }
                return new TableInfo(dbName, dbUserName, tableName, comment, columns);
            }
        }
    }

    /**
     * 数据库名称
     */
    private final String catalog;

    /**
     * 数据库用户名
     */
    private final String schema;

    /**
     * 数据表名称
     */
    private final String name;

    /**
     * 数据库表备注
     */
    private final String comment;

    /**
     * 主键字段名称
     */
    private final List<String> primaryKeys = new ArrayList<>();

    /**
     * 字段名称与字段描述信息映射
     */
    private final Map<String, ColumnInfo> columns;

    public TableInfo(String catalog, String schema, String name, String comment, Map<String, ColumnInfo> columns) {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.comment = comment;
        this.columns = columns;
        //
        if (columns != null) {
            columns.values().stream().filter(ColumnInfo::isPrimaryKey).map(ColumnInfo::getColumnName).forEachOrdered(primaryKeys::add);
        }
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getPrimaryKeys() {
        return Collections.unmodifiableList(primaryKeys);
    }

    public Map<String, ColumnInfo> getColumns() {
        return Collections.unmodifiableMap(columns);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableInfo tableInfo = (TableInfo) o;
        return Objects.equals(catalog, tableInfo.catalog) &&
                Objects.equals(schema, tableInfo.schema) &&
                name.equals(tableInfo.name) &&
                Objects.equals(primaryKeys, tableInfo.primaryKeys) &&
                Objects.equals(columns, tableInfo.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, name, primaryKeys, columns);
    }

    @Override
    public String toString() {
        return String.format("TableInfo{catalog='%s', schema='%s', name='%s', primaryKeys=%s, columns=%s}", catalog, schema, name, primaryKeys, columns);
    }
}
