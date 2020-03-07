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
package net.ymate.platform.starter.web.impl;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationInitializer;
import net.ymate.platform.core.impl.DefaultApplicationCreator;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 02:40
 * @since 2.1.0
 */
public class WebApplicationCreator extends DefaultApplicationCreator {

    @Override
    public IApplication create(Class<?> mainClass, String[] args, IApplicationInitializer... applicationInitializers) throws Exception {
        // TODO 通过自定义Maven插件生成特制Jar包, 启动时自动解压并采用嵌入式Web容器完成启动过程, 支持Tomcat、Jetty、Undertow等常用嵌入式Web容器;
        return super.create(mainClass, args, applicationInitializers);
    }
}
