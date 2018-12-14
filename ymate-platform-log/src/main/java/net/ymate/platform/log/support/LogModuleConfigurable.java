/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.log.support;

import net.ymate.platform.core.support.IModuleConfigurable;
import net.ymate.platform.log.ILog;
import net.ymate.platform.log.ILogModuleCfg;
import net.ymate.platform.log.ILogger;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 11:25
 * @version 1.0
 * @since 2.0.6
 */
public class LogModuleConfigurable implements IModuleConfigurable {

    public static LogModuleConfigurable create() {
        return new LogModuleConfigurable();
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    public LogModuleConfigurable configFile(String configFile) {
        __configs.put(ILogModuleCfg.CONFIG_FILE, StringUtils.trimToEmpty(configFile));
        return this;
    }

    public LogModuleConfigurable outputDir(String outputDir) {
        __configs.put(ILogModuleCfg.OUTPUT_DIR, StringUtils.trimToEmpty(outputDir));
        return this;
    }

    public LogModuleConfigurable loggerName(String loggerName) {
        __configs.put(ILogModuleCfg.LOGGER_NAME, StringUtils.trimToEmpty(loggerName));
        return this;
    }

    public LogModuleConfigurable loggerClass(Class<? extends ILogger> loggerClass) {
        __configs.put(ILogModuleCfg.LOGGER_CLASS, loggerClass.getName());
        return this;
    }

    public LogModuleConfigurable allowOutputConsole(boolean allowOutputConsole) {
        __configs.put(ILogModuleCfg.ALLOW_OUTPUT_CONSOLE, String.valueOf(allowOutputConsole));
        return this;
    }

    @Override
    public String getModuleName() {
        return ILog.MODULE_NAME;
    }

    @Override
    public Map<String, String> toMap() {
        return __configs;
    }
}
