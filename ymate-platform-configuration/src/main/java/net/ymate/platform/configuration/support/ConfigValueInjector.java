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
import net.ymate.platform.configuration.annotation.ConfigValue;
import net.ymate.platform.configuration.annotation.Configs;
import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanInjector;
import net.ymate.platform.core.configuration.IConfigFileParser;
import net.ymate.platform.core.configuration.IConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

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
        String valueStr = null;
        String keyStr = StringUtils.defaultIfBlank(configValueAnn.value(), field.getName());
        Class<? extends IConfiguration>[] configs = configValueAnn.configs();
        if (ArrayUtils.isEmpty(configs) || StringUtils.isBlank(categoryStr)) {
            Configs configsAnn = targetClass.getAnnotation(Configs.class);
            if (configsAnn != null) {
                if (ArrayUtils.isEmpty(configs)) {
                    configs = configsAnn.value();
                }
                if (StringUtils.isBlank(categoryStr)) {
                    categoryStr = configsAnn.category();
                }
            }
            if (ArrayUtils.isEmpty(configs) || StringUtils.isBlank(categoryStr)) {
                configsAnn = ClassUtils.getPackageAnnotation(targetClass, Configs.class);
                if (configsAnn != null) {
                    if (ArrayUtils.isEmpty(configs)) {
                        configs = configsAnn.value();
                    }
                    if (StringUtils.isBlank(categoryStr)) {
                        categoryStr = configsAnn.category();
                    }
                }
            }
        }
        categoryStr = StringUtils.defaultIfBlank(categoryStr, IConfigFileParser.DEFAULT_CATEGORY_NAME);
        if (ArrayUtils.isNotEmpty(configs)) {
            for (Class<? extends IConfiguration> configurationClass : configs) {
                IConfiguration configuration = beanFactory.getBean(configurationClass);
                if (configuration != null) {
                    valueStr = configuration.getString(categoryStr, keyStr, null);
                    if (StringUtils.isNotBlank(valueStr)) {
                        break;
                    }
                }
            }
        }
        if (StringUtils.isBlank(valueStr)) {
            if (!StringUtils.equalsIgnoreCase(IConfigFileParser.DEFAULT_CATEGORY_NAME, categoryStr)) {
                keyStr = categoryStr.concat(".").concat(keyStr);
            }
            valueStr = beanFactory.getOwner().getParam(keyStr);
        }
        return BlurObject.bind(StringUtils.defaultIfBlank(valueStr, configValueAnn.defaultValue())).toObjectValue(field.getType());
    }
}
