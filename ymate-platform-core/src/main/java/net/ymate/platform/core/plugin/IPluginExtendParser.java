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
package net.ymate.platform.core.plugin;

/**
 * 插件扩展内容解析器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/19 下午6:09
 * @version 1.0
 */
public interface IPluginExtendParser<T> {

    /**
     * @param context    插件环境上下文对象
     * @param extendPart 插件扩展内容对象
     * @return 执行解析过程并返回扩展对象
     * @throws Exception
     */
    public T doParser(IPluginContext context, Object extendPart) throws Exception;
}
