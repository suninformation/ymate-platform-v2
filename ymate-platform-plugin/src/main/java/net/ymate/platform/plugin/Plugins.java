/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.ApplicationEvent;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEventListener;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.plugin.impl.DefaultPluginConfig;
import net.ymate.platform.plugin.impl.DefaultPluginFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Arrays;

/**
 * 插件框架模块管理器及插件工厂相关工具方法
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-30 下午6:28:20
 * @version 1.0
 */
@Module
public class Plugins implements IModule, IPlugins, IPluginEventListener {

    public static final Version VERSION = new Version(2, 0, 9, Plugins.class.getPackage().getImplementationVersion(), Version.VersionType.Release);

    private static final Log _LOG = LogFactory.getLog(Plugins.class);

    private YMP __owner;

    private boolean __inited;

    private static volatile IPlugins __instance;

    private IPluginFactory __pluginFactory;

    /**
     * @return 返回默认插件框架管理器实例对象
     */
    public static IPlugins get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Plugins.class);
                }
            }
        }
        return __instance;
    }

    @Override
    public String getName() {
        return IPlugins.MODULE_NAME;
    }

    @Override
    public void init(YMP owner) throws Exception {
        if (!__inited) {
            __owner = owner;
            __owner.getEvents().registerEvent(PluginEvent.class);
            //
            IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(MODULE_NAME));
            //
            boolean _disabled = _moduleCfg.getBoolean(IPluginConfig.DISABLED);
            //
            _LOG.info("Initializing ymate-platform-plugin-" + VERSION + (_disabled ? " - disabled" : StringUtils.EMPTY));
            //
            if (!_disabled) {
                IPluginConfig _config = DefaultPluginConfig.create()
                        .pluginHome(new File(RuntimeUtils.replaceEnvVariable(_moduleCfg.getString(IPluginConfig.PLUGIN_HOME, "${root}/plugins"))))
                        .autoscanPackages(__owner.getConfig().getAutoscanPackages())
                        .autoscanPackages(Arrays.asList(_moduleCfg.getArray(IPluginConfig.AUTOSCAN_PACKAGES)))
                        .automatic(_moduleCfg.getBoolean(IPluginConfig.AUTOMATIC, true))
                        .eventListener(this).build();
                //
                __pluginFactory = new DefaultPluginFactory(__owner, _moduleCfg.getBoolean(IPluginConfig.INCLUDED_CLASSPATH, true)) {
                    // For Constructor With includedClassPath.
                };
                __pluginFactory.init(_config);
                //
                __owner.getEvents().registerListener(Events.MODE.NORMAL, ApplicationEvent.class, new IEventListener<ApplicationEvent>() {
                    @Override
                    public boolean handle(ApplicationEvent context) {
                        if (context.getEventName() == ApplicationEvent.EVENT.APPLICATION_INITED) {
                            try {
                                __pluginFactory.startup();
                            } catch (Exception e) {
                                _LOG.warn("A exception occurred while startup plugins: ", RuntimeUtils.unwrapThrow(e));
                            }
                        }
                        return false;
                    }
                });
            }
            //
            __inited = true;
        }
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            if (__pluginFactory != null) {
                __pluginFactory.destroy();
                __pluginFactory = null;
            }
            //
            __owner = null;
        }
    }

    @Override
    public IPluginConfig getConfig() {
        if (__pluginFactory == null) {
            return null;
        }
        return __pluginFactory.getPluginConfig();
    }

    @Override
    public IPlugin getPlugin(String id) {
        if (__pluginFactory == null) {
            return null;
        }
        return __pluginFactory.getPlugin(id);
    }

    @Override
    public <T> T getPlugin(Class<T> clazz) {
        if (__pluginFactory == null) {
            return null;
        }
        return __pluginFactory.getPlugin(clazz);
    }

    @Override
    public void onInited(IPluginContext context, IPlugin plugin) {
        if (__pluginFactory != null) {
            if (__pluginFactory.getOwner().getConfig().isDevelopMode() && _LOG.isInfoEnabled()) {
                _LOG.info("--> " + context.getPluginMeta().toString() + " initialized.");
            }
            __owner.getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_INITED));
        }
    }

    @Override
    public void onStarted(IPluginContext context, IPlugin plugin) {
        if (__pluginFactory != null) {
            if (__pluginFactory.getOwner().getConfig().isDevelopMode() && _LOG.isInfoEnabled()) {
                _LOG.info("--> " + context.getPluginMeta().toString() + " started.");
            }
            __owner.getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_STARTED));
        }
    }

    @Override
    public void onShutdown(IPluginContext context, IPlugin plugin) {
        if (__pluginFactory != null) {
            if (__pluginFactory.getOwner().getConfig().isDevelopMode() && _LOG.isInfoEnabled()) {
                _LOG.info("--> " + context.getPluginMeta().toString() + " shutdown.");
            }
            __owner.getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_SHUTDOWN));
        }
    }

    @Override
    public void onDestroy(IPluginContext context, IPlugin plugin) {
        if (__pluginFactory != null) {
            if (__pluginFactory.getOwner().getConfig().isDevelopMode() && _LOG.isInfoEnabled()) {
                _LOG.info("--> " + context.getPluginMeta().toString() + " destroyed.");
            }
            __owner.getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_DESTROYED));
        }
    }
}
