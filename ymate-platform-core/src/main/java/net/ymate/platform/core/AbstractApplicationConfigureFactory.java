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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-11-11 12:46
 * @since 2.1.0
 */
public abstract class AbstractApplicationConfigureFactory implements IApplicationConfigureFactory {

    private static final Log LOG = LogFactory.getLog(AbstractApplicationConfigureFactory.class);

    private Class<?> mainClass;

    private String[] args;

    @Override
    public Class<?> getMainClass() {
        return mainClass;
    }

    @Override
    public void setMainClass(Class<?> mainClass) {
        if (LOG.isInfoEnabled() && this.mainClass == null && mainClass != null) {
            LOG.info(String.format("Set the main startup class: %s", mainClass.getName()));
        }
        this.mainClass = mainClass;
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }
}
