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
import net.ymate.platform.persistence.jdbc.annotation.Dialect;
import net.ymate.platform.persistence.jdbc.dialect.AbstractDialect;
import org.apache.commons.lang3.StringUtils;

/**
 * SQLServer2005及以上数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-4-19 下午3:38:40
 */
@Dialect(value = Type.DATABASE.SQLSERVER, driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver")
public class SQLServerDialect extends AbstractDialect {

    private static final String SELECT = "SELECT";

    private static final String DISTINCT = "DISTINCT";

    public SQLServerDialect() {
        super("[", "]");
    }

    @Override
    public String getName() {
        return Type.DATABASE.SQLSERVER;
    }

    @Override
    public String buildPagedQuerySql(String originSql, int page, int pageSize) {
        int limit = (page - 1) * pageSize;
        String tmpSqlStr = StringUtils.trim(originSql);
        if (StringUtils.startsWithIgnoreCase(tmpSqlStr, SELECT)) {
            tmpSqlStr = StringUtils.trim(StringUtils.substring(tmpSqlStr, SELECT.length()));
        }
        boolean distinct = false;
        if (StringUtils.startsWithIgnoreCase(tmpSqlStr, DISTINCT)) {
            tmpSqlStr = StringUtils.substring(tmpSqlStr, DISTINCT.length());
            distinct = true;
        }
        return ExpressionUtils.bind("SELECT * FROM (SELECT ROW_NUMBER() OVER(ORDER BY __tc__) __rn__, * FROM (SELECT ${_distinct} TOP ${_limit} 0 __tc__, ${_sql}) t) tt WHERE __rn__ > ${_offset}")
                .set("_distinct", distinct ? DISTINCT : StringUtils.EMPTY)
                .set("_limit", String.valueOf(limit + pageSize))
                .set("_sql", tmpSqlStr)
                .set("_offset", String.valueOf(limit)).getResult();
    }
}
