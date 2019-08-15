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
package net.ymate.platform.plugin.annotation;

import net.ymate.platform.plugin.IPluginBeanLoaderFactory;
import net.ymate.platform.plugin.IPluginEventListener;
import net.ymate.platform.plugin.impl.DefaultPluginBeanLoaderFactory;
import net.ymate.platform.plugin.impl.DefaultPluginEventListener;

import java.lang.annotation.*;

/**
 * 声明一个类作为插件工厂的注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/19 下午7:41
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginFactory {

    /**
     * @return 插件存放路径
     */
    String pluginHome();

    /**
     * @return 自动扫描包名，默认为插件工厂所在包路径
     */
    String[] packageNames() default {};

    /**
     * @return 插件是否自动启动
     */
    boolean automatic() default true;

    /**
     * @return 插件生命周期事件监听器类对象
     */
    Class<? extends IPluginEventListener> listenerClass() default DefaultPluginEventListener.class;

    /**
     * @return 插件对象加载器工厂类
     */
    Class<? extends IPluginBeanLoaderFactory> loaderFactoryClass() default DefaultPluginBeanLoaderFactory.class;
}
