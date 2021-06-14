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
package net.ymate.platform.serv.nio.codec;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.nio.AbstractNioCodec;
import net.ymate.platform.serv.nio.support.ByteBufferBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/6/14 10:48 下午
 * @since 2.1.0
 */
public class ByteArrayCodec extends AbstractNioCodec {

    private static final Log LOG = LogFactory.getLog(ByteArrayCodec.class);

    @Override
    public ByteBufferBuilder encode(Object message) {
        byte[] bytes = null;
        if (message instanceof byte[]) {
            bytes = (byte[]) message;
        } else if (message instanceof String) {
            try {
                bytes = ((String) message).getBytes(getCharset());
            } catch (UnsupportedEncodingException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        if (bytes != null) {
            return ByteBufferBuilder.allocate()
                    .append(bytes.length)
                    .append(bytes).flip();
        }
        return null;
    }

    @Override
    public Object decode(ByteBufferBuilder buffer) {
        if (buffer.remaining() < 4) {
            return null;
        }
        buffer.mark();
        int len = buffer.getInt();
        if (buffer.remaining() < len) {
            buffer.reset();
            return null;
        }
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }
}
