/*
 * Copyright 2007-present the original author or authors.
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

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.LazyHolder;
import net.ymate.platform.commons.impl.DefaultPasswordProcessor;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.commons.util.ResourceUtils;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.impl.DefaultApplicationConfigureFactory;
import net.ymate.platform.core.impl.DefaultApplicationConfigureParseFactory;
import net.ymate.platform.core.impl.DefaultApplicationCreator;
import net.ymate.platform.core.module.IModule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * YMP框架核心管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-23 下午5:52:44
 */
public final class YMP {

    public static final Version VERSION = Version.VERSION;

    private static final String DEFAULT_BANNER_STR = "__  __ __  ___ ___\n" +
            "\\ \\/ //  |/  // _ \\\n" +
            " \\  // /|_/ // ___/\n" +
            " /_//_/  /_//_/  www.ymate.net";

    private static final String VERSION_STR = "version";

    private static final Log LOG = LogFactory.getLog(YMP.class);

    private static volatile IApplication instance;

    /**
     * 主类解析器，从系统属性中延迟解析主类
     *
     * @since 2.1.4
     */
    private static final class MainClassResolver {

        static final Class<?> MAIN_CLASS;

        static {
            Class<?> clazz = null;
            String mainClassName = System.getProperty(IApplication.SYSTEM_MAIN_CLASS);
            if (StringUtils.isNotBlank(mainClassName)) {
                try {
                    clazz = Class.forName(mainClassName);
                } catch (ClassNotFoundException ignored) {
                }
            }
            MAIN_CLASS = clazz;
        }
    }

    /**
     * 静态配置持有者，集中管理所有延迟初始化的工厂实例
     *
     * @since 2.1.4
     */
    private static final class Config {

        static final LazyHolder<IApplicationConfigureFactory> CONFIGURE_FACTORY =
                LazyHolder.of(() -> {
                    IApplicationConfigureFactory factory = ClassUtils.loadClass(IApplicationConfigureFactory.class, DefaultApplicationConfigureFactory.class);
                    factory.setMainClass(MainClassResolver.MAIN_CLASS);
                    if (LOG != null && LOG.isInfoEnabled()) {
                        LOG.info(String.format("Using IApplicationConfigureFactory class [%s].", factory.getClass().getName()));
                    }
                    return factory;
                });

        static final LazyHolder<IApplicationConfigureParseFactory> CONFIGURE_PARSE_FACTORY =
                LazyHolder.of(() -> {
                    IApplicationConfigureParseFactory factory = ClassUtils.loadClass(IApplicationConfigureParseFactory.class, DefaultApplicationConfigureParseFactory.class);
                    if (LOG != null && LOG.isInfoEnabled()) {
                        LOG.info(String.format("Using IApplicationConfigureParseFactory class [%s].", factory.getClass().getName()));
                    }
                    return factory;
                });

        static final LazyHolder<IBeanLoadFactory> BEAN_LOAD_FACTORY =
                LazyHolder.of(() -> {
                    IBeanLoadFactory factory = ClassUtils.loadClass(IBeanLoadFactory.class);
                    if (LOG != null && LOG.isInfoEnabled()) {
                        LOG.info(String.format("Using IBeanLoadFactory class [%s].", factory.getClass().getName()));
                    }
                    return factory;
                });

        static final LazyHolder<IProxyFactory> PROXY_FACTORY =
                LazyHolder.of(() -> {
                    IProxyFactory factory = ClassUtils.loadClass(IProxyFactory.class);
                    if (LOG != null && LOG.isInfoEnabled()) {
                        LOG.info(String.format("Using IProxyFactory class [%s].", factory.getClass().getName()));
                    }
                    return factory;
                });

        static final LazyHolder<IPasswordProcessor> PASSWORD_PROCESSOR =
                LazyHolder.of(() -> {
                    String passwordClassName = System.getProperty(IApplication.SYSTEM_PASS_CLASS);
                    if (StringUtils.isNotBlank(passwordClassName)) {
                        IPasswordProcessor proc = ClassUtils.impl(passwordClassName, IPasswordProcessor.class, YMP.class);
                        if (proc != null) {
                            if (LOG != null && LOG.isInfoEnabled()) {
                                LOG.info(String.format("Using IPasswordProcessor class [%s].", proc.getClass().getName()));
                            }
                            return proc;
                        }
                    }
                    IPasswordProcessor proc = ClassUtils.loadClass(IPasswordProcessor.class, DefaultPasswordProcessor.class);
                    if (LOG != null && LOG.isInfoEnabled()) {
                        LOG.info(String.format("Using IPasswordProcessor class [%s].", proc.getClass().getName()));
                    }
                    return proc;
                });

        static final LazyHolder<IApplication.Environment> ENVIRONMENT =
                LazyHolder.of(() -> {
                    try {
                        String runDevStr = System.getProperty(IApplication.SYSTEM_ENV);
                        if (StringUtils.isNotBlank(runDevStr)) {
                            return IApplication.Environment.valueOf(runDevStr.toUpperCase());
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                    return IApplication.Environment.UNKNOWN;
                });
    }

    static {
        showBanner();
    }

    public static IApplicationConfigureFactory getConfigureFactory() {
        return Config.CONFIGURE_FACTORY.get();
    }

    public static IApplicationConfigureParseFactory getConfigureParseFactory() {
        return Config.CONFIGURE_PARSE_FACTORY.get();
    }

    public static IBeanLoadFactory getBeanLoadFactory() {
        return Config.BEAN_LOAD_FACTORY.get();
    }

    public static IProxyFactory getProxyFactory() {
        return Config.PROXY_FACTORY.get();
    }

    /**
     * @return 返回全局密码处理器接口实例
     * @since 2.1.2
     */
    public static IPasswordProcessor getPasswordProcessor() {
        return Config.PASSWORD_PROCESSOR.get();
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
        IApplication.Environment env = Config.ENVIRONMENT.get();
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
        return run(null, applicationInitializers);
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
        IApplication application = instance;
        if (application == null) {
            synchronized (YMP.class) {
                application = instance;
                if (application == null) {
                    IApplicationCreator creator = ClassUtils.getExtensionLoader(IApplicationCreator.class).getExtension();
                    if (creator == null) {
                        creator = new DefaultApplicationCreator();
                    }
                    if (LOG != null && LOG.isInfoEnabled()) {
                        LOG.info(String.format("Using IApplicationCreator class [%s].", creator.getClass().getName()));
                    }
                    application = creator.create(MainClassResolver.MAIN_CLASS, args, applicationInitializers);
                    if (application == null) {
                        throw new IllegalStateException(String.format("IApplicationCreator [%s] returns the IApplication interface instance object invalid.", creator.getClass().getName()));
                    }
                    //
                    instance = application;
                    //
                    application.initialize();
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
        if (LOG != null && LOG.isInfoEnabled()) {
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
        String bannerStr = null;
        try (InputStream inputStream = ResourceUtils.getResourceAsStream("banner.txt", YMP.class)) {
            if (inputStream != null) {
                bannerStr = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }
        bannerStr = String.format("\n%s", StringUtils.defaultIfBlank(bannerStr, DEFAULT_BANNER_STR));
        if (LOG != null && LOG.isInfoEnabled()) {
            LOG.info(bannerStr);
        } else {
            System.out.println(bannerStr);
        }
    }

    private YMP() {
    }
}
