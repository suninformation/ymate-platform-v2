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
package net.ymate.platform.persistence.jdbc;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据源适配器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 上午12:28:41
 */
@Ignored
public interface IDatabaseDataSourceAdapter extends IDataSourceAdapter<IDatabase, IDatabaseDataSourceConfig, Connection> {

    /**
     * 获取数据库方言
     *
     * @return 返回数据库方言
     */
    IDialect getDialect();

    /**
     * 获取数据源
     *
     * @return 返回数据源接口实例对象
     */
    DataSource getDataSource();
}
