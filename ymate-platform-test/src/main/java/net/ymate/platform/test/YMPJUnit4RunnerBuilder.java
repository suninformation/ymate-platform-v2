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
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/01/16 23:54
 * @since 1.0.0
 */
public class YMPJUnit4RunnerBuilder extends RunnerBuilder {

    private final IApplication application;

    public YMPJUnit4RunnerBuilder(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        try {
            application = YMPTestUtils.initializeYMP(klass, suiteClasses);
        } catch (Exception e) {
            throw new InitializationError(RuntimeUtils.unwrapThrow(e));
        }
    }

    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        return new YMPJUnit4ClassRunner(application, testClass);
    }
}
