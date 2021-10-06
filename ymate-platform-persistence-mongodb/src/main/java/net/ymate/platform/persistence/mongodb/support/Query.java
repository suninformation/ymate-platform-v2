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
package net.ymate.platform.persistence.mongodb.support;

import net.ymate.platform.persistence.mongodb.IOperator;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/23 下午3:45
 */
public final class Query extends BaseOperator<Query> {

    public static Query create() {
        return new Query();
    }

    public static Query create(String key, IOperator operator) {
        Query query = new Query();
        return query.cond(key, operator);
    }

    private Query() {
    }

    public Query cond(String key, IOperator operator) {
        putOperator(key, operator.toBson());
        return this;
    }
}
