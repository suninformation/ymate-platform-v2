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
package net.ymate.platform.core.serialize.impl;

import net.ymate.platform.core.serialize.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:13
 */
public class DefaultSerializer implements ISerializer {

    public final static String NAME = "default";

    @Override
    public String getContentType() {
        return "application/x-java-serialized-object";
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(stream)) {
            output.writeObject(object);
            return stream.toByteArray();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) input.readObject();
        }
    }
}
