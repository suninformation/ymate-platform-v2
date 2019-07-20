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
package net.ymate.platform.webmvc;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.webmvc.base.Type;

import java.util.Map;

/**
 * WebMVC请求映射路径分析器接口
 *
 * @author 刘镇 (suninformation@163.com) on 17/3/17 下午10:45
 */
@Ignored
public interface IRequestMappingParser {

    /**
     * 注册控制器请求映射元数据描述
     *
     * @param requestMeta 控制器请求映射元数据描述
     */
    void registerRequestMeta(RequestMeta requestMeta);

    /**
     * 根据HTTP请求方式获取对应的控制器请求映射元数据描述映射
     *
     * @param httpMethod HTTP请求方式
     * @return 返回请求映射元数据描述映射
     * @since 2.0.6
     */
    Map<String, RequestMeta> getRequestMetas(Type.HttpMethod httpMethod);

    /**
     * 分析请求映射串，匹配成功则返回对应映射集合的键值，同时处理请求串中的参数变量存入WebContext容器中的PathVariable参数池
     *
     * @param context 请求上下文对象
     * @return 返回请求映射元数据描述对象
     */
    RequestMeta parse(IRequestContext context);
}
