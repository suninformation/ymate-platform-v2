/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 验证框架管理器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/15 上午10:01
 * @version 1.0
 */
public interface IValidation {

    /**
     * 注册验证器
     *
     * @param annotationClass 验证器作用的注解
     * @param validatorClass  验证器接口类型
     */
    public void registerValidator(Class<? extends Annotation> annotationClass, Class<? extends IValidator> validatorClass);

    /**
     * @param annotationClass 验证器作用的注解
     * @return 判断是否包含处理annoClass注解的验证器存在
     */
    public boolean containsValidator(Class<? extends Annotation> annotationClass);

    /**
     * 执行类成员参数验证
     *
     * @param targetClass
     * @param paramValues
     * @return 返回验证结果映射
     */
    public <T> Map<String, ValidateResult> validate(Class<T> targetClass, Map<String, Object> paramValues);

    /**
     * 执行类方法参数验证
     *
     * @param targetClass
     * @param targetMethod
     * @param paramValues
     * @return 返回验证结果映射
     */
    public <T> Map<String, ValidateResult> validate(Class<T> targetClass, Method targetMethod, Map<String, Object> paramValues);
}
