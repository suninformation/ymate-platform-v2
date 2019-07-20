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
package net.ymate.platform.persistence.mongodb.impl;

import net.ymate.platform.core.persistence.AbstractPersistenceConfigurable;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoClientOptionsHandler;
import net.ymate.platform.persistence.mongodb.IMongoConfig;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceConfigurable;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 04:16
 * @since 2.1.0
 */
public final class DefaultMongoConfigurable extends AbstractPersistenceConfigurable<IMongoDataSourceConfigurable> {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultMongoConfigurable() {
        super(IMongo.MODULE_NAME);
    }

    public static final class Builder extends AbstractBuilder<Builder, DefaultMongoConfigurable, IMongoDataSourceConfigurable> {

        private Builder() {
            super(new DefaultMongoConfigurable());
        }

        public Builder dataSourceOptionsHandlerClass(Class<? extends IMongoClientOptionsHandler> dataSourceOptionsHandlerClass) {
            configurable.addConfig(IMongoConfig.DS_OPTIONS_HANDLER_CLASS, dataSourceOptionsHandlerClass.getName());
            return this;
        }
    }
}
