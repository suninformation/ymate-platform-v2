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
package net.ymate.platform.core.beans;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/3/9 上午12:24
 */
public interface IBeanInitializer {

    /**
     * 执行类初始化方法
     *
     * @param beanFactory 对象工厂
     * @throws Exception 可能产生的任何异常
     */
    void afterInitialized(IBeanFactory beanFactory) throws Exception;
}
