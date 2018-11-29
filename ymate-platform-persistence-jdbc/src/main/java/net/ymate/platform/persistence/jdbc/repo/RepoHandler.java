/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.persistence.jdbc.DataSourceCfgMeta;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import org.apache.commons.lang.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 16/4/24 上午12:35
 * @version 1.0
 */
public class RepoHandler implements IBeanHandler {

    private final IDatabase __owner;

    public RepoHandler(IDatabase owner) throws Exception {
        __owner = owner;
        __owner.getOwner().registerExcludedClass(IRepository.class);
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        Repository _anno = targetClass.getAnnotation(Repository.class);
        if (JDBC.DATABASE.UNKNOWN.equals(_anno.dbType())) {
            return BeanMeta.create(targetClass, true);
        } else {
            DataSourceCfgMeta _cfgMeta;
            if (StringUtils.isBlank(_anno.dsName())) {
                _cfgMeta = __owner.getModuleCfg().getDefaultDataSourceCfg();
            } else {
                _cfgMeta = __owner.getModuleCfg().getDataSourceCfg(_anno.dsName());
            }
            if (_cfgMeta != null && _cfgMeta.getType().equals(_anno.dbType())) {
                return BeanMeta.create(targetClass, true);
            }
        }
        return null;
    }
}
