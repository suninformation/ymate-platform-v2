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
package net.ymate.platform.configuration.support;

import net.ymate.platform.configuration.Cfgs;
import net.ymate.platform.configuration.annotation.ConfigValue;
import net.ymate.platform.configuration.annotation.Configuration;
import net.ymate.platform.configuration.handle.ConfigHandler;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationInitializer;
import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanLoader;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-27 10:29
 * @since 2.1.0
 */
public class ConfigurationApplicationInitializer implements IApplicationInitializer {

    @Override
    public void beforeBeanLoad(IApplication application, IBeanLoader beanLoader) {
        beanLoader.registerHandler(Configuration.class, new ConfigHandler(Cfgs.get()));
    }

    @Override
    public void beforeBeanFactoryInit(IApplication application, IBeanFactory beanFactory) {
        beanFactory.registerInjector(ConfigValue.class, new ConfigValueInjector());
    }
}
