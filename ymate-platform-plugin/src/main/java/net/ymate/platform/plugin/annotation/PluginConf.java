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
package net.ymate.platform.plugin.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/3/9 9:24 下午
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginConf {

    /**
     * @return 是否启用插件模块
     */
    boolean enabled() default true;

    /**
     * 插件存放路径
     *
     * @return 返回插件存放路径
     */
    String pluginHome() default StringUtils.EMPTY;

    /**
     * @return 插件自动扫描的包名前缀集合（若未设置将默认包含主程序类所在包）
     */
    String[] packageNames() default {};

    /**
     * @return 插件自动扫描时排除包名称集合, 被包含在包路径下的类文件在扫描过程中将被忽略
     */
    String[] excludedPackageNames() default {};

    /**
     * @return 插件自动扫描时排除包文件名称集合, 被包含的JAR或ZIP文件在扫描过程中将被忽略
     */
    String[] excludedFileNames() default {};

    /**
     * @return 是否扫描当前CLASSPATH内的相关插件
     */
    boolean includeClasspath() default false;

    /**
     * @return 是否允许插件自动启动
     */
    boolean automatic() default true;
}
