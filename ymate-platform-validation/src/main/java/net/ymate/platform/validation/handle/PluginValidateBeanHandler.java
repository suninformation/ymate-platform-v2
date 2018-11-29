/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.validation.handle;

import net.ymate.platform.core.YMP;
import net.ymate.platform.plugin.PluginClassLoader;
import net.ymate.platform.plugin.annotation.Handler;
import net.ymate.platform.validation.Validations;
import net.ymate.platform.validation.annotation.Validator;

/**
 * 插件验证器类处理器 (from ymate-framework-addons)
 *
 * @author 刘镇 (suninformation@163.com) on 15/8/15 上午9:31
 * @version 1.0
 */
@Handler(Validator.class)
public class PluginValidateBeanHandler extends ValidateHandler {

    public PluginValidateBeanHandler(YMP owner) throws Exception {
        super(owner.getModule(Validations.class));
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (targetClass.getClassLoader() instanceof PluginClassLoader) {
            return super.handle(targetClass);
        }
        return null;
    }
}
