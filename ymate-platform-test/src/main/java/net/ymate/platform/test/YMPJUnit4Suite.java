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

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/01/16 23:54
 * @since 1.0.0
 */
public class YMPJUnit4Suite extends Suite {

    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
        }
        return annotation.value();
    }

    public YMPJUnit4Suite(Class<?> klass) throws InitializationError {
        super(klass, new YMPJUnit4RunnerBuilder(klass, getAnnotatedClasses(klass)));
    }
}
