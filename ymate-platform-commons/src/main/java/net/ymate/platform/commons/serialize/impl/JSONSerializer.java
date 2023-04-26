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
package net.ymate.platform.commons.serialize.impl;

import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.TypeReferenceWrapper;
import net.ymate.platform.commons.serialize.ISerializer;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:14
 */
public class JSONSerializer implements ISerializer {

    public final static String NAME = "json";

    private final IJsonAdapter jsonAdapter;

    public JSONSerializer() {
        jsonAdapter = JsonWrapper.getJsonAdapter();
    }

    public JSONSerializer(IJsonAdapter jsonAdapter) {
        this.jsonAdapter = jsonAdapter;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        return jsonAdapter.serialize(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return jsonAdapter.deserialize(bytes, clazz);
    }

    @Override
    public <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef) throws Exception {
        return jsonAdapter.deserialize(bytes, typeRef);
    }
}
