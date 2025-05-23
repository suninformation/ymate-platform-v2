/*
 * Copyright 2007-2025 the original author or authors.
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
package net.ymate.platform.commons.ext;

import net.ymate.platform.commons.lang.BlurObject;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * (提取自 ymate-module-security 模块)
 *
 * @author 刘镇 (suninformation@163.com) on 2023/1/31 00:39
 * @since 2.1.4
 */
public abstract class AbstractAttributeExt implements IAttributeExt {

    private static final long serialVersionUID = 1L;

    private final Map<String, Object> attributes = new HashMap<>();

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    protected void addAttributes(Map<String, Object> attributes) {
        if (attributes != null && !attributes.isEmpty())
            this.attributes.putAll(attributes);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public BlurObject getAttributeBlur(String name) {
        return BlurObject.bind(getAttribute(name));
    }

    @Override
    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttributes(String... names) {
        if (ArrayUtils.isEmpty(names)) {
            attributes.clear();
        } else {
            Arrays.stream(names).forEach(attributes::remove);
        }
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public static class AbstractBuilder<T extends IAttributeExtBuilder<?>, E extends AbstractAttributeExt> implements IAttributeExtBuilder<T> {

        protected final E target;

        protected AbstractBuilder(E target) {
            this.target = target;
        }

        public E build() {
            return target;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T attributes(Map<String, Object> attributes) {
            target.addAttributes(attributes);
            return (T) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T attribute(String name, Object value) {
            target.addAttribute(name, value);
            return (T) this;
        }
    }
}
