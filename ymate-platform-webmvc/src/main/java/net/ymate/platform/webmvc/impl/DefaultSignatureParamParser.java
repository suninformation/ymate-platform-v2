/*
 * Copyright 2007-2020 the original author or authors.
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

import net.ymate.platform.webmvc.ISignatureParamParser;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.context.WebContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/18 21:38
 * @since 2.1.0
 */
public class DefaultSignatureParamParser implements ISignatureParamParser {

    @Override
    public Map<String, Object> getParams(IWebMvc owner, RequestMeta requestMeta) {
        Map<String, String[]> parameters = WebContext.getRequest().getParameterMap();
        Map<String, Object> returnValue = new HashMap<>(parameters.size());
        parameters.forEach((key, value) -> {
            if (value != null) {
                returnValue.put(key, value.length > 1 ? value : value[0]);
            }
        });
        return returnValue;
    }
}
