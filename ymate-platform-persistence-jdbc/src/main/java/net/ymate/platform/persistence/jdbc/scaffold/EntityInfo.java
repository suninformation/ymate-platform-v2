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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-12-25 19:31
 */
public class EntityInfo implements Serializable {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 实体类名称
     */
    private String name;

    /**
     * 数据表名称
     */
    private String tableName;

    /**
     * 主键类型
     */
    private String primaryKeyType;

    /**
     * 主键名称
     */
    private String primaryKeyName;

    /**
     * 主键属性集合
     */
    private final Set<Attr> primaryKeys = new LinkedHashSet<>();

    /**
     * 用于完整的构造方法
     */
    private final Set<Attr> fields = new LinkedHashSet<>();

    /**
     * 用于非空字段的构造方法
     */
    private final Set<Attr> nonNullableFields = new LinkedHashSet<>();

    /**
     * 用于生成字段名称常量
     */
    private final Set<ConstAttr> constFields = new LinkedHashSet<>();

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPrimaryKeyType() {
        return primaryKeyType;
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public Set<Attr> getPrimaryKeys() {
        return Collections.unmodifiableSet(primaryKeys);
    }

    public Set<Attr> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    public Set<Attr> getNonNullableFields() {
        return Collections.unmodifiableSet(nonNullableFields);
    }

    public Set<Attr> getConstFields() {
        return Collections.unmodifiableSet(constFields);
    }

    @Override
    public String toString() {
        return String.format("EntityInfo{name='%s', tableName='%s', primaryKeyType='%s', primaryKeyName='%s', primaryKeys=%s, fields=%s, nonNullableFields=%s, constFields=%s}", name, tableName, primaryKeyType, primaryKeyName, primaryKeys, fields, nonNullableFields, constFields);
    }

    public static class Builder {

        private final EntityInfo target = new EntityInfo();

        public Builder name(String name) {
            target.name = name;
            return this;
        }

        public Builder tableName(String tableName) {
            target.tableName = tableName;
            return this;
        }

        public Builder primaryKeyType(String primaryKeyType) {
            target.primaryKeyType = primaryKeyType;
            return this;
        }

        public Builder primaryKeyName(String primaryKeyName) {
            target.primaryKeyName = primaryKeyName;
            return this;
        }

        public Builder addPrimaryKey(Attr primaryKey) {
            target.primaryKeys.add(primaryKey);
            return this;
        }

        public Builder addField(Attr field) {
            target.fields.add(field);
            return this;
        }

        public Builder addNonNullableField(Attr nonNullableField) {
            target.nonNullableFields.add(nonNullableField);
            return this;
        }

        public Builder addConstField(ConstAttr constField) {
            target.constFields.add(constField);
            return this;
        }

        public EntityInfo build() {
            return target;
        }
    }
}
