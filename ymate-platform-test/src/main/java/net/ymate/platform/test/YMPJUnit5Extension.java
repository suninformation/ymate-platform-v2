/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.test;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import org.junit.jupiter.api.extension.*;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2026/01/02 03:58
 * @since 2.1.4
 */
public class YMPJUnit5Extension implements TestInstanceFactory, ParameterResolver, AfterAllCallback {

    private static final Map<Class<?>, WeakReference<IApplication>> APPLICATION_CACHE = new ConcurrentHashMap<>();

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) throws TestInstantiationException {
        Class<?> testClass = factoryContext.getTestClass();
        IApplication application = getOrCreateApplication(testClass);
        try {
            return application.getBeanFactory().getBean(testClass);
        } catch (Exception e) {
            throw new TestInstantiationException("Failed to create test instance", RuntimeUtils.unwrapThrow(e));
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return IApplication.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> testClass = extensionContext.getTestClass().orElse(null);
        if (testClass == null) {
            throw new IllegalArgumentException("Test class not found");
        }
        return getOrCreateApplication(testClass);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        extensionContext.getTestClass().ifPresent(testClass -> {
            WeakReference<IApplication> ref = APPLICATION_CACHE.remove(testClass);
            if (ref != null) {
                IApplication application = ref.get();
                if (application != null) {
                    try {
                        application.close();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to close YMP application", e);
                    }
                }
            }
        });
        // 清理过期的弱引用
        APPLICATION_CACHE.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

    private IApplication getOrCreateApplication(Class<?> testClass) {
        return APPLICATION_CACHE.computeIfAbsent(testClass, clazz -> {
            try {
                IApplication application = YMPTestUtils.initializeYMP(clazz);
                return new WeakReference<>(application);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize YMP application", e);
            }
        }).get();
    }
}
