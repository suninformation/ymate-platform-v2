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
 * @author 刘镇 (suninformation@163.com) on 15/5/7 下午6:15
 */
public final class Params implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数集合
     */
    private final List<Object> params;

    public static String wrapQuote(String param) {
        return wrapQuote(param, "'");
    }

    public static String wrapQuote(String param, String quote) {
        quote = StringUtils.trimToEmpty(quote);
        return quote + StringUtils.trimToEmpty(param) + quote;
    }

    public static Params create(Object... params) {
        return new Params(params);
    }

    public static Params create(Collection<?> params) {
        return new Params().add(params);
    }

    private Params(Object... params) {
        this.params = new ArrayList<>();
        if (params != null && params.length > 0) {
            Arrays.stream(params).forEach(this::add);
        }
    }

    public List<Object> params() {
        return Collections.unmodifiableList(this.params);
    }

    public Params add(Object param) {
        if (param instanceof Params) {
            add((Params) param);
        } else if (param instanceof Collection) {
            add((Collection<?>) param);
        } else if (param != null && param.getClass().isArray()) {
            add(Arrays.asList((Object[]) param));
        } else {
            this.params.add(param);
        }
        return this;
    }

    public Params add(Params params) {
        this.params.addAll(params.params());
        return this;
    }

    public Params add(Collection<?> params) {
        params.forEach(this::add);
        return this;
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    public Object[] toArray() {
        return params.toArray(new Object[0]);
    }
}
