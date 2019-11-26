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

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.annotation.*;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.annotation.Event;
import net.ymate.platform.core.event.annotation.EventRegister;
import net.ymate.platform.core.handle.*;
import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.module.ModuleManager;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.serialize.annotation.Serializer;
import net.ymate.platform.core.support.RecycleHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-04-26 01:12
 * @since 2.1.0
 */
public final class Application implements IApplication {

    private static final Log LOG = LogFactory.getLog(Application.class);

    private final Map<String, String> parameters = new HashMap<>();

    private final IApplicationConfigurer configurer;

    private final IApplicationInitializer initializer;

    private ModuleManager moduleManager;

    private IBeanFactory beanFactory;

    private final RecycleHelper recycleHelper = RecycleHelper.getInstance();

    private I18N i18n;

    private Environment runEnv;

    private Events events;

    private final InterceptSettings interceptSettings;

    private boolean initialized;

    private boolean errorFlag;

    public Application(IApplicationConfigureFactory factory) {
        this(factory.getConfigurer());
    }

    public Application(IApplicationConfigureFactory factory, IApplicationInitializer initializer) {
        this(factory.getConfigurer(), initializer);
    }

    public Application(IApplicationConfigurer configurer) {
        this(configurer, null);
    }

    public Application(IApplicationConfigurer configurer, IApplicationInitializer initializer) {
        this.configurer = configurer;
        this.initializer = initializer;
        //
        this.moduleManager = new ModuleManager();
        this.beanFactory = new DefaultBeanFactory(configurer.getProxyFactory());
        this.i18n = new I18N(configurer.getDefaultLocale(), configurer.getI18nEventHandler());
        this.events = new Events(this);
        this.interceptSettings = configurer.getInterceptSettings() != null ? configurer.getInterceptSettings() : InterceptSettings.create();
        this.runEnv = configurer.getRunEnv();
        //
        if (configurer.getParameters() != null && !configurer.getParameters().isEmpty()) {
            this.parameters.putAll(configurer.getParameters());
        }
    }

    @Override
    public void initialize() {
        if (!initialized) {
            //
            YMP.showVersion(String.format("Initializing ymate-platform-core-${version} - debug:%s - env:%s - PID:%s", isDevEnv(), runEnv != null ? runEnv.name().toLowerCase() : Environment.UNKNOWN, RuntimeUtils.getProcessId()), YMP.VERSION);
            //
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //
            if (!i18n.isInitialized()) {
                i18n.initialize();
            }
            //
            if (!events.isInitialized()) {
                events.initialize();
            }
            events.registerEvent(ApplicationEvent.class);
            //
            if (initializer != null) {
                initializer.afterEventInit(this, events);
            }
            //
            try {
                moduleManager.addExcludedModules(configurer.getExcludedModules());
                // 触发容器启动事件
                events.fireEvent(new ApplicationEvent(this, ApplicationEvent.EVENT.APPLICATION_STARTUP));
                //
                if (initializer != null) {
                    initializer.beforeModuleManagerInit(this, moduleManager);
                }
                moduleManager.initialize(this);
                //
                if (initializer != null) {
                    initializer.beforeBeanFactoryInit(this, beanFactory);
                }
                // 尝试使用自定义加载器加载模块
                IBeanLoadFactory beanLoadFactory = configurer.getBeanLoadFactory();
                if (beanLoadFactory != null && beanLoadFactory.getBeanLoader() != null) {
                    IBeanLoader beanLoader = beanLoadFactory.getBeanLoader();
                    //
                    beanLoader.registerPackageName(YMP_BASE_PACKAGE_NAME);
                    beanLoader.registerPackageNames(configurer.getPackageNames());
                    beanLoader.registerExcludedPackageNames(configurer.getExcludedPackageNames());
                    beanLoader.registerExcludedFiles(configurer.getExcludedFiles());
                    //
                    beanLoader.registerHandler(Bean.class);
                    beanLoader.registerHandler(Interceptor.class, new InterceptorHandler(this));
                    beanLoader.registerHandler(Packages.class, new PackagesHandler(this));
                    //
                    beanLoader.registerHandler(Injector.class, new InjectorHandler(this));
                    beanLoader.registerHandler(Event.class, new EventHandler(this));
                    beanLoader.registerHandler(EventRegister.class, new EventRegisterHandler(this));
                    beanLoader.registerHandler(Module.class, new ModuleHandler(this));
                    beanLoader.registerHandler(Proxy.class, new ProxyHandler(this));
                    beanLoader.registerHandler(Serializer.class, new SerializerHandler());
                    //
                    if (initializer != null) {
                        initializer.beforeBeanLoad(this, beanLoader);
                    }
                    beanLoader.load(beanFactory);
                }
                beanFactory.initialize(this);
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
                errorFlag = true;
            }
            if (!errorFlag) {
                // 触发容器初始化完成事件
                events.fireEvent(new ApplicationEvent(this, ApplicationEvent.EVENT.APPLICATION_INITIALIZED));
                //
                stopWatch.stop();
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("RecycleHelper has registered the number of resources to be recycled: %d", recycleHelper.size()));
                    LOG.info(String.format("Initialization completed, Total time: %dms", stopWatch.getTime()));
                }
            }
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IApplicationConfigurer getConfigurer() {
        return configurer;
    }

    @Override
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            recycleHelper.recycle();
            //
            if (!errorFlag) {
                // 触发容器销毁事件
                events.fireEvent(new ApplicationEvent(this, ApplicationEvent.EVENT.APPLICATION_DESTROYED));
            }
            initialized = false;
            //
            moduleManager.close();
            //
            i18n.close();
            beanFactory.close();
            events.destroy();
        }
    }

    @Override
    public IBeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public RecycleHelper getRecycleHelper() {
        return recycleHelper;
    }

    @Override
    public I18N getI18n() {
        return i18n;
    }

    @Override
    public boolean isTestEnv() {
        return Environment.TEST.equals(runEnv);
    }

    @Override
    public boolean isDevEnv() {
        return Environment.DEV.equals(runEnv);
    }

    @Override
    public boolean isProductEnv() {
        return Environment.PRODUCT.equals(runEnv);
    }

    @Override
    public Environment getRunEnv() {
        return runEnv;
    }

    @Override
    public Events getEvents() {
        return events;
    }

    @Override
    public InterceptSettings getInterceptSettings() {
        return interceptSettings;
    }

    @Override
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String getParam(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return parameters.get(name);
    }

    @Override
    public String getParam(String name, String defaultValue) {
        return StringUtils.defaultIfBlank(getParam(name), defaultValue);
    }
}
