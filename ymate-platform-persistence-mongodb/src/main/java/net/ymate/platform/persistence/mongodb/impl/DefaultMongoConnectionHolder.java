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
package net.ymate.platform.persistence.mongodb.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.ymate.platform.persistence.mongodb.IMongoConnectionHolder;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceAdapter;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceConfig;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/21 下午1:10
 */
public class DefaultMongoConnectionHolder implements IMongoConnectionHolder {

    private IMongoDataSourceAdapter dataSourceAdapter;

    private MongoClient mongoClient;

    public DefaultMongoConnectionHolder(IMongoDataSourceAdapter dataSourceAdapter) throws Exception {
        this.dataSourceAdapter = dataSourceAdapter;
        this.mongoClient = dataSourceAdapter.getConnection();
    }

    @Override
    public IMongoDataSourceConfig getDataSourceConfig() {
        return dataSourceAdapter.getDataSourceConfig();
    }

    @Override
    public MongoDatabase getConnection() {
        return mongoClient.getDatabase(dataSourceAdapter.getDataSourceConfig().getDatabaseName());
    }

    @Override
    public MongoDatabase getConnection(String databaseName) throws Exception {
        return mongoClient.getDatabase(databaseName);
    }

    @Override
    public IMongoDataSourceAdapter getDataSourceAdapter() {
        return dataSourceAdapter;
    }

    @Override
    public void close() throws Exception {
        dataSourceAdapter = null;
        mongoClient = null;
    }
}
