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
package net.ymate.platform.commons.annotation;

import net.ymate.platform.commons.XPathHelper;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/3/27 上午10:40
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XPathNode {

    /**
     * @return 节点路径表达式
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 若节点不存在或为空，使用此默认值
     */
    String defaultValue() default StringUtils.EMPTY;

    /**
     * @return 指定成员对象为子节点类型(即非基本数据类型)
     */
    boolean child() default false;

    /**
     * @return 当child=true且对应的成员对象为接口类型时，用此参数指定接口实现类
     */
    Class<?> implClass() default Void.class;

    /**
     * @return 自定义节点值解析器
     */
    Class<? extends XPathHelper.INodeValueParser> parser() default XPathHelper.INodeValueParser.class;
}
