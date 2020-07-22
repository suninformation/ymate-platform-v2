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

import net.ymate.platform.core.persistence.base.EntityMeta;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/19 下午3:18
 */
public class ColumnInfo implements Serializable {

    /**
     * 字段映射属性名称
     */
    private String name;

    /**
     * 字段名称
     */
    private final String columnName;

    /**
     * 字段类型
     */
    private final String columnType;

    /**
     * 是否为自增字段
     */
    private final boolean autoIncrement;

    /**
     * 是否为主键
     */
    private final boolean primaryKey;

    /**
     * 是否有符号
     */
    private final boolean signed;

    /**
     * 字段精度
     */
    private final int precision;

    /**
     * 字段长度/位数
     */
    private final int scale;

    /**
     * 是否可空
     */
    private final boolean nullable;

    /**
     * 是否只读
     */
    private final boolean readonly;

    /**
     * 默认值
     */
    private final String defaultValue;

    /**
     * 备注
     */
    private final String remarks;

    public ColumnInfo(INamedFilter namedFilter, String columnName, String columnType, boolean autoIncrement, boolean primaryKey, boolean signed, int precision, int scale, int nullable, boolean readonly, String defaultValue, String remarks) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.autoIncrement = autoIncrement;
        this.primaryKey = primaryKey;
        this.signed = signed;
        this.precision = precision;
        this.scale = scale;
        this.nullable = nullable > 0;
        this.readonly = readonly;
        this.defaultValue = defaultValue;
        this.remarks = StringUtils.replaceEach(remarks, new String[]{"\"", "\r\n", "\r", "\n", "\t"}, new String[]{"\\\"", "[\\r][\\n]", "[\\r]", "[\\n]", "[\\t]"});
        //
        if (namedFilter != null) {
            this.name = StringUtils.defaultIfBlank(namedFilter.filter(INamedFilter.Type.COLUMN, columnName), columnName);
        } else {
            this.name = columnName;
        }
        this.name = StringUtils.uncapitalize(EntityMeta.propertyNameToFieldName(this.name.toLowerCase()));
    }

    public String getName() {
        return name;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
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

    public boolean isReadonly() {
        return readonly;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public String toString() {
        return String.format("ColumnInfo{name='%s', columnName='%s', columnType='%s', autoIncrement=%s, primaryKey=%s, signed=%s, precision=%d, scale=%d, nullable=%s, readonly=%s, defaultValue='%s', remarks='%s'}", name, columnName, columnType, autoIncrement, primaryKey, signed, precision, scale, nullable, readonly, defaultValue, remarks);
    }
}
