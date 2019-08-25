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

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用基础过滤器 (from ymate-framework-core)
 * <p>
 * 目前支持: 自定义响应头，在 HTTP 响应头信息中的 X-Frame-Options，可以指示浏览器是否应该加载一个 iframe 中的页面。
 * 如果服务器响应头信息中没有 X-Frame-Options，则该网站存在 ClickJacking 攻击风险。
 * 网站可以通过设置 X-Frame-Options 阻止站点内的页面被其他页面嵌入从而防止点击劫持。
 * 添加 X-Frame-Options 响应头，赋值有如下三种：
 * 1、DENY: 不能被嵌入到任何iframe或者frame中。
 * 2、SAMEORIGIN: 页面只能被本站页面嵌入到iframe或者frame中。
 * 3、ALLOW-FROM uri: 只能被嵌入到指定域名的框架中。
 * <p>
 * 例如：
 * <pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;responseHeaders&lt;/param-name&gt;
 *         &lt;param-value&gt;X-Frame-Options=SAMEORIGIN&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * </pre>
 * 注：多个响应头之间采用'|'分隔
 *
 * @author 刘镇 (suninformation@163.com) on 2018/10/10 下午5:38
 * @since 2.1.0
 */
public class GeneralWebFilter implements Filter {

    private Map<String, PairObject<Type.HeaderType, Object>> responseHeaders;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String[] responseHeadersArr = StringUtils.split(StringUtils.defaultIfBlank(filterConfig.getInitParameter("responseHeaders"), filterConfig.getInitParameter("response_headers")), "|");
        if (ArrayUtils.isNotEmpty(responseHeadersArr)) {
            responseHeaders = new HashMap<>(responseHeadersArr.length);
            for (String header : responseHeadersArr) {
                String[] headerArr = StringUtils.split(header, "=");
                if (headerArr != null && headerArr.length == 2) {
                    responseHeaders.put(headerArr[0], new PairObject<>(NumberUtils.isDigits(headerArr[1]) ? Type.HeaderType.INT : Type.HeaderType.STRING, headerArr[1]));
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (responseHeaders != null && !responseHeaders.isEmpty()) {
            responseHeaders.forEach((key, value) -> {
                if (value.getKey() == Type.HeaderType.INT) {
                    ((HttpServletResponse) response).addIntHeader(key, BlurObject.bind(value.getValue()).toIntValue());
                } else {
                    ((HttpServletResponse) response).addHeader(key, BlurObject.bind(value.getValue()).toStringValue());
                }
            });
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        responseHeaders.clear();
        responseHeaders = null;
    }
}
