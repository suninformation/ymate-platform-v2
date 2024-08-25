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
package net.ymate.platform.core.persistence;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

/**
 * @param <OWNER> 所属持久化容器类型
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 16:20
 * @since 2.1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractDataSourceConfig<OWNER extends IPersistence> implements IDataSourceConfig<OWNER> {

    private final String name;

    private String username;

    private String password;

    private boolean passwordEncrypted;

    private Class<? extends IPasswordProcessor> passwordClass;

    private boolean autoConnection;

    private boolean initialized;

    public AbstractDataSourceConfig(String dataSourceName) {
        if (StringUtils.isBlank(dataSourceName)) {
            throw new NullArgumentException("dataSourceName");
        }
        this.name = dataSourceName;
    }

    @SuppressWarnings("unchecked")
    public AbstractDataSourceConfig(String dataSourceName, IConfigReader configReader) throws ClassNotFoundException {
        this(dataSourceName);
        //
        this.username = configReader.getString(IPersistenceConfig.USERNAME);
        this.password = configReader.getString(IPersistenceConfig.PASSWORD);
        this.autoConnection = configReader.getBoolean(IPersistenceConfig.AUTO_CONNECTION);
        //
        if (StringUtils.isNotBlank(this.password)) {
            this.passwordEncrypted = configReader.getBoolean(IPersistenceConfig.PASSWORD_ENCRYPTED);
            if (this.passwordEncrypted) {
                String passwordClassStr = configReader.getString(IPersistenceConfig.PASSWORD_CLASS);
                if (StringUtils.isNotBlank(passwordClassStr)) {
                    this.passwordClass = (Class<? extends IPasswordProcessor>) ClassUtils.loadClass(passwordClassStr, this.getClass());
                }
            }
        }
    }

    @Override
    public void initialize(OWNER owner) throws Exception {
        if (!initialized) {
            doInitialize(owner);
            //
            initialized = true;
        }
    }

    /**
     * 由子类实现具体初始化逻辑
     *
     * @param owner 所属容器参数对象
     * @throws Exception 可能产生的任何异常
     */
    protected abstract void doInitialize(OWNER owner) throws Exception;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (!initialized) {
            this.username = username;
        }
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (!initialized) {
            this.password = password;
        }
    }

    @Override
    public boolean isPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(boolean passwordEncrypted) {
        if (!initialized) {
            this.passwordEncrypted = passwordEncrypted;
        }
    }

    @Override
    public Class<? extends IPasswordProcessor> getPasswordClass() {
        return passwordClass;
    }

    public void setPasswordClass(Class<? extends IPasswordProcessor> passwordClass) {
        if (!initialized) {
            this.passwordClass = passwordClass;
        }
    }

    @Override
    public boolean isAutoConnection() {
        return autoConnection;
    }

    public void setAutoConnection(boolean autoConnection) {
        if (!isInitialized()) {
            this.autoConnection = autoConnection;
        }
    }
}
