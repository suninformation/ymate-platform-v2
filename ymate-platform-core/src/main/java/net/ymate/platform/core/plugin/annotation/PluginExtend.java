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

import net.ymate.platform.core.plugin.IPluginExtendParser;

import java.lang.annotation.*;

/**
 * 配置插件扩展内容如何解析的注解
 * <p>
 * 1、若与@Plugin配合使用时其作用域仅为当前插件范围;<br>
 * 2、若与@PluginFactory配合使用时其作用域为当前插件工厂范围;<br>
 * 3、两者兼备时，@Plugin级别的@PluginExtend优先生效;
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/19 下午7:21
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginExtend {

    /**
     * @return 指定解析成功后的数据包装对象类型
     */
    Class<?> value();

    /**
     * @return 插件扩展部份内容分析器类对象
     */
    Class<? extends IPluginExtendParser> parserClass();
}
