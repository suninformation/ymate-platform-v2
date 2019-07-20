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

import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IRequestMappingParser;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 默认基于RESTFul风格的WebMVC请求映射路径分析器
 *
 * @author 刘镇 (suninformation@163.com) on 2011-7-26 上午11:11:45
 */
public class DefaultRequestMappingParser implements IRequestMappingParser {

    private final Map<String, RequestMeta> MAPPING_META_FOR_GET = new HashMap<>();

    private final Map<String, RequestMeta> MAPPING_META_FOR_POST = new HashMap<>();

    private final Map<String, RequestMeta> MAPPING_META_FOR_DELETE = new HashMap<>();

    private final Map<String, RequestMeta> MAPPING_META_FOR_PUT = new HashMap<>();

    private final Map<String, RequestMeta> MAPPING_META_FOR_OPTIONS = new HashMap<>();

    private final Map<String, RequestMeta> MAPPING_META_FOR_HEAD = new HashMap<>();

    private final Map<String, RequestMeta> MAPPING_META_FOR_TRACE = new HashMap<>();

    public DefaultRequestMappingParser() {
    }

    @Override
    public final void registerRequestMeta(RequestMeta requestMeta) {
        for (Type.HttpMethod httpMethod : requestMeta.getAllowMethods()) {
            switch (httpMethod) {
                case POST:
                    MAPPING_META_FOR_POST.put(requestMeta.getMapping(), requestMeta);
                    break;
                case DELETE:
                    MAPPING_META_FOR_DELETE.put(requestMeta.getMapping(), requestMeta);
                    break;
                case PUT:
                    MAPPING_META_FOR_PUT.put(requestMeta.getMapping(), requestMeta);
                    break;
                case OPTIONS:
                    MAPPING_META_FOR_OPTIONS.put(requestMeta.getMapping(), requestMeta);
                    break;
                case HEAD:
                    MAPPING_META_FOR_HEAD.put(requestMeta.getMapping(), requestMeta);
                    break;
                case TRACE:
                    MAPPING_META_FOR_TRACE.put(requestMeta.getMapping(), requestMeta);
                    break;
                default:
                    MAPPING_META_FOR_GET.put(requestMeta.getMapping(), requestMeta);
            }
        }
    }

    @Override
    public Map<String, RequestMeta> getRequestMetas(Type.HttpMethod httpMethod) {
        Map<String, RequestMeta> mappingMetas;
        switch (httpMethod) {
            case POST:
                mappingMetas = MAPPING_META_FOR_POST;
                break;
            case DELETE:
                mappingMetas = MAPPING_META_FOR_DELETE;
                break;
            case PUT:
                mappingMetas = MAPPING_META_FOR_PUT;
                break;
            case OPTIONS:
                mappingMetas = MAPPING_META_FOR_OPTIONS;
                break;
            case HEAD:
                mappingMetas = MAPPING_META_FOR_HEAD;
                break;
            case TRACE:
                mappingMetas = MAPPING_META_FOR_TRACE;
                break;
            default:
                mappingMetas = MAPPING_META_FOR_GET;
        }
        return Collections.unmodifiableMap(mappingMetas);
    }

    /**
     * @param partStr 参数段
     * @return 返回去掉首尾'/'字符的串
     */
    private String fixMappingPart(String partStr) {
        partStr = StringUtils.trimToEmpty(partStr);
        if (StringUtils.startsWith(partStr, Type.Const.PATH_SEPARATOR)) {
            partStr = StringUtils.substringAfter(partStr, Type.Const.PATH_SEPARATOR);
        }
        if (StringUtils.endsWith(partStr, Type.Const.PATH_SEPARATOR)) {
            partStr = StringUtils.substringBeforeLast(partStr, Type.Const.PATH_SEPARATOR);
        }
        return partStr;
    }

    @Override
    public final RequestMeta parse(IRequestContext context) {
        Map<String, RequestMeta> requestMetas = getRequestMetas(context.getHttpMethod());
        RequestMeta requestMeta = requestMetas.get(context.getRequestMapping());
        if (requestMeta == null) {
            return doParse(context, requestMetas);
        }
        return requestMeta;
    }

    private RequestMeta doParse(IRequestContext context, Map<String, RequestMeta> mappings) {
        String fixedRequestMapping = fixMappingPart(context.getRequestMapping());
        String[] originalParts = StringUtils.split(fixedRequestMapping, Type.Const.PATH_SEPARATOR);
        // 收集参数段数量相同的映射
        Set<PairObject<String[], RequestMeta>> filtered = new HashSet<>();
        mappings.entrySet().stream().filter((entry) -> (entry.getKey().contains("{"))).forEachOrdered((entry) -> {
            String[] parts = StringUtils.split(fixMappingPart(entry.getKey()), Type.Const.PATH_SEPARATOR);
            if (parts.length == originalParts.length) {
                filtered.add(new PairObject<>(parts, entry.getValue()));
            }
        });
        // 遍历已过滤映射集合通过与请求映射串参数比较，找出最接近的一个并提取参数:
        Map<String, String> params = new HashMap<>(filtered.size());
        for (PairObject<String[], RequestMeta> item : filtered) {
            boolean breakFlag = false;
            for (int idx = 0; idx < originalParts.length; idx++) {
                if (item.getKey()[idx].contains("{")) {
                    String paramName = StringUtils.substringBetween(item.getKey()[idx], "{", "}");
                    if (paramName != null) {
                        params.put(paramName, originalParts[idx]);
                    }
                } else if (!StringUtils.equalsIgnoreCase(item.getKey()[idx], originalParts[idx])) {
                    breakFlag = true;
                    break;
                }
            }
            if (!breakFlag) {
                // 参数变量存入WebContext容器中的PathVariable参数池
                params.forEach(context::addAttribute);
                return item.getValue();
            }
        }
        return null;
    }
}
