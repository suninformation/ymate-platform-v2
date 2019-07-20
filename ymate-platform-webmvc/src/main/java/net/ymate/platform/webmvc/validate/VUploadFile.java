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
package net.ymate.platform.webmvc.validate;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 对上传的文件大小和类型进行验证
 *
 * @author 刘镇 (suninformation@163.com) on 16/3/20 上午3:22
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VUploadFile {

    /**
     * @return 设置最小字节长度，0为不限制
     */
    int min() default 0;

    /**
     * @return 设置最大字节长度，0为不限制
     */
    int max() default 0;

    /**
     * @return 上传文件总量最大字节长度(仅作用于数组参数)，0为不限制
     */
    long totalMax() default 0;

    /**
     * @return 允许的文件类型
     */
    String[] contentTypes() default {};

    /**
     * @return 自定义验证消息
     */
    String msg() default StringUtils.EMPTY;
}
