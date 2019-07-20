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

import com.alibaba.fastjson.JSON;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.exception.ValidationResultException;
import net.ymate.platform.webmvc.support.GenericResponseWrapper;
import net.ymate.platform.webmvc.util.*;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-10 13:54
 * @since 2.0.6
 */
public class DefaultWebErrorProcessor implements IWebErrorProcessor, IWebInitialization {

    private static final Log LOG = LogFactory.getLog(DefaultWebErrorProcessor.class);

    private IWebMvc owner;

    private String errorDefaultViewFormat;

    private boolean analysisDisabled;

    private boolean initialized;

    @Override
    public void initialize(WebMVC owner) throws Exception {
        this.owner = owner;
        //
        errorDefaultViewFormat = StringUtils.trimToEmpty(owner.getOwner().getParam(IWebMvcConfig.PARAMS_ERROR_DEFAULT_VIEW_FORMAT)).toLowerCase();
        analysisDisabled = BlurObject.bind(owner.getOwner().getParam(IWebMvcConfig.PARAMS_EXCEPTION_ANALYSIS_DISABLED)).toBooleanValue();
        //
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
    }

    public IView showErrorMsg(int code, String msg, Map<String, Object> dataMap) {
        if (WebUtils.isAjax(WebContext.getRequest(), true, true) || Type.Const.FORMAT_JSON.equals(getErrorDefaultViewFormat())) {
            return WebResult.formatView(WebResult.create(code).msg(msg).data(dataMap), Type.Const.FORMAT_JSON);
        }
        return WebUtils.buildErrorView(owner, code, msg).addAttribute(Type.Const.PARAM_DATA, dataMap);
    }

    @Override
    public void onError(IWebMvc owner, Throwable e) {
        try {
            Throwable unwrapThrow = RuntimeUtils.unwrapThrow(e);
            if (unwrapThrow != null) {
                if (unwrapThrow instanceof ValidationResultException) {
                    ValidationResultException exception = (ValidationResultException) unwrapThrow;
                    if (exception.getResultView() != null) {
                        exception.getResultView().render();
                    } else {
                        View.httpStatusView(exception.getHttpStatus(), exception.getMessage()).render();
                    }
                } else {
                    IExceptionProcessor exceptionProcessor = ExceptionProcessHelper.DEFAULT.bind(unwrapThrow.getClass());
                    if (exceptionProcessor != null) {
                        IExceptionProcessor.Result result = exceptionProcessor.process(unwrapThrow);
                        if (result != null) {
                            showErrorMsg(result.getCode(), WebUtils.errorCodeI18n(this.owner, result), null).render();
                        } else if (LOG.isErrorEnabled()) {
                            if (!analysisDisabled && owner.getOwner().isDevEnv()) {
                                LOG.error(exceptionAnalysis(unwrapThrow));
                            } else {
                                LOG.error(StringUtils.EMPTY, unwrapThrow);
                            }
                        }
                    } else {
                        if (LOG.isErrorEnabled()) {
                            if (!analysisDisabled && owner.getOwner().isDevEnv()) {
                                LOG.error(exceptionAnalysis(unwrapThrow));
                            } else {
                                LOG.error(StringUtils.EMPTY, unwrapThrow);
                            }
                        }
                        showErrorMsg(ErrorCode.INTERNAL_SYSTEM_ERROR, WebUtils.errorCodeI18n(this.owner, ErrorCode.INTERNAL_SYSTEM_ERROR, ErrorCode.MSG_INTERNAL_SYSTEM_ERROR), null).render();
                    }
                }
            }
        } catch (Exception e1) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e1));
            }
        }
    }

    @Override
    public IView onValidation(IWebMvc owner, Map<String, ValidateResult> results) {
        String message = WebUtils.errorCodeI18n(this.owner, ErrorCode.INVALID_PARAMS_VALIDATION, ErrorCode.MSG_INVALID_PARAMS_VALIDATION);
        Map<String, Object> dataMap = new HashMap<>(results.size());
        results.values().forEach((result) -> {
            dataMap.put(result.getName(), result.getMsg());
        });
        //
        if (!WebUtils.isAjax(WebContext.getRequest(), true, true) && !Type.Const.FORMAT_JSON.equals(getErrorDefaultViewFormat())) {
            // 拼装所有的验证消息
            message = WebUtils.messageWithTemplate(owner.getOwner(), message, results.values());
        }
        return showErrorMsg(ErrorCode.INVALID_PARAMS_VALIDATION, message, dataMap);
    }

    @Override
    public IView onConvention(IWebMvc owner, IRequestContext requestContext) throws Exception {
        return null;
    }

    // ----------

    public final IWebMvc getOwner() {
        return owner;
    }

    public final String getErrorDefaultViewFormat() {
        return errorDefaultViewFormat;
    }

    public final boolean isAnalysisDisabled() {
        return analysisDisabled;
    }

    // ----------

    public final String exceptionAnalysis(Throwable e) {
        StringBuilder stringBuilder = new StringBuilder("An exception occurred at ").append(DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS)).append(":\n");
        stringBuilder.append("-------------------------------------------------\n");
        stringBuilder.append("-- ThreadId: ").append(Thread.currentThread().getId()).append("\n");
        stringBuilder.append("-- RequestMapping: ").append(WebContext.getRequestContext().getRequestMapping()).append("\n");
        stringBuilder.append("-- ResponseStatus: ").append(((GenericResponseWrapper) WebContext.getResponse()).getStatus()).append("\n");
        stringBuilder.append("-- Method: ").append(WebContext.getRequestContext().getHttpMethod().name()).append("\n");
        stringBuilder.append("-- RemoteAddress: ").append(JSON.toJSONString(WebUtils.getRemoteAddresses(WebContext.getRequest()))).append("\n");
        //
        RequestMeta requestMeta = WebContext.getContext().getAttribute(RequestMeta.class.getName());
        if (requestMeta != null) {
            stringBuilder.append("-- Controller: ").append(requestMeta.getTargetClass().getName()).append(":").append(requestMeta.getMethod().getName()).append("\n");
        }
        //
        stringBuilder.append("-- ContextAttributes:").append("\n");
        WebContext.getContext().getAttributes().entrySet().stream()
                .filter(entry -> !StringUtils.startsWith(entry.getKey(), WebMVC.class.getPackage().getName()))
                .forEachOrdered(entry -> stringBuilder.append("\t  ").append(entry.getKey()).append(": ").append(JSON.toJSONString(entry.getValue())).append("\n"));
        //
        stringBuilder.append("-- Parameters:").append("\n");
        WebContext.getContext().getParameters().forEach((key, value) -> stringBuilder.append("\t  ").append(key).append(": ").append(JSON.toJSONString(value)).append("\n"));
        //
        stringBuilder.append("-- Attributes:").append("\n");
        Enumeration enumeration = WebContext.getRequest().getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String attrName = (String) enumeration.nextElement();
            stringBuilder.append("\t  ").append(attrName).append(": ").append(JSON.toJSONString(WebContext.getRequest().getAttribute(attrName))).append("\n");
        }
        //
        stringBuilder.append("-- Headers:").append("\n");
        enumeration = WebContext.getRequest().getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String headName = (String) enumeration.nextElement();
            if ("cookie".equalsIgnoreCase(headName)) {
                continue;
            }
            stringBuilder.append("\t  ").append(headName).append(": ").append(JSON.toJSONString(WebContext.getRequest().getHeader(headName))).append("\n");
        }
        //
        stringBuilder.append("-- Cookies:").append("\n");
        Cookie[] cookies = WebContext.getRequest().getCookies();
        if (cookies != null) {
            Arrays.stream(cookies).forEach(cookie -> stringBuilder.append("\t  ").append(cookie.getName()).append(": ").append(JSON.toJSONString(cookie.getValue())).append("\n"));
        }
        //
        stringBuilder.append("-- Session:").append("\n");
        WebContext.getContext().getSession().forEach((key, value) -> stringBuilder.append("\t  ").append(key).append(": ").append(JSON.toJSONString(value)).append("\n"));
        //
        stringBuilder.append(ExceptionProcessHelper.exceptionToString(e)).append("-------------------------------------------------\n");
        //
        return stringBuilder.toString();
    }
}
