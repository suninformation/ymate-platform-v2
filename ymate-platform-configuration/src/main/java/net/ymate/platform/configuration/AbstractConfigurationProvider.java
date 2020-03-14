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
package net.ymate.platform.configuration;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.core.configuration.IConfigFileParser;
import net.ymate.platform.core.configuration.IConfigurationProvider;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/7/31 上午12:39
 */
public abstract class AbstractConfigurationProvider implements IConfigurationProvider {

    private static final Log LOG = LogFactory.getLog(AbstractConfigurationProvider.class);

    /**
     * 配置对象缓存，对于重复的文件加载会使用缓存，减少文件读写频率
     */
    private static final Map<String, IConfigFileParser> CONFIG_CACHE_MAPS = new ConcurrentHashMap<>();

    private static final ReentrantLockHelper LOCK = new ReentrantLockHelper();

    /**
     * 配置对象
     */
    private IConfigFileParser configFileParser;

    /**
     * 装载配置文件参数
     */
    private String cfgFileName;

    @Override
    public void load(String cfgFileName) throws Exception {
        if (StringUtils.isBlank(cfgFileName)) {
            throw new NullArgumentException("cfgFileName");
        }
        this.cfgFileName = cfgFileName;
        //
        ReentrantLock locker = LOCK.getLocker(this.cfgFileName);
        locker.lock();
        try {
            doLoad(false);
        } finally {
            locker.unlock();
        }
    }

    /**
     * 执行配置文件加载
     *
     * @param update 是否为重新加载
     * @throws Exception 可能产生的任何异常
     */
    private void doLoad(boolean update) throws Exception {
        if (update || !CONFIG_CACHE_MAPS.containsKey(cfgFileName)) {
            configFileParser = buildConfigFileParser(FileUtils.toURL(cfgFileName)).load(true);
            CONFIG_CACHE_MAPS.put(cfgFileName, configFileParser);
            if (update && LOG.isInfoEnabled()) {
                LOG.info(String.format("Configuration file [%s] reloaded.", cfgFileName));
            }
        } else {
            configFileParser = CONFIG_CACHE_MAPS.get(cfgFileName);
        }
    }

    /**
     * 构建配置文件分析器对象
     *
     * @param cfgFileName 配置文件URL路径
     * @return 返回配置文件分析器对象
     * @throws Exception 可能产生的任何异常
     */
    protected abstract IConfigFileParser buildConfigFileParser(URL cfgFileName) throws Exception;

    @Override
    public IConfigFileParser getConfigFileParser() {
        return configFileParser;
    }

    @Override
    public void reload() throws Exception {
        ReentrantLock locker = LOCK.getLocker(cfgFileName);
        locker.lock();
        try {
            // 加载配置
            doLoad(true);
        } finally {
            locker.unlock();
        }
    }

    @Override
    public String getCfgFileName() {
        return cfgFileName;
    }

    @Override
    public String getString(String key) {
        IConfigFileParser.Property prop = configFileParser.getDefaultCategory().getProperty(key);
        return prop == null ? null : prop.getContent();
    }

    @Override
    public String getString(String key, String defaultValue) {
        return StringUtils.defaultIfBlank(getString(key), defaultValue);
    }

    @Override
    public String getString(String category, String key, String defaultValue) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        if (categoryObj == null) {
            return defaultValue;
        }
        IConfigFileParser.Property prop = categoryObj.getProperty(key);
        return StringUtils.defaultIfBlank(prop == null ? null : prop.getContent(), defaultValue);
    }

    @Override
    public List<String> getList(String key) {
        return getList(IConfigFileParser.DEFAULT_CATEGORY_NAME, key);
    }

    @Override
    public List<String> getList(String category, String key) {
        List<String> returnValue = new ArrayList<>();
        IConfigFileParser.Property prop = configFileParser.getCategory(category).getProperty(key);
        if (prop != null) {
            prop.getAttributeMap().values().stream().filter((attr) -> (StringUtils.isBlank(attr.getValue())))
                    .forEachOrdered((attr) -> returnValue.add(attr.getKey()));
        }
        return returnValue;
    }

    @Override
    public Map<String, String> getMap(String key) {
        return getMap(IConfigFileParser.DEFAULT_CATEGORY_NAME, key);
    }

    @Override
    public Map<String, String> getMap(String category, String key) {
        Map<String, String> returnValue = new LinkedHashMap<>();
        IConfigFileParser.Property prop = configFileParser.getCategory(category).getProperty(key);
        if (prop != null) {
            prop.getAttributeMap().values().stream().filter((attr) -> (StringUtils.isNotBlank(attr.getValue())))
                    .forEachOrdered((attr) -> returnValue.put(attr.getKey(), attr.getValue()));
        }
        return returnValue;
    }

    @Override
    public String[] getArray(String key) {
        List<String> resultValue = getList(key);
        return resultValue.toArray(new String[0]);
    }

    @Override
    public String[] getArray(String key, String[] defaultValue) {
        List<String> resultValue = getList(key);
        return resultValue.isEmpty() ? defaultValue : resultValue.toArray(new String[0]);
    }

    @Override
    public String[] getArray(String key, boolean zeroSize) {
        return getArray(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, zeroSize);
    }

    @Override
    public String[] getArray(String category, String key, boolean zeroSize) {
        List<String> values = getList(category, key);
        if (values.isEmpty() && !zeroSize) {
            return null;
        }
        return values.toArray(new String[0]);
    }

    @Override
    public int getInt(String key) {
        return getInt(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return getInt(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public int getInt(String category, String key, int defaultValue) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        if (categoryObj != null) {
            IConfigFileParser.Property prop = categoryObj.getProperty(key);
            if (prop != null) {
                return new BlurObject(prop.getContent()).toIntValue();
            }
        }
        return defaultValue;
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public boolean getBoolean(String category, String key, boolean defaultValue) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        if (categoryObj != null) {
            IConfigFileParser.Property prop = categoryObj.getProperty(key);
            if (prop != null) {
                return new BlurObject(prop.getContent()).toBooleanValue();
            }
        }
        return defaultValue;
    }

    @Override
    public long getLong(String key) {
        return getLong(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0L);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return getLong(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public long getLong(String category, String key, long defaultValue) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        if (categoryObj != null) {
            IConfigFileParser.Property prop = categoryObj.getProperty(key);
            if (prop != null) {
                return new BlurObject(prop.getContent()).toLongValue();
            }
        }
        return defaultValue;
    }

    @Override
    public float getFloat(String key) {
        return getFloat(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0f);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return getFloat(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public float getFloat(String category, String key, float defaultValue) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        if (categoryObj != null) {
            IConfigFileParser.Property prop = categoryObj.getProperty(key);
            if (prop != null) {
                return new BlurObject(prop.getContent()).toFloatValue();
            }
        }
        return defaultValue;
    }

    @Override
    public double getDouble(String key) {
        return getDouble(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0d);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return getDouble(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public <T> T getClassImpl(String key, Class<T> interfaceClass) {
        return ClassUtils.impl(getString(key), interfaceClass, getClass());
    }

    @Override
    public <T> T getClassImpl(String key, String defaultValue, Class<T> interfaceClass) {
        return ClassUtils.impl(getString(key, defaultValue), interfaceClass, getClass());
    }

    @Override
    public <T> T getClassImpl(String category, String key, String defaultValue, Class<T> interfaceClass) {
        return ClassUtils.impl(getString(category, key, defaultValue), interfaceClass, getClass());
    }

    @Override
    public double getDouble(String category, String key, double defaultValue) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        if (categoryObj != null) {
            IConfigFileParser.Property prop = categoryObj.getProperty(key);
            if (prop != null) {
                return new BlurObject(prop.getContent()).toDoubleValue();
            }
        }
        return defaultValue;
    }

    @Override
    public Map<String, String> toMap() {
        return toMap(IConfigFileParser.DEFAULT_CATEGORY_NAME);
    }

    @Override
    public Map<String, String> toMap(String category) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        if (categoryObj == null) {
            return Collections.emptyMap();
        }
        Collection<IConfigFileParser.Property> properties = categoryObj.getPropertyMap().values();
        Map<String, String> returnValue = new LinkedHashMap<>(properties.size());
        properties.stream().peek((prop) -> returnValue.put(prop.getName(), prop.getContent()))
                .forEachOrdered((prop) -> prop.getAttributeMap().values()
                        .forEach((attr) -> returnValue.put(prop.getName().concat(".").concat(attr.getKey()), attr.getValue())));
        return returnValue;
    }

    @Override
    public List<String> getCategoryNames() {
        return new ArrayList<>(configFileParser.getCategories().keySet());
    }

    @Override
    public boolean contains(String key) {
        return configFileParser.getDefaultCategory().getProperty(key) != null;
    }

    @Override
    public boolean contains(String category, String key) {
        IConfigFileParser.Category categoryObj = configFileParser.getCategory(category);
        return categoryObj != null && categoryObj.getProperty(key) != null;
    }
}
