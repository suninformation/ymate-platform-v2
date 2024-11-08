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
package net.ymate.platform.webmvc.cors;

import net.ymate.platform.core.beans.intercept.AbstractInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptContext;
import net.ymate.platform.core.beans.intercept.InterceptException;

/**
 * 为允许跨域的请求添加必要的请求头参数 (from ymate-framework-core)
 *
 * @author 刘镇 (suninformation@163.com) on 17/3/23 下午5:01
 * @since 2.1.0
 * @deprecated 从 `2.1.3` 开始不再使用拦截器处理跨域请求
 */
@Deprecated
public final class CrossDomainInterceptor extends AbstractInterceptor {

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        return null;
    }
}
