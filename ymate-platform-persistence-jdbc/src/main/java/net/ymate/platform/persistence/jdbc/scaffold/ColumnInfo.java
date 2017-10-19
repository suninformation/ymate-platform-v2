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
package net.ymate.platform.persistence.jdbc.scaffold;

import net.ymate.platform.persistence.base.EntityMeta;
import org.apache.commons.lang.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/19 下午3:18
 * @version 1.0
 */
public class ColumnInfo {

    public static Map<String, ColumnInfo> create(ConfigInfo configInfo, String dbType, String tableName, List<String> primaryKeys, DatabaseMetaData databaseMetaData, ResultSetMetaData metaData) throws SQLException {
        Map<String, ColumnInfo> _returnValue = new LinkedHashMap<String, ColumnInfo>(metaData.getColumnCount());
        //
        System.out.println(">>> " + "COLUMN_NAME / " +
                "COLUMN_CLASS_NAME / " +
                "PRIMARY_KEY / " +
                "AUTO_INCREMENT / " +
                "SIGNED / " +
                "PRECISION / " +
                "SCALE / " +
                "NULLABLE / " +
                "DEFAULT / " +
                "REMARKS");
        //
        for (int _idx = 1; _idx <= metaData.getColumnCount(); _idx++) {
            // 获取字段元数据对象
            ResultSet _column = databaseMetaData.getColumns(configInfo.getDbName(),
                    dbType.equalsIgnoreCase("oracle") ? configInfo.getDbUserName().toUpperCase() : configInfo.getDbUserName(), tableName, metaData.getColumnName(_idx));
            if (_column.next()) {
                // 提取字段定义及字段默认值
                String _name = metaData.getColumnName(_idx).toLowerCase();
                ColumnInfo _columnInfo = new ColumnInfo(
                        configInfo.getNamedFilter(),
                        _name,
                        metaData.getColumnClassName(_idx),
                        metaData.isAutoIncrement(_idx),
                        metaData.isSigned(_idx),
                        metaData.getPrecision(_idx),
                        metaData.getScale(_idx),
                        metaData.isNullable(_idx),
                        _column.getString("COLUMN_DEF"),
                        _column.getString("REMARKS"));
                _returnValue.put(_name, _columnInfo);
                //
                System.out.println("--> " + _name + "\t" +
                        _columnInfo.getColumnName() + "\t" +
                        primaryKeys.contains(_name) + "\t" +
                        _columnInfo.isAutoIncrement() + "\t" +
                        _columnInfo.isSigned() + "\t" +
                        _columnInfo.getPrecision() + "\t" +
                        _columnInfo.getScale() + "\t" +
                        _columnInfo.isNullable() + "\t" +
                        _columnInfo.getDefaultValue() + "\t" +
                        _columnInfo.getRemarks());
            }
            _column.close();
        }
        return _returnValue;
    }

    private String name;

    private String columnName;

    private String columnType;

    private boolean autoIncrement;

    private boolean signed;

    private int precision;

    private int scale;

    private boolean nullable;

    private String defaultValue;

    private String remarks;

    public ColumnInfo(IEntityNamedFilter namedFilter, String columnName, String columnType, boolean autoIncrement, boolean signed, int precision, int scale, int nullable, String defaultValue, String remarks) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.autoIncrement = autoIncrement;
        this.signed = signed;
        this.precision = precision;
        this.scale = scale;
        this.nullable = nullable <= 0;
        this.defaultValue = defaultValue;
        this.remarks = remarks;
        //
        if (namedFilter != null) {
            this.name = StringUtils.defaultIfBlank(namedFilter.doFilter(columnName), columnName);
        } else {
            this.name = columnName;
        }
        this.name = StringUtils.uncapitalize(EntityMeta.propertyNameToFieldName(this.name.toLowerCase()));
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isSigned() {
        return signed;
    }

    public int getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getRemarks() {
        return remarks;
    }

    public Attr toAttr() {
        return new Attr(getColumnType(), this.name, getColumnName(), isAutoIncrement(), isSigned(), getPrecision(), getScale(), isNullable(), getDefaultValue(), getRemarks());
    }
}
