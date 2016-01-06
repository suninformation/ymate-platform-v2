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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.WebEvent;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 通用请求分发器助手
 *
 * @author 刘镇 (suninformation@163.com) on 2013年8月18日 下午7:11:29
 * @version 1.0
 */
public final class GenericDispatcher {

    private IWebMvc __owner;

    public static GenericDispatcher create(IWebMvc webMvc) {
        return new GenericDispatcher(webMvc);
    }

    private GenericDispatcher(IWebMvc webMvc) {
        __owner = webMvc;
    }

    private void __doFireEvent(WebEvent.EVENT event, Object eventSource) {
        __owner.getOwner().getEvents()
                .fireEvent(Events.MODE.NORMAL, new WebEvent(__owner, event).addParamExtend(WebEvent.EVENT_SOURCE, eventSource));
    }

    public void execute(IRequestContext requestContext,
                        ServletContext servletContext,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException, ServletException {

        try {
            request.setCharacterEncoding(__owner.getModuleCfg().getDefaultCharsetEncoding());
            response.setCharacterEncoding(__owner.getModuleCfg().getDefaultCharsetEncoding());
            //
            response.setContentType(Type.ContentType.HTML.getContentType().concat(";charset=").concat(__owner.getModuleCfg().getDefaultCharsetEncoding()));
            //
            WebContext.create(__owner, requestContext, servletContext, request, response);
            //
            __doFireEvent(WebEvent.EVENT.REQUEST_RECEIVED, requestContext);
            //
            __owner.processRequest(requestContext, servletContext, request, response);
        } catch (Throwable e) {
            if (__owner.getModuleCfg().getErrorProcessor() != null) {
                __owner.getModuleCfg().getErrorProcessor().onError(__owner, e);
            } else {
                throw new ServletException(RuntimeUtils.unwrapThrow(e));
            }
        } finally {
            __doFireEvent(WebEvent.EVENT.REQUEST_COMPLETED, requestContext);
            WebContext.destroy();
        }
    }
}
