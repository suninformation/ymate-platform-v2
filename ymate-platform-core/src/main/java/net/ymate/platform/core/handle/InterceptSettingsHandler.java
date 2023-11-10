/*
 * Copyright 2007-2023 the original author or authors.
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
package net.ymate.platform.core.handle;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.beans.annotation.InterceptSettings;
import net.ymate.platform.core.beans.annotation.InterceptSettings.Item;
import net.ymate.platform.core.beans.intercept.IInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptSetting;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2023/11/9 23:21
 * @since 2.1.3
 */
public final class InterceptSettingsHandler implements IBeanHandler {

    private final IApplication owner;

    public InterceptSettingsHandler(IApplication owner) {
        this.owner = owner;
    }

    private InterceptSetting[] doInterceptSettingToArray(Item item) {
        List<InterceptSetting> interceptSettingList = new ArrayList<>();
        for (Class<? extends IInterceptor> interceptorClass : item.value()) {
            interceptSettingList.add(new InterceptSetting(item.type(), interceptorClass));
        }
        return interceptSettingList.toArray(new InterceptSetting[0]);
    }

    private Map<String, String> doParseContextParams(InterceptSettings.ContextParamSet[] contextParamSets) {
        return Arrays.stream(contextParamSets).collect(Collectors.toMap(InterceptSettings.ContextParamSet::key, InterceptSettings.ContextParamSet::value, (a, b) -> b));
    }

    @Override
    public Object handle(Class<?> targetClass) {
        InterceptSettings settingsAnn = targetClass.getAnnotation(InterceptSettings.class);
        if (settingsAnn != null) {
            Arrays.stream(settingsAnn.globals()).map(Class::getName).forEach(owner.getInterceptSettings()::registerInterceptGlobal);
            Arrays.stream(settingsAnn.packages())
                    .forEach(packageSet -> Arrays.stream(packageSet.names())
                            .forEach(name -> Arrays.stream(packageSet.value())
                                    .forEach(item -> owner.getInterceptSettings().registerInterceptPackage(name, doParseContextParams(packageSet.params()), doInterceptSettingToArray(item)))));
            Arrays.stream(settingsAnn.value())
                    .forEach(interceptSet -> Arrays.stream(interceptSet.targets())
                            .map(target -> String.format("%s#%s", target.getName(), StringUtils.trimToEmpty(interceptSet.name())))
                            .forEach(name -> Arrays.stream(interceptSet.value())
                                    .forEach(item -> owner.getInterceptSettings().registerInterceptSettings(name, doParseContextParams(interceptSet.params()), doInterceptSettingToArray(item)))));
        }
        return null;
    }
}
