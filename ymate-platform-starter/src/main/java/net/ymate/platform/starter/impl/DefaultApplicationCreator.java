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
package net.ymate.platform.starter.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.configuration.support.ConfigurationApplicationInitializer;
import net.ymate.platform.core.*;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-06 19:10
 * @since 2.1.0
 */
public class DefaultApplicationCreator implements IApplicationCreator {

    private static final Log LOG = LogFactory.getLog(DefaultApplicationCreator.class);

    private static final List<IApplicationInitializer> INITIALIZERS = new ArrayList<>();

    private IApplication application;

    static {
        try {
            for (Class<IApplicationInitializer> initializerClass : ClassUtils.getExtensionLoader(IApplicationInitializer.class).getExtensionClasses()) {
                INITIALIZERS.add(initializerClass.newInstance());
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    public DefaultApplicationCreator() {
    }

    @Override
    public IApplication create(Class<?> mainClass, IApplicationInitializer... applicationInitializers) throws Exception {
        if (application == null) {
            ApplicationInitializer initializers = new ApplicationInitializer(new ConfigurationApplicationInitializer())
                    .addInitializer(INITIALIZERS)
                    .addInitializer(applicationInitializers);
            IApplicationConfigureFactory configureFactory = YMP.getConfigureFactory();
            if (configureFactory == null) {
                throw new NullArgumentException("IApplicationConfigureFactory interface implementation class");
            }
            configureFactory.initialize(mainClass);
            application = new Application(configureFactory, initializers);
        }
        return application;
    }
}
