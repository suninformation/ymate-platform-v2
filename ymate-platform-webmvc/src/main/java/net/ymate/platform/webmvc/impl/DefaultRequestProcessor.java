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

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.annotation.*;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.CookieHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认(基于标准WEB)控制器请求处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/27 下午5:42
 */
public class DefaultRequestProcessor implements IRequestProcessor {

    private static final Log LOG = LogFactory.getLog(DefaultRequestProcessor.class);

    @Override
    public Map<String, Object> processRequestParams(IWebMvc owner, RequestMeta requestMeta) throws Exception {
        Map<String, Object> returnValues = new LinkedHashMap<>();
        // 非单例控制器类将不执行类成员的参数处理
        if (!requestMeta.isSingleton()) {
            returnValues.putAll(doGetParamValueFromParameterMetas(owner, requestMeta, requestMeta.getClassParameterMetas()));
        }
        // 处理控制器方法参数
        returnValues.putAll(doGetParamValueFromParameterMetas(owner, requestMeta, requestMeta.getMethodParameterMetas()));
        //
        return returnValues;
    }

    private Map<String, Object> doGetParamValueFromParameterMetas(IWebMvc owner, RequestMeta requestMeta, Collection<ParameterMeta> metas) throws Exception {
        Map<String, Object> resultMap = new HashMap<>(metas.size());
        for (ParameterMeta parameterMeta : metas) {
            String paramName = parameterMeta.isKeepParamName() ? parameterMeta.getParamName() : ParameterMeta.buildParamName(null, parameterMeta.getParamName(), parameterMeta.getFieldName(), requestMeta.isSnakeCase());
            Object paramValue = doGetParamValue(owner, requestMeta, parameterMeta, paramName);
            if (paramValue != null) {
                resultMap.put(parameterMeta.getFieldName(), paramValue);
                if (!StringUtils.equals(paramName, parameterMeta.getFieldName())) {
                    resultMap.put(paramName, paramValue);
                }
            }
        }
        return resultMap;
    }

    /**
     * 分析请求参数的值
     *
     * @param owner       Owner
     * @param requestMeta 请求元描述对象
     * @param paramMeta   参数元描述对象
     * @param paramName   参数名称
     * @return 返回参数名称与值对象
     * @throws Exception 可能产生的异常
     */
    private Object doGetParamValue(IWebMvc owner, RequestMeta requestMeta, ParameterMeta paramMeta, String paramName/*, Class<?> paramType, Annotation annotation*/) throws Exception {
        Object paramValue = null;
        Annotation annotation = paramMeta.getParamAnnotation();
        if (annotation instanceof CookieVariable) {
            CookieVariable ann = (CookieVariable) annotation;
            String value = CookieHelper.bind(owner).getCookie(paramName).toStringValue();
            paramValue = doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), value, StringUtils.trimToNull(ann.defaultValue()), ann.fullScope());
        } else if (annotation instanceof PathVariable) {
            String value = WebContext.getRequestContext().getAttribute(paramName);
            paramValue = doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), value, null, false);
        } else if (annotation instanceof RequestHeader) {
            RequestHeader ann = (RequestHeader) annotation;
            String value = WebContext.getRequest().getHeader(paramName);
            paramValue = doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), value, StringUtils.trimToNull(ann.defaultValue()), ann.fullScope());
        } else if (annotation instanceof RequestParam) {
            RequestParam ann = (RequestParam) annotation;
            paramValue = this.doParseRequestParam(owner, paramMeta, paramName, StringUtils.trimToNull(ann.defaultValue()), ann.fullScope());
        } else if (annotation instanceof ModelBind) {
            paramValue = doParseModelBind(owner, requestMeta, paramMeta, paramMeta.getParamType());
        }
        return paramValue;
    }

    protected Object doParseRequestParam(IWebMvc owner, ParameterMeta paramMeta, String paramName, String defaultValue, boolean fullScope) {
        HttpServletRequest httpServletRequest = WebContext.getRequest();
        if (paramMeta.isArray()) {
            if (paramMeta.getParamType().equals(IUploadFileWrapper[].class)) {
                if (httpServletRequest instanceof IMultipartRequestWrapper) {
                    return ((IMultipartRequestWrapper) httpServletRequest).getUploadFiles(paramName);
                }
                return null;
            }
            String[] values = httpServletRequest.getParameterMap().get(paramName);
            if (values == null || values.length == 0) {
                values = StringUtils.split(defaultValue, StringUtils.defaultIfBlank(paramMeta.getSplitArraySeparator(), ","));
            } else if (values.length == 1 && StringUtils.isNotBlank(paramMeta.getSplitArraySeparator())) {
                values = StringUtils.split(values[0], StringUtils.defaultIfBlank(paramMeta.getSplitArraySeparator(), ","));
            }
            if (values != null && values.length > 0) {
                return doSafeGetParamValueArray(owner, paramName, ClassUtils.getArrayClassType(paramMeta.getParamType()), values);
            }
            return null;
        } else if (paramMeta.getParamType().equals(IUploadFileWrapper.class)) {
            if (httpServletRequest instanceof IMultipartRequestWrapper) {
                return ((IMultipartRequestWrapper) httpServletRequest).getUploadFile(paramName);
            }
            return null;
        }
        return doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), httpServletRequest.getParameter(paramName), defaultValue, fullScope);
    }

    protected Object[] doSafeGetParamValueArray(IWebMvc owner, String paramName, Class<?> arrayClassType, String[] values) {
        Object[] newArray = (Object[]) Array.newInstance(arrayClassType, values.length);
        for (int arrayIdx = 0; arrayIdx < values.length; arrayIdx++) {
            newArray[arrayIdx] = doSafeGetParamValue(owner, paramName, arrayClassType, values[arrayIdx], null, false);
        }
        return newArray;
    }

    protected Object doSafeGetParamValue(IWebMvc owner, String paramName, Class<?> paramType, String paramValue, String defaultValue, boolean fullScope) {
        Object returnValue = null;
        try {
            if (paramValue == null) {
                if (fullScope) {
                    returnValue = new BlurObject(WebContext.getRequest().getParameter(paramName)).toObjectValue(paramType);
                    if (returnValue == null) {
                        returnValue = new BlurObject(WebContext.getContext().getSession().get(paramName)).toObjectValue(paramType);
                        if (returnValue == null) {
                            returnValue = new BlurObject(WebContext.getContext().getApplication().get(paramName)).toObjectValue(paramType);
                        }
                    }
                }
            }
            if (returnValue == null) {
                returnValue = new BlurObject(StringUtils.defaultIfBlank(paramValue, defaultValue)).toObjectValue(paramType);
            }
        } catch (Throwable e) {
            if (owner.getOwner().isDevEnv() && LOG.isWarnEnabled()) {
                LOG.warn(String.format("Invalid '%s' value: %s", paramName, paramValue), RuntimeUtils.unwrapThrow(e));
            }
        }
        return returnValue;
    }

    protected Object doParseModelBindSingleton(IWebMvc owner, RequestMeta requestMeta, ParameterMeta paramMeta, Class<?> paramType) throws Exception {
        ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapperClass(paramType);
        if (beanWrapper != null) {
            for (String fieldName : beanWrapper.getFieldNames()) {
                ParameterMeta parameterMeta = new ParameterMeta(beanWrapper.getField(fieldName));
                if (parameterMeta.isParamField()) {
                    Object paramValue = doGetParamValue(owner, requestMeta, parameterMeta, parameterMeta.isKeepParamName() ? parameterMeta.getParamName() : ParameterMeta.buildParamName(paramMeta.getPrefix(), parameterMeta.getParamName(), fieldName, requestMeta.isSnakeCase()));
                    if (paramValue != null) {
                        beanWrapper.setValue(fieldName, paramValue);
                    }
                }
            }
            return beanWrapper.getTargetObject();
        }
        return null;
    }

    protected Object doParseModelBind(IWebMvc owner, RequestMeta requestMeta, ParameterMeta paramMeta, Class<?> paramType) throws Exception {
        if (paramType.isArray()) {
            HttpServletRequest httpServletRequest = WebContext.getRequest();
            Map<String, ParameterMeta> parameterMetaMap = new HashMap<>(16);
            Object[] returnValue = null;
            //
            Class<?> arrayClassType = ClassUtils.getArrayClassType(paramType);
            if (arrayClassType != null) {
                ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapperClass(arrayClassType);
                if (beanWrapper != null) {
                    int maxLength = 0;
                    for (String fieldName : beanWrapper.getFieldNames()) {
                        // 当控制器参数为@ModelBind数组时，仅支持通过@RequestParam注解获取参数
                        Field field = beanWrapper.getField(fieldName);
                        if (field.isAnnotationPresent(RequestParam.class)) {
                            ParameterMeta parameterMeta = new ParameterMeta(field);
                            if (parameterMeta.isParamField()) {
                                parameterMetaMap.put(fieldName, parameterMeta);
                                // 尝试计算数组长度并创建数组对象实例
                                String[] param = httpServletRequest.getParameterMap().get(ParameterMeta.buildParamName(paramMeta.getPrefix(), parameterMeta.getParamName(), fieldName, requestMeta.isSnakeCase()));
                                if (param != null) {
                                    maxLength = Math.max(param.length, maxLength);
                                }
                            }
                        }
                    }
                    returnValue = (Object[]) Array.newInstance(arrayClassType, maxLength);
                }
            }
            //
            if (returnValue != null && !parameterMetaMap.isEmpty()) {
                for (int idx = 0; idx < returnValue.length; idx++) {
                    ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapperClass(arrayClassType);
                    if (beanWrapper != null) {
                        for (Map.Entry<String, ParameterMeta> parameterMetaEntry : parameterMetaMap.entrySet()) {
                            String paramName = ParameterMeta.buildParamName(paramMeta.getPrefix(), parameterMetaEntry.getValue().getParamName(), parameterMetaEntry.getKey(), requestMeta.isSnakeCase());
                            Object paramValue = null;
                            if (parameterMetaEntry.getValue().getParamAnnotation() instanceof RequestParam) {
                                RequestParam ann = (RequestParam) parameterMetaEntry.getValue().getParamAnnotation();
                                if (parameterMetaEntry.getValue().isArray()) {
                                    if (parameterMetaEntry.getValue().getParamType().equals(IUploadFileWrapper[].class)) {
                                        if (httpServletRequest instanceof IMultipartRequestWrapper) {
                                            return ((IMultipartRequestWrapper) httpServletRequest).getUploadFiles(paramName);
                                        }
                                        return null;
                                    }
                                    String[] values = httpServletRequest.getParameterMap().get(paramName);
                                    if (values == null || values.length == 0) {
                                        values = StringUtils.split(ann.defaultValue(), ",");
                                    }
                                    if (values != null && values.length > 0) {
                                        Class<?> paramArrayClassType = ClassUtils.getArrayClassType(parameterMetaEntry.getValue().getParamType());
                                        if (paramArrayClassType != null) {
                                            Object[] newParamArray = (Object[]) Array.newInstance(paramArrayClassType, values.length);
                                            for (int pArrayIdx = 0; pArrayIdx < values.length; pArrayIdx++) {
                                                newParamArray[pArrayIdx] = new BlurObject(values[pArrayIdx]).toObjectValue(paramArrayClassType);
                                            }
                                            paramValue = newParamArray;
                                        }
                                    }
                                } else if (parameterMetaEntry.getValue().getParamType().equals(IUploadFileWrapper.class)) {
                                    if (httpServletRequest instanceof IMultipartRequestWrapper) {
                                        return ((IMultipartRequestWrapper) httpServletRequest).getUploadFile(paramName);
                                    }
                                } else {
                                    String[] values = httpServletRequest.getParameterMap().get(paramName);
                                    if (values != null && values.length >= idx) {
                                        paramValue = new BlurObject(StringUtils.defaultIfBlank(values[idx], ann.defaultValue())).toObjectValue(parameterMetaEntry.getValue().getParamType());
                                    }
                                }
                            }
                            beanWrapper.setValue(parameterMetaEntry.getKey(), paramValue);
                        }
                        returnValue[idx] = beanWrapper.getTargetObject();
                    }
                }
            }
            return returnValue;
        }
        return doParseModelBindSingleton(owner, requestMeta, paramMeta, paramType);
    }
}
