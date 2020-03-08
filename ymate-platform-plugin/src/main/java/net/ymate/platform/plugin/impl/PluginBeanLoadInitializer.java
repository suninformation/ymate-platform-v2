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
package net.ymate.platform.plugin.impl;

import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.plugin.IPluginBeanLoadInitializer;
import net.ymate.platform.plugin.IPlugins;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/10 09:55
 * @since 2.1.0
 */
public final class PluginBeanLoadInitializer implements IPluginBeanLoadInitializer {

    private final List<IPluginBeanLoadInitializer> initializers = new ArrayList<>();

    public PluginBeanLoadInitializer(IPluginBeanLoadInitializer... initializers) {
        addInitializer(initializers);
    }

    public List<IPluginBeanLoadInitializer> getInitializers() {
        return Collections.unmodifiableList(initializers);
    }

    public PluginBeanLoadInitializer addInitializer(IPluginBeanLoadInitializer... initializers) {
        Arrays.stream(initializers).filter(Objects::nonNull).forEachOrdered(initializer -> {
            if (initializer instanceof PluginBeanLoadInitializer) {
                this.initializers.addAll(((PluginBeanLoadInitializer) initializer).getInitializers());
            } else {
                this.initializers.add(initializer);
            }
        });
        return this;
    }

    public PluginBeanLoadInitializer addInitializer(Collection<IPluginBeanLoadInitializer> initializers) {
        initializers.stream().filter(Objects::nonNull).forEachOrdered(initializer -> {
            if (initializer instanceof PluginBeanLoadInitializer) {
                this.initializers.addAll(((PluginBeanLoadInitializer) initializer).getInitializers());
            } else {
                this.initializers.add(initializer);
            }
        });
        return this;
    }

    @Override
    public void beforeBeanLoad(IPlugins plugins, IBeanLoader beanLoader) {
        initializers.forEach(initializer -> initializer.beforeBeanLoad(plugins, beanLoader));
    }
}
