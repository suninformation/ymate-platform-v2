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
package net.ymate.platform.core.beans.support;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * @author 刘镇 (suninformation@163.com) on 16/7/3 上午2:05
 */
public class PropertyStateMeta {

    private final String propertyName;

    private final String aliasName;

    private final Object originalValue;

    private Object newValue;

    private boolean changed;

    public PropertyStateMeta(String propertyName, String aliasName, Object originalValue) {
        this.propertyName = propertyName;
        this.aliasName = aliasName;
        this.originalValue = originalValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public Object getOriginalValue() {
        return originalValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
        this.changed = !Objects.equals(this.originalValue, newValue);
    }

    public boolean isChanged() {
        return changed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PropertyStateMeta that = (PropertyStateMeta) o;
        return new EqualsBuilder()
                .append(changed, that.changed)
                .append(propertyName, that.propertyName)
                .append(aliasName, that.aliasName)
                .append(originalValue, that.originalValue)
                .append(newValue, that.newValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(propertyName)
                .append(aliasName)
                .append(originalValue)
                .append(newValue)
                .append(changed)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("propertyName", propertyName)
                .append("aliasName", aliasName)
                .append("originalValue", originalValue)
                .append("newValue", newValue)
                .append("changed", changed)
                .toString();
    }
}
