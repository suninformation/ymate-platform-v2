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
package net.ymate.platform.webmvc.validate;

import net.ymate.platform.core.support.IContext;
import net.ymate.platform.webmvc.IWebMvcConfig;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/8/12 上午4:31
 * @since 2.0.6
 */
public interface IHostNameChecker {

    IHostNameChecker DEFAULT = (context, url) -> {
        String hosts = context.getOwner().getParam(IWebMvcConfig.PARAMS_ALLOWED_ACCESS_HOSTS);
        if (StringUtils.isNotBlank(hosts)) {
            return StringUtils.containsIgnoreCase(hosts, new URL(url).getHost());
        }
        return true;
    };

    /**
     * 检查重定向URL主机是否被允许
     *
     * @param context 上下文对象接口实例
     * @param url     URL地址
     * @return 若主机列表为空或包含则返回true
     * @throws Exception 可能产生的任何异常
     */
    boolean check(IContext context, String url) throws Exception;
}
