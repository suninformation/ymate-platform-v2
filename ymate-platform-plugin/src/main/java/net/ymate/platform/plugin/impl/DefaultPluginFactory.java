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
package net.ymate.platform.plugin.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.core.beans.annotation.Interceptor;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.plugin.*;
import net.ymate.platform.plugin.annotation.Plugin;
import net.ymate.platform.plugin.annotation.PluginConf;
import net.ymate.platform.plugin.annotation.PluginFactory;
import net.ymate.platform.plugin.annotation.PluginRefer;
import net.ymate.platform.plugin.handle.PluginBeanHandler;
import net.ymate.platform.plugin.handle.PluginHandler;
import net.ymate.platform.plugin.handle.PluginInterceptorHandler;
import net.ymate.platform.plugin.handle.PluginReferInjector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 默认插件工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-2 下午3:22:17
 */
public class DefaultPluginFactory implements IPluginFactory {

    private static final Log LOG = LogFactory.getLog(DefaultPluginFactory.class);

    private IApplication owner;

    private IPluginBeanFactory pluginBeanFactory;

    private IPluginConfig pluginConfig;

    private IBeanLoader beanLoader;

    private PluginClassLoader pluginClassLoader;

    private final IPluginEventListener eventListener;

    private final boolean includedClassPath;

    private boolean initialized;

    private boolean started;

    /**
     * @param owner        指定所属容器参数对象
     * @param pluginHome   插件根路径
     * @param packageNames 自动扫描包路径
     * @return 创建并返回默认插件工厂实例
     * @throws Exception 创建插件工厂时可能产生的异常
     */
    public static DefaultPluginFactory create(IApplication owner, String pluginHome, String[] packageNames) throws Exception {
        DefaultPluginFactory pluginFactory = new DefaultPluginFactory(DefaultPluginConfig.load(pluginHome, packageNames), false);
        pluginFactory.initialize(owner);
        //
        return pluginFactory;
    }

    /**
     * @param owner 指定所属容器参数对象
     * @param clazz 指定的插件工厂类型
     * @return 创建并返回由clazz指定类型的插件工厂实例
     * @throws Exception 创建插件工厂时可能产生的异常
     */
    public static IPluginFactory create(IApplication owner, Class<? extends IPluginFactory> clazz) throws Exception {
        return create(owner, clazz, null);
    }

    /**
     * @param owner        指定所属容器参数对象
     * @param clazz        指定的插件工厂类型
     * @param pluginConfig 指定的插件工厂初始化配置
     * @return 采用指定的初始化配置创建并返回由clazz指定类型的插件工厂实例
     * @throws Exception 创建插件工厂时可能产生的异常
     */
    public static IPluginFactory create(IApplication owner, Class<? extends IPluginFactory> clazz, IPluginConfig pluginConfig) throws Exception {
        IPluginFactory pluginFactory = ClassUtils.impl(clazz, IPluginFactory.class, new Class<?>[]{IPluginConfig.class}, new Object[]{pluginConfig == null && clazz.isAnnotationPresent(PluginFactory.class) ? DefaultPluginConfig.load(clazz) : pluginConfig}, false);
        if (pluginFactory != null) {
            pluginFactory.initialize(owner);
        }
        return pluginFactory;
    }

    public static IPluginFactory create(IModuleConfigurer moduleConfigurer) {
        return create(null, moduleConfigurer);
    }

    public static IPluginFactory create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        PluginConf confAnn = mainClass == null ? null : mainClass.getAnnotation(PluginConf.class);
        //
        boolean included = configReader.getBoolean(IPluginConfig.INCLUDED_CLASSPATH, confAnn != null && confAnn.includeClasspath());
        //
        List<String> packageNames = new ArrayList<>(configReader.getList(IPluginConfig.PACKAGE_NAMES));
        if (packageNames.isEmpty() && confAnn != null) {
            packageNames.add(mainClass.getPackage().getName());
            packageNames.addAll(Arrays.asList(confAnn.packageNames()));
        }
        IPluginConfig pluginConfig = DefaultPluginConfig.builder()
                .pluginHome(new File(RuntimeUtils.replaceEnvVariable(configReader.getString(IPluginConfig.PLUGIN_HOME, StringUtils.defaultIfBlank(confAnn == null ? null : confAnn.pluginHome(), IPluginConfig.DEFAULT_PLUGIN_HOME)))))
                .packageNames(packageNames)
                .enabled(configReader.getBoolean(IPluginConfig.ENABLED, confAnn == null || confAnn.enabled()))
                .automatic(configReader.getBoolean(IPluginConfig.AUTOMATIC, confAnn == null || confAnn.automatic()))
                .eventListener(new IPluginEventListener() {
                    private boolean doCheckContext(IPluginContext context, PluginEvent.EVENT event) {
                        if (context.getPluginFactory() != null) {
                            if (context.getPluginFactory().getOwner().isDevEnv() && LOG.isInfoEnabled()) {
                                LOG.info(String.format("%s %s.", context.getPluginMeta().toString(), StringUtils.substringAfter(event.name(), "_").toLowerCase()));
                            }
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onInitialized(IPluginContext context, IPlugin plugin) {
                        if (doCheckContext(context, PluginEvent.EVENT.PLUGIN_INITIALIZED)) {
                            context.getPluginFactory().getOwner().getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_INITIALIZED));
                        }
                    }

                    @Override
                    public void onStarted(IPluginContext context, IPlugin plugin) {
                        if (doCheckContext(context, PluginEvent.EVENT.PLUGIN_STARTED)) {
                            context.getPluginFactory().getOwner().getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_STARTED));
                        }
                    }

                    @Override
                    public void onShutdown(IPluginContext context, IPlugin plugin) {
                        if (doCheckContext(context, PluginEvent.EVENT.PLUGIN_SHUTDOWN)) {
                            context.getPluginFactory().getOwner().getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_SHUTDOWN));
                        }
                    }

                    @Override
                    public void onDestroy(IPluginContext context, IPlugin plugin) {
                        if (doCheckContext(context, PluginEvent.EVENT.PLUGIN_DESTROYED)) {
                            context.getPluginFactory().getOwner().getEvents().fireEvent(new PluginEvent(plugin, PluginEvent.EVENT.PLUGIN_DESTROYED));
                        }
                    }
                }).build();
        return new DefaultPluginFactory(pluginConfig, included);
    }

    /**
     * 构造器(仅限内部使用)
     *
     * @param pluginConfig      指定的插件工厂初始化配置
     * @param includedClassPath 是否扫描当前CLASSPATH内的相关插件
     */
    private DefaultPluginFactory(IPluginConfig pluginConfig, boolean includedClassPath) {
        this.pluginConfig = pluginConfig;
        this.includedClassPath = includedClassPath;
        this.eventListener = pluginConfig.getPluginEventListener() != null ? pluginConfig.getPluginEventListener() : new DefaultPluginEventListener();
        this.beanLoader = (pluginConfig.getPluginBeanLoaderFactory() != null ? pluginConfig.getPluginBeanLoaderFactory() : new DefaultPluginBeanLoaderFactory()).getPluginBeanLoader();
        if (pluginConfig.isEnabled()) {
            this.beanLoader.registerPackageNames(pluginConfig.getPackageNames());
            this.beanLoader.registerHandler(Bean.class, new PluginBeanHandler(this));
            this.beanLoader.registerHandler(Interceptor.class, new PluginInterceptorHandler(this));
            this.beanLoader.registerHandler(Plugin.class, new PluginHandler(this));
        }
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized && pluginConfig.isEnabled()) {
            this.owner = owner;
            this.owner.getBeanFactory().registerInjector(PluginRefer.class, new PluginReferInjector(this));
            //
            pluginBeanFactory = new DefaultPluginBeanFactory(this, includedClassPath);
            pluginClassLoader = buildPluginClassLoader();
            //
            beanLoader.setClassLoader(pluginClassLoader);
            beanLoader.load(pluginBeanFactory);
            //
            pluginBeanFactory.initialize(owner);
            //
            initialized = true;
        }
    }

    @Override
    public void startup() throws Exception {
        if (!started) {
            for (PluginMeta pluginMeta : pluginBeanFactory.getPluginMetas()) {
                IPlugin plugin = pluginBeanFactory.getBean(pluginMeta.getInitClass());
                if (plugin != null && !plugin.isInitialized()) {
                    plugin.initialize(new DefaultPluginContext(this, pluginMeta));
                    eventListener.onInitialized(plugin.getPluginContext(), plugin);
                    if (pluginConfig.isAutomatic() && pluginMeta.isAutomatic()) {
                        plugin.startup();
                        eventListener.onStarted(plugin.getPluginContext(), plugin);
                    }
                }
            }
            //
            started = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            started = false;
            //
            for (PluginMeta pluginMeta : pluginBeanFactory.getPluginMetas()) {
                IPlugin plugin = pluginBeanFactory.getBean(pluginMeta.getInitClass());
                if (plugin != null) {
                    plugin.shutdown();
                    eventListener.onShutdown(plugin.getPluginContext(), plugin);
                    eventListener.onDestroy(plugin.getPluginContext(), plugin);
                    plugin.close();
                }
            }
            //
            pluginBeanFactory.close();
            pluginBeanFactory = null;
            //
            pluginConfig = null;
            pluginClassLoader = null;
            beanLoader = null;
        }
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public IPluginConfig getPluginConfig() {
        return pluginConfig;
    }

    @Override
    public IBeanLoader getBeanLoader() {
        return beanLoader;
    }

    private synchronized PluginClassLoader buildPluginClassLoader() throws Exception {
        if (pluginClassLoader == null) {
            if (pluginConfig.getPluginHome() != null && pluginConfig.getPluginHome().exists() && pluginConfig.getPluginHome().isDirectory()) {
                List<URL> libs = new ArrayList<>();
                findLibFiles(new File(pluginConfig.getPluginHome(), ".plugin"), libs);
                findLibFiles(pluginConfig.getPluginHome(), libs);
                //
                pluginClassLoader = new PluginClassLoader(pluginConfig.getPluginHome().getPath(), libs.toArray(new URL[0]), this.getClass().getClassLoader());
            } else {
                throw new IllegalArgumentException(String.format("Invalid plugin home path [%s].", pluginConfig.getPluginHome()));
            }
        }
        return pluginClassLoader;
    }

    private void findLibFiles(File targetDir, List<URL> results) throws MalformedURLException {
        if (targetDir != null && targetDir.exists() && targetDir.isDirectory()) {
            File[] files = targetDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    // 扫描所有正式插件目录(即目录名称不以'.'开头的)
                    if (file.getName().charAt(0) != '.') {
                        if (file.isDirectory()) {
                            // 设置并扫描lib目录中jar文件
                            findLibFiles(new File(file, "lib"), results);
                            // 设置类文件路径
                            File classesFile = new File(file, "classes");
                            if (classesFile.exists() && classesFile.isDirectory()) {
                                results.add(classesFile.toURI().toURL());
                            }
                        }
                    } else if (file.isFile() && file.getAbsolutePath().endsWith("jar")) {
                        results.add(file.toURI().toURL());
                    }
                }
            }
        }
    }

    private boolean checkPluginStatus(IPlugin plugin) {
        if (plugin != null && plugin.isInitialized() && !plugin.isStarted()) {
            try {
                plugin.startup();
                eventListener.onStarted(plugin.getPluginContext(), plugin);
                return true;
            } catch (Exception e) {
                LOG.warn(String.format("A exception occurred while starting [%s]: ", plugin.getPluginContext().getPluginMeta().toString()), RuntimeUtils.unwrapThrow(e));
            }
        }
        return false;
    }

    @Override
    public IPlugin getPlugin(String idOrAlias) {
        IPlugin plugin = pluginBeanFactory.getPlugin(idOrAlias);
        if (checkPluginStatus(plugin)) {
            return plugin;
        }
        return null;
    }

    @Override
    public <T> T getPlugin(Class<T> clazz) {
        T plugin = pluginBeanFactory.getBean(clazz);
        if (checkPluginStatus((IPlugin) plugin)) {
            return plugin;
        }
        return null;
    }

    @Override
    public boolean isIncludedClassPath() {
        return includedClassPath;
    }
}
