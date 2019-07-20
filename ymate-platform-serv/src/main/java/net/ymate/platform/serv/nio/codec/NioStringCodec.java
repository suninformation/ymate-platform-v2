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
import net.ymate.platform.serv.nio.AbstractNioCodec;
import net.ymate.platform.serv.nio.support.ByteBufferBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午7:05
 */
public class NioStringCodec extends AbstractNioCodec {

    private static final Log LOG = LogFactory.getLog(NioStringCodec.class);

    @Override
    public ByteBufferBuilder encode(Object message) {
        if (message instanceof String) {
            try {
                byte[] bytes = ((String) message).getBytes(getCharset());
                return ByteBufferBuilder.allocate()
                        .append(bytes.length)
                        .append(bytes).flip();
            } catch (UnsupportedEncodingException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                }
            }
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
        try {
            return new String(bytes, getCharset());
        } catch (UnsupportedEncodingException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }
}
