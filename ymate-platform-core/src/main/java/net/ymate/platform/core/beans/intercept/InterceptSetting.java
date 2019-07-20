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
package net.ymate.platform.core.beans.intercept;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-14 17:42
 * @since 2.1.0
 */
public final class InterceptSetting {

    private Class<? extends IInterceptor> interceptorClass;

    private IInterceptor.SettingType type;

    public static InterceptSetting[] create(String[] settings) {
        if (ArrayUtils.isNotEmpty(settings)) {
            List<InterceptSetting> returnValue = Arrays.stream(settings).map(InterceptSetting::create).collect(Collectors.toList());
            if (!returnValue.isEmpty()) {
                return returnValue.toArray(new InterceptSetting[0]);
            }
        }
        return null;
    }

    public static InterceptSetting create(String setting) {
        return new InterceptSetting(setting);
    }

    private InterceptSetting(String setting) {
        String[] itemArr = StringUtils.split(setting, ":");
        if (itemArr != null) {
            if (itemArr.length == 1) {
                if (StringUtils.equals(itemArr[0], "*")) {
                    type = IInterceptor.SettingType.CLEAN_ALL;
                } else {
                    if (StringUtils.endsWith(itemArr[0], "-")) {
                        interceptorClass = InterceptSettings.loadInterceptorClass(StringUtils.substringBefore(itemArr[0], "-"));
                        type = IInterceptor.SettingType.REMOVE_ALL;
                    } else {
                        interceptorClass = InterceptSettings.loadInterceptorClass(StringUtils.substringBefore(itemArr[0], "+"));
                        type = IInterceptor.SettingType.ADD_ALL;
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(itemArr[0], IInterceptor.Direction.BEFORE.name())) {
                if (StringUtils.equals(itemArr[1], "*")) {
                    type = IInterceptor.SettingType.CLEAN_BEFORE;
                } else {
                    if (StringUtils.endsWith(itemArr[1], "-")) {
                        interceptorClass = InterceptSettings.loadInterceptorClass(StringUtils.substringBefore(itemArr[1], "-"));
                        type = IInterceptor.SettingType.REMOVE_BEFORE;
                    } else {
                        interceptorClass = InterceptSettings.loadInterceptorClass(StringUtils.substringBefore(itemArr[1], "+"));
                        type = IInterceptor.SettingType.ADD_BEFORE;
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(itemArr[0], IInterceptor.Direction.AFTER.name())) {
                if (StringUtils.equals(itemArr[1], "*")) {
                    type = IInterceptor.SettingType.CLEAN_AFTER;
                } else {
                    if (StringUtils.endsWith(itemArr[1], "-")) {
                        interceptorClass = InterceptSettings.loadInterceptorClass(StringUtils.substringBefore(itemArr[1], "-"));
                        type = IInterceptor.SettingType.REMOVE_AFTER;
                    } else {
                        interceptorClass = InterceptSettings.loadInterceptorClass(StringUtils.substringBefore(itemArr[1], "+"));
                        type = IInterceptor.SettingType.ADD_AFTER;
                    }
                }
            }
        }
    }

    public boolean isValid() {
        return interceptorClass != null && type != null || IInterceptor.SettingType.CLEAN_ALL.equals(type) || IInterceptor.SettingType.CLEAN_BEFORE.equals(type) || IInterceptor.SettingType.CLEAN_AFTER.equals(type);
    }

    public Class<? extends IInterceptor> getInterceptorClass() {
        return interceptorClass;
    }

    public IInterceptor.SettingType getType() {
        return type;
    }
}
