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
import net.ymate.platform.configuration.annotation.ConfigValue;
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
        ConfigValue configValueAnn = ((ConfigValue) annotation);
        String valueStr = null;
        String keyStr = StringUtils.defaultIfBlank(configValueAnn.value(), field.getName());
        if (ArrayUtils.isNotEmpty(configValueAnn.configs())) {
            for (Class<? extends IConfiguration> configurationClass : configValueAnn.configs()) {
                IConfiguration configuration = beanFactory.getBean(configurationClass);
                if (configuration != null) {
                    valueStr = configuration.getString(configValueAnn.category(), keyStr, null);
                    if (StringUtils.isNotBlank(valueStr)) {
                        break;
                    }
                }
            }
        }
        if (StringUtils.isBlank(valueStr)) {
            if (!StringUtils.equalsIgnoreCase(IConfigFileParser.DEFAULT_CATEGORY_NAME, configValueAnn.category())) {
                keyStr = configValueAnn.category().concat(".").concat(keyStr);
            }
            valueStr = beanFactory.getOwner().getParam(keyStr);
        }
        return BlurObject.bind(StringUtils.defaultIfBlank(valueStr, configValueAnn.defaultValue())).toObjectValue(field.getType());
    }
}
