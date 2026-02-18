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
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author 刘镇 (suninformation@163.com) on 2026/01/02 03:58
 * @since 2.1.4
 */
public class YMPJUnit5SuiteExtension implements BeforeAllCallback, AfterAllCallback {

    private static volatile IApplication application;

    private static volatile boolean initialized = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!initialized) {
            synchronized (YMPJUnit5SuiteExtension.class) {
                if (!initialized) {
                    Class<?> suiteClass = context.getTestClass().orElseThrow(() -> new IllegalArgumentException("Suite class not found"));
                    YMPJUnit5Suite annotation = suiteClass.getAnnotation(YMPJUnit5Suite.class);
                    if (annotation == null) {
                        throw new IllegalArgumentException("YMPJUnit5Suite annotation not found");
                    }
                    try {
                        application = YMPTestUtils.initializeYMP(suiteClass, annotation.value());
                        initialized = true;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize YMP suite", RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        synchronized (YMPJUnit5SuiteExtension.class) {
            if (initialized && application != null) {
                try {
                    application.close();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to close YMP application", RuntimeUtils.unwrapThrow(e));
                } finally {
                    application = null;
                    initialized = false;
                }
            }
        }
    }
}
