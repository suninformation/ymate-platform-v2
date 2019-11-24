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
package net.ymate.platform.persistence.jdbc.repo.annotation;

import net.ymate.platform.core.persistence.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 声明一个类为存储器对象, 声明一个类方法开启存储器操作
 *
 * @author 刘镇 (suninformation@163.com) on 16/4/22 下午1:49
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Repository {

    /**
     * @return 数据源名称, 默认为空
     */
    String dsName() default StringUtils.EMPTY;

    /**
     * @return 从资源文件中加载item指定的配置项, 默认为空
     */
    String item() default StringUtils.EMPTY;

    /**
     * @return 自定义SQL配置
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 是否为更新操作, 默认为false
     */
    boolean update() default false;

    /**
     * @return 是否分页查询, 默认为false
     */
    boolean page() default false;

    /**
     * @return 是否调用方法过滤, 默认为false
     */
    boolean useFilter() default false;

    /**
     * @return 指定当前存储器适用的数据库类型，默认为全部，否则将根据数据库类型进行存储器加载
     */
    String dbType() default Type.DATABASE.UNKNOWN;

    /**
     * @return 指定结果集类型，若设置则使用BeanResultSetHandler进行处理否则默认使用ArrayResultSetHandler处理结果集
     */
    Class<?> resultClass() default Void.class;
}
