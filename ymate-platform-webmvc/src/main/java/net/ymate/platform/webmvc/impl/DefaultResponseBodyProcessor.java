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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.webmvc.IResponseBodyProcessor;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.IWebResult;
import net.ymate.platform.webmvc.IWebResultBuilder;
import net.ymate.platform.webmvc.util.WebResult;
import net.ymate.platform.webmvc.view.IView;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/10 上午3:19
 */
public class DefaultResponseBodyProcessor implements IResponseBodyProcessor {

    @Override
    public IView processBody(IWebMvc owner, Object result, boolean contentType, boolean keepNull, boolean snakeCase) throws Exception {
        IWebResult<?> returnValue;
        if (result instanceof IWebResult) {
            returnValue = (IWebResult<?>) result;
        } else {
            if (result instanceof IWebResultBuilder) {
                returnValue = ((IWebResultBuilder) result).build();
            } else {
                returnValue = WebResult.builder().succeed().data(result).build();
            }
            if (snakeCase) {
                returnValue.snakeCase();
            }
            if (keepNull) {
                returnValue.keepNullValue();
            }
            if (contentType) {
                returnValue.withContentType();
            }
        }
        return WebResult.formatView(returnValue);
    }
}
