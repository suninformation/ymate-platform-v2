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

import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IRequestMappingParser;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * 默认基于RESTFul风格的WebMVC请求映射路径分析器
 *
 * @author 刘镇 (suninformation@163.com) on 2011-7-26 上午11:11:45
 */
public class DefaultRequestMappingParser implements IRequestMappingParser {

    private static final Log LOG = LogFactory.getLog(DefaultRequestMappingParser.class);

    private final Map<Type.HttpMethod, Map<String, RequestMeta>> exactMappingMap = new EnumMap<>(Type.HttpMethod.class);

    private final Map<Type.HttpMethod, RadixTreeRouter> routers = new EnumMap<>(Type.HttpMethod.class);

    public DefaultRequestMappingParser() {
        for (Type.HttpMethod method : Type.HttpMethod.values()) {
            exactMappingMap.put(method, new HashMap<>());
            routers.put(method, new RadixTreeRouter());
        }
    }

    @Override
    public final void registerRequestMeta(RequestMeta requestMeta) {
        for (Type.HttpMethod httpMethod : requestMeta.getAllowMethods()) {
            Map<String, RequestMeta> exactMap = exactMappingMap.get(httpMethod);
            RequestMeta prev = exactMap.put(requestMeta.getMapping(), requestMeta);
            if (prev != null && LOG.isWarnEnabled()) {
                LOG.warn(String.format("--> %s: %s : %s.%s has been replaced!", httpMethod, prev.getMapping(), prev.getTargetClass().getName(), prev.getMethod().getName()));
            }
            // 同时注册到 Trie 路由树，用于变量路径的 O(k) 匹配
            routers.get(httpMethod).register(requestMeta.getMapping(), requestMeta);
        }
    }

    @Override
    public Map<String, RequestMeta> getRequestMetas(Type.HttpMethod httpMethod) {
        return Collections.unmodifiableMap(exactMappingMap.get(httpMethod));
    }

    @Override
    public final RequestMeta parse(IRequestContext context) {
        if (Strings.CS.containsAny(context.getRequestMapping(), "{", "}")) {
            return null;
        }
        String mapping = context.getRequestMapping();
        Type.HttpMethod method = context.getHttpMethod();
        // 快速路径：精确查找（O(1)）
        Map<String, RequestMeta> exactMap = exactMappingMap.get(method);
        RequestMeta requestMeta = exactMap.get(mapping);
        if (requestMeta != null) {
            if (!requestMeta.allowSuffix(context.getSuffix())) {
                // 精确匹配但后缀不匹配，尝试 Trie 变量匹配
                return doTrieParse(context, method, mapping);
            }
            return requestMeta;
        }
        // Trie 路径：变量匹配（O(k)，k = 路径深度）
        return doTrieParse(context, method, mapping);
    }

    /**
     * 通过 Trie 路由树进行变量路径匹配
     *
     * @param context 请求上下文对象
     * @param method  HTTP 请求方式
     * @param mapping 请求映射路径
     * @return 返回匹配的请求映射元数据描述对象，若未匹配则返回 null
     * @since 2.1.4
     */
    private RequestMeta doTrieParse(IRequestContext context, Type.HttpMethod method, String mapping) {
        RadixTreeRouter router = routers.get(method);
        MatchResult result = router.resolve(mapping);
        if (result != null) {
            if (!result.getMeta().allowSuffix(context.getSuffix())) {
                return null;
            }
            Map<String, String> pathVars = result.getPathVariables();
            if (pathVars != null && !pathVars.isEmpty()) {
                pathVars.forEach(context::addAttribute);
            }
            return result.getMeta();
        }
        return null;
    }

    /**
     * Trie 路由树（Radix Tree），用于将变量路径匹配从 O(N) 优化至 O(k)
     *
     * @since 2.1.4
     */
    static class RadixTreeRouter {

        private final RadixTreeNode root = new RadixTreeNode();

        /**
         * Trie 路由树节点
         */
        static class RadixTreeNode {

            private final Map<String, RadixTreeNode> children = new HashMap<>();

            private String variableName;

            private RequestMeta meta;
        }

        /**
         * 注册路径映射
         *
         * @param path 路径模式（如 /users/{id}/posts/{postId}）
         * @param meta 请求映射元数据
         */
        void register(String path, RequestMeta meta) {
            String[] segments = StringUtils.split(path, Type.Const.PATH_SEPARATOR_CHAR);
            RadixTreeNode node = root;
            for (String seg : segments) {
                if (StringUtils.isEmpty(seg)) {
                    continue;
                }
                if (seg.startsWith("{") && seg.endsWith("}")) {
                    // 变量段：使用 __var__ 作为占位键
                    String varName = seg.substring(1, seg.length() - 1);
                    RadixTreeNode varNode = node.children.get("__var__");
                    if (varNode == null) {
                        varNode = new RadixTreeNode();
                        varNode.variableName = varName;
                        node.children.put("__var__", varNode);
                    } else if (varNode.variableName == null) {
                        varNode.variableName = varName;
                    }
                    node = varNode;
                } else {
                    node = node.children.computeIfAbsent(seg, k -> new RadixTreeNode());
                }
            }
            node.meta = meta;
        }

        /**
         * 解析请求路径，匹配成功返回结果
         *
         * @param path 请求路径（如 /users/123/posts/456）
         * @return 匹配结果，若未匹配则返回 null
         */
        MatchResult resolve(String path) {
            String[] segments = StringUtils.split(path, Type.Const.PATH_SEPARATOR_CHAR);
            Map<String, String> pathVars = new LinkedHashMap<>();
            RadixTreeNode node = root;
            for (String seg : segments) {
                if (StringUtils.isEmpty(seg)) {
                    continue;
                }
                // 优先精确匹配
                RadixTreeNode child = node.children.get(seg);
                if (child != null) {
                    node = child;
                } else {
                    // 尝试变量匹配
                    RadixTreeNode varNode = node.children.get("__var__");
                    if (varNode != null) {
                        if (varNode.variableName != null) {
                            pathVars.put(varNode.variableName, seg);
                        }
                        node = varNode;
                    } else {
                        return null;
                    }
                }
            }
            return node.meta != null ? new MatchResult(node.meta, pathVars) : null;
        }
    }

    /**
     * Trie 路由匹配结果
     *
     * @since 2.1.4
     */
    static class MatchResult {

        private final RequestMeta meta;

        private final Map<String, String> pathVariables;

        MatchResult(RequestMeta meta, Map<String, String> pathVariables) {
            this.meta = meta;
            this.pathVariables = pathVariables;
        }

        RequestMeta getMeta() {
            return meta;
        }

        Map<String, String> getPathVariables() {
            return pathVariables;
        }
    }
}