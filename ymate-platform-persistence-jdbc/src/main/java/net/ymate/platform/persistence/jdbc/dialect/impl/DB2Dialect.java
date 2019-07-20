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
package net.ymate.platform.persistence.jdbc.dialect.impl;

import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.dialect.AbstractDialect;

/**
 * DB2数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/16 上午11:26
 */
public class DB2Dialect extends AbstractDialect {

    @Override
    public String getName() {
        return Type.DATABASE.DB2.name();
    }

    @Override
    public String buildPagedQuerySql(String originSql, int page, int pageSize) {
        int limit = ((page - 1) * pageSize);
        return ExpressionUtils.bind("SELECT * FROM (SELECT ROW_NUMBER() OVER() AS __rn__, __row__.* FROM (${_sql}) AS __row__) WHERE __rn__ BETWEEN ${_limit} AND ${_offset}")
                .set("_limit", String.valueOf(limit + pageSize))
                .set("_sql", originSql)
                .set("_offset", String.valueOf(limit)).getResult();
    }
}
