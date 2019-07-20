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
package net.ymate.platform.core.beans.intercept;

/**
 * 拦截器异常
 *
 * @author 刘镇 (suninformation@163.com) on 2018/9/10 下午4:58
 */
public class InterceptException extends Exception {

    /**
     * 用于目标方法的返回值类型为void时向上层返回拦截器执行结果
     */
    private Object returnValue;

    public InterceptException() {
    }

    public InterceptException(String message) {
        super(message);
    }

    public InterceptException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterceptException(Throwable cause) {
        super(cause);
    }

    public InterceptException(Object returnValue) {
        super();
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
