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

import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-11-11 12:46
 * @since 2.1.0
 */
public abstract class AbstractApplicationConfigureFactory implements IApplicationConfigureFactory {

    private Class<?> mainClass;

    private boolean initialized;

    @Override
    public void initialize(Class<?> mainClass) throws Exception {
        if (!initialized) {
            if (mainClass == null) {
                String mainClassName = System.getProperty(IApplication.SYSTEM_MAIN_CLASS);
                if (StringUtils.isNotBlank(mainClassName)) {
                    mainClass = Class.forName(mainClassName);
                }
            }
            this.mainClass = mainClass;
            initialized = true;
        }
    }

    @Override
    public Class<?> getMainClass() {
        return mainClass;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
}
