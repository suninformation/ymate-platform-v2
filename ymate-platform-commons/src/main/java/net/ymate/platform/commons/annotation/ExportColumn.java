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

import net.ymate.platform.commons.IExportDataRender;
import net.ymate.platform.commons.util.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-02-01 03:36
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExportColumn {

    /**
     * @return 列名称
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 针对数值数据通过下标输出值(若下标越界将输出原始值)
     */
    String[] dataRange() default {};

    /**
     * @return 指定将列值转换为日期
     */
    boolean dateTime() default false;

    /**
     * @return 日期时间输出模式
     * @since 2.1.2
     */
    String pattern() default DateTimeUtils.YYYY_MM_DD_HH_MM_SS;

    /**
     * @return 指定列为货币类型将值保留小数
     */
    boolean currency() default false;

    /**
     * @return 配置货币类型计算时是否将原值除以10的decimals次方后计算（基于数值以整数存储的情况）
     * @since 2.1.2
     */
    boolean accuracy() default true;

    /**
     * @return 配置货币类型计算时保留小数位数
     */
    int decimals() default 2;

    /**
     * @return 排除导出属性
     */
    boolean excluded() default false;

    /**
     * @return 自定义列渲染器接口实现类
     */
    Class<? extends IExportDataRender> render() default IExportDataRender.class;

    /**
     * @return 排序
     * @since 2.1.3
     */
    int order() default 0;
}
