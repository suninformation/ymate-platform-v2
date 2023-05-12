/*
 * Copyright 2007-2021 the original author or authors.
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/5/16 1:31 上午
 * @since 2.1.0
 */
public class ConstAttr extends Attr {

    private final String attrName;

    public ConstAttr(String varType, String varName, String attrName) {
        super(varType, varName);
        setConstVarName(varName);
        this.attrName = attrName;
    }

    public ConstAttr(String varType, String varName, String columnName, String attrName) {
        super(varType, varName, columnName);
        setConstVarName(varName);
        this.attrName = attrName;
    }

    public ConstAttr(String varType, String varName, String columnName, String attrName, boolean autoIncrement, boolean signed, int precision, int scale, boolean nullable, String defaultValue, String remarks) {
        super(varType, varName, columnName, autoIncrement, signed, precision, scale, nullable, defaultValue, remarks);
        setConstVarName(varName);
        this.attrName = attrName;
    }

    public String getAttrName() {
        return attrName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConstAttr constAttr = (ConstAttr) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(attrName, constAttr.attrName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(attrName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("ConstAttr{attrName='%s', varName='%s'}", attrName, super.toString());
    }
}
