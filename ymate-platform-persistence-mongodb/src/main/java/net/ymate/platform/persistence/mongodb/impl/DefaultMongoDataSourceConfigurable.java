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
import net.ymate.platform.core.persistence.AbstractDataSourceConfigurable;
import net.ymate.platform.persistence.mongodb.IMongoConfig;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceConfigurable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 04:18
 * @since 2.1.0
 */
public class DefaultMongoDataSourceConfigurable extends AbstractDataSourceConfigurable implements IMongoDataSourceConfigurable {

    public static Builder builder(String dataSourceName) {
        return new Builder(dataSourceName);
    }

    private DefaultMongoDataSourceConfigurable(String dataSourceName) {
        super(dataSourceName);
    }

    public static final class Builder {

        private final DefaultMongoDataSourceConfigurable configurable;

        private Builder(String dataSourceName) {
            configurable = new DefaultMongoDataSourceConfigurable(dataSourceName);
        }

        public Builder connectionUrl(String connectionUrl) {
            configurable.addConfig(IMongoConfig.CONNECTION_URL, connectionUrl);
            return this;
        }

        public Builder username(String username) {
            configurable.addConfig(IMongoConfig.USERNAME, username);
            return this;
        }

        public Builder password(String password) {
            configurable.addConfig(IMongoConfig.PASSWORD, password);
            return this;
        }

        public Builder passwordEncrypted(boolean passwordEncrypted) {
            configurable.addConfig(IMongoConfig.PASSWORD_ENCRYPTED, String.valueOf(passwordEncrypted));
            return this;
        }

        public Builder passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
            configurable.addConfig(IMongoConfig.PASSWORD_CLASS, passwordClass.getName());
            return this;
        }

        public Builder collectionPrefix(String collectionPrefix) {
            configurable.addConfig(IMongoConfig.COLLECTION_PREFIX, collectionPrefix);
            return this;
        }

        public Builder databaseName(String databaseName) {
            configurable.addConfig(IMongoConfig.DATABASE_NAME, databaseName);
            return this;
        }

        public Builder authenticationDatabaseName(String authenticationDatabaseName) {
            configurable.addConfig(IMongoConfig.AUTHENTICATION_DATABASE_NAME, authenticationDatabaseName);
            return this;
        }

        public Builder addServerAddresses(ServerAddress... serverAddresses) {
            if (ArrayUtils.isNotEmpty(serverAddresses)) {
                List<String> servers = new ArrayList<>();
                for (ServerAddress serverAddress : serverAddresses) {
                    String host = serverAddress.getHost();
                    if (serverAddress.getPort() > 0) {
                        host += ":" + serverAddress.getPort();
                    }
                    servers.add(host);
                }
                configurable.addConfig(IMongoConfig.SERVERS, StringUtils.join(servers, "|"));
            }
            return this;
        }

        public DefaultMongoDataSourceConfigurable build() {
            return configurable;
        }
    }
}
