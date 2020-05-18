/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.webmvc.annotation;

import net.ymate.platform.webmvc.ISignatureExtraParamProcessor;
import net.ymate.platform.webmvc.ISignatureParamParser;
import net.ymate.platform.webmvc.ISignatureValidator;
import net.ymate.platform.webmvc.impl.DefaultSignatureParamParser;
import net.ymate.platform.webmvc.impl.DefaultSignatureValidator;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 签名验证规则注解
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/18 11:40
 * @since 2.1.0
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignatureValidate {

    /**
     * @return 签名参数名称
     */
    String paramName() default "sign";

    /**
     * @return 随机参数名称
     */
    String nonceName() default StringUtils.EMPTY;

    /**
     * @return 是否进行编码
     */
    boolean encode() default false;

    /**
     * @return 是否转换签名字符串为大写
     */
    boolean upperCase() default true;

    /**
     * @return 是否已禁用
     */
    boolean disabled() default false;

    /**
     * @return 排除的参数名称集合
     */
    String[] excludedParams() default {};

    /**
     * @return 签名验证器类
     */
    Class<? extends ISignatureValidator> validatorClass() default DefaultSignatureValidator.class;

    /**
     * @return 签名参数分析器类型
     */
    Class<? extends ISignatureParamParser> parserClass() default DefaultSignatureParamParser.class;

    /**
     * @return 附加签名参数处理器类型
     */
    Class<? extends ISignatureExtraParamProcessor> processorClass() default ISignatureExtraParamProcessor.class;
}
