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
package net.ymate.platform.core.impl;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureParser;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 18:48
 * @since 2.1.0
 */
public class DefaultApplicationConfigureParser implements IApplicationConfigureParser {

    private static final Log LOG = LogFactory.getLog(DefaultApplicationConfigureParser.class);

    private static final String CONFIG_FILE_PREFIX = "ymp-conf";

    private static final String MODULE_CONFIG_PREFIX = "ymp.configs.";

    private static final Map<String, IModuleConfigurer> CONFIGURERS_CACHE = new ConcurrentHashMap<>();

    private final IConfigReader configReader;

    public static IApplicationConfigureParser defaultEmpty() {
        return new DefaultApplicationConfigureParser(Collections.emptyMap());
    }

    public static IApplicationConfigureParser systemDefault() {
        final Properties properties = new Properties();
        try (InputStream inputStream = loadSystemConfig()) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return new DefaultApplicationConfigureParser(properties);
    }

    private static InputStream loadSystemConfig() {
        String configFileName = System.getProperty(IApplication.SYSTEM_CONFIG_FILE);
        if (StringUtils.isNotBlank(configFileName) && StringUtils.equalsIgnoreCase(FileUtils.getExtName(configFileName), FileUtils.FILE_SUFFIX_PROPERTIES)) {
            configFileName = RuntimeUtils.replaceEnvVariable(configFileName);
            File configFile = new File(configFileName);
            if (configFile.isAbsolute() && configFile.exists() && configFile.isFile()) {
                try {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Found and load the configuration file: %s", configFile.getPath()));
                    }
                    return new FileInputStream(configFile);
                } catch (FileNotFoundException ignored) {
                }
            }
        }
        IApplication.Environment runEnv = YMP.getPriorityRunEnv(IApplication.Environment.DEV);
        String prefix = StringUtils.EMPTY;
        if (runEnv != IApplication.Environment.UNKNOWN) {
            prefix = "_" + runEnv.name();
        }
        configFileName = CONFIG_FILE_PREFIX + prefix;
        //
        List<String> filePaths = new ArrayList<>();
        filePaths.add(String.format("%s.properties", configFileName));
        if (RuntimeUtils.isWindows()) {
            filePaths.add(String.format("%s_WIN.properties", configFileName));
        } else if (RuntimeUtils.isUnixOrLinux()) {
            filePaths.add(String.format("%s_UNIX.properties", configFileName));
        }
        filePaths.add(String.format("%s.properties", CONFIG_FILE_PREFIX));
        return loadSystemConfigAsStream(filePaths.toArray(new String[0]));
    }

    private static InputStream loadSystemConfigAsStream(String... filePaths) {
        InputStream inputStream = null;
        if (filePaths != null && filePaths.length > 0) {
            ClassLoader classLoader = DefaultApplicationConfigureParser.class.getClassLoader();
            for (String filePath : filePaths) {
                if (StringUtils.isNotBlank(filePath)) {
                    URL url = classLoader.getResource(filePath);
                    if (url != null) {
                        try {
                            inputStream = url.openStream();
                            if (LOG.isInfoEnabled()) {
                                LOG.info(String.format("Found and load the configuration file: %s", url));
                            }
                            break;
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }
        return inputStream;
    }

    public DefaultApplicationConfigureParser(Map<?, ?> configData) {
        if (configData == null) {
            throw new NullArgumentException("configData");
        }
        this.configReader = MapSafeConfigReader.bind(configData);
    }

    public DefaultApplicationConfigureParser(Properties configData) {
        this((Map<?, ?>) configData);
    }

    @Override
    public IConfigReader getConfigReader() {
        return configReader;
    }

    @Override
    public IModuleConfigurer getModuleConfigurer(String moduleName) {
        if (StringUtils.isNotBlank(moduleName) && !configReader.toMap().isEmpty()) {
            try {
                return ReentrantLockHelper.putIfAbsentAsync(CONFIGURERS_CACHE, moduleName, () -> {
                    Map<String, String> configs = new HashMap<>(16);
                    String prefix = MODULE_CONFIG_PREFIX + moduleName + ".";
                    configReader.toMap().keySet().forEach(key -> {
                        String keyStr = BlurObject.bind(key).toStringValue();
                        if (StringUtils.startsWith(keyStr, prefix)) {
                            configs.put(StringUtils.substring(keyStr, prefix.length()), configReader.getString(key));
                        }
                    });
                    return new DefaultModuleConfigurer(moduleName, MapSafeConfigReader.bind(configs));
                });
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return null;
    }
}
