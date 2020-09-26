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
package net.ymate.platform.persistence.redis;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IPersistenceConfig;

/**
 * Redis配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2019-05-22 02:42
 */
@Ignored
public interface IRedisConfig extends IPersistenceConfig<IRedis, IRedisDataSourceConfig> {

    String CONNECTION_TYPE = "connection_type";

    String MASTER_SERVER_NAME = "master_server_name";

    String SERVER_NAME_LIST = "server_name_list";

    String HOST = "host";

    String PORT = "port";

    String CLIENT_NAME = "client_name";

    String TIMEOUT = "timeout";

    String SOCKET_TIMEOUT = "socket_timeout";

    String MAX_ATTEMPTS = "max_attempts";

    String WEIGHT = "weight";

    String DATABASE = "database";

    String MIN_IDLE = "min_idle";

    String MAX_IDLE = "max_idle";

    String MAX_TOTAL = "max_total";

    String BLOCK_WHEN_EXHAUSTED = "block_when_exhausted";

    String FAIRNESS = "fairness";

    String JMX_ENABLE = "jmx_enabled";

    String JMX_NAME_BASE = "jmx_name_base";

    String JMX_NAME_PREFIX = "jmx_name_prefix";

    String EVICTION_POLICY_CLASS_NAME = "eviction_policy_class_name";

    String LIFO = "lifo";

    String MAX_WAIT_MILLIS = "max_wait_millis";

    String MIN_EVICTABLE_IDLE_TIME_MILLIS = "min_evictable_idle_time_millis";

    String SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS = "soft_min_evictable_idle_time_millis";

    String TEST_ON_BORROW = "test_on_borrow";

    String TEST_ON_RETURN = "test_on_return";

    String TEST_ON_CREATE = "test_on_create";

    String TEST_WHILE_IDLE = "test_while_idle";

    String NUM_TESTS_PER_EVICTION_RUN = "num_tests_per_eviction_run";

    String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "time_between_eviction_runs_millis";
}
