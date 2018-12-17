/*
 * Copyright 2007-2018 the original author or authors.
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

import com.alibaba.fastjson.JSON;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.exception.ValidationResultException;
import net.ymate.platform.webmvc.support.GenericResponseWrapper;
import net.ymate.platform.webmvc.util.*;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-10 13:54
 * @version 1.0
 * @since 2.0.6
 */
public class DefaultWebErrorProcessor implements IWebErrorProcessor, IWebInitializable {

    private static final Log _LOG = LogFactory.getLog(DefaultWebErrorProcessor.class);

    private IWebMvc __owner;

    private String __errorDefaultViewFormat;

    private boolean __analysisDisabled;

    @Override
    public void init(WebMVC owner) throws Exception {
        __owner = owner;
        //
        __errorDefaultViewFormat = StringUtils.trimToEmpty(owner.getOwner().getConfig().getParam(IWebMvcModuleCfg.PARAMS_ERROR_DEFAULT_VIEW_FORMAT)).toLowerCase();
        __analysisDisabled = BlurObject.bind(owner.getOwner().getConfig().getParam(IWebMvcModuleCfg.PARAMS_EXCEPTION_ANALYSIS_DISABLED)).toBooleanValue();
    }

    @Override
    public void destroy() throws Exception {
    }

    public IView showErrorMsg(int code, String msg, Map<String, Object> dataMap) {
        if (WebUtils.isAjax(WebContext.getRequest(), true, true) || Type.Const.FORMAT_JSON.equals(getErrorDefaultViewFormat())) {
            return WebResult.formatView(WebResult.create(code).msg(msg).data(dataMap), Type.Const.FORMAT_JSON);
        }
        return WebUtils.buildErrorView(__owner, code, msg).addAttribute(Type.Const.PARAM_DATA, dataMap);
    }

    @Override
    public void onError(IWebMvc owner, Throwable e) {
        try {
            Throwable _unwrapThrow = RuntimeUtils.unwrapThrow(e);
            if (_unwrapThrow instanceof ValidationResultException) {
                ValidationResultException _exception = (ValidationResultException) _unwrapThrow;
                if (_exception.getResultView() != null) {
                    _exception.getResultView().render();
                } else {
                    View.httpStatusView(_exception.getHttpStatus(), _exception.getMessage()).render();
                }
            } else {
                IExceptionProcessor _processor = ExceptionProcessHelper.DEFAULT.bind(_unwrapThrow.getClass());
                if (_processor != null) {
                    IExceptionProcessor.Result _result = _processor.process(_unwrapThrow);
                    showErrorMsg(_result.getCode(), WebUtils.errorCodeI18n(__owner, _result), null).render();
                } else {
                    if (!__analysisDisabled && owner.getOwner().getConfig().isDevelopMode()) {
                        _LOG.error(exceptionAnalysis(_unwrapThrow));
                    } else {
                        _LOG.error("", _unwrapThrow);
                    }
                    showErrorMsg(ErrorCode.INTERNAL_SYSTEM_ERROR, WebUtils.errorCodeI18n(__owner, ErrorCode.INTERNAL_SYSTEM_ERROR, ErrorCode.MSG_INTERNAL_SYSTEM_ERROR), null).render();
                }
            }
        } catch (Throwable e1) {
            _LOG.warn("", RuntimeUtils.unwrapThrow(e1));
        }
    }

    @Override
    public IView onValidation(IWebMvc owner, Map<String, ValidateResult> results) {
        String _message = WebUtils.errorCodeI18n(__owner, ErrorCode.INVALID_PARAMS_VALIDATION, ErrorCode.MSG_INVALID_PARAMS_VALIDATION);
        Map<String, Object> _dataMap = new HashMap<String, Object>();
        for (ValidateResult _vResult : results.values()) {
            _dataMap.put(_vResult.getName(), _vResult.getMsg());
        }
        //
        if (!WebUtils.isAjax(WebContext.getRequest(), true, true) && !Type.Const.FORMAT_JSON.equals(getErrorDefaultViewFormat())) {
            // 拼装所有的验证消息
            _message = WebUtils.messageWithTemplate(owner.getOwner(), _message, results.values());
        }
        return showErrorMsg(ErrorCode.INVALID_PARAMS_VALIDATION, _message, _dataMap);
    }

    @Override
    public IView onConvention(IWebMvc owner, IRequestContext requestContext) throws Exception {
        return null;
    }

    // ----------

    public final IWebMvc getOwner() {
        return __owner;
    }

    public final String getErrorDefaultViewFormat() {
        return __errorDefaultViewFormat;
    }

    public final boolean isAnalysisDisabled() {
        return __analysisDisabled;
    }

    // ----------

    public final String exceptionAnalysis(Throwable e) {
        StringBuilder _errSB = new StringBuilder("An exception occurred at ").append(DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS)).append(":\n");
        _errSB.append("-------------------------------------------------\n");
        _errSB.append("-- ThreadId: ").append(Thread.currentThread().getId()).append("\n");
        _errSB.append("-- RequestMapping: ").append(WebContext.getRequestContext().getRequestMapping()).append("\n");
        _errSB.append("-- ResponseStatus: ").append(((GenericResponseWrapper) WebContext.getResponse()).getStatus()).append("\n");
        _errSB.append("-- Method: ").append(WebContext.getRequestContext().getHttpMethod().name()).append("\n");
        _errSB.append("-- RemoteAddrs: ").append(JSON.toJSONString(WebUtils.getRemoteAddrs(WebContext.getRequest()))).append("\n");
        RequestMeta _meta = WebContext.getContext().getAttribute(RequestMeta.class.getName());
        if (_meta != null) {
            _errSB.append("-- Controller: ").append(_meta.getTargetClass().getName()).append(":").append(_meta.getMethod().getName()).append("\n");
        }
        _errSB.append("-- ContextAttributes:").append("\n");
        for (Map.Entry<String, Object> _entry : WebContext.getContext().getAttributes().entrySet()) {
            if (!StringUtils.startsWith(_entry.getKey(), WebMVC.class.getPackage().getName())) {
                _errSB.append("\t  ").append(_entry.getKey()).append(": ").append(JSON.toJSONString(_entry.getValue())).append("\n");
            }
        }
        _errSB.append("-- Parameters:").append("\n");
        for (Map.Entry<String, Object> _entry : WebContext.getContext().getParameters().entrySet()) {
            _errSB.append("\t  ").append(_entry.getKey()).append(": ").append(JSON.toJSONString(_entry.getValue())).append("\n");
        }
        _errSB.append("-- Attributes:").append("\n");
        Enumeration _enum = WebContext.getRequest().getAttributeNames();
        while (_enum.hasMoreElements()) {
            String _attrName = (String) _enum.nextElement();
            _errSB.append("\t  ").append(_attrName).append(": ").append(JSON.toJSONString(WebContext.getRequest().getAttribute(_attrName))).append("\n");
        }
        _errSB.append("-- Headers:").append("\n");
        _enum = WebContext.getRequest().getHeaderNames();
        while (_enum.hasMoreElements()) {
            String _headName = (String) _enum.nextElement();
            if ("cookie".equalsIgnoreCase(_headName)) {
                continue;
            }
            _errSB.append("\t  ").append(_headName).append(": ").append(JSON.toJSONString(WebContext.getRequest().getHeader(_headName))).append("\n");
        }
        _errSB.append("-- Cookies:").append("\n");
        Cookie[] _cookies = WebContext.getRequest().getCookies();
        if (_cookies != null) {
            for (Cookie _cookie : _cookies) {
                _errSB.append("\t  ").append(_cookie.getName()).append(": ").append(JSON.toJSONString(_cookie.getValue())).append("\n");
            }
        }
        _errSB.append("-- Session:").append("\n");
        for (Map.Entry<String, Object> _entry : WebContext.getContext().getSession().entrySet()) {
            _errSB.append("\t  ").append(_entry.getKey()).append(": ").append(JSON.toJSONString(_entry.getValue())).append("\n");
        }
        _errSB.append(ExceptionProcessHelper.exceptionToString(e));
        _errSB.append("-------------------------------------------------\n");
        //
        return _errSB.toString();
    }
}
