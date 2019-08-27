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
package net.ymate.platform.core;

import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.module.ModuleManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-27 10:34
 * @since 2.1.0
 */
public final class ApplicationInitializer implements IApplicationInitializer {

    private final List<IApplicationInitializer> initializers = new ArrayList<>();

    public ApplicationInitializer(IApplicationInitializer... applicationInitializers) {
        if (ArrayUtils.isNotEmpty(applicationInitializers)) {
            addInitializer(applicationInitializers);
        }
    }

    public ApplicationInitializer addInitializer(IApplicationInitializer... applicationInitializers) {
        Arrays.stream(applicationInitializers).filter(Objects::nonNull).forEachOrdered(initializers::add);
        return this;
    }

    public ApplicationInitializer addInitializer(Collection<IApplicationInitializer> applicationInitializers) {
        applicationInitializers.stream().filter(Objects::nonNull).forEachOrdered(initializers::add);
        return this;
    }

    @Override
    public void afterEventInit(IApplication application, Events events) {
        initializers.forEach(initializer -> initializer.afterEventInit(application, events));
    }

    @Override
    public void beforeBeanLoad(IApplication application, IBeanLoader beanLoader) {
        initializers.forEach(initializer -> initializer.beforeBeanLoad(application, beanLoader));
    }

    @Override
    public void beforeModuleManagerInit(IApplication application, ModuleManager moduleManager) {
        initializers.forEach(initializer -> initializer.beforeModuleManagerInit(application, moduleManager));
    }

    @Override
    public void beforeBeanFactoryInit(IApplication application, IBeanFactory beanFactory) {
        initializers.forEach(initializer -> initializer.beforeBeanFactoryInit(application, beanFactory));
    }
}
