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
package net.ymate.platform.persistence.jdbc.repo.handle;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceConfig;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Proxy;

/**
 * @author 刘镇 (suninformation@163.com) on 16/4/24 上午12:35
 */
public class RepositoryHandler implements IBeanHandler {

    private final IDatabase owner;

    public RepositoryHandler(IDatabase owner) {
        this.owner = owner;
    }

    private Object createBeanMeta(Class<?> targetClass) {
        if (targetClass.isInterface()) {
            // 为接口类创建代理实例
            ClassLoader classLoader = targetClass.getClassLoader();
            Class<?>[] interfaces = new Class[]{targetClass};
            Object newObj = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> args != null && args.length > 0 ? args[args.length - 1] : null);
            BeanMeta beanMeta = BeanMeta.create(targetClass, true);
            beanMeta.setBeanObject(newObj);
            return beanMeta;
        }
        return BeanMeta.create(targetClass, true);
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isNormalClass(targetClass)) {
            Repository repositoryAnn = targetClass.getAnnotation(Repository.class);
            if (Type.DATABASE.UNKNOWN.equals(repositoryAnn.dbType())) {
                return createBeanMeta(targetClass);
            } else {
                IDatabaseDataSourceConfig dataSourceConfig;
                if (StringUtils.isBlank(repositoryAnn.dsName())) {
                    dataSourceConfig = owner.getConfig().getDefaultDataSourceConfig();
                } else {
                    dataSourceConfig = owner.getConfig().getDataSourceConfig(repositoryAnn.dsName());
                }
                if (dataSourceConfig != null && dataSourceConfig.getType().equals(repositoryAnn.dbType())) {
                    return createBeanMeta(targetClass);
                }
            }
        }
        return null;
    }
}
