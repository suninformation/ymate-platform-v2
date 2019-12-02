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
package net.ymate.platform.persistence.jdbc.query;

import org.apache.commons.lang3.StringUtils;

/**
 * @param <T> 目标对象类型
 * @author 刘镇 (suninformation@163.com) on 2019-11-21 10:48
 * @since 2.1.0
 */
public class QueryHandleAdapter<T> {

    private IQueryHandler<T> queryHandler;

    public IQueryHandler<T> queryHandler() {
        return queryHandler;
    }

    @SuppressWarnings("unchecked")
    public T queryHandler(IQueryHandler<T> queryHandler) {
        this.queryHandler = queryHandler;
        return (T) this;
    }

    public String getExpressionStr(String defaultExpressionStr) {
        if (queryHandler != null) {
            return StringUtils.defaultIfBlank(queryHandler.getExpressionStr(), defaultExpressionStr);
        }
        return defaultExpressionStr;
    }
}
