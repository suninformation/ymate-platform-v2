/*
 * Copyright 2007-2017 the original author or authors.
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

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/30 上午3:18
 * @version 1.0
 */
public interface IRedisModuleCfg {

    String DS_DEFAULT_NAME = "ds_default_name";

    String DS_NAME_LIST = "ds_name_list";

    String CONNECTION_TYPE = "connection_type";

    String MASTER_SERVER_NAME = "master_server_name";

    String SERVER_NAME_LIST = "server_name_list";

    String HOST = "host";

    String PORT = "host";

    String CLIENT_NAME = "client_name";

    String TIMEOUT = "timeout";

    String SOCKET_TIMEOUT = "socket_timeout";

    String MAX_ATTEMPTS = "max_attempts";

    String WEIGHT = "weight";

    String PASSWORD = "password";

    String PASSWORD_ENCRYPTED = "password_encrypted";

    String PASSWORD_CLASS = "password_class";

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

    String getDataSourceDefaultName();

    Map<String, RedisDataSourceCfgMeta> getDataSourceCfgs();

    RedisDataSourceCfgMeta getDefaultDataSourceCfg();

    RedisDataSourceCfgMeta getDataSourceCfg(String name);

    class ServerMeta {
        private String name;
        private String host;
        private int port;
        private int timeout;
        private int socketTimeout;
        private int maxAttempts;
        private int weight;
        private int database;
        private String clientName;
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getSocketTimeout() {
            return socketTimeout;
        }

        public void setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
