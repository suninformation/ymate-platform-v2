/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.core.support;

import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.event.IEventProvider;
import net.ymate.platform.core.i18n.II18NEventHandler;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/18 下午2:19
 * @version 1.0
 */
public class ConfigBuilder {

    private boolean __isDevelopMode;

    private List<String> __packageNames;

    private List<String> __excludeModules;

    private Locale __locale;

    private II18NEventHandler __i18nEventHandler;

    private Map<String, String> __paramsMap;

    private Map<String, Map<String, String>> __moduleCfgs;

    private Map<String, String> __eventConfigs;

    private IModuleCfgProcessor __processor;

    private static List<String> __doParserArrayStr(Properties properties, String key) {
        String[] _strArr = StringUtils.split(properties.getProperty(key), "|");
        if (_strArr != null) {
            return new ArrayList<String>(Arrays.asList(_strArr));
        }
        return Collections.emptyList();
    }

    public static ConfigBuilder system() {
        final Properties __props = new Properties();
        InputStream _in = null;
        try {
            ClassLoader _classLoader = ConfigBuilder.class.getClassLoader();
            if (RuntimeUtils.isWindows()) {
                _in = _classLoader.getResourceAsStream("ymp-conf_WIN.properties");
            } else if (RuntimeUtils.isUnixOrLinux()) {
                _in = _classLoader.getResourceAsStream("ymp-conf_UNIX.properties");
            }
            //
            if (_in == null) {
                _in = _classLoader.getResourceAsStream("ymp-conf.properties");
            }
            if (_in != null) {
                __props.load(_in);
            }
            //
            IModuleCfgProcessor _processor = new IModuleCfgProcessor() {
                @Override
                public Map<String, String> getModuleCfg(String moduleName) {
                    Map<String, String> _cfgsMap = new HashMap<String, String>();
                    // 提取模块配置
                    for (Object _key : __props.keySet()) {
                        String _prefix = "ymp.configs." + moduleName + ".";
                        if (StringUtils.startsWith((String) _key, _prefix)) {
                            String _cfgKey = StringUtils.substring((String) _key, _prefix.length());
                            String _cfgValue = __props.getProperty((String) _key);
                            _cfgsMap.put(_cfgKey, _cfgValue);
                        }
                    }
                    return _cfgsMap;
                }
            };
            //
            ConfigBuilder _builder = ConfigBuilder.create(_processor)
                    .developMode(new BlurObject(__props.getProperty("ymp.dev_mode")).toBooleanValue())
                    .packageNames(__doParserArrayStr(__props, "ymp.autoscan_packages"))
                    .excludeModules(__doParserArrayStr(__props, "ymp.excluded_modules"))
                    .locale(StringUtils.trimToNull(__props.getProperty("ymp.i18n_default_locale")))
                    .i18nEventHandler(ClassUtils.impl(__props.getProperty("ymp.i18n_event_handler_class"), II18NEventHandler.class, ConfigBuilder.class));
            // 提取模块配置
            String _prefix = "ymp.params.";
            for (Object _key : __props.keySet()) {
                if (StringUtils.startsWith((String) _key, _prefix)) {
                    String _cfgKey = StringUtils.substring((String) _key, _prefix.length());
                    String _cfgValue = __props.getProperty((String) _key);
                    _builder.param(_cfgKey, _cfgValue);
                }
            }
            //
            _prefix = "ymp.event.";
            for (Object _key : __props.keySet()) {
                if (StringUtils.startsWith((String) _key, _prefix)) {
                    String _cfgKey = StringUtils.substring((String) _key, _prefix.length());
                    String _cfgValue = __props.getProperty((String) _key);
                    _builder.__eventConfigs.put(_cfgKey, _cfgValue);
                }
            }
            //
            return _builder;
        } catch (Exception e) {
            throw new RuntimeException(RuntimeUtils.unwrapThrow(e));
        } finally {
            try {
                if (_in != null) _in.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static ConfigBuilder create() {
        return new ConfigBuilder(new IModuleCfgProcessor() {
            public Map<String, String> getModuleCfg(String moduleName) {
                return Collections.emptyMap();
            }
        });
    }

    public static ConfigBuilder create(IModuleCfgProcessor processor) {
        return new ConfigBuilder(processor);
    }

    private ConfigBuilder(IModuleCfgProcessor processor) {
        __packageNames = new ArrayList<String>();
        __excludeModules = new ArrayList<String>();
        __paramsMap = new HashMap<String, String>();
        __moduleCfgs = new HashMap<String, Map<String, String>>();
        __eventConfigs = new HashMap<String, String>();
        //
        __processor = processor;
    }

    public ConfigBuilder developMode(boolean isDevelopMode) {
        __isDevelopMode = isDevelopMode;
        return this;
    }

    public ConfigBuilder packageNames(Collection<String> packageNames) {
        __packageNames.addAll(packageNames);
        return this;
    }

    public ConfigBuilder packageName(String packageName) {
        __packageNames.add(packageName);
        return this;
    }

    public ConfigBuilder excludeModules(Collection<String> excludeModules) {
        __excludeModules.addAll(excludeModules);
        return this;
    }

    public ConfigBuilder excludeModule(String excludeModule) {
        __excludeModules.add(excludeModule);
        return this;
    }

    public ConfigBuilder locale(Locale locale) {
        __locale = locale;
        return this;
    }

    public ConfigBuilder locale(String locale) {
        __locale = LocaleUtils.toLocale(locale);
        return this;
    }

    public ConfigBuilder i18nEventHandler(II18NEventHandler i18NEventHandler) {
        __i18nEventHandler = i18NEventHandler;
        return this;
    }

    public ConfigBuilder params(Map<String, String> params) {
        __paramsMap.putAll(params);
        return this;
    }

    public ConfigBuilder param(String paramName, String paramValue) {
        __paramsMap.put(paramName, paramValue);
        return this;
    }

    public ConfigBuilder eventMode(boolean async) {
        __eventConfigs.put("default_mode", async ? "ASYNC" : "NORMAL");
        return this;
    }

    public ConfigBuilder eventProviderClass(Class<? extends IEventProvider> providerClass) {
        __eventConfigs.put("provider_class", providerClass.getName());
        return this;
    }

    public ConfigBuilder eventThreadPoolSize(int threadPoolSize) {
        __eventConfigs.put("thread_pool_size", threadPoolSize + "");
        return this;
    }

    public ConfigBuilder eventParams(Map<String, String> params) {
        for (Map.Entry<String, String> _param : params.entrySet()) {
            __eventConfigs.put("params." + _param.getKey(), _param.getValue());
        }
        return this;
    }

    public ConfigBuilder eventParam(String paramName, String paramValue) {
        __eventConfigs.put("params." + paramName, paramValue);
        return this;
    }

    public IConfig build() {
        return new IConfig() {

            public boolean isDevelopMode() {
                return __isDevelopMode;
            }

            public List<String> getAutoscanPackages() {
                return Collections.unmodifiableList(__packageNames);
            }

            public List<String> getExcludedModules() {
                return Collections.unmodifiableList(__excludeModules);
            }

            public Locale getDefaultLocale() {
                return __locale != null ? __locale : Locale.getDefault();
            }

            public II18NEventHandler getI18NEventHandlerClass() {
                return __i18nEventHandler;
            }

            public Map<String, String> getParams() {
                return Collections.unmodifiableMap(__paramsMap);
            }

            public String getParam(String name) {
                return __paramsMap.get(name);
            }

            public Map<String, String> getModuleConfigs(String moduleName) {
                Map<String, String> _cfgsMap = __moduleCfgs.get(moduleName);
                if (_cfgsMap == null) {
                    _cfgsMap = Collections.unmodifiableMap(__processor.getModuleCfg(moduleName));
                    __moduleCfgs.put(moduleName, _cfgsMap);
                }
                return _cfgsMap;
            }

            public Map<String, String> getEventConfigs() {
                return Collections.unmodifiableMap(__eventConfigs);
            }
        };
    }
}
