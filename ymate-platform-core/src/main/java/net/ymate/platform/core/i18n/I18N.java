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
package net.ymate.platform.core.i18n;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.support.IDestroyable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 国际化资源管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-14 下午1:36:11
 */
public final class I18N implements IDestroyable {

    private static final Log LOG = LogFactory.getLog(I18N.class);

    private static final Map<Locale, Map<String, Properties>> RESOURCES_CACHES = new ConcurrentHashMap<>();

    private Locale defaultLocale;

    private II18nEventHandler eventHandler;

    private final ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>() {
        @Override
        protected Locale initialValue() {
            Locale locale = null;
            if (eventHandler != null) {
                locale = eventHandler.onLocale();
            }
            return locale == null ? defaultLocale : locale;
        }
    };

    private boolean initialized;

    /**
     * 默认构造
     */
    public I18N() {
    }

    /**
     * 构造
     *
     * @param defaultLocale 默认语言，若为空则采用JVM默认语言
     * @param eventHandler  事件监听处理器
     */
    public I18N(Locale defaultLocale, II18nEventHandler eventHandler) {
        this.defaultLocale = defaultLocale;
        this.eventHandler = eventHandler;
    }

    /**
     * 初始化
     */
    public void initialize() {
        if (!initialized) {
            this.defaultLocale = defaultLocale == null ? Locale.getDefault() : defaultLocale;
            //
            initialized = true;
        }
    }

    /**
     * @return 判断是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * 获取当前本地线程语言，若为空则返回默认
     *
     * @return 返回Locale对象
     */
    public Locale current() {
        return currentLocale.get();
    }

    /**
     * 修改当前线程语言设置，不触发事件
     *
     * @param locale 预设置语言
     * @return 返回修改结果
     */
    public boolean current(Locale locale) {
        if (locale != null && !locale.equals(currentLocale.get())) {
            currentLocale.set(locale);
            return true;
        }
        return false;
    }

    /**
     * 修改当前线程语言设置并触发onLocaleChanged事件
     *
     * @param locale 预设置语言
     */
    public void change(Locale locale) {
        if (current(locale)) {
            if (eventHandler != null) {
                eventHandler.onChanged(locale);
            }
        }
    }

    /**
     * @param resourceName 资源名称
     * @param key          键值
     * @return 加载资源并提取key指定的值
     */
    public String load(String resourceName, String key) {
        return load(resourceName, key, StringUtils.EMPTY);
    }

    /**
     * 加载资源并提取key指定的值
     *
     * @param resourceName 资源名称
     * @param key          键值
     * @param defaultValue 默认值
     * @return 返回Key值
     */
    public String load(String resourceName, String key, String defaultValue) {
        Locale local = current();
        Map<String, Properties> cache = RESOURCES_CACHES.get(local);
        Properties prop = cache != null ? cache.get(resourceName) : null;
        if (prop == null && eventHandler != null) {
            try {
                List<String> resourceNames = getResourceNames(local, resourceName);
                for (String resName : resourceNames) {
                    try (InputStream inputStream = eventHandler.onLoad(resName)) {
                        if (inputStream != null) {
                            prop = new Properties();
                            prop.load(inputStream);
                            break;
                        }
                    }
                }
                if (prop != null && !prop.isEmpty()) {
                    if (cache == null) {
                        cache = ReentrantLockHelper.putIfAbsentAsync(RESOURCES_CACHES, local, () -> new ConcurrentHashMap<>(16));
                    }
                    cache.put(resourceName, prop);
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        String returnValue = null;
        if (prop != null) {
            returnValue = prop.getProperty(key, defaultValue);
        } else {
            try {
                returnValue = ResourceBundle.getBundle(resourceName, local).getString(key);
            } catch (Exception ignored) {
            }
        }
        return StringUtils.defaultIfBlank(returnValue, defaultValue);
    }

    /**
     * @param resourceName 资源名称
     * @param key          资源键名
     * @param defaultValue 默认值
     * @param args         参数集合
     * @return 格式化消息字符串与参数绑定
     */
    public String formatMessage(String resourceName, String key, String defaultValue, Object... args) {
        String msg = load(resourceName, key, defaultValue);
        if (StringUtils.isNotBlank(msg)) {
            if (ArrayUtils.isNotEmpty(args)) {
                return formatMsg(msg, args);
            }
        }
        return msg;
    }

    public String formatMsg(String message, Object... args) {
        if (StringUtils.isNotBlank(message) && ArrayUtils.isNotEmpty(args)) {
            return MessageFormat.format(message, args);
        }
        return message;
    }

    /**
     * @param locale       指定语言
     * @param resourceName 资源名称
     * @return 拼装资源文件名称集合
     */
    private List<String> getResourceNames(Locale locale, String resourceName) {
        List<String> names = new ArrayList<>();
        names.add(resourceName + ".properties");
        String localeKey = (locale == null) ? StringUtils.EMPTY : locale.toString();
        if (localeKey.length() > 0) {
            resourceName += ("_" + localeKey) + ".properties";
            names.add(0, resourceName);
        }
        return names;
    }

    /**
     * 销毁
     */
    @Override
    public void close() {
        if (initialized) {
            initialized = false;
            //
            defaultLocale = null;
            eventHandler = null;
        }
    }
}
