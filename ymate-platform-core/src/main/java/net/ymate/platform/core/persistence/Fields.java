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
package net.ymate.platform.core.persistence;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 字段过滤对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 下午4:06
 */
public final class Fields implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段名称集合
     */
    private final List<String> fields;

    /**
     * 是否为排除字段集合
     */
    private boolean excluded;

    /**
     * @param prefix 字段名前缀
     * @param field  字段名
     * @return 组合后的字段名称
     */
    public static String field(String prefix, String field) {
        if (StringUtils.isNotBlank(prefix)) {
            return prefix.concat(".").concat(field);
        }
        return field;
    }

    public static Fields create(String... fields) {
        return new Fields(fields);
    }

    public static Fields create(Collection<String> fields) {
        return new Fields().add(fields);
    }

    private Fields(String... fields) {
        this.fields = new ArrayList<>();
        if (fields != null && fields.length > 0) {
            this.fields.addAll(Arrays.asList(fields));
        }
    }

    public Fields add(String prefix, String field) {
        this.fields.add(field(prefix, field));
        return this;
    }

    public Fields add(String prefix, String field, String alias) {
        field = field(prefix, field);
        if (StringUtils.isNotBlank(alias)) {
            field = field.concat(StringUtils.SPACE).concat(alias);
        }
        this.fields.add(field);
        return this;
    }

    public Fields add(String field) {
        this.fields.add(field);
        return this;
    }

    public Fields add(IFunction func) {
        this.fields.add(func.build());
        return this;
    }

    public Fields add(IFunction func, String alias) {
        String field = func.build();
        if (StringUtils.isNotBlank(alias)) {
            field = field.concat(StringUtils.SPACE).concat(alias);
        }
        this.fields.add(field);
        return this;
    }

    public Fields add(Fields fields) {
        this.fields.addAll(fields.fields());
        this.excluded = fields.isExcluded();
        return this;
    }

    public Fields add(Collection<String> fields) {
        this.fields.addAll(fields);
        return this;
    }

    public Fields excluded(boolean excluded) {
        this.excluded = excluded;
        return this;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public List<String> fields() {
        return Collections.unmodifiableList(this.fields);
    }

    public String[] toArray() {
        return fields.toArray(new String[0]);
    }
}
