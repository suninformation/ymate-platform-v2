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

    private Fields params;

    private boolean flag;

    public AbstractFunction() {
        params = Fields.create();
        onBuild();
    }

    public AbstractFunction(String funcName) {
        if (StringUtils.isBlank(funcName)) {
            throw new NullArgumentException("funcName");
        }
        params = Fields.create(funcName, "(");
        flag = true;
        //
        onBuild();
    }

    /**
     * 处理函数构建过程
     */
    public abstract void onBuild();

    public AbstractFunction param(Number param) {
        params.add(param.toString());
        return this;
    }

    public AbstractFunction param(Number[] params) {
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

    public AbstractFunction operate(String opt, String param) {
        return space().param(opt).space().param(param);
    }

    public AbstractFunction separator() {
        params.add(", ");
        return this;
    }

    public AbstractFunction space() {
        params.add(StringUtils.SPACE);
        return this;
    }

    public AbstractFunction bracketBegin() {
        params.add("(");
        return this;
    }

    public AbstractFunction bracketEnd() {
        params.add(")");
        return this;
    }

    public AbstractFunction param(IFunction function) {
        params.add(function.build());
        return this;
    }

    public AbstractFunction paramWS(Object... params) {
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

    public AbstractFunction param(String param) {
        params.add(param);
        return this;
    }

    public AbstractFunction param(String[] params) {
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

    public AbstractFunction param(String prefix, String field) {
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

    @Override
    public String toString() {
        return build();
    }
}
