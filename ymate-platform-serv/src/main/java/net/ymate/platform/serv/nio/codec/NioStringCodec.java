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
package net.ymate.platform.serv.nio.codec;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.nio.support.ByteBufferBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午7:05
 */
public class NioStringCodec extends ByteArrayCodec {

    private static final Log LOG = LogFactory.getLog(NioStringCodec.class);

    @Override
    public ByteBufferBuilder encode(Object message) {
        return super.encode(message);
    }

    @Override
    public Object decode(ByteBufferBuilder buffer) {
        try {
            byte[] bytes = (byte[]) super.decode(buffer);
            if (bytes != null) {
                return new String(bytes, getCharset());
            }
        } catch (UnsupportedEncodingException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }
}
