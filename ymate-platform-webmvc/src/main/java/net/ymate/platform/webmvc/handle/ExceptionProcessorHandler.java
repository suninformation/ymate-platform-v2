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
package net.ymate.platform.webmvc.handle;

import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.webmvc.annotation.ExceptionProcessor;
import net.ymate.platform.webmvc.util.ExceptionProcessHelper;
import net.ymate.platform.webmvc.util.IExceptionProcessor;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-13 00:20
 * @version 1.0
 * @since 2.0.6
 */
public class ExceptionProcessorHandler implements IBeanHandler {

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isSubclassOf(targetClass, Throwable.class)) {
            final ExceptionProcessor _processorAnn = targetClass.getAnnotation(ExceptionProcessor.class);
            ExceptionProcessHelper.DEFAULT.registerProcessor((Class<? extends Throwable>) targetClass, new IExceptionProcessor() {
                @Override
                public Result process(Throwable target) throws Exception {
                    return new Result(_processorAnn.code(), _processorAnn.msg());
                }
            });
        }
        return null;
    }
}
