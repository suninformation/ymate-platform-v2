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
package net.ymate.platform.validation.handle;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.validation.IValidation;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.annotation.Validator;

/**
 * 验证器类处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/15 下午2:51
 */
public class ValidateHandler implements IBeanHandler {

    private final IValidation owner;

    public ValidateHandler(IValidation owner) {
        this.owner = owner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isNormalClass(targetClass) && !targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, IValidator.class)) {
            owner.registerValidator(targetClass.getAnnotation(Validator.class).value(), (Class<? extends IValidator>) targetClass);
        }
        return null;
    }
}
