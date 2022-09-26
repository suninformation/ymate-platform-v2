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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/20 下午2:52
 */
public class TextLineCodec extends AbstractNioCodec {

    private static final Log LOG = LogFactory.getLog(TextLineCodec.class);

    private static final String TEXT_EOF = "\r\n";

    @Override
    public ByteBufferBuilder encode(Object message) {
        byte[] bytes = ArrayUtils.addAll(messageToBytes(message), stringToBytes(TEXT_EOF));
        if (bytes != null) {
            return ByteBufferBuilder.allocate().append(bytes).flip();
        }
        return null;
    }

    @Override
    public Object decode(ByteBufferBuilder buffer) {
        if (buffer != null) {
            try {
                int counter = 0;
                ByteBufferBuilder tmpBuffer = ByteBufferBuilder.allocate();
                do {
                    byte b = buffer.get();
                    switch (b) {
                        case '\r':
                            break;
                        case '\n':
                            if (tmpBuffer.buffer() == null) {
                                break;
                            }
                            byte[] bytes = new byte[counter];
                            tmpBuffer.flip().get(bytes);
                            return new String(bytes, getCharset());
                        default:
                            tmpBuffer.append(b);
                            counter++;
                    }
                } while (buffer.buffer().hasRemaining());
            } catch (UnsupportedEncodingException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return null;
    }
}
