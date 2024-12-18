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
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.context.WebContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于JSON作为协议格式的控制器请求处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 上午11:51
 */
public class JSONRequestProcessor extends DefaultRequestProcessor {

    private static final Log LOG = LogFactory.getLog(JSONRequestProcessor.class);

    private JsonWrapper doGetProtocol(IWebMvc owner) {
        IRequestContext requestContext = WebContext.getRequestContext();
        JsonWrapper protocol = requestContext.getAttribute(JSONRequestProcessor.class.getName());
        if (protocol == null) {
            try (InputStream inputStream = WebContext.getRequest().getInputStream()) {
                String jsonStr = StringUtils.trimToNull(IOUtils.toString(inputStream, owner.getConfig().getDefaultCharsetEncoding()));
                JsonWrapper jsonWrapper = JsonWrapper.fromJson(jsonStr);
                if (jsonWrapper != null) {
                    protocol = jsonWrapper;
                    if (owner.getOwner().isDevEnv() && LOG.isDebugEnabled() && owner.getOwner().getParamConfigReader().getBoolean(REQUEST_PROTOCOL_LOG_ENABLED_KEY)) {
                        LOG.debug(String.format("Protocol content: %s", protocol));
                    }
                } else if (owner.getOwner().isDevEnv() && LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Invalid protocol content: %s", jsonStr.replaceAll("[\n\r]", "_")));
                }
            } catch (Exception e) {
                if (owner.getOwner().isDevEnv() && LOG.isWarnEnabled()) {
                    LOG.warn("Invalid protocol.", RuntimeUtils.unwrapThrow(e));
                }
            }
            if (protocol == null) {
                protocol = JsonWrapper.fromJson("{}");
            }
            requestContext.addAttribute(JSONRequestProcessor.class.getName(), protocol);
        }
        return protocol;
    }

    protected Object doParseRequestParam(IWebMvc owner, ParameterMeta paramMeta, IJsonObjectWrapper objectWrapper, String paramName, String defaultValue, boolean fullScope) {
        Object returnValue = null;
        String[] paramNameArr = StringUtils.split(paramName, ".");
        if (paramMeta.isArray()) {
            if (!paramMeta.getParamType().equals(IUploadFileWrapper[].class)) {
                Object[] values = null;
                if (paramNameArr.length > 1) {
                    IJsonObjectWrapper jsonObj = objectWrapper.getJsonObject(paramNameArr[0]);
                    if (jsonObj != null) {
                        IJsonArrayWrapper jsonArr = jsonObj.getJsonArray(paramNameArr[1]);
                        if (jsonArr != null) {
                            values = jsonArr.toArray();
                        }
                    }
                } else {
                    IJsonArrayWrapper jsonArr = objectWrapper.getJsonArray(paramName);
                    if (jsonArr != null) {
                        values = jsonArr.toArray();
                    }
                }
                if (values != null && values.length > 0) {
                    Class<?> arrayClassType = ClassUtils.getArrayClassType(paramMeta.getParamType());
                    Object[] newArray = (Object[]) Array.newInstance(arrayClassType, values.length);
                    for (int arrayIdx = 0; arrayIdx < values.length; arrayIdx++) {
                        newArray[arrayIdx] = doSafeGetParamValue(owner, paramName, arrayClassType, BlurObject.bind(values[arrayIdx]).toStringValue(), null, false);
                    }
                    returnValue = newArray;
                }
            }
        } else if (!paramMeta.getParamType().equals(IUploadFileWrapper.class)) {
            if (paramNameArr.length > 1) {
                IJsonObjectWrapper jsonObj = objectWrapper.getJsonObject(paramNameArr[0]);
                if (jsonObj != null) {
                    returnValue = doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), jsonObj.getString(paramNameArr[1]), defaultValue, fullScope);
                }
            } else {
                returnValue = doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), objectWrapper.getString(paramName), defaultValue, fullScope);
            }
        }
        return returnValue;
    }

    @Override
    protected Object doParseRequestParam(IWebMvc owner, ParameterMeta paramMeta, String paramName, String defaultValue, boolean fullScope) {
        JsonWrapper protocol = doGetProtocol(owner);
        IJsonObjectWrapper objectWrapper = null;
        if (protocol.isJsonArray()) {
            IJsonArrayWrapper arrayWrapper = protocol.getAsJsonArray();
            if (arrayWrapper != null && !arrayWrapper.isEmpty()) {
                objectWrapper = arrayWrapper.getJsonObject(0);
            }
        } else {
            objectWrapper = protocol.getAsJsonObject();
        }
        return objectWrapper != null ? doParseRequestParam(owner, paramMeta, objectWrapper, paramName, defaultValue, fullScope) : null;
    }

    @Override
    protected Object doParseModelBind(IWebMvc owner, RequestMeta requestMeta, ParameterMeta paramMeta, Class<?> paramType) throws Exception {
        if (paramType.isArray()) {
            Map<String, ParameterMeta> parameterMetaMap = new HashMap<>(16);
            List<Object> returnValue = new ArrayList<>();
            //
            JsonWrapper protocol = doGetProtocol(owner);
            IJsonArrayWrapper arrayWrapper;
            if (protocol.isJsonArray()) {
                arrayWrapper = protocol.getAsJsonArray();
            } else {
                arrayWrapper = JsonWrapper.createJsonArray().add(protocol.getAsJsonObject());
            }
            if (arrayWrapper != null && !arrayWrapper.isEmpty()) {
                Class<?> arrayClassType = ClassUtils.getArrayClassType(paramType);
                if (arrayClassType != null) {
                    ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapperClass(arrayClassType);
                    if (beanWrapper != null) {
                        for (String fieldName : beanWrapper.getFieldNames()) {
                            // 当控制器参数为@ModelBind数组时，仅支持通过@RequestParam注解获取参数
                            Field field = beanWrapper.getField(fieldName);
                            if (field.isAnnotationPresent(RequestParam.class)) {
                                ParameterMeta parameterMeta = new ParameterMeta(field);
                                if (parameterMeta.isParamField()) {
                                    parameterMetaMap.put(fieldName, parameterMeta);
                                }
                            }
                        }
                    }
                }
                if (!parameterMetaMap.isEmpty()) {
                    for (int idx = 0; idx < arrayWrapper.size(); idx++) {
                        IJsonNodeWrapper nodeWrapper = arrayWrapper.get(idx);
                        if (nodeWrapper != null && nodeWrapper.isJsonObject()) {
                            ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapperClass(arrayClassType);
                            if (beanWrapper != null) {
                                for (Map.Entry<String, ParameterMeta> parameterMetaEntry : parameterMetaMap.entrySet()) {
                                    String paramName = ParameterMeta.buildParamName(paramMeta.getPrefix(), parameterMetaEntry.getValue().getParamName(), parameterMetaEntry.getKey(), requestMeta.isSnakeCase());
                                    RequestParam ann = (RequestParam) parameterMetaEntry.getValue().getParamAnnotation();
                                    Object paramValue = doParseRequestParam(owner, parameterMetaEntry.getValue(), nodeWrapper.getJsonObject(), paramName, StringUtils.trimToNull(ann.defaultValue()), ann.fullScope());
                                    beanWrapper.setValue(parameterMetaEntry.getKey(), paramValue);
                                }
                                returnValue.add(beanWrapper.getTargetObject());
                            }
                        }
                    }
                }
                return returnValue.toArray((Object[]) Array.newInstance(arrayClassType, returnValue.size()));
            }
            return null;
        }
        return doParseModelBindSingleton(owner, requestMeta, paramMeta, paramType);
    }
}
