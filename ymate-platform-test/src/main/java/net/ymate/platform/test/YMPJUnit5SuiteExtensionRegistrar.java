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

import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * @author 刘镇 (suninformation@163.com) on 2026/01/02 03:58
 * @since 2.1.4
 */
public class YMPJUnit5SuiteExtensionRegistrar {
    @RegisterExtension
    static final YMPJUnit5SuiteExtension extension = new YMPJUnit5SuiteExtension();
}
