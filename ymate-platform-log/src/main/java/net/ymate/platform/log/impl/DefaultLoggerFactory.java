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
package net.ymate.platform.log.impl;

import net.ymate.platform.log.ILogConfig;
import net.ymate.platform.log.ILogger;
import net.ymate.platform.log.ILoggerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author 刘镇 (suninformation@163.com) on 2026/8/15 2:52
 * @since 2.1.4
 */
public class DefaultLoggerFactory implements ILoggerFactory {

    private ILogConfig config;

    private boolean initialized;

    @Override
    public void initialize(ILogConfig config) throws Exception {
        if (!initialized) {
            try (InputStream inputStream = Files.newInputStream(config.getConfigFile().toPath())) {
                ConfigurationSource source = new ConfigurationSource(inputStream);
                LoggerContext loggerContext = Configurator.initialize(null, source);
                ConfigurationFactory.setConfigurationFactory(new XmlConfigurationFactory() {

                    private final Configuration config = new DefaultConfiguration();

                    @Override
                    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
                        return config;
                    }
                });
                ConfigurationFactory.getInstance().getConfiguration(loggerContext, source);
                //
                this.config = config;
                this.initialized = true;
            }
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public ILogger getLogger(String loggerName) throws Exception {
        return new DefaultLogger().initialize(loggerName, config);
    }

    public void destroy() {
        if (initialized) {
            try {
                LogManager.shutdown();
            } catch (Throwable ignored) {
            }
        }
    }
}
