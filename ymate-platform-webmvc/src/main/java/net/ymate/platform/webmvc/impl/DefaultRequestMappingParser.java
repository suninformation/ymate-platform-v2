/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.lang.PairObject;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IRequestMappingParser;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 默认基于RESTFul风格的WebMVC请求映射路径分析器
 *
 * @author 刘镇 (suninformation@163.com) on 2011-7-26 上午11:11:45
 * @version 1.0
 */
public class DefaultRequestMappingParser implements IRequestMappingParser {

    private final Map<String, RequestMeta> __MAPPING_META_FOR_GET = new HashMap<String, RequestMeta>();

    private final Map<String, RequestMeta> __MAPPING_META_FOR_POST = new HashMap<String, RequestMeta>();

    private final Map<String, RequestMeta> __MAPPING_META_FOR_DELETE = new HashMap<String, RequestMeta>();

    private final Map<String, RequestMeta> __MAPPING_META_FOR_PUT = new HashMap<String, RequestMeta>();

    private final Map<String, RequestMeta> __MAPPING_META_FOR_OPTIONS = new HashMap<String, RequestMeta>();

    private final Map<String, RequestMeta> __MAPPING_META_FOR_HEAD = new HashMap<String, RequestMeta>();

    private final Map<String, RequestMeta> __MAPPING_META_FOR_TRACE = new HashMap<String, RequestMeta>();

    public DefaultRequestMappingParser() {
    }

    @Override
    public final void registerRequestMeta(RequestMeta requestMeta) {
        for (Type.HttpMethod _method : requestMeta.getAllowMethods()) {
            switch (_method) {
                case POST:
                    __MAPPING_META_FOR_POST.put(requestMeta.getMapping(), requestMeta);
                    break;
                case DELETE:
                    __MAPPING_META_FOR_DELETE.put(requestMeta.getMapping(), requestMeta);
                    break;
                case PUT:
                    __MAPPING_META_FOR_PUT.put(requestMeta.getMapping(), requestMeta);
                    break;
                case OPTIONS:
                    __MAPPING_META_FOR_OPTIONS.put(requestMeta.getMapping(), requestMeta);
                    break;
                case HEAD:
                    __MAPPING_META_FOR_HEAD.put(requestMeta.getMapping(), requestMeta);
                    break;
                case TRACE:
                    __MAPPING_META_FOR_TRACE.put(requestMeta.getMapping(), requestMeta);
                    break;
                default:
                    __MAPPING_META_FOR_GET.put(requestMeta.getMapping(), requestMeta);
            }
        }
    }

    @Override
    public Map<String, RequestMeta> getRequestMetas(Type.HttpMethod httpMethod) {
        Map<String, RequestMeta> _mappingMap;
        switch (httpMethod) {
            case POST:
                _mappingMap = __MAPPING_META_FOR_POST;
                break;
            case DELETE:
                _mappingMap = __MAPPING_META_FOR_DELETE;
                break;
            case PUT:
                _mappingMap = __MAPPING_META_FOR_PUT;
                break;
            case OPTIONS:
                _mappingMap = __MAPPING_META_FOR_OPTIONS;
                break;
            case HEAD:
                _mappingMap = __MAPPING_META_FOR_HEAD;
                break;
            case TRACE:
                _mappingMap = __MAPPING_META_FOR_TRACE;
                break;
            default:
                _mappingMap = __MAPPING_META_FOR_GET;
        }
        return Collections.unmodifiableMap(_mappingMap);
    }

    /**
     * @param partStr 参数段
     * @return 返回去掉首尾'/'字符的串
     */
    private String __doFixMappingPart(String partStr) {
        partStr = StringUtils.trimToEmpty(partStr);
        if (StringUtils.startsWith(partStr, "/")) {
            partStr = StringUtils.substringAfter(partStr, "/");
        }
        if (StringUtils.endsWith(partStr, "/")) {
            partStr = StringUtils.substringBeforeLast(partStr, "/");
        }
        return partStr;
    }

    @Override
    public final RequestMeta doParse(IRequestContext context) {
        Map<String, RequestMeta> _mappingMap = getRequestMetas(context.getHttpMethod());
        RequestMeta _meta = _mappingMap.get(context.getRequestMapping());
        if (_meta == null) {
            return __doParse(context, _mappingMap);
        }
        return _meta;
    }

    private RequestMeta __doParse(IRequestContext context, Map<String, RequestMeta> mappings) {
        String _requestMapping = __doFixMappingPart(context.getRequestMapping());
        String[] _originalParts = StringUtils.split(_requestMapping, "/");
        // 收集参数段数量相同的映射
        Set<PairObject<String[], RequestMeta>> _filtered = new HashSet<PairObject<String[], RequestMeta>>();
        for (Map.Entry<String, RequestMeta> _entry : mappings.entrySet()) {
            if (_entry.getKey().contains("{")) {
                String[] _parts = StringUtils.split(__doFixMappingPart(_entry.getKey()), "/");
                if (_parts.length == _originalParts.length) {
                    _filtered.add(new PairObject<String[], RequestMeta>(_parts, _entry.getValue()));
                }
            }
        }
        // 遍历已过滤映射集合通过与请求映射串参数比较，找出最接近的一个并提取参数:
        Map<String, String> _params = new HashMap<String, String>();
        for (PairObject<String[], RequestMeta> _item : _filtered) {
            boolean _breakFlag = false;
            for (int _idx = 0; _idx < _originalParts.length; _idx++) {
                if (_item.getKey()[_idx].contains("{")) {
                    String _paramName = StringUtils.substringBetween(_item.getKey()[_idx], "{", "}");
                    if (_paramName != null) {
                        _params.put(_paramName, _originalParts[_idx]);
                    }
                } else if (!StringUtils.equalsIgnoreCase(_item.getKey()[_idx], _originalParts[_idx])) {
                    _breakFlag = true;
                    break;
                }
            }
            if (!_breakFlag) {
                // 参数变量存入WebContext容器中的PathVariable参数池
                for (Map.Entry<String, String> _entry : _params.entrySet()) {
                    context.addAttribute(_entry.getKey(), _entry.getValue());
                }
                return _item.getValue();
            }
        }
        return null;
    }
}
