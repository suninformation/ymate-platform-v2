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
package net.ymate.platform.webmvc;

import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.exception.ValidationResultException;
import net.ymate.platform.webmvc.util.ExceptionProcessHelper;
import net.ymate.platform.webmvc.util.IExceptionProcessor;
import net.ymate.platform.webmvc.util.WebErrorCode;
import net.ymate.platform.webmvc.util.WebUtils;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/12/29 8:37 下午
 * @since 2.1.0
 */
public abstract class AbstractResponseErrorProcessor implements IResponseErrorProcessor {

    private static final Log LOG = LogFactory.getLog(AbstractResponseErrorProcessor.class);

    private String errorDefaultViewFormat;

    private Boolean errorWithStatusCode;

    private Boolean analysisDisabled;

    public AbstractResponseErrorProcessor() {
    }

    @Override
    public IView processError(IWebMvc owner, Throwable e) {
        IView returnView = null;
        try {
            Throwable unwrapThrow = RuntimeUtils.unwrapThrow(e);
            if (unwrapThrow != null) {
                if (unwrapThrow instanceof ValidationResultException) {
                    ValidationResultException exception = (ValidationResultException) unwrapThrow;
                    if (exception.getValidateResults() != null && !exception.getValidateResults().isEmpty()) {
                        returnView = showValidationResults(owner, exception.getValidateResults());
                    } else if (exception.getResultView() != null) {
                        returnView = exception.getResultView();
                        doProcessErrorStatusCodeIfNeed(owner);
                    } else {
                        returnView = View.httpStatusView(exception.getHttpStatus(), exception.getMessage());
                        doProcessErrorStatusCodeIfNeed(owner);
                    }
                } else {
                    IExceptionProcessor exceptionProcessor = ExceptionProcessHelper.DEFAULT.bind(unwrapThrow.getClass());
                    if (exceptionProcessor != null) {
                        IExceptionProcessor.Result result = exceptionProcessor.process(unwrapThrow);
                        if (result != null) {
                            returnView = showErrorMsg(owner, result.getCode(), WebUtils.errorCodeI18n(owner, result), result.getAttributes());
                        } else {
                            doProcessError(owner, unwrapThrow);
                        }
                    } else {
                        doProcessError(owner, unwrapThrow);
                        returnView = showErrorMsg(owner, String.valueOf(ErrorCode.INTERNAL_SYSTEM_ERROR), WebUtils.errorCodeI18n(owner, ErrorCode.INTERNAL_SYSTEM_ERROR, ErrorCode.MSG_INTERNAL_SYSTEM_ERROR), null);
                    }
                    doProcessErrorStatusCodeIfNeed(owner);
                }
            }
        } catch (Exception e1) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e1));
            }
        }
        return returnView;
    }

    public IView showValidationResults(IWebMvc owner, Map<String, ValidateResult> results) {
        doProcessErrorStatusCodeIfNeed(owner);
        String message = WebUtils.errorCodeI18n(owner, WebErrorCode.INVALID_PARAMS_VALIDATION, WebErrorCode.MSG_INVALID_PARAMS_VALIDATION);
        //
        HttpServletRequest httpServletRequest = WebContext.getRequest();
        if (!WebUtils.isAjax(httpServletRequest) && !WebUtils.isXmlFormat(httpServletRequest) && !WebUtils.isJsonFormat(httpServletRequest) && !StringUtils.containsAny(getErrorDefaultViewFormat(owner), Type.Const.FORMAT_JSON, Type.Const.FORMAT_XML)) {
            // 拼装所有的验证消息
            message = WebUtils.messageWithTemplate(owner.getOwner(), message, results.values());
        }
        Map<String, Object> dataMap = results.values().stream().collect(Collectors.toMap(ValidateResult::getName, ValidateResult::getMsg, (a, b) -> b, () -> new HashMap<>(results.size())));
        return showErrorMsg(owner, String.valueOf(WebErrorCode.INVALID_PARAMS_VALIDATION), message, dataMap);
    }

    public abstract IView showErrorMsg(IWebMvc owner, String code, String msg, Map<String, Object> dataMap);

    protected void doProcessError(IWebMvc owner, Throwable unwrapThrow) {
        WebEvent eventContext = (WebEvent) new WebEvent(owner, WebEvent.EVENT.REQUEST_UNEXPECTED_ERROR).setEventSource(unwrapThrow);
        if (LOG.isErrorEnabled()) {
            if (!isAnalysisDisabled(owner) && owner.getOwner().isDevEnv()) {
                String errMsg = exceptionAnalysis(unwrapThrow);
                LOG.error(errMsg);
                eventContext.addParamExtend("errorMessage", errMsg);
            } else {
                LOG.error(StringUtils.EMPTY, unwrapThrow);
            }
        }
        owner.getOwner().getEvents().fireEvent(eventContext);
    }

    protected void doProcessErrorStatusCodeIfNeed(IWebMvc owner) {
        if (isErrorWithStatusCode(owner)) {
            HttpServletResponse httpServletResponse = WebContext.getResponse();
            if (httpServletResponse.getStatus() == HttpServletResponse.SC_OK) {
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    public String exceptionAnalysis(Throwable e) {
        StringBuilder stringBuilder = new StringBuilder("An exception occurred at ").append(DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS)).append(":\n");
        stringBuilder.append("-------------------------------------------------\n");
        stringBuilder.append("-- ThreadId: ").append(Thread.currentThread().getId()).append("\n");
        stringBuilder.append("-- RequestMapping: ").append(WebContext.getRequestContext().getRequestMapping()).append("\n");
        stringBuilder.append("-- ResponseStatus: ").append((WebContext.getResponse()).getStatus()).append("\n");
        stringBuilder.append("-- Method: ").append(WebContext.getRequestContext().getHttpMethod().name()).append("\n");
        stringBuilder.append("-- RemoteAddress: ").append(JsonWrapper.toJsonString(WebUtils.getRemoteAddresses(WebContext.getRequest()), false, true)).append("\n");
        //
        RequestMeta requestMeta = WebContext.getContext().getAttribute(RequestMeta.class.getName());
        if (requestMeta != null) {
            stringBuilder.append("-- Controller: ").append(requestMeta.getTargetClass().getName()).append(":").append(requestMeta.getMethod().getName()).append("\n");
        }
        //
        stringBuilder.append("-- ContextAttributes:").append("\n");
        WebContext.getContext().getAttributes().entrySet().stream()
                .filter(entry -> !StringUtils.startsWith(entry.getKey(), WebMVC.class.getPackage().getName()))
                .forEachOrdered(entry -> stringBuilder.append("\t  ").append(entry.getKey()).append(": ").append(JsonWrapper.toJsonString(entry.getValue(), false, true)).append("\n"));
        //
        stringBuilder.append("-- Parameters:").append("\n");
        WebContext.getContext().getParameters().forEach((key, value) -> stringBuilder.append("\t  ").append(key).append(": ").append(JsonWrapper.toJsonString(value, false, true)).append("\n"));
        //
        stringBuilder.append("-- Attributes:").append("\n");
        Enumeration<?> enumeration = WebContext.getRequest().getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String attrName = (String) enumeration.nextElement();
            stringBuilder.append("\t  ").append(attrName).append(": ").append(JsonWrapper.toJsonString(WebContext.getRequest().getAttribute(attrName), false, true)).append("\n");
        }
        //
        stringBuilder.append("-- Headers:").append("\n");
        enumeration = WebContext.getRequest().getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String headName = (String) enumeration.nextElement();
            if ("cookie".equalsIgnoreCase(headName)) {
                continue;
            }
            stringBuilder.append("\t  ").append(headName).append(": ").append(JsonWrapper.toJsonString(WebContext.getRequest().getHeader(headName), false, true)).append("\n");
        }
        //
        stringBuilder.append("-- Cookies:").append("\n");
        Cookie[] cookies = WebContext.getRequest().getCookies();
        if (cookies != null) {
            Arrays.stream(cookies).forEach(cookie -> stringBuilder.append("\t  ").append(cookie.getName()).append(": ").append(JsonWrapper.toJsonString(cookie.getValue(), false, true)).append("\n"));
        }
        //
        stringBuilder.append("-- Session:").append("\n");
        WebContext.getContext().getSession().forEach((key, value) -> stringBuilder.append("\t  ").append(key).append(": ").append(JsonWrapper.toJsonString(value, false, true)).append("\n"));
        //
        stringBuilder.append(RuntimeUtils.exceptionToString(e)).append("-------------------------------------------------\n");
        //
        return stringBuilder.toString();
    }

    public String getErrorDefaultViewFormat(IWebMvc owner) {
        if (errorDefaultViewFormat == null) {
            errorDefaultViewFormat = StringUtils.trimToEmpty(owner.getOwner().getParam(IWebMvcConfig.PARAMS_ERROR_DEFAULT_VIEW_FORMAT)).toLowerCase();
        }
        return errorDefaultViewFormat;
    }

    public void setErrorDefaultViewFormat(String errorDefaultViewFormat) {
        this.errorDefaultViewFormat = errorDefaultViewFormat;
    }

    public boolean isErrorWithStatusCode(IWebMvc owner) {
        if (errorWithStatusCode == null) {
            errorWithStatusCode = BlurObject.bind(owner.getOwner().getParam(IWebMvcConfig.PARAMS_ERROR_WITH_STATUS_CODE)).toBooleanValue();
        }
        return errorWithStatusCode;
    }

    public void setErrorWithStatusCode(boolean errorWithStatusCode) {
        this.errorWithStatusCode = errorWithStatusCode;
    }

    public boolean isAnalysisDisabled(IWebMvc owner) {
        if (analysisDisabled == null) {
            analysisDisabled = BlurObject.bind(owner.getOwner().getParam(IWebMvcConfig.PARAMS_EXCEPTION_ANALYSIS_DISABLED)).toBooleanValue();
        }
        return analysisDisabled;
    }

    public void setAnalysisDisabled(boolean analysisDisabled) {
        this.analysisDisabled = analysisDisabled;
    }
}
