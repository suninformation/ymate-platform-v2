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

import java.util.List;

/**
 * 索引属性描述对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/20 下午18:25
 */
public class IndexMeta {

    public static IndexMeta create() {
        return new IndexMeta();
    }

    /**
     * 索引名称
     */
    private String name;
    /**
     * 是否唯一索引
     */
    private boolean unique;
    /**
     * 索引字段名称集合
     */
    private List<String> fields;

    public IndexMeta() {
    }

    public IndexMeta(String name, boolean unique, List<String> fields) {
        this.name = name;
        this.unique = unique;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public IndexMeta name(String name) {
        this.name = name;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public IndexMeta unique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public List<String> getFields() {
        return fields;
    }

    public IndexMeta fields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    @Override
    public String toString() {
        return String.format("IndexMeta [name='%s', unique=%s, fields=%s]", name, unique, fields);
    }

}
