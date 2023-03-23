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

import com.mongodb.ServerAddress;
import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.persistence.AbstractDataSourceConfig;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoClientOptionsHandler;
import net.ymate.platform.persistence.mongodb.IMongoConfig;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 14:48
 */
public final class DefaultMongoDataSourceConfig extends AbstractDataSourceConfig<IMongo> implements IMongoDataSourceConfig {

    private String collectionPrefix;

    private String connectionUrl;

    private final List<ServerAddress> serverAddresses = new ArrayList<>();

    private String databaseName;

    private String authenticationDatabaseName;

    private Class<? extends IMongoClientOptionsHandler> clientOptionsHandlerClass;

    public static DefaultMongoDataSourceConfig create(String dataSourceName, IConfigReader configReader) throws ClassNotFoundException {
        return new DefaultMongoDataSourceConfig(dataSourceName, configReader);
    }

    public static Builder builder(String dataSourceName) {
        return new Builder(dataSourceName);
    }

    private DefaultMongoDataSourceConfig(String dataSourceName) {
        super(dataSourceName);
    }

    @SuppressWarnings("unchecked")
    private DefaultMongoDataSourceConfig(String dataSourceName, IConfigReader configReader) throws ClassNotFoundException {
        super(dataSourceName, configReader);
        //
        this.collectionPrefix = configReader.getString(IMongoConfig.COLLECTION_PREFIX);
        this.connectionUrl = configReader.getString(IMongoConfig.CONNECTION_URL);
        this.databaseName = configReader.getString(IMongoConfig.DATABASE_NAME);
        this.authenticationDatabaseName = configReader.getString(IMongoConfig.AUTHENTICATION_DATABASE_NAME);
        //
        if (StringUtils.isBlank(this.connectionUrl)) {
            String clientOptionsHandlerClassName = configReader.getString(IMongoConfig.DS_OPTIONS_HANDLER_CLASS);
            if (StringUtils.isNotBlank(clientOptionsHandlerClassName)) {
                this.clientOptionsHandlerClass = (Class<? extends IMongoClientOptionsHandler>) ClassUtils.loadClass(clientOptionsHandlerClassName, getClass());
            }
            Arrays.stream(configReader.getArray(IMongoConfig.SERVERS, true)).map(serverStr -> StringUtils.split(serverStr, ":"))
                    .forEachOrdered(server -> serverAddresses.add(server.length > 1 ? new ServerAddress(server[0], Integer.parseInt(server[1])) : new ServerAddress(server[0])));
        }
    }

    @Override
    protected void doInitialize(IMongo iMongo) throws Exception {
    }

    @Override
    public String getCollectionPrefix() {
        return collectionPrefix;
    }

    public void setCollectionPrefix(String collectionPrefix) {
        if (!isInitialized()) {
            this.collectionPrefix = collectionPrefix;
        }
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        if (!isInitialized()) {
            this.connectionUrl = connectionUrl;
        }
    }

    @Override
    public List<ServerAddress> getServerAddresses() {
        return Collections.unmodifiableList(serverAddresses);
    }

    public void addServerAddress(ServerAddress serverAddress) {
        serverAddresses.add(serverAddress);
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        if (!isInitialized()) {
            this.databaseName = databaseName;
        }
    }

    @Override
    public String getAuthenticationDatabaseName() {
        return authenticationDatabaseName;
    }

    public void setAuthenticationDatabaseName(String authenticationDatabaseName) {
        if (!isInitialized()) {
            this.authenticationDatabaseName = authenticationDatabaseName;
        }
    }

    @Override
    public Class<? extends IMongoClientOptionsHandler> getClientOptionsHandlerClass() {
        return clientOptionsHandlerClass;
    }

    public void setClientOptionsHandlerClass(Class<? extends IMongoClientOptionsHandler> clientOptionsHandlerClass) {
        if (!isInitialized()) {
            this.clientOptionsHandlerClass = clientOptionsHandlerClass;
        }
    }

    public static final class Builder {

        private final DefaultMongoDataSourceConfig config;

        private Builder(String dataSourceName) {
            config = new DefaultMongoDataSourceConfig(dataSourceName);
        }

        public Builder collectionPrefix(String collectionPrefix) {
            config.setCollectionPrefix(collectionPrefix);
            return this;
        }

        public Builder databaseName(String databaseName) {
            config.setDatabaseName(databaseName);
            return this;
        }

        public Builder authenticationDatabaseName(String authenticationDatabaseName) {
            config.setAuthenticationDatabaseName(authenticationDatabaseName);
            return this;
        }

        public Builder clientOptionsHandlerClass(Class<? extends IMongoClientOptionsHandler> clientOptionsHandlerClass) {
            config.setClientOptionsHandlerClass(clientOptionsHandlerClass);
            return this;
        }

        public Builder connectionUrl(String connectionUrl) {
            config.setConnectionUrl(connectionUrl);
            return this;
        }

        public Builder addServerAddresses(ServerAddress... serverAddresses) {
            if (serverAddresses != null && serverAddresses.length > 0) {
                Arrays.stream(serverAddresses).forEachOrdered(config::addServerAddress);
            }
            return this;
        }

        public Builder username(String username) {
            config.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            config.setPassword(password);
            return this;
        }

        public Builder passwordEncrypted(boolean passwordEncrypted) {
            config.setPasswordEncrypted(passwordEncrypted);
            return this;
        }

        public Builder passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
            config.setPasswordClass(passwordClass);
            return this;
        }

        public DefaultMongoDataSourceConfig build() {
            return config;
        }
    }
}
