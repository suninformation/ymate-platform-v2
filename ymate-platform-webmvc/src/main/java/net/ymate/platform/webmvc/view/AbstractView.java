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
package net.ymate.platform.webmvc.view;

import net.ymate.platform.commons.util.ParamUtils;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象视图
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-20 下午6:56:56
 */
public abstract class AbstractView implements IView {

    private static final long serialVersionUID = 1L;

    protected static volatile String baseViewPath;

    protected HttpServletResponse response = WebContext.getResponse();

    protected Map<String, Object> attributes = new HashMap<>();

    protected String contentType;

    public static String getBaseViewPath(IWebMvc owner) {
        String viewPath = baseViewPath;
        if (viewPath == null) {
            synchronized (AbstractView.class) {
                viewPath = baseViewPath;
                if (viewPath == null) {
                    String path = StringUtils.trimToEmpty(owner.getConfig().getBaseViewPath());
                    // 模板基准路径并以'/WEB-INF'开始，以'/'结束
                    if (!path.endsWith(Type.Const.PATH_SEPARATOR)) {
                        path += Type.Const.PATH_SEPARATOR;
                    }
                    baseViewPath = viewPath = path;
                }
            }
        }
        return viewPath;
    }

    public AbstractView() {
    }

    @Override
    public IView addAttribute(String name, Object value) {
        if (!ParamUtils.isInvalid(value)) {
            attributes.put(name, value);
        }
        return this;
    }

    @Override
    public IView addAttributes(Map<String, Object> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            this.attributes.putAll(attributes);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public IView setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public IView addDateHeader(String name, long date) {
        if (response.containsHeader(name)) {
            response.addDateHeader(name, date);
        } else {
            response.setDateHeader(name, date);
        }
        return this;
    }

    @Override
    public IView addHeader(String name, String value) {
        if (response.containsHeader(name)) {
            response.addHeader(name, value);
        } else {
            response.setHeader(name, value);
        }
        return this;
    }

    @Override
    public IView addIntHeader(String name, int value) {
        if (response.containsHeader(name)) {
            response.addIntHeader(name, value);
        } else {
            response.setIntHeader(name, value);
        }
        return this;
    }

    @Override
    public void render() throws Exception {
        if (response.isCommitted()) {
            return;
        }
        if (StringUtils.isNotBlank(contentType)) {
            response.setContentType(contentType);
        }
        doRenderView();
    }

    /**
     * 初始化配置参数(全局)
     *
     * @param owner 所属WebMVC框架管理器
     */
    protected void doViewInit(IWebMvc owner) {
        getBaseViewPath(owner);
    }

    /**
     * 视图渲染具体操作
     *
     * @throws Exception 抛出任何可能异常
     */
    protected abstract void doRenderView() throws Exception;

    @Override
    public void render(OutputStream output) throws Exception {
        throw new UnsupportedOperationException();
    }

    protected String doProcessPath(String path, String suffix, boolean isJspView) {
        if (StringUtils.isNotBlank(contentType)) {
            WebContext.getResponse().setContentType(contentType);
        }
        if (StringUtils.isBlank(path)) {
            String requestMapping = WebContext.getRequestContext().getRequestMapping();
            if (requestMapping.endsWith(Type.Const.PATH_SEPARATOR)) {
                requestMapping = requestMapping.substring(0, requestMapping.length() - 1);
            }
            path = (isJspView ? baseViewPath : StringUtils.EMPTY) + requestMapping + StringUtils.trimToEmpty(suffix);
        } else {
            if (path.charAt(0) != Type.Const.PATH_SEPARATOR_CHAR) {
                path = Type.Const.PATH_SEPARATOR_CHAR + path;
            }
            if (isJspView) {
                if (!path.startsWith(baseViewPath)) {
                    path = baseViewPath + path.substring(1);
                }
                if (!path.contains("?") && !path.endsWith(suffix)) {
                    path += suffix;
                }
            } else {
                if (path.startsWith(baseViewPath)) {
                    path = StringUtils.substringAfter(path, baseViewPath);
                }
                if (StringUtils.isNotBlank(suffix) && !path.endsWith(suffix)) {
                    path += suffix;
                }
            }
        }
        return path;
    }

    /**
     * @param url 原始URL路径
     * @return 将参数与URL地址进行绑定
     * @throws UnsupportedEncodingException URL编码异常
     */
    protected String buildUrl(String url) throws UnsupportedEncodingException {
        if (attributes.isEmpty()) {
            return url;
        }
        StringBuilder stringBuilder = new StringBuilder(url);
        if (!url.contains("?")) {
            stringBuilder.append("?");
        } else {
            stringBuilder.append("&");
        }
        String characterEncoding = WebContext.getRequest().getCharacterEncoding();
        boolean flag = true;
        for (Map.Entry<String, Object> attrEntry : attributes.entrySet()) {
            if (flag) {
                flag = false;
            } else {
                stringBuilder.append("&");
            }
            stringBuilder.append(attrEntry.getKey()).append("=");
            if (attrEntry.getValue() != null && StringUtils.isNotBlank(attrEntry.getValue().toString())) {
                stringBuilder.append(URLEncoder.encode(attrEntry.getValue().toString(), characterEncoding));
            }
        }
        return stringBuilder.toString();
    }
}
