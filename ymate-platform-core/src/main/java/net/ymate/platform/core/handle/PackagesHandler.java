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
package net.ymate.platform.core.handle;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.IBeanHandler;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/8/3 下午6:45
 */
public final class PackagesHandler implements IBeanHandler {

    private static final String PACKAGE_INFO = "package-info";

    private final IApplication owner;

    public PackagesHandler(IApplication owner) {
        this.owner = owner;
    }

    @Override
    public Object handle(Class<?> targetClass) {
        if (targetClass.isInterface() && PACKAGE_INFO.equalsIgnoreCase(targetClass.getSimpleName())) {
            owner.getInterceptSettings().registerInterceptPackage(targetClass);
        }
        return null;
    }
}
