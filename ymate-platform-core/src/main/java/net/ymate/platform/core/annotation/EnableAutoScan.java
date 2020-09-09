/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.core.annotation;

import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.impl.DefaultBeanLoadFactory;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/08 18:05
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableAutoScan {

    /**
     * @return 自动扫描的包名称集合（将默认包含主程序类所在包）
     */
    String[] value() default {};

    /**
     * @return 排除包名称集合, 被包含在包路径下的类文件在扫描过程中将被忽略
     */
    String[] excluded() default {};

    /**
     * @return 排除包文件名称集合, 被包含的JAR或ZIP文件在扫描过程中将被忽略
     */
    String[] excludedFiles() default {};

    /**
     * @return 排除模块类名集合, 被包含的模块在加载过程中将被忽略
     */
    String[] excludedModules() default {};

    /**
     * @return 包含模块名集合，若设置该参数则框架初始化时仅加载被包含的模块
     */
    String[] includedModules() default {};

    /**
     * @return 对象加载器工厂类型
     */
    Class<? extends IBeanLoadFactory> factoryClass() default DefaultBeanLoadFactory.class;
}
