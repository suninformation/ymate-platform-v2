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
package net.ymate.platform.core.persistence;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IInitialization;

/**
 * 基准数据源配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2019-05-16 01:09
 * @since 2.1.0
 */
@Ignored
public interface IDataSourceConfig<OWNER extends IPersistence> extends IInitialization<OWNER> {

    /**
     * 获取数据源名称
     *
     * @return 返回数据源名称
     */
    String getName();

    /**
     * 数据源访问用户名称，必要参数
     *
     * @return 返回数据源用户名称
     */
    String getUsername();

    /**
     * 数据源访问密码，可选参数
     *
     * @return 返回数据源密码
     */
    String getPassword();

    /**
     * 数据源访问密码是否已加密，默认为false
     *
     * @return 返回true表示已加密
     */
    boolean isPasswordEncrypted();

    /**
     * 数据源密码处理器，可选参数，用于对已加密码数据源访问密码进行解密，默认为空
     *
     * @return 返回数据源密码处理器类型
     */
    Class<? extends IPasswordProcessor> getPasswordClass();
}
