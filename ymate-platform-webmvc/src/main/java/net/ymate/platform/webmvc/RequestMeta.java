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
package net.ymate.platform.webmvc;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.webmvc.annotation.*;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制器请求映射元数据描述
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-10 下午10:59:14
 */
public class RequestMeta {

    private static final Map<Class<?>, Map<String, ParameterMeta>> CLASS_PARAMETER_METAS = new ConcurrentHashMap<>();

    private final List<ParameterMeta> methodParameterMetas;

    private final Class<?> targetClass;

    private final String name;

    private final String mapping;

    private Class<? extends IRequestProcessor> processor;

    private Class<? extends IResponseErrorProcessor> errorProcessor;

    private final Method method;

    private final List<String> methodParamNames;

    private final boolean singleton;

    private ResponseCache responseCache;

    private ResponseView responseView;

    private ResponseBody responseBody;

    private final Set<Header> responseHeaders;

    private final Set<Type.HttpMethod> allowMethods;

    private final Map<String, String> allowHeaders;

    private final Map<String, String> allowParams;

    public RequestMeta(String requestMappingPrefix, Class<?> targetClass, Method method) throws Exception {
        this.targetClass = targetClass;
        this.method = method;
        //
        this.allowMethods = new HashSet<>();
        this.allowHeaders = new HashMap<>();
        this.allowParams = new HashMap<>();
        //
        Controller controller = targetClass.getAnnotation(Controller.class);
        this.name = StringUtils.defaultIfBlank(controller == null ? null : controller.name(), targetClass.getName());
        this.singleton = controller == null || controller.singleton();
        //
        this.responseCache = method.getAnnotation(ResponseCache.class);
        if (this.responseCache == null) {
            this.responseCache = targetClass.getAnnotation(ResponseCache.class);
            if (this.responseCache == null) {
                this.responseCache = targetClass.getPackage().getAnnotation(ResponseCache.class);
            }
        }
        //
        this.responseView = method.getAnnotation(ResponseView.class);
        if (this.responseView == null) {
            this.responseView = targetClass.getAnnotation(ResponseView.class);
            if (this.responseView == null) {
                this.responseView = targetClass.getPackage().getAnnotation(ResponseView.class);
            }
        }
        //
        this.responseBody = method.getAnnotation(ResponseBody.class);
        if (this.responseBody == null) {
            this.responseBody = targetClass.getAnnotation(ResponseBody.class);
            if (this.responseBody == null) {
                this.responseBody = targetClass.getPackage().getAnnotation(ResponseBody.class);
            }
        }
        //
        this.responseHeaders = new HashSet<>();
        ResponseHeader respHeader = targetClass.getPackage().getAnnotation(ResponseHeader.class);
        if (respHeader != null) {
            Collections.addAll(this.responseHeaders, respHeader.value());
        }
        respHeader = targetClass.getAnnotation(ResponseHeader.class);
        if (respHeader != null) {
            Collections.addAll(this.responseHeaders, respHeader.value());
        }
        respHeader = method.getAnnotation(ResponseHeader.class);
        if (respHeader != null) {
            Collections.addAll(this.responseHeaders, respHeader.value());
        }
        // 优化自定义请求映射前缀逻辑, 包级请求映射前缀优先处理
        RequestMapping requestMapping = targetClass.getPackage().getAnnotation(RequestMapping.class);
        String root;
        if (requestMapping != null) {
            root = doBuildRequestMapping(requestMappingPrefix, requestMapping);
            doSetAllowValues(requestMapping);
        } else {
            root = doCheckMappingSeparator(requestMappingPrefix);
        }
        requestMapping = targetClass.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            root = doBuildRequestMapping(root, requestMapping);
            doSetAllowValues(requestMapping);
        }
        requestMapping = method.getAnnotation(RequestMapping.class);
        doSetAllowValues(requestMapping);
        //
        if (this.allowMethods.isEmpty()) {
            this.allowMethods.add(Type.HttpMethod.GET);
        }
        //
        this.mapping = doBuildRequestMapping(root, requestMapping);
        //
        RequestProcessor requestProcessor = method.getAnnotation(RequestProcessor.class);
        if (requestProcessor == null) {
            requestProcessor = targetClass.getAnnotation(RequestProcessor.class);
            if (requestProcessor == null) {
                requestProcessor = targetClass.getPackage().getAnnotation(RequestProcessor.class);
            }
        }
        if (requestProcessor != null) {
            this.processor = requestProcessor.value();
        }
        //
        ResponseErrorProcessor responseErrorProcessor = method.getAnnotation(ResponseErrorProcessor.class);
        if (responseErrorProcessor == null) {
            responseErrorProcessor = targetClass.getAnnotation(ResponseErrorProcessor.class);
            if (responseErrorProcessor == null) {
                responseErrorProcessor = targetClass.getPackage().getAnnotation(ResponseErrorProcessor.class);
            }
        }
        if (responseErrorProcessor != null) {
            this.errorProcessor = responseErrorProcessor.value();
        }
        //
        ReentrantLockHelper.putIfAbsentAsync(CLASS_PARAMETER_METAS, targetClass, () -> {
            ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapper(targetClass);
            if (beanWrapper != null) {
                Map<String, ParameterMeta> parameterMetas = new HashMap<>();
                //
                beanWrapper.getFieldNames().stream().filter((fieldName) -> (!parameterMetas.containsKey(fieldName))).forEachOrdered((fieldName) -> {
                    ParameterMeta parameterMeta = new ParameterMeta(beanWrapper.getField(fieldName));
                    if (parameterMeta.isParamField()) {
                        parameterMetas.put(fieldName, parameterMeta);
                    }
                });
                return parameterMetas;
            }
            return null;
        });
        //
        this.methodParameterMetas = new ArrayList<>();
        this.methodParamNames = Arrays.asList(ClassUtils.getMethodParamNames(method));
        if (!this.methodParamNames.isEmpty()) {
            Parameter[] parameters = method.getParameters();
            int idx = 0;
            for (String methodName : methodParamNames) {
                ParameterMeta parameterMeta = new ParameterMeta(parameters[idx].getType(), methodName, parameters[idx].getAnnotations());
                if (parameterMeta.isParamField()) {
                    this.methodParameterMetas.add(parameterMeta);
                }
                idx++;
            }
        }
    }

    private void doSetAllowValues(RequestMapping requestMapping) {
        this.allowMethods.addAll(Arrays.asList(requestMapping.method()));
        for (String header : requestMapping.header()) {
            String[] headerParts = StringUtils.split(StringUtils.trimToEmpty(header), "=");
            if (headerParts.length == 2) {
                this.allowHeaders.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }
        for (String param : requestMapping.param()) {
            String[] paramParts = StringUtils.split(StringUtils.trimToEmpty(param), "=");
            if (paramParts.length == 2) {
                this.allowParams.put(paramParts[0].trim(), paramParts[1].trim());
            }
        }
    }

    private String doBuildRequestMapping(String root, RequestMapping requestMapping) {
        StringBuilder mappingBuilder = new StringBuilder(doCheckMappingSeparator(root));
        String mappingStr = requestMapping.value();
        if (StringUtils.isBlank(mappingStr)) {
            mappingBuilder.append(Type.Const.PATH_SEPARATOR).append(method.getName());
        } else {
            mappingBuilder.append(doCheckMappingSeparator(mappingStr));
        }
        return mappingBuilder.toString();
    }

    private String doCheckMappingSeparator(String requestMapping) {
        if (StringUtils.isBlank(requestMapping)) {
            return StringUtils.EMPTY;
        }
        if (!requestMapping.startsWith(Type.Const.PATH_SEPARATOR)) {
            requestMapping = Type.Const.PATH_SEPARATOR.concat(requestMapping);
        }
        if (requestMapping.endsWith(Type.Const.PATH_SEPARATOR)) {
            requestMapping = requestMapping.substring(0, requestMapping.length() - 1);
        }
        return requestMapping;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getMapping() {
        return mapping;
    }

    public Class<? extends IRequestProcessor> getProcessor() {
        return processor;
    }

    public Class<? extends IResponseErrorProcessor> getErrorProcessor() {
        return errorProcessor;
    }

    public Method getMethod() {
        return method;
    }

    public List<String> getMethodParamNames() {
        return Collections.unmodifiableList(methodParamNames);
    }

    public String getName() {
        return name;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public ResponseCache getResponseCache() {
        return responseCache;
    }

    public ResponseView getResponseView() {
        return responseView;
    }

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public Set<Header> getResponseHeaders() {
        return Collections.unmodifiableSet(responseHeaders);
    }

    /**
     * @param method 方法名称
     * @return 判断是否允许method方式的HTTP请求，若允许的请求方式集合为空，则默认不限制
     */
    public boolean allowHttpMethod(Type.HttpMethod method) {
        // 若允许的请求方式集合为空，则默认不限制
        return this.allowMethods.isEmpty() || this.allowMethods.contains(method);
    }

    public Set<Type.HttpMethod> getAllowMethods() {
        return Collections.unmodifiableSet(allowMethods);
    }

    public Map<String, String> getAllowHeaders() {
        return Collections.unmodifiableMap(allowHeaders);
    }

    public Map<String, String> getAllowParams() {
        return Collections.unmodifiableMap(allowParams);
    }

    public Collection<ParameterMeta> getClassParameterMetas() {
        return Collections.unmodifiableCollection(CLASS_PARAMETER_METAS.get(targetClass).values());
    }

    public List<ParameterMeta> getMethodParameterMetas() {
        return Collections.unmodifiableList(methodParameterMetas);
    }
}
