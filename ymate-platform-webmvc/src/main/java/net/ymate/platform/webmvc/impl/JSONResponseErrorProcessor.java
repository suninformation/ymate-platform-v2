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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.webmvc.AbstractResponseErrorProcessor;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.WebResult;
import net.ymate.platform.webmvc.view.IView;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/12/29 10:59 PM
 * @since 2.1.0
 */
public class JSONResponseErrorProcessor extends AbstractResponseErrorProcessor {

    public JSONResponseErrorProcessor() {
        setErrorDefaultViewFormat(Type.Const.FORMAT_JSON);
    }

    @Override
    public String getErrorDefaultViewFormat(IWebMvc owner) {
        return Type.Const.FORMAT_JSON;
    }

    @Override
    public IView showErrorMsg(IWebMvc owner, String code, String msg, Map<String, Object> dataMap) {
        doProcessErrorStatusCodeIfNeed(owner);
        return WebResult.builder()
                .code(code)
                .msg(msg)
                .data(dataMap)
                .build()
                .withContentType()
                .toJsonView(StringUtils.trimToNull(WebContext.getRequest().getParameter(Type.Const.PARAM_CALLBACK)));
    }
}
