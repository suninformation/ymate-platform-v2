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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.webmvc.IRequestProcessor;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.RequestMeta;

import java.util.Map;

/**
 * 基于XML作为协议格式控制器请求处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 上午11:52
 * @version 1.0
 */
public class XMLRequestProcessor implements IRequestProcessor {

    public Map<String, Object> processRequestParams(IWebMvc owner, RequestMeta requestMeta, String[] methodParamNames) throws Exception {
        return null;
    }
}
