/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.persistence.mongodb.support;

import com.mongodb.ServerAddress;
import net.ymate.platform.core.support.IPasswordProcessor;
import net.ymate.platform.persistence.mongodb.IMongoModuleCfg;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 13:44
 * @version 1.0
 * @since 2.0.6
 */
public class MongoDataSourceConfigurable {

    public static MongoDataSourceConfigurable create(String name) {
        return new MongoDataSourceConfigurable(name);
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    private String name;

    public MongoDataSourceConfigurable(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    private void __putItem(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return;
        }
        __configs.put("ds." + name + "." + key, value);
    }

    public MongoDataSourceConfigurable connectionUrl(String connectionUrl) {
        __putItem(IMongoModuleCfg.CONNECTION_URL, connectionUrl);
        return this;
    }

    public MongoDataSourceConfigurable username(String username) {
        __putItem(IMongoModuleCfg.USERNAME, username);
        return this;
    }

    public MongoDataSourceConfigurable password(String password) {
        __putItem(IMongoModuleCfg.PASSWORD, password);
        return this;
    }

    public MongoDataSourceConfigurable passwordEncrypted(boolean passwordEncrypted) {
        __putItem(IMongoModuleCfg.PASSWORD_ENCRYPTED, String.valueOf(passwordEncrypted));
        return this;
    }

    public MongoDataSourceConfigurable passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
        __putItem(IMongoModuleCfg.PASSWORD_CLASS, passwordClass.getName());
        return this;
    }

    public MongoDataSourceConfigurable collectionPrefix(String collectionPrefix) {
        __putItem(IMongoModuleCfg.COLLECTION_PREFIX, collectionPrefix);
        return this;
    }

    public MongoDataSourceConfigurable databaseName(String databaseName) {
        __putItem(IMongoModuleCfg.DATABASE_NAME, databaseName);
        return this;
    }

    public MongoDataSourceConfigurable servers(ServerAddress[] servers) {
        if (ArrayUtils.isNotEmpty(servers)) {
            List<String> _ips = new ArrayList<String>();
            for (ServerAddress _serverAddr : servers) {
                String _ip = _serverAddr.getHost();
                if (_serverAddr.getPort() > 0) {
                    _ip += ":" + _serverAddr.getPort();
                }
                _ips.add(_ip);
            }
            __putItem(IMongoModuleCfg.SERVERS, StringUtils.join(_ips, "|"));
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> toMap() {
        return __configs;
    }
}
