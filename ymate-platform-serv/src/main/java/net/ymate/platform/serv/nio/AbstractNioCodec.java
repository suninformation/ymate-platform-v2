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
package net.ymate.platform.serv.nio;

import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/6 2:42 PM
 */
public abstract class AbstractNioCodec implements INioCodec {

    private static final Log LOG = LogFactory.getLog(AbstractNioCodec.class);

    private String charset;

    @Override
    public void initialize(String charset) {
        this.charset = StringUtils.defaultIfBlank(charset, "UTF-8");
    }

    /**
     * @return 返回字符集名称
     */
    public String getCharset() {
        return charset;
    }

    protected byte[] stringToBytes(String str) {
        byte[] bytes = null;
        try {
            bytes = str.getBytes(getCharset());
        } catch (UnsupportedEncodingException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
        }
        return bytes;
    }

    protected byte[] messageToBytes(Object message) {
        byte[] bytes;
        if (message instanceof byte[]) {
            bytes = (byte[]) message;
        } else if (message instanceof String) {
            bytes = stringToBytes((String) message);
        } else {
            bytes = stringToBytes(message.toString());
        }
        return bytes;
    }
}
