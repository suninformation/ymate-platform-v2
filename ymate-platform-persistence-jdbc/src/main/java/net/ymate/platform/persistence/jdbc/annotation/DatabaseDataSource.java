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
package net.ymate.platform.persistence.jdbc.annotation;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/10 20:04
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(DatabaseConf.class)
public @interface DatabaseDataSource {

    /**
     * @return 数据源名称
     */
    String name();

    /**
     * @return 数据库连接字符串
     */
    String connectionUrl();

    /**
     * @return 数据库访问用户名称
     */
    String username();

    /**
     * @return 数据库访问密码
     */
    String password() default StringUtils.EMPTY;

    /**
     * @return 数据库访问密码是否已加密
     */
    boolean passwordEncrypted() default false;

    /**
     * @return 数据库密码处理器
     */
    Class<? extends IPasswordProcessor> passwordClass() default IPasswordProcessor.class;

    /**
     * @return 数据库类型
     */
    String type() default StringUtils.EMPTY;

    /**
     * @return 数据库方言
     */
    Class<? extends IDialect> dialectClass() default IDialect.class;

    /**
     * @return 数据源适配器
     */
    Class<? extends IDatabaseDataSourceAdapter> adapterClass() default IDatabaseDataSourceAdapter.class;

    /**
     * @return 数据源适配器配置文件
     */
    String configFile() default StringUtils.EMPTY;

    /**
     * @return 数据库默认驱动类名称
     */
    String driverClass() default StringUtils.EMPTY;

    /**
     * @return 是否自动连接
     * @since 2.1.3
     */
    boolean autoConnection() default false;

    /**
     * @return 是否显示执行的SQL语句
     */
    boolean showSql() default false;

    /**
     * @return 是否开启堆栈跟踪
     */
    boolean stackTraces() default false;

    /**
     * @return 堆栈跟踪层级深度
     */
    int stackTraceDepth() default 0;

    /**
     * @return 堆栈跟踪过滤包名前缀集合
     */
    String[] stackTracePackages() default {};

    /**
     * @return 数据库表前缀名称
     */
    String tablePrefix() default StringUtils.EMPTY;

    /**
     * @return 数据库引用标识符
     */
    String identifierQuote() default StringUtils.EMPTY;
}
