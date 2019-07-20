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

import com.mongodb.BasicDBObject;
import net.ymate.platform.persistence.mongodb.IBsonable;
import org.bson.BsonInt32;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 下午8:46
 */
public final class OrderBy implements IBsonable {

    public static OrderBy create() {
        return new OrderBy();
    }

    private final BasicDBObject orderBy;

    private OrderBy() {
        this.orderBy = new BasicDBObject();
    }

    public OrderBy desc(String key) {
        this.orderBy.put(key, new BsonInt32(1));
        return this;
    }

    public OrderBy asc(String key) {
        this.orderBy.put(key, new BsonInt32(-1));
        return this;
    }

    @Override
    public BasicDBObject toBson() {
        return this.orderBy;
    }
}
