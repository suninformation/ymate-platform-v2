/*
 * Copyright 2007-2021 the original author or authors.
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
package net.ymate.platform.core.configuration;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

import java.io.File;
import java.io.InputStream;

/**
 * 配置文件搜索器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2021/5/18 11:35 下午
 * @since 2.1.0
 */
@Ignored
public interface IConfigFileSearcher extends IInitialization<IConfig>, IDestroyable {

    /**
     * 搜索配置文件
     *
     * @param cfgFile 配置文件相对路径及名称
     * @return 配置文件对象
     */
    File search(String cfgFile);

    /**
     * 搜索配置文件并返回其真实资源路径
     *
     * @param cfgFile 配置文件相对路径及名称
     * @return 配置文件真实路径
     */
    String searchAsPath(String cfgFile);

    /**
     * 搜索配置文件并返回其文件输入流
     *
     * @param cfgFile 配置文件路径及名称
     * @return 配置文件输入流
     */
    InputStream searchAsStream(String cfgFile);
}
