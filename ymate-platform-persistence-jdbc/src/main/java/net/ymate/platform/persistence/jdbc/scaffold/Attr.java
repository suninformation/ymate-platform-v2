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

import net.ymate.platform.commons.util.ClassUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/19 下午3:19
 */
public class Attr implements Serializable {

    public static Attr build(ColumnInfo columnInfo) {
        return new Attr(columnInfo.getColumnType(), columnInfo.getName(), columnInfo.getColumnName(), columnInfo.isAutoIncrement(), columnInfo.isSigned(), columnInfo.getPrecision(), columnInfo.getScale(), columnInfo.isNullable(), columnInfo.getDefaultValue(), columnInfo.getRemarks());
    }

    private String varType;

    private String varName;

    private String columnName;

    private boolean autoIncrement;

    private boolean signed;

    private int precision;

    private int scale;

    private boolean nullable;

    private String defaultValue;

    private String remarks;

    private boolean readonly;

    public Attr(String varType, String varName) {
        if (StringUtils.isBlank(varName)) {
            throw new NullArgumentException("varName");
        }
        this.varName = varName;
        this.varType = varType;
    }

    public Attr(String varType, String varName, String columnName) {
        this(varType, varName);
        this.columnName = columnName;
    }

    public Attr(String varType, String varName, String columnName, boolean autoIncrement, boolean signed, int precision, int scale, boolean nullable, String defaultValue, String remarks) {
        this(varType, varName, columnName);
        //
        this.autoIncrement = autoIncrement;
        this.signed = signed;
        try {
            if (!signed && !ClassUtils.isSubclassOf(Class.forName(varType), Number.class)) {
                this.signed = true;
            }
        } catch (Exception ignored) {
        }
        this.precision = precision;
        this.scale = scale;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
        this.remarks = remarks;
    }

    public String getVarType() {
        return varType;
    }

    public Attr setVarType(String varType) {
        this.varType = varType;
        return this;
    }

    public String getVarName() {
        return varName;
    }

    public Attr setVarName(String varName) {
        this.varName = varName;
        return this;
    }

    public String getColumnName() {
        return columnName;
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

    public boolean isReadonly() {
        return readonly;
    }

    public Attr setReadonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Attr attr = (Attr) o;
        return autoIncrement == attr.autoIncrement &&
                signed == attr.signed &&
                precision == attr.precision &&
                scale == attr.scale &&
                nullable == attr.nullable &&
                readonly == attr.readonly &&
                Objects.equals(varType, attr.varType) &&
                varName.equals(attr.varName) &&
                Objects.equals(columnName, attr.columnName) &&
                Objects.equals(defaultValue, attr.defaultValue) &&
                Objects.equals(remarks, attr.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varType, varName, columnName, autoIncrement, signed, precision, scale, nullable, defaultValue, remarks, readonly);
    }

    @Override
    public String toString() {
        return this.getVarName();
    }
}
