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
package net.ymate.platform.log.impl;

import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;
import net.ymate.platform.log.ILog;
import net.ymate.platform.log.ILogConfig;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-12 17:53
 * @since 2.1.0
 */
public final class DefaultLogConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultLogConfigurable() {
        super(ILog.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultLogConfigurable configurable = new DefaultLogConfigurable();

        private Builder() {
        }

        public Builder configFile(String configFile) {
            configurable.addConfig(ILogConfig.CONFIG_FILE, configFile);
            return this;
        }

        public Builder outputDir(String outputDir) {
            configurable.addConfig(ILogConfig.OUTPUT_DIR, outputDir);
            return this;
        }

        public Builder defaultLoggerName(String defaultLoggerName) {
            configurable.addConfig(ILogConfig.LOGGER_NAME, defaultLoggerName);
            return this;
        }

        public Builder printStackCount(int printStackCount) {
            configurable.addConfig(ILogConfig.PRINT_STACK_COUNT, String.valueOf(printStackCount));
            return this;
        }

        public Builder logFormat(String logFormat) {
            configurable.addConfig(ILogConfig.LOG_FORMAT, logFormat);
            return this;
        }

        public Builder loggerClass(String loggerClass) {
            configurable.addConfig(ILogConfig.LOGGER_CLASS, loggerClass);
            return this;
        }

        public Builder allowConsoleOutput(boolean allowConsoleOutput) {
            configurable.addConfig(ILogConfig.ALLOW_OUTPUT_CONSOLE, String.valueOf(allowConsoleOutput));
            return this;
        }

        public Builder simplifiedPackageName(boolean simplifiedPackageName) {
            configurable.addConfig(ILogConfig.SIMPLIFIED_PACKAGE_NAME, String.valueOf(simplifiedPackageName));
            return this;
        }

        public Builder formatPaddedOutput(boolean formatPaddedOutput) {
            configurable.addConfig(ILogConfig.FORMAT_PADDED_OUTPUT, String.valueOf(formatPaddedOutput));
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
