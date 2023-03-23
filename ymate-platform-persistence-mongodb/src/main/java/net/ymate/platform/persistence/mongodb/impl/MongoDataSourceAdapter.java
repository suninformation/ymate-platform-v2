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
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.AbstractDataSourceAdapter;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoClientOptionsHandler;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceAdapter;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 上午12:00
 */
public class MongoDataSourceAdapter extends AbstractDataSourceAdapter<IMongo, IMongoDataSourceConfig, MongoClient> implements IMongoDataSourceAdapter {

    private MongoClient mongoClient;

    @Override
    protected void doInitialize(IMongo iMongo, IMongoDataSourceConfig dataSourceConfig) throws Exception {
        MongoClientOptions.Builder clientOptionsBuilder = null;
        if (dataSourceConfig.getClientOptionsHandlerClass() != null) {
            clientOptionsBuilder = ClassUtils.impl(dataSourceConfig.getClientOptionsHandlerClass(), IMongoClientOptionsHandler.class).handler(dataSourceConfig.getName());
        }
        if (clientOptionsBuilder == null) {
            clientOptionsBuilder = MongoClientOptions.builder();
        }
        if (StringUtils.isNotBlank(dataSourceConfig.getConnectionUrl())) {
            mongoClient = new MongoClient(new MongoClientURI(dataSourceConfig.getConnectionUrl(), clientOptionsBuilder));
        } else {
            String username = StringUtils.trimToNull(dataSourceConfig.getUsername());
            if (username != null) {
                String authDbName = StringUtils.defaultIfBlank(dataSourceConfig.getAuthenticationDatabaseName(), "admin");
                MongoCredential credential = MongoCredential.createCredential(username, authDbName, StringUtils.trimToEmpty(decryptPasswordIfNeed()).toCharArray());
                mongoClient = new MongoClient(dataSourceConfig.getServerAddresses(), credential, clientOptionsBuilder.build());
            } else {
                mongoClient = new MongoClient(dataSourceConfig.getServerAddresses(), clientOptionsBuilder.build());
            }
        }
    }

    @Override
    public boolean initializeIfNeed() throws Exception {
        return isInitialized();
    }

    @Override
    public MongoClient getConnection() throws Exception {
        return mongoClient;
    }

    @Override
    public void doClose() throws Exception {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
}
