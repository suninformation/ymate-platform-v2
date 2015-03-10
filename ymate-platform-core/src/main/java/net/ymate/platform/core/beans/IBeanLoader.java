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
package net.ymate.platform.core.beans;

import java.util.List;

/**
 * 对象加载器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-5 下午12:01
 * @version 1.0
 */
public interface IBeanLoader {

    /**
     * @param packageName 扫描的包名称
     * @return 返回加载的类对象集合
     * @throws Exception
     */
    public List<Class<?>> load(String packageName) throws Exception;

    /**
     * @param packageName 扫描的包名称
     * @param filter      类对象过滤器
     * @return 返回加载的类对象集合
     * @throws Exception
     */
    public List<Class<?>> load(String packageName, IBeanFilter filter) throws Exception;
}
