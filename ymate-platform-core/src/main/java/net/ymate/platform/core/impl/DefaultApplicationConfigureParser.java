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

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.ResourceUtils;
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
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 18:48
 * @since 2.1.0
 */
public final class DefaultApplicationConfigureParser implements IApplicationConfigureParser {

    private static final Log LOG = LogFactory.getLog(DefaultApplicationConfigureParser.class);

    private static final String CONFIG_FILE_PREFIX = "ymp-conf";

    private static final String MODULE_CONFIG_PREFIX = "ymp.configs.";

    private static final Map<String, IModuleConfigurer> CONFIGURERS_CACHE = new ConcurrentHashMap<>();

    private final IConfigReader configReader;

    public static IApplicationConfigureParser defaultEmpty() {
        return new DefaultApplicationConfigureParser(Collections.emptyMap());
    }

    public static IApplicationConfigureParser systemDefault() {
        Map<String, String> configData = new LinkedHashMap<>();
        String configFileName = System.getProperty(IApplication.SYSTEM_CONFIG_FILE);
        if (StringUtils.isNotBlank(configFileName) && isSupportedConfigFile(FileUtils.getExtName(configFileName))) {
            configFileName = RuntimeUtils.replaceEnvVariable(configFileName);
            File configFile = new File(configFileName);
            if (configFile.isAbsolute() && configFile.exists() && configFile.isFile()) {
                if (loadConfigFile(configData, configFile)) {
                    return new DefaultApplicationConfigureParser(configData);
                }
            }
        }
        for (List<String> layerPaths : buildConfigFilePathLayers()) {
            for (String filePath : layerPaths) {
                try (InputStream inputStream = ResourceUtils.getResourceAsStream(DefaultApplicationConfigureParser.class, filePath)) {
                    if (inputStream == null) {
                        continue;
                    }
                    configData.putAll(parseConfigFile(inputStream, FileUtils.getExtName(filePath)));
                    break;
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                    break;
                }
            }
        }
        return new DefaultApplicationConfigureParser(configData);
    }

    /**
     * 判断指定文件扩展名是否为受支持的配置文件类型（properties、yaml 或 yml）
     *
     * @param fileExtName 文件扩展名
     * @return 若受支持则返回 true
     * @since 2.1.4
     */
    private static boolean isSupportedConfigFile(String fileExtName) {
        return Strings.CI.equals(fileExtName, FileUtils.FILE_SUFFIX_PROPERTIES)
                || Strings.CI.equals(fileExtName, FileUtils.FILE_SUFFIX_YAML)
                || Strings.CI.equals(fileExtName, FileUtils.FILE_SUFFIX_YML);
    }

    /**
     * 加载指定的本地配置文件并将内容合并至 configData，根据文件扩展名选择解析方式
     *
     * @param configData 配置数据集合，加载成功后内容将被合并至此集合
     * @param configFile 配置文件对象
     * @return 若加载成功则返回 true
     * @since 2.1.4
     */
    private static boolean loadConfigFile(Map<String, String> configData, File configFile) {
        try (InputStream inputStream = Files.newInputStream(configFile.toPath())) {
            configData.putAll(parseConfigFile(inputStream, FileUtils.getExtName(configFile)));
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Found and load the configuration file: %s", configFile.getPath()));
            }
            return true;
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return false;
    }

    /**
     * 构建分层配置文件路径列表，加载顺序为：全量基础配置、操作系统特定配置、运行环境特定配置，后加载者覆盖先加载者的同名键
     * <p>
     * 每个层级内按 properties、yaml、yml 顺序排列，仅加载第一个存在的文件
     *
     * @return 返回按加载顺序排列的分层路径列表
     * @since 2.1.4
     */
    private static List<List<String>> buildConfigFilePathLayers() {
        List<List<String>> layerPaths = new ArrayList<>();
        layerPaths.add(buildLayerFilePaths(CONFIG_FILE_PREFIX));
        if (RuntimeUtils.isWindows()) {
            layerPaths.add(buildLayerFilePaths(CONFIG_FILE_PREFIX + "_WIN"));
        } else if (RuntimeUtils.isUnixOrLinux()) {
            layerPaths.add(buildLayerFilePaths(CONFIG_FILE_PREFIX + "_UNIX"));
        }
        IApplication.Environment runEnv = YMP.getPriorityRunEnv(IApplication.Environment.DEV);
        if (runEnv != IApplication.Environment.UNKNOWN) {
            layerPaths.add(buildLayerFilePaths(CONFIG_FILE_PREFIX + "_" + runEnv.name()));
        }
        return layerPaths;
    }

    /**
     * 构建同一层级的配置文件路径列表，按 properties、yaml、yml 顺序排列
     *
     * @param filePrefix 配置文件名称前缀
     * @return 返回该层级的候选文件路径列表
     * @since 2.1.4
     */
    private static List<String> buildLayerFilePaths(String filePrefix) {
        List<String> filePaths = new ArrayList<>(3);
        filePaths.add(String.format("%s.%s", filePrefix, FileUtils.FILE_SUFFIX_PROPERTIES));
        filePaths.add(String.format("%s.%s", filePrefix, FileUtils.FILE_SUFFIX_YAML));
        filePaths.add(String.format("%s.%s", filePrefix, FileUtils.FILE_SUFFIX_YML));
        return filePaths;
    }

    /**
     * 根据文件扩展名解析配置输入流并转换为键值对集合：properties 文件直接加载，yaml/yml 文件解析并扁平化处理
     *
     * @param inputStream 配置文件输入流
     * @param fileExtName 文件扩展名
     * @return 返回解析后的键值对集合，若文件格式不受支持或 SnakeYAML 类库不可用则返回空集合
     * @throws IOException 可能产生的任何异常
     * @since 2.1.4
     */
    private static Map<String, String> parseConfigFile(InputStream inputStream, String fileExtName) throws IOException {
        if (Strings.CI.equals(fileExtName, FileUtils.FILE_SUFFIX_YAML) || Strings.CI.equals(fileExtName, FileUtils.FILE_SUFFIX_YML)) {
            return YamlConfigLoader.loadAndFlatten(inputStream);
        }
        Properties properties = new Properties();
        properties.load(inputStream);
        Map<String, String> configData = new LinkedHashMap<>();
        properties.forEach((key, value) -> configData.put(BlurObject.bind(key).toStringValue(), BlurObject.bind(value).toStringValue()));
        return configData;
    }

    public DefaultApplicationConfigureParser(Map<?, ?> configData) {
        if (configData == null) {
            throw new NullArgumentException("configData");
        }
        Map<Object, Object> innerMap = new HashMap<>();
        IPasswordProcessor passwordProcessor = YMP.getPasswordProcessor();
        configData.forEach((key, value) -> {
            String keyStr = BlurObject.bind(key).toStringValue();
            String valueStr = BlurObject.bind(value).toStringValue();
            if (StringUtils.isNotBlank(valueStr)
                    && Strings.CS.startsWith(valueStr, PASS_PREFIX)
                    && Strings.CS.endsWith(valueStr, PASS_SUFFIX)) {
                String tmpValueStr = StringUtils.substringBetween(valueStr, PASS_PREFIX, PASS_SUFFIX);
                if (StringUtils.isNotBlank(tmpValueStr)) {
                    try {
                        valueStr = passwordProcessor.decrypt(tmpValueStr);
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("An exception occurred while decrypting configuration parameter '%s': ", keyStr), RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            }
            innerMap.put(keyStr, valueStr);
        });
        this.configReader = MapSafeConfigReader.bind(innerMap);
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
                    String prefix = MODULE_CONFIG_PREFIX + moduleName + ClassUtils.PACKAGE_SEPARATOR;
                    configReader.toMap().keySet().forEach(key -> {
                        String keyStr = BlurObject.bind(key).toStringValue();
                        if (Strings.CS.startsWith(keyStr, prefix)) {
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
