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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import net.ymate.platform.core.persistence.Params;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/27 下午6:04
 */
public abstract class AbstractOperator implements IOperator {

    private final BasicDBObject operation = new BasicDBObject();

    protected void addOperator(String opt, Object param) {
        if (param instanceof Params) {
            operation.put(opt, ((Params) param).toArray());
        } else {
            operation.put(opt, param);
        }
    }

    protected void putOperator(String opt, String field, Object value) {
        DBObject dbObject = (DBObject) operation.get(opt);
        if (dbObject == null) {
            dbObject = new BasicDBObject(field, value);
            addOperator(opt, dbObject);
        } else {
            dbObject.put(field, value);
        }
    }

    protected void putOperator(String opt, Map<?, ?> object) {
        DBObject dbObject = (DBObject) operation.get(opt);
        if (dbObject == null) {
            dbObject = new BasicDBObject();
            dbObject.putAll(object);
            addOperator(opt, dbObject);
        } else {
            dbObject.putAll(object);
        }
    }

    @Override
    public void add(IOperator operator) {
        operation.putAll((Map<?, ?>) operator.toBson());
    }

    @Override
    public BasicDBObject toBson() {
        return operation;
    }
}
