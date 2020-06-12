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
package net.ymate.platform.cache.impl;

import net.ymate.platform.cache.ICacheKeyGenerator;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.commons.serialize.ISerializer;
import net.ymate.platform.commons.serialize.SerializerManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/3 下午1:43
 */
public class DefaultCacheKeyGenerator implements ICacheKeyGenerator<Serializable> {

    private ISerializer serializer;

    private boolean initialized;

    @Override
    public void initialize(ICaches owner, ISerializer serializer) {
        if (!initialized) {
            if (serializer == null) {
                this.serializer = SerializerManager.getDefaultSerializer();
            } else {
                this.serializer = serializer;
            }
            initialized = true;
        }
    }

    @Override
    public Serializable generateKey(Method method, Object[] params) throws Exception {
        if (initialized) {
            // [className:methodName:{serializeStr}]
            String keyGenBuilder = String.format("[%s:%s{%s}]", method.getDeclaringClass().getName(), method.getName(), Base64.encodeBase64String(serializer.serialize(params)));
            return DigestUtils.md5Hex(keyGenBuilder);
        }
        return null;
    }
}
