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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.commons.util.ResourceUtils;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.impl.DefaultProxyFactory;
import net.ymate.platform.core.module.IModule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * YMP框架核心管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-23 下午5:52:44
 */
public final class YMP {

    public static final Version VERSION = new Version(2, 1, 0, Version.VersionType.Release);

    private static final String DEFAULT_BANNER_STR = "__  __ __  ___ ___\n" +
            "\\ \\/ //  |/  // _ \\\n" +
            " \\  // /|_/ // ___/\n" +
            " /_//_/  /_//_/  www.ymate.net";

    private static final String VERSION_STR = "version";

    private static final Log LOG = LogFactory.getLog(YMP.class);

    private static volatile IApplication instance;

    private static volatile IApplicationConfigureFactory configureFactory;

    private static volatile IApplicationConfigureParseFactory configureParseFactory;

    private static volatile IBeanLoadFactory beanLoadFactory;

    private static volatile IProxyFactory proxyFactory;

    private static volatile IApplication.Environment environment;

    static {
        showBanner();
    }

    public static IApplicationConfigureFactory getConfigureFactory() {
        IApplicationConfigureFactory inst = configureFactory;
        if (inst == null) {
            synchronized (YMP.class) {
                inst = configureFactory;
                if (inst == null) {
                    configureFactory = inst = ClassUtils.loadClass(IApplicationConfigureFactory.class);
                }
            }
        }
        return inst;
    }

    public static IApplicationConfigureParseFactory getConfigureParseFactory() {
        IApplicationConfigureParseFactory inst = configureParseFactory;
        if (inst == null) {
            synchronized (YMP.class) {
                inst = configureParseFactory;
                if (inst == null) {
                    configureParseFactory = inst = ClassUtils.loadClass(IApplicationConfigureParseFactory.class);
                }
            }
        }
        return inst;
    }

    public static IBeanLoadFactory getBeanLoadFactory() {
        IBeanLoadFactory inst = beanLoadFactory;
        if (inst == null) {
            synchronized (YMP.class) {
                inst = beanLoadFactory;
                if (inst == null) {
                    beanLoadFactory = inst = ClassUtils.loadClass(IBeanLoadFactory.class);
                }
            }
        }
        return inst;
    }

    public static IProxyFactory getProxyFactory() {
        IProxyFactory inst = proxyFactory;
        if (inst == null) {
            synchronized (YMP.class) {
                inst = proxyFactory;
                if (inst == null) {
                    proxyFactory = inst = ClassUtils.loadClass(IProxyFactory.class, DefaultProxyFactory.class);
                }
            }
        }
        return inst;
    }

    /**
     * 获取运行模式, 默认值: UNKNOWN
     *
     * @return 返回运行模式枚举值
     */
    public static IApplication.Environment getPriorityRunEnv() {
        return getPriorityRunEnv(null);
    }

    /**
     * 获取运行模式, 若系统级设置存在则优先使用, 否则返回由参数指定的值, 若参数值为空则返回默认值: UNKNOWN
     *
     * @param runEnv 指定运行模式枚举值
     * @return 返回运行模式枚举值
     */
    public static IApplication.Environment getPriorityRunEnv(IApplication.Environment runEnv) {
        IApplication.Environment env = environment;
        if (env == null) {
            synchronized (YMP.class) {
                env = environment;
                if (env == null) {
                    try {
                        String runDevStr = System.getProperty(IApplication.SYSTEM_ENV);
                        if (StringUtils.isNotBlank(runDevStr)) {
                            env = IApplication.Environment.valueOf(runDevStr.toUpperCase());
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                    environment = env = env != null ? env : IApplication.Environment.UNKNOWN;
                }
            }
        }
        return IApplication.Environment.UNKNOWN.equals(env) && runEnv != null ? runEnv : env;
    }

    /**
     * 执行框架初始化动作, 若已初始化则直接返回当前应用容器实例对象
     *
     * @param applicationInitializers 扩展初始化处理器
     * @return 返回应用容器实例对象
     * @throws Exception 可能产生的任何异常
     */
    public static IApplication run(IApplicationInitializer... applicationInitializers) throws Exception {
        return run(null, null, applicationInitializers);
    }

    /**
     * 执行框架初始化动作, 若已初始化则直接返回当前应用容器实例对象
     *
     * @param args                    启动参数集合
     * @param applicationInitializers 扩展初始化处理器
     * @return 返回应用容器实例对象
     * @throws Exception 可能产生的任何异常
     */
    public static IApplication run(String[] args, IApplicationInitializer... applicationInitializers) throws Exception {
        return run(null, args, applicationInitializers);
    }

    /**
     * 执行框架初始化动作, 若已初始化则直接返回当前应用容器实例对象
     *
     * @param mainClass               启动配置类(用于解析初始化配置注解)
     * @param args                    启动参数集合
     * @param applicationInitializers 扩展初始化处理器
     * @return 返回应用容器实例对象
     * @throws Exception 可能产生的任何异常
     */
    public static IApplication run(Class<?> mainClass, String[] args, IApplicationInitializer... applicationInitializers) throws Exception {
        IApplication application = instance;
        if (application == null) {
            synchronized (YMP.class) {
                application = instance;
                if (application == null) {
                    IApplicationCreator creator = ClassUtils.getExtensionLoader(IApplicationCreator.class).getExtension();
                    if (creator == null) {
                        throw new ClassNotFoundException(String.format("Implementation class of interface [%s] not found.", IApplicationCreator.class.getName()));
                    }
                    application = creator.create(mainClass, args, applicationInitializers);
                    if (application == null) {
                        throw new IllegalStateException(String.format("IApplicationCreator [%s] returns the IApplication interface instance object invalid.", creator.getClass().getName()));
                    }
                    //
                    application.initialize();
                    //
                    instance = application;
                }
            }
        }
        return application;
    }

    /**
     * 获取当前已初始化的应用容器实例
     *
     * @return 返回应用容器实例对象, 若尚未初始化将抛出IllegalStateException异常
     */
    public static IApplication get() {
        if (instance == null) {
            throw new IllegalStateException("IApplication has not been initialized. Call YMP.run method to complete the initialization first.");
        }
        return instance;
    }

    public static void destroy() throws Exception {
        if (isInitialized()) {
            instance.close();
            instance = null;
        }
    }

    public static boolean isInitialized() {
        return instance != null && instance.isInitialized();
    }

    public static void showModuleVersion(String moduleName, IModule module) {
        showModuleVersion(moduleName, null, module);
    }

    public static void showModuleVersion(String moduleName, String suffix, IModule module) {
        showVersion(String.format("Initializing %s-${version} %s", StringUtils.defaultIfBlank(moduleName, module.getName()), StringUtils.trimToEmpty(suffix)), new Version(VERSION, module.getClass()));
    }

    public static void showVersion(String formatStr, Version version) throws IllegalArgumentException {
        if (LOG.isInfoEnabled()) {
            if (StringUtils.isNotBlank(formatStr) && version != null) {
                ExpressionUtils expression = ExpressionUtils.bind(formatStr);
                if (!expression.getVariables().contains(VERSION_STR)) {
                    throw new IllegalArgumentException("Invalid parameter \"formatStr\" does not contain \"${version}\".");
                }
                LOG.info(expression.set(VERSION_STR, version.toString()).clean().getResult());
            }
        }
    }

    private static void showBanner() {
        if (LOG.isInfoEnabled()) {
            String bannerStr = null;
            try (InputStream inputStream = ResourceUtils.getResourceAsStream("banner.txt", YMP.class)) {
                if (inputStream != null) {
                    bannerStr = IOUtils.toString(inputStream, "UTF-8");
                }
            } catch (IOException ignored) {
            }
            LOG.info(String.format("\n%s", StringUtils.defaultIfBlank(bannerStr, DEFAULT_BANNER_STR)));
        }
    }

    private YMP() {
    }
}
