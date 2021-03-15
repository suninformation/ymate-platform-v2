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

import net.ymate.platform.cache.Caches;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.webmvc.IWebCacheProcessor;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.PageCacheElement;
import net.ymate.platform.webmvc.annotation.ResponseCache;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.support.GenericResponseWrapper;
import net.ymate.platform.webmvc.util.WebCacheHelper;
import net.ymate.platform.webmvc.view.IView;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author 刘镇 (suninformation@163.com) on 16/2/1 上午3:11
 */
public class DefaultWebCacheProcessor implements IWebCacheProcessor {

    private static final Log LOG = LogFactory.getLog(DefaultWebCacheProcessor.class);

    private static final ReentrantLockHelper LOCK = new ReentrantLockHelper();

    @Override
    public boolean processResponseCache(IWebMvc owner, ResponseCache responseCache, IView resultView) throws Exception {
        HttpServletRequest request = WebContext.getRequest();
        GenericResponseWrapper response = (GenericResponseWrapper) WebContext.getResponse();

        String cacheKey = doBuildCacheKey(owner, request, responseCache);
        ICaches caches = owner.getOwner().getModuleManager().getModule(Caches.class);
        PageCacheElement cacheElement = (PageCacheElement) caches.get(responseCache.cacheName(), cacheKey);
        if (cacheElement == null && resultView != null) {
            // 仅缓存处理状态为200响应
            // TODO 注意：需要设置"webmvc.error_with_status_code"参数值为true，以对验证验证、异常错误等设置为非200响应码，否则会造成错误内容被缓存的情况
            if (response.getStatus() == HttpServletResponse.SC_OK) {
                cacheElement = putCacheElement(response, caches, responseCache, cacheKey, resultView);
            }
        }
        if (cacheElement != null) {
            // 输出内容
            WebCacheHelper.bind(request, response, cacheElement, responseCache.scope()).writeResponse();
            //
            return true;
        }
        return false;
    }

    private PageCacheElement putCacheElement(GenericResponseWrapper response, ICaches caches, ResponseCache responseCache, String cacheKey, IView resultView) throws Exception {
        PageCacheElement cacheElement = null;
        ReentrantLock locker = LOCK.getLocker(cacheKey);
        locker.lock();
        try {
            // 尝试读取缓存
            cacheElement = (PageCacheElement) caches.get(responseCache.cacheName(), cacheKey);
            // 若缓存内容不存在或已过期
            if (cacheElement == null || cacheElement.isExpired()) {
                // 重新生成缓存内容
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                resultView.render(outputStream);

                cacheElement = new PageCacheElement(StringUtils.defaultIfBlank(resultView.getContentType(), response.getContentType()), response.getHeaders(), outputStream.toByteArray(), responseCache.useGZip());
                // 计算超时时间
                int timeout = responseCache.timeout() > 0 ? responseCache.timeout() : caches.getConfig().getDefaultCacheTimeout();
                if (timeout > 0) {
                    cacheElement.setTimeout(timeout);
                }
                // 存入缓存
                caches.put(responseCache.cacheName(), cacheKey, cacheElement);
            }
        } catch (UnsupportedOperationException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("%s Unsupported Render To OutputStream Operation, Skip Cache.", resultView.getClass().getName()));
            }
        } finally {
            locker.unlock();
        }
        return cacheElement;
    }

    private String doBuildCacheKey(IWebMvc owner, HttpServletRequest request, ResponseCache responseCache) {
        // 计算缓存KEY值
        StringBuilder stringBuilder = new StringBuilder()
                .append(ResponseCache.class.getSimpleName()).append(":").append(owner.getOwner().getI18n().current());
        if (StringUtils.isNotBlank(responseCache.key())) {
            // 若指定的缓存KEY中存在变量则尝试从请求参数中获取并替换
            ExpressionUtils expressionUtils = ExpressionUtils.bind(responseCache.key());
            for (String var : expressionUtils.getVariables()) {
                expressionUtils.set(var, request.getParameter(var));
            }
            stringBuilder.append(":").append(expressionUtils.clean().getResult());
        } else {
            // TODO 当前这种KEY生成方式存在的问题是请求参数数量由请求端控制，可能存在安全风险
            stringBuilder
                    .append(":").append(request.getMethod())
                    .append(":").append(request.getRequestURI())
                    .append(":").append(request.getQueryString());
        }
        if (responseCache.scope().equals(ICaches.Scope.SESSION)) {
            stringBuilder.insert(0, "|").insert(0, request.getSession().getId());
        }
        return DigestUtils.md5Hex(stringBuilder.toString());
    }
}
