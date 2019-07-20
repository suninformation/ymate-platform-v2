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
package net.ymate.platform.core.persistence.impl;

import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IFunction;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 17/6/22 上午10:50
 */
public class DefaultFunction implements IFunction {

    private Fields params;

    private boolean flag;

    public DefaultFunction() {
        params = Fields.create();
    }

    public DefaultFunction(String funcName) {
        if (StringUtils.isBlank(funcName)) {
            throw new NullArgumentException("funcName");
        }
        params = Fields.create(funcName, "(");
        flag = true;
    }

    private IFunction operate(String opt, String param) {
        return space().param(opt).space().param(param);
    }

    @Override
    public IFunction addition(Number param) {
        return operate("+", param.toString());
    }

    @Override
    public IFunction addition(String param) {
        return operate("+", param);
    }

    @Override
    public IFunction addition(IFunction param) {
        return operate("+", param.build());
    }

    @Override
    public IFunction subtract(Number param) {
        return operate("-", param.toString());
    }

    @Override
    public IFunction subtract(String param) {
        return operate("-", param);
    }

    @Override
    public IFunction subtract(IFunction param) {
        return operate("-", param.build());
    }

    @Override
    public IFunction multiply(Number param) {
        return operate("*", param.toString());
    }

    @Override
    public IFunction multiply(String param) {
        return operate("*", param);
    }

    @Override
    public IFunction multiply(IFunction param) {
        return operate("*", param.build());
    }

    @Override
    public IFunction divide(Number param) {
        return operate("/", param.toString());
    }

    @Override
    public IFunction divide(String param) {
        return operate("/", param);
    }

    @Override
    public IFunction divide(IFunction param) {
        return operate("/", param.build());
    }

    @Override
    public IFunction param(Number param) {
        params.add(param.toString());
        return this;
    }

    @Override
    public IFunction param(Number[] params) {
        if (params != null && params.length > 0) {
            boolean has = false;
            for (Number param : params) {
                if (param != null) {
                    if (has) {
                        separator();
                    }
                    this.params.add(param.toString());
                    has = true;
                }
            }
        }
        return this;
    }

    @Override
    public IFunction separator() {
        params.add(", ");
        return this;
    }

    @Override
    public IFunction space() {
        params.add(StringUtils.SPACE);
        return this;
    }

    @Override
    public IFunction bracketBegin() {
        params.add("(");
        return this;
    }

    @Override
    public IFunction bracketEnd() {
        params.add(")");
        return this;
    }

    @Override
    public IFunction param(IFunction param) {
        params.add(param.build());
        return this;
    }

    @Override
    public IFunction paramWS(Object... params) {
        if (params != null && params.length > 0) {
            boolean has = false;
            for (Object param : params) {
                if (param != null) {
                    if (has) {
                        separator();
                    }
                    if (param instanceof IFunction) {
                        this.params.add(((IFunction) param).build());
                    } else {
                        this.params.add(param.toString());
                    }
                    has = true;
                }
            }
        }
        return this;
    }

    @Override
    public IFunction param(String param) {
        params.add(param);
        return this;
    }

    @Override
    public IFunction param(String[] params) {
        if (params != null && params.length > 0) {
            boolean has = false;
            for (String param : params) {
                if (param != null) {
                    if (has) {
                        separator();
                    }
                    this.params.add(param);
                    has = true;
                }
            }
        }
        return this;
    }

    @Override
    public IFunction param(String prefix, String field) {
        params.add(prefix, field);
        return this;
    }

    public Fields params() {
        return params;
    }

    @Override
    public String build() {
        if (flag) {
            params.add(")");
        }
        return StringUtils.join(params.toArray(), StringUtils.EMPTY);
    }
}
