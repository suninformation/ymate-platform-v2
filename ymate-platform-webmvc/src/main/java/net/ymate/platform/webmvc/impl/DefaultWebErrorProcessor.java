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

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.WebResult;
import net.ymate.platform.webmvc.util.WebUtils;
import net.ymate.platform.webmvc.view.IView;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-10 13:54
 * @since 2.0.6
 */
public class DefaultWebErrorProcessor extends AbstractResponseErrorProcessor implements IWebErrorProcessor, IWebInitialization {

    private static final Log LOG = LogFactory.getLog(DefaultWebErrorProcessor.class);

    private IWebMvc owner;

    private boolean initialized;

    @Override
    public void initialize(IWebMvc owner) throws Exception {
        if (!initialized) {
            this.owner = owner;
            this.initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public IView showErrorMsg(IWebMvc owner, String code, String msg, Map<String, Object> dataMap) {
        doProcessErrorStatusCodeIfNeed(owner);
        HttpServletRequest httpServletRequest = WebContext.getRequest();
        String viewFormat = getErrorDefaultViewFormat(owner);
        if (WebUtils.isAjax(httpServletRequest) || WebUtils.isXmlFormat(httpServletRequest) || WebUtils.isJsonFormat(httpServletRequest) || StringUtils.containsAny(viewFormat, Type.Const.FORMAT_JSON, Type.Const.FORMAT_XML)) {
            return WebResult.formatView(WebResult.builder().code(code).msg(msg).data(dataMap).build(), viewFormat);
        }
        return WebUtils.buildErrorView(owner, null, code, msg, null, 0, dataMap);
    }

    @Override
    public void onError(IWebMvc owner, Throwable e) {
        try {
            IView resultView = processError(owner, e);
            if (resultView != null) {
                resultView.render();
            }
        } catch (Exception e1) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e1));
            }
        }
    }

    @Override
    public IView onValidation(IWebMvc owner, Map<String, ValidateResult> results) {
        return showValidationResults(owner, results);
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
        return getErrorDefaultViewFormat(owner);
    }

    public boolean isErrorWithStatusCode() {
        return isErrorWithStatusCode(owner);
    }

    public final boolean isAnalysisDisabled() {
        return isAnalysisDisabled(owner);
    }
}
