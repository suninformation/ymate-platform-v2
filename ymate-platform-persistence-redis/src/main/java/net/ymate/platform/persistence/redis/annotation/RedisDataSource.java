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
package net.ymate.platform.persistence.redis.annotation;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.persistence.redis.IRedis;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/10 23:59
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisDataSource {

    /**
     * @return 数据源名称
     */
    String name();

    /**
     * @return 数据源连接方式
     */
    IRedis.ConnectionType connectionType() default IRedis.ConnectionType.DEFAULT;

    /**
     * @return 主服务端名称
     */
    String masterServerName() default StringUtils.EMPTY;

    /**
     * @return Redis服务端集合
     */
    RedisServer[] servers();

    /**
     * @return 身份认证密码是否已加密
     */
    boolean passwordEncrypted() default false;

    /**
     * @return 密码处理器
     */
    Class<? extends IPasswordProcessor> passwordClass() default IPasswordProcessor.class;

    /**
     * @return 连接池--最小空闲连接数
     */
    int poolMinIdle() default GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

    /**
     * @return 连接池--最大空闲连接数
     */
    int poolMaxIdle() default GenericObjectPoolConfig.DEFAULT_MAX_IDLE;

    /**
     * @return 连接池--最大连接数
     */
    int poolMaxTotal() default GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;

    /**
     * @return 连接池--连接耗尽时是否阻塞
     */
    boolean poolBlockWhenExhausted() default GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;

    boolean poolFairness() default GenericObjectPoolConfig.DEFAULT_FAIRNESS;

    /**
     * @return 连接池--是否启用JMX管理功能
     */
    boolean poolJmxEnabled() default GenericObjectPoolConfig.DEFAULT_JMX_ENABLE;

    String poolJmxNameBase() default StringUtils.EMPTY;

    String poolJmxNamePrefix() default GenericObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX;

    /**
     * @return 连接池--设置逐出策略类名
     */
    String poolEvictionPolicyClassName() default StringUtils.EMPTY;

    /**
     * @return 连接池--是否启用后进先出
     */
    boolean poolLifo() default GenericObjectPoolConfig.DEFAULT_LIFO;

    /**
     * @return 连接池--获取连接时的最大等待毫秒数
     */
    long poolMaxWaitMillis() default GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;

    long poolMinEvictableIdleTimeMillis() default GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    /**
     * @return 连接池--对象空闲多久后逐出, 当空闲时间>该值且空闲连接>最大空闲数时直接逐出
     */
    long poolSoftMinEvictableIdleTimeMillis() default GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    /**
     * @return 连接池--在获取连接的时候检查有效性
     */
    boolean poolTestOnBorrow() default GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW;

    /**
     * @return 连接池--在归还到池中前进行检验
     */
    boolean poolTestOnReturn() default GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN;

    boolean poolTestOnCreate() default GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE;

    /**
     * @return 连接池--在空闲时检查有效性
     */
    boolean poolTestWhileIdle() default GenericObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE;

    /**
     * @return 连接池--每次逐出检查时逐出的最大数目
     */
    int poolNumTestsPerEvictionRun() default GenericObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;

    /**
     * @return 连接池--逐出扫描的时间间隔(毫秒)
     */
    long poolTimeBetweenEvictionRunsMillis() default GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
}
