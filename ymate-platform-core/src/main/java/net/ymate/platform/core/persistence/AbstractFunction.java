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

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 17/6/22 上午10:50
 */
public abstract class AbstractFunction implements IFunction {

    private final Fields fields;

    private final Params params = Params.create();

    private boolean flag;

    public AbstractFunction() {
        fields = Fields.create();
        onBuild();
    }

    public AbstractFunction(String funcName) {
        if (StringUtils.isBlank(funcName)) {
            throw new NullArgumentException("funcName");
        }
        fields = Fields.create(funcName, "(");
        flag = true;
        //
        onBuild();
    }

    /**
     * 处理函数构建过程
     */
    public abstract void onBuild();

    public AbstractFunction field(Number param) {
        fields.add(param.toString());
        return this;
    }

    public AbstractFunction field(Number[] params) {
        if (params != null && params.length > 0) {
            boolean has = false;
            for (Number param : params) {
                if (param != null) {
                    if (has) {
                        separator();
                    }
                    this.fields.add(param.toString());
                    has = true;
                }
            }
        }
        return this;
    }

    public AbstractFunction operate(String opt, String param) {
        return space().field(opt).space().field(param);
    }

    public AbstractFunction separator() {
        fields.add(", ");
        return this;
    }

    public AbstractFunction space() {
        fields.add(StringUtils.SPACE);
        return this;
    }

    public AbstractFunction bracketBegin() {
        fields.add("(");
        return this;
    }

    public AbstractFunction bracketEnd() {
        fields.add(")");
        return this;
    }

    public AbstractFunction field(IFunction function) {
        fields.add(function);
        params.add(function.params());
        return this;
    }

    public AbstractFunction field(IFunction function, String alias) {
        fields.add(function, alias);
        params.add(function.params());
        return this;
    }

    public AbstractFunction fieldWS(Object... fields) {
        if (fields != null && fields.length > 0) {
            boolean has = false;
            for (Object field : fields) {
                if (field != null) {
                    if (has) {
                        separator();
                    }
                    if (field instanceof IFunction) {
                        this.fields.add(((IFunction) field));
                        this.params.add(((IFunction) field).params());
                    } else {
                        this.fields.add(field.toString());
                    }
                    has = true;
                }
            }
        }
        return this;
    }

    public AbstractFunction field(String param) {
        fields.add(param);
        return this;
    }

    public AbstractFunction field(String[] params) {
        if (params != null && params.length > 0) {
            boolean has = false;
            for (String param : params) {
                if (param != null) {
                    if (has) {
                        separator();
                    }
                    this.fields.add(param);
                    has = true;
                }
            }
        }
        return this;
    }

    public AbstractFunction field(String prefix, String field) {
        fields.add(prefix, field);
        return this;
    }

    public Fields fields() {
        return fields;
    }

    @Override
    public String build() {
        if (flag) {
            fields.add(")");
        }
        return StringUtils.join(fields.toArray(), StringUtils.EMPTY);
    }

    @Override
    public Params params() {
        return params;
    }

    @Override
    public IFunction param(Object param) {
        params.add(param);
        return this;
    }

    @Override
    public IFunction param(Params params) {
        this.params.add(params);
        return this;
    }

    @Override
    public String toString() {
        return build();
    }
}
