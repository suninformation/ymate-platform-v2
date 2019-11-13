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
package net.ymate.platform.core.i18n;

import net.ymate.platform.core.beans.annotation.Ignored;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * 国际化资源管理器事件监听处理器
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-14 下午2:44:08
 */
@Ignored
public interface II18nEventHandler {

    /**
     * 加载当前Locale对象
     *
     * @return 返回Locale对象
     */
    Locale onLocale();

    /**
     * 当Locale改变时处理此方法
     *
     * @param locale Locale对象
     */
    void onChanged(Locale locale);

    /**
     * 加载资源文件的具体处理方法
     *
     * @param resourceName 资源名称
     * @return 返回加载的资源文件流
     * @throws IOException 加载资源文件可能发生异常
     */
    InputStream onLoad(String resourceName) throws IOException;
}
