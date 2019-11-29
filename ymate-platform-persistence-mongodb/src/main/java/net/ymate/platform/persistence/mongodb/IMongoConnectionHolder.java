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
package net.ymate.platform.persistence.mongodb;

import com.mongodb.client.MongoDatabase;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IConnectionHolder;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/21 上午11:45
 */
@Ignored
public interface IMongoConnectionHolder extends IConnectionHolder<IMongo, MongoDatabase, IMongoDataSourceConfig> {

    /**
     * 获取连接对象
     *
     * @param databaseName 数据库名称
     * @return 返回连接对象
     * @throws Exception 可能产生的异常
     */
    MongoDatabase getConnection(String databaseName) throws Exception;

    /**
     * 获取数据源适配器
     *
     * @return 返回数据源适配器对象
     */
    IMongoDataSourceAdapter getDataSourceAdapter();
}
