/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.persistence.jdbc.repo.support;

import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import net.ymate.platform.persistence.jdbc.repo.handle.PluginRepositoryHandler;
import net.ymate.platform.plugin.IPluginBeanLoadInitializer;
import net.ymate.platform.plugin.IPlugins;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/10 10:13
 * @since 2.1.0
 */
public class RepositoryPluginBeanLoadInitializer implements IPluginBeanLoadInitializer {

    @Override
    public void beforeBeanLoad(IPlugins plugins, IBeanLoader beanLoader) {
        beanLoader.registerHandler(Repository.class, new PluginRepositoryHandler(plugins.getOwner()));
    }
}
