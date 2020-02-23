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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    private SignatureValidate signatureValidate;

    private final Set<ResponseHeader> responseHeaders = new HashSet<>();

    private final Set<Type.HttpMethod> allowMethods = new HashSet<>();

    private final Map<String, String> allowHeaders = new HashMap<>();

    private final Map<String, String> allowParams = new HashMap<>();

    public RequestMeta(String requestMappingPrefix, Class<?> targetClass, Method method) throws Exception {
        this.targetClass = targetClass;
        this.method = method;
        //
        Controller controller = targetClass.getAnnotation(Controller.class);
        this.name = StringUtils.defaultIfBlank(controller == null ? null : controller.name(), targetClass.getName());
        this.singleton = controller == null || controller.singleton();
        //
        this.responseCache = findAnnotation(ResponseCache.class);
        this.responseView = findAnnotation(ResponseView.class);
        this.responseBody = findAnnotation(ResponseBody.class);
        this.signatureValidate = findAnnotation(SignatureValidate.class);
        //
        String packageMapping = processPackageAnnotations(targetClass);
        RequestMapping mappingAnn = targetClass.getAnnotation(RequestMapping.class);
        if (mappingAnn != null) {
            packageMapping = doBuildRequestMapping(packageMapping, mappingAnn.value(), false);
            doSetAllowValues(mappingAnn);
        }
        mappingAnn = method.getAnnotation(RequestMapping.class);
        doSetAllowValues(mappingAnn);
        this.mapping = doBuildRequestMapping(requestMappingPrefix, doBuildRequestMapping(packageMapping, mappingAnn.value(), true), false);
        //
        if (this.allowMethods.isEmpty()) {
            this.allowMethods.add(Type.HttpMethod.GET);
        }
        //
        ResponseHeader responseHeaderAnn = targetClass.getAnnotation(ResponseHeader.class);
        if (responseHeaderAnn != null) {
            this.responseHeaders.add(responseHeaderAnn);
        }
        ResponseHeaders responseHeadersAnn = targetClass.getAnnotation(ResponseHeaders.class);
        if (responseHeadersAnn != null) {
            Collections.addAll(this.responseHeaders, responseHeadersAnn.value());
        }
        responseHeaderAnn = method.getAnnotation(ResponseHeader.class);
        if (responseHeaderAnn != null) {
            this.responseHeaders.add(responseHeaderAnn);
        }
        responseHeadersAnn = method.getAnnotation(ResponseHeaders.class);
        if (responseHeadersAnn != null) {
            Collections.addAll(this.responseHeaders, responseHeadersAnn.value());
        }
        //
        RequestProcessor requestProcessor = findAnnotation(RequestProcessor.class);
        if (requestProcessor != null) {
            this.processor = requestProcessor.value();
        }
        //
        ResponseErrorProcessor responseErrorProcessor = findAnnotation(ResponseErrorProcessor.class);
        if (responseErrorProcessor != null) {
            this.errorProcessor = responseErrorProcessor.value();
        }
        //
        ReentrantLockHelper.putIfAbsentAsync(CLASS_PARAMETER_METAS, targetClass, () -> {
            Map<String, ParameterMeta> parameterMetas = new HashMap<>();
            for (Field field : targetClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !parameterMetas.containsKey(field.getName())) {
                    ParameterMeta parameterMeta = new ParameterMeta(field);
                    if (parameterMeta.isParamField()) {
                        parameterMetas.put(field.getName(), parameterMeta);
                    }
                }
            }
            return parameterMetas;
        });
        //
        this.methodParameterMetas = new ArrayList<>();
        this.methodParamNames = Arrays.asList(ClassUtils.getMethodParamNames(method));
        if (!this.methodParamNames.isEmpty()) {
            Parameter[] parameters = method.getParameters();
            int idx = 0;
            for (String methodName : methodParamNames) {
                if (parameters.length <= idx) {
                    break;
                }
                ParameterMeta parameterMeta = new ParameterMeta(parameters[idx].getType(), methodName, parameters[idx].getAnnotations());
                if (parameterMeta.isParamField()) {
                    this.methodParameterMetas.add(parameterMeta);
                }
                idx++;
            }
        }
    }

    private String processPackageAnnotations(Class<?> targetClass) {
        String packageMapping = null;
        Package targetPackage = targetClass.getPackage();
        if (targetPackage != null) {
            Class<?> parentPackage = ClassUtils.findParentPackage(targetClass);
            if (parentPackage != null) {
                packageMapping = processPackageAnnotations(parentPackage);
            }
            //
            RequestMapping requestMappingAnn = targetPackage.getAnnotation(RequestMapping.class);
            if (requestMappingAnn != null) {
                packageMapping = doBuildRequestMapping(packageMapping, requestMappingAnn.value(), false);
                doSetAllowValues(requestMappingAnn);
            } else {
                packageMapping = doCheckMappingSeparator(packageMapping);
            }
            //
            ResponseHeader responseHeaderAnn = targetPackage.getAnnotation(ResponseHeader.class);
            if (responseHeaderAnn != null) {
                this.responseHeaders.add(responseHeaderAnn);
            }
            ResponseHeaders responseHeadersAnn = targetPackage.getAnnotation(ResponseHeaders.class);
            if (responseHeadersAnn != null) {
                Collections.addAll(this.responseHeaders, responseHeadersAnn.value());
            }
        }
        return packageMapping;
    }

    private <T extends Annotation> T findAnnotation(Class<T> annotationClass) {
        T annotation = method.getAnnotation(annotationClass);
        if (annotation == null) {
            annotation = targetClass.getAnnotation(annotationClass);
            if (annotation == null) {
                annotation = ClassUtils.getPackageAnnotation(targetClass, annotationClass);
            }
        }
        return annotation;
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

    private String doBuildRequestMapping(String parentMapping, String mappingStr, boolean notEmpty) {
        StringBuilder mappingBuilder = new StringBuilder(doCheckMappingSeparator(parentMapping));
        if (notEmpty && StringUtils.isBlank(mappingStr)) {
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

    public SignatureValidate getSignatureValidate() {
        return signatureValidate;
    }

    public Set<ResponseHeader> getResponseHeaders() {
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
