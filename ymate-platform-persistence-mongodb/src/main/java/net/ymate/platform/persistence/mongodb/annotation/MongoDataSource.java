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
package net.ymate.platform.persistence.mongodb.annotation;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.persistence.mongodb.IMongoClientOptionsHandler;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/10 22:08
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(MongoConf.class)
public @interface MongoDataSource {

    /**
     * @return 数据源名称
     */
    String name();

    /**
     * @return 数据库访问用户名称
     */
    String username() default StringUtils.EMPTY;

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
     * @return 集合前缀名称
     */
    String collectionPrefix() default StringUtils.EMPTY;

    /**
     * @return 数据库名称
     */
    String databaseName();

    /**
     * @return 包含用户身份验证数据的数据库名称
     */
    String authenticationDatabaseName() default StringUtils.EMPTY;

    /**
     * @return 服务器主机连接字符串
     */
    String connectionUrl() default StringUtils.EMPTY;

    /**
     * @return 服务器主机集合
     */
    String[] servers() default {};

    /**
     * @return 数据源自定义配置处理器
     */
    Class<? extends IMongoClientOptionsHandler> optionsHandlerClass() default IMongoClientOptionsHandler.class;
}
