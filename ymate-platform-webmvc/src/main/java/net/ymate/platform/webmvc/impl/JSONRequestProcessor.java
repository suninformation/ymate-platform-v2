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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.context.WebContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Array;

/**
 * 基于JSON作为协议格式的控制器请求处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 上午11:51
 */
public class JSONRequestProcessor extends DefaultRequestProcessor {

    private static final Log LOG = LogFactory.getLog(JSONRequestProcessor.class);

    private IJsonObjectWrapper doGetProtocol(IWebMvc owner) {
        IRequestContext requestContext = WebContext.getRequestContext();
        IJsonObjectWrapper protocol = requestContext.getAttribute(JSONRequestProcessor.class.getName());
        if (protocol == null) {
            try {
                String jsonStr = StringUtils.defaultIfBlank(IOUtils.toString(WebContext.getRequest().getInputStream(), owner.getConfig().getDefaultCharsetEncoding()), "{}");
                JsonWrapper jsonWrapper = JsonWrapper.fromJson(jsonStr);
                if (jsonWrapper.isJsonObject()) {
                    protocol = jsonWrapper.getAsJsonObject();
                } else if (owner.getOwner().isDevEnv() && LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Invalid protocol content: %s", jsonStr));
                }
            } catch (IOException e) {
                if (owner.getOwner().isDevEnv() && LOG.isWarnEnabled()) {
                    LOG.warn("Invalid protocol.", RuntimeUtils.unwrapThrow(e));
                }
            }
            if (protocol == null) {
                protocol = JsonWrapper.createJsonObject();
            }
            requestContext.addAttribute(JSONRequestProcessor.class.getName(), protocol);
        }
        return protocol;
    }

    @Override
    protected Object doParseRequestParam(IWebMvc owner, String paramName, String defaultValue, Class<?> paramType, boolean fullScope) {
        Object returnValue = null;
        IJsonObjectWrapper protocol = doGetProtocol(owner);
        String[] paramNameArr = StringUtils.split(paramName, ".");
        if (paramType.isArray()) {
            if (!paramType.equals(IUploadFileWrapper[].class)) {
                Object[] values = null;
                if (paramNameArr.length > 1) {
                    IJsonObjectWrapper jsonObj = protocol.getJsonObject(paramNameArr[0]);
                    if (jsonObj != null) {
                        IJsonArrayWrapper jsonArr = jsonObj.getJsonArray(paramNameArr[1]);
                        if (jsonArr != null) {
                            values = jsonArr.toArray();
                        }
                    }
                } else {
                    IJsonArrayWrapper jsonArr = protocol.getJsonArray(paramName);
                    if (jsonArr != null) {
                        values = jsonArr.toArray();
                    }
                }
                if (values != null && values.length > 0) {
                    Class<?> arrayClassType = ClassUtils.getArrayClassType(paramType);
                    Object[] newArray = (Object[]) Array.newInstance(arrayClassType, values.length);
                    for (int arrayIdx = 0; arrayIdx < values.length; arrayIdx++) {
                        newArray[arrayIdx] = doSafeGetParamValue(owner, paramName, arrayClassType, BlurObject.bind(values[arrayIdx]).toStringValue(), null, false);
                    }
                    returnValue = newArray;
                }
            }
        } else if (!paramType.equals(IUploadFileWrapper.class)) {
            if (paramNameArr.length > 1) {
                IJsonObjectWrapper jsonObj = protocol.getJsonObject(paramNameArr[0]);
                if (jsonObj != null) {
                    returnValue = doSafeGetParamValue(owner, paramName, paramType, jsonObj.getString(paramNameArr[1]), defaultValue, fullScope);
                }
            } else {
                returnValue = doSafeGetParamValue(owner, paramName, paramType, protocol.getString(paramName), defaultValue, fullScope);
            }
        }
        return returnValue;
    }
}
