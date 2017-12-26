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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.platform.core.serialize.ISerializer;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:14
 * @version 1.0
 */
public class JSONSerializer implements ISerializer {

    @Override
    public byte[] serialize(Object object) throws Exception {
        com.alibaba.fastjson.serializer.JSONSerializer _serializer = new com.alibaba.fastjson.serializer.JSONSerializer();
        _serializer.config(SerializerFeature.WriteEnumUsingToString, true);
        _serializer.config(SerializerFeature.WriteClassName, true);
        _serializer.write(object);
        return _serializer.getWriter().toBytes(DEFAULT_CHARSET);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return JSON.parseObject(new String(bytes, DEFAULT_CHARSET), clazz);
    }
}
