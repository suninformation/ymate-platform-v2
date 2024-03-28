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
package net.ymate.platform.log;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.configuration.Cfgs;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.core.support.RecycleHelper;
import net.ymate.platform.log.impl.DefaultLogConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 下午03:56:24
 */
public final class Logs implements ILog {

    private static final Log LOG = LogFactory.getLog(Logs.class);

    private static final Map<String, ILogger> LOGGER_CACHE = new ConcurrentHashMap<>();

    private static final ILog INSTANCE;

    static {
        try {
            // 尝试优先初始化配置体系
            Class.forName(Cfgs.class.getName());
        } catch (NoClassDefFoundError | ClassNotFoundException ignored) {
        }
        //
        DefaultLogConfig logConfig = null;
        IApplicationConfigureFactory configureFactory = YMP.getConfigureFactory();
        if (configureFactory != null) {
            IApplicationConfigurer configurer = configureFactory.getConfigurer();
            IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
            if (moduleConfigurer != null) {
                logConfig = DefaultLogConfig.create(configureFactory.getMainClass(), moduleConfigurer);
            } else {
                logConfig = DefaultLogConfig.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
            }
        }
        ILog logInst = new Logs(logConfig != null ? logConfig : DefaultLogConfig.defaultConfig());
        try {
            logInst.initialize();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        INSTANCE = logInst;
    }

    private ILogConfig config;

    private boolean initialized;

    private ILogger logger;

    public static ILog get() {
        return INSTANCE;
    }

    private Logs(ILogConfig config) {
        this.config = config;
    }

    @Override
    public void initialize() throws Exception {
        if (!initialized) {
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Initializing ymate-platform-log-%s", new Version(YMP.VERSION, this.getClass())));
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            //
            System.setProperty(LOG_OUT_DIR, config.getOutputDir().getPath());
            //
            logger = ReentrantLockHelper.putIfAbsentAsync(LOGGER_CACHE, config.getDefaultLoggerName(), () -> ClassUtils.impl(config.getLoggerClass(), ILogger.class).initialize(config.getDefaultLoggerName(), config));
            //
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("-- LOG_CONFIG_FILE: %s", config.getConfigFile().getPath()));
                LOG.info(String.format("-- LOG_OUTPUT_DIR: %s", config.getOutputDir().getPath()));
                LOG.info(String.format("-- LOGGER_CLASS: %s", config.getLoggerClass().getName()));
                LOG.info(String.format("-- DEFAULT_LOG_NAME: %s", config.getDefaultLoggerName()));
                LOG.info(String.format("-- ALLOW_CONSOLE_OUTPUT: %s", config.isAllowConsoleOutput()));
                LOG.info(String.format("-- FORMAT_PADDED_OUTPUT: %s", config.isFormatPaddedOutput()));
                LOG.info(String.format("-- SIMPLIFIED_PACKAGE_NAME: %s", config.isSimplifiedPackageName()));
            }
            initialized = config.isInitialized();
            //
            RecycleHelper.getInstance().register(this);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() {
        if (initialized) {
            initialized = false;
            //
            LOGGER_CACHE.values().forEach(ILogger::destroy);
            //
            logger = null;
            config = null;
        }
    }

    @Override
    public ILogConfig getConfig() {
        return config;
    }

    @Override
    public ILogger getLogger() {
        return logger;
    }

    @Override
    public synchronized ILogger getLogger(String loggerName) throws Exception {
        return ReentrantLockHelper.putIfAbsentAsync(LOGGER_CACHE, loggerName, () -> getLogger().getLogger(loggerName, config));
    }

    @Override
    public ILogger getLogger(Class<?> clazz) throws Exception {
        return getLogger(clazz.getName());
    }
}
