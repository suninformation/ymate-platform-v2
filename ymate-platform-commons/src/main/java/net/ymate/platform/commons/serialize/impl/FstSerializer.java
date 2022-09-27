/*
 * Copyright 2007-2022 the original author or authors.
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

import net.ymate.platform.commons.serialize.ISerializer;
import net.ymate.platform.commons.serialize.fst.FstConfigurationFactory;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/9/22 13:20
 * @since 2.1.2
 */
public class FstSerializer implements ISerializer {

    private final FSTConfiguration fstConfiguration;

    public FstSerializer() {
        fstConfiguration = FstConfigurationFactory.getInstance().getFstConfiguration();
    }

    @Override
    public String getContentType() {
        return "application/x-java-serialized-fst";
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (FSTObjectOutput out = fstConfiguration.getObjectOutput(outputStream)) {
            out.writeObject(object);
            out.flush();
            return outputStream.toByteArray();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        try (FSTObjectInput input = fstConfiguration.getObjectInput(new ByteArrayInputStream(bytes))) {
            return (T) input.readObject(clazz);
        }
    }
}
