/*
 * Copyright 2007-2021 the original author or authors.
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

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import net.ymate.platform.commons.serialize.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/6/14 9:57 下午
 * @since 2.1.0
 */
public class HessianSerializer implements ISerializer {

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(outputStream);
        out.writeObject(object);
        out.flush();
        return outputStream.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
        return (T) input.readObject(clazz);
    }
}
