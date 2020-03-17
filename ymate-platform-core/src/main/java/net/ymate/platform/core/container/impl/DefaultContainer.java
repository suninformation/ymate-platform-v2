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
package net.ymate.platform.core.container.impl;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.container.IContainer;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/03/16 01:16
 */
public final class DefaultContainer implements IContainer {

    private IApplication application;

    @Override
    public void start(String... args) {
        if (application == null) {
            try {
                application = YMP.run(args);
            } catch (Exception e) {
                throw RuntimeUtils.wrapRuntimeThrow(e);
            }
        }
    }

    @Override
    public void stop() {
        if (application != null) {
            try {
                application.close();
            } catch (Exception e) {
                throw RuntimeUtils.wrapRuntimeThrow(e);
            }
        }
    }
}
