/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.annotation.*;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.support.MultipartRequestWrapper;
import net.ymate.platform.webmvc.util.CookieHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认(基于标准WEB)控制器请求处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/27 下午5:42
 * @version 1.0
 */
public class DefaultRequestProcessor implements IRequestProcessor {

    private final Log _LOG = LogFactory.getLog(DefaultRequestProcessor.class);

    public Map<String, Object> processRequestParams(IWebMvc owner, RequestMeta requestMeta) throws Exception {
        Map<String, Object> _returnValues = new LinkedHashMap<String, Object>();
        // 非单例控制器类将不执行类成员的参数处理
        if (!requestMeta.isSingleton()) {
            _returnValues.putAll(__doGetParamValueFromParameterMetas(owner, requestMeta.getClassParameterMetas()));
        }
        // 处理控制器方法参数
        _returnValues.putAll(__doGetParamValueFromParameterMetas(owner, requestMeta.getMethodParameterMetas()));
        //
        return _returnValues;
    }

    protected Map<String, Object> __doGetParamValueFromParameterMetas(IWebMvc owner, Collection<ParameterMeta> metas) throws Exception {
        Map<String, Object> _resultMap = new HashMap<String, Object>();
        for (ParameterMeta _meta : metas) {
            Object _result = __doGetParamValue(owner, _meta, _meta.getParamName(), _meta.getParamType(), _meta.getParamAnno());
            if (_result != null) {
                _result = _meta.doParamEscape(_meta, _result);
                //
                _resultMap.put(_meta.getParamName(), _result);
                _resultMap.put(_meta.getFieldName(), _result);
            }
        }
        return _resultMap;
    }

    /**
     * 分析请求参数的值
     *
     * @param owner       Owner
     * @param paramName   参数名称
     * @param paramType   参数类型
     * @param _annotation 参数上声明的参数绑定注解
     * @return 返回参数名称与值对象
     * @throws Exception
     */
    protected Object __doGetParamValue(IWebMvc owner, ParameterMeta paramMeta, String paramName, Class<?> paramType, Annotation _annotation) throws Exception {
        Object _pValue = null;
        if (_annotation instanceof CookieVariable) {
            CookieVariable _anno = (CookieVariable) _annotation;
            String _v = CookieHelper.bind(owner).getCookie(paramName).toStringValue();
            _pValue = __doSafeGetParamValue(owner, paramName, paramType, _v, StringUtils.trimToNull(_anno.defaultValue()));
        } else if (_annotation instanceof PathVariable) {
            String _v = WebContext.getRequestContext().getAttribute(paramName);
            _pValue = __doSafeGetParamValue(owner, paramName, paramType, _v, null);
        } else if (_annotation instanceof RequestHeader) {
            RequestHeader _anno = (RequestHeader) _annotation;
            String _v = WebContext.getRequest().getHeader(paramName);
            _pValue = __doSafeGetParamValue(owner, paramName, paramType, _v, StringUtils.trimToNull(_anno.defaultValue()));
        } else if (_annotation instanceof RequestParam) {
            RequestParam _anno = (RequestParam) _annotation;
            _pValue = this.__doParseRequestParam(owner, paramName, StringUtils.trimToNull(_anno.defaultValue()), paramType);
        } else if (_annotation instanceof ModelBind) {
            _pValue = __doParseModelBind(owner, paramMeta, paramType);
        }
        return _pValue;
    }

    protected Object __doParseRequestParam(IWebMvc owner, String paramName, String defaultValue, Class<?> paramType) {
        if (paramType.isArray()) {
            if (paramType.equals(IUploadFileWrapper[].class)) {
                if (WebContext.getRequest() instanceof MultipartRequestWrapper) {
                    return ((MultipartRequestWrapper) WebContext.getRequest()).getUploadFiles(paramName);
                }
                return null;
            }
            String[] _values = (String[]) WebContext.getRequest().getParameterMap().get(paramName);
            if (_values == null || _values.length == 0) {
                _values = StringUtils.split(defaultValue, ",");
            }
            if (_values != null && _values.length > 0) {
                Class<?> _arrayClassType = ClassUtils.getArrayClassType(paramType);
                Object[] _tempParams = (Object[]) Array.newInstance(_arrayClassType, _values.length);
                for (int _tempIdx = 0; _tempIdx < _values.length; _tempIdx++) {
                    _tempParams[_tempIdx] = __doSafeGetParamValue(owner, paramName, _arrayClassType, _values[_tempIdx], null);
                }
                return _tempParams;
            }
            return null;
        } else if (paramType.equals(IUploadFileWrapper.class)) {
            if (WebContext.getRequest() instanceof MultipartRequestWrapper) {
                return ((MultipartRequestWrapper) WebContext.getRequest()).getUploadFile(paramName);
            }
            return null;
        }
        return __doSafeGetParamValue(owner, paramName, paramType, null, defaultValue);
    }

    protected Object __doSafeGetParamValue(IWebMvc owner, String paramName, Class<?> paramType, String paramValue, String defaultValue) {
        Object _returnValue = null;
        String _pValue = paramValue;
        try {
            if (_pValue == null) {
                _pValue = WebContext.getRequest().getParameter(paramName);
            }
            _returnValue = new BlurObject(StringUtils.defaultIfBlank(_pValue, defaultValue)).toObjectValue(paramType);
        } catch (Throwable e) {
            if (owner.getOwner().getConfig().isDevelopMode()) {
                _LOG.warn("Invalid '" + paramName + "' value: " + _pValue, RuntimeUtils.unwrapThrow(e));
            }
        }
        return _returnValue;
    }

    protected Object __doParseModelBind(IWebMvc owner, ParameterMeta paramMeta, Class<?> paramType) throws Exception {
        ClassUtils.BeanWrapper<?> _wrapper = ClassUtils.wrapper(paramType);
        if (_wrapper != null) {
            for (String _fName : _wrapper.getFieldNames()) {
                ParameterMeta _meta = new ParameterMeta(_wrapper.getField(_fName));
                if (_meta.isParamField()) {
                    Object _result = __doGetParamValue(owner, _meta, _meta.doBuildParamName(paramMeta.getPrefix(), _meta.getParamName(), _fName), _meta.getParamType(), _meta.getParamAnno());
                    if (_result != null) {
                        _wrapper.setValue(_fName, _meta.doParamEscape(_meta, _result));
                    }
                }
            }
            return _wrapper.getTargetObject();
        }
        return null;
    }
}
