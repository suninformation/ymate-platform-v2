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
package net.ymate.platform.core.plugin.annotation;

import net.ymate.platform.core.plugin.IPluginEventListener;
import net.ymate.platform.core.plugin.IPluginParser;
import net.ymate.platform.core.plugin.impl.DefaultPluginEventListener;
import net.ymate.platform.core.plugin.impl.DefaultPluginParser;

import java.lang.annotation.*;

/**
 * 声明一个类作为插件工厂的注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/19 下午7:41
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginFactory {

    /**
     * @return 插件存放路径
     */
    String pluginHome();

    /**
     * @return 自动扫描路径，默认为插件工厂所在包路径
     */
    String[] autoscanPackages() default {};

    /**
     * @return 插件配置文件名称
     */
    String manifestFile() default "";

    /**
     * @return 插件是否自动启动
     */
    boolean automatic() default true;

    /**
     * @return 是否加载当前CLASSPATH内的所有包含插件配置文件的Jar包
     */
    boolean includedClassPath() default false;

    /**
     * @return 插件配置分析器类对象
     */
    Class<? extends IPluginParser> parserClass() default DefaultPluginParser.class;

    /**
     * @return 插件生命周期事件监听器类对象
     */
    Class<? extends IPluginEventListener> listenerClass() default DefaultPluginEventListener.class;
}
