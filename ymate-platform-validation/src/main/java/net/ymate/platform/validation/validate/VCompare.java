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
package net.ymate.platform.validation.validate;

import net.ymate.platform.validation.annotation.VField;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 参数值比较验证注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/26 下午11:24
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VCompare {

    /**
     * 比较条件
     */
    enum Cond {
        /**
         * 相等
         */
        EQ,

        /**
         * 不相等
         */
        NOT_EQ,

        /**
         * 大于
         */
        GT,

        /**
         * 大于等于
         */
        GT_EQ,

        /**
         * 小于
         */
        LT,

        /**
         * 小于等于
         */
        LT_EQ
    }

    /**
     * @return 比较的条件
     */
    Cond cond() default Cond.EQ;

    /**
     * @return 与之比较的参数名称
     */
    String with();

    /**
     * @return 与之比较的参数及标签名称
     * @since 2.1.0 调整方法数据为VField注解类型
     */
    VField withLabel() default @VField;

    /**
     * @return 自定义验证消息
     */
    String msg() default StringUtils.EMPTY;
}
