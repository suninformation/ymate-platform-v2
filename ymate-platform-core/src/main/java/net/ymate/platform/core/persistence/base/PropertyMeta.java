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
package net.ymate.platform.core.persistence.base;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 字段属性描述对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/20 上午10:47
 */
public class PropertyMeta implements Serializable {

    public static PropertyMeta create(String name, Type.FIELD type) {
        return new PropertyMeta(name, type);
    }

    public static final String NULL = "@NULL";

    /**
     * 属性名称
     */
    private String name;

    /**
     * 成员变量Field对象
     */
    private Field field;

    /**
     * 是否为自动增长
     */
    private boolean autoincrement;

    /**
     * 序列名称
     */
    private String sequenceName;

    /**
     * 键值生成器名称
     *
     * @since 2.1.0
     */
    private String useKeyGenerator;

    /**
     * 允许为空
     */
    private boolean nullable;

    /**
     * 是否为无符号
     */
    private boolean unsigned;

    /**
     * 数据长度，默认0为不限制
     */
    private int length;

    /**
     * 小数位数，默认0为无小数
     */
    private int decimals;

    /**
     * 数据类型
     */
    private Type.FIELD type;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 属性注释
     */
    private String comment;

    /**
     * 是否为只读属性
     */
    private boolean readonly;

    /**
     * 转换目标类型
     */
    private Class<?> conversionType;

    public PropertyMeta(String name, Type.FIELD type) {
        this.name = name;
        this.type = type;
    }

    public PropertyMeta(String name, Field field) {
        this.name = name;
        this.field = field;
    }

    public PropertyMeta(String name, Field field, boolean autoincrement, String sequenceName, boolean nullable, boolean unsigned, int length, int decimals, Type.FIELD type) {
        this.name = name;
        this.field = field;
        this.autoincrement = autoincrement;
        this.sequenceName = sequenceName;
        this.nullable = nullable;
        this.unsigned = unsigned;
        this.length = length;
        this.decimals = decimals;
        this.type = type;
    }

    public PropertyMeta(String name, Field field, boolean autoincrement, String sequenceName, boolean nullable, boolean unsigned, int length, int decimals, Type.FIELD type, String defaultValue, String comment, boolean readonly) {
        this(name, field, autoincrement, sequenceName, nullable, unsigned, length, decimals, type);
        //
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.readonly = readonly;
    }

    public String getName() {
        return name;
    }

    public PropertyMeta name(String name) {
        this.name = name;
        return this;
    }

    public Field getField() {
        return field;
    }

    public PropertyMeta field(Field field) {
        this.field = field;
        return this;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public PropertyMeta autoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
        return this;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public PropertyMeta sequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
        return this;
    }

    public String getUseKeyGenerator() {
        return useKeyGenerator;
    }

    public PropertyMeta useKeyGenerator(String useKeyGenerator) {
        this.useKeyGenerator = useKeyGenerator;
        return this;
    }

    public boolean isNullable() {
        return nullable;
    }

    public PropertyMeta nullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    public PropertyMeta unsigned(boolean unsigned) {
        this.unsigned = unsigned;
        return this;
    }

    public int getLength() {
        return length;
    }

    public PropertyMeta length(int length) {
        this.length = length;
        return this;
    }

    public int getDecimals() {
        return decimals;
    }

    public PropertyMeta decimals(int decimals) {
        this.decimals = decimals;
        return this;
    }

    public Type.FIELD getType() {
        return type;
    }

    public PropertyMeta type(Type.FIELD type) {
        this.type = type;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public PropertyMeta defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public PropertyMeta comment(String comment) {
        this.comment = comment;
        return this;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public PropertyMeta readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public Class<?> getConversionType() {
        return conversionType;
    }

    public PropertyMeta conversionType(Class<?> conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    @Override
    public String toString() {
        return String.format("PropertyMeta [name='%s', field=%s, autoincrement=%s, sequenceName='%s', useKeyGenerator='%s', nullable=%s, unsigned=%s, length=%d, decimals=%d, type=%s, defaultValue='%s', comment='%s', readonly=%s, conversionType=%s]", name, field, autoincrement, sequenceName, useKeyGenerator, nullable, unsigned, length, decimals, type, defaultValue, comment, readonly, conversionType);
    }

}
