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
package net.ymate.platform.plugin;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.*;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEventListener;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.plugin.impl.DefaultPluginFactory;
import net.ymate.platform.plugin.impl.PluginBeanLoadInitializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 插件框架模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-30 下午6:28:20
 */
public class Plugins implements IModule, IPlugins {

    private static final Log LOG = LogFactory.getLog(Plugins.class);

    private static final PluginBeanLoadInitializer PLUGIN_BEAN_LOAD_INITIALIZER = new PluginBeanLoadInitializer();

    static {
        try {
            for (Class<IPluginBeanLoadInitializer> initializerClass : ClassUtils.getExtensionLoader(IPluginBeanLoadInitializer.class, true).getExtensionClasses()) {
                PLUGIN_BEAN_LOAD_INITIALIZER.addInitializer(initializerClass.newInstance());
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    private IApplication owner;

    private IPluginFactory pluginFactory;

    private boolean initialized;

    private static volatile IPlugins instance;

    /**
     * @return 返回默认插件框架管理器实例对象
     */
    public static IPlugins get() {
        IPlugins inst = instance;
        if (inst == null) {
            synchronized (Plugins.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(Plugins.class);
                }
            }
        }
        return inst;
    }

    public Plugins() {
    }

    public Plugins(IPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    @Override
    public String getName() {
        return IPlugins.MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            if (pluginFactory == null) {
                IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
                if (configureFactory != null) {
                    IApplicationConfigurer configurer = configureFactory.getConfigurer();
                    IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
                    if (moduleConfigurer != null) {
                        pluginFactory = DefaultPluginFactory.create(configureFactory.getMainClass(), moduleConfigurer);
                    } else {
                        pluginFactory = DefaultPluginFactory.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
                    }
                }
            }
            //
            boolean enabled = pluginFactory != null && pluginFactory.getPluginConfig().isEnabled();
            YMP.showModuleVersion("ymate-platform-plugin", (enabled ? "- enabled" : "- disabled"), this);
            //
            if (enabled) {
                this.owner = owner;
                this.owner.getEvents().registerEvent(PluginEvent.class);
                this.owner.getEvents().registerListener(Events.MODE.NORMAL, ApplicationEvent.class, (IEventListener<ApplicationEvent>) context -> {
                    if (ApplicationEvent.EVENT.APPLICATION_INITIALIZED.equals(context.getEventName())) {
                        try {
                            pluginFactory.startup();
                        } catch (Exception e) {
                            LOG.warn("A exception occurred while startup plugins: ", RuntimeUtils.unwrapThrow(e));
                        }
                    }
                    return false;
                });
                //
                PLUGIN_BEAN_LOAD_INITIALIZER.beforeBeanLoad(this, pluginFactory.getBeanLoader());
                //
                if (pluginFactory.isIncludedClassPath()) {
                    pluginFactory.getBeanLoader().registerPackageName(IApplication.YMP_BASE_PACKAGE_NAME);
                }
                pluginFactory.initialize(owner);
                //
                initialized = true;
            }
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            if (pluginFactory != null) {
                pluginFactory.close();
                pluginFactory = null;
            }
            //
            owner = null;
        }
    }

    @Override
    public IPluginConfig getConfig() {
        if (pluginFactory == null) {
            return null;
        }
        return pluginFactory.getPluginConfig();
    }

    @Override
    public IPluginFactory getPluginFactory() {
        return pluginFactory;
    }

    @Override
    public IPlugin getPlugin(String id) {
        if (pluginFactory == null) {
            return null;
        }
        return pluginFactory.getPlugin(id);
    }

    @Override
    public <T> T getPlugin(Class<T> clazz) {
        if (pluginFactory == null) {
            return null;
        }
        return pluginFactory.getPlugin(clazz);
    }
}
