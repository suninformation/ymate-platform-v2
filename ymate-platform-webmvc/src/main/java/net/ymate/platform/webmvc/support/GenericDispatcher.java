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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IWebErrorProcessor;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.WebEvent;
import net.ymate.platform.webmvc.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通用请求分发器助手
 *
 * @author 刘镇 (suninformation@163.com) on 2013年8月18日 下午7:11:29
 */
public final class GenericDispatcher {

    private final IWebMvc owner;

    private final IWebErrorProcessor errorProcessor;

    public static GenericDispatcher create(IWebMvc owner) {
        return new GenericDispatcher(owner);
    }

    private GenericDispatcher(IWebMvc owner) {
        this.owner = owner;
        errorProcessor = owner.getConfig().getErrorProcessor();
    }

    private void doFireEvent(WebEvent.EVENT event, Object eventSource) {
        owner.getOwner().getEvents().fireEvent(new WebEvent(owner, event).addParamExtend(WebEvent.EVENT_SOURCE, eventSource));
    }

    public void execute(IRequestContext requestContext,
                        ServletContext servletContext,
                        HttpServletRequest request,
                        HttpServletResponse response) throws ServletException {
        try {
            //
            WebContext.create(owner, requestContext, servletContext, request, response);
            //
            doFireEvent(WebEvent.EVENT.REQUEST_RECEIVED, requestContext);
            //
            owner.processRequest(requestContext, servletContext, request, response);
        } catch (Exception e) {
            if (errorProcessor != null) {
                errorProcessor.onError(owner, e);
            } else {
                throw new ServletException(RuntimeUtils.unwrapThrow(e));
            }
        } finally {
            doFireEvent(WebEvent.EVENT.REQUEST_COMPLETED, requestContext);
            ValidateContext.removeLocalAttributes();
            WebContext.destroy();
        }
    }
}
