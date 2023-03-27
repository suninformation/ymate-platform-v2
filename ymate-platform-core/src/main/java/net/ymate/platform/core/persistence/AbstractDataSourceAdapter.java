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
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.YMP;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @param <OWNER>             所属持久化容器类型
 * @param <DATASOURCE_CONFIG> 数据源配置类型
 * @param <CONNECTION>        连接类型
 * @author 刘镇 (suninformation@163.com) on 2019-08-01 10:51
 * @since 2.1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractDataSourceAdapter<OWNER extends IPersistence, DATASOURCE_CONFIG extends IDataSourceConfig, CONNECTION> implements IDataSourceAdapter<OWNER, DATASOURCE_CONFIG, CONNECTION> {

    private static final Log LOG = LogFactory.getLog(AbstractDataSourceAdapter.class);

    private OWNER owner;

    private DATASOURCE_CONFIG dataSourceConfig;

    private boolean initialized;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(OWNER owner, DATASOURCE_CONFIG dataSourceConfig) throws Exception {
        if (!initialized) {
            if (!dataSourceConfig.isInitialized()) {
                dataSourceConfig.initialize(owner);
            }
            //
            this.owner = owner;
            this.dataSourceConfig = dataSourceConfig;
            //
            doInitialize(owner, dataSourceConfig);
            //
            initialized = true;
        }
    }

    @Override
    public OWNER getOwner() {
        return owner;
    }

    /**
     * 由子类实现具体初始化逻辑
     *
     * @param owner            所属容器参数对象
     * @param dataSourceConfig 数据源配置对象
     * @throws Exception 可能产生的任何异常
     */
    protected abstract void doInitialize(OWNER owner, DATASOURCE_CONFIG dataSourceConfig) throws Exception;

    @Override
    public boolean initializeIfNeed() throws Exception {
        if (!initialized) {
            try {
                doInitialize(owner, dataSourceConfig);
                initialized = true;
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Data source '%s' initialization failed...", dataSourceConfig.getName()), RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return initialized;
    }

    @Override
    public DATASOURCE_CONFIG getDataSourceConfig() {
        return dataSourceConfig;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            doClose();
            //
            dataSourceConfig = null;
            owner = null;
        }
    }

    /**
     * 由子类实现具体关闭逻辑
     *
     * @throws Exception 可能产生的任何异常
     */
    protected abstract void doClose() throws Exception;

    protected boolean isInitialized() {
        return initialized;
    }

    protected String decryptPasswordIfNeed() throws Exception {
        return decryptPasswordIfNeed(dataSourceConfig.getPassword());
    }

    protected String decryptPasswordIfNeed(String password) throws Exception {
        if (StringUtils.isNotBlank(password) && dataSourceConfig.isPasswordEncrypted()) {
            if (dataSourceConfig.getPasswordClass() != null) {
                return ((IPasswordProcessor) dataSourceConfig.getPasswordClass().newInstance()).decrypt(password);
            }
            IPasswordProcessor passwordProcessor = owner.getOwner().getConfigureFactory().getConfigurer().getPasswordProcessor();
            if (passwordProcessor == null) {
                passwordProcessor = YMP.getPasswordProcessor();
            }
            return passwordProcessor.decrypt(password);
        }
        return password;
    }
}
