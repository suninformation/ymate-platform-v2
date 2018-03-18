/*
 * Copyright 2007-2017 the original author or authors.
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
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:13
 * @version 1.0
 */
public class DefaultSerializer implements ISerializer {

    @Override
    public String getContentType() {
        return "application/x-java-serialized-object";
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        ObjectOutputStream _output = null;
        try {
            ByteArrayOutputStream _stream = new ByteArrayOutputStream();
            _output = new ObjectOutputStream(_stream);
            _output.writeObject(object);
            return _stream.toByteArray();
        } finally {
            IOUtils.closeQuietly(_output);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        ObjectInputStream _input = null;
        try {
            _input = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) _input.readObject();
        } finally {
            IOUtils.closeQuietly(_input);
        }
    }
}
