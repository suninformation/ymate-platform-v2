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
package net.ymate.platform.core.annotation;

import net.ymate.platform.core.i18n.II18nEventHandler;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/3/8 6:50 下午
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface I18nConf {

    /**
     * @return 国际化资源默认语言设置, 默认采用系统环境语言
     */
    String defaultLocale() default StringUtils.EMPTY;

    /**
     * @return 国际化资源事件监听处理器
     */
    Class<? extends II18nEventHandler> eventHandlerClass() default II18nEventHandler.class;
}
