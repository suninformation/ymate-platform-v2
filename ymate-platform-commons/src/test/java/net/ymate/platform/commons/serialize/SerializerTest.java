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
package net.ymate.platform.commons.serialize;

import net.ymate.platform.commons.json.impl.GsonAdapter;
import net.ymate.platform.commons.json.impl.JacksonAdapter;
import net.ymate.platform.commons.serialize.impl.FstSerializer;
import net.ymate.platform.commons.serialize.impl.HessianSerializer;
import net.ymate.platform.commons.serialize.impl.JSONSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/9/19 22:21
 * @since 2.1.2
 */
public class SerializerTest {

    private static final Log LOG = LogFactory.getLog(SerializerTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private void serializer(String name, ISerializer serializer) {
        try {
            SerializeBeanImpl beanImpl = new SerializeBeanImpl(name);
            beanImpl.setContentType(serializer.getContentType());
            //
            byte[] bytes = serializer.serialize(beanImpl);
            ISerializeBean serializeBean = serializer.deserialize(bytes, SerializeBeanImpl.class);
            LOG.info(serializeBean.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSerializers() {
        serializer("Default", SerializerManager.getDefaultSerializer());
        serializer("Hessian", SerializerManager.getSerializer(HessianSerializer.class));
        serializer("Fst", SerializerManager.getSerializer(FstSerializer.class));
        serializer("Fastjson", SerializerManager.getJsonSerializer());
        serializer("Gson", new JSONSerializer(new GsonAdapter()));
        serializer("Jackson", new JSONSerializer(new JacksonAdapter()));
    }
}