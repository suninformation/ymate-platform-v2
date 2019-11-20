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

import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.annotation.Dialect;
import net.ymate.platform.persistence.jdbc.dialect.AbstractDialect;

/**
 * MySQL数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-30 下午01:55:13
 */
@Dialect(value = Type.DATABASE.MYSQL, driverClass = "com.mysql.jdbc.Driver")
public class MySQLDialect extends AbstractDialect {

    public MySQLDialect() {
        super("`", "`");
    }

    @Override
    public String getName() {
        return Type.DATABASE.MYSQL;
    }
}
