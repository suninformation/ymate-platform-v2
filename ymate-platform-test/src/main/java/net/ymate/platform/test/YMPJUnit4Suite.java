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

import net.ymate.platform.core.IApplication;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/01/16 23:54
 * @since 1.0.0
 */
public class YMPJUnit4Suite extends Suite {

    private final IApplication application;

    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
        }
        return annotation.value();
    }

    public YMPJUnit4Suite(Class<?> klass) throws InitializationError {
        this(klass, new YMPJUnit4RunnerBuilder(klass, getAnnotatedClasses(klass)));
    }

    private YMPJUnit4Suite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        this.application = ((YMPJUnit4RunnerBuilder) builder).getApplication();
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        final Statement classBlock = super.classBlock(notifier);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    classBlock.evaluate();
                } finally {
                    if (application != null) {
                        application.close();
                    }
                }
            }
        };
    }
}
