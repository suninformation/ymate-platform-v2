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
package net.ymate.platform.core.container;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2018/03/15 14:30
 */
public class ContainerManager {

    private static final Log LOG = LogFactory.getLog(ContainerManager.class);

    private static final Map<String, IContainer> CONTAINERS = new ConcurrentHashMap<>();

    static {
        try {
            ClassUtils.ExtensionLoader<IContainer> extensionLoader = ClassUtils.getExtensionLoader(IContainer.class, true);
            for (Class<IContainer> containerClass : extensionLoader.getExtensionClasses()) {
                registerContainer(containerClass);
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    public static void registerContainer(Class<? extends IContainer> targetClass) throws Exception {
        String key = targetClass.getName().toLowerCase();
        if (!CONTAINERS.containsKey(key)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Container class [%s] registered.", targetClass.getName()));
            }
            CONTAINERS.put(key, targetClass.newInstance());
        }
    }

    public static Collection<IContainer> getContainers() {
        return Collections.unmodifiableCollection(CONTAINERS.values());
    }
}
