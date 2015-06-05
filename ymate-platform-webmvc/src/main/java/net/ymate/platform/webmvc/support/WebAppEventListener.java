/*
 * Copyright 2007-2107 the original author or authors.
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

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.WebEvent;
import net.ymate.platform.webmvc.WebMVC;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * WebMVC框架初始化及上下文事件监听器(初始化YMP框架)
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-7 下午8:33:43
 * @version 1.0
 */
public class WebAppEventListener implements ServletContextListener, ServletContextAttributeListener,
        HttpSessionListener, HttpSessionAttributeListener, HttpSessionBindingListener,
        ServletRequestListener, ServletRequestAttributeListener {

    static {
        try {
            YMP.get().init();
        } catch (Exception ex) {
            throw new RuntimeException(RuntimeUtils.unwrapThrow(ex));
        }
    }

    private void __doFireEvent(WebEvent.EVENT event, Object eventSource) {
        YMP.get().getEvents()
                .fireEvent(Events.MODE.NORMAL, new WebEvent(WebMVC.get(), event).addParamExtend(WebEvent.EVENT_SOURCE, eventSource));
    }

    //// ServletContextListener

    public void contextInitialized(ServletContextEvent sce) {
        __doFireEvent(WebEvent.EVENT.SERVLET_CONTEXT_INITED, sce);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        __doFireEvent(WebEvent.EVENT.SERVLET_CONTEXT_DESTROYED, sce);
        try {
            YMP.get().destroy();
        } catch (Exception ex) {
            throw new RuntimeException(RuntimeUtils.unwrapThrow(ex));
        }
    }

    //// ServletContextAttributeListener

    public void attributeAdded(ServletContextAttributeEvent scab) {
        __doFireEvent(WebEvent.EVENT.SERVLET_CONTEXT_ATTR_ADDED, scab);
    }

    public void attributeRemoved(ServletContextAttributeEvent scab) {
        __doFireEvent(WebEvent.EVENT.SERVLET_CONTEXT_ATTR_REMOVEED, scab);
    }

    public void attributeReplaced(ServletContextAttributeEvent scab) {
        __doFireEvent(WebEvent.EVENT.SERVLET_CONTEXT_ATTR_REPLACED, scab);
    }

    //// HttpSessionListener

    public void sessionCreated(HttpSessionEvent se) {
        __doFireEvent(WebEvent.EVENT.SESSION_CREATED, se);
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        __doFireEvent(WebEvent.EVENT.SESSION_DESTROYED, se);
    }

    //// HttpSessionAttributeListener

    public void attributeAdded(HttpSessionBindingEvent se) {
        __doFireEvent(WebEvent.EVENT.SESSION_ATTR_ADDED, se);
    }

    public void attributeRemoved(HttpSessionBindingEvent se) {
        __doFireEvent(WebEvent.EVENT.SESSION_ATTR_REMOVEED, se);
    }

    public void attributeReplaced(HttpSessionBindingEvent se) {
        __doFireEvent(WebEvent.EVENT.SESSION_ATTR_REPLACED, se);
    }

    //// HttpSessionBindingListener

    public void valueBound(HttpSessionBindingEvent event) {
        __doFireEvent(WebEvent.EVENT.SESSION_VALUE_BOUND, event);
    }

    public void valueUnbound(HttpSessionBindingEvent event) {
        __doFireEvent(WebEvent.EVENT.SESSION_VALUE_UNBOUND, event);
    }

    //// ServletRequestListener

    public void requestInitialized(ServletRequestEvent sre) {
        __doFireEvent(WebEvent.EVENT.REQUEST_INITED, sre);
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        __doFireEvent(WebEvent.EVENT.REQUEST_DESTROYED, sre);
    }

    //// ServletRequestAttributeListener

    public void attributeAdded(ServletRequestAttributeEvent srae) {
        __doFireEvent(WebEvent.EVENT.REQUEST_ATTR_ADDED, srae);
    }

    public void attributeRemoved(ServletRequestAttributeEvent srae) {
        __doFireEvent(WebEvent.EVENT.REQUEST_ATTR_REMOVEED, srae);
    }

    public void attributeReplaced(ServletRequestAttributeEvent srae) {
        __doFireEvent(WebEvent.EVENT.REQUEST_ATTR_REPLACED, srae);
    }
}
