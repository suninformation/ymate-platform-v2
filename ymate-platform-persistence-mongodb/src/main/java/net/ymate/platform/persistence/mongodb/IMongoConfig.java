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
package net.ymate.platform.persistence.mongodb;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IPersistenceConfig;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/21 上午10:00
 */
@Ignored
public interface IMongoConfig extends IPersistenceConfig<IMongo, IMongoDataSourceConfig> {

    String DS_OPTIONS_HANDLER_CLASS = "ds_options_handler_class";

    String CONNECTION_URL = "connection_url";

    String COLLECTION_PREFIX = "collection_prefix";

    String DATABASE_NAME = "database_name";

    String AUTHENTICATION_DATABASE_NAME = "authentication_database_name";

    String SERVERS = "servers";
}
