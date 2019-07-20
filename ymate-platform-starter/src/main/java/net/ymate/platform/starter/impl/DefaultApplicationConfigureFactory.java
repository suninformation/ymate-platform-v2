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

import net.ymate.platform.core.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-12 04:20
 * @since 2.1.0
 */
public class DefaultApplicationConfigureFactory implements IApplicationConfigureFactory {

    private final IApplicationConfigurer configurer;

    public DefaultApplicationConfigureFactory() {
        IApplicationConfigureParseFactory configureParseFactory = YMP.getConfigureParseFactory();
        IApplicationConfigureParser configureParser;
        if (configureParseFactory != null && (configureParser = configureParseFactory.getConfigureParser()) != null) {
            configurer = new DefaultApplicationConfigurer(configureParser);
        } else {
            configurer = new DefaultApplicationConfigurer();
        }
    }

    @Override
    public IApplicationConfigurer getConfigurer() {
        return configurer;
    }
}
