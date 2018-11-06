/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.serv;

import org.apache.commons.lang.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/6 2:36 PM
 * @version 1.0
 */
public abstract class AbstractCodec<T> implements ICodec<T> {

    private String __charset;

    @Override
    public ICodec init(String charset) {
        __charset = StringUtils.defaultIfBlank(charset, "UTF-8");
        return this;
    }

    /**
     * @return 返回字符集名称
     */
    public String getCharset() {
        return __charset;
    }
}
