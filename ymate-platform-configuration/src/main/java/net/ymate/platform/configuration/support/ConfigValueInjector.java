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
package net.ymate.platform.configuration.support;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.configuration.Cfgs;
import net.ymate.platform.configuration.annotation.ConfigValue;
import net.ymate.platform.configuration.annotation.Configs;
import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanInjector;
import net.ymate.platform.core.configuration.IConfigFileParser;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.IConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-04-25 13:52
 */
public class ConfigValueInjector implements IBeanInjector {

    @Override
    public Object inject(IBeanFactory beanFactory, Annotation annotation, Class<?> targetClass, Field field, Object originInject) {
        if (ClassUtils.isInterfaceOf(targetClass, IConfiguration.class)) {
            return originInject;
        }
        ConfigValue configValueAnn = ((ConfigValue) annotation);
        String categoryStr = configValueAnn.category();
        String keyStr = StringUtils.defaultIfBlank(configValueAnn.value(), field.getName());
        Set<IConfiguration> configs = new LinkedHashSet<>();
        parseConfigurations(beanFactory, configValueAnn.configs(), configs);
        if (configs.isEmpty()) {
            parseConfigurations(configValueAnn.files(), configs);
        }
        if (configs.isEmpty() || StringUtils.isBlank(categoryStr)) {
            Configs configsAnn = targetClass.getAnnotation(Configs.class);
            if (configsAnn != null) {
                parseConfigurations(beanFactory, configsAnn.value(), configs);
                if (configs.isEmpty()) {
                    parseConfigurations(configsAnn.files(), configs);
                }
                if (StringUtils.isBlank(categoryStr)) {
                    categoryStr = configsAnn.category();
                }
            }
            if (configs.isEmpty() || StringUtils.isBlank(categoryStr)) {
                configsAnn = ClassUtils.getPackageAnnotation(targetClass, Configs.class);
                if (configsAnn != null) {
                    parseConfigurations(beanFactory, configsAnn.value(), configs);
                    if (configs.isEmpty()) {
                        parseConfigurations(configsAnn.files(), configs);
                    }
                    if (StringUtils.isBlank(categoryStr)) {
                        categoryStr = configsAnn.category();
                    }
                }
            }
        }
        Object returnValue = null;
        categoryStr = StringUtils.defaultIfBlank(categoryStr, IConfigFileParser.DEFAULT_CATEGORY_NAME);
        for (IConfiguration configuration : configs) {
            returnValue = parseParam(configuration, field, categoryStr, keyStr);
            if (returnValue != null) {
                break;
            }
        }
        if (returnValue == null) {
            if (!StringUtils.equalsIgnoreCase(IConfigFileParser.DEFAULT_CATEGORY_NAME, categoryStr)) {
                keyStr = categoryStr.concat(".").concat(keyStr);
            }
            returnValue = parseParam(beanFactory, field, keyStr, configValueAnn.defaultValue());
        }
        return returnValue;
    }

    private void parseConfigurations(IBeanFactory beanFactory, Class<? extends IConfiguration>[] configurationClasses, Set<IConfiguration> configs) {
        if (ArrayUtils.isNotEmpty(configurationClasses)) {
            for (Class<? extends IConfiguration> configurationClass : configurationClasses) {
                IConfiguration configuration = beanFactory.getBean(configurationClass);
                if (configuration != null) {
                    configs.add(configuration);
                }
            }
        }
    }

    private void parseConfigurations(String[] files, Set<IConfiguration> configs) {
        if (ArrayUtils.isNotEmpty(files)) {
            for (String file : files) {
                IConfiguration configuration = Cfgs.get().loadCfg(file, true);
                if (configuration != null) {
                    configs.add(configuration);
                }
            }
        }
    }

    private Object parseParam(IConfiguration configuration, Field field, String categoryStr, String keyStr) {
        Object returnValue;
        Class<?> fieldType;
        boolean isArray = field.getType().isArray();
        if (isArray) {
            fieldType = ClassUtils.getArrayClassType(field.getType());
            String[] valueArray = configuration.getArray(categoryStr, keyStr, false);
            returnValue = parseArrayValue(valueArray, fieldType);
        } else {
            fieldType = field.getType();
            if (List.class.equals(fieldType)) {
                returnValue = parseListValue(configuration.getList(categoryStr, keyStr), null);
            } else if (Map.class.equals(fieldType)) {
                returnValue = parseMapValue(configuration.getMap(categoryStr, keyStr), null);
            } else {
                returnValue = BlurObject.bind(configuration.getString(categoryStr, keyStr)).toObjectValue(field.getType());
            }
        }
        return returnValue;
    }

    private Object parseParam(IBeanFactory beanFactory, Field field, String keyStr, String defaultValue) {
        IConfigReader configReader = beanFactory.getOwner().getParamConfigReader();
        Object returnValue;
        Class<?> fieldType;
        boolean isArray = field.getType().isArray();
        if (isArray) {
            fieldType = ClassUtils.getArrayClassType(field.getType());
            String[] valueArray = configReader.getArray(keyStr, StringUtils.split(defaultValue, "|"));
            returnValue = parseArrayValue(valueArray, fieldType);
        } else {
            fieldType = field.getType();
            if (List.class.equals(fieldType)) {
                returnValue = parseListValue(configReader.getList(keyStr), defaultValue);
            } else if (Map.class.equals(fieldType)) {
                returnValue = parseMapValue(configReader.getMap(keyStr), defaultValue);
            } else {
                returnValue = BlurObject.bind(configReader.getString(keyStr, defaultValue)).toObjectValue(field.getType());
            }
        }
        return returnValue;
    }

    private Object[] parseArrayValue(String[] valueArray, Class<?> fieldType) {
        if (valueArray != null) {
            if (Objects.equals(fieldType, String.class)) {
                return valueArray;
            } else {
                Object[] newArray = (Object[]) Array.newInstance(fieldType, valueArray.length);
                IntStream.range(0, valueArray.length).forEach(idx -> newArray[idx] = BlurObject.bind(valueArray[idx]).toObjectValue(fieldType));
                return newArray;
            }
        }
        return null;
    }

    private List<String> parseListValue(List<String> valueList, String defaultValue) {
        if (valueList == null || valueList.isEmpty()) {
            if (StringUtils.isBlank(defaultValue)) {
                return null;
            }
            return Arrays.asList(StringUtils.split(StringUtils.trimToEmpty(defaultValue), "|"));
        }
        return valueList;
    }

    private Map<String, String> parseMapValue(Map<String, String> valueMap, String defaultValue) {
        if (valueMap == null || valueMap.isEmpty()) {
            if (StringUtils.isBlank(defaultValue)) {
                return null;
            }
            String[] mapItems = StringUtils.split(StringUtils.trimToEmpty(defaultValue), "|");
            if (ArrayUtils.isNotEmpty(mapItems)) {
                if (valueMap == null) {
                    valueMap = new HashMap<>(mapItems.length);
                }
                for (String mapItem : mapItems) {
                    String[] itemArray = StringUtils.split(mapItem, ":");
                    if (itemArray != null && itemArray.length == 2) {
                        valueMap.put(itemArray[0], itemArray[1]);
                    }
                }
                return valueMap;
            }
            return Collections.emptyMap();
        }
        return valueMap;
    }
}
