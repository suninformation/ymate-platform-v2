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
package net.ymate.platform.core.support;

import net.ymate.platform.core.IApplication;

import java.util.Collections;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-05-07 14:46
 * @since 2.1.0
 */
public abstract class AbstractContext implements IContext {

    private final IApplication owner;

    private final Map<String, String> contextParams;

    public AbstractContext(IApplication owner) {
        this(owner, null);
    }

    public AbstractContext(IApplication owner, Map<String, String> contextParams) {
        this.owner = owner;
        this.contextParams = contextParams != null ? contextParams : Collections.emptyMap();
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public Map<String, String> getContextParams() {
        return Collections.unmodifiableMap(contextParams);
    }
}
