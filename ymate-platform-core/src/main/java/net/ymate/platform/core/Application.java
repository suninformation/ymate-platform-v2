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

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.serialize.annotation.Serializer;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.annotation.ParamValue;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.core.beans.annotation.Injector;
import net.ymate.platform.core.beans.annotation.Interceptor;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.core.beans.intercept.IInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.annotation.Event;
import net.ymate.platform.core.event.annotation.EventListener;
import net.ymate.platform.core.event.annotation.EventRegister;
import net.ymate.platform.core.handle.*;
import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.module.ModuleManager;
import net.ymate.platform.core.support.RecycleHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-04-26 01:12
 * @since 2.1.0
 */
public final class Application implements IApplication {

    private static final Log LOG = LogFactory.getLog(Application.class);

    private final IConfigReader parameters;

    private final IApplicationConfigureFactory configureFactory;

    private final IApplicationInitializer initializer;

    private final ModuleManager moduleManager;

    private final IBeanFactory beanFactory;

    private final RecycleHelper recycleHelper = RecycleHelper.create();

    private final I18N i18n;

    private final Environment runEnv;

    private final Events events;

    private final InterceptSettings interceptSettings;

    private boolean initialized;

    private boolean errorFlag;

    public Application(IApplicationConfigureFactory configureFactory) {
        this(configureFactory, null);
    }

    public Application(IApplicationConfigureFactory configureFactory, IApplicationInitializer initializer) {
        this.configureFactory = configureFactory;
        this.initializer = initializer;
        this.moduleManager = new ModuleManager();
        //
        IApplicationConfigurer configurer = configureFactory.getConfigurer();
        this.moduleManager.addExcludedModules(configurer.getExcludedModules());
        this.moduleManager.addIncludedModules(configurer.getIncludedModules());
        this.runEnv = configurer.getRunEnv();
        this.beanFactory = new DefaultBeanFactory(configurer.getProxyFactory());
        this.i18n = new I18N(configurer.getDefaultLocale(), configurer.getI18nEventHandler());
        this.events = new Events(this);
        this.interceptSettings = configurer.getInterceptSettings() != null ? configurer.getInterceptSettings() : InterceptSettings.create();
        this.parameters = MapSafeConfigReader.bind(configurer.getParameters());
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
                IApplicationConfigurer configurer = configureFactory.getConfigurer();
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
                // 尝试执行自定义加载器
                IBeanLoadFactory beanLoadFactory = configurer.getBeanLoadFactory();
                if (beanLoadFactory != null) {
                    IBeanLoader beanLoader = beanLoadFactory.getBeanLoader();
                    if (beanLoader != null) {
                        beanLoader.registerPackageName(YMP_BASE_PACKAGE_NAME);
                        beanLoader.registerPackageNames(configurer.getPackageNames());
                        beanLoader.registerExcludedPackageNames(configurer.getExcludedPackageNames());
                        beanLoader.registerExcludedFiles(configurer.getExcludedFiles());
                        //
                        beanLoader.registerHandler(Bean.class);
                        beanLoader.registerHandler(Interceptor.class, new InterceptorHandler(this));
                        if (interceptSettings.isEnabled()) {
                            beanLoader.registerHandler(net.ymate.platform.core.beans.annotation.InterceptSettings.class, new InterceptSettingsHandler(this));
                        }
                        beanLoader.registerHandler(Injector.class, new InjectorHandler(this));
                        beanLoader.registerHandler(Event.class, new EventHandler(this));
                        beanLoader.registerHandler(EventRegister.class, new EventRegisterHandler(this));
                        beanLoader.registerHandler(EventListener.class, new EventListenerHandler(this));
                        beanLoader.registerHandler(Proxy.class, new ProxyHandler(this));
                        beanLoader.registerHandler(Serializer.class, new SerializerHandler());
                        if (initializer != null) {
                            initializer.beforeBeanLoad(this, beanLoader);
                        }
                        beanLoader.load(beanFactory);
                    }
                }
                beanFactory.registerInjector(ParamValue.class, (beanFactoryImpl, annotation, targetClass, field, originInject) -> {
                    ParamValue paramValueAnn = (ParamValue) annotation;
                    String paramName = StringUtils.defaultIfBlank(paramValueAnn.value(), field.getName());
                    String paramValue = getParam(paramName, StringUtils.trimToNull(paramValueAnn.defaultValue()));
                    if (String.class.equals(field.getType())) {
                        if (paramValueAnn.replaceEnvVariable()) {
                            return RuntimeUtils.replaceEnvVariable(paramValue);
                        }
                        return paramValue;
                    }
                    return BlurObject.bind(paramValue).toObjectValue(field.getType());
                });
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
                    LOG.info(String.format("RecycleHelper has registered the number of resources to be recycled: %d, global: %d", recycleHelper.size(), RecycleHelper.getInstance().size()));
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
    public IApplicationConfigureFactory getConfigureFactory() {
        return configureFactory;
    }

    @Override
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            recycleHelper.recycle(true);
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
    public void registerInterceptor(Class<? extends IInterceptor> interceptClass) {
        if (!interceptClass.isInterface()) {
            Interceptor interceptorAnn = interceptClass.getAnnotation(Interceptor.class);
            if (interceptorAnn != null) {
                if (!Annotation.class.equals(interceptorAnn.value())) {
                    registerInterceptAnnotation(interceptorAnn.value(), interceptClass, interceptorAnn.singleton());
                } else {
                    beanFactory.registerBean(BeanMeta.create(interceptClass, interceptorAnn.singleton()));
                }
            } else {
                beanFactory.registerBean(BeanMeta.create(interceptClass, true));
            }
        }
    }

    @Override
    public void registerInterceptAnnotation(Class<? extends Annotation> annotationClass, Class<? extends IInterceptor> interceptClass, boolean singleton) {
        if (annotationClass != null && !interceptClass.isInterface()) {
            interceptSettings.registerInterceptAnnotation(annotationClass, interceptClass);
            beanFactory.registerBean(BeanMeta.create(interceptClass, singleton));
        }
    }

    @Override
    public Map<String, String> getParams() {
        return parameters.toMap();
    }

    @Override
    public String getParam(String name) {
        return parameters.getString(name);
    }

    @Override
    public String getParam(String name, String defaultValue) {
        return parameters.getString(name, defaultValue);
    }

    @Override
    public IConfigReader getParamConfigReader() {
        return parameters;
    }
}
