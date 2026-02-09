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
package net.ymate.platform.mock.web;

import java.util.*;

public class HeaderValueHolder {

    private final List<Object> values = new LinkedList<>();

    /**
     * 设置头部值，替换所有现有值
     *
     * @param value 头部值
     */
    public void setValue(Object value) {
        this.values.clear();
        this.values.add(value);
    }

    /**
     * 添加头部值，保留现有值
     *
     * @param value 头部值
     */
    public void addValue(Object value) {
        this.values.add(value);
    }

    /**
     * 添加多个头部值
     *
     * @param values 头部值集合
     */
    public void addValues(Collection<?> values) {
        this.values.addAll(values);
    }

    /**
     * 添加数组类型的头部值
     *
     * @param values 头部值数组
     * @throws IllegalArgumentException 如果不是数组类型
     */
    public void addValueArray(Object values) {
        if (values instanceof Object[]) {
            Collections.addAll(this.values, (Object[]) values);
        } else if (!values.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + values);
        }
    }

    /**
     * 获取所有头部值
     *
     * @return 不可修改的头部值列表
     */
    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    /**
     * 获取所有头部值的字符串表示
     *
     * @return 不可修改的字符串值列表
     */
    public List<String> getStringValues() {
        List<String> stringList = new ArrayList<>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    /**
     * 获取第一个头部值
     *
     * @return 第一个头部值，如果不存在则返回null
     */
    public Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    /**
     * 获取第一个头部值的字符串表示
     *
     * @return 第一个头部值的字符串表示，如果不存在则返回null
     */
    public String getStringValue() {
        return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
    }

    /**
     * 返回头部值的字符串表示
     *
     * @return 头部值的字符串表示
     */
    @Override
    public String toString() {
        return this.values.toString();
    }

    /**
     * 根据名称获取头部值持有者
     *
     * @param headers 头部映射
     * @param name    头部名称
     * @return 头部值持有者，如果不存在则返回null
     */
    public static HeaderValueHolder getByName(Map<String, HeaderValueHolder> headers, String name) {
        Objects.requireNonNull(name, "Header name must not be null");
        for (String headerName : headers.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return headers.get(headerName);
            }
        }
        return null;
    }

    public static class Builder {
        private final HeaderValueHolder headerValueHolder;

        public static Builder create() {
            return new Builder();
        }

        private Builder() {
            this.headerValueHolder = new HeaderValueHolder();
        }

        public Builder setValue(Object value) {
            headerValueHolder.setValue(value);
            return this;
        }

        public Builder addValue(Object value) {
            headerValueHolder.addValue(value);
            return this;
        }

        public Builder addValues(Collection<?> values) {
            headerValueHolder.addValues(values);
            return this;
        }

        public Builder addValueArray(Object values) {
            headerValueHolder.addValueArray(values);
            return this;
        }

        public HeaderValueHolder build() {
            return headerValueHolder;
        }
    }
}
