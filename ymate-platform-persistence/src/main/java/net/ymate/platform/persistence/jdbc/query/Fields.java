/*
 * Copyright 2007-2107 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 字段过滤对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 下午4:06
 * @version 1.0
 */
public class Fields {

    /**
     * 字段名称集合
     */
    private List<String> __fields;

    /**
     * 是否为排除字段集合
     */
    private boolean __excluded;

    public static Fields create() {
        return new Fields();
    }

    private Fields() {
        this.__fields = new ArrayList<String>();
    }

    public Fields add(String field) {
        this.__fields.add(field);
        return this;
    }

    public Fields add(Fields fields) {
        this.__fields.addAll(fields.getFields());
        return this;
    }

    public Fields addAll(Collection<String> fields) {
        this.__fields.addAll(fields);
        return this;
    }

    public Fields excluded(boolean excluded) {
        this.__excluded = excluded;
        return this;
    }

    public boolean isExcluded() {
        return __excluded;
    }

    public List<String> getFields() {
        return this.__fields;
    }

    public String[] toArray() {
        return __fields.toArray(new String[__fields.size()]);
    }
}
